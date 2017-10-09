package com.aioerp.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.FreeMarkerRender;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.template.Configuration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class LoginInterceptor
  implements Interceptor
{
  public void intercept(ActionInvocation ai)
  {
    Controller controller = ai.getController();
    Record user = (Record)controller.getSessionAttr("user");
    
    Map<String, String> actionKeyMap = new HashMap();
    actionKeyMap.put("/aioerpom", "OM订单系统接口");
    actionKeyMap.put("/user/login", "普通用户登录");
    actionKeyMap.put("/user/logout", "注销");
    actionKeyMap.put("/supAdmin/user/checkWhickDb", "校验用户名密码");
    actionKeyMap.put("/supAdmin/user/toWhickDbList", "账套列表");
    actionKeyMap.put("/supAdmin/user/supAdminLogin", "超级管理员登录");
    actionKeyMap.put("/upload/basePrdUploadImg", "文件上传");
    actionKeyMap.put("/upload/orderFujianUpLoad", "单据附件");
    actionKeyMap.put("/user/souPing", "锁屏输入密码");
    

    String actionKey = ai.getActionKey();
    if ((actionKeyMap.containsKey(ai.getActionKey())) || (user != null) || (actionKey.indexOf("uploadImg") > 0))
    {
      controller.setAttr("session", new HttpSessionHashModel(controller.getSession(), FreeMarkerRender.getConfiguration().getObjectWrapper()));
      ai.invoke();
    }
    else if ("XMLHttpRequest".equals(controller.getRequest().getHeader("x-requested-with")))
    {
      Map<String, String> result = new HashMap();
      result.put("statusCode", "301");
      result.put("message", "会话超时,请重新登录");
      controller.renderJson(result);
    }
    else
    {
      controller.redirect("/user/login");
    }
  }
}
