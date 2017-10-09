package com.aioerp.common.procedure;

import com.jfinal.plugin.activerecord.ICallback;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CommonProc
  implements ICallback
{
  private String procName;
  private ResultSet rs;
  private ResultSet mdrs = null;
  private Connection conn;
  private Map<String, Object> map;
  private int pageIndex;
  private int pageSize;
  private int totalRow = 0;
  private int totalPage = 0;
  private CallableStatement proc = null;
  
  public CommonProc(String procName, int pageIndex, int pageSize, Map<String, Object> map)
  {
    this.procName = procName;
    this.pageIndex = pageIndex;
    this.pageSize = pageSize;
    this.map = map;
  }
  
  private void getMetaData()
  {
    try
    {
      DatabaseMetaData dbmd = this.conn.getMetaData();
      this.mdrs = dbmd.getProcedureColumns(null, null, this.procName, "%");
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }
  
  private int getProcParameterCount()
  {
    int Columncount = 0;
    try
    {
      this.mdrs.last();
      Columncount = this.mdrs.getRow();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    return Columncount;
  }
  
  private void setParameter()
  {
    try
    {
      this.mdrs.first();
      String proCol = "";
      while (this.mdrs.next())
      {
        proCol = this.mdrs.getString("COLUMN_NAME");
        if ((!"totalRow".equals(proCol)) && (!"totalPage".equals(proCol)) && 
          (!"pageIndex".equals(proCol)) && (!"pageSize".equals(proCol)))
        {
          String para = String.valueOf(this.map.get(proCol));
          if ("null".equals(para))
          {
            int type = this.mdrs.getInt("DATA_TYPE");
            this.proc.setNull(proCol, type);
          }
          else
          {
            this.proc.setString(proCol, para);
          }
        }
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }
  
  private ResultSet execute()
  {
    StringBuffer sql = new StringBuffer("call ");
    sql.append(this.procName + " (");
    int count = getProcParameterCount();
    for (int i = 1; i <= count; i++) {
      if (i == count) {
        sql.append("?");
      } else {
        sql.append("?,");
      }
    }
    sql.append(");");
    try
    {
      this.proc = this.conn.prepareCall(sql.toString());
      this.proc.registerOutParameter(1, 4);
      this.proc.registerOutParameter(2, 4);
      this.proc.setInt(3, this.pageIndex);
      this.proc.setInt(4, this.pageSize);
      
      setParameter();
      
      this.proc.execute();
      this.rs = this.proc.getResultSet();
      this.totalRow = this.proc.getInt(1);
      this.totalPage = this.proc.getInt(2);
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    return this.rs;
  }
  
  private int getTotalRow()
  {
    return this.totalRow;
  }
  
  private int getTotalPage()
  {
    return this.totalPage;
  }
  
  public Object call(Connection conn)
    throws SQLException
  {
    this.conn = conn;
    getMetaData();
    
    Map<String, Object> map = new HashMap();
    map.put("resultSet", execute());
    map.put("totalRow", Integer.valueOf(getTotalRow()));
    map.put("totalPage", Integer.valueOf(getTotalPage()));
    return map;
  }
}
