package com.aioerp.controller.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.finance.AccountVoucherBill;
import com.aioerp.model.finance.AccountVoucherDetail;
import com.aioerp.model.finance.AccountVoucherDraftBill;
import com.aioerp.model.finance.AccountVoucherDraftDetail;
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

public class AccountVoucherController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE32;
  
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
  {
    String configName = loginConfigName();
    
    printBeforSave1();
    AccountVoucherBill bill = (AccountVoucherBill)getModel(AccountVoucherBill.class, "accountVoucherBill");
    
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    bill.set("id", null);
    boolean hasOther = AccountVoucherBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
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
    List<AccountVoucherDetail> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), AccountVoucherDetail.class, "accountVoucherDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择会计科目!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    BigDecimal debitMoneys = bill.getBigDecimal("debitMoneys");
    BigDecimal lendMoneys = bill.getBigDecimal("lendMoneys");
    if (BigDecimalUtils.compare(debitMoneys, lendMoneys) != 0)
    {
      setAttr("message", "借方和贷方必须相等!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      AccountVoucherDetail detail = (AccountVoucherDetail)detailList.get(i);
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1);
      Integer accountsId = detail.getInt("accountsId");
      if (accountsId == null)
      {
        setAttr("message", "第" + trIndex + "行会计科目录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      BigDecimal debitMoney = detail.getBigDecimal("debitMoney");
      BigDecimal lendMoney = detail.getBigDecimal("lendMoney");
      if ((!BigDecimalUtils.notNullZero(debitMoney)) && (!BigDecimalUtils.notNullZero(lendMoney)))
      {
        setAttr("message", "第" + trIndex + "行借方或贷方录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if ((BigDecimalUtils.notNullZero(debitMoney)) && (BigDecimalUtils.notNullZero(lendMoney)))
      {
        setAttr("message", "第" + trIndex + "行录入错误,不能借贷方都 有金额!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    bill.set("staffId", getParaToInt("staff.id"));
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (draftId != 0) {
      billCodeIncrease(bill, "draftPost");
    } else {
      billCodeIncrease(bill, "add");
    }
    bill.save(configName);
    
    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("lendMoneys"));
    for (int i = 0; i < detailList.size(); i++)
    {
      AccountVoucherDetail detail = (AccountVoucherDetail)detailList.get(i);
      detail.set("billId", bill.getInt("id"));
      detail.set("id", null);
      detail.set("updateTime", time);
      BigDecimal debitMoney = detail.getBigDecimal("debitMoney");
      BigDecimal lendMoney = detail.getBigDecimal("lendMoney");
      Integer accountsId = detail.getInt("accountsId");
      
      int positiveOrNegative = AioConstants.ACCOUNT_MONEY0;
      BigDecimal money = BigDecimal.ZERO;
      if ((detail.getInt("detailId") != null) && (detail.getInt("detailId").intValue() != 0)) {
        accountsId = detail.getInt("detailId");
      }
      if ((BigDecimalUtils.notNullZero(debitMoney)) && (Accounts.dao.isJf(configName, accountsId).booleanValue()))
      {
        money = debitMoney;
        positiveOrNegative = AioConstants.ACCOUNT_MONEY0;
      }
      else if ((BigDecimalUtils.notNullZero(debitMoney)) && (!Accounts.dao.isJf(configName, accountsId).booleanValue()))
      {
        money = debitMoney;
        positiveOrNegative = AioConstants.ACCOUNT_MONEY1;
      }
      else if ((BigDecimalUtils.notNullZero(lendMoney)) && (Accounts.dao.isDf(configName, accountsId).booleanValue()))
      {
        money = lendMoney;
        positiveOrNegative = AioConstants.ACCOUNT_MONEY0;
      }
      else if ((BigDecimalUtils.notNullZero(lendMoney)) && (!Accounts.dao.isDf(configName, accountsId).booleanValue()))
      {
        money = lendMoney;
        positiveOrNegative = AioConstants.ACCOUNT_MONEY1;
      }
      if (detail.getInt("unitId") != null)
      {
        int payType = AioConstants.PAY_TYLE0;
        if (accountsId.intValue() == 53) {
          payType = AioConstants.PAY_TYLE1;
        }
        if (accountsId.intValue() == 14) {
          payType = AioConstants.PAY_TYLE0;
        }
        String operte = AioConstants.OPERTE_ADD;
        if (positiveOrNegative == AioConstants.ACCOUNT_MONEY1) {
          operte = AioConstants.OPERTE_DEL;
        }
        PayTypeController.updateUnitNeedGetOrOut(configName, operte, payType, detail.getInt("unitId").intValue(), money, BigDecimal.ZERO);
      }
      PayTypeController.addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(positiveOrNegative), Integer.valueOf(billTypeId), bill.getInt("id"), detail.getInt("unitId"), getParaToInt("staff.id"), null, null, Integer.valueOf(AioConstants.ARAPACCOUNT1), accountsId, money, time, loginUserId());
      
      detail.save(configName);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      setDraftStrs();
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      AccountVoucherDraftBill.dao.deleteById(configName, draftBillId);
      AccountVoucherDraftDetail.dao.delByBill(configName, draftBillId);
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "accountVoucherView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    renderJson();
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> result = new HashMap();
    AccountVoucherBill bill = (AccountVoucherBill)AccountVoucherBill.dao.findById(configName, billId);
    if (bill == null)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", "该会计凭证不存在");
      return result;
    }
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
    bill.update(configName);
    AccountVoucherBill newBill = bill;
    newBill.set("id", null);
    newBill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
    Date time = new Date();
    newBill.set("updateTime", time);
    
    billRcwAddRemark(bill, null);
    newBill.save(configName);
    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("lendMoneys"));
    PayTypeController.rcwAccountsRecoder(configName, billTypeId, billId, newBill.getInt("id"));
    List<Model> detailList = AccountVoucherDetail.dao.getListByBillId(configName, billId);
    for (Model odlDetail : detailList)
    {
      AccountVoucherDetail newDetail = (AccountVoucherDetail)odlDetail;
      
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.set("id", null);
      newDetail.set("billId", newBill.getInt("id"));
      newDetail.set("updateTime", time);
      newDetail.save(configName);
      
      BigDecimal debitMoney = newDetail.getBigDecimal("debitMoney");
      BigDecimal lendMoney = newDetail.getBigDecimal("lendMoney");
      Integer accountsId = newDetail.getInt("accountsId");
      int positiveOrNegative = AioConstants.ACCOUNT_MONEY0;
      BigDecimal money = BigDecimal.ZERO;
      if ((newDetail.getInt("detailId") != null) && (newDetail.getInt("detailId").intValue() != 0)) {
        accountsId = newDetail.getInt("detailId");
      }
      if ((BigDecimalUtils.notNullZero(debitMoney)) && (Accounts.dao.isJf(configName, accountsId).booleanValue()))
      {
        money = debitMoney;
        positiveOrNegative = AioConstants.ACCOUNT_MONEY0;
      }
      else if ((BigDecimalUtils.notNullZero(debitMoney)) && (!Accounts.dao.isJf(configName, accountsId).booleanValue()))
      {
        money = debitMoney;
        positiveOrNegative = AioConstants.ACCOUNT_MONEY1;
      }
      else if ((BigDecimalUtils.notNullZero(lendMoney)) && (Accounts.dao.isDf(configName, accountsId).booleanValue()))
      {
        money = lendMoney;
        positiveOrNegative = AioConstants.ACCOUNT_MONEY0;
      }
      else if ((BigDecimalUtils.notNullZero(lendMoney)) && (!Accounts.dao.isDf(configName, accountsId).booleanValue()))
      {
        money = lendMoney;
        positiveOrNegative = AioConstants.ACCOUNT_MONEY1;
      }
      if (newDetail.getInt("unitId") != null)
      {
        int payType = AioConstants.PAY_TYLE0;
        if (accountsId.intValue() == 53) {
          payType = AioConstants.PAY_TYLE1;
        }
        if (accountsId.intValue() == 14) {
          payType = AioConstants.PAY_TYLE0;
        }
        String operte = AioConstants.OPERTE_RCW;
        if (positiveOrNegative == AioConstants.ACCOUNT_MONEY1) {
          operte = AioConstants.OPERTE_ADD;
        }
        PayTypeController.updateUnitNeedGetOrOut(configName, operte, payType, newDetail.getInt("unitId").intValue(), money, BigDecimal.ZERO);
      }
    }
    result.put("status", Integer.valueOf(AioConstants.RCW_BY));
    result.put("message", "红冲成功");
    return result;
  }
  
  public void look()
  {
    Integer id = getParaToInt(0, Integer.valueOf(0));
    String configName = loginConfigName();
    AccountVoucherBill bill = (AccountVoucherBill)AccountVoucherBill.dao.findById(configName, id);
    List<Model> detailList = AccountVoucherDetail.dao.getListByBillId(configName, id);
    detailList = addTrSize(detailList, 15);
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    render("look.html");
  }
  
  public void print()
    throws ParseException
  {
    Integer billId = getParaToInt("billId");
    Integer draftId = getParaToInt("draftId");
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "会计凭证");
    
    List<String> headData = new ArrayList();
    
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门 :" + getPara("department.fullName", ""));
    String recodeDate = DateUtils.format(getParaToDate("accountVoucherBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    
    headData.add("单据编号:" + getPara("accountVoucherBill.code", ""));
    
    headData.add("摘要:" + getPara("accountVoucherBill.remark", ""));
    headData.add("附加说明 :" + getPara("accountVoucherBill.memo", ""));
    Integer userId = null;
    if ((billId != null) && (billId.intValue() != 0))
    {
      Model bill = AccountVoucherBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    else if ((draftId != null) && (draftId.intValue() != 0))
    {
      Model bill = AccountVoucherDraftBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    setBillUser(headData, userId);
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("总账科目编号");
    colTitle.add("总账科目全名");
    colTitle.add("明细科目");
    colTitle.add("总账科目简称");
    colTitle.add("总账科目拼音码");
    colTitle.add("总账科目备注");
    colTitle.add("借方");
    colTitle.add("贷方");
    colTitle.add("摘要");
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
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), AccountVoucherDetail.class, "accountVoucherDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailList = AccountVoucherDetail.dao.getListByBillId(configName, billId);
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      int accountsId = detail.getInt("accountsId").intValue();
      Accounts accounts = (Accounts)Accounts.dao.findById(configName, Integer.valueOf(accountsId));
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(accounts.getStr("code")));
      rowData.add(trimNull(accounts.getStr("fullName")));
      rowData.add(trimNull(detail.get("accountsDetail")));
      rowData.add(trimNull(accounts.getStr("smallName")));
      rowData.add(trimNull(accounts.getStr("spell")));
      rowData.add(trimNull(accounts.getStr("memo")));
      rowData.add(trimNull(detail.get("debitMoney")));
      rowData.add(trimNull(detail.get("lendMoney")));
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
}
