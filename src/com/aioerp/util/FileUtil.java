package com.aioerp.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil
{
  public static boolean validateType(byte[] b, String customTypes)
  {
    if (b != null)
    {
      int size = b.length;
      String hex = null;
      StringBuilder contentType = new StringBuilder();
      for (int i = 0; i < size; i++)
      {
        hex = Integer.toHexString(b[i] & 0xFF);
        if (hex.length() == 1) {
          hex = "0" + hex;
        }
        contentType.append(hex);
        if (i > 2) {
          break;
        }
      }
      if (customTypes.indexOf(contentType.toString()) > -1) {
        return Boolean.TRUE.booleanValue();
      }
    }
    return Boolean.FALSE.booleanValue();
  }
  
  public static byte[] getBytesFromFile(File f)
  {
    if (f == null) {
      return null;
    }
    try
    {
      FileInputStream stream = new FileInputStream(f);
      ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
      byte[] b = new byte[1000];
      int n;
      while ((n = stream.read(b)) != -1)
      {
       
        out.write(b, 0, n);
      }
      stream.close();
      out.close();
      return out.toByteArray();
    }
    catch (IOException localIOException) {}
    return null;
  }
  
  public static File getFileFromBytes(byte[] b, String outputFile)
  {
    BufferedOutputStream stream = null;
    File file = null;
    try
    {
      file = new File(outputFile);
      FileOutputStream fstream = new FileOutputStream(file);
      stream = new BufferedOutputStream(fstream);
      stream.write(b);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      if (stream != null) {
        try
        {
          stream.close();
        }
        catch (IOException e1)
        {
          e1.printStackTrace();
        }
      }
    }
    finally
    {
      if (stream != null) {
        try
        {
          stream.close();
        }
        catch (IOException e1)
        {
          e1.printStackTrace();
        }
      }
    }
    return file;
  }
}
