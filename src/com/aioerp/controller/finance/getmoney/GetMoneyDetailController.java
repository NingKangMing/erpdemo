package com.aioerp.controller.finance.getmoney;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.sell.sell.SellBill;
import com.aioerp.model.sell.sell.SellDetail;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Model;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetMoneyDetailController
  extends BaseController
{
  public void checkBack()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String ids = getPara(1);
    Map<String, Object> map = new HashMap();
    List<Model> detailList = SellDetail.dao.getList(configName, ids);
    map.put("detailList", detailList);
    map.put("bill", SellBill.dao.getRecordById(configName, billId));
    renderJson(map);
  }
  
  public static void editDetailAmount(String configName, BigDecimal sellReturnAmount, SellDetail sellDetail)
  {
    BigDecimal sellAmount = sellDetail.getBigDecimal("baseAmount");
    BigDecimal arrivalAmount = sellDetail.getBigDecimal("arrivalAmount");
    


    BigDecimal totalArrivalAmount = BigDecimalUtils.add(sellReturnAmount, arrivalAmount);
    if (BigDecimalUtils.compare(sellAmount, totalArrivalAmount) == 1)
    {
      sellDetail.set("untreatedAmount", BigDecimalUtils.sub(sellAmount, totalArrivalAmount));
      sellDetail.set("arrivalAmount", totalArrivalAmount);
    }
    else if (BigDecimalUtils.compare(sellAmount, totalArrivalAmount) == 0)
    {
      sellDetail.set("untreatedAmount", Integer.valueOf(0));
      sellDetail.set("arrivalAmount", totalArrivalAmount);
    }
    else
    {
      sellDetail.set("untreatedAmount", Integer.valueOf(0));
      sellDetail.set("arrivalAmount", sellAmount);
    }
    sellDetail.set("updateTime", new Date());
    sellDetail.update(configName);
    Integer billId = sellDetail.getInt("billId");
    List<Model> detail = SellDetail.dao.getUntreatedList(configName, billId);
    if ((detail == null) || (detail.size() == 0)) {
      SellBill.dao.findById(configName, billId).set("relStatus", Integer.valueOf(AioConstants.STATUS_ENABLE)).set("updateTime", new Date()).update(configName);
    }
  }
}
