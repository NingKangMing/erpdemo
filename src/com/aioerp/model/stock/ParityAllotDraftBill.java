package com.aioerp.model.stock;

import com.aioerp.bean.FlowBill;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;

public class ParityAllotDraftBill
  extends BaseDbModel
{
  public static final ParityAllotDraftBill dao = new ParityAllotDraftBill();
  public static final String TABLE_NAME = "cc_parityallot_draft_bill";
  private Staff billStaff;
  private Storage billOutStorage;
  private Storage billInStorage;
  private Department billDepartment;
  
  public Staff getBillStaff(String configName)
  {
    if (this.billStaff == null) {
      this.billStaff = ((Staff)Staff.dao.findById(configName, get("staffId")));
    }
    return this.billStaff;
  }
  
  public Storage getBillOutStorage(String configName)
  {
    if (this.billOutStorage == null) {
      this.billOutStorage = ((Storage)Storage.dao.findById(configName, get("outStorageId")));
    }
    return this.billOutStorage;
  }
  
  public Storage getBillInStorage(String configName)
  {
    if (this.billInStorage == null) {
      this.billInStorage = ((Storage)Storage.dao.findById(configName, get("inStorageId")));
    }
    return this.billInStorage;
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
    return Db.use(configName).queryLong("select count(*) from cc_parityallot_draft_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public FlowBill outPublic(Record bill, FlowBill fBill)
  {
    fBill.setInStorageId(bill.getInt("inStorageId"));
    fBill.setOutStorageId(bill.getInt("outStorageId"));
    fBill.setAmounts(bill.getBigDecimal("amounts"));
    fBill.setMoneys(bill.getBigDecimal("moneys"));
    return fBill;
  }
  
  public void inPublic(Record bill, FlowBill fBill)
  {
    bill.set("staffId", fBill.getStaffId());
    bill.set("departmentId", fBill.getDepartmentId());
    bill.set("remark", fBill.getRemark());
    bill.set("memo", fBill.getMemo());
    bill.set("moneys", fBill.getMoneys());
    bill.set("outStorageId", fBill.getOutStorageId());
    bill.set("inStorageId", fBill.getInStorageId());
  }
}