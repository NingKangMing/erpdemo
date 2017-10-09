package com.aioerp.model.stock;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;

public class DismountDraftBill
  extends BaseDbModel
{
  public static final DismountDraftBill dao = new DismountDraftBill();
  public static final String TABLE_NAME = "cc_dismount_draft_bill";
  private Staff billStaff;
  private Storage billInStorage;
  private Storage billOutStorage;
  private Department billDepartment;
  
  public Staff getBillStaff(String configName)
  {
    if (this.billStaff == null) {
      this.billStaff = ((Staff)Staff.dao.findById(configName, get("staffId")));
    }
    return this.billStaff;
  }
  
  public Storage getBillInStorage(String configName)
  {
    if (this.billInStorage == null) {
      this.billInStorage = ((Storage)Storage.dao.findById(configName, get("inStorageId")));
    }
    return this.billInStorage;
  }
  
  public Storage getBillOutStorage(String configName)
  {
    if (this.billOutStorage == null) {
      this.billOutStorage = ((Storage)Storage.dao.findById(configName, get("outStorageId")));
    }
    return this.billOutStorage;
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
    return Db.use(configName).queryLong("select count(*) from cc_dismount_draft_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
}
