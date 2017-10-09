package com.aioerp.auth;

import com.aioerp.common.AioConstants;
import java.util.Map;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;

public class MySessionListener1
  implements SessionListener
{
  public void onStart(Session session)
  {
    AioConstants.PROJECT_SESSION_MAP.put(session.getId(), "0");
  }
  
  public void onExpiration(Session session)
  {
    AioConstants.PROJECT_SESSION_MAP.remove(String.valueOf(session.getId()));
  }
  
  public void onStop(Session session)
  {
    AioConstants.PROJECT_SESSION_MAP.remove(String.valueOf(session.getId()));
  }
}
