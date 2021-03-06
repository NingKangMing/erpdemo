package com.aioerp.model.finance.changemoney;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Accounts;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.SqlHelper;
import java.util.List;

public class ChangeMoneyDetail
  extends BaseDbModel
{
  public static final ChangeMoneyDetail dao = new ChangeMoneyDetail();
  public static final String TABLE_NAME = "cw_c_money_detail";
  
  public List<Model> getListByBillId(String configName, Integer billId)
  {
    SqlHelper helper = SqlHelper.getHelper();
    helper.addAlias("accounts", Accounts.class);
    
    helper.appendSelect(" select detail.*,accounts.* ");
    helper.appendSql(" from cw_c_money_detail as detail");
    helper.appendSql(" left join b_accounts as accounts on detail.accountsId=accounts.id");
    
    helper.appendSql(" where 1=1");
    if (billId != null) {
      helper.appendSql(" and detail.billId=" + billId);
    }
    return dao.manyList(configName, helper);
  }
}
