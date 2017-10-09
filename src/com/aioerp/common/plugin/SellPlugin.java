package com.aioerp.common.plugin;

import com.aioerp.model.sell.barter.SellBarterBill;
import com.aioerp.model.sell.barter.SellBarterDetail;
import com.aioerp.model.sell.barter.SellBarterDraftBill;
import com.aioerp.model.sell.barter.SellBarterDraftDetail;
import com.aioerp.model.sell.sell.SellBill;
import com.aioerp.model.sell.sell.SellDetail;
import com.aioerp.model.sell.sell.SellDraftBill;
import com.aioerp.model.sell.sell.SellDraftDetail;
import com.aioerp.model.sell.sellbook.SellbookBill;
import com.aioerp.model.sell.sellbook.SellbookDetail;
import com.aioerp.model.sell.sellreturn.SellReturnBill;
import com.aioerp.model.sell.sellreturn.SellReturnDetail;
import com.aioerp.model.sell.sellreturn.SellReturnDraftBill;
import com.aioerp.model.sell.sellreturn.SellReturnDraftDetail;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class SellPlugin
{
  public SellPlugin(ActiveRecordPlugin arp)
  {
    arp.addMapping("xs_sellbook_bill", SellbookBill.class);
    arp.addMapping("xs_sellbook_detail", SellbookDetail.class);
    
    arp.addMapping("xs_sell_bill", SellBill.class);
    arp.addMapping("xs_sell_detail", SellDetail.class);
    arp.addMapping("xs_sell_draft_bill", SellDraftBill.class);
    arp.addMapping("xs_sell_draft_detail", SellDraftDetail.class);
    
    arp.addMapping("xs_return_bill", SellReturnBill.class);
    arp.addMapping("xs_return_detail", SellReturnDetail.class);
    arp.addMapping("xs_return_draft_bill", SellReturnDraftBill.class);
    arp.addMapping("xs_return_draft_detail", SellReturnDraftDetail.class);
    
    arp.addMapping("xs_barter_bill", SellBarterBill.class);
    arp.addMapping("xs_barter_detail", SellBarterDetail.class);
    arp.addMapping("xs_barter_draft_bill", SellBarterDraftBill.class);
    arp.addMapping("xs_barter_draft_detail", SellBarterDraftDetail.class);
  }
}
