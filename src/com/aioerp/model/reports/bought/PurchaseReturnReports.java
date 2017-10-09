package com.aioerp.model.reports.bought;

import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.reports.sell.SellReports;
import com.aioerp.util.DwzUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseReturnReports
  extends BaseDbModel
{
  public static final PurchaseReturnReports dao = new PurchaseReturnReports();
  
  public String getHelpAmount()
  {
    BigDecimal amount = getBigDecimal("baseAmount");
    String calculateUnit1 = getStr("calculateUnit1");
    String calculateUnit2 = getStr("calculateUnit2");
    String calculateUnit3 = getStr("calculateUnit3");
    BigDecimal unitRelation2 = getBigDecimal("unitRelation2");
    BigDecimal unitRelation3 = getBigDecimal("unitRelation3");
    return DwzUtils.helpAmount(amount, calculateUnit1, calculateUnit2, calculateUnit3, unitRelation2, unitRelation3);
  }
  
  public Map<String, Object> getPage(String configName, String privs, int pageNum, int numPerPage, String listID, Map<String, Object> map)
  {
    Integer supId = (Integer)map.get("supId");
    
    StringBuffer sel = new StringBuffer("select t.*");
    StringBuffer sql = new StringBuffer(" from (");
    List<Object> paras = new ArrayList();
    
    relevance(configName, privs, sql, supId, map);
    
    sql.append(" where 1=1");
    ComFunController.basePrivsSql(sql, (String)map.get("productPrivs"), "t");
    if (map != null) {
      query(sql, paras, map);
    }
    rank(sql, map);
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, sel.toString(), sql.toString(), paras.toArray());
  }
  
  private void rank(StringBuffer sql, Map<String, Object> map)
  {
    String orderField = (String)map.get("orderField");
    String orderDirection = (String)map.get("orderDirection");
    if ((StringUtils.isBlank(orderField)) || (StringUtils.isBlank(orderDirection))) {
      return;
    }
    sql.append(" order by " + orderField + " " + orderDirection);
  }
  
  private void relevance(String configName, String privs, StringBuffer sql, Integer supId, Map<String, Object> map)
  {
    StringBuffer sel = new StringBuffer("select b.*");
    StringBuffer from = new StringBuffer(" from b_product b ");
    



    StringBuffer stat = new StringBuffer();
    
    stat.append(" select product.pids, s.productId,sum(s.baseAmount) as baseAmount,sum(s.money) as money,sum(s.returnAmount) as returnAmount,sum(s.returnMoney) as returnMoney,sum(s.putAmount) as putAmount,sum(s.putMoney) as putMoney ,sum(s.returnAmount)/sum(s.putAmount)*100 as returnRate from (");
    
    stat.append(" SELECT p.productId");
    stat.append(",case when tit.isRCW = 2 then p.baseAmount *-1 else p.baseAmount end as baseAmount");
    stat.append(",case when tit.isRCW = 2 then p.baseAmount*p.basePrice *-1 else p.baseAmount*p.basePrice end as money");
    stat.append(",case when tit.isRCW = 2 then p.baseAmount  else 0 end as returnAmount");
    stat.append(",case when tit.isRCW = 2 then p.baseAmount*p.basePrice else 0 end as returnMoney");
    stat.append(",case when tit.isRCW = 2 then 0  else p.baseAmount end as putAmount");
    stat.append(",case when tit.isRCW = 2 then 0 else p.baseAmount*p.basePrice end as putMoney");
    stat.append(" from cg_purchase_detail p");
    stat.append(" left join b_storage storage on p.storageId = storage.id ");
    stat.append(" left join ( select cg.*,unit.pids as unitPs,staff.pids as staffPs from cg_purchase_bill cg  ");
    stat.append(" left join b_unit unit on cg.unitId = unit.id");
    stat.append(" left join b_staff staff on cg.staffId = staff.id ) as tit");
    stat.append(" on p.billId = tit.id");
    
    stat.append(" where 1=1");
    ComFunController.queryProductPrivs(stat, (String)map.get("productPrivs"), "p");
    ComFunController.queryUnitPrivs(stat, (String)map.get("unitPrivs"), "tit");
    ComFunController.queryStoragePrivs(stat, (String)map.get("storagePrivs"), "p");
    ComFunController.queryStaffPrivs(stat, (String)map.get("staffPrivs"), "tit");
    ComFunController.queryDepartmentPrivs(stat, (String)map.get("departmentPrivs"), "tit");
    if (map.get("startDate") != null) {
      stat.append(" and tit.recodeDate >= '" + map.get("startDate") + "'");
    }
    if (map.get("endDate") != null) {
      stat.append(" and tit.recodeDate <= '" + map.get("endDate") + " 23:59:59'");
    }
    if (map.get("storageId") != null) {
      stat.append(" and storage.pids like '%{" + map.get("storageId") + "}%' or storage.id = " + map.get("storageId"));
    }
    if (map.get("unitId") != null) {
      stat.append(" and tit.unitPs like '%{" + map.get("unitId") + "}%' or tit.unitId = " + map.get("unitId"));
    }
    if (map.get("staffId") != null) {
      stat.append(" and tit.staffPs like '%{" + map.get("staffId") + "}%' or tit.staffId = " + map.get("staffId"));
    }
    stat.append(" UNION ALL");
    
    stat.append(" SELECT r.productId");
    stat.append(",case when tit.isRCW != 2 then r.baseAmount *-1 else r.baseAmount end as baseAmount");
    stat.append(",case when tit.isRCW != 2 then r.baseAmount*r.basePrice*-1 else r.baseAmount*r.basePrice end as money");
    stat.append(",case when tit.isRCW != 2 then r.baseAmount else 0 end as returnAmount");
    stat.append(",case when tit.isRCW != 2 then r.baseAmount*r.basePrice else 0 end as returnMoney");
    stat.append(",case when tit.isRCW != 2 then 0 else r.baseAmount end as putAmount");
    stat.append(",case when tit.isRCW != 2 then 0 else r.baseAmount*r.basePrice end as putMoney");
    stat.append(" from cg_return_detail r");
    stat.append(" left join b_storage storage on r.storageId = storage.id ");
    stat.append(" left join ( select cg.*,unit.pids as unitPs,staff.pids as staffPs from cg_return_bill cg  ");
    stat.append(" left join b_unit unit on cg.unitId = unit.id");
    stat.append(" left join b_staff staff on cg.staffId = staff.id ) as tit");
    stat.append(" on r.billId = tit.id");
    
    stat.append(" where 1=1");
    ComFunController.queryProductPrivs(stat, (String)map.get("productPrivs"), "r");
    ComFunController.queryUnitPrivs(stat, (String)map.get("unitPrivs"), "tit");
    ComFunController.queryStoragePrivs(stat, (String)map.get("storagePrivs"), "r");
    ComFunController.queryStaffPrivs(stat, (String)map.get("staffPrivs"), "tit");
    ComFunController.queryDepartmentPrivs(stat, (String)map.get("departmentPrivs"), "tit");
    if (map.get("startDate") != null) {
      stat.append(" and tit.recodeDate >= '" + map.get("startDate") + "'");
    }
    if (map.get("endDate") != null) {
      stat.append(" and tit.recodeDate <= '" + map.get("endDate") + " 23:59:59'");
    }
    if (map.get("storageId") != null) {
      stat.append(" and storage.pids like '%{" + map.get("storageId") + "}%' or storage.id = " + map.get("storageId"));
    }
    if (map.get("unitId") != null) {
      stat.append(" and tit.unitPs like '%{" + map.get("unitId") + "}%' or tit.unitId = " + map.get("unitId"));
    }
    if (map.get("staffId") != null) {
      stat.append(" and tit.staffPs like '%{" + map.get("staffId") + "}%' or tit.staffId = " + map.get("staffId"));
    }
    stat.append(" UNION ALL");
    
    stat.append(" SELECT r.productId");
    stat.append(",case when tit.isRCW != 2 and r.type=2 then r.baseAmount*-1  when tit.isRCW != 2 and r.type=1 then r.baseAmount when tit.isRCW=2 and r.type=2 then r.baseAmount when tit.isRCW=2 and r.type=1 then r.baseAmount*-1 end as baseAmount");
    stat.append(",case when tit.isRCW != 2 and r.type=2 then r.baseAmount*r.basePrice*-1  when tit.isRCW != 2 and r.type=1 then r.baseAmount*r.basePrice when tit.isRCW=2 and r.type=2 then r.baseAmount*r.basePrice when tit.isRCW=2 and r.type=1 then r.baseAmount*r.basePrice*-1 end as money");
    stat.append(",case when tit.isRCW != 2 and r.type=2 then r.baseAmount when tit.isRCW != 2 and r.type=1 then 0 when tit.isRCW=2 and r.type=2 then 0 when tit.isRCW=2 and r.type=1 then r.baseAmount end as returnAmount");
    stat.append(",case when tit.isRCW != 2 and r.type=2 then r.baseAmount*r.basePrice when tit.isRCW != 2 and r.type=1 then 0 when tit.isRCW=2 and r.type=2 then 0 when tit.isRCW=2 and r.type=1 then r.baseAmount*r.basePrice end as returnMoney");
    stat.append(",case when tit.isRCW != 2 and r.type=2 then 0 when tit.isRCW != 2 and r.type=1 then r.baseAmount when tit.isRCW=2 and r.type=2 then r.baseAmount when tit.isRCW=2 and r.type=1 then 0 end as putAmount");
    stat.append(",case when tit.isRCW != 2 and r.type=2 then 0 when tit.isRCW != 2 and r.type=1 then r.baseAmount*r.basePrice when tit.isRCW=2 and r.type=2 then r.baseAmount*r.basePrice when tit.isRCW=2 and r.type=1 then 0 end as putMoney");
    stat.append(" from cg_barter_detail r");
    stat.append(" left join b_storage storage on r.storageId = storage.id ");
    stat.append(" left join ( select cg.*,unit.pids as unitPs,staff.pids as staffPs from cg_barter_bill cg  ");
    stat.append(" left join b_unit unit on cg.unitId = unit.id");
    stat.append(" left join b_staff staff on cg.staffId = staff.id ) as tit");
    stat.append(" on r.billId = tit.id");
    
    stat.append(" where 1=1");
    ComFunController.queryProductPrivs(stat, (String)map.get("productPrivs"), "r");
    ComFunController.queryUnitPrivs(stat, (String)map.get("unitPrivs"), "tit");
    ComFunController.queryStoragePrivs(stat, (String)map.get("storagePrivs"), "r");
    ComFunController.queryStaffPrivs(stat, (String)map.get("staffPrivs"), "tit");
    ComFunController.queryDepartmentPrivs(stat, (String)map.get("departmentPrivs"), "tit");
    if (map.get("startDate") != null) {
      stat.append(" and tit.recodeDate >= '" + map.get("startDate") + "'");
    }
    if (map.get("endDate") != null) {
      stat.append(" and tit.recodeDate <= '" + map.get("endDate") + " 23:59:59'");
    }
    if (map.get("storageId") != null) {
      stat.append(" and storage.pids like '%{" + map.get("storageId") + "}%' or storage.id = " + map.get("storageId"));
    }
    if (map.get("unitId") != null) {
      stat.append(" and tit.unitPs like '%{" + map.get("unitId") + "}%' or tit.unitId = " + map.get("unitId"));
    }
    if (map.get("staffId") != null) {
      stat.append(" and tit.staffPs like '%{" + map.get("staffId") + "}%' or tit.staffId = " + map.get("staffId"));
    }
    stat.append(" ) as s left join b_product product on s.productId = product.id group by s.productId ");
    
    StringBuffer baseAmount = new StringBuffer(" ,(select sum(bill.baseAmount) from (");
    baseAmount.append(stat.toString());
    baseAmount.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as baseAmount");
    sel.append(baseAmount.toString());
    
    StringBuffer money = new StringBuffer(" ,(select sum(bill.money) from (");
    money.append(stat.toString());
    money.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as money");
    sel.append(money.toString());
    
    StringBuffer returnAmount = new StringBuffer(" ,(select sum(bill.returnAmount) from (");
    returnAmount.append(stat.toString());
    returnAmount.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as returnAmount");
    sel.append(returnAmount.toString());
    
    StringBuffer returnMoney = new StringBuffer(" ,(select sum(bill.returnMoney) from (");
    returnMoney.append(stat.toString());
    returnMoney.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as returnMoney");
    sel.append(returnMoney.toString());
    
    StringBuffer putAmount = new StringBuffer(" ,(select sum(bill.putAmount) from (");
    putAmount.append(stat.toString());
    putAmount.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as putAmount");
    sel.append(putAmount.toString());
    
    StringBuffer putMoney = new StringBuffer(" ,(select sum(bill.putMoney) from (");
    putMoney.append(stat.toString());
    putMoney.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as putMoney");
    sel.append(putMoney.toString());
    
    StringBuffer returnRate = new StringBuffer(" ,(select sum(bill.returnRate) from (");
    returnRate.append(stat.toString());
    returnRate.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as returnRate");
    sel.append(returnRate.toString());
    
    sql.append(sel.toString());
    sql.append(from.toString());
    sql.append(") as t");
  }
  
  private void query(StringBuffer sql, List<Object> paras, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    int supId = Integer.valueOf(String.valueOf(map.get("supId"))).intValue();
    

    boolean flag = SellReports.addReportBaseCondition(sql, "t", String.valueOf(map.get("searchBaseAttr")), String.valueOf(map.get("searchBaseVal")));
    if (flag) {
      if ((map.get("isRow") != null) && ("true".equals(map.get("isRow"))))
      {
        sql.append(" and t.node = 1");
        sql.append(" and t.pids like '%{" + supId + "}%'");
      }
      else if (supId == 0)
      {
        sql.append(" and t.supId = 0");
      }
      else
      {
        sql.append(" and t.supId = " + supId);
      }
    }
    if ((map.get("isRow") != null) && ("true".equals(map.get("isRow")))) {
      sql.append(" and t.node = 1");
    }
    if (map.get("productId") != null)
    {
      sql.append(" and t.pids like ? or t.id = ?");
      paras.add("%{" + map.get("productId") + "}%");
      paras.add(map.get("productId"));
    }
  }
}
