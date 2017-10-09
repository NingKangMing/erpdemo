package com.aioerp.model.fz;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Model;
import java.util.List;

public class ReportRow
  extends BaseDbModel
{
  public static final ReportRow dao = new ReportRow();
  private static final String TABLE = "fz_report_row";
  
  public List<Model> getListByType(String configName, String reportType)
  {
    List<Model> list = dao.find(configName, "select * from  fz_report_row where reportType = '" + reportType + "' order by rank");
    return list;
  }
  
  public List<Model> getListByType(String configName, String reportType, int status)
  {
    return dao.find(configName, "select * from  fz_report_row where reportType = '" + reportType + "' and status = " + status + " order by rank");
  }
}
