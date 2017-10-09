package com.aioerp.model.sys;

import com.aioerp.model.BaseDbModel;
import com.aioerp.util.DateUtils;
import com.jfinal.log.Log4jLogger;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Model;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class BillCodeConfigFormat
  extends BaseDbModel
{
  public static final BillCodeConfigFormat dao = new BillCodeConfigFormat();
  private static Logger log = Log4jLogger.getLogger(BillCodeConfigFormat.class);
  private static final String TABLE_NAME = "sys_billcodeconfig_format";
  
  public List<Model> getList(String configName)
  {
    return dao.find(configName, "SELECT * FROM sys_billcodeconfig_format");
  }
  
  public String getFormat(String codeDateSep)
  {
    String format = getStr("formatHg");
    try
    {
      if (StringUtils.isBlank(codeDateSep)) {
        return DateUtils.format(new Date(), StringUtils.trim(format));
      }
      if ("0".equals(codeDateSep)) {
        format = getStr("formatHg");
      } else if ("1".equals(codeDateSep)) {
        format = getStr("formatDd");
      } else if ("2".equals(codeDateSep)) {
        format = getStr("formatCh");
      } else if ("3".equals(codeDateSep)) {
        format = getStr("formatNo");
      } else if ("4".equals(codeDateSep)) {
        format = getStr("formatKg");
      } else {
        format = getStr("formatHg");
      }
      return DateUtils.format(new Date(), StringUtils.trim(format));
    }
    catch (ParseException e)
    {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return format;
  }
  
  public BillCodeConfigFormat getObjByFormat(String configName, String format)
  {
    StringBuffer sql = new StringBuffer("SELECT * FROM sys_billcodeconfig_format where 1=1");
    if (StringUtils.isNotBlank(format))
    {
      sql.append(" and(");
      sql.append(" formatCh = '" + format + "'");
      sql.append(" or formatHg = '" + format + "'");
      sql.append(" or formatDd = '" + format + "'");
      sql.append(" or formatNo = '" + format + "'");
      sql.append(" or formatKg = '" + format + "'");
      sql.append(" )");
    }
    return (BillCodeConfigFormat)dao.findFirst(configName, sql.toString());
  }
  
  public String getFormatStr(String configName, String codeDateSep, String format)
    throws ParseException
  {
    if (StringUtils.isBlank(format)) {
      return null;
    }
    StringBuffer sql = new StringBuffer("SELECT * FROM sys_billcodeconfig_format where 1=1");
    if (StringUtils.isNotBlank(format))
    {
      sql.append(" and(");
      sql.append(" formatCh = '" + format + "'");
      sql.append(" or formatHg = '" + format + "'");
      sql.append(" or formatDd = '" + format + "'");
      sql.append(" or formatNo = '" + format + "'");
      sql.append(" or formatKg = '" + format + "'");
      sql.append(" )");
    }
    BillCodeConfigFormat configFormat = (BillCodeConfigFormat)dao.findFirst(configName, sql.toString());
    if (configFormat == null) {
      return format;
    }
    if ("0".equals(codeDateSep)) {
      format = configFormat.getStr("formatHg");
    } else if ("1".equals(codeDateSep)) {
      format = configFormat.getStr("formatDd");
    } else if ("2".equals(codeDateSep)) {
      format = configFormat.getStr("formatCh");
    } else if ("3".equals(codeDateSep)) {
      format = configFormat.getStr("formatNo");
    } else if ("4".equals(codeDateSep)) {
      format = configFormat.getStr("formatKg");
    } else {
      format = configFormat.getStr("formatHg");
    }
    return DateUtils.format(new Date(), StringUtils.trim(format));
  }
}
