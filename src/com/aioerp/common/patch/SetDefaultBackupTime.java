package com.aioerp.common.patch;

import com.aioerp.common.AioConstants;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SetDefaultBackupTime
{
  public static void insertBackUpTime(String configName)
  {
    List<Record> plans = Db.use(AioConstants.CONFIG_NAME).find("select distinct databaseIds from aioerp_sys_backup_plan");
    boolean canInsert = true;
    if (plans.size() > 0) {
      for (Record record : plans)
      {
        String databaseIds = (String)record.get("databaseIds");
        List<String> list = Arrays.asList(databaseIds.split(","));
        if (list.contains(configName)) {
          canInsert = false;
        }
      }
    }
    if (canInsert)
    {
      Record r = new Record();
      r.set("name", "defaultBackUpFor " + configName);
      r.set("databaseIds", configName);
      r.set("startTime", "18:00:00");
      r.set("update_time", new Date());
      Db.use(AioConstants.CONFIG_NAME).save("aioerp_sys_backup_plan", r);
    }
  }
}
