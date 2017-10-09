package com.aioerp.controller.reports.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Area;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessCountController
  extends BaseController
{
  public void areaBusiness()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String listId = "areaBusinessPage";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "areaRank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    

    Map<String, Object> map = Area.dao.getAreaBusCountPage(configName, basePrivs(AREA_PRIVS), listId, pageNum, numPerPage, orderField, orderDirection, startTime, endTime);
    

    columnConfig("cw527");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    
    render("areaBusiness.html");
  }
  
  public void deptBusiness()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String listId = "deptBusinessPage";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "deptRank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    

    Map<String, Object> map = Department.dao.getDeptBusCountPage(configName, basePrivs(DEPARTMENT_PRIVS), listId, pageNum, numPerPage, orderField, orderDirection, startTime, endTime);
    

    columnConfig("cw526");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    
    render("deptBusiness.html");
  }
  
  public void staffBusiness()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String listId = "staffBusinessPage";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "staffRank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    

    Map<String, Object> map = Staff.dao.getStaffBusCountPage(configName, basePrivs(STAFF_PRIVS), listId, pageNum, numPerPage, orderField, orderDirection, startTime, endTime);
    

    columnConfig("cw525");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    
    render("staffBusiness.html");
  }
  
  public void unitBusiness()
    throws SQLException, Exception
  {
    String listId = "unitBusinessPage";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "unitRank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    Map<String, Object> map = Unit.dao.getUnitBusCountPage(loginConfigName(), basePrivs(UNIT_PRIVS), basePrivs(AREA_PRIVS), listId, pageNum, numPerPage, orderField, orderDirection, startTime, endTime);
    

    columnConfig("cw524");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    
    render("unitBusiness.html");
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    int m = getParaToInt(0, Integer.valueOf(1)).intValue();
    
    data.put("reportNo", Integer.valueOf(301));
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    if (m == 1)
    {
      data.put("reportName", "单位业务统计");
      colTitle.add("单位编号");
      colTitle.add("单位全名");
      colTitle.add("地区编号");
      colTitle.add("地区全名");
    }
    else if (m == 2)
    {
      data.put("reportName", "职员业务统计");
      colTitle.add("职员编号");
      colTitle.add("职员全名");
    }
    else if (m == 3)
    {
      data.put("reportName", "部门业务统计");
      colTitle.add("部门编号");
      colTitle.add("部门全名");
    }
    else if (m == 4)
    {
      data.put("reportName", "地区业务统计");
      colTitle.add("地区编号");
      colTitle.add("地区全名名");
    }
    colTitle.add("进货金额");
    colTitle.add("进货未付款");
    colTitle.add("销售金额");
    colTitle.add("销售未付款");
    colTitle.add("其他收入");
    colTitle.add("费用合计");
    colTitle.add("回款合计");
    colTitle.add("成本金额");
    colTitle.add("利润额");
    colTitle.add("本期创利");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "unitRank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    

    Map<String, Object> pageMap = new HashMap();
    if (m == 1) {
      pageMap = Unit.dao.getUnitBusCountPage(configName, basePrivs(UNIT_PRIVS), basePrivs(AREA_PRIVS), "", pageNum, numPerPage, orderField, orderDirection, startTime, endTime);
    } else if (m == 2) {
      pageMap = Staff.dao.getStaffBusCountPage(configName, basePrivs(STAFF_PRIVS), "", pageNum, numPerPage, orderField, orderDirection, startTime, endTime);
    } else if (m == 3) {
      pageMap = Department.dao.getDeptBusCountPage(configName, basePrivs(DEPARTMENT_PRIVS), "", pageNum, numPerPage, orderField, orderDirection, startTime, endTime);
    } else if (m == 4) {
      pageMap = Area.dao.getAreaBusCountPage(configName, basePrivs(AREA_PRIVS), "", pageNum, numPerPage, orderField, orderDirection, startTime, endTime);
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
        Area area = (Area)record.get("ba");
        rowData.add(trimNull(area.get("code")));
        rowData.add(trimNull(area.get("fullName")));
      }
      else if (m == 2)
      {
        Staff staff = (Staff)record.get("staff");
        rowData.add(trimNull(staff.get("code")));
        rowData.add(trimNull(record.get("staffName")));
      }
      else if (m == 3)
      {
        Department dept = (Department)record.get("dept");
        rowData.add(trimNull(dept.get("code")));
        rowData.add(trimNull(record.get("deptName")));
      }
      else if (m == 4)
      {
        Area area = (Area)record.get("area");
        rowData.add(trimNull(area.get("code")));
        rowData.add(trimNull(record.get("areaName")));
      }
      rowData.add(trimNull(record.get("inMoney")));
      rowData.add(trimNull(record.get("inWaitMoney")));
      rowData.add(trimNull(record.get("outMoney")));
      rowData.add(trimNull(record.get("outWaitMoney")));
      rowData.add(trimNull(record.get("otherIn")));
      rowData.add(trimNull(record.get("costTotal")));
      rowData.add(trimNull(record.get("returenMoney")));
      if (!hasCostPermitted)
      {
        rowData.add("***");
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        rowData.add(trimNull(record.get("costMoney")));
        rowData.add(trimNull(record.get("profit")));
        rowData.add(trimNull(record.get("periodProfit")));
      }
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
