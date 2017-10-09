package com.aioerp.model.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.reports.sell.SellReports;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreakOrOverflowReports
  extends BaseDbModel
{
  public static final BreakOrOverflowReports dao = new BreakOrOverflowReports();
  
  public Map<String, Object> reportBreakOrOverflowCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String modelType = (String)paraMap.get("modelType");
    Integer prdSupId = (Integer)paraMap.get("supId");
    String staffId = (String)paraMap.get("staffId");
    String storageId = (String)paraMap.get("storageId");
    String startTime = paraMap.get("startTime").toString();
    String endTime = StringUtil.dataStrToStr(paraMap.get("endTime").toString());
    String aimDiv = paraMap.get("aimDiv").toString();
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    Integer node = Integer.valueOf(String.valueOf(paraMap.get("node")));
    
    StringBuffer selectSql = new StringBuffer("select t.*");
    StringBuffer fromSql = new StringBuffer();
    

    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    


    String billTable = "cc_breakage_bill";
    String detailTable = "cc_breakage_detail";
    if (modelType.equals("in"))
    {
      billTable = "cc_overflow_bill";
      detailTable = "cc_overflow_detail";
    }
    else if (modelType.equals("out"))
    {
      billTable = "cc_breakage_bill";
      detailTable = "cc_breakage_detail";
    }
    List<Object> paras = new ArrayList();
    
    fromSql.append(" from (select product.*,");
    


    fromSql.append(" (");
    fromSql.append(" select sum(case when b.isRCW = 2 then d.baseAmount*-1 else d.baseAmount end) baseAmount  from " + detailTable + " d");
    fromSql.append(" left join b_product p on p.id=d.productId");
    fromSql.append(" left join b_storage sto on sto.id=d.storageId");
    fromSql.append(" left join (select bb.*,bs.pids staffPids from " + billTable + " bb left join b_staff bs on bb.staffId=bs.id) b on b.id=d.billId");
    fromSql.append(" where p.pids like concat(product.pids,'%')");
    fromSql.append(" and (b.recodeDate between '" + startTime + "' and '" + endTime + "')");
    if ((staffId != null) && (!staffId.equals(""))) {
      fromSql.append(" and b.staffPids like '%{" + staffId + "}%'");
    }
    if ((storageId != null) && (!storageId.equals(""))) {
      fromSql.append(" and sto.pids like '%{" + storageId + "}%'");
    }
    ComFunController.queryStoragePrivs(fromSql, storagePrivs, "d");
    ComFunController.queryStaffPrivs(fromSql, staffPrivs, "b");
    fromSql.append(") baseAmounts,");
    

    fromSql.append(" (");
    fromSql.append(" select sum(case when b.isRCW = 2 then d.money*-1 else d.money end) money  from " + detailTable + " d");
    fromSql.append(" left join b_product p on p.id=d.productId");
    fromSql.append(" left join b_storage sto on sto.id=d.storageId");
    fromSql.append(" left join (select bb.*,bs.pids staffPids from " + billTable + " bb left join b_staff bs on bb.staffId=bs.id) b on b.id=d.billId");
    fromSql.append(" where p.pids like concat(product.pids,'%')");
    fromSql.append(" and (b.recodeDate between '" + startTime + "' and '" + endTime + "')");
    if ((staffId != null) && (!staffId.equals(""))) {
      fromSql.append(" and b.staffPids like '%{" + staffId + "}%'");
    }
    if ((storageId != null) && (!storageId.equals(""))) {
      fromSql.append(" and sto.pids like '%{" + storageId + "}%'");
    }
    ComFunController.queryStoragePrivs(fromSql, storagePrivs, "d");
    ComFunController.queryStaffPrivs(fromSql, staffPrivs, "b");
    fromSql.append(") baseMoneys");
    

    fromSql.append(" from b_product product where 1=1");
    boolean flag = SellReports.addReportBaseCondition(fromSql, "product", 
      String.valueOf(paraMap.get("searchBaseAttr")), 
      String.valueOf(paraMap.get("searchBaseVal")));
    if (flag) {
      if (AioConstants.NODE_1 == node.intValue())
      {
        if (prdSupId == null) {
          prdSupId = Integer.valueOf(0);
        }
        fromSql.append(" and product.pids like '%{" + prdSupId + "}%'");
      }
      else if ((prdSupId == null) || (prdSupId.intValue() == 0))
      {
        fromSql.append(" and product.supId=0");
      }
      else
      {
        fromSql.append(" and product.supId=?");
        paras.add(prdSupId);
      }
    }
    fromSql.append(" and product.status=?) t where 1=1");
    paras.add(Integer.valueOf(AioConstants.STATUS_ENABLE));
    

    ComFunController.queryProductPrivs(fromSql, productPrivs, "t", "id");
    if (AioConstants.NODE_1 == node.intValue()) {
      fromSql.append(" and t.node=" + node);
    }
    if (aimDiv.equals("notEq")) {
      fromSql.append(" and t.baseAmounts !=0");
    }
    fromSql.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Object> map = aioGetPageRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), null, paras.toArray());
    

    List<Record> list = (List)map.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Record r = (Record)list.get(i);
      String helpAmount = DwzUtils.helpAmount(r.getBigDecimal("baseAmounts"), r.getStr("calculateUnit1"), r.getStr("calculateUnit2"), r.getStr("calculateUnit3"), r.getBigDecimal("unitRelation2"), r.getBigDecimal("unitRelation3"));
      ((Record)list.get(i)).set("helpAmount", helpAmount);
    }
    map.put("pageList", list);
    return map;
  }
  
  public Map<String, Object> breakOrOverflowDetail(String configName, Map<String, Object> paraMap)
    throws Exception
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    
    String modelType = paraMap.get("modelType").toString();
    String aimDiv = paraMap.get("aimDiv").toString();
    String productId = (String)paraMap.get("productId");
    String storageId = (String)paraMap.get("storageId");
    String startTime = paraMap.get("startTime").toString();
    String endTime = StringUtil.dataStrToStr(paraMap.get("endTime").toString());
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    


    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    


    String billTable = "cc_breakage_bill";
    String detailTable = "cc_breakage_detail";
    if (modelType.equals("in"))
    {
      billTable = "cc_overflow_bill";
      detailTable = "cc_overflow_detail";
    }
    else if (modelType.equals("out"))
    {
      billTable = "cc_breakage_bill";
      detailTable = "cc_breakage_detail";
    }
    List<Object> paras = new ArrayList();
    StringBuffer selectSql = new StringBuffer("select bd.*,pro.*,sto.*");
    StringBuffer fromSql = new StringBuffer(" from (");
    fromSql.append("select bill.id billId,bill.code billCode,bill.recodeDate,");
    if (modelType.equals("in")) {
      fromSql.append(AioConstants.BILL_ROW_TYPE9 + " billTypeId,");
    } else if (modelType.equals("out")) {
      fromSql.append(AioConstants.BILL_ROW_TYPE8 + " billTypeId,");
    }
    fromSql.append("(case when bill.isRCW = 2 then detail.baseAmount*-1 else detail.baseAmount end) amounts,");
    fromSql.append("(case when bill.isRCW = 2 then detail.money*-1 else detail.money end) moneys,");
    fromSql.append("bill.isRCW,bill.storageId,bill.remark billRemark,detail.productId");
    fromSql.append(" from " + detailTable + " detail left join " + billTable + " bill on bill.id=detail.billId");
    fromSql.append(") bd");
    
    fromSql.append(" left join b_product pro on pro.id=bd.productId");
    fromSql.append(" left join b_storage sto on sto.id=bd.storageId where 1=1");
    fromSql.append(" and (bd.recodeDate between ? and ?)");
    paras.add(startTime);
    paras.add(endTime);
    if ((productId != null) && (!productId.equals(""))) {
      fromSql.append(" and pro.pids like '%{" + productId + "}%'");
    }
    if ((storageId != null) && (!storageId.equals(""))) {
      fromSql.append(" and sto.pids like '%{" + storageId + "}%'");
    }
    ComFunController.queryProductPrivs(fromSql, productPrivs, "bd");
    ComFunController.queryStoragePrivs(fromSql, storagePrivs, "bd");
    if (aimDiv.equals("notRedRow")) {
      fromSql.append(" and bd.isRCW=0");
    }
    fromSql.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Class<? extends Model>> objMap = new HashMap();
    objMap.put("pro", Product.class);
    objMap.put("sto", Storage.class);
    Map<String, Object> map = aioGetPageManyRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), objMap, paras.toArray());
    
    return map;
  }
}
