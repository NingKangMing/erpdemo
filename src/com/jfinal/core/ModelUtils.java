package com.jfinal.core;

import com.jfinal.plugin.activerecord.Model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("unchecked")
public class ModelUtils
{
  public static <T> List<T> batchInjectModel(HttpServletRequest request, Class<? extends Model> modelClass, String prefix)
  {
   
	List<T> modelList = new ArrayList();
    
    int size = getArrayLength(request, prefix);
    for (int i = 0; i < size; i++) {
      modelList.add((T) ModelInjector.inject(modelClass, prefix + "[" + i + 
      
        "]", request, false));
    }
    return modelList;
  }
  
  public static <T> List<T> batchInjectObjModel(HttpServletRequest request, Class<? extends Model> modelClass, String prefix)
  {
    List<T> modelList = new ArrayList();
    Object[] objs = getArrayKeys(request, prefix).toArray();
    for (Object object : objs) {
      modelList.add((T) ModelInjector.inject(modelClass, object.toString(), request, false));
    }
    return modelList;
  }
  
  public static <T> List<T> batchInjectSortObjModel(HttpServletRequest request, Class<? extends Model> modelClass, String prefix)
  {
    List<T> modelList = new ArrayList();
    Object[] objs = getArraySortKeys(request, prefix).toArray();
    for (Object object : objs) {
      modelList.add((T) ModelInjector.inject(modelClass, object.toString(), request, false));
    }
    return modelList;
  }
  
  public static <T> List<T> batchInjectHelpModel(HttpServletRequest request, Class<?> modelClass, String prefix)
  {
    List<T> modelList = new ArrayList();
    Object[] objs = getArrayKeys(request, prefix).toArray();
    for (Object object : objs) {
      modelList.add((T) ModelInjector.inject(modelClass, object.toString(), request, false));
    }
    return modelList;
  }
  
  public static <T> List<T> batchInjectSortHelpModel(HttpServletRequest request, Class<?> modelClass, String prefix)
  {
    List<T> modelList = new ArrayList();
    Object[] objs = getArraySortKeys(request, prefix).toArray();
    for (Object object : objs) {
      modelList.add((T) ModelInjector.inject(modelClass, object.toString(), request, false));
    }
    return modelList;
  }
  
  public static Set<String> getArrayKeys(HttpServletRequest request, String prefix)
  {
    Set<String> keys = new HashSet();
    String arrayPrefix = prefix + "[";
    String key = null;
    Enumeration<String> names = request.getParameterNames();
    while (names.hasMoreElements())
    {
      key = (String)names.nextElement();
      if (key.startsWith(arrayPrefix)) {
        if (key.indexOf("]") != -1) {
          keys.add(key.substring(0, key.indexOf("]") + 1));
        }
      }
    }
    return keys;
  }
  
  public static List<String> getArraySortKeys(HttpServletRequest request, String prefix)
  {
    List<String> keys = new ArrayList();
    Map<Integer, String> keyMaps = new HashMap();
    List<Integer> trIndexList = new ArrayList();
    String arrayPrefix = prefix + "[";
    String key = null;
    Enumeration<String> names = request.getParameterNames();
    while (names.hasMoreElements())
    {
      key = (String)names.nextElement();
      if (key.startsWith(arrayPrefix)) {
        if (key.indexOf("]") != -1)
        {
          Integer trIndex = Integer.valueOf(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
          String trIndexVal = key.substring(0, key.indexOf("]") + 1);
          if (!keyMaps.containsKey(trIndex)) {
            trIndexList.add(trIndex);
          }
          keyMaps.put(trIndex, trIndexVal);
        }
      }
    }
    Collections.sort(trIndexList);
    for (int i = 0; i < trIndexList.size(); i++) {
      keys.add((String)keyMaps.get(trIndexList.get(i)));
    }
    return keys;
  }
  
  public static int getArrayLength(HttpServletRequest request, String prefix)
  {
    return getArrayKeys(request, prefix).size();
  }
}
