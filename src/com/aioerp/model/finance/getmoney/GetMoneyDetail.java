package com.aioerp.model.finance.getmoney;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.util.List;

public class GetMoneyDetail
  extends BaseDbModel
{
  public static final GetMoneyDetail dao = new GetMoneyDetail();
  
  public List<Model> getList2(String configName, Integer billId, String tableName)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("accounts", Accounts.class);
    helper.appendSelect(" select detail.*,accounts.* ");
    helper.appendSql(" from " + tableName + " as detail");
    helper.appendSql(" left join b_accounts as accounts on detail.accountsId=accounts.id");
    helper.appendSql(" where 1=1");
    helper.appendSql(" and detail.billId=" + billId);
    return dao.manyList(configName, helper);
  }
}
