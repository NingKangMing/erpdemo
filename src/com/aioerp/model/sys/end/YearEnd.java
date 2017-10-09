package com.aioerp.model.sys.end;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class YearEnd
  extends BaseDbModel
{
  public static YearEnd dao = new YearEnd();
  
  public Map<String, Object> validateBillDate(String configName, Timestamp billDate)
  {

	Map<String, Object> map = new HashMap();
    map.put("statusCode", Integer.valueOf(200));
    if (billDate == null)
    {
      map.put("statusCode", Integer.valueOf(300));
      map.put("message", "录单日期不能为空,不能过账");
      return map;
    }
    String hasOpenAccountTime = AioerpSys.dao.getValue1(configName, "hasOpenAccountTime");
    if ((hasOpenAccountTime == null) || (hasOpenAccountTime.equals("")))
    {
      map.put("statusCode", Integer.valueOf(300));
      map.put("message", "未开账");
      return map;
    }
    Date date = null;
    try
    {
      date = DateUtils.parseDate(hasOpenAccountTime);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      map.put("statusCode", Integer.valueOf(300));
      map.put("message", "开账日期转换出错");
      return map;
    }
    boolean flag = billDate.after(date);
    if (!flag)
    {
      map.put("statusCode", Integer.valueOf(300));
      map.put("message", "录单日期小于开账日期,不能过账");
      return map;
    }
    Model monthModel = MonthEnd.dao.lastModel(configName);
    if (monthModel != null)
    {
      Date mydate = DateUtils.addDays(monthModel.getDate("endDate"), 1);
      boolean myflag = billDate.after(date);
      if (!myflag)
      {
        map.put("statusCode", Integer.valueOf(300));
        map.put("message", "录单日期小于月结存时间或还未开账,不能过账");
        return map;
      }
    }
    Model yearModel = dao.lastModel(configName);
    if (yearModel != null)
    {
      Date mydate = DateUtils.addDays(yearModel.getDate("endDate"), 1);
      boolean myflag = billDate.after(mydate);
      if (!myflag)
      {
        map.put("statusCode", Integer.valueOf(300));
        map.put("message", "录单日期小于年结存时间或还未开账,不能过账");
        return map;
      }
    }
    return map;
  }
  
  public Model lastModel(String configName)
  {
    return findFirst(configName, "select * from sys_yearend order by endDate desc");
  }
  
  public List<Model> getList(String configName)
  {
    return find(configName, "select * from sys_yearend");
  }
  
  public List<Model> getList(String configName, Integer[] ids)
  {
    StringBuffer sql = new StringBuffer("select * from sys_yearend where 1=1");
    String id = "";
    for (int i = 0; i < ids.length; i++)
    {
      if (i != 0) {
        id = id + ",";
      }
      id = id + ids[i].toString();
    }
    sql.append(" and id in (" + id + ")");
    return find(configName, sql.toString());
  }
  
  public boolean removeDraftAndBook(String configName)
    throws SQLException
  {
    Db.use(configName).update("delete bill,detail from cg_bought_bill as bill join cg_bought_detail as detail on detail.billId=bill.id where bill.status!=" + AioConstants.STATUS_DISABLE);
    Db.use(configName).update("delete bill,detail from xs_sellbook_bill as bill join xs_sellbook_detail as detail on detail.billId=bill.id where bill.relStatus!=" + AioConstants.STATUS_DISABLE);
    


    Connection con = DbKit.getConfig(configName).getConnection();
    try
    {
      DatabaseMetaData databaseMetaData = con.getMetaData();
      ResultSet rs = null;
      
      rs = databaseMetaData.getTables("", "", "", null);
      while (rs.next())
      {
        String tableType = rs.getString("TABLE_TYPE");
        String tableNmae = rs.getString("TABLE_NAME");
        if ((tableType != null) && (tableType.equals("TABLE")) && (tableNmae.indexOf("draft") != -1)) {
          Db.use(configName).update("delete from " + tableNmae);
        }
      }
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      con.setAutoCommit(false);
      con.rollback();
    }
    return false;
  }
  
  public boolean removeOneYearEnd(String configName)
    throws SQLException
  {
    Map<String, String> map = new HashMap();
    map.put("aioerp_sys_backup", "系统备份表");
    map.put("aioerp_file", "商品  职员  图片");
    map.put("sys_user", "");
    map.put("aioerp_sys", "系统配置");
    map.put("b_accounts", "基本信息-会计科目");
    map.put("b_area", "基本信息-地区");
    map.put("b_department", "基本信息-部门");
    map.put("b_product", "基本信息-商品");
    map.put("b_product_unit", "基本信息-商品对应单位");
    map.put("b_staff", "基本信息-职员");
    map.put("b_storage", "基本信息-仓库");
    map.put("b_unit", "基本信息-单位");
    map.put("cc_stock_init", "期初库存");
    map.put("om_port_record", "OM订单接口用户");
    map.put("om_port_record_detail", "OM订单接口用户");
    map.put("om_port_user", "OM订单接口用户");
    map.put("sys_bill_row", "单据列配置");
    map.put("sys_billcode_flow", "单据流水号Id");
    map.put("sys_billcodeconfig", "单据编号格式配置");
    map.put("sys_billcodeconfig_format", "格式定义表");
    map.put("sys_billsort", "单据类别");
    map.put("sys_billtype", "单据类型");
    map.put("aioerp_sys_user", "系统单位用户");
    map.put("aioerp_sys_config", "系统用户配置");
    map.put("fz_report_row", "报表列配置");
    map.put("delivery_company", "快递公司");
    map.put("sys_permission_type", "权限类型");
    map.put("sys_permission", "权限");
    
    map.put("cg_bought_bill", "进货订单");
    map.put("cg_bought_detail", "进货订单明细");
    map.put("xs_sellbook_bill", "销售订单");
    map.put("xs_sellbook_detail", "销售订单明细");
    
    map.put("bb_businessdraft", "业务草稿库");
    


    Connection con = DbKit.getConfig(configName).getConnection();
    try
    {
      DatabaseMetaData databaseMetaData = con.getMetaData();
      ResultSet rs = null;
      
      rs = databaseMetaData.getTables("", "", "", null);
      while (rs.next())
      {
        String tableType = rs.getString("TABLE_TYPE");
        String tableNmae = rs.getString("TABLE_NAME");
        if ((tableType != null) && (tableType.equals("TABLE")) && (!map.containsKey(tableNmae)) && (tableNmae.indexOf("_draft") == -1)) {
          Db.use(configName).update("delete from " + tableNmae);
        }
      }
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      con.setAutoCommit(false);
      con.rollback();
    }
    return false;
  }
  
  public boolean yearEndInitData(String configName)
  {
    boolean flag = true;
    
    AioerpSys.dao.getObj(configName, "hasOpenAccount");
    AioerpSys.dao.sysSaveOrUpdate(configName, "hasOpenAccount", AioConstants.HAS_OPEN_ACCOUNT0, "是否开账");
    

    Db.use(configName).update("delete from cw_accounts_init");
    

    AioerpSys.dao.sysDelKey(configName, "hasOpenAccountTime");
    return flag;
  }
}
