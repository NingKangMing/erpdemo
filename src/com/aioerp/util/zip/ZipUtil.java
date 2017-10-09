package com.aioerp.util.zip;

import com.aioerp.util.IO;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil
{
  private static final Logger logger = Logger.getLogger(ZipUtil.class.getName());
  private static final int BUFFER = 10240;
  
  public static boolean zip(String sourceFilePath, String zipFilePath)
  {
    boolean result = false;
    File source = new File(sourceFilePath);
    if (!source.exists())
    {
      logger.info(sourceFilePath + " doesn't exist.");
      return result;
    }
    if (!source.isDirectory())
    {
      logger.info(sourceFilePath + " is not a directory.");
      return result;
    }
    File zipFile = new File(zipFilePath);
    if (zipFile.exists())
    {
      logger.info(zipFile.getName() + " is already exist.");
      return result;
    }
    if ((!zipFile.getParentFile().exists()) && 
      (!zipFile.getParentFile().mkdirs()))
    {
      logger.info("cann't create file " + zipFile.getName());
      return result;
    }
    logger.info("creating zip file...");
    FileOutputStream dest = null;
    ZipOutputStream out = null;
    try
    {
      dest = new FileOutputStream(zipFile);
      CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
      out = new ZipOutputStream(new BufferedOutputStream(checksum));
      out.setMethod(8);
      compress(source, out, source.getName());
      result = true;
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (out != null)
      {
        try
        {
          out.closeEntry();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
        try
        {
          out.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
    if (result) {
      logger.info("done.");
    } else {
      logger.info("fail.");
    }
    return result;
  }
  
  private static void compress(File file, ZipOutputStream out, String mainFileName)
  {
    String entryName;
    ZipEntry entry;
    if (file.isFile())
    {
      FileInputStream fi = null;
      BufferedInputStream origin = null;
      try
      {
        fi = new FileInputStream(file);
        origin = new BufferedInputStream(fi, 10240);
        int index = file.getAbsolutePath().indexOf(mainFileName);
        entryName = file.getAbsolutePath().substring(index);
        System.out.println(entryName);
        entry = new ZipEntry(entryName);
        out.putNextEntry(entry);
        byte[] data = new byte[10240];
        int count;
        while ((count = origin.read(data, 0, 10240)) != -1)
        {
          int count1 = 0;
          out.write(data, 0, count1);
        }
      }
      catch (FileNotFoundException e)
      {
        e.printStackTrace();
        if (origin == null) {
          return;
        }
        try
        {
          origin.close();
        }
        catch (IOException e1)
        {
          e1.printStackTrace();
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
        if (origin == null) {
          return;
        }
        try
        {
          origin.close();
        }
        catch (IOException e1)
        {
          e1.printStackTrace();
        }
      }
      finally
      {
        if (origin != null) {
          try
          {
            origin.close();
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
        }
      }
      try
      {
        origin.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    else if (file.isDirectory())
    {
      File[] fs = file.listFiles();
      if ((fs != null) && (fs.length > 0))
      {
       /* entryName = (entry = fs).length;
        for (e = 0; e < entryName; e++)
        {
          File f = entry[e];
          compress(f, out, mainFileName);
        }*/
      }
    }
  }
  
  public static boolean unzip(File zipFile, String destPath)
  {
    boolean result = false;
    if (!zipFile.exists())
    {
      logger.info(zipFile.getName() + " doesn't exist.");
      return result;
    }
    File target = new File(destPath);
    if ((!target.exists()) && 
      (!target.mkdirs()))
    {
      logger.info("cann't create file " + target.getName());
      return result;
    }
    ZipInputStream zis = null;
    logger.info("start unzip file ...");
    try
    {
      FileInputStream fis = new FileInputStream(zipFile);
      CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
      zis = new ZipInputStream(new BufferedInputStream(checksum));
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null)
      {
        ZipEntry entry1 = null;
        byte[] data = new byte[10240];
        String entryName = entry1.getName();
        int index = 0;
        String newEntryName = destPath + "/" + entryName.substring(index);
        System.out.println(newEntryName);
        File temp = new File(newEntryName).getParentFile();
        if ((!temp.exists()) && 
          (!temp.mkdirs())) {
          throw new RuntimeException("create file " + temp.getName() + " fail");
        }
        FileOutputStream fos = new FileOutputStream(newEntryName);
        BufferedOutputStream dest = new BufferedOutputStream(fos, 10240);
        int count;
        while ((count = zis.read(data, 0, 10240)) != -1)
        {
          int count1 = 0;
          dest.write(data, 0, count1);
        }
        dest.flush();
        dest.close();
      }
      result = true;
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      if (zis != null) {
        try
        {
          zis.close();
        }
        catch (IOException e1)
        {
          e1.printStackTrace();
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      if (zis != null) {
        try
        {
          zis.close();
        }
        catch (IOException e1)
        {
          e1.printStackTrace();
        }
      }
    }
    finally
    {
      if (zis != null) {
        try
        {
          zis.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
    if (result) {
      logger.info("done.");
    } else {
      logger.info("fail.");
    }
    return result;
  }
  
  public static void main(String[] args)
    throws IOException
  {
    String zip = "E:\\projects\\project-javaweb\\aioerp-basic\\WebRoot\\backup\\555.zip";
    String floer = "E:\\projects\\project-javaweb\\aioerp-basic\\WebRoot\\backup\\555";
    zip(floer, zip);
  }
  
  public static void backUpImg(String configName, String fileName)
    throws Exception
  {
    String projectPath = IO.getWebrootPath();
    
    String sourcePath = projectPath + "backup" + File.separator + fileName;
    
    String zipPath = sourcePath + ".zip";
    
    IO.existFolder(sourcePath);
    



    IO.backUpImg(configName, fileName);
    
    zip(sourcePath, zipPath);
    
    IO.deleteFile(new File(sourcePath));
  }
  
  public static void recoverImgBefore(String configName, String fileName)
    throws Exception
  {
    String projectPath = IO.getWebrootPath();
    
    String descDir = projectPath + "backup";
    
    String zipDir = descDir + File.separator + fileName + ".zip";
    
    unzip(new File(zipDir), descDir);
  }
  
  public static void recoverImgAfter(String configName, String fileName)
    throws Exception
  {
    String unZipPath = IO.getWebrootPath() + "backup" + File.separator + fileName;
    
    IO.recoverImg(configName, unZipPath);
    
    IO.deleteFile(new File(unZipPath));
  }
}
