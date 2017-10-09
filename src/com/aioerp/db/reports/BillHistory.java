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
import com.jfinal.plugin.activerecord.Model;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class BillHistory
  extends BaseDbModel
{
  public static final BillHistory dao = new BillHistory();
  private String producerName;
  private String postUsreName;
  
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
  
  public String getPostUsreName(String configName)
  {
    if ((this.postUsreName == null) && (getInt("postUsreId") != null))
    {
      User u = (User)User.dao.findById(configName, get("postUsreId"));
      if (u != null) {
        return u.getStr("username");
      }
    }
    return this.postUsreName;
  }
  
  public Date getHistoryFristBillRecodeDate(String configName)
  {
    Model m = findFirst(configName, "select recodeDate from bb_billhistory order by recodeDate asc");
    if (m == null) {
      return null;
    }
    return m.getTimestamp("recodeDate");
  }
  
  public int getStartToEndDateHistoryBillCount(String configName, Date startDate, Date endDate)
  {
    Model m = null;
    if ((startDate == null) && (endDate == null)) {
      m = findFirst(configName, "select count(*) count from bb_billhistory");
    } else {
      m = findFirst(configName, "select count(*) count from bb_billhistory where recodeDate between ? and ?", new Object[] { startDate, endDate });
    }
    if (m == null) {
      return 0;
    }
    return Integer.valueOf(m.get("count").toString()).intValue();
  }
  
  public Model getRecordByBillIdAndTypeId(String configName, Integer billId, Integer billTypeId)
  {
    String sql = "SELECT * FROM bb_billhistory WHERE billId = ? AND billTypeId = ?";
    return findFirst(configName, sql, new Object[] { billId, billTypeId });
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
    this.MAP.put("post", User.class);
    
    this.SELECTSQL = " select bh.*,bt.*,unit.*,staff.*,dept.*,prod.*,post.* ";
    
    StringBuffer sb = new StringBuffer(" from bb_billhistory bh ");
    sb.append(" left join sys_billtype as bt on bh.billTypeId = bt.id  ");
    sb.append(" left join b_unit as unit on bh.unitId = unit.id  ");
    sb.append(" left join b_staff as staff on bh.staffId = staff.id  ");
    sb.append(" left join b_department as dept on bh.departmentId = dept.id ");
    sb.append(" left join sys_user as prod on bh.producerId = prod.id ");
    sb.append(" left join sys_user as post on bh.postUsreId = post.id ");
    
    sb.append(" where 1 = 1 ");
    ComFunController.queryStaffPrivs(sb, BaseController.basePrivs(BaseController.STAFF_PRIVS), "bh");
    ComFunController.queryUnitPrivs(sb, BaseController.basePrivs(BaseController.UNIT_PRIVS), "bh");
    ComFunController.queryDepartmentPrivs(sb, BaseController.basePrivs(BaseController.DEPARTMENT_PRIVS), "bh");
    
    this.FROMSQL = sb.toString();
  }
  
  public Model getModelById(String configName, int id)
  {
    init();
    return findFirst(configName, this.SELECTSQL + this.FROMSQL + "and bh.id = ?", this.MAP, new Object[] { Integer.valueOf(id) });
  }
  
  public Model getModelByUserId(String configName, int id, Integer userId)
  {
    init();
    if (userId == null) {
      userId = Integer.valueOf(0);
    }
    return findFirst(configName, this.SELECTSQL + this.FROMSQL + "and bh.id = ? and bh.userId = ?", this.MAP, new Object[] { Integer.valueOf(id), userId });
  }
  
  public Map<String, Object> getPage(String configName, String listID, int pageNum, int numPerPage, String orderField, String orderDirection, String startDate, String endDate, int billTypeId, int isRCW, String unitId, String staffId, String productId, String sgeId, String depmId, String autsId, String isMember, String billCode, String remark, String memo, String isCoupon, String priceCase, String price, String discountCase, String discount, String taxrateCase, String taxRate)
    throws Exception
  {
    init();
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    if (StringUtils.isNotBlank(startDate)) {
      sb.append(" and bh.recodeDate >= '" + startDate + "'");
    }
    if (StringUtils.isNotBlank(endDate)) {
      sb.append(" and bh.recodeDate <= '" + endDate + " 23:59:59'");
    }
    if (billTypeId > 0)
    {
      sb.append(" and bh.billTypeId = ?");
      paras.add(Integer.valueOf(billTypeId));
    }
    if (isRCW == 0) {
      sb.append(" and bh.isRCW = 0");
    }
    if (!StringUtil.isNull(unitId))
    {
      sb.append(" and bh.unitId = ?");
      paras.add(unitId);
    }
    if ((!StringUtil.isNull(staffId)) && (!"0".equals(staffId)))
    {
      sb.append(" and bh.staffId = ?");
      paras.add(staffId);
    }
    if (!StringUtil.isNull(depmId))
    {
      sb.append(" and bh.departmentId = ?");
      paras.add(depmId);
    }
    if (!StringUtil.isNull(billCode)) {
      sb.append(" and bh.billCode LIKE '%" + billCode + "%'");
    }
    if (!StringUtil.isNull(remark)) {
      sb.append(" and bh.remark LIKE '%" + remark + "%'");
    }
    if (!StringUtil.isNull(memo)) {
      sb.append(" and bh.memo LIKE '%" + memo + "%'");
    }
    "0".equals(isMember);
    if ((!StringUtil.isNull(productId)) || (!StringUtil.isNull(sgeId)) || (!"0".equals(priceCase)) || (!"0".equals(discountCase)) || (!"0".equals(taxrateCase)))
    {
      sb.append(" and (bh.billId,bh.billTypeId) IN ");
      sb.append("(");
      sb.append("SELECT billId,billTypeId FROM cc_stock_records WHERE 1 = 1");
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
      sb.append(" and (bh.billId,bh.billTypeId) IN ");
      sb.append("(");
      sb.append("SELECT billId,billTypeId FROM cw_pay_type WHERE 1 = 1");
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
        sb.append(" and (bh.billId,bh.billTypeId) IN ");
      } else if ("2".equals(isCoupon)) {
        sb.append(" and (bh.billId,bh.billTypeId) NOT IN ");
      }
      sb.append("(");
      sb.append("SELECT billId,billTypeId FROM cw_pay_type WHERE 1 = 1");
      sb.append(" AND accountId = 42 ");
      sb.append(" GROUP BY billId,billTypeId");
      sb.append(")");
    }
    if ("producer".equals(orderField)) {
      orderField = "prod.username";
    }
    if ("postUsre".equals(orderField)) {
      orderField = "post.username";
    }
    if ("memo".equals(orderField)) {
      orderField = "bh.memo";
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    

    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, this.SELECTSQL, sb.toString(), this.MAP, paras.toArray());
  }
  
  public List<Model> getList(String configName, String startDate, String endDate, int[] billTypes)
  {
    String billType = "";
    if (billTypes != null) {
      for (int i = 0; i < billTypes.length; i++)
      {
        if (i != 0) {
          billType = billType + ",";
        }
        billType = billType + billTypes[i];
      }
    }
    StringBuffer sql = new StringBuffer("select * from bb_billhistory where 1=1");
    if (StringUtils.isNotBlank(billType)) {
      sql.append(" and billTypeId in(" + billType + ")");
    }
    if (startDate != null) {
      sql.append(" and postTime >='" + startDate + "'");
    }
    if (endDate != null) {
      sql.append(" and postTime <'" + endDate + "'");
    }
    sql.append(" order by postTime");
    return find(configName, sql.toString());
  }
}
