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

public class DismountCountReports
  extends BaseDbModel
{
  public static final DismountCountReports dao = new DismountCountReports();
  
  public Map<String, Object> reportDismountCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    
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
    
    StringBuffer selectSql = new StringBuffer("select t.*");
    

    StringBuffer fromSql = new StringBuffer();
    String billTable = "cc_dismount_bill";
    String detailTable = "cc_dismount_detail";
    

    StringBuffer comSql = new StringBuffer();
    comSql.append(" left join b_product p on p.id=d.productId");
    comSql.append(" left join (select bb.*,bs.pids staffPids from " + billTable + " bb left join b_staff bs on bb.staffId=bs.id) b on b.id=d.billId");
    comSql.append(" left join b_storage inSto on (inSto.id= d.storageId and d.type=1)");
    comSql.append(" left join b_storage outSto on (outSto.id=d.storageId and d.type=2)");
    comSql.append(" where p.pids like concat(product.pids,'%')");
    comSql.append(" and (b.recodeDate between '" + startTime + "' and '" + endTime + "')");
    if ((staffId != null) && (!staffId.equals(""))) {
      comSql.append(" and b.staffPids like '%{" + staffId + "}%'");
    }
    if ((inStorageId != null) && (!inStorageId.equals("")) && (outStorageId != null) && (!outStorageId.equals("")))
    {
      comSql.append(" and (inSto.pids like '%{" + inStorageId + "}%' or outSto.pids like '%{" + outStorageId + "}%')");
    }
    else
    {
      if ((inStorageId != null) && (!inStorageId.equals(""))) {
        comSql.append(" and inSto.pids like '%{" + inStorageId + "}%'");
      }
      if ((outStorageId != null) && (!outStorageId.equals(""))) {
        comSql.append(" and outSto.pids like '%{" + outStorageId + "}%'");
      }
    }
    ComFunController.queryStoragePrivs(comSql, storagePrivs, "d");
    ComFunController.queryStaffPrivs(comSql, staffPrivs, "b");
    


    List<Object> paras = new ArrayList();
    fromSql.append(" from (select product.*,");
    
    fromSql.append(" (");
    fromSql.append(" select sum(case when b.isRCW = 2 then d.baseAmount*-1 else d.baseAmount end) from " + detailTable + " d");
    
    fromSql.append(comSql);
    fromSql.append(" and d.type=1");
    fromSql.append(") baseInAmounts,");
    
    fromSql.append(" (");
    fromSql.append(" select sum(case when b.isRCW = 2 then ifnull(d.basePrice,0)*ifnull(d.baseAmount,0)*-1 else ifnull(d.basePrice,0)*ifnull(d.baseAmount,0) end) from " + detailTable + " d");
    
    fromSql.append(comSql);
    fromSql.append(" and d.type=1");
    fromSql.append(") baseInMoneys,");
    
    fromSql.append(" (");
    fromSql.append(" select sum(case when b.isRCW = 2 then d.baseAmount*-1 else  d.baseAmount end) from " + detailTable + " d");
    
    fromSql.append(comSql);
    fromSql.append(" and d.type=2");
    fromSql.append(") baseOutAmounts,");
    

    fromSql.append(" (");
    fromSql.append(" select sum(case when b.isRCW = 2 then ifnull(d.costPrice,0)*ifnull(d.baseAmount,0)*-1  else ifnull(d.costPrice,0)*ifnull(d.baseAmount,0) end) from " + detailTable + " d");
    
    fromSql.append(comSql);
    fromSql.append(" and d.type=2");
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
    fromSql.append(" and product.status=?) t where 1=1");
    paras.add(Integer.valueOf(AioConstants.STATUS_ENABLE));
    


    ComFunController.queryProductPrivs(fromSql, productPrivs, "t", "id");
    if (AioConstants.NODE_1 == node.intValue()) {
      fromSql.append(" and t.node=" + node);
    }
    if (aimDiv.equals("notEq")) {
      fromSql.append(" and (t.baseInAmounts !=0 or t.baseOutAmounts !=0)");
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
  
  public Map<String, Object> dismountCountDetail(String configName, Map<String, Object> paraMap)
    throws Exception
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    

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
    
    String billTable = "cc_dismount_bill";
    String detailTable = "cc_dismount_detail";
    
    List<Object> paras = new ArrayList();
    StringBuffer selectSql = new StringBuffer("select bd.*,pro.*,inSto.*,outSto.*");
    StringBuffer fromSql = new StringBuffer(" from (");
    
    fromSql.append(" select bill.id billId,bill.code billCode,bill.recodeDate,");
    fromSql.append(AioConstants.BILL_ROW_TYPE16 + " billTypeId,'拆装单' billTypeName,");
    
    fromSql.append("(");
    fromSql.append("select case when bill.isRCW = 2 then sum(d.baseAmount*-1) else sum(d.baseAmount) end from " + detailTable + " d where d.billId=bill.id and d.type=1");
    fromSql.append(")inAmounts,");
    
    fromSql.append("(");
    fromSql.append("select case when bill.isRCW = 2 then sum(d.baseAmount*d.basePrice*-1) else sum(d.baseAmount*d.basePrice) end from " + detailTable + " d where d.billId=bill.id and d.type=1");
    fromSql.append(")inMoneys,");
    
    fromSql.append("(");
    fromSql.append("select case when bill.isRCW = 2 then sum(d.baseAmount*-1) else sum(d.baseAmount) end from " + detailTable + " d where d.billId=bill.id and d.type=2");
    fromSql.append(")outAmounts,");
    
    fromSql.append("(");
    fromSql.append("select case when bill.isRCW = 2 then sum(d.baseAmount*d.costPrice*-1) else sum(d.baseAmount*d.costPrice) end from " + detailTable + " d where d.billId=bill.id and d.type=2");
    fromSql.append(")outMoneys,");
    fromSql.append("bill.isRCW,bill.inStorageId,bill.outStorageId,bill.remark billRemark,detail.productId");
    fromSql.append(" from (");
    fromSql.append("select bb.* from " + detailTable + " dd ");
    fromSql.append(" left join b_product pp on pp.id=dd.productId");
    fromSql.append(" left join " + billTable + " bb on bb.id=dd.billId where 1=1");
    fromSql.append(" and (bb.recodeDate between ? and ?)");
    paras.add(startTime);
    paras.add(endTime);
    if ((productId != null) && (!productId.equals(""))) {
      fromSql.append(" and pp.pids like '%{" + productId + "}%'");
    }
    ComFunController.queryProductPrivs(fromSql, productPrivs, "pp", "id");
    fromSql.append(" ) bill");
    fromSql.append(" left join " + detailTable + " detail on detail.billId=bill.id");
    fromSql.append(" group by bill.id");
    fromSql.append(") bd");
    fromSql.append(" left join b_product pro on pro.id=bd.productId");
    fromSql.append(" left join b_storage inSto on inSto.id=bd.inStorageId");
    fromSql.append(" left join b_storage outSto on outSto.id=bd.outStorageId where 1=1");
    if ((inStorageId != null) && (!inStorageId.equals(""))) {
      fromSql.append(" and inSto.pids like '%{" + inStorageId + "}%'");
    }
    if ((outStorageId != null) && (!outStorageId.equals(""))) {
      fromSql.append(" and outSto.pids like '%{" + outStorageId + "}%'");
    }
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
