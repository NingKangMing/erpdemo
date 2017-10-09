package com.aioerp.controller.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.reports.sell.SellLayoutReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.CollectionUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellLayoutCountController
  extends BaseController
{
  private static final String storageListID = "xs_prdSellCount";
  private static final String unitListID = "xs_prdSellCount";
  
  public void toSearchDialog()
    throws ParseException
  {
    String modelType = getPara(0, "");
    setAttr("modelType", modelType);
    setStartDateAndEndDate();
    render("/WEB-INF/template/reports/sell/unitSellDisplayCount/searchDialog.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String type = getPara(0, "first");
    setAttr("pageMap", com(map));
    
    columnConfig("xs527");
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/reports/sell/unitSellDisplayCount/page.html";
    } else if ((type.equals("page")) || (type.equals("tree")) || (type.equals("up")) || (type.equals("line"))) {
      returnPage = "/WEB-INF/template/reports/sell/unitSellDisplayCount/list.html";
    }
    render(returnPage);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    String configName = loginConfigName();
    String modelType = getPara("modelType", "");
    String type = getPara(0, "first");
    map.put("node", getParaToInt("node", Integer.valueOf(AioConstants.NODE_2)));
    

    String unitId = getPara("unit.id", getPara("unitIds", "0"));
    Integer staffId = getParaToInt("staff.id", Integer.valueOf(0));
    String storageId = getPara("storage.id", getPara("storageIds", "0"));
    String unitFullName = getPara("unit.fullName", "");
    String staffFullName = getPara("staff.name", "");
    String storageFullName = getPara("storage.fullName", "");
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    

    String[] orderTypes = null;
    if ((!type.equals("first")) && (!type.equals("search"))) {
      if (type.equals("tree")) {
        supId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
      } else if (!type.equals("page")) {
        if (type.equals("up"))
        {
          int oldSupId = getParaToInt("supId").intValue();
          setAttr("objectId", Integer.valueOf(oldSupId));
          supId = Product.dao.getPsupId(configName, oldSupId);
        }
        else if (type.equals("line"))
        {
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
    Date endDate = getParaToDate("endDate");
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), startDate, DateUtils.format(endDate));
    }
    String aimDiv = getPara("aimDiv", "after");
    
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    


    String modelTypeName = "";
    if (modelType.equals("unit"))
    {
      modelTypeName = "单位";
      map.put("listID", "xs_prdSellCount");
    }
    else if (modelType.equals("storage"))
    {
      modelTypeName = "仓库";
      map.put("listID", "xs_prdSellCount");
    }
    map.put("aimDiv", getPara("aimDiv", ""));
    map.put("modelType", modelType);
    map.put("supId", Integer.valueOf(supId));
    map.put("unitId", unitId);
    map.put("staffId", staffId);
    map.put("storageId", storageId);
    map.put("orderTypes", orderTypes);
    map.put("startDate", startDate);
    map.put("aimDiv", aimDiv);
    map.put("endDate", StringUtil.dataToStr(endDate));
    
    boolean flag = isInitReportOtherCondition(map);
    if (flag) {
      map.put("aimDiv", "after");
    }
    List<Model> objList = null;
    if (modelType.equals("unit"))
    {
      objList = Unit.dao.getUnits(configName, basePrivs(UNIT_PRIVS), unitId);
    }
    else if (modelType.equals("storage"))
    {
      String storagePrivs = basePrivs(STORAGE_PRIVS);
      objList = Storage.dao.getStorages(configName, storagePrivs, storageId);
    }
    map.put("objList", objList);
    Map<String, Object> reportPrdSellCount = null;
    reportPrdSellCount = SellLayoutReports.dao.reportSellLayoutCount(configName, map);
    setAttr("modelTypeName", modelTypeName);
    setAttr("unitFullName", unitFullName);
    setAttr("staffFullName", staffFullName);
    setAttr("storageFullName", storageFullName);
    mapToResponse(map);
    setAttr("orderTypes", CollectionUtils.strArrToString(orderTypes, ","));
    return reportPrdSellCount;
  }
  
  public void print()
    throws ParseException
  {
    String modelType = getPara("modelType", "");
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    if (modelType.equals("unit")) {
      data.put("reportName", "单位销售分布");
    } else if (modelType.equals("storage")) {
      data.put("reportName", "仓库销售分布");
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
    Map<String, Object> pageMap = com(map);
    if (pageMap != null) {
      detailList = (List)pageMap.get("pageList");
    }
    List<Model> objList = (List)map.get("objList");
    

    printCommonData(headData);
    headData.add("查询时间:" + getPara("startDate", "") + " 至 " + getPara("endDate", ""));
    

    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简称");
    colTitle.add("商品拼音");
    colTitle.add("规格");
    colTitle.add("型号");
    colTitle.add("产地");
    for (int i = 0; i < objList.size(); i++)
    {
      Model m = (Model)objList.get(i);
      colTitle.add(m.getStr("fullName") + "数量");
      colTitle.add(m.getStr("fullName") + "金额");
    }
    colTitle.add("合计数量");
    colTitle.add("合计金额");
    


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
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("fullName")));
      rowData.add(trimNull(detail.getStr("smallName")));
      rowData.add(trimNull(detail.getStr("spell")));
      rowData.add(trimNull(detail.getStr("standard")));
      rowData.add(trimNull(detail.getStr("model")));
      rowData.add(trimNull(detail.getStr("field")));
      for (int j = 0; j < objList.size(); j++)
      {
        Model m = (Model)objList.get(j);
        rowData.add(trimNull(detail.getBigDecimal("amount" + m.getInt("id"))));
        rowData.add(trimNull(detail.getBigDecimal("money" + m.getInt("id"))));
      }
      rowData.add(trimNull(detail.getBigDecimal("allAmount")));
      rowData.add(trimNull(detail.getBigDecimal("allMoneys")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
