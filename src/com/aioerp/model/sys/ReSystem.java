package com.aioerp.model.sys;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.DbPro;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReSystem
  extends BaseDbModel
{
  public static final ReSystem dao = new ReSystem();
  
  public static boolean delTables(String configName, String item)
    throws SQLException
  {
    String[] tabNames = new String[0];
    String[] baseTabName = { "b_area", "b_department", "b_product", "b_product_unit", "b_staff", "b_storage", "b_unit" };
    String[] initTabName = { "cc_stock_init" };
    String[] orderTabName = { "cg_bought_bill", "cg_bought_detail", "xs_sellbook_bill", "xs_sellbook_detail" };
    if ("base".equals(item)) {
      tabNames = baseTabName;
    } else if ("init".equals(item)) {
      tabNames = initTabName;
    } else if ("order".equals(item)) {
      tabNames = orderTabName;
    }
    Connection con = DbKit.getConfig(configName).getConnection();
    try
    {
      for (int i = 0; i < tabNames.length; i++) {
        Db.use(configName).update("delete from " + tabNames[i]);
      }
      if ("init".equals(item))
      {
        Db.use(configName).update("UPDATE b_unit SET beginGetMoney=NULL,beginPayMoney=NULL,beginPreGetMoney=NULL,beginPrePayMoney=NULL");
        
        Db.use(configName).update("UPDATE b_unit SET totalGet=NULL,totalPay=NULL,totalPreGet=NULL,totalPrePay=NULL");
        

        Db.use(configName).update("UPDATE b_accounts SET money=NULL");
      }
      if ("draft".equals(item))
      {
        DatabaseMetaData databaseMetaData = con.getMetaData();
        ResultSet rs = null;
        
        rs = databaseMetaData.getTables("", "", "", null);
        while (rs.next())
        {
          String tableType = rs.getString("TABLE_TYPE");
          String tableNmae = rs.getString("TABLE_NAME");
          if ((tableType != null) && (tableType.equals("TABLE")) && (tableNmae.indexOf("_draft") != -1)) {
            Db.use(configName).update("delete from " + tableNmae);
          }
        }
        Db.use(configName).update("delete from bb_businessdraft");
        Db.use(configName).update("delete from om_port_record");
        Db.use(configName).update("delete from om_port_record_detail");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      con.setAutoCommit(false);
      con.rollback();
      return false;
    }
    return true;
  }
  
  public static void resetBookBillTables(String configName)
    throws SQLException
  {
    Db.use(configName).update("update cg_bought_bill set status =1,arrivalAmounts = 0");
    Db.use(configName).update("update cg_bought_detail SET arrivalAmount = 0 ,untreatedAmount = amount,replenishAmount=0,forceAmount=0,giftAmount=if(price=0,amount,0)");
    Db.use(configName).update("update xs_sellbook_bill SET relStatus = 1");
    Db.use(configName).update("update xs_sellbook_detail SET arrivalAmount=0,untreatedAmount = amount,replenishAmount=0,forceAmount=0,relStatus=1");
  }
}
