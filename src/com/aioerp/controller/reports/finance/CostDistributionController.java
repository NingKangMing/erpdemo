package com.aioerp.controller.reports.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.reports.finance.CostDistribution;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostDistributionController
  extends BaseController
{
  public void index()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String disType = getPara(0, "depm");
    String type = getPara(1, "first");
    if ("search".equals(type)) {
      disType = getPara("disType");
    }
    List<Record> objList = new ArrayList();
    if ("depm".equals(disType))
    {
      objList = CostDistribution.dao.getPayDepm(configName, basePrivs(DEPARTMENT_PRIVS));
      Record obj = new Record();
      obj.set("id", Integer.valueOf(0));
      obj.set("fullName", "其它部门");
      objList.add(obj);
    }
    else if ("staff".equals(disType))
    {
      objList = CostDistribution.dao.getPayStaff(configName, basePrivs(STAFF_PRIVS));
      Record obj = new Record();
      obj.set("id", Integer.valueOf(0));
      obj.set("fullName", "其它职员");
      objList.add(obj);
    }
    else if ("unit".equals(disType))
    {
      objList = CostDistribution.dao.getPayUnit(configName, basePrivs(UNIT_PRIVS));
    }
    else if ("area".equals(disType))
    {
      objList = CostDistribution.dao.getPayArea(configName, basePrivs(AREA_PRIVS));
      Record obj = new Record();
      obj.set("id", Integer.valueOf(0));
      obj.set("fullName", "其它地区");
      objList.add(obj);
    }
    Map<String, Object> map = new HashMap();
    map.put("startTime", getPara("startTime"));
    map.put("endTime", getPara("endTime"));
    map.put("disType", disType);
    map.put(ACCOUNT_PRIVS, basePrivs(ACCOUNT_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(AREA_PRIVS, basePrivs(AREA_PRIVS));
    Map<String, Object> pageMap = new HashMap();
    if ("depm".equals(disType)) {
      pageMap = CostDistribution.dao.getDepmPage(configName, pageNum, numPerPage, "costDistributionList", map);
    } else if ("staff".equals(disType)) {
      pageMap = CostDistribution.dao.getStaffPage(configName, pageNum, numPerPage, "costDistributionList", map);
    } else if ("unit".equals(disType)) {
      pageMap = CostDistribution.dao.getUnitPage(configName, pageNum, numPerPage, "costDistributionList", map);
    } else if ("area".equals(disType)) {
      pageMap = CostDistribution.dao.getAreaPage(configName, pageNum, numPerPage, "costDistributionList", map);
    }
    setAttr("objList", objList);
    setAttr("pageMap", pageMap);
    setAttr("params", map);
    setAttr("disType", disType);
    if (("first".equals(type)) || ("search".equals(type))) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void toSearch()
  {
    Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
    if (paraMap.size() > 0) {
      setAttr("paraMap", paraMap);
    }
    setAttr("disType", getPara("disTypes"));
    render("search.html");
  }
  
  public void go()
  {
    setAttr("statusCode", Integer.valueOf(200));
    
    setAttr("aimTabId", getPara("aimTabId", ""));
    setAttr("aimUrl", getPara("aimUrl", ""));
    setAttr("aimTitle", getPara("aimTitle", ""));
    

    Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
    paraMap.remove("aimTabId");
    paraMap.remove("aimUrl");
    paraMap.remove("aimTitle");
    if (paraMap.size() > 0) {
      setAttr("data", paraMap);
    }
    renderJson();
  }
  
  public void print()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String disType = getPara("disType");
    
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    List<Record> objList = new ArrayList();
    if ("depm".equals(disType))
    {
      data.put("reportName", "部门费用分布 ");
      objList = CostDistribution.dao.getPayDepm(configName, basePrivs(DEPARTMENT_PRIVS));
      Record obj = new Record();
      obj.set("id", Integer.valueOf(0));
      obj.set("fullName", "其它部门");
      objList.add(obj);
    }
    else if ("staff".equals(disType))
    {
      data.put("reportName", "职员费用分布 ");
      objList = CostDistribution.dao.getPayStaff(configName, basePrivs(STAFF_PRIVS));
      Record obj = new Record();
      obj.set("id", Integer.valueOf(0));
      obj.set("fullName", "其它职员");
      objList.add(obj);
    }
    else if ("unit".equals(disType))
    {
      data.put("reportName", "单位费用分布 ");
      objList = CostDistribution.dao.getPayUnit(configName, basePrivs(UNIT_PRIVS));
    }
    else if ("area".equals(disType))
    {
      data.put("reportName", "地区费用分布");
      objList = CostDistribution.dao.getPayArea(configName, basePrivs(AREA_PRIVS));
      Record obj = new Record();
      obj.set("id", Integer.valueOf(0));
      obj.set("fullName", "其它地区");
      objList.add(obj);
    }
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startTime", "") + "至" + getPara("endTime", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    colTitle.add("行号");
    colTitle.add("科目编号");
    colTitle.add("科目全名");
    for (Record record : objList) {
      colTitle.add(record.getStr("fullName") + "金额");
    }
    colTitle.add("金额");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    for (Record record : objList) {
      colWidth.add("500");
    }
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    
    Map<String, Object> map = new HashMap();
    map.put("startTime", getPara("startTime"));
    map.put("endTime", getPara("endTime"));
    map.put("disType", disType);
    map.put(ACCOUNT_PRIVS, basePrivs(ACCOUNT_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(AREA_PRIVS, basePrivs(AREA_PRIVS));
    Map<String, Object> pageMap = new HashMap();
    if ("depm".equals(disType)) {
      pageMap = CostDistribution.dao.getDepmPage(configName, 1, totalCount, "costDistributionList", map);
    } else if ("staff".equals(disType)) {
      pageMap = CostDistribution.dao.getStaffPage(configName, 1, totalCount, "costDistributionList", map);
    } else if ("unit".equals(disType)) {
      pageMap = CostDistribution.dao.getUnitPage(configName, 1, totalCount, "costDistributionList", map);
    } else if ("area".equals(disType)) {
      pageMap = CostDistribution.dao.getAreaPage(configName, 1, totalCount, "costDistributionList", map);
    }
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model model = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("fullName")));
      for (Record record : objList) {
        rowData.add(trimNull(model.get("money" + record.getInt("id"))));
      }
      rowData.add(trimNull(model.get("allMoney")));
    }
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
