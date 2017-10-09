package com.aioerp.model.stock;

import com.aioerp.model.BaseDbModel;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class StockDraftRecords
  extends BaseDbModel
{
  public static final StockDraftRecords dao = new StockDraftRecords();
  public static final String TABLE_NAME = "cc_stock_records_draft";
  
  public static void addRecords(String configName, int type, boolean isIn, Model bill, Model detail, BigDecimal price, BigDecimal amount, Date time, Timestamp recodeTime, Integer storageId)
  {
    StockDraftRecords records = new StockDraftRecords();
    
    BigDecimal taxPrice = BigDecimal.ZERO;
    if (BigDecimalUtils.compare(detail.getBigDecimal("taxPrice"), BigDecimal.ZERO) != 0) {
      taxPrice = detail.getBigDecimal("taxPrice");
    } else if (BigDecimalUtils.compare(detail.getBigDecimal("discountPrice"), BigDecimal.ZERO) != 0) {
      taxPrice = detail.getBigDecimal("discountPrice");
    } else if (BigDecimalUtils.compare(detail.getBigDecimal("price"), BigDecimal.ZERO) != 0) {
      taxPrice = detail.getBigDecimal("price");
    }
    records.set("createTime", time);
    records.set("billTypeId", Integer.valueOf(type));
    records.set("billId", detail.getInt("billId"));
    records.set("billCode", bill.getStr("code"));
    records.set("recodeTime", recodeTime);
    records.set("billAbstract", bill.getStr("remark"));
    records.set("productId", detail.getInt("productId"));
    records.set("unitId", bill.getInt("unitId"));
    records.set("storageId", storageId);
    records.set("batch", detail.getStr("batch"));
    records.set("produceDate", detail.getDate("produceDate"));
    records.set("produceEndDate", detail.getDate("produceEndDate"));
    records.set("price", price);
    records.set("taxPrice", taxPrice);
    records.set("taxMoney", BigDecimalUtils.mul(detail.getBigDecimal("amount"), taxPrice));
    records.set("discount", detail.getBigDecimal("discount"));
    records.set("taxRate", detail.getBigDecimal("taxRate"));
    records.set("selectUnitId", detail.getInt("selectUnitId"));
    if (isIn) {
      records.set("inAmount", amount);
    } else {
      records.set("outAmount", amount);
    }
    records.save(configName);
  }
  
  public static void delRecords(String configName, int billType, int draftId)
  {
    String sql = "DELETE FROM cc_stock_records_draft WHERE billTypeId = ? AND billId = ?";
    Db.use(configName).update(sql, new Object[] { Integer.valueOf(billType), Integer.valueOf(draftId) });
  }
  
  public List<Record> getRecordByBillIdAndType(String configName, int billType, int draftId)
  {
    String sql = "SELECT * FROM cc_stock_records_draft WHERE billTypeId = ? AND billId = ?";
    return Db.use(configName).find(sql, new Object[] { Integer.valueOf(billType), Integer.valueOf(draftId) });
  }
}
