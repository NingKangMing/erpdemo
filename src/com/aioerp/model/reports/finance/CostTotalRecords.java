package com.aioerp.model.reports.finance;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Staff;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class CostTotalRecords
  extends BaseDbModel
{
  public static final CostTotalRecords dao = new CostTotalRecords();
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
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws SQLException, Exception
  {
    Integer accountId = Db.use(configName).queryInt("select id from b_accounts where type=" + ACCOUNT_TYPE + " limit 1");
    if (accountId == null) {
      return null;
    }
    Map<String, Class<? extends Model>> alias = new HashMap();
    StringBuffer sel = new StringBuffer("select accounts.* ");
    StringBuffer sql = new StringBuffer("from b_accounts as accounts");
    String monthMoneySql = getMoneySql(map);
    String allMoneySql = getMoneySql(null);
    sel.append(" ,(select sum(money) from (" + monthMoneySql + ") as monthMoney where monthMoney.pids like concat('%{',accounts.id,'}%')) as monthMoneys");
    sel.append(" ,(select sum(money) from (" + allMoneySql + ") as allMoney where allMoney.pids like concat('%{',accounts.id,'}%')) as allMoneys");
    sql.append(" where accounts.pids like '%{" + accountId + "}%'");
    sql.append(" order by pids");
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, new Object[0]);
  }
  
  public Map<String, Object> getDetailPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("staff", Staff.class);
    alias.put("accounts", Accounts.class);
    StringBuffer sel = new StringBuffer("select his.*,case when pay.type = 0 then sum(pay.payMoney) else null  end as addMoney");
    sel.append(",case when pay.type != 0 then sum(pay.payMoney) else null  end as subMoney");
    sel.append(",billType.name as billTypeName,staff.*,accounts.*");
    StringBuffer sql = new StringBuffer(" from cw_pay_type as pay");
    sql.append(" left join bb_billhistory as his on his.billId = pay.billId and his.billTypeId = pay.billTypeId");
    sql.append(" left join sys_billtype as billType on his.billTypeId = billType.id");
    sql.append(" left join b_staff as staff on staff.id = his.staffId");
    sql.append(" left join b_accounts as accounts on accounts.id = pay.accountId");
    
    sql.append(" where 1=1");
    detailQuery(sql, map);
    sql.append(" group by pay.accountId,his.billId,pay.billTypeId");
    rank(sql, map);
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, new Object[0]);
  }
  
  public BigDecimal getBeforeMoney(String configName, int pageNum, int numPerPage, Map<String, Object> map)
  {
    if (pageNum <= 1) {
      return BigDecimal.ZERO;
    }
    int limit = (pageNum - 1) * numPerPage;
    
    StringBuffer sql = new StringBuffer(" select sum(t.money)as money from( ");
    sql.append("  select sum(case when pay.type = 0 then pay.payMoney else pay.payMoney*-1 end) as money from cw_pay_type as pay");
    sql.append(" left join bb_billhistory as his on his.billId = pay.billId and his.billTypeId = pay.billTypeId");
    sql.append(" left join b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" where 1=1");
    detailQuery(sql, map);
    sql.append(" group by pay.accountId,his.billId,pay.billTypeId");
    sql.append(" limit " + limit);
    rank(sql, map);
    sql.append(") as t");
    return Db.use(configName).queryBigDecimal(sql.toString());
  }
  
  private void detailQuery(StringBuffer sql, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    if (map.get("startTime") != null) {
      sql.append(" and his.recodeDate>='" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and his.recodeDate<='" + map.get("endTime") + " 23:59:59'");
    }
    if (map.get("accountId") != null) {
      sql.append(" and accounts.pids like '%{" + map.get("accountId") + "}%'");
    }
    if (map.get("isRcw") != null) {
      sql.append(" and his.isRcw = " + map.get("isRcw"));
    }
  }
  
  private String getMoneySql(Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer();
    if (map != null)
    {
      sql.append("select sum(case when cw.type=0 then  cw.payMoney else cw.payMoney*-1 end) as money,cw.accountId,account.pids from cw_pay_type as cw");
      sql.append(" left join b_accounts as account on account.id = cw.accountId ");
      
      sql.append(" where 1=1");
      if (map.get("startDate") != null) {
        sql.append(" and cw.payTime >='" + map.get("startDate") + "'");
      }
      if (map.get("endDate") != null) {
        sql.append(" and cw.payTime <='" + map.get("endDate") + "'");
      }
    }
    else
    {
      sql.append("select (case when sum(case when cw.type=0 then  cw.payMoney else cw.payMoney*-1 end) is null then 0 else sum(case when cw.type=0 then  cw.payMoney else cw.payMoney*-1 end) end )+ (case when account.money is null then 0 else account.money end)  as money,cw.accountId,account.pids from cw_pay_type as cw");
      sql.append(" left join b_accounts as account on account.id = cw.accountId ");
    }
    sql.append(" group by cw.accountId");
    return sql.toString();
  }
  
  private void rank(StringBuffer sql, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    String orderField = (String)map.get("orderField");
    String orderDirection = (String)map.get("orderDirection");
    if ((StringUtils.isBlank(orderField)) || (StringUtils.isBlank(orderDirection))) {
      return;
    }
    if ("staffCode".equals(orderField)) {
      sql.append(" order by staff.code  " + orderDirection);
    } else if ("staffName".equals(orderField)) {
      sql.append(" order by staff.name  " + orderDirection);
    } else if ("accountsCode".equals(orderField)) {
      sql.append(" order by accounts.code  " + orderDirection);
    } else if ("accountsFullName".equals(orderField)) {
      sql.append(" order by accounts.fullName  " + orderDirection);
    } else {
      sql.append(" order by " + orderField + " " + orderDirection);
    }
  }
}
