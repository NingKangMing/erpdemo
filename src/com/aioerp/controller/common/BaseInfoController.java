package com.aioerp.controller.common;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Area;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.sys.User;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class BaseInfoController
  extends BaseController
{
  public void verifyBaseAddPrivs()
  {
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String modelName = getPara(1);
    Subject subject = SecurityUtils.getSubject();
    User user = (User)subject.getPrincipal();
    Boolean flag = Boolean.valueOf(false);
    String isSort = getPara(2);
    String idsStr = "";
    boolean isDisable = true;
    String tableName = "b_product";
    String configName = loginConfigName();
    String privs = "";
    if ("product".equals(modelName))
    {
      tableName = "b_product";
      privs = user.getStr(PRODUCT_PRIVS);
      if (StringUtils.isNotBlank(isSort)) {
        flag = Boolean.valueOf(Product.dao.verify(configName, "productId", Integer.valueOf(supId)));
      }
      idsStr = Product.dao.supGetChildIdsBySupId(configName, Integer.valueOf(supId));
    }
    else if ("unit".equals(modelName))
    {
      tableName = "b_unit";
      privs = user.getStr(UNIT_PRIVS);
      if (StringUtils.isNotBlank(isSort)) {
        flag = Boolean.valueOf(Unit.dao.verify(configName, "unitId", Integer.valueOf(supId)));
      }
      idsStr = Unit.dao.supGetChildIdsBySupId(configName, Integer.valueOf(supId));
    }
    else if ("storage".equals(modelName))
    {
      tableName = "b_storage";
      privs = user.getStr(STORAGE_PRIVS);
      if (StringUtils.isNotBlank(isSort)) {
        flag = Boolean.valueOf(Storage.dao.verify(configName, "storageId", Integer.valueOf(supId)));
      }
      idsStr = Storage.dao.supGetChildIdsBySupId(configName, Integer.valueOf(supId));
    }
    else if ("acct".equals(modelName))
    {
      tableName = "b_accounts";
      privs = user.getStr(ACCOUNT_PRIVS);
      if (StringUtils.isNotBlank(isSort)) {
        flag = Accounts.dao.verify(configName, "accountId", Integer.valueOf(supId));
      }
      idsStr = Accounts.dao.supGetChildIdsBySupId(configName, Integer.valueOf(supId));
    }
    else if ("area".equals(modelName))
    {
      tableName = "b_area";
      privs = user.getStr(AREA_PRIVS);
      if (StringUtils.isNotBlank(isSort)) {
        flag = Boolean.valueOf(Area.dao.verify(configName, "areaId", Integer.valueOf(supId)));
      }
      idsStr = Area.dao.supGetChildIdsBySupId(configName, Integer.valueOf(supId));
    }
    else if ("staff".equals(modelName))
    {
      tableName = "b_staff";
      privs = user.getStr(STAFF_PRIVS);
      if (StringUtils.isNotBlank(isSort)) {
        flag = Boolean.valueOf(Staff.dao.verify(configName, "staffId", Integer.valueOf(supId)));
      }
      idsStr = Staff.dao.supGetChildIdsBySupId(configName, Integer.valueOf(supId));
    }
    else if ("depm".equals(modelName))
    {
      tableName = "b_department";
      privs = user.getStr(DEPARTMENT_PRIVS);
      if (StringUtils.isNotBlank(isSort)) {
        flag = Boolean.valueOf(Department.dao.verify(configName, "departmentId", Integer.valueOf(supId)));
      }
      idsStr = Department.dao.supGetChildIdsBySupId(configName, Integer.valueOf(supId));
    }
    isDisable = isDisable(configName, tableName, supId);
    if ((!isDisable) && (supId != 0))
    {
      setAttr("message", "该基本信息已停用不能再增加下级!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    if (flag.booleanValue())
    {
      setAttr("message", "该基本信息存在引用不能再增加下级!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    if ((StringUtils.isNotBlank(privs)) && (ALL_PRIVS.equals(privs)))
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      renderJson();
      return;
    }
    if ((StringUtils.isNotBlank(privs)) && (!ALL_PRIVS.equals(privs)))
    {
      String[] ids = idsStr.split(",");
      boolean verify = true;
      for (String id : ids) {
        if (privs.indexOf("," + id + ",") == -1)
        {
          verify = false;
          break;
        }
      }
      if (verify)
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN200);
        renderJson();
        return;
      }
      setAttr("message", "不具有该类的所有权限!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    setAttr("message", "不具有该类的所有权限!");
    setAttr("statusCode", AioConstants.HTTP_RETURN300);
    renderJson();
  }
  
  public static boolean isDisable(String configName, String tableName, int id)
  {
    boolean flag = true;
    Record r = Db.use(configName).findFirst("select * from " + tableName + " where id=" + id + " and status=" + AioConstants.STATUS_ENABLE);
    if (r == null) {
      flag = false;
    }
    return flag;
  }
}
