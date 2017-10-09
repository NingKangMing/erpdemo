package com.aioerp.model.sell.sellbook;

import com.aioerp.common.AioConstants;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class SellbookBill
  extends BaseDbModel
{
  public static final SellbookBill dao = new SellbookBill();
  public static final String TABLE_NAME = "xs_sellbook_bill";
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
    return Db.use(configName).queryLong("select count(*) from xs_sellbook_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public List<Model> getList(String configName, int relStatus, Integer unitId, Map<String, Object> paraMap)
  {
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("bill", SellbookBill.class);
    helper.addAlias("unit", Unit.class);
    helper.addAlias("staff", Staff.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select bill.*,unit.*,staff.*,storage.*");
    helper.appendSql(" from xs_sellbook_bill as bill");
    helper.appendSql(" left join b_unit as unit on bill.unitId = unit.id ");
    helper.appendSql(" left join b_staff as staff on bill.staffId = staff.id");
    helper.appendSql(" left join b_storage as storage on bill.storageId = storage.id where 1=1");
    helper.appendSql(" and bill.status =" + AioConstants.BILL_STATUS3);
    helper.appendSql(" and bill.relStatus =" + relStatus);
    if ((unitId != null) && (unitId.intValue() != 0)) {
      helper.appendSql(" and bill.unitId =" + unitId);
    }
    StringBuffer sbSql = new StringBuffer("");
    
    ComFunController.queryUnitPrivs(sbSql, unitPrivs, "unit", "id");
    ComFunController.queryStoragePrivs(sbSql, storagePrivs, "storage", "id");
    ComFunController.queryStaffPrivs(sbSql, staffPrivs, "staff", "id");
    query(sbSql, paraMap);
    
    helper.appendSql(sbSql.toString());
    
    helper.appendSql(" order by bill.updateTime ");
    return dao.manyList(configName, helper);
  }
  
  public Map<String, Object> sellExpirePage(String configName, String listID, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
    throws Exception
  {
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("unit", Unit.class);
    map.put("staff", Staff.class);
    StringBuffer sel = new StringBuffer("select bill.*,unit.*,staff.*");
    StringBuffer sql = new StringBuffer("from xs_sellbook_bill as bill");
    sql.append(" left join b_unit as unit on unit.id = bill.unitId");
    sql.append(" left join b_staff as staff on staff.id = bill.staffId");
    sql.append(" where 1=1 and bill.relStatus = 1");
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
  
  public Model getRecordById(String configName, int id)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("bill", SellbookBill.class);
    helper.addAlias("unit", Unit.class);
    helper.addAlias("staff", Staff.class);
    helper.addAlias("storage", Storage.class);
    helper.addAlias("department", Department.class);
    helper.appendSelect(" select bill.*,unit.*,staff.*,storage.*,department.*");
    helper.appendSql(" from xs_sellbook_bill as bill");
    helper.appendSql(" left join b_unit as unit on bill.unitId = unit.id ");
    helper.appendSql(" left join b_staff as staff on bill.staffId = staff.id");
    helper.appendSql(" left join b_storage as storage on bill.storageId = storage.id ");
    helper.appendSql(" left join b_department as department on staff.depmId = department.id");
    helper.appendSql(" where 1=1");
    helper.appendSql(" and bill.id = " + id);
    return dao.manyFirst(configName, helper);
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
      sql.append(" and  bill.updateTime <= '" + pars.get("endDate") + " 23:59:59'");
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
      sql.append(" and bill.id in (select distinct(d.billId) from xs_sellbook_detail d where d.memo like '%" + pars.get("detailMemo") + "%')");
    }
    if ((pars.get("productName") != null) && (!"".equals(pars.get("productName")))) {
      sql.append(" and bill.id in (select distinct(d.billId) from xs_sellbook_detail d left join b_product p on  d.productId = p.id  where p.fullName like '%" + pars.get("productName") + "%')");
    }
    if (pars.get("bill") == null) {
      return;
    }
    SellbookBill bill = (SellbookBill)pars.get("bill");
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
