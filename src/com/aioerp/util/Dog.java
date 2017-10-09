package com.aioerp.util;

public class Dog
{
  static
  {
   // System.loadLibrary("aioauth");
  }
  
  public static native boolean register(String paramString);
  
  public static native boolean isRegister();
  
  public static native boolean reload();
  
  public static native String getID();
  
  public static native String getCardNO();
  
  public static native int getVersion();
  
  public static native int getUserCount();
  
  public static native int getLastDay();
  
  public static native String getCompany();
  
  public static native String getArea();
  
  public static native String getRDate();
  
  public static native String getSDate();
}
