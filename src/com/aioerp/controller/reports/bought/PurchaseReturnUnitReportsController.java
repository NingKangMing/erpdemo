package com.aioerp.controller.reports.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Unit;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.reports.bought.PurchaseReturnUnitReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PurchaseReturnUnitReportsController
  extends BaseController
{
  public void index()
  {
    setAttr("startDate", DateUtils.getFirstDateOfMonth());
    setAttr("endDate", DateUtils.getCurrentDate());
    setAttr("supId", getParaToInt("supId"));
    render("dialogSearch.html");
  }
  
  public void tree()
  {
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    List<Model> cats = Unit.dao.getParentObjects(loginConfigName(), basePrivs(UNIT_PRIVS), supId);
    Iterator<Model> iter = cats.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model product = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", product.get("id"));
      node.put("pId", product.get("supId"));
      node.put("name", product.get("fullName"));
      node.put("url", "reports/bought/return/unit/search/tree-" + product.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(supId));
    node.put("pId", Integer.valueOf(supId));
    node.put("name", "单位信息");
    node.put("url", "reports/bought/return/unit/search/tree-" + supId);
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public void search()
  {
    String configName = loginConfigName();
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), getPara("startDate"), getPara("endDate"));
    }
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String type = getPara(0, "first");
    
    String productName = getPara("product.fullName");
    String staffName = getPara("staff.name");
    String unitName = getPara("unit.fullName");
    String storageName = getPara("storage.fullName");
    String isRow = getPara("isRow", "false");
    if ("row".equals(type)) {
      isRow = "true";
    }
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    if ("tree".equals(type)) {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
    }
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("productId", getParaToInt("product.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("supId", Integer.valueOf(supId));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("isRow", isRow);
    
    map.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    
    boolean flag = isInitReportOtherCondition(map);
    if (flag) {
      map.put("shoeType", null);
    }
    Map<String, Object> pageMap = PurchaseReturnUnitReports.dao.getPage(configName, basePrivs(UNIT_PRIVS), pageNum, numPerPage, "purchaseReturnUnitStatisticsList", map);
    List<Model> list = (List)pageMap.get("pageList");
    
    BigDecimal moneys = BigDecimal.ZERO;
    BigDecimal returnMoneys = BigDecimal.ZERO;
    for (Model model : list)
    {
      moneys = BigDecimalUtils.add(moneys, model.getBigDecimal("money"));
      returnMoneys = BigDecimalUtils.add(returnMoneys, model.getBigDecimal("discountMoney"));
    }
    Unit unit = (Unit)Unit.dao.findById(configName, getParaToInt("unit.id"));
    if (unit != null) {
      setAttr("node", unit.getInt("node"));
    }
    setAttr("baseAmounts", moneys);
    setAttr("discountMoneys", returnMoneys);
    setAttr("isRow", isRow);
    setAttr("productName", productName);
    setAttr("staffName", staffName);
    setAttr("unitName", unitName);
    setAttr("storageName", storageName);
    setAttr("params", map);
    setAttr("pageMap", pageMap);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg504", AioConstants.STATUS_ENABLE));
    if ("first".equals(type)) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "单位进货退货统计");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startDate", "") + "至" + getPara("endDate", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    

    colTitle.add("入库金额");
    colTitle.add("退货金额");
    colTitle.add("退货率");
    
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
    
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    String isRow = getPara("isRow", "false");
    
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("productId", getParaToInt("product.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("supId", Integer.valueOf(supId));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("isRow", isRow);
    
    map.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    
    boolean flag = isInitReportOtherCondition(map);
    if (flag) {
      map.put("shoeType", null);
    }
    Map<String, Object> pageMap = PurchaseReturnUnitReports.dao.getPage(loginConfigName(), basePrivs(UNIT_PRIVS), 1, totalCount, "purchaseReturnUnitStatisticsList", map);
    List<Model> list = (List)pageMap.get("pageList");
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < list.size(); i++)
    {
      PurchaseReturnUnitReports model = (PurchaseReturnUnitReports)list.get(i);
      rowData.add(trimNull(i + 1));
      
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("fullName")));
      if (!hasCostPermitted) {
        rowData.add("***");
      } else {
        rowData.add(trimNull(model.get("money")));
      }
      if (!hasCostPermitted) {
        rowData.add("***");
      } else {
        rowData.add(trimNull(model.get("returnMoney")));
      }
      rowData.add(trimNull(model.get("returnRate")));
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
