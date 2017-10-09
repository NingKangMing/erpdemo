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

public class SameOrDiffPriceReports
  extends BaseDbModel
{
  public static final SameOrDiffPriceReports dao = new SameOrDiffPriceReports();
  
  public Map<String, Object> reportSameOrDiffPriceCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String modelType = (String)paraMap.get("modelType");
    Integer prdSupId = (Integer)paraMap.get("supId");
    String staffId = (String)paraMap.get("staffId");
    String inStorageId = (String)paraMap.get("inStorageId");
    String outStorageId = (String)paraMap.get("outStorageId");
    String startTime = paraMap.get("startTime").toString();
    String endTime = StringUtil.dataStrToStr(paraMap.get("endTime").toString());
    String aimDiv = paraMap.get("aimDiv").toString();
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    Integer node = Integer.valueOf(String.valueOf(paraMap.get("node")));
    

    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    

    StringBuffer selectSql = new StringBuffer();
    if (modelType.equals("same")) {
      selectSql.append("select t.*,t.baseOutMoneys/t.baseAmounts outAvgPrice");
    } else if (modelType.equals("diff")) {
      selectSql.append("select t.*,t.baseOutMoneys/t.baseAmounts outAvgPrice,t.baseInMoneys/t.baseAmounts inAvgPrice,t.t.baseInMoneys-t.t.baseOutMoneys grapMoney");
    }
    StringBuffer fromSql = new StringBuffer();
    String billTable = "cc_difftallot_bill";
    String detailTable = "cc_difftallot_detail";
    if (modelType.equals("same"))
    {
      billTable = "cc_parityallot_bill";
      detailTable = "cc_parityallot_detail";
    }
    else if (modelType.equals("diff"))
    {
      billTable = "cc_difftallot_bill";
      detailTable = "cc_difftallot_detail";
    }
    StringBuffer comSql = new StringBuffer();
    comSql.append(" left join b_product p on p.id=d.productId");
    comSql.append(" left join (select bb.*,bs.pids staffPids from " + billTable + " bb left join b_staff bs on bb.staffId=bs.id) b on b.id=d.billId");
    comSql.append(" left join b_storage inSto on inSto.id= d.inStorageId");
    comSql.append(" left join b_storage outSto on outSto.id=d.outStorageId");
    comSql.append(" where p.pids like concat(product.pids,'%')");
    comSql.append(" and (b.recodeDate between '" + startTime + "' and '" + endTime + "')");
    if ((staffId != null) && (!staffId.equals(""))) {
      comSql.append(" and b.staffPids like '%{" + staffId + "}%'");
    }
    if ((inStorageId != null) && (!inStorageId.equals(""))) {
      comSql.append(" and inSto.pids like '%{" + inStorageId + "}%'");
    }
    if ((outStorageId != null) && (!outStorageId.equals(""))) {
      comSql.append(" and outSto.pids like '%{" + outStorageId + "}%'");
    }
    ComFunController.queryStaffPrivs(comSql, staffPrivs, "b");
    ComFunController.queryStoragePrivs(comSql, storagePrivs, "inSto", "id");
    ComFunController.queryStoragePrivs(comSql, storagePrivs, "outSto", "id");
    


    List<Object> paras = new ArrayList();
    fromSql.append(" from (select product.*,");
    
    fromSql.append(" (");
    fromSql.append(" select sum(case when b.isRCW = 2 then d.baseAmount*-1 else d.baseAmount end) baseAmount  from " + detailTable + " d");
    
    fromSql.append(comSql);
    fromSql.append(") baseAmounts,");
    if (modelType.equals("diff"))
    {
      fromSql.append(" (");
      fromSql.append(" select sum(case when b.isRCW = 2 then ifnull(d.basePrice,0)*ifnull(d.baseAmount,0)*-1 else ifnull(d.basePrice,0)*ifnull(d.baseAmount,0) end) money  from " + detailTable + " d");
      
      fromSql.append(comSql);
      fromSql.append(") baseInMoneys,");
    }
    fromSql.append(" (");
    fromSql.append(" select sum(case when b.isRCW = 2 then ifnull(d.costPrice,0)*ifnull(d.baseAmount,0)*-1 else ifnull(d.costPrice,0)*ifnull(d.baseAmount,0) end) money  from " + detailTable + " d");
    
    fromSql.append(comSql);
    fromSql.append(") baseOutMoneys");
    fromSql.append(" from b_product product where 1=1");
    
    boolean flag = SellReports.addReportBaseCondition(fromSql, "product", String.valueOf(paraMap.get("searchBaseAttr")), String.valueOf(paraMap.get("searchBaseVal")));
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
    ComFunController.queryProductPrivs(fromSql, productPrivs, "product", "id");
    
    fromSql.append(" and product.status=?) t where 1=1");
    paras.add(Integer.valueOf(AioConstants.STATUS_ENABLE));
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
  
  public Map<String, Object> sameOrDiffPriceDetail(String configName, Map<String, Object> paraMap)
    throws Exception
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    
    String modelType = paraMap.get("modelType").toString();
    String aimDiv = paraMap.get("aimDiv").toString();
    String productId = (String)paraMap.get("productId");
    String inStorageId = (String)paraMap.get("inStorageId");
    String outStorageId = (String)paraMap.get("outStorageId");
    String startTime = paraMap.get("startTime").toString();
    String endTime = StringUtil.dataStrToStr(paraMap.get("endTime").toString());
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    


    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    
    String billTable = "cc_difftallot_bill";
    String detailTable = "cc_difftallot_detail";
    if (modelType.equals("same"))
    {
      billTable = "cc_parityallot_bill";
      detailTable = "cc_parityallot_detail";
    }
    else if (modelType.equals("diff"))
    {
      billTable = "cc_difftallot_bill";
      detailTable = "cc_difftallot_detail";
    }
    List<Object> paras = new ArrayList();
    StringBuffer selectSql = new StringBuffer("select bd.*,pro.*,inSto.*,outSto.*");
    StringBuffer fromSql = new StringBuffer(" from (");
    fromSql.append("select bill.id billId,bill.code billCode,bill.recodeDate,");
    if (modelType.equals("same"))
    {
      fromSql.append(AioConstants.BILL_ROW_TYPE14 + " billTypeId,'同价调拨单' billTypeName,");
      fromSql.append("(case when bill.isRCW = 2 then ifnull(detail.baseAmount,0)*ifnull(detail.costPrice,0)*-1 else ifnull(detail.baseAmount,0)*ifnull(detail.costPrice,0) end) outMoneys,");
    }
    else if (modelType.equals("diff"))
    {
      fromSql.append(AioConstants.BILL_ROW_TYPE15 + " billTypeId,'变价调拨单' billTypeName,");
      fromSql.append("(case when bill.isRCW = 2 then ifnull(detail.baseAmount,0)*ifnull(detail.costPrice,0)*-1 else ifnull(detail.baseAmount,0)*ifnull(detail.costPrice,0) end) outMoneys,");
      fromSql.append("(case when bill.isRCW = 2 then ifnull(detail.baseAmount,0)*ifnull(detail.basePrice,0)*-1 else ifnull(detail.baseAmount,0)*ifnull(detail.basePrice,0) end) inMoneys,");
    }
    fromSql.append("(case when bill.isRCW = 2 then detail.baseAmount*-1 else detail.baseAmount end) amounts,");
    fromSql.append("bill.isRCW,bill.inStorageId,bill.outStorageId,bill.remark billRemark,detail.productId");
    fromSql.append(" from " + detailTable + " detail left join " + billTable + " bill on bill.id=detail.billId");
    fromSql.append(") bd");
    
    fromSql.append(" left join b_product pro on pro.id=bd.productId");
    fromSql.append(" left join b_storage inSto on inSto.id=bd.inStorageId");
    fromSql.append(" left join b_storage outSto on outSto.id=bd.outStorageId where 1=1");
    fromSql.append(" and (bd.recodeDate between ? and ?)");
    paras.add(startTime);
    paras.add(endTime);
    if ((productId != null) && (!productId.equals(""))) {
      fromSql.append(" and pro.pids like '%{" + productId + "}%'");
    }
    if ((inStorageId != null) && (!inStorageId.equals(""))) {
      fromSql.append(" and inSto.pids like '%{" + inStorageId + "}%'");
    }
    if ((outStorageId != null) && (!outStorageId.equals(""))) {
      fromSql.append(" and outSto.pids like '%{" + outStorageId + "}%'");
    }
    ComFunController.queryProductPrivs(fromSql, productPrivs, "pro", "id");
    ComFunController.queryStoragePrivs(fromSql, storagePrivs, "inSto", "id");
    ComFunController.queryStoragePrivs(fromSql, storagePrivs, "outSto", "id");
    if (aimDiv.equals("notRedRow")) {
      fromSql.append(" and bd.isRCW=0");
    }
    fromSql.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Class<? extends Model>> objMap = new HashMap();
    objMap.put("pro", Product.class);
    objMap.put("inSto", Storage.class);
    objMap.put("outSto", Storage.class);
    Map<String, Object> map = aioGetPageManyRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), objMap, paras.toArray());
    
    return map;
  }
}
