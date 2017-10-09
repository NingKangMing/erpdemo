package com.aioerp.common.plugin;

import com.aioerp.model.bought.BoughtBill;
import com.aioerp.model.bought.BoughtDetail;
import com.aioerp.model.bought.PurchaseBarterBill;
import com.aioerp.model.bought.PurchaseBarterDetail;
import com.aioerp.model.bought.PurchaseBarterDraftBill;
import com.aioerp.model.bought.PurchaseBarterDraftDetail;
import com.aioerp.model.bought.PurchaseBill;
import com.aioerp.model.bought.PurchaseDetail;
import com.aioerp.model.bought.PurchaseDraftBill;
import com.aioerp.model.bought.PurchaseDraftDetail;
import com.aioerp.model.bought.PurchaseReturnBill;
import com.aioerp.model.bought.PurchaseReturnDetail;
import com.aioerp.model.bought.PurchaseReturnDraftBill;
import com.aioerp.model.bought.PurchaseReturnDraftDetail;
import com.aioerp.model.reports.bought.PurchaseDepartmentReports;
import com.aioerp.model.reports.bought.PurchaseReports;
import com.aioerp.model.reports.bought.PurchaseReturnReports;
import com.aioerp.model.reports.bought.PurchaseReturnUnitReports;
import com.aioerp.model.reports.bought.PurchaseStaffReports;
import com.aioerp.model.reports.bought.PurchaseStorageReports;
import com.aioerp.model.reports.bought.PurchaseUnitReports;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class BoughtPlugin
{
  public BoughtPlugin(ActiveRecordPlugin arp)
  {
    arp.addMapping("cg_bought_bill", BoughtBill.class);
    arp.addMapping("cg_bought_detail", BoughtDetail.class);
    arp.addMapping("cg_purchase_bill", PurchaseBill.class);
    arp.addMapping("cg_purchase_detail", PurchaseDetail.class);
    arp.addMapping("cg_return_bill", PurchaseReturnBill.class);
    arp.addMapping("cg_return_detail", PurchaseReturnDetail.class);
    arp.addMapping("cg_barter_bill", PurchaseBarterBill.class);
    arp.addMapping("cg_barter_detail", PurchaseBarterDetail.class);
    
    arp.addMapping("b_product", PurchaseReports.class);
    arp.addMapping("b_unit", PurchaseUnitReports.class);
    arp.addMapping("b_product", PurchaseReturnReports.class);
    arp.addMapping("b_unit", PurchaseReturnUnitReports.class);
    arp.addMapping("b_department", PurchaseDepartmentReports.class);
    arp.addMapping("b_staff", PurchaseStaffReports.class);
    arp.addMapping("b_storage", PurchaseStorageReports.class);
    

    arp.addMapping("cg_purchase_draft_bill", PurchaseDraftBill.class);
    arp.addMapping("cg_purchase_draft_detail", PurchaseDraftDetail.class);
    arp.addMapping("cg_return_draft_bill", PurchaseReturnDraftBill.class);
    arp.addMapping("cg_return_draft_detail", PurchaseReturnDraftDetail.class);
    arp.addMapping("cg_barter_draft_bill", PurchaseBarterDraftBill.class);
    arp.addMapping("cg_barter_draft_detail", PurchaseBarterDraftDetail.class);
  }
}
