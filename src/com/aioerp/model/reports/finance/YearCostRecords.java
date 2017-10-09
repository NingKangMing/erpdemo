package com.aioerp.model.reports.finance;

import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.sys.end.MonthEnd;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YearCostRecords
  extends BaseDbModel
{
  public static final YearCostRecords dao = new YearCostRecords();
  private static String ACCOUNT_TYPE = "00036";
  
  public int getGrade()
  {
    String pids = getStr("pids");
    if (pids == null) {
      return 0;
    }
    String[] pidArra = pids.split("}");
    return pidArra.length - 1;
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
    Map<String, Class<? extends Model>> alias = new HashMap();
    String accountPrivs = (String)map.get("accountPrivs");
    List<Integer> accIds = Db.use(configName).query("select id from b_accounts where type =" + ACCOUNT_TYPE);
    
    List<Model> monthEndList = MonthEnd.dao.getList(configName);
    
    StringBuffer sel = new StringBuffer("select accounts.* ");
    StringBuffer sql = new StringBuffer(" from b_accounts as accounts");
    if ((monthEndList != null) && (monthEndList.size() > 0))
    {
      for (int i = 0; i < monthEndList.size(); i++)
      {
        Model model = (Model)monthEndList.get(i);
        map = new HashMap();
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
      map = new HashMap();
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
    map = new HashMap();
    Model yearEnd = YearEnd.dao.lastModel(configName);
    if (yearEnd != null)
    {
      Date startDate = yearEnd.getDate("endDate");
      startDate = DateUtils.addDays(startDate, 1);
      map.put("startDate", DateUtils.format(startDate));
    }
    String monthMoneySql = getMoneySql(map);
    sel.append(" ,(select sum(money) from (" + monthMoneySql + ") as monthMoney where monthMoney.pids like concat('%{',accounts.id,'}%')) as allMonthMoney");
    
    sql.append(" where 1=1 ");
    ComFunController.basePrivsSql(sql, accountPrivs, "accounts");
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
  
  public List<Record> getList(String configName, Map<String, Object> map)
    throws ParseException
  {
    String accountPrivs = (String)map.get("accountPrivs");
    List<Integer> accIds = Db.use(configName).query("select id from b_accounts where type =" + ACCOUNT_TYPE);
    
    List<Model> monthEndList = MonthEnd.dao.getList(configName);
    
    StringBuffer sel = new StringBuffer("select accounts.* ");
    StringBuffer sql = new StringBuffer(" from b_accounts as accounts");
    if ((monthEndList != null) && (monthEndList.size() > 0))
    {
      for (int i = 0; i < monthEndList.size(); i++)
      {
        Model model = (Model)monthEndList.get(i);
        map = new HashMap();
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
      map = new HashMap();
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
    map = new HashMap();
    Model yearEnd = YearEnd.dao.lastModel(configName);
    if (yearEnd != null)
    {
      Date startDate = yearEnd.getDate("endDate");
      startDate = DateUtils.addDays(startDate, 1);
      map.put("startDate", DateUtils.format(startDate));
    }
    String monthMoneySql = getMoneySql(map);
    sel.append(" ,(select sum(money) from (" + monthMoneySql + ") as monthMoney where monthMoney.pids like concat('%{',accounts.id,'}%')) as allMonthMoney");
    
    sql.append(" where 1=1 ");
    ComFunController.basePrivsSql(sql, accountPrivs, "accounts");
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
}
