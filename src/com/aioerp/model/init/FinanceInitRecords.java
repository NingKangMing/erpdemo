package com.aioerp.model.init;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.util.List;

public class FinanceInitRecords
  extends BaseDbModel
{
  public static final FinanceInitRecords dao = new FinanceInitRecords();
  
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
      blank = blank + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    }
    return blank;
  }
  
  public List<Model> getInitList(String configName, String m)
  {
    SqlHelper helper = SqlHelper.getHelper();
    
    helper.appendSelect("SELECT ");
    helper.appendSelect(" (SELECT IFNULL(SUM(money),0) FROM b_accounts WHERE pids LIKE CONCAT(accounts.pids,'%')) moneys,");
    helper.appendSelect("accounts.*");
    helper.appendSql(" FROM b_accounts a");
    helper.appendSql(" RIGHT JOIN  b_accounts AS accounts ON accounts.pids LIKE CONCAT('%{',a.id,'}%')");
    if ("bank".equals(m)) {
      helper.appendSql(" WHERE a.type='0003' OR a.type='0004'");
    } else if ("assets".equals(m)) {
      helper.appendSql(" WHERE a.type='0002' ");
    } else {
      helper.appendSql(" WHERE (a.type='0001' OR a.type='0007' OR a.type='00011')");
    }
    helper.appendSql(" ORDER BY a.rank,accounts.pids");
    
    return manyList(configName, helper);
  }
  
  public List<Record> getInitAccount(String configName)
  {
    return Db.use(configName).find("select * from b_accounts");
  }
  
  public List<Record> getInitProduct(String configName)
  {
    return Db.use(configName).find("select * from cc_stock_init");
  }
  
  public List<Record> getInitUnit(String configName)
  {
    return Db.use(configName).find("select * from b_unit");
  }
}
