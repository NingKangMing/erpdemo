package com.aioerp.controller.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.controller.reports.finance.arap.ArapRecordsController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.finance.PayMoneyBill;
import com.aioerp.model.finance.PayMoneyDetail;
import com.aioerp.model.finance.PayMoneyDraftBill;
import com.aioerp.model.finance.PayMoneyDraftDetail;
import com.aioerp.model.finance.PayMoneyToDraftOrder;
import com.aioerp.model.finance.PayMoneyToOrder;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PayMoneyController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE19;
  
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
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    Integer draftId = Integer.valueOf(0);
    try
    {
      PayMoneyBill bill = (PayMoneyBill)getModel(PayMoneyBill.class);
      

      Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
      if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(rmap);
        return;
      }
      Integer billId = bill.getInt("id");
      if (billId == null) {
        billId = Integer.valueOf(0);
      }
      String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
      Date recodeDate = bill.getTimestamp("recodeDate");
      if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
      {
        setAttr("message", "录单日期必须和当前日期一致!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
      boolean hasOther = PayMoneyBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
      if (("1".equals(codeAllowRep)) && (
        (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
      {
        setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
        setAttr("billTypeId", Integer.valueOf(billTypeId));
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      Integer unitId = getParaToInt("unit.id", Integer.valueOf(0));
      if ((unitId == null) || (unitId.intValue() == 0))
      {
        setAttr("message", "请选收款单位!");
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
      List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), PayMoneyDetail.class, "payMoneylDetail");
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
          setAttr("message", "第" + trIndex + "行科目金额录入错误!");
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
          map.put("formId", "cw_payMoneyForm");
          if (!trIndexs.equals("")) {
            trIndexs = trIndexs.substring(1, trIndexs.length());
          }
          map.put("title", "第" + trIndexs + "行单据结算金额超出未结算金额，是否保存!");
          renderJson(map);
          return;
        }
      }
      BigDecimal privilegeMoney = bill.getBigDecimal("privilegeMoney");
      Map<String, Object> map = verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_ADD, unitId, AioConstants.WAY_PAY, null, privilegeMoney);
      if (map != null)
      {
        renderJson(map);
        return;
      }
      String normalCode = getPara("billCode");
      if (!normalCode.equals(bill.getStr("code"))) {
        bill.set("codeIncrease", Integer.valueOf(-1));
      }
      bill.set("unitId", unitId);
      bill.set("staffId", getParaToInt("staff.id"));
      bill.set("departmentId", getParaToInt("department.id"));
      bill.set("status", Integer.valueOf(AioConstants.BILL_STATUS3));
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
      Date time = new Date();
      bill.set("updateTime", time);
      bill.set("userId", Integer.valueOf(loginUserId()));
      if (bill.get("id") == null)
      {
        bill.save(configName);
      }
      else
      {
        bill.remove("id");
        if (bill.getInt("codeIncrease").intValue() != -1)
        {
          String codeCreateRule = AioerpSys.dao.getValue1(configName, "codeCreateRule");
          if ("2".equals(codeCreateRule))
          {
            int codeIncrease = bill.getInt("codeIncrease").intValue();
            bill.set("codeIncrease", Integer.valueOf(codeIncrease + 1));
          }
        }
        bill.save(configName);
        
        draftId = getParaToInt("draftId");
        BusinessDraft.dao.deleteById(configName, draftId);
        
        PayMoneyDraftBill.dao.deleteById(configName, billId);
        PayMoneyDraftDetail.dao.delDeail(configName, billId);
        
        PayMoneyToDraftOrder.dao.delUnitOrders(configName, billTypeId, billId.intValue());
        
        PayDraft.dao.delete(configName, billId, Integer.valueOf(billTypeId));
      }
      ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
      
      BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
      for (int i = 0; i < detailList.size(); i++)
      {
        Model detail = (Model)detailList.get(i);
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        if (detail.get("id") != null) {
          detail.remove("id");
        }
        detail.save(configName);
      }
      if (isOrderGetMoney) {
        for (int i = 0; i < helpUit2lList.size(); i++)
        {
          HelpUtil h = (HelpUtil)helpUit2lList.get(i);
          if (BigDecimalUtils.compare(h.getSettlementAmount(), BigDecimal.ZERO) != 0)
          {
            PayMoneyToOrder model = new PayMoneyToOrder();
            model.set("unitId", unitId);
            model.set("type", Integer.valueOf(AioConstants.OUT_MONEY));
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
      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, unitId.intValue(), null, bill.getBigDecimal("privilegeMoney"));
      

      ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), bill.getBigDecimal("privilegeMoney"), null);
      


      Map<String, Object> record = new HashMap();
      record.put("accountList", detailList);
      record.put("needGetOrPay", bill.getBigDecimal("privilegeMoney"));
      record.put("loginUserId", Integer.valueOf(loginUserId()));
      PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), bill.getBigDecimal("privilege"), null, null, record);
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
    if (draftId.intValue() > 0)
    {
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
      setDraftStrs();
    }
    else
    {
      setAttr("navTabId", "cw_payMoneyView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    printBeforSave1();
    
    renderJson();
  }
  
  public void updateDraft()
  {
    String configName = loginConfigName();
    Integer draftId = getParaToInt("draftId");
    

    boolean falg = editDraftVerify(draftId).booleanValue();
    if (falg) {
      return;
    }
    PayMoneyDraftBill bill = (PayMoneyDraftBill)getModel(PayMoneyDraftBill.class, "payMoneyBill");
    Integer billId = bill.getInt("id");
    if (billId == null) {
      billId = Integer.valueOf(0);
    }
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    Date recodeDate = bill.getTimestamp("recodeDate");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), PayMoneyDraftDetail.class, "payMoneylDetail");
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
    Integer unitId = getParaToInt("unit.id");
    if ((unitId == null) || (unitId.intValue() == 0))
    {
      setAttr("message", "请选择收款单位!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    String normalCode = getPara("billCode");
    if (!normalCode.equals(bill.getStr("code"))) {
      bill.set("codeIncrease", Integer.valueOf(-1));
    }
    bill.set("unitId", unitId);
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("status", Integer.valueOf(AioConstants.BILL_STATUS3));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (draftId != null)
    {
      bill.update(configName);
      
      BusinessDraftController.updateBillDraft(configName, draftId.intValue(), bill, billTypeId, bill.getBigDecimal("moneys"));
      
      PayDraft.dao.delete(configName, billId, Integer.valueOf(billTypeId));
    }
    else
    {
      if (bill.getInt("codeIncrease").intValue() != -1)
      {
        String codeCreateRule = AioerpSys.dao.getValue1(configName, "codeCreateRule");
        if ("2".equals(codeCreateRule))
        {
          int codeIncrease = bill.getInt("codeIncrease").intValue();
          bill.set("codeIncrease", Integer.valueOf(codeIncrease - 1));
        }
      }
      bill.save(configName);
      BusinessDraftController.saveBillDraft(configName, billTypeId, bill, bill.getBigDecimal("moneys"));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    List<Integer> oldDetailIds = PayMoneyDraftDetail.dao.getListByDetailId(configName, billId);
    
    List<Integer> newDetaiIds = new ArrayList();
    Model detail;
    for (int i = 0; i < detailList.size(); i++)
    {
      detail = (Model)detailList.get(i);
      Integer detailId = detail.getInt("id");
      detail.set("billId", bill.getInt("id"));
      detail.set("updateTime", time);
      if (detailId == null)
      {
        detail.save(configName);
      }
      else
      {
        detail.update(configName);
        newDetaiIds.add(detailId);
      }
      PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), billTypeId, billId.intValue(), unitId, detail.getInt("accountsId"), detail.getBigDecimal("money"), time);
    }
    for (Integer id : oldDetailIds) {
      if (!newDetaiIds.contains(id)) {
        PayMoneyDraftDetail.dao.deleteById(configName, id);
      }
    }
    if (isOrderGetMoney)
    {
      PayMoneyToDraftOrder.dao.delUnitOrders(configName, billTypeId, bill.getInt("id").intValue());
      for (int i = 0; i < helpUit2lList.size(); i++)
      {
        HelpUtil h = (HelpUtil)helpUit2lList.get(i);
        if (BigDecimalUtils.compare(h.getSettlementAmount(), BigDecimal.ZERO) != 0)
        {
          PayMoneyToDraftOrder model = new PayMoneyToDraftOrder();
          model.set("unitId", unitId);
          model.set("type", Integer.valueOf(AioConstants.OUT_MONEY));
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
    PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE0), billTypeId, billId.intValue(), bill.getInt("unitId"), Integer.valueOf(42), bill.getBigDecimal("privilege"), time);
    

    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != null)
    {
      setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "cw_payMoneyView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
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
      int billId = getParaToInt("billId").intValue();
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
        map.put("formId", "cw_payMoneyForm");
        if (!trIndexs.equals("")) {
          trIndexs = trIndexs.substring(1, trIndexs.length());
        }
        map.put("title", "第" + trIndexs + "行单据结算金额超出未结算金额，是否保存!");
        renderJson(map);
        return;
      }
      PayMoneyBill bill = (PayMoneyBill)PayMoneyBill.dao.findById(configName, Integer.valueOf(billId));
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
          model.set("type", Integer.valueOf(AioConstants.OUT_MONEY));
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
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    renderJson();
  }
  
  public void look()
  {
    int orderId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    boolean isRCW = false;
    
    Model bill = PayMoneyBill.dao.findById(configName, Integer.valueOf(orderId));
    Integer orderIsRedRow = bill.getInt("isRCW");
    if (orderIsRedRow == null) {
      orderIsRedRow = Integer.valueOf(0);
    }
    List<Model> detailList = PayMoneyDetail.dao.getList(configName, Integer.valueOf(orderId));
    
    detailList = addTrSize(detailList, 15);
    
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    if (orderIsRedRow.intValue() == AioConstants.RCW_VS) {
      isRCW = true;
    } else {
      isRCW = false;
    }
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
      List<Record> orderList = PayMoneyBill.dao.getOrderByUnitLook(configName, billTypeId, orderId, bill.getInt("unitId").intValue());
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
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    
    render("look.html");
  }
  
  public void toEditDraft()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int sourceId = getParaToInt(1, Integer.valueOf(0)).intValue();
    Model bill = PayMoneyDraftBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = PayMoneyDraftDetail.dao.getList(configName, Integer.valueOf(billId));
    
    detailList = addTrSize(detailList, 15);
    
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("settlementType", "order");
    if ("order".equals("order"))
    {
      List<Record> orderList = PayMoneyDraftBill.dao.getOrderByUnitLook(configName, billTypeId, billId, bill.getInt("unitId").intValue());
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
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("draftId", Integer.valueOf(sourceId));
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    

    notEditStaff();
    setDraftAutoPost();
    render("editDraft.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, int billId)
    throws SQLException
  {
    Map<String, Object> map = new HashMap();
    

    Model bill = PayMoneyBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "收款单已经不存在！");
      return map;
    }
    List<Model> dateilList = PayMoneyDetail.dao.getList(configName, Integer.valueOf(billId));
    if ((dateilList == null) || (dateilList.size() == 0))
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "付款单会计科目信息已经不存在！");
      return map;
    }
    BigDecimal privilegeMoney = bill.getBigDecimal("privilegeMoney");
    Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unitId"), AioConstants.WAY_PAY, null, privilegeMoney);
    if (vmap != null)
    {
      map = vmap;
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      return map;
    }
    Date time = new Date();
    try
    {
      BigDecimal settlementAmount = PayMoneyBill.dao.getCanAssignMoneyAndDel(configName, bill.getInt("unitId").intValue(), billTypeId, bill.getInt("id").intValue());
      
      BigDecimal canAssignMoney = bill.getBigDecimal("canAssignMoney");
      canAssignMoney = BigDecimalUtils.add(settlementAmount, canAssignMoney);
      bill.set("canAssignMoney", canAssignMoney);
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
      bill.update(configName);
      bill.set("id", null);
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
      bill.set("updateTime", time);
      
      billRcwAddRemark(bill, null);
      bill.save(configName);
      
      BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
      for (int i = 0; i < dateilList.size(); i++)
      {
        Model detail = (Model)dateilList.get(i);
        
        detail.set("rcwId", detail.getInt("id"));
        detail.set("id", null);
        detail.set("billId", bill.getInt("id"));
        detail.save(configName);
      }
      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, bill.getBigDecimal("privilegeMoney"));
      
      ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
      
      PayTypeController.rcwAccountsRecoder(configName, billTypeId, Integer.valueOf(billId), bill.getInt("id"));
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
    throws ParseException
  {
    Integer billId = getParaToInt("billId");
    Integer editId = getParaToInt("payMoneyBill.id");
    String configName = loginConfigName();
    
    SysConfig.getConfig(configName, 12).booleanValue();
    

    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "付款单");
    List<String> headData = new ArrayList();
    
    String recodeDate = DateUtils.format(getParaToDate("payMoneyBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    
    headData.add("单据编号:" + getPara("payMoneyBill.code", ""));
    headData.add("收款单位 :" + getPara("unit.fullName", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门:" + getPara("department.fullName", ""));
    headData.add("摘要:" + getPara("payMoneyBill.remark", ""));
    headData.add("附加说明:" + getPara("payMoneyBill.memo", ""));
    headData.add("优惠金额:" + getPara("payMoneyBill.privilege", ""));
    Model bill = null;
    if ((billId != null) && (billId.intValue() != 0)) {
      bill = PayMoneyBill.dao.findById(configName, billId);
    } else if ((editId != null) && (editId.intValue() != 0)) {
      bill = PayMoneyDraftBill.dao.findById(configName, editId);
    }
    if (bill != null) {
      setBillUser(headData, bill.getInt("userId"));
    } else {
      setBillUser(headData, null);
    }
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), PayMoneyDetail.class, "payMoneylDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailList = PayMoneyDetail.dao.getList(configName, billId);
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      int accountsId = detail.getInt("accountsId").intValue();
      Accounts account = (Accounts)Accounts.dao.findById(configName, Integer.valueOf(accountsId));
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(account.getStr("code")));
      rowData.add(trimNull(account.getStr("fullName")));
      rowData.add(trimNull(account.getStr("smallName")));
      rowData.add(trimNull(account.getStr("spell")));
      rowData.add(trimNull(account.getStr("memo")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.getStr("memo")));
      rowData.add("");
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
