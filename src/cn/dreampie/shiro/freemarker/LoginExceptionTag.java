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

public class LoginExceptionTag
  extends SecureTag
{
  static final Logger log = Logger.getLogger("LoginExceptionTag");
  
  String getAttr(Map params)
  {
    return getParam(params, "name");
  }
  
  public void render(Environment env, Map params, TemplateDirectiveBody body)
    throws IOException, TemplateException
  {
    String result = null;
    Subject subject = getSubject();
    Session session = getSubject().getSession();
    String attr = getAttr(params);
    String value = null;
    if ((subject != null) && (session != null) && (attr != null))
    {
      if (log.isDebugEnabled()) {
        log.debug("Attr is exsit.");
      }
      if (session.getAttribute(attr) != null)
      {
        value = String.valueOf(session.getAttribute(attr));
        if (value.equalsIgnoreCase("UnknownUserException")) {
          result = "账户验证失败或已被禁用!";
        } else if (value.equalsIgnoreCase("IncorrectCredentialsException")) {
          result = "账户验证失败或已被禁用!";
        } else if (value.equalsIgnoreCase("IncorrectCaptchaException")) {
          result = "验证码验证失败!";
        } else {
          result = "账户验证失败或已被禁用!";
        }
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
