package com.aioerp.controller.sell.sellreturn;

import com.aioerp.controller.BaseController;
import com.aioerp.model.bought.BoughtDetail;
import com.jfinal.plugin.activerecord.Model;
import java.util.List;

public class SellReturnDetailController
  extends BaseController
{
  public void checkBack()
  {
    String configName = loginConfigName();
    String ids = getPara(0);
    List<Model> detailList = BoughtDetail.dao.getList(configName, ids);
    
    renderJson(detailList);
  }
}
