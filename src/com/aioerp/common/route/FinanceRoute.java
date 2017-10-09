package com.aioerp.common.route;

import com.aioerp.controller.finance.AccountVoucherController;
import com.aioerp.controller.finance.AccountVoucherDraftController;
import com.aioerp.controller.finance.AddAssetsController;
import com.aioerp.controller.finance.AdjustCostController;
import com.aioerp.controller.finance.AdjustCostDraftController;
import com.aioerp.controller.finance.DeprAssetsController;
import com.aioerp.controller.finance.OtherEarnController;
import com.aioerp.controller.finance.OtherEarnDraftController;
import com.aioerp.controller.finance.PayMoneyController;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.finance.SubAssetsController;
import com.aioerp.controller.finance.TransferController;
import com.aioerp.controller.finance.changegetorpay.ChangePayOrGetController;
import com.aioerp.controller.finance.changegetorpay.ChangePayOrGetDraftController;
import com.aioerp.controller.finance.feebill.FeeBillController;
import com.aioerp.controller.finance.feebill.FeeBillDraftController;
import com.aioerp.controller.finance.getmoney.GetMoneyController;
import com.aioerp.controller.finance.getmoney.GetMoneyDraftController;
import com.jfinal.config.Routes;

public class FinanceRoute
  extends Routes
{
  public void config()
  {
    add("/payType/payType", PayTypeController.class);
    
    add("/finance/getMoney", GetMoneyController.class);
    add("/finance/getMoney/draft", GetMoneyDraftController.class);
    

    add("/finance/fee", FeeBillController.class);
    add("/finance/fee/draft", FeeBillDraftController.class);
    

    add("/finance/otherEarn", OtherEarnController.class);
    add("/finance/otherEarn/draft", OtherEarnDraftController.class);
    

    add("/finance/payMoney", PayMoneyController.class);
    

    add("/finance/adjustCost", AdjustCostController.class);
    add("/finance/adjustCost/draft", AdjustCostDraftController.class);
    

    add("/finance/transfer", TransferController.class);
    

    add("/finance/addAssets", AddAssetsController.class);
    add("/finance/deprAssets", DeprAssetsController.class);
    add("/finance/subAssets", SubAssetsController.class);
    

    add("/finance/changeUnit", ChangePayOrGetController.class);
    add("/finance/changeUnit/draft", ChangePayOrGetDraftController.class);
    

    add("/finance/accountVoucher", AccountVoucherController.class);
    add("/finance/accountVoucher/draft", AccountVoucherDraftController.class);
  }
}
