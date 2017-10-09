package com.aioerp.controller.fz;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.ProductUnit;
import com.aioerp.model.fz.Formula;
import com.aioerp.model.fz.MinSellPrice;
import com.aioerp.model.fz.PriceDiscountTrack;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PricesManageController
  extends BaseController
{
  private static int formaluType0 = 0;
  private static int formaluType1 = 1;
  private static int formaluType2 = 2;
  private static int formaluType3 = 3;
  private static int formaluType4 = 4;
  private static int formaluType5 = 5;
  private static int itme0 = 0;
  private static int itme1 = 1;
  private static int itme2 = 2;
  private static int itme3 = 3;
  private static int itme4 = 4;
  private static int itme5 = 5;
  private static int itme6 = 6;
  
  public void index()
    throws SQLException, Exception
  {
    String[] productIds = getParaValues("productIdArr");
    String configName = loginConfigName();
    Map<String, Object> pageMap = Product.dao.getPageUnit(configName, "pricesManageList", AioConstants.START_PAGE, 20, productIds, null);
    setAttr("productIds", productIds);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "fz500", AioConstants.STATUS_ENABLE));
    setAttr("pageMap", pageMap);
    render("page.html");
  }
  
  public void search()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String[] productIds = getParaValues("productIdArr");
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    
    Map<String, Object> map = new HashMap();
    map.put("typeUnit", getPara("typeUnit"));
    map.put("orderField", getPara("orderField"));
    map.put("orderDirection", getPara("orderDirection"));
    
    Map<String, Object> pageMap = Product.dao.getPageUnit(configName, "pricesManageList", pageNum, numPerPage, productIds, map);
    
    setAttr("params", map);
    setAttr("productIds", productIds);
    setAttr("pageMap", pageMap);
    setAttr("orderField", getPara("orderField"));
    setAttr("orderDirection", getPara("orderDirection"));
    setAttr("rowList", ReportRow.dao.getListByType(configName, "fz500", AioConstants.STATUS_ENABLE));
    render("list.html");
  }
  
  public void toPrices()
  {
    String configName = loginConfigName();
    int productId = getParaToInt(0, Integer.valueOf(1)).intValue();
    int selectUnitId = getParaToInt(1, Integer.valueOf(1)).intValue();
    setAttr("productUnit", ProductUnit.dao.getObj(configName, productId, selectUnitId));
    setAttr("minSellPrice", MinSellPrice.dao.getMinObj(configName, null, productId, selectUnitId));
    setAttr("priceDiscountTrack", PriceDiscountTrack.dao.getRecentlyObj(configName, productId, selectUnitId));
    setAttr("productId", Integer.valueOf(productId));
    setAttr("selectUnitId", Integer.valueOf(selectUnitId));
    render("prices.html");
  }
  
  @Before({Tx.class})
  public void prices()
  {
    String configName = loginConfigName();
    int productId = getParaToInt("productId", Integer.valueOf(0)).intValue();
    int selectUnitId = getParaToInt("selectUnitId", Integer.valueOf(1)).intValue();
    Map<String, Object> result = new HashMap();
    BigDecimal retailPrice = new BigDecimal(getPara("retailPrice", "0"));
    BigDecimal defaultPrice1 = new BigDecimal(getPara("defaultPrice1", "0"));
    BigDecimal defaultPrice2 = new BigDecimal(getPara("defaultPrice2", "0"));
    BigDecimal defaultPrice3 = new BigDecimal(getPara("defaultPrice3", "0"));
    BigDecimal lastCostPrice = new BigDecimal(getPara("lastCostPrice", "0"));
    BigDecimal minSellPrice = null;
    if (StringUtils.isNotBlank(getPara("minSellPrice"))) {
      minSellPrice = new BigDecimal(getPara("minSellPrice"));
    }
    ProductUnit productUnit = ProductUnit.dao.getObj(configName, productId, selectUnitId);
    Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
    if (product != null)
    {
      if (selectUnitId == 2)
      {
        product.set("retailPrice2", retailPrice);
        product.set("defaultPrice21", defaultPrice1);
        product.set("defaultPrice22", defaultPrice2);
        product.set("defaultPrice23", defaultPrice3);
      }
      else if (selectUnitId == 3)
      {
        product.set("retailPrice3", retailPrice);
        product.set("defaultPrice31", defaultPrice1);
        product.set("defaultPrice32", defaultPrice2);
        product.set("defaultPrice33", defaultPrice3);
      }
      else
      {
        product.set("retailPrice1", retailPrice);
        product.set("defaultPrice11", defaultPrice1);
        product.set("defaultPrice12", defaultPrice2);
        product.set("defaultPrice13", defaultPrice3);
      }
      product.update(configName);
    }
    if (productUnit != null)
    {
      productUnit.set("retailPrice", retailPrice);
      productUnit.set("defaultPrice1", defaultPrice1);
      productUnit.set("defaultPrice2", defaultPrice2);
      productUnit.set("defaultPrice3", defaultPrice3);
      productUnit.update(configName);
    }
    else
    {
      productUnit = new ProductUnit();
      productUnit.set("productId", Integer.valueOf(productId));
      productUnit.set("selectUnitId", Integer.valueOf(selectUnitId));
      productUnit.set("retailPrice", retailPrice);
      productUnit.set("defaultPrice1", defaultPrice1);
      productUnit.set("defaultPrice2", defaultPrice2);
      productUnit.set("defaultPrice3", defaultPrice3);
      productUnit.save(configName);
    }
    PriceDiscountTrack priceDiscountTrack = PriceDiscountTrack.dao.getRecentlyObj(configName, productId, selectUnitId);
    if (priceDiscountTrack != null)
    {
      priceDiscountTrack.set("lastCostPrice", lastCostPrice);
      priceDiscountTrack.set("lastCostDate", new Date());
      priceDiscountTrack.update(configName);
    }
    else
    {
      priceDiscountTrack = new PriceDiscountTrack();
      priceDiscountTrack.set("productId", Integer.valueOf(productId));
      priceDiscountTrack.set("selectUnitId", Integer.valueOf(selectUnitId));
      priceDiscountTrack.set("lastCostPrice", lastCostPrice);
      priceDiscountTrack.set("lastCostDate", new Date());
      priceDiscountTrack.save(configName);
    }
    MinSellPrice sellPrice = MinSellPrice.dao.getMinObj(configName, null, productId, selectUnitId);
    if (sellPrice != null)
    {
      sellPrice.set("minSellPrice", minSellPrice);
      sellPrice.update(configName);
    }
    else
    {
      sellPrice = new MinSellPrice();
      sellPrice.set("productId", Integer.valueOf(productId));
      sellPrice.set("selectUnitId", Integer.valueOf(selectUnitId));
      sellPrice.set("minSellPrice", minSellPrice);
      sellPrice.save(configName);
    }
    result.put("statusCode", AioConstants.HTTP_RETURN200);
    result.put("rel", "pricesManageList");
    result.put("callbackType", "closeCurrent");
    renderJson(result);
  }
  
  public void toFormula()
  {
    List<Model> formulaList = Formula.dao.getModelTypeFormulaList(loginConfigName(), AioConstants.BILL_ROW_TYPE2002);
    if ((formulaList != null) && (formulaList.size() > 0)) {
      for (int i = 0; i < formulaList.size(); i++)
      {
        Model m = (Model)formulaList.get(i);
        if (m.getInt("type").intValue() == formaluType0) {
          setAttr("defaultPrice1", m);
        } else if (m.getInt("type").intValue() == formaluType1) {
          setAttr("defaultPrice2", m);
        } else if (m.getInt("type").intValue() == formaluType2) {
          setAttr("defaultPrice3", m);
        } else if (m.getInt("type").intValue() == formaluType3) {
          setAttr("retailPrice", m);
        } else if (m.getInt("type").intValue() == formaluType4) {
          setAttr("minSellPrice", m);
        } else if (m.getInt("type").intValue() == formaluType5) {
          setAttr("lastCostPrice", m);
        }
      }
    }
    setAttr("productIds", getParaValues("productIdArr"));
    setAttr("typeUnit", getPara("typeUnit"));
    render("formula.html");
  }
  
  @Before({Tx.class})
  public void formula()
  {
    String configName = loginConfigName();
    String[] productIds = getParaValues("productIdArr");
    String typeUnit = getPara("typeUnit");
    Formula defaultPrice1 = (Formula)getModel(Formula.class, "defaultPrice1");
    Formula defaultPrice2 = (Formula)getModel(Formula.class, "defaultPrice2");
    Formula defaultPrice3 = (Formula)getModel(Formula.class, "defaultPrice3");
    Formula retailPrice = (Formula)getModel(Formula.class, "retailPrice");
    Formula minSellPrice = (Formula)getModel(Formula.class, "minSellPrice");
    Formula lastCostPrice = (Formula)getModel(Formula.class, "lastCostPrice");
    
    int defaultPrice1T = defaultPrice1.getInt("item").intValue();
    int defaultPrice2T = defaultPrice2.getInt("item").intValue();
    int defaultPrice3T = defaultPrice3.getInt("item").intValue();
    int retailPriceT = retailPrice.getInt("item").intValue();
    int minSellPriceT = minSellPrice.getInt("item").intValue();
    int lastCostPriceT = lastCostPrice.getInt("item").intValue();
    
    String defaultPrice1O = defaultPrice1.getStr("operate");
    String defaultPrice2O = defaultPrice2.getStr("operate");
    String defaultPrice3O = defaultPrice3.getStr("operate");
    String retailPriceO = retailPrice.getStr("operate");
    String minSellPriceO = minSellPrice.getStr("operate");
    String lastCostPriceO = lastCostPrice.getStr("operate");
    
    BigDecimal defaultPrice1P = defaultPrice1.getBigDecimal("param");
    BigDecimal defaultPrice2P = defaultPrice2.getBigDecimal("param");
    BigDecimal defaultPrice3P = defaultPrice3.getBigDecimal("param");
    BigDecimal retailPriceP = retailPrice.getBigDecimal("param");
    BigDecimal minSellPriceP = minSellPrice.getBigDecimal("param");
    BigDecimal lastCostPriceP = lastCostPrice.getBigDecimal("param");
    if ((BigDecimalUtils.compare(defaultPrice1P, BigDecimal.ZERO) == -1) || (BigDecimalUtils.compare(defaultPrice2P, BigDecimal.ZERO) == -1) || 
      (BigDecimalUtils.compare(defaultPrice3P, BigDecimal.ZERO) == -1) || (BigDecimalUtils.compare(retailPriceP, BigDecimal.ZERO) == -1) || 
      (BigDecimalUtils.compare(minSellPriceP, BigDecimal.ZERO) == -1) || (BigDecimalUtils.compare(lastCostPriceP, BigDecimal.ZERO) == -1))
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "输入的运算数须大于或等于0！");
      renderJson();
      return;
    }
    if (((defaultPrice1T != 0) && ("/".equals(defaultPrice1O)) && (BigDecimalUtils.compare(defaultPrice1P, BigDecimal.ZERO) == 0)) || 
      ((defaultPrice2T != 0) && ("/".equals(defaultPrice2O)) && (BigDecimalUtils.compare(defaultPrice2P, BigDecimal.ZERO) == 0)) || 
      ((defaultPrice3T != 0) && ("/".equals(defaultPrice3O)) && (BigDecimalUtils.compare(defaultPrice3P, BigDecimal.ZERO) == 0)) || 
      ((retailPriceT != 0) && ("/".equals(retailPriceO)) && (BigDecimalUtils.compare(retailPriceP, BigDecimal.ZERO) == 0)) || 
      ((minSellPriceT != 0) && ("/".equals(minSellPriceO)) && (BigDecimalUtils.compare(minSellPriceP, BigDecimal.ZERO) == 0)) || (
      (lastCostPriceT != 0) && ("/".equals(lastCostPriceO)) && (BigDecimalUtils.compare(lastCostPriceP, BigDecimal.ZERO) == 0)))
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "除数不能为0！");
      renderJson();
      return;
    }
    Map<String, Object> map = new HashMap();
    map.put("typeUnit", typeUnit);
    List<Model> productUnitList = Product.dao.getListUnit(configName, productIds, map);
    for (Model productUnit : productUnitList)
    {
      ProductUnit oldUnit = (ProductUnit)productUnit.get("unit");
      if (oldUnit == null) {
        oldUnit = new ProductUnit();
      }
      BigDecimal retailPriceU = oldUnit.getBigDecimal("retailPrice");
      BigDecimal defaultPrice1U = oldUnit.getBigDecimal("defaultPrice1");
      BigDecimal defaultPrice2U = oldUnit.getBigDecimal("defaultPrice2");
      BigDecimal defaultPrice3U = oldUnit.getBigDecimal("defaultPrice3");
      BigDecimal lastCostPriceU = productUnit.getBigDecimal("lastCostPrice");
      BigDecimal avgpriceU = productUnit.getBigDecimal("avgprice");
      if (oldUnit != null)
      {
        editUnit(defaultPrice1T, defaultPrice1O, defaultPrice1P, 
          retailPriceU, defaultPrice1U, defaultPrice2U, 
          defaultPrice3U, lastCostPriceU, avgpriceU, oldUnit, "defaultPrice1");
        editUnit(defaultPrice2T, defaultPrice2O, defaultPrice2P, 
          retailPriceU, defaultPrice1U, defaultPrice2U, 
          defaultPrice3U, lastCostPriceU, avgpriceU, oldUnit, "defaultPrice2");
        editUnit(defaultPrice3T, defaultPrice3O, defaultPrice3P, 
          retailPriceU, defaultPrice1U, defaultPrice2U, 
          defaultPrice3U, lastCostPriceU, avgpriceU, oldUnit, "defaultPrice3");
        editUnit(retailPriceT, retailPriceO, retailPriceP, 
          retailPriceU, defaultPrice1U, defaultPrice2U, 
          defaultPrice3U, lastCostPriceU, avgpriceU, oldUnit, "retailPrice");
        oldUnit.update(configName);
        Integer productId = oldUnit.getInt("productId");
        Product product = (Product)Product.dao.findById(configName, productId);
        if (product != null)
        {
          Integer selectUnitId = oldUnit.getInt("selectUnitId");
          if ((selectUnitId != null) && (selectUnitId.intValue() == 2))
          {
            product.set("retailPrice2", oldUnit.getBigDecimal("retailPrice"));
            product.set("defaultPrice21", oldUnit.getBigDecimal("defaultPrice1"));
            product.set("defaultPrice22", oldUnit.getBigDecimal("defaultPrice2"));
            product.set("defaultPrice23", oldUnit.getBigDecimal("defaultPrice3"));
          }
          else if ((selectUnitId != null) && (selectUnitId.intValue() == 3))
          {
            product.set("retailPrice3", oldUnit.getBigDecimal("retailPrice"));
            product.set("defaultPrice31", oldUnit.getBigDecimal("defaultPrice1"));
            product.set("defaultPrice32", oldUnit.getBigDecimal("defaultPrice2"));
            product.set("defaultPrice33", oldUnit.getBigDecimal("defaultPrice3"));
          }
          else
          {
            product.set("retailPrice1", oldUnit.getBigDecimal("retailPrice"));
            product.set("defaultPrice11", oldUnit.getBigDecimal("defaultPrice1"));
            product.set("defaultPrice12", oldUnit.getBigDecimal("defaultPrice2"));
            product.set("defaultPrice13", oldUnit.getBigDecimal("defaultPrice3"));
          }
          product.update(configName);
        }
        if (productId == null) {
          productId = Integer.valueOf(0);
        }
        Integer selectUnitId = oldUnit.getInt("selectUnitId");
        if (selectUnitId == null) {
          selectUnitId = Integer.valueOf(0);
        }
        if (minSellPriceT != itme0)
        {
          MinSellPrice sellPrice = MinSellPrice.dao.getMinObj(configName, null, productId.intValue(), selectUnitId.intValue());
          BigDecimal price = getSetPrice(minSellPriceT, minSellPriceO, minSellPriceP, retailPriceU, defaultPrice1U, defaultPrice2U, defaultPrice3U, lastCostPriceU, avgpriceU);
          if (sellPrice != null)
          {
            sellPrice.set("minSellPrice", price);
            sellPrice.update(configName);
          }
          else
          {
            sellPrice = new MinSellPrice();
            sellPrice.set("productId", productId);
            sellPrice.set("selectUnitId", selectUnitId);
            sellPrice.set("minSellPrice", price);
            sellPrice.save(configName);
          }
        }
        if (lastCostPriceT != itme0)
        {
          PriceDiscountTrack priceDiscountTrack = PriceDiscountTrack.dao.getRecentlyObj(configName, productId.intValue(), selectUnitId.intValue());
          BigDecimal price = getSetPrice(lastCostPriceT, lastCostPriceO, lastCostPriceP, retailPriceU, defaultPrice1U, defaultPrice2U, defaultPrice3U, lastCostPriceU, avgpriceU);
          if (priceDiscountTrack != null)
          {
            priceDiscountTrack.set("lastCostPrice", price);
            priceDiscountTrack.set("lastCostDate", new Date());
            priceDiscountTrack.update(configName);
          }
          else
          {
            priceDiscountTrack = new PriceDiscountTrack();
            priceDiscountTrack.set("productId", productId);
            priceDiscountTrack.set("selectUnitId", selectUnitId);
            priceDiscountTrack.set("lastCostPrice", price);
            priceDiscountTrack.set("lastCostDate", new Date());
            priceDiscountTrack.save(configName);
          }
        }
      }
    }
    List<Model> formulaList = Formula.dao.getModelTypeFormulaList(configName, AioConstants.BILL_ROW_TYPE2002);
    
    Formula defaultPrice1F = null;
    Formula defaultPrice2F = null;
    Formula defaultPrice3F = null;
    Formula retailPriceF = null;
    Formula minSellPriceF = null;
    Formula lastCostPriceF = null;
    for (int i = 0; i < formulaList.size(); i++)
    {
      Formula m = (Formula)formulaList.get(i);
      if (m.getInt("type").intValue() == formaluType0) {
        defaultPrice1F = m;
      } else if (m.getInt("type").intValue() == formaluType1) {
        defaultPrice2F = m;
      } else if (m.getInt("type").intValue() == formaluType2) {
        defaultPrice3F = m;
      } else if (m.getInt("type").intValue() == formaluType3) {
        retailPriceF = m;
      } else if (m.getInt("type").intValue() == formaluType4) {
        minSellPriceF = m;
      } else if (m.getInt("type").intValue() == formaluType5) {
        lastCostPriceF = m;
      }
    }
    if (defaultPrice1F != null)
    {
      defaultPrice1F.set("item", Integer.valueOf(defaultPrice1T));
      defaultPrice1F.set("operate", defaultPrice1O);
      defaultPrice1F.set("param", defaultPrice1P);
      defaultPrice1F.update(configName);
    }
    else
    {
      defaultPrice1F = new Formula();
      defaultPrice1F.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2002));
      defaultPrice1F.set("type", Integer.valueOf(formaluType0));
      defaultPrice1F.set("item", Integer.valueOf(defaultPrice1T));
      defaultPrice1F.set("operate", defaultPrice1O);
      defaultPrice1F.set("param", defaultPrice1P);
      defaultPrice1F.save(configName);
    }
    if (defaultPrice2F != null)
    {
      defaultPrice2F.set("item", Integer.valueOf(defaultPrice2T));
      defaultPrice2F.set("operate", defaultPrice2O);
      defaultPrice2F.set("param", defaultPrice2P);
      defaultPrice2F.update(configName);
    }
    else
    {
      defaultPrice2F = new Formula();
      defaultPrice2F.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2002));
      defaultPrice2F.set("type", Integer.valueOf(formaluType1));
      defaultPrice2F.set("item", Integer.valueOf(defaultPrice2T));
      defaultPrice2F.set("operate", defaultPrice2O);
      defaultPrice2F.set("param", defaultPrice2P);
      defaultPrice2F.save(configName);
    }
    if (defaultPrice3F != null)
    {
      defaultPrice3F.set("item", Integer.valueOf(defaultPrice3T));
      defaultPrice3F.set("operate", defaultPrice3O);
      defaultPrice3F.set("param", defaultPrice3P);
      defaultPrice3F.update(configName);
    }
    else
    {
      defaultPrice3F = new Formula();
      defaultPrice3F.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2002));
      defaultPrice3F.set("type", Integer.valueOf(formaluType2));
      defaultPrice3F.set("item", Integer.valueOf(defaultPrice3T));
      defaultPrice3F.set("operate", defaultPrice3O);
      defaultPrice3F.set("param", defaultPrice3P);
      defaultPrice3F.save(configName);
    }
    if (retailPriceF != null)
    {
      retailPriceF.set("item", Integer.valueOf(retailPriceT));
      retailPriceF.set("operate", retailPriceO);
      retailPriceF.set("param", retailPriceP);
      retailPriceF.update(configName);
    }
    else
    {
      retailPriceF = new Formula();
      retailPriceF.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2002));
      retailPriceF.set("type", Integer.valueOf(formaluType3));
      retailPriceF.set("item", Integer.valueOf(retailPriceT));
      retailPriceF.set("operate", retailPriceO);
      retailPriceF.set("param", retailPriceP);
      retailPriceF.save(configName);
    }
    if (minSellPriceF != null)
    {
      minSellPriceF.set("item", Integer.valueOf(minSellPriceT));
      minSellPriceF.set("operate", minSellPriceO);
      minSellPriceF.set("param", minSellPriceP);
      minSellPriceF.update(configName);
    }
    else
    {
      minSellPriceF = new Formula();
      minSellPriceF.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2002));
      minSellPriceF.set("type", Integer.valueOf(formaluType4));
      minSellPriceF.set("item", Integer.valueOf(minSellPriceT));
      minSellPriceF.set("operate", minSellPriceO);
      minSellPriceF.set("param", minSellPriceP);
      minSellPriceF.save(configName);
    }
    if (lastCostPriceF != null)
    {
      lastCostPriceF.set("item", Integer.valueOf(lastCostPriceT));
      lastCostPriceF.set("operate", lastCostPriceO);
      lastCostPriceF.set("param", lastCostPriceP);
      lastCostPriceF.update(configName);
    }
    else
    {
      lastCostPriceF = new Formula();
      lastCostPriceF.set("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE2002));
      lastCostPriceF.set("type", Integer.valueOf(formaluType5));
      lastCostPriceF.set("item", Integer.valueOf(lastCostPriceT));
      lastCostPriceF.set("operate", lastCostPriceO);
      lastCostPriceF.set("param", lastCostPriceP);
      lastCostPriceF.save(configName);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("rel", "pricesManageList");
    setAttr("callbackType", "closeCurrent");
    renderJson();
  }
  
  private void editUnit(int itme, String operate, BigDecimal param, BigDecimal retailPriceU, BigDecimal defaultPrice1U, BigDecimal defaultPrice2U, BigDecimal defaultPrice3U, BigDecimal lastCostPriceU, BigDecimal avgpriceU, ProductUnit unit, String attribute)
  {
    if (itme1 == itme)
    {
      if ("+".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.add(lastCostPriceU, param));
      } else if ("-".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.sub(lastCostPriceU, param));
      } else if ("*".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.mul(lastCostPriceU, param));
      } else if ("/".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.div(lastCostPriceU, param));
      }
    }
    else if (itme2 == itme)
    {
      if ("+".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.add(avgpriceU, param));
      } else if ("-".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.sub(avgpriceU, param));
      } else if ("*".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.mul(avgpriceU, param));
      } else if ("/".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.div(avgpriceU, param));
      }
    }
    else if (itme3 == itme)
    {
      if ("+".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.add(retailPriceU, param));
      } else if ("-".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.sub(retailPriceU, param));
      } else if ("*".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.mul(retailPriceU, param));
      } else if ("/".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.div(retailPriceU, param));
      }
    }
    else if (itme4 == itme)
    {
      if ("+".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.add(defaultPrice1U, param));
      } else if ("-".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.sub(defaultPrice1U, param));
      } else if ("*".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.mul(defaultPrice1U, param));
      } else if ("/".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.div(defaultPrice1U, param));
      }
    }
    else if (itme5 == itme)
    {
      if ("+".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.add(defaultPrice2U, param));
      } else if ("-".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.sub(defaultPrice2U, param));
      } else if ("*".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.mul(defaultPrice2U, param));
      } else if ("/".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.div(defaultPrice2U, param));
      }
    }
    else if (itme6 == itme) {
      if ("+".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.add(defaultPrice3U, param));
      } else if ("-".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.sub(defaultPrice3U, param));
      } else if ("*".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.mul(defaultPrice3U, param));
      } else if ("/".equals(operate)) {
        unit.set(attribute, BigDecimalUtils.div(defaultPrice3U, param));
      }
    }
  }
  
  private BigDecimal getSetPrice(int itme, String operate, BigDecimal param, BigDecimal retailPriceU, BigDecimal defaultPrice1U, BigDecimal defaultPrice2U, BigDecimal defaultPrice3U, BigDecimal lastCostPriceU, BigDecimal avgpriceU)
  {
    BigDecimal price = BigDecimal.ZERO;
    if (itme1 == itme)
    {
      if ("+".equals(operate)) {
        price = BigDecimalUtils.add(lastCostPriceU, param);
      } else if ("-".equals(operate)) {
        price = BigDecimalUtils.sub(lastCostPriceU, param);
      } else if ("*".equals(operate)) {
        price = BigDecimalUtils.mul(lastCostPriceU, param);
      } else if ("/".equals(operate)) {
        price = BigDecimalUtils.div(lastCostPriceU, param);
      }
    }
    else if (itme2 == itme)
    {
      if ("+".equals(operate)) {
        price = BigDecimalUtils.add(avgpriceU, param);
      } else if ("-".equals(operate)) {
        price = BigDecimalUtils.sub(avgpriceU, param);
      } else if ("*".equals(operate)) {
        price = BigDecimalUtils.mul(avgpriceU, param);
      } else if ("/".equals(operate)) {
        price = BigDecimalUtils.div(avgpriceU, param);
      }
    }
    else if (itme3 == itme)
    {
      if ("+".equals(operate)) {
        price = BigDecimalUtils.add(retailPriceU, param);
      } else if ("-".equals(operate)) {
        price = BigDecimalUtils.sub(retailPriceU, param);
      } else if ("*".equals(operate)) {
        price = BigDecimalUtils.mul(retailPriceU, param);
      } else if ("/".equals(operate)) {
        price = BigDecimalUtils.div(retailPriceU, param);
      }
    }
    else if (itme4 == itme)
    {
      if ("+".equals(operate)) {
        price = BigDecimalUtils.add(defaultPrice1U, param);
      } else if ("-".equals(operate)) {
        price = BigDecimalUtils.sub(defaultPrice1U, param);
      } else if ("*".equals(operate)) {
        price = BigDecimalUtils.mul(defaultPrice1U, param);
      } else if ("/".equals(operate)) {
        price = BigDecimalUtils.div(defaultPrice1U, param);
      }
    }
    else if (itme5 == itme)
    {
      if ("+".equals(operate)) {
        price = BigDecimalUtils.add(defaultPrice2U, param);
      } else if ("-".equals(operate)) {
        price = BigDecimalUtils.sub(defaultPrice2U, param);
      } else if ("*".equals(operate)) {
        price = BigDecimalUtils.mul(defaultPrice2U, param);
      } else if ("/".equals(operate)) {
        price = BigDecimalUtils.div(defaultPrice2U, param);
      }
    }
    else if (itme6 == itme) {
      if ("+".equals(operate)) {
        price = BigDecimalUtils.add(defaultPrice3U, param);
      } else if ("-".equals(operate)) {
        price = BigDecimalUtils.sub(defaultPrice3U, param);
      } else if ("*".equals(operate)) {
        price = BigDecimalUtils.mul(defaultPrice3U, param);
      } else if ("/".equals(operate)) {
        price = BigDecimalUtils.div(defaultPrice3U, param);
      }
    }
    return price;
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "物价管理");
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
    colTitle.add("计量单位");
    colTitle.add("条码");
    colTitle.add("零售价");
    colTitle.add("预售价1");
    colTitle.add("预售价2");
    colTitle.add("预售价3");
    colTitle.add("最近进价");
    colTitle.add("最低售价");
    colTitle.add("商品成本价");
    colTitle.add("毛利率");
    colTitle.add("数量");
    
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
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    

    String[] productIds = getParaValues("productIdArr");
    
    Map<String, Object> map = new HashMap();
    map.put("typeUnit", getPara("typeUnit"));
    map.put("orderField", getPara("orderField"));
    map.put("orderDirection", getPara("orderDirection"));
    
    Map<String, Object> pageMap = Product.dao.getPageUnit(loginConfigName(), "pricesManageList", 1, totalCount, productIds, map);
    List<Model> list = (List)pageMap.get("pageList");
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < list.size(); i++)
    {
      Product model = (Product)list.get(i);
      rowData.add(trimNull(i + 1));
      
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("fullName")));
      rowData.add(trimNull(model.get("smallName")));
      rowData.add(trimNull(model.get("spell")));
      rowData.add(trimNull(model.get("standard")));
      rowData.add(trimNull(model.get("model")));
      rowData.add(trimNull(model.get("field")));
      
      ProductUnit unit = (ProductUnit)model.get("unit");
      if (unit != null)
      {
        rowData.add(trimNull(unit.get("calculateUnit")));
        rowData.add(trimNull(unit.get("barCode")));
        rowData.add(trimNull(unit.get("retailPrice")));
        rowData.add(trimNull(unit.get("defaultPrice1")));
        rowData.add(trimNull(unit.get("defaultPrice2")));
        rowData.add(trimNull(unit.get("defaultPrice3")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
        rowData.add("");
        rowData.add("");
        rowData.add("");
        rowData.add("");
      }
      if (!hasCostPermitted)
      {
        rowData.add("***");
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        rowData.add(trimNull(model.get("lastCostPrice")));
        rowData.add(trimNull(model.get("minSellPrice")));
        rowData.add(trimNull(model.get("avgprice")));
      }
      if ((unit != null) && (BigDecimalUtils.notNullZero(unit.getBigDecimal("defaultPrice1")))) {
        rowData.add(trimNull(BigDecimalUtils.div(BigDecimalUtils.sub(unit.getBigDecimal("defaultPrice1"), model.getBigDecimal("avgprice")), unit.getBigDecimal("defaultPrice1"))));
      } else {
        rowData.add("");
      }
      if (model.getBigDecimal("amount") != null) {
        rowData.add(trimNull(model.getBigDecimal("amount").setScale(4, 4)));
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
}
