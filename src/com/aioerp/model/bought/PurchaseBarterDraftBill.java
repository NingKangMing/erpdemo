package com.aioerp.model.bought;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseBarterDraftBill
  extends BaseDbModel
{
  public static final PurchaseBarterDraftBill dao = new PurchaseBarterDraftBill();
  public static final String TABLE_NAME = "cg_barter_bill";
  private Unit billUnit;
  private Staff billStaff;
  private Storage billOutStorage;
  private Storage billInStorage;
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
    return 
      Db.use(configName).queryLong("select count(*) from cg_barter_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, String orderField, String orderDirection)
  {
    String sql = " from cg_barter_bill";
    if (StringUtils.isNotBlank(orderField)) {
      sql = sql + " order by " + orderField + " " + orderDirection;
    }
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[0]);
  }
}
