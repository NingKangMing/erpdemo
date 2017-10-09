package com.aioerp.model.bought;

import com.aioerp.bean.FlowDetail;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class PurchaseDraftDetail
  extends BaseDbModel
{
  public static final PurchaseDraftDetail dao = new PurchaseDraftDetail();
  public static final String TABLE_NAME = "cg_purchase_draft_detail";
  
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
    String sql = "select bill.* from cg_purchase_draft_detail detail left join cg_purchase_draft_bill bill on detail.billId  =  bill.id where detail.billId = " + detailId;
    return Db.use(configName).findFirst(sql);
  }
  
  public List<Model> getUntreatedList(String configName, Integer billId)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("product", Product.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select detail.*,product.*,storage.* ");
    helper.appendSql("  from cg_purchase_draft_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" left join b_storage as storage on detail.storageId=storage.id");
    helper.appendSql(" where 1=1");
    if (billId != null) {
      helper.appendSql(" and detail.billId=" + billId);
    }
    helper.appendSql(" and detail.untreatedAmount>0");
    return dao.manyList(configName, helper);
  }
  
  public List<Model> getListByBillId(String configName, Integer billId)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("product", Product.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select detail.*,product.*,storage.* ");
    helper.appendSql(" from cg_purchase_draft_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" left join b_storage as storage on detail.storageId=storage.id");
    helper.appendSql(" where 1=1");
    if (billId != null) {
      helper.appendSql(" and detail.billId=" + billId);
    }
    return dao.manyList(configName, helper);
  }
  
  public List<Model> getList(String configName, String ids)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("detail", PurchaseDraftDetail.class);
    helper.addAlias("product", Product.class);
    helper.appendSelect(" select detail.*,product.* ");
    helper.appendSql(" from cg_purchase_draft_detail as detail");
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
      Long size = Db.use(configName).queryLong("select count(id) from cg_purchase_draft_detail where detailId=?", new Object[] { boughtDetailId });
      if (size.longValue() > 0L) {
        return true;
      }
    }
    return false;
  }
  
  public int delOthers(String configName, Integer billId, List<Integer> ids)
  {
    StringBuffer sql = new StringBuffer("delete from cg_purchase_draft_detail where 1=1");
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
  
  public int delByBill(String configName, Integer billId)
  {
    StringBuffer sql = new StringBuffer("delete from cg_purchase_draft_detail where 1=1");
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
    return Db.use(configName).update(sql.toString(), paras.toArray());
  }
  
  public void outPublic(Record detail, FlowDetail fDetail)
  {
    fDetail.setDiscount(detail.getBigDecimal("discount"));
    fDetail.setDiscountPrice(detail.getBigDecimal("discountPrice"));
    fDetail.setDiscountMoney(detail.getBigDecimal("discountMoney"));
    fDetail.setTaxRate(detail.getBigDecimal("taxRate"));
    fDetail.setTaxPrice(detail.getBigDecimal("taxPrice"));
    fDetail.setTaxes(detail.getBigDecimal("taxes"));
    fDetail.setTaxMoney(detail.getBigDecimal("taxMoney"));
    fDetail.setInStorageId(detail.getInt("storageId"));
    fDetail.setRetailPrice(detail.getBigDecimal("retailPrice"));
    fDetail.setRetailMoney(detail.getBigDecimal("retailMoney"));
    fDetail.setCostPrice(detail.getBigDecimal("costPrice"));
    fDetail.setGiftAmount(detail.getBigDecimal("giftAmount"));
  }
  
  public void inPublic(String configName, Record detail, FlowDetail fDetail, int userId)
  {
    detail.set("productId", fDetail.getProductId());
    detail.set("selectUnitId", fDetail.getSelectUnitId());
    detail.set("amount", fDetail.getAmount());
    detail.set("price", fDetail.getPrice());
    detail.set("batch", fDetail.getBatch());
    detail.set("produceDate", fDetail.getProduceDate());
    detail.set("money", fDetail.getMoney());
    detail.set("baseAmount", fDetail.getBaseAmount(configName));
    detail.set("basePrice", fDetail.getBasePrice());
    detail.set("memo", fDetail.getMemo());
    detail.set("status", fDetail.getStatus());
    detail.set("updateTime", new Date());
    detail.set("userId", Integer.valueOf(userId));
    detail.set("discount", fDetail.getDiscount());
    detail.set("discountPrice", fDetail.getDiscountPrice());
    detail.set("discountMoney", fDetail.getDiscountMoney());
    detail.set("taxRate", fDetail.getTaxRate());
    detail.set("taxPrice", fDetail.getTaxPrice());
    detail.set("taxes", fDetail.getTaxes());
    detail.set("taxMoney", fDetail.getTaxMoney());
    if (fDetail.getIsInSck().intValue() == -1) {
      detail.set("storageId", fDetail.getOutStorageId());
    } else {
      detail.set("storageId", fDetail.getInStorageId());
    }
    detail.set("retailPrice", fDetail.getRetailPrice());
    detail.set("retailMoney", fDetail.getRetailMoney());
    detail.set("costPrice", fDetail.getCostPrice());
    detail.set("giftAmount", fDetail.getGiftAmount(configName));
  }
}
