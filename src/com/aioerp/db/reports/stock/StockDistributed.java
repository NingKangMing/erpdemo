package com.aioerp.db.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class StockDistributed
  extends BaseDbModel
{
  public static final StockDistributed dao = new StockDistributed();
  Map<String, Class<? extends Model>> MAP = new HashMap();
  private StringBuffer SELECTSQL;
  private StringBuffer FROMSQL;
  
  public void init(String configName, List<Model> storages, String unitPattern, String priceWay)
  {
    this.MAP.put("pro", Product.class);
    
    this.SELECTSQL = new StringBuffer("select pro.*,");
    String amountSql = "";
    String moneySql = "";
    



    int index = 0;
    for (Model storage : storages)
    {
      String storageId = storage.getInt("id").toString();
      if (unitPattern.equals("baseUnit"))
      {
        amountSql = "(select SUM(zj.amount) from zj_product_avgprice zj LEFT JOIN b_product p ON p.id=zj.productId where p.pids LIKE CONCAT(pro.pids,'%') and zj.storageId = " + storageId + ")";
        if (priceWay.equals("stockPrice")) {
          moneySql = "(select SUM(zj.costMoneys) from zj_product_avgprice zj LEFT JOIN b_product p ON p.id=zj.productId where p.pids LIKE CONCAT(pro.pids,'%') and zj.storageId = " + storageId + ")";
        } else if (!priceWay.equals("budgetPrice1")) {
          if (!priceWay.equals("budgetPrice2")) {
            if (!priceWay.equals("budgetPrice3")) {
              if (!priceWay.equals("retailPrice")) {
                priceWay.equals("lastInPrice");
              }
            }
          }
        }
      }
      else if (unitPattern.equals("helpUnit1"))
      {
        amountSql = "( round((select SUM(zj.amount) from zj_product_avgprice zj LEFT JOIN b_product p ON p.id=zj.productId where p.pids LIKE CONCAT(pro.pids,'%') and zj.storageId = " + storageId + ") / (SELECT unitRelation2 FROM b_product p WHERE p.id=pro.id),4) )";
        if (priceWay.equals("stockPrice")) {
          moneySql = "(select SUM(zj.costMoneys) from zj_product_avgprice zj LEFT JOIN b_product p ON p.id=zj.productId where p.pids LIKE CONCAT(pro.pids,'%') and zj.storageId = " + storageId + ")";
        } else if (!priceWay.equals("budgetPrice1")) {
          if (!priceWay.equals("budgetPrice2")) {
            if (!priceWay.equals("budgetPrice3")) {
              if (!priceWay.equals("retailPrice")) {
                priceWay.equals("lastInPrice");
              }
            }
          }
        }
      }
      else if (unitPattern.equals("helpUnit2"))
      {
        amountSql = "( round((select SUM(zj.amount) from zj_product_avgprice zj LEFT JOIN b_product p ON p.id=zj.productId where p.pids LIKE CONCAT(pro.pids,'%') and zj.storageId = " + storageId + ") / (SELECT unitRelation3 FROM b_product p WHERE p.id=pro.id),4) )";
        if (priceWay.equals("stockPrice")) {
          moneySql = "(select SUM(zj.costMoneys) from zj_product_avgprice zj LEFT JOIN b_product p ON p.id=zj.productId where p.pids LIKE CONCAT(pro.pids,'%') and zj.storageId = " + storageId + ")";
        } else if (!priceWay.equals("budgetPrice1")) {
          if (!priceWay.equals("budgetPrice2")) {
            if (!priceWay.equals("budgetPrice3")) {
              if (!priceWay.equals("retailPrice")) {
                priceWay.equals("lastInPrice");
              }
            }
          }
        }
      }
      this.SELECTSQL.append(amountSql + " as amount" + index + ",");
      this.SELECTSQL.append(moneySql + " as money" + index + ",");
      index++;
    }
    this.SELECTSQL.append("0 as allMoneys ");
    
    this.FROMSQL = new StringBuffer(" FROM b_product pro");
    this.FROMSQL.append(" where 1 = 1");
  }
  
  public Map<String, Object> getListByParam(String configName, String proPrivs, List<Model> sList, String unitPattern, String priceWay, int supId, String listID, int pageNum, int numPerPage, String orderField, String orderDirection, int isNode, String searchBaseAttr, String searchBaseVal)
    throws SQLException, Exception
  {
    init(configName, sList, unitPattern, priceWay);
    if (isNode == 0)
    {
      this.FROMSQL.append(" and pro.supId = " + supId);
    }
    else if (isNode == 1)
    {
      this.FROMSQL.append(" and pro.node = " + AioConstants.NODE_1);
      this.FROMSQL.append(" and pro.pids like '%" + supId + "%'");
    }
    if ((StringUtils.isNotBlank(proPrivs)) && (!"*".equals(proPrivs))) {
      this.FROMSQL.append(" and pro.id in (" + proPrivs + ") ");
    } else if (StringUtils.isBlank(proPrivs)) {
      this.FROMSQL.append(" and pro.id = 0 ");
    }
    if ((StringUtils.isNotEmpty(searchBaseAttr)) && (StringUtils.isNotEmpty(searchBaseVal))) {
      if (searchBaseAttr.equals("barCode")) {
        this.FROMSQL.append(" and (pro.barCode1 like '%" + searchBaseVal + "%' or pro.barCode2 like '%" + searchBaseVal + "%' or pro.barCode3 like '%" + searchBaseVal + "%')");
      } else {
        this.FROMSQL.append(" and pro." + searchBaseAttr + " like '%" + searchBaseVal + "%'");
      }
    }
    this.FROMSQL.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, this.SELECTSQL.toString(), this.FROMSQL.toString(), this.MAP, new Object[0]);
  }
  
  public Map<String, Object> getSupPageByParam(String configName, List<Model> sList, String unitPattern, String priceWay, int pageNum, int numPerPage, int supId, int ObjectId, String listID, String orderField, String orderDirection)
    throws SQLException, Exception
  {
    init(configName, sList, unitPattern, priceWay);
    
    List<Object> paras = new ArrayList();
    this.FROMSQL.append(" and pro.supId = ? ");
    paras.add(Integer.valueOf(supId));
    
    this.FROMSQL.append(" order by " + orderField + " " + orderDirection);
    
    return aioGetPageManyByUpObject(configName, "pro", "id", listID, ObjectId, pageNum, numPerPage, this.SELECTSQL.toString(), this.FROMSQL.toString(), null, this.MAP, paras.toArray());
  }
}
