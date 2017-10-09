package com.aioerp.model.stock;

import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TakeStockBill
  extends BaseDbModel
{
  public static final TakeStockBill dao = new TakeStockBill();
  public static final String TABLE_NAME = "cc_takestock_bill";
  Map<String, Class<? extends Model>> MAP = new HashMap();
  private Staff staff;
  private Storage storage;
  
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
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from cc_takestock_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public Map<String, Object> getPageBySgePids(String configName, Map<String, Object> pMap, String sgePids, String startDate, String endDate, String listID, int pageNum, int numPerPage, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    this.MAP.put("takBill", TakeStockBill.class);
    this.MAP.put("stf", Staff.class);
    this.MAP.put("sge", Storage.class);
    StringBuffer selectSql = new StringBuffer("SELECT takBill.*,stf.*,sge.*");
    StringBuffer fromsql = new StringBuffer(" FROM cc_takestock_bill as takBill LEFT JOIN b_staff stf ON stf.id=takBill.staffId LEFT JOIN b_storage sge ON sge.id=takBill.storageId");
    fromsql.append(" where 1=1 ");
    fromsql.append(" AND takBill.createTime >= '" + startDate + "' AND takBill.createTime <= '" + endDate + " 23:59:59' ");
    if ((!"".equals(sgePids)) && (sgePids != null)) {
      fromsql.append(" AND takBill.storageId IN (select id from b_storage where pids LIKE CONCAT('" + sgePids + "','%')) ");
    }
    ComFunController.queryStoragePrivs(fromsql, (String)pMap.get("storagePrivs"), "sge", "id");
    ComFunController.queryStaffPrivs(fromsql, (String)pMap.get("staffPrivs"), "stf", "id");
    
    fromsql.append(" ORDER BY " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, selectSql.toString(), fromsql.toString(), this.MAP, new Object[0]);
  }
}
