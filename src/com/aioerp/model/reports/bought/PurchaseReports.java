package com.aioerp.model.reports.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.reports.sell.SellReports;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseReports
  extends BaseDbModel
{
  public static final PurchaseReports dao = new PurchaseReports();
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
    
    String[] orderTypes = (String[])map.get("orderTypes");
    if ((orderTypes != null) && (StringUtils.isNotBlank(orderTypes[0]))) {
      sel.append(",t.discountMoney/t.baseAmount as avgPrice,t.taxMoney/t.baseAmount as taxPrice");
    }
    relevance(configName, privs, sql, orderTypes, supId, map);
    sql.append(" where 1=1");
    ComFunController.basePrivsSql(sql, (String)map.get("productPrivs"), "t");
    
    query(sql, paras, map);
    
    rank(sql, map);
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, sel.toString(), sql.toString(), paras.toArray());
  }
  
  public Map<String, Object> getDetailPage(String configName, String untiPrivs, String proPrivs, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws Exception
  {
    StringBuffer sel = new StringBuffer();
    StringBuffer sql = new StringBuffer();
    List<Object> paras = new ArrayList();
    Map<String, Class<? extends Model>> alias = new HashMap();
    if ((map == null) || (map.get("orderTypes") == null)) {
      return null;
    }
    detailSql(configName, untiPrivs, proPrivs, sel, sql, paras, map, alias);
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, paras.toArray());
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
        sql.append("  ,p.status,bill.id as billId,bill.unitId,bill.code,bill.staffId,bill.remark,bill.recodeDate,bill.isRCW");
        sql.append(" from cg_purchase_detail as p");
        sql.append(" left join cg_purchase_bill as bill on p.billId = bill.id");
        sql.append(" where 1=1 ");
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
        sql.append(" ,r.status,bill.id as billId,bill.unitId,bill.code,bill.staffId,bill.remark,bill.recodeDate,bill.isRCW");
        sql.append(" from cg_return_detail   as r");
        sql.append(" left join cg_return_bill as bill on r.billId = bill.id");
        sql.append(" where 1=1 ");
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
        sql.append(" ,r.status,bill.id as billId,bill.unitId,bill.code,bill.staffId,bill.remark,bill.recodeDate,bill.isRCW");
        sql.append(" from cg_barter_detail as r");
        sql.append(" left join cg_barter_bill as bill on r.billId = bill.id");
        sql.append(" where 1=1 ");
        ComFunController.queryProductPrivs(sql, (String)map.get("productPrivs"), "r");
        ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "r");
        ComFunController.queryDepartmentPrivs(sql, (String)map.get("departmentPrivs"), "bill");
      }
    }
    sql.append(") as detail ");
    sql.append(" left join b_product product on detail.productId = product.id");
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
      Integer unitId = (Integer)map.get("unitId");
      String chilIds = Unit.dao.getChildIdsBySupId(configName, unitPrivs, unitId.intValue());
      if (StringUtils.isBlank(chilIds)) {
        chilIds = "''";
      }
      sql.append(" and (detail.unitId in (" + chilIds + ") or detail.unitId= ?)");
      paras.add(unitId);
    }
    if (map.get("staffId") != null)
    {
      Integer staffId = (Integer)map.get("staffId");
      String chilIds = Staff.dao.getChildIdsBySupId(configName, staffId.intValue());
      if (StringUtils.isBlank(chilIds)) {
        chilIds = "''";
      }
      sql.append("  and (detail.staffId in (" + chilIds + ") or detail.staffId = ?)");
      paras.add(staffId);
    }
    if (map.get("storageId") != null)
    {
      Integer storageId = (Integer)map.get("storageId");
      String childIds = Storage.dao.getChildIdsBySupId(configName, storageId);
      if (StringUtils.isBlank(childIds)) {
        childIds = "''";
      }
      sql.append("  and (detail.storageId in (" + childIds + ") or detail.storageId = ?)");
      paras.add(storageId);
    }
    if (map.get("productId") != null)
    {
      Integer supId = (Integer)map.get("productId");
      String childIds = Product.dao.getChildIdsBySupId(configName, proPrivs, supId.intValue());
      if (StringUtils.isBlank(childIds)) {
        childIds = "''";
      }
      sql.append("  and (detail.productId in (" + childIds + ") or detail.productId = ?)");
      paras.add(supId);
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
  
  private void relevance(String configName, String privs, StringBuffer sql, String[] orderTypes, Integer supId, Map<String, Object> map)
  {
    StringBuffer sel = new StringBuffer("select b.*");
    StringBuffer from = new StringBuffer(" from b_product b");
    if ((orderTypes == null) || (StringUtils.isBlank(orderTypes[0])))
    {
      sql.append(sel.toString());
      sql.append(from.toString());
      sql.append(") as t");
      return;
    }
    StringBuffer stat = new StringBuffer();
    
    stat.append(" select product.pids, s.productId,sum(s.baseAmount) as baseAmount,sum(s.discountMoney) as discountMoney,sum(s.taxes) as taxes,sum(s.taxMoney) as taxMoney,sum(s.giftAmount) as giftAmount,sum(s.giftAmount)*product.retailPrice1 as retailMoney from (");
    for (int i = 0; i < orderTypes.length; i++)
    {
      if (i != 0) {
        stat.append(" UNION ALL");
      }
      if (AioConstants.BILL_ROW_TYPE5 == Integer.parseInt(orderTypes[i]))
      {
        stat.append(" SELECT p.productId");
        stat.append(",case when tit.isRCW = 2 then p.baseAmount *-1 else p.baseAmount end as baseAmount");
        stat.append(",case when tit.isRCW = 2 then p.discountMoney *-1 else p.discountMoney end as discountMoney");
        stat.append(",case when tit.isRCW = 2 then p.taxes *-1 else p.taxes end as taxes");
        stat.append(",case when tit.isRCW = 2 then p.taxMoney *-1 else p.taxMoney end as taxMoney");
        stat.append(",case when tit.isRCW = 2 then p.giftAmount *-1 else p.giftAmount end as giftAmount");
        stat.append(" from cg_purchase_detail p ");
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
          stat.append(" and (storage.pids like '%{" + map.get("storageId") + "}%' or storage.id = " + map.get("storageId") + ")");
        }
        if (map.get("unitId") != null) {
          stat.append(" and (tit.unitPs like '%{" + map.get("unitId") + "}%' or tit.unitId = " + map.get("unitId") + ")");
        }
        if (map.get("staffId") != null) {
          stat.append(" and (tit.staffPs like '%{" + map.get("staffId") + "}%' or tit.staffId = " + map.get("staffId") + ")");
        }
      }
      else if (AioConstants.BILL_ROW_TYPE6 == Integer.parseInt(orderTypes[i]))
      {
        stat.append(" SELECT r.productId");
        stat.append(",case when tit.isRCW != 2 then r.baseAmount *-1 else r.baseAmount end as baseAmount");
        stat.append(",case when tit.isRCW != 2 then r.discountMoney *-1 else r.discountMoney end as discountMoney");
        stat.append(",case when tit.isRCW != 2 then r.taxes *-1 else r.taxes end as taxes");
        stat.append(",case when tit.isRCW != 2 then r.taxMoney *-1 else r.taxMoney end as taxMoney");
        stat.append(",case when tit.isRCW != 2 then r.giftAmount *-1 else r.giftAmount end as giftAmount");
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
          stat.append(" and (storage.pids like '%{" + map.get("storageId") + "}%' or storage.id = " + map.get("storageId") + ")");
        }
        if (map.get("unitId") != null) {
          stat.append(" and (tit.unitPs like '%{" + map.get("unitId") + "}%' or tit.unitId = " + map.get("unitId") + ")");
        }
        if (map.get("staffId") != null) {
          stat.append(" and (tit.staffPs like '%{" + map.get("staffId") + "}%' or tit.staffId = " + map.get("staffId") + ")");
        }
      }
      else if (AioConstants.BILL_ROW_TYPE12 == Integer.parseInt(orderTypes[i]))
      {
        stat.append(" SELECT r.productId");
        stat.append(",case when tit.isRCW != 2 and r.type=2 then r.baseAmount*-1  when tit.isRCW != 2 and r.type=1 then r.baseAmount when tit.isRCW=2 and r.type=2 then r.baseAmount when tit.isRCW=2 and r.type=1 then r.baseAmount*-1 end as baseAmount");
        stat.append(",case when tit.isRCW != 2 and r.type=2 then r.discountMoney*-1  when tit.isRCW != 2 and r.type=1 then r.discountMoney when tit.isRCW=2 and r.type=2 then r.discountMoney when tit.isRCW=2 and r.type=1 then r.discountMoney*-1 end as discountMoney");
        stat.append(",case when tit.isRCW != 2 and r.type=2 then r.taxes*-1  when tit.isRCW != 2 and r.type=1 then r.taxes when tit.isRCW=2 and r.type=2 then r.taxes when tit.isRCW=2 and r.type=1 then r.taxes*-1 end as taxes");
        stat.append(",case when tit.isRCW != 2 and r.type=2 then r.taxMoney*-1  when tit.isRCW != 2 and r.type=1 then r.taxMoney when tit.isRCW=2 and r.type=2 then r.taxMoney when tit.isRCW=2 and r.type=1 then r.taxMoney*-1 end as taxMoney");
        stat.append(",case when tit.isRCW != 2 and r.type=2 then r.giftAmount*-1  when tit.isRCW != 2 and r.type=1 then r.giftAmount when tit.isRCW=2 and r.type=2 then r.giftAmount when tit.isRCW=2 and r.type=1 then r.giftAmount*-1 end as giftAmount");
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
          stat.append(" and (storage.pids like '%{" + map.get("storageId") + "}%' or storage.id = " + map.get("storageId") + ")");
        }
        if (map.get("unitId") != null) {
          stat.append(" and (tit.unitPs like '%{" + map.get("unitId") + "}%' or tit.unitId = " + map.get("unitId") + ")");
        }
        if (map.get("staffId") != null) {
          stat.append(" and (tit.staffPs like '%{" + map.get("staffId") + "}%' or tit.staffId = " + map.get("staffId") + ")");
        }
      }
    }
    stat.append(" ) as s left join b_product product on s.productId = product.id group by s.productId ");
    
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
}
