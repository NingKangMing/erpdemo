package com.aioerp.controller.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Unit;
import com.aioerp.model.reports.finance.arap.ArapMoneyCheckReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArapMoneyCheckController
  extends BaseController
{
  public void toSearchDialog()
    throws ParseException
  {
    setStartDateAndEndDate();
    
    Integer unitId = getParaToInt("selectedObjectId");
    if (unitId != null)
    {
      Unit unit = (Unit)Unit.dao.findById(loginConfigName(), unitId);
      setAttr("unit", unit);
    }
    render("/WEB-INF/template/reports/finance/arap/arapMoneyCheck/searchDialog.html");
  }
  
  public void dialogSearch()
    throws SQLException, Exception
  {
    Map<String, Object> map = requestPageToMap(null, null);
    

    String checkBy = getPara("checkBy", "byProduct");
    String columnType = "";
    if (checkBy.equals("byProduct")) {
      columnType = "cw514";
    } else {
      columnType = "cw515";
    }
    setAttr("pageMap", com(map));
    
    columnConfig(columnType);
    
    String returnPage = "/WEB-INF/template/reports/finance/arap/arapMoneyCheck/page.html";
    




    render(returnPage);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws SQLException, Exception
  {
    String type = getPara(0, "first");
    
    String configName = loginConfigName();
    String checkBy = getPara("checkBy", "byProduct");
    if ((type.equals("first")) || (type.equals("search")))
    {
      map.put("billType", Integer.valueOf(0));
      map.put("filter", "all");
      map.put("orderField", "t.recodeDate");
    }
    else
    {
      map.put("billType", getParaToInt("billType"));
      map.put("filter", getPara("filter"));
    }
    map.put("checkBy", checkBy);
    
    map.put("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
    map.put("staffId", getParaToInt("staff.id", Integer.valueOf(0)));
    map.put("unitFullName", getPara("unit.fullName"));
    map.put("staffFullName", getPara("staff.name"));
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), getPara("startDate"), getPara("endDate"));
    }
    Map<String, Object> pageMap = ArapMoneyCheckReports.dao.arapMoneyCheck(configName, map);
    mapToResponse(map);
    return pageMap;
  }
  
  public void print()
    throws SQLException, Exception
  {
    String checkBy = getPara("checkBy", "byProduct");
    
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "往来对账");
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
    
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    String unitFullName = "全部单位";
    if (unitId != 0) {
      unitFullName = Unit.dao.findById(loginConfigName(), Integer.valueOf(unitId)).getStr("fullName");
    }
    headData.add("往来单位:" + unitFullName);
    

    colTitle.add("行号");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("职员编号");
    colTitle.add("职员全名");
    colTitle.add("地区全名");
    colTitle.add("部门编号");
    colTitle.add("部门全名");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("单据日期");
    colTitle.add("单据类型");
    colTitle.add("单据编号");
    if (checkBy.equals("byProduct"))
    {
      colTitle.add("商品编号");
      colTitle.add("商品全名");
      colTitle.add("商品简名");
      colTitle.add("商品拼音码");
      colTitle.add("商品规格");
      colTitle.add("商品型号");
      colTitle.add("商品产地");
      colTitle.add("辅助数量");
      colTitle.add("商品数量");
      colTitle.add("计量单位");
      colTitle.add("含税价");
    }
    colTitle.add("科目编号");
    colTitle.add("科目全名");
    colTitle.add("应收增加");
    colTitle.add("应收减少");
    colTitle.add("应收余额");
    colTitle.add("备注");
    colTitle.add("附加说明");
    

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
      Model staff = (Model)detail.get("staff");
      Model dept = (Model)detail.get("dept");
      Model sto = (Model)detail.get("sto");
      Model pro = (Model)detail.get("pro");
      Model accounts = (Model)detail.get("accounts");
      
      String billTypeName = detail.getStr("billTypeName");
      if ((!billTypeName.equals("其它出库单")) && (!billTypeName.equals("其它入库单")) && (!billTypeName.equals("进货单")) && (!billTypeName.equals("进货退货单")) && (!billTypeName.equals("进货换货单"))) {
        hasCostPermitted = true;
      }
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(unit.getStr("code")));
      rowData.add(trimNull(unit.getStr("fullName")));
      rowData.add(trimNull(staff.getStr("code")));
      rowData.add(trimNull(staff.getStr("fullName")));
      rowData.add(trimNull(unit.getStr("areaFullName1")));
      rowData.add(trimNull(dept.getStr("code")));
      rowData.add(trimNull(dept.getStr("fullName")));
      rowData.add(trimNull(sto.getStr("code")));
      rowData.add(trimNull(sto.getStr("fullName")));
      rowData.add(trimRecordDateNull(detail.getDate("recodeDate")));
      rowData.add(trimNull(detail.getStr("billTypeName")));
      rowData.add(trimNull(detail.getStr("billCode")));
      if (checkBy.equals("byProduct"))
      {
        rowData.add(trimNull(pro.getStr("code")));
        rowData.add(trimNull(pro.getStr("fullName")));
        rowData.add(trimNull(pro.getStr("smallName")));
        rowData.add(trimNull(pro.getStr("spell")));
        rowData.add(trimNull(pro.getStr("standard")));
        rowData.add(trimNull(pro.getStr("model")));
        rowData.add(trimNull(pro.getStr("field")));
        rowData.add(trimNull(pro.getStr("helpAmount")));
        rowData.add(trimNull(detail.getBigDecimal("amount")));
        rowData.add(trimNull(pro.getStr("baseUnit")));
        rowData.add(trimNull(detail.getBigDecimal("taxMoney"), hasCostPermitted));
      }
      rowData.add(trimNull(accounts.getStr("code")));
      rowData.add(trimNull(accounts.getStr("fullName")));
      rowData.add(trimNull(detail.getBigDecimal("getMoney1"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("payMoney1"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("lastMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getStr("billRemark")));
      rowData.add(trimNull(detail.getStr("billMemo")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
