package com.aioerp.model.sys.end;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Model;
import java.util.List;

public class MonthEnd
  extends BaseDbModel
{
  public static MonthEnd dao = new MonthEnd();
  
  public Model lastModel(String configName)
  {
    return findFirst(configName, "select * from sys_monthend order by endDate desc");
  }
  
  public List<Model> getList(String configName)
  {
    return find(configName, "select * from sys_monthend");
  }
  
  public long monthEndCount(String configName)
  {
    Model m = findFirst(configName, "select count(*) count from sys_monthend");
    if (m == null) {
      return 0L;
    }
    return m.getLong("count").longValue();
  }
}
