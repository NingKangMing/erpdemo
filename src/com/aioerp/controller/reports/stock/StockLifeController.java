package com.aioerp.controller.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.db.reports.stock.StockLife;
import com.aioerp.model.base.Avgprice;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.stock.Stock;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockLifeController
  extends BaseController
{
  public static String listID = "stockLifeList";
  
  public void in()
  {
    Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
    if (paraMap.size() > 0) {
      setAttr("paraMap", paraMap);
    }
    render("searchDialog.html");
  }
  
  public void index()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "sck.id");
    String orderDirection = getPara("orderDirection", "asc");
    int storageId = (getParaToInt("storageId", Integer.valueOf(0)).intValue() != 0 ? getParaToInt("storageId", Integer.valueOf(0)) : getParaToInt("storage.id", Integer.valueOf(0))).intValue();
    String storageName = getPara("storage.fullName", "全部仓库");
    Integer alertDay = getParaToInt("alertDay", Integer.valueOf(0));
    
    String searchBaseAttr = getPara("searchBaseAttr", "");
    String searchBaseVal = getPara("searchBaseVal", "");
    
    Map<String, Object> map = StockLife.dao.getListByParam(configName, storageId, alertDay, listID, pageNum, numPerPage, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    
    List<Model> list = (List)map.get("pageList");
    BigDecimal amountTotal = new BigDecimal(0);
    BigDecimal moneyTotal = new BigDecimal(0);
    for (Model record : list)
    {
      Avgprice zjInfo = (Avgprice)record.get("zj");
      amountTotal = BigDecimalUtils.add(amountTotal, zjInfo.getBigDecimal("amount"));
      moneyTotal = BigDecimalUtils.add(moneyTotal, zjInfo.getBigDecimal("costMoneys"));
    }
    columnConfig("cc523");
    
    setAttr("amountTotal", amountTotal);
    setAttr("moneyTotal", moneyTotal);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    setAttr("storageId", Integer.valueOf(storageId));
    setAttr("storageName", storageName);
    setAttr("alertDay", alertDay);
    render("page.html");
  }
  
  public void list()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "sck.id");
    String orderDirection = getPara("orderDirection", "asc");
    int storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    Integer alertDay = getParaToInt("alertDay", Integer.valueOf(0));
    String searchBaseAttr = getPara("searchBaseAttr", "");
    String searchBaseVal = getPara("searchBaseVal", "");
    Map<String, Object> map = StockLife.dao.getListByParam(configName, storageId, alertDay, listID, pageNum, numPerPage, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    
    List<Model> list = (List)map.get("pageList");
    BigDecimal amountTotal = new BigDecimal(0);
    BigDecimal moneyTotal = new BigDecimal(0);
    for (Model record : list)
    {
      Avgprice zjInfo = (Avgprice)record.get("zj");
      amountTotal = BigDecimalUtils.add(amountTotal, zjInfo.getBigDecimal("amount"));
      moneyTotal = BigDecimalUtils.add(moneyTotal, zjInfo.getBigDecimal("costMoneys"));
    }
    columnConfig("cc523");
    
    setAttr("amountTotal", amountTotal);
    setAttr("moneyTotal", moneyTotal);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    setAttr("storageId", Integer.valueOf(storageId));
    setAttr("alertDay", alertDay);
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "商品保质期");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("库存数量");
    colTitle.add("成本单价");
    colTitle.add("成本金额");
    colTitle.add("保质期");
    colTitle.add("生产日期");
    colTitle.add("到期日期");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "sck.id");
    String orderDirection = getPara("orderDirection", "asc");
    int storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    Integer alertDay = getParaToInt("alertDay", Integer.valueOf(0));
    
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    String searchBaseAttr = getPara("searchBaseAttr", "");
    String searchBaseVal = getPara("searchBaseVal", "");
    Map<String, Object> map = StockLife.dao.getListByParam(configName, storageId, alertDay, listID, pageNum, numPerPage, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    
    List<Model> list = (List)map.get("pageList");
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    int i = 1;
    for (Model record : list)
    {
      Product pro = (Product)record.get("pro");
      Storage sge = (Storage)record.get("sge");
      Avgprice zj = (Avgprice)record.get("zj");
      Stock sck = (Stock)record.get("sck");
      
      rowData.add(trimNull(i));
      rowData.add(trimNull(pro.get("code")));
      rowData.add(trimNull(pro.get("fullName")));
      rowData.add(trimNull(pro.get("smallName")));
      rowData.add(trimNull(pro.get("spell")));
      rowData.add(trimNull(pro.get("standard")));
      rowData.add(trimNull(pro.get("model")));
      rowData.add(trimNull(pro.get("field")));
      
      rowData.add(trimNull(sge.get("code")));
      rowData.add(trimNull(sge.get("fullName")));
      rowData.add(trimNull(zj.get("amount")));
      if (!hasCostPermitted)
      {
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        rowData.add(trimNull(zj.get("avgPrice")));
        rowData.add(trimNull(zj.get("costMoneys")));
      }
      rowData.add(trimNull(record.get("expire")));
      rowData.add(trimNull(sck.get("produceDate")));
      rowData.add(trimNull(sck.get("produceEndDate")));
      
      i++;
    }
    String storageName = "全部仓库";
    if (storageId != 0) {
      storageName = Storage.dao.findById(configName, Integer.valueOf(storageId)).getStr("fullName");
    }
    headData.add("仓库:" + storageName);
    headData.add("提前报警天数:" + alertDay);
    
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
