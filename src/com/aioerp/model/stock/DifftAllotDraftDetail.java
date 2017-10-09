package com.aioerp.model.stock;

import com.aioerp.bean.FlowDetail;
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

public class DifftAllotDraftDetail
  extends BaseDbModel
{
  public static final DifftAllotDraftDetail dao = new DifftAllotDraftDetail();
  public static final String TABLE_NAME = "cc_difftallot_draft_detail";
  
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
    helper.appendSql(" from cc_difftallot_draft_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" left join b_storage as inStorage on detail.inStorageId=inStorage.id");
    helper.appendSql(" left join b_storage as outStorage on detail.outStorageId=outStorage.id");
    helper.appendSql(" where 1=1");
    if (billId != null) {
      helper.appendSql(" and detail.billId=" + billId);
    }
    return dao.manyList(configName, helper);
  }
  
  public List<Integer> getListByDetailId(String configName, Integer billId)
  {
    String sql = "SELECT id FROM cc_difftallot_draft_detail WHERE billId = ?";
    List<Model> models = dao.find(configName, sql, new Object[] { billId });
    List<Integer> result = new ArrayList();
    for (Model model : models)
    {
      Integer id = model.getInt("id");
      result.add(id);
    }
    return result;
  }
  
  public Integer delDeail(String configName, Integer billId)
  {
    String sql = "delete from cc_difftallot_draft_detail where billId = ? ";
    return Integer.valueOf(Db.use(configName).update(sql, new Object[] { billId }));
  }
  
  public void outPublic(Record detail, FlowDetail fDetail)
  {
    fDetail.setOutStorageId(detail.getInt("outStorageId"));
    fDetail.setInStorageId(detail.getInt("inStorageId"));
    fDetail.setDiscount(detail.getBigDecimal("discount"));
    fDetail.setDiscountPrice(detail.getBigDecimal("discountPrice"));
    fDetail.setDiscountMoney(detail.getBigDecimal("discountMoney"));
    fDetail.setRetailPrice(detail.getBigDecimal("retailPrice"));
    fDetail.setRetailMoney(detail.getBigDecimal("retailMoney"));
    fDetail.setMessage(detail.getStr("message"));
    fDetail.setGiftAmount(detail.getBigDecimal("giftAmount"));
    fDetail.setCostPrice(detail.getBigDecimal("costPrice"));
  }
  
  public void inPublic(String configName, Record detail, FlowDetail fDetail, int userId)
  {
    detail.set("productId", fDetail.getProductId());
    detail.set("outStorageId", fDetail.getProductId());
    detail.set("inStorageId", fDetail.getProductId());
    detail.set("selectUnitId", fDetail.getSelectUnitId());
    detail.set("produceDate", fDetail.getProduceDate());
    detail.set("price", fDetail.getPrice());
    detail.set("money", fDetail.getMoney());
    detail.set("discount", fDetail.getDiscount());
    detail.set("discountPrice", fDetail.getDiscountPrice());
    detail.set("discountMoney", fDetail.getDiscountMoney());
    detail.set("retailPrice", fDetail.getRetailPrice());
    detail.set("retailMoney", fDetail.getRetailMoney());
    detail.set("amount", fDetail.getAmount());
    detail.set("batch", fDetail.getBatch());
    detail.set("baseAmount", fDetail.getBaseAmount(configName));
    detail.set("basePrice", fDetail.getBasePrice());
    detail.set("memo", fDetail.getMemo());
    detail.set("message", fDetail.getMessage());
    detail.set("updateTime", new Date());
    detail.set("giftAmount", fDetail.getGiftAmount(configName));
    detail.set("costPrice", fDetail.getCostPrice());
    detail.set("userId", Integer.valueOf(userId));
  }
}
