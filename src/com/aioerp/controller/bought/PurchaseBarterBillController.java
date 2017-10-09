package com.aioerp.controller.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AvgpriceConTroller;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.controller.reports.finance.arap.ArapRecordsController;
import com.aioerp.controller.stock.StockController;
import com.aioerp.controller.stock.StockRecordsController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.bought.PurchaseBarterBill;
import com.aioerp.model.bought.PurchaseBarterDetail;
import com.aioerp.model.bought.PurchaseBarterDraftBill;
import com.aioerp.model.bought.PurchaseBarterDraftDetail;
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
import java.io.UnsupportedEncodingException;
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

public class PurchaseBarterBillController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE12;
  
  public void index()
  {
    billCodeAuto(billTypeId);
    
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(loginConfigName(), billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    

    notEditStaff();
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws SQLException, UnsupportedEncodingException
  {
    String configName = loginConfigName();
    PurchaseBarterBill bill = (PurchaseBarterBill)getModel(PurchaseBarterBill.class);
    Integer unitId = getParaToInt("unit.id");
    if ((unitId == null) || (unitId.intValue() == 0))
    {
      setAttr("message", "请选择往来单位!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    Integer billId = bill.getInt("id");
    if (billId == null) {
      billId = Integer.valueOf(0);
    }
    boolean comfirm = getParaToBoolean("needComfirm", Boolean.valueOf(true)).booleanValue();
    boolean confirmAllow = getParaToBoolean("confirmAllow", Boolean.valueOf(true)).booleanValue();
    
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    Date recodeDate = bill.getTimestamp("recodeDate");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    boolean hasOther = PurchaseBarterBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(billTypeId));
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> outDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseBarterDetail.class, "purchaseBarterOutDetail");
    
    List<Model> inDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseBarterDetail.class, "purchaseBarterInDetail");
    
    List<HelpUtil> helpOutUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpOutUtil");
    
    List<HelpUtil> helpInUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpInUtil");
    

    Integer outStorageId = getParaToInt("outStorage.id", Integer.valueOf(0));
    Integer inStorageId = getParaToInt("inStorage.id", Integer.valueOf(0));
    if ((outDetailList.size() == 0) || (inDetailList.size() == 0))
    {
      setAttr("message", "无换入或换出商品不能保存，请将单据补充完整在重试!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    BigDecimal allowMoneys = PurchaseBarterBill.getAllowBarterMoney(configName, getParaToInt("unit.id").intValue(), getPara("purchaseBarterBill.recodeDate"), "in");
    BigDecimal outMoney = bill.getBigDecimal("outMoney");
    if ((confirmAllow) && (BigDecimalUtils.compare(allowMoneys, outMoney) < 0))
    {
      Map<String, Object> map = new HashMap();
      map.put("statusCode", AioConstants.HTTP_RETURN200);
      map.put("dialog", Boolean.valueOf(true));
      map.put("url", getAttr("base") + "/bought/barter/confirmAllow");
      map.put("dialogTitle", "提示");
      map.put("dialogId", "confirmAllow");
      map.put("width", "300");
      map.put("height", "150");
      
      renderJson(map);
      return;
    }
    Boolean isNegativeStock = SysConfig.getConfig(configName, 1);
    
    List<ResultHelp> rList = new ArrayList();
    for (int i = 0; i < outDetailList.size(); i++)
    {
      Model detail = (Model)outDetailList.get(i);
      BigDecimal amount = detail.getBigDecimal("amount");
      if ((detail.getInt("storageId") == null) || (detail.getInt("storageId").intValue() == 0)) {
        detail.set("storageId", outStorageId);
      }
      Integer detailSgeId = detail.getInt("storageId");
      Integer productId = detail.getInt("productId");
      

      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpOutUitlList.get(i)).getTrIndex(), 1);
      if (detailSgeId.intValue() < 1)
      {
        setAttr("message", "第" + trIndex + "行仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) != 1)
      {
        setAttr("message", "第" + trIndex + "行数量录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (productId.intValue() != 0)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        
        Integer selectUnitId = detail.getInt("selectUnitId");
        
        String selectUnit = product.getStr("calculateUnit1");
        if (2 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit2");
        } else if (3 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit3");
        }
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        

        Integer costArith = product.getInt("costArith");
        BigDecimal sckAmount;
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
          sckAmount = Stock.dao.getStockAmount(configName, productId.intValue(), detailSgeId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        } else {
          sckAmount = Stock.dao.getStockAmount(configName, productId, detailSgeId);
        }
        BigDecimal avgPrice = StockController.subAvgPrice(configName, detail, costArith, baseAmount, detail.getBigDecimal("costPrice"), detailSgeId.intValue(), productId.intValue());
        if ((comfirm) && (isNegativeStock.booleanValue()) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) && (BigDecimalUtils.compare(sckAmount, baseAmount) < 0))
        {
          if (SysConfig.getConfig(configName, 15).booleanValue())
          {
            Storage storage = (Storage)Storage.dao.findById(configName, detailSgeId);
            ResultHelp help = new ResultHelp(Integer.valueOf(trIndex), productId, product.getStr("fullName"), 
              product.getStr("code"), sckAmount, BigDecimalUtils.sub(sckAmount, baseAmount), detailSgeId, 
              storage.getStr("fullName"), storage.getStr("code"), avgPrice, selectUnit);
            rList.add(help);
          }
        }
        else if (((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && (BigDecimalUtils.compare(sckAmount, baseAmount) < 0)) || ((!isNegativeStock.booleanValue()) && (BigDecimalUtils.compare(sckAmount, baseAmount) < 0)))
        {
          setAttr("message", "错误类型：商品库存数量不够<br/> 商品编号：" + product.getStr("code") + "<br/> 商品全名：" + product.getStr("fullName"));
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
      map.put("url", getAttr("base") + "/bought/barter/negativeOption");
      map.put("dialogTitle", "负库存提示");
      map.put("dialogId", "jhhhd_nagetive_price_info");
      map.put("width", "600");
      map.put("height", "350");
      setSessionAttr("jhhhdResultList", rList);
      renderJson(map);
      return;
    }
    for (int i = 0; i < inDetailList.size(); i++)
    {
      Model detail = (Model)inDetailList.get(i);
      BigDecimal amount = detail.getBigDecimal("amount");
      if ((detail.getInt("storageId") == null) || (detail.getInt("storageId").intValue() == 0)) {
        detail.set("storageId", inStorageId);
      }
      Integer detailSgeId = detail.getInt("storageId");
      Integer productId = detail.getInt("productId");
      
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpInUitlList.get(i)).getTrIndex(), 1);
      if (detailSgeId.intValue() < 1)
      {
        setAttr("message", "第" + trIndex + "行仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) != 1)
      {
        setAttr("message", "第" + trIndex + "行数量录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    BigDecimal privilegeMoney = bill.getBigDecimal("privilegeMoney");
    BigDecimal payMoney = bill.getBigDecimal("payMoney");
    Map<String, Object> map = verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_ADD, unitId, AioConstants.WAY_PAY, payMoney, privilegeMoney);
    if (map != null)
    {
      renderJson(map);
      return;
    }
    Integer draftId = Integer.valueOf(0);
    
    bill.set("unitId", unitId);
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("userId", Integer.valueOf(loginUserId()));
    bill.set("outStorageId", outStorageId);
    bill.set("inStorageId", inStorageId);
    

    String normalCode = getPara("billCode");
    if (!normalCode.equals(bill.getStr("code"))) {
      bill.set("codeIncrease", Integer.valueOf(-1));
    }
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (billId.intValue() == 0)
    {
      bill.save(configName);
    }
    else
    {
      bill.remove("id");
      if (bill.getInt("codeIncrease").intValue() != -1)
      {
        String codeCreateRule = AioerpSys.dao.getValue1(configName, "codeCreateRule");
        if ("2".equals(codeCreateRule))
        {
          int codeIncrease = bill.getInt("codeIncrease").intValue();
          bill.set("codeIncrease", Integer.valueOf(codeIncrease + 1));
        }
      }
      bill.save(configName);
      
      draftId = getParaToInt("draftId");
      BusinessDraft.dao.deleteById(configName, draftId);
      
      PurchaseBarterDraftBill.dao.deleteById(configName, billId);
      PurchaseBarterDraftDetail.dao.delDeail(configName, billId);
      
      StockDraftRecords.delRecords(configName, billTypeId, billId.intValue());
      
      PayDraft.dao.delete(configName, billId, Integer.valueOf(billTypeId));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    

    BigDecimal gapMoney = BigDecimal.ZERO;
    BigDecimal outMoneyTot = BigDecimal.ZERO;
    BigDecimal inMoneyTot = BigDecimal.ZERO;
    

    List<Record> proAccountListOut = new ArrayList();
    BigDecimal outtaxes = BigDecimal.ZERO;
    for (int i = 0; i < outDetailList.size(); i++)
    {
      Record proAccount = new Record();
      Model outDetail = (Model)outDetailList.get(i);
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
          outDetail.set("giftAmount", amount);
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
          setAttr("message", "错误类型：非移动加权商品库存数量不够<br/> 商品编号：" + product.getStr("code") + "<br/> 商品全名：" + product.getStr("fullName"));
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
        BigDecimal costPrice = outDetail.getBigDecimal("costPrice");
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE1)
        {
          Map<String, String> costPriceMap = ComFunController.outProductGetCostPrice(configName, detailStorageId.intValue(), product, costPrice, selectUnitId.intValue());
          costPrice = new BigDecimal((String)costPriceMap.get("costPrice"));
          outDetail.set("costPrice", costPrice);
        }
        proAccount.set("productId", productId);
        proAccount.set("discountMoney", outDetail.get("discountMoney"));
        proAccount.set("costMoney", BigDecimalUtils.mul(costPrice, baseAmount));
        proAccountListOut.add(proAccount);
        
        BigDecimal costMoney = BigDecimalUtils.mul(baseAmount, costPrice);
        


        StockController.stockChange(configName, "out", detailStorageId.intValue(), productId.intValue(), costArith.intValue(), baseAmount, costPrice, outDetail.getStr("batch"), outDetail.getDate("produceDate"), outDetail.getDate("produceEndDate"));
        
        StockRecordsController.addRecords(configName, billTypeId, "out", bill, outDetail, costPrice, baseAmount, time, bill.getTimestamp("recodeDate"), outDetail.getInt("storageId"));
        
        AvgpriceConTroller.addAvgprice(configName, "out", detailStorageId, productId, baseAmount, costMoney);
        
        outDetail.set("type", Integer.valueOf(2));
        if (outDetail.get("id") != null) {
          outDetail.remove("id");
        }
        outDetail.save(configName);
        

        outtaxes = BigDecimalUtils.add(outtaxes, outDetail.getBigDecimal("taxes"));
        
        outMoneyTot = BigDecimalUtils.add(outMoneyTot, outDetail.getBigDecimal("taxMoney"));
      }
    }
    BigDecimal intaxes = BigDecimal.ZERO;
    for (int i = 0; i < inDetailList.size(); i++)
    {
      Model inDetail = (Model)inDetailList.get(i);
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
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        AvgpriceConTroller.addAvgprice(configName, "in", detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, basePrice));
        
        StockController.stockChange(configName, "in", detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, basePrice, inDetail.getStr("batch"), inDetail.getDate("produceDate"), inDetail.getDate("produceEndDate"));
        
        StockRecordsController.addRecords(configName, billTypeId, "in", bill, inDetail, basePrice, baseAmount, time, bill.getTimestamp("recodeDate"), inDetail.getInt("storageId"));
        
        inDetail.set("costPrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          inDetail.set("giftAmount", baseAmount);
        }
        inDetail.set("baseAmount", baseAmount);
        inDetail.set("basePrice", basePrice);
        inDetail.set("type", Integer.valueOf(1));
        if (inDetail.get("id") != null) {
          inDetail.remove("id");
        }
        inDetail.save(configName);
        

        intaxes = BigDecimalUtils.add(intaxes, inDetail.getBigDecimal("taxes"));
        
        inMoneyTot = BigDecimalUtils.add(inMoneyTot, inDetail.getBigDecimal("taxMoney"));
      }
    }
    gapMoney = BigDecimalUtils.sub(inMoneyTot, outMoneyTot);
    bill.set("inMoney", inMoneyTot);
    bill.set("outMoney", outMoneyTot);
    bill.set("gapMoney", gapMoney);
    bill.update(configName);
    


    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("gapMoney"));
    


    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
    


    BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("payMoney"));
    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, bill.getBigDecimal("payMoney"));
    


    Map<String, Object> record = new HashMap();
    record.put("gapMoney", bill.getBigDecimal("gapMoney"));
    record.put("proAccountListIn", inDetailList);
    record.put("proAccountListOut", proAccountListOut);
    record.put("payMoney", bill.getBigDecimal("payMoney"));
    record.put("taxes", BigDecimalUtils.sub(outtaxes, intaxes));
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), bill.getBigDecimal("privilege"), payTypeIds, payTypeMoneys, record);
    

    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId.intValue() > 0)
    {
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
      setDraftStrs();
    }
    else
    {
      setAttr("navTabId", "purchaseBarterView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    printBeforSave1();
    
    renderJson();
  }
  
  public void negativeOption()
  {
    List<ResultHelp> list = (List)getSessionAttr("jhhhdResultList");
    setAttr("rList", list);
    render("negativeOption.html");
  }
  
  public void setStockPrice()
  {
    List<ResultHelp> list = (List)getSessionAttr("jhhhdResultList");
    List<ResultHelp> recordList = new ArrayList();
    for (ResultHelp resultHelp : list)
    {
      BigDecimal avgPrice = resultHelp.getAvgPrice();
      if ((avgPrice == null) || (BigDecimalUtils.compare(avgPrice, BigDecimal.ZERO) == 0)) {
        recordList.add(resultHelp);
      }
    }
    getSession().removeAttribute("jhhhdResultList");
    setAttr("recordList", recordList);
    render("stockCostPriceOption.html");
  }
  
  public void confirmAllow()
  {
    render("confirmAllow.html");
  }
  
  public void look()
  {
    Integer id = getParaToInt(0, Integer.valueOf(0));
    
    boolean isRCW = false;
    String configName = loginConfigName();
    PurchaseBarterBill bill = (PurchaseBarterBill)PurchaseBarterBill.dao.findById(configName, id);
    Integer orderIsRedRow = bill.getInt("isRCW");
    if (orderIsRedRow == null) {
      orderIsRedRow = Integer.valueOf(0);
    }
    if (orderIsRedRow.intValue() == AioConstants.RCW_VS) {
      isRCW = true;
    } else {
      isRCW = false;
    }
    List<Model> outDetailList = PurchaseBarterDetail.dao.getListByBillId(configName, id, Integer.valueOf(2));
    addTrSize(outDetailList, 5);
    List<Model> inDetailList = PurchaseBarterDetail.dao.getListByBillId(configName, id, Integer.valueOf(1));
    addTrSize(inDetailList, 5);
    

    setPayTypeAttr(configName, "saveBill", billTypeId, bill.getInt("id").intValue());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("outDetailList", outDetailList);
    setAttr("inDetailList", inDetailList);
    setAttr("isRCW", Boolean.valueOf(isRCW));
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    render("look.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> map = new HashMap();
    try
    {
      PurchaseBarterBill bill = (PurchaseBarterBill)PurchaseBarterBill.dao.findById(configName, billId);
      if (bill == null)
      {
        map.put("status", Integer.valueOf(AioConstants.RCW_NO));
        map.put("message", "进货换货单已经不存在！");
        return map;
      }
      List<Model> outDetails = PurchaseBarterDetail.dao.getDetailByBillId(configName, billId, Integer.valueOf(2));
      List<Model> inDetails = PurchaseBarterDetail.dao.getDetailByBillId(configName, billId, Integer.valueOf(1));
      if ((outDetails == null) || (outDetails.size() == 0) || (inDetails == null) || (inDetails.size() == 0))
      {
        map.put("status", Integer.valueOf(AioConstants.RCW_NO));
        map.put("message", "进货换货商品信息已经不存在！");
        return map;
      }
      BigDecimal privilegeMoney = bill.getBigDecimal("privilegeMoney");
      BigDecimal payMoney = bill.getBigDecimal("payMoney");
      Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unitId"), AioConstants.WAY_PAY, payMoney, privilegeMoney);
      if (vmap != null)
      {
        map = vmap;
        map.put("status", Integer.valueOf(AioConstants.RCW_NO));
        return map;
      }
      String fullName;
      for (int i = 0; i < inDetails.size(); i++)
      {
        Model detail = (Model)inDetails.get(i);
        Model product = Product.dao.findById(configName, detail.getInt("productId"));
        fullName = product.getStr("fullName");
        int costArith = product.getInt("costArith").intValue();
        int productId = product.getInt("id").intValue();
        int detailStorageId = detail.getInt("storageId").intValue();
        BigDecimal amount = detail.getBigDecimal("baseAmount");
        BigDecimal sAmount = BigDecimal.ZERO;
        if (costArith == AioConstants.PRD_COST_PRICE4) {
          sAmount = Stock.dao.getStockAmount(configName, productId, 
            detailStorageId, detail.getBigDecimal("basePrice"), 
            detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        } else {
          sAmount = Stock.dao.getStockAmount(configName, Integer.valueOf(productId), 
            Integer.valueOf(detailStorageId));
        }
        if (BigDecimalUtils.compare(sAmount, amount) == -1)
        {
          if (!SysConfig.getConfig(configName, 1).booleanValue())
          {
            map.put("status", Integer.valueOf(AioConstants.RCW_NO));
            map.put("message", fullName + "是" + 
              ComFunController.getCostArithName(costArith) + 
              "商品,不允许负库存！");
            return map;
          }
          if (costArith != AioConstants.PRD_COST_PRICE1)
          {
            map.put("status", Integer.valueOf(AioConstants.RCW_NO));
            map.put("message", fullName + 
              "是" + 
              ComFunController.getCostArithName(costArith) + 
              "商品,不允许负库存！");
            return map;
          }
        }
      }
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
      bill.update(configName);
      
      Date date = new Date();
      Timestamp time = new Timestamp(date.getTime());
      bill.remove("id");
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
      bill.set("updateTime", time);
      
      billRcwAddRemark(bill, null);
      bill.save(configName);
      

      BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("gapMoney"));
      for (Model model : outDetails)
      {
        PurchaseBarterDetail detail = (PurchaseBarterDetail)model;
        int storageId = detail.getInt("storageId").intValue();
        int productId = detail.getInt("productId").intValue();
        
        Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
        int costArith = product.getInt("costArith").intValue();
        
        BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
        BigDecimal costPrice = detail.getBigDecimal("costPrice");
        BigDecimal money = BigDecimalUtils.mul(baseAmount, costPrice);
        String batch = detail.getStr("batch");
        Date produceDate = detail.getDate("produceDate");
        Date produceEndDate = detail.getDate("produceEndDate");
        
        detail.set("rcwId", detail.getInt("id"));
        detail.remove("id");
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", new Date());
        detail.save(configName);
        

        StockRecordsController.addRecords(configName, billTypeId, "in", bill, detail, costPrice, baseAmount, new Date(), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
        

        AvgpriceConTroller.addAvgprice(configName, "in", Integer.valueOf(storageId), Integer.valueOf(productId), baseAmount, money);
        


        StockController.stockChange(configName, "in", storageId, productId, costArith, baseAmount, costPrice, batch, produceDate, produceEndDate);
      }
      for (Model model : inDetails)
      {
        PurchaseBarterDetail detail = (PurchaseBarterDetail)model;
        int storageId = detail.getInt("storageId").intValue();
        int productId = detail.getInt("productId").intValue();
        
        Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
        int costArith = product.getInt("costArith").intValue();
        BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
        BigDecimal basePrice = detail.getBigDecimal("basePrice");
        BigDecimal money = BigDecimalUtils.mul(baseAmount, basePrice);
        String batch = detail.getStr("batch");
        Date produceDate = detail.getDate("produceDate");
        Date produceEndDate = detail.getDate("produceEndDate");
        

        detail.set("rcwId", detail.getInt("id"));
        detail.remove("id");
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", new Date());
        detail.save(configName);
        

        StockRecordsController.addRecords(configName, billTypeId, "out", bill, detail, basePrice, baseAmount, new Date(), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
        

        AvgpriceConTroller.addAvgprice(configName, "out", Integer.valueOf(storageId), Integer.valueOf(productId), baseAmount, money);
        


        StockController.stockChange(configName, "out", storageId, productId, costArith, baseAmount, basePrice, batch, produceDate, produceEndDate);
      }
      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
      

      ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), billId, null, null, null, null, null, null, null);
      

      PayTypeController.rcwAccountsRecoder(configName, billTypeId, billId, bill.getInt("id"));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "系统异常，请联系管理员！");
      return map;
    }
    map.put("status", Integer.valueOf(AioConstants.RCW_BY));
    return map;
  }
  
  @Before({Tx.class})
  public void updateDraft()
  {
    String configName = loginConfigName();
    Integer draftId = getParaToInt("draftId");
    

    boolean falg = editDraftVerify(draftId).booleanValue();
    if (falg) {
      return;
    }
    PurchaseBarterDraftBill bill = (PurchaseBarterDraftBill)getModel(PurchaseBarterDraftBill.class, "purchaseBarterBill");
    Integer billId = bill.getInt("id");
    if (billId == null) {
      billId = Integer.valueOf(0);
    }
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    Date recodeDate = bill.getTimestamp("recodeDate");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> outDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseBarterDraftDetail.class, "purchaseBarterOutDetail");
    
    List<Model> inDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseBarterDraftDetail.class, "purchaseBarterInDetail");
    
    Integer outStorageId = getParaToInt("outStorage.id", Integer.valueOf(0));
    Integer inStorageId = getParaToInt("inStorage.id", Integer.valueOf(0));
    if ((outDetailList.size() == 0) || (inDetailList.size() == 0))
    {
      setAttr("message", "无换入或换出商品不能保存，请将单据补充完整在重试!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    for (int i = 0; i < outDetailList.size(); i++)
    {
      Model detail = (Model)outDetailList.get(i);
      if ((detail.getInt("storageId") == null) || (detail.getInt("storageId").intValue() == 0)) {
        detail.set("storageId", outStorageId);
      }
    }
    for (int i = 0; i < inDetailList.size(); i++)
    {
      Model detail = (Model)inDetailList.get(i);
      if ((detail.getInt("storageId") == null) || (detail.getInt("storageId").intValue() == 0)) {
        detail.set("storageId", inStorageId);
      }
    }
    bill.set("unitId", getParaToInt("unit.id"));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    
    bill.set("outStorageId", outStorageId);
    bill.set("inStorageId", inStorageId);
    

    String normalCode = getPara("billCode");
    if (!normalCode.equals(bill.getStr("code"))) {
      bill.set("codeIncrease", Integer.valueOf(-1));
    }
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    
    BigDecimal billMoney = bill.getBigDecimal("gapMoney");
    if (draftId != null)
    {
      bill.update(configName);
      
      BusinessDraftController.updateBillDraft(configName, draftId.intValue(), bill, billTypeId, billMoney);
      
      StockDraftRecords.delRecords(configName, billTypeId, billId.intValue());
      
      PayDraft.dao.delete(configName, billId, Integer.valueOf(billTypeId));
    }
    else
    {
      if (bill.getInt("codeIncrease").intValue() != -1)
      {
        String codeCreateRule = AioerpSys.dao.getValue1(configName, "codeCreateRule");
        if ("2".equals(codeCreateRule))
        {
          int codeIncrease = bill.getInt("codeIncrease").intValue();
          bill.set("codeIncrease", Integer.valueOf(codeIncrease - 1));
        }
      }
      bill.save(configName);
      BusinessDraftController.saveBillDraft(configName, billTypeId, bill, billMoney);
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    List<Integer> oldDetailIds = PurchaseBarterDraftDetail.dao.getListByDetailId(configName, billId);
    
    List<Integer> newDetaiIds = new ArrayList();
    for (int i = 0; i < outDetailList.size(); i++)
    {
      Model outDetail = (Model)outDetailList.get(i);
      Integer outDetailId = outDetail.getInt("id");
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
      BigDecimal amount = outDetail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        outDetail.set("billId", bill.getInt("id"));
        outDetail.set("updateTime", time);
        
        Integer selectUnitId = outDetail.getInt("selectUnitId");
        Integer detailStorageId = outDetail.getInt("storageId");
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, product, Integer.valueOf(1));
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, product, Integer.valueOf(1));
        
        outDetail.set("baseAmount", baseAmount);
        outDetail.set("basePrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          outDetail.set("giftAmount", amount);
        }
        Integer costArith = product.getInt("costArith");
        
        BigDecimal costPrice = outDetail.getBigDecimal("costPrice");
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE1)
        {
          Map<String, String> costPriceMap = ComFunController.outProductGetCostPrice(configName, detailStorageId.intValue(), product, costPrice, selectUnitId.intValue());
          costPrice = new BigDecimal((String)costPriceMap.get("costPrice"));
          outDetail.set("costPrice", costPrice);
        }
        outDetail.set("type", Integer.valueOf(2));
        if (outDetailId == null)
        {
          outDetail.save(configName);
        }
        else
        {
          outDetail.update(configName);
          newDetaiIds.add(outDetailId);
        }
        StockDraftRecords.addRecords(configName, billTypeId, false, bill, outDetail, costPrice, baseAmount, time, bill.getTimestamp("recodeDate"), outDetail.getInt("storageId"));
      }
    }
    Model inDetail;
    for (int i = 0; i < inDetailList.size(); i++)
    {
      inDetail = (Model)inDetailList.get(i);
      Integer inDetailId = inDetail.getInt("id");
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
        BigDecimal price = inDetail.getBigDecimal("price");
        if ((inDetail.getBigDecimal("discountPrice") != null) && (BigDecimalUtils.compare(inDetail.getBigDecimal("discountPrice"), BigDecimal.ZERO) != 0)) {
          price = inDetail.getBigDecimal("discountPrice");
        }
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        inDetail.set("costPrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          inDetail.set("giftAmount", baseAmount);
        }
        inDetail.set("baseAmount", baseAmount);
        inDetail.set("basePrice", basePrice);
        inDetail.set("type", Integer.valueOf(1));
        if (inDetailId == null)
        {
          inDetail.save(configName);
        }
        else
        {
          inDetail.update(configName);
          newDetaiIds.add(inDetailId);
        }
        StockDraftRecords.addRecords(configName, billTypeId, true, bill, inDetail, basePrice, baseAmount, time, bill.getTimestamp("recodeDate"), inDetail.getInt("storageId"));
      }
    }
    for (Integer id : oldDetailIds) {
      if (!newDetaiIds.contains(id)) {
        PurchaseBarterDraftDetail.dao.deleteById(configName, id);
      }
    }
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    if (draftId != null) {
      PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(billTypeId));
    }
    addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), billTypeId, payTypeIds, payTypeMoneys, bill.getInt("id").intValue(), bill.getInt("unitId"), time);
    
    PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), billTypeId, billId.intValue(), bill.getInt("unitId"), Integer.valueOf(42), bill.getBigDecimal("privilege"), time);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != null)
    {
      setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "purchaseBarterView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    renderJson();
  }
  
  public void toEditDraft()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int sourceId = getParaToInt(1, Integer.valueOf(0)).intValue();
    Model bill = PurchaseBarterDraftBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    Integer unitId = bill.getInt("unitId");
    
    BigDecimal allowMoney = PurchaseBarterBill.getAllowBarterMoney(configName, unitId.intValue(), bill.getTimestamp("recodeDate").toString(), "in");
    
    List<Model> outDetailList = PurchaseBarterDraftDetail.dao.getListByBillId(configName, Integer.valueOf(billId), Integer.valueOf(2));
    addTrSize(outDetailList, 5);
    List<Model> inDetailList = PurchaseBarterDraftDetail.dao.getListByBillId(configName, Integer.valueOf(billId), Integer.valueOf(1));
    addTrSize(inDetailList, 5);
    

    setPayTypeAttr(configName, "draftBill", billTypeId, bill.getInt("id").intValue());
    
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("allowMoney", allowMoney);
    setAttr("outDetailList", outDetailList);
    setAttr("inDetailList", inDetailList);
    setAttr("draftId", Integer.valueOf(sourceId));
    setAttr("tableId", Integer.valueOf(billTypeId));
    
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    
    notEditStaff();
    setDraftAutoPost();
    render("editDraft.html");
  }
  
  public void print()
    throws ParseException
  {
    Integer billId = getParaToInt("billId");
    Integer editId = getParaToInt("purchaseBarterBill.id");
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "进货换货单");
    List<String> headData = new ArrayList();
    headData.add("往来单位 :" + getPara("unit.fullName", ""));
    headData.add("换出仓库 :" + getPara("outStorage.fullName", ""));
    String recodeDate = DateUtils.format(getParaToDate("purchaseBarterBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    headData.add("单据编号:" + getPara("purchaseBarterBill.code", ""));
    headData.add("换入仓库:" + getPara("inStorage.fullName", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门 :" + getPara("department.fullName", ""));
    
    Model bill = null;
    if ((billId != null) && (billId.intValue() != 0)) {
      bill = PurchaseBarterBill.dao.findById(configName, billId);
    } else if ((editId != null) && (editId.intValue() != 0)) {
      bill = PurchaseBarterDraftBill.dao.findById(configName, editId);
    }
    if (bill != null) {
      setBillUser(headData, bill.getInt("userId"));
    } else {
      setBillUser(headData, null);
    }
    headData.add("摘要:" + getPara("purchaseBarterBill.remark", ""));
    headData.add("附加说明 :" + getPara("purchaseBarterBill.memo", ""));
    
    headData.add("收款账户:" + getPara("payTypeAccounts", ""));
    headData.add("收款金额:" + getPara("purchaseBarterBill.payMoney", ""));
    headData.add("优惠金额:" + getPara("purchaseBarterBill.privilege", ""));
    headData.add("优惠后金额:" + getPara("purchaseBarterBill.privilegeMoney", ""));
    headData.add("换货差额:" + getPara("purchaseBarterBill.gapMoney", ""));
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    colTitle.add("类型");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("单位");
    colTitle.add("生产日期");
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
    colTitle.add("到期日期");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    
    List<String> rowData = new ArrayList();
    

    List<Model> detailOutList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseBarterDetail.class, "purchaseBarterOutDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailOutList = PurchaseBarterDetail.dao.getListByBillId(configName, billId, Integer.valueOf(2));
    }
    if ((detailOutList == null) || (detailOutList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailOutList.size(); i++)
    {
      Model detail = (Model)detailOutList.get(i);
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      Integer storageId = detail.getInt("storageId");
      Storage storage = (Storage)Storage.dao.findById(configName, storageId);
      rowData.add(trimNull(i + 1));
      rowData.add("换出");
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
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
      rowData.add(trimNull(detail.getBigDecimal("retailPrice")));
      rowData.add(trimNull(detail.getBigDecimal("retailMoney")));
      rowData.add(trimNull(detail.get("memo")));
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      rowData.add(status);
      rowData.add(trimNull(product.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(trimNull(product.getStr("assistUnit")));
      rowData.add(trimNull(detail.getStr("message")));
      rowData.add(trimNull(detail.getDate("produceEndDate")));
    }
    List<Model> detailInList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseBarterDetail.class, "purchaseBarterInDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailInList = PurchaseBarterDetail.dao.getListByBillId(configName, billId, Integer.valueOf(1));
    }
    if ((detailInList == null) || (detailInList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailInList.size(); i++)
    {
      Model detail = (Model)detailInList.get(i);
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      Integer storageId = detail.getInt("storageId");
      Storage storage = (Storage)Storage.dao.findById(configName, storageId);
      rowData.add(trimNull(i + 1));
      rowData.add("换入");
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
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
      rowData.add(trimNull(detail.getBigDecimal("retailPrice")));
      rowData.add(trimNull(detail.getBigDecimal("retailMoney")));
      rowData.add(trimNull(detail.get("memo")));
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      rowData.add(status);
      rowData.add(trimNull(product.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(trimNull(product.getStr("assistUnit")));
      rowData.add(trimNull(detail.getStr("message")));
      rowData.add(trimNull(detail.getDate("produceEndDate")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
