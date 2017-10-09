package com.aioerp.util.dbsource;

import com.aioerp.common.AioConstants;
import com.aioerp.common.plugin.BasePlugin;
import com.aioerp.common.plugin.BoughtPlugin;
import com.aioerp.common.plugin.FZPlugin;
import com.aioerp.common.plugin.FinancePlugin;
import com.aioerp.common.plugin.ReportsPlugin;
import com.aioerp.common.plugin.SellPlugin;
import com.aioerp.common.plugin.StockPlugin;
import com.aioerp.common.plugin.SupAdminPlugin;
import com.aioerp.common.plugin.SysPlugin;
import com.aioerp.common.plugin.port.AioerpOmPortPlugin;
import com.aioerp.util.IO;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import java.io.File;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.apache.tools.ant.taskdefs.SQLExec.OnError;
import org.apache.tools.ant.types.EnumeratedAttribute;

public class C3p0DateSourceUtil
{
  public static boolean createWhichDbStructure(String dataBaseName)
  {
    boolean flag = true;
    try
    {
      String oldPath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "tableStructure.sql";
      String newPath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "tableStructure_temp.sql";
      
      IO.replaceFileContent(oldPath, newPath, "lzmdatabasename", dataBaseName);
      
      SQLExec sqlExec = new SQLExec();
      sqlExec.setDriver(PropKit.get("driverClass"));
      
      sqlExec.setUrl(PropKit.get("jdbcUrl") + PropKit.get("jdbcDbName") + PropKit.get("jdbcUrlParam"));
      sqlExec.setUserid(PropKit.get("user"));
      sqlExec.setPassword(PropKit.get("password"));
      sqlExec.setEncoding(AioConstants.ENCODING);
      
      sqlExec.setSrc(new File(newPath));
      
      sqlExec.setOnerror((SQLExec.OnError)EnumeratedAttribute.getInstance(SQLExec.OnError.class, "abort"));
      sqlExec.setPrint(true);
      
      sqlExec.setOutput(new File("batchExportSqlError.out"));
      sqlExec.setProject(new Project());
      sqlExec.execute();
      
      IO.deleteFile(new File(newPath));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      flag = false;
    }
    return flag;
  }
  
  public static boolean initOrUpdateWhichDbData(String dataBaseName, String initDataPath)
  {
    boolean flag = true;
    try
    {
      SQLExec sqlExec = new SQLExec();
      sqlExec.setDriver(PropKit.get("driverClass"));
      
      sqlExec.setUrl(PropKit.get("jdbcUrl") + dataBaseName + PropKit.get("jdbcUrlParam"));
      sqlExec.setUserid(PropKit.get("user"));
      sqlExec.setPassword(PropKit.get("password"));
      sqlExec.setEncoding(AioConstants.ENCODING);
      
      sqlExec.setSrc(new File(initDataPath));
      
      sqlExec.setOnerror((SQLExec.OnError)EnumeratedAttribute.getInstance(SQLExec.OnError.class, "abort"));
      sqlExec.setPrint(true);
      
      sqlExec.setOutput(new File("batchInputSqlError.out"));
      sqlExec.setProject(new Project());
      sqlExec.execute();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      flag = false;
    }
    return flag;
  }
  
  public static C3p0Plugin createC3p0Plugin(String dataBaseName)
  {
    C3p0Plugin c3p0Plugin = new C3p0Plugin(PropKit.get("jdbcUrl") + dataBaseName + PropKit.get("jdbcUrlParam"), PropKit.get("user"), PropKit.get("password").trim());
    
    c3p0Plugin.setMaxPoolSize(100);
    
    c3p0Plugin.setMinPoolSize(10);
    
    c3p0Plugin.setInitialPoolSize(11);
    
    c3p0Plugin.setMaxIdleTime(300);
    
    c3p0Plugin.setAcquireIncrement(5);
    
    return c3p0Plugin;
  }
  
  public static ActiveRecordPlugin createMainActiveRecordPlugin(String dataBaseKey, C3p0Plugin c3p0Plugin, boolean isLocal)
  {
    ActiveRecordPlugin arp = new ActiveRecordPlugin(dataBaseKey, c3p0Plugin);
    
    new SupAdminPlugin(arp);
    if (isLocal) {
      arp.setShowSql(true);
    }
    return arp;
  }
  
  public static ActiveRecordPlugin createActiveRecordPlugin(String dataBaseKey, C3p0Plugin c3p0Plugin, boolean isLocal)
  {
    ActiveRecordPlugin arp = new ActiveRecordPlugin(dataBaseKey, c3p0Plugin);
    
    new BasePlugin(arp);
    new SysPlugin(arp);
    new BoughtPlugin(arp);
    new StockPlugin(arp);
    new SellPlugin(arp);
    new ReportsPlugin(arp);
    new FinancePlugin(arp);
    new FZPlugin(arp);
    new AioerpOmPortPlugin(arp);
    if (isLocal) {
      arp.setShowSql(true);
    }
    return arp;
  }
  
  public static boolean dataBaseNameIsExist(String dataBaseName)
  {
    Record r = Db.use(AioConstants.CONFIG_NAME).findFirst("SELECT `SCHEMA_NAME` FROM `information_schema`.`SCHEMATA` where SCHEMA_NAME=?", new Object[] { dataBaseName });
    if (r == null) {
      return false;
    }
    return true;
  }
}
