package com.aioerp.model.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Area;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class BillType
  extends BaseDbModel
{
  public static final BillType dao = new BillType();
  private static final String tableName = "sys_billtype";
  
  public Map<String, Object> getPage(String configName, String listID, int pageNum, int numPerPage, int sortId)
  {
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", "from sys_billtype WHERE sortId=?", new Object[] { Integer.valueOf(sortId) });
  }
  
  public Map<String, Object> getFormatPage(String configName, String listID, int pageNum, int numPerPage, Integer sortId)
    throws Exception
  {
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("config", BillCodeConfig.class);
    String sel = "select type.*,config.*";
    StringBuffer sql = new StringBuffer("from sys_billtype as type");
    List<Object> paras = new ArrayList();
    sql.append(" left join sys_billcodeconfig as config on type.id = config.billId");
    sql.append(" where 1=1");
    if (sortId != null)
    {
      sql.append(" and type.sortId = ?");
      paras.add(sortId);
    }
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), map, paras.toArray());
  }
  
  public List<Model> getList(String configName, String screenType, int val)
  {
    StringBuffer sb = new StringBuffer("select * from sys_billtype where 1 = 1 and node = 1");
    if (val != 3) {
      sb.append(" and " + screenType + " = " + val);
    }
    sb.append(" AND version <= " + AioConstants.VERSION);
    sb.append(" order by name");
    return dao.find(configName, sb.toString());
  }
  
  public List<Model> getAllList(String configName)
  {
    return dao.find(configName, "select * from sys_billtype");
  }
  
  public static int getPsupId(String configName, int sid)
  {
    if (sid <= 0) {
      return 0;
    }
    return Db.use(configName).queryInt("select supId from sys_billtype where id=?", new Object[] { Integer.valueOf(sid) }).intValue();
  }
  
  public Map<String, Object> getPageBySupId(String configName, String listID, int pageNum, int numPerPage, String orderField, String orderDirection, int supId, String screenType)
  {
    StringBuffer sql = new StringBuffer("from sys_billtype");
    sql.append(" where 1=1");
    sql.append(" AND " + screenType + " = 1 ");
    sql.append(" AND supId = ?");
    sql.append(" AND version <= " + AioConstants.VERSION);
    sql.append(" order by " + orderField + " " + orderDirection);
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql.toString(), new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getSupPage(String configName, String listID, int pageNum, int numPerPage, String orderField, String orderDirection, int supId, int upObjectId, String screenType)
  {
    StringBuffer sql = new StringBuffer("from sys_billtype");
    sql.append(" where 1=1");
    sql.append(" AND " + screenType + " = 1 ");
    sql.append(" AND supId = ?");
    sql.append(" AND version <= " + AioConstants.VERSION);
    sql.append(" order by " + orderField + " " + orderDirection);
    return aioGetPageByUpObject(configName, dao, listID, upObjectId, pageNum, numPerPage, "select *", sql.toString(), null, new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getPageDialogByTerms(String configName, int pageNum, int numPerPage, String orderField, String orderDirection, int supId, Map<String, Object> pars)
  {
    StringBuilder sql = new StringBuilder("from sys_billtype where 1=1");
    List<Object> paras = new ArrayList();
    if (pars != null)
    {
      if ((pars.get("termVal") != null) && (!"".equals(pars.get("termVal"))))
      {
        String term = pars.get("term").toString();
        Object val = pars.get("termVal");
        if ("quick".equals(term))
        {
          sql.append(" and ( prefix like ?");
          paras.add("%" + val + "%");
          sql.append(" or name like ? )");
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
    sql.append(" AND " + pars.get("screenType").toString() + " = 1 ");
    
    sql.append(" and node =? ");
    paras.add(Integer.valueOf(AioConstants.NODE_1));
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by rank asc ");
    }
    return Area.dao.aioGetPageRecord(configName, dao, "", pageNum, numPerPage, "select *", sql.toString(), paras.toArray());
  }
  
  public static Record getObj(String configName, int billTypeId)
  {
    Map<String, Map<String, Record>> tableNameMap = (Map)AioConstants.SYS_PARAM.get(configName);
    Map<String, Record> objMap = null;
    Record record = null;
    if (tableNameMap == null)
    {
      tableNameMap = new HashMap();
      objMap = new HashMap();
      record = Db.use(configName).findById("sys_billtype", Integer.valueOf(billTypeId));
    }
    else
    {
      objMap = (Map)tableNameMap.get("sys_billtype");
      if (objMap == null)
      {
        objMap = new HashMap();
        record = Db.use(configName).findById("sys_billtype", Integer.valueOf(billTypeId));
      }
      else
      {
        record = (Record)objMap.get(Integer.valueOf(billTypeId));
        if (record == null) {
          record = Db.use(configName).findById("sys_billtype", Integer.valueOf(billTypeId));
        }
      }
    }
    if (record == null) {
      return null;
    }
    objMap.put(String.valueOf(billTypeId), record);
    tableNameMap.put("sys_billtype", objMap);
    AioConstants.SYS_PARAM.put(configName, tableNameMap);
    return record;
  }
  
  public static String getValue1(String configName, int billTypeId, String key)
  {
    String str = "";
    Record record = getObj(configName, billTypeId);
    if (record != null) {
      str = String.valueOf(record.get(key));
    }
    return str;
  }
}
