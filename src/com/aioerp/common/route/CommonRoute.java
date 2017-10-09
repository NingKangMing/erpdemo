package com.aioerp.common.route;

import com.aioerp.controller.common.BaseInfoController;
import com.aioerp.controller.common.BillController;
import com.aioerp.controller.common.BillSearchDialogController;
import com.aioerp.controller.common.DateDialogController;
import com.aioerp.controller.common.HelpAmountPatternController;
import com.aioerp.controller.common.ProStockSearchDialogController;
import com.aioerp.controller.common.SearchDialogController;
import com.aioerp.controller.common.UnitPatternController;
import com.aioerp.controller.common.VerifyController;
import com.aioerp.controller.common.priceWayController;
import com.jfinal.config.Routes;

public class CommonRoute
  extends Routes
{
  public void config()
  {
    add("/common/dateDialog", DateDialogController.class);
    add("/common/billSearchDialog", BillSearchDialogController.class);
    add("/common/helpAmountPattern", HelpAmountPatternController.class);
    add("/common/proStockSearchDialog", ProStockSearchDialogController.class);
    add("/common/searchDialog", SearchDialogController.class);
    add("/common/unitPattern", UnitPatternController.class);
    add("/common/priceWay", priceWayController.class);
    
    add("/common/bill", BillController.class);
    add("/common/baseInfo", BaseInfoController.class);
    add("/common/verify", VerifyController.class);
  }
}
