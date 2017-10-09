package com.aioerp.common.patch;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.stock.StockRecordsController;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillType;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReloadBillRcw
{
  public static void reloadRcw(String configName)
  {
    Record reload = AioerpSys.dao.getObj(configName, "reloadBillRcw");
    if ((reload != null) && (reload.getStr("value1").equals("1"))) {
      return;
    }
    String str = "";
    

    Map<String, String> params = new HashMap();
    params.put("configName", configName);
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE4));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "in");
    



    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE5));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "in");
    str = commRcw(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE6));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "out");
    str = commRcw(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE12));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "inOrOut");
    str = commRcw(params);
    





    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE4));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "out");
    
    str = commRcw(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE7));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "in");
    
    str = commRcw(params);
    

    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE13));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "costPrice");
    params.put("inOrOut", "inOrOut");
    str = commRcw(params);
    





    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE8));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "out");
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE9));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "in");
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE10));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "in");
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE11));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "out");
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE14));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "inOrOut");
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE15));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "inOrOut");
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE16));
    params.put("isFinanceBill", "false");
    params.put("baseAmountAttr", "baseAmount");
    params.put("costPriceAttr", "basePrice");
    params.put("inOrOut", "inOrOut");
    str = commRcw(params);
    





    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE17));
    params.put("isFinanceBill", "true");
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE18));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE19));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE20));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE21));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE22));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE23));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE24));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE25));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE26));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE27));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE28));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE29));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE30));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE31));
    
    str = commRcw(params);
    
    params.put("billTypeId", String.valueOf(AioConstants.BILL_ROW_TYPE32));
    
    str = commRcw(params);
    


    AioerpSys.dao.sysSaveOrUpdate(configName, "reloadBillRcw", "1", "重新生成红冲单据     0未   1生成过【" + str + "】");
  }
  
  public static String commRcw(Map<String, String> params)
  {
    String configName = String.valueOf(params.get("configName"));
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    if (codeAllowRep.equals("0")) {
      return "单据编号可重复需要手动修改红冲ID";
    }
    boolean isFinanceBill = Boolean.valueOf(String.valueOf(params.get("isFinanceBill"))).booleanValue();
    int billTypeId = Integer.valueOf(String.valueOf(params.get("billTypeId"))).intValue();
    String baseAmountAttr = String.valueOf(params.get("baseAmountAttr"));
    String costPriceAttr = String.valueOf(params.get("costPriceAttr"));
    String inOrOut = String.valueOf(params.get("inOrOut"));
    String stockType = "out";
    
    String billTableName = BillType.getValue1(configName, billTypeId, "biillTableName");
    String detailTableName = BillType.getValue1(configName, billTypeId, "billDetailTableName");
    if (billTableName.equals("cg_purchase_bill")) {
      System.out.println("aa");
    }
    List<Record> oldBillList = Db.use(configName).find("select * from " + billTableName + " where isRCW=" + AioConstants.RCW_BY + " order by updateTime asc");
    
    List<Record> newBillList = Db.use(configName).find("select * from " + billTableName + " where isRCW=" + AioConstants.RCW_VS + " order by updateTime asc");
    
    Map<String, Integer> map = new HashMap();
    if ((newBillList != null) && (newBillList.size() > 0)) {
      for (int i = 0; i < newBillList.size(); i++)
      {
        map.put(((Record)newBillList.get(i)).getStr("code"), ((Record)newBillList.get(i)).getInt("id"));
        
        Db.use(configName).update("delete from " + detailTableName + " where billId=" + ((Record)newBillList.get(i)).getInt("id"));
        if (!isFinanceBill) {
          Db.use(configName).update("delete from cc_stock_records where billTypeId = " + billTypeId + " and billId=" + ((Record)newBillList.get(i)).getInt("id") + " and isRCW=" + AioConstants.RCW_VS);
        }
      }
    }
    if ((oldBillList != null) && (oldBillList.size() > 0)) {
      for (int i = 0; i < oldBillList.size(); i++)
      {
        Record bill = (Record)oldBillList.get(i);
        List<Record> detailList = Db.use(configName).find("select * from " + detailTableName + " where billId = " + bill.getInt("id"));
        for (int j = 0; j < detailList.size(); j++)
        {
          Record detail = (Record)detailList.get(j);
          detail.set("rcwId", detail.getInt("id"));
          detail.set("id", null);
          detail.set("billId", map.get(bill.getStr("code")));
          Db.use(configName).save(detailTableName, detail);
          if (!isFinanceBill)
          {
            BigDecimal baseAmount = detail.getBigDecimal(baseAmountAttr);
            BigDecimal costPrice = detail.getBigDecimal(costPriceAttr);
            if (inOrOut.equals("inOrOut")) {
              if ((billTypeId == AioConstants.BILL_ROW_TYPE12) || (billTypeId == AioConstants.BILL_ROW_TYPE13) || (billTypeId == AioConstants.BILL_ROW_TYPE16))
              {
                if (detail.getInt("type").intValue() == 1) {
                  stockType = "out";
                } else if (detail.getInt("type").intValue() == 2) {
                  stockType = "in";
                }
                StockRecordsController.addRecordsDb(configName, billTypeId, stockType, bill, detail, costPrice, baseAmount, bill.getDate("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
              }
              else if ((billTypeId == AioConstants.BILL_ROW_TYPE14) || (billTypeId == AioConstants.BILL_ROW_TYPE15))
              {
                StockRecordsController.addRecordsDb(configName, billTypeId, "out", bill, detail, costPrice, baseAmount, bill.getDate("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("inStorageId"));
                StockRecordsController.addRecordsDb(configName, billTypeId, "in", bill, detail, costPrice, baseAmount, bill.getDate("updateTime"), bill.getTimestamp("recodeDate"), detail.getInt("outStorageId"));
              }
            }
          }
        }
      }
    }
    return "";
  }
}
