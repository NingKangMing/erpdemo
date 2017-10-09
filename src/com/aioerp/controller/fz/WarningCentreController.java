package com.aioerp.controller.fz;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.db.reports.stock.StockBound;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.bought.BoughtBill;
import com.aioerp.model.reports.finance.arap.ArapOverPayOrGetMoneyReports;
import com.aioerp.model.sell.sellbook.SellbookBill;
import com.aioerp.model.stock.Stock;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarningCentreController
  extends BaseController
{
  public void index()
    throws SQLException, Exception
  {
    String listId = "warningList";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    String storagePids = getPara("storage.pids", "{0}");
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    
    Map<String, Object> map = StockBound.dao.getMaxPage(loginConfigName(), basePrivs(PRODUCT_PRIVS), listId, storageId, storagePids, pageNum, numPerPage, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    
    setAttr("storageId", storageId);
    setAttr("storagePids", storagePids);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("searchBaseAttr", searchBaseAttr);
    setAttr("searchBaseVal", searchBaseVal);
    render("page.html");
  }
  
  public void tree()
  {
    List<HashMap<String, Object>> nodeList = new ArrayList();
    
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "库存商品上限");
    node.put("url", "fz/warningCentre/productUp/");
    nodeList.add(node);
    node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "库存商品下限");
    node.put("url", "fz/warningCentre/productDown/");
    nodeList.add(node);
    node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "库存商品保质期");
    node.put("url", "fz/warningCentre/productExpire");
    nodeList.add(node);
    int version = AioConstants.VERSION;
    if (version > 1)
    {
      node = new HashMap();
      node.put("id", Integer.valueOf(0));
      node.put("pId", Integer.valueOf(0));
      node.put("name", "超期应收款");
      node.put("url", "fz/warningCentre/exceedPayOrGet/get");
      nodeList.add(node);
      node = new HashMap();
      node.put("id", Integer.valueOf(0));
      node.put("pId", Integer.valueOf(0));
      node.put("name", "超期应付款");
      node.put("url", "fz/warningCentre/exceedPayOrGet/pay");
      nodeList.add(node);
      node = new HashMap();
      node.put("id", Integer.valueOf(0));
      node.put("pId", Integer.valueOf(0));
      node.put("name", "应收款上限");
      node.put("url", "fz/warningCentre/shouldGetOrPay/get");
      nodeList.add(node);
      node = new HashMap();
      node.put("id", Integer.valueOf(0));
      node.put("pId", Integer.valueOf(0));
      node.put("name", "应付款上限");
      node.put("url", "fz/warningCentre/shouldGetOrPay/pay");
      nodeList.add(node);
    }
    node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "到期进货订单");
    node.put("url", "fz/warningCentre/orderExpire/bought");
    nodeList.add(node);
    node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "到期销售订单");
    node.put("url", "fz/warningCentre/orderExpire/sell");
    nodeList.add(node);
    node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "负库存报警");
    node.put("url", "fz/warningCentre/minusStock");
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public void productUp()
    throws SQLException, Exception
  {
    String listId = "warningList";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    String storagePids = getPara("storage.pids", "{0}");
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    
    Map<String, Object> map = StockBound.dao.getMaxPage(loginConfigName(), basePrivs(PRODUCT_PRIVS), listId, storageId, storagePids, pageNum, numPerPage, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    
    setAttr("storageId", storageId);
    setAttr("storagePids", storagePids);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("productUp.html");
  }
  
  public void productUpPrint()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "库存商品上限报警");
    
    List<String> headData = new ArrayList();
    Integer id = getParaToInt("storage.id", Integer.valueOf(0));
    Model storage = Storage.dao.findById(configName, id);
    String storageName = "";
    if (storage != null) {
      storageName = storage.getStr("fullName");
    }
    headData.add("仓库:" + storageName);
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("库存数量");
    colTitle.add("库存上线");
    colTitle.add("调整数量");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    String storagePids = getPara("storage.pids", "{0}");
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    
    Map<String, Object> pageMap = StockBound.dao.getMaxPage(configName, basePrivs(PRODUCT_PRIVS), null, storageId, storagePids, 1, totalCount, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model detail = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      Product pro = (Product)detail.get("pro");
      if (pro != null)
      {
        rowData.add(trimNull(pro.getStr("code")));
        rowData.add(trimNull(pro.getStr("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(detail.get("sckAmount")));
      rowData.add(trimNull(detail.get("maxNum")));
      rowData.add(trimNull(detail.get("adjust")));
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
  
  public void productDown()
    throws SQLException, Exception
  {
    String listId = "warningList";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    String storagePids = getPara("storage.pids", "{0}");
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    

    Map<String, Object> map = StockBound.dao.getMinPage(loginConfigName(), basePrivs(PRODUCT_PRIVS), listId, storageId, storagePids, pageNum, numPerPage, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    
    setAttr("storageId", storageId);
    setAttr("storagePids", storagePids);
    

    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("searchBaseAttr", searchBaseAttr);
    setAttr("searchBaseVal", searchBaseVal);
    render("productDown.html");
  }
  
  public void productDownPrint()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "库存商品下限报警");
    
    List<String> headData = new ArrayList();
    Integer id = getParaToInt("storage.id", Integer.valueOf(0));
    Model storage = Storage.dao.findById(configName, id);
    String storageName = "";
    if (storage != null) {
      storageName = storage.getStr("fullName");
    }
    headData.add("仓库:" + storageName);
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("库存数量");
    colTitle.add("库存下限");
    colTitle.add("调整数量");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    String storagePids = getPara("storage.pids", "{0}");
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    
    Map<String, Object> pageMap = StockBound.dao.getMinPage(configName, basePrivs(PRODUCT_PRIVS), "", storageId, storagePids, 1, totalCount, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model detail = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      Product pro = (Product)detail.get("pro");
      if (pro != null)
      {
        rowData.add(trimNull(pro.getStr("code")));
        rowData.add(trimNull(pro.getStr("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(detail.get("sckAmount")));
      rowData.add(trimNull(detail.get("minNum")));
      rowData.add(trimNull(detail.get("adjust")));
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
  
  public void productExpire()
    throws Exception
  {
    String listId = "warningList";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "product.rank");
    String orderDirection = getPara("orderDirection", "asc");
    int aheadDay = getParaToInt("aheadDay", Integer.valueOf(AioConstants.AHEAD_DAY)).intValue();
    AioConstants.AHEAD_DAY = aheadDay;
    Map<String, Object> params = new HashMap();
    params.put("productPrivs", basePrivs(BaseController.PRODUCT_PRIVS));
    params.put("storagePrivs", basePrivs(BaseController.STORAGE_PRIVS));
    Map<String, Object> map = Stock.dao.stockExpiration(loginConfigName(), listId, pageNum, numPerPage, orderField, orderDirection, aheadDay, params);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("aheadDay", Integer.valueOf(aheadDay));
    render("productExpire.html");
  }
  
  public void productExpirePrint()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "库存商品保质期报警");
    
    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("库存数量");
    colTitle.add("成本单价");
    colTitle.add("保质期(天)");
    colTitle.add("生产日期");
    colTitle.add("到期日期");
    
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
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField", "product.rank");
    String configName = loginConfigName();
    String orderDirection = getPara("orderDirection", "asc");
    int aheadDay = getParaToInt("aheadDay", Integer.valueOf(AioConstants.AHEAD_DAY)).intValue();
    AioConstants.AHEAD_DAY = aheadDay;
    Map<String, Object> params = new HashMap();
    params.put("productPrivs", basePrivs(BaseController.PRODUCT_PRIVS));
    params.put("storagePrivs", basePrivs(BaseController.STORAGE_PRIVS));
    Map<String, Object> pageMap = Stock.dao.stockExpiration(configName, null, 1, totalCount, orderField, orderDirection, aheadDay, params);
    List<Model> list = (List)pageMap.get("pageList");
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < list.size(); i++)
    {
      Model detail = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      Product pro = (Product)detail.get("product");
      if (pro != null)
      {
        rowData.add(trimNull(pro.getStr("code")));
        rowData.add(trimNull(pro.getStr("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Storage storage = (Storage)detail.get("storage");
      if (pro != null)
      {
        rowData.add(trimNull(storage.getStr("code")));
        rowData.add(trimNull(storage.getStr("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(detail.get("amount")));
      if (!hasCostPermitted) {
        rowData.add("***");
      } else {
        rowData.add(trimNull(detail.get("costPrice")));
      }
      if (pro != null) {
        rowData.add(trimNull(pro.getStr("validity")));
      } else {
        rowData.add("");
      }
      rowData.add(trimNull(detail.get("produceDate")));
      rowData.add(trimNull(detail.get("dueDate")));
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
  
  public void exceedPayOrGet()
    throws Exception
  {
    String listId = "warningList";
    String modelType = getPara(0, "get");
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "recodeDate");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    int aheadDay = getParaToInt("aheadDay", Integer.valueOf(AioConstants.GET_AHEAD_DAY)).intValue();
    if (modelType.equals("pay"))
    {
      aheadDay = getParaToInt("aheadDay", Integer.valueOf(AioConstants.PAY_AHEAD_DAY)).intValue();
      AioConstants.PAY_AHEAD_DAY = aheadDay;
    }
    else
    {
      AioConstants.GET_AHEAD_DAY = aheadDay;
    }
    Map<String, Object> map = new HashMap();
    map.put("pageNum", Integer.valueOf(pageNum));
    map.put("numPerPage", Integer.valueOf(numPerPage));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("warn", "all");
    map.put("unitId", Integer.valueOf(0));
    map.put("stopDate", DateUtils.format(new Date()));
    map.put("modelType", modelType);
    map.put("listID", listId);
    map.put("aheadDay", Integer.valueOf(aheadDay));
    Map<String, Object> pageMap = ArapOverPayOrGetMoneyReports.dao.arapOverPayOrGetMoney(loginConfigName(), map);
    mapToResponse(map);
    setAttr("modelType", modelType);
    setAttr("pageMap", pageMap);
    render("exceedPayOrGet.html");
  }
  
  public void exceedPrint()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String modelType = getPara("modelType", "get");
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    if (modelType.equals("pay")) {
      data.put("reportName", "超期应付报警");
    } else {
      data.put("reportName", "超期应收报警");
    }
    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("单据编号");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("价税合计");
    if (modelType.equals("pay")) {
      colTitle.add("已付金额");
    } else {
      colTitle.add("已收金额");
    }
    colTitle.add("期限日期");
    colTitle.add("延期日期");
    if (modelType.equals("pay")) {
      colTitle.add("未结金额");
    } else {
      colTitle.add("欠款金额");
    }
    colTitle.add("原因");
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
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField", "recodeDate");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    int aheadDay = getParaToInt("aheadDay", Integer.valueOf(AioConstants.GET_AHEAD_DAY)).intValue();
    if (modelType.equals("pay"))
    {
      aheadDay = getParaToInt("aheadDay", Integer.valueOf(AioConstants.PAY_AHEAD_DAY)).intValue();
      AioConstants.PAY_AHEAD_DAY = aheadDay;
    }
    else
    {
      AioConstants.GET_AHEAD_DAY = aheadDay;
    }
    Map<String, Object> map = new HashMap();
    map.put("pageNum", Integer.valueOf(1));
    map.put("numPerPage", Integer.valueOf(totalCount));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("warn", "all");
    map.put("unitId", Integer.valueOf(0));
    map.put("stopDate", DateUtils.format(new Date()));
    map.put("modelType", modelType);
    map.put("listID", "");
    map.put("aheadDay", Integer.valueOf(aheadDay));
    Map<String, Object> pageMap = ArapOverPayOrGetMoneyReports.dao.arapOverPayOrGetMoney(configName, map);
    List<Record> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Record detail = (Record)list.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.get("code")));
      rowData.add(trimNull(detail.get("unitCode")));
      rowData.add(trimNull(detail.get("unitFullName")));
      rowData.add(trimNull(detail.get("taxMoneys")));
      rowData.add(trimNull(detail.get("hasMoney")));
      rowData.add(trimNull(detail.get("termDate")));
      rowData.add(trimNull(detail.get("delayTermDate")));
      rowData.add(trimNull(detail.get("hasNoMoney")));
      if ((detail.getInt("isWarn") != null) && (detail.getInt("isWarn").intValue() == 1)) {
        rowData.add("取消报警");
      } else if (detail.get("termDate") != detail.get("delayTermDate")) {
        rowData.add("延迟报警");
      } else {
        rowData.add("");
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
  
  public void shouldGetOrPay()
  {
    String listId = "warningList";
    String modelType = getPara(0, "get");
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    Map<String, Object> pars = new HashMap();
    pars.put("modelType", modelType);
    Map<String, Object> pageMap = Unit.dao.shouldGetOrPay(loginConfigName(), listId, pageNum, numPerPage, orderField, orderDirection, pars, basePrivs(UNIT_PRIVS));
    setAttr("pageMap", pageMap);
    setAttr("modelType", modelType);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("shouldGetOrPay.html");
  }
  
  public void shouldPrint()
    throws SQLException, Exception
  {
    String modelType = getPara("modelType", "get");
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    if (modelType.equals("pay")) {
      data.put("reportName", "应付上限报警");
    } else {
      data.put("reportName", "应收上限报警");
    }
    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    if (modelType.equals("pay"))
    {
      colTitle.add("应付金额");
      colTitle.add("应付上限");
    }
    else
    {
      colTitle.add("应收金额");
      colTitle.add("应收上限");
    }
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    Map<String, Object> pars = new HashMap();
    pars.put("modelType", modelType);
    Map<String, Object> pageMap = Unit.dao.shouldGetOrPay(loginConfigName(), "", 1, totalCount, orderField, orderDirection, pars, basePrivs(UNIT_PRIVS));
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model detail = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.get("code")));
      rowData.add(trimNull(detail.get("fullName")));
      if (modelType.equals("pay"))
      {
        rowData.add(trimNull(detail.get("totalPay")));
        rowData.add(trimNull(detail.get("payMoneyLimit")));
      }
      else
      {
        rowData.add(trimNull(detail.get("totalGet")));
        rowData.add(trimNull(detail.get("getMoneyLimit")));
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
  
  public void orderExpire()
    throws Exception
  {
    String configName = loginConfigName();
    String listID = "warningList";
    String modelType = getPara(0, "bought");
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "bill.updateTime");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    int aheadDay = getParaToInt("aheadDay", Integer.valueOf(AioConstants.BOUGHT_AHEAD_DAY)).intValue();
    if ("sell".equals(modelType))
    {
      aheadDay = getParaToInt("aheadDay", Integer.valueOf(AioConstants.SELL_AHEAD_DAY)).intValue();
      AioConstants.SELL_AHEAD_DAY = aheadDay;
    }
    else
    {
      AioConstants.BOUGHT_AHEAD_DAY = aheadDay;
    }
    Map<String, Object> pars = new HashMap();
    pars.put("unitPrivs", basePrivs(UNIT_PRIVS));
    pars.put("staffPrivs", basePrivs(STAFF_PRIVS));
    pars.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    pars.put("aheadDay", Integer.valueOf(aheadDay));
    if ("sell".equals(modelType))
    {
      Map<String, Object> pageMap = SellbookBill.dao.sellExpirePage(configName, listID, pageNum, numPerPage, orderField, orderDirection, pars);
      setAttr("pageMap", pageMap);
    }
    else
    {
      Map<String, Object> pageMap = BoughtBill.dao.boughtExpirePage(configName, listID, pageNum, numPerPage, orderField, orderDirection, pars);
      setAttr("pageMap", pageMap);
    }
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("aheadDay", Integer.valueOf(aheadDay));
    setAttr("modelType", modelType);
    render("orderExpire.html");
  }
  
  public void orderExpirePrint()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String modelType = getPara("modelType", "bought");
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    if ("sell".equals(modelType)) {
      data.put("reportName", "到期销售订单报警");
    } else {
      data.put("reportName", "到期进货订单报警");
    }
    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("制单日期");
    colTitle.add("单据编号");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("交货日期");
    colTitle.add("职员编号");
    colTitle.add("职员全名");
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
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField", "bill.updateTime");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    int aheadDay = getParaToInt("aheadDay", Integer.valueOf(AioConstants.BOUGHT_AHEAD_DAY)).intValue();
    if ("sell".equals(modelType))
    {
      aheadDay = getParaToInt("aheadDay", Integer.valueOf(AioConstants.SELL_AHEAD_DAY)).intValue();
      AioConstants.SELL_AHEAD_DAY = aheadDay;
    }
    else
    {
      AioConstants.BOUGHT_AHEAD_DAY = aheadDay;
    }
    Map<String, Object> pars = new HashMap();
    pars.put("unitPrivs", basePrivs(UNIT_PRIVS));
    pars.put("staffPrivs", basePrivs(STAFF_PRIVS));
    pars.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    pars.put("aheadDay", Integer.valueOf(aheadDay));
    Map<String, Object> pageMap = new HashMap();
    if ("sell".equals(modelType)) {
      pageMap = SellbookBill.dao.sellExpirePage(configName, "", 1, totalCount, orderField, orderDirection, pars);
    } else {
      pageMap = BoughtBill.dao.boughtExpirePage(configName, "", 1, totalCount, orderField, orderDirection, pars);
    }
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model detail = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.get("updateTime")));
      rowData.add(trimNull(detail.get("code")));
      
      Unit unit = (Unit)detail.get("unit");
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
      rowData.add(trimNull(detail.get("deliveryDate")));
      
      Staff staff = (Staff)detail.get("staff");
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
      rowData.add(trimNull(detail.get("remark")));
      rowData.add(trimNull(detail.get("memo")));
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
  
  public void minusStock()
    throws Exception
  {
    String listID = "warningList";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    Integer storageId = getParaToInt("storage.id");
    
    Map<String, Object> params = new HashMap();
    params.put("productPrivs", basePrivs(BaseController.PRODUCT_PRIVS));
    params.put("storagePrivs", basePrivs(BaseController.STORAGE_PRIVS));
    params.put("storageId", storageId);
    
    Map<String, Object> pageMap = Stock.dao.minusStockPage(loginConfigName(), listID, pageNum, numPerPage, orderField, orderDirection, params);
    setAttr("pageMap", pageMap);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("storageId", storageId);
    render("minusStock.html");
  }
  
  public void minusStockPrint()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "负库存报警");
    


    List<String> headData = new ArrayList();
    Integer id = getParaToInt("storage.id", Integer.valueOf(0));
    Model storage = Storage.dao.findById(configName, id);
    String storageName = "";
    if (storage != null) {
      storageName = storage.getStr("fullName");
    }
    headData.add("仓库:" + storageName);
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("基本条码");
    colTitle.add("数量");
    

    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    

    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    Integer storageId = getParaToInt("storage.id");
    
    Map<String, Object> params = new HashMap();
    params.put("productPrivs", basePrivs(BaseController.PRODUCT_PRIVS));
    params.put("storagePrivs", basePrivs(BaseController.STORAGE_PRIVS));
    params.put("storageId", storageId);
    
    Map<String, Object> pageMap = Stock.dao.minusStockPage(configName, "", 1, totalCount, orderField, orderDirection, params);
    
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model detail = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      
      Product product = (Product)detail.get("product");
      if (product != null)
      {
        rowData.add(trimNull(product.get("code")));
        rowData.add(trimNull(product.get("fullName")));
        rowData.add(trimNull(product.get("barCode1")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(detail.get("amount")));
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
