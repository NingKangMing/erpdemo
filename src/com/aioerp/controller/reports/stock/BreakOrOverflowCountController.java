package com.aioerp.controller.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.base.Product;
import com.aioerp.model.reports.stock.BreakOrOverflowReports;
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

public class BreakOrOverflowCountController
  extends BaseController
{
  public void toSearchDialog()
    throws ParseException
  {
    String modelType = getPara(0, "in");
    setAttr("modelType", modelType);
    




    setStartDateAndEndDate();
    render("/WEB-INF/template/reports/stock/breakageOrOverflow/searchDialog.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String modelType = getPara("modelType", "");
    String type = getPara(0, "first");
    String columnType = "";
    if (modelType.equals("in")) {
      columnType = "cc506";
    } else if (modelType.equals("out")) {
      columnType = "cc504";
    }
    setAttr("pageMap", com(map));
    
    columnConfig(columnType);
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/reports/stock/breakageOrOverflow/page.html";
    } else if ((type.equals("page")) || (type.equals("tree")) || (type.equals("line"))) {
      returnPage = "/WEB-INF/template/reports/stock/breakageOrOverflow/list.html";
    }
    render(returnPage);
  }
  
  public void tree()
  {
    String configName = loginConfigName();
    String modelType = getPara("modelType", "in");
    List<Model> cats = null;
    String nodeName = "";
    if (modelType.equals("in"))
    {
      cats = Product.dao.getTreeEnableList(configName, basePrivs(PRODUCT_PRIVS));
      nodeName = "商品信息";
    }
    else if (modelType.equals("out"))
    {
      cats = Product.dao.getTreeEnableList(configName, basePrivs(PRODUCT_PRIVS));
      nodeName = "商品信息";
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
      node.put("url", "reports/stock/breakOrOverflow/dialogSearch/tree?modelType=" + modelType + "&supId=" + product.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", nodeName);
    node.put("url", "reports/stock/breakOrOverflow/dialogSearch/tree?modelType=" + modelType + "&supId=0");
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    String configName = loginConfigName();
    String modelType = getPara("modelType", "");
    String modelTypeName = "";
    String type = getPara(0, "first");
    map.put("node", getParaToInt("node", Integer.valueOf(AioConstants.NODE_2)));
    

    String staffId = getPara("staff.id", "");
    String storageId = getPara("storage.id", "");
    String staffFullName = getPara("staff.name", "");
    String storageFullName = getPara("storage.fullName", "");
    


    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    String aimDiv = getPara("aimDiv", "all");
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), startTime, endTime);
    }
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    if ((type.equals("first")) || (type.equals("search"))) {
      aimDiv = "all";
    } else if ((!type.equals("tree")) && 
      (!type.equals("page")) && 
      (type.equals("line"))) {
      map.put("node", Integer.valueOf(AioConstants.NODE_1));
    }
    map.put("modelType", modelType);
    map.put("supId", Integer.valueOf(supId));
    map.put("staffId", staffId);
    map.put("storageId", storageId);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    map.put("aimDiv", aimDiv);
    Map<String, Object> reportPrdSellCount = null;
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    
    boolean flag = isInitReportOtherCondition(map);
    if (flag) {
      map.put("shoeType", null);
    }
    String columnType = "";
    
    String listID = "";
    String ztreeID = "";
    if (modelType.equals("in"))
    {
      columnType = "cc506";
      ztreeID = "z_cc_overflowCount";
      listID = "cc_overflowCount";
      modelTypeName = "报溢";
    }
    else if (modelType.equals("out"))
    {
      columnType = "cc504";
      ztreeID = "z_cc_breakCount";
      listID = "cc_breakCount";
      modelTypeName = "报损";
    }
    map.put("listID", listID);
    setAttr("ztreeID", ztreeID);
    reportPrdSellCount = BreakOrOverflowReports.dao.reportBreakOrOverflowCount(configName, map);
    setAttr("modelType", modelType);
    setAttr("modelTypeName", modelTypeName);
    setAttr("staffFullName", staffFullName);
    setAttr("storageFullName", storageFullName);
    mapToResponse(map);
    return reportPrdSellCount;
  }
  
  public void print()
    throws ParseException
  {
    String modelType = getPara("modelType", "");
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    String billModelType = "";
    if (modelType.equals("in")) {
      billModelType = "溢";
    } else if (modelType.equals("out")) {
      billModelType = "损";
    }
    data.put("reportName", "报" + billModelType + "单统计");
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
    headData.add("查询时间:" + getPara("startTime", "") + " 至 " + getPara("endTime", ""));
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简称");
    colTitle.add("商品拼音");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("报" + billModelType + "数量");
    colTitle.add("辅助报" + billModelType + "数量");
    colTitle.add("报" + billModelType + "金额");
    

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
      rowData.add(trimNull(detail.getStr("smallName")));
      rowData.add(trimNull(detail.getStr("spell")));
      rowData.add(trimNull(detail.getStr("standard")));
      rowData.add(trimNull(detail.getStr("model")));
      rowData.add(trimNull(detail.getStr("field")));
      rowData.add(trimNull(detail.getBigDecimal("baseAmounts")));
      rowData.add(trimNull(detail.getStr("helpAmount")));
      rowData.add(trimNull(detail.getBigDecimal("baseMoneys"), hasCostPermitted));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
  
  public void breakOrOverflowDetail()
    throws Exception
  {
    String configName = loginConfigName();
    Map<String, Object> map = requestPageToMap(null, null);
    


    String modelType = getPara("modelType");
    String modelTypeName = "";
    String whereComeDetail = getPara("whereComeDetail", "other");
    

    String aimDiv = "all";
    if (whereComeDetail.equals("other"))
    {
      map.put("orderField", "recodeDate");
      map.put("orderDirection", "asc");
    }
    else
    {
      aimDiv = getPara("aimDiv", "");
    }
    String productId = getPara("product.id", getPara("selectedObjectId", ""));
    String storageId = getPara("storage.id", "");
    String storageFullName = getPara("storage.fullName", "");
    String startTime = getPara("startTime", "");
    String endTime = getPara("endTime");
    setAttr("productFullName", ComFunController.getTableAttrById(configName, "b_product", Integer.valueOf(productId).intValue(), "fullName").toString());
    setAttr("storageFullName", storageFullName);
    
    String columnType = "";
    if (modelType.equals("in"))
    {
      columnType = "cc507";
      modelTypeName = "报溢";
    }
    else if (modelType.equals("out"))
    {
      columnType = "cc505";
      modelTypeName = "报损";
    }
    map.put("listID", "cc_breakOrOverflowCount_detail");
    map.put("modelType", modelType);
    map.put("aimDiv", aimDiv);
    map.put("productId", productId);
    map.put("storageId", storageId);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    Map<String, Object> dataMap = null;
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    
    dataMap = BreakOrOverflowReports.dao.breakOrOverflowDetail(configName, map);
    setAttr("modelTypeName", modelTypeName);
    mapToResponse(map);
    

    setAttr("pageMap", dataMap);
    


    columnConfig(columnType);
    if (whereComeDetail.equals("other"))
    {
      render("/WEB-INF/template/reports/stock/breakageOrOverflow/detail/page.html");
    }
    else if (whereComeDetail.equals("self"))
    {
      if (getPara("whichRefresh", "").equals("navTabRefresh")) {
        render("/WEB-INF/template/reports/stock/breakageOrOverflow/detail/page.html");
      } else {
        render("/WEB-INF/template/reports/stock/breakageOrOverflow/detail/breakageOrOverflowList.html");
      }
      render("/WEB-INF/template/reports/stock/breakageOrOverflow/detail/page.html");
    }
  }
  
  public Map<String, Object> breakOrOverflowDetailOrPrint(Map<String, Object> map)
    throws Exception
  {
    String configName = loginConfigName();
    
    String modelType = getPara("modelType");
    String modelTypeName = "";
    String whereComeDetail = getPara("whereComeDetail", "other");
    

    String aimDiv = "all";
    if (whereComeDetail.equals("other"))
    {
      map.put("orderField", "recodeDate");
      map.put("orderDirection", "asc");
    }
    else
    {
      aimDiv = getPara("aimDiv", "");
    }
    String productId = getPara("product.id", getPara("selectedObjectId", ""));
    String storageId = getPara("storage.id", "");
    String storageFullName = getPara("storage.fullName", "");
    String startTime = getPara("startTime", "");
    String endTime = getPara("endTime", "");
    setAttr("productFullName", ComFunController.getTableAttrById(configName, "b_product", Integer.valueOf(productId).intValue(), "fullName").toString());
    setAttr("storageFullName", storageFullName);
    

    String columnType = "";
    if (modelType.equals("in"))
    {
      columnType = "cc507";
      modelTypeName = "报溢";
    }
    else if (modelType.equals("out"))
    {
      columnType = "cc505";
      modelTypeName = "报损";
    }
    map.put("listID", "cc_breakOrOverflowCount_detail");
    map.put("modelType", modelType);
    map.put("aimDiv", aimDiv);
    map.put("productId", productId);
    map.put("storageId", storageId);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    Map<String, Object> dataMap = null;
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    
    dataMap = BreakOrOverflowReports.dao.breakOrOverflowDetail(configName, map);
    setAttr("modelTypeName", modelTypeName);
    mapToResponse(map);
    return dataMap;
  }
  
  public void detailPrint()
    throws Exception
  {
    String modelType = getPara("modelType", "");
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    if (modelType.equals("in")) {
      data.put("reportName", "报溢单明细账本");
    } else if (modelType.equals("out")) {
      data.put("reportName", "报损单明细账本");
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
    Map<String, Object> pageMap = breakOrOverflowDetailOrPrint(map);
    if (pageMap != null) {
      detailList = (List)pageMap.get("pageList");
    }
    printCommonData(headData);
    headData.add("查询时间:" + getPara("startTime", "") + " 至 " + getPara("endTime", ""));
    

    colTitle.add("行号");
    colTitle.add("单据类型");
    colTitle.add("日期");
    colTitle.add("单据编号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简称");
    colTitle.add("商品拼音");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("数量");
    colTitle.add("金额");
    colTitle.add("摘要");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Model pro = (Model)detail.get("pro");
      Model sto = (Model)detail.get("sto");
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.getStr("billTypeName")));
      rowData.add(trimRecordDateNull(detail.getDate("recodeDate")));
      rowData.add(trimNull(detail.getStr("billCode")));
      rowData.add(trimNull(pro.getStr("code")));
      rowData.add(trimNull(pro.getStr("fullName")));
      rowData.add(trimNull(pro.getStr("smallName")));
      rowData.add(trimNull(pro.getStr("spell")));
      rowData.add(trimNull(pro.getStr("standard")));
      rowData.add(trimNull(pro.getStr("model")));
      rowData.add(trimNull(pro.getStr("field")));
      rowData.add(trimNull(sto.getStr("code")));
      rowData.add(trimNull(sto.getStr("fullName")));
      rowData.add(trimNull(detail.getBigDecimal("amounts")));
      rowData.add(trimNull(detail.getBigDecimal("moneys"), hasCostPermitted));
      rowData.add(trimNull(detail.getStr("billRemark")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
