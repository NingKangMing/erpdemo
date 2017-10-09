package com.aioerp.controller.sell.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.sell.sell.SellDetail;
import com.aioerp.model.sell.sell.SellDraftBill;
import com.aioerp.model.sell.sell.SellDraftDetail;
import com.aioerp.model.stock.StockDraftRecords;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.util.DwzUtils;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SellDraftController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE4;
  
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    Date time = new Date();
    SellDraftBill bill = (SellDraftBill)getModel(SellDraftBill.class, "sellBill");
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellDraftDetail.class, "sellDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    for (int i = 0; i < detailList.size(); i++) {
      ((Model)detailList.get(i)).put("costPrice", ((HelpUtil)helpUitlList.get(i)).getCostPrice());
    }
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    int billStorageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    bill.set("unitId", Integer.valueOf(unitId));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("storageId", Integer.valueOf(billStorageId));
    bill.set("updateTime", time);
    ComFunController.billOrderDefualPriceMoney(bill);
    bill.set("deliveryCompanyCode", getPara("deliveryCompany.code", ""));
    bill.set("deliveryCompany", getPara("deliveryCompany.name", ""));
    bill.set("userId", Integer.valueOf(loginUserId()));
    billCodeIncrease(bill, "draftAdd");
    bill.save(configName);
    

    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    BusinessDraftController.saveBillDraft(configName, billTypeId, bill, bill.getBigDecimal("taxMoneys"));
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      ComFunController.setDetailStorageId(detail, null, null, Integer.valueOf(billStorageId), "storageId", null, null);
      ComFunController.detailOrderDefualPriceMoney(detail);
      detail.set("retailPrice", detail.getBigDecimal("retailPrice"));
      detail.set("retailMoney", detail.getBigDecimal("retailMoney"));
      detail.set("billId", bill.getInt("id"));
      detail.set("relStatus", Integer.valueOf(AioConstants.STATUS_DISABLE));
      detail.set("updateTime", time);
      
      detail.save(configName);
      
      Product product = (Product)Product.dao.findById(configName, detail.getInt("productId"));
      BigDecimal litterAmount = DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), detail.getInt("selectUnitId"), product, Integer.valueOf(1));
      StockDraftRecords.addRecords(configName, billTypeId, false, bill, detail, null, litterAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
    }
    addPayRecords(configName, null, billTypeId, payTypeIds, payTypeMoneys, bill.getInt("id").intValue(), bill.getInt("unitId"), time);
    
    PayDraft.dao.addPayRecords(configName, null, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(42), bill.getBigDecimal("privilege"), time);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "sell_info");
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    renderJson();
  }
  
  public void toEditDraft()
  {
    String configName = loginConfigName();
    
    setDraftAutoPost();
    Integer id = getParaToInt(0, Integer.valueOf(0));
    Integer draftId = getParaToInt(1, Integer.valueOf(0));
    SellDraftBill bill = (SellDraftBill)SellDraftBill.dao.findById(configName, id);
    if (bill == null)
    {
      this.result.put("message", "单据已经不存在!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = SellDetail.dao.getList2(configName, id, "xs_sell_draft_detail");
    
    detailList = addTrSize(detailList, 15);
    
    setPayTypeAttr(configName, "draftBill", billTypeId, id.intValue());
    
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("draftId", draftId);
    
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    notEditStaff();
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
    render("/WEB-INF/template/sell/sell/draft/edit.html");
  }
  
  @Before({Tx.class})
  public void edit()
    throws SQLException
  {
    String configName = loginConfigName();
    SellDraftBill bill = (SellDraftBill)getModel(SellDraftBill.class, "sellBill");
    
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    
    boolean falg = editDraftVerify(Integer.valueOf(draftId)).booleanValue();
    if (falg) {
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellDraftDetail.class, "sellDetail");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer storageId = getParaToInt("storage.id");
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    

    bill.set("unitId", getParaToInt("unit.id"));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("storageId", storageId);
    Date time = new Date();
    bill.set("updateTime", time);
    ComFunController.billOrderDefualPriceMoney(bill);
    
    boolean storageFlag = false;
    if (storageId.intValue() > 0) {
      storageFlag = true;
    }
    bill.set("deliveryCompanyCode", getPara("deliveryCompany.code", ""));
    bill.set("deliveryCompany", getPara("deliveryCompany.name", ""));
    

    billCodeIncrease(bill, "drafEdit");
    bill.update(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    BusinessDraftController.updateBillDraft(configName, draftId, bill, billTypeId, bill.getBigDecimal("taxMoneys"));
    
    StockDraftRecords.delRecords(configName, billTypeId, bill.getInt("id").intValue());
    
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(billTypeId));
    
    List<Integer> detailIds = new ArrayList();
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      ComFunController.detailOrderDefualPriceMoney(detail);
      Integer detailId = detail.getInt("id");
      Integer productId = detail.getInt("productId");
      if (productId != null)
      {
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        Integer storageIdStr = detail.getInt("storageId");
        Integer nstorageId = Integer.valueOf(storageIdStr == null ? 0 : new Integer(storageIdStr.intValue()).intValue());
        if ((storageFlag) && 
          (nstorageId.intValue() < 1)) {
          detail.set("storageId", storageId);
        }
        if (detailId == null) {
          detail.save(configName);
        } else {
          detail.update(configName);
        }
      }
      detailIds.add(detail.getInt("id"));
      

      Product product = (Product)Product.dao.findById(configName, detail.getInt("productId"));
      BigDecimal litterAmount = DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), detail.getInt("selectUnitId"), product, Integer.valueOf(1));
      StockDraftRecords.addRecords(configName, billTypeId, false, bill, detail, null, litterAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
    }
    delTableDetailIdsByBillId("xs_sell_draft_detail", bill.getInt("id"), detailIds);
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(billTypeId));
    

    addPayRecords(configName, null, billTypeId, payTypeIds, payTypeMoneys, bill.getInt("id").intValue(), bill.getInt("unitId"), time);
    
    PayDraft.dao.addPayRecords(configName, null, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(42), bill.getBigDecimal("privilege"), time);
    

    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
