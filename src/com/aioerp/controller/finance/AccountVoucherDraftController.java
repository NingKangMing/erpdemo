package com.aioerp.controller.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.finance.AccountVoucherDraftBill;
import com.aioerp.model.finance.AccountVoucherDraftDetail;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
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

public class AccountVoucherDraftController
  extends BaseController
{
  @Before({Tx.class})
  public void add()
  {
    String configName = loginConfigName();
    AccountVoucherDraftBill bill = (AccountVoucherDraftBill)getModel(AccountVoucherDraftBill.class, "accountVoucherBill");
    

    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    








    Date recodeDate = bill.getTimestamp("recodeDate");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<AccountVoucherDraftDetail> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), AccountVoucherDraftDetail.class, "accountVoucherDetail");
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
      AccountVoucherDraftDetail detail = (AccountVoucherDraftDetail)detailList.get(i);
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
    
    billCodeIncrease(bill, "draftAdd");
    bill.save(configName);
    
    BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE32, bill, bill.getBigDecimal("lendMoneys"));
    for (int i = 0; i < detailList.size(); i++)
    {
      AccountVoucherDraftDetail detail = (AccountVoucherDraftDetail)detailList.get(i);
      detail.set("billId", bill.getInt("id"));
      detail.set("updateTime", time);
      detail.save(configName);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "accountVoucherView");
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE32, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    renderJson();
  }
  
  public void toEdit()
  {
    String configName = loginConfigName();
    Integer id = getParaToInt(0, Integer.valueOf(0));
    Integer draftId = getParaToInt(1, Integer.valueOf(0));
    AccountVoucherDraftBill bill = (AccountVoucherDraftBill)AccountVoucherDraftBill.dao.findById(configName, id);
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = AccountVoucherDraftDetail.dao.getListByBillId(configName, id);
    detailList = addTrSize(detailList, 15);
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE32, AioConstants.STATUS_ENABLE));
    
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE32));
    setAttr("draftId", draftId);
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    notEditStaff();
    setDraftAutoPost();
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE32, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    render("edit.html");
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String configName = loginConfigName();
    AccountVoucherDraftBill bill = (AccountVoucherDraftBill)getModel(AccountVoucherDraftBill.class, "accountVoucherBill");
    
    boolean hasOther = AccountVoucherDraftBill.dao.codeIsExist(configName, bill.getStr("code"), bill.getInt("id").intValue());
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    
    boolean falg = editDraftVerify(Integer.valueOf(draftId)).booleanValue();
    if (falg) {
      return;
    }
    if ((hasOther) || (StringUtils.isBlank(bill.getStr("code"))))
    {
      setAttr("message", "编号已经存在!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<AccountVoucherDraftDetail> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), AccountVoucherDraftDetail.class, "accountVoucherDetail");
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
      AccountVoucherDraftDetail detail = (AccountVoucherDraftDetail)detailList.get(i);
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
    
    billCodeIncrease(bill, "drafEdit");
    bill.update(configName);
    

    BusinessDraftController.updateBillDraft(configName, draftId, bill, AioConstants.BILL_ROW_TYPE32, bill.getBigDecimal("lendMoneys"));
    List<Integer> detailIds = new ArrayList();
    for (int i = 0; i < detailList.size(); i++)
    {
      AccountVoucherDraftDetail detail = (AccountVoucherDraftDetail)detailList.get(i);
      detail.set("billId", bill.getInt("id"));
      Integer detailId = detail.getInt("id");
      detail.set("updateTime", time);
      if (detailId == null) {
        detail.save(configName);
      } else {
        detail.update(configName);
      }
      detailIds.add(detail.getInt("id"));
    }
    AccountVoucherDraftDetail.dao.delOthers(configName, bill.getInt("id"), detailIds);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE32, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
