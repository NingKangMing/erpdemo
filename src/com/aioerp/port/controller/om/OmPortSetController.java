package com.aioerp.port.controller.om;

import com.aioerp.controller.BaseController;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.port.common.PortConstants;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.util.Map;
import org.json.JSONObject;

public class OmPortSetController
  extends BaseController
{
  public void index()
  {
    String configName = loginConfigName();
    Record r = Db.use(configName).findFirst("select * from aioerp_sys where key1='omDomainName'");
    if (r != null) {
      setAttr("omDomainName", r.getStr("value1"));
    }
    render("/WEB-INF/template/port/set/dialog.html");
  }
  
  public void edit()
  {
    String configName = loginConfigName();
    String omDomainName = getPara("omDomainName", "");
    String omDomainNamePwd = getPara("omDomainNamePwd", "");
    if ((omDomainName.equals("")) && (omDomainNamePwd.equals("")))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "域名与OM管理员密码不能都为空");
      renderJson(this.result);
      return;
    }
    String message = "";
    if (omDomainName.endsWith("/")) {
      omDomainName = omDomainName.substring(0, omDomainName.length() - 1);
    }
    if (!omDomainName.equals(""))
    {
      Db.use(configName).findFirst("select * from aioerp_sys where key1='omDomainName'");
      AioerpSys.dao.sysSaveOrUpdate(configName, "omDomainName", omDomainName, null);
      message = "接口域名修改成功";
    }
    if (!omDomainNamePwd.equals(""))
    {
      try
      {
        JSONObject j = OmPortController.portDominaNamePwd(omDomainName, omDomainNamePwd);
        if (!PortConstants.RET0.equals(j.get("ret")))
        {
          commonRollBack();
          renderJson(Boolean.valueOf(false));
          return;
        }
      }
      catch (Exception e)
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "连接超时或OM域名配置不正确！");
        renderJson(this.result);
        return;
      }
      message = "OM配置密码修改成功";
    }
    if ((!omDomainName.equals("")) && (!omDomainNamePwd.equals(""))) {
      message = "修改成功";
    }
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("message", message);
    renderJson(this.result);
  }
}
