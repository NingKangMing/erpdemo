package com.aioerp.model.finance;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Staff;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;

public class AccountVoucherBill
  extends BaseDbModel
{
  public static final AccountVoucherBill dao = new AccountVoucherBill();
  public static final String TABLE_NAME = "cw_accountvoucher_bill";
  private Staff billStaff;
  
  public Staff getBillStaff(String configName)
  {
    if (this.billStaff == null) {
      this.billStaff = ((Staff)Staff.dao.findById(configName, get("staffId")));
    }
    return this.billStaff;
  }
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from cw_accountvoucher_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
}
