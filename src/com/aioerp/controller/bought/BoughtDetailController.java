package com.aioerp.controller.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.bought.BoughtBill;
import com.aioerp.model.bought.BoughtDetail;
import com.aioerp.model.sys.BillRow;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoughtDetailController
  extends BaseController
{
  public void checkBack()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String ids = getPara(1);
    Map<String, Object> map = new HashMap();
    List<Model> detailList = BoughtDetail.dao.getList(configName, ids);
    map.put("detailList", detailList);
    map.put("bill", BoughtBill.dao.getRecordById(configName, billId));
    map.put("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, AioConstants.BILL_ROW_TYPE5, "discount")));
    map.put("showTaxRate", Boolean.valueOf(BillRow.dao.showRow(configName, AioConstants.BILL_ROW_TYPE5, "taxRate")));
    renderJson(map);
  }
  
  public static void editDetailAmount(String configName, BigDecimal baseAmount, BoughtDetail boughtDetail)
  {
    if (boughtDetail == null) {
      return;
    }
    BigDecimal untreatedAmount = boughtDetail.getBigDecimal("untreatedAmount");
    
    boughtDetail.set("updateTime", new Date());
    BigDecimal arrivalAmount = boughtDetail.getBigDecimal("arrivalAmount");
    boughtDetail.set("arrivalAmount", BigDecimalUtils.add(baseAmount, arrivalAmount));
    if ((untreatedAmount != null) && (baseAmount != null))
    {
      if (BigDecimalUtils.compare(untreatedAmount, baseAmount) == 1)
      {
        boughtDetail.set("untreatedAmount", BigDecimalUtils.sub(untreatedAmount, baseAmount));
        boughtDetail.update(configName);
      }
      else
      {
        boughtDetail.set("untreatedAmount", Integer.valueOf(0));
        boughtDetail.set("replenishAmount", BigDecimalUtils.sub(baseAmount, untreatedAmount));
        boughtDetail.update(configName);
      }
    }
    else if ((untreatedAmount == null) && (baseAmount != null))
    {
      boughtDetail.set("untreatedAmount", Integer.valueOf(0));
      boughtDetail.update(configName);
    }
    Integer billId = boughtDetail.getInt("billId");
    List<Model> detail = BoughtDetail.dao.getUntreatedList(configName, billId, basePrivs(PRODUCT_PRIVS));
    if ((detail == null) || (detail.size() == 0))
    {
      BoughtBill bill = (BoughtBill)BoughtBill.dao.findById(configName, billId);
      bill.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      bill.set("updateTime", new Date());
      bill.update(configName);
    }
  }
}
