package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.BillType;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class BillRowController
  extends BaseController
{
  public void index()
  {
    String configName = loginConfigName();
    setAttr("typeList", BillType.dao.getList(configName, "node", 1));
    render("page.html");
  }
  
  public void search()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billId));
    setAttr("bageBreak", "billRowList");
    render("list.html");
  }
  
  public void dialog()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billId));
    setAttr("bageBreak", "billRowList");
    render("dialog.html");
  }
  
  @Before({Tx.class})
  public void editStatus()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    Model<BillRow> row = BillRow.dao.findById(configName, Integer.valueOf(id));
    if (row == null) {
      return;
    }
    if (row.getInt("status").intValue() != AioConstants.STATUS_ENABLE)
    {
      row.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
    }
    else
    {
      Integer isSys = row.getInt("isSys");
      if (isSys.intValue() == 0) {
        row.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
      }
    }
    row.update(configName);
    
    String linkage = row.getStr("linkage");
    List<Integer> linkageIds = new ArrayList();
    if (StringUtils.isNotBlank(linkage))
    {
      Integer billId = row.getInt("billId");
      if (billId == null) {
        billId = Integer.valueOf(0);
      }
      Integer status = row.getInt("status");
      List<Model> rowList = BillRow.dao.getListByBillLinkage(configName, billId.intValue(), linkage);
      for (Model model : rowList)
      {
        linkageIds.add(model.getInt("id"));
        model.set("status", status);
        model.update(configName);
      }
    }
    setAttr("status", row.getInt("status"));
    setAttr("linkageIds", linkageIds);
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
        flag = BillRow.dao.findById(configName, ids[i]).set("rank", orders[i]).update(configName);
      }
    }
    if (flag)
    {
      result.put("statusCode", AioConstants.HTTP_RETURN200);
      result.put("rel", "billRowList");
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
    String configName = loginConfigName();
    List<BillRow> list = ModelUtils.batchInjectSortObjModel(getRequest(), BillRow.class, "billRow");
    for (BillRow billRow : list) {
      if ((StringUtils.isNotBlank(billRow.getStr("showName"))) || (billRow.getInt("width") != null)) {
        billRow.update(configName);
      }
    }
    renderJson();
  }
}
