package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.model.sys.SysUser;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SysConfigController
  extends BaseController
{
  public void index()
  {
    String configName = loginConfigName();
    SysUser sysUser = (SysUser)SysUser.dao.findById(configName, Integer.valueOf(1));
    List<Model> configList = SysConfig.dao.getList(configName);
    
    ComFunController.freeVesionRemoveSysConfigAttr(configList);
    

    Record setObj = AioerpSys.dao.getObj(configName, "autoSPTime");
    if (setObj == null) {
      setObj = AioerpSys.dao.sysSaveOrUpdate(configName, "autoSPTime", String.valueOf(1000), "自动锁屏时间(分钟)");
    }
    setAttr("autoSPTime", setObj.getStr("value1"));
    
    setAttr("sysUser", sysUser);
    setAttr("configList", configList);
    render("dialog.html");
  }
  
  public void check()
  {
    String configName = loginConfigName();
    Integer checkId = getParaToInt("checkId");
    Integer val = getParaToInt("val");
    SysConfig.dao.configUpdate(configName, checkId.intValue(), val.intValue());
    renderJson();
  }
  
  @Before({Tx.class})
  public void save()
    throws SQLException
  {
    String configName = loginConfigName();
    try
    {
      SysUser sysUser = (SysUser)getModel(SysUser.class);
      sysUser.update(configName);
      
      String autoSPTime = getPara("autoSPTime", String.valueOf(1000));
      if (autoSPTime.equals("0"))
      {
        commonRollBack();
        this.result.put("message", "自动锁屏时间必须大于0");
        this.result.put("statusCode", AioConstants.HTTP_RETURN300);
        renderJson(this.result);
        return;
      }
      AioerpSys.dao.sysSaveOrUpdate(configName, "autoSPTime", autoSPTime, null);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      this.result.put("message", "操作失败！");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    this.result.put("callbackType", "closeCurrent");
    this.result.put("statusCode", AioConstants.HTTP_RETURN200);
    renderJson(this.result);
  }
}
