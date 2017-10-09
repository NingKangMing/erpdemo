package com.aioerp.model.stock;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.math.BigDecimal;
import java.util.List;

public class ParityAllotDetail
  extends BaseDbModel
{
  public static final ParityAllotDetail dao = new ParityAllotDetail();
  public static final String TABLE_NAME = "cc_parityallot_detail";
  
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
  
  public BigDecimal getOtherAmount(Integer unitId)
  {
    Product product = (Product)get("product");
    return DwzUtils.getConversionAmount(getBigDecimal("amount"), getInt("selectUnitId"), 
      product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), unitId);
  }
  
  public List<Model> getListByBillId(String configName, Integer billId)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("product", Product.class);
    helper.addAlias("inStorage", Storage.class);
    helper.addAlias("outStorage", Storage.class);
    helper.appendSelect(" select detail.*,product.*,inStorage.* ,outStorage.*");
    helper.appendSql(" from cc_parityallot_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" left join b_storage as inStorage on detail.inStorageId=inStorage.id");
    helper.appendSql(" left join b_storage as outStorage on detail.outStorageId=outStorage.id");
    helper.appendSql(" where 1=1");
    if (billId != null) {
      helper.appendSql(" and detail.billId=" + billId);
    }
    return dao.manyList(configName, helper);
  }
  
  public List<Model> getList(String configName, Integer billId)
  {
    StringBuffer sql = new StringBuffer("select * from cc_parityallot_detail where 1=1");
    if (billId != null) {
      sql.append(" and billId=" + billId);
    }
    return find(configName, sql.toString());
  }
}
