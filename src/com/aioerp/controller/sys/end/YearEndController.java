package com.aioerp.controller.sys.end;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.sys.BackUpController;
import com.aioerp.db.reports.BillHistory;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YearEndController
  extends BaseController
{
  public void index()
  {
    setAttr("currentYearName", DateUtils.getYear(new java.util.Date()) + "年");
    render("/WEB-INF/template/sys/end/yearEnd/yearDialog.html");
  }
  
  @Before({Tx.class})
  public void yearEnd()
    throws Exception
  {
    String configName = loginConfigName();
    boolean flag = true;
    java.util.Date time = new java.util.Date();
    String yearEndType = getPara("yearEndType");
    String profitsInto = null;
    

    BigDecimal lastYearProfitMoney = PayTypeController.getLastYearProfitMoney(configName);
    if (yearEndType.equals("one"))
    {
      profitsInto = getPara("profitsIntoOne");
      
      this.result = BackUpController.commonBackUp(configName, Integer.valueOf(loginUserId()), loginAccountName(), "年结存" + loginAccountName() + DateUtils.getYear(new java.util.Date()) + "年");
      if (Integer.valueOf(this.result.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(this.result);
        return;
      }
      PayTypeController.delCurrentGetOrPay(configName);
      

      PayTypeController.yearEndInitData(configName);
      if (profitsInto.equals("realAssets"))
      {
        Accounts ac00045 = (Accounts)Accounts.dao.getModelFirst(configName, "00045");
        ac00045.set("money", BigDecimalUtils.add(ac00045.getBigDecimal("money"), lastYearProfitMoney)).update(configName);
      }
      else if (profitsInto.equals("unPayProfits"))
      {
        Accounts ac000420 = (Accounts)Accounts.dao.getModelFirst(configName, "000420");
        ac000420.set("money", BigDecimalUtils.add(ac000420.getBigDecimal("money"), lastYearProfitMoney)).update(configName);
      }
      String removeDraftAndBook = getPara("removeDraftAndBook", "no");
      if (removeDraftAndBook.equals("yes")) {
        flag = YearEnd.dao.removeDraftAndBook(configName);
      }
      if (!flag) {
        throw new Exception();
      }
      flag = YearEnd.dao.removeOneYearEnd(configName);
      if (!flag) {
        throw new Exception();
      }
      flag = YearEnd.dao.yearEndInitData(configName);
      if (!flag) {
        throw new Exception();
      }
    }
    else if (yearEndType.equals("many"))
    {
      profitsInto = getPara("profitsIntoMany", "realAssets");
      
      Record yearRecord = new Record();
      yearRecord.set("yearsName", getPara("currentYearName"));
      
      Model lastYearEndModel = YearEnd.dao.lastModel(configName);
      int billCount = 0;
      if (lastYearEndModel == null)
      {
        java.util.Date date = BillHistory.dao.getHistoryFristBillRecodeDate(configName);
        
        yearRecord.set("startDate", date);
        billCount = BillHistory.dao.getStartToEndDateHistoryBillCount(configName, date, DateUtils.addDays(time, 1));
        yearRecord.set("billCount", Integer.valueOf(billCount));
      }
      else
      {
        yearRecord.set("startDate", DateUtils.addDays(lastYearEndModel.getDate("endDate"), 1));
        billCount = BillHistory.dao.getStartToEndDateHistoryBillCount(configName, lastYearEndModel.getDate("startDate"), lastYearEndModel.getDate("endDate"));
        yearRecord.set("billCount", Integer.valueOf(billCount));
      }
      if (billCount == 0)
      {
        this.result.put("statusCode", AioConstants.HTTP_RETURN300);
        this.result.put("message", "本年没有发生过账单据，操作失败");
        renderJson(this.result);
        return;
      }
      yearRecord.set("endDate", time);
      
      String currentYearName = getPara("currentYearName");
      Record r = (Record)getSessionAttr("user");
      this.result = BackUpController.commonBackUp(configName, Integer.valueOf(loginUserId()), r.getStr("loginConfigName"), "多年结存" + currentYearName);
      if (Integer.valueOf(this.result.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(this.result);
        return;
      }
      Db.use(configName).save("sys_yearend", yearRecord);
      if (profitsInto.equals("realAssets"))
      {
        Accounts ac00045 = (Accounts)Accounts.dao.getModelFirst(configName, "00045");
        PayTypeController.addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE4), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), null, null, null, null, null, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00045.getInt("id"), lastYearProfitMoney, DateUtils.addDays(time, 1), loginUserId());
      }
      else if (profitsInto.equals("unPayProfits"))
      {
        Accounts ac000420 = (Accounts)Accounts.dao.getModelFirst(configName, "000420");
        PayTypeController.addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE4), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), null, null, null, null, null, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000420.getInt("id"), lastYearProfitMoney, DateUtils.addDays(time, 1), loginUserId());
      }
      Db.use(configName).update("delete from sys_monthend");
    }
    this.result.put("message", "操作成功");
    this.result.put("callbackType", "closeCurrent");
    this.result.put("statusCode", AioConstants.HTTP_RETURN200);
    renderJson(this.result);
  }
  
  public void yearEndCount()
    throws ParseException
  {
    String configName = loginConfigName();
    List<Model> list = YearEnd.dao.getList(configName);
    YearEnd model = new YearEnd();
    if ((list != null) && (list.size() > 0))
    {
      model.set("startDate", DateUtils.addDays(((Model)list.get(list.size() - 1)).getDate("endDate"), 1));
    }
    else
    {
      list = new ArrayList();
      Record sys = AioerpSys.dao.getObj(configName, "hasOpenAccountTime");
      String dateStr = null;
      java.util.Date date = null;
      if (sys == null)
      {
        this.result.put("message", "还未开账");
        this.result.put("statusCode", AioConstants.HTTP_RETURN300);
        renderJson(this.result);
        return;
      }
      dateStr = sys.getStr("value1");
      date = DateUtils.parseDate(dateStr);
      
      model.set("startDate", new java.sql.Date(DateUtils.parseDate(DateUtils.format(date)).getTime()));
    }
    java.sql.Date endDate = new java.sql.Date(new java.util.Date().getTime());
    if (model.getDate("startDate").before(endDate)) {
      model.set("endDate", endDate);
    } else {
      model.set("endDate", model.getDate("startDate"));
    }
    int billCount = BillHistory.dao.getStartToEndDateHistoryBillCount(configName, model.getDate("startDate"), DateUtils.addDays(model.getDate("endDate"), 1));
    model.set("billCount", Integer.valueOf(billCount));
    model.set("yearsName", "本年");
    list.add(model);
    setAttr("list", list);
    
    columnConfig("sys501");
    render("/WEB-INF/template/reports/sys/end/yearEnd/page.html");
  }
  
  public void print()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "年结存信息表");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    

    List<Model> list = YearEnd.dao.getList(configName);
    YearEnd model = new YearEnd();
    if ((list != null) && (list.size() > 0))
    {
      model.set("startDate", DateUtils.addDays(((Model)list.get(list.size() - 1)).getDate("endDate"), 1));
    }
    else
    {
      list = new ArrayList();
      Record sys = AioerpSys.dao.getObj(configName, "hasOpenAccountTime");
      String dateStr = null;
      java.util.Date date = null;
      if (sys == null)
      {
        this.result.put("message", "还未开账");
        this.result.put("statusCode", AioConstants.HTTP_RETURN300);
        renderJson(this.result);
        return;
      }
      dateStr = sys.getStr("value1");
      date = DateUtils.parseDate(dateStr);
      
      model.set("startDate", new java.sql.Date(DateUtils.parseDate(DateUtils.format(date)).getTime()));
    }
    java.sql.Date endDate = new java.sql.Date(new java.util.Date().getTime());
    if (model.getDate("startDate").before(endDate)) {
      model.set("endDate", endDate);
    } else {
      model.set("endDate", model.getDate("startDate"));
    }
    int billCount = BillHistory.dao.getStartToEndDateHistoryBillCount(configName, model.getDate("startDate"), DateUtils.addDays(model.getDate("endDate"), 1));
    model.set("billCount", Integer.valueOf(billCount));
    model.set("yearsName", "本年");
    list.add(model);
    



    printCommonData(headData);
    

    colTitle.add("行号");
    colTitle.add("年份");
    colTitle.add("起始日期");
    colTitle.add("结束日期");
    colTitle.add("本年单据数");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < list.size(); i++)
    {
      Model detail = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.getStr("yearsName")));
      rowData.add(trimNull(DateUtils.format(detail.getDate("startDate"), "yyyy-MM-dd")));
      rowData.add(trimNull(DateUtils.format(detail.getDate("endDate"), "yyyy-MM-dd")));
      rowData.add(trimNull(detail.getInt("billCount")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
