package com.aioerp.controller.fz;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.ProductUnit;
import com.aioerp.model.base.Unit;
import com.aioerp.model.fz.Formula;
import com.aioerp.model.fz.PriceDiscountTrack;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceDiscountTrackController
  extends BaseController
{
  public static String listID = "fz_priceDiscountTrack";
  public static int hasMark = 1;
  public static int removeMark = 0;
  protected static int formaluType0 = 0;
  protected static int formaluType1 = 1;
  protected static int formaluType2 = 2;
  protected static int formaluType3 = 3;
  public static int itme0 = 0;
  public static int itme1 = 1;
  public static int itme2 = 2;
  public static int itme10 = 10;
  public static int itme3 = 3;
  public static int itme4 = 4;
  public static int itme5 = 5;
  public static int itme6 = 6;
  public static int itme7 = 7;
  public static int itme8 = 8;
  public static int itme9 = 9;
  
  public void toSearchDialog()
  {
    render("/WEB-INF/template/fz/priceDiscountTrack/searchDialog.html");
  }
  
  @Before({Tx.class})
  public void dialogSearch()
    throws SQLException, Exception
  {
    Map<String, Object> map = requestPageToMap(null, null);
    String type = getPara(0, "first");
    setAttr("pageMap", com(map));
    

    columnConfig("fz502");
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/fz/priceDiscountTrack/page.html";
    } else if ((type.equals("page")) || (type.equals("newAdd"))) {
      returnPage = "/WEB-INF/template/fz/priceDiscountTrack/list.html";
    }
    render(returnPage);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String type = getPara(0, "first");
    String unitId = "0";
    String productId = "0";
    if (type.equals("first")) {
      map.put("orderField", "pd.id");
    }
    String assistUnit = "";
    String hasMark = "";
    


    String aimDiv = getPara("aimDiv", "all");
    if (type.equals("first"))
    {
      unitId = getPara("unit.id", "0");
      productId = getPara("product.id", "0");
      assistUnit = "all";
      hasMark = "all";
      aimDiv = assistUnit + "-" + hasMark;
    }
    else if (type.equals("search"))
    {
      unitId = getPara("unitId", "0");
      productId = getPara("productId", "0");
      String[] paras = aimDiv.split("-");
      assistUnit = paras[0];
      hasMark = paras[1];
    }
    else if (type.equals("page"))
    {
      unitId = getPara("unitId", "0");
      productId = getPara("productId", "0");
      String[] paras = aimDiv.split("-");
      assistUnit = paras[0];
      hasMark = paras[1];
    }
    else if (type.equals("newAdd"))
    {
      unitId = getPara("unitId", "0");
      productId = getPara("productId", "0");
      int newAddUnitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
      int newAddProductId = getParaToInt("product.id", Integer.valueOf(0)).intValue();
      PriceDiscountTrack.dao.ifNoExitsInsert(configName, newAddUnitId, newAddProductId);
    }
    map.put("listID", listID);
    map.put("unitId", unitId);
    map.put("productId", productId);
    map.put("assistUnit", assistUnit);
    map.put("hasMark", hasMark);
    mapToResponse(map);
    
    Map<String, Object> dataMap = PriceDiscountTrack.dao.loadPriceDiscountPage(configName, map);
    setAttr("aimDiv", aimDiv);
    return dataMap;
  }
  
  public void toEditPriceDiscount()
  {
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    Model m = PriceDiscountTrack.dao.findById(loginConfigName(), Integer.valueOf(id));
    setAttr("obj", m);
    render("/WEB-INF/template/fz/priceDiscountTrack/editPriceDiscount.html");
  }
  
  @Before({Tx.class})
  public void editPriceDiscount()
  {
    String configName = loginConfigName();
    Model m = (Model)getModel(PriceDiscountTrack.class);
    Model oldModel = PriceDiscountTrack.dao.findById(configName, m.getInt("id"));
    oldModel.set("lastCostPrice", m.getBigDecimal("lastCostPrice"));
    oldModel.set("lastSellPrice", m.getBigDecimal("lastSellPrice"));
    oldModel.set("lastCostDiscouont", m.getBigDecimal("lastCostDiscouont"));
    oldModel.set("lastSellDiscouont", m.getBigDecimal("lastSellDiscouont"));
    boolean flag = oldModel.update(configName);
    if (flag)
    {
      setAttr("statusCode", Integer.valueOf(200));
      setAttr("rel", listID);
      setAttr("callbackType", "closeCurrent");
    }
    else
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "保存失败");
    }
    renderJson();
  }
  
  public void delInfo()
  {
    String configName = loginConfigName();
    String type = getPara(0, "");
    int objId = 0;
    Model model = null;
    String message = "";
    if (type.equals("unit"))
    {
      objId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
      model = Unit.dao.findById(configName, Integer.valueOf(objId));
      message = "确定要删除指定客户(编号:" + model.getStr("code") + " 全名:" + model.getStr("fullName") + ")的所有价格跟踪吗？";
    }
    else if (type.equals("prd"))
    {
      objId = getParaToInt("product.id", Integer.valueOf(0)).intValue();
      model = Product.dao.findById(configName, Integer.valueOf(objId));
      message = "确定要删除指定商品(编号:" + model.getStr("code") + " 全名:" + model.getStr("fullName") + ")的所有价格跟踪吗？";
    }
    setAttr("message", message);
    setAttr("type", type);
    setAttr("objId", Integer.valueOf(objId));
    setAttr("listID", listID);
    render("/WEB-INF/template/fz/priceDiscountTrack/delInfo.html");
  }
  
  @Before({Tx.class})
  public void delete()
  {
    try
    {
      int id = getParaToInt(0, Integer.valueOf(0)).intValue();
      PriceDiscountTrack.dao.deleteById(loginConfigName(), Integer.valueOf(id));
      
      setAttr("statusCode", Integer.valueOf(200));
      setAttr("rel", listID);
      setAttr("message", "删除成功");
    }
    catch (Exception e)
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "保存失败");
    }
    renderJson();
  }
  
  @Before({Tx.class})
  public void delByUnitOrProduct()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String type = getPara(0, "unit");
    int objId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (type.equals("unit")) {
      PriceDiscountTrack.dao.deleteByUnit(configName, objId);
    } else if (type.equals("prd")) {
      PriceDiscountTrack.dao.deleteByProduct(configName, objId);
    }
    Map<String, Object> map = requestPageToMap(null, null);
    String assistUnit = "";
    String hasMark = "";
    String unitId = getPara("unitId", "0");
    String productId = getPara("productId", "0");
    
    String aimDiv = getPara("aimDiv", "all");
    String[] paras = aimDiv.split("-");
    assistUnit = paras[0];
    hasMark = paras[1];
    

    map.put("listID", listID);
    map.put("unitId", unitId);
    map.put("productId", productId);
    map.put("assistUnit", assistUnit);
    map.put("hasMark", hasMark);
    mapToResponse(map);
    Map<String, Object> dataMap = PriceDiscountTrack.dao.loadPriceDiscountPage(configName, map);
    
    columnConfig("fz502");
    setAttr("pageMap", dataMap);
    setAttr("aimDiv", aimDiv);
    render("/WEB-INF/template/fz/priceDiscountTrack/list.html");
  }
  
  @Before({Tx.class})
  public void isMark()
  {
    try
    {
      String type = getPara(0, "has");
      int id = getParaToInt(1, Integer.valueOf(0)).intValue();
      String configName = loginConfigName();
      
      PriceDiscountTrack pd = (PriceDiscountTrack)PriceDiscountTrack.dao.findById(configName, Integer.valueOf(id));
      if (type.equals("has")) {
        pd.set("isMark", Integer.valueOf(hasMark));
      } else if (type.equals("remove")) {
        pd.set("isMark", Integer.valueOf(removeMark));
      }
      pd.update(configName);
      
      setAttr("statusCode", Integer.valueOf(200));
      setAttr("message", "保存失败");
    }
    catch (Exception e)
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "保存失败");
    }
    renderJson();
  }
  
  public void toFormulaSet()
  {
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    int productId = getParaToInt("product.id", Integer.valueOf(0)).intValue();
    String aimDiv = getPara("aimDiv", "all-all");
    setAttr("unitId", Integer.valueOf(unitId));
    setAttr("productId", Integer.valueOf(productId));
    setAttr("aimDiv", aimDiv);
    


    List<Model> formulaList = Formula.dao.getModelTypeFormulaList(loginConfigName(), AioConstants.BILL_ROW_TYPE2000);
    if ((formulaList != null) && (formulaList.size() > 0)) {
      for (int i = 0; i < formulaList.size(); i++)
      {
        Model m = (Model)formulaList.get(i);
        if (m.getInt("type").intValue() == formaluType0) {
          setAttr("costPrice", m);
        } else if (m.getInt("type").intValue() == formaluType1) {
          setAttr("sellPrice", m);
        } else if (m.getInt("type").intValue() == formaluType2) {
          setAttr("costDiscount", m);
        } else if (m.getInt("type").intValue() == formaluType3) {
          setAttr("sellDiscount", m);
        }
      }
    }
    render("/WEB-INF/template/fz/priceDiscountTrack/formulaSet.html");
  }
  
  @Before({Tx.class})
  public void formulaSet()
  {
    String configName = loginConfigName();
    Formula costPrice = (Formula)getModel(Formula.class, "costPrice");
    Formula sellPrice = (Formula)getModel(Formula.class, "sellPrice");
    Formula costDiscount = (Formula)getModel(Formula.class, "costDiscount");
    Formula sellDiscount = (Formula)getModel(Formula.class, "sellDiscount");
    

    BigDecimal costPriceP = costPrice.getBigDecimal("param");
    BigDecimal sellPriceP = sellPrice.getBigDecimal("param");
    BigDecimal costDiscountP = costDiscount.getBigDecimal("param");
    BigDecimal sellDiscountP = sellDiscount.getBigDecimal("param");
    if ((BigDecimalUtils.compare(costPriceP, BigDecimal.ZERO) == -1) || (BigDecimalUtils.compare(sellPriceP, BigDecimal.ZERO) == -1) || (BigDecimalUtils.compare(costDiscountP, BigDecimal.ZERO) == -1) || (BigDecimalUtils.compare(sellDiscountP, BigDecimal.ZERO) == -1))
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "输入的运算数须大于或等于0！");
      renderJson();
      return;
    }
    String unitId = getPara("unit.id", "0");
    String productId = getPara("product.id", "0");
    String aimDiv = getPara("aimDiv", "all-all");
    String[] paras = aimDiv.split("-");
    String assistUnit = paras[0];
    String hasMark = paras[1];
    List<Model> list = PriceDiscountTrack.dao.formulaSetData(configName, unitId, productId, assistUnit, hasMark);
    


    int costPriceItem = costPrice.getInt("item").intValue();
    String costPriceOperate = costPrice.getStr("operate") == null ? "+" : costPrice.getStr("operate");
    BigDecimal costPriceMoneys = BigDecimal.ZERO;
    
    int sellPriceItem = sellPrice.getInt("item").intValue();
    String sellPriceOperate = sellPrice.getStr("operate") == null ? "+" : sellPrice.getStr("operate");
    BigDecimal sellPriceMoneys = BigDecimal.ZERO;
    
    int costDiscountItem = costDiscount.getInt("item").intValue();
    String costDiscountOperate = costDiscount.getStr("operate") == null ? "+" : costDiscount.getStr("operate");
    BigDecimal costDiscountMoneys = BigDecimal.ZERO;
    
    int sellDiscountItem = sellDiscount.getInt("item").intValue();
    String sellDiscountOperate = sellDiscount.getStr("operate") == null ? "+" : sellDiscount.getStr("operate");
    BigDecimal sellDiscountMoneys = BigDecimal.ZERO;
    

    List<Model> formulaList = Formula.dao.getModelTypeFormulaList(configName, AioConstants.BILL_ROW_TYPE2000);
    Formula m1;
    if ((formulaList != null) && (formulaList.size() > 0))
    {
      for (int i = 0; i < formulaList.size(); i++)
      {
        Model m = (Model)formulaList.get(i);
        if (m.getInt("type").intValue() == formaluType0)
        {
          m.set("item", Integer.valueOf(costPriceItem));
          m.set("operate", costPriceOperate);
          m.set("param", costPriceP);
        }
        else if (m.getInt("type").intValue() == formaluType1)
        {
          m.set("item", Integer.valueOf(sellPriceItem));
          m.set("operate", sellPriceOperate);
          m.set("param", sellPriceP);
        }
        else if (m.getInt("type").intValue() == formaluType2)
        {
          m.set("item", Integer.valueOf(costDiscountItem));
          m.set("operate", costDiscountOperate);
          m.set("param", costDiscountP);
        }
        else if (m.getInt("type").intValue() == formaluType3)
        {
          m.set("item", Integer.valueOf(sellDiscountItem));
          m.set("operate", sellDiscountOperate);
          m.set("param", sellDiscountP);
        }
        m.update(configName);
      }
    }
    else
    {
      Formula m0 = new Formula();
      m0.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2000));
      m0.set("type", Integer.valueOf(formaluType0));
      m0.set("item", Integer.valueOf(costPriceItem));
      m0.set("operate", costPriceOperate);
      m0.set("param", costPriceP);
      m0.save(configName);
      
      m1 = new Formula();
      m1.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2000));
      m1.set("type", Integer.valueOf(formaluType1));
      m1.set("item", Integer.valueOf(sellPriceItem));
      m1.set("operate", sellPriceOperate);
      m1.set("param", sellPriceP);
      m1.save(configName);
      
      Formula m2 = new Formula();
      m2.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2000));
      m2.set("type", Integer.valueOf(formaluType2));
      m2.set("item", Integer.valueOf(costDiscountItem));
      m2.set("operate", costDiscountOperate);
      m2.set("param", costDiscountP);
      m2.save(configName);
      
      Formula m3 = new Formula();
      m3.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2000));
      m3.set("type", Integer.valueOf(formaluType3));
      m3.set("item", Integer.valueOf(sellDiscountItem));
      m3.set("operate", sellDiscountOperate);
      m3.set("param", sellDiscountP);
      m3.save(configName);
    }
    for (Model model : list)
    {
      PriceDiscountTrack pd = (PriceDiscountTrack)model.get("pd");
      Product pro = (Product)model.get("pro");
      
      ProductUnit punit = (ProductUnit)model.get("punit");
      if (costPriceItem != itme0)
      {
        costPriceMoneys = PriceDiscountTrack.dao.whichFormula(configName, costPriceItem, pd, pro, punit);
        BigDecimal costPriceResult = BigDecimalUtils.operation(costPriceOperate, costPriceMoneys, costPriceP);
        pd.set("lastCostPrice", costPriceResult);
      }
      if (sellPriceItem != itme0)
      {
        sellPriceMoneys = PriceDiscountTrack.dao.whichFormula(configName, sellPriceItem, pd, pro, punit);
        BigDecimal costPriceResult = BigDecimalUtils.operation(sellPriceOperate, sellPriceMoneys, sellPriceP);
        pd.set("lastSellPrice", costPriceResult);
      }
      if (costDiscountItem != itme0)
      {
        costDiscountMoneys = PriceDiscountTrack.dao.whichFormula(configName, costDiscountItem, pd, pro, punit);
        BigDecimal costPriceResult = BigDecimalUtils.operation(costDiscountOperate, costDiscountMoneys, costDiscountP);
        pd.set("lastCostDiscouont", costPriceResult);
      }
      if (sellDiscountItem != itme0)
      {
        sellDiscountMoneys = PriceDiscountTrack.dao.whichFormula(configName, sellDiscountItem, pd, pro, punit);
        BigDecimal sellDiscountResult = BigDecimalUtils.operation(sellDiscountOperate, sellDiscountMoneys, sellDiscountP);
        pd.set("lastSellDiscouont", sellDiscountResult);
      }
      pd.update(configName);
    }
    setAttr("statusCode", Integer.valueOf(200));
    setAttr("rel", listID);
    setAttr("callbackType", "closeCurrent");
    renderJson();
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "价格折扣跟踪");
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
    

    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("计量单位");
    colTitle.add("最近进价");
    colTitle.add("最近售价");
    colTitle.add("最近进货折扣");
    colTitle.add("最近销售折扣");
    colTitle.add("最近进货时间");
    colTitle.add("最近销售时间");
    colTitle.add("条码");
    colTitle.add("标记");
    

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
      Model product = (Model)detail.get("pro");
      Model unit = (Model)detail.get("unit");
      Model pd = (Model)detail.get("pd");
      String selectUnitIdStr = "";
      String barCode = "";
      String isMarkStr = "";
      int selectUnitId = pd.getInt("selectUnitId").intValue();
      if (selectUnitId == 1)
      {
        selectUnitIdStr = product.getStr("calculateUnit1");
        selectUnitIdStr = product.getStr("barCode1");
      }
      else if (selectUnitId == 2)
      {
        selectUnitIdStr = product.getStr("calculateUnit2");
        selectUnitIdStr = product.getStr("barCode2");
      }
      else if (selectUnitId == 3)
      {
        selectUnitIdStr = product.getStr("calculateUnit3");
        selectUnitIdStr = product.getStr("barCode3");
      }
      Integer isMark = pd.getInt("isMark");
      if ((isMark != null) && (isMark.intValue() == 1)) {
        isMarkStr = "√";
      }
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      rowData.add(trimNull(product.getStr("smallName")));
      rowData.add(trimNull(product.getStr("spell")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
      rowData.add(trimNull(product.getStr("field")));
      rowData.add(trimNull(unit.getStr("code")));
      rowData.add(trimNull(unit.getStr("fullName")));
      rowData.add(trimNull(selectUnitIdStr));
      rowData.add(trimNull(pd.getBigDecimal("lastCostPrice"), hasCostPermitted));
      rowData.add(trimNull(pd.getBigDecimal("lastSellPrice")));
      rowData.add(trimNull(pd.getBigDecimal("lastCostDiscouont")));
      rowData.add(trimNull(pd.getBigDecimal("lastSellDiscouont")));
      rowData.add(trimNull(DateUtils.format(pd.getDate("lastCostDate"), "yyyy-MM-dd HH:mm:ss")));
      rowData.add(trimNull(DateUtils.format(pd.getDate("lastSellDate"), "yyyy-MM-dd HH:mm:ss")));
      rowData.add(trimNull(barCode));
      rowData.add(trimNull(isMarkStr));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
