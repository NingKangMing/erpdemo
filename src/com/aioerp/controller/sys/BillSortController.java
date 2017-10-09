package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.sys.BillSort;
import com.aioerp.model.sys.BillType;
import com.jfinal.plugin.activerecord.Model;

public class BillSortController
  extends BaseController
{
  String listID = "sys_setBillCode";
  
  public void setBillCodeView()
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    int sortId = getParaToInt(0, Integer.valueOf(1)).intValue();
    Model obj = BillSort.dao.findById(configName, Integer.valueOf(sortId));
    setAttr("sortId", Integer.valueOf(sortId));
    setAttr("sortName", obj.get("name"));
    setAttr("sorts", BillSort.dao.getList(configName));
    setAttr("pageMap", BillType.dao.getPage(configName, this.listID, pageNum, numPerPage, sortId));
    render("setBillCode.html");
  }
}
