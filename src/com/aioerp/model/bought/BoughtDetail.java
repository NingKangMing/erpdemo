package com.aioerp.model.bought;

import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class BoughtDetail
  extends BaseDbModel
{
  public static final BoughtDetail dao = new BoughtDetail();
  public static final String TABLE_NAME = "cg_bought_detail";
  
  public BigDecimal getReplenishMoney()
  {
    BigDecimal replenishMoney = BigDecimalUtils.mul(getBigDecimal("replenishAmount"), getBigDecimal("basePrice"));
    if (BigDecimalUtils.compare(replenishMoney, BigDecimal.ZERO) == 0) {
      replenishMoney = null;
    }
    return replenishMoney;
  }
  
  public BigDecimal getArrivalMoney()
  {
    BigDecimal arrivalMoney = BigDecimalUtils.mul(getBigDecimal("arrivalAmount"), getBigDecimal("basePrice"));
    if (BigDecimalUtils.compare(arrivalMoney, BigDecimal.ZERO) == 0) {
      arrivalMoney = null;
    }
    return arrivalMoney;
  }
  
  public BigDecimal getForceMoney()
  {
    BigDecimal forceMoney = BigDecimalUtils.mul(getBigDecimal("forceAmount"), getBigDecimal("basePrice"));
    if (BigDecimalUtils.compare(forceMoney, BigDecimal.ZERO) == 0) {
      forceMoney = null;
    }
    return forceMoney;
  }
  
  public BigDecimal getUntreatedMoney()
  {
    BigDecimal untreatedMoney = BigDecimalUtils.mul(getBigDecimal("untreatedAmount"), getBigDecimal("basePrice"));
    if (BigDecimalUtils.compare(untreatedMoney, BigDecimal.ZERO) == 0) {
      untreatedMoney = null;
    }
    return untreatedMoney;
  }
  
  public String getSelectUnit()
  {
    Product product = (Product)get("product");
    String selectUnit = "";
    if (product == null) {
      return null;
    }
    Integer selectUnitId = getInt("selectUnitId");
    if (3 == selectUnitId.intValue()) {
      selectUnit = product.getStr("calculateUnit3");
    } else if (2 == selectUnitId.intValue()) {
      selectUnit = product.getStr("calculateUnit2");
    } else {
      selectUnit = product.getStr("calculateUnit1");
    }
    return selectUnit;
  }
  
  public BigDecimal getOtherAmount(Integer unitId)
  {
    Product product = (Product)get("product");
    return DwzUtils.getConversionAmount(getBigDecimal("amount"), getInt("selectUnitId"), 
      product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), unitId);
  }
  
  public BigDecimal getConUntreatedAmount()
  {
    Product product = (Product)get("product");
    return DwzUtils.getConversionAmount(getBigDecimal("untreatedAmount"), Integer.valueOf(1), 
      product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), getInt("selectUnitId"));
  }
  
  public String getHelpAmount(String baseAmountStr)
  {
    if (StringUtils.isBlank(baseAmountStr)) {
      baseAmountStr = "baseAmount";
    }
    Product product = (Product)get("product");
    BigDecimal baseAmount = getBigDecimal(baseAmountStr);
    String calculateUnit1 = product.getStr("calculateUnit1");
    String calculateUnit2 = product.getStr("calculateUnit2");
    String calculateUnit3 = product.getStr("calculateUnit3");
    BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
    BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
    return DwzUtils.helpAmount(baseAmount, calculateUnit1, calculateUnit2, calculateUnit3, unitRelation2, unitRelation3);
  }
  
  public List<Model> getList1(String configName, Integer billId, String productPrivs)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("product", Product.class);
    helper.appendSelect(" select detail.*,product.* ");
    helper.appendSql(" from cg_bought_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" where 1=1");
    if ((StringUtils.isNotBlank(productPrivs)) && (!"*".equals(productPrivs))) {
      helper.appendSql(" and (detail.productId in(" + productPrivs + ") or detail.productId is null)");
    } else if (StringUtils.isBlank(productPrivs)) {
      helper.appendSql(" and (detail.productId =0 or detail.productId is null)");
    }
    if (billId != null) {
      helper.appendSql(" and detail.billId=" + billId);
    }
    return dao.manyList(configName, helper);
  }
  
  public List<Model> getUntreatedList(String configName, Integer billId, String productPrivs)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("product", Product.class);
    helper.appendSelect(" select detail.*,product.* ");
    helper.appendSql("  from cg_bought_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" where 1=1");
    if ((StringUtils.isNotBlank("productPrivs")) && (!"*".equals(productPrivs))) {
      helper.appendSql(" and (bill.productId in(" + productPrivs + ") or bill.productId is null )");
    } else if (StringUtils.isBlank("productPrivs")) {
      helper.appendSql(" and (bill.productId =0 or bill.productId is null)");
    }
    if (billId != null) {
      helper.appendSql(" and billId=" + billId);
    }
    helper.appendSql(" and untreatedAmount >0");
    return dao.manyList(configName, helper);
  }
  
  public int delOthers(String configName, Integer billId, List<Integer> ids)
  {
    StringBuffer sql = new StringBuffer("delete from cg_bought_detail where 1=1");
    List<Object> paras = new ArrayList();
    if (billId != null)
    {
      sql.append(" and billId = ?");
      paras.add(billId);
    }
    else
    {
      sql.append(" and billId = ?");
      paras.add(Integer.valueOf(0));
    }
    if (ids != null) {
      for (Integer id : ids) {
        if (id != null)
        {
          sql.append(" and id != ?");
          paras.add(id);
        }
      }
    }
    return Db.use(configName).update(sql.toString(), paras.toArray());
  }
  
  public List<Model> getList(String configName, String ids)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("detail", BoughtDetail.class);
    helper.addAlias("product", Product.class);
    helper.appendSelect(" select detail.*,product.* ");
    helper.appendSql(" from cg_bought_detail as detail");
    helper.appendSql(" left join b_product as product on product.id = detail.productId where 1=1 ");
    if (StringUtils.isNotBlank(ids)) {
      helper.appendSql(" and detail.id in (" + ids + ")");
    }
    return dao.manyList(configName, helper);
  }
  
  public Record getBill(String configName, Integer detailId)
  {
    if (detailId == null) {
      return null;
    }
    String sql = "select bill.* from cg_bought_detail detail left join cg_bought_bill bill on detail.billId  =  bill.id where detail.id = " + detailId;
    return Db.use(configName).findFirst(sql);
  }
  
  public Map<String, Object> getStatPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws Exception
  {
    Map<String, Class<? extends Model>> alias = new HashMap();
    
    StringBuffer sel = new StringBuffer("select avg(b.basePrice) as basePricAvg,sum(b.baseAmount) as baseAmountSum ,sum(b.replenishAmount) as replenishAmountSum ,sum(b.arrivalAmount) as arrivalAmountSum , sum(b.forceAmount) as forceAmountSum , sum(b.untreatedAmount) as untreatedAmountSum , sum(b.bargainMoney) as bargainMoneySum,sum(b.replenishAmount)*avg(b.basePrice) as replenishMoneySum , sum(b.arrivalAmount)*avg(b.basePrice) as arrivalMoneySum , sum(b.forceAmount)*avg(b.basePrice) as forceMoneySum,sum(b.untreatedAmount)*avg(b.basePrice) as untreatedMoneySum,sum(b.giftAmount) as giftAmountSum");
    


    StringBuffer sql = new StringBuffer(" from cg_bought_detail as b");
    sql.append(" left join cg_bought_bill c on b.billId = c.id");
    sql.append(" left join b_product as product on b.productId = product.id");
    sql.append(" left join b_staff staff on c.staffId = staff.id");
    sql.append(" left join b_unit unit on c.unitId = unit.id");
    sql.append(" where 1=1");
    ComFunController.queryProductPrivs(sql, (String)map.get("productPrivs"), "b");
    ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "c");
    ComFunController.queryStaffPrivs(sql, (String)map.get("staffPrivs"), "c");
    ComFunController.queryUnitPrivs(sql, (String)map.get("unitPrivs"), "c");
    List<Object> paras = new ArrayList();
    if (map != null) {
      query(sql, paras, map);
    }
    if (map != null)
    {
      Integer way = Integer.valueOf(Integer.parseInt(map.get("way") == null ? "" : map.get("way").toString()));
      if (way.intValue() == 1)
      {
        alias.put("product", Product.class);
        sel.append(" , b.productId");
        sel.append(" ,product.*");
        sql.append(" GROUP by b.productId");
      }
      else if (way.intValue() == 2)
      {
        alias.put("unit", Unit.class);
        sel.append(" , c.unitId");
        sel.append(" ,unit.*");
        sql.append(" GROUP by c.unitId");
      }
      else if (way.intValue() == 3)
      {
        alias.put("staff", Staff.class);
        sel.append(" , c.staffId");
        sel.append(" ,staff.*");
        sql.append(" GROUP by c.staffId");
      }
    }
    rank(sql, map);
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, paras.toArray());
  }
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws Exception
  {
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("product", Product.class);
    alias.put("unit", Unit.class);
    alias.put("staff", Staff.class);
    alias.put("storage", Storage.class);
    String sel = "select b.*,c.unitId,c.staffId,c.storageId,c.deliveryDate,c.recodeDate,c.code,c.remark,c.memo,product.*,unit.*,staff.*,storage.*";
    StringBuffer sql = new StringBuffer(" from cg_bought_detail as b");
    sql.append(" left join cg_bought_bill c on b.billId = c.id");
    sql.append(" left join b_product as product on b.productId = product.id");
    sql.append(" left join b_staff staff on c.staffId = staff.id");
    sql.append(" left join b_unit unit on c.unitId = unit.id");
    sql.append(" left join b_storage storage on c.storageId = storage.id");
    sql.append(" where 1=1");
    
    ComFunController.queryProductPrivs(sql, (String)map.get("productPrivs"), "b");
    ComFunController.queryUnitPrivs(sql, (String)map.get("unitPrivs"), "c");
    ComFunController.queryStoragePrivs(sql, (String)map.get("storagePrivs"), "c");
    ComFunController.queryStaffPrivs(sql, (String)map.get("staffPrivs"), "c");
    
    List<Object> paras = new ArrayList();
    if (map != null) {
      query(sql, paras, map);
    }
    rank(sql, map);
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), alias, paras.toArray());
  }
  
  private void rank(StringBuffer sql, Map<String, Object> map)
  {
    String orderField = (String)map.get("orderField");
    String orderDirection = (String)map.get("orderDirection");
    if ((StringUtils.isBlank(orderField)) || (StringUtils.isBlank(orderDirection))) {
      return;
    }
    if ("productCode".equals(orderField)) {
      sql.append(" order by product.code " + orderDirection);
    } else if ("productName".equals(orderField)) {
      sql.append(" order by product.fullName " + orderDirection);
    } else if ("unitCode".equals(orderField)) {
      sql.append(" order by unit.code " + orderDirection);
    } else if ("unitName".equals(orderField)) {
      sql.append(" order by unit.fullName " + orderDirection);
    } else if ("staffCode".equals(orderField)) {
      sql.append(" order by staff.code " + orderDirection);
    } else if ("staffName".equals(orderField)) {
      sql.append(" order by staff.name " + orderDirection);
    } else if ("storageCode".equals(orderField)) {
      sql.append(" order by storage.code " + orderDirection);
    } else if ("storageName".equals(orderField)) {
      sql.append(" order by storage.fullName " + orderDirection);
    } else if ("updateTime".equals(orderField)) {
      sql.append(" order by b.updateTime " + orderDirection);
    } else if ("replenishMoney".equals(orderField)) {
      sql.append(" order by replenishAmount,basePrice " + orderDirection);
    } else if ("arrivalMoney".equals(orderField)) {
      sql.append(" order by arrivalAmount,basePrice " + orderDirection);
    } else if ("forceMoney".endsWith(orderField)) {
      sql.append(" order by forceAmount,basePrice " + orderDirection);
    } else if ("untreatedMoney".equals(orderField)) {
      sql.append(" order by untreatedAmount,basePrice " + orderDirection);
    } else if ("code".equals(orderField)) {
      sql.append(" order by c.code " + orderDirection);
    } else if ("memo".equals(orderField)) {
      sql.append(" order by c.memo " + orderDirection);
    } else {
      sql.append(" order by " + orderField + " " + orderDirection);
    }
  }
  
  private void query(StringBuffer sql, List<Object> paras, Map<String, Object> pars)
  {
    if (pars == null) {
      return;
    }
    if ((pars.get("startDate") != null) && (!"".equals(pars.get("startDate"))))
    {
      sql.append(" and  c.recodeDate >= ?");
      paras.add(pars.get("startDate"));
    }
    if ((pars.get("endDate") != null) && (!"".equals(pars.get("endDate"))))
    {
      sql.append(" and  c.recodeDate <= ?");
      paras.add(pars.get("endDate") + " 23:59:59");
    }
    if ((pars.get("productId") != null) && (!"".equals(pars.get("productId"))))
    {
      sql.append(" and  b.productId in (select id from b_product pr where pr.pids like ? or pr.id = ?)");
      paras.add("%{" + pars.get("productId") + "}%");
      paras.add(pars.get("productId"));
    }
    if ((pars.get("unitId") != null) && (!"".equals(pars.get("unitId"))))
    {
      sql.append(" and  c.unitId in(select id from b_unit unit where unit.pids like ? or unit.id = ? )");
      paras.add("%{" + pars.get("unitId") + "}%");
      paras.add(pars.get("unitId"));
    }
    if ((pars.get("staffId") != null) && (!"".equals(pars.get("staffId"))))
    {
      sql.append(" and  c.staffId in(select id from b_staff staff where staff.pids like ? or staff.id = ? )");
      paras.add("%{" + pars.get("staffId") + "}%");
      paras.add(pars.get("staffId"));
    }
    if ((pars.get("storageId") != null) && (!"".equals(pars.get("storageId"))))
    {
      sql.append(" and  c.storageId in(select id from b_storage storage where storage.pids like ? or storage.id = ? )");
      paras.add("%{" + pars.get("storageId") + "}%");
      paras.add(pars.get("storageId"));
    }
    if ("1".equals(pars.get("status"))) {
      sql.append(" and b.untreatedAmount >0");
    } else if ("2".equals(pars.get("status"))) {
      sql.append(" and (b.untreatedAmount <= 0 or b.untreatedAmount is null)");
    }
  }
}
