package com.aioerp.db.reports.stock;

import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.stock.StockRecords;
import com.aioerp.model.sys.BillType;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class StockBatchTail
  extends BaseDbModel
{
  public static final StockBatchTail dao = new StockBatchTail();
  Map<String, Class<? extends Model>> MAP = new HashMap();
  private StringBuffer SELECTSQL;
  private StringBuffer FROMSQL;
  
  public void init()
  {
    this.MAP.put("sckRos", StockRecords.class);
    this.MAP.put("btype", BillType.class);
    this.MAP.put("pro", Product.class);
    this.MAP.put("sge", Storage.class);
    this.MAP.put("bunit", Unit.class);
    
    this.SELECTSQL = new StringBuffer("select sckRos.*,btype.*,pro.*,sge.*,bunit.*,");
    
    String unitName = " (case sckRos.selectUnitId when 1 then pro.calculateUnit1 when 2  then pro.calculateUnit2 when 3 then pro.calculateUnit3 end )";
    
    String amount = " (IFNULL(sckRos.outAmount,IFNULL(sckRos.inAmount,0)))";
    
    String costMoney = " ((IFNULL(sckRos.outAmount,IFNULL(sckRos.inAmount,0))) * sckRos.price)";
    this.SELECTSQL.append(unitName + " as unitName,");
    this.SELECTSQL.append(amount + " as amount,");
    this.SELECTSQL.append(costMoney + " as costMoney");
    
    this.FROMSQL = new StringBuffer(" FROM cc_stock_records sckRos");
    this.FROMSQL.append(" LEFT JOIN sys_billtype btype ON btype.id=sckRos.billTypeId");
    this.FROMSQL.append(" LEFT JOIN b_product pro ON pro.id=sckRos.productId");
    this.FROMSQL.append(" LEFT JOIN b_storage sge ON sge.id=sckRos.storageId");
    this.FROMSQL.append(" LEFT JOIN b_unit bunit ON bunit.id=sckRos.unitId");
    this.FROMSQL.append(" where 1 = 1");
  }
  
  public Map<String, Object> getListByParam(String configName, String storagePrivs, String unitPrivs, int productId, String batchNum, String startTime, String endTime, String listID, int pageNum, int numPerPage, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    init();
    
    this.FROMSQL.append(" and sckRos.productId = " + productId);
    
    ComFunController.queryStoragePrivs(this.FROMSQL, storagePrivs, "sckRos");
    ComFunController.queryUnitPrivs(this.FROMSQL, unitPrivs, "sckRos");
    if (!"".equals(batchNum)) {
      this.FROMSQL.append(" and sckRos.batch LIKE '%" + batchNum + "%'");
    }
    this.FROMSQL.append(" and sckRos.recodeTime >= '" + startTime + "'");
    this.FROMSQL.append(" and sckRos.recodeTime <= '" + endTime + " 23:59:59'");
    
    this.FROMSQL.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, this.SELECTSQL.toString(), this.FROMSQL.toString(), this.MAP, new Object[0]);
  }
}
