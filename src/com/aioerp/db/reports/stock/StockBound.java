package com.aioerp.db.reports.stock;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class StockBound
  extends BaseDbModel
{
  public static final StockBound dao = new StockBound();
  Map<String, Class<? extends Model>> MAP = new HashMap();
  private StringBuffer SELECTSQL;
  private StringBuffer FROMSQL;
  
  public void boundInit(Integer sgeId)
  {
    this.MAP.put("pro", Product.class);
    
    this.SELECTSQL = new StringBuffer("select pro.*, ");
    
    String maxs = "(SELECT SUM(sb.max) FROM b_product AS p LEFT JOIN cc_stock_bound sb ON sb.productId = p.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND sb.storageId = " + sgeId + ")";
    
    String mins = "(SELECT SUM(sb.min) FROM b_product AS p LEFT JOIN cc_stock_bound sb ON sb.productId = p.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND sb.storageId = " + sgeId + ")";
    
    this.SELECTSQL.append(maxs + " as maxs,");
    this.SELECTSQL.append(mins + " as mins ");
    
    this.FROMSQL = new StringBuffer(" FROM b_product pro ");
    this.FROMSQL.append(" where 1 = 1");
  }
  
  public Map<String, Object> getPageBySupId(String configName, String proPrivs, int pageNum, int numPerPage, int supId, Integer sgeId, String listId, String orderField, String orderDirection)
    throws Exception
  {
    boundInit(sgeId);
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    sb.append(" and pro.supId = ? ");
    paras.add(Integer.valueOf(supId));
    if ((StringUtils.isNotBlank(proPrivs)) && (!"*".equals(proPrivs))) {
      sb.append(" and pro.id in (" + proPrivs + ") ");
    } else if (StringUtils.isBlank(proPrivs)) {
      sb.append(" and pro.id = 0 ");
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, this.SELECTSQL.toString(), sb.toString(), this.MAP, paras.toArray());
  }
  
  public Map<String, Object> getSupPageBySupIdAndStorageId(String configName, String proPrivs, int pageNum, int numPerPage, int supId, Integer sgeId, int ObjectId, String listID, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    boundInit(sgeId);
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    
    sb.append(" and pro.supId = ? ");
    paras.add(Integer.valueOf(supId));
    if ((StringUtils.isNotBlank(proPrivs)) && (!"*".equals(proPrivs))) {
      sb.append(" and pro.id in (" + proPrivs + ") ");
    } else if (StringUtils.isBlank(proPrivs)) {
      sb.append(" and pro.id = 0 ");
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyByUpObject(configName, "pro", "id", listID, ObjectId, pageNum, numPerPage, this.SELECTSQL.toString(), sb.toString(), null, this.MAP, paras.toArray());
  }
  
  public Map<String, Object> getPageByParam(String configName, String proPrivs, String listId, Integer pageNum, Integer numPerPage, String orderField, String orderDirection, Integer sgeId, String searchPar, String searchVal)
    throws Exception
  {
    boundInit(sgeId);
    StringBuffer sb = new StringBuffer("from b_product pro where 1 = 1");
    List<Object> paras = new ArrayList();
    if ((!"".equals(searchPar)) && (searchPar != null))
    {
      if (searchPar.equals("all"))
      {
        sb.append(" and (pro.code LIKE ?");
        paras.add("%" + searchVal + "%");
        sb.append(" or pro.smallName LIKE ?");
        paras.add("%" + searchVal + "%");
        sb.append(" or pro.fullName LIKE ?");
        paras.add("%" + searchVal + "%");
        sb.append(" or pro.spell LIKE ?");
        paras.add("%" + searchVal + "%");
        sb.append(" or pro.standard LIKE ?");
        paras.add("%" + searchVal + "%");
        sb.append(" or pro.model LIKE ?");
        paras.add("%" + searchVal + "%");
        sb.append(" or pro.barCode1 LIKE ?");
        paras.add("%" + searchVal + "%");
        sb.append(" or pro.field LIKE ? )");
        paras.add("%" + searchVal + "%");
      }
      else
      {
        sb.append(" and pro." + searchPar + " LIKE ?");
        paras.add("%" + searchVal + "%");
      }
      sb.append(" and pro.node = 1");
      if ((StringUtils.isNotBlank(proPrivs)) && (!"*".equals(proPrivs))) {
        sb.append(" and pro.id in (" + proPrivs + ") ");
      } else if (StringUtils.isBlank(proPrivs)) {
        sb.append(" and pro.id = 0 ");
      }
      sb.append(" order by " + orderField + " " + orderDirection);
    }
    return aioGetPageManyRecord(configName, listId, pageNum.intValue(), numPerPage.intValue(), this.SELECTSQL.toString(), sb.toString(), this.MAP, paras.toArray());
  }
  
  public Model getModelByParam(String configName, Integer sgeId, Integer proId)
  {
    String sql = "SELECT * FROM cc_stock_bound WHERE storageId = ? AND productId = ?";
    return dao.findFirst(configName, sql, new Object[] { sgeId, proId });
  }
  
  public Map<String, Object> getMaxPage(String configName, String proPrivs, String listId, Integer sgeId, String sgePids, int pageNum, int numPerPage, String orderField, String orderDirection, String searchBaseAttr, String searchBaseVal)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("pro", Product.class);
    StringBuffer select = new StringBuffer();
    StringBuffer from = new StringBuffer();
    select.append("SELECT (sb.max*1) maxNum,");
    select.append("(SELECT SUM(zj.amount) FROM zj_product_avgprice zj WHERE zj.productId=pro.id AND zj.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%')) ) sckAmount,");
    select.append("ABS((sb.max*1)-(SELECT SUM(zj.amount) FROM zj_product_avgprice zj WHERE zj.productId=pro.id AND zj.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%')) )) adjust,");
    select.append("pro.* ");
    
    from.append("FROM b_product pro RIGHT JOIN cc_stock_bound sb ON sb.productId = pro.id WHERE 1=1 ");
    if ((StringUtils.isNotBlank(proPrivs)) && (!"*".equals(proPrivs))) {
      from.append(" and pro.id in (" + proPrivs + ") ");
    } else if (StringUtils.isBlank(proPrivs)) {
      from.append(" and pro.id = 0 ");
    }
    if ((StringUtils.isNotEmpty(searchBaseAttr)) && (StringUtils.isNotEmpty(searchBaseVal))) {
      if (searchBaseAttr.equals("barCode")) {
        from.append(" and (pro.barCode1 like '%" + searchBaseVal + "%' or pro.barCode2 like '%" + searchBaseVal + "%' or pro.barCode3 like '%" + searchBaseVal + "%')");
      } else {
        from.append(" and pro." + searchBaseAttr + " like '%" + searchBaseVal + "%'");
      }
    }
    from.append("AND sb.storageId = " + sgeId + "  AND pro.node = 1  AND sb.max > 0 ");
    from.append("AND sb.max < (SELECT SUM(zj.amount) FROM zj_product_avgprice zj WHERE zj.productId=pro.id AND zj.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%')))");
    from.append(" order by " + orderField + " " + orderDirection);
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, select.toString(), from.toString(), map, new Object[0]);
  }
  
  public Map<String, Object> getMinPage(String configName, String proPrivs, String listId, Integer sgeId, String sgePids, int pageNum, int numPerPage, String orderField, String orderDirection, String searchBaseAttr, String searchBaseVal)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("pro", Product.class);
    StringBuffer select = new StringBuffer();
    StringBuffer from = new StringBuffer();
    select.append("SELECT (sb.min*1) minNum,");
    select.append("(SELECT SUM(zj.amount) FROM zj_product_avgprice zj WHERE zj.productId=pro.id AND zj.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%')) ) sckAmount,");
    select.append("ABS((sb.min*1)-(SELECT SUM(zj.amount) FROM zj_product_avgprice zj WHERE zj.productId=pro.id AND zj.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%')) )) adjust,");
    select.append("pro.* ");
    
    from.append("FROM b_product pro RIGHT JOIN cc_stock_bound sb ON sb.productId = pro.id WHERE 1=1 ");
    if ((StringUtils.isNotBlank(proPrivs)) && (!"*".equals(proPrivs))) {
      from.append(" and pro.id in (" + proPrivs + ") ");
    } else if (StringUtils.isBlank(proPrivs)) {
      from.append(" and pro.id = 0 ");
    }
    if ((StringUtils.isNotEmpty(searchBaseAttr)) && (StringUtils.isNotEmpty(searchBaseVal))) {
      if (searchBaseAttr.equals("barCode")) {
        from.append(" and (pro.barCode1 like '%" + searchBaseVal + "%' or pro.barCode2 like '%" + searchBaseVal + "%' or pro.barCode3 like '%" + searchBaseVal + "%')");
      } else {
        from.append(" and pro." + searchBaseAttr + " like '%" + searchBaseVal + "%'");
      }
    }
    from.append("AND sb.storageId = " + sgeId + "  AND pro.node = 1 AND sb.min > 0 ");
    from.append("AND sb.min > (SELECT SUM(zj.amount) FROM zj_product_avgprice zj WHERE zj.productId=pro.id AND zj.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%')))");
    from.append(" order by " + orderField + " " + orderDirection);
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, select.toString(), from.toString(), map, new Object[0]);
  }
}
