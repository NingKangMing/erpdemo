package com.aioerp.model.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Staff;
import com.aioerp.util.MD5Util;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.util.List;

public class User
  extends BaseDbModel
{
  public static final User dao = new User();
  public static final String TABLE_NAME = "sys_user";
  private Staff staff;
  
  public Staff getStaff(String configName)
  {
    if (this.staff == null) {
      this.staff = ((Staff)Staff.dao.findById(configName, get("staffId")));
    }
    return this.staff;
  }
  
  public boolean codeIsExist(String configName, String username, int id)
  {
    return Db.use(configName).queryLong("select count(*) from sys_user where username =? and id != ? ", new Object[] { username, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public Model getOne(String configName)
  {
    return dao.findFirst(configName, "select * from sys_user");
  }
  
  public List<Model> getList(String configName)
  {
    StringBuffer sql = new StringBuffer("select user.*,staff.code as staffCode,staff.name as staffName from sys_user as user");
    sql.append(" left join b_staff as staff on staff.id = user.staffId order by user.id");
    
    return find(configName, sql.toString());
  }
  
  public List<Model> getListNoSelf(String configName, Integer userId)
  {
    StringBuffer sql = new StringBuffer("select user.* ,staff.code as staffCode,staff.name as staffName from sys_user as user");
    sql.append(" left join b_staff as staff on staff.id = user.staffId");
    sql.append(" where 1=1 and user.grade =1");
    if (userId != null) {
      sql.append(" and user.id !=" + userId);
    }
    return find(configName, sql.toString());
  }
  
  public List<Model> getPrivsList(String configName, String field)
  {
    StringBuffer sql = new StringBuffer("select user.* from sys_user as user");
    sql.append(" where 1=1 and user." + field + " is not null and user." + field + " != '*'");
    return find(configName, sql.toString());
  }
  
  public int getUserCount(String configName)
  {
    Record r = Db.use(configName).findFirst("select count(id) count from sys_user");
    if (r != null) {
      return Integer.valueOf(r.getLong("count").toString()).intValue();
    }
    return 0;
  }
  
  public int reloadAdminPwd(String configName)
  {
    String password = MD5Util.string2MD5(AioConstants.RELOAD_PWD);
    String sql = "update sys_user set password='" + password + "' where username='admin' and grade=2";
    return Db.use(configName).update(sql);
  }
}
