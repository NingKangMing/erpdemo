package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.util.Dog;

public class AboutUsController
  extends BaseController
{
  public void index()
  {
    try
    {
      if ((Dog.reload()) && (Dog.getVersion() == AioConstants.VERSION))
      {
        setAttr("Company", Dog.getCompany());
        setAttr("Area", Dog.getArea());
        setAttr("CardNO", Dog.getCardNO());
        setAttr("UserCount", Integer.valueOf(Dog.getUserCount()));
        setAttr("DogNO", Dog.getID());
        setAttr("RDate", Dog.getRDate());
        setAttr("SDate", Dog.getSDate());
      }
    }
    catch (Error localError) {}
    render("page.html");
  }
}
