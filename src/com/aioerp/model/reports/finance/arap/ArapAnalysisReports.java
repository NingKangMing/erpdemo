package com.aioerp.model.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArapAnalysisReports
  extends BaseDbModel
{
  public static final ArapAnalysisReports dao = new ArapAnalysisReports();
  
  public Map<String, Object> arapAnalysis(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String startDate = paraMap.get("startTime").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endTime").toString());
    String aimDiv = (String)paraMap.get("aimDiv");
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    BigDecimal totalGetDown = new BigDecimal(paraMap.get("totalGetDown").toString());
    BigDecimal totalGetUp = new BigDecimal(paraMap.get("totalGetUp").toString());
    BigDecimal totalPayDown = new BigDecimal(paraMap.get("totalPayDown").toString());
    BigDecimal totalPayUp = new BigDecimal(paraMap.get("totalPayUp").toString());
    
    String unitPrivs = BaseController.basePrivs(BaseController.UNIT_PRIVS);
    String departmentPrivs = BaseController.basePrivs(BaseController.DEPARTMENT_PRIVS);
    String staffPrivs = BaseController.basePrivs(BaseController.STAFF_PRIVS);
    String storagePrivs = BaseController.basePrivs(BaseController.STORAGE_PRIVS);
    String productPrivs = BaseController.basePrivs(BaseController.PRODUCT_PRIVS);
    String accountPrivs = BaseController.basePrivs(BaseController.ACCOUNT_PRIVS);
    Map<String, String> privsMap = new HashMap();
    privsMap.put("unitPrivs", unitPrivs);
    privsMap.put("departmentPrivs", departmentPrivs);
    privsMap.put("staffPrivs", staffPrivs);
    privsMap.put("storagePrivs", storagePrivs);
    privsMap.put("productPrivs", productPrivs);
    privsMap.put("accountPrivs", accountPrivs);
    

    StringBuffer totalGetSql = historySql("add", privsMap, startDate, endDate);
    
    StringBuffer totalPaySql = historySql("sub", privsMap, startDate, endDate);
    



    StringBuffer singleSql = new StringBuffer();
    
    StringBuffer selectSql = new StringBuffer("select tt.*,");
    selectSql.append("case when tt.totalGet1+tt.beginGetMoney>tt.totalPay1+tt.beginPayMoney then tt.totalGet1+tt.beginGetMoney-tt.totalPay1-tt.beginPayMoney else 0 end as totalGet,");
    selectSql.append("case when tt.totalPay1+tt.beginPayMoney>tt.totalGet1+tt.beginGetMoney then tt.totalPay1+tt.beginPayMoney-tt.totalGet1-tt.beginGetMoney else 0 end as totalPay");
    singleSql.append(" from (");
    singleSql.append(" select t.billId,t.id,t.code,t.fullName,t.rank,t.node,t.getMoneyLimit,t.payMoneyLimit,ifnull(t.beginGetMoney,0) beginGetMoney,ifnull(t.beginPayMoney,0) beginPayMoney,");
    singleSql.append("sum(t.inTaxMoneys) inTaxMoneys,sum(t.outTaxMoneys) outTaxMoneys,sum(t.payMoney) payMoney,sum(t.getMoney) getMoney," + totalGetSql + " totalGet1," + totalPaySql + " totalPay1");
    singleSql.append(" from (");
    if (!aimDiv.equals("in"))
    {
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,bill.taxMoneys inTaxMoneys, 0 outTaxMoneys,bill.payMoney payMoney,0 getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "cg_purchase_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "cg_purchase_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,(bill.taxMoneys*-1) inTaxMoneys, 0 outTaxMoneys,(bill.payMoney*-1) payMoney,0 getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "cg_return_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "cg_return_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,bill.gapMoney inTaxMoneys, 0 outTaxMoneys,bill.payMoney payMoney,0 getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "cg_barter_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "cg_barter_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,0 billId,bill.recodeDate,0 inTaxMoneys, 0 outTaxMoneys,bill.moneys payMoney,0 getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "cw_paymoney_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "cw_paymoney_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
    }
    if (!aimDiv.equals("out"))
    {
      if (!aimDiv.equals("in")) {
        singleSql.append(" union all");
      }
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,0 inTaxMoneys, bill.taxMoneys outTaxMoneys,0 payMoney,bill.payMoney getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "xs_sell_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "xs_sell_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,0 inTaxMoneys, (bill.taxMoneys*-1) outTaxMoneys,0 payMoney,(bill.payMoney*-1) getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "xs_return_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "xs_return_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,0 inTaxMoneys, bill.gapMoney outTaxMoneys,0 payMoney,bill.payMoney getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "xs_barter_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "xs_barter_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,0 billId,bill.recodeDate,0 inTaxMoneys, 0 outTaxMoneys,0 payMoney,bill.moneys getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "cw_getmoney_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "cw_getmoney_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
    }
    singleSql.append(") t ");
    ComFunController.basePrivsSql(singleSql, (String)privsMap.get("unitPrivs"), "t", "id");
    singleSql.append(" group by id order by " + orderField + " " + orderDirection);
    singleSql.append(") tt");
    
    singleSql.append(" where (tt.totalGet1-tt.totalPay1) between " + totalGetDown + " and " + totalGetUp);
    singleSql.append(" and (tt.totalPay1-tt.totalGet1) between " + totalPayDown + " and " + totalPayUp);
    singleSql.append(" and tt.node=" + AioConstants.NODE_1);
    

    Map<String, Object> pageMap = aioGetPageRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), singleSql.toString(), null, new Object[0]);
    if ((pageMap != null) && (pageMap.get("pageList") != null))
    {
      List<Record> list = (List)pageMap.get("pageList");
      for (int i = 0; i < list.size(); i++)
      {
        BigDecimal getMoneyLimit = ((Record)list.get(i)).getBigDecimal("getMoneyLimit");
        BigDecimal payMoneyLimit = ((Record)list.get(i)).getBigDecimal("payMoneyLimit");
        BigDecimal totalGet = ((Record)list.get(i)).getBigDecimal("totalGet");
        BigDecimal totalPay = ((Record)list.get(i)).getBigDecimal("payMoneyLimit");
        BigDecimal get = BigDecimalUtils.sub(totalGet, totalPay);
        if (BigDecimalUtils.compare(get, BigDecimal.ZERO) == 1)
        {
          if ((getMoneyLimit != null) && (BigDecimalUtils.compare(get, getMoneyLimit) == 1)) {
            ((Record)list.get(i)).set("hasOverGet", "yes");
          }
        }
        else
        {
          BigDecimal pay = BigDecimalUtils.negate(get);
          if ((payMoneyLimit != null) && (BigDecimalUtils.compare(pay, payMoneyLimit) == 1)) {
            ((Record)list.get(i)).set("hasOverPay", "yes");
          }
        }
      }
      pageMap.put("pageList", list);
    }
    return pageMap;
  }
  
  public StringBuffer commonSql(StringBuffer singleSql, String aimDiv)
  {
    if (aimDiv.equals("inOrOut")) {
      singleSql.append(" and bill.id>0");
    } else if (aimDiv.equals("in")) {
      singleSql.append(" and bill.id>0");
    } else if (aimDiv.equals("out")) {
      singleSql.append(" and bill.id>0");
    } else if (aimDiv.equals("no")) {
      singleSql.append(" and bill.id is null");
    }
    return singleSql;
  }
  
  public StringBuffer historySql(String type, Map<String, String> prvisMap, String startDate, String endDate)
  {
    StringBuffer sql = new StringBuffer();
    if (type.equals("add")) {
      sql.append("(select ifnull(sum(arap.addMoney),0)");
    } else {
      sql.append("(select ifnull(sum(arap.subMoney),0)");
    }
    sql.append(" from cw_arap_records arap where arap.unitId=t.id and arap.isRCW=" + AioConstants.RCW_NO + " and arap.createTime between '" + startDate + "' and '" + endDate + "')");
    ComFunController.basePrivsSql(sql, (String)prvisMap.get("unitPrivs"), "arap", "unitId");
    return sql;
  }
  
  private void childComSql(StringBuffer singleSql, String billTableIsLeft, String billTable, String startDate, String endDate)
  {
    if ((billTableIsLeft == null) || (billTableIsLeft.equals(""))) {
      billTableIsLeft = "yes";
    }
    if (billTableIsLeft.equals("yes")) {
      singleSql.append(" from " + billTable + " bill left join b_unit unit on unit.id=bill.unitId and bill.recodeDate between '" + startDate + "' and '" + endDate + "'");
    } else {
      singleSql.append(" from b_unit unit left join " + billTable + " bill on unit.id=bill.unitId and bill.recodeDate between '" + startDate + "' and '" + endDate + "'");
    }
  }
  
  public List<Record> limitCurrentUnitUpMoney(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String startDate = paraMap.get("startTime").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endTime").toString());
    String aimDiv = (String)paraMap.get("aimDiv");
    BigDecimal totalGetDown = new BigDecimal(paraMap.get("totalGetDown").toString());
    BigDecimal totalGetUp = new BigDecimal(paraMap.get("totalGetUp").toString());
    BigDecimal totalPayDown = new BigDecimal(paraMap.get("totalPayDown").toString());
    BigDecimal totalPayUp = new BigDecimal(paraMap.get("totalPayUp").toString());
    
    String unitPrivs = BaseController.basePrivs(BaseController.UNIT_PRIVS);
    Map<String, String> privsMap = new HashMap();
    privsMap.put("unitPrivs", unitPrivs);
    


    StringBuffer totalGetSql = historySql("add", privsMap, startDate, endDate);
    
    StringBuffer totalPaySql = historySql("sub", privsMap, startDate, endDate);
    



    StringBuffer singleSql = new StringBuffer();
    
    StringBuffer selectSql = new StringBuffer("select tt.*,");
    selectSql.append("case when tt.totalGet1>tt.totalPay1 then tt.totalGet1-tt.totalPay1 else 0 end as totalGet,");
    selectSql.append("case when tt.totalPay1>tt.totalGet1 then tt.totalPay1-tt.totalGet1 else 0 end as totalPay");
    singleSql.append(" from (");
    singleSql.append(" select t.billId,t.id,t.code,t.fullName,t.rank,ifnull(t.getMoneyLimit,0) getMoneyLimit,ifnull(t.payMoneyLimit,0) payMoneyLimit,");
    singleSql.append("sum(t.inTaxMoneys) inTaxMoneys,sum(t.outTaxMoneys) outTaxMoneys,sum(t.payMoney) payMoney,sum(t.getMoney) getMoney," + totalGetSql + " totalGet1," + totalPaySql + " totalPay1");
    singleSql.append(" from (");
    if (!aimDiv.equals("in"))
    {
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,bill.taxMoneys inTaxMoneys, 0 outTaxMoneys,bill.payMoney payMoney,0 getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "cg_purchase_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "cg_purchase_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,(bill.taxMoneys*-1) inTaxMoneys, 0 outTaxMoneys,(bill.payMoney*-1) payMoney,0 getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "cg_return_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "cg_return_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,bill.gapMoney inTaxMoneys, 0 outTaxMoneys,bill.payMoney payMoney,0 getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "cg_barter_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "cg_barter_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,0 billId,bill.recodeDate,0 inTaxMoneys, 0 outTaxMoneys,bill.moneys payMoney,0 getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "cw_paymoney_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "cw_paymoney_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
    }
    if (!aimDiv.equals("out"))
    {
      if (!aimDiv.equals("in")) {
        singleSql.append(" union all");
      }
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,0 inTaxMoneys, bill.taxMoneys outTaxMoneys,0 payMoney,bill.payMoney getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "xs_sell_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "xs_sell_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,0 inTaxMoneys, (bill.taxMoneys*-1) outTaxMoneys,0 payMoney,(bill.payMoney*-1) getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "xs_return_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "xs_return_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,bill.id billId,bill.recodeDate,0 inTaxMoneys, bill.gapMoney outTaxMoneys,0 payMoney,bill.payMoney getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "xs_barter_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "xs_barter_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
      
      singleSql.append(" union all");
      singleSql.append(" select unit.*,0 billId,bill.recodeDate,0 inTaxMoneys, 0 outTaxMoneys,0 payMoney,bill.moneys getMoney");
      if ((aimDiv.equals("all")) || (aimDiv.equals("no"))) {
        childComSql(singleSql, "no", "cw_getmoney_bill", startDate, endDate);
      } else {
        childComSql(singleSql, "yes", "cw_getmoney_bill", startDate, endDate);
      }
      singleSql = commonSql(singleSql, aimDiv);
    }
    singleSql.append(") t ");
    ComFunController.basePrivsSql(singleSql, (String)privsMap.get("unitPrivs"), "unit", "id");
    singleSql.append(" group by id");
    singleSql.append(") tt");
    
    singleSql.append(" where (tt.totalGet1-tt.totalPay1) between " + totalGetDown + " and " + totalGetUp);
    singleSql.append(" and (tt.totalPay1-tt.totalGet1) between " + totalPayDown + " and " + totalPayUp);
    
    List<Record> list = Db.use(configName).find(singleSql.toString());
    return list;
  }
}
