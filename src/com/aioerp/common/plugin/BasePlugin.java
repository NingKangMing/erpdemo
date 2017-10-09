package com.aioerp.common.plugin;

import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Area;
import com.aioerp.model.base.Avgprice;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.ProductUnit;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class BasePlugin
{
  public BasePlugin(ActiveRecordPlugin arp)
  {
    arp.addMapping("b_unit", Unit.class);
    arp.addMapping("b_department", Department.class);
    arp.addMapping("b_product", Product.class);
    arp.addMapping("b_product_unit", ProductUnit.class);
    arp.addMapping("b_area", Area.class);
    arp.addMapping("b_staff", Staff.class);
    arp.addMapping("b_storage", Storage.class);
    arp.addMapping("b_accounts", Accounts.class);
    arp.addMapping("zj_product_avgprice", Avgprice.class);
  }
}
