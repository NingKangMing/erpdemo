package com.aioerp.common.route;

import com.aioerp.controller.fz.ImportBaseController;
import com.aioerp.controller.fz.PriceDiscountTrackController;
import com.aioerp.controller.fz.PricesManageController;
import com.aioerp.controller.fz.ProduceTemplateController;
import com.aioerp.controller.fz.ReportRowController;
import com.aioerp.controller.fz.WarningCentreController;
import com.aioerp.controller.fz.move.DepmMoveController;
import com.aioerp.controller.fz.move.ProMoveController;
import com.aioerp.controller.fz.move.StaffMoveController;
import com.aioerp.controller.fz.move.StorageMoveController;
import com.aioerp.controller.fz.move.UnitMoveController;
import com.jfinal.config.Routes;

public class FZRoute
  extends Routes
{
  public void config()
  {
    add("/fz/produceTemplate", ProduceTemplateController.class);
    

    add("/fz/priceDiscountTrack", PriceDiscountTrackController.class);
    
    add("/fz/pricesManage", PricesManageController.class);
    
    add("/fz/importBase", ImportBaseController.class);
    

    add("/fz/baseMove/product", ProMoveController.class);
    add("/fz/baseMove/unit", UnitMoveController.class);
    add("/fz/baseMove/depm", DepmMoveController.class);
    add("/fz/baseMove/staff", StaffMoveController.class);
    add("/fz/baseMove/storage", StorageMoveController.class);
    
    add("/fz/warningCentre", WarningCentreController.class);
    add("/fz/reportRow", ReportRowController.class);
  }
}
