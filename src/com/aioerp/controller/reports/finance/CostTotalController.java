package com.aioerp.controller.reports.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.reports.finance.CostTotalRecords;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostTotalController
  extends BaseController
{
  public void index()
    throws SQLException, Exception
  {
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String type = getPara(0, "first");
    String configName = loginConfigName();
    Map<String, Object> map = new HashMap();
    
    map.put("startDate", DateUtils.getFirstDateOfMonth());
    map.put("endDate", DateUtils.getFirstDateOfNextMonth());
    Map<String, Object> pageMap = CostTotalRecords.dao.getPage(configName, pageNum, numPerPage, "costTotalList", map);
    
    setAttr("pageMap", pageMap);
    if ("first".equals(type)) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void detail()
    throws SQLException, Exception
  {
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String configName = loginConfigName();
    String orderField = getPara("orderField", "");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String type = getPara(0, "first");
    Integer accountId = getParaToInt("accountId");
    Map<String, Object> map = new HashMap();
    map.put("startTime", getPara("startTime"));
    map.put("endTime", getPara("endTime"));
    map.put("accountId", accountId);
    map.put("isRcw", getParaToInt("isRcw"));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    Map<String, Object> pageMap = CostTotalRecords.dao.getDetailPage(configName, pageNum, numPerPage, "costTotalDetailList", map);
    BigDecimal beforeMoney = CostTotalRecords.dao.getBeforeMoney(configName, pageNum, numPerPage, map);
    List<Model> rList = (List)pageMap.get("pageList");
    for (Model model : rList)
    {
      BigDecimal addMoney = model.getBigDecimal("addMoney");
      BigDecimal subMoney = model.getBigDecimal("subMoney");
      beforeMoney = BigDecimalUtils.sub(BigDecimalUtils.add(beforeMoney, addMoney), subMoney);
      model.put("money", beforeMoney);
    }
    setAttr("params", map);
    setAttr("pageMap", pageMap);
    setAttr("account", Accounts.dao.findById(configName, accountId));
    if ("first".equals(type)) {
      render("detail.html");
    } else {
      render("detailList.html");
    }
  }
}
