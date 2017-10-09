package com.aioerp.common.plugin;

import com.aioerp.model.finance.AccountVoucherBill;
import com.aioerp.model.finance.AccountVoucherDetail;
import com.aioerp.model.finance.AccountVoucherDraftBill;
import com.aioerp.model.finance.AccountVoucherDraftDetail;
import com.aioerp.model.finance.AccountsInit;
import com.aioerp.model.finance.AdjustCostBill;
import com.aioerp.model.finance.AdjustCostDetail;
import com.aioerp.model.finance.AdjustCostDraftBill;
import com.aioerp.model.finance.AdjustCostDraftDetail;
import com.aioerp.model.finance.OtherEarnBill;
import com.aioerp.model.finance.OtherEarnDetail;
import com.aioerp.model.finance.OtherEarnDraftBill;
import com.aioerp.model.finance.OtherEarnDraftDetail;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.finance.PayMoneyBill;
import com.aioerp.model.finance.PayMoneyDetail;
import com.aioerp.model.finance.PayMoneyDraftBill;
import com.aioerp.model.finance.PayMoneyDraftDetail;
import com.aioerp.model.finance.PayMoneyToDraftOrder;
import com.aioerp.model.finance.PayMoneyToOrder;
import com.aioerp.model.finance.PayType;
import com.aioerp.model.finance.TransferBill;
import com.aioerp.model.finance.TransferDetail;
import com.aioerp.model.finance.TransferDraftBill;
import com.aioerp.model.finance.TransferDraftDetail;
import com.aioerp.model.finance.assets.AddAssetsBill;
import com.aioerp.model.finance.assets.AddAssetsDetail;
import com.aioerp.model.finance.assets.AddAssetsDraftBill;
import com.aioerp.model.finance.assets.AddAssetsDraftDetail;
import com.aioerp.model.finance.assets.DeprAssetsBill;
import com.aioerp.model.finance.assets.DeprAssetsDetail;
import com.aioerp.model.finance.assets.DeprAssetsDraftBill;
import com.aioerp.model.finance.assets.DeprAssetsDraftDetail;
import com.aioerp.model.finance.assets.SubAssetsBill;
import com.aioerp.model.finance.assets.SubAssetsDetail;
import com.aioerp.model.finance.assets.SubAssetsDraftBill;
import com.aioerp.model.finance.assets.SubAssetsDraftDetail;
import com.aioerp.model.finance.changegetorpay.ChangeGetOrPay;
import com.aioerp.model.finance.changegetorpay.ChangeGetOrPayDetail;
import com.aioerp.model.finance.changegetorpay.ChangeGetOrPayDraft;
import com.aioerp.model.finance.changegetorpay.ChangeGetOrPayDraftDetail;
import com.aioerp.model.finance.changemoney.ChangeMoney;
import com.aioerp.model.finance.changemoney.ChangeMoneyDetail;
import com.aioerp.model.finance.changemoney.ChangeMoneyDraft;
import com.aioerp.model.finance.changemoney.ChangeMoneyDraftDetail;
import com.aioerp.model.finance.feebill.FeeBill;
import com.aioerp.model.finance.feebill.FeeDetail;
import com.aioerp.model.finance.feebill.FeeDraftBill;
import com.aioerp.model.finance.feebill.FeeDraftDetail;
import com.aioerp.model.finance.getmoney.GetMoneyBill;
import com.aioerp.model.finance.getmoney.GetMoneyDetail;
import com.aioerp.model.finance.getmoney.GetMoneyDraftBill;
import com.aioerp.model.finance.getmoney.GetMoneyDraftDetail;
import com.aioerp.model.reports.finance.arap.ArapRecords;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class FinancePlugin
{
  public FinancePlugin(ActiveRecordPlugin arp)
  {
    arp.addMapping("cw_pay_type", PayType.class);
    arp.addMapping("cw_pay_draft", PayDraft.class);
    arp.addMapping("cw_arap_records", ArapRecords.class);
    
    arp.addMapping("cw_getmoney_bill", GetMoneyBill.class);
    arp.addMapping("cw_getmoney_detail", GetMoneyDetail.class);
    arp.addMapping("cw_getmoney_draft_bill", GetMoneyDraftBill.class);
    arp.addMapping("cw_getmoney_draft_detail", GetMoneyDraftDetail.class);
    

    arp.addMapping("cw_paymoney_bill", PayMoneyBill.class);
    arp.addMapping("cw_paymoney_detail", PayMoneyDetail.class);
    arp.addMapping("cw_paymoney_draft_bill", PayMoneyDraftBill.class);
    arp.addMapping("cw_paymoney_draft_detail", PayMoneyDraftDetail.class);
    


    arp.addMapping("cw_pay_by_order", PayMoneyToOrder.class);
    arp.addMapping("cw_pay_by_draft_order", PayMoneyToDraftOrder.class);
    
    arp.addMapping("cc_adjust_cost_bill", AdjustCostBill.class);
    arp.addMapping("cc_adjust_cost_detail", AdjustCostDetail.class);
    arp.addMapping("cc_adjust_cost_draft_bill", AdjustCostDraftBill.class);
    arp.addMapping("cc_adjust_cost_draft_detail", AdjustCostDraftDetail.class);
    
    arp.addMapping("cw_otherearn_bill", OtherEarnBill.class);
    arp.addMapping("cw_otherearn_detail", OtherEarnDetail.class);
    arp.addMapping("cw_otherearn_draft_bill", OtherEarnDraftBill.class);
    arp.addMapping("cw_otherearn_draft_detail", OtherEarnDraftDetail.class);
    

    arp.addMapping("cw_fee_bill", FeeBill.class);
    arp.addMapping("cw_fee_detail", FeeDetail.class);
    arp.addMapping("cw_fee_draft_bill", FeeDraftBill.class);
    arp.addMapping("cw_fee_draft_detail", FeeDraftDetail.class);
    

    arp.addMapping("cw_transfer_bill", TransferBill.class);
    arp.addMapping("cw_transfer_detail", TransferDetail.class);
    arp.addMapping("cw_transfer_draft_bill", TransferDraftBill.class);
    arp.addMapping("cw_transfer_draft_detail", TransferDraftDetail.class);
    

    arp.addMapping("cw_addassets_bill", AddAssetsBill.class);
    arp.addMapping("cw_addassets_detail", AddAssetsDetail.class);
    arp.addMapping("cw_addassets_draft_bill", AddAssetsDraftBill.class);
    arp.addMapping("cw_addassets_draft_detail", AddAssetsDraftDetail.class);
    

    arp.addMapping("cw_deprassets_bill", DeprAssetsBill.class);
    arp.addMapping("cw_deprassets_detail", DeprAssetsDetail.class);
    arp.addMapping("cw_deprassets_draft_bill", DeprAssetsDraftBill.class);
    arp.addMapping("cw_deprassets_draft_detail", DeprAssetsDraftDetail.class);
    

    arp.addMapping("cw_subassets_bill", SubAssetsBill.class);
    arp.addMapping("cw_subassets_detail", SubAssetsDetail.class);
    arp.addMapping("cw_subassets_draft_bill", SubAssetsDraftBill.class);
    arp.addMapping("cw_subassets_draft_detail", SubAssetsDraftDetail.class);
    
    arp.addMapping("cw_accountvoucher_bill", AccountVoucherBill.class);
    arp.addMapping("cw_accountvoucher_detail", AccountVoucherDetail.class);
    arp.addMapping("cw_accountvoucher_draft_bill", AccountVoucherDraftBill.class);
    arp.addMapping("cw_accountvoucher_draft_detail", AccountVoucherDraftDetail.class);
    
    arp.addMapping("cw_c_unitgetorpay_bill", ChangeGetOrPay.class);
    arp.addMapping("cw_c_unitgetorpay_detail", ChangeGetOrPayDetail.class);
    arp.addMapping("cw_c_unitgetorpay_draft_bill", ChangeGetOrPayDraft.class);
    arp.addMapping("cw_c_unitgetorpay_draft_detail", ChangeGetOrPayDraftDetail.class);
    
    arp.addMapping("cw_c_money_bill", ChangeMoney.class);
    arp.addMapping("cw_c_money_detail", ChangeMoneyDetail.class);
    arp.addMapping("cw_c_money_draft_bill", ChangeMoneyDraft.class);
    arp.addMapping("cw_c_money_draft_detail", ChangeMoneyDraftDetail.class);
    
    arp.addMapping("cw_accounts_init", AccountsInit.class);
  }
}
