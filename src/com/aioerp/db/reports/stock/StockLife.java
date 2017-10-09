package com.aioerp.db.reports.stock;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Avgprice;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.reports.sell.SellReports;
import com.aioerp.model.stock.Stock;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class StockLife
  extends BaseDbModel
{
  public static final StockLife dao = new StockLife();
  Map<String, Class<? extends Model>> MAP = new HashMap();
  private StringBuffer SELECTSQL;
  private StringBuffer FROMSQL;
  
  public void init(int storageId, int alertDay)
  {
    this.MAP.put("sck", Stock.class);
    this.MAP.put("pro", Product.class);
    this.MAP.put("sge", Storage.class);
    this.MAP.put("zj", Avgprice.class);
    

    this.SELECTSQL = new StringBuffer("select DATEDIFF(sck.produceEndDate,sck.produceDate) as expire,sck.*,pro.*,sge.*,zj.*");
    
    this.FROMSQL = new StringBuffer(" from cc_stock sck LEFT JOIN b_product pro on sck.productId=pro.id LEFT JOIN b_storage sge on sge.id=sck.storageId LEFT JOIN zj_product_avgprice zj on zj.productId=pro.id where 1=1");
  }
  
  public Map<String, Object> getListByParam(String configName, int storageId, Integer alertDay, String listID, int pageNum, int numPerPage, String orderField, String orderDirection, String searchBaseAttr, String searchBaseVal)
    throws SQLException, Exception
  {
    init(storageId, alertDay.intValue());
    
    String storageSql = " and zj.storageId is null";
    if (storageId != 0)
    {
      storageSql = " and zj.storageId = " + storageId;
      this.FROMSQL.append(" and sck.storageId =" + storageId);
    }
    this.FROMSQL.append(storageSql);
    SellReports.addReportBaseCondition(this.FROMSQL, "pro", searchBaseAttr, searchBaseVal);
    
    this.FROMSQL.append(" and sck.produceEndDate < ADDDATE(curdate()," + alertDay + ")");
    this.FROMSQL.append(" and pro.costArith != 1");
    this.FROMSQL.append(" GROUP BY sck.productId,sck.storageId,sck.produceDate,sck.produceEndDate,sck.batchNum");
    this.FROMSQL.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, this.SELECTSQL.toString(), this.FROMSQL.toString(), this.MAP, new Object[0]);
  }
}
