package com.aioerp.controller.base;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.base.Storage;
import com.aioerp.util.NumberUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class StorageController
  extends BaseController
{
  public static String listID = "storageList";
  
  public void tree()
  {
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    List<Model> sorts = Storage.dao.getAllSorts(loginConfigName(), storagePrivs);
    Iterator<Model> iter = sorts.iterator();
    
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model mode = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", mode.get("id"));
      node.put("pId", mode.get("supId"));
      node.put("name", mode.get("fullName"));
      node.put("url", "base/storage/list/" + mode.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(-1));
    node.put("name", "仓库信息");
    node.put("url", "base/storage/list/0");
    nodeList.add(node);
    
    renderJson(nodeList);
  }
  
  public void list()
  {
    String configName = loginConfigName();
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String showLastPage = getPara("showLastPage");
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    Map<String, Object> map = new HashMap();
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      map = Storage.dao.getSupPage(configName, pageNum, numPerPage, supId, upObjectId, listID, orderField, orderDirection, storagePrivs);
    }
    else if ((showLastPage != null) && (showLastPage.equals("true")))
    {
      map = Storage.dao.getLastPage(configName, pageNum, numPerPage, supId, listID, orderField, orderDirection, storagePrivs);
    }
    else
    {
      map = Storage.dao.getPageBySupId(configName, listID, pageNum, numPerPage, supId, orderField, orderDirection, storagePrivs);
    }
    int pSupId = 0;
    if (supId > 0) {
      pSupId = Storage.getPsupId(configName, supId);
    }
    setAttr("pageMap", map);
    

    columnConfig("b506");
    

    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(0));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    String whichRefresh = getPara("whichRefresh", "");
    if (whichRefresh.equals("navTabRefresh")) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void toSave()
  {
    String configName = loginConfigName();
    String str = getPara(0);
    int id = getParaToInt(1, Integer.valueOf(0)).intValue();
    String action = "";
    String callback = "dialogAjaxDone";
    Storage storage = null;
    if (str.equals("add"))
    {
      setAttr("supId", Integer.valueOf(id));
      action = "add";
    }
    else if (str.equals("copy"))
    {
      storage = (Storage)Storage.dao.findById(configName, Integer.valueOf(id));
      action = "copy";
    }
    else if (str.equals("edit"))
    {
      storage = (Storage)Storage.dao.findById(configName, Integer.valueOf(id));
      action = "update";
    }
    else if (str.equals("sort"))
    {
      setAttr("supId", Integer.valueOf(id));
      action = "save";
    }
    setAttr("toAction", action);
    setAttr("callback", callback);
    setAttr("storage", storage);
    render("dialog.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws UnsupportedEncodingException
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Storage storage = (Storage)getModel(Storage.class);
      
      Map<String, Object> vesionMap = ComFunController.vaildateOneStorage(configName);
      if (Integer.valueOf(vesionMap.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(vesionMap);
        return;
      }
      int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
      storage.set("supId", Integer.valueOf(supId));
      String code = storage.getStr("code");
      Boolean exist = Storage.dao.codeIsExist(configName, code, 0);
      if (exist.booleanValue())
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "新增失败,编号已存在!");
      }
      else
      {
        boolean flag = false;
        

        String sPids = "";
        if (supId > 0)
        {
          Storage.dao.findById(configName, Integer.valueOf(supId)).set("node", Integer.valueOf(2)).update(configName);
          Storage pStorage = (Storage)Storage.dao.findById(configName, Integer.valueOf(supId));
          sPids = pStorage.getStr("pids") + "{" + 
            storage.getInt("supId") + "}";
        }
        else
        {
          sPids = "{0}";
        }
        storage.set("userId", Integer.valueOf(loginUserId()));
        flag = storage.set("createTime", new Date()).set("pids", sPids).save(configName);
        
        updateBasePrivs(storage.getInt("id"), storage.getInt("supId"), STORAGE_PRIVS);
        

        storage.set("pids", storage.get("pids") + "{" + storage.getInt("id") + "}");
        storage.set("rank", storage.getInt("id")).update(configName);
        if (flag)
        {
          setAttr("statusCode", Integer.valueOf(200));
          setAttr("rel", listID);
          if (StringUtils.isNotBlank(getPara("codeIncrement"))) {
            code = NumberUtils.increase(storage.getStr("code"));
          }
          if (!getParaToBoolean("codeIncrement", Boolean.valueOf(false)).booleanValue()) {
            code = "";
          }
          String url = "/base/storage/add/";
          
          String confirmType = getPara("confirmType");
          if ("dialogCloseReload".equals(confirmType))
          {
            url = "/base/storage/optionAdd/";
            setAttr("supId", Integer.valueOf(supId));
          }
          setAttr("reset", "reset");
          setAttr("dialogId", "b_storage_add");
          setAttr("base", getAttr("base"));
          setAttr("loadDialogUrl", url + 
            storage.getInt("supId") + "-" + 
            URLEncoder.encode(code, "utf-8") + "-" + 
            getPara("codeIncrement"));
          
          setAttr("opterate", "showLastPage");
          setAttr("selectedObjectId", storage.get("id"));
        }
        else
        {
          setAttr("statusCode", Integer.valueOf(300));
          setAttr("message", "新增失败");
        }
      }
      renderJson();
    }
    else
    {
      int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
      setAttr("supId", Integer.valueOf(supId));
      setAttr("code", URLDecoder.decode(getPara(1, ""), "utf-8"));
      setAttr("codeIncrement", getPara(2));
      
      setAttr("toAction", "add");
      setAttr("callback", "aioDialogAjaxDone");
      render("dialog.html");
    }
  }
  
  @Before({Tx.class})
  public void copy()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Storage storage = (Storage)getModel(Storage.class);
      String code = storage.getStr("code");
      Boolean exist = Storage.dao.codeIsExist(configName, code, 0);
      if (exist.booleanValue())
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "新增失败,编号已存在!");
      }
      else
      {
        String oldPids = (String)storage.get("pids");
        storage.set("pids", oldPids.subSequence(0, oldPids.lastIndexOf("{")));
        storage.set("createTime", new Date());
        Boolean flag = Boolean.valueOf(storage.save(configName));
        
        updateBasePrivs(storage.getInt("id"), storage.getInt("supId"), STORAGE_PRIVS);
        
        storage.set("userId", Integer.valueOf(loginUserId()));
        
        storage.set("pids", storage.get("pids") + "{" + storage.getInt("id") + "}");
        storage.set("rank", storage.getInt("id")).update(configName);
        if (flag.booleanValue())
        {
          setAttr("statusCode", Integer.valueOf(200));
          setAttr("rel", listID);
          if (StringUtils.isNotBlank(getPara("codeIncrement"))) {
            setAttr("code", NumberUtils.increase(storage.getStr("code")));
          }
          setAttr("opterate", "showLastPage");
          setAttr("selectedObjectId", storage.get("id"));
        }
        else
        {
          setAttr("statusCode", Integer.valueOf(300));
          setAttr("message", "新增失败");
        }
      }
      renderJson();
    }
    else
    {
      int sid = getParaToInt(0, Integer.valueOf(0)).intValue();
      Storage storage = (Storage)Storage.dao.findById(configName, Integer.valueOf(sid));
      
      setAttr("storage", storage);
      setAttr("supId", storage.get("supId"));
      
      setAttr("toAction", "copy");
      setAttr("callback", "aioDialogAjaxDone");
      render("dialog.html");
    }
  }
  
  public void edit()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Storage storage = (Storage)getModel(Storage.class);
      int sid = getParaToInt("sid").intValue();
      if (Storage.dao.codeIsExist(configName, storage.getStr("code"), sid).booleanValue())
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "修改失败,编码已存在!");
        
        renderJson();
        return;
      }
      storage.set("id", Integer.valueOf(sid));
      int supId = Storage.getPsupId(configName, sid);
      storage.set("userId", Integer.valueOf(loginUserId()));
      boolean flag = storage.update(configName);
      if (flag)
      {
        if (storage.hasChildren(configName, sid))
        {
          HashMap<String, Object> node = new HashMap();
          node.put("id", storage.get("id"));
          node.put("name", storage.get("fullName"));
          setAttr("node", node);
          setAttr("trel", "storageTree");
        }
        setAttr("statusCode", Integer.valueOf(200));
        setAttr("rel", listID);
        setAttr("callbackType", "closeCurrent");
        setAttr("url", "base/storage/list/" + supId);
        
        setAttr("selectedObjectId", storage.get("id"));
      }
      else
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "修改失败");
      }
      renderJson();
    }
    else
    {
      int sid = getParaToInt(0, Integer.valueOf(0)).intValue();
      Storage storage = (Storage)Storage.dao.findById(configName, Integer.valueOf(sid));
      
      setAttr("storage", storage);
      setAttr("toAction", "edit");
      setAttr("callback", "updateTodoAndTee");
      render("dialog.html");
    }
  }
  
  public void sort()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Storage storage = (Storage)getModel(Storage.class);
      
      int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
      storage.set("supId", Integer.valueOf(supId));
      if (Storage.dao.codeIsExist(configName, storage.getStr("code"), 0).booleanValue())
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "新增失败,编码已存在!");
        
        renderJson();
        return;
      }
      boolean flag = false;
      

      Storage pStorage = (Storage)Storage.dao.findById(configName, Integer.valueOf(supId));
      if (!Storage.dao.hasChildren(configName, supId)) {
        Storage.dao.findById(configName, Integer.valueOf(supId)).set("node", Integer.valueOf(2)).update(configName);
      }
      HashMap<String, Object> node = new HashMap();
      node.put("id", pStorage.get("id"));
      node.put("pId", pStorage.get("supId"));
      node.put("name", pStorage.get("fullName"));
      node.put("url", "base/storage/list/" + pStorage.get("id"));
      setAttr("pnode", node);
      


      String upids = pStorage.getStr("pids");
      
      flag = storage.set("createTime", new Date()).set(
        "pids", upids).save(configName);
      
      updateBasePrivs(storage.getInt("id"), storage.getInt("supId"), STORAGE_PRIVS);
      

      storage.set("pids", storage.get("pids") + "{" + storage.getInt("id") + "}");
      storage.set("userId", Integer.valueOf(loginUserId()));
      storage.set("rank", storage.getInt("id")).update(configName);
      if (flag)
      {
        setAttr("statusCode", Integer.valueOf(200));
        setAttr("callbackType", "closeCurrent");
        setAttr("rel", listID);
        setAttr("target", "ajax");
        setAttr("url", "base/storage/list/" + pStorage.get("id"));
        setAttr("trel", "storageTree");
        
        setAttr("opterate", "showLastPage");
        setAttr("selectedObjectId", storage.get("id"));
      }
      else
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "新增失败");
      }
      renderJson();
    }
    else
    {
      int sid = getParaToInt(0, Integer.valueOf(0)).intValue();
      


      setAttr("supId", Integer.valueOf(sid));
      setAttr("toAction", "sort");
      setAttr("callback", "dialogTreeAjaxDone");
      render("dialog.html");
    }
  }
  
  public void delete()
  {
    int sid = getParaToInt(0, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    if (sid == 0)
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "删除失败,数据不存在");
      
      renderJson();
      return;
    }
    boolean verify = Storage.dao.verify(configName, "storageId", Integer.valueOf(sid));
    if (verify)
    {
      returnToPage(AioConstants.HTTP_RETURN300, "数据存在引用，不能删除!", "", "", "", "");
      renderJson();
      return;
    }
    Storage storage = (Storage)Storage.dao.findById(configName, Integer.valueOf(sid));
    if (Storage.dao.hasChildren(configName, storage.getInt("id").intValue()))
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "删除失败,先删除下级数据");
      
      renderJson();
      return;
    }
    int supId = ((Integer)storage.get("supId", Integer.valueOf(0))).intValue();
    Boolean flag = Boolean.valueOf(Storage.dao.deleteById(configName, Integer.valueOf(sid)));
    if (flag.booleanValue())
    {
      if (supId != 0)
      {
        Storage pStorage = (Storage)Storage.dao.findById(configName, Integer.valueOf(supId));
        if (!Storage.dao.hasChildren(configName, supId))
        {
          pStorage.set("node", Integer.valueOf(1)).update(configName);
          
          HashMap<String, Object> node = new HashMap();
          node.put("id", pStorage.get("id"));
          node.put("pId", pStorage.get("supId"));
          setAttr("pnode", node);
          
          setAttr("trel", "storageTree");
          setAttr("url", "base/storage/list/" + pStorage.get("supId") + "-" + supId);
          setAttr("selectedObjectId", pStorage.get("id"));
        }
      }
      setAttr("statusCode", Integer.valueOf(200));
      setAttr("rel", listID);
    }
    else
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "删除失败");
    }
    renderJson();
  }
  
  public void stopOrStart()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    int status = Storage.dao.findById(configName, Integer.valueOf(id), "status").getInt("status").intValue();
    Boolean flag = Boolean.valueOf(false);
    if (status == 2)
    {
      flag = 
        Boolean.valueOf(Storage.dao.findById(configName, getParaToInt()).set("status", Integer.valueOf(1)).update(configName));
      status = 1;
    }
    else if (status == 1)
    {
      flag = 
        Boolean.valueOf(Storage.dao.findById(configName, getParaToInt()).set("status", Integer.valueOf(2)).update(configName));
      status = 2;
    }
    if (flag.booleanValue())
    {
      setAttr("id", Integer.valueOf(id));
      setAttr("rel", listID);
      setAttr("status", Integer.valueOf(status));
    }
    renderJson();
  }
  
  public void line()
  {
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    Map<String, Object> map = Storage.dao.getPageStorageBySupId(loginConfigName(), listID, pageNum, numPerPage, supId, orderField, orderDirection, storagePrivs);
    
    setAttr("supId", Integer.valueOf(supId));
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    columnConfig("b506");
    
    setAttr("pageMap", map);
    
    setAttr("node", Integer.valueOf(1));
    setAttr("goList", "line");
    
    render("list.html");
  }
  
  @Before({Tx.class})
  public void saveRank()
  {
    String configName = loginConfigName();
    Map<String, Object> result = new HashMap();
    String[] ids = getPara("ids").split(",");
    String[] orders = getPara("orders").split(",");
    
    boolean flag = true;
    for (int i = 0; i < ids.length; i++) {
      Storage.dao.findById(configName, ids[i]).set("rank", orders[i]).update(configName);
    }
    if (flag)
    {
      result.put("statusCode", AioConstants.HTTP_RETURN200);
      result.put("rel", listID);
    }
    else
    {
      result.put("statusCode", AioConstants.HTTP_RETURN300);
      result.put("message", "保存失败!");
    }
    renderJson(result);
  }
  
  public void toFilter()
  {
    if (isPost())
    {
      setAttr("screenPara", getPara("screenPara"));
      setAttr("screenVal", getPara("screenVal"));
      setAttr("scope", getPara("scope"));
      setAttr("supId", getParaToInt("supId"));
      setAttr("node", getParaToInt("node"));
      
      setAttr("attrId", "b_stroage_term_par");
      setAttr("valId", "b_stroage_term_val");
      setAttr("scopeId", "b_stroage_term_scope");
      setAttr("sup", "b_stroage_term_supId");
      setAttr("nodeId", "b_stroage_term_node");
      
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("close", "b_storage_filter");
      setAttr("rel", listID);
      setAttr("action", getAttr("base") + "/base/storage/filter");
      
      renderJson();
    }
    else
    {
      int supId = getParaToInt("supId", Integer.valueOf(-1)).intValue();
      int node = getParaToInt("node", Integer.valueOf(0)).intValue();
      setAttr("supId", Integer.valueOf(supId));
      setAttr("node", Integer.valueOf(node));
      render("filter.html");
    }
  }
  
  public void filter()
  {
    if (isPost())
    {
      int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
      int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
      String orderField = getPara("orderField", "rank");
      String orderDirection = getPara("orderDirection", "asc");
      
      String screenPara = getPara("screenPara");
      String screenVal = getPara("screenVal");
      String scope = getPara("scope");
      
      int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
      int node = getParaToInt("node", Integer.valueOf(0)).intValue();
      boolean inAll = true;
      if ((StringUtils.isBlank(scope)) || ("present".equals(scope))) {
        inAll = false;
      }
      String storagePrivs = basePrivs(STORAGE_PRIVS);
      Map<String, Object> map = Storage.dao.getPageStorageByTerm(loginConfigName(), listID, pageNum, numPerPage, screenPara, screenVal, inAll, supId, node, orderField, orderDirection, storagePrivs);
      setAttr("orderField", orderField);
      setAttr("orderDirection", orderDirection);
      
      setAttr("pageMap", map);
      

      columnConfig("b506");
      

      setAttr("screenPara", screenPara);
      setAttr("screenVal", screenVal);
      setAttr("scope", scope);
      setAttr("supId", Integer.valueOf(supId));
      setAttr("node", Integer.valueOf(node));
      
      setAttr("goList", "filter");
      render("list.html");
    }
    else
    {
      render("filter.html");
    }
  }
  
  public void toSearch()
  {
    if (isPost())
    {
      setAttr("supId", getParaToInt("supId", Integer.valueOf(0)));
      setAttr("node", getParaToInt("node", Integer.valueOf(0)));
      setAttr("searchPara", getPara("searchPara", ""));
      setAttr("searchVal", getPara("searchVal", ""));
      
      setAttr("attrId", "b_stroage_term_par");
      setAttr("valId", "b_stroage_term_val");
      setAttr("sup", "b_stroage_term_supId");
      setAttr("nodeId", "b_stroage_term_node");
      
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("close", "b_storage_search");
      setAttr("rel", listID);
      setAttr("action", getAttr("base") + "/base/storage/search");
      
      renderJson();
    }
    else
    {
      int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
      int node = getParaToInt(1, Integer.valueOf(0)).intValue();
      setAttr("supId", Integer.valueOf(supId));
      setAttr("node", Integer.valueOf(node));
      render("search.html");
    }
  }
  
  public void search()
  {
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    String configName = loginConfigName();
    String searchPara = getPara("screenPara");
    String searchVal = getPara("screenVal");
    
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    Map<String, Object> map = Storage.dao.getPageByTerms(configName, listID, pageNum, numPerPage, searchPara, searchVal, supId, node, orderField, orderDirection, storagePrivs);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("pageMap", map);
    

    columnConfig("b506");
    
    setAttr("screenPara", searchPara);
    setAttr("screenVal", searchVal);
    

    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(node));
    setAttr("goList", "search");
    render("list.html");
  }
  
  public void dialogList()
  {
    String configName = loginConfigName();
    String term = getPara("term", "");
    String termVal = getPara("termVal", "");
    String isAll = getPara("isAll", "");
    if ((term != "") && (termVal != "") && (isAll != ""))
    {
      dialogSearch();
      return;
    }
    int supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0))).intValue();
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String showLastPage = getPara("showLastPage");
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    Map<String, Object> map = new HashMap();
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      supId = Storage.getPsupId(configName, upObjectId);
      map = Storage.dao.getSupPage(configName, pageNum, numPerPage, supId, upObjectId, "", orderField, orderDirection, storagePrivs);
    }
    else if ((showLastPage != null) && (showLastPage.equals("true")))
    {
      map = Storage.dao.getLastPage(configName, pageNum, numPerPage, supId, "", orderField, orderDirection, storagePrivs);
    }
    else
    {
      map = Storage.dao.getPageBySupId(configName, "", pageNum, numPerPage, supId, orderField, orderDirection, storagePrivs);
    }
    String btnPattern = getPara("btnPattern", "");
    setAttr("btnPattern", btnPattern);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    columnConfig("zj102");
    

    setAttr("supId", Integer.valueOf(supId));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    setAttr("toAction", "dialogList");
    render("option.html");
  }
  
  public void dialogSearch()
  {
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    
    int id = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    Map<String, Object> map = new HashMap();
    String term = getPara("term");
    String termVal = getPara("termVal");
    String isAll = getPara("isAll");
    map.put("term", term);
    map.put("termVal", termVal);
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    
    Map<String, Object> pageMap = Storage.dao.getPageDialogByTerms(loginConfigName(), pageNum, numPerPage, orderField, orderDirection, supId, map, storagePrivs);
    if ((Integer.parseInt(pageMap.get("totalCount").toString()) == 1) && (isAll != null) && (!"".equals(isAll)))
    {
      List<Storage> list = (List)pageMap.get("pageList");
      Storage s = (Storage)list.get(0);
      
      HashMap<String, Object> result = new HashMap();
      result.put("id", s.getInt("id"));
      result.put("fullName", s.getStr("fullName"));
      result.put("code", s.getStr("code"));
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("obj", result);
      renderJson();
      return;
    }
    columnConfig("zj102");
    
    String btnPattern = getPara("btnPattern", "");
    setAttr("btnPattern", btnPattern);
    
    setAttr("pageMap", pageMap);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("term", term);
    setAttr("termVal", termVal);
    setAttr("objectId", Integer.valueOf(id));
    setAttr("toAction", "dialogSearch");
    render("option.html");
  }
  
  public void option()
  {
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    String configName = loginConfigName();
    String fullName = "";
    String code = "";
    
    Model model = (Model)getModel(Storage.class);
    fullName = model.getStr("fullName");
    code = model.getStr("code");
    if (!isPost())
    {
      int id = getParaToInt(0, Integer.valueOf(0)).intValue();
      int supId = Storage.getPsupId(configName, id);
      if (id > 0)
      {
        String storagePrivs = basePrivs(STORAGE_PRIVS);
        setAttr("pageMap", Storage.dao.getSupPage(configName, pageNum, numPerPage, orderField, orderDirection, supId, orderDirection, fullName, code, storagePrivs));
        setAttr("objectId", Integer.valueOf(id));
      }
    }
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
  
  public void storageMultiOption()
    throws UnsupportedEncodingException
  {
    String configName = loginConfigName();
    int pSupId = 0;
    int supId = 0;
    int upObjectId = 0;
    String oldDownSelectObjs = "";
    String newDownSelectObjs = "";
    String term = "";
    String termVal = "";
    
    String type = getPara(0, "toDialog");
    if (type.equals("toDialog"))
    {
      String selectIds = getPara("selectIds", "");
      newDownSelectObjs = selectIds;
      oldDownSelectObjs = selectIds;
    }
    else if (type.equals("page"))
    {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
      oldDownSelectObjs = getPara("oldSelectCheckBoxIds", "");
      newDownSelectObjs = getPara("selectCheckBoxIds", "");
    }
    else if (type.equals("search"))
    {
      oldDownSelectObjs = getPara("oldSelectCheckBoxIds", "");
      newDownSelectObjs = getPara("selectCheckBoxIds", "");
      term = getPara("term", "");
      termVal = getPara("termVal", "");
    }
    else if (type.equals("down"))
    {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
      oldDownSelectObjs = getPara(2, "");
      newDownSelectObjs = getPara(3, "");
    }
    else if (type.equals("up"))
    {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
      upObjectId = getParaToInt(2, Integer.valueOf(0)).intValue();
      oldDownSelectObjs = getPara(3, "");
      newDownSelectObjs = getPara(4, "");
    }
    if (supId > 0) {
      pSupId = Storage.getPsupId(configName, supId);
    }
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    setAttr("term", term);
    setAttr("termVal", termVal);
    Map<String, Object> map = new HashMap();
    map.put("term", term);
    map.put("termVal", termVal);
    
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    Map<String, Object> pageMap = null;
    if (type.equals("toDialog"))
    {
      pageMap = Storage.dao.sotrageMultiPage(configName, supId, pageNum, numPerPage, orderField, orderDirection, map, storagePrivs);
    }
    else if ((type.equals("search")) || (type.equals("page")) || (type.equals("down")) || (type.equals("up")))
    {
      if ((type.equals("up")) && (upObjectId > 0))
      {
        pageMap = Storage.dao.storageMultiUp(configName, supId, pageNum, numPerPage, orderField, orderDirection, upObjectId, "", map, storagePrivs);
      }
      else if (type.equals("search"))
      {
        pageMap = Storage.dao.sotrageMultiSearch(configName, pageNum, numPerPage, orderField, orderDirection, map, storagePrivs);
        
        List searchList = (List)pageMap.get("pageList");
        if ((pageMap != null) && (searchList != null) && (searchList.size() > 0))
        {
          Model storage = (Model)searchList.get(0);
          supId = Integer.valueOf(storage.get("supId").toString()).intValue();
        }
      }
      else if (type.equals("down"))
      {
        pageMap = Storage.dao.storageMultiDown(configName, supId, pageNum, numPerPage, orderField, orderDirection, map, storagePrivs);
      }
      else
      {
        pageMap = Storage.dao.sotrageMultiPage(configName, supId, pageNum, numPerPage, orderField, orderDirection, map, storagePrivs);
      }
      List<Model> alllist = (List)pageMap.get("listAllIdAndPids");
      List<Model> subBySupIdlist = (List)pageMap.get("listSubIdAndPidsBySupId");
      

      List<String> newSelectList = StringUtil.strsArrToList(newDownSelectObjs.split(","));
      
      Map<String, List<String>> addAndDelAndEqMapList = StringUtil.getAddAndDelAndEqList(oldDownSelectObjs.split(","), newDownSelectObjs.split(","));
      List<String> deleteList = (List)addAndDelAndEqMapList.get("delete");
      if ((deleteList != null) && (alllist != null)) {
        for (int i = 0; i < deleteList.size(); i++)
        {
          String deleteId = (String)deleteList.get(i);
          for (int j = 0; j < alllist.size(); j++)
          {
            String id = ((Model)alllist.get(j)).getInt("id").toString();
            String pids = ((Model)alllist.get(j)).getStr("pids");
            if (deleteId.equals(id))
            {
              pids = pids.substring(1, pids.length() - 1);
              List<String> pidsList = StringUtil.strsArrToList(pids.split("\\}\\{"));
              for (int k = 0; k < pidsList.size(); k++)
              {
                String pidsIds = (String)pidsList.get(k);
                newSelectList.remove(pidsIds);
              }
            }
          }
        }
      }
      if ((newSelectList.contains(String.valueOf(supId))) && (subBySupIdlist != null)) {
        for (int i = 0; i < subBySupIdlist.size(); i++)
        {
          String id = ((Model)subBySupIdlist.get(i)).getInt("id").toString();
          if (!newSelectList.contains(id)) {
            newSelectList.add(id);
          }
        }
      }
      newDownSelectObjs = "";
      for (int i = 0; i < newSelectList.size(); i++) {
        newDownSelectObjs = newDownSelectObjs + "," + (String)newSelectList.get(i);
      }
      if (!newDownSelectObjs.equals("")) {
        newDownSelectObjs = newDownSelectObjs.substring(1, newDownSelectObjs.length());
      }
    }
    setAttr("pageMap", pageMap);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("objectId", Integer.valueOf(upObjectId));
    setAttr("selectCheckboxObjs", newDownSelectObjs);
    render("/WEB-INF/template/base/storage/multiselectOption/storageMultiOption.html");
  }
  
  public void storageMultisearchBack()
  {
    String newDownSelectObjs = getPara(0, "");
    String selectIds = getPara("selectIds", "");
    if (selectIds != "") {
      newDownSelectObjs = selectIds;
    }
    List<Model> list = Storage.dao.storageMultiSearchBack(loginConfigName(), newDownSelectObjs);
    
    String storageIds = "";
    String storageFullNames = "";
    for (int i = 0; i < list.size(); i++)
    {
      storageIds = storageIds + ((Model)list.get(i)).getInt("id") + ",";
      storageFullNames = storageFullNames + ((Model)list.get(i)).getStr("fullName") + ",";
    }
    storageIds = storageIds.substring(0, storageIds.length() - 1);
    storageFullNames = storageFullNames.substring(0, storageFullNames.length() - 1);
    
    Map<String, Object> obj = new HashMap();
    obj.put("storageIds", storageIds);
    obj.put("storageFullNames", storageFullNames);
    renderJson(obj);
  }
  
  public void storageDisplayCount()
  {
    String storageIds = getPara("storageIds", "");
    String modelType = getPara("modelType", "unit");
    String tableName = "b_unit";
    if (modelType.equals("storage")) {
      tableName = "b_storage";
    }
    int count = Storage.dao.storageCountByIds(loginConfigName(), storageIds, tableName);
    if (modelType.equals("unit"))
    {
      if (count > AioConstants.UNIT_DISPLAY_COUNT)
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "单位最大个数大于" + AioConstants.UNIT_DISPLAY_COUNT + "!");
        renderJson(this.result);
      }
    }
    else if ((modelType.equals("storage")) && 
      (count > AioConstants.STORAGE_DISPLAY_COUNT))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "仓库最大个数大于" + AioConstants.STORAGE_DISPLAY_COUNT + "!");
      renderJson(this.result);
      return;
    }
    this.result.put("statusCode", Integer.valueOf(200));
    renderJson(this.result);
  }
  
  public void print()
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "仓库信息");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("拼音码");
    colTitle.add("仓库简称");
    colTitle.add("负责人");
    colTitle.add("负责人电话");
    colTitle.add("备注");
    colTitle.add("仓库地址");
    colTitle.add("状态");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    
    Integer pageNum = Integer.valueOf(1);
    Integer numPerPage = Integer.valueOf(totalCount);
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    Map<String, Object> pageMap = new HashMap();
    if (node == 0) {
      pageMap = Storage.dao.getPageBySupId(configName, listID, pageNum.intValue(), numPerPage.intValue(), supId, orderField, orderDirection, storagePrivs);
    } else if (node == 1) {
      pageMap = Storage.dao.getPageStorageBySupId(configName, listID, pageNum.intValue(), numPerPage.intValue(), supId, orderField, orderDirection, storagePrivs);
    }
    List<Model> list = (List)pageMap.get("pageList");
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    int i = 1;
    for (Model record : list)
    {
      rowData.add(trimNull(i));
      
      rowData.add(trimNull(record.get("code")));
      rowData.add(trimNull(record.get("fullName")));
      rowData.add(trimNull(record.get("spell")));
      rowData.add(trimNull(record.get("smallName")));
      rowData.add(trimNull(record.get("functionary")));
      rowData.add(trimNull(record.get("phone")));
      rowData.add(trimNull(record.get("memo")));
      rowData.add(trimNull(record.get("stoAddress")));
      int status = record.getInt("status").intValue();
      if (status == 1) {
        rowData.add("停用");
      } else {
        rowData.add("启用");
      }
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
