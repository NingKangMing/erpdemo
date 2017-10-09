package com.aioerp.util;

import com.aioerp.common.AioConstants;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class UrlCode
{
  public static String aioEncode(String str)
    throws UnsupportedEncodingException
  {
    return URLEncoder.encode(str, AioConstants.ENCODING);
  }
  
  public static String aioDecode(String str)
    throws Exception
  {
    return URLDecoder.decode(str, AioConstants.ENCODING);
  }
  
  public static void main(String[] args)
    throws Exception
  {
    String aa = aioEncode("张三");
    System.out.println(aa);
    System.out.println(aioDecode(aa));
  }
}
