package com.aioerp.model.stock.other;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;

public class OtherInBill
  extends BaseDbModel
{
  public static final OtherInBill dao = new OtherInBill();
  public static final String TABLE_NAME = "cc_otherin_bill";
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
    return Db.use(configName).queryLong("select count(*) from cc_otherin_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
}
