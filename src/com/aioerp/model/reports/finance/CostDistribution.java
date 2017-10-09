package com.aioerp.model.reports.finance;

import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostDistribution
  extends BaseDbModel
{
  public static final CostDistribution dao = new CostDistribution();
  private static String ACCOUNT_TYPE = "00036";
  
  public int getGrade()
  {
    String pids = getStr("pids");
    if (pids == null) {
      return 0;
    }
    String[] pidArra = pids.split("}");
    return pidArra.length - 2;
  }
  
  public String getBlank()
  {
    int grade = getGrade() - 1;
    String blank = "";
    for (int i = 0; i < grade; i++) {
      blank = blank + "&nbsp;&nbsp;&nbsp;&nbsp;";
    }
    return blank;
  }
  
  public Map<String, Object> getDepmPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws SQLException, Exception
  {
    Integer accountId = Db.use(configName).queryInt("select id from b_accounts where type=" + ACCOUNT_TYPE + " limit 1");
    if (accountId == null) {
      return null;
    }
    Map<String, Class<? extends Model>> alias = new HashMap();
    
    StringBuffer sel = new StringBuffer("select accounts.*");
    sel.append(" ,(select sum(payMoney) from (" + getPaySql(accountId, map) + ") as accountPay where accountPay.pids like concat('%{',accounts.id,'}%')");
    ComFunController.queryDepartmentPrivs(sel, (String)map.get("departmentPrivs"), "accountPay");
    sel.append(" ) as allMoney");
    StringBuffer sql = new StringBuffer("from b_accounts as accounts");
    sql.append(" left join cw_pay_type as payType on payType.accountId = accounts.id");
    
    sql.append(" where accounts.pids like '%{" + accountId + "}%'");
    ComFunController.basePrivsSql(sql, (String)map.get("accountPrivs"), "accounts");
    
    sql.append(" group by payType.accountId");
    

    String payDepmSql = getPayDepmSql(accountId, map);
    List<Record> depmList = getPayDepm(configName, (String)map.get("departmentPrivs"));
    Record obj = new Record();
    obj.set("id", Integer.valueOf(0));
    obj.set("fullName", "其它部门");
    depmList.add(obj);
    for (int i = 0; i < depmList.size(); i++)
    {
      Integer depmId = ((Record)depmList.get(i)).getInt("id");
      sel.append(",(select sum(payMoney) from (" + payDepmSql + ") as payDepm where payDepm.pids like concat('%{',accounts.id,'}%')");
      if (depmId.intValue() == 0) {
        sel.append(" and (payDepm.departmentId is null or payDepm.departmentId = " + depmId + ")) as money" + depmId);
      } else {
        sel.append(" and payDepm.departmentId = " + depmId + ") as money" + depmId);
      }
    }
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, new Object[0]);
  }
  
  public Map<String, Object> getStaffPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws SQLException, Exception
  {
    Integer accountId = Db.use(configName).queryInt("select id from b_accounts where type=" + ACCOUNT_TYPE + " limit 1");
    if (accountId == null) {
      return null;
    }
    Map<String, Class<? extends Model>> alias = new HashMap();
    
    StringBuffer sel = new StringBuffer("select accounts.*");
    sel.append(" ,(select sum(payMoney) from (" + getPaySql(accountId, map) + ") as accountPay where accountPay.pids like concat('%{',accounts.id,'}%')");
    ComFunController.queryStaffPrivs(sel, (String)map.get("staffPrivs"), "accountPay");
    sel.append(" ) as allMoney");
    
    StringBuffer sql = new StringBuffer("from b_accounts as accounts");
    sql.append(" left join cw_pay_type as payType on payType.accountId = accounts.id");
    
    sql.append(" where accounts.pids like '%{" + accountId + "}%'");
    ComFunController.basePrivsSql(sql, (String)map.get("accountPrivs"), "accounts");
    
    sql.append(" group by payType.accountId");
    

    String payStaffSql = getPayStaffSql(accountId, map);
    List<Record> staffList = getPayStaff(configName, (String)map.get("staffPrivs"));
    Record obj = new Record();
    obj.set("id", Integer.valueOf(0));
    obj.set("fullName", "其它职员");
    staffList.add(obj);
    for (int i = 0; i < staffList.size(); i++)
    {
      Integer staffId = ((Record)staffList.get(i)).getInt("id");
      if (staffId.intValue() == 0) {
        sel.append(",(select sum(payMoney) from (" + payStaffSql + ") as payStaff where payStaff.pids like concat('%{',accounts.id,'}%') and (payStaff.staffId is null or payStaff.staffId = " + staffId + ")) as money" + staffId);
      } else {
        sel.append(",(select sum(payMoney) from (" + payStaffSql + ") as payStaff where payStaff.pids like concat('%{',accounts.id,'}%') and payStaff.staffId = " + staffId + ") as money" + staffId);
      }
    }
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, new Object[0]);
  }
  
  public Map<String, Object> getUnitPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws SQLException, Exception
  {
    Integer accountId = Db.use(configName).queryInt("select id from b_accounts where type=" + ACCOUNT_TYPE + " limit 1");
    if (accountId == null) {
      return null;
    }
    Map<String, Class<? extends Model>> alias = new HashMap();
    
    StringBuffer sel = new StringBuffer("select accounts.*");
    sel.append(" ,(select sum(payMoney) from (" + getPaySql(accountId, map) + ") as accountPay where accountPay.pids like concat('%{',accounts.id,'}%')");
    ComFunController.queryUnitPrivs(sel, (String)map.get("unitPrivs"), "accountPay");
    sel.append(" ) as allMoney");
    
    StringBuffer sql = new StringBuffer("from b_accounts as accounts");
    sql.append(" left join cw_pay_type as payType on payType.accountId = accounts.id");
    
    sql.append(" where accounts.pids like '%{" + accountId + "}%'");
    ComFunController.basePrivsSql(sql, (String)map.get("accountPrivs"), "accounts");
    
    sql.append(" group by payType.accountId");
    

    String payUnitSql = getPayUnitSql(accountId, map);
    List<Record> unitList = getPayUnit(configName, (String)map.get("unitPrivs"));
    for (int i = 0; i < unitList.size(); i++)
    {
      Integer unitId = ((Record)unitList.get(i)).getInt("id");
      sel.append(",(select sum(payMoney) from (" + payUnitSql + ") as payUnit where payUnit.pids like concat('%{',accounts.id,'}%') and payUnit.unitId = " + unitId + ") as money" + unitId);
    }
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, new Object[0]);
  }
  
  public Map<String, Object> getAreaPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws SQLException, Exception
  {
    Integer accountId = Db.use(configName).queryInt("select id from b_accounts where type=" + ACCOUNT_TYPE + " limit 1");
    if (accountId == null) {
      return null;
    }
    Map<String, Class<? extends Model>> alias = new HashMap();
    
    StringBuffer sel = new StringBuffer("select accounts.*");
    sel.append(" ,(select sum(payMoney) from (" + getPayAreaAllSql(accountId, map) + ") as accountPay where accountPay.pids like concat('%{',accounts.id,'}%')");
    ComFunController.queryAreaPrivs(sel, (String)map.get("areaPrivs"), "accountPay", "areaId");
    sel.append(" ) as allMoney");
    
    StringBuffer sql = new StringBuffer("from b_accounts as accounts");
    sql.append(" left join cw_pay_type as payType on payType.accountId = accounts.id");
    
    sql.append(" where accounts.pids like '%{" + accountId + "}%'");
    ComFunController.basePrivsSql(sql, (String)map.get("accountPrivs"), "accounts");
    
    sql.append(" group by payType.accountId");
    

    String payAreaSql = getPayAreaSql(accountId, map);
    List<Record> areaList = getPayArea(configName, (String)map.get("areaPrivs"));
    Record obj = new Record();
    obj.set("id", Integer.valueOf(0));
    obj.set("fullName", "其它地区");
    areaList.add(obj);
    for (int i = 0; i < areaList.size(); i++)
    {
      Integer areaId = ((Record)areaList.get(i)).getInt("id");
      if (areaId.intValue() == 0) {
        sel.append(",(select sum(payMoney) from (" + payAreaSql + ") as payArea where payArea.pids like concat('%{',accounts.id,'}%') and  (payArea.areaId is null or payArea.areaId = " + areaId + ")) as money" + areaId);
      } else {
        sel.append(",(select sum(payMoney) from (" + payAreaSql + ") as payArea where payArea.pids like concat('%{',accounts.id,'}%') and payArea.areaId = " + areaId + ") as money" + areaId);
      }
    }
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, new Object[0]);
  }
  
  public List<Record> getPayDepm(String configName, String privs)
  {
    Integer accountId = Db.use(configName).queryInt("select id from b_accounts where type=" + ACCOUNT_TYPE + " limit 1");
    if (accountId == null) {
      return null;
    }
    StringBuffer sql = new StringBuffer("select department.* from b_department as department");
    
    sql.append(" left join cw_pay_type as pay on pay.departmentId = department.id ");
    sql.append(" left join  b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" where accounts.pids like '%{" + accountId + "}%'");
    sql.append(" and pay.departmentId is not null");
    sql.append(" and department.node = 1");
    ComFunController.basePrivsSql(sql, privs, "department");
    sql.append(" group by department.id");
    return Db.use(configName).find(sql.toString());
  }
  
  public List<Record> getPayStaff(String configName, String privs)
  {
    Integer accountId = Db.use(configName).queryInt("select id from b_accounts where type=" + ACCOUNT_TYPE + " limit 1");
    if (accountId == null) {
      return null;
    }
    StringBuffer sql = new StringBuffer("select staff.* from b_staff as staff");
    
    sql.append(" left join cw_pay_type as pay on pay.staffId = staff.id ");
    sql.append(" left join  b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" where accounts.pids like '%{" + accountId + "}%'");
    sql.append(" and pay.staffId is not null");
    sql.append(" and staff.node = 1");
    ComFunController.basePrivsSql(sql, privs, "staff");
    sql.append(" group by staff.id");
    return Db.use(configName).find(sql.toString());
  }
  
  public List<Record> getPayUnit(String configName, String privs)
  {
    Integer accountId = Db.use(configName).queryInt("select id from b_accounts where type=" + ACCOUNT_TYPE + " limit 1");
    if (accountId == null) {
      return null;
    }
    StringBuffer sql = new StringBuffer("select unit.* from b_unit as unit");
    
    sql.append(" left join cw_pay_type as pay on pay.unitId = unit.id ");
    sql.append(" left join  b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" where accounts.pids like '%{" + accountId + "}%'");
    sql.append(" and pay.unitId is not null");
    sql.append(" and unit.node = 1");
    ComFunController.basePrivsSql(sql, privs, "unit");
    sql.append(" group by unit.id");
    return Db.use(configName).find(sql.toString());
  }
  
  public List<Record> getPayArea(String configName, String privs)
  {
    Integer accountId = Db.use(configName).queryInt("select id from b_accounts where type=" + ACCOUNT_TYPE + " limit 1");
    if (accountId == null) {
      return null;
    }
    StringBuffer sql = new StringBuffer("select area.* from b_area as area");
    
    sql.append(" left join b_unit as unit on unit.areaId = area.id");
    sql.append(" left join cw_pay_type as pay on pay.unitId = unit.id ");
    sql.append(" left join  b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" where accounts.pids like '%{" + accountId + "}%'");
    sql.append(" and unit.areaId is not null");
    sql.append(" and area.node = 1");
    ComFunController.basePrivsSql(sql, privs, "area");
    sql.append(" group by area.id");
    return Db.use(configName).find(sql.toString());
  }
  
  private String getPayDepmSql(Integer accountId, Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer("select sum(case when pay.type =0 then pay.payMoney else pay.payMoney*-1 end) as payMoney,acc.*,pay.departmentId from cw_pay_type as pay");
    sql.append(" left join b_accounts as acc on acc.id = pay.accountId ");
    sql.append(" where acc.pids like '%{" + accountId + "}%'");
    
    query(sql, map);
    sql.append(" group by pay.accountId ,pay.departmentId");
    return sql.toString();
  }
  
  private String getPayStaffSql(Integer accountId, Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer("select sum(case when pay.type =0 then pay.payMoney else pay.payMoney*-1 end) as payMoney,acc.*,pay.staffId from cw_pay_type as pay");
    sql.append(" left join b_accounts as acc on acc.id = pay.accountId ");
    sql.append(" where acc.pids like '%{" + accountId + "}%'");
    
    query(sql, map);
    sql.append(" group by pay.accountId ,pay.staffId");
    return sql.toString();
  }
  
  private String getPayUnitSql(Integer accountId, Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer("select sum(case when pay.type =0 then pay.payMoney else pay.payMoney*-1 end) as payMoney,acc.*,pay.unitId from cw_pay_type as pay");
    sql.append(" left join b_accounts as acc on acc.id = pay.accountId ");
    sql.append(" where acc.pids like '%{" + accountId + "}%'");
    sql.append(" and pay.unitId is not null");
    query(sql, map);
    sql.append(" group by pay.accountId ,pay.unitId");
    return sql.toString();
  }
  
  private String getPayAreaSql(Integer accountId, Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer("select sum(case when pay.type =0 then pay.payMoney else pay.payMoney*-1 end) as payMoney,acc.*,u.areaId from cw_pay_type as pay");
    sql.append(" left join b_accounts as acc on acc.id = pay.accountId ");
    sql.append(" left join b_unit as u on u.id = pay.unitId ");
    sql.append(" where acc.pids like '%{" + accountId + "}%'");
    
    query(sql, map);
    sql.append(" group by pay.accountId ,u.areaId");
    return sql.toString();
  }
  
  private String getPaySql(Integer accountId, Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer("select sum(case when pay.type =0 then pay.payMoney else pay.payMoney*-1 end) as payMoney,acc.* from cw_pay_type as pay");
    sql.append(" left join b_accounts as acc on acc.id = pay.accountId ");
    sql.append(" where acc.pids like '%{" + accountId + "}%'");
    










    query(sql, map);
    sql.append(" group by pay.accountId");
    return sql.toString();
  }
  
  private String getPayAreaAllSql(Integer accountId, Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer("select sum(case when pay.type =0 then pay.payMoney else pay.payMoney*-1 end) as payMoney,acc.*,u.areaId from cw_pay_type as pay");
    sql.append(" left join b_accounts as acc on acc.id = pay.accountId ");
    sql.append(" left join b_unit as u on u.id = pay.unitId ");
    sql.append(" where acc.pids like '%{" + accountId + "}%'");
    
    query(sql, map);
    sql.append(" group by pay.accountId");
    return sql.toString();
  }
  
  private void query(StringBuffer sql, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    if (map.get("startTime") != null) {
      sql.append(" and payTime >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and payTime <='" + map.get("endTime") + " 23:59:59'");
    }
  }
}
