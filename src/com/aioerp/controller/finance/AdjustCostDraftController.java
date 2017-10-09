package com.aioerp.controller.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.finance.AdjustCostDraftBill;
import com.aioerp.model.finance.AdjustCostDraftDetail;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DwzUtils;
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

public class AdjustCostDraftController
  extends BaseController
{
  @Before({Tx.class})
  public void add()
  {
    String configName = loginConfigName();
    AdjustCostDraftBill bill = (AdjustCostDraftBill)getModel(AdjustCostDraftBill.class, "adjustCostBill");
    
    boolean hasOther = AdjustCostDraftBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("message", "编号已经存在!");
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
    List<AdjustCostDraftDetail> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), AdjustCostDraftDetail.class, "adjustCostDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      AdjustCostDraftDetail detail = (AdjustCostDraftDetail)detailList.get(i);
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1);
      Integer productId = detail.getInt("productId");
      if (productId == null)
      {
        setAttr("message", "第" + trIndex + "行商品录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    Integer storageId = getParaToInt("storage.id");
    
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    
    bill.set("storageId", storageId);
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    bill.save(configName);
    

    BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE20, bill, bill.getBigDecimal("lastMoneys"));
    for (int i = 0; i < detailList.size(); i++)
    {
      AdjustCostDraftDetail detail = (AdjustCostDraftDetail)detailList.get(i);
      Integer productId = detail.getInt("productId");
      BigDecimal amount = detail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        Integer selectUnitId = detail.getInt("selectUnitId");
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        BigDecimal lastPrice = detail.getBigDecimal("lastPrice");
        BigDecimal basePrice = DwzUtils.getConversionPrice(lastPrice, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        detail.set("baseAmount", baseAmount);
        detail.set("basePrice", basePrice);
        
        detail.save(configName);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "adjustCostView");
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE20, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    renderJson();
  }
  
  public void toEdit()
  {
    String configName = loginConfigName();
    Integer id = getParaToInt(0, Integer.valueOf(0));
    Integer draftId = getParaToInt(1, Integer.valueOf(0));
    AdjustCostDraftBill bill = (AdjustCostDraftBill)AdjustCostDraftBill.dao.findById(configName, id);
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = AdjustCostDraftDetail.dao.getListByBillId(configName, id);
    detailList = addTrSize(detailList, 15);
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE20, AioConstants.STATUS_ENABLE));
    
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE20));
    setAttr("draftId", draftId);
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    notEditStaff();
    setDraftAutoPost();
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE20, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    render("edit.html");
  }
  
  public void edit()
  {
    AdjustCostDraftBill bill = (AdjustCostDraftBill)getModel(AdjustCostDraftBill.class, "adjustCostBill");
    String configName = loginConfigName();
    boolean hasOther = AdjustCostDraftBill.dao.codeIsExist(configName, bill.getStr("code"), bill.getInt("id").intValue());
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
    List<AdjustCostDraftDetail> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), AdjustCostDraftDetail.class, "adjustCostDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      AdjustCostDraftDetail detail = (AdjustCostDraftDetail)detailList.get(i);
      
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1);
      Integer productId = detail.getInt("productId");
      if (productId == null)
      {
        setAttr("message", "第" + trIndex + "行商品录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    
    bill.set("storageId", getParaToInt("storage.id"));
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("userId", Integer.valueOf(loginUserId()));
    bill.update(configName);
    

    BusinessDraftController.updateBillDraft(configName, draftId, bill, AioConstants.BILL_ROW_TYPE20, bill.getBigDecimal("lastMoneys"));
    List<Integer> detailIds = new ArrayList();
    for (int i = 0; i < detailList.size(); i++)
    {
      AdjustCostDraftDetail detail = (AdjustCostDraftDetail)detailList.get(i);
      Integer detailId = detail.getInt("id");
      Integer productId = detail.getInt("productId");
      BigDecimal amount = detail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        Integer selectUnitId = detail.getInt("selectUnitId");
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        BigDecimal lastPrice = detail.getBigDecimal("lastPrice");
        BigDecimal basePrice = DwzUtils.getConversionPrice(lastPrice, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        detail.set("baseAmount", baseAmount);
        detail.set("basePrice", basePrice);
        if (detailId == null) {
          detail.save(configName);
        } else {
          detail.update(configName);
        }
      }
      detailIds.add(detail.getInt("id"));
    }
    AdjustCostDraftDetail.dao.delOthers(configName, bill.getInt("id"), detailIds);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE20, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
