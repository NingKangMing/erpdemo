package com.aioerp.model.reports.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.reports.sell.SellReports;
import com.jfinal.plugin.activerecord.Model;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PurchaseStorageReports
  extends BaseDbModel
{
  public static final PurchaseStorageReports dao = new PurchaseStorageReports();
  
  public Map<String, Object> getSpreadPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> map)
    throws Exception
  {
    String[] orderTypes = (String[])map.get("orderTypes");
    List<Model> storageList = (List)map.get("storageList");
    String startDate = (String)map.get("startDate");
    String endDate = (String)map.get("endDate");
    Integer staffId = Integer.valueOf((Integer)map.get("staffId") == null ? 0 : ((Integer)map.get("staffId")).intValue());
    Integer unitId = Integer.valueOf((Integer)map.get("unitId") == null ? 0 : ((Integer)map.get("unitId")).intValue());
    String spreadWay = (String)map.get("spreadWay");
    String unitPrivs = String.valueOf(map.get("unitPrivs"));
    String staffPrivs = String.valueOf(map.get("staffPrivs"));
    String storagePrivs = String.valueOf(map.get("storagePrivs"));
    String productPrivs = String.valueOf(map.get("productPrivs"));
    String departmentPrivs = String.valueOf(map.get("departmentPrivs"));
    
    StringBuffer selectSql = sumUnionSql(storageList, startDate, endDate, staffId.intValue(), spreadWay);
    StringBuffer fromSql = new StringBuffer("from b_product pro ");
    fromSql.append(" left join (" + unionallSql(unitPrivs, staffPrivs, storagePrivs, departmentPrivs, orderTypes, startDate, endDate, storageList, staffId.intValue(), String.valueOf(unitId)).toString() + ") t on t.pids LIKE CONCAT(pro.pids,'%') ");
    fromSql.append(" where 1=1");
    
    int supId = Integer.valueOf(String.valueOf(map.get("supId"))).intValue();
    
    boolean flag = SellReports.addReportBaseCondition(fromSql, "pro", 
      String.valueOf(map.get("searchBaseAttr")), String.valueOf(map.get("searchBaseVal")));
    if (flag) {
      if ((map.get("isRow") != null) && ("true".equals(map.get("isRow")))) {
        fromSql.append(" and pro.pids like '%{" + supId + "}%'");
      } else if (supId == 0) {
        fromSql.append(" and pro.supId = 0");
      } else {
        fromSql.append(" and pro.supId = " + supId);
      }
    }
    fromSql.append(" and pro.status=" + AioConstants.STATUS_ENABLE);
    if ((map.get("isRow") != null) && ("true".equals(map.get("isRow")))) {
      fromSql.append(" and pro.node = 1");
    }
    ComFunController.queryProductPrivs(fromSql, productPrivs, "pro", "id");
    fromSql.append(" group by pro.id ");
    if (map != null)
    {
      String orderField = (String)map.get("orderField");
      String orderDirection = (String)map.get("orderDirection");
      if ((StringUtils.isNotBlank(orderField)) && 
        (StringUtils.isNotBlank(orderDirection))) {
        fromSql.append(" order by " + orderField + " " + orderDirection);
      }
    }
    return aioGetPageRecord(configName, Product.dao, listID, pageNum, 
      numPerPage, selectSql.toString(), fromSql.toString(), new Object[0]);
  }
  
  private StringBuffer sumUnionSql(List<Model> objList, String startDate, String endDate, int staffId, String aimDiv)
  {
    StringBuffer all = new StringBuffer("select pro.*, ");
    for (Model obj : objList)
    {
      int objId = obj.getInt("id").intValue();
      all.append("sum(case when t.storageId=" + objId + " then t.baseAmount else 0 end ) as baseAmount" + objId + ", ");
      if ("0".equals(aimDiv)) {
        all.append("sum(case when t.storageId=" + objId + " then t.taxMoney else 0 end ) as money" + objId + ", ");
      } else {
        all.append("sum(case when t.storageId=" + objId + " then t.discountMoney else 0 end ) as money" + objId + ", ");
      }
    }
    all.append("sum(t.baseAmount) as allAmount, ");
    if ("0".equals(aimDiv)) {
      all.append("sum(t.taxMoney) as allMoney ");
    } else {
      all.append("sum(t.discountMoney) as allMoney ");
    }
    return all;
  }
  
  private StringBuffer unionallSql(String unitPrivs, String staffPrivs, String storagePrivs, String departmentPrivs, String[] orderTypes, String startDate, String endDate, List<Model> storageList, int staffId, String unitId)
  {
    StringBuffer unionSql = new StringBuffer();
    StringBuffer singleSql = null;
    String billTable = "";
    String billDetailTable = "";
    for (int i = 0; i < orderTypes.length; i++)
    {
      Integer type = Integer.valueOf(Integer.parseInt(orderTypes[i]));
      if (AioConstants.BILL_ROW_TYPE5 == type.intValue())
      {
        singleSql = new StringBuffer("select product.pids,detail.productId,bill.unitId,detail.storageId,detail.basePrice ");
        singleSql.append(",(case when bill.isRCW != 2 then detail.baseAmount else detail.baseAmount*-1 end) baseAmount");
        singleSql.append(",(case when bill.isRCW != 2 then detail.discountMoney else detail.discountMoney*-1 end) discountMoney");
        singleSql.append(",(case when bill.isRCW != 2 then detail.taxMoney else detail.taxMoney*-1 end) taxMoney");
        billTable = "cg_purchase_bill";
        billDetailTable = "cg_purchase_detail";
      }
      else if (AioConstants.BILL_ROW_TYPE6 == type.intValue())
      {
        singleSql = new StringBuffer("select product.pids,detail.productId,bill.unitId,detail.storageId,detail.basePrice");
        singleSql.append(",(case when bill.isRCW != 2 then detail.baseAmount*-1 else detail.baseAmount end) baseAmount");
        singleSql.append(",(case when bill.isRCW != 2 then detail.discountMoney*-1 else detail.discountMoney end) discountMoney");
        singleSql.append(",(case when bill.isRCW != 2 then detail.taxMoney*-1 else detail.taxMoney end) taxMoney");
        billTable = "cg_return_bill";
        billDetailTable = "cg_return_detail";
      }
      else if (AioConstants.BILL_ROW_TYPE12 == type.intValue())
      {
        singleSql = new StringBuffer("select product.pids,detail.productId,bill.unitId,detail.storageId,detail.basePrice");
        singleSql.append(",(case when (bill.isRCW != 2 and detail.type=1) or (bill.isRCW = 2 and detail.type!=1) then detail.baseAmount*-1 else detail.baseAmount end) baseAmount");
        singleSql.append(",(case when (bill.isRCW != 2 and detail.type=1) or (bill.isRCW = 2 and detail.type!=1) then detail.discountMoney*-1 else detail.discountMoney end) discountMoney");
        singleSql.append(",(case when (bill.isRCW != 2 and detail.type=1) or (bill.isRCW = 2 and detail.type!=1) then detail.taxMoney*-1 else detail.taxMoney end) taxMoney");
        billTable = "cg_barter_bill";
        billDetailTable = "cg_barter_detail";
      }
      singleSql.append(" from " + billDetailTable + " detail left join " + billTable + " bill on detail.billId=bill.id ");
      singleSql.append(" left join b_storage storage on storage.id=detail.storageId ");
      singleSql.append(" left join b_unit unit on unit.id=bill.unitId");
      singleSql.append(" left join b_staff staff on staff.id=bill.staffId");
      singleSql.append(" left join b_department department on department.id=bill.departmentId");
      singleSql.append(" left join b_product product on product.id=detail.productId where 1=1 ");
      singleSql.append(" and (bill.recodeDate between '" + startDate + "' and '" + endDate + " 23:59:59') ");
      if ((storageList != null) && (storageList.size() != 0))
      {
        for (int j = 0; j < storageList.size(); j++) {
          if (j == 0) {
            singleSql.append(" and ( storage.pids like '%{" + ((Model)storageList.get(j)).getInt("id") + "}%' ");
          } else {
            singleSql.append(" or storage.pids like '%{" + ((Model)storageList.get(j)).getInt("id") + "}%' ");
          }
        }
        singleSql.append(")");
      }
      if (staffId != 0) {
        singleSql.append(" and staff.pids like '%{" + staffId + "}%' ");
      }
      if ((unitId != null) && (!unitId.equals("")) && (unitId.equals("0")))
      {
        String[] stoArr = unitId.split(",");
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
      ComFunController.queryDepartmentPrivs(singleSql, departmentPrivs, "department", "id");
      
      unionSql.append(singleSql);
      if ((orderTypes.length > 1) && (orderTypes.length - 1 != i)) {
        unionSql.append("union all ");
      }
    }
    return unionSql;
  }
}
