package com.aioerp.util;

import com.aioerp.model.base.Product;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class DwzUtils
{
  public static String helpUnit(String calculateUnit1, String calculateUnit2, String calculateUnit3, Integer unitRelation1, BigDecimal unitRelation2, BigDecimal unitRelation3)
  {
    String str = "";
    if ((StringUtils.isNotBlank(calculateUnit1)) && (unitRelation1.intValue() > 0)) {
      if ((StringUtils.isNotBlank(calculateUnit2)) && (unitRelation2 != null))
      {
        if ((StringUtils.isNotBlank(calculateUnit3)) && (unitRelation3 != null)) {
          str = "1" + calculateUnit3 + "=" + BigDecimalUtils.div(unitRelation3, unitRelation2).toString() + calculateUnit2 + "=" + BigDecimalUtils.div(unitRelation3, unitRelation1.intValue()).toString() + calculateUnit1;
        } else {
          str = "1" + calculateUnit2 + "=" + BigDecimalUtils.div(unitRelation2, unitRelation1.intValue()) + calculateUnit1;
        }
      }
      else if ((StringUtils.isNotBlank(calculateUnit3)) && (unitRelation3 != null)) {
        str = "1" + calculateUnit3 + "=" + BigDecimalUtils.div(unitRelation3, unitRelation1.intValue()) + calculateUnit1;
      } else {
        str = "1" + calculateUnit1;
      }
    }
    return str;
  }
  
  public static String helpAmount(BigDecimal amount, String calculateUnit1, String calculateUnit2, String calculateUnit3, BigDecimal unitRelation2, BigDecimal unitRelation3)
  {
    Integer unitRelation1 = Integer.valueOf(1);
    String str = "";
    if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) == 0) {
      return str;
    }
    if ((StringUtils.isBlank(calculateUnit1)) || (unitRelation1.intValue() <= 0)) {
      return str;
    }
    if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) == -1)
    {
      str = "-";
      amount = amount.abs();
    }
    amount = BigDecimalUtils.trim(amount);
    if ((StringUtils.isNotBlank(calculateUnit2)) && (BigDecimalUtils.notNullZero(unitRelation2)))
    {
      if ((StringUtils.isNotBlank(calculateUnit3)) && (BigDecimalUtils.notNullZero(unitRelation3)))
      {
        int u3 = BigDecimalUtils.divInt(amount, unitRelation3);
        if (u3 > 0)
        {
          str = str + BigDecimalUtils.divInt(amount, unitRelation3) + calculateUnit3;
          amount = BigDecimalUtils.model(amount, unitRelation3);
        }
        int u2 = BigDecimalUtils.divInt(amount, unitRelation2);
        if (u2 > 0)
        {
          str = str + BigDecimalUtils.divInt(amount, unitRelation2) + calculateUnit2;
          amount = BigDecimalUtils.model(amount, unitRelation2);
        }
        if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) > 0) {
          str = str + BigDecimalUtils.trim(amount.setScale(4, 4)) + calculateUnit1;
        }
      }
      else
      {
        int u2 = BigDecimalUtils.divInt(amount, unitRelation2);
        if (u2 > 0)
        {
          str = str + BigDecimalUtils.divInt(amount, unitRelation2) + calculateUnit2;
          amount = BigDecimalUtils.model(amount, unitRelation2);
        }
        if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) > 0) {
          str = str + BigDecimalUtils.trim(amount.setScale(4, 4)) + calculateUnit1;
        }
      }
    }
    else {
      str = amount + calculateUnit1;
    }
    return BigDecimalUtils.trim(str);
  }
  
  public static String helpAmount(String pattern, BigDecimal litterAmount, String calculateUnit1, String calculateUnit2, String calculateUnit3, BigDecimal unitRelation2, BigDecimal unitRelation3)
  {
    String str = "";
    if (BigDecimalUtils.compare(litterAmount, BigDecimal.ZERO) == 0) {
      return str;
    }
    if ((pattern == null) || (pattern.equals(""))) {
      return str;
    }
    litterAmount = litterAmount.abs();
    if (pattern.equals("blendUnit")) {
      str = helpAmount(litterAmount, calculateUnit1, calculateUnit2, calculateUnit3, unitRelation2, unitRelation3);
    } else if (pattern.equals("helpUnit1"))
    {
      if ((StringUtils.isNotBlank(calculateUnit2)) && (BigDecimalUtils.notNullZero(unitRelation2))) {
        str = divIntUnitDecimal(litterAmount, unitRelation2, calculateUnit2);
      }
    }
    else if ((pattern.equals("helpUnit2")) && 
      (StringUtils.isNotBlank(calculateUnit3)) && (BigDecimalUtils.notNullZero(unitRelation3))) {
      str = divIntUnitDecimal(litterAmount, unitRelation3, calculateUnit3);
    }
    return str;
  }
  
  public static String divIntUnit(BigDecimal a, BigDecimal b, String unit)
  {
    String str = "";
    if (a == null) {
      a = BigDecimal.ZERO;
    }
    if (b == null) {
      b = BigDecimal.ZERO;
    }
    BigDecimal zero = BigDecimal.ZERO;
    int count = b.compareTo(zero);
    if (count < 1) {
      return "";
    }
    BigDecimal res = new BigDecimal(Double.toString(a.doubleValue() / b.doubleValue()));
    res = res.abs();
    double eachUnit = res.stripTrailingZeros().doubleValue();
    if (eachUnit > 0.0D) {
      str = eachUnit + unit;
    }
    return str;
  }
  
  public static String divIntUnitDecimal(BigDecimal a, BigDecimal b, String unit)
  {
    String str = "";
    if (a == null) {
      a = BigDecimal.ZERO;
    }
    if (b == null) {
      b = BigDecimal.ZERO;
    }
    BigDecimal zero = BigDecimal.ZERO;
    int count = b.compareTo(zero);
    if (count < 1) {
      return "";
    }
    BigDecimal res = new BigDecimal(Double.toString(a.doubleValue() / b.doubleValue()));
    Double eachUnit = Double.valueOf(res.doubleValue());
    if (eachUnit.doubleValue() > 0.0D) {
      str = eachUnit + unit;
    }
    return str;
  }
  
  public static String amountAndUnit(BigDecimal a, String unit)
  {
    String str = "";
    if (BigDecimalUtils.notNullZero(a)) {
      str = a + str;
    } else {
      return str;
    }
    return str;
  }
  
  public static BigDecimal getConversionAmount(BigDecimal amount, Integer selectUnitId, Product product, Integer newSelectUnitId)
  {
    return getConversionAmount(amount, selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), newSelectUnitId);
  }
  
  public static BigDecimal getConversionAmount(BigDecimal amount, Integer selectUnitId, Integer unitRelation1, BigDecimal unitRelation2, BigDecimal unitRelation3, Integer newSelectUnitId)
  {
    BigDecimal selUnitRelation = BigDecimal.ZERO;
    BigDecimal oldUnitRelation = BigDecimal.ZERO;
    if ((newSelectUnitId != null) && (selectUnitId != null) && (newSelectUnitId != selectUnitId))
    {
      if (selectUnitId.intValue() == 1) {
        selUnitRelation = new BigDecimal(unitRelation1.intValue());
      } else if (selectUnitId.intValue() == 2) {
        selUnitRelation = unitRelation2;
      } else if (selectUnitId.intValue() == 3) {
        selUnitRelation = unitRelation3;
      }
      if (newSelectUnitId.intValue() == 1) {
        oldUnitRelation = new BigDecimal(unitRelation1.intValue());
      } else if (newSelectUnitId.intValue() == 2) {
        oldUnitRelation = unitRelation2;
      } else if (newSelectUnitId.intValue() == 3) {
        oldUnitRelation = unitRelation3;
      }
      if (BigDecimalUtils.compare(selUnitRelation, new BigDecimal(0)) != 0) {
        amount = BigDecimalUtils.mul(selUnitRelation, BigDecimalUtils.div(amount, oldUnitRelation));
      }
    }
    if (amount != null) {
      amount = amount.setScale(4, 4);
    }
    amount = BigDecimalUtils.trim(amount);
    
    return amount;
  }
  
  public static BigDecimal getConversionPrice(BigDecimal price, Integer selectUnitId, Product product, Integer newSelectUnitId)
  {
    return getConversionPrice(price, selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), newSelectUnitId);
  }
  
  public static BigDecimal getConversionPrice(BigDecimal price, Integer selectUnitId, Integer unitRelation1, BigDecimal unitRelation2, BigDecimal unitRelation3, Integer newSelectUnitId)
  {
    BigDecimal selUnitRelation = new BigDecimal(0);
    BigDecimal oldUnitRelation = new BigDecimal(0);
    if (unitRelation1 == null) {
      unitRelation1 = Integer.valueOf(0);
    }
    if (unitRelation2 == null) {
      unitRelation2 = BigDecimal.ZERO;
    }
    if (unitRelation3 == null) {
      unitRelation3 = BigDecimal.ZERO;
    }
    if ((newSelectUnitId != null) && (selectUnitId != null) && (newSelectUnitId != selectUnitId))
    {
      if (selectUnitId.intValue() == 1) {
        selUnitRelation = new BigDecimal(unitRelation1.intValue());
      } else if (selectUnitId.intValue() == 2) {
        selUnitRelation = unitRelation2;
      } else if (selectUnitId.intValue() == 3) {
        selUnitRelation = unitRelation3;
      }
      if (newSelectUnitId.intValue() == 1) {
        oldUnitRelation = new BigDecimal(unitRelation1.intValue());
      } else if (newSelectUnitId.intValue() == 2) {
        oldUnitRelation = unitRelation2;
      } else if (newSelectUnitId.intValue() == 3) {
        oldUnitRelation = unitRelation3;
      }
      if (BigDecimalUtils.compare(selUnitRelation, new BigDecimal(0)) != 0) {
        price = BigDecimalUtils.mul(oldUnitRelation, BigDecimalUtils.div(price, selUnitRelation).setScale(4, 4)).setScale(4, 4);
      }
    }
    return price;
  }
  
  public static Map<String, Object> RequestConvertMap(Map<String, String[]> requestMap)
  {
    Map<String, Object> paraMap = new HashMap();
    Object[] keys = requestMap.keySet().toArray();
    for (int i = 0; i < requestMap.size(); i++) {
      if ((!"_".equals(keys[i])) && (!"aimTabId".equals(keys[i])) && (!"aimUrl".equals(keys[i])) && (!"aimTitle".equals(keys[i])) && (!"aimDiv".equals(keys[i])) && (!"aimDlgId".equals(keys[i])) && (!"aimWidth".equals(keys[i])) && (!"aimHeight".equals(keys[i])) && (!"isPost".equals(keys[i])))
      {
        String[] vals = (String[])requestMap.get(keys[i]);
        if ((vals == null) || (vals.length <= 1)) {
          paraMap.put((String)keys[i], ((String[])requestMap.get(keys[i]))[0]);
        } else {
          paraMap.put((String)keys[i], requestMap.get(keys[i]));
        }
      }
    }
    return paraMap;
  }
  
  public static String getSelectUnit(Product product, Integer selectUnitId)
  {
    if (selectUnitId == null) {
      selectUnitId = Integer.valueOf(1);
    }
    String selectUnit = "";
    if (selectUnitId.intValue() == 1) {
      selectUnit = product.getStr("calculateUnit1");
    } else if (selectUnitId.intValue() == 2) {
      selectUnit = product.getStr("calculateUnit2");
    } else if (selectUnitId.intValue() == 3) {
      selectUnit = product.getStr("calculateUnit3");
    }
    return selectUnit;
  }
}
