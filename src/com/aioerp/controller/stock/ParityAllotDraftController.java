package com.aioerp.controller.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.stock.ParityAllotDraftBill;
import com.aioerp.model.stock.ParityAllotDraftDetail;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ParityAllotDraftController
  extends BaseController
{
  @Before({Tx.class})
  public void add()
  {
    String configName = loginConfigName();
    ParityAllotDraftBill bill = (ParityAllotDraftBill)getModel(ParityAllotDraftBill.class, "parityAllotBill");
    

    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    








    Date recodeDate = bill.getTimestamp("recodeDate");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ParityAllotDraftDetail.class, "parityAllotDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer inStorageId = getParaToInt("inStorage.id");
    Integer outStorageId = getParaToInt("outStorage.id");
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Integer detailInStorage = detail.getInt("inStorageId");
      Integer detailOutStorage = detail.getInt("outStorageId");
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1);
      Integer productId = detail.getInt("productId");
      if (productId == null)
      {
        setAttr("message", "第" + trIndex + "行商品录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (((inStorageId == null) || (inStorageId.intValue() == 0)) && ((detailInStorage == null) || (detailInStorage.intValue() == 0)))
      {
        setAttr("message", "第" + trIndex + "行多收货仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (((outStorageId == null) || (outStorageId.intValue() == 0)) && ((detailOutStorage == null) || (detailOutStorage.intValue() == 0)))
      {
        setAttr("message", "第" + trIndex + "行多发货仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    
    bill.set("inStorageId", inStorageId);
    bill.set("outStorageId", outStorageId);
    bill.set("userId", Integer.valueOf(loginUserId()));
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    String staffName = getPara("staff.name");
    String remark = bill.getStr("remark");
    String inStorageFullName = getPara("inStorage.fullName");
    if (StringUtils.isNotBlank(remark))
    {
      remark = remark + "调拨货物到";
      if (StringUtils.isNotBlank(inStorageFullName)) {
        remark = remark + "【" + inStorageFullName + "】";
      }
      if (StringUtils.isNotBlank(staffName)) {
        remark = remark + ":" + staffName;
      }
    }
    else
    {
      remark = "调拨货物到";
      if (StringUtils.isNotBlank(inStorageFullName)) {
        remark = remark + "【" + inStorageFullName + "】";
      }
      if (StringUtils.isNotBlank(staffName)) {
        remark = remark + ":" + staffName;
      }
    }
    bill.set("remark", remark);
    
    billCodeIncrease(bill, "draftAdd");
    bill.save(configName);
    

    BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE14, bill, bill.getBigDecimal("moneys"));
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
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        BigDecimal price = detail.getBigDecimal("price");
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        detail.set("baseAmount", baseAmount);
        detail.set("basePrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          detail.set("giftAmount", baseAmount);
        }
        detail.save(configName);
        
        StockDraftRecords.addRecords(configName, AioConstants.BILL_ROW_TYPE14, false, bill, detail, basePrice, baseAmount, time, bill.getTimestamp("recodeDate"), detailOutStorage);
        StockDraftRecords.addRecords(configName, AioConstants.BILL_ROW_TYPE14, true, bill, detail, basePrice, baseAmount, time, bill.getTimestamp("recodeDate"), detailInStorage);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "parityAllotView");
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE14, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    renderJson();
  }
  
  public void toEdit()
  {
    String configName = loginConfigName();
    Integer id = getParaToInt(0, Integer.valueOf(0));
    Integer draftId = getParaToInt(1, Integer.valueOf(0));
    ParityAllotDraftBill bill = (ParityAllotDraftBill)ParityAllotDraftBill.dao.findById(configName, id);
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = ParityAllotDraftDetail.dao.getListByBillId(configName, id);
    detailList = addTrSize(detailList, 15);
    



    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE14, AioConstants.STATUS_ENABLE));
    
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE14));
    setAttr("draftId", draftId);
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    notEditStaff();
    setDraftAutoPost();
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE14, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    render("edit.html");
  }
  
  public void edit()
  {
    String configName = loginConfigName();
    ParityAllotDraftBill bill = (ParityAllotDraftBill)getModel(ParityAllotDraftBill.class, "parityAllotBill");
    
    boolean hasOther = ParityAllotDraftBill.dao.codeIsExist(configName, bill.getStr("code"), bill.getInt("id").intValue());
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ParityAllotDraftDetail.class, "parityAllotDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer inStorageId = getParaToInt("inStorage.id");
    Integer outStorageId = getParaToInt("outStorage.id");
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Integer detailInStorage = detail.getInt("inStorageId");
      Integer detailOutStorage = detail.getInt("outStorageId");
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1);
      Integer productId = detail.getInt("productId");
      if (productId == null)
      {
        setAttr("message", "第" + trIndex + "行商品录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (((inStorageId == null) || (inStorageId.intValue() == 0)) && ((detailInStorage == null) || (detailInStorage.intValue() == 0)))
      {
        setAttr("message", "第" + trIndex + "行多收货仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (((outStorageId == null) || (outStorageId.intValue() == 0)) && ((detailOutStorage == null) || (detailOutStorage.intValue() == 0)))
      {
        setAttr("message", "第" + trIndex + "行多发货仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    
    bill.set("inStorageId", inStorageId);
    bill.set("outStorageId", outStorageId);
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("userId", Integer.valueOf(loginUserId()));
    

    billCodeIncrease(bill, "drafEdit");
    bill.update(configName);
    

    BusinessDraftController.updateBillDraft(configName, draftId, bill, AioConstants.BILL_ROW_TYPE14, bill.getBigDecimal("moneys"));
    List<Integer> detailIds = new ArrayList();
    

    StockDraftRecords.delRecords(configName, AioConstants.BILL_ROW_TYPE14, bill.getInt("id").intValue());
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Integer detailId = detail.getInt("id");
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
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        BigDecimal price = detail.getBigDecimal("price");
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        detail.set("baseAmount", baseAmount);
        detail.set("basePrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          detail.set("giftAmount", baseAmount);
        }
        if (detailId == null) {
          detail.save(configName);
        } else {
          detail.update(configName);
        }
        StockDraftRecords.addRecords(configName, AioConstants.BILL_ROW_TYPE14, false, bill, detail, basePrice, baseAmount, time, bill.getTimestamp("recodeDate"), detailOutStorage);
        StockDraftRecords.addRecords(configName, AioConstants.BILL_ROW_TYPE14, true, bill, detail, basePrice, baseAmount, time, bill.getTimestamp("recodeDate"), detailInStorage);
      }
      detailIds.add(detail.getInt("id"));
    }
    ParityAllotDraftDetail.dao.delOthers(configName, bill.getInt("id"), detailIds);
    
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(AioConstants.BILL_ROW_TYPE14));
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    if (StringUtils.isNotBlank(payTypeIds))
    {
      String[] accounts = payTypeIds.split(",");
      String[] payMoneys = payTypeMoneys.split(",");
      for (int i = 0; i < accounts.length; i++)
      {
        BigDecimal payMoneyi = new BigDecimal(payMoneys[i]);
        PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), AioConstants.BILL_ROW_TYPE14, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(accounts[i]), payMoneyi, time);
      }
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE14, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
