package com.aioerp.model.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Model;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class Permission
  extends BaseDbModel
{
  public static final Permission dao = new Permission();
  public static final String TABLE_NAME = "sys_permission";
  
  public boolean getDisabled()
  {
    boolean isDisabled = false;
    String privs = getStr("privs");
    if ((StringUtils.isNotBlank(privs)) || ("*".equals(privs))) {
      isDisabled = true;
    }
    return isDisabled;
  }
  
  public boolean getChecked(String url)
  {
    boolean isChecked = false;
    String privs = getStr("privs");
    String urlVal = getStr(url) + ",";
    if ((StringUtils.isNotBlank(privs)) && (
      (privs.indexOf(urlVal) != -1) || ("*".equals(privs)))) {
      isChecked = true;
    }
    return isChecked;
  }
  
  public List<Model> getList(String configName)
  {
    StringBuffer sql = new StringBuffer("select permission.* from sys_permission as permission");
    return find(configName, sql.toString());
  }
  
  public List<Model> getList(String configName, int pid)
  {
    StringBuffer sql = new StringBuffer("select permission.* from sys_permission as permission");
    
    sql.append(" where permission.pids like '%," + pid + ",%'");
    sql.append(" and permission.version <= " + AioConstants.VERSION);
    return find(configName, sql.toString());
  }
  
  public static boolean isVersion(String p)
  {
    List<Model> permissionList = AioConstants.permissionList;
    for (Model model : permissionList)
    {
      String seeUrl = model.getStr("seeUrl");
      String entUrl = model.getStr("entUrl");
      String tranUrl = model.getStr("tranUrl");
      String printUrl = model.getStr("printUrl");
      String addUrl = model.getStr("addUrl");
      String editUrl = model.getStr("editUrl");
      Integer version = model.getInt("version");
      if ((StringUtils.isNotBlank(p)) && ((p.equals(seeUrl)) || (p.equals(entUrl)) || (p.equals(tranUrl)) || (p.equals(printUrl)) || (p.equals(addUrl)) || (p.equals(editUrl))) && (version.intValue() <= AioConstants.VERSION)) {
        return true;
      }
    }
    return false;
  }
}
