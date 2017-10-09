package cn.dreampie.shiro.freemarker;

public class LacksPermissionTag
  extends PermissionTag
{
  protected boolean showTagBody(String p)
  {
    return !isPermitted(p);
  }
}
