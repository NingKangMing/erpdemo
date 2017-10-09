package com.aioerp.common.plugin;

import com.aioerp.model.fz.Formula;
import com.aioerp.model.fz.MinSellPrice;
import com.aioerp.model.fz.PriceDiscountTrack;
import com.aioerp.model.fz.ProduceTemplate;
import com.aioerp.model.fz.ProduceTemplateDetail;
import com.aioerp.model.fz.ReportRow;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class FZPlugin
{
  public FZPlugin(ActiveRecordPlugin arp)
  {
    arp.addMapping("fz_produce_template", ProduceTemplate.class);
    arp.addMapping("fz_produce_template_detail", ProduceTemplateDetail.class);
    

    arp.addMapping("fz_pricediscount_track", PriceDiscountTrack.class);
    
    arp.addMapping("fz_formula", Formula.class);
    
    arp.addMapping("fz_minsell_price", MinSellPrice.class);
    
    arp.addMapping("fz_report_row", ReportRow.class);
  }
}
