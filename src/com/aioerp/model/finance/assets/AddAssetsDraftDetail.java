package com.aioerp.model.finance.assets;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.util.ArrayList;
import java.util.List;

public class AddAssetsDraftDetail
  extends BaseDbModel
{
  public static final AddAssetsDraftDetail dao = new AddAssetsDraftDetail();
  public static final String TABLE_NAME = "cw_addassets_draft_detail";
  
  public List<Model> getListByBillId(String configName, Integer billId)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("accounts", Accounts.class);
    
    helper.appendSelect(" select detail.*,accounts.* ");
    helper.appendSql(" from cw_addassets_draft_detail as detail");
    helper.appendSql(" left join b_accounts as accounts on detail.accountsId=accounts.id");
    
    helper.appendSql(" where 1=1");
    if (billId != null) {
      helper.appendSql(" and detail.billId=" + billId);
    }
    return dao.manyList(configName, helper);
  }
  
  public int delOthers(String configName, Integer billId, List<Integer> ids)
  {
    StringBuffer sql = new StringBuffer("delete from cw_addassets_draft_detail where 1=1");
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
    StringBuffer sql = new StringBuffer("delete from cw_addassets_draft_detail where 1=1");
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
  
  public Integer delDeail(String configName, Integer billId)
  {
    String sql = "delete from cw_addassets_draft_detail where billId = ? ";
    return Integer.valueOf(Db.use(configName).update(sql, new Object[] { billId }));
  }
}
