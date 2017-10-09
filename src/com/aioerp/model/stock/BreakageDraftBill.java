package com.aioerp.model.stock;

import com.aioerp.bean.FlowBill;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;

public class BreakageDraftBill
  extends BaseDbModel
{
  public static final BreakageDraftBill dao = new BreakageDraftBill();
  public static final String TABLE_NAME = "cc_breakage_bill";
  private Product product;
  private Staff staff;
  private Storage storage;
  private Department department;
  
  public Product getProduct(String configName)
  {
    if (this.product == null) {
      this.product = ((Product)Product.dao.findById(configName, get("productId")));
    }
    return this.product;
  }
  
  public Staff getStaff(String configName)
  {
    if (this.staff == null) {
      this.staff = ((Staff)Staff.dao.findById(configName, get("staffId")));
    }
    return this.staff;
  }
  
  public Storage getStorage(String configName)
  {
    if (this.storage == null) {
      this.storage = ((Storage)Storage.dao.findById(configName, get("storageId")));
    }
    return this.storage;
  }
  
  public Department getDepartment(String configName)
  {
    if (this.department == null) {
      this.department = ((Department)Department.dao.findById(configName, get("departmentId")));
    }
    return this.department;
  }
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from cc_breakage_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public void inPublic(Record bill, FlowBill fBill)
  {
    bill.set("staffId", fBill.getStaffId());
    bill.set("departmentId", fBill.getDepartmentId());
    bill.set("remark", fBill.getRemark());
    bill.set("memo", fBill.getMemo());
    bill.set("amounts", fBill.getAmounts());
    bill.set("moneys", fBill.getMoneys());
    Integer storageId = null;
    if (fBill.getIsInSck().intValue() == 1) {
      storageId = fBill.getInStorageId();
    } else {
      storageId = fBill.getOutStorageId();
    }
    bill.set("storageId", storageId);
  }
}
