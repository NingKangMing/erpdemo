package com.aioerp.model.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Area;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.sys.BillType;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArapMoneyCheckReports
  extends BaseDbModel
{
  public static final ArapMoneyCheckReports dao = new ArapMoneyCheckReports();
  
  public Map<String, Object> arapMoneyCheck(String configName, Map<String, Object> paraMap)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> map = new HashMap();
    
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String orderField = (String)paraMap.get("orderField");
    String orderDirection = (String)paraMap.get("orderDirection");
    Integer billType = (Integer)paraMap.get("billType");
    String checkBy = (String)paraMap.get("checkBy");
    String filter = (String)paraMap.get("filter");
    Integer unitId = (Integer)paraMap.get("unitId");
    Integer staffId = (Integer)paraMap.get("staffId");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    
    String unitPrivs = BaseController.basePrivs(BaseController.UNIT_PRIVS);
    String departmentPrivs = BaseController.basePrivs(BaseController.DEPARTMENT_PRIVS);
    String staffPrivs = BaseController.basePrivs(BaseController.STAFF_PRIVS);
    String storagePrivs = BaseController.basePrivs(BaseController.STORAGE_PRIVS);
    String productPrivs = BaseController.basePrivs(BaseController.PRODUCT_PRIVS);
    String accountPrivs = BaseController.basePrivs(BaseController.ACCOUNT_PRIVS);
    


    StringBuffer selectSql = new StringBuffer();
    StringBuffer fromSql = new StringBuffer();
    StringBuffer comSql = new StringBuffer("select ");
    
    comSql.append(" pt.hasAccount,pt.productId,pt.unitId payUnitId,pt.type accountType,pt.accountId,case when pt.type=" + AioConstants.ACCOUNT_MONEY1 + " then pt.payMoney end as getMoney,case when pt.type=" + AioConstants.ACCOUNT_MONEY0 + " then pt.payMoney end as payMoney,");
    
    comSql.append(" bh.isRCW billIsRCW,bh.billTypeId,bh.billId,bh.billCode,bh.recodeDate,bh.remark billRemark,bh.memo billMemo,bh.unitId,bh.staffId,bh.departmentId,");
    



    comSql.append("sr.storageId,sr.selectUnitId,case when ifnull(sr.inAmount,0)!=0 then sr.inAmount else 0-ifnull(sr.outAmount,0) end baseAmount,sr.taxPrice,sr.taxMoney,");
    

    comSql.append("bt.name billTypeName");
    comSql.append(" from bb_billhistory bh");
    comSql.append(" left join cw_pay_type pt on bh.billTypeId=pt.billTypeId and bh.billId=pt.billId");
    comSql.append(" left join sys_billtype bt on bt.id=pt.billTypeId");
    comSql.append(" left join cc_stock_records sr on sr.billTypeId=pt.billTypeId and sr.billId=pt.billId and sr.productId=pt.productId");
    comSql.append(" left join b_unit unit on unit.id = bh.unitId");
    comSql.append(" where bt.isArap=" + AioConstants.ISARAP1 + " and bt.node=" + AioConstants.NODE_1 + " and pt.hasAccount=" + AioConstants.ARAPACCOUNT1);
    if ((billType != null) && (billType.intValue() != 0)) {
      comSql.append(" and bh.billTypeId=" + billType);
    }
    if ((unitId != null) && (unitId.intValue() != 0)) {
      comSql.append(" and unit.pids like '%{" + unitId + "}%'");
    }
    if ((staffId != null) && (staffId.intValue() != 0)) {
      comSql.append(" and bh.staffId=" + staffId);
    }
    if ((startDate != null) && (!startDate.equals("")) && (endDate != null) && (!endDate.equals("")))
    {
      comSql.append(" and bh.recodeDate between '" + startDate + "' and '" + endDate + "'");
    }
    else
    {
      if ((startDate != null) && (!startDate.equals(""))) {
        comSql.append(" and bh.recodeDate > '" + startDate + "'");
      }
      if ((endDate != null) && (!endDate.equals(""))) {
        comSql.append(" and bh.recodeDate <= '" + endDate + "'");
      }
    }
    if (filter.equals("noRCW")) {
      comSql.append(" and bh.isRCW=" + AioConstants.RCW_NO);
    }
    selectSql.append("select t.*,unit.*,area.*,staff.*,dept.*,sto.*,accounts.*");
    selectSql.append(",case when accounts.id=52 then t.baseAmount else 0 end baseAmount1");
    
    selectSql.append(",sum(case when accounts.id=52 then (case when (t.getMoney!=0 and t.payUnitId is not null) then t.taxMoney  else 0 end) else t.getMoney end) getMoney1");
    selectSql.append(",sum(case when accounts.id=52 then (case when (t.payMoney!=0 and t.payUnitId is not null) then t.taxMoney  else 0 end) else t.payMoney end) payMoney1");
    if (checkBy.equals("byProduct")) {
      selectSql.append(",pro.*");
    }
    fromSql.append(" from (" + comSql + ") t");
    fromSql.append(" left join b_accounts accounts on accounts.id=t.accountId");
    fromSql.append(" left join b_storage sto on sto.id=t.storageId");
    
    fromSql.append(" left join b_unit unit on unit.id=t.unitId");
    fromSql.append(" left join b_area area on area.id=unit.areaId");
    fromSql.append(" left join b_staff staff on staff.id=t.staffId");
    fromSql.append(" left join b_department dept on dept.id=t.departmentId");
    ComFunController.basePrivsSql(fromSql, accountPrivs, "t", "accountId");
    ComFunController.basePrivsSql(fromSql, storagePrivs, "t", "storageId");
    ComFunController.basePrivsSql(fromSql, unitPrivs, "t", "unitId");
    ComFunController.basePrivsSql(fromSql, staffPrivs, "t", "staffId");
    ComFunController.basePrivsSql(fromSql, departmentPrivs, "t", "departmentId");
    

    map.put("sto", Storage.class);
    map.put("unit", Unit.class);
    map.put("area", Area.class);
    map.put("staff", Staff.class);
    map.put("dept", Department.class);
    map.put("bt", BillType.class);
    map.put("accounts", Accounts.class);
    if (checkBy.equals("byProduct"))
    {
      fromSql.append(" left join b_product pro on pro.id=t.productId");
      
      map.put("pro", Product.class);
      ComFunController.basePrivsSql(fromSql, productPrivs, "pro", "id");
    }
    fromSql.append(" where 1=1");
    

    fromSql.append(" group by t.accountId,t.billId,t.billTypeId");
    if (checkBy.equals("byProduct")) {
      fromSql.append(",t.productId");
    }
    fromSql.append(" order by " + orderField + " " + orderDirection);
    Map<String, Object> pageMap = aioGetPageManyRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), map, new Object[0]);
    BigDecimal preLastMoney = BigDecimal.ZERO;
    BigDecimal lastMoney = BigDecimal.ZERO;
    

    StringBuffer selectSqlCount = new StringBuffer();
    StringBuffer fromSqlCount = new StringBuffer();
    StringBuffer comSqlCount = new StringBuffer("select ");
    
    comSqlCount.append(" pt.hasAccount,pt.productId,pt.unitId payUnitId,pt.type accountType,pt.accountId,case when pt.type=" + AioConstants.ACCOUNT_MONEY1 + " then pt.payMoney end as getMoney,case when pt.type=" + AioConstants.ACCOUNT_MONEY0 + " then pt.payMoney end as payMoney,");
    
    comSqlCount.append(" bh.billTypeId,bh.billId,bh.recodeDate,bh.unitId,bh.isRCW billIsRCW,");
    
    comSqlCount.append("sr.storageId,sr.selectUnitId,case when ifnull(sr.inAmount,0)!=0 then sr.inAmount else 0-ifnull(sr.outAmount,0) end baseAmount,case when ifnull(sr.inAmount,0)!=0 then sr.inAmount*sr.price when ifnull(sr.outAmount,0)!=0 then sr.outAmount*sr.price end taxMoney");
    
    comSqlCount.append(" from bb_billhistory bh");
    comSqlCount.append(" left join cw_pay_type pt on bh.billTypeId=pt.billTypeId and bh.billId=pt.billId");
    comSqlCount.append(" left join sys_billtype bt on bt.id=pt.billTypeId");
    comSqlCount.append(" left join cc_stock_records sr on sr.billTypeId=pt.billTypeId and sr.billId=pt.billId and sr.productId=pt.productId");
    comSqlCount.append(" left join b_unit unit on unit.id = bh.unitId");
    comSqlCount.append(" where bt.isArap=" + AioConstants.ISARAP1 + " and bt.node=" + AioConstants.NODE_1 + " and pt.hasAccount=" + AioConstants.ARAPACCOUNT1);
    if ((billType != null) && (billType.intValue() != 0)) {
      comSqlCount.append(" and bh.billTypeId=" + billType);
    }
    if ((unitId != null) && (unitId.intValue() != 0)) {
      comSqlCount.append(" and unit.pids like '%{" + unitId + "}%'");
    }
    if ((staffId != null) && (staffId.intValue() != 0)) {
      comSqlCount.append(" and bh.staffId=" + staffId);
    }
    if ((endDate != null) && (!endDate.equals(""))) {
      comSqlCount.append(" and bh.recodeDate < '" + startDate + "'");
    }
    if (filter.equals("noRCW")) {
      comSqlCount.append(" and bh.isRCW=" + AioConstants.RCW_NO);
    }
    selectSqlCount.append("select ");
    selectSqlCount.append("sum(case when accounts.id=52 then (case when (t.getMoney!=0 and t.payUnitId is not null)  then t.taxMoney  else 0 end) else t.getMoney end) getMoney1");
    selectSqlCount.append(",sum(case when accounts.id=52 then (case when (t.payMoney!=0 and t.payUnitId is not null) then t.taxMoney  else 0 end) else t.payMoney end) payMoney1");
    fromSqlCount.append(" from (" + comSqlCount + ") t");
    fromSqlCount.append(" left join b_accounts accounts on accounts.id=t.accountId");
    
    ComFunController.basePrivsSql(fromSqlCount, accountPrivs, "t", "accountId");
    ComFunController.basePrivsSql(fromSqlCount, storagePrivs, "t", "storageId");
    ComFunController.basePrivsSql(fromSqlCount, unitPrivs, "t", "unitId");
    ComFunController.basePrivsSql(fromSqlCount, staffPrivs, "t", "staffId");
    ComFunController.basePrivsSql(fromSqlCount, departmentPrivs, "t", "departmentId");
    
    fromSqlCount.append(" where 1=1");
    Record rr = Db.use(configName).findFirst(fromSqlCount.toString());
    if (rr != null)
    {
      preLastMoney = BigDecimalUtils.add(preLastMoney, rr.getBigDecimal("getMoney1"));
      preLastMoney = BigDecimalUtils.sub(preLastMoney, rr.getBigDecimal("payMoney1"));
    }
    if ((pageMap != null) && (((List)pageMap.get("pageList")).size() > 0))
    {
      List<Model> list = (List)pageMap.get("pageList");
      for (int i = 0; i < list.size(); i++)
      {
        BigDecimal getMoney1 = ((Model)list.get(i)).getBigDecimal("getMoney1");
        BigDecimal payMoney1 = ((Model)list.get(i)).getBigDecimal("payMoney1");
        if (checkBy.equals("byProduct"))
        {
          Integer selectUnitId = ((Model)list.get(i)).getInt("selectUnitId");
          BigDecimal baseAmount1 = ((Model)list.get(i)).getBigDecimal("baseAmount1");
          if ((BigDecimalUtils.compare(baseAmount1, BigDecimal.ZERO) != 0) && (selectUnitId != null))
          {
            Model pro = (Model)((Model)list.get(i)).get("pro");
            BigDecimal amount = BigDecimal.ZERO;
            ((Model)list.get(i)).put("helpAmount", DwzUtils.helpAmount(baseAmount1, pro.getStr("calculateUnit1"), pro.getStr("calculateUnit2"), pro.getStr("calculateUnit3"), pro.getBigDecimal("unitRelation2"), pro.getBigDecimal("unitRelation2")));
            if (selectUnitId.intValue() == 1)
            {
              amount = baseAmount1;
              ((Model)list.get(i)).put("baseUnit", pro.getStr("calculateUnit1"));
            }
            else if ((selectUnitId.intValue() == 2) && (BigDecimalUtils.compare(pro.getBigDecimal("unitRelation2"), BigDecimal.ZERO) > 0))
            {
              amount = BigDecimalUtils.div(baseAmount1, pro.getBigDecimal("unitRelation2"));
              ((Model)list.get(i)).put("baseUnit", pro.getStr("calculateUnit2"));
            }
            else if ((selectUnitId.intValue() == 3) && (BigDecimalUtils.compare(pro.getBigDecimal("unitRelation3"), BigDecimal.ZERO) > 0))
            {
              amount = BigDecimalUtils.div(baseAmount1, pro.getBigDecimal("unitRelation3"));
              ((Model)list.get(i)).put("baseUnit", pro.getStr("calculateUnit3"));
            }
            ((Model)list.get(i)).put("amount", amount);
          }
        }
        lastMoney = BigDecimalUtils.add(lastMoney, getMoney1);
        lastMoney = BigDecimalUtils.sub(lastMoney, payMoney1);
        ((Model)list.get(i)).put("lastMoney", lastMoney);
      }
      pageMap.put("pageList", list);
    }
    pageMap.put("preLastMoney", preLastMoney);
    return pageMap;
  }
}
