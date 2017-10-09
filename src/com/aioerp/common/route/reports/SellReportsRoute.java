package com.aioerp.common.route.reports;

import com.aioerp.controller.reports.sell.PrdSellCostCountController;
import com.aioerp.controller.reports.sell.PrdSellDetailCountController;
import com.aioerp.controller.reports.sell.SellBookCountController;
import com.aioerp.controller.reports.sell.SellCountController;
import com.aioerp.controller.reports.sell.SellLayoutCountController;
import com.aioerp.controller.reports.sell.SellPrivilegeCountController;
import com.aioerp.controller.reports.sell.SellRankCountController;
import com.aioerp.controller.reports.sell.SellReturnCountController;
import com.jfinal.config.Routes;

public class SellReportsRoute
  extends Routes
{
  public void config()
  {
    add("/reports/book", SellBookCountController.class);
    
    add("/reports/prdSellCount", SellCountController.class);
    
    add("/reports/sellReturnCount", SellReturnCountController.class);
    
    add("/reports/sellPrivilegeCount", SellPrivilegeCountController.class);
    
    add("/reports/sellRankCount", SellRankCountController.class);
    
    add("/reports/prdSellCostCount", PrdSellCostCountController.class);
    
    add("/reports/layout", SellLayoutCountController.class);
    
    add("/reports/prdSellDetailCount", PrdSellDetailCountController.class);
  }
}
