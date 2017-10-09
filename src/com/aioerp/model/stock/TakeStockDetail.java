package com.aioerp.model.stock;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TakeStockDetail
  extends BaseDbModel
{
  public static final TakeStockDetail dao = new TakeStockDetail();
  public static final String TABLE_NAME = "cc_takestock_detail";
  
  public String getTakeHelpAmount()
  {
    Product product = (Product)get("product");
    BigDecimal amount = getBigDecimal("takeStockAmount");
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
  
  public String getSckHelpAmount()
  {
    Product product = (Product)get("product");
    BigDecimal amount = getBigDecimal("stockAmount");
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
    helper.appendSelect(" select detail.*,product.* ");
    helper.appendSql(" from cc_takestock_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" where 1=1");
    helper.appendSql(" and detail.billId=" + billId);
    return dao.manyList(configName, helper);
  }
  
  public List<Integer> getListByDetailId(String configName, Integer billId)
  {
    String sql = "SELECT id FROM cc_takestock_detail WHERE billId = ?";
    List<Model> models = dao.find(configName, sql, new Object[] { billId });
    List<Integer> result = new ArrayList();
    for (Model model : models)
    {
      Integer id = model.getInt("id");
      result.add(id);
    }
    return result;
  }
}
