package com.aioerp.controller.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.bought.BoughtDetail;
import com.aioerp.model.bought.PurchaseDraftBill;
import com.aioerp.model.bought.PurchaseDraftDetail;
import com.aioerp.model.finance.PayDraft;
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
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseDraftBillController
  extends BaseController
{
  @Before({Tx.class})
  public void add()
    throws SQLException
  {
    String configName = loginConfigName();
    PurchaseDraftBill bill = (PurchaseDraftBill)getModel(PurchaseDraftBill.class, "purchaseBill");
    
    String codeCurrentFit = AioerpSys.dao.getValue1(loginConfigName(), "codeCurrentFit");
    








    Date recodeDate = bill.getTimestamp("recodeDate");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseDraftDetail.class, "purchaseDetail");
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
    bill.set("userId", Integer.valueOf(loginUserId()));
    bill.set("storageId", storageId);
    Date time = new Date();
    bill.set("updateTime", time);
    if (bill.get("discountMoneys") == null) {
      bill.set("discountMoneys", bill.get("moneys"));
    }
    if (bill.get("taxMoneys") == null) {
      bill.set("taxMoneys", bill.get("discountMoneys"));
    }
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    
    billCodeIncrease(bill, "draftAdd");
    
    bill.save(configName);
    
    BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE5, bill, bill.getBigDecimal("taxMoneys"));
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
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
        if ((detail.getBigDecimal("taxPrice") != null) && (BigDecimalUtils.compare(detail.getBigDecimal("taxPrice"), BigDecimal.ZERO) != 0)) {
          price = detail.getBigDecimal("taxPrice");
        }
        if (detailStorageId == null) {
          detailStorageId = storageId;
        }
        BigDecimal purchasePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(purchaseAmount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        detail.set("untreatedAmount", baseAmount);
        
        detail.set("costPrice", purchasePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          detail.set("giftAmount", baseAmount);
        }
        detail.set("baseAmount", baseAmount);
        detail.set("basePrice", purchasePrice);
        detail.save(configName);
        

        StockDraftRecords.addRecords(configName, AioConstants.BILL_ROW_TYPE5, true, bill, detail, purchasePrice, baseAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
      }
    }
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    









    addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), AioConstants.BILL_ROW_TYPE5, payTypeIds, payTypeMoneys, bill.getInt("id").intValue(), bill.getInt("unitId"), time);
    
    PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), AioConstants.BILL_ROW_TYPE5, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(42), bill.getBigDecimal("privilege"), time);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "purchaseView");
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE5, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    renderJson();
  }
  
  public void toEdit()
  {
    Integer id = getParaToInt(0, Integer.valueOf(0));
    Integer draftId = getParaToInt(1, Integer.valueOf(0));
    String configName = loginConfigName();
    PurchaseDraftBill bill = (PurchaseDraftBill)PurchaseDraftBill.dao.findById(configName, id);
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = PurchaseDraftDetail.dao.getListByBillId(configName, id);
    detailList = addTrSize(detailList, 15);
    

    setPayTypeAttr(configName, "draftBill", AioConstants.BILL_ROW_TYPE5, bill.getInt("id").intValue());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE5, AioConstants.STATUS_ENABLE));
    
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE5));
    setAttr("draftId", draftId);
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, AioConstants.BILL_ROW_TYPE5, "discount")));
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE5, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    
    notEditStaff();
    
    setDraftAutoPost();
    render("edit.html");
  }
  
  @Before({Tx.class})
  public void edit()
    throws SQLException
  {
    String configName = loginConfigName();
    PurchaseDraftBill bill = (PurchaseDraftBill)getModel(PurchaseDraftBill.class, "purchaseBill");
    boolean hasOther = PurchaseDraftBill.dao.codeIsExist(loginConfigName(), bill.getStr("code"), bill.getInt("id").intValue());
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    
    boolean falg = editDraftVerify(Integer.valueOf(draftId)).booleanValue();
    if (falg) {
      return;
    }
    if ((hasOther) || (StringUtils.isBlank(bill.getStr("code"))))
    {
      setAttr("message", "编号已经存在!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), PurchaseDraftDetail.class, "purchaseDetail");
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
    bill.set("userId", Integer.valueOf(loginUserId()));
    bill.set("storageId", storageId);
    Date time = new Date();
    bill.set("updateTime", time);
    if (bill.get("discountMoneys") == null) {
      bill.set("discountMoneys", bill.get("moneys"));
    }
    if (bill.get("taxMoneys") == null) {
      bill.set("taxMoneys", bill.get("discountMoneys"));
    }
    billCodeIncrease(bill, "drafEdit");
    
    bill.update(configName);
    

    BusinessDraftController.updateBillDraft(configName, draftId, bill, AioConstants.BILL_ROW_TYPE5, bill.getBigDecimal("taxMoneys"));
    List<Integer> detailIds = new ArrayList();
    

    StockDraftRecords.delRecords(configName, AioConstants.BILL_ROW_TYPE5, bill.getInt("id").intValue());
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Integer detailId = detail.getInt("id");
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
        if ((detail.getBigDecimal("taxPrice") != null) && (BigDecimalUtils.compare(detail.getBigDecimal("taxPrice"), BigDecimal.ZERO) != 0)) {
          price = detail.getBigDecimal("taxPrice");
        }
        if (detailStorageId == null) {
          detailStorageId = storageId;
        }
        BigDecimal purchasePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(purchaseAmount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        detail.set("untreatedAmount", baseAmount);
        
        detail.set("costPrice", purchasePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          detail.set("giftAmount", baseAmount);
        }
        detail.set("baseAmount", baseAmount);
        detail.set("basePrice", purchasePrice);
        if (detailId == null) {
          detail.save(configName);
        } else {
          detail.update(configName);
        }
        StockDraftRecords.addRecords(configName, AioConstants.BILL_ROW_TYPE5, true, bill, detail, purchasePrice, baseAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
      }
      detailIds.add(detail.getInt("id"));
    }
    PurchaseDraftDetail.dao.delOthers(configName, bill.getInt("id"), detailIds);
    
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(AioConstants.BILL_ROW_TYPE5));
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    if (StringUtils.isNotBlank(payTypeIds))
    {
      String[] accounts = payTypeIds.split(",");
      String[] payMoneys = payTypeMoneys.split(",");
      for (int i = 0; i < accounts.length; i++)
      {
        BigDecimal payMoneyi = new BigDecimal(payMoneys[i]);
        PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), AioConstants.BILL_ROW_TYPE5, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(accounts[i]), payMoneyi, time);
      }
    }
    PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), AioConstants.BILL_ROW_TYPE5, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(42), bill.getBigDecimal("privilege"), time);
    
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE5, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
