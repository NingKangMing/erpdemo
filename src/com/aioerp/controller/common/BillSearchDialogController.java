package com.aioerp.controller.common;

import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.sys.BillType;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BillSearchDialogController
  extends BaseController
{
  public void index()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String startDate = sdf.format(DateUtils.addDays(new Date(), -7));
    setAttr("startDate", getPara("startTime", startDate));
    setAttr("endDate", getPara("endTime", DateUtils.getCurrentDate()));
    

    String unitId = getPara("unitId");
    String staffId = getPara("staffId");
    String productId = getPara("productId");
    String storageId = getPara("storageId");
    String departmentId = getPara("departmentId");
    String accountId = getPara("accountId");
    Integer billTypeId = getParaToInt("billTypeId", Integer.valueOf(0));
    String isCoupon = getPara("isCoupon", "0");
    String billCode = getPara("billCode");
    String remark = getPara("remark");
    String memo = getPara("memo");
    String isMember = getPara("isMember", "0");
    String priceCase = getPara("priceCase", "0");
    String price = getPara("price", "0");
    String discountCase = getPara("discountCase", "0");
    String discount = getPara("discount", "0");
    String taxrateCase = getPara("taxrateCase", "0");
    String taxrate = getPara("taxrate", "0");
    
    String configName = loginConfigName();
    
    setAttr("unitId", unitId);
    if (!StringUtil.isNull(unitId)) {
      setAttr("unit", Unit.dao.findById(configName, unitId));
    }
    setAttr("staffId", staffId);
    if (!StringUtil.isNull(staffId)) {
      setAttr("staff", Staff.dao.findById(configName, staffId));
    }
    setAttr("productId", productId);
    if (!StringUtil.isNull(productId)) {
      setAttr("product", Product.dao.findById(configName, productId));
    }
    setAttr("storageId", storageId);
    if (!StringUtil.isNull(storageId)) {
      setAttr("storage", Storage.dao.findById(configName, storageId));
    }
    setAttr("departmentId", departmentId);
    if (!StringUtil.isNull(departmentId)) {
      setAttr("department", Department.dao.findById(configName, departmentId));
    }
    setAttr("accountId", accountId);
    if (!StringUtil.isNull(accountId)) {
      setAttr("account", Accounts.dao.findById(configName, accountId));
    }
    setAttr("billTypeId", billTypeId);
    if (billTypeId.intValue() > 0) {
      setAttr("billType", BillType.dao.findById(configName, billTypeId));
    }
    setAttr("isCoupon", isCoupon);
    setAttr("billCode", billCode);
    setAttr("remark", remark);
    setAttr("memo", memo);
    setAttr("isMember", isMember);
    setAttr("priceCase", priceCase);
    setAttr("price", price);
    setAttr("discountCase", discountCase);
    setAttr("discount", discount);
    setAttr("taxrateCase", taxrateCase);
    setAttr("taxrate", taxrate);
    

    render("page.html");
  }
  
  public void go()
  {
    setAttr("statusCode", Integer.valueOf(200));
    setAttr("aimTabId", getPara("aimTabId", ""));
    setAttr("aimUrl", getPara("aimUrl", ""));
    
    Map<String, Object> map = new HashMap();
    map.put("unitId", getPara("unit.id"));
    map.put("staffId", getPara("staff.id"));
    map.put("productId", getPara("product.id"));
    map.put("storageId", getPara("storage.id"));
    map.put("departmentId", getPara("department.id"));
    
    map.put("accountId", getPara("account.id"));
    map.put("billTypeId", getParaToInt("billType.id", Integer.valueOf(0)));
    map.put("isCoupon", getPara("isCoupon", "0"));
    
    map.put("billCode", getPara("billCode"));
    map.put("remark", getPara("remark"));
    map.put("memo", getPara("memo"));
    
    map.put("isMember", getPara("isMember", "0"));
    map.put("priceCase", getPara("priceCase", "0"));
    map.put("price", getPara("price", "0"));
    map.put("discountCase", getPara("discountCase", "0"));
    map.put("discount", getPara("discount", "0"));
    map.put("taxrateCase", getPara("taxrateCase", "0"));
    map.put("taxrate", getPara("taxrate", "0"));
    

    map.put("startDate", getPara("startDate"));
    map.put("endDate", getPara("endDate"));
    

    setAttr("data", map);
    

    renderJson();
  }
}
