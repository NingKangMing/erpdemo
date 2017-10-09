package com.aioerp.controller.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.base.Area;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.reports.sell.SellRankReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.CollectionUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SellRankCountController
  extends BaseController
{
  public void toSearchDialog()
    throws ParseException
  {
    String modelType = getPara(0, "prd");
    setAttr("modelType", modelType);
    




    setStartDateAndEndDate();
    render("/WEB-INF/template/reports/sell/sellRankCount/searchDialog.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String modelType = getPara("modelType", getPara(1, ""));
    String type = getPara(0, "first");
    String columnType = "xs506";
    if (modelType.equals("prd")) {
      columnType = "xs506";
    } else if (modelType.equals("unit")) {
      columnType = "xs507";
    } else if (modelType.equals("staff")) {
      columnType = "xs508";
    } else if (modelType.equals("dept")) {
      columnType = "xs509";
    } else if (modelType.equals("storage")) {
      columnType = "xs510";
    } else if (modelType.equals("area")) {
      columnType = "xs511";
    }
    setAttr("pageMap", com(map));
    

    columnConfig(columnType);
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/reports/sell/sellRankCount/page.html";
    } else if ((type.equals("page")) || (type.equals("tree")) || (type.equals("line"))) {
      returnPage = "/WEB-INF/template/reports/sell/sellRankCount/list.html";
    }
    render(returnPage);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    String configName = loginConfigName();
    String modelType = getPara("modelType", getPara(1, ""));
    String modelTypeName = "";
    String type = getPara(0, "first");
    map.put("node", getParaToInt("node", Integer.valueOf(AioConstants.NODE_2)));
    
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    String unitFullName = getPara("unit.fullName", ComFunController.getTableAttrById(configName, "b_unit", unitId, "fullName").toString());
    int productId = getParaToInt("product.id", Integer.valueOf(0)).intValue();
    String productFullName = getPara("product.fullName", ComFunController.getTableAttrById(configName, "b_product", productId, "fullName").toString());
    int staffId = getParaToInt("staff.id", Integer.valueOf(0)).intValue();
    String staffFullName = getPara("staff.name", ComFunController.getTableAttrById(configName, "b_staff", staffId, "fullName").toString());
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    String storageFullName = getPara("storage.fullName", ComFunController.getTableAttrById(configName, "b_storage", storageId, "fullName").toString());
    


    String[] orderTypes = null;
    if ((!type.equals("first")) && (!type.equals("search"))) {
      if (type.equals("tree")) {
        supId = getParaToInt(2).intValue();
      } else if (!type.equals("page")) {
        if (type.equals("line")) {
          map.put("node", Integer.valueOf(AioConstants.NODE_1));
        }
      }
    }
    if (getParaValues("orderTypes") != null) {
      orderTypes = getParaValues("orderTypes");
    } else {
      orderTypes = getPara("pageOrderTypes", "").split(",");
    }
    String startDate = getPara("startDate");
    String endDate = getPara("endDate");
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), startDate, endDate);
    }
    String aimDiv = getPara("aimDiv", "all");
    

    map.put("modelType", modelType);
    map.put("supId", Integer.valueOf(supId));
    map.put("unitId", Integer.valueOf(unitId));
    map.put("productId", Integer.valueOf(productId));
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
    map.put("departmentPrivs", basePrivs(DEPARTMENT_PRIVS));
    map.put("areaPrivs", basePrivs(AREA_PRIVS));
    
    boolean flag = isInitReportOtherCondition(map);
    if (flag) {
      map.put("aimDiv", "all");
    }
    String columnType = "xs506";
    
    String listID = "";
    String ztreeID = "";
    if (modelType.equals("prd"))
    {
      ztreeID = "z_xs_prdSellRankCount";
      listID = "xs_prdSellRankCount";
      modelTypeName = "商品";
      columnType = "xs506";
    }
    else if (modelType.equals("unit"))
    {
      ztreeID = "z_xs_unitSellRankCount";
      listID = "xs_unitSellRankCount";
      modelTypeName = "客户";
      columnType = "xs507";
    }
    else if (modelType.equals("staff"))
    {
      ztreeID = "z_xs_staffSellRankCount";
      listID = "xs_staffSellRankCount";
      modelTypeName = "职员";
      columnType = "xs508";
    }
    else if (modelType.equals("dept"))
    {
      ztreeID = "z_xs_deptSellRankCount";
      listID = "xs_deptSellRankCount";
      modelTypeName = "部门";
      columnType = "xs509";
    }
    else if (modelType.equals("storage"))
    {
      ztreeID = "z_xs_storageSellRankCount";
      listID = "xs_storageSellRankCount";
      modelTypeName = "仓库";
      columnType = "xs510";
    }
    else if (modelType.equals("area"))
    {
      ztreeID = "z_xs_areaSellRankCount";
      listID = "xs_areaSellRankCount";
      modelTypeName = "地区";
      columnType = "xs511";
    }
    setAttr("ztreeID", ztreeID);
    map.put("listID", listID);
    reportPrdSellCount = SellRankReports.dao.reportSellRankCount(configName, map);
    




    setAttr("modelTypeName", modelTypeName);
    setAttr("unitFullName", unitFullName);
    setAttr("productFullName", productFullName);
    setAttr("staffFullName", staffFullName);
    setAttr("storageFullName", storageFullName);
    mapToResponse(map);
    if (orderTypes == null) {
      setAttr("orderTypes", "");
    } else {
      setAttr("orderTypes", CollectionUtils.strArrToString(orderTypes, ","));
    }
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
    else if (modelType.equals("staff"))
    {
      String staffPrivs = basePrivs(STAFF_PRIVS);
      cats = Staff.dao.getParent(configName, staffPrivs);
      nodeName = "职员信息";
    }
    else if (modelType.equals("dept"))
    {
      String departmentPrivs = basePrivs(DEPARTMENT_PRIVS);
      cats = Department.dao.getAllSorts(configName, departmentPrivs);
      nodeName = "部门信息";
    }
    else if (modelType.equals("storage"))
    {
      String storagePrivs = basePrivs(STORAGE_PRIVS);
      cats = Storage.dao.getAllSorts(configName, storagePrivs);
      nodeName = "仓库信息";
    }
    else if (modelType.equals("area"))
    {
      String areaPrivs = basePrivs(AREA_PRIVS);
      cats = Area.dao.getAllSorts(configName, areaPrivs);
      nodeName = "地区信息";
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
      node.put("url", "reports/sellRankCount/dialogSearch/tree-" + modelType + "-" + product.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", nodeName);
    node.put("url", "reports/sellRankCount/dialogSearch/tree-" + modelType + "-0");
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public void print()
    throws ParseException
  {
    String modelType = getPara("modelType", "");
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    String modelTypeNmae = "";
    if (modelType.equals("prd")) {
      modelTypeNmae = "商品";
    } else if (modelType.equals("unit")) {
      modelTypeNmae = "客户";
    } else if (modelType.equals("staff")) {
      modelTypeNmae = "职员";
    } else if (modelType.equals("dept")) {
      modelTypeNmae = "部门";
    } else if (modelType.equals("storage")) {
      modelTypeNmae = "仓库";
    } else if (modelType.equals("area")) {
      modelTypeNmae = "地区";
    }
    data.put("reportName", modelTypeNmae + "销售排行榜");
    
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
    colTitle.add(modelTypeNmae + "编号");
    colTitle.add(modelTypeNmae + "全名");
    if (modelType.equals("prd"))
    {
      colTitle.add("商品简称");
      colTitle.add("商品拼音");
      colTitle.add("规格");
      colTitle.add("型号");
      colTitle.add("产地");
      colTitle.add("辅助单位");
      colTitle.add("辅助进货数量");
      colTitle.add("销售单价");
    }
    colTitle.add("销售数量");
    colTitle.add("其中赠送数量");
    colTitle.add("折前金额");
    colTitle.add("折扣金额");
    colTitle.add("折后单价");
    colTitle.add("折后金额");
    colTitle.add("税后单价");
    colTitle.add("税额");
    colTitle.add("价税合计");
    colTitle.add("成本单价");
    colTitle.add("成本金额");
    colTitle.add("毛利");
    colTitle.add("毛利率(%)");
    colTitle.add("销售比重(%)");
    colTitle.add("利润比重(%)");
    

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
        rowData.add(trimNull(detail.getStr("assistUnit")));
        rowData.add(trimNull(detail.getStr("helpAmount")));
        rowData.add(trimNull(detail.getBigDecimal("avgPrice")));
      }
      rowData.add(trimNull(detail.getBigDecimal("sellAmount")));
      rowData.add(trimNull(detail.getBigDecimal("sellGiftAmount")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.getBigDecimal("sellDiscountHasMoney")));
      rowData.add(trimNull(detail.getBigDecimal("sellDiscountAvgPrice")));
      rowData.add(trimNull(detail.getBigDecimal("sellDiscountMoney")));
      rowData.add(trimNull(detail.getBigDecimal("sellTaxAvgPrice")));
      rowData.add(trimNull(detail.getBigDecimal("sellTaxes")));
      rowData.add(trimNull(detail.getBigDecimal("sellTaxMoney")));
      rowData.add(trimNull(detail.getBigDecimal("sellCostMoneyAvgPrice"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("sellCostMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("profit"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("profitPercent"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("allSellPercent")));
      rowData.add(trimNull(detail.getBigDecimal("allProfitPercent")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
