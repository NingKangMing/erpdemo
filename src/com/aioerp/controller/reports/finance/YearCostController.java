package com.aioerp.controller.reports.finance;

import com.aioerp.controller.BaseController;
import com.aioerp.model.reports.finance.YearCostRecords;
import com.jfinal.plugin.activerecord.Record;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YearCostController
  extends BaseController
{
  public void index()
    throws SQLException, Exception
  {
    String type = getPara(0, "first");
    Map<String, Object> map = new HashMap();
    map.put(ACCOUNT_PRIVS, basePrivs(ACCOUNT_PRIVS));
    
    List<Record> accountList = YearCostRecords.dao.getList(loginConfigName(), map);
    for (Record record : accountList) {
      record.set("blank", getBlank(record.getStr("pids")));
    }
    setAttr("accountList", accountList);
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
    data.put("reportName", "年度费用报表");
    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("科目编号");
    colTitle.add("科目全名");
    colTitle.add("一月 ");
    colTitle.add("二月 ");
    colTitle.add("三月 ");
    colTitle.add("四月 ");
    colTitle.add("五月 ");
    colTitle.add("六月 ");
    colTitle.add("七月 ");
    colTitle.add("八月 ");
    colTitle.add("九月 ");
    colTitle.add("十月 ");
    colTitle.add("十一月 ");
    colTitle.add("十二月 ");
    colTitle.add("合计 ");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    Map<String, Object> map = new HashMap();
    map.put(ACCOUNT_PRIVS, basePrivs(ACCOUNT_PRIVS));
    
    List<Record> accountList = YearCostRecords.dao.getList(loginConfigName(), map);
    for (int i = 0; i < accountList.size(); i++)
    {
      Record model = (Record)accountList.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("fullName")));
      rowData.add(trimNull(model.get("monthMoney1")));
      rowData.add(trimNull(model.get("monthMoney2")));
      rowData.add(trimNull(model.get("monthMoney3")));
      rowData.add(trimNull(model.get("monthMoney4")));
      rowData.add(trimNull(model.get("monthMoney5")));
      rowData.add(trimNull(model.get("monthMoney6")));
      rowData.add(trimNull(model.get("monthMoney7")));
      rowData.add(trimNull(model.get("monthMoney8")));
      rowData.add(trimNull(model.get("monthMoney9")));
      rowData.add(trimNull(model.get("monthMoney10")));
      rowData.add(trimNull(model.get("monthMoney11")));
      rowData.add(trimNull(model.get("monthMoney12")));
      rowData.add(trimNull(model.get("allMonthMoney")));
    }
    if ((accountList == null) || (accountList.size() == 0)) {
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
  
  private String getBlank(String pids)
  {
    if (pids == null) {
      pids = "";
    }
    String[] pidArra = pids.split("}");
    
    int grade = pidArra.length - 2;
    String blank = "";
    for (int i = 0; i < grade; i++) {
      blank = blank + "&nbsp;&nbsp;&nbsp;&nbsp;";
    }
    return blank;
  }
}
