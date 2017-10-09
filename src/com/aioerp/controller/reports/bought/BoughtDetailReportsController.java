package com.aioerp.controller.reports.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.bought.BoughtDetail;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.sys.SysUserSearchDate;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoughtDetailReportsController
  extends BaseController
{
  public void index()
    throws ParseException
  {
    setStartDateAndEndDate();
    
    render("search.html");
  }
  
  public void search()
    throws Exception
  {
    String configName = loginConfigName();
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(loginConfigName(), loginUserId(), getPara("startDate"), getPara("endDate"));
    }
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String productName = getPara("product.fullName");
    String unitName = getParaOne("unit.fullName");
    String staffName = getParaOne("staff.name");
    String storageName = getPara("storage.fullName");
    String type = getPara(0, "first");
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("productId", getParaToInt("product.id"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    
    Map<String, Object> pageMap = BoughtDetail.dao.getPage(configName, pageNum, numPerPage, "boughtDetailReportsList", map);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5018", AioConstants.STATUS_ENABLE));
    
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("params", map);
    setAttr("productName", productName);
    setAttr("unitName", unitName);
    setAttr("staffName", staffName);
    setAttr("storageName", storageName);
    setAttr("pageMap", pageMap);
    if ("refresh".equals(type)) {
      render("list.html");
    } else {
      render("page.html");
    }
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "进货订单明细查询");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startDate", "") + "至" + getPara("endDate", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    colTitle.add("行号");
    colTitle.add("单据类型");
    colTitle.add("订单编号");
    colTitle.add("日期");
    
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简称");
    colTitle.add("商品拼音");
    colTitle.add("规格");
    colTitle.add("型号");
    colTitle.add("产地");
    
    colTitle.add("订货辅助数量");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("经手人编号");
    colTitle.add("经手人全名");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("交货日期");
    colTitle.add("订货进价");
    colTitle.add("订货数量");
    colTitle.add("赠品数量");
    colTitle.add("补订数量");
    colTitle.add("完成数量");
    colTitle.add("强制完成数量");
    colTitle.add("未完成数量");
    colTitle.add("订货金额");
    colTitle.add("补订货金额");
    colTitle.add("完成金额");
    colTitle.add("强制完成金额");
    colTitle.add("未完成金额");
    colTitle.add("摘要");
    colTitle.add("附加说明");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("productId", getParaToInt("product.id"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    
    Map<String, Object> pageMap = BoughtDetail.dao.getPage(loginConfigName(), 1, totalCount, "boughtDetailReportsList", map);
    List<Model> list = (List)pageMap.get("pageList");
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < list.size(); i++)
    {
      BoughtDetail model = (BoughtDetail)list.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add("进货订单");
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("recodeDate")));
      
      Model product = (Model)model.get("product");
      if (product != null)
      {
        rowData.add(trimNull(product.get("code")));
        rowData.add(trimNull(product.get("fullName")));
        rowData.add(trimNull(product.get("smallName")));
        rowData.add(trimNull(product.get("spell")));
        rowData.add(trimNull(product.get("standard")));
        rowData.add(trimNull(product.get("model")));
        rowData.add(trimNull(product.get("field")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
        rowData.add("");
        rowData.add("");
        rowData.add("");
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(model.getHelpAmount("baseAmount")));
      Model unit = (Model)model.get("unit");
      if (unit != null)
      {
        rowData.add(trimNull(unit.get("code")));
        rowData.add(trimNull(unit.get("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Model staff = (Model)model.get("staff");
      if (unit != null)
      {
        rowData.add(trimNull(staff.get("code")));
        rowData.add(trimNull(staff.get("name")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Model storage = (Model)model.get("storage");
      if (unit != null)
      {
        rowData.add(trimNull(storage.get("code")));
        rowData.add(trimNull(storage.get("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(model.get("deliveryDate")));
      rowData.add(trimNull(model.get("basePrice")));
      rowData.add(trimNull(model.get("baseAmount")));
      rowData.add(trimNull(model.get("giftAmount")));
      rowData.add(trimNull(model.get("replenishAmount")));
      rowData.add(trimNull(model.get("arrivalAmount")));
      rowData.add(trimNull(model.get("forceAmount")));
      rowData.add(trimNull(model.get("untreatedAmount")));
      if (!hasCostPermitted)
      {
        rowData.add("***");
        rowData.add("***");
        rowData.add("***");
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        rowData.add(trimNull(model.get("bargainMoney")));
        rowData.add(trimNull(model.getReplenishMoney()));
        rowData.add(trimNull(model.getArrivalMoney()));
        rowData.add(trimNull(model.getForceMoney()));
        rowData.add(trimNull(model.getUntreatedMoney()));
      }
      rowData.add(trimNull(model.get("remark")));
      rowData.add(trimNull(model.get("memo")));
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
