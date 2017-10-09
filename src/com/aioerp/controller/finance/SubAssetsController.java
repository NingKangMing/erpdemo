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
import com.aioerp.model.finance.assets.SubAssetsBill;
import com.aioerp.model.finance.assets.SubAssetsDetail;
import com.aioerp.model.finance.assets.SubAssetsDraftBill;
import com.aioerp.model.finance.assets.SubAssetsDraftDetail;
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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class SubAssetsController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE25;
  
  public void index()
  {
    billCodeAuto(billTypeId);
    
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(loginConfigName(), billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    

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
      SubAssetsBill bill = (SubAssetsBill)getModel(SubAssetsBill.class);
      
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
      boolean hasOther = SubAssetsBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
      
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
      Integer unitId = getParaToInt("unit.id", Integer.valueOf(0));
      if ((unitId == null) || (unitId.intValue() == 0))
      {
        setAttr("message", "请选收购单位!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), SubAssetsDetail.class, "subAssetsDetail");
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
          setAttr("message", "第" + trIndex + "行科目金额录入错误!");
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
      }
      Integer getAccountId = getParaToInt("account.id");
      if ((getAccountId == null) || (getAccountId.intValue() == 0)) {
        bill.set("getMoney", BigDecimal.ZERO);
      }
      BigDecimal moneys = bill.getBigDecimal("moneys");
      BigDecimal getMoney = bill.getBigDecimal("getMoney");
      Map<String, Object> map = verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_ADD, unitId, AioConstants.WAY_GET, getMoney, moneys);
      if (map != null)
      {
        renderJson(map);
        return;
      }
      Date time = new Date();
      
      bill.set("unitId", unitId);
      bill.set("staffId", getParaToInt("staff.id"));
      bill.set("departmentId", getParaToInt("department.id"));
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
      bill.set("updateTime", time);
      bill.set("getAccountId", getAccountId);
      bill.set("userId", Integer.valueOf(loginUserId()));
      

      String normalCode = getPara("billCode");
      if (!normalCode.equals(bill.getStr("code"))) {
        bill.set("codeIncrease", Integer.valueOf(-1));
      }
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
        
        SubAssetsDraftBill.dao.deleteById(configName, billId);
        SubAssetsDraftDetail.dao.delByBill(configName, billId);
        
        PayDraft.dao.delete(configName, billId, Integer.valueOf(billTypeId));
      }
      ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
      
      BillHistoryController.saveBillHistory(configName, bill, billTypeId, moneys);
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
      BigDecimal money = BigDecimalUtils.sub(moneys, getMoney);
      

      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, unitId.intValue(), null, money);
      

      ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE1, Integer.valueOf(billTypeId), null, bill, null, bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), money, getMoney);
      


      Map<String, Object> record = new HashMap();
      record.put("accountList", detailList);
      record.put("needGetMoney", money);
      record.put("getAccountId", bill.getInt("getAccountId"));
      record.put("getMoney", bill.getBigDecimal("getMoney"));
      record.put("loginUserId", Integer.valueOf(loginUserId()));
      PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, record);
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
      setAttr("navTabId", "subAssetsView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    printBeforSave1();
    
    renderJson();
  }
  
  @Before({Tx.class})
  public void updateDraft()
  {
    String configName = loginConfigName();
    Integer draftId = getParaToInt("draftId");
    
    boolean falg = editDraftVerify(draftId).booleanValue();
    if (falg) {
      return;
    }
    SubAssetsDraftBill bill = (SubAssetsDraftBill)getModel(SubAssetsDraftBill.class, "subAssetsBill");
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), SubAssetsDraftDetail.class, "subAssetsDetail");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择会计科目!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer unitId = getParaToInt("unit.id");
    Integer getAccountId = getParaToInt("account.id");
    String getAccountIds = null;
    if ((getAccountId == null) || (getAccountId.intValue() == 0)) {
      bill.set("getMoney", BigDecimal.ZERO);
    } else {
      getAccountIds = getAccountId.toString();
    }
    bill.set("unitId", unitId);
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("getAccountId", getAccountId);
    bill.set("userId", Integer.valueOf(loginUserId()));
    

    String normalCode = getPara("billCode");
    if (!normalCode.equals(bill.getStr("code"))) {
      bill.set("codeIncrease", Integer.valueOf(-1));
    }
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
    
    List<Integer> newDetaiIds = new ArrayList();
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Integer detailId = detail.getInt("id");
      detail.set("billId", bill.getInt("id"));
      detail.set("updateTime", time);
      if (detailId == null)
      {
        detail.save(configName);
        detailId = detail.getInt("id");
      }
      else
      {
        detail.update(configName);
      }
      newDetaiIds.add(detailId);
    }
    SubAssetsDraftDetail.dao.delOthers(configName, billId, newDetaiIds);
    

    addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE1), billTypeId, getAccountIds, bill.getBigDecimal("getMoney").toString(), bill.getInt("id").intValue(), bill.getInt("unitId"), time);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】保存成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int orderId = getParaToInt(0, Integer.valueOf(0)).intValue();
    boolean isRCW = false;
    Model bill = SubAssetsBill.dao.findById(configName, Integer.valueOf(orderId));
    Integer orderIsRedRow = bill.getInt("isRCW");
    if (orderIsRedRow == null) {
      orderIsRedRow = Integer.valueOf(0);
    }
    List<Model> detailList = SubAssetsDetail.dao.getListByBillId(configName, Integer.valueOf(orderId));
    
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
    Model bill = SubAssetsDraftBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = SubAssetsDraftDetail.dao.getListByBillId(configName, Integer.valueOf(billId));
    
    detailList = addTrSize(detailList, 15);
    
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("detailList", detailList);
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
    

    Model bill = SubAssetsBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "固定资产变卖单已经不存在！");
      return map;
    }
    List<Model> dateilList = SubAssetsDetail.dao.getListByBillId(configName, Integer.valueOf(billId));
    if ((dateilList == null) || (dateilList.size() == 0))
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "固定资产变卖单会计科目信息已经不存在！");
      return map;
    }
    BigDecimal moneys = bill.getBigDecimal("moneys");
    BigDecimal getMoney = bill.getBigDecimal("getMoney");
    Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unitId"), AioConstants.WAY_GET, getMoney, moneys);
    if (vmap != null)
    {
      map = vmap;
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      return map;
    }
    Date time = new Date();
    try
    {
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
      bill.update(configName);
      bill.remove("id");
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
      BigDecimal money = BigDecimalUtils.sub(bill.getBigDecimal("moneys"), bill.getBigDecimal("getMoney"));
      
      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE1, bill.getInt("unitId").intValue(), null, money);
      

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
  {
    Integer billId = getParaToInt("billId");
    Integer editId = getParaToInt("subAssetsBill.id");
    String configName = loginConfigName();
    

    SysConfig.getConfig(configName, 12).booleanValue();
    
    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "固定资产变卖单");
    List<String> headData = new ArrayList();
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String recodeDate = sdf.format(getParaToDate("subAssetsBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    headData.add("单据编号:" + getPara("subAssetsBill.code", ""));
    headData.add("收购单位 :" + getPara("unit.fullName", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门:" + getPara("department.fullName", ""));
    headData.add("摘要:" + getPara("subAssetsBill.remark", ""));
    headData.add("附加说明:" + getPara("subAssetsBill.memo", ""));
    headData.add("收款账户:" + getPara("account.fullName", ""));
    headData.add("实收金额:" + getPara("subAssetsBill.getMoney", ""));
    Model bill = null;
    if ((billId != null) && (billId.intValue() != 0)) {
      bill = SubAssetsBill.dao.findById(configName, billId);
    } else if ((editId != null) && (editId.intValue() != 0)) {
      bill = SubAssetsDraftBill.dao.findById(configName, editId);
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), SubAssetsDetail.class, "subAssetsDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailList = SubAssetsDetail.dao.getListByBillId(configName, billId);
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
      rowData.add(trimNull(detail.getStr("message")));
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
