package com.aioerp.common;

import com.aioerp.common.patch.ReloadBillRcw;
import com.aioerp.common.patch.ReloadDetailId;
import com.aioerp.common.patch.ResetImage;
import com.aioerp.common.patch.SetDefaultBackupTime;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.util.IO;
import com.jfinal.plugin.activerecord.Record;

public class PatchProgram
{
  public static void allPatch(String configName)
  {
    ReloadBillRcw.reloadRcw(configName);
    ResetImage.reset(configName);
    ReloadDetailId.reloadDetailId(configName);
    getIssueMysqlBinPath(configName);
    SetDefaultBackupTime.insertBackUpTime(configName);
  }
  
  public static void getIssueMysqlBinPath(String configName)
  {
    Record getMysqlBinPath = AioerpSys.dao.getObj(AioConstants.CONFIG_NAME, "getMysqlBinPath");
    if ((getMysqlBinPath != null) && (!getMysqlBinPath.getStr("value1").equals(""))) {
      return;
    }
    String path = IO.getMySQLPath();
    if (path == null) {
      path = "";
    }
    AioerpSys.dao.sysSaveOrUpdate(AioConstants.CONFIG_NAME, "getMysqlBinPath", path, "获取msyql默认bin目录,开发人员手动配置");
  }
}
