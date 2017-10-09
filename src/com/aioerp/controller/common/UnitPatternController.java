package com.aioerp.controller.common;

import com.aioerp.controller.BaseController;

public class UnitPatternController
  extends BaseController
{
  public void index()
  {
    setAttr("unitPattern", getPara("unitPattern", "baseUnit"));
    render("page.html");
  }
}
