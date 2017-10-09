package cn.dreampie.shiro.freemarker;

import freemarker.core.Environment;
import freemarker.log.Logger;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.Map;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

public class IsLoginFailureTag
  extends SecureTag
{
  private static final Logger log = Logger.getLogger("AuthenticatedTag");
  
  String getAttr(Map params)
  {
    return getParam(params, "name");
  }
  
  public void render(Environment env, Map params, TemplateDirectiveBody body)
    throws IOException, TemplateException
  {
    Subject subject = getSubject();
    Session session = getSubject().getSession();
    String attr = getAttr(params);
    if ((attr != null) && (subject != null) && (session != null) && (session.getAttribute(attr) != null))
    {
      if (log.isDebugEnabled()) {
        log.debug("Attr is exsit.");
      }
      renderBody(env, body);
    }
    else if (log.isDebugEnabled())
    {
      log.debug("Attr is not exsit.");
    }
  }
}
