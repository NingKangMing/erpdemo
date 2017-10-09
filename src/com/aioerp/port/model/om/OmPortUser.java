package com.aioerp.port.model.om;

import com.aioerp.model.BaseDbModel;
import com.aioerp.port.common.PortConstants;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.util.Map;

public class OmPortUser
  extends BaseDbModel
{
  public static String COMMON_SQL = "from om_port_user u left join b_unit unit on unit.id=u.unitId where u.status<>" + PortConstants.OM_STATUS0;
  public static final OmPortUser dao = new OmPortUser();
  
  public Model userUpdateById(String configName, int id)
  {
    return findFirst(configName, "select u.*,unit.code,unit.fullName from om_port_user u left join b_unit unit on unit.id=u.unitId where u.id=" + id + " and u.status<>" + PortConstants.OM_STATUS0);
  }
  
  public boolean existObjectByLoginName(String configName, int id, String loginName)
  {
    Model model = findFirst(configName, "select count(id) as num from om_port_user where status<>" + PortConstants.OM_STATUS0 + " and id<>? and loginName=?", new Object[] { Integer.valueOf(id), loginName });
    Long count = model.getLong("num");
    if (count.compareTo(new Long(0L)) > 0) {
      return true;
    }
    return false;
  }
  
  public boolean existObjectByLoginUnitId(String configName, int id, int unitId)
  {
    Model model = findFirst(configName, "select count(id) as num from om_port_user where status<>" + PortConstants.OM_STATUS0 + " and id<>? and unitId=?", new Object[] { Integer.valueOf(id), Integer.valueOf(unitId) });
    Long count = model.getLong("num");
    if (count.compareTo(new Long(0L)) > 0) {
      return true;
    }
    return false;
  }
  
  public boolean existOrderByLoginUnitId(String configName, int unitId)
  {
    Record r = Db.use(configName).findFirst("select count(id) as num from om_port_record where  unitId=?", new Object[] { Integer.valueOf(unitId) });
    Long count = r.getLong("num");
    if (count.compareTo(new Long(0L)) > 0) {
      return true;
    }
    return false;
  }
  
  public Map<String, Object> getPageByFilterLast(String configName, String listId, String searchPar1, String searchValue1, int pageIndex, int pageSize, String orderField, String orderDirection)
  {
    return aioGetLastPageRecord(configName, this, listId, pageIndex, pageSize, "select u.*,unit.code,unit.fullName", commSql(searchPar1, searchValue1, orderField, orderDirection).toString(), new Object[0]);
  }
  
  public Map<String, Object> getPageByFilter(String configName, String listId, String searchPar1, String searchValue1, int pageIndex, int pageSize, String orderField, String orderDirection)
  {
    return aioGetPageRecord(configName, this, listId, pageIndex, pageSize, "select u.*,unit.code,unit.fullName", commSql(searchPar1, searchValue1, orderField, orderDirection).toString(), new Object[0]);
  }
  
  public Map<String, Object> getPageByCondtion(String configName, String listId, String searchPar1, String searchValue1, int cPage, int pageSize, String orderField, String orderDirection)
  {
    return aioGetPageRecord(configName, this, listId, cPage, pageSize, "select u.*,unit.code,unit.fullName", commSql(searchPar1, searchValue1, orderField, orderDirection).toString(), new Object[0]);
  }
  
  public StringBuffer commSql(String searchPar1, String searchValue1, String orderField, String orderDirection)
  {
    StringBuffer sb = new StringBuffer(COMMON_SQL);
    if ((searchPar1 != null) && (!searchPar1.equals(""))) {
      if (searchPar1.equals("all"))
      {
        sb.append(" and (fullName like '%" + searchValue1 + "%'");
        sb.append(" or code like '%" + searchValue1 + "%'");
        sb.append(" or u.status =" + StringUtil.convertToInt(searchValue1));
        sb.append(" or loginName like '%" + searchValue1 + "%')");
      }
      else if (searchPar1.equals("status"))
      {
        sb.append(" and u.status=" + StringUtil.convertToInt(searchValue1));
      }
      else
      {
        sb.append(" and " + searchPar1 + " like '%" + searchValue1 + "%'");
      }
    }
    if ((orderField != null) && (!orderField.equals("")) && (orderDirection != null) && (!orderDirection.equals(""))) {
      sb.append(" order by u." + orderField + " " + orderDirection);
    } else {
      sb.append(" order by u.rank asc");
    }
    return sb;
  }
}
