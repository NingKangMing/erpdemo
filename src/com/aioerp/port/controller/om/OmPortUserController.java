package com.aioerp.port.controller.om;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.port.common.PortConstants;
import com.aioerp.port.model.om.OmPortUser;
import com.aioerp.util.MD5Util;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OmPortUserController
  extends BaseController
{
  private static String LIST_TABLE = "om_port_user_paging";
  
  public void index()
  {
    String configName = loginConfigName();
    Map<String, Object> pageMap = OmPortUser.dao.aioGetPageRecord(configName, new OmPortUser(), LIST_TABLE, AioConstants.START_PAGE, 20, "select u.*,unit.code,unit.fullName", "from om_port_user u left join b_unit unit on unit.id=u.unitId where u.status<>" + PortConstants.OM_STATUS0 + " order by u.rank desc ", new Object[0]);
    pageMap = showManyStorageFullNames(pageMap);
    setAttr("pageMap", pageMap);
    setAttr("listID", LIST_TABLE);
    render("/WEB-INF/template/port/om/page.html");
  }
  
  public void list()
  {
    String configName = loginConfigName();
    setAttr("listID", LIST_TABLE);
    Map<String, Object> pageMap = null;
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    


    String searchPar1 = getPara("searchPar1");
    String searchValue1 = getPara("searchValue1");
    
    setAttr("searchPar1", searchPar1);
    setAttr("searchValue1", searchValue1);
    

    String showLastPage = getPara("showLastPage");
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    if ((showLastPage != null) && (showLastPage.equals("true"))) {
      pageMap = OmPortUser.dao.getPageByFilterLast(configName, LIST_TABLE, searchPar1, searchValue1, pageNum, numPerPage, ORDER_FIELD, ORDER_DIRECTION);
    } else {
      pageMap = OmPortUser.dao.getPageByFilter(configName, LIST_TABLE, searchPar1, searchValue1, pageNum, numPerPage, orderField, orderDirection);
    }
    pageMap = showManyStorageFullNames(pageMap);
    

    setAttr("pageMap", pageMap);
    setAttr("toAction", "list");
    setAttr("trel", LIST_TABLE);
    setAttr("refreshTable", LIST_TABLE);
    
    render("/WEB-INF/template/port/om/list.html");
  }
  
  public void toAdd()
  {
    setAttr("toAction", "add");
    render("/WEB-INF/template/port/om/dialog.html");
  }
  
  public void add()
  {
    String configName = loginConfigName();
    Model model = (Model)getModel(OmPortUser.class);
    String loginName = model.getStr("loginName");
    if (OmPortUser.dao.existObjectByLoginName(configName, 0, loginName))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "该用户已经存在！");
      renderJson(this.result);
      return;
    }
    int unitId = getParaToInt("unit.id").intValue();
    if (OmPortUser.dao.existObjectByLoginUnitId(configName, 0, unitId))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "该单位用户已经存在！");
      renderJson(this.result);
      return;
    }
    model.set("loginPwd", MD5Util.string2MD5(model.getStr("loginPwd")));
    model.set("portKey", StringUtil.getCharAndNumr(16));
    model.set("unitId", Integer.valueOf(unitId));
    model.set("status", Integer.valueOf(PortConstants.OM_STATUS2));
    Boolean flag = Boolean.valueOf(model.save(configName));
    if (flag.booleanValue())
    {
      model.set("rank", model.getInt("id"));
      model.update(configName);
      this.result.put("statusCode", Integer.valueOf(200));
      this.result.put("message", "");
      this.result.put("rel", LIST_TABLE);
      this.result.put("base", getAttr("base"));
      this.result.put("loadDialogUrl", "/aioerpom/user/toAdd/");
      
      this.result.put("reset", "reset");
      this.result.put("dialogId", "om_port_user_dialog");
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
  
  public void toCopyAdd()
  {
    String configName = loginConfigName();
    OmPortUser omPortUser = (OmPortUser)OmPortUser.dao.userUpdateById(configName, getParaToInt(0, Integer.valueOf(0)).intValue());
    omPortUser.set("loginPwd", "");
    setAttr("omPortUser", omPortUser);
    setAttr("toAction", "copyAdd");
    render("/WEB-INF/template/port/om/dialog.html");
  }
  
  public void copyAdd()
  {
    String configName = loginConfigName();
    Model model = (Model)getModel(OmPortUser.class);
    String loginName = model.getStr("loginName");
    if (OmPortUser.dao.existObjectByLoginName(configName, 0, loginName))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "该用户已经存在！");
      renderJson(this.result);
      return;
    }
    int unitId = getParaToInt("unit.id").intValue();
    if (OmPortUser.dao.existObjectByLoginUnitId(configName, 0, unitId))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "该单位用户已经存在！");
      renderJson(this.result);
      return;
    }
    model.set("loginPwd", MD5Util.string2MD5(model.getStr("loginPwd")));
    model.set("portKey", StringUtil.getCharAndNumr(16));
    model.set("unitId", Integer.valueOf(unitId));
    
    Model oldModel = OmPortUser.dao.findById(configName, model.getInt("id"));
    model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
    model.set("id", null);
    Boolean flag = Boolean.valueOf(model.save(configName));
    if (flag.booleanValue())
    {
      model.set("rank", model.getInt("id")).update(configName);
      this.result.put("statusCode", Integer.valueOf(200));
      this.result.put("message", "");
      this.result.put("rel", LIST_TABLE);
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
  
  public void toEdit()
  {
    String configName = loginConfigName();
    OmPortUser omPortUser = (OmPortUser)OmPortUser.dao.userUpdateById(configName, getParaToInt(0, Integer.valueOf(0)).intValue());
    omPortUser = (OmPortUser)showManyStorageFullName(omPortUser);
    setAttr("omPortUser", omPortUser);
    setAttr("toAction", "edit");
    render("/WEB-INF/template/port/om/dialog.html");
  }
  
  public void edit()
  {
    String configName = loginConfigName();
    Model model = (Model)getModel(OmPortUser.class);
    String loginName = model.getStr("loginName");
    if (OmPortUser.dao.existObjectByLoginName(configName, model.getInt("id").intValue(), loginName))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "该用户已经存在！");
      renderJson(this.result);
      return;
    }
    int unitId = getParaToInt("unit.id").intValue();
    if (OmPortUser.dao.existObjectByLoginUnitId(configName, model.getInt("id").intValue(), unitId))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "该单位用户已经存在！");
      renderJson(this.result);
      return;
    }
    model.set("unitId", Integer.valueOf(unitId));
    Model oldModel = OmPortUser.dao.findById(configName, model.getInt("id"));
    model.set("portKey", oldModel.get("portKey"));
    model.set("rank", oldModel.get("rank"));
    model.set("status", oldModel.get("status"));
    
    Boolean flag = Boolean.valueOf(model.update(configName));
    if (flag.booleanValue())
    {
      this.result.put("statusCode", Integer.valueOf(200));
      this.result.put("message", "");
      this.result.put("callbackType", "closeCurrent");
      this.result.put("rel", LIST_TABLE);
      this.result.put("url", "aioerpom/user/list/");
      this.result.put("selectedObjectId", model.get("id"));
    }
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void reSetPwd()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    Model omPortUser = OmPortUser.dao.findById(configName, Integer.valueOf(id));
    omPortUser.set("loginPwd", MD5Util.string2MD5("123456"));
    if (omPortUser.update(configName))
    {
      this.result.put("statusCode", Integer.valueOf(200));
      this.result.put("message", "重置后的密码为123456。");
      this.result.put("rel", LIST_TABLE);
    }
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void delete()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    Model omPortUser = OmPortUser.dao.findById(configName, Integer.valueOf(id));
    
    int unitId = omPortUser.getInt("unitId").intValue();
    if (OmPortUser.dao.existOrderByLoginUnitId(configName, unitId))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "该单位有OM订单,不能删除");
      renderJson(this.result);
      return;
    }
    if (OmPortUser.dao.deleteById(configName, Integer.valueOf(id)))
    {
      this.result.put("statusCode", Integer.valueOf(200));
      this.result.put("message", "");
      this.result.put("rel", LIST_TABLE);
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
        OmPortUser.dao.findById(configName, configName, ids[i]).set("rank", seqs[i]).update(configName);
      }
    }
    returnToPage("", "", "", "", "", "");
    renderJson();
  }
  
  public void disableOrEnable()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    OmPortUser omPortUser = (OmPortUser)OmPortUser.dao.findById(configName, Integer.valueOf(id));
    Boolean flag = Boolean.valueOf(false);
    if (omPortUser.getInt("status").intValue() == AioConstants.STATUS_DISABLE) {
      flag = Boolean.valueOf(omPortUser.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE)).update(configName));
    } else {
      flag = Boolean.valueOf(omPortUser.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE)).update(configName));
    }
    if (flag.booleanValue())
    {
      this.result.put("id", Integer.valueOf(id));
      this.result.put("status", omPortUser.getInt("status"));
    }
    renderJson(this.result);
  }
  
  public void toFilter()
  {
    render("/WEB-INF/template/port/om/filter.html");
  }
  
  public void filter()
  {
    this.result.put("searchPar1", getPara("searchPar1"));
    this.result.put("searchValue1", getPara("searchValue1"));
    this.result.put("statusCode", Integer.valueOf(200));
    this.result.put("rel", LIST_TABLE);
    renderJson(this.result);
  }
  
  public void filterData()
  {
    String configName = loginConfigName();
    setAttr("listID", LIST_TABLE);
    String searchPar1 = getPara("searchPar1");
    String searchValue1 = getPara("searchValue1");
    setAttr("searchPar1", searchPar1);
    setAttr("searchValue1", searchValue1);
    int pageNum = 1;
    int numPerPage = getParaToInt("numPerPage").intValue();
    
    Map<String, Object> pageMap = OmPortUser.dao.getPageByCondtion(configName, LIST_TABLE, searchPar1, searchValue1, pageNum, numPerPage, ORDER_FIELD, ORDER_DIRECTION);
    setAttr("pageMap", pageMap);
    

    setAttr("refreshTable", LIST_TABLE);
    render("/WEB-INF/template/port/om/list.html");
  }
  
  public Map<String, Object> showManyStorageFullNames(Map<String, Object> pageMap)
  {
    String configName = loginConfigName();
    List<Model> pageList = (List)pageMap.get("pageList");
    if ((pageList == null) || (pageList.size() == 0)) {
      return pageMap;
    }
    Map<String, String> map = new HashMap();
    String storageAllIds = "";
    for (int i = 0; i < pageList.size(); i++)
    {
      String[] stroageIdArr = ((Model)pageList.get(i)).getStr("storageIds").split(",");
      for (int j = 0; j < stroageIdArr.length; j++) {
        if (!map.containsKey(stroageIdArr[j]))
        {
          map.put(stroageIdArr[j], "");
          storageAllIds = storageAllIds + stroageIdArr[j] + ",";
        }
      }
    }
    if (!storageAllIds.equals("")) {
      storageAllIds = storageAllIds.substring(0, storageAllIds.length() - 1);
    }
    List<Record> storageList = Db.use(configName).find("select id,fullName from b_storage where id in(" + storageAllIds + ")");
    for (int i = 0; i < storageList.size(); i++) {
      map.put(((Record)storageList.get(i)).getInt("id").toString(), ((Record)storageList.get(i)).getStr("fullName"));
    }
    for (int i = 0; i < pageList.size(); i++)
    {
      String storageFullNames = "";
      String[] stroageIdArr = ((Model)pageList.get(i)).getStr("storageIds").split(",");
      for (int j = 0; j < stroageIdArr.length; j++) {
        storageFullNames = storageFullNames + (String)map.get(stroageIdArr[j]) + ",";
      }
      storageFullNames = storageFullNames.substring(0, storageFullNames.length() - 1);
      ((Model)pageList.get(i)).put("storageFullNames", storageFullNames);
    }
    pageMap.put("pageList", pageList);
    return pageMap;
  }
  
  public Model showManyStorageFullName(Model model)
  {
    String configName = loginConfigName();
    List<Record> storageList = Db.use(configName).find("select id,fullName from b_storage where id in(" + model.getStr("storageIds") + ")");
    String storageFullNames = "";
    for (int i = 0; i < storageList.size(); i++) {
      storageFullNames = storageFullNames + ((Record)storageList.get(i)).getStr("fullName") + ",";
    }
    storageFullNames = storageFullNames.substring(0, storageFullNames.length() - 1);
    model.put("storageFullNames", storageFullNames);
    return model;
  }
}
