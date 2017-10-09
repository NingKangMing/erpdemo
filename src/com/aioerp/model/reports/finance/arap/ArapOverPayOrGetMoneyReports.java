package com.aioerp.model.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.util.DateUtils;
import com.aioerp.util.Format;
import com.aioerp.util.StringUtil;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public class ArapOverPayOrGetMoneyReports
  extends BaseDbModel
{
  public static final ArapOverPayOrGetMoneyReports dao = new ArapOverPayOrGetMoneyReports();
  
  public Map<String, Object> arapOverPayOrGetMoney(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    String modelType = (String)paraMap.get("modelType");
    String warn = (String)paraMap.get("warn");
    Integer unitId = (Integer)paraMap.get("unitId");
    String stopDate = StringUtil.dataStrToStr(paraMap.get("stopDate").toString());
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    Integer aheadDay = (Integer)paraMap.get("aheadDay");
    if (aheadDay != null) {
      stopDate = Format.getDate(DateUtils.addDays(DateUtils.parseDate(stopDate), aheadDay.intValue()));
    }
    StringBuffer selectSql = new StringBuffer("select t.* ");
    StringBuffer fromSql = null;
    
    StringBuffer settlementSql = new StringBuffer("select sum(ifnull(settlementAmount,0)) ");
    if (modelType.equals("get")) {
      settlementSql.append(" from cw_getmoney_bill g");
    } else if (modelType.equals("pay")) {
      settlementSql.append(" from cw_paymoney_bill g");
    }
    settlementSql.append(" left join cw_pay_by_order o on o.cwBillId=g.id");
    settlementSql.append(" left join b_unit u on u.id=g.unitId  where 1=1 and g.recodeDate <'" + stopDate + "'");
    if ((unitId != null) && (unitId.intValue() != 0)) {
      settlementSql.append(" and u.pids like '%{" + unitId + "}%'");
    }
    ComFunController.basePrivsSql(settlementSql, BaseController.basePrivs(BaseController.UNIT_PRIVS), "u", "id");
    if (modelType.equals("get"))
    {
      settlementSql.append(" and o.type=" + AioConstants.GET_MONEY + " and billTypeId=" + AioConstants.BILL_ROW_TYPE4);
      
      fromSql = new StringBuffer(" from (select " + AioConstants.BILL_ROW_TYPE4 + " billTypeId,'销售单' navTabTitile,unit.code unitCode,unit.fullName unitFullName,bill.id billId,bill.code,bill.recodeDate,bill.deliveryDate termDate,bill.delayDeliveryDate delayTermDate,bill.taxMoneys,(ifnull(bill.payMoney,0)+ifnull(bill.privilege,0)+(" + settlementSql.toString() + ")) hasMoney,");
      fromSql.append("(ifnull(bill.taxMoneys,0)-ifnull(bill.payMoney,0)-ifnull(bill.privilege,0)-(" + settlementSql.toString() + ")) hasNoMoney,bill.isWarn");
      fromSql.append(" from xs_sell_bill bill left join b_unit unit on unit.id=bill.unitId");
      fromSql.append(" where isRCW=" + AioConstants.RCW_NO);
      
      fromSql.append(" and (case when unit.settlePeriod is null then unit.id=0 else date_sub(bill.delayDeliveryDate,interval 0 day) < '" + stopDate + "'  end) ");
      if ((unitId != null) && (unitId.intValue() != 0)) {
        fromSql.append(" and unit.pids like '%{" + unitId + "}%'");
      }
    }
    else if (modelType.equals("pay"))
    {
      settlementSql.append(" and o.type=" + AioConstants.OUT_MONEY + " and billTypeId=" + AioConstants.BILL_ROW_TYPE5);
      
      fromSql = new StringBuffer(" from (select " + AioConstants.BILL_ROW_TYPE5 + " billTypeId,'进货单' navTabTitile,unit.code unitCode,unit.fullName unitFullName,bill.id billId,bill.code,bill.recodeDate,bill.payDate termDate,bill.delayDeliveryDate delayTermDate,bill.taxMoneys,(ifnull(bill.payMoney,0)+ifnull(bill.privilege,0)+(" + settlementSql.toString() + ")) hasMoney,");
      fromSql.append("(ifnull(bill.taxMoneys,0)-ifnull(bill.payMoney,0)-ifnull(bill.privilege,0)-(" + settlementSql.toString() + ")) hasNoMoney,bill.isWarn");
      fromSql.append(" from cg_purchase_bill bill left join b_unit unit on unit.id=bill.unitId");
      fromSql.append(" where isRCW=" + AioConstants.RCW_NO);
      
      fromSql.append(" and (case when unit.settlePeriod is null then unit.id=0 else date_sub(bill.delayDeliveryDate,interval 0 day) < '" + stopDate + "'  end) ");
      if ((unitId != null) && (unitId.intValue() != 0)) {
        fromSql.append(" and unit.pids like '%{" + unitId + "}%'");
      }
    }
    fromSql.append(") t");
    if (warn.equals("cancel")) {
      fromSql.append(" where t.isWarn=" + AioConstants.OVERGETPAY_WARN1);
    } else if (warn.equals("delay")) {
      fromSql.append(" where t.termDate <> t.delayTermDate");
    }
    fromSql.append(" order by " + orderField + " " + orderDirection);
    Map<String, Object> pageMap = aioGetPageRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), null, new Object[0]);
    return pageMap;
  }
  
  public String commonSqlCreat(Date startDate1, Date endDate1, String payOrGet)
    throws ParseException
  {
    String startDate = DateUtils.format(startDate1);
    String endDate = null;
    if (endDate1 != null) {
      endDate = DateUtils.format(endDate1);
    }
    StringBuffer sb = new StringBuffer("(");
    if (payOrGet.equals("payOrGet")) {
      sb.append("select sum(ifnull(r.addMoney,0)-ifnull(r.subMoney,0))");
    } else if (payOrGet.equals("get")) {
      sb.append("select sum(ifnull(r.addMoney,0))");
    } else if (payOrGet.equals("pay")) {
      sb.append("select sum(ifnull(r.subMoney,0))");
    }
    sb.append(" from cw_arap_records r where r.isRCW=" + AioConstants.RCW_NO + " and r.unitId =unit.id");
    if (endDate1 == null) {
      sb.append(" and r.recodeTime < '" + startDate + "'");
    } else {
      sb.append(" and r.recodeTime between '" + startDate + "' and '" + endDate + "'");
    }
    sb.append(")");
    return sb.toString();
  }
}
