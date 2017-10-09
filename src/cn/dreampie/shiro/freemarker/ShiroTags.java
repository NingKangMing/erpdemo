package cn.dreampie.shiro.freemarker;

import freemarker.template.SimpleHash;

public class ShiroTags
  extends SimpleHash
{
  public ShiroTags()
  {
    put("authenticated", new AuthenticatedTag());
    put("guest", new GuestTag());
    put("hasAnyRoles", new HasAnyRolesTag());
    put("hasPermission", new HasPermissionTag());
    put("hasRole", new HasRoleTag());
    put("lacksPermission", new LacksPermissionTag());
    put("lacksRole", new LacksRoleTag());
    put("notAuthenticated", new NotAuthenticatedTag());
    put("user", new UserTag());
    put("isLoginFailure", new IsLoginFailureTag());
    put("loginException", new LoginExceptionTag());
    put("loginUsername", new LoginUsernameTag());
  }
}
