package com.aioerp.common.route;

import com.aioerp.controller.supadmin.SupAdminController;
import com.aioerp.controller.supadmin.WhichDbController;
import com.jfinal.config.Routes;

public class SupAdminRoute
  extends Routes
{
  public void config()
  {
    add("/supAdmin/user", SupAdminController.class);
    
    add("/supAdmin/whichDb", WhichDbController.class);
  }
}
