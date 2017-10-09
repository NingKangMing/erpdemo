package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.db.reports.BillHistory;
import com.aioerp.model.supadmin.WhichDb;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.AioerpSysBackup;
import com.aioerp.model.sys.AioerpSysBackupAuto;
import com.aioerp.model.sys.BillCodeFlow;
import com.aioerp.util.IO;
import com.aioerp.util.MyObjectOutputStream;
import com.aioerp.util.dbsource.C3p0DateSourceUtil;
import com.aioerp.util.zip.ZipUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;

public class BackUpController
  extends BaseController
{
  public void index()
  {
    render("page.html");
  }
  
  public static Map<String, Object> commonBackUp(String configName, Integer userId, String whichDbName, String fileName)
    throws Exception
  {
    AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS1));
    Map<String, Object> map = new HashMap();
    map.put("statusCode", "200");
    map.put("message", "操作成功！");
    if (fileName.equals(""))
    {
      map.put("statusCode", "300");
      map.put("message", "文件名不能为空，备份失败！");
      AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
      return map;
    }
    if (AioerpSysBackup.dao.existObjectByFileName(fileName))
    {
      map.put("statusCode", "300");
      map.put("message", "该文件名称【" + fileName + "】已经存在，备份失败！");
      AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
      return map;
    }
    String projectPath = IO.getWebrootPath();
    String folder = "";
    if (AioConstants.BACKUP_BY_CMD) {
      folder = projectPath + "backup" + File.separator + fileName;
    } else {
      folder = projectPath + "backup";
    }
    IO.existFolder(folder);
    folder = folder + File.separator + fileName + ".bak";
    File f = new File(folder);
    boolean flag = true;
    

    Connection con = DbKit.getConfig(configName).getConnection();
    String dataBaseName = con.getCatalog();
    PrintWriter p = null;
    BufferedReader reader = null;
    try
    {
      if (AioConstants.BACKUP_BY_CMD)
      {
        String mysqlBinPath = AioerpSys.dao.getValue1(AioConstants.CONFIG_NAME, "getMysqlBinPath");
        String command = mysqlBinPath + File.separator + "mysqldump -u" + PropKit.get("user") + " -p" + PropKit.get("password") + " " + whichDbName + " -t --default-character-set=utf8";
        p = new PrintWriter(new OutputStreamWriter(new FileOutputStream(folder), "utf8"));
        Process process = Runtime.getRuntime().exec(command);
        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), "utf8");
        reader = new BufferedReader(inputStreamReader);
        String line = null;
        while ((line = reader.readLine()) != null) {
          p.println(line);
        }
        p.flush();
        if (reader != null) {
          reader.close();
        }
        if (p != null) {
          p.close();
        }
        ZipUtil.backUpImg(configName, fileName);
      }
      else
      {
        DatabaseMetaData databaseMetaData = con.getMetaData();
        ResultSet rs = null;
        Map<String, List<Record>> dataMap = new HashMap();
        rs = databaseMetaData.getTables("", "", "", null);
        OutputStream out = new FileOutputStream(f, true);
        MyObjectOutputStream os = MyObjectOutputStream.newInstance(f, out);
        while (rs.next())
        {
          String tableType = rs.getString("TABLE_TYPE");
          String tableNmae = rs.getString("TABLE_NAME");
          if ((tableType != null) && (tableType.equals("TABLE")))
          {
            List<Record> dataList = Db.use(configName).find("select * from " + tableNmae);
            dataMap.put(tableNmae, dataList);
            os.writeObject(dataMap);
            dataMap = new HashMap();
          }
        }
        os.close();
      }
    }
    catch (Exception e)
    {
      flag = false;
      e.printStackTrace();
      con.setAutoCommit(false);
      con.rollback();
      map.put("statusCode", "300");
      map.put("message", "备份失败！");
    }
    finally
    {
      if (AioConstants.BACKUP_BY_CMD) {
        try
        {
          if (reader != null) {
            reader.close();
          }
          if (p != null) {
            p.close();
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      DbKit.getConfig(configName).close(con);
      AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
    }
    if (flag)
    {
      Record curent = new Record();
      curent.set("dataBaseName", dataBaseName);
      curent.set("whichDbName", whichDbName);
      curent.set("file_name", fileName);
      curent.set("update_time", new Date());
      curent.set("userId", userId);
      flag = Db.use(AioConstants.CONFIG_NAME).save("aioerp_sys_backup", curent);
      if (!flag)
      {
        commonMainRollBack();
        map.put("statusCode", "300");
        map.put("message", "备份失败！");
        AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
        return map;
      }
    }
    return map;
  }
  
  public static Map<String, Object> commonRecover(String configName, String folder, String fileName)
    throws Exception
  {
    Map<String, Object> map = new HashMap();
    map.put("statusCode", Integer.valueOf(200));
    map.put("message", "操作成功！");
    boolean recover_status = true;
    
    Config config = DbKit.getConfig(configName);
    Connection con = config.getConnection();
    con.setAutoCommit(false);
    try
    {
      ComFunController.recoverByRecordObject(configName, config, con, folder + ".bak");
    }
    catch (Exception e)
    {
      if (AioConstants.BACKUP_BY_CMD)
      {
        ZipUtil.recoverImgBefore(configName, fileName);
        OutputStream out = null;
        BufferedReader br = null;
        OutputStreamWriter writer = null;
        try
        {
          String mysqlBinPath = AioerpSys.dao.getValue1(AioConstants.CONFIG_NAME, "getMysqlBinPath");
          DatabaseMetaData databaseMetaData = con.getMetaData();
          ResultSet rs = null;
          folder = folder + File.separator + fileName + ".bak";
          rs = databaseMetaData.getTables("", "", "", null);
          while (rs.next())
          {
            String tableType = rs.getString("TABLE_TYPE");
            String tableNmae = rs.getString("TABLE_NAME");
            if ((tableType != null) && (tableType.equals("TABLE"))) {
              Db.use(configName).update("truncate table " + tableNmae);
            }
          }
          Record r = Db.use(AioConstants.CONFIG_NAME).findById("aioerp_which_db", configName);
          String dbName = r.getStr("dataBaseName");
          String command = mysqlBinPath + File.separator + "mysql -u" + PropKit.get("user") + " -p" + PropKit.get("password") + " --default-character-set=utf8 " + dbName;
          Process process = Runtime.getRuntime().exec(command);
          out = process.getOutputStream();
          writer = new OutputStreamWriter(out, "utf-8");
          String inStr = "";
          String outStr = "";
          StringBuffer sb = new StringBuffer("");
          br = new BufferedReader(new InputStreamReader(new FileInputStream(folder), "utf-8"));
          int count = 0;
          while ((inStr = br.readLine()) != null)
          {
            sb.append(inStr + "\r\n");
            count++;
            if (count > 5)
            {
              outStr = sb.toString();
              writer.write(outStr);
              sb = new StringBuffer("");
            }
          }
          outStr = sb.toString();
          writer.write(outStr);
          writer.flush();
          
          ZipUtil.recoverImgAfter(configName, fileName);
        }
        catch (Exception e3)
        {
          e3.printStackTrace();
          recover_status = false;
        }
        finally
        {
          if (out != null) {
            out.close();
          }
          if (br != null) {
            br.close();
          }
          if (writer != null) {
            writer.close();
          }
        }
      }
      else
      {
        recover_status = false;
      }
    }
    finally
    {
      if (con != null)
      {
        con.commit();
        con.setAutoCommit(false);
      }
    }
    label852:
    try
    {
      if (recover_status)
      {
        Record aioerpSys = AioerpSys.dao.getObj(configName, "lastDBUpdateTime");
        if (aioerpSys != null)
        {
          Integer clientLastUpdateTime = Integer.valueOf(aioerpSys.getStr("value1"));
          if (Integer.valueOf("20150611").intValue() <= clientLastUpdateTime.intValue()) {
            break label852;
          }
          if (AioConstants.PROJECT_IS_OK)
          {
            List<Model> whichDbs = new ArrayList();
            whichDbs.add(WhichDb.dao.findById(AioConstants.CONFIG_NAME, Integer.valueOf(configName)));
            
            whichDbsStructureAndDataChange(whichDbs, false, clientLastUpdateTime.intValue(), Integer.valueOf("20150611").intValue());
          }
        }
      }
    }
    catch (Exception e)
    {
      recover_status = false;
    }
    finally
    {
      DbKit.getConfig(configName).close(con);
    }
    if (!recover_status)
    {
      map.put("statusCode", Integer.valueOf(300));
      map.put("message", "操作失败！");
    }
    int billCount = BillHistory.dao.getStartToEndDateHistoryBillCount(configName, null, null);
    AioConstants.accountBillCount.put(configName, Integer.valueOf(billCount));
    
    Map<String, Map<String, Record>> paraMap = (Map)AioConstants.SYS_PARAM.get(configName);
    if ((paraMap != null) && (paraMap.size() > 0)) {
      AioConstants.SYS_PARAM.put(configName, null);
    }
    return map;
  }
  
  public void toBackup()
  {
    render("/WEB-INF/template/sys/backup/backupDialog.html");
  }
  
  @Before({Tx.class})
  public void backup()
    throws Exception
  {
    String configName = loginConfigName();
    String fileName = getPara("fileName", "");
    renderJson(commonBackUp(configName, Integer.valueOf(loginUserId()), loginAccountName(), fileName));
  }
  
  public String listID = "sys_backup_table";
  
  public void toRecover()
  {
    Map<String, Object> pageMap = AioerpSysBackup.dao.getPageByRoot(AioConstants.START_PAGE, 10, this.listID);
    setAttr("pageMap", pageMap);
    setAttr("listID", this.listID);
    render("/WEB-INF/template/sys/backup/recoverDialog.html");
  }
  
  @Before({Tx.class})
  public void recover()
    throws SQLException
  {
    String configName = loginConfigName();
    AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS2));
    try
    {
      String fileName = getPara("fileName", "");
      
      String projectPath = IO.getWebrootPath();
      String folder = projectPath + "backup" + File.separator + fileName;
      

      boolean flag = true;
      File hasFile = new File(folder + ".bak");
      if (!hasFile.exists()) {
        flag = false;
      }
      if (!flag)
      {
        hasFile = new File(folder + ".zip");
        if (hasFile.exists()) {
          flag = true;
        }
      }
      if (!flag)
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该文件不存在，请重新选择！");
        renderJson(this.result);
        return;
      }
      this.result = commonRecover(configName, folder, fileName);
      this.result.put("callbackType", "closeCurrent");
      

      AioConstants.CONFIGSESSIONNAMELIST.add(configName);
      getSession().invalidate();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack(configName);
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "操作失败！");
    }
    finally
    {
      AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
    }
    renderJson(this.result);
  }
  
  public void recoverPage()
  {
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    Map<String, Object> pageMap = AioerpSysBackup.dao.getPageByRoot(pageNum, numPerPage, this.listID);
    setAttr("pageMap", pageMap);
    setAttr("listID", this.listID);
    render("/WEB-INF/template/sys/backup/recoverDialogList.html");
  }
  
  @Before({Tx.class})
  public void recoverDel()
    throws SQLException
  {
    try
    {
      String fileName = getPara("fullName", "");
      Db.use(AioConstants.CONFIG_NAME).update("delete from aioerp_sys_backup where file_name=?", new Object[] { fileName });
      
      String projectPath = IO.getWebrootPath();
      String folder = projectPath + "backup" + File.separator + fileName + ".zip";
      File f = new File(folder);
      if (f.exists()) {
        IO.deleteFile(f);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "操作失败！");
      renderJson(this.result);
      return;
    }
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("message", "操作成功！");
    this.result.put("callbackType", "refreshCurrent");
    renderJson(this.result);
  }
  
  public void toMainBackup()
  {
    List<Record> list = AioerpSysBackup.dao.getAllDataBase();
    if (list == null) {
      list = new ArrayList();
    }
    setAttr("list", list);
    render("/WEB-INF/template/sys/backup/main/backupDialog.html");
  }
  
  @Before({Tx.class})
  public void mainBackup()
    throws Exception
  {
    int dataBaseNameId = getParaToInt("dataBaseNameId", Integer.valueOf(0)).intValue();
    Record r = Db.use(AioConstants.CONFIG_NAME).findById("aioerp_which_db", Integer.valueOf(dataBaseNameId));
    String configName = String.valueOf(r.getInt("id"));
    String fileName = getPara("fileName", "");
    renderJson(commonBackUp(configName, Integer.valueOf(loginUserId()), r.getStr("dataBaseName"), fileName));
  }
  
  public void toMainRecover()
  {
    List<Record> list = AioerpSysBackup.dao.getAllDataBase();
    if (list == null) {
      list = new ArrayList();
    }
    setAttr("list", list);
    
    Map<String, Object> pageMap = AioerpSysBackup.dao.getPageByRoot(AioConstants.START_PAGE, 10, this.listID);
    setAttr("pageMap", pageMap);
    setAttr("listID", this.listID);
    render("/WEB-INF/template/sys/backup/main/recoverDialog.html");
  }
  
  @Before({Tx.class})
  public void mainRecover()
    throws SQLException
  {
    int dataBaseNameId = getParaToInt("dataBaseNameId", Integer.valueOf(0)).intValue();
    String configName = String.valueOf(dataBaseNameId);
    AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS2));
    try
    {
      String fileName = getPara("fileName", "");
      
      String projectPath = IO.getWebrootPath();
      String folder = projectPath + "backup" + File.separator + fileName;
      

      boolean flag = true;
      File hasFile = new File(folder + ".bak");
      if (!hasFile.exists()) {
        flag = false;
      }
      if (!flag)
      {
        hasFile = new File(folder + ".zip");
        if (hasFile.exists()) {
          flag = true;
        }
      }
      if (!flag)
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该文件不存在，请重新选择！");
        renderJson(this.result);
        return;
      }
      this.result = commonRecover(configName, folder, fileName);
      
      this.result.put("callbackType", "closeCurrent");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack(configName);
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "操作失败！");
    }
    finally
    {
      AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
    }
    renderJson(this.result);
  }
  
  public void toAutoBackup()
  {
    List<Record> list = AioerpSysBackup.dao.getAllBackupPlan();
    if (list == null) {
      list = new ArrayList();
    }
    setAttr("list", list);
    setAttr("listID", this.listID);
    render("/WEB-INF/template/sys/backup/auto/autoBackupDialog.html");
  }
  
  public void toAutoBackupPlan()
  {
    List<Record> list = AioerpSysBackup.dao.getAllDataBase();
    if (list == null) {
      list = new ArrayList();
    }
    setAttr("list", list);
    setAttr("listID", this.listID);
    
    Date date = new Date();
    setAttr("date", date);
    render("/WEB-INF/template/sys/backup/auto/planDialog.html");
  }
  
  public void lookAutoBackupPlan()
  {
    String configName = loginConfigName();
    List<Record> list = AioerpSysBackup.dao.getAllDataBase();
    if (list == null) {
      list = new ArrayList();
    }
    setAttr("list", list);
    setAttr("listID", this.listID);
    
    int databaseId = getParaToInt(0, Integer.valueOf(0)).intValue();
    Model model = AioerpSysBackupAuto.dao.findById(configName, Integer.valueOf(databaseId));
    setAttr("plan", model);
    
    render("/WEB-INF/template/sys/backup/auto/planDialog.html");
  }
  
  @Before({Tx.class})
  public void autoBackup()
  {
    String planName = getPara("planName", "");
    String autoBackupTime = getPara("autoBackupTime", "");
    String[] ids = getParaValues("ids");
    if (planName.equals(""))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "请填写备份计划名称！");
      renderJson(this.result);
      return;
    }
    if ((ids == null) || (ids.equals("")))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "请选择要备份的数据库！");
      renderJson(this.result);
      return;
    }
    String idsStr = "";
    for (int i = 0; i < ids.length; i++) {
      idsStr = idsStr + "," + ids[i];
    }
    idsStr = idsStr + ",";
    
    Record r = new Record();
    r.set("name", planName);
    r.set("databaseIds", idsStr);
    r.set("startTime", autoBackupTime);
    r.set("update_time", new Date());
    Db.use(AioConstants.CONFIG_NAME).save("aioerp_sys_backup_plan", r);
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("message", "操作成功！");
    this.result.put("callbackType", "closeCurrent");
    this.result.put("callbackType1", "closeCurentRefreshNext");
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void delBackupPlan()
    throws SQLException
  {
    String configName = loginConfigName();
    try
    {
      int id = getParaToInt(0, Integer.valueOf(0)).intValue();
      AioerpSysBackupAuto.dao.deleteById(configName, Integer.valueOf(id));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "操作失败！");
      renderJson(this.result);
      return;
    }
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("message", "操作成功！");
    this.result.put("callbackType", "refreshCurrent");
    renderJson(this.result);
  }
  
  public static void whichDbsStructureAndDataChange(List<Model> whichDbs, boolean isStructureUp, int startDate, int endDate)
    throws Exception
  {
    boolean flag = true;
    if (isStructureUp)
    {
      String childUpdateTableSqlPath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "updateTable_log.sql";
      String updateChildTablePathTemp = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "updateTableTmp.sql";
      IO.getLogHistoryBySameDate(childUpdateTableSqlPath, updateChildTablePathTemp, startDate, endDate);
      for (Model m : whichDbs)
      {
        flag = C3p0DateSourceUtil.initOrUpdateWhichDbData(m.getStr("dataBaseName"), updateChildTablePathTemp);
        if (!flag) {
          throw new Exception();
        }
      }
      IO.deleteFile(new File(updateChildTablePathTemp));
    }
    String childUpdateDataSqlPath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "updateData_log.sql";
    String updateChildDataPathTemp = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "updateDataTmp.sql";
    IO.getLogHistoryBySameDate(childUpdateDataSqlPath, updateChildDataPathTemp, startDate, endDate);
    for (Model m : whichDbs)
    {
      flag = C3p0DateSourceUtil.initOrUpdateWhichDbData(m.getStr("dataBaseName"), updateChildDataPathTemp);
      if (!flag) {
        throw new Exception();
      }
    }
    IO.deleteFile(new File(updateChildDataPathTemp));
  }
  
  public static void whichDbsUpChangeParams(List<Model> whichDbs, Integer endDate)
  {
    for (int i = 0; i < whichDbs.size(); i++)
    {
      String configName = String.valueOf(((Model)whichDbs.get(i)).getInt("id"));
      
      AioerpSys.dao.getObj(configName, "lastDBUpdateTime");
      AioerpSys.dao.sysSaveOrUpdate(configName, "lastDBUpdateTime", endDate.toString(), null);
      

      AioConstants.WHICHDBID_STATUS.put(configName, ((Model)whichDbs.get(i)).getInt("status"));
      
      Record reloadBillCode = AioerpSys.dao.getObj(configName, "reloadBillCode");
      if ((reloadBillCode == null) || (reloadBillCode.getStr("value1").equals("0")))
      {
        String codeIncreaseRule = AioerpSys.dao.getValue1(configName, "codeIncreaseRule");
        BillCodeFlow.dao.batchUpdateZCodeNum(configName, codeIncreaseRule);
      }
      AioerpSys.dao.sysSaveOrUpdate(configName, "reloadBillCode", "1", "重新生成单据流水号记录表     0未   1生成过");
    }
  }
}
