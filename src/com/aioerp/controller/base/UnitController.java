package com.aioerp.controller.base;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Area;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
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

public class UnitController
  extends BaseController
{
  private static String LIST_TABLE = "b_unit_listTable";
  
  public void list()
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    Integer node = getParaToInt("node", Integer.valueOf(0));
    String actionType = getPara("actionType", "");
    if (actionType.equals("line")) {
      node = Integer.valueOf(AioConstants.NODE_1);
    }
    String searchPar1 = getPara("searchPar1");
    String searchValue1 = getPara("searchValue1");
    
    Map<String, Object> pageMap = null;
    int objectId = 0;
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (upObjectId > 0)
    {
      pageMap = Unit.dao.getPageByUpObject(configName, basePrivs(UNIT_PRIVS), basePrivs(STAFF_PRIVS), basePrivs(AREA_PRIVS), LIST_TABLE, supId, upObjectId, pageNum, numPerPage, null, searchPar1, searchValue1, ORDER_FIELD, ORDER_DIRECTION, AioConstants.STATUS_DELETE);
      objectId = upObjectId;
    }
    else
    {
      String showLastPage = getPara("showLastPage");
      int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
      objectId = selectedObjectId;
      if ((showLastPage != null) && (showLastPage.equals("true"))) {
        pageMap = Unit.dao.getPageByFilterLast(configName, basePrivs(UNIT_PRIVS), basePrivs(STAFF_PRIVS), basePrivs(AREA_PRIVS), LIST_TABLE, supId, null, searchPar1, searchValue1, pageNum, numPerPage, ORDER_FIELD, ORDER_DIRECTION, AioConstants.STATUS_DELETE);
      } else {
        pageMap = Unit.dao.getPageByCondtion(configName, basePrivs(UNIT_PRIVS), basePrivs(STAFF_PRIVS), basePrivs(AREA_PRIVS), LIST_TABLE, supId, node, null, searchPar1, searchValue1, pageNum, numPerPage, orderField, orderDirection, AioConstants.STATUS_DELETE);
      }
    }
    int pSupId = 0;
    if (supId > 0) {
      pSupId = Unit.dao.getPsupId(configName, supId);
    }
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("node", node);
    setAttr("searchPar1", searchPar1);
    setAttr("searchValue1", searchValue1);
    setAttr("pageMap", pageMap);
    setAttr("objectId", Integer.valueOf(objectId));
    

    columnConfig("b502");
    String whichRefresh = getPara("whichRefresh", "");
    if (whichRefresh.equals("navTabRefresh")) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void tree()
  {
    String configName = loginConfigName();
    List<Model> cats = Unit.dao.getParentObjects(configName, basePrivs(UNIT_PRIVS));
    Iterator<Model> iter = cats.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model object = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", object.get("id"));
      node.put("pId", object.get("supId"));
      node.put("name", object.get("fullName"));
      node.put("url", "base/unit/list/" + object.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "单位信息");
    node.put("url", "base/unit/list/0");
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public void add()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Unit.class);
      int numAutoAdd = getParaToInt("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0)).intValue();
      model.set("areaId", getParaToInt("area.id"));
      model.set("staffId", getParaToInt("staff.id"));
      String code = model.getStr("code");
      if (Unit.dao.existObjectByNum(configName, 0, code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson(this.result);
        return;
      }
      if ((model.getInt("status") != null) && (model.getInt("status").intValue() == 1)) {
        model.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
      } else {
        model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      }
      String upids = "";
      int supId = model.getInt("supId").intValue();
      if (supId > 0) {
        upids = Unit.dao.findById(configName, model.get("supId")).getStr("pids") + "{" + model.getInt("supId") + "}";
      } else {
        upids = "{0}";
      }
      model.set("node", Integer.valueOf(AioConstants.NODE_1));
      model.set("pids", upids);
      model.set("createTime", new Date());
      model.set("updateTime", new Date());
      model.set("userId", Integer.valueOf(loginUserId()));
      updateAreaAndStaff(model);
      Boolean flag = Boolean.valueOf(model.save(configName));
      updateBasePrivs(model.getInt("id"), model.getInt("supId"), UNIT_PRIVS);
      if (flag.booleanValue())
      {
        model.set("pids", model.get("pids") + "{" + model.getInt("id") + "}");
        model.set("rank", model.getInt("id")).update(configName);
        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("rel", LIST_TABLE);
        this.result.put("base", getAttr("base"));
        


        String url = "/base/unit/add/";
        String confirmType = getPara("confirmType");
        if ("dialogCloseReload".equals(confirmType))
        {
          url = "/base/unit/optionAdd/";
          setAttr("supId", Integer.valueOf(supId));
        }
        if (numAutoAdd == AioConstants.NUMNOADD1)
        {
          code = NumberUtils.increase(code);
          
          this.result.put("loadDialogUrl", url + supId + "-" + URLEncoder.encode(code) + "-" + numAutoAdd);
        }
        else
        {
          this.result.put("loadDialogUrl", url + supId);
        }
        this.result.put("reset", "reset");
        this.result.put("dialogId", "b_unit_id");
        this.result.put("opterate", "showLastPage");
        this.result.put("selectedObjectId", model.get("id"));
      }
      else
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "操作失败！！！");
      }
      renderJson(this.result);
    }
    else
    {
      int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
      setAttr("supId", Integer.valueOf(supId));
      String one = getPara(1);
      if ((one != null) && (!one.equals("")))
      {
        setAttr("code", URLDecoder.decode(one));
        setAttr("numAutoAdd", getParaToInt(2));
      }
      setAttr("toAction", "add");
      setAttr("toActionCall", "add");
      render("dialog.html");
    }
  }
  
  public void copyAdd()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Unit.class);
      int numAutoAdd = getParaToInt("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0)).intValue();
      model.set("areaId", getParaToInt("area.id"));
      model.set("staffId", getParaToInt("staff.id"));
      String code = model.getStr("code");
      if (Unit.dao.existObjectByNum(configName, 0, code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson(this.result);
        return;
      }
      Model oldModel = Unit.dao.findById(configName, model.getInt("id"));
      
      model.set("supId", oldModel.get("supId"));
      String oldPids = (String)oldModel.get("pids");
      model.set("pids", oldPids.subSequence(0, oldPids.lastIndexOf("{")));
      model.set("node", Integer.valueOf(AioConstants.NODE_1));
      model.set("createTime", new Date());
      model.set("updateTime", new Date());
      model.set("id", null);
      model.set("userId", Integer.valueOf(loginUserId()));
      if ((model.getInt("status") != null) && (model.getInt("status").intValue() == 1)) {
        model.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
      } else {
        model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      }
      updateAreaAndStaff(model);
      Boolean flag = Boolean.valueOf(model.save(configName));
      updateBasePrivs(model.getInt("id"), model.getInt("supId"), UNIT_PRIVS);
      if (flag.booleanValue())
      {
        model.set("pids", model.get("pids") + "{" + model.getInt("id") + "}");
        model.set("rank", model.getInt("id")).update(configName);
        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("rel", LIST_TABLE);
        if (numAutoAdd == AioConstants.NUMNOADD1)
        {
          code = NumberUtils.increase(code);
          this.result.put("code", code);
        }
        this.result.put("opterate", "showLastPage");
        this.result.put("selectedObjectId", model.get("id"));
      }
      else
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "操作失败！！！");
      }
      renderJson(this.result);
    }
    else
    {
      Unit unit = (Unit)Unit.dao.getObjectById(configName, getParaToInt(0, Integer.valueOf(0)).intValue());
      unit.set("beginGetMoney", "");
      unit.set("beginPayMoney", "");
      unit.set("beginPreGetMoney", "");
      unit.set("beginPrePayMoney", "");
      unit.set("getMoneyLimit", "");
      unit.set("payMoneyLimit", "");
      unit.set("totalGet", "");
      unit.set("totalPay", "");
      unit.set("totalPreGet", "");
      unit.set("totalPrePay", "");
      











      setAttr("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0));
      setAttr("toAction", "copyAdd");
      setAttr("toActionCall", "copyAdd");
      
      setAttr("unit", unit);
      render("dialog.html");
    }
  }
  
  public void edit()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Unit.class);
      int numAutoAdd = getParaToInt("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0)).intValue();
      model.set("areaId", getParaToInt("area.id"));
      model.set("staffId", getParaToInt("staff.id"));
      String code = model.getStr("code");
      if (Unit.dao.existObjectByNum(configName, model.getInt("id").intValue(), code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson();
        return;
      }
      Model oldModel = Unit.dao.findById(configName, model.getInt("id"));
      model.set("supId", oldModel.get("supId"));
      model.set("pids", oldModel.get("pids"));
      model.set("node", oldModel.get("node"));
      model.set("rank", oldModel.get("rank"));
      model.set("createTime", oldModel.get("createTime"));
      model.set("updateTime", new Date());
      model.set("userId", Integer.valueOf(loginUserId()));
      if ((model.getInt("status") != null) && (model.getInt("status").intValue() == 1)) {
        model.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
      } else {
        model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      }
      updateAreaAndStaff(model);
      Boolean flag = Boolean.valueOf(model.update(configName));
      if (flag.booleanValue())
      {
        if (numAutoAdd == AioConstants.NUMNOADD1) {
          setAttr("code", NumberUtils.increase(model.getStr("code")));
        }
        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("callbackType", "closeCurrent");
        this.result.put("rel", LIST_TABLE);
        HashMap<String, Object> node = new HashMap();
        node.put("id", model.get("id"));
        node.put("name", model.get("name"));
        this.result.put("node", node);
        this.result.put("trel", "b_unit_treeId");
        this.result.put("url", "base/unit/list/" + model.get("supId"));
        
        this.result.put("selectedObjectId", model.get("id"));
      }
      renderJson(this.result);
    }
    else
    {
      Unit unit = (Unit)Unit.dao.getObjectById(configName, getParaToInt(0, Integer.valueOf(0)).intValue());
      
      Integer staffId = unit.getInt("staffId");
      if ((unit != null) && (staffId != null) && (staffId.intValue() != 0)) {
        setAttr("staff", Staff.dao.findById(configName, staffId));
      }
      Integer areaId = unit.getInt("areaId");
      if ((unit != null) && (areaId != null) && (areaId.intValue() != 0)) {
        setAttr("area", Area.dao.findById(configName, areaId));
      }
      setAttr("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0));
      setAttr("toAction", "edit");
      setAttr("toActionCall", "edit");
      setAttr("unit", unit);
      render("dialog.html");
    }
  }
  
  public void updateAreaAndStaff(Model model)
  {
    String configName = loginConfigName();
    Integer areaId = model.getInt("areaId");
    Integer staffId = model.getInt("staffId");
    if ((areaId != null) && (areaId.intValue() != 0))
    {
      Model m = Area.dao.findById(configName, areaId);
      if (m == null)
      {
        model.set("areaId", null);
        model.set("areaFullName", null);
      }
      else
      {
        model.set("areaFullName", m.getStr("fullName"));
      }
    }
    if ((staffId != null) && (staffId.intValue() != 0))
    {
      Model m = Staff.dao.findById(configName, staffId);
      if (m == null)
      {
        model.set("staffId", null);
        model.set("staffName", null);
      }
      else
      {
        model.set("staffName", m.getStr("fullName"));
      }
    }
  }
  
  @Before({Tx.class})
  public void delete()
  {
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    



    String configName = loginConfigName();
    if (Unit.dao.hasChildren(configName, id))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "删除失败，请先删除下级!");
      renderJson(this.result);
      return;
    }
    boolean verify = Unit.dao.verify(configName, "unitId", Integer.valueOf(id));
    if (verify)
    {
      returnToPage(AioConstants.HTTP_RETURN300, "数据存在引用，不能删除!", "", "", "", "");
      renderJson();
      return;
    }
    Model<Unit> unit = Unit.dao.findById(configName, Integer.valueOf(id));
    int supId = ((Integer)unit.get("supId", Integer.valueOf(0))).intValue();
    if (Unit.dao.deleteById(configName, Integer.valueOf(id)))
    {
      this.result.put("statusCode", Integer.valueOf(200));
      this.result.put("message", "");
      this.result.put("rel", LIST_TABLE);
    }
    if ((!Unit.dao.hasChildren(configName, supId)) && (supId != 0))
    {
      Model<Unit> pUnit = Unit.dao.findById(configName, Integer.valueOf(supId));
      ((Unit)pUnit.set("node", Integer.valueOf(AioConstants.NODE_1))).update(configName);
      this.result.put("trel", "b_unit_treeId");
      
      HashMap<String, Object> node = new HashMap();
      node.put("id", pUnit.get("id"));
      node.put("pId", pUnit.get("supId"));
      this.result.put("pnode", node);
      
      this.result.put("rel", LIST_TABLE);
      this.result.put("url", "base/unit/list/" + pUnit.get("supId") + "-" + supId);
      this.result.put("selectedObjectId", pUnit.get("id"));
    }
    renderJson(this.result);
  }
  
  public void validateUnit()
  {
    int id = getParaToInt("id", Integer.valueOf(0)).intValue();
    boolean verify = Unit.dao.verify(loginConfigName(), "unitId", Integer.valueOf(id));
    if (verify)
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "该编号已经存在！");
    }
    else
    {
      this.result.put("statusCode", Integer.valueOf(200));
    }
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void sort()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Unit.class);
      model.set("areaId", getParaToInt("area.id"));
      model.set("staffId", getParaToInt("staff.id"));
      String code = model.getStr("code");
      if (Unit.dao.existObjectByNum(configName, 0, code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson(this.result);
        return;
      }
      if ((model.getInt("status") != null) && (model.getInt("status").intValue() == 1)) {
        model.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
      } else {
        model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      }
      Model pUnit = Unit.dao.findById(configName, model.getInt("id"));
      model.set("supId", pUnit.get("id"));
      model.set("node", Integer.valueOf(AioConstants.NODE_1));
      
      model.set("pids", pUnit.getStr("pids"));
      model.set("createTime", new Date());
      model.set("updateTime", new Date());
      model.set("id", null);
      model.set("userId", Integer.valueOf(loginUserId()));
      updateAreaAndStaff(model);
      Boolean flag = Boolean.valueOf(model.save(configName));
      updateBasePrivs(model.getInt("id"), model.getInt("supId"), UNIT_PRIVS);
      if (flag.booleanValue())
      {
        model.set("pids", model.get("pids") + "{" + model.getInt("id") + "}");
        model.set("rank", model.getInt("id")).update(configName);
        
        pUnit.set("node", Integer.valueOf(AioConstants.NODE_2)).update(configName);
        



        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("callbackType", "closeCurrent");
        this.result.put("rel", LIST_TABLE);
        this.result.put("target", "ajax");
        this.result.put("trel", "b_unit_treeId");
        this.result.put("url", "base/unit/list/" + pUnit.get("id"));
        HashMap<String, Object> node = new HashMap();
        node.put("id", pUnit.get("id"));
        node.put("pId", pUnit.get("supId"));
        node.put("name", pUnit.get("fullName"));
        node.put("url", "base/unit/list/" + pUnit.get("id"));
        this.result.put("pnode", node);
        this.result.put("opterate", "showLastPage");
        this.result.put("selectedObjectId", model.get("id"));
      }
      else
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "操作失败！");
      }
      renderJson(this.result);
    }
    else
    {
      Unit unit = (Unit)Unit.dao.getObjectById(configName, getParaToInt(0, Integer.valueOf(0)).intValue());
      











      setAttr("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0));
      setAttr("supId", unit.getInt("id"));
      setAttr("toAction", "sort");
      setAttr("toActionCall", "sort");
      setAttr("unit", unit);
      render("dialog.html");
    }
  }
  
  public void disableOrEnable()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    Unit unit = (Unit)Unit.dao.getObjectById(configName, id);
    Boolean flag = Boolean.valueOf(false);
    if (unit.getInt("status").intValue() == AioConstants.STATUS_DISABLE) {
      flag = Boolean.valueOf(unit.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE)).update(configName));
    } else {
      flag = Boolean.valueOf(unit.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE)).update(configName));
    }
    if (flag.booleanValue())
    {
      this.result.put("id", Integer.valueOf(id));
      this.result.put("status", unit.getInt("status"));
    }
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
        Unit.dao.findById(configName, ids[i]).set("rank", seqs[i]).update(configName);
      }
    }
    returnToPage("", "", "", "", "", "");
    renderJson();
  }
  
  public void filter()
  {
    if (isPost())
    {
      this.result.put("searchPar1", getPara("searchPar1"));
      this.result.put("searchValue1", getPara("searchValue1"));
      this.result.put("statusCode", Integer.valueOf(200));
      
      renderJson(this.result);
    }
    else
    {
      render("filter.html");
    }
  }
  
  public void filterData()
  {
    String searchPar1 = getPara("searchPar1");
    String searchValue1 = getPara("searchValue1");
    int supId = 0;
    int pageNum = 1;
    int numPerPage = getParaToInt("numPerPage").intValue();
    
    Map<String, Object> pageMap = Unit.dao.getPageByCondtion(loginConfigName(), basePrivs(UNIT_PRIVS), basePrivs(STAFF_PRIVS), basePrivs(AREA_PRIVS), LIST_TABLE, supId, null, null, searchPar1, searchValue1, pageNum, numPerPage, ORDER_FIELD, ORDER_DIRECTION, AioConstants.STATUS_DELETE);
    setAttr("pageMap", pageMap);
    
    setAttr("pSupId", Integer.valueOf(0));
    setAttr("supId", Integer.valueOf(supId));
    




    columnConfig("b502");
    render("page.html");
  }
  
  @Before({Tx.class})
  public void optionAdd()
    throws UnsupportedEncodingException
  {
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    setAttr("supId", Integer.valueOf(supId));
    String one = getPara(1);
    if ((one != null) && (!one.equals("")))
    {
      setAttr("code", URLDecoder.decode(one, "utf-8"));
      setAttr("numAutoAdd", getParaToInt(2));
    }
    setAttr("toAction", "add");
    setAttr("toActionCall", "optionAdd");
    setAttr("confirmType", "dialogCloseReload");
    
    render("dialog.html");
  }
  
  public void option()
  {
    String configName = loginConfigName();
    String actionType = getPara("actionType", "first");
    String btnPattern = getPara("btnPattern", "");
    setAttr("btnPattern", btnPattern);
    String whichCallBack = getPara("whichCallBack", "");
    setAttr("whichCallBack", whichCallBack);
    int pageNum = AioConstants.START_PAGE;
    int numPerPage = 10;
    if (StringUtils.isNotBlank(getPara("pageNum"))) {
      pageNum = getParaToInt("pageNum").intValue();
    }
    if (StringUtils.isNotBlank(getPara("numPerPage"))) {
      numPerPage = getParaToInt("numPerPage").intValue();
    }
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    if (actionType.equals("first"))
    {
      orderField = "rank";
      orderDirection = "asc";
    }
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    String showLastPage = getPara("showLastPage");
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    String term = getPara("term", "");
    String termVal = getPara("termVal", "");
    String name = getPara("param", "");
    if (!name.equals(""))
    {
      term = "quick";
      termVal = name;
    }
    setAttr("term", term);
    setAttr("termVal", termVal);
    Map<String, Object> map = new HashMap();
    map.put("term", term);
    map.put("termVal", termVal);
    


    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int id = getParaToInt("id", Integer.valueOf(0)).intValue();
    if (actionType.equals("first"))
    {
      Map<String, Object> unitMap = Unit.dao.supIdSearch(configName, basePrivs(UNIT_PRIVS), supId, pageNum, numPerPage, orderField, orderDirection, map);
      if ((!termVal.equals("")) && 
        (Integer.parseInt(unitMap.get("totalCount").toString()) == 1) && (StringUtils.isNotBlank(termVal)))
      {
        List<Unit> list = (List)unitMap.get("pageList");
        Unit s = (Unit)list.get(0);
        HashMap<String, Object> result = new HashMap();
        result.put("id", s.getInt("id"));
        result.put("fullName", s.getStr("fullName"));
        result.put("code", s.getStr("code"));
        setAttr("statusCode", AioConstants.HTTP_RETURN200);
        setAttr("obj", result);
        renderJson();
        return;
      }
      setAttr("pageMap", unitMap);
    }
    else if (actionType.equals("search"))
    {
      setAttr("pageMap", Unit.dao.search(configName, basePrivs(UNIT_PRIVS), pageNum, numPerPage, orderField, orderDirection, map));
    }
    else if (actionType.equals("page"))
    {
      if ((showLastPage != null) && (showLastPage.equals("true")))
      {
        setAttr("pageMap", Unit.dao.getPageByFilterLast(configName, basePrivs(UNIT_PRIVS), basePrivs(STAFF_PRIVS), basePrivs(AREA_PRIVS), "", supId, null, null, null, pageNum, numPerPage, ORDER_FIELD, ORDER_DIRECTION, AioConstants.STATUS_DISABLE));
        setAttr("objectId", Integer.valueOf(selectedObjectId));
      }
      else
      {
        setAttr("pageMap", Unit.dao.supIdSearch(configName, basePrivs(UNIT_PRIVS), supId, pageNum, numPerPage, orderField, orderDirection, map));
      }
    }
    else if (actionType.equals("down"))
    {
      setAttr("pageMap", Unit.dao.supIdSearch(configName, basePrivs(UNIT_PRIVS), supId, pageNum, numPerPage, orderField, orderDirection, map));
    }
    else if (actionType.equals("up"))
    {
      setAttr("pageMap", Unit.dao.supIdSearch(configName, basePrivs(UNIT_PRIVS), supId, pageNum, numPerPage, orderField, orderDirection, map));
      setAttr("objectId", Integer.valueOf(id));
    }
    columnConfig("zj101");
    
    setAttr("actionType", actionType);
    setAttr("supId", Integer.valueOf(supId));
    render("option.html");
  }
  
  public void checkedBack()
  {
    int unitId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String whichCallBack = getPara(1, "");
    Model unit = Unit.dao.findById(loginConfigName(), Integer.valueOf(unitId));
    Map<String, Object> obj = new HashMap();
    obj.put("whichCallBack", whichCallBack);
    obj.put("unit", unit);
    
    renderJson(obj);
  }
  
  public void unitListById()
  {
    int unitId = getParaToInt("unitId").intValue();
    List<Model> list = Unit.dao.getUnitListById(loginConfigName(), basePrivs(UNIT_PRIVS), unitId);
    renderJson(list);
  }
  
  public void unitMultiOption()
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
    String unitPrivs = basePrivs(UNIT_PRIVS);
    if (supId > 0) {
      pSupId = Unit.dao.getPsupId(configName, supId);
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
    
    Map<String, Object> pageMap = null;
    if (type.equals("toDialog"))
    {
      pageMap = Unit.dao.unitMultiPage(configName, supId, pageNum, numPerPage, orderField, orderDirection, map, unitPrivs);
    }
    else if ((type.equals("search")) || (type.equals("page")) || (type.equals("down")) || (type.equals("up")))
    {
      if ((type.equals("up")) && (upObjectId > 0))
      {
        pageMap = Unit.dao.unitMultiUp(configName, supId, pageNum, numPerPage, orderField, orderDirection, upObjectId, "", map, unitPrivs);
      }
      else if (type.equals("search"))
      {
        pageMap = Unit.dao.unitMultiSearch(configName, pageNum, numPerPage, orderField, orderDirection, map, unitPrivs);
        
        List searchList = (List)pageMap.get("pageList");
        if ((pageMap != null) && (searchList != null) && (searchList.size() > 0))
        {
          Model storage = (Model)searchList.get(0);
          supId = Integer.valueOf(storage.get("supId").toString()).intValue();
        }
      }
      else if (type.equals("down"))
      {
        pageMap = Unit.dao.unitMultiDown(configName, supId, pageNum, numPerPage, orderField, orderDirection, map, unitPrivs);
      }
      else
      {
        pageMap = Unit.dao.unitMultiPage(configName, supId, pageNum, numPerPage, orderField, orderDirection, map, unitPrivs);
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
    render("/WEB-INF/template/base/unit/multiselectOption/unitMultiOption.html");
  }
  
  public void unitMultisearchBack()
  {
    String newDownSelectObjs = getPara(0, "");
    String selectIds = getPara("selectIds", "");
    if (selectIds != "") {
      newDownSelectObjs = selectIds;
    }
    List<Model> list = Unit.dao.unitMultiSearchBack(loginConfigName(), newDownSelectObjs);
    
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
  
  public void print()
  {
    String configName = loginConfigName();
    
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "单位信息");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    List<Model> detailList = new ArrayList();
    

    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int pageNum = AioConstants.START_PAGE;
    int numPerPage = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String searchPar1 = getPara("searchPar1");
    String searchValue1 = getPara("searchValue1");
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    Map<String, Object> pageMap = null;
    pageMap = Unit.dao.getPageByCondtion(configName, basePrivs(UNIT_PRIVS), basePrivs(STAFF_PRIVS), basePrivs(AREA_PRIVS), LIST_TABLE, supId, Integer.valueOf(node), null, searchPar1, searchValue1, pageNum, numPerPage, orderField, orderDirection, AioConstants.STATUS_DELETE);
    if (pageMap != null) {
      detailList = (List)pageMap.get("pageList");
    }
    printCommonData(headData);
    

    colTitle.add("行号");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("单位简名");
    colTitle.add("单位拼音码");
    colTitle.add("单位地址");
    colTitle.add("单位电话");
    colTitle.add("电子邮件");
    colTitle.add("联系人一");
    colTitle.add("手机一");
    colTitle.add("联系人二");
    colTitle.add("手机二");
    colTitle.add("默认经手人");
    colTitle.add("开户银行");
    colTitle.add("银行账号");
    colTitle.add("邮政编码");
    colTitle.add("传真");
    colTitle.add("税号");
    colTitle.add("地区全名");
    colTitle.add("适用价格");
    colTitle.add("备注");
    colTitle.add("状态");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      String status = "";
      if (detail.getInt("status").intValue() == AioConstants.STATUS_DISABLE) {
        status = "停用";
      } else {
        status = "启用";
      }
      String fitPriceStr = "无";
      if (detail.getInt("fitPrice").intValue() == 0) {
        fitPriceStr = "无";
      } else if (detail.getInt("fitPrice").intValue() == 1) {
        fitPriceStr = "零售价";
      } else if (detail.getInt("fitPrice").intValue() == 2) {
        fitPriceStr = "预设售价1";
      } else if (detail.getInt("fitPrice").intValue() == 3) {
        fitPriceStr = "预设售价2";
      } else if (detail.getInt("fitPrice").intValue() == 4) {
        fitPriceStr = "预设售价3";
      }
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("fullName")));
      rowData.add(trimNull(detail.getStr("smallName")));
      rowData.add(trimNull(detail.getStr("spell")));
      rowData.add(trimNull(detail.getStr("address")));
      rowData.add(trimNull(detail.getStr("phone")));
      rowData.add(trimNull(detail.getStr("email")));
      rowData.add(trimNull(detail.getStr("contact1")));
      rowData.add(trimNull(detail.getStr("mobile1")));
      rowData.add(trimNull(detail.getStr("contact2")));
      rowData.add(trimNull(detail.getStr("mobile2")));
      rowData.add(trimNull(detail.getStr("staffName")));
      rowData.add(trimNull(detail.getStr("bank")));
      rowData.add(trimNull(detail.getStr("bankAccount")));
      rowData.add(trimNull(detail.getStr("zipCode")));
      rowData.add(trimNull(detail.getStr("fax")));
      rowData.add(trimNull(detail.getStr("tariff")));
      rowData.add(trimNull(detail.getStr("areaFullName")));
      rowData.add(trimNull(fitPriceStr));
      rowData.add(trimNull(detail.getStr("memo")));
      rowData.add(trimNull(status));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
