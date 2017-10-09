package com.aioerp.model.reports.retmoney;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ReturnMoneyArea
  extends BaseDbModel
{
  public static final ReturnMoneyArea dao = new ReturnMoneyArea();
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws Exception
  {
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("area", Department.class);
    List<Object> params = new ArrayList();
    StringBuffer sel = new StringBuffer("select area.*");
    String tableName = "b_area";
    Integer supId = (Integer)map.get("supId");
    if (supId.intValue() == 0) {
      tableName = "(select d.* from b_unit s left join b_area d on s.areaId = d.id where s.areaId is null limit 1 UNION all select * from b_area )";
    }
    StringBuffer sql = new StringBuffer(" from " + tableName + " as area");
    
    sql.append(" where 1=1  and area.status = 2 ");
    ComFunController.basePrivsSql(sql, (String)map.get("areaPrivs"), "area");
    query(sql, params, map);
    rank(sql, map);
    
    String sellMoneysSql = sellMoneysSql(map);
    String statSellSql = statSellSql(map);
    
    sel.append(",case when area.id is null then (select sum(sellMoney) from (" + statSellSql + ") as sellArea where sellArea.areaId is null) else (select sum(sellMoney) from (" + sellMoneysSql + ") as sellArea where sellArea.pids like concat('%{',area.id,'}%')) end as sellMoneys");
    

    String cashReturnSql = returnMoneysSql(configName, map, "0003");
    String statCashMoneySql = statGetMoneySql(configName, map, "0003");
    sel.append(",case when area.id is null then (select sum(getMoneys) from (" + statCashMoneySql + ") as cashReturn where cashReturn.areaId is null) else (select sum(getMoneys) from (" + cashReturnSql + ") as cashReturn where cashReturn.pids like concat('%{',area.id,'}%') ) end as cashGetMoneys");
    
    String bankReturnSql = returnMoneysSql(configName, map, "0004");
    String statBankMoneySql = statGetMoneySql(configName, map, "0004");
    sel.append(",case when area.id is null then (select sum(getMoneys) from (" + statBankMoneySql + ") as bankReturn where bankReturn.areaId is null) else(select sum(getMoneys) from (" + bankReturnSql + ") as bankReturn where bankReturn.pids like concat('%{',area.id,'}%') ) end as bankGetMoneys");
    
    String arapSql = arapSql(map);
    String statArapSql = statArapSql(map);
    sel.append(",case when area.id is null then (select sum(shouldGetMoney) from (" + statArapSql + ") as araps where araps.areaId is null) else(select sum(shouldGetMoney) from (" + arapSql + ") as araps where araps.pids like concat('%{',area.id,'}%') )end as shouldGetMoneys");
    sel.append(",case when area.id is null then (select sum(shouldPayMoney) from (" + statArapSql + ") as araps where araps.areaId is null) else (select sum(shouldPayMoney) from (" + arapSql + ") as araps where araps.pids like concat('%{',area.id,'}%') ) end as shouldPayMoneys");
    sel.append(",case when area.id is null then (select sum(getMoney) from (" + statArapSql + ") as araps where araps.areaId is null ) else(select sum(getMoney) from (" + arapSql + ") as araps where araps.pids like concat('%{',area.id,'}%') ) end as getMoneys");
    sel.append(",case when area.id is null then (select sum(payMoney) from (" + statArapSql + ") as araps where araps.areaId is null ) else (select sum(payMoney) from (" + arapSql + ") as araps where araps.pids like concat('%{',area.id,'}%') ) end as payMoneys");
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, params.toArray());
  }
  
  public Map<String, Object> getAccountPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws SQLException, Exception
  {
    Integer accountCashPid = Db.use(configName).queryInt("select acc.id from b_accounts acc where  acc.type= '0003'");
    if (accountCashPid == null) {
      accountCashPid = Integer.valueOf(0);
    }
    Integer accountBankPid = Db.use(configName).queryInt("select acc.id from b_accounts acc where  acc.type= '0004'");
    if (accountBankPid == null) {
      accountBankPid = Integer.valueOf(0);
    }
    String billType = AioConstants.BILL_ROW_TYPE4 + "," + AioConstants.BILL_ROW_TYPE7 + "," + AioConstants.BILL_ROW_TYPE13 + "," + AioConstants.BILL_ROW_TYPE17;
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("staff", Staff.class);
    alias.put("accounts", Accounts.class);
    String detailGetMoneySql = detailGetMoneySql(map);
    StringBuffer sel = new StringBuffer("select sell.*,staff.*,accounts.*,case when pay.type=0 then  pay.payMoney else pay.payMoney*-1 end payMoney ,billType.name as billTypeName");
    StringBuffer sql = new StringBuffer(" from cw_pay_type as pay");
    sql.append(" left join(" + detailGetMoneySql + ") as sell on sell.id = pay.billId and sell.billTypeId = pay.billTypeId");
    sql.append(" left join b_staff as staff on staff.id = sell.staffId");
    sql.append(" left join b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" left join sys_billtype as billType on billType.id = pay.billTypeId");
    sql.append(" where 1=1");
    ComFunController.queryAccountPrivs(sql, (String)map.get("accountPrivs"), "pay");
    ComFunController.queryStaffPrivs(sql, (String)map.get("staffPrivs"), "pay");
    sql.append(" and pay.billTypeId  in (" + billType + ") and (accounts.pids like '%{" + accountCashPid + "}%' or accounts.pids like '%{" + accountBankPid + "}%')");
    if (map != null)
    {
      Integer areaId = (Integer)map.get("areaId");
      if ((areaId == null) || (areaId.intValue() == 0)) {
        sql.append(" and sell.areaId is null ");
      } else {
        sql.append(" and sell.areaId = " + areaId);
      }
      if (map.get("isRcw") != null) {
        sql.append(" and sell.isRcw = " + map.get("isRcw"));
      }
    }
    rank(sql, map);
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, new Object[0]);
  }
  
  private String arapSql(Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer("select * from b_area as s left join (");
    
    sql.append(statArapSql(map));
    
    sql.append(" )as arap  on s.id = arap.areaId");
    return sql.toString();
  }
  
  private String statArapSql(Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer();
    sql.append(" select u.areaId,sum(bill.addMoney)as shouldGetMoney, sum(bill.subMoney)as shouldPayMoney,sum(bill.getMoney)as getMoney,sum(bill.payMoney)as payMoney from cw_arap_records as bill");
    sql.append(" left join b_unit as u on u.id = bill.unitId");
    sql.append(" where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeTime >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeTime <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" group by u.areaId");
    
    return sql.toString();
  }
  
  private String returnMoneysSql(String configName, Map<String, Object> map, String accountsType)
  {
    StringBuffer sql = new StringBuffer("select * from b_area as s left join (");
    
    sql.append(statGetMoneySql(configName, map, accountsType));
    
    sql.append(" )as getMoneys  on s.id = getMoneys.areaId");
    
    return sql.toString();
  }
  
  private String statGetMoneySql(String configName, Map<String, Object> map, String accountsType)
  {
    Integer accountPid = Db.use(configName).queryInt("select acc.id from b_accounts acc where  acc.type=" + accountsType);
    if (accountPid == null) {
      accountPid = Integer.valueOf(0);
    }
    String billType = AioConstants.BILL_ROW_TYPE4 + "," + AioConstants.BILL_ROW_TYPE7 + "," + AioConstants.BILL_ROW_TYPE13 + "," + AioConstants.BILL_ROW_TYPE17;
    StringBuffer sql = new StringBuffer();
    sql.append(" select u.areaId,sum(case when pay.type=0 then  pay.payMoney else pay.payMoney*-1 end) as getMoneys from  cw_pay_type  as pay");
    sql.append(" left join (");
    sql.append(" select bill.id,bill.unitId,4 as billTypeId from xs_sell_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL");
    sql.append(" select bill.id,bill.unitId,7 as billTypeId from xs_return_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL");
    sql.append(" select bill.id,bill.unitId,13 as billTypeId from xs_barter_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL ");
    sql.append(" select bill.id,bill.unitId,17 as billTypeId from cw_getmoney_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" ) as xs  on xs.billTypeId = pay.billTypeId and xs.id = pay.billId");
    sql.append(" left join b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" left join b_unit as u on u.id = pay.unitId");
    sql.append(" where  pay.billTypeId  in (" + billType + ") and accounts.pids like '%{" + accountPid + "}%'");
    sql.append(" group by u.areaId");
    
    return sql.toString();
  }
  
  private String detailGetMoneySql(Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer(" select xs.*,u.areaId from( ");
    sql.append(" select bill.id,bill.unitId,4 as billTypeId,bill.recodeDate,bill.code,bill.remark,bill.isRCW,bill.staffId from xs_sell_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL");
    sql.append(" select bill.id,bill.unitId,7 as billTypeId ,bill.recodeDate,bill.code,bill.remark,bill.isRCW,bill.staffId  from xs_return_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL");
    sql.append(" select bill.id,bill.unitId,13 as billTypeId,bill.recodeDate,bill.code,bill.remark ,bill.isRCW,bill.staffId from xs_barter_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL ");
    sql.append(" select bill.id,bill.unitId,17 as billTypeId,bill.recodeDate,bill.code,bill.remark,bill.isRCW,bill.staffId  from cw_getmoney_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" ) as xs left join b_unit as u on u.id = xs.unitId");
    return sql.toString();
  }
  
  private String sellMoneysSql(Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer("select * from b_area as s ");
    sql.append(" left join (");
    sql.append(statSellSql(map));
    sql.append(" )as moneys  on s.id = moneys.areaId");
    
    return sql.toString();
  }
  
  private String statSellSql(Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer();
    sql.append(" select sum(sell.money) as sellMoney ,u.areaId from (");
    sql.append(" select his.unitId,case when his.isRCW = 2 then his.taxMoneys*-1 else his.taxMoneys end as money from xs_sell_bill as his where 1=1");
    ComFunController.queryUnitPrivs(sql, (String)map.get("unitPrivs"), "his");
    ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "his");
    ComFunController.queryStaffPrivs(sql, (String)map.get("staffPrivs"), "his");
    ComFunController.queryDepartmentPrivs(sql, (String)map.get("departmentPrivs"), "his");
    if (map.get("startTime") != null) {
      sql.append(" and his.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and his.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL");
    sql.append(" select his.unitId ,case when his.isRCW = 2 then his.taxMoneys else his.taxMoneys*-1 end as money from xs_return_bill as his where 1=1");
    ComFunController.queryUnitPrivs(sql, (String)map.get("unitPrivs"), "his");
    ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "his");
    ComFunController.queryStaffPrivs(sql, (String)map.get("staffPrivs"), "his");
    ComFunController.queryDepartmentPrivs(sql, (String)map.get("departmentPrivs"), "his");
    if (map.get("startTime") != null) {
      sql.append(" and his.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and his.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL");
    sql.append(" select his.unitId ,case when his.isRCW = 2 then his.gapMoney*-1 else his.gapMoney end as money from xs_barter_bill as his where 1=1");
    ComFunController.queryUnitPrivs(sql, (String)map.get("unitPrivs"), "his");
    ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "his", "outStorageId");
    ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "his", "inStorageId");
    ComFunController.queryStaffPrivs(sql, (String)map.get("staffPrivs"), "his");
    ComFunController.queryDepartmentPrivs(sql, (String)map.get("departmentPrivs"), "his");
    if (map.get("startTime") != null) {
      sql.append(" and his.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and his.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" ) as sell left join b_unit u on u.id = sell.unitId group by u.areaId");
    return sql.toString();
  }
  
  private void query(StringBuffer sql, List<Object> params, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    if (map.get("supId") != null)
    {
      if ((map.get("isRow") != null) && ("true".equals(map.get("isRow"))))
      {
        sql.append(" and area.node = 1");
        sql.append(" and area.pids like '%{" + map.get("supId") + "}%'");
      }
      else
      {
        sql.append(" and area.supId = ?");
        params.add(map.get("supId"));
      }
      sql.append(" or area.id is null ");
    }
    else
    {
      sql.append(" or area.id is null ");
    }
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
    if ("areaCode".equals(orderField)) {
      sql.append(" order by area.code " + orderDirection);
    } else if ("areaFullName".equals(orderField)) {
      sql.append(" order by area.fullName " + orderDirection);
    } else if ("getMoneySum".equals(orderField)) {
      sql.append(" order by cashGetMoneys,bankGetMoneys  " + orderDirection);
    } else if ("shouldGetMoneys".equals(orderField)) {
      sql.append(" order by shouldGetMoneys,shouldPayMoneys  " + orderDirection);
    } else if ("shouldPayMoneys".equals(orderField)) {
      sql.append(" order by shouldPayMoneys,shouldGetMoneys  " + orderDirection);
    } else if ("billCode".equals(orderField)) {
      sql.append(" order by sell.code  " + orderDirection);
    } else if ("staffCode".equals(orderField)) {
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
