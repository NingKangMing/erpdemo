package com.aioerp.model.finance.getmoney;

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

public class GetMoneyBill
  extends BaseDbModel
{
  public static final GetMoneyBill dao = new GetMoneyBill();
  public static final String TABLE_NAME = "cw_getmoney_bill";
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
    return Db.use(configName).queryLong("select count(*) from cw_getmoney_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public List<Record> getOrderByUnitLook(String configName, int cwBillTypeId, int cwBillId, int unitId)
  {
    StringBuffer sb = new StringBuffer("");
    

    sb.append("select " + AioConstants.BILL_ROW_TYPE_1 + " as billTypeId,c.settlementAmount,'期初应收' navTabTitle,0 id, '本年期初应收' code,null recodeDate,concat(x.fullName,'本年期初应收') remark,x.beginGetMoney taxMoneys,0 privilege,0 payMoney");
    sb.append("  from cw_pay_by_order c left join b_unit x on x.id=c.unitId where c.unitId=" + unitId + " and c.billTypeId=" + AioConstants.BILL_ROW_TYPE_1 + " and c.cwBillTypeId=" + cwBillTypeId + " and c.cwBillId=" + cwBillId);
    sb.append(" union all ");
    

    sb.append("select c.billTypeId,c.settlementAmount,'销售单详情' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.taxMoneys,x.privilege,x.payMoney");
    sb.append(getOrderByUnitLook1(cwBillTypeId, cwBillId, AioConstants.BILL_ROW_TYPE4, "xs_sell_bill", unitId));
    sb.append(" union all ");
    
    sb.append("select c.billTypeId,c.settlementAmount,'销售退货详情' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.taxMoneys*-1,x.privilege*-1,x.payMoney*-1");
    sb.append(getOrderByUnitLook1(cwBillTypeId, cwBillId, AioConstants.BILL_ROW_TYPE7, "xs_return_bill", unitId));
    sb.append(" union all ");
    
    sb.append("select c.billTypeId,c.settlementAmount,'销售换货详情' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.gapMoney as taxMoneys,x.privilege,x.payMoney");
    sb.append(getOrderByUnitLook1(cwBillTypeId, cwBillId, AioConstants.BILL_ROW_TYPE13, "xs_barter_bill", unitId));
    sb.append(" union all ");
    
    sb.append("select c.billTypeId,c.settlementAmount,'其它收入单' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.moneys as taxMoneys,0 as privilege,0 as payMoney");
    sb.append(getOrderByUnitLook1(cwBillTypeId, cwBillId, AioConstants.BILL_ROW_TYPE10, "cc_otherin_bill", unitId));
    List<Record> list = Db.use(configName).find(sb.toString());
    return list;
  }
  
  private String getOrderByUnitLook1(int cwBillTypeId, int cwBillId, int billTypeId, String tableName, int unitId)
  {
    StringBuffer sb = new StringBuffer();
    sb.append(" from cw_pay_by_order c left join " + tableName + " x on x.id=c.billId where x.unitId=" + unitId + " and c.billTypeId=" + billTypeId + " and cwBillTypeId=" + cwBillTypeId + " and cwBillId=" + cwBillId);
    return sb.toString();
  }
  
  public List<Record> getOrderByUnit(String configName, int unitId)
  {
    StringBuffer sb = new StringBuffer("select * from (");
    
    sb.append("select " + AioConstants.BILL_ROW_TYPE_1 + " as billTypeId,'期初应收' navTabTitle,0 id, '本年期初应收' code,null recodeDate,concat(x.fullName,'本年期初应收') remark,x.beginGetMoney taxMoneys,0 privilege,0 payMoney,");
    sb.append("(IFNULL(x.beginGetMoney,0)-IFNULL((select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE_1 + " ),0) ) lastMoney,");
    sb.append("(select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE_1 + ") settlementAmount");
    sb.append(" from b_unit x where x.id=" + unitId);
    sb.append(" HAVING lastMoney != 0");
    
    sb.append(" union all ");
    


    sb.append("select " + AioConstants.BILL_ROW_TYPE4 + " as billTypeId,'销售单详情' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.taxMoneys,x.privilege,x.payMoney,");
    sb.append("(IFNULL(x.taxMoneys,0)-IFNULL(x.privilege,0)-IFNULL(x.payMoney,0)-IFNULL((select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE4 + " and billId=x.id),0) ) lastMoney,");
    sb.append("(select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE4 + " and billId=x.id) settlementAmount");
    sb.append(" from xs_sell_bill x where x.unitId=" + unitId + " and isRCW=" + AioConstants.RCW_NO);
    sb.append(" HAVING lastMoney != 0");
    
    sb.append(" union all ");
    


    sb.append("select " + AioConstants.BILL_ROW_TYPE7 + " as billTypeId,'销售退货详情' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.taxMoneys*-1,x.privilege*-1,x.payMoney*-1,");
    sb.append("(IFNULL(x.taxMoneys*-1,0)-IFNULL(x.privilege*-1,0)-IFNULL(x.payMoney*-1,0)-IFNULL((select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE7 + " and billId=x.id),0) ) lastMoney,");
    sb.append("(select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE7 + " and billId=x.id) settlementAmount");
    sb.append(" from xs_return_bill x where x.unitId=" + unitId + " and isRCW=" + AioConstants.RCW_NO);
    sb.append(" HAVING lastMoney != 0");
    
    sb.append(" union all ");
    

    sb.append("select " + AioConstants.BILL_ROW_TYPE13 + " as billTypeId,'销售换货详情' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.gapMoney as taxMoneys,x.privilege,x.payMoney,");
    sb.append("(IFNULL(x.gapMoney,0)-IFNULL(x.privilege,0)-IFNULL(x.payMoney,0)-IFNULL((select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE13 + " and billId=x.id),0) ) lastMoney,");
    sb.append("(select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE13 + " and billId=x.id) settlementAmount");
    sb.append(" from xs_barter_bill x where x.unitId=" + unitId + " and isRCW=" + AioConstants.RCW_NO);
    sb.append(" HAVING lastMoney != 0");
    
    sb.append(" union all ");
    

    sb.append("select " + AioConstants.BILL_ROW_TYPE18 + " as billTypeId,'其它收入单' navTabTitle,x.id,x.code,x.recodeDate,x.remark,x.moneys as taxMoneys,0 as privilege,x.getMoney as payMoney,");
    sb.append("(IFNULL(x.moneys,0)-IFNULL((select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE18 + " and billId=x.id),0) ) lastMoney,");
    sb.append("(select sum(settlementAmount) from cw_pay_by_order where unitId=" + unitId + " and billTypeId=" + AioConstants.BILL_ROW_TYPE18 + " and billId=x.id) settlementAmount");
    sb.append(" from cw_otherearn_bill x where x.unitId=" + unitId + " and isRCW=" + AioConstants.RCW_NO);
    sb.append(" HAVING lastMoney != 0");
    
    sb.append(") temp order by recodeDate asc");
    List<Record> list = Db.use(configName).find(sb.toString());
    return list;
  }
  
  public BigDecimal getCanAssignMoneyAndDel(String configName, String tableName, int unitId, int cwBillTypeId, int cwBillId)
  {
    Record r = Db.use(configName).findFirst("select sum(settlementAmount) settlementAmount from " + tableName + " where unitId=" + unitId + " and cwBillTypeId=" + cwBillTypeId + " and cwBillId=" + cwBillId);
    BigDecimal settlementAmount = BigDecimal.ZERO;
    if ((r != null) && (BigDecimalUtils.compare(r.getBigDecimal("settlementAmount"), BigDecimal.ZERO) != 0)) {
      settlementAmount = r.getBigDecimal("settlementAmount");
    }
    deleteBeforeCwOrder(configName, tableName, false, unitId, cwBillTypeId, cwBillId);
    return settlementAmount;
  }
  
  public void deleteBeforeCwOrder(String configName, String tableName, boolean isDraft, int unitId, int cwBillTypeId, int cwBillId)
  {
    Record r = null;
    String initBillTable = "";
    if (cwBillTypeId == AioConstants.BILL_ROW_TYPE17)
    {
      if (isDraft) {
        initBillTable = "cw_getmoney_draft_bill";
      } else {
        initBillTable = "cw_getmoney_bill";
      }
    }
    else if (cwBillTypeId == AioConstants.BILL_ROW_TYPE19) {
      if (isDraft) {
        initBillTable = "cw_paymoney_draft_bill";
      } else {
        initBillTable = "cw_paymoney_bill";
      }
    }
    r = Db.use(configName).findById(initBillTable, Integer.valueOf(cwBillId));
    

    Record rr = Db.use(configName).findFirst("select sum(settlementAmount) settlementAmounts from " + tableName + " where unitId=" + unitId + " and cwBillTypeId=" + cwBillTypeId + " and cwBillId=" + cwBillId);
    BigDecimal settlementAmounts = BigDecimal.ZERO;
    if (rr != null) {
      settlementAmounts = rr.getBigDecimal("settlementAmounts");
    }
    Db.use(configName).update("delete from " + tableName + " where unitId=" + unitId + " and cwBillTypeId=" + cwBillTypeId + " and cwBillId=" + cwBillId);
    if (r != null)
    {
      BigDecimal canAssignMoney = r.getBigDecimal("canAssignMoney");
      canAssignMoney = BigDecimalUtils.add(canAssignMoney, settlementAmounts);
    }
    Db.use(configName).update(initBillTable, r);
  }
}
