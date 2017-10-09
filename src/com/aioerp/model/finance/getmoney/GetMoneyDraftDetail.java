package com.aioerp.model.finance.getmoney;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import java.util.ArrayList;
import java.util.List;

public class GetMoneyDraftDetail
  extends BaseDbModel
{
  public static final GetMoneyDraftDetail dao = new GetMoneyDraftDetail();
  public static final String TABLE_NAME = "cw_getmoney_draft_detail";
  
  public int delOthers(String configName, Integer billId, List<Integer> ids)
  {
    StringBuffer sql = new StringBuffer("delete from cw_getmoney_draft_detail where 1=1");
    List<Object> paras = new ArrayList();
    if (billId != null)
    {
      sql.append(" and billId = ?");
      paras.add(billId);
    }
    else
    {
      sql.append(" and billId = ?");
      paras.add(Integer.valueOf(0));
    }
    if (ids != null) {
      for (Integer id : ids) {
        if (id != null)
        {
          sql.append(" and id != ?");
          paras.add(id);
        }
      }
    }
    return Db.use(configName).update(sql.toString(), paras.toArray());
  }
  
  public int delByBill(String configName, Integer billId)
  {
    StringBuffer sql = new StringBuffer("delete from cw_getmoney_draft_detail where 1=1");
    List<Object> paras = new ArrayList();
    if (billId != null)
    {
      sql.append(" and billId = ?");
      paras.add(billId);
    }
    else
    {
      sql.append(" and billId = ?");
      paras.add(Integer.valueOf(0));
    }
    return Db.use(configName).update(sql.toString(), paras.toArray());
  }
}
