package com.aioerp.model.base;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class Storage
  extends BaseDbModel
{
  public static final Storage dao = new Storage();
  public static final String TABLE_NAME = "b_storage";
  public static String COMMON_DIALOG_SQL = "from b_storage where status = " + AioConstants.STATUS_ENABLE;
  public ArrayList<Object> paras;
  
  public boolean existObjectByIdCodeNode(String configName, int id, String attrType, String val, int node)
  {
    Model model = null;
    if (attrType.equals("code")) {
      model = findFirst(configName, "select count(id) as num " + COMMON_DIALOG_SQL + " and id<>? and code=? and node=?", new Object[] { Integer.valueOf(id), val, Integer.valueOf(node) });
    } else if (attrType.equals("fullName")) {
      model = findFirst(configName, "select count(id) as num " + COMMON_DIALOG_SQL + " and id<>? and fullName=? and node=?", new Object[] { Integer.valueOf(id), val, Integer.valueOf(node) });
    }
    Long count = Long.valueOf(0L);
    if (model != null) {
      count = model.getLong("num");
    }
    if (count.compareTo(new Long(0L)) > 0) {
      return true;
    }
    return false;
  }
  
  public Map<String, Object> getPageBySupId(String configName, String listID, int pageNum, int numPerPage, int supId, String orderField, String orderDirection, String storagePrivs)
  {
    String sql = "from b_storage where supId=?";
    if ((listID == null) || ("".equals(listID))) {
      sql = sql + " and status = 2 ";
    }
    sql = queryPrivs(sql, storagePrivs);
    sql = sql + " order by " + orderField + " " + orderDirection;
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[] { Integer.valueOf(supId) });
  }
  
  public Boolean add(String configName, Storage storage)
  {
    storage.set("createTime", new Date());
    storage.set("status", Integer.valueOf(2));
    return Boolean.valueOf(storage.save(configName));
  }
  
  public List<Model> getAllSorts(String configName, String storagePrivs)
  {
    String sql = "select * from b_storage where node=2";
    sql = queryPrivs(sql, storagePrivs);
    sql = sql + " order by rank";
    return dao.find(configName, sql);
  }
  
  public List<Model> getAllSorts(String configName, String storagePids, String storagePrivs)
  {
    String sql = "select * from b_storage where node=2 and pids like ?";
    sql = queryPrivs(sql, storagePrivs);
    sql = sql + " order by rank ";
    return dao.find(sql, storagePids + "%");
  }
  
  public Page<Model> getPageSortsBySupId(String configName, int pageNumber, int pageSize, int supId, String storagePrivs)
  {
    String sql = "from b_storage where node>1 and supId=? ";
    sql = queryPrivs(sql, storagePrivs);
    sql = sql + " order by rank ";
    
    return dao.paginate(configName, pageNumber, pageSize, "select *", sql, null, new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getPageStorageBySupId(String configName, String listID, int pageNumber, int pageSize, int supId, String orderField, String orderDirection, String storagePrivs)
  {
    String sql = "from b_storage where node=1 and pids like ?";
    sql = queryPrivs(sql, storagePrivs);
    sql = sql + " order by " + orderField + " " + orderDirection;
    return aioGetPageRecord(configName, dao, listID, pageNumber, pageSize, "select *", sql, new Object[] { "%{" + supId + "}%" });
  }
  
  public Map<String, Object> getPageStorageByTerm(String configName, String listID, int pageNum, int numPerPage, String term, String val, boolean inAll, int supId, int node, String orderField, String orderDirection, String storagePrivs)
  {
    StringBuilder sql = new StringBuilder("from b_storage where 1=1");
    List<Object> paras = new ArrayList();
    if (StringUtils.isNotBlank(val))
    {
      String[] terms = term.split("#");
      String[] vals = val.split("#");
      if (inAll)
      {
        if ("status".equals(terms[(terms.length - 1)]))
        {
          int statusVal = StringUtil.convertToInt(vals[(vals.length - 1)].trim());
          if (statusVal != 3)
          {
            sql.append(" and " + terms[(terms.length - 1)] + " like ?");
            paras.add("%" + statusVal + "%");
          }
        }
        else
        {
          sql.append(" and " + terms[(terms.length - 1)] + " like ?");
          paras.add("%" + vals[(vals.length - 1)].trim() + "%");
        }
      }
      else {
        for (int i = 0; i < terms.length; i++) {
          if ("status".equals(terms[i]))
          {
            int statusVal = StringUtil.convertToInt(vals[i].trim());
            if (statusVal != 3)
            {
              sql.append(" and " + terms[i] + " like ?");
              paras.add("%" + statusVal + "%");
            }
          }
          else
          {
            sql.append(" and " + terms[i] + " like ?");
            paras.add("%" + vals[i].trim() + "%");
          }
        }
      }
    }
    queryPrivs(sql, storagePrivs);
    if (supId >= 0) {
      if (node != 0)
      {
        sql.append(" and node = ?");
        paras.add(Integer.valueOf(node));
        sql.append(" and pids like ?");
        paras.add("%" + supId + "%");
      }
      else
      {
        sql.append(" and supId like ?");
        paras.add(Integer.valueOf(supId));
      }
    }
    sql.append(" order by " + orderField + " " + orderDirection);
    
    return dao.aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql.toString(), paras.toArray());
  }
  
  public Map<String, Object> getPageByTerms(String configName, String listID, int pageNum, int numPerPage, String term, String val, int supId, int node, String orderField, String orderDirection, String storagePrivs)
  {
    StringBuilder sql = new StringBuilder("from b_storage where 1=1");
    List<Object> paras = new ArrayList();
    if ((!val.equals("")) && (val != null))
    {
      if (term.equals("quick"))
      {
        sql.append(" and (code like ?");
        paras.add("%" + val + "%");
        sql.append(" or fullName like ?");
        paras.add("%" + val + "%");
        int statusVal = StringUtil.convertToInt(val.trim());
        if (statusVal != 3)
        {
          sql.append(" or status = ? )");
          paras.add(Integer.valueOf(statusVal));
        }
        else
        {
          sql.append(" or status <> ? )");
          paras.add(Integer.valueOf(0));
        }
      }
      if (term.equals("code"))
      {
        sql.append(" and code like ?");
        paras.add("%" + val + "%");
      }
      if (term.equals("name"))
      {
        sql.append(" and fullName like ?");
        paras.add("%" + val + "%");
      }
      if (term.equals("status"))
      {
        int statusVal = StringUtil.convertToInt(val.trim());
        if (statusVal != 3)
        {
          sql.append(" and status = ?");
          paras.add(Integer.valueOf(statusVal));
        }
      }
    }
    queryPrivs(sql, storagePrivs);
    if (supId >= 0)
    {
      if (node != 0)
      {
        sql.append(" and node = ?");
        paras.add(Integer.valueOf(node));
      }
      sql.append(" and pids like ?");
      paras.add("%" + supId + "%");
    }
    sql.append(" order by " + orderField + " " + orderDirection);
    
    return dao.aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql.toString(), paras.toArray());
  }
  
  public boolean hasChildren(String configName, int supId)
  {
    return Db.use(configName).queryLong("select count(id) from b_storage where supId=?", new Object[] { Integer.valueOf(supId) }).longValue() > 0L;
  }
  
  public Boolean codeIsExist(String configName, String code, int id)
  {
    return Boolean.valueOf(Db.use(configName).queryLong("SELECT count(code) FROM b_storage WHERE code = ?  and id <> ? ", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L);
  }
  
  public boolean verify(String configName, String attr, Integer id)
  {
    if (id == null) {
      return true;
    }
    StringBuffer sql = new StringBuffer("select " + attr + " from cc_stock_records where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from xs_sellbook_bill where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from cg_bought_bill where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from cc_stock_init where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from cc_takestock_bill where " + attr + " = " + id);
    

    sql.append(" limit 1");
    id = Db.use(configName).queryInt(sql.toString());
    return (id != null) && (id.intValue() > 0);
  }
  
  public static int getPsupId(String configName, int sid)
  {
    if (sid <= 0) {
      return 0;
    }
    return Db.use(configName).queryInt("select supId from b_storage where id=?", new Object[] { Integer.valueOf(sid) }).intValue();
  }
  
  public Map<String, Object> getLastPage(String configName, int pageNum, int numPerPage, int supId, String listID, String orderField, String orderDirection, String storagePrivs)
  {
    String sql = "from b_storage where status != 0 and supId = ?";
    if ((listID == null) || ("".equals(listID))) {
      sql = sql + " and status = 2 ";
    }
    sql = queryPrivs(sql, storagePrivs);
    sql = sql + "  order by " + orderField + " " + orderDirection;
    return aioGetLastPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getSupPage(String configName, int pageNum, int numPerPage, int supId, int ObjectId, String listID, String orderField, String orderDirection, String storagePrivs)
  {
    String sql = "from b_storage where status != 0 and supId = ?";
    if ((listID == null) || ("".equals(listID))) {
      sql = sql + " and status = 2 ";
    }
    sql = queryPrivs(sql, storagePrivs);
    sql = sql + "  order by " + orderField + " " + orderDirection;
    return aioGetPageByUpObject(configName, dao, listID, ObjectId, pageNum, numPerPage, "select *", sql, null, new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getSupPage(String configName, int pageNum, int numPerPage, String orderField, String orderDirection, int ObjectId, String listID, String fullName, String code, String storagePrivs)
  {
    StringBuffer sql = new StringBuffer(" from b_storage where 1=1 ");
    List<Object> paras = new ArrayList();
    if ((fullName != null) && (!fullName.trim().equals("")))
    {
      sql.append(" and fullName like ?");
      paras.add("%" + fullName + "%");
    }
    if ((code != null) && (!code.trim().equals("")))
    {
      sql.append(" and code like ?");
      paras.add("%" + code + "%");
    }
    queryPrivs(sql, storagePrivs);
    sql.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageByUpObject(configName, dao, "", ObjectId, pageNum, numPerPage, "select *", sql.toString(), null, new Object[] { paras });
  }
  
  public Map<String, Object> getPageDialogByTerms(String configName, int pageNum, int numPerPage, String orderField, String orderDirection, int supId, Map<String, Object> pars, String storagePrivs)
  {
    StringBuilder sql = new StringBuilder("from b_storage where 1=1");
    List<Object> paras = new ArrayList();
    if (pars != null)
    {
      if ((pars.get("termVal") != null) && (!"".equals(pars.get("termVal"))))
      {
        String term = pars.get("term").toString();
        Object val = pars.get("termVal");
        if ("quick".equals(term))
        {
          sql.append(" and ( code like ?");
          paras.add("%" + val + "%");
          sql.append(" or fullName like ?");
          paras.add("%" + val + "%");
          sql.append(" or spell like ? )");
          paras.add("%" + val + "%");
        }
        else
        {
          sql.append(" and " + term + " like ?");
          paras.add("%" + val + "%");
        }
      }
    }
    else if (supId >= 0)
    {
      sql.append(" and supId = ?");
      paras.add(Integer.valueOf(supId));
    }
    queryPrivs(sql, storagePrivs);
    sql.append(" and status =? ");
    paras.add(Integer.valueOf(AioConstants.STATUS_ENABLE));
    
    sql.append(" and node =? ");
    paras.add(Integer.valueOf(AioConstants.NODE_1));
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by rank asc ");
    }
    return Area.dao.aioGetPageRecord(configName, dao, "", pageNum, numPerPage, "select *", sql.toString(), paras.toArray());
  }
  
  public List<Model> loadNegativeStockInfo(String configName, String trIndexs, String productIds, String storageIds, String stockAmounts, String negativeStockAmounts)
  {
    List<Model> list = new ArrayList();
    if (!productIds.equals(""))
    {
      String[] trIndexArra = trIndexs.split(":");
      String[] productIdArra = productIds.split(":");
      String[] storageIdArra = storageIds.split(":");
      String[] stockAmountArra = stockAmounts.split(":");
      String[] negativeStockAmountArra = negativeStockAmounts.split(":");
      StringBuffer sb = null;
      for (int i = 0; i < productIdArra.length; i++)
      {
        sb = new StringBuffer("");
        sb.append("select product.*,storage.* from b_storage storage,b_product product where storage.id=? and product.id=?");
        Map<String, Class<? extends Model>> mapAlias = new HashMap();
        mapAlias.put("storage", Storage.class);
        mapAlias.put("product", Product.class);
        
        Model prdAndSto = findFirst(configName, sb.toString(), mapAlias, new Object[] { storageIdArra[i], productIdArra[i] });
        
        prdAndSto.put("trIndex", trIndexArra[i]);
        prdAndSto.put("stockAmount", stockAmountArra[i]);
        prdAndSto.put("negativeStockAmount", negativeStockAmountArra[i]);
        list.add(prdAndSto);
      }
    }
    return list;
  }
  
  public List<Model> getStorages(String configName, String storagePrivs, String storageIds)
  {
    StringBuffer sql = new StringBuffer();
    sql.append("select * from b_storage where node = 1");
    if ((storageIds != null) && (!storageIds.equals("")))
    {
      String[] stoArr = storageIds.split(",");
      for (int i = 0; i < stoArr.length; i++) {
        if (i == 0) {
          sql.append(" and ( pids like '%{" + stoArr[i] + "}%' ");
        } else {
          sql.append(" or pids like '%{" + stoArr[i] + "}%' ");
        }
      }
      sql.append(")");
    }
    queryPrivs(sql, storagePrivs);
    return dao.find(configName, sql.toString());
  }
  
  public List<Model> getChilds(String configName, String[] ids, String storagePrivs)
  {
    if ((ids == null) || (StringUtils.isBlank(ids[0])))
    {
      String sql = "select * from b_storage where node=?";
      sql = queryPrivs(sql, storagePrivs);
      return dao.find(configName, sql, new Object[] { Integer.valueOf(AioConstants.NODE_1) });
    }
    String idl = "";
    for (int i = 0; i < ids.length; i++)
    {
      if (i != 0) {
        idl = idl + ",";
      }
      idl = idl + ids[i];
    }
    return dao.find(configName, "select * from b_storage where id in (" + idl + ") and node=?", new Object[] { Integer.valueOf(AioConstants.NODE_1) });
  }
  
  public String getChildIdsBySupId(String configName, Integer supId)
  {
    String str = "";
    String sql = "select id from b_storage where pids like '%{" + supId + "}%' and status=" + AioConstants.STATUS_ENABLE;
    List<Model> modelList = dao.find(configName, sql);
    if ((modelList != null) && (modelList.size() > 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public String getAllChildIdsBySupId(String configName, Integer supId, String storagePrivs)
  {
    String str = "";
    String sql = "select id from b_storage where pids like '%{" + supId + "}%'";
    sql = queryPrivs(sql, storagePrivs);
    List<Model> modelList = dao.find(configName, sql);
    if ((modelList != null) && (modelList.size() > 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public String getAllChildIdsNoSupId(String configName, Integer supId, String storagePrivs)
  {
    String str = "";
    String sql = "select id from b_storage where pids not like '%{" + supId + "}%'";
    sql = queryPrivs(sql, storagePrivs);
    List<Model> modelList = dao.find(configName, sql);
    if ((modelList != null) && (modelList.size() > 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public Map<String, Object> sotrageMultiPage(String configName, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars, String storagePrivs)
  {
    StringBuffer selectSql = new StringBuffer("select *  ");
    StringBuffer fromSql = new StringBuffer(" from b_storage storage where storage.status!= " + AioConstants.STATUS_DISABLE);
    
    queryPrivs(fromSql, storagePrivs);
    if ((pars.get("termVal") != null) && (!pars.get("termVal").equals(""))) {
      fromSql.append(" and storage.node=" + AioConstants.NODE_1);
    } else {
      fromSql.append(" and storage.supId=" + supId);
    }
    StringBuffer appendStockSql = appendMutilStorageSql(orderField, orderDirection, pars);
    fromSql.append(appendStockSql);
    return aioGetPageRecord(configName, dao, "", pageNum, numPerPage, selectSql.toString(), fromSql.toString(), this.paras.toArray());
  }
  
  public Map<String, Object> sotrageMultiSearch(String configName, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars, String storagePrivs)
  {
    StringBuffer selectSql = new StringBuffer("select *  ");
    StringBuffer fromSql = new StringBuffer(" from b_storage storage where 1=1 ");
    queryPrivs(fromSql, storagePrivs);
    if ((pars.get("termVal") == null) || (pars.get("termVal").equals(""))) {
      fromSql.append(" and storage.supId=0");
    } else {
      fromSql.append(" and storage.node=" + AioConstants.NODE_1);
    }
    StringBuffer appendStockSql = appendMutilStorageSql(orderField, orderDirection, pars);
    fromSql.append(appendStockSql);
    return aioGetPageRecord(configName, dao, "", pageNum, numPerPage, selectSql.toString(), fromSql.toString(), this.paras.toArray());
  }
  
  public Map<String, Object> storageMultiDown(String configName, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars, String storagePrivs)
  {
    StringBuffer selectSql = new StringBuffer("select *  ");
    StringBuffer fromSql = new StringBuffer(" from b_storage storage where 1=1 ");
    queryPrivs(fromSql, storagePrivs);
    fromSql.append(" and storage.supId=" + supId);
    StringBuffer appendStockSql = appendMutilStorageSql(orderField, orderDirection, pars);
    fromSql.append(appendStockSql);
    return storageMultiDownPage(configName, "", supId, pageNum, numPerPage, selectSql.toString(), fromSql.toString(), storagePrivs, this.paras.toArray());
  }
  
  public Map<String, Object> storageMultiUp(String configName, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, int ObjectId, String listID, Map<String, Object> pars, String storagePrivs)
  {
    StringBuffer selectSql = new StringBuffer("select *  ");
    StringBuffer fromSql = new StringBuffer(" from b_storage storage where 1=1 ");
    queryPrivs(fromSql, storagePrivs);
    fromSql.append(" and storage.status=" + AioConstants.STATUS_ENABLE + " and storage.supId=" + supId);
    StringBuffer appendStockSql = appendMutilStorageSql(orderField, orderDirection, pars);
    fromSql.append(appendStockSql);
    return storageMultiUpPage(configName, "", supId, ObjectId, pageNum, numPerPage, selectSql.toString(), fromSql.toString(), storagePrivs, this.paras.toArray());
  }
  
  public Map<String, Object> storageMultiDownPage(String configName, String listID, int supId, int pageNum, int numPerPage, String selectSql, String fromSql, String storagePrivs, Object... pars)
  {
    Page page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, pars);
    List pageList = page.getList();
    String sqlId = "select id,pids from b_storage where supId=" + supId + " and status=" + AioConstants.STATUS_ENABLE;
    sqlId = queryPrivs(sqlId, storagePrivs);
    List<Model> listId = dao.find(configName, sqlId);
    String sql = "select id,pids from b_storage where status=" + AioConstants.STATUS_ENABLE;
    sql = queryPrivs(sql, storagePrivs);
    List<Model> listAllId = dao.find(configName, sql);
    if ((pageList.size() <= 0) && (pageNum > 1))
    {
      int count = page.getTotalRow();
      pageNum = count / numPerPage;
      if (count / numPerPage > count / numPerPage) {
        pageNum++;
      }
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, pars);
      pageList = page.getList();
    }
    int tp = page.getTotalPage();
    int tr = page.getTotalRow();
    int limit = (pageNum - 1) * numPerPage;
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(tp));
    maps.put("totalCount", Integer.valueOf(tr));
    maps.put("pageList", pageList);
    maps.put("listID", listID);
    maps.put("limit", Integer.valueOf(limit));
    
    maps.put("listAllIdAndPids", listAllId);
    maps.put("listSubIdAndPidsBySupId", listId);
    return maps;
  }
  
  public Map<String, Object> storageMultiUpPage(String configName, String listID, int supId, int upObjectId, int pageNum, int numPerPage, String selectSql, String fromSql, String storagePrivs, Object... pars)
  {
    String sqlId = "select id,pids from b_storage where supId=" + supId + " and status=" + AioConstants.STATUS_ENABLE;
    sqlId = queryPrivs(sqlId, storagePrivs);
    List<Model> listId = dao.find(configName, sqlId);
    
    String sql = "select id,pids from b_storage where status=" + AioConstants.STATUS_ENABLE;
    sql = queryPrivs(sql, storagePrivs);
    List<Model> listAllId = dao.find(configName, sql);
    
    List<Integer> ids = null;
    ids = Db.use(configName).query("select id from b_storage where supId=" + supId + " and status=" + AioConstants.STATUS_ENABLE);
    int objectIndex = 0;
    for (int i = 0; i < ids.size(); i++)
    {
      objectIndex++;
      if (((Integer)ids.get(i)).intValue() == upObjectId) {
        break;
      }
    }
    pageNum = objectIndex % numPerPage == 0 ? objectIndex / numPerPage : objectIndex / numPerPage + 1;
    Page page = null;
    if ((pars.length > 0) && (!pars[0].toString().equals("[]"))) {
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, pars);
    } else {
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null);
    }
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(page.getTotalPage()));
    maps.put("totalCount", Integer.valueOf(page.getTotalRow()));
    maps.put("pageList", page.getList());
    maps.put("listID", listID);
    maps.put("limit", Integer.valueOf((pageNum - 1) * numPerPage));
    
    maps.put("listAllIdAndPids", listAllId);
    maps.put("listSubIdAndPidsBySupId", listId);
    return maps;
  }
  
  public StringBuffer appendMutilStorageSql(String orderField, String orderDirection, Map<String, Object> pars)
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
        sql.append(" and( storage.code like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or storage.fullName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or storage.spell like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or storage.smallName like ? )");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("code"))
      {
        sql.append(" and storage.code like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("fullName"))
      {
        sql.append(" and storage.fullName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("spell"))
      {
        sql.append(" and storage.spell like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("smallName"))
      {
        sql.append(" and storage.smallName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
    }
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by storage.rank asc");
    }
    return sql;
  }
  
  public List<Model> storageMultiSearchBack(String configName, String ids)
  {
    String[] idArr = ids.split(",");
    StringBuffer sb = new StringBuffer("select id,fullName from b_storage where node=" + AioConstants.NODE_1);
    for (int i = 0; i < idArr.length; i++) {
      if (i == 0) {
        sb.append(" and pids like '%{" + idArr[i] + "}%'");
      } else {
        sb.append(" or pids like '%{" + idArr[i] + "}%'");
      }
    }
    return find(configName, sb.toString());
  }
  
  public Model getRecordByCode(String configName, Object code)
  {
    Model model = null;
    if (code != null)
    {
      String sql = "SELECT * FROM b_storage WHERE code=?";
      model = dao.findFirst(configName, sql, new Object[] { code });
    }
    return model;
  }
  
  public int getMaxRank(String configName)
  {
    int maxRank = 1;
    String sql = "SELECT MAX(rank) maxRank FROM b_storage";
    Model model = dao.findFirst(configName, sql);
    if (model.get("maxRank") != null) {
      maxRank = model.getInt("maxRank").intValue() + 1;
    }
    return maxRank;
  }
  
  public List<Model> getListByPids(String configName, String pids)
  {
    String sql = "SELECT * FROM  b_storage WHERE pids LIKE CONCAT('" + pids + "','%')";
    return find(configName, sql);
  }
  
  private String queryPrivs(String sql, String storagePrivs)
  {
    if ((StringUtils.isNotBlank(storagePrivs)) && (!"*".equals(storagePrivs))) {
      sql = sql + " and id in(" + storagePrivs + ")";
    } else if (StringUtils.isBlank(storagePrivs)) {
      sql = sql + " and id =0";
    }
    return sql;
  }
  
  private void queryPrivs(StringBuilder sql, String storagePrivs)
  {
    if ((StringUtils.isNotBlank(storagePrivs)) && (!"*".equals(storagePrivs))) {
      sql.append(" and id in(" + storagePrivs + ")");
    } else if (StringUtils.isBlank(storagePrivs)) {
      sql.append(" and id =0");
    }
  }
  
  private void queryPrivs(StringBuffer sql, String storagePrivs)
  {
    if ((StringUtils.isNotBlank(storagePrivs)) && (!"*".equals(storagePrivs))) {
      sql.append(" and id in(" + storagePrivs + ")");
    } else if (StringUtils.isBlank(storagePrivs)) {
      sql.append(" and id =0");
    }
  }
  
  public int storageCountByIds(String configName, String stroageIds, String tableName)
  {
    if ((stroageIds == null) || (stroageIds.equals(""))) {
      return 0;
    }
    StringBuffer sql = new StringBuffer();
    sql.append("select count(*) from " + tableName + " s where s.node=" + AioConstants.NODE_1);
    
    String[] storageArr = stroageIds.split(",");
    for (int i = 0; i < storageArr.length; i++) {
      if (i == 0) {
        sql.append(" and ( s.pids like '%{" + storageArr[i] + "}%' ");
      } else {
        sql.append(" or s.pids like '%{" + storageArr[i] + "}%' ");
      }
    }
    sql.append(")");
    Integer count = Integer.valueOf(String.valueOf(Db.use(configName).queryLong(sql.toString())));
    if (count == null) {
      count = Integer.valueOf(0);
    }
    return count.intValue();
  }
}
