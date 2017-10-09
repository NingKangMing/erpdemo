package com.aioerp.model.sys;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Model;
import java.util.List;

public class BillSort
  extends BaseDbModel
{
  public static final BillSort dao = new BillSort();
  public String tableName = "sys_billsort";
  
  public List<Model> getList(String configName)
  {
    return dao.find(configName, "SELECT * FROM " + this.tableName);
  }
}
