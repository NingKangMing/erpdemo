package com.jfinal.plugin.activerecord;

import com.jfinal.plugin.activerecord.dialect.Dialect;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ModelUtil
  extends Model<Model>
  implements Serializable
{
  public static String defualtConfigName = "main";
  private static final long serialVersionUID = 1041659364664812848L;
  
  private List<Model> find(String configName, Connection conn, String sql, Map<String, Class<? extends Model>> alias, Object... paras)
    throws Exception
  {
    Class<? extends Model> modelClass = getClass();
    PreparedStatement pst = conn.prepareStatement(sql);
    DbKit.getConfig(configName).dialect.fillStatement(pst, paras);
    ResultSet rs = pst.executeQuery();
    List<Model> result;
  
    if ((alias == null) || (alias.isEmpty())) {
      result = ModelBuilder.build(rs, modelClass);
    } else {
      result = ModelBuilderUtil.build(rs, modelClass, alias);
    }
    DbKit.closeQuietly(rs, pst);
    return result;
  }
  
  public List<Model> find(String configName, String sql, Map<String, Class<? extends Model>> alias, Object... paras)
  {
    Connection conn = null;
    try
    {
      conn = DbKit.getConfig(configName).getConnection();
      return find(configName, conn, sql, alias, paras);
    }
    catch (Exception e)
    {
      throw new ActiveRecordException(e);
    }
    finally
    {
      DbKit.getConfig(configName).close(conn);
    }
  }
  
  public List<Model> find(String configName, String sql, Map<String, Class<? extends Model>> alias)
  {
    return find(configName, sql, alias, DbKit.NULL_PARA_ARRAY);
  }
  
  public Model findFirst(String configName, String sql, Map<String, Class<? extends Model>> alias, Object... paras)
  {
    List<Model> result = find(configName, sql, alias, paras);
    return result.size() > 0 ? (Model)result.get(0) : null;
  }
  
  public Model findFirst(String configName, String sql, Map<String, Class<? extends Model>> alias)
  {
    List<Model> result = find(configName, sql, alias, DbKit.NULL_PARA_ARRAY);
    return result.size() > 0 ? (Model)result.get(0) : null;
  }
  
  public Page<Model> paginate(String configName, int pageNumber, int pageSize, String select, String sqlExceptSelect, String distinctSql, Map<String, Class<? extends Model>> alias, Object... paras)
    throws SQLException, Exception
  {
    if ((pageNumber < 1) || (pageSize < 1)) {
      throw new ActiveRecordException("pageNumber and pageSize must be more than 0");
    }
    if (DbKit.getConfig(configName).dialect.isTakeOverModelPaginate()) {
      return DbKit.getConfig(configName).dialect.takeOverModelPaginate(DbKit.getConfig(configName).getConnection(), getClass(), pageNumber, pageSize, select, sqlExceptSelect, paras);
    }
    Config config = DbKit.getConfig(configName);
    Connection conn = null;
    try
    {
      conn = config.getConnection();
      long totalRow = 0L;
      int totalPage = 0;
      List result = null;
      if ((distinctSql != null) && (distinctSql.indexOf("distinct") != -1)) {
        result = Db.use(configName).query(config, conn, "select count(" + distinctSql + ") " + DbKit.replaceFormatSqlOrderBy(sqlExceptSelect), paras);
      } else {
        result = Db.use(configName).query(config, conn, "select count(*) " + DbKit.replaceFormatSqlOrderBy(sqlExceptSelect), paras);
      }
      int size = result.size();
      Page localPage;
      if (size == 1) {
        totalRow = ((Number)result.get(0)).longValue();
      } else if (size > 1) {
        totalRow = result.size();
      } else {
        return new Page(new ArrayList(0), pageNumber, pageSize, 0, 0);
      }
      totalPage = (int)(totalRow / pageSize);
      if (totalRow % pageSize != 0L) {
        totalPage++;
      }
      StringBuilder sql = new StringBuilder();
      DbKit.getConfig(configName).dialect.forPaginate(sql, pageNumber, pageSize, select, sqlExceptSelect);
      List<Model> list = find(configName, conn, sql.toString(), alias, paras);
      return new Page(list, pageNumber, pageSize, totalPage, (int)totalRow);
    }
    catch (Exception e)
    {
      throw new ActiveRecordException(e);
    }
    finally
    {
      DbKit.getConfig(configName).close(conn);
    }
  }
  
  public Page<Model> paginate(String configName, int pageNumber, int pageSize, ICallback callback, Map<String, Class<? extends Model>> alias, Object... paras)
    throws SQLException, Exception
  {
    if ((pageNumber < 1) || (pageSize < 1)) {
      throw new ActiveRecordException("pageNumber and pageSize must be more than 0");
    }
    Config config = DbKit.getConfig(configName);
    Connection conn = null;
    try
    {
      conn = config.getConnection();
      Map<String, Object> map = (Map)callback.call(conn);
      ResultSet rs = (ResultSet)map.get("resultSet");
      int totalRow = ((Integer)map.get("totalRow")).intValue();
      int totalPage = ((Integer)map.get("totalPage")).intValue();
      
      List<Model> list = find(configName, conn, rs, alias, paras);
      return new Page(list, pageNumber, pageSize, totalPage, totalRow);
    }
    catch (Exception e)
    {
      throw new ActiveRecordException(e);
    }
    finally
    {
      DbKit.getConfig(configName).close(conn);
    }
  }
  
  public List<Model> find(String configName, Connection conn, ResultSet rs, Map<String, Class<? extends Model>> alias, Object... paras)
    throws Exception
  {
    Class<? extends Model> modelClass = getClass();
    List<Model> result;
   
    if ((alias == null) || (alias.isEmpty())) {
      result = ModelBuilder.build(rs, modelClass);
    } else {
      result = ModelBuilderUtil.build(rs, modelClass, alias);
    }
    DbKit.closeQuietly(rs, null);
    return result;
  }
}
