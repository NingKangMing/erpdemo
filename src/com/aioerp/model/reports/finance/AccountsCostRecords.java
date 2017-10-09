package com.aioerp.model.reports.finance;

import com.aioerp.common.procedure.CommonProc;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Staff;
import com.aioerp.model.sys.end.MonthEnd;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class AccountsCostRecords
  extends BaseDbModel
{
  public static final AccountsCostRecords dao = new AccountsCostRecords();
  
  public int getGrade()
  {
    String pids = getStr("pids");
    if (pids == null) {
      return 0;
    }
    String[] pidArra = pids.split("}");
    return pidArra.length;
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
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map, List<String> accTypes)
    throws SQLException, Exception
  {
    String accTypeStr = "";
    for (int i = 0; i < accTypes.size(); i++)
    {
      if (i != 0) {
        accTypeStr = accTypeStr + ",";
      }
      accTypeStr = accTypeStr + (String)accTypes.get(i);
    }
    if (StringUtils.isBlank(accTypeStr)) {
      return new HashMap();
    }
    List<Integer> accIds = Db.use(configName).query("select id from b_accounts where type in (" + accTypeStr + ")");
    
    Map<String, Class<? extends Model>> alias = new HashMap();
    StringBuffer sel = new StringBuffer("select accounts.* ");
    StringBuffer sql = new StringBuffer("from b_accounts as accounts");
    String monthMoneySql = getMoneySql(map);
    Model yearEnd = YearEnd.dao.lastModel(configName);
    Map<String, Object> mapY = new HashMap();
    if (yearEnd != null)
    {
      Date startDate = yearEnd.getDate("endDate");
      startDate = DateUtils.addDays(startDate, 1);
      mapY.put("startDate", DateUtils.format(startDate));
    }
    String allMoneySql = getMoneySql(mapY);
    sel.append(" ,(select sum(money) from (" + monthMoneySql + ") as monthMoney where monthMoney.pids like concat('%{',accounts.id,'}%')) as monthMoneys");
    sel.append(" ,(select sum(money) from (" + allMoneySql + ") as allMoney where allMoney.pids like concat('%{',accounts.id,'}%')) as allMoneys");
    sql.append(" where 1=1 ");
    ComFunController.basePrivsSql(sql, (String)map.get("accountPrivs"), "accounts");
    if (accIds.size() > 0)
    {
      sql.append(" and (");
      for (int i = 0; i < accIds.size(); i++) {
        if (i == 0) {
          sql.append("  accounts.pids like '%{" + accIds.get(i) + "}%'");
        } else {
          sql.append(" or accounts.pids like '%{" + accIds.get(i) + "}%'");
        }
      }
      sql.append(" )");
    }
    sql.append(" order by pids");
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, new Object[0]);
  }
  
  public List<Record> getList(String configName, Map<String, Object> map, List<String> accTypes)
    throws ParseException
  {
    String accTypeStr = "";
    for (int i = 0; i < accTypes.size(); i++)
    {
      if (i != 0) {
        accTypeStr = accTypeStr + ",";
      }
      accTypeStr = accTypeStr + (String)accTypes.get(i);
    }
    if (StringUtils.isBlank(accTypeStr)) {
      return new ArrayList();
    }
    List<Integer> accIds = Db.use(configName).query("select id from b_accounts where type in (" + accTypeStr + ")");
    StringBuffer sel = new StringBuffer("select accounts.* ");
    StringBuffer sql = new StringBuffer(" from b_accounts as accounts");
    String monthMoneySql = getMoneySql(map);
    Model yearEnd = YearEnd.dao.lastModel(configName);
    Map<String, Object> mapY = new HashMap();
    if (yearEnd != null)
    {
      Date startDate = yearEnd.getDate("endDate");
      startDate = DateUtils.addDays(startDate, 1);
      mapY.put("startDate", DateUtils.format(startDate));
    }
    String allMoneySql = "";
    if ((map != null) && (map.get("isMoney") != null) && ("yes".equals(map.get("isMoney")))) {
      allMoneySql = getMoneySql(map);
    } else {
      allMoneySql = getMoneySql(mapY);
    }
    sel.append(" ,(select sum(money) from (" + monthMoneySql + ") as monthMoney where monthMoney.pids like concat('%{',accounts.id,'}%')) as monthMoneys");
    sel.append(" ,(select sum(money) from (" + allMoneySql + ") as allMoney where allMoney.pids like concat('%{',accounts.id,'}%')) as allMoneys");
    if (yearEnd == null) {
      sel.append(" ,(select sum(accountsInit.money) from b_accounts as mset left join cw_accounts_init as accountsInit on accountsInit.accountsId = mset.id where mset.pids like concat('%{',accounts.id,'}%')) as setMoneys");
    }
    sql.append(" where 1=1 ");
    if (map != null) {
      ComFunController.basePrivsSql(sql, (String)map.get("accountPrivs"), "accounts");
    }
    if (accIds.size() > 0)
    {
      sql.append(" and (");
      for (int i = 0; i < accIds.size(); i++) {
        if (i == 0) {
          sql.append("  accounts.pids like '%{" + accIds.get(i) + "}%'");
        } else {
          sql.append(" or accounts.pids like '%{" + accIds.get(i) + "}%'");
        }
      }
      sql.append(" )");
    }
    sql.append(" order by pids");
    sel.append(sql.toString());
    return Db.use(configName).find(sel.toString());
  }
  
  public Map<String, Object> getDetailPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("staff", Staff.class);
    alias.put("accounts", Accounts.class);
    StringBuffer sel = new StringBuffer("select his.*,pay.*");
    sel.append(" ,sum(case when pay.type = 0 then pay.payMoney else null  end) as addMoney");
    sel.append(" ,sum(case when pay.type != 0 then pay.payMoney else null  end) as subMoney");
    sel.append(" ,billType.name as billTypeName,staff.*,accounts.*");
    
    StringBuffer sql = new StringBuffer(" from bb_billhistory as his");
    sql.append(" left join cw_pay_type as pay on his.billId = pay.billId and his.billTypeId = pay.billTypeId");
    sql.append(" left join sys_billtype as billType on his.billTypeId = billType.id");
    sql.append(" left join b_staff as staff on staff.id = his.staffId");
    sql.append(" left join b_accounts as accounts on accounts.id = pay.accountId");
    
    sql.append(" where 1=1");
    detailQuery(sql, map);
    sql.append(" group by pay.accountId,his.billId,pay.billTypeId");
    rank(sql, map);
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, new Object[0]);
  }
  
  public Map<String, Object> getDetailPageByPro(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("staff", Staff.class);
    alias.put("accounts", Accounts.class);
    CommonProc cwPro = new CommonProc("pro_abc", pageNum, numPerPage, map);
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, cwPro, alias);
  }
  
  public BigDecimal getBeforeMoney(String configName, int pageNum, int numPerPage, Map<String, Object> map)
  {
    if (pageNum <= 1) {
      return BigDecimal.ZERO;
    }
    int limit = (pageNum - 1) * numPerPage;
    
    StringBuffer sql = new StringBuffer(" select sum(t.money)as money from( ");
    
    sql.append(" select cw.money from cw_accounts_init cw");
    sql.append(" LEFT JOIN b_accounts accounts on cw.accountsId = accounts.id where 1=1");
    sql.append(" and accounts.pids like '%{" + map.get("accountId") + "}%'");
    sql.append(" UNION all");
    sql.append(" select sum(case when pay.type = 0 then pay.payMoney else pay.payMoney*-1 end) as money from cw_pay_type as pay");
    sql.append(" left join bb_billhistory as his on his.billId = pay.billId and his.billTypeId = pay.billTypeId");
    sql.append(" left join b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" where 1=1");
    detailBeforeQuery(sql, map);
    sql.append(" group by his.recodeDate, pay.accountId,his.billId,pay.billTypeId");
    sql.append(" union all");
    sql.append(" select sum(case when pay.type = 0 then pay.payMoney else pay.payMoney*-1 end) as money from cw_pay_type as pay");
    sql.append(" left join bb_billhistory as his on his.billId = pay.billId and his.billTypeId = pay.billTypeId");
    sql.append(" left join b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" where 1=1");
    detailQuery(sql, map);
    sql.append(" group by pay.accountId,his.billId,pay.billTypeId LIMIT 0," + limit);
    rank(sql, map);
    sql.append(") as t");
    return Db.use(configName).queryBigDecimal(sql.toString());
  }
  
  public Record getContrast(String configName, Integer accountId)
    throws ParseException
  {
    if (accountId == null) {
      accountId = Integer.valueOf(0);
    }
    List<Model> monthEndList = MonthEnd.dao.getList(configName);
    
    StringBuffer sel = new StringBuffer("select accounts.* ");
    StringBuffer sql = new StringBuffer(" from b_accounts as accounts");
    if ((monthEndList != null) && (monthEndList.size() > 0))
    {
      for (int i = 0; i < monthEndList.size(); i++)
      {
        Model model = (Model)monthEndList.get(i);
        Map<String, Object> map = new HashMap();
        map.put("startDate", DateUtils.format(model.getDate("startDate")));
        map.put("endDate", DateUtils.format(model.getDate("endDate")));
        String monthMoneySql = getMoneySql(map);
        sel.append(" ,(select sum(money) from (" + monthMoneySql + ") as monthMoney where monthMoney.pids like concat('%{',accounts.id,'}%')) as monthMoney" + model.getInt("monthCount"));
        if (i + 1 == monthEndList.size())
        {
          map = new HashMap();
          Date startDate = model.getDate("endDate");
          startDate = DateUtils.addDays(startDate, 1);
          map.put("startDate", DateUtils.format(startDate));
          monthMoneySql = getMoneySql(map);
          sel.append(" ,(select sum(money) from (" + monthMoneySql + ") as monthMoney where monthMoney.pids like concat('%{',accounts.id,'}%')) as monthMoney" + model.getInt("monthCount") + 1);
        }
      }
    }
    else
    {
      Map<String, Object> map = new HashMap();
      Model yearEnd = YearEnd.dao.lastModel(configName);
      if (yearEnd != null)
      {
        Date startDate = yearEnd.getDate("endDate");
        startDate = DateUtils.addDays(startDate, 1);
        map.put("startDate", DateUtils.format(startDate));
      }
      String monthMoneySql = getMoneySql(map);
      sel.append(" ,(select sum(money) from (" + monthMoneySql + ") as monthMoney where monthMoney.pids like concat('%{',accounts.id,'}%')) as monthMoney" + 1);
    }
    sql.append(" where 1=1 ");
    sql.append(" and accounts.id = " + accountId);
    sel.append(sql.toString());
    return Db.use(configName).findFirst(sel.toString());
  }
  
  private void detailQuery(StringBuffer sql, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    if ((map.get("startTime") != null) && (map.get("endTime") != null))
    {
      sql.append(" and his.recodeDate BETWEEN '" + map.get("startTime") + "' and '" + map.get("endTime") + " 23:59:59'");
    }
    else
    {
      if (map.get("startTime") != null) {
        sql.append(" and his.recodeDate>='" + map.get("startTime") + "'");
      }
      if (map.get("endTime") != null) {
        sql.append(" and his.recodeDate<='" + map.get("endTime") + " 23:59:59'");
      }
    }
    if (map.get("accountId") != null) {
      sql.append(" and accounts.pids like '%{" + map.get("accountId") + "}%'");
    }
    if ((map.get("isRcw") != null) && (!"".equals(map.get("isRcw")))) {
      sql.append(" and his.isRcw = " + map.get("isRcw"));
    }
  }
  
  private void detailBeforeQuery(StringBuffer sql, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    if (map.get("endTime") != null) {
      sql.append(" and his.recodeDate<='" + map.get("startTime") + "'");
    }
    if (map.get("accountId") != null) {
      sql.append(" and accounts.pids like '%{" + map.get("accountId") + "}%'");
    }
    if ((map.get("isRcw") != null) && (!"".equals(map.get("isRcw")))) {
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
        sql.append(" and cw.payTime <='" + map.get("endDate") + " 23:59:59'");
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
  
  public List<BigDecimal> getAllUnitMoney(String configName, Map<String, Object> map, List<String> accTypes)
    throws ParseException
  {
    String accTypeStr = "";
    for (int i = 0; i < accTypes.size(); i++)
    {
      if (i != 0) {
        accTypeStr = accTypeStr + ",";
      }
      accTypeStr = accTypeStr + (String)accTypes.get(i);
    }
    List<Integer> accIds = Db.use(configName).query("select id from b_accounts where type in (" + accTypeStr + ")");
    StringBuffer sql = new StringBuffer();
    Model yearEnd = YearEnd.dao.lastModel(configName);
    if ((yearEnd != null) && (map.get("startDate") == null))
    {
      Date startDate = yearEnd.getDate("endDate");
      startDate = DateUtils.addDays(startDate, 1);
      map.put("startDate", DateUtils.format(startDate));
    }
    sql.append("select sum(uncw.money) as money from (");
    sql.append("select sum(");
    sql.append(" case when cw.type=0 and account.type='000413' then  cw.payMoney");
    sql.append(" when cw.type=0 and account.type!='000413' then  cw.payMoney*-1");
    sql.append(" when cw.type!=0 and account.type='000413' then  cw.payMoney *-1");
    sql.append(" when cw.type!=0 and account.type!='000413' then  cw.payMoney");
    sql.append(" end) as money,cw.unitId from cw_pay_type as cw");
    sql.append(" left join b_accounts as account on account.id = cw.accountId ");
    if (map != null)
    {
      sql.append(" where 1=1");
      if (map.get("startDate") != null) {
        sql.append(" and cw.payTime >='" + map.get("startDate") + "'");
      }
      if (map.get("endDate") != null) {
        sql.append(" and cw.payTime <='" + map.get("endDate") + " 23:59:59'");
      }
    }
    if (accIds.size() > 0)
    {
      sql.append(" and (");
      for (int i = 0; i < accIds.size(); i++) {
        if (i == 0) {
          sql.append("  account.pids like '%{" + accIds.get(i) + "}%'");
        } else {
          sql.append(" or account.pids like '%{" + accIds.get(i) + "}%'");
        }
      }
      sql.append(" )");
    }
    sql.append(" group by cw.unitId");
    
    sql.append(" UNION ALL");
    if (map != null)
    {
      sql.append(" select sum(");
      sql.append(" case when account.type='000413' then  cw.money");
      sql.append(" when account.type='00013' then  cw.money*-1");
      sql.append(" end) as money,cw.unitId from cw_accounts_init as cw");
      sql.append(" left join b_accounts as account on account.id = cw.accountsId ");
      
      sql.append(" where 1=1");
    }
    else
    {
      sql.append("select sum(");
      sql.append(" case when account.type='000413' then  cw.money");
      sql.append(" when account.type='00013' then  cw.money*-1");
      sql.append(" end) as money,cw.unitId from cw_pay_type as cw");
      sql.append(" left join b_accounts as account on account.id = cw.accountsId ");
      sql.append(" where 1=1");
    }
    if (accIds.size() > 0)
    {
      sql.append(" and (");
      for (int i = 0; i < accIds.size(); i++) {
        if (i == 0) {
          sql.append("  account.pids like '%{" + accIds.get(i) + "}%'");
        } else {
          sql.append(" or account.pids like '%{" + accIds.get(i) + "}%'");
        }
      }
      sql.append(" )");
    }
    sql.append(" group by cw.unitId");
    
    sql.append(")as uncw group by uncw.unitId ");
    
    return Db.use(configName).query(sql.toString());
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
