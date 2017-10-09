package com.aioerp.model.sell.sellreturn;

import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class SellReturnBill
  extends BaseDbModel
{
  public static final SellReturnBill dao = new SellReturnBill();
  public static final String TABLE_NAME = "xs_return_bill";
  private Unit billUnit;
  private Staff billStaff;
  private Storage billStorage;
  private Department billDepartment;
  
  public Unit getBillUnit(String configName)
  {
    if (this.billUnit == null) {
      this.billUnit = ((Unit)Unit.dao.findById(configName, get("unitId")));
    }
    return this.billUnit;
  }
  
  public Staff getBillStaff(String configName)
  {
    if (this.billStaff == null) {
      this.billStaff = ((Staff)Staff.dao.findById(configName, get("staffId")));
    }
    return this.billStaff;
  }
  
  public Storage getBillStorage(String configName)
  {
    if (this.billStorage == null) {
      this.billStorage = ((Storage)Storage.dao.findById(configName, get("storageId")));
    }
    return this.billStorage;
  }
  
  public Department getBillDepartment(String configName)
  {
    if (this.billDepartment == null) {
      this.billDepartment = ((Department)Department.dao.findById(configName, get("departmentId")));
    }
    return this.billDepartment;
  }
  
  public boolean codeIsExist(String configName, String code, int id)
  {
    return Db.use(configName).queryLong("select count(*) from xs_return_bill where code =? and id != ?", new Object[] { code, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public Map<String, Object> getPage(String configName, int pageNum, int numPerPage, String listID, String orderField, String orderDirection)
  {
    String sql = " from xs_return_bill";
    if (StringUtils.isNotBlank(orderField)) {
      sql = sql + " order by " + orderField + " " + orderDirection;
    }
    return aioGetPageRecord(configName, dao, listID, pageNum, numPerPage, "select *", sql, new Object[0]);
  }
  
  public List<Model> loadNegativeStockInfo(String configName, String trIndexs, String productIds, String storageIds, String stockAmounts, String negativeStockAmounts)
  {
    List<Model> list = new ArrayList();
    if (!productIds.equals(""))
    {
      String[] trIndexArra = trIndexs.split(":");
      String[] productIdArra = productIds.split(":");
      String[] storageIdArra = storageIds.split(":");
      String[] stockAmountArra = stockAmounts.split(":");
      String[] negativeStockAmountArra = negativeStockAmounts.split(":");
      StringBuffer sb = null;
      for (int i = 0; i < productIdArra.length; i++)
      {
        sb = new StringBuffer("");
        sb.append("select product.*,storage.* from b_storage storage,b_product product where storage.id=? and product.id=?");
        Map<String, Class<? extends Model>> mapAlias = new HashMap();
        mapAlias.put("storage", Storage.class);
        mapAlias.put("product", Product.class);
        
        Model prdAndSto = findFirst(configName, sb.toString(), mapAlias, new Object[] { storageIdArra[i], productIdArra[i] });
        prdAndSto.put("trIndex", trIndexArra[i]);
        prdAndSto.put("stockAmount", stockAmountArra[i]);
        prdAndSto.put("negativeStockAmount", negativeStockAmountArra[i]);
        list.add(prdAndSto);
      }
    }
    return list;
  }
}
