package com.aioerp.model.reports.finance;

import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class YearCostTotalRecords
  extends BaseDbModel
{
  public static final YearCostTotalRecords dao = new YearCostTotalRecords();
  
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
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, Integer[] yearEndIds, List<String> accTypes, String accountPrivs)
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
    for (Integer yearEndId : yearEndIds)
    {
      String monthMoneySql = getMoneySql(configName, yearEndId);
      sel.append(" ,(select sum(money) from (" + monthMoneySql + ") as monthMoney where monthMoney.pids like concat('%{',accounts.id,'}%')) as yearMoney" + yearEndId);
    }
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
  
  public List<Record> getList(String configName, Integer[] yearEndIds, List<String> accTypes, String accountPrivs)
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
    for (Integer yearEndId : yearEndIds)
    {
      String monthMoneySql = getMoneySql(configName, yearEndId);
      sel.append(" ,(select sum(money) from (" + monthMoneySql + ") as monthMoney where monthMoney.pids like concat('%{',accounts.id,'}%')) as yearMoney" + yearEndId);
      if ((yearEndId == null) || (yearEndId.intValue() == 0)) {
        sel.append(" ,(select sum(accountsInit.money) from b_accounts as mset  left join cw_accounts_init as accountsInit on accountsInit.accountsId = mset.id  where mset.pids like concat('%{',accounts.id,'}%')) as setMoneys");
      }
    }
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
  
  private String getMoneySql(String configName, Integer yearEndId)
    throws ParseException
  {
    StringBuffer sql = new StringBuffer();
    YearEnd y = (YearEnd)YearEnd.dao.findById(configName, yearEndId);
    if (y != null)
    {
      sql.append("select (case when sum(case when cw.type=0 then  cw.payMoney else cw.payMoney*-1 end) is null then 0 else sum(case when cw.type=0 then  cw.payMoney else cw.payMoney*-1 end) end )+ (case when account.money is null then 0 else account.money end)  as money,cw.accountId,account.pids from cw_pay_type as cw");
      sql.append(" left join b_accounts as account on account.id = cw.accountId ");
      
      sql.append(" where 1=1");
      Date startDate = y.getDate("startDate");
      if (startDate != null) {
        sql.append(" and cw.payTime >='" + DateUtils.format(startDate) + "'");
      }
      Date endDate = y.getDate("endDate");
      if (endDate != null) {
        sql.append(" and cw.payTime <='" + DateUtils.format(endDate) + " 23:59:59'");
      }
    }
    else
    {
      sql.append("select (case when sum(case when cw.type=0 then  cw.payMoney else cw.payMoney*-1 end) is null then 0 else sum(case when cw.type=0 then  cw.payMoney else cw.payMoney*-1 end) end )+ (case when account.money is null then 0 else account.money end)  as money,cw.accountId,account.pids from cw_pay_type as cw");
      sql.append(" left join b_accounts as account on account.id = cw.accountId ");
      sql.append(" where 1=1");
      
      y = (YearEnd)YearEnd.dao.lastModel(configName);
      if (y != null)
      {
        Date endDate = y.getDate("endDate");
        endDate = DateUtils.addDays(endDate, 1);
        sql.append(" and cw.payTime >='" + DateUtils.format(endDate) + "'");
      }
      sql.append(" and cw.payTime <='" + DateUtils.getCurrentDate() + " 23:59:59'");
    }
    sql.append(" group by cw.accountId");
    return sql.toString();
  }
}
