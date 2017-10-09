package com.aioerp.controller.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StaffArapController
  extends BaseController
{
  private static String listId = "staffArapList";
  
  public void index()
    throws SQLException, Exception
  {
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "staffRank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    Integer node = getParaToInt("node", Integer.valueOf(0));
    
    Map<String, Object> pageMap = Staff.dao.getStaffArapPage(loginConfigName(), basePrivs(STAFF_PRIVS), listId, pageNum, numPerPage, orderField, orderDirection, Integer.valueOf(supId), null, startTime, endTime, node.intValue());
    

    columnConfig("cw521");
    
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
    String orderField = getPara("orderField", "staffRank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer node = getParaToInt("node", Integer.valueOf(0));
    
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (upObjectId > 0)
    {
      selectedObjectId = Integer.valueOf(upObjectId);
      node = Integer.valueOf(0);
    }
    Map<String, Object> map = new HashMap();
    map = Staff.dao.getStaffArapPage(configName, basePrivs(STAFF_PRIVS), listId, pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection, Integer.valueOf(supId), Integer.valueOf(upObjectId), startTime, endTime, node.intValue());
    
    Integer pSupId = Integer.valueOf(0);
    if (supId > 0) {
      pSupId = Integer.valueOf(Staff.dao.getPsupId(configName, supId));
    }
    columnConfig("cw521");
    
    setAttr("pageMap", map);
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
    String configName = loginConfigName();
    String staffPrivs = basePrivs(STAFF_PRIVS);
    List<Model> staffs = Staff.dao.getParent(configName, staffPrivs);
    Iterator<Model> iter = staffs.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model<Staff> staff = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", staff.get("id"));
      node.put("pId", staff.get("supId"));
      node.put("name", staff.get("name"));
      node.put("url", "reports/finance/arap/staffArap/list/" + staff.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "职员信息");
    node.put("url", "reports/finance/arap/staffArap/list/0");
    nodeList.add(node);
    
    renderJson(nodeList);
  }
  
  public void detail()
    throws SQLException, Exception
  {
    String divId = "staffArapDetaiPage";
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    String staffName = "其他职员";
    String pids = null;
    String configName = loginConfigName();
    if (selectedObjectId.intValue() > 0)
    {
      Staff staff = (Staff)Staff.dao.findById(configName, selectedObjectId);
      staffName = staff.getStr("fullName");
      pids = staff.getStr("pids");
    }
    Integer pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    Integer numPerPage = getParaToInt("numPerPage", Integer.valueOf(20));
    String orderField = getPara("orderField", "arap.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    Map<String, Object> map = Staff.dao.getStaffArapDetailPage(configName, basePrivs(UNIT_PRIVS), divId, pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection, startTime, endTime, pids);
    


    columnConfig("cw530");
    

    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    setAttr("objectId", selectedObjectId);
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    setAttr("staffName", staffName);
    
    render("detail.html");
  }
  
  public void toDetail()
  {
    Boolean isPost = Boolean.valueOf(getParaToBoolean("isPost") == null ? false : getParaToBoolean("isPost").booleanValue());
    if (isPost.booleanValue())
    {
      setAttr("statusCode", Integer.valueOf(200));
      
      String aimTitle = getPara("aimTitle", "");
      String pattern = getPara("pattern");
      if (pattern.equals("gets")) {
        aimTitle = "职员应收明细";
      } else if (pattern.equals("pays")) {
        aimTitle = "职员应付明细";
      }
      setAttr("aimDlgId", getPara("aimDlgId"));
      setAttr("aimUrl", getPara("aimUrl"));
      setAttr("aimTitle", aimTitle);
      setAttr("aimWidth", getPara("aimWidth"));
      setAttr("aimHeight", getPara("aimHeight"));
      

      Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
      setAttr("data", paraMap);
      
      renderJson();
    }
    else
    {
      Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
      if (paraMap.size() > 0) {
        setAttr("paraMap", paraMap);
      }
      render("optionDialog.html");
    }
  }
  
  public void unitDetail()
    throws SQLException, Exception
  {
    String divId = "staffArapUnitDetaiPage";
    String pattern = getPara("pattern");
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    String staffName = "其他职员";
    String pids = null;
    String configName = loginConfigName();
    if (selectedObjectId.intValue() > 0)
    {
      Staff staff = (Staff)Staff.dao.findById(configName, selectedObjectId);
      staffName = staff.getStr("fullName");
      pids = staff.getStr("pids");
    }
    Integer pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    Integer numPerPage = getParaToInt("numPerPage", Integer.valueOf(10));
    String orderField = getPara("orderField", "arap.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    BigDecimal totalMoney = BigDecimal.ZERO;
    Map<String, Object> map = Staff.dao.getStaffArapUnitDetailPage(configName, divId, pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection, pids, pattern);
    List<Model> rList = (List)map.get("pageList");
    for (Model model : rList) {
      totalMoney = BigDecimalUtils.add(totalMoney, (BigDecimal)model.get("arapMoney"));
    }
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    setAttr("totalMoney", totalMoney);
    setAttr("pattern", pattern);
    setAttr("objectId", selectedObjectId);
    setAttr("staffName", staffName);
    
    render("uniotDetail.html");
  }
  
  public void printDetail()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "职员应收应付详情");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("金额");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "arap.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    
    String pids = null;
    if (selectedObjectId.intValue() > 0)
    {
      Staff staff = (Staff)Staff.dao.findById(configName, selectedObjectId);
      
      pids = staff.getStr("pids");
    }
    Map<String, Object> pageMap = Staff.dao.getStaffArapDetailPage(configName, basePrivs(UNIT_PRIVS), "", pageNum, numPerPage, orderField, orderDirection, startTime, endTime, pids);
    

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
      
      Unit unit = (Unit)record.get("bu");
      rowData.add(trimNull(unit.get("code")));
      rowData.add(trimNull(unit.get("fullName")));
      rowData.add(trimNull(record.get("money")));
      
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
