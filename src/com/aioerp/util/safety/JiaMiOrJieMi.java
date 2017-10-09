package com.aioerp.util.safety;

import com.aioerp.util.IO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class JiaMiOrJieMi
{
  public static final String en = "jiaMi";
  public static final String un = "jieMi";
  
  public static void doMethod(String type, String path, String fileName, String ext)
    throws Exception
  {
    if ((type == null) || (type.equals(""))) {
      type = "jiaMi";
    }
    String tempPath = path + "temp." + ext;
    String srcPath = path + fileName + "." + ext;
    

    SecureRandom sr = new SecureRandom();
    
    DESKeySpec dks = new DESKeySpec("abcd12345678".getBytes());
    
    SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(dks);
    
    Cipher cipher = Cipher.getInstance("DES");
    if (type.equals("jiaMi")) {
      cipher.init(1, key, sr);
    } else {
      cipher.init(2, key, sr);
    }
    FileInputStream fi2 = new FileInputStream(new File(srcPath));
    
    FileOutputStream fo = new FileOutputStream(new File(tempPath));
    
    byte[] bb = new byte[2048];
    while (fi2.read(bb) != -1)
    {
      byte[] encryptedData = cipher.doFinal(bb);
      fo.write(encryptedData);
    }
    fi2.close();
    fo.close();
    IO.deleteFile(new File(srcPath));
    IO.renameFileName(tempPath, fileName, ext);
  }
  
  public static void main(String[] args)
    throws Exception
  {
    doMethod("jiaMi", "D:\\abcd\\", "abcd", "txt");
    doMethod("jieMi", "D:\\abcd\\", "abcd", "txt");
  }
}
