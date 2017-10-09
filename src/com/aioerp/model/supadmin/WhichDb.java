package com.aioerp.model.supadmin;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.util.List;

public class WhichDb
  extends BaseDbModel
{
  public static WhichDb dao = new WhichDb();
  
  public List<Model> getList()
  {
    return find(AioConstants.CONFIG_NAME, "select * from aioerp_which_db order by rank");
  }
  
  public List<Model> getEnableList(String dbIdStrs)
  {
    return find(AioConstants.CONFIG_NAME, "select * from aioerp_which_db where status=? and id in(" + dbIdStrs + ") order by rank", new Object[] { Integer.valueOf(AioConstants.STATUS_ENABLE) });
  }
  
  public List<Record> getDbList()
  {
    StringBuffer sb = new StringBuffer("select * from  aioerp_which_db where status=" + AioConstants.STATUS_ENABLE + " order by rank");
    List<Record> list = Db.use(AioConstants.CONFIG_NAME).find(sb.toString());
    return list;
  }
  
  public int getTruePwdListSize(String loginConfigId, String username, String password)
  {
    StringBuffer sb = new StringBuffer("select count(u.id) count from sys_user u ");
    sb.append("where u.username='" + username + "' and u.password='" + password + "' and u.status=" + AioConstants.STATUS_ENABLE);
    Record r = Db.use(loginConfigId).findFirst(sb.toString());
    if (r != null) {
      return r.getLong("count").intValue();
    }
    return 0;
  }
  
  public Record getFirstTrueWhichDb(String username, String password)
  {
    StringBuffer sb = new StringBuffer("select u.* from sys_user u left join aioerp_which_db db on db.id=u.whichDbId ");
    sb.append("where u.username='" + username + "' and u.password='" + password + "' and u.status=" + AioConstants.STATUS_ENABLE + " and u.whichDbId<>0 and db.status=" + AioConstants.STATUS_ENABLE + " group by whichDbId");
    Record r = Db.use(AioConstants.CONFIG_NAME).findFirst(sb.toString());
    return r;
  }
  
  public void updateToDisableOrEnable(int status, int id)
  {
    Db.use(AioConstants.CONFIG_NAME).update("update aioerp_which_db set status = ? where id = ?", new Object[] { Integer.valueOf(status), Integer.valueOf(id) });
  }
  
  public void updateToHasOm(int id)
  {
    Db.use(AioConstants.CONFIG_NAME).update("update aioerp_which_db set hasOm = " + AioConstants.STATUS_DISABLE);
    Db.use(AioConstants.CONFIG_NAME).update("update aioerp_which_db set hasOm = " + AioConstants.STATUS_ENABLE + " where id = " + id);
  }
  
  public String getChildConfigNameId()
  {
    Record r = Db.use(AioConstants.CONFIG_NAME).findFirst("select * from aioerp_which_db where status=" + AioConstants.STATUS_ENABLE + " and hasOm=" + AioConstants.STATUS_ENABLE);
    if (r == null) {
      return null;
    }
    return String.valueOf(r.getInt("id"));
  }
  
  public int maxAccountSet()
  {
    Record r = Db.use(AioConstants.CONFIG_NAME).findFirst("select count(id) count from aioerp_which_db where status=?", new Object[] { Integer.valueOf(AioConstants.STATUS_ENABLE) });
    if (r != null) {
      return Integer.valueOf(r.getLong("count").toString()).intValue();
    }
    return 0;
  }
  
  public boolean existWhiceObject(String configName, int id, String dataBaseName, String whichDbCode, String whichDbName)
  {
    Model model = findFirst(configName, "select count(id) as num from aioerp_which_db where id<>? and (dataBaseName=? or whichDbCode=? or whichDbName=?)", new Object[] { Integer.valueOf(id), dataBaseName, whichDbCode, whichDbName });
    Long count = model.getLong("num");
    if (count.compareTo(new Long(0L)) > 0) {
      return true;
    }
    return false;
  }
}
