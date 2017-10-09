package com.aioerp.controller.finance.getmoney;

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
import com.aioerp.model.finance.PayMoneyToDraftOrder;
import com.aioerp.model.finance.PayMoneyToOrder;
import com.aioerp.model.finance.getmoney.GetMoneyBill;
import com.aioerp.model.finance.getmoney.GetMoneyDetail;
import com.aioerp.model.finance.getmoney.GetMoneyDraftBill;
import com.aioerp.model.finance.getmoney.GetMoneyDraftDetail;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class GetMoneyController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE17;
  
  public void index()
  {
    billCodeAuto(billTypeId);
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(loginConfigName(), billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    if (AioConstants.IS_FREE_VERSION == "yes") {
      setAttr("settlementType", "money");
    } else {
      setAttr("settlementType", "order");
    }
    notEditStaff();
    render("/WEB-INF/template/finance/getMoney/add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    try
    {
      GetMoneyBill bill = (GetMoneyBill)getModel(GetMoneyBill.class);
      

      Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
      if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(rmap);
        return;
      }
      BigDecimal privilegeMoney = bill.getBigDecimal("privilegeMoney");
      Map<String, Object> mapmap = verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_ADD, getParaToInt("unit.id"), AioConstants.WAY_GET, null, privilegeMoney);
      if (mapmap != null)
      {
        renderJson(mapmap);
        return;
      }
      int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
      Integer draftBillId = bill.getInt("id");
      bill.set("id", null);
      
      boolean hasOther = GetMoneyBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
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
      BigDecimal canAssignMoney = bill.getBigDecimal("canAssignMoney");
      if (BigDecimalUtils.compare(canAssignMoney, BigDecimal.ZERO) == -1)
      {
        setAttr("message", "结算金额超过可分配金额，不允保存!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), GetMoneyDetail.class, "getMoneylDetail");
      List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
      List<HelpUtil> helpUit2lList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "orderUitl2");
      

      boolean isOrderGetMoney = false;
      if ((helpUit2lList != null) && (helpUit2lList.size() > 0)) {
        isOrderGetMoney = true;
      }
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
      if (isOrderGetMoney)
      {
        boolean flag = false;
        String trIndexs = "";
        for (int i = 0; i < helpUit2lList.size(); i++)
        {
          HelpUtil h = (HelpUtil)helpUit2lList.get(i);
          if ((BigDecimalUtils.compare(h.getSettlementAmount(), BigDecimal.ZERO) != 0) && (BigDecimalUtils.compare(h.getSettlementAmount(), h.getLastMoney()) == 1))
          {
            trIndexs = trIndexs + "," + h.getTrIndex();
            flag = true;
          }
        }
        if (flag)
        {
          Map<String, Object> map = new HashMap();
          map.put("statusCode", AioConstants.HTTP_RETURN200);
          map.put("formId", "cw_getMoneyForm");
          if (!trIndexs.equals("")) {
            trIndexs = trIndexs.substring(1, trIndexs.length());
          }
          map.put("title", "第" + trIndexs + "行单据结算金额超出未结算金额，是否保存!");
          renderJson(map);
          return;
        }
      }
      bill.set("unitId", Integer.valueOf(unitId));
      bill.set("staffId", getParaToInt("staff.id"));
      bill.set("departmentId", getParaToInt("department.id"));
      bill.set("status", Integer.valueOf(AioConstants.BILL_STATUS3));
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
      Date time = new Date();
      bill.set("updateTime", time);
      
      bill.set("userId", Integer.valueOf(loginUserId()));
      if (draftId != 0) {
        billCodeIncrease(bill, "draftPost");
      } else {
        billCodeIncrease(bill, "add");
      }
      bill.save(configName);
      ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
      

      BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
      for (int i = 0; i < detailList.size(); i++)
      {
        Model detail = (Model)detailList.get(i);
        detail.set("id", null);
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        
        detail.save(configName);
      }
      if (isOrderGetMoney) {
        for (int i = 0; i < helpUit2lList.size(); i++)
        {
          HelpUtil h = (HelpUtil)helpUit2lList.get(i);
          if (BigDecimalUtils.compare(h.getSettlementAmount(), BigDecimal.ZERO) != 0)
          {
            PayMoneyToOrder model = new PayMoneyToOrder();
            model.set("unitId", Integer.valueOf(unitId));
            model.set("type", Integer.valueOf(AioConstants.GET_MONEY));
            model.set("billTypeId", Integer.valueOf(h.getOrderType()));
            model.set("billId", Integer.valueOf(h.getId()));
            model.set("cwBillTypeId", Integer.valueOf(billTypeId));
            model.set("cwBillId", bill.getInt("id"));
            model.set("lastMoney", h.getLastMoney());
            model.set("settlementAmount", h.getSettlementAmount());
            model.set("updateTime", bill.getDate("updateTime"));
            model.save(configName);
          }
        }
      }
      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, unitId, null, bill.getBigDecimal("privilegeMoney"));
      

      ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), null, bill, null, Integer.valueOf(unitId), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), bill.getBigDecimal("privilegeMoney"), null);
      

      Map<String, Object> record = new HashMap();
      record.put("accountList", detailList);
      record.put("needGetOrPay", bill.getBigDecimal("privilegeMoney"));
      record.put("loginUserId", Integer.valueOf(loginUserId()));
      PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), Integer.valueOf(unitId), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), bill.getBigDecimal("privilege"), null, null, record);
      
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      if (draftId != 0)
      {
        BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
        GetMoneyDraftBill.dao.deleteById(configName, draftBillId);
        GetMoneyDraftDetail.dao.delByBill(configName, draftBillId);
        PayMoneyToDraftOrder.dao.delUnitOrders(configName, billTypeId, draftBillId.intValue());
        
        PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(billTypeId));
        
        setDraftStrs();
        setAttr("message", "过账成功！");
        setAttr("callbackType", "closeCurrent");
        setAttr("navTabId", "businessDraftView");
      }
      else
      {
        setAttr("navTabId", "cw_getMoneyView");
      }
      if (!SysConfig.getConfig(configName, 9).booleanValue()) {
        setAttr("isClose", Boolean.valueOf(true));
      }
      printBeforSave1();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    renderJson();
  }
  
  @Before({Tx.class})
  public void reLoadCountSave()
    throws SQLException
  {
    String configName = loginConfigName();
    try
    {
      int billId = getParaToInt("bill.id").intValue();
      BigDecimal canAssignMoney = new BigDecimal(getPara("canAssignMoney"));
      if (BigDecimalUtils.compare(canAssignMoney, BigDecimal.ZERO) == -1)
      {
        setAttr("message", "结算金额超过可分配金额，不允保存!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      List<HelpUtil> helpUit2lList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "orderUitl2");
      
      boolean flag = false;
      String trIndexs = "";
      for (int i = 0; i < helpUit2lList.size(); i++)
      {
        HelpUtil h = (HelpUtil)helpUit2lList.get(i);
        if ((BigDecimalUtils.compare(h.getSettlementAmount(), BigDecimal.ZERO) != 0) && (BigDecimalUtils.compare(h.getSettlementAmount(), h.getLastMoney()) == 1))
        {
          trIndexs = trIndexs + "," + h.getTrIndex();
          flag = true;
        }
      }
      if (flag)
      {
        Map<String, Object> map = new HashMap();
        map.put("statusCode", AioConstants.HTTP_RETURN200);
        map.put("formId", "cw_getMoneyForm");
        if (!trIndexs.equals("")) {
          trIndexs = trIndexs.substring(1, trIndexs.length());
        }
        map.put("title", "第" + trIndexs + "行单据结算金额超出未结算金额，是否保存!");
        renderJson(map);
        return;
      }
      GetMoneyBill bill = (GetMoneyBill)GetMoneyBill.dao.findById(configName, Integer.valueOf(billId));
      int unitId = bill.getInt("unitId").intValue();
      bill.set("canAssignMoney", canAssignMoney);
      
      bill.update(configName);
      for (int i = 0; i < helpUit2lList.size(); i++)
      {
        HelpUtil h = (HelpUtil)helpUit2lList.get(i);
        if (BigDecimalUtils.compare(h.getSettlementAmount(), BigDecimal.ZERO) != 0)
        {
          PayMoneyToOrder model = new PayMoneyToOrder();
          model.set("unitId", Integer.valueOf(unitId));
          model.set("type", Integer.valueOf(AioConstants.GET_MONEY));
          model.set("billTypeId", Integer.valueOf(h.getOrderType()));
          model.set("billId", Integer.valueOf(h.getId()));
          model.set("cwBillTypeId", Integer.valueOf(billTypeId));
          model.set("cwBillId", bill.getInt("id"));
          model.set("settlementAmount", h.getSettlementAmount());
          model.set("updateTime", bill.getDate("updateTime"));
          model.save(configName);
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "重新结算成功");
    setAttr("callbackType", "closeCurrent");
    renderJson();
  }
  
  public void look()
  {
    int orderId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    boolean isRCW = false;
    
    Model bill = GetMoneyBill.dao.findById(configName, Integer.valueOf(orderId));
    Integer orderIsRedRow = bill.getInt("isRCW");
    if (orderIsRedRow == null) {
      orderIsRedRow = Integer.valueOf(0);
    }
    List<Model> detailList = GetMoneyDetail.dao.getList2(configName, Integer.valueOf(orderId), "cw_getmoney_detail");
    
    detailList = addTrSize(detailList, 15);
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    if (orderIsRedRow.intValue() == AioConstants.RCW_VS) {
      isRCW = true;
    } else {
      isRCW = false;
    }
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    if (AioConstants.IS_FREE_VERSION == "yes") {
      setAttr("settlementType", "money");
    } else {
      setAttr("settlementType", "order");
    }
    if ("order".equals("order"))
    {
      List<Record> orderList = GetMoneyBill.dao.getOrderByUnitLook(configName, billTypeId, orderId, bill.getInt("unitId").intValue());
      setAttr("orderList", orderList);
      int detailLen = 5;
      if (orderList != null) {
        if (orderList.size() > 5) {
          detailLen = 0;
        } else {
          detailLen = 5 - orderList.size();
        }
      }
      setAttr("detailLen", Integer.valueOf(detailLen));
    }
    setAttr("isRCW", Boolean.valueOf(isRCW));
    setAttr("tableId", Integer.valueOf(billTypeId));
    render("/WEB-INF/template/finance/getMoney/look.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, int billId)
    throws SQLException
  {
    Map<String, Object> map = new HashMap();
    int oldBillId = 0;
    int newBillId = 0;
    


    Model bill = GetMoneyBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "收款单已经不存在！");
      return map;
    }
    BigDecimal privilegeMoney = bill.getBigDecimal("privilegeMoney");
    Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unit.id"), AioConstants.WAY_GET, null, privilegeMoney);
    if (vmap != null)
    {
      map = vmap;
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      return map;
    }
    List<Model> dateilList = GetMoneyDetail.dao.getList2(configName, Integer.valueOf(billId), "cw_getmoney_detail");
    if ((dateilList == null) || (dateilList.size() == 0))
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "收款单会计科目信息已经不存在！");
      return map;
    }
    Date time = new Date();
    try
    {
      BigDecimal settlementAmount = GetMoneyBill.dao.getCanAssignMoneyAndDel(configName, "cw_pay_by_order", bill.getInt("unitId").intValue(), billTypeId, bill.getInt("id").intValue());
      
      BigDecimal canAssignMoney = bill.getBigDecimal("canAssignMoney");
      canAssignMoney = BigDecimalUtils.add(settlementAmount, canAssignMoney);
      bill.set("canAssignMoney", canAssignMoney);
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
      oldBillId = bill.getInt("id").intValue();
      bill.update(configName);
      bill.set("id", null);
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
      bill.set("updateTime", time);
      
      billRcwAddRemark(bill, null);
      bill.save(configName);
      newBillId = bill.getInt("id").intValue();
      
      BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
      for (int i = 0; i < dateilList.size(); i++)
      {
        Model detail = (Model)dateilList.get(i);
        
        detail.set("rcwId", detail.getInt("id"));
        detail.set("id", null);
        detail.set("billId", bill.getInt("id"));
        detail.save(configName);
      }
      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), null, bill.getBigDecimal("privilegeMoney"));
      

      ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
      

      PayTypeController.rcwAccountsRecoder(configName, billTypeId, Integer.valueOf(oldBillId), Integer.valueOf(newBillId));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack(configName);
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "系统异常,请联系管理员！");
      return map;
    }
    map.put("status", Integer.valueOf(AioConstants.RCW_BY));
    return map;
  }
  
  public void print()
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "收款单");
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
      bill = GetMoneyBill.dao.findById(configName, getParaToInt("bill.id", Integer.valueOf(0)));
      detailList = GetMoneyDetail.dao.getList2(configName, bill.getInt("id"), "cw_getmoney_detail");
      billId = bill.getInt("id");
    }
    else
    {
      bill = (Model)getModel(GetMoneyBill.class);
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("staffId", getParaToInt("staff.id", Integer.valueOf(0)));
      bill.set("departmentId", getParaToInt("department.id", Integer.valueOf(0)));
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), GetMoneyDetail.class, "getMoneylDetail");
    }
    billData.put("tableName", "cw_getmoney_detail");
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
    headData.add("录单日期:" + trimRecordDateNull(bill.getDate("recodeDate")));
    headData.add("单据编号:" + trimNull(bill.getStr("code")));
    headData.add("经手人 :" + trimNull(staffFullName));
    headData.add("部门 :" + trimNull(departmentName));
    headData.add("摘要:" + trimNull(bill.getStr("remark")));
    headData.add("附加说明 :" + trimNull(bill.getStr("memo")));
    headData.add("优惠金额 :" + trimNull(bill.getBigDecimal("privilege")));
    
    setOrderUserId(flag, bill, actionType, headData);
    headData.add("付款单位 :" + trimNull(unitFullName));
    headData.add("单位编号:" + trimNull(unit == null ? "" : unit.getStr("code")));
    headData.add("单位简名:" + trimNull(unit == null ? "" : unit.getStr("smallName")));
    headData.add("单位税号:" + trimNull(unit == null ? "" : unit.getStr("tariff")));
    headData.add("单位地址:" + trimNull(unit == null ? "" : unit.getStr("address")));
    headData.add("单位电话:" + trimNull(unit == null ? "" : unit.getStr("phone")));
    headData.add("单位手机一:" + trimNull(unit == null ? "" : unit.getStr("mobile1")));
    headData.add("开户银行:" + trimNull(unit == null ? "" : unit.getStr("bank")));
    headData.add("银行账号:" + trimNull(unit == null ? "" : unit.getStr("bankAccount")));
    headData.add("联系人一:" + trimNull(unit == null ? "" : unit.getStr("contact1")));
    headData.add("邮政:" + trimNull(unit == null ? "" : unit.getStr("zipCode")));
    headData.add("传真号:" + trimNull(unit == null ? "" : unit.getStr("fax")));
    
    BigDecimal totalGet = BigDecimal.ZERO;
    BigDecimal totalPay = BigDecimal.ZERO;
    if (unit != null)
    {
      totalGet = unit.getBigDecimal("totalGet");
      totalPay = unit.getBigDecimal("totalPay");
    }
    boolean hasUnitPermitted = hasPermitted("6-103-104-617-s");
    BigDecimal moneys = bill.getBigDecimal("moneys");
    if (actionType.equals("look"))
    {
      if (BigDecimalUtils.compare(BigDecimalUtils.sub(totalGet, moneys), BigDecimal.ZERO) > -1)
      {
        headData.add("此前应收款:" + trimNull(BigDecimalUtils.sub(totalGet, moneys), hasUnitPermitted));
        headData.add("此前应付款:" + trimNull("0", hasUnitPermitted));
      }
      else if (BigDecimalUtils.compare(totalGet, BigDecimal.ZERO) > 0)
      {
        headData.add("此前应收款:" + trimNull("0", hasUnitPermitted));
        headData.add("此前应付款:" + trimNull(BigDecimalUtils.sub(moneys, totalGet), hasUnitPermitted));
      }
      else
      {
        headData.add("此前应收款:" + trimNull("0", hasUnitPermitted));
        headData.add("此前应付款:" + trimNull(BigDecimalUtils.add(totalPay, moneys), hasUnitPermitted));
      }
      headData.add("累积应收款:" + trimNull(totalGet, hasUnitPermitted));
    }
    else
    {
      headData.add("此前应收款:" + trimNull(totalGet, hasUnitPermitted));
      headData.add("此前应付款:" + trimNull(totalPay, hasUnitPermitted));
      if (BigDecimalUtils.compare(BigDecimalUtils.sub(totalGet, moneys), BigDecimal.ZERO) > -1) {
        headData.add("累积应收款:" + trimNull(BigDecimalUtils.sub(totalGet, moneys), hasUnitPermitted));
      } else {
        headData.add("累积应收款:" + trimNull("0", hasUnitPermitted));
      }
    }
    headData.add("结算金额:" + trimNull(BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), bill.getBigDecimal("canAssignMoney"))));
    headData.add("可分配金额:" + trimNull(bill.getBigDecimal("canAssignMoney")));
    headData.add("合计金额:" + trimNull(bill.getBigDecimal("privilegeMoney")));
    

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
