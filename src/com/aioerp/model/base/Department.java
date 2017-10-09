package com.aioerp.model.base;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.reports.finance.arap.ArapRecords;
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

public class Department
  extends BaseDbModel
{
  public static final Department dao = new Department();
  public static final String TABLE_NAME = "b_department";
  
  public List<Model> getList(String configName, String departmentPrivs)
  {
    String sql = "select * from b_department";
    sql = queryPrivs(sql, departmentPrivs);
    sql = sql + " order by rank ";
    return dao.find(configName, sql);
  }
  
  public List<Model> getAllSorts(String configName, String departmentPrivs)
  {
    String sql = "select * from b_department where node=2";
    sql = queryPrivs(sql, departmentPrivs);
    sql = sql + " order by rank ";
    return dao.find(configName, sql);
  }
  
  public Map<String, Object> getSupPageBySupId(String configName, int pageNum, int numPerPage, int supId, int ObjectId, String listID, String orderField, String orderDirection, String departmentPrivs)
  {
    String sql = "from b_department  where supId = ?";
    sql = queryPrivs(sql, departmentPrivs);
    sql = sql + "  order by " + orderField + " " + orderDirection;
    return aioGetPageByUpObject(configName, dao, listID, ObjectId, pageNum, numPerPage, "select *", sql, null, new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getLastPageBySupId(String configName, int pageNum, int numPerPage, int supId, String listID, String orderField, String orderDirection, String departmentPrivs)
  {
    String sql = "from b_department  where supId = ?";
    sql = queryPrivs(sql, departmentPrivs);
    sql = sql + " order by " + orderField + " " + orderDirection;
    return aioGetLastPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getLastOptionBySupId(String configName, int pageNum, int numPerPage, int supId, String listID, String orderField, String orderDirection, String departmentPrivs)
  {
    String sql = "from b_department  where supId = ?";
    sql = queryPrivs(sql, departmentPrivs);
    sql = sql + " and status = 2";
    sql = sql + " order by " + orderField + " " + orderDirection;
    return aioGetLastPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getPageBySupId(String configName, int pageNum, int numPerPage, int supId, String listID, String orderField, String orderDirection, String departmentPrivs)
  {
    String sql = "from b_department  where supId = ? ";
    sql = queryPrivs(sql, departmentPrivs);
    sql = sql + " order by " + orderField + " " + orderDirection;
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[] { Integer.valueOf(supId) });
  }
  
  public String getChildIdsBySupId(String configName, int supId)
  {
    String str = "";
    List<Model> modelList = dao.find(configName, "select id from b_department where pids like '%{" + supId + "}%' and status=" + AioConstants.STATUS_ENABLE);
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
    List<Model> modelList = dao.find(configName, "select id from b_department where pids like '%{" + supId + "}%'");
    if ((modelList != null) && (modelList.size() > 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public String getAllChildIdsNoSupId(String configName, Integer supId)
  {
    String str = "";
    List<Model> modelList = dao.find(configName, "select id from b_department where pids not like '%{" + supId + "}%'");
    if ((modelList != null) && (modelList.size() > 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public int getPsupId(String configName, int sid)
  {
    if (sid <= 0) {
      return 0;
    }
    return Db.use(configName).queryInt("select supId from b_department where id=?", new Object[] { Integer.valueOf(sid) }).intValue();
  }
  
  public boolean hasChildren(String configName, int supId)
  {
    return Db.use(configName).queryLong("select count(id) from b_department where supId=?", new Object[] { Integer.valueOf(supId) }).longValue() > 0L;
  }
  
  public Map<String, Object> getPage(String configName, String listId, int pageNum, int numPerPage, String departmentPrivs)
  {
    String sql = "from b_department where 1=1";
    sql = queryPrivs(sql, departmentPrivs);
    sql = sql + "  order by rank asc";
    return aioGetPageRecord(configName, dao, listId, pageNum, numPerPage, "select *", sql, new Object[0]);
  }
  
  public Map<String, Object> getSupPage(String configName, int pageNum, int numPerPage, Integer supId, int ObjectId, String listID, String orderField, String orderDirection, Map<String, Object> pars, String departmentPrivs)
  {
    StringBuffer sql = new StringBuffer(" from b_department where 1=1 ");
    queryPrivs(sql, departmentPrivs);
    List<Object> paras = new ArrayList();
    if (pars != null)
    {
      if ((pars.get("fullName") != null) && (!"".equals(pars.get("fullName"))))
      {
        sql.append(" and fullName like ?");
        paras.add("%" + pars.get("fullName") + "%");
      }
      if ((pars.get("code") != null) && (!"".equals(pars.get("code"))))
      {
        sql.append(" and code like ?");
        paras.add("%" + pars.get("code") + "%");
      }
      if (pars.get("supId") != null)
      {
        sql.append(" and supId = ?");
        paras.add(pars.get("supId"));
      }
    }
    if (ObjectId > 0)
    {
      sql.append(" and supId = ?");
      paras.add(supId);
    }
    sql.append(" and status = ?");
    paras.add(Integer.valueOf(AioConstants.STATUS_ENABLE));
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by rank asc ");
    }
    return aioGetPageByUpObject(configName, dao, "", ObjectId, pageNum, numPerPage, "select *", sql.toString(), null, paras.toArray());
  }
  
  public Map<String, Object> getPageByTerm(String configName, int pageNum, int pageSize, String term, String val, boolean inAll, int supId, int node, String listID, String orderField, String orderDirection, String departmentPrivs)
  {
    StringBuffer sql = new StringBuffer(" from b_department where 1=1");
    queryPrivs(sql, departmentPrivs);
    List<Object> paras = new ArrayList();
    if (StringUtils.isNotBlank(val))
    {
      String[] terms = term.split("#");
      String[] vals = val.split("#");
      if (inAll)
      {
        if ("status".equals(terms[(terms.length - 1)]))
        {
          int statusVal = StringUtil.convertToInt(vals[(vals.length - 1)].trim());
          if (statusVal != 3)
          {
            sql.append(" and " + terms[(terms.length - 1)] + " like ?");
            paras.add("%" + statusVal + "%");
          }
        }
        else
        {
          sql.append(" and " + terms[(terms.length - 1)] + " like ?");
          paras.add("%" + vals[(vals.length - 1)].trim() + "%");
        }
      }
      else {
        for (int i = 0; i < terms.length; i++) {
          if ("status".equals(terms[i]))
          {
            int statusVal = StringUtil.convertToInt(vals[i].trim());
            if (statusVal != 3)
            {
              sql.append(" and " + terms[i] + " like ?");
              paras.add("%" + statusVal + "%");
            }
          }
          else
          {
            sql.append(" and " + terms[i] + " like ?");
            paras.add("%" + vals[i].trim() + "%");
          }
        }
      }
    }
    if (supId >= 0) {
      if (node != 0)
      {
        sql.append(" and node = ?");
        paras.add(Integer.valueOf(node));
        sql.append(" and pids like ?");
        paras.add("%" + supId + "%");
      }
      else
      {
        sql.append(" and supId like ?");
        paras.add(Integer.valueOf(supId));
      }
    }
    sql.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageRecord(configName, dao, listID, pageNum, pageSize, "select *", sql.toString(), paras.toArray());
  }
  
  public Map<String, Object> getListPageBySupId(String configName, int pageNum, int pageSize, int supId, String listID, String orderField, String orderDirection, String departmentPrivs)
  {
    String sql = "from b_department where node=1  and pids like ?";
    sql = queryPrivs(sql, departmentPrivs);
    sql = sql + " order by " + orderField + " " + orderDirection;
    return aioGetPageRecord(configName, dao, listID, pageNum, pageSize, "select *", sql, new Object[] { "%{" + supId + "}%" });
  }
  
  public Map<String, Object> getPage(String configName, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars, int supId, String departmentPrivs)
  {
    StringBuffer sql = new StringBuffer(" from b_department where 1=1 ");
    queryPrivs(sql, departmentPrivs);
    List<Object> paras = new ArrayList();
    if ((pars != null) && (!pars.isEmpty()))
    {
      if ((pars.get("termVal") != null) && (!"".equals(pars.get("termVal"))))
      {
        String term = pars.get("term").toString();
        Object val = pars.get("termVal");
        if ("quick".equals(term))
        {
          sql.append(" and ( code like ?");
          paras.add("%" + val + "%");
          sql.append(" or fullName like ?");
          paras.add("%" + val + "%");
          sql.append(" or spell like ? )");
          paras.add("%" + val + "%");
        }
        else
        {
          sql.append(" and " + term + " like ?");
          paras.add("%" + val + "%");
        }
        sql.append(" and node = " + AioConstants.NODE_1);
      }
    }
    else
    {
      sql.append(" and supId = ?");
      paras.add(Integer.valueOf(supId));
    }
    sql.append(" and status = ? ");
    paras.add(Integer.valueOf(AioConstants.STATUS_ENABLE));
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by rank asc ");
    }
    return aioGetPageRecord(configName, dao, listId, pageNum, numPerPage, "select *", sql.toString(), paras.toArray());
  }
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from b_department where `code`=? and `id`<>?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public boolean verify(String configName, String attr, Integer id)
  {
    if (id == null) {
      return true;
    }
    StringBuffer sql = new StringBuffer("select depmId from b_staff where depmId = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from bb_billhistory where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from bb_businessdraft where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from cw_pay_type where " + attr + " = " + id);
    
    sql.append(" limit 1");
    id = Db.use(configName).queryInt(sql.toString());
    return (id != null) && (id.intValue() > 0);
  }
  
  public Object getPageByTerms(String configName, String listID, int pageNum, int numPerPage, String term, String val, int supId, int node, String orderField, String orderDirection, String departmentPrivs)
  {
    StringBuilder sql = new StringBuilder("from b_department where 1=1");
    queryPrivs(sql, departmentPrivs);
    List<Object> paras = new ArrayList();
    if ((!val.equals("")) && (val != null))
    {
      if (term.equals("quick"))
      {
        sql.append(" and (code like ?");
        paras.add("%" + val + "%");
        sql.append(" or fullName like ?");
        paras.add("%" + val + "%");
        int statusVal = StringUtil.convertToInt(val.trim());
        if (statusVal != 3)
        {
          sql.append(" or status = ? )");
          paras.add(Integer.valueOf(statusVal));
        }
        else
        {
          sql.append(" or status <> ? )");
          paras.add(Integer.valueOf(0));
        }
      }
      if (term.equals("code"))
      {
        sql.append(" and code like ?");
        paras.add("%" + val + "%");
      }
      if (term.equals("name"))
      {
        sql.append(" and fullName like ?");
        paras.add("%" + val + "%");
      }
      if (term.equals("status"))
      {
        int statusVal = StringUtil.convertToInt(val.trim());
        if (statusVal != 3)
        {
          sql.append(" and status = ?");
          paras.add(Integer.valueOf(statusVal));
        }
      }
    }
    if (supId >= 0)
    {
      if (node != 0)
      {
        sql.append(" and node = ?");
        paras.add(Integer.valueOf(node));
      }
      sql.append(" and pids like ?");
      paras.add("%" + supId + "%");
    }
    sql.append(" order by " + orderField + " " + orderDirection);
    
    return dao.aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql.toString(), paras.toArray());
  }
  
  public Map<String, Object> getDeptArapPage(String configName, String deptPrivs, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, int supId, Integer ObjectId, String startTime, String endTime, int node)
    throws SQLException, Exception
  {
    StringBuffer arapOccur = new StringBuffer("SELECT IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0) FROM cw_arap_records arap LEFT JOIN b_department b ON b.id=arap.departmentId WHERE b.pids LIKE CONCAT(bt.pids,'%')");
    StringBuffer arapPyOccur = new StringBuffer("SELECT IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0) FROM cw_arap_records arap LEFT JOIN b_department b ON b.id=arap.departmentId WHERE b.pids LIKE CONCAT(bt.pids,'%')");
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
    StringBuffer ssb = new StringBuffer(" SELECT temp.*,dept.* ");
    StringBuffer sb = new StringBuffer(" FROM");
    sb.append(" (");
    
    sb.append("SELECT  bt.id deptId,bt.supId deptSupId,bt.rank deptRank, bt.fullName deptName, bt.node deptNode, bt.pids deptPids,");
    sb.append("(CASE WHEN (" + arapOccur.toString() + ")<0 THEN ABS((" + arapOccur.toString() + ")) ELSE 0 END) getsOccur,");
    sb.append("(CASE WHEN (" + arapOccur.toString() + ")>0 THEN ABS((" + arapOccur.toString() + ")) ELSE 0 END) paysOccur,");
    sb.append("(CASE WHEN (" + arapPyOccur.toString() + ")<0 THEN ABS((" + arapPyOccur.toString() + ")) ELSE 0 END) getsPyOccur,");
    sb.append("(CASE WHEN (" + arapPyOccur.toString() + ")>0 THEN ABS((" + arapPyOccur.toString() + ")) ELSE 0 END) paysPyOccur");
    sb.append(" FROM b_department bt");
    
    sb.append(" UNION ALL");
    
    sb.append(" SELECT");
    sb.append(" (CASE WHEN arap.departmentId IS NULL THEN 0 END) deptId,");
    sb.append("(CASE WHEN arap.departmentId IS NULL THEN 0 END) deptSupId,");
    sb.append("(CASE WHEN arap.departmentId IS NULL THEN (SELECT MAX(rank)+1 FROM b_department) END) deptRank,");
    sb.append("(CASE WHEN arap.departmentId IS NULL THEN '其他部门' END) deptName,");
    sb.append("(CASE WHEN arap.departmentId IS NULL THEN 1 END) deptNode,");
    sb.append("(CASE WHEN arap.departmentId IS NULL THEN '{0}' END) deptPids,");
    sb.append("(CASE WHEN (IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0))<0 THEN ABS(IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0)) ELSE 0 END ) getsOccur,");
    sb.append("(CASE WHEN (IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0))>0 THEN ABS(IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0)) ELSE 0 END ) paysOccur,");
    sb.append("(CASE WHEN (IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0))<0 THEN ABS(IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0)) ELSE 0 END ) getsPyOccur,");
    sb.append("(CASE WHEN (IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0))>0 THEN ABS(IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0)) ELSE 0 END ) paysPyOccur");
    sb.append(" FROM cw_arap_records arap WHERE arap.departmentId IS NULL");
    if (StringUtils.isNotBlank(startTime)) {
      sb.append(" and arap.recodeTime >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      sb.append(" and arap.recodeTime < '" + endTime + " 23:59:59'");
    }
    sb.append(")");
    sb.append(" temp LEFT JOIN b_department dept ON temp.deptId = dept.id WHERE 1=1");
    if ((StringUtils.isNotBlank(deptPrivs)) && (!"*".equals(deptPrivs))) {
      sb.append(" and dept.id in (" + deptPrivs + ") ");
    } else if (StringUtils.isBlank(deptPrivs)) {
      sb.append(" and dept.id = 0 ");
    }
    if (node == 0)
    {
      sb.append(" AND deptSupId=" + supId);
    }
    else if (node == 1)
    {
      sb.append(" AND deptNode = 1 ");
      sb.append(" AND deptPids LIKE '%" + supId + "%' ");
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("dept", Department.class);
    if ((ObjectId != null) && (ObjectId.intValue() > 0)) {
      return aioGetPageManyByUpObject(configName, "dept", "id", listId, ObjectId.intValue(), pageNum, numPerPage, ssb.toString(), sb.toString(), null, alias, new Object[0]);
    }
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, ssb.toString(), sb.toString(), alias, new Object[0]);
  }
  
  public Map<String, Object> getUnitArapPageByDept(String configName, String privs, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, String startTime, String endTime, String pids, String alias)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("arap", ArapRecords.class);
    
    StringBuffer selectSql = new StringBuffer("SELECT arap.*," + alias + ".*");
    
    StringBuffer sb = new StringBuffer(" FROM cw_arap_records arap");
    if (alias.equals("bu"))
    {
      selectSql.append(",(CASE WHEN bu.id IS NULL THEN '其他单位' ELSE bu.fullName END) unitName");
      map.put("bu", Unit.class);
      sb.append(" LEFT JOIN b_unit bu ON bu.id=arap.unitId");
    }
    else if (alias.equals("bs"))
    {
      selectSql.append(",(CASE WHEN bs.id IS NULL THEN '其他职员' ELSE bs.fullName END) staffName");
      map.put("bs", Staff.class);
      sb.append(" LEFT JOIN b_staff bs ON bs.id=arap.staffId");
    }
    sb.append(" WHERE 1=1");
    if (alias.equals("bu"))
    {
      if ((StringUtils.isNotBlank(privs)) && (!"*".equals(privs))) {
        sb.append(" and bu.id in (" + privs + ") ");
      } else if (StringUtils.isBlank(privs)) {
        sb.append(" and bu.id = 0 ");
      }
    }
    else if (alias.equals("bs")) {
      if ((StringUtils.isNotBlank(privs)) && (!"*".equals(privs))) {
        sb.append(" and bs.id in (" + privs + ") ");
      } else if (StringUtils.isBlank(privs)) {
        sb.append(" and bs.id = 0 ");
      }
    }
    if (StringUtils.isNotBlank(startTime)) {
      sb.append(" and arap.recodeTime >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      sb.append(" and arap.recodeTime < '" + endTime + " 23:59:59'");
    }
    if (pids == null) {
      sb.append(" AND arap.departmentId IS NULL");
    } else {
      sb.append(" AND arap.departmentId IN (SELECT id FROM b_department WHERE pids LIKE CONCAT('" + pids + "','%')) ");
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, selectSql.toString(), sb.toString(), map, new Object[0]);
  }
  
  public Map<String, Object> getDeptBusCountPage(String configName, String depmPrivs, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, String startTime, String endTime)
    throws SQLException, Exception
  {
    StringBuffer accounts = new StringBuffer("SELECT IFNULL(SUM(CASE WHEN cw.type=0 THEN  cw.payMoney ELSE cw.payMoney*-1 END),0) FROM cw_pay_type cw LEFT JOIN b_accounts ba ON cw.accountId=ba.id WHERE 1=1 AND IFNULL(cw.departmentId,0)=temp.wDeptId");
    
    StringBuffer jhdMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory WHERE billTypeId=5 AND IFNULL(departmentId,0)=wDeptId");
    StringBuffer jhReturnMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory WHERE billTypeId=6 AND IFNULL(departmentId,0)=wDeptId");
    StringBuffer jhBarterMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory WHERE billTypeId=12 AND IFNULL(departmentId,0)=wDeptId");
    
    StringBuffer jhdArap = new StringBuffer("SELECT IFNULL(SUM(subMoney),0) FROM cw_arap_records WHERE billTypeId=5 AND IFNULL(departmentId,0)=wDeptId");
    StringBuffer jhReturnArap = new StringBuffer("SELECT IFNULL(SUM(addMoney),0) FROM cw_arap_records WHERE billTypeId=6 AND IFNULL(departmentId,0)=wDeptId");
    StringBuffer jhBarterArap = new StringBuffer("SELECT IFNULL(SUM(subMoney),0) FROM cw_arap_records WHERE billTypeId=12 AND IFNULL(departmentId,0)=wDeptId");
    

    StringBuffer xsdMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory  WHERE billTypeId=4 AND IFNULL(departmentId,0)=wDeptId");
    StringBuffer xsReturnMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory WHERE billTypeId=7 AND IFNULL(departmentId,0)=wDeptId");
    StringBuffer xsBarterMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory WHERE billTypeId=13 AND IFNULL(departmentId,0)=wDeptId");
    
    StringBuffer xsdArap = new StringBuffer("SELECT IFNULL(SUM(addMoney),0) FROM cw_arap_records WHERE billTypeId=4 AND IFNULL(departmentId,0)=wDeptId");
    StringBuffer xsReturnArap = new StringBuffer("SELECT IFNULL(SUM(subMoney),0) FROM cw_arap_records WHERE billTypeId=7 AND IFNULL(departmentId,0)=wDeptId");
    StringBuffer xsBarterArap = new StringBuffer("SELECT IFNULL(SUM(addMoney),0) FROM cw_arap_records WHERE billTypeId=13 AND IFNULL(departmentId,0)=wDeptId");
    

    StringBuffer xsdCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_sell_detail d LEFT JOIN xs_sell_bill b ON d.billId = b.id WHERE IFNULL(departmentId,0)=wDeptId");
    StringBuffer xsReturnCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_return_detail d LEFT JOIN xs_return_bill b ON d.billId = b.id WHERE IFNULL(departmentId,0)=wDeptId");
    StringBuffer xsBarterOutCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_barter_detail d LEFT JOIN xs_barter_bill b ON d.billId = b.id WHERE IFNULL(departmentId,0)=wDeptId AND d.type=2");
    StringBuffer xsBarterInCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_barter_detail d LEFT JOIN xs_barter_bill b ON d.billId = b.id WHERE IFNULL(departmentId,0)=wDeptId AND d.type=1");
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
    ssb.append("dept.* ");
    

    StringBuffer sb = new StringBuffer(" FROM");
    sb.append(" (");
    
    sb.append("SELECT bd.id wDeptId,bd.rank deptRank, bd.fullName deptName,");
    sb.append("( (" + jhdMoney.toString() + ") - (" + jhReturnMoney.toString() + ") + (" + jhBarterMoney.toString() + ") ) inMoney,");
    sb.append("( (" + jhdArap.toString() + ")-(" + jhReturnArap.toString() + ")+(" + jhBarterArap.toString() + ") ) inWaitMoney,");
    sb.append("( (" + xsdMoney.toString() + ") - (" + xsReturnMoney.toString() + ") + (" + xsBarterMoney.toString() + ") ) outMoney,");
    sb.append("( (" + xsdArap.toString() + ")-(" + xsReturnArap.toString() + ")+(" + xsBarterArap.toString() + ") ) outWaitMoney,");
    sb.append("( (" + xsdCost.toString() + ") - (" + xsReturnCost.toString() + ") + ((" + xsBarterOutCost.toString() + ") - (" + xsBarterInCost.toString() + ")) ) costMoney");
    sb.append(" FROM b_department bd WHERE bd.node=1");
    if ((StringUtils.isNotBlank(depmPrivs)) && (!"*".equals(depmPrivs))) {
      sb.append(" and bd.id in (" + depmPrivs + ") ");
    } else if (StringUtils.isBlank(depmPrivs)) {
      sb.append(" and bd.id = 0 ");
    }
    sb.append(" UNION ALL");
    
    sb.append(" SELECT");
    sb.append(" (0) wDeptId,");
    sb.append("(SELECT MAX(rank)+1 FROM b_department) deptRank,");
    sb.append("('其他部门') deptName,");
    sb.append("( (" + jhdMoney.toString() + ") - (" + jhReturnMoney.toString() + ") + (" + jhBarterMoney.toString() + ") ) inMoney,");
    sb.append("( (" + jhdArap.toString() + ")-(" + jhReturnArap.toString() + ")+(" + jhBarterArap.toString() + ") ) inWaitMoney,");
    sb.append("( (" + xsdMoney.toString() + ") - (" + xsReturnMoney.toString() + ") + (" + xsBarterMoney.toString() + ") ) outMoney,");
    sb.append("( (" + xsdArap.toString() + ")-(" + xsReturnArap.toString() + ")+(" + xsBarterArap.toString() + ") ) outWaitMoney,");
    sb.append("( (" + xsdCost.toString() + ") - (" + xsReturnCost.toString() + ") + ((" + xsBarterOutCost.toString() + ") - (" + xsBarterInCost.toString() + ")) ) costMoney");
    sb.append(" FROM DUAL");
    

    sb.append(")");
    sb.append(" temp LEFT JOIN  b_department dept ON temp.wDeptId = dept.id ");
    sb.append(" WHERE 1=1 ");
    


    sb.append(" and (temp.wDeptId = 0 OR ((temp.outMoney-temp.costMoney)+(" + accounts.toString() + " AND ba.pids LIKE CONCAT('{0}{9}{22}','%'))-(" + accounts.toString() + " AND ba.pids LIKE CONCAT('{0}{11}{37}','%')) ) != 0)");
    sb.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("dept", Department.class);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, ssb.toString(), sb.toString(), alias, new Object[0]);
  }
  
  private String queryPrivs(String sql, String departmentPrivs)
  {
    if ((StringUtils.isNotBlank(departmentPrivs)) && (!"*".equals(departmentPrivs))) {
      sql = sql + " and id in(" + departmentPrivs + ")";
    } else if (StringUtils.isBlank(departmentPrivs)) {
      sql = sql + " and id =0";
    }
    return sql;
  }
  
  private void queryPrivs(StringBuffer sql, String departmentPrivs)
  {
    if ((StringUtils.isNotBlank(departmentPrivs)) && (!"*".equals(departmentPrivs))) {
      sql.append(" and id in(" + departmentPrivs + ")");
    } else if (StringUtils.isBlank(departmentPrivs)) {
      sql.append(" and id =0");
    }
  }
  
  private void queryPrivs(StringBuilder sql, String departmentPrivs)
  {
    if ((StringUtils.isNotBlank(departmentPrivs)) && (!"*".equals(departmentPrivs))) {
      sql.append(" and id in(" + departmentPrivs + ")");
    } else if (StringUtils.isBlank(departmentPrivs)) {
      sql.append(" and id =0");
    }
  }
  
  public List<Model> getListByPids(String configName, String pids)
  {
    String sql = "SELECT * FROM  b_department WHERE pids LIKE CONCAT('" + pids + "','%')";
    return find(configName, sql);
  }
}
