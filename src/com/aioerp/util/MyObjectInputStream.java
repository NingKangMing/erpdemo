package com.aioerp.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class MyObjectInputStream
  extends ObjectInputStream
{
  public MyObjectInputStream(InputStream in)
    throws IOException
  {
    super(in);
  }
  
  protected void readStreamHeader()
    throws IOException
  {}
}
