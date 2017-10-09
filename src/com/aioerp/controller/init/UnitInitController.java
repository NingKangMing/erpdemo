package com.aioerp.controller.init;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Unit;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UnitInitController
  extends BaseController
{
  private final String listID = "unitInitList";
  private final String treeID = "unitInitTree";
  
  public void index()
  {
    String searchBaseAttr = getPara("searchBaseAttr");
    String searchBaseVal = getPara("searchBaseVal");
    String configName = loginConfigName();
    setAttr("supId", Integer.valueOf(0));
    Map<String, Object> pageMap = Unit.dao.getPageBySupId(configName, basePrivs(UNIT_PRIVS), basePrivs(STAFF_PRIVS), basePrivs(AREA_PRIVS), AioConstants.START_PAGE, 20, 0, "", "unitInitList", searchBaseAttr, searchBaseVal);
    setAttr("pageMap", pageMap);
    String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
    if (AioConstants.HAS_OPEN_ACCOUNT1.equals(hasOpenAccount)) {
      setAttr("hasOpen", "yes");
    } else {
      setAttr("hasOpen", "no");
    }
    setAttr("aimDiv", "all");
    setAttr("listID", "unitInitList");
    setAttr("treeID", "unitInitTree");
    
    setAttr("searchBaseAttr", searchBaseAttr);
    setAttr("searchBaseVal", searchBaseVal);
    

    columnConfig("init501");
    render("/WEB-INF/template/init/unitInit/page.html");
  }
  
  public void tree()
  {
    List<Model> cats = Unit.dao.getParentObjects(loginConfigName(), basePrivs(UNIT_PRIVS));
    Iterator<Model> iter = cats.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model object = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", object.get("id"));
      node.put("pId", object.get("supId"));
      node.put("name", object.get("fullName"));
      node.put("url", "init/unitInit/list/" + object.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "单位信息");
    node.put("url", "init/unitInit/list/0");
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public void list()
  {
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    setAttr("pageMap", com(pageNum, numPerPage));
    
    columnConfig("init501");
    render("/WEB-INF/template/init/unitInit/list.html");
  }
  
  public Map<String, Object> com(int pageNum, int numPerPage)
  {
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    String configName = loginConfigName();
    




    String aimDiv = getPara("aimDiv");
    setAttr("aimDiv", aimDiv);
    


    String searchPar1 = getPara("searchBaseAttr");
    String searchValue1 = getPara("searchBaseVal");
    setAttr("searchBaseAttr", searchPar1);
    setAttr("searchBaseVal", searchValue1);
    
    Map<String, Object> pageMap = null;
    

    int supId = getParaToInt("supId", getParaToInt(0, Integer.valueOf(0))).intValue();
    setAttr("supId", Integer.valueOf(supId));
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (upObjectId > 0)
    {
      pageMap = Unit.dao.getPageByUpObject(configName, basePrivs(UNIT_PRIVS), basePrivs(STAFF_PRIVS), basePrivs(AREA_PRIVS), "unitInitList", supId, upObjectId, pageNum, numPerPage, aimDiv, searchPar1, searchValue1, ORDER_FIELD, ORDER_DIRECTION, AioConstants.STATUS_DELETE);
      setAttr("objectId", Integer.valueOf(upObjectId));
    }
    else
    {
      String showLastPage = getPara("showLastPage");
      int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
      setAttr("objectId", Integer.valueOf(selectedObjectId));
      if ((showLastPage != null) && (showLastPage.equals("true"))) {
        pageMap = Unit.dao.getPageByFilterLast(configName, basePrivs(UNIT_PRIVS), basePrivs(STAFF_PRIVS), basePrivs(AREA_PRIVS), "unitInitList", supId, aimDiv, searchPar1, searchValue1, pageNum, numPerPage, ORDER_FIELD, ORDER_DIRECTION, AioConstants.STATUS_DELETE);
      } else {
        pageMap = Unit.dao.getPageByCondtion(configName, basePrivs(UNIT_PRIVS), basePrivs(STAFF_PRIVS), basePrivs(AREA_PRIVS), "unitInitList", supId, null, aimDiv, searchPar1, searchValue1, pageNum, numPerPage, orderField, orderDirection, AioConstants.STATUS_DELETE);
      }
    }
    int pSupId = 0;
    if (supId > 0) {
      pSupId = Unit.dao.getPsupId(configName, supId);
    }
    String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
    if (AioConstants.HAS_OPEN_ACCOUNT1.equals(hasOpenAccount)) {
      setAttr("hasOpen", "yes");
    } else {
      setAttr("hasOpen", "no");
    }
    setAttr("listID", "unitInitList");
    setAttr("treeID", "unitInitTree");
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("toAction", "list");
    setAttr("toActionCall", "list");
    return pageMap;
  }
  
  public void toEditUnitInit()
  {
    int unitId = getParaToInt("selectedObjectId").intValue();
    Unit u = (Unit)Unit.dao.findById(loginConfigName(), Integer.valueOf(unitId));
    setAttr("unit", u);
    render("/WEB-INF/template/init/unitInit/editUnitInit.html");
  }
  
  @Before({Tx.class})
  public void editUnitInit()
  {
    String configName = loginConfigName();
    try
    {
      String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
      if (AioConstants.HAS_OPEN_ACCOUNT1.equals(hasOpenAccount))
      {
        this.result.put("message", "已经开账不能修改期初");
        this.result.put("statusCode", AioConstants.HTTP_RETURN300);
        renderJson(this.result);
        return;
      }
      Model model = (Model)getModel(Unit.class);
      Model oldModel = Unit.dao.findById(configName, model.getInt("id"));
      oldModel.set("beginGetMoney", model.getBigDecimal("beginGetMoney"));
      oldModel.set("beginPayMoney", model.getBigDecimal("beginPayMoney"));
      


      oldModel.update(configName);
      this.result.put("statusCode", AioConstants.HTTP_RETURN200);
      this.result.put("callbackType", "closeCurrent");
      this.result.put("rel", "unitInitList");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.result.put("message", "系统异常");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
    }
    renderJson(this.result);
  }
  
  public void print()
    throws ParseException
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "期初应收应付");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    List<Model> detailList = new ArrayList();
    

    Map<String, Object> map = new HashMap();
    map.put("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    int numPerPage = getParaToInt("totalCount", Integer.valueOf(9999999)).intValue();
    if (numPerPage == 0) {
      numPerPage = 1;
    }
    Map<String, Object> pageMap = com(AioConstants.START_PAGE, numPerPage);
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
    colTitle.add("状态");
    colTitle.add("期初应收款");
    colTitle.add("期初应付款");
    


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
      rowData.add(trimNull(status));
      rowData.add(trimNull(fitPriceStr));
      rowData.add(trimNull(detail.getBigDecimal("beginGetMoneys")));
      rowData.add(trimNull(detail.getBigDecimal("beginPayMoneys")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
