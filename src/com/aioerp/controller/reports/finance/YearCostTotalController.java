package com.aioerp.controller.reports.finance;

import com.aioerp.controller.BaseController;
import com.aioerp.model.finance.PayType;
import com.aioerp.model.reports.finance.AccountsCostRecords;
import com.aioerp.model.reports.finance.YearCostTotalRecords;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class YearCostTotalController
  extends BaseController
{
/*
  public void index()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String action = getPara(0, "operation");
    String type = getPara(1, "first");
    Integer[] yearEndIds = getParaValuesToInt("yearEndId[]");
    if (yearEndIds == null) {
      yearEndIds = getParaValuesToInt("yearEndId");
    }
    List<String> accTypes = new ArrayList();
    String view = "";
    if ("profit".equals(action))
    {
      accTypes.add("0008");
      accTypes.add("00010");
      accTypes.add("00046");
      view = "yearCostTotalProfitView";
    }
    else if ("asset".equals(action))
    {
      accTypes.add("0001");
      accTypes.add("0007");
      accTypes.add("00011");
      view = "yearCostTotalAssetView";
    }
    List<Model> yearEndList = YearEnd.dao.getList(configName, yearEndIds);
    for (Integer yearEndId : yearEndIds) {
      if (yearEndId.intValue() == 0)
      {
        YearEnd y = new YearEnd();
        y.set("id", Integer.valueOf(0));
        y.set("yearsName", "本年");
        yearEndList.add(y);
      }
    }
    List<Record> accountList = new ArrayList();
    if ("profit".equals(action))
    {
      for (String acc : accTypes)
      {
        Object accT = new ArrayList();
        ((List)accT).add(acc);
        List<Record> accounts = YearCostTotalRecords.dao.getList(configName, yearEndIds, (List)accT, basePrivs(ACCOUNT_PRIVS));
        accountList.addAll(accounts);
      }
      for (Model yearEnd : yearEndList)
      {
        BigDecimal yearGetMoney = BigDecimal.ZERO;
        BigDecimal yearPayMoney = BigDecimal.ZERO;
        for (Record account : accountList) {
          if ("0008".equals(account.getStr("type"))) {
            yearGetMoney = BigDecimalUtils.add(account.getBigDecimal("yearMoney" + yearEnd.getInt("id")), account.getBigDecimal("setMoneys"));
          } else if ("00010".equals(account.getStr("type"))) {
            yearPayMoney = BigDecimalUtils.add(account.getBigDecimal("yearMoney" + yearEnd.getInt("id")), account.getBigDecimal("setMoneys"));
          }
        }
        if (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(yearGetMoney, yearPayMoney))) {
          for (Record account : accountList) {
            if ("00046".equals(account.getStr("type")))
            {
              account.set("yearMoney" + yearEnd.getInt("id"), BigDecimalUtils.sub(yearGetMoney, yearPayMoney));
              break;
            }
          }
        }
      }
    }
    else if ("asset".equals(action))
    {
      for (String acc : accTypes)
      {
        Object accT = new ArrayList();
        ((List)accT).add(acc);
        List<Record> accounts = YearCostTotalRecords.dao.getList(configName, yearEndIds, (List)accT, basePrivs(ACCOUNT_PRIVS));
        accountList.addAll(accounts);
      }
      Object getOrPayList;
      for (??? = yearEndList.iterator(); ???.hasNext(); ((Iterator)getOrPayList).hasNext())
      {
        Model yearEnd = (Model)???.next();
        BigDecimal yearGetMoney = BigDecimal.ZERO;
        BigDecimal yearPayMoney = BigDecimal.ZERO;
        if (yearEnd.getInt("id").intValue() == 0)
        {
          List<String> unitTypes = new ArrayList();
          unitTypes.add("000413");
          unitTypes.add("00013");
          getOrPayList = AccountsCostRecords.dao.getAllUnitMoney(configName, new HashMap(), unitTypes);
          
          BigDecimal shouldGetList = BigDecimal.ZERO;
          BigDecimal shouldPayList = BigDecimal.ZERO;
          for (BigDecimal bigDecimal : (List)getOrPayList) {
            if (BigDecimalUtils.compare(bigDecimal, BigDecimal.ZERO) == -1) {
              shouldPayList = BigDecimalUtils.sub(shouldPayList, bigDecimal);
            } else {
              shouldGetList = BigDecimalUtils.add(shouldGetList, bigDecimal);
            }
          }
          BigDecimal shouldGet = BigDecimal.ZERO;
          BigDecimal shouldPay = BigDecimal.ZERO;
          BigDecimal initGetMoney = BigDecimal.ZERO;
          BigDecimal initPayMoney = BigDecimal.ZERO;
          for (Record record : accountList)
          {
            String recordType = record.getStr("type");
            BigDecimal allMoneys = record.getBigDecimal("yearMoney0");
            if ("000413".equals(recordType))
            {
              shouldGet = BigDecimalUtils.add(shouldGet, allMoneys);
              initGetMoney = record.getBigDecimal("setMoneys");
            }
            if ("00013".equals(recordType))
            {
              shouldPay = BigDecimalUtils.add(shouldPay, allMoneys);
              initPayMoney = record.getBigDecimal("setMoneys");
            }
          }
          for (Record record : accountList)
          {
            String recordType = record.getStr("type");
            BigDecimal allMoneys = record.getBigDecimal("yearMoney0");
            if ("0007".equals(recordType))
            {
              record.set("yearMoney0", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldPayList), shouldPay));
              record.set("setMoneys", BigDecimalUtils.sub(record.getBigDecimal("setMoneys"), initPayMoney));
            }
            if ("00013".equals(recordType))
            {
              record.set("yearMoney0", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldPayList), shouldPay));
              record.set("setMoneys", null);
            }
            if ("0001".equals(recordType))
            {
              record.set("yearMoney0", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldGetList), shouldGet));
              record.set("setMoneys", BigDecimalUtils.sub(record.getBigDecimal("setMoneys"), initGetMoney));
            }
            if ("000413".equals(recordType))
            {
              record.set("yearMoney0", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldGetList), shouldGet));
              record.set("setMoneys", null);
            }
          }
        }
        getOrPayList = accountList.iterator(); continue;Record account = (Record)((Iterator)getOrPayList).next();
        if ("00011".equals(account.getStr("type")))
        {
          List<String> accT = new ArrayList();
          accT.add("0008");
          List<Record> accountsOth = YearCostTotalRecords.dao.getList(configName, yearEndIds, accT, basePrivs(ACCOUNT_PRIVS));
          yearGetMoney = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("yearMoney" + yearEnd.getInt("id")), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
          
          accT = new ArrayList();
          accT.add("00010");
          accountsOth = YearCostTotalRecords.dao.getList(configName, yearEndIds, accT, basePrivs(ACCOUNT_PRIVS));
          yearPayMoney = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("yearMoney" + yearEnd.getInt("id")), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
        }
        if ((BigDecimalUtils.notNullZero(BigDecimalUtils.sub(yearGetMoney, yearPayMoney))) && (
          ("00046".equals(account.getStr("type"))) || ("00011".equals(account.getStr("type"))))) {
          account.set("yearMoney" + yearEnd.getInt("id"), BigDecimalUtils.sub(yearGetMoney, yearPayMoney));
        }
      }
    }
    else
    {
      accountList = YearCostTotalRecords.dao.getList(configName, yearEndIds, accTypes, basePrivs(ACCOUNT_PRIVS));
    }
    for (Record record : accountList) {
      record.set("blank", getBlank(record.getStr("pids")));
    }
    setAttr("accountList", accountList);
    
    setAttr("action", action);
    setAttr("view", view);
    setAttr("yearEndList", yearEndList);
    setAttr("yearEndIds", yearEndIds);
    if ("first".equals(type)) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void print()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    String action = getPara("action", "operation");
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    if ("profit".equals(action)) {
      data.put("reportName", "年度利润对比表");
    } else if ("asset".equals(action)) {
      data.put("reportName", "年度资产负债表");
    }
    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    Integer[] yearEndIds = getParaValuesToInt("yearEndId");
    List<Model> yearEndList = YearEnd.dao.getList(configName, yearEndIds);
    YearEnd y;
    for (Integer yearEndId : yearEndIds) {
      if (yearEndId.intValue() == 0)
      {
        y = new YearEnd();
        y.set("id", Integer.valueOf(0));
        y.set("yearsName", "本年");
        yearEndList.add(y);
      }
    }
    colTitle.add("行号");
    colTitle.add("科目编号");
    colTitle.add("科目全名");
    for (Model model : yearEndList) {
      colTitle.add(model.getStr("yearsName") + "数据");
    }
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    for (Model model : yearEndList) {
      colWidth.add("500");
    }
    List<String> rowData = new ArrayList();
    
    Object accTypes = new ArrayList();
    if ("profit".equals(action))
    {
      ((List)accTypes).add("0008");
      ((List)accTypes).add("00010");
      ((List)accTypes).add("00046");
    }
    else if ("asset".equals(action))
    {
      ((List)accTypes).add("0001");
      ((List)accTypes).add("0007");
      ((List)accTypes).add("00011");
    }
    Object accountList = new ArrayList();
    BigDecimal yearPayMoney;
    if ("profit".equals(action))
    {
      for (String acc : (List)accTypes)
      {
        List<String> accT = new ArrayList();
        accT.add(acc);
        List<Record> accounts = YearCostTotalRecords.dao.getList(configName, yearEndIds, accT, basePrivs(ACCOUNT_PRIVS));
        ((List)accountList).addAll(accounts);
      }
      for (Model yearEnd : yearEndList)
      {
        BigDecimal yearGetMoney = BigDecimal.ZERO;
        BigDecimal yearPayMoney = BigDecimal.ZERO;
        for (Record account : (List)accountList) {
          if ("0008".equals(account.getStr("type"))) {
            yearGetMoney = BigDecimalUtils.add(account.getBigDecimal("yearMoney" + yearEnd.getInt("id")), account.getBigDecimal("setMoneys"));
          } else if ("00010".equals(account.getStr("type"))) {
            yearPayMoney = BigDecimalUtils.add(account.getBigDecimal("yearMoney" + yearEnd.getInt("id")), account.getBigDecimal("setMoneys"));
          }
        }
        if (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(yearGetMoney, yearPayMoney))) {
          for (Record account : (List)accountList) {
            if ("00046".equals(account.getStr("type")))
            {
              account.set("yearMoney" + yearEnd.getInt("id"), BigDecimalUtils.sub(yearGetMoney, yearPayMoney));
              break;
            }
          }
        }
      }
    }
    else if ("asset".equals(action))
    {
      for (String acc : (List)accTypes)
      {
        List<String> accT = new ArrayList();
        accT.add(acc);
        List<Record> accounts = YearCostTotalRecords.dao.getList(configName, yearEndIds, accT, basePrivs(ACCOUNT_PRIVS));
        ((List)accountList).addAll(accounts);
      }
      for (Model yearEnd : yearEndList)
      {
        BigDecimal yearGetMoney = BigDecimal.ZERO;
        yearPayMoney = BigDecimal.ZERO;
        for (Record account : (List)accountList) {
          if ("0001".equals(account.getStr("type"))) {
            yearGetMoney = BigDecimalUtils.add(account.getBigDecimal("yearMoney" + yearEnd.getInt("id")), account.getBigDecimal("setMoneys"));
          } else if ("0007".equals(account.getStr("type"))) {
            yearPayMoney = BigDecimalUtils.add(account.getBigDecimal("yearMoney" + yearEnd.getInt("id")), account.getBigDecimal("setMoneys"));
          }
        }
        Object getOrPayList;
        if (yearEnd.getInt("id").intValue() == 0)
        {
          List<String> unitTypes = new ArrayList();
          unitTypes.add("000413");
          unitTypes.add("00013");
          getOrPayList = AccountsCostRecords.dao.getAllUnitMoney(configName, new HashMap(), unitTypes);
          BigDecimal shouldGetList = BigDecimal.ZERO;
          BigDecimal shouldPayList = BigDecimal.ZERO;
          for (BigDecimal bigDecimal : (List)getOrPayList) {
            if (BigDecimalUtils.compare(bigDecimal, BigDecimal.ZERO) == -1) {
              shouldPayList = BigDecimalUtils.sub(shouldPayList, bigDecimal);
            } else {
              shouldGetList = BigDecimalUtils.add(shouldGetList, bigDecimal);
            }
          }
          BigDecimal shouldGet = BigDecimal.ZERO;
          BigDecimal shouldPay = BigDecimal.ZERO;
          BigDecimal initGetMoney = BigDecimal.ZERO;
          BigDecimal initPayMoney = BigDecimal.ZERO;
          for (Record record : (List)accountList)
          {
            String recordType = record.getStr("type");
            BigDecimal allMoneys = record.getBigDecimal("yearMoney0");
            if ("000413".equals(recordType))
            {
              shouldGet = BigDecimalUtils.add(shouldGet, allMoneys);
              initGetMoney = record.getBigDecimal("setMoneys");
            }
            if ("00013".equals(recordType))
            {
              shouldPay = BigDecimalUtils.add(shouldPay, allMoneys);
              initPayMoney = record.getBigDecimal("setMoneys");
            }
          }
          for (Record record : (List)accountList)
          {
            String recordType = record.getStr("type");
            BigDecimal allMoneys = record.getBigDecimal("yearMoney0");
            if ("0007".equals(recordType))
            {
              record.set("yearMoney0", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldPayList), shouldPay));
              record.set("setMoneys", BigDecimalUtils.sub(record.getBigDecimal("setMoneys"), initPayMoney));
            }
            if ("00013".equals(recordType))
            {
              record.set("yearMoney0", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldPayList), shouldPay));
              record.set("setMoneys", null);
            }
            if ("0001".equals(recordType))
            {
              record.set("yearMoney0", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldGetList), shouldGet));
              record.set("setMoneys", BigDecimalUtils.sub(record.getBigDecimal("setMoneys"), initGetMoney));
            }
            if ("000413".equals(recordType))
            {
              record.set("yearMoney0", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldGetList), shouldGet));
              record.set("setMoneys", null);
            }
          }
        }
        if (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(yearGetMoney, yearPayMoney))) {
          for (getOrPayList = ((List)accountList).iterator(); ((Iterator)getOrPayList).hasNext();)
          {
            Record account = (Record)((Iterator)getOrPayList).next();
            if ("00011".equals(account.getStr("type")))
            {
              List<String> accT = new ArrayList();
              accT.add("0008");
              List<Record> accountsOth = YearCostTotalRecords.dao.getList(configName, yearEndIds, accT, basePrivs(ACCOUNT_PRIVS));
              yearGetMoney = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("yearMoney" + yearEnd.getInt("id")), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
              
              accT = new ArrayList();
              accT.add("00010");
              accountsOth = YearCostTotalRecords.dao.getList(configName, yearEndIds, accT, basePrivs(ACCOUNT_PRIVS));
              yearPayMoney = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("yearMoney" + yearEnd.getInt("id")), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
            }
            if ((BigDecimalUtils.notNullZero(BigDecimalUtils.sub(yearGetMoney, yearPayMoney))) && (
              ("00046".equals(account.getStr("type"))) || ("00011".equals(account.getStr("type"))))) {
              account.set("yearMoney" + yearEnd.getInt("id"), BigDecimalUtils.sub(yearGetMoney, yearPayMoney));
            }
          }
        }
      }
    }
    else
    {
      accountList = YearCostTotalRecords.dao.getList(configName, yearEndIds, (List)accTypes, basePrivs(ACCOUNT_PRIVS));
    }
    for (int i = 0; i < ((List)accountList).size(); i++)
    {
      Record model = (Record)((List)accountList).get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("fullName")));
      for (Model year : yearEndList) {
        rowData.add(trimNull(BigDecimalUtils.add(model.getBigDecimal("yearMoney" + year.getInt("id")), model.getBigDecimal("setMoneys"))));
      }
    }
    if ((accountList == null) || (((List)accountList).size() == 0)) {
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
  
  public void yearEnd()
    throws ParseException
  {
    String configName = loginConfigName();
    Model yearEnd = YearEnd.dao.lastModel(configName);
    List<Model> yearEndList = YearEnd.dao.getList(configName);
    if (yearEnd != null)
    {
      Date endDate = yearEnd.getDate("endDate");
      endDate = DateUtils.addDays(endDate, 1);
      setAttr("startDate", DateUtils.format(endDate));
      setAttr("endDate", DateUtils.getCurrentDate());
    }
    else
    {
      Model payType = PayType.dao.lastModel(configName);
      if (payType != null)
      {
        Timestamp startDate = payType.getTimestamp("payTime");
        setAttr("startDate", DateUtils.format(startDate));
      }
      setAttr("endDate", DateUtils.getCurrentDate());
    }
    setAttr("yearEndList", yearEndList);
    
    Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
    if (paraMap.size() > 0) {
      setAttr("paraMap", paraMap);
    }
    render("yearEnd.html");
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

  */
}
