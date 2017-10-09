package com.aioerp.util;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Format
{
  public static Timestamp getYYYYMMDDHHMM(String strTime)
  {
    Date date = null;
    String str = "";
    Timestamp time = null;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    try
    {
      date = format.parse(strTime);
      str = format.format(date);
      time = Timestamp.valueOf(str);
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return time;
  }
  
  public static String getDate(Timestamp time)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    String date = format.format(time);
    return date;
  }
  
  public static String getDateAndTimeStr(Timestamp time)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = format.format(time);
    return date;
  }
  
  public static Timestamp getDateAndTime(Date time)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    String date = format.format(time);
    return Timestamp.valueOf(date);
  }
  
  public static String getDate(Date time)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    return format.format(time);
  }
  
  public static String getTime(Date time)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = format.format(time);
    return date;
  }
  
  public static String getYYYYMMDDHHMM(Date date)
  {
    String returnStr = "";
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    returnStr = format.format(date);
    return returnStr + ":00.000";
  }
  
  public static int getHour(Timestamp time)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(time);
    return calendar.get(11);
  }
  
  public static int getMin(Timestamp time)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(time);
    return calendar.get(12);
  }
  
  public static String getMoneyForm(double money)
  {
    DecimalFormat df = new DecimalFormat("####.00");
    return df.format(money);
  }
  
  public static String delZero(double money)
  {
    DecimalFormat df = new DecimalFormat("####");
    return df.format(money);
  }
  
  public static int getDays(String start, String end)
  {
    String[] starts = start.split("-");
    String[] ends = end.split("-");
    int styear = Integer.parseInt(starts[0]);
    int stmon = Integer.parseInt(starts[1]);
    int stday = Integer.parseInt(starts[2]);
    int endyear = Integer.parseInt(ends[0]);
    int endmon = Integer.parseInt(ends[1]);
    int endday = Integer.parseInt(ends[2]);
    Calendar startTime = Calendar.getInstance();
    Calendar endTime = Calendar.getInstance();
    startTime.set(styear, stmon, stday);
    endTime.set(endyear, endmon, endday);
    
    long mins = endTime.getTimeInMillis() - startTime.getTimeInMillis();
    return (int)(mins / 1000L / 60L / 60L / 24L);
  }
  
  public static String addOneDay(String start, int num)
  {
    String[] starts = start.split("-");
    int styear = Integer.parseInt(starts[0]);
    int stmon = Integer.parseInt(starts[1]) - 1;
    int stday = Integer.parseInt(starts[2]);
    Calendar startTime = Calendar.getInstance();
    startTime.set(styear, stmon, stday);
    int days = startTime.get(6);
    startTime.set(6, days + num);
    Date date = startTime.getTime();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    return format.format(date);
  }
  
  public int compareTo(Date d1, Date d2)
  {
    Calendar c1 = Calendar.getInstance();
    Calendar c2 = Calendar.getInstance();
    c1.setTime(d1);
    c2.setTime(d2);
    return c1.compareTo(c2);
  }
  
  public static Date getCurrDateAndTime()
  {
    Calendar c = Calendar.getInstance();
    return c.getTime();
  }
  
  public static Timestamp getCurrTimestamp()
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    Calendar c = Calendar.getInstance();
    String str = format.format(c.getTime());
    return Timestamp.valueOf(str);
  }
  
  public static String getCurrDate()
  {
    Calendar c = Calendar.getInstance();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    return format.format(c.getTime());
  }
  
  public static List findBoxRegion(List boxList)
  {
    int size = boxList.size();
    

    List regList = new ArrayList();
    int[] buf = new int[2];
    if (size > 0)
    {
      buf[0] = Integer.parseInt((String)boxList.get(0));
      if (size == 1) {
        buf[1] = buf[0];
      }
      regList.add(buf);
      for (int i = 0; i < size - 1; i++)
      {
        int tmp1 = Integer.parseInt((String)boxList.get(i));
        int tmp2 = Integer.parseInt((String)boxList.get(i + 1));
        if (tmp2 - tmp1 == 1)
        {
          if (i == size - 2)
          {
            int[] b = (int[])regList.get(regList.size() - 1);
            b[1] = tmp2;
          }
        }
        else
        {
          int[] b = (int[])regList.get(regList.size() - 1);
          b[1] = tmp1;
          buf = new int[2];
          buf[0] = tmp2;
          regList.add(buf);
          if (i == size - 2)
          {
            b = (int[])regList.get(regList.size() - 1);
            b[1] = tmp2;
          }
        }
      }
    }
    return regList;
  }
  
  public static List getBoxNoList(String fromBox, String toBox)
  {
    int to = Integer.parseInt(toBox);
    int from = Integer.parseInt(fromBox);
    int count = to - from + 1;
    List list = new ArrayList();
    for (int i = 0; i < count; i++)
    {
      String tmp = String.valueOf(from);
      list.add(tmp);
      from++;
    }
    return list;
  }
}
