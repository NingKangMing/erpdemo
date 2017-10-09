package com.aioerp.controller.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.db.reports.stock.StockBound;
import com.aioerp.db.reports.stock.StockStatus;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.fz.Formula;
import com.aioerp.model.stock.Stock;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StockBoundController
  extends BaseController
{
  public static String listID = "stockBoundList";
  
  public void index()
    throws Exception
  {
    String configName = loginConfigName();
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer isSearch = getParaToInt("isSearch", Integer.valueOf(0));
    Integer storageId = getParaToInt("storageId", Integer.valueOf(0));
    String storagePids = getPara("storage.pids", "{0}");
    String storageName = getPara("storageName", "全部仓库");
    
    Map<String, Object> map = new HashMap();
    if (isSearch.intValue() == 0)
    {
      map = StockBound.dao.getPageBySupId(configName, basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, supId, Integer.valueOf(0), listID, orderField, orderDirection);
    }
    else
    {
      String searchPar = getPara("searchPar", "all");
      String searchVal = getPara("searchVal");
      map = StockBound.dao.getPageByParam(configName, basePrivs(PRODUCT_PRIVS), listID, Integer.valueOf(pageNum), Integer.valueOf(numPerPage), orderField, orderDirection, storageId, searchPar, searchVal);
      setAttr("searchPar", searchPar);
      setAttr("searchVal", searchVal);
    }
    columnConfig("cc526");
    
    setAttr("isSearch", isSearch);
    setAttr("storageId", storageId);
    setAttr("storagePids", storagePids);
    setAttr("storgeName", storageName);
    
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    
    setAttr("supId", Integer.valueOf(supId));
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
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
      node.put("url", "reports/stock/stockBound/list/" + model.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(-1));
    node.put("name", "商品信息");
    node.put("url", "reports/stock/stockBound/list/0");
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
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer isSearch = getParaToInt("isSearch", Integer.valueOf(0));
    
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    
    Map<String, Object> map = new HashMap();
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      map = StockBound.dao.getSupPageBySupIdAndStorageId(configName, basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, supId, Integer.valueOf(storageId), upObjectId, listID, orderField, orderDirection);
    }
    else if (isSearch.intValue() == 0)
    {
      map = StockBound.dao.getPageBySupId(configName, basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, supId, Integer.valueOf(storageId), listID, orderField, orderDirection);
    }
    else
    {
      String searchPar = getPara("searchPar", "all");
      String searchVal = getPara("searchVal");
      map = StockBound.dao.getPageByParam(configName, basePrivs(PRODUCT_PRIVS), listID, Integer.valueOf(pageNum), Integer.valueOf(numPerPage), orderField, orderDirection, Integer.valueOf(storageId), searchPar, searchVal);
      setAttr("isSearch", isSearch);
      setAttr("searchPar", searchPar);
      setAttr("searchVal", searchVal);
    }
    Integer pSupId = Integer.valueOf(0);
    if (supId > 0) {
      pSupId = Integer.valueOf(Product.dao.getPsupIdBySupId(configName, supId));
    }
    columnConfig("cc526");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    setAttr("storageId", Integer.valueOf(storageId));
    setAttr("storagePids", storagePids);
    setAttr("pSupId", pSupId);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(0));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    
    render("list.html");
  }
  
  public void proSearch()
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
  
  @Before({Tx.class})
  public void boundSet()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      StockBound stockBound = (StockBound)getModel(StockBound.class);
      Integer productId = stockBound.getInt("productId");
      Integer storageId = stockBound.getInt("storageId");
      Product product = (Product)Product.dao.findById(configName, productId);
      Integer node = product.getInt("node");
      
      BigDecimal inputMax = stockBound.getBigDecimal("max");
      BigDecimal inputMin = stockBound.getBigDecimal("min");
      if ((BigDecimalUtils.compare(inputMax, BigDecimal.ZERO) == -1) || (BigDecimalUtils.compare(inputMin, BigDecimal.ZERO) == -1))
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "请输入正确的上下限！");
        renderJson();
        return;
      }
      boolean flag = false;
      if (node.intValue() == 2)
      {
        String pids = product.getStr("pids");
        List<Integer> ids = product.getLastChilIds(configName, basePrivs(PRODUCT_PRIVS), pids);
        for (Integer pid : ids)
        {
          StockBound sb = (StockBound)StockBound.dao.getModelByParam(configName, storageId, pid);
          if (sb == null)
          {
            sb = new StockBound();
            sb.set("productId", pid);
            sb.set("storageId", storageId);
            sb.set("max", inputMax);
            sb.set("min", inputMin);
            flag = sb.save(configName);
          }
          else
          {
            sb.set("max", inputMax);
            sb.set("min", inputMin);
            flag = sb.update(configName);
          }
          if (!flag) {
            break;
          }
        }
      }
      else if (stockBound.getInt("id").intValue() == 0)
      {
        stockBound.remove("id");
        flag = stockBound.save(configName);
      }
      else
      {
        flag = stockBound.update(configName);
      }
      if (flag)
      {
        Integer supId = Integer.valueOf(product.getPsupId(configName, productId.intValue()));
        setAttr("statusCode", Integer.valueOf(200));
        setAttr("rel", listID);
        setAttr("callbackType", "closeCurrent");
        setAttr("url", "reports/stock/stockBound/list/" + supId);
        
        setAttr("selectedObjectId", productId);
      }
      else
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "保存失败");
      }
      renderJson();
    }
    else
    {
      Integer productId = getParaToInt(0, Integer.valueOf(0));
      Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
      Integer isSearch = getParaToInt("isSearch", Integer.valueOf(0));
      String storageName = "全部仓库";
      if (storageId.intValue() > 0)
      {
        Storage storage = (Storage)Storage.dao.findById(configName, storageId);
        storageName = storage.getStr("fullName");
      }
      StockBound stockBound = (StockBound)StockBound.dao.getModelByParam(configName, storageId, productId);
      setAttr("productId", productId);
      setAttr("storageId", storageId);
      setAttr("isSearch", isSearch);
      setAttr("storageName", storageName);
      setAttr("stockBound", stockBound);
      render("boundSet.html");
    }
  }
  
  public void maxPage()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String listId = "stockMaxBoundPage";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    String storagePids = getPara("storage.pids", "{0}");
    String storageName = getPara("storage.fullName", "全部仓库");
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    
    Map<String, Object> map = StockBound.dao.getMaxPage(configName, basePrivs(PRODUCT_PRIVS), listId, storageId, storagePids, pageNum, numPerPage, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    

    columnConfig("cc527");
    
    setAttr("storageId", storageId);
    setAttr("storagePids", storagePids);
    setAttr("storgeName", storageName);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("searchBaseAttr", searchBaseAttr);
    setAttr("searchBaseVal", searchBaseVal);
    render("maxPage.html");
  }
  
  public void minPage()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String listId = "stockMinBoundPage";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    String storagePids = getPara("storage.pids", "{0}");
    String storageName = getPara("storage.fullName", "全部仓库");
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    
    Map<String, Object> map = StockBound.dao.getMinPage(configName, basePrivs(PRODUCT_PRIVS), listId, storageId, storagePids, pageNum, numPerPage, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    

    columnConfig("cc528");
    
    setAttr("storageId", storageId);
    setAttr("storagePids", storagePids);
    setAttr("storgeName", storageName);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("searchBaseAttr", searchBaseAttr);
    setAttr("searchBaseVal", searchBaseVal);
    render("minPage.html");
  }
  
  @Before({Tx.class})
  public void boundBuild()
  {
    String configName = loginConfigName();
    Integer sgeId = getParaToInt("storage.id");
    String sgePids = getPara("storage.pids");
    Integer supId = getParaToInt("supId");
    String searchPar = getPara("searchPar", "all");
    String searchVal = getPara("searchVal");
    if (isPost())
    {
      Formula maxLimit = (Formula)getModel(Formula.class, "maxLimit");
      Formula minLimit = (Formula)getModel(Formula.class, "minLimit");
      
      BigDecimal inputMax = maxLimit.getBigDecimal("param");
      BigDecimal inputMin = minLimit.getBigDecimal("param");
      if ((BigDecimalUtils.compare(inputMax, BigDecimal.ZERO) == -1) || (BigDecimalUtils.compare(inputMin, BigDecimal.ZERO) == -1))
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "输入的运算数须大于或等于0！");
        renderJson();
        return;
      }
      int maxItem = maxLimit.getInt("item").intValue();
      String maxOperate = maxLimit.getStr("operate") == null ? "+" : maxLimit.getStr("operate");
      BigDecimal maxParam = inputMax;
      
      int minItem = minLimit.getInt("item").intValue();
      String minOperate = minLimit.getStr("operate") == null ? "+" : minLimit.getStr("operate");
      BigDecimal minParam = inputMin;
      
      List<Map<String, Object>> results = new ArrayList();
      List<Model> products = Product.dao.getProducts(configName, basePrivs(PRODUCT_PRIVS), supId, searchPar, searchVal);
      Integer proId;
      for (Model model : products)
      {
        proId = model.getInt("id");
        BigDecimal maxAmount = Stock.getAmountByItem(configName, maxItem, proId.intValue(), sgePids);
        BigDecimal max = BigDecimalUtils.operation(maxOperate, maxAmount, maxParam);
        
        BigDecimal minAmount = Stock.getAmountByItem(configName, minItem, proId.intValue(), sgePids);
        BigDecimal min = BigDecimalUtils.operation(minOperate, minAmount, minParam);
        if (BigDecimalUtils.compare(max, BigDecimal.ZERO) == -1) {
          max = BigDecimal.ZERO;
        }
        if (BigDecimalUtils.compare(min, BigDecimal.ZERO) == -1) {
          min = BigDecimal.ZERO;
        }
        if (BigDecimalUtils.compare(max, min) != 1)
        {
          setAttr("statusCode", Integer.valueOf(300));
          setAttr("message", "商品【" + model.getStr("fullName") + "】设置的库存上限须大于库存下限，请重试！");
          renderJson();
          return;
        }
        Map<String, Object> map = new HashMap();
        map.put("proId", proId);
        map.put("max", max);
        map.put("min", min);
        results.add(map);
      }
      Boolean flag = Boolean.valueOf(false);
      if (maxLimit.getInt("id") == null)
      {
        maxLimit.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2001));
        maxLimit.set("type", Integer.valueOf(1));
        maxLimit.set("operate", maxOperate);
        flag = Boolean.valueOf(maxLimit.save(configName));
      }
      else
      {
        flag = Boolean.valueOf(maxLimit.update(configName));
      }
      if (minLimit.getInt("id") == null)
      {
        minLimit.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2001));
        minLimit.set("type", Integer.valueOf(-1));
        minLimit.set("operate", minOperate);
        flag = Boolean.valueOf(minLimit.save(configName));
      }
      else
      {
        flag = Boolean.valueOf(minLimit.update(configName));
      }
      if (flag.booleanValue())
      {
        for (Object reMap : results)
        {
           proId = (Integer)((Map)reMap).get("proId");
          BigDecimal max = (BigDecimal)((Map)reMap).get("max");
          BigDecimal min = (BigDecimal)((Map)reMap).get("min");
          StockBound sb = (StockBound)StockBound.dao.getModelByParam(configName, sgeId, proId);
          if (sb == null)
          {
            sb = new StockBound();
            sb.set("productId", proId);
            sb.set("storageId", sgeId);
            sb.set("max", max);
            sb.set("min", min);
            flag = Boolean.valueOf(sb.save(configName));
          }
          else
          {
            sb.set("max", max);
            sb.set("min", min);
            flag = Boolean.valueOf(sb.update(configName));
          }
        }
        setAttr("statusCode", Integer.valueOf(200));
        setAttr("rel", listID);
        setAttr("callbackType", "closeCurrent");
        setAttr("url", "reports/stock/stockBound/list/0");
      }
      else
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "保存失败");
      }
      renderJson();
    }
    else
    {
      Formula maxLimit = (Formula)Formula.dao.getFormulaByType(configName, AioConstants.BILL_ROW_TYPE2001, 1);
      Formula minLimit = (Formula)Formula.dao.getFormulaByType(configName, AioConstants.BILL_ROW_TYPE2001, -1);
      setAttr("sgeId", sgeId);
      setAttr("sgePids", sgePids);
      setAttr("supId", supId);
      setAttr("searchPar", searchPar);
      setAttr("searchVal", searchVal);
      
      setAttr("maxLimit", maxLimit);
      setAttr("minLimit", minLimit);
      render("boundBuild.html");
    }
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "库存上下限报警设置");
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
    colTitle.add("库存上限");
    colTitle.add("库存下限");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    
    int supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0))).intValue();
    int storageId = getParaToInt("storage.id").intValue();
    
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer isSearch = getParaToInt("isSearch", Integer.valueOf(0));
    
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    Map<String, Object> pageMap;
    if (isSearch.intValue() == 0)
    {
      pageMap = StockBound.dao.getPageBySupId(configName, basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, supId, Integer.valueOf(storageId), listID, orderField, orderDirection);
    }
    else
    {
      String searchPar = getPara("searchPar", "all");
      String searchVal = getPara("searchVal");
      pageMap = StockBound.dao.getPageByParam(configName, basePrivs(PRODUCT_PRIVS), listID, Integer.valueOf(pageNum), Integer.valueOf(numPerPage), orderField, orderDirection, Integer.valueOf(storageId), searchPar, searchVal);
    }
    List<Model> list = (List)pageMap.get("pageList");
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
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
      
      rowData.add(trimNull(record.get("maxs")));
      rowData.add(trimNull(record.get("mins")));
      
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public void printMaxAndMin()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    int m = getParaToInt(0, Integer.valueOf(1)).intValue();
    
    data.put("reportNo", Integer.valueOf(301));
    
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
    colTitle.add("基本条码");
    colTitle.add("基本单位");
    colTitle.add("辅助单位");
    colTitle.add("库存数量");
    if (m == 1)
    {
      colTitle.add("库存上限");
      data.put("reportName", "库存商品上限报警");
    }
    else
    {
      colTitle.add("库存下限");
      data.put("reportName", "库存商品下限报警");
    }
    colTitle.add("调整数量");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    String storagePids = getPara("storage.pids", "{0}");
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    Map<String, Object> pageMap;
   
    if (m == 1) {
      pageMap = StockBound.dao.getMaxPage(configName, basePrivs(PRODUCT_PRIVS), "", storageId, storagePids, pageNum, numPerPage, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    } else {
      pageMap = StockBound.dao.getMinPage(configName, basePrivs(PRODUCT_PRIVS), "", storageId, storagePids, pageNum, numPerPage, orderField, orderDirection, searchBaseAttr, searchBaseVal);
    }
    List<Model> list = (List)pageMap.get("pageList");
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
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
      
      rowData.add(trimNull(record.get("sckAmount")));
      if (m == 1) {
        rowData.add(trimNull(record.get("maxNum")));
      } else {
        rowData.add(trimNull(record.get("minNum")));
      }
      rowData.add(trimNull(record.get("adjust")));
      
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
