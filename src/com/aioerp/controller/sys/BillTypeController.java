package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.sys.BillType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillTypeController
  extends BaseController
{
  public void dialogList()
  {
    String configName = loginConfigName();
    String screenType = getPara("screenType");
    
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
    String orderField = getPara("orderField", "id");
    String orderDirection = getPara("orderDirection", "asc");
    
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    
    Map<String, Object> map = new HashMap();
    if (upObjectId > 0)
    {
      selectedObjectId = upObjectId;
      supId = BillType.getPsupId(configName, upObjectId);
      map = BillType.dao.getSupPage(configName, "", pageNum, numPerPage, orderField, orderDirection, supId, upObjectId, screenType);
    }
    else
    {
      map = BillType.dao.getPageBySupId(configName, "", pageNum, numPerPage, orderField, orderDirection, supId, screenType);
    }
    String btnPattern = getPara("btnPattern", "");
    setAttr("btnPattern", btnPattern);
    
    setAttr("screenType", screenType);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    setAttr("supId", Integer.valueOf(supId));
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    setAttr("toAction", "dialogList");
    render("option.html");
  }
  
  public void dialogSearch()
  {
    String configName = loginConfigName();
    String screenType = getPara("screenType");
    
    String orderField = getPara("orderField", "id");
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
    map.put("screenType", screenType);
    

    Map<String, Object> pageMap = BillType.dao.getPageDialogByTerms(configName, pageNum, numPerPage, orderField, orderDirection, supId, map);
    if ((Integer.parseInt(pageMap.get("totalCount").toString()) == 1) && (isAll != null) && (!"".equals(isAll)))
    {
      List<BillType> list = (List)pageMap.get("pageList");
      BillType s = (BillType)list.get(0);
      
      HashMap<String, Object> result = new HashMap();
      result.put("id", s.getInt("id"));
      result.put("prefix", s.getStr("prefix"));
      result.put("name", s.getStr("name"));
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("obj", result);
      renderJson();
      return;
    }
    String btnPattern = getPara("btnPattern", "");
    setAttr("btnPattern", btnPattern);
    
    setAttr("screenType", screenType);
    
    setAttr("pageMap", pageMap);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("term", term);
    setAttr("termVal", termVal);
    setAttr("objectId", Integer.valueOf(id));
    setAttr("toAction", "dialogSearch");
    render("option.html");
  }
}
