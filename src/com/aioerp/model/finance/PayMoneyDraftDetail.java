package com.aioerp.model.finance;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.util.ArrayList;
import java.util.List;

public class PayMoneyDraftDetail
  extends BaseDbModel
{
  public static final PayMoneyDraftDetail dao = new PayMoneyDraftDetail();
  public static final String TABLE_NAME = "cw_paymoney_draft_detail";
  
  public List<Model> getList(String configName, Integer billId)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("accounts", Accounts.class);
    helper.appendSelect(" select detail.*,accounts.* ");
    helper.appendSql(" from cw_paymoney_draft_detail as detail");
    helper.appendSql(" left join b_accounts as accounts on detail.accountsId=accounts.id");
    helper.appendSql(" where 1=1");
    helper.appendSql(" and detail.billId=" + billId);
    return dao.manyList(configName, helper);
  }
  
  public List<Integer> getListByDetailId(String configName, Integer billId)
  {
    String sql = "SELECT id FROM cw_paymoney_draft_detail WHERE billId = ?";
    List<Model> models = dao.find(configName, sql, new Object[] { billId });
    List<Integer> result = new ArrayList();
    for (Model model : models)
    {
      Integer id = model.getInt("id");
      result.add(id);
    }
    return result;
  }
  
  public Integer delDeail(String configName, Integer billId)
  {
    String sql = "delete from cw_paymoney_draft_detail where billId = ? ";
    return Integer.valueOf(Db.use(configName).update(sql, new Object[] { billId }));
  }
}
