package com.aioerp.controller.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.reports.finance.arap.ArapAgeAnalysisReports;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArapAgeController
  extends BaseController
{
  private static String listID = "cw_arapAnalysis";
  
  public void toSearchDialog()
  {
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("base", getAttr("base"));
    render("/WEB-INF/template/reports/finance/arap/arapAge/searchDialog.html");
  }
  
  public void searchDialog()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String type = getPara(0, "first");
    setAttr("pageMap", com(map));
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search")) || (type.equals("analysisSearch"))) {
      returnPage = "/WEB-INF/template/reports/finance/arap/arapAge/page.html";
    } else if (type.equals("page")) {
      returnPage = "/WEB-INF/template/reports/finance/arap/arapAge/list.html";
    }
    render(returnPage);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    int day1 = getParaToInt("day1", Integer.valueOf(30)).intValue();
    int day2 = getParaToInt("day2", Integer.valueOf(30)).intValue();
    int day3 = getParaToInt("day3", Integer.valueOf(30)).intValue();
    int day4 = getParaToInt("day4", Integer.valueOf(30)).intValue();
    int day5 = getParaToInt("day5", Integer.valueOf(30)).intValue();
    int day6 = getParaToInt("day6", Integer.valueOf(30)).intValue();
    int day7 = getParaToInt("day7", Integer.valueOf(30)).intValue();
    int day8 = getParaToInt("day8", Integer.valueOf(30)).intValue();
    int day9 = getParaToInt("day9", Integer.valueOf(30)).intValue();
    int day10 = getParaToInt("day10", Integer.valueOf(30)).intValue();
    int day11 = getParaToInt("day11", Integer.valueOf(30)).intValue();
    int day12 = getParaToInt("day12", Integer.valueOf(30)).intValue();
    int day13 = getParaToInt("day13", Integer.valueOf(30)).intValue();
    String aimDiv = getPara("aimDiv", "all");
    String stopDate = getPara("stopDate");
    String payOrGet = getPara("payOrGet", "get");
    String unitIds = getPara("unitIds", ",");
    String typeName = "应收应付";
    if (payOrGet.equals("get")) {
      typeName = "应收账款";
    } else if (payOrGet.equals("pay")) {
      typeName = "应付账款";
    }
    setAttr("typeName", typeName);
    

    map.put("listID", listID);
    map.put("day1", Integer.valueOf(day1));
    map.put("day2", Integer.valueOf(day2));
    map.put("day3", Integer.valueOf(day3));
    map.put("day4", Integer.valueOf(day4));
    map.put("day5", Integer.valueOf(day5));
    map.put("day6", Integer.valueOf(day6));
    map.put("day7", Integer.valueOf(day7));
    map.put("day8", Integer.valueOf(day8));
    map.put("day9", Integer.valueOf(day9));
    map.put("day10", Integer.valueOf(day10));
    map.put("day11", Integer.valueOf(day11));
    map.put("day12", Integer.valueOf(day12));
    map.put("day13", Integer.valueOf(day13));
    map.put("aimDiv", aimDiv);
    map.put("stopDate", stopDate);
    map.put("payOrGet", payOrGet);
    map.put("unitIds", unitIds);
    Map<String, Object> pageMap = ArapAgeAnalysisReports.dao.arapAgeAnalysis(loginConfigName(), map);
    mapToResponse(map);
    return pageMap;
  }
  
  public void print()
    throws ParseException
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "账龄分析");
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
    headData.add("查询时间: 至 " + getPara("stopDate", ""));
    
    int day1 = Integer.valueOf(String.valueOf(map.get("day1"))).intValue();
    int day2 = Integer.valueOf(String.valueOf(map.get("day2"))).intValue();
    int day3 = Integer.valueOf(String.valueOf(map.get("day3"))).intValue();
    int day4 = Integer.valueOf(String.valueOf(map.get("day4"))).intValue();
    int day5 = Integer.valueOf(String.valueOf(map.get("day5"))).intValue();
    int day6 = Integer.valueOf(String.valueOf(map.get("day6"))).intValue();
    int day7 = Integer.valueOf(String.valueOf(map.get("day7"))).intValue();
    int day8 = Integer.valueOf(String.valueOf(map.get("day8"))).intValue();
    int day9 = Integer.valueOf(String.valueOf(map.get("day9"))).intValue();
    int day10 = Integer.valueOf(String.valueOf(map.get("day10"))).intValue();
    int day11 = Integer.valueOf(String.valueOf(map.get("day11"))).intValue();
    int day12 = Integer.valueOf(String.valueOf(map.get("day12"))).intValue();
    int startDay = 1;
    colTitle.add("行号");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("累计");
    colTitle.add("从1至" + day1 + "天");
    
    startDay += day1;
    colTitle.add("从" + startDay + "至" + day2 + "天");
    
    startDay += day2;
    colTitle.add("从" + startDay + "至" + day3 + "天");
    
    startDay += day3;
    colTitle.add("从" + startDay + "至" + day4 + "天");
    
    startDay += day4;
    colTitle.add("从" + startDay + "至" + day5 + "天");
    
    startDay += day5;
    colTitle.add("从" + startDay + "至" + day6 + "天");
    
    startDay += day6;
    colTitle.add("从" + startDay + "至" + day7 + "天");
    
    startDay += day7;
    colTitle.add("从" + startDay + "至" + day8 + "天");
    
    startDay += day8;
    colTitle.add("从" + startDay + "至" + day9 + "天");
    
    startDay += day9;
    colTitle.add("从" + startDay + "至" + day10 + "天");
    
    startDay += day10;
    colTitle.add("从" + startDay + "至" + day11 + "天");
    
    startDay += day11;
    colTitle.add("从" + startDay + "至" + day12 + "天");
    
    startDay += day12;
    colTitle.add("从" + startDay + "天以上");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Record detail = (Record)detailList.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("fullName")));
      rowData.add(trimNull(detail.getBigDecimal("countMoney")));
      rowData.add(trimNull(detail.getBigDecimal("money1")));
      rowData.add(trimNull(detail.getBigDecimal("money2")));
      rowData.add(trimNull(detail.getBigDecimal("money3")));
      rowData.add(trimNull(detail.getBigDecimal("money4")));
      rowData.add(trimNull(detail.getBigDecimal("money5")));
      rowData.add(trimNull(detail.getBigDecimal("money6")));
      rowData.add(trimNull(detail.getBigDecimal("money7")));
      rowData.add(trimNull(detail.getBigDecimal("money8")));
      rowData.add(trimNull(detail.getBigDecimal("money9")));
      rowData.add(trimNull(detail.getBigDecimal("money10")));
      rowData.add(trimNull(detail.getBigDecimal("money11")));
      rowData.add(trimNull(detail.getBigDecimal("money12")));
      rowData.add(trimNull(detail.getBigDecimal("money13")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
