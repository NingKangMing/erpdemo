package com.aioerp.controller.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Unit;
import com.aioerp.model.reports.sell.SellReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.CollectionUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SellCountController
  extends BaseController
{
  private static final String prdListID = "xs_prdSellCount";
  private static final String unitListID = "xs_unitSellCount";
  private static final String prdDetailListID = "prdDetailListID";
  private static final String unitDetailListID = "unitDetailListID";
  private static final String staffDetailListID = "staffDetailListID";
  private static final String deptDetailListID = "deptDetailListID";
  private static final String storageDetailListID = "storageDetailListID";
  private static final String areaDetailListID = "areaDetailListID";
  
  public void toSearchDialog()
    throws ParseException
  {
    String modelType = getPara(0, "prd");
    setAttr("modelType", modelType);
    setStartDateAndEndDate();
    render("/WEB-INF/template/reports/sell/prdSellCount/searchDialog.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String modelType = getPara("modelType", getPara(1, ""));
    String type = getPara(0, "first");
    setAttr("pageMap", com(map));
    
    String returnPage = "";
    if (modelType.equals("prd"))
    {
      columnConfig("xs500");
      if ((type.equals("first")) || (type.equals("search"))) {
        returnPage = "/WEB-INF/template/reports/sell/prdSellCount/page.html";
      } else if ((type.equals("page")) || (type.equals("tree")) || (type.equals("line"))) {
        returnPage = "/WEB-INF/template/reports/sell/prdSellCount/list.html";
      }
    }
    else if (modelType.equals("unit"))
    {
      columnConfig("xs501");
      if ((type.equals("first")) || (type.equals("search"))) {
        returnPage = "/WEB-INF/template/reports/sell/unitSellCount/page.html";
      } else if ((type.equals("page")) || (type.equals("tree")) || (type.equals("line"))) {
        returnPage = "/WEB-INF/template/reports/sell/unitSellCount/list.html";
      }
    }
    render(returnPage);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    String configName = loginConfigName();
    String modelType = getPara("modelType", "");
    String type = getPara(0, "first");
    if (type.equals("tree")) {
      modelType = getPara(1);
    }
    map.put("node", getParaToInt("node", Integer.valueOf(AioConstants.NODE_2)));
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    
    int staffId = getParaToInt("staff.id", Integer.valueOf(0)).intValue();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    String staffFullName = getPara("staff.name", ComFunController.getTableAttrById(configName, "b_staff", staffId, "fullName").toString());
    String storageFullName = getPara("storage.fullName", ComFunController.getTableAttrById(configName, "b_storage", storageId, "fullName").toString());
    setAttr("modelType", modelType);
    setAttr("staffFullName", staffFullName);
    setAttr("storageFullName", storageFullName);
    

    String[] orderTypes = getPara("pageOrderTypes", "").split(",");
    if ((type.equals("first")) || (type.equals("search")))
    {
      orderTypes = getParaValues("orderTypes");
      if (orderTypes == null) {
        orderTypes = getPara("pageOrderTypes", "").split(",");
      }
    }
    else if (type.equals("tree"))
    {
      supId = getParaToInt(2).intValue();
      orderTypes = getPara("pageOrderTypes", "").split(",");
    }
    else if (type.equals("page"))
    {
      orderTypes = getPara("pageOrderTypes", "").split(",");
    }
    else if (type.equals("line"))
    {
      map.put("node", Integer.valueOf(AioConstants.NODE_1));
      orderTypes = getPara("pageOrderTypes", "").split(",");
    }
    String startDate = getPara("startDate");
    String endDate = getPara("endDate");
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), startDate, endDate);
    }
    String aimDiv = getPara("aimDiv", "all");
    



    map.put("supId", Integer.valueOf(supId));
    map.put("staffId", Integer.valueOf(staffId));
    map.put("storageId", Integer.valueOf(storageId));
    map.put("orderTypes", orderTypes);
    map.put("startDate", startDate);
    map.put("endDate", endDate);
    map.put("aimDiv", aimDiv);
    Map<String, Object> reportPrdSellCount = null;
    

    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    

    boolean flag = isInitReportOtherCondition(map);
    if (flag) {
      map.put("aimDiv", "all");
    }
    if (modelType.equals("prd"))
    {
      map.put("listID", "xs_prdSellCount");
      int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
      String unitFullName = getPara("unit.fullName", ComFunController.getTableAttrById(configName, "b_unit", unitId, "fullName").toString());
      map.put("unitId", Integer.valueOf(unitId));
      reportPrdSellCount = SellReports.dao.reportPrdSellCount(configName, map);
      setAttr("unitId", Integer.valueOf(unitId));
      setAttr("unitFullName", unitFullName);
    }
    else if (modelType.equals("unit"))
    {
      map.put("listID", "xs_unitSellCount");
      int productId = getParaToInt("product.id", Integer.valueOf(0)).intValue();
      map.put("productId", Integer.valueOf(productId));
      reportPrdSellCount = SellReports.dao.reportUnitSellCount(configName, map);
      setAttr("productId", Integer.valueOf(productId));
      setAttr("productFullName", ComFunController.getTableAttrById(configName, "b_product", productId, "fullName").toString());
    }
    mapToResponse(map);
    setAttr("orderTypes", CollectionUtils.strArrToString(orderTypes, ","));
    

    setAttr("pageMap", reportPrdSellCount);
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
  
  public void prdXsDetail()
    throws SQLException, Exception
  {
    Map<String, Object> map = requestPageToMap(null, "recodeDate");
    
    String modelType = getPara("modelType", getPara(0, ""));
    String whereComeDetail = getPara("whereComeDetail", "other");
    String returnPage = "page";
    if (whereComeDetail.equals("other")) {
      map.put("orderField", "recodeDate");
    } else {
      returnPage = "list";
    }
    setAttr("pageMap", prdXsDetailOrPrint(map));
    String columnType = "";
    if (modelType.equals("prd")) {
      columnType = "xs517";
    } else if (modelType.equals("unit")) {
      columnType = "xs517";
    } else if (modelType.equals("staff")) {
      columnType = "xs519";
    } else if (modelType.equals("dept")) {
      columnType = "xs518";
    } else if (modelType.equals("storage")) {
      columnType = "xs521";
    } else if (modelType.equals("area")) {
      columnType = "xs520";
    }
    columnConfig(columnType);
    if (returnPage.equals("page")) {
      render("/WEB-INF/template/reports/sell/prdSellCount/prdSellDetail/page.html");
    } else if (getPara("whichRefresh", "").equals("navTabRefresh")) {
      render("/WEB-INF/template/reports/sell/prdSellCount/prdSellDetail/page.html");
    } else if ((modelType.equals("prd")) || (modelType.equals("unit"))) {
      render("/WEB-INF/template/reports/sell/prdSellCount/prdSellDetail/prdUnitList.html");
    } else if (modelType.equals("staff")) {
      render("/WEB-INF/template/reports/sell/prdSellCount/prdSellDetail/staffList.html");
    } else if (modelType.equals("dept")) {
      render("/WEB-INF/template/reports/sell/prdSellCount/prdSellDetail/deptList.html");
    } else if (modelType.equals("storage")) {
      render("/WEB-INF/template/reports/sell/prdSellCount/prdSellDetail/storageList.html");
    } else if (modelType.equals("area")) {
      render("/WEB-INF/template/reports/sell/prdSellCount/prdSellDetail/areaList.html");
    }
  }
  
  public Map<String, Object> prdXsDetailOrPrint(Map<String, Object> map)
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    
    String modelType = getPara("modelType", getPara(0, ""));
    String modelTypeName = "";
    String whereComeDetail = getPara("whereComeDetail", "other");
    if (whereComeDetail.equals("other")) {
      map.put("orderField", "recodeDate");
    }
    String returnPage = "page";
    String aimDiv = getPara("aimDiv", "all");
    if (modelType.equals("")) {
      returnPage = "list";
    }
    int nodeType1 = getParaToInt("nodeType1", Integer.valueOf(0)).intValue();
    String orderTypeStrs = getPara("pageOrderTypes");
    String unitId = getPara("unit.id", "0");
    String productId = getPara("product.id", "0");
    String staffId = getPara("staff.id", "0");
    String storageId = getPara("storage.id", "0");
    String departmentId = "0";
    String areaId = "0";
    String startDate = getPara("startDate", "");
    String endDate = getPara("endDate");
    
    String listId = "";
    
    String columnType = "";
    int objId = getParaToInt("id", Integer.valueOf(0)).intValue();
    if (modelType.equals("prd"))
    {
      columnType = "xs517";
      listId = "prdDetailListID";
      productId = String.valueOf(objId);
      setAttr("productFullName", ComFunController.getTableAttrById(configName, "b_product", Integer.valueOf(productId).intValue(), "fullName").toString());
      modelTypeName = "商品";
    }
    else if (modelType.equals("unit"))
    {
      columnType = "xs517";
      listId = "unitDetailListID";
      if (objId != 0)
      {
        unitId = String.valueOf(objId);
      }
      else if (unitId.equals("0"))
      {
        if (objId == 0) {
          objId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
        }
      }
      else
      {
        objId = Integer.valueOf(unitId).intValue();
        productId = getPara("selectedObjectId", "0");
      }
      setAttr("productFullName", ComFunController.getTableAttrById(configName, "b_product", Integer.valueOf(productId).intValue(), "fullName").toString());
      modelTypeName = "单位";
    }
    else if (modelType.equals("staff"))
    {
      columnType = "xs519";
      listId = "staffDetailListID";
      staffId = String.valueOf(objId);
      setAttr("productFullName", ComFunController.getTableAttrById(configName, "b_product", Integer.valueOf(productId).intValue(), "fullName").toString());
      modelTypeName = "职员";
    }
    else if (modelType.equals("dept"))
    {
      columnType = "xs518";
      listId = "deptDetailListID";
      departmentId = String.valueOf(objId);
      setAttr("productFullName", ComFunController.getTableAttrById(configName, "b_product", Integer.valueOf(productId).intValue(), "fullName").toString());
      modelTypeName = "部门";
    }
    else if (modelType.equals("storage"))
    {
      columnType = "xs521";
      listId = "storageDetailListID";
      if (objId != 0)
      {
        storageId = String.valueOf(objId);
      }
      else if (storageId.equals("0"))
      {
        if (objId == 0) {
          objId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
        }
      }
      else
      {
        objId = Integer.valueOf(storageId).intValue();
        productId = getPara("selectedObjectId", "0");
      }
      setAttr("productFullName", ComFunController.getTableAttrById(configName, "b_product", Integer.valueOf(productId).intValue(), "fullName").toString());
      modelTypeName = "仓库";
    }
    else if (modelType.equals("area"))
    {
      columnType = "xs520";
      listId = "areaDetailListID";
      areaId = String.valueOf(objId);
      modelTypeName = "地区";
    }
    setAttr("modelTypeName", modelTypeName);
    setAttr("id", Integer.valueOf(objId));
    

    map.put("listID", listId);
    map.put("modelType", modelType);
    map.put("aimDiv", aimDiv);
    map.put("productId", productId);
    map.put("unitId", unitId);
    map.put("staffId", staffId);
    map.put("storageId", storageId);
    map.put("departmentId", departmentId);
    map.put("areaId", areaId);
    map.put("orderTypes", orderTypeStrs.split(","));
    map.put("startDate", startDate);
    map.put("endDate", endDate);
    Map<String, Object> dataMap = null;
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    map.put("departmentPrivs", basePrivs(DEPARTMENT_PRIVS));
    map.put("areaPrivs", basePrivs(AREA_PRIVS));
    map.put("accountPrivs", basePrivs(ACCOUNT_PRIVS));
    
    dataMap = SellReports.dao.sellDetail(configName, map);
    setAttr("nodeType1", Integer.valueOf(nodeType1));
    mapToResponse(map);
    setAttr("orderTypes", orderTypeStrs);
    return dataMap;
  }
  
  public void prdXsDetailPrint()
    throws SQLException, Exception
  {
    String modelType = getPara("modelType", "");
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    String modelTypeName = "";
    if ((modelType.equals("prd")) || (modelType.equals("unit")))
    {
      data.put("reportName", "销售明细账本");
    }
    else if (modelType.equals("staff"))
    {
      modelTypeName = "职员";
      data.put("reportName", "职员明细账本");
    }
    else if (modelType.equals("dept"))
    {
      modelTypeName = "部门";
      data.put("reportName", "部门明细账本");
    }
    else if (modelType.equals("storage"))
    {
      modelTypeName = "仓库";
      data.put("reportName", "仓库明细账本");
    }
    else if (modelType.equals("area"))
    {
      modelTypeName = "地区";
      data.put("reportName", "地区明细账本");
    }
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
    Map<String, Object> pageMap = prdXsDetailOrPrint(map);
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
    if ((modelType.equals("prd")) || (modelType.equals("unit")))
    {
      colTitle.add("商品编号");
      colTitle.add("商品全名");
      colTitle.add("商品简称");
      colTitle.add("商品拼音");
      colTitle.add("商品规格");
      colTitle.add("商品型号");
      colTitle.add("商品产地");
      colTitle.add("仓库编号");
      colTitle.add("仓库全名");
      colTitle.add("单位编号");
      colTitle.add("单位全名");
      colTitle.add("销售出库数");
      colTitle.add("销售退货数");
      colTitle.add("税后单价");
      colTitle.add("税后金额");
    }
    else if (modelType.equals("staff"))
    {
      colTitle.add(modelTypeName + "编号");
      colTitle.add(modelTypeName + "全名");
      colTitle.add("金额");
    }
    else
    {
      colTitle.add(modelTypeName + "编号");
      colTitle.add(modelTypeName + "全名");
      colTitle.add("出库数量");
      colTitle.add("金额");
    }
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
      if ((modelType.equals("prd")) || (modelType.equals("unit")))
      {
        Model product = (Model)detail.get("product");
        Model storage = (Model)detail.get("storage");
        Model unit = (Model)detail.get("unit");
        rowData.add(trimNull(product.getStr("code")));
        rowData.add(trimNull(product.getStr("fullName")));
        rowData.add(trimNull(product.getStr("smallName")));
        rowData.add(trimNull(product.getStr("spell")));
        rowData.add(trimNull(product.getStr("standard")));
        rowData.add(trimNull(product.getStr("model")));
        rowData.add(trimNull(product.getStr("field")));
        rowData.add(trimNull(storage.getStr("code")));
        rowData.add(trimNull(storage.getStr("fullName")));
        rowData.add(trimNull(unit.getStr("code")));
        rowData.add(trimNull(unit.getStr("fullName")));
        rowData.add(trimNull(detail.getBigDecimal("sellOutAmount")));
        rowData.add(trimNull(detail.getBigDecimal("sellInAmount")));
        rowData.add(trimNull(detail.getBigDecimal("basePrice")));
        rowData.add(trimNull(detail.getBigDecimal("sellMoney")));
      }
      else if (modelType.equals("staff"))
      {
        Model staff = (Model)detail.get("staff");
        rowData.add(trimNull(staff.getStr("code")));
        rowData.add(trimNull(staff.getStr("fullName")));
        rowData.add(trimNull(detail.getBigDecimal("sellMoney")));
      }
      else if (modelType.equals("dept"))
      {
        Model department = (Model)detail.get("department");
        rowData.add(trimNull(department.getStr("code")));
        rowData.add(trimNull(department.getStr("fullName")));
        rowData.add(trimNull(detail.getBigDecimal("sellOutAmount")));
        rowData.add(trimNull(detail.getBigDecimal("sellMoney")));
      }
      else if (modelType.equals("storage"))
      {
        Model storage = (Model)detail.get("storage");
        rowData.add(trimNull(storage.getStr("code")));
        rowData.add(trimNull(storage.getStr("fullName")));
        rowData.add(trimNull(detail.getBigDecimal("sellOutAmount")));
        rowData.add(trimNull(detail.getBigDecimal("sellMoney")));
      }
      else if (modelType.equals("area"))
      {
        Model area = (Model)detail.get("area");
        rowData.add(trimNull(area.getStr("code")));
        rowData.add(trimNull(area.getStr("fullName")));
        rowData.add(trimNull(detail.getBigDecimal("sellOutAmount")));
        rowData.add(trimNull(detail.getBigDecimal("sellMoney")));
      }
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
    if (modelType.equals("prd")) {
      data.put("reportName", "商品销售统计");
    } else if (modelType.equals("unit")) {
      data.put("reportName", "单位销售统计");
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
    if (modelType.equals("prd"))
    {
      colTitle.add("商品编号");
      colTitle.add("商品全名");
      colTitle.add("商品简称");
      colTitle.add("商品拼音");
      colTitle.add("规格");
      colTitle.add("型号");
      colTitle.add("产地");
    }
    else if (modelType.equals("unit"))
    {
      colTitle.add("单位编号");
      colTitle.add("单位全名");
      colTitle.add("销售次数");
      colTitle.add("优惠金额");
      colTitle.add("优惠后金额");
    }
    colTitle.add("销售数量");
    colTitle.add("辅助单位");
    colTitle.add("辅助进货数量");
    colTitle.add("折后销售均价");
    colTitle.add("折后金额");
    colTitle.add("税额");
    colTitle.add("含税单价");
    colTitle.add("价税合计");
    colTitle.add("赠品数量");
    colTitle.add("赠品零售额");
    colTitle.add("赠品金额");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < detailList.size(); i++)
    {
      Record detail = (Record)detailList.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("fullName")));
      if (modelType.equals("prd"))
      {
        rowData.add(trimNull(detail.getStr("smallName")));
        rowData.add(trimNull(detail.getStr("spell")));
        rowData.add(trimNull(detail.getStr("standard")));
        rowData.add(trimNull(detail.getStr("model")));
        rowData.add(trimNull(detail.getStr("field")));
      }
      else if (modelType.equals("unit"))
      {
        rowData.add(trimNull(detail.getBigDecimal("sellCounts")));
        rowData.add(trimNull(detail.getBigDecimal("privilege")));
        rowData.add(trimNull(detail.getBigDecimal("privilegeMoney")));
      }
      rowData.add(trimNull(detail.getBigDecimal("sellAmount")));
      rowData.add(trimNull(detail.getStr("assistUnit")));
      rowData.add(trimNull(detail.getStr("helpAmount")));
      rowData.add(trimNull(detail.getBigDecimal("sellDiscountAvgPrice")));
      rowData.add(trimNull(detail.getBigDecimal("sellDiscountMoney")));
      rowData.add(trimNull(detail.getBigDecimal("sellTaxes")));
      rowData.add(trimNull(detail.getBigDecimal("sellTaxAvgPrice")));
      rowData.add(trimNull(detail.getBigDecimal("sellTaxMoney")));
      rowData.add(trimNull(detail.getBigDecimal("sellGiftAmount")));
      rowData.add(trimNull(detail.getBigDecimal("sellGiftRetailPrice1")));
      rowData.add(trimNull(detail.getBigDecimal("sellGiftMoney"), hasCostPermitted));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
