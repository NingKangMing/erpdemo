package com.aioerp.model.sys;

import com.aioerp.model.BaseDbModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class DeliveryCompany
  extends BaseDbModel
{
  public static final DeliveryCompany dao = new DeliveryCompany();
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, Map<String, Object> parms)
  {
    StringBuffer sql = new StringBuffer("from delivery_company where 1=1");
    List<Object> paras = new ArrayList();
    query(sql, parms, paras);
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql.toString(), paras.toArray());
  }
  
  private void query(StringBuffer sql, Map<String, Object> parms, List<Object> paras)
  {
    if (parms == null) {
      return;
    }
    String param = (String)parms.get("param");
    if (StringUtils.isNotBlank(param))
    {
      sql.append(" and (name like ?");
      paras.add("%" + param + "%");
      sql.append(" or code like ?)");
      paras.add("%" + param + "%");
    }
  }
}
