package com.aioerp.model.bought;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseBarterBill
  extends BaseDbModel
{
  public static final PurchaseBarterBill dao = new PurchaseBarterBill();
  public static final String TABLE_NAME = "cg_barter_bill";
  private Unit billUnit;
  private Staff billStaff;
  private Storage billOutStorage;
  private Storage billInStorage;
  private Department billDepartment;
  
  public Unit getBillUnit(String configName)
  {
    if (this.billUnit == null) {
      this.billUnit = ((Unit)Unit.dao.findById(configName, get("unitId")));
    }
    return this.billUnit;
  }
  
  public Staff getBillStaff(String configName)
  {
    if (this.billStaff == null) {
      this.billStaff = ((Staff)Staff.dao.findById(configName, get("staffId")));
    }
    return this.billStaff;
  }
  
  public Storage getBillOutStorage(String configName)
  {
    if (this.billOutStorage == null) {
      this.billOutStorage = ((Storage)Storage.dao.findById(configName, get("outStorageId")));
    }
    return this.billOutStorage;
  }
  
  public Storage getBillInStorage(String configName)
  {
    if (this.billInStorage == null) {
      this.billInStorage = ((Storage)Storage.dao.findById(configName, get("inStorageId")));
    }
    return this.billInStorage;
  }
  
  public Department getBillDepartment(String configName)
  {
    if (this.billDepartment == null) {
      this.billDepartment = ((Department)Department.dao.findById(configName, get("departmentId")));
    }
    return this.billDepartment;
  }
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return 
      Db.use(configName).queryLong("select count(*) from cg_barter_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, String orderField, String orderDirection)
  {
    String sql = " from cg_barter_bill";
    if (StringUtils.isNotBlank(orderField)) {
      sql = sql + " order by " + orderField + " " + orderDirection;
    }
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[0]);
  }
  
  public static BigDecimal getAllowBarterMoney(String configName, int unitId, String recodeTime, String type)
  {
    Unit unit = (Unit)Unit.dao.findById(configName, Integer.valueOf(unitId));
    Integer limitDay = unit.getInt("replacePrdPeriod");
    if (limitDay == null) {
      limitDay = Integer.valueOf(0);
    }
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    try
    {
      date = sdf.parse(recodeTime);
    }
    catch (ParseException e)
    {
      e.printStackTrace();
      return BigDecimal.ZERO;
    }
    date = DateUtils.addDays(date, limitDay.intValue() * -1);
    String startDate = sdf.format(date);
    
    BigDecimal ratio = unit.getBigDecimal("replacePrdPercentage");
    if (ratio == null) {
      ratio = new BigDecimal(100);
    }
    ratio = BigDecimalUtils.div(ratio, 100);
    
    BigDecimal allowBarterMoney = BigDecimal.ZERO;
    
    String str = "";
    if (limitDay.intValue() > 0) {
      str = " AND recodeDate>='" + startDate + "'";
    }
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ");
    if (type.equals("in"))
    {
      sql.append("(SELECT IFNULL(SUM(taxMoneys),0) FROM cg_purchase_bill WHERE unitId = bunit.id " + str + ") dealMoney");
      sql.append(",");
      sql.append("(SELECT IFNULL(SUM(taxMoneys),0) FROM cg_return_bill WHERE unitId = bunit.id   " + str + ") returnMoney");
      sql.append(",");
      sql.append("(SELECT IFNULL(SUM(outMoney),0) FROM cg_barter_bill WHERE unitId = bunit.id    " + str + ") barterMoney");
    }
    else if (type.equals("out"))
    {
      sql.append("(SELECT IFNULL(SUM(taxMoneys),0) FROM xs_sell_bill WHERE unitId = bunit.id     " + str + ") dealMoney");
      sql.append(",");
      sql.append("(SELECT IFNULL(SUM(taxMoneys),0) FROM xs_return_bill WHERE unitId = bunit.id   " + str + ") returnMoney");
      sql.append(",");
      sql.append("(SELECT IFNULL(SUM(inMoney),0) FROM  xs_barter_bill WHERE unitId = bunit.id   " + str + ") barterMoney");
    }
    sql.append(" FROM b_unit bunit WHERE bunit.id = " + unitId);
    Record record = Db.use(configName).findFirst(sql.toString());
    BigDecimal dealMoney = record.getBigDecimal("dealMoney");
    BigDecimal returnMoney = record.getBigDecimal("returnMoney");
    BigDecimal barterMoney = record.getBigDecimal("barterMoney");
    
    allowBarterMoney = BigDecimalUtils.sub(BigDecimalUtils.mul(BigDecimalUtils.sub(dealMoney, returnMoney), ratio), barterMoney);
    return allowBarterMoney;
  }
}
