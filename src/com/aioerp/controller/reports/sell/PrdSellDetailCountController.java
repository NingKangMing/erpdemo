package com.aioerp.controller.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.reports.sell.SellReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.CollectionUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrdSellDetailCountController
  extends BaseController
{
  private static final String LISTID = "xs_prdSellDetailCount";
  
  public void toSearchDialog()
    throws ParseException
  {
    setStartDateAndEndDate();
    render("/WEB-INF/template/reports/sell/prdSellDetailCount/searchDialog.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, "productCode");
    String type = getPara(0, "first");
    
    columnConfig("xs528");
    
    setAttr("pageMap", com(map));
    if (("first".equals(type)) || ("search".equals(type))) {
      render("/WEB-INF/template/reports/sell/prdSellDetailCount/page.html");
    } else {
      render("/WEB-INF/template/reports/sell/prdSellDetailCount/list.html");
    }
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    String configName = loginConfigName();
    int staffId = getParaToInt("staff.id", Integer.valueOf(0)).intValue();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    int productId = getParaToInt("product.id", Integer.valueOf(0)).intValue();
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    int areaId = getParaToInt("area.id", Integer.valueOf(0)).intValue();
    
    String unitFullName = getPara("unit.fullName", "");
    String productFullName = getPara("product.fullName", "");
    String areaFullName = getPara("area.fullName", "");
    String staffFullName = getPara("staff.name", "");
    String storageFullName = getPara("storage.fullName", "");
    
    String startDate = getPara("startDate");
    String endDate = getPara("endDate");
    String aimDiv = getPara("aimDiv", "all-all");
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), startDate, endDate);
    }
    String type = getPara(0, "first");
    
    String[] orderTypes = getPara("pageOrderTypes", "").split(",");
    if ((type.equals("first")) || (type.equals("search")))
    {
      orderTypes = getParaValues("orderTypes");
      if (orderTypes == null) {
        orderTypes = getPara("pageOrderTypes", "").split(",");
      }
    }
    else if (type.equals("page"))
    {
      orderTypes = getPara("pageOrderTypes", "").split(",");
    }
    map.put("staffId", Integer.valueOf(staffId));
    map.put("storageId", Integer.valueOf(storageId));
    map.put("unitId", Integer.valueOf(unitId));
    map.put("productId", Integer.valueOf(productId));
    map.put("areaId", Integer.valueOf(areaId));
    
    map.put("orderTypes", orderTypes);
    map.put("startDate", startDate);
    map.put("endDate", endDate);
    map.put("aimDiv", aimDiv);
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    map.put("listID", "xs_prdSellDetailCount");
    
    Map<String, Object> pageMap = SellReports.dao.reportPrdSellDetailCount(configName, map);
    

    setAttr("staffFullName", staffFullName);
    setAttr("storageFullName", storageFullName);
    setAttr("unitFullName", unitFullName);
    setAttr("productFullName", productFullName);
    setAttr("areaFullName", areaFullName);
    
    mapToResponse(map);
    setAttr("orderTypes", CollectionUtils.strArrToString(orderTypes, ","));
    return pageMap;
  }
  
  public void print()
    throws ParseException
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(528));
    data.put("reportName", "商品销售明细汇总表");
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
    colTitle.add("单据类型");
    colTitle.add("单据日期");
    colTitle.add("单据编号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("职员编号");
    colTitle.add("职员全名");
    colTitle.add("部门编号");
    colTitle.add("部门全名");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("销售数量");
    colTitle.add("辅助单位");
    colTitle.add("辅助数量");
    colTitle.add("单价");
    colTitle.add("金额");
    colTitle.add("折扣");
    colTitle.add("折后单价");
    colTitle.add("折后金额");
    colTitle.add("税额");
    colTitle.add("含税单价");
    colTitle.add("含税金额");
    colTitle.add("成本金额");
    colTitle.add("备注");
    

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
      rowData.add(trimNull(detail.getLong("billTypeId")));
      rowData.add(trimNull(detail.getTimestamp("recodeDate")));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("productCode")));
      rowData.add(trimNull(detail.getStr("productFullName")));
      rowData.add(trimNull(detail.getStr("staffCode")));
      rowData.add(trimNull(detail.getStr("staffFullName")));
      rowData.add(trimNull(detail.getStr("departmentCode")));
      rowData.add(trimNull(detail.getStr("departmentFullName")));
      rowData.add(trimNull(detail.getStr("storageCode")));
      rowData.add(trimNull(detail.getStr("storageFullName")));
      rowData.add(trimNull(detail.getStr("unitCode")));
      rowData.add(trimNull(detail.getStr("unitFullName")));
      
      rowData.add(trimNull(detail.getBigDecimal("baseAmount")));
      rowData.add(trimNull(detail.getStr("assistUnit")));
      rowData.add(trimNull(detail.getStr("helpAmount")));
      rowData.add(trimNull(detail.getBigDecimal("basePrice")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.getBigDecimal("discount")));
      rowData.add(trimNull(detail.getBigDecimal("discountPrice")));
      rowData.add(trimNull(detail.getBigDecimal("discountMoney")));
      rowData.add(trimNull(detail.getBigDecimal("taxes")));
      rowData.add(trimNull(detail.getBigDecimal("taxPrice")));
      rowData.add(trimNull(detail.getBigDecimal("taxMoney")));
      rowData.add(trimNull(detail.getBigDecimal("costMoneys")));
      rowData.add(trimNull(detail.getStr("remark")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
