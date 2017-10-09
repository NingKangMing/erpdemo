package com.aioerp.controller.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Avgprice;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.ProductUnit;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.bought.PurchaseBarterBill;
import com.aioerp.model.finance.PayMoneyBill;
import com.aioerp.model.finance.getmoney.GetMoneyBill;
import com.aioerp.model.fz.PriceDiscountTrack;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class StockController
  extends BaseController
{
  private static final String Moudle_1 = "ProData";
  
  public void orderOneRecord()
    throws UnsupportedEncodingException
  {
    String configName = loginConfigName();
    String whichCallBack = getPara(0, "");
    String attrType = getPara(1, "");
    String attrVal = getPara(2, "");
    int storageId = getParaToInt(3, Integer.valueOf(0)).intValue();
    
    attrVal = URLDecoder.decode(attrVal, "UTF-8");
    

    Map<String, Object> paras = new HashMap();
    String moudle = getPara("moudle", "ProData");
    String handleDate = getPara("handleDate", "");
    if (storageId == 0) {
      storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    }
    paras.put("moudle", moudle);
    paras.put("handleDate", handleDate);
    paras.put("storageId", Integer.valueOf(storageId));
    setAttr("moudle", moudle);
    setAttr("handleDate", handleDate);
    setAttr("storageId", Integer.valueOf(storageId));
    if ((!attrVal.equals("")) && (Product.dao.existOneObjectByAttr(configName, 0, attrType, attrVal, AioConstants.NODE_1)))
    {
      Integer billType = getParaToInt("billType");
      int unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
      String priceTail = "";
      String discountTail = "";
      if ((billType != null) && (billType.intValue() == AioConstants.BILL_TYPE_JH))
      {
        Boolean purchasePrice = SysConfig.getConfig(configName, 4);
        if (purchasePrice.booleanValue()) {
          priceTail = "purchasePrice";
        }
        Boolean purchaseDiscount = SysConfig.getConfig(configName, 13);
        if (purchaseDiscount.booleanValue()) {
          discountTail = "purchaseDiscount";
        }
      }
      else if ((billType != null) && (billType.intValue() == AioConstants.BILL_TYPE_XS))
      {
        Boolean sellPrice = SysConfig.getConfig(configName, 5);
        if (sellPrice.booleanValue()) {
          priceTail = "sellPrice";
        }
        Boolean sellDiscount = SysConfig.getConfig(configName, 13);
        if (sellDiscount.booleanValue()) {
          discountTail = "sellDiscount";
        }
      }
      if ((StringUtils.isBlank(priceTail)) && (unitId != 0))
      {
        Model unit = Unit.dao.findById(configName, Integer.valueOf(unitId));
        if ((unit != null) && (unit.getInt("fitPrice") != null) && (unit.getInt("fitPrice").intValue() != 0)) {
          if (unit.getInt("fitPrice").intValue() == 1) {
            priceTail = "retailPrice1";
          } else if (unit.getInt("fitPrice").intValue() == 2) {
            priceTail = "defaultPrice11";
          } else if (unit.getInt("fitPrice").intValue() == 3) {
            priceTail = "defaultPrice12";
          } else if (unit.getInt("fitPrice").intValue() == 4) {
            priceTail = "defaultPrice13";
          }
        }
      }
      this.result.put("success", AioConstants.HTTP_RETURN200);
      Model model = Stock.dao.getStockByNum(configName, whichCallBack, basePrivs(PRODUCT_PRIVS), attrType, attrVal, paras, unitId, priceTail, discountTail);
      if (attrType.equals("code")) {
        setSelectUnitId(model, attrVal);
      }
      this.result.put("obj", model);
    }
    else
    {
      this.result.put("success", AioConstants.HTTP_RETURN300);
    }
    this.result.put("whichCallBack", whichCallBack);
    renderJson(this.result);
  }
  
  public void setSelectUnitId(Model model, String attrVal)
  {
    String barCode1 = ((Model)model.get("product")).getStr("barCode1") == null ? "" : ((Model)model.get("product")).getStr("barCode1").toUpperCase();
    String barCode2 = ((Model)model.get("product")).getStr("barCode2") == null ? "" : ((Model)model.get("product")).getStr("barCode2").toUpperCase();
    String barCode3 = ((Model)model.get("product")).getStr("barCode3") == null ? "" : ((Model)model.get("product")).getStr("barCode3").toUpperCase();
    int selectUnitId = model.getInt("selectUnitId").intValue();
    attrVal = attrVal.toUpperCase();
    if (barCode1.indexOf(attrVal) != -1) {
      selectUnitId = 1;
    }
    if (barCode2.indexOf(attrVal) != -1) {
      selectUnitId = 2;
    } else if (barCode3.indexOf(attrVal) != -1) {
      selectUnitId = 3;
    }
    model.put("selectUnitId", Integer.valueOf(selectUnitId));
  }
  
  public void orderDialog()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int pSupId = 0;
    int supId = 0;
    int upObjectId = 0;
    String oldDownSelectObjs = "";
    String newDownSelectObjs = "";
    String opterate = "";
    String term = "";
    String termVal = "";
    int storageId = 0;
    String attrType = "";
    
    String type = getPara(0, "toDialog");
    if (type.equals("toDialog"))
    {
      storageId = getParaToInt(1, Integer.valueOf(0)).intValue();
    }
    else if (type.equals("page"))
    {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
      oldDownSelectObjs = getPara("oldSelectCheckBoxIds", "");
      newDownSelectObjs = getPara("selectCheckBoxIds", "");
    }
    else if (type.equals("search"))
    {
      oldDownSelectObjs = getPara("oldSelectCheckBoxIds", "");
      newDownSelectObjs = getPara("selectCheckBoxIds", "");
      attrType = getPara(1, "");
      termVal = getPara(2, "");
      termVal = URLDecoder.decode(termVal, "UTF-8");
      if ((attrType != "") && (attrType.equals("code"))) {
        term = "code";
      } else if ((attrType != "") && (attrType.equals("fullName"))) {
        term = "fullName";
      }
    }
    else if (type.equals("down"))
    {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
      opterate = getPara(2, "");
      oldDownSelectObjs = getPara(3, "");
      newDownSelectObjs = getPara(4, "");
    }
    else if (type.equals("up"))
    {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
      upObjectId = getParaToInt(2, Integer.valueOf(0)).intValue();
      oldDownSelectObjs = getPara(3, "");
      opterate = getPara(4, "");
      setAttr("opterate", opterate);
      newDownSelectObjs = getPara(5, "");
    }
    if (supId > 0) {
      pSupId = Product.dao.getPsupIdBySupId(configName, supId);
    }
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    if (term.equals(""))
    {
      term = getPara("term");
      termVal = getPara("termVal");
    }
    setAttr("term", term);
    setAttr("termVal", termVal);
    Map<String, Object> map = new HashMap();
    map.put("term", term);
    map.put("termVal", termVal);
    
    String moudle = getPara("moudle", "ProData");
    String handleDate = getPara("handleDate", "");
    if (storageId == 0) {
      storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    }
    map.put("moudle", moudle);
    map.put("handleDate", handleDate);
    map.put("storageId", Integer.valueOf(storageId));
    setAttr("moudle", moudle);
    setAttr("handleDate", handleDate);
    setAttr("storageId", Integer.valueOf(storageId));
    


    Map<String, Object> pageMap = null;
    if (type.equals("toDialog"))
    {
      pageMap = Stock.dao.orderStockSearch(configName, basePrivs(PRODUCT_PRIVS), supId, pageNum, numPerPage, orderField, orderDirection, map);
    }
    else if ((type.equals("search")) || (type.equals("page")) || (type.equals("down")) || (type.equals("up")))
    {
      if ((type.equals("up")) && (upObjectId > 0))
      {
        pageMap = Stock.dao.orderUpPlace(configName, basePrivs(PRODUCT_PRIVS), supId, pageNum, numPerPage, orderField, orderDirection, upObjectId, "", map);
      }
      else if (type.equals("search"))
      {
        pageMap = Stock.dao.orderSearch(configName, basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, orderField, orderDirection, map);
        
        List searchList = (List)pageMap.get("pageList");
        if ((pageMap != null) && (searchList != null) && (searchList.size() > 0))
        {
          Model product = (Model)((Model)searchList.get(0)).get("product");
          supId = Integer.valueOf(product.get("supId").toString()).intValue();
        }
      }
      else if (type.equals("down"))
      {
        pageMap = Stock.dao.orderDown(configName, basePrivs(PRODUCT_PRIVS), supId, pageNum, numPerPage, orderField, orderDirection, map);
      }
      else
      {
        pageMap = Stock.dao.orderStockSearch(configName, basePrivs(PRODUCT_PRIVS), supId, pageNum, numPerPage, orderField, orderDirection, map);
      }
      List<Product> alllist = (List)pageMap.get("listAllIdAndPids");
      List<Model> subBySupIdlist = (List)pageMap.get("listSubIdAndPidsBySupId");
      

      List<String> newSelectList = StringUtil.strsArrToList(newDownSelectObjs.split(","));
      
      Map<String, List<String>> addAndDelAndEqMapList = StringUtil.getAddAndDelAndEqList(oldDownSelectObjs.split(","), newDownSelectObjs.split(","));
      List<String> deleteList = (List)addAndDelAndEqMapList.get("delete");
      if ((deleteList != null) && (alllist != null)) {
        for (int i = 0; i < deleteList.size(); i++)
        {
          String deleteId = (String)deleteList.get(i);
          for (int j = 0; j < alllist.size(); j++)
          {
            String id = ((Product)alllist.get(j)).getInt("id").toString();
            String pids = ((Product)alllist.get(j)).getStr("pids");
            if (deleteId.equals(id))
            {
              pids = pids.substring(1, pids.length() - 1);
              List<String> pidsList = StringUtil.strsArrToList(pids.split("\\}\\{"));
              for (int k = 0; k < pidsList.size(); k++)
              {
                String pidsIds = (String)pidsList.get(k);
                newSelectList.remove(pidsIds);
              }
            }
          }
        }
      }
      if ((newSelectList.contains(String.valueOf(supId))) && (subBySupIdlist != null)) {
        for (int i = 0; i < subBySupIdlist.size(); i++)
        {
          String id = ((Model)subBySupIdlist.get(i)).getInt("id").toString();
          if (!newSelectList.contains(id)) {
            newSelectList.add(id);
          }
        }
      }
      newDownSelectObjs = "";
      for (int i = 0; i < newSelectList.size(); i++) {
        newDownSelectObjs = newDownSelectObjs + "," + (String)newSelectList.get(i);
      }
      newDownSelectObjs = newDownSelectObjs.substring(1, newDownSelectObjs.length());
    }
    if ((attrType.equals("code")) || (attrType.equals("fullName"))) {
      setAttr("term", "");
    }
    if (pageMap != null)
    {
      List<Model> list = (List)pageMap.get("pageList");
      for (int i = 0; i < list.size(); i++)
      {
        Model one = (Model)((Model)list.get(i)).get("product");
        
        BigDecimal amount = ((Model)list.get(i)).getBigDecimal("samount");
        if (amount != null)
        {
          ((Model)((Model)list.get(i)).get("product")).put("helpUnit", DwzUtils.helpAmount(amount.abs(), one.getStr("calculateUnit1"), one.getStr("calculateUnit2"), one.getStr("calculateUnit3"), one.getBigDecimal("unitRelation2"), one.getBigDecimal("unitRelation3")));
          ((Model)((Model)list.get(i)).get("product")).put("samount", amount);
        }
        else
        {
          ((Model)list.get(i)).put("helpUnit", "");
        }
      }
      pageMap.put("pageList", list);
    }
    setAttr("pageMap", pageMap);
    
    setAttr("supId", Integer.valueOf(supId));
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("objectId", Integer.valueOf(upObjectId));
    setAttr("selectCheckboxObjs", newDownSelectObjs);
    if ("".equals(opterate)) {
      opterate = getPara("opterate", "");
    }
    if ("".equals(opterate)) {
      setAttr("opterate", "select");
    } else {
      setAttr("opterate", opterate);
    }
    setAttr("aimTabId", getPara("aimTabId", ""));
    setAttr("aimUrl", getPara("aimUrl", ""));
    setAttr("aimTitle", getPara("aimTitle", ""));
    setAttr("billType", getParaToInt("billType"));
    setAttr("unitId", getParaToInt("unitId"));
    

    String btnPattern = getPara("btnPattern", "optionAdd");
    setAttr("btnPattern", btnPattern);
    

    columnConfig("zj100");
    render("productOption.html");
  }
  
  public void orderDialogEdit()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int selectId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int selectSupId = getParaToInt(1, Integer.valueOf(0)).intValue();
    int storageId = getParaToInt(2, Integer.valueOf(0)).intValue();
    

    Map<String, Object> paras = new HashMap();
    String moudle = getPara("moudle", "ProData");
    String handleDate = getPara("handleDate", "");
    if (storageId == 0) {
      storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    }
    paras.put("moudle", moudle);
    paras.put("handleDate", handleDate);
    paras.put("storageId", Integer.valueOf(storageId));
    setAttr("moudle", moudle);
    setAttr("handleDate", handleDate);
    setAttr("storageId", Integer.valueOf(storageId));
    

    Map<String, Object> map = Stock.dao.orderStockPrdSelectEdit(configName, basePrivs(PRODUCT_PRIVS), selectSupId, selectId, AioConstants.START_PAGE, 10, null, null, paras);
    if (map != null)
    {
      List<Model> list = (List)map.get("pageList");
      for (int i = 0; i < list.size(); i++)
      {
        Model one = (Model)((Model)list.get(i)).get("product");
        
        BigDecimal amount = ((Model)list.get(i)).getBigDecimal("samount");
        if (amount != null)
        {
          ((Model)((Model)list.get(i)).get("product")).put("helpUnit", DwzUtils.helpAmount(amount.abs(), one.getStr("calculateUnit1"), one.getStr("calculateUnit2"), one.getStr("calculateUnit3"), one.getBigDecimal("unitRelation2"), one.getBigDecimal("unitRelation3")));
          ((Model)((Model)list.get(i)).get("product")).put("samount", amount);
        }
        else
        {
          ((Model)list.get(i)).put("helpUnit", "");
        }
      }
      map.put("pageList", list);
    }
    setAttr("pageMap", map);
    setAttr("objectId", Integer.valueOf(selectId));
    setAttr("opterate", "edit");
    
    Integer billType = getParaToInt("billType");
    int unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
    setAttr("billType", billType);
    setAttr("unitId", Integer.valueOf(unitId));
    if (selectSupId == 0)
    {
      setAttr("supId", Integer.valueOf(0));
      setAttr("pSupId", Integer.valueOf(0));
    }
    else
    {
      setAttr("supId", Integer.valueOf(selectSupId));
      Model model = Product.dao.findById(configName, Integer.valueOf(selectSupId));
      setAttr("pSupId", model.getInt("supId"));
    }
    columnConfig("zj100");
    render("productOption.html");
  }
  
  public void searchBack()
  {
    String configName = loginConfigName();
    String newDownSelectObjs = getPara(0, "");
    String whichCallBack = getPara(1, "");
    Integer storageId = getParaToInt("storageId", Integer.valueOf(0));
    Integer billType = getParaToInt("billType");
    int unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
    String term = getPara("term", "");
    String termVal = getPara("termVal", "");
    

    String priceTail = "";
    String discountTail = "";
    if ((billType != null) && (billType.intValue() == AioConstants.BILL_TYPE_JH))
    {
      Boolean purchasePrice = SysConfig.getConfig(configName, 4);
      if (purchasePrice.booleanValue()) {
        priceTail = "purchasePrice";
      }
      Boolean purchaseDiscount = SysConfig.getConfig(configName, 13);
      if (purchaseDiscount.booleanValue()) {
        discountTail = "purchaseDiscount";
      }
    }
    else if ((billType != null) && (billType.intValue() == AioConstants.BILL_TYPE_XS))
    {
      Boolean sellPrice = SysConfig.getConfig(configName, 5);
      if (sellPrice.booleanValue()) {
        priceTail = "sellPrice";
      }
      Boolean sellDiscount = SysConfig.getConfig(configName, 14);
      if (sellDiscount.booleanValue()) {
        discountTail = "sellDiscount";
      }
    }
    Model unit = Unit.dao.findById(configName, Integer.valueOf(unitId));
    if ((StringUtils.isBlank(priceTail)) && (unitId != 0)) {
      if ((unit != null) && (unit.getInt("fitPrice") != null) && (unit.getInt("fitPrice").intValue() != 0)) {
        if (unit.getInt("fitPrice").intValue() == 1) {
          priceTail = "retailPrice1";
        } else if (unit.getInt("fitPrice").intValue() == 2) {
          priceTail = "defaultPrice11";
        } else if (unit.getInt("fitPrice").intValue() == 3) {
          priceTail = "defaultPrice12";
        } else if (unit.getInt("fitPrice").intValue() == 4) {
          priceTail = "defaultPrice13";
        }
      }
    }
    List<Model> list = Stock.dao.searchBack(configName, whichCallBack, basePrivs(PRODUCT_PRIVS), newDownSelectObjs, storageId, unitId, priceTail, discountTail);
    if ((unit != null) && (unit.getInt("fitPrice") != null) && (unit.getInt("fitPrice").intValue() != 0)) {
      for (Model model : list) {
        if ((("purchasePrice".equals(priceTail)) || ("sellPrice".equals(priceTail))) && (!BigDecimalUtils.notNullZero(model.getBigDecimal("price"))))
        {
          Model product = (Model)model.get("product");
          if (unit.getInt("fitPrice").intValue() == 1)
          {
            if (product != null) {
              model.put("price", product.get("retailPrice1"));
            }
          }
          else if (unit.getInt("fitPrice").intValue() == 2)
          {
            if (product != null) {
              model.put("price", product.get("defaultPrice11"));
            }
          }
          else if (unit.getInt("fitPrice").intValue() == 3)
          {
            if (product != null) {
              model.put("price", product.get("defaultPrice12"));
            }
          }
          else if ((unit.getInt("fitPrice").intValue() == 4) && 
            (product != null)) {
            model.put("price", product.get("defaultPrice13"));
          }
        }
      }
    }
    if ((term.equals("code")) && (list != null)) {
      for (int i = 0; i < list.size(); i++)
      {
        Model model = (Model)list.get(i);
        setSelectUnitId(model, termVal);
      }
    }
    Map<String, Object> obj = new HashMap();
    obj.put("whichCallBack", whichCallBack);
    obj.put("list", list);
    renderJson(obj);
  }
  
  public void storageOneRecord()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String whichCallBack = getPara(0, "");
    int productId = getParaToInt(1, Integer.valueOf(0)).intValue();
    String attrType = getPara(2, "");
    String code = getPara(3, "");
    code = URLDecoder.decode(code, "UTF-8");
    if ((!code.equals("")) && (Storage.dao.existObjectByIdCodeNode(configName, 0, attrType, code, AioConstants.NODE_1)))
    {
      this.result.put("success", AioConstants.HTTP_RETURN200);
      
      this.result.put("obj", Stock.dao.getStorageByNum(configName, attrType, productId, code));
    }
    else
    {
      this.result.put("success", AioConstants.HTTP_RETURN300);
    }
    this.result.put("whichCallBack", whichCallBack);
    renderJson(this.result);
  }
  
  public void storageDialog()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int pSupId = 0;
    int supId = 0;
    int upObjectId = 0;
    int productId = 0;
    String hasCheck = "";
    String term = "";
    String termVal = "";
    
    productId = getParaToInt("productId", Integer.valueOf(0)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    
    String caseVal = getPara("case", "");
    String type = getPara(0, "toDialog");
    if (type.equals("toDialog"))
    {
      productId = getParaToInt(1, Integer.valueOf(0)).intValue();
    }
    else if (type.equals("page"))
    {
      hasCheck = getPara("hasCheck", "all");
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
    }
    else if (type.equals("search"))
    {
      hasCheck = getPara("hasCheck", "all");
      String attrType = getPara(1, "");
      termVal = getPara(2, "");
      termVal = URLDecoder.decode(termVal, "UTF-8");
      if ((attrType != "") && (attrType.equals("code"))) {
        term = "code";
      } else if ((attrType != "") && (attrType.equals("fullName"))) {
        term = "fullName";
      }
    }
    else if (type.equals("down"))
    {
      productId = getParaToInt(1, Integer.valueOf(0)).intValue();
      supId = getParaToInt(2, Integer.valueOf(0)).intValue();
    }
    else if (type.equals("up"))
    {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
      upObjectId = getParaToInt(2, Integer.valueOf(0)).intValue();
      productId = getParaToInt(3, Integer.valueOf(0)).intValue();
      if (supId > 0) {
        pSupId = Product.dao.getPsupIdBySupId(configName, supId);
      }
    }
    else if (type.equals("hasProductStockShow"))
    {
      hasCheck = getPara(1, "");
      pageNum = AioConstants.START_PAGE;
      numPerPage = 10;
    }
    if (term.equals(""))
    {
      term = getPara("term");
      termVal = getPara("termVal");
    }
    setAttr("term", term);
    setAttr("termVal", termVal);
    Map<String, Object> map = new HashMap();
    if ((termVal != null) && (!termVal.equals("")))
    {
      map.put("term", term);
      map.put("termVal", termVal);
    }
    Map<String, Object> pageMap = null;
    if (type.equals("toDialog"))
    {
      pageMap = Stock.dao.storageAllPrdStockSearch(configName, "page", productId, supId, pageNum, numPerPage, orderField, orderDirection, map);
    }
    else if (type.equals("hasProductStockShow"))
    {
      if (hasCheck.equals("all")) {
        pageMap = Stock.dao.storageAllPrdStockSearch(configName, "page", productId, supId, pageNum, numPerPage, orderField, orderDirection, map);
      } else if (hasCheck.equals("has")) {
        pageMap = Stock.dao.storageHasPrdStockSearch(configName, productId, pageNum, numPerPage, orderField, orderDirection, map);
      }
    }
    else if (type.equals("search"))
    {
      if (hasCheck.equals("all")) {
        pageMap = Stock.dao.storageAllPrdStockSearch(configName, "search", productId, 0, pageNum, numPerPage, orderField, orderDirection, map);
      } else if (hasCheck.equals("has")) {
        pageMap = Stock.dao.storageHasPrdStockSearch(configName, productId, pageNum, numPerPage, orderField, orderDirection, map);
      }
    }
    else if (type.equals("page"))
    {
      if (hasCheck.equals("all")) {
        pageMap = Stock.dao.storageAllPrdStockSearch(configName, "page", productId, supId, pageNum, numPerPage, orderField, orderDirection, map);
      } else if (hasCheck.equals("has")) {
        pageMap = Stock.dao.storageHasPrdStockSearch(configName, productId, pageNum, numPerPage, orderField, orderDirection, map);
      }
    }
    else if (type.equals("up"))
    {
      caseVal = getPara(4, "");
      pageMap = Stock.dao.storageUpPlace(configName, productId, supId, pageNum, numPerPage, orderField, orderDirection, upObjectId, map);
    }
    else if (type.equals("down"))
    {
      caseVal = getPara(3, "");
      pageMap = Stock.dao.storageDown(configName, productId, supId, pageNum, numPerPage, orderField, orderDirection, map);
    }
    if (pageMap != null)
    {
      List<Model> list = (List)pageMap.get("pageList");
      for (int i = 0; i < list.size(); i++)
      {
        Model one = (Model)((Model)list.get(i)).get("product");
        BigDecimal amount = ((Model)list.get(i)).getBigDecimal("samount");
        if (amount != null) {
          ((Model)list.get(i)).put("helpUnit", DwzUtils.helpAmount(amount, one.getStr("calculateUnit1"), one.getStr("calculateUnit2"), one.getStr("calculateUnit3"), one.getBigDecimal("unitRelation2"), one.getBigDecimal("unitRelation2")));
        } else {
          ((Model)list.get(i)).put("helpUnit", "");
        }
      }
      pageMap.put("pageList", list);
    }
    setAttr("pageMap", pageMap);
    

    setAttr("supId", Integer.valueOf(supId));
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("objectId", Integer.valueOf(upObjectId));
    setAttr("productId", Integer.valueOf(productId));
    setAttr("hasCheck", hasCheck);
    setAttr("case", caseVal);
    render("storageOption.html");
  }
  
  public void storageDialogEdit()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int productId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int selectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    int selectSupId = getParaToInt(2, Integer.valueOf(0)).intValue();
    
    Map<String, Object> map = Stock.dao.storageSelectEdit(configName, productId, selectSupId, selectId, AioConstants.START_PAGE, 10, null, null, null);
    setAttr("pageMap", map);
    setAttr("objectId", Integer.valueOf(selectId));
    if (selectSupId == 0)
    {
      setAttr("supId", Integer.valueOf(0));
      setAttr("pSupId", Integer.valueOf(0));
    }
    else
    {
      setAttr("supId", Integer.valueOf(selectSupId));
      Model model = Storage.dao.findById(configName, Integer.valueOf(selectSupId));
      setAttr("pSupId", model.getInt("supId"));
    }
    setAttr("productId", Integer.valueOf(productId));
    setAttr("case", getPara("case", ""));
    setAttr("hasCheck", "all");
    render("storageOption.html");
  }
  
  public void storageSearchBack()
  {
    String configName = loginConfigName();
    int storageId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String whichCallBack = getPara(1, "");
    int productId = getParaToInt(2, Integer.valueOf(0)).intValue();
    Record storage = Stock.dao.storageSearchBack(configName, productId, storageId);
    Map<String, Object> obj = new HashMap();
    obj.put("whichCallBack", whichCallBack);
    obj.put("storage", storage);
    obj.put("case", getPara(3, ""));
    renderJson(obj);
  }
  
  public void manSelPrdSotock()
  {
    String configName = loginConfigName();
    int storageId = getParaToInt("param[storageId]", Integer.valueOf(0)).intValue();
    int productId = getParaToInt("param[productId]", Integer.valueOf(0)).intValue();
    String trIndex = getPara("param[trIndex]", "");
    String trIndexs = getPara("param[trIndexs]", "");
    String productIds = getPara("param[productIds]", "");
    String trcostAriths = getPara("param[trcostAriths]", "");
    String tbodyId = getPara("param[tbodyId]", "");
    
    List<Model> stockList = Stock.dao.getStockGroupAttr(configName, Integer.valueOf(storageId), Integer.valueOf(productId));
    if ((stockList != null) && (stockList.size() > 0)) {
      for (int i = 0; i < stockList.size(); i++)
      {
        BigDecimal money = BigDecimalUtils.mul(((Model)stockList.get(i)).getBigDecimal("amount"), ((Model)stockList.get(i)).getBigDecimal("costPrice"));
        ((Model)stockList.get(i)).put("money", money);
      }
    }
    setAttr("productFullName", Product.dao.findById(configName, Integer.valueOf(productId)).get("fullName"));
    if (storageId == 0) {
      setAttr("storageFullName", "全部仓库");
    } else {
      setAttr("storageFullName", Storage.dao.findById(configName, Integer.valueOf(storageId)).get("fullName"));
    }
    String module = getPara("param[module]", "");
    setAttr("module", module);
    

    setAttr("currentIndexTr", trIndex);
    setAttr("trIndexs", trIndexs);
    setAttr("productIds", productIds);
    setAttr("trcostAriths", trcostAriths);
    setAttr("tbodyId", tbodyId);
    setAttr("stockList", stockList);
    render("manSelPrdStockOption.html");
  }
  
  public void avgProPice()
  {
    String configName = loginConfigName();
    int storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    int productId = getParaToInt("productId", Integer.valueOf(0)).intValue();
    int costArith = getParaToInt("costArith", Integer.valueOf(1)).intValue();
    BigDecimal avgPrice = BigDecimal.ZERO;
    if (costArith == 1) {
      avgPrice = Stock.dao.getProAvgPriceBySgeIdAndProIds(configName, Integer.valueOf(storageId), Integer.valueOf(productId));
    }
    setAttr("avgPrice", avgPrice);
    renderJson();
  }
  
  public void lastBuyPrice()
  {
    String configName = loginConfigName();
    int productId = getParaToInt("productId", Integer.valueOf(0)).intValue();
    int selectUnitId = getParaToInt("selectUnitId", Integer.valueOf(0)).intValue();
    BigDecimal lastBuyPrice = null;
    lastBuyPrice = Stock.dao.getProBuyPriceBySgeIdAndProIds(configName, Integer.valueOf(productId), Integer.valueOf(selectUnitId));
    setAttr("lastBuyPrice", lastBuyPrice);
    renderJson();
  }
  
  public void avgPiceOrLastBugPriceBySelectUnitId()
  {
    String configName = loginConfigName();
    int storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    int productId = getParaToInt("productId", Integer.valueOf(0)).intValue();
    int costArith = getParaToInt("costArith", Integer.valueOf(1)).intValue();
    int selectUnitId = getParaToInt("selectUnitId", Integer.valueOf(0)).intValue();
    BigDecimal price = BigDecimal.ZERO;
    if (costArith == AioConstants.PRD_COST_PRICE1) {
      if (storageId == 0)
      {
        price = Stock.dao.getProBuyPriceBySgeIdAndProIds(configName, Integer.valueOf(productId), Integer.valueOf(selectUnitId));
      }
      else
      {
        price = Stock.dao.getProAvgPriceBySgeIdAndProIds2(configName, Integer.valueOf(storageId), Integer.valueOf(productId), selectUnitId);
        if (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0) {
          price = Stock.dao.getProBuyPriceBySgeIdAndProIds(configName, Integer.valueOf(productId), Integer.valueOf(selectUnitId));
        }
      }
    }
    setAttr("avgPrice", price);
    renderJson();
  }
  
  public void getManyProSckAmount()
  {
    String configName = loginConfigName();
    int storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    String productIds = getPara("productIds");
    List<Record> list = Avgprice.dao.getManyProSckAmount(configName, Integer.valueOf(storageId), productIds);
    setAttr("list", list);
    renderJson();
  }
  
  public void getManyProSck()
  {
    String configName = loginConfigName();
    int storageId = getParaToInt("storageId", Integer.valueOf(0)).intValue();
    String productIds = getPara("productIds");
    List<Record> list = Avgprice.dao.getManyProSck(configName, Integer.valueOf(storageId), productIds);
    setAttr("list", list);
    renderJson();
  }
  
  public void allowBarterMoney()
  {
    String configName = loginConfigName();
    int unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
    String recodeTime = getPara("recodeTime");
    String type = getPara("type");
    BigDecimal money = BigDecimal.ZERO;
    money = PurchaseBarterBill.getAllowBarterMoney(configName, unitId, recodeTime, type);
    setAttr("money", money);
    renderJson();
  }
  
  public void getOrderByUnit()
  {
    String configName = loginConfigName();
    int unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
    boolean isReload = getParaToBoolean("isReload", Boolean.valueOf(false)).booleanValue();
    String whichCallBack = getPara("whichCallBack");
    int billId = getParaToInt("billId", Integer.valueOf(0)).intValue();
    boolean isDraft = getParaToBoolean("isDraft", Boolean.valueOf(false)).booleanValue();
    List<Record> list = null;
    String tableName = "cw_pay_by_order";
    if (isDraft) {
      tableName = "cw_pay_by_draft_order";
    }
    if ("getMoney".equals(whichCallBack))
    {
      if (isReload) {
        GetMoneyBill.dao.deleteBeforeCwOrder(configName, tableName, isDraft, unitId, AioConstants.BILL_ROW_TYPE17, billId);
      }
      list = GetMoneyBill.dao.getOrderByUnit(configName, unitId);
    }
    else if ("payMoney".equals(whichCallBack))
    {
      if (isReload) {
        GetMoneyBill.dao.deleteBeforeCwOrder(configName, tableName, isDraft, unitId, AioConstants.BILL_ROW_TYPE19, billId);
      }
      list = PayMoneyBill.dao.getOrderByUnit(configName, unitId);
    }
    setAttr("list", list);
    renderJson();
  }
  
  public void getProductPrics()
  {
    String configName = loginConfigName();
    int selectUnitId = getParaToInt("selectUnitId", Integer.valueOf(1)).intValue();
    Integer storageId = getParaToInt("storageId");
    int productId = getParaToInt("productId", Integer.valueOf(0)).intValue();
    Model product = Product.dao.findById(configName, Integer.valueOf(productId));
    
    ProductUnit productUnit = ProductUnit.dao.getObj(configName, productId, selectUnitId);
    
    PriceDiscountTrack priceDiscount = PriceDiscountTrack.dao.getRecentlyObj(configName, productId, selectUnitId);
    Avgprice avgprice = Avgprice.dao.getAvgprice(configName, storageId, Integer.valueOf(productId));
    BigDecimal stockAvgprice = BigDecimal.ZERO;
    if (avgprice != null) {
      stockAvgprice = avgprice.getBigDecimal("avgPrice");
    }
    stockAvgprice = DwzUtils.getConversionPrice(stockAvgprice, Integer.valueOf(1), product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(selectUnitId));
    
    setAttr("stockAvgprice", stockAvgprice);
    setAttr("priceDiscount", priceDiscount);
    setAttr("productUnit", productUnit);
    render("pricesOption.html");
  }
  
  public static void stockSubChange(String configName, int costArith, BigDecimal amouont, int storageId, int productId, BigDecimal price, String batch, Date produceDate, Date produceEndDate, Model preStock)
  {
    Model stock = null;
    if (costArith == AioConstants.PRD_COST_PRICE1)
    {
      stock = Stock.dao.getStockFastInFastOut(configName, Integer.valueOf(storageId), Integer.valueOf(productId), "");
    }
    else if (costArith == AioConstants.PRD_COST_PRICE4)
    {
      stock = Stock.dao.getObj(configName, Integer.valueOf(productId), Integer.valueOf(storageId), batch, price, produceDate, produceEndDate);
      if (BigDecimalUtils.compare(stock.getBigDecimal("amount"), amouont) == 0) {
        stock.delete(configName);
      } else {
        stock.set("amount", BigDecimalUtils.sub(stock.getBigDecimal("amount"), amouont)).update(configName);
      }
      return;
    }
    if (stock != null)
    {
      BigDecimal samount = stock.getBigDecimal("amount");
      if (BigDecimalUtils.compare(samount, amouont) == 1)
      {
        BigDecimal allMoney = BigDecimalUtils.mul(stock.getBigDecimal("amount"), stock.getBigDecimal("costPrice"));
        BigDecimal subMoney = BigDecimalUtils.mul(amouont, price);
        stock.set("amount", BigDecimalUtils.sub(samount, amouont));
        stock.set("costPrice", BigDecimalUtils.div(BigDecimalUtils.sub(allMoney, subMoney), stock.getBigDecimal("amount")));
        stock.update(configName);
        return;
      }
      if (BigDecimalUtils.compare(samount, amouont) == 0)
      {
        stock.set("amount", BigDecimal.ZERO).delete(configName);
        return;
      }
      stock.set("amount", BigDecimal.ZERO).delete(configName);
      stockSubChange(configName, costArith, BigDecimalUtils.sub(amouont, samount), storageId, productId, price, batch, produceDate, produceEndDate, null);
      return;
    }
    if ((stock == null) && (preStock != null)) {
      preStock.set("amount", BigDecimalUtils.sub(BigDecimal.ZERO, amouont)).update(configName);
    } else if ((stock == null) && (preStock == null) && 
      (costArith == AioConstants.PRD_COST_PRICE1)) {
      Stock.dao.notStockAndAddTypeProduct(configName, storageId, productId, BigDecimalUtils.sub(BigDecimal.ZERO, amouont), price, batch, produceDate, produceEndDate);
    }
  }
  
  @Before({Tx.class})
  public static void stockChange(String configName, String type, int storageId, int productId, int costArith, BigDecimal baseAmount, BigDecimal basePrice, String batch, Date produceDate, Date produceEndDate)
  {
    Date date = new Date();
    Timestamp time = new Timestamp(date.getTime());
    Stock stock = null;
    if ("in".equals(type))
    {
      if (costArith == AioConstants.PRD_COST_PRICE1)
      {
        stock = Stock.dao.getStock(configName, Integer.valueOf(productId), Integer.valueOf(storageId));
        if (stock == null)
        {
          stock = new Stock();
          stock.set("storageId", Integer.valueOf(storageId));
          stock.set("productId", Integer.valueOf(productId));
          stock.set("amount", baseAmount);
          stock.set("costPrice", basePrice);
          stock.set("updateTime", time);
          stock.save(configName);
        }
        else
        {
          BigDecimal newMoney = BigDecimalUtils.mul(baseAmount, basePrice);
          BigDecimal sckAmount = stock.getBigDecimal("amount");
          BigDecimal sckMoney = BigDecimalUtils.mul(sckAmount, stock.getBigDecimal("costPrice"));
          BigDecimal addmoney = BigDecimalUtils.add(newMoney, sckMoney);
          BigDecimal addAmount = BigDecimalUtils.add(baseAmount, sckAmount);
          if (BigDecimalUtils.compare(addAmount, BigDecimal.ZERO) == 0)
          {
            stock.delete(configName);
          }
          else
          {
            stock.set("amount", addAmount);
            stock.set("costPrice", BigDecimalUtils.div(addmoney, addAmount));
            stock.set("updateTime", time);
            stock.update(configName);
          }
        }
      }
      else if (costArith == AioConstants.PRD_COST_PRICE4)
      {
        stock = (Stock)Stock.dao.getObj(configName, Integer.valueOf(productId), Integer.valueOf(storageId), batch, basePrice, produceDate, produceEndDate);
        if (stock != null)
        {
          stock.set("updateTime", time);
          stock.set("amount", BigDecimalUtils.add(stock.getBigDecimal("amount"), baseAmount));
          stock.update(configName);
        }
        else
        {
          stock = new Stock();
          stock.set("productId", Integer.valueOf(productId));
          stock.set("amount", baseAmount);
          stock.set("updateTime", time);
          stock.set("batchNum", batch);
          stock.set("storageId", Integer.valueOf(storageId));
          stock.set("produceDate", produceDate);
          stock.set("produceEndDate", produceEndDate);
          stock.set("costPrice", basePrice);
          stock.save(configName);
        }
      }
      else
      {
        stock = new Stock();
        stock.set("productId", Integer.valueOf(productId));
        stock.set("amount", baseAmount);
        stock.set("updateTime", time);
        stock.set("batchNum", batch);
        stock.set("storageId", Integer.valueOf(storageId));
        stock.set("produceDate", produceDate);
        stock.set("produceEndDate", produceEndDate);
        stock.set("costPrice", basePrice);
        stock.save(configName);
      }
    }
    else if ("out".equals(type)) {
      stockSubChange(configName, costArith, baseAmount, storageId, productId, basePrice, batch, produceDate, produceEndDate, null);
    }
  }
  
  public static BigDecimal stockChangeSubYDPrice(String configName, int storageId, int productId, int costArith, BigDecimal baseAmount, BigDecimal basePrice)
  {
    Stock stock = Stock.dao.getStock(configName, Integer.valueOf(productId), Integer.valueOf(storageId));
    BigDecimal price = basePrice;
    if ((costArith == AioConstants.PRD_COST_PRICE1) && (stock != null))
    {
      BigDecimal newMoney = BigDecimalUtils.mul(baseAmount, basePrice);
      BigDecimal sckAmount = stock.getBigDecimal("amount");
      BigDecimal sckMoney = BigDecimalUtils.mul(sckAmount, stock.getBigDecimal("costPrice"));
      BigDecimal submoney = BigDecimalUtils.sub(sckMoney, newMoney);
      BigDecimal subAmount = BigDecimalUtils.sub(sckAmount, baseAmount);
      price = BigDecimalUtils.div(submoney, subAmount);
      stock.set("costPrice", price);
      
      stock.update(configName);
    }
    return price;
  }
  
  public static BigDecimal subMovingAverage(String configName, int storageId, int productId)
  {
    Record stock = Db.use(configName).findFirst("select avgPrice from zj_product_avgprice zj where zj.storageId=? and zj.productId=? ", new Object[] { Integer.valueOf(storageId), Integer.valueOf(productId) });
    BigDecimal litterPrice = BigDecimal.ZERO;
    if (stock != null) {
      litterPrice = stock.getBigDecimal("avgPrice");
    }
    return litterPrice;
  }
  
  public static BigDecimal subPeopleOperater(String configName, BigDecimal curPrdAmount, int storageId, int productId, BigDecimal costPrice, String batchNum, Date produceDate, Date produceEndDate)
  {
    StringBuffer sb = new StringBuffer("select costPrice from cc_stock where storageId =? and productId =?");
    if (costPrice == null) {
      sb.append(" and costPrice is null");
    } else {
      sb.append(" and costPrice =" + costPrice);
    }
    if (batchNum == null) {
      sb.append(" and batchNum is null");
    } else {
      sb.append(" and batchNum ='" + batchNum + "'");
    }
    if (produceDate == null) {
      sb.append(" and produceDate is null");
    } else {
      sb.append(" and produceDate ='" + produceDate + "'");
    }
    if (produceEndDate == null) {
      sb.append(" and produceEndDate is null");
    } else {
      sb.append(" and produceEndDate ='" + produceEndDate + "'");
    }
    Record stock = Db.use(configName).findFirst(sb.toString(), new Object[] { Integer.valueOf(storageId), Integer.valueOf(productId) });
    if (stock == null) {
      return BigDecimal.ZERO;
    }
    return BigDecimalUtils.mul(stock.getBigDecimal("costPrice"), curPrdAmount);
  }
  
  public static BigDecimal subAvgPrice(String configName, Model detail, Integer costArith, BigDecimal litterAmount, BigDecimal costPrice, int curStorageId, int productIdi)
  {
    BigDecimal avgCostPrice = BigDecimal.ZERO;
    BigDecimal costMoneys = BigDecimal.ZERO;
    if (AioConstants.PRD_COST_PRICE1 == costArith.intValue())
    {
      if (BigDecimalUtils.compare(costPrice, BigDecimal.ZERO) == 1) {
        avgCostPrice = costPrice;
      } else {
        avgCostPrice = subMovingAverage(configName, curStorageId, productIdi);
      }
      return avgCostPrice;
    }
    if (AioConstants.PRD_COST_PRICE4 == costArith.intValue())
    {
      String batchNum = detail.getStr("batch");
      Date produceDate = detail.getDate("produceDate");
      Date produceEndDate = detail.getDate("produceEndDate");
      costMoneys = subPeopleOperater(configName, litterAmount, curStorageId, productIdi, costPrice, batchNum, produceDate, produceEndDate);
    }
    avgCostPrice = BigDecimalUtils.div(costMoneys, litterAmount);
    return avgCostPrice;
  }
}
