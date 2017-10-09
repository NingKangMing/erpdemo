package com.aioerp.controller.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.db.reports.stock.StockDistributed;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class StockDistributedController
  extends BaseController
{
  public static String listID = "stockDistributedList";
  
  public void index()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0))).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    if (StringUtils.isNotEmpty(searchBaseVal)) {
      node = 1;
    } else {
      node = 0;
    }
    String priceWay = getPara("priceWay", "stockPrice");
    String unitPattern = getPara("unitPattern", "baseUnit");
    
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    List<Model> storageList = Storage.dao.getStorages(configName, storagePrivs, null);
    setAttr("storageList", storageList);
    
    Map<String, Object> map = StockDistributed.dao.getListByParam(configName, basePrivs(PRODUCT_PRIVS), storageList, unitPattern, priceWay, supId, listID, pageNum, numPerPage, orderField, orderDirection, node, searchBaseAttr, searchBaseVal);
    

    columnConfig("cc522");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("node", Integer.valueOf(node));
    setAttr("searchBaseAttr", searchBaseAttr);
    setAttr("searchBaseVal", searchBaseVal);
    
    setAttr("priceWay", priceWay);
    setAttr("unitPattern", unitPattern);
    
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    
    setAttr("supId", Integer.valueOf(supId));
    render("page.html");
  }
  
  public void list()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String priceWay = getPara("priceWay", "stockPrice");
    String unitPattern = getPara("unitPattern", "baseUnit");
    
    int isNode = getParaToInt("node", Integer.valueOf(0)).intValue();
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    if (StringUtils.isNotEmpty(searchBaseVal)) {
      isNode = 1;
    } else {
      isNode = 0;
    }
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    List<Model> storageList = Storage.dao.getStorages(configName, storagePrivs, null);
    
    Map<String, Object> map = new HashMap();
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      map = StockDistributed.dao.getSupPageByParam(configName, storageList, unitPattern, priceWay, pageNum, numPerPage, supId, upObjectId, listID, orderField, orderDirection);
    }
    else
    {
      map = StockDistributed.dao.getListByParam(configName, basePrivs(PRODUCT_PRIVS), storageList, unitPattern, priceWay, supId, listID, pageNum, numPerPage, orderField, orderDirection, isNode, searchBaseAttr, searchBaseVal);
    }
    Integer pSupId = Integer.valueOf(0);
    if (supId > 0) {
      pSupId = Integer.valueOf(Product.dao.getPsupId(configName, supId));
    }
    columnConfig("cc522");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("node", Integer.valueOf(isNode));
    setAttr("searchBaseAttr", searchBaseAttr);
    setAttr("searchBaseVal", searchBaseVal);
    
    setAttr("priceWay", priceWay);
    setAttr("unitPattern", unitPattern);
    
    setAttr("pSupId", pSupId);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    
    setAttr("storageList", storageList);
    render("list.html");
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "库存状况分布");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    
    printCommonData(headData);
    
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    

    int supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0))).intValue();
    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "pro.rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String unitPattern = getPara("unitPattern", "baseUnit");
    String priceWay = getPara("priceWay", "stockPrice");
    
    int isNode = getParaToInt("node", Integer.valueOf(0)).intValue();
    
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    if (StringUtils.isNotEmpty(searchBaseVal)) {
      isNode = 1;
    } else {
      isNode = 0;
    }
    List<Model> storageList = Storage.dao.getStorages(configName, basePrivs(STORAGE_PRIVS), null);
    
    Map<String, Object> pageMap = StockDistributed.dao.getListByParam(configName, basePrivs(PRODUCT_PRIVS), storageList, unitPattern, priceWay, supId, listID, pageNum, numPerPage, orderField, orderDirection, isNode, searchBaseAttr, searchBaseVal);
    

    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("基本条码");
    if ("baseUnit".equals(unitPattern)) {
      colTitle.add("基本单位");
    } else if ("helpUnit1".equals(unitPattern)) {
      colTitle.add("辅助单位1");
    } else if ("helpUnit2".equals(unitPattern)) {
      colTitle.add("辅助单位2");
    }
    for (int i = 0; i < storageList.size(); i++)
    {
      Storage sge = (Storage)storageList.get(i);
      String sgeName = sge.getStr("fullName");
      colTitle.add(sgeName + "数量");
      colTitle.add(sgeName + "金额");
    }
    colTitle.add("合计数量");
    colTitle.add("合计金额");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    

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
      if ("baseUnit".equals(unitPattern)) {
        rowData.add(trimNull(pro.get("calculateUnit1")));
      } else if ("helpUnit1".equals(unitPattern)) {
        rowData.add(trimNull(pro.get("calculateUnit2")));
      } else if ("helpUnit2".equals(unitPattern)) {
        rowData.add(trimNull(pro.get("calculateUnit3")));
      }
      for (int j = 0; j < storageList.size(); j++)
      {
        rowData.add(trimNull(record.get("amount" + j)));
        rowData.add(trimNull(record.get("money" + j)));
      }
      rowData.add(trimNull(record.get("allAmount")));
      if (!hasCostPermitted) {
        rowData.add("***");
      } else {
        rowData.add(trimNull(record.get("allMoneys")));
      }
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
