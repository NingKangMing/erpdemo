package com.aioerp.util;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionUtils
{
  public static ArrayList<Object> arrayToList(Map<String, Object> map)
  {
    ArrayList<Object> paras = new ArrayList();
    if ((map != null) && (map.size() > 0))
    {
      Iterator<String> iter = map.keySet().iterator();
      while (iter.hasNext()) {
        paras.add(map.get(iter.next()));
      }
    }
    return paras;
  }
  
  public static void arraySort(List<Model> list, final String attr, String sort)
  {
	 /* 
    Collections.sort(list, new Comparator()
    {
      public int compare(Object a, Object b)
      {
        ret = 0;
        try
        {
          if ((CollectionUtils.this != null) && ("desc".equals(CollectionUtils.this))) {
            try
            {
              ret = new BigDecimal(((Model)b).get(attr, "0").toString()).compareTo(new BigDecimal(((Model)a).get(attr, "0").toString()));
            }
            catch (Exception e)
            {
              ret = ((Model)b).get(attr, "0").toString().compareTo(((Model)a).get(attr, "0").toString());
            }
          } else {
            try
            {
              ret = new BigDecimal(((Model)a).get(attr, "0").toString()).compareTo(new BigDecimal(((Model)b).get(attr, "0").toString()));
            }
            catch (Exception e)
            {
              ret = ((Model)a).get(attr, "0").toString().compareTo(((Model)b).get(attr, "0").toString());
            }
          }
          return ret;
        }
        catch (Exception ne)
        {
          ne.printStackTrace();
        }
      }
    });
    */
  }
  
  public static void recordArraySort(List<Record> list, final String attr, String sort)
  {
	/*  
    Collections.sort(list, new Comparator()
    {
      public int compare(Object a, Object b)
      {
        ret = 0;
        try
        {
          if ((CollectionUtils.this != null) && ("desc".equals(CollectionUtils.this))) {
            try
            {
              ret = new BigDecimal(((Record)b).get(attr, "0").toString()).compareTo(new BigDecimal(((Record)a).get(attr, "0").toString()));
            }
            catch (Exception e)
            {
              ret = ((Record)b).get(attr, "0").toString().compareTo(((Record)a).get(attr, "0").toString());
            }
          } else {
            try
            {
              ret = new BigDecimal(((Record)a).get(attr, "0").toString()).compareTo(new BigDecimal(((Record)b).get(attr, "0").toString()));
            }
            catch (Exception e)
            {
              ret = ((Record)a).get(attr, "0").toString().compareTo(((Record)b).get(attr, "0").toString());
            }
          }
          return ret;
        }
        catch (Exception ne)
        {
          ne.printStackTrace();
        }
      }
    });
    */
  }
  
  public static String strArrToString(String[] strs, String separator)
  {
    String str = "";
    for (int i = 0; i < strs.length; i++) {
      str = str + separator + strs[i];
    }
    str = str.substring(separator.length(), str.length());
    return str;
  }
}
