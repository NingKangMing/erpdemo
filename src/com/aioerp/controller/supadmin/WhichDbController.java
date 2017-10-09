package com.aioerp.controller.supadmin;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.supadmin.WhichDb;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.User;
import com.aioerp.util.MD5Util;
import com.aioerp.util.dbsource.C3p0DateSourceUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WhichDbController
  extends BaseController
{
  public void toList()
  {
    List<Model> list = WhichDb.dao.getList();
    setAttr("objectId", getParaToInt("selectedObjectId", Integer.valueOf(0)));
    setAttr("list", list);
    render("/WEB-INF/template/supadmin/whichDb/dialog.html");
  }
  
  public void toAdd()
    throws ParseException
  {
    render("/WEB-INF/template/supadmin/whichDb/addDialog.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    

    Map<String, Object> vesionMap = ComFunController.vaildateOneWhichDb();
    if (Integer.valueOf(vesionMap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(vesionMap);
      return;
    }
    WhichDb model = (WhichDb)getModel(WhichDb.class);
    String dataBaseName = model.getStr("dataBaseName");
    if (C3p0DateSourceUtil.dataBaseNameIsExist(dataBaseName))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "数据库名称已经在数据库存在！");
      renderJson(this.result);
      return;
    }
    if (WhichDb.dao.existWhiceObject(AioConstants.CONFIG_NAME, 0, dataBaseName, model.getStr("whichDbCode"), model.getStr("whichDbName")))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "数据库名称   账套编号   账套名称 任何一个都不能与列表中值相同！");
      renderJson(this.result);
      return;
    }
    model.set("aioerpVersion", AioConstants.AIOERP_VERSION);
    model.set("create_time", new Date());
    model.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
    boolean flag = model.save(AioConstants.CONFIG_NAME);
    model.set("rank", model.getInt("id")).update(configName);
    setAttr("objectId", model.getInt("id"));
    if (!flag)
    {
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "系统异常,请联系管理员！");
      renderJson(this.result);
      return;
    }
    flag = C3p0DateSourceUtil.createWhichDbStructure(dataBaseName);
    if (!flag)
    {
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "系统异常,请联系管理员！");
      renderJson(this.result);
      return;
    }
    String initDataPath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "initData.sql";
    flag = C3p0DateSourceUtil.initOrUpdateWhichDbData(dataBaseName, initDataPath);
    if (!flag)
    {
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "数据库初始化失败,请联系管理员！");
      renderJson(this.result);
      return;
    }
    String updateTablePath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "updateTable_log.sql";
    flag = C3p0DateSourceUtil.initOrUpdateWhichDbData(dataBaseName, updateTablePath);
    if (!flag)
    {
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "数据库结构更新失败,请联系管理员！");
      renderJson(this.result);
      return;
    }
    String updateDataPath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "updateData_log.sql";
    flag = C3p0DateSourceUtil.initOrUpdateWhichDbData(dataBaseName, updateDataPath);
    if (!flag)
    {
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "系统异常,请联系管理员！");
      renderJson(this.result);
      return;
    }
    if (AioConstants.CREATE_PROC)
    {
      String createProcPath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "procedure" + File.separator;
      try
      {
        File file = new File(createProcPath);
        File[] fileList = file.listFiles();
        for (File tmpFile : fileList) {
          if (tmpFile.isFile()) {
            C3p0DateSourceUtil.initOrUpdateWhichDbData(dataBaseName, tmpFile.getAbsolutePath());
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "系统异常,请联系管理员！");
        renderJson(this.result);
        return;
      }
    }
    Config conf = DbKit.getConfig(model.getInt("id").toString());
    if (conf == null)
    {
      C3p0Plugin c3p0Plugin = C3p0DateSourceUtil.createC3p0Plugin(model.getStr("dataBaseName"));
      c3p0Plugin.start();
      ActiveRecordPlugin arp = C3p0DateSourceUtil.createActiveRecordPlugin(model.getInt("id").toString(), c3p0Plugin, PropKit.getBoolean("devMode").booleanValue());
      arp.start();
    }
    AioConstants.PROJECT_STATUS_MAP.put(model.getInt("id").toString(), Integer.valueOf(AioConstants.PROJECT_STATUS0));
    

    Record adminUser = new Record();
    adminUser.set("username", AioConstants.LOGIN_USERNAME);
    adminUser.set("password", MD5Util.string2MD5(AioConstants.LOGIN_PWD));
    adminUser.set("grade", Integer.valueOf(2));
    adminUser.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
    adminUser.set("privs", "*");
    adminUser.set("productPrivs", "*");
    adminUser.set("unitPrivs", "*");
    adminUser.set("storagePrivs", "*");
    adminUser.set("accountPrivs", "*");
    adminUser.set("areaPrivs", "*");
    adminUser.set("staffPrivs", "*");
    adminUser.set("departmentPrivs", "*");
    Db.use(String.valueOf(model.getInt("id"))).save("sys_user", adminUser);
    


    AioerpSys aioerpSys = new AioerpSys();
    aioerpSys.set("key1", "lastDBUpdateTime");
    aioerpSys.set("value1", "20150611");
    aioerpSys.set("memo", "").save(String.valueOf(model.getInt("id")));
    

    AioConstants.WHICHDBID_STATUS.put(model.getInt("id").toString(), model.getInt("status"));
    
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("message", "操作成功！");
    this.result.put("callbackType", "closeCurrent");
    this.result.put("callbackType1", "refreshDialogByPara");
    this.result.put("selectedObjectId", model.getInt("id"));
    renderJson(this.result);
  }
  
  public void toEdit()
  {
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    Model obj = WhichDb.dao.findById(AioConstants.CONFIG_NAME, Integer.valueOf(id));
    setAttr("obj", obj);
    render("/WEB-INF/template/supadmin/whichDb/editDialog.html");
  }
  
  @Before({Tx.class})
  public void edit()
    throws SQLException
  {
    try
    {
      WhichDb model = (WhichDb)getModel(WhichDb.class);
      if (WhichDb.dao.existWhiceObject(AioConstants.CONFIG_NAME, model.getInt("id").intValue(), model.getStr("dataBaseName"), model.getStr("whichDbCode"), model.getStr("whichDbName")))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "数据库名称   账套编号   账套名称  都不能相！");
        renderJson(this.result);
        return;
      }
      model.update(AioConstants.CONFIG_NAME);
      this.result.put("selectedObjectId", model.getInt("id"));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "系统异常,请联系管理员！");
      renderJson(this.result);
      return;
    }
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("message", "操作成功！");
    this.result.put("callbackType", "closeCurrent");
    this.result.put("callbackType1", "refreshDialogByPara");
    
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void del()
    throws SQLException
  {
    try
    {
      int id = getParaToInt(0, Integer.valueOf(0)).intValue();
      WhichDb.dao.deleteById(AioConstants.CONFIG_NAME, Integer.valueOf(id));
      

      AioConstants.WHICHDBID_STATUS.remove(String.valueOf(id));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "系统异常,请联系管理员！");
      renderJson(this.result);
      return;
    }
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("callbackType", "refreshCurrent");
    this.result.put("message", "操作成功！");
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void enable()
    throws SQLException
  {
    try
    {
      int id = getParaToInt(0, Integer.valueOf(0)).intValue();
      Model m = WhichDb.dao.findById(AioConstants.CONFIG_NAME, Integer.valueOf(id));
      if (m == null)
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该对象已不存在！");
        renderJson(this.result);
        return;
      }
      int status = AioConstants.STATUS_DISABLE;
      if (m.getInt("status").intValue() == AioConstants.STATUS_DISABLE)
      {
        int maxAccountSet = WhichDb.dao.maxAccountSet();
        if (maxAccountSet >= AioConstants.TOTAL_ACCOUNTSET)
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "超出最大激活账套个数" + AioConstants.TOTAL_ACCOUNTSET + "!");
          renderJson(this.result);
          return;
        }
        status = AioConstants.STATUS_ENABLE;
      }
      this.result.put("selectedObjectId", m.getInt("id"));
      WhichDb.dao.updateToDisableOrEnable(status, id);
      
      Config conf = DbKit.getConfig(m.getInt("id").toString());
      if (conf == null)
      {
        C3p0Plugin c3p0Plugin = C3p0DateSourceUtil.createC3p0Plugin(m.getStr("dataBaseName"));
        c3p0Plugin.start();
        ActiveRecordPlugin arp = C3p0DateSourceUtil.createActiveRecordPlugin(m.getInt("id").toString(), c3p0Plugin, PropKit.getBoolean("devMode").booleanValue());
        arp.start();
      }
      String configName = m.getInt("id").toString();
      
      AioConstants.WHICHDBID_STATUS.put(configName, Integer.valueOf(status));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "系统异常,请联系管理员！");
      renderJson(this.result);
      return;
    }
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("callbackType", "refreshCurrent");
    this.result.put("callbackType1", "refreshDialogByPara");
    this.result.put("message", "操作成功！");
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void reloadPwd()
    throws SQLException
  {
    try
    {
      int id = getParaToInt(0, Integer.valueOf(0)).intValue();
      Model m = WhichDb.dao.findById(AioConstants.CONFIG_NAME, Integer.valueOf(id));
      if (m == null)
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该对象已不存在！");
        renderJson(this.result);
        return;
      }
      String configName = m.getInt("id").toString();
      User.dao.reloadAdminPwd(configName);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "系统异常,请联系管理员！");
      renderJson(this.result);
      return;
    }
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("callbackType", "refreshCurrent");
    this.result.put("callbackType1", "refreshDialogByPara");
    this.result.put("message", "操作成功！");
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void hasOm()
    throws SQLException
  {
    try
    {
      int id = getParaToInt(0, Integer.valueOf(0)).intValue();
      Model m = WhichDb.dao.findById(AioConstants.CONFIG_NAME, Integer.valueOf(id));
      if (m == null)
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该对象已不存在！");
        renderJson(this.result);
        return;
      }
      this.result.put("selectedObjectId", m.getInt("id"));
      WhichDb.dao.updateToHasOm(id);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonMainRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "系统异常,请联系管理员！");
      renderJson(this.result);
      return;
    }
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("callbackType", "refreshCurrent");
    this.result.put("callbackType1", "refreshDialogByPara");
    this.result.put("message", "操作成功！");
    renderJson(this.result);
  }
  
  public void saveRank()
  {
    String configName = loginConfigName();
    String id = getPara("ids");
    if (!id.equals(""))
    {
      String seq = getPara("orders");
      String[] ids = id.split(",");
      String[] seqs = seq.split(",");
      for (int i = 0; i < ids.length; i++) {
        WhichDb.dao.findById(AioConstants.CONFIG_NAME, ids[i]).set("rank", seqs[i]).update(configName);
      }
    }
    returnToPage("", "", "", "", "", "");
    renderJson();
  }
}
