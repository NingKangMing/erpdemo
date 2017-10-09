package com.aioerp.controller.init;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Unit;
import com.aioerp.model.stock.StockInit;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class MakeInitContriller
  extends BaseController
{
  public void index()
  {
    setAttr("today", DateUtils.getCurrentDate());
    String hasOpen = AioerpSys.dao.getValue1(loginConfigName(), "hasOpenAccount");
    String hasOpenAccount = "close";
    if (AioConstants.HAS_OPEN_ACCOUNT1.equals(hasOpen))
    {
      hasOpenAccount = "open";
      setAttr("hasOpenAccountTime", AioerpSys.dao.getValue1(loginConfigName(), "hasOpenAccountTime"));
    }
    setAttr("hasOpenAccount", hasOpenAccount);
    render("page.html");
  }
  
  @Before({Tx.class})
  public void make()
    throws SQLException
  {
    try
    {
      Boolean isOption = getParaToBoolean("isOption", Boolean.valueOf(true));
      String configName = loginConfigName();
      Model sszb = Accounts.dao.getModelFirst(configName, "00045");
      Model wfplr = Accounts.dao.getModelFirst(configName, "000420");
      BigDecimal qyhj = BigDecimalUtils.add(sszb.getBigDecimal("money"), wfplr.getBigDecimal("money"));
      
      Map<String, BigDecimal> map = Unit.dao.getInitArap(configName);
      BigDecimal zcje = Accounts.dao.getMoneysByType(configName, "0001");
      BigDecimal sckMoney = StockInit.dao.getMoneyTotal(configName);
      BigDecimal getMoney = (BigDecimal)map.get("getMoney");
      zcje = BigDecimalUtils.add(zcje, sckMoney);
      zcje = BigDecimalUtils.add(zcje, getMoney);
      
      BigDecimal fzje = Accounts.dao.getMoneysByType(configName, "0007");
      BigDecimal payMoney = (BigDecimal)map.get("payMoney");
      fzje = BigDecimalUtils.add(fzje, payMoney);
      
      int baseVersion = 1;
      if ((AioConstants.VERSION > baseVersion) && 
        (isOption.booleanValue()) && (BigDecimalUtils.compare(zcje, BigDecimalUtils.add(qyhj, fzje)) != 0))
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN301);
        setAttr("confirmMsg", "资产不等于负债加所有权益，是否将差额计入实收资本并开账？");
        renderJson();
        return;
      }
      if (BigDecimalUtils.compare(zcje, BigDecimalUtils.add(qyhj, fzje)) != 0)
      {
        BigDecimal usszb = BigDecimalUtils.sub(zcje, fzje);
        usszb = BigDecimalUtils.sub(usszb, wfplr.getBigDecimal("money"));
        sszb.set("money", usszb);
        sszb.update(configName);
      }
      Record aioerpSys = AioerpSys.dao.getObj(configName, "hasOpenAccount");
      if ((aioerpSys != null) && ("1".equals(aioerpSys.getStr("value1"))))
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        setAttr("message", "期初已开账！");
        renderJson();
        return;
      }
      AioerpSys.dao.sysSaveOrUpdate(configName, "hasOpenAccount", AioConstants.HAS_OPEN_ACCOUNT1, "0未开账   1开账");
      

      String hasOpenAccountTime = getPara("hasOpenAccountTime", DateUtils.format(new Date(), "yyyy-MM-dd"));
      
      AioerpSys.dao.sysSaveOrUpdate(configName, "hasOpenAccountTime", hasOpenAccountTime, "开账日期");
      


      StockInit.dao.openInitDateMove(configName, hasOpenAccountTime, null);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "系统异常，请联系管理员");
      renderJson();
      return;
    }
    setAttr("statusCode", "200");
    setAttr("message", "开账成功");
    setAttr("isCloseType", "init");
    renderJson();
  }
  
  @Before({Tx.class})
  public void invertMake()
    throws SQLException
  {
    String configName = loginConfigName();
    Boolean falg = AioerpSys.dao.verifyInvertMake(configName);
    if (falg.booleanValue())
    {
      AioerpSys.dao.sysDelKey(configName, "hasOpenAccountTime");
      AioerpSys.dao.getObj(configName, "hasOpenAccount");
      AioerpSys.dao.sysSaveOrUpdate(configName, "hasOpenAccount", AioConstants.HAS_OPEN_ACCOUNT0, "是否开账");
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("message", "反开账成功！");
      setAttr("isCloseType", "init");
      
      AioerpSys.dao.sysDelKey(configName, "hasOpenAccountTime");
      BaseDbModel.delAll(configName, "cw_accounts_init");
      BaseDbModel.delAll(configName, "cc_stock");
      BaseDbModel.delAll(configName, "zj_product_avgprice");
      Unit.dao.invertOpenAccountUpdateUnitGetOrPay(configName);
    }
    else
    {
      setAttr("statusCode", "300");
      setAttr("message", "已经有过账单据，不能回到期初！");
    }
    renderJson();
  }
}
