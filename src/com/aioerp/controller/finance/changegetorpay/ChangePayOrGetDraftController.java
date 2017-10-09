package com.aioerp.controller.finance.changegetorpay;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.finance.changegetorpay.ChangeGetOrPayDraft;
import com.aioerp.model.finance.changegetorpay.ChangeGetOrPayDraftDetail;
import com.aioerp.model.finance.changemoney.ChangeMoneyDraft;
import com.aioerp.model.finance.changemoney.ChangeMoneyDraftDetail;
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

public class ChangePayOrGetDraftController
  extends BaseController
{
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String model = getPara("model", "getDown");
    Model bill = null;
    String configName = loginConfigName();
    int modelBillId = AioConstants.BILL_ROW_TYPE26;
    String navTabId = "cw_c_unitgetDown";
    if (model.equals("getDown"))
    {
      bill = (Model)getModel(ChangeGetOrPayDraft.class, "changeGetOrPay");
      navTabId = "cw_c_unitgetDown";
      modelBillId = AioConstants.BILL_ROW_TYPE26;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MERGETYPE0));
    }
    else if (model.equals("getUp"))
    {
      bill = (Model)getModel(ChangeGetOrPayDraft.class, "changeGetOrPay");
      navTabId = "cw_c_unitgetUp";
      modelBillId = AioConstants.BILL_ROW_TYPE27;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MERGETYPE1));
    }
    else if (model.equals("payDown"))
    {
      bill = (Model)getModel(ChangeGetOrPayDraft.class, "changeGetOrPay");
      navTabId = "cw_c_unitpayDown";
      modelBillId = AioConstants.BILL_ROW_TYPE28;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MERGETYPE2));
    }
    else if (model.equals("payUp"))
    {
      bill = (Model)getModel(ChangeGetOrPayDraft.class, "changeGetOrPay");
      navTabId = "cw_c_unitpayUp";
      modelBillId = AioConstants.BILL_ROW_TYPE29;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MERGETYPE3));
    }
    else if (model.equals("moneyDown"))
    {
      bill = (Model)getModel(ChangeMoneyDraft.class, "changeGetOrPay");
      navTabId = "cw_c_moneyDown";
      modelBillId = AioConstants.BILL_ROW_TYPE30;
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MONEYTYPE0));
    }
    else if (model.equals("moneyUp"))
    {
      bill = (Model)getModel(ChangeMoneyDraft.class, "changeGetOrPay");
      navTabId = "cw_c_moneyUp";
      modelBillId = AioConstants.BILL_ROW_TYPE31;
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MONEYTYPE1));
    }
    Date time = new Date();
    bill.set("updateTime", time);
    















    List<Model> detailList = null;
    if ((model.equals("moneyDown")) || (model.equals("moneyUp")))
    {
      bill.set("accountId", getParaToInt("account.id"));
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ChangeMoneyDraftDetail.class, "changeGetOrPayDetail");
    }
    else
    {
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ChangeGetOrPayDraftDetail.class, "changeGetOrPayDetail");
    }
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择会计科目!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    
    billCodeIncrease(bill, "draftAdd");
    bill.save(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), modelBillId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    BusinessDraftController.saveBillDraft(configName, modelBillId, bill, bill.getBigDecimal("moneys"));
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      detail.set("billId", bill.getInt("id"));
      detail.set("updateTime", time);
      detail.save(configName);
      
      addPayRecords(configName, null, modelBillId, String.valueOf(detail.getInt("accountsId")), String.valueOf(detail.getBigDecimal("money")), bill.getInt("id").intValue(), bill.getInt("unitId"), time);
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
        PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), modelBillId, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(accounts[i]), payMoneyi, time);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", navTabId);
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    renderJson();
  }
  
  public void toEditDraft()
  {
    setDraftAutoPost();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    int draftId = getParaToInt(1, Integer.valueOf(0)).intValue();
    int modelBillId = getParaToInt(2, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    Model bill = null;
    List<Model> detailList = null;
    if (modelBillId == AioConstants.BILL_ROW_TYPE26)
    {
      bill = ChangeGetOrPayDraft.dao.findById(configName, Integer.valueOf(id));
      detailList = ChangeGetOrPayDraftDetail.dao.getListByBillId(configName, Integer.valueOf(id));
      setAttr("model", "getDown");
      setAttr("modelName", "应收款减少");
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE27)
    {
      bill = ChangeGetOrPayDraft.dao.findById(configName, Integer.valueOf(id));
      detailList = ChangeGetOrPayDraftDetail.dao.getListByBillId(configName, Integer.valueOf(id));
      setAttr("model", "getUp");
      setAttr("modelName", "应收款增加");
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE28)
    {
      bill = ChangeGetOrPayDraft.dao.findById(configName, Integer.valueOf(id));
      detailList = ChangeGetOrPayDraftDetail.dao.getListByBillId(configName, Integer.valueOf(id));
      setAttr("model", "payDown");
      setAttr("modelName", "应付款减少");
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE29)
    {
      bill = ChangeGetOrPayDraft.dao.findById(configName, Integer.valueOf(id));
      detailList = ChangeGetOrPayDraftDetail.dao.getListByBillId(configName, Integer.valueOf(id));
      setAttr("model", "payUp");
      setAttr("modelName", "应付款增加");
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE30)
    {
      bill = ChangeMoneyDraft.dao.findById(configName, Integer.valueOf(id));
      detailList = ChangeMoneyDraftDetail.dao.getListByBillId(configName, Integer.valueOf(id));
      setAttr("model", "moneyDown");
      setAttr("modelName", "资金减少");
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE31)
    {
      bill = ChangeMoneyDraft.dao.findById(configName, Integer.valueOf(id));
      detailList = ChangeMoneyDraftDetail.dao.getListByBillId(configName, Integer.valueOf(id));
      setAttr("model", "moneyUp");
      setAttr("modelName", "资金增加");
    }
    if ((modelBillId == AioConstants.BILL_ROW_TYPE30) || (modelBillId == AioConstants.BILL_ROW_TYPE31)) {
      setPayTypeAttr(configName, "draftBill", modelBillId, id);
    }
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    setAttr("orderFuJianIds", orderFuJianIds(modelBillId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    setAttr("rowList", BillRow.dao.getListByBillId(configName, modelBillId, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    
    detailList = addTrSize(detailList, 15);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(modelBillId));
    setAttr("draftId", Integer.valueOf(draftId));
    
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    notEditStaff();
    render("/WEB-INF/template/finance/change_getorpay/draft/edit.html");
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String model = getPara("model", "getDown");
    Model bill = null;
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    
    boolean falg = editDraftVerify(Integer.valueOf(draftId)).booleanValue();
    if (falg) {
      return;
    }
    int modelBillId = AioConstants.BILL_ROW_TYPE26;
    if (model.equals("getDown"))
    {
      bill = (Model)getModel(ChangeGetOrPayDraft.class, "changeGetOrPay");
      

      modelBillId = AioConstants.BILL_ROW_TYPE26;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MERGETYPE0));
    }
    else if (model.equals("getUp"))
    {
      bill = (Model)getModel(ChangeGetOrPayDraft.class, "changeGetOrPay");
      

      modelBillId = AioConstants.BILL_ROW_TYPE27;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MERGETYPE1));
    }
    else if (model.equals("payDown"))
    {
      bill = (Model)getModel(ChangeGetOrPayDraft.class, "changeGetOrPay");
      

      modelBillId = AioConstants.BILL_ROW_TYPE28;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MERGETYPE2));
    }
    else if (model.equals("payUp"))
    {
      bill = (Model)getModel(ChangeGetOrPayDraft.class, "changeGetOrPay");
      

      modelBillId = AioConstants.BILL_ROW_TYPE29;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MERGETYPE3));
    }
    else if (model.equals("moneyDown"))
    {
      bill = (Model)getModel(ChangeMoneyDraft.class, "changeGetOrPay");
      

      modelBillId = AioConstants.BILL_ROW_TYPE30;
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MONEYTYPE0));
    }
    else if (model.equals("moneyUp"))
    {
      bill = (Model)getModel(ChangeMoneyDraft.class, "changeGetOrPay");
      

      modelBillId = AioConstants.BILL_ROW_TYPE31;
      bill.set("mergeType", Integer.valueOf(ChangePayOrGetController.MONEYTYPE1));
    }
    List<Model> detailList = null;
    if ((model.equals("moneyDown")) || (model.equals("moneyUp")))
    {
      bill.set("accountId", getParaToInt("account.id"));
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ChangeMoneyDraftDetail.class, "changeGetOrPayDetail");
    }
    else
    {
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ChangeGetOrPayDraftDetail.class, "changeGetOrPayDetail");
    }
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择会计科目!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    
    billCodeIncrease(bill, "drafEdit");
    bill.update(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), modelBillId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    BusinessDraftController.updateBillDraft(configName, draftId, bill, modelBillId, bill.getBigDecimal("moneys"));
    
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(modelBillId));
    List<Integer> detailIds = new ArrayList();
    for (Model detail : detailList)
    {
      detail.set("billId", bill.getInt("id"));
      Integer detailId = detail.getInt("id");
      detail.set("updateTime", time);
      if (detailId == null) {
        detail.save(configName);
      } else {
        detail.update(configName);
      }
      detailIds.add(detail.getInt("id"));
      
      addPayRecords(configName, null, modelBillId, String.valueOf(detail.getInt("accountsId")), String.valueOf(detail.getBigDecimal("money")), bill.getInt("id").intValue(), bill.getInt("unitId"), time);
    }
    if ((model.equals("moneyDown")) || (model.equals("moneyUp"))) {
      ChangeMoneyDraftDetail.dao.delOthers(configName, bill.getInt("id"), detailIds);
    } else {
      ChangeGetOrPayDraftDetail.dao.delOthers(configName, bill.getInt("id"), detailIds);
    }
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(modelBillId));
    if ((!model.equals("getDown")) && (!model.equals("getUp")) && (!model.equals("payDown")) && (!model.equals("payUp"))) {
      if ((model.equals("moneyDown")) || (model.equals("moneyUp")))
      {
        String payTypeIds = getPara("payTypeIds");
        String payTypeMoneys = getPara("payTypeMoneys");
        if (StringUtils.isNotBlank(payTypeIds))
        {
          String[] accounts = payTypeIds.split(",");
          String[] payMoneys = payTypeMoneys.split(",");
          for (int i = 0; i < accounts.length; i++)
          {
            BigDecimal payMoneyi = new BigDecimal(payMoneys[i]);
            PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), modelBillId, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(accounts[i]), payMoneyi, time);
          }
        }
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
