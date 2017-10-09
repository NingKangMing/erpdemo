package com.aioerp.model.fz;

import com.aioerp.model.BaseDbModel;
import java.util.ArrayList;
import java.util.List;

public class MinSellPrice
  extends BaseDbModel
{
  public static final MinSellPrice dao = new MinSellPrice();
  public static final String TABLE_NAME = "fz_minsell_price";
  
  public MinSellPrice getMinObj(String configName, Integer storageId, int productId, int selectUnitId)
  {
    StringBuffer sql = new StringBuffer(" select * from fz_minsell_price where 1=1");
    List<Object> params = new ArrayList();
    sql.append(" and productId = ?");
    params.add(Integer.valueOf(productId));
    
    sql.append(" and selectUnitId = ?");
    params.add(Integer.valueOf(selectUnitId));
    





    sql.append(" order by minSellPrice");
    return (MinSellPrice)dao.findFirst(configName, sql.toString(), params.toArray());
  }
}
