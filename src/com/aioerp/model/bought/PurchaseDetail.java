package com.aioerp.model.bought;

import com.aioerp.controller.BaseController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class PurchaseDetail
  extends BaseDbModel
{
  public static final PurchaseDetail dao = new PurchaseDetail();
  public static final String TABLE_NAME = "cg_purchase_detail";
  
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
  
  public Record getBill(String configName, Integer detailId)
  {
    if (detailId == null) {
      return null;
    }
    String sql = "select bill.* from cg_purchase_detail detail left join cg_purchase_bill bill on detail.billId  =  bill.id where detail.billId = " + detailId;
    return Db.use(configName).findFirst(sql);
  }
  
  public List<Model> getUntreatedList(String configName, Integer billId)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("product", Product.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select detail.*,product.*,storage.* ");
    helper.appendSql("  from cg_purchase_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" left join b_storage as storage on detail.storageId=storage.id");
    helper.appendSql(" where 1=1");
    if (billId != null) {
      helper.appendSql(" and detail.billId=" + billId);
    }
    helper.appendSql(" and detail.untreatedAmount>0");
    return dao.manyList(configName, helper);
  }
  
  public List<Model> getListByBillId(String configName, Integer billId, String productPrivs, String storagePrivs)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("product", Product.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select detail.*,product.*,storage.* ");
    helper.appendSql(" from cg_purchase_detail as detail");
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
    StringBuffer sql = new StringBuffer("select * from cg_purchase_detail where 1=1");
    if (billId != null) {
      sql.append(" and billId=" + billId);
    }
    return find(configName, sql.toString());
  }
  
  public List<Model> getList(String configName, String ids)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("detail", PurchaseDetail.class);
    helper.addAlias("product", Product.class);
    helper.appendSelect(" select detail.*,product.* ");
    helper.appendSql(" from cg_purchase_detail as detail");
    helper.appendSql(" left join b_product as product on product.id = detail.productId where 1=1 ");
    if (StringUtils.isNotBlank(ids)) {
      helper.appendSql(" and detail.id in (" + ids + ")");
    }
    return dao.manyList(configName, helper);
  }
  
  public boolean isHaveBought(String configName, int boughtId)
  {
    List<Model> boughtList = BoughtDetail.dao.getList1(configName, Integer.valueOf(boughtId), BaseController.basePrivs(BaseController.PRODUCT_PRIVS));
    for (Model bought : boughtList)
    {
      Integer boughtDetailId = bought.getInt("id");
      Long size = Db.use(configName).queryLong("select count(id) from cg_purchase_detail where detailId=?", new Object[] { boughtDetailId });
      if (size.longValue() > 0L) {
        return true;
      }
    }
    return false;
  }
}
