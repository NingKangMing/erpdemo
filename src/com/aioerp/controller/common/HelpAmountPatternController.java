package com.aioerp.controller.common;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import java.util.HashMap;
import java.util.Map;

public class HelpAmountPatternController
  extends BaseController
{
  public void index()
  {
    setAttr("helpAmunitPattern", getPara("helpAmunitPattern", "blendUnit"));
    render("page.html");
  }
  
  public void go()
  {
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("rel", getPara("aimDiv", ""));
    
    Map<String, Object> map = new HashMap();
    map.put("helpAmunitPattern", getPara("helpAmunitPattern", "blendUnit"));
    setAttr("data", map);
    
    renderJson();
  }
}
