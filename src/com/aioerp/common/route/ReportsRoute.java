package com.aioerp.common.route;

import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.jfinal.config.Routes;

public class ReportsRoute
  extends Routes
{
  public void config()
  {
    add("/reports/billHistory", BillHistoryController.class);
    add("/reports/businessDraft", BusinessDraftController.class);
  }
}
