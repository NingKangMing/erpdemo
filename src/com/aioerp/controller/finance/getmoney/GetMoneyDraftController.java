package com.aioerp.controller.finance.getmoney;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.finance.PayMoneyToDraftOrder;
import com.aioerp.model.finance.getmoney.GetMoneyDetail;
import com.aioerp.model.finance.getmoney.GetMoneyDraftBill;
import com.aioerp.model.finance.getmoney.GetMoneyDraftDetail;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GetMoneyDraftController
  extends BaseController
{
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    try
    {
      GetMoneyDraftBill bill = (GetMoneyDraftBill)getModel(GetMoneyDraftBill.class, "getMoneyBill");
      










      List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), GetMoneyDraftDetail.class, "getMoneylDetail");
      List<HelpUtil> helpUit2lList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "orderUitl2");
      

      boolean isOrderGetMoney = false;
      if ((helpUit2lList != null) && (helpUit2lList.size() > 0)) {
        isOrderGetMoney = true;
      }
      if (detailList.size() == 0)
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN200);
        setAttr("navTabId", "cw_getMoneyView");
        renderJson();
        return;
      }
      int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
      bill.set("unitId", Integer.valueOf(unitId));
      bill.set("staffId", getParaToInt("staff.id"));
      bill.set("departmentId", getParaToInt("department.id"));
      bill.set("status", Integer.valueOf(AioConstants.BILL_STATUS3));
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
      Date time = new Date();
      bill.set("updateTime", time);
      bill.set("userId", Integer.valueOf(loginUserId()));
      

      billCodeIncrease(bill, "draftAdd");
      bill.save(configName);
      ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE17, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
      

      BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE17, bill, bill.getBigDecimal("privilegeMoney"));
      for (int i = 0; i < detailList.size(); i++)
      {
        Model detail = (Model)detailList.get(i);
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        
        detail.save(configName);
        
        addPayRecords(configName, null, AioConstants.BILL_ROW_TYPE4, String.valueOf(detail.getInt("accountsId")), String.valueOf(detail.getBigDecimal("money")), bill.getInt("id").intValue(), bill.getInt("unitId"), time);
      }
      if (isOrderGetMoney) {
        for (int i = 0; i < helpUit2lList.size(); i++)
        {
          HelpUtil h = (HelpUtil)helpUit2lList.get(i);
          if (BigDecimalUtils.compare(h.getSettlementAmount(), BigDecimal.ZERO) != 0)
          {
            PayMoneyToDraftOrder model = new PayMoneyToDraftOrder();
            model.set("unitId", Integer.valueOf(unitId));
            model.set("type", Integer.valueOf(AioConstants.GET_MONEY));
            model.set("billTypeId", Integer.valueOf(h.getOrderType()));
            model.set("billId", Integer.valueOf(h.getId()));
            model.set("cwBillTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE17));
            model.set("cwBillId", bill.getInt("id"));
            model.set("lastMoney", h.getLastMoney());
            model.set("settlementAmount", h.getSettlementAmount());
            model.set("updateTime", bill.getDate("updateTime"));
            model.save(configName);
          }
        }
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("navTabId", "cw_getMoneyView");
      if (!SysConfig.getConfig(configName, 9).booleanValue()) {
        setAttr("isClose", Boolean.valueOf(true));
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
    renderJson();
  }
  
  public void toEditDraft()
  {
    String configName = loginConfigName();
    setDraftAutoPost();
    Integer id = getParaToInt(0, Integer.valueOf(0));
    Integer draftId = getParaToInt(1, Integer.valueOf(0));
    GetMoneyDraftBill bill = (GetMoneyDraftBill)GetMoneyDraftBill.dao.findById(configName, id);
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    List<Model> detailList = GetMoneyDetail.dao.getList2(configName, id, "cw_getmoney_draft_detail");
    
    detailList = addTrSize(detailList, 15);
    if ("order".equals("order"))
    {
      List<Record> orderList = GetMoneyDraftBill.dao.readOrderByUnit(configName, AioConstants.BILL_ROW_TYPE17, id.intValue());
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
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE17, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE17, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    
    setAttr("settlementType", "order");
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE17));
    setAttr("draftId", draftId);
    
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    notEditStaff();
    render("/WEB-INF/template/finance/getMoney/draft/edit.html");
  }
  
  @Before({Tx.class})
  public void edit()
    throws SQLException
  {
    String configName = loginConfigName();
    GetMoneyDraftBill bill = (GetMoneyDraftBill)getModel(GetMoneyDraftBill.class, "getMoneyBill");
    try
    {
      int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
      
      boolean falg = editDraftVerify(Integer.valueOf(draftId)).booleanValue();
      if (falg) {
        return;
      }
      List<GetMoneyDraftDetail> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), GetMoneyDraftDetail.class, "getMoneylDetail");
      List<HelpUtil> helpUit2lList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "orderUitl2");
      if (detailList.size() == 0)
      {
        setAttr("message", "请选择会计科目!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      bill.set("unitId", getParaToInt("unit.id"));
      bill.set("staffId", getParaToInt("staff.id"));
      bill.set("departmentId", getParaToInt("department.id"));
      Date time = new Date();
      bill.set("updateTime", time);
      
      billCodeIncrease(bill, "drafEdit");
      bill.update(configName);
      ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE17, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
      

      BusinessDraftController.updateBillDraft(configName, draftId, bill, AioConstants.BILL_ROW_TYPE17, bill.getBigDecimal("privilegeMoney"));
      
      PayDraft.dao.delete(configName, bill.getInt("id"), Integer.valueOf(AioConstants.BILL_ROW_TYPE17));
      List<Integer> detailIds = new ArrayList();
      for (int i = 0; i < detailList.size(); i++)
      {
        Model detail = (Model)detailList.get(i);
        Integer detailId = detail.getInt("id");
        Integer accountsId = detail.getInt("accountsId");
        if ((accountsId != null) && (accountsId.intValue() > 0))
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
        
        addPayRecords(configName, null, AioConstants.BILL_ROW_TYPE4, String.valueOf(detail.getInt("accountsId")), String.valueOf(detail.getBigDecimal("money")), bill.getInt("id").intValue(), bill.getInt("unitId"), time);
      }
      GetMoneyDraftDetail.dao.delOthers(configName, bill.getInt("id"), detailIds);
      
      boolean isOrderGetMoney = false;
      if ((helpUit2lList != null) && (helpUit2lList.size() > 0)) {
        isOrderGetMoney = true;
      }
      if (isOrderGetMoney)
      {
        PayMoneyToDraftOrder.dao.delUnitOrders(configName, AioConstants.BILL_ROW_TYPE17, bill.getInt("id").intValue());
        for (int i = 0; i < helpUit2lList.size(); i++)
        {
          HelpUtil h = (HelpUtil)helpUit2lList.get(i);
          if (BigDecimalUtils.compare(h.getSettlementAmount(), BigDecimal.ZERO) != 0)
          {
            PayMoneyToDraftOrder model = new PayMoneyToDraftOrder();
            model.set("unitId", bill.getInt("unitId"));
            model.set("type", Integer.valueOf(AioConstants.GET_MONEY));
            model.set("billTypeId", Integer.valueOf(h.getOrderType()));
            model.set("billId", Integer.valueOf(h.getId()));
            model.set("cwBillTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE17));
            model.set("cwBillId", bill.getInt("id"));
            model.set("lastMoney", h.getLastMoney());
            model.set("settlementAmount", h.getSettlementAmount());
            model.set("updateTime", bill.getDate("updateTime"));
            model.save(configName);
          }
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
    setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "businessDraftView");
    renderJson();
  }
}
