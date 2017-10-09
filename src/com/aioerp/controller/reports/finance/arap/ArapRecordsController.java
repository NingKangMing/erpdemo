package com.aioerp.controller.reports.finance.arap;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Area;
import com.aioerp.model.reports.finance.arap.ArapRecords;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class ArapRecordsController
  extends BaseController
{
  public static void addRecords(String configName, boolean timeChange, String operate, int type, Integer billTypeId, Integer billId, Model bill, Integer areaId, Integer unitId, Integer staffId, Integer departmentId, BigDecimal money, BigDecimal getOrPayMoney)
  {
    Date time = new Date();
    if (!timeChange)
    {
      Record re = Db.use(configName).findFirst("select * from cw_arap_records where billTypeId=" + billTypeId + " and billId=" + bill.getInt("id"));
      if (re == null) {
        return;
      }
      time = re.getDate("recodeTime");
      Db.use(configName).delete("cw_arap_records", re);
    }
    ArapRecords records = null;
    if ((areaId == null) && (!operate.equals(AioConstants.OPERTE_RCW))) {
      areaId = Area.dao.getIdByUnitId(configName, unitId);
    }
    if ((operate.equals(AioConstants.OPERTE_RCW)) || (operate.equals(AioConstants.OPERTE_DEL))) {
      if (type == AioConstants.PAY_TYLE0) {
        type = AioConstants.PAY_TYLE1;
      } else if (type == AioConstants.PAY_TYLE1) {
        type = AioConstants.PAY_TYLE0;
      }
    }
    if (operate == AioConstants.OPERTE_ADD)
    {
      records = new ArapRecords();
      records.set("createTime", time);
      records.set("billTypeId", billTypeId);
      
      records.set("billId", bill.getInt("id"));
      records.set("billCode", bill.getStr("code"));
      records.set("recodeTime", bill.getTimestamp("recodeDate"));
      records.set("billAbstract", bill.getStr("remark"));
      records.set("isRCW", bill.getInt("isRCW"));
      
      records.set("areaId", areaId);
      records.set("unitId", unitId);
      records.set("staffId", staffId);
      records.set("departmentId", departmentId);
      if (type == AioConstants.PAY_TYLE0)
      {
        records.set("subMoney", money);
        records.set("payMoney", getOrPayMoney);
      }
      else if (type == AioConstants.PAY_TYLE1)
      {
        records.set("addMoney", money);
        records.set("getMoney", getOrPayMoney);
      }
      records.save(configName);
    }
    else if (operate.equals(AioConstants.OPERTE_RCW))
    {
      records = (ArapRecords)ArapRecords.dao.getArapRecordsByBillTypeId(configName, billTypeId.intValue(), billId.intValue());
      if (records == null) {
        return;
      }
      BigDecimal addMoney = records.getBigDecimal("addMoney");
      BigDecimal subMoney = records.getBigDecimal("subMoney");
      BigDecimal getMoney = records.getBigDecimal("getMoney");
      BigDecimal payMoney = records.getBigDecimal("payMoney");
      records.set("createTime", time);
      records.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
      records.update(configName);
      
      records.remove("id");
      String remark = records.getStr("billAbstract");
      if (StringUtils.isBlank(remark)) {
        remark = "(红字反冲)";
      } else {
        remark = remark + "(红字反冲)";
      }
      records.set("billAbstract", remark);
      records.set("subMoney", addMoney);
      records.set("addMoney", subMoney);
      records.set("getMoney", payMoney);
      records.set("payMoney", getMoney);
      records.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
      records.save(configName);
    }
    else
    {
      operate.equals(AioConstants.OPERTE_RCW);
    }
  }
}
