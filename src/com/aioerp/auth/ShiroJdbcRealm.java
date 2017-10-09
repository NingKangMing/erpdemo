package com.aioerp.auth;

import com.aioerp.model.sys.User;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

public class ShiroJdbcRealm
  extends AuthorizingRealm
{
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
    throws AuthenticationException
  {
    UsernamePasswordToken userToken = (UsernamePasswordToken)token;
    User user = null;
    String username = userToken.getUsername();
    Subject currentUser = SecurityUtils.getSubject();
    Integer whichDbId = (Integer)currentUser.getSession().getAttribute("whichDbId");
    user = (User)User.dao.findFirst(String.valueOf(whichDbId), "select * from sys_user where username=?", new Object[] { username });
    if (user != null)
    {
      SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, user.getStr("password"), getName());
      return info;
    }
    return null;
  }
  
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
  {
    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    Set<String> permissionSet = new LinkedHashSet();
    
    User user = (User)principals.fromRealm(getName()).iterator().next();
    String privsString = user.getStr("privs");
    if (StringUtils.isNotBlank(privsString)) {
      permissionSet.addAll(Arrays.asList(user.getStr("privs").split(",")));
    }
    if (StringUtils.isNotBlank(privsString))
    {
      info.addRoles(permissionSet);
      info.setStringPermissions(permissionSet);
    }
    return info;
  }
  
  public void clearCachedAuthorizationInfo(Object principal)
  {
    SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
    clearCachedAuthorizationInfo(principals);
  }
  
  public void clearAllCachedAuthorizationInfo()
  {
    Cache<Object, AuthorizationInfo> cache = getAuthorizationCache();
    if (cache != null) {
      for (Object key : cache.keys()) {
        cache.remove(key);
      }
    }
  }
}
