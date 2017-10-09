package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AvgpriceConTroller;
import com.aioerp.controller.bought.PurchaseReturnBillController;
import com.aioerp.controller.finance.AdjustCostController;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.reports.finance.arap.ArapRecordsController;
import com.aioerp.controller.sell.SellBarterBillController;
import com.aioerp.controller.stock.DismountBillController;
import com.aioerp.controller.stock.StockController;
import com.aioerp.controller.stock.StockRecordsController;
import com.aioerp.db.reports.BillHistory;
import com.aioerp.model.base.Product;
import com.aioerp.model.bought.PurchaseBarterBill;
import com.aioerp.model.bought.PurchaseBarterDetail;
import com.aioerp.model.bought.PurchaseBill;
import com.aioerp.model.bought.PurchaseDetail;
import com.aioerp.model.bought.PurchaseReturnBill;
import com.aioerp.model.bought.PurchaseReturnDetail;
import com.aioerp.model.finance.AdjustCostBill;
import com.aioerp.model.finance.AdjustCostDetail;
import com.aioerp.model.finance.PayMoneyBill;
import com.aioerp.model.finance.PayType;
import com.aioerp.model.finance.TransferBill;
import com.aioerp.model.finance.assets.AddAssetsBill;
import com.aioerp.model.finance.assets.DeprAssetsBill;
import com.aioerp.model.finance.assets.SubAssetsBill;
import com.aioerp.model.finance.changegetorpay.ChangeGetOrPay;
import com.aioerp.model.finance.changemoney.ChangeMoney;
import com.aioerp.model.finance.feebill.FeeBill;
import com.aioerp.model.finance.getmoney.GetMoneyBill;
import com.aioerp.model.sell.barter.SellBarterBill;
import com.aioerp.model.sell.barter.SellBarterDetail;
import com.aioerp.model.sell.sell.SellBill;
import com.aioerp.model.sell.sell.SellDetail;
import com.aioerp.model.sell.sellreturn.SellReturnBill;
import com.aioerp.model.sell.sellreturn.SellReturnDetail;
import com.aioerp.model.stock.BreakageBill;
import com.aioerp.model.stock.BreakageDetail;
import com.aioerp.model.stock.DifftAllotBill;
import com.aioerp.model.stock.DifftAllotDetail;
import com.aioerp.model.stock.DismountBill;
import com.aioerp.model.stock.DismountDetail;
import com.aioerp.model.stock.OverflowBill;
import com.aioerp.model.stock.OverflowDetail;
import com.aioerp.model.stock.ParityAllotBill;
import com.aioerp.model.stock.ParityAllotDetail;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.stock.StockInit;
import com.aioerp.model.stock.StockRecords;
import com.aioerp.model.stock.other.OtherInBill;
import com.aioerp.model.stock.other.OtherInDetail;
import com.aioerp.model.stock.other.OtherOutBill;
import com.aioerp.model.stock.other.OtherOutDetail;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DwzUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ReloadCostPriceController
  extends BaseController
{
	/*
  private static final int[] BILL_TYPES = {
    AioConstants.BILL_ROW_TYPE4, 
    AioConstants.BILL_ROW_TYPE5, 
    AioConstants.BILL_ROW_TYPE6, 
    AioConstants.BILL_ROW_TYPE7, 
    AioConstants.BILL_ROW_TYPE8, 
    AioConstants.BILL_ROW_TYPE9, 
    AioConstants.BILL_ROW_TYPE10, 
    AioConstants.BILL_ROW_TYPE11, 
    AioConstants.BILL_ROW_TYPE12, 
    AioConstants.BILL_ROW_TYPE13, 
    AioConstants.BILL_ROW_TYPE14, 
    AioConstants.BILL_ROW_TYPE15, 
    AioConstants.BILL_ROW_TYPE16, 
    AioConstants.BILL_ROW_TYPE20 };
  
  public void toReloadCostPrice()
  {
    setAttr("billCount", Boolean.valueOf(AioConstants.BILL_COUNT_IS_OK));
    render("/WEB-INF/template/sys/reLoadCostPrice/dialog.html");
  }
  
  @Before({Tx.class})
  public void reloadCostPrice()
    throws SQLException
  {
    String configName = loginConfigName();
    try
    {
      AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS4));
      

      this.result = BackUpController.commonBackUp(configName, Integer.valueOf(loginUserId()), loginAccountName(), "成本重算" + loginAccountName() + DateUtils.format(new Date(), "yyyy-MM-ddHHmmss"));
      if (Integer.valueOf(this.result.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(this.result);
        AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
        return;
      }
      Db.use(configName).update("delete from cc_stock_records");
      

      Db.use(configName).update("delete from zj_product_avgprice");
      

      Db.use(configName).update("delete from cc_stock");
      

      StockInit.dao.openInitDateMove(configName, null, "reloadCostPrice");
      

      List<Model> yearList = YearEnd.dao.getList(configName);
      if (yearList == null) {
        yearList = new ArrayList();
      }
      YearEnd m = new YearEnd();
      if (yearList.size() > 0) {
        m.set("startDate", ((Model)yearList.get(yearList.size() - 1)).getDate("endDate"));
      }
      yearList.add(m);
      for (Model model : yearList) {
        reloadAfterInsertBill(configName, model.getDate("startDate"), model.getDate("endDate"));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      
      commonRollBack(configName);
      this.result.put("message", "操作失败！");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    finally
    {
      AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
    }
    AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
    


    this.result.put("message", "操作成功");
    this.result.put("statusCode", AioConstants.HTTP_RETURN200);
    renderJson(this.result);
  }
  
  public static void reloadInsertBillCostPrice(String configName, Date insertBillDate, List<Model> detailList)
    throws ParseException
  {
    for (Model model : detailList) {
      try
      {
        int costArith = Product.dao.findById(configName, model.getInt("productId")).getInt("costArith").intValue();
        
        BigDecimal costPrice = model.getBigDecimal("costPrice");
        if (costPrice == null) {
          costPrice = BigDecimal.ZERO;
        }
        Model result = StockRecords.dao.getInsertBillStockObj(configName, insertBillDate, model, costPrice, costArith);
        if (result != null)
        {
          if (costArith == 1) {
            Db.use(configName).update("update cc_stock set costPrice = ? where productId = ? and storageId = ? ", new Object[] { result.getBigDecimal("costPrice"), model.getInt("productId"), model.getInt("storageId") });
          } else {
            Db.use(configName).update("update cc_stock set costPrice = ? where productId = ? and storageId = ? and batch = ? and produceDate =? and produceEndDate =?", new Object[] { result.getBigDecimal("costPrice"), model.getInt("productId"), model.getInt("storageId"), result.getInt("batch"), result.getTimestamp("produceDate"), result.getTimestamp("produceEndDate") });
          }
          Db.use(configName).update("update zj_product_avgprice set avgPrice = ?,costMoneys = ?,amount= ? where productId = ? and storageId = ?", new Object[] { result.getBigDecimal("costPrice"), result.getBigDecimal("totalMoney"), result.getBigDecimal("totalAmount"), result.getInt("productId"), result.getInt("stroageId") });
        }
      }
      catch (ParseException e)
      {
        e.printStackTrace();
      }
    }
    Db.use(configName).update("delete from cc_stock_records where recodeTime>='" + DateUtils.format(insertBillDate, "yyyy-MM-dd HH:mm:ss") + "'");
    

    reloadAfterInsertBill(configName, insertBillDate, null);
  }
  
  public static void reloadAfterInsertBill(String configName, Date startDate, Date endDate)
    throws ParseException
  {
    List<Model> list = BillHistory.dao.getList(configName, DateUtils.format(startDate, "yyyy-MM-dd HH:mm:ss"), DateUtils.format(endDate, "yyyy-MM-dd HH:mm:ss"), BILL_TYPES);
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Model r = (Model)list.get(i);
        Integer billId = r.getInt("billId");
        Integer billTypeId = r.getInt("billTypeId");
        Integer loginUserId = r.getInt("userId");
        

        boolean hasCom = true;
        boolean hasSameAccount = false;
        Map<String, Object> record = new HashMap();
        List<Record> proAccountList = new ArrayList();
        int isRCW = AioConstants.RCW_NO;
        Model bill = null;
        List<Model> detailList = null;
        if ((billTypeId.intValue() == AioConstants.BILL_ROW_TYPE4) || (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE7) || (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE10) || (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE11))
        {
          String inOrOut = "in";
          String relType = null;
          if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE4)
          {
            bill = SellBill.dao.findById(configName, billId);
            detailList = SellDetail.dao.getList1(configName, billId);
            inOrOut = "out";
          }
          else if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE7)
          {
            bill = SellReturnBill.dao.findById(configName, billId);
            detailList = SellReturnDetail.dao.getList1(configName, billId.intValue());
            relType = "sellReutnrRelSell";
          }
          else if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE10)
          {
            bill = OtherInBill.dao.findById(configName, billId);
            detailList = OtherInDetail.dao.getList1(configName, billId.intValue());
          }
          else if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE11)
          {
            bill = OtherOutBill.dao.findById(configName, billId);
            detailList = OtherOutDetail.dao.getList1(configName, billId.intValue());
            inOrOut = "out";
          }
          BigDecimal sellCosts = BigDecimal.ZERO;
          for (int j = 0; j < detailList.size(); j++)
          {
            Model detail = (Model)detailList.get(j);
            Record proAccount = new Record();
            
            detailReLoad(configName, billTypeId.intValue(), bill, detail, bill.getInt("isRCW").intValue(), inOrOut, relType, detail.getInt("productId").intValue());
            
            proAccount.set("productId", detail.getInt("productId"));
            if ((billTypeId.intValue() == AioConstants.BILL_ROW_TYPE10) || (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE11)) {
              proAccount.set("costMoney", BigDecimalUtils.mul(detail.getBigDecimal("baseAmount"), detail.getBigDecimal("basePrice")));
            } else {
              proAccount.set("costMoney", detail.getBigDecimal("costMoneys"));
            }
            proAccountList.add(proAccount);
            sellCosts = BigDecimalUtils.add(sellCosts, proAccount.getBigDecimal("costMoney"));
          }
          if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE4)
          {
            record.put("sellCosts", sellCosts);
            record.put("taxes", bill.getBigDecimal("taxes"));
            record.put("sellMoneys", bill.getBigDecimal("discountMoneys"));
            record.put("needGetOrPay", BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney")));
          }
          else if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE7)
          {
            record.put("sellReturnCosts", sellCosts);
            record.put("taxes", bill.getBigDecimal("taxes"));
            record.put("sellReturnMoneys", bill.getBigDecimal("discountMoneys"));
            record.put("needGetOrPay", BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney")));
          }
          else if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE10)
          {
            record.put("accountId", Integer.valueOf(bill.getStr("accountsId")));
            record.put("billMoneys", sellCosts);
          }
          else if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE11)
          {
            record.put("accountId", Integer.valueOf(bill.getStr("accountsId")));
            record.put("billMoneys", sellCosts);
          }
          record.put("proAccountList", proAccountList);
          record.put("loginUserId", loginUserId);
          hasSameAccount = true;
        }
        else
        {
          BigDecimal discountMoneys;
          if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE5)
          {
            bill = PurchaseBill.dao.findById(configName, billId);
            detailList = PurchaseDetail.dao.getListByBillId(configName, billId);
            isRCW = bill.getInt("isRCW").intValue();
            String stockType = "in";
            if (AioConstants.RCW_VS == isRCW) {
              stockType = "out";
            }
            BigDecimal amounts = null;
            
            BigDecimal moneys = null;
            discountMoneys = null;
            BigDecimal taxMoneys = null;
            for (Model detail : detailList)
            {
              amounts = BigDecimalUtils.add(amounts, detail.getBigDecimal("amount"));
              moneys = BigDecimalUtils.add(moneys, detail.getBigDecimal("money"));
              discountMoneys = BigDecimalUtils.add(discountMoneys, detail.getBigDecimal("discountMoney"));
              taxMoneys = BigDecimalUtils.add(taxMoneys, detail.getBigDecimal("taxMoney"));
              BigDecimal costPrice = detail.getBigDecimal("basePrice");
              detail.set("costPrice", costPrice).update(configName);
              Integer detailStorageId = detail.getInt("storageId");
              Integer productId = detail.getInt("productId");
              BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
              Product product = (Product)Product.dao.findById(configName, productId);
              
              AvgpriceConTroller.addAvgprice(configName, stockType, detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, costPrice));
              
              StockController.stockChange(configName, stockType, detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
              
              StockRecordsController.addRecords(configName, billTypeId.intValue(), stockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
            }
            if (BigDecimalUtils.compare(moneys, bill.getBigDecimal("moneys")) != 0)
            {
              bill.set("amounts", amounts);
              bill.set("moneys", moneys);
              bill.set("discountMoneys", discountMoneys);
              bill.set("taxMoneys", taxMoneys);
              bill.set("privilegeMoney", BigDecimalUtils.sub(taxMoneys, bill.getBigDecimal("privilege")));
              bill.update(configName);
              Model history = BillHistory.dao.getRecordByBillIdAndTypeId(configName, billId, billTypeId);
              if (history != null)
              {
                history.set("money", taxMoneys);
                history.update(configName);
              }
            }
            record.put("purchaseMoneys", bill.getBigDecimal("taxMoneys"));
            record.put("proAccountList", detailList);
            record.put("payMoney", bill.getBigDecimal("payMoney"));
            record.put("taxes", bill.getBigDecimal("taxes"));
            record.put("loginUserId", loginUserId);
            hasSameAccount = true;
          }
          else if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE6)
          {
            bill = PurchaseReturnBill.dao.findById(configName, billId);
            detailList = PurchaseReturnDetail.dao.getListByBillId(configName, billId);
            BigDecimal costMoneys = BigDecimal.ZERO;
            isRCW = bill.getInt("isRCW").intValue();
            String stockType = "out";
            if (AioConstants.RCW_VS == isRCW) {
              stockType = "in";
            }
            for (Model detail : detailList)
            {
              BigDecimal costPrice = detail.getBigDecimal("costPrice");
              
              Integer detailStorageId = detail.getInt("storageId");
              Integer productId = detail.getInt("productId");
              BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
              Product product = (Product)Product.dao.findById(configName, productId);
              Integer costArith = product.getInt("costArith");
              Integer detailId = detail.getInt("detailId");
              if (detailId != null)
              {
                Model purchaseDetail = PurchaseDetail.dao.findById(configName, detailId);
                if (purchaseDetail != null) {
                  costPrice = purchaseDetail.getBigDecimal("costPrice");
                }
              }
              else if (AioConstants.PRD_COST_PRICE1 == costArith.intValue())
              {
                costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId.intValue(), isRCW, detailStorageId.intValue(), product, detail, "costPrice");
              }
              detail.set("costPrice", costPrice).update(configName);
              
              AvgpriceConTroller.addAvgprice(configName, stockType, detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, costPrice));
              
              StockController.stockChange(configName, stockType, detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
              
              StockRecordsController.addRecords(configName, billTypeId.intValue(), stockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
              

              costMoneys = BigDecimalUtils.add(costMoneys, BigDecimalUtils.mul(costPrice, detail.getBigDecimal("baseAmount")));
            }
            record.put("privilegeType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
            record.put("getOrPayType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
            record.put("proAccountType", Integer.valueOf(AioConstants.ACCOUNT_MONEY1));
            record.put("proAccountList", detailList);
            record.put("loginUserId", loginUserId);
            record.put("accounts", PurchaseReturnBillController.getOtherAccount(bill.getBigDecimal("taxes"), BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney")), BigDecimalUtils.sub(bill.getBigDecimal("discountMoneys"), costMoneys)));
            hasSameAccount = true;
          }
          else if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE8)
          {
            bill = BreakageBill.dao.findById(configName, billId);
            detailList = BreakageDetail.dao.getList(configName, billId);
            BigDecimal costMoneys = BigDecimal.ZERO;
            isRCW = bill.getInt("isRCW").intValue();
            String stockType = "out";
            if (AioConstants.RCW_VS == isRCW) {
              stockType = "in";
            }
            for (Model detail : detailList)
            {
              BigDecimal costPrice = detail.getBigDecimal("basePrice");
              Integer detailStorageId = detail.getInt("storageId");
              Integer productId = detail.getInt("productId");
              BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
              Product product = (Product)Product.dao.findById(configName, productId);
              Integer costArith = product.getInt("costArith");
              Integer selectUnitId = detail.getInt("selectUnitId");
              BigDecimal amount = detail.getBigDecimal("amount");
              if (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) {
                costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId.intValue(), isRCW, detailStorageId.intValue(), product, detail, "basePrice");
              }
              BigDecimal price = DwzUtils.getConversionPrice(costPrice, Integer.valueOf(1), product, selectUnitId);
              detail.set("price", price);
              detail.set("money", BigDecimalUtils.mul(price, amount));
              detail.set("basePrice", costPrice).update(configName);
              
              AvgpriceConTroller.addAvgprice(configName, stockType, detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, costPrice));
              
              StockController.stockChange(configName, stockType, detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
              
              StockRecordsController.addRecords(configName, billTypeId.intValue(), stockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
              
              costMoneys = BigDecimalUtils.add(costMoneys, BigDecimalUtils.mul(costPrice, amount));
            }
            bill.set("moneys", costMoneys).update(configName);
            

            r.set("money", costMoneys).update(configName);
            

            PayType.dao.delete(configName, billId, billTypeId);
            if (bill != null)
            {
              record.put("proAccountList", detailList);
              record.put("moneys", bill.getBigDecimal("moneys"));
              record.put("loginUserId", loginUserId);
              PayTypeController.addAccountsRecoder(configName, billTypeId.intValue(), bill.getInt("id").intValue(), Integer.valueOf(0), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, record);
            }
            if (AioConstants.RCW_VS == isRCW) {
              PayTypeController.editRcwAccountsRecoder(configName, billTypeId.intValue(), billId);
            }
          }
          else if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE9)
          {
            bill = OverflowBill.dao.findById(configName, billId);
            detailList = OverflowDetail.dao.getList(configName, billId);
            BigDecimal costMoneys = BigDecimal.ZERO;
            isRCW = bill.getInt("isRCW").intValue();
            String stockType = "in";
            if (AioConstants.RCW_VS == isRCW) {
              stockType = "out";
            }
            for (Model detail : detailList)
            {
              BigDecimal costPrice = detail.getBigDecimal("basePrice");
              Integer detailStorageId = detail.getInt("storageId");
              Integer productId = detail.getInt("productId");
              BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
              Product product = (Product)Product.dao.findById(configName, productId);
              Integer costArith = product.getInt("costArith");
              Integer selectUnitId = detail.getInt("selectUnitId");
              BigDecimal amount = detail.getBigDecimal("amount");
              if (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) {
                costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId.intValue(), isRCW, detailStorageId.intValue(), product, detail, "basePrice");
              }
              BigDecimal price = DwzUtils.getConversionPrice(costPrice, Integer.valueOf(1), product, selectUnitId);
              detail.set("price", price);
              detail.set("money", BigDecimalUtils.mul(price, amount));
              detail.set("basePrice", costPrice).update(configName);
              
              AvgpriceConTroller.addAvgprice(configName, stockType, detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, costPrice));
              
              StockController.stockChange(configName, stockType, detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
              
              StockRecordsController.addRecords(configName, billTypeId.intValue(), stockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
              
              costMoneys = BigDecimalUtils.add(costMoneys, detail.getBigDecimal("money"));
            }
            bill.set("moneys", costMoneys).update(configName);
            

            r.set("money", costMoneys).update(configName);
            

            PayType.dao.delete(configName, billId, billTypeId);
            if (bill != null)
            {
              record.put("proAccountList", detailList);
              record.put("moneys", bill.getBigDecimal("moneys"));
              record.put("loginUserId", loginUserId);
              PayTypeController.addAccountsRecoder(configName, billTypeId.intValue(), bill.getInt("id").intValue(), Integer.valueOf(0), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, record);
            }
            if (AioConstants.RCW_VS == isRCW) {
              PayTypeController.editRcwAccountsRecoder(configName, billTypeId.intValue(), billId);
            }
          }
          else
          {
            List<Record> proAccountListOut;
            if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE12)
            {
              bill = PurchaseBarterBill.dao.findById(configName, billId);
              isRCW = bill.getInt("isRCW").intValue();
              
              BigDecimal intaxes = BigDecimal.ZERO;
              List<Model> inDetailList = PurchaseBarterDetail.dao.getDetailByBillId(configName, billId, Integer.valueOf(1));
              Integer productId=0;
              for (Model detail : inDetailList)
              {
                BigDecimal basePrice = detail.getBigDecimal("basePrice");
                Integer detailStorageId = detail.getInt("storageId");
                productId = detail.getInt("productId");
                BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
                Product product = (Product)Product.dao.findById(configName, productId);
                
                String stockType = "in";
                if (AioConstants.RCW_VS == isRCW) {
                  stockType = "out";
                }
                AvgpriceConTroller.addAvgprice(configName, stockType, detailStorageId, (Integer)productId, baseAmount, BigDecimalUtils.mul(baseAmount, basePrice));
                
                StockController.stockChange(configName, stockType, detailStorageId.intValue(), ((Integer)productId).intValue(), product.getInt("costArith").intValue(), baseAmount, basePrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
                
                StockRecordsController.addRecords(configName, billTypeId.intValue(), stockType, bill, detail, basePrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
                

                intaxes = BigDecimalUtils.add(intaxes, detail.getBigDecimal("taxes"));
              }
              List<Model> outDetailList = PurchaseBarterDetail.dao.getDetailByBillId(configName, billId, Integer.valueOf(2));
              proAccountListOut = new ArrayList();
              BigDecimal outtaxes = BigDecimal.ZERO;
              for (Object productId1 = outDetailList.iterator(); ((Iterator)productId1).hasNext();)
              {
                Model detail = (Model)((Iterator)productId1).next();
                Record proAccount = new Record();
                BigDecimal costPrice = detail.getBigDecimal("costPrice");
                Integer detailStorageId = detail.getInt("storageId");
                 productId = detail.getInt("productId");
                BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
                Product product = (Product)Product.dao.findById(configName, productId);
                Integer costArith = product.getInt("costArith");
                if (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) {
                  costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId.intValue(), isRCW, detailStorageId.intValue(), product, detail, "costPrice");
                }
                BigDecimal costMoney = BigDecimalUtils.mul(baseAmount, costPrice).setScale(4, 4);
                detail.set("costPrice", costPrice);
                detail.update(configName);
                
                proAccount.set("productId", productId);
                proAccount.set("discountMoney", detail.get("discountMoney"));
                proAccount.set("costMoney", costMoney);
                proAccountListOut.add(proAccount);
                

                String stockType = "out";
                if (AioConstants.RCW_VS == isRCW) {
                  stockType = "in";
                }
                AvgpriceConTroller.addAvgprice(configName, stockType, detailStorageId, productId, baseAmount, costMoney);
                
                StockController.stockChange(configName, stockType, detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
                
                StockRecordsController.addRecords(configName, billTypeId.intValue(), stockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
                

                outtaxes = BigDecimalUtils.add(outtaxes, detail.getBigDecimal("taxes"));
              }
              record.put("gapMoney", bill.getBigDecimal("gapMoney"));
              record.put("proAccountListIn", inDetailList);
              record.put("proAccountListOut", proAccountListOut);
              record.put("payMoney", bill.getBigDecimal("payMoney"));
              record.put("taxes", BigDecimalUtils.sub(outtaxes, intaxes));
              record.put("loginUserId", loginUserId);
              hasSameAccount = true;
            }
            else
            {
              Model detail;
              if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE13)
              {
                bill = SellBarterBill.dao.findById(configName, billId);
                isRCW = bill.getInt("isRCW").intValue();
                
                List<Model> inDetailList = SellBarterDetail.dao.getList(configName, billId, Integer.valueOf(1));
                BigDecimal costInMoneys = BigDecimal.ZERO;
                for (Model detail : inDetailList)
                {
                  BigDecimal costPrice = detail.getBigDecimal("costPrice");
                  detailStorageId = detail.getInt("storageId");
                  Integer productId = detail.getInt("productId");
                  BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
                  Product product = (Product)Product.dao.findById(configName, productId);
                  Integer costArith = product.getInt("costArith");
                  if (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) {
                    costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId.intValue(), isRCW, detailStorageId.intValue(), product, detail, "costPrice");
                  }
                  BigDecimal costMoney = BigDecimalUtils.mul(baseAmount, costPrice);
                  detail.set("costPrice", costPrice);
                  detail.set("costMoneys", costMoney);
                  detail.update(configName);
                  costInMoneys = BigDecimalUtils.add(costInMoneys, costMoney);
                  
                  String stockType = "in";
                  if (AioConstants.RCW_VS == isRCW) {
                    stockType = "out";
                  }
                  AvgpriceConTroller.addAvgprice(configName, stockType, detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, costPrice));
                  
                  StockController.stockChange(configName, stockType, detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
                  
                  StockRecordsController.addRecords(configName, billTypeId.intValue(), stockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
                }
                List<Model> outDetailList = SellBarterDetail.dao.getList(configName, billId, Integer.valueOf(2));
                BigDecimal costOutMoneys = BigDecimal.ZERO;
                for (Integer detailStorageId = outDetailList.iterator(); detailStorageId.hasNext();)
                {
                  detail = (Model)detailStorageId.next();
                  BigDecimal costPrice = detail.getBigDecimal("costPrice");
                  
                  Integer detailStorageId = detail.getInt("storageId");
                  Integer productId = detail.getInt("productId");
                  BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
                  Product product = (Product)Product.dao.findById(configName, productId);
                  Integer costArith = product.getInt("costArith");
                  if (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) {
                    costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId.intValue(), isRCW, detailStorageId.intValue(), product, detail, "costPrice");
                  }
                  BigDecimal costMoney = BigDecimalUtils.mul(baseAmount, costPrice);
                  detail.set("costPrice", costPrice);
                  detail.set("costMoneys", costMoney);
                  detail.update(configName);
                  costOutMoneys = BigDecimalUtils.add(costOutMoneys, costMoney);
                  
                  String stockType = "out";
                  if (AioConstants.RCW_VS == isRCW) {
                    stockType = "in";
                  }
                  AvgpriceConTroller.addAvgprice(configName, stockType, detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, costPrice));
                  
                  StockController.stockChange(configName, stockType, detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
                  
                  StockRecordsController.addRecords(configName, billTypeId.intValue(), stockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
                }
                record.put("privilegeType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
                record.put("getOrPayType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
                record.put("proOutList", outDetailList);
                record.put("proInList", inDetailList);
                record.put("accounts", SellBarterBillController.getOtherAccount(BigDecimalUtils.sub(bill.getBigDecimal("outTaxes"), bill.getBigDecimal("inTaxes")), 
                  BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney")), 
                  BigDecimalUtils.sub(bill.getBigDecimal("outDiscountMoneys"), bill.getBigDecimal("inDiscountMoneys")), 
                  BigDecimalUtils.sub(costOutMoneys, costInMoneys)));
                record.put("loginUserId", loginUserId);
                hasSameAccount = true;
              }
              else
              {
                Map<String, Object> outRecord;
                if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE14)
                {
                  bill = ParityAllotBill.dao.findById(configName, billId);
                  detailList = ParityAllotDetail.dao.getList(configName, billId);
                  isRCW = bill.getInt("isRCW").intValue();
                  String inStockType = "in";
                  
                  String outStockType = "out";
                  if (AioConstants.RCW_VS == isRCW)
                  {
                    inStockType = "out";
                    
                    outStockType = "in";
                  }
                  BigDecimal moneys = null;
                  for (Model detail : detailList)
                  {
                    BigDecimal costPrice = detail.getBigDecimal("costPrice");
                    Integer detailInStorage = detail.getInt("inStorageId");
                    Integer detailOutStorage = detail.getInt("outStorageId");
                    Integer productId = detail.getInt("productId");
                    BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
                    Product product = (Product)Product.dao.findById(configName, productId);
                    Integer costArith = product.getInt("costArith");
                    if (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) {
                      costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId.intValue(), isRCW, detailOutStorage.intValue(), product, detail, "costPrice");
                    }
                    detail.set("costPrice", costPrice);
                    detail.set("money", BigDecimalUtils.mul(costPrice, baseAmount));
                    BigDecimal amount = detail.getBigDecimal("amount");
                    detail.set("price", BigDecimalUtils.div(detail.getBigDecimal("money"), amount));
                    detail.update(configName);
                    moneys = BigDecimalUtils.add(moneys, detail.getBigDecimal("money"));
                    BigDecimal costMoney = BigDecimalUtils.mul(baseAmount, costPrice);
                    

                    StockController.stockChange(configName, outStockType, detailOutStorage.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
                    StockController.stockChange(configName, inStockType, detailInStorage.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
                    
                    StockRecordsController.addRecords(configName, billTypeId.intValue(), outStockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detailOutStorage);
                    StockRecordsController.addRecords(configName, billTypeId.intValue(), inStockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detailInStorage);
                    
                    AvgpriceConTroller.addAvgprice(configName, outStockType, detailOutStorage, productId, baseAmount, costMoney);
                    AvgpriceConTroller.addAvgprice(configName, inStockType, detailInStorage, productId, baseAmount, costMoney);
                  }
                  r.set("money", moneys).update(configName);
                  bill.set("moneys", moneys).update(configName);
                  
                  PayType.dao.delete(configName, billId, billTypeId);
                  if (bill != null)
                  {
                    outRecord = new HashMap();
                    outRecord.put("proAccountType", Integer.valueOf(AioConstants.ACCOUNT_MONEY1));
                    outRecord.put("proAccountList", detailList);
                    outRecord.put("loginUserId", loginUserId);
                    PayTypeController.addAccountsRecoder(configName, billTypeId.intValue(), bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, outRecord);
                    Map<String, Object> inRecord = new HashMap();
                    inRecord.put("proAccountType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
                    inRecord.put("proAccountList", detailList);
                    inRecord.put("loginUserId", loginUserId);
                    PayTypeController.addAccountsRecoder(configName, billTypeId.intValue(), bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, inRecord);
                  }
                  if (AioConstants.RCW_VS == isRCW) {
                    PayTypeController.editRcwAccountsRecoder(configName, billTypeId.intValue(), billId);
                  }
                }
                else
                {
                  Model detail;
                  if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE15)
                  {
                    bill = DifftAllotBill.dao.findById(configName, billId);
                    detailList = DifftAllotDetail.dao.getList(configName, billId);
                    isRCW = bill.getInt("isRCW").intValue();
                    String inStockType = "in";
                    
                    String outStockType = "out";
                    if (AioConstants.RCW_VS == isRCW)
                    {
                      inStockType = "out";
                      outStockType = "in";
                    }
                    for (outRecord = detailList.iterator(); outRecord.hasNext();)
                    {
                      detail = (Model)outRecord.next();
                      BigDecimal costPrice = detail.getBigDecimal("costPrice");
                      BigDecimal basePrice = detail.getBigDecimal("basePrice");
                      Integer detailInStorage = detail.getInt("inStorageId");
                      Integer detailOutStorage = detail.getInt("outStorageId");
                      Integer productId = detail.getInt("productId");
                      BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
                      Product product = (Product)Product.dao.findById(configName, productId);
                      Integer costArith = product.getInt("costArith");
                      if (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) {
                        costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId.intValue(), isRCW, detailOutStorage.intValue(), product, detail, "costPrice");
                      }
                      detail.set("costPrice", costPrice).update(configName);
                      
                      BigDecimal outCostMoney = BigDecimalUtils.mul(baseAmount, costPrice);
                      BigDecimal inCostMoney = BigDecimalUtils.mul(baseAmount, basePrice);
                      
                      StockController.stockChange(configName, outStockType, detailOutStorage.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
                      StockController.stockChange(configName, inStockType, detailInStorage.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, basePrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
                      
                      StockRecordsController.addRecords(configName, billTypeId.intValue(), outStockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detailOutStorage);
                      StockRecordsController.addRecords(configName, billTypeId.intValue(), inStockType, bill, detail, basePrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detailInStorage);
                      
                      AvgpriceConTroller.addAvgprice(configName, outStockType, detailOutStorage, productId, baseAmount, outCostMoney);
                      AvgpriceConTroller.addAvgprice(configName, inStockType, detailInStorage, productId, baseAmount, inCostMoney);
                    }
                    PayType.dao.delete(configName, billId, billTypeId);
                    if (bill != null)
                    {
                      record.put("proAccountList", detailList);
                      record.put("loginUserId", loginUserId);
                      PayTypeController.addAccountsRecoder(configName, billTypeId.intValue(), bill.getInt("id").intValue(), Integer.valueOf(0), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, record);
                    }
                    if (AioConstants.RCW_VS == isRCW) {
                      PayTypeController.editRcwAccountsRecoder(configName, billTypeId.intValue(), billId);
                    }
                  }
                  else
                  {
                    Map<String, Object> outRecord;
                    if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE16)
                    {
                      bill = DismountBill.dao.findById(configName, billId);
                      isRCW = bill.getInt("isRCW").intValue();
                      
                      List<Model> inDetailList = DismountDetail.dao.getList(configName, billId, Integer.valueOf(1));
                      BigDecimal costPrice;
                      for (Model detail : inDetailList)
                      {
                        costPrice = detail.getBigDecimal("basePrice");
                        
                        Integer detailStorageId = detail.getInt("storageId");
                        Integer productId = detail.getInt("productId");
                        BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
                        Product product = (Product)Product.dao.findById(configName, productId);
                        detail.set("costPrice", costPrice).update(configName);
                        
                        String stockType = "in";
                        if (AioConstants.RCW_VS == isRCW) {
                          stockType = "out";
                        }
                        AvgpriceConTroller.addAvgprice(configName, stockType, detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, costPrice));
                        
                        StockController.stockChange(configName, stockType, detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
                        
                        StockRecordsController.addRecords(configName, billTypeId.intValue(), stockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
                      }
                      List<Model> outDetailList = DismountDetail.dao.getList(configName, billId, Integer.valueOf(2));
                      for (Model detail : outDetailList)
                      {
                        BigDecimal costPrice = detail.getBigDecimal("costPrice");
                        
                        Integer detailStorageId = detail.getInt("storageId");
                        Integer productId = detail.getInt("productId");
                        BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
                        Product product = (Product)Product.dao.findById(configName, productId);
                        Integer costArith = product.getInt("costArith");
                        if (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) {
                          costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId.intValue(), isRCW, detailStorageId.intValue(), product, detail, "costPrice");
                        }
                        detail.set("costPrice", costPrice).update(configName);
                        
                        String stockType = "out";
                        if (AioConstants.RCW_VS == isRCW) {
                          stockType = "in";
                        }
                        AvgpriceConTroller.addAvgprice(configName, stockType, detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, costPrice));
                        
                        StockController.stockChange(configName, stockType, detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
                        
                        StockRecordsController.addRecords(configName, billTypeId.intValue(), stockType, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
                      }
                      PayType.dao.delete(configName, billId, billTypeId);
                      if (bill != null)
                      {
                        outRecord = new HashMap();
                        outRecord.put("proAccountType", Integer.valueOf(AioConstants.ACCOUNT_MONEY1));
                        outRecord.put("proAccountList", outDetailList);
                        outRecord.put("loginUserId", loginUserId);
                        PayTypeController.addAccountsRecoder(configName, billTypeId.intValue(), bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, outRecord);
                        Map<String, Object> inRecord = new HashMap();
                        inRecord.put("proAccountType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
                        inRecord.put("proAccountList", inDetailList);
                        inRecord.put("accounts", DismountBillController.getOtherAccount(BigDecimalUtils.sub(bill.getBigDecimal("inMoney"), bill.getBigDecimal("outMoney"))));
                        inRecord.put("loginUserId", loginUserId);
                        PayTypeController.addAccountsRecoder(configName, billTypeId.intValue(), bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, inRecord);
                      }
                      if (AioConstants.RCW_VS == isRCW) {
                        PayTypeController.editRcwAccountsRecoder(configName, billTypeId.intValue(), billId);
                      }
                    }
                    else if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE17)
                    {
                      if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE18) {
                        if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE19) {
                          if (billTypeId.intValue() == AioConstants.BILL_ROW_TYPE20)
                          {
                            bill = AdjustCostBill.dao.findById(configName, billId);
                            detailList = AdjustCostDetail.dao.getList(configName, billId);
                            isRCW = bill.getInt("isRCW").intValue();
                            Integer storageId = bill.getInt("storageId");
                            for (Model detail : detailList)
                            {
                              BigDecimal costPrice = detail.getBigDecimal("costPrice");
                              

                              Integer productId = detail.getInt("productId");
                              
                              Product product = (Product)Product.dao.findById(configName, productId);
                              Integer costArith = product.getInt("costArith");
                              if (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) {
                                costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId.intValue(), isRCW, storageId.intValue(), product, detail, "costPrice");
                              }
                              detail.set("costPrice", costPrice).update(configName);
                              
                              AvgpriceConTroller.eidtAvgprice(configName, storageId, productId, costPrice);
                              
                              Stock stock = null;
                              if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
                                stock = Stock.dao.getStock(configName, productId.intValue(), storageId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
                              } else {
                                stock = Stock.dao.getStock(configName, productId, storageId);
                              }
                              if (stock != null)
                              {
                                stock.set("costPrice", detail.getBigDecimal("basePrice"));
                                stock.update(configName);
                              }
                            }
                            PayType.dao.delete(configName, billId, billTypeId);
                            record = new HashMap();
                            record.put("accounts", AdjustCostController.getOtherAccount(bill.getBigDecimal("moneys"), bill.getBigDecimal("lastMoneys"), bill.getBigDecimal("adjustMoneys")));
                            record.put("loginUserId", loginUserId);
                            PayTypeController.addAccountsRecoder(configName, billTypeId.intValue(), bill.getInt("id").intValue(), null, bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, record);
                            if (AioConstants.RCW_VS == isRCW) {
                              PayTypeController.editRcwAccountsRecoder(configName, billTypeId.intValue(), billId);
                            }
                          }
                          else if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE21)
                          {
                            if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE22) {
                              if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE23) {
                                if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE24) {
                                  if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE25) {
                                    if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE26) {
                                      if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE27) {
                                        if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE28) {
                                          if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE29) {
                                            if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE30) {
                                              if (billTypeId.intValue() != AioConstants.BILL_ROW_TYPE31) {
                                                billTypeId.intValue();
                                              }
                                            }
                                          }
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        if (hasSameAccount)
        {
          Map<String, String> map = ComFunController.billPayTypeAttr(configName, "saveBill", billTypeId.intValue(), bill.getInt("id").intValue());
          PayType.dao.delete(configName, billId, billTypeId);
          if (bill != null)
          {
            PayTypeController.addAccountsRecoder(configName, billTypeId.intValue(), bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), bill.getBigDecimal("privilege"), (String)map.get("payTypeIdStrs"), (String)map.get("payTypeMoneyStrs"), record);
            if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
              PayTypeController.editRcwAccountsRecoder(configName, billTypeId.intValue(), billId);
            }
          }
        }
      }
    }
  }
  
  public static void detailReLoad(String configName, int billTypeId, Model bill, Model detail, int isRCW, String inOrOut, String relType, int productId)
  {
    Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
    int costArith = product.getInt("costArith").intValue();
    BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
    
    boolean hasRel = false;
    if ((relType != null) && (relType.equals("sellReutnrRelSell")) && (detail.getInt("detailId") != null) && (detail.getInt("detailId").intValue() > 0))
    {
      hasRel = true;
      
      int relDetailId = detail.getInt("detailId").intValue();
      Model relDetail = SellDetail.dao.findById(configName, Integer.valueOf(relDetailId));
      
      detail.set("costPrice", relDetail.getBigDecimal("costPrice"));
      detail.set("costMoneys", BigDecimalUtils.mul(baseAmount, relDetail.getBigDecimal("costPrice")));
      detail.update(configName);
    }
    if ((!hasRel) && 
      (costArith == AioConstants.PRD_COST_PRICE1))
    {
      String costPriceAttr = "costPrice";
      if ((billTypeId == AioConstants.BILL_ROW_TYPE10) || (billTypeId == AioConstants.BILL_ROW_TYPE11)) {
        costPriceAttr = "basePrice";
      }
      BigDecimal costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId, isRCW, detail.getInt("storageId").intValue(), product, detail, costPriceAttr);
      if ((billTypeId == AioConstants.BILL_ROW_TYPE10) || (billTypeId == AioConstants.BILL_ROW_TYPE11))
      {
        detail.set("basePrice", costPrice);
        BigDecimal selectUnitPrice = DwzUtils.getConversionPrice(costPrice, Integer.valueOf(1), product, detail.getInt("selectUnitId"));
        detail.set("price", selectUnitPrice);
      }
      else
      {
        detail.set("costPrice", costPrice);
        detail.set("costMoneys", BigDecimalUtils.mul(baseAmount, costPrice));
      }
      detail.update(configName);
    }
    BigDecimal costPrice = detail.getBigDecimal("costPrice");
    if ((billTypeId == AioConstants.BILL_ROW_TYPE10) || (billTypeId == AioConstants.BILL_ROW_TYPE11)) {
      costPrice = detail.getBigDecimal("basePrice");
    }
    BigDecimal costMoneys = BigDecimalUtils.mul(costPrice, baseAmount);
    


    String type = "out";
    if (isRCW == AioConstants.RCW_VS)
    {
      if (inOrOut.equals("out")) {
        type = "in";
      }
    }
    else if (inOrOut.equals("in")) {
      type = "in";
    }
    StockRecordsController.addRecords(configName, billTypeId, type, bill, detail, costPrice, baseAmount, bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
    
    StockController.stockChange(configName, type, detail.getInt("storageId").intValue(), detail.getInt("productId").intValue(), costArith, baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
    
    AvgpriceConTroller.addAvgprice(configName, type, detail.getInt("storageId"), detail.getInt("productId"), baseAmount, costMoneys);
  }
  
  @Before({Tx.class})
  public void reloadBillCount()
  {
    String configName = loginConfigName();
    int billTypeId = 0;
    StringBuffer sql = null;
    List<Record> list = null;
    

    billTypeId = AioConstants.BILL_ROW_TYPE5;
    sql = new StringBuffer("select b.id,b.code,b.amounts amounts,b.taxMoneys,");
    sql.append(" sum(d.amount) damounts,");
    sql.append(" sum(d.money) dmoneys,");
    sql.append(" sum(d.discountMoney) ddiscountMoneys,");
    sql.append(" sum(d.taxes) dtaxess, ");
    sql.append(" sum(d.taxMoney) dtaxMoneys,");
    sql.append(" sum(d.retailMoney) dretailMoneys ");
    sql.append(" from cg_purchase_detail d left JOIN cg_purchase_bill b on d.billId=b.id ");
    sql.append(" GROUP BY b.id,b.code,b.amounts,b.taxMoneys having b.taxMoneys != dtaxMoneys");
    
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record pur_rr = (Record)list.get(i);
        int billId = pur_rr.getInt("id").intValue();
        
        BigDecimal oldTaxMoneys = pur_rr.getBigDecimal("taxMoneys");
        Model bill = PurchaseBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("amounts", pur_rr.getBigDecimal("damounts"));
        bill.set("moneys", pur_rr.getBigDecimal("dmoneys"));
        bill.set("discountMoneys", pur_rr.getBigDecimal("ddiscountMoneys"));
        bill.set("taxes", pur_rr.getBigDecimal("dtaxess"));
        bill.set("taxMoneys", pur_rr.getBigDecimal("dtaxMoneys"));
        bill.set("retailMoneys", pur_rr.getBigDecimal("dretailMoneys"));
        
        bill.set("privilegeMoney", BigDecimalUtils.sub(bill.getBigDecimal("taxMoneys"), bill.getBigDecimal("privilege")));
        bill.update(configName);
        BigDecimal newTaxMoneys = bill.getBigDecimal("taxMoneys");
        
        Db.use(configName).update("update bb_billhistory set money=" + newTaxMoneys + " where billId=" + billId + " and billTypeId=" + billTypeId);
        

        BigDecimal taxes = bill.getBigDecimal("taxes");
        BigDecimal purchaseMoneys = bill.getBigDecimal("taxMoneys");
        
        BigDecimal shouldPay = BigDecimalUtils.sub(purchaseMoneys, BigDecimalUtils.add(bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilege")));
        
        Db.use(configName).update("update cw_pay_type set payMoney=" + taxes + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 17);
        

        Db.use(configName).update("update cw_pay_type set payMoney=" + shouldPay + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 14);
        

        BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newTaxMoneys, oldTaxMoneys);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        } else {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        }
        BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney"));
        ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(AioConstants.BILL_ROW_TYPE5), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, bill.getBigDecimal("payMoney"));
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(AioConstants.BILL_ROW_TYPE5), Integer.valueOf(billId), null, null, null, null, null, null, null);
        }
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE6;
    sql = new StringBuffer("select b.id,b.code,b.amounts amounts,b.taxMoneys, ");
    sql.append(" sum(d.amount) damounts,");
    sql.append(" sum(d.money) dmoneys, ");
    sql.append(" sum(d.discountMoney) ddiscountMoneys,");
    sql.append(" sum(d.taxes) dtaxess, ");
    sql.append(" sum(d.taxMoney) dtaxMoneys,");
    sql.append(" sum(d.retailMoney) dretailMoneys ");
    sql.append(" from cg_return_detail d left JOIN cg_return_bill b on d.billId=b.id");
    sql.append(" GROUP BY b.id having b.taxMoneys != dtaxMoneys");
    
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        
        BigDecimal oldTaxMoneys = rr.getBigDecimal("taxMoneys");
        Model bill = PurchaseReturnBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("amounts", rr.getBigDecimal("damounts"));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.set("discountMoneys", rr.getBigDecimal("ddiscountMoneys"));
        bill.set("taxes", rr.getBigDecimal("dtaxess"));
        bill.set("taxMoneys", rr.getBigDecimal("dtaxMoneys"));
        bill.set("retailMoneys", rr.getBigDecimal("dretailMoneys"));
        

        bill.set("privilegeMoney", BigDecimalUtils.sub(bill.getBigDecimal("taxMoneys"), bill.getBigDecimal("privilege")));
        bill.update(configName);
        BigDecimal newTaxMoneys = bill.getBigDecimal("taxMoneys");
        

        Db.use(configName).update("update bb_billhistory set money=" + newTaxMoneys + " where billId=" + billId + " and billTypeId=" + billTypeId);
        

        BigDecimal taxes = bill.getBigDecimal("taxes");
        BigDecimal purchaseMoneys = bill.getBigDecimal("taxMoneys");
        BigDecimal shouldReceive = BigDecimalUtils.sub(purchaseMoneys, BigDecimalUtils.add(bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilege")));
        
        Db.use(configName).update("update cw_pay_type set payMoney=" + taxes + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 17);
        

        Db.use(configName).update("update cw_pay_type set payMoney=" + shouldReceive + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 14);
        

        BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newTaxMoneys, oldTaxMoneys);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        } else {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        }
        BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney"));
        ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, Integer.valueOf(AioConstants.BILL_ROW_TYPE6), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, bill.getBigDecimal("payMoney"));
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, Integer.valueOf(AioConstants.BILL_ROW_TYPE6), Integer.valueOf(billId), null, null, null, null, null, null, null);
        }
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE4;
    

    sql = new StringBuffer("select b.id,b.inAmount,b.inMoney,b.gapMoney,b.outAmount,b.outMoney,");
    sql.append(" sum(d.amount) dinAmount,");
    sql.append(" SUM(d.taxMoney) dinMoney, ");
    sql.append(" SUM(d.taxMoney)-SUM(c.taxMoney) dgapMoney,");
    sql.append(" SUM(c.amount) doutAmount,");
    sql.append(" SUM(c.taxMoney) doutMoney");
    sql.append(" from cg_barter_bill b LEFT JOIN cg_barter_detail d on (b.id = d.billId and d.type = 1)");
    sql.append(" LEFT JOIN cg_barter_detail c on (b.id = c.billId and c.type = 2)");
    sql.append(" GROUP BY b.id HAVING dgapMoney!=b.gapMoney");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        
        BigDecimal oldgapMoney = rr.getBigDecimal("gapMoney");
        
        Model bill = PurchaseBarterBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("inAmount", rr.getBigDecimal("dinAmount"));
        bill.set("inMoney", rr.getBigDecimal("dinMoney"));
        bill.set("gapMoney", rr.getBigDecimal("dgapMoney"));
        bill.set("outAmount", rr.getBigDecimal("doutAmount"));
        bill.set("outMoney", rr.getBigDecimal("doutMoney"));
        
        bill.set("privilegeMoney", BigDecimalUtils.sub(bill.getBigDecimal("gapMoney"), bill.getBigDecimal("privilege")));
        bill.update(configName);
        BigDecimal newgapMoney = bill.getBigDecimal("gapMoney");
        
        Db.use(configName).update("update bb_billhistory set money=" + newgapMoney + " where billId=" + billId + " and billTypeId=" + billTypeId);
        

        BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newgapMoney, oldgapMoney);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        } else {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        }
        BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney"));
        ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(AioConstants.BILL_ROW_TYPE12), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, bill.getBigDecimal("payMoney"));
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(AioConstants.BILL_ROW_TYPE12), Integer.valueOf(billId), null, null, null, null, null, null, null);
        }
        BigDecimal shouldPay = BigDecimalUtils.sub(newgapMoney, BigDecimalUtils.add(bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilege")));
        Db.use(configName).update("UPDATE cw_pay_type SET payMoney = " + shouldPay + " WHERE accountId = 14 AND billId=" + billId + " AND billTypeId=" + billTypeId);
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE4;
    

    sql = new StringBuffer("select b.id,b.code,b.amounts,b.taxMoneys");
    sql.append(",sum(d.amount) damounts");
    sql.append(",sum(d.money) dmoneys");
    sql.append(", sum(d.discountMoney) ddiscountMoneys");
    sql.append(", sum(d.taxes) dtaxess");
    sql.append(", sum(d.taxMoney) dtaxMoneys");
    sql.append(", sum(d.retailMoney) dretailMoneys");
    sql.append(" from xs_sell_bill b left join xs_sell_detail d on d.billId=b.id   group by b.id  having b.taxMoneys != dtaxMoneys");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        BigDecimal oldTaxMoneys = rr.getBigDecimal("taxMoneys");
        
        Model bill = SellBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("amounts", rr.getBigDecimal("damounts"));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.set("discountMoneys", rr.getBigDecimal("ddiscountMoneys"));
        bill.set("taxes", rr.getBigDecimal("dtaxess"));
        bill.set("taxMoneys", rr.getBigDecimal("dtaxMoneys"));
        bill.set("retailMoneys", rr.getBigDecimal("dretailMoneys"));
        
        bill.set("privilegeMoney", BigDecimalUtils.sub(bill.getBigDecimal("taxMoneys"), bill.getBigDecimal("privilege")));
        bill.update(configName);
        BigDecimal newTaxMoneys = bill.getBigDecimal("taxMoneys");
        
        Db.use(configName).update("update bb_billhistory set money=" + bill.getBigDecimal("taxMoneys") + " where billId=" + billId + " and billTypeId=" + billTypeId);
        

        BigDecimal taxes = bill.getBigDecimal("taxes");
        BigDecimal sellMoneys = bill.getBigDecimal("discountMoneys");
        BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney"));
        
        Db.use(configName).update("update cw_pay_type set payMoney=" + taxes + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 17);
        
        Db.use(configName).update("update cw_pay_type set payMoney=" + sellMoneys + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 20);
        
        Db.use(configName).update("update cw_pay_type set payMoney=" + needGetOrPay + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 53);
        


        BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newTaxMoneys, oldTaxMoneys);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        } else {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        }
        BigDecimal getOrPayMoneys = bill.getBigDecimal("payMoney");
        ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, getOrPayMoneys);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
        }
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE7;
    

    sql = new StringBuffer("select b.id,b.code,b.amounts,b.taxMoneys");
    sql.append(",sum(d.amount) damounts");
    sql.append(",sum(d.money) dmoneys");
    sql.append(", sum(d.discountMoney) ddiscountMoneys");
    sql.append(", sum(d.taxes) dtaxess");
    sql.append(", sum(d.taxMoney) dtaxMoneys");
    sql.append(", sum(d.retailMoney) dretailMoneys");
    sql.append(" from xs_return_bill b left join xs_return_detail d on d.billId=b.id   group by b.id  having b.taxMoneys != dtaxMoneys");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        BigDecimal oldTaxMoneys = rr.getBigDecimal("taxMoneys");
        
        Model bill = SellReturnBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("amounts", rr.getBigDecimal("damounts"));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.set("discountMoneys", rr.getBigDecimal("ddiscountMoneys"));
        bill.set("taxes", rr.getBigDecimal("dtaxess"));
        bill.set("taxMoneys", rr.getBigDecimal("dtaxMoneys"));
        bill.set("retailMoneys", rr.getBigDecimal("dretailMoneys"));
        
        bill.set("privilegeMoney", BigDecimalUtils.sub(bill.getBigDecimal("taxMoneys"), bill.getBigDecimal("privilege")));
        bill.update(configName);
        BigDecimal newTaxMoneys = bill.getBigDecimal("taxMoneys");
        
        Db.use(configName).update("update bb_billhistory set money=" + bill.getBigDecimal("taxMoneys") + " where billId=" + billId + " and billTypeId=" + billTypeId);
        

        BigDecimal taxes = bill.getBigDecimal("taxes");
        BigDecimal sellReturnMoneys = bill.getBigDecimal("discountMoneys");
        BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney"));
        
        Db.use(configName).update("update cw_pay_type set payMoney=" + taxes + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 17);
        
        Db.use(configName).update("update cw_pay_type set payMoney=" + sellReturnMoneys + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 20);
        
        Db.use(configName).update("update cw_pay_type set payMoney=" + needGetOrPay + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 53);
        



        BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newTaxMoneys, oldTaxMoneys);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        } else {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        }
        BigDecimal getOrPayMoneys = bill.getBigDecimal("payMoney");
        ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, getOrPayMoneys);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
        }
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE13;
    sql = new StringBuffer(
      "select b.id,b.inAmount,b.inMoney,b.gapMoney,b.outAmount,b.outMoney,");
    sql.append(" SUM(d.amount) dinAmount,");
    sql.append(" SUM(d.taxMoney) dinMoney,");
    sql.append(" SUM(c.taxMoney) - SUM(d.taxMoney) dgapMoney,");
    sql.append(" SUM(c.amount) doutAmount,");
    sql.append(" SUM(c.taxMoney) doutMoney");
    sql.append(" from xs_barter_bill b LEFT JOIN xs_barter_detail d on (b.id = d.billId and d.type = 1)");
    sql.append(" LEFT JOIN xs_barter_detail c on (b.id= c.billId and c.type = 2)");
    sql.append(" GROUP BY b.id HAVING dgapMoney!= b.gapMoney");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        BigDecimal oldgapMoney = rr.getBigDecimal("gapMoney");
        

        Model bill = SellBarterBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("inAmount", rr.getBigDecimal("dinAmount"));
        bill.set("inMoney", rr.getBigDecimal("dinMoney"));
        bill.set("gapMoney", rr.getBigDecimal("dgapMoney"));
        bill.set("outAmount", rr.getBigDecimal("doutAmount"));
        bill.set("outMoney", rr.getBigDecimal("doutMoney"));
        
        bill.set("privilegeMoney", BigDecimalUtils.sub(bill.getBigDecimal("gapMoney"), bill.getBigDecimal("privilege")));
        bill.update(configName);
        BigDecimal newgapMoney = bill.getBigDecimal("gapMoney");
        

        Db.use(configName).update("update bb_billhistory set money=" + newgapMoney + " where billId=" + billId + " and billTypeId=" + billTypeId);
        

        BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newgapMoney, oldgapMoney);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        } else {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        }
        BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney"));
        ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, bill.getBigDecimal("payMoney"));
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
        }
        BigDecimal shouldPay = BigDecimalUtils.sub(newgapMoney, BigDecimalUtils.add(bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilege")));
        Db.use(configName).update("UPDATE cw_pay_type SET payMoney = " + shouldPay + " WHERE accountId in (53,20) AND billId=" + billId + " AND billTypeId=" + billTypeId);
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE10;
    
    sql = new StringBuffer("select b.id,b.code,b.amounts");
    sql.append(",sum(d.amount) damounts");
    sql.append(",sum(d.money) dmoneys");
    sql.append(", sum(d.retailMoney) dretailMoneys");
    sql.append(" from cc_otherin_bill b left join cc_otherin_detail d on d.billId=b.id   group by b.id  having b.amounts != damounts");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        
        Model bill = OtherInBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("amounts", rr.getBigDecimal("damounts"));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.set("retailMoneys", rr.getBigDecimal("dretailMoneys"));
        bill.update(configName);
        
        Db.use(configName).update("update bb_billhistory set money=" + bill.getBigDecimal("moneys") + " where billId=" + billId + " and billTypeId=" + billTypeId);
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE11;
    
    sql = new StringBuffer("select b.id,b.code,b.amounts");
    sql.append(",sum(d.amount) damounts");
    sql.append(",sum(d.money) dmoneys");
    sql.append(", sum(d.retailMoney) dretailMoneys");
    sql.append(" from cc_otherout_bill b left join cc_otherout_detail d on d.billId=b.id   group by b.id  having b.amounts != damounts");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        
        Model bill = OtherOutBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("amounts", rr.getBigDecimal("damounts"));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.set("retailMoneys", rr.getBigDecimal("dretailMoneys"));
        bill.update(configName);
        
        Db.use(configName).update("update bb_billhistory set money=" + bill.getBigDecimal("moneys") + " where billId=" + billId + " and billTypeId=" + billTypeId);
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE15;
    
    sql = new StringBuffer("select * from (select b.id,b.moneys");
    sql.append(",(select sum(d.discountMoney) from cc_difftallot_detail d where d.billId=b.id) ddiscountMoney");
    sql.append(" from cc_difftallot_bill b) temp where temp.moneys!=temp.ddiscountMoney");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        
        Model bill = DifftAllotBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("moneys", rr.getBigDecimal("ddiscountMoney"));
        bill.update(configName);
        
        Db.use(configName).update("update bb_billhistory set money=" + bill.getBigDecimal("moneys") + " where billId=" + billId + " and billTypeId=" + billTypeId);
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE17;
    
    sql = new StringBuffer("select b.id,b.code,b.moneys");
    sql.append(",sum(d.money) dmoneys");
    sql.append(" from cw_getmoney_bill b left join cw_getmoney_detail d on d.billId=b.id   group by b.id  having b.moneys != dmoneys");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        BigDecimal oldTaxMoneys = rr.getBigDecimal("moneys");
        
        Model bill = GetMoneyBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        
        bill.set("privilegeMoney", BigDecimalUtils.add(bill.getBigDecimal("moneys"), bill.getBigDecimal("privilege")));
        
        BigDecimal newTaxMoneys = bill.getBigDecimal("moneys");
        bill.set("canAssignMoney", BigDecimalUtils.add(bill.getBigDecimal("canAssignMoney"), BigDecimalUtils.sub(oldTaxMoneys, newTaxMoneys)));
        bill.update(configName);
        
        Db.use(configName).update("update bb_billhistory set money=" + bill.getBigDecimal("moneys") + " where billId=" + billId + " and billTypeId=" + billTypeId);
        
        BigDecimal needGetOrPay = bill.getBigDecimal("privilegeMoney");
        
        Db.use(configName).update("update cw_pay_type set payMoney=" + needGetOrPay + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 53);
        
        BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newTaxMoneys, oldTaxMoneys);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        } else {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        }
        ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, null);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
        }
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE19;
    
    sql = new StringBuffer("select b.id,b.code,b.moneys");
    sql.append(",sum(d.money) dmoneys");
    sql.append(" from cw_paymoney_bill b left join cw_paymoney_detail d on d.billId=b.id   group by b.id  having b.moneys != dmoneys");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        BigDecimal oldTaxMoneys = rr.getBigDecimal("moneys");
        
        Model bill = PayMoneyBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        
        bill.set("privilegeMoney", BigDecimalUtils.add(bill.getBigDecimal("moneys"), bill.getBigDecimal("privilege")));
        
        BigDecimal newTaxMoneys = bill.getBigDecimal("moneys");
        bill.set("canAssignMoney", BigDecimalUtils.add(bill.getBigDecimal("canAssignMoney"), BigDecimalUtils.sub(oldTaxMoneys, newTaxMoneys)));
        bill.update(configName);
        
        Db.use(configName).update("update bb_billhistory set money=" + newTaxMoneys + " where billId=" + billId + " and billTypeId=" + billTypeId);
        
        BigDecimal needGetOrPay = bill.getBigDecimal("privilegeMoney");
        
        Db.use(configName).update("update cw_pay_type set payMoney=" + needGetOrPay + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=14");
        
        BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newTaxMoneys, oldTaxMoneys);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        } else {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        }
        ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, null);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
        }
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE21;
    
    sql = new StringBuffer("select b.id,b.code,b.moneys");
    sql.append(",sum(d.money) dmoneys");
    sql.append(" from cw_transfer_bill b left join cw_transfer_detail d on d.billId=b.id   group by b.id  having b.moneys != dmoneys");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        
        Model bill = TransferBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.update(configName);
        BigDecimal newTaxMoneys = bill.getBigDecimal("moneys");
        
        Db.use(configName).update("update bb_billhistory set money=" + newTaxMoneys + " where billId=" + billId + " and billTypeId=" + billTypeId);
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE23;
    
    sql = new StringBuffer("select b.id,b.code,b.moneys");
    sql.append(",sum(d.money) dmoneys");
    sql.append(" from cw_addassets_bill b left join cw_addassets_detail d on d.billId=b.id group by b.id  having b.moneys != dmoneys");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        
        Model bill = AddAssetsBill.dao.findById(configName, Integer.valueOf(billId));
        BigDecimal oldTaxMoneys = bill.getBigDecimal("moneys");
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.update(configName);
        BigDecimal newTaxMoneys = bill.getBigDecimal("moneys");
        BigDecimal payMoney = bill.getBigDecimal("payMoney");
        
        Db.use(configName).update("update bb_billhistory set money=" + newTaxMoneys + " where billId=" + billId + " and billTypeId=" + billTypeId);
        

        BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newTaxMoneys, oldTaxMoneys);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        } else {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        }
        ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), newTaxMoneys, payMoney);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
        }
        BigDecimal shouldPay = BigDecimalUtils.sub(newTaxMoneys, payMoney);
        Db.use(configName).update("UPDATE cw_pay_type SET payMoney = " + shouldPay + " WHERE accountId = 14 AND billId=" + billId + " AND billTypeId=" + billTypeId);
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE24;
    
    sql = new StringBuffer("select b.id,b.code,b.moneys");
    sql.append(",sum(d.money) dmoneys");
    sql.append(" from cw_deprassets_bill b left join cw_deprassets_detail d on d.billId=b.id group by b.id  having b.moneys != dmoneys");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        
        Model bill = DeprAssetsBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.update(configName);
        BigDecimal newTaxMoneys = bill.getBigDecimal("moneys");
        
        Db.use(configName).update("update bb_billhistory set money=" + newTaxMoneys + " where billId=" + billId + " and billTypeId=" + billTypeId);
        

        Db.use(configName).update("UPDATE cw_pay_type SET payMoney = " + newTaxMoneys + " WHERE accountId = 41 AND billId=" + billId + " AND billTypeId=" + billTypeId);
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE25;
    
    sql = new StringBuffer("select b.id,b.code,b.moneys");
    sql.append(",sum(d.money) dmoneys");
    sql.append(" from cw_subassets_bill b left join cw_subassets_detail d on d.billId=b.id group by b.id  having b.moneys != dmoneys");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        
        Model bill = SubAssetsBill.dao.findById(configName, Integer.valueOf(billId));
        BigDecimal oldTaxMoneys = bill.getBigDecimal("moneys");
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.update(configName);
        BigDecimal newTaxMoneys = bill.getBigDecimal("moneys");
        
        Db.use(configName).update("update bb_billhistory set money=" + newTaxMoneys + " where billId=" + billId + " and billTypeId=" + billTypeId);
        

        BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newTaxMoneys, oldTaxMoneys);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        } else {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        }
        ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), newTaxMoneys, bill.getBigDecimal("getMoney"));
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
        }
        BigDecimal shouldPay = BigDecimalUtils.sub(newTaxMoneys, bill.getBigDecimal("getMoney"));
        Db.use(configName).update("UPDATE cw_pay_type SET payMoney = " + shouldPay + " WHERE accountId = 53 AND billId=" + billId + " AND billTypeId=" + billTypeId);
      }
    }
    billTypeId = AioConstants.BILL_ROW_TYPE22;
    
    sql = new StringBuffer("select b.id,b.code,b.moneys");
    sql.append(",sum(d.money) dmoneys");
    sql.append(" from cw_fee_bill b left join cw_fee_detail d on d.billId=b.id   group by b.id  having b.moneys != dmoneys");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int billId = rr.getInt("id").intValue();
        BigDecimal oldTaxMoneys = rr.getBigDecimal("moneys");
        
        Model bill = FeeBill.dao.findById(configName, Integer.valueOf(billId));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.update(configName);
        BigDecimal newTaxMoneys = bill.getBigDecimal("moneys");
        
        Db.use(configName).update("update bb_billhistory set money=" + bill.getBigDecimal("moneys") + " where billId=" + billId + " and billTypeId=" + billTypeId);
        
        Integer unitId = bill.getInt("unitId");
        if ((unitId != null) && (unitId.intValue() != 0))
        {
          BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("moneys"), bill.getBigDecimal("getMoney"));
          
          Db.use(configName).update("update cw_pay_type set payMoney=" + needGetOrPay + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 53);
          

          BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newTaxMoneys, oldTaxMoneys);
          if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
            PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
          } else {
            PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
          }
          ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), bill.getBigDecimal("moneys"), bill.getBigDecimal("getMoney"));
          if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
            ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
          }
        }
      }
    }
    sql = new StringBuffer("select b.id,b.code,b.moneys");
    sql.append(",sum(d.money) dmoneys");
    sql.append(" from cw_c_unitgetorpay_draft_bill b left join cw_c_unitgetorpay_detail d on d.billId=b.id   group by b.id,b.mergeType  having b.moneys != dmoneys");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int mergeType = rr.getInt("mergeType").intValue();
        if (mergeType == 0) {
          billTypeId = AioConstants.BILL_ROW_TYPE30;
        } else if (mergeType == 1) {
          billTypeId = AioConstants.BILL_ROW_TYPE31;
        } else if (mergeType == 2) {
          billTypeId = AioConstants.BILL_ROW_TYPE31;
        } else if (mergeType == 3) {
          billTypeId = AioConstants.BILL_ROW_TYPE31;
        }
        int billId = rr.getInt("id").intValue();
        BigDecimal oldTaxMoneys = rr.getBigDecimal("moneys");
        
        Model bill = ChangeGetOrPay.dao.findById(configName, Integer.valueOf(billId));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.update(configName);
        BigDecimal newTaxMoneys = bill.getBigDecimal("moneys");
        
        Db.use(configName).update("update bb_billhistory set money=" + bill.getBigDecimal("moneys") + " where billId=" + billId + " and billTypeId=" + billTypeId);
        

        BigDecimal needGetOrPay = bill.getBigDecimal("moneys");
        
        Db.use(configName).update("update cw_pay_type set payMoney=" + needGetOrPay + " where billTypeId=" + billTypeId + " and billId=" + billId + " and accountId=" + 53);
        

        BigDecimal needGetOrPayChange = BigDecimalUtils.sub(newTaxMoneys, oldTaxMoneys);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        } else {
          PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, needGetOrPayChange);
        }
        ArapRecordsController.addRecords(configName, false, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), bill.getBigDecimal("moneys"), null);
        if (AioConstants.RCW_VS == bill.getInt("isRCW").intValue()) {
          ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
        }
      }
    }
    sql = new StringBuffer("select b.id,b.code,b.moneys");
    sql.append(",sum(d.money) dmoneys");
    sql.append(" from cw_c_money_bill b left join cw_c_money_detail d on d.billId=b.id   group by b.id,b.mergeType  having b.moneys != dmoneys");
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record rr = (Record)list.get(i);
        int mergeType = rr.getInt("mergeType").intValue();
        if (mergeType == 0) {
          billTypeId = AioConstants.BILL_ROW_TYPE30;
        } else {
          billTypeId = AioConstants.BILL_ROW_TYPE31;
        }
        int billId = rr.getInt("id").intValue();
        
        Model bill = ChangeMoney.dao.findById(configName, Integer.valueOf(billId));
        bill.set("moneys", rr.getBigDecimal("dmoneys"));
        bill.update(configName);
        
        Db.use(configName).update("update bb_billhistory set money=" + bill.getBigDecimal("moneys") + " where billId=" + billId + " and billTypeId=" + billTypeId);
      }
    }
    this.result.put("message", "操作成功");
    this.result.put("statusCode", AioConstants.HTTP_RETURN200);
    renderJson(this.result);
  }

  */
}
