package com.aioerp.model.reports.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.reports.sell.SellReports;
import com.jfinal.plugin.activerecord.Model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseUnitReports
  extends BaseDbModel
{
  public static final PurchaseUnitReports dao = new PurchaseUnitReports();
  private Unit unit;
  private Storage storage;
  
  public Unit getUnit(String configName)
  {
    if (this.unit == null) {
      this.unit = ((Unit)Unit.dao.findById(configName, get("unitId")));
    }
    return this.unit;
  }
  
  public Storage getStorage(String configName)
  {
    if (this.storage == null) {
      this.storage = ((Storage)Storage.dao.findById(configName, get("storageId")));
    }
    return this.storage;
  }
  
  public Map<String, Object> getPage(String configName, String unitPrivs, int pageNum, int numPerPage, String listID, Map<String, Object> map)
  {
    Integer supId = (Integer)map.get("supId");
    
    StringBuffer sel = new StringBuffer("select t.*");
    StringBuffer sql = new StringBuffer(" from (");
    List<Object> paras = new ArrayList();
    
    String[] orderTypes = (String[])map.get("orderTypes");
    if ((orderTypes != null) && (StringUtils.isNotBlank(orderTypes[0]))) {
      sel.append(",t.discountMoney/t.baseAmount as avgPrice,t.taxMoney/t.baseAmount as taxPrice");
    }
    relevance(configName, unitPrivs, sql, orderTypes, supId, map);
    
    sql.append(" where 1=1");
    ComFunController.basePrivsSql(sql, unitPrivs, "t");
    if (map != null) {
      query(sql, paras, map);
    }
    rank(sql, map);
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, sel.toString(), sql.toString(), paras.toArray());
  }
  
  public Map<String, Object> getRowPage(String configName, String unitPrivs, int pageNum, int numPerPage, String listID, Map<String, Object> map)
  {
    Integer supId = (Integer)map.get("supId");
    
    StringBuffer sel = new StringBuffer("select t.*");
    StringBuffer sql = new StringBuffer(" from (");
    List<Object> paras = new ArrayList();
    
    String[] orderTypes = (String[])map.get("orderTypes");
    relevance(configName, unitPrivs, sql, orderTypes, supId, map);
    
    sql.append(" where 1=1");
    ComFunController.basePrivsSql(sql, unitPrivs, "t");
    sql.append(" and t.node = 1");
    sql.append(" and t.pids like '%{" + supId + "}%'");
    if ((orderTypes != null) && (StringUtils.isNotBlank(orderTypes[0]))) {
      sql.append(" and t.baseAmount > 0 ");
    }
    rank(sql, map);
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, sel.toString(), sql.toString(), paras.toArray());
  }
  
  public Map<String, Object> getDetailPage(String configName, String unitPrivs, String proPrivs, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws Exception
  {
    StringBuffer sel = new StringBuffer();
    StringBuffer sql = new StringBuffer();
    List<Object> paras = new ArrayList();
    Map<String, Class<? extends Model>> alias = new HashMap();
    if ((map == null) || (map.get("orderTypes") == null)) {
      return null;
    }
    detailSql(configName, unitPrivs, proPrivs, sel, sql, paras, map, alias);
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, paras.toArray());
  }
  
  public Map<String, Object> getSpreadPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws Exception
  {
    String[] orderTypes = (String[])map.get("orderTypes");
    List<Model> unitList = (List)map.get("unitList");
    String startDate = (String)map.get("startDate");
    String endDate = (String)map.get("endDate");
    Integer staffId = Integer.valueOf((Integer)map.get("staffId") == null ? 0 : ((Integer)map.get("staffId")).intValue());
    Integer storageId = Integer.valueOf((Integer)map.get("storageId") == null ? 0 : ((Integer)map.get("storageId")).intValue());
    String spreadWay = (String)map.get("spreadWay");
    
    String unitPrivs = String.valueOf(map.get("unitPrivs"));
    String staffPrivs = String.valueOf(map.get("staffPrivs"));
    String storagePrivs = String.valueOf(map.get("storagePrivs"));
    String productPrivs = String.valueOf(map.get("productPrivs"));
    String departmentPrivs = String.valueOf(map.get("departmentPrivs"));
    
    StringBuffer selectSql = sumUnionSql(unitList, "unit", startDate, endDate, staffId.intValue(), spreadWay);
    StringBuffer fromSql = new StringBuffer("from b_product pro ");
    fromSql.append(" left join (" + unionallSql(unitPrivs, staffPrivs, storagePrivs, departmentPrivs, orderTypes, startDate, endDate, 
      unitList, staffId.intValue(), String.valueOf(storageId)).toString() + ") t on t.pids LIKE CONCAT(pro.pids,'%') ");
    fromSql.append(" where 1=1");
    
    int supId = Integer.valueOf(String.valueOf(map.get("supId"))).intValue();
    
    boolean flag = SellReports.addReportBaseCondition(fromSql, "pro", String.valueOf(map.get("searchBaseAttr")), String.valueOf(map.get("searchBaseVal")));
    if (flag) {
      if ((map.get("isRow") != null) && ("true".equals(map.get("isRow")))) {
        fromSql.append(" and pro.pids like '%{" + supId + "}%'");
      } else if (supId == 0) {
        fromSql.append(" and pro.supId = 0");
      } else {
        fromSql.append(" and pro.supId = " + supId);
      }
    }
    fromSql.append(" and pro.status=" + AioConstants.STATUS_ENABLE);
    if ((map.get("isRow") != null) && ("true".equals(map.get("isRow")))) {
      fromSql.append(" and pro.node = 1");
    }
    ComFunController.queryProductPrivs(fromSql, productPrivs, "pro", "id");
    fromSql.append(" group by pro.id ");
    if (map != null)
    {
      String orderField = (String)map.get("orderField");
      String orderDirection = (String)map.get("orderDirection");
      if ((StringUtils.isNotBlank(orderField)) && (StringUtils.isNotBlank(orderDirection))) {
        fromSql.append(" order by " + orderField + " " + orderDirection);
      }
    }
    return aioGetPageRecord(configName, Product.dao, listID, pageNum, 
      numPerPage, selectSql.toString(), fromSql.toString(), new Object[0]);
  }
  
  public Map<String, Object> getPrivilegePage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
  {
    StringBuffer sel = new StringBuffer("select t.*");
    sel.append(",sum(case when bills.isRCW != 2 then bills.taxMoneys else bills.taxMoneys*-1 end)as taxMoneys");
    sel.append(",sum(case when bills.isRCW != 2 then bills.privilege else bills.privilege*-1 end)as privilege");
    sel.append(",sum(case when bills.isRCW != 2 then bills.privilegeMoney else bills.privilegeMoney*-1 end)as privilegeMoney");
    StringBuffer sql = new StringBuffer(" from b_unit t");
    List<Object> paras = new ArrayList();
    String[] tables = { "cg_purchase_bill", "cg_return_bill", "cg_barter_bill" };
    sql.append(" left join (");
    for (int i = 0; i < tables.length; i++)
    {
      if (i != 0) {
        sql.append(" UNION ALL");
      }
      if ("cg_barter_bill".equals(tables[i])) {
        sql.append(" select bill.unitId,bill.isRCW,bill.gapMoney as taxMoneys,bill.privilege,bill.privilegeMoney from " + tables[i] + " bill");
      } else if ("cg_return_bill".equals(tables[i])) {
        sql.append(" select bill.unitId,bill.isRCW,bill.taxMoneys*-1 as taxMoneys,bill.privilege*-1 as privilege,bill.privilegeMoney*-1 as privilegeMoney from " + tables[i] + " bill");
      } else {
        sql.append(" select bill.unitId,bill.isRCW,bill.taxMoneys,bill.privilege,bill.privilegeMoney from " + tables[i] + " bill");
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
        if (map.get("staffId") != null)
        {
          sql.append(" and (bill.staffId in (select staff.id from b_staff staff where staff.pids like ?) or bill.staffId = ?)");
          paras.add("'%{" + map.get("staffId") + "}%'");
          paras.add(map.get("staffId"));
        }
      }
      sql.append(" and bill.privilege != 0 and bill.privilege is not null");
      query(sql, paras, map);
    }
    sql.append(" ) as bills on bills.unitId = t.id");
    sql.append(" where t.node= ?");
    paras.add(Integer.valueOf(AioConstants.NODE_1));
    sql.append(" and bills.privilege != 0 and bills.privilege is not null");
    sql.append(" group by t.id");
    if (map != null)
    {
      String orderField = (String)map.get("orderField");
      String orderDirection = (String)map.get("orderDirection");
      if ((StringUtils.isNotBlank(orderField)) && (StringUtils.isNotBlank(orderDirection))) {
        sql.append(" order by " + orderField + " " + orderDirection);
      }
    }
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, sel.toString(), sql.toString(), paras.toArray());
  }
  
  private void detailSql(String configName, String unitPrivs, String proPrivs, StringBuffer sel, StringBuffer sql, List<Object> paras, Map<String, Object> map, Map<String, Class<? extends Model>> alias)
  {
    if (map == null) {
      return;
    }
    String[] orderTypes = (String[])map.get("orderTypes");
    sql.append(" from (");
    for (int i = 0; i < orderTypes.length; i++)
    {
      if (i != 0) {
        sql.append(" UNION ALL");
      }
      if (AioConstants.BILL_ROW_TYPE5 == Integer.parseInt(orderTypes[i]))
      {
        sql.append(" select p.productId,p.id,5 as type ,p.storageId,p.basePrice");
        sql.append(",case when bill.isRCW = 2 then null else p.baseAmount end as pAmount");
        sql.append(",case when bill.isRCW = 2 then p.baseAmount else null end as rAmount");
        sql.append(",case when bill.isRCW = 2 then p.baseAmount*p.basePrice*-1 else p.baseAmount*p.basePrice end as money");
        sql.append("  ,p.status,bill.id as billId,bill.unitId,bill.staffId,bill.code,bill.remark,bill.recodeDate,bill.isRCW");
        sql.append(" from cg_purchase_detail as p");
        sql.append(" left join cg_purchase_bill as bill on p.billId = bill.id");
        sql.append(" where 1=1");
        ComFunController.queryProductPrivs(sql, (String)map.get("productPrivs"), "p");
        ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "p");
        ComFunController.queryDepartmentPrivs(sql, (String)map.get("departmentPrivs"), "bill");
      }
      else if (AioConstants.BILL_ROW_TYPE6 == Integer.parseInt(orderTypes[i]))
      {
        sql.append(" select r.productId,r.id,6 as type ,r.storageId,r.basePrice");
        sql.append(",case when bill.isRCW != 2 then null else r.baseAmount end as pAmount");
        sql.append(",case when bill.isRCW != 2 then r.baseAmount else null end as rAmount");
        sql.append(",case when bill.isRCW != 2 then r.baseAmount*r.basePrice*-1 else r.baseAmount*r.basePrice end as money");
        sql.append(" ,r.status ,bill.id as billId,bill.unitId,bill.staffId,bill.code,bill.remark,bill.recodeDate,bill.isRCW");
        sql.append(" from cg_return_detail as r");
        sql.append(" left join cg_return_bill as bill on r.billId = bill.id");
        sql.append(" where 1=1");
        ComFunController.queryProductPrivs(sql, (String)map.get("productPrivs"), "r");
        ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "r");
        ComFunController.queryDepartmentPrivs(sql, (String)map.get("departmentPrivs"), "bill");
      }
      else if (AioConstants.BILL_ROW_TYPE12 == Integer.parseInt(orderTypes[i]))
      {
        sql.append(" select r.productId,r.id,12 as type ,r.storageId,r.basePrice");
        sql.append(",case when bill.isRCW != 2 and r.type=2 then null when bill.isRCW = 2 and r.type=2 then r.baseAmount when bill.isRCW = 2 and r.type=1 then null when bill.isRCW != 2 and r.type=1 then r.baseAmount end as pAmount");
        sql.append(",case when bill.isRCW != 2 and r.type=2 then r.baseAmount when bill.isRCW = 2 and r.type=2 then null when bill.isRCW = 2 and r.type=1 then r.baseAmount when bill.isRCW != 2 and r.type=1 then null end as rAmount");
        sql.append(",case when bill.isRCW != 2 and r.type=2 then r.baseAmount*r.basePrice*-1 when bill.isRCW = 2 and r.type=2 then r.baseAmount*r.basePrice when bill.isRCW = 2 and r.type=1 then r.baseAmount*r.basePrice*-1 when bill.isRCW != 2 and r.type=1 then r.baseAmount*r.basePrice end as money");
        sql.append(" ,r.status,bill.id as billId,bill.unitId,bill.staffId,bill.code,bill.remark,bill.recodeDate,bill.isRCW");
        sql.append(" from cg_barter_detail as r");
        sql.append(" left join cg_barter_bill as bill on r.billId = bill.id");
        sql.append(" where 1=1");
        ComFunController.queryProductPrivs(sql, (String)map.get("productPrivs"), "r");
        ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "r");
        ComFunController.queryDepartmentPrivs(sql, (String)map.get("departmentPrivs"), "bill");
      }
    }
    sql.append(") as detail ");
    sql.append(" left join b_product as product on detail.productId = product.id");
    sql.append(" left join b_storage storage on detail.storageId = storage.id");
    sql.append(" left join  b_unit unit on detail.unitId = unit.id");
    sql.append(" left join  b_staff staff on detail.staffId = staff.id");
    sql.append(" where 1=1");
    ComFunController.queryUnitPrivs(sql, (String)map.get("unitPrivs"), "detail");
    ComFunController.queryStaffPrivs(sql, (String)map.get("staffPrivs"), "detail");
    alias.put("product", Product.class);
    sel.append(" select detail.*,product.*,storage.code as storageCode,storage.fullName as storageName");
    sel.append(" ,unit.code as unitCode, unit.fullName as unitName");
    if (map.get("unitId") != null)
    {
      Integer supId = (Integer)map.get("unitId");
      String childIds = Unit.dao.getChildIdsBySupId(configName, unitPrivs, supId.intValue());
      if (StringUtils.isNotBlank(childIds)) {
        sql.append("  and (detail.unitId in (" + childIds + ") or detail.unitId = ?)");
      } else {
        sql.append("  and  detail.unitId = ?");
      }
      paras.add(supId);
    }
    if (map.get("staffId") != null)
    {
      Integer staffId = (Integer)map.get("staffId");
      String chilIds = Staff.dao.getChildIdsBySupId(configName, staffId.intValue());
      if (StringUtils.isNotBlank(chilIds)) {
        sql.append("  and (detail.staffId in (" + chilIds + ") or detail.staffId = ?)");
      } else {
        sql.append("  and  detail.staffId = ?");
      }
      paras.add(staffId);
    }
    if (map.get("storageId") != null)
    {
      Integer storageId = (Integer)map.get("storageId");
      String childIds = Storage.dao.getChildIdsBySupId(configName, storageId);
      if (StringUtils.isNotBlank(childIds)) {
        sql.append("  and (detail.storageId in (" + childIds + ") or detail.storageId = ?)");
      } else {
        sql.append("  and detail.storageId = ?");
      }
      paras.add(storageId);
    }
    if (map.get("productId") != null)
    {
      Integer productId = (Integer)map.get("productId");
      String childIds = Product.dao.getChildIdsBySupId(configName, proPrivs, productId.intValue());
      if (StringUtils.isNotBlank(childIds)) {
        sql.append("  and (detail.productId in (" + childIds + ") or detail.productId = ?)");
      } else {
        sql.append("  and detail.productId = ?");
      }
      paras.add(productId);
    }
    if (map.get("startDate") != null) {
      sql.append(" and recodeDate >= '" + map.get("startDate") + "'");
    }
    if (map.get("endDate") != null) {
      sql.append(" and recodeDate <= '" + map.get("endDate") + " 23:59:59'");
    }
    if (map.get("notStatus") != null)
    {
      sql.append(" and detail.isRCW = ?");
      paras.add(map.get("notStatus"));
    }
    rank(sql, map);
  }
  
  private void rank(StringBuffer sql, Map<String, Object> map)
  {
    String[] orderTypes = (String[])map.get("orderTypes");
    String orderField = (String)map.get("orderField");
    String orderDirection = (String)map.get("orderDirection");
    if ((StringUtils.isBlank(orderField)) || (StringUtils.isBlank(orderDirection)))
    {
      sql.append(" order by recodeDate");
      return;
    }
    if (orderTypes == null)
    {
      if ("code".equals(orderField)) {
        sql.append(" order by code " + orderDirection);
      } else if ("fullName".equals(orderField)) {
        sql.append(" order by fullName " + orderDirection);
      } else if ("assistUnit".equals(orderField)) {
        sql.append(" order by assistUnit " + orderDirection);
      }
      return;
    }
    sql.append(" order by " + orderField + " " + orderDirection);
  }
  
  private void relevance(String configName, String unitPrivs, StringBuffer sql, String[] orderTypes, Integer supId, Map<String, Object> map)
  {
    StringBuffer sel = new StringBuffer("select b.*");
    StringBuffer from = new StringBuffer(" from b_unit b ");
    if ((orderTypes == null) || (StringUtils.isBlank(orderTypes[0])))
    {
      sql.append(sel.toString());
      sql.append(from.toString());
      sql.append(") as t");
      return;
    }
    StringBuffer stat = new StringBuffer();
    
    stat.append(" select unit.pids, s.unitId,sum(s.baseAmount) as baseAmount,sum(s.discountMoney) as discountMoney,sum(s.taxes) as taxes,sum(s.taxMoney) as taxMoney,sum(s.giftAmount) as giftAmount,sum(s.retailMoney) as retailMoney,sum(several) as several from (");
    for (int i = 0; i < orderTypes.length; i++)
    {
      if (i != 0) {
        stat.append(" UNION ALL");
      }
      if (AioConstants.BILL_ROW_TYPE5 == Integer.parseInt(orderTypes[i]))
      {
        stat.append(" select p.id,p.unitId,p.staffId");
        stat.append(",case when p.isRCW = 2 then sum(cg.baseAmount)*-1 else sum(cg.baseAmount) end as baseAmount");
        stat.append(",case when p.isRCW = 2 then sum(cg.discountMoney)*-1 else sum(cg.discountMoney) end as discountMoney");
        stat.append(",case when p.isRCW = 2 then sum(cg.taxes)*-1 else sum(cg.taxes) end as taxes");
        stat.append(",case when p.isRCW = 2 then sum(cg.taxMoney)*-1 else sum(cg.taxMoney) end as taxMoney");
        stat.append(",case when p.isRCW = 2 then sum(cg.giftAmount)*-1 else sum(cg.giftAmount) end as giftAmount");
        stat.append(",case when p.isRCW = 2 then sum(cg.giftAmount*cg.retailPrice1)*-1 else sum(cg.giftAmount*cg.retailPrice1) end as retailMoney");
        stat.append(",case when p.isRCW = 2 then -1 else 1 end as several");
        stat.append(" from cg_purchase_bill p");
        stat.append(" right join (");
        stat.append(" select detail.*,storage.pids as storagePs,product.pids as productPs,product.retailPrice1 from cg_purchase_detail detail ");
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
          stat.append(" and (cg.storagePs like '%{" + map.get("storageId") + "}%' or cg.storageId = " + map.get("storageId") + ")");
        }
        if (map.get("productId") != null) {
          stat.append(" and (cg.productPs like '%{" + map.get("productId") + "}%' or cg.productId = " + map.get("productId") + ")");
        }
        if (map.get("staffId") != null) {
          stat.append(" and (staff.pids like '%{" + map.get("staffId") + "}%' or staff.id= " + map.get("staffId") + ")");
        }
        stat.append(" group by p.id");
      }
      else if (AioConstants.BILL_ROW_TYPE6 == Integer.parseInt(orderTypes[i]))
      {
        stat.append(" select p.id,p.unitId,p.staffId");
        stat.append(",case when p.isRCW != 2 then sum(cg.baseAmount)*-1 else sum(cg.baseAmount) end as baseAmount");
        stat.append(",case when p.isRCW != 2 then sum(cg.discountMoney)*-1 else sum(cg.discountMoney) end as discountMoney");
        stat.append(",case when p.isRCW != 2 then sum(cg.taxes)*-1 else sum(cg.taxes) end as taxes");
        stat.append(",case when p.isRCW != 2 then sum(cg.taxMoney)*-1 else sum(cg.taxMoney) end as taxMoney");
        stat.append(",case when p.isRCW != 2 then sum(cg.giftAmount)*-1 else sum(cg.giftAmount) end as giftAmount");
        stat.append(",case when p.isRCW != 2 then sum(cg.giftAmount*cg.retailPrice1)*-1 else sum(cg.giftAmount*cg.retailPrice1) end as retailMoney");
        stat.append(",0 as several");
        stat.append(" from  cg_return_bill p");
        stat.append(" right join (");
        stat.append(" select detail.*,storage.pids as storagePs,product.pids as productPs,product.retailPrice1 from cg_return_detail detail ");
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
          stat.append(" and (cg.storagePs like '%{" + map.get("storageId") + "}%' or cg.storageId = " + map.get("storageId") + ")");
        }
        if (map.get("productId") != null) {
          stat.append(" and (cg.productPs like '%{" + map.get("productId") + "}%' or cg.productId = " + map.get("productId") + ")");
        }
        if (map.get("staffId") != null) {
          stat.append(" and (staff.pids like '%{" + map.get("staffId") + "}%' or staff.id= " + map.get("staffId") + ")");
        }
        stat.append(" group by p.id");
      }
      else if (AioConstants.BILL_ROW_TYPE12 == Integer.parseInt(orderTypes[i]))
      {
        stat.append(" select p.id,p.unitId,p.staffId");
        stat.append(",case when p.isRCW != 2 and cg.type=2 then sum(cg.baseAmount)*-1 when p.isRCW = 2 and cg.type=2 then sum(cg.baseAmount) when p.isRCW = 2 and cg.type=1 then sum(cg.baseAmount)*-1 when p.isRCW != 2 and cg.type=1 then sum(cg.baseAmount) end as baseAmount");
        stat.append(",case when p.isRCW != 2 and cg.type=2 then sum(cg.discountMoney)*-1 when p.isRCW = 2 and cg.type=2 then sum(cg.discountMoney) when p.isRCW = 2 and cg.type=1 then sum(cg.discountMoney)*-1 when p.isRCW != 2 and cg.type=1 then sum(cg.discountMoney) end as discountMoney");
        stat.append(",case when p.isRCW != 2 and cg.type=2 then sum(cg.taxes)*-1 when p.isRCW = 2 and cg.type=2 then sum(cg.taxes) when p.isRCW = 2 and cg.type=1 then sum(cg.taxes)*-1 when p.isRCW != 2 and cg.type=1 then sum(cg.taxes) end as taxes");
        stat.append(",case when p.isRCW != 2 and cg.type=2 then sum(cg.taxMoney)*-1 when p.isRCW = 2 and cg.type=2 then sum(cg.taxMoney) when p.isRCW = 2 and cg.type=1 then sum(cg.taxMoney)*-1 when p.isRCW != 2 and cg.type=1 then sum(cg.taxMoney) end as taxMoney");
        stat.append(",case when p.isRCW != 2 and cg.type=2 then sum(cg.giftAmount)*-1 when p.isRCW = 2 and cg.type=2 then sum(cg.giftAmount) when p.isRCW = 2 and cg.type=1 then sum(cg.giftAmount)*-1 when p.isRCW != 2 and cg.type=1 then sum(cg.giftAmount) end as giftAmount");
        stat.append(",case when p.isRCW != 2 and cg.type=2 then sum(cg.giftAmount*cg.retailPrice1)*-1 when p.isRCW = 2 and cg.type=2 then sum(cg.giftAmount*cg.retailPrice1) when p.isRCW = 2 and cg.type=1 then sum(cg.giftAmount*cg.retailPrice1)*-1 when p.isRCW != 2 and cg.type=1 then sum(cg.giftAmount*cg.retailPrice1) end as retailMoney");
        stat.append(",0 as several");
        stat.append(" from  cg_barter_bill p");
        stat.append(" right join (");
        stat.append(" select detail.*,storage.pids as storagePs,product.pids as productPs,product.retailPrice1 from cg_barter_detail detail ");
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
          stat.append(" and (cg.storagePs like '%{" + map.get("storageId") + "}%' or cg.storageId = " + map.get("storageId") + ")");
        }
        if (map.get("productId") != null) {
          stat.append(" and (cg.productPs like '%{" + map.get("productId") + "}%' or cg.productId = " + map.get("productId") + ")");
        }
        if (map.get("staffId") != null) {
          stat.append(" and (staff.pids like '%{" + map.get("staffId") + "}%' or staff.id= " + map.get("staffId") + ")");
        }
        stat.append(" group by p.id,cg.type");
      }
    }
    stat.append(" ) as s left join b_unit unit on s.unitId = unit.id group by s.unitId ");
    
    StringBuffer baseAmount = new StringBuffer(" ,(select sum(bill.baseAmount) from (");
    baseAmount.append(stat.toString());
    baseAmount.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as baseAmount");
    sel.append(baseAmount.toString());
    
    StringBuffer discountMoney = new StringBuffer(" ,(select sum(bill.discountMoney) from (");
    discountMoney.append(stat.toString());
    discountMoney.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as discountMoney");
    sel.append(discountMoney.toString());
    
    StringBuffer taxes = new StringBuffer(" ,(select sum(bill.taxes) from (");
    taxes.append(stat.toString());
    taxes.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as taxes");
    sel.append(taxes.toString());
    
    StringBuffer taxMoney = new StringBuffer(" ,(select sum(bill.taxMoney) from (");
    taxMoney.append(stat.toString());
    taxMoney.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as taxMoney");
    sel.append(taxMoney.toString());
    
    StringBuffer giftAmount = new StringBuffer(" ,(select sum(bill.giftAmount) from (");
    giftAmount.append(stat.toString());
    giftAmount.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as giftAmount");
    sel.append(giftAmount.toString());
    
    StringBuffer retailMoney = new StringBuffer(" ,(select sum(bill.retailMoney) from (");
    retailMoney.append(stat.toString());
    retailMoney.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as retailMoney");
    sel.append(retailMoney.toString());
    
    StringBuffer several = new StringBuffer(" ,(select sum(bill.several) from (");
    several.append(stat.toString());
    several.append(" ) as bill where bill.pids like concat('%{',b.id,'}%')) as several");
    sel.append(several.toString());
    
    sql.append(sel.toString());
    sql.append(from.toString());
    sql.append(") as t");
  }
  
  private void query(StringBuffer sql, List<Object> paras, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    boolean flag = SellReports.addReportBaseCondition(sql, "t", String.valueOf(map.get("searchBaseAttr")), String.valueOf(map.get("searchBaseVal")));
    if (map.get("supId") != null)
    {
      int supId = Integer.valueOf(String.valueOf(map.get("supId"))).intValue();
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
    }
    if ((map.get("isRow") != null) && ("true".equals(map.get("isRow")))) {
      sql.append(" and t.node = 1");
    }
    if (map.get("shoeType") != null)
    {
      Integer shoeType = (Integer)map.get("shoeType");
      if (shoeType.intValue() == 0) {
        sql.append(" and baseAmount = 0 or baseAmount is null");
      } else {
        sql.append(" and baseAmount > 0 ");
      }
    }
  }
  
  private StringBuffer sumUnionSql(List<Model> objList, String modelType, String startDate, String endDate, int staffId, String aimDiv)
  {
    StringBuffer all = new StringBuffer("select pro.*, ");
    for (Model obj : objList)
    {
      int objId = obj.getInt("id").intValue();
      if (modelType.equals("unit"))
      {
        all.append("sum(case when t.unitId=" + objId + " then t.baseAmount else 0 end ) as baseAmount" + objId + ", ");
        if ("0".equals(aimDiv)) {
          all.append("sum(case when t.unitId=" + objId + " then t.taxMoney else 0 end ) as money" + objId + ", ");
        } else {
          all.append("sum(case when t.unitId=" + objId + " then t.discountMoney else 0 end ) as money" + objId + ", ");
        }
      }
      else if (modelType.equals("storage"))
      {
        all.append("sum(case when t.storageId=" + objId + " then t.baseAmount else 0 end ) as baseAmount" + objId + ", ");
        if ("0".equals(aimDiv)) {
          all.append("sum(case when t.storageId=" + objId + " then t.taxMoney else 0 end ) as money" + objId + ", ");
        } else {
          all.append("sum(case when t.storageId=" + objId + " then t.discountMoney else 0 end ) as money" + objId + ", ");
        }
      }
    }
    all.append("sum(t.baseAmount) as allAmount, ");
    if ("0".equals(aimDiv)) {
      all.append("sum(t.taxMoney) as allMoney ");
    } else {
      all.append("sum(t.discountMoney) as allMoney ");
    }
    return all;
  }
  
  private StringBuffer unionallSql(String unitPrivs, String staffPrivs, String storagePrivs, String departmentPrivs, String[] orderTypes, String startDate, String endDate, List<Model> unitIds, int staffId, String storageId)
  {
    StringBuffer unionSql = new StringBuffer();
    StringBuffer singleSql = null;
    String billTable = "";
    String billDetailTable = "";
    for (int i = 0; i < orderTypes.length; i++)
    {
      Integer type = Integer.valueOf(Integer.parseInt(orderTypes[i]));
      if (AioConstants.BILL_ROW_TYPE5 == type.intValue())
      {
        singleSql = new StringBuffer("select product.pids,detail.productId,bill.unitId,detail.storageId,detail.basePrice ");
        singleSql.append(",(case when bill.isRCW != 2 then detail.baseAmount else detail.baseAmount*-1 end) baseAmount");
        singleSql.append(",(case when bill.isRCW != 2 then detail.discountMoney else detail.discountMoney*-1 end) discountMoney");
        singleSql.append(",(case when bill.isRCW != 2 then detail.taxMoney else detail.taxMoney*-1 end) taxMoney");
        billTable = "cg_purchase_bill";
        billDetailTable = "cg_purchase_detail";
      }
      else if (AioConstants.BILL_ROW_TYPE6 == type.intValue())
      {
        singleSql = new StringBuffer("select product.pids,detail.productId,bill.unitId,detail.storageId,detail.basePrice");
        singleSql.append(",(case when bill.isRCW != 2 then detail.baseAmount*-1 else detail.baseAmount end) baseAmount");
        singleSql.append(",(case when bill.isRCW != 2 then detail.discountMoney*-1 else detail.discountMoney end) discountMoney");
        singleSql.append(",(case when bill.isRCW != 2 then detail.taxMoney*-1 else detail.taxMoney end) taxMoney");
        billTable = "cg_return_bill";
        billDetailTable = "cg_return_detail";
      }
      else if (AioConstants.BILL_ROW_TYPE12 == type.intValue())
      {
        singleSql = new StringBuffer("select product.pids,detail.productId,bill.unitId,detail.storageId,detail.basePrice");
        singleSql.append(",(case when (bill.isRCW != 2 and detail.type=1) or (bill.isRCW = 2 and detail.type!=1) then detail.baseAmount*-1 else detail.baseAmount end) baseAmount");
        singleSql.append(",(case when (bill.isRCW != 2 and detail.type=1) or (bill.isRCW = 2 and detail.type!=1) then detail.discountMoney*-1 else detail.discountMoney end) discountMoney");
        singleSql.append(",(case when (bill.isRCW != 2 and detail.type=1) or (bill.isRCW = 2 and detail.type!=1) then detail.taxMoney*-1 else detail.taxMoney end) taxMoney");
        billTable = "cg_barter_bill";
        billDetailTable = "cg_barter_detail";
      }
      singleSql.append(" from " + billDetailTable + " detail left join " + billTable + " bill on detail.billId=bill.id ");
      singleSql.append(" left join b_storage storage on storage.id=detail.storageId ");
      singleSql.append(" left join b_unit unit on unit.id=bill.unitId");
      singleSql.append(" left join b_staff staff on staff.id=bill.staffId");
      singleSql.append(" left join b_department department on department.id=bill.departmentId");
      singleSql.append(" left join b_product product on product.id=detail.productId where 1=1 ");
      singleSql.append(" and (bill.recodeDate between '" + startDate + "' and '" + endDate + " 23:59:59') ");
      if ((unitIds != null) && (unitIds.size() != 0))
      {
        for (int j = 0; j < unitIds.size(); j++) {
          if (j == 0) {
            singleSql.append(" and ( unit.pids like '%{" + ((Model)unitIds.get(j)).getInt("id") + "}%' ");
          } else {
            singleSql.append(" or unit.pids like '%{" + ((Model)unitIds.get(j)).getInt("id") + "}%' ");
          }
        }
        singleSql.append(")");
      }
      if (staffId != 0) {
        singleSql.append(" and staff.pids like '%{" + staffId + "}%' ");
      }
      if ((storageId != null) && (!storageId.equals("")) && (storageId.equals("0")))
      {
        String[] stoArr = storageId.split(",");
        for (int j = 0; j < stoArr.length; j++) {
          if (j == 0) {
            singleSql.append(" and ( storage.pids like '%{" + stoArr[j] + "}%' ");
          } else {
            singleSql.append(" or storage.pids like '%{" + stoArr[j] + "}%' ");
          }
        }
        singleSql.append(")");
      }
      ComFunController.queryUnitPrivs(singleSql, unitPrivs, "unit", "id");
      ComFunController.queryStaffPrivs(singleSql, staffPrivs, "staff", "id");
      ComFunController.queryStoragePrivs(singleSql, storagePrivs, "storage", "id");
      ComFunController.queryDepartmentPrivs(singleSql, departmentPrivs, "department", "id");
      
      unionSql.append(singleSql);
      if ((orderTypes.length > 1) && (orderTypes.length - 1 != i)) {
        unionSql.append("union all ");
      }
    }
    return unionSql;
  }
}
