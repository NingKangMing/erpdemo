package com.aioerp.model.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.CollectionUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SellPrivilegeReports
  extends BaseDbModel
{
  public static final SellPrivilegeReports dao = new SellPrivilegeReports();
  
  public Map<String, Object> reportSPrivilege(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String modelType = (String)paraMap.get("modelType");
    Integer unitId = (Integer)paraMap.get("unitId");
    Integer staffId = (Integer)paraMap.get("staffId");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String departmentPrivs = String.valueOf(paraMap.get("departmentPrivs"));
    

    StringBuffer unionSql = new StringBuffer();
    StringBuffer singleSql = null;
    String billTable = "";
    
    String[] orderTypes = { "xsd", "xsthd", "xshhd" };
    for (int i = 0; i < orderTypes.length; i++)
    {
      if (orderTypes[i].equals("xsd")) {
        billTable = "xs_sell_bill";
      } else if (orderTypes[i].equals("xsthd")) {
        billTable = "xs_return_bill";
      } else if (orderTypes[i].equals("xshhd")) {
        billTable = "xs_barter_bill";
      }
      String comSql = "";
      boolean hasUnit = false;
      boolean hasStaff = false;
      singleSql = new StringBuffer();
      if (modelType.equals("unit"))
      {
        comSql = "unit.*";
        hasStaff = true;
      }
      else if (modelType.equals("dept"))
      {
        comSql = "dept.*";
        hasUnit = true;
        hasStaff = true;
      }
      else if (modelType.equals("staff"))
      {
        comSql = "staff.*";
      }
      singleSql.append("select " + comSql);
      if (orderTypes[i].equals("xsd"))
      {
        singleSql.append("," + AioConstants.BILL_ROW_TYPE4 + " billTypeId");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.taxMoneys else bill.taxMoneys*-1 end) as taxMoneys1");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilege else bill.privilege*-1 end) as privilege1");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilegeMoney else bill.privilegeMoney*-1 end) as privilegeMoney1");
      }
      else if (orderTypes[i].equals("xsthd"))
      {
        singleSql.append("," + AioConstants.BILL_ROW_TYPE7 + " billTypeId");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.taxMoneys*-1 else bill.taxMoneys end) as taxMoneys1");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilege*-1 else bill.privilege end) as privilege1");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilegeMoney*-1 else bill.privilegeMoney end) as privilegeMoney1");
      }
      else if (orderTypes[i].equals("xshhd"))
      {
        singleSql.append("," + AioConstants.BILL_ROW_TYPE13 + " billTypeId");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.gapMoney else bill.gapMoney*-1 end) as taxMoneys1");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilege else bill.privilege*-1 end) as privilege1");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilegeMoney else bill.privilegeMoney*-1 end) as privilegeMoney1");
      }
      singleSql.append(" from " + billTable + " bill ");
      singleSql.append("left join b_unit unit  on bill.unitId=unit.id ");
      singleSql.append("left join b_staff staff  on bill.staffId=staff.id ");
      singleSql.append("left join b_department dept on bill.departmentId=dept.id where 1=1 ");
      singleSql.append("and (bill.recodeDate between '" + startDate + "' and '" + endDate + "') ");
      if ((unitId != null) && (hasUnit)) {
        singleSql.append("and unit.pids like '%{" + unitId + "}%' ");
      }
      if ((staffId != null) && (hasStaff)) {
        singleSql.append("and staff.pids like '%{" + staffId + "}%' ");
      }
      if (modelType.equals("unit")) {
        ComFunController.queryUnitPrivs(singleSql, unitPrivs, "unit", "id");
      } else if (modelType.equals("dept")) {
        ComFunController.queryDepartmentPrivs(singleSql, departmentPrivs, "dept", "id");
      } else if (modelType.equals("staff")) {
        ComFunController.queryStaffPrivs(singleSql, staffPrivs, "staff", "id");
      }
      unionSql.append(singleSql);
      if ((orderTypes.length > 1) && (orderTypes.length - 1 != i)) {
        unionSql.append(" union all ");
      }
    }
    StringBuffer selectSql = new StringBuffer();
    StringBuffer fromSql = new StringBuffer();
    selectSql.append("select t.*");
    selectSql.append(",sum(t.taxMoneys1) taxMoneys");
    selectSql.append(",sum(t.privilege1) privilege");
    selectSql.append(",sum(t.privilegeMoney1) privilegeMoney");
    fromSql.append(" from (");
    fromSql.append(unionSql);
    fromSql.append(") t where 1=1");
    if (modelType.equals("unit")) {
      singleSql.append("group by bill.unitId ");
    } else if (modelType.equals("dept")) {
      singleSql.append("group by bill.departmentId");
    } else if (modelType.equals("staff")) {
      singleSql.append("group by bill.staffId");
    }
    fromSql.append(" order by " + orderField + " " + orderDirection);
    Map<String, Object> map = aioGetPageRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), null, new Object[0]);
    return map;
  }
  
  public Model prdMerageModel(String type, Model oldMode, Record newModel)
  {
    BigDecimal price = newModel.getBigDecimal("price");
    BigDecimal amount = newModel.getBigDecimal("baseAmount");
    if (type.equals("add"))
    {
      if (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)
      {
        oldMode.put("presentAmount", BigDecimalUtils.add(oldMode.getBigDecimal("presentAmount"), amount));
        oldMode.put("presentMoney", BigDecimalUtils.add(oldMode.getBigDecimal("presentMoney"), newModel.getBigDecimal("costMoneys")));
      }
      oldMode.put("baseAmount", BigDecimalUtils.add(oldMode.getBigDecimal("baseAmount"), amount));
      oldMode.put("discountMoney", BigDecimalUtils.add(oldMode.getBigDecimal("discountMoney"), newModel.getBigDecimal("discountMoney")));
      oldMode.put("avgDiscountPrice", BigDecimalUtils.div(oldMode.getBigDecimal("discountMoney"), oldMode.getBigDecimal("baseAmount")));
      
      oldMode.put("taxes", BigDecimalUtils.add(oldMode.getBigDecimal("taxes"), newModel.getBigDecimal("taxes")));
      oldMode.put("taxMoney", BigDecimalUtils.add(oldMode.getBigDecimal("taxMoney"), newModel.getBigDecimal("taxMoney")));
      oldMode.put("avgTaxPrice", BigDecimalUtils.div(oldMode.getBigDecimal("taxMoney"), oldMode.getBigDecimal("baseAmount")));
    }
    else if (type.equals("sub"))
    {
      if (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)
      {
        oldMode.put("presentAmount", BigDecimalUtils.sub(oldMode.getBigDecimal("presentAmount"), amount));
        oldMode.put("presentMoney", BigDecimalUtils.sub(oldMode.getBigDecimal("presentMoney"), newModel.getBigDecimal("costMoneys")));
      }
      oldMode.put("baseAmount", BigDecimalUtils.sub(oldMode.getBigDecimal("baseAmount"), amount));
      oldMode.put("discountMoney", BigDecimalUtils.sub(oldMode.getBigDecimal("discountMoney"), newModel.getBigDecimal("discountMoney")));
      oldMode.put("avgDiscountPrice", BigDecimalUtils.div(oldMode.getBigDecimal("discountMoney"), oldMode.getBigDecimal("baseAmount")));
      
      oldMode.put("taxes", BigDecimalUtils.sub(oldMode.getBigDecimal("taxes"), newModel.getBigDecimal("taxes")));
      oldMode.put("taxMoney", BigDecimalUtils.sub(oldMode.getBigDecimal("taxMoney"), newModel.getBigDecimal("taxMoney")));
      oldMode.put("avgTaxPrice", BigDecimalUtils.div(oldMode.getBigDecimal("taxMoney"), oldMode.getBigDecimal("baseAmount")));
    }
    return oldMode;
  }
  
  public Map<String, Object> reportUnitSellCount(String configName, Map<String, Object> paraMap)
  {
    Integer unitSupId = (Integer)paraMap.get("supId");
    String productId = (String)paraMap.get("productId");
    String staffId = (String)paraMap.get("staffId");
    String storageId = (String)paraMap.get("storageId");
    String[] orderTypes = (String[])paraMap.get("orderTypes");
    String startDate = paraMap.get("startDate").toString();
    String endDate = paraMap.get("endDate").toString();
    String aimDiv = paraMap.get("aimDiv").toString();
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    



    StringBuffer unionSql = new StringBuffer();
    StringBuffer singleSql = null;
    List<Object> paraList = new ArrayList();
    String unitIds = "";
    String billTable = "";
    String billDetailTable = "";
    Map<Integer, Model> unitMerageMap = new HashMap();
    Map<Integer, Integer> unitPidsMap = new HashMap();
    Map<Integer, Map<String, Record>> unitBillMap = new HashMap();
    Map<String, Object> unitMap = new HashMap();
    int unitId = 0;
    


    List<Model> unitList = Unit.dao.find(configName, "select unit.* from b_unit unit where unit.supId=? and unit.status=" + AioConstants.STATUS_ENABLE + " order by unit.rank", new Object[] { unitSupId });
    



    StringBuffer pidsSql = new StringBuffer("select unit.id,unit.pids from b_unit unit ");
    for (int i = 0; i < unitList.size(); i++)
    {
      unitId = ((Unit)unitList.get(i)).getInt("id").intValue();
      if (i == 0) {
        pidsSql.append("where unit.pids like '%{" + unitId + "}%' ");
      } else {
        pidsSql.append("or unit.pids like '%{" + unitId + "}%' ");
      }
      pidsSql.append(" and unit.node=" + AioConstants.NODE_1 + " ");
      ((Model)unitList.get(i)).put("baseAmount", BigDecimal.ZERO);
      ((Model)unitList.get(i)).put("avgDiscountPrice", BigDecimal.ZERO);
      ((Model)unitList.get(i)).put("discountMoney", BigDecimal.ZERO);
      
      ((Model)unitList.get(i)).put("taxes", BigDecimal.ZERO);
      ((Model)unitList.get(i)).put("avgTaxPrice", BigDecimal.ZERO);
      ((Model)unitList.get(i)).put("taxMoney", BigDecimal.ZERO);
      
      ((Model)unitList.get(i)).put("presentAmount", BigDecimal.ZERO);
      ((Model)unitList.get(i)).put("presentMoney", BigDecimal.ZERO);
      ((Model)unitList.get(i)).put("costMoneys", BigDecimal.ZERO);
      

      ((Model)unitList.get(i)).put("sellCounts", BigDecimal.ZERO);
      ((Model)unitList.get(i)).put("privileges", BigDecimal.ZERO);
      ((Model)unitList.get(i)).put("privilegeMoneys", BigDecimal.ZERO);
      ((Model)unitList.get(i)).put("helpAmount", "");
      
      unitMerageMap.put(Integer.valueOf(unitId), (Model)unitList.get(i));
      unitBillMap.put(Integer.valueOf(unitId), new HashMap());
    }
    List<Record> unitPidsList = Db.use(configName).find(pidsSql.toString(), paraList.toArray());
    for (int i = 0; i < unitPidsList.size(); i++)
    {
      unitId = ((Record)unitPidsList.get(i)).getInt("id").intValue();
      String pids = ((Record)unitPidsList.get(i)).getStr("pids");
      unitIds = unitIds + "," + unitId;
      for (Integer key : unitMerageMap.keySet()) {
        if (pids.contains("{" + key + "}")) {
          unitPidsMap.put(Integer.valueOf(unitId), key);
        }
      }
    }
    if (!unitIds.equals("")) {
      unitIds = unitIds.substring(1, unitIds.length());
    }
    for (int i = 0; i < orderTypes.length; i++)
    {
      if (orderTypes[i].equals("xsd"))
      {
        billTable = "xs_sell_bill";
        billDetailTable = "xs_sell_detail";
      }
      else if (orderTypes[i].equals("xsthd"))
      {
        billTable = "xs_return_bill";
        billDetailTable = "xs_return_detail";
      }
      else
      {
        if (orderTypes[i].equals("xshhd"))
        {
          billTable = "";
          billDetailTable = "";
          continue;
        }
        if (orderTypes[i].equals("jzxsd"))
        {
          billTable = "";
          billDetailTable = "";
          continue;
        }
        if (orderTypes[i].equals("wtjsd"))
        {
          billTable = "";
          billDetailTable = "";
          continue;
        }
      }
      singleSql = new StringBuffer();
      


      singleSql.append("select '" + orderTypes[i] + "' sqlOrderType,bill.id,bill.unitId,bill.privilege,bill.privilegeMoney,detail.baseAmount,detail.price,detail.discountMoney,detail.taxes,detail.taxMoney,detail.costMoneys ");
      singleSql.append("from " + billDetailTable + " detail left join " + billTable + " bill on detail.billId=bill.id where bill.unitId in (" + unitIds + ") ");
      
      singleSql.append("and (bill.recodeDate between ? and ?) ");
      paraList.add(startDate);
      paraList.add(endDate);
      if (!productId.equals("0")) {
        singleSql.append("and detail.productId in(" + productId + ") ");
      }
      if (!staffId.equals("0")) {
        singleSql.append("and bill.staffId in(" + staffId + ") ");
      }
      if (!storageId.equals("0")) {
        singleSql.append("and detail.storageId in(" + storageId + ") ");
      }
      unionSql.append(singleSql);
      if ((orderTypes.length > 1) && (orderTypes.length - 1 != i)) {
        unionSql.append("union all ");
      }
    }
    List<Record> orderList = Db.use(configName).find(unionSql.toString(), paraList.toArray());
    

    Record orderModel = null;
    Model unitModel = null;
    for (int i = 0; i < orderList.size(); i++)
    {
      orderModel = (Record)orderList.get(i);
      unitId = orderModel.getInt("unitId").intValue();
      unitId = ((Integer)unitPidsMap.get(Integer.valueOf(unitId))).intValue();
      String sqlOrderType = orderModel.getStr("sqlOrderType");
      unitModel = (Model)unitMerageMap.get(Integer.valueOf(unitId));
      if (sqlOrderType.equals("xsd"))
      {
        unitModel = prdMerageModel("add", unitModel, orderModel);
      }
      else if (sqlOrderType.equals("xsthd"))
      {
        unitModel = prdMerageModel("sub", unitModel, orderModel);
      }
      else
      {
        if (sqlOrderType.equals("xshhd")) {
          continue;
        }
        if (sqlOrderType.equals("jzxsd")) {
          continue;
        }
        if (sqlOrderType.equals("wtjsd")) {
          continue;
        }
      }
      unitMerageMap.put(Integer.valueOf(unitId), unitModel);
      
      Map<String, Record> billMap = (Map)unitBillMap.get(Integer.valueOf(unitId));
      String key = sqlOrderType + "-" + orderModel.getInt("id");
      if (!billMap.containsKey(key)) {
        billMap.put(key, orderModel);
      }
      unitBillMap.put(Integer.valueOf(unitId), billMap);
    }
    BigDecimal sellAmounts = BigDecimal.ZERO;
    BigDecimal totalDiscountMoneys = BigDecimal.ZERO;
    BigDecimal taxs = BigDecimal.ZERO;
    BigDecimal taxMoneys = BigDecimal.ZERO;
    BigDecimal preSentAmounts = BigDecimal.ZERO;
    BigDecimal preSentSinglePrices = BigDecimal.ZERO;
    BigDecimal preSentMoneys = BigDecimal.ZERO;
    BigDecimal totalSellCount = BigDecimal.ZERO;
    BigDecimal totalPrivilege = BigDecimal.ZERO;
    BigDecimal totalPrivilegeMoney = BigDecimal.ZERO;
    BigDecimal sellAmount = BigDecimal.ZERO;
    List newList = new ArrayList();
    for (Iterator localIterator2 = unitMerageMap.keySet().iterator(); localIterator2.hasNext();)
    {
      int key = ((Integer)localIterator2.next()).intValue();
      Model m = (Model)unitMerageMap.get(Integer.valueOf(key));
      sellAmount = m.getBigDecimal("baseAmount");
      if (!aimDiv.equals("all"))
      {
        if ((!aimDiv.equals("eq")) || (BigDecimalUtils.compare(sellAmount, BigDecimal.ZERO) == 0)) {
          if ((aimDiv.equals("gt")) && (BigDecimalUtils.compare(sellAmount, BigDecimal.ZERO) < 1)) {}
        }
      }
      else
      {
        sellAmounts = BigDecimalUtils.add(sellAmounts, sellAmount);
        totalDiscountMoneys = BigDecimalUtils.add(totalDiscountMoneys, m.getBigDecimal("discountMoney"));
        taxs = BigDecimalUtils.add(taxs, m.getBigDecimal("taxes"));
        taxMoneys = BigDecimalUtils.add(taxMoneys, m.getBigDecimal("taxMoney"));
        preSentAmounts = BigDecimalUtils.add(preSentAmounts, m.getBigDecimal("presentAmount"));
        preSentSinglePrices = BigDecimalUtils.add(preSentSinglePrices, m.getBigDecimal("retailPrice1"));
        preSentMoneys = BigDecimalUtils.add(preSentMoneys, m.getBigDecimal("presentMoney"));
        



        Map<String, Record> billMap1 = (Map)unitBillMap.get(Integer.valueOf(key));
        BigDecimal privileges = BigDecimal.ZERO;
        BigDecimal privilegeMoneys = BigDecimal.ZERO;
        for (String billKey : billMap1.keySet())
        {
          String[] strs = billKey.split("-");
          String billOrderType = strs[0];
          Record billRecord = (Record)billMap1.get(billKey);
          if (billOrderType.equals("xsd"))
          {
            privileges = BigDecimalUtils.add(privileges, billRecord.getBigDecimal("privilege"));
            privilegeMoneys = BigDecimalUtils.add(privilegeMoneys, billRecord.getBigDecimal("privilegeMoney"));
          }
          else if (billOrderType.equals("xsthd"))
          {
            privileges = BigDecimalUtils.sub(privileges, billRecord.getBigDecimal("privilege"));
            privilegeMoneys = BigDecimalUtils.sub(privilegeMoneys, billRecord.getBigDecimal("privilegeMoney"));
          }
          else if (!billOrderType.equals("xshhd"))
          {
            if (!billOrderType.equals("jzxsd")) {
              if (!billOrderType.equals("wtjsd")) {}
            }
          }
        }
        m.put("sellCounts", Integer.valueOf(billMap1.size()));
        m.put("privileges", privileges);
        m.put("privilegeMoneys", privilegeMoneys);
        
        totalSellCount = BigDecimalUtils.add(totalSellCount, new BigDecimal(billMap1.size()));
        totalPrivilege = BigDecimalUtils.add(totalPrivilege, privileges);
        totalPrivilegeMoney = BigDecimalUtils.add(totalPrivilegeMoney, privilegeMoneys);
        newList.add(m);
      }
    }
    CollectionUtils.arraySort(newList, orderField, orderDirection);
    unitMap.put("pageList", newList);
    unitMap.put("sellAmounts", sellAmounts);
    unitMap.put("totalDiscountMoneys", totalDiscountMoneys);
    unitMap.put("taxs", taxs);
    unitMap.put("taxMoneys", taxMoneys);
    unitMap.put("preSentAmounts", preSentAmounts);
    unitMap.put("preSentSinglePrices", preSentSinglePrices);
    unitMap.put("preSentMoneys", preSentMoneys);
    unitMap.put("totalSellCount", totalSellCount);
    unitMap.put("totalPrivilege", totalPrivilege);
    unitMap.put("totalPrivilegeMoney", totalPrivilegeMoney);
    return unitMap;
  }
  
  public Map<String, Object> sellPrivilegeDetail(String configName, Map<String, Object> paraMap)
    throws Exception
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String modelType = paraMap.get("modelType").toString();
    String aimDiv = paraMap.get("aimDiv").toString();
    String unitId = (String)paraMap.get("unitId");
    String staffId = (String)paraMap.get("staffId");
    String departmentId = (String)paraMap.get("departmentId");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    



    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String departmentPrivs = String.valueOf(paraMap.get("departmentPrivs"));
    

    String billTable = "";
    StringBuffer singleSql = null;
    StringBuffer unionSql = new StringBuffer();
    



    String[] orderTypes = { "xsd", "xsthd", "xshhd" };
    for (int i = 0; i < orderTypes.length; i++)
    {
      if (orderTypes[i].equals("xsd"))
      {
        billTable = "xs_sell_bill";
      }
      else if (orderTypes[i].equals("xsthd"))
      {
        billTable = "xs_return_bill";
      }
      else if (orderTypes[i].equals("xshhd"))
      {
        billTable = "xs_barter_bill";
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
      singleSql = new StringBuffer();
      

      singleSql.append("select ");
      
      singleSql.append("bill.id,bill.isRCW,bill.code,bill.recodeDate,bill.remark,bill.unitId,bill.staffId,bill.departmentId");
      if (orderTypes[i].equals("xsd"))
      {
        singleSql.append("," + AioConstants.BILL_ROW_TYPE4 + " billTypeId,'销售单' billTypeName");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.taxMoneys else bill.taxMoneys*-1 end) as taxMoneys");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilege else bill.privilege*-1 end) as privilege");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilegeMoney else bill.privilegeMoney*-1 end) as privilegeMoney");
      }
      else if (orderTypes[i].equals("xsthd"))
      {
        singleSql.append("," + AioConstants.BILL_ROW_TYPE7 + " billTypeId,'销售退货单' billTypeName");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.taxMoneys*-1 else bill.taxMoneys end) as taxMoneys");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilege*-1 else bill.privilege end) as privilege");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilegeMoney*-1 else bill.privilegeMoney end) as privilegeMoney");
      }
      else if (orderTypes[i].equals("xshhd"))
      {
        singleSql.append("," + AioConstants.BILL_ROW_TYPE13 + " billTypeId,'销售换货单' billTypeName");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.gapMoney else bill.gapMoney*-1 end) as taxMoneys");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilege else bill.privilege*-1 end) as privilege");
        singleSql.append(",sum(case when bill.isRCW != 2 then bill.privilegeMoney else bill.privilegeMoney*-1 end) as privilegeMoney");
      }
      singleSql.append(" from ");
      singleSql.append(billTable + " bill ");
      singleSql.append(" left join b_unit u on u.id=bill.unitId");
      singleSql.append(" left join b_department d on d.id=bill.departmentId");
      singleSql.append(" left join b_staff s on s.id=bill.staffId");
      singleSql.append(" where 1=1");
      singleSql.append(" and (bill.recodeDate between '" + startDate + "' and '" + endDate + "') ");
      if ((unitId != null) && (!unitId.equals(""))) {
        singleSql.append(" and u.pids like '%{" + unitId + "}%'");
      }
      if (modelType.equals("dept"))
      {
        if ((departmentId == null) || (departmentId.equals(""))) {
          singleSql.append(" and d.id is null");
        } else {
          singleSql.append(" and d.pids like '%{" + departmentId + "}%'");
        }
        if ((staffId != null) && (!staffId.equals(""))) {
          singleSql.append(" and s.pids like '%{" + staffId + "}%'");
        }
      }
      else if (modelType.equals("staff"))
      {
        if ((staffId == null) || (staffId.equals(""))) {
          singleSql.append(" and s.id is null");
        } else {
          singleSql.append(" and s.pids like '%{" + staffId + "}%'");
        }
        if ((departmentId != null) && (!departmentId.equals(""))) {
          singleSql.append(" and d.pids like '%{" + departmentId + "}%'");
        }
      }
      ComFunController.queryUnitPrivs(singleSql, unitPrivs, "u", "id");
      ComFunController.queryDepartmentPrivs(singleSql, departmentPrivs, "d", "id");
      ComFunController.queryStaffPrivs(singleSql, staffPrivs, "s", "id");
      singleSql.append(" group by bill.id");
      
      unionSql.append(singleSql);
      if ((orderTypes.length > 1) && (orderTypes.length - 1 != i)) {
        unionSql.append(" union all ");
      }
    }
    StringBuffer selectSql = new StringBuffer();
    StringBuffer formSql = new StringBuffer();
    selectSql.append("select t.*,unit.*,department.*,staff.* ");
    formSql.append(" from (");
    formSql.append(unionSql);
    formSql.append(" ) t ");
    formSql.append(" left join b_unit unit on unit.id=t.unitId");
    formSql.append(" left join b_department department on department.id=t.departmentId");
    formSql.append(" left join b_staff staff on staff.id=t.staffId");
    formSql.append(" where 1=1 and t.id is not null and t.id!=0 ");
    if ((!aimDiv.equals("all")) && 
      (aimDiv.equals("notRedRow"))) {
      singleSql.append(" and t.isRCW=" + AioConstants.RCW_NO);
    }
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("unit", Unit.class);
    map.put("staff", Staff.class);
    map.put("department", Department.class);
    Map<String, Object> pageMap = aioGetPageManyRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), formSql.toString(), map, new Object[0]);
    return pageMap;
  }
}
