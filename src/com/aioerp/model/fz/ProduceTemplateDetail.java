package com.aioerp.model.fz;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.util.ArrayList;
import java.util.List;

public class ProduceTemplateDetail
  extends BaseDbModel
{
  public static final ProduceTemplateDetail dao = new ProduceTemplateDetail();
  public static final String TABLE_NAME = "fz_produce_template_detail";
  
  public List<Model> getDetailsByTmpId(String configName, Integer id)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("product", Product.class);
    helper.appendSelect(" select detail.*,product.* ");
    helper.appendSql(" from fz_produce_template_detail as detail");
    helper.appendSql(" left join b_product as product on detail.productId=product.id");
    helper.appendSql(" where 1=1");
    helper.appendSql(" and detail.tmpId=" + id);
    return dao.manyList(configName, helper);
  }
  
  public List<Integer> getListByDetailId(String configName, Integer billId)
  {
    String sql = "SELECT id FROM fz_produce_template_detail WHERE tmpId = ?";
    List<Model> models = dao.find(configName, sql, new Object[] { billId });
    List<Integer> result = new ArrayList();
    for (Model model : models)
    {
      Integer id = model.getInt("id");
      result.add(id);
    }
    return result;
  }
  
  public Integer deleteDetails(String configName, Integer tmpId)
  {
    String sql = "delete from fz_produce_template_detail where tmpId = ?";
    return Integer.valueOf(Db.use(configName).update(sql, new Object[] { tmpId }));
  }
}
