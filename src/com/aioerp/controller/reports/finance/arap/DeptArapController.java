package com.aioerp.controller.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.aioerp.model.reports.finance.arap.ArapRecords;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DeptArapController
  extends BaseController
{
  private static String listID = "deptArapList";
  
  public void index()
    throws SQLException, Exception
  {
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "deptRank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    Integer node = getParaToInt("node", Integer.valueOf(0));
    
    Map<String, Object> pageMap = Department.dao.getDeptArapPage(loginConfigName(), basePrivs(DEPARTMENT_PRIVS), listID, pageNum, numPerPage, orderField, orderDirection, supId, null, startTime, endTime, node.intValue());
    

    columnConfig("cw522");
    
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", node);
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    
    setAttr("pageMap", pageMap);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    
    render("page.html");
  }
  
  public void list()
    throws SQLException, Exception
  {
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    String configName = loginConfigName();
    Integer pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    Integer numPerPage = getParaToInt("numPerPage", Integer.valueOf(20));
    String orderField = getPara("orderField", "deptRank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    
    Integer node = getParaToInt("node", Integer.valueOf(0));
    if (upObjectId > 0)
    {
      selectedObjectId = Integer.valueOf(upObjectId);
      node = Integer.valueOf(0);
    }
    Map<String, Object> pageMap = Department.dao.getDeptArapPage(configName, basePrivs(DEPARTMENT_PRIVS), listID, pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection, supId, Integer.valueOf(upObjectId), startTime, endTime, node.intValue());
    
    Integer pSupId = Integer.valueOf(0);
    if (supId > 0) {
      pSupId = Integer.valueOf(Department.dao.getPsupId(configName, supId));
    }
    columnConfig("cw522");
    
    setAttr("pageMap", pageMap);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    setAttr("pSupId", pSupId);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", node);
    setAttr("objectId", selectedObjectId);
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    
    render("list.html");
  }
  
  public void tree()
  {
    String departmentPrivs = basePrivs(DEPARTMENT_PRIVS);
    String configName = loginConfigName();
    List<Model> sorts = Department.dao.getAllSorts(configName, departmentPrivs);
    Iterator<Model> iter = sorts.iterator();
    
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model mode = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", mode.get("id"));
      node.put("pId", mode.get("supId"));
      node.put("name", mode.get("fullName"));
      node.put("url", "reports/finance/arap/deptArap/list/" + mode.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(-1));
    node.put("name", "部门信息");
    node.put("url", "reports/finance/arap/deptArap/list/0");
    nodeList.add(node);
    
    renderJson(nodeList);
  }
  
  public void unitDetail()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String pageId = "deptUnitArapPage";
    String alias = "bu";
    
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    String deptName = "其他部门";
    String pids = null;
    if (selectedObjectId.intValue() > 0)
    {
      Department dept = (Department)Department.dao.findById(configName, selectedObjectId);
      deptName = dept.getStr("fullName");
      pids = dept.getStr("pids");
    }
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "arap.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    Map<String, Object> pageMap = Department.dao.getUnitArapPageByDept(configName, basePrivs(UNIT_PRIVS), pageId, pageNum, numPerPage, orderField, orderDirection, startTime, endTime, pids, alias);
    

    columnConfig("cw531");
    
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    setAttr("objectId", selectedObjectId);
    setAttr("deptName", deptName);
    
    setAttr("pageMap", pageMap);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    render("unitDetail.html");
  }
  
  public void staffDetail()
    throws SQLException, Exception
  {
    String pageId = "staffStaffArapPage";
    String alias = "bs";
    String configName = loginConfigName();
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    String deptName = "其他部门";
    String pids = null;
    if (selectedObjectId.intValue() > 0)
    {
      Department dept = (Department)Department.dao.findById(configName, selectedObjectId);
      deptName = dept.getStr("fullName");
      pids = dept.getStr("pids");
    }
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "arap.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    Map<String, Object> pageMap = Department.dao.getUnitArapPageByDept(configName, basePrivs(STAFF_PRIVS), pageId, pageNum, numPerPage, orderField, orderDirection, startTime, endTime, pids, alias);
    

    columnConfig("cw532");
    
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    setAttr("objectId", selectedObjectId);
    setAttr("deptName", deptName);
    
    setAttr("pageMap", pageMap);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    render("staffDetail.html");
  }
  
  public void printDetail()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    int m = getParaToInt(0, Integer.valueOf(1)).intValue();
    
    data.put("reportNo", Integer.valueOf(301));
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    if (m == 1)
    {
      data.put("reportName", "应收应付-单位明细 ");
      colTitle.add("单位编号");
      colTitle.add("单位全名");
      colTitle.add("应收金额");
      colTitle.add("应付金额");
    }
    else if (m == 2)
    {
      data.put("reportName", "应收应付-职员明细 ");
      colTitle.add("职员编号");
      colTitle.add("职员全名");
      colTitle.add("应收金额");
      colTitle.add("应付金额");
    }
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    

    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    
    String pids = null;
    if (selectedObjectId.intValue() > 0)
    {
      Department dept = (Department)Department.dao.findById(configName, selectedObjectId);
      
      pids = dept.getStr("pids");
    }
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "arap.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    

    Map<String, Object> pageMap = new HashMap();
    if (m == 1)
    {
      String alias = "bu";
      pageMap = Department.dao.getUnitArapPageByDept(configName, basePrivs(UNIT_PRIVS), "", pageNum, numPerPage, orderField, orderDirection, startTime, endTime, pids, alias);
    }
    else if (m == 2)
    {
      String alias = "bs";
      pageMap = Department.dao.getUnitArapPageByDept(configName, basePrivs(STAFF_PRIVS), "", pageNum, numPerPage, orderField, orderDirection, startTime, endTime, pids, alias);
    }
    List<Model> list = (List)pageMap.get("pageList");
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    int i = 1;
    for (Model record : list)
    {
      rowData.add(trimNull(i));
      if (m == 1)
      {
        Unit unit = (Unit)record.get("bu");
        rowData.add(trimNull(unit.get("code")));
        rowData.add(trimNull(record.get("unitName")));
      }
      else if (m == 2)
      {
        Staff staff = (Staff)record.get("bs");
        rowData.add(trimNull(staff.get("code")));
        rowData.add(trimNull(record.get("staffName")));
      }
      ArapRecords arap = (ArapRecords)record.get("arap");
      rowData.add(trimNull(arap.get("addMoney")));
      rowData.add(trimNull(arap.get("subMoney")));
      
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
