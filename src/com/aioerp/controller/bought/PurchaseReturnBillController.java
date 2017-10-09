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
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.bought.PurchaseDetail;
import com.aioerp.model.bought.PurchaseReturnBill;
import com.aioerp.model.bought.PurchaseReturnDetail;
import com.aioerp.model.bought.PurchaseReturnDraftBill;
import com.aioerp.model.bought.PurchaseReturnDraftDetail;
import com.aioerp.model.bought.ResultHelp;
import com.aioerp.model.finance.PayDraft;
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
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;

public class PurchaseReturnBillController
  extends BaseController
{
  public void index()
  {
    String configName = loginConfigName();
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE6, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE6));
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, AioConstants.BILL_ROW_TYPE6, "discount")));
    
    billCodeAuto(AioConstants.BILL_ROW_TYPE6);
    
    loadOnlyOneBaseInfo(configName);
    

    notEditStaff();
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws SQLException
  {
    String configName = loginConfigName();
    
    printBeforSave1();
    PurchaseReturnBill bill = (PurchaseReturnBill)getModel(PurchaseReturnBill.class);
    
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
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    bill.set("id", null);
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    
    boolean comfirm = getParaToBoolean("needComfirm", Boolean.valueOf(true)).booleanValue();
    boolean hasOther = PurchaseReturnBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE6));
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseReturnDetail.class, "purchaseReturnDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer storageId = getParaToInt("storage.id");
    
    List<ResultHelp> rList = new ArrayList();
    

    Boolean isNegativeStock = SysConfig.getConfig(configName, 1);
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Integer detailstorage = detail.getInt("storageId");
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1);
      if (((storageId == null) || (storageId.intValue() == 0)) && ((detailstorage == null) || (detailstorage.intValue() == 0)))
      {
        setAttr("message", "第" + trIndex + "行多仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      Integer productId = detail.getInt("productId");
      BigDecimal returnAmount = detail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        Integer selectUnitId = detail.getInt("selectUnitId");
        String selectUnit = product.getStr("calculateUnit1");
        if (2 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit2");
        } else if (3 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit3");
        }
        Integer detailStorageId = detail.getInt("storageId");
        if ((detailStorageId == null) || (detailStorageId.intValue() == 0)) {
          detailStorageId = storageId;
        }
        Integer costArith = product.getInt("costArith");
        BigDecimal amount = DwzUtils.getConversionAmount(returnAmount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal sAmount = BigDecimal.ZERO;
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4)
        {
          sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), detailStorageId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        }
        else
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, storageId.intValue(), product, detail.getBigDecimal("costPrice"), selectUnitId.intValue());
          sAmount = new BigDecimal((String)map.get("stockAmount"));
          detail.set("costPrice", new BigDecimal((String)map.get("costPrice")));
        }
        BigDecimal avgPrice = StockController.subAvgPrice(configName, detail, costArith, amount, detail.getBigDecimal("costPrice"), detailStorageId.intValue(), productId.intValue());
        if ((comfirm) && (isNegativeStock.booleanValue()) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0))
        {
          if (SysConfig.getConfig(configName, 15).booleanValue())
          {
            Storage storage = (Storage)Storage.dao.findById(configName, detailStorageId);
            ResultHelp help = new ResultHelp(Integer.valueOf(trIndex), productId, product.getStr("fullName"), 
              product.getStr("code"), sAmount, BigDecimalUtils.sub(sAmount, amount), detailStorageId, 
              storage.getStr("fullName"), storage.getStr("code"), avgPrice, selectUnit);
            rList.add(help);
          }
        }
        else if (((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0)) || ((!isNegativeStock.booleanValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0)))
        {
          setAttr("message", "错误类型：库存数量不够<br/> 商品编号：" + product.getStr("code") + "<br/> 商品全名：" + product.getStr("fullName"));
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
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
    bill.set("unitId", getParaToInt("unit.id"));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("userId", Integer.valueOf(loginUserId()));
    bill.set("storageId", storageId);
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    if (bill.get("discountMoneys") == null) {
      bill.set("discountMoneys", bill.get("moneys"));
    }
    if (bill.get("taxMoneys") == null) {
      bill.set("taxMoneys", bill.get("discountMoneys"));
    }
    if (draftId != 0) {
      billCodeIncrease(bill, "draftPost");
    } else {
      billCodeIncrease(bill, "add");
    }
    bill.save(configName);
    
    BillHistoryController.saveBillHistory(configName, bill, AioConstants.BILL_ROW_TYPE6, bill.getBigDecimal("taxMoneys"));
    BigDecimal costMoneys = BigDecimal.ZERO;
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      detail.set("id", null);
      Integer detailstorage = detail.getInt("storageId");
      if ((detailstorage == null) || (detailstorage.intValue() == 0)) {
        detail.set("storageId", storageId);
      }
      if (detail.get("discountPrice") == null) {
        detail.set("discountPrice", detail.get("price"));
      }
      if (detail.get("taxPrice") == null) {
        detail.set("taxPrice", detail.get("discountPrice"));
      }
      if (detail.get("discountMoney") == null) {
        detail.set("discountMoney", detail.get("money"));
      }
      if (detail.get("taxMoney") == null) {
        detail.set("taxMoney", detail.get("discountMoney"));
      }
      Integer productId = detail.getInt("productId");
      Integer bDetailId = detail.getInt("detailId");
      Record record = PurchaseDetail.dao.getBill(configName, bDetailId);
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
      BigDecimal price = detail.getBigDecimal("price");
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
      BigDecimal returnAmount = detail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        

        Integer selectUnitId = detail.getInt("selectUnitId");
        Integer detailStorageId = detail.getInt("storageId");
        
        BigDecimal amount = DwzUtils.getConversionAmount(returnAmount, selectUnitId, product, Integer.valueOf(1));
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, product, Integer.valueOf(1));
        
        detail.set("baseAmount", amount);
        detail.set("basePrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          detail.set("giftAmount", amount);
        }
        Integer costArith = product.getInt("costArith");
        


        BigDecimal sAmount = BigDecimal.ZERO;
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
          sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), detailStorageId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
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
        Integer purchaseDetailId = detail.getInt("detailId");
        BigDecimal stockPrice = detail.getBigDecimal("costPrice");
        BigDecimal costMoney = BigDecimalUtils.mul(amount, stockPrice);
        if (detail.getBigDecimal("costPrice") == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, storageId.intValue(), product, detail.getBigDecimal("costPrice"), selectUnitId.intValue());
          costMoney = BigDecimalUtils.mul(amount, new BigDecimal((String)map.get("costPrice")));
          stockPrice = new BigDecimal((String)map.get("costPrice"));
          detail.set("costPrice", stockPrice);
        }
        if (purchaseDetailId != null) {
          stockPrice = detail.getBigDecimal("costPrice");
        }
        if (((HelpUtil)helpUitlList.get(i)).getCostPrice() != null) {
          stockPrice = ((HelpUtil)helpUitlList.get(i)).getCostPrice();
        }
        if ((AioConstants.PRD_COST_PRICE1 == costArith.intValue()) && (detail.getBigDecimal("costPrice") != null)) {
          StockController.stockChangeSubYDPrice(configName, storageId.intValue(), productId.intValue(), costArith.intValue(), amount, detail.getBigDecimal("costPrice"));
        }
        StockController.stockSubChange(configName, costArith.intValue(), amount, detailStorageId.intValue(), productId.intValue(), stockPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"), null);
        
        StockRecordsController.addRecords(configName, AioConstants.BILL_ROW_TYPE6, "out", bill, detail, stockPrice, amount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
        


        AvgpriceConTroller.addAvgprice(configName, "out", detailStorageId, productId, amount, costMoney);
        if (purchaseDetailId != null)
        {
          PurchaseDetail purchaseDetail = (PurchaseDetail)PurchaseDetail.dao.findById(configName, purchaseDetailId);
          PurchaseDetailController.editDetailAmount(configName, purchaseDetail, amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3);
        }
        detail.save(configName);
        
        costMoneys = BigDecimalUtils.add(costMoneys, BigDecimalUtils.mul(stockPrice, detail.getBigDecimal("baseAmount")));
      }
    }
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    
    Map<String, Object> record = new HashMap();
    record.put("privilegeType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    record.put("getOrPayType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    record.put("proAccountType", Integer.valueOf(AioConstants.ACCOUNT_MONEY1));
    record.put("proAccountList", detailList);
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    record.put("accounts", getOtherAccount(bill.getBigDecimal("taxes"), BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney")), BigDecimalUtils.sub(bill.getBigDecimal("discountMoneys"), costMoneys)));
    PayTypeController.addAccountsRecoder(configName, AioConstants.BILL_ROW_TYPE6, bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), bill.getBigDecimal("privilege"), payTypeIds, payTypeMoneys, record);
    

    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
    





    BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney"));
    Unit unit = (Unit)Unit.dao.findById(configName, bill.getInt("unitId"));
    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, Integer.valueOf(AioConstants.BILL_ROW_TYPE6), null, bill, unit.getInt("areaId"), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, bill.getBigDecimal("payMoney"));
    

    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      setDraftStrs();
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      PurchaseReturnDraftBill.dao.deleteById(configName, draftBillId);
      PurchaseReturnDraftDetail.dao.delByBill(configName, draftBillId);
      PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(AioConstants.BILL_ROW_TYPE6));
      
      StockDraftRecords.delRecords(configName, AioConstants.BILL_ROW_TYPE6, draftBillId.intValue());
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "purchaseReturnView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE6, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    
    renderJson();
  }
  
  public void negativeOption()
  {
    List<ResultHelp> list = (List)getSessionAttr("resultList");
    setAttr("rList", list);
    render("negativeOption.html");
  }
  
  public void setStockPrice()
  {
    List<ResultHelp> list = (List)getSessionAttr("resultList");
    List<ResultHelp> recordList = new ArrayList();
    for (ResultHelp resultHelp : list)
    {
      BigDecimal avgPrice = resultHelp.getAvgPrice();
      if ((avgPrice == null) || (BigDecimalUtils.compare(avgPrice, BigDecimal.ZERO) == 0)) {
        recordList.add(resultHelp);
      }
    }
    getSession().removeAttribute("resultList");
    setAttr("recordList", recordList);
    render("stockCostPriceOption.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> result = new HashMap();
    PurchaseReturnBill bill = (PurchaseReturnBill)PurchaseReturnBill.dao.findById(configName, billId);
    if (bill == null)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", "该进货退货单不存在");
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
    Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unit.id"), AioConstants.WAY_GET, payMoney, privilegeMoney);
    if (vmap != null)
    {
      result = vmap;
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      return result;
    }
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
    bill.update(configName);
    PurchaseReturnBill newBill = bill;
    newBill.set("id", null);
    newBill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
    Date time = new Date();
    newBill.set("updateTime", time);
    newBill.set("recodeDate", time);
    
    billRcwAddRemark(bill, null);
    newBill.save(configName);
    BillHistoryController.saveBillHistory(configName, newBill, AioConstants.BILL_ROW_TYPE6, newBill.getBigDecimal("taxMoneys"));
    
    List<Model> detailList = PurchaseReturnDetail.dao.getListByBillId(configName, billId, basePrivs(PRODUCT_PRIVS), basePrivs(STORAGE_PRIVS));
    for (Model odlDetail : detailList)
    {
      PurchaseReturnDetail newDetail = (PurchaseReturnDetail)odlDetail;
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.set("id", null);
      newDetail.set("billId", newBill.getInt("id"));
      newDetail.set("updateTime", time);
      newDetail.save(configName);
      
      BigDecimal amount = newDetail.getBigDecimal("baseAmount");
      Integer productId = newDetail.getInt("productId");
      Integer storageId = newDetail.getInt("storageId");
      Product product = (Product)newDetail.get("product");
      BigDecimal costPrice = newDetail.getBigDecimal("costPrice");
      
      AvgpriceConTroller.addAvgprice(configName, "in", storageId, productId, amount, BigDecimalUtils.mul(amount, costPrice));
      StockController.stockChange(configName, "in", storageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), amount, costPrice, newDetail.getStr("batch"), newDetail.getDate("produceDate"), newDetail.getDate("produceEndDate"));
      Timestamp times = new Timestamp(time.getTime());
      StockRecordsController.addRecords(configName, AioConstants.BILL_ROW_TYPE6, "in", newBill, newDetail, costPrice, amount, time, times, newDetail.getInt("storageId"));
    }
    PayTypeController.rcwAccountsRecoder(configName, AioConstants.BILL_ROW_TYPE6, billId, newBill.getInt("id"));
    

    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
    

    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, Integer.valueOf(AioConstants.BILL_ROW_TYPE6), billId, null, null, null, null, null, null, null);
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
    data.put("reportName", "进货退货单");
    Integer unitId = getParaToInt("unit.id");
    
    List<String> headData = new ArrayList();
    
    setHeadUnitData(headData, unitId);
    headData.add("收货单位:" + getPara("unit.fullName", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门 :" + getPara("department.fullName", ""));
    headData.add("付款日期:" + getPara("purchaseReturnBill.payDate", ""));
    String recodeDate = DateUtils.format(getParaToDate("purchaseReturnBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    
    headData.add("发货仓库:" + getPara("storage.fullName", ""));
    headData.add("摘要:" + getPara("purchaseReturnBill.remark", ""));
    headData.add("附加说明 :" + getPara("purchaseReturnBill.memo", ""));
    headData.add("整单折扣 :" + getPara("purchaseReturnBill.discounts", ""));
    headData.add("单据编号:" + getPara("purchaseReturnBill.code", ""));
    headData.add("付款账户:" + getPara("payTypeAccounts", ""));
    headData.add("付款金额:" + getPara("purchaseReturnBill.payMoney", ""));
    headData.add("优惠金额:" + getPara("purchaseReturnBill.privilege", ""));
    headData.add("优惠后金额:" + getPara("purchaseReturnBill.privilegeMoney", ""));
    Integer userId = null;
    if ((billId != null) && (billId.intValue() != 0))
    {
      Model bill = PurchaseReturnBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    else if ((draftId != null) && (draftId.intValue() != 0))
    {
      Model bill = PurchaseReturnDraftBill.dao.findById(configName, billId);
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
    colTitle.add("零售价");
    colTitle.add("零金额");
    colTitle.add("备注");
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseReturnDetail.class, "purchaseReturnDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailList = PurchaseReturnDetail.dao.getListByBillId(configName, billId, basePrivs(PRODUCT_PRIVS), basePrivs(STORAGE_PRIVS));
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
  
  public static List<Record> getOtherAccount(BigDecimal taxes, BigDecimal shouldPay, BigDecimal disMoney)
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
    should.set("moneyType", Integer.valueOf(AioConstants.ACCOUNT_MONEY1));
    should.set("accountType", Integer.valueOf(AioConstants.ACCOUNT_TYPE3));
    should.set("arapAccount", Integer.valueOf(AioConstants.ARAPACCOUNT0));
    should.set("money", shouldPay);
    should.set("account", "00013");
    accountList.add(should);
    
    Record dis = new Record();
    dis.set("moneyType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    dis.set("accountType", Integer.valueOf(AioConstants.ACCOUNT_TYPE0));
    dis.set("arapAccount", Integer.valueOf(AioConstants.ARAPACCOUNT0));
    dis.set("money", disMoney);
    dis.set("account", "00026");
    accountList.add(dis);
    return accountList;
  }
}
