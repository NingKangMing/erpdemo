package com.aioerp.common.route;

import com.aioerp.controller.bought.BoughtBillController;
import com.aioerp.controller.bought.BoughtDetailController;
import com.aioerp.controller.bought.PurchaseBarterBillController;
import com.aioerp.controller.bought.PurchaseBillController;
import com.aioerp.controller.bought.PurchaseDetailController;
import com.aioerp.controller.bought.PurchaseDraftBillController;
import com.aioerp.controller.bought.PurchaseReturnBillController;
import com.aioerp.controller.bought.PurchaseReturnDraftBillController;
import com.jfinal.config.Routes;

public class BoughtRoute
  extends Routes
{
  public void config()
  {
    add("/bought/bought", BoughtBillController.class);
    add("/bought/boughtDetail", BoughtDetailController.class);
    add("/bought/purchase", PurchaseBillController.class);
    add("/bought/purchaseDetail", PurchaseDetailController.class);
    add("/bought/return", PurchaseReturnBillController.class);
    add("/bought/barter", PurchaseBarterBillController.class);
    

    add("/bought/purchase/draft", PurchaseDraftBillController.class);
    add("/bought/return/draft", PurchaseReturnDraftBillController.class);
  }
}
