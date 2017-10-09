package com.aioerp.model.reports.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseDepartmentReports
  extends BaseDbModel
{
  public static final PurchaseDepartmentReports dao = new PurchaseDepartmentReports();
  
  public Map<String, Object> getPrivilegePage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
  {
    StringBuffer sel = new StringBuffer("select t.*");
    sel.append(",sum(case when bills.isRCW != 2 then bills.taxMoneys else bills.taxMoneys*-1 end)as taxMoneys");
    sel.append(",sum(case when bills.isRCW != 2 then bills.privilege else bills.privilege*-1 end)as privilege");
    sel.append(",sum(case when bills.isRCW != 2 then bills.privilegeMoney else bills.privilegeMoney*-1 end)as privilegeMoney");
    String[] tables = { "cg_purchase_bill", "cg_return_bill", "cg_barter_bill" };
    List<Object> paras = new ArrayList();
    StringBuffer sql = new StringBuffer(" from (");
    for (int i = 0; i < tables.length; i++)
    {
      if (i != 0) {
        sql.append(" UNION ALL");
      }
      if ("cg_barter_bill".equals(tables[i])) {
        sql.append(" select bill.departmentId,bill.isRCW,bill.gapMoney as taxMoneys,bill.privilege,bill.privilegeMoney from " + tables[i] + " bill");
      } else if ("cg_return_bill".equals(tables[i])) {
        sql.append(" select bill.departmentId,bill.isRCW,bill.taxMoneys*-1 as taxMoneys,bill.privilege*-1 as privilege,bill.privilegeMoney*-1 as privilegeMoney from " + tables[i] + " bill");
      } else {
        sql.append(" select bill.departmentId,bill.isRCW,bill.taxMoneys,bill.privilege,bill.privilegeMoney from " + tables[i] + " bill");
      }
      sql.append(" where 1=1");
      ComFunController.queryUnitPrivs(sql, (String)map.get("unitPrivs"), "bill");
      if ("cg_barter_bill".equals(tables[i]))
      {
        ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "bill", "outStorageId");
        ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "bill", "inStorageId");
      }
      else
      {
        ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "bill");
      }
      ComFunController.queryStaffPrivs(sql, (String)map.get("staffPrivs"), "bill");
      ComFunController.queryDepartmentPrivs(sql, (String)map.get("departmentPrivs"), "bill");
      if (map != null)
      {
        if (map.get("startDate") != null) {
          sql.append(" and bill.recodeDate >= '" + map.get("startDate") + "'");
        }
        if (map.get("endDate") != null) {
          sql.append("  and bill.recodeDate <= '" + map.get("endDate") + " 23:59:59'");
        }
        if (map.get("unitId") != null)
        {
          sql.append(" and bill.unitId in (select unit.id from b_unit unit where unit.pids like ?) or bill.unitId = ?");
          paras.add("'%{" + map.get("unitId") + "}%'");
          paras.add(map.get("unitId"));
        }
        if (map.get("staffId") != null)
        {
          sql.append(" and bill.staffId in (select staff.id from b_staff staff where staff.pids like ?) or bill.staffId = ?");
          paras.add("'%{" + map.get("staffId") + "}%'");
          paras.add(map.get("staffId"));
        }
      }
      sql.append(" and bill.privilege != 0 and bill.privilege is not null");
      query(sql, paras, map);
    }
    sql.append(" ) as bills");
    
    sql.append(" left join b_department t on bills.departmentId = t.id ");
    sql.append(" where (t.node= ? or  t.node is null)");
    paras.add(Integer.valueOf(AioConstants.NODE_1));
    
    sql.append(" group by bills.departmentId");
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
  
  private void query(StringBuffer sql, List<Object> paras, Map<String, Object> map)
  {
    if (map == null) {}
  }
}
