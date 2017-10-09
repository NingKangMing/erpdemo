package com.aioerp.model.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Area;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellReports
  extends BaseDbModel
{
  public static final SellReports dao = new SellReports();
  
  public Map<String, Object> reportPrdSellCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    Integer prdSupId = (Integer)paraMap.get("supId");
    Integer unitId = (Integer)paraMap.get("unitId");
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
    String searchBaseAttr = String.valueOf(paraMap.get("searchBaseAttr"));
    String searchBaseVal = String.valueOf(paraMap.get("searchBaseVal"));
    

    StringBuffer childSql = proCommSql(unitPrivs, staffPrivs, storagePrivs, productPrivs, orderTypes, unitId.intValue(), staffId.intValue(), storageId.intValue(), 0, 0, startDate, endDate, searchBaseAttr, searchBaseVal, false);
    if (childSql.length() == 0) {
      return null;
    }
    StringBuffer selectSql = new StringBuffer("select pr.*");
    StringBuffer fromSql = new StringBuffer();
    
    StringBuffer sellAmount = new StringBuffer();
    sellAmount.append(",sum(temp.baseAmount) sellAmount");
    selectSql.append(sellAmount);
    
    StringBuffer sellDiscountMoney = new StringBuffer();
    sellDiscountMoney.append(",sum(temp.discountMoney) sellDiscountMoney");
    selectSql.append(sellDiscountMoney);
    
    StringBuffer sellTaxes = new StringBuffer();
    sellTaxes.append(",sum(temp.taxes) sellTaxes");
    selectSql.append(sellTaxes);
    
    StringBuffer sellTaxMoney = new StringBuffer();
    sellTaxMoney.append(",sum(temp.taxMoney) sellTaxMoney");
    selectSql.append(sellTaxMoney);
    
    StringBuffer sellGiftAmount = new StringBuffer();
    sellGiftAmount.append(",sum(case when (temp.basePrice=0 or temp.basePrice is null) then temp.baseAmount else 0 end) sellGiftAmount");
    selectSql.append(sellGiftAmount);
    
    StringBuffer sellGiftMoney = new StringBuffer();
    sellGiftMoney.append(",sum(case when (temp.basePrice=0 or temp.basePrice is null) then (temp.baseAmount*temp.costPrice) else 0 end) sellGiftMoney");
    selectSql.append(sellGiftMoney);
    
    StringBuffer sellGiftRetailPrice1 = new StringBuffer();
    sellGiftRetailPrice1.append(",sum(case when (temp.basePrice=0 or temp.basePrice is null) then (temp.baseAmount*pr.retailPrice1) else 0 end) sellGiftRetailPrice1");
    selectSql.append(sellGiftRetailPrice1);
    

    StringBuffer sellDiscountAvgPrice = new StringBuffer();
    sellDiscountAvgPrice.append(",case when sum(temp.baseAmount)!=0 then sum(temp.discountMoney)/sum(temp.baseAmount) else 0 end sellDiscountAvgPrice");
    selectSql.append(sellDiscountAvgPrice);
    
    StringBuffer sellTaxAvgPrice = new StringBuffer();
    sellTaxAvgPrice.append(",case when sum(temp.baseAmount)!=0 then sum(temp.taxMoney)/sum(temp.baseAmount) else 0 end sellTaxAvgPrice");
    selectSql.append(sellTaxAvgPrice);
    

    fromSql.append(" from b_product pr left join " + childSql + " temp on temp.pids like concat(pr.pids,'%') where 1=1");
    
    boolean flag = addReportBaseCondition(fromSql, "pr", searchBaseAttr, searchBaseVal);
    if (flag) {
      if (AioConstants.NODE_1 == node.intValue()) {
        fromSql.append(" and pr.pids like '%{" + prdSupId + "}%'");
      } else if (prdSupId.intValue() == 0) {
        fromSql.append(" and pr.supId=0");
      } else {
        fromSql.append(" and pr.supId=" + prdSupId);
      }
    }
    fromSql.append(" and pr.status=" + AioConstants.STATUS_ENABLE);
    if (AioConstants.NODE_1 == node.intValue()) {
      fromSql.append(" and pr.node=" + node);
    }
    ComFunController.queryProductPrivs(fromSql, productPrivs, "pr", "id");
    
    String amount = "(select sum(temp.baseAmount) bb from " + childSql + " temp where temp.pids like concat(pr.pids,'%'))";
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
      String helpAmount = DwzUtils.helpAmount(r.getBigDecimal("baseAmount"), r.getStr("calculateUnit1"), r.getStr("calculateUnit2"), r.getStr("calculateUnit3"), r.getBigDecimal("unitRelation2"), r.getBigDecimal("unitRelation3"));
      ((Record)list.get(i)).set("helpAmount", helpAmount);
    }
    map.put("pageList", list);
    return map;
  }
  
  public Map<String, Object> reportPrdSellDetailCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    Integer productId = (Integer)paraMap.get("productId");
    Integer unitId = (Integer)paraMap.get("unitId");
    Integer staffId = (Integer)paraMap.get("staffId");
    Integer storageId = (Integer)paraMap.get("storageId");
    Integer areaId = (Integer)paraMap.get("areaId");
    String[] orderTypes = (String[])paraMap.get("orderTypes");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    

    StringBuffer childSql = proCommSql(unitPrivs, staffPrivs, storagePrivs, productPrivs, orderTypes, unitId.intValue(), staffId.intValue(), storageId.intValue(), productId.intValue(), areaId.intValue(), startDate, endDate, null, null, true);
    if (childSql.length() == 0) {
      return null;
    }
    StringBuffer selectSql = new StringBuffer("select t.* ");
    StringBuffer fromSql = new StringBuffer();
    

    fromSql.append(" from  " + childSql + " t where 1=1");
    
    fromSql.append(" and t.status=" + AioConstants.STATUS_ENABLE);
    

    fromSql.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Object> map = aioGetPageRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), null, new Object[0]);
    
    List<Record> list = (List)map.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Record r = (Record)list.get(i);
      String helpAmount = DwzUtils.helpAmount(r.getBigDecimal("baseAmount"), r.getStr("calculateUnit1"), r.getStr("calculateUnit2"), r.getStr("calculateUnit3"), r.getBigDecimal("unitRelation2"), r.getBigDecimal("unitRelation3"));
      ((Record)list.get(i)).set("helpAmount", helpAmount);
    }
    map.put("pageList", list);
    return map;
  }
  
  public static StringBuffer proCommSql(String unitPrivs, String staffPrivs, String storagePrivs, String productPrivs, String[] orderTypes, int unitId, int staffId, int storageId, int productId, int areaId, String startDate, String endDate, String searchBaseAttr, String searchBaseVal, boolean needOther)
  {
    StringBuffer unionSql = new StringBuffer();
    StringBuffer singleSql = null;
    String billTable = "";
    String billDetailTable = "";
    if ((orderTypes != null) && (!orderTypes[0].equals("")))
    {
      unionSql.append("(");
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
        else if (orderTypes[i].equals("xshhd"))
        {
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
        singleSql = new StringBuffer("");
        
        singleSql.append(" select product.pids pids,detail.billId,detail.productId,detail.basePrice,detail.costPrice,detail.discount");
        if (needOther)
        {
          singleSql.append(",detail.memo,product.status,product.`code` productCode,product.fullName productFullName,unit.`code` unitCode,unit.fullName unitFullName,staff.`code` staffCode,staff.fullName staffFullName,department.`code` departmentCode,department.fullName departmentFullName,area.`code` areaCode,area.fullName areaFullName,`storage`.`code` storageCode,storage.fullName storageFullName");
          singleSql.append(",product.calculateUnit1,product.calculateUnit2,product.calculateUnit3,product.unitRelation2,product.unitRelation3");
        }
        singleSql.append(",bill.isRCW,bill.unitId,bill.staffId,bill.departmentId,unit.pids unitPids, staff.pids staffPids,department.pids departmentPids,area.id areaId,area.pids areaPids");
        singleSql.append(",bill.code,bill.recodeDate,bill.remark");
        singleSql.append(",storage.id bstroageId,storage.pids storagePids");
        if (orderTypes[i].equals("xsd"))
        {
          singleSql.append("," + AioConstants.BILL_ROW_TYPE4 + " billTypeId ,'销售单' billTypeName");
          singleSql.append(",case when bill.isRCW = 2 then detail.baseAmount*-1 else detail.baseAmount end baseAmount");
          singleSql.append(",case when bill.isRCW = 2 then detail.money*-1 else detail.money end money");
          singleSql.append(",case when bill.isRCW = 2 then detail.discountPrice*-1 else detail.discountPrice end discountPrice");
          singleSql.append(",case when bill.isRCW = 2 then detail.discountMoney*-1 else detail.discountMoney end discountMoney");
          singleSql.append(",case when bill.isRCW = 2 then detail.taxes*-1 else detail.taxes end taxes");
          singleSql.append(",case when bill.isRCW = 2 then detail.taxPrice*-1 else detail.taxPrice end taxPrice");
          singleSql.append(",case when bill.isRCW = 2 then detail.taxMoney*-1 else detail.taxMoney end taxMoney");
          singleSql.append(",case when bill.isRCW = 2 then detail.costMoneys*-1 else detail.costMoneys end costMoneys");
          singleSql.append(",2 type");
        }
        else if (orderTypes[i].equals("xsthd"))
        {
          singleSql.append("," + AioConstants.BILL_ROW_TYPE7 + " billTypeId ,'销售退货单' billTypeName");
          singleSql.append(",case when bill.isRCW = 2 then detail.baseAmount else detail.baseAmount*-1 end baseAmount");
          singleSql.append(",case when bill.isRCW = 2 then detail.money else detail.money*-1 end money");
          singleSql.append(",case when bill.isRCW = 2 then detail.discountPrice  else detail.discountPrice*-1 end discountPrice");
          singleSql.append(",case when bill.isRCW = 2 then detail.discountMoney else detail.discountMoney*-1 end discountMoney");
          singleSql.append(",case when bill.isRCW = 2 then detail.taxes else detail.taxes*-1 end taxes");
          singleSql.append(",case when bill.isRCW = 2 then detail.taxPrice else detail.taxPrice*-1 end taxPrice");
          singleSql.append(",case when bill.isRCW = 2 then detail.taxMoney else detail.taxMoney*-1 end taxMoney");
          singleSql.append(",case when bill.isRCW = 2 then detail.costMoneys else detail.costMoneys*-1 end costMoneys");
          singleSql.append(",1 type");
        }
        else if (orderTypes[i].equals("xshhd"))
        {
          singleSql.append("," + AioConstants.BILL_ROW_TYPE13 + " billTypeId ,'销售换货单' billTypeName");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.baseAmount when detail.type=2 then detail.baseAmount*-1 end else case when detail.type=1 then detail.baseAmount*-1 when detail.type=2 then detail.baseAmount end end  baseAmount");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.money when detail.type=2 then detail.money*-1 end  else case when detail.type=1 then detail.money*-1 when detail.type=2 then detail.money end end money");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.discountPrice when detail.type=2 then detail.discountPrice*-1 end  else case when detail.type=1 then detail.discountPrice*-1 when detail.type=2 then detail.discountPrice end end discountPrice");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.discountMoney when detail.type=2 then detail.discountMoney*-1 end  else case when detail.type=1 then detail.discountMoney*-1 when detail.type=2 then detail.discountMoney end end discountMoney");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.taxes when detail.type=2 then detail.taxes*-1 end else case when detail.type=1 then detail.taxes*-1 when detail.type=2 then detail.taxes end end taxes");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.taxPrice when detail.type=2 then detail.taxPrice*-1 end  else case when detail.type=1 then detail.taxPrice*-1 when detail.type=2 then detail.taxPrice end end taxPrice");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.taxMoney when detail.type=2 then detail.taxMoney*-1 end else case when detail.type=1 then detail.taxMoney*-1 when detail.type=2 then detail.taxMoney end end taxMoney");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.costMoneys when detail.type=2 then detail.costMoneys*-1 end else case when detail.type=1 then detail.costMoneys*-1 when detail.type=2 then detail.costMoneys end end costMoneys");
          singleSql.append(",detail.type");
        }
        singleSql.append(" from " + billDetailTable + " detail");
        singleSql.append(" left join " + billTable + " bill on bill.id=detail.billId");
        singleSql.append(" left join b_unit unit on unit.id=bill.unitId");
        singleSql.append(" left join b_staff staff on staff.id=bill.staffId");
        singleSql.append(" left join b_department department on department.id=bill.departmentId");
        singleSql.append(" left join b_area area on area.id=unit.areaId");
        

        singleSql.append(" left join b_product product on product.id=detail.productId");
        singleSql.append(" left join b_storage storage on storage.id=detail.storageId where 1=1");
        singleSql.append(" and (bill.recodeDate between '" + startDate + "' and '" + endDate + "') ");
        
        ComFunController.queryProductPrivs(singleSql, productPrivs, "detail");
        ComFunController.queryUnitPrivs(singleSql, unitPrivs, "bill");
        ComFunController.queryStoragePrivs(singleSql, storagePrivs, "detail");
        ComFunController.queryStaffPrivs(singleSql, staffPrivs, "bill");
        

        addReportBaseCondition(singleSql, "product", searchBaseAttr, searchBaseVal);
        if (unitId != 0) {
          singleSql.append(" and unit.pids like '%{" + unitId + "}%'");
        }
        if (staffId != 0) {
          singleSql.append(" and staff.pids like '%{" + staffId + "}%'");
        }
        if (storageId != 0) {
          singleSql.append(" and storage.pids like '%{" + storageId + "}%'");
        }
        if (productId != 0) {
          singleSql.append(" and product.pids like '%{" + productId + "}%'");
        }
        if (areaId != 0) {
          singleSql.append(" and area.pids like '%{" + areaId + "}%'");
        }
        unionSql.append(singleSql);
        if ((orderTypes.length > 1) && (orderTypes.length - 1 != i)) {
          unionSql.append(" union all ");
        }
      }
      unionSql.append(")");
    }
    return unionSql;
  }
  
  public static boolean addReportBaseCondition(StringBuffer singleSql, String alias, String searchBaseAttr, String searchBaseVal)
  {
    boolean flag = false;
    if (alias == null) {
      alias = "";
    }
    if ((searchBaseAttr != null) && (!searchBaseAttr.equals("")) && (searchBaseVal != null) && (!searchBaseVal.equals("")))
    {
      if (searchBaseAttr.equals("barCode")) {
        singleSql.append(" and (" + alias + ".barCode1 like '%" + searchBaseVal + "%' or " + alias + ".barCode2 like '%" + searchBaseVal + "%' or " + alias + ".barCode3 like '%" + searchBaseVal + "%')");
      } else {
        singleSql.append(" and " + alias + "." + searchBaseAttr + " like '%" + searchBaseVal + "%'");
      }
      singleSql.append(" and " + alias + ".node=" + AioConstants.NODE_1);
    }
    else
    {
      flag = true;
    }
    return flag;
  }
  
  public static StringBuffer unitCommSql(String unitPrivs, String staffPrivs, String storagePrivs, String productPrivs, String[] orderTypes, int productId, int staffId, int storageId, String startDate, String endDate, String searchBaseAttr, String searchBaseVal)
  {
    StringBuffer unionSql = new StringBuffer();
    StringBuffer singleSql = null;
    String billTable = "";
    String billDetailTable = "";
    if ((orderTypes != null) && (!orderTypes[0].equals("")))
    {
      unionSql.append("(");
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
        else if (orderTypes[i].equals("xshhd"))
        {
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
        singleSql = new StringBuffer("");
        singleSql.append(" select unit.pids,bill.id billId,bill.unitId,detail.basePrice,detail.costPrice,bill.isRCW");
        if (orderTypes[i].equals("xsd"))
        {
          singleSql.append(",case when bill.isRCW = 2 then bill.privilege*-1 else bill.privilege end privilege");
          singleSql.append(",case when bill.isRCW = 2 then bill.privilegeMoney*-1 else bill.privilegeMoney end privilegeMoney");
          singleSql.append(",case when bill.isRCW = 2 then -1 else 1 end sellCount");
        }
        else if (orderTypes[i].equals("xsthd"))
        {
          singleSql.append(",case when bill.isRCW = 2 then bill.privilege else bill.privilege*-1 end privilege");
          singleSql.append(",case when bill.isRCW = 2 then bill.privilegeMoney else bill.privilegeMoney*-1 end privilegeMoney");
          singleSql.append(",0 sellCount");
        }
        else if (orderTypes[i].equals("xshhd"))
        {
          singleSql.append(",case when bill.isRCW = 2 then bill.privilege*-1 else bill.privilege end privilege");
          singleSql.append(",case when bill.isRCW = 2 then bill.privilegeMoney*-1 else bill.privilegeMoney end privilegeMoney");
          singleSql.append(",1 sellCount");
        }
        if (orderTypes[i].equals("xsd"))
        {
          singleSql.append("," + AioConstants.BILL_ROW_TYPE4 + " billTypeId");
          singleSql.append(",case when bill.isRCW = 2 then detail.baseAmount*-1 else detail.baseAmount end baseAmount");
          singleSql.append(",case when bill.isRCW = 2 then detail.discountMoney*-1 else detail.discountMoney end discountMoney");
          singleSql.append(",case when bill.isRCW = 2 then detail.taxes*-1 else detail.taxes end taxes");
          singleSql.append(",case when bill.isRCW = 2 then detail.taxMoney*-1 else detail.taxMoney end taxMoney");
          singleSql.append(",case when bill.isRCW = 2 then detail.costMoneys*-1 else detail.costMoneys end costMoneys");
          singleSql.append(",2 type");
        }
        else if (orderTypes[i].equals("xsthd"))
        {
          singleSql.append("," + AioConstants.BILL_ROW_TYPE7 + " billTypeId");
          singleSql.append(",case when bill.isRCW = 2 then detail.baseAmount else detail.baseAmount*-1 end baseAmount");
          singleSql.append(",case when bill.isRCW = 2 then detail.discountMoney else detail.discountMoney*-1 end discountMoney");
          singleSql.append(",case when bill.isRCW = 2 then detail.taxes else detail.taxes*-1 end taxes");
          singleSql.append(",case when bill.isRCW = 2 then detail.taxMoney else detail.taxMoney*-1 end taxMoney");
          singleSql.append(",case when bill.isRCW = 2 then detail.costMoneys else detail.costMoneys*-1 end costMoneys");
          singleSql.append(",1 type");
        }
        else if (orderTypes[i].equals("xshhd"))
        {
          singleSql.append("," + AioConstants.BILL_ROW_TYPE13 + " billTypeId");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.baseAmount when detail.type=2 then detail.baseAmount*-1 end else case when detail.type=1 then detail.baseAmount*-1 when detail.type=2 then detail.baseAmount end end  baseAmount");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.discountMoney when detail.type=2 then detail.discountMoney*-1 end else case when detail.type=1 then detail.discountMoney*-1 when detail.type=2 then detail.discountMoney end end discountMoney");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.taxes when detail.type=2 then detail.taxes*-1 end else case when detail.type=1 then detail.taxes*-1 when detail.type=2 then detail.taxes end end taxes");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.taxMoney when detail.type=2 then detail.taxMoney*-1 end else case when detail.type=1 then detail.taxMoney*-1 when detail.type=2 then detail.taxMoney end end taxMoney");
          singleSql.append(",case when bill.isRCW = 2 then case when detail.type=1 then detail.costMoneys when detail.type=2 then detail.costMoneys*-1 end else case when detail.type=1 then detail.costMoneys*-1 when detail.type=2 then detail.costMoneys end end costMoneys");
          singleSql.append(",detail.type");
        }
        singleSql.append(",case when bill.isRCW = 2 then case when (detail.price is null or detail.price=0) then detail.baseAmount*product.retailPrice1*-1 else 0 end else case when (detail.price is null or detail.price=0) then detail.baseAmount*product.retailPrice1 else 0 end end retailMoney");
        

        singleSql.append(" from " + billDetailTable + " detail");
        singleSql.append(" left join " + billTable + " bill on bill.id=detail.billId ");
        singleSql.append(" left join b_unit unit on unit.id = bill.unitId");
        singleSql.append(" left join b_staff staff on staff.id = bill.staffId");
        singleSql.append(" left join b_product product on product.id=detail.productId");
        singleSql.append(" left join b_storage storage on storage.id=detail.storageId where 1=1");
        singleSql.append(" and (bill.recodeDate between '" + startDate + "' and '" + endDate + "') ");
        
        ComFunController.queryProductPrivs(singleSql, productPrivs, "detail");
        ComFunController.queryUnitPrivs(singleSql, unitPrivs, "bill");
        ComFunController.queryStoragePrivs(singleSql, storagePrivs, "detail");
        ComFunController.queryStaffPrivs(singleSql, staffPrivs, "bill");
        

        addReportBaseCondition(singleSql, "unit", searchBaseAttr, searchBaseVal);
        if (productId != 0) {
          singleSql.append(" and product.pids like '%{" + productId + "}%'");
        }
        if (staffId != 0) {
          singleSql.append(" and staff.pids like '%{" + staffId + "}%'");
        }
        if (storageId != 0) {
          singleSql.append(" and storage.pids like '%{" + storageId + "}%'");
        }
        unionSql.append(singleSql);
        if ((orderTypes.length > 1) && (orderTypes.length - 1 != i)) {
          unionSql.append(" union all ");
        }
      }
      unionSql.append(")");
    }
    return unionSql;
  }
  
  public Map<String, Object> reportUnitSellCount(String configName, Map<String, Object> paraMap)
    throws ParseException
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    Integer unitSupId = (Integer)paraMap.get("supId");
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
    String searchBaseAttr = String.valueOf(paraMap.get("searchBaseAttr"));
    String searchBaseVal = String.valueOf(paraMap.get("searchBaseVal"));
    




    StringBuffer childSql = unitCommSql(unitPrivs, staffPrivs, storagePrivs, productPrivs, orderTypes, productId.intValue(), staffId.intValue(), storageId.intValue(), startDate, endDate, searchBaseAttr, searchBaseVal);
    if (childSql.equals("")) {
      return null;
    }
    StringBuffer selectSql = new StringBuffer("select pr.*");
    StringBuffer fromSql = new StringBuffer();
    
    StringBuffer sellCounts = new StringBuffer();
    sellCounts.append(",(select sum(temp.sellCount) from (select ttt.* from " + childSql + " ttt group by ttt.billTypeId,ttt.billId) temp where temp.pids like concat(pr.pids,'%')) sellCounts");
    selectSql.append(sellCounts);
    

    StringBuffer sellAmount = new StringBuffer();
    sellAmount.append(",sum(temp.baseAmount) sellAmount");
    selectSql.append(sellAmount);
    
    StringBuffer sellDiscountMoney = new StringBuffer();
    sellDiscountMoney.append(",sum(temp.discountMoney) sellDiscountMoney");
    selectSql.append(sellDiscountMoney);
    
    StringBuffer sellTaxes = new StringBuffer();
    sellTaxes.append(",sum(temp.taxes) sellTaxes");
    selectSql.append(sellTaxes);
    
    StringBuffer sellTaxMoney = new StringBuffer();
    sellTaxMoney.append(",sum(temp.taxMoney) sellTaxMoney");
    selectSql.append(sellTaxMoney);
    
    StringBuffer sellGiftAmount = new StringBuffer();
    
    sellGiftAmount.append(",sum(case when (temp.basePrice=0 or temp.basePrice is null) then temp.baseAmount else 0 end) sellGiftAmount");
    selectSql.append(sellGiftAmount);
    
    StringBuffer sellGiftMoney = new StringBuffer();
    
    sellGiftMoney.append(",sum(case when (temp.basePrice=0 or temp.basePrice is null) then temp.baseAmount*temp.costPrice else 0 end) sellGiftMoney");
    selectSql.append(sellGiftMoney);
    
    StringBuffer sellGiftRetailPrice1 = new StringBuffer();
    sellGiftRetailPrice1.append(",sum(temp.retailMoney) sellGiftRetailPrice1");
    selectSql.append(sellGiftRetailPrice1);
    

    StringBuffer privilege = new StringBuffer();
    privilege.append(",(select sum(temp.privilege) from (select ttt.* from " + childSql + " ttt group by ttt.billTypeId,ttt.billId) temp where temp.pids like concat(pr.pids,'%')) privilege");
    selectSql.append(privilege);
    
    StringBuffer privilegeMoney = new StringBuffer();
    privilegeMoney.append(",(select sum(temp.privilegeMoney) from (select ttt.* from " + childSql + " ttt group by ttt.billTypeId,ttt.billId) temp where temp.pids like concat(pr.pids,'%')) privilegeMoney");
    selectSql.append(privilegeMoney);
    

    StringBuffer sellDiscountAvgPrice = new StringBuffer();
    sellDiscountAvgPrice.append(",(select case when sum(temp.baseAmount)!=0 then sum(temp.discountMoney)/sum(temp.baseAmount) else 0 end from (select ttt.* from " + childSql + " ttt group by ttt.billTypeId,ttt.billId) temp where temp.pids like concat(pr.pids,'%')) sellDiscountAvgPrice");
    selectSql.append(sellDiscountAvgPrice);
    
    StringBuffer sellTaxAvgPrice = new StringBuffer();
    sellTaxAvgPrice.append(",(select case when sum(temp.baseAmount)!=0 then sum(temp.taxMoney)/sum(temp.baseAmount) else 0 end from (select ttt.* from " + childSql + " ttt group by ttt.billTypeId,ttt.billId) temp where temp.pids like concat(pr.pids,'%')) sellTaxAvgPrice");
    selectSql.append(sellTaxAvgPrice);
    



    fromSql.append(" from b_unit pr ");
    fromSql.append(" left join " + childSql + " temp on temp.pids like concat(pr.pids,'%')");
    fromSql.append(" where 1=1");
    

    boolean flag = addReportBaseCondition(fromSql, "pr", searchBaseAttr, searchBaseVal);
    if (flag) {
      if (AioConstants.NODE_1 == node.intValue()) {
        fromSql.append(" and pr.pids like '%{" + unitSupId + "}%'");
      } else if (unitSupId.intValue() == 0) {
        fromSql.append(" and pr.supId=0");
      } else {
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
    String amount = "(select sum(temp.sellCount) from (select ttt.* from " + childSql + " ttt group by ttt.billTypeId,ttt.billId) temp where temp.pids like concat(pr.pids,'%'))";
    if (!aimDiv.equals("all")) {
      if (aimDiv.equals("eq")) {
        fromSql.append(" and IFNULL(" + amount + ",0) = 0 ");
      } else if (aimDiv.equals("gt")) {
        fromSql.append(" and IFNULL(" + amount + ",0) != 0 ");
      }
    }
    fromSql.append(" group by pr.id");
    fromSql.append(" order by " + orderField + " " + orderDirection);
    Map<String, Object> map = aioGetPageRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), "distinct pr.id", new Object[0]);
    return map;
  }
  
  public Map<String, Object> sellDetail(String configName, Map<String, Object> paraMap)
    throws SQLException, Exception
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    
    String modelType = paraMap.get("modelType").toString();
    String aimDiv = paraMap.get("aimDiv").toString();
    String productId = (String)paraMap.get("productId");
    String unitId = (String)paraMap.get("unitId");
    String staffId = (String)paraMap.get("staffId");
    String storageId = (String)paraMap.get("storageId");
    String departmentId = (String)paraMap.get("departmentId");
    String areaId = (String)paraMap.get("areaId");
    String[] orderTypes = (String[])paraMap.get("orderTypes");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    String departmentPrivs = String.valueOf(paraMap.get("departmentPrivs"));
    String areaPrivs = String.valueOf(paraMap.get("areaPrivs"));
    String accountPrivs = String.valueOf(paraMap.get("accountPrivs"));
    




    String billTable = "";
    String billDetailTable = "";
    
    StringBuffer selectSql = new StringBuffer("select temp.*,unit.*,storage.*,product.*,department.*,staff.*,area.*");
    StringBuffer fromSql = new StringBuffer(" from (");
    for (int i = 0; i < orderTypes.length; i++)
    {
      Map<String, String> map = new HashMap();
      String commBill = "";
      String commDetail = "";
      if (orderTypes[i].equals("xsd"))
      {
        billTable = "xs_sell_bill";
        billDetailTable = "xs_sell_detail";
        commBill = AioConstants.BILL_ROW_TYPE4 + " billTypeId,'销售单' billTypeName";
        commDetail = "2 type";
      }
      else if (orderTypes[i].equals("xsthd"))
      {
        billTable = "xs_return_bill";
        billDetailTable = "xs_return_detail";
        commBill = AioConstants.BILL_ROW_TYPE7 + " billTypeId,'销售退货单' billTypeName";
        commDetail = "1 type";
      }
      else if (orderTypes[i].equals("xshhd"))
      {
        billTable = "xs_barter_bill";
        billDetailTable = "xs_barter_detail";
        commBill = AioConstants.BILL_ROW_TYPE13 + " billTypeId,'销售换货单' billTypeName";
        commDetail = "detail.type";
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
      map.put("billTable", billTable);
      map.put("billDetailTable", billDetailTable);
      map.put("commBill", commBill);
      map.put("commDetail", commDetail);
      map.put("unitPrivs", unitPrivs);
      map.put("staffPrivs", staffPrivs);
      map.put("storagePrivs", storagePrivs);
      map.put("productPrivs", productPrivs);
      map.put("departmentPrivs", departmentPrivs);
      map.put("areaPrivs", areaPrivs);
      map.put("accountPrivs", accountPrivs);
      map.put("startDate", startDate);
      map.put("endDate", endDate);
      map.put("unitId", unitId);
      map.put("storageId", storageId);
      map.put("productId", productId);
      map.put("departmentId", departmentId);
      map.put("staffId", staffId);
      map.put("areaId", areaId);
      if ((modelType.equals("prd")) || (modelType.equals("unit")))
      {
        fromSql.append("select t.*");
        
        fromSql.append(",case when t.isRCW=2 then case when t.type=1 then t.baseAmount when t.type=2 then t.baseAmount*-1 else 0 end else case when t.type=2 then t.baseAmount else t.baseAmount*-1 end end sellOutAmount");
        
        fromSql.append(",case when t.isRCW=2 then case when t.type=1 then 0 when t.type=2 then t.baseAmount else 0 end else case when t.type=1 then t.baseAmount else 0 end end sellInAmount");
        
        fromSql.append(",case when t.isRCW=2 then case when t.type=1 then t.baseAmount*t.basePrice when t.type=2 then t.baseAmount*t.basePrice*-1 end else case when t.type=1 then t.baseAmount*t.basePrice*-1 when t.type=2 then t.baseAmount*t.basePrice end end sellMoney");
        fromSql.append(" from " + rankDetail(map) + " t where 1=1 ");
      }
      else if (modelType.equals("staff"))
      {
        fromSql.append("select t.*");
        
        fromSql.append(",case when t.isRCW=2 then case when t.type=1 then sum(t.baseAmount*t.basePrice) when t.type=2 then sum(t.baseAmount*t.basePrice*-1) end else case when t.type=1 then sum(t.baseAmount*t.basePrice*-1) when t.type=2 then sum(t.baseAmount*t.basePrice) end end sellMoney");
        fromSql.append(" from " + rankDetail(map) + " t where 1=1 group by t.billTypeId,t.id");
      }
      else if (modelType.equals("dept"))
      {
        fromSql.append("select t.*");
        
        fromSql.append(",case when t.isRCW=2 then case when t.type=1 then sum(t.baseAmount) when t.type=2 then sum(t.baseAmount*-1) else 0 end else case when t.type=2 then sum(t.baseAmount) else sum(t.baseAmount*-1) end end sellOutAmount");
        
        fromSql.append(",case when t.isRCW=2 then case when t.type=1 then sum(t.baseAmount*t.basePrice) when t.type=2 then sum(t.baseAmount*t.basePrice*-1) end else case when t.type=1 then sum(t.baseAmount*t.basePrice*-1) when t.type=2 then sum(t.baseAmount*t.basePrice) end end sellMoney");
        fromSql.append(" from " + rankDetail(map) + " t where 1=1 group by t.billTypeId,t.id");
      }
      else if (modelType.equals("storage"))
      {
        fromSql.append("select t.*");
        
        fromSql.append(",case when t.isRCW=2 then case when t.type=1 then sum(t.baseAmount) when t.type=2 then sum(t.baseAmount*-1) else 0 end else case when t.type=2 then sum(t.baseAmount) else sum(t.baseAmount*-1) end end sellOutAmount");
        
        fromSql.append(",case when t.isRCW=2 then case when t.type=1 then sum(t.baseAmount*t.basePrice) when t.type=2 then sum(t.baseAmount*t.basePrice*-1) end else case when t.type=1 then sum(t.baseAmount*t.basePrice*-1) when t.type=2 then sum(t.baseAmount*t.basePrice) end end sellMoney");
        fromSql.append(" from " + rankDetail(map) + " t where 1=1 group by t.billTypeId,t.id");
      }
      else if (modelType.equals("area"))
      {
        fromSql.append("select t.*");
        
        fromSql.append(",case when t.isRCW=2 then case when t.type=1 then sum(t.baseAmount) when t.type=2 then sum(t.baseAmount*-1) else 0 end else case when t.type=2 then sum(t.baseAmount) else sum(t.baseAmount*-1) end end sellOutAmount");
        
        fromSql.append(",case when t.isRCW=2 then case when t.type=1 then sum(t.baseAmount*t.basePrice) when t.type=2 then sum(t.baseAmount*t.basePrice*-1) end else case when t.type=1 then sum(t.baseAmount*t.basePrice*-1) when t.type=2 then sum(t.baseAmount*t.basePrice) end end sellMoney");
        fromSql.append(" from " + rankDetail(map) + " t where 1=1 group by t.billTypeId,t.id");
      }
      if ((orderTypes.length > 1) && (orderTypes.length - 1 != i)) {
        fromSql.append(" union all ");
      }
    }
    fromSql.append(") temp");
    fromSql.append(" left join b_unit unit on unit.id=temp.unitId");
    fromSql.append(" left join b_storage storage on storage.id=temp.dstorageId");
    fromSql.append(" left join b_product product on product.id=temp.productId");
    fromSql.append(" left join b_department department on department.id=temp.departmentId");
    fromSql.append(" left join b_staff staff on staff.id=temp.staffId");
    fromSql.append(" left join b_area area on area.id=temp.areaId where 1=1");
    if ((!aimDiv.equals("all")) && 
      (aimDiv.equals("notRedRow"))) {
      fromSql.append(" and temp.isRCW=" + AioConstants.RCW_NO);
    }
    fromSql.append(" order by " + orderField + " " + orderDirection);
    
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("unit", Unit.class);
    map.put("storage", Storage.class);
    map.put("product", Product.class);
    map.put("department", Department.class);
    map.put("staff", Staff.class);
    map.put("area", Area.class);
    Map<String, Object> dataMap = aioGetPageManyRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), map, new Object[0]);
    

    return dataMap;
  }
  
  public StringBuffer rankDetail(Map<String, String> map)
  {
    String billTable = String.valueOf(map.get("billTable"));
    String billDetailTable = String.valueOf(map.get("billDetailTable"));
    String commBill = String.valueOf(map.get("commBill"));
    String commDetail = String.valueOf(map.get("commDetail"));
    String unitPrivs = String.valueOf(map.get("unitPrivs"));
    String staffPrivs = String.valueOf(map.get("staffPrivs"));
    String storagePrivs = String.valueOf(map.get("storagePrivs"));
    String productPrivs = String.valueOf(map.get("productPrivs"));
    String departmentPrivs = String.valueOf(map.get("departmentPrivs"));
    String areaPrivs = String.valueOf(map.get("areaPrivs"));
    

    String startDate = String.valueOf(map.get("startDate"));
    String endDate = String.valueOf(map.get("endDate"));
    

    String unitId = String.valueOf(map.get("unitId"));
    String storageId = String.valueOf(map.get("storageId"));
    String productId = String.valueOf(map.get("productId"));
    String departmentId = String.valueOf(map.get("departmentId"));
    String staffId = String.valueOf(map.get("staffId"));
    String areaId = String.valueOf(map.get("areaId"));
    


    StringBuffer singleSql = new StringBuffer("(");
    
    StringBuffer aa = new StringBuffer();
    
    aa.append(" select " + commDetail + "," + commBill);
    
    aa.append(",bill.id,bill.code,bill.recodeDate,bill.remark,bill.isRCW,bill.unitId,bill.staffId,bill.departmentId,unit.areaId,area.pids areaPids");
    
    aa.append(",detail.storageId dstorageId,detail.productId,detail.discountMoney,detail.taxes,detail.taxMoney,detail.baseAmount,detail.basePrice,detail.costPrice");
    
    aa.append(",s.pids storagePids,p.pids productPids");
    aa.append(" from " + billDetailTable + " detail");
    aa.append(" left join " + billTable + " bill on bill.id = detail.billId");
    aa.append(" left join b_department department on department.id = bill.departmentId");
    aa.append(" left join b_staff staff on staff.id = bill.staffId");
    aa.append(" left join b_unit unit on unit.id = bill.unitId");
    aa.append(" left join b_area area on area.id = unit.areaId");
    aa.append(" left join b_storage s on s.id=detail.storageId");
    aa.append(" left join b_product p on p.id=detail.productId");
    
    aa.append(" where 1=1");
    aa.append(" and (bill.recodeDate between '" + startDate + "' and '" + endDate + "') ");
    
    singleSql.append(aa);
    if (!unitId.equals("0")) {
      singleSql.append(" and unit.pids like '%{" + unitId + "}%' ");
    }
    if (!storageId.equals("0")) {
      singleSql.append(" and s.pids like '%{" + storageId + "}%' ");
    }
    if (!productId.equals("0")) {
      singleSql.append(" and p.pids like '%{" + productId + "}%' ");
    }
    if (!departmentId.equals("0")) {
      singleSql.append(" and department.pids like '%{" + departmentId + "}%' ");
    }
    if (!staffId.equals("0")) {
      singleSql.append(" and staff.pids like '%{" + staffId + "}%' ");
    }
    if (!areaId.equals("0")) {
      singleSql.append(" and area.pids like '%{" + areaId + "}%' ");
    }
    ComFunController.queryProductPrivs(singleSql, productPrivs, "detail", "productId");
    ComFunController.queryUnitPrivs(singleSql, unitPrivs, "bill", "unitId");
    ComFunController.queryStoragePrivs(singleSql, storagePrivs, "detail", "storageId");
    ComFunController.queryStaffPrivs(singleSql, staffPrivs, "bill", "staffId");
    ComFunController.queryDepartmentPrivs(singleSql, departmentPrivs, "bill", "departmentId");
    ComFunController.queryAreaPrivs(singleSql, areaPrivs, "bill", "areaId");
    singleSql.append(")");
    return singleSql;
  }
  
  public static Map<String, Object> page(String listID, List dataList, int pageNum, int numPerPage, Map<String, Object> dataMap)
  {
    int totalCount = dataList.size();
    int totalPage = totalCount % numPerPage == 0 ? totalCount / numPerPage : totalCount / numPerPage + 1;
    if ((totalCount > 0) && (pageNum * numPerPage > totalCount)) {
      pageNum = totalPage;
    }
    List newList = new ArrayList();
    if ((pageNum == totalPage) && (totalPage > 0)) {
      newList.addAll(dataList.subList((pageNum - 1) * numPerPage, dataList.size()));
    } else if ((pageNum != totalPage) && (totalPage > 0)) {
      newList.addAll(dataList.subList((pageNum - 1) * numPerPage, pageNum * numPerPage));
    }
    dataMap.put("listID", listID);
    dataMap.put("pageList", newList);
    dataMap.put("totalCount", Integer.valueOf(totalCount));
    dataMap.put("totalPage", Integer.valueOf(totalPage));
    dataMap.put("pageNum", Integer.valueOf(pageNum));
    dataMap.put("numPerPage", Integer.valueOf(numPerPage));
    dataMap.put("limit", Integer.valueOf((pageNum - 1) * numPerPage));
    return dataMap;
  }
}
