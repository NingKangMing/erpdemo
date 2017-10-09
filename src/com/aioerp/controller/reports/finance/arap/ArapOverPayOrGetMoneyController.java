package com.aioerp.controller.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.bought.PurchaseBill;
import com.aioerp.model.reports.finance.arap.ArapOverPayOrGetMoneyReports;
import com.aioerp.model.sell.sell.SellBill;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArapOverPayOrGetMoneyController
  extends BaseController
{
  private static String overGetMoney = "cw_overGetMoney_listID";
  private static String overPayMoney = "cw_overPayMoney_listID";
  
  public void search()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String modelType = getPara(0, "get");
    String type = getPara(1, "first");
    String columnType = "cw517";
    if (modelType.equals("pay")) {
      columnType = "cw518";
    }
    setAttr("pageMap", com(map));
    
    columnConfig(columnType);
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/reports/finance/arap/overGetOrPayMoney/page.html";
    } else if (type.equals("page")) {
      returnPage = "/WEB-INF/template/reports/finance/arap/overGetOrPayMoney/list.html";
    }
    render(returnPage);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    String modelType = getPara(0, "get");
    String type = getPara(1, "first");
    String modelName = "超期应收款";
    String listID = overGetMoney;
    if (modelType.equals("pay"))
    {
      modelName = "超期应付款";
      listID = overPayMoney;
    }
    setAttr("modelName", modelName);
    if (type.equals("first"))
    {
      String orderField = getPara("orderField", "recodeDate");
      map.put("orderField", orderField);
      map.put("warn", "all");
      map.put("unitId", Integer.valueOf(0));
      map.put("stopDate", DateUtils.format(new Date()));
    }
    else
    {
      map.put("warn", getPara("warn"));
      map.put("unitId", getParaToInt("unitId", Integer.valueOf(0)));
      map.put("stopDate", getPara("stopDate", DateUtils.format(new Date())));
    }
    map.put("modelType", modelType);
    

    map.put("listID", listID);
    Map<String, Object> pageMap = ArapOverPayOrGetMoneyReports.dao.arapOverPayOrGetMoney(loginConfigName(), map);
    mapToResponse(map);
    return pageMap;
  }
  
  public void print()
    throws ParseException
  {
    String modelType = getPara("modelType", "get");
    
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    
    String modelName = "付";
    if (modelType.equals("pay")) {
      modelName = "付";
    } else if (modelType.equals("get")) {
      modelName = "收";
    }
    data.put("reportName", "超期应" + modelName + "款");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    List<Record> detailList = new ArrayList();
    

    Map<String, Object> map = new HashMap();
    map.put("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    map.put("numPerPage", getParaToInt("totalCount", Integer.valueOf(9999999)));
    map.put("orderField", getPara("orderField", ORDER_FIELD));
    map.put("orderDirection", getPara("orderDirection", ORDER_DIRECTION));
    if (String.valueOf(map.get("numPerPage")).equals("0")) {
      map.put("numPerPage", Integer.valueOf(1));
    }
    Map<String, Object> pageMap = com(map);
    if (pageMap != null) {
      detailList = (List)pageMap.get("pageList");
    }
    printCommonData(headData);
    

    colTitle.add("行号");
    colTitle.add("单据编号");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("价税合计");
    if (modelType.equals("pay")) {
      colTitle.add("已付金额");
    } else if (modelType.equals("get")) {
      colTitle.add("已收金额");
    }
    colTitle.add("期限日期");
    colTitle.add("延期日期");
    if (modelType.equals("pay")) {
      colTitle.add("未结金额");
    } else if (modelType.equals("get")) {
      colTitle.add("欠款金额");
    }
    colTitle.add("原因");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Record detail = (Record)detailList.get(i);
      String isWarnStr = "";
      if (detail.getInt("isWarn").intValue() == 1) {
        isWarnStr = "取消报警";
      } else {
        isWarnStr = "延迟报警";
      }
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("unitCode")));
      rowData.add(trimNull(detail.getStr("unitFullName")));
      rowData.add(trimNull(detail.getBigDecimal("taxMoneys")));
      rowData.add(trimNull(detail.getBigDecimal("hasMoney")));
      rowData.add(trimNull(detail.getDate("termDate")));
      rowData.add(trimNull(detail.getDate("delayTermDate")));
      rowData.add(trimNull(detail.getBigDecimal("hasNoMoney")));
      rowData.add(isWarnStr);
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
  
  @Before({Tx.class})
  public void cancelWarn()
  {
    String configName = loginConfigName();
    String listID = overGetMoney;
    try
    {
      String modelType = getPara(0, "");
      int billId = getParaToInt(1, Integer.valueOf(0)).intValue();
      Model model = null;
      if (modelType.equals("get"))
      {
        model = SellBill.dao.findById(configName, Integer.valueOf(billId));
      }
      else if (modelType.equals("pay"))
      {
        model = PurchaseBill.dao.findById(configName, Integer.valueOf(billId));
        listID = overPayMoney;
      }
      model.set("isWarn", Integer.valueOf(AioConstants.OVERGETPAY_WARN1));
      model.update(configName);
      this.result.put("statusCode", Integer.valueOf(200));
    }
    catch (Exception e)
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "操作失败");
      e.printStackTrace();
    }
    this.result.put("rel", listID);
    renderJson(this.result);
  }
  
  public void recoveryWarn()
  {
    String listID = overGetMoney;
    String configName = loginConfigName();
    try
    {
      String modelType = getPara(0);
      int billId = getParaToInt(1).intValue();
      Model model = null;
      if (modelType.equals("get"))
      {
        model = SellBill.dao.findById(configName, Integer.valueOf(billId));
      }
      else if (modelType.equals("pay"))
      {
        model = PurchaseBill.dao.findById(configName, Integer.valueOf(billId));
        listID = overPayMoney;
      }
      model.set("delayDeliveryDate", model.getDate("deliveryDate"));
      model.set("isWarn", Integer.valueOf(AioConstants.OVERGETPAY_WARN0));
      model.update(configName);
      this.result.put("statusCode", Integer.valueOf(200));
    }
    catch (Exception e)
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "操作失败");
      e.printStackTrace();
    }
    this.result.put("rel", listID);
    renderJson(this.result);
  }
  
  public void toDelayWarn()
    throws ParseException
  {
    setAttr("modelType", getPara(0, "get"));
    setAttr("billId", getParaToInt(1));
    render("/WEB-INF/template/reports/finance/arap/overGetOrPayMoney/delayWarn.html");
  }
  
  @Before({Tx.class})
  public void delayWarn()
  {
    String listID = overGetMoney;
    String configName = loginConfigName();
    try
    {
      String modelType = getPara("modelType");
      int billId = getParaToInt("billId").intValue();
      String delayDay = getPara("delayDay");
      Model model = null;
      if (modelType.equals("get"))
      {
        model = SellBill.dao.findById(configName, Integer.valueOf(billId));
      }
      else if (modelType.equals("pay"))
      {
        model = PurchaseBill.dao.findById(configName, Integer.valueOf(billId));
        listID = overPayMoney;
      }
      String delayDate = null;
      if (delayDay.equals("delayDay"))
      {
        int days = getParaToInt("days").intValue();
        delayDate = DateUtils.format(DateUtils.addDays(model.getDate("deliveryDate"), days));
      }
      else if (delayDay.equals("toDelayDay"))
      {
        delayDate = getPara("date");
      }
      model.set("delayDeliveryDate", delayDate).update(configName);
      this.result.put("statusCode", Integer.valueOf(200));
    }
    catch (Exception e)
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "操作失败");
      e.printStackTrace();
    }
    this.result.put("rel", listID);
    this.result.put("callbackType", "closeCurrent");
    renderJson(this.result);
  }
}
