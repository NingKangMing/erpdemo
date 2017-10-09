package com.aioerp.util.dbsource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

public class Abc
{
  public static void main(String[] args)
    throws IOException
  {
    String aa = "E:\\projects\\project-javaweb\\aioerp-basic\\WebRoot\\backup\\555.zip";
    String bb = "E:\\projects\\project-javaweb\\aioerp-basic\\WebRoot\\backup\\555";
    
    unZipFiles(new File(aa), bb);
  }
  
  public static void unZipFiles(File zipFile, String descDir)
    throws IOException
  {
    File pathFile = new File(descDir);
    if (!pathFile.exists()) {
      pathFile.mkdirs();
    }
    ZipFile zip = new ZipFile(zipFile);
    for (Enumeration entries = zip.getEntries(); entries.hasMoreElements();)
    {
      ZipEntry entry = (ZipEntry)entries.nextElement();
      String zipEntryName = entry.getName();
      InputStream in = zip.getInputStream(entry);
      String outPath = descDir + File.separator + zipEntryName;
      
      File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
      if (!file.exists()) {
        file.mkdirs();
      }
      if (!new File(outPath).isDirectory())
      {
        System.out.println(outPath);
        
        OutputStream out = new FileOutputStream(outPath);
        byte[] buf1 = new byte[1024];
        int len;
        while ((len = in.read(buf1)) > 0)
        {
          int len1 = 0;
          out.write(buf1, 0, len1);
        }
        in.close();
        out.close();
      }
    }
    System.out.println("-------解压完成------");
  }
}
