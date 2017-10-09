package com.aioerp.model.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SysConfig
  extends BaseDbModel
{
  public static final SysConfig dao = new SysConfig();
  public static final String TABLE_NAME = "aioerp_sys_config";
  
  public List<Model> getList(String configName)
  {
    String sql = "SELECT * FROM aioerp_sys_config";
    return find(configName, sql);
  }
  
  public static Boolean getConfig(String configName, int configId)
  {
    boolean flag = false;
    Map<String, Map<String, Record>> tableNameMap = (Map)AioConstants.SYS_PARAM.get(configName);
    Map<String, Record> objMap = null;
    Record record = null;
    if (tableNameMap == null)
    {
      tableNameMap = new HashMap();
      objMap = new HashMap();
      record = Db.use(configName).findById("aioerp_sys_config", Integer.valueOf(configId));
    }
    else
    {
      objMap = (Map)tableNameMap.get("aioerp_sys_config");
      if (objMap == null)
      {
        objMap = new HashMap();
        record = Db.use(configName).findById("aioerp_sys_config", Integer.valueOf(configId));
      }
      else
      {
        record = (Record)objMap.get(Integer.valueOf(configId));
        if (record == null) {
          record = Db.use(configName).findById("aioerp_sys_config", Integer.valueOf(configId));
        }
      }
    }
    if (record == null) {
      return Boolean.valueOf(false);
    }
    objMap.put(String.valueOf(configId), record);
    tableNameMap.put("aioerp_sys_config", objMap);
    AioConstants.SYS_PARAM.put(configName, tableNameMap);
    
    int val = 0;
    if (record != null) {
      val = record.getInt("isAllow").intValue();
    }
    if (val == 1) {
      flag = true;
    }
    return Boolean.valueOf(flag);
  }
  
  public boolean configUpdate(String configName, int configId, int isAllow)
  {
    boolean flag = false;
    Map<String, Map<String, Record>> tableNameMap = (Map)AioConstants.SYS_PARAM.get(configName);
    Map<String, Record> objMap = null;
    Record record = null;
    if (tableNameMap == null)
    {
      tableNameMap = new HashMap();
      objMap = new HashMap();
      record = Db.use(configName).findById("aioerp_sys_config", Integer.valueOf(configId));
    }
    else
    {
      objMap = (Map)tableNameMap.get("aioerp_sys_config");
      if (objMap == null)
      {
        objMap = new HashMap();
        record = Db.use(configName).findById("aioerp_sys_config", Integer.valueOf(configId));
      }
      else
      {
        record = (Record)objMap.get(Integer.valueOf(configId));
        if (record == null) {
          record = Db.use(configName).findById("aioerp_sys_config", Integer.valueOf(configId));
        }
      }
    }
    if (record == null) {
      return false;
    }
    record.set("isAllow", Integer.valueOf(isAllow));
    flag = Db.use(configName).update("aioerp_sys_config", record);
    
    objMap.put(String.valueOf(configId), record);
    tableNameMap.put("aioerp_sys_config", objMap);
    AioConstants.SYS_PARAM.put(configName, tableNameMap);
    return flag;
  }
}
