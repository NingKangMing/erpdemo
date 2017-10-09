package com.aioerp.controller.finance.feebill;

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
import com.aioerp.model.finance.feebill.FeeBill;
import com.aioerp.model.finance.feebill.FeeDetail;
import com.aioerp.model.finance.feebill.FeeDraftBill;
import com.aioerp.model.finance.feebill.FeeDraftDetail;
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

public class FeeBillController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE22;
  
  public void index()
  {
    billCodeAuto(billTypeId);
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(loginConfigName(), billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    

    notEditStaff();
    render("/WEB-INF/template/finance/feeBill/add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    FeeBill bill = (FeeBill)getModel(FeeBill.class);
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    if (unitId != 0)
    {
      BigDecimal privilegeMoney = bill.getBigDecimal("moneys");
      BigDecimal payMoney = bill.getBigDecimal("getMoney ");
      Map<String, Object> mapmap = verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_ADD, getParaToInt("unit.id"), AioConstants.WAY_PAY, payMoney, privilegeMoney);
      if (mapmap != null)
      {
        renderJson(mapmap);
        return;
      }
    }
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    bill.set("id", null);
    Date time = new Date();
    bill.set("updateTime", time);
    
    boolean hasOther = FeeBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), FeeDetail.class, "feeDetail");
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
    BigDecimal moneys = bill.getBigDecimal("moneys");
    BigDecimal transactionMoney = new BigDecimal(getPara("feeBill.getMoney", "0"));
    if (unitId == 0) {
      if (BigDecimalUtils.compare(moneys, transactionMoney) != 0)
      {
        setAttr("message", "【科目总金额】必须与【实付金额】相等!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    bill.set("unitId", Integer.valueOf(unitId));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("accountId", getParaToInt("account.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (draftId != 0) {
      billCodeIncrease(bill, "draftPost");
    } else {
      billCodeIncrease(bill, "add");
    }
    bill.save(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    
    int accountId = getParaToInt("account.id", Integer.valueOf(0)).intValue();
    if (accountId == 0)
    {
      Accounts ac = (Accounts)Accounts.dao.getModelFirst(configName, "0006");
      accountId = ac.getInt("id").intValue();
    }
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    if (unitId != 0)
    {
      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, unitId, transactionMoney, moneys);
      

      ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), null, bill, null, Integer.valueOf(unitId), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), moneys, transactionMoney);
      


      Map<String, Object> record = new HashMap();
      record.put("accountList", detailList);
      record.put("needGetOrPay", BigDecimalUtils.sub(moneys, transactionMoney));
      record.put("loginUserId", Integer.valueOf(loginUserId()));
      PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), Integer.valueOf(unitId), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), null, payTypeIds, payTypeMoneys, record);
    }
    else
    {
      Map<String, Object> record = new HashMap();
      record.put("accountList", detailList);
      record.put("needGetOrPay", BigDecimal.ZERO);
      record.put("loginUserId", Integer.valueOf(loginUserId()));
      PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), Integer.valueOf(unitId), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), null, payTypeIds, payTypeMoneys, record);
    }
    BillHistoryController.saveBillHistory(configName, bill, billTypeId, moneys);
    for (int i = 0; i < detailList.size(); i++)
    {
      FeeDetail detail = (FeeDetail)detailList.get(i);
      detail.set("id", null);
      detail.set("updateTime", time);
      detail.set("billId", bill.getInt("id"));
      detail.save(configName);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      FeeDraftBill.dao.deleteById(configName, draftBillId);
      FeeDraftDetail.dao.delByBill(configName, draftBillId);
      PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(billTypeId));
      
      PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(billTypeId));
      

      setDraftStrs();
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "cw_feeBillView");
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
    String configName = loginConfigName();
    FeeBill bill = (FeeBill)FeeBill.dao.findById(configName, Integer.valueOf(id));
    List<Model> detailList = FeeDetail.dao.getListByBillId(configName, Integer.valueOf(id));
    
    detailList = addTrSize(detailList, 15);
    setPayTypeAttr(configName, "saveBill", billTypeId, id);
    
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    render("/WEB-INF/template/finance/feeBill/look.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> result = new HashMap();
    int oldBillId = 0;
    int newBillId = 0;
    
    FeeBill bill = (FeeBill)FeeBill.dao.findById(configName, billId);
    if (bill == null)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", "该其它收入单不存在");
      return result;
    }
    BigDecimal privilegeMoney = bill.getBigDecimal("moneys");
    BigDecimal payMoney = bill.getBigDecimal("getMoney");
    Integer unitId = bill.getInt("unitId");
    if ((unitId != null) && (unitId.intValue() != 0))
    {
      Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unit.id"), AioConstants.WAY_PAY, payMoney, privilegeMoney);
      if (vmap != null)
      {
        result = vmap;
        result.put("status", Integer.valueOf(AioConstants.RCW_NO));
        return result;
      }
    }
    List<Model> detailList = FeeDetail.dao.getListByBillId(configName, billId);
    oldBillId = bill.getInt("id").intValue();
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
    bill.update(configName);
    
    FeeBill newBill = bill;
    newBill.set("id", null);
    newBill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
    Date time = new Date();
    newBill.set("updateTime", time);
    
    billRcwAddRemark(bill, null);
    newBill.save(configName);
    newBillId = bill.getInt("id").intValue();
    
    BillHistoryController.saveBillHistory(configName, newBill, billTypeId, newBill.getBigDecimal("moneys"));
    for (Model odlDetail : detailList)
    {
      FeeDetail newDetail = (FeeDetail)odlDetail;
      
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.set("id", null);
      newDetail.set("updateTime", time);
      newDetail.set("billId", newBill.getInt("id"));
      newDetail.save(configName);
    }
    BigDecimal moneys = bill.getBigDecimal("moneys");
    BigDecimal transactionMoney = bill.getBigDecimal("getMoney");
    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, unitId.intValue(), transactionMoney, moneys);
    

    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), billId, null, null, null, null, null, null, null);
    

    PayTypeController.rcwAccountsRecoder(configName, billTypeId, Integer.valueOf(oldBillId), Integer.valueOf(newBillId));
    
    result.put("status", Integer.valueOf(AioConstants.RCW_BY));
    result.put("message", "红冲成功");
    return result;
  }
  
  public void print()
    throws ParseException
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "费用单");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    Map<String, Object> billData = new HashMap();
    String configName = loginConfigName();
    
    Model bill = null;
    List<Model> detailList = null;
    
    Integer billId = Integer.valueOf(0);
    
    String actionType = getPara("actionType", "");
    if (actionType.equals("look"))
    {
      bill = FeeBill.dao.findById(configName, getParaToInt("bill.id", Integer.valueOf(0)));
      detailList = FeeDetail.dao.getListByBillId(configName, bill.getInt("id"));
      billId = bill.getInt("id");
    }
    else
    {
      bill = (Model)getModel(FeeBill.class);
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("staffId", getParaToInt("staff.id", Integer.valueOf(0)));
      bill.set("departmentId", getParaToInt("department.id", Integer.valueOf(0)));
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), FeeDetail.class, "feeDetail");
    }
    billData.put("tableName", "cw_fee_bill");
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
