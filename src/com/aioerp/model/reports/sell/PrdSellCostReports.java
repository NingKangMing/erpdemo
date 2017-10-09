package com.aioerp.model.reports.sell;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrdSellCostReports
  extends BaseDbModel
{
  public static final PrdSellCostReports dao = new PrdSellCostReports();
  
  public Map<String, Object> reportPrdSellCostCount(String configName, Map<String, Object> paraMap)
    throws SQLException, Exception
  {
    String listID = (String)paraMap.get("listID");
    Integer pageNum = (Integer)paraMap.get("pageNum");
    Integer numPerPage = (Integer)paraMap.get("numPerPage");
    Integer unitId = (Integer)paraMap.get("unitId");
    Integer productId = (Integer)paraMap.get("productId");
    Integer staffId = (Integer)paraMap.get("staffId");
    Integer storageId = (Integer)paraMap.get("storageId");
    String startDate = paraMap.get("startDate").toString();
    String endDate = StringUtil.dataStrToStr(paraMap.get("endDate").toString());
    String orderType = (String)paraMap.get("orderType");
    String isRCW = (String)paraMap.get("isRCW");
    String orderField = paraMap.get("orderField").toString();
    String orderDirection = paraMap.get("orderDirection").toString();
    
    String unitPrivs = String.valueOf(paraMap.get("unitPrivs"));
    String staffPrivs = String.valueOf(paraMap.get("staffPrivs"));
    String storagePrivs = String.valueOf(paraMap.get("storagePrivs"));
    String productPrivs = String.valueOf(paraMap.get("productPrivs"));
    

    String[] orderTypes = null;
    if (orderType.equals("all")) {
      orderTypes = new String[] { "xsd", "xsthd", "xshhd" };
    } else if (orderType.equals("xsd")) {
      orderTypes = new String[] { "xsd" };
    } else if (orderType.equals("xsthd")) {
      orderTypes = new String[] { "xsthd" };
    } else if (orderType.equals("xshhd")) {
      orderTypes = new String[] { "xshhd" };
    } else if (orderType.equals("jzxsd")) {
      orderTypes = new String[] { "jzxsd" };
    } else if (orderType.equals("wtjsd")) {
      orderTypes = new String[] { "wtjsd" };
    }
    StringBuffer childSql = SellReports.proCommSql(unitPrivs, staffPrivs, storagePrivs, productPrivs, orderTypes, unitId.intValue(), staffId.intValue(), storageId.intValue(), productId.intValue(), 0, startDate, endDate, null, null, false);
    if (childSql.equals("")) {
      return null;
    }
    StringBuffer selectSql = new StringBuffer("select t.*,unit.*,staff.*,storage.*,department.*");
    StringBuffer fromSql = new StringBuffer();
    fromSql.append(" from (");
    fromSql.append(" select temp.*");
    
    fromSql.append(",sum(temp.discountMoney) sellTaxMoney");
    
    fromSql.append(",sum(temp.baseAmount*temp.costPrice) sellCostMoney");
    
    fromSql.append(",(sum(temp.discountMoney)-sum(temp.baseAmount*temp.costPrice)) sellWinMoney");
    fromSql.append(" from " + childSql + "temp group by temp.billTypeId,temp.billId,temp.type ");
    fromSql.append(") t");
    fromSql.append(" left join b_unit unit on unit.id=t.unitId");
    fromSql.append(" left join b_department department on department.id=t.departmentId");
    fromSql.append(" left join b_staff staff on staff.id=t.staffId");
    fromSql.append(" left join b_storage storage on storage.id=t.bstroageId");
    fromSql.append(" where 1=1");
    

    Map<String, Class<? extends Model>> aliasMap = new HashMap();
    aliasMap.put("unit", Unit.class);
    aliasMap.put("staff", Staff.class);
    aliasMap.put("storage", Storage.class);
    aliasMap.put("department", Department.class);
    if (isRCW.equals("notRedRow")) {
      fromSql.append(" and t.isRCW=" + AioConstants.RCW_NO);
    }
    fromSql.append(" order by " + orderField + " " + orderDirection);
    Map<String, Object> map = aioGetPageManyRecord(configName, listID, pageNum.intValue(), numPerPage.intValue(), selectSql.toString(), fromSql.toString(), aliasMap, new Object[0]);
    return map;
  }
  
  public Map<String, Object> prdsellCostDetail(String configName, String orderType, int billId)
  {
    Map<String, Object> map = new HashMap();
    if ((orderType == null) || (orderType.equals(""))) {
      return null;
    }
    String billTypeSql = "";
    List<Record> list = null;
    String billTable = "";
    String detailTable = "";
    if (orderType.equals("xsd"))
    {
      billTable = "xs_sell_bill";
      detailTable = "xs_sell_detail";
      billTypeSql = ",2 type";
    }
    else if (orderType.equals("xsthd"))
    {
      billTable = "xs_return_bill";
      detailTable = "xs_return_detail";
      billTypeSql = ",1 type";
    }
    else if (orderType.equals("xshhd"))
    {
      billTable = "xs_barter_bill";
      detailTable = "xs_barter_detail";
      billTypeSql = ",detail.type";
    }
    else if (!orderType.equals("jzxsd"))
    {
      orderType.equals("wtjsd");
    }
    StringBuffer sql = new StringBuffer("select bill.*,detail.*,storage.code storageCode,storage.fullName storageFullName,product.code productCode,product.fullName productFullName" + billTypeSql + " from " + detailTable + " detail ");
    sql.append("left join ( ");
    sql.append("select b.isRCW,b.id billId,b.recodeDate,unit.code unitCode,unit.fullName unitFullName,staff.code staffCode,staff.fullName staffFullName ");
    sql.append("from " + billTable + " b ");
    sql.append("left join b_unit unit on unit.id=b.unitId ");
    sql.append("left join b_staff staff on staff.id=b.staffId ");
    sql.append(") bill on bill.billId=detail.billId ");
    sql.append("left join b_storage storage on storage.id=detail.storageId ");
    sql.append("left join b_product product on product.id=detail.productId ");
    sql.append("where bill.billId=" + billId + " ");
    
    list = Db.use(configName).find(sql.toString());
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record r = (Record)list.get(i);
        BigDecimal baseAmount = BigDecimal.ZERO;
        int isRCW = ((Integer)r.get("isRCW")).intValue();
        int type = Integer.valueOf(String.valueOf(r.get("type"))).intValue();
        baseAmount = r.getBigDecimal("baseAmount");
        if (isRCW == AioConstants.RCW_VS)
        {
          if (type == 2) {
            baseAmount = BigDecimalUtils.negate(baseAmount);
          }
        }
        else if (type == 1) {
          baseAmount = BigDecimalUtils.negate(baseAmount);
        }
        BigDecimal costMoneys = r.getBigDecimal("costMoneys");
        ((Record)list.get(i)).set("sellMoney", r.getBigDecimal("discountMoney"));
        ((Record)list.get(i)).set("costPrice", BigDecimalUtils.div(costMoneys, baseAmount));
        ((Record)list.get(i)).set("sellWinMoney", BigDecimalUtils.sub(((Record)list.get(i)).getBigDecimal("sellMoney"), costMoneys));
      }
    }
    map.put("pageList", list);
    return map;
  }
}
