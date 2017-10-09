package com.jfinal.plugin.activerecord;

import com.aioerp.model.BaseDbModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelBuilderUtil
  extends ModelBuilder
{
  private static final void buildTableNamesAndLabelNamesAndTypes(ResultSetMetaData rsmd, String[] tableNames, String[] labelNames, int[] types)
    throws SQLException
  {
    for (int i = 1; i < labelNames.length; i++)
    {
      tableNames[i] = rsmd.getTableName(i);
      labelNames[i] = rsmd.getColumnLabel(i);
      types[i] = rsmd.getColumnType(i);
    }
  }
  
  @SuppressWarnings("unchecked")
public static final <T> List<T> build(ResultSet rs, Class<? extends Model> modelClass, Map<String, Class<? extends Model>> alias)
    throws SQLException, InstantiationException, IllegalAccessException
  {
    List<T> result = new ArrayList();
    
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] tableNames = new String[columnCount + 1];
    String[] labelNames = new String[columnCount + 1];
    int[] types = new int[columnCount + 1];
    buildTableNamesAndLabelNamesAndTypes(rsmd, tableNames, labelNames, types);
    while (rs.next())
    {
      Model<?> row = (Model)modelClass.newInstance();
      for (String key : alias.keySet())
      {
        Model<?> ar = (Model)((Class)alias.get(key)).newInstance();
        row.put(key, ar);
      }
      for (int i = 1; i <= columnCount; i++)
      {
        Object value;
  
        if (types[i] < 2004)
        {
          value = rs.getObject(i);
        }
        else
        {
        
          if (types[i] == 2005)
          {
            value = handleClob(rs.getClob(i));
          }
          else
          {
        
            if (types[i] == 2011)
            {
              value = handleClob(rs.getNClob(i));
            }
            else
            {
            
              if (types[i] == 2004) {
                value = handleBlob(rs.getBlob(i));
              } else {
                value = rs.getObject(i);
              }
            }
          }
        }
        if (row.getAttrs().containsKey(tableNames[i]))
        {
          if ((row.get(tableNames[i]) != null) && ((row.get(tableNames[i]) instanceof Model))) {
            ((Model)row.get(tableNames[i])).getAttrs().put(labelNames[i], value);
          }
        }
        else {
          row.put(labelNames[i], value);
        }
      }
      result.add((T) row);
    }
    return result;
  }
  
  public static final List<Map<String, Object>> build(ResultSet rs, Map<String, Class<? extends Model>> alias)
    throws SQLException, InstantiationException, IllegalAccessException
  {
    List<Map<String, Object>> result = new ArrayList();
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] tableNames = new String[columnCount + 1];
    String[] labelNames = new String[columnCount + 1];
    int[] types = new int[columnCount + 1];
    buildTableNamesAndLabelNamesAndTypes(rsmd, tableNames, labelNames, types);
    while (rs.next())
    {
      Map<String, Object> row = new HashMap();
      for (String key : alias.keySet())
      {
        Model<?> ar = (Model)((Class)alias.get(key)).newInstance();
        row.put(key, ar);
      }
      for (int i = 1; i <= columnCount; i++)
      {
        Object value;
       
        if (types[i] < 2004)
        {
          value = rs.getObject(i);
        }
        else
        {
          
          if (types[i] == 2005)
          {
            value = handleClob(rs.getClob(i));
          }
          else
          {
           
            if (types[i] == 2011)
            {
              value = handleClob(rs.getNClob(i));
            }
            else
            {
             
              if (types[i] == 2004) {
                value = handleBlob(rs.getBlob(i));
              } else {
                value = rs.getObject(i);
              }
            }
          }
        }
        if (row.containsKey(tableNames[i]))
        {
          Object o = row.get(tableNames[i]);
          if ((o != null) && ((o instanceof BaseDbModel))) {
            ((Model)o).getAttrs().put(labelNames[i], value);
          }
        }
        else
        {
          row.put(labelNames[i], value);
        }
      }
      result.add(row);
    }
    return result;
  }
}
