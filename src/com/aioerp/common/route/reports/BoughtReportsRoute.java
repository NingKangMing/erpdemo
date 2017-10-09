package com.aioerp.common.route.reports;

import com.aioerp.controller.reports.bought.BoughtDetailReportsController;
import com.aioerp.controller.reports.bought.BoughtReportsController;
import com.aioerp.controller.reports.bought.PurchasePrivilegeReportsController;
import com.aioerp.controller.reports.bought.PurchaseReportsController;
import com.aioerp.controller.reports.bought.PurchaseReturnReportsController;
import com.aioerp.controller.reports.bought.PurchaseReturnUnitReportsController;
import com.aioerp.controller.reports.bought.PurchaseStorageReportsController;
import com.aioerp.controller.reports.bought.PurchaseUnitReportsController;
import com.jfinal.config.Routes;

public class BoughtReportsRoute
  extends Routes
{
  public void config()
  {
    add("/reports/bought", BoughtReportsController.class);
    add("/reports/bought/detail", BoughtDetailReportsController.class);
    add("/reports/bought/purchase", PurchaseReportsController.class);
    add("/reports/bought/purchase/unit", PurchaseUnitReportsController.class);
    add("/reports/bought/return", PurchaseReturnReportsController.class);
    add("/reports/bought/return/unit", PurchaseReturnUnitReportsController.class);
    add("/reports/bought/purchase/privilege", PurchasePrivilegeReportsController.class);
    add("/reports/bought/purchase/storage", PurchaseStorageReportsController.class);
  }
}
