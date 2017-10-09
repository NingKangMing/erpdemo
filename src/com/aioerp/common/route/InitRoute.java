package com.aioerp.common.route;

import com.aioerp.controller.init.FinanceInitController;
import com.aioerp.controller.init.MakeInitContriller;
import com.aioerp.controller.init.ProductInitController;
import com.aioerp.controller.init.UnitInitController;
import com.jfinal.config.Routes;

public class InitRoute
  extends Routes
{
  public void config()
  {
    add("/init/productInit", ProductInitController.class);
    add("/init/unitInit", UnitInitController.class);
    add("/init/financeInit", FinanceInitController.class);
    add("/init/makeInit", MakeInitContriller.class);
  }
}
