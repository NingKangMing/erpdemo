package com.aioerp.controller.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AvgpriceConTroller;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.reports.finance.arap.ArapRecordsController;
import com.aioerp.controller.stock.StockController;
import com.aioerp.controller.stock.StockRecordsController;
import com.aioerp.controller.sys.ReloadCostPriceController;
import com.aioerp.db.reports.BillHistory;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.bought.BoughtDetail;
import com.aioerp.model.bought.PurchaseBill;
import com.aioerp.model.bought.PurchaseDetail;
import com.aioerp.model.bought.PurchaseDraftBill;
import com.aioerp.model.bought.PurchaseDraftDetail;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.fz.PriceDiscountTrack;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.stock.StockDraftRecords;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
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

public class PurchaseBillController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE5;
  
  public void index()
  {
    String configName = loginConfigName();
    boolean hashasPermitted = hasPermitted("3-302-e");
    boolean checkout = getParaToBoolean("checkout", Boolean.valueOf(false)).booleanValue();
    if ((checkout) && (!hashasPermitted))
    {
      setAttr("message", "该用户没有此项权限!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    if ((checkout) && (hashasPermitted))
    {
      renderJson();
      return;
    }
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
    

    billCodeAuto(billTypeId);
    
    loadOnlyOneBaseInfo(configName);
    

    notEditStaff();
    
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws SQLException, ParseException
  {
    String configName = loginConfigName();
    
    printBeforSave1();
    PurchaseBill bill = (PurchaseBill)getModel(PurchaseBill.class);
    
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    BigDecimal privilegeMoney = bill.getBigDecimal("privilegeMoney");
    BigDecimal payMoney = bill.getBigDecimal("payMoney");
    Map<String, Object> map = verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_ADD, getParaToInt("unit.id"), AioConstants.WAY_PAY, payMoney, privilegeMoney);
    if (map != null)
    {
      renderJson(map);
      return;
    }
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    bill.set("id", null);
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    boolean hasOther = PurchaseBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseDetail.class, "purchaseDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer storageId = getParaToInt("storage.id");
    
    bill.set("unitId", getParaToInt("unit.id"));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    
    bill.set("storageId", storageId);
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (bill.get("discountMoneys") == null) {
      bill.set("discountMoneys", bill.get("moneys"));
    }
    if (bill.get("taxMoneys") == null) {
      bill.set("taxMoneys", bill.get("discountMoneys"));
    }
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    if ((AioConstants.VERSION == 1) || (AioConstants.IS_FREE_VERSION == "yes")) {
      bill.set("delayDeliveryDate", time);
    } else {
      bill.set("delayDeliveryDate", bill.getDate("payDate"));
    }
    bill.set("isWarn", Integer.valueOf(AioConstants.OVERGETPAY_WARN0));
    if (draftId != 0) {
      billCodeIncrease(bill, "draftPost");
    } else {
      billCodeIncrease(bill, "add");
    }
    bill.save(configName);
    

    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("taxMoneys"));
    
    BigDecimal amounts = null;
    BigDecimal moneys = null;
    BigDecimal discountMoneys = null;
    BigDecimal taxMoneys = null;
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      
      detail.set("id", null);
      Integer detailstorage = detail.getInt("storageId");
      if (((storageId == null) || (storageId.intValue() == 0)) && ((detailstorage == null) || (detailstorage.intValue() == 0)))
      {
        commonRollBack();
        int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1);
        
        setAttr("message", "第" + trIndex + "行多仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (detail.get("discountPrice") == null) {
        detail.set("discountPrice", detail.get("price"));
      }
      if (detail.get("taxPrice") == null) {
        detail.set("taxPrice", detail.get("discountPrice"));
      }
      if (detail.getBigDecimal("discount") == null) {
        detail.set("discount", BigDecimal.ONE);
      }
      if (detail.get("discountMoney") == null) {
        detail.set("discountMoney", BigDecimalUtils.mul(BigDecimalUtils.mul(detail.getBigDecimal("amount"), detail.getBigDecimal("price")), detail.getBigDecimal("discount")));
      }
      if (detail.get("taxMoney") == null) {
        detail.set("taxMoney", BigDecimalUtils.add(detail.getBigDecimal("discountMoney"), detail.getBigDecimal("taxes")));
      }
      if ((detailstorage == null) || (detailstorage.intValue() == 0)) {
        detail.set("storageId", storageId);
      }
      Integer productId = detail.getInt("productId");
      Integer bDetailId = detail.getInt("detailId");
      Record record = BoughtDetail.dao.getBill(configName, bDetailId);
      String memo = detail.getStr("memo");
      if (record != null)
      {
        if (StringUtils.isNotBlank(memo)) {
          memo = memo + ";" + record.getStr("code");
        } else {
          memo = record.getStr("code");
        }
        detail.set("memo", memo);
      }
      BigDecimal purchaseAmount = detail.getBigDecimal("amount");
      if (productId != null)
      {
        amounts = BigDecimalUtils.add(amounts, detail.getBigDecimal("amount"));
        moneys = BigDecimalUtils.add(moneys, detail.getBigDecimal("money"));
        discountMoneys = BigDecimalUtils.add(discountMoneys, detail.getBigDecimal("discountMoney"));
        taxMoneys = BigDecimalUtils.add(taxMoneys, detail.getBigDecimal("taxMoney"));
        
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        detail.set("billId", bill.getInt("id"));
        
        detail.set("updateTime", time);
        
        Integer selectUnitId = detail.getInt("selectUnitId");
        Integer detailStorageId = detail.getInt("storageId");
        BigDecimal price = detail.getBigDecimal("price");
        if ((detail.getBigDecimal("discountPrice") != null) && (BigDecimalUtils.compare(detail.getBigDecimal("discountPrice"), BigDecimal.ZERO) != 0)) {
          price = detail.getBigDecimal("discountPrice");
        }
        if (detailStorageId == null) {
          detailStorageId = storageId;
        }
        BigDecimal purchasePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(purchaseAmount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        detail.set("untreatedAmount", baseAmount);
        AvgpriceConTroller.addAvgprice(configName, "in", detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, purchasePrice));
        


        StockController.stockChange(configName, "in", detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, purchasePrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        
        StockRecordsController.addRecords(configName, billTypeId, "in", bill, detail, purchasePrice, baseAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
        


        PriceDiscountTrack.addPriceDiscountTrack(configName, "purchase", bill.getInt("unitId").intValue(), productId.intValue(), detail.getInt("selectUnitId"), detail.getBigDecimal("price"), detail.getBigDecimal("discount"));
        

        detail.set("costPrice", purchasePrice);
        
        Integer boughtDetailId = detail.getInt("detailId");
        if (boughtDetailId != null)
        {
          BoughtDetail boughtDetail = (BoughtDetail)BoughtDetail.dao.findById(configName, boughtDetailId);
          BoughtDetailController.editDetailAmount(configName, baseAmount, boughtDetail);
        }
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          detail.set("giftAmount", baseAmount);
        }
        detail.set("baseAmount", baseAmount);
        detail.set("basePrice", purchasePrice);
        detail.save(configName);
      }
    }
    if (BigDecimalUtils.compare(moneys, bill.getBigDecimal("moneys")) != 0)
    {
      bill.set("amounts", amounts);
      bill.set("moneys", moneys);
      bill.set("discountMoneys", discountMoneys);
      bill.set("taxMoneys", taxMoneys);
      bill.set("privilegeMoney", BigDecimalUtils.sub(taxMoneys, bill.getBigDecimal("privilege")));
      bill.update(configName);
      Model history = BillHistory.dao.getRecordByBillIdAndTypeId(configName, bill.getInt("id"), Integer.valueOf(billTypeId));
      if (history != null)
      {
        history.set("money", taxMoneys);
        history.update(configName);
      }
    }
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    

    Map<String, Object> record = new HashMap();
    record.put("purchaseMoneys", bill.getBigDecimal("taxMoneys"));
    record.put("proAccountList", detailList);
    record.put("payMoney", bill.getBigDecimal("payMoney"));
    record.put("taxes", bill.getBigDecimal("taxes"));
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), bill.getBigDecimal("privilege"), payTypeIds, payTypeMoneys, record);
    

    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
    

    BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney"));
    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, bill.getBigDecimal("payMoney"));
    


    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      setDraftStrs();
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      PurchaseDraftBill.dao.deleteById(configName, draftBillId);
      PurchaseDraftDetail.dao.delByBill(configName, draftBillId);
      PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(billTypeId));
      
      StockDraftRecords.delRecords(configName, billTypeId, draftBillId.intValue());
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "purchaseView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    if (AioConstants.INSERT_BILL) {
     // ReloadCostPriceController.reloadInsertBillCostPrice(configName, new Date(), detailList);
    }
    renderJson();
  }
  
  public void option()
  {
    Integer unitId = getParaToInt("unitId");
    Integer storageId = getParaToInt("storageId");
    Integer staffId = getParaToInt("staffId");
    Integer departmentId = getParaToInt("departmentId");
    String configName = loginConfigName();
    PurchaseBill bill = (PurchaseBill)getModel(PurchaseBill.class);
    Map<String, Object> map = new HashMap();
    map.put("unitId", unitId);
    map.put("storageId", storageId);
    map.put("staffId", staffId);
    map.put("departmentId", departmentId);
    map.put("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    map.put("bill", bill);
    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    map.put("unitName", getPara("unit.fullName"));
    map.put("staffName", getPara("staff.name"));
    map.put("storageName", getPara("storage.fullName"));
    map.put("productName", getPara("product.fullName"));
    map.put("departmentName", getPara("department.fullName"));
    map.put("detailMemo", getPara("purchaseDetail.memo"));
    
    List<Model> billList = PurchaseBill.dao.getList(configName, Integer.valueOf(AioConstants.STATUS_DISABLE), map);
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
      setAttr("detailList", PurchaseDetail.dao.getUntreatedList(configName, ((Model)billList.get(0)).getInt("id")));
      setAttr("billId", ((Model)billList.get(0)).getInt("id"));
    }
    setAttr("unitId", unitId);
    setAttr("storageId", storageId);
    setAttr("staffId", staffId);
    setAttr("departmentId", departmentId);
    setAttr("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    
    setAttr("bill", bill);
    setAttr("startDate", getPara("startDate"));
    setAttr("endDate", getPara("endDate"));
    setAttr("unitName", getPara("unit.fullName"));
    setAttr("staffName", getPara("staff.name"));
    setAttr("storageName", getPara("storage.fullName"));
    setAttr("productName", getPara("product.fullName"));
    setAttr("detailMemo", getPara("purchaseDetail.memo"));
    
    setAttr("billList", billList);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "jhd", AioConstants.STATUS_ENABLE));
    setAttr("detailRowList", ReportRow.dao.getListByType(configName, "jhdyy", AioConstants.STATUS_ENABLE));
    render("option.html");
  }
  
  public void detail()
  {
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    setAttr("detailList", PurchaseDetail.dao.getUntreatedList(configName, Integer.valueOf(billId)));
    setAttr("detailRowList", ReportRow.dao.getListByType(configName, "jhdyy", AioConstants.STATUS_ENABLE));
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
      PurchaseBill bill = (PurchaseBill)getModel(PurchaseBill.class);
      Map<String, Object> map = new HashMap();
      map.put("bill", bill);
      map.put("startDate", getPara("startDate"));
      map.put("endDate", getPara("endDate"));
      map.put("unitName", getPara("unit.fullName"));
      map.put("staffName", getPara("staff.name"));
      map.put("storageName", getPara("storage.fullName"));
      map.put("productName", getPara("product.fullName"));
      map.put("departmentName", getPara("department.fullName"));
      map.put("detailMemo", getPara("purchaseDetail.memo"));
      map.put("isRCW", Integer.valueOf(AioConstants.RCW_NO));
      List<Model> billList = PurchaseBill.dao.getRelList(configName, Integer.valueOf(AioConstants.STATUS_DISABLE), map);
      if (billList.size() > 0) {
        setAttr("detailList", PurchaseDetail.dao.getUntreatedList(configName, ((Model)billList.get(0)).getInt("id")));
      }
      setAttr("bill", bill);
      setAttr("startDate", getPara("startDate"));
      setAttr("endDate", getPara("endDate"));
      setAttr("unitName", getPara("unit.fullName"));
      setAttr("staffName", getPara("staff.name"));
      setAttr("storageName", getPara("storage.fullName"));
      setAttr("productName", getPara("product.fullName"));
      setAttr("departmentName", getPara("department.fullName"));
      setAttr("detailMemo", getPara("purchaseDetail.memo"));
      setAttr("isRCW", Integer.valueOf(AioConstants.RCW_NO));
      
      setAttr("billList", billList);
      setAttr("rowList", ReportRow.dao.getListByType(configName, "jhd", AioConstants.STATUS_ENABLE));
      setAttr("detailRowList", ReportRow.dao.getListByType(configName, "jhdyy", AioConstants.STATUS_ENABLE));
      render("option.html");
    }
    else
    {
      setStartDateAndEndDate();
      render("dialogSearch.html");
    }
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    PurchaseBill bill = (PurchaseBill)PurchaseBill.dao.findById(configName, Integer.valueOf(id));
    List<Model> detailList = PurchaseDetail.dao.getListByBillId(configName, Integer.valueOf(id), basePrivs(PRODUCT_PRIVS), basePrivs(STORAGE_PRIVS));
    detailList = addTrSize(detailList, 15);
    

    setPayTypeAttr(configName, "saveBill", billTypeId, bill.getInt("id").intValue());
    
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    render("look.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> result = new HashMap();
    PurchaseBill bill = (PurchaseBill)PurchaseBill.dao.findById(configName, billId);
    if (bill == null)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", "该进货单不存在");
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
    Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unitId"), AioConstants.WAY_PAY, payMoney, privilegeMoney);
    if (vmap != null)
    {
      result = vmap;
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      return result;
    }
    List<Model> detailList = PurchaseDetail.dao.getListByBillId(configName, billId, basePrivs(PRODUCT_PRIVS), basePrivs(STORAGE_PRIVS));
    boolean comfirm = SysConfig.getConfig(configName, 1).booleanValue();
    Integer productId;
    for (Model detail : detailList)
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
        result.put("message", "该进货单有商品库存不足");
        return result;
      }
      if ((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0))
      {
        result.put("status", Integer.valueOf(AioConstants.RCW_NO));
        result.put("message", "该进货单有商品库存不足");
        return result;
      }
    }
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
    bill.update(configName);
    PurchaseBill newBill = (PurchaseBill)DeepClone.deepClone(bill);
    newBill.set("id", null);
    newBill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
    Date time = new Date();
    newBill.set("updateTime", time);
    
    billRcwAddRemark(bill, null);
    newBill.save(configName);
    
    BillHistoryController.saveBillHistory(configName, newBill, billTypeId, newBill.getBigDecimal("taxMoneys"));
    for (Model odlDetail : detailList)
    {
      PurchaseDetail newDetail = (PurchaseDetail)odlDetail;
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
      if (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) {
        StockController.stockChangeSubYDPrice(configName, storageId.intValue(), productId.intValue(), costArith.intValue(), amount, newDetail.getBigDecimal("costPrice"));
      }
      StockController.stockSubChange(configName, costArith.intValue(), amount, storageId.intValue(), productId.intValue(), costPrice, newDetail.getStr("batch"), newDetail.getDate("produceDate"), newDetail.getDate("produceEndDate"), null);
      
      Timestamp times = new Timestamp(time.getTime());
      StockRecordsController.addRecords(configName, billTypeId, "out", newBill, newDetail, costPrice, amount, time, times, newDetail.getInt("storageId"));
      
      AvgpriceConTroller.addAvgprice(configName, "out", storageId, productId, amount, BigDecimalUtils.mul(amount, costPrice));
      newDetail.save(configName);
      Integer detailId = odlDetail.getInt("detailId");
      
      PurchaseDetailController.rcwDetailAmount(configName, amount, detailId);
    }
    PayTypeController.rcwAccountsRecoder(configName, billTypeId, billId, newBill.getInt("id"));
    

    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
    

    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), billId, null, null, null, null, null, null, null);
    


    result.put("status", Integer.valueOf(AioConstants.RCW_BY));
    result.put("message", "红冲成功");
    return result;
  }
  
  public void print()
    throws ParseException
  {
    Integer billId = getParaToInt("billId");
    Integer draftId = getParaToInt("draftId");
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "进货单");
    Integer unitId = getParaToInt("unit.id");
    List<String> headData = new ArrayList();
    
    setHeadUnitData(headData, unitId);
    headData.add("供货单位:" + getPara("unit.fullName", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门 :" + getPara("department.fullName", ""));
    headData.add("付款日期:" + getPara("purchaseBill.payDate", ""));
    String recodeDate = DateUtils.format(getParaToDate("purchaseBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    
    headData.add("收货仓库:" + getPara("storage.fullName", ""));
    headData.add("摘要:" + getPara("purchaseBill.remark", ""));
    headData.add("附加说明 :" + getPara("purchaseBill.memo", ""));
    headData.add("整单折扣 :" + getPara("purchaseBill.discounts", ""));
    headData.add("单据编号:" + getPara("purchaseBill.code", ""));
    headData.add("付款账户:" + getPara("payTypeAccounts", ""));
    headData.add("付款金额:" + getPara("purchaseBill.payMoney", ""));
    headData.add("优惠金额:" + getPara("purchaseBill.privilege", ""));
    headData.add("优惠后金额:" + getPara("purchaseBill.privilegeMoney", ""));
    Integer userId = null;
    if ((billId != null) && (billId.intValue() != 0))
    {
      Model bill = PurchaseBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    else if ((draftId != null) && (draftId.intValue() != 0))
    {
      Model bill = PurchaseDraftBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    setBillUser(headData, userId);
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
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
    colTitle.add("备注");
    colTitle.add("零售价");
    colTitle.add("零售金额");
    colTitle.add("状态");
    colTitle.add("到货数量");
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseDetail.class, "purchaseDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailList = PurchaseDetail.dao.getListByBillId(configName, billId, basePrivs(PRODUCT_PRIVS), basePrivs(STORAGE_PRIVS));
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
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
      rowData.add(trimNull(detail.get("memo")));
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      setRetailPrice(rowData, detail, product);
      rowData.add(status);
      rowData.add(trimNull(detail.getBigDecimal("arrivalAmount")));
      rowData.add(trimNull(product.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(DwzUtils.helpUnit(product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(detail.getStr("message")));
    }
    if ((detailList == null) || (detailList.size() == 0)) {
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
}
