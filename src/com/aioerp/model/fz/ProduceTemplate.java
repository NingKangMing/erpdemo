package com.aioerp.model.fz;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ProduceTemplate
  extends BaseDbModel
{
  public static final ProduceTemplate dao = new ProduceTemplate();
  public static final String TABLE_NAME = "fz_produce_template";
  private Product product;
  
  public Product getProduct(String configName)
  {
    if (this.product == null) {
      this.product = ((Product)Product.dao.findById(configName, get("productId")));
    }
    return this.product;
  }
  
  public boolean nameIsExist(String configName, String tmpName, int id)
  {
    return 
      Db.use(configName).queryLong("select count(*) from fz_produce_template where tmpName =? and id != ?", new Object[] { tmpName, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public Map<String, Object> getListByParam(String configName, String proPrivs, String listID, int pageNum, int numPerPage, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> MAP = new HashMap();
    MAP.put("tmp", ProduceTemplate.class);
    MAP.put("pro", Product.class);
    String selectSql = "select tmp.* , pro.* ";
    StringBuffer fromSql = new StringBuffer(" from fz_produce_template tmp left join b_product pro on tmp.productId = pro.id");
    fromSql.append(" where 1 = 1");
    if ((StringUtils.isNotBlank(proPrivs)) && (!"*".equals(proPrivs))) {
      fromSql.append(" and pro.id in (" + proPrivs + ") ");
    } else if (StringUtils.isBlank(proPrivs)) {
      fromSql.append(" and pro.id = 0 ");
    }
    fromSql.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, selectSql, fromSql.toString(), MAP, new Object[0]);
  }
  
  public Map<String, Object> getLastPage(String configName, String listID, int pageNum, int numPerPage, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> MAP = new HashMap();
    MAP.put("tmp", ProduceTemplate.class);
    MAP.put("pro", Product.class);
    String selectSql = "select tmp.* , pro.* ";
    String fromSql = " from fz_produce_template tmp left join b_product pro on tmp.productId = pro.id where 1 = 1 order by " + orderField + " " + orderDirection;
    
    return aioGetLastPageManyRecord(configName, listID, pageNum, numPerPage, selectSql, fromSql, "tmp.id", MAP, new Object[0]);
  }
  
  public Map<String, Object> getPageDialogByTerms(String configName, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
    throws Exception
  {
    Map<String, Class<? extends Model>> MAP = new HashMap();
    MAP.put("tmp", ProduceTemplate.class);
    MAP.put("pro", Product.class);
    
    String selectSql = "select tmp.* , pro.* ";
    StringBuilder sql = new StringBuilder(" from fz_produce_template tmp left join b_product pro on tmp.productId = pro.id where 1 = 1");
    List<Object> paras = new ArrayList();
    if ((pars != null) && 
      (pars.get("termVal") != null) && (!"".equals(pars.get("termVal"))))
    {
      String term = pars.get("term").toString();
      Object val = pars.get("termVal");
      if ("quick".equals(term))
      {
        sql.append(" and ( tmp.tmpName like ?");
        paras.add("%" + val + "%");
        sql.append(" or pro.code like ?");
        paras.add("%" + val + "%");
        sql.append(" or pro.fullName like ? )");
        paras.add("%" + val + "%");
      }
      else
      {
        sql.append(" and " + term + " like ?");
        paras.add("%" + val + "%");
      }
    }
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by tmp.id asc ");
    }
    return aioGetPageManyRecord(configName, "", pageNum, numPerPage, selectSql, sql.toString(), MAP, paras.toArray());
  }
}
