package cn.dreampie.shiro.freemarker;

public class HasPermissionTag
  extends PermissionTag
{
  protected boolean showTagBody(String p)
  {
    return isPermitted(p);
  }
}
