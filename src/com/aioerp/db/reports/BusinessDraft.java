package com.aioerp.db.reports;

import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.aioerp.model.sys.BillType;
import com.aioerp.model.sys.User;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class BusinessDraft
  extends BaseDbModel
{
  public static final BusinessDraft dao = new BusinessDraft();
  private String producerName;
  
  public String getProducerName(String configName)
  {
    if ((this.producerName == null) && (getInt("producerId") != null))
    {
      User u = (User)User.dao.findById(configName, get("producerId"));
      if (u != null) {
        return u.getStr("username");
      }
    }
    return this.producerName;
  }
  
  Map<String, Class<? extends Model>> MAP = new HashMap();
  private String SELECTSQL = "";
  private String FROMSQL = "";
  
  private void init()
  {
    this.MAP.put("bt", BillType.class);
    this.MAP.put("unit", Unit.class);
    this.MAP.put("staff", Staff.class);
    this.MAP.put("dept", Department.class);
    this.MAP.put("prod", User.class);
    
    this.SELECTSQL = " select bd.*,bt.*,unit.*,staff.*,dept.*,prod.* ";
    

    StringBuffer sb = new StringBuffer(" from bb_businessdraft bd ");
    sb.append(" left join sys_billtype as bt on bd.billTypeId = bt.id  ");
    sb.append(" left join b_unit as unit on bd.unitId = unit.id  ");
    sb.append(" left join b_staff as staff on bd.staffId = staff.id  ");
    sb.append(" left join b_department as dept on bd.departmentId = dept.id ");
    sb.append(" left join sys_user as prod on bd.producerId = prod.id ");
    
    sb.append(" where 1 = 1 ");
    ComFunController.queryStaffPrivs(sb, BaseController.basePrivs(BaseController.STAFF_PRIVS), "bd");
    ComFunController.queryUnitPrivs(sb, BaseController.basePrivs(BaseController.UNIT_PRIVS), "bd");
    ComFunController.queryDepartmentPrivs(sb, BaseController.basePrivs(BaseController.DEPARTMENT_PRIVS), "bd");
    
    this.FROMSQL = sb.toString();
  }
  
  public Model getRecrodById(String configName, Integer id)
  {
    init();
    return findFirst(configName, this.SELECTSQL + this.FROMSQL + "and bd.id = ?", this.MAP, new Object[] { id });
  }
  
  public Integer delete(String configName, Integer billId, Integer billType)
  {
    StringBuffer sql = new StringBuffer("delete from bb_businessdraft where 1=1");
    if (billId != null) {
      sql.append(" and billId = " + billId);
    }
    if (billType != null) {
      sql.append(" and billTypeId = " + billType);
    }
    return Integer.valueOf(Db.use(configName).update(sql.toString()));
  }
  
  public Model getModeByBill(String configName, Integer billId, Integer billType)
  {
    StringBuffer sql = new StringBuffer("SELECT * from bb_businessdraft where 1=1");
    if (billId != null) {
      sql.append(" and billId = " + billId);
    }
    if (billType != null) {
      sql.append(" and billTypeId = " + billType);
    }
    return dao.findFirst(configName, sql.toString());
  }
  
  public Map<String, Object> getPage(String configName, String listID, int pageNum, int numPerPage, String orderField, String orderDirection, int billTypeId, String startDate, String endDate, String unitId, String staffId, String productId, String sgeId, String depmId, String autsId, String isMember, String billCode, String remark, String memo, String isCoupon, String priceCase, String price, String discountCase, String discount, String taxrateCase, String taxRate)
    throws Exception
  {
    init();
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    if (StringUtils.isNotBlank(startDate)) {
      sb.append(" and bd.recodeDate >= '" + startDate + "'");
    }
    if (StringUtils.isNotBlank(endDate)) {
      sb.append(" and bd.recodeDate <= '" + endDate + " 23:59:59'");
    }
    if (billTypeId > 0)
    {
      sb.append(" and bd.billTypeId = ?");
      paras.add(Integer.valueOf(billTypeId));
    }
    if (!StringUtil.isNull(unitId))
    {
      sb.append(" and bd.unitId = ?");
      paras.add(unitId);
    }
    if ((!StringUtil.isNull(staffId)) && (!"0".equals(staffId)))
    {
      sb.append(" and bd.staffId = ?");
      paras.add(staffId);
    }
    if (!StringUtil.isNull(depmId))
    {
      sb.append(" and bd.departmentId = ?");
      paras.add(depmId);
    }
    if (!StringUtil.isNull(billCode))
    {
      sb.append(" and bd.billCode LIKE '%" + billCode + "%'");
      paras.add(billCode);
    }
    if (!StringUtil.isNull(remark))
    {
      sb.append(" and bd.remark LIKE '%" + remark + "%'");
      paras.add(remark);
    }
    if (!StringUtil.isNull(memo))
    {
      sb.append(" and bd.memo LIKE '%" + memo + "%'");
      paras.add(memo);
    }
    "0".equals(isMember);
    if ((!StringUtil.isNull(productId)) || (!StringUtil.isNull(sgeId)) || (!"0".equals(priceCase)) || (!"0".equals(discountCase)) || (!"0".equals(taxrateCase)))
    {
      sb.append(" and (bd.billId,bd.billTypeId) IN ");
      sb.append("(");
      sb.append("SELECT billId,billTypeId FROM cc_stock_records_draft WHERE 1 = 1");
      if (!StringUtil.isNull(productId))
      {
        sb.append(" AND productId = ?");
        paras.add(productId);
      }
      if (!StringUtil.isNull(sgeId))
      {
        sb.append(" AND storageId = ?");
        paras.add(sgeId);
      }
      if (!"0".equals(priceCase))
      {
        String oper = StringUtil.convertToOperator(priceCase);
        sb.append(" AND taxPrice " + oper + " ?");
        paras.add(price);
      }
      if (!"0".equals(discountCase))
      {
        String oper = StringUtil.convertToOperator(discountCase);
        sb.append(" AND discount " + oper + " ?");
        paras.add(discount);
      }
      if (!"0".equals(taxrateCase))
      {
        String oper = StringUtil.convertToOperator(taxrateCase);
        sb.append(" AND taxRate " + oper + " ?");
        paras.add(taxRate);
      }
      sb.append(" GROUP BY billId,billTypeId");
      sb.append(")");
    }
    if (!StringUtil.isNull(autsId))
    {
      sb.append(" and (bd.billId,bd.billTypeId) IN ");
      sb.append("(");
      sb.append("SELECT billId,billTypeId FROM cw_pay_draft WHERE 1 = 1");
      if (!StringUtil.isNull(autsId))
      {
        sb.append(" AND accountId = ?");
        paras.add(autsId);
      }
      sb.append(" GROUP BY billId,billTypeId");
      sb.append(")");
    }
    if (!"0".equals(isCoupon))
    {
      if ("1".equals(isCoupon)) {
        sb.append(" and (bd.billId,bd.billTypeId) IN ");
      } else if ("2".equals(isCoupon)) {
        sb.append(" and (bd.billId,bd.billTypeId) NOT IN ");
      }
      sb.append("(");
      sb.append("SELECT billId,billTypeId FROM cw_pay_draft WHERE 1 = 1");
      sb.append(" AND accountId = 42 ");
      sb.append(" GROUP BY billId,billTypeId");
      sb.append(")");
    }
    if ("producer".equals(orderField)) {
      orderField = "prod.username";
    }
    if ("memo".equals(orderField)) {
      orderField = "bd.memo";
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, this.SELECTSQL, sb.toString(), this.MAP, paras.toArray());
  }
}
