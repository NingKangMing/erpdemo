package com.aioerp.model.sys;

import com.aioerp.model.BaseDbModel;
import com.aioerp.util.DateUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class BillCodeFlow
  extends BaseDbModel
{
  public static final BillCodeFlow dao = new BillCodeFlow();
  private static final String TABLE = "sys_billcode_flow";
  
  public BillCodeFlow getObj(String configName, int billId, String type)
  {
    if (StringUtils.isBlank(type)) {
      type = "1";
    }
    StringBuffer sql = new StringBuffer("select * from sys_billcode_flow where 1=1");
    List<Object> params = new ArrayList();
    sql.append(" and billId =?");
    params.add(Integer.valueOf(billId));
    sql.append(" and type =?");
    params.add(type);
    return (BillCodeFlow)dao.findFirst(configName, sql.toString(), params.toArray());
  }
  
  public int getBillMaxCodeNum(String configName, Date date, String billTabName, String mode)
  {
    String draftBillTabName = billTabName.replace("_bill", "_draft_bill");
    int yy = DateUtils.getYear(date);
    int mm = DateUtils.getMonth(date);
    int dd = DateUtils.getDay(date);
    
    String recodeDate = "updateTime";
    if ("cc_takeStock_bill".equals(billTabName)) {
      recodeDate = "createTime";
    }
    StringBuilder sql = new StringBuilder("SELECT IFNULL(MAX(num),0)+1 num FROM (");
    sql.append("SELECT YEAR(" + recodeDate + ") yy,MONTH(" + recodeDate + ") mm,DAY(" + recodeDate + ") dd,codeIncrease num FROM " + billTabName);
    if ((!"cg_bought_bill".equals(billTabName)) && (!"xs_sellbook_bill".equals(billTabName)) && (!"cc_takeStock_bill".equals(billTabName)))
    {
      sql.append(" UNION ALL");
      sql.append(" SELECT YEAR(" + recodeDate + ") yy,MONTH(" + recodeDate + ") mm,DAY(" + recodeDate + ") dd,codeIncrease num FROM " + draftBillTabName);
    }
    sql.append(") temp WHERE 1=1");
    
    sql.append(" AND yy = " + yy);
    if (("1".equals(mode)) || ("0".equals(mode))) {
      sql.append(" AND mm = " + mm);
    }
    if ("0".equals(mode)) {
      sql.append(" AND dd = " + dd);
    }
    Record r = Db.use(configName).findFirst(sql.toString());
    Long num = r.getLong("num");
    return num.intValue();
  }
  
  @Before({Tx.class})
  public void batchUpdateZCodeNum(String configName, String codeIncreaseRule)
  {
    Date currDate = new Date();
    List<Model> typeList = BillType.dao.getList(configName, "", 3);
    for (Model t : typeList)
    {
      int id = t.getInt("id").intValue();
      String tableName = t.getStr("biillTableName");
      int num = dao.getBillMaxCodeNum(configName, currDate, tableName, codeIncreaseRule);
      num++;
      BillCodeFlow codeFlow = dao.getObj(configName, id, "z");
      if (codeFlow == null)
      {
        codeFlow = new BillCodeFlow();
        codeFlow.set("codeIncrease", Integer.valueOf(num));
        codeFlow.set("type", "z");
        codeFlow.set("updateDate", currDate);
        codeFlow.set("billId", Integer.valueOf(id));
        codeFlow.save(configName);
      }
      else
      {
        codeFlow.set("codeIncrease", Integer.valueOf(num));
        codeFlow.set("updateDate", currDate);
        codeFlow.update(configName);
      }
    }
  }
  
  @Before({Tx.class})
  public void batchDelZNum(String configName)
  {
    String sql = "UPDATE sys_billcode_flow SET codeIncrease = 1,updateDate=DATE(NOW()) WHERE TYPE='z'";
    Db.use(configName).update(sql);
  }
}
