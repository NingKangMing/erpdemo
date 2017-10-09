package com.aioerp.controller.sell.sellreturn;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AvgpriceConTroller;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.reports.finance.arap.ArapRecordsController;
import com.aioerp.controller.sell.sell.SellController;
import com.aioerp.controller.sell.sell.SellDetailController;
import com.aioerp.controller.stock.StockController;
import com.aioerp.controller.stock.StockRecordsController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.StockComfirmUtil;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.sell.sell.SellDetail;
import com.aioerp.model.sell.sellreturn.SellReturnBill;
import com.aioerp.model.sell.sellreturn.SellReturnDetail;
import com.aioerp.model.sell.sellreturn.SellReturnDraftBill;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.stock.StockDraftRecords;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DeepClone;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class SellReturnController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE7;
  
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
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws UnsupportedEncodingException
  {
    String configName = loginConfigName();
    Date time = new Date();
    SellReturnBill bill = (SellReturnBill)getModel(SellReturnBill.class);
    List<Model> noMergeDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellReturnDetail.class, "sellReturnDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    String priceStockHasComfirmId = getPara("priceStockHasComfirmId", "nhas");
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    Date recodeDate = bill.getTimestamp("recodeDate");
    int billStorageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    Map<Integer, Product> productMap = new HashMap();
    


    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    Map<String, Object> mapmap = verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_ADD, getParaToInt("unit.id"), AioConstants.WAY_PAY, bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
    if (mapmap != null)
    {
      renderJson(mapmap);
      return;
    }
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    boolean hasOther = SellReturnBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(billTypeId));
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    if (unitId == 0)
    {
      setAttr("message", "请选购买单位!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    if (noMergeDetailList.size() == 0)
    {
      setAttr("message", "请选择要商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    for (int i = 0; i < noMergeDetailList.size(); i++)
    {
      Model detail = (Model)noMergeDetailList.get(i);
      Integer productId = detail.getInt("productId");
      Integer selectUnitId = detail.getInt("selectUnitId");
      BigDecimal amount = detail.getBigDecimal("amount");
      Product product = (Product)Product.dao.findById(configName, productId);
      productMap.put(productId, product);
      String trIndex = String.valueOf(StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1));
      ComFunController.detailOrderDefualPriceMoney(detail);
      BigDecimal taxPrice = detail.getBigDecimal("taxPrice");
      
      Map<String, Object> map = ComFunController.setDetailStorageId(detail, amount, trIndex, Integer.valueOf(billStorageId), "storageId", null, null);
      if (Integer.valueOf(map.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(map);
        return;
      }
      detail.set("baseAmount", DwzUtils.getConversionAmount(amount, selectUnitId, product, Integer.valueOf(1)));
      detail.set("basePrice", DwzUtils.getConversionPrice(taxPrice, selectUnitId, product, Integer.valueOf(1)));
      detail.put("trIndex", trIndex);
      detail.put("costPrice", ((HelpUtil)helpUitlList.get(i)).getCostPrice());
    }
    List<Model> mergeDetailList = (List)DeepClone.deepClone(noMergeDetailList);
    mergeDetailList = ComFunController.productMerge(mergeDetailList, productMap);
    
    List<StockComfirmUtil> costPriceInfoList = new ArrayList();
    Map<Integer, Model> relMap = new HashMap();
    for (int i = 0; i < mergeDetailList.size(); i++)
    {
      Model detail = (Model)mergeDetailList.get(i);
      Integer detailId = detail.getInt("detailId");
      int storageId = detail.getInt("storageId").intValue();
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)productMap.get(Integer.valueOf(productId));
      int costArith = product.getInt("costArith").intValue();
      int selectUnitId = detail.getInt("selectUnitId").intValue();
      BigDecimal stockAmount = BigDecimal.ZERO;
      BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
      BigDecimal costPrice = detail.getBigDecimal("costPrice");
      if ((detailId != null) && (detailId.intValue() != 0))
      {
        SellDetail relDetail = (SellDetail)SellDetail.dao.findById(configName, detailId);
        relMap.put(Integer.valueOf(productId), relDetail);
        BigDecimal relCostMoneys = relDetail.getBigDecimal("costMoneys");
        BigDecimal relAmount = relDetail.getBigDecimal("amount");
        BigDecimal relBaseAmount = DwzUtils.getConversionAmount(relAmount, relDetail.getInt("selectUnitId"), product, Integer.valueOf(1));
        if (BigDecimalUtils.compare(baseAmount, relBaseAmount) == 1) {
          costPrice = BigDecimalUtils.div(relCostMoneys, relBaseAmount);
        } else if (BigDecimalUtils.compare(baseAmount, relBaseAmount) == 0) {
          costPrice = BigDecimalUtils.div(relCostMoneys, relBaseAmount);
        } else {
          costPrice = BigDecimalUtils.div(relCostMoneys, relBaseAmount);
        }
      }
      else if (costArith == AioConstants.PRD_COST_PRICE4)
      {
        stockAmount = Stock.dao.getStockAmount(configName, productId, storageId, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
      }
      else
      {
        Map<String, String> map = ComFunController.outProductGetCostPrice(configName, storageId, product, costPrice, selectUnitId);
        stockAmount = new BigDecimal((String)map.get("stockAmount"));
        costPrice = new BigDecimal((String)map.get("costPrice"));
      }
      detail.set("costPrice", costPrice);
      detail.set("costMoneys", BigDecimalUtils.mul(baseAmount, costPrice));
      SellController.setCostPrice(noMergeDetailList, productId, "costPrice", costPrice);
      if (((detailId == null) || (detailId.intValue() == 0)) && (BigDecimalUtils.compare(stockAmount, BigDecimal.ZERO) == 0)) {
        ComFunController.costPriceData(costPriceInfoList, detail.getStr("trIndex"), storageId, product, costArith, selectUnitId, 2);
      }
      ((Model)mergeDetailList.get(i)).remove("trIndex");
    }
    if ((priceStockHasComfirmId.equals("nhas")) && (costPriceInfoList.size() > 0))
    {
      Map<String, Object> map = ComFunController.costPriceDataInfo(String.valueOf(getAttr("base")), costPriceInfoList);
      renderJson(map);
      return;
    }
    String payTypeIds = getPara("payTypeIds");
    String payTypeMoneys = getPara("payTypeMoneys");
    bill.set("unitId", Integer.valueOf(unitId));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("storageId", Integer.valueOf(billStorageId));
    bill.set("status", Integer.valueOf(AioConstants.BILL_STATUS3));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("updateTime", time);
    if ((AioConstants.VERSION == 1) || (AioConstants.IS_FREE_VERSION == "yes")) {
      bill.set("deliveryDate", time);
    } else {
      bill.set("deliveryDate", bill.getDate("deliveryDate"));
    }
    ComFunController.billOrderDefualPriceMoney(bill);
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (draftId != 0) {
      billCodeIncrease(bill, "draftPost");
    } else {
      billCodeIncrease(bill, "add");
    }
    bill.set("id", null);
    bill.save(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    

    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("taxMoneys"));
    
    BigDecimal sellReturnCosts = BigDecimal.ZERO;
    List<Record> proAccountList = new ArrayList();
    for (int i = 0; i < noMergeDetailList.size(); i++)
    {
      Record proAccount = new Record();
      Model detail = (Model)noMergeDetailList.get(i);
      Integer detailId = detail.getInt("detailId");
      int storageId = detail.getInt("storageId").intValue();
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)productMap.get(Integer.valueOf(productId));
      BigDecimal costPrice = detail.getBigDecimal("costPrice");
      BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
      BigDecimal costMoneys = BigDecimalUtils.mul(costPrice, baseAmount);
      
      detail.set("costMoneys", costMoneys);
      detail.set("billId", bill.getInt("id"));
      detail.set("updateTime", time);
      detail.set("id", null);
      detail.save(configName);
      
      sellReturnCosts = BigDecimalUtils.add(sellReturnCosts, costMoneys);
      proAccount.set("productId", Integer.valueOf(productId));
      proAccount.set("costMoney", costMoneys);
      
      StockRecordsController.addRecords(configName, billTypeId, "in", bill, detail, costPrice, baseAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
      
      StockController.stockChange(configName, "in", storageId, productId, product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
      
      AvgpriceConTroller.addAvgprice(configName, "in", Integer.valueOf(storageId), Integer.valueOf(productId), baseAmount, costMoneys);
      if (detailId != null) {
        SellDetailController.editDetailAmount(configName, baseAmount, (Model)relMap.get(Integer.valueOf(productId)));
      }
      proAccountList.add(proAccount);
    }
    BigDecimal getOrPayMoneys = bill.getBigDecimal("payMoney");
    
    BigDecimal needGetOrPay = BigDecimalUtils.sub(bill.getBigDecimal("privilegeMoney"), getOrPayMoneys);
    PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, unitId, null, needGetOrPay);
    

    ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_ADD, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), null, bill, null, Integer.valueOf(unitId), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), needGetOrPay, getOrPayMoneys);
    

    Map<String, Object> record = new HashMap();
    record.put("sellReturnCosts", sellReturnCosts);
    record.put("taxes", bill.getBigDecimal("taxes"));
    record.put("sellReturnMoneys", bill.getBigDecimal("discountMoneys"));
    record.put("needGetOrPay", needGetOrPay);
    record.put("proAccountList", proAccountList);
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), Integer.valueOf(unitId), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), bill.getBigDecimal("privilege"), payTypeIds, payTypeMoneys, record);
    

    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      SellReturnDraftBill.dao.deleteById(configName, draftBillId);
      BaseDbModel.delDetailByBillId(configName, "xs_return_draft_detail", draftBillId);
      PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(billTypeId));
      
      StockDraftRecords.delRecords(configName, billTypeId, draftBillId.intValue());
      setDraftStrs();
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "sell_return_info");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    printBeforSave1();
    
    renderJson();
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int orderId = getParaToInt(0, Integer.valueOf(0)).intValue();
    boolean isRCW = false;
    
    Model bill = SellReturnBill.dao.findById(configName, Integer.valueOf(orderId));
    Integer orderIsRedRow = bill.getInt("isRCW");
    if (orderIsRedRow == null) {
      orderIsRedRow = Integer.valueOf(0);
    }
    List<Model> detailList = SellReturnDetail.dao.getList2(configName, orderId, "xs_return_detail");
    
    detailList = addTrSize(detailList, 15);
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    if (orderIsRedRow.intValue() == AioConstants.RCW_VS) {
      isRCW = true;
    } else {
      isRCW = false;
    }
    setPayTypeAttr(configName, "saveBill", billTypeId, bill.getInt("id").intValue());
    
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("isRCW", Boolean.valueOf(isRCW));
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("showDiscount", Boolean.valueOf(BillRow.dao.showRow(configName, billTypeId, "discount")));
    render("/WEB-INF/template/sell/return/look.html");
  }
  
  public static Map<String, Object> doRCW(String configName, int billId)
    throws SQLException
  {
    Map<String, Object> map = new HashMap();
    int oldBillId = 0;
    int newBillId = 0;
    
    Model bill = SellReturnBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "销售退货单已经不存在！");
      return map;
    }
    BigDecimal privilegeMoney = bill.getBigDecimal("privilegeMoney");
    BigDecimal payMoney = bill.getBigDecimal("payMoney");
    Map<String, Object> vmap = BaseController.verifyUnitMaxGetMoney(configName, AioConstants.OPERTE_RCW, bill.getInt("unit.id"), AioConstants.WAY_PAY, payMoney, privilegeMoney);
    if (vmap != null)
    {
      map = vmap;
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      return map;
    }
    List<Model> dateilList = SellReturnDetail.dao.getList2(configName, billId, "xs_return_detail");
    if ((dateilList == null) || (dateilList.size() == 0))
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "销售退货单商品信息已经不存在！");
      return map;
    }
    for (int i = 0; i < dateilList.size(); i++)
    {
      Model detail = (Model)dateilList.get(i);
      Model product = (Model)detail.get("product");
      String fullName = product.getStr("fullName");
      int costArith = product.getInt("costArith").intValue();
      int productId = product.getInt("id").intValue();
      int detailStorageId = detail.getInt("storageId").intValue();
      BigDecimal costMoneys = detail.getBigDecimal("costMoneys");
      BigDecimal amount = detail.getBigDecimal("baseAmount");
      BigDecimal costPrice = BigDecimal.ZERO;
      if (BigDecimalUtils.compare(BigDecimal.ZERO, amount) != 0) {
        costPrice = BigDecimalUtils.div(costMoneys, amount);
      }
      BigDecimal sAmount = BigDecimal.ZERO;
      if (costArith == AioConstants.PRD_COST_PRICE4) {
        sAmount = Stock.dao.getStockAmount(configName, productId, detailStorageId, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
      } else {
        sAmount = Stock.dao.getStockAmount(configName, Integer.valueOf(productId), Integer.valueOf(detailStorageId));
      }
      if (BigDecimalUtils.compare(sAmount, amount) == -1)
      {
        if (!SysConfig.getConfig(configName, 1).booleanValue())
        {
          map.put("status", Integer.valueOf(AioConstants.RCW_NO));
          map.put("message", "【" + fullName + "】是" + ComFunController.getCostArithName(costArith) + ",不允许负库存！");
          return map;
        }
        if (costArith != AioConstants.PRD_COST_PRICE1)
        {
          map.put("status", Integer.valueOf(AioConstants.RCW_NO));
          map.put("message", "【" + fullName + "】是" + ComFunController.getCostArithName(costArith) + ",不允许负库存！");
          return map;
        }
      }
    }
    Date time = new Date();
    try
    {
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
      oldBillId = bill.getInt("id").intValue();
      bill.update(configName);
      bill.set("id", null);
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
      bill.set("updateTime", time);
      
      billRcwAddRemark(bill, null);
      bill.save(configName);
      newBillId = bill.getInt("id").intValue();
      
      BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("taxMoneys"));
      for (int i = 0; i < dateilList.size(); i++)
      {
        Model detail = (Model)dateilList.get(i);
        Product product = (Product)((Model)dateilList.get(i)).get("product");
        Integer storageId = detail.getInt("storageId");
        int productId = detail.getInt("productId").intValue();
        int costArith = product.getInt("costArith").intValue();
        BigDecimal litterAmount = detail.getBigDecimal("baseAmount");
        BigDecimal costMoneys = detail.getBigDecimal("costMoneys");
        BigDecimal litterCostPrice = BigDecimalUtils.div(costMoneys, litterAmount);
        
        detail.set("rcwId", detail.getInt("id"));
        detail.set("id", null);
        detail.set("billId", bill.getInt("id"));
        detail.save(configName);
        


        StockRecordsController.addRecords(configName, billTypeId, "out", bill, detail, litterCostPrice, litterAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
        
        AvgpriceConTroller.addAvgprice(configName, "out", storageId, Integer.valueOf(productId), litterAmount, costMoneys);
        
        StockController.stockSubChange(configName, costArith, litterAmount, storageId.intValue(), productId, litterCostPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"), null);
      }
      PayTypeController.updateUnitNeedGetOrOut(configName, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, bill.getInt("unitId").intValue(), bill.getBigDecimal("payMoney"), bill.getBigDecimal("privilegeMoney"));
      


      ArapRecordsController.addRecords(configName, true, AioConstants.OPERTE_RCW, AioConstants.PAY_TYLE0, Integer.valueOf(billTypeId), Integer.valueOf(billId), null, null, null, null, null, null, null);
      

      PayTypeController.rcwAccountsRecoder(configName, billTypeId, Integer.valueOf(oldBillId), Integer.valueOf(newBillId));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack(configName);
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "系统异常,请联系管理员！");
      return map;
    }
    map.put("status", Integer.valueOf(AioConstants.RCW_BY));
    return map;
  }
  
  public void print()
  {
    String configName = loginConfigName();
    
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "销售退货单");
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
      bill = SellReturnBill.dao.findById(configName, getParaToInt("bill.id", Integer.valueOf(0)));
      detailList = SellReturnDetail.dao.getList1(configName, bill.getInt("id").intValue());
      billId = bill.getInt("id");
    }
    else
    {
      bill = (Model)getModel(SellReturnBill.class);
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("staffId", getParaToInt("staff.id", Integer.valueOf(0)));
      bill.set("departmentId", getParaToInt("department.id", Integer.valueOf(0)));
      bill.set("storageId", getParaToInt("storage.id", Integer.valueOf(0)));
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), SellReturnDetail.class, "sellReturnDetail");
    }
    billData.put("tableName", "xs_return_bill");
    billData.put("base", getAttr("base"));
    Integer id = bill.getInt("id");
    if ((id != null) && (id.intValue() != 0)) {
      billData.put("id", id);
    }
    boolean flag = printBeforSave2(data, billId).booleanValue();
    if (flag)
    {
      renderJson(data);
      return;
    }
    Model unit = Unit.dao.findById(configName, bill.getInt("unitId"));
    Model staff = Staff.dao.findById(configName, bill.getInt("staffId"));
    Model department = Department.dao.findById(configName, bill.getInt("departmentId"));
    Model storage = Storage.dao.findById(configName, bill.getInt("storageId"));
    String unitFullName = unit == null ? "" : unit.getStr("fullName");
    String staffFullName = staff == null ? "" : staff.getStr("fullName");
    String departmentName = department == null ? "" : department.getStr("fullName");
    String storageFullName = storage == null ? "" : storage.getStr("fullName");
    


    printCommonData(headData);
    headData.add("录单日期:" + trimRecordDateNull(bill.getDate("recodeDate")));
    headData.add("单据编号:" + trimNull(bill.getStr("code")));
    headData.add("退货单位 :" + trimNull(unitFullName));
    printUnitCommonData(headData, unit);
    headData.add("经手人 :" + trimNull(staffFullName));
    headData.add("部门 :" + trimNull(departmentName));
    headData.add("交货日期:" + trimNull(bill.getDate("deliveryDate")));
    headData.add("收货仓库:" + trimNull(storageFullName));
    headData.add("摘要:" + trimNull(bill.getStr("remark")));
    headData.add("附加说明 :" + trimNull(bill.getStr("memo")));
    headData.add("整单折扣 :" + trimNull(bill.getBigDecimal("discounts")));
    headData.add("付款账户 :" + trimNull(bill.getStr("bankPay")));
    headData.add("付款金额 :" + trimNull(bill.getBigDecimal("payMoney")));
    headData.add("优惠金额 :" + trimNull(bill.getBigDecimal("privilege")));
    headData.add("优惠后金额 :" + trimNull(bill.getBigDecimal("privilegeMoney")));
    
    setOrderUserId(flag, bill, actionType, headData);
    

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
    
    colTitle.add("零售价");
    colTitle.add("零售金额");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Product p = (Product)Product.dao.findById(configName, detail.getInt("productId"));
      Integer selectUnitId = detail.getInt("selectUnitId");
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      Integer storageId = detail.getInt("storageId");
      if ((storageId == null) || (storageId.intValue() == 0)) {
        storageId = bill.getInt("storageId");
      }
      Storage s = (Storage)Storage.dao.findById(configName, storageId);
      
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(p.getStr("code")));
      rowData.add(trimNull(p.getStr("fullName")));
      
      rowData.add(trimNull(p.getStr("smallName")));
      rowData.add(trimNull(p.getStr("spell")));
      rowData.add(trimNull(p.getStr("standard")));
      rowData.add(trimNull(p.getStr("model")));
      rowData.add(trimNull(p.getStr("field")));
      
      rowData.add(trimNull(DwzUtils.getSelectUnit(p, selectUnitId)));
      rowData.add(DwzUtils.helpUnit(p.getStr("calculateUnit1"), p.getStr("calculateUnit2"), p.getStr("calculateUnit3"), p.getInt("unitRelation1"), p.getBigDecimal("unitRelation2"), p.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(detail.getDate("produceDate")));
      rowData.add(trimNull(detail.getDate("produceEndDate")));
      rowData.add(trimNull(detail.getStr("batch")));
      rowData.add(DwzUtils.helpAmount(detail.getBigDecimal("amount"), p.getStr("calculateUnit1"), p.getStr("calculateUnit2"), p.getStr("calculateUnit3"), p.getBigDecimal("unitRelation2"), p.getBigDecimal("unitRelation3")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, p.getInt("unitRelation1"), p.getBigDecimal("unitRelation2"), p.getBigDecimal("unitRelation3"), Integer.valueOf(1)) + trimNull(p.getStr("calculateUnit1")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, p.getInt("unitRelation1"), p.getBigDecimal("unitRelation2"), p.getBigDecimal("unitRelation3"), Integer.valueOf(2)) + trimNull(p.getStr("calculateUnit2")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, p.getInt("unitRelation1"), p.getBigDecimal("unitRelation2"), p.getBigDecimal("unitRelation3"), Integer.valueOf(3)) + trimNull(p.getStr("calculateUnit3")));
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
      rowData.add(trimNull(p.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(trimNull(detail.getStr("message")));
      



      ComFunController.setRetailPrice(rowData, detail, p);
      rowData.add(trimNull(s == null ? "" : s.getStr("code")));
      rowData.add(trimNull(s == null ? "" : s.getStr("fullName")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    data.put("billData", billData);
    renderJson(data);
  }
}
