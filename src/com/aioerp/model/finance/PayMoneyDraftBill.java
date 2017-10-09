package com.aioerp.model.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.util.List;

public class PayMoneyDraftBill
  extends BaseDbModel
{
  public static final PayMoneyDraftBill dao = new PayMoneyDraftBill();
  public static final String TABLE_NAME = "cw_paymoney_draft_bill";
  private Unit billUnit;
  private Staff billStaff;
  private Department billDepartment;
  
  public Unit getBillUnit(String configName)
  {
    if (this.billUnit == null) {
      this.billUnit = ((Unit)Unit.dao.findById(configName, get("unitId")));
    }
    return this.billUnit;
  }
  
  public Staff getBillStaff(String configName)
  {
    if (this.billStaff == null) {
      this.billStaff = ((Staff)Staff.dao.findById(configName, get("staffId")));
    }
    return this.billStaff;
  }
  
  public Department getBillDepartment(String configName)
  {
    if (this.billDepartment == null) {
      this.billDepartment = ((Department)Department.dao.findById(configName, get("departmentId")));
    }
    return this.billDepartment;
  }
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from cw_paymoney_draft_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public List<Record> getOrderByUnitLook(String configName, int cwBillTypeId, int cwBillId, int unitId)
  {
    StringBuffer sb = new StringBuffer("");
    

    sb.append("select " + AioConstants.BILL_ROW_TYPE_2 + " as billTypeId,c.settlementAmount,'期初应付' navTabTitle,0 id, '本年期初应付' code,null recodeDate,concat(x.fullName,'本年期初应收') remark,x.beginPayMoney taxMoneys,0 privilege,0 payMoney");
    sb.append("  from cw_pay_by_draft_order c left join b_unit x on x.id=c.unitId where c.unitId=" + unitId + " and c.billTypeId=" + AioConstants.BILL_ROW_TYPE_2 + " and c.cwBillTypeId=" + cwBillTypeId + " and c.cwBillId=" + cwBillId);
    sb.append(" union all ");
    

    sb.append("select c.billTypeId,c.settlementAmount,'进货单查看' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.taxMoneys,x.privilege,x.payMoney");
    sb.append(getOrderByUnitLook1(cwBillTypeId, cwBillId, AioConstants.BILL_ROW_TYPE5, "cg_purchase_bill", unitId));
    sb.append(" union all ");
    
    sb.append("select c.billTypeId,c.settlementAmount,'进货退货查看' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.taxMoneys*-1,x.privilege*-1,x.payMoney*-1");
    sb.append(getOrderByUnitLook1(cwBillTypeId, cwBillId, AioConstants.BILL_ROW_TYPE6, "cg_return_bill", unitId));
    sb.append(" union all ");
    
    sb.append("select c.billTypeId,c.settlementAmount,'进货换货查看' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.gapMoney as taxMoneys,x.privilege,x.payMoney");
    sb.append(getOrderByUnitLook1(cwBillTypeId, cwBillId, AioConstants.BILL_ROW_TYPE12, "cg_barter_bill", unitId));
    sb.append(" union all ");
    

    sb.append("select c.billTypeId,c.settlementAmount,'费用单查看' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.moneys as taxMoneys,0 as privilege,0 as payMoney");
    sb.append(getOrderByUnitLook1(cwBillTypeId, cwBillId, AioConstants.BILL_ROW_TYPE22, "cw_fee_bill", unitId));
    
    List<Record> list = Db.use(configName).find(sb.toString());
    return list;
  }
  
  private String getOrderByUnitLook1(int cwBillTypeId, int cwBillId, int billTypeId, String tableName, int unitId)
  {
    StringBuffer sb = new StringBuffer();
    sb.append(" from cw_pay_by_draft_order c left join " + tableName + " x on x.id=c.billId where x.unitId=" + unitId + " and c.billTypeId=" + billTypeId + " and cwBillTypeId=" + cwBillTypeId + " and cwBillId=" + cwBillId);
    return sb.toString();
  }
  
  public List<Record> getOrderByUnit(String configName, int unitId, boolean isReload)
  {
    StringBuffer sb = new StringBuffer("");
    

    sb.append("select " + AioConstants.BILL_ROW_TYPE_2 + " as billTypeId,'期初应付' navTabTitle,0 id, '本年期初应付' code,null recodeDate,concat(x.fullName,'本年期初应付') remark,x.beginPayMoney taxMoneys,0 privilege,0 payMoney,");
    sb.append("(IFNULL(x.beginPayMoney,0)-IFNULL((select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE_2 + " ),0) ) lastMoney,");
    sb.append("(select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE_2 + ") settlementAmount");
    sb.append(" from b_unit x where x.id=" + unitId);
    sb.append(" HAVING lastMoney != 0");
    
    sb.append(" union all ");
    
    sb.append("select * from (");
    sb.append("select " + AioConstants.BILL_ROW_TYPE5 + " as billTypeId,'进货单详情' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.taxMoneys,x.privilege,x.payMoney,");
    sb.append("(IFNULL(x.taxMoneys,0)-IFNULL(x.privilege,0)-IFNULL(x.payMoney,0)-IFNULL((select sum(settlementAmount) from cw_pay_by_draft_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE5 + " and billId=x.id),0) ) lastMoney,");
    sb.append("(select sum(settlementAmount) from cw_pay_by_draft_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE5 + " and billId=x.id) settlementAmount");
    sb.append(" from cg_purchase_bill x where x.unitId=" + unitId + " and isRCW=" + AioConstants.RCW_NO);
    sb.append(" HAVING lastMoney != 0");
    
    sb.append(" union all ");
    
    sb.append("select " + AioConstants.BILL_ROW_TYPE6 + " as billTypeId,'进货退货详情' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.taxMoneys,x.privilege,x.payMoney,");
    sb.append("(IFNULL(x.taxMoneys,0)-IFNULL(x.privilege,0)-IFNULL(x.payMoney,0)-IFNULL((SELECT SUM(settlementAmount) FROM cw_pay_by_draft_order WHERE unitId=" + unitId + " AND billTypeId=" + AioConstants.BILL_ROW_TYPE6 + " AND billId=x.id),0)  ) lastMoney,");
    sb.append("(select sum(settlementAmount) from cw_pay_by_draft_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE6 + " and billId=x.id) settlementAmount");
    sb.append(" from cg_return_bill x where x.unitId=" + unitId + " and isRCW=" + AioConstants.RCW_NO);
    sb.append(" HAVING lastMoney != 0");
    
    sb.append(" union all ");
    
    sb.append("select " + AioConstants.BILL_ROW_TYPE12 + " as billTypeId,'进货换货详情' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.gapMoney as taxMoneys,x.privilege,x.payMoney,");
    sb.append("(IFNULL(x.gapMoney,0)--IFNULL(x.privilege,0)-IFNULL(x.payMoney,0)-IFNULL((SELECT SUM(settlementAmount) FROM cw_pay_by_draft_order WHERE unitId=" + unitId + " AND billTypeId=" + AioConstants.BILL_ROW_TYPE12 + " AND billId=x.id),0)  ) lastMoney,");
    sb.append("(select sum(settlementAmount) from cw_pay_by_draft_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE12 + " and billId=x.id) settlementAmount");
    sb.append(" from cg_barter_bill x where x.unitId=" + unitId + " and isRCW=" + AioConstants.RCW_NO);
    sb.append(" HAVING lastMoney != 0");
    

    sb.append("select " + AioConstants.BILL_ROW_TYPE22 + " as billTypeId,'费用单' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.moneys as taxMoneys,0 as privilege,x.getMoney as payMoney,");
    sb.append("(IFNULL(x.moneys,0)-IFNULL((select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE22 + " and billId=x.id),0) ) lastMoney,");
    sb.append("(select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE22 + " and billId=x.id) settlementAmount");
    sb.append(" from cw_fee_bill x where x.unitId=" + unitId + " and isRCW=" + AioConstants.RCW_NO);
    sb.append(" HAVING lastMoney != 0");
    
    sb.append(") temp order by recodeDate asc");
    List<Record> list = Db.use(configName).find(sb.toString());
    return list;
  }
  
  public BigDecimal getCanAssignMoneyAndDel(String configName, int unitId, int cwBillTypeId, int cwBillId)
  {
    Record r = Db.use(configName).findFirst("select sum(settlementAmount) settlementAmount from cw_pay_by_draft_order where unitId=" + unitId + " and cwBillTypeId=" + cwBillTypeId + " and cwBillId=" + cwBillId);
    BigDecimal settlementAmount = BigDecimal.ZERO;
    if ((r != null) && (BigDecimalUtils.compare(r.getBigDecimal("settlementAmount"), BigDecimal.ZERO) != 0)) {
      settlementAmount = r.getBigDecimal("settlementAmount");
    }
    deleteBeforeCwOrder(configName, unitId, cwBillTypeId, cwBillId);
    return settlementAmount;
  }
  
  public void deleteBeforeCwOrder(String configName, int unitId, int cwBillTypeId, int cwBillId)
  {
    Db.use(configName).update("delete from cw_pay_by_draft_order where unitId=" + unitId + " and cwBillTypeId=" + cwBillTypeId + " and cwBillId=" + cwBillId);
  }
}
