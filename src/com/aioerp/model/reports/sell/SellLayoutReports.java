package com.aioerp.model.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class SellLayoutReports
  extends BaseDbModel
{
  public static final SellLayoutReports dao = new SellLayoutReports();
  
  public Map<String, Object> reportSellLayoutCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    List<Model> objList = (List)paraMap.get("objList");
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String modelType = (String)paraMap.get("modelType");
    int supId = Integer.valueOf(paraMap.get("supId").toString()).intValue();
    String unitId = String.valueOf(paraMap.get("unitId"));
    Integer staffId = (Integer)paraMap.get("staffId");
    String storageId = String.valueOf(paraMap.get("storageId"));
    String[] orderTypes = (String[])paraMap.get("orderTypes");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    String aimDiv = paraMap.get("aimDiv").toString();
    Integer node = Integer.valueOf(String.valueOf(paraMap.get("node")));
    

    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    String searchBaseAttr = String.valueOf(paraMap.get("searchBaseAttr"));
    String searchBaseVal = String.valueOf(paraMap.get("searchBaseVal"));
    

    StringBuffer selectSql = sumUnionSql(unitPrivs, staffPrivs, storagePrivs, objList, modelType, orderTypes, aimDiv, startDate, endDate, unitId, staffId.intValue(), storageId);
    StringBuffer fromSql = new StringBuffer("from b_product pro ");
    fromSql.append(" left join (" + unionallSql(unitPrivs, staffPrivs, storagePrivs, orderTypes, startDate, endDate, unitId, staffId.intValue(), storageId).toString() + ") t on t.pids LIKE CONCAT(pro.pids,'%') ");
    fromSql.append(" where 1=1");
    
    boolean flag = SellReports.addReportBaseCondition(fromSql, "pro", searchBaseAttr, searchBaseVal);
    if (flag) {
      if (AioConstants.NODE_1 == node.intValue()) {
        fromSql.append(" and pro.pids like '%{" + supId + "}%'");
      } else if (supId == 0) {
        fromSql.append(" and pro.supId=0");
      } else {
        fromSql.append(" and pro.supId=" + supId);
      }
    }
    fromSql.append(" and pro.status=" + AioConstants.STATUS_ENABLE);
    if (AioConstants.NODE_1 == node.intValue()) {
      fromSql.append(" and pro.node=" + node);
    }
    ComFunController.queryProductPrivs(fromSql, productPrivs, "pro", "id");
    fromSql.append(" group by pro.id ");
    fromSql.append(" order by " + orderField + " " + orderDirection);
    return aioGetPageRecord(configName, Product.dao, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), new Object[0]);
  }
  
  public StringBuffer sumUnionSql(String unitPrivs, String staffPrivs, String storagePrivs, List<Model> objList, String modelType, String[] orderTypes, String aimDiv, String startDate, String endDate, String unitId, int staffId, String storageId)
  {
    StringBuffer all = new StringBuffer("select pro.*, ");
    for (Model obj : objList)
    {
      int objId = obj.getInt("id").intValue();
      if (modelType.equals("unit"))
      {
        all.append("sum(case when t.unitId=" + objId + " then t.baseAmount else 0 end ) as amount" + objId + ", ");
        if (aimDiv.equals("after")) {
          all.append("sum(case when t.unitId=" + objId + " then t.taxMoney else 0 end ) as money" + objId + ", ");
        } else {
          all.append("sum(case when t.unitId=" + objId + " then t.discountMoney else 0 end ) as money" + objId + ", ");
        }
      }
      else if (modelType.equals("storage"))
      {
        all.append("sum(case when t.storageId=" + objId + " then t.baseAmount else 0 end ) as amount" + objId + ", ");
        if (aimDiv.equals("after")) {
          all.append("sum(case when t.storageId=" + objId + " then t.taxMoney else 0 end ) as money" + objId + ", ");
        } else {
          all.append("sum(case when t.storageId=" + objId + " then t.discountMoney else 0 end ) as money" + objId + ", ");
        }
      }
    }
    if (modelType.equals("unit"))
    {
      all.append("sum(t.baseAmount) as allAmount, ");
      if (aimDiv.equals("after")) {
        all.append("sum(t.taxMoney) as allMoneys ");
      } else {
        all.append("sum(t.discountMoney) as allMoneys ");
      }
    }
    else if (modelType.equals("storage"))
    {
      all.append("sum(t.baseAmount) as allAmount, ");
      if (aimDiv.equals("after")) {
        all.append("sum(t.taxMoney) as allMoneys ");
      } else {
        all.append("sum(t.discountMoney) as allMoneys ");
      }
    }
    return all;
  }
  
  public StringBuffer unionallSql(String unitPrivs, String staffPrivs, String storagePrivs, String[] orderTypes, String startDate, String endDate, String unitId, int staffId, String storageId)
  {
    StringBuffer unionSql = new StringBuffer();
    StringBuffer singleSql = null;
    String billTable = "";
    String billDetailTable = "";
    for (int i = 0; i < orderTypes.length; i++)
    {
      if (orderTypes[i].equals("xsd"))
      {
        singleSql = new StringBuffer("select product.pids,detail.productId,bill.unitId,detail.storageId,detail.basePrice ");
        singleSql.append(",(case when bill.isRCW != 2 then detail.baseAmount else detail.baseAmount*-1 end) baseAmount");
        singleSql.append(",(case when bill.isRCW != 2 then detail.discountMoney else detail.discountMoney*-1 end) discountMoney");
        singleSql.append(",(case when bill.isRCW != 2 then detail.taxMoney else detail.taxMoney*-1 end) taxMoney");
        billTable = "xs_sell_bill";
        billDetailTable = "xs_sell_detail";
      }
      else if (orderTypes[i].equals("xsthd"))
      {
        singleSql = new StringBuffer("select product.pids,detail.productId,bill.unitId,detail.storageId,detail.basePrice");
        singleSql.append(",(case when bill.isRCW != 2 then detail.baseAmount*-1 else detail.baseAmount end) baseAmount");
        singleSql.append(",(case when bill.isRCW != 2 then detail.discountMoney*-1 else detail.discountMoney end) discountMoney");
        singleSql.append(",(case when bill.isRCW != 2 then detail.taxMoney*-1 else detail.taxMoney end) taxMoney");
        billTable = "xs_return_bill";
        billDetailTable = "xs_return_detail";
      }
      else if (orderTypes[i].equals("xshhd"))
      {
        singleSql = new StringBuffer("select product.pids,detail.productId,bill.unitId,detail.storageId,detail.basePrice");
        singleSql.append(",(case when (bill.isRCW != 2 and detail.type=1) or (bill.isRCW = 2 and detail.type!=1) then detail.baseAmount*-1 else detail.baseAmount end) baseAmount");
        singleSql.append(",(case when (bill.isRCW != 2 and detail.type=1) or (bill.isRCW = 2 and detail.type!=1) then detail.discountMoney*-1 else detail.discountMoney end) discountMoney");
        singleSql.append(",(case when (bill.isRCW != 2 and detail.type=1) or (bill.isRCW = 2 and detail.type!=1) then detail.taxMoney*-1 else detail.taxMoney end) taxMoney");
        billTable = "xs_barter_bill";
        billDetailTable = "xs_barter_detail";
      }
      else
      {
        if (orderTypes[i].equals("jzxsd")) {
          continue;
        }
        if (orderTypes[i].equals("wtjsd")) {
          continue;
        }
      }
      singleSql.append(" from " + billDetailTable + " detail left join " + billTable + " bill on detail.billId=bill.id ");
      singleSql.append(" left join b_storage storage on storage.id=detail.storageId ");
      singleSql.append(" left join b_unit unit on unit.id=bill.unitId");
      singleSql.append(" left join b_staff staff on staff.id=bill.staffId");
      singleSql.append(" left join b_product product on product.id=detail.productId where 1=1 ");
      singleSql.append(" and (bill.recodeDate between '" + startDate + "' and '" + endDate + "') ");
      if ((unitId != null) && (!unitId.equals("")) && (!unitId.equals("0")))
      {
        String[] unitArr = unitId.split(",");
        for (int j = 0; j < unitArr.length; j++) {
          if (j == 0) {
            singleSql.append(" and ( unit.pids like '%{" + unitArr[j] + "}%' ");
          } else {
            singleSql.append(" or unit.pids like '%{" + unitArr[j] + "}%' ");
          }
        }
        singleSql.append(")");
      }
      if (staffId != 0) {
        singleSql.append(" and staff.pids like '%{" + staffId + "}%' ");
      }
      if ((storageId != null) && (!storageId.equals("")) && (storageId.equals("0")))
      {
        String[] stoArr = storageId.split(",");
        for (int j = 0; j < stoArr.length; j++) {
          if (j == 0) {
            singleSql.append(" and ( storage.pids like '%{" + stoArr[j] + "}%' ");
          } else {
            singleSql.append(" or storage.pids like '%{" + stoArr[j] + "}%' ");
          }
        }
        singleSql.append(")");
      }
      ComFunController.queryUnitPrivs(singleSql, unitPrivs, "unit", "id");
      ComFunController.queryStaffPrivs(singleSql, staffPrivs, "staff", "id");
      ComFunController.queryStoragePrivs(singleSql, storagePrivs, "storage", "id");
      

      unionSql.append(singleSql);
      if ((orderTypes.length > 1) && (orderTypes.length - 1 != i)) {
        unionSql.append("union all ");
      }
    }
    return unionSql;
  }
}
