package com.aioerp.model.sell.sell;

import com.aioerp.bean.FlowBill;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.sys.DeliveryCompany;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;

public class SellDraftBill
  extends BaseDbModel
{
  public static final SellDraftBill dao = new SellDraftBill();
  public static final String TABLE_NAME = "xs_sell_draft_bill";
  private Unit billUnit;
  private Staff billStaff;
  private Storage billStorage;
  private Department billDepartment;
  private DeliveryCompany billDeliveryCompany;
  
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
  
  public DeliveryCompany getBillDeliveryCompany(String configName)
  {
    if (this.billDeliveryCompany == null) {
      this.billDeliveryCompany = ((DeliveryCompany)DeliveryCompany.dao.findFirst(configName, "select * from delivery_company where code='" + getStr("deliveryCompanyCode") + "'"));
    }
    return this.billDeliveryCompany;
  }
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from xs_sell_draft_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public FlowBill outPublic(Record bill, FlowBill fBill)
  {
    fBill.setOutStorageId(bill.getInt("storageId"));
    fBill.setRecodeDate(bill.getDate("recodeDate"));
    fBill.setUnitId(bill.getInt("unitId"));
    fBill.setStaffId(bill.getInt("staffId"));
    fBill.setDepartmentId(bill.getInt("departmentId"));
    fBill.setRemark(bill.getStr("remark"));
    fBill.setMemo(bill.getStr("memo"));
    fBill.setDeliveryDate(bill.getDate("deliveryDate"));
    fBill.setAmounts(bill.getBigDecimal("amounts"));
    fBill.setMoneys(bill.getBigDecimal("moneys"));
    fBill.setDiscounts(bill.getBigDecimal("discounts"));
    fBill.setDiscountMoneys(bill.getBigDecimal("discountMoneys"));
    fBill.setTaxes(bill.getBigDecimal("taxes"));
    fBill.setTaxMoneys(bill.getBigDecimal("taxMoneys"));
    fBill.setRetailMoneys(bill.getBigDecimal("retailMoneys"));
    fBill.setPrivilege(bill.getBigDecimal("privilege"));
    fBill.setPrivilegeMoney(bill.getBigDecimal("privilegeMoney"));
    fBill.setPayMoney(bill.getBigDecimal("payMoney"));
    return fBill;
  }
  
  public void inPublic(Record bill, FlowBill fBill)
  {
    FlowBill.setComStorage(bill, fBill, null, "storageId");
    bill.set("recodeDate", fBill.getRecodeDate());
    bill.set("unitId", fBill.getUnitId());
    bill.set("staffId", fBill.getStaffId());
    bill.set("departmentId", fBill.getDepartmentId());
    bill.set("remark", fBill.getRemark());
    bill.set("memo", fBill.getMemo());
    bill.set("deliveryDate", fBill.getDeliveryDate());
    bill.set("amounts", fBill.getAmounts());
    bill.set("moneys", fBill.getMoneys());
    bill.set("discounts", fBill.getDiscounts());
    bill.set("discountMoneys", fBill.getDiscountMoneys());
    bill.set("taxes", fBill.getTaxes());
    bill.set("taxMoneys", fBill.getTaxMoneys());
    bill.set("retailMoneys", fBill.getRetailMoneys());
    bill.set("privilege", fBill.getPrivilege());
    bill.set("privilegeMoney", fBill.getPrivilegeMoney());
    bill.set("payMoney", fBill.getPayMoney());
  }
}
