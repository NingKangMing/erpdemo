package com.aioerp.model.finance;

import com.aioerp.bean.FlowDetail;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.util.BigDecimalUtils;
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

public class AdjustCostDraftDetail
  extends BaseDbModel
{
  public static final AdjustCostDraftDetail dao = new AdjustCostDraftDetail();
  public static final String TABLE_NAME = "cc_adjust_cost_draft_detail";
  
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
    
    helper.appendSelect(" select detail.*,product.* ");
    helper.appendSql(" from cc_adjust_cost_draft_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    
    helper.appendSql(" where 1=1");
    if (billId != null) {
      helper.appendSql(" and detail.billId=" + billId);
    }
    return dao.manyList(configName, helper);
  }
  
  public int delOthers(String configName, Integer billId, List<Integer> ids)
  {
    StringBuffer sql = new StringBuffer("delete from cc_adjust_cost_draft_detail where 1=1");
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
    StringBuffer sql = new StringBuffer("delete from cc_adjust_cost_draft_detail where 1=1");
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
    fDetail.setRetailPrice(detail.getBigDecimal("retailPrice"));
    fDetail.setRetailMoney(detail.getBigDecimal("retailMoney"));
    fDetail.setCostPrice(detail.getBigDecimal("costPrice"));
    fDetail.setGiftAmount(detail.getBigDecimal("giftAmount"));
    fDetail.setLastPrice(detail.getBigDecimal("lastPrice"));
    fDetail.setAdjustMoney(detail.getBigDecimal("adjustMoney"));
    fDetail.setLastMoney(detail.getBigDecimal("lastMoney"));
  }
  
  public void inPublic(String configName, Record detail, FlowDetail fDetail, int userId)
  {
    detail.set("productId", fDetail.getProductId());
    detail.set("selectUnitId", Integer.valueOf(1));
    detail.set("amount", fDetail.getBaseAmount(configName));
    detail.set("price", BigDecimalUtils.div(fDetail.getMoney(), fDetail.getBaseAmount(configName)));
    detail.set("batch", fDetail.getBatch());
    detail.set("produceDate", fDetail.getProduceDate());
    detail.set("money", fDetail.getMoney());
    detail.set("baseAmount", fDetail.getBaseAmount(configName));
    detail.set("basePrice", fDetail.getBasePrice());
    detail.set("memo", fDetail.getMemo());
    detail.set("status", fDetail.getStatus());
    detail.set("updateTime", new Date());
    detail.set("userId", Integer.valueOf(userId));
    detail.set("retailPrice", fDetail.getRetailPrice());
    detail.set("retailMoney", fDetail.getRetailMoney());
    detail.set("costPrice", fDetail.getCostPrice());
    detail.set("lastPrice", fDetail.getLastPrice(configName));
    detail.set("adjustMoney", fDetail.getAdjustMoney());
    detail.set("lastMoney", fDetail.getLastMoney());
  }
}
