package com.aioerp.auth;

import com.aioerp.common.AioConstants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;

public class MySessionDAO
  extends CachingSessionDAO
{
  protected void doDelete(Session session) {}
  
  protected void doUpdate(Session session)
  {
    if (AioConstants.CONFIGSESSIONNAMELIST.size() > 0)
    {
      for (int i = 0; i < AioConstants.CONFIGSESSIONNAMELIST.size(); i++) {
        for (Map.Entry<Serializable, String> entry : AioConstants.PROJECT_SESSION_MAP.entrySet()) {
          if (((String)AioConstants.PROJECT_SESSION_MAP.get(entry.getKey())).equals(AioConstants.CONFIGSESSIONNAMELIST.get(i))) {
            AioConstants.PROJECT_SESSION_MAP.put((Serializable)entry.getKey(), "-1");
          }
        }
      }
      AioConstants.CONFIGSESSIONNAMELIST = new ArrayList();
    }
    if ((AioConstants.PROJECT_SESSION_MAP.containsKey(session.getId())) && (((String)AioConstants.PROJECT_SESSION_MAP.get(session.getId())).equals("-1")))
    {
      session.setAttribute("user", null);
      AioConstants.PROJECT_SESSION_MAP.put(session.getId(), "0");
      
      return;
    }
    if (((session instanceof ValidatingSession)) && (!((ValidatingSession)session).isValid()))
    {
      if (AioConstants.PROJECT_SESSION_MAP.containsKey(session.getId())) {
        AioConstants.PROJECT_SESSION_MAP.remove(session.getId());
      }
      return;
    }
  }
  
  protected Serializable doCreate(Session session)
  {
    Serializable sessionId = generateSessionId(session);
    assignSessionId(session, sessionId);
    








    return session.getId();
  }
  
  protected Session doReadSession(Serializable sessionId)
  {
    return null;
  }
}
