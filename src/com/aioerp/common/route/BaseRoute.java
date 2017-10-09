package com.aioerp.common.route;

import com.aioerp.controller.base.AccountsController;
import com.aioerp.controller.base.AreaController;
import com.aioerp.controller.base.DepartmentController;
import com.aioerp.controller.base.ProductController;
import com.aioerp.controller.base.StaffController;
import com.aioerp.controller.base.StorageController;
import com.aioerp.controller.base.UnitController;
import com.jfinal.config.Routes;

public class BaseRoute
  extends Routes
{
  public void config()
  {
    add("/base/unit", UnitController.class);
    add("/base/department", DepartmentController.class);
    add("/base/product", ProductController.class);
    add("/base/area", AreaController.class);
    add("/base/staff", StaffController.class);
    add("/base/storage", StorageController.class);
    add("/base/accounts", AccountsController.class);
  }
}
