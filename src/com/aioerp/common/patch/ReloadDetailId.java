package com.aioerp.common.patch;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.stock.StockRecordsController;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillType;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ReloadDetailId
{
  public static void reloadDetailId(String configName)
  {
    Record reload = AioerpSys.dao.getObj(configName, "reloadDetailId");
    if ((reload != null) && (reload.getStr("value1").equals("1"))) {
      return;
    }
    Db.use(configName).update("TRUNCATE table cc_stock_records");
    
    Map<String, String> params = new HashMap();
    params.put("configName", configName);
    



    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE5));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "in");
    commonReload(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE6));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "out");
    commonReload(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE12));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "inOrOut");
    commonReload(params);
    


    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE4));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "out");
    commonReload(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE7));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "in");
    commonReload(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE13));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "inOrOut");
    commonReload(params);
    


    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE8));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "out");
    commonReload(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE9));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "in");
    commonReload(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE10));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "in");
    commonReload(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE11));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "out");
    commonReload(params);
    


    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE14));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "inOrOut");
    commonReload(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE15));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "inOrOut");
    commonReload(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE16));
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "inOrOut");
    commonReload(params);
    
    AioerpSys.dao.sysSaveOrUpdate(configName, "reloadDetailId", "1", "重新生成商品库存记录     0未   1生成过");
  }
  
  private static void commonReload(Map<String, String> params)
  {
    String configName = String.valueOf(params.get("configName"));
    int billTypeId = Integer.valueOf(String.valueOf(params.get("billTypeId"))).intValue();
    String baseAmountAttr = String.valueOf(params.get("baseAmountAttr"));
    String costPriceAttr = String.valueOf(params.get("costPriceAttr"));
    String inOrOut = String.valueOf(params.get("inOrOut"));
    
    String billTableName = BillType.getValue1(configName, billTypeId, "biillTableName");
    String detailTableName = BillType.getValue1(configName, billTypeId, "billDetailTableName");
    
    List<Record> billList = Db.use(configName).find("select * from " + billTableName);
    Iterator localIterator2;
    for (Iterator localIterator1 = billList.iterator(); localIterator1.hasNext(); localIterator2.hasNext())
    {
      Record bill = (Record)localIterator1.next();
      List<Record> detailBillList = Db.use(configName).find("select * from " + detailTableName + " where billId = " + bill.getInt("id"));
      localIterator2 = detailBillList.iterator(); 
     // continue;
      Record detailBill = (Record)localIterator2.next();
      StockRecordsController.addRecordsDb(configName, billTypeId, inOrOut, bill, detailBill, detailBill.getBigDecimal(costPriceAttr), detailBill.getBigDecimal(baseAmountAttr), bill.getTimestamp("updateTime"), bill.getTimestamp("recodeDate"), detailBill.getInt("storageId"));
    }
  }
}
