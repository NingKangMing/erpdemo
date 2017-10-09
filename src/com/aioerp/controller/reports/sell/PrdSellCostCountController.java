package com.aioerp.controller.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.reports.sell.PrdSellCostReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrdSellCostCountController
  extends BaseController
{
  private static final String prdListID = "xs_prdSellCostCount";
  
  public void toSearchDialog()
    throws ParseException
  {
    setStartDateAndEndDate();
    render("/WEB-INF/template/reports/sell/prdSellCostCount/searchDialog.html");
  }
  
  public void dialogSearch()
    throws SQLException, Exception
  {
    Map<String, Object> map = requestPageToMap(null, "recodeDate");
    String type = getPara(0, "first");
    setAttr("pageMap", com(map));
    
    columnConfig("xs505");
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/reports/sell/prdSellCostCount/page.html";
    } else if ((type.equals("page")) || (type.equals("tree"))) {
      returnPage = "/WEB-INF/template/reports/sell/prdSellCostCount/list.html";
    }
    render(returnPage);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String type = getPara(0, "first");
    

    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    int productId = getParaToInt("product.id", Integer.valueOf(0)).intValue();
    int staffId = getParaToInt("staff.id", Integer.valueOf(0)).intValue();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    String unitFullName = getPara("unit.fullName", "");
    String productFullName = getPara("product.fullName", "");
    String staffFullName = getPara("staff.name", "");
    String storageFullName = getPara("storage.fullName", "");
    String startDate = getPara("startDate");
    String endDate = getPara("endDate");
    String aimDiv = getPara("aimDiv", "all-all");
    String orderType = "";
    String isRCW = "";
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), startDate, endDate);
    }
    if (!type.equals("first")) {
      if (!type.equals("search")) {
        type.equals("page");
      }
    }
    String[] paras = aimDiv.split("-");
    orderType = paras[0];
    isRCW = paras[1];
    
    setAttr("unitFullName", unitFullName);
    setAttr("productFullName", productFullName);
    setAttr("staffFullName", staffFullName);
    setAttr("storageFullName", storageFullName);
    setAttr("aimDiv", aimDiv);
    setAttr("orderType", orderType);
    setAttr("isRCW", isRCW);
    
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    

    map.put("listID", "xs_prdSellCostCount");
    map.put("unitId", Integer.valueOf(unitId));
    map.put("productId", Integer.valueOf(productId));
    map.put("staffId", Integer.valueOf(staffId));
    map.put("storageId", Integer.valueOf(storageId));
    map.put("startDate", startDate);
    map.put("endDate", endDate);
    map.put("orderType", orderType);
    
    map.put("isRCW", isRCW);
    Map<String, Object> reportPrdSellCount = PrdSellCostReports.dao
      .reportPrdSellCostCount(configName, map);
    mapToResponse(map);
    return reportPrdSellCount;
  }
  
  public void prdXsCostDetail()
    throws Exception
  {
    setAttr("pageMap", costDetailCom());
    
    columnConfig("xs525");
    render("/WEB-INF/template/reports/sell/prdSellCostCount/prdSellCostDetail/page.html");
  }
  
  public Map<String, Object> costDetailCom()
    throws Exception
  {
    String configName = loginConfigName();
    
    int orderId = getParaToInt(0, getParaToInt("orderId", Integer.valueOf(0))).intValue();
    int billTypeId = getParaToInt(1, getParaToInt("billTypeId", Integer.valueOf(0))).intValue();
    setAttr("orderId", Integer.valueOf(orderId));
    setAttr("billTypeId", Integer.valueOf(billTypeId));
    
    String orderType = "";
    if (billTypeId == AioConstants.BILL_ROW_TYPE4) {
      orderType = "xsd";
    } else if (billTypeId == AioConstants.BILL_ROW_TYPE7) {
      orderType = "xsthd";
    } else if (billTypeId == AioConstants.BILL_ROW_TYPE13) {
      orderType = "xshhd";
    }
    Map<String, Object> sd = PrdSellCostReports.dao.prdsellCostDetail(
      configName, orderType, orderId);
    return sd;
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "商品销售毛利表");
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
    printCommonData(headData);
    headData.add("查询时间:" + getPara("startDate", "") + " 至 " + getPara("endDate", ""));
    

    colTitle.add("行号");
    colTitle.add("日期");
    colTitle.add("单据类型");
    colTitle.add("单据编号");
    colTitle.add("单位编号");
    colTitle.add("单位全称");
    colTitle.add("仓库编号");
    colTitle.add("仓库全称");
    colTitle.add("折后销售收入");
    colTitle.add("销售成本");
    colTitle.add("销售毛利");
    colTitle.add("摘要");
    colTitle.add("职员编号");
    colTitle.add("职员全称");
    colTitle.add("部门编号");
    colTitle.add("部门全称");
    

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
      Model unit = (Model)detail.get("unit");
      Model storage = (Model)detail.get("storage");
      Model department = (Model)detail.get("department");
      Model staff = (Model)detail.get("staff");
      rowData.add(trimNull(i + 1));
      rowData.add(trimRecordDateNull(detail.getDate("recodeDate")));
      rowData.add(trimNull(detail.getStr("billTypeName")));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(unit.getStr("code")));
      rowData.add(trimNull(unit.getStr("fullName")));
      rowData.add(trimNull(storage.getStr("code")));
      rowData.add(trimNull(storage.getStr("fullName")));
      rowData.add(trimNull(detail.getBigDecimal("sellTaxMoney")));
      rowData.add(trimNull(detail.getBigDecimal("sellCostMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("sellWinMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getStr("remark")));
      rowData.add(trimNull(staff.getStr("code")));
      rowData.add(trimNull(staff.getStr("fullName")));
      rowData.add(trimNull(department.getStr("code")));
      rowData.add(trimNull(department.getStr("fullName")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
  
  public void costDetailprint()
    throws Exception
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "商品销售成本表");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    List<Record> detailList = new ArrayList();
    










    Map<String, Object> pageMap = costDetailCom();
    if (pageMap != null)
    {
      detailList = (List)pageMap.get("pageList");
      if (detailList == null) {
        detailList = new ArrayList();
      }
    }
    printCommonData(headData);
    


    colTitle.add("行号");
    colTitle.add("日期");
    colTitle.add("商品编号");
    colTitle.add("商品全称");
    colTitle.add("销售单价");
    colTitle.add("销售数量");
    colTitle.add("折后销售收入");
    colTitle.add("成本单价");
    colTitle.add("成本金额");
    colTitle.add("销售毛利");
    colTitle.add("单位编号");
    colTitle.add("单位全称");
    colTitle.add("职员编号");
    colTitle.add("职员全称");
    colTitle.add("仓库编号");
    colTitle.add("仓库全称");
    

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
      rowData.add(trimNull(detail.getStr("productCode")));
      rowData.add(trimNull(detail.getStr("productFullName")));
      rowData.add(trimNull(detail.getBigDecimal("basePrice")));
      rowData.add(trimNull(detail.getBigDecimal("baseAmount")));
      rowData.add(trimNull(detail.getBigDecimal("sellMoney")));
      rowData.add(trimNull(detail.getBigDecimal("costPrice")));
      rowData.add(trimNull(detail.getBigDecimal("costMoneys")));
      rowData.add(trimNull(detail.getBigDecimal("sellWinMoney")));
      rowData.add(trimNull(detail.getStr("unitCode")));
      rowData.add(trimNull(detail.getStr("unitFullName")));
      rowData.add(trimNull(detail.getStr("staffCode")));
      rowData.add(trimNull(detail.getStr("staffFullName")));
      rowData.add(trimNull(detail.getStr("storageCode")));
      rowData.add(trimNull(detail.getStr("storageFullName")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
