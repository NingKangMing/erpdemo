package com.aioerp.model.finance;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;

public class TransferDraftBill
  extends BaseDbModel
{
  public static final TransferDraftBill dao = new TransferDraftBill();
  public static final String TABLE_NAME = "cw_transfer_draft_bill";
  private Staff billStaff;
  private Department billDepartment;
  private Accounts billAccount;
  
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
      this.billAccount = ((Accounts)Accounts.dao.findById(configName, get("payAccountId")));
    }
    return this.billAccount;
  }
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from cw_transfer_draft_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
}
