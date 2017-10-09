package com.aioerp.controller.stock;

import com.aioerp.controller.BaseController;
import com.aioerp.model.stock.StockRecords;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class StockRecordsController
  extends BaseController
{
  public static void addRecordsDb(String configName, int billTypeId, String stockType, Record bill, Record detail, BigDecimal price, BigDecimal amount, Date time, Timestamp recodeTime, Integer storageId)
  {
    StockRecords records = new StockRecords();
    BigDecimal taxPrice = BigDecimal.ZERO;
    if (BigDecimalUtils.compare(detail.getBigDecimal("taxPrice"), BigDecimal.ZERO) != 0) {
      taxPrice = detail.getBigDecimal("taxPrice");
    } else if (BigDecimalUtils.compare(detail.getBigDecimal("discountPrice"), BigDecimal.ZERO) != 0) {
      taxPrice = detail.getBigDecimal("discountPrice");
    } else if (BigDecimalUtils.compare(detail.getBigDecimal("price"), BigDecimal.ZERO) != 0) {
      taxPrice = detail.getBigDecimal("price");
    }
    records.set("billId", detail.getInt("billId"));
    records.set("detailId", detail.getInt("Id"));
    records.set("productId", detail.getInt("productId"));
    records.set("storageId", storageId);
    records.set("batch", detail.getStr("batch"));
    records.set("produceDate", detail.getDate("produceDate"));
    records.set("produceEndDate", detail.getDate("produceEndDate"));
    records.set("selectUnitId", detail.getInt("selectUnitId"));
    records.set("price", price);
    records.set("taxPrice", taxPrice);
    records.set("taxMoney", BigDecimalUtils.mul(detail.getBigDecimal("amount"), taxPrice));
    records.set("discount", detail.getBigDecimal("discount"));
    records.set("taxRate", detail.getBigDecimal("taxRate"));
    if (stockType.equals("in")) {
      records.set("inAmount", amount);
    } else {
      records.set("outAmount", amount);
    }
    if (time != null) {
      records.set("createTime", time);
    }
    records.set("billTypeId", Integer.valueOf(billTypeId));
    records.set("recodeTime", recodeTime);
    records.set("billCode", bill.getStr("code"));
    records.set("billAbstract", bill.getStr("remark"));
    records.set("unitId", bill.getInt("unitId"));
    records.set("isRCW", bill.getInt("isRCW"));
    records.save(configName);
  }
  
  public static void addRecords(String configName, int billTypeId, String stockType, Model bill, Model detail, BigDecimal price, BigDecimal amount, Date time, Timestamp recodeTime, Integer storageId)
  {
    Record rBill = modelChangeDb(bill);
    Record rDetail = modelChangeDb(detail);
    addRecordsDb(configName, billTypeId, stockType, rBill, rDetail, price, amount, time, recodeTime, storageId);
  }
}
