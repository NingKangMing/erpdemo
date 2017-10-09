package com.aioerp.controller.common;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.finance.PayType;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import java.util.List;
import java.util.Map;

public class BillController
  extends BaseController
{
  public void index() {}
  
  public int getRealBillId(String configName, int billId, String reloadType, String tableName)
  {
    if ((reloadType == null) || (reloadType.equals(""))) {
      return billId;
    }
    if ((tableName == null) || (tableName.equals(""))) {
      return billId;
    }
    String orderby = "desc";
    StringBuffer sql = new StringBuffer();
    if (reloadType.equals("firstBill"))
    {
      orderby = "asc";
      sql.append("select id from " + tableName + " where 1=1");
    }
    else if (reloadType.equals("preBill"))
    {
      orderby = "desc";
      sql.append("select id from " + tableName + " where id<" + billId);
    }
    else if (reloadType.equals("nextBill"))
    {
      orderby = "asc";
      sql.append("select id from " + tableName + " where id>" + billId);
    }
    else if (reloadType.equals("lastBill"))
    {
      orderby = "desc";
      sql.append("select id from " + tableName + " where 1=1");
    }
    sql.append(" order by id " + orderby);
    Record record = Db.use(configName).findFirst(sql.toString());
    if (record != null) {
      billId = record.getInt("id").intValue();
    }
    return billId;
  }
  
  public void lookBill()
  {
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int type = getParaToInt(1, Integer.valueOf(0)).intValue();
    

    String configName = loginConfigName();
    String tableName = "";
    String forwardUrl = "";
    String hasUrlParam = "";
    if (AioConstants.BILL_ROW_TYPE1 != type) {
      if (AioConstants.BILL_ROW_TYPE2 != type) {
        if (AioConstants.BILL_ROW_TYPE3 != type) {
          if (AioConstants.BILL_ROW_TYPE4 == type)
          {
            tableName = "xs_sell_bill";
            forwardUrl = "/sell/sell/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE5 == type)
          {
            tableName = "cg_purchase_bill";
            forwardUrl = "/bought/purchase/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE6 == type)
          {
            tableName = "cg_return_bill";
            forwardUrl = "/reports/bought/return/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE7 == type)
          {
            tableName = "xs_return_bill";
            forwardUrl = "/sell/return/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE8 == type)
          {
            tableName = "cc_breakage_bill";
            forwardUrl = "/stock/breakage/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE9 == type)
          {
            tableName = "cc_overflow_bill";
            forwardUrl = "/stock/overflow/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE10 == type)
          {
            tableName = "cc_otherin_bill";
            forwardUrl = "/stock/otherin/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE11 == type)
          {
            tableName = "cc_otherout_bill";
            forwardUrl = "/stock/otherout/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE12 == type)
          {
            tableName = "cg_barter_bill";
            forwardUrl = "/bought/barter/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE13 == type)
          {
            tableName = "xs_barter_bill";
            forwardUrl = "/sell/barter/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE14 == type)
          {
            tableName = "cc_parityallot_bill";
            forwardUrl = "/stock/parityAllot/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE15 == type)
          {
            tableName = "cc_difftallot_bill";
            forwardUrl = "/stock/difftAllot/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE16 == type)
          {
            tableName = "cc_dismount_bill";
            forwardUrl = "/stock/dismount/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE17 == type)
          {
            tableName = "cw_getmoney_bill";
            forwardUrl = "/finance/getMoney/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE18 == type)
          {
            tableName = "cw_otherearn_bill";
            forwardUrl = "/finance/otherEarn/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE19 == type)
          {
            tableName = "cw_paymoney_bill";
            forwardUrl = "/finance/payMoney/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE20 == type)
          {
            tableName = "cc_adjust_cost_bill";
            forwardUrl = "/finance/adjustCost/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE21 == type)
          {
            tableName = "cw_transfer_bill";
            forwardUrl = "/finance/transfer/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE22 == type)
          {
            tableName = "cw_fee_bill";
            forwardUrl = "/finance/fee/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE23 == type)
          {
            tableName = "cw_addassets_bill";
            forwardUrl = "/finance/addAssets/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE24 == type)
          {
            tableName = "cw_deprassets_bill";
            forwardUrl = "/finance/deprAssets/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE25 == type)
          {
            tableName = "cw_subassets_bill";
            forwardUrl = "/finance/subAssets/look/" + billId;
          }
          else if (AioConstants.BILL_ROW_TYPE26 == type)
          {
            tableName = "cw_c_unitgetorpay_bill";
            forwardUrl = "/finance/changeUnit/look/" + billId + "-" + type;
          }
          else if (AioConstants.BILL_ROW_TYPE27 == type)
          {
            tableName = "cw_c_unitgetorpay_bill";
            forwardUrl = "/finance/changeUnit/look/" + billId + "-" + type;
          }
          else if (AioConstants.BILL_ROW_TYPE28 == type)
          {
            tableName = "cw_c_unitgetorpay_bill";
            forwardUrl = "/finance/changeUnit/look/" + billId + "-" + type;
          }
          else if (AioConstants.BILL_ROW_TYPE29 == type)
          {
            tableName = "cw_c_unitgetorpay_bill";
            forwardUrl = "/finance/changeUnit/look/" + billId + "-" + type;
          }
          else if (AioConstants.BILL_ROW_TYPE30 == type)
          {
            tableName = "cw_c_money_bill";
            forwardUrl = "/finance/changeUnit/look/" + billId + "-" + type;
          }
          else if (AioConstants.BILL_ROW_TYPE31 == type)
          {
            tableName = "cw_c_money_bill";
            forwardUrl = "/finance/changeUnit/look/" + billId + "-" + type;
          }
          else if (AioConstants.BILL_ROW_TYPE32 == type)
          {
            tableName = "cw_accountvoucher_bill";
            forwardUrl = "/finance/accountVoucher/look/" + billId;
          }
        }
      }
    }
    String reloadType = getPara("reloadType");
    if ((reloadType == null) || (reloadType.equals("")))
    {
      if (!hasUrlParam.equals("")) {
        forwardUrl = forwardUrl + hasUrlParam;
      }
      forwardAction(forwardUrl);
    }
    else
    {
      billId = getRealBillId(configName, billId, reloadType, tableName);
      this.result.put("url", "/common/bill/lookBill/" + billId + "-" + type);
      renderJson(this.result);
      return;
    }
  }
  
  public void editDraftBill()
  {
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int type = getParaToInt(1, Integer.valueOf(0)).intValue();
    int sourceId = getParaToInt(2, Integer.valueOf(0)).intValue();
    
    String currerStr = "";
    String checkObjIds = getPara("checkObjIds", "");
    if (!"".equals(checkObjIds))
    {
      currerStr = checkObjIds.substring(0, checkObjIds.indexOf(","));
      String[] currs = currerStr.split("-");
      billId = Integer.parseInt(currs[0]);
      type = Integer.parseInt(currs[1]);
      sourceId = Integer.parseInt(currs[2]);
      String nextdraft = checkObjIds.replace(currerStr + ",", "");
      setSessionAttr("draftAutoPost", Boolean.valueOf(true));
      if (!"".equals(nextdraft)) {
        setSessionAttr("draftStrs", nextdraft);
      } else {
        removeSessionAttr("draftStrs");
      }
    }
    else
    {
      removeSessionAttr("draftStrs");
      removeSessionAttr("draftAutoPost");
    }
    if (AioConstants.BILL_ROW_TYPE1 != type) {
      if (AioConstants.BILL_ROW_TYPE2 != type) {
        if (AioConstants.BILL_ROW_TYPE3 != type) {
          if (AioConstants.BILL_ROW_TYPE4 == type) {
            forwardAction("/sell/sell/draft/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE5 == type) {
            forwardAction("/bought/purchase/draft/toEdit/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE6 == type) {
            forwardAction("/bought/return/draft/toEdit/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE7 == type) {
            forwardAction("/sell/return/draft/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE8 == type) {
            forwardAction("/stock/breakage/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE9 == type) {
            forwardAction("/stock/overflow/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE10 == type) {
            forwardAction("/stock/otherin/draft/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE11 == type) {
            forwardAction("/stock/otherout/draft/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE12 == type) {
            forwardAction("/bought/barter/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE13 == type) {
            forwardAction("/sell/barter/draft/toEdit/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE14 == type) {
            forwardAction("/stock/parityAllot/draft/toEdit/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE15 == type) {
            forwardAction("/stock/difftAllot/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE16 == type) {
            forwardAction("/stock/dismount/draft/toEdit/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE17 == type) {
            forwardAction("/finance/getMoney/draft/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE18 == type) {
            forwardAction("/finance/otherEarn/draft/toEdit/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE19 == type) {
            forwardAction("/finance/payMoney/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE20 == type) {
            forwardAction("/finance/adjustCost/draft/toEdit/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE21 == type) {
            forwardAction("/finance/transfer/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE22 == type) {
            forwardAction("/finance/fee/draft/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE23 == type) {
            forwardAction("/finance/addAssets/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE24 == type) {
            forwardAction("/finance/deprAssets/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE25 == type) {
            forwardAction("/finance/subAssets/toEditDraft/" + billId + "-" + sourceId);
          } else if (AioConstants.BILL_ROW_TYPE26 == type) {
            forwardAction("/finance/changeUnit/draft/toEditDraft/" + billId + "-" + sourceId + "-" + type);
          } else if (AioConstants.BILL_ROW_TYPE27 == type) {
            forwardAction("/finance/changeUnit/draft/toEditDraft/" + billId + "-" + sourceId + "-" + type);
          } else if (AioConstants.BILL_ROW_TYPE28 == type) {
            forwardAction("/finance/changeUnit/draft/toEditDraft/" + billId + "-" + sourceId + "-" + type);
          } else if (AioConstants.BILL_ROW_TYPE29 == type) {
            forwardAction("/finance/changeUnit/draft/toEditDraft/" + billId + "-" + sourceId + "-" + type);
          } else if (AioConstants.BILL_ROW_TYPE30 == type) {
            forwardAction("/finance/changeUnit/draft/toEditDraft/" + billId + "-" + sourceId + "-" + type);
          } else if (AioConstants.BILL_ROW_TYPE31 == type) {
            forwardAction("/finance/changeUnit/draft/toEditDraft/" + billId + "-" + sourceId + "-" + type);
          } else if (AioConstants.BILL_ROW_TYPE32 == type) {
            forwardAction("/finance/accountVoucher/draft/toEdit/" + billId + "-" + sourceId);
          }
        }
      }
    }
  }
  
  public void billAccountDetail()
  {
    int billTypeId = getParaToInt("billTypeId", Integer.valueOf(0)).intValue();
    int billId = getParaToInt("billId", Integer.valueOf(0)).intValue();
    List<Record> list = PayType.dao.billAccountDetail(loginConfigName(), billTypeId, billId);
    setAttr("list", list);
    render("/WEB-INF/template/finance/payType/billAccountDetail.html");
  }
  
  public void addPrintCount()
  {
    this.result.put("statusCode", AioConstants.HTTP_RETURN200);
    this.result.put("message", "操作成功");
    String tableName = getPara("tableName", "");
    int id = getParaToInt("id", Integer.valueOf(0)).intValue();
    String attr = getPara("attr", "");
    try
    {
      if ((tableName.equals("")) || (id == 0)) {
        return;
      }
      if ((attr == null) || (attr.equals(""))) {
        attr = "printNum";
      }
      String configName = loginConfigName();
      Record r = Db.use(configName).findById(tableName, Integer.valueOf(id));
      Integer printNum = r.getInt(attr);
      if (printNum == null) {
        printNum = Integer.valueOf(0);
      }
      printNum = Integer.valueOf(printNum.intValue() + 1);
      r.set(attr, printNum);
      Db.use(configName).update(tableName, r);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      this.result.put("message", "操作失败");
    }
    renderJson(this.result);
  }
}
