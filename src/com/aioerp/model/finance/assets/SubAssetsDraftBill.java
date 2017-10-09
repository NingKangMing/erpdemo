package com.aioerp.model.finance.assets;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;

public class SubAssetsDraftBill
  extends BaseDbModel
{
  public static final SubAssetsDraftBill dao = new SubAssetsDraftBill();
  public static final String TABLE_NAME = "cw_subassets_draft_bill";
  private Unit billUnit;
  private Staff billStaff;
  private Department billDepartment;
  private Accounts billAccount;
  
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
  
  public Accounts getBillAccount(String configName)
  {
    if (this.billAccount == null) {
      this.billAccount = ((Accounts)Accounts.dao.findById(configName, get("getAccountId")));
    }
    return this.billAccount;
  }
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from cw_subassets_draft_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
}
