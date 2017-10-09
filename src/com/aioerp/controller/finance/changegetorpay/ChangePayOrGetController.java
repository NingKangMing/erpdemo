package com.aioerp.controller.finance.changegetorpay;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.reports.finance.arap.ArapRecordsController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.finance.changegetorpay.ChangeGetOrPay;
import com.aioerp.model.finance.changegetorpay.ChangeGetOrPayDetail;
import com.aioerp.model.finance.changegetorpay.ChangeGetOrPayDraft;
import com.aioerp.model.finance.changegetorpay.ChangeGetOrPayDraftDetail;
import com.aioerp.model.finance.changemoney.ChangeMoney;
import com.aioerp.model.finance.changemoney.ChangeMoneyDetail;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ChangePayOrGetController
  extends BaseController
{
  public static int MERGETYPE0 = 0;
  public static int MERGETYPE1 = 1;
  public static int MERGETYPE2 = 2;
  public static int MERGETYPE3 = 3;
  protected static int MONEYTYPE0 = 0;
  protected static int MONEYTYPE1 = 1;
  
  public void index()
  {
    String model = getPara(0, "getDown");
    setAttr("model", model);
    int modelBillId = AioConstants.BILL_ROW_TYPE26;
    String modelName = "应收款减少";
    if (model.equals("getDown"))
    {
      modelBillId = AioConstants.BILL_ROW_TYPE26;
      modelName = "应收款减少";
    }
    else if (model.equals("getUp"))
    {
      modelBillId = AioConstants.BILL_ROW_TYPE27;
      modelName = "应收款增加";
    }
    else if (model.equals("payDown"))
    {
      modelBillId = AioConstants.BILL_ROW_TYPE28;
      modelName = "应付款减少";
    }
    else if (model.equals("payUp"))
    {
      modelBillId = AioConstants.BILL_ROW_TYPE29;
      modelName = "应付款增加";
    }
    else if (model.equals("moneyDown"))
    {
      modelBillId = AioConstants.BILL_ROW_TYPE30;
      modelName = "资金减少";
    }
    else if (model.equals("moneyUp"))
    {
      modelBillId = AioConstants.BILL_ROW_TYPE31;
      modelName = "资金增加";
    }
    billCodeAuto(modelBillId);
    
    setAttr("modelName", modelName);
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(loginConfigName(), modelBillId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(modelBillId));
    

    notEditStaff();
    render("/WEB-INF/template/finance/change_getorpay/add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    String model = getPara("model", "getDown");
    Model bill = null;
    
    int limitType = -100;
    int type = AioConstants.PAY_TYLE0;
    int modelBillId = AioConstants.BILL_ROW_TYPE26;
    String navTabId = "cw_c_unitgetDown";
    if (model.equals("getDown"))
    {
      limitType = AioConstants.WAY_PAY;
      bill = (Model)getModel(ChangeGetOrPay.class, "changeGetOrPay");
      navTabId = "cw_c_unitgetDown";
      type = AioConstants.PAY_TYLE0;
      modelBillId = AioConstants.BILL_ROW_TYPE26;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(MERGETYPE0));
    }
    else if (model.equals("getUp"))
    {
      limitType = AioConstants.WAY_GET;
      bill = (Model)getModel(ChangeGetOrPay.class, "changeGetOrPay");
      navTabId = "cw_c_unitgetUp";
      type = AioConstants.PAY_TYLE1;
      modelBillId = AioConstants.BILL_ROW_TYPE27;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(MERGETYPE1));
    }
    else if (model.equals("payDown"))
    {
      limitType = AioConstants.WAY_GET;
      bill = (Model)getModel(ChangeGetOrPay.class, "changeGetOrPay");
      navTabId = "cw_c_unitpayDown";
      type = AioConstants.PAY_TYLE1;
      modelBillId = AioConstants.BILL_ROW_TYPE28;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(MERGETYPE2));
    }
    else if (model.equals("payUp"))
    {
      limitType = AioConstants.WAY_PAY;
      bill = (Model)getModel(ChangeGetOrPay.class, "changeGetOrPay");
      navTabId = "cw_c_unitpayUp";
      type = AioConstants.PAY_TYLE0;
      modelBillId = AioConstants.BILL_ROW_TYPE29;
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("mergeType", Integer.valueOf(MERGETYPE3));
    }
    else if (model.equals("moneyDown"))
    {
      bill = (Model)getModel(ChangeMoney.class, "changeGetOrPay");
      navTabId = "cw_c_moneyDown";
      type = AioConstants.PAY_TYLE0;
      modelBillId = AioConstants.BILL_ROW_TYPE30;
      bill.set("mergeType", Integer.valueOf(MONEYTYPE0));
    }
    else if (model.equals("moneyUp"))
    {
      bill = (Model)getModel(ChangeMoney.class, "changeGetOrPay");
      navTabId = "cw_c_moneyUp";
      type = AioConstants.PAY_TYLE0;
      modelBillId = AioConstants.BILL_ROW_TYPE31;
      bill.set("mergeType", Integer.valueOf(MONEYTYPE1));
    }
    if (limitType != -100)
    {
      BigDecimal privilegeMoney = bill.getBigDecimal("moneys");
      Map<String, Object> mapmap = verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_ADD, getParaToInt("unit.id"), limitType, null, privilegeMoney);
      if (mapmap != null)
      {
        renderJson(mapmap);
        return;
      }
    }
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    bill.set("id", null);
    Date time = new Date();
    bill.set("updateTime", time);
    


    boolean hasOther = false;
    if ((model.equals("getDown")) || (model.equals("getUp")) || (model.equals("payDown")) || (model.equals("payUp"))) {
      hasOther = ChangeGetOrPay.dao.codeIsExist(configName, bill.getStr("code"), 0);
    } else if ((model.equals("moneyDown")) || (model.equals("moneyUp"))) {
      hasOther = ChangeMoney.dao.codeIsExist(configName, bill.getStr("code"), 0);
    }
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(modelBillId));
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
    List<Model> detailList = null;
    if ((model.equals("moneyDown")) || (model.equals("moneyUp")))
    {
      bill.set("accountId", getParaToInt("account.id"));
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ChangeMoneyDetail.class, "changeGetOrPayDetail");
    }
    else
    {
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ChangeGetOrPayDetail.class, "changeGetOrPayDetail");
    }
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择会计科目!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      BigDecimal money = ((Model)detailList.get(i)).getBigDecimal("money");
      if (BigDecimalUtils.compare(money, BigDecimal.ZERO) != 1)
      {
        String trIndex = String.valueOf(StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1));
        setAttr("message", "第" + trIndex + "行录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    if ((model.equals("moneyDown")) || (model.equals("moneyUp")))
    {
      BigDecimal moneys = new BigDecimal(getPara("feeBill.getMoney"));
      if (BigDecimalUtils.compare(moneys, bill.getBigDecimal("moneys")) != 0)
      {
        setAttr("message", "付(收)款总金额与应对付(收)款总金额不等，请修正!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      String accountId = getPara("account.fullName");
      if ((accountId == null) || ("".equals(accountId)))
      {
        setAttr("message", "请选择付(收)款账户!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (draftId != 0) {
      billCodeIncrease(bill, "draftPost");
    } else {
      billCodeIncrease(bill, "add");
    }
    bill.save(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), modelBillId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    

    BigDecimal moneys = bill.getBigDecimal("moneys");
    


    BillHistoryController.saveBillHistory(configName, bill, modelBillId, moneys);
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      detail.set("id", null);
      detail.set("updateTime", time);
      detail.set("billId", bill.getInt("id"));
      detail.save(configName);
    }
    if ((model.equals("getDown")) || (model.equals("getUp")) || (model.equals("payDown")) || (model.equals("payUp")))
    {
      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, type, bill.getInt("unitId").intValue(), null, moneys);
      

      ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, type, Integer.valueOf(modelBillId), null, bill, null, bill.getInt("unitId"), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), moneys, null);
      

      Map<String, Object> record = new HashMap();
      record.put("accountList", detailList);
      record.put("totalMoney", moneys);
      record.put("loginUserId", Integer.valueOf(loginUserId()));
      PayTypeController.addAccountsRecoder(configName, modelBillId, bill.getInt("id").intValue(), getParaToInt("unit.id", Integer.valueOf(0)), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), null, null, null, record);
    }
    else if ((model.equals("moneyDown")) || (model.equals("moneyUp")))
    {
      String payTypeIds = getPara("payTypeIds");
      String payTypeMoneys = getPara("payTypeMoneys");
      
      Map<String, Object> record = new HashMap();
      record.put("accountList", detailList);
      record.put("loginUserId", Integer.valueOf(loginUserId()));
      
      PayTypeController.addAccountsRecoder(configName, modelBillId, bill.getInt("id").intValue(), getParaToInt("unit.id", Integer.valueOf(0)), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), null, payTypeIds, payTypeMoneys.toString(), record);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      ChangeGetOrPayDraft.dao.deleteById(configName, draftBillId);
      ChangeGetOrPayDraftDetail.dao.delByBill(configName, draftBillId);
      PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(modelBillId));
      
      setDraftStrs();
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", navTabId);
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    printBeforSave1();
    
    renderJson();
  }
  
  public void look()
  {
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    int modelBillId = getParaToInt(1, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    

    Model bill = null;
    List<Model> detailList = null;
    if ((modelBillId == AioConstants.BILL_ROW_TYPE26) || (modelBillId == AioConstants.BILL_ROW_TYPE27) || (modelBillId == AioConstants.BILL_ROW_TYPE28) || (modelBillId == AioConstants.BILL_ROW_TYPE29))
    {
      bill = ChangeGetOrPay.dao.findById(configName, Integer.valueOf(id));
      detailList = ChangeGetOrPayDetail.dao.getListByBillId(configName, Integer.valueOf(id));
    }
    else if ((modelBillId == AioConstants.BILL_ROW_TYPE30) || (modelBillId == AioConstants.BILL_ROW_TYPE31))
    {
      bill = ChangeMoney.dao.findById(configName, Integer.valueOf(id));
      detailList = ChangeMoneyDetail.dao.getListByBillId(configName, Integer.valueOf(id));
      

      setPayTypeAttr(configName, "saveBill", modelBillId, id);
    }
    if (modelBillId == AioConstants.BILL_ROW_TYPE26)
    {
      setAttr("model", "getDown");
      setAttr("modelName", "应收款减少");
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE27)
    {
      setAttr("model", "getUp");
      setAttr("modelName", "应收款增加");
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE28)
    {
      setAttr("model", "payDown");
      setAttr("modelName", "应付款减少");
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE29)
    {
      setAttr("model", "payUp");
      setAttr("modelName", "应付款增加");
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE30)
    {
      setAttr("model", "moneyDown");
      setAttr("modelName", "资金减少");
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE31)
    {
      setAttr("model", "moneyUp");
      setAttr("modelName", "资金增加");
    }
    setAttr("orderFuJianIds", orderFuJianIds(modelBillId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    setAttr("rowList", BillRow.dao.getListByBillId(configName, modelBillId, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    
    detailList = addTrSize(detailList, 15);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(modelBillId));
    render("/WEB-INF/template/finance/change_getorpay/look.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, int billId, int modelBillId)
  {
    Map<String, Object> result = new HashMap();
    Model bill = null;
    List<Model> detailList = null;
    int oldBillId = 0;
    int newBillId = 0;
    
    int limitType = -100;
    
    int type = AioConstants.PAY_TYLE0;
    String model = "";
    if (modelBillId == AioConstants.BILL_ROW_TYPE26)
    {
      limitType = AioConstants.WAY_PAY;
      model = "getDown";
      type = AioConstants.PAY_TYLE0;
      bill = (ChangeGetOrPay)ChangeGetOrPay.dao.findById(configName, Integer.valueOf(billId));
      detailList = ChangeGetOrPayDetail.dao.getListByBillId(configName, Integer.valueOf(billId));
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE27)
    {
      limitType = AioConstants.WAY_GET;
      model = "getUp";
      type = AioConstants.PAY_TYLE1;
      bill = (ChangeGetOrPay)ChangeGetOrPay.dao.findById(configName, Integer.valueOf(billId));
      detailList = ChangeGetOrPayDetail.dao.getListByBillId(configName, Integer.valueOf(billId));
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE28)
    {
      limitType = AioConstants.WAY_GET;
      model = "payDown";
      type = AioConstants.PAY_TYLE1;
      bill = (ChangeGetOrPay)ChangeGetOrPay.dao.findById(configName, Integer.valueOf(billId));
      detailList = ChangeGetOrPayDetail.dao.getListByBillId(configName, Integer.valueOf(billId));
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE29)
    {
      limitType = AioConstants.WAY_PAY;
      model = "payUp";
      type = AioConstants.PAY_TYLE0;
      bill = (ChangeGetOrPay)ChangeGetOrPay.dao.findById(configName, Integer.valueOf(billId));
      detailList = ChangeGetOrPayDetail.dao.getListByBillId(configName, Integer.valueOf(billId));
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE30)
    {
      model = "moneyDown";
      type = AioConstants.PAY_TYLE0;
      bill = ChangeMoney.dao.findById(configName, Integer.valueOf(billId));
      detailList = ChangeMoneyDetail.dao.getListByBillId(configName, Integer.valueOf(billId));
    }
    else if (modelBillId == AioConstants.BILL_ROW_TYPE31)
    {
      model = "moneyUp";
      type = AioConstants.PAY_TYLE0;
      bill = ChangeMoney.dao.findById(configName, Integer.valueOf(billId));
      detailList = ChangeMoneyDetail.dao.getListByBillId(configName, Integer.valueOf(billId));
    }
    if (bill == null)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", "该单位已经不存在");
      return result;
    }
    if (limitType != -100)
    {
      BigDecimal privilegeMoney = bill.getBigDecimal("moneys");
      Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unit.id"), limitType, null, privilegeMoney);
      if (vmap != null)
      {
        result = vmap;
        result.put("status", Integer.valueOf(AioConstants.RCW_NO));
        return result;
      }
    }
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
    oldBillId = bill.getInt("id").intValue();
    bill.update(configName);
    
    bill.set("id", null);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
    Date time = new Date();
    bill.set("updateTime", time);
    
    billRcwAddRemark(bill, null);
    bill.save(configName);
    newBillId = bill.getInt("id").intValue();
    

    BillHistoryController.saveBillHistory(configName, bill, modelBillId, bill.getBigDecimal("moneys"));
    for (Model odlDetail : detailList)
    {
      ChangeGetOrPayDetail newDetail = (ChangeGetOrPayDetail)odlDetail;
      
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.set("id", null);
      newDetail.set("updateTime", time);
      newDetail.set("billId", bill.getInt("id"));
      newDetail.save(configName);
    }
    if ((model.equals("getDown")) || (model.equals("getUp")) || (model.equals("payDown")) || (model.equals("payUp")))
    {
      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, type, bill.getInt("unitId").intValue(), null, bill.getBigDecimal("moneys"));
      

      ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, type, Integer.valueOf(modelBillId), Integer.valueOf(billId), null, null, null, null, null, null, null);
      

      PayTypeController.rcwAccountsRecoder(configName, modelBillId, Integer.valueOf(oldBillId), Integer.valueOf(newBillId));
    }
    else if ((model.equals("moneyDown")) || (model.equals("moneyUp")))
    {
      PayTypeController.rcwAccountsRecoder(configName, modelBillId, Integer.valueOf(oldBillId), Integer.valueOf(newBillId));
    }
    result.put("status", Integer.valueOf(AioConstants.RCW_BY));
    result.put("message", "红冲成功");
    return result;
  }
  
  public void print()
    throws ParseException
  {
    String model = getPara("model", "getDown");
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    String reportName = "";
    String tableName = "cw_c_money_bill";
    if (model.equals("getDown"))
    {
      reportName = "单位应收减少";
      tableName = "cw_c_unitgetorpay_bill";
    }
    else if (model.equals("getUp"))
    {
      reportName = "单位应收增加";
      tableName = "cw_c_unitgetorpay_bill";
    }
    else if (model.equals("payDown"))
    {
      reportName = "单位应付减少";
      tableName = "cw_c_unitgetorpay_bill";
    }
    else if (model.equals("payUp"))
    {
      reportName = "单位应付增加";
      tableName = "cw_c_unitgetorpay_bill";
    }
    else if (model.equals("moneyDown"))
    {
      reportName = "资金减少";
      tableName = "cw_c_money_bill";
    }
    else if (model.equals("moneyUp"))
    {
      reportName = "资金增加";
      tableName = "cw_c_money_bill";
    }
    data.put("reportName", reportName);
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    Map<String, Object> billData = new HashMap();
    
    Model bill = null;
    List<Model> detailList = null;
    
    Integer billId = Integer.valueOf(0);
    String actionType = getPara("actionType", "");
    if (actionType.equals("look"))
    {
      if ((model.equals("moneyDown")) || (model.equals("moneyUp")))
      {
        bill = ChangeMoney.dao.findById(configName, getParaToInt("bill.id", Integer.valueOf(0)));
        detailList = ChangeMoneyDetail.dao.getListByBillId(configName, bill.getInt("id"));
      }
      else
      {
        bill = ChangeGetOrPay.dao.findById(configName, getParaToInt("bill.id", Integer.valueOf(0)));
        detailList = ChangeGetOrPayDetail.dao.getListByBillId(configName, bill.getInt("id"));
      }
      billId = bill.getInt("id");
    }
    else
    {
      if (model.equals("getDown"))
      {
        bill = (Model)getModel(ChangeGetOrPay.class, "changeGetOrPay");
        bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      }
      else if (model.equals("getUp"))
      {
        bill = (Model)getModel(ChangeGetOrPay.class, "changeGetOrPay");
        bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      }
      else if (model.equals("payDown"))
      {
        bill = (Model)getModel(ChangeGetOrPay.class, "changeGetOrPay");
        bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      }
      else if (model.equals("payUp"))
      {
        bill = (Model)getModel(ChangeGetOrPay.class, "changeGetOrPay");
        bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      }
      else if (model.equals("moneyDown"))
      {
        bill = (Model)getModel(ChangeMoney.class, "changeGetOrPay");
      }
      else if (model.equals("moneyUp"))
      {
        bill = (Model)getModel(ChangeMoney.class, "changeGetOrPay");
      }
      if ((model.equals("moneyDown")) || (model.equals("moneyUp")))
      {
        bill.set("accountId", getParaToInt("account.id"));
        detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ChangeMoneyDetail.class, "changeGetOrPayDetail");
      }
      else
      {
        detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ChangeGetOrPayDetail.class, "changeGetOrPayDetail");
      }
    }
    billData.put("tableName", tableName);
    billData.put("base", getAttr("base"));
    Integer id = bill.getInt("id");
    if ((id != null) && (id.intValue() != 0)) {
      billData.put("id", id);
    }
    boolean flag = printBeforSave2(data, billId).booleanValue();
    if (flag)
    {
      renderJson(data);
      return;
    }
    Model unit = Unit.dao.findById(configName, bill.getInt("unitId"));
    Model staff = Staff.dao.findById(configName, bill.getInt("staffId"));
    Model department = Department.dao.findById(configName, bill.getInt("departmentId"));
    String unitFullName = unit == null ? "" : unit.getStr("fullName");
    String staffFullName = staff == null ? "" : staff.getStr("fullName");
    String departmentName = department == null ? "" : department.getStr("fullName");
    


    printCommonData(headData);
    String recodeDate = DateUtils.format(bill.getDate("recodeDate"));
    headData.add("录单日期:" + recodeDate);
    
    headData.add("单据编号:" + trimNull(bill.getStr("code")));
    headData.add("收费单位 :" + trimNull(unitFullName));
    printUnitCommonData(headData, unit);
    headData.add("经手人 :" + trimNull(staffFullName));
    headData.add("部门 :" + trimNull(departmentName));
    headData.add("摘要:" + trimNull(bill.getStr("remark")));
    headData.add("附加说明 :" + trimNull(bill.getStr("memo")));
    if ((model.equals("moneyDown")) || (model.equals("moneyUp")))
    {
      Integer accountId = bill.getInt("accountId");
      if ((accountId != null) && (accountId.intValue() != 0))
      {
        Accounts acc = (Accounts)Accounts.dao.findById(configName, accountId);
        headData.add("付款账户 :" + trimNull(acc.getStr("fullName")));
      }
      else
      {
        headData.add("付款账户 :");
      }
      headData.add("实付金额 :" + trimNull(bill.getBigDecimal("getMoney")));
      headData.add("合计 :" + trimNull(bill.getBigDecimal("moneys")));
    }
    setOrderUserId(flag, bill, actionType, headData);
    

    colTitle.add("行号");
    colTitle.add("科目编号");
    colTitle.add("科目全名");
    colTitle.add("科目简称");
    colTitle.add("科目拼音码");
    colTitle.add("科目备注");
    colTitle.add("金额");
    colTitle.add("单据备注");
    colTitle.add("附加信息");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Accounts accounts = (Accounts)Accounts.dao.findById(configName, detail.getInt("accountsId"));
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(accounts.getStr("code")));
      rowData.add(trimNull(accounts.getStr("fullName")));
      rowData.add(trimNull(accounts.getStr("smallName")));
      rowData.add(trimNull(accounts.getStr("spell")));
      rowData.add(trimNull(accounts.getStr("memo")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.getStr("memo")));
      rowData.add(trimNull(detail.getStr("message")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    data.put("billData", billData);
    renderJson(data);
  }
}
