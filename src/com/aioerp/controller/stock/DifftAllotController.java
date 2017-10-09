package com.aioerp.controller.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AvgpriceConTroller;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.bought.ResultHelp;
import com.aioerp.model.stock.DifftAllotBill;
import com.aioerp.model.stock.DifftAllotDetail;
import com.aioerp.model.stock.DifftAllotDraftBill;
import com.aioerp.model.stock.DifftAllotDraftDetail;
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
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class DifftAllotController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE15;
  
  public void index()
  {
    String configName = loginConfigName();
    
    billCodeAuto(billTypeId);
    
    setAttr("recodeTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    

    notEditStaff();
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
  {
    String configName = loginConfigName();
    DifftAllotBill bill = (DifftAllotBill)getModel(DifftAllotBill.class);
    
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
    boolean hasOther = DifftAllotBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(billTypeId));
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), DifftAllotDetail.class, "difftAllotDetail");
    
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    
    Integer outStorageId = getParaToInt("outStorage.id", Integer.valueOf(0));
    Integer inStorageId = getParaToInt("inStorage.id", Integer.valueOf(0));
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品！");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Boolean isNegativeStock = SysConfig.getConfig(configName, 1);
    
    List<ResultHelp> rList = new ArrayList();
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      BigDecimal amount = detail.getBigDecimal("amount");
      if ((detail.getInt("outStorageId") == null) || (detail.getInt("outStorageId").intValue() == 0)) {
        detail.set("outStorageId", outStorageId);
      }
      if ((detail.getInt("inStorageId") == null) || (detail.getInt("inStorageId").intValue() == 0)) {
        detail.set("inStorageId", inStorageId);
      }
      Integer dOutStorageId = detail.getInt("outStorageId");
      Integer dInStorageId = detail.getInt("inStorageId");
      
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1);
      if (dOutStorageId.intValue() < 1)
      {
        setAttr("message", "第" + trIndex + "行发货仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (dInStorageId.intValue() < 1)
      {
        setAttr("message", "第" + trIndex + "行收货仓库录入错误!");
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
      Integer productId = detail.getInt("productId");
      if (productId.intValue() != 0)
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
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        Integer costArith = product.getInt("costArith");
        BigDecimal costPrice = ((HelpUtil)helpUitlList.get(i)).getCostPrice();
        detail.set("costPrice", costPrice);
        BigDecimal sckAmount;
       
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
          sckAmount = Stock.dao.getStockAmount(configName, productId.intValue(), dOutStorageId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        } else {
          sckAmount = Stock.dao.getStockAmount(configName, productId, dOutStorageId);
        }
        BigDecimal avgPrice = StockController.subAvgPrice(configName, detail, costArith, baseAmount, detail.getBigDecimal("costPrice"), dOutStorageId.intValue(), productId.intValue());
        if ((comfirm) && (isNegativeStock.booleanValue()) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) && (BigDecimalUtils.compare(sckAmount, baseAmount) < 0))
        {
          if (SysConfig.getConfig(configName, 15).booleanValue())
          {
            Storage storage = (Storage)Storage.dao.findById(configName, dOutStorageId);
            ResultHelp help = new ResultHelp(Integer.valueOf(trIndex), productId, product.getStr("fullName"), 
              product.getStr("code"), sckAmount, BigDecimalUtils.sub(sckAmount, baseAmount), dOutStorageId, 
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
      map.put("dialogId", "jhhhd_nagetive_info");
      map.put("width", "600");
      map.put("height", "350");
      setSessionAttr("jhhhdResultList", rList);
      renderJson(map);
      return;
    }
    Integer draftId = Integer.valueOf(0);
    
    bill.set("outStorageId", outStorageId);
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("inStorageId", inStorageId);
    

    String normalCode = getPara("billCode");
    if (!normalCode.equals(bill.getStr("code"))) {
      bill.set("codeIncrease", Integer.valueOf(-1));
    }
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    












    bill.set("userId", Integer.valueOf(loginUserId()));
    if (bill.get("id") == null)
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
      
      DifftAllotDraftBill.dao.deleteById(configName, billId);
      DifftAllotDraftDetail.dao.delDeail(configName, billId);
      
      StockDraftRecords.delRecords(configName, billTypeId, billId.intValue());
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    
    BigDecimal moneysTot = BigDecimal.ZERO;
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Integer detailInStorage = detail.getInt("inStorageId");
      Integer detailOutStorage = detail.getInt("outStorageId");
      Integer productId = detail.getInt("productId");
      BigDecimal amount = detail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        Integer selectUnitId = detail.getInt("selectUnitId");
        if ((detailInStorage == null) || (detailInStorage.intValue() == 0)) {
          detailInStorage = inStorageId;
        }
        if ((detailOutStorage == null) || (detailOutStorage.intValue() == 0)) {
          detailOutStorage = outStorageId;
        }
        detail.set("inStorageId", detailInStorage);
        detail.set("outStorageId", detailOutStorage);
        
        Integer costArith = product.getInt("costArith");
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        if (detail.get("discountPrice") == null) {
          detail.set("discountPrice", detail.get("price"));
        }
        if (detail.getBigDecimal("discount") == null) {
          detail.set("discount", BigDecimal.ONE);
        }
        if (detail.get("discountMoney") == null) {
          detail.set("discountMoney", BigDecimalUtils.mul(BigDecimalUtils.mul(detail.getBigDecimal("amount"), detail.getBigDecimal("price")), detail.getBigDecimal("discount")));
        }
        BigDecimal price = detail.getBigDecimal("price");
        if ((detail.getBigDecimal("discountPrice") != null) && (BigDecimalUtils.compare(detail.getBigDecimal("discountPrice"), BigDecimal.ZERO) != 0)) {
          price = detail.getBigDecimal("discountPrice");
        }
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        detail.set("baseAmount", baseAmount);
        detail.set("basePrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          detail.set("giftAmount", baseAmount);
        }
        BigDecimal stockPrice = detail.getBigDecimal("costPrice");
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE1)
        {
          Map<String, String> costPriceMap = ComFunController.outProductGetCostPrice(configName, detailOutStorage.intValue(), product, stockPrice, selectUnitId.intValue());
          stockPrice = new BigDecimal((String)costPriceMap.get("costPrice"));
          detail.set("costPrice", stockPrice);
        }
        BigDecimal outCostMoney = BigDecimalUtils.mul(baseAmount, stockPrice);
        BigDecimal inCostMoney = BigDecimalUtils.mul(baseAmount, basePrice);
        
        StockController.stockSubChange(configName, costArith.intValue(), baseAmount, detailOutStorage.intValue(), productId.intValue(), stockPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"), null);
        
        StockController.stockChange(configName, "in", detailInStorage.intValue(), productId.intValue(), costArith.intValue(), baseAmount, basePrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        
        StockRecordsController.addRecords(configName, billTypeId, "out", bill, detail, stockPrice, baseAmount, time, bill.getTimestamp("recodeDate"), detailOutStorage);
        
        StockRecordsController.addRecords(configName, billTypeId, "in", bill, detail, basePrice, baseAmount, time, bill.getTimestamp("recodeDate"), detailInStorage);
        
        AvgpriceConTroller.addAvgprice(configName, "out", detailOutStorage, productId, baseAmount, outCostMoney);
        
        AvgpriceConTroller.addAvgprice(configName, "in", detailInStorage, productId, baseAmount, inCostMoney);
        if (detail.get("id") != null) {
          detail.remove("id");
        }
        detail.save(configName);
        

        moneysTot = BigDecimalUtils.add(moneysTot, detail.getBigDecimal("discountMoney"));
      }
    }
    bill.set("moneys", moneysTot);
    bill.update(configName);
    

    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
    

    Map<String, Object> record = new HashMap();
    record.put("proAccountList", detailList);
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), Integer.valueOf(0), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, record);
    

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
      setAttr("navTabId", "difftAllotView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    printBeforSave1();
    
    renderJson();
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    boolean isRCW = false;
    
    DifftAllotBill bill = (DifftAllotBill)DifftAllotBill.dao.findById(configName, Integer.valueOf(id));
    Integer orderIsRedRow = bill.getInt("isRCW");
    if (orderIsRedRow == null) {
      orderIsRedRow = Integer.valueOf(0);
    }
    List<Model> detailList = DifftAllotDetail.dao.getListByBillId(configName, Integer.valueOf(id));
    
    detailList = addTrSize(detailList, 15);
    if (orderIsRedRow.intValue() == AioConstants.RCW_VS) {
      isRCW = true;
    } else {
      isRCW = false;
    }
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("isRCW", Boolean.valueOf(isRCW));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    render("look.html");
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
    DifftAllotDraftBill bill = (DifftAllotDraftBill)getModel(DifftAllotDraftBill.class, "difftAllotBill");
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), DifftAllotDraftDetail.class, "difftAllotDetail");
    
    Integer outStorageId = getParaToInt("outStorage.id", Integer.valueOf(0));
    Integer inStorageId = getParaToInt("inStorage.id", Integer.valueOf(0));
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品！");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    bill.set("outStorageId", outStorageId);
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("inStorageId", inStorageId);
    

    String normalCode = getPara("billCode");
    if (!normalCode.equals(bill.getStr("code"))) {
      bill.set("codeIncrease", Integer.valueOf(-1));
    }
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (draftId != null)
    {
      bill.update(configName);
      
      BusinessDraftController.updateBillDraft(configName, draftId.intValue(), bill, billTypeId, bill.getBigDecimal("moneys"));
      
      StockDraftRecords.delRecords(configName, billTypeId, billId.intValue());
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
      BusinessDraftController.saveBillDraft(configName, billTypeId, bill, bill.getBigDecimal("moneys"));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    List<Integer> oldDetailIds = DifftAllotDraftDetail.dao.getListByDetailId(configName, billId);
    
    List<Integer> newDetaiIds = new ArrayList();
    
    BigDecimal moneysTot = BigDecimal.ZERO;
    DifftAllotDraftDetail detail;
    for (int i = 0; i < detailList.size(); i++)
    {
      detail = (DifftAllotDraftDetail)detailList.get(i);
      Integer detailId = detail.getInt("id");
      Integer detailInStorage = detail.getInt("inStorageId");
      Integer detailOutStorage = detail.getInt("outStorageId");
      Integer productId = detail.getInt("productId");
      BigDecimal amount = detail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        int costArith = product.getInt("costArith").intValue();
        Integer selectUnitId = detail.getInt("selectUnitId");
        if ((detailInStorage == null) || (detailInStorage.intValue() == 0)) {
          detailInStorage = inStorageId;
        }
        if ((detailOutStorage == null) || (detailOutStorage.intValue() == 0)) {
          detailOutStorage = outStorageId;
        }
        detail.set("inStorageId", detailInStorage);
        detail.set("outStorageId", detailOutStorage);
        if (detail.get("discountPrice") == null) {
          detail.set("discountPrice", detail.get("price"));
        }
        if (detail.getBigDecimal("discount") == null) {
          detail.set("discount", BigDecimal.ONE);
        }
        if (detail.get("discountMoney") == null) {
          detail.set("discountMoney", BigDecimalUtils.mul(BigDecimalUtils.mul(detail.getBigDecimal("amount"), detail.getBigDecimal("price")), detail.getBigDecimal("discount")));
        }
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, product, Integer.valueOf(1));
        BigDecimal price = detail.getBigDecimal("price");
        if ((detail.getBigDecimal("discountPrice") != null) && (BigDecimalUtils.compare(detail.getBigDecimal("discountPrice"), BigDecimal.ZERO) != 0)) {
          price = detail.getBigDecimal("discountPrice");
        }
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, product, Integer.valueOf(1));
        
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        detail.set("baseAmount", baseAmount);
        detail.set("basePrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          detail.set("giftAmount", baseAmount);
        }
        BigDecimal stockPrice = detail.getBigDecimal("costPrice");
        if (costArith == AioConstants.PRD_COST_PRICE1)
        {
          Map<String, String> costPriceMap = ComFunController.outProductGetCostPrice(configName, detailOutStorage.intValue(), product, stockPrice, selectUnitId.intValue());
          stockPrice = new BigDecimal((String)costPriceMap.get("costPrice"));
          detail.set("costPrice", stockPrice);
        }
        if (detailId == null)
        {
          detail.save(configName);
        }
        else
        {
          detail.update(configName);
          newDetaiIds.add(detailId);
        }
        StockDraftRecords.addRecords(configName, billTypeId, false, bill, detail, stockPrice, baseAmount, time, bill.getTimestamp("recodeDate"), detailOutStorage);
        
        StockDraftRecords.addRecords(configName, billTypeId, true, bill, detail, basePrice, baseAmount, time, bill.getTimestamp("recodeDate"), detailInStorage);
        


        moneysTot = BigDecimalUtils.add(moneysTot, detail.getBigDecimal("discountMoney"));
      }
    }
    bill.set("moneys", moneysTot);
    bill.update(configName);
    
    BusinessDraftController.updateBillDraft(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
    for (Integer id : oldDetailIds) {
      if (!newDetaiIds.contains(id)) {
        DifftAllotDraftDetail.dao.deleteById(configName, id);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != null)
    {
      setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "difftAllotView");
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
    Model bill = DifftAllotDraftBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = DifftAllotDraftDetail.dao.getListByBillId(configName, Integer.valueOf(billId));
    
    detailList = addTrSize(detailList, 15);
    
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("draftId", Integer.valueOf(sourceId));
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    
    notEditStaff();
    setDraftAutoPost();
    render("editDraft.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> result = new HashMap();
    DifftAllotBill bill = (DifftAllotBill)DifftAllotBill.dao.findById(configName, billId);
    if (bill == null)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", "该变价调拨单不存在");
      return result;
    }
    List<Model> detailList = DifftAllotDetail.dao.getListByBillId(configName, billId);
    boolean comfirm = SysConfig.getConfig(configName, 1).booleanValue();
    Integer productId;
    for (Model detail : detailList)
    {
      BigDecimal amount = detail.getBigDecimal("baseAmount");
      productId = detail.getInt("productId");
      Integer inStorageId = detail.getInt("inStorageId");
      
      Product product = (Product)detail.get("product");
      Integer costArith = product.getInt("costArith");
      
      BigDecimal sAmount = BigDecimal.ZERO;
      if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
        sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), inStorageId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
      } else {
        sAmount = Stock.dao.getStockAmount(configName, productId, inStorageId);
      }
      if ((!comfirm) && (BigDecimalUtils.compare(sAmount, amount) < 0))
      {
        result.put("status", Integer.valueOf(AioConstants.RCW_NO));
        result.put("message", "不允许商品出现负库存！");
        return result;
      }
      if ((AioConstants.PRD_COST_PRICE4 == costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0))
      {
        result.put("status", Integer.valueOf(AioConstants.RCW_NO));
        result.put("message", "该变价调拨单有非移动加权商品库存不足！");
        return result;
      }
    }
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
    bill.update(configName);
    DifftAllotBill newBill = bill;
    newBill.remove("id");
    newBill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
    Date time = new Date();
    newBill.set("updateTime", time);
    
    billRcwAddRemark(bill, null);
    newBill.save(configName);
    
    BillHistoryController.saveBillHistory(configName, newBill, billTypeId, bill.getBigDecimal("moneys"));
    for (Model odlDetail : detailList)
    {
      DifftAllotDetail newDetail = (DifftAllotDetail)odlDetail;
      
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.remove("id");
      newDetail.set("billId", newBill.getInt("id"));
      newDetail.set("updateTime", time);
      BigDecimal amount = odlDetail.getBigDecimal("baseAmount");
       productId = odlDetail.getInt("productId");
      Integer inStorageId = odlDetail.getInt("inStorageId");
      Integer outStorageId = odlDetail.getInt("outStorageId");
      Product product = (Product)odlDetail.get("product");
      Integer costArith = product.getInt("costArith");
      BigDecimal costPrice = newDetail.getBigDecimal("costPrice");
      BigDecimal basePrice = newDetail.getBigDecimal("basePrice");
      
      StockController.stockSubChange(configName, costArith.intValue(), amount, inStorageId.intValue(), productId.intValue(), basePrice, newDetail.getStr("batch"), newDetail.getDate("produceDate"), newDetail.getDate("produceEndDate"), null);
      
      StockController.stockChange(configName, "in", outStorageId.intValue(), productId.intValue(), costArith.intValue(), amount, costPrice, newDetail.getStr("batch"), newDetail.getDate("produceDate"), newDetail.getDate("produceEndDate"));
      
      Timestamp times = new Timestamp(time.getTime());
      
      StockRecordsController.addRecords(configName, billTypeId, "out", newBill, newDetail, basePrice, amount, time, times, inStorageId);
      
      StockRecordsController.addRecords(configName, billTypeId, "in", newBill, newDetail, costPrice, amount, time, times, outStorageId);
      
      AvgpriceConTroller.addAvgprice(configName, "out", inStorageId, productId, amount, BigDecimalUtils.mul(amount, basePrice));
      
      AvgpriceConTroller.addAvgprice(configName, "in", outStorageId, productId, amount, BigDecimalUtils.mul(amount, costPrice));
      newDetail.save(configName);
    }
    PayTypeController.rcwAccountsRecoder(configName, billTypeId, billId, newBill.getInt("id"));
    

    result.put("status", Integer.valueOf(AioConstants.RCW_BY));
    result.put("message", "红冲成功");
    return result;
  }
  
  public void print()
    throws ParseException
  {
    Integer billId = getParaToInt("billId");
    Integer editId = getParaToInt("difftAllotBill.id");
    String configName = loginConfigName();
    

    SysConfig.getConfig(configName, 12).booleanValue();
    
    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "变价调拨单");
    List<String> headData = new ArrayList();
    
    String recodeDate = DateUtils.format(getParaToDate("difftAllotBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    headData.add("单据编号:" + getPara("difftAllotBill.code", ""));
    headData.add("发货仓库:" + getPara("outStorage.fullName", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门:" + getPara("department.fullName", ""));
    headData.add("收货仓库:" + getPara("inStorage.fullName", ""));
    headData.add("摘要:" + getPara("difftAllotBill.remark", ""));
    headData.add("附加说明:" + getPara("difftAllotBill.memo", ""));
    headData.add("整单折扣:" + getPara("difftAllotBill.discounts", ""));
    Model bill = null;
    if ((billId != null) && (billId.intValue() != 0)) {
      bill = DifftAllotBill.dao.findById(configName, billId);
    } else if ((editId != null) && (editId.intValue() != 0)) {
      bill = DifftAllotDraftBill.dao.findById(configName, editId);
    }
    if (bill != null) {
      setBillUser(headData, bill.getInt("userId"));
    } else {
      setBillUser(headData, null);
    }
    List<String> colWidth = new ArrayList();
    List<String> colTitle = new ArrayList();
    
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
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("发货仓库编号");
    colTitle.add("发货仓库全名");
    colTitle.add("收货仓库编号");
    colTitle.add("收货仓库全名");
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
    colTitle.add("零售价");
    colTitle.add("零售金额");
    colTitle.add("单据备注");
    colTitle.add("状态");
    colTitle.add("条码");
    colTitle.add("辅助单位");
    colTitle.add("附加信息");
    colTitle.add("到期日期");
    
    List<String> rowData = new ArrayList();
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), DifftAllotDetail.class, "difftAllotDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailList = DifftAllotDetail.dao.getListByBillId(configName, billId);
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      Integer outStorageId = detail.getInt("outStorageId");
      Storage outStorage = (Storage)Storage.dao.findById(configName, outStorageId);
      Integer inStorageId = detail.getInt("inStorageId");
      Storage inStorage = (Storage)Storage.dao.findById(configName, inStorageId);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
      if (outStorage != null)
      {
        rowData.add(trimNull(outStorage.getStr("code")));
        rowData.add(trimNull(outStorage.getStr("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      if (inStorage != null)
      {
        rowData.add(trimNull(inStorage.getStr("code")));
        rowData.add(trimNull(inStorage.getStr("fullName")));
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
      rowData.add(trimNull(detail.getBigDecimal("retailPrice")));
      rowData.add(trimNull(detail.getBigDecimal("retailMoney")));
      rowData.add(trimNull(detail.get("memo")));
      
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      rowData.add(status);
      
      String barCode = "";
      if (selectUnitId.intValue() == 1) {
        barCode = trimNull(product.getStr("barCode1"));
      }
      if (selectUnitId.intValue() == 2) {
        barCode = trimNull(product.getStr("barCode2"));
      }
      if (selectUnitId.intValue() == 3) {
        barCode = trimNull(product.getStr("barCode3"));
      }
      rowData.add(barCode);
      
      rowData.add(trimNull(product.getStr("assistUnit")));
      rowData.add(trimNull(detail.getStr("message")));
      rowData.add(trimNull(detail.getDate("produceEndDate")));
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
}
