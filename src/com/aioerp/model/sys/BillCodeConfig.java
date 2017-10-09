package com.aioerp.model.sys;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import java.util.List;

public class BillCodeConfig
  extends BaseDbModel
{
  public static final BillCodeConfig dao = new BillCodeConfig();
  private static final String TABLE_NAME = "sys_billcodeconfig";
  
  public List<Model> getList(String configName)
  {
    return dao.find(configName, "SELECT * FROM sys_billcodeconfig");
  }
  
  public BillCodeConfig getObj(String configName, Integer billId)
  {
    if (billId == null) {
      return null;
    }
    return (BillCodeConfig)dao.findFirst(configName, "SELECT * FROM sys_billcodeconfig where billId = " + billId);
  }
  
  public String getFormat(String index)
  {
    String format = getStr("format1");
    if ("1".equals(index)) {
      format = getStr("format1");
    } else if ("2".equals(index)) {
      format = getStr("format2");
    } else if ("3".equals(index)) {
      format = getStr("format3");
    } else if ("4".equals(index)) {
      format = getStr("format4");
    } else if ("5".equals(index)) {
      format = getStr("format5");
    } else if ("6".equals(index)) {
      format = getStr("format6");
    } else if ("7".equals(index)) {
      format = getStr("format7");
    } else if ("8".equals(index)) {
      format = getStr("format8");
    } else if ("9".equals(index)) {
      format = getStr("format9");
    } else if ("10".equals(index)) {
      format = getStr("format10");
    }
    return format;
  }
  
  public int delAll(String configName)
  {
    return Db.use(configName).update("truncate table sys_billcodeconfig");
  }
}
