package com.aioerp.controller.base;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Staff;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.sys.AioerpFile;
import com.aioerp.model.sys.User;
import com.aioerp.util.IO;
import com.aioerp.util.NumberUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class StaffController
  extends BaseController
{
  public void tree()
  {
    String configName = loginConfigName();
    
    String staffPrivs = basePrivs(STAFF_PRIVS);
    List<Model> staffs = Staff.dao.getParent(configName, staffPrivs);
    Iterator<Model> iter = staffs.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model<Staff> staff = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", staff.get("id"));
      node.put("pId", staff.get("supId"));
      node.put("name", staff.get("name"));
      node.put("url", "base/staff/child/" + staff.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "职员信息");
    node.put("url", "base/staff/child/0");
    nodeList.add(node);
    
    renderJson(nodeList);
  }
  
  public void child()
  {
    String configName = loginConfigName();
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    int pSupId = Staff.dao.getPsupId(configName, supId);
    
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(upObjectId)).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    String staffPrivs = basePrivs(STAFF_PRIVS);
    int pageSize = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String showLastPage = getPara("showLastPage");
    if (upObjectId > 0) {
      setAttr("pageMap", Staff.dao.getSupPage(configName, pageNum, pageSize, supId, upObjectId, "staffList", null, orderField, orderDirection, staffPrivs));
    } else if ((showLastPage != null) && (showLastPage.equals("true"))) {
      setAttr("pageMap", Staff.dao.getLastPage(configName, pageNum, pageSize, supId, "staffList", orderField, orderDirection, staffPrivs));
    } else {
      setAttr("pageMap", Staff.dao.getChildPage(configName, pageNum, pageSize, supId, "staffList", "", orderField, orderDirection, null, staffPrivs));
    }
    Model sup = Staff.dao.findById(configName, Integer.valueOf(supId));
    if ((sup == null) || (sup.getInt("status").intValue() == AioConstants.STATUS_ENABLE)) {
      setAttr("showStatus", Boolean.valueOf(true));
    }
    setAttr("toAction", "child");
    setAttr("rowList", ReportRow.dao.getListByType(configName, "b500", AioConstants.STATUS_ENABLE));
    
    String whichRefresh = getPara("whichRefresh", "");
    if (whichRefresh.equals("navTabRefresh")) {
      render("page.html");
    } else {
      render("table.html");
    }
  }
  
  public void list()
  {
    String configName = loginConfigName();
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = 20;
    numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    String staffPrivs = basePrivs(STAFF_PRIVS);
    setAttr("sizeNum", Integer.valueOf(numPerPage));
    setAttr("pageMap", Staff.dao.getListPage(configName, pageNum, numPerPage, supId, "staffList", orderField, orderDirection, staffPrivs));
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(AioConstants.NODE_1));
    setAttr("pSupId", Integer.valueOf(Staff.dao.getPsupId(configName, supId)));
    setAttr("toAction", "list");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "b500", AioConstants.STATUS_ENABLE));
    render("table.html");
  }
  
  public void toAdd()
    throws Exception
  {
    setAttr("toAction", "add");
    setAttr("supId", getPara(0));
    setAttr("code", URLDecoder.decode(getPara(1, ""), AioConstants.ENCODING));
    setAttr("callback", "aioDialogAjaxDone");
    setAttr("codeIncrement", getPara(2));
    render("dialog.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    Map<String, Object> result = new HashMap();
    Model model = (Model)getModel(Staff.class);
    if (model.getInt("supId").intValue() == 0) {
      model.set("supId", getParaToInt("supId"));
    }
    model.set("depmId", getParaToInt("depm.id"));
    Integer status = model.getInt("status");
    
    model.remove("id");
    if (status == null) {
      model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
    }
    List<Model> list = Staff.dao.getListByCode(configName, model.getStr("code"), null);
    if (list.size() > 0)
    {
      result.put("statusCode", AioConstants.HTTP_RETURN300);
      result.put("message", "该编号已经存在!");
      renderJson(result);
      return;
    }
    String upids = "";
    if (model.getInt("supId").intValue() > 0)
    {
      Model<Staff> pStaff = Staff.dao.findById(configName, model.getInt("supId"));
      ((Staff)pStaff.set("node", Integer.valueOf(AioConstants.NODE_2))).update(configName);
      upids = pStaff.getStr("pids") + "{" + model.getInt("supId") + "}";
    }
    else
    {
      upids = "{0}";
    }
    model.set("pids", upids);
    model.set("fullName", model.get("name"));
    model.set("userId", Integer.valueOf(loginUserId()));
    Boolean flag = Staff.dao.add(configName, (Staff)model);
    updateBasePrivs(model.getInt("id"), model.getInt("supId"), STAFF_PRIVS);
    if (flag.booleanValue())
    {
      model.set("pids", model.get("pids") + "{" + model.getInt("id") + "}");
      model.set("rank", model.getInt("id")).update(configName);
      result.put("statusCode", AioConstants.HTTP_RETURN200);
      result.put("rel", "staffList");
    }
    String code = "";
    if (StringUtils.isNotBlank(getPara("codeIncrement")))
    {
      code = NumberUtils.increase(model.getStr("code"));
      result.put("code", code);
    }
    if (StringUtils.isBlank(getPara("isCopy")))
    {
      result.put("reset", "reset");
      result.put("dialogId", "b_staff_id");
      result.put("base", getAttr("base"));
      
      result.put("loadDialogUrl", "/base/staff/toAdd/" + model.getInt("supId") + "-" + URLEncoder.encode(code, AioConstants.ENCODING) + "-" + getPara("codeIncrement"));
    }
    if (StringUtils.isNotBlank(getPara("isCopy")))
    {
      result.put("statusCode", Integer.valueOf(200));
      result.put("rel", "staffList");
    }
    String confirmType = getPara("confirmType");
    if ("dialogCloseReload".equals(confirmType))
    {
      result.put("supId", model.getInt("supId"));
      
      result.put("reset", "reset");
      result.put("dialogId", "b_staff_id");
      result.put("base", getAttr("base"));
      result.put("loadDialogUrl", "/base/staff/optionAdd/" + model.getInt("supId") + "-" + URLEncoder.encode(code, AioConstants.ENCODING) + "-" + getPara("codeIncrement"));
    }
    result.put("opterate", "showLastPage");
    result.put("selectedObjectId", model.get("id"));
    renderJson(result);
  }
  
  @Before({Tx.class})
  public void optionAdd()
    throws UnsupportedEncodingException
  {
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    setAttr("confirmType", "dialogCloseReload");
    setAttr("supId", Integer.valueOf(supId));
    setAttr("code", URLDecoder.decode(getPara(1, ""), "utf-8"));
    setAttr("codeIncrement", getPara(2));
    
    setAttr("toAction", "add");
    setAttr("callback", "aioDialogReloadOption");
    render("dialog.html");
  }
  
  public void toEdit()
  {
    String configName = loginConfigName();
    setAttr("toAction", "edit");
    int id = getParaToInt().intValue();
    setAttr("callback", "updateTodoAndTee");
    setAttr("staff", Staff.dao.findById(configName, Integer.valueOf(id)));
    render("dialog.html");
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String configName = loginConfigName();
    Model model = (Model)getModel(Staff.class);
    Map<String, Object> result = new HashMap();
    model.set("depmId", getParaToInt("depm.id"));
    Integer status = model.getInt("status");
    if (status == null) {
      model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
    }
    List<Model> list = Staff.dao.getListByCode(configName, model.getStr("code"), model.getInt("id"));
    if (list.size() > 0)
    {
      result.put("statusCode", AioConstants.HTTP_RETURN300);
      result.put("message", "该编号已经存在!");
      renderJson(result);
      return;
    }
    model.set("fullName", model.get("name"));
    model.set("userId", Integer.valueOf(loginUserId()));
    Boolean flag = Boolean.valueOf(model.update(configName));
    if (flag.booleanValue())
    {
      result.put("statusCode", AioConstants.HTTP_RETURN200);
      result.put("rel", "staffList");
      result.put("callbackType", "closeCurrent");
      HashMap<String, Object> node = new HashMap();
      node.put("id", model.get("id"));
      node.put("name", model.get("name"));
      result.put("node", node);
      result.put("trel", "staffTree");
      result.put("url", "base/staff/child/" + model.getInt("supId"));
    }
    result.put("selectedObjectId", model.get("id"));
    renderJson(result);
  }
  
  public void toCopy()
  {
    String configName = loginConfigName();
    setAttr("toAction", "add");
    int id = getParaToInt().intValue();
    setAttr("staff", Staff.dao.findById(configName, Integer.valueOf(id)));
    setAttr("isCopy", "true");
    setAttr("callback", "aioDialogAjaxDone");
    render("dialog.html");
  }
  
  @Before({Tx.class})
  public void delete()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    String staffPrivs = basePrivs(STAFF_PRIVS);
    List<Model> childs = Staff.dao.getChild(configName, id, staffPrivs);
    if (childs.size() > 0)
    {
      returnToPage(AioConstants.HTTP_RETURN300, "删除失败，请先删除下级!", "", "", "", "");
      renderJson();
      return;
    }
    boolean verify = Staff.dao.verify(configName, "staffId", Integer.valueOf(id));
    if (verify)
    {
      returnToPage(AioConstants.HTTP_RETURN300, "数据存在引用，不能删除!", "", "", "", "");
      renderJson();
      return;
    }
    Model<Staff> staff = Staff.dao.findById(configName, Integer.valueOf(id));
    int supId = ((Integer)staff.get("supId", Integer.valueOf(0))).intValue();
    Boolean flag = Boolean.valueOf(Staff.dao.deleteById(configName, Integer.valueOf(id)));
    if (flag.booleanValue()) {
      returnToPage(AioConstants.HTTP_RETURN200, "", "", "staffList", "", "");
    }
    List<Model> supChilds = Staff.dao.getChild(configName, supId, staffPrivs);
    if ((supChilds.size() == 0) && (supId != 0))
    {
      Model<Staff> pStaff = Staff.dao.findById(configName, Integer.valueOf(supId));
      ((Staff)pStaff.set("node", Integer.valueOf(AioConstants.NODE_1))).update(configName);
      setAttr("trel", "staffTree");
      
      HashMap<String, Object> node = new HashMap();
      node.put("id", pStaff.get("id"));
      node.put("pId", pStaff.get("supId"));
      setAttr("pnode", node);
      
      setAttr("rel", "staffList");
      setAttr("url", "base/staff/child/" + pStaff.get("supId"));
      

      setAttr("selectedObjectId", pStaff.get("id"));
    }
    renderJson();
  }
  
  public void toSort()
  {
    setAttr("toAction", "sort");
    int id = getParaToInt().intValue();
    
    setAttr("callback", "dialogTreeAjaxDone");
    setAttr("id", Integer.valueOf(id));
    render("dialog.html");
  }
  
  @Before({Tx.class})
  public void sort()
  {
    String configName = loginConfigName();
    Map<String, Object> result = new HashMap();
    Model model = (Model)getModel(Staff.class);
    model.set("depmId", getParaToInt("depm.id"));
    Integer status = model.getInt("status");
    
    model.set("supId", model.getInt("id"));
    
    model.remove("id");
    if (status == null) {
      model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
    }
    List<Model> list = Staff.dao.getListByCode(configName, model.getStr("code"), null);
    if (list.size() > 0)
    {
      result.put("statusCode", AioConstants.HTTP_RETURN300);
      result.put("message", "该编号已经存在!");
      renderJson(result);
      return;
    }
    Model<Staff> pStaff = Staff.dao.findById(configName, model.getInt("supId"));
    
    String upids = "";
    if (model.getInt("supId").intValue() > 0)
    {
      ((Staff)pStaff.set("node", Integer.valueOf(AioConstants.NODE_2))).update(configName);
      upids = pStaff.getStr("pids");
    }
    else
    {
      upids = "{0}";
    }
    model.set("pids", upids);
    model.set("fullName", model.get("name"));
    model.set("userId", Integer.valueOf(loginUserId()));
    Boolean flag = Staff.dao.add(configName, (Staff)model);
    updateBasePrivs(model.getInt("id"), model.getInt("supId"), STAFF_PRIVS);
    if (flag.booleanValue())
    {
      model.set("pids", model.get("pids") + "{" + model.getInt("id") + "}");
      model.set("rank", model.getInt("id")).update(configName);
      result.put("statusCode", AioConstants.HTTP_RETURN200);
      result.put("rel", "staffList");
      result.put("target", "ajax");
      result.put("url", "base/staff/child/" + model.getInt("supId"));
      result.put("trel", "staffTree");
      
      HashMap<String, Object> node = new HashMap();
      node.put("id", pStaff.get("id"));
      node.put("pId", pStaff.get("supId"));
      node.put("name", pStaff.get("name"));
      node.put("url", "base/staff/child/" + pStaff.get("id"));
      result.put("pnode", node);
    }
    else
    {
      returnToPage(AioConstants.HTTP_RETURN300, "操作失败!", "", "", "", "");
    }
    String code = "";
    if (StringUtils.isNotBlank(getPara("codeIncrement")))
    {
      code = NumberUtils.increase(model.getStr("code"));
      result.put("code", code);
    }
    result.put("opterate", "showLastPage");
    result.put("selectedObjectId", model.get("id"));
    renderJson(result);
  }
  
  public void stopAndStart()
  {
    String configName = loginConfigName();
    int id = getParaToInt().intValue();
    Staff staff = (Staff)Staff.dao.findById(configName, Integer.valueOf(id));
    Boolean flag = Boolean.valueOf(false);
    if (AioConstants.STATUS_DISABLE == staff.getInt("status").intValue())
    {
      flag = Boolean.valueOf(staff.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE)).update(configName));
    }
    else
    {
      flag = Boolean.valueOf(staff.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE)).update(configName));
      staff.updateChildsStatus(configName, staff.getInt("id").intValue(), AioConstants.STATUS_DISABLE);
    }
    if (flag.booleanValue())
    {
      setAttr("id", Integer.valueOf(id));
      setAttr("rel", "staffList");
      setAttr("status", staff.getInt("status"));
    }
    renderJson();
  }
  
  public void toUploadImg()
  {
    setAttr("toAction", "uploadImg");
    render("upload_img.html");
  }
  
  public void uploadImg()
  {
    HttpServletRequest request = getRequest();
    String fileName = "";
    String folder = "";
    String projectPath = IO.getWebrootPath();
    Map<String, String> map = new HashMap();
    
    map.put("base", getAttr("base").toString());
    int recordId = Integer.valueOf(request.getParameter("recordId")).intValue();
    ServletFileUpload sfu = new ServletFileUpload(new DiskFileItemFactory());
    sfu.setHeaderEncoding("UTF-8");
    String loginConfigName = getPara("loginConfigName");
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
              name = new String(name.getBytes("gbk"), "utf-8");
              
              extName = sourceName.substring(sourceName.lastIndexOf("."));
            }
            String saveFillName =trimNull(new Date().getTime());
            fileName = saveFillName + extName;
            
            folder = projectPath + "upLoadImg" + File.separator + "base" + File.separator + loginConfigName + File.separator + "staff";
            IO.existFolder(folder);
            savePath = "base/" + loginConfigName + "/staff/" + fileName;
            File ff = new File(folder + "/" + fileName);
            AioerpFile a = new AioerpFile();
            a.set("sourceName", name)
              .set("saveName", saveFillName)
              .set("savePath", savePath)
              .set("typeFile", Integer.valueOf(AioConstants.FILE_TYPE1))
              .set("tableId", Integer.valueOf(AioConstants.TABLE_2))
              .set("recordId", Integer.valueOf(recordId))
              .set("status", Integer.valueOf(AioConstants.STATUS_ENABLE))
              .set("updateTime", new Date())
              .save(loginConfigName);
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
    }
    renderJson(map);
  }
  
  public void toScreen()
  {
    if (isPost())
    {
      setAttr("screenPara", getPara("screenPara"));
      setAttr("screenVal", getPara("screenVal"));
      
      setAttr("attrId", "staff_filter_attr");
      setAttr("valId", "staff_filter_value");
      
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("close", "staff_screening");
      setAttr("rel", "staffList");
      setAttr("action", getAttr("base") + "/base/staff/screen");
      
      renderJson();
    }
    else
    {
      render("screen.html");
    }
  }
  
  public void screen()
  {
    String configName = loginConfigName();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = 20;
    numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String screenPara = getPara("screenPara");
    String screenVal = getPara("screenVal");
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    String staffPrivs = basePrivs(STAFF_PRIVS);
    setAttr("pageMap", Staff.dao.getPageByTerm(configName, pageNum, numPerPage, screenPara, screenVal, supId, "staffList", orderField, orderDirection, null, staffPrivs));
    
    setAttr("supId", Integer.valueOf(supId));
    setAttr("screenPara", screenPara);
    setAttr("screenVal", screenVal);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("toAction", "screen");
    setAttr("pSupId", Integer.valueOf(Staff.dao.getPsupId(configName, supId)));
    setAttr("rowList", ReportRow.dao.getListByType(configName, "b500", AioConstants.STATUS_ENABLE));
    render("table.html");
  }
  
  @Before({Tx.class})
  public void saveRank()
  {
    String configName = loginConfigName();
    HashMap<String, Object> result = new HashMap();
    String[] ids = getPara("ids").split(",");
    String[] orders = getPara("orders").split(",");
    if (ids.length <= 0)
    {
      result.put("statusCode", AioConstants.HTTP_RETURN300);
      result.put("message", "没有排序变更!");
      renderJson(result);
      return;
    }
    boolean flag = true;
    for (int i = 0; i < ids.length; i++) {
      if (StringUtils.isNotBlank(ids[i])) {
        flag = Staff.dao.findById(configName, ids[i]).set("rank", orders[i]).update(configName);
      }
    }
    if (flag)
    {
      result.put("statusCode", AioConstants.HTTP_RETURN200);
      
      result.put("rel", "staffList");
    }
    else
    {
      result.put("statusCode", AioConstants.HTTP_RETURN300);
      result.put("message", "更新失败");
    }
    renderJson(result);
  }
  
  public void dialogChild()
  {
    String configName = loginConfigName();
    String name = getPara("param");
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    if (supId == 0) {
      supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    }
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    int pSupId = Staff.dao.getPsupId(configName, supId);
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(upObjectId)).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    String staffPrivs = basePrivs(STAFF_PRIVS);
    
    String btnPattern = getPara("btnPattern", "");
    if (btnPattern.equals("optionAdd")) {
      setAttr("showAdd", "showAdd");
    }
    setAttr("btnPattern", btnPattern);
    
    int pageSize = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    if (upObjectId > 0)
    {
      setAttr("pageMap", Staff.dao.getSupPage(configName, pageNum, pageSize, supId, upObjectId, "", Integer.valueOf(AioConstants.STATUS_ENABLE), orderField, orderDirection, staffPrivs));
    }
    else
    {
      Map<String, Object> map = Staff.dao.getChildPage(configName, pageNum, pageSize, supId, "", name, orderField, orderDirection, Integer.valueOf(AioConstants.STATUS_ENABLE), staffPrivs);
      if ((Integer.parseInt(map.get("totalCount").toString()) == 1) && (StringUtils.isNotBlank(name)))
      {
        List<Staff> list = (List)map.get("pageList");
        Staff s = (Staff)list.get(0);
        
        HashMap<String, Object> result = new HashMap();
        result.put("id", s.getInt("id"));
        result.put("name", s.getStr("name"));
        result.put("code", s.getStr("code"));
        setAttr("statusCode", AioConstants.HTTP_RETURN200);
        setAttr("obj", result);
        renderJson();
        return;
      }
      if (StringUtils.isNotBlank(name))
      {
        setAttr("term", "name");
        setAttr("termVal", name);
      }
      setAttr("pageMap", map);
    }
    columnConfig("zj103");
    
    render("option.html");
  }
  
  public void dialogLeaf()
  {
    String termVal = getPara("termVal");
    String term = getPara("term");
    String configName = loginConfigName();
    String type = getPara(0, "first");
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    String staffPrivs = basePrivs(STAFF_PRIVS);
    int pageSize = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    Map<String, Object> map = new HashMap();
    if (StringUtils.isNotBlank(termVal))
    {
      setAttr("term", term);
      setAttr("termVal", termVal);
      map = Staff.dao.getPageByTerm(configName, pageNum, pageSize, term, termVal, 0, "leafList", orderField, orderDirection, Integer.valueOf(1), staffPrivs);
    }
    else
    {
      map = Staff.dao.getListPage(configName, pageNum, pageSize, 0, "leafList", orderField, orderDirection, staffPrivs);
    }
    setAttr("pageMap", map);
    if (type.equals("first")) {
      render("leaf.html");
    } else {
      render("leafList.html");
    }
  }
  
  public void dialogScreen()
  {
    String configName = loginConfigName();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    String haveNode = getPara("haveNode", "false");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    String term = getPara("term");
    String termVal = getPara("termVal");
    if (StringUtils.isBlank(termVal)) {
      haveNode = "true";
    }
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    String staffPrivs = basePrivs(STAFF_PRIVS);
    Model model = (Model)getModel(Staff.class);
    Map<String, Object> map = new HashMap();
    int id = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    map.put("term", term);
    map.put("termVal", termVal);
    map.put("haveNode", haveNode);
    if (id > 0) {
      setAttr("pageMap", Staff.dao.getSupPage(configName, pageNum, numPerPage, orderField, orderDirection, id, "", map, staffPrivs));
    } else {
      setAttr("pageMap", Staff.dao.getPage(configName, "", pageNum, numPerPage, orderField, orderDirection, map, staffPrivs));
    }
    String btnPattern = getPara("btnPattern", "");
    String showAdd = getPara("showAdd", "");
    if ((StringUtils.isNotBlank(termVal)) && (btnPattern.indexOf("optionAdd") != -1)) {
      btnPattern = btnPattern.replace("optionAdd", "");
    } else if ((StringUtils.isBlank(termVal)) && (showAdd.equals("showAdd"))) {
      btnPattern = btnPattern + "optionAdd";
    }
    columnConfig("zj103");
    
    setAttr("btnPattern", btnPattern);
    setAttr("showAdd", showAdd);
    setAttr("staff", model);
    setAttr("objectId", Integer.valueOf(id));
    setAttr("term", term);
    setAttr("termVal", termVal);
    setAttr("haveNode", haveNode);
    setAttr("hasAddBut", "false");
    render("option.html");
  }
  
  public void verifyStaffPrivs()
  {
    String configName = loginConfigName();
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    Subject subject = SecurityUtils.getSubject();
    User user = (User)subject.getPrincipal();
    String isSort = getPara(1, "");
    if ("sort".equals(isSort))
    {
      boolean verifyBill = Staff.dao.verify(configName, "staffId", Integer.valueOf(supId));
      if (verifyBill)
      {
        setAttr("message", "单据中存在数据，不能增加下级!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    String privs = user.getStr(STAFF_PRIVS);
    if ((StringUtils.isNotBlank(privs)) && (ALL_PRIVS.equals(privs)))
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      renderJson();
      return;
    }
    if ((StringUtils.isNotBlank(privs)) && (!ALL_PRIVS.equals(privs)))
    {
      String idsStr = Staff.dao.getAllChildIdsBySupId(configName, Integer.valueOf(supId));
      String[] ids = idsStr.split(",");
      boolean verify = true;
      for (String id : ids) {
        if (privs.indexOf("," + id + ",") == -1)
        {
          verify = false;
          break;
        }
      }
      if (verify)
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN200);
        renderJson();
        return;
      }
      setAttr("message", "不具有该类的所有权限!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    setAttr("message", "不具有该类的所有权限!");
    setAttr("statusCode", AioConstants.HTTP_RETURN300);
    renderJson();
  }
  
  public void print()
  {
    String configName = loginConfigName();
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    String toAction = getPara("toAction", "child");
    Map<String, Object> data = new HashMap();
    String staffPrivs = basePrivs(STAFF_PRIVS);
    Map<String, Object> pageMap = new HashMap();
    if ("list".equals(toAction)) {
      pageMap = Staff.dao.getListPage(configName, totalCount, supId, supId, "staffList", orderField, orderDirection, staffPrivs);
    } else if ("list".equals(toAction)) {
      pageMap = Staff.dao.getPageByTerm(configName, 1, totalCount, getPara("screenPara", ""), getPara("screenVal", ""), supId, "staffList", orderField, orderDirection, null, staffPrivs);
    } else {
      pageMap = Staff.dao.getChildPage(configName, 1, totalCount, supId, "staffList", "", orderField, orderDirection, null, staffPrivs);
    }
    List<Model> pageList = (List)pageMap.get("pageList");
    if (pageList == null)
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "职员信息表");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("职员编号");
    colTitle.add("职员全名");
    colTitle.add("职员部门");
    colTitle.add("职员电话");
    colTitle.add("职员职务");
    colTitle.add("职员地址");
    colTitle.add("备注");
    colTitle.add("状态");
    colTitle.add("有无图片");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    List<String> rowData = new ArrayList();
    for (int i = 0; i < pageList.size(); i++)
    {
      Model model = (Model)pageList.get(i);
      rowData.add(trimNull(i+1));
      rowData.add(trimNull(model.getStr("code")));
      rowData.add(trimNull(model.getStr("name")));
      rowData.add(trimNull(model.getStr("departmentName")));
      rowData.add(trimNull(model.getStr("phone")));
      rowData.add(trimNull(model.getStr("role")));
      rowData.add(trimNull(model.getStr("address")));
      rowData.add(trimNull(model.getStr("memo")));
      Integer status = model.getInt("status");
      if (status.intValue() == AioConstants.STATUS_DISABLE) {
        rowData.add("停用");
      } else if (status.intValue() == AioConstants.STATUS_ENABLE) {
        rowData.add("启用");
      } else {
        rowData.add("");
      }
      Integer sysImgId = model.getInt("sysImgId");
      if (sysImgId != null) {
        rowData.add("有");
      } else {
        rowData.add("无");
      }
    }
    if ((pageList == null) || (pageList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
