package com.aioerp.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.config.Constants;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import com.jfinal.core.JFinal;
import com.jfinal.i18n.I18N;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.ResourceBundleModel;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;

public class ResourceBundleInterceptor
  implements Interceptor
{
  public void intercept(ActionInvocation ai)
  {
    String local_i18n = ai.getController().getCookie("__I18N_LOCALE__");
    
    ResourceBundle RESOURCE_BUNDLE = null;
    if ((local_i18n == null) || (local_i18n == ""))
    {
      HttpServletRequest request = ai.getController().getRequest();
      
      Locale locale = request != null ? request.getLocale() : 
        Locale.getDefault();
      if (locale != null) {
        RESOURCE_BUNDLE = ResourceBundle.getBundle(JFinal.me()
          .getConstants().getI18nResourceBaseName(), locale);
      } else {
        RESOURCE_BUNDLE = ResourceBundle.getBundle(JFinal.me()
          .getConstants().getI18nResourceBaseName(), 
          I18N.getDefaultLocale());
      }
    }
    else
    {
      RESOURCE_BUNDLE = ResourceBundle.getBundle(JFinal.me()
        .getConstants().getI18nResourceBaseName(), 
        I18N.localeFromString(local_i18n));
    }
    ResourceBundleModel rsbm = new ResourceBundleModel(RESOURCE_BUNDLE, 
      new BeansWrapper());
    
    ai.getController().setAttr("bundle", rsbm);
    
    ai.invoke();
  }
}
