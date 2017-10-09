package com.aioerp.common.route;

import com.aioerp.controller.sell.SellBarterBillController;
import com.aioerp.controller.sell.SellBarterDraftController;
import com.aioerp.controller.sell.sell.SellController;
import com.aioerp.controller.sell.sell.SellDetailController;
import com.aioerp.controller.sell.sell.SellDraftController;
import com.aioerp.controller.sell.sellbook.SellBookController;
import com.aioerp.controller.sell.sellbook.SellBookDetailController;
import com.aioerp.controller.sell.sellreturn.SellReturnController;
import com.aioerp.controller.sell.sellreturn.SellReturnDetailController;
import com.aioerp.controller.sell.sellreturn.SellReturnDraftController;
import com.jfinal.config.Routes;

public class SellRoute
  extends Routes
{
  public void config()
  {
    add("/sell/book", SellBookController.class);
    add("/sell/bookDetail", SellBookDetailController.class);
    
    add("/sell/sell", SellController.class);
    add("/sell/sell/draft", SellDraftController.class);
    add("/sell/sellDetail", SellDetailController.class);
    

    add("/sell/return", SellReturnController.class);
    add("/sell/return/draft", SellReturnDraftController.class);
    add("/sell/returnDetail", SellReturnDetailController.class);
    
    add("/sell/barter", SellBarterBillController.class);
    
    add("/sell/barter/draft", SellBarterDraftController.class);
  }
}
