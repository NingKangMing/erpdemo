package com.aioerp.common.route;

import com.aioerp.controller.UserController;
import com.aioerp.controller.sys.AboutUsController;
import com.aioerp.controller.sys.BackUpController;
import com.aioerp.controller.sys.BasePermissionController;
import com.aioerp.controller.sys.BillCodeConfigController;
import com.aioerp.controller.sys.BillRowController;
import com.aioerp.controller.sys.BillSortController;
import com.aioerp.controller.sys.BillTypeController;
import com.aioerp.controller.sys.DeliveryCompanyController;
import com.aioerp.controller.sys.PermissionController;
import com.aioerp.controller.sys.ReSystemController;
import com.aioerp.controller.sys.RegisterController;
import com.aioerp.controller.sys.ReloadCostPriceController;
import com.aioerp.controller.sys.SysConfigController;
import com.aioerp.controller.sys.end.MonthEndController;
import com.aioerp.controller.sys.end.YearEndController;
import com.jfinal.config.Routes;

public class SysRoute
  extends Routes
{
  public void config()
  {
    add("/sys/billSort", BillSortController.class);
    add("/sys/billType", BillTypeController.class);
    add("/sys/billRow", BillRowController.class);
    add("/sys/billCodeConfig", BillCodeConfigController.class);
    
    add("/sys/deliveryCompany", DeliveryCompanyController.class);
    add("/user", UserController.class);
    add("/sys/backup", BackUpController.class);
    add("/sys/reSystem", ReSystemController.class);
    add("/sys/about", AboutUsController.class);
    add("/sys/register", RegisterController.class);
    

    add("/sys/monthEnd", MonthEndController.class);
    add("/sys/yearEnd", YearEndController.class);
    
    add("/sys/permission", PermissionController.class);
    add("/sys/basePermission", BasePermissionController.class);
    
    add("/sys/sysConfig", SysConfigController.class);
    
    add("/sys/reloadCostPrice", ReloadCostPriceController.class);
  }
}
