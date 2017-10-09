package com.aioerp.controller.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.base.Product;
import com.aioerp.model.reports.stock.DismountCountReports;
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

public class DismountCountController
  extends BaseController
{
  public void toSearchDialog()
    throws ParseException
  {
    String modelType = getPara(0, "same");
    setAttr("modelType", modelType);
    




    setStartDateAndEndDate();
    render("/WEB-INF/template/reports/stock/dismonunt/searchDialog.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    
    String type = getPara(0, "first");
    
    setAttr("pageMap", com(map));
    
    columnConfig("cc512");
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/reports/stock/dismonunt/page.html";
    } else if ((type.equals("page")) || (type.equals("tree")) || (type.equals("line"))) {
      returnPage = "/WEB-INF/template/reports/stock/dismonunt/list.html";
    }
    render(returnPage);
  }
  
  public void tree()
  {
    String configName = loginConfigName();
    String modelType = getPara("modelType", "same");
    List<Model> cats = Product.dao.getTreeEnableList(configName, basePrivs(PRODUCT_PRIVS));
    String nodeName = "商品信息";
    Iterator<Model> iter = cats.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model product = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", product.get("id"));
      node.put("pId", product.get("supId"));
      node.put("name", product.get("fullName"));
      node.put("url", "reports/stock/dismonunt/dialogSearch/tree?modelType=" + modelType + "&supId=" + product.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", nodeName);
    node.put("url", "reports/stock/dismonunt/dialogSearch/tree?modelType=" + modelType + "&supId=0");
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
    String inStorageId = getPara("inStorage.id", "");
    String outStorageId = getPara("outStorage.id", "");
    String inStorageFullName = getPara("inStorage.fullName", "");
    String outStorageFullName = getPara("outStorage.fullName", "");
    String staffFullName = getPara("staff.name", "");
    


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
    map.put("inStorageId", inStorageId);
    map.put("outStorageId", outStorageId);
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
    String listID = "cc_removeCount";
    String ztreeID = "z_cc_removeCount";
    modelTypeName = "生产拆装单统计";
    map.put("listID", listID);
    setAttr("ztreeID", ztreeID);
    
    reportPrdSellCount = DismountCountReports.dao.reportDismountCount(configName, map);
    setAttr("modelType", modelType);
    setAttr("modelTypeName", modelTypeName);
    setAttr("staffFullName", staffFullName);
    setAttr("inStorageFullName", inStorageFullName);
    setAttr("outStorageFullName", outStorageFullName);
    mapToResponse(map);
    return reportPrdSellCount;
  }
  
  public void print()
    throws ParseException
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "商品折装单统计");
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
    colTitle.add("规格");
    colTitle.add("型号");
    colTitle.add("产地");
    colTitle.add("入库数量");
    colTitle.add("辅助入库数量");
    colTitle.add("入库金额");
    colTitle.add("出库数量");
    colTitle.add("辅助出库数量");
    colTitle.add("出库金额");
    

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
      rowData.add(trimNull(detail.getStr("smallName")));
      rowData.add(trimNull(detail.getStr("spell")));
      rowData.add(trimNull(detail.getStr("standard")));
      rowData.add(trimNull(detail.getStr("model")));
      rowData.add(trimNull(detail.getStr("field")));
      rowData.add(trimNull(detail.getBigDecimal("baseInAmounts")));
      rowData.add(trimNull(detail.getStr("helpInAmount")));
      rowData.add(trimNull(detail.getBigDecimal("baseInMoneys")));
      rowData.add(trimNull(detail.getBigDecimal("baseOutAmounts")));
      rowData.add(trimNull(detail.getStr("helpOutAmount")));
      rowData.add(trimNull(detail.getBigDecimal("baseOutMoneys")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
  
  public void dismonuntDetail()
    throws Exception
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String whereComeDetail = getPara("whereComeDetail", "other");
    setAttr("pageMap", dismonuntDetailOrPrint(map));
    
    columnConfig("cc513");
    if (whereComeDetail.equals("other")) {
      render("/WEB-INF/template/reports/stock/dismonunt/detail/page.html");
    } else if (whereComeDetail.equals("self")) {
      if (getPara("whichRefresh", "").equals("navTabRefresh")) {
        render("/WEB-INF/template/reports/stock/dismonunt/detail/page.html");
      } else {
        render("/WEB-INF/template/reports/stock/dismonunt/detail/sameOrDiffPirceList.html");
      }
    }
  }
  
  public Map<String, Object> dismonuntDetailOrPrint(Map<String, Object> map)
    throws Exception
  {
    String configName = loginConfigName();
    
    String modelType = getPara("modelType");
    String modelTypeName = "生产拆装单明细账本";
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
    String inStorageId = getPara("inStorage.id", "");
    String outStorageId = getPara("outStorage.id", "");
    String inStorageFullName = getPara("inStorage.fullName", "");
    String outStorageFullName = getPara("outStorage.fullName", "");
    String startTime = getPara("startTime", "");
    String endTime = getPara("endTime");
    setAttr("productFullName", ComFunController.getTableAttrById(configName, "b_product", Integer.valueOf(productId).intValue(), "fullName").toString());
    setAttr("inStorageFullName", inStorageFullName);
    setAttr("outStorageFullName", outStorageFullName);
    


    map.put("listID", "cc_removeCount_detail");
    map.put("modelType", modelType);
    map.put("aimDiv", aimDiv);
    map.put("productId", productId);
    map.put("inStorageId", inStorageId);
    map.put("outStorageId", outStorageId);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    Map<String, Object> dataMap = null;
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    
    dataMap = DismountCountReports.dao.dismountCountDetail(configName, map);
    setAttr("modelTypeName", modelTypeName);
    mapToResponse(map);
    return dataMap;
  }
  
  public void detailPrint()
    throws Exception
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "商品折装单明细账本");
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
    Map<String, Object> pageMap = dismonuntDetailOrPrint(map);
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
    colTitle.add("入库编号");
    colTitle.add("入库全名");
    colTitle.add("入库数量");
    colTitle.add("入库金额");
    colTitle.add("出库编号");
    colTitle.add("出库全名");
    colTitle.add("出库数量");
    colTitle.add("出库金额");
    colTitle.add("摘要");
    


    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Model pro = (Model)detail.get("pro");
      Model inSto = (Model)detail.get("inSto");
      Model outSto = (Model)detail.get("outSto");
      
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
      rowData.add(trimNull(inSto.getStr("code")));
      rowData.add(trimNull(inSto.getStr("fullName")));
      rowData.add(trimNull(detail.getBigDecimal("inAmounts")));
      rowData.add(trimNull(detail.getBigDecimal("inMoneys")));
      rowData.add(trimNull(outSto.getStr("code")));
      rowData.add(trimNull(outSto.getStr("fullName")));
      rowData.add(trimNull(detail.getBigDecimal("outAmounts")));
      rowData.add(trimNull(detail.getBigDecimal("outMoneys")));
      rowData.add(trimNull(detail.getStr("billRemark")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
