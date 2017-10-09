package com.aioerp.controller.stock.other;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.stock.other.OtherOutDetail;
import com.aioerp.model.stock.other.OtherOutDraftBill;
import com.aioerp.model.stock.other.OtherOutDraftDetail;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OtherOutDraftController
  extends BaseController
{
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    OtherOutDraftBill bill = (OtherOutDraftBill)getModel(OtherOutDraftBill.class, "otherOutBill");
    










    List<Model> detail = ModelUtils.batchInjectSortObjModel(getRequest(), OtherOutDraftDetail.class, "otherOutDetail");
    if (detail.size() == 0)
    {
      setAttr("message", "请选择要商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    int accountId = getParaToInt("account.id", Integer.valueOf(0)).intValue();
    bill.set("accountsId", Integer.valueOf(accountId));
    bill.set("unitId", Integer.valueOf(unitId));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("storageId", Integer.valueOf(storageId));
    


    Date time = new Date();
    bill.set("userId", Integer.valueOf(loginUserId()));
    bill.set("updateTime", time);
    

    billCodeIncrease(bill, "draftAdd");
    bill.save(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE11, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE11, bill, bill.getBigDecimal("moneys"));
    for (int i = 0; i < detail.size(); i++)
    {
      Model sellDetail1 = (Model)detail.get(i);
      sellDetail1.set("billId", bill.getInt("id"));
      sellDetail1.set("updateTime", time);
      
      sellDetail1.save(configName);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "stockotherout");
    renderJson();
  }
  
  public void toEditDraft()
  {
    String configName = loginConfigName();
    setDraftAutoPost();
    Integer id = getParaToInt(0, Integer.valueOf(0));
    Integer draftId = getParaToInt(1, Integer.valueOf(0));
    OtherOutDraftBill bill = (OtherOutDraftBill)OtherOutDraftBill.dao.findById(configName, id);
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = OtherOutDetail.dao.getList2(configName, id, "cc_otherout_draft_detail");
    
    setAttr("accountTypeId", "36");
    

    detailList = addTrSize(detailList, 15);
    
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE11, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE11, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE11));
    setAttr("draftId", draftId);
    
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    notEditStaff();
    render("/WEB-INF/template/stock/other/otherout/draft/edit.html");
  }
  
  @Before({Tx.class})
  public void edit()
    throws SQLException
  {
    String configName = loginConfigName();
    OtherOutDraftBill bill = (OtherOutDraftBill)getModel(OtherOutDraftBill.class, "otherOutBill");
    
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    
    boolean falg = editDraftVerify(Integer.valueOf(draftId)).booleanValue();
    if (falg) {
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), OtherOutDraftDetail.class, "otherOutDetail");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer storageId = getParaToInt("storage.id");
    
    bill.set("unitId", getParaToInt("unit.id"));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("storageId", storageId);
    Date time = new Date();
    bill.set("updateTime", time);
    

    billCodeIncrease(bill, "drafEdit");
    bill.update(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE11, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    




    BusinessDraftController.updateBillDraft(configName, draftId, bill, AioConstants.BILL_ROW_TYPE11, bill.getBigDecimal("moneys"));
    List<Integer> detailIds = new ArrayList();
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Integer detailId = detail.getInt("id");
      Integer productId = detail.getInt("productId");
      if (productId != null)
      {
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        if (detailId == null) {
          detail.save(configName);
        } else {
          detail.update(configName);
        }
      }
      detailIds.add(detail.getInt("id"));
    }
    delTableDetailIdsByBillId("cc_otherout_draft_detail", bill.getInt("id"), detailIds);
    
    PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(AioConstants.BILL_ROW_TYPE11));
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
