package com.aioerp.model.stock.other;

import com.aioerp.bean.FlowBill;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;

public class OtherInDraftBill
  extends BaseDbModel
{
  public static final OtherInDraftBill dao = new OtherInDraftBill();
  public static final String TABLE_NAME = "cc_otherin_draft_bill";
  private Unit billUnit;
  private Staff billStaff;
  private Storage billStorage;
  private Department billDepartment;
  private Accounts billAccounts;
  
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
  
  public Storage getBillStorage(String configName)
  {
    if (this.billStorage == null) {
      this.billStorage = ((Storage)Storage.dao.findById(configName, get("storageId")));
    }
    return this.billStorage;
  }
  
  public Department getBillDepartment(String configName)
  {
    if (this.billDepartment == null) {
      this.billDepartment = ((Department)Department.dao.findById(configName, get("departmentId")));
    }
    return this.billDepartment;
  }
  
  public Accounts getBillAccounts(String configName)
  {
    if (this.billAccounts == null) {
      this.billAccounts = ((Accounts)Accounts.dao.findById(configName, get("accountsId")));
    }
    return this.billAccounts;
  }
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from cc_otherin_draft_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public FlowBill outPublic(Record bill, FlowBill fBill)
  {
    fBill.setInStorageId(bill.getInt("storageId"));
    fBill.setRecodeDate(bill.getDate("recodeDate"));
    fBill.setUnitId(bill.getInt("unitId"));
    fBill.setStaffId(bill.getInt("staffId"));
    fBill.setDepartmentId(bill.getInt("departmentId"));
    fBill.setRemark(bill.getStr("remark"));
    fBill.setMemo(bill.getStr("memo"));
    fBill.setAmounts(bill.getBigDecimal("amounts"));
    fBill.setMoneys(bill.getBigDecimal("moneys"));
    fBill.setRetailMoneys(bill.getBigDecimal("retailMoneys"));
    return fBill;
  }
  
  public Record inPublic(Record record, FlowBill fBill)
  {
    FlowBill.setComStorage(record, fBill, "storageId", null);
    record.set("recodeDate", fBill.getRecodeDate());
    record.set("unitId", fBill.getUnitId());
    record.set("staffId", fBill.getStaffId());
    record.set("departmentId", fBill.getDepartmentId());
    record.set("remark", fBill.getRemark());
    record.set("memo", fBill.getMemo());
    record.set("amounts", fBill.getAmounts());
    record.set("moneys", fBill.getMoneys());
    record.set("retailMoneys", fBill.getRetailMoneys());
    record.set("accountsId", Integer.valueOf(25));
    return record;
  }
}
