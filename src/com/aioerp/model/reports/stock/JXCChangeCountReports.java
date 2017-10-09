package com.aioerp.model.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Storage;
import com.aioerp.model.reports.sell.SellReports;
import com.aioerp.model.stock.StockInit;
import com.aioerp.model.stock.StockRecords;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JXCChangeCountReports
  extends BaseDbModel
{
  public static final JXCChangeCountReports dao = new JXCChangeCountReports();
  
  public Map<String, Object> reportJXCChangeCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    
    Integer prdSupId = (Integer)paraMap.get("supId");
    String storageIdStr = String.valueOf(paraMap.get("storageId"));
    int storageId = 0;
    if ((storageIdStr != null) && (!storageIdStr.equals(""))) {
      storageId = Integer.valueOf(storageIdStr).intValue();
    }
    String startTime = paraMap.get("startTime").toString();
    String endTime = StringUtil.dataStrToStr(paraMap.get("endTime").toString());
    String aimDiv = paraMap.get("aimDiv").toString();
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    Integer node = Integer.valueOf(String.valueOf(paraMap.get("node")));
    

    String unitPrivs = BaseController.basePrivs(BaseController.UNIT_PRIVS);
    String departmentPrivs = BaseController.basePrivs(BaseController.DEPARTMENT_PRIVS);
    String staffPrivs = BaseController.basePrivs(BaseController.STAFF_PRIVS);
    String storagePrivs = BaseController.basePrivs(BaseController.STORAGE_PRIVS);
    String productPrivs = BaseController.basePrivs(BaseController.PRODUCT_PRIVS);
    String accountPrivs = BaseController.basePrivs(BaseController.ACCOUNT_PRIVS);
    String searchBaseAttr = String.valueOf(paraMap.get("searchBaseAttr"));
    String searchBaseVal = String.valueOf(paraMap.get("searchBaseVal"));
    Map<String, String> privsMap = new HashMap();
    privsMap.put("unitPrivs", unitPrivs);
    privsMap.put("departmentPrivs", departmentPrivs);
    privsMap.put("staffPrivs", staffPrivs);
    privsMap.put("storagePrivs", storagePrivs);
    privsMap.put("productPrivs", productPrivs);
    privsMap.put("accountPrivs", accountPrivs);
    



    String storagePids = "{0}";
    if (storageId > 0)
    {
      Storage storage = (Storage)Storage.dao.findById(configName, Integer.valueOf(storageId));
      storagePids = storage.getStr("pids");
    }
    List<Object> paras = new ArrayList();
    


    StringBuffer selectSql = new StringBuffer("select temp.*");
    StringBuffer fromSql = new StringBuffer(" from (");
    fromSql.append("select t.*");
    
    fromSql.append(",(ifnull(t.pruchaseAmount,0)+ifnull(t.sellReturnAmount,0)+ifnull(t.dismountInAmount,0)+ifnull(t.overflowAmount,0)+ifnull(t.parityallotInAmount,0)+ifnull(t.difftallotInAmount,0)+ifnull(t.sellBarterInAmount,0)+ifnull(t.pruchaseBarterInAmount,0)+ifnull(t.otherinAmount,0)) inAllAmount");
    
    fromSql.append(",(ifnull(t.pruchaseMoney,0)+ifnull(t.sellReturnMoney,0)+ifnull(t.dismountInMoney,0)+ifnull(t.overflowMoney,0)+ifnull(t.parityallotInMoney,0)+ifnull(t.difftallotInMoney,0)+ifnull(t.sellBarterInMoney,0)+ifnull(t.pruchaseBarterInMoney,0)+ifnull(t.otherinMoney,0)) inAllMoney");
    
    fromSql.append(",(ifnull(t.sellAmount,0)+ifnull(t.pruchaseReturnAmount,0)+ifnull(t.dismountOutAmount,0)+ifnull(t.breakageAmount,0)+ifnull(t.parityalloOutAmount,0)+ifnull(t.difftallotOutAmount,0)+ifnull(t.sellBarterOutAmount,0)+ifnull(t.pruchaseBarterOutAmount,0)+ifnull(t.otheroutAmount,0)) outAllAmount");
    
    fromSql.append(",(ifnull(t.sellMoney,0)+ifnull(t.pruchaseReturnMoney,0)+ifnull(t.dismountOutMoney,0)+ifnull(t.breakageMoney,0)+ifnull(t.parityalloOutMoney,0)+ifnull(t.difftallotOutMoney,0)+ifnull(t.sellBarterOutMoney,0)+ifnull(t.pruchaseBarterOutMoney,0)+ifnull(t.otheroutMoney,0)) outAllMoney");
    fromSql.append(" from (");
    fromSql.append("select pro.* ");
    

    StringBuffer preInitAmountSql = StockInit.preInitComSql(storagePrivs, productPrivs, storagePids, "pro.pids", "amount");
    StringBuffer preAmountSql = StockRecords.preStockProductAmount(storagePrivs, productPrivs, startTime, storagePids, "pro.pids", "amount");
    fromSql.append(",(ifnull(" + preInitAmountSql + ",0)+ifnull(" + preAmountSql + ",0)) preAmount");
    
    StringBuffer preInitMoneySql = StockInit.preInitComSql(storagePrivs, productPrivs, storagePids, "pro.pids", "money");
    StringBuffer preMoneySql = StockRecords.preStockProductAmount(storagePrivs, productPrivs, startTime, storagePids, "pro.pids", "money");
    fromSql.append(",(ifnull(" + preInitMoneySql + ",0)+ifnull(" + preMoneySql + ",0)) preMoney");
    



    oneSum(fromSql, AioConstants.BILL_ROW_TYPE5, "in", "amount", "pruchaseAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE5, "in", "money", "pruchaseMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE7, "in", "amount", "sellReturnAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE7, "in", "money", "sellReturnMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE16, "in", "amount", "dismountInAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE16, "in", "money", "dismountInMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE9, "in", "amount", "overflowAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE9, "in", "money", "overflowMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE14, "in", "amount", "parityallotInAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE14, "in", "money", "parityallotInMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE15, "in", "amount", "difftallotInAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE15, "in", "money", "difftallotInMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE13, "in", "amount", "sellBarterInAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE13, "in", "money", "sellBarterInMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE12, "in", "amount", "pruchaseBarterInAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE12, "in", "money", "pruchaseBarterInMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE10, "in", "amount", "otherinAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE10, "in", "money", "otherinMoney");
    





    oneSum(fromSql, AioConstants.BILL_ROW_TYPE4, "out", "amount", "sellAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE4, "out", "money", "sellMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE6, "out", "amount", "pruchaseReturnAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE6, "out", "money", "pruchaseReturnMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE16, "out", "amount", "dismountOutAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE16, "out", "money", "dismountOutMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE8, "out", "amount", "breakageAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE8, "out", "money", "breakageMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE14, "out", "amount", "parityalloOutAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE14, "out", "money", "parityalloOutMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE15, "out", "amount", "difftallotOutAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE15, "out", "money", "difftallotOutMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE13, "out", "amount", "sellBarterOutAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE13, "out", "money", "sellBarterOutMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE12, "out", "amount", "pruchaseBarterOutAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE12, "out", "money", "pruchaseBarterOutMoney");
    

    oneSum(fromSql, AioConstants.BILL_ROW_TYPE11, "out", "amount", "otheroutAmount");
    
    oneSum(fromSql, AioConstants.BILL_ROW_TYPE11, "out", "money", "otheroutMoney");
    





    StringBuffer afterInitAmountSql = StockInit.preInitComSql(storagePrivs, productPrivs, storagePids, "pro.pids", "amount");
    StringBuffer afterAmountSql = StockRecords.preStockProductAmount(storagePrivs, productPrivs, endTime, storagePids, "pro.pids", "amount");
    fromSql.append(",(ifnull(" + afterInitAmountSql + ",0)+ifnull(" + afterAmountSql + ",0)) afterAmount");
    
    StringBuffer afterInitMoneySql = StockInit.preInitComSql(storagePrivs, productPrivs, storagePids, "pro.pids", "money");
    StringBuffer afterMoneySql = StockRecords.preStockProductAmount(storagePrivs, productPrivs, endTime, storagePids, "pro.pids", "money");
    fromSql.append(",(ifnull(" + afterInitMoneySql + ",0)+ifnull(" + afterMoneySql + ",0)) afterMoney");
    
    fromSql.append(" from b_product pro ");
    fromSql.append(" left join " + jxcLeftComSql(privsMap, startTime, endTime, storagePids) + " base on base.pids like concat(pro.pids,'%') ");
    fromSql.append(" where 1=1");
    ComFunController.basePrivsSql(fromSql, productPrivs, "pro", "id");
    boolean flag = SellReports.addReportBaseCondition(fromSql, "pro", searchBaseAttr, searchBaseVal);
    if (flag) {
      if (AioConstants.NODE_1 == node.intValue())
      {
        if (prdSupId == null) {
          prdSupId = Integer.valueOf(0);
        }
        fromSql.append(" and pro.pids like '%{" + prdSupId + "}%'");
      }
      else if ((prdSupId == null) || (prdSupId.intValue() == 0))
      {
        fromSql.append(" and pro.supId=0");
      }
      else
      {
        fromSql.append(" and pro.supId=?");
        paras.add(prdSupId);
      }
    }
    fromSql.append(" and pro.status= " + AioConstants.STATUS_ENABLE);
    if (AioConstants.NODE_1 == node.intValue()) {
      fromSql.append(" and pro.node=" + node);
    }
    fromSql.append(" group by pro.id");
    fromSql.append(" ) t");
    fromSql.append(" ) temp where 1=1");
    if (aimDiv.equals("notEq")) {
      fromSql.append(" and (temp.preAmount!=0 or temp.preMoney!=0 or temp.inAllAmount!=0 or temp.inAllMoney!=0  or temp.outAllAmount!=0 or temp.outAllMoney!=0 or temp.afterAmount!=0 or temp.afterMoney!=0)");
    } else if (aimDiv.equals("change")) {
      fromSql.append(" and (temp.inAllAmount!=0 or temp.outAllAmount!=0) ");
    }
    fromSql.append(" order by " + orderField + " " + orderDirection);
    Map<String, Object> map = aioGetPageRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), null, paras.toArray());
    
    List<Record> list = (List)map.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Record r = (Record)list.get(i);
      String helpInAmount = DwzUtils.helpAmount(r.getBigDecimal("baseInAmounts"), r.getStr("calculateUnit1"), r.getStr("calculateUnit2"), r.getStr("calculateUnit3"), r.getBigDecimal("unitRelation2"), r.getBigDecimal("unitRelation3"));
      ((Record)list.get(i)).set("helpInAmount", helpInAmount);
      String helpOutAmount = DwzUtils.helpAmount(r.getBigDecimal("baseOutAmounts"), r.getStr("calculateUnit1"), r.getStr("calculateUnit2"), r.getStr("calculateUnit3"), r.getBigDecimal("unitRelation2"), r.getBigDecimal("unitRelation3"));
      ((Record)list.get(i)).set("helpOutAmount", helpOutAmount);
    }
    map.put("pageList", list);
    return map;
  }
  
  public void oneSum(StringBuffer sql, int billTypeId, String type, String attr, String as)
  {
    if (type.equals("in"))
    {
      if (attr.equals("amount")) {
        sql.append(",sum(case when base.billTypeId=" + billTypeId + " then base.inAmount else 0 end )");
      } else if (attr.equals("money")) {
        sql.append(",sum(case when base.billTypeId=" + billTypeId + " then ifnull(base.inAmount,0)*ifnull(base.price,0) else 0 end )");
      }
    }
    else if (attr.equals("amount")) {
      sql.append(",sum(case when base.billTypeId=" + billTypeId + " then base.outAmount else 0 end )");
    } else if (attr.equals("money")) {
      sql.append(",sum(case when base.billTypeId=" + billTypeId + " then ifnull(base.outAmount,0)*ifnull(base.price,0) else 0 end )");
    }
    sql.append(" " + as);
  }
  
  public static StringBuffer jxcComSql(Map<String, String> prvisMap, int billTypeId, String type, String attr, String startTime, String endTime, String storagePids, String productPids)
  {
    if ((type == null) || (type.equals(""))) {
      type = "in";
    }
    if ((attr == null) || (attr.equals(""))) {
      type = "amount";
    }
    StringBuffer sql = new StringBuffer("(select");
    if (type.equals("in"))
    {
      if (attr.equals("amount")) {
        sql.append(" sum(sr.inAmount)");
      } else {
        sql.append(" sum(ifnull(sr.inAmount,0)*ifnull(sr.price,0))");
      }
    }
    else if (attr.equals("amount")) {
      sql.append(" sum(sr.outAmount)");
    } else if (attr.equals("money")) {
      sql.append(" sum(ifnull(sr.outAmount,0)*ifnull(sr.price,0))");
    }
    sql.append(" from cc_stock_records sr");
    

    sql.append(" left join b_storage s on s.id=sr.storageId");
    sql.append(" left join b_product p on p.id=sr.productId");
    sql.append(" where sr.billTypeId=" + billTypeId);
    sql.append(" and (sr.recodeTime between '" + startTime + "' and '" + endTime + "' )");
    if ((storagePids != null) && (!"".equals(storagePids))) {
      sql.append(" AND s.pids LIKE CONCAT('" + storagePids + "','%') ");
    }
    if ((productPids != null) && (!"".equals(productPids))) {
      sql.append(" AND p.pids LIKE CONCAT(" + productPids + ",'%') ");
    }
    ComFunController.queryProductPrivs(sql, (String)prvisMap.get("productPrivs"), "sr");
    ComFunController.queryStoragePrivs(sql, (String)prvisMap.get("storagePrivs"), "sr");
    





    sql.append(")");
    return sql;
  }
  
  public static StringBuffer jxcLeftComSql(Map<String, String> prvisMap, String startTime, String endTime, String storagePids)
  {
    StringBuffer sql = new StringBuffer("(select");
    sql.append(" sr.inAmount,sr.outAmount,sr.price,sr.productId,sr.billTypeId,p.pids ");
    












    sql.append(" from cc_stock_records sr");
    

    sql.append(" left join b_storage s on s.id=sr.storageId");
    sql.append(" left join b_product p on p.id=sr.productId");
    sql.append(" where 1=1");
    sql.append(" and (sr.recodeTime between '" + startTime + "' and '" + endTime + "' )");
    if ((storagePids != null) && (!"".equals(storagePids))) {
      sql.append(" AND s.pids LIKE CONCAT('" + storagePids + "','%') ");
    }
    ComFunController.queryProductPrivs(sql, (String)prvisMap.get("productPrivs"), "sr");
    ComFunController.queryStoragePrivs(sql, (String)prvisMap.get("storagePrivs"), "sr");
    





    sql.append(")");
    return sql;
  }
}
