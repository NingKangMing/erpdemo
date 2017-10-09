package com.aioerp.controller.fz;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.fz.ReportRow;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class ReportRowController
  extends BaseController
{
  public void search()
  {
    String reportType = getPara(0, "");
    setAttr("rowList", ReportRow.dao.getListByType(loginConfigName(), reportType));
    setAttr("bageBreak", "reportRowList");
    render("page.html");
  }
  
  public void editStatus()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    Model<ReportRow> row = ReportRow.dao.findById(configName, Integer.valueOf(id));
    if (row == null) {
      return;
    }
    if (row.getInt("status").intValue() != AioConstants.STATUS_ENABLE) {
      row.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
    } else {
      row.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
    }
    row.update(configName);
    setAttr("status", row.getInt("status"));
    renderJson();
  }
  
  @Before({Tx.class})
  public void saveRank()
  {
    String configName = loginConfigName();
    HashMap<String, Object> result = new HashMap();
    String[] ids = getPara("ids").split(",");
    String[] orders = getPara("orders").split(",");
    if (ids.length <= 0)
    {
      result.put("statusCode", AioConstants.HTTP_RETURN300);
      result.put("message", "没有排序变更!");
      renderJson(result);
      return;
    }
    boolean flag = true;
    for (int i = 0; i < ids.length; i++) {
      if (StringUtils.isNotBlank(ids[i])) {
        flag = ReportRow.dao.findById(configName, ids[i]).set("rank", orders[i]).update(configName);
      }
    }
    if (flag)
    {
      result.put("statusCode", AioConstants.HTTP_RETURN200);
      result.put("rel", "reportRowList");
    }
    else
    {
      result.put("statusCode", AioConstants.HTTP_RETURN300);
      result.put("message", "操作失败!");
    }
    renderJson(result);
  }
  
  public void edit()
  {
    List<ReportRow> list = ModelUtils.batchInjectSortObjModel(getRequest(), ReportRow.class, "reportRow");
    for (ReportRow reportRow : list) {
      if ((StringUtils.isNotBlank(reportRow.getStr("showName"))) || (reportRow.getInt("width") != null)) {
        reportRow.update(loginConfigName());
      }
    }
    renderJson();
  }
}
