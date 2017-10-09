package com.aioerp.controller.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.db.reports.stock.StockStatus;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.stock.StockRecords;
import com.aioerp.model.sys.BillType;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StockStatusController
  extends BaseController
{
  public static String listID = "stockStatusList";
  public static String proDistributionListID = "proDistributionDialog";
  
  public void index()
    throws Exception
  {
    String configName = loginConfigName();
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    String sgePids = getPara("storage.pids", "{0}");
    String storageFullName = getPara("storage.fullName", "全部仓库");
    if (storageId.intValue() == 0)
    {
      Record record = ComFunController.isOnlyOne(configName, "b_storage", null);
      if (record != null)
      {
        storageId = record.getInt("id");
        storageFullName = record.getStr("fullName");
      }
    }
    String filterVal = getPara("filterVal", "all");
    
    setAttr("storgeName", storageFullName);
    setAttr("supId", Integer.valueOf(supId));
    
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    setAttr("node", Integer.valueOf(node));
    
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    
    Map<String, Object> map = StockStatus.dao.getPageBySupId(configName, filterVal, pmap, pageNum, numPerPage, supId, sgePids, listID, orderField, orderDirection, node);
    

    String helpAmunitPattern = getPara("helpAmunitPattern", "blendUnit");
    
    List<Model> list = (List)map.get("pageList");
    for (Model record : list) {
      if (BigDecimalUtils.compare(record.getBigDecimal("amounts"), BigDecimal.ZERO) != 0)
      {
        Product pro = (Product)record.get("pro");
        String helpAmout = DwzUtils.helpAmount(helpAmunitPattern, record.getBigDecimal("amounts"), pro.getStr("calculateUnit1"), pro.getStr("calculateUnit2"), pro.getStr("calculateUnit3"), pro.getBigDecimal("unitRelation2"), pro.getBigDecimal("unitRelation3"));
        record.put("helpAmout", helpAmout);
      }
    }
    columnConfig("cc520");
    
    setAttr("helpAmunitPattern", helpAmunitPattern);
    setAttr("filterVal", filterVal);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    


    Integer pSupId = Integer.valueOf(0);
    if (supId > 0) {
      pSupId = Integer.valueOf(Product.dao.getPsupIdBySupId(configName, supId));
    }
    setAttr("storageId", storageId);
    setAttr("storagePids", sgePids);
    setAttr("pSupId", pSupId);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(node));
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    

    render("page.html");
  }
  
  public void tree()
  {
    String configName = loginConfigName();
    List<Model> sorts = null;
    
    sorts = StockStatus.dao.getSortsByStorageId(configName, 0);
    Iterator<Model> iter = sorts.iterator();
    
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model model = (Model)((Model)iter.next()).get("pro");
      HashMap<String, Object> node = new HashMap();
      node.put("id", model.get("id"));
      node.put("pId", model.get("supId"));
      node.put("name", model.get("fullName"));
      node.put("url", "stock/stockStatus/list/" + model.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(-1));
    node.put("name", "商品信息");
    node.put("url", "stock/stockStatus/list/0");
    nodeList.add(node);
    
    renderJson(nodeList);
  }
  
  public void list()
    throws Exception
  {
    String configName = loginConfigName();
    
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    
    int storageId = getParaToInt("storage.id").intValue();
    String storagePids = getPara("storage.pids");
    String storgeName = getPara("storage.fullName");
    setAttr("storgeName", storgeName);
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    
    String filterVal = getPara("filterVal", "all");
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    
    Map<String, Object> map = new HashMap();
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      map = StockStatus.dao.getSupPageBySupIdAndStorageId(configName, filterVal, pmap, pageNum, numPerPage, supId, storagePids, upObjectId, listID, orderField, orderDirection);
      node = 0;
    }
    else
    {
      map = StockStatus.dao.getPageBySupId(configName, filterVal, pmap, pageNum, numPerPage, supId, storagePids, listID, orderField, orderDirection, node);
    }
    Integer pSupId = Integer.valueOf(0);
    if (supId > 0) {
      pSupId = Integer.valueOf(Product.dao.getPsupIdBySupId(configName, supId));
    }
    String helpAmunitPattern = getPara("helpAmunitPattern", "blendUnit");
    
    List<Model> list = (List)map.get("pageList");
    for (Model record : list) {
      if (BigDecimalUtils.compare(record.getBigDecimal("amounts"), BigDecimal.ZERO) != 0)
      {
        Product pro = (Product)record.get("pro");
        String helpAmout = DwzUtils.helpAmount(helpAmunitPattern, record.getBigDecimal("amounts"), pro.getStr("calculateUnit1"), pro.getStr("calculateUnit2"), pro.getStr("calculateUnit3"), pro.getBigDecimal("unitRelation2"), pro.getBigDecimal("unitRelation3"));
        record.put("helpAmout", helpAmout);
      }
    }
    columnConfig("cc520");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    setAttr("helpAmunitPattern", helpAmunitPattern);
    setAttr("filterVal", filterVal);
    
    setAttr("storageId", Integer.valueOf(storageId));
    setAttr("storagePids", storagePids);
    setAttr("pSupId", pSupId);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(node));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    
    render("list.html");
  }
  
  public void line()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0))).intValue();
    String storagePids = getPara("storage.pids");
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String filterVal = getPara("filterVal", "all");
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    
    Map<String, Object> map = StockStatus.dao.getPageLine(configName, filterVal, pmap, pageNum, numPerPage, supId, storagePids, listID, orderField, orderDirection);
    
    String helpAmunitPattern = getPara("helpAmunitPattern", "blendUnit");
    
    List<Model> list = (List)map.get("pageList");
    for (Model record : list) {
      if (BigDecimalUtils.compare(record.getBigDecimal("amounts"), BigDecimal.ZERO) != 0)
      {
        Product pro = (Product)record.get("pro");
        String helpAmout = DwzUtils.helpAmount(helpAmunitPattern, record.getBigDecimal("amounts"), pro.getStr("calculateUnit1"), pro.getStr("calculateUnit2"), pro.getStr("calculateUnit3"), pro.getBigDecimal("unitRelation2"), pro.getBigDecimal("unitRelation3"));
        record.put("helpAmout", helpAmout);
      }
    }
    Integer pSupId = Integer.valueOf(0);
    if (supId > 0) {
      pSupId = Integer.valueOf(Product.dao.getPsupIdBySupId(configName, supId));
    }
    columnConfig("cc520");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    setAttr("helpAmunitPattern", helpAmunitPattern);
    setAttr("filterVal", filterVal);
    
    setAttr("storagePids", storagePids);
    setAttr("pSupId", pSupId);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(1));
    
    render("list.html");
  }
  
  public void proDistribution()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    
    Integer pSupId = Integer.valueOf(0);
    if (supId > 0) {
      pSupId = Integer.valueOf(Storage.getPsupId(configName, supId));
    }
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    String orderField = getPara("orderField", "sge.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer pid = getParaToInt("product.id", getParaToInt("selectedObjectId", Integer.valueOf(0)));
    
    Record proRecord = new Record();
    String proPids = "";
    if (pid.intValue() != 0)
    {
      proRecord = StockStatus.dao.productReport(configName, pid);
      proPids = proRecord.getStr("pids");
    }
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    
    Map<String, Object> map = new HashMap();
    if (!"".equals(proPids)) {
      if (upObjectId > 0) {
        map = StockStatus.dao.getSupPageBySupIdAndProId(configName, proPids, pageNum, numPerPage, supId, upObjectId, proDistributionListID, orderField, orderDirection);
      } else {
        map = StockStatus.dao.getProductDistribute(configName, proPids, pageNum, numPerPage, supId, proDistributionListID, orderField, orderDirection);
      }
    }
    List<Model> list = (List)map.get("pageList");
    BigDecimal amountTotal = new BigDecimal(0);
    BigDecimal stockMoneyTotal = new BigDecimal(0);
    for (Model record : list)
    {
      amountTotal = BigDecimalUtils.add(amountTotal, record.getBigDecimal("amounts"));
      stockMoneyTotal = BigDecimalUtils.add(stockMoneyTotal, record.getBigDecimal("moneys"));
    }
    setAttr("amoutTotal", amountTotal);
    setAttr("stockMoneyTotal", stockMoneyTotal);
    

    setAttr("proPids", proPids);
    setAttr("pSupId", pSupId);
    setAttr("supId", Integer.valueOf(supId));
    
    setAttr("objectId", Integer.valueOf(supId));
    
    setAttr("productId", pid);
    setAttr("proRecord", proRecord);
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("proDistribution.html");
  }
  
  public void proBatchNum()
  {
    String configName = loginConfigName();
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    String storagePids = getPara("storage.pids", "{0}");
    Integer productId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    
    List<Model> stockList = Stock.dao.getStockGroupAttr(configName, storagePids, productId);
    BigDecimal amountTotal = new BigDecimal(0);
    BigDecimal moneyTotal = new BigDecimal(0);
    for (Model record : stockList)
    {
      BigDecimal money = BigDecimalUtils.mul(record.getBigDecimal("amount"), record.getBigDecimal("costPrice"));
      record.put("money", money);
      amountTotal = BigDecimalUtils.add(amountTotal, record.getBigDecimal("amount"));
      moneyTotal = BigDecimalUtils.add(moneyTotal, money);
    }
    setAttr("amountTotal", amountTotal);
    setAttr("moneyTotal", moneyTotal);
    if (productId.intValue() != 0) {
      setAttr("productFullName", Product.dao.findById(configName, productId).get("fullName"));
    }
    if (storageId.intValue() == 0) {
      setAttr("storageFullName", "全部仓库");
    } else {
      setAttr("storageFullName", Storage.dao.findById(configName, storageId).get("fullName"));
    }
    setAttr("stockList", stockList);
    render("proBatchNum.html");
  }
  
  public void proDetailBill()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String listId = "proDetailBill";
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "sckRos.createTime,sckRos.id");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer isRcw = getParaToInt("isRcw", Integer.valueOf(-1));
    String startTime = getPara("startTime", "");
    String endTime = getPara("endTime", "");
    Integer storageId = getParaToInt("storage.id", getParaToInt("storageId", Integer.valueOf(0)));
    Integer productId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    
    String storageName = "全部仓库";
    String storagePids = "{0}";
    if (storageId.intValue() > 0)
    {
      Storage storage = (Storage)Storage.dao.findById(configName, storageId);
      storageName = storage.getStr("fullName");
      storagePids = storage.getStr("pids");
    }
    Product product = (Product)Product.dao.findById(configName, productId);
    if (product == null) {
      return;
    }
    String productName = product.getStr("fullName");
    String productPids = product.getStr("pids");
    
    BigDecimal beforeAmount = new BigDecimal(0);
    BigDecimal beforeMoney = new BigDecimal(0);
    
    int recordCount = (pageNum - 1) * numPerPage;
    StockRecords sckRecords = StockRecords.dao.getBeforeFirst(configName, basePrivs(STORAGE_PRIVS), basePrivs(PRODUCT_PRIVS), startTime, endTime, recordCount, storagePids, productPids);
    if (sckRecords != null)
    {
      beforeAmount = sckRecords.getBigDecimal("beforeAmout");
      beforeMoney = sckRecords.getBigDecimal("beforeMoney");
    }
    setAttr("beforeAmount", beforeAmount);
    setAttr("beforeMoney", beforeMoney);
    
    Map<String, Object> map = StockRecords.dao.getPageByProIdAndSgeId(configName, basePrivs(STORAGE_PRIVS), productPids, storagePids, startTime, endTime, isRcw, listId, pageNum, numPerPage, orderField, orderDirection);
    List<Model> rList = (List)map.get("pageList");
    BigDecimal sckAmount = beforeAmount;
    BigDecimal sckMoney = beforeMoney;
    for (Model model : rList)
    {
      StockRecords sckRecode = (StockRecords)model.get("sckRos");
      BigDecimal inAmount = sckRecode.getBigDecimal("inAmount");
      BigDecimal outAmount = sckRecode.getBigDecimal("outAmount");
      BigDecimal price = sckRecode.getBigDecimal("price");
      sckAmount = BigDecimalUtils.add(sckAmount, BigDecimalUtils.sub(inAmount, outAmount));
      sckMoney = BigDecimalUtils.add(sckMoney, BigDecimalUtils.mul(BigDecimalUtils.sub(inAmount, outAmount), price));
      model.put("sckAmount", sckAmount);
      model.put("sckMoney", sckMoney);
    }
    columnConfig("cc521");
    
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    setAttr("isRcw", isRcw);
    setAttr("storageId", storageId);
    setAttr("productId", productId);
    setAttr("storageName", storageName);
    setAttr("productName", productName);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("proDetailBill.html");
  }
  
  public void proDetailBillList()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String listId = "proDetailBill";
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "sckRos.recodeTime,sckRos.id");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer isRcw = getParaToInt("isRcw", Integer.valueOf(-1));
    String startTime = getPara("startTime", "");
    String endTime = getPara("endTime", "");
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    Integer productId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    
    String storagePids = "{0}";
    String storageName = "全部仓库";
    if (storageId.intValue() > 0)
    {
      Storage storage = (Storage)Storage.dao.findById(configName, storageId);
      storagePids = storage.getStr("pids");
      storageName = storage.getStr("fullName");
    }
    Product product = (Product)Product.dao.findById(configName, productId);
    String productPids = product.getStr("pids");
    String productName = product.getStr("fullName");
    
    Map<String, Object> map = StockRecords.dao.getPageByProIdAndSgeId(configName, basePrivs(STORAGE_PRIVS), productPids, storagePids, startTime, endTime, isRcw, listId, pageNum, numPerPage, orderField, orderDirection);
    List<Model> rList = (List)map.get("pageList");
    
    BigDecimal sckAmount = new BigDecimal(0);
    BigDecimal sckMoney = new BigDecimal(0);
    
    pageNum = ((Integer)map.get("pageNum")).intValue();
    numPerPage = ((Integer)map.get("numPerPage")).intValue();
    
    int recordCount = (pageNum - 1) * numPerPage;
    StockRecords sckRecords = StockRecords.dao.getBeforeFirst(configName, basePrivs(STORAGE_PRIVS), basePrivs(PRODUCT_PRIVS), startTime, endTime, recordCount, storagePids, productPids);
    if (sckRecords != null)
    {
      sckAmount = sckRecords.getBigDecimal("beforeAmout");
      sckMoney = sckRecords.getBigDecimal("beforeMoney");
    }
    setAttr("beforeAmount", sckAmount);
    setAttr("beforeMoney", sckMoney);
    for (Model model : rList)
    {
      StockRecords sckRecode = (StockRecords)model.get("sckRos");
      BigDecimal inAmount = sckRecode.getBigDecimal("inAmount");
      BigDecimal outAmount = sckRecode.getBigDecimal("outAmount");
      BigDecimal price = sckRecode.getBigDecimal("price");
      sckAmount = BigDecimalUtils.add(sckAmount, BigDecimalUtils.sub(inAmount, outAmount));
      sckMoney = BigDecimalUtils.add(sckMoney, BigDecimalUtils.mul(BigDecimalUtils.sub(inAmount, outAmount), price));
      model.put("sckAmount", sckAmount);
      model.put("sckMoney", sckMoney);
    }
    columnConfig("cc521");
    



    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    setAttr("isRcw", isRcw);
    setAttr("storageId", storageId);
    setAttr("productId", productId);
    setAttr("storageName", storageName);
    setAttr("productName", productName);
    

    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("proDetailBill.html");
  }
  
  public void ProSearch()
  {
    String configName = loginConfigName();
    if (isPost())
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
    else
    {
      Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
      String storagePids = getPara("storage.pids");
      if (storageId.intValue() > 0)
      {
        String storageName = Storage.dao.findById(configName, storageId, "fullName").getStr("fullName");
        setAttr("storageName", storageName);
      }
      setAttr("storagePids", storagePids);
      setAttr("storageId", storageId);
      render("proSearch.html");
    }
  }
  
  public void proStockLinePage()
    throws Exception
  {
    String configName = loginConfigName();
    String listId = "proStockLinePage";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "sck.id");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer storageId = getParaToInt("storageId");
    String storagePids = getPara("storagePids");
    String storageName = getPara("storageName", "全部仓库");
    String searchPar = getPara("searchPar", "all");
    String searchVal = getPara("searchVal");
    String pattern = getPara("batchPattern", "merge");
    
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    
    Map<String, Object> map = StockStatus.dao.getPageByParam(configName, pmap, listId, Integer.valueOf(pageNum), Integer.valueOf(numPerPage), orderField, orderDirection, storagePids, searchPar, searchVal, pattern);
    
    String helpAmunitPattern = getPara("helpAmunitPattern", "blendUnit");
    
    List<Model> list = (List)map.get("pageList");
    for (Model record : list) {
      if (BigDecimalUtils.compare(record.getBigDecimal("amounts"), BigDecimal.ZERO) != 0)
      {
        Product pro = (Product)record.get("pro");
        String helpAmout = DwzUtils.helpAmount(helpAmunitPattern, record.getBigDecimal("amounts"), pro.getStr("calculateUnit1"), pro.getStr("calculateUnit2"), pro.getStr("calculateUnit3"), pro.getBigDecimal("unitRelation2"), pro.getBigDecimal("unitRelation3"));
        record.put("helpAmout", helpAmout);
      }
    }
    columnConfig("cc524");
    
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("storageId", storageId);
    setAttr("storagePids", storagePids);
    setAttr("storageName", storageName);
    setAttr("searchPar", searchPar);
    setAttr("searchVal", searchVal);
    setAttr("batchPattern", pattern);
    setAttr("pageMap", map);
    
    render("proStockLine.html");
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "库存状况");
    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("基本条码");
    colTitle.add("基本单位");
    colTitle.add("辅助单位");
    colTitle.add("批号");
    colTitle.add("生产日期");
    colTitle.add("库存数量");
    colTitle.add("辅助数量");
    colTitle.add("成本单价");
    colTitle.add("库存金额");
    

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
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    
    int supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0))).intValue();
    String storagePids = getPara("storage.pids");
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    

    String filterVal = getPara("filterVal", "all");
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    
    Map<String, Object> pageMap = StockStatus.dao.getPageBySupId(configName, filterVal, pmap, pageNum, numPerPage, supId, storagePids, listID, orderField, orderDirection, node);
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    

    String helpAmunitPattern = getPara("helpAmunitPattern", "blendUnit");
    
    List<Model> list = (List)pageMap.get("pageList");
    int i = 1;
    for (Model record : list)
    {
      rowData.add(trimNull(i));
      Product pro = (Product)record.get("pro");
      rowData.add(trimNull(pro.get("code")));
      rowData.add(trimNull(pro.get("fullName")));
      rowData.add(trimNull(pro.get("smallName")));
      rowData.add(trimNull(pro.get("spell")));
      rowData.add(trimNull(pro.get("standard")));
      rowData.add(trimNull(pro.get("model")));
      rowData.add(trimNull(pro.get("field")));
      rowData.add(trimNull(pro.get("barCode1")));
      rowData.add(trimNull(pro.get("calculateUnit1")));
      rowData.add(trimNull(pro.get("assistUnit")));
      Stock sck = (Stock)record.get("sck");
      rowData.add(trimNull(sck.get("batchNum")));
      rowData.add(trimNull(sck.get("produceDate")));
      rowData.add(trimNull(record.get("amounts")));
      if (BigDecimalUtils.compare(record.getBigDecimal("amounts"), BigDecimal.ZERO) != 0)
      {
        String helpAmout = DwzUtils.helpAmount(helpAmunitPattern, record.getBigDecimal("amounts"), pro.getStr("calculateUnit1"), pro.getStr("calculateUnit2"), pro.getStr("calculateUnit3"), pro.getBigDecimal("unitRelation2"), pro.getBigDecimal("unitRelation3"));
        rowData.add(helpAmout);
      }
      else
      {
        rowData.add("");
      }
      if (!hasCostPermitted)
      {
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        rowData.add(trimNull(record.get("avgPrice")));
        rowData.add(trimNull(record.get("moneys")));
      }
      i++;
    }
    if ((list == null) || (list.size() == 0)) {
      for (int j = 0; j < colTitle.size(); j++) {
        rowData.add("");
      }
    }
    String storageName = "全部仓库";
    if (!"{0}".equals(storagePids)) {
      storageName = Db.use(configName).findFirst("SELECT * FROM b_storage WHERE pids = '" + storagePids + "'").getStr("fullName");
    }
    headData.add("仓库:" + storageName);
    
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public void printStockLine()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "库存状况列表");
    
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("基本条码");
    colTitle.add("基本单位");
    colTitle.add("辅助单位");
    colTitle.add("批号");
    colTitle.add("生产日期");
    colTitle.add("库存数量");
    colTitle.add("辅助数量");
    colTitle.add("成本单价");
    colTitle.add("库存金额");
    

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
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    
    String listId = "proStockLinePage";
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "sck.id");
    String orderDirection = getPara("orderDirection", "asc");
    

    String storagePids = getPara("storagePids");
    
    String searchPar = getPara("searchPar", "all");
    String searchVal = getPara("searchVal");
    String pattern = getPara("batchPattern", "merge");
    

    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    
    Map<String, Object> pageMap = StockStatus.dao.getPageByParam(configName, pmap, listId, Integer.valueOf(pageNum), Integer.valueOf(numPerPage), orderField, orderDirection, storagePids, searchPar, searchVal, pattern);
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    

    String helpAmunitPattern = getPara("helpAmunitPattern", "blendUnit");
    
    List<Model> list = (List)pageMap.get("pageList");
    int i = 1;
    for (Model record : list)
    {
      rowData.add(trimNull(i));
      Product pro = (Product)record.get("pro");
      rowData.add(trimNull(pro.get("code")));
      rowData.add(trimNull(pro.get("fullName")));
      rowData.add(trimNull(pro.get("smallName")));
      rowData.add(trimNull(pro.get("spell")));
      rowData.add(trimNull(pro.get("standard")));
      rowData.add(trimNull(pro.get("model")));
      rowData.add(trimNull(pro.get("field")));
      rowData.add(trimNull(pro.get("barCode1")));
      rowData.add(trimNull(pro.get("calculateUnit1")));
      rowData.add(trimNull(pro.get("assistUnit")));
      Stock sck = (Stock)record.get("sck");
      rowData.add(trimNull(sck.get("batchNum")));
      rowData.add(trimNull(sck.get("produceDate")));
      rowData.add(trimNull(record.get("amounts")));
      if (BigDecimalUtils.compare(record.getBigDecimal("amounts"), BigDecimal.ZERO) != 0)
      {
        String helpAmout = DwzUtils.helpAmount(helpAmunitPattern, record.getBigDecimal("amounts"), pro.getStr("calculateUnit1"), pro.getStr("calculateUnit2"), pro.getStr("calculateUnit3"), pro.getBigDecimal("unitRelation2"), pro.getBigDecimal("unitRelation3"));
        rowData.add(helpAmout);
      }
      else
      {
        rowData.add("");
      }
      if (!hasCostPermitted)
      {
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        rowData.add(trimNull(record.get("avgPrice")));
        rowData.add(trimNull(record.get("moneys")));
      }
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public void printDetail()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "商品明细账本 ");
    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("单据类型");
    colTitle.add("单据字头");
    colTitle.add("单据编号");
    colTitle.add("录单时间");
    colTitle.add("摘要");
    colTitle.add("入库数量");
    colTitle.add("出库数量");
    colTitle.add("库存余量");
    colTitle.add("成本单价");
    colTitle.add("当前余额");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    

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
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "sckRos.recodeTime");
    String orderDirection = getPara("orderDirection", "asc");
    String startTime = getPara("startTime", "");
    String endTime = getPara("endTime", "");
    Integer isRcw = getParaToInt("isRcw", Integer.valueOf(-1));
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    Integer productId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    
    String storagePids = "{0}";
    if (storageId.intValue() > 0)
    {
      Storage storage = (Storage)Storage.dao.findById(configName, storageId);
      storagePids = storage.getStr("pids");
    }
    Product product = (Product)Product.dao.findById(configName, productId);
    String productPids = product.getStr("pids");
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    
    Map<String, Object> map = StockRecords.dao.getPageByProIdAndSgeId(configName, basePrivs(STORAGE_PRIVS), productPids, storagePids, startTime, endTime, isRcw, "", pageNum, numPerPage, orderField, orderDirection);
    List<Model> rList = (List)map.get("pageList");
    
    BigDecimal sckAmount = new BigDecimal(0);
    BigDecimal sckMoney = new BigDecimal(0);
    










    pageNum = ((Integer)map.get("pageNum")).intValue();
    numPerPage = ((Integer)map.get("numPerPage")).intValue();
    int recordCount = (pageNum - 1) * numPerPage;
    StockRecords sckRecords = StockRecords.dao.getBeforeFirst(configName, basePrivs(STORAGE_PRIVS), basePrivs(PRODUCT_PRIVS), startTime, endTime, recordCount, storagePids, productPids);
    if (sckRecords != null)
    {
      sckAmount = sckRecords.getBigDecimal("beforeAmout");
      sckMoney = sckRecords.getBigDecimal("beforeMoney");
    }
    headData.add("此前余量:" + sckAmount.doubleValue());
    headData.add("此前余额:" + sckMoney.doubleValue());
    
    int i = 1;
    for (Model model : rList)
    {
      StockRecords sckRos = (StockRecords)model.get("sckRos");
      BillType bt = (BillType)model.get("bt");
      Product pro = (Product)model.get("pro");
      BigDecimal inAmount = sckRos.getBigDecimal("inAmount");
      BigDecimal outAmount = sckRos.getBigDecimal("outAmount");
      BigDecimal price = sckRos.getBigDecimal("price");
      sckAmount = BigDecimalUtils.add(sckAmount, BigDecimalUtils.sub(inAmount, outAmount));
      sckMoney = BigDecimalUtils.add(sckMoney, BigDecimalUtils.mul(BigDecimalUtils.sub(inAmount, outAmount), price));
      

      rowData.add(trimNull(i));
      rowData.add(trimNull(bt.get("name")));
      rowData.add(trimNull(bt.get("prefix")));
      rowData.add(trimNull(sckRos.get("billCode")));
      rowData.add(trimNull(sckRos.get("recodeTime")));
      rowData.add(trimNull(sckRos.get("billAbstract")));
      rowData.add(trimNull(inAmount));
      rowData.add(trimNull(outAmount));
      rowData.add(trimNull(sckAmount));
      if (!hasCostPermitted)
      {
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        rowData.add(trimNull(price));
        rowData.add(trimNull(sckMoney));
      }
      rowData.add(trimNull(pro.get("code")));
      rowData.add(trimNull(pro.get("fullName")));
      rowData.add(trimNull(pro.get("smallName")));
      rowData.add(trimNull(pro.get("spell")));
      rowData.add(trimNull(pro.get("standard")));
      rowData.add(trimNull(pro.get("model")));
      rowData.add(trimNull(pro.get("field")));
      
      i++;
    }
    headData.add("查询时间:" + startTime + " 至 " + endTime);
    String storageName = "全部仓库";
    if (!"{0}".equals(storagePids)) {
      storageName = Db.use(configName).findFirst("SELECT * FROM b_storage WHERE pids = '" + storagePids + "'").getStr("fullName");
    }
    headData.add("仓库:" + storageName);
    

    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
