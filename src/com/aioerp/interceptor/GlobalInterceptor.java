package com.aioerp.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

public class GlobalInterceptor
  implements Interceptor
{
  public void intercept(ActionInvocation ai)
  {
    Controller c = ai.getController();
    
    String sysPantKey = "/common/verify/sysPant";
    if (!sysPantKey.equals(ai.getActionKey())) {
      c.setSessionAttr("lastOperTime", new Date());
    }
    String tmp = c.getCookie("__I18N_LOCALE__");
    String i18n = c.getRequest().getLocale().toString();
    if (!i18n.equals(tmp)) {
      ai.getController().setCookie("__I18N_LOCALE__", i18n, 
        999999999);
    }
    String userAgent = c.getRequest().getHeader("User-Agent").toLowerCase();
    if ((userAgent.contains("chrome")) || (userAgent.contains("firefox"))) {
      c.setAttr("isFF", Boolean.valueOf(true));
    } else {
      c.setAttr("isFF", Boolean.valueOf(false));
    }
    ai.invoke();
  }
}
