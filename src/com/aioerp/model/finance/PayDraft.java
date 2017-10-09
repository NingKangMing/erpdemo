package com.aioerp.model.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class PayDraft
  extends BaseDbModel
{
  public static final PayDraft dao = new PayDraft();
  public static final String TABLE_NAME = "cw_pay_draft";
  
  public List<Record> payTypes(String configName)
  {
    List<Record> fixs = Db.use(configName).find("select * from b_accounts where type in ('0006','00017')");
    Record record = Db.use(configName).findFirst("select id from b_accounts where type='0004'");
    int bankId = record.getInt("id").intValue();
    List<Record> banks = Db.use(configName).find("select * from b_accounts where pids like '%{" + bankId + "}%' and id<>" + bankId + " and node=" + AioConstants.NODE_1);
    fixs.addAll(banks);
    return fixs;
  }
  
  public List<Model> getListByBillId(String configName, Integer billId, Integer type)
  {
    StringBuffer sql = new StringBuffer("select * from cw_pay_draft cd");
    sql.append(" LEFT JOIN b_accounts  ba ON cd.accountId = ba.id");
    sql.append(" where 1=1");
    if (billId != null) {
      sql.append(" and cd.billId = " + billId);
    }
    if (type != null) {
      sql.append(" and cd.billTypeId = " + type);
    }
    sql.append(" AND (pids LIKE CONCAT('{0}{1}{3}','%') OR pids LIKE CONCAT('{0}{1}{4}','%'))");
    return dao.find(configName, sql.toString());
  }
  
  public List<Model> getListByBillId(String configName, Integer billId, Integer type, Integer accountId)
  {
    StringBuffer sql = new StringBuffer("select * from cw_pay_draft where 1=1");
    if (billId != null) {
      sql.append(" and billId = " + billId);
    }
    if (type != null) {
      sql.append(" and billTypeId = " + type);
    }
    if (accountId != null) {
      sql.append(" and accountId = " + accountId);
    }
    return dao.find(configName, sql.toString());
  }
  
  public int delete(String configName, Integer billId, Integer type)
  {
    StringBuffer sql = new StringBuffer("delete from cw_pay_draft where 1=1");
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
  
  public void addPayRecords(String configName, Integer type, int billTypeId, int billId, Integer unitId, Integer accountId, BigDecimal payMoney, Date payTime)
  {
    PayDraft pay = new PayDraft();
    pay.set("type", type);
    pay.set("billTypeId", Integer.valueOf(billTypeId));
    pay.set("billId", Integer.valueOf(billId));
    pay.set("unitId", unitId);
    pay.set("accountId", accountId);
    pay.set("payMoney", payMoney);
    pay.set("payTime", payTime);
    pay.save(configName);
  }
  
  public void editPayRecords(String configName, PayDraft pay, int type, int billTypeId, int billId, Integer unitId, Integer accountId, BigDecimal payMoney, Date payTime)
  {
    pay.set("type", Integer.valueOf(type));
    pay.set("billTypeId", Integer.valueOf(billTypeId));
    pay.set("billId", Integer.valueOf(billId));
    pay.set("unitId", unitId);
    pay.set("accountId", accountId);
    pay.set("payMoney", payMoney);
    pay.set("payTime", payTime);
    pay.update(configName);
  }
}
