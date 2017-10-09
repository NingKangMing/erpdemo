package com.aioerp.controller.bought;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.base.Product;
import com.aioerp.model.bought.BoughtBill;
import com.aioerp.model.bought.BoughtDetail;
import com.aioerp.model.bought.PurchaseDetail;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DwzUtils;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoughtBillController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE1;
  
  public void index()
  {
    String configName = loginConfigName();
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
    
    loadOnlyOneBaseInfo(configName);
    

    billCodeAuto(billTypeId);
    

    notEditStaff();
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
  {
    String configName = loginConfigName();
    
    printBeforSave1();
    BoughtBill bought = (BoughtBill)getModel(BoughtBill.class);
    
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bought.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    Date recodeDate = bought.getTimestamp("recodeDate");
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<BoughtDetail> detail = ModelUtils.batchInjectSortObjModel(getRequest(), BoughtDetail.class, "boughtDetail");
    if (detail.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Date time = new Date();
    bought.set("unitId", getParaToInt("unit.id"));
    bought.set("staffId", getParaToInt("staff.id"));
    bought.set("storageId", getParaToInt("storage.id"));
    bought.set("updateTime", time);
    bought.set("userId", Integer.valueOf(loginUserId()));
    if (bought.get("discountMoneys") == null) {
      bought.set("discountMoneys", bought.get("moneys"));
    }
    if (bought.get("taxMoneys") == null) {
      bought.set("taxMoneys", bought.get("discountMoneys"));
    }
    billCodeIncrease(bought, "orderSave");
    bought.save(configName);
    for (BoughtDetail boughtDetail : detail)
    {
      Integer productId = boughtDetail.getInt("productId");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(boughtDetail.getBigDecimal("amount"), boughtDetail.getInt("selectUnitId"), unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        BigDecimal orderPrice = boughtDetail.getBigDecimal("price");
        BigDecimal discountPrice = boughtDetail.getBigDecimal("discountPrice");
        if (boughtDetail.get("discountPrice") == null) {
          boughtDetail.set("discountPrice", boughtDetail.get("price"));
        }
        if (boughtDetail.get("taxPrice") == null) {
          boughtDetail.set("taxPrice", boughtDetail.get("discountPrice"));
        }
        if (boughtDetail.getBigDecimal("discount") == null) {
          boughtDetail.set("discount", BigDecimal.ONE);
        }
        if (boughtDetail.get("discountMoney") == null) {
          boughtDetail.set("discountMoney", BigDecimalUtils.mul(BigDecimalUtils.mul(boughtDetail.getBigDecimal("amount"), boughtDetail.getBigDecimal("price")), boughtDetail.getBigDecimal("discount")));
        }
        if (boughtDetail.get("taxMoney") == null) {
          boughtDetail.set("taxMoney", BigDecimalUtils.add(boughtDetail.getBigDecimal("discountMoney"), boughtDetail.getBigDecimal("taxes")));
        }
        if ((discountPrice != null) && (!"".equals(discountPrice)) && (BigDecimalUtils.compare(discountPrice, BigDecimal.ZERO) != 0)) {
          orderPrice = discountPrice;
        }
        BigDecimal basePrice = DwzUtils.getConversionPrice(orderPrice, boughtDetail.getInt("selectUnitId"), unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        boughtDetail.set("billId", bought.getInt("id"));
        boughtDetail.set("updateTime", time);
        boughtDetail.set("baseAmount", baseAmount);
        boughtDetail.set("basePrice", basePrice);
        boughtDetail.set("untreatedAmount", baseAmount);
        boughtDetail.set("bargainMoney", BigDecimalUtils.mul(baseAmount, basePrice));
        if ((orderPrice == null) || ("".equals(orderPrice)) || (BigDecimalUtils.compare(orderPrice, BigDecimal.ZERO) == 0)) {
          boughtDetail.set("giftAmount", baseAmount);
        }
        boughtDetail.save(configName);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "boughtView");
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bought.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    renderJson();
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    if (isPost())
    {
      BoughtBill bought = (BoughtBill)getModel(BoughtBill.class);
      int billId = ((Integer)bought.get("id", Integer.valueOf(0))).intValue();
      






      List<BoughtDetail> detail = ModelUtils.batchInjectObjModel(getRequest(), BoughtDetail.class, "boughtDetail");
      if (detail.size() == 0)
      {
        setAttr("message", "请选择商品!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      Date time = new Date();
      bought.set("unitId", getParaToInt("unit.id"));
      bought.set("staffId", getParaToInt("staff.id"));
      bought.set("storageId", getParaToInt("storage.id"));
      bought.set("updateTime", time);
      bought.set("userId", Integer.valueOf(loginUserId()));
      billCodeIncrease(bought, "orderSave");
      bought.update(configName);
      List<Integer> detailIds = new ArrayList();
      for (BoughtDetail boughtDetail : detail)
      {
        Integer productId = boughtDetail.getInt("productId");
        Integer detailId = boughtDetail.getInt("id");
        if (productId != null)
        {
          Product product = (Product)Product.dao.findById(configName, productId);
          Integer unitRelation1 = product.getInt("unitRelation1");
          BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
          BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
          
          BigDecimal baseAmount = DwzUtils.getConversionAmount(boughtDetail.getBigDecimal("amount"), Integer.valueOf(1), unitRelation1, unitRelation2, unitRelation3, boughtDetail.getInt("selectUnitId"));
          BigDecimal orderPrice = boughtDetail.getBigDecimal("price");
          BigDecimal discountPrice = boughtDetail.getBigDecimal("discountPrice");
          if ((discountPrice != null) && (!"".equals(discountPrice))) {
            orderPrice = discountPrice;
          }
          BigDecimal basePrice = DwzUtils.getConversionPrice(orderPrice, Integer.valueOf(1), unitRelation1, unitRelation2, unitRelation3, boughtDetail.getInt("selectUnitId"));
          boughtDetail.set("billId", bought.getInt("id"));
          boughtDetail.set("updateTime", time);
          boughtDetail.set("untreatedAmount", baseAmount);
          if ((orderPrice == null) || ("".equals(orderPrice))) {
            boughtDetail.set("giftAmount", baseAmount);
          }
          boughtDetail.set("baseAmount", baseAmount);
          boughtDetail.set("basePrice", basePrice);
          boughtDetail.set("bargainMoney", BigDecimalUtils.mul(baseAmount, basePrice));
          if (detailId == null) {
            boughtDetail.save(configName);
          } else {
            boughtDetail.update(configName);
          }
          detailIds.add(boughtDetail.getInt("id"));
        }
      }
      BoughtDetail.dao.delOthers(configName, Integer.valueOf(billId), detailIds);
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "boughtSearchView");
      ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bought.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
      renderJson();
    }
    else
    {
      boolean isHave = PurchaseDetail.dao.isHaveBought(configName, id);
      boolean checkout = getParaToBoolean("checkout", Boolean.valueOf(false)).booleanValue();
      if ((checkout) && (isHave))
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        setAttr("message", "订单已经被执行，请选择   【查看单据】！");
        renderJson();
        return;
      }
      if ((checkout) && (!isHave))
      {
        renderJson();
        return;
      }
      BoughtBill bill = (BoughtBill)BoughtBill.dao.findById(configName, Integer.valueOf(id));
      if ((checkout) && (AioConstants.STATUS_ENABLE == bill.getInt("status").intValue()))
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        setAttr("message", "订单已完成，不能被修改！");
        renderJson();
        return;
      }
      if ((checkout) && (AioConstants.STATUS_ENABLE != bill.getInt("status").intValue()))
      {
        renderJson();
        return;
      }
      List<Model> detailList = BoughtDetail.dao.getList1(configName, Integer.valueOf(id), basePrivs(PRODUCT_PRIVS));
      detailList = addTrSize(detailList, 15);
      setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
      setAttr("bill", bill);
      setAttr("detailList", detailList);
      setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
      setAttr("tableId", Integer.valueOf(billTypeId));
      setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
      setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
      render("edit.html");
    }
  }
  
  public void search()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String unitPrivs = basePrivs(UNIT_PRIVS);
    String staffPrivs = basePrivs(STAFF_PRIVS);
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    Map<String, Object> map = new HashMap();
    map.put("unitPrivs", unitPrivs);
    map.put("staffPrivs", staffPrivs);
    map.put("storagePrivs", storagePrivs);
    Map<String, Object> pageMap = BoughtBill.dao.getPage(configName, pageNum, numPerPage, "boughtBillList", orderField, orderDirection, map);
    List<Model> list = (List)pageMap.get("pageList");
    BigDecimal moneys = BigDecimal.ZERO;
    BigDecimal discountMoneys = BigDecimal.ZERO;
    BigDecimal taxMoneys = BigDecimal.ZERO;
    Map<String, Object> result = new HashMap();
    for (Model model : list)
    {
      moneys = BigDecimalUtils.add(moneys, model.getBigDecimal("moneys"));
      discountMoneys = BigDecimalUtils.add(discountMoneys, model.getBigDecimal("discountMoneys"));
      taxMoneys = BigDecimalUtils.add(taxMoneys, model.getBigDecimal("taxMoneys"));
    }
    result.put("moneys", moneys);
    result.put("discountMoneys", discountMoneys);
    result.put("taxMoneys", taxMoneys);
    
    setAttr("total", result);
    setAttr("pageMap", pageMap);
    setAttr("params", result);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5012", AioConstants.STATUS_ENABLE));
    render("page.html");
  }
  
  public void list()
    throws Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    BoughtBill bill = (BoughtBill)getModel(BoughtBill.class);
    Map<String, Object> map = new HashMap();
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    map.put("bill", bill);
    
    Map<String, Object> pageMap = BoughtBill.dao.getPage(configName, pageNum, numPerPage, "boughtBillList", map);
    List<Model> list = (List)pageMap.get("pageList");
    BigDecimal moneys = BigDecimal.ZERO;
    BigDecimal discountMoneys = BigDecimal.ZERO;
    BigDecimal taxMoneys = BigDecimal.ZERO;
    Map<String, Object> result = new HashMap();
    for (Model model : list)
    {
      moneys = BigDecimalUtils.add(moneys, model.getBigDecimal("moneys"));
      discountMoneys = BigDecimalUtils.add(discountMoneys, model.getBigDecimal("discountMoneys"));
      taxMoneys = BigDecimalUtils.add(taxMoneys, model.getBigDecimal("taxMoneys"));
    }
    result.put("moneys", moneys);
    result.put("discountMoneys", discountMoneys);
    result.put("taxMoneys", taxMoneys);
    
    setAttr("total", result);
    setAttr("pageMap", pageMap);
    setAttr("boughtBill", bill);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5012", AioConstants.STATUS_ENABLE));
    render("list.html");
  }
  
  public void option()
  {
    String configName = loginConfigName();
    Integer unitId = getParaToInt("unitId", Integer.valueOf(0));
    Integer storageId = getParaToInt("storageId", Integer.valueOf(0));
    Integer staffId = getParaToInt("staffId", Integer.valueOf(0));
    BoughtBill bill = (BoughtBill)getModel(BoughtBill.class);
    Map<String, Object> billMap = new HashMap();
    billMap.put("unitPrivs", basePrivs(UNIT_PRIVS));
    billMap.put("staffPrivs", basePrivs(STAFF_PRIVS));
    billMap.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    billMap.put("unitId", unitId);
    billMap.put("storageId", storageId);
    billMap.put("staffId", staffId);
    billMap.put("bill", bill);
    billMap.put("startDate", getPara("startDate"));
    billMap.put("endDate", getPara("endDate"));
    billMap.put("unitName", getPara("unit.fullName"));
    billMap.put("staffName", getPara("staff.name"));
    billMap.put("storageName", getPara("storage.fullName"));
    billMap.put("productName", getPara("product.fullName"));
    billMap.put("detailMemo", getPara("boughtDetail.memo"));
    

    List<Model> billList = BoughtBill.dao.getList(configName, Integer.valueOf(AioConstants.STATUS_DISABLE), billMap);
    if (billList.size() == 0)
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "没有符合条件的数据!");
      setAttr("callbackType", "closeCurrent");
      renderJson();
      return;
    }
    if (billList.size() > 0)
    {
      setAttr("detailList", BoughtDetail.dao.getUntreatedList(configName, ((Model)billList.get(0)).getInt("id"), basePrivs(PRODUCT_PRIVS)));
      setAttr("billId", ((Model)billList.get(0)).getInt("id"));
    }
    setAttr("unitId", unitId);
    setAttr("storageId", storageId);
    setAttr("staffId", staffId);
    
    setAttr("bill", bill);
    setAttr("startDate", getPara("startDate"));
    setAttr("endDate", getPara("endDate"));
    setAttr("unitName", getPara("unit.fullName"));
    setAttr("staffName", getPara("staff.name"));
    setAttr("storageName", getPara("storage.fullName"));
    setAttr("productName", getPara("product.fullName"));
    setAttr("detailMemo", getPara("boughtDetail.memo"));
    
    setAttr("billList", billList);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "jhdd", AioConstants.STATUS_ENABLE));
    setAttr("detailRowList", ReportRow.dao.getListByType(configName, "jhddyy", AioConstants.STATUS_ENABLE));
    render("option.html");
  }
  
  public void detail()
  {
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    setAttr("detailList", BoughtDetail.dao.getUntreatedList(configName, Integer.valueOf(billId), basePrivs(PRODUCT_PRIVS)));
    setAttr("detailRowList", ReportRow.dao.getListByType(configName, "jhddyy", AioConstants.STATUS_ENABLE));
    render("detailList.html");
  }
  
  public void dialogSearch()
    throws ParseException
  {
    String configName = loginConfigName();
    if (isPost())
    {
      if (getPara("userSearchDate", "").equals("checked")) {
        SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), getPara("startDate"), getPara("endDate"));
      }
      BoughtBill bill = (BoughtBill)getModel(BoughtBill.class);
      Map<String, Object> map = new HashMap();
      map.put("bill", bill);
      map.put("startDate", getPara("startDate"));
      map.put("endDate", getPara("endDate"));
      map.put("unitName", getPara("unit.fullName"));
      map.put("staffName", getPara("staff.name"));
      map.put("storageName", getPara("storage.fullName"));
      map.put("productName", getPara("product.fullName"));
      map.put("detailMemo", getPara("boughtDetail.memo"));
      
      map.put("unitPrivs", basePrivs(UNIT_PRIVS));
      map.put("staffPrivs", basePrivs(STAFF_PRIVS));
      map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
      
      List<Model> billList = BoughtBill.dao.getList(configName, Integer.valueOf(AioConstants.STATUS_DISABLE), map);
      if (billList.size() > 0) {
        setAttr("detailList", BoughtDetail.dao.getUntreatedList(configName, ((Model)billList.get(0)).getInt("id"), basePrivs(PRODUCT_PRIVS)));
      }
      setAttr("bill", bill);
      setAttr("startDate", getPara("startDate"));
      setAttr("endDate", getPara("endDate"));
      setAttr("unitName", getPara("unit.fullName"));
      setAttr("staffName", getPara("staff.name"));
      setAttr("storageName", getPara("storage.fullName"));
      setAttr("productName", getPara("product.fullName"));
      setAttr("detailMemo", getPara("boughtDetail.memo"));
      
      setAttr("billList", billList);
      setAttr("rowList", ReportRow.dao.getListByType(configName, "jhdd", AioConstants.STATUS_ENABLE));
      setAttr("detailRowList", ReportRow.dao.getListByType(configName, "jhddyy", AioConstants.STATUS_ENABLE));
      render("option.html");
    }
    else
    {
      setStartDateAndEndDate();
      render("dialogSearch.html");
    }
  }
  
  public void query()
    throws Exception
  {
    String configName = loginConfigName();
    if (isPost())
    {
      if (getPara("userSearchDate", "").equals("checked")) {
        SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), getPara("startDate"), getPara("endDate"));
      }
      int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
      int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
      String orderField = getPara("orderField");
      String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
      BoughtBill bill = (BoughtBill)getModel(BoughtBill.class);
      Map<String, Object> map = new HashMap();
      map.put("startDate", getParaToDate("startDate"));
      map.put("endDate", getParaToDate("endDate"));
      map.put("unitId", getParaToInt("unit.id"));
      map.put("staffId", getParaToInt("staff.id"));
      map.put("storageId", getParaToInt("storage.id"));
      map.put("orderField", orderField);
      map.put("orderDirection", orderDirection);
      map.put("bill", bill);
      map.put("unitPrivs", basePrivs(UNIT_PRIVS));
      map.put("staffPrivs", basePrivs(STAFF_PRIVS));
      map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
      
      Map<String, Object> pageMap = BoughtBill.dao.getPage(configName, pageNum, numPerPage, "boughtBillList", map);
      List<Model> list = (List)pageMap.get("pageList");
      BigDecimal moneys = BigDecimal.ZERO;
      BigDecimal discountMoneys = BigDecimal.ZERO;
      BigDecimal taxMoneys = BigDecimal.ZERO;
      Map<String, Object> result = new HashMap();
      for (Model model : list)
      {
        moneys = BigDecimalUtils.add(moneys, model.getBigDecimal("moneys"));
        discountMoneys = BigDecimalUtils.add(discountMoneys, model.getBigDecimal("discountMoneys"));
        taxMoneys = BigDecimalUtils.add(taxMoneys, model.getBigDecimal("taxMoneys"));
      }
      result.put("moneys", moneys);
      result.put("discountMoneys", discountMoneys);
      result.put("taxMoneys", taxMoneys);
      
      setAttr("total", result);
      setAttr("pageMap", pageMap);
      setAttr("params", map);
      setAttr("boughtBill", bill);
      setAttr("orderField", orderField);
      setAttr("orderDirection", orderDirection);
      setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5012", AioConstants.STATUS_ENABLE));
      render("list.html");
    }
    else
    {
      setStartDateAndEndDate();
      render("search.html");
    }
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    BoughtBill bill = (BoughtBill)BoughtBill.dao.findById(configName, Integer.valueOf(id));
    List<Model> detailList = BoughtDetail.dao.getList1(configName, Integer.valueOf(id), basePrivs(PRODUCT_PRIVS));
    detailList = addTrSize(detailList, 15);
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    render("look.html");
  }
  
  @Before({Tx.class})
  public void delete()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    BoughtBill bill = (BoughtBill)BoughtBill.dao.findById(configName, Integer.valueOf(id));
    if (AioConstants.STATUS_ENABLE == bill.getInt("status").intValue())
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "订单已完成，不能被删除！");
      renderJson();
      return;
    }
    boolean isHave = PurchaseDetail.dao.isHaveBought(configName, id);
    if (isHave)
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "订单已经被执行，请选择   【查看单据】！");
      renderJson();
      return;
    }
    bill.delete(configName);
    BoughtDetail.dao.delOthers(configName, Integer.valueOf(id), null);
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("rel", "boughtBillList");
    renderJson();
  }
  
  @Before({Tx.class})
  public void force()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    BoughtBill bill = (BoughtBill)BoughtBill.dao.findById(configName, Integer.valueOf(id));
    if ((AioConstants.STATUS_ENABLE == bill.getInt("status").intValue()) || (AioConstants.STATUS_FORCE == bill.getInt("status").intValue()))
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "订单已完成，不能再强制完成！");
      renderJson();
      return;
    }
    Date time = new Date();
    bill.set("status", Integer.valueOf(AioConstants.STATUS_FORCE));
    Object memo = bill.get("memo");
    if (memo != null) {
      bill.set("memo", bill.get("memo") + "[强制完成]");
    } else {
      bill.set("memo", "[强制完成]");
    }
    Record user = (Record)getSessionAttr("user");
    bill.set("userId", user.getInt("id"));
    bill.set("updateTime", time);
    bill.update(configName);
    
    List<Model> betailList = BoughtDetail.dao.getList1(configName, Integer.valueOf(id), basePrivs(PRODUCT_PRIVS));
    for (Model model : betailList)
    {
      BoughtDetail detail = (BoughtDetail)model;
      BigDecimal untreatedAmount = detail.getBigDecimal("untreatedAmount");
      BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
      detail.set("updateTime", time);
      if (BigDecimalUtils.compare(baseAmount, untreatedAmount) >= 0)
      {
        detail.set("forceAmount", untreatedAmount);
        detail.set("untreatedAmount", BigDecimal.ZERO);
        detail.update(configName);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("rel", "boughtBillList");
    renderJson();
  }
  
  public void execute()
    throws SQLException, Exception
  {
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "updateTime");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String type = getPara(1, "first");
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    String configName = loginConfigName();
    Map<String, Object> map = new HashMap();
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("departmentPrivs", basePrivs(DEPARTMENT_PRIVS));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    
    Map<String, Object> pageMap = BoughtBill.dao.getExecutePage(configName, "boughtBillExecuteList", pageNum, numPerPage, billId, map);
    
    List<Model> list = (List)pageMap.get("pageList");
    BigDecimal moneys = BigDecimal.ZERO;
    Map<String, Object> result = new HashMap();
    for (Model model : list) {
      moneys = BigDecimalUtils.add(moneys, model.getBigDecimal("moneys"));
    }
    result.put("moneys", moneys);
    
    setAttr("total", result);
    setAttr("pageMap", pageMap);
    setAttr("pageNum", Integer.valueOf(pageNum));
    setAttr("billId", Integer.valueOf(billId));
    setAttr("numPerPage", Integer.valueOf(numPerPage));
    setAttr("params", map);
    setAttr("rowList", ReportRow.dao.getListByType(configName, "cg5013", AioConstants.STATUS_ENABLE));
    if ("first".equals(type)) {
      render("execute.html");
    } else {
      render("executeList.html");
    }
  }
  
  public void print()
    throws ParseException
  {
    String configName = loginConfigName();
    Integer billId = getParaToInt("billId", Integer.valueOf(0));
    Integer boughtBillId = getParaToInt("boughtBill.id", Integer.valueOf(0));
    if ((boughtBillId == null) || (boughtBillId.intValue() == 0)) {
      boughtBillId = billId;
    }
    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "进货订单");
    
    List<String> headData = new ArrayList();
    Integer unitId = getParaToInt("unit.id");
    setHeadUnitData(headData, unitId);
    headData.add("供货单位 :" + getPara("unit.fullName", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("交货日期:" + getPara("boughtBill.deliveryDate", ""));
    String recodeDate = DateUtils.format(getParaToDate("boughtBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    
    headData.add("收货仓库:" + getPara("storage.fullName", ""));
    headData.add("摘要:" + getPara("boughtBill.remark", ""));
    headData.add("附加说明 :" + getPara("boughtBill.memo", ""));
    headData.add("整单折扣 :" + getPara("boughtBill.discounts", ""));
    headData.add("单据编号:" + getPara("boughtBill.code", ""));
    
    Integer userId = null;
    Model boughtBill = BoughtBill.dao.findById(configName, boughtBillId);
    if (boughtBill != null) {
      userId = boughtBill.getInt("userId");
    }
    setBillUser(headData, userId);
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("单位");
    colTitle.add("生产日期");
    colTitle.add("到期日期");
    colTitle.add("批号");
    colTitle.add("辅助数量");
    colTitle.add("基本数量");
    colTitle.add("辅助数量1");
    colTitle.add("辅助数量2");
    colTitle.add("数量");
    colTitle.add("单价");
    colTitle.add("金额");
    colTitle.add("折扣");
    colTitle.add("折后单价");
    colTitle.add("折后金额");
    colTitle.add("税率");
    colTitle.add("含税单价");
    colTitle.add("税额");
    colTitle.add("含税金额");
    colTitle.add("备注");
    colTitle.add("状态");
    colTitle.add("到货数量");
    colTitle.add("条码");
    colTitle.add("辅助单位");
    colTitle.add("附加信息");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), BoughtDetail.class, "boughtDetail");
    if ((billId != null) && (billId.intValue() != 0))
    {
      detailList = BoughtDetail.dao.getList1(configName, billId, basePrivs(PRODUCT_PRIVS));
      Model bill = BoughtBill.dao.findById(configName, billId);
      Integer printNum = bill.getInt("printNum");
      if (printNum == null) {
        printNum = Integer.valueOf(0);
      }
      printNum = Integer.valueOf(printNum.intValue() + 1);
      bill.set("printNum", printNum);
      bill.update(configName);
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
      rowData.add(trimNull(product.getStr("field")));
      Integer selectUnitId = detail.getInt("selectUnitId");
      rowData.add(trimNull(DwzUtils.getSelectUnit(product, selectUnitId)));
      rowData.add(trimNull(detail.getDate("produceDate")));
      rowData.add(trimNull(detail.getDate("produceEndDate")));
      rowData.add(trimNull(detail.getStr("batch")));
      rowData.add(DwzUtils.helpAmount(detail.getBigDecimal("amount"), product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(1))) + trimNull(product.getStr("calculateUnit1")));
      rowData.add(trimNull(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(2))) + trimNull(product.getStr("calculateUnit2")));
      rowData.add(trimNull(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(3))) + trimNull(product.getStr("calculateUnit3")));
      rowData.add(trimNull(detail.getBigDecimal("amount")));
      rowData.add(trimNull(detail.getBigDecimal("price")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.getBigDecimal("discount")));
      rowData.add(trimNull(detail.getBigDecimal("discountPrice")));
      rowData.add(trimNull(detail.getBigDecimal("discountMoney")));
      rowData.add(trimNull(detail.getBigDecimal("taxRate")));
      rowData.add(trimNull(detail.getBigDecimal("taxPrice")));
      rowData.add(trimNull(detail.getBigDecimal("taxes")));
      rowData.add(trimNull(detail.getBigDecimal("taxMoney")));
      rowData.add(trimNull(detail.get("memo")));
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      rowData.add(status);
      rowData.add(trimNull(detail.getBigDecimal("arrivalAmount")));
      rowData.add(trimNull(product.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(DwzUtils.helpUnit(product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(detail.getStr("message")));
    }
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public void printSearch()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "进货订单查询");
    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("单据类型");
    colTitle.add("订单编号");
    colTitle.add("日期");
    colTitle.add("存盘时间");
    colTitle.add("单位编号");
    colTitle.add("单位全称");
    colTitle.add("经手人编号");
    colTitle.add("经手人全名");
    colTitle.add("到货日期");
    colTitle.add("折前金额");
    colTitle.add("折后金额");
    colTitle.add("税后金额");
    colTitle.add("是否完成");
    colTitle.add("附加说明");
    colTitle.add("打印次数");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String unitPrivs = basePrivs(UNIT_PRIVS);
    String staffPrivs = basePrivs(STAFF_PRIVS);
    String storagePrivs = basePrivs(STORAGE_PRIVS);
    Map<String, Object> map = new HashMap();
    map.put("unitPrivs", unitPrivs);
    map.put("staffPrivs", staffPrivs);
    map.put("storagePrivs", storagePrivs);
    Map<String, Object> pageMap = BoughtBill.dao.getPage(configName, 1, totalCount, "boughtBillList", orderField, orderDirection, map);
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model model = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      rowData.add("进货订单");
      rowData.add(trimNull(model.getStr("code")));
      rowData.add(trimNull(model.get("recodeDate")));
      rowData.add(trimNull(model.get("updateTime")));
      Model unit = (Model)model.get("unit");
      if (unit != null)
      {
        rowData.add(trimNull(unit.get("code")));
        rowData.add(trimNull(unit.get("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Model staff = (Model)model.get("staff");
      if (staff != null)
      {
        rowData.add(trimNull(staff.get("code")));
        rowData.add(trimNull(staff.get("name")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(model.get("deliveryDate")));
      rowData.add(trimNull(model.get("moneys")));
      rowData.add(trimNull(model.get("discountMoneys")));
      rowData.add(trimNull(model.get("taxMoneys")));
      Integer status = model.getInt("status");
      if ((status != null) && (status.intValue() == 1)) {
        rowData.add("否");
      } else if ((status != null) && ((status.intValue() == 2) || (status.intValue() == 3))) {
        rowData.add("是");
      }
      rowData.add(trimNull(model.get("memo")));
      rowData.add(trimNull(model.get("printNum")));
    }
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public void printExecute()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "进货订单执行情况");
    List<String> headData = new ArrayList();
    
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("单据类型");
    colTitle.add("单据编号");
    colTitle.add("执行日期");
    colTitle.add("摘要");
    colTitle.add("经手人编号");
    colTitle.add("经手人全名");
    colTitle.add("经手人");
    colTitle.add("部门编号");
    colTitle.add("部门全名");
    colTitle.add("金额");
    colTitle.add("附加说明");
    
    colWidth.add("30");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    int billId = getParaToInt("billId", Integer.valueOf(0)).intValue();
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    Map<String, Object> map = new HashMap();
    map.put("unitPrivs", basePrivs(UNIT_PRIVS));
    map.put("staffPrivs", basePrivs(STAFF_PRIVS));
    map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    map.put("departmentPrivs", basePrivs(DEPARTMENT_PRIVS));
    map.put("orderField", orderField);
    map.put("orderDirection", orderDirection);
    
    Map<String, Object> pageMap = BoughtBill.dao.getExecutePage(configName, "boughtBillExecuteList", 1, totalCount, billId, map);
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      Model model = (Model)list.get(i);
      rowData.add(trimNull(i + 1));
      Long type = model.getLong("type");
      if ((type != null) && (type.longValue() == AioConstants.BILL_ROW_TYPE5)) {
        rowData.add("进货单");
      } else {
        rowData.add("进货订单");
      }
      rowData.add(trimNull(model.get("code")));
      rowData.add(trimNull(model.get("updateTime")));
      rowData.add(trimNull(model.get("remark")));
      Model staff = (Model)model.get("staff");
      if (staff != null)
      {
        rowData.add(trimNull(staff.get("code")));
        rowData.add(trimNull(staff.get("name")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Model user = (Model)model.get("user");
      if (user != null) {
        rowData.add(trimNull(user.get("username")));
      } else {
        rowData.add("");
      }
      Model department = (Model)model.get("department");
      if (department != null)
      {
        rowData.add(trimNull(department.get("code")));
        rowData.add(trimNull(department.get("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      rowData.add(trimNull(model.get("moneys")));
      rowData.add(trimNull(model.get("memo")));
    }
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public void executeLook()
  {
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int type = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (billTypeId == type) {
      forwardAction("/bought/bought/look/" + billId);
    } else {
      forwardAction("/bought/purchase/look/" + billId);
    }
  }
}
