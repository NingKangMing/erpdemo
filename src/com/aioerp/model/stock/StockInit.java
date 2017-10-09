package com.aioerp.model.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Avgprice;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.finance.AccountsInit;
import com.aioerp.model.init.FinanceInitRecords;
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

public class StockInit
  extends BaseDbModel
{
  public static final StockInit dao = new StockInit();
  public static final String TABLE_NAME = "cc_stock_init";
  Map<String, Class<? extends Model>> MAP = new HashMap();
  private String SELECTSQL = "";
  private String FROMSQL = "";
  
  private void init(String sgePids, String stoPrivs, String proPrivs)
  {
    this.MAP.put("pro", Product.class);
    this.MAP.put("sck", Stock.class);
    
    StringBuffer commStorageSql = new StringBuffer("SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%')");
    
    commStorageSql = ComFunController.basePrivsSql(commStorageSql, stoPrivs, null);
    

    String amounts = "(SELECT SUM(st.amount) FROM b_product AS p LEFT JOIN cc_stock_init st ON st.productId = p.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND st.storageId IN (" + commStorageSql.toString() + ") )";
    String avgPrice = "(SELECT ROUND((SUM(money) / SUM(amount) ),4)  FROM cc_stock_init AS st LEFT JOIN b_product AS p ON st.productId = p.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND st.storageId IN (" + commStorageSql.toString() + ") )";
    String moneys = "(SELECT SUM(money) FROM cc_stock_init AS st LEFT JOIN b_product AS p ON st.productId = p.id WHERE p.pids LIKE CONCAT(pro.pids,'%') AND p.node=1 AND st.storageId IN (" + commStorageSql.toString() + ") )";
    
    this.SELECTSQL = (" select " + amounts + " amounts," + avgPrice + " avgPrice," + moneys + " moneys,pro.* ");
    
    StringBuffer sb = new StringBuffer(" from b_product pro ");
    sb.append(" where 1 = 1 ");
    
    sb = ComFunController.basePrivsSql(sb, proPrivs, "pro");
    this.FROMSQL = sb.toString();
  }
  
  private void StorageInit(String proPids)
  {
    this.MAP.put("sge", Storage.class);
    
    String amounts = "";
    String moneys = "";
    if ((proPids != null) && (!"".equals(proPids)))
    {
      amounts = "(SELECT SUM(st.amount) FROM b_product AS p\tLEFT JOIN cc_stock_init st ON st.productId = p.id LEFT JOIN b_storage s ON st.storageId = s.id\tWHERE s.pids LIKE CONCAT(sge.pids,'%') AND p.pids LIKE CONCAT('" + proPids + "','%') AND p.node=1)";
      moneys = "(SELECT SUM(st.money) FROM b_product AS p LEFT JOIN cc_stock_init st ON st.productId = p.id LEFT JOIN b_storage s ON st.storageId = s.id WHERE s.pids LIKE CONCAT(sge.pids,'%') AND p.pids LIKE CONCAT('" + proPids + "','%') AND p.node=1 )";
    }
    this.SELECTSQL = ("select sge.*," + amounts + " amounts," + moneys + " moneys");
    
    StringBuffer sb = new StringBuffer(" from b_storage AS sge");
    sb.append(" where 1 = 1 ");
    this.FROMSQL = sb.toString();
  }
  
  public Map<String, Object> getPageBySupId(String configName, int node, String stoPrivs, String proPrivs, int pageNum, int numPerPage, int supId, String sgePids, String listId, String orderField, String orderDirection, String searchBaseAttr, String searchBaseVal)
    throws Exception
  {
    init(sgePids, stoPrivs, proPrivs);
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    if (node == 0)
    {
      sb.append(" and pro.supId = ? ");
      paras.add(Integer.valueOf(supId));
    }
    else
    {
      sb.append(" and pro.node = ? ");
      paras.add(Integer.valueOf(node));
      sb.append(" and pro.pids like '%" + supId + "%'");
    }
    if ((StringUtils.isNotEmpty(searchBaseAttr)) && (StringUtils.isNotEmpty(searchBaseVal))) {
      if (searchBaseAttr.equals("barCode")) {
        sb.append(" and (pro.barCode1 like '%" + searchBaseVal + "%' or pro.barCode2 like '%" + searchBaseVal + "%' or pro.barCode3 like '%" + searchBaseVal + "%')");
      } else {
        sb.append(" and pro." + searchBaseAttr + " like '%" + searchBaseVal + "%'");
      }
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, this.SELECTSQL, sb.toString(), this.MAP, paras.toArray());
  }
  
  public Map<String, Object> getSupPageBySupIdAndStorageId(String configName, String stoPrivs, String proPrivs, int pageNum, int numPerPage, int supId, String sgePids, int ObjectId, String listID, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    init(sgePids, stoPrivs, proPrivs);
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    

    sb.append(" and pro.supId = ? ");
    paras.add(Integer.valueOf(supId));
    
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyByUpObject(configName, "pro", "id", listID, ObjectId, pageNum, numPerPage, this.SELECTSQL, sb.toString(), null, this.MAP, paras.toArray());
  }
  
  public Map<String, Object> getPageLine(String configName, String stoPrivs, String proPrivs, int pageNum, int numPerPage, int supId, String sgePids, String listId, String orderField, String orderDirection, String searchBaseAttr, String searchBaseVal)
    throws SQLException, Exception
  {
    init(sgePids, stoPrivs, proPrivs);
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    sb.append(" and pro.node = " + AioConstants.NODE_1);
    sb.append(" and pro.pids like '%" + supId + "%'");
    if ((StringUtils.isNotEmpty(searchBaseAttr)) && (StringUtils.isNotEmpty(searchBaseAttr))) {
      if (searchBaseAttr.equals("barCode")) {
        sb.append(" and (pro.barCode1 like '%" + searchBaseVal + "%' or pro.barCode2 like '%" + searchBaseVal + "%' or pro.barCode3 like '%" + searchBaseVal + "%')");
      } else {
        sb.append(" and pro." + searchBaseAttr + " like '%" + searchBaseVal + "%'");
      }
    }
    sb.append(" order by " + orderField + " " + orderDirection);
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, this.SELECTSQL, sb.toString(), this.MAP, new Object[0]);
  }
  
  public Map<String, Object> getSupPageBySupIdAndProId(String configName, String proPids, int pageNum, int numPerPage, int supId, int ObjectId, String listID, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    StorageInit(proPids);
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    
    sb.append(" AND sge.supId = ?");
    paras.add(Integer.valueOf(supId));
    
    String sgePrivs = BaseController.basePrivs(BaseController.STORAGE_PRIVS);
    ComFunController.basePrivsSql(sb, sgePrivs, "sge", "id");
    
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyByUpObject(configName, "sge", "id", listID, ObjectId, pageNum, numPerPage, this.SELECTSQL, sb.toString(), null, this.MAP, paras.toArray());
  }
  
  public Map<String, Object> getProductDistribute(String configName, String proPids, int pageNum, int numPerPage, int supId, String listId, String orderField, String orderDirection)
    throws Exception
  {
    StorageInit(proPids);
    StringBuffer sb = new StringBuffer(this.FROMSQL);
    List<Object> paras = new ArrayList();
    
    sb.append(" AND sge.supId = ?");
    paras.add(Integer.valueOf(supId));
    
    String sgePrivs = BaseController.basePrivs(BaseController.STORAGE_PRIVS);
    ComFunController.basePrivsSql(sb, sgePrivs, "sge", "id");
    
    sb.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listId, pageNum, numPerPage, this.SELECTSQL, sb.toString(), this.MAP, paras.toArray());
  }
  
  public List<Model> getStockGroupAttr(String configName, Integer storageId, Integer productId)
  {
    StringBuffer sql = new StringBuffer("select * from cc_stock_init where amount is not null");
    sql.append(" and productId =?");
    if ((storageId != null) && (storageId.intValue() > 0)) {
      sql.append(" and storageId =?");
    }
    sql.append(" group by productId,batch,price,produceDate,produceEndDate");
    sql.append(" order by id ");
    if ((storageId != null) && (storageId.intValue() > 0)) {
      return dao.find(configName, sql.toString(), new Object[] { productId, storageId });
    }
    return dao.find(configName, sql.toString(), new Object[] { productId });
  }
  
  public Boolean recodeIsExist(String configName, int storageId, int productId, BigDecimal price, String batch, Date produceDate, Date produceEndDate, int id)
  {
    StringBuffer sb = new StringBuffer(" SELECT COUNT(price) FROM cc_stock_init");
    List<Object> param = new ArrayList();
    sb.append("  WHERE storageId=? AND productId=? AND price=?");
    param.add(Integer.valueOf(storageId));
    param.add(Integer.valueOf(productId));
    param.add(price);
    if ((batch == null) || ("".equals(batch)))
    {
      sb.append(" and batch IS NULL");
    }
    else
    {
      sb.append(" and batch = ?");
      param.add(batch);
    }
    if ((produceDate == null) || ("".equals(produceDate))) {
      sb.append(" and produceDate IS NULL");
    } else {
      sb.append(" and produceDate = '" + produceDate + "'");
    }
    if ((produceDate == null) || ("".equals(produceDate))) {
      sb.append(" and produceEndDate IS NULL");
    } else {
      sb.append(" and produceEndDate = '" + produceEndDate + "'");
    }
    sb.append(" and id <> ?");
    param.add(Integer.valueOf(id));
    return Boolean.valueOf(Db.use(configName).queryLong(sb.toString(), param.toArray()).longValue() > 0L);
  }
  
  public StockInit getStockInit(String configName, Integer storageId, Integer productId)
  {
    String sql = "SELECT * FROM cc_stock_init WHERE storageId=? AND productId=?";
    return (StockInit)dao.findFirst(configName, sql, new Object[] { storageId, productId });
  }
  
  public StockInit getStockInit(String configName, Integer storageId, Integer productId, BigDecimal price, String batch, Date produceDate)
  {
    StringBuffer sql = new StringBuffer("select * from cc_stock_init where 1=1");
    List<Object> param = new ArrayList();
    if (productId != null)
    {
      sql.append(" and productId =?");
      param.add(productId);
    }
    else
    {
      sql.append(" and productId is null");
    }
    if (storageId != null)
    {
      sql.append(" and storageId =?");
      param.add(storageId);
    }
    else
    {
      sql.append(" and storageId is null");
    }
    if (StringUtils.isNotBlank(batch))
    {
      sql.append(" and batch =?");
      param.add(batch);
    }
    else
    {
      sql.append(" and batch is null");
    }
    if (price != null)
    {
      sql.append(" and price =?");
      param.add(price);
    }
    else
    {
      sql.append(" and price is null");
    }
    if (produceDate != null)
    {
      sql.append(" and produceDate=?");
      param.add(produceDate);
    }
    else
    {
      sql.append(" and produceDate is null");
    }
    return (StockInit)dao.findFirst(configName, sql.toString(), param.toArray());
  }
  
  public Record getStockInit(String configName, String storagePrivs, String productPrivs, String sgePisd, String proPids)
  {
    StringBuffer sql = preInitComSql(storagePrivs, productPrivs, sgePisd, proPids, "all");
    return Db.use(configName).findFirst(sql.toString());
  }
  
  public static StringBuffer preInitComSql(String storagePrivs, String productPrivs, String sgePisd, String proPids, String type)
  {
    StringBuffer sql = new StringBuffer("(SELECT");
    if (type.equals("all")) {
      sql.append(" SUM(amount) amounts,SUM(money) moneys");
    } else if (type.equals("amount")) {
      sql.append(" SUM(amount) amounts");
    } else if (type.equals("money")) {
      sql.append(" SUM(money) moneys");
    }
    sql.append(" FROM cc_stock_init i ");
    sql.append(" left join b_storage s on s.id=i.storageId");
    sql.append(" left join b_product p on p.id=i.productId");
    sql.append(" where 1=1");
    if ((sgePisd != null) && (!"".equals(sgePisd))) {
      if (sgePisd.startsWith("{")) {
        sql.append(" AND s.pids LIKE CONCAT('" + sgePisd + "','%') ");
      } else {
        sql.append(" AND s.pids LIKE CONCAT(" + sgePisd + ",'%') ");
      }
    }
    if ((proPids != null) && (!"".equals(proPids))) {
      if (proPids.startsWith("{")) {
        sql.append(" AND p.pids LIKE CONCAT('" + proPids + "','%') ");
      } else {
        sql.append(" AND p.pids LIKE CONCAT(" + proPids + ",'%') ");
      }
    }
    ComFunController.queryProductPrivs(sql, productPrivs, "i");
    ComFunController.queryStoragePrivs(sql, storagePrivs, "i");
    
    sql.append(")");
    return sql;
  }
  
  public BigDecimal getMoneyTotal(String configName)
  {
    String sql = "SELECT SUM(money) sckInitMoney FROM cc_stock_init";
    return dao.findFirst(configName, sql).getBigDecimal("sckInitMoney");
  }
  
  public List<Model> getRecordsBySgeAndPro(String configName)
  {
    String sql = "SELECT storageId,productId,SUM(amount) amounts,ROUND(SUM(money)/SUM(amount),4) avgPrice,SUM(money) moneys FROM cc_stock_init GROUP BY storageId,productId";
    return dao.find(configName, sql);
  }
  
  public void moveInitStockToStock(String configName)
  {
    List<Model> stockInitList = find(configName, "SELECT * FROM cc_stock_init");
    for (Model sckInit : stockInitList)
    {
      Stock stock = (Stock)Stock.dao.getObj(configName, sckInit.getInt("productId"), sckInit.getInt("storageId"), sckInit.getStr("batch"), sckInit.getBigDecimal("price"), sckInit.getDate("produceDate"), sckInit.getDate("produceEndDate"));
      BigDecimal amount = sckInit.getBigDecimal("amount");
      if (stock == null)
      {
        stock = new Stock();
        stock.set("storageId", sckInit.getInt("storageId"));
        stock.set("productId", sckInit.getInt("productId"));
      }
      stock.set("amount", amount);
      stock.set("costPrice", sckInit.getBigDecimal("price"));
      stock.set("batchNum", sckInit.getStr("batch"));
      stock.set("produceDate", sckInit.getDate("produceDate"));
      stock.set("produceEndDate", sckInit.getDate("produceEndDate"));
      stock.set("updateTime", DateUtils.getCurrentTime());
      if (stock.get("id") == null)
      {
        if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) != 0) {
          stock.save(configName);
        }
      }
      else if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) != 0) {
        stock.update(configName);
      } else {
        stock.delete(configName);
      }
    }
  }
  
  public void moveInitStockToZjProductAvgPriceBySgeAndPro(String configName)
  {
    List<Model> stockInitPriceList = dao.getRecordsBySgeAndPro(configName);
    for (Model sckInit : stockInitPriceList)
    {
      Integer storageId = sckInit.getInt("storageId");
      Integer productId = sckInit.getInt("productId");
      BigDecimal amounts = sckInit.getBigDecimal("amounts");
      BigDecimal moneys = sckInit.getBigDecimal("moneys");
      BigDecimal avgPrice = sckInit.getBigDecimal("avgPrice");
      Avgprice avgpriceObj = Avgprice.dao.getAvgprice(configName, storageId, productId);
      if ((avgpriceObj == null) && (BigDecimalUtils.compare(amounts, BigDecimal.ZERO) != 0))
      {
        Avgprice avgprice = new Avgprice();
        avgprice.set("storageId", storageId);
        avgprice.set("productId", productId);
        avgprice.set("avgPrice", avgPrice);
        avgprice.set("costMoneys", moneys);
        avgprice.set("amount", amounts);
        avgprice.save(configName);
      }
      else if (avgpriceObj != null)
      {
        avgpriceObj.set("amount", amounts);
        avgpriceObj.set("costMoneys", moneys);
        avgpriceObj.set("avgPrice", avgPrice);
        if (BigDecimalUtils.compare(amounts, BigDecimal.ZERO) != 0) {
          avgpriceObj.update(configName);
        } else {
          avgpriceObj.delete(configName);
        }
      }
    }
  }
  
  public void moveInitStockToZjProductAvgPriceByAllPro(String configName)
  {
    List<Record> priceDataList = Avgprice.dao.getInitAllData(configName);
    for (Record priceData : priceDataList)
    {
      Integer productId = priceData.getInt("productId");
      BigDecimal amounts = priceData.getBigDecimal("amounts");
      BigDecimal moneys = priceData.getBigDecimal("costMoneys");
      BigDecimal avgPrice = priceData.getBigDecimal("avgPrice");
      Avgprice avgpriceObj = Avgprice.dao.getAvgprice(configName, null, productId);
      if ((avgpriceObj == null) && (BigDecimalUtils.compare(amounts, BigDecimal.ZERO) != 0))
      {
        Avgprice avgprice = new Avgprice();
        avgprice.set("productId", productId);
        avgprice.set("avgPrice", avgPrice);
        avgprice.set("costMoneys", moneys);
        avgprice.set("amount", amounts);
        avgprice.save(configName);
      }
      else if (avgpriceObj != null)
      {
        avgpriceObj.set("amount", amounts);
        avgpriceObj.set("costMoneys", moneys);
        avgpriceObj.set("avgPrice", avgPrice);
        if (BigDecimalUtils.compare(amounts, BigDecimal.ZERO) != 0) {
          avgpriceObj.update(configName);
        } else {
          avgpriceObj.delete(configName);
        }
      }
    }
  }
  
  public void openInitDateMove(String configName, String hasOpenAccountTime, String type)
    throws ParseException
  {
    if (hasOpenAccountTime == null) {
      hasOpenAccountTime = DateUtils.format(new Date(), "yyyy-MM-dd");
    }
    if (type == null) {
      Unit.dao.openAccountUpdateUnitGetOrPay(configName);
    }
    dao.moveInitStockToStock(configName);
    


    dao.moveInitStockToZjProductAvgPriceBySgeAndPro(configName);
    
    dao.moveInitStockToZjProductAvgPriceByAllPro(configName);
    if (type == null)
    {
      List<Record> initAccountList = FinanceInitRecords.dao.getInitAccount(configName);
      if (initAccountList != null) {
        for (int i = 0; i < initAccountList.size(); i++)
        {
          Record r = (Record)initAccountList.get(i);
          if (BigDecimalUtils.compare(r.getBigDecimal("money"), BigDecimal.ZERO) != 0)
          {
            AccountsInit accountsInit = new AccountsInit();
            accountsInit.set("accountsId", r.getInt("id"));
            accountsInit.set("money", r.getBigDecimal("money"));
            accountsInit.set("time", hasOpenAccountTime);
            accountsInit.save(configName);
          }
        }
      }
      List<Record> initProducttList = FinanceInitRecords.dao.getInitProduct(configName);
      if (initAccountList != null)
      {
        Accounts initProAccount = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
        Integer accountId = initProAccount != null ? initProAccount.getInt("id") : null;
        for (int i = 0; i < initProducttList.size(); i++)
        {
          Record r = (Record)initProducttList.get(i);
          if (BigDecimalUtils.compare(r.getBigDecimal("money"), BigDecimal.ZERO) != 0)
          {
            AccountsInit accountsInit = new AccountsInit();
            accountsInit.set("accountsId", accountId);
            accountsInit.set("productId", r.getInt("id"));
            accountsInit.set("money", r.getBigDecimal("money"));
            accountsInit.set("time", hasOpenAccountTime);
            accountsInit.save(configName);
          }
        }
      }
      if (type == null)
      {
        List<Record> initUnitList = FinanceInitRecords.dao.getInitUnit(configName);
        if (initUnitList != null)
        {
          Accounts account00013 = (Accounts)Accounts.dao.getModelFirst(configName, "00013");
          Accounts account000413 = (Accounts)Accounts.dao.getModelFirst(configName, "000413");
          Integer accountId00013 = account00013 != null ? account00013.getInt("id") : null;
          Integer accountId000413 = account000413 != null ? account000413.getInt("id") : null;
          for (int i = 0; i < initUnitList.size(); i++)
          {
            Record r = (Record)initUnitList.get(i);
            if (BigDecimalUtils.compare(r.getBigDecimal("beginGetMoney"), BigDecimal.ZERO) != 0)
            {
              AccountsInit accountsInit = new AccountsInit();
              accountsInit.set("accountsId", accountId000413);
              accountsInit.set("unitId", r.getInt("id"));
              accountsInit.set("money", r.getBigDecimal("beginGetMoney"));
              accountsInit.set("time", hasOpenAccountTime);
              accountsInit.save(configName);
            }
            if (BigDecimalUtils.compare(r.getBigDecimal("beginPayMoney"), BigDecimal.ZERO) != 0)
            {
              AccountsInit accountsInit = new AccountsInit();
              accountsInit.set("accountsId", accountId00013);
              accountsInit.set("unitId", r.getInt("id"));
              accountsInit.set("money", r.getBigDecimal("beginPayMoney"));
              accountsInit.set("time", hasOpenAccountTime);
              accountsInit.save(configName);
            }
          }
        }
      }
    }
  }
}
