package com.aioerp.model.bought;

import com.aioerp.bean.FlowBill;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseDraftBill
  extends BaseDbModel
{
  public static final PurchaseDraftBill dao = new PurchaseDraftBill();
  public static final String TABLE_NAME = "cg_purchase_draft_bill";
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
    return Db.use(configName).queryLong("select count(*) from cg_purchase_draft_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, String orderField, String orderDirection)
  {
    String sql = " from cg_purchase_draft_bill";
    if (StringUtils.isNotBlank(orderField)) {
      sql = sql + " order by " + orderField + " " + orderDirection;
    }
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[0]);
  }
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
  {
    StringBuffer sel = new StringBuffer("select b.* ");
    sel.append(",case when b.isRCW = 2 then b.taxMoneys*-1 else b.taxMoneys end as taxMoneys");
    sel.append(",case when b.isRCW = 2 then b.privilege*-1 else b.privilege end as privilege");
    sel.append(",case when b.isRCW = 2 then b.privilegeMoney*-1 else b.privilegeMoney end as privilegeMoney");
    StringBuffer sql = new StringBuffer(" from cg_purchase_draft_bill as b");
    List<Object> paras = new ArrayList();
    sql.append(" left join b_unit as unit on b.unitId = unit.id");
    sql.append(" left join b_staff as staff on b.staffId = staff.id");
    sql.append(" left join b_department as department on b.departmentId = department.id");
    sel.append(" ,unit.code as unitCode, unit.fullName as unitName");
    sel.append(" ,staff.code as staffCode, staff.name as staffName");
    sel.append(" ,department.code as departmentCode, department.fullName as departmentName");
    sql.append(" where 1=1");
    if (map != null)
    {
      if (map.get("startDate") != null) {
        sql.append(" and b.recodeDate >= '" + map.get("startDate") + "'");
      }
      if (map.get("endDate") != null) {
        sql.append("  and b.recodeDate <= '" + map.get("endDate") + " 23:59:59'");
      }
      if (map.get("unitId") != null)
      {
        sql.append(" and (b.unitId in (select unit.id from b_unit unit where unit.pids like ?) or b.unitId = ?)");
        paras.add("'%{" + map.get("unitId") + "}%'");
        paras.add(map.get("unitId"));
      }
      if (map.get("isPrivilege") != null) {
        sql.append(" and b.privilege != 0 and b.privilege is not null");
      }
      if (map.get("staffId") != null)
      {
        sql.append(" and( b.staffId in (select staff.id from b_staff staff where staff.pids like ?) or b.staffId = ?)");
        paras.add("'%{" + map.get("staffId") + "}%'");
        paras.add(map.get("staffId"));
      }
      if (map.get("departmentId") != null)
      {
        sql.append(" and (b.departmentId in (select department.id from b_department department where department.pids like ?) or b.departmentId = ?)");
        paras.add("'%{" + map.get("departmentId") + "}%'");
        paras.add(map.get("departmentId"));
      }
      if ((map.get("notStatus") != null) && (!"".equals(map.get("notStatus")))) {
        sql.append(" and b.isRCW =0");
      }
    }
    rank(sql, map);
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, sel.toString(), sql.toString(), paras.toArray());
  }
  
  public List<Model> getList(String configName, Integer status, Map<String, Object> map)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("unit", Unit.class);
    helper.addAlias("staff", Staff.class);
    helper.addAlias("storage", Storage.class);
    helper.addAlias("department", Department.class);
    helper.appendSelect("select bill.*,unit.*,staff.*,storage.*,department.*");
    helper.appendSql(" from cg_purchase_draft_bill as bill");
    helper.appendSql(" left join b_unit as unit on bill.unitId = unit.id ");
    helper.appendSql(" left join b_staff as staff on bill.staffId = staff.id");
    helper.appendSql(" left join b_storage as storage on bill.storageId = storage.id");
    helper.appendSql(" left join b_department as department on bill.departmentId = department.id");
    helper.appendSql(" where 1=1");
    if (status != null)
    {
      helper.appendSql(" and bill.status = ?");
      helper.appendParam(status);
    }
    query(helper, map);
    helper.appendSql(" order by recodeDate desc");
    return dao.manyList(configName, helper);
  }
  
  private void rank(StringBuffer sql, Map<String, Object> map)
  {
    String orderField = (String)map.get("orderField");
    String orderDirection = (String)map.get("orderDirection");
    if ((StringUtils.isBlank(orderField)) || (StringUtils.isBlank(orderDirection))) {
      return;
    }
    sql.append(" order by " + orderField + " " + orderDirection);
  }
  
  private void query(SqlHelper helper, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    if ((map.get("unitId") != null) && (!"".equals(map.get("unitId"))))
    {
      helper.appendSql(" and bill.unitId = ?");
      helper.appendParam(map.get("unitId"));
    }
    if ((map.get("storageId") != null) && (!"".equals(map.get("storageId"))))
    {
      helper.appendSql(" and bill.storageId = ?");
      helper.appendParam(map.get("storageId"));
    }
    if ((map.get("staffId") != null) && (!"".equals(map.get("staffId"))))
    {
      helper.appendSql(" and bill.staffId = ?");
      helper.appendParam(map.get("staffId"));
    }
    if ((map.get("departmentId") != null) && (!"".equals(map.get("departmentId"))))
    {
      helper.appendSql(" and bill.departmentId = ?");
      helper.appendParam(map.get("departmentId"));
    }
    if (map.get("isRCW") != null)
    {
      helper.appendSql(" and bill.isRCW = ?");
      helper.appendParam(map.get("isRCW"));
    }
  }
  
  private void query(StringBuffer sql, List<Object> params, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    if (map.get("isRCW") != null)
    {
      sql.append(" and b.isRCW = ?");
      params.add(map.get("isRCW"));
    }
    if ((map.get("unitId") != null) && (!"".equals(map.get("unitId"))))
    {
      sql.append(" and unitId = ?");
      params.add(map.get("unitId"));
    }
    if ((map.get("storageId") != null) && (!"".equals(map.get("storageId"))))
    {
      sql.append(" and storageId = ?");
      params.add(map.get("storageId"));
    }
    if ((map.get("staffId") != null) && (!"".equals(map.get("staffId"))))
    {
      sql.append(" and staffId = ?");
      params.add(map.get("staffId"));
    }
    if ((map.get("departmentId") != null) && (!"".equals(map.get("departmentId"))))
    {
      sql.append(" and departmentId = ?");
      params.add(map.get("departmentId"));
    }
    if ((map.get("startDate") != null) && (!"".equals(map.get("startDate"))))
    {
      sql.append(" and  b.updateTime >= ?");
      params.add(map.get("startDate"));
    }
    if ((map.get("endDate") != null) && (!"".equals(map.get("endDate"))))
    {
      sql.append(" and  b.updateTime <= ?");
      params.add(map.get("endDate") + " 23:59:59");
    }
    if ((map.get("unitName") != null) && (!"".equals(map.get("unitName"))))
    {
      sql.append(" and u.fullName like ?");
      params.add("%" + map.get("unitName") + "%");
    }
    if ((map.get("staffName") != null) && (!"".equals(map.get("staffName"))))
    {
      sql.append(" and s.name like ?");
      params.add("%" + map.get("staffName") + "%");
    }
    if ((map.get("storageName") != null) && (!"".equals(map.get("storageName"))))
    {
      sql.append(" and t.fullName like ?");
      params.add("%" + map.get("storageName") + "%");
    }
    if ((map.get("departmentName") != null) && (!"".equals(map.get("departmentName"))))
    {
      sql.append(" and d.fullName like ?");
      params.add("%" + map.get("departmentName") + "%");
    }
    if ((map.get("detailMemo") != null) && (!"".equals(map.get("detailMemo"))))
    {
      sql.append(" and b.id in (select distinct(d.billId) from cg_purchase_draft_detail d where d.memo like ?)");
      params.add("%" + map.get("detailMemo") + "%");
    }
    if ((map.get("productName") != null) && (!"".equals(map.get("productName"))))
    {
      sql.append(" and b.id in (select distinct(d.billId) from cg_purchase_draft_detail d left join b_product p on  d.productId = p.id  where p.fullName like ?)");
      params.add("%" + map.get("productName") + "%");
    }
    if (map.get("bill") == null) {
      return;
    }
    PurchaseDraftBill bill = (PurchaseDraftBill)map.get("bill");
    if (StringUtils.isNotBlank(bill.getStr("code")))
    {
      sql.append(" and b.code like ?");
      params.add("%" + bill.getStr("code") + "%");
    }
    if (StringUtils.isNotBlank(bill.getStr("remark")))
    {
      sql.append(" and b.remark like ?");
      params.add("%" + bill.getStr("remark") + "%");
    }
    if (StringUtils.isNotBlank(bill.getStr("memo")))
    {
      sql.append(" and b.memo like ?");
      params.add("%" + bill.getStr("memo") + "%");
    }
  }
  
  public Model getRecordById(String configName, int id)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("bill", PurchaseDraftBill.class);
    helper.addAlias("unit", Unit.class);
    helper.addAlias("staff", Staff.class);
    helper.addAlias("storage", Storage.class);
    helper.addAlias("department", Department.class);
    helper.appendSelect(" select bill.*,unit.*,staff.*,storage.*,department.*");
    helper.appendSql(" from cg_purchase_draft_bill as bill");
    helper.appendSql(" left join b_unit as unit on bill.unitId = unit.id ");
    helper.appendSql(" left join b_staff as staff on bill.staffId = staff.id");
    helper.appendSql(" left join b_department as department on bill.departmentId = department.id");
    helper.appendSql(" left join b_storage as storage on bill.storageId = storage.id where 1=1");
    helper.appendSql(" and bill.id = " + id);
    return dao.manyFirst(configName, helper);
  }
  
  public List<Model> getRelList(String configName, Integer status, Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer("select b.* from cg_purchase_draft_bill b left join b_unit u on b.unitId = u.id left join b_staff s on b.staffId = s.id left join b_storage t on b.storageId = t.id left join b_department d on b.departmentId=d.id where 1=1");
    
    List<Object> params = new ArrayList();
    if (status != null)
    {
      sql.append(" and b.status = ?");
      params.add(status);
    }
    query(sql, params, map);
    sql.append(" order by updateTime ");
    return dao.find(configName, sql.toString(), params.toArray());
  }
  
  public FlowBill outPublic(Record bill, FlowBill fBill)
  {
    fBill.setInStorageId(bill.getInt("storageId"));
    fBill.setAmounts(bill.getBigDecimal("amounts"));
    fBill.setMoneys(bill.getBigDecimal("moneys"));
    fBill.setDiscountMoneys(bill.getBigDecimal("discountMoneys"));
    fBill.setTaxMoneys(bill.getBigDecimal("taxMoneys"));
    fBill.setPrivilege(bill.getBigDecimal("privilege"));
    fBill.setPrivilegeMoney(bill.getBigDecimal("privilegeMoney"));
    fBill.setPayDate(bill.getDate("payDate"));
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
    if (fBill.getIsInSck().intValue() == -1) {
      bill.set("storageId", fBill.getOutStorageId());
    } else {
      bill.set("storageId", fBill.getInStorageId());
    }
    bill.set("privilege", fBill.getPrivilege());
    bill.set("payDate", fBill.getPayDate());
    bill.set("discounts", fBill.getDiscounts());
    bill.set("discountMoneys", fBill.getDiscountMoneys());
    bill.set("taxes", fBill.getTaxes());
    bill.set("taxMoneys", fBill.getTaxMoneys());
    bill.set("retailMoneys", fBill.getRetailMoneys());
    bill.set("payMoney", fBill.getPayMoney());
    bill.set("privilegeMoney", fBill.getPrivilegeMoney());
  }
}
