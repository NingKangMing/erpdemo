package com.aioerp.common.plugin;

import com.aioerp.db.reports.stock.StockBound;
import com.aioerp.model.stock.BreakageBill;
import com.aioerp.model.stock.BreakageDetail;
import com.aioerp.model.stock.BreakageDraftBill;
import com.aioerp.model.stock.BreakageDraftDetail;
import com.aioerp.model.stock.DifftAllotBill;
import com.aioerp.model.stock.DifftAllotDetail;
import com.aioerp.model.stock.DifftAllotDraftBill;
import com.aioerp.model.stock.DifftAllotDraftDetail;
import com.aioerp.model.stock.DismountBill;
import com.aioerp.model.stock.DismountDetail;
import com.aioerp.model.stock.DismountDraftBill;
import com.aioerp.model.stock.DismountDraftDetail;
import com.aioerp.model.stock.OverflowBill;
import com.aioerp.model.stock.OverflowDetail;
import com.aioerp.model.stock.OverflowDraftBill;
import com.aioerp.model.stock.OverflowDraftDetail;
import com.aioerp.model.stock.ParityAllotBill;
import com.aioerp.model.stock.ParityAllotDetail;
import com.aioerp.model.stock.ParityAllotDraftBill;
import com.aioerp.model.stock.ParityAllotDraftDetail;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.stock.StockDraftRecords;
import com.aioerp.model.stock.StockInit;
import com.aioerp.model.stock.StockRecords;
import com.aioerp.model.stock.TakeStockBill;
import com.aioerp.model.stock.TakeStockDetail;
import com.aioerp.model.stock.other.OtherInBill;
import com.aioerp.model.stock.other.OtherInDetail;
import com.aioerp.model.stock.other.OtherInDraftBill;
import com.aioerp.model.stock.other.OtherInDraftDetail;
import com.aioerp.model.stock.other.OtherOutBill;
import com.aioerp.model.stock.other.OtherOutDetail;
import com.aioerp.model.stock.other.OtherOutDraftBill;
import com.aioerp.model.stock.other.OtherOutDraftDetail;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class StockPlugin
{
  public StockPlugin(ActiveRecordPlugin arp)
  {
    arp.addMapping("cc_stock", Stock.class);
    arp.addMapping("cc_stock_init", StockInit.class);
    arp.addMapping("cc_stock_records", StockRecords.class);
    arp.addMapping("cc_stock_records_draft", StockDraftRecords.class);
    arp.addMapping("cc_takestock_bill", TakeStockBill.class);
    arp.addMapping("cc_takestock_detail", TakeStockDetail.class);
    
    arp.addMapping("cc_stock_bound", StockBound.class);
    

    arp.addMapping("cc_breakage_bill", BreakageBill.class);
    arp.addMapping("cc_breakage_detail", BreakageDetail.class);
    arp.addMapping("cc_breakage_draft_bill", BreakageDraftBill.class);
    arp.addMapping("cc_breakage_draft_detail", BreakageDraftDetail.class);
    
    arp.addMapping("cc_overflow_bill", OverflowBill.class);
    arp.addMapping("cc_overflow_detail", OverflowDetail.class);
    arp.addMapping("cc_overflow_draft_bill", OverflowDraftBill.class);
    arp.addMapping("cc_overflow_draft_detail", OverflowDraftDetail.class);
    


    arp.addMapping("cc_otherin_bill", OtherInBill.class);
    arp.addMapping("cc_otherin_detail", OtherInDetail.class);
    arp.addMapping("cc_otherin_draft_bill", OtherInDraftBill.class);
    arp.addMapping("cc_otherin_draft_detail", OtherInDraftDetail.class);
    
    arp.addMapping("cc_otherout_bill", OtherOutBill.class);
    arp.addMapping("cc_otherout_detail", OtherOutDetail.class);
    arp.addMapping("cc_otherout_draft_bill", OtherOutDraftBill.class);
    arp.addMapping("cc_otherout_draft_detail", OtherOutDraftDetail.class);
    
    arp.addMapping("cc_parityallot_bill", ParityAllotBill.class);
    arp.addMapping("cc_parityallot_detail", ParityAllotDetail.class);
    
    arp.addMapping("cc_difftallot_bill", DifftAllotBill.class);
    arp.addMapping("cc_difftallot_detail", DifftAllotDetail.class);
    arp.addMapping("cc_difftallot_draft_bill", DifftAllotDraftBill.class);
    arp.addMapping("cc_difftallot_draft_detail", DifftAllotDraftDetail.class);
    
    arp.addMapping("cc_dismount_bill", DismountBill.class);
    arp.addMapping("cc_dismount_detail", DismountDetail.class);
    
    arp.addMapping("cc_parityallot_draft_bill", ParityAllotDraftBill.class);
    arp.addMapping("cc_parityallot_draft_detail", ParityAllotDraftDetail.class);
    
    arp.addMapping("cc_dismount_draft_bill", DismountDraftBill.class);
    arp.addMapping("cc_dismount_draft_detail", DismountDraftDetail.class);
  }
}
