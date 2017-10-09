package com.aioerp.controller.base;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Department;
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

public class DepartmentController
  extends BaseController
{
  public static String listID = "departmentList";
  
  public void tree()
  {
    String configName = loginConfigName();
    String departmentPrivs = basePrivs(DEPARTMENT_PRIVS);
    List<Model> sorts = Department.dao.getAllSorts(configName, departmentPrivs);
    Iterator<Model> iter = sorts.iterator();
    
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model mode = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", mode.get("id"));
      node.put("pId", mode.get("supId"));
      node.put("name", mode.get("fullName"));
      node.put("url", "base/department/list/" + mode.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(-1));
    node.put("name", "部门信息");
    node.put("url", "base/department/list/0");
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
    
    Map<String, Object> map = new HashMap();
    String departmentPrivs = basePrivs(DEPARTMENT_PRIVS);
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      map = Department.dao.getSupPageBySupId(configName, pageNum, numPerPage, supId, upObjectId, listID, orderField, orderDirection, departmentPrivs);
    }
    else if ((showLastPage != null) && (showLastPage.equals("true")))
    {
      map = Department.dao.getLastPageBySupId(configName, pageNum, numPerPage, supId, listID, orderField, orderDirection, departmentPrivs);
    }
    else
    {
      map = Department.dao.getPageBySupId(configName, pageNum, numPerPage, supId, listID, orderField, orderDirection, departmentPrivs);
    }
    int pSupId = 0;
    if (supId > 0) {
      pSupId = Department.dao.getPsupId(configName, supId);
    }
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("supId", Integer.valueOf(supId));
    

    columnConfig("b505");
    
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
      Department department = (Department)getModel(Department.class);
      int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
      department.set("supId", Integer.valueOf(supId));
      String code = department.getStr("code");
      Boolean exist = Boolean.valueOf(Department.dao.codeIsExist(configName, code, 0));
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
          Department.dao.findById(configName, Integer.valueOf(supId)).set("node", Integer.valueOf(2)).update(configName);
          Department pDepartment = (Department)Department.dao.findById(configName, Integer.valueOf(supId));
          sPids = pDepartment.getStr("pids") + "{" + 
            department.getInt("supId") + "}";
        }
        else
        {
          sPids = "{0}";
        }
        flag = department.set("createTime", new Date()).set("pids", sPids).save(configName);
        
        updateBasePrivs(department.getInt("id"), department.getInt("supId"), DEPARTMENT_PRIVS);
        

        department.set("pids", department.get("pids") + "{" + department.getInt("id") + "}");
        department.set("rank", department.getInt("id")).update(configName);
        department.set("userId", Integer.valueOf(loginUserId()));
        if (flag)
        {
          setAttr("statusCode", Integer.valueOf(200));
          setAttr("rel", listID);
          if (StringUtils.isNotBlank(getPara("codeIncrement"))) {
            code = NumberUtils.increase(department.getStr("code"));
          }
          if (!getParaToBoolean("codeIncrement", Boolean.valueOf(false)).booleanValue()) {
            code = "";
          }
          String url = "/base/department/add/";
          
          String confirmType = getPara("confirmType");
          if ("dialogCloseReload".equals(confirmType))
          {
            url = "/base/department/optionAdd/";
            setAttr("supId", Integer.valueOf(supId));
          }
          setAttr("reset", "reset");
          setAttr("dialogId", "b_department_add");
          setAttr("base", getAttr("base"));
          setAttr("loadDialogUrl", url + 
            department.getInt("supId") + "-" + 
            URLEncoder.encode(code, "utf-8") + "-" + 
            getPara("codeIncrement"));
          
          setAttr("opterate", "showLastPage");
          setAttr("selectedObjectId", department.get("id"));
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
      Department department = (Department)getModel(Department.class);
      String code = department.getStr("code");
      Boolean exist = Boolean.valueOf(Department.dao.codeIsExist(configName, code, 0));
      if (exist.booleanValue())
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "新增失败,编号已存在!");
      }
      else
      {
        String oldPids = (String)department.get("pids");
        department.set("pids", oldPids.subSequence(0, oldPids.lastIndexOf("{")));
        department.set("createTime", new Date());
        
        department.set("userId", Integer.valueOf(loginUserId()));
        Boolean flag = Boolean.valueOf(department.save(configName));
        
        updateBasePrivs(department.getInt("id"), department.getInt("supId"), DEPARTMENT_PRIVS);
        

        department.set("pids", department.get("pids") + "{" + department.getInt("id") + "}");
        department.set("rank", department.getInt("id")).update(configName);
        if (flag.booleanValue())
        {
          setAttr("statusCode", Integer.valueOf(200));
          setAttr("rel", listID);
          if (StringUtils.isNotBlank(getPara("codeIncrement"))) {
            setAttr("code", NumberUtils.increase(department.getStr("code")));
          }
          setAttr("opterate", "showLastPage");
          setAttr("selectedObjectId", department.get("id"));
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
      int pid = getParaToInt(0, Integer.valueOf(0)).intValue();
      Department department = (Department)Department.dao.findById(configName, Integer.valueOf(pid));
      
      setAttr("department", department);
      setAttr("supId", department.get("supId"));
      
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
      Department department = (Department)getModel(Department.class);
      int pid = getParaToInt("pid").intValue();
      if (Department.dao.codeIsExist(configName, department.getStr("code"), pid))
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "修改失败,编码已存在!");
        
        renderJson();
        return;
      }
      department.set("id", Integer.valueOf(pid));
      int supId = department.getPsupId(configName, pid);
      department.set("userId", Integer.valueOf(loginUserId()));
      boolean flag = department.update(configName);
      if (flag)
      {
        if (department.hasChildren(configName, pid))
        {
          HashMap<String, Object> node = new HashMap();
          node.put("id", department.get("id"));
          node.put("name", department.get("fullName"));
          setAttr("node", node);
          setAttr("trel", "departmentTree");
        }
        setAttr("statusCode", Integer.valueOf(200));
        setAttr("rel", listID);
        setAttr("callbackType", "closeCurrent");
        setAttr("url", "base/department/list/" + supId);
        
        setAttr("selectedObjectId", department.get("id"));
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
      int pid = getParaToInt(0, Integer.valueOf(0)).intValue();
      Department department = (Department)Department.dao.findById(configName, Integer.valueOf(pid));
      
      setAttr("department", department);
      setAttr("toAction", "edit");
      setAttr("callBack", "updateTodoAndTee");
      render("dialog.html");
    }
  }
  
  @Before({Tx.class})
  public void delete()
  {
    int pid = getParaToInt(0, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    if (pid == 0)
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "删除失败,数据不存在");
      
      renderJson();
      return;
    }
    boolean verify = Department.dao.verify(configName, "departmentId", Integer.valueOf(pid));
    if (verify)
    {
      returnToPage(AioConstants.HTTP_RETURN300, "数据存在引用，不能删除!", "", "", "", "");
      renderJson();
      return;
    }
    Department department = (Department)Department.dao.findById(configName, Integer.valueOf(pid));
    if (Department.dao.hasChildren(configName, department.getInt("id").intValue()))
    {
      setAttr("statusCode", Integer.valueOf(300));
      setAttr("message", "删除失败,先删除下级数据");
      
      renderJson();
      return;
    }
    int supId = ((Integer)department.get("supId", Integer.valueOf(0))).intValue();
    Boolean flag = Boolean.valueOf(Department.dao.deleteById(configName, Integer.valueOf(pid)));
    if (flag.booleanValue())
    {
      if (supId != 0)
      {
        Department pDepartment = (Department)Department.dao.findById(configName, Integer.valueOf(supId));
        if (!Department.dao.hasChildren(configName, supId))
        {
          pDepartment.set("node", Integer.valueOf(1)).update(configName);
          
          HashMap<String, Object> node = new HashMap();
          node.put("id", pDepartment.get("id"));
          node.put("pId", pDepartment.get("supId"));
          setAttr("pnode", node);
          
          setAttr("trel", "departmentTree");
          setAttr("url", "base/department/list/" + pDepartment.get("supId") + "-" + supId);
          setAttr("selectedObjectId", pDepartment.get("id"));
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
      Department department = (Department)getModel(Department.class);
      
      int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
      department.set("supId", Integer.valueOf(supId));
      if (Department.dao.codeIsExist(configName, department.getStr("code"), 0))
      {
        setAttr("statusCode", Integer.valueOf(300));
        setAttr("message", "新增失败,编码已存在!");
        
        renderJson();
        return;
      }
      boolean flag = false;
      

      Department pDepartment = (Department)Department.dao.findById(configName, Integer.valueOf(supId));
      if (!Department.dao.hasChildren(configName, supId)) {
        Department.dao.findById(configName, Integer.valueOf(supId)).set("node", Integer.valueOf(2)).update(configName);
      }
      HashMap<String, Object> node = new HashMap();
      node.put("id", pDepartment.get("id"));
      node.put("pId", pDepartment.get("supId"));
      node.put("name", pDepartment.get("fullName"));
      node.put("url", "base/department/list/" + pDepartment.get("id"));
      setAttr("pnode", node);
      

      String pids = pDepartment.getStr("pids");
      
      flag = department.set("createTime", new Date()).set("status", Integer.valueOf(2)).set(
        "pids", pids).save(configName);
      
      updateBasePrivs(department.getInt("id"), department.getInt("supId"), DEPARTMENT_PRIVS);
      

      department.set("pids", department.get("pids") + "{" + department.getInt("id") + "}");
      department.set("rank", department.getInt("id")).update(configName);
      department.set("userId", Integer.valueOf(loginUserId()));
      if (flag)
      {
        setAttr("statusCode", Integer.valueOf(200));
        setAttr("callbackType", "closeCurrent");
        setAttr("rel", listID);
        setAttr("target", "ajax");
        setAttr("url", "base/department/list/" + pDepartment.get("id"));
        setAttr("trel", "departmentTree");
        
        setAttr("opterate", "showLastPage");
        setAttr("selectedObjectId", department.get("id"));
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
      int pid = getParaToInt(0, Integer.valueOf(0)).intValue();
      Department department = (Department)Department.dao.findById(configName, Integer.valueOf(pid));
      setAttr("department", department);
      
      setAttr("supId", Integer.valueOf(pid));
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
    String departmentPrivs = basePrivs(DEPARTMENT_PRIVS);
    Map<String, Object> map = Department.dao.getListPageBySupId(configName, pageNum, numPerPage, supId, listID, orderField, orderDirection, departmentPrivs);
    
    setAttr("supId", Integer.valueOf(supId));
    

    columnConfig("b505");
    
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
      Department.dao.findById(configName, ids[i]).set("rank", orders[i]).update(configName);
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
      
      setAttr("attrId", "b_department_term_par");
      setAttr("valId", "b_department_term_val");
      setAttr("scopeId", "b_department_term_scope");
      
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("close", "b_department_filter");
      setAttr("rel", listID);
      setAttr("action", getAttr("base") + "/base/department/filter");
      
      renderJson();
    }
    else
    {
      render("filter.html");
    }
  }
  
  public void filter()
  {
    String configName = loginConfigName();
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
      String departmentPrivs = basePrivs(DEPARTMENT_PRIVS);
      setAttr("pageMap", Department.dao.getPageByTerm(configName, pageNum, numPerPage, screenPara, screenVal, inAll, supId, node, listID, orderField, orderDirection, departmentPrivs));
      setAttr("orderField", orderField);
      setAttr("orderDirection", orderDirection);
      

      columnConfig("b505");
      
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
  
  public void option()
  {
    String configName = loginConfigName();
    String term = getPara("term", "");
    String termVal = getPara("termVal", "");
    String isAll = getPara("isAll", "");
    if ((term != "") && (termVal != "") && (isAll != ""))
    {
      search();
      return;
    }
    int pageNum = AioConstants.START_PAGE;
    int numPerPage = 10;
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    if (StringUtils.isNotBlank(getPara("pageNum"))) {
      pageNum = getParaToInt("pageNum").intValue();
    }
    if (StringUtils.isNotBlank(getPara("numPerPage"))) {
      numPerPage = getParaToInt("numPerPage").intValue();
    }
    String departmentPrivs = basePrivs(DEPARTMENT_PRIVS);
    if (id > 0)
    {
      Integer supId = Integer.valueOf(Department.dao.getPsupId(configName, id));
      setAttr("pSupId", supId);
      setAttr("pageMap", Department.dao.getSupPage(configName, pageNum, numPerPage, supId, id, "", "", "", null, departmentPrivs));
    }
    else
    {
      setAttr("pageMap", Department.dao.getPage(configName, "", getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue(), getParaToInt("numPerPage", Integer.valueOf(10)).intValue(), "", "", null, 0, departmentPrivs));
    }
    String btnPattern = getPara("btnPattern", "");
    setAttr("btnPattern", btnPattern);
    
    setAttr("objectId", Integer.valueOf(id));
    

    columnConfig("zj104");
    
    render("option.html");
  }
  
  public void search()
  {
    String configName = loginConfigName();
    Model model = (Model)getModel(Department.class);
    Map<String, Object> map = new HashMap();
    Integer supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0)));
    Integer upObjectId = getParaToInt(1, Integer.valueOf(0));
    String term = getPara("term");
    String termVal = getPara("termVal");
    String isAll = getPara("isAll");
    if (!StringUtils.isBlank(termVal))
    {
      map.put("term", term);
      map.put("termVal", termVal);
      
      setAttr("term", term);
      setAttr("termVal", termVal);
    }
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    
    String showLastPage = getPara("showLastPage");
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    
    Map<String, Object> pageMap = new HashMap();
    String departmentPrivs = basePrivs(DEPARTMENT_PRIVS);
    if (upObjectId.intValue() > 0)
    {
      selectedObjectId = upObjectId.intValue();
      supId = Integer.valueOf(Department.dao.getPsupId(configName, upObjectId.intValue()));
      pageMap = Department.dao.getSupPage(configName, pageNum, numPerPage, supId, upObjectId.intValue(), "", "", "", null, departmentPrivs);
    }
    else if ((showLastPage != null) && (showLastPage.equals("true")))
    {
      pageMap = Department.dao.getLastOptionBySupId(configName, pageNum, numPerPage, supId.intValue(), "", orderField, orderDirection, departmentPrivs);
    }
    else
    {
      pageMap = Department.dao.getPage(configName, "", pageNum, numPerPage, orderField, orderDirection, map, supId.intValue(), departmentPrivs);
    }
    if ((Integer.parseInt(pageMap.get("totalCount").toString()) == 1) && (isAll != null) && (!"".equals(isAll)))
    {
      List<Department> list = (List)pageMap.get("pageList");
      Department d = (Department)list.get(0);
      
      HashMap<String, Object> result = new HashMap();
      result.put("id", d.getInt("id"));
      result.put("fullName", d.getStr("fullName"));
      result.put("code", d.getStr("code"));
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("obj", result);
      renderJson();
      return;
    }
    columnConfig("zj104");
    

    String btnPattern = getPara("btnPattern", "");
    setAttr("btnPattern", btnPattern);
    
    setAttr("pageMap", pageMap);
    
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("depm", model);
    setAttr("supId", supId);
    
    setAttr("pSupId", Integer.valueOf(Department.dao.getPsupId(configName, supId.intValue())));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    render("option.html");
  }
  
  public void toSearch()
  {
    if (isPost())
    {
      setAttr("supId", getParaToInt("supId", Integer.valueOf(0)));
      setAttr("node", getParaToInt("node", Integer.valueOf(0)));
      setAttr("searchPara", getPara("searchPara", ""));
      setAttr("searchVal", getPara("searchVal", ""));
      
      setAttr("attrId", "b_department_term_par");
      setAttr("valId", "b_department_term_val");
      setAttr("sup", "b_department_term_supId");
      setAttr("nodeId", "b_department_term_node");
      
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("close", "b_department_search");
      setAttr("rel", listID);
      setAttr("action", getAttr("base") + "/base/department/searchDepartment");
      
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
  
  public void searchDepartment()
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
    String departmentPrivs = basePrivs(DEPARTMENT_PRIVS);
    setAttr("pageMap", Department.dao.getPageByTerms(configName, listID, pageNum, numPerPage, searchPara, searchVal, supId, node, orderField, orderDirection, departmentPrivs));
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    columnConfig("b505");
    
    setAttr("screenPara", searchPara);
    setAttr("screenVal", searchVal);
    

    setAttr("supId", Integer.valueOf(supId));
    setAttr("node", Integer.valueOf(node));
    setAttr("goList", "searchDepartment");
    render("list.html");
  }
  
  public void stopOrStart()
  {
    String configName = loginConfigName();
    int id = getParaToInt().intValue();
    int status = Department.dao.findById(configName, Integer.valueOf(id), "status").getInt("status").intValue();
    Boolean flag = Boolean.valueOf(false);
    if (status == 2)
    {
      flag = Boolean.valueOf(Department.dao.findById(configName, getParaToInt()).set("status", Integer.valueOf(1)).update(configName));
      status = 1;
    }
    else if (status == 1)
    {
      flag = Boolean.valueOf(Department.dao.findById(configName, getParaToInt()).set("status", Integer.valueOf(2)).update(configName));
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
    setAttr("callBack", "aioDialogReloadOption");
    render("dialog.html");
  }
  
  public void print()
  {
    Map<String, Object> data = new HashMap();
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "部门信息");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    colTitle.add("部门编号");
    colTitle.add("部门全名");
    colTitle.add("拼音码");
    colTitle.add("备注");
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
    
    String departmentPrivs = basePrivs(DEPARTMENT_PRIVS);
    Map<String, Object> pageMap = new HashMap();
    if (node == 0) {
      pageMap = Department.dao.getPageBySupId(configName, pageNum.intValue(), numPerPage.intValue(), supId, listID, orderField, orderDirection, departmentPrivs);
    } else if (node == 1) {
      pageMap = Department.dao.getListPageBySupId(configName, pageNum.intValue(), numPerPage.intValue(), supId, listID, orderField, orderDirection, departmentPrivs);
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
