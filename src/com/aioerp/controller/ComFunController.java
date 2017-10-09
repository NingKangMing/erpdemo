package com.aioerp.controller;

import com.aioerp.common.AioConstants;
import com.aioerp.model.StockComfirmUtil;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Avgprice;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.finance.PayType;
import com.aioerp.model.fz.MinSellPrice;
import com.aioerp.model.fz.PriceDiscountTrack;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.supadmin.WhichDb;
import com.aioerp.model.sys.BillType;
import com.aioerp.port.common.PortConstants;
import com.aioerp.port.controller.om.OmPortController;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class ComFunController
{
  public static Record isOnlyOne(String configName, String tableName, Integer status)
  {
    String sql = "select count(*) from " + tableName + " obj";
    String where = "";
    if ((status != null) && (status.intValue() != 0)) {
      where = where + " where obj.status=" + status;
    }
    long count = Db.use(configName).queryLong(sql + where).longValue();
    if (count == 1L) {
      return Db.use(configName).findFirst("select * from " + tableName + " obj " + where);
    }
    return null;
  }
  
  public static Object getTableAttrById(String configName, String tableName, int id, String attr)
  {
    Object str = null;
    if (id == 0)
    {
      str = "";
    }
    else
    {
      Record r = Db.use(configName).findById(tableName, Integer.valueOf(id));
      if (r == null) {
        str = "";
      } else {
        str = r.get(attr);
      }
    }
    return str;
  }
  
  public static String getCostArithName(int costArith)
  {
    if (costArith == AioConstants.PRD_COST_PRICE1) {
      return "移动加权平均";
    }
    if (costArith == AioConstants.PRD_COST_PRICE4) {
      return "手工指定法";
    }
    return "";
  }
  
  public static List<Model> productMerge(List<Model> detail, Map<Integer, Product> productMap)
  {
    for (int i = 0; i < detail.size(); i++)
    {
      Model sellDetaili = (Model)detail.get(i);
      int productIdi = sellDetaili.getInt("productId").intValue();
      Product producti = (Product)productMap.get(Integer.valueOf(productIdi));
      int storageIdi = sellDetaili.getInt("storageId").intValue();
      String batchi = sellDetaili.getStr("batch");
      Date produceDatei = sellDetaili.getDate("produceDate");
      Date produceEndDatei = sellDetaili.getDate("produceEndDate");
      int selectUintIdi = sellDetaili.getInt("selectUnitId").intValue();
      for (int j = i + 1; j < detail.size(); j++)
      {
        Model sellDetailj = (Model)detail.get(j);
        int productIdj = sellDetailj.getInt("productId").intValue();
        int storageIdj = sellDetailj.getInt("storageId").intValue();
        String batchj = sellDetailj.getStr("batch");
        Date produceDatej = sellDetailj.getDate("produceDate");
        Date produceEndDatej = sellDetailj.getDate("produceEndDate");
        int selectUintIdj = sellDetailj.getInt("selectUnitId").intValue();
        if (producti.getInt("costArith").intValue() == AioConstants.PRD_COST_PRICE4)
        {
          if ((productIdi == productIdj) && (storageIdi == storageIdj) && (((batchi == null) && (batchj == null)) || ((batchi != null) && (batchi.equals(batchj))) || ((batchj != null) && (batchj.equals(batchi)) && (DateUtils.hasEqual(produceDatei, produceDatej)) && (DateUtils.hasEqual(produceEndDatei, produceEndDatej)) && (selectUintIdi == selectUintIdj))))
          {
            BigDecimal litterAmounti = DwzUtils.getConversionAmount(sellDetaili.getBigDecimal("amount"), sellDetaili.getInt("selectUnitId"), producti, Integer.valueOf(1));
            BigDecimal litterAmountj = DwzUtils.getConversionAmount(sellDetailj.getBigDecimal("amount"), sellDetailj.getInt("selectUnitId"), producti, Integer.valueOf(1));
            BigDecimal amounti = BigDecimalUtils.add(litterAmounti, litterAmountj);
            String trIndex = sellDetaili.getStr("trIndex") + "," + sellDetailj.getStr("trIndex");
            ((Model)detail.get(i)).put("trIndex", trIndex);
            ((Model)detail.get(i)).set("baseAmount", amounti);
            detail.remove(j);
            i--;
            break;
          }
        }
        else if ((productIdi == productIdj) && (storageIdi == storageIdj))
        {
          BigDecimal litterAmounti = DwzUtils.getConversionAmount(sellDetaili.getBigDecimal("amount"), sellDetaili.getInt("selectUnitId"), producti, Integer.valueOf(1));
          BigDecimal litterAmountj = DwzUtils.getConversionAmount(sellDetailj.getBigDecimal("amount"), sellDetailj.getInt("selectUnitId"), producti, Integer.valueOf(1));
          BigDecimal amounti = BigDecimalUtils.add(litterAmounti, litterAmountj);
          String trIndex = sellDetaili.getStr("trIndex") + "," + sellDetailj.getStr("trIndex");
          ((Model)detail.get(i)).put("trIndex", trIndex);
          ((Model)detail.get(i)).set("baseAmount", amounti);
          detail.remove(j);
          i--;
          break;
        }
      }
    }
    return detail;
  }
  
  public static void setRetailPrice(List<String> rowData, Model detail, Product product)
  {
    if (BigDecimalUtils.compare(detail.getBigDecimal("retailPrice"), BigDecimal.ZERO) == 0)
    {
      Integer selectUnitId = detail.getInt("selectUnitId");
      if (selectUnitId == null) {
        selectUnitId = Integer.valueOf(0);
      }
      BigDecimal retailPrice = product.getBigDecimal("retailPrice1");
      if (selectUnitId.intValue() == 2) {
        retailPrice = product.getBigDecimal("retailPrice2");
      } else if (selectUnitId.intValue() == 2) {
        retailPrice = product.getBigDecimal("retailPrice3");
      }
      rowData.add(BaseController.trimNull(retailPrice));
      rowData.add(BaseController.trimNull(BigDecimalUtils.mul(detail.getBigDecimal("amount"), retailPrice)));
    }
    else
    {
      rowData.add(BaseController.trimNull(detail.getBigDecimal("retailPrice")));
      rowData.add(BaseController.trimNull(detail.getBigDecimal("retailMoney")));
    }
  }
  
  public static void orderFuJian(String configName, String orderFuJianIds, int billTypeId, int billId, int draft)
  {
    if (orderFuJianIds == null) {
      orderFuJianIds = "";
    }
    if (!orderFuJianIds.equals(""))
    {
      StringBuffer sb = new StringBuffer("update aioerp_file set recordId=" + billId + ",isDraft=" + draft + " where tableId=" + billTypeId);
      sb.append(" and id in(" + orderFuJianIds + ")");
      Db.use(configName).update(sb.toString());
    }
  }
  
  public static Map<String, String> billPayTypeAttr(String configName, String type, int billTypeId, int billId)
  {
    Map<String, String> map = new HashMap();
    if ((type == null) || (type.equals(""))) {
      type = "saveBill";
    }
    List<Model> payList = null;
    if (type.equals("saveBill")) {
      payList = PayType.dao.getListByBillId(configName, Integer.valueOf(billId), Integer.valueOf(billTypeId));
    } else if (type.equals("draftBill")) {
      payList = PayDraft.dao.getListByBillId(configName, Integer.valueOf(billId), Integer.valueOf(billTypeId));
    }
    String payTypeIdStrs = "";
    String payTypeMoneyStrs = "";
    String payTypeAccounts = "";
    BigDecimal payOrGetMoneys = BigDecimal.ZERO;
    if ((payList != null) && (payList.size() != 0)) {
      if ((payList != null) && (payList.size() != 1))
      {
        for (int i = 0; i < payList.size(); i++)
        {
          if (i != 0)
          {
            payTypeIdStrs = payTypeIdStrs + ",";
            payTypeMoneyStrs = payTypeMoneyStrs + ",";
          }
          payTypeIdStrs = payTypeIdStrs + ((Model)payList.get(i)).getInt("accountId");
          payTypeMoneyStrs = payTypeMoneyStrs + ((Model)payList.get(i)).getBigDecimal("payMoney");
          payOrGetMoneys = BigDecimalUtils.add(payOrGetMoneys, ((Model)payList.get(i)).getBigDecimal("payMoney"));
        }
        payTypeAccounts = "现金银行多账户";
      }
      else
      {
        Integer accountId = ((Model)payList.get(0)).getInt("accountId");
        Accounts accounts = (Accounts)Accounts.dao.findById(configName, accountId);
        payTypeIdStrs = String.valueOf(accountId);
        payTypeMoneyStrs = String.valueOf(((Model)payList.get(0)).getBigDecimal("payMoney"));
        payTypeAccounts = accounts.getStr("fullName");
        payOrGetMoneys = BigDecimalUtils.add(payOrGetMoneys, ((Model)payList.get(0)).getBigDecimal("payMoney"));
      }
    }
    map.put("payTypeIdStrs", payTypeIdStrs);
    map.put("payTypeMoneyStrs", payTypeMoneyStrs);
    map.put("payTypeAccounts", payTypeAccounts);
    map.put("payOrgetMoneys", String.valueOf(payOrGetMoneys));
    return map;
  }
  
  public static Map<String, Object> setDetailStorageId(Model detail, BigDecimal amount, String trIndex, Integer billStorageId1, String attr1, Integer billStorageId2, String attr2)
  {
    Map<String, Object> map = new HashMap();
    map.put("statusCode", Integer.valueOf(200));
    if (trIndex == null) {
      trIndex = "";
    }
    Integer detailStorageId1 = null;
    Integer detailStorageId2 = null;
    if ((attr1 != null) && (!attr1.equals(""))) {
      detailStorageId1 = detail.getInt(attr1);
    }
    if ((attr2 != null) && (!attr2.equals(""))) {
      detailStorageId2 = detail.getInt(attr2);
    }
    if (detailStorageId1 == null) {
      detailStorageId1 = Integer.valueOf(0);
    }
    if (detailStorageId2 == null) {
      detailStorageId2 = Integer.valueOf(0);
    }
    boolean billStorageFlag1 = false;
    boolean billStorageFlag2 = false;
    if ((billStorageId1 != null) && (billStorageId1.intValue() > 0)) {
      billStorageFlag1 = true;
    }
    if ((billStorageId2 != null) && (billStorageId2.intValue() > 0)) {
      billStorageFlag2 = true;
    }
    if ((attr1 != null) && (!attr1.equals(""))) {
      if (!billStorageFlag1)
      {
        if (detailStorageId1.intValue() < 1)
        {
          map.put("message", "第" + trIndex + "行多仓库录入错误!");
          map.put("statusCode", AioConstants.HTTP_RETURN300);
          return map;
        }
      }
      else if (detailStorageId1.intValue() < 1) {
        detail.set(attr1, billStorageId1);
      }
    }
    if ((attr2 != null) && (!attr2.equals(""))) {
      if (!billStorageFlag2)
      {
        if (detailStorageId2.intValue() < 1)
        {
          map.put("message", "第" + trIndex + "行多仓库录入错误!");
          map.put("statusCode", AioConstants.HTTP_RETURN300);
          return map;
        }
      }
      else if (detailStorageId2.intValue() < 1) {
        detail.set(attr2, billStorageId2);
      }
    }
    if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) < 1)
    {
      map.put("message", "第" + trIndex + "行数量录入错误!");
      map.put("statusCode", AioConstants.HTTP_RETURN300);
      return map;
    }
    return map;
  }
  
  public static void billOrderDefualPriceMoney(Model model)
  {
    BigDecimal moneys = model.getBigDecimal("moneys");
    BigDecimal discountMoneys = model.getBigDecimal("discountMoneys");
    if (BigDecimalUtils.compare(discountMoneys, BigDecimal.ZERO) == 0)
    {
      discountMoneys = moneys;
      model.set("discountMoneys", discountMoneys);
    }
    BigDecimal taxMoneys = model.getBigDecimal("taxMoneys");
    if (BigDecimalUtils.compare(taxMoneys, BigDecimal.ZERO) == 0)
    {
      taxMoneys = discountMoneys;
      model.set("taxMoneys", taxMoneys);
    }
  }
  
  public static void detailOrderDefualPriceMoney(Model detail)
  {
    BigDecimal price = detail.getBigDecimal("price");
    BigDecimal amount = detail.getBigDecimal("amount");
    
    BigDecimal discount = detail.getBigDecimal("discount");
    if (BigDecimalUtils.compare(discount, BigDecimal.ZERO) == 0) {
      discount = BigDecimal.ONE;
    }
    BigDecimal discountPrice = detail.getBigDecimal("discountPrice");
    if ((discountPrice == null) && (BigDecimalUtils.compare(discountPrice, BigDecimal.ZERO) == 0))
    {
      detail.set("discountPrice", price);
      detail.set("discountMoney", BigDecimalUtils.mul(amount, price));
      discountPrice = price;
    }
    BigDecimal taxPrice = detail.getBigDecimal("taxPrice");
    if ((taxPrice == null) || (BigDecimalUtils.compare(taxPrice, BigDecimal.ZERO) == 0))
    {
      detail.set("taxPrice", discountPrice);
      detail.set("taxMoney", BigDecimalUtils.mul(amount, price));
      taxPrice = discountPrice;
    }
  }
  
  public static BigDecimal outProductGetCostPriceAll(String configName, int billTypeId, int isRCW, int storageId, Product product, Model detail, String costPriceAttr)
  {
    int selectUnitId = detail.getInt("selectUnitId").intValue();
    BigDecimal costPrice = detail.getBigDecimal(costPriceAttr);
    if (isRCW != AioConstants.RCW_VS)
    {
      Map<String, String> map = outProductGetCostPrice(configName, storageId, product, costPrice, selectUnitId);
      costPrice = new BigDecimal((String)map.get("costPrice"));
    }
    else
    {
      String detailTableName = BillType.getValue1(configName, billTypeId, "billDetailTableName");
      Record oldDetailRecord = Db.use(configName).findById(detailTableName, detail.getInt("rcwId"));
      if (oldDetailRecord != null) {
        costPrice = oldDetailRecord.getBigDecimal("costPrice");
      }
    }
    return costPrice;
  }
  
  public static Map<String, String> outProductGetCostPrice(String configName, int storageId, Product product, BigDecimal costPrice, int selectUnitId)
  {
    Map<String, String> returnMap = new HashMap();
    int notStockFlag = 0;
    BigDecimal stockAmount = BigDecimal.ZERO;
    int productId = product.getInt("id").intValue();
    Avgprice avgprice = Avgprice.dao.getAvgprice(configName, Integer.valueOf(storageId), Integer.valueOf(productId));
    if (avgprice != null)
    {
      costPrice = avgprice.getBigDecimal("avgPrice");
      stockAmount = avgprice.getBigDecimal("amount");
    }
    else
    {
      BigDecimal lastInPrice = Stock.dao.getProBuyPriceBySgeIdAndProIds(configName, Integer.valueOf(productId), Integer.valueOf(selectUnitId));
      if (BigDecimalUtils.notNullZero(lastInPrice))
      {
        notStockFlag = 1;
        costPrice = DwzUtils.getConversionPrice(lastInPrice, Integer.valueOf(selectUnitId), product, Integer.valueOf(1));
      }
      else
      {
        notStockFlag = 2;
      }
    }
    if (stockAmount == null) {
      stockAmount = BigDecimal.ZERO;
    }
    if (costPrice == null) {
      costPrice = BigDecimal.ZERO;
    }
    returnMap.put("stockAmount", String.valueOf(stockAmount));
    returnMap.put("costPrice", String.valueOf(costPrice));
    returnMap.put("notStockFlag", String.valueOf(notStockFlag));
    return returnMap;
  }
  
  public static void sellPriceValidate(String configName, boolean isUnPriceConfim, String unPriceConfim, int storageIdi, Product product, int selectUnitId, BigDecimal price, BigDecimal costPrice, String trIndex, List<Map<String, String>> underInPriceList, List<Map<String, String>> underMinSellPriceList, List<Map<String, String>> underCostPriceList)
  {
    int productIdi = product.getInt("id").intValue();
    if (isUnPriceConfim)
    {
      BigDecimal sellPrice = price;
      if (sellPrice == null) {
        sellPrice = BigDecimal.ZERO;
      }
      Storage sge = (Storage)Storage.dao.findById(configName, Integer.valueOf(storageIdi));
      if (unPriceConfim.indexOf("1") != -1)
      {
        PriceDiscountTrack pTrack = PriceDiscountTrack.dao.getRecentlyObj(configName, productIdi, selectUnitId);
        if (pTrack != null)
        {
          BigDecimal lastInPrice = pTrack.getBigDecimal("lastCostPrice");
          if ((lastInPrice != null) && (BigDecimalUtils.compare(sellPrice, lastInPrice) == -1))
          {
            Map<String, String> r = new HashMap();
            r.put("trIndex", trIndex);
            r.put("productCode", product.getStr("code"));
            r.put("productName", product.getStr("fullName"));
            r.put("sellPrice", String.valueOf(sellPrice));
            r.put("price", String.valueOf(lastInPrice));
            r.put("storageCode", sge.getStr("code"));
            r.put("storageName", sge.getStr("fullName"));
            r.put("selectUnit", product.getStr("calculateUnit" + selectUnitId));
            underInPriceList.add(r);
          }
        }
      }
      if (unPriceConfim.indexOf("2") != -1)
      {
        MinSellPrice mSellPrice = MinSellPrice.dao.getMinObj(configName, null, productIdi, selectUnitId);
        if (mSellPrice != null)
        {
          BigDecimal minSellPrice = mSellPrice.getBigDecimal("minSellPrice");
          if (minSellPrice == null) {
            minSellPrice = BigDecimal.ZERO;
          }
          if ((minSellPrice != null) && (BigDecimalUtils.compare(sellPrice, minSellPrice) == -1))
          {
            Map<String, String> r = new HashMap();
            r.put("trIndex", trIndex);
            r.put("productCode", product.getStr("code"));
            r.put("productName", product.getStr("fullName"));
            r.put("sellPrice", String.valueOf(sellPrice));
            r.put("price", String.valueOf(minSellPrice));
            r.put("storageCode", sge.getStr("code"));
            r.put("storageName", sge.getStr("fullName"));
            r.put("selectUnit", product.getStr("calculateUnit" + selectUnitId));
            underMinSellPriceList.add(r);
          }
        }
      }
      if (unPriceConfim.indexOf("3") != -1)
      {
        BigDecimal multiple = new BigDecimal(1);
        if (selectUnitId != 1) {
          if (selectUnitId == 2) {
            multiple = product.getBigDecimal("unitRelation2");
          } else if (selectUnitId == 3) {
            multiple = product.getBigDecimal("unitRelation3");
          }
        }
        BigDecimal currCostPrice = BigDecimalUtils.mul(costPrice, multiple);
        if (currCostPrice == null) {
          currCostPrice = BigDecimal.ZERO;
        }
        if (BigDecimalUtils.compare(sellPrice, currCostPrice) == -1)
        {
          Map<String, String> r = new HashMap();
          r.put("trIndex", trIndex);
          r.put("productCode", product.getStr("code"));
          r.put("productName", product.getStr("fullName"));
          r.put("sellPrice", String.valueOf(sellPrice));
          r.put("price", String.valueOf(currCostPrice));
          r.put("storageCode", sge.getStr("code"));
          r.put("storageName", sge.getStr("fullName"));
          r.put("selectUnit", product.getStr("calculateUnit" + selectUnitId));
          underCostPriceList.add(r);
        }
      }
    }
  }
  
  public static Map<String, Object> sellPriceValidateInfo(String basePath, List<Map<String, String>> underInPriceList, List<Map<String, String>> underMinSellPriceList, List<Map<String, String>> underCostPriceList)
  {
    Map<String, Object> map = new HashMap();
    Map<String, Object> mapParas = new HashMap();
    map.put("statusCode", AioConstants.HTTP_RETURN200);
    map.put("dialog", Boolean.valueOf(true));
    map.put("url", basePath + "/sell/sell/underPricePrompt");
    map.put("dialogId", "underPricePrompt");
    map.put("width", "600");
    map.put("height", "350");
    if (underInPriceList.size() > 0)
    {
      mapParas.put("way", Integer.valueOf(1));
      mapParas.put("wayName", "最近进价");
      
      map.put("jsonData", mapParas);
      map.put("dialogTitle", "售价低于最近进价");
      map.put("underPriceList", underInPriceList);
    }
    else if (underMinSellPriceList.size() > 0)
    {
      mapParas.put("way", Integer.valueOf(2));
      mapParas.put("wayName", "最低售价");
      
      map.put("jsonData", mapParas);
      map.put("dialogTitle", "售价低于最低售价");
      map.put("underPriceList", underMinSellPriceList);
    }
    else if (underCostPriceList.size() > 0)
    {
      mapParas.put("way", Integer.valueOf(3));
      mapParas.put("wayName", "成本价");
      
      map.put("jsonData", mapParas);
      map.put("dialogTitle", "售价低于成本价");
      map.put("underPriceList", underCostPriceList);
    }
    return map;
  }
  
  public static void negetiveStockData(List<StockComfirmUtil> negetiveStockList, String trIndex, int storageId, Product product, int costArith, int selectUnitId, BigDecimal stockAmount, BigDecimal baseAmount, int notStockFlag)
    throws UnsupportedEncodingException
  {
    StockComfirmUtil negetiveStock = new StockComfirmUtil();
    negetiveStock.setTrIndex(trIndex);
    negetiveStock.setProductId(product.getInt("id").intValue());
    negetiveStock.setSelectUnitId(selectUnitId);
    negetiveStock.setStorageId(storageId);
    negetiveStock.setStockAmount(stockAmount);
    negetiveStock.setNegativeStockAmount(BigDecimalUtils.sub(stockAmount, baseAmount));
    negetiveStock.setCostArith(costArith);
    
    String selectUnitName = "";
    if (selectUnitId == 1) {
      selectUnitName = product.getStr("calculateUnit1");
    } else if (selectUnitId == 2) {
      selectUnitName = product.getStr("calculateUnit2");
    } else if (selectUnitId == 3) {
      selectUnitName = product.getStr("calculateUnit3");
    }
    selectUnitName = selectUnitName == null ? " " : selectUnitName;
    negetiveStock.setPrdUnitName(URLEncoder.encode(selectUnitName, "utf-8"));
    if (notStockFlag == 0) {
      negetiveStock.setNotStock(0);
    } else if (notStockFlag == 1) {
      negetiveStock.setNotStock(1);
    } else if (notStockFlag == 2) {
      negetiveStock.setNotStock(2);
    }
    negetiveStockList.add(negetiveStock);
  }
  
  public static Map<String, Object> negetiveStockDataInfo(String basePath, List<StockComfirmUtil> negetiveStockList)
  {
    Map<String, Object> map = new HashMap();
    map.put("statusCode", AioConstants.HTTP_RETURN200);
    map.put("dialog", Boolean.valueOf(true));
    map.put("url", basePath + "/sell/sell/negativeStockShowDialog/");
    map.put("dialogTitle", "负库存");
    map.put("dialogId", "xsd_nagetive_info");
    map.put("width", "600");
    map.put("height", "350");
    
    String trIndexs = "";
    String storageIds = "";
    String productIds = "";
    String costAriths = "";
    String selectUnitIds = "";
    String prdUnitNames = "";
    String stockAmounts = "";
    String negativeStockAmounts = "";
    String notStocks = "";
    for (int i = 0; i < negetiveStockList.size(); i++)
    {
      StockComfirmUtil negetiveStock = (StockComfirmUtil)negetiveStockList.get(i);
      String splitStr = "";
      if (i != 0) {
        splitStr = ":";
      }
      trIndexs = trIndexs + splitStr + negetiveStock.getTrIndex();
      storageIds = storageIds + splitStr + negetiveStock.getStorageId();
      productIds = productIds + splitStr + negetiveStock.getProductId();
      costAriths = costAriths + splitStr + negetiveStock.getCostArith();
      selectUnitIds = selectUnitIds + splitStr + negetiveStock.getSelectUnitId();
      prdUnitNames = prdUnitNames + splitStr + negetiveStock.getPrdUnitName();
      stockAmounts = stockAmounts + splitStr + negetiveStock.getStockAmount();
      negativeStockAmounts = negativeStockAmounts + splitStr + negetiveStock.getNegativeStockAmount();
      notStocks = notStocks + splitStr + negetiveStock.getNotStock();
    }
    Map<String, Object> mapParas = new HashMap();
    mapParas.put("modelType", "sell");
    mapParas.put("trIndexs", trIndexs);
    mapParas.put("storageIds", storageIds);
    mapParas.put("productIds", productIds);
    mapParas.put("costAriths", costAriths);
    mapParas.put("selectUnitIds", selectUnitIds);
    mapParas.put("prdUnitNames", prdUnitNames);
    mapParas.put("stockAmounts", stockAmounts);
    mapParas.put("negativeStockAmounts", negativeStockAmounts);
    mapParas.put("notStocks", notStocks);
    map.put("params", mapParas);
    return map;
  }
  
  public static void costPriceData(List<StockComfirmUtil> costPriceInfoList, String trIndex, int storageId, Product product, int costArith, int selectUnitId, int notStockFlag)
    throws UnsupportedEncodingException
  {
    StockComfirmUtil costPriceStock = new StockComfirmUtil();
    costPriceStock.setTrIndex(trIndex);
    costPriceStock.setProductId(product.getInt("id").intValue());
    costPriceStock.setProductCode(URLEncoder.encode(product.getStr("code"), "utf-8"));
    costPriceStock.setProductFullName(URLEncoder.encode(product.getStr("fullName"), "utf-8"));
    costPriceStock.setStorageId(storageId);
    costPriceStock.setCostArith(costArith);
    String selectUnitName = "";
    if (selectUnitId == 1) {
      selectUnitName = product.getStr("calculateUnit1");
    } else if (selectUnitId == 2) {
      selectUnitName = product.getStr("calculateUnit2");
    } else if (selectUnitId == 3) {
      selectUnitName = product.getStr("calculateUnit3");
    }
    selectUnitName = selectUnitName == null ? " " : selectUnitName;
    costPriceStock.setPrdUnitName(URLEncoder.encode(selectUnitName, "utf-8"));
    costPriceStock.setSelectUnitId(selectUnitId);
    if (notStockFlag == 0) {
      costPriceStock.setNotStock(0);
    } else if (notStockFlag == 2) {
      costPriceStock.setNotStock(2);
    }
    costPriceInfoList.add(costPriceStock);
  }
  
  public static Map<String, Object> costPriceDataInfo(String basePath, List<StockComfirmUtil> costPriceInfoList)
  {
    Map<String, Object> map = new HashMap();
    map.put("statusCode", AioConstants.HTTP_RETURN200);
    map.put("dialog", Boolean.valueOf(true));
    map.put("url", basePath + "/sell/sell/getNagetiveStockPrice/");
    map.put("dialogTitle", "商品成本价格");
    map.put("dialogId", "xsd_nagetive_price_info");
    map.put("width", "600");
    map.put("height", "350");
    
    String trIndexs = "";
    String storageIds = "";
    String productIds = "";
    String productCodes = "";
    String productFullNames = "";
    String costAriths = "";
    String selectUnitIds = "";
    String prdUnitNames = "";
    String notStocks = "";
    for (int j = 0; j < costPriceInfoList.size(); j++)
    {
      StockComfirmUtil negetiveStock = (StockComfirmUtil)costPriceInfoList.get(j);
      String splitStr = "";
      if (j != 0) {
        splitStr = ":";
      }
      trIndexs = trIndexs + splitStr + negetiveStock.getTrIndex();
      storageIds = storageIds + splitStr + negetiveStock.getStorageId();
      productIds = productIds + splitStr + negetiveStock.getProductId();
      productCodes = productCodes + splitStr + negetiveStock.getProductCode();
      productFullNames = productFullNames + splitStr + negetiveStock.getProductFullName();
      costAriths = costAriths + splitStr + negetiveStock.getCostArith();
      selectUnitIds = selectUnitIds + splitStr + negetiveStock.getSelectUnitId();
      prdUnitNames = prdUnitNames + splitStr + negetiveStock.getPrdUnitName();
      notStocks = notStocks + splitStr + negetiveStock.getNotStock();
    }
    Map<String, Object> mapParas = new HashMap();
    mapParas.put("modelType", "sellReturn");
    mapParas.put("trIndexs", trIndexs);
    mapParas.put("productIds", productIds);
    mapParas.put("selectUnitIds", selectUnitIds);
    mapParas.put("prdCodes", productCodes);
    mapParas.put("prdFullNames", productFullNames);
    mapParas.put("storageIds", storageIds);
    mapParas.put("costAriths", costAriths);
    mapParas.put("prdUnitNames", prdUnitNames);
    mapParas.put("notStocks", notStocks);
    map.put("params", mapParas);
    return map;
  }
  
  public static boolean hasOpenOm(String configName)
  {
    boolean hasOm = false;
    if (PortConstants.OM_PORT == 1)
    {
      String currentConfigName = configName;
      String selectConfigName = WhichDb.dao.getChildConfigNameId();
      if (currentConfigName.equals(selectConfigName)) {
        hasOm = true;
      }
    }
    return hasOm;
  }
  
  public static String hasDelivery(List<Record> sellbookDetailList, String sellbookDetailIds, int detailId, BigDecimal baseAmount)
  {
    if (sellbookDetailList.size() == 0)
    {
      Record rec = new Record();
      sellbookDetailIds = sellbookDetailIds + "," + detailId;
      rec.set("sellbookDetailId", Integer.valueOf(detailId));
      rec.set("arrivalAmount", baseAmount);
      sellbookDetailList.add(rec);
    }
    else
    {
      for (int j = 0; j < sellbookDetailList.size(); j++) {
        if (((Record)sellbookDetailList.get(j)).getInt("sellbookDetailId").intValue() == detailId)
        {
          BigDecimal listAmouont = BigDecimalUtils.add(((Record)sellbookDetailList.get(j)).getBigDecimal("arrivalAmount"), baseAmount);
          ((Record)sellbookDetailList.get(j)).set("arrivalAmount", listAmouont);
        }
        else
        {
          Record rec = new Record();
          sellbookDetailIds = sellbookDetailIds + "," + detailId;
          rec.set("sellbookDetailId", Integer.valueOf(detailId));
          rec.set("arrivalAmount", baseAmount);
          sellbookDetailList.add(rec);
          j++;
        }
      }
    }
    return sellbookDetailIds;
  }
  
  public static Map<String, Object> hasDeliveryInfo(String configName, List<Record> sellbookDetailList, String sellbookDetailIds, String waybillnumber, String deliveryCompanyCode, String deliveryCompany)
  {
    Map<String, Object> map = new HashMap();
    map.put("statusCode", Integer.valueOf(200));
    if ((waybillnumber == null) || (waybillnumber.equals("")))
    {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
      waybillnumber = PortConstants.KUAIDI_CODE + sdf.format(new Date());
    }
    sellbookDetailIds = sellbookDetailIds.substring(1, sellbookDetailIds.length());
    List<Record> portDetailList = Db.use(configName).find("select * from om_port_record_detail where sellbookDetaillId in(" + sellbookDetailIds + ")");
    if ((portDetailList != null) && (portDetailList.size() > 0))
    {
      for (int i = 0; i < sellbookDetailList.size(); i++) {
        for (int j = 0; j < portDetailList.size(); j++) {
          if (((Record)sellbookDetailList.get(i)).getInt("sellbookDetailId") == ((Record)portDetailList.get(j)).getInt("sellbookDetaillId"))
          {
            Record rr = (Record)portDetailList.get(j);
            rr.set("hasSentAmount", ((Record)sellbookDetailList.get(i)).getBigDecimal("arrivalAmount"));
            Db.use(configName).update("om_port_record_detail", rr);
            
            ((Record)sellbookDetailList.get(i)).set("omOrderId", ((Record)portDetailList.get(j)).getInt("omOrderId"));
            ((Record)sellbookDetailList.get(i)).remove("sellbookDetailId");
            break;
          }
        }
      }
      try
      {
        if ((sellbookDetailList != null) && (sellbookDetailList.size() > 0))
        {
          Record r = Db.use(configName).findFirst("select * from aioerp_sys where key1='omDomainName'");
          JSONObject j = OmPortController.portNoticeOmHasDelivery(r.getStr("value1"), sellbookDetailList, waybillnumber, deliveryCompanyCode, deliveryCompany);
          if (!PortConstants.RET0.equals(j.get("ret")))
          {
            map.put("statusCode", AioConstants.HTTP_RETURN300);
            map.put("message", "域名配置不正确!");
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        map.put("message", "域名配置不正确!");
        map.put("statusCode", AioConstants.HTTP_RETURN300);
      }
    }
    return map;
  }
  
  public static void recoverByRecordObject(String configName, Config config, Connection con, String filePath)
    throws Exception
  {
    ObjectInputStream is = null;
    FileInputStream in = null;
    try
    {
      in = new FileInputStream(filePath);
      is = new ObjectInputStream(in);
      Iterator localIterator;
      for (;; localIterator.hasNext())
      {
        Map<String, List<Record>> dataMap = (Map)is.readObject();
        DbPro dbPro = new DbPro();
        localIterator = dataMap.entrySet().iterator();
        //continue;
        Map.Entry<String, List<Record>> data = (Map.Entry)localIterator.next();
        String tableName = (String)data.getKey();
        List<Record> list = (List)data.getValue();
        Db.use(configName).update("delete from  " + tableName);
        if (list != null)
        {
          Iterator<Record> iter = list.iterator();
          while (iter.hasNext()) {
            dbPro.save(config, con, tableName, "id", (Record)iter.next());
          }
        }
      }
    }
    catch (EOFException localEOFException) {}catch (Exception e)
    {
      throw new Exception();
    }
    finally
    {
      if (in != null) {
        in.close();
      }
      if (is != null) {
        is.close();
      }
    }
  }
  
  public static void queryBasePrivs(StringBuffer sql, String privs, String obj, String field, String defaultAttr)
  {
    if (StringUtils.isBlank(field)) {
      field = defaultAttr;
    }
    if ((StringUtils.isNotBlank(privs)) && (!"*".equals(privs)))
    {
      if (StringUtils.isNotBlank(obj)) {
        sql.append(" and (" + obj + "." + field + " in(" + privs + ") or " + obj + "." + field + " is null or " + obj + "." + field + "=0)");
      } else {
        sql.append(" and (" + field + " in(" + privs + ") or " + field + " is null or " + field + "=0)");
      }
    }
    else if (StringUtils.isBlank(privs)) {
      if (StringUtils.isNotBlank(obj)) {
        sql.append(" and (" + obj + "." + field + " =0 or " + obj + "." + field + " is null)");
      } else {
        sql.append(" and (" + field + " =0 or " + field + " is null or " + field + "=0)");
      }
    }
  }
  
  public static StringBuffer basePrivsSql(StringBuffer sql, String privs, String tableAs)
  {
    basePrivsSql(sql, privs, tableAs, "id");
    return sql;
  }
  
  public static void basePrivsSql(StringBuffer sql, String privs, String tableAs, String attr)
  {
    queryBasePrivs(sql, privs, tableAs, attr, "");
  }
  
  public static void queryProductPrivs(StringBuffer sql, String productPrivs, String obj)
  {
    queryProductPrivs(sql, productPrivs, obj, "productId");
  }
  
  public static void queryProductPrivs(StringBuffer sql, String productPrivs, String obj, String field)
  {
    queryBasePrivs(sql, productPrivs, obj, field, "productId");
  }
  
  public static void queryAccountPrivs(StringBuffer sql, String accountPrivs, String obj)
  {
    queryBasePrivs(sql, accountPrivs, obj, "accountId", "accountId");
  }
  
  public static void queryUnitPrivs(StringBuffer sql, String unitPrivs, String obj)
  {
    queryBasePrivs(sql, unitPrivs, obj, "unitId", "unitId");
  }
  
  public static void queryUnitPrivs(StringBuffer sql, String unitPrivs, String obj, String field)
  {
    queryBasePrivs(sql, unitPrivs, obj, field, "unitId");
  }
  
  public static void queryStoragePrivs(StringBuffer sql, String storagePrivs, String obj)
  {
    queryBasePrivs(sql, storagePrivs, obj, "storageId", "storageId");
  }
  
  public static void queryStoragePrivs(StringBuffer sql, String storagePrivs, String obj, String field)
  {
    queryBasePrivs(sql, storagePrivs, obj, field, "storageId");
  }
  
  public static void queryStaffPrivs(StringBuffer sql, String staffPrivs, String obj)
  {
    queryBasePrivs(sql, staffPrivs, obj, "staffId", "staffId");
  }
  
  public static void queryStaffPrivs(StringBuffer sql, String staffPrivs, String obj, String field)
  {
    queryBasePrivs(sql, staffPrivs, obj, field, "staffId");
  }
  
  public static void queryDepartmentPrivs(StringBuffer sql, String departmentPrivs, String obj)
  {
    queryBasePrivs(sql, departmentPrivs, obj, "departmentId", "departmentId");
  }
  
  public static void queryDepartmentPrivs(StringBuffer sql, String departmentPrivs, String obj, String field)
  {
    queryBasePrivs(sql, departmentPrivs, obj, field, "departmentId");
  }
  
  public static void queryAreaPrivs(StringBuffer sql, String areaPrivs, String obj, String field)
  {
    queryBasePrivs(sql, areaPrivs, obj, field, "areaId");
  }
  
  public static Map<String, Object> vaildateOneBaseUnit(Model model)
  {
    Map<String, Object> map = new HashMap();
    map.put("statusCode", Integer.valueOf(200));
    if (AioConstants.IS_FREE_VERSION == "yes")
    {
      BigDecimal unitRelation2 = model.getBigDecimal("unitRelation2");
      BigDecimal unitRelation3 = model.getBigDecimal("unitRelation3");
      if ((BigDecimalUtils.compare(unitRelation2, BigDecimal.ZERO) != 0) || (BigDecimalUtils.compare(unitRelation3, BigDecimal.ZERO) != 0))
      {
        map.put("statusCode", Integer.valueOf(300));
        map.put("message", "免费版本只能录入基本单位!!!");
      }
    }
    return map;
  }
  
  public static Map<String, Object> vaildateOneStorage(String configName)
  {
    Map<String, Object> map = new HashMap();
    map.put("statusCode", Integer.valueOf(200));
    if (AioConstants.IS_FREE_VERSION == "yes")
    {
      long count = Db.use(configName).queryLong("SELECT count(id) FROM b_storage").longValue();
      if (count > 0L)
      {
        map.put("statusCode", Integer.valueOf(300));
        map.put("message", "免费版本只能录入一个仓库!!!");
      }
    }
    return map;
  }
  
  public static Map<String, Object> vaildateOneWhichDb()
  {
    Map<String, Object> map = new HashMap();
    map.put("statusCode", Integer.valueOf(200));
    if (AioConstants.IS_FREE_VERSION == "yes")
    {
      long count = Db.use(AioConstants.CONFIG_NAME).queryLong("SELECT count(id) FROM aioerp_which_db").longValue();
      if (count > 0L)
      {
        map.put("statusCode", Integer.valueOf(300));
        map.put("message", "免费版本只能录入一个帐套!!!");
      }
    }
    return map;
  }
  
  public static void freeVesionRemoveSysConfigAttr(List<Model> list)
  {
    if (AioConstants.IS_FREE_VERSION == "yes")
    {
      List<Model> removeList = new ArrayList();
      for (int i = 0; i < list.size(); i++)
      {
        int id = ((Model)list.get(i)).getInt("id").intValue();
        if (id == 2) {
          removeList.add((Model)list.get(i));
        } else if (id == 3) {
          removeList.add((Model)list.get(i));
        } else if (id == 4) {
          removeList.add((Model)list.get(i));
        } else if (id == 5) {
          removeList.add((Model)list.get(i));
        } else if (id == 13) {
          removeList.add((Model)list.get(i));
        } else if (id == 14) {
          removeList.add((Model)list.get(i));
        }
      }
      list.removeAll(removeList);
    }
  }
}
