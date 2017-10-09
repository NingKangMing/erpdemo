package com.aioerp.controller.base;

import com.aioerp.model.base.Avgprice;
import com.aioerp.util.BigDecimalUtils;
import java.math.BigDecimal;

public class AvgpriceConTroller
{
  public static void eidtAvgprice(String configName, Integer storageId, Integer productId, BigDecimal avgprice)
  {
    Avgprice storageAvg = Avgprice.dao.getAvgprice(configName, storageId, productId);
    if (storageAvg != null)
    {
      BigDecimal amount = storageAvg.getBigDecimal("amount");
      BigDecimal costMoneys = BigDecimalUtils.mul(avgprice, amount);
      storageAvg.set("avgPrice", avgprice);
      storageAvg.set("costMoneys", costMoneys);
      storageAvg.update(configName);
      
      Avgprice all = Avgprice.dao.getAvgprice(configName, null, productId);
      if (all != null)
      {
        BigDecimal allAmount = all.getBigDecimal("amount");
        BigDecimal surAllAmount = BigDecimalUtils.sub(allAmount, amount);
        BigDecimal surAllCostMoneys = BigDecimalUtils.mul(surAllAmount, all.getBigDecimal("avgPrice"));
        BigDecimal allAvgPrice = BigDecimalUtils.div(BigDecimalUtils.add(costMoneys, surAllCostMoneys), allAmount);
        all.set("avgPrice", allAvgPrice);
        all.set("costMoneys", BigDecimalUtils.mul(allAvgPrice, allAmount));
        all.update(configName);
      }
    }
  }
  
  public static BigDecimal eidtAvgprice(String configName, Integer storageId, Integer productId, BigDecimal avgprice, BigDecimal baseAmount)
  {
    Avgprice storageAvg = Avgprice.dao.getAvgprice(configName, storageId, productId);
    BigDecimal newAvgprice = avgprice;
    if (storageAvg != null)
    {
      BigDecimal amount = storageAvg.getBigDecimal("amount");
      BigDecimal surAmount = BigDecimalUtils.sub(amount, baseAmount);
      BigDecimal costMoneys = BigDecimal.ZERO;
      if (BigDecimalUtils.compare(surAmount, BigDecimal.ZERO) >= 0)
      {
        BigDecimal surMoneys = BigDecimalUtils.mul(surAmount, storageAvg.getBigDecimal("avgPrice"));
        costMoneys = BigDecimalUtils.mul(avgprice, baseAmount);
        newAvgprice = BigDecimalUtils.div(BigDecimalUtils.add(surMoneys, costMoneys), amount);
        storageAvg.set("avgPrice", newAvgprice);
        storageAvg.set("costMoneys", BigDecimalUtils.mul(newAvgprice, amount));
        storageAvg.update(configName);
      }
      else
      {
        costMoneys = BigDecimalUtils.mul(avgprice, amount);
        storageAvg.set("avgPrice", avgprice);
        storageAvg.set("costMoneys", costMoneys);
        storageAvg.update(configName);
      }
      Avgprice all = Avgprice.dao.getAvgprice(configName, null, productId);
      if (all != null)
      {
        BigDecimal allAmount = all.getBigDecimal("amount");
        BigDecimal surAllAmount = BigDecimalUtils.sub(allAmount, amount);
        BigDecimal surAllCostMoneys = BigDecimalUtils.mul(surAllAmount, all.getBigDecimal("avgPrice"));
        BigDecimal allAvgPrice = BigDecimalUtils.div(BigDecimalUtils.add(costMoneys, surAllCostMoneys), allAmount);
        all.set("avgPrice", allAvgPrice);
        all.set("costMoneys", BigDecimalUtils.mul(allAvgPrice, allAmount));
        all.update(configName);
      }
    }
    return newAvgprice;
  }
  
  public static BigDecimal addAvgprice(String configName, String type, Integer storageId, Integer productId, BigDecimal amount, BigDecimal costMoney)
  {
    BigDecimal stockMoneys = BigDecimal.ZERO;
    BigDecimal stockAmount = BigDecimal.ZERO;
    
    Avgprice all = Avgprice.dao.getAvgprice(configName, null, productId);
    if (all != null)
    {
      if (type.equals("in"))
      {
        stockMoneys = BigDecimalUtils.add(all.getBigDecimal("costMoneys"), costMoney);
        stockAmount = BigDecimalUtils.add(all.getBigDecimal("amount"), amount);
      }
      else if (type.equals("out"))
      {
        stockMoneys = BigDecimalUtils.sub(all.getBigDecimal("costMoneys"), costMoney);
        stockAmount = BigDecimalUtils.sub(all.getBigDecimal("amount"), amount);
      }
      if ((BigDecimalUtils.compare(stockAmount, BigDecimal.ZERO) == 0) && (BigDecimalUtils.compare(stockMoneys, BigDecimal.ZERO) == 0))
      {
        all.delete(configName);
      }
      else
      {
        all.set("costMoneys", stockMoneys);
        all.set("amount", stockAmount);
        all.set("avgPrice", BigDecimalUtils.div(stockMoneys, stockAmount));
        all.update(configName);
      }
    }
    else
    {
      BigDecimal amount1 = amount;
      BigDecimal costMoney1 = costMoney;
      if (type.equals("out"))
      {
        amount1 = BigDecimalUtils.negate(amount1);
        costMoney1 = BigDecimalUtils.negate(costMoney1);
      }
      all = new Avgprice();
      all.set("productId", productId);
      all.set("costMoneys", costMoney1);
      all.set("amount", amount1);
      all.set("avgPrice", BigDecimalUtils.div(costMoney1, amount1));
      all.save(configName);
    }
    Avgprice storageAvg = Avgprice.dao.getAvgprice(configName, storageId, productId);
    if (storageAvg != null)
    {
      if (type.equals("in"))
      {
        stockMoneys = BigDecimalUtils.add(storageAvg.getBigDecimal("costMoneys"), costMoney);
        stockAmount = BigDecimalUtils.add(storageAvg.getBigDecimal("amount"), amount);
      }
      else if (type.equals("out"))
      {
        stockMoneys = BigDecimalUtils.sub(storageAvg.getBigDecimal("costMoneys"), costMoney);
        stockAmount = BigDecimalUtils.sub(storageAvg.getBigDecimal("amount"), amount);
      }
      if ((BigDecimalUtils.compare(stockAmount, BigDecimal.ZERO) == 0) && (BigDecimalUtils.compare(stockMoneys, BigDecimal.ZERO) == 0))
      {
        storageAvg.delete(configName);
        return BigDecimal.ZERO;
      }
      BigDecimal avgPrice = BigDecimalUtils.div(stockMoneys, stockAmount);
      storageAvg.set("costMoneys", stockMoneys);
      storageAvg.set("amount", stockAmount);
      storageAvg.set("avgPrice", avgPrice);
      storageAvg.update(configName);
      return avgPrice;
    }
    BigDecimal amount1 = amount;
    BigDecimal costMoney1 = costMoney;
    if (type.equals("out"))
    {
      amount1 = BigDecimalUtils.negate(amount1);
      costMoney1 = BigDecimalUtils.negate(costMoney1);
    }
    storageAvg = new Avgprice();
    BigDecimal avgPrice = BigDecimalUtils.div(costMoney1, amount1);
    storageAvg.set("storageId", storageId);
    storageAvg.set("productId", productId);
    storageAvg.set("costMoneys", costMoney1);
    storageAvg.set("amount", amount1);
    storageAvg.set("avgPrice", avgPrice);
    storageAvg.save(configName);
    return avgPrice;
  }
}
