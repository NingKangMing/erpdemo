package com.aioerp.model.base;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class Unit
  extends BaseDbModel
{
  public static final Unit dao = new Unit();
  public static final String TABLE_NAME = "b_unit";
  public List<Object> paras;
  
  public String initOption(String unitPrivs)
  {
    StringBuffer sql = new StringBuffer(" from b_unit u where u.pids like concat(unit.pids,'%') ");
    
    sql = ComFunController.basePrivsSql(sql, unitPrivs, "u");
    
    StringBuffer sb = new StringBuffer();
    sb.append("select unit.*,staff.fullName staffFullName2,area.fullName areaFullName2,staff.departmentId,staff.departmentName,");
    sb.append("(select sum(u.totalGet) " + sql.toString() + ") totalGets,");
    sb.append("(select sum(u.totalPay) " + sql.toString() + ") totalPays,");
    sb.append("(select sum(u.beginGetMoney) " + sql.toString() + ") beginGetMoneys,");
    sb.append("(select sum(u.beginPayMoney) " + sql.toString() + ") beginPayMoneys");
    return sb.toString();
  }
  
  public Staff getStaff(String configName)
  {
    return (Staff)Staff.dao.findById(configName, get("staffId"));
  }
  
  public Area getArea(String configName)
  {
    return (Area)Area.dao.findById(configName, get("areaId"));
  }
  
  public Model getObjectById(String configName, int id)
  {
    return findById(configName, Integer.valueOf(id));
  }
  
  public Boolean add(String configName, Unit model)
  {
    return Boolean.valueOf(model.save(configName));
  }
  
  public Boolean deleteById(String configName, Unit model, int id)
  {
    return Boolean.valueOf(model.deleteById(configName, Integer.valueOf(id)));
  }
  
  public Boolean update(String configName, Unit model)
  {
    return Boolean.valueOf(model.update(configName));
  }
  
  public List<Model> getUnits(String configName, String unitPrivs, String unitIds)
  {
    StringBuffer sql = new StringBuffer("select * from b_unit where node = " + AioConstants.NODE_1);
    String[] unitArr = unitIds.split(",");
    for (int i = 0; i < unitArr.length; i++) {
      if (i == 0) {
        sql.append(" and ( pids like '%{" + unitArr[i] + "}%' ");
      } else {
        sql.append(" or pids like '%{" + unitArr[i] + "}%' ");
      }
    }
    sql.append(")");
    
    sql = ComFunController.basePrivsSql(sql, unitPrivs, null);
    return dao.find(configName, sql.toString());
  }
  
  public String getChildIdsBySupId(String configName, String unitPrivs, int supId)
  {
    String str = "";
    StringBuffer sql = new StringBuffer("select id from b_unit where pids like '%{" + supId + "}%' and status=" + AioConstants.STATUS_ENABLE);
    
    sql = ComFunController.basePrivsSql(sql, unitPrivs, null);
    List<Model> modelList = dao.find(configName, sql.toString());
    if ((modelList != null) && (modelList.size() != 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public String getAllChildIdsBySupId(String configName, String unitPrivs, int supId)
  {
    String str = "";
    StringBuffer sql = new StringBuffer("select id from b_unit where pids like '%{" + supId + "}%'");
    
    sql = ComFunController.basePrivsSql(sql, unitPrivs, null);
    List<Model> modelList = dao.find(configName, sql.toString());
    if ((modelList != null) && (modelList.size() != 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public String getAllChildIdsNoSupId(String configName, String unitPrivs, int supId)
  {
    String str = "";
    StringBuffer sql = new StringBuffer("select id from b_unit where pids not like '%{" + supId + "}%'");
    
    sql = ComFunController.basePrivsSql(sql, unitPrivs, null);
    List<Model> modelList = dao.find(configName, sql.toString());
    if ((modelList != null) && (modelList.size() != 0))
    {
      for (int i = 0; i < modelList.size(); i++) {
        str = str + "," + ((Model)modelList.get(i)).getInt("id");
      }
      str = str.substring(1, str.length());
    }
    return str;
  }
  
  public int getPsupId(String configName, int supId)
  {
    if (supId <= 0) {
      return 0;
    }
    StringBuffer sql = new StringBuffer("select supId from b_unit unit where status != " + AioConstants.STATUS_DELETE + " and id=?");
    return Db.use(configName).queryInt(sql.toString(), new Object[] { Integer.valueOf(supId) }).intValue();
  }
  
  public boolean hasChildren(String configName, int supId)
  {
    StringBuffer sql = new StringBuffer("select count(*) from b_unit unit where status != " + AioConstants.STATUS_DELETE + " and supId=?");
    return Db.use(configName).queryLong(sql.toString(), new Object[] { Integer.valueOf(supId) }).longValue() > 0L;
  }
  
  public List<Model> getParentObjects(String configName, String unitPrivs)
  {
    StringBuffer sql = new StringBuffer("select * from b_unit unit where status != " + AioConstants.STATUS_DELETE + " and node=? ");
    
    sql = ComFunController.basePrivsSql(sql, unitPrivs, null);
    sql.append(" order by rank asc");
    return dao.find(configName, sql.toString(), new Object[] { Integer.valueOf(AioConstants.NODE_2) });
  }
  
  public List<Model> getParentObjects(String configName, String unitPrivs, int supId)
  {
    StringBuffer sql = new StringBuffer("select * from b_unit unit where status != " + AioConstants.STATUS_DELETE + " and node=? and supId = ? ");
    
    sql = ComFunController.basePrivsSql(sql, unitPrivs, null);
    sql.append("order by rank asc");
    return dao.find(configName, sql.toString(), new Object[] { Integer.valueOf(AioConstants.NODE_2), Integer.valueOf(supId) });
  }
  
  public List<Model> getChilds(String configName, String unitPrivs, String[] ids)
  {
    if ((ids == null) || (StringUtils.isBlank(ids[0])))
    {
      StringBuffer sql = new StringBuffer("select * from b_unit where node=?");
      
      sql = ComFunController.basePrivsSql(sql, unitPrivs, null);
      return dao.find(configName, sql.toString(), new Object[] { Integer.valueOf(AioConstants.NODE_1) });
    }
    String idl = "";
    for (int i = 0; i < ids.length; i++)
    {
      if (i != 0) {
        idl = idl + ",";
      }
      idl = idl + ids[i];
    }
    return dao.find(configName, "select * from b_unit where id in (" + idl + ") and node=?", new Object[] { Integer.valueOf(AioConstants.NODE_1) });
  }
  
  public boolean existObjectByNum(String configName, int id, String num)
  {
    Model model = findFirst(configName, "select count(id) as num from b_unit unit where status != " + AioConstants.STATUS_DELETE + " and id<>? and code=?", new Object[] { Integer.valueOf(id), num });
    Long count = model.getLong("num");
    if (count.compareTo(new Long(0L)) > 0) {
      return true;
    }
    return false;
  }
  
  public boolean verify(String configName, String attr, Integer id)
  {
    if (id == null) {
      return true;
    }
    StringBuffer sql = new StringBuffer("select " + attr + " from cc_stock_records where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from xs_sellbook_bill where " + attr + " = " + id);
    
    sql.append(" union all");
    sql.append(" select " + attr + " from cg_bought_bill where " + attr + " = " + id);
    
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
  
  public List<Model> getUnitListById(String configName, String unitPrivs, int unitId)
  {
    List<Model> list = null;
    StringBuffer sql = new StringBuffer("select * from b_unit where node=" + AioConstants.NODE_1);
    
    sql = ComFunController.basePrivsSql(sql, unitPrivs, null);
    if (unitId != 0) {
      sql.append(" and pids like '%{" + unitId + "}%'");
    }
    list = dao.find(configName, sql.toString());
    return list;
  }
  
  public Map<String, Object> getPageByCondtion(String configName, String unitPrivs, String staffPrivs, String areaPrivs, String listId, int supId, Integer node, String initGetOrPay, String searchPar1, String searchValue1, int cPage, int pageSize, String orderField, String orderDirection, int status)
  {
    return aioGetPageRecord(configName, this, listId, cPage, pageSize, initOption(unitPrivs), commSql(unitPrivs, staffPrivs, areaPrivs, supId, node, initGetOrPay, searchPar1, searchValue1, orderField, orderDirection, status).toString(), new Object[0]);
  }
  
  public Map<String, Object> getBottomLists(String configName, String unitPrivs, String staffPrivs, String areaPrivs, int pageNumber, int pageSize, String orderField, String orderDirection, int supId, String listId)
  {
    StringBuffer sb = new StringBuffer("from b_unit unit left join (select bs.*, bd.id departmentId ,bd.fullName departmentName from b_staff bs left join b_department bd on bs.depmId=bd.id) staff on staff.id=unit.staffId left join b_area area on area.id=unit.areaId where unit.status != " + AioConstants.STATUS_DELETE + " and unit.node=1 and unit.pids like ? ");
    
    sb = ComFunController.basePrivsSql(sb, unitPrivs, null);
    if ((orderField != null) && (!orderField.equals("")) && (orderDirection != null) && (!orderDirection.equals(""))) {
      sb.append(" order by " + orderField + " " + orderDirection);
    } else {
      sb.append(" order by unit.rank asc");
    }
    return aioGetPageRecord(configName, this, listId, pageNumber, pageSize, initOption(unitPrivs), sb.toString(), new Object[] { "%{" + supId + "}%" });
  }
  
  public Map<String, Object> getPageBySupId(String configName, String unitPrivs, String staffPrivs, String areaPrivs, int pageIndex, int pageSize, int supId, String fullName, String listId, String searchBaseAttr, String searchBaseVal)
  {
    StringBuffer sb = new StringBuffer("from b_unit unit left join (select bs.*, bd.id departmentId ,bd.fullName departmentName from b_staff bs left join b_department bd on bs.depmId=bd.id) staff on staff.id=unit.staffId left join b_area area on area.id=unit.areaId where unit.status != " + AioConstants.STATUS_DELETE);
    if (StringUtils.isNotBlank(fullName)) {
      sb.append(" and unit.fullName like '%" + fullName + "%'");
    }
    sb = ComFunController.basePrivsSql(sb, unitPrivs, "unit");
    if ((StringUtils.isNotEmpty(searchBaseAttr)) && (StringUtils.isNotEmpty(searchBaseVal)))
    {
      sb.append(" and unit." + searchBaseAttr + " like '%" + searchBaseVal + "%'");
      sb.append(" and  unit.pids like '%" + supId + "%' ");
      sb.append(" and  unit.node = 1 ");
    }
    else
    {
      sb.append(" and  unit.supId=" + supId);
    }
    return aioGetPageRecord(configName, this, listId, pageIndex, pageSize, initOption(unitPrivs), sb.toString() + " order by unit.rank asc", new Object[0]);
  }
  
  public Map<String, Object> getPageByUpObject(String configName, String unitPrivs, String staffPrivs, String areaPrivs, String listId, int supId, int upObjectId, int pageIndex, int pageSize, String initGetOrPay, String searchPar1, String searchValue1, String orderField, String orderDirection, int status)
  {
    return aioGetPageByUpObject(configName, this, listId, upObjectId, pageIndex, pageSize, initOption(unitPrivs), commSql(unitPrivs, staffPrivs, areaPrivs, supId, null, initGetOrPay, searchPar1, searchValue1, orderField, orderDirection, status).toString(), "unit", new Object[0]);
  }
  
  public Map<String, Object> getPageByFilterLast(String configName, String unitPrivs, String staffPrivs, String areaPrivs, String listId, int supId, String initGetOrPay, String searchPar1, String searchValue1, int pageIndex, int pageSize, String orderField, String orderDirection, int status)
  {
    return aioGetLastPageRecord(configName, this, listId, pageIndex, pageSize, initOption(unitPrivs), commSql(unitPrivs, staffPrivs, areaPrivs, supId, null, initGetOrPay, searchPar1, searchValue1, orderField, orderDirection, status).toString(), new Object[0]);
  }
  
  public StringBuffer commSql(String unitPrivs, String staffPrivs, String areaPrivs, int supId, Integer node, String initGetOrPay, String searchPar1, String searchValue1, String orderField, String orderDirection, int status)
  {
    StringBuffer sb = new StringBuffer("from b_unit unit left join (select bs.*, bd.id departmentId ,bd.fullName departmentName from b_staff bs left join b_department bd on bs.depmId=bd.id) staff on staff.id=unit.staffId left join b_area area on area.id=unit.areaId where unit.status != " + status);
    if ((searchPar1 != null) && (!searchPar1.equals("")) && (StringUtils.isNotEmpty(searchValue1)))
    {
      if ((searchValue1 != null) && (!searchValue1.equals("")))
      {
        if (searchPar1.equals("all"))
        {
          sb.append(" and (unit.fullName like '%" + searchValue1 + "%'");
          sb.append(" or unit.code like '%" + searchValue1 + "%'");
          sb.append(" or unit.smallName like '%" + searchValue1 + "%'");
          sb.append(" or unit.spell like '%" + searchValue1 + "%'");
          sb.append(searchByStatus("fast", searchValue1, "unit"));
          sb.append(" or unit.memo like '%" + searchValue1 + "%')");
        }
        else if (searchPar1.equals("status"))
        {
          sb.append(searchByStatus("condition", searchValue1, "unit"));
        }
        else
        {
          sb.append(" and unit." + searchPar1 + " like '%" + searchValue1 + "%'");
        }
        sb.append(" and unit.pids like '%{" + supId + "}%'");
      }
      else
      {
        sb.append(" and unit.supId=" + supId);
      }
    }
    else if ((node != null) && (node.intValue() == 1)) {
      sb.append(" and unit.pids like '%{" + supId + "}%'");
    } else {
      sb.append(" and unit.supId=" + supId);
    }
    if ((node != null) && (node.intValue() == 1)) {
      sb.append(" and unit.node=" + node);
    }
    if ((initGetOrPay != null) && (!initGetOrPay.equals("all")))
    {
      StringBuffer beginGetMoney = new StringBuffer("(select sum(u.beginGetMoney) from b_unit u where u.pids like concat(unit.pids,'%') ");
      ComFunController.basePrivsSql(beginGetMoney, unitPrivs, "u");
      beginGetMoney.append(")");
      StringBuffer beginPayMoney = new StringBuffer("(select sum(u.beginPayMoney) from b_unit u where u.pids like concat(unit.pids,'%') ");
      ComFunController.basePrivsSql(beginPayMoney, unitPrivs, "u");
      beginPayMoney.append(")");
      if (initGetOrPay.equals("noZero")) {
        sb.append(" and (" + beginGetMoney.toString() + " !=0 or " + beginPayMoney.toString() + "!=0) ");
      } else if (initGetOrPay.equals("zero")) {
        sb.append(" and (" + beginGetMoney.toString() + " =0) and (" + beginPayMoney.toString() + "=0) ");
      }
    }
    sb = ComFunController.basePrivsSql(sb, unitPrivs, "unit");
    if ((orderField != null) && (!orderField.equals("")) && (!orderField.equals("rank")) && (orderDirection != null) && (!orderDirection.equals(""))) {
      sb.append(" order by " + orderField + " " + orderDirection);
    } else {
      sb.append(" order by unit.rank asc");
    }
    return sb;
  }
  
  public Map<String, Object> search(String configName, String unitPrivs, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
  {
    StringBuffer sql = new StringBuffer(" from b_unit unit left join (select bs.*, bd.id departmentId ,bd.fullName departmentName from b_staff bs left join b_department bd on bs.depmId=bd.id) staff on staff.id=unit.staffId left join b_area area on area.id=unit.areaId where unit.status=" + AioConstants.STATUS_ENABLE);
    if ((pars.get("termVal") == null) || (pars.get("termVal").equals(""))) {
      sql.append(" and unit.supId=0");
    } else {
      sql.append(" and unit.node=" + AioConstants.NODE_1);
    }
    sql = ComFunController.basePrivsSql(sql, unitPrivs, "unit");
    StringBuffer appendSql = appendSql(orderField, orderDirection, pars, "unit");
    sql.append(appendSql);
    return aioGetPageRecord(configName, dao, "", pageNum, numPerPage, initOption(unitPrivs), sql.toString(), this.paras.toArray());
  }
  
  public Map<String, Object> supIdSearch(String configName, String unitPrivs, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
  {
    StringBuffer sql = null;
    if ((pars.get("termVal") != null) && (!pars.get("termVal").equals(""))) {
      sql = new StringBuffer(" from b_unit unit left join (select bs.*, bd.id departmentId ,bd.fullName departmentName from b_staff bs left join b_department bd on bs.depmId=bd.id) staff on staff.id=unit.staffId left join b_area area on area.id=unit.areaId where unit.status=" + AioConstants.STATUS_ENABLE + " and  unit.node=" + AioConstants.NODE_1);
    } else {
      sql = new StringBuffer(" from b_unit unit left join (select bs.*, bd.id departmentId ,bd.fullName departmentName from b_staff bs left join b_department bd on bs.depmId=bd.id) staff on staff.id=unit.staffId left join b_area area on area.id=unit.areaId where unit.status=" + AioConstants.STATUS_ENABLE + " and unit.supId=" + supId);
    }
    sql = ComFunController.basePrivsSql(sql, unitPrivs, "unit");
    StringBuffer appendSql = appendSql(orderField, orderDirection, pars, "unit");
    sql.append(appendSql);
    return aioGetPageRecord(configName, dao, "", pageNum, numPerPage, initOption(unitPrivs), sql.toString(), this.paras.toArray());
  }
  
  public Map<String, Object> shouldGetOrPay(String configName, String listID, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars, String privs)
  {
    StringBuffer sql = new StringBuffer("from b_unit where 1=1");
    if (pars != null) {
      if ((pars.get("modelType") != null) && ("pay".equals(pars.get("modelType")))) {
        sql.append(" and payMoneyLimit is not null and  totalPay !=0 and totalPay >= payMoneyLimit");
      } else {
        sql.append(" and getMoneyLimit is not null and  totalGet !=0 and totalGet >= getMoneyLimit");
      }
    }
    ComFunController.basePrivsSql(sql, privs, null);
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    }
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql.toString(), new Object[0]);
  }
  
  public StringBuffer appendSql(String orderField, String orderDirection, Map<String, Object> pars, String obj)
  {
    String objStr = "";
    if ((obj != null) && (!obj.equals(""))) {
      objStr = obj + ".";
    }
    StringBuffer sql = new StringBuffer("");
    this.paras = new ArrayList();
    String term = "";
    if ((pars != null) && (pars.size() > 0)) {
      term = (String)pars.get("term");
    }
    if ((pars != null) && (term != null) && (!term.equals(""))) {
      if (term.equals("quick"))
      {
        sql.append(" and( " + objStr + "code like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or " + objStr + "fullName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or " + objStr + "spell like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or " + objStr + "address like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or " + objStr + "phone like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or " + objStr + "fax like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or " + objStr + "bank like ? )");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("code"))
      {
        sql.append(" and " + objStr + "code like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("fullName"))
      {
        sql.append(" and " + objStr + "fullName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("spell"))
      {
        sql.append(" and " + objStr + "spell like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("address"))
      {
        sql.append(" and " + objStr + "address like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("phone"))
      {
        sql.append(" and " + objStr + "phone like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("fax"))
      {
        sql.append(" and " + objStr + "fax like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("bank"))
      {
        sql.append(" and " + objStr + "bank like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
    }
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by " + objStr + "rank asc");
    }
    return sql;
  }
  
  public Map<String, Object> getOptionPage(String configName, String unitPrivs, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
  {
    StringBuffer sql = new StringBuffer(" from b_unit where 1=1 ");
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
    else
    {
      sql.append(" and supId = ?");
      paras.add(Integer.valueOf(0));
    }
    sql = ComFunController.basePrivsSql(sql, unitPrivs, null);
    
    sql.append(" and status =? ");
    paras.add(Integer.valueOf(AioConstants.STATUS_ENABLE));
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by rank asc ");
    }
    return aioGetPageRecord(configName, dao, listId, pageNum, numPerPage, "select *", sql.toString(), paras.toArray());
  }
  
  public Map<String, Object> unitMultiPage(String configName, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars, String unitPrivs)
  {
    StringBuffer selectSql = new StringBuffer("select *  ");
    StringBuffer fromSql = new StringBuffer(" from b_unit unit where unit.status!= " + AioConstants.STATUS_DISABLE);
    
    queryPrivs(fromSql, unitPrivs);
    if ((pars.get("termVal") != null) && (!pars.get("termVal").equals(""))) {
      fromSql.append(" and unit.node=" + AioConstants.NODE_1);
    } else {
      fromSql.append(" and unit.supId=" + supId);
    }
    StringBuffer appendStockSql = appendMutilUnitSql(orderField, orderDirection, pars);
    fromSql.append(appendStockSql);
    return aioGetPageRecord(configName, Storage.dao, "", pageNum, numPerPage, selectSql.toString(), fromSql.toString(), this.paras.toArray());
  }
  
  public Map<String, Object> unitMultiSearch(String configName, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars, String unitPrivs)
  {
    StringBuffer selectSql = new StringBuffer("select *  ");
    StringBuffer fromSql = new StringBuffer(" from b_unit unit where 1=1 ");
    queryPrivs(fromSql, unitPrivs);
    if ((pars.get("termVal") == null) || (pars.get("termVal").equals(""))) {
      fromSql.append(" and unit.supId=0");
    } else {
      fromSql.append(" and unit.node=" + AioConstants.NODE_1);
    }
    StringBuffer appendStockSql = appendMutilUnitSql(orderField, orderDirection, pars);
    fromSql.append(appendStockSql);
    return aioGetPageRecord(configName, Storage.dao, "", pageNum, numPerPage, selectSql.toString(), fromSql.toString(), this.paras.toArray());
  }
  
  public Map<String, Object> unitMultiDown(String configName, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars, String unitPrivs)
  {
    StringBuffer selectSql = new StringBuffer("select *  ");
    StringBuffer fromSql = new StringBuffer(" from b_unit unit where 1=1 ");
    queryPrivs(fromSql, unitPrivs);
    fromSql.append(" and unit.supId=" + supId);
    StringBuffer appendStockSql = appendMutilUnitSql(orderField, orderDirection, pars);
    fromSql.append(appendStockSql);
    return unitMultiDownPage(configName, "", supId, pageNum, numPerPage, selectSql.toString(), fromSql.toString(), unitPrivs, this.paras.toArray());
  }
  
  public Map<String, Object> unitMultiUp(String configName, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, int ObjectId, String listID, Map<String, Object> pars, String unitPrivs)
  {
    StringBuffer selectSql = new StringBuffer("select *  ");
    StringBuffer fromSql = new StringBuffer(" from b_unit unit where 1=1 ");
    queryPrivs(fromSql, unitPrivs);
    fromSql.append(" and unit.status=" + AioConstants.STATUS_ENABLE + " and unit.supId=" + supId);
    StringBuffer appendStockSql = appendMutilUnitSql(orderField, orderDirection, pars);
    fromSql.append(appendStockSql);
    return unitMultiUpPage(configName, "", supId, ObjectId, pageNum, numPerPage, selectSql.toString(), fromSql.toString(), unitPrivs, this.paras.toArray());
  }
  
  public Map<String, Object> unitMultiDownPage(String configName, String listID, int supId, int pageNum, int numPerPage, String selectSql, String fromSql, String unitPrivs, Object... pars)
  {
    Page page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, pars);
    List pageList = page.getList();
    String sqlId = "select id,pids from b_unit where supId=" + supId + " and status=" + AioConstants.STATUS_ENABLE;
    sqlId = queryPrivs(sqlId, unitPrivs);
    List<Model> listId = Storage.dao.find(configName, sqlId);
    String sql = "select id,pids from b_unit where status=" + AioConstants.STATUS_ENABLE;
    sql = queryPrivs(sql, unitPrivs);
    List<Model> listAllId = Storage.dao.find(configName, sql);
    if ((pageList.size() <= 0) && (pageNum > 1))
    {
      int count = page.getTotalRow();
      pageNum = count / numPerPage;
      if (count / numPerPage > count / numPerPage) {
        pageNum++;
      }
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, pars);
      pageList = page.getList();
    }
    int tp = page.getTotalPage();
    int tr = page.getTotalRow();
    int limit = (pageNum - 1) * numPerPage;
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(tp));
    maps.put("totalCount", Integer.valueOf(tr));
    maps.put("pageList", pageList);
    maps.put("listID", listID);
    maps.put("limit", Integer.valueOf(limit));
    
    maps.put("listAllIdAndPids", listAllId);
    maps.put("listSubIdAndPidsBySupId", listId);
    return maps;
  }
  
  public Map<String, Object> unitMultiUpPage(String configName, String listID, int supId, int upObjectId, int pageNum, int numPerPage, String selectSql, String fromSql, String unitPrivs, Object... pars)
  {
    String sqlId = "select id,pids from b_unit where supId=" + supId + " and status=" + AioConstants.STATUS_ENABLE;
    sqlId = queryPrivs(sqlId, unitPrivs);
    List<Model> listId = Storage.dao.find(configName, sqlId);
    
    String sql = "select id,pids from b_unit where status=" + AioConstants.STATUS_ENABLE;
    sql = queryPrivs(sql, unitPrivs);
    List<Model> listAllId = Storage.dao.find(configName, sql);
    
    List<Integer> ids = null;
    ids = Db.use(configName).query("select id from b_unit where supId=" + supId + " and status=" + AioConstants.STATUS_ENABLE);
    int objectIndex = 0;
    for (int i = 0; i < ids.size(); i++)
    {
      objectIndex++;
      if (((Integer)ids.get(i)).intValue() == upObjectId) {
        break;
      }
    }
    pageNum = objectIndex % numPerPage == 0 ? objectIndex / numPerPage : objectIndex / numPerPage + 1;
    Page page = null;
    if ((pars.length > 0) && (!pars[0].toString().equals("[]"))) {
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, pars);
    } else {
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null);
    }
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(page.getTotalPage()));
    maps.put("totalCount", Integer.valueOf(page.getTotalRow()));
    maps.put("pageList", page.getList());
    maps.put("listID", listID);
    maps.put("limit", Integer.valueOf((pageNum - 1) * numPerPage));
    
    maps.put("listAllIdAndPids", listAllId);
    maps.put("listSubIdAndPidsBySupId", listId);
    return maps;
  }
  
  public StringBuffer appendMutilUnitSql(String orderField, String orderDirection, Map<String, Object> pars)
  {
    StringBuffer sql = new StringBuffer("");
    this.paras = new ArrayList();
    String term = "";
    if ((pars != null) && (pars.size() > 0)) {
      term = (String)pars.get("term");
    }
    if ((pars != null) && (term != null) && (!term.equals(""))) {
      if (term.equals("quick"))
      {
        sql.append(" and( unit.code like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or unit.fullName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or unit.spell like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or unit.smallName like ? )");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("code"))
      {
        sql.append(" and unit.code like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("fullName"))
      {
        sql.append(" and unit.fullName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("spell"))
      {
        sql.append(" and unit.spell like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("smallName"))
      {
        sql.append(" and unit.smallName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
    }
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by unit.rank asc");
    }
    return sql;
  }
  
  private void queryPrivs(StringBuffer sql, String unitPrivs)
  {
    if ((StringUtils.isNotBlank(unitPrivs)) && (!"*".equals(unitPrivs))) {
      sql.append(" and id in(" + unitPrivs + ")");
    } else if (StringUtils.isBlank(unitPrivs)) {
      sql.append(" and id =0");
    }
  }
  
  private String queryPrivs(String sql, String unitPrivs)
  {
    if ((StringUtils.isNotBlank(unitPrivs)) && (!"*".equals(unitPrivs))) {
      sql = sql + " and id in(" + unitPrivs + ")";
    } else if (StringUtils.isBlank(unitPrivs)) {
      sql = sql + " and id =0";
    }
    return sql;
  }
  
  public List<Model> unitMultiSearchBack(String configName, String ids)
  {
    String[] idArr = ids.split(",");
    StringBuffer sb = new StringBuffer("select id,fullName from b_unit where node=" + AioConstants.NODE_1);
    for (int i = 0; i < idArr.length; i++) {
      if (i == 0) {
        sb.append(" and pids like '%{" + idArr[i] + "}%'");
      } else {
        sb.append(" or pids like '%{" + idArr[i] + "}%'");
      }
    }
    return find(configName, sb.toString());
  }
  
  public Map<String, Object> getUnitArapPage(String configName, String unitPrivs, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, Integer supId, Integer ObjectId, Integer node, String unitPids, Integer areaId)
  {
    StringBuffer sql = new StringBuffer(" FROM b_unit unit WHERE unit.pids LIKE CONCAT(bu.pids,'%') ");
    
    sql = ComFunController.basePrivsSql(sql, unitPrivs, "unit");
    
    StringBuffer ssb = new StringBuffer(" SELECT ");
    ssb.append(" staff.fullName staffFullName2,area.fullName areaFullName2,");
    ssb.append("(SELECT SUM(unit.totalGet) " + sql.toString() + ") gets,");
    ssb.append("(SELECT SUM(unit.totalPay) " + sql.toString() + ") pays,");
    ssb.append("(SELECT SUM(unit.totalPreGet) " + sql.toString() + ") preGets,");
    ssb.append("(SELECT SUM(unit.totalPrePay) " + sql.toString() + ") prePays,");
    ssb.append("bu.*");
    StringBuffer sb = new StringBuffer(" FROM b_unit bu left join b_staff staff on staff.id=bu.staffId left join b_area area on area.id=bu.areaId WHERE 1=1 ");
    List<Object> paras = new ArrayList();
    if ((("".equals(unitPids)) || ("{0}".equals(unitPids))) && (areaId.intValue() == 0) && (node.intValue() == 0))
    {
      sb.append(" and bu.supId = ? ");
      paras.add(supId);
    }
    else
    {
      if (!"".equals(unitPids))
      {
        sb.append(" and bu.pids LIKE CONCAT(?,'%') ");
        paras.add(unitPids);
      }
      if ((areaId != null) && (areaId.intValue() != 0))
      {
        sb.append(" and bu.areaId = ? ");
        paras.add(areaId);
      }
      if (node.intValue() == 1)
      {
        sb.append(" and bu.node = 1 ");
        sb.append(" and bu.pids LIKE '%" + supId + "%' ");
      }
    }
    sb = ComFunController.basePrivsSql(sb, unitPrivs, "bu");
    
    sb.append(" order by bu." + orderField + " " + orderDirection);
    if ((ObjectId != null) && (ObjectId.intValue() > 0)) {
      return aioGetPageByUpObject(configName, this, listId, ObjectId.intValue(), pageNum, numPerPage, ssb.toString(), sb.toString(), "bu", paras.toArray());
    }
    return aioGetPageRecord(configName, this, listId, pageNum, numPerPage, ssb.toString(), sb.toString(), paras.toArray());
  }
  
  public Map<String, Object> getUnitBusCountPage(String configName, String unitPrivs, String areaPrivs, String listId, int pageNum, int numPerPage, String orderField, String orderDirection, String startTime, String endTime)
    throws SQLException, Exception
  {
    String selSql = "SELECT temp.*,bu.*,ba.*";
    
    StringBuilder fSql = new StringBuilder(" FROM (");
    fSql.append("SELECT temp2.*,(temp2.outMoney-temp2.costMoney) profit,");
    fSql.append("SUM(CASE WHEN bas.pids LIKE CONCAT('{0}{9}{22}','%') THEN CASE WHEN cw.type=0 THEN cw.payMoney ELSE cw.payMoney*-1 END ELSE 0 END) otherIn,");
    fSql.append("SUM(CASE WHEN bas.pids LIKE CONCAT('{0}{11}{37}','%') THEN CASE WHEN cw.type=0 THEN cw.payMoney ELSE cw.payMoney*-1 END ELSE 0 END) costTotal,");
    fSql.append("SUM(CASE WHEN (bas.pids LIKE CONCAT('{0}{1}{3}','%') OR bas.pids LIKE CONCAT('{0}{1}{4}','%')) AND cw.billTypeId IN (4,7,13) THEN CASE WHEN cw.type=0 THEN cw.payMoney ELSE cw.payMoney*-1 END ELSE 0 END) returenMoney,");
    fSql.append("((temp2.outMoney-temp2.costMoney) + (SUM(CASE WHEN bas.pids LIKE CONCAT('{0}{9}{22}','%') THEN CASE WHEN cw.type=0 THEN cw.payMoney ELSE cw.payMoney*-1 END ELSE 0 END)) - (SUM(CASE WHEN bas.pids LIKE CONCAT('{0}{11}{37}','%') THEN CASE WHEN cw.type=0 THEN cw.payMoney ELSE cw.payMoney*-1 END ELSE 0 END)) ) periodProfit");
    fSql.append(" FROM");
    fSql.append(" (");
    fSql.append("SELECT bu.id unitId,bu.rank unitRank, bu.fullName unitName,");
    
    fSql.append("IFNULL(");
    fSql.append("(");
    fSql.append(" SELECT (SUM(CASE WHEN billTypeId = 5 THEN money ELSE 0 END) - SUM(CASE WHEN billTypeId = 6 THEN money ELSE 0 END) + SUM(CASE WHEN billTypeId = 12 THEN money ELSE 0 END) )");
    fSql.append(" FROM bb_billhistory WHERE unitId = bu.id");
    if (StringUtils.isNotBlank(startTime)) {
      fSql.append(" AND recodeDate >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      fSql.append(" AND recodeDate < '" + endTime + " 23:59:59'");
    }
    fSql.append("),0) inMoney,");
    
    fSql.append("IFNULL(");
    fSql.append("(");
    fSql.append(" SUM(CASE WHEN billTypeId = 5 THEN subMoney ELSE 0 END) - SUM(CASE WHEN billTypeId = 6 THEN addMoney ELSE 0 END) + SUM(CASE WHEN billTypeId = 12 THEN subMoney ELSE 0 END)");
    fSql.append("),0) inWaitMoney,");
    
    fSql.append("IFNULL(");
    fSql.append("(");
    fSql.append(" SELECT (SUM(CASE WHEN billTypeId = 4 THEN money ELSE 0 END) - SUM(CASE WHEN billTypeId = 7 THEN money ELSE 0 END) + SUM(CASE WHEN billTypeId = 13 THEN money ELSE 0 END) )");
    fSql.append(" FROM bb_billhistory WHERE unitId = bu.id");
    if (StringUtils.isNotBlank(startTime)) {
      fSql.append(" AND recodeDate >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      fSql.append(" AND recodeDate < '" + endTime + " 23:59:59'");
    }
    fSql.append("),0) outMoney,");
    
    fSql.append("IFNULL(");
    fSql.append("(");
    fSql.append(" SUM(CASE WHEN billTypeId = 4 THEN addMoney ELSE 0 END) - SUM(CASE WHEN billTypeId = 7 THEN subMoney ELSE 0 END) + SUM(CASE WHEN billTypeId = 13 THEN addMoney ELSE 0 END)");
    fSql.append("),0) outWaitMoney,");
    
    fSql.append("IFNULL(");
    fSql.append("(");
    fSql.append("(");
    fSql.append("SELECT SUM(costMoneys) FROM xs_sell_detail d LEFT JOIN xs_sell_bill b ON d.billId = b.id WHERE b.unitId=bu.id");
    if (StringUtils.isNotBlank(startTime)) {
      fSql.append(" AND b.recodeDate >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      fSql.append(" AND b.recodeDate < '" + endTime + " 23:59:59'");
    }
    fSql.append(") - ");
    fSql.append("(");
    fSql.append("SELECT SUM(costMoneys) FROM xs_return_detail d LEFT JOIN xs_return_bill b ON d.billId = b.id WHERE b.unitId=bu.id");
    if (StringUtils.isNotBlank(startTime)) {
      fSql.append(" AND b.recodeDate >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      fSql.append(" AND b.recodeDate < '" + endTime + " 23:59:59'");
    }
    fSql.append(") + ");
    fSql.append("(");
    fSql.append("SELECT SUM(CASE WHEN d.type=2 THEN costMoneys ELSE costMoneys*-1 END) FROM xs_barter_detail d LEFT JOIN xs_barter_bill b ON d.billId = b.id WHERE b.unitId=bu.id");
    if (StringUtils.isNotBlank(startTime)) {
      fSql.append(" AND b.recodeDate >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      fSql.append(" AND b.recodeDate < '" + endTime + " 23:59:59'");
    }
    fSql.append(")");
    fSql.append("),0) costMoney");
    fSql.append(" FROM b_unit bu");
    fSql.append(" LEFT JOIN cw_arap_records cr ON bu.id = cr.unitId");
    fSql.append(" WHERE bu.node=1");
    if (StringUtils.isNotBlank(startTime)) {
      fSql.append(" AND cr.recodeTime >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      fSql.append(" AND cr.recodeTime < '" + endTime + " 23:59:59'");
    }
    if ((StringUtils.isNotBlank(unitPrivs)) && (!"*".equals(unitPrivs))) {
      fSql.append(" and bu.id in (" + unitPrivs + ") ");
    } else if (StringUtils.isBlank(unitPrivs)) {
      fSql.append(" and bu.id = 0 ");
    }
    if ((StringUtils.isNotBlank(areaPrivs)) && (!"*".equals(areaPrivs))) {
      fSql.append(" and bu.areaId in (" + areaPrivs + ") ");
    } else if (StringUtils.isBlank(areaPrivs)) {
      fSql.append(" and bu.areaId = 0 ");
    }
    fSql.append(" GROUP BY bu.id");
    
    fSql.append(" UNION ALL");
    
    fSql.append(" SELECT (0) unitId,(SELECT MAX(rank)+1 FROM b_unit) unitRank,('其他单位') unitName,(0) inMoney,(0) inWaitMoney,(0) outMoney,(0) outWaitMoney,(0) costMoney FROM DUAL");
    
    fSql.append(" ) temp2");
    fSql.append(" LEFT JOIN cw_pay_type cw ON cw.unitId = temp2.unitId");
    fSql.append(" LEFT JOIN b_accounts bas ON cw.accountId=bas.id");
    fSql.append(" WHERE 1=1 ");
    if (StringUtils.isNotBlank(startTime)) {
      fSql.append(" AND cw.payTime >= '" + startTime + "'");
    }
    if (StringUtils.isNotBlank(endTime)) {
      fSql.append(" AND cw.payTime < '" + endTime + " 23:59:59'");
    }
    fSql.append(" GROUP BY unitId");
    
    fSql.append(") temp ");
    fSql.append(" LEFT JOIN  b_unit bu ON temp.unitId = bu.id ");
    fSql.append(" LEFT JOIN  b_area ba ON bu.areaId = ba.id ");
    fSql.append(" WHERE periodProfit != 0");
    fSql.append(" ORDER BY " + orderField + " " + orderDirection);
    

    Map<String, Class<? extends Model>> alias = new HashMap();
    alias.put("bu", Unit.class);
    alias.put("ba", Area.class);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, selSql, fSql.toString(), alias, new Object[0]);
  }
  
  public Model getUnitArapInit(String configName, String unitPrivs, String unitPids, Integer type)
  {
    StringBuffer sql = new StringBuffer("SELECT");
    String arap = " (IFNULL(SUM(beginGetMoney),0)-IFNULL(SUM(beginPayMoney),0))";
    String prep = " SUM(beginPrePayMoney)";
    if (type.intValue() == 1)
    {
      arap = " (IFNULL(SUM(beginPayMoney),0)-IFNULL(SUM(beginGetMoney),0))";
      prep = " SUM(beginPreGetMoney)";
    }
    sql.append(arap + " beginMoney,");
    sql.append(prep + " beginPreMoney");
    sql.append(" FROM b_unit bu WHERE 1=1");
    sql.append(" AND bu.pids LIKE CONCAT('" + unitPids + "','%')");
    sql = ComFunController.basePrivsSql(sql, unitPrivs, "bu");
    return findFirst(configName, sql.toString());
  }
  
  public List<Model> getUnits(String configName, Integer supId, String searchPar, String searchVal)
  {
    StringBuffer sb = new StringBuffer("SELECT * FROM b_unit WHERE 1=1");
    if ((searchPar != null) && (!searchVal.equals(""))) {
      if (searchPar.equals("all"))
      {
        sb.append(" and (fullName like '%" + searchVal + "%'");
        sb.append(" or code like '%" + searchVal + "%'");
        sb.append(" or smallName like '%" + searchVal + "%'");
        sb.append(" or spell like '%" + searchVal + "%'");
        sb.append(searchByStatus("fast", searchVal, null));
        sb.append(" or memo like '%" + searchVal + "%')");
      }
      else if (searchPar.equals("status"))
      {
        sb.append(searchByStatus("condition", searchVal, null));
      }
      else
      {
        sb.append(" and " + searchPar + " like '%" + searchVal + "%'");
      }
    }
    if (supId != null) {
      sb.append(" and pids like '%{" + supId + "}%' ");
    }
    sb.append(" and node = 1 ");
    return dao.find(configName, sb.toString());
  }
  
  public Model getRecordByCode(String configName, Object code)
  {
    Model model = null;
    if (code != null)
    {
      String sql = "SELECT * FROM b_unit WHERE code=?";
      model = dao.findFirst(configName, sql, new Object[] { code });
    }
    return model;
  }
  
  public int getMaxRank(String configName)
  {
    int maxRank = 1;
    String sql = "SELECT MAX(rank) maxRank FROM b_unit";
    Model model = dao.findFirst(configName, sql);
    if (model.get("maxRank") != null) {
      maxRank = model.getInt("maxRank").intValue() + 1;
    }
    return maxRank;
  }
  
  public BigDecimal getNumById(String configName, String str, Integer id)
  {
    String sql = "SELECT " + str + " FROM b_unit WHERE id = ?";
    return Db.use(configName).queryBigDecimal(sql, new Object[] { id });
  }
  
  public Map<String, BigDecimal> getInitArap(String configName)
  {
    String sql = "SELECT SUM(beginGetMoney) bgGetMoney, SUM(beginPayMoney) bgPayMoney FROM b_unit";
    Model m = dao.findFirst(configName, sql);
    BigDecimal bgGetMoney = m.getBigDecimal("bgGetMoney");
    BigDecimal bgPayMoney = m.getBigDecimal("bgPayMoney");
    Map<String, BigDecimal> map = new HashMap();
    map.put("getMoney", bgGetMoney);
    map.put("payMoney", bgPayMoney);
    return map;
  }
  
  public List<Model> getListByPids(String configName, String pids)
  {
    String sql = "SELECT * FROM  b_unit WHERE pids LIKE CONCAT('" + pids + "','%')";
    return find(configName, sql);
  }
  
  public Model getModelByPids(String configName, String tableName, String pids)
  {
    String sql = "SELECT * FROM " + tableName + " WHERE pids ='" + pids + "'";
    return findFirst(configName, sql);
  }
  
  public void openAccountUpdateUnitGetOrPay(String configName)
  {
    Db.use(configName).update("update b_unit set totalGet = beginGetMoney , totalPay = beginPayMoney");
  }
  
  public void invertOpenAccountUpdateUnitGetOrPay(String configName)
  {
    Db.use(configName).update("update b_unit set totalGet = null , totalPay = null");
  }
}
