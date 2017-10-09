package com.aioerp.controller;

import com.aioerp.common.AioConstants;
import com.aioerp.db.reports.BillHistory;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.SysUserLog;
import com.aioerp.util.IP;
import com.aioerp.util.MD5Util;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

public class UserController
  extends BaseController
{
  public void logout()
    throws SQLException
  {
    getSession().invalidate();
    redirect("/");
  }
  
  public void exit()
    throws SQLException
  {
    getSession().invalidate();
    





    renderJson();
  }
  
  @Before({Tx.class})
  public void login()
  {
    if (isPost())
    {
      int flag = setSessionLoginUser();
      if (flag == 1)
      {
        this.result.put("statusCode", "200");
        this.result.put("callbackType", "forward");
        this.result.put("url", "/");
        renderJson(this.result);
        if (AioConstants.ISOPENUSERLOG) {
          SysUserLog.addUserLog(getParaToInt("whichDbId", Integer.valueOf(-1)).toString(), getPara("username"), IP.getIpAddr(getRequest()), "用户登录");
        }
        return;
      }
      if (flag == 0)
      {
        this.result.put("statusCode", "300");
        this.result.put("message", "用户名或密码不正确！");
        renderJson(this.result);
        return;
      }
      if (flag == -1)
      {
        this.result.put("statusCode", "300");
        this.result.put("message", "系统参数异常！");
        renderJson(this.result);
        return;
      }
      if (flag == -2)
      {
        this.result.put("statusCode", "300");
        this.result.put("message", "系统升级异常！");
        renderJson(this.result);
      }
    }
    else
    {
      render("/WEB-INF/template/user/login.html");
    }
  }
  
  public int setSessionLoginUser()
  {
    int flag = 1;
    int whichDbId = getParaToInt("whichDbId", Integer.valueOf(-1)).intValue();
    String username = getPara("username");
    String password = getPara("password");
    password = MD5Util.string2MD5(password);
    Record user = Db.use(String.valueOf(whichDbId)).findFirst("select * from sys_user where username=? and password=? and status = ?", new Object[] { username, password, Integer.valueOf(AioConstants.STATUS_ENABLE) });
    if (user == null) {
      return 0;
    }
    String lastDBUpdateTime = AioerpSys.dao.getValue1(String.valueOf(whichDbId), "lastDBUpdateTime");
    if ((!StringUtil.isNull(lastDBUpdateTime)) && (!"20150611".equals(lastDBUpdateTime)) && (AioConstants.PROJECT_IS_OK)) {
      return -2;
    }
    Subject currentUser = SecurityUtils.getSubject();
    UsernamePasswordToken token = new UsernamePasswordToken(username, password);
    try
    {
      setSessionAttr("whichDbId", Integer.valueOf(whichDbId));
      currentUser.login(token);
      
      String hasOpenAccount = AioerpSys.dao.getValue1(String.valueOf(whichDbId), "hasOpenAccount");
      
      user.set("hasOpenAccount", hasOpenAccount);
      user.set("loginConfigId", String.valueOf(whichDbId));
      Record accountDataBase = Db.use(AioConstants.CONFIG_NAME).findFirst("select * from aioerp_which_db where id=" + whichDbId);
      user.set("loginConfigName", accountDataBase.getStr("whichDbName"));
      user.set("loginConfigCreatTime", accountDataBase.getDate("create_time"));
      user.set("souPing", "false");
      setSessionAttr("user", user);
      setSessionAttr("lastOperTime", new Date());
      
      setSessionAttr("loginConfigId", String.valueOf(whichDbId));
      

      int billCount = BillHistory.dao.getStartToEndDateHistoryBillCount(String.valueOf(whichDbId), null, null);
      AioConstants.accountBillCount.put(String.valueOf(whichDbId), Integer.valueOf(billCount));
      

      AioConstants.PROJECT_SESSION_MAP.put(SecurityUtils.getSubject().getSession().getId(), String.valueOf(whichDbId));
    }
    catch (Exception e)
    {
      flag = -1;
    }
    return flag;
  }
  
  public void toSouPing()
  {
    Record r = (Record)getSessionAttr("user");
    r.set("souPing", "true");
    setSessionAttr("user", r);
    setAttr("username", r.getStr("username"));
    setAttr("whichDbId", r.getStr("loginConfigId"));
    render("/WEB-INF/template/user/souPingDialog.html");
  }
  
  public void souPing()
  {
    int flag = setSessionLoginUser();
    if (flag == 1)
    {
      this.result.put("statusCode", "200");
      this.result.put("callbackType", "closeCurrent");
      this.result.put("isSP", Boolean.valueOf(false));
      renderJson(this.result);
      return;
    }
    if (flag == 0)
    {
      this.result.put("statusCode", "300");
      this.result.put("message", "用户名或密码不正确！");
      renderJson(this.result);
      return;
    }
    if (flag == -1)
    {
      this.result.put("statusCode", "300");
      this.result.put("message", "系统参数异常！");
      renderJson(this.result);
      return;
    }
    if (flag == -2)
    {
      this.result.put("statusCode", "300");
      this.result.put("message", "系统升级异常！");
      renderJson(this.result);
      return;
    }
  }
  
  public void toEditPwd()
  {
    render("/WEB-INF/template/user/editPwdDialog.html");
  }
  
  public void editPwd()
  {
    String configName = loginConfigName();
    try
    {
      String oldPwd = getPara("oldPwd", "");
      String newPwd = getPara("newPwd", "");
      String rePwd = getPara("rePwd", "");
      if (!newPwd.equals(rePwd))
      {
        setAttr("statusCode", "300");
        setAttr("message", "两次密码输入不一致");
        renderJson();
        return;
      }
      oldPwd = MD5Util.string2MD5(oldPwd);
      newPwd = MD5Util.string2MD5(newPwd);
      
      Record r = (Record)getSessionAttr("user");
      String username = r.getStr("username");
      









      Record loginUser = Db.use(configName).findFirst("select * from sys_user where username=?", new Object[] { username });
      if ((loginUser == null) || (!loginUser.getStr("password").equals(oldPwd)))
      {
        setAttr("statusCode", "300");
        setAttr("message", "旧密码不正确");
        renderJson();
        return;
      }
      loginUser.set("password", newPwd);
      Db.use(configName).update("sys_user", loginUser);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      setAttr("statusCode", "300");
      setAttr("message", "旧密码不正确");
      renderJson();
      return;
    }
    setAttr("statusCode", "200");
    setAttr("callbackType", "closeCurrent");
    setAttr("message", "修改成功");
    renderJson();
  }
}
