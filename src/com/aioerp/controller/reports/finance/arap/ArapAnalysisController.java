package com.aioerp.controller.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Unit;
import com.aioerp.model.reports.finance.arap.ArapAnalysisReports;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArapAnalysisController
  extends BaseController
{
  private static String listID = "cw_arapAnalysis";
  
  public void searchDialog()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String type = getPara(0, "first");
    setAttr("pageMap", com(map));
    

    columnConfig("cw513");
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search")) || (type.equals("analysisSearch"))) {
      returnPage = "/WEB-INF/template/reports/finance/arap/arapAnalysis/page.html";
    } else if (type.equals("page")) {
      returnPage = "/WEB-INF/template/reports/finance/arap/arapAnalysis/list.html";
    }
    render(returnPage);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    String type = getPara(0, "first");
    

    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    String aimDiv = getPara("aimDiv", "all");
    
    String totalGetDown = getPara("totalGetDown", "-999999999");
    String totalGetUp = getPara("totalGetUp", "999999999");
    String totalPayDown = getPara("totalPayDown", "-999999999");
    String totalPayUp = getPara("totalPayUp", "999999999");
    String filterRange = getPara("filterRange", "current");
    if (type.equals("search"))
    {
      totalGetDown = "-999999999";
      totalGetUp = "999999999";
      totalPayDown = "-999999999";
      totalPayUp = "999999999";
      filterRange = "current";
    }
    if (filterRange.equals("all")) {
      aimDiv = "all";
    }
    map.put("totalGetDown", totalGetDown);
    map.put("totalGetUp", totalGetUp);
    map.put("totalPayDown", totalPayDown);
    map.put("totalPayUp", totalPayUp);
    map.put("filterRange", filterRange);
    map.put("listID", listID);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    map.put("aimDiv", aimDiv);
    Map<String, Object> pageMap = ArapAnalysisReports.dao.arapAnalysis(loginConfigName(), map);
    mapToResponse(map);
    return pageMap;
  }
  
  public void print()
    throws ParseException
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "往来分析");
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
    headData.add("查询时间:" + getPara("startTime", "") + " 至 " + getPara("endTime", ""));
    

    colTitle.add("行号");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("进货价税合计");
    colTitle.add("销售价税合计");
    colTitle.add("付款金额");
    colTitle.add("回款金额");
    colTitle.add("应收余额");
    colTitle.add("应付余额");
    colTitle.add("应收上限");
    colTitle.add("应付上限");
    colTitle.add("应收越限");
    colTitle.add("应付越限");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Record detail = (Record)detailList.get(i);
      String hasOverGet = detail.getStr("hasOverGet");
      String hasOverPay = detail.getStr("hasOverPay");
      if ((hasOverGet != null) && (hasOverGet.equals("yes"))) {
        hasOverGet = "√";
      } else {
        hasOverGet = "";
      }
      if ((hasOverPay != null) && (hasOverPay.equals("yes"))) {
        hasOverPay = "√";
      } else {
        hasOverPay = "";
      }
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("fullName")));
      rowData.add(trimNull(detail.getBigDecimal("inTaxMoneys")));
      rowData.add(trimNull(detail.getBigDecimal("outTaxMoneys")));
      rowData.add(trimNull(detail.getBigDecimal("payMoney")));
      rowData.add(trimNull(detail.getBigDecimal("getMoney")));
      rowData.add(trimNull(detail.getBigDecimal("totalGet")));
      rowData.add(trimNull(detail.getBigDecimal("totalPay")));
      rowData.add(trimNull(detail.getBigDecimal("getMoneyLimit")));
      rowData.add(trimNull(detail.getBigDecimal("payMoneyLimit")));
      rowData.add(hasOverGet);
      rowData.add(hasOverPay);
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
  
  public void toLimitMoneyAnalysis()
  {
    setAttr("listID", listID);
    setAttr("totalGetDown", getPara("totalGetDown"));
    setAttr("totalGetUp", getPara("totalGetUp"));
    setAttr("totalPayDown", getPara("totalPayDown"));
    setAttr("totalPayUp", getPara("totalPayUp"));
    setAttr("filterRange", getPara("filterRange", "current"));
    render("/WEB-INF/template/reports/finance/arap/arapAnalysis/limitMoneyAnalysis.html");
  }
  
  public void toLimitMoneyUpdate()
  {
    int unitId = getParaToInt(0, Integer.valueOf(0)).intValue();
    Unit unit = (Unit)Unit.dao.findById(loginConfigName(), Integer.valueOf(unitId));
    setAttr("unitId", unit.getInt("id"));
    setAttr("unitFullName", unit.getStr("fullName"));
    setAttr("getMoneyLimit", unit.getBigDecimal("getMoneyLimit"));
    setAttr("payMoneyLimit", unit.getBigDecimal("payMoneyLimit"));
    



    setAttr("startTime", getPara("startTime", ""));
    setAttr("endTime", getPara("endTime", ""));
    setAttr("aimDiv", getPara("aimDiv", "all"));
    setAttr("totalGetDown", getPara("totalGetDown"));
    setAttr("totalGetUp", getPara("totalGetUp"));
    setAttr("totalPayDown", getPara("totalPayDown"));
    setAttr("totalPayUp", getPara("totalPayUp"));
    

    render("/WEB-INF/template/reports/finance/arap/arapAnalysis/limitMoneyUpdate.html");
  }
  
  @Before({Tx.class})
  public void limitMoneyUpdate()
    throws SQLException
  {
    String configName = loginConfigName();
    try
    {
      String getMoneyLimit = getPara("getMoneyLimit", "");
      String payMoneyLimit = getPara("payMoneyLimit", "");
      String currentUnit = getPara("currentUnit", "");
      if (currentUnit.equals("yes"))
      {
        Map<String, Object> map = new HashMap();
        map.put("startTime", getPara("startTime"));
        map.put("endTime", getPara("endTime"));
        map.put("aimDiv", getPara("aimDiv"));
        map.put("totalGetDown", getPara("totalGetDown"));
        map.put("totalGetUp", getPara("totalGetUp"));
        map.put("totalPayDown", getPara("totalPayDown"));
        map.put("totalPayUp", getPara("totalPayUp"));
        List<Record> list = ArapAnalysisReports.dao.limitCurrentUnitUpMoney(configName, map);
        if ((list != null) && (list.size() > 0)) {
          for (int i = 0; i < list.size(); i++)
          {
            Unit unit = (Unit)Unit.dao.findById(configName, ((Record)list.get(i)).getInt("id"));
            unit.set("getMoneyLimit", getMoneyLimit);
            unit.set("payMoneyLimit", payMoneyLimit);
            unit.update(configName);
          }
        }
      }
      else
      {
        int unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
        Unit unit = (Unit)Unit.dao.findById(configName, Integer.valueOf(unitId));
        unit.set("getMoneyLimit", getMoneyLimit);
        unit.set("payMoneyLimit", payMoneyLimit);
        unit.update(configName);
      }
      this.result.put("statusCode", AioConstants.HTTP_RETURN200);
      this.result.put("navTabForm", "has");
      this.result.put("navTabId", "cw_report_arapanalysis_view");
      this.result.put("forwardUrl", getAttr("base") + "/reports/finance/arap/analysis/searchDialog/search");
      this.result.put("callbackType", "closeCurrent");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      this.result.put("message", "系统异常,请联系管理员！");
    }
    renderJson(this.result);
  }
}
