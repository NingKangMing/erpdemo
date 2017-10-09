package com.aioerp.controller.init;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Unit;
import com.aioerp.model.init.FinanceInitRecords;
import com.aioerp.model.stock.StockInit;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinanceInitController
  extends BaseController
{
  public void index()
  {
    String m = getPara("m");
    String configName = loginConfigName();
    List<Model> list = FinanceInitRecords.dao.getInitList(configName, m);
    if (StringUtil.isNull(m))
    {
      BigDecimal sckMoney = StockInit.dao.getMoneyTotal(configName);
      Map<String, BigDecimal> map = Unit.dao.getInitArap(configName);
      BigDecimal getMoney = (BigDecimal)map.get("getMoney");
      BigDecimal payMoney = (BigDecimal)map.get("payMoney");
      
      setAttr("sckMoney", sckMoney == null ? Integer.valueOf(0) : sckMoney);
      setAttr("getMoney", getMoney == null ? Integer.valueOf(0) : getMoney);
      setAttr("payMoney", payMoney == null ? Integer.valueOf(0) : payMoney);
    }
    String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
    if (AioConstants.HAS_OPEN_ACCOUNT1.equals(hasOpenAccount)) {
      setAttr("hasOpen", "yes");
    } else {
      setAttr("hasOpen", "no");
    }
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    setAttr("pattern", m);
    setAttr("selectedObjectId", Integer.valueOf(selectedObjectId));
    setAttr("list", list);
    render("page.html");
  }
  
  public void editInit()
  {
    String configName = loginConfigName();
    Boolean isPost = Boolean.valueOf(getParaToBoolean("isPost") == null ? false : getParaToBoolean("isPost").booleanValue());
    if (isPost.booleanValue())
    {
      try
      {
        String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
        if (AioConstants.HAS_OPEN_ACCOUNT1.equals(hasOpenAccount))
        {
          this.result.put("message", "已经开账不能修改期初");
          this.result.put("statusCode", AioConstants.HTTP_RETURN300);
          renderJson(this.result);
          return;
        }
        Model model = (Model)getModel(Accounts.class, "account");
        model.update(configName);
        this.result.put("statusCode", AioConstants.HTTP_RETURN200);
        this.result.put("callbackType", "closeCurrent");
        this.result.put("rel", "financeInitPage");
        this.result.put("selectedObjectId", model.getInt("id"));
      }
      catch (Exception e)
      {
        e.printStackTrace();
        this.result.put("message", "系统异常");
        this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      }
      renderJson(this.result);
    }
    else
    {
      Integer accountId = getParaToInt("selectedObjectId");
      Accounts account = (Accounts)Accounts.dao.findById(configName, accountId);
      setAttr("account", account);
      render("editInit.html");
    }
  }
  
  public void print()
  {
    String configName = loginConfigName();
    String m = getPara("m");
    
    List<Model> list = FinanceInitRecords.dao.getInitList(configName, m);
    
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    
    BigDecimal sckMoney = BigDecimal.ZERO;
    Object getMoney = Integer.valueOf(0);
    Object payMoney = Integer.valueOf(0);
    if (StringUtil.isNull(m))
    {
      data.put("reportName", "期初财务");
      sckMoney = StockInit.dao.getMoneyTotal(configName);
      Map<String, BigDecimal> map = Unit.dao.getInitArap(configName);
      getMoney = map.get("getMoney") == null ? Integer.valueOf(0) : (Number)map.get("getMoney");
      payMoney = map.get("payMoney") == null ? Integer.valueOf(0) : (Number)map.get("payMoney");
    }
    else if ("bank".equals(m))
    {
      data.put("reportName", "期初现金银行");
    }
    else if ("assets".equals(m))
    {
      data.put("reportName", "期初固定资产");
    }
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    

    colTitle.add("行号");
    colTitle.add("科目编号");
    colTitle.add("科目全名");
    colTitle.add("期初金额");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    List<String> rowData = new ArrayList();
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < list.size(); i++)
    {
      rowData.add(trimNull(i + 1));
      Model account = (Model)list.get(i);
      rowData.add(trimNull(account.get("code")));
      rowData.add(trimNull(account.getStr("fullName")));
      String type = account.getStr("type");
      BigDecimal moneys = account.getBigDecimal("moneys");
      if ("000412".equals(type))
      {
        rowData.add(trimNull(sckMoney));
      }
      else if ("000413".equals(type))
      {
        rowData.add(trimNull(getMoney));
      }
      else if ("00013".equals(type))
      {
        rowData.add(trimNull(payMoney));
      }
      else if ("0001".equals(type))
      {
        BigDecimal bGetMoney = new BigDecimal(getMoney.toString());
        rowData.add(trimNull(BigDecimalUtils.add(BigDecimalUtils.add(moneys, sckMoney), bGetMoney)));
      }
      else if ("0007".equals(type))
      {
        BigDecimal bPayMoney = new BigDecimal(payMoney.toString());
        rowData.add(trimNull(BigDecimalUtils.add(moneys, bPayMoney)));
      }
      else
      {
        rowData.add(trimNull(moneys));
      }
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
