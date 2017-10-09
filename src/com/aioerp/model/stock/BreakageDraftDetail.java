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

public class BreakageDraftDetail
  extends BaseDbModel
{
  public static final BreakageDraftDetail dao = new BreakageDraftDetail();
  public static final String TABLE_NAME = "cc_breakage_draft_detail";
  
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
  
  public List<Model> getList(String configName, Integer billId)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("product", Product.class);
    helper.addAlias("storage", Storage.class);
    helper.appendSelect(" select detail.*,product.*,storage.* ");
    helper.appendSql(" from cc_breakage_draft_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" left join b_storage as storage on detail.storageId=storage.id");
    helper.appendSql(" where 1=1");
    helper.appendSql(" and detail.billId=" + billId);
    return dao.manyList(configName, helper);
  }
  
  public List<Integer> getListByDetailId(String configName, Integer billId)
  {
    String sql = "SELECT id FROM cc_breakage_draft_detail WHERE billId = ?";
    List<Model> models = dao.find(configName, sql, new Object[] { billId });
    List<Integer> result = new ArrayList();
    for (Model model : models)
    {
      Integer id = model.getInt("id");
      result.add(id);
    }
    return result;
  }
  
  public List<Model> getDetailsByBillId(String configName, Integer billId)
  {
    String sql = "SELECT * FROM cc_breakage_draft_detail WHERE billId = ? ";
    return dao.find(configName, sql, new Object[] { billId });
  }
  
  public Integer delDeail(String configName, Integer billId)
  {
    String sql = "delete from cc_breakage_draft_detail where billId = ? ";
    return Integer.valueOf(Db.use(configName).update(sql, new Object[] { billId }));
  }
  
  public void inPublic(String configName, Record detail, FlowDetail fDetail, int userId)
  {
    detail.set("productId", fDetail.getProductId());
    Integer storageId = null;
    if (fDetail.getIsInSck().intValue() == 1) {
      storageId = fDetail.getInStorageId();
    } else {
      storageId = fDetail.getOutStorageId();
    }
    detail.set("storageId", storageId);
    detail.set("selectUnitId", fDetail.getSelectUnitId());
    detail.set("amount", fDetail.getAmount());
    detail.set("price", fDetail.getPrice());
    detail.set("batch", fDetail.getBatch());
    detail.set("produceDate", fDetail.getProduceDate());
    detail.set("money", fDetail.getMoney());
    detail.set("baseAmount", fDetail.getBaseAmount(configName));
    detail.set("basePrice", fDetail.getCostPrice());
    detail.set("memo", fDetail.getMemo());
    detail.set("status", fDetail.getStatus());
    detail.set("updateTime", new Date());
    detail.set("userId", Integer.valueOf(userId));
  }
}
