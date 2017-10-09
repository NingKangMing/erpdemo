package com.aioerp.common;

import cn.dreampie.shiro.freemarker.ShiroTags;
import com.aioerp.common.route.BaseRoute;
import com.aioerp.common.route.BoughtRoute;
import com.aioerp.common.route.CommonRoute;
import com.aioerp.common.route.FZRoute;
import com.aioerp.common.route.FinanceRoute;
import com.aioerp.common.route.InitRoute;
import com.aioerp.common.route.ReportsRoute;
import com.aioerp.common.route.SellRoute;
import com.aioerp.common.route.StockRoute;
import com.aioerp.common.route.SupAdminRoute;
import com.aioerp.common.route.SysRoute;
import com.aioerp.common.route.port.AioerpOmPortRoute;
import com.aioerp.common.route.reports.BoughtReportsRoute;
import com.aioerp.common.route.reports.FinanceReportsRoute;
import com.aioerp.common.route.reports.SellReportsRoute;
import com.aioerp.common.route.reports.StockReportsRoute;
import com.aioerp.controller.IndexController;
import com.aioerp.controller.UploadController;
import com.aioerp.controller.sys.BackUpController;
import com.aioerp.interceptor.AioerpListener;
import com.aioerp.interceptor.GlobalInterceptor;
import com.aioerp.interceptor.LoginInterceptor;
import com.aioerp.interceptor.OpenAccountInterceptor;
import com.aioerp.model.supadmin.WhichDb;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.Permission;
import com.aioerp.util.IO;
import com.aioerp.util.dbsource.C3p0DateSourceUtil;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.ext.plugin.shiro.ShiroPlugin;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.render.FreeMarkerRender;
import com.jfinal.render.ViewType;
import freemarker.template.Configuration;
import java.io.File;
import java.util.List;
import java.util.Map;

