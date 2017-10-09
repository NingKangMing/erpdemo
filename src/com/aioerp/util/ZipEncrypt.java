package com.aioerp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.security.Key;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ZipEncrypt
{
  private void directoryZip(ZipOutputStream out, File f, String base)
    throws Exception
  {
    if (f.isDirectory())
    {
      File[] fl = f.listFiles();
      
      out.putNextEntry(new ZipEntry(base + "/"));
      if (base.length() == 0) {
        base = "";
      } else {
        base = base + "/";
      }
      for (int i = 0; i < fl.length; i++) {
        directoryZip(out, fl[i], base + fl[i].getName());
      }
    }
    else
    {
      out.putNextEntry(new ZipEntry(base));
      FileInputStream in = new FileInputStream(f);
      byte[] bb = new byte[2048];
      int aa = 0;
      while ((aa = in.read(bb)) != -1) {
        out.write(bb, 0, aa);
      }
      in.close();
    }
  }
  
  private void fileZip(ZipOutputStream zos, File file)
    throws Exception
  {
    if (file.isFile())
    {
      zos.putNextEntry(new ZipEntry(file.getName()));
      FileInputStream fis = new FileInputStream(file);
      byte[] bb = new byte[2048];
      int aa = 0;
      while ((aa = fis.read(bb)) != -1) {
        zos.write(bb, 0, aa);
      }
      fis.close();
      System.out.println(file.getName());
    }
    else
    {
      directoryZip(zos, file, "");
    }
  }
  
  private void fileUnZip(ZipInputStream zis, File file)
    throws Exception
  {
    ZipEntry zip = zis.getNextEntry();
    if (zip == null) {
      return;
    }
    String name = zip.getName();
    File f = new File(file.getAbsolutePath() + "/" + name);
    if (zip.isDirectory())
    {
      f.mkdirs();
      fileUnZip(zis, file);
    }
    else
    {
      f.createNewFile();
      FileOutputStream fos = new FileOutputStream(f);
      byte[] b = new byte[2048];
      int aa = 0;
      while ((aa = zis.read(b)) != -1) {
        fos.write(b, 0, aa);
      }
      fos.close();
      fileUnZip(zis, file);
    }
  }
  
  private void zip(String directory, String zipFile)
  {
    try
    {
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
      fileZip(zos, new File(directory));
      zos.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  private void unZip(String directory, String zipFile)
  {
    try
    {
      ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
      File f = new File(directory);
      f.mkdirs();
      fileUnZip(zis, f);
      zis.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  private static Key getKey(String keyPath)
    throws Exception
  {
    FileInputStream fis = new FileInputStream(keyPath);
    byte[] b = new byte[16];
    fis.read(b);
    SecretKeySpec dks = new SecretKeySpec(b, "AES");
    fis.close();
    return dks;
  }
  
  private void encrypt(String srcFile, String destFile, Key privateKey)
    throws Exception
  {
    SecureRandom sr = new SecureRandom();
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    IvParameterSpec spec = new IvParameterSpec(privateKey.getEncoded());
    cipher.init(1, privateKey, spec, sr);
    FileInputStream fis = new FileInputStream(srcFile);
    FileOutputStream fos = new FileOutputStream(destFile);
    byte[] b = new byte[2048];
    while (fis.read(b) != -1) {
      fos.write(cipher.doFinal(b));
    }
    fos.close();
    fis.close();
  }
  
  private void decrypt(String srcFile, String destFile, Key privateKey)
    throws Exception
  {
    SecureRandom sr = new SecureRandom();
    Cipher ciphers = Cipher.getInstance("AES/CBC/PKCS5Padding");
    IvParameterSpec spec = new IvParameterSpec(privateKey.getEncoded());
    ciphers.init(2, privateKey, spec, sr);
    FileInputStream fis = new FileInputStream(srcFile);
    FileOutputStream fos = new FileOutputStream(destFile);
    byte[] b = new byte[2064];
    while (fis.read(b) != -1) {
      fos.write(ciphers.doFinal(b));
    }
    fos.close();
    fis.close();
  }
  
  public void encryptZip(String srcFile, String destfile, String keyfile)
    throws Exception
  {
    SecureRandom sr = new SecureRandom();
    KeyGenerator kg = KeyGenerator.getInstance("AES");
    kg.init(128, sr);
    SecretKey key = kg.generateKey();
    File f = new File(keyfile);
    if (!f.getParentFile().exists()) {
      f.getParentFile().mkdirs();
    }
    f.createNewFile();
    FileOutputStream fos = new FileOutputStream(f);
    fos.write(key.getEncoded());
    File temp = new File(UUID.randomUUID().toString() + ".zip");
    temp.deleteOnExit();
    
    zip(srcFile, temp.getAbsolutePath());
    
    encrypt(temp.getAbsolutePath(), destfile, key);
    temp.delete();
  }
  
  public void decryptUnzip(String srcfile, String destfile, String keyfile)
    throws Exception
  {
    File temp = new File(UUID.randomUUID().toString() + ".zip");
    temp.deleteOnExit();
    decrypt(srcfile, temp.getAbsolutePath(), getKey(keyfile));
    
    unZip(destfile, temp.getAbsolutePath());
    temp.delete();
  }
  
  public static void main(String[] args)
    throws Exception
  {
    String projectPath = "E:\\projects\\project-javaweb\\aioerp-basic\\WebRoot\\";
    
    String fileName = "abc";
    
    String keyPath = projectPath + "/public.key";
    
    String sourcePath = projectPath + "backup" + File.separator + fileName;
    
    String destPath = sourcePath + ".zip";
    

    new ZipEncrypt().encryptZip(sourcePath, destPath, keyPath);
    
    new ZipEncrypt().decryptUnzip(destPath, sourcePath, keyPath);
  }
}
