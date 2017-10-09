package com.aioerp.model.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Model;
import java.util.List;

public class PermissionType
  extends BaseDbModel
{
  public static final PermissionType dao = new PermissionType();
  public static final String TABLE_NAME = "sys_permission_type";
  
  public List<Model> getCacheList(String configName)
  {
    return findByCache(configName, "system", "permissionTypeList", "select * from sys_permission_type where  version <= " + AioConstants.VERSION);
  }
}
