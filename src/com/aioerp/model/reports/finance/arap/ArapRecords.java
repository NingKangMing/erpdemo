package com.aioerp.model.reports.finance.arap;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Unit;
import com.aioerp.model.sys.BillType;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArapRecords
  extends BaseDbModel
{
  public static final ArapRecords dao = new ArapRecords();
  public static final String TABLE_NAME = "cw_arap_records";
  Map<String, Class<? extends Model>> MAP = new HashMap();
  
  public Model getArapRecordsByBillTypeId(String configName, int billTypeId, int billId)
  {
    return dao.findFirst(configName, "select * from cw_arap_records where 1=1  and billTypeId=? and billId=?", new Object[] { Integer.valueOf(billTypeId), Integer.valueOf(billId) });
  }
  
  public Map<String, Object> getPageByUnitPids(String configName, String unitPids, String startTime, String endTime, Integer isRcw, String listID, int pageNum, int numPerPage, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    this.MAP.put("arap", ArapRecords.class);
    this.MAP.put("bt", BillType.class);
    StringBuffer selectSql = new StringBuffer("SELECT arap.*,bt.* ");
    StringBuffer fromsql = new StringBuffer(" FROM cw_arap_records as arap LEFT JOIN sys_billtype bt ON bt.id=arap.billTypeId  LEFT JOIN b_unit ut ON ut.id=arap.unitId");
    fromsql.append(" where 1=1 ");
    fromsql.append(" and ut.pids LIKE CONCAT('" + unitPids + "','%') ");
    fromsql.append(" AND arap.recodeTime >= '" + startTime + "' AND arap.recodeTime <= '" + endTime + " 23:59:59' ");
    if (-1 != isRcw.intValue()) {
      fromsql.append(" AND arap.isRCW =" + isRcw);
    }
    fromsql.append(" ORDER BY " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, selectSql.toString(), fromsql.toString(), this.MAP, new Object[0]);
  }
  
  public ArapRecords getBeforeFirst(String configName, String unitPrivs, String recodeTime, String unitPids, Integer type)
  {
    StringBuffer sql = new StringBuffer("select *,");
    if (type.intValue() == 0)
    {
      sql.append("(IFNULL(SUM(addMoney),0)-(IFNULL(SUM(subMoney),0))) bfArapMoney,");
      sql.append("(IFNULL(SUM(addPrepay),0)-(IFNULL(SUM(subPrepay),0))) bfPrepayMoney  ");
    }
    else
    {
      sql.append("(IFNULL(SUM(subMoney),0)-(IFNULL(SUM(addMoney),0))) bfArapMoney,");
      sql.append("(IFNULL(SUM(subPrepay),0)-(IFNULL(SUM(addPrepay),0))) bfPrepayMoney  ");
    }
    sql.append(" from cw_arap_records where 1=1");
    List<Object> paras = new ArrayList();
    if (recodeTime != null)
    {
      sql.append(" and recodeTime < ?");
      paras.add(recodeTime);
    }
    if (!"".equals(unitPids))
    {
      sql.append(" AND unitId IN (select id from b_unit where pids LIKE CONCAT(?,'%')) ");
      paras.add(unitPids);
    }
    ArapRecords records = (ArapRecords)dao.findFirst(configName, sql.toString(), paras.toArray());
    

    BigDecimal bfArapMoney = records.getBigDecimal("bfArapMoney");
    BigDecimal bfPrepayMoney = records.getBigDecimal("bfPrepayMoney");
    
    Model model = Unit.dao.getUnitArapInit(configName, unitPrivs, unitPids, type);
    BigDecimal beginMoney = model.getBigDecimal("beginMoney");
    BigDecimal beginPreMoney = model.getBigDecimal("beginPreMoney");
    
    records.put("bfArapMoney", BigDecimalUtils.add(beginMoney, bfArapMoney));
    records.put("bfPrepayMoney", BigDecimalUtils.add(beginPreMoney, bfPrepayMoney));
    return records;
  }
}
