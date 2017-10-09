package com.aioerp.model.fz;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.fz.PriceDiscountTrackController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.ProductUnit;
import com.aioerp.model.base.Unit;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PriceDiscountTrack
  extends BaseDbModel
{
  public static final PriceDiscountTrack dao = new PriceDiscountTrack();
  public static final String TABLE_NAME = "fz_pricediscount_track";
  
  public static void addPriceDiscountTrack(String configName, String type, int unitId, int productId, Integer selectUnitId, BigDecimal price, BigDecimal discouont)
  {
    if (type == null) {
      return;
    }
    Date time = new Date();
    Boolean unitCostPrice = SysConfig.getConfig(configName, 4);
    Boolean unitSellPrice = SysConfig.getConfig(configName, 5);
    Boolean unitCostDiscount = SysConfig.getConfig(configName, 13);
    Boolean unitSellDiscount = SysConfig.getConfig(configName, 14);
    PriceDiscountTrack pd = (PriceDiscountTrack)dao.findFirst(configName, "select * from fz_pricediscount_track where 1=1 and unitId=? and productId=? and selectUnitId=?", new Object[] { Integer.valueOf(unitId), Integer.valueOf(productId), selectUnitId });
    if ((type.equals("purchase")) && ((unitCostPrice.booleanValue()) || (unitCostDiscount.booleanValue())))
    {
      if (pd != null)
      {
        if (unitCostPrice.booleanValue()) {
          pd.set("lastCostPrice", price);
        }
        if (unitCostDiscount.booleanValue()) {
          pd.set("lastCostDiscouont", discouont);
        }
        pd.set("lastCostDate", time);
        pd.update(configName);
      }
      else
      {
        pd = new PriceDiscountTrack();
        pd.set("unitId", Integer.valueOf(unitId));
        pd.set("productId", Integer.valueOf(productId));
        pd.set("selectUnitId", selectUnitId);
        pd.set("isMark", Integer.valueOf(PriceDiscountTrackController.removeMark));
        if (unitCostPrice.booleanValue()) {
          pd.set("lastCostPrice", price);
        }
        if (unitCostDiscount.booleanValue()) {
          pd.set("lastCostDiscouont", discouont);
        }
        pd.set("lastCostDate", time);
        pd.save(configName);
      }
    }
    else if ((type.equals("sell")) && ((unitSellPrice.booleanValue()) || (unitSellDiscount.booleanValue()))) {
      if (pd != null)
      {
        if (unitSellPrice.booleanValue()) {
          pd.set("lastSellPrice", price);
        }
        if (unitSellDiscount.booleanValue()) {
          pd.set("lastSellDiscouont", discouont);
        }
        pd.set("lastSellDate", time);
        pd.update(configName);
      }
      else
      {
        pd = new PriceDiscountTrack();
        pd.set("unitId", Integer.valueOf(unitId));
        pd.set("productId", Integer.valueOf(productId));
        pd.set("selectUnitId", selectUnitId);
        pd.set("isMark", Integer.valueOf(PriceDiscountTrackController.removeMark));
        if (unitSellPrice.booleanValue()) {
          pd.set("lastSellPrice", price);
        }
        if (unitSellDiscount.booleanValue()) {
          pd.set("lastSellDiscouont", discouont);
        }
        pd.set("lastSellDate", time);
        pd.save(configName);
      }
    }
  }
  
  public Map<String, Object> loadPriceDiscountPage(String configName, Map<String, Object> paraMap)
    throws SQLException, Exception
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String productId = (String)paraMap.get("productId");
    String unitId = (String)paraMap.get("unitId");
    String assistUnit = (String)paraMap.get("assistUnit");
    String hasMark = (String)paraMap.get("hasMark");
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    


    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("pd", PriceDiscountTrack.class);
    map.put("pro", Product.class);
    map.put("unit", Unit.class);
    


    String selectSql = "select pd.* , pro.* , unit.* ";
    StringBuilder sql = new StringBuilder(" from fz_pricediscount_track pd left join b_product pro on pd.productId = pro.id  left join b_unit unit on pd.unitId = unit.id where 1 = 1");
    if ((!unitId.equals("0")) && (!unitId.equals(""))) {
      sql.append(" and unit.pids like '%{" + unitId + "}%'");
    }
    if ((!productId.equals("0")) && (!productId.equals(""))) {
      sql.append(" and pro.pids like '%{" + productId + "}%'");
    }
    if (assistUnit.equals("base1")) {
      sql.append(" and  pd.selectUnitId =1");
    } else if (assistUnit.equals("base2")) {
      sql.append(" and  pd.selectUnitId =2");
    } else if (assistUnit.equals("base3")) {
      sql.append(" and  pd.selectUnitId =3");
    } else if (assistUnit.equals("base23")) {
      sql.append(" and  (pd.selectUnitId =2 or pd.selectUnitId =3)");
    }
    if (hasMark.equals("hasMark")) {
      sql.append(" and pd.isMark=1");
    } else if (hasMark.equals("noMark")) {
      sql.append(" and pd.isMark=0");
    }
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by tmp.id asc ");
    }
    return aioGetPageManyRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql, sql.toString(), map, new Object[0]);
  }
  
  public void ifNoExitsInsert(String configName, int newAddUnitId, int newAddProductId)
  {
    StringBuffer sb = new StringBuffer("select * from fz_pricediscount_track pd where 1=1 ");
    
    List<Model> unitList = Unit.dao.find(configName, "select * from b_unit u where u.status=" + AioConstants.STATUS_ENABLE + " and u.node=" + AioConstants.NODE_1 + " and u.pids like '%{" + newAddUnitId + "}%'");
    String unitIds = "";
    for (int i = 0; i < unitList.size(); i++) {
      unitIds = unitIds + ((Model)unitList.get(i)).getInt("id") + ",";
    }
    if (!unitIds.equals(""))
    {
      unitIds = unitIds.substring(0, unitIds.length() - 1);
      sb.append(" and pd.unitId in(" + unitIds + ")");
    }
    List<Model> productList = Product.dao.find(configName, "select * from b_product p where p.status=" + AioConstants.STATUS_ENABLE + " and p.node=" + AioConstants.NODE_1 + " and p.pids like '%{" + newAddProductId + "}%'");
    String productIds = "";
    for (int i = 0; i < productList.size(); i++) {
      productIds = productIds + ((Model)productList.get(i)).getInt("id") + ",";
    }
    if (!productIds.equals(""))
    {
      productIds = productIds.substring(0, productIds.length() - 1);
      sb.append(" and pd.productId in(" + productIds + ")");
    }
    List<Model> oldAllList = dao.find(configName, sb.toString());
    

    List<Record> newAddAllList = new ArrayList();
    Record r = null;
    for (int i = 0; i < unitList.size(); i++)
    {
      int unitId = ((Model)unitList.get(i)).getInt("id").intValue();
      for (int j = 0; j < productList.size(); j++)
      {
        Model product = (Model)productList.get(j);
        int productId = product.getInt("id").intValue();
        int unitRelation1 = product.getInt("unitRelation1").intValue();
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        if (unitRelation1 != 0)
        {
          r = new Record();
          r.set("unitId", Integer.valueOf(unitId));
          r.set("productId", Integer.valueOf(productId));
          r.set("selectUnitId", Integer.valueOf(1));
          r.set("lastCostDiscouont", Integer.valueOf(1));
          r.set("lastSellDiscouont", Integer.valueOf(1));
          

          r.set("isMark", Integer.valueOf(0));
          newAddAllList.add(r);
        }
        if (BigDecimalUtils.compare(unitRelation2, BigDecimal.ZERO) != 0)
        {
          r = new Record();
          r.set("unitId", Integer.valueOf(unitId));
          r.set("productId", Integer.valueOf(productId));
          r.set("selectUnitId", Integer.valueOf(2));
          r.set("lastCostDiscouont", Integer.valueOf(1));
          r.set("lastSellDiscouont", Integer.valueOf(1));
          

          r.set("isMark", Integer.valueOf(0));
          newAddAllList.add(r);
        }
        if (BigDecimalUtils.compare(unitRelation3, BigDecimal.ZERO) != 0)
        {
          r = new Record();
          r.set("unitId", Integer.valueOf(unitId));
          r.set("productId", Integer.valueOf(productId));
          r.set("selectUnitId", Integer.valueOf(3));
          r.set("lastCostDiscouont", Integer.valueOf(1));
          r.set("lastSellDiscouont", Integer.valueOf(1));
          

          r.set("isMark", Integer.valueOf(0));
          newAddAllList.add(r);
        }
      }
    }
    for (int i = 0; i < newAddAllList.size(); i++)
    {
      Record n = (Record)newAddAllList.get(i);
      for (int j = 0; j < oldAllList.size(); j++)
      {
        Model o = (Model)oldAllList.get(j);
        if ((n.getInt("unitId") == o.getInt("unitId")) && (n.getInt("productId") == o.getInt("productId")) && (n.getInt("selectUnitId") == o.getInt("selectUnitId")))
        {
          newAddAllList.remove(i);
          i--;
        }
      }
    }
    for (int i = 0; i < newAddAllList.size(); i++) {
      Db.use(configName).save("fz_pricediscount_track", (Record)newAddAllList.get(i));
    }
  }
  
  public void deleteByUnit(String configName, int id)
  {
    String sql = "delete from fz_pricediscount_track where unitId in (select id from b_unit u where u.status=" + AioConstants.STATUS_ENABLE + " and u.node=" + AioConstants.NODE_1 + " and u.pids like '%{" + id + "}%')";
    Db.use(configName).update(sql);
  }
  
  public void deleteByProduct(String configName, int id)
  {
    Db.use(configName).update("delete from fz_pricediscount_track where productId in (select id from b_product u where u.status=" + AioConstants.STATUS_ENABLE + " and u.node=" + AioConstants.NODE_1 + " and u.pids like '%{" + id + "}%')");
  }
  
  public List<Model> formulaSetData(String configName, String unitId, String productId, String assistUnit, String hasMark)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("pd", PriceDiscountTrack.class);
    helper.addAlias("pro", Product.class);
    helper.addAlias("unit", Unit.class);
    helper.addAlias("punit", ProductUnit.class);
    helper.appendSelect("select pd.* , pro.* , unit.*,punit.*");
    helper.appendSql(" from fz_pricediscount_track pd");
    helper.appendSql(" left join b_product pro on pd.productId = pro.id");
    helper.appendSql(" left join b_unit unit on pd.unitId = unit.id");
    helper.appendSql(" left join b_product_unit punit on pd.productId = punit.productId");
    helper.appendSql(" where pd.selectUnitId=punit.selectUnitId ");
    if ((!unitId.equals("0")) && (!unitId.equals(""))) {
      helper.appendSql(" and pd.unitId like '%{" + unitId + "}%'");
    }
    if ((!productId.equals("0")) && (!productId.equals(""))) {
      helper.appendSql(" and pd.productId like '%{" + productId + "}%'");
    }
    if (assistUnit.equals("base1")) {
      helper.appendSql(" and  pd.selectUnitId =1");
    } else if (assistUnit.equals("base2")) {
      helper.appendSql(" and  pd.selectUnitId =2");
    } else if (assistUnit.equals("base3")) {
      helper.appendSql(" and  pd.selectUnitId =3");
    } else if (assistUnit.equals("base23")) {
      helper.appendSql(" and  (pd.selectUnitId =2 or pd.selectUnitId =3)");
    }
    if (hasMark.equals("hasMark")) {
      helper.appendSql(" and pd.isMark=1");
    } else if (hasMark.equals("noMark")) {
      helper.appendSql(" and pd.isMark=0");
    }
    return dao.manyList(configName, helper);
  }
  
  public PriceDiscountTrack getRecentlyObj(String configName, int productId, int selectUnitId)
  {
    String sql = "select * from fz_pricediscount_track where productId =" + productId + " and selectUnitId =" + selectUnitId + " order by lastCostDate desc";
    
    return (PriceDiscountTrack)dao.findFirst(configName, sql);
  }
  
  public PriceDiscountTrack getPriceTrackObj(String configName, int productId, int selectUnitId, int billType)
  {
    String orderByAttr = "lastCostDate";
    if (billType == AioConstants.BILL_TYPE_JH) {
      orderByAttr = "lastCostDate";
    } else if (billType == AioConstants.BILL_TYPE_XS) {
      orderByAttr = "lastSellDate";
    }
    String sql = "select * from fz_pricediscount_track where productId =" + productId + " and selectUnitId =" + selectUnitId + " order by " + orderByAttr + " desc";
    return (PriceDiscountTrack)dao.findFirst(configName, sql);
  }
  
  public BigDecimal whichFormula(String configName, int item, PriceDiscountTrack pd, Product pro, ProductUnit punit)
  {
    if (item == PriceDiscountTrackController.itme0) {
      return null;
    }
    if (item == PriceDiscountTrackController.itme1) {
      return punit.getBigDecimal("retailPrice");
    }
    if (item == PriceDiscountTrackController.itme2) {
      return pd.getBigDecimal("lastCostPrice");
    }
    if (item == PriceDiscountTrackController.itme3) {
      return Stock.dao.getProAvgPriceByProIds(configName, pro.getInt("id"));
    }
    if (item == PriceDiscountTrackController.itme4) {
      return punit.getBigDecimal("defaultPrice1");
    }
    if (item == PriceDiscountTrackController.itme5) {
      return punit.getBigDecimal("defaultPrice2");
    }
    if (item == PriceDiscountTrackController.itme6) {
      return punit.getBigDecimal("defaultPrice3");
    }
    if (item == PriceDiscountTrackController.itme7) {
      return pd.getBigDecimal("lastCostDiscouont");
    }
    if (item == PriceDiscountTrackController.itme8) {
      return pd.getBigDecimal("lastSellDiscouont");
    }
    if (item == PriceDiscountTrackController.itme9) {
      return new BigDecimal("1");
    }
    if (item == PriceDiscountTrackController.itme10) {
      return pd.getBigDecimal("lastSellPrice");
    }
    return null;
  }
}
