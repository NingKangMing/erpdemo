package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Area;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.sys.User;
import com.jfinal.plugin.activerecord.Model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class BasePermissionController
  extends BaseController
{
  public void index()
  {
    String configName = loginConfigName();
    
    initProduct(ALL_PRIVS);
    
    initUnit(ALL_PRIVS);
    
    initArea(ALL_PRIVS);
    
    initStorage(ALL_PRIVS);
    
    initAccount(ALL_PRIVS);
    
    initStaff(ALL_PRIVS);
    
    initDepartment(ALL_PRIVS);
    Model user = User.dao.getOne(configName);
    if (user != null) {
      setAttr("userId", user.getInt("id"));
    }
    setAttr("baseType", "product");
    render("page.html");
  }
  
  public void base()
  {
    String configName = loginConfigName();
    int userId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String type = getPara(1, "product");
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    String productPrivs = user.getStr("productPrivs");
    String unitPrivs = user.getStr("unitPrivs");
    String storagePrivs = user.getStr("storagePrivs");
    String accountPrivs = user.getStr("accountPrivs");
    String areaPrivs = user.getStr("areaPrivs");
    String staffPrivs = user.getStr("staffPrivs");
    String departmentPrivs = user.getStr("departmentPrivs");
    
    initProduct(productPrivs);
    
    initUnit(unitPrivs);
    
    initStorage(storagePrivs);
    
    initAccount(accountPrivs);
    
    initArea(areaPrivs);
    
    initStaff(staffPrivs);
    
    initDepartment(departmentPrivs);
    setAttr("baseType", type);
    setAttr("userId", Integer.valueOf(userId));
    render("base.html");
  }
  
  public void tree()
  {
    String configName = loginConfigName();
    List<Model> userList = User.dao.getList(configName);
    Iterator<Model> iter = userList.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model<Staff> user = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", user.get("id"));
      node.put("pId", Integer.valueOf(0));
      node.put("name", user.get("username"));
      node.put("url", "sys/basePermission/base/" + user.get("id") + "-{baseType}");
      nodeList.add(node);
    }
    renderJson(nodeList);
  }
  
  public void product()
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    if (supId == 0) {
      supId = getParaToInt("pSupId", Integer.valueOf(0)).intValue();
    }
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int userId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (userId == 0) {
      userId = getParaToInt("userId", Integer.valueOf(0)).intValue();
    }
    int objectId = getParaToInt("objectId", Integer.valueOf(0)).intValue();
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    String productPrivs = "";
    if (user != null) {
      productPrivs = user.getStr("productPrivs");
    }
    if (supId == -1)
    {
      initProduct(productPrivs);
      setAttr("pSupId", Integer.valueOf(-1));
    }
    else
    {
      Map<String, Object> productPageMap = Product.dao.getPageByCondtion(configName, ALL_PRIVS, "productPermissionList", supId, null, null, null, pageNum, numPerPage, orderField, orderDirection);
      List<Model> pageList = (List)productPageMap.get("pageList");
      for (Model model : pageList)
      {
        int id = model.getInt("id").intValue();
        if (((StringUtils.isNotBlank(productPrivs)) && (productPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_1)) || (ALL_PRIVS.equals(productPrivs)))
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
        }
        else if ((StringUtils.isNotBlank(productPrivs)) && (productPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_2))
        {
          String idStr = Product.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, id);
          String[] ids = idStr.split(",");
          boolean include = true;
          for (String i : ids) {
            if (productPrivs.indexOf("," + i + ",") == -1)
            {
              include = false;
              break;
            }
          }
          if (include) {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
          } else {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
          }
        }
        else
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
        }
      }
      setAttr("productPageMap", productPageMap);
      Model sup = Product.dao.findById(configName, Integer.valueOf(supId));
      if (sup != null) {
        setAttr("pSupId", sup.getInt("supId"));
      } else {
        setAttr("pSupId", Integer.valueOf(-1));
      }
    }
    setAttr("objectId", Integer.valueOf(objectId));
    setAttr("supId", Integer.valueOf(supId));
    setAttr("userId", Integer.valueOf(userId));
    render("productList.html");
  }
  
  public void unit()
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    if (supId == 0) {
      supId = getParaToInt("pSupId", Integer.valueOf(0)).intValue();
    }
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int userId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (userId == 0) {
      userId = getParaToInt("userId", Integer.valueOf(0)).intValue();
    }
    int objectId = getParaToInt("objectId", Integer.valueOf(0)).intValue();
    
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    String unitPrivs = "";
    if (user != null) {
      unitPrivs = user.getStr("unitPrivs");
    }
    if (supId == -1)
    {
      initUnit(unitPrivs);
      setAttr("pSupId", Integer.valueOf(-1));
    }
    else
    {
      Map<String, Object> unitPageMap = Unit.dao.getPageByCondtion(configName, ALL_PRIVS, ALL_PRIVS, ALL_PRIVS, "unitPermissionList", supId, null, null, null, null, pageNum, numPerPage, orderField, orderDirection, AioConstants.STATUS_DELETE);
      List<Model> pageList = (List)unitPageMap.get("pageList");
      for (Model model : pageList)
      {
        int id = model.getInt("id").intValue();
        if (((StringUtils.isNotBlank(unitPrivs)) && (unitPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_1)) || (ALL_PRIVS.equals(unitPrivs)))
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
        }
        else if ((StringUtils.isNotBlank(unitPrivs)) && (unitPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_2))
        {
          String idStr = Unit.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, id);
          String[] ids = idStr.split(",");
          boolean include = true;
          for (String i : ids) {
            if (unitPrivs.indexOf("," + i + ",") == -1)
            {
              include = false;
              break;
            }
          }
          if (include) {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
          } else {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
          }
        }
        else
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
        }
      }
      setAttr("unitPageMap", unitPageMap);
      
      Model sup = Unit.dao.findById(configName, Integer.valueOf(supId));
      if (sup != null) {
        setAttr("pSupId", sup.getInt("supId"));
      } else {
        setAttr("pSupId", Integer.valueOf(-1));
      }
    }
    setAttr("supId", Integer.valueOf(supId));
    setAttr("userId", Integer.valueOf(userId));
    setAttr("objectId", Integer.valueOf(objectId));
    render("unitList.html");
  }
  
  public void storage()
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    if (supId == 0) {
      supId = getParaToInt("pSupId", Integer.valueOf(0)).intValue();
    }
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int userId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (userId == 0) {
      userId = getParaToInt("userId", Integer.valueOf(0)).intValue();
    }
    int objectId = getParaToInt("objectId", Integer.valueOf(0)).intValue();
    
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    String storagePrivs = "";
    if (user != null) {
      storagePrivs = user.getStr("storagePrivs");
    }
    if (supId == -1)
    {
      initStorage(storagePrivs);
      setAttr("pSupId", Integer.valueOf(-1));
    }
    else
    {
      Map<String, Object> storagePageMap = Storage.dao.getPageStorageBySupId(configName, "storagePermissionList", pageNum, numPerPage, supId, orderField, orderDirection, ALL_PRIVS);
      List<Model> pageList = (List)storagePageMap.get("pageList");
      for (Model model : pageList)
      {
        int id = model.getInt("id").intValue();
        if (((StringUtils.isNotBlank(storagePrivs)) && (storagePrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_1)) || (ALL_PRIVS.equals(storagePrivs)))
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
        }
        else if ((StringUtils.isNotBlank(storagePrivs)) && (storagePrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_2))
        {
          String idStr = Storage.dao.getAllChildIdsBySupId(configName, Integer.valueOf(id), ALL_PRIVS);
          String[] ids = idStr.split(",");
          boolean include = true;
          for (String i : ids) {
            if (storagePrivs.indexOf("," + i + ",") == -1)
            {
              include = false;
              break;
            }
          }
          if (include) {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
          } else {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
          }
        }
        else
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
        }
      }
      setAttr("storagePageMap", storagePageMap);
      
      Model sup = Storage.dao.findById(configName, Integer.valueOf(supId));
      if (sup != null) {
        setAttr("pSupId", sup.getInt("supId"));
      } else {
        setAttr("pSupId", Integer.valueOf(-1));
      }
    }
    setAttr("supId", Integer.valueOf(supId));
    setAttr("userId", Integer.valueOf(userId));
    setAttr("objectId", Integer.valueOf(objectId));
    render("storageList.html");
  }
  
  public void account()
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    if (supId == 0) {
      supId = getParaToInt("pSupId", Integer.valueOf(0)).intValue();
    }
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int userId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (userId == 0) {
      userId = getParaToInt("userId", Integer.valueOf(0)).intValue();
    }
    int objectId = getParaToInt("objectId", Integer.valueOf(0)).intValue();
    
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    String accountPrivs = "";
    if (user != null) {
      accountPrivs = user.getStr("accountPrivs");
    }
    if (supId == -1)
    {
      initAccount(accountPrivs);
      setAttr("pSupId", Integer.valueOf(-1));
    }
    else
    {
      Map<String, Object> accountPageMap = Accounts.dao.getPageByCondtion(configName, ALL_PRIVS, "accountPermissionList", supId, null, null, null, pageNum, numPerPage, orderField, orderDirection);
      List<Model> pageList = (List)accountPageMap.get("pageList");
      for (Model model : pageList)
      {
        int id = model.getInt("id").intValue();
        if (((StringUtils.isNotBlank(accountPrivs)) && (accountPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_1)) || (ALL_PRIVS.equals(accountPrivs)))
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
        }
        else if ((StringUtils.isNotBlank(accountPrivs)) && (accountPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_2))
        {
          String idStr = Accounts.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, Integer.valueOf(id));
          String[] ids = idStr.split(",");
          boolean include = true;
          for (String i : ids) {
            if (accountPrivs.indexOf("," + i + ",") == -1)
            {
              include = false;
              break;
            }
          }
          if (include) {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
          } else {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
          }
        }
        else
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
        }
      }
      setAttr("accountPageMap", accountPageMap);
      Model sup = Accounts.dao.findById(configName, Integer.valueOf(supId));
      if (sup != null) {
        setAttr("pSupId", sup.getInt("supId"));
      } else {
        setAttr("pSupId", Integer.valueOf(-1));
      }
    }
    setAttr("supId", Integer.valueOf(supId));
    setAttr("userId", Integer.valueOf(userId));
    setAttr("objectId", Integer.valueOf(objectId));
    render("accountList.html");
  }
  
  public void area()
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    if (supId == 0) {
      supId = getParaToInt("pSupId", Integer.valueOf(0)).intValue();
    }
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int userId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (userId == 0) {
      userId = getParaToInt("userId", Integer.valueOf(0)).intValue();
    }
    int objectId = getParaToInt("objectId", Integer.valueOf(0)).intValue();
    
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    String areaPrivs = "";
    if (user != null) {
      areaPrivs = user.getStr("areaPrivs");
    }
    if (supId == -1)
    {
      initArea(areaPrivs);
      setAttr("pSupId", Integer.valueOf(-1));
    }
    else
    {
      Map<String, Object> areaPageMap = Area.dao.getPageBySupId(configName, pageNum, numPerPage, supId, "areaPermissionList", orderField, orderDirection, ALL_PRIVS);
      List<Model> pageList = (List)areaPageMap.get("pageList");
      for (Model model : pageList)
      {
        int id = model.getInt("id").intValue();
        if (((StringUtils.isNotBlank(areaPrivs)) && (areaPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_1)) || (ALL_PRIVS.equals(areaPrivs)))
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
        }
        else if ((StringUtils.isNotBlank(areaPrivs)) && (areaPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_2))
        {
          String idStr = Area.dao.getAllChildIdsBySupId(configName, Integer.valueOf(id));
          String[] ids = idStr.split(",");
          boolean include = true;
          for (String i : ids) {
            if (areaPrivs.indexOf("," + i + ",") == -1)
            {
              include = false;
              break;
            }
          }
          if (include) {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
          } else {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
          }
        }
        else
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
        }
      }
      setAttr("areaPageMap", areaPageMap);
      Model sup = Area.dao.findById(configName, Integer.valueOf(supId));
      if (sup != null) {
        setAttr("pSupId", sup.getInt("supId"));
      } else {
        setAttr("pSupId", Integer.valueOf(-1));
      }
    }
    setAttr("supId", Integer.valueOf(supId));
    setAttr("userId", Integer.valueOf(userId));
    setAttr("objectId", Integer.valueOf(objectId));
    render("areaList.html");
  }
  
  public void staff()
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    if (supId == 0) {
      supId = getParaToInt("pSupId", Integer.valueOf(0)).intValue();
    }
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    int userId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (userId == 0) {
      userId = getParaToInt("userId", Integer.valueOf(0)).intValue();
    }
    int objectId = getParaToInt("objectId", Integer.valueOf(0)).intValue();
    
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    String staffPrivs = "";
    if (user != null) {
      staffPrivs = user.getStr("staffPrivs");
    }
    if (supId == -1)
    {
      initStaff(staffPrivs);
      setAttr("pSupId", Integer.valueOf(-1));
    }
    else
    {
      Map<String, Object> staffPageMap = Staff.dao.getLastPage(configName, pageNum, numPerPage, supId, "staffPermissionList", orderField, orderDirection, ALL_PRIVS);
      List<Model> pageList = (List)staffPageMap.get("pageList");
      for (Model model : pageList)
      {
        int id = model.getInt("id").intValue();
        if (((StringUtils.isNotBlank(staffPrivs)) && (staffPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_1)) || (ALL_PRIVS.equals(staffPrivs)))
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
        }
        else if ((StringUtils.isNotBlank(staffPrivs)) && (staffPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_2))
        {
          String idStr = Staff.dao.getAllChildIdsBySupId(configName, Integer.valueOf(id));
          String[] ids = idStr.split(",");
          boolean include = true;
          for (String i : ids) {
            if (staffPrivs.indexOf("," + i + ",") == -1)
            {
              include = false;
              break;
            }
          }
          if (include) {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
          } else {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
          }
        }
        else
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
        }
      }
      setAttr("staffPageMap", staffPageMap);
      Model sup = Staff.dao.findById(configName, Integer.valueOf(supId));
      if (sup != null) {
        setAttr("pSupId", sup.getInt("supId"));
      } else {
        setAttr("pSupId", Integer.valueOf(-1));
      }
    }
    setAttr("supId", Integer.valueOf(supId));
    setAttr("userId", Integer.valueOf(userId));
    setAttr("objectId", Integer.valueOf(objectId));
    render("staffList.html");
  }
  
  public void department()
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    if (supId == 0) {
      supId = getParaToInt("pSupId", Integer.valueOf(0)).intValue();
    }
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int userId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (userId == 0) {
      userId = getParaToInt("userId", Integer.valueOf(0)).intValue();
    }
    int objectId = getParaToInt("objectId", Integer.valueOf(0)).intValue();
    
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    String departmentPrivs = "";
    if (user != null) {
      departmentPrivs = user.getStr("departmentPrivs");
    }
    if (supId == -1)
    {
      initDepartment(departmentPrivs);
      setAttr("pSupId", Integer.valueOf(-1));
    }
    else
    {
      Map<String, Object> departmentPageMap = Department.dao.getPageBySupId(configName, pageNum, numPerPage, supId, "departmentPermissionList", orderField, orderDirection, ALL_PRIVS);
      List<Model> pageList = (List)departmentPageMap.get("pageList");
      for (Model model : pageList)
      {
        int id = model.getInt("id").intValue();
        if (((StringUtils.isNotBlank(departmentPrivs)) && (departmentPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_1)) || (ALL_PRIVS.equals(departmentPrivs)))
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
        }
        else if ((StringUtils.isNotBlank(departmentPrivs)) && (departmentPrivs.indexOf("," + id + ",") != -1) && (model.getInt("node").intValue() == AioConstants.NODE_2))
        {
          String idStr = Department.dao.getAllChildIdsBySupId(configName, Integer.valueOf(id));
          String[] ids = idStr.split(",");
          boolean include = true;
          for (String i : ids) {
            if (departmentPrivs.indexOf("," + i + ",") == -1)
            {
              include = false;
              break;
            }
          }
          if (include) {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
          } else {
            model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
          }
        }
        else
        {
          model.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
        }
      }
      setAttr("departmentPageMap", departmentPageMap);
      Model sup = Department.dao.findById(configName, Integer.valueOf(supId));
      if (sup != null) {
        setAttr("pSupId", sup.getInt("supId"));
      } else {
        setAttr("pSupId", Integer.valueOf(-1));
      }
    }
    setAttr("supId", Integer.valueOf(supId));
    setAttr("userId", Integer.valueOf(userId));
    setAttr("objectId", Integer.valueOf(objectId));
    render("departmentList.html");
  }
  
  public void allChoose()
  {
    String configName = loginConfigName();
    int userId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String baseType = getPara(1, "product");
    int baseId = getParaToInt(2, Integer.valueOf(0)).intValue();
    
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    if (user != null)
    {
      if (1 != user.getInt("grade").intValue())
      {
        returnToPage(AioConstants.HTTP_RETURN300, "不能修改系统管理员权限!", "", "", "", "");
        renderJson();
      }
    }
    else
    {
      returnToPage(AioConstants.HTTP_RETURN300, "修改用户权限失败!", "", "", "", "");
      renderJson();
      return;
    }
    int supId = -1;
    if ("unit".equals(baseType))
    {
      Model unit = Unit.dao.findById(configName, Integer.valueOf(baseId));
      if (unit != null) {
        supId = unit.getInt("supId").intValue();
      }
      user.set("unitPrivs", ALL_PRIVS);
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "unitPermissionList");
      setAttr("url", "sys/basePermission/unit?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("storage".equals(baseType))
    {
      Model storage = Storage.dao.findById(configName, Integer.valueOf(baseId));
      if (storage != null) {
        supId = storage.getInt("supId").intValue();
      }
      user.set("storagePrivs", ALL_PRIVS);
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "storagePermissionList");
      setAttr("url", "sys/basePermission/storage?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("account".equals(baseType))
    {
      Model account = Accounts.dao.findById(configName, Integer.valueOf(baseId));
      if (account != null) {
        supId = account.getInt("supId").intValue();
      }
      user.set("accountPrivs", ALL_PRIVS);
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "accountPermissionList");
      setAttr("url", "sys/basePermission/account?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("area".equals(baseType))
    {
      Model area = Area.dao.findById(configName, Integer.valueOf(baseId));
      if (area != null) {
        supId = area.getInt("supId").intValue();
      }
      user.set("areaPrivs", ALL_PRIVS);
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "areaPermissionList");
      setAttr("url", "sys/basePermission/area?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("staff".equals(baseType))
    {
      Model staff = Staff.dao.findById(configName, Integer.valueOf(baseId));
      if (staff != null) {
        supId = staff.getInt("supId").intValue();
      }
      user.set("staffPrivs", ALL_PRIVS);
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "staffPermissionList");
      setAttr("url", "sys/basePermission/staff?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("department".equals(baseType))
    {
      Model department = Department.dao.findById(configName, Integer.valueOf(baseId));
      if (department != null) {
        supId = department.getInt("supId").intValue();
      }
      user.set("departmentPrivs", ALL_PRIVS);
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "departmentPermissionList");
      setAttr("url", "sys/basePermission/department?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else
    {
      Model product = Product.dao.findById(configName, Integer.valueOf(baseId));
      if (product != null) {
        supId = product.getInt("supId").intValue();
      }
      user.set("productPrivs", ALL_PRIVS);
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "productPermissionList");
      setAttr("url", "sys/basePermission/product?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    user.update(configName);
    renderJson();
  }
  
  public void choose()
  {
    String configName = loginConfigName();
    int userId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String baseType = getPara(1, "product");
    int baseId = getParaToInt(2, Integer.valueOf(0)).intValue();
    
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    if (user != null)
    {
      if (1 != user.getInt("grade").intValue())
      {
        returnToPage(AioConstants.HTTP_RETURN300, "不能修改系统管理员权限!", "", "", "", "");
        renderJson();
      }
    }
    else
    {
      returnToPage(AioConstants.HTTP_RETURN300, "修改用户权限失败!", "", "", "", "");
      renderJson();
      return;
    }
    String productPrivs = user.getStr("productPrivs");
    String unitPrivs = user.getStr("unitPrivs");
    String storagePrivs = user.getStr("storagePrivs");
    String accountPrivs = user.getStr("accountPrivs");
    String areaPrivs = user.getStr("areaPrivs");
    String staffPrivs = user.getStr("staffPrivs");
    String departmentPrivs = user.getStr("departmentPrivs");
    int supId = -1;
    if ("unit".equals(baseType))
    {
      Model unit = Unit.dao.findById(configName, Integer.valueOf(baseId));
      if (unit != null) {
        supId = unit.getInt("supId").intValue();
      }
      if (baseId == 0)
      {
        if ((StringUtils.isNotBlank(unitPrivs)) && (ALL_PRIVS.equals(unitPrivs))) {
          user.set("unitPrivs", "");
        } else {
          user.set("unitPrivs", ALL_PRIVS);
        }
      }
      else if ((StringUtils.isNotBlank(unitPrivs)) && (ALL_PRIVS.equals(unitPrivs)))
      {
        String unitids = Unit.dao.getAllChildIdsNoSupId(configName, ALL_PRIVS, baseId);
        if (!unitids.endsWith(",")) {
          unitids = "," + unitids + ",";
        }
        user.set("unitPrivs", unitids);
      }
      else if ((StringUtils.isNotBlank(unitPrivs)) && (!ALL_PRIVS.equals(unitPrivs)))
      {
        if (unitPrivs.indexOf("," + baseId + ",") == -1)
        {
          String pids = unit.getStr("pids").replace("{", "").replace("}", ",");
          String unitids = Unit.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, baseId);
          if (StringUtils.isNotBlank(unitids)) {
            pids = pids + unitids;
          }
          if (!pids.endsWith(",")) {
            pids = pids + ",";
          }
          String[] ids = pids.split(",");
          for (String id : ids)
          {
            unitPrivs = unitPrivs.replace("," + id + ",", ",");
            unitPrivs = unitPrivs.replace("," + id + ",", ",");
          }
          unitPrivs = unitPrivs + pids;
          unitPrivs = unitPrivs.replace(",0,", ",");
          user.set("unitPrivs", unitPrivs);
        }
        else
        {
          String idsStr = baseId + ",";
          String unitids = Unit.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, baseId);
          if (StringUtils.isNotBlank(unitids)) {
            idsStr = idsStr + unitids;
          }
          String[] ids = idsStr.split(",");
          for (String id : ids)
          {
            unitPrivs = unitPrivs.replace("," + id + ",", ",");
            unitPrivs = unitPrivs.replace("," + id + ",", ",");
          }
          if (",".equals(unitPrivs)) {
            unitPrivs = "";
          }
          user.set("unitPrivs", unitPrivs);
        }
      }
      else
      {
        String pids = unit.getStr("pids").replace("{", "").replace("}", ",");
        String unitids = Unit.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, baseId);
        if (StringUtils.isNotBlank(unitids)) {
          pids = pids + unitids;
        }
        if (!pids.startsWith(",")) {
          pids = "," + pids;
        }
        if (!pids.endsWith(",")) {
          pids = pids + ",";
        }
        pids = pids.replace(",0,", ",");
        user.set("unitPrivs", pids);
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "unitPermissionList");
      setAttr("url", "sys/basePermission/unit?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("storage".equals(baseType))
    {
      Model storage = Storage.dao.findById(configName, Integer.valueOf(baseId));
      if (storage != null) {
        supId = storage.getInt("supId").intValue();
      }
      if (baseId == 0)
      {
        if ((StringUtils.isNotBlank(storagePrivs)) && (ALL_PRIVS.equals(storagePrivs))) {
          user.set("storagePrivs", "");
        } else {
          user.set("storagePrivs", ALL_PRIVS);
        }
      }
      else if ((StringUtils.isNotBlank(storagePrivs)) && (ALL_PRIVS.equals(storagePrivs)))
      {
        String storageids = Storage.dao.getAllChildIdsNoSupId(configName, Integer.valueOf(baseId), ALL_PRIVS);
        if (!storageids.endsWith(",")) {
          storageids = "," + storageids + ",";
        }
        user.set("storagePrivs", storageids);
      }
      else if ((StringUtils.isNotBlank(storagePrivs)) && (!ALL_PRIVS.equals(storagePrivs)))
      {
        if (storagePrivs.indexOf("," + baseId + ",") == -1)
        {
          String pids = storage.getStr("pids").replace("{", "").replace("}", ",");
          String storageids = Storage.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId), ALL_PRIVS);
          if (StringUtils.isNotBlank(storageids)) {
            pids = pids + storageids;
          }
          if (!pids.endsWith(",")) {
            pids = pids + ",";
          }
          String[] ids = pids.split(",");
          for (String id : ids)
          {
            storagePrivs = storagePrivs.replace("," + id + ",", ",");
            storagePrivs = storagePrivs.replace("," + id + ",", ",");
          }
          storagePrivs = storagePrivs + pids;
          storagePrivs = storagePrivs.replace(",0,", ",");
          user.set("storagePrivs", storagePrivs);
        }
        else
        {
          String idsStr = baseId + ",";
          String storageids = Storage.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId), ALL_PRIVS);
          if (StringUtils.isNotBlank(storageids)) {
            idsStr = idsStr + storageids;
          }
          String[] ids = idsStr.split(",");
          for (String id : ids)
          {
            storagePrivs = storagePrivs.replace("," + id + ",", ",");
            storagePrivs = storagePrivs.replace("," + id + ",", ",");
          }
          if (",".equals(storagePrivs)) {
            storagePrivs = "";
          }
          user.set("storagePrivs", storagePrivs);
        }
      }
      else
      {
        String pids = storage.getStr("pids").replace("{", "").replace("}", ",");
        String storageids = Storage.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId), ALL_PRIVS);
        if (StringUtils.isNotBlank(storageids)) {
          pids = pids + storageids;
        }
        if (!pids.startsWith(",")) {
          pids = "," + pids;
        }
        if (!pids.endsWith(",")) {
          pids = pids + ",";
        }
        pids = pids.replace(",0,", ",");
        user.set("storagePrivs", pids);
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "storagePermissionList");
      setAttr("url", "sys/basePermission/storage?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("account".equals(baseType))
    {
      Model account = Accounts.dao.findById(configName, Integer.valueOf(baseId));
      if (account != null) {
        supId = account.getInt("supId").intValue();
      }
      if (baseId == 0)
      {
        if ((StringUtils.isNotBlank(accountPrivs)) && (ALL_PRIVS.equals(accountPrivs))) {
          user.set("accountPrivs", "");
        } else {
          user.set("accountPrivs", ALL_PRIVS);
        }
      }
      else if ((StringUtils.isNotBlank(accountPrivs)) && (ALL_PRIVS.equals(accountPrivs)))
      {
        String accountids = Accounts.dao.getAllChildIdsNoSupId(configName, ALL_PRIVS, Integer.valueOf(baseId));
        if (!accountids.endsWith(",")) {
          accountids = "," + accountids + ",";
        }
        user.set("accountPrivs", accountids);
      }
      else if ((StringUtils.isNotBlank(accountPrivs)) && (!ALL_PRIVS.equals(accountPrivs)))
      {
        if (accountPrivs.indexOf("," + baseId + ",") == -1)
        {
          String pids = account.getStr("pids").replace("{", "").replace("}", ",");
          String accountids = Accounts.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, Integer.valueOf(baseId));
          if (StringUtils.isNotBlank(accountids)) {
            pids = pids + accountids;
          }
          if (!pids.endsWith(",")) {
            pids = pids + ",";
          }
          String[] ids = pids.split(",");
          for (String id : ids)
          {
            accountPrivs = accountPrivs.replace("," + id + ",", ",");
            accountPrivs = accountPrivs.replace("," + id + ",", ",");
          }
          accountPrivs = accountPrivs + pids;
          accountPrivs = accountPrivs.replace(",0,", ",");
          user.set("accountPrivs", accountPrivs);
        }
        else
        {
          String idsStr = baseId + ",";
          String accountids = Accounts.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, Integer.valueOf(baseId));
          if (StringUtils.isNotBlank(accountids)) {
            idsStr = idsStr + accountids;
          }
          String[] ids = idsStr.split(",");
          for (String id : ids)
          {
            accountPrivs = accountPrivs.replace("," + id + ",", ",");
            accountPrivs = accountPrivs.replace("," + id + ",", ",");
          }
          if (",".equals(accountPrivs)) {
            accountPrivs = "";
          }
          user.set("accountPrivs", accountPrivs);
        }
      }
      else
      {
        String pids = account.getStr("pids").replace("{", "").replace("}", ",");
        String accountids = Accounts.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, Integer.valueOf(baseId));
        if (StringUtils.isNotBlank(accountids)) {
          pids = pids + accountids;
        }
        if (!pids.startsWith(",")) {
          pids = "," + pids;
        }
        if (!pids.endsWith(",")) {
          pids = pids + ",";
        }
        pids = pids.replace(",0,", ",");
        user.set("accountPrivs", pids);
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "accountPermissionList");
      setAttr("url", "sys/basePermission/account?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("area".equals(baseType))
    {
      Model area = Area.dao.findById(configName, Integer.valueOf(baseId));
      if (area != null) {
        supId = area.getInt("supId").intValue();
      }
      if (baseId == 0)
      {
        if ((StringUtils.isNotBlank(areaPrivs)) && (ALL_PRIVS.equals(areaPrivs))) {
          user.set("areaPrivs", "");
        } else {
          user.set("areaPrivs", ALL_PRIVS);
        }
      }
      else if ((StringUtils.isNotBlank(areaPrivs)) && (ALL_PRIVS.equals(areaPrivs)))
      {
        String areaids = Area.dao.getAllChildIdsNoSupId(configName, Integer.valueOf(baseId));
        if (!areaids.endsWith(",")) {
          areaids = "," + areaids + ",";
        }
        user.set("areaPrivs", areaids);
      }
      else if ((StringUtils.isNotBlank(areaPrivs)) && (!ALL_PRIVS.equals(areaPrivs)))
      {
        if (areaPrivs.indexOf("," + baseId + ",") == -1)
        {
          String pids = area.getStr("pids").replace("{", "").replace("}", ",");
          String areaids = Area.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId));
          if (StringUtils.isNotBlank(areaids)) {
            pids = pids + areaids;
          }
          if (!pids.endsWith(",")) {
            pids = pids + ",";
          }
          String[] ids = pids.split(",");
          for (String id : ids)
          {
            areaPrivs = areaPrivs.replace("," + id + ",", ",");
            areaPrivs = areaPrivs.replace("," + id + ",", ",");
          }
          areaPrivs = areaPrivs + pids;
          areaPrivs = areaPrivs.replace(",0,", ",");
          user.set("areaPrivs", areaPrivs);
        }
        else
        {
          String idsStr = baseId + ",";
          String areaids = Area.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId));
          if (StringUtils.isNotBlank(areaids)) {
            idsStr = idsStr + areaids;
          }
          String[] ids = idsStr.split(",");
          for (String id : ids)
          {
            areaPrivs = areaPrivs.replace("," + id + ",", ",");
            areaPrivs = areaPrivs.replace("," + id + ",", ",");
          }
          if (",".equals(areaPrivs)) {
            areaPrivs = "";
          }
          user.set("areaPrivs", areaPrivs);
        }
      }
      else
      {
        String pids = area.getStr("pids").replace("{", "").replace("}", ",");
        String areaids = Area.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId));
        if (StringUtils.isNotBlank(areaids)) {
          pids = pids + areaids;
        }
        if (!pids.startsWith(",")) {
          pids = "," + pids;
        }
        if (!pids.endsWith(",")) {
          pids = pids + ",";
        }
        pids = pids.replace(",0,", ",");
        user.set("areaPrivs", pids);
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "areaPermissionList");
      setAttr("url", "sys/basePermission/area?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("staff".equals(baseType))
    {
      Model staff = Staff.dao.findById(configName, Integer.valueOf(baseId));
      if (staff != null) {
        supId = staff.getInt("supId").intValue();
      }
      if (baseId == 0)
      {
        if ((StringUtils.isNotBlank(staffPrivs)) && (ALL_PRIVS.equals(staffPrivs))) {
          user.set("staffPrivs", "");
        } else {
          user.set("staffPrivs", ALL_PRIVS);
        }
      }
      else if ((StringUtils.isNotBlank(staffPrivs)) && (ALL_PRIVS.equals(staffPrivs)))
      {
        String staffids = Staff.dao.getAllChildIdsNoSupId(configName, Integer.valueOf(baseId), ALL_PRIVS);
        if (!staffids.endsWith(",")) {
          staffids = "," + staffids + ",";
        }
        user.set("staffPrivs", staffids);
      }
      else if ((StringUtils.isNotBlank(staffPrivs)) && (!ALL_PRIVS.equals(staffPrivs)))
      {
        if (staffPrivs.indexOf("," + baseId + ",") == -1)
        {
          String pids = staff.getStr("pids").replace("{", "").replace("}", ",");
          String staffids = Staff.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId));
          if (StringUtils.isNotBlank(staffids)) {
            pids = pids + staffids;
          }
          if (!pids.endsWith(",")) {
            pids = pids + ",";
          }
          String[] ids = pids.split(",");
          for (String id : ids)
          {
            staffPrivs = staffPrivs.replace("," + id + ",", ",");
            staffPrivs = staffPrivs.replace("," + id + ",", ",");
          }
          staffPrivs = staffPrivs + pids;
          staffPrivs = staffPrivs.replace(",0,", ",");
          user.set("staffPrivs", staffPrivs);
        }
        else
        {
          String idsStr = baseId + ",";
          String staffids = Staff.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId));
          if (StringUtils.isNotBlank(staffids)) {
            idsStr = idsStr + staffids;
          }
          String[] ids = idsStr.split(",");
          for (String id : ids)
          {
            staffPrivs = staffPrivs.replace("," + id + ",", ",");
            staffPrivs = staffPrivs.replace("," + id + ",", ",");
          }
          if (",".equals(staffPrivs)) {
            staffPrivs = "";
          }
          user.set("staffPrivs", staffPrivs);
        }
      }
      else
      {
        String pids = staff.getStr("pids").replace("{", "").replace("}", ",");
        String staffids = Staff.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId));
        if (StringUtils.isNotBlank(staffids)) {
          pids = pids + staffids;
        }
        if (!pids.startsWith(",")) {
          pids = "," + pids;
        }
        if (!pids.endsWith(",")) {
          pids = pids + ",";
        }
        pids = pids.replace(",0,", ",");
        user.set("staffPrivs", pids);
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "staffPermissionList");
      setAttr("url", "sys/basePermission/staff?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("department".equals(baseType))
    {
      Model department = Department.dao.findById(configName, Integer.valueOf(baseId));
      if (department != null) {
        supId = department.getInt("supId").intValue();
      }
      if (baseId == 0)
      {
        if ((StringUtils.isNotBlank(departmentPrivs)) && (ALL_PRIVS.equals(departmentPrivs))) {
          user.set("departmentPrivs", "");
        } else {
          user.set("departmentPrivs", ALL_PRIVS);
        }
      }
      else if ((StringUtils.isNotBlank(departmentPrivs)) && (ALL_PRIVS.equals(departmentPrivs)))
      {
        String departmentids = Department.dao.getAllChildIdsNoSupId(configName, Integer.valueOf(baseId));
        if (!departmentids.endsWith(",")) {
          departmentids = "," + departmentids + ",";
        }
        user.set("departmentPrivs", departmentids);
      }
      else if ((StringUtils.isNotBlank(departmentPrivs)) && (!ALL_PRIVS.equals(departmentPrivs)))
      {
        if (departmentPrivs.indexOf("," + baseId + ",") == -1)
        {
          String pids = department.getStr("pids").replace("{", "").replace("}", ",");
          String departmentids = Department.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId));
          if (StringUtils.isNotBlank(departmentids)) {
            pids = pids + departmentids;
          }
          if (!pids.endsWith(",")) {
            pids = pids + ",";
          }
          String[] ids = pids.split(",");
          for (String id : ids)
          {
            departmentPrivs = departmentPrivs.replace("," + id + ",", ",");
            departmentPrivs = departmentPrivs.replace("," + id + ",", ",");
          }
          departmentPrivs = departmentPrivs + pids;
          departmentPrivs = departmentPrivs.replace(",0,", ",");
          user.set("departmentPrivs", departmentPrivs);
        }
        else
        {
          String idsStr = baseId + ",";
          String departmentids = Department.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId));
          if (StringUtils.isNotBlank(departmentids)) {
            idsStr = idsStr + departmentids;
          }
          String[] ids = idsStr.split(",");
          for (String id : ids)
          {
            departmentPrivs = departmentPrivs.replace("," + id + ",", ",");
            departmentPrivs = departmentPrivs.replace("," + id + ",", ",");
          }
          if (",".equals(departmentPrivs)) {
            departmentPrivs = "";
          }
          user.set("departmentPrivs", departmentPrivs);
        }
      }
      else
      {
        String pids = department.getStr("pids").replace("{", "").replace("}", ",");
        String departmentids = Department.dao.getAllChildIdsBySupId(configName, Integer.valueOf(baseId));
        if (StringUtils.isNotBlank(departmentids)) {
          pids = pids + departmentids;
        }
        if (!pids.startsWith(",")) {
          pids = "," + pids;
        }
        if (!pids.endsWith(",")) {
          pids = pids + ",";
        }
        pids = pids.replace(",0,", ",");
        user.set("departmentPrivs", pids);
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "departmentPermissionList");
      setAttr("url", "sys/basePermission/department?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else
    {
      Model product = Product.dao.findById(configName, Integer.valueOf(baseId));
      if (product != null) {
        supId = product.getInt("supId").intValue();
      }
      if (baseId == 0)
      {
        if ((StringUtils.isNotBlank(productPrivs)) && (ALL_PRIVS.equals(productPrivs))) {
          user.set("productPrivs", "");
        } else {
          user.set("productPrivs", ALL_PRIVS);
        }
      }
      else if ((StringUtils.isNotBlank(productPrivs)) && (ALL_PRIVS.equals(productPrivs)))
      {
        String productids = Product.dao.getAllChildIdsNoSupId(configName, ALL_PRIVS, baseId);
        if (!productids.endsWith(",")) {
          productids = "," + productids + ",";
        }
        user.set("productPrivs", productids);
      }
      else if ((StringUtils.isNotBlank(productPrivs)) && (!ALL_PRIVS.equals(productPrivs)))
      {
        if (productPrivs.indexOf("," + baseId + ",") == -1)
        {
          String pids = product.getStr("pids").replace("{", "").replace("}", ",");
          String productids = Product.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, baseId);
          if (StringUtils.isNotBlank(productids)) {
            pids = pids + productids;
          }
          if (!pids.endsWith(",")) {
            pids = pids + ",";
          }
          String[] ids = pids.split(",");
          for (String id : ids)
          {
            productPrivs = productPrivs.replace("," + id + ",", ",");
            productPrivs = productPrivs.replace("," + id + ",", ",");
          }
          productPrivs = productPrivs + pids;
          productPrivs = productPrivs.replace(",0,", ",");
          user.set("productPrivs", productPrivs);
        }
        else
        {
          String idsStr = baseId + ",";
          String productids = Product.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, baseId);
          if (StringUtils.isNotBlank(productids)) {
            idsStr = idsStr + productids;
          }
          String[] ids = idsStr.split(",");
          for (String id : ids)
          {
            productPrivs = productPrivs.replace("," + id + ",", ",");
            productPrivs = productPrivs.replace("," + id + ",", ",");
          }
          if (",".equals(productPrivs)) {
            productPrivs = "";
          }
          user.set("productPrivs", productPrivs);
        }
      }
      else
      {
        String pids = product.getStr("pids").replace("{", "").replace("}", ",");
        String productids = Product.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, baseId);
        if (StringUtils.isNotBlank(productids)) {
          pids = pids + productids;
        }
        if (!pids.startsWith(",")) {
          pids = "," + pids;
        }
        if (!pids.endsWith(",")) {
          pids = pids + ",";
        }
        pids = pids.replace(",0,", ",");
        user.set("productPrivs", pids);
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "productPermissionList");
      setAttr("url", "sys/basePermission/product?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    user.update(configName);
    renderJson();
  }
  
  public void empty()
  {
    String configName = loginConfigName();
    int userId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String baseType = getPara(1, "product");
    int baseId = getParaToInt(2, Integer.valueOf(0)).intValue();
    
    Model user = User.dao.findById(configName, Integer.valueOf(userId));
    if (user != null)
    {
      if (1 != user.getInt("grade").intValue())
      {
        returnToPage(AioConstants.HTTP_RETURN300, "不能修改系统管理员权限!", "", "", "", "");
        renderJson();
      }
    }
    else
    {
      returnToPage(AioConstants.HTTP_RETURN300, "修改用户权限失败!", "", "", "", "");
      renderJson();
      return;
    }
    int supId = -1;
    if ("unit".equals(baseType))
    {
      Model unit = Unit.dao.findById(configName, Integer.valueOf(baseId));
      if (unit != null) {
        supId = unit.getInt("supId").intValue();
      }
      user.set("unitPrivs", "");
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "unitPermissionList");
      setAttr("url", "sys/basePermission/unit?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("storage".equals(baseType))
    {
      Model storage = Storage.dao.findById(configName, Integer.valueOf(baseId));
      if (storage != null) {
        supId = storage.getInt("supId").intValue();
      }
      user.set("storagePrivs", "");
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "storagePermissionList");
      setAttr("url", "sys/basePermission/storage?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("account".equals(baseType))
    {
      Model account = Accounts.dao.findById(configName, Integer.valueOf(baseId));
      if (account != null) {
        supId = account.getInt("supId").intValue();
      }
      user.set("accountPrivs", "");
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "accountPermissionList");
      setAttr("url", "sys/basePermission/account?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("area".equals(baseType))
    {
      Model area = Area.dao.findById(configName, Integer.valueOf(baseId));
      if (area != null) {
        supId = area.getInt("supId").intValue();
      }
      user.set("areaPrivs", "");
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "areaPermissionList");
      setAttr("url", "sys/basePermission/area?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("staff".equals(baseType))
    {
      Model staff = Staff.dao.findById(configName, Integer.valueOf(baseId));
      if (staff != null) {
        supId = staff.getInt("supId").intValue();
      }
      user.set("staffPrivs", "");
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "staffPermissionList");
      setAttr("url", "sys/basePermission/staff?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else if ("department".equals(baseType))
    {
      Model department = Department.dao.findById(configName, Integer.valueOf(baseId));
      if (department != null) {
        supId = department.getInt("supId").intValue();
      }
      user.set("departmentPrivs", "");
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "departmentPermissionList");
      setAttr("url", "sys/basePermission/department?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    else
    {
      Model product = Product.dao.findById(configName, Integer.valueOf(baseId));
      if (product != null) {
        supId = product.getInt("supId").intValue();
      }
      user.set("productPrivs", "");
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "productPermissionList");
      setAttr("url", "sys/basePermission/product?pSupId=" + supId + "&userId=" + userId + "&objectId=" + baseId);
    }
    user.update(configName);
    renderJson();
  }
  
  private void initProduct(String privs)
  {
    String configName = loginConfigName();
    Map<String, Object> productPageMap = new HashMap();
    List<Model> productPageList = new ArrayList();
    Product product = new Product();
    product.put("id", Integer.valueOf(0));
    product.put("code", "00000000");
    product.put("fullName", "全部商品");
    if ((StringUtils.isNotBlank(privs)) && (ALL_PRIVS.equals(privs)))
    {
      product.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
    }
    else if ((StringUtils.isNotBlank(privs)) && (!ALL_PRIVS.equals(privs)))
    {
      String idStr = Product.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, 0);
      String[] ids = idStr.split(",");
      boolean include = true;
      for (String id : ids) {
        if (privs.indexOf("," + id + ",") == -1)
        {
          include = false;
          break;
        }
      }
      if (include) {
        product.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
      } else {
        product.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
      }
    }
    else
    {
      product.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
    }
    product.put("node", Integer.valueOf(AioConstants.NODE_2));
    productPageList.add(product);
    productPageMap.put("pageList", productPageList);
    productPageMap.put("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    productPageMap.put("numPerPage", Integer.valueOf(20));
    productPageMap.put("limit", Integer.valueOf(0));
    productPageMap.put("totalCount", Integer.valueOf(1));
    productPageMap.put("listID", "productPermissionList");
    setAttr("productPageMap", productPageMap);
  }
  
  private void initUnit(String privs)
  {
    String configName = loginConfigName();
    Map<String, Object> unitPageMap = new HashMap();
    List<Model> unitPageList = new ArrayList();
    Unit unit = new Unit();
    unit.put("id", Integer.valueOf(0));
    unit.put("code", "00000000");
    unit.put("fullName", "全部单位");
    if ((StringUtils.isNotBlank(privs)) && (ALL_PRIVS.equals(privs)))
    {
      unit.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
    }
    else if ((StringUtils.isNotBlank(privs)) && (!ALL_PRIVS.equals(privs)))
    {
      String idStr = Unit.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, 0);
      String[] ids = idStr.split(",");
      boolean include = true;
      for (String id : ids) {
        if (privs.indexOf("," + id + ",") == -1)
        {
          include = false;
          break;
        }
      }
      if (include) {
        unit.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
      } else {
        unit.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
      }
    }
    else
    {
      unit.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
    }
    unit.put("node", Integer.valueOf(AioConstants.NODE_2));
    unitPageList.add(unit);
    unitPageMap.put("pageList", unitPageList);
    unitPageMap.put("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    unitPageMap.put("numPerPage", Integer.valueOf(20));
    unitPageMap.put("limit", Integer.valueOf(0));
    unitPageMap.put("totalCount", Integer.valueOf(1));
    unitPageMap.put("listID", "unitPermissionList");
    setAttr("unitPageMap", unitPageMap);
  }
  
  private void initArea(String privs)
  {
    String configName = loginConfigName();
    Map<String, Object> areaPageMap = new HashMap();
    List<Model> areaPageList = new ArrayList();
    Area area = new Area();
    area.put("id", Integer.valueOf(0));
    area.put("code", "00000000");
    area.put("fullName", "全部地区");
    if ((StringUtils.isNotBlank(privs)) && (ALL_PRIVS.equals(privs)))
    {
      area.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
    }
    else if ((StringUtils.isNotBlank(privs)) && (!ALL_PRIVS.equals(privs)))
    {
      String idStr = Area.dao.getAllChildIdsBySupId(configName, Integer.valueOf(0));
      String[] ids = idStr.split(",");
      boolean include = true;
      for (String id : ids) {
        if (privs.indexOf("," + id + ",") == -1)
        {
          include = false;
          break;
        }
      }
      if (include) {
        area.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
      } else {
        area.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
      }
    }
    else
    {
      area.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
    }
    area.put("node", Integer.valueOf(AioConstants.NODE_2));
    areaPageList.add(area);
    areaPageMap.put("pageList", areaPageList);
    areaPageMap.put("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    areaPageMap.put("numPerPage", Integer.valueOf(20));
    areaPageMap.put("limit", Integer.valueOf(0));
    areaPageMap.put("totalCount", Integer.valueOf(1));
    areaPageMap.put("listID", "areaPermissionList");
    setAttr("areaPageMap", areaPageMap);
  }
  
  private void initStorage(String privs)
  {
    String configName = loginConfigName();
    Map<String, Object> storagePageMap = new HashMap();
    List<Model> storagePageList = new ArrayList();
    Storage storage = new Storage();
    storage.put("id", Integer.valueOf(0));
    storage.put("code", "00000000");
    storage.put("fullName", "全部仓库");
    if ((StringUtils.isNotBlank(privs)) && (ALL_PRIVS.equals(privs)))
    {
      storage.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
    }
    else if ((StringUtils.isNotBlank(privs)) && (!ALL_PRIVS.equals(privs)))
    {
      String idStr = Storage.dao.getAllChildIdsBySupId(configName, Integer.valueOf(0), ALL_PRIVS);
      String[] ids = idStr.split(",");
      boolean include = true;
      for (String id : ids) {
        if (privs.indexOf("," + id + ",") == -1)
        {
          include = false;
          break;
        }
      }
      if (include) {
        storage.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
      } else {
        storage.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
      }
    }
    else
    {
      storage.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
    }
    storage.put("node", Integer.valueOf(AioConstants.NODE_2));
    storagePageList.add(storage);
    storagePageMap.put("pageList", storagePageList);
    storagePageMap.put("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    storagePageMap.put("numPerPage", Integer.valueOf(20));
    storagePageMap.put("limit", Integer.valueOf(0));
    storagePageMap.put("totalCount", Integer.valueOf(1));
    storagePageMap.put("listID", "storagePermissionList");
    setAttr("storagePageMap", storagePageMap);
  }
  
  private void initAccount(String privs)
  {
    String configName = loginConfigName();
    Map<String, Object> accountPageMap = new HashMap();
    List<Model> accountPageList = new ArrayList();
    Accounts account = new Accounts();
    account.put("id", Integer.valueOf(0));
    account.put("code", "00000000");
    account.put("fullName", "全部会计科目");
    if ((StringUtils.isNotBlank(privs)) && (ALL_PRIVS.equals(privs)))
    {
      account.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
    }
    else if ((StringUtils.isNotBlank(privs)) && (!ALL_PRIVS.equals(privs)))
    {
      String idStr = Accounts.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, Integer.valueOf(0));
      String[] ids = idStr.split(",");
      boolean include = true;
      for (String id : ids) {
        if (privs.indexOf("," + id + ",") == -1)
        {
          include = false;
          break;
        }
      }
      if (include) {
        account.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
      } else {
        account.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
      }
    }
    else
    {
      account.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
    }
    account.put("node", Integer.valueOf(AioConstants.NODE_2));
    accountPageList.add(account);
    accountPageMap.put("pageList", accountPageList);
    accountPageMap.put("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    accountPageMap.put("numPerPage", Integer.valueOf(20));
    accountPageMap.put("limit", Integer.valueOf(0));
    accountPageMap.put("totalCount", Integer.valueOf(1));
    accountPageMap.put("listID", "accountPermissionList");
    setAttr("accountPageMap", accountPageMap);
  }
  
  private void initStaff(String privs)
  {
    String configName = loginConfigName();
    Map<String, Object> staffPageMap = new HashMap();
    List<Model> staffPageList = new ArrayList();
    Staff staff = new Staff();
    staff.put("id", Integer.valueOf(0));
    staff.put("code", "00000000");
    staff.put("fullName", "全部职员");
    if ((StringUtils.isNotBlank(privs)) && (ALL_PRIVS.equals(privs)))
    {
      staff.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
    }
    else if ((StringUtils.isNotBlank(privs)) && (!ALL_PRIVS.equals(privs)))
    {
      String idStr = Staff.dao.getAllChildIdsBySupId(configName, Integer.valueOf(0));
      String[] ids = idStr.split(",");
      boolean include = true;
      for (String id : ids) {
        if (privs.indexOf("," + id + ",") == -1)
        {
          include = false;
          break;
        }
      }
      if (include) {
        staff.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
      } else {
        staff.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
      }
    }
    else
    {
      staff.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
    }
    staff.put("node", Integer.valueOf(AioConstants.NODE_2));
    staffPageList.add(staff);
    staffPageMap.put("pageList", staffPageList);
    staffPageMap.put("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    staffPageMap.put("numPerPage", Integer.valueOf(20));
    staffPageMap.put("limit", Integer.valueOf(0));
    staffPageMap.put("totalCount", Integer.valueOf(1));
    staffPageMap.put("listID", "staffPermissionList");
    setAttr("staffPageMap", staffPageMap);
  }
  
  private void initDepartment(String privs)
  {
    String configName = loginConfigName();
    Map<String, Object> departmentPageMap = new HashMap();
    List<Model> departmentPageList = new ArrayList();
    Department department = new Department();
    department.put("id", Integer.valueOf(0));
    department.put("code", "00000000");
    department.put("fullName", "全部部门");
    if ((StringUtils.isNotBlank(privs)) && (ALL_PRIVS.equals(privs)))
    {
      department.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
    }
    else if ((StringUtils.isNotBlank(privs)) && (!ALL_PRIVS.equals(privs)))
    {
      String idStr = Department.dao.getAllChildIdsBySupId(configName, Integer.valueOf(0));
      String[] ids = idStr.split(",");
      boolean include = true;
      for (String id : ids) {
        if (privs.indexOf("," + id + ",") == -1)
        {
          include = false;
          break;
        }
      }
      if (include) {
        department.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_All));
      } else {
        department.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_PART));
      }
    }
    else
    {
      department.put("privs", Integer.valueOf(AioConstants.PRIVS_STATUS_NO));
    }
    department.put("node", Integer.valueOf(AioConstants.NODE_2));
    departmentPageList.add(department);
    departmentPageMap.put("pageList", departmentPageList);
    departmentPageMap.put("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    departmentPageMap.put("numPerPage", Integer.valueOf(20));
    departmentPageMap.put("limit", Integer.valueOf(0));
    departmentPageMap.put("totalCount", Integer.valueOf(1));
    departmentPageMap.put("listID", "departmentPermissionList");
    setAttr("departmentPageMap", departmentPageMap);
  }
}