public class Config
  extends JFinalConfig
{
  private boolean isLocal = false;
  private static Routes routes;
  
  public void configConstant(Constants me)
  {
    loadPropertyFile(AioConstants.SETFILEPATH);
    this.isLocal = getPropertyToBoolean("devMode", Boolean.valueOf(false)).booleanValue();
    me.setDevMode(true);
    me.setViewType(ViewType.FREE_MARKER);
    me.setBaseViewPath("/WEB-INF/template");
    me.setError404View("/");
  }
  
  public void configRoute(Routes me)
  {
    me.add("/", IndexController.class);
    me.add("/upload", UploadController.class);
    me.add(new InitRoute());
    me.add(new BaseRoute());
    me.add(new BoughtRoute());
    me.add(new StockRoute());
    me.add(new SellRoute());
    me.add(new FinanceRoute());
    me.add(new ReportsRoute());
    me.add(new SysRoute());
    me.add(new CommonRoute());
    me.add(new FZRoute());
    
    me.add(new SellReportsRoute());
    me.add(new BoughtReportsRoute());
    me.add(new StockReportsRoute());
    me.add(new FinanceReportsRoute());
    
    me.add(new SupAdminRoute());
    me.add(new AioerpOmPortRoute());
    routes = me;
  }
  
  public void configPlugin(Plugins me)
  {
    C3p0Plugin c3p0Plugin = C3p0DateSourceUtil.createC3p0Plugin(PropKit.get("jdbcDbName"));
    me.add(c3p0Plugin);
    ActiveRecordPlugin arp = C3p0DateSourceUtil.createMainActiveRecordPlugin(AioConstants.CONFIG_NAME, c3p0Plugin, this.isLocal);
    me.add(arp);
    
    me.add(new EhCachePlugin());
    me.add(new ShiroPlugin(routes));
  }
  
  public void configInterceptor(Interceptors me)
  {
    me.add(new GlobalInterceptor());
    me.add(new LoginInterceptor());
    me.add(new OpenAccountInterceptor());
  }
  
  public void configHandler(Handlers me)
  {
    me.add(new ContextPathHandler("base"));
    me.add(new VersionHandler());
  }
  
  public void afterJFinalStart()
  {
    super.afterJFinalStart();
    try
    {
      setVersionInfo();
      
      List<Model> whichDbs = WhichDb.dao.getList();
      Integer lastDBUpdateTime = Integer.valueOf("20150611");
      
      Record aioerpSys = AioerpSys.dao.getObj(AioConstants.CONFIG_NAME, "lastDBUpdateTime");
      if (aioerpSys == null) {
        AioerpSys.dao.sysSaveOrUpdate(AioConstants.CONFIG_NAME, "lastDBUpdateTime", String.valueOf(lastDBUpdateTime), "客户最后一次更新日期");
      }
      if (AioConstants.PROJECT_IS_OK) {
        try
        {
          for (Model m : whichDbs)
          {
            String version = m.getStr("aioerpVersion");
            if (!AioConstants.AIOERP_VERSION.equals(version)) {
              m.set("aioerpVersion", AioConstants.AIOERP_VERSION).update(AioConstants.CONFIG_NAME);
            }
          }
          Integer clientLastUpdateTime = Integer.valueOf(Integer.parseInt(aioerpSys.getStr("value1")));
          if (lastDBUpdateTime.intValue() > clientLastUpdateTime.intValue())
          {
            String mainUpdateSqlPath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "mainSql" + File.separator + "mianDatabase_sql_log.sql";
            String updateMainDataPathTemp = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "mainSql" + File.separator + "updateLog.sql";
            IO.getLogHistoryBySameDate(mainUpdateSqlPath, updateMainDataPathTemp, clientLastUpdateTime.intValue(), lastDBUpdateTime.intValue());
            boolean flag = C3p0DateSourceUtil.initOrUpdateWhichDbData("aioerp", updateMainDataPathTemp);
            if (!flag) {
              throw new Exception();
            }
            IO.deleteFile(new File(updateMainDataPathTemp));
            
            BackUpController.whichDbsStructureAndDataChange(whichDbs, true, clientLastUpdateTime.intValue(), lastDBUpdateTime.intValue());
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      if ((whichDbs != null) && (whichDbs.size() > 0)) {
        for (int i = 0; i < whichDbs.size(); i++)
        {
          Model r = (Model)whichDbs.get(i);
          C3p0Plugin c3p0Plugin = C3p0DateSourceUtil.createC3p0Plugin(r.getStr("dataBaseName"));
          c3p0Plugin.start();
          ActiveRecordPlugin arp = C3p0DateSourceUtil.createActiveRecordPlugin(r.getInt("id").toString(), c3p0Plugin, this.isLocal);
          arp.start();
          

          String configName = String.valueOf(r.getInt("id"));
          
          AioConstants.PROJECT_STATUS_MAP.put(configName, Integer.valueOf(AioConstants.PROJECT_STATUS0));
          
          AioConstants.WHICHDBID_STATUS.put(configName, r.getInt("status"));
        }
      }
      AioConstants.PROJECT_STATUS_MAP.put(AioConstants.CONFIG_NAME, Integer.valueOf(AioConstants.PROJECT_STATUS0));
      

      AioerpSys.dao.sysSaveOrUpdate(AioConstants.CONFIG_NAME, "lastDBUpdateTime", String.valueOf(lastDBUpdateTime), null);
      



      BackUpController.whichDbsUpChangeParams(whichDbs, lastDBUpdateTime);
      


      new AioerpListener().loadListener();
      
      FreeMarkerRender.getConfiguration().setSharedVariable("shiro", new ShiroTags());
      AioConstants.permissionList = Permission.dao.getList(AioConstants.CONFIG_NAME);
      if ((whichDbs != null) && (whichDbs.size() > 0)) {
        for (int i = 0; i < whichDbs.size(); i++)
        {
          String configName = String.valueOf(((Model)whichDbs.get(i)).getInt("id"));
          PatchProgram.allPatch(configName);
          if (AioConstants.IS_FREE_VERSION == "yes") {
            Db.use(configName).update("update aioerp_sys_config set isAllow=0 where id in(4,5)");
          }
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void setVersionInfo()
  {
    int v = 2;
    if (v == 1)
    {
      AioConstants.VERSION = 1;
      AioConstants.IS_FREE_VERSION = "no";
      AioConstants.LOG_VERSION = "基础版";
      AioConstants.AIOERP_VERSION = "基础版" + AioConstants.AIOERP_VERSION;
    }
    else if (v == 2)
    {
      AioConstants.VERSION = 2;
      AioConstants.IS_FREE_VERSION = "no";
      AioConstants.LOG_VERSION = "标准版";
      AioConstants.AIOERP_VERSION = "标准版" + AioConstants.AIOERP_VERSION;
    }
    else if (v == 3)
    {
      AioConstants.VERSION = 2;
      AioConstants.IS_FREE_VERSION = "yes";
      AioConstants.LOG_VERSION = "免费版";
      AioConstants.AIOERP_VERSION = "免费版" + AioConstants.AIOERP_VERSION;
    }
  }
}
