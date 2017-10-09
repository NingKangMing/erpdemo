package com.aioerp.model.base;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.sys.AioerpFile;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class Staff
  extends BaseDbModel
{
  public static final Staff dao = new Staff();
  public static final String TABLE_NAME = "b_staff";
  private Department department;
  
  public Department getDepartment(String configName)
  {
    if (this.department == null) {
      this.department = ((Department)Department.dao.findById(configName, get("depmId")));
    }
    return this.department;
  }
  
  public AioerpFile getAioerpFile(String configName)
  {
    return (AioerpFile)AioerpFile.dao.findById(configName, get("sysImgId"));
  }
  
  public Map<String, Object> getPage(String configName, int pageNum, int pageSize, String listID, String staffPrivs)
  {
    StringBuffer sql = new StringBuffer(" from b_staff where status != 0");
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql.append(" and id in(" + staffPrivs + ")");
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql.append(" and id =0");
    }
    sql.append("order by rank");
    return aioGetPageRecord(configName, dao, listID, pageNum, pageSize, "select *", sql.toString(), new Object[0]);
  }
  
  public Map<String, Object> getChildPage(String configName, int pageNum, int pageSize, int supId, String listID, String name, String orderField, String orderDirection, Integer status, String staffPrivs)
  {
    String sql = " from b_staff as staff left join b_department as dep on staff.depmId = dep.id";
    sql = sql + " where staff.status != 0  and staff.supId =?";
    if (StringUtils.isNotBlank(name)) {
      sql = sql + " and staff.name like '%" + name + "%'";
    }
    if (status != null) {
      sql = sql + " and staff.status = " + status;
    }
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql = sql + " and staff.id in(" + staffPrivs + ")";
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql = sql + " and staff.id =0";
    }
    if (StringUtils.isNotBlank(orderField))
    {
      if (("departmentCode".equals(orderField)) || ("departmentName".equals(orderField))) {
        sql = sql + " order by " + orderField + " " + orderDirection;
      } else {
        sql = sql + " order by staff." + orderField + " " + orderDirection;
      }
    }
    else if (StringUtils.isBlank(staffPrivs)) {
      sql = sql + " order by staff.rank";
    }
    return aioGetPageRecord(configName, dao, listID, pageNum, pageSize, "select staff.*,dep.fullName as departmentName,dep.code as departmentCode", sql, new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getLastPage(String configName, int pageNum, int numPerPage, int supId, String listID, String orderField, String orderDirection, String staffPrivs)
  {
    String sql = " from b_staff as staff ";
    sql = sql + " left join b_department as dep on staff.depmId = dep.id";
    sql = sql + " where staff.status != 0 and staff.supId =?";
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql = sql + " and staff.id in(" + staffPrivs + ")";
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql = sql + " and staff.id =0";
    }
    if (StringUtils.isNotBlank(orderField))
    {
      if (("departmentCode".equals(orderField)) || ("departmentName".equals(orderField))) {
        sql = sql + " order by " + orderField + " " + orderDirection;
      } else {
        sql = sql + " order by staff." + orderField + " " + orderDirection;
      }
    }
    else {
      sql = sql + " order by staff.rank";
    }
    return aioGetLastPageRecord(configName, dao, listID, pageNum, numPerPage, "select staff.*,dep.code as departmentCode,dep.fullName as departmentName", sql, new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getSupPage(String configName, int pageNum, int numPerPage, int supId, int ObjectId, String listID, Integer status, String orderField, String orderDirection, String staffPrivs)
  {
    String sql = " from b_staff as staff ";
    sql = sql + " left join b_department as dep on staff.depmId = dep.id";
    sql = sql + " where staff.status != 0 and staff.supId =?";
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql = sql + " and staff.id in(" + staffPrivs + ")";
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql = sql + " and staff.id =0";
    }
    if (status != null) {
      sql = sql + " and staff.status=" + status;
    }
    if (StringUtils.isNotBlank(orderField))
    {
      if (("departmentCode".equals(orderField)) || ("departmentName".equals(orderField))) {
        sql = sql + " order by " + orderField + " " + orderDirection;
      } else {
        sql = sql + " order by staff." + orderField + " " + orderDirection;
      }
    }
    else {
      sql = sql + " order by staff.rank";
    }
    return aioGetPageByUpObject(configName, dao, listID, ObjectId, pageNum, numPerPage, "select staff.*,dep.fullName as departmentName,dep.code as departmentCode", sql, "staff", new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getPageByTerm(String configName, int pageNum, int pageSize, String term, String val, int supId, String listID, String orderField, String orderDirection, Integer node, String staffPrivs)
  {
    StringBuffer sql = new StringBuffer(" from b_staff as staff ");
    sql.append(" left join b_department as dep on staff.depmId = dep.id");
    sql.append(" where 1=1");
    List<Object> paras = new ArrayList();
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql.append(" and staff.id in(" + staffPrivs + ")");
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql.append(" and staff.id =0");
    }
    if (StringUtils.isNotBlank(val)) {
      if ("quick".equals(term))
      {
        sql.append(" and ( staff.code like ?");
        paras.add("%" + val + "%");
        sql.append(" or staff.name like ? ");
        paras.add("%" + val + "%");
        sql.append(" or staff.favoriteLimit like ?");
        paras.add("%" + val + "%");
        if (StringUtil.convertToInts(val) != 0) {
          if (StringUtil.convertToInts(val) == 1) {
            sql.append(" or staff.sysImgId  is not null");
          } else {
            sql.append(" or staff.sysImgId  is null");
          }
        }
        sql.append(" or staff.status = ? )");
        paras.add(Integer.valueOf(StringUtil.convertToInt(val)));
      }
      else if ("sysImgId".equals(term))
      {
        if (StringUtil.convertToInts(val) != 0)
        {
          if (StringUtil.convertToInts(val) == 1) {
            sql.append(" and staff.sysImgId  is not null");
          } else {
            sql.append(" and staff.sysImgId  is null");
          }
        }
        else
        {
          sql.append(" and staff.sysImgId = ?");
          paras.add(val);
        }
      }
      else if ("status".equals(term))
      {
        sql.append(" and staff.status = ? ");
        paras.add(Integer.valueOf(StringUtil.convertToInt(val)));
      }
      else
      {
        sql.append(" and staff." + term + " like ?");
        paras.add("%" + val + "%");
      }
    }
    if (supId >= 0)
    {
      sql.append(" and staff.pids like ?");
      paras.add("%{" + supId + "}%");
    }
    if (node != null)
    {
      sql.append(" and staff.node = ?");
      paras.add(node);
    }
    if (StringUtils.isNotBlank(orderField))
    {
      if (("departmentCode".equals(orderField)) || ("departmentName".equals(orderField))) {
        sql.append(" order by " + orderField + " " + orderDirection);
      } else {
        sql.append(" order by staff." + orderField + " " + orderDirection);
      }
    }
    else {
      sql.append(" order by staff.rank");
    }
    return aioGetPageRecord(configName, dao, listID, pageNum, pageSize, "select staff.*,dep.fullName as departmentName,dep.code as departmentCode", sql.toString(), paras.toArray());
  }
  
  public Map<String, Object> getListPage(String configName, int pageNum, int pageSize, int supId, String listID, String orderField, String orderDirection, String staffPrivs)
  {
    String sql = " from b_staff as staff left join b_department as dep on staff.depmId = dep.id";
    sql = sql + " where staff.node=1 and staff.status != 0 and staff.pids like ?";
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql = sql + " and staff.id in(" + staffPrivs + ")";
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql = sql + " and staff.id =0";
    }
    if (StringUtils.isNotBlank(orderField))
    {
      if (("departmentCode".equals(orderField)) || ("departmentName".equals(orderField))) {
        sql = sql + " order by " + orderField + " " + orderDirection;
      } else {
        sql = sql + " order by staff." + orderField + " " + orderDirection;
      }
    }
    else {
      sql = sql + " order by staff.rank";
    }
    return aioGetPageRecord(configName, dao, listID, pageNum, pageSize, "select staff.*,dep.fullName as departmentName,dep.code as departmentCode", sql, new Object[] { "%{" + supId + "}%" });
  }
  
  public String getChildIdsBySupId(String configName, int supId)
  {
    String str = "";
    String sql = "select id from b_staff where pids like '%{" + supId + "}%' and status=" + AioConstants.STATUS_ENABLE;
    List<Model> modelList = dao.find(configName, sql);
    if (modelList != null)
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public String getAllChildIdsBySupId(String configName, Integer supId)
  {
    String str = "";
    String sql = "select id from b_staff where pids like '%{" + supId + "}%'";
    List<Model> modelList = dao.find(configName, sql);
    if ((modelList != null) && (modelList.size() > 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public String getAllChildIdsNoSupId(String configName, Integer supId, String staffPrivs)
  {
    String str = "";
    String sql = "select id from b_staff where pids not like '%{" + supId + "}%'";
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql = sql + " and id in(" + staffPrivs + ")";
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql = sql + " and id =0";
    }
    List<Model> modelList = dao.find(configName, sql);
    if ((modelList != null) && (modelList.size() > 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public List<Model> getParent(String configName, String staffPrivs)
  {
    StringBuffer sql = new StringBuffer("select * from b_staff where  node = " + AioConstants.NODE_2);
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql.append(" and id in(" + staffPrivs + ")");
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql.append(" and id =0");
    }
    return dao.find(configName, sql.toString());
  }
  
  public List<Model> getChild(String configName, int supId, String staffPrivs)
  {
    StringBuffer sql = new StringBuffer("select * from b_staff where  supId =? ");
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql.append(" and id in(" + staffPrivs + ")");
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql.append(" and id =0");
    }
    sql.append(" order by rank");
    return dao.find(configName, sql.toString(), new Object[] { Integer.valueOf(supId) });
  }
  
  public List<Model> getTree11(String configName)
  {
    return dao.find(configName, "select id as id ,name as name, supId as pId,CONCAT('base/staff/child/',id) as url from b_staff where status != ?", new Object[] { Integer.valueOf(AioConstants.STATUS_DELETE) });
  }
  
  public List<Model> getAllStaff(String configName, String staffPrivs)
  {
    StringBuffer sql = new StringBuffer("select * from b_staff where status = ?");
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql.append(" and id in(" + staffPrivs + ")");
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql.append(" and id =0");
    }
    sql.append(" order by rank");
    return dao.find(configName, sql.toString(), new Object[] { Integer.valueOf(AioConstants.STATUS_ENABLE) });
  }
  
  public Boolean add(String configName, Staff model)
  {
    return Boolean.valueOf(model.save(configName));
  }
  
  public List<Model> getListByCode(String configName, String code, Integer id)
  {
    if (id != null) {
      return dao.find(configName, "select * from b_staff where status !=? and code =? and id !=?", new Object[] { Integer.valueOf(AioConstants.STATUS_DELETE), code, id });
    }
    return dao.find(configName, "select * from b_staff where status !=? and code =?", new Object[] { Integer.valueOf(AioConstants.STATUS_DELETE), code });
  }
  
  public int getPsupId(String configName, int sid)
  {
    if (sid <= 0) {
      return 0;
    }
    return Db.use(configName).queryInt("select supId from b_staff where id=?", new Object[] { Integer.valueOf(sid) }).intValue();
  }
  
  public Map<String, Object> getSupPage(String configName, int pageNum, int numPerPage, String orderField, String orderDirection, int ObjectId, String listID, Map<String, Object> pars, String staffPrivs)
  {
    StringBuffer sql = new StringBuffer(" from b_staff where 1=1 ");
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql.append(" and id in(" + staffPrivs + ")");
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql.append(" and id =0");
    }
    List<Object> paras = new ArrayList();
    if (pars != null)
    {
      if ((pars.get("name") != null) && (!"".equals(pars.get("name"))))
      {
        sql.append(" and name like ?");
        paras.add("%" + pars.get("name") + "%");
      }
      if ((pars.get("code") != null) && (!"".equals(pars.get("code"))))
      {
        sql.append(" and code like ?");
        paras.add("%" + pars.get("code") + "%");
      }
    }
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by rank asc");
    }
    return aioGetPageByUpObject(configName, dao, "", ObjectId, pageNum, numPerPage, "select *", sql.toString(), null, new Object[] { paras });
  }
  
  public Map<String, Object> getPage(String configName, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, String staffPrivs)
  {
    String sql = "";
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql = sql + " and id in(" + staffPrivs + ")";
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql = sql + " and id =0";
    }
    if (StringUtils.isNotBlank(orderField)) {
      sql = sql + " order by " + orderField + " " + orderDirection;
    } else {
      sql = sql + " order by rank asc";
    }
    return aioGetPageRecord(configName, dao, listId, pageNum, numPerPage, "select *", " from b_staff where 1=1" + sql, new Object[0]);
  }
  
  public Map<String, Object> getPage(String configName, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars, String staffPrivs)
  {
    StringBuffer sql = new StringBuffer("  from b_staff  s left join b_department d  on  s.depmId = d.id where 1=1 ");
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sql.append(" and s.id in(" + staffPrivs + ")");
    } else if (StringUtils.isBlank(staffPrivs)) {
      sql.append(" and s.id =0");
    }
    List<Object> paras = new ArrayList();
    if (pars != null)
    {
      if ((pars.get("termVal") != null) && (!"".equals(pars.get("termVal"))))
      {
        String term = pars.get("term").toString();
        Object val = pars.get("termVal");
        if ("quick".equals(term))
        {
          sql.append(" and ( s.code like ?");
          paras.add("%" + val + "%");
          sql.append(" or s.name like ?");
          paras.add("%" + val + "%");
          sql.append(" or s.spell like ? ");
          paras.add("%" + val + "%");
          sql.append(" or d.fullName like ? )");
          paras.add("%" + val + "%");
        }
        else if ("department".equals(term))
        {
          sql.append(" and d.fullName like ? ");
          paras.add("%" + val + "%");
        }
        else
        {
          sql.append(" and s." + term + " like ?");
          paras.add("%" + val + "%");
        }
      }
      if ((pars.get("haveNode") != null) && ("false".equals(pars.get("haveNode"))))
      {
        sql.append(" and s.node = ?");
        paras.add(Integer.valueOf(AioConstants.NODE_1));
      }
      else
      {
        sql.append(" and s.supId = ?");
        paras.add(Integer.valueOf(0));
      }
    }
    sql.append(" and s.status = ?");
    paras.add(Integer.valueOf(AioConstants.STATUS_ENABLE));
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by s." + orderField + " " + orderDirection);
    } else {
      sql.append(" order by s.rank asc");
    }
    return aioGetPageRecord(configName, listId, pageNum, numPerPage, "select s.*", sql.toString(), null, paras.toArray());
  }
  
  public int updateChildsStatus(String configName, int supId, int status)
  {
    StringBuffer sql = new StringBuffer("update b_staff set status = ? where pids like ?");
    List<Object> paras = new ArrayList();
    paras.add(Integer.valueOf(status));
    paras.add("%{" + supId + "}%");
    return Db.use(configName).update(sql.toString(), paras.toArray());
  }
  
  public boolean verify(String configName, String attr, Integer id)
  {
    if (id == null) {
      return true;
    }
    StringBuffer sql = new StringBuffer("select " + attr + " from bb_billhistory where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from bb_businessdraft where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from xs_sellbook_bill where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from cg_bought_bill where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from b_unit where " + attr + " = " + id);
    


    sql.append(" limit 1");
    id = Db.use(configName).queryInt(sql.toString());
    return (id != null) && (id.intValue() > 0);
  }
  
  public Map<String, Object> getStaffArapPage(String configName, String staffPrivs, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, Integer supId, Integer ObjectId, String startTime, String endTime, int node)
    throws SQLException, Exception
  {
    StringBuffer arapOccur = new StringBuffer("SELECT IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0) FROM cw_arap_records arap LEFT JOIN b_staff b ON b.id=arap.staffId WHERE b.pids LIKE CONCAT(st.pids,'%')");
    StringBuffer arapPyOccur = new StringBuffer("SELECT IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0) FROM cw_arap_records arap LEFT JOIN b_staff b ON b.id=arap.staffId WHERE b.pids LIKE CONCAT(st.pids,'%')");
    if (StringUtils.isNotBlank(startTime))
    {
      arapOccur.append(" and arap.recodeTime >= '" + startTime + "'");
      arapPyOccur.append(" and arap.recodeTime >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime))
    {
      arapOccur.append(" and arap.recodeTime < '" + endTime + " 23:59:59'");
      arapPyOccur.append(" and arap.recodeTime < '" + endTime + " 23:59:59'");
    }
    StringBuffer ssb = new StringBuffer(" SELECT temp.*,bs.* ");
    StringBuffer sb = new StringBuffer(" FROM");
    sb.append(" (");
    
    sb.append("SELECT  st.id staffId,st.supId staffSupId,st.rank staffRank, st.fullName staffName, st.node staffNode, st.pids staffPids,");
    sb.append("(CASE WHEN (" + arapOccur.toString() + ")<0 THEN ABS((" + arapOccur.toString() + ")) ELSE 0 END) getsOccur,");
    sb.append("(CASE WHEN (" + arapOccur.toString() + ")>0 THEN ABS((" + arapOccur.toString() + ")) ELSE 0 END) paysOccur,");
    sb.append("(CASE WHEN (" + arapPyOccur.toString() + ")<0 THEN ABS((" + arapPyOccur.toString() + ")) ELSE 0 END) getsPyOccur,");
    sb.append("(CASE WHEN (" + arapPyOccur.toString() + ")>0 THEN ABS((" + arapPyOccur.toString() + ")) ELSE 0 END) paysPyOccur");
    sb.append(" FROM b_staff st");
    
    sb.append(" UNION ALL");
    
    sb.append(" SELECT");
    sb.append(" (CASE WHEN arap.staffId IS NULL THEN 0 END) staffId,");
    sb.append("(CASE WHEN arap.staffId IS NULL THEN 0 END) staffSupId,");
    sb.append("(CASE WHEN arap.staffId IS NULL THEN (SELECT MAX(rank)+1 FROM b_staff) END) staffRank,");
    sb.append("(CASE WHEN arap.staffId IS NULL THEN '其他职员' END) staffName,");
    sb.append("(CASE WHEN arap.staffId IS NULL THEN 1 END) staffNode,");
    sb.append("(CASE WHEN arap.staffId IS NULL THEN '{0}' END) staffPids,");
    sb.append("(CASE WHEN (IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0))<0 THEN ABS(IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0)) ELSE 0 END ) getsOccur,");
    sb.append("(CASE WHEN (IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0))>0 THEN ABS(IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0)) ELSE 0 END ) paysOccur,");
    sb.append("(CASE WHEN (IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0))<0 THEN ABS(IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0)) ELSE 0 END ) getsPyOccur,");
    sb.append("(CASE WHEN (IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0))>0 THEN ABS(IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0)) ELSE 0 END ) paysPyOccur");
    sb.append(" FROM cw_arap_records arap WHERE arap.staffId IS NULL");
    if (StringUtils.isNotBlank(startTime)) {
      sb.append(" and arap.recodeTime >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      sb.append(" and arap.recodeTime < '" + endTime + " 23:59:59'");
    }
    sb.append(")");
    sb.append(" temp LEFT JOIN b_staff bs ON temp.staffId = bs.id WHERE 1=1");
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sb.append(" and bs.id in (" + staffPrivs + ") ");
    } else if (StringUtils.isBlank(staffPrivs)) {
      sb.append(" and bs.id = 0 ");
    }
    if (node == 0)
    {
      sb.append(" AND staffSupId=" + supId);
    }
    else if (node == 1)
    {
      sb.append(" AND staffNode = 1 ");
      sb.append(" AND staffPids LIKE '%" + supId + "%' ");
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("bs", Staff.class);
    if ((ObjectId != null) && (ObjectId.intValue() > 0)) {
      return aioGetPageManyByUpObject(configName, "bs", "id", listId, ObjectId.intValue(), pageNum, numPerPage, ssb.toString(), sb.toString(), null, alias, new Object[0]);
    }
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, ssb.toString(), sb.toString(), alias, new Object[0]);
  }
  
  public Map<String, Object> getStaffArapDetailPage(String configName, String unitPrivs, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, String startTime, String endTime, String pids)
    throws SQLException, Exception
  {
    StringBuffer ssb = new StringBuffer("SELECT (IFNULL(arap.addMoney,0)-IFNULL(arap.subMoney,0)) money,bu.*");
    
    StringBuffer sb = new StringBuffer(" FROM cw_arap_records arap LEFT JOIN b_unit bu ON bu.id=arap.unitId");
    sb.append(" WHERE 1=1");
    if ((StringUtils.isNotBlank(unitPrivs)) && (!"*".equals(unitPrivs))) {
      sb.append(" and bu.id in (" + unitPrivs + ") ");
    } else if (StringUtils.isBlank(unitPrivs)) {
      sb.append(" and bu.id = 0 ");
    }
    if (pids != null) {
      sb.append(" and arap.staffId IN(SELECT id FROM b_staff WHERE pids LIKE CONCAT('" + pids + "','%'))");
    } else {
      sb.append(" AND arap.staffId IS NULL");
    }
    if (StringUtils.isNotBlank(startTime)) {
      sb.append(" and arap.recodeTime >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      sb.append(" and arap.recodeTime < '" + endTime + " 23:59:59'");
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("bu", Unit.class);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, ssb.toString(), sb.toString(), alias, new Object[0]);
  }
  
  public Map<String, Object> getStaffArapUnitDetailPage(String configName, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, String pids, String pattern)
    throws SQLException, Exception
  {
    String arapMoney = "arap.addMoney";
    String ppyMoney = "arap.addPrepay";
    if (pattern.equals("pays"))
    {
      arapMoney = "arap.subMoney";
      ppyMoney = "arap.subPrepay";
    }
    StringBuffer ssb = new StringBuffer("SELECT " + arapMoney + " arapMoney," + ppyMoney + " ppyMoney,bu.*");
    
    StringBuffer sb = new StringBuffer(" FROM cw_arap_records arap LEFT JOIN b_unit bu ON bu.id=arap.unitId");
    sb.append(" WHERE 1=1");
    if (pids != null) {
      sb.append(" and arap.staffId IN(SELECT id FROM b_staff WHERE pids LIKE CONCAT('" + pids + "','%'))");
    } else {
      sb.append(" AND arap.staffId IS NULL");
    }
    sb.append(" and " + arapMoney + ">0");
    
    sb.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("bu", Unit.class);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, ssb.toString(), sb.toString(), alias, new Object[0]);
  }
  
  public Map<String, Object> getStaffBusCountPage(String configName, String staffPrivs, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, String startTime, String endTime)
    throws SQLException, Exception
  {
    StringBuffer accounts = new StringBuffer("SELECT IFNULL(SUM(CASE WHEN cw.type=0 THEN  cw.payMoney ELSE cw.payMoney*-1 END),0) FROM cw_pay_type cw LEFT JOIN b_accounts ba ON cw.accountId=ba.id WHERE 1=1 AND IFNULL(cw.staffId,0)=temp.wStaffId");
    
    StringBuffer jhdMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory WHERE billTypeId=5 AND IFNULL(staffId,0)=wStaffId");
    StringBuffer jhReturnMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory WHERE billTypeId=6 AND IFNULL(staffId,0)=wStaffId");
    StringBuffer jhBarterMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory WHERE billTypeId=12 AND IFNULL(staffId,0)=wStaffId");
    
    StringBuffer jhdArap = new StringBuffer("SELECT IFNULL(SUM(subMoney),0) FROM cw_arap_records WHERE billTypeId=5 AND IFNULL(staffId,0)=wStaffId");
    StringBuffer jhReturnArap = new StringBuffer("SELECT IFNULL(SUM(addMoney),0) FROM cw_arap_records WHERE billTypeId=6 AND IFNULL(staffId,0)=wStaffId");
    StringBuffer jhBarterArap = new StringBuffer("SELECT IFNULL(SUM(subMoney),0) FROM cw_arap_records WHERE billTypeId=12 AND IFNULL(staffId,0)=wStaffId");
    

    StringBuffer xsdMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory  WHERE billTypeId=4 AND IFNULL(staffId,0)=wStaffId");
    StringBuffer xsReturnMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory WHERE billTypeId=7 AND IFNULL(staffId,0)=wStaffId");
    StringBuffer xsBarterMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory WHERE billTypeId=13 AND IFNULL(staffId,0)=wStaffId");
    
    StringBuffer xsdArap = new StringBuffer("SELECT IFNULL(SUM(addMoney),0) FROM cw_arap_records WHERE billTypeId=4 AND IFNULL(staffId,0)=wStaffId");
    StringBuffer xsReturnArap = new StringBuffer("SELECT IFNULL(SUM(subMoney),0) FROM cw_arap_records WHERE billTypeId=7 AND IFNULL(staffId,0)=wStaffId");
    StringBuffer xsBarterArap = new StringBuffer("SELECT IFNULL(SUM(addMoney),0) FROM cw_arap_records WHERE billTypeId=13 AND IFNULL(staffId,0)=wStaffId");
    

    StringBuffer xsdCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_sell_detail d LEFT JOIN xs_sell_bill b ON d.billId = b.id WHERE IFNULL(staffId,0)=wStaffId");
    StringBuffer xsReturnCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_return_detail d LEFT JOIN xs_return_bill b ON d.billId = b.id WHERE IFNULL(staffId,0)=wStaffId");
    StringBuffer xsBarterOutCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_barter_detail d LEFT JOIN xs_barter_bill b ON d.billId = b.id WHERE IFNULL(staffId,0)=wStaffId AND d.type=2");
    StringBuffer xsBarterInCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_barter_detail d LEFT JOIN xs_barter_bill b ON d.billId = b.id WHERE IFNULL(staffId,0)=wStaffId AND d.type=1");
    if (StringUtils.isNotBlank(startTime))
    {
      accounts.append(" and cw.payTime >= '" + startTime + "'");
      
      jhdMoney.append(" and recodeDate >= '" + startTime + "'");
      jhReturnMoney.append(" and recodeDate >= '" + startTime + "'");
      jhBarterMoney.append(" and recodeDate >= '" + startTime + "'");
      jhdArap.append(" and recodeTime >= '" + startTime + "'");
      jhReturnArap.append(" and recodeTime >= '" + startTime + "'");
      jhBarterArap.append(" and recodeTime >= '" + startTime + "'");
      
      xsdMoney.append(" and recodeDate >= '" + startTime + "'");
      xsReturnMoney.append(" and recodeDate >= '" + startTime + "'");
      xsBarterMoney.append(" and recodeDate >= '" + startTime + "'");
      xsdArap.append(" and recodeTime >= '" + startTime + "'");
      xsReturnArap.append(" and recodeTime >= '" + startTime + "'");
      xsBarterArap.append(" and recodeTime >= '" + startTime + "'");
      
      xsdCost.append(" and b.recodeDate >= '" + startTime + "'");
      xsReturnCost.append(" and b.recodeDate >= '" + startTime + "'");
      xsBarterOutCost.append(" and b.recodeDate >= '" + startTime + "'");
      xsBarterInCost.append(" and b.recodeDate >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime))
    {
      accounts.append(" and cw.payTime < '" + endTime + " 23:59:59'");
      
      jhdMoney.append(" and recodeDate < '" + endTime + " 23:59:59'");
      jhReturnMoney.append(" and recodeDate < '" + endTime + " 23:59:59'");
      jhBarterMoney.append(" and recodeDate < '" + endTime + " 23:59:59'");
      jhdArap.append(" and recodeTime < '" + endTime + " 23:59:59'");
      jhReturnArap.append(" and recodeTime < '" + endTime + " 23:59:59'");
      jhBarterArap.append(" and recodeTime < '" + endTime + " 23:59:59'");
      
      xsdMoney.append(" and recodeDate < '" + endTime + " 23:59:59'");
      xsReturnMoney.append(" and recodeDate < '" + endTime + " 23:59:59'");
      xsBarterMoney.append(" and recodeDate < '" + endTime + " 23:59:59'");
      xsdArap.append(" and recodeTime < '" + endTime + " 23:59:59'");
      xsReturnArap.append(" and recodeTime < '" + endTime + " 23:59:59'");
      xsBarterArap.append(" and recodeTime < '" + endTime + " 23:59:59'");
      
      xsdCost.append(" and b.recodeDate < '" + endTime + " 23:59:59'");
      xsReturnCost.append(" and b.recodeDate < '" + endTime + " 23:59:59'");
      xsBarterOutCost.append(" and b.recodeDate < '" + endTime + " 23:59:59'");
      xsBarterInCost.append(" and b.recodeDate < '" + endTime + " 23:59:59'");
    }
    StringBuffer ssb = new StringBuffer(" SELECT temp.*,(temp.outMoney-temp.costMoney) profit,");
    ssb.append("(" + accounts.toString() + " AND ba.pids LIKE CONCAT('{0}{9}{22}','%') ) otherIn,");
    ssb.append("(" + accounts.toString() + " AND ba.pids LIKE CONCAT('{0}{11}{37}','%') ) costTotal,");
    ssb.append("(" + accounts.toString() + " AND (ba.pids LIKE CONCAT('{0}{1}{3}','%') OR ba.pids LIKE CONCAT('{0}{1}{4}','%')) AND cw.billTypeId IN (4,7,13) ) returenMoney,");
    ssb.append("( (temp.outMoney-temp.costMoney)+(" + accounts.toString() + " AND ba.pids LIKE CONCAT('{0}{9}{22}','%'))-(" + accounts.toString() + " AND ba.pids LIKE CONCAT('{0}{11}{37}','%')) ) periodProfit,");
    ssb.append("staff.* ");
    

    StringBuffer sb = new StringBuffer(" FROM");
    sb.append(" (");
    
    sb.append("SELECT bs.id wStaffId,bs.rank staffRank, bs.fullName staffName,");
    sb.append("( (" + jhdMoney.toString() + ") - (" + jhReturnMoney.toString() + ") + (" + jhBarterMoney.toString() + ") ) inMoney,");
    sb.append("( (" + jhdArap.toString() + ")-(" + jhReturnArap.toString() + ")+(" + jhBarterArap.toString() + ") ) inWaitMoney,");
    sb.append("( (" + xsdMoney.toString() + ") - (" + xsReturnMoney.toString() + ") + (" + xsBarterMoney.toString() + ") ) outMoney,");
    sb.append("( (" + xsdArap.toString() + ")-(" + xsReturnArap.toString() + ")+(" + xsBarterArap.toString() + ") ) outWaitMoney,");
    sb.append("( (" + xsdCost.toString() + ") - (" + xsReturnCost.toString() + ") + ((" + xsBarterOutCost.toString() + ") - (" + xsBarterInCost.toString() + ")) ) costMoney");
    sb.append(" FROM b_staff bs WHERE bs.node=1");
    if ((StringUtils.isNotBlank(staffPrivs)) && (!"*".equals(staffPrivs))) {
      sb.append(" and bs.id in (" + staffPrivs + ") ");
    } else if (StringUtils.isBlank(staffPrivs)) {
      sb.append(" and bs.id = 0 ");
    }
    sb.append(" UNION ALL");
    
    sb.append(" SELECT");
    sb.append(" (0) wStaffId,");
    sb.append("(SELECT MAX(rank)+1 FROM b_staff) staffRank,");
    sb.append("('其他职员') staffName,");
    sb.append("( (" + jhdMoney.toString() + ") - (" + jhReturnMoney.toString() + ") + (" + jhBarterMoney.toString() + ") ) inMoney,");
    sb.append("( (" + jhdArap.toString() + ")-(" + jhReturnArap.toString() + ")+(" + jhBarterArap.toString() + ") ) inWaitMoney,");
    sb.append("( (" + xsdMoney.toString() + ") - (" + xsReturnMoney.toString() + ") + (" + xsBarterMoney.toString() + ") ) outMoney,");
    sb.append("( (" + xsdArap.toString() + ")-(" + xsReturnArap.toString() + ")+(" + xsBarterArap.toString() + ") ) outWaitMoney,");
    sb.append("( (" + xsdCost.toString() + ") - (" + xsReturnCost.toString() + ") + ((" + xsBarterOutCost.toString() + ") - (" + xsBarterInCost.toString() + ")) ) costMoney");
    sb.append(" FROM DUAL");
    

    sb.append(")");
    sb.append(" temp LEFT JOIN  b_staff staff ON temp.wStaffId = staff.id ");
    sb.append(" WHERE 1=1 ");
    

    sb.append(" and (temp.wStaffId = 0 OR ((temp.outMoney-temp.costMoney)+(" + accounts.toString() + " AND ba.pids LIKE CONCAT('{0}{9}{22}','%'))-(" + accounts.toString() + " AND ba.pids LIKE CONCAT('{0}{11}{37}','%')) ) != 0)");
    sb.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("staff", Staff.class);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, ssb.toString(), sb.toString(), alias, new Object[0]);
  }
  
  public List<Model> getListByPids(String configName, String pids)
  {
    String sql = "SELECT * FROM  b_staff WHERE pids LIKE CONCAT('" + pids + "','%')";
    return find(configName, sql);
  }
}
