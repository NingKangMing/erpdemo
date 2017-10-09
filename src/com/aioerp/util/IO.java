package com.aioerp.util;

import com.aioerp.model.sys.AioerpFile;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Model;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.List;
import org.apache.commons.fileupload.RequestContext;

public class IO
{
  public static void backUpImg(String configName, String dirPath)
    throws IOException
  {
    String FolderPath = getWebrootPath() + "upLoadImg" + File.separator;
    String targetPath = getWebrootPath() + "backup" + File.separator + dirPath + File.separator + "upLoadImg" + File.separator;
    existFolder(targetPath + "base" + File.separator + configName + File.separator + "product" + File.separator);
    existFolder(targetPath + "base" + File.separator + configName + File.separator + "staff" + File.separator);
    existFolder(targetPath + "base" + File.separator + configName + File.separator + "order" + File.separator);
    
    List<Model> attachments = AioerpFile.dao.find(configName, "select * from aioerp_file");
    File imgFile = null;
    for (Model attachment : attachments)
    {
      String savePath = attachment.getStr("savePath");
      imgFile = new File(FolderPath + savePath);
      if (imgFile.exists())
      {
        File dirFile = new File(targetPath + savePath);
        fileChannelCopy(imgFile, dirFile);
      }
    }
  }
  
