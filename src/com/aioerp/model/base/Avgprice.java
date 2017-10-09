package com.aioerp.model.base;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class Avgprice
  extends BaseDbModel
{
  public static final Avgprice dao = new Avgprice();
  public static final String TABLE_NAME = "zj_product_avgprice";
  
  public Avgprice getAvgprice(String configName, Integer storageId, Integer productId)
  {
    StringBuffer sql = new StringBuffer("select * from zj_product_avgprice where 1=1");
    List<Object> paras = new ArrayList();
    if ((storageId != null) && (storageId.intValue() != 0))
    {
      sql.append(" and storageId =?");
      paras.add(storageId);
    }
    else
    {
      sql.append(" and storageId is null");
    }
    if (productId != null)
    {
      sql.append(" and productId =?");
      paras.add(productId);
    }
    else
    {
      sql.append(" and productId is null");
    }
    return (Avgprice)dao.findFirst(configName, sql.toString(), paras.toArray());
  }
  
  public Record getAvgpriceByStockRecord(String configName, Integer storageId, Integer productId)
  {
    StringBuffer sql = new StringBuffer();
    sql.append("select sum(c.amount) as amount,case when sum(c.amount)=0 then 0 else sum(c.amount*c.costPrice)/sum(c.amount) end as avgPrice from cc_stock c where 1 =1 ");
    List<Object> paras = new ArrayList();
    if ((storageId != null) && (storageId.intValue() != 0))
    {
      sql.append(" and storageId =?");
      paras.add(storageId);
    }
    else
    {
      sql.append(" and storageId is null");
    }
    if (productId != null)
    {
      sql.append(" and productId =?");
      paras.add(productId);
    }
    else
    {
      sql.append(" and productId is null");
    }
    return Db.use(configName).findFirst(sql.toString(), paras.toArray());
  }
  
  public List<Record> getManyProSckAmount(String configName, Integer storageId, String productIds)
  {
    StringBuffer sql = new StringBuffer("select productId,amount,id,storageId from zj_product_avgprice where 1=1");
    List<Object> paras = new ArrayList();
    if ((storageId != null) && (storageId.intValue() != 0))
    {
      sql.append(" and storageId =?");
      paras.add(storageId);
    }
    else
    {
      sql.append(" and storageId is null");
    }
    sql.append(" and productId in(" + productIds + ") ");
    
    return Db.use(configName).find(sql.toString(), paras.toArray());
  }
  
  public List<Record> getManyProSck(String configName, Integer storageId, String productIds)
  {
    StringBuffer sql = new StringBuffer("select * from zj_product_avgprice where 1=1");
    List<Object> paras = new ArrayList();
    if ((storageId != null) && (storageId.intValue() != 0))
    {
      sql.append(" and storageId =?");
      paras.add(storageId);
    }
    else
    {
      sql.append(" and storageId is null");
    }
    if (StringUtils.isNotBlank(productIds)) {
      sql.append(" and productId in(" + productIds + ") ");
    }
    return Db.use(configName).find(sql.toString(), paras.toArray());
  }
  
  public List<Record> getInitAllData(String configName)
  {
    String sql = "SELECT productId, SUM(amount) amounts, ROUND(SUM(costMoneys)/SUM(amount),4) avgPrice,SUM(costMoneys) costMoneys FROM zj_product_avgprice WHERE storageId IS NOT NULL GROUP BY productId";
    return Db.use(configName).find(sql);
  }
}
