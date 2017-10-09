package com.aioerp.common.plugin;

import com.aioerp.db.reports.BillHistory;
import com.aioerp.db.reports.BusinessDraft;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class ReportsPlugin
{
  public ReportsPlugin(ActiveRecordPlugin arp)
  {
    arp.addMapping("bb_billhistory", BillHistory.class);
    arp.addMapping("bb_businessdraft", BusinessDraft.class);
  }
}
