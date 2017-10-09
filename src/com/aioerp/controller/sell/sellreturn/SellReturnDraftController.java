package com.aioerp.controller.sell.sellreturn;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.base.Product;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.sell.sellreturn.SellReturnDetail;
import com.aioerp.model.sell.sellreturn.SellReturnDraftBill;
import com.aioerp.model.sell.sellreturn.SellReturnDraftDetail;
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

public class SellReturnDraftController
  extends BaseController
{
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    SellReturnDraftBill bill = (SellReturnDraftBill)getModel(SellReturnDraftBill.class, "sellReturnBill");
    










    List<Model> detail = ModelUtils.batchInjectSortObjModel(getRequest(), SellReturnDraftDetail.class, "sellReturnDetail");
    if (detail.size() == 0)
    {
      setAttr("message", "请选择要商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    

    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    bill.set("unitId", Integer.valueOf(unitId));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("storageId", Integer.valueOf(storageId));
    


    Date time = new Date();
    bill.set("updateTime", time);
    ComFunController.billOrderDefualPriceMoney(bill);
    
    bill.set("userId", Integer.valueOf(loginUserId()));
    

    billCodeIncrease(bill, "draftAdd");
    bill.save(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE7, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE7, bill, bill.getBigDecimal("taxMoneys"));
    
    boolean storageFlag = false;
    if (storageId > 0) {
      storageFlag = true;
    }
    for (int i = 0; i < detail.size(); i++)
    {
      Model sellDetail1 = (Model)detail.get(i);
      Integer storageIdStr = sellDetail1.getInt("storageId");
      Integer nstorageId = Integer.valueOf(storageIdStr == null ? 0 : new Integer(storageIdStr.intValue()).intValue());
      if ((storageFlag) && 
        (nstorageId.intValue() < 1)) {
        sellDetail1.set("storageId", Integer.valueOf(storageId));
      }
      ComFunController.detailOrderDefualPriceMoney(sellDetail1);
      sellDetail1.set("retailPrice", sellDetail1.getBigDecimal("retailPrice"));
      sellDetail1.set("retailMoney", sellDetail1.getBigDecimal("retailMoney"));
      
      sellDetail1.set("billId", bill.getInt("id"));
      
      sellDetail1.set("updateTime", time);
      
      sellDetail1.save(configName);
      
      Product product = (Product)Product.dao.findById(configName, sellDetail1.getInt("productId"));
      BigDecimal litterAmount = DwzUtils.getConversionAmount(sellDetail1.getBigDecimal("amount"), sellDetail1.getInt("selectUnitId"), product, Integer.valueOf(1));
      StockDraftRecords.addRecords(configName, AioConstants.BILL_ROW_TYPE7, false, bill, sellDetail1, null, litterAmount, time, bill.getTimestamp("recodeDate"), sellDetail1.getInt("storageId"));
    }
    addPayRecords(configName, null, AioConstants.BILL_ROW_TYPE7, payTypeIds, payTypeMoneys, bill.getInt("id").intValue(), bill.getInt("unitId"), time);
    
    PayDraft.dao.addPayRecords(configName, null, AioConstants.BILL_ROW_TYPE7, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(42), bill.getBigDecimal("privilege"), time);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "sell_return_info");
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
    SellReturnDraftBill bill = (SellReturnDraftBill)SellReturnDraftBill.dao.findById(configName, id);
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = SellReturnDetail.dao.getList2(configName, id.intValue(), "xs_return_draft_detail");
    
    detailList = addTrSize(detailList, 15);
    
    setPayTypeAttr(configName, "draftBill", AioConstants.BILL_ROW_TYPE7, id.intValue());
    
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE7, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE7, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE7));
    setAttr("draftId", draftId);
    
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    notEditStaff();
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, AioConstants.BILL_ROW_TYPE7, "discount")));
    render("/WEB-INF/template/sell/return/draft/edit.html");
  }
  
  @Before({Tx.class})
  public void edit()
    throws SQLException
  {
    String configName = loginConfigName();
    SellReturnDraftBill bill = (SellReturnDraftBill)getModel(SellReturnDraftBill.class, "sellReturnBill");
    
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    
    boolean falg = editDraftVerify(Integer.valueOf(draftId)).booleanValue();
    if (falg) {
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellReturnDraftDetail.class, "sellReturnDetail");
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
    
    billCodeIncrease(bill, "drafEdit");
    bill.update(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE7, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    
    boolean storageFlag = false;
    if (storageId.intValue() > 0) {
      storageFlag = true;
    }
    BusinessDraftController.updateBillDraft(configName, draftId, bill, AioConstants.BILL_ROW_TYPE7, bill.getBigDecimal("taxMoneys"));
    
    StockDraftRecords.delRecords(configName, AioConstants.BILL_ROW_TYPE7, bill.getInt("id").intValue());
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
    }
    delTableDetailIdsByBillId("xs_return_draft_detail", bill.getInt("id"), detailIds);
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(AioConstants.BILL_ROW_TYPE7));
    

    addPayRecords(configName, null, AioConstants.BILL_ROW_TYPE7, payTypeIds, payTypeMoneys, bill.getInt("id").intValue(), bill.getInt("unitId"), time);
    
    PayDraft.dao.addPayRecords(configName, null, AioConstants.BILL_ROW_TYPE7, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(42), bill.getBigDecimal("privilege"), time);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
