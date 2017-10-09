package com.aioerp.model.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.util.List;

public class PayType
  extends BaseDbModel
{
  public static final PayType dao = new PayType();
  public static final String TABLE_NAME = "cw_pay_type";
  
  public Model lastModel(String configName)
  {
    return dao.findFirst(configName, "select * from cw_pay_type");
  }
  
  public List<Record> payTypes(String configName, String accountPrivs)
  {
    StringBuffer sql = new StringBuffer();
    sql.append("select * from b_accounts where (pids like '%{0}{1}{3}%' or pids like '%{0}{1}{4}%') and id<>3 and id<>4 and node=" + AioConstants.NODE_1);
    ComFunController.basePrivsSql(sql, accountPrivs, null, "id");
    List<Record> fixs = Db.use(configName).find(sql.toString());
    return fixs;
  }
  
  public List<Model> getListByBillId(String configName, Integer billId, Integer type)
  {
    Integer accountCashPid = Db.use(configName).queryInt("select acc.id from b_accounts acc where  acc.type= '0003'");
    if (accountCashPid == null) {
      accountCashPid = Integer.valueOf(0);
    }
    Integer accountBankPid = Db.use(configName).queryInt("select acc.id from b_accounts acc where  acc.type= '0004'");
    if (accountBankPid == null) {
      accountBankPid = Integer.valueOf(0);
    }
    StringBuffer sql = new StringBuffer("select * from cw_pay_type as pay");
    sql.append(" left join b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" where 1=1 ");
    if (billId != null) {
      sql.append(" and billId = " + billId);
    }
    if (type != null) {
      sql.append(" and billTypeId = " + type);
    }
    sql.append(" and (accounts.pids like '%{" + accountCashPid + "}%' or accounts.pids like '%{" + accountBankPid + "}%')");
    return dao.find(configName, sql.toString());
  }
  
  public List<Model> getListByBillId1(String configName, Integer billId, Integer type, Integer accountType)
  {
    StringBuffer sql = new StringBuffer("select * from cw_pay_type where 1=1");
    if (billId != null) {
      sql.append(" and billId = " + billId);
    }
    if (type != null) {
      sql.append(" and billTypeId = " + type);
    }
    if (accountType != null) {
      sql.append(" and accountType=" + accountType);
    }
    return dao.find(configName, sql.toString());
  }
  
  public List<Record> billAccountDetail(String configName, int billTypeId, int billId)
  {
    StringBuffer sb = new StringBuffer("");
    sb.append("SELECT accountFullName,SUM(money) money FROM ");
    sb.append("(");
    sb.append("SELECT ba.id bId,ba.type btype,ba.fullName accountFullName,");
    sb.append("CASE WHEN pt.type=0 THEN pt.payMoney ELSE pt.payMoney*-1 END money FROM cw_pay_type pt");
    sb.append(" LEFT JOIN b_accounts ba ON pt.accountId=ba.id");
    sb.append(" where pt.billTypeId=" + billTypeId + " and billId=" + billId);
    sb.append(") temp");
    sb.append(" GROUP BY  temp.btype");
    sb.append(" ORDER BY bId ASC");
    
    return Db.use(configName).find(sb.toString());
  }
  
  public List<Record> getPayDetailByBillAndType(String configName, int billTypeId, int billId)
  {
    String sql = "SELECT * FROM cw_pay_type WHERE billTypeId = ? AND billId = ?";
    return Db.use(configName).find(sql, new Object[] { Integer.valueOf(billTypeId), Integer.valueOf(billId) });
  }
  
  public int delete(String configName, Integer billId, Integer type)
  {
    StringBuffer sql = new StringBuffer("delete from cw_pay_type where 1=1");
    if (billId != null) {
      sql.append(" and billId = " + billId);
    } else {
      sql.append(" and billId = 0");
    }
    if (type != null) {
      sql.append(" and billTypeId = " + type);
    } else {
      sql.append(" and billTypeId = 0");
    }
    return Db.use(configName).update(sql.toString());
  }
}
