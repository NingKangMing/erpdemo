package com.aioerp.controller.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AvgpriceConTroller;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.reports.finance.arap.ArapRecordsController;
import com.aioerp.controller.stock.StockController;
import com.aioerp.controller.stock.StockRecordsController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.bought.PurchaseBarterBill;
import com.aioerp.model.bought.ResultHelp;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.fz.MinSellPrice;
import com.aioerp.model.fz.PriceDiscountTrack;
import com.aioerp.model.sell.barter.SellBarterBill;
import com.aioerp.model.sell.barter.SellBarterDetail;
import com.aioerp.model.sell.barter.SellBarterDraftBill;
import com.aioerp.model.sell.barter.SellBarterDraftDetail;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.stock.StockDraftRecords;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class SellBarterBillController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE13;
  
  public void index()
  {
    String configName = loginConfigName();
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    

    billCodeAuto(billTypeId);
    

    notEditStaff();
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws SQLException
  {
    String configName = loginConfigName();
    
    SellBarterBill bill = (SellBarterBill)getModel(SellBarterBill.class);
    
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    BigDecimal privilegeMoney = bill.getBigDecimal("privilegeMoney");
    BigDecimal payMoney = bill.getBigDecimal("payMoney");
    Map<String, Object> pmap = verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_ADD, getParaToInt("unit.id"), AioConstants.WAY_GET, payMoney, privilegeMoney);
    if (pmap != null)
    {
      renderJson(pmap);
      return;
    }
    boolean comfirm = getParaToBoolean("needComfirm", Boolean.valueOf(true)).booleanValue();
    boolean confirmAllow = getParaToBoolean("confirmAllow", Boolean.valueOf(true)).booleanValue();
    
    boolean hasOther = SellBarterBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    bill.set("id", null);
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(billTypeId));
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Date recodeDate = bill.getTimestamp("recodeDate");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> inDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellBarterDetail.class, "sellBarterInDetail");
    List<HelpUtil> helpInList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpInUtil");
    
    List<Model> outDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellBarterDetail.class, "sellBarterOutDetail");
    List<HelpUtil> helpOutList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpOutUtil");
    if ((inDetailList.size() == 0) || (outDetailList.size() == 0))
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    BigDecimal allowMoneys = PurchaseBarterBill.getAllowBarterMoney(configName, getParaToInt("unit.id").intValue(), getPara("sellBarterBill.recodeDate"), "out");
    BigDecimal inMoney = bill.getBigDecimal("inMoney");
    if ((confirmAllow) && (BigDecimalUtils.compare(allowMoneys, inMoney) < 0))
    {
      Map<String, Object> map = new HashMap();
      map.put("statusCode", AioConstants.HTTP_RETURN200);
      map.put("dialog", Boolean.valueOf(true));
      map.put("url", getAttr("base") + "/sell/barter/confirmAllow");
      map.put("dialogTitle", "提示");
      map.put("dialogId", "confirmAllow");
      map.put("width", "300");
      map.put("height", "150");
      
      renderJson(map);
      return;
    }
    Integer inStorageId = getParaToInt("inStorage.id");
    Integer outStorageId = getParaToInt("outStorage.id");
    bill.set("unitId", getParaToInt("unit.id"));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("outStorageId", outStorageId);
    bill.set("inStorageId", inStorageId);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    

    List<ResultHelp> costList = new ArrayList();
    for (int i = 0; i < inDetailList.size(); i++)
    {
      Model inDetail = (Model)inDetailList.get(i);
      Integer detailstorage = inDetail.getInt("storageId");
      if (((inStorageId == null) || (inStorageId.intValue() == 0)) && ((detailstorage == null) || (detailstorage.intValue() == 0)))
      {
        int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpInList.get(i)).getTrIndex(), 1);
        
        setAttr("message", "第" + trIndex + "行多仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      BigDecimal amount = inDetail.getBigDecimal("amount");
      if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) < 1)
      {
        int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpInList.get(i)).getTrIndex(), 1);
        
        setAttr("message", "第" + trIndex + "行数量录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      BigDecimal stockPrice = inDetail.getBigDecimal("costPrice");
      Integer detailStorageId = inDetail.getInt("storageId");
      if (detailStorageId == null) {
        detailStorageId = inStorageId;
      }
      Integer productId = inDetail.getInt("productId");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        if (inDetail.getBigDecimal("costPrice") == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, detailStorageId.intValue(), product, inDetail.getBigDecimal("costPrice"), inDetail.getInt("selectUnitId").intValue());
          stockPrice = new BigDecimal((String)map.get("costPrice"));
          inDetail.set("costPrice", stockPrice);
        }
        int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpInList.get(i)).getTrIndex(), 1);
        Integer selectUnitId = inDetail.getInt("selectUnitId");
        String selectUnit = product.getStr("calculateUnit1");
        if (2 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit2");
        } else if (3 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit3");
        }
        if (!BigDecimalUtils.notNullZero(stockPrice))
        {
          ResultHelp help = new ResultHelp(Integer.valueOf(trIndex), productId, product.getStr("fullName"), 
            product.getStr("code"), null, null, detailStorageId, 
            null, null, null, selectUnit);
          costList.add(help);
        }
      }
    }
    if (costList.size() > 0)
    {
      Map<String, Object> map = new HashMap();
      map.put("statusCode", AioConstants.HTTP_RETURN200);
      map.put("dialog", Boolean.valueOf(true));
      map.put("url", getAttr("base") + "/sell/barter/setStockPrice");
      map.put("dialogTitle", "商品成本价格");
      map.put("dialogId", "xshh_costprice_info");
      map.put("width", "500");
      map.put("height", "350");
      setSessionAttr("costList", costList);
      renderJson(map);
      return;
    }
    Boolean isNegativeStock = SysConfig.getConfig(configName, 1);
    Boolean isNotUnPrice = SysConfig.getConfig(configName, 7);
    Boolean isUnPriceConfim = SysConfig.getConfig(configName, 6);
    String unPriceConfim = getPara("unPriceConfim", "123");
    
    List<Map<String, String>> underInPriceList = new ArrayList();
    List<Map<String, String>> underMinSellPriceList = new ArrayList();
    List<Map<String, String>> underCostPriceList = new ArrayList();
    


    List<ResultHelp> rList = new ArrayList();
    for (int i = 0; i < outDetailList.size(); i++)
    {
      Model outDetail = (Model)outDetailList.get(i);
      Integer detailstorage = outDetail.getInt("storageId");
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpOutList.get(i)).getTrIndex(), 1);
      if (((outStorageId == null) || (outStorageId.intValue() == 0)) && ((detailstorage == null) || (detailstorage.intValue() == 0)))
      {
        setAttr("message", "第" + trIndex + "行多仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      BigDecimal amount = outDetail.getBigDecimal("amount");
      if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) < 1)
      {
        setAttr("message", "第" + trIndex + "行数量录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      Integer productId = outDetail.getInt("productId");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        Integer selectUnitId = outDetail.getInt("selectUnitId");
        String selectUnit = product.getStr("calculateUnit1");
        if (2 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit2");
        } else if (3 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit3");
        }
        Integer detailStorageId = outDetail.getInt("storageId");
        if ((detailStorageId == null) || (detailStorageId.intValue() == 0)) {
          detailStorageId = outStorageId;
        }
        BigDecimal price = outDetail.getBigDecimal("price");
        if ((outDetail.getBigDecimal("discountPrice") != null) && (BigDecimalUtils.compare(outDetail.getBigDecimal("discountPrice"), BigDecimal.ZERO) != 0)) {
          price = outDetail.getBigDecimal("discountPrice");
        }
        if ((outDetail.getBigDecimal("taxPrice") != null) && (BigDecimalUtils.compare(outDetail.getBigDecimal("taxPrice"), BigDecimal.ZERO) != 0)) {
          price = outDetail.getBigDecimal("taxPrice");
        }
        if (isNotUnPrice.booleanValue())
        {
          MinSellPrice MinSellPriceObj = MinSellPrice.dao.getMinObj(configName, detailStorageId, productId.intValue(), selectUnitId.intValue());
          if (MinSellPriceObj != null)
          {
            BigDecimal minSellPrice = MinSellPriceObj.getBigDecimal("minSellPrice");
            if ((minSellPrice != null) && (BigDecimalUtils.compare(price, minSellPrice) == 1))
            {
              setAttr("message", "第" + trIndex + "行商品价格低于最低售价不能过账!");
              setAttr("statusCode", AioConstants.HTTP_RETURN300);
              renderJson();
              return;
            }
          }
        }
        if (isUnPriceConfim.booleanValue())
        {
          BigDecimal sellPrice = price;
          if (sellPrice == null) {
            sellPrice = BigDecimal.ZERO;
          }
          Storage sge = (Storage)Storage.dao.findById(configName, detailStorageId);
          if (unPriceConfim.indexOf("1") != -1)
          {
            PriceDiscountTrack pTrack = PriceDiscountTrack.dao.getRecentlyObj(configName, productId.intValue(), selectUnitId.intValue());
            if (pTrack != null)
            {
              BigDecimal lastInPrice = pTrack.getBigDecimal("lastCostPrice");
              if (lastInPrice == null) {
                lastInPrice = BigDecimal.ZERO;
              }
              if ((lastInPrice != null) && (BigDecimalUtils.compare(sellPrice, lastInPrice) == -1))
              {
                Map<String, String> r = new HashMap();
                r.put("trIndex",trimNull(trIndex));
                r.put("productCode", product.getStr("code"));
                r.put("productName", product.getStr("fullName"));
                r.put("sellPrice", sellPrice.toString());
                r.put("price", lastInPrice.toString());
                r.put("storageCode", sge.getStr("code"));
                r.put("storageName", sge.getStr("fullName"));
                r.put("selectUnit", product.getStr("calculateUnit" + selectUnitId));
                underInPriceList.add(r);
              }
            }
          }
          if (unPriceConfim.indexOf("2") != -1)
          {
            MinSellPrice mSellPrice = MinSellPrice.dao.getMinObj(configName, null, productId.intValue(), selectUnitId.intValue());
            if (mSellPrice != null)
            {
              BigDecimal minSellPrice = mSellPrice.getBigDecimal("minSellPrice");
              if (minSellPrice == null) {
                minSellPrice = BigDecimal.ZERO;
              }
              if ((minSellPrice != null) && (BigDecimalUtils.compare(sellPrice, minSellPrice) == -1))
              {
                Map<String, String> r = new HashMap();
                r.put("trIndex", trimNull(trIndex));
                r.put("productCode", product.getStr("code"));
                r.put("productName", product.getStr("fullName"));
                r.put("sellPrice", sellPrice.toString());
                r.put("price", minSellPrice.toString());
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
            if (selectUnitId.intValue() != 1) {
              if (selectUnitId.intValue() == 2) {
                multiple = product.getBigDecimal("unitRelation2");
              } else if (selectUnitId.intValue() == 3) {
                multiple = product.getBigDecimal("unitRelation3");
              }
            }
            BigDecimal costPrice = outDetail.getBigDecimal("costPrice");
            if (costPrice == null)
            {
              Map<String, String> map = ComFunController.outProductGetCostPrice(configName, detailStorageId.intValue(), product, costPrice, selectUnitId.intValue());
              costPrice = new BigDecimal((String)map.get("costPrice"));
            }
            BigDecimal currCostPrice = BigDecimalUtils.mul(costPrice, multiple);
            if (BigDecimalUtils.compare(sellPrice, currCostPrice) == -1)
            {
              Map<String, String> r = new HashMap();
              r.put("trIndex", trimNull(trIndex));
              r.put("productCode", product.getStr("code"));
              r.put("productName", product.getStr("fullName"));
              r.put("sellPrice", sellPrice.toString());
              r.put("price", currCostPrice.toString());
              r.put("storageCode", sge.getStr("code"));
              r.put("storageName", sge.getStr("fullName"));
              r.put("selectUnit", product.getStr("calculateUnit" + selectUnitId));
              underCostPriceList.add(r);
            }
          }
        }
        Integer costArith = product.getInt("costArith");
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal sAmount = BigDecimal.ZERO;
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4)
        {
          sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), detailStorageId.intValue(), outDetail.getBigDecimal("costPrice"), outDetail.getStr("batch"), outDetail.getDate("produceDate"), outDetail.getDate("produceEndDate"));
        }
        else
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, detailStorageId.intValue(), product, outDetail.getBigDecimal("costPrice"), selectUnitId.intValue());
          sAmount = new BigDecimal((String)map.get("stockAmount"));
          outDetail.set("costPrice", new BigDecimal((String)map.get("costPrice")));
        }
        BigDecimal avgPrice = StockController.subAvgPrice(configName, outDetail, costArith, baseAmount, outDetail.getBigDecimal("costPrice"), detailStorageId.intValue(), productId.intValue());
        if ((comfirm) && (isNegativeStock.booleanValue()) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0))
        {
          if (SysConfig.getConfig(configName, 15).booleanValue())
          {
            Storage storage = (Storage)Storage.dao.findById(configName, detailStorageId);
            ResultHelp help = new ResultHelp(Integer.valueOf(trIndex), productId, product.getStr("fullName"), 
              product.getStr("code"), sAmount, BigDecimalUtils.sub(sAmount, baseAmount), detailStorageId, 
              storage.getStr("fullName"), storage.getStr("code"), avgPrice, selectUnit);
            rList.add(help);
          }
        }
        else if (((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && (BigDecimalUtils.compare(sAmount, baseAmount) < 0)) || ((!isNegativeStock.booleanValue()) && (BigDecimalUtils.compare(sAmount, baseAmount) < 0)))
        {
          setAttr("message", "错误类型：库存数量不够<br/> 商品编号：" + product.getStr("code") + "<br/> 商品全名：" + product.getStr("fullName"));
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
      }
    }
    if (isUnPriceConfim.booleanValue())
    {
      Map<String, Object> map = new HashMap();
      Map<String, Object> mapParas = new HashMap();
      map.put("statusCode", AioConstants.HTTP_RETURN200);
      map.put("dialog", Boolean.valueOf(true));
      map.put("url", getAttr("base") + "/sell/sell/underPricePrompt");
      map.put("dialogId", "underPricePrompt");
      map.put("width", "600");
      map.put("height", "350");
      if (underInPriceList.size() > 0)
      {
        mapParas.put("way", Integer.valueOf(1));
        mapParas.put("wayName", "最近进价");
        
        map.put("jsonData", mapParas);
        map.put("dialogTitle", "售价低于最近进价");
        setSessionAttr("underPriceList", underInPriceList);
        renderJson(map);
        return;
      }
      if (underMinSellPriceList.size() > 0)
      {
        mapParas.put("way", Integer.valueOf(2));
        mapParas.put("wayName", "最低售价");
        
        map.put("jsonData", mapParas);
        map.put("dialogTitle", "售价低于最低售价");
        setSessionAttr("underPriceList", underMinSellPriceList);
        renderJson(map);
        return;
      }
      if (underCostPriceList.size() > 0)
      {
        mapParas.put("way", Integer.valueOf(3));
        mapParas.put("wayName", "成本价");
        
        map.put("jsonData", mapParas);
        map.put("dialogTitle", "售价低于成本价");
        setSessionAttr("underPriceList", underCostPriceList);
        renderJson(map);
        return;
      }
    }
    if (rList.size() > 0)
    {
      Map<String, Object> map = new HashMap();
      map.put("statusCode", AioConstants.HTTP_RETURN200);
      map.put("dialog", Boolean.valueOf(true));
      map.put("url", getAttr("base") + "/bought/return/negativeOption");
      map.put("dialogTitle", "负库存");
      map.put("dialogId", "jhthd_nagetive_info");
      map.put("width", "600");
      map.put("height", "350");
      setSessionAttr("resultList", rList);
      renderJson(map);
      return;
    }
    Date time = new Date();
    bill.set("updateTime", time);
    if (bill.getBigDecimal("inDiscountMoneys") == null) {
      bill.set("inDiscountMoneys", BigDecimalUtils.sub(bill.getBigDecimal("inMoney"), bill.getBigDecimal("inTaxes")));
    }
    if (bill.getBigDecimal("outDiscountMoneys") == null) {
      bill.set("outDiscountMoneys", BigDecimalUtils.sub(bill.getBigDecimal("outMoney"), bill.getBigDecimal("outTaxes")));
    }
    billCodeIncrease(bill, "add");
    bill.save(configName);
    

    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("gapMoney"));
    
    BigDecimal costInMoneys = BigDecimal.ZERO;
    for (int i = 0; i < inDetailList.size(); i++)
    {
      Model inDetail = (Model)inDetailList.get(i);
      inDetail.set("id", null);
      inDetail.set("type", Integer.valueOf(1));
      Integer detailstorage = inDetail.getInt("storageId");
      if (inDetail.get("discountPrice") == null) {
        inDetail.set("discountPrice", inDetail.get("price"));
      }
      if (inDetail.get("taxPrice") == null) {
        inDetail.set("taxPrice", inDetail.get("discountPrice"));
      }
      if (inDetail.getBigDecimal("discount") == null) {
        inDetail.set("discount", BigDecimal.ONE);
      }
      if (inDetail.get("discountMoney") == null) {
        inDetail.set("discountMoney", BigDecimalUtils.mul(BigDecimalUtils.mul(inDetail.getBigDecimal("amount"), inDetail.getBigDecimal("price")), inDetail.getBigDecimal("discount")));
      }
      if (inDetail.get("taxMoney") == null) {
        inDetail.set("taxMoney", BigDecimalUtils.add(inDetail.getBigDecimal("discountMoney"), inDetail.getBigDecimal("taxes")));
      }
      if ((detailstorage == null) || (detailstorage.intValue() == 0)) {
        inDetail.set("storageId", inStorageId);
      }
      Integer productId = inDetail.getInt("productId");
      BigDecimal amount = inDetail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        inDetail.set("billId", bill.getInt("id"));
        
        inDetail.set("updateTime", time);
        
        Integer selectUnitId = inDetail.getInt("selectUnitId");
        Integer detailStorageId = inDetail.getInt("storageId");
        BigDecimal price = inDetail.getBigDecimal("price");
        if ((inDetail.getBigDecimal("discountPrice") != null) && (BigDecimalUtils.compare(inDetail.getBigDecimal("discountPrice"), BigDecimal.ZERO) != 0)) {
          price = inDetail.getBigDecimal("discountPrice");
        }
        if (detailStorageId == null) {
          detailStorageId = inStorageId;
        }
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        



        BigDecimal stockPrice = inDetail.getBigDecimal("costPrice");
        BigDecimal costMoney = BigDecimalUtils.mul(baseAmount, stockPrice);
        if (inDetail.getBigDecimal("costPrice") == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, detailStorageId.intValue(), product, inDetail.getBigDecimal("costPrice"), selectUnitId.intValue());
          costMoney = BigDecimalUtils.mul(baseAmount, new BigDecimal((String)map.get("costPrice")));
          stockPrice = new BigDecimal((String)map.get("costPrice"));
          
          inDetail.set("costPrice", stockPrice);
        }
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          inDetail.set("giftAmount", baseAmount);
        }
        inDetail.set("baseAmount", baseAmount);
        inDetail.set("basePrice", basePrice);
        inDetail.set("costMoneys", costMoney);
        AvgpriceConTroller.addAvgprice(configName, "in", detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, stockPrice));
        

        StockController.stockChange(configName, "in", detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, stockPrice, inDetail.getStr("batch"), inDetail.getDate("produceDate"), inDetail.getDate("produceEndDate"));
        
        StockRecordsController.addRecords(configName, billTypeId, "in", bill, inDetail, stockPrice, baseAmount, time, bill.getTimestamp("recodeDate"), inDetail.getInt("storageId"));
        
        costInMoneys = BigDecimalUtils.add(costInMoneys, inDetail.getBigDecimal("costMoneys"));
        
        inDetail.save(configName);
      }
    }
    BigDecimal costOutMoneys = BigDecimal.ZERO;
    for (int i = 0; i < outDetailList.size(); i++)
    {
      Model outDetail = (Model)outDetailList.get(i);
      outDetail.set("type", Integer.valueOf(2));
      outDetail.set("id", null);
      
      Integer detailstorage = outDetail.getInt("storageId");
      if ((detailstorage == null) || (detailstorage.intValue() == 0)) {
        outDetail.set("storageId", outStorageId);
      }
      if (outDetail.get("discountPrice") == null) {
        outDetail.set("discountPrice", outDetail.get("price"));
      }
      if (outDetail.get("taxPrice") == null) {
        outDetail.set("taxPrice", outDetail.get("discountPrice"));
      }
      if (outDetail.getBigDecimal("discount") == null) {
        outDetail.set("discount", BigDecimal.ONE);
      }
      if (outDetail.get("discountMoney") == null) {
        outDetail.set("discountMoney", BigDecimalUtils.mul(BigDecimalUtils.mul(outDetail.getBigDecimal("amount"), outDetail.getBigDecimal("price")), outDetail.getBigDecimal("discount")));
      }
      if (outDetail.get("taxMoney") == null) {
        outDetail.set("taxMoney", BigDecimalUtils.add(outDetail.getBigDecimal("discountMoney"), outDetail.getBigDecimal("taxes")));
      }
      Integer productId = outDetail.getInt("productId");
      
      BigDecimal price = outDetail.getBigDecimal("price");
      if ((outDetail.getBigDecimal("discountPrice") != null) && (BigDecimalUtils.compare(outDetail.getBigDecimal("discountPrice"), BigDecimal.ZERO) != 0)) {
        price = outDetail.getBigDecimal("discountPrice");
      }
      if ((outDetail.getBigDecimal("taxPrice") != null) && (BigDecimalUtils.compare(outDetail.getBigDecimal("taxPrice"), BigDecimal.ZERO) != 0)) {
        price = outDetail.getBigDecimal("taxPrice");
      }
      BigDecimal amount = outDetail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        
        outDetail.set("billId", bill.getInt("id"));
        outDetail.set("updateTime", time);
        
        Integer selectUnitId = outDetail.getInt("selectUnitId");
        Integer detailStorageId = outDetail.getInt("storageId");
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        outDetail.set("baseAmount", baseAmount);
        outDetail.set("basePrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          outDetail.set("giftAmount", baseAmount);
        }
        Integer costArith = product.getInt("costArith");
        
        BigDecimal sAmount = BigDecimal.ZERO;
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
          sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), detailStorageId.intValue(), outDetail.getBigDecimal("costPrice"), outDetail.getStr("batch"), outDetail.getDate("produceDate"), outDetail.getDate("produceEndDate"));
        } else {
          sAmount = Stock.dao.getStockAmount(configName, productId, detailStorageId);
        }
        if (((!isNegativeStock.booleanValue()) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue())) || ((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && 
          (BigDecimalUtils.compare(sAmount, amount) == -1)))
        {
          commonRollBack();
          setAttr("message", "错误类型：库存数量不够<br/> 商品编号：" + product.getStr("code") + "<br/> 商品全名：" + product.getStr("fullName"));
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
        BigDecimal stockPrice = outDetail.getBigDecimal("costPrice");
        BigDecimal costMoney = BigDecimalUtils.mul(baseAmount, stockPrice);
        if (outDetail.getBigDecimal("costPrice") == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, detailStorageId.intValue(), product, outDetail.getBigDecimal("costPrice"), selectUnitId.intValue());
          costMoney = BigDecimalUtils.mul(baseAmount, new BigDecimal((String)map.get("costPrice")));
          stockPrice = new BigDecimal((String)map.get("costPrice"));
          
          outDetail.set("costPrice", stockPrice);
        }
        StockController.stockSubChange(configName, costArith.intValue(), baseAmount, detailStorageId.intValue(), productId.intValue(), stockPrice, outDetail.getStr("batch"), outDetail.getDate("produceDate"), outDetail.getDate("produceEndDate"), null);
        
        StockRecordsController.addRecords(configName, billTypeId, "out", bill, outDetail, stockPrice, baseAmount, time, bill.getTimestamp("recodeDate"), outDetail.getInt("storageId"));
        


        AvgpriceConTroller.addAvgprice(configName, "out", detailStorageId, productId, baseAmount, costMoney);
        

        outDetail.set("costMoneys", costMoney);
        
        costOutMoneys = BigDecimalUtils.add(costOutMoneys, outDetail.getBigDecimal("costMoneys"));
        outDetail.save(configName);
      }
    }
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    
    Map<String, Object> record = new HashMap();
    record.put("privilegeType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    record.put("getOrPayType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    record.put("proOutList", outDetailList);
    record.put("proInList", inDetailList);
    record.put("accounts", getOtherAccount(BigDecimalUtils.sub(bill.getBigDecimal("outTaxes"), bill.getBigDecimal("inTaxes")), 
      BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney")), 
      BigDecimalUtils.sub(bill.getBigDecimal("outDiscountMoneys"), bill.getBigDecimal("inDiscountMoneys")), 
      BigDecimalUtils.sub(costOutMoneys, costInMoneys)));
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), bill.getBigDecimal("privilege"), payTypeIds, payTypeMoneys, record);
    
    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
    

    BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney"));
    Unit unit = (Unit)Unit.dao.findById(configName, bill.getInt("unitId"));
    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), null, bill, unit.getInt("areaId"), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, bill.getBigDecimal("payMoney"));
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      setDraftStrs();
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      SellBarterDraftBill.dao.deleteById(configName, draftBillId);
      SellBarterDraftDetail.dao.delByBill(configName, draftBillId);
      PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(billTypeId));
      
      StockDraftRecords.delRecords(configName, billTypeId, draftBillId.intValue());
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "sell_barter_info");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    printBeforSave1();
    
    renderJson();
  }
  
  public void confirmAllow()
  {
    render("confirmAllow.html");
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    SellBarterBill bill = (SellBarterBill)SellBarterBill.dao.findById(configName, Integer.valueOf(id));
    List<Model> detailInList = SellBarterDetail.dao.getListByBillId(configName, Integer.valueOf(id), Integer.valueOf(1));
    detailInList = addTrSize(detailInList, 5);
    List<Model> detailOutList = SellBarterDetail.dao.getListByBillId(configName, Integer.valueOf(id), Integer.valueOf(2));
    detailOutList = addTrSize(detailOutList, 5);
    

    setPayTypeAttr(configName, "saveBill", billTypeId, bill.getInt("id").intValue());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("detailInList", detailInList);
    setAttr("detailOutList", detailOutList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    render("look.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> result = new HashMap();
    SellBarterBill bill = (SellBarterBill)SellBarterBill.dao.findById(configName, billId);
    if (bill == null)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", "该销售换货单不存在");
      return result;
    }
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", rmap.get("message"));
      return result;
    }
    BigDecimal privilegeMoney = bill.getBigDecimal("privilegeMoney");
    BigDecimal payMoney = bill.getBigDecimal("payMoney");
    Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unitId"), AioConstants.WAY_GET, payMoney, privilegeMoney);
    if (vmap != null)
    {
      result = vmap;
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      return result;
    }
    List<Model> detailInList = SellBarterDetail.dao.getListByBillId(configName, billId, Integer.valueOf(1));
    boolean comfirm = SysConfig.getConfig(configName, 1).booleanValue();
    Integer productId;
    for (Model detail : detailInList)
    {
      BigDecimal amount = detail.getBigDecimal("baseAmount");
      productId = detail.getInt("productId");
      Integer storageId = detail.getInt("storageId");
      Product product = (Product)detail.get("product");
      Integer costArith = product.getInt("costArith");
      
      BigDecimal sAmount = BigDecimal.ZERO;
      if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
        sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), storageId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
      } else {
        sAmount = Stock.dao.getStockAmount(configName, productId, storageId);
      }
      if ((!comfirm) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0))
      {
        result.put("status", Integer.valueOf(AioConstants.RCW_NO));
        result.put("message", "该销售换货单有商品库存不足");
        return result;
      }
      if ((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0))
      {
        result.put("status", Integer.valueOf(AioConstants.RCW_NO));
        result.put("message", "该销售换货单有商品库存不足");
        return result;
      }
    }
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
    bill.update(configName);
    SellBarterBill newBill = bill;
    newBill.set("id", null);
    newBill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
    Date time = new Date();
    newBill.set("updateTime", time);
    
    billRcwAddRemark(bill, null);
    newBill.save(configName);
    
    BillHistoryController.saveBillHistory(configName, newBill, billTypeId, newBill.getBigDecimal("gapMoney"));
    SellBarterDetail newDetail;
    for (Model odlDetail : detailInList)
    {
      newDetail = (SellBarterDetail)odlDetail;
      
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.set("id", null);
      newDetail.set("billId", newBill.getInt("id"));
      newDetail.set("updateTime", time);
      BigDecimal amount = odlDetail.getBigDecimal("baseAmount");
       productId = odlDetail.getInt("productId");
      Integer storageId = odlDetail.getInt("storageId");
      Product product = (Product)odlDetail.get("product");
      Integer costArith = product.getInt("costArith");
      BigDecimal costPrice = newDetail.getBigDecimal("costPrice");
      
      StockController.stockSubChange(configName, costArith.intValue(), amount, storageId.intValue(), productId.intValue(), costPrice, newDetail.getStr("batch"), newDetail.getDate("produceDate"), newDetail.getDate("produceEndDate"), null);
      
      Timestamp times = new Timestamp(time.getTime());
      StockRecordsController.addRecords(configName, billTypeId, "out", newBill, newDetail, costPrice, amount, time, times, newDetail.getInt("storageId"));
      
      AvgpriceConTroller.addAvgprice(configName, "out", storageId, productId, amount, BigDecimalUtils.mul(amount, costPrice));
      newDetail.save(configName);
    }
    List<Model> detailOutList = SellBarterDetail.dao.getListByBillId(configName, billId, Integer.valueOf(2));
    for (Model odlDetail : detailOutList)
    {
       newDetail = (SellBarterDetail)odlDetail;
      
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.set("id", null);
      newDetail.set("billId", newBill.getInt("id"));
      newDetail.set("updateTime", time);
      newDetail.save(configName);
      
      BigDecimal amount = newDetail.getBigDecimal("baseAmount");
       productId = newDetail.getInt("productId");
      Integer storageId = newDetail.getInt("storageId");
      Product product = (Product)newDetail.get("product");
      BigDecimal costPrice = newDetail.getBigDecimal("costPrice");
      
      AvgpriceConTroller.addAvgprice(configName, "in", storageId, productId, amount, BigDecimalUtils.mul(amount, costPrice));
      StockController.stockChange(configName, "in", storageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), amount, costPrice, newDetail.getStr("batch"), newDetail.getDate("produceDate"), newDetail.getDate("produceEndDate"));
      Timestamp times = new Timestamp(time.getTime());
      StockRecordsController.addRecords(configName, billTypeId, "in", newBill, newDetail, costPrice, amount, time, times, newDetail.getInt("storageId"));
    }
    PayTypeController.rcwAccountsRecoder(configName, billTypeId, billId, newBill.getInt("id"));
    
    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
    

    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), billId, null, null, null, null, null, null, null);
    
    result.put("status", Integer.valueOf(AioConstants.RCW_BY));
    result.put("message", "红冲成功");
    return result;
  }
  
  public void print()
    throws ParseException
  {
    String configName = loginConfigName();
    Integer billId = getParaToInt("billId");
    Integer draftId = getParaToInt("draftId");
    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "销售换货单");
    List<String> headData = new ArrayList();
    Integer unitId = getParaToInt("unit.id");
    setHeadUnitData(headData, unitId);
    headData.add("发货仓库 :" + getPara("outStorage.fullName", ""));
    
    headData.add("往来单位 :" + getPara("unit.fullName", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门 :" + getPara("department.fullName", ""));
    headData.add("入库仓库:" + getPara("inStorage.fullName", ""));
    String recodeDate = DateUtils.format(getParaToDate("sellBarterBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    

    headData.add("摘要:" + getPara("sellBarterBill.remark", ""));
    headData.add("附加说明 :" + getPara("sellBarterBill.memo", ""));
    headData.add("单据编号:" + getPara("sellBarterBill.code", ""));
    
    headData.add("收款账户:" + getPara("payTypeAccounts", ""));
    headData.add("收款金额:" + getPara("sellBarterBill.payMoney", ""));
    headData.add("优惠金额:" + getPara("sellBarterBill.privilege", ""));
    headData.add("优惠后金额:" + getPara("sellBarterBill.privilegeMoney", ""));
    headData.add("换货差额:" + getPara("sellBarterBill.gapMoney", ""));
    Integer userId = null;
    if ((billId != null) && (billId.intValue() != 0))
    {
      Model bill = SellBarterBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    else if ((draftId != null) && (draftId.intValue() != 0))
    {
      Model bill = SellBarterDraftBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    setBillUser(headData, userId);
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("类型");
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("单位");
    colTitle.add("生产日期");
    colTitle.add("到期日期");
    colTitle.add("批号");
    colTitle.add("辅助数量");
    colTitle.add("基本数量");
    colTitle.add("辅助数量1");
    colTitle.add("辅助数量2");
    colTitle.add("数量");
    colTitle.add("单价");
    colTitle.add("金额");
    colTitle.add("折扣");
    colTitle.add("折后单价");
    colTitle.add("折后金额");
    colTitle.add("税率");
    colTitle.add("含税单价");
    colTitle.add("税额");
    colTitle.add("含税金额");
    colTitle.add("零售价");
    colTitle.add("零售金额");
    colTitle.add("备注");
    colTitle.add("状态");
    colTitle.add("条码");
    colTitle.add("辅助单位");
    colTitle.add("附加信息");
    
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
    

    List<Model> detailInList = ModelUtils.batchInjectSortObjModel(getRequest(), SellBarterDetail.class, "sellBarterInDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailInList = SellBarterDetail.dao.getListByBillId(configName, billId, Integer.valueOf(1));
    }
    for (int i = 0; i < detailInList.size(); i++)
    {
      rowData.add("入库");
      Model detail = (Model)detailInList.get(i);
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      Integer storageId = detail.getInt("storageId");
      Storage storage = (Storage)Storage.dao.findById(configName, storageId);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
      rowData.add(trimNull(product.getStr("field")));
      if (storage != null)
      {
        rowData.add(trimNull(storage.getStr("code")));
        rowData.add(trimNull(storage.getStr("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Integer selectUnitId = detail.getInt("selectUnitId");
      rowData.add(trimNull(DwzUtils.getSelectUnit(product, selectUnitId)));
      rowData.add(trimNull(detail.getDate("produceDate")));
      rowData.add(trimNull(detail.getDate("produceEndDate")));
      rowData.add(trimNull(detail.getStr("batch")));
      rowData.add(DwzUtils.helpAmount(detail.getBigDecimal("amount"), product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(1)) + trimNull(product.getStr("calculateUnit1")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(2)) + trimNull(product.getStr("calculateUnit2")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(3)) + trimNull(product.getStr("calculateUnit3")));
      rowData.add(trimNull(detail.getBigDecimal("amount")));
      rowData.add(trimNull(detail.getBigDecimal("price")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.getBigDecimal("discount")));
      rowData.add(trimNull(detail.getBigDecimal("discountPrice")));
      rowData.add(trimNull(detail.getBigDecimal("discountMoney")));
      rowData.add(trimNull(detail.getBigDecimal("taxRate")));
      rowData.add(trimNull(detail.getBigDecimal("taxPrice")));
      rowData.add(trimNull(detail.getBigDecimal("taxes")));
      rowData.add(trimNull(detail.getBigDecimal("taxMoney")));
      setRetailPrice(rowData, detail, product);
      rowData.add(trimNull(detail.get("memo")));
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      rowData.add(status);
      rowData.add(trimNull(product.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(DwzUtils.helpUnit(product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(detail.getStr("message")));
    }
    if ((detailInList == null) || (detailInList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    List<Model> detailOutList = ModelUtils.batchInjectSortObjModel(getRequest(), SellBarterDetail.class, "sellBarterOutDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailOutList = SellBarterDetail.dao.getListByBillId(configName, billId, Integer.valueOf(2));
    }
    for (int i = 0; i < detailOutList.size(); i++)
    {
      rowData.add("出库");
      Model detail = (Model)detailOutList.get(i);
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      Integer storageId = detail.getInt("storageId");
      Storage storage = (Storage)Storage.dao.findById(configName, storageId);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
      rowData.add(trimNull(product.getStr("field")));
      if (storage != null)
      {
        rowData.add(trimNull(storage.getStr("code")));
        rowData.add(trimNull(storage.getStr("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Integer selectUnitId = detail.getInt("selectUnitId");
      rowData.add(trimNull(DwzUtils.getSelectUnit(product, selectUnitId)));
      rowData.add(trimNull(detail.getDate("produceDate")));
      rowData.add(trimNull(detail.getDate("produceEndDate")));
      rowData.add(trimNull(detail.getStr("batch")));
      rowData.add(DwzUtils.helpAmount(detail.getBigDecimal("amount"), product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(1)) + trimNull(product.getStr("calculateUnit1")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(2)) + trimNull(product.getStr("calculateUnit2")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(3)) + trimNull(product.getStr("calculateUnit3")));
      rowData.add(trimNull(detail.getBigDecimal("amount")));
      rowData.add(trimNull(detail.getBigDecimal("price")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.getBigDecimal("discount")));
      rowData.add(trimNull(detail.getBigDecimal("discountPrice")));
      rowData.add(trimNull(detail.getBigDecimal("discountMoney")));
      rowData.add(trimNull(detail.getBigDecimal("taxRate")));
      rowData.add(trimNull(detail.getBigDecimal("taxPrice")));
      rowData.add(trimNull(detail.getBigDecimal("taxes")));
      rowData.add(trimNull(detail.getBigDecimal("taxMoney")));
      setRetailPrice(rowData, detail, product);
      rowData.add(trimNull(detail.get("memo")));
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      rowData.add(status);
      rowData.add(trimNull(product.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(DwzUtils.helpUnit(product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(detail.getStr("message")));
    }
    if ((detailOutList == null) || (detailOutList.size() == 0)) {
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
      rowData.add(trimNull(retailPrice));
      rowData.add(trimNull(BigDecimalUtils.mul(detail.getBigDecimal("amount"), retailPrice)));
    }
    else
    {
      rowData.add(trimNull(detail.getBigDecimal("retailPrice")));
      rowData.add(trimNull(detail.getBigDecimal("retailMoney")));
    }
  }
  
  public static List<Record> getOtherAccount(BigDecimal taxes, BigDecimal shouldPay, BigDecimal sellIncome, BigDecimal costMoney)
  {
    List<Record> accountList = new ArrayList();
    Record tax = new Record();
    tax.set("moneyType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    tax.set("accountType", Integer.valueOf(AioConstants.ACCOUNT_TYPE0));
    tax.set("arapAccount", Integer.valueOf(AioConstants.ARAPACCOUNT0));
    tax.set("money", taxes);
    tax.set("account", "00016");
    accountList.add(tax);
    Record should = new Record();
    should.set("moneyType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    should.set("accountType", Integer.valueOf(AioConstants.ACCOUNT_TYPE3));
    should.set("arapAccount", Integer.valueOf(AioConstants.ARAPACCOUNT0));
    should.set("money", shouldPay);
    should.set("account", "000413");
    accountList.add(should);
    Record income = new Record();
    income.set("moneyType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    income.set("accountType", Integer.valueOf(AioConstants.ACCOUNT_TYPE0));
    income.set("arapAccount", Integer.valueOf(AioConstants.ARAPACCOUNT0));
    income.set("money", sellIncome);
    income.set("account", "00019");
    accountList.add(income);
    Record cost = new Record();
    cost.set("moneyType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    cost.set("accountType", Integer.valueOf(AioConstants.ACCOUNT_TYPE0));
    cost.set("arapAccount", Integer.valueOf(AioConstants.ARAPACCOUNT0));
    cost.set("money", costMoney);
    cost.set("account", "00033");
    accountList.add(cost);
    
    return accountList;
  }
  
  public void setStockPrice()
  {
    List<ResultHelp> list = (List)getSessionAttr("costList");
    List<ResultHelp> recordList = new ArrayList();
    for (ResultHelp resultHelp : list)
    {
      BigDecimal avgPrice = resultHelp.getAvgPrice();
      if ((avgPrice == null) || (BigDecimalUtils.compare(avgPrice, BigDecimal.ZERO) == 0)) {
        recordList.add(resultHelp);
      }
    }
    removeSessionAttr("costList");
    setAttr("recordList", recordList);
    render("stockCostPriceOption.html");
  }
}
