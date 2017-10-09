package com.aioerp.controller.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.db.reports.stock.StockReport;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class VirtualStockController
  extends BaseController
{
  public static String listID = "virtualStockList";
  
  public void index()
    throws Exception
  {
    String configName = loginConfigName();
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String filterVal = getPara("selectFilter", "all");
    
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    Integer productId = getParaToInt("product.id", Integer.valueOf(0));
    String storageName = "全部仓库";
    String productName = "全部商品";
    String storagePids = getPara("storagePids", "{0}");
    String productPids = getPara("productPids", "{0}");
    int proNode = 2;
    if (storageId.intValue() != 0)
    {
      storageName = getPara("storage.fullName", "全部仓库");
      storagePids = Storage.dao.findById(configName, storageId, "pids").getStr("pids");
    }
    if (productId.intValue() != 0)
    {
      productName = getPara("product.fullName", "全部商品");
      Product product = (Product)Product.dao.findById(configName, productId);
      productPids = product.getStr("pids");
      proNode = product.getInt("node").intValue();
      if (proNode == 2) {
        supId = productId.intValue();
      }
    }
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    
    Map<String, Object> map = StockReport.dao.getPageByParam(configName, pmap, pageNum, numPerPage, supId, storagePids, proNode, productPids, filterVal, listID, orderField, orderDirection);
    
    setAttr("storagePids", storagePids);
    setAttr("productPids", productPids);
    setAttr("storageName", storageName);
    setAttr("productName", productName);
    
    setAttr("proNode", Integer.valueOf(proNode));
    setAttr("filterVal", filterVal);
    setAttr("supId", Integer.valueOf(supId));
    
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    

    columnConfig("cc529");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("page.html");
  }
  
  public void tree()
  {
    String configName = loginConfigName();
    List<Model> sorts = null;
    String productPids = getPara("productPids", "{0}");
    sorts = Product.dao.getAllSorts(configName, productPids);
    
    Iterator<Model> iter = sorts.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model model = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", model.get("id"));
      node.put("pId", model.get("supId"));
      node.put("name", model.get("fullName"));
      node.put("url", "reports/stock/virtualStock/list/" + model.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(-1));
    node.put("name", "商品信息");
    node.put("url", "reports/stock/virtualStock/list/0");
    nodeList.add(node);
    
    renderJson(nodeList);
  }
  
  public void list()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    
    int node = getParaToInt("node", Integer.valueOf(2)).intValue();
    
    int storageId = getParaToInt("storageId", getParaToInt(2, Integer.valueOf(0))).intValue();
    String storagePids = getPara("storagePids", "{0}");
    String productPids = getPara("productPids", "{0}");
    
    String inlet = getPara("inlet");
    if ("tree".equals(inlet)) {
      productPids = "{0}";
    }
    String filterVal = getPara("filterVal", "all");
    setAttr("filterVal", filterVal);
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    
    Map<String, Object> map = new HashMap();
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      map = StockReport.dao.getSupPageByParam(configName, pmap, pageNum, numPerPage, supId, storagePids, upObjectId, listID, orderField, orderDirection);
      node = 2;
    }
    else
    {
      map = StockReport.dao.getPageByParam(configName, pmap, pageNum, numPerPage, supId, storagePids, node, productPids, filterVal, listID, orderField, orderDirection);
    }
    Integer pSupId = Integer.valueOf(0);
    if (supId > 0) {
      pSupId = Integer.valueOf(Product.dao.getPsupId(configName, supId));
    }
    columnConfig("cc529");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    setAttr("storagePids", storagePids);
    setAttr("productPids", productPids);
    
    setAttr("storageId", Integer.valueOf(storageId));
    setAttr("pSupId", pSupId);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(node));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    
    render("list.html");
  }
  
  public void line() {}
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "虚拟库存状况");
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
    colTitle.add("库存数量");
    colTitle.add("成本单价");
    colTitle.add("库存金额");
    colTitle.add("进货订单");
    colTitle.add("销售订单");
    colTitle.add("订单库存");
    colTitle.add("草稿数量");
    colTitle.add("虚拟库存");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    

    int pageNum = 1;
    int numPerPage = totalCount;
    int supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0))).intValue();
    String storagePids = getPara("storagePids", "{0}");
    int node = getParaToInt("node", Integer.valueOf(2)).intValue();
    String productPids = getPara("productPids", "{0}");
    String filterVal = getPara("filterVal", "all");
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    
    Map<String, Object> pageMap = StockReport.dao.getPageByParam(configName, pmap, pageNum, numPerPage, supId, storagePids, node, productPids, filterVal, listID, orderField, orderDirection);
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    



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
      


      rowData.add(trimNull(record.get("amounts")));
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
      rowData.add(trimNull(record.get("boughtOrde")));
      rowData.add(trimNull(record.get("sellOrde")));
      rowData.add(trimNull(record.get("ordeAmount")));
      rowData.add(trimNull(record.get("draftAmount")));
      rowData.add(trimNull(record.get("virtualAmount")));
      
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
  
  public void toFormula()
  {
    String configName = loginConfigName();
    String getValue1 = AioerpSys.dao.getValue1(configName, "virtualStockFormula");
    if (StringUtils.isBlank(getValue1)) {
      getValue1 = " 库存数量 + 进货订货 - 销售订货 + 草稿数量";
    } else {
      getValue1 = getValue1.replaceAll("amounts", "库存数量").replaceAll("boughtOrde", "进货订货").replaceAll("sellOrde", "销售订货").replaceAll("draftAmount", "草稿数量");
    }
    setAttr("virtualStockFormula", getValue1);
    render("formula.html");
  }
  
  public void formula()
  {
    String configName = loginConfigName();
    String formula = getPara("formula");
    formula = formula.replaceAll("库存数量", "amounts").replaceAll("进货订货", "boughtOrde").replaceAll("销售订货", "sellOrde").replaceAll("草稿数量", "draftAmount");
    AioerpSys.dao.getObj(configName, "virtualStockFormula");
    AioerpSys.dao.sysSaveOrUpdate(configName, "virtualStockFormula", formula, null);
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("callbackType", "closeCurrent");
    renderJson();
  }
}
