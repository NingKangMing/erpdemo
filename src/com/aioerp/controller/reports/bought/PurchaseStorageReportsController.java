package com.aioerp.controller.reports.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.reports.bought.PurchaseStorageReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.CollectionUtils;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseStorageReportsController
  extends BaseController
{
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
    String[] storageIds = null;
    Integer supId = getParaToInt("supId", Integer.valueOf(0));
    String unitName = getPara("unit.fullName");
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
      storageIds = getParaValues("storageIds");
      String storages = getPara("storageIds", "");
      if (storages.indexOf(",") != -1) {
        storageIds = getPara("storageIds", "").split(",");
      }
    }
    else
    {
      orderTypes = getPara("orderTypes", "").split(",");
      storageIds = getPara("storageIds", "").split(",");
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
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    List<Model> storageList = Storage.dao.getChilds(configName, storageIds, storagePrivs);
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("orderTypes", orderTypes);
    map.put("storageList", storageList);
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
    Map<String, Object> pageMap = PurchaseStorageReports.dao.getSpreadPage(configName, pageNum, numPerPage, "purchaseStorageSpreadList", map);
    setAttr("params", map);
    setAttr("storageList", storageList);
    setAttr("unitName", unitName);
    setAttr("staffName", staffName);
    if (product != null) {
      setAttr("productName", product.get("fullName"));
    }
    if (orderTypes != null) {
      setAttr("orderTypes", CollectionUtils.strArrToString(orderTypes, ","));
    }
    if (storageIds != null) {
      setAttr("storageIds", CollectionUtils.strArrToString(storageIds, ","));
    }
    setAttr("supId", supId);
    setAttr("pageMap", pageMap);
    setAttr("isRow", isRow);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5011", AioConstants.STATUS_ENABLE));
    if (type == "first") {
      render("spread.html");
    } else {
      render("spreadList.html");
    }
  }
  
  public void printSpread()
    throws SQLException, Exception
  {
    String[] storageIds = getPara("storageIds", "").split(",");
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    String configName = loginConfigName();
    List<Model> storageList = Storage.dao.getChilds(configName, storageIds, storagePrivs);
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "仓库进货分布");
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
    for (Model storage : storageList)
    {
      colTitle.add(storage.getStr("fullName") + "数量");
      colTitle.add(storage.getStr("fullName") + "金额");
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
    for (int i = 0; i < storageList.size(); i++)
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
    map.put("unitId", getParaToInt("unit.id"));
    map.put("orderTypes", orderTypes);
    map.put("storageList", storageList);
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
    Map<String, Object> pageMap = PurchaseStorageReports.dao.getSpreadPage(configName, 1, totalCount, "purchaseStorageSpreadList", map);
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
      for (Model storage : storageList)
      {
        rowData.add(trimNull(model.get("baseAmount" + storage.getInt("id"))));
        if (!hasCostPermitted) {
          rowData.add("***");
        } else {
          rowData.add(trimNull(model.get("money" + storage.getInt("id"))));
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
