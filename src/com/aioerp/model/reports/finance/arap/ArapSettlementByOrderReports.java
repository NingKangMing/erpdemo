package com.aioerp.model.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.finance.changegetorpay.ChangePayOrGetController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class ArapSettlementByOrderReports
  extends BaseDbModel
{
  public static final ArapSettlementByOrderReports dao = new ArapSettlementByOrderReports();
  
  public Map<String, Object> settlementByOrder(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String isOver = (String)paraMap.get("isOver");
    String billType = (String)paraMap.get("billType");
    Integer unitId = (Integer)paraMap.get("unitId");
    Integer staffId = (Integer)paraMap.get("staffId");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    
    StringBuffer selectSql = new StringBuffer("select t.* ");
    StringBuffer formSql = new StringBuffer(" from (");
    if (billType.equals("initGet"))
    {
      String initGetSql = "(select ifnull(u.beginGetMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE_1 + " and o.unitId=u.id) lastMoney";
      formSql.append(" select '期初应收' orderTypeName," + AioConstants.BILL_ROW_TYPE_1 + " billTypId," + AioConstants.RCW_NO + " isRCW,u.id unitId,0 staffId, null id,'本年期初应收' code,null recodeDate,null deliveryDate,concat(u.fullName,'本年期初应收') remark,u.beginGetMoney taxMoneys,0 privilege,0 accountMoney," + initGetSql + " from b_unit u where 1=1 ");
      if ((unitId != null) && (unitId.intValue() != 0)) {
        formSql.append(" and u.id=" + unitId);
      }
    }
    else if (billType.equals("initPay"))
    {
      String initPaySql = "(select ifnull(u.beginPayMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE_2 + " and o.unitId=u.id) lastMoney";
      formSql.append(" select '期初应付' orderTypeName," + AioConstants.BILL_ROW_TYPE_2 + " billTypId," + AioConstants.RCW_NO + " isRCW,u.id unitId,0 staffId, null id,'本年期初应付' code,null recodeDate,null deliveryDate,concat(u.fullName,'本年期初应付') remark,u.beginPayMoney taxMoneys,0 privilege,0 accountMoney," + initPaySql + " from b_unit u where 1=1");
      if ((unitId != null) && (unitId.intValue() != 0)) {
        formSql.append(" and u.id=" + unitId);
      }
    }
    else if (billType.equals("5"))
    {
      String inSql = "(select ifnull(bill.taxMoneys,0)-ifnull(bill.privilege,0)-ifnull(bill.payMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE5 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '进货单' orderTypeName," + AioConstants.BILL_ROW_TYPE5 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,bill.payDate deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney," + inSql + " from cg_purchase_bill bill where 1=1");
    }
    else if (billType.equals("6"))
    {
      String inReturnSql = "(select ifnull(bill.taxMoneys*-1,0)-ifnull(bill.privilege*-1,0)-ifnull(bill.payMoney*-1,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE6 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '进货退货单' orderTypeName," + AioConstants.BILL_ROW_TYPE6 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney," + inReturnSql + " from cg_return_bill bill where 1=1");
    }
    else if (billType.equals("12"))
    {
      String inChangeSql = "(select ifnull(bill.gapMoney,0)-ifnull(bill.privilege,0)-ifnull(bill.payMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE12 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '进货换货单' orderTypeName," + AioConstants.BILL_ROW_TYPE12 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.gapMoney taxMoneys,bill.privilege,bill.payMoney accountMoney," + inChangeSql + " from cg_barter_bill bill where 1=1");
    }
    else if (billType.equals("4"))
    {
      String sellSql = "(select ifnull(bill.taxMoneys,0)-ifnull(bill.privilege,0)-ifnull(bill.payMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE4 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '销售单' orderTypeName," + AioConstants.BILL_ROW_TYPE4 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,bill.deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney," + sellSql + " from xs_sell_bill bill where 1=1");
    }
    else if (billType.equals("7"))
    {
      String sellReturnSql = "(select ifnull(bill.taxMoneys*-1,0)-ifnull(bill.privilege*-1,0)-ifnull(bill.payMoney*-1,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE7 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '销售退货单' orderTypeName," + AioConstants.BILL_ROW_TYPE7 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney," + sellReturnSql + " from xs_return_bill bill where 1=1");
    }
    else if (billType.equals("13"))
    {
      String sellChangeSql = "(select ifnull(bill.gapMoney,0)-ifnull(bill.privilege,0)-ifnull(bill.payMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE13 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '销售换货单' orderTypeName," + AioConstants.BILL_ROW_TYPE13 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.gapMoney taxMoneys,bill.privilege,bill.payMoney accountMoney," + sellChangeSql + " from xs_barter_bill bill where 1=1");
    }
    else if (billType.equals("18"))
    {
      String otherGetSql = "(select ifnull(bill.moneys,0)-ifnull(bill.getMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE18 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '其他收入单' orderTypeName," + AioConstants.BILL_ROW_TYPE18 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.getMoney accountMoney," + otherGetSql + " from cw_otherearn_bill bill where 1=1");
    }
    else if (billType.equals("22"))
    {
      String otherPaySql = "(select ifnull(bill.moneys,0)-ifnull(bill.getMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE22 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '费用单' orderTypeName," + AioConstants.BILL_ROW_TYPE22 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.getMoney accountMoney," + otherPaySql + " from cw_fee_bill bill where bill.unitId is not null and bill.unitId!=0");
    }
    else if (billType.equals("23"))
    {
      String buySql = "(ifnull(bill.moneys,0)-ifnull(bill.payMoney,0)) lastMoney";
      formSql.append(" select '固定资产购置单' orderTypeName," + AioConstants.BILL_ROW_TYPE23 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.payMoney accountMoney," + buySql + " from cw_addassets_bill bill where 1=1");
    }
    else if (billType.equals("25"))
    {
      String buySql = "(ifnull(bill.moneys,0)-ifnull(bill.getMoney,0)) lastMoney";
      formSql.append(" select '固定资产变卖单' orderTypeName," + AioConstants.BILL_ROW_TYPE25 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.getMoney accountMoney," + buySql + " from cw_subassets_bill bill where 1=1");
    }
    else if (billType.equals("26"))
    {
      formSql.append(" select '应收款减少' orderTypeName," + AioConstants.BILL_ROW_TYPE26 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney,bill.moneys lastMoney from cw_c_unitgetorpay_bill bill where mergeType=" + ChangePayOrGetController.MERGETYPE0);
    }
    else if (billType.equals("27"))
    {
      formSql.append(" select '应收款增加' orderTypeName," + AioConstants.BILL_ROW_TYPE27 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney,bill.moneys lastMoney from cw_c_unitgetorpay_bill bill where mergeType=" + ChangePayOrGetController.MERGETYPE1);
    }
    else if (billType.equals("28"))
    {
      formSql.append(" select '应付款减少' orderTypeName," + AioConstants.BILL_ROW_TYPE28 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney,bill.moneys lastMoney from cw_c_unitgetorpay_bill bill where mergeType=" + ChangePayOrGetController.MERGETYPE2);
    }
    else if (billType.equals("29"))
    {
      formSql.append(" select '应付款增加' orderTypeName," + AioConstants.BILL_ROW_TYPE29 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney,bill.moneys lastMoney from cw_c_unitgetorpay_bill bill where mergeType=" + ChangePayOrGetController.MERGETYPE3);
    }
    else if (billType.equals("all"))
    {
      String initGetSql = "(select ifnull(u.beginGetMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE_1 + " and o.unitId=u.id) lastMoney";
      formSql.append(" select '期初应收' orderTypeName," + AioConstants.BILL_ROW_TYPE_1 + " billTypId," + AioConstants.RCW_NO + " isRCW,u.id unitId,0 staffId, null id,'本年期初应收' code,null recodeDate,null deliveryDate,concat(u.fullName,'本年期初应收') remark,u.beginGetMoney taxMoneys,0 privilege,0 accountMoney," + initGetSql + " from b_unit u where 1=1 ");
      if ((unitId != null) && (unitId.intValue() != 0)) {
        formSql.append(" and u.id=" + unitId);
      }
      formSql.append(" union all");
      String initPaySql = "(select ifnull(u.beginPayMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE_2 + " and o.unitId=u.id) lastMoney";
      formSql.append(" select '期初应付' orderTypeName," + AioConstants.BILL_ROW_TYPE_2 + " billTypId," + AioConstants.RCW_NO + " isRCW,u.id unitId,0 staffId, null id,'本年期初应付' code,null recodeDate,null deliveryDate,concat(u.fullName,'本年期初应付') remark,u.beginPayMoney taxMoneys,0 privilege,0 accountMoney," + initPaySql + " from b_unit u where 1=1 ");
      if ((unitId != null) && (unitId.intValue() != 0)) {
        formSql.append(" and u.id=" + unitId);
      }
      formSql.append(" union all");
      String sellSql = "(select ifnull(bill.taxMoneys,0)-ifnull(bill.privilege,0)-ifnull(bill.payMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE4 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '销售单' orderTypeName," + AioConstants.BILL_ROW_TYPE4 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,bill.deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney," + sellSql + " from xs_sell_bill bill where 1=1");
      
      formSql.append(" union all");
      String sellReturnSql = "(select ifnull(bill.taxMoneys*-1,0)-ifnull(bill.privilege*-1,0)-ifnull(bill.payMoney*-1,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE7 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '销售退货单' orderTypeName," + AioConstants.BILL_ROW_TYPE7 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney," + sellReturnSql + " from xs_return_bill bill where 1=1");
      
      formSql.append(" union all");
      String sellChangeSql = "(select ifnull(bill.gapMoney,0)-ifnull(bill.privilege,0)-ifnull(bill.payMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE13 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '销售换货单' orderTypeName," + AioConstants.BILL_ROW_TYPE13 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.gapMoney taxMoneys,bill.privilege,bill.payMoney accountMoney," + sellChangeSql + " from xs_barter_bill bill where 1=1");
      
      formSql.append(" union all");
      String inSql = "(select ifnull(bill.taxMoneys,0)-ifnull(bill.privilege,0)-ifnull(bill.payMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE5 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '进货单' orderTypeName," + AioConstants.BILL_ROW_TYPE5 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,bill.payDate deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney," + inSql + " from cg_purchase_bill bill where 1=1");
      
      formSql.append(" union all");
      String inReturnSql = "(select ifnull(bill.taxMoneys*-1,0)-ifnull(bill.privilege*-1,0)-ifnull(bill.payMoney*-1,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE6 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '进货退货单' orderTypeName," + AioConstants.BILL_ROW_TYPE6 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney," + inReturnSql + " from cg_return_bill bill where 1=1");
      
      formSql.append(" union all");
      String inChangeSql = "(select ifnull(bill.gapMoney,0)-ifnull(bill.privilege,0)-ifnull(bill.payMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE12 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '进货换货单' orderTypeName," + AioConstants.BILL_ROW_TYPE12 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.gapMoney taxMoneys,bill.privilege,bill.payMoney accountMoney," + inChangeSql + " from cg_barter_bill bill where 1=1");
      
      formSql.append(" union all");
      String otherGetSql = "(select ifnull(bill.moneys,0)-ifnull(bill.getMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE18 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '其他收入单' orderTypeName," + AioConstants.BILL_ROW_TYPE18 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.getMoney accountMoney," + otherGetSql + " from cw_otherearn_bill bill where 1=1");
      
      formSql.append(" union all");
      String otherPaySql = "(select ifnull(bill.moneys,0)-ifnull(bill.getMoney,0)-ifnull(sum(o.settlementAmount),0) from cw_pay_by_order o where o.billTypeId=" + AioConstants.BILL_ROW_TYPE22 + " and o.billId=bill.id) lastMoney";
      formSql.append(" select '费用单' orderTypeName," + AioConstants.BILL_ROW_TYPE22 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.getMoney accountMoney," + otherPaySql + " from cw_fee_bill bill where bill.unitId is not null and bill.unitId!=0");
      
      formSql.append(" union all");
      String buySql = "(ifnull(bill.moneys,0)-ifnull(bill.payMoney,0)) lastMoney";
      formSql.append(" select '固定资产购置单' orderTypeName," + AioConstants.BILL_ROW_TYPE23 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.payMoney accountMoney," + buySql + " from cw_addassets_bill bill where 1=1");
      
      formSql.append(" union all");
      String changeBuySql = "(ifnull(bill.moneys,0)-ifnull(bill.getMoney,0)) lastMoney";
      formSql.append(" select '固定资产变卖单' orderTypeName," + AioConstants.BILL_ROW_TYPE25 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.getMoney accountMoney," + changeBuySql + " from cw_subassets_bill bill where 1=1");
      
      formSql.append(" union all");
      formSql.append(" select '应收款减少' orderTypeName," + AioConstants.BILL_ROW_TYPE26 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney,bill.moneys lastMoney from cw_c_unitgetorpay_bill bill where mergeType=" + ChangePayOrGetController.MERGETYPE0);
      
      formSql.append(" union all");
      formSql.append(" select '应收款增加' orderTypeName," + AioConstants.BILL_ROW_TYPE27 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney,bill.moneys lastMoney from cw_c_unitgetorpay_bill bill where mergeType=" + ChangePayOrGetController.MERGETYPE1);
      
      formSql.append(" union all");
      formSql.append(" select '应付款减少' orderTypeName," + AioConstants.BILL_ROW_TYPE28 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney,bill.moneys lastMoney from cw_c_unitgetorpay_bill bill where mergeType=" + ChangePayOrGetController.MERGETYPE2);
      
      formSql.append(" union all");
      formSql.append(" select '应付款增加' orderTypeName," + AioConstants.BILL_ROW_TYPE29 + " billTypId,bill.isRCW,bill.unitId,bill.staffId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney,bill.moneys lastMoney from cw_c_unitgetorpay_bill bill where mergeType=" + ChangePayOrGetController.MERGETYPE3);
    }
    if ((billType.equals("initGet")) || (billType.equals("initPay")))
    {
      formSql.append(" ) t where 1=1");
    }
    else
    {
      formSql.append(" ) t left join b_unit u on u.id=t.unitId left join b_staff s on s.id=t.staffId ");
      formSql.append(" where 1=1 and case when (t.billTypId=" + AioConstants.BILL_ROW_TYPE_1 + " or t.billTypId=" + AioConstants.BILL_ROW_TYPE_2 + " ) then (t.taxMoneys is not null and t.taxMoneys!=0) else t.isRCW=" + AioConstants.RCW_NO + " end ");
      formSql.append(" and case when (t.billTypId=" + AioConstants.BILL_ROW_TYPE_1 + " or t.billTypId=" + AioConstants.BILL_ROW_TYPE_2 + " ) then 1=1 else t.recodeDate between '" + startDate + "' and '" + endDate + "' end ");
      if ((unitId != null) && (unitId.intValue() != 0)) {
        formSql.append(" and case when (t.billTypId=" + AioConstants.BILL_ROW_TYPE_1 + " or t.billTypId=" + AioConstants.BILL_ROW_TYPE_2 + " ) then 1=1 else u.pids like'%{" + unitId + "}%' end ");
      }
      if ((staffId != null) && (staffId.intValue() != 0)) {
        formSql.append(" and case when (t.billTypId=" + AioConstants.BILL_ROW_TYPE_1 + " or t.billTypId=" + AioConstants.BILL_ROW_TYPE_2 + " ) then 1=1 else s.pids like'%{" + staffId + "}%' end ");
      }
    }
    ComFunController.basePrivsSql(formSql, BaseController.basePrivs(BaseController.UNIT_PRIVS), "u", "id");
    if (isOver.equals("over")) {
      formSql.append(" and t.lastMoney=0");
    } else if (isOver.equals("no")) {
      formSql.append(" and t.lastMoney!=0");
    }
    formSql.append(" order by t.recodeDate asc");
    
    Map<String, Object> pageMap = aioGetPageRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), formSql.toString(), null, new Object[0]);
    return pageMap;
  }
  
  public List<Record> settlementByOrderDetail(String configName, int billTypeId, int billId)
    throws ParseException
  {
    List<Record> list = null;
    StringBuffer sql = new StringBuffer("select t.* from (");
    
    StringBuffer getSql = new StringBuffer("select '收款单' orderTypeName," + AioConstants.BILL_ROW_TYPE17 + " billTypId,");
    getSql.append("bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,0 taxMoneys,0 privilege,abs(o.settlementAmount) accountMoney");
    getSql.append(" from cw_getmoney_bill bill left join cw_pay_by_order o on o.cwBillId=bill.id where o.cwBillTypeId=" + AioConstants.BILL_ROW_TYPE17 + " and o.billId=" + billId);
    
    StringBuffer paySql = new StringBuffer("select '付款单' orderTypeName," + AioConstants.BILL_ROW_TYPE19 + " billTypId,");
    paySql.append("bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,null taxMoneys,null privilege,abs(o.settlementAmount) accountMoney");
    paySql.append(" from cw_paymoney_bill bill left join cw_pay_by_order o on o.cwBillId=bill.id where o.cwBillTypeId=" + AioConstants.BILL_ROW_TYPE19 + " and o.billId=" + billId);
    if (billTypeId == AioConstants.BILL_ROW_TYPE_1)
    {
      getSql = new StringBuffer("select '收款单' orderTypeName," + AioConstants.BILL_ROW_TYPE17 + " billTypId,");
      getSql.append("bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,(select sum(u.beginGetMoney) from b_unit u where u.id =o.unitId) taxMoneys,null privilege,o.settlementAmount accountMoney");
      getSql.append(" from cw_getmoney_bill bill left join cw_pay_by_order o on o.cwBillId=bill.id where o.cwBillTypeId=" + AioConstants.BILL_ROW_TYPE17 + " and o.billId=" + billId);
      sql.append(getSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE_2)
    {
      paySql = new StringBuffer("select '付款单' orderTypeName," + AioConstants.BILL_ROW_TYPE19 + " billTypId,");
      paySql.append("bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,(select sum(u.beginPayMoney) from b_unit u where u.id =o.unitId) taxMoneys,null privilege,o.settlementAmount accountMoney");
      paySql.append(" from cw_paymoney_bill bill left join cw_pay_by_order o on o.cwBillId=bill.id where o.cwBillTypeId=" + AioConstants.BILL_ROW_TYPE19 + " and o.billId=" + billId);
      sql.append(paySql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE5)
    {
      sql.append(paySql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '进货单' orderTypeName," + AioConstants.BILL_ROW_TYPE5 + " billTypId,bill.id,bill.code,bill.recodeDate,bill.payDate deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney from cg_purchase_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE6)
    {
      sql.append(paySql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '进货退货单' orderTypeName," + AioConstants.BILL_ROW_TYPE6 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney from cg_return_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE12)
    {
      sql.append(paySql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '进货换货单' orderTypeName," + AioConstants.BILL_ROW_TYPE12 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.gapMoney taxMoneys,bill.privilege,bill.payMoney accountMoney from cg_barter_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE4)
    {
      sql.append(getSql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '销售单' orderTypeName," + AioConstants.BILL_ROW_TYPE4 + " billTypId,bill.id,bill.code,bill.recodeDate,bill.deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney from xs_sell_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE7)
    {
      sql.append(getSql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '销售退货单' orderTypeName," + AioConstants.BILL_ROW_TYPE7 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.taxMoneys,bill.privilege,bill.payMoney accountMoney from xs_return_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE13)
    {
      sql.append(getSql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '销售换货单' orderTypeName," + AioConstants.BILL_ROW_TYPE13 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.gapMoney taxMoneys,bill.privilege,bill.payMoney accountMoney from xs_barter_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE18)
    {
      sql.append(getSql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '其他收入单' orderTypeName," + AioConstants.BILL_ROW_TYPE18 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.getMoney accountMoney from cw_otherearn_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE22)
    {
      sql.append(paySql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '费用单' orderTypeName," + AioConstants.BILL_ROW_TYPE22 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.getMoney accountMoney from cw_fee_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE23)
    {
      sql.append(paySql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '固定资产购置单' orderTypeName," + AioConstants.BILL_ROW_TYPE23 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.payMoney accountMoney from cw_addassets_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE25)
    {
      sql.append(getSql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '固定资产变卖单' orderTypeName," + AioConstants.BILL_ROW_TYPE25 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,bill.getMoney accountMoney from cw_subassets_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE26)
    {
      sql.append(getSql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '应收款减少' orderTypeName," + AioConstants.BILL_ROW_TYPE26 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney from cw_c_unitgetorpay_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE27)
    {
      sql.append(getSql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '应收款增加' orderTypeName," + AioConstants.BILL_ROW_TYPE27 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney from cw_c_unitgetorpay_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE28)
    {
      sql.append(paySql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '应付款减少' orderTypeName," + AioConstants.BILL_ROW_TYPE28 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney from cw_c_unitgetorpay_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    else if (billTypeId == AioConstants.BILL_ROW_TYPE29)
    {
      sql.append(paySql);
      sql.append(" union all ");
      StringBuffer billSql = new StringBuffer("select '应付款减少' orderTypeName," + AioConstants.BILL_ROW_TYPE28 + " billTypId,bill.id,bill.code,bill.recodeDate,null deliveryDate,bill.remark,bill.moneys taxMoneys,0 privilege,0 accountMoney from cw_c_unitgetorpay_bill bill where bill.id=" + billId);
      sql.append(billSql);
    }
    sql.append(") t order by t.recodeDate asc");
    list = Db.use(configName).find(sql.toString());
    
    BigDecimal lastMoney = BigDecimal.ZERO;
    for (int i = 0; i < list.size(); i++)
    {
      if (i == 0) {
        lastMoney = ((Record)list.get(i)).getBigDecimal("taxMoneys");
      }
      lastMoney = BigDecimalUtils.sub(BigDecimalUtils.sub(lastMoney, ((Record)list.get(i)).getBigDecimal("privilege")), ((Record)list.get(i)).getBigDecimal("accountMoney"));
      ((Record)list.get(i)).set("lastMoney", lastMoney);
    }
    return list;
  }
}
