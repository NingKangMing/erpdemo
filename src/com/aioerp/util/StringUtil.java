package com.aioerp.util;

import com.aioerp.common.AioConstants;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;

public class StringUtil
{
  public static boolean getEQStrs(String[] strs)
  {
    for (int i = 0; i < strs.length - 1; i++) {
      for (int j = i + 1; j < strs.length; j++) {
        if ((!isNull(strs[i])) && (!isNull(strs[j])) && (strs[i].equals(strs[j]))) {
          return true;
        }
      }
    }
    return false;
  }
  
  public static BigDecimal strSwitchZero(String val)
  {
    if ((val == null) || (val.equals("")) || (val.equals("null"))) {
      val = "0";
    }
    return new BigDecimal(val);
  }
  
  public static String[] strCovIds(String str)
  {
    if (isNull(str)) {
      return null;
    }
    String[] ss = str.split(",");
    for (int i = 0; i < ss.length; i++)
    {
      int lof = ss[i].lastIndexOf("-");
      if (lof > -1) {
        ss[i] = ss[i].substring(lof + 1);
      }
    }
    return ss;
  }
  
  public static int convertToInt(String s)
  {
    if (StringUtils.isNotBlank(s))
    {
      if (("启".equals(s)) || ("启用".equals(s))) {
        return AioConstants.STATUS_ENABLE;
      }
      if (("停".equals(s)) || ("停用".equals(s))) {
        return AioConstants.STATUS_DISABLE;
      }
      if ("用".equals(s)) {
        return 3;
      }
    }
    return -1;
  }
  
  public static int convertToInts(String s)
  {
    if (StringUtils.isNotBlank(s))
    {
      if ("有".equals(s)) {
        return 1;
      }
      if (("无".equals(s)) || ("没有".equals(s))) {
        return 2;
      }
    }
    return 0;
  }
  
  public static Map<String, List<String>> getAddAndDelAndEqList(String[] olds, String[] news)
  {
    Map<String, List<String>> returnMap = new HashMap();
    List<String> addList = new ArrayList();
    List<String> deleteList = new ArrayList();
    List<String> equalList = new ArrayList();
    for (int i = 0; i < news.length; i++)
    {
      String newObj = news[i];
      boolean flag = true;
      for (int j = 0; j < olds.length; j++) {
        if (newObj.equals(olds[j])) {
          flag = false;
        }
      }
      if (flag) {
        addList.add(newObj);
      } else {
        equalList.add(newObj);
      }
    }
    for (int i = 0; (i < olds.length) && (olds.length > 0) && (!olds[0].equals("")); i++)
    {
      boolean flag = true;
      for (int j = 0; j < equalList.size(); j++) {
        if (olds[i].equals(equalList.get(j))) {
          flag = false;
        }
      }
      if (flag) {
        deleteList.add(olds[i]);
      }
    }
    returnMap.put("add", addList);
    returnMap.put("delete", deleteList);
    returnMap.put("equal", equalList);
    return returnMap;
  }
  
  public static List<String> strsArrToList(String[] strs)
  {
    List<String> list = new ArrayList();
    if ((strs == null) || (strs.equals(""))) {
      return list;
    }
    for (int i = 0; i < strs.length; i++) {
      list.add(strs[i]);
    }
    return list;
  }
  
  public static boolean isNull(String str)
  {
    boolean b = true;
    if ((str != null) && (!str.equals("")) && (!str.trim().equals(""))) {
      b = false;
    }
    return b;
  }
  
  public static int strAddNumToInt(String intStr, int addNum)
  {
    if ((intStr == null) || (intStr.trim().equals(""))) {
      intStr = "0";
    }
    int in = Integer.valueOf(intStr).intValue();
    return in + addNum;
  }
  
  public static String dataToStrAddDay(Date date)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    date = DateUtils.addDays(date, 1);
    return sdf.format(date);
  }
  
  public static String dataToStr(Date date)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    return sdf.format(date);
  }
  
  public static String dataStrToStr(String dateStr)
    throws ParseException
  {
    if ((dateStr == null) || (dateStr.equals(""))) {
      return "";
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    return dataToStrAddDay(sdf.parse(dateStr));
  }
  
  public static String getCharAndNumr(int length)
  {
    String val = "";
    Random random = new Random();
    for (int i = 0; i < length; i++)
    {
      String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
      if ("char".equalsIgnoreCase(charOrNum))
      {
        int choice = random.nextInt(2) % 2 == 0 ? 65 : 97;
        val = val + (char)(choice + random.nextInt(26));
      }
      else if ("num".equalsIgnoreCase(charOrNum))
      {
        val = val + String.valueOf(random.nextInt(10));
      }
    }
    return val;
  }
  
  public static int getWordLen(String s)
  {
    if (StringUtils.isBlank(s)) {
      return 0;
    }
    int length = 0;
    for (int i = 0; i < s.length(); i++)
    {
      int ascii = Character.codePointAt(s, i);
      if ((ascii >= 0) && (ascii <= 255)) {
        length++;
      } else {
        length += 2;
      }
    }
    return length;
  }
  
  public static String convertToOperator(String str)
  {
    String operator = "=";
    if ("1".equals(str)) {
      operator = ">";
    } else if ("2".equals(str)) {
      operator = "=";
    } else if ("3".equals(str)) {
      operator = "<";
    } else if ("4".equals(str)) {
      operator = "<=";
    } else if ("5".equals(str)) {
      operator = ">=";
    }
    return operator;
  }
  
  public static List<String> printWidthRemoveNo(int size)
  {
    List<String> colWidth = new ArrayList();
    colWidth.add("30");
    for (int i = 0; i < size; i++) {
      colWidth.add("1000");
    }
    return colWidth;
  }
  
  public static String nullToStr(Object obj, String defaultName)
  {
    String objStr = String.valueOf(obj);
    if ((objStr == null) || (objStr.equals(""))) {
      return defaultName;
    }
    return objStr;
  }
}
