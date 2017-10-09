package cn.dreampie.shiro.freemarker;

import org.apache.shiro.subject.Subject;

public class HasRoleTag
  extends RoleTag
{
  protected boolean showTagBody(String roleName)
  {
    return (getSubject() != null) && (getSubject().hasRole(roleName));
  }
}
