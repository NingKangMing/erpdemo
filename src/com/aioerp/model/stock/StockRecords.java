package com.aioerp.model.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.sys.BillType;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class StockRecords
  extends BaseDbModel
{
  public static final StockRecords dao = new StockRecords();
  public static final String TABLE_NAME = "cc_stock_records";
  Map<String, Class<? extends Model>> MAP = new HashMap();
  
  public Map<String, Object> getPageByProIdAndSgeId(String configName, String storagePrivs, String proPids, String sgePids, String startTime, String endTime, Integer isRcw, String listID, int pageNum, int numPerPage, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    this.MAP.put("sckRos", StockRecords.class);
    this.MAP.put("bt", BillType.class);
    this.MAP.put("pro", Product.class);
    StringBuffer selectSql = new StringBuffer("SELECT sckRos.*,bt.*,pro.*");
    StringBuffer fromsql = new StringBuffer(" FROM cc_stock_records as sckRos LEFT JOIN sys_billtype bt ON bt.id=sckRos.billTypeId LEFT JOIN b_product pro ON pro.id=sckRos.productId");
    fromsql.append(" where 1=1 ");
    fromsql.append(" and pro.pids LIKE CONCAT('" + proPids + "','%') ");
    fromsql.append(" AND sckRos.createTime >= '" + startTime + "' AND sckRos.createTime <= '" + endTime + " 23:59:59' ");
    if ((!"".equals(sgePids)) && (sgePids != null)) {
      if (StringUtils.isBlank(storagePrivs)) {
        fromsql.append(" AND sckRos.storageId = 0 ");
      } else if ((StringUtils.isNotBlank(storagePrivs)) && (!"*".equals(storagePrivs))) {
        fromsql.append(" AND sckRos.storageId IN (select id from b_storage where pids LIKE CONCAT('" + sgePids + "','%') AND id IN(" + storagePrivs + ") ) ");
      } else if ("*".equals(storagePrivs)) {
        fromsql.append(" AND sckRos.storageId IN (select id from b_storage where pids LIKE CONCAT('" + sgePids + "','%')) ");
      }
    }
    if (-1 != isRcw.intValue()) {
      fromsql.append(" AND sckRos.isRCW =" + isRcw);
    }
    fromsql.append(" ORDER BY " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, selectSql.toString(), fromsql.toString(), this.MAP, new Object[0]);
  }
  
  public StockRecords getUpFirstStockRecords222(String configName, Date recodeTime, Integer productId, Integer storageId)
  {
    StringBuffer sql = new StringBuffer("select * from cc_stock_records where 1=1");
    List<Object> paras = new ArrayList();
    if (recodeTime != null)
    {
      sql.append(" and recodeTime <= ?");
      paras.add(recodeTime);
    }
    if (storageId.intValue() != 0)
    {
      sql.append(" and storageId = ?");
      paras.add(storageId);
    }
    if (productId.intValue() != 0)
    {
      sql.append(" and productId = ?");
      paras.add(productId);
    }
    sql.append(" order by id desc");
    return (StockRecords)dao.findFirst(configName, sql.toString(), paras.toArray());
  }
  
  public StockRecords getBeforeFirst(String configName, String storagePrivs, String productPrivs, String startTime, String endTime, int recordCount, String storagePids, String productPids)
  {
    StringBuffer sql = new StringBuffer("SELECT");
    sql.append("(IFNULL(SUM(inAmount),0)-(IFNULL(SUM(outAmount),0))) beforeAmout,");
    sql.append("(IFNULL(SUM(inMoney),0)-(IFNULL(SUM(outMoney),0))) beforeMoney");
    sql.append(" FROM");
    sql.append(" (");
    sql.append("SELECT sr.inAmount,sr.outAmount,(sr.inAmount*sr.price) inMoney,(sr.outAmount*sr.price) outMoney");
    sql.append(" FROM cc_stock_records sr ");
    sql.append(" LEFT JOIN b_storage s ON s.id=sr.storageId");
    sql.append(" LEFT JOIN b_product p ON p.id=sr.productId");
    sql.append(" WHERE 1=1 AND s.pids LIKE CONCAT('" + storagePids + "','%')");
    sql.append(" AND p.pids LIKE CONCAT('" + productPids + "','%') ");
    sql.append(" AND sr.createTime >= '" + startTime + "' AND sr.createTime <= '" + endTime + " 23:59:59'");
    if (StringUtils.isBlank(storagePrivs)) {
      sql.append(" AND sr.storageId = 0 ");
    } else if ((StringUtils.isNotBlank(storagePrivs)) && (!"*".equals(storagePrivs))) {
      sql.append(" AND sr.storageId IN (select id from b_storage where pids LIKE CONCAT('" + storagePids + "','%') AND id IN(" + storagePrivs + ") ) ");
    } else if ("*".equals(storagePrivs)) {
      sql.append(" AND sr.storageId IN (select id from b_storage where pids LIKE CONCAT('" + storagePids + "','%')) ");
    }
    sql.append(" ORDER BY sr.createTime,sr.id");
    sql.append(" LIMIT 0," + recordCount);
    sql.append(" ) tmp");
    
    StockRecords records = (StockRecords)dao.findFirst(configName, sql.toString());
    BigDecimal beforeAmout = records.getBigDecimal("beforeAmout");
    BigDecimal beforeMoney = records.getBigDecimal("beforeMoney");
    

    Record record = StockInit.dao.getStockInit(configName, storagePrivs, productPrivs, storagePids, productPids);
    BigDecimal initAmouts = record.getBigDecimal("amounts");
    BigDecimal initMoneys = record.getBigDecimal("moneys");
    
    records.put("beforeAmout", BigDecimalUtils.add(initAmouts, beforeAmout));
    records.put("beforeMoney", BigDecimalUtils.add(initMoneys, beforeMoney));
    return records;
  }
  
  public static StringBuffer preStockProductAmount(String storagePrivs, String productPrivs, String recodeTime, String storagePids, String productPids, String type)
  {
    if ((type == null) || (type.equals(""))) {
      type = "all";
    }
    StringBuffer sql = new StringBuffer("(select");
    if (type.equals("all"))
    {
      sql.append(" *,");
      sql.append("(IFNULL(SUM(inAmount),0)-(IFNULL(SUM(outAmount),0))) beforeAmout,");
      sql.append("((IFNULL((SUM(inAmount*price)),0))-(IFNULL((SUM(outAmount*price)),0))) beforeMoney ");
    }
    else if (type.equals("amount"))
    {
      sql.append("(IFNULL(SUM(inAmount),0)-(IFNULL(SUM(outAmount),0))) beforeAmout ");
    }
    else if (type.equals("money"))
    {
      sql.append("((IFNULL((SUM(inAmount*price)),0))-(IFNULL((SUM(outAmount*price)),0))) beforeMoney ");
    }
    sql.append(" from cc_stock_records sr");
    sql.append(" left join b_storage s on s.id=sr.storageId");
    sql.append(" left join b_product p on p.id=sr.productId");
    sql.append(" where 1=1");
    if (recodeTime != null) {
      sql.append(" and sr.recodeTime < '" + recodeTime + "'");
    }
    if ((storagePids != null) && (!"".equals(storagePids))) {
      if (storagePids.startsWith("{")) {
        sql.append(" AND s.pids LIKE CONCAT('" + storagePids + "','%') ");
      } else {
        sql.append(" AND s.pids LIKE CONCAT(" + storagePids + ",'%') ");
      }
    }
    if ((productPids != null) && (!"".equals(productPids))) {
      if (productPids.startsWith("{")) {
        sql.append(" AND p.pids LIKE CONCAT('" + productPids + "','%') ");
      } else {
        sql.append(" AND p.pids LIKE CONCAT(" + productPids + ",'%') ");
      }
    }
    ComFunController.queryProductPrivs(sql, productPrivs, "sr");
    ComFunController.queryStoragePrivs(sql, storagePrivs, "sr");
    
    sql.append(")");
    return sql;
  }
  
  public static Integer batchUpdateIsRCW(String configName, Integer typeId, Integer billId)
  {
    String sql = "UPDATE cc_stock_records set isRCW = " + AioConstants.RCW_BY + " WHERE billTypeId=" + typeId + " AND billId=" + billId;
    Integer row = Integer.valueOf(Db.use(configName).update(sql));
    return row;
  }
  
  public List<Record> getRecordByBillIdAndType(String configName, Integer typeId, Integer billId)
  {
    String sql = "SELECT * FROM cc_stock_records WHERE billTypeId = ? AND billId = ?";
    return Db.use(configName).find(sql, new Object[] { typeId, billId });
  }
  
  public Model getInsertBillStockObj(String configName, Date insertBillDate, Model model, BigDecimal costPrice, int costArith)
    throws ParseException
  {
    StringBuffer sql = new StringBuffer("select sr.storageId,sr.productId");
    if (costArith != 1) {
      sql.append(" ,sr.batch,sr.produceDate,sr.produceEndDate");
    }
    sql.append(" ,sum(ifnull(init.amount,0) + ifnull(sr.inAmount,0))-SUM(ifnull(sr.outAmount,0)) as totalAmount");
    sql.append(" ,sum(init.amount)*IFNULL(init.price,0) + sum(sr.inAmount)*IFNULL(sr.price,0)- sum(sr.outAmount)*IFNULL(sr.price,0) as totalMoney");
    sql.append(" ,(sum(init.amount)*IFNULL(init.price,0) + sum(sr.inAmount)*IFNULL(sr.price,0)- sum(sr.outAmount)*IFNULL(sr.price,0))/(sum(ifnull(init.amount,0) + ifnull(sr.inAmount,0))-SUM(ifnull(sr.outAmount,0))) as costPrice");
    sql.append(" from cc_stock_records sr LEFT JOIN b_product pro ON sr.productId = pro.id");
    sql.append(" left join cc_stock_init init on (init.storageId=sr.storageId and init.productId=sr.productId");
    if (costArith == 4) {
      sql.append(" and init.price=sr.price and init.batch=sr.batch and init.produceDate=sr.produceDate and init.produceEndDate=sr.produceEndDate");
    }
    sql.append(")");
    sql.append(" where sr.createTime < '" + DateUtils.format(insertBillDate, "yyyy-MM-dd HH:mm:ss") + "' and sr.storageId = " + model.getInt("storageId"));
    sql.append(" and sr.productId = " + model.getInt("productId") + " and sr.isRCW = " + AioConstants.RCW_NO);
    if (costArith == 1)
    {
      sql.append(" and pro.costArith = 1");
      sql.append(" GROUP BY sr.storageId,sr.productId");
    }
    else
    {
      sql.append(" and pro.costArith = 4 and sr.price =" + costPrice + " and sr.batch='" + model.getStr("batch") + "' and sr.produceDate='" + DateUtils.format(model.getDate("produceDate"), "yyyy-MM-dd HH:mm:ss") + "' and sr.produceEndDate='" + DateUtils.format(model.getDate("produceEndDate"), "yyyy-MM-dd HH:mm:ss") + "'");
      sql.append(" GROUP BY sr.storageId,sr.productId,sr.batch,sr.produceDate,sr.produceEndDate");
    }
    return dao.findFirst(configName, sql.toString());
  }
}
