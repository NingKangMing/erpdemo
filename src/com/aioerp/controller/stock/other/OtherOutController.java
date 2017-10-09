package com.aioerp.controller.stock.other;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AvgpriceConTroller;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.sell.sell.SellController;
import com.aioerp.controller.stock.StockController;
import com.aioerp.controller.stock.StockRecordsController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.stock.other.OtherOutBill;
import com.aioerp.model.stock.other.OtherOutDetail;
import com.aioerp.model.stock.other.OtherOutDraftBill;
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
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class OtherOutController
  extends BaseController
{
  public void index()
  {
    String configName = loginConfigName();
    billCodeAuto(AioConstants.BILL_ROW_TYPE11);
    setAttr("accountTypeId", "36");
    
    Record r = Db.use(configName).findFirst("select * from b_accounts where type='00038'");
    if (r != null)
    {
      setAttr("accountId", r.getInt("id"));
      setAttr("accountFullName", r.getStr("fullName"));
    }
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE11, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE11));
    
    loadOnlyOneBaseInfo(configName);
    

    notEditStaff();
    render("/WEB-INF/template/stock/other/otherout/add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    OtherOutBill bill = (OtherOutBill)getModel(OtherOutBill.class);
    

    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    String nagetiveStockHasComfirm = getPara("nagetiveStockHasComfirm", "nhas");
    
    boolean hasOther = OtherOutBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE11));
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Date recodeDate = bill.getTimestamp("recodeDate");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    boolean unitFlag = true;
    if (unitId == 0) {
      unitFlag = false;
    }
    List<Model> detail = ModelUtils.batchInjectSortObjModel(getRequest(), OtherOutDetail.class, "otherOutDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detail.size() == 0)
    {
      setAttr("message", "请选择要商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    boolean storageFlag = false;
    if (storageId > 0) {
      storageFlag = true;
    }
    Map<Integer, Product> productMap = new HashMap();
    for (int i = 0; i < detail.size(); i++)
    {
      Model sellDetail = (Model)detail.get(i);
      Integer storageIdStr = sellDetail.getInt("storageId");
      Integer nstorageId = Integer.valueOf(storageIdStr == null ? 0 : new Integer(storageIdStr.intValue()).intValue());
      String trIndex = String.valueOf(StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1));
      if (!storageFlag)
      {
        if (nstorageId.intValue() < 1)
        {
          setAttr("message", "第" + trIndex + "行多仓库录入错误!");
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
        }
      }
      else if (nstorageId.intValue() < 1) {
        ((Model)detail.get(i)).set("storageId", Integer.valueOf(storageId));
      }
      Integer productId = sellDetail.getInt("productId");
      Product product = (Product)Product.dao.findById(configName, productId);
      productMap.put(productId, product);
      Integer selectUnitId = sellDetail.getInt("selectUnitId");
      BigDecimal sellAmount = sellDetail.getBigDecimal("amount");
      if (BigDecimalUtils.compare(sellAmount, BigDecimal.ZERO) < 1)
      {
        setAttr("message", "第" + trIndex + "行数量录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      BigDecimal price = sellDetail.getBigDecimal("price");
      
      BigDecimal total = DwzUtils.getConversionAmount(sellAmount, selectUnitId, product, Integer.valueOf(1));
      

















      ((Model)detail.get(i)).set("baseAmount", total);
      
      ((Model)detail.get(i)).put("trIndex", trIndex);
    }
    List<Model> oldDetail = (List)DeepClone.deepClone(detail);
    

    detail = ComFunController.productMerge(detail, productMap);
    


    String trIndexs = "";
    String productIds = "";
    String selectUnitIds = "";
    String storageIds = "";
    String stockAmounts = "";
    String negativeStockAmounts = "";
    
    String costAriths = "";
    String prdUnitNames = "";
    String notStocks = "";
    





    Model sellDetail = null;
    







    Boolean isNegativeStock = SysConfig.getConfig(configName, 1);
    
    boolean nagetiveStockFlag = false;
    for (int i = 0; i < detail.size(); i++)
    {
      sellDetail = (Model)detail.get(i);
      int productIdi = sellDetail.getInt("productId").intValue();
      int storageIdi = sellDetail.getInt("storageId").intValue();
      
      BigDecimal costPrice = ((HelpUtil)helpUitlList.get(i)).getCostPrice();
      String batchNum = sellDetail.getStr("batch");
      Date produceDate = sellDetail.getDate("produceDate");
      Date produceEndDate = sellDetail.getDate("produceEndDate");
      BigDecimal amount = sellDetail.getBigDecimal("baseAmount");
      BigDecimal samount = BigDecimal.ZERO;
      Product product = (Product)productMap.get(Integer.valueOf(productIdi));
      int costArith = product.getInt("costArith").intValue();
      Integer selectUnitId = sellDetail.getInt("selectUnitId");
      String selectUnitName = "";
      

      int notStockFlag = 0;
      if (costArith == AioConstants.PRD_COST_PRICE4)
      {
        samount = Stock.dao.getStockAmount(configName, productIdi, storageIdi, costPrice, batchNum, produceDate, produceEndDate);
        
        sellDetail.set("basePrice", costPrice);
        
        SellController.setCostPrice(oldDetail, productIdi, "basePrice", costPrice);
      }
      else if (costArith == AioConstants.PRD_COST_PRICE1)
      {
        samount = Stock.dao.getStockAmount(configName, Integer.valueOf(productIdi), Integer.valueOf(storageIdi));
        if (costPrice == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, storageId, product, costPrice, selectUnitId.intValue());
          notStockFlag = Integer.valueOf((String)map.get("notStockFlag")).intValue();
          costPrice = new BigDecimal((String)map.get("costPrice"));
          sellDetail.set("basePrice", costPrice);
          SellController.setCostPrice(oldDetail, productIdi, "basePrice", costPrice);
        }
      }
      if (samount == null) {
        samount = BigDecimal.ZERO;
      }
      if (BigDecimalUtils.compare(samount, amount) < 0) {
        if ((isNegativeStock.booleanValue()) && (costArith == AioConstants.PRD_COST_PRICE1))
        {
          trIndexs = trIndexs + ":" + sellDetail.getStr("trIndex");
          productIds = productIds + ":" + productIdi;
          selectUnitIds = selectUnitIds + ":" + selectUnitId;
          storageIds = storageIds + ":" + storageIdi;
          stockAmounts = stockAmounts + ":" + samount;
          negativeStockAmounts = negativeStockAmounts + ":" + BigDecimalUtils.sub(samount, amount);
          costAriths = costAriths + ":" + costArith;
          if (selectUnitId.intValue() == 1) {
            selectUnitName = product.getStr("calculateUnit1");
          } else if (selectUnitId.intValue() == 2) {
            selectUnitName = product.getStr("calculateUnit2");
          } else if (selectUnitId.intValue() == 3) {
            selectUnitName = product.getStr("calculateUnit3");
          }
          selectUnitName = selectUnitName == null ? " " : selectUnitName;
          prdUnitNames = prdUnitNames + ":" + URLEncoder.encode(selectUnitName, "utf-8");
          if (notStockFlag == 0) {
            notStocks = notStocks + ":0";
          } else if (notStockFlag == 1) {
            notStocks = notStocks + ":1";
          } else if (notStockFlag == 2) {
            notStocks = notStocks + ":2";
          }
          nagetiveStockFlag = true;
        }
        else
        {
          setAttr("message", "第" + sellDetail.getStr("trIndex") + "行商品<br/>商品编号：" + product.getStr("code") + ",<br/>商品全名：" + product.getStr("fullName") + ",<br/>数量" + amount + ",超出库存" + samount + "!");
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
      }
      ((Model)detail.get(i)).remove("trIndex");
    }
    if (("nhas".equals(nagetiveStockHasComfirm)) && 
      (isNegativeStock.booleanValue()) && (nagetiveStockFlag))
    {
      if (SysConfig.getConfig(configName, 15).booleanValue())
      {
        Map<String, Object> map = new HashMap();
        map.put("statusCode", AioConstants.HTTP_RETURN200);
        map.put("unitFlag", Boolean.valueOf(unitFlag));
        map.put("confirmMsg", "往来单位为空，是否继续?");
        map.put("dialog", Boolean.valueOf(true));
        map.put("url", getAttr("base") + "/sell/sell/negativeStockShowDialog/");
        map.put("dialogTitle", "负库存");
        map.put("dialogId", "xsd_nagetive_info");
        map.put("width", "600");
        map.put("height", "350");
        if (!trIndexs.equals("")) {
          trIndexs = trIndexs.substring(1, trIndexs.length());
        }
        if (!productIds.equals("")) {
          productIds = productIds.substring(1, productIds.length());
        }
        if (!selectUnitIds.equals("")) {
          selectUnitIds = selectUnitIds.substring(1, selectUnitIds.length());
        }
        if (!storageIds.equals("")) {
          storageIds = storageIds.substring(1, storageIds.length());
        }
        if (!stockAmounts.equals("")) {
          stockAmounts = stockAmounts.substring(1, stockAmounts.length());
        }
        if (!negativeStockAmounts.equals("")) {
          negativeStockAmounts = negativeStockAmounts.substring(1, negativeStockAmounts.length());
        }
        if (!costAriths.equals("")) {
          costAriths = costAriths.substring(1, costAriths.length());
        }
        if (!prdUnitNames.equals("")) {
          prdUnitNames = prdUnitNames.substring(1, prdUnitNames.length());
        }
        if (!notStocks.equals("")) {
          notStocks = notStocks.substring(1, notStocks.length());
        }
        Map<String, Object> mapParas = new HashMap();
        mapParas.put("modelType", "otherout");
        mapParas.put("trIndexs", trIndexs);
        mapParas.put("productIds", productIds);
        mapParas.put("selectUnitIds", selectUnitIds);
        mapParas.put("storageIds", storageIds);
        mapParas.put("stockAmounts", stockAmounts);
        mapParas.put("negativeStockAmounts", negativeStockAmounts);
        mapParas.put("costAriths", costAriths);
        mapParas.put("prdUnitNames", prdUnitNames);
        mapParas.put("notStocks", notStocks);
        map.put("params", mapParas);
        renderJson(map);
        return;
      }
      if (!unitFlag)
      {
        Map<String, Object> map = new HashMap();
        map.put("statusCode", AioConstants.HTTP_RETURN200);
        map.put("unitFlag", Boolean.valueOf(unitFlag));
        map.put("confirmMsg", "往来单位为空，是否继续?");
        map.put("stockHasComfirmId", "nagetiveStockHasComfirmId");
        map.put("fromId", "cc_qtckdForm");
        map.put("dialog", Boolean.valueOf(false));
        renderJson(map);
        return;
      }
    }
    bill.set("unitId", Integer.valueOf(unitId));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("storageId", Integer.valueOf(storageId));
    bill.set("accountsId", getParaToInt("account.id"));
    bill.set("status", Integer.valueOf(AioConstants.BILL_STATUS3));
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("userId", Integer.valueOf(loginUserId()));
    
    bill.set("id", null);
    if (draftId != 0) {
      billCodeIncrease(bill, "draftPost");
    } else {
      billCodeIncrease(bill, "add");
    }
    bill.save(configName);
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE11, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    

    BillHistoryController.saveBillHistory(configName, bill, AioConstants.BILL_ROW_TYPE11, bill.getBigDecimal("moneys"));
    
    BigDecimal billMoneys = BigDecimal.ZERO;
    List<Record> proAccountList = new ArrayList();
    for (int i = 0; i < oldDetail.size(); i++)
    {
      Record proAccount = new Record();
      Model otherOutDetail1 = (Model)oldDetail.get(i);
      otherOutDetail1.set("id", null);
      int productIdi = otherOutDetail1.getInt("productId").intValue();
      Product producti = (Product)productMap.get(Integer.valueOf(productIdi));
      int costArith = producti.getInt("costArith").intValue();
      int storageIdi = otherOutDetail1.getInt("storageId").intValue();
      BigDecimal sellAmount = otherOutDetail1.getBigDecimal("baseAmount");
      String batchNum = otherOutDetail1.getStr("batch");
      Date produceDate = otherOutDetail1.getDate("produceDate");
      BigDecimal litterAmount = sellAmount;
      BigDecimal costPrice = otherOutDetail1.getBigDecimal("basePrice");
      BigDecimal costMoneys = BigDecimalUtils.mul(costPrice, sellAmount);
      otherOutDetail1.set("billId", bill.getInt("id"));
      otherOutDetail1.set("updateTime", time);
      
      otherOutDetail1.save(configName);
      billMoneys = BigDecimalUtils.add(billMoneys, costMoneys);
      

      StockRecordsController.addRecords(configName, AioConstants.BILL_ROW_TYPE11, "out", bill, otherOutDetail1, BigDecimalUtils.div(costMoneys, sellAmount), litterAmount, time, bill.getTimestamp("recodeDate"), otherOutDetail1.getInt("storageId"));
      

      AvgpriceConTroller.addAvgprice(configName, "out", Integer.valueOf(storageIdi), Integer.valueOf(productIdi), litterAmount, costMoneys);
      

      StockController.stockSubChange(configName, costArith, litterAmount, storageIdi, productIdi, costPrice, otherOutDetail1.getStr("batch"), otherOutDetail1.getDate("produceDate"), otherOutDetail1.getDate("produceEndDate"), null);
      
      proAccount.set("productId", Integer.valueOf(productIdi));
      proAccount.set("costMoney", costMoneys);
      proAccountList.add(proAccount);
    }
    Map<String, Object> record = new HashMap();
    record.put("accountId", bill.getInt("accountsId"));
    record.put("billMoneys", billMoneys);
    record.put("proAccountList", proAccountList);
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, AioConstants.BILL_ROW_TYPE11, bill.getInt("id").intValue(), Integer.valueOf(unitId), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), null, null, null, record);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      OtherOutDraftBill.dao.deleteById(configName, draftBillId);
      BaseDbModel.delDetailByBillId(configName, "cc_otherout_draft_detail", draftBillId);
      
      setDraftStrs();
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "stockotherout");
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
    
    Model bill = OtherOutBill.dao.findById(configName, Integer.valueOf(orderId));
    Integer orderIsRedRow = bill.getInt("isRCW");
    if (orderIsRedRow == null) {
      orderIsRedRow = Integer.valueOf(0);
    }
    List<Model> detailList = OtherOutDetail.dao.getList2(configName, Integer.valueOf(orderId), "cc_otherout_detail");
    
    detailList = addTrSize(detailList, 15);
    List<Model> rowList = BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE11, AioConstants.STATUS_ENABLE);
    if (orderIsRedRow.intValue() == AioConstants.RCW_VS) {
      isRCW = true;
    } else {
      isRCW = false;
    }
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE11, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("isRCW", Boolean.valueOf(isRCW));
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE11));
    render("/WEB-INF/template/stock/other/otherout/look.html");
  }
  
  public static Map<String, Object> doRCW(String configName, int billId)
    throws SQLException
  {
    Map<String, Object> map = new HashMap();
    int oldBillId = 0;
    int newBillId = 0;
    

    Model bill = OtherOutBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "单据已经不存在！");
      return map;
    }
    List<Model> dateilList = OtherOutDetail.dao.getList2(configName, Integer.valueOf(billId), "cc_otherout_detail");
    if ((dateilList == null) || (dateilList.size() == 0))
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "单据商品信息已经不存在！");
      return map;
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
      
      BillHistoryController.saveBillHistory(configName, bill, AioConstants.BILL_ROW_TYPE11, bill.getBigDecimal("moneys"));
      for (int i = 0; i < dateilList.size(); i++)
      {
        Model detail = (Model)dateilList.get(i);
        Product product = (Product)((Model)dateilList.get(i)).get("product");
        Integer storageId = detail.getInt("storageId");
        int productId = detail.getInt("productId").intValue();
        BigDecimal litterAmount = detail.getBigDecimal("baseAmount");
        BigDecimal litterCostPrice = detail.getBigDecimal("basePrice");
        BigDecimal costMoneys = BigDecimalUtils.mul(litterAmount, litterCostPrice);
        
        detail.set("rcwId", detail.getInt("id"));
        detail.set("id", null);
        detail.set("billId", bill.getInt("id"));
        detail.save(configName);
        


        StockRecordsController.addRecords(configName, AioConstants.BILL_ROW_TYPE11, "in", bill, detail, litterCostPrice, litterAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
        
        StockController.stockChange(configName, "in", storageId.intValue(), productId, product.getInt("costArith").intValue(), litterAmount, litterCostPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        
        AvgpriceConTroller.addAvgprice(configName, "in", storageId, Integer.valueOf(productId), litterAmount, costMoneys);
      }
      PayTypeController.rcwAccountsRecoder(configName, AioConstants.BILL_ROW_TYPE11, Integer.valueOf(oldBillId), Integer.valueOf(newBillId));
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
    data.put("reportName", "其它出库单");
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
      bill = OtherOutBill.dao.findById(configName, getParaToInt("bill.id", Integer.valueOf(0)));
      detailList = OtherOutDetail.dao.getList1(configName, bill.getInt("id").intValue());
      billId = bill.getInt("id");
    }
    else
    {
      bill = (Model)getModel(OtherOutBill.class);
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("staffId", getParaToInt("staff.id", Integer.valueOf(0)));
      bill.set("departmentId", getParaToInt("department.id", Integer.valueOf(0)));
      bill.set("accountsId", getParaToInt("account.id", Integer.valueOf(0)));
      bill.set("storageId", getParaToInt("storage.id", Integer.valueOf(0)));
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), OtherOutDetail.class, "otherOutDetail");
    }
    billData.put("tableName", "cc_otherout_bill");
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
    Model accounts = Department.dao.findById(configName, bill.getInt("accountsId"));
    Model storage = Storage.dao.findById(configName, bill.getInt("storageId"));
    String unitFullName = unit == null ? "" : unit.getStr("fullName");
    String staffFullName = staff == null ? "" : staff.getStr("fullName");
    String departmentName = department == null ? "" : department.getStr("fullName");
    String accountsName = accounts == null ? "" : accounts.getStr("fullName");
    String storageFullName = storage == null ? "" : storage.getStr("fullName");
    


    printCommonData(headData);
    headData.add("录单日期:" + trimRecordDateNull(bill.getDate("recodeDate")));
    headData.add("单据编号:" + trimNull(bill.getStr("code")));
    headData.add("收货单位 :" + trimNull(unitFullName));
    printUnitCommonData(headData, unit);
    headData.add("经手人 :" + trimNull(staffFullName));
    headData.add("部门 :" + trimNull(departmentName));
    headData.add("会计科目:" + trimNull(accountsName));
    headData.add("发货仓库:" + trimNull(storageFullName));
    headData.add("摘要:" + trimNull(bill.getStr("remark")));
    headData.add("附加说明 :" + trimNull(bill.getStr("memo")));
    
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
    colTitle.add("备注");
    colTitle.add("状态");
    colTitle.add("条码");
    colTitle.add("附加信息");
    
    colTitle.add("零售价");
    colTitle.add("零售金额");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    
    boolean hasCostPermitted = hasPermitted("1101-s");
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
      rowData.add(trimNull(detail.getBigDecimal("price"), hasCostPermitted));
      rowData.add(trimNull(detail.getBigDecimal("money"), hasCostPermitted));
      rowData.add(trimNull(detail.getStr("memo")));
      rowData.add(status);
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
