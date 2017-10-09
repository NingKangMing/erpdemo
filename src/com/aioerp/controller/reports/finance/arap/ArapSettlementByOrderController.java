package com.aioerp.controller.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.reports.finance.arap.ArapSettlementByOrderReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArapSettlementByOrderController
  extends BaseController
{
  private static String listID = "cw_arapAnalysis";
  
  public void toSearchDialog()
    throws ParseException
  {
    setStartDateAndEndDate();
    render("/WEB-INF/template/reports/finance/arap/settlementByOrder/searchDialog.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String type = getPara(0, "first");
    setAttr("pageMap", com(map));
    
    columnConfig("cw516");
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/reports/finance/arap/settlementByOrder/page.html";
    } else if (type.equals("page")) {
      returnPage = "/WEB-INF/template/reports/finance/arap/settlementByOrder/list.html";
    }
    render(returnPage);
  }
  
  public void settlementDetail()
    throws ParseException
  {
    int billId = getParaToInt("billId", Integer.valueOf(0)).intValue();
    int billTypeId = getParaToInt("billTypeId").intValue();
    List<Record> list = ArapSettlementByOrderReports.dao.settlementByOrderDetail(loginConfigName(), billTypeId, billId);
    setAttr("list", list);
    render("/WEB-INF/template/reports/finance/arap/settlementByOrder/settlementDetail.html");
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    String configName = loginConfigName();
    String type = getPara(0, "first");
    if ((type.equals("first")) || (type.equals("search")))
    {
      map.put("isOver", "all");
      map.put("billType", "all");
    }
    else
    {
      map.put("isOver", getPara("isOver"));
      map.put("billType", getPara("billType"));
    }
    map.put("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
    map.put("staffId", getParaToInt("staff.id", Integer.valueOf(0)));
    map.put("unitFullName", getPara("unit.fullName"));
    map.put("staffFullName", getPara("staff.name"));
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), getPara("startDate"), getPara("endDate"));
    }
    map.put("listID", listID);
    Map<String, Object> pageMap = ArapSettlementByOrderReports.dao.settlementByOrder(configName, map);
    mapToResponse(map);
    return pageMap;
  }
  
  public void print()
    throws ParseException
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "按单结算查询");
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
    

    colTitle.add("行号");
    colTitle.add("单据类型");
    colTitle.add("单据编号");
    colTitle.add("日期");
    colTitle.add("收付款日期");
    colTitle.add("摘要");
    colTitle.add("金额");
    colTitle.add("余额");
    

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
      rowData.add(trimNull(detail.getStr("orderTypeName")));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimRecordDateNull(detail.getDate("recodeDate")));
      rowData.add(trimNull(detail.getDate("deliveryDate")));
      rowData.add(trimNull(detail.getStr("remark")));
      rowData.add(trimNull(detail.getBigDecimal("taxMoneys")));
      rowData.add(trimNull(detail.getBigDecimal("lastMoney")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
