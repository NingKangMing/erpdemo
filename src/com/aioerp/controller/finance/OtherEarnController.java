package com.aioerp.controller.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.reports.finance.arap.ArapRecordsController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.finance.OtherEarnBill;
import com.aioerp.model.finance.OtherEarnDetail;
import com.aioerp.model.finance.OtherEarnDraftBill;
import com.aioerp.model.finance.OtherEarnDraftDetail;
import com.aioerp.model.finance.PayDraft;
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
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class OtherEarnController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE18;
  
  public void index()
  {
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(loginConfigName(), billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    
    billCodeAuto(billTypeId);
    
    notEditStaff();
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    
    printBeforSave1();
    OtherEarnBill bill = (OtherEarnBill)getModel(OtherEarnBill.class);
    
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    Map<String, Object> pmap = verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_ADD, getParaToInt("unit.id"), AioConstants.WAY_GET, bill.getBigDecimal("getMoney"), bill.getBigDecimal("moneys"));
    if (pmap != null)
    {
      renderJson(pmap);
      return;
    }
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    bill.set("id", null);
    Date time = new Date();
    bill.set("updateTime", time);
    
    boolean hasOther = OtherEarnBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(billTypeId));
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
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    if (unitId == 0)
    {
      setAttr("message", "请选购买单位!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), OtherEarnDetail.class, "otherEarnDetail");
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
      if (BigDecimalUtils.compare(money, BigDecimal.ZERO) == 0)
      {
        String trIndex = String.valueOf(StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1));
        setAttr("message", "第" + trIndex + "行录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    bill.set("unitId", getParaToInt("unit.id"));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("accountId", getParaToInt("account.id"));
    if ((getParaToInt("account.id") == null) || (getParaToInt("account.id").intValue() == 0)) {
      bill.set("getMoney", BigDecimal.ZERO);
    }
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (draftId != 0) {
      billCodeIncrease(bill, "draftPost");
    } else {
      billCodeIncrease(bill, "add");
    }
    bill.save(configName);
    
    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
    for (int i = 0; i < detailList.size(); i++)
    {
      OtherEarnDetail detail = (OtherEarnDetail)detailList.get(i);
      detail.set("id", null);
      detail.set("updateTime", time);
      detail.set("billId", bill.getInt("id"));
      detail.save(configName);
      
      PayTypeController.addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(billTypeId), bill.getInt("id"), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), null, Integer.valueOf(AioConstants.ARAPACCOUNT1), detail.getInt("accountsId"), detail.getBigDecimal("money"), time, loginUserId());
    }
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    Map<String, Object> record = new HashMap();
    record.put("getOrPayType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    record.put("accounts", getOtherAccount(BigDecimalUtils.sub(bill.getBigDecimal("moneys"), bill.getBigDecimal("getMoney"))));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), null, payTypeIds, payTypeMoneys, record);
    

    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), bill.getBigDecimal("getMoney"), bill.getBigDecimal("moneys"));
    

    BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("moneys"), bill.getBigDecimal("getMoney"));
    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), needGetOrPay, bill.getBigDecimal("getMoney"));
    

    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      setDraftStrs();
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      OtherEarnDraftBill.dao.deleteById(configName, draftBillId);
      OtherEarnDraftDetail.dao.delByBill(configName, draftBillId);
      PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(billTypeId));
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "otherEarnView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    renderJson();
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    OtherEarnBill bill = (OtherEarnBill)OtherEarnBill.dao.findById(configName, Integer.valueOf(id));
    List<Model> detailList = OtherEarnDetail.dao.getListByBillId(configName, Integer.valueOf(id));
    detailList = addTrSize(detailList, 15);
    setPayTypeAttr(configName, "saveBill", billTypeId, id);
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    render("look.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> result = new HashMap();
    
    OtherEarnBill bill = (OtherEarnBill)OtherEarnBill.dao.findById(configName, billId);
    if (bill == null)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", "该其它收入单不存在");
      return result;
    }
    Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unitId"), AioConstants.WAY_GET, bill.getBigDecimal("getMoney"), bill.getBigDecimal("moneys"));
    if (vmap != null)
    {
      result = vmap;
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      return result;
    }
    List<Model> detailList = OtherEarnDetail.dao.getListByBillId(configName, billId);
    
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
    bill.update(configName);
    
    OtherEarnBill newBill = bill;
    newBill.set("id", null);
    newBill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
    Date time = new Date();
    newBill.set("updateTime", time);
    
    billRcwAddRemark(bill, null);
    newBill.save(configName);
    

    BillHistoryController.saveBillHistory(configName, newBill, billTypeId, newBill.getBigDecimal("moneys"));
    for (Model odlDetail : detailList)
    {
      OtherEarnDetail newDetail = (OtherEarnDetail)odlDetail;
      
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.set("id", null);
      newDetail.set("updateTime", time);
      newDetail.set("billId", newBill.getInt("id"));
      newDetail.save(configName);
    }
    PayTypeController.rcwAccountsRecoder(configName, billTypeId, billId, newBill.getInt("id"));
    

    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), bill.getBigDecimal("getMoney"), bill.getBigDecimal("moneys"));
    

    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), billId, null, null, null, null, null, null, null);
    
    result.put("status", Integer.valueOf(AioConstants.RCW_BY));
    result.put("message", "红冲成功");
    return result;
  }
  
  public void print()
    throws ParseException
  {
    String configName = loginConfigName();
    Integer billId = getParaToInt("billId");
    Integer draftId = getParaToInt("draftId");
    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "其它收入单");
    Integer unitId = getParaToInt("unit.id");
    
    List<String> headData = new ArrayList();
    
    setHeadUnitData(headData, unitId);
    headData.add("付款单位 :" + getPara("unit.fullName", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门 :" + getPara("department.fullName", ""));
    String recodeDate = DateUtils.format(getParaToDate("otherEarnBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    
    headData.add("单据编号:" + getPara("otherEarnBill.code", ""));
    
    headData.add("摘要:" + getPara("otherEarnBill.remark", ""));
    headData.add("附加说明 :" + getPara("otherEarnBill.memo", ""));
    

    headData.add("收款账户:" + getPara("account.fullName", ""));
    headData.add("实收金额:" + getPara("otherEarnBill.moneys", ""));
    Integer userId = null;
    if ((billId != null) && (billId.intValue() != 0))
    {
      Model bill = OtherEarnBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    else if ((draftId != null) && (draftId.intValue() != 0))
    {
      Model bill = OtherEarnDraftBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    setBillUser(headData, userId);
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("科目编号");
    colTitle.add("科目全名");
    colTitle.add("科目简称");
    colTitle.add("科目拼音码");
    colTitle.add("科目备注");
    colTitle.add("金额");
    colTitle.add("单据备注");
    colTitle.add("附加信息");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), OtherEarnDetail.class, "otherEarnDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailList = OtherEarnDetail.dao.getListByBillId(configName, billId);
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      int accountsId = detail.getInt("accountsId").intValue();
      Accounts accounts = (Accounts)Accounts.dao.findById(configName, Integer.valueOf(accountsId));
      
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(accounts.getStr("code")));
      rowData.add(trimNull(accounts.getStr("fullName")));
      rowData.add(trimNull(accounts.getStr("smallName")));
      rowData.add(trimNull(accounts.getStr("spell")));
      rowData.add(trimNull(accounts.getStr("memo")));
      rowData.add(trimNull(detail.get("money")));
      rowData.add(trimNull(detail.get("memo")));
      rowData.add(trimNull(detail.get("message")));
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
  
  private List<Record> getOtherAccount(BigDecimal shouldGet)
  {
    List<Record> accountList = new ArrayList();
    
    Record should = new Record();
    should.set("moneyType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    should.set("accountType", Integer.valueOf(AioConstants.ACCOUNT_TYPE3));
    should.set("arapAccount", Integer.valueOf(AioConstants.ARAPACCOUNT0));
    should.set("money", shouldGet);
    should.set("account", "000413");
    accountList.add(should);
    
    return accountList;
  }
}
