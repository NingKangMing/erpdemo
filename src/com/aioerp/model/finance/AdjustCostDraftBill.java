package com.aioerp.model.finance;

import com.aioerp.bean.FlowBill;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;

public class AdjustCostDraftBill
  extends BaseDbModel
{
  public static final AdjustCostDraftBill dao = new AdjustCostDraftBill();
  public static final String TABLE_NAME = "cc_adjust_cost_draft_bill";
  private Staff billStaff;
  private Storage billStorage;
  private Department billDepartment;
  
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
  
  public Storage getBillStorage(String configName)
  {
    if (this.billStorage == null) {
      this.billStorage = ((Storage)Storage.dao.findById(configName, get("storageId")));
    }
    return this.billStorage;
  }
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from cc_adjust_cost_draft_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public FlowBill outPublic(Record bill, FlowBill fBill)
  {
    fBill.setInStorageId(bill.getInt("storageId"));
    fBill.setAmounts(bill.getBigDecimal("amounts"));
    fBill.setMoneys(bill.getBigDecimal("moneys"));
    fBill.setDiscountMoneys(bill.getBigDecimal("moneys"));
    fBill.setAdjustMoneys(bill.getBigDecimal("adjustMoneys"));
    fBill.setLastMoneys(bill.getBigDecimal("lastMoneys"));
    return fBill;
  }
  
  public Record inPublic(Record bill, FlowBill fBill)
  {
    bill.set("staffId", fBill.getStaffId());
    bill.set("departmentId", fBill.getDepartmentId());
    bill.set("remark", fBill.getRemark());
    bill.set("memo", fBill.getMemo());
    bill.set("moneys", fBill.getMoneys());
    if (fBill.getIsInSck().intValue() == -1) {
      bill.set("storageId", fBill.getOutStorageId());
    } else {
      bill.set("storageId", fBill.getInStorageId());
    }
    bill.set("adjustMoneys", fBill.getAdjustMoneys());
    bill.set("lastMoneys", fBill.getLastMoneys());
    return bill;
  }
}
