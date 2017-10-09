package com.aioerp.model.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.util.DateUtils;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ArapAgeAnalysisReports
  extends BaseDbModel
{
  public static final ArapAgeAnalysisReports dao = new ArapAgeAnalysisReports();
  
  public Map<String, Object> arapAgeAnalysis(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    String aimDiv = (String)paraMap.get("aimDiv");
    String payOrGet = paraMap.get("payOrGet").toString();
    Date stopDate = DateUtils.addDays(DateUtils.parseDate(paraMap.get("stopDate").toString()), 1);
    String unitIds = paraMap.get("unitIds").toString();
    unitIds = unitIds.substring(1, unitIds.length() - 1);
    
    String unitPrivs = BaseController.basePrivs(BaseController.UNIT_PRIVS);
    Map<String, String> privsMap = new HashMap();
    privsMap.put("unitPrivs", unitPrivs);
    

    Integer day1 = (Integer)paraMap.get("day1");
    Integer day2 = (Integer)paraMap.get("day2");
    Integer day3 = (Integer)paraMap.get("day3");
    Integer day4 = (Integer)paraMap.get("day4");
    Integer day5 = (Integer)paraMap.get("day5");
    Integer day6 = (Integer)paraMap.get("day6");
    Integer day7 = (Integer)paraMap.get("day7");
    Integer day8 = (Integer)paraMap.get("day8");
    Integer day9 = (Integer)paraMap.get("day9");
    Integer day10 = (Integer)paraMap.get("day10");
    Integer day11 = (Integer)paraMap.get("day11");
    Integer day12 = (Integer)paraMap.get("day12");
    Integer day13 = (Integer)paraMap.get("day13");
    Date newStopDate = stopDate;
    

    String day1Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day1.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day1.intValue());
    
    String day2Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day2.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day2.intValue());
    
    String day3Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day3.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day3.intValue());
    
    String day4Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day4.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day4.intValue());
    
    String day5Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day5.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day5.intValue());
    
    String day6Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day6.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day6.intValue());
    
    String day7Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day7.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day7.intValue());
    
    String day8Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day8.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day8.intValue());
    
    String day9Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day9.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day9.intValue());
    
    String day10Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day10.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day10.intValue());
    
    String day11Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day11.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day11.intValue());
    
    String day12Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day12.intValue()), newStopDate, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day12.intValue());
    
    String day13Sql = commonSqlCreat(privsMap, DateUtils.addDays(newStopDate, 0 - day13.intValue()), null, payOrGet);
    newStopDate = DateUtils.addDays(stopDate, 0 - day13.intValue());
    

    String countMoneySql = commonSqlCreat(privsMap, null, null, payOrGet);
    



    StringBuffer selectSql = new StringBuffer("select temp.* ");
    StringBuffer fromSql = new StringBuffer(" from (select unit.*, ");
    fromSql.append(day1Sql + " money1,");
    fromSql.append(day2Sql + " money2,");
    fromSql.append(day3Sql + " money3,");
    fromSql.append(day4Sql + " money4,");
    fromSql.append(day5Sql + " money5,");
    fromSql.append(day6Sql + " money6,");
    fromSql.append(day7Sql + " money7,");
    fromSql.append(day8Sql + " money8,");
    fromSql.append(day9Sql + " money9,");
    fromSql.append(day10Sql + " money10,");
    fromSql.append(day11Sql + " money11,");
    fromSql.append(day12Sql + " money12,");
    fromSql.append(day13Sql + " money13,");
    fromSql.append(countMoneySql + " countMoney");
    fromSql.append(" from b_unit unit where unit.id in(" + unitIds + ")");
    fromSql.append(") temp where 1=1");
    if (aimDiv.equals("hasData")) {
      fromSql.append(" and temp.countMoney !=0");
    } else if (aimDiv.equals("hasNoData")) {
      fromSql.append(" and temp.countMoney =0 or temp.countMoney is null");
    }
    fromSql.append(" group by id order by " + orderField + " " + orderDirection);
    Map<String, Object> pageMap = aioGetPageRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), null, new Object[0]);
    return pageMap;
  }
  
  public String commonSqlCreat(Map<String, String> prvisMap, Date startDate1, Date endDate1, String payOrGet)
    throws ParseException
  {
    String startDate = DateUtils.format(startDate1);
    String endDate = null;
    if (endDate1 != null) {
      endDate = DateUtils.format(endDate1);
    }
    StringBuffer getSql = new StringBuffer("select u.*,case when o.type=1 then o.settlementAmount else null end as addMoney,case when o.type=0 then o.settlementAmount else null end as subMoney from cw_pay_by_order o left join b_unit u on u.id=o.unitId where 1=1");
    if ((startDate != null) && (endDate != null)) {
      getSql.append(" and o.updateTime between '" + startDate + "' and '" + endDate + "'");
    } else if (endDate1 == null) {
      getSql.append(" and o.updateTime < '" + startDate + "'");
    }
    ComFunController.basePrivsSql(getSql, (String)prvisMap.get("unitPrivs"), "u", "id");
    
    StringBuffer getOrPaySql = new StringBuffer("select u.*,r.addMoney,r.subMoney from cw_arap_records r left join b_unit u on u.id=r.unitId where 1=1");
    getOrPaySql.append(" and r.isRCW=" + AioConstants.RCW_NO);
    if ((startDate != null) && (endDate != null)) {
      getOrPaySql.append(" and r.recodeTime between '" + startDate + "' and '" + endDate + "'");
    } else if (endDate1 == null) {
      getOrPaySql.append(" and r.recodeTime < '" + startDate + "'");
    }
    ComFunController.basePrivsSql(getSql, (String)prvisMap.get("unitPrivs"), "u", "id");
    

    StringBuffer sql = new StringBuffer("(");
    if (payOrGet.equals("payOrGet"))
    {
      sql.append("select abs(sum(ifnull(tb.addMoney,0)-ifnull(tb.subMoney,0))) from ( ");
      sql.append(getSql);
      sql.append(" union all ");
      sql.append(getOrPaySql);
      sql.append(" ) tb where tb.id=unit.id");
    }
    else if (payOrGet.equals("get"))
    {
      sql.append("select sum(ifnull(tb.addMoney,0)-ifnull(tb.subMoney,0)) from ( ");
      sql.append(getSql);
      sql.append(" union all ");
      sql.append(getOrPaySql);
      sql.append(" ) tb where tb.id=unit.id");
    }
    else if (payOrGet.equals("pay"))
    {
      sql.append("select sum(ifnull(tb.subMoney,0)-ifnull(tb.addMoney,0)) from ( ");
      sql.append(getSql);
      sql.append(" union all ");
      sql.append(getOrPaySql);
      sql.append(" ) tb where tb.id=unit.id");
    }
    sql.append(")");
    return sql.toString();
  }
}
