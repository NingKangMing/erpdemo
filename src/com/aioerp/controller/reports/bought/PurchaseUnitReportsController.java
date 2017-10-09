package com.aioerp.controller.reports.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Unit;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.reports.bought.PurchaseUnitReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.CollectionUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PurchaseUnitReportsController
  extends BaseController
{
  private static final String LISTID = "purchaseUnitSpreadList";
  
  public void index()
    throws ParseException
  {
    setStartDateAndEndDate();
    setAttr("supId", getParaToInt("supId"));
    setAttr("shoeType", getParaToInt("shoeType"));
    render("dialogSearch.html");
  }
  
  public void tree()
  {
    List<Model> cats = Unit.dao.getParentObjects(loginConfigName(), basePrivs(UNIT_PRIVS));
    Iterator<Model> iter = cats.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model product = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", product.get("id"));
      node.put("pId", product.get("supId"));
      node.put("name", product.get("fullName"));
      node.put("url", "reports/bought/purchase/unit/search/tree-" + product.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "单位信息");
    node.put("url", "reports/bought/purchase/unit/search/tree-0");
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
    String storageName = getPara("storage.fullName");
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    String isRow = getPara("isRow", "false");
    if ("row".equals(type)) {
      isRow = "true";
    }
    String[] orderTypes = null;
    if ("first".equals(type))
    {
      orderTypes = getParaValues("orderTypes");
      String bills = getPara("orderTypes", "");
      if (bills.indexOf(",") != -1) {
        orderTypes = getPara("orderTypes", "").split(",");
      }
    }
    else if ("tree".equals(type))
    {
      orderTypes = getPara("orderTypes", "").split(",");
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
    }
    else
    {
      orderTypes = getPara("orderTypes", "").split(",");
    }
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("productId", getParaToInt("product.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("supId", Integer.valueOf(supId));
    map.put("orderTypes", orderTypes);
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("shoeType", getParaToInt("shoeType"));
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
    Map<String, Object> pageMap = PurchaseUnitReports.dao.getPage(configName, basePrivs(UNIT_PRIVS), pageNum, numPerPage, "purchaseUnitStatisticsList", map);
    List<Model> list = (List)pageMap.get("pageList");
    BigDecimal baseAmounts = BigDecimal.ZERO;
    BigDecimal discountMoneys = BigDecimal.ZERO;
    BigDecimal taxess = BigDecimal.ZERO;
    BigDecimal taxMoneys = BigDecimal.ZERO;
    BigDecimal giftAmounts = BigDecimal.ZERO;
    BigDecimal retailMoneys = BigDecimal.ZERO;
    BigDecimal severals = BigDecimal.ZERO;
    for (Model model : list)
    {
      baseAmounts = BigDecimalUtils.add(baseAmounts, model.getBigDecimal("baseAmount"));
      discountMoneys = BigDecimalUtils.add(discountMoneys, model.getBigDecimal("discountMoney"));
      taxess = BigDecimalUtils.add(taxess, model.getBigDecimal("taxes"));
      taxMoneys = BigDecimalUtils.add(taxMoneys, model.getBigDecimal("taxMoney"));
      giftAmounts = BigDecimalUtils.add(giftAmounts, model.getBigDecimal("giftAmount"));
      retailMoneys = BigDecimalUtils.add(retailMoneys, model.getBigDecimal("retailMoney"));
      severals = BigDecimalUtils.add(severals, model.getBigDecimal("several"));
    }
    setAttr("baseAmounts", baseAmounts);
    setAttr("discountMoneys", discountMoneys);
    setAttr("taxess", taxess);
    setAttr("taxMoneys", taxMoneys);
    setAttr("giftAmounts", giftAmounts);
    setAttr("retailMoneys", retailMoneys);
    setAttr("severals", severals);
    if (orderTypes != null) {
      setAttr("orderTypes", CollectionUtils.strArrToString(orderTypes, ","));
    }
    setAttr("isRow", isRow);
    setAttr("productName", productName);
    setAttr("staffName", staffName);
    setAttr("storageName", storageName);
    setAttr("params", map);
    setAttr("pageMap", pageMap);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg502", AioConstants.STATUS_ENABLE));
    if ("first".equals(type)) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void detail()
    throws Exception
  {
    String configName = loginConfigName();
    int pageNum = AioConstants.START_PAGE;
    int numPerPage = 20;
    String[] orderTypes = getPara("orderTypes").split(",");
    boolean checkout = getParaToBoolean("checkout", Boolean.valueOf(false)).booleanValue();
    int unitId = getParaToInt(0, Integer.valueOf(0)).intValue();
    BigDecimal amount = new BigDecimal(getPara("baseAmount", "0"));
    if (amount == null) {
      amount = new BigDecimal(getPara(2, "0"));
    }
    if (unitId == 0) {
      unitId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    }
    if (unitId == 0) {
      unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
    }
    if (((orderTypes == null) || (orderTypes.length == 0) || ("".equals(orderTypes[0])) || (amount == null)) && (checkout))
    {
      setAttr("message", "数据库中没有符合要求的数据!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    if (checkout)
    {
      renderJson();
      return;
    }
    Unit unit = (Unit)Unit.dao.findById(configName, Integer.valueOf(unitId));
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("unitId", Integer.valueOf(unitId));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("productId", getParaToInt("product.id"));
    map.put("orderTypes", orderTypes);
    map.put("notStatus", getParaToInt("notStatus"));
    
    map.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    
    Map<String, Object> pageMap = PurchaseUnitReports.dao.getDetailPage(configName, basePrivs(UNIT_PRIVS), basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, "purchaseUnitDetailAccountList", map);
    if ((pageMap == null) && (checkout))
    {
      setAttr("message", "数据库中没有符合要求的数据!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    if (checkout)
    {
      renderJson();
      return;
    }
    List<Model> list = (List)pageMap.get("pageList");
    BigDecimal pAmounts = BigDecimal.ZERO;
    BigDecimal rAmounts = BigDecimal.ZERO;
    BigDecimal moneys = BigDecimal.ZERO;
    for (Model model : list)
    {
      pAmounts = BigDecimalUtils.add(pAmounts, model.getBigDecimal("pAmount"));
      rAmounts = BigDecimalUtils.add(rAmounts, model.getBigDecimal("rAmount"));
      moneys = BigDecimalUtils.add(moneys, model.getBigDecimal("money"));
    }
    setAttr("pAmounts", pAmounts);
    setAttr("rAmounts", rAmounts);
    setAttr("moneys", moneys);
    setAttr("params", map);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg501", AioConstants.STATUS_ENABLE));
    if (unit != null) {
      setAttr("unitName", unit.getStr("fullName"));
    }
    setAttr("pageMap", pageMap);
    if (orderTypes != null) {
      setAttr("orderTypes", CollectionUtils.strArrToString(orderTypes, ","));
    }
    render("detail.html");
  }
  
  public void detailSearch()
    throws Exception
  {
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String configName = loginConfigName();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String[] orderTypes = getPara("orderTypes").split(",");
    String[] unitIds = getPara("unitIds", "").split(",");
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("unitId", getParaToInt("unitId"));
    map.put("orderTypes", orderTypes);
    map.put("notStatus", getParaToInt("notStatus"));
    map.put("staffId", getParaToInt("staffId"));
    map.put("storageId", getParaToInt("storageId"));
    map.put("productId", getParaToInt("productId"));
    map.put("unitIds", unitIds);
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    
    map.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    
    Map<String, Object> pageMap = PurchaseUnitReports.dao.getDetailPage(configName, basePrivs(UNIT_PRIVS), basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, "purchaseUnitDetailAccountList", map);
    
    List<Model> list = (List)pageMap.get("pageList");
    BigDecimal pAmounts = BigDecimal.ZERO;
    BigDecimal rAmounts = BigDecimal.ZERO;
    BigDecimal moneys = BigDecimal.ZERO;
    for (Model model : list)
    {
      pAmounts = BigDecimalUtils.add(pAmounts, model.getBigDecimal("pAmount"));
      rAmounts = BigDecimalUtils.add(rAmounts, model.getBigDecimal("rAmount"));
      moneys = BigDecimalUtils.add(moneys, model.getBigDecimal("money"));
    }
    setAttr("pAmounts", pAmounts);
    setAttr("rAmounts", rAmounts);
    setAttr("moneys", moneys);
    setAttr("params", map);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg501", AioConstants.STATUS_ENABLE));
    if (orderTypes != null) {
      setAttr("orderTypes", CollectionUtils.strArrToString(orderTypes, ","));
    }
    setAttr("pageMap", pageMap);
    render("detailList.html");
  }
  
  public void toSpread()
    throws ParseException
  {
    setStartDateAndEndDate();
    setAttr("supId", getParaToInt("supId"));
    setAttr("spreadWay", getPara("spreadWay"));
    render("spreadSearch.html");
  }
  
  public void spread()
    throws Exception
  {
    String configName = loginConfigName();
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), getPara("startDate"), getPara("endDate"));
    }
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String[] unitIds = null;
    Integer supId = getParaToInt("supId", Integer.valueOf(0));
    String storageName = getPara("storage.fullName");
    String staffName = getPara("staff.name");
    String type = getPara(0, "first");
    String isRow = getPara("isRow", "false");
    if ("row".equals(type)) {
      isRow = "true";
    }
    String[] orderTypes = null;
    if ("first".equals(type))
    {
      orderTypes = getParaValues("orderTypes");
      String bills = getPara("orderTypes", "");
      if (bills.indexOf(",") != -1) {
        orderTypes = getPara("orderTypes", "").split(",");
      }
      unitIds = getParaValues("unitIds");
      String units = getPara("unitIds", "");
      if (units.indexOf(",") != -1) {
        unitIds = getPara("unitIds", "").split(",");
      }
    }
    else
    {
      orderTypes = getPara("orderTypes", "").split(",");
      unitIds = getPara("unitIds", "").split(",");
    }
    if ("tree".equals(type))
    {
      supId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    }
    else if ("up".equals(type))
    {
      int oldSupId = getParaToInt("supId").intValue();
      supId = Integer.valueOf(Product.dao.getPsupId(configName, oldSupId));
    }
    Product product = (Product)Product.dao.findById(configName, supId);
    List<Model> unitList = Unit.dao.getChilds(configName, basePrivs(UNIT_PRIVS), unitIds);
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("orderTypes", orderTypes);
    map.put("unitList", unitList);
    map.put("supId", supId);
    map.put("spreadWay", getPara("spreadWay"));
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
    Map<String, Object> pageMap = PurchaseUnitReports.dao.getSpreadPage(configName, pageNum, numPerPage, "purchaseUnitSpreadList", map);
    setAttr("params", map);
    setAttr("unitList", unitList);
    setAttr("storageName", storageName);
    setAttr("staffName", staffName);
    if (product != null) {
      setAttr("productName", product.get("fullName"));
    }
    if (orderTypes != null) {
      setAttr("orderTypes", CollectionUtils.strArrToString(orderTypes, ","));
    }
    if (unitIds != null) {
      setAttr("unitIds", CollectionUtils.strArrToString(unitIds, ","));
    }
    setAttr("supId", supId);
    setAttr("isRow", isRow);
    setAttr("pageMap", pageMap);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5011", AioConstants.STATUS_ENABLE));
    if (type == "first") {
      render("spread.html");
    } else {
      render("spreadList.html");
    }
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "单位进货统计");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startDate", "") + "至" + getPara("endDate", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    
    colTitle.add("进货数量");
    colTitle.add("折后均价");
    colTitle.add("折后金额");
    colTitle.add("含税单价");
    colTitle.add("税额");
    colTitle.add("价税合计");
    colTitle.add("进货次数");
    colTitle.add("赠品数量");
    colTitle.add("赠品零售额");
    
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
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    String[] orderTypes = null;
    String isRow = getPara("isRow", "false");
    
    orderTypes = getPara("orderTypes", "").split(",");
    
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("staffId", getParaToInt("staffId"));
    map.put("storageId", getParaToInt("storageId"));
    map.put("productId", getParaToInt("productId"));
    map.put("supId", Integer.valueOf(supId));
    map.put("orderTypes", orderTypes);
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("shoeType", getParaToInt("shoeType"));
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
    Map<String, Object> pageMap = PurchaseUnitReports.dao.getPage(loginConfigName(), basePrivs(UNIT_PRIVS), 1, totalCount, "purchaseUnitStatisticsList", map);
    List<Model> list = (List)pageMap.get("pageList");
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < list.size(); i++)
    {
      PurchaseUnitReports model = (PurchaseUnitReports)list.get(i);
      rowData.add(trimNull(i + 1));
      
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("fullName")));
      rowData.add(trimNull(model.get("baseAmount")));
      if (!hasCostPermitted)
      {
        rowData.add("***");
        rowData.add("***");
        rowData.add("***");
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        if (model.getBigDecimal("avgPrice") != null) {
          rowData.add(trimNull(model.getBigDecimal("avgPrice").setScale(4, 4)));
        } else {
          rowData.add("");
        }
        rowData.add(trimNull(model.get("discountMoney")));
        if (model.getBigDecimal("taxPrice") != null) {
          rowData.add(trimNull(model.getBigDecimal("taxPrice").setScale(4, 4)));
        } else {
          rowData.add("");
        }
        rowData.add(trimNull(model.get("taxes")));
        rowData.add(trimNull(model.get("taxMoney")));
      }
      rowData.add(trimNull(model.get("several")));
      rowData.add(trimNull(model.get("giftAmount")));
      if (!hasCostPermitted) {
        rowData.add("***");
      } else {
        rowData.add(trimNull(model.get("retailMoney")));
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
    String configName = loginConfigName();
    
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "进货明细账本");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startDate", "") + "至" + getPara("endDate", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("单据类型");
    colTitle.add("日期 ");
    colTitle.add("单据编号");
    colTitle.add("摘要");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简称");
    colTitle.add("商品拼音");
    colTitle.add("规格");
    colTitle.add("型号");
    colTitle.add("产地");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("进货数量");
    colTitle.add("退货数量");
    colTitle.add("单价");
    colTitle.add("金额");
    

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
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    int unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
    String[] orderTypes = getPara("orderTypes", "").split(",");
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("unitId", Integer.valueOf(unitId));
    map.put("staffId", getParaToInt("staffId"));
    map.put("storageId", getParaToInt("storageId"));
    map.put("productId", getParaToInt("productId"));
    map.put("orderTypes", orderTypes);
    map.put("notStatus", getParaToInt("notStatus"));
    
    map.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    map.put(UNIT_PRIVS, basePrivs(UNIT_PRIVS));
    map.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    map.put(DEPARTMENT_PRIVS, basePrivs(DEPARTMENT_PRIVS));
    
    Map<String, Object> pageMap = PurchaseUnitReports.dao.getDetailPage(configName, basePrivs(UNIT_PRIVS), basePrivs(PRODUCT_PRIVS), 1, totalCount, "purchaseUnitDetailAccountList", map);
    List<Model> list = (List)pageMap.get("pageList");
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < list.size(); i++)
    {
      PurchaseUnitReports model = (PurchaseUnitReports)list.get(i);
      rowData.add(trimNull(i + 1));
      Long type = model.getLong("type");
      if ((type != null) && (type.longValue() == AioConstants.BILL_ROW_TYPE5)) {
        rowData.add("进货单");
      } else if ((type != null) && (type.longValue() == AioConstants.BILL_ROW_TYPE6)) {
        rowData.add("进货退货单");
      } else if ((type != null) && (type.longValue() == AioConstants.BILL_ROW_TYPE12)) {
        rowData.add("进货换货单");
      } else {
        rowData.add("");
      }
      rowData.add(trimNull(model.get("recodeDate")));
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("remark")));
      Model product = (Model)model.get("product");
      if (product != null)
      {
        rowData.add(trimNull(product.get("code")));
        rowData.add(trimNull(product.get("fullName")));
        rowData.add(trimNull(product.get("smallName")));
        rowData.add(trimNull(product.get("spell")));
        rowData.add(trimNull(product.get("standard")));
        rowData.add(trimNull(product.get("model")));
        rowData.add(trimNull(product.get("field")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
        rowData.add("");
        rowData.add("");
        rowData.add("");
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(model.get("storageCode")));
      rowData.add(trimNull(model.get("storageName")));
      rowData.add(trimNull(model.get("unitCode")));
      rowData.add(trimNull(model.get("unitName")));
      
      rowData.add(trimNull(model.get("pAmount")));
      rowData.add(trimNull(model.get("rAmount")));
      if (!hasCostPermitted)
      {
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        rowData.add(trimNull(model.get("basePrice")));
        rowData.add(trimNull(model.get("money")));
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
  
  public void printSpread()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String[] unitIds = getPara("unitIds", "").split(",");
    List<Model> unitList = Unit.dao.getChilds(configName, basePrivs(UNIT_PRIVS), unitIds);
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "单位进货分布");
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
    for (Model unit : unitList)
    {
      colTitle.add(unit.getStr("fullName") + "数量");
      colTitle.add(unit.getStr("fullName") + "金额");
    }
    colTitle.add("数量");
    colTitle.add("金额");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    for (int i = 0; i < unitList.size(); i++)
    {
      colWidth.add("500");
      colWidth.add("500");
    }
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    

    Integer supId = getParaToInt("supId", Integer.valueOf(0));
    String isRow = getPara("isRow", "false");
    String[] orderTypes = getPara("orderTypes", "").split(",");
    
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("orderTypes", orderTypes);
    map.put("unitList", unitList);
    map.put("supId", supId);
    map.put("spreadWay", getPara("spreadWay"));
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
    Map<String, Object> pageMap = PurchaseUnitReports.dao.getSpreadPage(configName, 1, totalCount, "purchaseUnitSpreadList", map);
    List<Model> list = (List)pageMap.get("pageList");
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < list.size(); i++)
    {
      Model model = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("fullName")));
      rowData.add(trimNull(model.get("smallName")));
      rowData.add(trimNull(model.get("spell")));
      rowData.add(trimNull(model.get("standard")));
      rowData.add(trimNull(model.get("model")));
      rowData.add(trimNull(model.get("field")));
      for (Model unit : unitList)
      {
        rowData.add(trimNull(model.get("baseAmount" + unit.getInt("id"))));
        if (!hasCostPermitted) {
          rowData.add("***");
        } else {
          rowData.add(trimNull(model.get("money" + unit.getInt("id"))));
        }
      }
      rowData.add(trimNull(model.get("allAmount")));
      if (!hasCostPermitted) {
        rowData.add("***");
      } else {
        rowData.add(trimNull(model.get("allMoney")));
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
