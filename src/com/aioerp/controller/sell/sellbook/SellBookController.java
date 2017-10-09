package com.aioerp.controller.sell.sellbook;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.sell.sell.SellDetail;
import com.aioerp.model.sell.sellbook.SellbookBill;
import com.aioerp.model.sell.sellbook.SellbookDetail;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellBookController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE2;
  
  public void index()
  {
    String configName = loginConfigName();
    billCodeAuto(billTypeId);
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    
    loadOnlyOneBaseInfo(configName);
    
    notEditStaff();
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
    render("page.html");
  }
  
  @Before({Tx.class})
  public void add()
  {
    String configName = loginConfigName();
    SellbookBill bill = (SellbookBill)getModel(SellbookBill.class);
    

    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    Date recodeDate = bill.getTimestamp("recodeDate");
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> detail = ModelUtils.batchInjectSortObjModel(getRequest(), SellbookDetail.class, "sellbookDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detail.size() == 0)
    {
      setAttr("message", "请选择要商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(); return;
    }
    BigDecimal sellBookAmount;
    for (int i = 0; i < detail.size(); i++)
    {
      Model sellBookDetail = (Model)detail.get(i);
      String trIndex = String.valueOf(StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1));
      sellBookAmount = sellBookDetail.getBigDecimal("amount");
      if (BigDecimalUtils.compare(sellBookAmount, BigDecimal.ZERO) < 1)
      {
        setAttr("message", "第" + trIndex + "行数量录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    bill.set("unitId", getParaToInt("unit.id"));
    bill.set("staffId", getParaToInt("staff.id"));
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    bill.set("storageId", Integer.valueOf(storageId));
    Date time = new Date();
    bill.set("status", Integer.valueOf(AioConstants.BILL_STATUS3));
    bill.set("relStatus", Integer.valueOf(AioConstants.STATUS_DISABLE));
    bill.set("updateTime", time);
    ComFunController.billOrderDefualPriceMoney(bill);
    bill.set("userId", Integer.valueOf(loginUserId()));
    

    billCodeIncrease(bill, "orderSave");
    bill.save(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    for (Model sellbookDetail : detail)
    {
      Integer productId = sellbookDetail.getInt("productId");
      if (productId != null)
      {
        Product productIdi = (Product)Product.dao.findById(configName, productId);
        BigDecimal sellbookAmount = sellbookDetail.getBigDecimal("amount");
        int selectUnitId = sellbookDetail.getInt("selectUnitId").intValue();
        
        BigDecimal litterAmount = DwzUtils.getConversionAmount(sellbookAmount, Integer.valueOf(selectUnitId), productIdi, Integer.valueOf(1));
        
        ComFunController.detailOrderDefualPriceMoney(sellbookDetail);
        
        BigDecimal taxPrice = sellbookDetail.getBigDecimal("taxPrice");
        
        BigDecimal litterPrice = DwzUtils.getConversionPrice(taxPrice, Integer.valueOf(selectUnitId), productIdi.getInt("unitRelation1"), productIdi.getBigDecimal("unitRelation2"), productIdi.getBigDecimal("unitRelation3"), Integer.valueOf(1));
        



        sellbookDetail.set("billId", bill.getInt("id"));
        sellbookDetail.set("baseAmount", litterAmount);
        sellbookDetail.set("basePrice", litterPrice);
        sellbookDetail.set("updateTime", time);
        sellbookDetail.set("untreatedAmount", litterAmount);
        sellbookDetail.set("relStatus", Integer.valueOf(AioConstants.STATUS_DISABLE));
        sellbookDetail.save(configName);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    
    setAttr("navTabId", "sell_book_info");
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    printBeforSave1();
    
    renderJson();
  }
  
  public void toEditValidate()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    boolean isHave = SellDetail.dao.sellbookHasRel(configName, id);
    if (isHave)
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "订单已经被执行，请选择   【查看单据】！");
      renderJson();
      return;
    }
    SellbookBill bill = (SellbookBill)SellbookBill.dao.findById(configName, Integer.valueOf(id));
    if (AioConstants.STATUS_DISABLE != bill.getInt("relStatus").intValue())
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "订单已完成，不能被修改！");
      renderJson();
      return;
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    renderJson();
  }
  
  public void toEdit()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    boolean isHave = SellDetail.dao.sellbookHasRel(configName, id);
    boolean checkout = getParaToBoolean("checkout", Boolean.valueOf(false)).booleanValue();
    if (isHave)
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
    if (isHave)
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "订单已经被执行，请选择   【查看单据】！");
      renderJson();
      return;
    }
    SellbookBill bill = (SellbookBill)SellbookBill.dao.findById(configName, Integer.valueOf(id));
    if (AioConstants.STATUS_DISABLE != bill.getInt("relStatus").intValue())
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
    List<Model> detailList = SellbookDetail.dao.getList2(configName, id);
    
    detailList = addTrSize(detailList, 15);
    
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    

    notEditStaff();
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
    render("/WEB-INF/template/sell/book/edit.html");
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String configName = loginConfigName();
    
    SellbookBill sellbook = (SellbookBill)getModel(SellbookBill.class);
    int billId = ((Integer)sellbook.get("id", Integer.valueOf(0))).intValue();
    SellbookBill oldSellbook = (SellbookBill)SellbookBill.dao.findById(configName, Integer.valueOf(billId));
    








    List<Model> detail = ModelUtils.batchInjectSortObjModel(getRequest(), SellbookDetail.class, "sellbookDetail");
    if (detail.size() == 0)
    {
      setAttr("message", "请选择要商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    sellbook.set("unitId", getParaToInt("unit.id"));
    sellbook.set("staffId", getParaToInt("staff.id"));
    sellbook.set("storageId", getParaToInt("storage.id", Integer.valueOf(0)));
    Date time = new Date();
    sellbook.set("updateTime", time);
    sellbook.set("printNum", oldSellbook.getInt("printNum"));
    sellbook.set("status", oldSellbook.getInt("status"));
    sellbook.set("relStatus", oldSellbook.getInt("relStatus"));
    sellbook.set("isRCW", oldSellbook.getInt("isRCW"));
    ComFunController.billOrderDefualPriceMoney(sellbook);
    sellbook.set("userId", Integer.valueOf(loginUserId()));
    

    billCodeIncrease(sellbook, "orderSave");
    sellbook.update(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, sellbook.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    
    List<Integer> detailIds = new ArrayList();
    for (Model sellbookDetail : detail)
    {
      Integer productId = sellbookDetail.getInt("productId");
      Integer detailId = sellbookDetail.getInt("id");
      if (productId != null)
      {
        Product productIdi = (Product)Product.dao.findById(configName, productId);
        BigDecimal sellbookAmount = sellbookDetail.getBigDecimal("amount");
        int selectUnitId = sellbookDetail.getInt("selectUnitId").intValue();
        
        BigDecimal litterAmount = DwzUtils.getConversionAmount(sellbookAmount, Integer.valueOf(selectUnitId), productIdi, Integer.valueOf(1));
        
        ComFunController.detailOrderDefualPriceMoney(sellbookDetail);
        
        BigDecimal taxPrice = sellbookDetail.getBigDecimal("taxPrice");
        
        BigDecimal litterPrice = DwzUtils.getConversionPrice(taxPrice, Integer.valueOf(selectUnitId), productIdi.getInt("unitRelation1"), productIdi.getBigDecimal("unitRelation2"), productIdi.getBigDecimal("unitRelation3"), Integer.valueOf(1));
        



        sellbookDetail.set("billId", sellbook.getInt("id"));
        sellbookDetail.set("baseAmount", litterAmount);
        sellbookDetail.set("basePrice", litterPrice);
        sellbookDetail.set("updateTime", time);
        sellbookDetail.set("untreatedAmount", litterAmount);
        sellbookDetail.set("relStatus", Integer.valueOf(AioConstants.STATUS_DISABLE));
        if (detailId == null)
        {
          sellbookDetail.save(configName);
        }
        else
        {
          SellbookDetail oldSellbookDetail = (SellbookDetail)SellbookDetail.dao.findById(configName, detailId);
          sellbookDetail.set("updateTime", time);
          sellbookDetail.set("replenishAmount", oldSellbookDetail.getBigDecimal("replenishAmount"));
          sellbookDetail.set("forceAmount", oldSellbookDetail.getBigDecimal("forceAmount"));
          sellbookDetail.set("relStatus", oldSellbookDetail.getInt("relStatus"));
          sellbookDetail.update(configName);
        }
        detailIds.add(sellbookDetail.getInt("id"));
      }
    }
    SellbookDetail.dao.delOthers(configName, Integer.valueOf(billId), detailIds);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "sell_book_info");
    renderJson();
  }
  
  @Before({Tx.class})
  public void delete()
    throws SQLException
  {
    String configName = loginConfigName();
    try
    {
      int id = getParaToInt(0, Integer.valueOf(0)).intValue();
      
      SellbookBill bill = (SellbookBill)SellbookBill.dao.findById(configName, Integer.valueOf(id));
      if (AioConstants.STATUS_DISABLE != bill.getInt("relStatus").intValue())
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        setAttr("message", "订单已完成，不能被删除！");
        renderJson();
        return;
      }
      boolean isHave = SellDetail.dao.sellbookHasRel(configName, id);
      if (isHave)
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        setAttr("message", "订单已经被执行，请选择   【查看单据】！");
        renderJson();
        return;
      }
      bill.delete(configName);
      Db.use(configName).update("delete from xs_sellbook_detail where billId=" + id);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "系统出现异常，请联系管理员！");
      renderJson();
      return;
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("rel", "xs_sellBookSearch1");
    renderJson();
  }
  
  @Before({Tx.class})
  public void force()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    SellbookBill bill = (SellbookBill)SellbookBill.dao.findById(configName, Integer.valueOf(id));
    if ((AioConstants.STATUS_ENABLE == bill.getInt("relStatus").intValue()) || (AioConstants.STATUS_FORCE == bill.getInt("relStatus").intValue()))
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "订单已完成，不能再强制完成！");
      renderJson();
      return;
    }
    Date time = new Date();
    bill.set("relStatus", Integer.valueOf(AioConstants.STATUS_FORCE));
    Object memo = bill.get("memo");
    if (memo != null) {
      bill.set("memo", bill.get("memo") + "[强制完成]");
    } else {
      bill.set("memo", "[强制完成]");
    }
    bill.set("updateTime", time);
    bill.update(configName);
    
    List<Model> detailList = SellbookDetail.dao.getList1(configName, id);
    for (Model model : detailList)
    {
      SellbookDetail detail = (SellbookDetail)model;
      BigDecimal untreatedAmount = detail.getBigDecimal("untreatedAmount");
      BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
      detail.set("updateTime", time);
      if (BigDecimalUtils.compare(baseAmount, untreatedAmount) >= 0)
      {
        detail.set("forceAmount", untreatedAmount);
        detail.set("untreatedAmount", BigDecimal.ZERO);
        detail.set("relStatus", Integer.valueOf(AioConstants.STATUS_ENABLE));
        detail.update(configName);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("rel", "xs_sellBookSearch1");
    renderJson();
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int orderId = getParaToInt(0, Integer.valueOf(0)).intValue();
    
    Model bill = SellbookBill.dao.findById(configName, Integer.valueOf(orderId));
    List<Model> detailList = SellbookDetail.dao.getList2(configName, orderId);
    
    detailList = addTrSize(detailList, 15);
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
    render("/WEB-INF/template/sell/book/look.html");
  }
  
  public void option()
  {
    String configName = loginConfigName();
    Integer unitId = getParaToInt("unitId", Integer.valueOf(0));
    SellbookBill bill = (SellbookBill)getModel(SellbookBill.class);
    Map<String, Object> billMap = new HashMap();
    billMap.put("unitPrivs", basePrivs(UNIT_PRIVS));
    billMap.put("staffPrivs", basePrivs(STAFF_PRIVS));
    billMap.put("storagePrivs", basePrivs(STORAGE_PRIVS));
    
    billMap.put("bill", bill);
    billMap.put("startDate", getPara("startDate"));
    billMap.put("endDate", getPara("endDate"));
    billMap.put("unitName", getPara("unit.fullName"));
    billMap.put("staffName", getPara("staff.name"));
    billMap.put("storageName", getPara("storage.fullName"));
    billMap.put("productName", getPara("product.fullName"));
    billMap.put("detailMemo", getPara("detail.memo"));
    
    List<Model> billList = SellbookBill.dao.getList(configName, AioConstants.STATUS_DISABLE, unitId, billMap);
    if (billList.size() == 0)
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "没有符合条件的数据!");
      setAttr("callbackType", "closeCurrent");
      renderJson();
      return;
    }
    if (billList.size() > 0) {
      setAttr("detailList", SellbookDetail.dao.getList(configName, ((Model)((Model)billList.get(0)).get("bill")).getInt("id").intValue(), basePrivs(PRODUCT_PRIVS)));
    }
    setAttr("billList", billList);
    setAttr("unitId", unitId);
    setAttr("bill", bill);
    setAttr("startDate", getPara("startDate"));
    setAttr("endDate", getPara("endDate"));
    setAttr("unitName", getPara("unit.fullName"));
    setAttr("staffName", getPara("staff.name"));
    setAttr("storageName", getPara("storage.fullName"));
    setAttr("productName", getPara("product.fullName"));
    setAttr("detailMemo", getPara("detail.memo"));
    
    setAttr("rowList", ReportRow.dao.getListByType(configName, "xsdd", AioConstants.STATUS_ENABLE));
    setAttr("detailRowList", ReportRow.dao.getListByType(configName, "xsddyy", AioConstants.STATUS_ENABLE));
    

    render("option.html");
  }
  
  public void detail()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    setAttr("detailList", SellbookDetail.dao.getList(configName, billId, basePrivs(PRODUCT_PRIVS)));
    setAttr("bageBreak", "sellbookDetailTable");
    setAttr("detailRowList", ReportRow.dao.getListByType(configName, "xsddyy", AioConstants.STATUS_ENABLE));
    
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
      SellbookBill bill = (SellbookBill)getModel(SellbookBill.class);
      Map<String, Object> map = new HashMap();
      int unitId = bill.getInt("unitId").intValue();
      map.put("bill", bill);
      map.put("startDate", getPara("startDate"));
      map.put("endDate", getPara("endDate"));
      map.put("unitName", getPara("unit.fullName"));
      map.put("staffName", getPara("staff.name"));
      map.put("storageName", getPara("storage.fullName"));
      map.put("productName", getPara("product.fullName"));
      map.put("detailMemo", getPara("detail.memo"));
      
      map.put("unitPrivs", basePrivs(UNIT_PRIVS));
      map.put("staffPrivs", basePrivs(STAFF_PRIVS));
      map.put("storagePrivs", basePrivs(STORAGE_PRIVS));
      

      List<Model> billList = SellbookBill.dao.getList(configName, AioConstants.STATUS_DISABLE, Integer.valueOf(unitId), map);
      if (billList.size() > 0) {
        setAttr("detailList", SellbookDetail.dao.getList(configName, ((Model)((Model)billList.get(0)).get("bill")).getInt("id").intValue(), basePrivs(PRODUCT_PRIVS)));
      }
      setAttr("unitId", Integer.valueOf(unitId));
      setAttr("bill", bill);
      setAttr("startDate", getPara("startDate"));
      setAttr("endDate", getPara("endDate"));
      setAttr("unitName", getPara("unit.fullName"));
      setAttr("staffName", getPara("staff.name"));
      setAttr("storageName", getPara("storage.fullName"));
      setAttr("productName", getPara("product.fullName"));
      setAttr("detailMemo", getPara("detail.memo"));
      
      setAttr("billList", billList);
      setAttr("rowList", ReportRow.dao.getListByType(configName, "xsdd", AioConstants.STATUS_ENABLE));
      setAttr("detailRowList", ReportRow.dao.getListByType(configName, "xsddyy", AioConstants.STATUS_ENABLE));
      
      render("option.html");
    }
    else
    {
      setAttr("unitId", getParaToInt("unitId", Integer.valueOf(0)));
      setStartDateAndEndDate();
      render("dialogSearch.html");
    }
  }
  
  public void print()
  {
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "销售订单");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    Map<String, Object> billData = new HashMap();
    Model bill = null;
    List<Model> detailList = null;
    
    Integer billId = Integer.valueOf(0);
    
    String actionType = getPara("actionType", "");
    if (actionType.equals("look"))
    {
      bill = SellbookBill.dao.findById(configName, getParaToInt("bill.id", Integer.valueOf(0)));
      detailList = SellbookDetail.dao.getList1(configName, bill.getInt("id").intValue());
      billId = bill.getInt("id");
    }
    else
    {
      bill = (Model)getModel(SellbookBill.class);
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("staffId", getParaToInt("staff.id", Integer.valueOf(0)));
      bill.set("storageId", getParaToInt("storage.id", Integer.valueOf(0)));
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellbookDetail.class, "sellbookDetail");
    }
    billData.put("tableName", "xs_sellbook_bill");
    billData.put("base", getAttr("base"));
    Integer id = bill.getInt("id");
    if ((id != null) && (id.intValue() != 0)) {
      billData.put("id", id);
    }
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    Model unit = Unit.dao.findById(configName, bill.getInt("unitId"));
    Model staff = Staff.dao.findById(configName, bill.getInt("staffId"));
    Model storage = Storage.dao.findById(configName, bill.getInt("storageId"));
    String unitFullName = unit == null ? "" : unit.getStr("fullName");
    String staffFullName = staff == null ? "" : staff.getStr("fullName");
    String storageFullName = storage == null ? "" : storage.getStr("fullName");
    


    printCommonData(headData);
    headData.add("录单日期:" + trimRecordDateNull(bill.getDate("recodeDate")));
    headData.add("单据编号:" + trimNull(bill.getStr("code")));
    headData.add("购买单位 :" + trimNull(unitFullName));
    printUnitCommonData(headData, unit);
    headData.add("经手人 :" + trimNull(staffFullName));
    headData.add("交货日期:" + trimNull(bill.getDate("deliveryDate")));
    headData.add("发货仓库:" + trimNull(storageFullName));
    headData.add("摘要:" + trimNull(bill.getStr("remark")));
    headData.add("附加说明 :" + trimNull(bill.getStr("memo")));
    headData.add("整单折扣 :" + trimNull(bill.getBigDecimal("discounts")));
    
    setOrderUserId(false, bill, actionType, headData);
    

    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    
    colTitle.add("单位");
    colTitle.add("辅助单位");
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
    colTitle.add("附加信息");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      Integer selectUnitId = detail.getInt("selectUnitId");
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      
      rowData.add(trimNull(product.getStr("smallName")));
      rowData.add(trimNull(product.getStr("spell")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
      rowData.add(trimNull(product.getStr("field")));
      
      rowData.add(trimNull(DwzUtils.getSelectUnit(product, selectUnitId)));
      rowData.add(DwzUtils.helpUnit(product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(detail.getDate("produceDate")));
      rowData.add(trimNull(detail.getDate("produceEndDate")));
      rowData.add(trimNull(detail.getStr("batch")));
      rowData.add(DwzUtils.helpAmount(detail.getBigDecimal("amount"), product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(1)) + trimNull(product.getStr("calculateUnit1")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(2)) + trimNull(product.getStr("calculateUnit2")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(3)) + trimNull(product.getStr("calculateUnit3")));
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
      rowData.add(trimNull(detail.getStr("memo")));
      rowData.add(status);
      rowData.add(trimNull(detail.getBigDecimal("arrivalAmount")));
      rowData.add(trimNull(product.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(trimNull(detail.getStr("message")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    data.put("billData", billData);
    renderJson(data);
  }
}
