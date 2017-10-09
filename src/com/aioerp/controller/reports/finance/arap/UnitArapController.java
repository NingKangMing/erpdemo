package com.aioerp.controller.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Area;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.aioerp.model.reports.finance.arap.ArapRecords;
import com.aioerp.model.sys.BillType;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UnitArapController
  extends BaseController
{
  public void searcDialog()
  {
    Boolean isPost = Boolean.valueOf(getParaToBoolean("isPost") == null ? false : getParaToBoolean("isPost").booleanValue());
    if (isPost.booleanValue())
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
    else
    {
      render("searchDialog.html");
    }
  }
  
  public void toDetail()
    throws ParseException
  {
    String configName = loginConfigName();
    Boolean isPost = Boolean.valueOf(getParaToBoolean("isPost") == null ? false : getParaToBoolean("isPost").booleanValue());
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    if (isPost.booleanValue())
    {
      setAttr("statusCode", Integer.valueOf(200));
      
      String aimTabId = getPara("aimTabId", "");
      String aimUrl = getPara("aimUrl", "");
      String aimTitle = getPara("aimTitle", "");
      if (getPara("userSearchDate", "").equals("checked")) {
        SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), getPara("startTime"), getPara("endTime"));
      }
      String pattern = getPara("pattern");
      if (pattern.equals("gets"))
      {
        aimTabId = "unitGetsDetailView";
        aimUrl = aimUrl + "getsDetail";
        aimTitle = "单位应收明细账本";
      }
      else if (pattern.equals("pays"))
      {
        aimTabId = "unitPaysDetailView";
        aimUrl = aimUrl + "paysDetail";
        aimTitle = "单位应付明细账本";
      }
      Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
      paraMap.remove("aimTabId");
      paraMap.remove("aimUrl");
      paraMap.remove("aimTitle");
      if (paraMap.size() > 0) {
        setAttr("data", paraMap);
      }
      setAttr("aimTabId", aimTabId);
      setAttr("aimUrl", aimUrl);
      setAttr("aimTitle", aimTitle);
      renderJson();
    }
    else
    {
      setAttr("selectedObjectId", selectedObjectId);
      



      setStartDateAndEndDate();
      render("detailOption.html");
    }
  }
  
  private static String listID = "unitArapList";
  private static String detailListId = "arapDetailList";
  
  public void index()
  {
    String configName = loginConfigName();
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer isSearch = getParaToInt("isSearch", Integer.valueOf(0));
    Integer isLine = getParaToInt(1, Integer.valueOf(0));
    Integer node = Integer.valueOf(0);
    if (isLine.intValue() != 0) {
      node = Integer.valueOf(1);
    }
    String unitPids = getPara("unit.pids", "{0}");
    String unitName = getPara("unit.fullName", "全部单位");
    Integer areaId = getParaToInt("area.id", Integer.valueOf(0));
    if (((!"".equals(unitPids)) && (!"{0}".equals(unitPids))) || (areaId.intValue() != 0) || (isLine.intValue() != 0))
    {
      isSearch = Integer.valueOf(1);
      Model m = Unit.dao.getModelByPids(configName, "b_unit", unitPids);
      if (m != null) {
        unitName = m.getStr("fullName");
      }
    }
    Map<String, Object> pageMap = Unit.dao.getUnitArapPage(configName, basePrivs(UNIT_PRIVS), listID, pageNum, numPerPage, orderField, orderDirection, Integer.valueOf(supId), null, node, unitPids, areaId);
    

    columnConfig("cw520");
    
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", node);
    setAttr("isLine", isLine);
    
    setAttr("pageMap", pageMap);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    setAttr("isSearch", isSearch);
    setAttr("unitPids", unitPids);
    setAttr("unitName", unitName);
    setAttr("areaId", areaId);
    
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    
    render("page.html");
  }
  
  public void list()
  {
    String configName = loginConfigName();
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    
    Integer areaId = getParaToInt("area.id", Integer.valueOf(0));
    String unitPids = getPara("unit.pids", "{0}");
    Integer node = getParaToInt("node", Integer.valueOf(0));
    
    Integer pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    Integer numPerPage = getParaToInt("numPerPage", Integer.valueOf(20));
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    

    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      node = Integer.valueOf(0);
    }
    Map<String, Object> map = new HashMap();
    map = Unit.dao.getUnitArapPage(configName, basePrivs(UNIT_PRIVS), listID, pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection, Integer.valueOf(supId), Integer.valueOf(upObjectId), node, unitPids, areaId);
    
    Integer pSupId = Integer.valueOf(0);
    if (supId > 0) {
      pSupId = Integer.valueOf(Unit.dao.getPsupId(configName, supId));
    }
    columnConfig("cw520");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    setAttr("areaId", areaId);
    setAttr("unitPids", unitPids);
    setAttr("pSupId", pSupId);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", node);
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    
    render("list.html");
  }
  
  public void tree()
  {
    String configName = loginConfigName();
    List<Model> cats = Unit.dao.getParentObjects(configName, basePrivs(UNIT_PRIVS));
    Iterator<Model> iter = cats.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model object = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", object.get("id"));
      node.put("pId", object.get("supId"));
      node.put("name", object.get("fullName"));
      node.put("url", "reports/finance/arap/unitArap/list/" + object.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "单位信息");
    node.put("url", "reports/finance/arap/unitArap/list/0");
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public void print()
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
      data.put("reportName", "单位应收应付");
      colTitle.add("单位编号");
      colTitle.add("单位全名");
      
      colTitle.add("单位简名");
      colTitle.add("单位拼音码");
      colTitle.add("单位地址");
      colTitle.add("单位电话");
      colTitle.add("电子邮件");
      colTitle.add("联系人一");
      colTitle.add("手机一");
      colTitle.add("联系人二");
      colTitle.add("手机二");
      colTitle.add("默认经手人");
      colTitle.add("开户银行");
      colTitle.add("银行账号");
      colTitle.add("邮政编码");
      colTitle.add("传真");
      colTitle.add("税号");
      colTitle.add("地区全名");
      
      colTitle.add("应收余额");
      colTitle.add("应付余额");
    }
    else if (m == 2)
    {
      data.put("reportName", "职员应收应付");
      colTitle.add("职员编号");
      colTitle.add("职员全名");
      colTitle.add("应收发生额");
      colTitle.add("应付发生额");
    }
    else if (m == 3)
    {
      data.put("reportName", "部门应收应付");
      colTitle.add("部门编号");
      colTitle.add("部门全名");
      colTitle.add("应收发生额");
      colTitle.add("应付发生额");
    }
    else if (m == 4)
    {
      data.put("reportName", "地区应收应付");
      colTitle.add("地区编号");
      colTitle.add("地区全名名");
      colTitle.add("应收发生额");
      colTitle.add("应付发生额");
    }
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    Integer areaId = getParaToInt("area.id", Integer.valueOf(0));
    String unitPids = getPara("unit.pids", "{0}");
    Integer node = getParaToInt("node", Integer.valueOf(0));
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    Integer pageNum = Integer.valueOf(1);
    Integer numPerPage = Integer.valueOf(totalCount);
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    
    Map<String, Object> pageMap = new HashMap();
    if (m == 1) {
      pageMap = Unit.dao.getUnitArapPage(configName, basePrivs(UNIT_PRIVS), "", pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection, Integer.valueOf(supId), Integer.valueOf(upObjectId), node, unitPids, areaId);
    } else if (m == 2) {
      pageMap = Staff.dao.getStaffArapPage(configName, basePrivs(STAFF_PRIVS), "", pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection, Integer.valueOf(supId), Integer.valueOf(upObjectId), startTime, endTime, node.intValue());
    } else if (m == 3) {
      pageMap = Department.dao.getDeptArapPage(configName, basePrivs(DEPARTMENT_PRIVS), "", pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection, supId, Integer.valueOf(upObjectId), startTime, endTime, node.intValue());
    } else if (m == 4) {
      pageMap = Area.dao.getAreaArapPage(configName, basePrivs(AREA_PRIVS), listID, pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection, supId, null, startTime, endTime, node.intValue());
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
        rowData.add(trimNull(record.get("code")));
        rowData.add(trimNull(record.get("fullName")));
        
        rowData.add(trimNull(record.getStr("smallName")));
        rowData.add(trimNull(record.getStr("spell")));
        rowData.add(trimNull(record.getStr("address")));
        rowData.add(trimNull(record.getStr("phone")));
        rowData.add(trimNull(record.getStr("email")));
        rowData.add(trimNull(record.getStr("contact1")));
        rowData.add(trimNull(record.getStr("mobile1")));
        rowData.add(trimNull(record.getStr("contact2")));
        rowData.add(trimNull(record.getStr("mobile2")));
        rowData.add(trimNull(record.getStr("staffName")));
        rowData.add(trimNull(record.getStr("bank")));
        rowData.add(trimNull(record.getStr("bankAccount")));
        rowData.add(trimNull(record.getStr("zipCode")));
        rowData.add(trimNull(record.getStr("fax")));
        rowData.add(trimNull(record.getStr("tariff")));
        rowData.add(trimNull(record.getStr("areaFullName2")));
        
        rowData.add(trimNull(record.get("gets")));
        rowData.add(trimNull(record.get("pays")));
      }
      else if (m == 2)
      {
        Staff staff = (Staff)record.get("bs");
        rowData.add(trimNull(staff.get("code")));
        rowData.add(trimNull(record.get("staffName")));
        rowData.add(trimNull(record.get("getsOccur")));
        rowData.add(trimNull(record.get("paysOccur")));
      }
      else if (m == 3)
      {
        Department dept = (Department)record.get("dept");
        rowData.add(trimNull(dept.get("code")));
        rowData.add(trimNull(record.get("deptName")));
        rowData.add(trimNull(record.get("getsOccur")));
        rowData.add(trimNull(record.get("paysOccur")));
      }
      else if (m == 4)
      {
        Area area = (Area)record.get("area");
        rowData.add(trimNull(area.get("code")));
        rowData.add(trimNull(record.get("areaName")));
        rowData.add(trimNull(record.get("getsOccur")));
        rowData.add(trimNull(record.get("paysOccur")));
      }
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public void getsDetail()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Integer arapType = Integer.valueOf(AioConstants.PAY_TYLE0);
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "arap.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    Unit unit = (Unit)Unit.dao.findById(configName, selectedObjectId);
    String unitPids = unit.getStr("pids");
    String unitName = unit.getStr("fullName");
    
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    Integer isRcw = getParaToInt("isRcw", Integer.valueOf(-1));
    

    BigDecimal bfArapMoney = new BigDecimal(0);
    BigDecimal bfPrepayMoney = new BigDecimal(0);
    ArapRecords arapRecords = ArapRecords.dao.getBeforeFirst(configName, basePrivs(UNIT_PRIVS), startTime, unitPids, arapType);
    if (arapRecords != null)
    {
      bfArapMoney = arapRecords.getBigDecimal("bfArapMoney");
      bfPrepayMoney = arapRecords.getBigDecimal("bfPrepayMoney");
    }
    setAttr("bfArapMoney", bfArapMoney);
    setAttr("bfPrepayMoney", bfPrepayMoney);
    

    Map<String, Object> map = ArapRecords.dao.getPageByUnitPids(configName, unitPids, startTime, endTime, isRcw, detailListId, pageNum, numPerPage, orderField, orderDirection);
    List<Model> rList = (List)map.get("pageList");
    BigDecimal arapMoney = bfArapMoney;
    BigDecimal prepayMoney = bfPrepayMoney;
    for (Model model : rList)
    {
      ArapRecords arapRecode = (ArapRecords)model.get("arap");
      BigDecimal addMoney = arapRecode.getBigDecimal("addMoney");
      BigDecimal subMoney = arapRecode.getBigDecimal("subMoney");
      BigDecimal addPrepay = arapRecode.getBigDecimal("addPrepay");
      BigDecimal subPrepay = arapRecode.getBigDecimal("subPrepay");
      arapMoney = BigDecimalUtils.add(arapMoney, BigDecimalUtils.sub(addMoney, subMoney));
      prepayMoney = BigDecimalUtils.add(prepayMoney, BigDecimalUtils.sub(addPrepay, subPrepay));
      model.put("arapMoney", arapMoney);
      model.put("prepayMoney", prepayMoney);
    }
    setAttr("selectedObjectId", selectedObjectId);
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    setAttr("isRcw", isRcw);
    setAttr("unitPids", unitPids);
    setAttr("unitName", unitName);
    
    setAttr("arapType", arapType);
    

    columnConfig("cw528");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("arapDetail.html");
  }
  
  public void getsDetailList()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Integer arapType = Integer.valueOf(AioConstants.PAY_TYLE0);
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "arap.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    String unitPids = getPara("unitPids");
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    Integer isRcw = getParaToInt("isRcw", Integer.valueOf(-1));
    

    Map<String, Object> map = ArapRecords.dao.getPageByUnitPids(configName, unitPids, startTime, endTime, isRcw, detailListId, pageNum, numPerPage, orderField, orderDirection);
    List<Model> rList = (List)map.get("pageList");
    BigDecimal arapMoney = new BigDecimal(0);
    BigDecimal prepayMoney = new BigDecimal(0);
    ArapRecords firstArapRecode;
    if (rList.size() > 0)
    {
      Model firstModel = (Model)rList.get(0);
      firstArapRecode = (ArapRecords)firstModel.get("arap");
      String time = firstArapRecode.getTimestamp("recodeTime").toString();
      ArapRecords arapRecords = ArapRecords.dao.getBeforeFirst(configName, basePrivs(UNIT_PRIVS), time, unitPids, arapType);
      if (arapRecords != null)
      {
        arapMoney = arapRecords.getBigDecimal("bfArapMoney");
        prepayMoney = arapRecords.getBigDecimal("bfPrepayMoney");
      }
    }
    for (Model model : rList)
    {
      ArapRecords arapRecode = (ArapRecords)model.get("arap");
      BigDecimal addMoney = arapRecode.getBigDecimal("addMoney");
      BigDecimal subMoney = arapRecode.getBigDecimal("subMoney");
      BigDecimal addPrepay = arapRecode.getBigDecimal("addPrepay");
      BigDecimal subPrepay = arapRecode.getBigDecimal("subPrepay");
      arapMoney = BigDecimalUtils.add(arapMoney, BigDecimalUtils.sub(addMoney, subMoney));
      prepayMoney = BigDecimalUtils.add(prepayMoney, BigDecimalUtils.sub(addPrepay, subPrepay));
      model.put("arapMoney", arapMoney);
      model.put("prepayMoney", prepayMoney);
    }
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    setAttr("selectedObjectId", selectedObjectId);
    
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    setAttr("isRcw", isRcw);
    setAttr("unitPids", unitPids);
    
    setAttr("arapType", arapType);
    

    columnConfig("cw528");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    render("arapDetailList.html");
  }
  
  public void paysDetail()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Integer arapType = Integer.valueOf(AioConstants.PAY_TYLE1);
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "arap.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    Unit unit = (Unit)Unit.dao.findById(configName, selectedObjectId);
    String unitPids = unit.getStr("pids");
    String unitName = unit.getStr("fullName");
    
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    Integer isRcw = getParaToInt("isRcw", Integer.valueOf(-1));
    

    BigDecimal bfArapMoney = new BigDecimal(0);
    BigDecimal bfPrepayMoney = new BigDecimal(0);
    ArapRecords arapRecords = ArapRecords.dao.getBeforeFirst(configName, basePrivs(UNIT_PRIVS), startTime, unitPids, arapType);
    if (arapRecords != null)
    {
      bfArapMoney = arapRecords.getBigDecimal("bfArapMoney");
      bfPrepayMoney = arapRecords.getBigDecimal("bfPrepayMoney");
    }
    setAttr("bfArapMoney", bfArapMoney);
    setAttr("bfPrepayMoney", bfPrepayMoney);
    

    Map<String, Object> map = ArapRecords.dao.getPageByUnitPids(configName, unitPids, startTime, endTime, isRcw, detailListId, pageNum, numPerPage, orderField, orderDirection);
    List<Model> rList = (List)map.get("pageList");
    BigDecimal arapMoney = bfArapMoney;
    BigDecimal prepayMoney = bfPrepayMoney;
    for (Model model : rList)
    {
      ArapRecords arapRecode = (ArapRecords)model.get("arap");
      BigDecimal addMoney = arapRecode.getBigDecimal("addMoney");
      BigDecimal subMoney = arapRecode.getBigDecimal("subMoney");
      BigDecimal addPrepay = arapRecode.getBigDecimal("addPrepay");
      BigDecimal subPrepay = arapRecode.getBigDecimal("subPrepay");
      arapMoney = BigDecimalUtils.add(arapMoney, BigDecimalUtils.sub(subMoney, addMoney));
      prepayMoney = BigDecimalUtils.add(prepayMoney, BigDecimalUtils.sub(subPrepay, addPrepay));
      model.put("arapMoney", arapMoney);
      model.put("prepayMoney", prepayMoney);
    }
    setAttr("selectedObjectId", selectedObjectId);
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    setAttr("isRcw", isRcw);
    setAttr("unitPids", unitPids);
    setAttr("unitName", unitName);
    
    setAttr("arapType", arapType);
    

    columnConfig("cw529");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    render("arapDetail.html");
  }
  
  public void paysDetailList()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Integer arapType = Integer.valueOf(AioConstants.PAY_TYLE1);
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "arap.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    String unitPids = getPara("unitPids");
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    Integer isRcw = getParaToInt("isRcw", Integer.valueOf(-1));
    

    Map<String, Object> map = ArapRecords.dao.getPageByUnitPids(configName, unitPids, startTime, endTime, isRcw, detailListId, pageNum, numPerPage, orderField, orderDirection);
    List<Model> rList = (List)map.get("pageList");
    BigDecimal arapMoney = new BigDecimal(0);
    BigDecimal prepayMoney = new BigDecimal(0);
    ArapRecords firstArapRecode;
    if (rList.size() > 0)
    {
      Model firstModel = (Model)rList.get(0);
      firstArapRecode = (ArapRecords)firstModel.get("arap");
      String time = firstArapRecode.getTimestamp("recodeTime").toString();
      ArapRecords arapRecords = ArapRecords.dao.getBeforeFirst(configName, basePrivs(UNIT_PRIVS), time, unitPids, arapType);
      if (arapRecords != null)
      {
        arapMoney = arapRecords.getBigDecimal("bfArapMoney");
        prepayMoney = arapRecords.getBigDecimal("bfPrepayMoney");
      }
    }
    for (Model model : rList)
    {
      ArapRecords arapRecode = (ArapRecords)model.get("arap");
      BigDecimal addMoney = arapRecode.getBigDecimal("addMoney");
      BigDecimal subMoney = arapRecode.getBigDecimal("subMoney");
      BigDecimal addPrepay = arapRecode.getBigDecimal("addPrepay");
      BigDecimal subPrepay = arapRecode.getBigDecimal("subPrepay");
      arapMoney = BigDecimalUtils.add(arapMoney, BigDecimalUtils.sub(subMoney, addMoney));
      prepayMoney = BigDecimalUtils.add(prepayMoney, BigDecimalUtils.sub(subPrepay, addPrepay));
      model.put("arapMoney", arapMoney);
      model.put("prepayMoney", prepayMoney);
    }
    Integer selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    setAttr("selectedObjectId", selectedObjectId);
    
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    setAttr("isRcw", isRcw);
    setAttr("unitPids", unitPids);
    
    setAttr("arapType", arapType);
    

    columnConfig("cw529");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    render("arapDetailList.html");
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
    if (m == 0) {
      data.put("reportName", "单位应收明细账本");
    } else if (m == 1) {
      data.put("reportName", "单位应付明细账本");
    }
    colTitle.add("单据类型");
    colTitle.add("单据字头");
    colTitle.add("单据编号");
    colTitle.add("录单时间");
    colTitle.add("摘要");
    colTitle.add("增加金额");
    colTitle.add("减少金额");
    colTitle.add("应收金额");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "arap.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    String unitPids = getPara("unitPids");
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    Integer isRcw = getParaToInt("isRcw", Integer.valueOf(-1));
    
    Integer arapType = Integer.valueOf(0);
    Map<String, Object> pageMap = new HashMap();
    if (m == 0)
    {
      arapType = Integer.valueOf(AioConstants.PAY_TYLE0);
      pageMap = ArapRecords.dao.getPageByUnitPids(configName, unitPids, startTime, endTime, isRcw, "", pageNum, numPerPage, orderField, orderDirection);
    }
    else if (m == 1)
    {
      arapType = Integer.valueOf(AioConstants.PAY_TYLE1);
      pageMap = ArapRecords.dao.getPageByUnitPids(configName, unitPids, startTime, endTime, isRcw, "", pageNum, numPerPage, orderField, orderDirection);
    }
    List<Model> list = (List)pageMap.get("pageList");
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    BigDecimal arapMoney = new BigDecimal(0);
    BigDecimal prepayMoney = new BigDecimal(0);
    String time;
    if (list.size() > 0)
    {
      Model firstModel = (Model)list.get(0);
      ArapRecords firstArapRecode = (ArapRecords)firstModel.get("arap");
      time = firstArapRecode.getTimestamp("recodeTime").toString();
      ArapRecords arapRecords = ArapRecords.dao.getBeforeFirst(configName, basePrivs(UNIT_PRIVS), time, unitPids, arapType);
      if (arapRecords != null)
      {
        arapMoney = arapRecords.getBigDecimal("bfArapMoney");
        prepayMoney = arapRecords.getBigDecimal("bfPrepayMoney");
      }
    }
    int i = 1;
    for (Model model : list)
    {
      ArapRecords arapRecode = (ArapRecords)model.get("arap");
      BigDecimal addMoney = arapRecode.getBigDecimal("addMoney");
      BigDecimal subMoney = arapRecode.getBigDecimal("subMoney");
      BigDecimal addPrepay = arapRecode.getBigDecimal("addPrepay");
      BigDecimal subPrepay = arapRecode.getBigDecimal("subPrepay");
      arapMoney = BigDecimalUtils.add(arapMoney, BigDecimalUtils.sub(addMoney, subMoney));
      prepayMoney = BigDecimalUtils.add(prepayMoney, BigDecimalUtils.sub(addPrepay, subPrepay));
      model.put("arapMoney", arapMoney);
      model.put("prepayMoney", prepayMoney);
      
      BillType bType = (BillType)model.get("bt");
      
      rowData.add(trimNull(i));
      rowData.add(trimNull(bType.get("name")));
      rowData.add(trimNull(bType.get("prefix")));
      rowData.add(trimNull(arapRecode.get("billCode")));
      rowData.add(trimNull(arapRecode.get("recodeTime")));
      rowData.add(trimNull(arapRecode.get("billAbstract")));
      if (arapType.intValue() == 0)
      {
        rowData.add(trimNull(arapRecode.get("addMoney")));
        rowData.add(trimNull(arapRecode.get("subMoney")));
      }
      else if (arapType.intValue() == 1)
      {
        rowData.add(trimNull(arapRecode.get("subMoney")));
        rowData.add(trimNull(arapRecode.get("addMoney")));
      }
      rowData.add(trimNull(model.get("arapMoney")));
      
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
