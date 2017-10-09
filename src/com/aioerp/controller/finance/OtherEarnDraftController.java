package com.aioerp.controller.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.finance.OtherEarnDraftBill;
import com.aioerp.model.finance.OtherEarnDraftDetail;
import com.aioerp.model.finance.PayDraft;
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

public class OtherEarnDraftController
  extends BaseController
{
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    OtherEarnDraftBill bill = (OtherEarnDraftBill)getModel(OtherEarnDraftBill.class, "otherEarnBill");
    
    Date time = new Date();
    bill.set("updateTime", time);
    

    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    








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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), OtherEarnDraftDetail.class, "otherEarnDetail");
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
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    
    billCodeIncrease(bill, "draftAdd");
    bill.save(configName);
    
    BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE18, bill, bill.getBigDecimal("moneys"));
    for (int i = 0; i < detailList.size(); i++)
    {
      OtherEarnDraftDetail detail = (OtherEarnDraftDetail)detailList.get(i);
      detail.set("billId", bill.getInt("id"));
      detail.set("updateTime", time);
      detail.save(configName);
    }
    if (StringUtils.isNotBlank(payTypeIds))
    {
      String[] accounts = payTypeIds.split(",");
      String[] payMoneys = payTypeMoneys.split(",");
      for (int i = 0; i < accounts.length; i++)
      {
        BigDecimal payMoneyi = new BigDecimal(payMoneys[i]);
        PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE1), AioConstants.BILL_ROW_TYPE18, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(accounts[i]), payMoneyi, time);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "otherEarnView");
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE18, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    renderJson();
  }
  
  public void toEdit()
  {
    String configName = loginConfigName();
    Integer id = getParaToInt(0, Integer.valueOf(0));
    Integer draftId = getParaToInt(1, Integer.valueOf(0));
    OtherEarnDraftBill bill = (OtherEarnDraftBill)OtherEarnDraftBill.dao.findById(configName, id);
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = OtherEarnDraftDetail.dao.getListByBillId(configName, id);
    detailList = addTrSize(detailList, 15);
    setPayTypeAttr(configName, "draftBill", AioConstants.BILL_ROW_TYPE18, id.intValue());
    
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE18, AioConstants.STATUS_ENABLE));
    
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE18));
    setAttr("draftId", draftId);
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    notEditStaff();
    setDraftAutoPost();
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE18, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    render("edit.html");
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String configName = loginConfigName();
    OtherEarnDraftBill bill = (OtherEarnDraftBill)getModel(OtherEarnDraftBill.class, "otherEarnBill");
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    
    boolean falg = editDraftVerify(Integer.valueOf(draftId)).booleanValue();
    if (falg) {
      return;
    }
    Date time = new Date();
    bill.set("updateTime", time);
    
    boolean hasOther = OtherEarnDraftBill.dao.codeIsExist(configName, bill.getStr("code"), bill.getInt("id").intValue());
    if ((hasOther) || (StringUtils.isBlank(bill.getStr("code"))))
    {
      setAttr("message", "编号已经存在!");
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), OtherEarnDraftDetail.class, "otherEarnDetail");
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
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    
    billCodeIncrease(bill, "drafEdit");
    bill.update(configName);
    

    BusinessDraftController.updateBillDraft(configName, draftId, bill, AioConstants.BILL_ROW_TYPE18, bill.getBigDecimal("moneys"));
    List<Integer> detailIds = new ArrayList();
    for (int i = 0; i < detailList.size(); i++)
    {
      OtherEarnDraftDetail detail = (OtherEarnDraftDetail)detailList.get(i);
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
    OtherEarnDraftDetail.dao.delOthers(configName, bill.getInt("id"), detailIds);
    
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(AioConstants.BILL_ROW_TYPE18));
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    if (StringUtils.isNotBlank(payTypeIds))
    {
      String[] accounts = payTypeIds.split(",");
      String[] payMoneys = payTypeMoneys.split(",");
      for (int i = 0; i < accounts.length; i++)
      {
        BigDecimal payMoneyi = new BigDecimal(payMoneys[i]);
        PayDraft.dao.addPayRecords(configName, Integer.valueOf(AioConstants.PAY_TYLE1), AioConstants.BILL_ROW_TYPE18, bill.getInt("id").intValue(), bill.getInt("unitId"), Integer.valueOf(accounts[i]), payMoneyi, time);
      }
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE18, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
