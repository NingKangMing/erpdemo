package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Staff;
import com.aioerp.model.sys.Permission;
import com.aioerp.model.sys.PermissionType;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.model.sys.User;
import com.aioerp.util.MD5Util;
import com.jfinal.plugin.activerecord.Model;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class PermissionController
  extends BaseController
{
  public void index()
  {
    String configName = loginConfigName();
    List<Model> userList = User.dao.getList(configName);
    List<Model> typeList = PermissionType.dao.getCacheList(AioConstants.CONFIG_NAME);
    
    setAttr("userList", userList);
    setAttr("typeList", typeList);
    render("page.html");
  }
  
  public void toPermission()
  {
    String configName = loginConfigName();
    int pid = getParaToInt(0, Integer.valueOf(0)).intValue();
    int typeNum = getParaToInt(1, Integer.valueOf(1)).intValue();
    int userId = getParaToInt(2, Integer.valueOf(0)).intValue();
    List<Model> permissionList = Permission.dao.getList(AioConstants.CONFIG_NAME, pid);
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    for (Model model : permissionList) {
      model.put("privs", user.getStr("privs"));
    }
    if ((user != null) && (user.getInt("grade").intValue() != 1)) {
      setAttr("disabled", Boolean.valueOf(true));
    } else {
      setAttr("disabled", Boolean.valueOf(false));
    }
    setAttr("permissionList", permissionList);
    setAttr("typeNum", Integer.valueOf(typeNum));
    render("permission.html");
  }
  
  public void permission()
  {
    String configName = loginConfigName();
    Integer userId = getParaToInt("userId");
    String permission = getPara("permission");
    String[] permissionArr = permission.split(",");
    String[] noPermissionArr = getPara("noPermission").split(",");
    Model user = User.dao.findById(configName, userId);
    if (user != null)
    {
      String privs = user.getStr("privs");
      if (StringUtils.isBlank(privs))
      {
        user.set("privs", permission);
      }
      else
      {
        for (String str : noPermissionArr) {
          if ((privs.endsWith(",")) && (StringUtils.isNotBlank(str))) {
            privs = privs.replaceAll(str + ",", "");
          } else {
            privs = privs.replaceAll(str, "");
          }
        }
        for (String str : permissionArr) {
          if ((privs.endsWith(",")) && (StringUtils.isNotBlank(str))) {
            privs = privs.replaceAll(str + ",", "");
          } else {
            privs = privs.replaceAll(str, "");
          }
        }
        if ((!privs.endsWith(",")) && (StringUtils.isNotBlank(privs))) {
          privs = privs + ",";
        }
        privs = privs + permission;
        user.set("privs", privs);
      }
      user.update(configName);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    renderJson();
  }
  
  public void allPermission()
  {
    String configName = loginConfigName();
    String permission = getPara(0, "all");
    Integer userId = getParaToInt(1);
    

    Model user = User.dao.findById(configName, userId);
    if (user != null)
    {
      if ("no".equals(permission)) {
        user.set("privs", "");
      } else {
        user.set("privs", AioConstants.ALL_PRIVS);
      }
      user.update(configName);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("isTrigger", Boolean.valueOf(true));
    setAttr("rel", "userList");
    setAttr("isClose", "no");
    renderJson();
  }
  
  public void toCopy()
  {
    String configName = loginConfigName();
    int userId = getParaToInt(0, Integer.valueOf(0)).intValue();
    List<Model> userList = User.dao.getListNoSelf(configName, Integer.valueOf(userId));
    setAttr("userId", Integer.valueOf(userId));
    setAttr("userList", userList);
    render("copy.html");
  }
  
  public void copy()
  {
    String configName = loginConfigName();
    int copyId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int userId = getParaToInt(1, Integer.valueOf(0)).intValue();
    Model copyUser = User.dao.findById(configName, Integer.valueOf(copyId));
    if (copyUser != null)
    {
      String privs = copyUser.getStr("privs");
      Model user = User.dao.findById(configName, Integer.valueOf(userId));
      if ((user != null) && (user.getInt("grade").intValue() != 3))
      {
        user.set("privs", privs);
        user.update(configName);
      }
    }
    setAttr("message", "权限复制成功!");
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    renderJson();
  }
  
  public void user()
  {
    String configName = loginConfigName();
    List<Model> userList = User.dao.getList(configName);
    setAttr("userList", userList);
    setAttr("objectId", getParaToInt(0, Integer.valueOf(0)));
    render("list.html");
  }
  
  public void addUser()
    throws ParseException
  {
    String configName = loginConfigName();
    Integer staffId = getParaToInt(0);
    Model staff = Staff.dao.findById(configName, staffId);
    if (staff == null)
    {
      setAttr("message", "用户信息错误!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    String name = staff.getStr("name");
    if (StringUtils.isBlank(name))
    {
      setAttr("message", "用户名错误!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    boolean isExist = User.dao.codeIsExist(configName, name, 0);
    if (isExist)
    {
      setAttr("message", "用户已存在!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    User user = new User();
    user.set("username", name);
    user.set("password", MD5Util.string2MD5(""));
    user.set("addTime", new Date());
    user.set("grade", Integer.valueOf(1));
    user.set("staffId", staff.getInt("id"));
    user.set("privs", "");
    user.set("productPrivs", "*");
    user.set("unitPrivs", "*");
    user.set("storagePrivs", "*");
    user.set("accountPrivs", "*");
    user.set("areaPrivs", "*");
    user.set("staffPrivs", "*");
    user.set("departmentPrivs", "*");
    user.save(configName);
    SysUserSearchDate.dao.addUserSearchDate(configName, loginUserId(), null, null);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("url", "sys/permission/user/" + user.getInt("id"));
    setAttr("rel", "userList");
    setAttr("isTrigger", Boolean.valueOf(true));
    renderJson();
  }
  
  public void delUser()
  {
    String configName = loginConfigName();
    Integer userId = getParaToInt(0);
    Model user = User.dao.findById(configName, userId);
    if (user == null)
    {
      setAttr("message", "用户不存在!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    if (user.getInt("grade").intValue() != 1)
    {
      setAttr("message", "系统管理员不能删除!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    user.delete(configName);
    SysUserSearchDate.dao.delUserSearchDateByUserId(configName, userId.intValue());
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("url", "sys/permission");
    setAttr("isClose", "no");
    renderJson();
  }
  
  public void status()
  {
    String configName = loginConfigName();
    Integer userId = getParaToInt(0);
    Model user = User.dao.findById(configName, userId);
    if (user == null)
    {
      setAttr("message", "用户不存在!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    if (user.getInt("grade").intValue() != 1)
    {
      setAttr("message", "系统管理员不能停用!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer status = user.getInt("status");
    if ((status != null) && (status.intValue() == AioConstants.STATUS_DISABLE)) {
      user.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
    } else {
      user.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
    }
    user.update(configName);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("url", "sys/permission/user/" + userId);
    setAttr("rel", "userList");
    setAttr("isClose", "no");
    renderJson();
  }
  
  public void toPassword()
  {
    String configName = loginConfigName();
    Integer userId = getParaToInt(0);
    Model user = User.dao.findById(configName, userId);
    setAttr("user", user);
    render("password.html");
  }
  
  public void password()
  {
    String configName = loginConfigName();
    Integer userId = getParaToInt("userId");
    Model user = User.dao.findById(configName, userId);
    String username = getPara("username");
    String password = getPara("password");
    
    boolean isExist = User.dao.codeIsExist(configName, username, userId.intValue());
    if (isExist)
    {
      setAttr("message", "用户已存在!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    if (user != null)
    {
      user.set("password", MD5Util.string2MD5(password));
      user.set("username", username);
      user.update(configName);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("url", "sys/permission/user/" + userId);
    setAttr("rel", "userList");
    renderJson();
  }
}
