package com.aioerp.controller.common;

import com.aioerp.controller.BaseController;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.util.DwzUtils;
import java.text.ParseException;
import java.util.Map;

public class DateDialogController
  extends BaseController
{
  public void index()
    throws ParseException
  {
    Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
    if (paraMap.size() > 0) {
      setAttr("paraMap", paraMap);
    }
    setStartDateAndEndDate();
    render("page.html");
  }
  
  public void go()
  {
    setAttr("statusCode", Integer.valueOf(200));
    
    setAttr("aimTabId", getPara("aimTabId", ""));
    setAttr("aimUrl", getPara("aimUrl", ""));
    setAttr("aimTitle", getPara("aimTitle", ""));
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(loginConfigName(), loginUserId(), getPara("startTime"), getPara("endTime"));
    }
    Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
    paraMap.remove("aimTabId");
    paraMap.remove("aimUrl");
    paraMap.remove("aimTitle");
    if (paraMap.size() > 0) {
      setAttr("data", paraMap);
    }
    renderJson();
  }
}
