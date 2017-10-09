package com.aioerp.model.bought;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class PurchaseReturnDetail
  extends BaseDbModel
{
  public static final PurchaseReturnDetail dao = new PurchaseReturnDetail();
  public static final String TABLE_NAME = "cg_return_detail";
  
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
  
  public List<Model> getListByBillId(String configName, Integer billId, String productPrivs, String storagePrivs)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("product", Product.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select detail.*,product.*,storage.* ");
    helper.appendSql(" from cg_return_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" left join b_storage as storage on detail.storageId=storage.id");
    helper.appendSql(" where 1=1");
    if ((StringUtils.isNotBlank(productPrivs)) && (!"*".equals(productPrivs))) {
      helper.appendSql(" and (detail.productId in(" + productPrivs + ") or detail.productId is null )");
    } else if (StringUtils.isBlank(productPrivs)) {
      helper.appendSql(" and (detail.productId =0 or detail.productId is null)");
    }
    if ((StringUtils.isNotBlank(storagePrivs)) && (!"*".equals(storagePrivs))) {
      helper.appendSql(" and (detail.storageId in(" + storagePrivs + ") or detail.storageId is null )");
    } else if (StringUtils.isBlank(storagePrivs)) {
      helper.appendSql(" and (detail.storageId =0 or detail.storageId is null)");
    }
    if (billId != null) {
      helper.appendSql(" and detail.billId=" + billId);
    }
    return dao.manyList(configName, helper);
  }
  
  public List<Model> getListByBillId(String configName, Integer billId)
  {
    StringBuffer sql = new StringBuffer("select * from cg_return_detail where 1=1");
    if (billId != null) {
      sql.append(" and billId=" + billId);
    }
    return find(configName, sql.toString());
  }
}
