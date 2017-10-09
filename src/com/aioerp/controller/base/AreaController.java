package com.aioerp.controller.base;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Area;
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

public class AreaController
  extends BaseController
{
  public static String listID = "areaList";
  
  public void tree()
  {
    String configName = loginConfigName();
    String areaPrivs = basePrivs(AREA_PRIVS);
    List<Model> sorts = Area.dao.getAllSorts(configName, areaPrivs);
    Iterator<Model> iter = sorts.iterator();
    
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model mode = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", mode.get("id"));
      node.put("pId", mode.get("supId"));
      node.put("name", mode.get("fullName"));
      node.put("url", "base/area/list/" + mode.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(-1));
    node.put("name", "地区信息");
    node.put("url", "base/area/list/0");
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
    String areaPrivs = basePrivs(AREA_PRIVS);
    String showLastPage = getPara("showLastPage");
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    
    Map<String, Object> map = new HashMap();
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      map = Area.dao.getSupPageBySupId(configName, pageNum, numPerPage, supId, upObjectId, listID, orderField, orderDirection, areaPrivs);
    }
    else if ((showLastPage != null) && (showLastPage.equals("true")))
    {
      map = Area.dao.getLastPageBySupId(configName, pageNum, numPerPage, supId, listID, orderField, orderDirection, areaPrivs);
    }
    else
    {
      map = Area.dao.getPageBySupId(configName, pageNum, numPerPage, supId, listID, orderField, orderDirection, areaPrivs);
    }
    int pSupId = 0;
    if (supId > 0) {
      pSupId = Area.dao.getPsupId(configName, supId);
    }
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("supId", Integer.valueOf(supId));
    

    columnConfig("b504");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(0));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    
    String whichRefresh = getPara("whichRefresh", "");
    if (whichRefresh.equals("navTabRefresh")) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  @Before({Tx.class})
  public void add()
    throws UnsupportedEncodingException
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Area area = (Area)getModel(Area.class);
      int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
      area.set("supId", Integer.valueOf(supId));
      String code = area.getStr("code");
      Boolean exist = Area.dao.codeIsExist(configName, code, 0);
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
          Area.dao.findById(configName, Integer.valueOf(supId)).set("node", Integer.valueOf(2)).update(configName);
          Area pArea = (Area)Area.dao.findById(configName, Integer.valueOf(supId));
          sPids = pArea.getStr("pids") + "{" + 
            area.getInt("supId") + "}";
        }
        else
        {
          sPids = "{0}";
        }
        flag = area.set("createTime", new Date()).set("pids", sPids).save(configName);
        
        updateBasePrivs(area.getInt("id"), area.getInt("supId"), AREA_PRIVS);
        

        area.set("pids", area.get("pids") + "{" + area.getInt("id") + "}");
        area.set("rank", area.getInt("id")).update(configName);
        area.set("userId", Integer.valueOf(loginUserId()));
        if (flag)
        {
          setAttr("statusCode", Integer.valueOf(200));
          setAttr("rel", listID);
          if (StringUtils.isNotBlank(getPara("codeIncrement"))) {
            code = NumberUtils.increase(area.getStr("code"));
          }
          if (!getParaToBoolean("codeIncrement", Boolean.valueOf(false)).booleanValue()) {
            code = "";
          }
          setAttr("reset", "reset");
          setAttr("dialogId", "b_area_add");
          setAttr("base", getAttr("base"));
          setAttr("loadDialogUrl", "/base/area/add/" + 
            area.getInt("supId") + "-" + 
            URLEncoder.encode(code, "utf-8") + "-" + 
            getPara("codeIncrement"));
          
          setAttr("opterate", "showLastPage");
          setAttr("selectedObjectId", area.get("id"));
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
      setAttr("callBack", "aioDialogAjaxDone");
      render("dialog.html");
    }
  }
  
  @Before({Tx.class})
  public void copy()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Area area = (Area)getModel(Area.class);
      String code = area.getStr("code");
      Boolean exist = Area.dao.codeIsExist(configName, code, 0);
      if (exist.booleanValue())
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "新增失败,编号已存在!");
      }
      else
      {
        String oldPids = (String)area.get("pids");
        area.set("pids", oldPids.subSequence(0, oldPids.lastIndexOf("{")));
        area.set("createTime", new Date());
        
        Boolean flag = Boolean.valueOf(area.save(configName));
        
        updateBasePrivs(area.getInt("id"), area.getInt("supId"), AREA_PRIVS);
        

        area.set("pids", area.get("pids") + "{" + area.getInt("id") + "}");
        area.set("rank", area.getInt("id")).update(configName);
        area.set("userId", Integer.valueOf(loginUserId()));
        if (flag.booleanValue())
        {
          setAttr("statusCode", Integer.valueOf(200));
          setAttr("rel", listID);
          if (StringUtils.isNotBlank(getPara("codeIncrement"))) {
            setAttr("code", NumberUtils.increase(area.getStr("code")));
          }
          setAttr("opterate", "showLastPage");
          setAttr("selectedObjectId", area.get("id"));
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
      int aid = getParaToInt(0, Integer.valueOf(0)).intValue();
      Area area = (Area)Area.dao.findById(configName, Integer.valueOf(aid));
      
      setAttr("area", area);
      setAttr("supId", area.get("supId"));
      
      setAttr("toAction", "copy");
      setAttr("callBack", "aioDialogAjaxDone");
      render("dialog.html");
    }
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Area area = (Area)getModel(Area.class);
      int aid = getParaToInt("aid").intValue();
      if (Area.dao.codeIsExist(configName, area.getStr("code"), aid).booleanValue())
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "修改失败,编码已存在!");
        
        renderJson();
        return;
      }
      area.set("id", Integer.valueOf(aid));
      area.set("userId", Integer.valueOf(loginUserId()));
      int supId = area.getPsupId(configName, aid);
      boolean flag = area.update(configName);
      if (flag)
      {
        if (area.hasChildren(configName, aid))
        {
          HashMap<String, Object> node = new HashMap();
          node.put("id", area.get("id"));
          node.put("name", area.get("fullName"));
          setAttr("node", node);
          setAttr("trel", "areaTree");
        }
        setAttr("statusCode", Integer.valueOf(200));
        setAttr("rel", listID);
        setAttr("callbackType", "closeCurrent");
        setAttr("url", "base/area/list/" + supId);
        
        setAttr("selectedObjectId", area.get("id"));
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
      int aid = getParaToInt(0, Integer.valueOf(0)).intValue();
      Area area = (Area)Area.dao.findById(configName, Integer.valueOf(aid));
      
      setAttr("area", area);
      setAttr("toAction", "edit");
      setAttr("callBack", "updateTodoAndTee");
      render("dialog.html");
    }
  }
  
  @Before({Tx.class})
  public void delete()
  {
    int aid = getParaToInt(0, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    if (aid == 0)
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "删除失败,数据不存在");
      
      renderJson();
      return;
    }
    boolean verify = Area.dao.verify(configName, "areaId", Integer.valueOf(aid));
    if (verify)
    {
      returnToPage(AioConstants.HTTP_RETURN300, "数据存在引用，不能删除!", "", "", "", "");
      renderJson();
      return;
    }
    Area area = (Area)Area.dao.findById(configName, Integer.valueOf(aid));
    if (Area.dao.hasChildren(configName, area.getInt("id").intValue()))
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "删除失败,先删除下级数据");
      
      renderJson();
      return;
    }
    int supId = ((Integer)area.get("supId", Integer.valueOf(0))).intValue();
    Boolean flag = Boolean.valueOf(Area.dao.deleteById(configName, Integer.valueOf(aid)));
    if (flag.booleanValue())
    {
      if (supId != 0)
      {
        Area pArea = (Area)Area.dao.findById(configName, Integer.valueOf(supId));
        if (!Area.dao.hasChildren(configName, supId))
        {
          pArea.set("node", Integer.valueOf(1)).update(configName);
          
          HashMap<String, Object> node = new HashMap();
          node.put("id", pArea.get("id"));
          node.put("pId", pArea.get("supId"));
          setAttr("pnode", node);
          
          setAttr("trel", "areaTree");
          setAttr("url", "base/area/list/" + pArea.get("supId") + "-" + supId);
          setAttr("selectedObjectId", pArea.get("id"));
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
  
  @Before({Tx.class})
  public void sort()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Area area = (Area)getModel(Area.class);
      
      int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
      area.set("supId", Integer.valueOf(supId));
      if (Area.dao.codeIsExist(configName, area.getStr("code"), 0).booleanValue())
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "新增失败,编码已存在!");
        
        renderJson();
        return;
      }
      boolean flag = false;
      

      Area pArea = (Area)Area.dao.findById(configName, Integer.valueOf(supId));
      if (!Area.dao.hasChildren(configName, supId)) {
        Area.dao.findById(configName, Integer.valueOf(supId)).set("node", Integer.valueOf(2)).update(configName);
      }
      HashMap<String, Object> node = new HashMap();
      node.put("id", pArea.get("id"));
      node.put("pId", pArea.get("supId"));
      node.put("name", pArea.get("fullName"));
      node.put("url", "base/area/list/" + pArea.get("id"));
      setAttr("pnode", node);
      

      String pids = pArea.getStr("pids");
      
      flag = area.set("createTime", new Date()).set("status", Integer.valueOf(2)).set(
        "pids", pids).save(configName);
      
      updateBasePrivs(area.getInt("id"), area.getInt("supId"), AREA_PRIVS);
      

      area.set("pids", area.get("pids") + "{" + area.getInt("id") + "}");
      area.set("rank", area.getInt("id")).update(configName);
      area.set("userId", Integer.valueOf(loginUserId()));
      if (flag)
      {
        setAttr("statusCode", Integer.valueOf(200));
        setAttr("callbackType", "closeCurrent");
        setAttr("rel", listID);
        setAttr("target", "ajax");
        setAttr("url", "base/area/list/" + pArea.get("id"));
        setAttr("trel", "areaTree");
        
        setAttr("opterate", "showLastPage");
        setAttr("selectedObjectId", area.get("id"));
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
      int aid = getParaToInt(0, Integer.valueOf(0)).intValue();
      if (Area.dao.verify(configName, "areaId", Integer.valueOf(aid)))
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "数据存在引用不可分类!");
        renderJson();
        return;
      }
      Area area = (Area)Area.dao.findById(configName, Integer.valueOf(aid));
      setAttr("area", area);
      
      setAttr("supId", Integer.valueOf(aid));
      setAttr("toAction", "sort");
      setAttr("callBack", "dialogTreeAjaxDone");
      render("dialog.html");
    }
  }
  
  public void line()
  {
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    String areaPrivs = basePrivs(AREA_PRIVS);
    
    Map<String, Object> map = Area.dao.getListPageBySupId(configName, pageNum, numPerPage, supId, listID, orderField, orderDirection, areaPrivs);
    
    setAttr("supId", Integer.valueOf(supId));
    

    columnConfig("b504");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    setAttr("node", Integer.valueOf(1));
    setAttr("goList", "line");
    
    render("list.html");
  }
  
  @Before({Tx.class})
  public void saveRank()
  {
    Map<String, Object> result = new HashMap();
    String[] ids = getPara("ids").split(",");
    String[] orders = getPara("orders").split(",");
    String configName = loginConfigName();
    
    boolean flag = true;
    for (int i = 0; i < ids.length; i++) {
      Area.dao.findById(configName, ids[i]).set("rank", orders[i]).update(configName);
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
      
      setAttr("attrId", "b_area_term_par");
      setAttr("valId", "b_area_term_val");
      setAttr("scopeId", "b_area_term_scope");
      
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("close", "b_area_filter");
      setAttr("rel", listID);
      setAttr("action", getAttr("base") + "/base/area/filter");
      
      renderJson();
    }
    else
    {
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
      String configName = loginConfigName();
      String screenPara = getPara("screenPara");
      String screenVal = getPara("screenVal");
      String scope = getPara("scope");
      String areaPrivs = basePrivs(AREA_PRIVS);
      int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
      int node = getParaToInt("node", Integer.valueOf(0)).intValue();
      
      boolean inAll = true;
      if ((StringUtils.isBlank(scope)) || ("present".equals(scope))) {
        inAll = false;
      }
      setAttr("pageMap", Area.dao.getPageByTerm(configName, pageNum, numPerPage, screenPara, screenVal, inAll, supId, node, listID, orderField, orderDirection, areaPrivs));
      setAttr("orderField", orderField);
      setAttr("orderDirection", orderDirection);
      

      columnConfig("b504");
      
      setAttr("screenPara", screenPara);
      setAttr("screenVal", screenVal);
      setAttr("scope", scope);
      
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
      
      setAttr("attrId", "b_area_term_par");
      setAttr("valId", "b_area_term_val");
      setAttr("sup", "b_area_term_supId");
      setAttr("nodeId", "b_area_term_node");
      
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("close", "b_area_search");
      setAttr("rel", listID);
      setAttr("action", getAttr("base") + "/base/area/search");
      
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
    String areaPrivs = basePrivs(AREA_PRIVS);
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    
    setAttr("pageMap", Area.dao.getPageByTerms(configName, listID, pageNum, numPerPage, searchPara, searchVal, supId, node, orderField, orderDirection, areaPrivs));
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    


    columnConfig("b504");
    
    setAttr("screenPara", searchPara);
    setAttr("screenVal", searchVal);
    

    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(node));
    setAttr("goList", "search");
    render("list.html");
  }
  
  public void stopOrStart()
  {
    String configName = loginConfigName();
    int id = getParaToInt().intValue();
    int status = Area.dao.findById(configName, Integer.valueOf(id), "status").getInt("status").intValue();
    Boolean flag = Boolean.valueOf(false);
    if (status == 2)
    {
      flag = Boolean.valueOf(Area.dao.findById(configName, getParaToInt()).set("status", Integer.valueOf(1)).update(configName));
      status = 1;
    }
    else if (status == 1)
    {
      flag = Boolean.valueOf(Area.dao.findById(configName, getParaToInt()).set("status", Integer.valueOf(2)).update(configName));
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
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    Map<String, Object> map = new HashMap();
    String areaPrivs = basePrivs(AREA_PRIVS);
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      map = Area.dao.getSupPageBySupId(configName, pageNum, numPerPage, supId, upObjectId, "", orderField, orderDirection, areaPrivs);
    }
    else
    {
      map = Area.dao.getPageBySupId(configName, pageNum, numPerPage, supId, "", orderField, orderDirection, areaPrivs);
    }
    int pSupId = 0;
    if (supId > 0) {
      pSupId = Area.dao.getPsupId(configName, supId);
    }
    String btnPattern = getPara("btnPattern", "");
    setAttr("btnPattern", btnPattern);
    

    setAttr("pageMap", map);
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("supId", Integer.valueOf(supId));
    
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(0));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    setAttr("toAction", "dialogList");
    render("option.html");
  }
  
  public void dialogSearch()
  {
    String configName = loginConfigName();
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
    String areaPrivs = basePrivs(AREA_PRIVS);
    Map<String, Object> pageMap = Area.dao.getPageDialogByTerms(configName, pageNum, numPerPage, orderField, orderDirection, map, areaPrivs);
    if ((Integer.parseInt(pageMap.get("totalCount").toString()) == 1) && (isAll != null) && (!"".equals(isAll)))
    {
      List<Area> list = (List)pageMap.get("pageList");
      Area area = (Area)list.get(0);
      
      HashMap<String, Object> result = new HashMap();
      result.put("id", area.getInt("id"));
      result.put("fullName", area.getStr("fullName"));
      result.put("code", area.getStr("code"));
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("obj", result);
      renderJson();
      return;
    }
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
  
  public void print()
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "地区信息");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    colTitle.add("地区编号");
    colTitle.add("地区全名");
    colTitle.add("拼音码");
    colTitle.add("备注");
    colTitle.add("简称");
    colTitle.add("负责人");
    colTitle.add("负责人电话");
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
    
    String areaPrivs = basePrivs(AREA_PRIVS);
    Map<String, Object> pageMap = new HashMap();
    if (node == 0) {
      pageMap = Area.dao.getPageBySupId(configName, pageNum.intValue(), numPerPage.intValue(), supId, listID, orderField, orderDirection, areaPrivs);
    } else if (node == 1) {
      pageMap = Area.dao.getListPageBySupId(configName, pageNum.intValue(), numPerPage.intValue(), supId, listID, orderField, orderDirection, areaPrivs);
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
      rowData.add(trimNull(record.get("memo")));
      rowData.add(trimNull(record.get("smallName")));
      rowData.add(trimNull(record.get("handler")));
      rowData.add(trimNull(record.get("handlerTel")));
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
