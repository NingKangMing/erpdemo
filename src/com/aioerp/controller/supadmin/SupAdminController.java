package com.aioerp.controller.supadmin;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.supadmin.WhichDb;
import com.aioerp.model.sys.SysUserLog;
import com.aioerp.util.IP;
import com.aioerp.util.MD5Util;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.util.List;
import java.util.Map;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

public class SupAdminController
  extends BaseController
{
  public void index()
  {
    render("/WEB-INF/template/supIndex.html");
  }
  
  public void logout()
  {
    removeSessionAttr("user");
    removeSessionAttr("userID");
    removeCookie("aioerpID");
    redirect("/");
  }
  
  public void exit()
  {
    removeSessionAttr("user");
    removeSessionAttr("userID");
    removeCookie("aioerpID");
    renderJson();
  }
  
  public void supAdminLogin()
  {
    String username = getPara("username");
    String password = getPara("password");
    password = MD5Util.string2MD5(password);
    Record superUser = Db.use(AioConstants.CONFIG_NAME).findFirst("select * from sys_user where username='" + username + "' and password='" + password + "' and grade=" + 3);
    if (superUser == null)
    {
      setAttr("statusCode", "300");
      setAttr("message", "对不起，用户名或密码不正确！");
      renderJson();
      return;
    }
    superUser.set("souPing", "false");
    setSessionAttr("user", superUser);
    setSessionAttr("loginConfigId", AioConstants.CONFIG_NAME);
    
    setAttr("statusCode", "200");
    setAttr("callbackType", "forward");
    setAttr("url", "/supAdmin/user");
    AioConstants.PROJECT_SESSION_MAP.put(SecurityUtils.getSubject().getSession().getId(), AioConstants.CONFIG_NAME);
    if (AioConstants.ISOPENUSERLOG) {
      SysUserLog.addUserLog(AioConstants.CONFIG_NAME, username, IP.getIpAddr(getRequest()), "超级管理员登录");
    }
    renderJson();
  }
  
  public void checkWhickDb()
  {
    String username = getPara("username");
    String password = getPara("password");
    password = MD5Util.string2MD5(password);
    

    int totalOkUser = 0;
    String whichDbIds = "";
    
    List<Record> list = WhichDb.dao.getDbList();
    if ((list == null) || (list.size() == 0))
    {
      this.result.put("statusCode", "300");
      this.result.put("message", "对不起，用户名或密码不正确");
      renderJson(this.result);
      return;
    }
    for (int i = 0; i < list.size(); i++)
    {
      int dbCount = WhichDb.dao.getTruePwdListSize(String.valueOf(((Record)list.get(i)).getInt("id")), username, password);
      if (dbCount != 0)
      {
        totalOkUser++;
        whichDbIds = whichDbIds + ((Record)list.get(i)).getInt("id") + ",";
      }
    }
    if (!whichDbIds.equals("")) {
      whichDbIds = whichDbIds.substring(0, whichDbIds.length() - 1);
    }
    if (totalOkUser == 0)
    {
      this.result.put("statusCode", "300");
      this.result.put("message", "对不起，用户名或密码不正确");
      renderJson(this.result);
      return;
    }
    if (totalOkUser == 1)
    {
      this.result.put("statusCode", "200");
      this.result.put("whichDbIds", whichDbIds);
      this.result.put("callbackType", "submit");
      this.result.put("rel", "userLogin");
      this.result.put("url", "/user/login");
      renderJson(this.result);
      return;
    }
    this.result.put("statusCode", "200");
    this.result.put("callbackType", "dialog");
    this.result.put("whichDbIds", whichDbIds);
    renderJson(this.result);
  }
  
  public void toWhickDbList()
  {
    String dbIdStrs = getPara("dbIdStrs");
    List<Model> list = WhichDb.dao.getEnableList(dbIdStrs);
    setAttr("list", list);
    render("/WEB-INF/template/user/selectWhichDbDialog.html");
  }
}
