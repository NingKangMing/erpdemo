package com.aioerp.controller.sell.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AvgpriceConTroller;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.reports.finance.arap.ArapRecordsController;
import com.aioerp.controller.sell.sellbook.SellBookDetailController;
import com.aioerp.controller.stock.StockController;
import com.aioerp.controller.stock.StockRecordsController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.StockComfirmUtil;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.fz.MinSellPrice;
import com.aioerp.model.fz.PriceDiscountTrack;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.sell.sell.SellBill;
import com.aioerp.model.sell.sell.SellDetail;
import com.aioerp.model.sell.sell.SellDraftBill;
import com.aioerp.model.sell.sellbook.SellbookDetail;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.stock.StockDraftRecords;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.BillType;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DeepClone;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class SellController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE4;
  
  public void index()
  {
    String configName = loginConfigName();
    
    billCodeAuto(billTypeId);
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    
    loadOnlyOneBaseInfo(configName);
    

    notEditStaff();
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    Date time = new Date();
    SellBill bill = (SellBill)getModel(SellBill.class);
    List<Model> noMergeDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellDetail.class, "sellDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    String nagetiveStockHasComfirm = getPara("nagetiveStockHasComfirm", "nhas");
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    Date recodeDate = bill.getTimestamp("recodeDate");
    int billStorageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    Map<Integer, Product> productMap = new HashMap();
    



    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    Map<String, Object> mapmap = verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_ADD, getParaToInt("unit.id"), AioConstants.WAY_GET, bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
    if (mapmap != null)
    {
      renderJson(mapmap);
      return;
    }
    boolean hasOther = codeIsExist(configName, BillType.getValue1(configName, billTypeId, "biillTableName"), "code", bill.getStr("code"), 0);
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
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    String waybillnumber = getPara("sellBill.waybillnumber", "");
    if (!waybillnumber.equals(""))
    {
      boolean kuaiHasOther = codeIsExist(configName, BillType.getValue1(configName, billTypeId, "biillTableName"), "waybillnumber", waybillnumber, 0);
      if (kuaiHasOther)
      {
        setAttr("message", "运单号已经存在!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    if (unitId == 0)
    {
      setAttr("message", "请选购买单位!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    if (noMergeDetailList.size() == 0)
    {
      setAttr("message", "请选择要商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Boolean isNotUnPrice = SysConfig.getConfig(configName, 7);
    for (int i = 0; i < noMergeDetailList.size(); i++)
    {
      Model detail = (Model)noMergeDetailList.get(i);
      String trIndex = String.valueOf(StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1));
      Integer productId = detail.getInt("productId");
      Integer selectUnitId = detail.getInt("selectUnitId");
      BigDecimal amount = detail.getBigDecimal("amount");
      Product product = (Product)Product.dao.findById(configName, productId);
      productMap.put(productId, product);
      ComFunController.detailOrderDefualPriceMoney(detail);
      BigDecimal taxPrice = detail.getBigDecimal("taxPrice");
      
      Map<String, Object> map = ComFunController.setDetailStorageId(detail, amount, trIndex, Integer.valueOf(billStorageId), "storageId", null, null);
      if (Integer.valueOf(map.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(map);
        return;
      }
      if (isNotUnPrice.booleanValue())
      {
        MinSellPrice MinSellPriceObj = MinSellPrice.dao.getMinObj(configName, Integer.valueOf(billStorageId), productId.intValue(), selectUnitId.intValue());
        if (MinSellPriceObj != null)
        {
          BigDecimal minSellPrice = MinSellPriceObj.getBigDecimal("minSellPrice");
          if ((minSellPrice != null) && (BigDecimalUtils.compare(taxPrice, minSellPrice) == -1))
          {
            setAttr("message", "第" + trIndex + "行商品价格低于最低售价不能过账!");
            setAttr("statusCode", AioConstants.HTTP_RETURN300);
            renderJson();
            return;
          }
        }
      }
      detail.set("baseAmount", DwzUtils.getConversionAmount(amount, selectUnitId, product, Integer.valueOf(1)));
      detail.set("basePrice", DwzUtils.getConversionPrice(taxPrice, selectUnitId, product, Integer.valueOf(1)));
      detail.put("trIndex", trIndex);
      detail.put("costPrice", ((HelpUtil)helpUitlList.get(i)).getCostPrice());
    }
    List<Model> mergeDetailList = (List)DeepClone.deepClone(noMergeDetailList);
    mergeDetailList = ComFunController.productMerge(mergeDetailList, productMap);
    
    Boolean isNegativeStock = SysConfig.getConfig(configName, 1);
    Boolean isUnPriceConfim = SysConfig.getConfig(configName, 6);
    String unPriceConfim = getPara("unPriceConfim", "123");
    List<Map<String, String>> underInPriceList = new ArrayList();
    List<Map<String, String>> underMinSellPriceList = new ArrayList();
    List<Map<String, String>> underCostPriceList = new ArrayList();
    List<StockComfirmUtil> negetiveStockList = new ArrayList();
    for (int i = 0; i < mergeDetailList.size(); i++)
    {
      Model detail = (Model)mergeDetailList.get(i);
      int storageId = detail.getInt("storageId").intValue();
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)productMap.get(Integer.valueOf(productId));
      BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
      BigDecimal costPrice = detail.getBigDecimal("costPrice");
      BigDecimal stockAmount = BigDecimal.ZERO;
      int costArith = product.getInt("costArith").intValue();
      Integer selectUnitId = detail.getInt("selectUnitId");
      int notStockFlag = 0;
      if (costArith == AioConstants.PRD_COST_PRICE4)
      {
        stockAmount = Stock.dao.getStockAmount(configName, productId, storageId, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
      }
      else if (costArith == AioConstants.PRD_COST_PRICE1)
      {
        Map<String, String> map = ComFunController.outProductGetCostPrice(configName, storageId, product, costPrice, selectUnitId.intValue());
        stockAmount = new BigDecimal((String)map.get("stockAmount"));
        costPrice = new BigDecimal((String)map.get("costPrice"));
        notStockFlag = Integer.valueOf((String)map.get("notStockFlag")).intValue();
      }
      detail.set("costPrice", costPrice);
      detail.set("costMoneys", BigDecimalUtils.mul(baseAmount, costPrice));
      setCostPrice(noMergeDetailList, productId, "costPrice", costPrice);
      
      ComFunController.sellPriceValidate(configName, isUnPriceConfim.booleanValue(), unPriceConfim, storageId, product, selectUnitId.intValue(), detail.getBigDecimal("taxPrice"), costPrice, detail.getStr("trIndex"), underInPriceList, underMinSellPriceList, underCostPriceList);
      if (stockAmount == null) {
        stockAmount = BigDecimal.ZERO;
      }
      if (BigDecimalUtils.compare(stockAmount, baseAmount) < 0) {
        if ((isNegativeStock.booleanValue()) && (costArith == AioConstants.PRD_COST_PRICE1))
        {
          ComFunController.negetiveStockData(negetiveStockList, detail.getStr("trIndex"), storageId, product, costArith, selectUnitId.intValue(), stockAmount, baseAmount, notStockFlag);
        }
        else
        {
          setAttr("message", "第" + detail.getStr("trIndex") + "行商品<br/>商品编号：" + product.getStr("code") + ",<br/>商品全名：" + product.getStr("fullName") + ",<br/>数量" + baseAmount + ",超出库存" + stockAmount + "!");
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
      }
      ((Model)mergeDetailList.get(i)).remove("trIndex");
    }
    if (isUnPriceConfim.booleanValue())
    {
      Map<String, Object> map = ComFunController.sellPriceValidateInfo(String.valueOf(getAttr("base")), underInPriceList, underMinSellPriceList, underCostPriceList);
      if (map.get("underPriceList") != null)
      {
        List<Map<String, String>> underPriceList = (List)map.get("underPriceList");
        setSessionAttr("underPriceList", underPriceList);
        map.remove("underPriceList");
        renderJson(map);
        return;
      }
    }
    if (("nhas".equals(nagetiveStockHasComfirm)) && 
      (isNegativeStock.booleanValue()) && (negetiveStockList.size() > 0) && 
      (SysConfig.getConfig(configName, 15).booleanValue()))
    {
      Map<String, Object> map = ComFunController.negetiveStockDataInfo(String.valueOf(getAttr("base")), negetiveStockList);
      renderJson(map);
      return;
    }
    bill.set("deliveryCompanyCode", getPara("deliveryCompany.code", ""));
    bill.set("deliveryCompany", getPara("deliveryCompany.name", ""));
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    bill.set("unitId", Integer.valueOf(unitId));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("storageId", Integer.valueOf(billStorageId));
    bill.set("status", Integer.valueOf(AioConstants.BILL_STATUS3));
    bill.set("relStatus", Integer.valueOf(AioConstants.STATUS_DISABLE));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("updateTime", time);
    if ((AioConstants.VERSION == 1) || (AioConstants.IS_FREE_VERSION == "yes")) {
      bill.set("delayDeliveryDate", time);
    } else {
      bill.set("delayDeliveryDate", bill.getDate("deliveryDate"));
    }
    bill.set("isWarn", Integer.valueOf(AioConstants.OVERGETPAY_WARN0));
    ComFunController.billOrderDefualPriceMoney(bill);
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (draftId != 0) {
      billCodeIncrease(bill, "draftPost");
    } else {
      billCodeIncrease(bill, "add");
    }
    bill.set("id", null);
    bill.save(configName);
    
    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("taxMoneys"));
    
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    


    BigDecimal sellCosts = BigDecimal.ZERO;
    List<Record> proAccountList = new ArrayList();
    

    boolean hasOm = ComFunController.hasOpenOm(configName);
    List<Record> sellbookDetailList = new ArrayList();
    String sellbookDetailIds = "";
    for (int i = 0; i < noMergeDetailList.size(); i++)
    {
      Record proAccount = new Record();
      Model detail = (Model)noMergeDetailList.get(i);
      int storageId = detail.getInt("storageId").intValue();
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)productMap.get(Integer.valueOf(productId));
      int costArith = product.getInt("costArith").intValue();
      BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
      BigDecimal costPrice = detail.getBigDecimal("costPrice");
      BigDecimal costMoneys = BigDecimalUtils.mul(baseAmount, costPrice);
      detail.set("costMoneys", costMoneys);
      detail.set("billId", bill.getInt("id"));
      detail.set("untreatedAmount", baseAmount);
      detail.set("relStatus", Integer.valueOf(AioConstants.STATUS_DISABLE));
      detail.set("updateTime", time);
      detail.set("id", null);
      detail.save(configName);
      

      StockRecordsController.addRecords(configName, billTypeId, "out", bill, detail, BigDecimalUtils.div(costMoneys, baseAmount), baseAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
      
      PriceDiscountTrack.addPriceDiscountTrack(configName, "sell", unitId, productId, detail.getInt("selectUnitId"), detail.getBigDecimal("price"), detail.getBigDecimal("discount"));
      
      AvgpriceConTroller.addAvgprice(configName, "out", Integer.valueOf(storageId), Integer.valueOf(productId), baseAmount, costMoneys);
      
      StockController.stockSubChange(configName, costArith, baseAmount, storageId, productId, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"), null);
      
      Integer sellbookDetailId = detail.getInt("detailId");
      if (sellbookDetailId != null)
      {
        SellbookDetail sellbookDetail = (SellbookDetail)SellbookDetail.dao.findById(configName, sellbookDetailId);
        if (sellbookDetail != null)
        {
          SellBookDetailController.editDetailAmount(configName, baseAmount, sellbookDetail);
          if (hasOm) {
            sellbookDetailIds = ComFunController.hasDelivery(sellbookDetailList, sellbookDetailIds, sellbookDetail.getInt("id").intValue(), detail.getBigDecimal("baseAmount"));
          }
        }
      }
      sellCosts = BigDecimalUtils.add(sellCosts, costMoneys);
      proAccount.set("productId", Integer.valueOf(productId));
      proAccount.set("costMoney", costMoneys);
      proAccountList.add(proAccount);
    }
    if ((hasOm) && (sellbookDetailList.size() > 0))
    {
      Map<String, Object> map = ComFunController.hasDeliveryInfo(configName, sellbookDetailList, sellbookDetailIds, bill.getStr("waybillnumber"), bill.getStr("deliveryCompanyCode"), bill.getStr("deliveryCompany"));
      if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
      {
        commonRollBack();
        renderJson(map);
        return;
      }
    }
    BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney"));
    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, unitId, null, needGetOrPay);
    
    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), null, bill, null, Integer.valueOf(unitId), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), needGetOrPay, bill.getBigDecimal("payMoney"));
    
    Map<String, Object> record = new HashMap();
    record.put("sellCosts", sellCosts);
    record.put("taxes", bill.getBigDecimal("taxes"));
    record.put("sellMoneys", bill.getBigDecimal("discountMoneys"));
    record.put("needGetOrPay", needGetOrPay);
    record.put("proAccountList", proAccountList);
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), Integer.valueOf(unitId), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), bill.getBigDecimal("privilege"), payTypeIds, payTypeMoneys, record);
    

    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      SellDraftBill.dao.deleteById(configName, draftBillId);
      BaseDbModel.delDetailByBillId(configName, "xs_sell_draft_detail", draftBillId);
      PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(billTypeId));
      
      StockDraftRecords.delRecords(configName, billTypeId, draftBillId.intValue());
      
      PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(billTypeId));
      setDraftStrs();
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "sell_info");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    printBeforSave1();
    
    renderJson();
  }
  
  public static Map<String, Object> doRCW(String configName, int billId)
    throws SQLException
  {
    Map<String, Object> map = new HashMap();
    int oldBillId = 0;
    int newBillId = 0;
    
    Model bill = SellBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "销售单已经不存在！");
      return map;
    }
    Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unitId"), AioConstants.WAY_GET, bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
    if (vmap != null)
    {
      map = vmap;
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      return map;
    }
    List<Model> dateilList = SellDetail.dao.getList2(configName, Integer.valueOf(billId), BillType.getValue1(configName, billTypeId, "billDetailTableName"));
    if ((dateilList == null) || (dateilList.size() == 0))
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "销售单商品信息已经不存在！");
      return map;
    }
    Date time = new Date();
    try
    {
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
      oldBillId = bill.getInt("id").intValue();
      bill.update(configName);
      bill.set("id", null);
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
      bill.set("updateTime", time);
      
      billRcwAddRemark(bill, null);
      bill.save(configName);
      newBillId = bill.getInt("id").intValue();
      
      BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("taxMoneys"));
      for (int i = 0; i < dateilList.size(); i++)
      {
        Model detail = (Model)dateilList.get(i);
        Product product = (Product)((Model)dateilList.get(i)).get("product");
        Integer storageId = detail.getInt("storageId");
        int productId = detail.getInt("productId").intValue();
        BigDecimal litterAmount = detail.getBigDecimal("baseAmount");
        BigDecimal costMoneys = detail.getBigDecimal("costMoneys");
        BigDecimal litterCostPrice = BigDecimalUtils.div(costMoneys, litterAmount);
        detail.set("rcwId", detail.getInt("id"));
        detail.set("id", null);
        detail.set("billId", bill.getInt("id"));
        detail.save(configName);
        

        StockRecordsController.addRecords(configName, billTypeId, "in", bill, detail, litterCostPrice, litterAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
        
        StockController.stockChange(configName, "in", storageId.intValue(), productId, product.getInt("costArith").intValue(), litterAmount, litterCostPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        
        AvgpriceConTroller.addAvgprice(configName, "in", storageId, Integer.valueOf(productId), litterAmount, costMoneys);
        
        SellBookDetailController.rcwDetailAmount(configName, litterAmount, detail.getInt("detailId"));
      }
      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
      
      ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
      
      PayTypeController.rcwAccountsRecoder(configName, billTypeId, Integer.valueOf(oldBillId), Integer.valueOf(newBillId));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack(configName);
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "系统异常,请联系管理员！");
      return map;
    }
    map.put("status", Integer.valueOf(AioConstants.RCW_BY));
    return map;
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int orderId = getParaToInt(0, Integer.valueOf(0)).intValue();
    boolean isRCW = false;
    
    Model bill = SellBill.dao.findById(configName, Integer.valueOf(orderId));
    Integer orderIsRedRow = bill.getInt("isRCW");
    if (orderIsRedRow == null) {
      orderIsRedRow = Integer.valueOf(0);
    }
    List<Model> detailList = SellDetail.dao.getList2(configName, Integer.valueOf(orderId), BillType.getValue1(configName, billTypeId, "billDetailTableName"));
    
    detailList = addTrSize(detailList, 15);
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    if (orderIsRedRow.intValue() == AioConstants.RCW_VS) {
      isRCW = true;
    } else {
      isRCW = false;
    }
    setPayTypeAttr(configName, "saveBill", billTypeId, bill.getInt("id").intValue());
    
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("isRCW", Boolean.valueOf(isRCW));
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
    render("/WEB-INF/template/sell/sell/look.html");
  }
  
  public void negativeStockShowDialog()
  {
    String configName = loginConfigName();
    
    String modelType = getPara("param[modelType]", "");
    String trIndexs = getPara("param[trIndexs]", "");
    String productIds = getPara("param[productIds]", "");
    String storageIds = getPara("param[storageIds]");
    String stockAmounts = getPara("param[stockAmounts]");
    String negativeStockAmounts = getPara("param[negativeStockAmounts]");
    
    String costAriths = getPara("param[costAriths]");
    String selectUnitIds = getPara("param[selectUnitIds]");
    String prdUnitNames = getPara("param[prdUnitNames]");
    String notStocks = getPara("param[notStocks]");
    
    List<Model> list = Storage.dao.loadNegativeStockInfo(configName, trIndexs, productIds, storageIds, stockAmounts, negativeStockAmounts);
    setAttr("prdAndStoRecords", list);
    setAttr("basePath", getAttr("base"));
    setAttr("modelType", modelType);
    setAttr("trIndexs", trIndexs);
    setAttr("productIds", productIds);
    setAttr("negativeStockAmounts", negativeStockAmounts);
    setAttr("costAriths", costAriths);
    setAttr("selectUnitIds", selectUnitIds);
    setAttr("prdUnitNames", prdUnitNames);
    setAttr("notStocks", notStocks);
    render("negativeStockOption.html");
  }
  
  public void getNagetiveStockPrice()
    throws UnsupportedEncodingException
  {
    String type = getPara("param[modelType]", "");
    String trIndexs = getPara("param[trIndexs]", "");
    String selectUnitIds = getPara("param[selectUnitIds]", "");
    String prdCodes = getPara("param[prdCodes]", "");
    String prdFullNames = getPara("param[prdFullNames]", "");
    String prdUnitNames = getPara("param[prdUnitNames]");
    String costAriths = getPara("param[costAriths]");
    String notStocks = getPara("param[notStocks]");
    
    List<Record> recordList = new ArrayList();
    String[] trIndexArra = trIndexs.split(":");
    String[] selectUnitIdArra = selectUnitIds.split(":");
    String[] notStockArra = notStocks.split(":");
    String[] prdCodeArra = prdCodes.split(":");
    String[] prdFullNameArra = prdFullNames.split(":");
    String[] prdUnitNameArra = prdUnitNames.split(":");
    String[] costArithArra = costAriths.split(":");
    Record record = null;
    for (int i = 0; i < notStockArra.length; i++) {
      if ("2".equals(notStockArra[i]))
      {
        record = new Record();
        record.set("type", type);
        record.set("trIndex", trIndexArra[i]);
        record.set("selectUnitIds", selectUnitIdArra[i]);
        record.set("prdCode", URLDecoder.decode(prdCodeArra[i], "utf-8"));
        record.set("prdFullName", URLDecoder.decode(prdFullNameArra[i], "utf-8"));
        record.set("prdUnitNmae", URLDecoder.decode(prdUnitNameArra[i], "utf-8"));
        record.set("costArith", costArithArra[i]);
        recordList.add(record);
      }
    }
    setAttr("recordList", recordList);
    render("stockCostPriceOption.html");
  }
  
  public void option()
  {
    String configName = loginConfigName();
    
    Integer unitId = getParaToInt("unitId", Integer.valueOf(0));
    setAttr("unitId", unitId);
    SellBill bill = (SellBill)getModel(SellBill.class);
    Map<String, Object> billMap = new HashMap();
    billMap.put("unitPrivs", basePrivs(UNIT_PRIVS));
    billMap.put("staffPrivs", basePrivs(STAFF_PRIVS));
    billMap.put("departmentPrivs", basePrivs(DEPARTMENT_PRIVS));
    billMap.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    
    billMap.put("bill", bill);
    billMap.put("startDate", getPara("startDate"));
    billMap.put("endDate", getPara("endDate"));
    billMap.put("unitName", getPara("unit.fullName"));
    billMap.put("staffName", getPara("staff.name"));
    billMap.put("storageName", getPara("storage.fullName"));
    billMap.put("productName", getPara("product.fullName"));
    billMap.put("detailMemo", getPara("detail.memo"));
    
    List<Model> billList = SellBill.dao.getList(configName, Integer.valueOf(AioConstants.STATUS_DISABLE), unitId, billMap);
    if (billList.size() == 0)
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "没有符合条件的数据!");
      setAttr("callbackType", "closeCurrent");
      renderJson();
      return;
    }
    if (billList.size() > 0)
    {
      setAttr("detailList", SellDetail.dao.getList(configName, ((Model)((Model)billList.get(0)).get("bill")).getInt("id"), basePrivs(PRODUCT_PRIVS), basePrivs(STORAGE_PRIVS)));
      setAttr("billId", ((Model)billList.get(0)).getInt("id"));
    }
    setAttr("billList", billList);
    
    setAttr("unitId", unitId);
    setAttr("bill", bill);
    setAttr("startDate", getPara("startDate"));
    setAttr("endDate", getPara("endDate"));
    setAttr("unitName", getPara("unit.fullName"));
    setAttr("staffName", getPara("staff.name"));
    setAttr("storageName", getPara("storage.fullName"));
    setAttr("productName", getPara("product.fullName"));
    setAttr("detailMemo", getPara("detail.memo"));
    

    setAttr("billList", billList);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "xsd", AioConstants.STATUS_ENABLE));
    setAttr("detailRowList", ReportRow.dao.getListByType(configName, "xsdyy", AioConstants.STATUS_ENABLE));
    
    render("option.html");
  }
  
  public void detail()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    
    setAttr("detailList", SellDetail.dao.getList(configName, Integer.valueOf(billId), basePrivs(PRODUCT_PRIVS), basePrivs(STORAGE_PRIVS)));
    setAttr("bageBreak", "sellDateilTable");
    setAttr("detailRowList", ReportRow.dao.getListByType(configName, "xsdyy", AioConstants.STATUS_ENABLE));
    
    render("detailList.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    String configName = loginConfigName();
    if (isPost())
    {
      if (getPara("userSearchDate", "").equals("checked")) {
        SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), getPara("startDate"), getPara("endDate"));
      }
      SellBill bill = (SellBill)getModel(SellBill.class);
      Map<String, Object> billMap = new HashMap();
      int unitId = bill.getInt("unitId").intValue();
      billMap.put("bill", bill);
      billMap.put("startDate", getPara("startDate"));
      billMap.put("endDate", getPara("endDate"));
      billMap.put("unitName", getPara("unit.fullName"));
      billMap.put("staffName", getPara("staff.name"));
      billMap.put("storageName", getPara("storage.fullName"));
      billMap.put("productName", getPara("product.fullName"));
      billMap.put("detailMemo", getPara("detail.memo"));
      
      billMap.put("unitPrivs", basePrivs(UNIT_PRIVS));
      billMap.put("staffPrivs", basePrivs(STAFF_PRIVS));
      billMap.put("departmentPrivs", basePrivs(DEPARTMENT_PRIVS));
      billMap.put("storagePrivs", basePrivs(STORAGE_PRIVS));
      
      List<Model> billList = SellBill.dao.getList(configName, Integer.valueOf(AioConstants.STATUS_DISABLE), Integer.valueOf(unitId), billMap);
      if (billList.size() > 0) {
        setAttr("detailList", SellDetail.dao.getList(configName, ((Model)((Model)billList.get(0)).get("bill")).getInt("id"), basePrivs(PRODUCT_PRIVS), basePrivs(STORAGE_PRIVS)));
      }
      setAttr("bill", bill);
      setAttr("startDate", getPara("startDate"));
      setAttr("endDate", getPara("endDate"));
      setAttr("unitName", getPara("unit.fullName"));
      setAttr("staffName", getPara("staff.name"));
      setAttr("storageName", getPara("storage.fullName"));
      setAttr("productName", getPara("product.fullName"));
      setAttr("detailMemo", getPara("detail.memo"));
      
      setAttr("billList", billList);
      setAttr("rowList", ReportRow.dao.getListByType(configName, "xsd", AioConstants.STATUS_ENABLE));
      setAttr("detailRowList", ReportRow.dao.getListByType(configName, "xsdyy", AioConstants.STATUS_ENABLE));
      
      render("option.html");
    }
    else
    {
      setAttr("unitId", getParaToInt("unitId", Integer.valueOf(0)));
      setStartDateAndEndDate();
      render("dialogSearch.html");
    }
  }
  
  public void underPricePrompt()
  {
    Integer way = getParaToInt("way");
    String wayName = getPara("wayName");
    List<Map<String, String>> list = (List)getSessionAttr("underPriceList");
    
    setAttr("rList", list);
    setAttr("way", way);
    setAttr("wayName", wayName);
    render("underPriceOption.html");
  }
  
  public static void setCostPrice(List<Model> noMergeDetail, int productId, String attr, BigDecimal costPrice)
  {
    for (int j = 0; j < noMergeDetail.size(); j++) {
      if (((Model)noMergeDetail.get(j)).getInt("productId").intValue() == productId) {
        ((Model)noMergeDetail.get(j)).set(attr, costPrice);
      }
    }
  }
  
  public static void setTrIndexHelpCostPrice(List<HelpUtil> helpUitlList, String indexTr, BigDecimal costPrice)
  {
    String[] index = indexTr.split(",");
    for (int i = 0; i < index.length; i++) {
      ((HelpUtil)helpUitlList.get(Integer.valueOf(index[i]).intValue() - 1)).setCostPrice(costPrice);
    }
  }
  
  public void print()
  {
    Map<String, Object> data = new HashMap();
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "销售单");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    Map<String, Object> billData = new HashMap();
    Model bill = null;
    List<Model> detailList = null;
    

    Integer billId = Integer.valueOf(0);
    
    String actionType = getPara("actionType", "");
    if (actionType.equals("look"))
    {
      bill = SellBill.dao.findById(configName, getParaToInt("bill.id", Integer.valueOf(0)));
      detailList = SellDetail.dao.getList1(configName, bill.getInt("id"));
      billId = bill.getInt("id");
    }
    else
    {
      bill = (Model)getModel(SellBill.class);
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("staffId", getParaToInt("staff.id", Integer.valueOf(0)));
      bill.set("departmentId", getParaToInt("department.id", Integer.valueOf(0)));
      bill.set("storageId", getParaToInt("storage.id", Integer.valueOf(0)));
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellDetail.class, "sellDetail");
    }
    billData.put("tableName", BillType.getValue1(configName, billTypeId, "biillTableName"));
    billData.put("base", getAttr("base"));
    Integer id = bill.getInt("id");
    if ((id != null) && (id.intValue() != 0)) {
      billData.put("id", id);
    }
    boolean flag = printBeforSave2(data, billId).booleanValue();
    if (flag)
    {
      renderJson(data);
      return;
    }
    Model unit = Unit.dao.findById(configName, bill.getInt("unitId"));
    Model staff = Staff.dao.findById(configName, bill.getInt("staffId"));
    Model department = Department.dao.findById(configName, bill.getInt("departmentId"));
    Model storage = Storage.dao.findById(configName, bill.getInt("storageId"));
    String unitFullName = unit == null ? "" : unit.getStr("fullName");
    String staffFullName = staff == null ? "" : staff.getStr("fullName");
    String departmentName = department == null ? "" : department.getStr("fullName");
    String storageFullName = storage == null ? "" : storage.getStr("fullName");
    


    printCommonData(headData);
    headData.add("录单日期:" + trimRecordDateNull(bill.getDate("recodeDate")));
    headData.add("单据编号:" + trimNull(bill.getStr("code")));
    headData.add("购买单位 :" + trimNull(unitFullName));
    printUnitCommonData(headData, unit);
    headData.add("经手人 :" + trimNull(staffFullName));
    headData.add("部门 :" + trimNull(departmentName));
    headData.add("收款日期:" + trimNull(bill.getDate("deliveryDate")));
    headData.add("发货仓库:" + trimNull(storageFullName));
    headData.add("摘要:" + trimNull(bill.getStr("remark")));
    headData.add("附加说明 :" + trimNull(bill.getStr("memo")));
    headData.add("整单折扣 :" + trimNull(bill.getBigDecimal("discounts")));
    headData.add("收款账户 :" + trimNull(bill.getStr("bankPay")));
    headData.add("收款金额 :" + trimNull(bill.getBigDecimal("payMoney")));
    headData.add("优惠金额 :" + trimNull(bill.getBigDecimal("privilege")));
    headData.add("优惠后金额 :" + trimNull(bill.getBigDecimal("privilegeMoney")));
    
    setOrderUserId(flag, bill, actionType, headData);
    


    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("商品备注");
    
    colTitle.add("单位");
    colTitle.add("辅助单位");
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
    colTitle.add("备注");
    colTitle.add("状态");
    colTitle.add("到货数量");
    colTitle.add("条码");
    colTitle.add("附加信息");
    
    colTitle.add("零售价");
    colTitle.add("零售金额");
    
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    


    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Product p = (Product)Product.dao.findById(configName, detail.getInt("productId"));
      Integer selectUnitId = detail.getInt("selectUnitId");
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      Integer storageId = detail.getInt("storageId");
      if ((storageId == null) || (storageId.intValue() == 0)) {
        storageId = bill.getInt("storageId");
      }
      Storage s = (Storage)Storage.dao.findById(configName, storageId);
      
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(p.getStr("code")));
      rowData.add(trimNull(p.getStr("fullName")));
      

      rowData.add(trimNull(p.getStr("smallName")));
      rowData.add(trimNull(p.getStr("spell")));
      rowData.add(trimNull(p.getStr("standard")));
      rowData.add(trimNull(p.getStr("model")));
      rowData.add(trimNull(p.getStr("field")));
      rowData.add(trimNull(p.getStr("memo")));
      
      rowData.add(trimNull(DwzUtils.getSelectUnit(p, selectUnitId)));
      rowData.add(DwzUtils.helpUnit(p.getStr("calculateUnit1"), p.getStr("calculateUnit2"), p.getStr("calculateUnit3"), p.getInt("unitRelation1"), p.getBigDecimal("unitRelation2"), p.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(detail.getDate("produceDate")));
      rowData.add(trimNull(detail.getDate("produceEndDate")));
      rowData.add(trimNull(detail.getStr("batch")));
      rowData.add(DwzUtils.helpAmount(detail.getBigDecimal("amount"), p.getStr("calculateUnit1"), p.getStr("calculateUnit2"), p.getStr("calculateUnit3"), p.getBigDecimal("unitRelation2"), p.getBigDecimal("unitRelation3")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, p.getInt("unitRelation1"), p.getBigDecimal("unitRelation2"), p.getBigDecimal("unitRelation3"), Integer.valueOf(1)) + trimNull(p.getStr("calculateUnit1")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, p.getInt("unitRelation1"), p.getBigDecimal("unitRelation2"), p.getBigDecimal("unitRelation3"), Integer.valueOf(2)) + trimNull(p.getStr("calculateUnit2")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, p.getInt("unitRelation1"), p.getBigDecimal("unitRelation2"), p.getBigDecimal("unitRelation3"), Integer.valueOf(3)) + trimNull(p.getStr("calculateUnit3")));
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
      rowData.add(trimNull(detail.getStr("memo")));
      rowData.add(status);
      rowData.add(trimNull(detail.getBigDecimal("arrivalAmount")));
      rowData.add(trimNull(p.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(trimNull(detail.getStr("message")));
      
      ComFunController.setRetailPrice(rowData, detail, p);
      rowData.add(trimNull(s == null ? "" : s.getStr("code")));
      rowData.add(trimNull(s == null ? "" : s.getStr("fullName")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    data.put("billData", billData);
    renderJson(data);
  }
}
