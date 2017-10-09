package com.aioerp.controller.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Unit;
import com.aioerp.model.reports.sell.SellReturnReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SellReturnCountController
  extends BaseController
{
  private static final String prdListID = "xsth_prdSellCount";
  private static final String unitListID = "xsth_unitSellCount";
  
  public void toSearchDialog()
    throws ParseException
  {
    String modelType = getPara(0, "prd");
    setAttr("modelType", modelType);
    




    setStartDateAndEndDate();
    render("/WEB-INF/template/reports/sell/prdSellreturnCount/searchDialog.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String modelType = getPara("modelType", getPara(1, ""));
    String type = getPara(0, "first");
    setAttr("pageMap", com(map));
    
    String columnType = "";
    String returnPage = "";
    if (modelType.equals("prd"))
    {
      columnType = "xs502";
      if ((type.equals("first")) || (type.equals("search"))) {
        returnPage = "/WEB-INF/template/reports/sell/prdSellreturnCount/page.html";
      } else if ((type.equals("page")) || (type.equals("tree")) || (type.equals("line"))) {
        returnPage = "/WEB-INF/template/reports/sell/prdSellreturnCount/list.html";
      }
    }
    else if (modelType.equals("unit"))
    {
      columnType = "xs503";
      if ((type.equals("first")) || (type.equals("search"))) {
        returnPage = "/WEB-INF/template/reports/sell/unitSellreturnCount/page.html";
      } else if ((type.equals("page")) || (type.equals("tree")) || (type.equals("line"))) {
        returnPage = "/WEB-INF/template/reports/sell/unitSellreturnCount/list.html";
      }
    }
    columnConfig(columnType);
    render(returnPage);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    String configName = loginConfigName();
    String modelType = getPara("modelType", getPara(1, ""));
    String type = getPara(0, "first");
    map.put("node", getParaToInt("node", Integer.valueOf(AioConstants.NODE_2)));
    
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    if ((!type.equals("first")) && (!type.equals("search"))) {
      if (type.equals("tree")) {
        supId = getParaToInt(2).intValue();
      } else if (!type.equals("page")) {
        if (type.equals("line")) {
          map.put("node", Integer.valueOf(AioConstants.NODE_1));
        }
      }
    }
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    String unitFullName = getPara("unit.fullName", "");
    int productId = getParaToInt("product.id", Integer.valueOf(0)).intValue();
    String productFullName = getPara("product.fullName", "");
    int staffId = getParaToInt("staff.id", Integer.valueOf(0)).intValue();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    String staffFullName = getPara("staff.name", "");
    String storageFullName = getPara("storage.fullName", "");
    setAttr("modelType", modelType);
    setAttr("unitFullName", unitFullName);
    setAttr("productFullName", productFullName);
    setAttr("staffFullName", staffFullName);
    setAttr("storageFullName", storageFullName);
    


    String startDate = getPara("startDate");
    String endDate = getPara("endDate");
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), startDate, endDate);
    }
    map.put("supId", Integer.valueOf(supId));
    map.put("unitId", Integer.valueOf(unitId));
    map.put("productId", Integer.valueOf(productId));
    map.put("staffId", Integer.valueOf(staffId));
    map.put("storageId", Integer.valueOf(storageId));
    map.put("startDate", startDate);
    map.put("endDate", endDate);
    Map<String, Object> reportPrdSellCount = null;
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    

    boolean flag = isInitReportOtherCondition(map);
    if (modelType.equals("prd"))
    {
      map.put("listID", "xsth_prdSellCount");
      reportPrdSellCount = SellReturnReports.dao.reportPrdSellReturnCount(configName, map);
    }
    else if (modelType.equals("unit"))
    {
      map.put("listID", "xsth_unitSellCount");
      reportPrdSellCount = SellReturnReports.dao.reportUnitSellReturnCount(configName, map);
    }
    mapToResponse(map);
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
      node.put("url", "reports/sellReturnCount/dialogSearch/tree-" + modelType + "-" + product.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", nodeName);
    node.put("url", "reports/sellReturnCount/dialogSearch/tree-" + modelType + "-0");
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public void print()
    throws ParseException
  {
    String modelType = getPara("modelType", "");
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    if (modelType.equals("prd")) {
      data.put("reportName", "商品销售退货统计");
    } else if (modelType.equals("unit")) {
      data.put("reportName", "单位销售退货统计");
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
      colTitle.add("销售数量");
      colTitle.add("销售税后金额");
      colTitle.add("销售退货数量");
      colTitle.add("销售出库数量");
    }
    else if (modelType.equals("unit"))
    {
      colTitle.add("单位编号");
      colTitle.add("单位全名");
    }
    colTitle.add("销售出库金额");
    colTitle.add("销售退货税后金额");
    colTitle.add("退货率(%)");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
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
        rowData.add(trimNull(detail.getBigDecimal("sellAmount")));
        rowData.add(trimNull(detail.getBigDecimal("sellTaxMoney")));
        rowData.add(trimNull(detail.getBigDecimal("sellInAmount")));
        rowData.add(trimNull(detail.getBigDecimal("sellOutAmount")));
      }
      else
      {
        modelType.equals("unit");
      }
      rowData.add(trimNull(detail.getBigDecimal("sellTaxMoney")));
      rowData.add(trimNull(detail.getBigDecimal("sellReturnTaxMoney")));
      rowData.add(trimNull(detail.getBigDecimal("returnPrecent")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
