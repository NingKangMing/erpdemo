package com.aioerp.controller.sys.end;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.db.reports.BillHistory;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.end.MonthEnd;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthEndController
  extends BaseController
{
  public void vaildateHasOpenAccount()
  {
    String configName = loginConfigName();
    
    Record aioerpSys = AioerpSys.dao.getObj(configName, "hasOpenAccount");
    if ((aioerpSys == null) || (aioerpSys.getStr("value1").equals(AioConstants.HAS_OPEN_ACCOUNT0)))
    {
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      this.result.put("message", "请先开账");
      renderJson(this.result);
      return;
    }
    if ((aioerpSys != null) && (aioerpSys.getStr("value1").equals(AioConstants.HAS_OPEN_ACCOUNT1)))
    {
      java.util.Date date = BillHistory.dao.getHistoryFristBillRecodeDate(configName);
      if (date == null)
      {
        this.result.put("statusCode", AioConstants.HTTP_RETURN300);
        this.result.put("message", "没有过账单据");
        renderJson(this.result);
      }
      else
      {
        this.result.put("statusCode", AioConstants.HTTP_RETURN200);
        renderJson(this.result);
      }
      return;
    }
  }
  
  public void index()
  {
    String configName = loginConfigName();
    
    Model monthModel = MonthEnd.dao.lastModel(configName);
    if (monthModel == null)
    {
      Model yearModel = YearEnd.dao.lastModel(configName);
      if (yearModel == null)
      {
        java.util.Date date = BillHistory.dao.getHistoryFristBillRecodeDate(configName);
        
        setAttr("startDate", date);
      }
      else
      {
        setAttr("startDate", DateUtils.addDays(yearModel.getDate("endDate"), 1));
      }
    }
    else
    {
      setAttr("preEndDate", monthModel.getDate("endDate"));
      setAttr("startDate", DateUtils.addDays(monthModel.getDate("endDate"), 1));
      setAttr("monthCount", Integer.valueOf(monthModel.getInt("monthCount").intValue() + 1));
    }
    setAttr("endDate", new java.util.Date());
    setAttr("currentMonthName", DateUtils.getMonth(new java.util.Date()) + "月");
    

    long monthEndCount = MonthEnd.dao.monthEndCount(configName);
    setAttr("monthEndCount", Long.valueOf(monthEndCount));
    render("/WEB-INF/template/sys/end/monthEnd/monthDialog.html");
  }
  
  @Before({Tx.class})
  public void monthEnd()
  {
    String configName = loginConfigName();
    try
    {
      Model model = (Model)getModel(MonthEnd.class);
      
      int billCount = BillHistory.dao.getStartToEndDateHistoryBillCount(configName, model.getDate("startDate"), DateUtils.addDays(model.getDate("endDate"), 1));
      if (billCount == 0)
      {
        this.result.put("statusCode", AioConstants.HTTP_RETURN300);
        this.result.put("message", "本月没有发生过账单据，操作失败");
        renderJson(this.result);
        return;
      }
      model.set("billCount", Integer.valueOf(billCount));
      model.set("userId", Integer.valueOf(loginUserId()));
      model.save(configName);
      this.result.put("statusCode", AioConstants.HTTP_RETURN200);
      this.result.put("message", "操作成功");
      this.result.put("callbackType", "closeCurrent");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
    }
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void unMonthEnd()
  {
    String configName = loginConfigName();
    try
    {
      Model model = MonthEnd.dao.lastModel(configName);
      if (model == null)
      {
        this.result.put("message", "尚未开账或没有月结存过");
        this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      }
      else
      {
        model.delete(configName);
        this.result.put("message", "操作成功");
        this.result.put("callbackType", "closeCurrent");
        this.result.put("statusCode", AioConstants.HTTP_RETURN200);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.result.put("message", "系统异常");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
    }
    renderJson(this.result);
  }
  
  public void monthEndCount()
    throws ParseException
  {
    String configName = loginConfigName();
    List<Model> list = MonthEnd.dao.getList(configName);
    MonthEnd model = new MonthEnd();
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
      
      model.set("startDate", date);
    }
    java.sql.Date endDate = new java.sql.Date(new java.util.Date().getTime());
    if (model.getDate("startDate").before(endDate)) {
      model.set("endDate", endDate);
    } else {
      model.set("endDate", model.getDate("startDate"));
    }
    int billCount = BillHistory.dao.getStartToEndDateHistoryBillCount(configName, model.getDate("startDate"), model.getDate("endDate"));
    model.set("billCount", Integer.valueOf(billCount));
    model.set("monthsName", "本月");
    list.add(model);
    setAttr("list", list);
    

    columnConfig("sys500");
    render("/WEB-INF/template/reports/sys/end/monthEnd/page.html");
  }
  
  public void print()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "月结存信息表");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    

    List<Model> list = MonthEnd.dao.getList(configName);
    MonthEnd model = new MonthEnd();
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
      
      model.set("startDate", date);
    }
    java.sql.Date endDate = new java.sql.Date(new java.util.Date().getTime());
    if (model.getDate("startDate").before(endDate)) {
      model.set("endDate", endDate);
    } else {
      model.set("endDate", model.getDate("startDate"));
    }
    int billCount = BillHistory.dao.getStartToEndDateHistoryBillCount(configName, model.getDate("startDate"), model.getDate("endDate"));
    model.set("billCount", Integer.valueOf(billCount));
    model.set("monthsName", "本月");
    list.add(model);
    



    printCommonData(headData);
    

    colTitle.add("行号");
    colTitle.add("账龄");
    colTitle.add("起始日期");
    colTitle.add("结束日期");
    colTitle.add("本月单据数");
    colTitle.add("月结期间");
    

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
      rowData.add(trimNull(detail.getStr("monthsName")));
      rowData.add(trimNull(DateUtils.format(detail.getDate("startDate"), "yyyy-MM-dd")));
      rowData.add(trimNull(DateUtils.format(detail.getDate("endDate"), "yyyy-MM-dd")));
      rowData.add(trimNull(detail.getInt("billCount")));
      rowData.add(trimNull(detail.getInt("monthCount")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
