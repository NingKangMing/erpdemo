package com.aioerp.common.route.port;

import com.aioerp.port.controller.om.OmPortController;
import com.aioerp.port.controller.om.OmPortSetController;
import com.aioerp.port.controller.om.OmPortUserController;
import com.jfinal.config.Routes;

public class AioerpOmPortRoute
  extends Routes
{
  public void config()
  {
    add("/aioerpom", OmPortController.class);
    
    add("/aioerpom/user", OmPortUserController.class);
    
    add("/aioerpom/set", OmPortSetController.class);
  }
}
