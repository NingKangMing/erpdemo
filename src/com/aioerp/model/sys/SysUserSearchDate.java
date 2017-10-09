package com.aioerp.model.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SysUserSearchDate
  extends BaseDbModel
{
  public static final SysUserSearchDate dao = new SysUserSearchDate();
  
  public Record getUserSearchDate(String configName, int userId)
    throws ParseException
  {
    Map<String, Map<String, Record>> tableNameMap = (Map)AioConstants.SYS_PARAM.get(configName);
    Map<String, Record> objMap = null;
    Record record = null;
    if (tableNameMap == null)
    {
      tableNameMap = new HashMap();
      objMap = new HashMap();
      record = Db.use(configName).findFirst("select * from sys_user_searchdate where userId=" + userId);
    }
    else
    {
      objMap = (Map)tableNameMap.get("sys_user_searchdate");
      if ((objMap == null) || (objMap.size() == 0))
      {
        objMap = new HashMap();
        record = Db.use(configName).findFirst("select * from sys_user_searchdate where userId=" + userId);
      }
      else
      {
        record = (Record)objMap.get(Integer.valueOf(userId));
        if (record == null) {
          record = Db.use(configName).findFirst("select * from sys_user_searchdate where userId=" + userId);
        }
      }
    }
    if (record == null) {
      record = addUserSearchDate(configName, userId, null, null);
    }
    objMap.put(String.valueOf(userId), record);
    tableNameMap.put("sys_user_searchdate", objMap);
    AioConstants.SYS_PARAM.put(configName, tableNameMap);
    return record;
  }
  
  public Record addUserSearchDate(String configName, int userId, String startDate, String endDate)
    throws ParseException
  {
    Record record = new Record();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    record.set("userId", Integer.valueOf(userId));
    if ((startDate == null) || (startDate.equals(""))) {
      startDate = sdf.format(DateUtils.addDays(new Date(), -7));
    }
    if ((endDate == null) || (endDate.equals(""))) {
      endDate = sdf.format(new Date());
    }
    record.set("startDate", sdf.parse(startDate));
    record.set("endDate", sdf.parse(endDate));
    Db.use(configName).save("sys_user_searchdate", record);
    return record;
  }
  
  public void setUserSearchDate(String configName, int userId, String startDate, String endDate)
  {
    Map<String, Map<String, Record>> tableNameMap = (Map)AioConstants.SYS_PARAM.get(configName);
    Map<String, Record> objMap = null;
    Record record = null;
    if (tableNameMap == null)
    {
      tableNameMap = new HashMap();
      objMap = new HashMap();
      record = Db.use(configName).findFirst("select * from sys_user_searchdate where userId=" + userId);
    }
    else
    {
      objMap = (Map)tableNameMap.get("aioerp_sys");
      if (objMap == null)
      {
        objMap = new HashMap();
        record = Db.use(configName).findFirst("select * from sys_user_searchdate where userId=" + userId);
      }
      else
      {
        record = (Record)objMap.get(Integer.valueOf(userId));
        if (record == null) {
          record = Db.use(configName).findFirst("select * from sys_user_searchdate where userId=" + userId);
        }
      }
    }
    record.set("startDate", startDate);
    record.set("endDate", endDate);
    Db.use(configName).update("sys_user_searchdate", record);
    
    objMap.put(String.valueOf(userId), record);
    tableNameMap.put("sys_user_searchdate", objMap);
    AioConstants.SYS_PARAM.put(configName, tableNameMap);
  }
  
  public void delUserSearchDateByUserId(String configName, int userId)
  {
    Map<String, Map<String, Record>> tableNameMap = (Map)AioConstants.SYS_PARAM.get(configName);
    Map<String, Record> objMap = null;
    Record record = null;
    if (tableNameMap != null)
    {
      objMap = (Map)tableNameMap.get("sys_user_searchdate");
      if (objMap != null) {
        record = (Record)objMap.get(Integer.valueOf(userId));
      }
    }
    if (record == null) {
      return;
    }
    Db.use(configName).delete("sys_user_searchdate", record);
    objMap.remove(String.valueOf(userId));
    tableNameMap.put("sys_user_searchdate", objMap);
    AioConstants.SYS_PARAM.put(configName, tableNameMap);
  }
}
