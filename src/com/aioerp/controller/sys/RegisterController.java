package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.util.Dog;

public class RegisterController
  extends BaseController
{
  public void index()
  {
    if (isPost())
    {
      String activeCode = getPara("activecode").trim();
      try
      {
        if (Dog.register(activeCode))
        {
          setAttr("statusCode", "200");
          setAttr("message", "注册成功");
          setAttr("callbackType", "closeCurrent");
          renderJson();
          return;
        }
      }
      catch (Error e)
      {
        setAttr("statusCode", "300");
        setAttr("message", "此系统不支持加密狗注册!");
        renderJson();
        return;
      }
      setAttr("statusCode", "300");
      setAttr("message", "注册失败，注册码不正确!");
      renderJson();
    }
    else
    {
      try
      {
        if ((Dog.reload()) && (!"".equals(Dog.getID())) && (Dog.getVersion() == AioConstants.VERSION))
        {
          if (Dog.isRegister())
          {
            setAttr("statusCode", "300");
            setAttr("message", "产品已注册!");
            renderJson();
          }
        }
        else
        {
          setAttr("statusCode", "300");
          setAttr("message", "请插入加密狗注册!");
          renderJson();
          return;
        }
      }
      catch (Error e)
      {
        setAttr("statusCode", "300");
        setAttr("message", "系统不支持加密狗注册!");
        renderJson();
        return;
      }
      render("page_register.html");
    }
  }
}
