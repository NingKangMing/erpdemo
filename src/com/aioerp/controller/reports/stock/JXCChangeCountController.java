package com.aioerp.controller.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Product;
import com.aioerp.model.reports.stock.JXCChangeCountReports;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JXCChangeCountController
  extends BaseController
{
  public void toSearchDialog()
    throws ParseException
  {
    String modelType = getPara(0, "same");
    setAttr("modelType", modelType);
    




    setStartDateAndEndDate();
    render("/WEB-INF/template/reports/stock/jxcChange/searchDialog.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    Map<String, Object> map = requestPageToMap(null, null);
    
    String type = getPara(0, "first");
    
    setAttr("pageMap", com(map));
    
    columnConfig("cc514");
    
    String returnPage = "";
    if ((type.equals("first")) || (type.equals("search"))) {
      returnPage = "/WEB-INF/template/reports/stock/jxcChange/page.html";
    } else if ((type.equals("page")) || (type.equals("tree")) || (type.equals("line"))) {
      returnPage = "/WEB-INF/template/reports/stock/jxcChange/list.html";
    }
    render(returnPage);
  }
  
  public void tree()
  {
    String modelType = getPara("modelType", "same");
    List<Model> cats = Product.dao.getTreeEnableList(loginConfigName(), basePrivs(PRODUCT_PRIVS));
    String nodeName = "商品信息";
    Iterator<Model> iter = cats.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model product = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", product.get("id"));
      node.put("pId", product.get("supId"));
      node.put("name", product.get("fullName"));
      node.put("url", "reports/stock/jxcChange/dialogSearch/tree?modelType=" + modelType + "&supId=" + product.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", nodeName);
    node.put("url", "reports/stock/jxcChange/dialogSearch/tree?modelType=" + modelType + "&supId=0");
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public Map<String, Object> com(Map<String, Object> map)
    throws ParseException
  {
    String configName = loginConfigName();
    String modelType = getPara("modelType", "");
    String modelTypeName = "";
    String type = getPara(0, "first");
    
    map.put("node", getParaToInt("node", Integer.valueOf(AioConstants.NODE_2)));
    

    String storageId = getPara("storage.id", "");
    String storageFullName = getPara("storage.fullName", "");
    


    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    String aimDiv = getPara("aimDiv", "all");
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), startTime, endTime);
    }
    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    if ((type.equals("first")) || (type.equals("search"))) {
      aimDiv = "all";
    } else if ((!type.equals("tree")) && 
      (!type.equals("page")) && 
      (type.equals("line"))) {
      map.put("node", Integer.valueOf(AioConstants.NODE_1));
    }
    map.put("modelType", modelType);
    map.put("supId", Integer.valueOf(supId));
    map.put("storageId", storageId);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    map.put("aimDiv", aimDiv);
    Map<String, Object> reportPrdSellCount = null;
    


    String listID = "cc_jxcChangeCount";
    String ztreeID = "z_cc_jxcChangeCount";
    modelTypeName = "全能进销存变动报表";
    map.put("listID", listID);
    setAttr("ztreeID", ztreeID);
    
    boolean flag = isInitReportOtherCondition(map);
    if (flag) {
      map.put("shoeType", null);
    }
    reportPrdSellCount = JXCChangeCountReports.dao.reportJXCChangeCount(configName, map);
    setAttr("modelType", modelType);
    setAttr("modelTypeName", modelTypeName);
    setAttr("storageFullName", storageFullName);
    mapToResponse(map);
    return reportPrdSellCount;
  }
  
  public void print()
    throws ParseException
  {
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "全能进销存变动报表");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    List<Record> detailList = new ArrayList();
    

    Map<String, Object> map = new HashMap();
    map.put("pageNum", Integer.valueOf(AioConstants.START_PAGE));
    map.put("numPerPage", getParaToInt("totalCount", Integer.valueOf(9999999)));
    map.put("orderField", getPara("orderField", ORDER_FIELD));
    map.put("orderDirection", getPara("orderDirection", ORDER_DIRECTION));
    if (String.valueOf(map.get("numPerPage")).equals("0")) {
      map.put("numPerPage", Integer.valueOf(1));
    }
    Map<String, Object> pageMap = com(map);
    if (pageMap != null) {
      detailList = (List)pageMap.get("pageList");
    }
    printCommonData(headData);
    headData.add("查询时间:" + getPara("startTime", "") + " 至 " + getPara("endTime", ""));
    

    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简称");
    colTitle.add("商品拼音");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    

    colTitle.add("上期数量");
    colTitle.add("上期金额");
    

    colTitle.add("进货数量");
    colTitle.add("进货金额");
    colTitle.add("销售退货数量");
    colTitle.add("销售退货金额");
    colTitle.add("报溢数量");
    colTitle.add("报溢金额");
    colTitle.add("同价调拨入库数量");
    colTitle.add("同价调拨入库金额");
    colTitle.add("变价调拨入库数量");
    colTitle.add("变价调拨入库金额");
    colTitle.add("销售换货入库数量");
    colTitle.add("销售换货入库金额");
    colTitle.add("进货换货入库数量");
    colTitle.add("进货换货入库金额");
    colTitle.add("其它入库数量");
    colTitle.add("其它入库金额");
    colTitle.add("入库合计数量");
    colTitle.add("入库合计金额");
    

    colTitle.add("销售数量");
    colTitle.add("销售金额");
    colTitle.add("进货退货数量");
    colTitle.add("进货退货金额");
    colTitle.add("报损数量");
    colTitle.add("报损金额");
    colTitle.add("同价调拨出库数量");
    colTitle.add("同价调拨出库金额");
    colTitle.add("变价调拨出库数量");
    colTitle.add("变价调拨出库金额");
    colTitle.add("销售换货出库数量");
    colTitle.add("销售换货出库金额");
    colTitle.add("进货换货出库数量");
    colTitle.add("进货换货出库金额");
    colTitle.add("其它出库数量");
    colTitle.add("其它出库金额");
    colTitle.add("出库合计数量");
    colTitle.add("出库合计金额");
    

    colTitle.add("本期数量");
    colTitle.add("本期金额");
    


    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    boolean hasCostPermitted = hasPermitted("1101-s");
    for (int i = 0; i < detailList.size(); i++)
    {
      Record detail = (Record)detailList.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("fullName")));
      rowData.add(trimNull(detail.getStr("smallName")));
      rowData.add(trimNull(detail.getStr("spell")));
      rowData.add(trimNull(detail.getStr("standard")));
      rowData.add(trimNull(detail.getStr("model")));
      rowData.add(trimNull(detail.getStr("field")));
      
      rowData.add(trimNull(detail.getBigDecimal("preAmount")));
      rowData.add(trimNull(detail.getBigDecimal("preMoney"), hasCostPermitted));
      

      rowData.add(trimNull(detail.getBigDecimal("pruchaseAmount")));
      rowData.add(trimNull(detail.getBigDecimal("pruchaseMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("sellReturnAmount")));
      rowData.add(trimNull(detail.getBigDecimal("sellReturnMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("overflowAmount")));
      rowData.add(trimNull(detail.getBigDecimal("overflowMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("parityallotInAmount")));
      rowData.add(trimNull(detail.getBigDecimal("parityallotInMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("difftallotInAmount")));
      rowData.add(trimNull(detail.getBigDecimal("difftallotInMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("sellBarterInAmount")));
      rowData.add(trimNull(detail.getBigDecimal("sellBarterInMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("pruchaseBarterInAmount")));
      rowData.add(trimNull(detail.getBigDecimal("pruchaseBarterInMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("otherinAmount")));
      rowData.add(trimNull(detail.getBigDecimal("otherinMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("inAllAmount")));
      rowData.add(trimNull(detail.getBigDecimal("inAllMoney"), hasCostPermitted));
      

      rowData.add(trimNull(detail.getBigDecimal("sellAmount")));
      rowData.add(trimNull(detail.getBigDecimal("sellMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("pruchaseReturnAmount")));
      rowData.add(trimNull(detail.getBigDecimal("pruchaseReturnMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("breakageAmount")));
      rowData.add(trimNull(detail.getBigDecimal("breakageMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("parityalloOutAmount")));
      rowData.add(trimNull(detail.getBigDecimal("parityalloOutMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("difftallotOutAmount")));
      rowData.add(trimNull(detail.getBigDecimal("difftallotOutMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("sellBarterOutAmount")));
      rowData.add(trimNull(detail.getBigDecimal("sellBarterOutMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("pruchaseBarterOutAmount")));
      rowData.add(trimNull(detail.getBigDecimal("pruchaseBarterOutMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("otheroutAmount")));
      rowData.add(trimNull(detail.getBigDecimal("otheroutMoney"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("outAllAmount")));
      rowData.add(trimNull(detail.getBigDecimal("outAllMoney"), hasCostPermitted));
      

      rowData.add(trimNull(detail.getBigDecimal("afterAmount")));
      rowData.add(trimNull(detail.getBigDecimal("afterMoney"), hasCostPermitted));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
