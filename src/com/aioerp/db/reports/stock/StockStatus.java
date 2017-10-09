package com.aioerp.db.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class StockStatus
  extends BaseDbModel
{
  public static final StockStatus dao = new StockStatus();
  Map<String, Class<? extends Model>> MAP = new HashMap();
  private String SELECTSQL = "";
  private String FROMSQL = "";
  String amounts = "";
  
  private void init(String sgePids, String storagePrivs)
  {
    this.MAP.put("pro", Product.class);
    this.MAP.put("sck", Stock.class);
    
    String avgPrice = "";
    String moneys = "";
    if ((StringUtils.isNotBlank(storagePrivs)) && (!"*".equals(storagePrivs)))
    {
      this.amounts = ("(SELECT SUM(st.amount) FROM b_product AS p LEFT JOIN cc_stock st ON st.productId = p.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND st.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%') AND id IN(" + storagePrivs + ")))");
      avgPrice = "(SELECT ROUND(SUM(costMoneys) / SUM(amount),4)  FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%') AND id IN(" + storagePrivs + ")))";
      moneys = "(SELECT SUM(costMoneys) FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%') AND id IN(" + storagePrivs + ")))";
    }
    else if ("*".equals(storagePrivs))
    {
      this.amounts = ("(SELECT SUM(st.amount) FROM b_product AS p LEFT JOIN cc_stock st ON st.productId = p.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND st.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%') ) )");
      avgPrice = "(SELECT ROUND(SUM(costMoneys) / SUM(amount),4)  FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%')) )";
      moneys = "(SELECT SUM(costMoneys) FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%')) )";
    }
    else if (StringUtils.isBlank(storagePrivs))
    {
      this.amounts = ("(SELECT SUM(st.amount) FROM b_product AS p LEFT JOIN cc_stock st ON st.productId = p.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND st.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%') AND id = 0))");
      avgPrice = "(SELECT ROUND(SUM(costMoneys) / SUM(amount),4)  FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%') AND id = 0))";
      moneys = "(SELECT SUM(costMoneys) FROM zj_product_avgprice AS price LEFT JOIN b_product AS p ON p.id = price.productId WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND price.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%') AND id = 0))";
    }
    this.SELECTSQL = (" select " + this.amounts + " amounts," + avgPrice + " avgPrice," + moneys + " moneys,pro.* ");
    
    StringBuffer sb = new StringBuffer(" from b_product pro ");
    sb.append(" where 1 = 1 ");
    this.FROMSQL = sb.toString();
  }
  
  private void proDistribution(String proPids, String configName)
  {
    this.MAP.put("sge", Storage.class);
    
    String amounts = "";
    String moneys = "";
    String boughtOrde = "";
    String sellOrde = "";
    String draftAmount = "";
    StringBuffer virtualAmount = new StringBuffer("");
    if ((proPids != null) && (!"".equals(proPids)))
    {
      amounts = "(SELECT SUM(st.amount) FROM b_product AS p\tLEFT JOIN cc_stock st ON st.productId = p.id LEFT JOIN b_storage s ON st.storageId = s.id\tWHERE s.pids LIKE CONCAT(sge.pids,'%') AND p.pids LIKE CONCAT('" + proPids + "','%') AND p.node=1)";
      moneys = "(SELECT SUM(costMoneys) FROM zj_product_avgprice AS price LEFT JOIN b_storage s ON price.storageId = s.id LEFT JOIN b_product p ON price.productId = p.id WHERE  1=1 AND s.pids LIKE CONCAT(sge.pids,'%') AND p.pids LIKE CONCAT('" + proPids + "','%') AND p.node=1) ";
      boughtOrde = "(SELECT SUM(b.untreatedAmount) FROM b_product AS p LEFT JOIN cg_bought_detail AS b ON b.productId = p.id LEFT JOIN cg_bought_bill AS bb ON b.billId=bb.id LEFT JOIN b_storage s ON bb.storageId = s.id WHERE  p.pids LIKE CONCAT('" + proPids + "','%') AND p.node=1 AND s.pids LIKE CONCAT(sge.pids,'%'))";
      sellOrde = "(SELECT SUM(sd.untreatedAmount) FROM b_product AS p LEFT JOIN xs_sellbook_detail AS sd ON sd.productId = p.id LEFT JOIN xs_sellbook_bill AS sb ON sd.billId=sb.id LEFT JOIN b_storage s ON sb.storageId = s.id WHERE  p.pids LIKE CONCAT('" + proPids + "','%') AND p.node=1 AND s.pids LIKE CONCAT(sge.pids,'%'))";
      draftAmount = "(SELECT SUM(IFNULL(inAmount,0)-IFNULL(outAmount,0)) FROM b_product p LEFT JOIN cc_stock_records_draft cd ON p.id = cd.productId LEFT JOIN b_storage sg ON sg.id=cd.storageId WHERE p.pids LIKE CONCAT('" + proPids + "','%') AND p.node=1 AND sg.pids LIKE CONCAT(sge.pids,'%'))";
      

      String virtualStockFormula = AioerpSys.dao.getValue1(configName, "virtualStockFormula");
      if (StringUtils.isBlank(virtualStockFormula)) {
        virtualStockFormula = " amounts + boughtOrde - sellOrde + draftAmount";
      }
      virtualStockFormula = virtualStockFormula.replaceAll("amounts", "ifnull(" + amounts + ",0)").replaceAll("boughtOrde", "ifnull(" + boughtOrde + ",0)").replaceAll("sellOrde", "ifnull(" + sellOrde + ",0)").replaceAll("draftAmount", "ifnull(" + draftAmount + ",0)");
      
      virtualAmount.append("(");
      virtualAmount.append(virtualStockFormula);
      virtualAmount.append(")");
    }
    this.SELECTSQL = ("select sge.*," + amounts + " amounts," + moneys + " moneys," + virtualAmount.toString() + " virtualAmounts");
    
    StringBuffer sb = new StringBuffer(" from b_storage AS sge");
    sb.append(" where 1 = 1 ");
    this.FROMSQL = sb.toString();
  }
  
  public Record productReport(String configName, Integer proId)
  {
    if (proId.intValue() > 0)
    {
      Product product = (Product)Product.dao.findById(configName, proId);
      String proPids = product.getStr("pids");
      

      String lastSellPrice = "(SELECT lastSellPrice FROM fz_pricediscount_track WHERE productId = pro.id AND selectUnitId = 1 ORDER BY lastSellDate DESC LIMIT 1)";
      
      String lastBuyPrice = "(SELECT lastCostPrice FROM fz_pricediscount_track WHERE productId = pro.id AND selectUnitId = 1 ORDER BY lastCostDate DESC LIMIT 1)";
      String weekdata = "(SELECT SUM(baseAmount) weekAmout,SUM(basePrice) weekMoney FROM xs_sell_detail AS sell WHERE YEARWEEK(DATE_FORMAT(sell.updateTime,'%Y-%m-%d')) = YEARWEEK(NOW()) AND sell.productId IN (SELECT id FROM b_product WHERE pids LIKE CONCAT('" + proPids + "','%')))";
      String monthdata = "(SELECT SUM(baseAmount) monthAmout,SUM(basePrice) monthMoney FROM xs_sell_detail AS sell WHERE DATE_FORMAT(sell.updateTime,'%Y-%m')=DATE_FORMAT(NOW(),'%Y-%m') AND sell.productId IN (SELECT id FROM b_product WHERE pids LIKE CONCAT('" + proPids + "','%')))";
      String yeardata = "(SELECT SUM(baseAmount) yearAmout,SUM(basePrice) yearMoney FROM xs_sell_detail AS sell WHERE YEARWEEK(DATE_FORMAT(sell.updateTime,'%Y-%m-%d')) = YEARWEEK(NOW()) AND sell.productId IN (SELECT id FROM b_product WHERE pids LIKE CONCAT('" + proPids + "','%')))";
      
      this.SELECTSQL = ("SELECT pro.id,pro.fullName,pro.pids,weekdata.weekAmout,weekdata.weekMoney,monthdata.monthAmout,monthdata.monthMoney,yeardata.yearAmout,yeardata.yearMoney," + lastBuyPrice + " as lastBuyPrice," + lastSellPrice + " as lastSellPrice ");
      this.FROMSQL = (" FROM b_product pro," + weekdata + " as weekdata," + monthdata + " as monthdata," + yeardata + " as yeardata where pro.id=" + proId);
      
      return Db.use(configName).findFirst(this.SELECTSQL + this.FROMSQL);
    }
    return null;
  }
  
  public List<Model> getSortsByStorageId(String configName, int storageId)
  {
    this.MAP.put("pro", Product.class);
    if (storageId == 0)
    {
      StringBuffer sb = new StringBuffer("SELECT * FROM b_product pro WHERE 1=1 and pro.node = ?");
      String proPrivs = BaseController.basePrivs(BaseController.PRODUCT_PRIVS);
      ComFunController.basePrivsSql(sb, proPrivs, "pro", "id");
      sb.append(" GROUP BY pro.id order by pro.rank asc");
      return find(configName, sb.toString(), this.MAP, new Object[] { Integer.valueOf(AioConstants.NODE_2) });
    }
    return null;
  }
  
  public Map<String, Object> getPageBySupId(String configName, String filterVal, Map<String, Object> pMap, int pageNum, int numPerPage, int supId, String sgePids, String listId, String orderField, String orderDirection, int isNode)
    throws Exception
  {
    init(sgePids, (String)pMap.get("storagePrivs"));
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    if (isNode == 0)
    {
      sb.append(" and pro.supId = ? ");
      paras.add(Integer.valueOf(supId));
    }
    else if (isNode == 1)
    {
      sb.append(" and pro.node = " + AioConstants.NODE_1);
      sb.append(" and pro.pids like '%" + supId + "%'");
    }
    String proPrivs = (String)pMap.get("productPrivs");
    if ((StringUtils.isNotBlank(proPrivs)) && (!"*".equals(proPrivs))) {
      sb.append(" and pro.id in (" + proPrivs + ") ");
    } else if (StringUtils.isBlank(proPrivs)) {
      sb.append(" and pro.id = 0 ");
    }
    if ("noZero".equals(filterVal)) {
      sb.append(" and IFNULL(" + this.amounts + ",0) != 0 ");
    } else if ("onlyZero".equals(filterVal)) {
      sb.append(" and IFNULL(" + this.amounts + ",0) = 0 ");
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, this.SELECTSQL, sb.toString(), this.MAP, paras.toArray());
  }
  
  public Map<String, Object> getPageLine(String configName, String filterVal, Map<String, Object> pMap, int pageNum, int numPerPage, int supId, String sgePids, String listId, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    init(sgePids, (String)pMap.get("storagePrivs"));
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    sb.append(" and pro.node = " + AioConstants.NODE_1);
    sb.append(" and pro.pids like '%" + supId + "%'");
    ComFunController.basePrivsSql(sb, (String)pMap.get("productPrivs"), "pro", "id");
    if ("noZero".equals(filterVal)) {
      sb.append(" and IFNULL(" + this.amounts + ",0) != 0 ");
    } else if ("onlyZero".equals(filterVal)) {
      sb.append(" and IFNULL(" + this.amounts + ",0) = 0 ");
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, this.SELECTSQL, sb.toString(), this.MAP, new Object[0]);
  }
  
  public Map<String, Object> getSupPageBySupIdAndStorageId(String configName, String filterVal, Map<String, Object> pMap, int pageNum, int numPerPage, int supId, String sgePids, int ObjectId, String listID, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    init(sgePids, (String)pMap.get("storagePrivs"));
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    
    sb.append(" and pro.supId = ? ");
    paras.add(Integer.valueOf(supId));
    
    String proPrivs = BaseController.basePrivs(BaseController.PRODUCT_PRIVS);
    ComFunController.basePrivsSql(sb, proPrivs, "pro", "id");
    if ("noZero".equals(filterVal)) {
      sb.append(" and IFNULL(" + this.amounts + ",0) != 0 ");
    } else if ("onlyZero".equals(filterVal)) {
      sb.append(" and IFNULL(" + this.amounts + ",0) = 0 ");
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyByUpObject(configName, "pro", "id", listID, ObjectId, pageNum, numPerPage, this.SELECTSQL, sb.toString(), null, this.MAP, paras.toArray());
  }
  
  public Map<String, Object> getProductDistribute(String configName, String proPids, int pageNum, int numPerPage, int supId, String listId, String orderField, String orderDirection)
    throws Exception
  {
    proDistribution(proPids, configName);
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    
    sb.append(" AND sge.supId = ?");
    paras.add(Integer.valueOf(supId));
    
    String sgePrivs = BaseController.basePrivs(BaseController.STORAGE_PRIVS);
    ComFunController.basePrivsSql(sb, sgePrivs, "sge", "id");
    
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, this.SELECTSQL, sb.toString(), this.MAP, paras.toArray());
  }
  
  public Map<String, Object> getSupPageBySupIdAndProId(String configName, String proPids, int pageNum, int numPerPage, int supId, int ObjectId, String listID, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    proDistribution(proPids, configName);
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    
    sb.append(" AND sge.supId = ?");
    paras.add(Integer.valueOf(supId));
    
    String sgePrivs = BaseController.basePrivs(BaseController.STORAGE_PRIVS);
    ComFunController.basePrivsSql(sb, sgePrivs, "sge", "id");
    
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyByUpObject(configName, "sge", "id", listID, ObjectId, pageNum, numPerPage, this.SELECTSQL, sb.toString(), null, this.MAP, paras.toArray());
  }
  
  public Map<String, Object> getPageByParam(String configName, Map<String, Object> pMap, String listId, Integer pageNum, Integer numPerPage, String orderField, String orderDirection, String sgePids, String searchPar, String searchVal, String pattern)
    throws Exception
  {
    init(sgePids, (String)pMap.get("storagePrivs"));
    StringBuffer sb = new StringBuffer("from b_product pro RIGHT JOIN cc_stock sck on pro.id=sck.productId where 1 = 1");
    List<Object> paras = new ArrayList();
    
    String proPrivs = (String)pMap.get("productPrivs");
    if ((StringUtils.isNotBlank(proPrivs)) && (!"*".equals(proPrivs))) {
      sb.append(" and pro.id in (" + proPrivs + ") ");
    } else if (StringUtils.isBlank(proPrivs)) {
      sb.append(" and pro.id = 0 ");
    }
    if ((!"".equals(searchPar)) && (searchPar != null))
    {
      if (searchPar.equals("all"))
      {
        if (!StringUtil.isNull(searchVal))
        {
          sb.append(" and (pro.code LIKE ?");
          paras.add("%" + searchVal + "%");
          sb.append(" or pro.smallName LIKE ?");
          paras.add("%" + searchVal + "%");
          sb.append(" or pro.fullName LIKE ?");
          paras.add("%" + searchVal + "%");
          sb.append(" or pro.spell LIKE ?");
          paras.add("%" + searchVal + "%");
          sb.append(" or pro.standard LIKE ?");
          paras.add("%" + searchVal + "%");
          sb.append(" or pro.model LIKE ?");
          paras.add("%" + searchVal + "%");
          sb.append(" or pro.barCode1 LIKE ?");
          paras.add("%" + searchVal + "%");
          sb.append(" or pro.barCode2 LIKE ?");
          paras.add("%" + searchVal + "%");
          sb.append(" or pro.barCode3 LIKE ?");
          paras.add("%" + searchVal + "%");
          sb.append(" or pro.field LIKE ? )");
          paras.add("%" + searchVal + "%");
        }
      }
      else if ("barCode1".equals(searchPar))
      {
        sb.append(" and (pro.barCode1 LIKE ? or pro.barCode2 LIKE ? or pro.barCode2 LIKE ?)");
        paras.add("%" + searchVal + "%");
        paras.add("%" + searchVal + "%");
        paras.add("%" + searchVal + "%");
      }
      else
      {
        sb.append(" and pro." + searchPar + " LIKE ?");
        paras.add("%" + searchVal + "%");
      }
      sb.append(" and pro.node = 1");
      if (!"".equals(sgePids))
      {
        sb.append(" and sck.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT(?,'%'))");
        paras.add(sgePids);
      }
      if (pattern.equals("merge"))
      {
        sb.append(" GROUP BY pro.id ");
      }
      else if (pattern.equals("split"))
      {
        String amounts = "(sck.amount*1)";
        String avgPrice = "(sck.costPrice*1)";
        String moneys = "(sck.amount*sck.costPrice)";
        
        this.SELECTSQL = (" select " + amounts + " amounts," + avgPrice + " avgPrice," + moneys + " moneys,pro.*,sck.* ");
        
        sb.append(" GROUP BY sck.productId,sck.storageId,sck.batchNum,sck.costPrice,sck.produceDate ");
      }
      sb.append(" order by " + orderField + " " + orderDirection);
    }
    return aioGetPageManyRecord(configName, listId, pageNum.intValue(), numPerPage.intValue(), this.SELECTSQL, sb.toString(), this.MAP, paras.toArray());
  }
}
