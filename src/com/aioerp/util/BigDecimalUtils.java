package com.aioerp.util;

import java.math.BigDecimal;

public class BigDecimalUtils
{
  public static BigDecimal operation(String type, BigDecimal a, BigDecimal b)
  {
    BigDecimal result = BigDecimal.ZERO;
    if ("+".equals(type)) {
      result = add(a, b);
    } else if ("-".equals(type)) {
      result = sub(a, b);
    } else if ("*".equals(type)) {
      result = mul(a, b);
    } else if ("/".equals(type)) {
      result = div(a, b);
    }
    return result;
  }
  
  public static BigDecimal add(BigDecimal a, BigDecimal b)
  {
    if (a == null) {
      a = BigDecimal.ZERO;
    }
    if (b == null) {
      b = BigDecimal.ZERO;
    }
    return a.add(b);
  }
  
  public static BigDecimal sub(BigDecimal a, BigDecimal b)
  {
    if (a == null) {
      a = BigDecimal.ZERO;
    }
    if (b == null) {
      b = BigDecimal.ZERO;
    }
    return a.subtract(b);
  }
  
  public static BigDecimal mul(BigDecimal a, BigDecimal b)
  {
    if (a == null) {
      a = BigDecimal.ZERO;
    }
    if (b == null) {
      b = BigDecimal.ZERO;
    }
    return a.multiply(b);
  }
  
  public static BigDecimal div(BigDecimal a, BigDecimal b)
  {
    if (a == null) {
      a = BigDecimal.ZERO;
    }
    if (b == null) {
      b = BigDecimal.ZERO;
    }
    BigDecimal zero = BigDecimal.ZERO;
    if (b.compareTo(zero) == 0) {
      return zero;
    }
    BigDecimal res = new BigDecimal(Double.toString(a.doubleValue() / b.doubleValue()));
    return trim(res);
  }
  
  public static BigDecimal negate(BigDecimal a)
  {
    if (a == null) {
      return BigDecimal.ZERO;
    }
    return a.negate();
  }
  
  public static String trim(String str)
  {
    if ((str.indexOf(".") != -1) && (str.charAt(str.length() - 1) == '0')) {
      return trim(str.substring(0, str.length() - 1));
    }
    return str.charAt(str.length() - 1) == '.' ? str.substring(0, str.length() - 1) : str;
  }
  
  public static BigDecimal trim(BigDecimal bigDecimal)
  {
    if (bigDecimal == null) {
      return bigDecimal;
    }
    String str = trim(bigDecimal.toString());
    return new BigDecimal(str);
  }
  
  public static BigDecimal div(BigDecimal a, int b)
  {
    if (a == null) {
      a = BigDecimal.ZERO;
    }
    if (b == 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal bb = new BigDecimal(b);
    BigDecimal res = new BigDecimal(Double.toString(a.doubleValue() / bb.doubleValue()));
    return trim(res);
  }
  
  public static int divInt(BigDecimal a, BigDecimal b)
  {
    if (a == null) {
      a = BigDecimal.ZERO;
    }
    if (b == null) {
      b = BigDecimal.ZERO;
    }
    BigDecimal zero = BigDecimal.ZERO;
    int count = b.compareTo(zero);
    if (count < 1) {
      return 0;
    }
    BigDecimal res = new BigDecimal(Double.toString(a.doubleValue() / b.doubleValue()));
    return trim(res).intValue();
  }
  
  public static int compare(BigDecimal a, BigDecimal b)
  {
    if (a == null) {
      a = BigDecimal.ZERO;
    }
    if (b == null) {
      b = BigDecimal.ZERO;
    }
    return a.compareTo(b);
  }
  
  public static BigDecimal model(BigDecimal a, BigDecimal b)
  {
    return a.remainder(b);
  }
  
  public static boolean notNullZero(BigDecimal a)
  {
    boolean flag = true;
    if (a == null) {
      flag = false;
    } else if (a.compareTo(BigDecimal.ZERO) == 0) {
      flag = false;
    }
    return flag;
  }
}
