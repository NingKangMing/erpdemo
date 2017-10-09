package com.aioerp.model.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
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

public class OtherInOrOutReports
  extends BaseDbModel
{
  public static final OtherInOrOutReports dao = new OtherInOrOutReports();
  
  public Map<String, Object> reportOhterInOrOutCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String modelType = (String)paraMap.get("modelType");
    Integer prdSupId = (Integer)paraMap.get("supId");
    String unitId = (String)paraMap.get("unitId");
    String staffId = (String)paraMap.get("staffId");
    String storageId = (String)paraMap.get("storageId");
    String startTime = paraMap.get("startTime").toString();
    String endTime = StringUtil.dataStrToStr(paraMap.get("endTime").toString());
    String aimDiv = paraMap.get("aimDiv").toString();
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    Integer node = Integer.valueOf(String.valueOf(paraMap.get("node")));
    
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    
    StringBuffer selectSql = new StringBuffer("select t.*");
    StringBuffer fromSql = new StringBuffer();
    

    String billTable = "cc_otherin_bill";
    String detailTable = "cc_otherin_detail";
    if (modelType.equals("in"))
    {
      billTable = "cc_otherin_bill";
      detailTable = "cc_otherin_detail";
    }
    else if (modelType.equals("out"))
    {
      billTable = "cc_otherout_bill";
      detailTable = "cc_otherout_detail";
    }
    List<Object> paras = new ArrayList();
    
    fromSql.append(" from (select product.*,");
    


    fromSql.append(" (");
    fromSql.append(" select sum(case when b.isRCW = 2 then d.baseAmount*-1 else d.baseAmount end) baseAmount  from " + detailTable + " d");
    fromSql.append(" left join b_product p on p.id=d.productId");
    fromSql.append(" left join b_storage sto on sto.id=d.storageId");
    fromSql.append(" left join (select bb.*,bs.pids staffPids,bu.pids unitPids from " + billTable + " bb left join b_unit bu on bu.id=bb.unitId left join b_staff bs on bb.staffId=bs.id) b on b.id=d.billId");
    fromSql.append(" where p.pids like concat(product.pids,'%')");
    fromSql.append(" and (b.recodeDate between '" + startTime + "' and '" + endTime + "')");
    if ((unitId != null) && (!unitId.equals(""))) {
      fromSql.append(" and b.unitPids like '%{" + unitId + "}%'");
    }
    if ((staffId != null) && (!staffId.equals(""))) {
      fromSql.append(" and b.staffPids like '%{" + staffId + "}%'");
    }
    if ((storageId != null) && (!storageId.equals(""))) {
      fromSql.append(" and sto.pids like '%{" + storageId + "}%'");
    }
    ComFunController.queryUnitPrivs(fromSql, unitPrivs, "b");
    ComFunController.queryStaffPrivs(fromSql, staffPrivs, "b");
    ComFunController.queryStoragePrivs(fromSql, storagePrivs, "d");
    
    fromSql.append(") baseAmounts,");
    

    fromSql.append(" (");
    fromSql.append(" select sum(case when b.isRCW = 2 then d.money*-1 else d.money end) money  from " + detailTable + " d");
    fromSql.append(" left join b_product p on p.id=d.productId");
    fromSql.append(" left join b_storage sto on sto.id=d.storageId");
    fromSql.append(" left join (select bb.*,bs.pids staffPids,bu.pids unitPids from " + billTable + " bb left join b_unit bu on bu.id=bb.unitId left join b_staff bs on bb.staffId=bs.id) b on b.id=d.billId");
    fromSql.append(" where p.pids like concat(product.pids,'%')");
    fromSql.append(" and (b.recodeDate between '" + startTime + "' and '" + endTime + "')");
    if ((unitId != null) && (!unitId.equals(""))) {
      fromSql.append(" and b.unitPids like '%{" + unitId + "}%'");
    }
    if ((staffId != null) && (!staffId.equals(""))) {
      fromSql.append(" and b.staffPids like '%{" + staffId + "}%'");
    }
    if ((storageId != null) && (!storageId.equals(""))) {
      fromSql.append(" and sto.pids like '%{" + storageId + "}%'");
    }
    ComFunController.queryUnitPrivs(fromSql, unitPrivs, "b");
    ComFunController.queryStaffPrivs(fromSql, staffPrivs, "b");
    ComFunController.queryStoragePrivs(fromSql, storagePrivs, "d");
    fromSql.append(") baseMoneys");
    

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
  
  public Map<String, Object> otherInOrOutDetail(String configName, Map<String, Object> paraMap)
    throws Exception
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    
    String modelType = paraMap.get("modelType").toString();
    String aimDiv = paraMap.get("aimDiv").toString();
    String productId = (String)paraMap.get("productId");
    String unitId = (String)paraMap.get("unitId");
    String storageId = (String)paraMap.get("storageId");
    String startTime = paraMap.get("startTime").toString();
    String endTime = StringUtil.dataStrToStr(paraMap.get("endTime").toString());
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    

    String billTable = "";
    String detailTable = "";
    if (modelType.equals("in"))
    {
      billTable = "cc_otherin_bill";
      detailTable = "cc_otherin_detail";
    }
    else if (modelType.equals("out"))
    {
      billTable = "cc_otherout_bill";
      detailTable = "cc_otherout_detail";
    }
    List<Object> paras = new ArrayList();
    StringBuffer selectSql = new StringBuffer("select bd.*,unit.*,sto.*,pro.*,acc.*");
    StringBuffer fromSql = new StringBuffer(" from (");
    fromSql.append("select bill.id billId,bill.code billCode,bill.recodeDate,");
    if (modelType.equals("in")) {
      fromSql.append(AioConstants.BILL_ROW_TYPE10 + " billTypeId,'其它入库单' billTypeName,");
    } else if (modelType.equals("out")) {
      fromSql.append(AioConstants.BILL_ROW_TYPE11 + " billTypeId,'其它出库单' billTypeName,");
    }
    fromSql.append("(case when bill.isRCW = 2 then detail.baseAmount*-1 else detail.baseAmount end) amounts,");
    fromSql.append("(case when bill.isRCW = 2 then detail.money*-1 else detail.money end) moneys,");
    fromSql.append("bill.isRCW,bill.unitId,bill.storageId,bill.accountsId,bill.remark billRemark,detail.productId");
    fromSql.append(" from " + detailTable + " detail left join " + billTable + " bill on bill.id=detail.billId");
    fromSql.append(") bd");
    
    fromSql.append(" left join b_product pro on pro.id=bd.productId");
    fromSql.append(" left join  b_unit unit on unit.id=bd.unitId");
    fromSql.append(" left join b_storage sto on sto.id=bd.storageId");
    fromSql.append(" left join b_accounts acc on acc.id=bd.accountsId where 1=1");
    fromSql.append(" and (bd.recodeDate between ? and ?)");
    paras.add(startTime);
    paras.add(endTime);
    if ((productId != null) && (!productId.equals(""))) {
      fromSql.append(" and pro.pids like '%{" + productId + "}%'");
    }
    if ((unitId != null) && (!unitId.equals(""))) {
      fromSql.append(" and unit.pids like '%{" + unitId + "}%'");
    }
    if ((storageId != null) && (!storageId.equals(""))) {
      fromSql.append(" and sto.pids like '%{" + storageId + "}%'");
    }
    ComFunController.queryProductPrivs(fromSql, productPrivs, "pro", "id");
    ComFunController.queryUnitPrivs(fromSql, unitPrivs, "unit", "id");
    ComFunController.queryStoragePrivs(fromSql, storagePrivs, "sto", "id");
    if (aimDiv.equals("notRedRow")) {
      fromSql.append(" and bd.isRCW=0");
    }
    fromSql.append(" order by " + orderField + " " + orderDirection);
    

    Map<String, Class<? extends Model>> objMap = new HashMap();
    objMap.put("pro", Product.class);
    objMap.put("sto", Storage.class);
    objMap.put("unit", Unit.class);
    objMap.put("acc", Accounts.class);
    Map<String, Object> map = aioGetPageManyRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), objMap, paras.toArray());
    
    return map;
  }
}
