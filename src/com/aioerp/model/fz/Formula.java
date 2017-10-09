package com.aioerp.model.fz;

import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Model;
import java.util.List;

public class Formula
  extends BaseDbModel
{
  public static final Formula dao = new Formula();
  public static final String TABLE_NAME = "fz_formula";
  
  public Model getFormulaByType(String configName, int moduleId, int type)
  {
    return findFirst(configName, "SELECT * FROM fz_formula where 1=1 and billTypeId =" + moduleId + " and type = " + type);
  }
  
  public List<Model> getModelTypeFormulaList(String configName, int moduleId)
  {
    return dao.find(configName, "select * from fz_formula where 1=1 and billTypeId =" + moduleId);
  }
}
