package com.aioerp.controller.reports.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.aioerp.model.bought.BoughtDetail;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoughtReportsController
  extends BaseController
{
  public void index()
    throws ParseException
  {
    setStartDateAndEndDate();
    setAttr("status", getPara("status"));
    render("dialogSearch.html");
  }
  
  public void reports()
    throws Exception
  {
    String configName = loginConfigName();
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), getPara("startDate"), getPara("endDate"));
    }
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String productName = getPara("product.fullName");
    String unitName = getParaOne("unit.fullName");
    String staffName = getParaOne("staff.name");
    String storageName = getPara("storage.fullName");
    int way = getParaToInt("way", Integer.valueOf(1)).intValue();
    
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("productId", getParaToInt("product.id"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("way", Integer.valueOf(way));
    map.put("status", getPara("status"));
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    
    Map<String, Object> pageMap = BoughtDetail.dao.getStatPage(configName, pageNum, numPerPage, null, map);
    if (way == 2) {
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5015", AioConstants.STATUS_ENABLE));
    } else if (way == 3) {
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5016", AioConstants.STATUS_ENABLE));
    } else {
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5014", AioConstants.STATUS_ENABLE));
    }
    setAttr("params", map);
    setAttr("productName", productName);
    setAttr("unitName", unitName);
    setAttr("staffName", staffName);
    setAttr("storageName", storageName);
    setAttr("pageMap", pageMap);
    render("reports.html");
  }
  
  public void go()
  {
    setAttr("statusCode", Integer.valueOf(200));
    
    setAttr("aimTabId", getPara("aimTabId", ""));
    setAttr("aimUrl", getPara("aimUrl", ""));
    setAttr("aimTitle", getPara("aimTitle", ""));
    

    Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
    paraMap.remove("aimTabId");
    paraMap.remove("aimUrl");
    paraMap.remove("aimTitle");
    if (paraMap.size() > 0) {
      setAttr("data", paraMap);
    }
    renderJson();
  }
  
  public void search()
    throws Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String productName = getPara("product.fullName");
    String unitName = getPara("unit.fullName");
    String staffName = getPara("staff.name");
    String storageName = getPara("storage.fullName");
    int way = getParaToInt("way", Integer.valueOf(1)).intValue();
    
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("productId", getParaToInt("product.id"));
    map.put("unitId", getParaToInt("unit.id"));
    map.put("staffId", getParaToInt("staff.id"));
    map.put("storageId", getParaToInt("storage.id"));
    map.put("way", Integer.valueOf(way));
    map.put("status", getPara("status"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    Map<String, Object> pageMap = BoughtDetail.dao.getStatPage(configName, pageNum, numPerPage, "boughtBillReportsList", map);
    if (way == 2) {
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5015", AioConstants.STATUS_ENABLE));
    } else if (way == 3) {
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5016", AioConstants.STATUS_ENABLE));
    } else {
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5014", AioConstants.STATUS_ENABLE));
    }
    setAttr("params", map);
    setAttr("productName", productName);
    setAttr("unitName", unitName);
    setAttr("staffName", staffName);
    setAttr("storageName", storageName);
    setAttr("pageMap", pageMap);
    render("list.html");
  }
  
  public void look()
    throws Exception
  {
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String configName = loginConfigName();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String productName = getPara("product.fullName");
    String unitName = getPara("unit.fullName");
    String staffName = getPara("staff.name");
    String storageName = getPara("storage.fullName");
    String type = getPara(1, "first");
    int way = getParaToInt("way", Integer.valueOf(1)).intValue();
    Integer unitId = getParaToInt("unit.id");
    Integer staffId = getParaToInt("staff.id");
    Integer productId = getParaToInt("product.id");
    Integer objId = getParaToInt(0);
    if (way == 3)
    {
      staffId = objId;
      staffName = Staff.dao.findById(configName, staffId).getStr("name");
    }
    else if (way == 2)
    {
      unitId = objId;
      unitName = Unit.dao.findById(configName, unitId).getStr("fullName");
    }
    else
    {
      productId = objId;
      productName = Product.dao.findById(configName, productId).getStr("fullName");
    }
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("productId", productId);
    map.put("unitId", unitId);
    map.put("staffId", staffId);
    map.put("storageId", getParaToInt("storage.id"));
    map.put("status", getPara("status"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    
    Map<String, Object> pageMap = BoughtDetail.dao.getPage(configName, pageNum, numPerPage, "boughtDetailReportsList", map);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5017", AioConstants.STATUS_ENABLE));
    setAttr("params", map);
    setAttr("productName", productName);
    setAttr("unitName", unitName);
    setAttr("staffName", staffName);
    setAttr("storageName", storageName);
    setAttr("pageMap", pageMap);
    setAttr("objId", objId);
    setAttr("way", Integer.valueOf(way));
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    if ("refresh".equals(type)) {
      render("lookList.html");
    } else {
      render("look.html");
    }
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "进货订单统计");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startDate", "") + "至" + getPara("endDate", ""));
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    int way = getParaToInt("way", Integer.valueOf(1)).intValue();
    
    colTitle.add("行号");
    if (way == 3)
    {
      colTitle.add("职员编号");
      colTitle.add("职员全名");
    }
    else if (way == 2)
    {
      colTitle.add("单位编号");
      colTitle.add("单位全名");
    }
    else
    {
      colTitle.add("商品编号");
      colTitle.add("商品全名");
      colTitle.add("商品简称");
      colTitle.add("商品拼音");
      colTitle.add("规格");
      colTitle.add("型号");
      colTitle.add("产地");
    }
    colTitle.add("订货进价");
    colTitle.add("订货数量");
    colTitle.add("补货数量");
    colTitle.add("完成数量");
    colTitle.add("强制完成数量");
    colTitle.add("未完成数量");
    colTitle.add("订货金额");
    colTitle.add("补订货金额");
    colTitle.add("完成金额");
    colTitle.add("强制完成金额");
    colTitle.add("未完成金额");
    colTitle.add("赠品数量");
    
    colWidth.add("30");
    if (way == 3)
    {
      colWidth.add("500");
      colWidth.add("500");
    }
    else if (way == 2)
    {
      colWidth.add("500");
      colWidth.add("500");
    }
    else
    {
      colWidth.add("500");
      colWidth.add("500");
      colWidth.add("500");
      colWidth.add("500");
      colWidth.add("500");
      colWidth.add("500");
      colWidth.add("500");
    }
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
    map.put("way", Integer.valueOf(way));
    map.put("status", getPara("status"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("productPrivs", basePrivs(PRODUCT_PRIVS));
    Map<String, Object> pageMap = BoughtDetail.dao.getStatPage(loginConfigName(), 1, totalCount, "boughtBillReportsList", map);
    List<Model> list = (List)pageMap.get("pageList");
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < list.size(); i++)
    {
      BoughtDetail model = (BoughtDetail)list.get(i);
      rowData.add(trimNull(i + 1));
      if (way == 3)
      {
        Model staff = (Model)model.get("staff");
        if (staff != null)
        {
          rowData.add(trimNull(staff.get("code")));
          rowData.add(trimNull(staff.get("name")));
        }
        else
        {
          rowData.add("");
          rowData.add("");
        }
      }
      else if (way == 2)
      {
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
      }
      else
      {
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
      }
      if (!hasCostPermitted) {
        rowData.add("***");
      } else {
        rowData.add(trimNull(model.get("basePricAvg")));
      }
      rowData.add(trimNull(model.get("baseAmountSum")));
      rowData.add(trimNull(model.get("replenishAmountSum")));
      rowData.add(trimNull(model.get("arrivalAmountSum")));
      rowData.add(trimNull(model.get("forceAmountSum")));
      rowData.add(trimNull(model.get("untreatedAmountSum")));
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
        rowData.add(trimNull(model.get("bargainMoneySum")));
        rowData.add(trimNull(model.get("replenishMoneySum")));
        rowData.add(trimNull(model.get("arrivalMoneySum")));
        rowData.add(trimNull(model.get("forceMoneySum")));
        rowData.add(trimNull(model.get("untreatedMoneySum")));
      }
      rowData.add(trimNull(model.get("giftAmountSum")));
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
  
  public void printLook()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "进货订单统计详情");
    List<String> headData = new ArrayList();
    
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
    
    colTitle.add("订货辅助数量");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("订货日期");
    colTitle.add("到货日期");
    colTitle.add("订货数量");
    colTitle.add("补订数量");
    colTitle.add("完成数量");
    colTitle.add("强制完成数量");
    colTitle.add("未完成数量");
    colTitle.add("订货金额");
    colTitle.add("补订货金额");
    colTitle.add("完成金额");
    colTitle.add("强制完成金额");
    colTitle.add("未完成金额");
    colTitle.add("赠品数量");
    
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
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    Integer unitId = getParaToInt("unit.id");
    Integer staffId = getParaToInt("staff.id");
    Integer productId = getParaToInt("product.id");
    Map<String, Object> map = new HashMap();
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("productId", productId);
    map.put("unitId", unitId);
    map.put("staffId", staffId);
    map.put("storageId", getParaToInt("storage.id"));
    map.put("status", getPara("status"));
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
      rowData.add(trimNull(model.get("updateTime")));
      rowData.add(trimNull(model.get("deliveryDate")));
      rowData.add(trimNull(model.get("baseAmount")));
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
      rowData.add(trimNull(model.get("giftAmount")));
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
