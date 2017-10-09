package com.aioerp.common.route.reports;

import com.aioerp.controller.reports.stock.BreakOrOverflowCountController;
import com.aioerp.controller.reports.stock.DismountCountController;
import com.aioerp.controller.reports.stock.JXCChangeCountController;
import com.aioerp.controller.reports.stock.OtherInOrOutCountController;
import com.aioerp.controller.reports.stock.SameOrDiffPriceCountController;
import com.aioerp.controller.reports.stock.StockBatchTailController;
import com.aioerp.controller.reports.stock.StockBoundController;
import com.aioerp.controller.reports.stock.StockDistributedController;
import com.aioerp.controller.reports.stock.StockLifeController;
import com.aioerp.controller.reports.stock.VirtualStockController;
import com.jfinal.config.Routes;

public class StockReportsRoute
  extends Routes
{
  public void config()
  {
    add("/reports/stock/virtualStock", VirtualStockController.class);
    add("/reports/stock/stockDistributed", StockDistributedController.class);
    add("/reports/stock/stockBatchTail", StockBatchTailController.class);
    add("/reports/stock/stockLife", StockLifeController.class);
    add("/reports/stock/other", OtherInOrOutCountController.class);
    add("/reports/stock/breakOrOverflow", BreakOrOverflowCountController.class);
    add("/reports/stock/sameOrDiffPrice", SameOrDiffPriceCountController.class);
    add("/reports/stock/dismonunt", DismountCountController.class);
    add("/reports/stock/jxcChange", JXCChangeCountController.class);
    
    add("/reports/stock/stockBound", StockBoundController.class);
  }
}
