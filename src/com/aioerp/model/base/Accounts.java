package com.aioerp.model.base;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AccountsController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;

public class Accounts
  extends BaseDbModel
{
  public static final String TABLE_NAME = "b_accounts";
  public static String COMMON_SQL = "from b_accounts where status <> " + AioConstants.STATUS_DELETE;
  public static String COMMON_SQL_DIALOG = "from b_accounts where status = " + AioConstants.STATUS_ENABLE;
  public static final Accounts dao = new Accounts();
  public StringBuffer selectSql;
  public StringBuffer fromSql;
  public List<Object> paras;
  
  public void init(String accPrivs)
  {
    this.selectSql = new StringBuffer();
    this.fromSql = new StringBuffer();
    this.selectSql.append(" select *");
    this.fromSql.append(" from  b_accounts  where status=" + AioConstants.STATUS_ENABLE);
    
    this.fromSql = ComFunController.basePrivsSql(this.fromSql, accPrivs, null);
  }
  
  public Model getObjectById(String configName, int id)
  {
    return findById(configName, Integer.valueOf(id));
  }
  
  public Boolean add(String configName, Accounts model)
  {
    return Boolean.valueOf(model.save(configName));
  }
  
  public Boolean deleteById(String configName, Accounts model, int id)
  {
    return Boolean.valueOf(model.deleteById(configName, Integer.valueOf(id)));
  }
  
  public Boolean update(String configName, Accounts model)
  {
    return Boolean.valueOf(model.update(configName));
  }
  
  public Model getModelFirst(String configName, String type)
  {
    return dao.findFirst(configName, "select * " + COMMON_SQL + " and type=?", new Object[] { type });
  }
  
  public BigDecimal getMoneysByType(String configName, String type)
  {
    String sql = "SELECT (SELECT SUM(money) FROM b_accounts WHERE pids LIKE CONCAT(a.pids,'%')) moneys FROM b_accounts a WHERE a.type=?";
    return dao.findFirst(configName, sql, new Object[] { type }).getBigDecimal("moneys");
  }
  
  public int getPsupId(String configName, int supId)
  {
    StringBuffer sql = new StringBuffer("select supId " + COMMON_SQL + " and id=?");
    


    return Db.use(configName).queryInt(sql.toString(), new Object[] { Integer.valueOf(supId) }).intValue();
  }
  
  public String getAllChildIdsBySupId(String configName, String accPrivs, Integer supId)
  {
    String str = "";
    StringBuffer sql = new StringBuffer("select id from b_accounts where pids like '%{" + supId + "}%'");
    
    sql = ComFunController.basePrivsSql(sql, accPrivs, null);
    
    List<Model> modelList = dao.find(configName, sql.toString());
    if ((modelList != null) && (modelList.size() > 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public String getAllChildIdsNoSupId(String configName, String accPrivs, Integer supId)
  {
    String str = "";
    StringBuffer sql = new StringBuffer("select id from b_accounts where pids not like '%{" + supId + "}%'");
    
    sql = ComFunController.basePrivsSql(sql, accPrivs, null);
    
    List<Model> modelList = dao.find(configName, sql.toString());
    if ((modelList != null) && (modelList.size() > 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public boolean hasChildren(String configName, int supId)
  {
    StringBuffer sql = new StringBuffer("select count(*) " + COMMON_SQL + " and supId=?");
    return Db.use(configName).queryLong(sql.toString(), new Object[] { Integer.valueOf(supId) }).longValue() > 0L;
  }
  
  public List<Model> getParentObjects(String configName, String accPrivs, int supId)
  {
    StringBuffer sql = new StringBuffer("select * " + COMMON_SQL + " and node=? and pids like '%{" + supId + "}%' ");
    
    sql = ComFunController.basePrivsSql(sql, accPrivs, null);
    sql.append(" order by rank asc");
    return dao.find(configName, sql.toString(), new Object[] { Integer.valueOf(AioConstants.NODE_2) });
  }
  
  public boolean existObjectByNum(String configName, int id, String num)
  {
    Model model = findFirst(configName, "select count(id) as num " + COMMON_SQL + " and id<>? and code=?", new Object[] { Integer.valueOf(id), num });
    Long count = model.getLong("num");
    if (count.compareTo(new Long(0L)) > 0) {
      return true;
    }
    return false;
  }
  
  public Map<String, Object> getPageByCondtion(String configName, String accPrivs, String listId, int supId, Integer node, String searchPar1, String searchValue1, int cPage, int pageSize, String orderField, String orderDirection)
  {
    return aioGetPageRecord(configName, this, listId, cPage, pageSize, "select *", commSql(accPrivs, supId, node, searchPar1, searchValue1, orderField, orderDirection).toString(), new Object[0]);
  }
  
  public Map<String, Object> getBottomLists(String configName, String accPrivs, int pageNumber, int pageSize, String orderField, String orderDirection, int supId, String listId)
  {
    StringBuffer sql = new StringBuffer(COMMON_SQL + " and node=1 and pids like ? ");
    
    sql = ComFunController.basePrivsSql(sql, accPrivs, null);
    if ((orderField != null) && (!orderField.equals("")) && (orderDirection != null) && (!orderDirection.equals(""))) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by rank asc");
    }
    return aioGetPageRecord(configName, this, listId, pageNumber, pageSize, "select *", sql.toString(), new Object[] { "%{" + supId + "}%" });
  }
  
  public Map<String, Object> getPageBySupId(String configName, String accPrivs, int pageIndex, int pageSize, int supId, String listId)
  {
    StringBuffer sql = new StringBuffer(COMMON_SQL + " and  supId=? ");
    
    sql = ComFunController.basePrivsSql(sql, accPrivs, null);
    sql.append(" order by rank asc");
    return aioGetPageRecord(configName, this, listId, pageIndex, pageSize, "select *", sql.toString(), new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getPageByUpObject(String configName, String accPrivs, String listId, int supId, int upObjectId, int pageIndex, int pageSize, String searchPar1, String searchValue1, String orderField, String orderDirection)
  {
    return aioGetPageByUpObject(configName, this, listId, upObjectId, pageIndex, pageSize, "select *", commSql(accPrivs, supId, null, searchPar1, searchValue1, orderField, orderDirection).toString(), null, new Object[0]);
  }
  
  public Map<String, Object> getPageByFilterLast(String configName, String accPrivs, String listId, int supId, String searchPar1, String searchValue1, int pageIndex, int pageSize, String orderField, String orderDirection)
  {
    return aioGetLastPageRecord(configName, this, listId, pageIndex, pageSize, "select *", commSql(accPrivs, supId, null, searchPar1, searchValue1, orderField, orderDirection).toString(), new Object[0]);
  }
  
  public StringBuffer commSql(String accPrivs, int supId, Integer node, String searchPar1, String searchValue1, String orderField, String orderDirection)
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
          sb.append(searchByStatus("fast", searchValue1, null));
          sb.append(" or memo like '%" + searchValue1 + "%')");
        }
        else if (searchPar1.equals("status"))
        {
          sb.append(searchByStatus("condition", searchValue1, null));
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
    if ((node != null) && (node.intValue() == 1)) {
      sb.append(" and node=" + node);
    }
    sb = ComFunController.basePrivsSql(sb, accPrivs, null);
    if ((orderField != null) && (!orderField.equals("")) && (orderDirection != null) && (!orderDirection.equals(""))) {
      sb.append(" order by " + orderField + " " + orderDirection);
    } else {
      sb.append(" order by rank asc");
    }
    return sb;
  }
  
  public Map<String, Object> getTypeAccountFirst(String configName, String accPrivs, int supId, int pageIndex, int pageSize, String listId)
  {
    StringBuffer sql = new StringBuffer(COMMON_SQL_DIALOG + " and  supId= " + supId);
    
    sql = ComFunController.basePrivsSql(sql, accPrivs, null);
    sql.append(" order by rank asc");
    return aioGetPageRecord(configName, this, listId, pageIndex, pageSize, "select *", sql.toString(), new Object[0]);
  }
  
  public Map<String, Object> getTypeAccountList(String configName, String accPrivs, int pageIndex, int pageSize, String orderField, String orderDirection, String codeTypes, Map<String, Object> pars)
  {
    StringBuffer sql = new StringBuffer(COMMON_SQL_DIALOG);
    List<Object> params = new ArrayList();
    String[] accountIds = codeTypes.split(",");
    String term = pars.get("term").toString();
    String termVal = pars.get("termVal").toString();
    if (!StringUtil.isNull(termVal))
    {
      sql.append(" and node=" + AioConstants.NODE_1);
      sql.append(" and( 1<>1");
      for (int i = 0; i < accountIds.length; i++) {
        sql.append(" or pids like '%{" + accountIds[i] + "}%' ");
      }
      sql.append(")");
      if (term.equals("quick"))
      {
        sql.append(" and( code like ?");
        params.add("%" + termVal + "%");
        sql.append(" or fullName like ?");
        params.add("%" + termVal + "%");
        sql.append(" or spell like ? )");
        params.add("%" + termVal + "%");
      }
      else if (term.equals("code"))
      {
        sql.append(" and code like ?");
        params.add("%" + termVal + "%");
      }
      else if (term.equals("fullName"))
      {
        sql.append(" and fullName like ?");
        params.add("%" + termVal + "%");
      }
      else if (term.equals("spell"))
      {
        sql.append(" and spell like ?");
        params.add("%" + termVal + "%");
      }
    }
    else if (!StringUtil.isNull(codeTypes))
    {
      sql.append(" and( 1<>1");
      for (int i = 0; i < accountIds.length; i++)
      {
        sql.append(" or supId = ?");
        params.add(accountIds[i]);
      }
      sql.append(")");
    }
    sql = ComFunController.basePrivsSql(sql, accPrivs, null);
    sql.append(" order by " + orderField + " " + orderDirection);
    return aioGetPageRecord(configName, dao, "", pageIndex, pageSize, "select *", sql.toString(), params.toArray());
  }
  
  public Map<String, Object> getTypeAccountPageSearch(String configName, String accPrivs, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
  {
    StringBuffer sql = new StringBuffer(" from b_accounts where status=" + AioConstants.STATUS_ENABLE);
    sql.append(" and pids like '%{" + supId + "}%' ");
    sql.append(" and supId=" + supId);
    if ((pars != null) && (pars.get("termVal") != null) && (!pars.get("termVal").equals(""))) {
      sql.append(" and node=" + AioConstants.NODE_1);
    }
    sql = ComFunController.basePrivsSql(sql, accPrivs, null);
    StringBuffer appendSql = appendSql(orderField, orderDirection, pars);
    sql.append(appendSql);
    return aioGetPageRecord(configName, dao, "", pageNum, numPerPage, "select *", sql.toString(), this.paras.toArray());
  }
  
  public StringBuffer appendSql(String orderField, String orderDirection, Map<String, Object> pars)
  {
    StringBuffer sql = new StringBuffer("");
    this.paras = new ArrayList();
    String term = "";
    String termVal = "";
    if ((pars != null) && (pars.size() > 0))
    {
      term = (String)pars.get("term");
      termVal = (String)pars.get("termVal");
    }
    if ((pars != null) && (term != null) && (!term.equals("")) && (termVal != null) && (!termVal.equals(""))) {
      if (term.equals("quick"))
      {
        sql.append(" and( code like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or fullName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or spell like ? )");
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
    }
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by rank asc");
    }
    return sql;
  }
  
  public String whichCallBackAccountSql(String configName, String whichCallBack, String attrType, String val, int accountsTypeSupId)
  {
    StringBuffer accountBillSql = new StringBuffer(" and status=" + AioConstants.STATUS_ENABLE);
    Map<String, String> map = AccountsController.setAccountsArea(whichCallBack);
    if (map.size() == 0) {
      return accountBillSql.toString();
    }
    if (accountsTypeSupId != 0) {
      accountBillSql.append(" and supId=" + accountsTypeSupId);
    } else if (attrType != null) {
      attrType.equals("");
    }
    if (accountsTypeSupId == 0)
    {
      accountBillSql.append(" and (");
      int i = 0;
      for (Map.Entry<String, String> entry : map.entrySet())
      {
        if (((String)entry.getValue()).equals("noSelf"))
        {
          if (i == 0) {
            accountBillSql.append(" supId=(select id from b_accounts where type='" + (String)entry.getKey() + "')");
          } else {
            accountBillSql.append(" or supId=(select id from b_accounts where type='" + (String)entry.getKey() + "')");
          }
        }
        else if (((String)entry.getValue()).equals("self")) {
          if (i == 0) {
            accountBillSql.append(" type='" + (String)entry.getKey() + "'");
          } else {
            accountBillSql.append(" or type='" + (String)entry.getKey() + "'");
          }
        }
        i++;
      }
      accountBillSql.append(")");
    }
    if ((val != null) && (!val.equals(""))) {
      if (attrType.equals("accountsCode")) {
        accountBillSql.append(" and code like '%" + val + "%'");
      } else if (attrType.equals("accountsFullName")) {
        accountBillSql.append(" and fullName like '%" + val + "%'");
      }
    }
    StringBuffer allSql = new StringBuffer("select id from b_accounts where type in (");
    for (Map.Entry<String, String> entry : map.entrySet()) {
      allSql.append("'" + (String)entry.getKey() + "'");
    }
    allSql.append(")");
    List<Record> list = Db.use(configName).find(allSql.toString());
    for (int i = 0; i < list.size(); i++) {
      if (list.size() == 1) {
        accountBillSql.append(" and pids like '%{" + ((Record)list.get(i)).getInt("id") + "}%'");
      } else if (i == 0) {
        accountBillSql.append(" and (pids like '%{" + ((Record)list.get(i)).getInt("id") + "}%'");
      } else if (i == list.size() - 1) {
        accountBillSql.append(" or pids like '%{" + ((Record)list.get(i)).getInt("id") + "}%')");
      } else {
        accountBillSql.append(" or pids like '%{" + ((Record)list.get(i)).getInt("id") + "}%'");
      }
    }
    for (Object entry : map.entrySet()) {
      if (((String)((Map.Entry)entry).getValue()).equals("noSelf")) {
        accountBillSql.append(" and type<>'" + (String)((Map.Entry)entry).getKey() + "'");
      }
    }
    return accountBillSql.toString();
  }
  
  public Model getAccountsByNum(String configName, String accPrivs, String whichCallBack, String attrType, String val, int supId)
  {
    StringBuffer sql = new StringBuffer("select * from b_accounts where 1=1 ");
    
    sql = ComFunController.basePrivsSql(sql, accPrivs, null);
    
    sql.append(whichCallBackAccountSql(configName, whichCallBack, attrType, val, supId));
    return dao.findFirst(configName, sql.toString());
  }
  
  public boolean existObjectByIdCodeNode(String configName, String accPrivs, String whichCallBack, int id, String attrType, String attrVale, int node)
  {
    Model model = null;
    StringBuffer sql = new StringBuffer("select count(id) as num " + COMMON_SQL_DIALOG + " and id<>" + id);
    
    sql = ComFunController.basePrivsSql(sql, accPrivs, null);
    

    sql.append(whichCallBackAccountSql(configName, whichCallBack, attrType, attrVale, 0));
    sql.append(" and node=" + node);
    model = dao.findFirst(configName, sql.toString());
    Long count = Long.valueOf(0L);
    if (model != null) {
      count = model.getLong("num");
    }
    if (count.compareTo(new Long(0L)) > 0) {
      return true;
    }
    return false;
  }
  
  public Map<String, Object> orderStockSearch(String configName, String accPrivs, String whichCallBack, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
  {
    init(accPrivs);
    this.fromSql.append(whichCallBackAccountSql(configName, whichCallBack, "", "", supId));
    if ((pars.get("termVal") != null) && (!pars.get("termVal").equals(""))) {
      this.fromSql.append(" and node=" + AioConstants.NODE_1);
    }
    StringBuffer appendStockSql = appendSql(orderField, orderDirection, pars);
    this.fromSql.append(appendStockSql);
    return aioGetPageRecord(configName, this, "", pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), this.paras.toArray());
  }
  
  public Map<String, Object> orderDown(String configName, String accPrivs, String whichCallBack, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
  {
    init(accPrivs);
    this.fromSql.append(whichCallBackAccountSql(configName, whichCallBack, "", "", supId));
    StringBuffer appendStockSql = appendSql(orderField, orderDirection, pars);
    this.fromSql.append(appendStockSql);
    return orderDown(configName, accPrivs, whichCallBack, "", supId, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), this.paras.toArray());
  }
  
  public Map<String, Object> orderDown(String configName, String accPrivs, String whichCallBack, String listID, int supId, int pageNum, int numPerPage, String selectSql, String fromSql, Object... pars)
  {
    Page page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, pars);
    List pageList = page.getList();
    StringBuffer sb1 = new StringBuffer("select id,pids from b_accounts where status=" + AioConstants.STATUS_ENABLE);
    
    sb1 = ComFunController.basePrivsSql(sb1, accPrivs, null);
    sb1.append(whichCallBackAccountSql(configName, whichCallBack, "", "", supId));
    List<Model> listId = dao.find(configName, sb1.toString());
    
    StringBuffer sb = new StringBuffer("select id,pids from b_accounts where status=" + AioConstants.STATUS_ENABLE);
    
    sb = ComFunController.basePrivsSql(sb, accPrivs, null);
    sb.append(whichCallBackAccountSql(configName, whichCallBack, "", "", 0));
    List<Model> listAllId = dao.find(configName, sb.toString());
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
  
  public Map<String, Object> orderUpPlace(String configName, String accPrivs, String whichCallBack, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, int ObjectId, String listID, Map<String, Object> pars)
  {
    init(accPrivs);
    this.fromSql.append(whichCallBackAccountSql(configName, whichCallBack, "", "", supId));
    StringBuffer appendStockSql = appendSql(orderField, orderDirection, pars);
    this.fromSql.append(appendStockSql);
    return orderUpPlace(configName, accPrivs, whichCallBack, "", supId, ObjectId, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), this.paras.toArray());
  }
  
  public Map<String, Object> orderUpPlace(String configName, String accPrivs, String whichCallBack, String listID, int supId, int upObjectId, int pageNum, int numPerPage, String selectSql, String fromSql, Object... pars)
  {
    StringBuffer sb1 = new StringBuffer("select id,pids from b_accounts where status=" + AioConstants.STATUS_ENABLE);
    
    sb1 = ComFunController.basePrivsSql(sb1, accPrivs, null);
    sb1.append(whichCallBackAccountSql(configName, whichCallBack, "", "", supId));
    List<Model> listId = dao.find(configName, sb1.toString());
    
    StringBuffer sb = new StringBuffer("select id,pids from b_accounts where status=" + AioConstants.STATUS_ENABLE);
    
    sb = ComFunController.basePrivsSql(sb, accPrivs, null);
    sb.append(whichCallBackAccountSql(configName, whichCallBack, "", "", 0));
    List<Model> listAllId = dao.find(configName, sb.toString());
    
    List<Integer> ids = null;
    StringBuffer sb2 = new StringBuffer("select id from b_accounts where status=" + AioConstants.STATUS_ENABLE);
    
    sb2 = ComFunController.basePrivsSql(sb2, accPrivs, null);
    sb2.append(whichCallBackAccountSql(configName, whichCallBack, "", "", supId));
    ids = Db.use(configName).query(sb2.toString());
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
  
  public Map<String, Object> orderSearch(String configName, String accPrivs, String whichCallBack, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
  {
    init(accPrivs);
    this.fromSql.append(whichCallBackAccountSql(configName, whichCallBack, pars.get("term").toString(), pars.get("termVal").toString(), 0));
    
    StringBuffer appendStockSql = appendSql(orderField, orderDirection, pars);
    this.fromSql.append(appendStockSql);
    return aioGetPageRecord(configName, this, "", pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), this.paras.toArray());
  }
  
  public List<Model> searchBack(String configName, String accPrivs, String ids)
  {
    init(accPrivs);
    StringBuffer sb = new StringBuffer("select pids ");
    sb.append(this.fromSql);
    String[] strs = ids.split(",");
    for (int i = 0; i < strs.length; i++) {
      if (i == 0) {
        sb.append(" and pids like '%{" + strs[i] + "}%'");
      } else {
        sb.append(" or pids like '%{" + strs[i] + "}%'");
      }
    }
    sb.append(" group by id");
    List<Model> prds = find(configName, sb.toString());
    String intIds = "";
    for (int i = 0; i < prds.size(); i++)
    {
      String strPids = ((Model)prds.get(i)).getStr("pids");
      intIds = intIds + strPids.substring(strPids.lastIndexOf("{") + 1, strPids.lastIndexOf("}")) + ",";
    }
    intIds = intIds.substring(0, intIds.length() - 1);
    this.fromSql.append(" and node=" + AioConstants.NODE_1 + " and id in (" + intIds + ") group by id");
    this.selectSql.append(this.fromSql);
    List<Model> list = find(configName, this.selectSql.toString());
    return list;
  }
  
  public Accounts getSysParent(String configName, String accPrivs, Integer id)
  {
    if (id == null) {
      return null;
    }
    Accounts accounts = (Accounts)dao.findById(configName, id);
    if (accounts == null) {
      return null;
    }
    String pids = accounts.getStr("pids");
    String[] pidArr = pids.split("}");
    String ids = "";
    for (int i = 0; i < pidArr.length; i++)
    {
      if (i != 0) {
        ids = ids + ",";
      }
      ids = ids + pidArr[i].replace("{", "");
    }
    return getSysParent(configName, accPrivs, ids);
  }
  
  public Accounts getSysParent(String configName, String accPrivs, String ids)
  {
    StringBuffer sql = new StringBuffer("select * from b_accounts where 1=1 and isSysRow = 1");
    if (StringUtils.isNotBlank(ids)) {
      sql.append(" and id in(" + ids + ")");
    }
    sql = ComFunController.basePrivsSql(sql, accPrivs, null);
    return (Accounts)dao.findFirst(configName, sql.toString());
  }
  
  public Map<String, Object> orderStockPrdSelectEdit(String configName, String accPrivs, String whichCallBack, int supId, int selectId, int pageNum, int numPerPage, String orderField, String orderDirection)
  {
    init(accPrivs);
    this.fromSql.append(whichCallBackAccountSql(configName, whichCallBack, "", "", supId));
    
    StringBuffer appendStockSql = appendSql(orderField, orderDirection, null);
    this.fromSql.append(appendStockSql);
    Map<String, Object> mapResult = orderStockPrdSelectEdit(configName, whichCallBack, "", selectId, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), this.paras.toArray());
    return mapResult;
  }
  
  public Map<String, Object> orderStockPrdSelectEdit(String configName, String whichCallBack, String listID, int upObjectId, int pageNum, int numPerPage, String selectSql, String fromSql, Object... pars)
  {
    List<Model> proIds = null;
    if ((pars.length > 0) && (!pars[0].toString().equals("[]"))) {
      proIds = find(configName, "select id " + fromSql, pars);
    } else {
      proIds = find(configName, "select id " + fromSql);
    }
    int objectIndex = 0;
    for (int i = 0; i < proIds.size(); i++)
    {
      objectIndex++;
      if (((Model)proIds.get(i)).getInt("id").intValue() == upObjectId) {
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
    return maps;
  }
  
  public Boolean verify(String configName, String attr, Integer id)
  {
    if (id == null) {
      return Boolean.valueOf(true);
    }
    StringBuffer sql = new StringBuffer("select " + attr + " from cw_pay_type where " + attr + " = " + id);
    
    sql.append(" limit 1");
    id = Db.use(configName).queryInt(sql.toString());
    return Boolean.valueOf((id != null) && (id.intValue() > 0));
  }
  
  public Boolean isJf(String configName, Integer id)
  {
    boolean result = false;
    String typeStr = "0001,00010,00012";
    String sqlTtpe = "select * from b_accounts where type in(" + typeStr + ")";
    List<Model> list = find(configName, sqlTtpe);
    Model account = findById(configName, id);
    if (account == null) {
      return Boolean.valueOf(result);
    }
    String pids = account.getStr("pids");
    for (Model model : list)
    {
      Integer modelId = model.getInt("id");
      if (pids.indexOf("{" + modelId + "}") != -1)
      {
        result = true;
        break;
      }
    }
    return Boolean.valueOf(result);
  }
  
  public Boolean isDf(String configName, Integer id)
  {
    boolean result = false;
    String typeStr = "0007,0008,00011";
    String sqlTtpe = "select * from b_accounts where type in(" + typeStr + ")";
    List<Model> list = find(configName, sqlTtpe);
    Model account = findById(configName, id);
    if (account == null) {
      return Boolean.valueOf(result);
    }
    String pids = account.getStr("pids");
    for (Model model : list)
    {
      Integer modelId = model.getInt("id");
      if (pids.indexOf("{" + modelId + "}") != -1)
      {
        result = true;
        break;
      }
    }
    return Boolean.valueOf(result);
  }
}
