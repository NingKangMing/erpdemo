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
import com.aioerp.model.stock.other.OtherInBill;
import com.aioerp.model.stock.other.OtherInDetail;
import com.aioerp.model.stock.other.OtherInDraftBill;
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

public class OtherInController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE10;
  
  public void index()
  {
    String configName = loginConfigName();
    billCodeAuto(billTypeId);
    setAttr("accountTypeId", "21");
    
    Record r = Db.use(configName).findFirst("select * from b_accounts where type='00024'");
    if (r != null)
    {
      setAttr("accountId", r.getInt("id"));
      setAttr("accountFullName", r.getStr("fullName"));
    }
    setAttr("today", DateUtils.getCurrentDate());
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    
    loadOnlyOneBaseInfo(configName);
    

    notEditStaff();
    render("/WEB-INF/template/stock/other/otherin/add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws Exception
  {
    String configName = loginConfigName();
    OtherInBill bill = (OtherInBill)getModel(OtherInBill.class);
    

    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    String priceStockHasComfirmId = getPara("priceStockHasComfirmId", "nhas");
    
    boolean hasOther = OtherInBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(billTypeId));
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
    List<Model> detail = ModelUtils.batchInjectSortObjModel(getRequest(), OtherInDetail.class, "otherInDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    List<Model> oldDetail = new ArrayList();
    if (detail.size() == 0)
    {
      setAttr("message", "请选择要商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    int unitId = getParaToInt("unit.id", Integer.valueOf(0)).intValue();
    int storageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    boolean storageFlag = false;
    if (storageId > 0) {
      storageFlag = true;
    }
    boolean unitFlag = true;
    if (unitId == 0) {
      unitFlag = false;
    }
    Map<Integer, Product> productMap = new HashMap();
    for (int i = 0; i < detail.size(); i++)
    {
      Model otherinDetail = (Model)detail.get(i);
      Integer productId = otherinDetail.getInt("productId");
      Product product = (Product)Product.dao.findById(configName, productId);
      productMap.put(productId, product);
      
      String trIndex = String.valueOf(StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1));
      Integer storageIdStr = otherinDetail.getInt("storageId");
      Integer nstorageId = Integer.valueOf(storageIdStr == null ? 0 : new Integer(storageIdStr.intValue()).intValue());
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
      BigDecimal returnAmount = otherinDetail.getBigDecimal("amount");
      if (BigDecimalUtils.compare(returnAmount, BigDecimal.ZERO) < 1)
      {
        setAttr("message", "第" + trIndex + "行数量录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      ((Model)detail.get(i)).put("trIndex", trIndex);
    }
    oldDetail = (List)DeepClone.deepClone(detail);
    


    detail = ComFunController.productMerge(detail, productMap);
    


    String trIndexs = "";
    String productIds = "";
    String selectUnitIds = "";
    String productCodes = "";
    String productFullNames = "";
    String storageIds = "";
    
    String costAriths = "";
    String prdUnitNames = "";
    String notStocks = "";
    












    boolean priceStockFlag = false;
    for (int i = 0; i < detail.size(); i++)
    {
      Model otherinDetail = (Model)detail.get(i);
      int storageIdi = otherinDetail.getInt("storageId").intValue();
      int productIdi = otherinDetail.getInt("productId").intValue();
      Product product = (Product)productMap.get(Integer.valueOf(productIdi));
      int costArith = product.getInt("costArith").intValue();
      String code = product.getStr("code");
      String fullName = product.getStr("fullName");
      String batchNum = otherinDetail.getStr("batch");
      Date produceDate = otherinDetail.getDate("produceDate");
      Date produceEndDate = otherinDetail.getDate("produceEndDate");
      int selectUnitId = otherinDetail.getInt("selectUnitId").intValue();
      BigDecimal samount = BigDecimal.ZERO;
      
      BigDecimal costPrice = ((HelpUtil)helpUitlList.get(Integer.valueOf(otherinDetail.getStr("trIndex").split(",")[0]).intValue() - 1)).getCostPrice();
      if (BigDecimalUtils.compare(costPrice, BigDecimal.ZERO) == 0)
      {
        boolean hasLastInPrice = false;
        int notStockFlag = 0;
        if (costArith == AioConstants.PRD_COST_PRICE4)
        {
          samount = Stock.dao.getStockAmount(configName, productIdi, storageIdi, costPrice, batchNum, produceDate, produceEndDate);
        }
        else
        {
          samount = Stock.dao.getStockAmount(configName, Integer.valueOf(productIdi), Integer.valueOf(storageIdi));
          
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, storageId, product, costPrice, selectUnitId);
          costPrice = new BigDecimal((String)map.get("costPrice"));
          SellController.setTrIndexHelpCostPrice(helpUitlList, otherinDetail.getStr("trIndex"), costPrice);
          hasLastInPrice = true;
        }
        if ((BigDecimalUtils.compare(samount, BigDecimal.ZERO) == 0) && (!hasLastInPrice))
        {
          notStockFlag = 2;
          trIndexs = trIndexs + ":" + otherinDetail.getStr("trIndex");
          productIds = productIds + ":" + productIdi;
          productCodes = productCodes + ":" + URLEncoder.encode(code, "utf-8");
          productFullNames = productFullNames + ":" + URLEncoder.encode(fullName, "utf-8");
          storageIds = storageIds + ":" + storageIdi;
          costAriths = costAriths + ":" + costArith;
          String selectUnitName = "";
          if (selectUnitId == 1) {
            selectUnitName = product.getStr("calculateUnit1");
          } else if (selectUnitId == 2) {
            selectUnitName = product.getStr("calculateUnit2");
          } else if (selectUnitId == 3) {
            selectUnitName = product.getStr("calculateUnit3");
          }
          selectUnitIds = selectUnitIds + ":" + selectUnitId;
          selectUnitName = selectUnitName == null ? " " : selectUnitName;
          prdUnitNames = prdUnitNames + ":" + URLEncoder.encode(selectUnitName, "utf-8");
          if (notStockFlag == 0) {
            notStocks = notStocks + ":0";
          } else if (notStockFlag == 2) {
            notStocks = notStocks + ":2";
          }
          priceStockFlag = true;
        }
        ((Model)detail.get(i)).remove("trIndex");
      }
    }
    if ((priceStockHasComfirmId.equals("nhas")) && (priceStockFlag))
    {
      Map<String, Object> map = new HashMap();
      map.put("statusCode", AioConstants.HTTP_RETURN200);
      map.put("unitFlag", Boolean.valueOf(unitFlag));
      map.put("confirmMsg", "往来单位为空，是否继续?");
      map.put("dialog", Boolean.valueOf(true));
      map.put("url", getAttr("base") + "/sell/sell/getNagetiveStockPrice/");
      map.put("dialogTitle", "商品成本价格");
      map.put("dialogId", "xsd_nagetive_price_info");
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
      if (!productCodes.equals("")) {
        productCodes = productCodes.substring(1, productCodes.length());
      }
      if (!productFullNames.equals("")) {
        productFullNames = productFullNames.substring(1, productFullNames.length());
      }
      if (!storageIds.equals("")) {
        storageIds = storageIds.substring(1, storageIds.length());
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
      mapParas.put("modelType", "otherin");
      mapParas.put("trIndexs", trIndexs);
      mapParas.put("productIds", productIds);
      mapParas.put("selectUnitIds", selectUnitIds);
      mapParas.put("prdCodes", productCodes);
      mapParas.put("prdFullNames", productFullNames);
      mapParas.put("storageIds", storageIds);
      mapParas.put("costAriths", costAriths);
      mapParas.put("prdUnitNames", prdUnitNames);
      mapParas.put("notStocks", notStocks);
      map.put("params", mapParas);
      renderJson(map);
      return;
    }
    if ((priceStockHasComfirmId.equals("nhas")) && (!unitFlag))
    {
      Map<String, Object> map = new HashMap();
      map.put("statusCode", AioConstants.HTTP_RETURN200);
      map.put("unitFlag", Boolean.valueOf(unitFlag));
      map.put("confirmMsg", "往来单位为空，是否继续?");
      map.put("stockHasComfirmId", "priceStockHasComfirmId");
      map.put("fromId", "qtjhd_otherinForm");
      map.put("dialog", Boolean.valueOf(false));
      renderJson(map);
      return;
    }
    bill.set("unitId", Integer.valueOf(unitId));
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("storageId", Integer.valueOf(storageId));
    int accountId = getParaToInt("account.id").intValue();
    bill.set("accountsId", Integer.valueOf(accountId));
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
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    
    BigDecimal billMoneys = BigDecimal.ZERO;
    List<Record> proAccountList = new ArrayList();
    
    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
    for (int i = 0; i < oldDetail.size(); i++)
    {
      Record proAccount = new Record();
      Model otherinDetail = (Model)oldDetail.get(i);
      otherinDetail.set("id", null);
      int curStorageId = otherinDetail.getInt("storageId").intValue();
      int productIdi = otherinDetail.getInt("productId").intValue();
      Product product = (Product)productMap.get(Integer.valueOf(productIdi));
      BigDecimal sellReturnAmount = otherinDetail.getBigDecimal("amount");
      BigDecimal costPrice = ((HelpUtil)helpUitlList.get(i)).getCostPrice();
      int selectUnitId = otherinDetail.getInt("selectUnitId").intValue();
      BigDecimal realLitterPrice = costPrice;
      int costArith = product.getInt("costArith").intValue();
      BigDecimal litterAmount = DwzUtils.getConversionAmount(sellReturnAmount, Integer.valueOf(selectUnitId), product, Integer.valueOf(1));
      otherinDetail.set("baseAmount", litterAmount);
      
















      costPrice = DwzUtils.getConversionPrice(realLitterPrice, Integer.valueOf(1), product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(selectUnitId));
      
      billMoneys = BigDecimalUtils.add(billMoneys, BigDecimalUtils.mul(costPrice, sellReturnAmount));
      BigDecimal costMoneys = BigDecimalUtils.mul(realLitterPrice, litterAmount);
      otherinDetail.set("price", costPrice);
      otherinDetail.set("basePrice", realLitterPrice);
      otherinDetail.set("money", costMoneys);
      otherinDetail.set("billId", bill.getInt("id"));
      otherinDetail.set("updateTime", time);
      
      otherinDetail.save(configName);
      

      StockRecordsController.addRecords(configName, billTypeId, "in", bill, otherinDetail, realLitterPrice, litterAmount, time, bill.getTimestamp("recodeDate"), otherinDetail.getInt("storageId"));
      

      StockController.stockChange(configName, "in", storageId, productIdi, product.getInt("costArith").intValue(), litterAmount, realLitterPrice, otherinDetail.getStr("batch"), otherinDetail.getDate("produceDate"), otherinDetail.getDate("produceEndDate"));
      
      AvgpriceConTroller.addAvgprice(configName, "in", Integer.valueOf(curStorageId), Integer.valueOf(productIdi), litterAmount, costMoneys);
      
      proAccount.set("productId", Integer.valueOf(productIdi));
      proAccount.set("costMoney", costMoneys);
      proAccountList.add(proAccount);
    }
    bill.set("moneys", billMoneys).update(configName);
    

    Map<String, Object> record = new HashMap();
    record.put("accountId", Integer.valueOf(accountId));
    record.put("billMoneys", billMoneys);
    record.put("proAccountList", proAccountList);
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), Integer.valueOf(unitId), getParaToInt("staff.id", Integer.valueOf(0)), getParaToInt("department.id", Integer.valueOf(0)), null, null, null, record);
    

    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      OtherInDraftBill.dao.deleteById(configName, draftBillId);
      BaseDbModel.delDetailByBillId(configName, "cc_otherin_draft_detail", draftBillId);
      
      setDraftStrs();
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "stockotherin");
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
    
    Model bill = OtherInBill.dao.findById(configName, Integer.valueOf(orderId));
    Integer orderIsRedRow = bill.getInt("isRCW");
    if (orderIsRedRow == null) {
      orderIsRedRow = Integer.valueOf(0);
    }
    List<Model> detailList = OtherInDetail.dao.getList2(configName, Integer.valueOf(orderId), "cc_otherin_detail");
    
    detailList = addTrSize(detailList, 15);
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    if (orderIsRedRow.intValue() == AioConstants.RCW_VS) {
      isRCW = true;
    } else {
      isRCW = false;
    }
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("isRCW", Boolean.valueOf(isRCW));
    setAttr("tableId", Integer.valueOf(billTypeId));
    render("/WEB-INF/template/stock/other/otherin/look.html");
  }
  
  public static Map<String, Object> doRCW(String configName, int billId)
    throws SQLException
  {
    Map<String, Object> map = new HashMap();
    int oldBillId = 0;
    int newBillId = 0;
    


    Model bill = OtherInBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "单据已经不存在！");
      return map;
    }
    List<Model> dateilList = OtherInDetail.dao.getList2(configName, Integer.valueOf(billId), "cc_otherin_detail");
    if ((dateilList == null) || (dateilList.size() == 0))
    {
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "单据商品信息已经不存在！");
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
      BigDecimal amount = detail.getBigDecimal("baseAmount");
      BigDecimal costPrice = detail.getBigDecimal("basePrice");
      
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
      
      BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
      for (int i = 0; i < dateilList.size(); i++)
      {
        Model detail = (Model)dateilList.get(i);
        Product product = (Product)((Model)dateilList.get(i)).get("product");
        Integer storageId = detail.getInt("storageId");
        int productId = detail.getInt("productId").intValue();
        BigDecimal litterAmount = detail.getBigDecimal("baseAmount");
        BigDecimal litterCostPrice = detail.getBigDecimal("basePrice");
        
        detail.set("rcwId", detail.getInt("id"));
        detail.set("id", null);
        detail.set("billId", bill.getInt("id"));
        detail.save(configName);
        


        StockRecordsController.addRecords(configName, billTypeId, "out", bill, detail, litterCostPrice, litterAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
        
        StockController.stockChange(configName, "out", storageId.intValue(), productId, product.getInt("costArith").intValue(), litterAmount, litterCostPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        
        AvgpriceConTroller.addAvgprice(configName, "out", storageId, Integer.valueOf(productId), litterAmount, BigDecimalUtils.mul(litterAmount, litterCostPrice));
      }
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
    data.put("reportName", "其它入库单");
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
      bill = OtherInBill.dao.findById(configName, getParaToInt("bill.id", Integer.valueOf(0)));
      detailList = OtherInDetail.dao.getList1(configName, bill.getInt("id").intValue());
      billId = bill.getInt("id");
    }
    else
    {
      bill = (Model)getModel(OtherInBill.class);
      bill.set("unitId", getParaToInt("unit.id", Integer.valueOf(0)));
      bill.set("staffId", getParaToInt("staff.id", Integer.valueOf(0)));
      bill.set("departmentId", getParaToInt("department.id", Integer.valueOf(0)));
      bill.set("accountsId", getParaToInt("account.id", Integer.valueOf(0)));
      bill.set("storageId", getParaToInt("storage.id", Integer.valueOf(0)));
      detailList = ModelUtils.batchInjectSortObjModel(getRequest(), OtherInDetail.class, "otherInDetail");
    }
    billData.put("tableName", "cc_otherin_bill");
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
    headData.add("往来单位 :" + trimNull(unitFullName));
    printUnitCommonData(headData, unit);
    headData.add("经手人 :" + trimNull(staffFullName));
    headData.add("部门 :" + trimNull(departmentName));
    headData.add("会计科目:" + trimNull(accountsName));
    headData.add("收货仓库:" + trimNull(storageFullName));
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
