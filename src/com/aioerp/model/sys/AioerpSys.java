package com.aioerp.model.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.util.HashMap;
import java.util.Map;

public class AioerpSys
  extends BaseDbModel
{
  public static AioerpSys dao = new AioerpSys();
  
  public Record getObj(String configName, String key)
  {
    Map<String, Map<String, Record>> tableNameMap = (Map)AioConstants.SYS_PARAM.get(configName);
    Map<String, Record> objMap = null;
    Record record = null;
    if (tableNameMap == null)
    {
      tableNameMap = new HashMap();
      objMap = new HashMap();
      record = Db.use(configName).findFirst("select * from aioerp_sys where key1='" + key + "'");
    }
    else
    {
      objMap = (Map)tableNameMap.get("aioerp_sys");
      if (objMap == null)
      {
        objMap = new HashMap();
        record = Db.use(configName).findFirst("select * from aioerp_sys where key1='" + key + "'");
      }
      else
      {
        record = (Record)objMap.get(key);
        if (record == null) {
          record = Db.use(configName).findFirst("select * from aioerp_sys where key1='" + key + "'");
        }
      }
    }
    if (record == null) {
      return null;
    }
    objMap.put(key, record);
    tableNameMap.put("aioerp_sys", objMap);
    AioConstants.SYS_PARAM.put(configName, tableNameMap);
    return record;
  }
  
  public String getValue1(String configName, String key)
  {
    String str = "";
    Record record = getObj(configName, key);
    if (record != null) {
      str = record.getStr("value1");
    }
    return str;
  }
  
  public Record sysSaveOrUpdate(String configName, String key, String value, String memo)
  {
    Map<String, Map<String, Record>> tableNameMap = (Map)AioConstants.SYS_PARAM.get(configName);
    Map<String, Record> objMap = null;
    Record record = null;
    if (tableNameMap == null)
    {
      tableNameMap = new HashMap();
      objMap = new HashMap();
      record = Db.use(configName).findFirst("select * from aioerp_sys where key1='" + key + "'");
    }
    else
    {
      objMap = (Map)tableNameMap.get("aioerp_sys");
      if (objMap == null)
      {
        objMap = new HashMap();
        record = Db.use(configName).findFirst("select * from aioerp_sys where key1='" + key + "'");
      }
      else
      {
        record = (Record)objMap.get(key);
        if (record == null) {
          record = Db.use(configName).findFirst("select * from aioerp_sys where key1='" + key + "'");
        }
      }
    }
    if (record == null)
    {
      record = new Record();
      record.set("key1", key);
      record.set("value1", value);
      record.set("memo", memo);
      Db.use(configName).save("aioerp_sys", record);
    }
    else
    {
      record.set("value1", value);
      Db.use(configName).update("aioerp_sys", record);
    }
    objMap.put(key, record);
    tableNameMap.put("aioerp_sys", objMap);
    AioConstants.SYS_PARAM.put(configName, tableNameMap);
    return record;
  }
  
  public int sysDelKey(String configName, String key)
  {
    Map<String, Map<String, Record>> tableNameMap = (Map)AioConstants.SYS_PARAM.get(configName);
    Map<String, Record> objMap = null;
    Record record = null;
    if (tableNameMap == null) {
      return 0;
    }
    objMap = (Map)tableNameMap.get("aioerp_sys");
    if (objMap == null) {
      return 0;
    }
    record = (Record)objMap.get(key);
    if (record == null) {
      return 0;
    }
    Db.use(configName).delete("aioerp_sys", record);
    objMap.remove(key);
    tableNameMap.put("aioerp_sys", objMap);
    AioConstants.SYS_PARAM.put(configName, tableNameMap);
    return 1;
  }
  
  public Boolean verifyInvertMake(String configName)
  {
    Boolean flag = Boolean.valueOf(true);
    String sql = "SELECT COUNT(id) FROM cc_stock_records";
    Long count = Db.use(configName).queryLong(sql);
    if (count.longValue() > 0L)
    {
      flag = Boolean.valueOf(false);
    }
    else
    {
      sql = "SELECT COUNT(id) FROM cc_takestock_bill";
      count = Db.use(configName).queryLong(sql);
      if (count.longValue() > 0L) {
        flag = Boolean.valueOf(false);
      }
    }
    return flag;
  }
}
