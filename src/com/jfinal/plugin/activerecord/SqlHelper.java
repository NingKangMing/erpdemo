package com.jfinal.plugin.activerecord;

import com.aioerp.model.BaseDbModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class SqlHelper
{
  public static final String LIKE_TYPE_LEFT = "left";
  public static final String LIKE_TYPE_RIGHT = "right";
  public static final String LIKE_TYPE_BOTH = "both";
  private Page<?> page;
  private StringBuffer sql = new StringBuffer("");
  private StringBuffer select = new StringBuffer("");
  private List<Object> params = new ArrayList();
  private Map<String, Class<? extends Model>> alias = new HashMap();
  
  public static SqlHelper getHelper(Page page)
  {
    return new SqlHelper(page);
  }
  
  public static SqlHelper getHelper()
  {
    return new SqlHelper();
  }
  
  public SqlHelper(Page<?> page)
  {
    this.page = page;
  }
  
  public SqlHelper() {}
  
  public SqlHelper appendParam(Object o)
  {
    this.params.add(o);
    return this;
  }
  
  public SqlHelper appendLikeParam(Object o)
  {
    appendLikeParam(o, "both");
    return this;
  }
  
  public SqlHelper appendLikeParam(Object o, String likeType)
  {
    if (StringUtils.equalsIgnoreCase(likeType, "both")) {
      this.params.add("%" + o + "%");
    } else if (StringUtils.equalsIgnoreCase(likeType, "left")) {
      this.params.add(o + "%");
    } else if (StringUtils.equalsIgnoreCase(likeType, "right")) {
      this.params.add("%" + o);
    } else {
      this.params.add("%" + o + "%");
    }
    return this;
  }
  
  public SqlHelper appendParams(Object[] objs)
  {
    for (Object o : objs) {
      this.params.add(o);
    }
    generateInSql(objs.length);
    return this;
  }
  
  private void generateInSql(int size)
  {
    if (size > 1)
    {
      this.sql.insert(this.sql.lastIndexOf("?") + 1, ",?");
      generateInSql(--size);
    }
  }
  
  public SqlHelper appendParams(List ls)
  {
    for (Object o : ls) {
      this.params.add(o);
    }
    generateInSql(ls.size());
    return this;
  }
  
  public SqlHelper appendSql(String str)
  {
    this.sql.append(str);
    return this;
  }
  
  public SqlHelper appendSelect(String str)
  {
    this.select.append(str);
    return this;
  }
  
  public String getSqlSelect()
  {
    if (StringUtils.isBlank(this.select)) {
      this.select.append(" select * ");
    }
    return this.select.toString();
  }
  
  public String getSqlExceptSelect()
  {
    return this.sql.toString();
  }
  
  public String getSql()
  {
    return this.sql.toString().trim();
  }
  
  public Object[] getParams()
  {
    return this.params == null ? null : this.params.toArray();
  }
  
  public int getPageNumber()
  {
    return this.page.getPageNumber();
  }
  
  public int getPageSize()
  {
    return this.page.getPageSize();
  }
  
  public boolean isPage()
  {
    return this.page != null;
  }
  
  public SqlHelper addAlias(String alias, Class<? extends Model> classz)
  {
    this.alias.put(alias, classz);
    return this;
  }
  
  public SqlHelper addAlias(Class<? extends BaseDbModel>... classes)
  {
    for (Class<? extends BaseDbModel> classz : classes) {
      try
      {
        this.alias.put(((BaseDbModel)classz.newInstance()).getTableName(), classz);
      }
      catch (InstantiationException e)
      {
        e.printStackTrace();
      }
      catch (IllegalAccessException e)
      {
        e.printStackTrace();
      }
    }
    return this;
  }
  
  public Map<String, Class<? extends Model>> getAlias()
  {
    return this.alias;
  }
  
  public boolean hasAlias()
  {
    return this.alias.isEmpty();
  }
}
