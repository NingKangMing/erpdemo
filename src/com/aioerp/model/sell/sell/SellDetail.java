package com.aioerp.model.sell.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.sell.sellbook.SellbookDetail;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class SellDetail
  extends BaseDbModel
{
  public static final SellDetail dao = new SellDetail();
  public static final String TABLE_NAME = "xs_sell_detail";
  
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
  
  public String getHelpAmount()
  {
    Product product = (Product)get("product");
    BigDecimal amount = getBigDecimal("amount");
    String calculateUnit1 = product.getStr("calculateUnit1");
    String calculateUnit2 = product.getStr("calculateUnit2");
    String calculateUnit3 = product.getStr("calculateUnit3");
    Integer unitRelation1 = product.getInt("unitRelation1");
    BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
    BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
    BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, getInt("selectUnitId"), 
      unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
    return DwzUtils.helpAmount(baseAmount, calculateUnit1, calculateUnit2, calculateUnit3, unitRelation2, unitRelation3);
  }
  
  public List<Model> getList(String configName, Integer billId, String productPrivs, String storagePrivs)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("detail", SellbookDetail.class);
    helper.addAlias("product", Product.class);
    helper.appendSelect(" select detail.*,product.* ");
    helper.appendSql(" from xs_sell_detail as detail");
    helper.appendSql(" left join b_product as product on product.id = detail.productId where 1=1 ");
    helper.appendSql(" and detail.billId=" + billId);
    helper.appendSql(" and detail.relStatus = " + AioConstants.STATUS_DISABLE);
    StringBuffer sbSql = new StringBuffer("");
    
    ComFunController.queryUnitPrivs(sbSql, productPrivs, "product", "id");
    ComFunController.queryUnitPrivs(sbSql, storagePrivs, "detail");
    helper.appendSql(sbSql.toString());
    return dao.manyList(configName, helper);
  }
  
  public List<Model> getList1(String configName, Integer billId)
  {
    StringBuffer sql = new StringBuffer("select * from xs_sell_detail where 1=1");
    if (billId != null) {
      sql.append(" and billId=" + billId);
    }
    return dao.find(configName, sql.toString());
  }
  
  public List<Model> getList2(String configName, Integer billId, String tableName)
  {
    SqlHelper helper = SqlHelper.getHelper();
    
    helper.addAlias("product", Product.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select detail.*,product.*,storage.* ");
    helper.appendSql(" from " + tableName + " as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" left join b_storage as storage on detail.storageId=storage.id");
    helper.appendSql(" where 1=1");
    helper.appendSql(" and detail.billId=" + billId);
    return dao.manyList(configName, helper);
  }
  
  public List<Model> getList(String configName, String ids)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("detail", SellbookDetail.class);
    helper.addAlias("product", Product.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select detail.*,product.*,storage.* ");
    helper.appendSql(" from xs_sell_detail as detail");
    helper.appendSql(" left join b_product as product on product.id = detail.productId ");
    helper.appendSql(" left join b_storage as storage on storage.id = detail.storageId where 1=1 ");
    if (StringUtils.isNotBlank(ids)) {
      helper.appendSql(" and detail.id in (" + ids + ")");
    }
    return dao.manyList(configName, helper);
  }
  
  public List<Model> getUntreatedList(String configName, Integer billId)
  {
    StringBuffer sql = new StringBuffer("select * from xs_sell_detail where 1=1");
    if (billId != null) {
      sql.append(" and billId=" + billId);
    }
    sql.append(" and untreatedAmount >0");
    return dao.find(configName, sql.toString());
  }
  
  public boolean sellbookHasRel(String configName, int sellBookBillId)
  {
    Record r = Db.use(configName).findFirst("select selldetail.id objId from xs_sellbook_detail sellbookdetail left join xs_sell_detail selldetail on selldetail.detailId=sellbookdetail.id where sellbookdetail.billId=" + sellBookBillId);
    if ((r != null) && (r.get("objId") != null)) {
      return true;
    }
    return false;
  }
}
