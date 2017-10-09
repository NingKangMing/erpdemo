package com.aioerp.model.reports.retmoney;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Staff;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ReturnMoneyUser
  extends BaseDbModel
{
  public static final ReturnMoneyUser dao = new ReturnMoneyUser();
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, String orderField, String orderDirection, Map<String, Object> map)
    throws Exception
  {
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("staff", Staff.class);
    StringBuffer sel = new StringBuffer("select user.*,staff.*");
    String tableName = "sys_user";
    StringBuffer sql = new StringBuffer(" from " + tableName + " as user");
    sql.append(" left join b_staff as staff on staff.id = user.staffId");
    sql.append(" where 1=1");
    ComFunController.queryStaffPrivs(sql, (String)map.get("staffPrivs"), "user");
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    }
    String statCashMoneySql = statGetMoneySql(configName, map, "0003");
    sel.append(",(select sum(getMoneys) from (" + statCashMoneySql + ") as cashReturn where cashReturn.userId = user.id) as cashGetMoneys");
    
    String statBankMoneySql = statGetMoneySql(configName, map, "0004");
    sel.append(", (select sum(getMoneys) from (" + statBankMoneySql + ") as bankReturn where bankReturn.userId = user.id) as bankGetMoneys");
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, new Object[0]);
  }
  
  private String statGetMoneySql(String configName, Map<String, Object> map, String accountsType)
  {
    Integer accountPid = Db.use(configName).queryInt("select acc.id from b_accounts acc where  acc.type=" + accountsType);
    if (accountPid == null) {
      accountPid = Integer.valueOf(0);
    }
    String billType = AioConstants.BILL_ROW_TYPE4 + "," + AioConstants.BILL_ROW_TYPE7 + "," + AioConstants.BILL_ROW_TYPE13 + "," + AioConstants.BILL_ROW_TYPE17;
    StringBuffer sql = new StringBuffer();
    sql.append(" select xs.userId,sum(case when pay.type=0 then  pay.payMoney else pay.payMoney*-1 end) as getMoneys from  cw_pay_type  as pay");
    sql.append(" left join (");
    sql.append(" select bill.id,bill.userId,4 as billTypeId from xs_sell_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL");
    sql.append(" select bill.id,bill.userId,7 as billTypeId from xs_return_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL");
    sql.append(" select bill.id,bill.userId,13 as billTypeId from xs_barter_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL ");
    sql.append(" select bill.id,bill.userId,17 as billTypeId from cw_getmoney_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" ) as xs  on xs.billTypeId = pay.billTypeId and xs.id = pay.billId");
    sql.append(" left join b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" where  pay.billTypeId  in (" + billType + ") and accounts.pids like '%{" + accountPid + "}%'");
    sql.append(" group by xs.userId");
    
    return sql.toString();
  }
}
