package com.aioerp.controller.reports.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.finance.PayType;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.reports.finance.AccountsCostRecords;
import com.aioerp.model.sys.end.MonthEnd;
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
import java.util.List;
import java.util.Map;

public class AccountsCostTotalController
  extends BaseController
{
  public void index()
    throws SQLException, Exception
  {
    String action = getPara(0, "operation");
    String type = getPara(1, "first");
    String configName = loginConfigName();
    Map<String, Object> map = new HashMap();
    map.put(ACCOUNT_PRIVS, basePrivs(ACCOUNT_PRIVS));
    List<String> accTypes = new ArrayList();
    String view = "";
    if ("operation".equals(action))
    {
      accTypes.add("0008");
      accTypes.add("00010");
      accTypes.add("00046");
      view = "operationStateView";
    }
    else if ("cashBank".equals(action))
    {
      accTypes.add("0003");
      accTypes.add("0004");
      view = "cashBankStateView";
    }
    else if ("otherIncome".equals(action))
    {
      accTypes.add("00021");
      view = "otherIncomeStateView";
    }
    else if ("fixedAssets".equals(action))
    {
      accTypes.add("0002");
      view = "fixedAssetsStateView";
    }
    else if ("assetsDebtTree".equals(action))
    {
      accTypes.add("0001");
      accTypes.add("0007");
      accTypes.add("00011");
      view = "assetsDebtTreeStateView";
    }
    else if ("costTotal".equals(action))
    {
      accTypes.add("00036");
      view = "costTotalStateView";
    }
    Integer monthEndId = getParaToInt("id");
    Model monthEnd = MonthEnd.dao.findById(configName, monthEndId);
    if (monthEnd == null)
    {
      monthEnd = MonthEnd.dao.lastModel(configName);
      if (monthEnd != null)
      {
        Date endDate = monthEnd.getDate("endDate");
        endDate = DateUtils.addDays(endDate, 1);
        map.put("startDate", DateUtils.format(endDate));
        map.put("endDate", DateUtils.getCurrentDate());
      }
      else
      {
        Model yearEnd = YearEnd.dao.lastModel(configName);
        if (yearEnd != null)
        {
          Date startDate = yearEnd.getDate("endDate");
          startDate = DateUtils.addDays(startDate, 1);
          map.put("startDate", DateUtils.format(startDate));
        }
        map.put("endDate", DateUtils.getCurrentDate());
      }
    }
    else
    {
      Date endDate = monthEnd.getDate("endDate");
      Date startDate = monthEnd.getDate("startDate");
      
      map.put("startDate", DateUtils.format(startDate));
      map.put("endDate", DateUtils.format(endDate));
    }
    List<Record> accountList = new ArrayList();
    List<Record> costList;
    if ("operation".equals(action))
    {
      List<String> accT = new ArrayList();
      accT.add("0008");
      List<Record> accounts = AccountsCostRecords.dao.getList(configName, map, accT);
      
      accountList.addAll(accounts);
      BigDecimal getMonthMoneys = null;
      if ((accounts != null) && (accounts.size() > 0)) {
        getMonthMoneys = ((Record)accounts.get(0)).getBigDecimal("monthMoneys");
      }
      BigDecimal getAllMoneys = null;
      if ((accounts != null) && (accounts.size() > 0)) {
        getAllMoneys = BigDecimalUtils.add(((Record)accounts.get(0)).getBigDecimal("allMoneys"), ((Record)accounts.get(0)).getBigDecimal("setMoneys"));
      }
      accT = new ArrayList();
      accT.add("00010");
      accounts = AccountsCostRecords.dao.getList(configName, map, accT);
      
      accountList.addAll(accounts);
      BigDecimal payMonthMoneys = null;
      if ((accounts != null) && (accounts.size() > 0)) {
        payMonthMoneys = ((Record)accounts.get(0)).getBigDecimal("monthMoneys");
      }
      BigDecimal payAllMoneys = null;
      if ((accounts != null) && (accounts.size() > 0)) {
        payAllMoneys = BigDecimalUtils.add(((Record)accounts.get(0)).getBigDecimal("allMoneys"), ((Record)accounts.get(0)).getBigDecimal("setMoneys"));
      }
      accT = new ArrayList();
      accT.add("00046");
      accounts = AccountsCostRecords.dao.getList(configName, map, accT);
      for (Record record : accounts)
      {
        if (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getMonthMoneys, payMonthMoneys))) {
          record.set("monthMoneys", BigDecimalUtils.sub(getMonthMoneys, payMonthMoneys));
        }
        if (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getAllMoneys, payAllMoneys))) {
          record.set("allMoneys", BigDecimalUtils.sub(getAllMoneys, payAllMoneys));
        }
      }
      accountList.addAll(accounts);
    }
    else if ("assetsDebtTree".equals(action))
    {
      accTypes.add("0001");
      accTypes.add("0007");
      accTypes.add("00011");
      
      List<String> accT = new ArrayList();
      accT.add("0001");
      costList = AccountsCostRecords.dao.getList(configName, map, accT);
      accountList.addAll(costList);
      

      accT = new ArrayList();
      accT.add("0007");
      List<Record> debtList = AccountsCostRecords.dao.getList(configName, map, accT);
      accountList.addAll(debtList);
      
      accT = new ArrayList();
      accT.add("00011");
      List<Record> equityList = AccountsCostRecords.dao.getList(configName, map, accT);
      BigDecimal getMonthMoneys = null;
      BigDecimal getAllMoneys = null;
      BigDecimal payMonthMoneys = null;
      BigDecimal payAllMoneys = null;
      for (Record record : equityList)
      {
        if ("00011".equals(record.getStr("type")))
        {
          accT = new ArrayList();
          accT.add("0008");
          List<Record> accountsOth = AccountsCostRecords.dao.getList(configName, map, accT);
          if ((accountsOth != null) && (accountsOth.size() > 0))
          {
            getMonthMoneys = ((Record)accountsOth.get(0)).getBigDecimal("monthMoneys");
            getAllMoneys = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("allMoneys"), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
          }
          accT = new ArrayList();
          accT.add("00010");
          accountsOth = AccountsCostRecords.dao.getList(configName, map, accT);
          if ((accountsOth != null) && (accountsOth.size() > 0))
          {
            payMonthMoneys = ((Record)accountsOth.get(0)).getBigDecimal("monthMoneys");
            payAllMoneys = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("allMoneys"), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
          }
        }
        if ((("00011".equals(record.getStr("type"))) || ("00046".equals(record.getStr("type")))) && 
          (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getMonthMoneys, payMonthMoneys)))) {
          record.set("monthMoneys", BigDecimalUtils.sub(getMonthMoneys, payMonthMoneys));
        }
        if (("00011".equals(record.getStr("type"))) && 
          (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getAllMoneys, payAllMoneys)))) {
          record.set("allMoneys", BigDecimalUtils.sub(getAllMoneys, payAllMoneys));
        }
        if (("00046".equals(record.getStr("type"))) && 
          (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getAllMoneys, payAllMoneys)))) {
          record.set("allMoneys", BigDecimalUtils.sub(getMonthMoneys, payMonthMoneys));
        }
      }
      accountList.addAll(equityList);
    }
    else
    {
      accountList = AccountsCostRecords.dao.getList(configName, map, accTypes);
    }
    for (Record record : accountList) {
      record.set("blank", getBlank(record.getStr("pids")));
    }
    setAttr("accountList", accountList);
    setAttr("action", action);
    setAttr("view", view);
    setAttr("id", monthEndId);
    if ("first".equals(type)) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void detail()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    
    Integer accountId = getParaToInt("accountId");
    Map<String, Object> map = new HashMap();
    map.put("startTime", getPara("startTime"));
    map.put("endTime", getPara("endTime"));
    map.put("accountId", accountId);
    String isRcw = getPara("isRcw", "");
    map.put("isRcw", getParaToInt("isRcw"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    Map<String, Object> pageMap = null;
    if (AioConstants.CALL_PROC) {
      pageMap = AccountsCostRecords.dao.getDetailPageByPro(configName, pageNum, numPerPage, "costTotalDetailPage", map);
    } else {
      pageMap = AccountsCostRecords.dao.getDetailPage(configName, pageNum, numPerPage, "costTotalDetailPage", map);
    }
    BigDecimal beforeMoney = AccountsCostRecords.dao.getBeforeMoney(configName, pageNum, numPerPage, map);
    setAttr("beforeMoney", beforeMoney);
    List<Model> rList = (List)pageMap.get("pageList");
    for (Model model : rList)
    {
      BigDecimal addMoney = model.getBigDecimal("addMoney");
      BigDecimal subMoney = model.getBigDecimal("subMoney");
      beforeMoney = BigDecimalUtils.sub(BigDecimalUtils.add(beforeMoney, addMoney), subMoney);
      model.put("money", beforeMoney);
    }
    setAttr("params", map);
    setAttr("isRcw", isRcw);
    setAttr("pageMap", pageMap);
    setAttr("account", Accounts.dao.findById(configName, accountId));
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cw512", AioConstants.STATUS_ENABLE));
    
    render("detail.html");
  }
  
  public void toHistory()
    throws ParseException
  {
    String configName = loginConfigName();
    Model monthEnd = MonthEnd.dao.lastModel(configName);
    
    List<Model> monthEndList = MonthEnd.dao.getList(configName);
    if (monthEnd != null)
    {
      Date endDate = monthEnd.getDate("endDate");
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
      Model yearEnd = YearEnd.dao.lastModel(configName);
      if (yearEnd != null)
      {
        Date startDate = yearEnd.getDate("endDate");
        startDate = DateUtils.addDays(startDate, 1);
        setAttr("startDate", DateUtils.format(startDate));
      }
      setAttr("endDate", DateUtils.getCurrentDate());
    }
    setAttr("monthEndList", monthEndList);
    
    Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
    if (paraMap.size() > 0) {
      setAttr("paraMap", paraMap);
    }
    render("history.html");
  }
  
  public void print()
    throws ParseException
  {
    String configName = loginConfigName();
    String action = getPara("action", "operation");
    
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    if ("operation".equals(action)) {
      data.put("reportName", "利润表");
    } else if ("cashBank".equals(action)) {
      data.put("reportName", "现金银行");
    } else if ("otherIncome".equals(action)) {
      data.put("reportName", "其它收入");
    } else if ("fixedAssets".equals(action)) {
      data.put("reportName", "固定资产");
    } else if ("assetsDebtTree".equals(action)) {
      data.put("reportName", "资产负债表（树形表）");
    } else if ("costTotal".equals(action)) {
      data.put("reportName", "费用合计统计");
    }
    List<String> headData = new ArrayList();
    

    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("科目编号");
    colTitle.add("科目全名");
    colTitle.add("本月发生额");
    colTitle.add("累计金额");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    


    Map<String, Object> map = new HashMap();
    map.put(ACCOUNT_PRIVS, basePrivs(ACCOUNT_PRIVS));
    List<String> accTypes = new ArrayList();
    if ("operation".equals(action))
    {
      accTypes.add("0008");
      accTypes.add("00010");
      accTypes.add("00046");
    }
    else if ("cashBank".equals(action))
    {
      accTypes.add("0003");
      accTypes.add("0004");
    }
    else if ("otherIncome".equals(action))
    {
      accTypes.add("00021");
    }
    else if ("fixedAssets".equals(action))
    {
      accTypes.add("0002");
    }
    else if ("assetsDebtTree".equals(action))
    {
      accTypes.add("0001");
      accTypes.add("0007");
      accTypes.add("00011");
    }
    else if ("costTotal".equals(action))
    {
      accTypes.add("00036");
    }
    Integer monthEndId = getParaToInt("id");
    Model monthEnd = MonthEnd.dao.findById(configName, monthEndId);
    if (monthEnd == null)
    {
      monthEnd = MonthEnd.dao.lastModel(configName);
      if (monthEnd != null)
      {
        Date endDate = monthEnd.getDate("endDate");
        endDate = DateUtils.addDays(endDate, 1);
        map.put("startDate", DateUtils.format(endDate));
        map.put("endDate", DateUtils.getCurrentDate());
      }
      else
      {
        Model yearEnd = YearEnd.dao.lastModel(configName);
        if (yearEnd != null)
        {
          Date startDate = yearEnd.getDate("endDate");
          startDate = DateUtils.addDays(startDate, 1);
          map.put("startDate", DateUtils.format(startDate));
        }
        map.put("endDate", DateUtils.getCurrentDate());
      }
    }
    else
    {
      Date endDate = monthEnd.getDate("endDate");
      Date startDate = monthEnd.getDate("startDate");
      
      map.put("startDate", DateUtils.format(startDate));
      map.put("endDate", DateUtils.format(endDate));
    }
    List<Record> detailList = new ArrayList();
    if ("operation".equals(action))
    {
      List<String> accT = new ArrayList();
      accT.add("0008");
      List<Record> accounts = AccountsCostRecords.dao.getList(configName, map, accT);
      detailList.addAll(accounts);
      BigDecimal getMonthMoneys = null;
      BigDecimal getAllMoneys = null;
      if ((accounts != null) && (accounts.size() > 0))
      {
        getMonthMoneys = ((Record)accounts.get(0)).getBigDecimal("monthMoneys");
        getAllMoneys = BigDecimalUtils.add(((Record)accounts.get(0)).getBigDecimal("allMoneys"), ((Record)accounts.get(0)).getBigDecimal("setMoneys"));
      }
      accT = new ArrayList();
      accT.add("00010");
      accounts = AccountsCostRecords.dao.getList(configName, map, accT);
      detailList.addAll(accounts);
      BigDecimal payMonthMoneys = null;
      BigDecimal payAllMoneys = null;
      if ((accounts != null) && (accounts.size() > 0))
      {
        payMonthMoneys = ((Record)accounts.get(0)).getBigDecimal("monthMoneys");
        payAllMoneys = BigDecimalUtils.add(((Record)accounts.get(0)).getBigDecimal("allMoneys"), ((Record)accounts.get(0)).getBigDecimal("setMoneys"));
      }
      accT = new ArrayList();
      accT.add("00046");
      accounts = AccountsCostRecords.dao.getList(configName, map, accT);
      for (Record record : accounts)
      {
        if (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getMonthMoneys, payMonthMoneys))) {
          record.set("monthMoneys", BigDecimalUtils.sub(getMonthMoneys, payMonthMoneys));
        }
        if (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getAllMoneys, payAllMoneys))) {
          record.set("allMoneys", BigDecimalUtils.sub(getAllMoneys, payAllMoneys));
        }
      }
      detailList.addAll(accounts);
    }
    else if ("assetsDebtTree".equals(action))
    {
      List<String> accT = new ArrayList();
      accT.add("0001");
      List<Record> accounts = AccountsCostRecords.dao.getList(configName, map, accT);
      detailList.addAll(accounts);
      

      accT = new ArrayList();
      accT.add("0007");
      accounts = AccountsCostRecords.dao.getList(configName, map, accT);
      detailList.addAll(accounts);
      

      accT = new ArrayList();
      accT.add("00011");
      accounts = AccountsCostRecords.dao.getList(configName, map, accT);
      BigDecimal getMonthMoneys = null;
      BigDecimal getAllMoneys = null;
      BigDecimal payMonthMoneys = null;
      BigDecimal payAllMoneys = null;
      for (Record record : accounts)
      {
        if ("00011".equals(record.getStr("type")))
        {
          accT = new ArrayList();
          accT.add("0008");
          List<Record> accountsOth = AccountsCostRecords.dao.getList(configName, map, accT);
          if ((accountsOth != null) && (accountsOth.size() > 0))
          {
            getMonthMoneys = ((Record)accountsOth.get(0)).getBigDecimal("monthMoneys");
            getAllMoneys = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("allMoneys"), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
          }
          accT = new ArrayList();
          accT.add("00010");
          accountsOth = AccountsCostRecords.dao.getList(configName, map, accT);
          if ((accountsOth != null) && (accountsOth.size() > 0))
          {
            payMonthMoneys = ((Record)accountsOth.get(0)).getBigDecimal("monthMoneys");
            payAllMoneys = BigDecimalUtils.add(((Record)accountsOth.get(0)).getBigDecimal("allMoneys"), ((Record)accountsOth.get(0)).getBigDecimal("setMoneys"));
          }
        }
        if ((("00011".equals(record.getStr("type"))) || ("00046".equals(record.getStr("type")))) && 
          (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getMonthMoneys, payMonthMoneys)))) {
          record.set("monthMoneys", BigDecimalUtils.sub(getMonthMoneys, payMonthMoneys));
        }
        if (("00011".equals(record.getStr("type"))) && 
          (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getAllMoneys, payAllMoneys)))) {
          record.set("allMoneys", BigDecimalUtils.sub(getAllMoneys, payAllMoneys));
        }
        if (("00046".equals(record.getStr("type"))) && 
          (BigDecimalUtils.notNullZero(BigDecimalUtils.sub(getAllMoneys, payAllMoneys)))) {
          record.set("allMoneys", BigDecimalUtils.sub(getMonthMoneys, payMonthMoneys));
        }
      }
      detailList.addAll(accounts);
    }
    else
    {
      detailList = AccountsCostRecords.dao.getList(configName, map, accTypes);
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Record model = (Record)detailList.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(model.getStr("code")));
      rowData.add(trimNull(model.getStr("fullName")));
      rowData.add(trimNull(model.get("monthMoneys")));
      rowData.add(trimNull(BigDecimalUtils.sub(model.getBigDecimal("allMoneys"), model.getBigDecimal("setMoneys"))));
    }
    if ((detailList == null) || (detailList.size() == 0)) {
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
  
  public void printDetail()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "财务明细账本");
    List<String> headData = new ArrayList();
    headData.add("查询时间:" + getPara("startTime", "") + "至" + getPara("endTime", ""));
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("单据类型");
    colTitle.add("日期 ");
    colTitle.add("单据编号");
    colTitle.add("职员编号");
    colTitle.add("职员全名");
    colTitle.add("科目编号");
    colTitle.add("科目全名");
    colTitle.add("增加金额");
    colTitle.add("减少金额");
    colTitle.add("金额");
    colTitle.add("摘要");
    

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
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    
    String orderField = getPara("orderField", "");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    Integer accountId = getParaToInt("accountId");
    Map<String, Object> map = new HashMap();
    map.put("startTime", getPara("startTime"));
    map.put("endTime", getPara("endTime"));
    map.put("accountId", accountId);
    map.put("isRcw", getParaToInt("isRcw"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    Map<String, Object> pageMap = AccountsCostRecords.dao.getDetailPage(configName, 1, totalCount, "costTotalDetailList", map);
    BigDecimal beforeMoney = AccountsCostRecords.dao.getBeforeMoney(configName, 1, totalCount, map);
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model model = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(model.get("billTypeName")));
      rowData.add(trimNull(model.get("recodeDate")));
      rowData.add(trimNull(model.get("billCode")));
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
      Model accounts = (Model)model.get("accounts");
      if (accounts != null)
      {
        rowData.add(trimNull(accounts.get("code")));
        rowData.add(trimNull(accounts.get("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      BigDecimal addMoney = model.getBigDecimal("addMoney");
      BigDecimal subMoney = model.getBigDecimal("subMoney");
      rowData.add(trimNull(addMoney));
      rowData.add(trimNull(subMoney));
      beforeMoney = BigDecimalUtils.sub(BigDecimalUtils.add(beforeMoney, addMoney), subMoney);
      rowData.add(trimNull(beforeMoney));
      rowData.add(trimNull(model.get("remark")));
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
  
  public void printContrast()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "比较-会计科目");
    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("项目");
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
    
    List<String> rowData = new ArrayList();
    
    Integer accountId = getParaToInt("accountsId", Integer.valueOf(0));
    Record cost = AccountsCostRecords.dao.getContrast(configName, accountId);
    Model account = Accounts.dao.findById(configName, accountId);
    if ((account != null) && ("00046".equals(account.getStr("type"))))
    {
      Model get = Accounts.dao.getModelFirst(configName, "0008");
      Model pay = Accounts.dao.getModelFirst(configName, "00010");
      Record getCost = AccountsCostRecords.dao.getContrast(configName, get.getInt("id"));
      Record payCost = AccountsCostRecords.dao.getContrast(configName, pay.getInt("id"));
      cost.set("monthMoney1", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney1"), payCost.getBigDecimal("monthMoney1")));
      cost.set("monthMoney2", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney2"), payCost.getBigDecimal("monthMoney2")));
      cost.set("monthMoney3", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney3"), payCost.getBigDecimal("monthMoney3")));
      cost.set("monthMoney4", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney4"), payCost.getBigDecimal("monthMoney4")));
      cost.set("monthMoney5", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney5"), payCost.getBigDecimal("monthMoney5")));
      cost.set("monthMoney6", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney6"), payCost.getBigDecimal("monthMoney6")));
      cost.set("monthMoney7", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney7"), payCost.getBigDecimal("monthMoney7")));
      cost.set("monthMoney8", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney8"), payCost.getBigDecimal("monthMoney8")));
      cost.set("monthMoney9", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney9"), payCost.getBigDecimal("monthMoney9")));
      cost.set("monthMoney10", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney10"), payCost.getBigDecimal("monthMoney10")));
      cost.set("monthMoney11", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney11"), payCost.getBigDecimal("monthMoney11")));
      cost.set("monthMoney12", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney12"), payCost.getBigDecimal("monthMoney12")));
    }
    rowData.add("1");
    rowData.add("发生金额");
    if (cost != null)
    {
      rowData.add(trimNull(cost.get("monthMoney1")));
      rowData.add(trimNull(cost.get("monthMoney2")));
      rowData.add(trimNull(cost.get("monthMoney3")));
      rowData.add(trimNull(cost.get("monthMoney4")));
      rowData.add(trimNull(cost.get("monthMoney5")));
      rowData.add(trimNull(cost.get("monthMoney6")));
      rowData.add(trimNull(cost.get("monthMoney7")));
      rowData.add(trimNull(cost.get("monthMoney8")));
      rowData.add(trimNull(cost.get("monthMoney9")));
      rowData.add(trimNull(cost.get("monthMoney10")));
      rowData.add(trimNull(cost.get("monthMoney11")));
      rowData.add(trimNull(cost.get("monthMoney12")));
    }
    else
    {
      rowData.add("");
      rowData.add("");
      rowData.add("");
      rowData.add("");
      rowData.add("");
      rowData.add("");
      rowData.add("");
      rowData.add("");
      rowData.add("");
      rowData.add("");
      rowData.add("");
      rowData.add("");
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public void contrast()
    throws ParseException
  {
    String configName = loginConfigName();
    Integer accountId = getParaToInt(0, Integer.valueOf(0));
    Model account = Accounts.dao.findById(configName, accountId);
    Record cost = AccountsCostRecords.dao.getContrast(configName, accountId);
    if ((account != null) && ("00046".equals(account.getStr("type"))))
    {
      Model get = Accounts.dao.getModelFirst(configName, "0008");
      Model pay = Accounts.dao.getModelFirst(configName, "00010");
      Record getCost = AccountsCostRecords.dao.getContrast(configName, get.getInt("id"));
      Record payCost = AccountsCostRecords.dao.getContrast(configName, pay.getInt("id"));
      cost.set("monthMoney1", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney1"), payCost.getBigDecimal("monthMoney1")));
      cost.set("monthMoney2", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney2"), payCost.getBigDecimal("monthMoney2")));
      cost.set("monthMoney3", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney3"), payCost.getBigDecimal("monthMoney3")));
      cost.set("monthMoney4", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney4"), payCost.getBigDecimal("monthMoney4")));
      cost.set("monthMoney5", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney5"), payCost.getBigDecimal("monthMoney5")));
      cost.set("monthMoney6", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney6"), payCost.getBigDecimal("monthMoney6")));
      cost.set("monthMoney7", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney7"), payCost.getBigDecimal("monthMoney7")));
      cost.set("monthMoney8", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney8"), payCost.getBigDecimal("monthMoney8")));
      cost.set("monthMoney9", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney9"), payCost.getBigDecimal("monthMoney9")));
      cost.set("monthMoney10", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney10"), payCost.getBigDecimal("monthMoney10")));
      cost.set("monthMoney11", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney11"), payCost.getBigDecimal("monthMoney11")));
      cost.set("monthMoney12", BigDecimalUtils.sub(getCost.getBigDecimal("monthMoney12"), payCost.getBigDecimal("monthMoney12")));
    }
    setAttr("cost", cost);
    setAttr("account", account);
    render("contrast.html");
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
