package com.aioerp.port;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class GetUrlProtData
{
  public static JSONObject getJsonToPortByParms(String uri, Map<String, Object> parms, String parm)
  {
    try
    {
      URL url = new URL(uri);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setUseCaches(false);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      
      Object[] it = parms.keySet().toArray();
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < it.length; i++)
      {
        if (i != 0) {
          sb.append("&");
        }
        sb.append(it[i]).append("=").append(URLEncoder.encode(parms.get(it[i]).toString(), "UTF-8"));
      }
      if (StringUtils.isNotBlank(parm)) {
        sb.append(parm);
      }
      byte[] bypes = sb.toString().getBytes();
      conn.getOutputStream().write(bypes);
      if (conn.getResponseCode() != 200) {
        return new JSONObject();
      }
      InputStream is = conn.getInputStream();
      Reader reader = new InputStreamReader(is, "UTF-8");
      BufferedReader br = new BufferedReader(reader);
      String line = br.readLine();
      return new JSONObject(line);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
}
