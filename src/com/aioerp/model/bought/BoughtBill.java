package com.aioerp.model.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.sys.User;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class BoughtBill
  extends BaseDbModel
{
  public static final BoughtBill dao = new BoughtBill();
  public static final String TABLE_NAME = "cg_bought_bill";
  private Unit billUnit;
  private Staff billStaff;
  private Storage billStorage;
  
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
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from cg_bought_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public List<Model> search(String configName)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("b", BoughtBill.class);
    helper.addAlias("u", Unit.class);
    helper.appendSelect(" select b.*,u.* ");
    helper.appendSql(" from cg_bought_bill b ");
    helper.appendSql(" left join b_unit as u on b.unitId=u.id ");
    helper.appendSql(" where 1=1  ");
    return dao.manyList(configName, helper);
  }
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, String orderField, String orderDirection, Map<String, Object> map)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> obj = new HashMap();
    obj.put("staff", Staff.class);
    obj.put("unit", Unit.class);
    String sel = "select bill.*,staff.*,unit.*";
    StringBuffer sql = new StringBuffer(" from cg_bought_bill as bill");
    sql.append(" left join b_staff as staff on bill.staffId = staff.id");
    sql.append(" left join b_unit as unit on bill.unitId = unit.id");
    sql.append(" left join b_storage as storage on bill.storageId = storage.id");
    sql.append(" where 1=1");
    queryBasePrivs(sql, (String)map.get("unitPrivs"), (String)map.get("staffPrivs"), (String)map.get("storagePrivs"));
    rank(sql, orderField, orderDirection);
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel, sql.toString(), obj, new Object[0]);
  }
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws Exception
  {
    Map<String, Class<? extends Model>> obj = new HashMap();
    obj.put("staff", Staff.class);
    obj.put("unit", Unit.class);
    obj.put("storage", Storage.class);
    String sel = "select bill.*,staff.*,unit.*,storage.*";
    StringBuffer sql = new StringBuffer(" from cg_bought_bill as bill");
    sql.append(" left join b_staff as staff on bill.staffId = staff.id");
    sql.append(" left join b_unit as unit on bill.unitId = unit.id");
    sql.append(" left join b_storage as storage on bill.storageId = storage.id");
    sql.append(" where 1=1");
    
    queryBasePrivs(sql, (String)map.get("unitPrivs"), (String)map.get("staffPrivs"), (String)map.get("storagePrivs"));
    
    List<Object> paras = new ArrayList();
    if (map != null)
    {
      query(sql, paras, map);
      if ((map.get("orderField") != null) && (map.get("orderDirection") != null))
      {
        String orderField = (String)map.get("orderField");
        String orderDirection = (String)map.get("orderDirection");
        rank(sql, orderField, orderDirection);
      }
    }
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel, sql.toString(), obj, paras.toArray());
  }
  
  public List<Model> getRecordList(String configName, Integer status, Integer unitId, Integer storageId, Integer staffId, Map<String, Object> params)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("unit", Unit.class);
    helper.addAlias("staff", Staff.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select bill.*,unit.*,staff.*,storage.*");
    helper.appendSql(" from cg_bought_bill as bill");
    helper.appendSql(" left join b_unit as unit on bill.unitId = unit.id ");
    helper.appendSql(" left join b_staff as staff on bill.staffId = staff.id");
    helper.appendSql(" left join b_storage as storage on bill.storageId = storage.id where 1=1");
    if ((StringUtils.isNotBlank((String)params.get("unitPrivs"))) && (!"*".equals(params.get("unitPrivs")))) {
      helper.appendSql(" and (bill.unitId in(" + params.get("unitPrivs") + ") or bill.unitId is null )");
    } else if (StringUtils.isBlank((String)params.get("unitPrivs"))) {
      helper.appendSql(" and (bill.unitId =0 or bill.unitId is null)");
    }
    if ((StringUtils.isNotBlank((String)params.get("staffPrivs"))) && (!"*".equals(params.get("staffPrivs")))) {
      helper.appendSql(" and (bill.staffId in(" + params.get("staffPrivs") + ") or bill.staffId is null )");
    } else if (StringUtils.isBlank((String)params.get("staffPrivs"))) {
      helper.appendSql(" and (bill.staffId =0 or bill.staffId is null)");
    }
    if ((StringUtils.isNotBlank((String)params.get("storagePrivs"))) && (!"*".equals(params.get("storagePrivs")))) {
      helper.appendSql(" and (bill.storageId in(" + params.get("storagePrivs") + ") or bill.storageId is null )");
    } else if (StringUtils.isBlank((String)params.get("storagePrivs"))) {
      helper.appendSql(" and (bill.storageId =0 or bill.storageId is null)");
    }
    if (status != null) {
      helper.appendSql(" and bill.status =" + status);
    }
    if ((unitId != null) && (unitId.intValue() != 0)) {
      helper.appendSql(" and bill.unitId =" + unitId);
    }
    if ((storageId != null) && (storageId.intValue() != 0)) {
      helper.appendSql(" and bill.storageId =" + storageId);
    }
    if ((staffId != null) && (staffId.intValue() != 0)) {
      helper.appendSql(" and bill.staffId =" + staffId);
    }
    helper.appendSql(" order by bill.updateTime ");
    return dao.manyList(configName, helper);
  }
  
  public Model getRecordById(String configName, int id)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("bill", BoughtBill.class);
    helper.addAlias("unit", Unit.class);
    helper.addAlias("staff", Staff.class);
    helper.addAlias("storage", Storage.class);
    helper.addAlias("department", Department.class);
    helper.appendSelect(" select bill.*,unit.*,staff.*,storage.*,department.*");
    helper.appendSql(" from cg_bought_bill as bill");
    helper.appendSql(" left join b_unit as unit on bill.unitId = unit.id ");
    helper.appendSql(" left join b_staff as staff on bill.staffId = staff.id");
    helper.appendSql(" left join b_storage as storage on bill.storageId = storage.id");
    helper.appendSql(" left join b_department as department on staff.depmId = department.id");
    helper.appendSql(" where 1=1");
    helper.appendSql(" and bill.id = " + id);
    return dao.manyFirst(configName, helper);
  }
  
  public List<Model> getList(String configName, Integer status, Map<String, Object> pars)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("unit", Unit.class);
    helper.addAlias("staff", Staff.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select bill.*,unit.*,staff.*,storage.*");
    helper.appendSql(" from cg_bought_bill as bill");
    helper.appendSql(" left join b_unit as unit on bill.unitId = unit.id ");
    helper.appendSql(" left join b_staff as staff on bill.staffId = staff.id");
    helper.appendSql(" left join b_storage as storage on bill.storageId = storage.id where 1=1");
    if ((StringUtils.isNotBlank((String)pars.get("unitPrivs"))) && (!"*".equals(pars.get("unitPrivs")))) {
      helper.appendSql(" and (bill.unitId in(" + pars.get("unitPrivs") + ") or bill.unitId is null )");
    } else if (StringUtils.isBlank((String)pars.get("unitPrivs"))) {
      helper.appendSql(" and (bill.unitId =0 or bill.unitId is null)");
    }
    if ((StringUtils.isNotBlank((String)pars.get("staffPrivs"))) && (!"*".equals(pars.get("staffPrivs")))) {
      helper.appendSql(" and (bill.staffId in(" + pars.get("staffPrivs") + ") or bill.staffId is null )");
    } else if (StringUtils.isBlank((String)pars.get("staffPrivs"))) {
      helper.appendSql(" and (bill.staffId =0 or bill.staffId is null)");
    }
    if ((StringUtils.isNotBlank((String)pars.get("storagePrivs"))) && (!"*".equals(pars.get("storagePrivs")))) {
      helper.appendSql(" and (bill.storageId in(" + pars.get("storagePrivs") + ") or bill.storageId is null )");
    } else if (StringUtils.isBlank((String)pars.get("storagePrivs"))) {
      helper.appendSql(" and (bill.storageId =0 or bill.storageId is null)");
    }
    if (status != null)
    {
      helper.appendSql(" and bill.status = ?");
      helper.appendParam(status);
    }
    query(helper, pars);
    

    helper.appendSql(" order by bill.updateTime");
    return dao.manyList(configName, helper);
  }
  
  public Map<String, Object> getExecutePage(String configName, String listID, int pageNum, int numPerPage, int billId, Map<String, Object> params)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> map = new HashMap();
    


    map.put("staff", Staff.class);
    map.put("department", Department.class);
    map.put("user", User.class);
    
    String sel = "select bill.*,department.*, staff.*,user.*";
    
    StringBuffer from = new StringBuffer(" from ");
    from.append(" (");
    from.append(" SELECT purchase.isRCW,purchase.id ,purchase.userId,purchase.code,purchase.updateTime,purchase.memo,purchase.unitId,purchase.storageId,purchase.staffId,purchase.departmentId,case when purchase.isRCW!=2 then sum(taxMoney) else 0-sum(taxMoney) end as moneys,purchase.remark ,5 as 'type',purchase.status");
    from.append(" FROM cg_purchase_bill purchase");
    from.append(" right join (select d.* from cg_purchase_detail d where d.detailId in (select c.id from cg_bought_detail c where c.billId=" + billId + ") )  as detail");
    from.append(" on purchase.id = detail.billId  group by detail.billId ");
    
    from.append(" UNION ALL");
    from.append(" SELECT null isRCW,bought.id ,bought.userId,bought.code,bought.updateTime,bought.memo,bought.unitId,bought.storageId,bought.staffId,null as 'departmentId',bought.moneys,bought.remark,1 as 'type',bought.status");
    from.append(" FROM cg_bought_bill bought");
    from.append(" where bought.id = " + billId + " and bought.status = " + AioConstants.STATUS_FORCE);
    from.append(" ) as bill");
    
    from.append(" left join b_department as department on bill.departmentId = department.id ");
    from.append(" left join b_storage as storage on bill.storageId = storage.id ");
    from.append(" left join b_unit as unit on bill.unitId = unit.id ");
    from.append(" left join sys_user as user on bill.userId = user.id ");
    from.append(" left join b_staff as staff on user.staffId = staff.id  ");
    from.append(" where 1=1");
    queryBasePrivs(from, (String)params.get("unitPrivs"), (String)params.get("staffPrivs"), (String)params.get("storagePrivs"));
    String departmentPrivs = (String)params.get("departmentPrivs");
    if ((StringUtils.isNotBlank(departmentPrivs)) && (!"*".equals(departmentPrivs))) {
      from.append(" and (bill.departmentId in(" + departmentPrivs + ") or bill.departmentId is null)");
    } else if (StringUtils.isBlank(departmentPrivs)) {
      from.append(" and (bill.departmentId =0 or bill.staffId is null)");
    }
    rank(from, (String)params.get("orderField"), (String)params.get("orderDirection"));
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel, from.toString(), map, new Object[0]);
  }
  
  public Map<String, Object> boughtExpirePage(String configName, String listID, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
    throws Exception
  {
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("unit", Unit.class);
    map.put("staff", Staff.class);
    StringBuffer sel = new StringBuffer("select bill.*,unit.*,staff.*");
    StringBuffer sql = new StringBuffer("from cg_bought_bill as bill");
    sql.append(" left join b_unit as unit on unit.id = bill.unitId");
    sql.append(" left join b_staff as staff on staff.id = bill.staffId");
    sql.append(" where 1=1 and bill.status = 1");
    Integer aheadDay = Integer.valueOf(0);
    if (pars.get("aheadDay") != null) {
      aheadDay = (Integer)pars.get("aheadDay");
    }
    sql.append(" and deliveryDate > date_sub(CURDATE(),INTERVAL " + aheadDay + " day)");
    queryBasePrivs(sql, (String)pars.get("unitPrivs"), (String)pars.get("staffPrivs"), (String)pars.get("storagePrivs"));
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    }
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), map, new Object[0]);
  }
  
  private void query(StringBuffer sql, List<Object> paras, Map<String, Object> pars)
  {
    if (pars == null) {
      return;
    }
    if ((pars.get("startDate") != null) && (!"".equals(pars.get("startDate"))))
    {
      sql.append(" and  bill.recodeDate >= ?");
      paras.add(pars.get("startDate"));
    }
    if ((pars.get("endDate") != null) && (!"".equals(pars.get("endDate"))))
    {
      sql.append(" and  bill.recodeDate <= ?");
      paras.add(pars.get("endDate") + " 23:59:59");
    }
    if ((pars.get("unitName") != null) && (!"".equals(pars.get("unitName"))))
    {
      sql.append(" and unit.fullName like ?");
      paras.add("%" + pars.get("unitName") + "%");
    }
    if (pars.get("unitId") != null)
    {
      sql.append(" and bill.unitId in(select id from b_unit unit where unit.pids like ? or unit.id = ? )");
      paras.add("%{" + pars.get("unitId") + "}%");
      paras.add(pars.get("unitId"));
    }
    if ((pars.get("staffName") != null) && (!"".equals(pars.get("staffName"))))
    {
      sql.append(" and staff.name like ?");
      paras.add("%" + pars.get("staffName") + "%");
    }
    if (pars.get("staffId") != null)
    {
      sql.append(" and bill.staffId in(select id from b_staff staff where staff.pids like ? or staff.id = ? )");
      paras.add("%{" + pars.get("staffId") + "}%");
      paras.add(pars.get("staffId"));
    }
    if ((pars.get("storageName") != null) && (!"".equals(pars.get("storageName"))))
    {
      sql.append(" and storage.fullName like ?");
      paras.add("%" + pars.get("storageName") + "%");
    }
    if (pars.get("storageId") != null)
    {
      sql.append(" and bill.storageId in(select id from b_storage storage where storage.pids like ? or storage.id = ? )");
      paras.add("%{" + pars.get("storageId") + "}%");
      paras.add(pars.get("storageId"));
    }
    if ((pars.get("detailMemo") != null) && (!"".equals(pars.get("detailMemo"))))
    {
      sql.append(" and bill.id in (select distinct(d.billId) from cg_bought_detail d where d.memo like ? )");
      paras.add("%" + pars.get("detailMemo") + "%");
    }
    if ((pars.get("productName") != null) && (!"".equals(pars.get("productName"))))
    {
      sql.append(" and bill.id in (select distinct(d.billId) from cg_bought_detail d left join b_product p on  d.productId = p.id  where p.fullName like ?)");
      paras.add("%" + pars.get("productName") + "%");
    }
    if (pars.get("bill") == null) {
      return;
    }
    BoughtBill bill = (BoughtBill)pars.get("bill");
    if (StringUtils.isNotBlank(bill.getStr("code")))
    {
      sql.append(" and bill.code like ?");
      paras.add("%" + bill.getStr("code") + "%");
    }
    if (StringUtils.isNotBlank(bill.getStr("remark")))
    {
      sql.append(" and bill.remark like ?");
      paras.add("%" + bill.getStr("remark") + "%");
    }
    if (StringUtils.isNotBlank(bill.getStr("memo")))
    {
      sql.append(" and bill.memo like ?");
      paras.add("%" + bill.getStr("memo") + "%");
    }
    if ((bill.getInt("status") != null) && (bill.getInt("status").intValue() != 0)) {
      if (bill.getInt("status").intValue() == AioConstants.STATUS_ENABLE)
      {
        sql.append(" and (bill.status = ? or bill.status =?)");
        paras.add(bill.getInt("status"));
        paras.add(Integer.valueOf(AioConstants.STATUS_FORCE));
      }
      else
      {
        sql.append(" and bill.status = ? ");
        paras.add(bill.getInt("status"));
      }
    }
  }
  
  private void query(SqlHelper helper, Map<String, Object> pars)
  {
    if (pars == null) {
      return;
    }
    if ((pars.get("startDate") != null) && (!"".equals(pars.get("startDate"))))
    {
      helper.appendSql(" and  bill.recodeDate >= ?");
      helper.appendParam(pars.get("startDate"));
    }
    if ((pars.get("endDate") != null) && (!"".equals(pars.get("endDate"))))
    {
      helper.appendSql(" and  bill.recodeDate <= ?");
      helper.appendParam(pars.get("endDate") + " 23:59:59");
    }
    if ((pars.get("unitName") != null) && (!"".equals(pars.get("unitName"))))
    {
      helper.appendSql(" and unit.fullName like ?");
      helper.appendParam("%" + pars.get("unitName") + "%");
    }
    if ((pars.get("unitId") != null) && (pars.get("unitId").equals("0")))
    {
      helper.appendSql(" and bill.unitId in(select id from b_unit unit where unit.pids like ? or unit.id = ? )");
      helper.appendParam("%{" + pars.get("unitId") + "}%");
      helper.appendParam(pars.get("unitId"));
    }
    if ((pars.get("staffName") != null) && (!"".equals(pars.get("staffName"))))
    {
      helper.appendSql(" and staff.name like ?");
      helper.appendParam("%" + pars.get("staffName") + "%");
    }
    if ((pars.get("staffId") != null) && (pars.get("staffId").equals("0")))
    {
      helper.appendSql(" and bill.staffId in(select id from b_staff staff where staff.pids like ? or staff.id = ? )");
      helper.appendParam("%{" + pars.get("staffId") + "}%");
      helper.appendParam(pars.get("staffId"));
    }
    if ((pars.get("storageName") != null) && (!"".equals(pars.get("storageName"))))
    {
      helper.appendSql(" and storage.fullName like ?");
      helper.appendParam("%" + pars.get("storageName") + "%");
    }
    if ((pars.get("storageId") != null) && (pars.get("storageId").equals("0")))
    {
      helper.appendSql(" and bill.storageId in(select id from b_storage storage where storage.pids like ? or storage.id = ? )");
      helper.appendParam("%{" + pars.get("storageId") + "}%");
      helper.appendParam(pars.get("storageId"));
    }
    if ((pars.get("detailMemo") != null) && (!"".equals(pars.get("detailMemo"))))
    {
      helper.appendSql(" and bill.id in (select distinct(d.billId) from cg_bought_detail d where d.memo like ? )");
      helper.appendParam("%" + pars.get("detailMemo") + "%");
    }
    if ((pars.get("productName") != null) && (!"".equals(pars.get("productName"))))
    {
      helper.appendSql(" and bill.id in (select distinct(d.billId) from cg_bought_detail d left join b_product p on  d.productId = p.id  where p.fullName like ?)");
      helper.appendParam("%" + pars.get("productName") + "%");
    }
    if (pars.get("bill") == null) {
      return;
    }
    BoughtBill bill = (BoughtBill)pars.get("bill");
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
    if ((bill.getInt("status") != null) && (bill.getInt("status").intValue() != 0)) {
      if (bill.getInt("status").intValue() == AioConstants.STATUS_ENABLE)
      {
        helper.appendSql(" and (bill.status = ? or bill.status =?)");
        helper.appendParam(bill.getInt("status"));
        helper.appendParam(Integer.valueOf(AioConstants.STATUS_FORCE));
      }
      else
      {
        helper.appendSql(" and bill.status = ? ");
        helper.appendParam(bill.getInt("status"));
      }
    }
  }
  
  private void rank(StringBuffer sql, String orderField, String orderDirection)
  {
    if ((StringUtils.isBlank(orderField)) || (StringUtils.isBlank(orderDirection))) {
      return;
    }
    if ("unitCode".equals(orderField)) {
      sql.append(" order by unit.code " + orderDirection);
    } else if ("unitName".equals(orderField)) {
      sql.append(" order by unit.fullName " + orderDirection);
    } else if ("staffCode".equals(orderField)) {
      sql.append(" order by staff.code " + orderDirection);
    } else if ("staffName".equals(orderField)) {
      sql.append(" order by staff.name " + orderDirection);
    } else if ("departmentCode".equals(orderField)) {
      sql.append(" order by department.code " + orderDirection);
    } else if ("departmentName".equals(orderField)) {
      sql.append(" order by department.fullName " + orderDirection);
    } else if ("username".equals(orderField)) {
      sql.append(" order by user.username " + orderDirection);
    } else {
      sql.append(" order by bill." + orderField + " " + orderDirection);
    }
  }
  
  private void queryBasePrivs(StringBuffer sql, String unitPrivs, String staffPrivs, String storagePrivs)
  {
    if ((StringUtils.isNotBlank(unitPrivs)) && (!"*".equals(unitPrivs))) {
      sql.append(" and (bill.unitId in(" + unitPrivs + ") or bill.unitId is null )");
    } else if (StringUtils.isBlank(unitPrivs)) {
      sql.append(" and (bill.unitId =0 or bill.unitId is null)");
    }
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql.append(" and (bill.staffId in(" + staffPrivs + ") or bill.staffId is null)");
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql.append(" and (bill.staffId =0 or bill.staffId is null)");
    }
    if ((StringUtils.isNotBlank(storagePrivs)) && (!"*".equals(storagePrivs))) {
      sql.append(" and (bill.storageId in(" + storagePrivs + ") or bill.storageId is null)");
    } else if (StringUtils.isBlank(storagePrivs)) {
      sql.append(" and (bill.storageId =0 or bill.storageId is null)");
    }
  }
}
