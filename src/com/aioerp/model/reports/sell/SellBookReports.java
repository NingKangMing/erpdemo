package com.aioerp.model.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.CollectionUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellBookReports
  extends BaseDbModel
{
  public static final SellBookReports dao = new SellBookReports();
  
  public Map<String, Object> reportBookSearchCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    Integer unitId = (Integer)paraMap.get("unitId");
    Integer staffId = (Integer)paraMap.get("staffId");
    Integer storageId = (Integer)paraMap.get("storageId");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    String aimDiv = paraMap.get("aimDiv").toString();
    
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    


    StringBuffer singleSql = null;
    List<Object> paraList = new ArrayList();
    Map<String, Object> productMap = new HashMap();
    

    singleSql = new StringBuffer("select bill.*,detail.money,detail.discountMoney,detail.taxMoney from xs_sellbook_detail detail ");
    singleSql.append("left join ( ");
    singleSql.append("select b.*,unit.pids unitPids,unit.code unitCode,unit.fullName unitFullName,staff.pids staffPids,staff.code staffCode,staff.fullName staffFullName from xs_sellbook_bill b ");
    singleSql.append("left join b_unit unit on unit.id=b.unitId ");
    singleSql.append("left join b_staff staff on staff.id=b.staffId");
    singleSql.append(") bill on bill.id =detail.billId ");
    singleSql.append(" left join b_storage storage on storage.id=bill.storageId ");
    singleSql.append("where (bill.status=" + AioConstants.BILL_STATUS0 + " or bill.status=" + AioConstants.BILL_STATUS3 + ") ");
    if (!aimDiv.equals("all")) {
      if (aimDiv.equals("notOver")) {
        singleSql.append(" and bill.relStatus=" + AioConstants.STATUS_DISABLE + " ");
      } else if (aimDiv.equals("over")) {
        singleSql.append(" and (bill.relStatus=" + AioConstants.STATUS_ENABLE + " or bill.relStatus=" + AioConstants.STATUS_FORCE + ") ");
      }
    }
    if ((startDate != null) && (!startDate.equals("")))
    {
      singleSql.append("and (bill.recodeDate between ? and ?) ");
      paraList.add(startDate);
      paraList.add(endDate);
    }
    if (unitId.intValue() != 0) {
      singleSql.append("and bill.unitPids like '%{" + unitId + "}%' ");
    }
    if (staffId.intValue() != 0) {
      singleSql.append("and bill.staffPids like '%{" + staffId + "}%' ");
    }
    if (storageId.intValue() != 0) {
      singleSql.append("and storage.pids like '%{" + storageId + "}%' ");
    }
    ComFunController.queryUnitPrivs(singleSql, unitPrivs, "bill");
    ComFunController.queryStaffPrivs(singleSql, staffPrivs, "bill");
    ComFunController.queryStoragePrivs(singleSql, storagePrivs, "storage", "id");
    



    List<Record> orderList = Db.use(configName).find(singleSql.toString(), paraList.toArray());
    


    Map<Integer, Record> map = new HashMap();
    int key;
    for (int i = 0; i < orderList.size(); i++)
    {
      Record r = (Record)orderList.get(i);
      key = r.getInt("id").intValue();
      if (map.containsKey(Integer.valueOf(key)))
      {
        Record oldRecord = (Record)map.get(Integer.valueOf(key));
        oldRecord.set("money", BigDecimalUtils.add(r.getBigDecimal("money"), oldRecord.getBigDecimal("money")));
        oldRecord.set("discountMoney", BigDecimalUtils.add(r.getBigDecimal("discountMoney"), oldRecord.getBigDecimal("discountMoney")));
        oldRecord.set("taxMoney", BigDecimalUtils.add(r.getBigDecimal("taxMoney"), oldRecord.getBigDecimal("taxMoney")));
        map.put(Integer.valueOf(key), oldRecord);
      }
      else
      {
        map.put(Integer.valueOf(key), r);
      }
    }
    List newList = new ArrayList();
    for (Integer key1 : map.keySet())
    {
      Record r = (Record)map.get(key1);
      newList.add(r);
    }
    CollectionUtils.recordArraySort(newList, orderField, orderDirection);
    SellReports.page(listID, newList, pageNum.intValue(), numPerPage.intValue(), productMap);
    return productMap;
  }
  
  public Map<String, Object> reportBookExcuteDetail(String configName, String listID, int pageNum, int numPerPage, int billId)
    throws SQLException, Exception
  {
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("staff", Staff.class);
    map.put("department", Department.class);
    
    String sel = "select bill.*,department.*, staff.*";
    
    StringBuffer from = new StringBuffer(" from ");
    from.append(" (");
    from.append(" SELECT bill.isRCW,bill.id ,bill.code,bill.updateTime,bill.memo,bill.staffId,bill.departmentId,case when bill.isRCW!=2 then sum(detail.taxMoney) else 0-sum(detail.taxMoney) end as sellMoney,bill.remark ,'xsd' as 'orderType',bill.status,bill.relStatus");
    from.append(" FROM xs_sell_bill bill");
    from.append(" right join (select d.* from xs_sell_detail d where d.detailId in (select c.id from xs_sellbook_detail c where c.billId=" + billId + ") )  as detail");
    from.append(" on bill.id = detail.billId  where bill.status=" + AioConstants.BILL_STATUS0 + " or  bill.status=" + AioConstants.BILL_STATUS3 + " group by detail.billId");
    
    from.append(" UNION ALL");
    from.append(" SELECT null isRCW,bill.id ,bill.code,bill.updateTime,bill.memo,bill.staffId,null as 'departmentId',bill.moneys sellMoney,bill.remark,'xsdd' as 'orderType',bill.status,bill.relStatus");
    from.append(" FROM xs_sellbook_bill bill");
    from.append(" where bill.id = " + billId + " and bill.relStatus = " + AioConstants.STATUS_FORCE);
    from.append(" ) as bill");
    from.append(" left join b_staff as staff on bill.staffId = staff.id  ");
    from.append(" left join b_department as department on bill.departmentId = department.id ");
    from.append(" order by bill.updateTime");
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel, from.toString(), map, new Object[0]);
  }
  
  public Map<String, Object> reportBookCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String modelType = (String)paraMap.get("modelType");
    Integer productId = (Integer)paraMap.get("productId");
    Integer unitId = (Integer)paraMap.get("unitId");
    Integer staffId = (Integer)paraMap.get("staffId");
    Integer storageId = (Integer)paraMap.get("storageId");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    String aimDiv = paraMap.get("aimDiv").toString();
    
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    

    StringBuffer singleSql = null;
    List<Object> paraList = new ArrayList();
    Map<String, Object> productMap = new HashMap();
    

    boolean hasUnit = true;
    boolean hasStaff = true;
    boolean hasProduct = true;
    boolean hasStorage = true;
    if (modelType.equals("prd"))
    {
      singleSql = new StringBuffer("select product.id objId,detail.*,product.code objCode,product.fullName objFullName, ");
      singleSql.append("product.calculateUnit1,product.calculateUnit2,product.calculateUnit3,product.unitRelation2,product.unitRelation2 from xs_sellbook_detail detail ");
      singleSql.append("left join (select sb.*,u.pids unitPids,s.pids staffPids,storage.pids storagePids from xs_sellbook_bill sb left join b_unit u on u.id=sb.unitId left join b_staff s on s.id=sb.staffId left join b_storage storage on storage.id=sb.storageId)  bill on bill.id=detail.billId ");
      singleSql.append("left join b_product product on product.id=detail.productId where 1=1 ");
      singleSql.append("and (bill.status=" + AioConstants.BILL_STATUS0 + " or bill.status=" + AioConstants.BILL_STATUS3 + ") ");
    }
    else if (modelType.equals("unit"))
    {
      singleSql = new StringBuffer("select bill.objId,detail.*,bill.objCode,bill.objFullName from xs_sellbook_detail detail ");
      singleSql.append("left join ( ");
      singleSql.append("select b.relStatus,b.status billStatus,b.id billId,b.status,b.recodeDate,b.staffId,b.unitId,storage.pids storagePids,unit.pids unitPids,unit.id objId,unit.code objCode,unit.fullName objFullName,staff.pids staffPids from xs_sellbook_bill b ");
      singleSql.append("left join b_unit unit on unit.id=b.unitId ");
      singleSql.append("left join b_staff staff on staff.id=b.staffId ");
      singleSql.append("left join b_storage storage on storage.id=b.storageId ");
      singleSql.append(") bill on bill.billId=detail.billId ");
      singleSql.append(" left join b_product product on product.id=detail.productId where 1=1 ");
      singleSql.append("and (bill.billStatus=" + AioConstants.BILL_STATUS0 + " or bill.billStatus=" + AioConstants.BILL_STATUS3 + ") ");
    }
    else if (modelType.equals("staff"))
    {
      hasUnit = false;
      hasStaff = false;
      hasProduct = false;
      hasStorage = false;
      singleSql = new StringBuffer("select bill.objId,detail.*,bill.objCode,bill.objFullName from xs_sellbook_detail detail ");
      singleSql.append("left join ( ");
      singleSql.append("select b.relStatus,b.status billStatus,b.id billId,b.status,b.recodeDate,b.unitId,b.staffId,staff.id objId, staff.code objCode,staff.fullName objFullName from xs_sellbook_bill b ");
      singleSql.append("left join b_staff staff on staff.id=b.staffId ");
      singleSql.append(") bill on bill.billId=detail.billId where 1=1 ");
      singleSql.append("and (bill.billStatus=" + AioConstants.BILL_STATUS0 + " or bill.billStatus=" + AioConstants.BILL_STATUS3 + ") ");
    }
    if (!aimDiv.equals("all")) {
      if (aimDiv.equals("notOver")) {
        singleSql.append(" and bill.relStatus=" + AioConstants.STATUS_DISABLE + " ");
      } else if (aimDiv.equals("over")) {
        singleSql.append(" and (bill.relStatus=" + AioConstants.STATUS_ENABLE + " or bill.relStatus=" + AioConstants.STATUS_FORCE + ") ");
      }
    }
    if ((startDate != null) && (!startDate.equals("")))
    {
      singleSql.append("and (bill.recodeDate between ? and ?) ");
      paraList.add(startDate);
      paraList.add(endDate);
    }
    if ((productId.intValue() != 0) && (hasProduct)) {
      singleSql.append("and product.pids like '%{" + productId + "}%' ");
    }
    if ((unitId.intValue() != 0) && (hasUnit)) {
      singleSql.append("and bill.unitPids like '%{" + unitId + "}%' ");
    }
    if ((staffId.intValue() != 0) && (hasStaff)) {
      singleSql.append("and bill.staffPids like '%{" + staffId + "}%' ");
    }
    if ((storageId.intValue() != 0) && (hasStorage)) {
      singleSql.append("and bill.storagePids like '%{" + storageId + "}%' ");
    }
    ComFunController.queryProductPrivs(singleSql, productPrivs, "product", "id");
    ComFunController.queryUnitPrivs(singleSql, unitPrivs, "bill");
    ComFunController.queryStaffPrivs(singleSql, staffPrivs, "bill");
    ComFunController.queryStoragePrivs(singleSql, storagePrivs, "bill");
    

    List<Record> orderList = Db.use(configName).find(singleSql.toString(), paraList.toArray());
    


    Map<Integer, Record> map = new HashMap();
    Integer key;
    for (int i = 0; i < orderList.size(); i++)
    {
      Record r = (Record)orderList.get(i);
      key = Integer.valueOf(r.getInt("objId") == null ? 0 : r.getInt("objId").intValue());
      BigDecimal basePrice = r.getBigDecimal("basePrice");
      BigDecimal baseAmount = r.getBigDecimal("baseAmount");
      BigDecimal untreatedAmount = r.getBigDecimal("untreatedAmount");
      BigDecimal arrivalAmount = r.getBigDecimal("arrivalAmount");
      BigDecimal forceAmount = r.getBigDecimal("forceAmount");
      BigDecimal replenishAmount = r.getBigDecimal("replenishAmount");
      

      BigDecimal taxMoney = r.getBigDecimal("taxMoney");
      BigDecimal untreatedMoney = BigDecimalUtils.mul(untreatedAmount, basePrice);
      BigDecimal arrivalMoney = BigDecimalUtils.mul(arrivalAmount, basePrice);
      BigDecimal forceMoney = BigDecimalUtils.mul(forceAmount, basePrice);
      BigDecimal replenishMoney = BigDecimalUtils.mul(replenishAmount, basePrice);
      BigDecimal presentAmount = BigDecimal.ZERO;
      if (BigDecimalUtils.compare(basePrice, BigDecimal.ZERO) == 0) {
        presentAmount = baseAmount;
      }
      if (map.containsKey(key))
      {
        Record oldRecord = (Record)map.get(key);
        oldRecord.set("baseAmount", BigDecimalUtils.add(baseAmount, oldRecord.getBigDecimal("baseAmount")));
        oldRecord.set("taxMoney", BigDecimalUtils.add(taxMoney, oldRecord.getBigDecimal("taxMoney")));
        oldRecord.set("basePrice", BigDecimalUtils.div(oldRecord.getBigDecimal("taxMoney"), oldRecord.getBigDecimal("baseAmount")));
        
        oldRecord.set("untreatedAmount", BigDecimalUtils.add(untreatedAmount, oldRecord.getBigDecimal("untreatedAmount")));
        oldRecord.set("untreatedMoney", BigDecimalUtils.add(untreatedMoney, oldRecord.getBigDecimal("untreatedMoney")));
        
        oldRecord.set("arrivalAmount", BigDecimalUtils.add(arrivalAmount, oldRecord.getBigDecimal("arrivalAmount")));
        oldRecord.set("arrivalMoney", BigDecimalUtils.add(arrivalMoney, oldRecord.getBigDecimal("arrivalMoney")));
        
        oldRecord.set("forceAmount", BigDecimalUtils.add(forceAmount, oldRecord.getBigDecimal("forceAmount")));
        oldRecord.set("forceMoney", BigDecimalUtils.add(forceMoney, oldRecord.getBigDecimal("forceMoney")));
        
        oldRecord.set("replenishAmount", BigDecimalUtils.add(replenishAmount, oldRecord.getBigDecimal("replenishAmount")));
        oldRecord.set("replenishMoney", BigDecimalUtils.add(replenishMoney, oldRecord.getBigDecimal("replenishMoney")));
        
        oldRecord.set("presentAmount", BigDecimalUtils.add(presentAmount, oldRecord.getBigDecimal("presentAmount")));
        if (modelType.equals("prd"))
        {
          oldRecord.set("baseAmountHelp", getRecordHelpAmount(oldRecord.getBigDecimal("baseAmount"), oldRecord));
          oldRecord.set("untreatedAmountHelp", getRecordHelpAmount(oldRecord.getBigDecimal("untreatedAmount"), oldRecord));
          oldRecord.set("arrivalAmountHelp", getRecordHelpAmount(oldRecord.getBigDecimal("arrivalAmount"), oldRecord));
          oldRecord.set("forceAmountHelp", getRecordHelpAmount(oldRecord.getBigDecimal("forceAmount"), oldRecord));
          oldRecord.set("replenishAmountHelp", getRecordHelpAmount(oldRecord.getBigDecimal("replenishAmount"), oldRecord));
          oldRecord.set("presentAmountHelp", getRecordHelpAmount(oldRecord.getBigDecimal("presentAmount"), oldRecord));
        }
        map.put(key, oldRecord);
      }
      else
      {
        r.set("baseAmount", baseAmount);
        r.set("taxMoney", taxMoney);
        r.set("basePrice", BigDecimalUtils.div(taxMoney, baseAmount));
        
        r.set("untreatedAmount", untreatedAmount);
        r.set("untreatedMoney", untreatedMoney);
        
        r.set("arrivalAmount", arrivalAmount);
        r.set("arrivalMoney", arrivalMoney);
        
        r.set("forceAmount", forceAmount);
        r.set("forceMoney", forceMoney);
        
        r.set("replenishAmount", replenishAmount);
        r.set("replenishMoney", replenishMoney);
        
        r.set("presentAmount", presentAmount);
        if (modelType.equals("prd"))
        {
          r.set("baseAmountHelp", getRecordHelpAmount(baseAmount, r));
          r.set("untreatedAmountHelp", getRecordHelpAmount(untreatedAmount, r));
          r.set("arrivalAmountHelp", getRecordHelpAmount(arrivalAmount, r));
          r.set("forceAmountHelp", getRecordHelpAmount(forceAmount, r));
          r.set("replenishAmountHelp", getRecordHelpAmount(replenishAmount, r));
          r.set("presentAmountHelp", getRecordHelpAmount(presentAmount, r));
        }
        map.put(key, r);
      }
    }
    List newList = new ArrayList();
    for (Integer key1 : map.keySet())
    {
      Record r = (Record)map.get(key1);
      newList.add(r);
    }
    CollectionUtils.recordArraySort(newList, orderField, orderDirection);
    SellReports.page(listID, newList, pageNum.intValue(), numPerPage.intValue(), productMap);
    return productMap;
  }
  
  public String getRecordHelpAmount(BigDecimal amount, Record r)
  {
    return DwzUtils.helpAmount(amount, r.getStr("calculateUnit1"), r.getStr("calculateUnit2"), r.getStr("calculateUnit3"), r.getBigDecimal("unitRelation2"), r.getBigDecimal("unitRelation3"));
  }
  
  public Map<String, Object> reportBookDetailCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    Integer productId = (Integer)paraMap.get("productId");
    Integer unitId = (Integer)paraMap.get("unitId");
    Integer staffId = (Integer)paraMap.get("staffId");
    Integer storageId = (Integer)paraMap.get("storageId");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    

    StringBuffer singleSql = null;
    List<Object> paraList = new ArrayList();
    Map<String, Object> productMap = new HashMap();
    

    singleSql = new StringBuffer("select detail.*,bill.*,product.code productCode,product.fullName productFullName from xs_sellbook_detail detail ");
    singleSql.append("left join ( ");
    singleSql.append("select b.status billStatus,b.id billId,b.code billCode, b.recodeDate,b.deliveryDate,b.remark billRemark,b.memo billMemo,b.unitId,b.storageId,b.staffId,unit.pids unitPids,unit.code unitCode,unit.fullName unitFullName,storage.pids storagePids,storage.code storageCode,storage.fullName storageFullName,staff.pids staffPids,staff.code staffCode,staff.fullName staffFullName ");
    singleSql.append("from xs_sellbook_bill b ");
    singleSql.append("left join b_unit unit on unit.id=b.unitId ");
    singleSql.append("left join b_storage storage on storage.id=b.storageId ");
    singleSql.append("left join b_staff staff on staff.id=b.staffId ");
    singleSql.append(") bill on bill.billId=detail.billId ");
    singleSql.append("left join b_product product on product.id=detail.productId where 1=1 ");
    singleSql.append("and (bill.billStatus=" + AioConstants.BILL_STATUS0 + " or bill.billStatus=" + AioConstants.BILL_STATUS3 + ") ");
    if ((startDate != null) && (!startDate.equals("")))
    {
      singleSql.append("and (bill.recodeDate between ? and ?) ");
      paraList.add(startDate);
      paraList.add(endDate);
    }
    if (productId.intValue() != 0) {
      singleSql.append("and product.pids like '%{" + productId + "}%' ");
    }
    if (unitId.intValue() != 0) {
      singleSql.append("and bill.unitPids like '%{" + unitId + "}%' ");
    }
    if (staffId.intValue() != 0) {
      singleSql.append("and bill.staffPids like '%{" + staffId + "}%' ");
    }
    if (storageId.intValue() != 0) {
      singleSql.append("and bill.storagePids like '%{" + storageId + "}%' ");
    }
    ComFunController.queryProductPrivs(singleSql, productPrivs, "product", "id");
    ComFunController.queryUnitPrivs(singleSql, unitPrivs, "bill");
    ComFunController.queryStaffPrivs(singleSql, staffPrivs, "bill");
    ComFunController.queryStoragePrivs(singleSql, storagePrivs, "bill");
    

    List<Record> orderList = Db.use(configName).find(singleSql.toString(), paraList.toArray());
    


    Map<Integer, Record> map = new HashMap();
    int key;
    for (int i = 0; i < orderList.size(); i++)
    {
      Record r = (Record)orderList.get(i);
      key = r.getInt("id").intValue();
      BigDecimal basePrice = r.getBigDecimal("basePrice");
      BigDecimal taxMoney = r.getBigDecimal("taxMoney");
      BigDecimal replenishMoney = BigDecimalUtils.mul(r.getBigDecimal("replenishAmount"), basePrice);
      BigDecimal arrivalMoney = BigDecimalUtils.mul(r.getBigDecimal("arrivalAmount"), basePrice);
      BigDecimal forceMoney = BigDecimalUtils.mul(r.getBigDecimal("forceAmount"), basePrice);
      BigDecimal untreatedMoney = BigDecimalUtils.mul(r.getBigDecimal("untreatedAmount"), basePrice);
      r.set("untreatedMoney", untreatedMoney);
      BigDecimal presentAmount = BigDecimal.ZERO;
      if (BigDecimalUtils.compare(basePrice, BigDecimal.ZERO) == 0) {
        presentAmount = r.getBigDecimal("baseAmount");
      }
      if (map.containsKey(Integer.valueOf(key)))
      {
        Record oldRecord = (Record)map.get(Integer.valueOf(key));
        oldRecord.set("baseAmount", BigDecimalUtils.add(r.getBigDecimal("baseAmount"), oldRecord.getBigDecimal("baseAmount")));
        oldRecord.set("replenishAmount", BigDecimalUtils.add(r.getBigDecimal("replenishAmount"), oldRecord.getBigDecimal("replenishAmount")));
        oldRecord.set("arrivalAmount", BigDecimalUtils.add(r.getBigDecimal("arrivalAmount"), oldRecord.getBigDecimal("arrivalAmount")));
        oldRecord.set("forceAmount", BigDecimalUtils.add(r.getBigDecimal("forceAmount"), oldRecord.getBigDecimal("forceAmount")));
        oldRecord.set("untreatedAmount", BigDecimalUtils.add(r.getBigDecimal("untreatedAmount"), oldRecord.getBigDecimal("untreatedAmount")));
        oldRecord.set("taxMoney", BigDecimalUtils.add(taxMoney, oldRecord.getBigDecimal("taxMoney")));
        oldRecord.set("basePrice", BigDecimalUtils.div(oldRecord.getBigDecimal("taxMoney"), oldRecord.getBigDecimal("baseAmount")));
        oldRecord.set("replenishMoney", BigDecimalUtils.add(replenishMoney, oldRecord.getBigDecimal("replenishMoney")));
        oldRecord.set("arrivalMoney", BigDecimalUtils.add(arrivalMoney, oldRecord.getBigDecimal("arrivalMoney")));
        oldRecord.set("forceMoney", BigDecimalUtils.add(forceMoney, oldRecord.getBigDecimal("forceMoney")));
        oldRecord.set("untreatedMoney", BigDecimalUtils.add(untreatedMoney, oldRecord.getBigDecimal("untreatedMoney")));
        oldRecord.set("presentAmount", BigDecimalUtils.add(presentAmount, oldRecord.getBigDecimal("presentAmount")));
        map.put(Integer.valueOf(key), oldRecord);
      }
      else
      {
        r.set("replenishMoney", replenishMoney);
        r.set("arrivalMoney", arrivalMoney);
        r.set("forceMoney", forceMoney);
        r.set("untreatedMoney", untreatedMoney);
        r.set("presentAmount", presentAmount);
        map.put(Integer.valueOf(key), r);
      }
    }
    List newList = new ArrayList();
    for (Integer key1 : map.keySet())
    {
      Record r = (Record)map.get(key1);
      newList.add(r);
    }
    CollectionUtils.recordArraySort(newList, orderField, orderDirection);
    SellReports.page(listID, newList, pageNum.intValue(), numPerPage.intValue(), productMap);
    return productMap;
  }
}
