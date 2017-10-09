package com.aioerp.model.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import java.util.Map;

public class AioerpFile
  extends BaseDbModel
{
  public static AioerpFile dao = new AioerpFile();
  public static String COMMON_SQL = "from aioerp_file";
  
  public Map<String, Object> getPageByRoot(String configName, int pageIndex, int pageSize, int tableId, int action, int billId, String ids, String listId)
  {
    StringBuffer sb = new StringBuffer(COMMON_SQL);
    sb.append(" where status=" + AioConstants.STATUS_ENABLE + " and tableId=" + tableId);
    if (!ids.equals("")) {
      sb.append(" and id in(" + ids + ")");
    } else {
      sb.append(" and id = 0");
    }
    sb.append(" order by updateTime desc");
    return aioGetPageRecord(configName, this, listId, pageIndex, pageSize, "select *", sb.toString(), new Object[0]);
  }
}
