package com.aioerp.controller.reports.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.db.reports.stock.StockBatchTail;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.stock.StockRecords;
import com.aioerp.model.sys.BillType;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockBatchTailController
  extends BaseController
{
  public static String listID = "stockBatchTailList";
  
  public void in()
    throws ParseException
  {
    Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
    if (paraMap.size() > 0) {
      setAttr("paraMap", paraMap);
    }
    setStartDateAndEndDate();
    render("searchDialog.html");
  }
  
  public void index()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), getPara("startTime"), getPara("endTime"));
    }
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "sckRos.id");
    String orderDirection = getPara("orderDirection", "asc");
    int productId = getParaToInt("product.id", getParaToInt("productId", Integer.valueOf(0))).intValue();
    String productName = getPara("product.fullName", getPara("productName"));
    String batchNum = getPara("batchNum");
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    Map<String, Object> map = StockBatchTail.dao.getListByParam(configName, basePrivs(STORAGE_PRIVS), basePrivs(UNIT_PRIVS), productId, batchNum, startTime, endTime, listID, pageNum, numPerPage, orderField, orderDirection);
    

    columnConfig("cc525");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    setAttr("productId", Integer.valueOf(productId));
    setAttr("productName", productName);
    setAttr("batchNum", batchNum);
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    
    render("page.html");
  }
  
  public void list()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "sckRos.id");
    String orderDirection = getPara("orderDirection", "asc");
    Integer productId = getParaToInt("productId");
    String productName = getPara("productName");
    String batchNum = getPara("batchNum");
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    Map<String, Object> map = StockBatchTail.dao.getListByParam(configName, basePrivs(STORAGE_PRIVS), basePrivs(UNIT_PRIVS), productId.intValue(), batchNum, startTime, endTime, listID, pageNum, numPerPage, orderField, orderDirection);
    

    columnConfig("cc525");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    setAttr("productId", productId);
    setAttr("productName", productName);
    setAttr("batchNum", batchNum);
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    
    render("list.html");
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "商品批次跟踪");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("基本条码");
    colTitle.add("基本单位");
    colTitle.add("辅助单位");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("单据类型");
    colTitle.add("单据字头");
    colTitle.add("时间");
    colTitle.add("单据编号");
    colTitle.add("摘要");
    colTitle.add("批号");
    colTitle.add("生产日期");
    colTitle.add("计量单位");
    colTitle.add("数量");
    colTitle.add("单价");
    colTitle.add("金额");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    

    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "sckRos.id");
    String orderDirection = getPara("orderDirection", "asc");
    Integer productId = getParaToInt("productId");
    
    String batchNum = getPara("batchNum");
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    Map<String, Object> pageMap = StockBatchTail.dao.getListByParam(configName, basePrivs(STORAGE_PRIVS), basePrivs(UNIT_PRIVS), productId.intValue(), batchNum, startTime, endTime, listID, pageNum, numPerPage, orderField, orderDirection);
    
    boolean hasCostPermitted = hasPermitted("1101-s");
    

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
      
      Product pro = (Product)record.get("pro");
      rowData.add(trimNull(pro.get("code")));
      rowData.add(trimNull(pro.get("fullName")));
      rowData.add(trimNull(pro.get("smallName")));
      rowData.add(trimNull(pro.get("spell")));
      rowData.add(trimNull(pro.get("standard")));
      rowData.add(trimNull(pro.get("model")));
      rowData.add(trimNull(pro.get("field")));
      rowData.add(trimNull(pro.get("barCode1")));
      rowData.add(trimNull(pro.get("calculateUnit1")));
      rowData.add(trimNull(pro.get("assistUnit")));
      
      Unit unit = (Unit)record.get("bunit");
      rowData.add(trimNull(unit.get("code")));
      rowData.add(trimNull(unit.get("fullName")));
      
      Storage sge = (Storage)record.get("sge");
      rowData.add(trimNull(sge.get("code")));
      rowData.add(trimNull(sge.get("fullName")));
      
      BillType btype = (BillType)record.get("btype");
      rowData.add(trimNull(btype.get("name")));
      rowData.add(trimNull(btype.get("prefix")));
      
      StockRecords sckRos = (StockRecords)record.get("sckRos");
      rowData.add(trimNull(sckRos.get("recodeTime")));
      rowData.add(trimNull(sckRos.get("billCode")));
      rowData.add(trimNull(sckRos.get("billAbstract")));
      rowData.add(trimNull(sckRos.get("batch")));
      rowData.add(trimNull(sckRos.get("produceDate")));
      
      rowData.add(trimNull(record.get("unitName")));
      rowData.add(trimNull(record.get("amount")));
      
      int btypeId = btype.getInt("id").intValue();
      if ((!hasCostPermitted) && ((btypeId == AioConstants.BILL_ROW_TYPE5) || (btypeId == AioConstants.BILL_ROW_TYPE12) || (btypeId == AioConstants.BILL_ROW_TYPE13) || (btypeId == AioConstants.BILL_ROW_TYPE9)))
      {
        rowData.add("***");
        rowData.add("***");
      }
      else
      {
        rowData.add(trimNull(sckRos.get("price")));
        rowData.add(trimNull(record.get("costMoney")));
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
