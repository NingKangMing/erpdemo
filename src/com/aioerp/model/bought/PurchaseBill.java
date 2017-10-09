package com.aioerp.model.bought;

import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseBill
  extends BaseDbModel
{
  public static final PurchaseBill dao = new PurchaseBill();
  public static final String TABLE_NAME = "cg_purchase_bill";
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
    return Db.use(configName).queryLong("select count(*) from cg_purchase_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, String orderField, String orderDirection)
  {
    String sql = " from cg_purchase_bill";
    if (StringUtils.isNotBlank(orderField)) {
      sql = sql + " order by " + orderField + " " + orderDirection;
    }
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[0]);
  }
  
  public Map<String, Object> getPrivilegePage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
  {
    StringBuffer sel = new StringBuffer("select b.* ");
    sel.append(",case when b.isRCW = 2 then b.taxMoneys*-1 else b.taxMoneys end as taxMoneys");
    sel.append(",case when b.isRCW = 2 then b.privilege*-1 else b.privilege end as privilege");
    sel.append(",case when b.isRCW = 2 then b.privilegeMoney*-1 else b.privilegeMoney end as privilegeMoney");
    StringBuffer sql = new StringBuffer(" from (");
    
    String[] tables = { "cg_purchase_bill", "cg_return_bill", "cg_barter_bill" };
    String withField = ",bill.id,bill.recodeDate,bill.code,bill.unitId,bill.staffId,bill.departmentId,bill.remark,bill.memo";
    for (int i = 0; i < tables.length; i++)
    {
      if (i != 0) {
        sql.append(" UNION ALL");
      }
      if ("cg_barter_bill".equals(tables[i])) {
        sql.append(" select 12 as type, bill.isRCW,bill.gapMoney as taxMoneys,bill.privilege,bill.privilegeMoney " + withField + " from " + tables[i] + " bill");
      } else if ("cg_return_bill".equals(tables[i])) {
        sql.append(" select 6 as type, bill.isRCW,bill.taxMoneys*-1 as taxMoneys,bill.privilege*-1 as privilege,bill.privilegeMoney*-1 as privilegeMoney " + withField + " from " + tables[i] + " bill");
      } else {
        sql.append(" select 5 as type, bill.isRCW,bill.taxMoneys,bill.privilege,bill.privilegeMoney " + withField + " from " + tables[i] + " bill");
      }
      sql.append(" where 1=1 ");
      sql.append(" and bill.privilege != 0 and bill.privilege is not null");
      ComFunController.queryUnitPrivs(sql, (String)map.get("unitPrivs"), "bill");
      if ("cg_barter_bill".equals(tables[i]))
      {
        ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "bill", "outStorageId");
        ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "bill", "inStorageId");
      }
      else
      {
        ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "bill");
      }
      ComFunController.queryStaffPrivs(sql, (String)map.get("staffPrivs"), "bill");
      ComFunController.queryDepartmentPrivs(sql, (String)map.get("departmentPrivs"), "bill");
    }
    sql.append(") as b");
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
    helper.appendSql(" from cg_purchase_bill as bill");
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
    if ((!"0".equals(String.valueOf(map.get("unitId")))) && (map.get("unitId") != null) && (!"".equals(map.get("unitId"))))
    {
      helper.appendSql(" and bill.unitId = ?");
      helper.appendParam(map.get("unitId"));
    }
    if ((!"0".equals(String.valueOf(map.get("storageId")))) && (map.get("storageId") != null) && (!"".equals(map.get("storageId"))))
    {
      helper.appendSql(" and bill.storageId = ?");
      helper.appendParam(map.get("storageId"));
    }
    if ((!"0".equals(String.valueOf(map.get("staffId")))) && (map.get("staffId") != null) && (!"".equals(map.get("staffId"))))
    {
      helper.appendSql(" and bill.staffId = ?");
      helper.appendParam(map.get("staffId"));
    }
    if ((!"0".equals(String.valueOf(map.get("departmentId")))) && (map.get("departmentId") != null) && (!"".equals(map.get("departmentId"))))
    {
      helper.appendSql(" and bill.departmentId = ?");
      helper.appendParam(map.get("departmentId"));
    }
    if (map.get("isRCW") != null)
    {
      helper.appendSql(" and bill.isRCW = ?");
      helper.appendParam(map.get("isRCW"));
    }
    if ((map.get("startDate") != null) && (!"".equals(map.get("startDate"))))
    {
      helper.appendSql(" and  bill.updateTime >= ?");
      helper.appendParam(map.get("startDate"));
    }
    if ((map.get("endDate") != null) && (!"".equals(map.get("endDate"))))
    {
      helper.appendSql(" and  bill.updateTime <= ?");
      helper.appendParam(map.get("endDate") + " 23:59:59");
    }
    if ((map.get("unitName") != null) && (!"".equals(map.get("unitName"))))
    {
      helper.appendSql(" and unit.fullName like ?");
      helper.appendParam("%" + map.get("unitName") + "%");
    }
    if ((map.get("staffName") != null) && (!"".equals(map.get("staffName"))))
    {
      helper.appendSql(" and staff.name like ?");
      helper.appendParam("%" + map.get("staffName") + "%");
    }
    if ((map.get("storageName") != null) && (!"".equals(map.get("storageName"))))
    {
      helper.appendSql(" and storage.fullName like ?");
      helper.appendParam("%" + map.get("storageName") + "%");
    }
    if ((map.get("departmentName") != null) && (!"".equals(map.get("departmentName"))))
    {
      helper.appendSql(" and department.fullName like ?");
      helper.appendParam("%" + map.get("departmentName") + "%");
    }
    if ((map.get("detailMemo") != null) && (!"".equals(map.get("detailMemo"))))
    {
      helper.appendSql(" and bill.id in (select distinct(d.billId) from cg_purchase_detail d where d.memo like ?)");
      helper.appendParam("%" + map.get("detailMemo") + "%");
    }
    if ((map.get("productName") != null) && (!"".equals(map.get("productName"))))
    {
      helper.appendSql(" and bill.id in (select distinct(d.billId) from cg_purchase_detail d left join b_product p on  d.productId = p.id  where p.fullName like ?)");
      helper.appendParam("%" + map.get("productName") + "%");
    }
    if (map.get("bill") == null) {
      return;
    }
    PurchaseBill bill = (PurchaseBill)map.get("bill");
    if (StringUtils.isNotBlank(bill.getStr("code")))
    {
      helper.appendSql(" and bill.code like ?");
      helper.appendParam("%" + bill.getStr("code") + "%");
    }
    if (StringUtils.isNotBlank(bill.getStr("remark")))
    {
      helper.appendSql(" and bill.remark like ?");
      helper.appendParam("%" + bill.getStr("remark") + "%");
    }
    if (StringUtils.isNotBlank(bill.getStr("memo")))
    {
      helper.appendSql(" and bill.memo like ?");
      helper.appendParam("%" + bill.getStr("memo") + "%");
    }
  }
  
  public Model getRecordById(String configName, int id)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("bill", PurchaseBill.class);
    helper.addAlias("unit", Unit.class);
    helper.addAlias("staff", Staff.class);
    helper.addAlias("storage", Storage.class);
    helper.addAlias("department", Department.class);
    helper.appendSelect(" select bill.*,unit.*,staff.*,storage.*,department.*");
    helper.appendSql(" from cg_purchase_bill as bill");
    helper.appendSql(" left join b_unit as unit on bill.unitId = unit.id ");
    helper.appendSql(" left join b_staff as staff on bill.staffId = staff.id");
    helper.appendSql(" left join b_department as department on bill.departmentId = department.id");
    helper.appendSql(" left join b_storage as storage on bill.storageId = storage.id where 1=1");
    helper.appendSql(" and bill.id = " + id);
    return dao.manyFirst(configName, helper);
  }
  
  public List<Model> getRelList(String configName, Integer status, Map<String, Object> map)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("unit", Unit.class);
    helper.addAlias("staff", Staff.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select bill.*,unit.*,staff.*,storage.*");
    helper.appendSql(" from cg_purchase_bill as bill");
    helper.appendSql(" left join b_unit as unit on bill.unitId = unit.id ");
    helper.appendSql(" left join b_staff as staff on bill.staffId = staff.id");
    helper.appendSql(" left join b_storage as storage on bill.storageId = storage.id");
    helper.appendSql(" left join b_department as department on bill.departmentId = department.id where 1=1");
    if (status != null)
    {
      helper.appendSql(" and bill.status = ?");
      helper.appendParam(status);
    }
    query(helper, map);
    
    helper.appendSql(" order by bill.updateTime");
    return dao.manyList(configName, helper);
  }
}
