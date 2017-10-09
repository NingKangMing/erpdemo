package com.aioerp.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtils
{
  public static Timestamp strToTimestamp(String str)
  {
    if ((str == null) || (str.equals(""))) {
      return null;
    }
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Timestamp ts = null;
    try
    {
      ts = new Timestamp(format.parse(str).getTime());
    }
    catch (Exception e)
    {
      return null;
    }
    return ts;
  }
  
  public static int strDateCSompare(String dateStr1, String dateStr2)
  {
    try
    {
      Date date1 = parseDate(dateStr1);
      Date date2 = parseDate(dateStr2);
      if (date1.before(date2)) {
        return -1;
      }
      if (date1.equals(date2)) {
        return 0;
      }
      if (date1.after(date2)) {
        return 1;
      }
    }
    catch (Exception e)
    {
      return -2;
    }
    return 0;
  }
  
  public static boolean hasEqual(Date date1, Date date2)
  {
    if ((date1 == null) && (date2 == null)) {
      return true;
    }
    if ((date1 == null) && (date2 != null)) {
      return false;
    }
    if ((date1 != null) && (date2 == null)) {
      return false;
    }
    return date1.compareTo(date2) == 0;
  }
  
  public static boolean isEqualDate(Date date1, Date date2)
  {
    if ((date1 == null) && (date2 == null)) {
      return true;
    }
    if ((date1 == null) && (date2 != null)) {
      return false;
    }
    if ((date1 != null) && (date2 == null)) {
      return false;
    }
    int date1Y = getYear(date1);
    int date1M = getMonth(date1);
    int date1D = getDay(date1);
    int date2Y = getYear(date2);
    int date2M = getMonth(date2);
    int date2D = getDay(date2);
    if ((date1Y == date2Y) && (date1M == date2M) && (date1D == date2D)) {
      return true;
    }
    return false;
  }
  
  public static String getFirstDateOfWeek()
  {
    Calendar c = Calendar.getInstance();
    int dayOfWeek = c.get(7) - 2;
    if (c.get(7) == 1) {
      return Format.addOneDay(getCurrentDate(), -6);
    }
    return Format.addOneDay(getCurrentDate(), -dayOfWeek);
  }
  
  public static String getFirstDateOfMonth()
  {
    Calendar c = Calendar.getInstance();
    int dayOfMonth = c.get(5) - 1;
    if (c.get(5) == 1) {
      return Format.addOneDay(getCurrentDate(), -(dayOfMonth + 1));
    }
    return Format.addOneDay(getCurrentDate(), -dayOfMonth);
  }
  
  public static String getFirstDateOfNextMonth()
  {
    Calendar c = Calendar.getInstance();
    c.add(2, 1);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
    return sdf.format(c.getTime()) + "-01";
  }
  
  public static String getLastDateOfMonth()
  {
    Calendar c = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
    return sdf.format(c.getTime()) + "-" + c.getActualMaximum(5);
  }
  
  public static String getLastDateOfByMonth(String date)
  {
    Calendar c = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
    try
    {
      c.setTime(sdf.parse(date));
    }
    catch (ParseException localParseException) {}
    return sdf.format(c.getTime()) + "-" + c.getActualMaximum(5);
  }
  
  public static String getFirstDateOfYear()
  {
    Calendar c = Calendar.getInstance();
    int dayOfYear = c.get(6);
    return Format.addOneDay(getCurrentDate(), -(dayOfYear - 1));
  }
  
  public static String getCurrentDate()
  {
    return Format.getDate(new Date());
  }
  
  public static String getCurrentTime()
  {
    Date date = new Date();
    return Format.getTime(date);
  }
  
  public static String getSqMonth(String str)
  {
    String str1 = "";
    String str2 = "";
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM");
    try
    {
      Date date = sf.parse(str);
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      cal.add(2, -1);
      str1 = sf.format(cal.getTime());
      cal.add(2, -1);
      str2 = sf.format(cal.getTime());
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return str1 + "," + str2;
  }
  
  public static String getBeferThreeMonth(String str)
  {
    String str1 = "";
    String str2 = "";
    String str3 = "";
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM");
    try
    {
      Date date = sf.parse(str);
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      cal.add(2, -1);
      str1 = sf.format(cal.getTime());
      cal.add(2, -1);
      str2 = sf.format(cal.getTime());
      cal.add(2, -1);
      str3 = sf.format(cal.getTime());
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return str1 + "," + str2 + "," + str3;
  }
  
  public static int getDayOfYear(String date)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Calendar c = Calendar.getInstance();
    try
    {
      c.setTime(format.parse(date));
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return c.get(6);
  }
  
  public static int getDayOfWeek(String strDate)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Calendar cal = Calendar.getInstance();
    int week = 0;
    try
    {
      cal.setTime(format.parse(strDate));
      week = cal.get(7) - 1;
      if (week == 0) {
        week = 7;
      }
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return week;
  }
  
  public static int getDayOfWeek(Date date)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int week = cal.get(7) - 1;
    if (week == 0) {
      week = 7;
    }
    return week;
  }
  
  public static int getYear(String date)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Calendar c = Calendar.getInstance();
    try
    {
      c.setTime(format.parse(date));
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return c.get(1);
  }
  
  public static int getYear(Date date)
  {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.get(1);
  }
  
  public static int getMonth(Date date)
  {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.get(2) + 1;
  }
  
  public static int getMonth(String date)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Calendar c = Calendar.getInstance();
    try
    {
      c.setTime(format.parse(date));
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return c.get(2) + 1;
  }
  
  public static int getMonth2(String date)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
    Calendar c = Calendar.getInstance();
    try
    {
      c.setTime(format.parse(date));
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return c.get(2) + 1;
  }
  
  public static int getDay(Date date)
  {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    
    return c.get(5);
  }
  
  public static String getFirstDayOfMonth()
  {
    Calendar c = Calendar.getInstance();
    int currDay = c.get(5);
    Date date = addDays(new Date(), -(currDay - 1));
    return Format.getDate(date);
  }
  
  public static String getChinese(String date)
  {
    String cdate = null;
    if (date != null)
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      try
      {
        Calendar c = Calendar.getInstance();
        c.setTime(format.parse(date));
        int year = c.get(1);
        int month = c.get(2) + 1;
        int day = c.get(5);
        cdate = year + "年" + month + "月" + day + "日";
      }
      catch (ParseException e)
      {
        e.printStackTrace();
      }
    }
    return cdate;
  }
  
  public static String getChinese(Date date)
  {
    String cdate = null;
    if (date != null) {
      try
      {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(1);
        int month = c.get(2) + 1;
        int day = c.get(5);
        cdate = year + "年" + month + "月" + day + "日";
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    return cdate;
  }
  
  public static String getChineseMonthDay(String date)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    String cdate = null;
    try
    {
      Calendar c = Calendar.getInstance();
      c.setTime(format.parse(date));
      
      int month = c.get(2) + 1;
      int day = c.get(5);
      cdate = month + "月" + day + "日";
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return cdate;
  }
  
  public static int getYear()
  {
    Calendar c = Calendar.getInstance();
    return c.get(1);
  }
  
  public static int getMonth()
  {
    Calendar c = Calendar.getInstance();
    return c.get(2);
  }
  
  public static int getDayOfMonth()
  {
    Calendar c = Calendar.getInstance();
    return c.get(5);
  }
  
  public static String getYYYYMMDD()
  {
    String year = String.valueOf(getYear());
    String month = String.valueOf(getMonth());
    if (month.length() == 1) {
      month = "0" + month;
    }
    String day = String.valueOf(getDayOfMonth());
    if (day.length() == 1) {
      day = "0" + day;
    }
    String value = year + month + day;
    return value;
  }
  
  public static String getRandom(int len)
  {
    String value = "";
    for (int i = 0; i < len; i++)
    {
      int num = (int)(Math.random() * 10.0D);
      value = value + num;
    }
    return value;
  }
  
  public static String getNumber(int len)
  {
    return getYYYYMMDD() + getRandom(len);
  }
  
  public static Date addDays(Date date, int day)
  {
    if (date == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(5, day);
    return c.getTime();
  }
  
  public static Date addMonths(Date date, int day)
  {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(2, day);
    return c.getTime();
  }
  
  public static Date addYears(Date date, int day)
  {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(1, day);
    return c.getTime();
  }
  
  public static Date addMillisecond(Date date, int millisecond)
  {
    if (date == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(14, millisecond);
    return c.getTime();
  }
  
  public static Date addMinute(Date date, int millisecond)
  {
    if (date == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(12, millisecond);
    return c.getTime();
  }
  
  public static Date parseDate(String date)
    throws ParseException
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    return format.parse(date);
  }
  
  public static Date parseDate(String date, String parse)
    throws ParseException
  {
    SimpleDateFormat format = new SimpleDateFormat(parse);
    return format.parse(date);
  }
  
  public static String format(Date date)
    throws ParseException
  {
    if (date == null) {
      return null;
    }
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    return format.format(date);
  }
  
  public static String format(Date date, String pattern)
    throws ParseException
  {
    if (date == null) {
      return null;
    }
    SimpleDateFormat format = new SimpleDateFormat(pattern);
    return format.format(date);
  }
  
  public static boolean isSameMonth(String date1, String date2)
    throws ParseException
  {
    Date d1 = parseDate(date1);
    Date d2 = parseDate(date2);
    
    Calendar c1 = Calendar.getInstance();
    c1.setTime(d1);
    Calendar c2 = Calendar.getInstance();
    c2.setTime(d2);
    if (c1.get(2) == c2.get(2)) {
      return true;
    }
    return false;
  }
  
  public static long getDayFromNow(Date date)
  {
    long old_ = date.getTime();
    long new_ = new Date().getTime();
    long result = new_ - old_;
    return result / 86400000L;
  }
  
  public static String getDate(long value)
  {
    Date d = new Date(value);
    try
    {
      return format(d);
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  public static long getDayFromNow(long date)
  {
    long new_ = new Date().getTime();
    long result = date - new_;
    return result / 86400000L;
  }
  
  public static int compareDay(Date dateFrom, Date dateTo)
  {
    int result = 0;
    if ((dateFrom != null) && (dateTo != null))
    {
      long timeFrom = dateFrom.getTime();
      long timeTo = dateTo.getTime();
      result = (int)((timeTo - timeFrom) / 86400000L);
    }
    return result;
  }
  
  public static Date parseDate_ymdhm(String date)
    throws ParseException
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    Date da = format.parse(date);
    return da;
  }
  
  private static void addValue(List list, String[] starts, String[] ends)
  {
    for (int mon = Integer.parseInt(starts[1]); mon <= Integer.parseInt(ends[1]); mon++)
    {
      String mon_ = String.valueOf(mon);
      if (mon_.length() != 2) {
        mon_ = "0" + mon_;
      }
      String date = starts[0] + "-" + mon_;
      list.add(date);
    }
  }
  
  public static List getDate(String sTime, String endTime, List list)
    throws ParseException
  {
    if (list == null) {
      list = new ArrayList();
    }
    String[] starts = sTime.split("-");
    String[] ends = endTime.split("-");
    for (int year = Integer.parseInt(starts[0]); year <= Integer.parseInt(ends[0]); year++)
    {
      String s = year + "-";
      if (year == Integer.parseInt(starts[0])) {
        s = s + starts[1];
      } else {
        s = s + "01";
      }
      String end = "";
      if (year == Integer.parseInt(ends[0])) {
        end = year + "-" + ends[1];
      } else {
        end = year + "-" + "12";
      }
      addValue(list, s.split("-"), end.split("-"));
    }
    return list;
  }
  
  public static String getCurrDate()
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月");
    return format.format(new Date());
  }
  
  public static int getActualMaximum(String monthDate)
  {
    int day = 0;
    try
    {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
      Calendar calendar = Calendar.getInstance();
      Date date = sdf.parse(monthDate);
      calendar.setTime(date);
      day = calendar.getActualMaximum(5);
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return day;
  }
  
  public static int getWorkDayBy(String monthDate)
  {
    int day = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String date1 = monthDate + "-01";
    String date2 = "";
    Calendar cal = Calendar.getInstance();
    try
    {
      cal.setTime(sdf.parse(date1));
      cal.add(5, getActualMaximum(monthDate) - 1);
      date2 = sdf.format(cal.getTime());
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    day = getWorkDay(date1, date2);
    return day;
  }
  
  public static int getWorkDay(String date1, String date2)
  {
    Calendar gc = Calendar.getInstance();
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat holidaysdf = new SimpleDateFormat("MM-dd");
    
    int workDay = 0;
    try
    {
      Date d1 = sdf.parse(date1);
      Date d2 = sdf.parse(date2);
      gc.setTime(d1);
      long time = d2.getTime() - d1.getTime();
      long day = time / 3600000L / 24L + 1L;
      for (int i = 0; i < day; i++)
      {
        if ((gc.get(7) != 7) && (gc.get(7) != 1) && 
          (!holidayList(holidaysdf.format(gc.getTime()))) && (!holidayOfCN(sdf.format(gc.getTime())))) {
          workDay++;
        }
        gc.add(5, 1);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return workDay;
  }
  
  public static boolean holidayOfCN(String year)
  {
    List<String> ls = new ArrayList();
    ls.add("2005-02-09");ls.add("2005-02-10");ls.add("2005-02-11");
    ls.add("2006-01-29");ls.add("2006-01-30");ls.add("2006-01-31");
    ls.add("2007-02-18");ls.add("2007-02-19");ls.add("2007-02-21");
    ls.add("2008-02-07");ls.add("2008-02-08");ls.add("2008-02-09");
    ls.add("2009-01-26");ls.add("2009-01-27");ls.add("2009-01-28");
    ls.add("2010-02-14");ls.add("2010-02-15");ls.add("2010-02-16");
    ls.add("2011-02-03");ls.add("2011-02-04");ls.add("2011-02-05");
    ls.add("2012-01-23");ls.add("2012-01-24");ls.add("2012-01-25");
    ls.add("2013-02-10");ls.add("2013-02-11");ls.add("2013-02-12");
    ls.add("2014-01-31");ls.add("2014-02-01");ls.add("2014-02-02");
    ls.add("2015-02-19");ls.add("2015-02-20");ls.add("2015-02-21");
    ls.add("2006-02-08");ls.add("2006-02-09");ls.add("2006-02-10");
    ls.add("2017-01-28");ls.add("2017-01-29");ls.add("2017-01-30");
    ls.add("2018-02-16");ls.add("2018-02-17");ls.add("2018-02-18");
    ls.add("2019-02-05");ls.add("2019-02-06");ls.add("2019-02-07");
    ls.add("2020-01-25");ls.add("2020-01-26");ls.add("2020-01-27");
    

    ls.add("2005-01-01");
    ls.add("2006-01-01");
    ls.add("2007-01-01");
    ls.add("2008-01-01");
    ls.add("2009-01-01");
    ls.add("2010-01-01");
    ls.add("2011-01-01");
    ls.add("2012-01-01");
    ls.add("2013-01-01");
    ls.add("2014-01-01");
    ls.add("2015-01-01");
    ls.add("2016-01-01");
    ls.add("2017-01-01");
    ls.add("2018-01-01");
    ls.add("2019-01-01");
    ls.add("2020-01-01");
    
    ls.add("2011-04-05");
    ls.add("2012-04-04");
    ls.add("2013-04-04");
    ls.add("2014-04-05");
    ls.add("2015-04-05");
    ls.add("2016-04-04");
    ls.add("2017-04-04");
    

    ls.add("2011-06-06");
    ls.add("2012-06-22");
    ls.add("2013-06-16");
    ls.add("2014-06-02");
    ls.add("2015-06-20");
    ls.add("2016-06-09");
    ls.add("2017-05-30");
    
    ls.add("2011-09-12");
    ls.add("2012-09-30");
    ls.add("2013-09-19");
    ls.add("2013-09-20");
    ls.add("2013-09-21");
    ls.add("2014-09-08");
    ls.add("2015-09-27");
    ls.add("2016-09-15");
    ls.add("2017-10-04");
    if (ls.contains(year)) {
      return true;
    }
    return false;
  }
  
  public static boolean holidayList(String findDate)
  {
    List<String> ls = new ArrayList();
    ls.add("04-30");
    ls.add("05-01");
    ls.add("05-02");
    ls.add("05-03");
    
    ls.add("09-28");
    ls.add("09-29");
    ls.add("09-30");
    ls.add("10-01");
    ls.add("10-02");
    ls.add("10-03");
    ls.add("10-04");
    ls.add("10-05");
    ls.add("10-06");
    if (ls.contains(findDate)) {
      return true;
    }
    return false;
  }
}
