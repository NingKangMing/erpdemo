package com.aioerp.model.base;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class Product
  extends BaseDbModel
{
  public static final String TABLE_NAME = "b_product";
  public static String COMMON_SQL = "from b_product where status != " + AioConstants.STATUS_DELETE;
  public static String COMMON_DIALOG_SQL = "from b_product where status = " + AioConstants.STATUS_ENABLE;
  public static final Product dao = new Product();
  public List<Object> paras;
  
  public List<Model> getAllSorts(String configName, String productPids)
  {
    return dao.find(configName, "select * from b_product where node=2 and pids like ? order by rank ", new Object[] { productPids + "%" });
  }
  
  public Product findObjById(String configName, Integer id)
  {
    if (id == null) {
      return null;
    }
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("product", Product.class);
    return (Product)dao.findFirst(configName, "select * from b_product as product where product.id=" + id, map);
  }
  
  public Model getObjectAttrById(String configName, String formSql, int id)
  {
    List<Model> list = find(configName, formSql + COMMON_DIALOG_SQL + " and id=" + id);
    if ((list != null) && (list.size() > 0)) {
      return (Model)list.get(0);
    }
    return null;
  }
  
  public Boolean add(String configName, Product model)
  {
    return Boolean.valueOf(model.save(configName));
  }
  
  public Boolean deleteById(String configName, Product model, int id)
  {
    return Boolean.valueOf(model.deleteById(configName, Integer.valueOf(id)));
  }
  
  public Boolean update(String configName, Product model)
  {
    return Boolean.valueOf(model.update(configName));
  }
  
  public String getChildIdsBySupId(String configName, String privs, int supId)
  {
    String str = "";
    StringBuffer sql = new StringBuffer("select id from b_product where pids like '%{" + supId + "}%' and status=" + AioConstants.STATUS_ENABLE);
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    List<Model> modelList = dao.find(configName, sql.toString());
    if ((modelList != null) && (modelList.size() != 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public String getAllChildIdsBySupId(String configName, String privs, int supId)
  {
    String str = "";
    StringBuffer sql = new StringBuffer("select id from b_product where pids like '%{" + supId + "}%'");
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    List<Model> modelList = dao.find(configName, sql.toString());
    if ((modelList != null) && (modelList.size() != 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public String getAllChildIdsNoSupId(String configName, String privs, int supId)
  {
    String str = "";
    StringBuffer sql = new StringBuffer("select id from b_product where pids not like '%{" + supId + "}%'");
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    List<Model> modelList = dao.find(configName, sql.toString());
    if ((modelList != null) && (modelList.size() != 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public List<Integer> getChildIds(String configName, String privs, int supId)
  {
    StringBuffer sql = new StringBuffer("select id from b_product where pids like '%{" + supId + "}%' and status=" + AioConstants.STATUS_ENABLE);
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    return Db.use(configName).query(sql.toString());
  }
  
  public List<Integer> getLastChilIds(String configName, String privs, String pids)
  {
    StringBuffer sql = new StringBuffer("select id from b_product where pids LIKE CONCAT('" + pids + "','%')  AND node=1  and status=" + AioConstants.STATUS_ENABLE);
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    return Db.use(configName).query(sql.toString());
  }
  
  public int getPsupIdBySupId(String configName, int supId)
  {
    return Db.use(configName).queryInt("select supId " + COMMON_SQL + " and id=" + supId).intValue();
  }
  
  public boolean hasChildBySupId(String configName, String privs, int supId)
  {
    StringBuffer sql = new StringBuffer("select count(*) " + COMMON_SQL + " and supId=?");
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    return Db.use(configName).queryLong(sql.toString(), new Object[] { Integer.valueOf(supId) }).longValue() > 0L;
  }
  
  public List<Model> getTreeEnableList(String configName, String privs)
  {
    StringBuffer sql = new StringBuffer("select * " + COMMON_DIALOG_SQL + " and node=?");
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    sql.append(" order by rank asc");
    return dao.find(configName, sql.toString(), new Object[] { Integer.valueOf(AioConstants.NODE_2) });
  }
  
  public List<Model> getTreeEnableList(String configName, String privs, int supId)
  {
    StringBuffer sql = new StringBuffer("select * " + COMMON_DIALOG_SQL + " and node=? and supId=?");
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    sql.append(" order by rank asc");
    return dao.find(configName, sql.toString(), new Object[] { Integer.valueOf(AioConstants.NODE_2), Integer.valueOf(supId) });
  }
  
  public List<Model> getTreeAllList(String configName, String privs)
  {
    StringBuffer sql = new StringBuffer("select * " + COMMON_SQL + " and node=? ");
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    sql.append(" order by rank asc");
    return dao.find(configName, sql.toString(), new Object[] { Integer.valueOf(AioConstants.NODE_2) });
  }
  
  public boolean existObjectByNum(String configName, int id, String code)
  {
    Model model = findFirst(configName, "select count(id) as num " + COMMON_SQL + " and id<>? and code=?", new Object[] { Integer.valueOf(id), code });
    Long count = model.getLong("num");
    if (count.compareTo(new Long(0L)) > 0) {
      return true;
    }
    return false;
  }
  
  public boolean existOneObjectByAttr(String configName, int id, String attr, String val, int node)
  {
    Model model = null;
    if (attr.equals("code")) {
      model = findFirst(configName, "select count(id) as num " + COMMON_DIALOG_SQL + " and id<>" + id + " and (code like '%" + val + "%' or fullName like '%" + val + "%' or smallName like '%" + val + "%' or spell like '%" + val + "%' or standard like '%" + val + "%' or model like '%" + val + "%' or field like '%" + val + "%' or barCode1 like '%" + val + "%' or barCode2 like '%" + val + "%' or barCode3 like '%" + val + "%') and node=" + node);
    } else if (attr.equals("fullName")) {
      model = findFirst(configName, "select count(id) as num " + COMMON_DIALOG_SQL + " and id<>" + id + " and fullName like '%" + val + "%' and node=" + node);
    }
    int count = 0;
    if (model != null) {
      count = model.getLong("num").intValue();
    }
    if (count == 1) {
      return true;
    }
    return false;
  }
  
  public boolean verify(String configName, String attr, Integer id)
  {
    if (id == null) {
      return true;
    }
    StringBuffer sql = new StringBuffer("select " + attr + " from cc_stock_records where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from xs_sellbook_detail where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from cg_bought_detail where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from cc_stock_init where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from cc_takestock_detail where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from fz_produce_template where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from fz_produce_template_detail where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from cw_pay_type where " + attr + " = " + id);
    
    sql.append(" limit 1");
    id = Db.use(configName).queryInt(sql.toString());
    return (id != null) && (id.intValue() > 0);
  }
  
  public boolean existBarCode(String configName, String barCode, Integer id)
  {
    return Db.use(configName).queryLong("SELECT count(code) FROM b_product WHERE (barCode1 = ? OR barCode2 = ? OR barCode3 = ? )  and id <> ? ", new Object[] { barCode, barCode, barCode, id }).longValue() > 0L;
  }
  
  public Map<String, Object> getPageByCondtion(String configName, String privs, String listId, int supId, Integer node, String searchPar1, String searchValue1, int cPage, int pageSize, String orderField, String orderDirection)
  {
    return aioGetPageRecord(configName, this, listId, cPage, pageSize, "select *", commSql(privs, supId, node, searchPar1, searchValue1, orderField, orderDirection).toString(), new Object[0]);
  }
  
  public Map<String, Object> getPageByRoot(String configName, String privs, int pageIndex, int pageSize, int supId, String listId)
  {
    StringBuffer sql = new StringBuffer(COMMON_SQL);
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    sql.append(" and supId=? order by rank asc");
    return aioGetPageRecord(configName, this, listId, pageIndex, pageSize, "select *", sql.toString(), new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getPageSelectUpObject(String configName, String privs, String listId, int supId, int upObjectId, int pageIndex, int pageSize, String searchPar1, String searchValue1, String orderField, String orderDirection)
  {
    return aioGetPageByUpObject(configName, this, listId, upObjectId, pageIndex, pageSize, "select *", commSql(privs, supId, null, searchPar1, searchValue1, orderField, orderDirection).toString(), null, new Object[0]);
  }
  
  public Map<String, Object> getLastPageByCondition(String configName, String privs, String listId, int supId, String searchPar1, String searchValue1, int pageIndex, int pageSize, String orderField, String orderDirection)
  {
    return aioGetLastPageRecord(configName, this, listId, pageIndex, pageSize, "select *", commSql(privs, supId, null, searchPar1, searchValue1, orderField, orderDirection).toString(), new Object[0]);
  }
  
  public StringBuffer commSql(String privs, int supId, Integer node, String searchPar1, String searchValue1, String orderField, String orderDirection)
  {
    StringBuffer sb = new StringBuffer(COMMON_SQL);
    if ((searchPar1 != null) && (!searchPar1.equals("")))
    {
      if ((searchValue1 != null) && (!searchValue1.equals("")))
      {
        if (searchPar1.equals("all"))
        {
          sb.append(" and (fullName like '%" + searchValue1 + "%'");
          sb.append(" or code like '%" + searchValue1 + "%'");
          sb.append(" or smallName like '%" + searchValue1 + "%'");
          sb.append(" or spell like '%" + searchValue1 + "%'");
          sb.append(" or barCode1 like '%" + searchValue1 + "%'");
          sb.append(" or barCode2 like '%" + searchValue1 + "%'");
          sb.append(" or barCode3 like '%" + searchValue1 + "%'");
          sb.append(searchByStatus("fast", searchValue1, null));
          sb.append(" or memo like '%" + searchValue1 + "%')");
        }
        else if (searchPar1.equals("status"))
        {
          sb.append(searchByStatus("condition", searchValue1, null));
        }
        else if (searchPar1.equals("barCode"))
        {
          sb.append(" and (barCode1 like '%" + searchValue1 + "%' or barCode1 like '%" + searchValue1 + "%' or barCode1 like '%" + searchValue1 + "%')");
        }
        else
        {
          sb.append(" and " + searchPar1 + " like '%" + searchValue1 + "%'");
        }
        sb.append(" and pids like '%{" + supId + "}%'");
      }
      else
      {
        sb.append(" and supId=" + supId);
      }
    }
    else if ((node != null) && (node.intValue() == 1)) {
      sb.append(" and pids like '%{" + supId + "}%'");
    } else {
      sb.append(" and supId=" + supId);
    }
    if ((StringUtils.isNotBlank(privs)) && (!"*".equals(privs))) {
      sb.append(" and id in(" + privs + ")");
    } else if (StringUtils.isBlank(privs)) {
      sb.append(" and id =0");
    }
    if ((node != null) && (node.intValue() == 1)) {
      sb.append(" and node=" + node);
    }
    if ((orderField != null) && (!orderField.equals("")) && (orderDirection != null) && (!orderDirection.equals(""))) {
      sb.append(" order by " + orderField + " " + orderDirection);
    } else {
      sb.append(" order by rank asc");
    }
    return sb;
  }
  
  public Map<String, Object> search(String configName, String privs, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
  {
    StringBuffer sql = new StringBuffer(" from b_product where status=" + AioConstants.STATUS_ENABLE);
    if ((pars.get("termVal") == null) || (pars.get("termVal").equals(""))) {
      sql.append(" and supId=0");
    } else {
      sql.append(" and node=" + AioConstants.NODE_1);
    }
    sql = ComFunController.basePrivsSql(sql, privs, null);
    StringBuffer appendSql = appendSql(orderField, orderDirection, pars);
    sql.append(appendSql);
    return aioGetPageRecord(configName, dao, "", pageNum, numPerPage, "select *", sql.toString(), this.paras.toArray());
  }
  
  public Map<String, Object> supIdSearch(String configName, String privs, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
  {
    StringBuffer sql = null;
    if ((pars.get("termVal") != null) && (!pars.get("termVal").equals(""))) {
      sql = new StringBuffer(" from b_product where status=" + AioConstants.STATUS_ENABLE + " and  node=" + AioConstants.NODE_1);
    } else {
      sql = new StringBuffer(" from b_product where status=" + AioConstants.STATUS_ENABLE + " and supId=" + supId);
    }
    sql = ComFunController.basePrivsSql(sql, privs, null);
    StringBuffer appendSql = appendSql(orderField, orderDirection, pars);
    sql.append(appendSql);
    return aioGetPageRecord(configName, dao, "", pageNum, numPerPage, "select *", sql.toString(), this.paras.toArray());
  }
  
  public Map<String, Object> getPageBySupId(String configName, String privs, int pageIndex, int pageSize, int supId, String listId)
  {
    StringBuffer sql = new StringBuffer(COMMON_SQL + " and  supId=? ");
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    sql.append(" order by rank asc");
    return aioGetPageRecord(configName, this, listId, pageIndex, pageSize, "select *", sql.toString(), new Object[] { Integer.valueOf(supId) });
  }
  
  public int getPsupId(String configName, int supId)
  {
    if (supId <= 0) {
      return 0;
    }
    StringBuffer sql = new StringBuffer("select supId " + COMMON_SQL + " and id=?");
    return Db.use(configName).queryInt(sql.toString(), new Object[] { Integer.valueOf(supId) }).intValue();
  }
  
  public StringBuffer appendSql(String orderField, String orderDirection, Map<String, Object> pars)
  {
    StringBuffer sql = new StringBuffer("");
    this.paras = new ArrayList();
    String term = "";
    if ((pars != null) && (pars.size() > 0)) {
      term = (String)pars.get("term");
    }
    if ((pars != null) && (term != null) && (!term.equals(""))) {
      if (term.equals("quick"))
      {
        sql.append(" and( code like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or fullName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or spell like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or standard like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or model like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or field like ? )");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("code"))
      {
        sql.append(" and code like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("fullName"))
      {
        sql.append(" and fullName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("spell"))
      {
        sql.append(" and spell like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("standard"))
      {
        sql.append(" and standard like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("model"))
      {
        sql.append(" and model like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("field"))
      {
        sql.append(" and field like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
    }
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by rank asc");
    }
    return sql;
  }
  
  public List<Model> getProducts(String configName, String privs, Integer supId, String searchPar, String searchVal)
  {
    StringBuffer sb = new StringBuffer("SELECT * FROM b_product WHERE 1=1");
    if ((searchPar != null) && (!searchVal.equals(""))) {
      if (searchPar.equals("all"))
      {
        sb.append(" and (fullName like '%" + searchVal + "%'");
        sb.append(" or code like '%" + searchVal + "%'");
        sb.append(" or smallName like '%" + searchVal + "%'");
        sb.append(" or spell like '%" + searchVal + "%'");
        sb.append(searchByStatus("fast", searchVal, null));
        sb.append(" or memo like '%" + searchVal + "%')");
      }
      else if (searchPar.equals("status"))
      {
        sb.append(searchByStatus("condition", searchVal, null));
      }
      else
      {
        sb.append(" and " + searchPar + " like '%" + searchVal + "%'");
      }
    }
    if (supId != null) {
      sb.append(" and pids like '%{" + supId + "}%' ");
    }
    sb.append(" and node = 1 ");
    
    sb = ComFunController.basePrivsSql(sb, privs, null);
    return dao.find(configName, sb.toString());
  }
  
  public List<Model> getListUnit(String configName, String[] ids, Map<String, Object> map)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("unit", ProductUnit.class);
    helper.appendSelect(" select unit.*,product.*,track.lastCostPrice,minsell.minSellPrice,avgprice.amount/unit.unitRelation as amount,avgprice.avgprice*unit.unitRelation as avgprice");
    helper.appendSql("  from b_product_unit as unit");
    helper.appendSql(" left join b_product as product on unit.productId=product.id");
    helper.appendSql(" left join (select * from fz_pricediscount_track where lastCostDate in (select max(lastCostDate) from fz_pricediscount_track group by productId,selectUnitId))as track on track.productId=unit.productId and unit.selectUnitId = track.selectUnitId");
    helper.appendSql(" left join (select * from fz_minsell_price where minSellPrice in (select min(minSellPrice) from fz_minsell_price group by productId,selectUnitId))as minsell on minsell.productId=unit.productId and unit.selectUnitId = minsell.selectUnitId");
    helper.appendSql(" left join  zj_product_avgprice avgprice on unit.productId = avgprice.productId and avgprice.storageId is null");
    helper.appendSql(" where 1=1 and product.node=1");
    if ((ids != null) && (ids.length > 0))
    {
      String idStr = "";
      helper.appendSql(" and (");
      for (int i = 0; i < ids.length; i++)
      {
        if (i == 0)
        {
          helper.appendSql(" (product.pids like '%{" + ids[i] + "}%'");
        }
        else
        {
          helper.appendSql(" or product.pids like '%{" + ids[i] + "}%'");
          idStr = idStr + ",";
        }
        idStr = idStr + ids[i];
      }
      helper.appendSql(" )");
      helper.appendSql(" or product.id in(" + idStr + ")");
      helper.appendSql(" )");
    }
    helper.appendSql(" and unit.unitRelation is not null");
    query(helper, map);
    rank(helper, map);
    return dao.manyList(configName, helper);
  }
  
  public Map<String, Object> getPageUnit(String configName, String listID, int pageNum, int numPerPage, String[] ids, Map<String, Object> map)
    throws SQLException, Exception
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("unit", ProductUnit.class);
    String sel = "select unit.*,product.*,track.lastCostPrice,minsell.minSellPrice,(price.amount/unit.unitRelation) as amount,(price.avgprice*unit.unitRelation) as avgprice";
    
    helper.appendSql(" from b_product_unit as unit");
    helper.appendSql(" left join b_product as product on unit.productId=product.id");
    helper.appendSql(" left join (select * from fz_pricediscount_track where lastCostDate in (select max(lastCostDate) from fz_pricediscount_track group by productId,selectUnitId))as track on track.productId=unit.productId and unit.selectUnitId = track.selectUnitId");
    helper.appendSql(" left join (select * from fz_minsell_price where minSellPrice in (select min(minSellPrice) from fz_minsell_price group by productId,selectUnitId))as minsell on minsell.productId=unit.productId and unit.selectUnitId = minsell.selectUnitId");
    helper.appendSql(" left join  (");
    helper.appendSql(" select zj.productId,sum(zj.amount) as amount,ROUND(SUM(costMoneys) / SUM(amount),4) as avgprice from zj_product_avgprice as zj where zj.storageId is not null  group by zj.productId");
    helper.appendSql(" )as price on unit.productId = price.productId ");
    helper.appendSql(" where 1=1 and product.node=1");
    if ((ids != null) && (ids.length > 0))
    {
      String idStr = "";
      helper.appendSql(" and (");
      for (int i = 0; i < ids.length; i++)
      {
        if (i == 0)
        {
          helper.appendSql(" (product.pids like '%{" + ids[i] + "}%'");
        }
        else
        {
          helper.appendSql(" or product.pids like '%{" + ids[i] + "}%'");
          idStr = idStr + ",";
        }
        idStr = idStr + ids[i];
      }
      helper.appendSql(" )");
      helper.appendSql(" or product.id in(" + idStr + ")");
      helper.appendSql(" )");
    }
    helper.appendSql(" and unit.unitRelation is not null");
    query(helper, map);
    rank(helper, map);
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel, helper.getSql().toString(), helper.getAlias(), new Object[0]);
  }
  
  private void query(SqlHelper helper, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    if (map.get("typeUnit") != null)
    {
      if ("base1".equals(map.get("typeUnit"))) {
        helper.appendSql(" and unit.selectUnitId = 1");
      }
      if ("base2".equals(map.get("typeUnit"))) {
        helper.appendSql(" and unit.selectUnitId = 2");
      }
      if ("base3".equals(map.get("typeUnit"))) {
        helper.appendSql(" and unit.selectUnitId = 3");
      }
      if ("base23".equals(map.get("typeUnit"))) {
        helper.appendSql(" and (unit.selectUnitId = 2 or unit.selectUnitId = 3)");
      }
    }
  }
  
  private void rank(SqlHelper helper, Map<String, Object> map)
  {
    if (map == null) {
      return;
    }
    String orderField = (String)map.get("orderField");
    String orderDirection = (String)map.get("orderDirection");
    if (StringUtils.isBlank(orderField)) {
      return;
    }
    if ("code".equals(orderField)) {
      helper.appendSql(" order by product.code " + orderDirection);
    } else if ("fullName".equals(orderField)) {
      helper.appendSql(" order by product.fullName " + orderDirection);
    } else if ("calculateUnit".equals(orderField)) {
      helper.appendSql(" order by unit.calculateUnit " + orderDirection);
    } else if ("barCode".equals(orderField)) {
      helper.appendSql(" order by unit.barCode " + orderDirection);
    } else if ("retailPrice".equals(orderField)) {
      helper.appendSql(" order by unit.retailPrice " + orderDirection);
    } else if ("defaultPrice1".equals(orderField)) {
      helper.appendSql(" order by unit.defaultPrice1 " + orderDirection);
    } else if ("defaultPrice2".equals(orderField)) {
      helper.appendSql(" order by unit.defaultPrice2 " + orderDirection);
    } else if ("defaultPrice3".equals(orderField)) {
      helper.appendSql(" order by unit.defaultPrice3 " + orderDirection);
    } else if ("lastCostPrice".equals(orderField)) {
      helper.appendSql(" order by track.lastCostPrice " + orderDirection);
    } else if ("minSellPrice".equals(orderField)) {
      helper.appendSql(" order by minsell.minSellPrice " + orderDirection);
    } else if ("avgprice".equals(orderField)) {
      helper.appendSql(" order by avgprice " + orderDirection);
    } else if ("amount".equals(orderField)) {
      helper.appendSql(" order by amount " + orderDirection);
    } else if ("grossMargin".equals(orderField)) {
      helper.appendSql(" order by unit.defaultPrice1 " + orderDirection + " ,avgprice " + orderDirection);
    } else {
      helper.appendSql(" order by " + orderField + " " + orderDirection);
    }
  }
  
  public Model getRecordByCode(String configName, Object code)
  {
    Model model = null;
    if (code != null)
    {
      StringBuffer sql = new StringBuffer("SELECT * FROM b_product WHERE code=?");
      model = dao.findFirst(configName, sql.toString(), new Object[] { code });
    }
    return model;
  }
  
  public int getMaxRank(String configName)
  {
    int maxRank = 1;
    StringBuffer sql = new StringBuffer("SELECT MAX(rank) maxRank FROM b_product");
    Model model = dao.findFirst(configName, sql.toString());
    if (model.get("maxRank") != null) {
      maxRank = model.getInt("maxRank").intValue() + 1;
    }
    return maxRank;
  }
  
  public List<Model> getListByPids(String configName, String privs, String pids)
  {
    StringBuffer sql = new StringBuffer("SELECT * FROM  b_product WHERE pids LIKE CONCAT('" + pids + "','%')");
    
    sql = ComFunController.basePrivsSql(sql, privs, null);
    return find(configName, sql.toString());
  }
}
