package com.aioerp.model;

import com.aioerp.common.AioConstants;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.ICallback;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.ModelUtil;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlHelper;
import com.jfinal.plugin.activerecord.Table;
import com.jfinal.plugin.activerecord.TableMapping;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class BaseDbModel
  extends ModelUtil
{
  public String getTableName()
  {
    return getTableInfo().getName();
  }
  
  public Table getTableInfo()
  {
    return TableMapping.me().getTable(getClass());
  }
  
  public List<Model> manyList(String configName, SqlHelper helper)
  {
    return find(configName, helper.getSql(), helper.getAlias(), helper.getParams());
  }
  
  public Model manyFirst(String configName, SqlHelper helper)
  {
    return findFirst(configName, helper.getSql(), helper.getAlias(), helper.getParams());
  }
  
  public Page<Model> manyPaginate(String configName, SqlHelper helper)
    throws SQLException, Exception
  {
    return super.paginate(configName, helper.getPageNumber(), helper.getPageSize(), helper.getSqlSelect(), helper.getSqlExceptSelect(), null, helper.getAlias(), helper.getParams());
  }
  
  public Map<String, Object> aioGetPageByUpObject(String configName, Model model, String listID, int upObjectId, int pageNum, int numPerPage, String selectSql, String fromSql, String obj, Object... pars)
  {
    List<Integer> ids = null;
    if ((pars != null) && (pars.length > 0) && (!pars[0].toString().equals("[]")))
    {
      if (StringUtils.isNotBlank(obj)) {
        ids = Db.use(configName).query("select " + obj + ".id " + fromSql, pars);
      } else {
        ids = Db.use(configName).query("select id " + fromSql, pars);
      }
    }
    else if (StringUtils.isNotBlank(obj)) {
      ids = Db.use(configName).query("select " + obj + ".id " + fromSql);
    } else {
      ids = Db.use(configName).query("select id  " + fromSql);
    }
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
      page = model.paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, pars);
    } else {
      page = model.paginate(configName, pageNum, numPerPage, selectSql, fromSql, null);
    }
    return comPageParam(page, pageNum, numPerPage, listID);
  }
  
  public Map<String, Object> aioGetPageManyByUpObject(String configName, String aliasName, String column, String listID, int upObjectId, int pageNum, int numPerPage, String selectSql, String fromSql, String distinctSql, Map<String, Class<? extends Model>> alias, Object... pars)
    throws SQLException, Exception
  {
    List<Model> ids = null;
    if ((pars.length > 0) && (!pars[0].toString().equals("[]"))) {
      ids = find(configName, "select " + aliasName + "." + column + " " + fromSql, alias, pars);
    } else {
      ids = find(configName, "select " + aliasName + "." + column + " " + fromSql, alias);
    }
    int objectIndex = 0;
    for (int i = 0; i < ids.size(); i++)
    {
      objectIndex++;
      Model model = (Model)ids.get(i);
      Model model2 = (Model)model.get(aliasName);
      Integer number = model2.getInt(column);
      if (number.intValue() == upObjectId) {
        break;
      }
    }
    pageNum = objectIndex % numPerPage == 0 ? objectIndex / numPerPage : objectIndex / numPerPage + 1;
    Page page = null;
    if ((pars.length > 0) && (!pars[0].toString().equals("[]"))) {
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, distinctSql, alias, pars);
    } else {
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, distinctSql, alias, new Object[0]);
    }
    return comPageParam(page, pageNum, numPerPage, listID);
  }
  
  public Map<String, Object> aioGetPageRecord(String configName, Model model, String listID, int pageNum, int numPerPage, String selectSql, String fromSql, Object... pars)
  {
    if (numPerPage == 0) {
      numPerPage = 1;
    }
    Page page = model.paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, pars);
    List pageList = page.getList();
    if ((pageList.size() <= 0) && (pageNum > 1))
    {
      int count = page.getTotalRow();
      pageNum = count / numPerPage;
      if (count == 0) {
        pageNum = 1;
      }
      if (count / numPerPage > count / numPerPage) {
        pageNum++;
      }
      page = model.paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, pars);
      pageList = page.getList();
    }
    return comPageParam(page, pageNum, numPerPage, listID);
  }
  
  public Map<String, Object> aioGetPageRecord(String configName, String listID, int pageNum, int numPerPage, String selectSql, String fromSql, String distinctSql, Object... pars)
  {
    Page page = Db.use(configName).paginate(pageNum, numPerPage, selectSql, fromSql, distinctSql, pars);
    List pageList = page.getList();
    if ((pageList.size() <= 0) && (pageNum > 1))
    {
      int count = page.getTotalRow();
      pageNum = count == 0 ? 1 : count / numPerPage;
      if ((count > 0) && 
        (count / numPerPage > count / numPerPage)) {
        pageNum++;
      }
      page = Db.use(configName).paginate(pageNum, numPerPage, selectSql, fromSql, distinctSql, pars);
      pageList = page.getList();
    }
    return comPageParam(page, pageNum, numPerPage, listID);
  }
  
  public Map<String, Object> aioGetPageManyRecord(String configName, String listID, int pageNum, int numPerPage, String selectSql, String fromSql, Map<String, Class<? extends Model>> alias, Object... pars)
    throws Exception
  {
    if (numPerPage == 0) {
      numPerPage = 1;
    }
    Page page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, alias, pars);
    List pageList = page.getList();
    if ((pageList.size() <= 0) && (pageNum > 1))
    {
      int count = page.getTotalRow();
      pageNum = count == 0 ? 1 : count / numPerPage;
      if ((count > 0) && 
        (count / numPerPage > count / numPerPage)) {
        pageNum++;
      }
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, alias, pars);
      pageList = page.getList();
    }
    return comPageParam(page, pageNum, numPerPage, listID);
  }
  
  public Map<String, Object> aioGetLastPageRecord(String configName, Model model, String listID, int pageNum, int numPerPage, String selectSql, String fromSql, Object... pars)
  {
    int totalRow = 0;
    totalRow = Integer.parseInt(Db.use(configName).queryLong("select count(*)  " + fromSql, pars).toString());
    
     if (totalRow == 0) {
    	 totalRow =1;
	} else {
		totalRow =totalRow / numPerPage;
	}
     pageNum =totalRow;
    if (pageNum==0) {
      pageNum++;
    }
    Page page = model.paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, pars);
    return comPageParam(page, pageNum, numPerPage, listID);
  }
  
  public Map<String, Object> aioGetLastPageManyRecord(String configName, String listID, int pageNum, int numPerPage, String selectSql, String fromSql, String tableId, Map<String, Class<? extends Model>> alias, Object... pars)
    throws SQLException, Exception
  {
    int totalRow = Integer.parseInt(Db.use(configName).queryLong("select count(" + tableId + ")  " + fromSql, pars).toString());
    pageNum = totalRow == 0 ? 1 : totalRow / numPerPage;
    if ((totalRow > 0) && 
      (totalRow / numPerPage > totalRow / numPerPage)) {
      pageNum++;
    }
    Page page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, alias, pars);
    return comPageParam(page, pageNum, numPerPage, listID);
  }
  
  public static int delAll(String configName, String tableName)
  {
    String sql = "DELETE FROM " + tableName;
    return Db.use(configName).update(sql);
  }
  
  public static int delDetailByBillId(String configName, String tableName, Integer billId)
  {
    String sql = "DELETE FROM " + tableName + " WHERE billId = " + billId;
    return Db.use(configName).update(sql);
  }
  
  public static List<Record> queryDetailsByBillId(String configName, String tableName, Integer billId)
  {
    String sql = "SELECT * FROM " + tableName + " WHERE billId = " + billId;
    return Db.use(configName).find(sql);
  }
  
  public Map<String, Object> aioGetPageManyRecord(String configName, String listID, int pageNum, int numPerPage, ICallback callback, Map<String, Class<? extends Model>> alias)
    throws SQLException, Exception
  {
    if (numPerPage == 0) {
      numPerPage = 1;
    }
    Page page = paginate(configName, pageNum, numPerPage, callback, null, new Object[] { alias });
    List pageList = page.getList();
    if ((pageList.size() <= 0) && (pageNum > 1))
    {
      int count = page.getTotalRow();
      pageNum = count / numPerPage;
      if (count == 0) {
        pageNum = 1;
      }
      if (count / numPerPage > count / numPerPage) {
        pageNum++;
      }
      page = paginate(configName, pageNum, numPerPage, callback, null, new Object[] { alias });
      pageList = page.getList();
    }
    return comPageParam(page, pageNum, numPerPage, listID);
  }
  
  public String searchByStatus(String type, String searchValue1, String obj)
  {
    String str = "";
    int status = StringUtil.convertToInt(searchValue1);
    if (status != AioConstants.STATUS_NULL) {
      if (type.equals("fast"))
      {
        if (obj == null) {
          str = " or status =" + status;
        } else {
          str = " or " + obj + ".status =" + status;
        }
      }
      else if (type.equals("condition")) {
        if (obj == null) {
          str = " and status =" + status;
        } else {
          str = " and " + obj + ".status =" + status;
        }
      }
    }
    return str;
  }
  
  public String supGetChildIdsBySupId(String configName, Integer supId)
  {
    String str = "";
    String tableName = getTableName();
    String sql = "select id from " + tableName + " where pids like '%{" + supId + "}%'";
    List<Model> modelList = find(configName, sql);
    if ((modelList != null) && (modelList.size() > 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public static Map<String, Object> comPageParam(Page page, int pageNum, int numPerPage, String listID)
  {
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
}
