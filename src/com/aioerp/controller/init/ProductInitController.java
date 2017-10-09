package com.aioerp.controller.init;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.db.reports.stock.StockStatus;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.stock.StockInit;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ProductInitController
  extends BaseController
{
  private final String listID = "productInitList";
  private final String proDistributionListID = "proDistribution";
  
  public void index()
    throws Exception
  {
    String configName = loginConfigName();
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    String storagePids = getPara("storage.pids", "{0}");
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    if (StringUtils.isNotEmpty(searchBaseVal)) {
      node = 1;
    } else {
      node = 0;
    }
    String storageFullName = "全部仓库";
    if (storageId > 0)
    {
      Record record = Db.use(configName).findById("b_storage", Integer.valueOf(storageId));
      if (record != null) {
        storageFullName = record.getStr("fullName");
      }
    }
    else
    {
      Record record = ComFunController.isOnlyOne(configName, "b_storage", null);
      if (record != null)
      {
        storageId = record.getInt("id").intValue();
        storageFullName = record.getStr("fullName");
      }
    }
    setAttr("storgeName", storageFullName);
    setAttr("supId", Integer.valueOf(supId));
    
    Map<String, Object> map = StockInit.dao.getPageBySupId(configName, node, basePrivs(STORAGE_PRIVS), basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, supId, "{0}", "productInitList", orderField, orderDirection, searchBaseAttr, searchBaseVal);
    

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
    columnConfig("init502");
    
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    setAttr("node", Integer.valueOf(node));
    
    setAttr("helpAmunitPattern", helpAmunitPattern);
    
    setAttr("storageId", Integer.valueOf(storageId));
    setAttr("storagePids", storagePids);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("searchBaseAttr", searchBaseAttr);
    setAttr("searchBaseVal", searchBaseVal);
    
    String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
    if (AioConstants.HAS_OPEN_ACCOUNT1.equals(hasOpenAccount)) {
      render("look.html");
    } else {
      render("page.html");
    }
  }
  
  public void tree()
  {
    List<Model> sorts = null;
    
    sorts = StockStatus.dao.getSortsByStorageId(loginConfigName(), 0);
    Iterator<Model> iter = sorts.iterator();
    
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model model = (Model)((Model)iter.next()).get("pro");
      HashMap<String, Object> node = new HashMap();
      node.put("id", model.get("id"));
      node.put("pId", model.get("supId"));
      node.put("name", model.get("fullName"));
      node.put("url", "init/productInit/list/" + model.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(-1));
    node.put("name", "商品信息");
    node.put("url", "init/productInit/list/0");
    nodeList.add(node);
    
    renderJson(nodeList);
  }
  
  public void list()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0))).intValue();
    int storageId = getParaToInt("storage.id").intValue();
    String storagePids = getPara("storage.pids");
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    if (StringUtils.isNotEmpty(searchBaseVal)) {
      node = 1;
    }
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    
    Map<String, Object> map = new HashMap();
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      map = StockInit.dao.getSupPageBySupIdAndStorageId(configName, basePrivs(STORAGE_PRIVS), basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, supId, storagePids, upObjectId, "productInitList", orderField, orderDirection);
    }
    else
    {
      map = StockInit.dao.getPageBySupId(configName, node, basePrivs(STORAGE_PRIVS), basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, supId, storagePids, "productInitList", orderField, orderDirection, searchBaseAttr, searchBaseVal);
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
    columnConfig("init502");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("searchBaseAttr", searchBaseAttr);
    setAttr("searchBaseVal", searchBaseVal);
    

    setAttr("helpAmunitPattern", helpAmunitPattern);
    setAttr("storageId", Integer.valueOf(storageId));
    setAttr("storagePids", storagePids);
    setAttr("pSupId", pSupId);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(node));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    

    String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
    if ("1".equals(hasOpenAccount)) {
      render("lookList.html");
    } else {
      render("list.html");
    }
  }
  
  public void line()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0))).intValue();
    String storageId = getPara("storage.id");
    String storagePids = getPara("storage.pids");
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    
    Map<String, Object> map = StockInit.dao.getPageLine(configName, basePrivs(STORAGE_PRIVS), basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, supId, storagePids, "productInitList", orderField, orderDirection, searchBaseAttr, searchBaseVal);
    
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
    columnConfig("init502");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("searchBaseAttr", searchBaseAttr);
    setAttr("searchBaseVal", searchBaseVal);
    

    setAttr("helpAmunitPattern", helpAmunitPattern);
    setAttr("storagePids", storagePids);
    setAttr("storageId", storageId);
    
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(1));
    
    String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
    if ("1".equals(hasOpenAccount)) {
      render("lookList.html");
    } else {
      render("list.html");
    }
  }
  
  public void proDistribution()
    throws SQLException, Exception
  {
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    String configName = loginConfigName();
    Integer pSupId = Integer.valueOf(0);
    if (supId > 0) {
      pSupId = Integer.valueOf(Storage.getPsupId(configName, supId));
    }
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "sge.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer pid = getParaToInt("product.id", getParaToInt("selectedObjectId", Integer.valueOf(0)));
    
    Product pro = new Product();
    String proPids = "";
    if (pid.intValue() != 0)
    {
      pro = (Product)Product.dao.findById(configName, pid);
      proPids = pro.getStr("pids");
    }
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    
    Map<String, Object> map = new HashMap();
    if (!"".equals(proPids)) {
      if (upObjectId > 0) {
        map = StockInit.dao.getSupPageBySupIdAndProId(configName, proPids, pageNum, numPerPage, supId, upObjectId, "proDistribution", orderField, orderDirection);
      } else {
        map = StockInit.dao.getProductDistribute(configName, proPids, pageNum, numPerPage, supId, "proDistribution", orderField, orderDirection);
      }
    }
    setAttr("proPids", proPids);
    setAttr("pSupId", pSupId);
    setAttr("supId", Integer.valueOf(supId));
    
    setAttr("objectId", Integer.valueOf(supId));
    
    setAttr("productId", pid);
    setAttr("pro", pro);
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("proDistribution.html");
  }
  
  public void proBatchNum()
  {
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    Integer productId = getParaToInt("selectedObjectId", Integer.valueOf(0));
    String configName = loginConfigName();
    Integer objectId = getParaToInt("objectId", Integer.valueOf(0));
    
    List<Model> stockList = StockInit.dao.getStockGroupAttr(configName, storageId, productId);
    
    String productFullName = "";
    if (productId.intValue() != 0)
    {
      Record record = Db.use(configName).findById("b_product", productId);
      if (record != null) {
        productFullName = record.getStr("fullName");
      }
    }
    setAttr("productFullName", productFullName);
    
    String storageFullName = "全部仓库";
    if (storageId.intValue() > 0)
    {
      Record record = Db.use(configName).findById("b_storage", storageId);
      if (record != null)
      {
        storageFullName = record.getStr("fullName");
        setAttr("storageFullName", Storage.dao.findById(configName, storageId).get("fullName"));
      }
    }
    setAttr("storageFullName", storageFullName);
    
    setAttr("storageId", storageId);
    setAttr("productId", productId);
    Product product = (Product)Product.dao.findById(configName, productId);
    setAttr("validity", product.getInt("validity"));
    setAttr("stockList", stockList);
    setAttr("objectId", objectId);
    
    String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
    if ("0".equals(hasOpenAccount)) {
      setAttr("hasOpenAccount", Integer.valueOf(0));
    } else {
      setAttr("hasOpenAccount", Integer.valueOf(1));
    }
    render("proBatchNum.html");
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "期初库存");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    


    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("规格");
    colTitle.add("型号");
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
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    

    int supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0))).intValue();
    String storagePids = getPara("storage.pids");
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    if (StringUtils.isNotEmpty(searchBaseVal)) {
      node = 1;
    }
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    
    Map<String, Object> pageMap = new HashMap();
    if (node == 0) {
      pageMap = StockInit.dao.getPageBySupId(configName, node, basePrivs(STORAGE_PRIVS), basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, supId, storagePids, "productInitList", orderField, orderDirection, null, null);
    } else if (node == 1) {
      pageMap = StockInit.dao.getPageLine(configName, basePrivs(STORAGE_PRIVS), basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, supId, storagePids, "productInitList", orderField, orderDirection, searchBaseAttr, searchBaseVal);
    }
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
  
  public void ProSearch()
  {
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
        String storageName = Storage.dao.findById(loginConfigName(), storageId, "fullName").getStr("fullName");
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
    
    Map<String, Object> map = StockStatus.dao.getPageByParam(loginConfigName(), pmap, listId, Integer.valueOf(pageNum), Integer.valueOf(numPerPage), orderField, orderDirection, storagePids, searchPar, searchVal, pattern);
    

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
  
  @Before({Tx.class})
  public void editInit()
  {
    String configName = loginConfigName();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    int productId = getParaToInt("selectedObjectId").intValue();
    StockInit stockInit = StockInit.dao.getStockInit(configName, Integer.valueOf(storageId), Integer.valueOf(productId));
    if (isPost())
    {
      String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
      if ("1".equals(hasOpenAccount))
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "期初已开账，不能保存");
        renderJson();
        return;
      }
      String amountStr = getPara("amount");
      String priceStr = getPara("price");
      BigDecimal amount;
    
      if (StringUtil.isNull(amountStr)) {
        amount = BigDecimal.ZERO;
      } else {
        amount = new BigDecimal(amountStr);
      }
      BigDecimal price;
    
      if (StringUtil.isNull(priceStr)) {
        price = BigDecimal.ZERO;
      } else {
        price = new BigDecimal(priceStr);
      }
      boolean flag = true;
      if (BigDecimalUtils.notNullZero(amount))
      {
        if (stockInit == null)
        {
          stockInit = new StockInit();
          stockInit.set("storageId", Integer.valueOf(storageId));
          stockInit.set("productId", Integer.valueOf(productId));
        }
        stockInit.set("amount", amount);
        stockInit.set("price", price);
        stockInit.set("money", BigDecimalUtils.mul(amount, price));
        stockInit.set("updateTime", DateUtils.getCurrentTime());
        stockInit.set("userId", Integer.valueOf(loginUserId()));
        if (stockInit.get("id") == null) {
          flag = stockInit.save(configName);
        } else {
          flag = stockInit.update(configName);
        }
      }
      else if (stockInit != null)
      {
        stockInit.delete(configName);
      }
      if (flag)
      {
        int supId = Product.dao.getPsupId(configName, productId);
        setAttr("statusCode", Integer.valueOf(200));
        setAttr("rel", "productInitList");
        setAttr("callbackType", "closeCurrent");
        setAttr("url", "init/productInit/list/" + supId);
        setAttr("selectedObjectId", Integer.valueOf(productId));
      }
      else
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "保存失败！");
      }
      renderJson();
    }
    else
    {
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      if (product.getInt("costArith").intValue() == 1)
      {
        setAttr("storageId", Integer.valueOf(storageId));
        setAttr("productId", Integer.valueOf(productId));
        setAttr("stockInit", stockInit);
        render("proInitAdd.html");
      }
      else
      {
        proBatchNum();
      }
    }
  }
  
  @Before({Tx.class})
  public void initBatchAdd()
  {
    String configName = loginConfigName();
    int storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    int productId = getParaToInt("productId", Integer.valueOf(0)).intValue();
    if (isPost())
    {
      String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
      if ("1".equals(hasOpenAccount))
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "期初已开账，不能保存");
        renderJson();
        return;
      }
      Boolean falg = Boolean.valueOf(true);
      StockInit sckInit = (StockInit)getModel(StockInit.class);
      

      Date produceDate = sckInit.getDate("produceDate");
      Date produceEndDate = sckInit.getDate("produceEndDate");
      if ((produceDate == null) && (produceEndDate != null))
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        setAttr("message", "到期日期要在生产日期之后");
        renderJson();
        return;
      }
      if ((produceDate != null) && (produceEndDate != null) && 
        (produceEndDate.before(produceDate)))
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        setAttr("message", "到期日期要在生产日期之后");
        renderJson();
        return;
      }
      if (BigDecimalUtils.notNullZero(sckInit.getBigDecimal("amount")))
      {
        falg = StockInit.dao.recodeIsExist(configName, storageId, productId, sckInit.getBigDecimal("price"), sckInit.getStr("batch"), sckInit.getDate("produceDate"), sckInit.getDate("produceEndDate"), 0);
        if (falg.booleanValue())
        {
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          setAttr("message", "单价、生产日期、到期日期、批号不能重复!");
          renderJson();
          return;
        }
        sckInit.remove("id");
        sckInit.set("storageId", Integer.valueOf(storageId));
        sckInit.set("productId", Integer.valueOf(productId));
        sckInit.set("updateTime", new Date());
        sckInit.set("userId", Integer.valueOf(loginUserId()));
        falg = Boolean.valueOf(sckInit.save(configName));
      }
      if (falg.booleanValue())
      {
        Map<String, Object> map = new HashMap();
        map.put("storage.id", Integer.valueOf(storageId));
        map.put("selectedObjectId", Integer.valueOf(productId));
        map.put("objectId", sckInit.get("id"));
        setAttr("statusCode", AioConstants.HTTP_RETURN200);
        setAttr("url", "init/productInit/proBatchNum");
        setAttr("data", map);
      }
      renderJson();
    }
    else
    {
      int validity = getParaToInt("validity", Integer.valueOf(0)).intValue();
      setAttr("storageId", Integer.valueOf(storageId));
      setAttr("productId", Integer.valueOf(productId));
      setAttr("validity", Integer.valueOf(validity));
      setAttr("action", "Add");
      render("proInitBatchAdd.html");
    }
  }
  
  @Before({Tx.class})
  public void initBatchEdit()
  {
    String configName = loginConfigName();
    int storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    int productId = getParaToInt("productId", Integer.valueOf(0)).intValue();
    if (isPost())
    {
      String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
      if ("1".equals(hasOpenAccount))
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "期初已开账，不能保存");
        renderJson();
        return;
      }
      Boolean falg = Boolean.valueOf(false);
      StockInit sckInit = (StockInit)getModel(StockInit.class);
      

      Date produceDate = sckInit.getDate("produceDate");
      Date produceEndDate = sckInit.getDate("produceEndDate");
      if ((produceDate == null) && (produceEndDate != null))
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        setAttr("message", "到期日期要在生产日期之后");
        renderJson();
        return;
      }
      if ((produceDate != null) && (produceEndDate != null) && 
        (produceEndDate.before(produceDate)))
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        setAttr("message", "到期日期要在生产日期之后");
        renderJson();
        return;
      }
      if (BigDecimalUtils.notNullZero(sckInit.getBigDecimal("amount")))
      {
        falg = StockInit.dao.recodeIsExist(configName, storageId, productId, sckInit.getBigDecimal("price"), sckInit.getStr("batch"), sckInit.getDate("produceDate"), sckInit.getDate("produceEndDate"), sckInit.getInt("id").intValue());
        if (falg.booleanValue())
        {
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          setAttr("message", "单价、生产日期、到期日期、批号不能重复!");
          renderJson();
          return;
        }
        sckInit.set("storageId", Integer.valueOf(storageId));
        sckInit.set("productId", Integer.valueOf(productId));
        sckInit.set("updateTime", new Date());
        sckInit.set("userId", Integer.valueOf(loginUserId()));
        falg = Boolean.valueOf(sckInit.update(configName));
      }
      else if (sckInit != null)
      {
        falg = Boolean.valueOf(sckInit.delete(configName));
      }
      if (falg.booleanValue())
      {
        Map<String, Object> map = new HashMap();
        map.put("storage.id", Integer.valueOf(storageId));
        map.put("selectedObjectId", Integer.valueOf(productId));
        map.put("objectId", sckInit.get("id"));
        setAttr("statusCode", AioConstants.HTTP_RETURN200);
        setAttr("url", "init/productInit/proBatchNum");
        setAttr("data", map);
      }
      else
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        setAttr("message", "系统异常，请联系客服！");
      }
      renderJson();
    }
    else
    {
      int selectObjID = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
      StockInit stockInit = (StockInit)StockInit.dao.findById(configName, Integer.valueOf(selectObjID));
      setAttr("storageId", Integer.valueOf(storageId));
      setAttr("productId", Integer.valueOf(productId));
      setAttr("stockInit", stockInit);
      setAttr("action", "Edit");
      render("proInitBatchAdd.html");
    }
  }
  
  @Before({Tx.class})
  public void initBatchDel()
  {
    String configName = loginConfigName();
    String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
    if ("1".equals(hasOpenAccount))
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "期初已开账，不能保存");
      renderJson();
      return;
    }
    int storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    int productId = getParaToInt("productId", Integer.valueOf(0)).intValue();
    int selectObjID = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    if (selectObjID == 0)
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "删除失败,数据不存在");
      renderJson();
      return;
    }
    StockInit stockInit = (StockInit)StockInit.dao.findById(configName, Integer.valueOf(selectObjID));
    Boolean flag = Boolean.valueOf(stockInit.delete(configName));
    if (flag.booleanValue())
    {
      Map<String, Object> map = new HashMap();
      map.put("storage.id", Integer.valueOf(storageId));
      map.put("selectedObjectId", Integer.valueOf(productId));
      
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("url", "init/productInit/proBatchNum");
      setAttr("isClose", "no");
      setAttr("data", map);
    }
    else
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "删除失败");
    }
    renderJson();
  }
}
