package com.aioerp.common;

import com.jfinal.handler.Handler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VersionHandler
  extends Handler
{
  public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled)
  {
    request.setAttribute("version", Integer.valueOf(AioConstants.VERSION));
    request.setAttribute("aioerpVersion", AioConstants.AIOERP_VERSION);
    request.setAttribute("logVersion", AioConstants.LOG_VERSION);
    

    request.setAttribute("isFreeVersion", AioConstants.IS_FREE_VERSION);
    this.nextHandler.handle(target, request, response, isHandled);
  }
}
