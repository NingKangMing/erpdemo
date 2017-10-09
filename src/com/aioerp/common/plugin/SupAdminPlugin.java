package com.aioerp.common.plugin;

import com.aioerp.model.supadmin.WhichDb;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.AioerpSysBackup;
import com.aioerp.model.sys.AioerpSysBackupAuto;
import com.aioerp.model.sys.Permission;
import com.aioerp.model.sys.PermissionType;
import com.aioerp.model.sys.SysUserLog;
import com.aioerp.model.sys.User;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class SupAdminPlugin
{
  public SupAdminPlugin(ActiveRecordPlugin arp)
  {
    arp.addMapping("sys_user", User.class);
    arp.addMapping("sys_user_log", SysUserLog.class);
    arp.addMapping("aioerp_which_db", WhichDb.class);
    arp.addMapping("aioerp_sys_backup", AioerpSysBackup.class);
    arp.addMapping("aioerp_sys_backup_plan", AioerpSysBackupAuto.class);
    arp.addMapping("aioerp_sys", AioerpSys.class);
    arp.addMapping("sys_permission", Permission.class);
    arp.addMapping("sys_permission_type", PermissionType.class);
  }
}
