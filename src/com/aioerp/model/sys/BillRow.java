package com.aioerp.model.sys;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Model;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class BillRow
  extends BaseDbModel
{
  public static final BillRow dao = new BillRow();
  private static final String TABLE = "sys_bill_row";
  
  public List<Model> getListByBillId(String configName, int billId)
  {
    return dao.find(configName, "select * from  sys_bill_row where billId = " + billId + " order by rank");
  }
  
  public List<Model> getListByBillId(String configName, int billId, int status)
  {
    return dao.find(configName, "select * from  sys_bill_row where billId = " + billId + " and status = " + status + " order by rank");
  }
  
  public List<Model> getListByBillLinkage(String configName, int billId, String linkage)
  {
    return dao.find(configName, "select * from  sys_bill_row where billId = " + billId + " and linkage = " + linkage);
  }
  
  public boolean showRow(String configName, int billId, String code)
  {
    String sql = "select * from  sys_bill_row where billId = " + billId + " and status = 2";
    if (StringUtils.isNotBlank(code)) {
      sql = sql + " and code ='" + code + "'";
    }
    Model model = findFirst(configName, sql);
    return model != null;
  }
}
