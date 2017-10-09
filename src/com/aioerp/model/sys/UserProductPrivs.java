package com.aioerp.model.sys;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;

public class UserProductPrivs
  extends BaseDbModel
{
  public static final UserProductPrivs dao = new UserProductPrivs();
  public static final String TABLE_NAME = "sys_user_product_privs";
  
  public Integer getStatusByUserId(String configName, int productId, int userId)
  {
    String sql = "select privs from sys_user_product_privs where productId = ? and userId = ? limit 1";
    return Db.use(configName).queryInt(sql, new Object[] { Integer.valueOf(productId), Integer.valueOf(userId) });
  }
}
