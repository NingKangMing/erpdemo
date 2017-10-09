package com.aioerp.controller.reports.finance;

import com.aioerp.controller.BaseController;
import com.aioerp.model.reports.finance.AccountsCostRecords;
import com.aioerp.model.sys.end.MonthEnd;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetDebtTotalController
  extends BaseController
{
  public void index()
    throws ParseException
  {
    String configName = loginConfigName();
    String type = getPara(0, "first");
    Map<String, Object> map = new HashMap();
    List<String> costTypes = new ArrayList();
    List<String> debtTypes = new ArrayList();
    List<String> equityTypes = new ArrayList();
    costTypes.add("0001");
    debtTypes.add("0007");
    equityTypes.add("00011");
    

    Integer monthEndId = getParaToInt("id");
    Model monthEnd = MonthEnd.dao.findById(configName, monthEndId);
    Model yearEnd = YearEnd.dao.lastModel(configName);
    if (yearEnd != null)
    {
      Date startDate = yearEnd.getDate("endDate");
      startDate = DateUtils.addDays(startDate, 1);
      map.put("startDate", DateUtils.format(startDate));
    }
    if (monthEnd == null)
    {
      monthEnd = MonthEnd.dao.lastModel(configName);
      if (monthEnd != null)
      {
        map.put("endDate", DateUtils.getCurrentDate());
        map.put("isMoney", "yes");
      }
    }
    else
    {
      map.put("endDate", monthEnd.getDate("endDate"));
      map.put("isMoney", "yes");
    }
    map.put(ACCOUNT_PRIVS, basePrivs(ACCOUNT_PRIVS));
    



    List<String> unitTypes = new ArrayList();
    unitTypes.add("000413");
    unitTypes.add("00013");
    
    List<BigDecimal> getOrPayList = AccountsCostRecords.dao.getAllUnitMoney(configName, map, unitTypes);
    BigDecimal shouldGetTotal = BigDecimal.ZERO;
    BigDecimal shouldPayTotal = BigDecimal.ZERO;
    for (BigDecimal unitMoney : getOrPayList) {
      if (BigDecimalUtils.compare(unitMoney, BigDecimal.ZERO) == -1) {
        shouldPayTotal = BigDecimalUtils.sub(shouldPayTotal, unitMoney);
      } else {
        shouldGetTotal = BigDecimalUtils.add(shouldGetTotal, unitMoney);
      }
    }
    BigDecimal shouldGet = BigDecimal.ZERO;
    BigDecimal shouldPay = BigDecimal.ZERO;
    BigDecimal initGetMoney = BigDecimal.ZERO;
    List<Record> costList = AccountsCostRecords.dao.getList(configName, map, costTypes);
    BigDecimal allMoneys;
    for (Record record : costList)
    {
      String recordType = record.getStr("type");
      allMoneys = record.getBigDecimal("allMoneys");
      if ("000413".equals(recordType))
      {
        shouldGet = BigDecimalUtils.add(shouldGet, allMoneys);
        initGetMoney = record.getBigDecimal("setMoneys");
        break;
      }
    }
    List<Record> debtList = AccountsCostRecords.dao.getList(configName, map, debtTypes);
    BigDecimal initPayMoney = BigDecimal.ZERO;
    for (Record record : debtList)
    {
      String recordType = record.getStr("type");
       allMoneys = record.getBigDecimal("allMoneys");
      if ("00013".equals(recordType))
      {
        shouldPay = BigDecimalUtils.add(shouldPay, allMoneys);
        initPayMoney = record.getBigDecimal("setMoneys");
        break;
      }
    }
    for (Record record : debtList)
    {
      String recordType = record.getStr("type");
       allMoneys = record.getBigDecimal("allMoneys");
      if ("0007".equals(recordType))
      {
        record.set("allMoneys", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldPayTotal), shouldPay));
        record.set("setMoneys", BigDecimalUtils.sub(record.getBigDecimal("setMoneys"), initPayMoney));
      }
      if ("00013".equals(recordType))
      {
        record.set("allMoneys", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldPayTotal), shouldPay));
        record.set("setMoneys", null);
        break;
      }
    }
    for (Record record : costList)
    {
      String recordType = record.getStr("type");
       allMoneys = record.getBigDecimal("allMoneys");
      if ("0001".equals(recordType))
      {
        record.set("allMoneys", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldGetTotal), shouldGet));
        record.set("setMoneys", BigDecimalUtils.sub(record.getBigDecimal("setMoneys"), initGetMoney));
      }
      if ("000413".equals(recordType))
      {
        record.set("allMoneys", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldGetTotal), shouldGet));
        record.set("setMoneys", null);
        break;
      }
    }
    List<Record> equityList = AccountsCostRecords.dao.getList(configName, map, equityTypes);
    BigDecimal getAllMoneys = null;
    BigDecimal payAllMoneys = null;
    for (Record record : equityList)
    {
      if ("00011".equals(record.getStr("type")))
      {
        List<String> accT = new ArrayList();
        accT.add("0008");
        List<Record> accountsOth = AccountsCostRecords.dao.getList(configName, map, accT);
        if ((accountsOth != null) && (accountsOth.size() > 0)) {
          getAllMoneys = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("allMoneys"), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
        }
        accT = new ArrayList();
        accT.add("00010");
        accountsOth = AccountsCostRecords.dao.getList(configName, map, accT);
        if ((accountsOth != null) && (accountsOth.size() > 0)) {
          payAllMoneys = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("allMoneys"), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
        }
        if (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getAllMoneys, payAllMoneys))) {
          record.set("allMoneys", BigDecimalUtils.sub(getAllMoneys, payAllMoneys));
        }
      }
      if (("00046".equals(record.getStr("type"))) && 
        (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getAllMoneys, payAllMoneys)))) {
        record.set("allMoneys", BigDecimalUtils.sub(getAllMoneys, payAllMoneys));
      }
    }
    debtList.addAll(equityList);
    List<Record> allList = new ArrayList();
    if (costList.size() > debtList.size()) {
      for (int i = 0; i < costList.size(); i++)
      {
        Record record = new Record();
        record.set("cost", ((Record)costList.get(i)).set("blank", getBlank(((Record)costList.get(i)).getStr("pids"))));
        if (i >= debtList.size() - 1) {
          record.set("debt", new Record());
        } else {
          record.set("debt", ((Record)debtList.get(i)).set("blank", getBlank(((Record)debtList.get(i)).getStr("pids"))));
        }
        allList.add(record);
      }
    } else {
      for (int i = 0; i < debtList.size(); i++)
      {
        Record record = new Record();
        if (i >= costList.size() - 1) {
          record.set("cost", new Record());
        } else {
          record.set("cost", ((Record)costList.get(i)).set("blank", getBlank(((Record)costList.get(i)).getStr("pids"))));
        }
        record.set("debt", ((Record)debtList.get(i)).set("blank", getBlank(((Record)debtList.get(i)).getStr("pids"))));
        allList.add(record);
      }
    }
    setAttr("accountList", allList);
    setAttr("index", Integer.valueOf(allList.size() + 1));
    BigDecimal costMoneyTotle = null;
    if ((costList != null) && (costList.size() > 0)) {
      costMoneyTotle = BigDecimalUtils.add(((Record)costList.get(0)).getBigDecimal("allMoneys"), ((Record)costList.get(0)).getBigDecimal("setMoneys"));
    }
    setAttr("costMoneyTotle", costMoneyTotle);
    BigDecimal debtMoneys = null;
    if ((debtList != null) && (debtList.size() > 0)) {
      debtMoneys = BigDecimalUtils.add(((Record)debtList.get(0)).getBigDecimal("allMoneys"), ((Record)debtList.get(0)).getBigDecimal("setMoneys"));
    }
    BigDecimal equityMoneys = null;
    if ((equityList != null) && (equityList.size() > 0)) {
      equityMoneys = BigDecimalUtils.add(((Record)equityList.get(0)).getBigDecimal("allMoneys"), ((Record)equityList.get(0)).getBigDecimal("setMoneys"));
    }
    setAttr("debtMoneyTotle", BigDecimalUtils.add(debtMoneys, equityMoneys));
    setAttr("id", monthEndId);
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
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "资产负债表");
    

    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("资产类");
    colTitle.add("累计金额(资产类)");
    colTitle.add("负债及权益类");
    colTitle.add("累计金额(负债及权益类)");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    
    Map<String, Object> map = new HashMap();
    List<String> costTypes = new ArrayList();
    costTypes.add("0001");
    List<String> debtTypes = new ArrayList();
    debtTypes.add("0007");
    List<String> equityTypes = new ArrayList();
    equityTypes.add("00011");
    
    Integer monthEndId = getParaToInt("id");
    Model monthEnd = MonthEnd.dao.findById(configName, monthEndId);
    Model yearEnd = YearEnd.dao.lastModel(configName);
    if (yearEnd != null)
    {
      Date startDate = yearEnd.getDate("endDate");
      startDate = DateUtils.addDays(startDate, 1);
      map.put("startDate", DateUtils.format(startDate));
    }
    if (monthEnd == null)
    {
      monthEnd = MonthEnd.dao.lastModel(configName);
      if (monthEnd != null)
      {
        map.put("endDate", DateUtils.getCurrentDate());
        map.put("isMoney", "yes");
      }
    }
    else
    {
      map.put("endDate", monthEnd.getDate("endDate"));
      map.put("isMoney", "yes");
    }
    map.put(ACCOUNT_PRIVS, basePrivs(ACCOUNT_PRIVS));
    List<Record> costList = AccountsCostRecords.dao.getList(configName, map, costTypes);
    
    List<Record> debtList = AccountsCostRecords.dao.getList(configName, map, debtTypes);
    
    List<String> unitTypes = new ArrayList();
    unitTypes.add("000413");
    unitTypes.add("00013");
    List<BigDecimal> getOrPayList = AccountsCostRecords.dao.getAllUnitMoney(configName, map, unitTypes);
    BigDecimal shouldGetList = BigDecimal.ZERO;
    BigDecimal shouldPayList = BigDecimal.ZERO;
    for (BigDecimal bigDecimal : getOrPayList) {
      if (BigDecimalUtils.compare(bigDecimal, BigDecimal.ZERO) == -1) {
        shouldPayList = BigDecimalUtils.sub(shouldPayList, bigDecimal);
      } else {
        shouldGetList = BigDecimalUtils.add(shouldGetList, bigDecimal);
      }
    }
    BigDecimal shouldGet = BigDecimal.ZERO;
    BigDecimal shouldPay = BigDecimal.ZERO;
    
    BigDecimal initGetMoney = BigDecimal.ZERO;
    String recordType;
    for (Record record : costList)
    {
      recordType = record.getStr("type");
      BigDecimal allMoneys = record.getBigDecimal("allMoneys");
      if ("000413".equals(recordType))
      {
        shouldGet = BigDecimalUtils.add(shouldGet, allMoneys);
        initGetMoney = record.getBigDecimal("setMoneys");
        break;
      }
    }
    BigDecimal initPayMoney = BigDecimal.ZERO;
    for (Record record : debtList)
    {
       recordType = record.getStr("type");
      BigDecimal allMoneys = record.getBigDecimal("allMoneys");
      if ("00013".equals(recordType))
      {
        shouldPay = BigDecimalUtils.add(shouldPay, allMoneys);
        initPayMoney = record.getBigDecimal("setMoneys");
        break;
      }
    }
    for (Record record : debtList)
    {
       recordType = record.getStr("type");
      BigDecimal allMoneys = record.getBigDecimal("allMoneys");
      if ("0007".equals(recordType))
      {
        record.set("allMoneys", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldPayList), shouldPay));
        record.set("setMoneys", BigDecimalUtils.sub(record.getBigDecimal("setMoneys"), initPayMoney));
      }
      if ("00013".equals(recordType))
      {
        record.set("allMoneys", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldPayList), shouldPay));
        record.set("setMoneys", null);
        break;
      }
    }
    for (Record record : costList)
    {
       recordType = record.getStr("type");
      BigDecimal allMoneys = record.getBigDecimal("allMoneys");
      if ("0001".equals(recordType))
      {
        record.set("allMoneys", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldGetList), shouldGet));
        record.set("setMoneys", BigDecimalUtils.sub(record.getBigDecimal("setMoneys"), initGetMoney));
      }
      if ("000413".equals(recordType))
      {
        record.set("allMoneys", BigDecimalUtils.sub(BigDecimalUtils.add(allMoneys, shouldGetList), shouldGet));
        record.set("setMoneys", null);
        break;
      }
    }
    List<Record> equityList = AccountsCostRecords.dao.getList(configName, map, equityTypes);
    
    BigDecimal getAllMoneys = null;
    BigDecimal payAllMoneys = null;
    for (int i=0;i<equityList.size();i++)
    {
    	Record record=equityList.get(i);
      if ("00011".equals(record.getStr("type")))
      {
        List<String> accT = new ArrayList();
        accT.add("0008");
        List<Record> accountsOth = AccountsCostRecords.dao.getList(configName, map, accT);
        if ((accountsOth != null) && (accountsOth.size() > 0)) {
          getAllMoneys = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("allMoneys"), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
        }
        accT = new ArrayList();
        accT.add("00010");
        accountsOth = AccountsCostRecords.dao.getList(configName, map, accT);
        if ((accountsOth != null) && (accountsOth.size() > 0)) {
          payAllMoneys = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("allMoneys"), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
        }
        if (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getAllMoneys, payAllMoneys))) {
          record.set("allMoneys", BigDecimalUtils.sub(getAllMoneys, payAllMoneys));
        }
      }
      if (("00046".equals(record.getStr("type"))) && 
        (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getAllMoneys, payAllMoneys)))) {
        record.set("allMoneys", BigDecimalUtils.sub(getAllMoneys, payAllMoneys));
      }
    }
    debtList.addAll((Collection)equityList);
    List<Record> allList = new ArrayList();
    if (costList.size() > debtList.size()) {
      for (int i = 0; i < costList.size(); i++)
      {
        Record record = new Record();
        if (i >= debtList.size() - 1) {
          record.set("debt", new Record());
        } else {
          record.set("debt", debtList.get(i));
        }
        record.set("cost", costList.get(i));
        allList.add(record);
      }
    } else {
      for (int i = 0; i < debtList.size(); i++)
      {
        Record record = new Record();
        if (i >= costList.size() - 1) {
          record.set("cost", new Record());
        } else {
          record.set("cost", costList.get(i));
        }
        record.set("debt", debtList.get(i));
        allList.add(record);
      }
    }
    for (int i = 0; i < allList.size(); i++)
    {
      Record model = (Record)allList.get(i);
      rowData.add(trimNull(i + 1));
      Record cost = (Record)model.get("cost");
      if (cost != null)
      {
        rowData.add(trimNull(cost.get("fullName")));
        rowData.add(trimNull(BigDecimalUtils.add(cost.getBigDecimal("allMoneys"), cost.getBigDecimal("setMoneys"))));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Record debt = (Record)model.get("debt");
      if (cost != null)
      {
        rowData.add(trimNull(debt.get("fullName")));
        rowData.add(trimNull(BigDecimalUtils.add(debt.getBigDecimal("allMoneys"), debt.getBigDecimal("setMoneys"))));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
    }
    rowData.add(trimNull(allList.size() + 1));
    rowData.add("资产总计");
    BigDecimal costMoneys = null;
    if ((costList != null) && (costList.size() > 0)) {
      costMoneys = BigDecimalUtils.add(((Record)costList.get(0)).getBigDecimal("allMoneys"), ((Record)costList.get(0)).getBigDecimal("setMoneys"));
    }
    rowData.add(trimNull(costMoneys));
    rowData.add("负债及权益总计");
    BigDecimal debtMoneys = null;
    if ((debtList != null) && (debtList.size() > 0)) {
      debtMoneys = BigDecimalUtils.add(((Record)debtList.get(0)).getBigDecimal("allMoneys"), ((Record)debtList.get(0)).getBigDecimal("setMoneys"));
    }
    BigDecimal equityMoneys = null;
    if ((equityList != null) && (((List)equityList).size() > 0)) {
      equityMoneys = BigDecimalUtils.add(((Record)((List)equityList).get(0)).getBigDecimal("allMoneys"), ((Record)((List)equityList).get(0)).getBigDecimal("setMoneys"));
    }
    rowData.add(trimNull(BigDecimalUtils.add(debtMoneys, equityMoneys)));
    
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
    
    int grade = pidArra.length - 1;
    String blank = "";
    for (int i = 0; i < grade; i++) {
      blank = blank + "&nbsp;&nbsp;&nbsp;&nbsp;";
    }
    return blank;
  }
}
