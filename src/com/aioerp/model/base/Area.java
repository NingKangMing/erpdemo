package com.aioerp.model.base;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.reports.finance.arap.ArapRecords;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class Area
  extends BaseDbModel
{
  public static Area dao = new Area();
  public static final String TABLE_NAME = "b_area";
  
  public Integer getIdByUnitId(String configName, Integer unitId)
  {
    String sql = "SELECT areaId FROM b_unit WHERE id = ? ";
    return Db.use(configName).findFirst(sql, new Object[] { unitId }).getInt("areaId");
  }
  
  public Map<String, Object> getPageBySupId(String configName, int pageNum, int numPerPage, int supId, String listID, String orderField, String orderDirection, String areaPrivs)
  {
    String sql = "from b_area  where supId = ?";
    if ((listID == null) || ("".equals(listID))) {
      sql = sql + " and status = 2 ";
    }
    if ((StringUtils.isNotBlank(areaPrivs)) && (!"*".equals(areaPrivs))) {
      sql = sql + " and id in(" + areaPrivs + ")";
    } else if (StringUtils.isBlank(areaPrivs)) {
      sql = sql + " and id =0";
    }
    sql = sql + " order by " + orderField + " " + orderDirection;
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getLastPageBySupId(String configName, int pageNum, int numPerPage, int supId, String listID, String orderField, String orderDirection, String areaPrivs)
  {
    String sql = "from b_area  where supId = ?";
    if ((StringUtils.isNotBlank(areaPrivs)) && (!"*".equals(areaPrivs))) {
      sql = sql + " and id in(" + areaPrivs + ")";
    } else if (StringUtils.isBlank(areaPrivs)) {
      sql = sql + " and id =0";
    }
    sql = sql + " order by " + orderField + " " + orderDirection;
    
    return aioGetLastPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[] { Integer.valueOf(supId) });
  }
  
  public Map<String, Object> getSupPageBySupId(String configName, int pageNum, int numPerPage, int supId, int ObjectId, String listID, String orderField, String orderDirection, String areaPrivs)
  {
    String sql = "from b_area  where supId = ?";
    if ((listID == null) || ("".equals(listID))) {
      sql = sql + " and status = 2 ";
    }
    if ((StringUtils.isNotBlank(areaPrivs)) && (!"*".equals(areaPrivs))) {
      sql = sql + " and id in(" + areaPrivs + ")";
    } else if (StringUtils.isBlank(areaPrivs)) {
      sql = sql + " and id =0";
    }
    sql = sql + " order by " + orderField + " " + orderDirection;
    return aioGetPageByUpObject(configName, dao, listID, ObjectId, pageNum, numPerPage, "select *", sql, null, new Object[] { Integer.valueOf(supId) });
  }
  
  public String getChildIdsBySupId(String configName, int supId)
  {
    String str = "";
    List<Model> modelList = dao.find(configName, "select id from b_area where pids like '%{" + supId + "}%' and status=" + AioConstants.STATUS_ENABLE);
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
    List<Model> modelList = dao.find(configName, "select id from b_area where pids like '%{" + supId + "}%'");
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
    List<Model> modelList = dao.find(configName, "select id from b_area where pids not like '%{" + supId + "}%'");
    if ((modelList != null) && (modelList.size() > 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public List<Model> getAllSorts(String configName, String areaPrivs)
  {
    String sql = "select * from b_area where node=2";
    if ((StringUtils.isNotBlank(areaPrivs)) && (!"*".equals(areaPrivs))) {
      sql = sql + " and id in(" + areaPrivs + ")";
    } else if (StringUtils.isBlank(areaPrivs)) {
      sql = sql + " and id =0";
    }
    sql = sql + " order by rank ";
    return dao.find(configName, "select * from b_area where node=2 order by rank ");
  }
  
  public Map<String, Object> getListPageBySupId(String configName, int pageNum, int pageSize, int supId, String listID, String orderField, String orderDirection, String areaPrivs)
  {
    String sql = "from b_area where node=1  and pids like ?";
    if ((StringUtils.isNotBlank(areaPrivs)) && (!"*".equals(areaPrivs))) {
      sql = sql + " and id in(" + areaPrivs + ")";
    } else if (StringUtils.isBlank(areaPrivs)) {
      sql = sql + " and id =0";
    }
    sql = sql + " order by " + orderField + " " + orderDirection;
    
    return aioGetPageRecord(configName, dao, listID, pageNum, pageSize, "select *", sql, new Object[] { "%{" + supId + "}%" });
  }
  
  public boolean hasChildren(String configName, int supId)
  {
    return Db.use(configName).queryLong("select count(id) from b_area where supId=?", new Object[] { Integer.valueOf(supId) }).longValue() > 0L;
  }
  
  public boolean verify(String configName, String attr, Integer id)
  {
    if (id == null) {
      return true;
    }
    StringBuffer sql = new StringBuffer("select " + attr + " from b_unit where " + attr + " = " + id);
    
    sql.append(" limit 1");
    id = Db.use(configName).queryInt(sql.toString());
    return (id != null) && (id.intValue() > 0);
  }
  
  public Boolean codeIsExist(String configName, String code, int id)
  {
    return Boolean.valueOf(Db.use(configName).queryLong("SELECT count(code) FROM b_area WHERE code = ?  and id <> ? ", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L);
  }
  
  public int getPsupId(String configName, int sid)
  {
    if (sid <= 0) {
      return 0;
    }
    return Db.use(configName).queryInt("select supId from b_area where id=?", new Object[] { Integer.valueOf(sid) }).intValue();
  }
  
  public Map<String, Object> getPageByTerm(String configName, int pageNum, int pageSize, String term, String val, boolean inAll, int supId, int node, String listID, String orderField, String orderDirection, String areaPrivs)
  {
    StringBuffer sql = new StringBuffer(" from b_area where 1=1");
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
    if ((StringUtils.isNotBlank(areaPrivs)) && (!"*".equals(areaPrivs))) {
      sql.append("and id in(" + areaPrivs + ")");
    } else if (StringUtils.isBlank(areaPrivs)) {
      sql.append(" and id =0");
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
    sql.append("  " + orderField + " " + orderDirection);
    
    return aioGetPageRecord(configName, dao, listID, pageNum, pageSize, "select *", sql.toString(), paras.toArray());
  }
  
  public Map<String, Object> getPageDialogByTerms(String configName, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars, String areaPrivs)
  {
    StringBuilder sql = new StringBuilder("from b_area where 1=1");
    sql.append("  and status = 2 ");
    List<Object> paras = new ArrayList();
    if (pars != null)
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
        sql.append(" and node =? ");
        paras.add(Integer.valueOf(AioConstants.NODE_1));
      }
    }
    else {
      sql.append(" and supId = 0");
    }
    sql.append(" and status =? ");
    paras.add(Integer.valueOf(AioConstants.STATUS_ENABLE));
    if ((StringUtils.isNotBlank(areaPrivs)) && (!"*".equals(areaPrivs))) {
      sql.append("and id in(" + areaPrivs + ")");
    } else if (StringUtils.isBlank(areaPrivs)) {
      sql.append(" and id =0");
    }
    sql.append(" order by " + orderField + " " + orderDirection);
    
    return dao.aioGetPageRecord(configName, dao, "", pageNum, numPerPage, "select *", sql.toString(), paras.toArray());
  }
  
  public Object getPageByTerms(String configName, String listID, int pageNum, int numPerPage, String term, String val, int supId, int node, String orderField, String orderDirection, String areaPrivs)
  {
    StringBuilder sql = new StringBuilder("from b_area where 1=1");
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
    if ((StringUtils.isNotBlank(areaPrivs)) && (!"*".equals(areaPrivs))) {
      sql.append("and id in(" + areaPrivs + ")");
    } else if (StringUtils.isBlank(areaPrivs)) {
      sql.append(" and id =0");
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
  
  public Map<String, Object> getAreaArapPage(String configName, String areaPrivs, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, int supId, Integer ObjectId, String startTime, String endTime, int node)
    throws SQLException, Exception
  {
    StringBuffer arapOccur = new StringBuffer("SELECT IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0) FROM cw_arap_records arap LEFT JOIN b_area b ON b.id=arap.areaId WHERE b.pids LIKE CONCAT(ba.pids,'%')");
    StringBuffer arapPyOccur = new StringBuffer("SELECT IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0) FROM cw_arap_records arap LEFT JOIN b_area b ON b.id=arap.areaId WHERE b.pids LIKE CONCAT(ba.pids,'%')");
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
    StringBuffer ssb = new StringBuffer(" SELECT temp.*,area.* ");
    StringBuffer sb = new StringBuffer(" FROM");
    sb.append(" (");
    
    sb.append("SELECT  ba.id areaId,ba.supId areaSupId,ba.rank areaRank,ba.fullName areaName,ba.node areaNode,ba.pids areaPids,");
    sb.append("(CASE WHEN (" + arapOccur.toString() + ")<0 THEN ABS((" + arapOccur.toString() + ")) ELSE 0 END) getsOccur,");
    sb.append("(CASE WHEN (" + arapOccur.toString() + ")>0 THEN ABS((" + arapOccur.toString() + ")) ELSE 0 END) paysOccur,");
    sb.append("(CASE WHEN (" + arapPyOccur.toString() + ")<0 THEN ABS((" + arapPyOccur.toString() + ")) ELSE 0 END) getsPyOccur,");
    sb.append("(CASE WHEN (" + arapPyOccur.toString() + ")>0 THEN ABS((" + arapPyOccur.toString() + ")) ELSE 0 END) paysPyOccur");
    sb.append(" FROM b_area ba");
    
    sb.append(" UNION ALL");
    
    sb.append(" SELECT");
    sb.append(" (CASE WHEN arap.areaId IS NULL THEN 0 END) areaId,");
    sb.append("(CASE WHEN arap.areaId IS NULL THEN 0 END) areaSupId,");
    sb.append("(CASE WHEN arap.areaId IS NULL THEN (SELECT MAX(rank)+1 FROM b_area) END) areaRank,");
    sb.append("(CASE WHEN arap.areaId IS NULL THEN '其他地区' END) areaName,");
    sb.append("(CASE WHEN arap.areaId IS NULL THEN 1 END) areaNode,");
    sb.append("(CASE WHEN arap.areaId IS NULL THEN '{0}' END) areaPids,");
    sb.append("(CASE WHEN (IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0))<0 THEN ABS(IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0)) ELSE 0 END ) getsOccur,");
    sb.append("(CASE WHEN (IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0))>0 THEN ABS(IFNULL(SUM(subMoney),0)-IFNULL(SUM(addMoney),0)) ELSE 0 END ) paysOccur,");
    sb.append("(CASE WHEN (IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0))<0 THEN ABS(IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0)) ELSE 0 END ) getsPyOccur,");
    sb.append("(CASE WHEN (IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0))>0 THEN ABS(IFNULL(SUM(addPrepay),0)-IFNULL(SUM(subPrepay),0)) ELSE 0 END ) paysPyOccur");
    sb.append(" FROM cw_arap_records arap WHERE arap.areaId IS NULL");
    if (StringUtils.isNotBlank(startTime)) {
      sb.append(" and arap.recodeTime >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      sb.append(" and arap.recodeTime < '" + endTime + " 23:59:59'");
    }
    sb.append(")");
    sb.append(" temp LEFT JOIN b_area area ON temp.areaId = area.id WHERE 1=1");
    if ((StringUtils.isNotBlank(areaPrivs)) && (!"*".equals(areaPrivs))) {
      sb.append(" and area.id in (" + areaPrivs + ") ");
    } else if (StringUtils.isBlank(areaPrivs)) {
      sb.append(" and area.id = 0 ");
    }
    if (node == 0)
    {
      sb.append(" AND areaSupId=" + supId);
    }
    else if (node == 1)
    {
      sb.append(" AND areaNode = 1 ");
      sb.append(" AND areaPids LIKE '%" + supId + "%' ");
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("area", Area.class);
    if ((ObjectId != null) && (ObjectId.intValue() > 0)) {
      return aioGetPageManyByUpObject(configName, "area", "id", listId, ObjectId.intValue(), pageNum, numPerPage, ssb.toString(), sb.toString(), null, alias, new Object[0]);
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
      sb.append(" AND arap.areaId IS NULL");
    } else {
      sb.append(" AND arap.areaId IN (SELECT id FROM b_area WHERE pids LIKE CONCAT('" + pids + "','%')) ");
    }
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, selectSql.toString(), sb.toString(), map, new Object[0]);
  }
  
  public Map<String, Object> getAreaBusCountPage(String configName, String areaPrivs, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, String startTime, String endTime)
    throws SQLException, Exception
  {
    StringBuffer accounts = new StringBuffer("SELECT IFNULL(SUM(CASE WHEN cw.type=0 THEN  cw.payMoney ELSE cw.payMoney*-1 END),0) FROM cw_pay_type cw LEFT JOIN b_accounts ba ON cw.accountId=ba.id LEFT JOIN b_unit bu ON cw.unitId=bu.id WHERE 1=1 AND IFNULL(bu.areaId,0)=temp.wareaId");
    
    StringBuffer jhdMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory bhy LEFT JOIN b_unit bu ON bhy.unitId=bu.id WHERE bhy.billTypeId=5 AND IFNULL(bu.areaId,0)=wareaId");
    StringBuffer jhReturnMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory bhy LEFT JOIN b_unit bu ON bhy.unitId=bu.id WHERE bhy.billTypeId=6 AND IFNULL(bu.areaId,0)=wareaId");
    StringBuffer jhBarterMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory bhy LEFT JOIN b_unit bu ON bhy.unitId=bu.id WHERE bhy.billTypeId=12 AND IFNULL(bu.areaId,0)=wareaId");
    
    StringBuffer jhdArap = new StringBuffer("SELECT IFNULL(SUM(subMoney),0) FROM cw_arap_records cw LEFT JOIN b_unit bu ON cw.unitId=bu.id WHERE cw.billTypeId=5 AND IFNULL(bu.areaId,0)=wareaId");
    StringBuffer jhReturnArap = new StringBuffer("SELECT IFNULL(SUM(addMoney),0) FROM cw_arap_records cw LEFT JOIN b_unit bu ON cw.unitId=bu.id WHERE cw.billTypeId=6 AND IFNULL(bu.areaId,0)=wareaId");
    StringBuffer jhBarterArap = new StringBuffer("SELECT IFNULL(SUM(subMoney),0) FROM cw_arap_records cw LEFT JOIN b_unit bu ON cw.unitId=bu.id WHERE cw.billTypeId=12 AND IFNULL(bu.areaId,0)=wareaId");
    

    StringBuffer xsdMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory bhy LEFT JOIN b_unit bu ON bhy.unitId=bu.id WHERE bhy.billTypeId=4 AND IFNULL(bu.areaId,0)=wareaId");
    StringBuffer xsReturnMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory bhy LEFT JOIN b_unit bu ON bhy.unitId=bu.id WHERE bhy.billTypeId=7 AND IFNULL(bu.areaId,0)=wareaId");
    StringBuffer xsBarterMoney = new StringBuffer("SELECT IFNULL(SUM(money),0) FROM bb_billhistory bhy LEFT JOIN b_unit bu ON bhy.unitId=bu.id WHERE bhy.billTypeId=13 AND IFNULL(bu.areaId,0)=wareaId");
    
    StringBuffer xsdArap = new StringBuffer("SELECT IFNULL(SUM(addMoney),0) FROM cw_arap_records cw LEFT JOIN b_unit bu ON cw.unitId=bu.id WHERE cw.billTypeId=4 AND IFNULL(bu.areaId,0)=wareaId");
    StringBuffer xsReturnArap = new StringBuffer("SELECT IFNULL(SUM(subMoney),0) FROM cw_arap_records cw LEFT JOIN b_unit bu ON cw.unitId=bu.id WHERE cw.billTypeId=7 AND IFNULL(bu.areaId,0)=wareaId");
    StringBuffer xsBarterArap = new StringBuffer("SELECT IFNULL(SUM(addMoney),0) FROM cw_arap_records cw LEFT JOIN b_unit bu ON cw.unitId=bu.id WHERE cw.billTypeId=13 AND IFNULL(bu.areaId,0)=wareaId");
    

    StringBuffer xsdCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_sell_detail d LEFT JOIN xs_sell_bill b ON d.billId = b.id LEFT JOIN b_unit bu ON b.unitId=bu.id WHERE IFNULL(bu.areaId,0)=wareaId");
    StringBuffer xsReturnCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_return_detail d LEFT JOIN xs_return_bill b ON d.billId = b.id LEFT JOIN b_unit bu ON b.unitId=bu.id WHERE IFNULL(bu.areaId,0)=wareaId");
    StringBuffer xsBarterOutCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_barter_detail d LEFT JOIN xs_barter_bill b ON d.billId = b.id LEFT JOIN b_unit bu ON b.unitId=bu.id WHERE IFNULL(bu.areaId,0)=wareaId AND d.type=2");
    StringBuffer xsBarterInCost = new StringBuffer("SELECT IFNULL(SUM(costMoneys),0) FROM xs_barter_detail d LEFT JOIN xs_barter_bill b ON d.billId = b.id LEFT JOIN b_unit bu ON b.unitId=bu.id WHERE IFNULL(bu.areaId,0)=wareaId AND d.type=1");
    if (StringUtils.isNotBlank(startTime))
    {
      accounts.append(" and cw.payTime >= '" + startTime + "'");
      
      jhdMoney.append(" and bhy.recodeDate >= '" + startTime + "'");
      jhReturnMoney.append(" and bhy.recodeDate >= '" + startTime + "'");
      jhBarterMoney.append(" and bhy.recodeDate >= '" + startTime + "'");
      jhdArap.append(" and cw.recodeTime >= '" + startTime + "'");
      jhReturnArap.append(" and cw.recodeTime >= '" + startTime + "'");
      jhBarterArap.append(" and cw.recodeTime >= '" + startTime + "'");
      
      xsdMoney.append(" and bhy.recodeDate >= '" + startTime + "'");
      xsReturnMoney.append(" and bhy.recodeDate >= '" + startTime + "'");
      xsBarterMoney.append(" and bhy.recodeDate >= '" + startTime + "'");
      xsdArap.append(" and cw.recodeTime >= '" + startTime + "'");
      xsReturnArap.append(" and cw.recodeTime >= '" + startTime + "'");
      xsBarterArap.append(" and cw.recodeTime >= '" + startTime + "'");
      
      xsdCost.append(" and b.recodeDate >= '" + startTime + "'");
      xsReturnCost.append(" and b.recodeDate >= '" + startTime + "'");
      xsBarterOutCost.append(" and b.recodeDate >= '" + startTime + "'");
      xsBarterInCost.append(" and b.recodeDate >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime))
    {
      accounts.append(" and cw.payTime < '" + endTime + " 23:59:59'");
      
      jhdMoney.append(" and bhy.recodeDate < '" + endTime + " 23:59:59'");
      jhReturnMoney.append(" and bhy.recodeDate < '" + endTime + " 23:59:59'");
      jhBarterMoney.append(" and bhy.recodeDate < '" + endTime + " 23:59:59'");
      jhdArap.append(" and cw.recodeTime < '" + endTime + " 23:59:59'");
      jhReturnArap.append(" and cw.recodeTime < '" + endTime + " 23:59:59'");
      jhBarterArap.append(" and cw.recodeTime < '" + endTime + " 23:59:59'");
      
      xsdMoney.append(" and bhy.recodeDate < '" + endTime + " 23:59:59'");
      xsReturnMoney.append(" and bhy.recodeDate < '" + endTime + " 23:59:59'");
      xsBarterMoney.append(" and bhy.recodeDate < '" + endTime + " 23:59:59'");
      xsdArap.append(" and cw.recodeTime < '" + endTime + " 23:59:59'");
      xsReturnArap.append(" and cw.recodeTime < '" + endTime + " 23:59:59'");
      xsBarterArap.append(" and cw.recodeTime < '" + endTime + " 23:59:59'");
      
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
    ssb.append("area.* ");
    

    StringBuffer sb = new StringBuffer(" FROM");
    sb.append(" (");
    
    sb.append("SELECT ba.id wareaId,ba.rank areaRank, ba.fullName areaName,");
    sb.append("( (" + jhdMoney.toString() + ") - (" + jhReturnMoney.toString() + ") + (" + jhBarterMoney.toString() + ") ) inMoney,");
    sb.append("( (" + jhdArap.toString() + ")-(" + jhReturnArap.toString() + ")+(" + jhBarterArap.toString() + ") ) inWaitMoney,");
    sb.append("( (" + xsdMoney.toString() + ") - (" + xsReturnMoney.toString() + ") + (" + xsBarterMoney.toString() + ") ) outMoney,");
    sb.append("( (" + xsdArap.toString() + ")-(" + xsReturnArap.toString() + ")+(" + xsBarterArap.toString() + ") ) outWaitMoney,");
    sb.append("( (" + xsdCost.toString() + ") - (" + xsReturnCost.toString() + ") + ((" + xsBarterOutCost.toString() + ") - (" + xsBarterInCost.toString() + ")) ) costMoney");
    sb.append(" FROM b_area ba WHERE ba.node=1");
    if ((StringUtils.isNotBlank(areaPrivs)) && (!"*".equals(areaPrivs))) {
      sb.append(" and ba.id in (" + areaPrivs + ") ");
    } else if (StringUtils.isBlank(areaPrivs)) {
      sb.append(" and ba.id = 0 ");
    }
    sb.append(" UNION ALL");
    
    sb.append(" SELECT");
    sb.append(" (0) wareaId,");
    sb.append("(SELECT MAX(rank)+1 FROM b_area) areaRank,");
    sb.append("('其他地区') areaName,");
    sb.append("( (" + jhdMoney.toString() + ") - (" + jhReturnMoney.toString() + ") + (" + jhBarterMoney.toString() + ") ) inMoney,");
    sb.append("( (" + jhdArap.toString() + ")-(" + jhReturnArap.toString() + ")+(" + jhBarterArap.toString() + ") ) inWaitMoney,");
    sb.append("( (" + xsdMoney.toString() + ") - (" + xsReturnMoney.toString() + ") + (" + xsBarterMoney.toString() + ") ) outMoney,");
    sb.append("( (" + xsdArap.toString() + ")-(" + xsReturnArap.toString() + ")+(" + xsBarterArap.toString() + ") ) outWaitMoney,");
    sb.append("( (" + xsdCost.toString() + ") - (" + xsReturnCost.toString() + ") + ((" + xsBarterOutCost.toString() + ") - (" + xsBarterInCost.toString() + ")) ) costMoney");
    sb.append(" FROM DUAL");
    

    sb.append(")");
    sb.append(" temp LEFT JOIN  b_area area ON temp.wareaId = area.id ");
    sb.append(" WHERE 1=1 ");
    


    sb.append(" and (temp.wareaId = 0 OR ((temp.outMoney-temp.costMoney)+(" + accounts.toString() + " AND ba.pids LIKE CONCAT('{0}{9}{22}','%'))-(" + accounts.toString() + " AND ba.pids LIKE CONCAT('{0}{11}{37}','%')) ) != 0)");
    sb.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("area", Area.class);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, ssb.toString(), sb.toString(), alias, new Object[0]);
  }
}
