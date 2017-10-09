package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillCodeFlow;
import com.aioerp.model.sys.ReSystem;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.DateUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class ReSystemController
  extends BaseController
{
  public void index()
  {
    render("dialog.html");
  }
  
  @Before({Tx.class})
  public void rebuild()
    throws SQLException
  {
    Integer rmvBase = getParaToInt("removeBase", Integer.valueOf(0));
    Integer rmvInit = getParaToInt("removeInit", Integer.valueOf(0));
    Integer rmvDraft = getParaToInt("removeDraft", Integer.valueOf(0));
    Integer rmvOrder = getParaToInt("removeOrder", Integer.valueOf(0));
    String configName = loginConfigName();
    if (AioConstants.IS_FREE_VERSION == "yes")
    {
      rmvDraft = Integer.valueOf(1);
      rmvOrder = Integer.valueOf(1);
    }
    if ((rmvBase != null) && (rmvBase.intValue() > 0) && (
      (rmvInit.intValue() <= 0) || (rmvDraft.intValue() <= 0) || (rmvOrder.intValue() <= 0)))
    {
      this.result.put("message", "清除基本信息必须勾选全部！");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    try
    {
      AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS3));
      

      this.result = BackUpController.commonBackUp(configName, Integer.valueOf(loginUserId()), loginAccountName(), "系统重建" + loginAccountName() + DateUtils.format(new Date(), "yyyy-MM-ddHHmmss"));
      if (Integer.valueOf(this.result.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(this.result);
        AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
        return;
      }
      YearEnd.dao.removeOneYearEnd(configName);
      

      BillCodeFlow.dao.batchDelZNum(configName);
      

      Db.use(configName).update("UPDATE b_unit SET totalGet=NULL,totalPay=NULL,totalPreGet=NULL,totalPrePay=NULL");
      if ((rmvBase != null) && (rmvBase.intValue() > 0)) {
        ReSystem.delTables(configName, "base");
      }
      if ((rmvInit != null) && (rmvInit.intValue() > 0)) {
        ReSystem.delTables(configName, "init");
      }
      if ((rmvDraft != null) && (rmvDraft.intValue() > 0)) {
        ReSystem.delTables(configName, "draft");
      }
      if ((rmvOrder != null) && (rmvOrder.intValue() > 0)) {
        ReSystem.delTables(configName, "order");
      }
      AioerpSys.dao.getObj(configName, "hasOpenAccount");
      AioerpSys.dao.sysSaveOrUpdate(configName, "hasOpenAccount", AioConstants.HAS_OPEN_ACCOUNT0, "是否开账");
      

      AioerpSys.dao.sysDelKey(configName, "hasOpenAccountTime");
      if (rmvOrder.intValue() != 1) {
        ReSystem.resetBookBillTables(configName);
      }
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
    finally
    {
      AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
    }
    AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
    

    this.result.put("message", "操作成功");
    this.result.put("callbackType", "closeAll");
    this.result.put("statusCode", AioConstants.HTTP_RETURN200);
    renderJson(this.result);
  }
}
