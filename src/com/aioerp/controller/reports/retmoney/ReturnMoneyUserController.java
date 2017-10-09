package com.aioerp.controller.reports.retmoney;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.reports.retmoney.ReturnMoneyUser;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Model;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReturnMoneyUserController
  extends BaseController
{
  public void search()
    throws Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "username");
    String orderDirection = getPara("orderDirection", "asc");
    String type = getPara(0, "first");
    
    Map<String, Object> map = new HashMap();
    map.put("startTime", getPara("startTime"));
    map.put("endTime", getPara("endTime"));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    Map<String, Object> pageMap = ReturnMoneyUser.dao.getPage(configName, pageNum, numPerPage, "returnMomeyUserList", orderField, orderDirection, map);
    
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("params", map);
    setAttr("pageMap", pageMap);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cw506", AioConstants.STATUS_ENABLE));
    if ("first".equals(type)) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "制单人收款统计");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startTime", "") + "至" + getPara("endTime", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("职员编号");
    colTitle.add("职员全名");
    colTitle.add("用户名");
    
    colTitle.add("收款总额");
    colTitle.add("现金收款");
    colTitle.add("银行收款");
    

    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    
    Map<String, Object> map = new HashMap();
    map.put("startTime", getPara("startTime"));
    map.put("endTime", getPara("endTime"));
    map.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    Map<String, Object> pageMap = ReturnMoneyUser.dao.getPage(loginConfigName(), 1, totalCount, "returnMomeyUserList", orderField, orderDirection, map);
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model model = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      Model staff = (Model)model.get("staff");
      if (staff != null)
      {
        rowData.add(trimNull(staff.get("code")));
        rowData.add(trimNull(staff.get("name")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(model.get("username")));
      rowData.add(trimNull(BigDecimalUtils.add(model.getBigDecimal("cashGetMoneys"), model.getBigDecimal("bankGetMoneys"))));
      rowData.add(trimNull(model.get("cashGetMoneys")));
      rowData.add(trimNull(model.get("bankGetMoneys")));
    }
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
