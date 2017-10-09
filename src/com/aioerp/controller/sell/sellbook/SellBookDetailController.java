package com.aioerp.controller.sell.sellbook;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.sell.sellbook.SellbookBill;
import com.aioerp.model.sell.sellbook.SellbookDetail;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellBookDetailController
  extends BaseController
{
  public void checkBack()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String ids = getPara(1);
    Map<String, Object> map = new HashMap();
    List<Model> detailList = SellbookDetail.dao.getList(configName, ids);
    map.put("detailList", detailList);
    map.put("bill", SellbookBill.dao.getRecordById(configName, billId));
    renderJson(map);
  }
  
  public static void editDetailAmount(String configName, BigDecimal sellAmount, SellbookDetail sellbookDetail)
  {
    if (sellbookDetail == null) {
      return;
    }
    BigDecimal sellbookAmount = sellbookDetail.getBigDecimal("baseAmount");
    BigDecimal arrivalAmount = sellbookDetail.getBigDecimal("arrivalAmount");
    
    BigDecimal totalArrivalAmount = BigDecimalUtils.add(sellAmount, arrivalAmount);
    if (BigDecimalUtils.compare(sellbookAmount, totalArrivalAmount) == 1)
    {
      sellbookDetail.set("untreatedAmount", BigDecimalUtils.sub(sellbookAmount, totalArrivalAmount));
      sellbookDetail.set("arrivalAmount", totalArrivalAmount);
    }
    else if (BigDecimalUtils.compare(sellbookAmount, totalArrivalAmount) == 0)
    {
      sellbookDetail.set("relStatus", Integer.valueOf(AioConstants.STATUS_ENABLE));
      sellbookDetail.set("untreatedAmount", Integer.valueOf(0));
      sellbookDetail.set("arrivalAmount", totalArrivalAmount);
    }
    else
    {
      sellbookDetail.set("relStatus", Integer.valueOf(AioConstants.STATUS_ENABLE));
      sellbookDetail.set("untreatedAmount", Integer.valueOf(0));
      sellbookDetail.set("replenishAmount", BigDecimalUtils.sub(totalArrivalAmount, sellbookAmount));
      sellbookDetail.set("arrivalAmount", totalArrivalAmount);
    }
    sellbookDetail.set("updateTime", new Date());
    sellbookDetail.update(configName);
    Integer billId = sellbookDetail.getInt("billId");
    List<Model> detail = SellbookDetail.dao.getUntreatedList(configName, billId);
    if ((detail == null) || (detail.size() == 0)) {
      SellbookBill.dao.findById(configName, billId).set("relStatus", Integer.valueOf(AioConstants.STATUS_ENABLE)).set("updateTime", new Date()).update(configName);
    }
  }
  
  public static void rcwDetailAmount(String configName, BigDecimal sellAmount, Integer sellbookDetailId)
  {
    if ((sellbookDetailId == null) || (sellbookDetailId.intValue() == 0)) {
      return;
    }
    SellbookDetail sellbookDetail = (SellbookDetail)SellbookDetail.dao.findById(configName, sellbookDetailId);
    int billId = sellbookDetail.getInt("billId").intValue();
    BigDecimal arrivalAmount = sellbookDetail.getBigDecimal("arrivalAmount");
    BigDecimal untreatedAmount = sellbookDetail.getBigDecimal("untreatedAmount");
    BigDecimal replenishAmount = sellbookDetail.getBigDecimal("replenishAmount");
    BigDecimal forceAmount = sellbookDetail.getBigDecimal("forceAmount");
    int sellbookDetailStatus = sellbookDetail.getInt("relStatus").intValue();
    if (sellbookDetailStatus == AioConstants.STATUS_DISABLE)
    {
      sellbookDetail.set("untreatedAmount", BigDecimalUtils.add(untreatedAmount, sellAmount));
      sellbookDetail.set("arrivalAmount", BigDecimalUtils.sub(arrivalAmount, sellAmount));
    }
    else
    {
      boolean flag = false;
      if (BigDecimalUtils.compare(replenishAmount, BigDecimal.ZERO) == 1)
      {
        if (BigDecimalUtils.compare(replenishAmount, sellAmount) == 1)
        {
          sellbookDetail.set("replenishAmount", BigDecimalUtils.sub(replenishAmount, sellAmount));
          sellbookDetail.set("untreatedAmount", BigDecimal.ZERO);
          sellbookDetail.set("arrivalAmount", BigDecimalUtils.sub(arrivalAmount, sellAmount));
        }
        else if (BigDecimalUtils.compare(replenishAmount, sellAmount) == 0)
        {
          sellbookDetail.set("replenishAmount", Integer.valueOf(0));
          sellbookDetail.set("arrivalAmount", sellbookDetail.getBigDecimal("baseAmount"));
          sellbookDetail.set("untreatedAmount", Integer.valueOf(0));
        }
        else
        {
          sellbookDetail.set("replenishAmount", Integer.valueOf(0));
          sellbookDetail.set("untreatedAmount", BigDecimalUtils.sub(sellAmount, replenishAmount));
          sellbookDetail.set("arrivalAmount", BigDecimalUtils.sub(sellbookDetail.getBigDecimal("baseAmount"), sellbookDetail.getBigDecimal("untreatedAmount")));
          sellbookDetail.set("relStatus", Integer.valueOf(AioConstants.STATUS_DISABLE));
        }
        flag = true;
      }
      if (BigDecimalUtils.compare(forceAmount, BigDecimal.ZERO) == 1)
      {
        sellbookDetail.set("forceAmount", BigDecimalUtils.add(forceAmount, sellAmount));
        sellbookDetail.set("arrivalAmount", BigDecimalUtils.sub(arrivalAmount, sellAmount));
        flag = true;
      }
      if (!flag)
      {
        sellbookDetail.set("untreatedAmount", BigDecimalUtils.add(untreatedAmount, sellAmount));
        sellbookDetail.set("arrivalAmount", BigDecimalUtils.sub(arrivalAmount, sellAmount));
        sellbookDetail.set("relStatus", Integer.valueOf(AioConstants.STATUS_DISABLE));
      }
    }
    sellbookDetail.update(configName);
    

    List<Model> detail = SellbookDetail.dao.getUntreatedList(configName, Integer.valueOf(billId));
    if ((detail == null) || (detail.size() == 0)) {
      SellbookBill.dao.findById(configName, Integer.valueOf(billId)).set("relStatus", Integer.valueOf(AioConstants.STATUS_ENABLE)).set("updateTime", new Date()).update(configName);
    } else {
      SellbookBill.dao.findById(configName, Integer.valueOf(billId)).set("relStatus", Integer.valueOf(AioConstants.STATUS_DISABLE)).set("updateTime", new Date()).update(configName);
    }
  }
}
