package com.aioerp.model.sell.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.sell.sellbook.SellbookBill;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class SellBill
  extends BaseDbModel
{
  public static final SellBill dao = new SellBill();
  public static final String TABLE_NAME = "xs_sell_bill";
  private Product billProduct;
  private Unit billUnit;
  private Staff billStaff;
  private Storage billStorage;
  private Department billDepartment;
  
  public Product getBillProduct(String configName)
  {
    if (this.billProduct == null) {
      this.billProduct = ((Product)Product.dao.findById(configName, get("productId")));
    }
    return this.billProduct;
  }
  
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
  
  public Model getRecordById(String configName, int id)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("bill", SellbookBill.class);
    helper.addAlias("unit", Unit.class);
    helper.addAlias("staff", Staff.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select bill.*,unit.*,staff.*,storage.*");
    helper.appendSql(" from xs_sell_bill as bill");
    helper.appendSql(" left join b_unit as unit on bill.unitId = unit.id ");
    helper.appendSql(" left join b_staff as staff on bill.staffId = staff.id");
    helper.appendSql(" left join b_storage as storage on bill.storageId = storage.id where 1=1");
    helper.appendSql(" and bill.id = " + id);
    return dao.manyFirst(configName, helper);
  }
  
  public List<Model> getList(String configName, Integer relStatus, Integer unitId, Map<String, Object> paraMap)
  {
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String departmentPrivs = String.valueOf(paraMap.get("departmentPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("bill", SellBill.class);
    helper.addAlias("unit", Unit.class);
    helper.addAlias("staff", Staff.class);
    helper.addAlias("department", Department.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select bill.*,unit.*,staff.*,storage.*");
    helper.appendSql(" from xs_sell_bill as bill");
    helper.appendSql(" left join b_unit as unit on bill.unitId = unit.id ");
    helper.appendSql(" left join b_staff as staff on bill.staffId = staff.id");
    helper.appendSql(" left join b_department as department on bill.departmentId = department.id");
    helper.appendSql(" left join b_storage as storage on bill.storageId = storage.id where 1=1");
    helper.appendSql(" and bill.status =" + AioConstants.BILL_STATUS3);
    helper.appendSql(" and bill.relStatus =" + relStatus);
    helper.appendSql(" and bill.isRCW =" + AioConstants.RCW_NO);
    if ((unitId != null) && (unitId.intValue() != 0)) {
      helper.appendSql(" and bill.unitId =" + unitId);
    }
    StringBuffer sbSql = new StringBuffer("");
    
    ComFunController.queryUnitPrivs(sbSql, unitPrivs, "unit", "id");
    ComFunController.queryStoragePrivs(sbSql, storagePrivs, "storage", "id");
    ComFunController.queryStaffPrivs(sbSql, staffPrivs, "staff", "id");
    ComFunController.queryStaffPrivs(sbSql, departmentPrivs, "department", "id");
    query(sbSql, paraMap);
    helper.appendSql(sbSql.toString());
    helper.appendSql(" order by bill.updateTime ");
    return dao.manyList(configName, helper);
  }
  
  private void query(StringBuffer sql, Map<String, Object> pars)
  {
    if (pars == null) {
      return;
    }
    if ((pars.get("startDate") != null) && (!"".equals(pars.get("startDate")))) {
      sql.append(" and  bill.updateTime >= '" + pars.get("startDate") + "'");
    }
    if ((pars.get("endDate") != null) && (!"".equals(pars.get("endDate")))) {
      sql.append(" and  bill.updateTime <= '" + pars.get("endDate") + "'");
    }
    if ((pars.get("unitName") != null) && (!"".equals(pars.get("unitName")))) {
      sql.append(" and unit.fullName like '%" + pars.get("unitName") + "%'");
    }
    if ((pars.get("staffName") != null) && (!"".equals(pars.get("staffName")))) {
      sql.append(" and staff.name like '%" + pars.get("staffName") + "%'");
    }
    if ((pars.get("storageName") != null) && (!"".equals(pars.get("storageName")))) {
      sql.append(" and storage.fullName like '%" + pars.get("storageName") + "%'");
    }
    if ((pars.get("detailMemo") != null) && (!"".equals(pars.get("detailMemo")))) {
      sql.append(" and bill.id in (select distinct(d.billId) from xs_sell_detail d where d.memo like '%" + pars.get("detailMemo") + "%')");
    }
    if ((pars.get("productName") != null) && (!"".equals(pars.get("productName")))) {
      sql.append(" and bill.id in (select distinct(d.billId) from xs_sell_detail d left join b_product p on  d.productId = p.id  where p.fullName like '%" + pars.get("productName") + "%')");
    }
    if (pars.get("bill") == null) {
      return;
    }
    SellBill bill = (SellBill)pars.get("bill");
    if (StringUtils.isNotBlank(bill.getStr("code"))) {
      sql.append(" and bill.code like '%" + bill.getStr("code") + "%'");
    }
    if (StringUtils.isNotBlank(bill.getStr("remark"))) {
      sql.append(" and bill.remark like '%" + bill.getStr("remark") + "%'");
    }
    if (StringUtils.isNotBlank(bill.getStr("memo"))) {
      sql.append(" and bill.memo like '%" + bill.getStr("memo") + "%'");
    }
  }
}
