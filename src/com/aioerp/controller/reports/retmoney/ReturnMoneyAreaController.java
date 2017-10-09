package com.aioerp.controller.reports.retmoney;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Area;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.reports.retmoney.ReturnMoneyArea;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ReturnMoneyAreaController
  extends BaseController
{
  public void tree()
  {
    String configName = loginConfigName();
    String areaPrivs = basePrivs(AREA_PRIVS);
    List<Model> sorts = Area.dao.getAllSorts(configName, areaPrivs);
    Iterator<Model> iter = sorts.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model<Area> area = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", area.get("id"));
      node.put("pId", area.get("supId"));
      node.put("name", area.get("fullName"));
      node.put("url", "reports/retmoney/area/search/tree-" + area.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "地区信息");
    node.put("url", "reports/retmoney/area/search/tree-0");
    nodeList.add(node);
    
    renderJson(nodeList);
  }
  
  public void search()
    throws Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", "desc");
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    String type = getPara(0, "first");
    if (("tree".equals(type)) || ("child".equals(type))) {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
    }
    String isRow = getPara("isRow", "false");
    if ("row".equals(type)) {
      isRow = "true";
    }
    Map<String, Object> map = new HashMap();
    map.put("startTime", getPara("startTime"));
    map.put("endTime", getPara("endTime"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("supId", Integer.valueOf(supId));
    map.put("isRow", isRow);
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    map.put(AREA_PRIVS, basePrivs(AREA_PRIVS));
    
    Map<String, Object> pageMap = ReturnMoneyArea.dao.getPage(configName, pageNum, numPerPage, "returnMomeyAreaList", map);
    
    setAttr("params", map);
    setAttr("pageMap", pageMap);
    setAttr("isRow", isRow);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cw505", AioConstants.STATUS_ENABLE));
    if ("first".equals(type)) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void account()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String type = getPara(1, "first");
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    Integer areaId = getParaToInt("areaId");
    if ("first".equals(type))
    {
      pageNum = AioConstants.START_PAGE;
      numPerPage = 20;
      orderField = "";
      orderDirection = "";
      areaId = getParaToInt(0);
    }
    Area area = (Area)Area.dao.findById(configName, areaId);
    
    Map<String, Object> map = new HashMap();
    map.put("startTime", getPara("startTime"));
    map.put("endTime", getPara("endTime"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("isRcw", getParaToInt("isRcw"));
    map.put("areaId", areaId);
    map.put(ACCOUNT_PRIVS, basePrivs(ACCOUNT_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    Map<String, Object> pageMap = ReturnMoneyArea.dao.getAccountPage(configName, pageNum, numPerPage, "returnMomeyAreaAccountList", map);
    

    setAttr("area", area);
    setAttr("params", map);
    setAttr("pageMap", pageMap);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cw511", AioConstants.STATUS_ENABLE));
    if ("first".equals(type)) {
      render("account.html");
    } else {
      render("accountList.html");
    }
  }
  
  public void print()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "地区回款统计");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startTime", "") + "至" + getPara("endTime", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("地区编号");
    colTitle.add("地区全名");
    
    colTitle.add("销售合计");
    colTitle.add("现金回款");
    colTitle.add("银行回款");
    colTitle.add("回款总额");
    colTitle.add("应收发生额");
    colTitle.add("应付发生额");
    colTitle.add("应收余额");
    colTitle.add("应付余额");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    
    String isRow = getPara("isRow", "false");
    Map<String, Object> map = new HashMap();
    map.put("startTime", getPara("startTime"));
    map.put("endTime", getPara("endTime"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("supId", Integer.valueOf(supId));
    map.put("isRow", isRow);
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    map.put(AREA_PRIVS, basePrivs(AREA_PRIVS));
    
    Map<String, Object> pageMap = ReturnMoneyArea.dao.getPage(configName, 1, totalCount, "returnMomeyAreaList", map);
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model model = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      
      Model area = (Model)model.get("area");
      if (area != null)
      {
        rowData.add(trimNull(area.get("code")));
        if (area.get("id") == null) {
          rowData.add("其它地区");
        } else {
          rowData.add(trimNull(area.get("fullName")));
        }
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(model.get("sellMoneys")));
      rowData.add(trimNull(model.get("cashGetMoneys")));
      rowData.add(trimNull(model.get("bankGetMoneys")));
      rowData.add(trimNull(BigDecimalUtils.add(model.getBigDecimal("cashGetMoneys"), model.getBigDecimal("bankGetMoneys"))));
      rowData.add(trimNull(model.get("getMoneys")));
      rowData.add(trimNull(model.get("payMoneys")));
      if (BigDecimalUtils.compare(BigDecimalUtils.sub(model.getBigDecimal("shouldGetMoneys"), model.getBigDecimal("shouldPayMoneys")), BigDecimal.ZERO) == 1) {
        rowData.add(trimNull(BigDecimalUtils.sub(model.getBigDecimal("shouldGetMoneys"), model.getBigDecimal("shouldPayMoneys"))));
      } else {
        rowData.add("");
      }
      if (BigDecimalUtils.compare(BigDecimalUtils.sub(model.getBigDecimal("shouldPayMoneys"), model.getBigDecimal("shouldGetMoneys")), BigDecimal.ZERO) == 1) {
        rowData.add(trimNull(BigDecimalUtils.sub(model.getBigDecimal("shouldPayMoneys"), model.getBigDecimal("shouldGetMoneys"))));
      } else {
        rowData.add("");
      }
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
  
  public void printAccount()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "地区回款明细账本");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startTime", "") + "至" + getPara("endTime", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("单据类型");
    colTitle.add("日期");
    colTitle.add("单据编号");
    colTitle.add("职员编号");
    colTitle.add("职员全名");
    colTitle.add("收款账户编号");
    colTitle.add("收款账户全名");
    colTitle.add("回款合计");
    colTitle.add("摘要");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    
    Integer areaId = getParaToInt("areaId");
    Map<String, Object> map = new HashMap();
    map.put("startTime", getPara("startTime"));
    map.put("endTime", getPara("endTime"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("isRcw", getParaToInt("isRcw"));
    map.put("areaId", areaId);
    map.put(ACCOUNT_PRIVS, basePrivs(ACCOUNT_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    Map<String, Object> pageMap = ReturnMoneyArea.dao.getAccountPage(configName, 1, totalCount, "returnMomeyAreaAccountList", map);
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model model = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(model.get("billTypeName")));
      rowData.add(trimNull(model.get("recodeDate")));
      rowData.add(trimNull(model.get("code")));
      
      Model staff = (Model)model.get("staff");
      if (staff != null)
      {
        rowData.add(trimNull(staff.get("code")));
        rowData.add(trimNull(staff.get("name")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Model accounts = (Model)model.get("accounts");
      if (accounts != null)
      {
        rowData.add(trimNull(accounts.get("code")));
        rowData.add(trimNull(accounts.get("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(model.get("payMoney")));
      rowData.add(trimNull(model.get("remark")));
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
