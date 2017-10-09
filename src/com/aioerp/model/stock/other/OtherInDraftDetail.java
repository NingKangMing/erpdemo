package com.aioerp.model.stock.other;

import com.aioerp.bean.FlowDetail;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.util.Date;

public class OtherInDraftDetail
  extends BaseDbModel
{
  public static final OtherInDraftDetail dao = new OtherInDraftDetail();
  public static final String TABLE_NAME = "cc_otherin_draft_detail";
  
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
  
  public void outPublic(Record detail, FlowDetail fDetail)
  {
    fDetail.setInStorageId(detail.getInt("storageId"));
    fDetail.setProductId(detail.getInt("productId"));
    fDetail.setSelectUnitId(detail.getInt("selectUnitId"));
    fDetail.setProduceDate(detail.getDate("produceDate"));
    fDetail.setBatch(detail.getStr("batch"));
    fDetail.setAmount(detail.getBigDecimal("amount"));
    fDetail.setBaseAmount(detail.getBigDecimal("baseAmount"));
    fDetail.setPrice(detail.getBigDecimal("price"));
    fDetail.setBasePrice(detail.getBigDecimal("basePrice"));
    fDetail.setMoney(detail.getBigDecimal("money"));
    fDetail.setRetailPrice(detail.getBigDecimal("retailPrice"));
    fDetail.setRetailMoney(detail.getBigDecimal("retailMoney"));
    fDetail.setMemo(detail.getStr("memo"));
    fDetail.setMessage(detail.getStr("message"));
  }
  
  public void inPublic(String configName, Record detail, FlowDetail fDetail, int userId)
  {
    FlowDetail.setComStorage(detail, fDetail, "storageId", null);
    detail.set("productId", fDetail.getProductId());
    detail.set("selectUnitId", fDetail.getSelectUnitId());
    detail.set("produceDate", fDetail.getProduceDate());
    detail.set("batch", fDetail.getBatch());
    detail.set("amount", fDetail.getAmount());
    detail.set("baseAmount", fDetail.getBaseAmount(configName));
    detail.set("price", fDetail.getPrice());
    detail.set("basePrice", fDetail.getBasePrice());
    detail.set("money", fDetail.getMoney());
    detail.set("retailPrice", fDetail.getRetailPrice());
    detail.set("retailMoney", fDetail.getRetailMoney());
    detail.set("memo", fDetail.getMemo());
    detail.set("message", fDetail.getMessage());
    detail.set("updateTime", new Date());
    detail.set("userId", Integer.valueOf(userId));
  }
}
