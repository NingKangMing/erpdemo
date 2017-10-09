package com.aioerp.common.route;

import com.aioerp.controller.stock.BreakageController;
import com.aioerp.controller.stock.DifftAllotController;
import com.aioerp.controller.stock.DismountBillController;
import com.aioerp.controller.stock.DismountDraftBillController;
import com.aioerp.controller.stock.OverflowController;
import com.aioerp.controller.stock.ParityAllotController;
import com.aioerp.controller.stock.ParityAllotDraftController;
import com.aioerp.controller.stock.StockController;
import com.aioerp.controller.stock.StockRecordsController;
import com.aioerp.controller.stock.StockStatusController;
import com.aioerp.controller.stock.TakeStockController;
import com.aioerp.controller.stock.other.OtherInController;
import com.aioerp.controller.stock.other.OtherInDraftController;
import com.aioerp.controller.stock.other.OtherOutController;
import com.aioerp.controller.stock.other.OtherOutDraftController;
import com.jfinal.config.Routes;

public class StockRoute
  extends Routes
{
  public void config()
  {
    add("/stock/stock", StockController.class);
    add("/stock/stockRecords", StockRecordsController.class);
    add("/stock/stockStatus", StockStatusController.class);
    add("/stock/takeStock", TakeStockController.class);
    
    add("/stock/breakage", BreakageController.class);
    add("/stock/overflow", OverflowController.class);
    

    add("/stock/otherin", OtherInController.class);
    add("/stock/otherin/draft", OtherInDraftController.class);
    add("/stock/otherout", OtherOutController.class);
    add("/stock/otherout/draft", OtherOutDraftController.class);
    
    add("/stock/parityAllot", ParityAllotController.class);
    
    add("/stock/difftAllot", DifftAllotController.class);
    
    add("/stock/dismount", DismountBillController.class);
    
    add("/stock/parityAllot/draft", ParityAllotDraftController.class);
    
    add("/stock/dismount/draft", DismountDraftBillController.class);
  }
}
