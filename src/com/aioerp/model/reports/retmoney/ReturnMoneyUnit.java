package com.aioerp.model.reports.retmoney;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Area;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ReturnMoneyUnit
  extends BaseDbModel
{
  public static final ReturnMoneyUnit dao = new ReturnMoneyUnit();
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws Exception
  {
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("area", Area.class);
    List<Object> params = new ArrayList();
    StringBuffer sel = new StringBuffer("select unit.*");
    String tableName = "b_unit";
    
    StringBuffer sql = new StringBuffer(" from " + tableName + " as unit");
    sql.append(" left join b_area as area on unit.areaId = area.id");
    sql.append(" where 1=1  and unit.status = 2 ");
    ComFunController.basePrivsSql(sql, (String)map.get("unitPrivs"), "unit");
    query(sql, params, map);
    rank(sql, map);
    
    String sellMoneysSql = sellMoneysSql(map);
    

    sel.append(", (select sum(sellMoney) from (" + sellMoneysSql + ") as sellUnit where sellUnit.pids like concat('%{',unit.id,'}%')) as sellMoneys");
    

    String cashReturnSql = returnMoneysSql(configName, map, "0003");
    
    sel.append(",(select sum(getMoneys) from (" + cashReturnSql + ") as cashReturn where cashReturn.pids like concat('%{',unit.id,'}%') ) as cashGetMoneys");
    
    String bankReturnSql = returnMoneysSql(configName, map, "0004");
    
    sel.append(",(select sum(getMoneys) from (" + bankReturnSql + ") as bankReturn where bankReturn.pids like concat('%{',unit.id,'}%') ) as bankGetMoneys");
    
    String arapSql = arapSql(map);
    
    sel.append(",(select sum(shouldGetMoney) from (" + arapSql + ") as araps where araps.pids like concat('%{',unit.id,'}%') ) as shouldGetMoneys");
    sel.append(",(select sum(shouldPayMoney) from (" + arapSql + ") as araps where araps.pids like concat('%{',unit.id,'}%') ) as shouldPayMoneys");
    sel.append(",(select sum(getMoney) from (" + arapSql + ") as araps where araps.pids like concat('%{',unit.id,'}%') ) as getMoneys");
    sel.append(",(select sum(payMoney) from (" + arapSql + ") as araps where araps.pids like concat('%{',unit.id,'}%') ) as payMoneys");
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, params.toArray());
  }
  
  public Map<String, Object> getRowPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws Exception
  {
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("area", Area.class);
    List<Object> params = new ArrayList();
    StringBuffer sel = new StringBuffer("select unit.*");
    String tableName = "b_unit";
    
    StringBuffer sql = new StringBuffer(" from " + tableName + " as unit");
    sql.append(" left join b_area as area on unit.areaId = area.id");
    

    String statSellSql = statSellSql(map);
    sel.append(" ,sellUnit.sellMoney as sellMoneys");
    sql.append(" left join (" + statSellSql + ") as sellUnit on case when unit.id is null then sellUnit.unitId is null  else sellUnit.unitId = unit.id end  ");
    
    String statCashMoneySql = statGetMoneySql(configName, map, "0003");
    sel.append(" ,cashReturn.getMoneys as cashGetMoneys");
    sql.append(" left join (" + statCashMoneySql + ") as cashReturn on case when unit.id is null then cashReturn.unitId is null  else cashReturn.unitId = unit.id end  ");
    
    String statBankMoneySql = statGetMoneySql(configName, map, "0004");
    sel.append(" ,bankReturn.getMoneys as bankGetMoneys");
    sql.append(" left join (" + statBankMoneySql + ") as bankReturn on case when unit.id is null then bankReturn.unitId is null  else bankReturn.unitId = unit.id end  ");
    
    String statArapSql = statArapSql(map);
    sel.append(" ,araps.shouldGetMoney as shouldGetMoneys,araps.shouldPayMoney as shouldPayMoneys,araps.getMoney as getMoneys,araps.payMoney as payMoneys");
    sql.append(" left join (" + statArapSql + ") as araps on case when unit.id is null then araps.unitId is null  else araps.unitId = unit.id end  ");
    
    sql.append(" where 1=1  and unit.status = 2 and unit.node = 1");
    sql.append(" and sellUnit.sellMoney is not null");
    
    query(sql, params, map);
    rank(sql, map);
    
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
    alias.put("unit", Unit.class);
    alias.put("staff", Staff.class);
    alias.put("accounts", Accounts.class);
    String detailGetMoneySql = detailGetMoneySql(map);
    StringBuffer sel = new StringBuffer("select sell.*,unit.*,accounts.*,staff.*,case when pay.type=0 then  pay.payMoney else pay.payMoney*-1 end payMoney ,billType.name as billTypeName");
    StringBuffer sql = new StringBuffer(" from cw_pay_type as pay");
    sql.append(" left join(" + detailGetMoneySql + ") as sell on sell.id = pay.billId and sell.billTypeId = pay.billTypeId");
    sql.append(" left join b_unit as unit on unit.id = pay.unitId");
    sql.append(" left join b_accounts as accounts on accounts.id = pay.accountId");
    sql.append(" left join sys_billtype as billType on billType.id = pay.billTypeId");
    sql.append(" left join b_staff as staff on staff.id = sell.staffId");
    sql.append(" where 1=1");
    
    ComFunController.queryAccountPrivs(sql, (String)map.get("accountPrivs"), "pay");
    ComFunController.queryUnitPrivs(sql, (String)map.get("unitPrivs"), "pay");
    
    sql.append(" and pay.billTypeId  in (" + billType + ") and (accounts.pids like '%{" + accountCashPid + "}%' or accounts.pids like '%{" + accountBankPid + "}%')");
    if (map != null)
    {
      Integer unitId = (Integer)map.get("unitId");
      if (unitId == null) {
        unitId = Integer.valueOf(0);
      }
      sql.append(" and unit.pids like '%{" + unitId + "}%'");
      if (map.get("isRcw") != null) {
        sql.append(" and sell.isRcw = " + map.get("isRcw"));
      }
    }
    rank(sql, map);
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, new Object[0]);
  }
  
  private String arapSql(Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer("select * from b_unit as s left join (");
    
    sql.append(statArapSql(map));
    
    sql.append(" )as arap  on s.id = arap.unitId");
    return sql.toString();
  }
  
  private String statArapSql(Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer();
    sql.append(" select bill.unitId,sum(bill.addMoney)as shouldGetMoney, sum(bill.subMoney)as shouldPayMoney,sum(bill.getMoney)as getMoney,sum(bill.payMoney)as payMoney from cw_arap_records as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeTime >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeTime <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" group by bill.unitId");
    
    return sql.toString();
  }
  
  private String returnMoneysSql(String configName, Map<String, Object> map, String accountsType)
  {
    StringBuffer sql = new StringBuffer("select * from b_unit as s left join (");
    
    sql.append(statGetMoneySql(configName, map, accountsType));
    
    sql.append(" )as getMoneys  on s.id = getMoneys.unitId");
    
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
    sql.append(" select xs.unitId,sum(case when pay.type=0 then  pay.payMoney else pay.payMoney*-1 end) as getMoneys from  cw_pay_type  as pay");
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
    sql.append(" where  pay.billTypeId  in (" + billType + ") and accounts.pids like '%{" + accountPid + "}%'");
    sql.append(" group by xs.unitId");
    
    return sql.toString();
  }
  
  private String detailGetMoneySql(Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer();
    sql.append(" select bill.id,bill.unitId,4 as billTypeId,bill.recodeDate,bill.code,bill.remark,bill.isRCW,bill.staffId from xs_sell_bill as bill where 1=1");
    if (map.get("startTime") != null) {
      sql.append(" and bill.recodeDate >= '" + map.get("startTime") + "'");
    }
    if (map.get("endTime") != null) {
      sql.append(" and bill.recodeDate <= '" + map.get("endTime") + " 23:59:59'");
    }
    sql.append(" UNION ALL");
    sql.append(" select bill.id,bill.unitId,7 as billTypeId ,bill.recodeDate,bill.code,bill.remark,bill.isRCW ,bill.staffId from xs_return_bill as bill where 1=1");
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
    return sql.toString();
  }
  
  private String sellMoneysSql(Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer("select * from b_unit as s left join (");
    sql.append(statSellSql(map));
    sql.append(" )as moneys  on s.id = moneys.unitId");
    return sql.toString();
  }
  
  private String statSellSql(Map<String, Object> map)
  {
    StringBuffer sql = new StringBuffer();
    sql.append(" select sum(sell.money) as sellMoney ,sell.unitId from (");
    sql.append(" select his.unitId ,case when his.isRCW = 2 then his.taxMoneys*-1 else his.taxMoneys end as money from xs_sell_bill as his where 1=1");
    
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
    sql.append(" ) as sell group by sell.unitId");
    return sql.toString();
  }
  
  private void query(StringBuffer sql, List<Object> params, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    if (map.get("supId") != null) {
      if ((map.get("isRow") != null) && ("true".equals(map.get("isRow"))))
      {
        sql.append(" and unit.node = 1");
        sql.append(" and unit.pids like '%{" + map.get("supId") + "}%'");
      }
      else
      {
        sql.append(" and unit.supId = ?");
        params.add(map.get("supId"));
      }
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
    } else if ("areaName".equals(orderField)) {
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
