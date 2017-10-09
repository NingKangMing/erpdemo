package com.aioerp.model.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Model;
import java.util.Date;

public class SysUserLog
  extends BaseDbModel
{
  public static final SysUserLog dao = new SysUserLog();
  
  public static void addUserLog(String whichDbName, String username, String ip, String actionType)
  {
    SysUserLog s = new SysUserLog();
    s.set("whichDbName", whichDbName);
    s.set("username", username);
    s.set("ip", ip);
    s.set("actionType", actionType);
    s.set("updateTime", new Date()).save(AioConstants.CONFIG_NAME);
  }
}
