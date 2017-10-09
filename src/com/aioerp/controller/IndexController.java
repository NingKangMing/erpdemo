package com.aioerp.controller;

import com.jfinal.core.Controller;

public class IndexController
  extends Controller
{
  public void index()
  {
    render("index.html");
  }
  
  public void home()
  {
    render("layout/main.html");
  }
  
  public void printDialog()
  {
    render("common/print/printDialog.html");
  }
  
  public void leaveMsg()
  {
    render("/template/leaveMsg.html");
  }
  
  public void regist()
  {
    render("/template/regist.html");
  }
  
  public void toLogin()
  {
    render("/template/login.html");
  }
  
  public void test()
  {
    render("/template/test.html");
  }
}
