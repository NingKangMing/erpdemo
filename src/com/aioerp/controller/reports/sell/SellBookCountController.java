package com.aioerp.controller.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.reports.sell.SellBookReports;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellBookCountController
  extends BaseController
{
  public static final String searchListID = "xs_sellBookSearch1";
  private static final String excuteDetailListID = "xs_sellBookExcuteDetail";
  private static final String countListID = "xs_sellBookSearchCount";
  private static final String detailSearchListID = "xs_sellBookDetailSearch";
  
  public void toSearchDialog()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String startTime = sdf.format(DateUtils.addDays(new Date(), -7));
    setAttr("startDate", startTime);
    setAttr("endDate", DateUtils.getCurrentDate());
    render("/WEB-INF/template/reports/sell/sellBookCount/billSearch/searchDialog.html");
  }
  
  public void search()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String type = getPara(0, "first");
    setAttr("pageMap", searchOrSearchPrint(map));
    
    columnConfig("xs512");
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/reports/sell/sellBookCount/billSearch/page.html";
    } else if ((type.equals("page")) || (type.equals("tree"))) {
      returnPage = "/WEB-INF/template/reports/sell/sellBookCount/billSearch/list.html";
    }
    render(returnPage);
  }
  
  public Map<String, Object> searchOrSearchPrint(Map<String, Object> map)
    throws ParseException
  {
    String configName = loginConfigName();
    String type = getPara(0, "first");
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    int staffId = getParaToInt("staff.id", Integer.valueOf(0)).intValue();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    String unitFullName = "";
    String staffFullName = "";
    String storageFullName = "";
    String startDate = getPara("startDate", "");
    String endDate = getPara("endDate", "");
    String aimDiv = getPara("aimDiv", "all");
    if (type.equals("search"))
    {
      unitFullName = getPara("unit.fullName", "");
      staffFullName = getPara("staff.name", "");
      storageFullName = getPara("storage.fullName", "");
    }
    else
    {
      type.equals("page");
    }
    setAttr("unitFullName", unitFullName);
    setAttr("staffFullName", staffFullName);
    setAttr("storageFullName", storageFullName);
    

    map.put("listID", "xs_sellBookSearch1");
    map.put("unitId", Integer.valueOf(unitId));
    map.put("staffId", Integer.valueOf(staffId));
    map.put("storageId", Integer.valueOf(storageId));
    map.put("startDate", startDate);
    map.put("endDate", endDate);
    map.put("aimDiv", aimDiv);
    

    Map<String, Object> reportSellBookCount = SellBookReports.dao.reportBookSearchCount(configName, map);
    mapToResponse(map);
    
    setAttr("pageMap", reportSellBookCount);
    return reportSellBookCount;
  }
  
  public void executeDetail()
    throws SQLException, Exception
  {
    String type = getPara(0, "frist");
    setAttr("pageMap", executeDetailOrPrint());
    
    columnConfig("xs526");
    if (type.equals("frist")) {
      render("/WEB-INF/template/reports/sell/sellBookCount/billSearch/excuteDetail/page.html");
    } else if (type.equals("page")) {
      render("/WEB-INF/template/reports/sell/sellBookCount/billSearch/excuteDetail/list.html");
    }
  }
  
  public Map<String, Object> executeDetailOrPrint()
    throws SQLException, Exception
  {
    String type = getPara(0, "frist");
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    int billId = getParaToInt("billId", Integer.valueOf(0)).intValue();
    if (type.equals("frist")) {
      billId = getParaToInt("id", getParaToInt("billId", Integer.valueOf(0))).intValue();
    } else if (type.equals("page")) {
      billId = getParaToInt("billId", Integer.valueOf(0)).intValue();
    }
    Map<String, Object> pageMap = SellBookReports.dao.reportBookExcuteDetail(loginConfigName(), "xs_sellBookExcuteDetail", pageNum, numPerPage, billId);
    setAttr("listID", "xs_sellBookExcuteDetail");
    setAttr("billId", Integer.valueOf(billId));
    
    return pageMap;
  }
  
  public void executeDetailPrint()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "销售订单执行情况");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    List<Model> detailList = new ArrayList();
    










    Map<String, Object> pageMap = executeDetailOrPrint();
    if (pageMap != null)
    {
      detailList = (List)pageMap.get("pageList");
      if (detailList == null) {
        detailList = new ArrayList();
      }
    }
    printCommonData(headData);
    

    colTitle.add("行号");
    colTitle.add("单据类型");
    colTitle.add("单据编号");
    colTitle.add("执行日期");
    colTitle.add("摘要");
    colTitle.add("执行人编号");
    colTitle.add("执行人全称");
    colTitle.add("部门编号");
    colTitle.add("部门全称");
    colTitle.add("税后金额");
    colTitle.add("附加说明");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Model staff = (Model)detail.get("staff");
      Model department = (Model)detail.get("department");
      String billTypeName = "销售订单";
      if (detail.getStr("orderType").equals("xsd")) {
        billTypeName = "销售单";
      }
      rowData.add(trimNull(i + 1));
      rowData.add(billTypeName);
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getDate("updateTime")));
      rowData.add(trimNull(detail.getStr("remark")));
      rowData.add(trimNull(staff.getStr("code")));
      rowData.add(trimNull(staff.getStr("fullName")));
      rowData.add(trimNull(department.getStr("code")));
      rowData.add(trimNull(department.getStr("fullName")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.getBigDecimal("sellMoney")));
      rowData.add(trimNull(detail.getStr("memo")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
  
  public void searchPrint()
    throws ParseException
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "销售订单查询");
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
    Map<String, Object> pageMap = searchOrSearchPrint(map);
    if (pageMap != null) {
      detailList = (List)pageMap.get("pageList");
    }
    printCommonData(headData);
    headData.add("查询时间:" + getPara("startDate", "") + " 至 " + getPara("endDate", ""));
    

    colTitle.add("行号");
    colTitle.add("日期");
    colTitle.add("存盘时间");
    colTitle.add("订单编号");
    colTitle.add("单位编号");
    colTitle.add("单位全称");
    colTitle.add("经手人编号");
    colTitle.add("经手人全称");
    colTitle.add("到期日期");
    colTitle.add("折前金额");
    colTitle.add("折后金额");
    colTitle.add("税后金额");
    colTitle.add("是否完成");
    colTitle.add("附加说明");
    

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
      rowData.add(trimRecordDateNull(detail.getDate("recodeDate")));
      rowData.add(trimNull(detail.getDate("updateTime")));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("unitCode")));
      rowData.add(trimNull(detail.getStr("unitFullName")));
      rowData.add(trimNull(detail.getStr("staffCode")));
      rowData.add(trimNull(detail.getStr("staffFullName")));
      rowData.add(trimNull(detail.getDate("deliveryDate")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.getBigDecimal("discountMoney")));
      rowData.add(trimNull(detail.getBigDecimal("taxMoney")));
      rowData.add(trimNull(detail.getInt("relStatus")));
      rowData.add(trimNull(detail.getStr("memo")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
  
  public void toCountSearch()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String startTime = sdf.format(DateUtils.addDays(new Date(), -7));
    setAttr("startDate", startTime);
    setAttr("endDate", DateUtils.getCurrentDate());
    render("/WEB-INF/template/reports/sell/sellBookCount/count/searchDialog.html");
  }
  
  public void countSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, "objId");
    String type = getPara(0, "first");
    String modelType = getPara("modelType", "");
    setAttr("pageMap", countOrPrint(map));
    String columnType = "";
    if (modelType.equals("prd")) {
      columnType = "xs513";
    } else if (modelType.equals("unit")) {
      columnType = "xs514";
    } else if (modelType.equals("staff")) {
      columnType = "xs515";
    }
    columnConfig(columnType);
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/reports/sell/sellBookCount/count/page.html";
    } else if ((type.equals("page")) || (type.equals("tree"))) {
      returnPage = "/WEB-INF/template/reports/sell/sellBookCount/count/list.html";
    }
    render(returnPage);
  }
  
  public Map<String, Object> countOrPrint(Map<String, Object> map)
    throws ParseException
  {
    String modelType = getPara("modelType", "");
    String modelTypeName = "";
    int productId = getParaToInt("product.id", Integer.valueOf(0)).intValue();
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    int staffId = getParaToInt("staff.id", Integer.valueOf(0)).intValue();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    String productFullName = getPara("product.fullName", "");
    String unitFullName = getPara("unit.fullName", "");
    String staffFullName = getPara("staff.name", "");
    String storageFullName = getPara("storage.fullName", "");
    String startDate = getPara("startDate", "");
    String endDate = getPara("endDate", "");
    String aimDiv = getPara("aimDiv", "all");
    setAttr("productFullName", productFullName);
    setAttr("unitFullName", unitFullName);
    setAttr("staffFullName", staffFullName);
    setAttr("storageFullName", storageFullName);
    
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    if (modelType.equals("prd")) {
      modelTypeName = "商品";
    } else if (modelType.equals("unit")) {
      modelTypeName = "单位";
    } else if (modelType.equals("staff")) {
      modelTypeName = "职员";
    }
    setAttr("modelTypeName", modelTypeName);
    

    map.put("listID", "xs_sellBookSearchCount");
    map.put("modelType", modelType);
    map.put("productId", Integer.valueOf(productId));
    map.put("unitId", Integer.valueOf(unitId));
    map.put("staffId", Integer.valueOf(staffId));
    map.put("storageId", Integer.valueOf(storageId));
    map.put("startDate", startDate);
    map.put("endDate", endDate);
    map.put("aimDiv", aimDiv);
    

    Map<String, Object> reportSellBookCount = SellBookReports.dao.reportBookCount(loginConfigName(), map);
    mapToResponse(map);
    return reportSellBookCount;
  }
  
  public void countPrint()
    throws ParseException
  {
    String modelType = getPara("modelType", "");
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    String modelTypeName = "";
    if (modelType.equals("prd")) {
      modelTypeName = "商品";
    } else if (modelType.equals("unit")) {
      modelTypeName = "单位";
    } else if (modelType.equals("staff")) {
      modelTypeName = "职员";
    }
    data.put("reportName", "销售订单统计(" + modelTypeName + ")");
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
    Map<String, Object> pageMap = countOrPrint(map);
    if (pageMap != null) {
      detailList = (List)pageMap.get("pageList");
    }
    printCommonData(headData);
    headData.add("查询时间:" + getPara("startDate", "") + " 至 " + getPara("endDate", ""));
    

    colTitle.add("行号");
    colTitle.add(modelTypeName + "编号");
    colTitle.add(modelTypeName + "全称");
    colTitle.add("订货售价");
    colTitle.add("订货数量");
    colTitle.add("订货金额");
    colTitle.add("完成数量");
    colTitle.add("完成金额");
    colTitle.add("未完成数量");
    colTitle.add("未完成金额");
    colTitle.add("补订数量");
    colTitle.add("补订货金额");
    colTitle.add("强制完成数量");
    colTitle.add("强制完成金额");
    colTitle.add("赠品数量");
    if (modelType.equals("prd"))
    {
      colTitle.add("订货辅助数量");
      colTitle.add("完成辅助数量");
      colTitle.add("未完成辅助数量");
      colTitle.add("补订辅助数量");
      colTitle.add("强制完成辅助数量");
      colTitle.add("赠品辅助数量");
    }
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
      rowData.add(trimNull(detail.getStr("objCode")));
      rowData.add(trimNull(detail.getStr("objFullName")));
      rowData.add(trimNull(detail.getBigDecimal("basePrice")));
      rowData.add(trimNull(detail.getBigDecimal("baseAmount")));
      rowData.add(trimNull(detail.getBigDecimal("taxMoney")));
      rowData.add(trimNull(detail.getBigDecimal("arrivalAmount")));
      rowData.add(trimNull(detail.getBigDecimal("arrivalMoney")));
      rowData.add(trimNull(detail.getBigDecimal("untreatedAmount")));
      rowData.add(trimNull(detail.getBigDecimal("untreatedMoney")));
      rowData.add(trimNull(detail.getBigDecimal("replenishAmount")));
      rowData.add(trimNull(detail.getBigDecimal("replenishMoney")));
      rowData.add(trimNull(detail.getBigDecimal("forceAmount")));
      rowData.add(trimNull(detail.getBigDecimal("forceMoney")));
      rowData.add(trimNull(detail.getBigDecimal("presentAmount")));
      if (modelType.equals("prd"))
      {
        rowData.add(trimNull(detail.getStr("baseAmountHelp")));
        rowData.add(trimNull(detail.getStr("arrivalAmountHelp")));
        rowData.add(trimNull(detail.getStr("untreatedAmountHelp")));
        rowData.add(trimNull(detail.getStr("replenishAmountHelp")));
        rowData.add(trimNull(detail.getStr("forceAmountHelp")));
        rowData.add(trimNull(detail.getStr("presentAmountHelp")));
      }
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
  
  public void executeLook()
  {
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String type = getPara(1, "xsd");
    if ("xsd".equals(type)) {
      forwardAction("/sell/sell/look/" + billId);
    } else if ("xsdd".equals(type)) {
      forwardAction("/sell/book/look/" + billId);
    }
  }
  
  public void toSearchDetail()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String startTime = sdf.format(DateUtils.addDays(new Date(), -7));
    setAttr("startDate", startTime);
    setAttr("endDate", DateUtils.getCurrentDate());
    render("/WEB-INF/template/reports/sell/sellBookCount/detailSearch/searchDialog.html");
  }
  
  public void detailSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String type = getPara(0, "first");
    setAttr("pageMap", detailOrPrint(map));
    
    columnConfig("xs516");
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/reports/sell/sellBookCount/detailSearch/page.html";
    } else if ((type.equals("page")) || (type.equals("tree"))) {
      returnPage = "/WEB-INF/template/reports/sell/sellBookCount/detailSearch/list.html";
    } else if (type.equals("productDetail")) {
      returnPage = "/WEB-INF/template/reports/sell/sellBookCount/detailSearch/page.html";
    }
    render(returnPage);
  }
  
  public Map<String, Object> detailOrPrint(Map<String, Object> map)
    throws ParseException
  {
    String type = getPara(0, "first");
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    

    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    int productId = getParaToInt("product.id", Integer.valueOf(0)).intValue();
    int staffId = getParaToInt("staff.id", Integer.valueOf(0)).intValue();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    String unitFullName = getPara("unit.fullName", "");
    String productFullName = getPara("product.fullName", "");
    String staffFullName = getPara("staff.name", "");
    String storageFullName = getPara("storage.fullName", "");
    String startDate = getPara("startDate");
    Date endDate = getParaToDate("endDate");
    setAttr("unitFullName", unitFullName);
    setAttr("productFullName", productFullName);
    setAttr("staffFullName", staffFullName);
    setAttr("storageFullName", storageFullName);
    if (type.equals("productDetail")) {
      productId = getParaToInt("id", Integer.valueOf(0)).intValue();
    }
    map.put("listID", "xs_sellBookDetailSearch");
    map.put("unitId", Integer.valueOf(unitId));
    map.put("productId", Integer.valueOf(productId));
    map.put("staffId", Integer.valueOf(staffId));
    map.put("storageId", Integer.valueOf(storageId));
    map.put("startDate", startDate);
    map.put("endDate", StringUtil.dataToStr(endDate));
    Map<String, Object> reportPrdSellCount = SellBookReports.dao.reportBookDetailCount(loginConfigName(), map);
    mapToResponse(map);
    return reportPrdSellCount;
  }
  
  public void detailPrint()
    throws ParseException
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "销售订单明细查询");
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
    Map<String, Object> pageMap = detailOrPrint(map);
    if (pageMap != null)
    {
      detailList = (List)pageMap.get("pageList");
      if (detailList == null) {
        detailList = new ArrayList();
      }
    }
    printCommonData(headData);
    headData.add("查询时间:" + getPara("startDate", "") + " 至 " + getPara("endDate", ""));
    


    colTitle.add("行号");
    colTitle.add("日期");
    colTitle.add("单据编号");
    colTitle.add("商品编号");
    colTitle.add("商品全称");
    colTitle.add("单位编号");
    colTitle.add("单位全称");
    colTitle.add("经手人编号");
    colTitle.add("经手人全称");
    colTitle.add("仓库编号");
    colTitle.add("仓库全称");
    colTitle.add("交货日期");
    colTitle.add("订货售价");
    colTitle.add("订货数量");
    colTitle.add("订货金额");
    colTitle.add("未完成数量");
    colTitle.add("未完成金额");
    colTitle.add("完成数量");
    colTitle.add("完成金额");
    colTitle.add("强制完成数量");
    colTitle.add("强制完成金额");
    colTitle.add("补订数量");
    colTitle.add("补订货金额");
    colTitle.add("赠品数量");
    colTitle.add("摘要");
    

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
      rowData.add(trimRecordDateNull(detail.getDate("recodeDate")));
      rowData.add(trimNull(detail.getStr("billCode")));
      rowData.add(trimNull(detail.getStr("productCode")));
      rowData.add(trimNull(detail.getStr("productFullName")));
      rowData.add(trimNull(detail.getStr("unitCode")));
      rowData.add(trimNull(detail.getStr("unitFullName")));
      rowData.add(trimNull(detail.getStr("staffCode")));
      rowData.add(trimNull(detail.getStr("staffFullName")));
      rowData.add(trimNull(detail.getStr("storageCode")));
      rowData.add(trimNull(detail.getStr("storageFullName")));
      rowData.add(trimNull(detail.getDate("deliveryDate")));
      rowData.add(trimNull(detail.getBigDecimal("basePrice")));
      rowData.add(trimNull(detail.getBigDecimal("baseAmount")));
      rowData.add(trimNull(detail.getBigDecimal("taxMoney")));
      rowData.add(trimNull(detail.getBigDecimal("untreatedAmount")));
      rowData.add(trimNull(detail.getBigDecimal("untreatedMoney")));
      rowData.add(trimNull(detail.getBigDecimal("arrivalAmount")));
      rowData.add(trimNull(detail.getBigDecimal("arrivalMoney")));
      rowData.add(trimNull(detail.getBigDecimal("forceAmount")));
      rowData.add(trimNull(detail.getBigDecimal("forceMoney")));
      rowData.add(trimNull(detail.getBigDecimal("replenishAmount")));
      rowData.add(trimNull(detail.getBigDecimal("replenishMoney")));
      rowData.add(trimNull(detail.getBigDecimal("presentAmount")));
      rowData.add(trimNull(detail.getStr("remark")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
