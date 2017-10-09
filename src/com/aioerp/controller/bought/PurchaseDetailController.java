package com.aioerp.controller.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.bought.BoughtBill;
import com.aioerp.model.bought.BoughtDetail;
import com.aioerp.model.bought.PurchaseBill;
import com.aioerp.model.bought.PurchaseDetail;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseDetailController
  extends BaseController
{
  public void checkBack()
  {
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String ids = getPara(1);
    String configName = loginConfigName();
    Map<String, Object> map = new HashMap();
    List<Model> detailList = PurchaseDetail.dao.getList(configName, ids);
    map.put("detailList", detailList);
    map.put("bill", PurchaseBill.dao.getRecordById(configName, billId));
    renderJson(map);
  }
  
  public static void editDetailAmount(String configName, PurchaseDetail detail, BigDecimal returnAmount, Integer selectUnitId, Integer unitRelation1, BigDecimal unitRelation2, BigDecimal unitRelation3)
  {
    if (detail == null) {
      return;
    }
    BigDecimal untreatedAmount = detail.getBigDecimal("untreatedAmount");
    if (unitRelation2 == null) {
      unitRelation2 = BigDecimal.ZERO;
    }
    if (unitRelation3 == null) {
      unitRelation3 = BigDecimal.ZERO;
    }
    detail.set("updateTime", new Date());
    

    untreatedAmount = BigDecimalUtils.sub(untreatedAmount, returnAmount);
    BigDecimal currentReturnAmount = BigDecimalUtils.add(returnAmount, detail.getBigDecimal("returnAmount"));
    detail.set("returnAmount", currentReturnAmount);
    detail.set("untreatedAmount", untreatedAmount);
    detail.update(configName);
    Integer billId = detail.getInt("billId");
    List<Model> detailList = detail.getUntreatedList(configName, billId);
    if ((detailList == null) || (detailList.size() == 0)) {
      PurchaseBill.dao.findById(configName, billId).set("status", Integer.valueOf(AioConstants.STATUS_ENABLE)).update(configName);
    }
  }
  
  public static void rcwDetailAmount(String configName, BigDecimal purchaseAmount, Integer boughtDetailId)
  {
    if ((boughtDetailId == null) || (boughtDetailId.intValue() == 0)) {
      return;
    }
    Model boughtDetail = BoughtDetail.dao.findById(configName, boughtDetailId);
    int billId = boughtDetail.getInt("billId").intValue();
    BigDecimal arrivalAmount = boughtDetail.getBigDecimal("arrivalAmount");
    BigDecimal untreatedAmount = boughtDetail.getBigDecimal("untreatedAmount");
    BigDecimal replenishAmount = boughtDetail.getBigDecimal("replenishAmount");
    BigDecimal forceAmount = boughtDetail.getBigDecimal("forceAmount");
    
    Model boughtBill = BoughtBill.dao.findById(configName, Integer.valueOf(billId));
    int boughtStatus = boughtBill.getInt("status").intValue();
    if (boughtStatus == AioConstants.STATUS_DISABLE)
    {
      boughtDetail.set("untreatedAmount", BigDecimalUtils.add(untreatedAmount, purchaseAmount));
      boughtDetail.set("arrivalAmount", BigDecimalUtils.sub(arrivalAmount, purchaseAmount));
    }
    else if (BigDecimalUtils.compare(replenishAmount, BigDecimal.ZERO) == 1)
    {
      if (BigDecimalUtils.compare(replenishAmount, purchaseAmount) == 1)
      {
        boughtDetail.set("replenishAmount", BigDecimalUtils.sub(replenishAmount, purchaseAmount));
        boughtDetail.set("untreatedAmount", BigDecimal.ZERO);
        boughtDetail.set("arrivalAmount", BigDecimalUtils.sub(arrivalAmount, purchaseAmount));
      }
      else if (BigDecimalUtils.compare(replenishAmount, purchaseAmount) == 0)
      {
        boughtDetail.set("replenishAmount", Integer.valueOf(0));
        boughtDetail.set("arrivalAmount", boughtDetail.getBigDecimal("baseAmount"));
        boughtDetail.set("untreatedAmount", Integer.valueOf(0));
      }
      else
      {
        boughtDetail.set("replenishAmount", Integer.valueOf(0));
        boughtDetail.set("untreatedAmount", BigDecimalUtils.sub(purchaseAmount, replenishAmount));
        boughtDetail.set("arrivalAmount", BigDecimalUtils.sub(boughtDetail.getBigDecimal("baseAmount"), boughtDetail.getBigDecimal("untreatedAmount")));
        boughtBill.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
      }
    }
    else if (BigDecimalUtils.compare(forceAmount, BigDecimal.ZERO) == 1)
    {
      boughtDetail.set("forceAmount", BigDecimalUtils.add(forceAmount, purchaseAmount));
      boughtDetail.set("arrivalAmount", BigDecimalUtils.sub(arrivalAmount, purchaseAmount));
    }
    else
    {
      boughtDetail.set("untreatedAmount", BigDecimalUtils.add(untreatedAmount, purchaseAmount));
      boughtDetail.set("arrivalAmount", BigDecimalUtils.sub(arrivalAmount, purchaseAmount));
      boughtBill.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
    }
    boughtDetail.update(configName);
    boughtBill.set("updateTime", new Date()).update(configName);
  }
}
