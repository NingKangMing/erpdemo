package com.aioerp.controller;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class LoginValidator
  extends Validator
{
  protected void validate(Controller c)
  {
    validateEmail("email", "msg", "错误的邮箱地址");
    validateRequired("password", "msg", "密码不能为空");
  }
  
  protected void handleError(Controller c)
  {
    c.keepPara(new String[] { "email" });
    c.render("/user/login.html");
  }
}
