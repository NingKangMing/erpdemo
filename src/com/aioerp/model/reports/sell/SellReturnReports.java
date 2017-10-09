package com.aioerp.model.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class SellReturnReports
  extends BaseDbModel
{
  public static final SellReturnReports dao = new SellReturnReports();
  
  public Map<String, Object> reportPrdSellReturnCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    Integer prdSupId = (Integer)paraMap.get("supId");
    Integer productId = (Integer)paraMap.get("productId");
    Integer unitId = (Integer)paraMap.get("unitId");
    Integer staffId = (Integer)paraMap.get("staffId");
    Integer storageId = (Integer)paraMap.get("storageId");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    String[] orderTypes = { "xsd", "xsthd", "xshhd" };
    Integer node = Integer.valueOf(String.valueOf(paraMap.get("node")));
    
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    String searchBaseAttr = String.valueOf(paraMap.get("searchBaseAttr"));
    String searchBaseVal = String.valueOf(paraMap.get("searchBaseVal"));
    



    StringBuffer childSql = SellReports.proCommSql(unitPrivs, staffPrivs, storagePrivs, productPrivs, orderTypes, unitId.intValue(), staffId.intValue(), storageId.intValue(), productId.intValue(), 0, startDate, endDate, searchBaseAttr, searchBaseVal, false);
    if (childSql.equals("")) {
      return null;
    }
    StringBuffer selectSql = new StringBuffer("select pr.*");
    StringBuffer fromSql = new StringBuffer();
    

    StringBuffer sellAmount = new StringBuffer();
    
    sellAmount.append(",sum(temp.baseAmount) sellAmount");
    selectSql.append(sellAmount);
    
    StringBuffer sellTaxMoney = new StringBuffer();
    sellTaxMoney.append(",sum(temp.discountMoney) sellTaxMoney");
    selectSql.append(sellTaxMoney);
    

    StringBuffer sellOutAmount = new StringBuffer();
    
    sellOutAmount.append(",sum(case when temp.type=2 then temp.baseAmount else 0 end) sellOutAmount");
    selectSql.append(sellOutAmount);
    
    StringBuffer sellOutMoney = new StringBuffer();
    
    sellOutMoney.append(",sum(case when temp.type=2 then temp.discountMoney else 0 end) sellOutMoney");
    selectSql.append(sellOutMoney);
    


    StringBuffer sellInAmount = new StringBuffer();
    
    sellInAmount.append(",sum(case when temp.type=1 then (temp.baseAmount*-1) else 0 end) sellInAmount");
    selectSql.append(sellInAmount);
    
    StringBuffer sellInMoney = new StringBuffer();
    
    sellInMoney.append(",sum(case when temp.type=1 then (temp.discountMoney*-1) else 0 end) sellInMoney");
    selectSql.append(sellInMoney);
    


    StringBuffer returnPrecent = new StringBuffer();
    
    returnPrecent.append(",case when sum(case when temp.type=2 then temp.baseAmount else 0 end)!=0 then sum(case when temp.type=1 then temp.baseAmount*-1 else 0 end)*100/sum(case when temp.type=2 then temp.baseAmount else 0 end) else 0 end returnPrecent");
    selectSql.append(returnPrecent);
    


    fromSql.append(" from b_product pr ");
    fromSql.append(" left join " + childSql + " temp on temp.pids like concat(pr.pids,'%')");
    fromSql.append(" where 1=1 ");
    

    boolean flag = SellReports.addReportBaseCondition(fromSql, "pr", searchBaseAttr, searchBaseVal);
    if (flag) {
      if (AioConstants.NODE_1 == node.intValue()) {
        fromSql.append(" and pr.pids like '%{" + prdSupId + "}%'");
      } else if (prdSupId.intValue() == 0)
      {
        if (productId.intValue() != 0) {
          fromSql.append(" and pr.pids like '%{" + productId + "}%'");
        } else {
          fromSql.append(" and pr.supId=0");
        }
      }
      else {
        fromSql.append(" and pr.supId=" + prdSupId);
      }
    }
    fromSql.append(" and pr.status=" + AioConstants.STATUS_ENABLE);
    if (AioConstants.NODE_1 == node.intValue()) {
      fromSql.append(" and pr.node=" + node);
    }
    ComFunController.queryProductPrivs(fromSql, productPrivs, "pr", "id");
    
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
  
  public Model prdSellReturnMerageModel(String type, Model oldMode, Record newModel)
  {
    int isRCW = newModel.getInt("isRCW").intValue();
    
    BigDecimal amount = newModel.getBigDecimal("baseAmount");
    if (type.equals("add"))
    {
      if (isRCW == AioConstants.RCW_VS)
      {
        oldMode.put("baseAmount", BigDecimalUtils.sub(oldMode.getBigDecimal("baseAmount"), amount));
        oldMode.put("taxMoney", BigDecimalUtils.sub(oldMode.getBigDecimal("taxMoney"), newModel.getBigDecimal("taxMoney")));
        
        oldMode.put("sellAmount", BigDecimalUtils.sub(oldMode.getBigDecimal("sellAmount"), amount));
        oldMode.put("sellTaxMoney", BigDecimalUtils.sub(oldMode.getBigDecimal("sellTaxMoney"), newModel.getBigDecimal("taxMoney")));
      }
      else
      {
        oldMode.put("baseAmount", BigDecimalUtils.add(oldMode.getBigDecimal("baseAmount"), amount));
        oldMode.put("taxMoney", BigDecimalUtils.add(oldMode.getBigDecimal("taxMoney"), newModel.getBigDecimal("taxMoney")));
        
        oldMode.put("sellAmount", BigDecimalUtils.add(oldMode.getBigDecimal("sellAmount"), amount));
        oldMode.put("sellTaxMoney", BigDecimalUtils.add(oldMode.getBigDecimal("sellTaxMoney"), newModel.getBigDecimal("taxMoney")));
      }
    }
    else if (type.equals("sub")) {
      if (isRCW == AioConstants.RCW_VS)
      {
        oldMode.put("baseAmount", BigDecimalUtils.add(oldMode.getBigDecimal("baseAmount"), amount));
        oldMode.put("taxMoney", BigDecimalUtils.add(oldMode.getBigDecimal("taxMoney"), newModel.getBigDecimal("taxMoney")));
        
        oldMode.put("sellReturnAmount", BigDecimalUtils.add(oldMode.getBigDecimal("sellReturnAmount"), amount));
        oldMode.put("sellReturnTaxMoney", BigDecimalUtils.add(oldMode.getBigDecimal("sellReturnTaxMoney"), newModel.getBigDecimal("taxMoney")));
      }
      else
      {
        oldMode.put("baseAmount", BigDecimalUtils.sub(oldMode.getBigDecimal("baseAmount"), amount));
        oldMode.put("taxMoney", BigDecimalUtils.sub(oldMode.getBigDecimal("taxMoney"), newModel.getBigDecimal("taxMoney")));
        
        oldMode.put("sellReturnAmount", BigDecimalUtils.add(oldMode.getBigDecimal("sellReturnAmount"), amount));
        oldMode.put("sellReturnTaxMoney", BigDecimalUtils.add(oldMode.getBigDecimal("sellReturnTaxMoney"), newModel.getBigDecimal("taxMoney")));
      }
    }
    oldMode.put("sellReutrnPercent", BigDecimalUtils.mul(BigDecimalUtils.div(oldMode.getBigDecimal("sellReturnAmount"), oldMode.getBigDecimal("sellAmount")), new BigDecimal(100)));
    return oldMode;
  }
  
  public Map<String, Object> reportUnitSellReturnCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    Integer unitSupId = (Integer)paraMap.get("supId");
    Integer unitId = (Integer)paraMap.get("unitId");
    Integer productId = (Integer)paraMap.get("productId");
    Integer staffId = (Integer)paraMap.get("staffId");
    Integer storageId = (Integer)paraMap.get("storageId");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    String[] orderTypes = { "xsd", "xsthd", "xshhd" };
    Integer node = Integer.valueOf(String.valueOf(paraMap.get("node")));
    
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    String searchBaseAttr = String.valueOf(paraMap.get("searchBaseAttr"));
    String searchBaseVal = String.valueOf(paraMap.get("searchBaseVal"));
    



    StringBuffer childSql = SellReports.unitCommSql(unitPrivs, staffPrivs, storagePrivs, productPrivs, orderTypes, productId.intValue(), staffId.intValue(), storageId.intValue(), startDate, endDate, searchBaseAttr, searchBaseVal);
    if (childSql.equals("")) {
      return null;
    }
    StringBuffer selectSql = new StringBuffer("select pr.*");
    StringBuffer fromSql = new StringBuffer();
    



    StringBuffer sellTaxMoney = new StringBuffer();
    
    sellTaxMoney.append(",sum(case when (temp.type=2) then temp.discountMoney else 0 end) sellTaxMoney");
    selectSql.append(sellTaxMoney);
    
    StringBuffer sellReturnTaxMoney = new StringBuffer();
    
    sellReturnTaxMoney.append(",sum(case when (temp.type=1) then temp.discountMoney*-1 else 0 end) sellReturnTaxMoney");
    selectSql.append(sellReturnTaxMoney);
    

    StringBuffer returnPrecent = new StringBuffer();
    
    returnPrecent.append(",case when sum(case when temp.type=2 then temp.discountMoney else 0 end)!=0 then sum(case when temp.type=1 then temp.discountMoney*-1 else 0 end)*100/sum(case when temp.type=2 then temp.discountMoney else 0 end) else 0 end returnPrecent");
    selectSql.append(returnPrecent);
    


    fromSql.append(" from b_unit pr");
    fromSql.append(" left join " + childSql + " temp on temp.pids like concat(pr.pids,'%')");
    fromSql.append(" where 1=1");
    

    boolean flag = SellReports.addReportBaseCondition(fromSql, "pr", searchBaseAttr, searchBaseVal);
    if (flag) {
      if (AioConstants.NODE_1 == node.intValue()) {
        fromSql.append(" and pr.pids like '%{" + unitSupId + "}%'");
      } else if (unitSupId.intValue() == 0)
      {
        if (unitId.intValue() != 0) {
          fromSql.append(" and pr.pids like '%{" + unitId + "}%'");
        } else {
          fromSql.append(" and pr.supId=0");
        }
      }
      else {
        fromSql.append(" and pr.supId=" + unitSupId);
      }
    }
    fromSql.append(" and pr.status=" + AioConstants.STATUS_ENABLE);
    if (AioConstants.NODE_1 == node.intValue()) {
      fromSql.append(" and pr.node=" + node);
    }
    ComFunController.queryUnitPrivs(fromSql, unitPrivs, "pr", "id");
    if ((unitPrivs == null) || (unitPrivs.equals(""))) {
      fromSql.append(" and pr.id =0");
    } else if (!unitPrivs.equals(AioConstants.ALL_PRIVS)) {
      fromSql.append(" and pr.id in (" + unitPrivs + ")");
    }
    fromSql.append(" group by pr.id");
    fromSql.append(" order by " + orderField + " " + orderDirection);
    Map<String, Object> map = aioGetPageRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), "distinct pr.id", new Object[0]);
    return map;
  }
  
  public Model unitSellReturnMerageModel(String type, Model oldMode, Record newModel)
  {
    int isRCW = newModel.getInt("isRCW").intValue();
    if (type.equals("add"))
    {
      if (isRCW == AioConstants.RCW_VS) {
        oldMode.put("sellTaxMoney", BigDecimalUtils.sub(oldMode.getBigDecimal("sellTaxMoney"), newModel.getBigDecimal("taxMoney")));
      } else {
        oldMode.put("sellTaxMoney", BigDecimalUtils.add(oldMode.getBigDecimal("sellTaxMoney"), newModel.getBigDecimal("taxMoney")));
      }
    }
    else if (type.equals("sub")) {
      if (isRCW == AioConstants.RCW_VS) {
        oldMode.put("sellReturnTaxMoney", BigDecimalUtils.sub(oldMode.getBigDecimal("sellReturnTaxMoney"), newModel.getBigDecimal("taxMoney")));
      } else {
        oldMode.put("sellReturnTaxMoney", BigDecimalUtils.add(oldMode.getBigDecimal("sellReturnTaxMoney"), newModel.getBigDecimal("taxMoney")));
      }
    }
    oldMode.put("sellReutrnPercent", BigDecimalUtils.mul(BigDecimalUtils.div(oldMode.getBigDecimal("sellReturnTaxMoney"), oldMode.getBigDecimal("sellTaxMoney")), new BigDecimal(100)));
    return oldMode;
  }
}
