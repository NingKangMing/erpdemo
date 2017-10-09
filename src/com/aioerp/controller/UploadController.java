package com.aioerp.controller;

import com.aioerp.common.AioConstants;
import com.aioerp.model.sys.AioerpFile;
import com.aioerp.util.IO;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadController
  extends BaseController
{
  public void basePrdUploadImg()
  {
    if (isPost())
    {
      String configName = getPara("loginConfigId");
      
      String projectPath = IO.getWebrootPath();
      HttpServletRequest request = getRequest();
      String fileName = "";
      String folder = "";
      Map<String, String> map = new HashMap();
      map.put("base", getAttr("base").toString());
      ServletFileUpload sfu = new ServletFileUpload(new DiskFileItemFactory());
      sfu.setHeaderEncoding("UTF-8");
      try
      {
        List<?> fileList = sfu.parseRequest(request);
        for (int i = 0; i < fileList.size(); i++)
        {
          String sourceName = "";
          String extName = "";
          String name = "";
          String savePath = "";
          FileItem fi = (FileItem)fileList.get(i);
          if (!fi.isFormField())
          {
            sourceName = fi.getName();
            if ((sourceName != null) && (!"".equals(sourceName.trim())))
            {
              if (sourceName.lastIndexOf(".") >= 0)
              {
                name = sourceName.substring(0, sourceName.lastIndexOf("."));
                
                extName = sourceName.substring(sourceName.lastIndexOf("."));
              }
              String saveFillName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + System.currentTimeMillis();
              
              fileName = saveFillName + extName;
              
              folder = projectPath + "upLoadImg" + File.separator + "base" + File.separator + configName + File.separator + "product";
              IO.existFolder(folder);
              savePath = "base/" + configName + "/product/" + fileName;
              File ff = new File(folder + File.separator + fileName);
              Record a = new Record();
              a.set("sourceName", name)
                .set("saveName", saveFillName)
                .set("fileType", extName)
                .set("savePath", savePath)
                .set("typeFile", Integer.valueOf(AioConstants.FILE_TYPE1))
                .set("tableId", Integer.valueOf(AioConstants.TABLE_1))
                .set("status", Integer.valueOf(AioConstants.STATUS_ENABLE))
                .set("updateTime", new Date());
              Db.use(configName).save("aioerp_file", a);
              fi.write(ff);
              map.put("recordId", a.get("id").toString());
              map.put("savePath", savePath);
            }
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        map.put("a", "err");
      }
      renderJson(map);
    }
    else
    {
      render("/WEB-INF/template/base/product/upload_img.html");
    }
  }
  
  public static String listID = "aa";
  
  public void toOrderFujianUpLoad()
  {
    String configName = loginConfigName();
    String ids = getPara(0, "");
    int tableId = getParaToInt("tableId", Integer.valueOf(0)).intValue();
    int action = getParaToInt("action", Integer.valueOf(AioConstants.IS_DRAFT)).intValue();
    int billId = getParaToInt("billId", Integer.valueOf(0)).intValue();
    int isDraft = getParaToInt("action", Integer.valueOf(1)).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    Map<String, Object> pageMap = AioerpFile.dao.getPageByRoot(configName, pageNum, numPerPage, tableId, action, billId, ids, listID);
    setAttr("pageMap", pageMap);
    setAttr("listID", listID);
    setAttr("tableId", Integer.valueOf(tableId));
    setAttr("isDraft", Integer.valueOf(isDraft));
    setAttr("billId", Integer.valueOf(billId));
    if (((List)pageMap.get("pageList")).size() > 0) {
      setAttr("ids", ids);
    } else {
      setAttr("ids", "");
    }
    render("/WEB-INF/template/common/orderFujian/fujianDialog.html");
  }
  
  @Before({Tx.class})
  public void orderFujianUpLoad()
    throws SQLException
  {
    if (isPost())
    {
      String configName = getPara("loginConfigId");
      String projectPath = IO.getWebrootPath();
      HttpServletRequest request = getRequest();
      String fileName = "";
      String folder = "";
      Map<String, Object> map = new HashMap();
      int tableId = Integer.valueOf(request.getParameter("tableId")).intValue();
      Integer billId = Integer.valueOf(request.getParameter("billId"));
      Integer isDraft = Integer.valueOf(request.getParameter("isDraft"));
      map.put("base", getAttr("base").toString());
      map.put("tableId", Integer.valueOf(tableId));
      ServletFileUpload sfu = new ServletFileUpload(new DiskFileItemFactory());
      sfu.setHeaderEncoding("UTF-8");
      try
      {
        List<?> fileList = sfu.parseRequest(request);
        for (int i = 0; i < fileList.size(); i++)
        {
          String sourceName = "";
          String extName = "";
          String name = "";
          String savePath = "";
          FileItem fi = (FileItem)fileList.get(i);
          if (!fi.isFormField())
          {
            sourceName = fi.getName();
            if ((sourceName != null) && (!"".equals(sourceName.trim())))
            {
              if (sourceName.lastIndexOf(".") >= 0)
              {
                name = sourceName.substring(0, sourceName.lastIndexOf("."));
                
                extName = sourceName.substring(sourceName.lastIndexOf("."));
              }
              String saveFillName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + System.currentTimeMillis();
              fileName = saveFillName + extName;
              
              folder = projectPath + "upLoadImg" + File.separator + "base" + File.separator + configName + File.separator + "order";
              IO.existFolder(folder);
              savePath = "base/" + configName + "/order/" + fileName;
              File ff = new File(folder + File.separator + fileName);
              AioerpFile a = new AioerpFile();
              a.set("sourceName", name)
                .set("saveName", saveFillName)
                .set("fileType", extName)
                .set("savePath", savePath)
                .set("tableId", Integer.valueOf(tableId))
                .set("recordId", billId)
                .set("isDraft", isDraft)
                .set("status", Integer.valueOf(AioConstants.STATUS_ENABLE))
                .set("updateTime", new Date())
                .save(configName);
              fi.write(ff);
              map.put("refrshDialogUrl", getAttr("base") + "/up");
              map.put("id", a.getInt("id"));
            }
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        commonRollBack();
        renderJson();
        return;
      }
      renderJson(map);
    }
    else
    {
      int tableId = getParaToInt(0, Integer.valueOf(0)).intValue();
      int billId = getParaToInt(1, Integer.valueOf(0)).intValue();
      int isDraft = getParaToInt(2, Integer.valueOf(0)).intValue();
      setAttr("tableId", Integer.valueOf(tableId));
      setAttr("billId", Integer.valueOf(billId));
      setAttr("isDraft", Integer.valueOf(isDraft));
      render("/WEB-INF/template/common/orderFujian/upload_img.html");
    }
  }
  
  @Before({Tx.class})
  public void orderFujianDown()
    throws FileNotFoundException
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    String type = getPara(1, "");
    String projectPath = IO.getWebrootPath();
    Record upload = Db.use(configName).findById("aioerp_file", Integer.valueOf(id));
    if (type.equals("validate"))
    {
      if (upload == null)
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "文件不存在！");
        renderJson(this.result);
        return;
      }
      File file = new File(projectPath + "upLoadImg" + File.separator + upload.getStr("savePath"));
      if (file.exists())
      {
        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "文件存在！");
        renderJson(this.result);
        return;
      }
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "文件不存在！");
      renderJson(this.result);
      return;
    }
    if (type.equals("down"))
    {
      File file = new File(projectPath + "upLoadImg" + File.separator + upload.getStr("savePath"));
      renderFile(file);
      return;
    }
  }
  
  public void toOrderFujianUpdateMemo()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    Record r = Db.use(configName).findById("aioerp_file", Integer.valueOf(id));
    setAttr("obj", r);
    render("/WEB-INF/template/common/orderFujian/memoDialog.html");
  }
  
  @Before({Tx.class})
  public void orderFujianUpdateMemo()
    throws SQLException
  {
    String configName = loginConfigName();
    try
    {
      int id = getParaToInt("id", Integer.valueOf(0)).intValue();
      String memo = getPara("memo", "");
      Record r = Db.use(configName).findById("aioerp_file", Integer.valueOf(id));
      if (r != null)
      {
        r.set("memo", memo);
        Db.use(configName).update("aioerp_file", r);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "操作失败！");
      renderJson(this.result);
      return;
    }
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("message", "操作成功！");
    this.result.put("callbackType", "closeCurrent");
    this.result.put("callbackType1", "refreshDialogByPara");
    this.result.put("dialogId", "order_fujianDialog_id");
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void orderFujianDel()
    throws SQLException
  {
    String configName = loginConfigName();
    try
    {
      int id = getParaToInt(0, Integer.valueOf(0)).intValue();
      Db.use(configName).deleteById("aioerp_file", Integer.valueOf(id));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "操作失败！");
      renderJson(this.result);
      return;
    }
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("message", "操作成功！");
    this.result.put("callbackType1", "refreshDialogByPara");
    this.result.put("dialogId", "order_fujianDialog_id");
    renderJson(this.result);
  }
}
