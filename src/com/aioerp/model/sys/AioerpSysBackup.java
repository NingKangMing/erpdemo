package com.aioerp.model.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.util.List;
import java.util.Map;

public class AioerpSysBackup
  extends BaseDbModel
{
  public static AioerpSysBackup dao = new AioerpSysBackup();
  public static String COMMON_SQL = "from aioerp_sys_backup";
  
  public boolean existObjectByFileName(String fileName)
  {
    Record r = Db.use(AioConstants.CONFIG_NAME).findFirst("select count(id) as num from aioerp_sys_backup where file_name=?", new Object[] { fileName });
    Long count = r.getLong("num");
    if (count.compareTo(new Long(0L)) > 0) {
      return true;
    }
    return false;
  }
  
  public List<Record> getAllDataBase()
  {
    return Db.use(AioConstants.CONFIG_NAME).find("select * from aioerp_which_db");
  }
  
  public List<Record> getAllBackupPlan()
  {
    return Db.use(AioConstants.CONFIG_NAME).find("select * from aioerp_sys_backup_plan");
  }
  
  public Map<String, Object> getPageByRoot(int pageIndex, int pageSize, String listId)
  {
    return aioGetPageRecord(AioConstants.CONFIG_NAME, this, listId, pageIndex, pageSize, "select *", COMMON_SQL + " order by update_time desc", new Object[0]);
  }
}
