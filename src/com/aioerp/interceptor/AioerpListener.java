package com.aioerp.interceptor;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.sys.BackUpController;
import com.aioerp.util.DateUtils;
import com.aioerp.util.Dog;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class AioerpListener
{
  private static Map<String, String> BACK_TIME = new HashMap();
  private static Calendar calendar = Calendar.getInstance();
  
  public void loadListener()
  {
    try
    {
      Timer timer = new Timer();
      Date currentDate = new Date();
      
      String currentDateStr = DateUtils.format(currentDate, "HH:mm:ss");
      Record r = Db.use(AioConstants.CONFIG_NAME).findFirst("select * from aioerp_sys where key1='autoBackUpDateTime'");
      if (r == null)
      {
        r = new Record();
        r.set("key1", "autoBackUpDateTime");
        r.set("value1", currentDateStr);
        r.set("memo", "自动备份时间");
        Db.use(AioConstants.CONFIG_NAME).save("aioerp_sys", r);
      }
      else
      {
        r.set("value1", currentDateStr);
        Db.use(AioConstants.CONFIG_NAME).update("aioerp_sys", r);
      }
      calendar.setTime(currentDate);
      timer.schedule(new AutoBackupCheckTimerTask(), calendar.getTime(), AioConstants.GRAP_TIME);
      new Timer().schedule(new ClientDataTimerTask(), calendar.getTime(), AioConstants.GRAP_TIME);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public class AutoBackupCheckTimerTask
    extends TimerTask
  {
    public AutoBackupCheckTimerTask() {}
    
    public void run()
    {
      try
      {
        System.out.println("--------------------------数据库检测自动备份--------------------------------");
        Record record = Db.use(AioConstants.CONFIG_NAME).findFirst("select * from aioerp_sys where key1='autoBackUpDateTime'");
        if (record == null)
        {
          record = new Record();
          record.set("key1", "autoBackUpDateTime");
          record.set("value1", DateUtils.format(new Date(), "HH:mm:ss"));
          record.set("memo", "自动备份时间");
          Db.use(AioConstants.CONFIG_NAME).save("aioerp_sys", record);
        }
        String currentDateStr = record.getStr("value1");
        Date currentDate = DateUtils.parseDate(currentDateStr, "HH:mm:ss");
        Date grapDate = DateUtils.addMillisecond(currentDate, AioConstants.GRAP_TIME);
        String grapDateStr = DateUtils.format(grapDate, "HH:mm:ss");
        Date oldCurrentDate = DateUtils.parseDate(grapDateStr, "HH:mm:ss");
        List<Record> list = null;
        if (currentDate.before(oldCurrentDate)) {
          list = Db.find("select * from aioerp_sys_backup_plan where startTime >='" + currentDateStr + "' and startTime<'" + grapDateStr + "'");
        } else {
          list = Db.find("select * from aioerp_sys_backup_plan where startTime >='" + currentDateStr + "' or startTime<'" + grapDateStr + "'");
        }
        if ((list != null) && (list.size() > 0)) {
          for (int i = 0; i < list.size(); i++)
          {
            Record r = (Record)list.get(i);
            String startTimeStr = r.getStr("startTime");
            String newDateStr = DateUtils.format(new Date(), "yyyy-MM-dd");
            AioerpListener.calendar.setTime(DateUtils.parseDate(newDateStr + " " + startTimeStr, "yyyy-MM-dd HH:mm:ss"));
            AioerpListener.BACK_TIME.put(startTimeStr, startTimeStr);
            

            //new Timer().schedule(new AioerpListener.AutoBackupTimerTask(AioerpListener.this), AioerpListener.calendar.getTime());
          }
        }
        record.set("value1", grapDateStr);
        Db.use(AioConstants.CONFIG_NAME).update("aioerp_sys", record);
        System.out.println("------------------------end--数据库检测自动备份--------------------------------");
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
  
  public class AutoBackupTimerTask
    extends TimerTask
  {
    public AutoBackupTimerTask() {}
    
    public void run()
    {
      try
      {
        String curDateStr = DateUtils.format(new Date(), "HH:mm:ss");
        StringBuffer sql = new StringBuffer("select * from aioerp_sys_backup_plan where 1=1");
        boolean flag = false;
        for (Map.Entry<String, String> entry : AioerpListener.BACK_TIME.entrySet()) {
          if (AioerpListener.BACK_TIME.containsKey(curDateStr))
          {
            sql.append(" or startTime ='" + (String)AioerpListener.BACK_TIME.get(entry.getKey()) + "'");
            flag = true;
          }
        }
        Record r = null;
        if (flag) {
          r = Db.findFirst(sql.toString());
        }
        if (r != null)
        {
          AioerpListener.BACK_TIME.remove(r.get("startTime"));
          String databaseIds = r.getStr("databaseIds");
          String[] ids = databaseIds.split(",");
          for (int j = 0; j < ids.length; j++) {
            if (!ids[j].equals(""))
            {
              Record zt = Db.use(AioConstants.CONFIG_NAME).findById("aioerp_which_db", Integer.valueOf(ids[j].trim()));
              if (zt != null)
              {
                String strDate = DateUtils.format(new Date(), "yyyyMMddHHmmss");
                
                Map<String, Object> map = BackUpController.commonBackUp(ids[j], null, zt.getStr("whichDbName"), "自动备份" + zt.getStr("whichDbName") + strDate);
                String.valueOf(map.get("statusCode")).equals("200");
              }
            }
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
  
  public class ClientDataTimerTask
    extends TimerTask
  {
    public ClientDataTimerTask() {}
    
    public void run()
    {
      BigDecimal dataLength = BigDecimal.ZERO;
      long reportNum = 0L;
      long accountNum = 0L;
      try
      {
        Record anr = Db.use(AioConstants.CONFIG_NAME).findFirst("select count(*) as total from aioerp_which_db");
        accountNum = anr.getLong("total").longValue();
        List<Record> zts = Db.use(AioConstants.CONFIG_NAME).find("select * from aioerp_which_db where status=2");
        for (Record r : zts)
        {
          Record tr = Db.use(AioConstants.CONFIG_NAME).findFirst("SELECT sum(`DATA_LENGTH`) as total FROM `information_schema`.`TABLES` where TABLE_SCHEMA=?", new Object[] { r.get("dataBaseName") });
          dataLength = dataLength.add(tr.getBigDecimal("total"));
          Record rr = Db.use(r.get("id").toString()).findFirst("select count(*) as total from bb_billhistory");
          reportNum += rr.getLong("total").longValue();
        }
        Map<String, String> data = new HashMap();
        data.put("versionDetail", AioConstants.AIOERP_VERSION);
        data.put("dataLength", dataLength.toString());
       // data.put("reportNum", reportNum);
       // data.put("dogno", Dog.getID());
       // data.put("cardno", Dog.getCardNO());
       // data.put("userCount", Dog.getUserCount());
      //  data.put("accountNum", accountNum);
        data.put("version", "2");
        






        Document doc = Jsoup.connect("http://update.aioerp.com/soft-auth").data(data).userAgent("Mozilla").timeout(3000).post();
        JSONObject jsonobj = new JSONObject(doc.text());
        if (jsonobj.getInt("status") == 1) {
          AioConstants.IO_OVER = true;
        } else if (jsonobj.getInt("status") == 2) {
          AioConstants.IO_OVER = false;
        }
      }
      catch (Exception localException) {}
    }
  }
}
