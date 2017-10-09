package com.aioerp.model.bought;

import com.aioerp.bean.FlowBill;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseReturnDraftBill
  extends BaseDbModel
{
  public static final PurchaseReturnDraftBill dao = new PurchaseReturnDraftBill();
  public static final String TABLE_NAME = "cg_return_draft_bill";
  private Unit billUnit;
  private Staff billStaff;
  private Storage billStorage;
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
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from cg_return_draft_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, String orderField, String orderDirection)
  {
    String sql = " from cg_return_draft_bill";
    if (StringUtils.isNotBlank(orderField)) {
      sql = sql + " order by " + orderField + " " + orderDirection;
    }
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[0]);
  }
  
  public FlowBill outPublic(Record bill, FlowBill fBill)
  {
    fBill.setOutStorageId(bill.getInt("storageId"));
    fBill.setAmounts(bill.getBigDecimal("amounts"));
    fBill.setMoneys(bill.getBigDecimal("moneys"));
    fBill.setDiscountMoneys(bill.getBigDecimal("discountMoneys"));
    fBill.setTaxMoneys(bill.getBigDecimal("taxMoneys"));
    fBill.setPrivilege(bill.getBigDecimal("privilege"));
    fBill.setPrivilegeMoney(bill.getBigDecimal("privilegeMoney"));
    fBill.setRetailMoneys(bill.getBigDecimal("retailMoneys"));
    fBill.setPayMoney(bill.getBigDecimal("payMoney"));
    return fBill;
  }
  
  public void inPublic(Record bill, FlowBill fBill)
  {
    bill.set("unitId", fBill.getUnitId());
    bill.set("staffId", fBill.getStaffId());
    bill.set("departmentId", fBill.getDepartmentId());
    bill.set("remark", fBill.getRemark());
    bill.set("memo", fBill.getMemo());
    bill.set("amounts", fBill.getAmounts());
    bill.set("moneys", fBill.getMoneys());
    if (fBill.getIsInSck().intValue() == 1) {
      bill.set("storageId", fBill.getInStorageId());
    } else {
      bill.set("storageId", fBill.getOutStorageId());
    }
    bill.set("privilege", fBill.getPrivilege());
    bill.set("privilegeMoney", fBill.getPrivilegeMoney());
    bill.set("discounts", fBill.getDiscounts());
    bill.set("discountMoneys", fBill.getDiscountMoneys());
    bill.set("taxes", fBill.getTaxes());
    bill.set("taxMoneys", fBill.getTaxMoneys());
    bill.set("retailMoneys", fBill.getRetailMoneys());
    bill.set("payMoney", fBill.getPayMoney());
  }
}
