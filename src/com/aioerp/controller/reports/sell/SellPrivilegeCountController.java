package com.aioerp.controller.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Unit;
import com.aioerp.model.reports.sell.SellPrivilegeReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SellPrivilegeCountController
  extends BaseController
{
  private static final String listID = "xs_sellPrivilegeCount";
  
  public void toSearchDialog()
    throws ParseException
  {
    setStartDateAndEndDate();
    render("/WEB-INF/template/reports/sell/sellPrivilegeCount/searchDialog.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    
    String type = getPara(0, "first");
    setAttr("pageMap", com(map));
    
    columnConfig("xs504");
    if ((type.equals("first")) || (type.equals("search"))) {
      render("/WEB-INF/template/reports/sell/sellPrivilegeCount/page.html");
    } else if ((type.equals("page")) || (type.equals("tree"))) {
      render("/WEB-INF/template/reports/sell/sellPrivilegeCount/list.html");
    }
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    String configName = loginConfigName();
    String modelType = getPara("modelType", "");
    
    String modelTypeName = "";
    if (modelType.equals("unit")) {
      modelTypeName = "单位";
    } else if (modelType.equals("dept")) {
      modelTypeName = "部门";
    } else if (modelType.equals("staff")) {
      modelTypeName = "职员";
    }
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    map.put("departmentPrivs", basePrivs(DEPARTMENT_PRIVS));
    

    Integer unitId = getParaToInt("unit.id");
    String unitFullName = getPara("unit.fullName", "");
    Integer staffId = getParaToInt("staff.id");
    String staffFullName = getPara("staff.name", "");
    setAttr("modelTypeName", modelTypeName);
    setAttr("unitFullName", unitFullName);
    setAttr("staffFullName", staffFullName);
    

    String startDate = getPara("startDate");
    Date endDate = getParaToDate("endDate");
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), startDate, DateUtils.format(endDate));
    }
    map.put("listID", "xs_sellPrivilegeCount");
    map.put("modelType", modelType);
    map.put("unitId", unitId);
    map.put("staffId", staffId);
    map.put("startDate", startDate);
    map.put("endDate", StringUtil.dataToStr(endDate));
    
    Map<String, Object> reportPrdSellCount = SellPrivilegeReports.dao.reportSPrivilege(configName, map);
    mapToResponse(map);
    

    return reportPrdSellCount;
  }
  
  public void tree()
  {
    String configName = loginConfigName();
    String modelType = getPara(0, "prd");
    List<Model> cats = null;
    String nodeName = "";
    if (modelType.equals("prd"))
    {
      cats = Product.dao.getTreeEnableList(configName, basePrivs(PRODUCT_PRIVS));
      nodeName = "商品信息";
    }
    else if (modelType.equals("unit"))
    {
      cats = Unit.dao.getParentObjects(configName, basePrivs(UNIT_PRIVS));
      nodeName = "单位信息";
    }
    Iterator<Model> iter = cats.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model product = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", product.get("id"));
      node.put("pId", product.get("supId"));
      node.put("name", product.get("fullName"));
      node.put("url", "reports/prdSellCount/dialogSearch/tree-" + modelType + "-" + product.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", nodeName);
    node.put("url", "reports/prdSellCount/dialogSearch/tree-" + modelType + "-0");
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public void xsPrivilegeDetail()
    throws Exception
  {
    Map<String, Object> map = requestPageToMap(null, "recodeDate");
    
    String modelType = getPara("modelType", "");
    
    String columnType = "";
    if (modelType.equals("unit")) {
      columnType = "xs522";
    } else if (modelType.equals("dept")) {
      columnType = "xs523";
    } else if (modelType.equals("staff")) {
      columnType = "xs524";
    }
    setAttr("pageMap", xsPrivilegeDetailOrPrint(map));
    
    columnConfig(columnType);
    if (getPara("whereComeDetail").equals("other")) {
      render("/WEB-INF/template/reports/sell/sellPrivilegeCount/sellPrivilegeDetail/page.html");
    } else if (getPara("whereComeDetail").equals("self")) {
      render("/WEB-INF/template/reports/sell/sellPrivilegeCount/sellPrivilegeDetail/list.html");
    }
  }
  
  public Map<String, Object> xsPrivilegeDetailOrPrint(Map<String, Object> map)
    throws Exception
  {
    String configName = loginConfigName();
    
    String modelType = getPara("modelType", "");
    String whereComeDetail = getPara("whereComeDetail", "other");
    String aimDiv = getPara("aimDiv", "all");
    String orderField = "recodeDate";
    String orderDirection = "asc";
    if (whereComeDetail.equals("other"))
    {
      map.put("orderField", "recodeDate");
      map.put("orderDirection", "asc");
    }
    else
    {
      aimDiv = getPara("aimDiv", "");
    }
    String unitId = getPara("unit.id", getPara("id"));
    String staffId = getPara("staff.id", getPara("id"));
    String departmentId = getPara("department.id", getPara("id"));
    String startDate = getPara("startDate", "");
    String endDate = getPara("endDate");
    

    String columnType = "";
    String modelTypeName = "";
    if (modelType.equals("unit"))
    {
      columnType = "xs522";
      setAttr("obj", "unit");
      modelTypeName = "单位";
    }
    else if (modelType.equals("dept"))
    {
      columnType = "xs523";
      setAttr("obj", "department");
      modelTypeName = "部门";
    }
    else if (modelType.equals("staff"))
    {
      columnType = "xs524";
      setAttr("obj", "staff");
      modelTypeName = "职员";
    }
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    map.put("departmentPrivs", basePrivs(DEPARTMENT_PRIVS));
    

    map.put("listID", "xs_sellPrivilegeCount_detail");
    map.put("modelType", modelType);
    map.put("aimDiv", aimDiv);
    map.put("unitId", unitId);
    map.put("staffId", staffId);
    map.put("departmentId", departmentId);
    map.put("startDate", startDate);
    map.put("endDate", endDate);
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    Map<String, Object> dataMap = null;
    dataMap = SellPrivilegeReports.dao.sellPrivilegeDetail(configName, map);
    mapToResponse(map);
    setAttr("modelTypeName", modelTypeName);
    return dataMap;
  }
  
  public void xsPrivilegeDetailPrint()
    throws Exception
  {
    String modelType = getPara("modelType", "");
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    String billTypeName = "";
    if (modelType.equals("unit")) {
      billTypeName = "单位";
    } else if (modelType.equals("dept")) {
      billTypeName = "部门";
    } else if (modelType.equals("staff")) {
      billTypeName = "职员";
    }
    data.put("reportName", billTypeName + "销售明细账本");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    List<Model> detailList = new ArrayList();
    

    Map<String, Object> map = new HashMap();
    map.put("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    map.put("numPerPage", getParaToInt("totalCount", Integer.valueOf(9999999)));
    map.put("orderField", getPara("orderField", ORDER_FIELD));
    map.put("orderDirection", getPara("orderDirection", ORDER_DIRECTION));
    if (String.valueOf(map.get("numPerPage")).equals("0")) {
      map.put("numPerPage", Integer.valueOf(1));
    }
    Map<String, Object> pageMap = xsPrivilegeDetailOrPrint(map);
    if (pageMap != null) {
      detailList = (List)pageMap.get("pageList");
    }
    printCommonData(headData);
    headData.add("查询时间:" + getPara("startDate", "") + " 至 " + getPara("endDate", ""));
    

    colTitle.add("行号");
    colTitle.add("单据类型");
    colTitle.add("日期");
    colTitle.add("单据编号");
    colTitle.add("摘要");
    colTitle.add(billTypeName + "编号");
    colTitle.add(billTypeName + "全名");
    colTitle.add("价税合计");
    colTitle.add("优惠金额");
    colTitle.add("优惠后金额");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.getStr("billTypeName")));
      rowData.add(trimRecordDateNull(detail.getDate("recodeDate")));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("remark")));
      if (modelType.equals("unit"))
      {
        Model unit = (Model)detail.get("unit");
        rowData.add(trimNull(unit.getStr("code")));
        rowData.add(trimNull(unit.getStr("fullName")));
      }
      else if (modelType.equals("dept"))
      {
        Model department = (Model)detail.get("department");
        rowData.add(trimNull(department.getStr("code")));
        rowData.add(trimNull(department.getStr("fullName")));
      }
      else if (modelType.equals("staff"))
      {
        Model staff = (Model)detail.get("unit");
        rowData.add(trimNull(staff.getStr("code")));
        rowData.add(trimNull(staff.getStr("fullName")));
      }
      rowData.add(trimNull(detail.getBigDecimal("taxMoneys")));
      rowData.add(trimNull(detail.getBigDecimal("privilege")));
      rowData.add(trimNull(detail.getBigDecimal("privilegeMoney")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
  
  public void print()
    throws ParseException
  {
    String modelType = getPara("modelType", "");
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    if (modelType.equals("unit")) {
      data.put("reportName", "单位销售优惠统计");
    } else if (modelType.equals("dept")) {
      data.put("reportName", "部门销售优惠统计");
    } else if (modelType.equals("staff")) {
      data.put("reportName", "职员销售优惠统计");
    }
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
    headData.add("查询时间:" + getPara("startDate", "") + " 至 " + getPara("endDate", ""));
    


    colTitle.add("行号");
    colTitle.add("编号");
    colTitle.add("全名");
    colTitle.add("价税合计");
    colTitle.add("优惠金额");
    colTitle.add("优惠后金额");
    

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
      rowData.add(trimNull(detail.getBigDecimal("taxMoneys")));
      rowData.add(trimNull(detail.getBigDecimal("privilege")));
      rowData.add(trimNull(detail.getBigDecimal("privilegeMoney")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
