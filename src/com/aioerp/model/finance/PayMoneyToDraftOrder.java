package com.aioerp.model.finance;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;

public class PayMoneyToDraftOrder
  extends BaseDbModel
{
  public static final PayMoneyToDraftOrder dao = new PayMoneyToDraftOrder();
  
  public void delUnitOrders(String configName, int cwBillTypeId, int cwBillId)
  {
    Db.use(configName).update("delete from cw_pay_by_draft_order where cwBillTypeId=" + cwBillTypeId + " and cwBillId=" + cwBillId);
  }
}
