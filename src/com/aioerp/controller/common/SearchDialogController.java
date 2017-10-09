package com.aioerp.controller.common;

import com.aioerp.controller.BaseController;
import com.aioerp.util.DwzUtils;
import java.util.Map;

public class SearchDialogController
  extends BaseController
{
  public void go()
  {
    setAttr("statusCode", Integer.valueOf(200));
    
    setAttr("aimTabId", getPara("aimTabId", ""));
    setAttr("aimUrl", getPara("aimUrl", ""));
    setAttr("aimTitle", getPara("aimTitle", ""));
    setAttr("rel", getPara("aimDiv", ""));
    

    Map<String, Object> paraMap = DwzUtils.RequestConvertMap(getParaMap());
    paraMap.remove("aimTabId");
    paraMap.remove("aimUrl");
    paraMap.remove("aimTitle");
    paraMap.remove("aimDiv");
    if (paraMap.size() > 0) {
      setAttr("data", paraMap);
    }
    renderJson();
  }
}
