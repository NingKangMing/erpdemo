package com.aioerp.controller.reports.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Product;
import com.aioerp.model.bought.PurchaseReturnBill;
import com.aioerp.model.bought.PurchaseReturnDetail;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.reports.bought.PurchaseReturnReports;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PurchaseReturnReportsController
  extends BaseController
{
  public void index()
    throws ParseException
  {
    setStartDateAndEndDate();
    setAttr("supId", getParaToInt("supId"));
    render("dialogSearch.html");
  }
  
  public void tree()
  {
    String configName = loginConfigName();
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    List<Model> cats = Product.dao.getTreeEnableList(configName, basePrivs(PRODUCT_PRIVS), supId);
    Iterator<Model> iter = cats.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model product = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", product.get("id"));
      node.put("pId", product.get("supId"));
      node.put("name", product.get("fullName"));
      node.put("url", "reports/bought/return/search/tree-" + product.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(supId));
    node.put("pId", Integer.valueOf(supId));
    node.put("name", "商品信息");
    node.put("url", "reports/bought/return/search/tree-" + supId);
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
    String unitName = getPara("unit.fullName");
    String staffName = getPara("staff.name");
    String storageName = getPara("storage.fullName");
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    String isRow = getPara("isRow", "false");
    if ("row".equals(type)) {
      isRow = "true";
    }
    if ("tree".equals(type)) {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
    }
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("productId", getParaToInt("product.id"));
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
    Map<String, Object> pageMap = PurchaseReturnReports.dao.getPage(configName, basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, "purchaseReturnStatisticsList", map);
    List<Model> list = (List)pageMap.get("pageList");
    
    BigDecimal baseAmounts = BigDecimal.ZERO;
    BigDecimal moneys = BigDecimal.ZERO;
    BigDecimal returnAmounts = BigDecimal.ZERO;
    BigDecimal returnMoneys = BigDecimal.ZERO;
    BigDecimal putAmounts = BigDecimal.ZERO;
    BigDecimal putMoneys = BigDecimal.ZERO;
    for (Model model : list)
    {
      baseAmounts = BigDecimalUtils.add(baseAmounts, model.getBigDecimal("baseAmount"));
      moneys = BigDecimalUtils.add(moneys, model.getBigDecimal("money"));
      returnAmounts = BigDecimalUtils.add(returnAmounts, model.getBigDecimal("returnAmounts"));
      returnMoneys = BigDecimalUtils.add(returnMoneys, model.getBigDecimal("returnMoney"));
      putAmounts = BigDecimalUtils.add(putAmounts, model.getBigDecimal("putAmount"));
      putMoneys = BigDecimalUtils.add(putMoneys, model.getBigDecimal("putMoney"));
    }
    Product product = (Product)Product.dao.findById(configName, getParaToInt("product.id"));
    if (product != null) {
      setAttr("node", product.getInt("node"));
    }
    setAttr("isRow", isRow);
    setAttr("baseAmounts", baseAmounts);
    setAttr("moneys", moneys);
    setAttr("returnAmounts", returnAmounts);
    setAttr("returnMoneys", returnMoneys);
    setAttr("putAmounts", putAmounts);
    setAttr("putMoneys", putMoneys);
    setAttr("productName", productName);
    setAttr("unitName", unitName);
    setAttr("staffName", staffName);
    setAttr("storageName", storageName);
    setAttr("params", map);
    setAttr("pageMap", pageMap);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg503", AioConstants.STATUS_ENABLE));
    if ("first".equals(type)) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void look()
  {
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    PurchaseReturnBill bill = (PurchaseReturnBill)PurchaseReturnBill.dao.findById(configName, Integer.valueOf(id));
    List<Model> detailList = PurchaseReturnDetail.dao.getListByBillId(configName, Integer.valueOf(id), basePrivs(PRODUCT_PRIVS), basePrivs(STORAGE_PRIVS));
    detailList = addTrSize(detailList, 15);
    

    setPayTypeAttr(configName, "saveBill", AioConstants.BILL_ROW_TYPE6, bill.getInt("id").intValue());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE6, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE6));
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, AioConstants.BILL_ROW_TYPE6, "discount")));
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE6, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    render("look.html");
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "商品进货退货统计");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startDate", "") + "至" + getPara("endDate", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简称");
    colTitle.add("商品拼音");
    colTitle.add("规格");
    colTitle.add("型号");
    colTitle.add("产地");
    
    colTitle.add("辅助单位");
    colTitle.add("辅助进货数量");
    colTitle.add("进货数量");
    colTitle.add("进货金额");
    colTitle.add("入库数量");
    colTitle.add("入库金额");
    colTitle.add("退货数量");
    colTitle.add("退货金额");
    colTitle.add("退货率");
    
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
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("productId", getParaToInt("product.id"));
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
    Map<String, Object> pageMap = PurchaseReturnReports.dao.getPage(loginConfigName(), basePrivs(PRODUCT_PRIVS), 1, totalCount, "purchaseReturnStatisticsList", map);
    List<Model> list = (List)pageMap.get("pageList");
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < list.size(); i++)
    {
      PurchaseReturnReports model = (PurchaseReturnReports)list.get(i);
      rowData.add(trimNull(i + 1));
      
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("fullName")));
      rowData.add(trimNull(model.get("smallName")));
      rowData.add(trimNull(model.get("spell")));
      rowData.add(trimNull(model.get("standard")));
      rowData.add(trimNull(model.get("model")));
      rowData.add(trimNull(model.get("field")));
      
      rowData.add(trimNull(model.get("assistUnit")));
      rowData.add(trimNull(model.getHelpAmount()));
      rowData.add(trimNull(model.get("baseAmount")));
      if (!hasCostPermitted) {
        rowData.add("***");
      } else {
        rowData.add(trimNull(model.get("money")));
      }
      rowData.add(trimNull(model.get("putAmount")));
      if (!hasCostPermitted) {
        rowData.add("***");
      } else {
        rowData.add(trimNull(model.get("putMoney")));
      }
      rowData.add(trimNull(model.get("returnAmount")));
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
