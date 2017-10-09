package com.aioerp.model.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class SellRankReports
  extends BaseDbModel
{
  public static final SellRankReports dao = new SellRankReports();
  
  public Map<String, Object> reportSellRankCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    String modelType = (String)paraMap.get("modelType");
    Integer prdSupId = (Integer)paraMap.get("supId");
    Integer unitId = (Integer)paraMap.get("unitId");
    Integer productId = (Integer)paraMap.get("productId");
    Integer staffId = (Integer)paraMap.get("staffId");
    Integer storageId = (Integer)paraMap.get("storageId");
    String[] orderTypes = (String[])paraMap.get("orderTypes");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    String aimDiv = paraMap.get("aimDiv").toString();
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    Integer node = Integer.valueOf(String.valueOf(paraMap.get("node")));
    

    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    String departmentPrivs = String.valueOf(paraMap.get("departmentPrivs"));
    String areaPrivs = String.valueOf(paraMap.get("areaPrivs"));
    String searchBaseAttr = String.valueOf(paraMap.get("searchBaseAttr"));
    String searchBaseVal = String.valueOf(paraMap.get("searchBaseVal"));
    

    StringBuffer privsSql = new StringBuffer();
    

    String str = "pids";
    String baseTableName = "";
    if (modelType.equals("prd"))
    {
      baseTableName = "b_product";
      if (prdSupId.intValue() == 0)
      {
        baseTableName = "(select d.* from cc_stock_init s left join b_product d on s.productId = d.id where s.productId is null limit 1 UNION all select * from b_product )";
        orderDirection = "desc";
      }
      ComFunController.queryProductPrivs(privsSql, productPrivs, "pr", "id");
      str = "pids";
    }
    else if (modelType.equals("unit"))
    {
      baseTableName = "b_unit";
      if (prdSupId.intValue() == 0)
      {
        baseTableName = "(select d.* from cw_accounts_init s left join b_unit d on s.unitId = d.id where s.unitId is null limit 1 UNION all select * from b_unit )";
        orderDirection = "desc";
      }
      ComFunController.queryUnitPrivs(privsSql, unitPrivs, "pr", "id");
      str = "unitPids";
    }
    else if (modelType.equals("staff"))
    {
      baseTableName = "b_staff";
      if (prdSupId.intValue() == 0)
      {
        baseTableName = "(select s.* from b_department d left join  b_staff s on s.depmId = d.id where s.depmId is null limit 1 UNION all select *from b_staff )";
        orderDirection = "desc";
      }
      ComFunController.queryStaffPrivs(privsSql, staffPrivs, "pr", "id");
      str = "staffPids";
    }
    else if (modelType.equals("dept"))
    {
      baseTableName = "b_department";
      if (prdSupId.intValue() == 0)
      {
        baseTableName = "(select d.* from xs_sell_bill s left join b_department d on s.departmentId = d.id where s.departmentId is null limit 1 UNION all select * from b_department )";
        orderDirection = "desc";
      }
      ComFunController.queryDepartmentPrivs(privsSql, departmentPrivs, "pr", "id");
      str = "departmentPids";
    }
    else if (modelType.equals("storage"))
    {
      baseTableName = "b_storage";
      if (prdSupId.intValue() == 0)
      {
        baseTableName = "(select d.* from cc_stock_init s left join b_storage d on s.storageId = d.id where s.storageId is null limit 1 UNION all select * from b_storage )";
        orderDirection = "desc";
      }
      ComFunController.queryStoragePrivs(privsSql, storagePrivs, "pr", "id");
      str = "storagePids";
    }
    else if (modelType.equals("area"))
    {
      baseTableName = "b_area";
      if (prdSupId.intValue() == 0)
      {
        baseTableName = "(select d.* from b_unit s left join b_area d on s.areaId = d.id where s.areaId is null limit 1 UNION all select * from b_area )";
        orderDirection = "desc";
      }
      ComFunController.queryAreaPrivs(privsSql, areaPrivs, "pr", "id");
      str = "areaPids";
    }
    StringBuffer childSql = SellReports.proCommSql(unitPrivs, staffPrivs, storagePrivs, productPrivs, orderTypes, unitId.intValue(), staffId.intValue(), storageId.intValue(), productId.intValue(), 0, startDate, endDate, searchBaseAttr, searchBaseVal, false);
    if (childSql.equals("")) {
      return null;
    }
    StringBuffer selectSql = new StringBuffer("select pr.*");
    StringBuffer fromSql = new StringBuffer();
    
    StringBuffer sellAmount = new StringBuffer();
    
    sellAmount.append(",sum(temp.baseAmount) sellAmount");
    selectSql.append(sellAmount);
    
    StringBuffer sellGiftAmount = new StringBuffer();
    
    sellGiftAmount.append(",sum(case when (temp.basePrice=0 or temp.basePrice is null) then temp.baseAmount else 0 end) sellGiftAmount");
    selectSql.append(sellGiftAmount);
    
    StringBuffer money = new StringBuffer();
    
    money.append(",sum(temp.money) money");
    selectSql.append(money);
    
    StringBuffer sellDiscountMoney = new StringBuffer();
    
    sellDiscountMoney.append(",sum(temp.discountMoney) sellDiscountMoney");
    selectSql.append(sellDiscountMoney);
    
    StringBuffer sellTaxes = new StringBuffer();
    
    sellTaxes.append(",sum(temp.taxes) sellTaxes");
    selectSql.append(sellTaxes);
    
    StringBuffer sellTaxMoney = new StringBuffer();
    
    sellTaxMoney.append(",sum(temp.taxMoney) sellTaxMoney");
    selectSql.append(sellTaxMoney);
    
    StringBuffer sellCostMoney = new StringBuffer();
    
    sellCostMoney.append(",sum(temp.baseAmount*temp.costPrice) sellCostMoney");
    selectSql.append(sellCostMoney);
    

    StringBuffer sellDiscountHasMoney = new StringBuffer();
    
    sellDiscountHasMoney.append(",sum(temp.money-temp.discountMoney) sellDiscountHasMoney");
    selectSql.append(sellDiscountHasMoney);
    
    StringBuffer avgPrice = new StringBuffer();
    
    avgPrice.append(",case when sum(temp.baseAmount)!=0 then sum(temp.money)/sum(temp.baseAmount) else 0 end avgPrice");
    selectSql.append(avgPrice);
    
    StringBuffer sellDiscountAvgPrice = new StringBuffer();
    
    sellDiscountAvgPrice.append(",case when sum(temp.baseAmount)!=0 then sum(temp.discountMoney)/sum(temp.baseAmount) else 0 end sellDiscountAvgPrice");
    selectSql.append(sellDiscountAvgPrice);
    
    StringBuffer sellTaxAvgPrice = new StringBuffer();
    
    sellTaxAvgPrice.append(",case when sum(temp.baseAmount)!=0 then sum(temp.taxMoney)/sum(temp.baseAmount) else 0 end sellTaxAvgPrice");
    selectSql.append(sellTaxAvgPrice);
    
    StringBuffer sellCostMoneyAvgPrice = new StringBuffer();
    
    sellCostMoneyAvgPrice.append(",case when sum(temp.baseAmount)!=0 then sum(temp.baseAmount*temp.costPrice)/sum(temp.baseAmount) else 0 end sellCostMoneyAvgPrice");
    selectSql.append(sellCostMoneyAvgPrice);
    
    StringBuffer profit = new StringBuffer();
    
    profit.append(",sum(temp.discountMoney-temp.baseAmount*temp.costPrice) profit");
    selectSql.append(profit);
    
    StringBuffer profitPercent = new StringBuffer();
    
    profitPercent.append(",case when sum(temp.discountMoney)!=0 then sum(temp.discountMoney-temp.baseAmount*temp.costPrice)*100/sum(temp.discountMoney) else 0 end profitPercent");
    selectSql.append(profitPercent);
    



    StringBuffer allSellPercent = new StringBuffer();
    
    allSellPercent.append(",(sum(temp.baseAmount))/((select sum(temp.baseAmount) from " + childSql + " temp)*100) allSellPercent ");
    selectSql.append(allSellPercent);
    
    StringBuffer allProfitPercent = new StringBuffer();
    
    allProfitPercent.append(",(sum(temp.discountMoney-temp.baseAmount*temp.costPrice))/((select sum(temp.discountMoney-temp.baseAmount*temp.costPrice) from " + childSql + " temp)*100) allProfitPercent");
    selectSql.append(allProfitPercent);
    


    fromSql.append(" from " + baseTableName + " pr");
    fromSql.append(" left join " + childSql + " temp on temp." + str + " like concat(pr.pids,'%')");
    fromSql.append(" where 1=1 ");
    

    boolean flag = SellReports.addReportBaseCondition(fromSql, "pr", searchBaseAttr, searchBaseVal);
    if (flag) {
      if (AioConstants.NODE_1 == node.intValue()) {
        fromSql.append(" and pr.pids like '%{" + prdSupId + "}%'");
      } else if (prdSupId.intValue() == 0) {
        fromSql.append(" and (pr.supId=0 or pr.supId is null)");
      } else {
        fromSql.append(" and pr.supId=" + prdSupId);
      }
    }
    fromSql.append(" and (pr.status=" + AioConstants.STATUS_ENABLE + " or pr.status is null)");
    if (AioConstants.NODE_1 == node.intValue()) {
      fromSql.append(" and pr.node=" + node);
    }
    fromSql.append(privsSql);
    
    String amount = "(select sum(temp.baseAmount) from " + childSql + " temp where temp." + str + " like concat(pr.pids,'%'))";
    if (!aimDiv.equals("all")) {
      if (aimDiv.equals("eq")) {
        fromSql.append(" and IFNULL(" + amount + ",0) = 0 ");
      } else if (aimDiv.equals("gt")) {
        fromSql.append(" and IFNULL(" + amount + ",0) != 0 ");
      }
    }
    fromSql.append(" group by pr.id ");
    fromSql.append(" order by " + orderField + " " + orderDirection);
    Map<String, Object> map = aioGetPageRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), "distinct pr.id", new Object[0]);
    
    List<Record> list = (List)map.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Record r = (Record)list.get(i);
      String helpAmount = DwzUtils.helpAmount(r.getBigDecimal("sellAmount"), r.getStr("calculateUnit1"), r.getStr("calculateUnit2"), r.getStr("calculateUnit3"), r.getBigDecimal("unitRelation2"), r.getBigDecimal("unitRelation3"));
      ((Record)list.get(i)).set("helpAmount", helpAmount);
    }
    map.put("pageList", list);
    return map;
  }
}
