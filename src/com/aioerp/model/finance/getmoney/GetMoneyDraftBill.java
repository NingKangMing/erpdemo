package com.aioerp.model.finance.getmoney;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.util.List;

public class GetMoneyDraftBill
  extends BaseDbModel
{
  public static final GetMoneyDraftBill dao = new GetMoneyDraftBill();
  public static final String TABLE_NAME = "cw_getmoney_draft_bill";
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
    return Db.use(configName).queryLong("select count(*) from cw_getmoney_draft_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public List<Record> readOrderByUnit(String configName, int cwBillTypeId, int cwBillId)
  {
    StringBuffer sb = new StringBuffer("");
    
    sb.append("select " + AioConstants.BILL_ROW_TYPE_1 + " orderType,0 oldBillId,'本年期初应收' code,null recodeDate,concat(x.fullName,'本年期初应收') remark,x.beginGetMoney taxMoneys,c.billTypeId,c.settlementAmount,c.lastMoney");
    sb.append(" from cw_pay_by_draft_order c left join b_unit x on c.unitId=x.id where x.id>0 and c.cwBillTypeId=" + cwBillTypeId + " and c.cwBillId=" + cwBillId + " and c.billTypeId=" + AioConstants.BILL_ROW_TYPE_1);
    sb.append(" union all ");
    
    sb.append("select " + AioConstants.BILL_ROW_TYPE4 + " orderType,x.id oldBillId,x.code,x.recodeDate,x.remark,x.taxMoneys,c.billTypeId,c.settlementAmount,c.lastMoney");
    sb.append(" from cw_pay_by_draft_order c left join xs_sell_bill x on c.billId=x.id where x.id>0 and c.cwBillTypeId=" + cwBillTypeId + " and c.cwBillId=" + cwBillId);
    sb.append(" union all ");
    
    sb.append("select " + AioConstants.BILL_ROW_TYPE7 + " orderType,x.id oldBillId,x.code,x.recodeDate,x.remark,x.taxMoneys,c.billTypeId,c.settlementAmount,c.lastMoney");
    sb.append(" from cw_pay_by_draft_order c left join xs_return_bill x on c.billId=x.id where x.id>0 and c.cwBillTypeId=" + cwBillTypeId + " and c.cwBillId=" + cwBillId);
    sb.append(" union all ");
    
    sb.append("select " + AioConstants.BILL_ROW_TYPE13 + " orderType,x.id oldBillId,x.code,x.recodeDate,x.remark,x.gapMoney as taxMoneys,c.billTypeId,c.settlementAmount,c.lastMoney");
    sb.append(" from cw_pay_by_draft_order c left join xs_barter_bill x on c.billId=x.id where x.id>0 and c.cwBillTypeId=" + cwBillTypeId + " and c.cwBillId=" + cwBillId);
    sb.append(" union all ");
    
    sb.append("select " + AioConstants.BILL_ROW_TYPE10 + " orderType,x.id oldBillId,x.code,x.recodeDate,x.remark,x.moneys as taxMoneys,c.billTypeId,c.settlementAmount,c.lastMoney");
    sb.append(" from cw_pay_by_draft_order c left join cc_otherin_bill x on c.billId=x.id where x.id>0 and c.cwBillTypeId=" + cwBillTypeId + " and c.cwBillId=" + cwBillId);
    

    return Db.use(configName).find(sb.toString());
  }
}
