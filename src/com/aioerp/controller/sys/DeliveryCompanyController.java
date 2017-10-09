package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.sys.DeliveryCompany;
import java.util.HashMap;
import java.util.Map;

public class DeliveryCompanyController
  extends BaseController
{
  public void option()
  {
    String configName = loginConfigName();
    String param = getPara("param");
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int pageSize = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    Map<String, Object> parms = new HashMap();
    parms.put("param", param);
    Map<String, Object> pageMap = DeliveryCompany.dao.getPage(configName, pageNum, pageSize, "deliveryCompanypageContent", parms);
    setAttr("pageMap", pageMap);
    setAttr("param", param);
    render("option.html");
  }
}
