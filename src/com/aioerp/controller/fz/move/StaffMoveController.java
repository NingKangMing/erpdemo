package com.aioerp.controller.fz.move;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Staff;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StaffMoveController
  extends BaseController
{
  public void index()
  {
    Integer supId = getParaToInt(0, Integer.valueOf(0));
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    setAttr("supId", supId);
    setAttr("pageMap", Staff.dao.getChildPage(loginConfigName(), pageNum, numPerPage, 0, "", "", "rank", "asc", null, ALL_PRIVS));
    render("page.html");
  }
  
  public void sourceList()
  {
    Integer supId = getParaToInt(0, Integer.valueOf(0));
    String configName = loginConfigName();
    String listID = "sourceList";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (upObjectId > 0)
    {
      setAttr("pageMap", Staff.dao.getSupPage(configName, pageNum, numPerPage, supId.intValue(), upObjectId, listID, null, "rank", "asc", ALL_PRIVS));
      setAttr("objectId", Integer.valueOf(upObjectId));
    }
    else
    {
      setAttr("pageMap", Staff.dao.getChildPage(configName, pageNum, numPerPage, 0, "", "", "rank", "asc", null, ALL_PRIVS));
    }
    int pSupId = 0;
    if (supId.intValue() > 0) {
      pSupId = Staff.dao.getPsupId(configName, supId.intValue());
    }
    setAttr("supId", supId);
    setAttr("pSupId", Integer.valueOf(pSupId));
    
    String selectObjs = getPara("selectObjs");
    setAttr("selectObjs", selectObjs);
    
    List<String> idArray = new ArrayList();
    String[] ss = StringUtil.strCovIds(selectObjs);
    if ((ss != null) && (ss.length > 0)) {
      idArray.addAll(Arrays.asList(ss));
    }
    setAttr("idArray", idArray);
    
    render("source.html");
  }
  
  public void aimList()
  {
    String configName = loginConfigName();
    Integer supId = getParaToInt(0, Integer.valueOf(0));
    String listID = "aimList";
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (upObjectId > 0)
    {
      setAttr("pageMap", Staff.dao.getSupPage(configName, pageNum, numPerPage, supId.intValue(), upObjectId, listID, null, "rank", "asc", ALL_PRIVS));
      setAttr("objectId", Integer.valueOf(upObjectId));
    }
    else
    {
      setAttr("pageMap", Staff.dao.getChildPage(configName, pageNum, numPerPage, 0, "", "", "rank", "asc", null, ALL_PRIVS));
    }
    int pSupId = 0;
    if (supId.intValue() > 0) {
      pSupId = Staff.dao.getPsupId(configName, supId.intValue());
    }
    setAttr("supId", supId);
    setAttr("pSupId", Integer.valueOf(pSupId));
    
    render("aim.html");
  }
  
  @Before({Tx.class})
  public void move()
  {
    Integer moveNode = getParaToInt("moveNode");
    Integer aimId = getParaToInt("aimId");
    String selectObjs = getPara("selectObjs");
    if (StringUtil.isNull(selectObjs))
    {
      setAttr("msg", "请选择需要搬移的源节点数据！");
      renderJson();
      return;
    }
    if ((aimId == null) || (moveNode == null))
    {
      setAttr("msg", "参数异常请联系管理员！");
      renderJson();
      return;
    }
    String configName = loginConfigName();
    Staff aimObj = (Staff)Staff.dao.findById(configName, aimId);
    int supId = aimObj.getInt("supId").intValue();
    int node = aimObj.getInt("node").intValue();
    String pids = aimObj.getStr("pids");
    if (moveNode.intValue() == -1) {
      supId = aimId.intValue();
    }
    String[] ss = StringUtil.strCovIds(selectObjs);
    for (int i = 0; i < ss.length; i++)
    {
      Integer id = Integer.valueOf(Integer.parseInt(ss[i]));
      int sSupId = Staff.dao.getPsupId(configName, id.intValue());
      if (supId == sSupId)
      {
        setAttr("msg", "此种搬移没有意义，不会有任何变化！");
        renderJson();
        return;
      }
      if (pids.indexOf("{" + id + "}") > -1)
      {
        setAttr("msg", "不能将基本信息向该基本信息或其下级搬移！");
        renderJson();
        return;
      }
    }
    if ((node == 1) && (moveNode.intValue() == -1))
    {
      boolean verify = Staff.dao.verify(configName, "staffId", aimId);
      if (verify)
      {
        setAttr("msg", "目标节点已经使用过不能向下搬移数据!");
        renderJson();
        return;
      }
    }
    try
    {
      if (moveNode.intValue() == 1)
      {
        pids = pids.substring(0, pids.lastIndexOf("{"));
      }
      else
      {
        aimObj.set("node", Integer.valueOf(2));
        aimObj.update(configName);
      }
      for (int i = 0; i < ss.length; i++)
      {
        int id = Integer.parseInt(ss[i]);
        Staff p = (Staff)Staff.dao.findById(configName, Integer.valueOf(id));
        String pPids = p.getStr("pids");
        p.set("supId", Integer.valueOf(supId));
        p.set("pids", pids + "{" + id + "}");
        p.update(configName);
        List<Model> ms = Staff.dao.getListByPids(configName, pPids + "{");
        for (Model m : ms)
        {
          Staff sp = (Staff)m;
          String spids = m.getStr("pids").replace(pPids, p.getStr("pids"));
          sp.set("pids", spids);
          sp.update(configName);
        }
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("msg", "数据搬移成功！");
      renderJson();
    }
    catch (Exception e)
    {
      setAttr("msg", "系统异常！");
      renderJson();
      return;
    }
  }
}
