package com.aioerp.model.reports.bought;

import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.reports.sell.SellReports;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseReturnUnitReports
  extends BaseDbModel
{
  public static final PurchaseReturnUnitReports dao = new PurchaseReturnUnitReports();
  
  public Map<String, Object> getPage(String configName, String unitPrivs, int pageNum, int numPerPage, String listID, Map<String, Object> map)
  {
    Integer supId = (Integer)map.get("supId");
    
    StringBuffer sel = new StringBuffer("select t.*");
    StringBuffer sql = new StringBuffer(" from (");
    List<Object> paras = new ArrayList();
    
    relevance(configName, unitPrivs, sql, supId, map);
    
    sql.append(" where 1=1");
    ComFunController.basePrivsSql(sql, (String)map.get("unitPrivs"), "t");
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
  
  private void relevance(String configName, String unitPrivs, StringBuffer sql, Integer supId, Map<String, Object> map)
  {
    StringBuffer sel = new StringBuffer("select b.*");
    StringBuffer from = new StringBuffer(" from b_unit b ");
    



    StringBuffer stat = new StringBuffer();
    
    stat.append(" select unit.pids, s.unitId,sum(s.money) as money,sum(s.returnMoney) as returnMoney,sum(s.returnMoney)/sum(s.money)*100 as returnRate from (");
    
    stat.append(" select p.id,p.unitId,p.staffId");
    stat.append(",case when p.isRCW != 2 then sum(cg.baseAmount*cg.basePrice) else null end as money");
    stat.append(",case when p.isRCW != 2 then null else sum(cg.baseAmount*cg.basePrice) end as returnMoney");
    stat.append(" from cg_purchase_bill p");
    stat.append(" right join (");
    stat.append(" select detail.*,storage.pids as storagePs,product.pids as productPs from cg_purchase_detail detail ");
    stat.append(" left join b_storage as storage on detail.storageId = storage.id");
    stat.append(" left join b_product as product on detail.productId = product.id");
    stat.append(" ) as cg on p.id = cg.billId");
    stat.append(" left join b_staff staff on p.staffId = staff.id");
    
    stat.append(" where 1=1");
    ComFunController.queryProductPrivs(stat, (String)map.get("productPrivs"), "cg");
    ComFunController.queryUnitPrivs(stat, (String)map.get("unitPrivs"), "p");
    ComFunController.queryStoragePrivs(stat, (String)map.get("storagePrivs"), "cg");
    ComFunController.queryStaffPrivs(stat, (String)map.get("staffPrivs"), "p");
    ComFunController.queryDepartmentPrivs(stat, (String)map.get("departmentPrivs"), "p");
    if (map.get("startDate") != null) {
      stat.append(" and p.recodeDate >= '" + map.get("startDate") + "'");
    }
    if (map.get("endDate") != null) {
      stat.append(" and p.recodeDate <= '" + map.get("endDate") + " 23:59:59'");
    }
    if (map.get("storageId") != null) {
      stat.append(" and cg.storagePs like '%{" + map.get("storageId") + "}%' or cg.storageId = " + map.get("storageId"));
    }
    if (map.get("productId") != null) {
      stat.append(" and cg.productPs like '%{" + map.get("productId") + "}%' or cg.productId = " + map.get("productId"));
    }
    if (map.get("staffId") != null) {
      stat.append(" and staff.pids like '%{" + map.get("staffId") + "}%' or staff.id= " + map.get("staffId"));
    }
    stat.append(" group by p.id");
    
    stat.append(" UNION ALL");
    
    stat.append(" select p.id,p.unitId,p.staffId");
    stat.append(",case when p.isRCW = 2 then sum(cg.baseAmount*cg.basePrice) else null end as money");
    stat.append(",case when p.isRCW = 2 then null else sum(cg.baseAmount*cg.basePrice) end as returnMoney");
    stat.append(" from  cg_return_bill p");
    stat.append(" right join (");
    stat.append(" select detail.*,storage.pids as storagePs,product.pids as productPs from cg_return_detail detail ");
    stat.append(" left join b_storage as storage on detail.storageId = storage.id");
    stat.append(" left join b_product as product on detail.productId = product.id");
    stat.append(" ) as cg on p.id = cg.billId");
    stat.append(" left join b_staff staff on p.staffId = staff.id");
    
    stat.append(" where 1=1");
    ComFunController.queryProductPrivs(stat, (String)map.get("productPrivs"), "cg");
    ComFunController.queryUnitPrivs(stat, (String)map.get("unitPrivs"), "p");
    ComFunController.queryStoragePrivs(stat, (String)map.get("storagePrivs"), "cg");
    ComFunController.queryStaffPrivs(stat, (String)map.get("staffPrivs"), "p");
    ComFunController.queryDepartmentPrivs(stat, (String)map.get("departmentPrivs"), "p");
    if (map.get("startDate") != null) {
      stat.append(" and p.recodeDate >= '" + map.get("startDate") + "'");
    }
    if (map.get("endDate") != null) {
      stat.append(" and p.recodeDate <= '" + map.get("endDate") + " 23:59:59'");
    }
    if (map.get("storageId") != null) {
      stat.append(" and cg.storagePs like '%{" + map.get("storageId") + "}%' or cg.storageId = " + map.get("storageId"));
    }
    if (map.get("productId") != null) {
      stat.append(" and cg.productPs like '%{" + map.get("productId") + "}%' or cg.productId = " + map.get("productId"));
    }
    if (map.get("staffId") != null) {
      stat.append(" and staff.pids like '%{" + map.get("staffId") + "}%' or staff.id= " + map.get("staffId"));
    }
    stat.append(" group by p.id");
    
    stat.append(" UNION ALL");
    
    stat.append(" select p.id,p.unitId,p.staffId");
    stat.append(",case when p.isRCW != 2 and cg.type=2 then null when p.isRCW = 2 and cg.type=2 then sum(cg.baseAmount*cg.basePrice) when p.isRCW = 2 and cg.type=1 then null when p.isRCW != 2 and cg.type=1 then sum(cg.baseAmount*cg.basePrice) end as money");
    stat.append(",case when p.isRCW != 2 and cg.type=2 then sum(cg.baseAmount*cg.basePrice) when p.isRCW = 2 and cg.type=2 then null when p.isRCW = 2 and cg.type=1 then sum(cg.baseAmount*cg.basePrice) when p.isRCW != 2 and cg.type=1 then null end as returnMoney");
    stat.append(" from  cg_barter_bill p");
    stat.append(" right join (");
    stat.append(" select detail.*,storage.pids as storagePs,product.pids as productPs from cg_barter_detail detail ");
    stat.append(" left join b_storage as storage on detail.storageId = storage.id");
    stat.append(" left join b_product as product on detail.productId = product.id");
    stat.append(" ) as cg on p.id = cg.billId");
    stat.append(" left join b_staff staff on p.staffId = staff.id");
    
    stat.append(" where 1=1");
    ComFunController.queryProductPrivs(stat, (String)map.get("productPrivs"), "cg");
    ComFunController.queryUnitPrivs(stat, (String)map.get("unitPrivs"), "p");
    ComFunController.queryStoragePrivs(stat, (String)map.get("storagePrivs"), "cg");
    ComFunController.queryStaffPrivs(stat, (String)map.get("staffPrivs"), "p");
    ComFunController.queryDepartmentPrivs(stat, (String)map.get("departmentPrivs"), "p");
    if (map.get("startDate") != null) {
      stat.append(" and p.recodeDate >= '" + map.get("startDate") + "'");
    }
    if (map.get("endDate") != null) {
      stat.append(" and p.recodeDate <= '" + map.get("endDate") + " 23:59:59'");
    }
    if (map.get("storageId") != null) {
      stat.append(" and cg.storagePs like '%{" + map.get("storageId") + "}%' or cg.storageId = " + map.get("storageId"));
    }
    if (map.get("productId") != null) {
      stat.append(" and cg.productPs like '%{" + map.get("productId") + "}%' or cg.productId = " + map.get("productId"));
    }
    if (map.get("staffId") != null) {
      stat.append(" and staff.pids like '%{" + map.get("staffId") + "}%' or staff.id= " + map.get("staffId"));
    }
    stat.append(" group by p.id,cg.type");
    

    stat.append(" ) as s left join b_unit unit on s.unitId = unit.id group by s.unitId ");
    
    StringBuffer money = new StringBuffer(" ,(select sum(bill.money) from (");
    money.append(stat.toString());
    money.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as money");
    sel.append(money.toString());
    
    StringBuffer returnMoney = new StringBuffer(" ,(select sum(bill.returnMoney) from (");
    returnMoney.append(stat.toString());
    returnMoney.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as returnMoney");
    sel.append(returnMoney.toString());
    
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
    if (map.get("unitId") != null)
    {
      sql.append(" and t.pids like ? or t.id = ?");
      paras.add("%{" + map.get("unitId") + "}%");
      paras.add(map.get("unitId"));
    }
  }
}
