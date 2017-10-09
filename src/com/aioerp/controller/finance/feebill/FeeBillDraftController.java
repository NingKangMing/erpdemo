package com.aioerp.controller.finance.feebill;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.finance.feebill.FeeDraftBill;
import com.aioerp.model.finance.feebill.FeeDraftDetail;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
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

public class FeeBillDraftController
  extends BaseController
{
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    FeeDraftBill bill = (FeeDraftBill)getModel(FeeDraftBill.class, "feeBill");
    String configName = loginConfigName();
    Date time = new Date();
    bill.set("updateTime", time);
    



















    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), FeeDraftDetail.class, "feeDetail");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择会计科目!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    bill.set("unitId", getParaToInt("unit.id"));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("accountId", getParaToInt("account.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    
    billCodeIncrease(bill, "draftAdd");
    bill.save(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE22, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    

    BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE22, bill, bill.getBigDecimal("moneys"));
    for (int i = 0; i < detailList.size(); i++)
    {
      FeeDraftDetail detail = (FeeDraftDetail)detailList.get(i);
      detail.set("billId", bill.getInt("id"));
      detail.set("updateTime", time);
      detail.save(configName);
      
      addPayRecords(configName, null, AioConstants.BILL_ROW_TYPE22, String.valueOf(detail.getInt("accountsId")), String.valueOf(detail.getBigDecimal("money")), bill.getInt("id").intValue(), bill.getInt("unitId"), time);
    }
    if (StringUtils.isNotBlank(payTypeIds))
    {
      String[] accounts = payTypeIds.split(",");
      String[] payMoneys = payTypeMoneys.split(",");
      for (int i = 0; i < accounts.length; i++)
      {
        BigDecimal payMoneyi = new BigDecimal(payMoneys[i]);
        PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), AioConstants.BILL_ROW_TYPE22, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(accounts[i]), payMoneyi, time);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "cw_feeBillView");
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
    FeeDraftBill bill = (FeeDraftBill)FeeDraftBill.dao.findById(configName, id);
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = FeeDraftDetail.dao.getListByBillId(configName, id);
    
    detailList = addTrSize(detailList, 15);
    setPayTypeAttr(configName, "draftBill", AioConstants.BILL_ROW_TYPE22, id.intValue());
    
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE22, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE22, AioConstants.STATUS_ENABLE));
    
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE22));
    setAttr("draftId", draftId);
    
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    notEditStaff();
    render("/WEB-INF/template/finance/feeBill/draft/edit.html");
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String configName = loginConfigName();
    FeeDraftBill bill = (FeeDraftBill)getModel(FeeDraftBill.class, "feeBill");
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    
    boolean falg = editDraftVerify(Integer.valueOf(draftId)).booleanValue();
    if (falg) {
      return;
    }
    Date time = new Date();
    bill.set("updateTime", time);
    



















    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), FeeDraftDetail.class, "feeDetail");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择会计科目!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    bill.set("unitId", getParaToInt("unit.id"));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    
    billCodeIncrease(bill, "drafEdit");
    bill.update(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE22, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    BusinessDraftController.updateBillDraft(configName, draftId, bill, AioConstants.BILL_ROW_TYPE22, bill.getBigDecimal("moneys"));
    
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(AioConstants.BILL_ROW_TYPE22));
    List<Integer> detailIds = new ArrayList();
    for (int i = 0; i < detailList.size(); i++)
    {
      FeeDraftDetail detail = (FeeDraftDetail)detailList.get(i);
      detail.set("billId", bill.getInt("id"));
      Integer detailId = detail.getInt("id");
      detail.set("updateTime", time);
      if (detailId == null) {
        detail.save(configName);
      } else {
        detail.update(configName);
      }
      detailIds.add(detail.getInt("id"));
      
      addPayRecords(configName, null, AioConstants.BILL_ROW_TYPE4, String.valueOf(detail.getInt("accountsId")), String.valueOf(detail.getBigDecimal("money")), bill.getInt("id").intValue(), bill.getInt("unitId"), time);
    }
    FeeDraftDetail.dao.delOthers(configName, bill.getInt("id"), detailIds);
    
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(AioConstants.BILL_ROW_TYPE22));
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    if (StringUtils.isNotBlank(payTypeIds))
    {
      String[] accounts = payTypeIds.split(",");
      String[] payMoneys = payTypeMoneys.split(",");
      for (int i = 0; i < accounts.length; i++)
      {
        BigDecimal payMoneyi = new BigDecimal(payMoneys[i]);
        PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), AioConstants.BILL_ROW_TYPE22, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(accounts[i]), payMoneyi, time);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
