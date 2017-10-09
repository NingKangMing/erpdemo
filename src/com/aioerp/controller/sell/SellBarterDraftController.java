package com.aioerp.controller.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.bought.PurchaseBarterBill;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.sell.barter.SellBarterDraftBill;
import com.aioerp.model.sell.barter.SellBarterDraftDetail;
import com.aioerp.model.stock.StockDraftRecords;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class SellBarterDraftController
  extends BaseController
{
  @Before({Tx.class})
  public void add()
    throws SQLException
  {
    String configName = loginConfigName();
    SellBarterDraftBill bill = (SellBarterDraftBill)getModel(SellBarterDraftBill.class, "sellBarterBill");
    
    boolean hasOther = SellBarterDraftBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("message", "编号已经存在!");
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
    List<Model> inDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellBarterDraftDetail.class, "sellBarterInDetail");
    List<HelpUtil> helpInList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpInUtil");
    
    List<Model> outDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellBarterDraftDetail.class, "sellBarterOutDetail");
    List<HelpUtil> helpOutList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpOutUtil");
    if ((inDetailList.size() == 0) || (outDetailList.size() == 0))
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
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
    }
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
    }
    Date time = new Date();
    bill.set("updateTime", time);
    
    billCodeIncrease(bill, "draftAdd");
    bill.save(configName);
    

    BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE13, bill, bill.getBigDecimal("gapMoney"));
    for (int i = 0; i < inDetailList.size(); i++)
    {
      Model inDetail = (Model)inDetailList.get(i);
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
        if ((inDetail.getBigDecimal("taxPrice") != null) && (BigDecimalUtils.compare(inDetail.getBigDecimal("taxPrice"), BigDecimal.ZERO) != 0)) {
          price = inDetail.getBigDecimal("taxPrice");
        }
        if (detailStorageId == null) {
          detailStorageId = inStorageId;
        }
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          inDetail.set("giftAmount", baseAmount);
        }
        inDetail.set("baseAmount", baseAmount);
        inDetail.set("basePrice", basePrice);
        
        inDetail.save(configName);
        
        BigDecimal stockPrice = inDetail.getBigDecimal("costPrice");
        if (inDetail.getBigDecimal("costPrice") == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, detailStorageId.intValue(), product, inDetail.getBigDecimal("costPrice"), selectUnitId.intValue());
          stockPrice = new BigDecimal((String)map.get("costPrice"));
          
          inDetail.set("costPrice", stockPrice);
        }
        StockDraftRecords.addRecords(configName, AioConstants.BILL_ROW_TYPE13, true, bill, inDetail, stockPrice, baseAmount, time, bill.getTimestamp("recodeDate"), inDetail.getInt("storageId"));
      }
    }
    for (int i = 0; i < outDetailList.size(); i++)
    {
      Model outDetail = (Model)outDetailList.get(i);
      outDetail.set("type", Integer.valueOf(2));
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
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        outDetail.set("baseAmount", baseAmount);
        outDetail.set("basePrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          outDetail.set("giftAmount", baseAmount);
        }
        outDetail.save(configName);
        
        BigDecimal stockPrice = outDetail.getBigDecimal("costPrice");
        if (outDetail.getBigDecimal("costPrice") == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, outDetail.getInt("storageId").intValue(), product, outDetail.getBigDecimal("costPrice"), selectUnitId.intValue());
          stockPrice = new BigDecimal((String)map.get("costPrice"));
          
          outDetail.set("costPrice", stockPrice);
        }
        StockDraftRecords.addRecords(configName, AioConstants.BILL_ROW_TYPE13, false, bill, outDetail, stockPrice, baseAmount, time, bill.getTimestamp("recodeDate"), outDetail.getInt("storageId"));
      }
    }
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    if (StringUtils.isNotBlank(payTypeIds))
    {
      String[] accounts = payTypeIds.split(",");
      String[] payMoneys = payTypeMoneys.split(",");
      for (int i = 0; i < accounts.length; i++)
      {
        BigDecimal payMoneyi = new BigDecimal(payMoneys[i]);
        PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE1), AioConstants.BILL_ROW_TYPE13, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(accounts[i]), payMoneyi, time);
      }
    }
    PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE1), AioConstants.BILL_ROW_TYPE13, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(42), bill.getBigDecimal("privilege"), time);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "sell_barter_info");
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE13, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    renderJson();
  }
  
  public void toEdit()
  {
    String configName = loginConfigName();
    Integer id = getParaToInt(0, Integer.valueOf(0));
    Integer draftId = getParaToInt(1, Integer.valueOf(0));
    SellBarterDraftBill bill = (SellBarterDraftBill)SellBarterDraftBill.dao.findById(configName, id);
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailInList = SellBarterDraftDetail.dao.getListByBillId(configName, id, Integer.valueOf(1));
    detailInList = addTrSize(detailInList, 5);
    List<Model> detailOutList = SellBarterDraftDetail.dao.getListByBillId(configName, id, Integer.valueOf(2));
    detailOutList = addTrSize(detailOutList, 5);
    
    setPayTypeAttr(configName, "draftBill", AioConstants.BILL_ROW_TYPE13, bill.getInt("id").intValue());
    
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    BigDecimal allowMoneys = PurchaseBarterBill.getAllowBarterMoney(configName, bill.getInt("unitId").intValue(), df.format(new Date()), "out");
    
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE13, AioConstants.STATUS_ENABLE));
    setAttr("allowMoneys", allowMoneys);
    setAttr("bill", bill);
    setAttr("detailInList", detailInList);
    setAttr("detailOutList", detailOutList);
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE13));
    setAttr("draftId", draftId);
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    notEditStaff();
    setDraftAutoPost();
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE13, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    render("edit.html");
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String configName = loginConfigName();
    SellBarterDraftBill bill = (SellBarterDraftBill)getModel(SellBarterDraftBill.class, "sellBarterBill");
    

    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    
    boolean falg = editDraftVerify(Integer.valueOf(draftId)).booleanValue();
    if (falg) {
      return;
    }
    List<SellBarterDraftDetail> inDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellBarterDraftDetail.class, "sellBarterInDetail");
    List<HelpUtil> helpInList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpInUtil");
    
    List<SellBarterDraftDetail> outDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellBarterDraftDetail.class, "sellBarterOutDetail");
    List<HelpUtil> helpOutList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpOutUtil");
    if ((inDetailList.size() == 0) || (outDetailList.size() == 0))
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer inStorageId = getParaToInt("inStorage.id");
    Integer outStorageId = getParaToInt("outStorage.id");
    bill.set("unitId", getParaToInt("unit.id"));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("outStorageId", outStorageId);
    bill.set("inStorageId", inStorageId);
    bill.set("userId", Integer.valueOf(loginUserId()));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    for (int i = 0; i < inDetailList.size(); i++)
    {
      SellBarterDraftDetail inDetail = (SellBarterDraftDetail)inDetailList.get(i);
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
    }
    for (int i = 0; i < outDetailList.size(); i++)
    {
      SellBarterDraftDetail outDetail = (SellBarterDraftDetail)outDetailList.get(i);
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
    }
    Date time = new Date();
    bill.set("updateTime", time);
    
    billCodeIncrease(bill, "drafEdit");
    bill.update(configName);
    

    BusinessDraftController.updateBillDraft(configName, draftId, bill, AioConstants.BILL_ROW_TYPE13, bill.getBigDecimal("gapMoney"));
    

    StockDraftRecords.delRecords(configName, AioConstants.BILL_ROW_TYPE13, bill.getInt("id").intValue());
    
    List<Integer> detailIds = new ArrayList();
    for (int i = 0; i < inDetailList.size(); i++)
    {
      SellBarterDraftDetail inDetail = (SellBarterDraftDetail)inDetailList.get(i);
      inDetail.set("type", Integer.valueOf(1));
      Integer detailstorage = inDetail.getInt("storageId");
      Integer detailId = inDetail.getInt("id");
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
        if ((inDetail.getBigDecimal("taxPrice") != null) && (BigDecimalUtils.compare(inDetail.getBigDecimal("taxPrice"), BigDecimal.ZERO) != 0)) {
          price = inDetail.getBigDecimal("taxPrice");
        }
        if (detailStorageId == null) {
          detailStorageId = inStorageId;
        }
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          inDetail.set("giftAmount", baseAmount);
        }
        inDetail.set("baseAmount", baseAmount);
        inDetail.set("basePrice", basePrice);
        if (detailId == null) {
          inDetail.save(configName);
        } else {
          inDetail.update(configName);
        }
        BigDecimal stockPrice = inDetail.getBigDecimal("costPrice");
        if (inDetail.getBigDecimal("costPrice") == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, detailStorageId.intValue(), product, inDetail.getBigDecimal("costPrice"), selectUnitId.intValue());
          stockPrice = new BigDecimal((String)map.get("costPrice"));
          
          inDetail.set("costPrice", stockPrice);
        }
        StockDraftRecords.addRecords(configName, AioConstants.BILL_ROW_TYPE13, true, bill, inDetail, stockPrice, baseAmount, time, bill.getTimestamp("recodeDate"), inDetail.getInt("storageId"));
      }
      detailIds.add(inDetail.getInt("id"));
    }
    for (int i = 0; i < outDetailList.size(); i++)
    {
      SellBarterDraftDetail outDetail = (SellBarterDraftDetail)outDetailList.get(i);
      outDetail.set("type", Integer.valueOf(2));
      Integer detailstorage = outDetail.getInt("storageId");
      Integer detailId = outDetail.getInt("id");
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
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        outDetail.set("baseAmount", baseAmount);
        outDetail.set("basePrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          outDetail.set("giftAmount", baseAmount);
        }
        if (detailId == null) {
          outDetail.save(configName);
        } else {
          outDetail.update(configName);
        }
        BigDecimal stockPrice = outDetail.getBigDecimal("costPrice");
        if (outDetail.getBigDecimal("costPrice") == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, outDetail.getInt("storageId").intValue(), product, outDetail.getBigDecimal("costPrice"), selectUnitId.intValue());
          stockPrice = new BigDecimal((String)map.get("costPrice"));
          
          outDetail.set("costPrice", stockPrice);
        }
        StockDraftRecords.addRecords(configName, AioConstants.BILL_ROW_TYPE13, false, bill, outDetail, stockPrice, baseAmount, time, bill.getTimestamp("recodeDate"), outDetail.getInt("storageId"));
      }
      detailIds.add(outDetail.getInt("id"));
    }
    SellBarterDraftDetail.dao.delOthers(configName, bill.getInt("id"), detailIds);
    
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(AioConstants.BILL_ROW_TYPE13));
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    if (StringUtils.isNotBlank(payTypeIds))
    {
      String[] accounts = payTypeIds.split(",");
      String[] payMoneys = payTypeMoneys.split(",");
      for (int i = 0; i < accounts.length; i++)
      {
        BigDecimal payMoneyi = new BigDecimal(payMoneys[i]);
        PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE1), AioConstants.BILL_ROW_TYPE13, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(accounts[i]), payMoneyi, time);
      }
    }
    PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE1), AioConstants.BILL_ROW_TYPE13, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(42), bill.getBigDecimal("privilege"), time);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE13, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
