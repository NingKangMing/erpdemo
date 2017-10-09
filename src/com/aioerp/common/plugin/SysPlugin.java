package com.aioerp.common.plugin;

import com.aioerp.model.sys.AioerpFile;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillCodeConfig;
import com.aioerp.model.sys.BillCodeConfigFormat;
import com.aioerp.model.sys.BillCodeFlow;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.BillSort;
import com.aioerp.model.sys.BillType;
import com.aioerp.model.sys.DeliveryCompany;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.model.sys.SysUser;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.model.sys.end.MonthEnd;
import com.aioerp.model.sys.end.YearEnd;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class SysPlugin
{
  public SysPlugin(ActiveRecordPlugin arp)
  {
    arp.addMapping("aioerp_file", AioerpFile.class);
    arp.addMapping("sys_billsort", BillSort.class);
    arp.addMapping("sys_billtype", BillType.class);
    arp.addMapping("sys_bill_row", BillRow.class);
    arp.addMapping("sys_billcodeconfig_format", BillCodeConfigFormat.class);
    arp.addMapping("sys_billcodeconfig", BillCodeConfig.class);
    arp.addMapping("sys_billcode_flow", BillCodeFlow.class);
    
    arp.addMapping("delivery_company", DeliveryCompany.class);
    arp.addMapping("aioerp_sys", AioerpSys.class);
    
    arp.addMapping("sys_monthend", MonthEnd.class);
    arp.addMapping("sys_yearend", YearEnd.class);
    
    arp.addMapping("aioerp_sys_user", SysUser.class);
    arp.addMapping("aioerp_sys_config", SysConfig.class);
    arp.addMapping("sys_user_searchdate", SysUserSearchDate.class);
  }
}
