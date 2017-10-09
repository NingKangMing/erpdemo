package com.aioerp.util.safety;

import java.io.File;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Key
{
  public static final String KEY = "abcd12345678";
  
  public void createKey(String keyName)
    throws Exception
  {
    SecureRandom sr = new SecureRandom();
    
    KeyGenerator kg = KeyGenerator.getInstance("DES");
    
    kg.init(sr);
    
    SecretKey key = kg.generateKey();
    
    byte[] rawKeyData = key.getEncoded();
    
    FileOutputStream fo = new FileOutputStream(new File(keyName));
    fo.write(rawKeyData);
  }
  
  public static void main(String[] args)
  {
    try
    {
      Key key = new Key();
      key.createKey("d://key.txt");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
