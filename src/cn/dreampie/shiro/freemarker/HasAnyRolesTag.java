package cn.dreampie.shiro.freemarker;

import com.aioerp.model.sys.User;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;

public class HasAnyRolesTag
  extends RoleTag
{
  private static final String ROLE_NAMES_DELIMETER = ",";
  
  protected boolean showTagBody(String roleNames)
  {
    boolean hasAnyRole = false;
    Subject subject = getSubject();
    
    User user = (User)subject.getPrincipal();
    String privStr = "";
    if (user != null) {
      privStr = user.getStr("privs");
    }
    Set<String> permissionSet = new LinkedHashSet();
    if (StringUtils.isNotBlank(privStr)) {
      permissionSet.addAll(Arrays.asList(privStr.split(",")));
    }
    if (subject != null) {
      for (String role : roleNames.split(","))
      {
        if ((subject.hasRole(role.trim())) || (subject.isPermitted(role.trim())))
        {
          hasAnyRole = true;
          break;
        }
        if (role.indexOf("*") != -1)
        {
          String p = role.substring(0, role.indexOf("*"));
          for (String permission : permissionSet) {
            if (permission.startsWith(p))
            {
              hasAnyRole = true;
              break;
            }
          }
        }
      }
    }
    return hasAnyRole;
  }
}