  public static void recoverImg(String configName, String sourcePath)
  {
    String backupPath = sourcePath + File.separator + "upLoadImg";
    
    String targetPath = getWebrootPath() + "upLoadImg" + File.separator + "base" + File.separator + configName + File.separator;
    File targetFile = new File(targetPath);
    if (targetFile.exists()) {
      deleteFile(targetFile);
    }
    existFolder(targetPath + "product" + File.separator);
    existFolder(targetPath + "staff" + File.separator);
    existFolder(targetPath + "order" + File.separator);
    File backupDir = new File(backupPath);
    try
    {
      recoverImgToSys(backupDir, targetPath);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  private static FileFilter fileFilter = new FileFilter()
  {
    public boolean accept(File file)
    {
      if (!file.getName().equals(".svn")) {
        return true;
      }
      return false;
    }
  };
  
  public static void recoverImgToSys(File dir, String targetPath)
    throws IOException
  {
    File[] files = dir.listFiles(fileFilter);
    if (files != null) {
      for (File file : files)
      {
        if (file.isFile())
        {
          String imgPath = file.getParent();
          
          File targetFile = null;
          if (imgPath.endsWith("product")) {
            targetFile = new File(targetPath + "product" + File.separator + file.getName());
          } else if (imgPath.endsWith("staff")) {
            targetFile = new File(targetPath + "staff" + File.separator + file.getName());
          } else {
            targetFile = new File(targetPath + "order" + File.separator + file.getName());
          }
          fileChannelCopy(file, targetFile);
        }
        if (file.isDirectory()) {
          try
          {
            recoverImgToSys(file, targetPath);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
      }
    }
  }
  
  public static void existFolder(String pathFolder)
  {
    File file = new File(pathFolder);
    if ((!file.exists()) && (!file.isDirectory())) {
      file.mkdirs();
    }
  }
  
  public static boolean deleteFile(File f)
  {
    try
    {
      if (f.isFile())
      {
        f.delete();
        return true;
      }
      File[] fs = f.listFiles();
      for (File file : fs) {
        deleteFile(file);
      }
      f.delete();
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  public static void renameFileName(String srcFilePath, String fileName, String ext)
  {
    File file = new File(srcFilePath);
    String c = file.getParent();
    File mm = new File(c + File.separator + fileName + "." + ext);
    if (file.renameTo(mm)) {
      System.out.println("修改成功");
    } else {
      System.out.println("修改失败");
    }
  }
  
  public static final String getWebrootPath()
  {
    String root = RequestContext.class.getResource("/").getFile();
    try
    {
      if (root.endsWith(".svn/")) {
        root = new File(root).getParentFile().getParentFile().getParentFile().getCanonicalPath();
      } else {
        root = new File(root).getParentFile().getParentFile().getCanonicalPath();
      }
      root = URLDecoder.decode(root, "utf-8");
      root = root + File.separator;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return root;
  }
  
  public static final String getMySQLPath()
  {
    String projectPath = getWebrootPath();
    int index = projectPath.toLowerCase().indexOf("tomcat");
    String mysqlBinPath = "";
    if (index != -1) {
      mysqlBinPath = projectPath.substring(0, index) + "MySQL" + File.separator + "bin" + File.separator;
    } else {
      return "";
    }
    File mysqlFile = new File(mysqlBinPath);
    return mysqlFile.getAbsolutePath();
  }
  
  public static void fileChannelCopy(File oldFile, File newFile)
  {
    FileInputStream fi = null;
    FileOutputStream fo = null;
    
    FileChannel in = null;
    FileChannel out = null;
    try
    {
      fi = new FileInputStream(oldFile);
      fo = new FileOutputStream(newFile);
      in = fi.getChannel();
      out = fo.getChannel();
      in.transferTo(0L, in.size(), out);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      try
      {
        fi.close();
        in.close();
        fo.close();
        out.close();
      }
      catch (IOException e1)
      {
        e1.printStackTrace();
      }
    }
    finally
    {
      try
      {
        fi.close();
        in.close();
        fo.close();
        out.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }
  
  public static void replaceFileContent(String oldFilePath, String newFilePath, String oldStr, String newStr)
    throws Exception
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(oldFilePath), "utf-8"));
    
    StringBuilder sb = new StringBuilder();
    String s;
    while ((s = br.readLine()) != null)
    {
     
      for (int i = 0; i < s.length(); i++) {
        if (s.contains(oldStr)) {
          s = s.replace(oldStr, newStr);
        }
      }
      sb.append(s);
      sb.append("\r");
    }
    br.close();
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFilePath), "utf-8"));
    bw.write(sb.toString());
    bw.flush();
    
    bw.close();
  }
  
  public static boolean getLogHistoryBySameDate(String oldFilePath, String newFilePath, int startDate, int endDate)
    throws Exception
  {
    boolean flag = true;
    BufferedReader br = null;
    BufferedWriter bw = null;
    try
    {
      br = new BufferedReader(new InputStreamReader(new FileInputStream(oldFilePath), "utf-8"));
      
      int isStart = -1;
      int tempDateInt = 0;
      StringBuilder sb = new StringBuilder();
      String s;
      while ((s = br.readLine()) != null)
      {
       
        int firstFIndex = s.indexOf("##", 1);
        int secoedFIndex = s.indexOf("##", firstFIndex + 1);
        
        int firstLIndex = s.indexOf("#@", 1);
        int secoedLIndex = s.indexOf("@#", firstLIndex + 1);
        if (secoedFIndex - firstFIndex == 10)
        {
          if (isStart == 1)
          {
            System.out.println("在文件" + oldFilePath + "【" + s + "】有结束标记出错");
            throw new Exception();
          }
          String tempDateStr = s.substring(firstFIndex + 2, secoedFIndex);
          if (!tempDateStr.equals("yyyymmdd"))
          {
            tempDateInt = Integer.valueOf(tempDateStr).intValue();
            isStart = 1;
          }
        }
        else if (secoedLIndex - firstLIndex == 10)
        {
          if (isStart == 0)
          {
            System.out.println("在文件" + oldFilePath + "【" + s + "】有开始标记出错");
            throw new Exception();
          }
          String tempDateStr = s.substring(firstLIndex + 2, secoedLIndex);
          if (!tempDateStr.equals("yyyymmdd"))
          {
            if (Integer.valueOf(tempDateStr).intValue() != tempDateInt) {
              System.out.println("在文件" + oldFilePath + "【" + s + "】开始与结束日期写法不同");
            }
            isStart = 0;
          }
        }
        else
        {
          if (((firstFIndex != -1) && (secoedFIndex != -1)) || ((firstLIndex != -1) && (secoedLIndex != -1))) {
            System.out.println("在文件" + oldFilePath + "【" + s + "】标记yyyymmdd不是8位");
          }
          if ((isStart == 1) && (tempDateInt > startDate) && (tempDateInt <= endDate))
          {
            sb.append(s);
            sb.append("\r");
          }
        }
      }
      bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFilePath), "utf-8"));
      bw.write(sb.toString());
    }
    catch (Exception e)
    {
      flag = false;
    }
    finally
    {
      br.close();
      bw.flush();
      bw.close();
    }
    return flag;
  }
  
  public static void main(String[] args)
    throws Exception
  {
    int startDate = 19900101;
    int endDate = 29000101;
    

    String mainUpdateSqlPath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "mainSql" + File.separator + "mianDatabase_sql_log.sql";
    String updateMainDataPathTemp = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "mainSql" + File.separator + "updateLog.sql";
    getLogHistoryBySameDate(mainUpdateSqlPath, updateMainDataPathTemp, startDate, endDate);
    
    deleteFile(new File(updateMainDataPathTemp));
    


    String childUpdateTableSqlPath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "updateTable_log.sql";
    String updateChildTablePathTemp = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "updateTableTmp.sql";
    getLogHistoryBySameDate(childUpdateTableSqlPath, updateChildTablePathTemp, startDate, endDate);
    
    deleteFile(new File(updateChildTablePathTemp));
    


    String childUpdateDataSqlPath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "updateData_log.sql";
    String updateChildDataPathTemp = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "childSql" + File.separator + "updateDataTmp.sql";
    getLogHistoryBySameDate(childUpdateDataSqlPath, updateChildDataPathTemp, startDate, endDate);
    
    deleteFile(new File(updateChildDataPathTemp));
  }
}
