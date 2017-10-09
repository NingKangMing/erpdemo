package com.aioerp.controller.reports.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.bought.PurchaseBill;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.reports.bought.PurchaseDepartmentReports;
import com.aioerp.model.reports.bought.PurchaseStaffReports;
import com.aioerp.model.reports.bought.PurchaseUnitReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchasePrivilegeReportsController
  extends BaseController
{
  public void index()
    throws ParseException
  {
    setStartDateAndEndDate();
    setAttr("billType", getPara("billType"));
    render("search.html");
  }
  
  public void search()
  {
    String configName = loginConfigName();
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), getPara("startDate"), getPara("endDate"));
    }
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String type = getPara(0, "first");
    String billType = getPara("billType");
    String unitName = getPara("unit.fullName");
    String staffName = getPara("staff.name");
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    
    boolean flag = isInitReportOtherCondition(map);
    if (flag) {
      map.put("shoeType", null);
    }
    Map<String, Object> pageMap = new HashMap();
    if ("staff".equals(billType))
    {
      String staffPrivs = basePrivs(STAFF_PRIVS);
      pageMap = PurchaseStaffReports.dao.getPrivilegePage(configName, pageNum, numPerPage, "purchasePrivilegeStatisticsList", map, staffPrivs);
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg506", AioConstants.STATUS_ENABLE));
    }
    else if ("department".equals(billType))
    {
      pageMap = PurchaseDepartmentReports.dao.getPrivilegePage(configName, pageNum, numPerPage, "purchasePrivilegeStatisticsList", map);
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg507", AioConstants.STATUS_ENABLE));
    }
    else
    {
      pageMap = PurchaseUnitReports.dao.getPrivilegePage(configName, pageNum, numPerPage, "purchasePrivilegeStatisticsList", map);
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg505", AioConstants.STATUS_ENABLE));
    }
    List<Model> list = (List)pageMap.get("pageList");
    BigDecimal taxMoneys = BigDecimal.ZERO;
    BigDecimal privileges = BigDecimal.ZERO;
    BigDecimal privilegeMoneys = BigDecimal.ZERO;
    for (Model model : list)
    {
      taxMoneys = BigDecimalUtils.add(taxMoneys, model.getBigDecimal("taxMoneys"));
      privileges = BigDecimalUtils.add(privileges, model.getBigDecimal("privilege"));
      privilegeMoneys = BigDecimalUtils.add(privilegeMoneys, model.getBigDecimal("privilegeMoney"));
    }
    setAttr("taxMoneys", taxMoneys);
    setAttr("privileges", privileges);
    setAttr("privilegeMoneys", privilegeMoneys);
    setAttr("billType", billType);
    setAttr("unitName", unitName);
    setAttr("staffName", staffName);
    setAttr("params", map);
    setAttr("pageMap", pageMap);
    if ("first".equals(type)) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void detail()
  {
    String configName = loginConfigName();
    int pageNum = AioConstants.START_PAGE;
    int numPerPage = 20;
    String billType = getPara("billType");
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("isPrivilege", "true");
    if ("staff".equals(billType))
    {
      map.put("staffId", Integer.valueOf(id));
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg509", AioConstants.STATUS_ENABLE));
    }
    else if ("department".equals(billType))
    {
      map.put("departmentId", Integer.valueOf(id));
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5010", AioConstants.STATUS_ENABLE));
    }
    else
    {
      map.put("unitId", Integer.valueOf(id));
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg508", AioConstants.STATUS_ENABLE));
    }
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    
    Map<String, Object> pageMap = PurchaseBill.dao.getPrivilegePage(configName, pageNum, numPerPage, "purchasePrivilegeStatisticsDetailList", map);
    List<Model> list = (List)pageMap.get("pageList");
    BigDecimal taxMoneys = BigDecimal.ZERO;
    BigDecimal privileges = BigDecimal.ZERO;
    BigDecimal privilegeMoneys = BigDecimal.ZERO;
    for (Model model : list)
    {
      taxMoneys = BigDecimalUtils.add(taxMoneys, model.getBigDecimal("taxMoneys"));
      privileges = BigDecimalUtils.add(privileges, model.getBigDecimal("privilege"));
      privilegeMoneys = BigDecimalUtils.add(privilegeMoneys, model.getBigDecimal("privilegeMoney"));
    }
    setAttr("billType", billType);
    setAttr("taxMoneys", taxMoneys);
    setAttr("privileges", privileges);
    setAttr("privilegeMoneys", privilegeMoneys);
    setAttr("params", map);
    setAttr("pageMap", pageMap);
    render("detail.html");
  }
  
  public void detailSearch()
  {
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String configName = loginConfigName();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String billType = getPara("billType");
    
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("departmentId", getParaToInt("department.id"));
    map.put("notStatus", getPara("notStatus"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("isPrivilege", "true");
    
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    
    Map<String, Object> pageMap = PurchaseBill.dao.getPrivilegePage(configName, pageNum, numPerPage, "purchasePrivilegeStatisticsDetailList", map);
    List<Model> list = (List)pageMap.get("pageList");
    BigDecimal taxMoneys = BigDecimal.ZERO;
    BigDecimal privileges = BigDecimal.ZERO;
    BigDecimal privilegeMoneys = BigDecimal.ZERO;
    for (Model model : list)
    {
      taxMoneys = BigDecimalUtils.add(taxMoneys, model.getBigDecimal("taxMoneys"));
      privileges = BigDecimalUtils.add(privileges, model.getBigDecimal("privilege"));
      privilegeMoneys = BigDecimalUtils.add(privilegeMoneys, model.getBigDecimal("privilegeMoney"));
    }
    if ("staff".equals(billType)) {
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg509", AioConstants.STATUS_ENABLE));
    } else if ("department".equals(billType)) {
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5010", AioConstants.STATUS_ENABLE));
    } else {
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg508", AioConstants.STATUS_ENABLE));
    }
    setAttr("billType", billType);
    setAttr("taxMoneys", taxMoneys);
    setAttr("privileges", privileges);
    setAttr("privilegeMoneys", privilegeMoneys);
    setAttr("params", map);
    setAttr("pageMap", pageMap);
    render("detailList.html");
  }
  
  public void print()
    throws SQLException, Exception
  {
    String billType = getPara("billType");
    Map<String, Object> data = new HashMap();
    String configName = loginConfigName();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "进货优惠统计");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startDate", "") + "至" + getPara("endDate", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    if ("staff".equals(billType))
    {
      colTitle.add("职员编号");
      colTitle.add("职员全名");
    }
    else if ("department".equals(billType))
    {
      colTitle.add("部门编号");
      colTitle.add("部门全名");
    }
    else
    {
      colTitle.add("单位编号");
      colTitle.add("单位全名");
    }
    colTitle.add("进货金额");
    colTitle.add("优惠金额");
    colTitle.add("优惠后金额");
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    
    boolean flag = isInitReportOtherCondition(map);
    if (flag) {
      map.put("shoeType", null);
    }
    Map<String, Object> pageMap = new HashMap();
    if ("staff".equals(billType))
    {
      String staffPrivs = basePrivs(STAFF_PRIVS);
      pageMap = PurchaseStaffReports.dao.getPrivilegePage(configName, 1, totalCount, "purchasePrivilegeStatisticsList", map, staffPrivs);
    }
    else if ("department".equals(billType))
    {
      pageMap = PurchaseDepartmentReports.dao.getPrivilegePage(configName, 1, totalCount, "purchasePrivilegeStatisticsList", map);
    }
    else
    {
      pageMap = PurchaseUnitReports.dao.getPrivilegePage(configName, 1, totalCount, "purchasePrivilegeStatisticsList", map);
    }
    List<Model> list = (List)pageMap.get("pageList");
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < list.size(); i++)
    {
      Model model = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("fullName")));
      if (!hasCostPermitted)
      {
        rowData.add("***");
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        rowData.add(trimNull(model.get("taxMoneys")));
        rowData.add(trimNull(model.get("privilege")));
        rowData.add(trimNull(model.get("privilegeMoney")));
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
  
  public void printDetail()
    throws SQLException, Exception
  {
    String billType = getPara("billType");
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "优惠明细账本");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startDate", "") + "至" + getPara("endDate", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("日期");
    colTitle.add("单据编号");
    colTitle.add("摘要");
    if ("staff".equals(billType))
    {
      colTitle.add("职员编号");
      colTitle.add("职员全名");
    }
    else if ("department".equals(billType))
    {
      colTitle.add("部门编号");
      colTitle.add("部门全名");
    }
    else
    {
      colTitle.add("单位编号");
      colTitle.add("单位全名");
    }
    colTitle.add("进货金额");
    colTitle.add("优惠金额");
    colTitle.add("优惠后金额");
    colWidth.add("30");
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
    
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("departmentId", getParaToInt("department.id"));
    map.put("notStatus", getPara("notStatus"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("isPrivilege", "true");
    
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    
    Map<String, Object> pageMap = PurchaseBill.dao.getPrivilegePage(loginConfigName(), 1, totalCount, "purchasePrivilegeStatisticsDetailList", map);
    List<Model> list = (List)pageMap.get("pageList");
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < list.size(); i++)
    {
      Model model = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(model.get("recodeDate")));
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("remark")));
      if ("staff".equals(billType))
      {
        rowData.add(trimNull(model.get("staffCode")));
        rowData.add(trimNull(model.get("staffName")));
      }
      else if ("department".equals(billType))
      {
        rowData.add(trimNull(model.get("departmentCode")));
        rowData.add(trimNull(model.get("departmentName")));
      }
      else
      {
        rowData.add(trimNull(model.get("unitCode")));
        rowData.add(trimNull(model.get("unitName")));
      }
      if (!hasCostPermitted)
      {
        rowData.add("***");
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        rowData.add(trimNull(model.get("taxMoneys")));
        rowData.add(trimNull(model.get("privilege")));
        rowData.add(trimNull(model.get("privilegeMoney")));
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
}
