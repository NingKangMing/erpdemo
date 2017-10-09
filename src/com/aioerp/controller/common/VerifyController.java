package com.aioerp.controller.common;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.db.reports.BillHistory;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.util.DateUtils;
import com.jfinal.plugin.activerecord.Model;
import java.util.Date;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

public class VerifyController
  extends BaseController
{
  public void permittedVerify()
  {
    String key = getPara("key");
    boolean hashasPermitted = hasPermitted(key);
    if (!hashasPermitted)
    {
      setAttr("message", "该用户没有此项权限!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    renderJson();
  }
  
  public void permittedVerifyBillLook()
  {
    int id = getParaToInt(2, Integer.valueOf(0)).intValue();
    String key = "1111-s";
    boolean hashasPermitted = hasPermitted(key);
    if (!hashasPermitted)
    {
      Integer userId = Integer.valueOf(loginUserId());
      Model bill = BillHistory.dao.getModelByUserId(loginConfigName(), id, userId);
      if (bill == null)
      {
        setAttr("message", "该用户无查看他人单据的权限!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      renderJson();
      return;
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    renderJson();
  }
  
  public void permittedVerifyDraftLook()
  {
    int id = getParaToInt(2, Integer.valueOf(0)).intValue();
    String key = "1107-s";
    boolean hashasPermitted = hasPermitted(key);
    if (!hashasPermitted)
    {
      Integer userId = Integer.valueOf(loginUserId());
      Model draft = BusinessDraft.dao.getRecrodById(loginConfigName(), Integer.valueOf(id));
      Integer draftUserId = draft.getInt("userId");
      if (userId != draftUserId)
      {
        setAttr("message", "该用户无查看他人草稿的权限!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      renderJson();
      return;
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    renderJson();
  }
  
  public void sysPant()
  {
    Subject subject = SecurityUtils.getSubject();
    Session session = subject.getSession();
    Boolean isSP = Boolean.valueOf(false);
    
    String loginConfigId = session.getAttribute("loginConfigId").toString();
    Date lastOperTime = (Date)session.getAttribute("lastOperTime");
    
    String atuoSPTime = AioerpSys.dao.getValue1(loginConfigId, "autoSPTime");
    if ((atuoSPTime == null) || (atuoSPTime.equals("")))
    {
      atuoSPTime = String.valueOf(1000);
      AioerpSys.dao.sysSaveOrUpdate(loginConfigId, "autoSPTime", atuoSPTime, "自动锁屏时间(分钟)");
    }
    int autoSP = Integer.parseInt(atuoSPTime);
    Date n = new Date();
    
    Date temp = DateUtils.addMinute(lastOperTime, autoSP);
    if (n.after(temp)) {
      isSP = Boolean.valueOf(true);
    }
    setAttr("isSP", isSP);
    renderJson();
  }
}
