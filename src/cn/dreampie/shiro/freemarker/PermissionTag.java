package cn.dreampie.shiro.freemarker;

import com.aioerp.model.sys.Permission;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.util.Map;
import org.apache.shiro.subject.Subject;

public abstract class PermissionTag
  extends SecureTag
{
  String getName(Map params)
  {
    return getParam(params, "name");
  }
  
  protected void verifyParameters(Map params)
    throws TemplateModelException
  {
    String permission = getName(params);
    if ((permission == null) || (permission.length() == 0)) {
      throw new TemplateModelException("The 'name' tag attribute must be set.");
    }
  }
  
  public void render(Environment env, Map params, TemplateDirectiveBody body)
    throws IOException, TemplateException
  {
    String p = getName(params);
    
    boolean show = showTagBody(p);
    if (show) {
      renderBody(env, body);
    }
  }
  
  protected boolean isPermitted(String p)
  {
    if (!Permission.isVersion(p)) {
      return false;
    }
    return (getSubject() != null) && (getSubject().isPermitted(p));
  }
  
  protected abstract boolean showTagBody(String paramString);
}
