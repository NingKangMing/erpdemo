package com.aioerp.util;

import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class NumberUtils
{
  public static String increase(String code)
  {
    if (StringUtils.isBlank(code)) {
      return code = "1";
    }
    Pattern pattern = Pattern.compile("[0-9]*");
    String subLast = code.replaceAll(".*[^\\d](?=(\\d+))", "");
    String subFront = code.substring(0, code.indexOf(subLast));
    if (pattern.matcher(subLast).matches())
    {
      Integer pSub = Integer.valueOf(Integer.parseInt(subLast));
      String zeroStr = "";
      if (subLast.length() > pSub.toString().length())
      {
        int len = subLast.length() - pSub.toString().length();
        for (int i = 0; i < len; i++) {
          zeroStr = zeroStr + "0";
        }
      }
      int add = pSub.intValue() + 1;
      code = subFront + zeroStr + add;
    }
    else
    {
      code = code + "1";
    }
    return code;
  }
  
  public static String digitizer(int number, int charSize)
  {
    NumberFormat formatter = NumberFormat.getNumberInstance();
    formatter.setMinimumIntegerDigits(charSize);
    formatter.setGroupingUsed(false);
    return formatter.format(number);
  }
  
  public static int getMinRrcodeByArray(int[] bigs)
  {
    int min = bigs[0];
    for (int i = 0; i < bigs.length; i++) {
      if (bigs[i] < min) {
        min = bigs[i];
      }
    }
    return min;
  }
  
  public static int integerSub(Integer a, Integer b)
  {
    if (a == null) {
      a = Integer.valueOf(0);
    }
    if (b == null) {
      b = Integer.valueOf(0);
    }
    return a.intValue() - b.intValue();
  }
}
