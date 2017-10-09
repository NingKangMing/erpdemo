package com.aioerp.controller.common;

import com.aioerp.controller.BaseController;

public class priceWayController
  extends BaseController
{
  public void index()
  {
    setAttr("priceWay", getPara("priceWay", "stockPrice"));
    render("page.html");
  }
}
