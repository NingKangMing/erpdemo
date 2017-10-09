package com.aioerp.db.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.sys.AioerpSys;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class StockReport
  extends BaseDbModel
{
  public static final StockReport dao = new StockReport();
  Map<String, Class<? extends Model>> MAP = new HashMap();
  private StringBuffer SELECTSQL;
  private StringBuffer FROMSQL;
  String amounts;
  StringBuffer ordeAmount;
  String draftAmount;
  
  public void virtualStockInit(String storagePids, String storagePrivs, String configName)
  {
    this.MAP.put("pro", Product.class);
    this.MAP.put("sck", Stock.class);
    
    this.SELECTSQL = new StringBuffer("select ");
    

    String avgPrice = "";
    String moneys = "";
    String sellOrde = "";
    String boughtOrde = "";
    
    String dtaftStoragePids = "";
    if ((StringUtils.isNotBlank(storagePrivs)) && (!"*".equals(storagePrivs)))
    {
      this.amounts = ("(SELECT SUM(st.amount) FROM b_product AS p LEFT JOIN cc_stock st ON st.productId = p.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND st.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id IN(" + storagePrivs + ")))");
      avgPrice = "(SELECT ROUND(SUM(costMoneys) / SUM(amount),4)  FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id IN(" + storagePrivs + ")))";
      moneys = "(SELECT SUM(costMoneys) FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id IN(" + storagePrivs + ")))";
      
      sellOrde = "(SELECT SUM(s.untreatedAmount) FROM b_product AS p LEFT JOIN xs_sellbook_detail AS s ON s.productId = p.id LEFT JOIN xs_sellbook_bill sb ON s.billId = sb.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND sb.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id IN(" + storagePrivs + ")))";
      boughtOrde = "(SELECT SUM(b.untreatedAmount) FROM b_product AS p LEFT JOIN cg_bought_detail AS b ON b.productId = p.id LEFT JOIN cg_bought_bill cb ON b.billId = cb.id WHERE  p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND cb.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id IN(" + storagePrivs + ")))";
      this.draftAmount = ("(SELECT SUM(IFNULL(inAmount,0)-IFNULL(outAmount,0)) draftAmount FROM b_product p  LEFT JOIN cc_stock_records_draft cd ON p.id = cd.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND (cd.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id IN(" + storagePrivs + ")) " + dtaftStoragePids + " ) )");
    }
    else if ("*".equals(storagePrivs))
    {
      this.amounts = ("(SELECT SUM(st.amount) FROM b_product AS p LEFT JOIN cc_stock st ON st.productId = p.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND st.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') ) )");
      avgPrice = "(SELECT ROUND(SUM(costMoneys) / SUM(amount),4)  FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%')) )";
      moneys = "(SELECT SUM(costMoneys) FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%')) )";
      
      sellOrde = "(SELECT SUM(s.untreatedAmount) FROM b_product AS p LEFT JOIN xs_sellbook_detail AS s ON s.productId = p.id LEFT JOIN xs_sellbook_bill sb ON s.billId = sb.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND sb.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%')) )";
      boughtOrde = "(SELECT SUM(b.untreatedAmount) FROM b_product AS p LEFT JOIN cg_bought_detail AS b ON b.productId = p.id LEFT JOIN cg_bought_bill cb ON b.billId = cb.id WHERE  p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND cb.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%')) )";
      this.draftAmount = ("(SELECT SUM(IFNULL(inAmount,0)-IFNULL(outAmount,0)) draftAmount FROM b_product p  LEFT JOIN cc_stock_records_draft cd ON p.id = cd.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND (cd.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%')) " + dtaftStoragePids + ") )");
    }
    else if (StringUtils.isBlank(storagePrivs))
    {
      this.amounts = ("(SELECT SUM(st.amount) FROM b_product AS p LEFT JOIN cc_stock st ON st.productId = p.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND st.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id = 0))");
      avgPrice = "(SELECT ROUND(SUM(costMoneys) / SUM(amount),4)  FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id = 0))";
      moneys = "(SELECT SUM(costMoneys) FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id = 0))";
      
      sellOrde = "(SELECT SUM(s.untreatedAmount) FROM b_product AS p LEFT JOIN xs_sellbook_detail AS s ON s.productId = p.id LEFT JOIN xs_sellbook_bill sb ON s.billId = sb.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND sb.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id = 0) )";
      boughtOrde = "(SELECT SUM(b.untreatedAmount) FROM b_product AS p LEFT JOIN cg_bought_detail AS b ON b.productId = p.id LEFT JOIN cg_bought_bill cb ON b.billId = cb.id WHERE  p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND cb.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id = 0) )";
      this.draftAmount = ("(SELECT SUM(IFNULL(inAmount,0)-IFNULL(outAmount,0)) draftAmount FROM b_product p  LEFT JOIN cc_stock_records_draft cd ON p.id = cd.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND (cd.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + storagePids + "','%') AND id = 0) " + dtaftStoragePids + ") )");
    }
    this.ordeAmount = new StringBuffer("(");
    this.ordeAmount.append("ifnull(" + this.amounts + ",0)");
    this.ordeAmount.append(" + ");
    this.ordeAmount.append("ifnull(" + boughtOrde + ",0)");
    this.ordeAmount.append(" - ");
    this.ordeAmount.append("ifnull(" + sellOrde + ",0)");
    this.ordeAmount.append(")");
    


    String virtualStockFormula = AioerpSys.dao.getValue1(configName, "virtualStockFormula");
    if (StringUtils.isBlank(virtualStockFormula)) {
      virtualStockFormula = " amounts + boughtOrde - sellOrde + draftAmount";
    }
    virtualStockFormula = virtualStockFormula.replaceAll("amounts", "ifnull(" + this.amounts + ",0)").replaceAll("boughtOrde", "ifnull(" + boughtOrde + ",0)").replaceAll("sellOrde", "ifnull(" + sellOrde + ",0)").replaceAll("draftAmount", "ifnull(" + this.draftAmount + ",0)");
    StringBuffer virtualAmount = new StringBuffer("(");
    virtualAmount.append(virtualStockFormula);
    virtualAmount.append(")");
    
    this.SELECTSQL.append(this.amounts + " as amounts,");
    this.SELECTSQL.append(avgPrice + " as avgPrice,");
    this.SELECTSQL.append(moneys + " as moneys,");
    this.SELECTSQL.append(sellOrde + " as sellOrde,");
    this.SELECTSQL.append(boughtOrde + " as boughtOrde,");
    this.SELECTSQL.append(this.draftAmount + " as draftAmount,");
    this.SELECTSQL.append(this.ordeAmount.toString() + " as ordeAmount,");
    this.SELECTSQL.append(virtualAmount.toString() + " as virtualAmount,");
    this.SELECTSQL.append("pro.*");
    
    this.FROMSQL = new StringBuffer(" from b_product AS pro where 1 = 1 ");
  }
  
  public Map<String, Object> getPageByParam(String configName, Map<String, Object> pMap, int pageNum, int numPerPage, int supId, String storagePids, int proNode, String productPids, String filterVal, String listId, String orderField, String orderDirection)
    throws Exception
  {
    virtualStockInit(storagePids, (String)pMap.get("storagePrivs"), configName);
    
    List<Object> paras = new ArrayList();
    if ((!"".equals(productPids)) && (!"{0}".equals(productPids)))
    {
      this.FROMSQL.append(" and pro.pids LIKE ?");
      paras.add(productPids + "%");
    }
    else if (proNode == 2)
    {
      this.FROMSQL.append(" and pro.supId = ? ");
      paras.add(Integer.valueOf(supId));
    }
    else if (proNode == 1)
    {
      this.FROMSQL.append(" and pro.node = " + AioConstants.NODE_1);
      this.FROMSQL.append(" and pro.pids like '%" + supId + "%'");
    }
    if ("sckNoN".equals(filterVal)) {
      this.FROMSQL.append(" and ifnull(" + this.amounts + ",0) != 0 ");
    }
    if ("dsckNoN".equals(filterVal)) {
      this.FROMSQL.append(" and ifnull(" + this.ordeAmount.toString() + ",0) != 0 ");
    }
    if ("csckNoN".equals(filterVal)) {
      this.FROMSQL.append(" and ifnull(" + this.draftAmount + ",0) != 0 ");
    }
    String proPrivs = (String)pMap.get("productPrivs");
    if ((StringUtils.isNotBlank(proPrivs)) && (!"*".equals(proPrivs))) {
      this.FROMSQL.append(" and pro.id in (" + proPrivs + ") ");
    } else if (StringUtils.isBlank(proPrivs)) {
      this.FROMSQL.append(" and pro.id = 0 ");
    }
    this.FROMSQL.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, this.SELECTSQL.toString(), this.FROMSQL.toString(), this.MAP, paras.toArray());
  }
  
  public Map<String, Object> getSupPageByParam(String configName, Map<String, Object> pMap, int pageNum, int numPerPage, int supId, String storagePids, int ObjectId, String listID, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    virtualStockInit(storagePids, (String)pMap.get("storagePrivs"), configName);
    
    List<Object> paras = new ArrayList();
    this.FROMSQL.append(" and pro.supId = ? ");
    paras.add(Integer.valueOf(supId));
    
    String proPrivs = (String)pMap.get("productPrivs");
    if ((StringUtils.isNotBlank(proPrivs)) && (!"*".equals(proPrivs))) {
      this.FROMSQL.append(" and pro.id in (" + proPrivs + ") ");
    } else if (StringUtils.isBlank(proPrivs)) {
      this.FROMSQL.append(" and pro.id = 0 ");
    }
    this.FROMSQL.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyByUpObject(configName, "pro", "id", listID, ObjectId, pageNum, numPerPage, this.SELECTSQL.toString(), this.FROMSQL.toString(), null, this.MAP, paras.toArray());
  }
}
