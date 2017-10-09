package cn.dreampie.shiro.freemarker;

import freemarker.core.Environment;
import freemarker.log.Logger;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

public class LoginUsernameTag
  extends SecureTag
{
  static final Logger log = Logger.getLogger("LoginUsernameTag");
  public static final String DEFAULT_USERNAME_PARAM = "username";
  
  public void render(Environment env, Map params, TemplateDirectiveBody body)
    throws IOException, TemplateException
  {
    String result = null;
    Session session = getSubject().getSession();
    if ((session != null) && (session.getAttribute("username") != null))
    {
      if (log.isDebugEnabled()) {
        log.debug("tempUser is exsit.");
      }
      String username = session.getAttribute("username").toString();
      if (username != null) {
        result = username;
      }
    }
    else if (log.isDebugEnabled())
    {
      log.debug("Attr is not exsit.");
    }
    if (result != null) {
      try
      {
        env.getOut().write(result);
      }
      catch (IOException ex)
      {
        throw new TemplateException("Error writing [" + result + "] to Freemarker.", ex, env);
      }
    } else {
      renderBody(env, body);
    }
  }
}
