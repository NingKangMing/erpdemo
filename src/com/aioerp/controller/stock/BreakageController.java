package com.aioerp.controller.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AvgpriceConTroller;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.bought.ResultHelp;
import com.aioerp.model.stock.BreakageBill;
import com.aioerp.model.stock.BreakageDetail;
import com.aioerp.model.stock.BreakageDraftBill;
import com.aioerp.model.stock.BreakageDraftDetail;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.stock.StockDraftRecords;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;

public class BreakageController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE8;
  
  public void index()
  {
    String configName = loginConfigName();
    
    billCodeAuto(billTypeId);
    
    setAttr("recodeTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    
    loadOnlyOneBaseInfo(configName);
    

    notEditStaff();
    render("page.html");
  }
  
  @Before({Tx.class})
  public void add()
  {
    String configName = loginConfigName();
    BreakageBill bill = (BreakageBill)getModel(BreakageBill.class);
    
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    Integer billId = bill.getInt("id");
    if (billId == null) {
      billId = Integer.valueOf(0);
    }
    boolean comfirm = getParaToBoolean("needComfirm", Boolean.valueOf(true)).booleanValue();
    
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    Date recodeDate = bill.getTimestamp("recodeDate");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    boolean hasOther = BreakageBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(billTypeId));
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), BreakageDetail.class, "brekageBillDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    BigDecimal amountTotal = new BigDecimal(0);
    BigDecimal moneysTotal = new BigDecimal(0);
    

    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    




    Boolean isNegativeStock = SysConfig.getConfig(configName, 1);
    List<ResultHelp> rList = new ArrayList();
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      BigDecimal amount = detail.getBigDecimal("amount");
      if ((detail.getInt("storageId") == null) || (detail.getInt("storageId").intValue() == 0)) {
        detail.set("storageId", storageId);
      }
      Integer detailSgeId = detail.getInt("storageId");
      Integer productId = detail.getInt("productId");
      

      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1);
      if (detailSgeId.intValue() < 1)
      {
        setAttr("message", "第" + trIndex + "行仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) != 1)
      {
        setAttr("message", "第" + trIndex + "行数量录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (productId.intValue() != 0)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        
        Integer selectUnitId = detail.getInt("selectUnitId");
        String selectUnit = product.getStr("calculateUnit1");
        if (2 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit2");
        } else if (3 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit3");
        }
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, product, Integer.valueOf(1));
        detail.set("baseAmount", baseAmount);
        
        Integer costArith = product.getInt("costArith");
        BigDecimal costPrice = ((HelpUtil)helpUitlList.get(i)).getCostPrice();
        BigDecimal sckAmount;
      
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4)
        {
          sckAmount = Stock.dao.getStockAmount(configName, productId.intValue(), detailSgeId.intValue(), costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        }
        else
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, storageId.intValue(), product, costPrice, selectUnitId.intValue());
          sckAmount = new BigDecimal((String)map.get("stockAmount"));
          costPrice = new BigDecimal((String)map.get("costPrice"));
          detail.set("basePrice", costPrice);
        }
        BigDecimal price = DwzUtils.getConversionPrice(costPrice, Integer.valueOf(1), Integer.valueOf(1), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), selectUnitId);
        detail.set("price", price);
        BigDecimal money = BigDecimalUtils.mul(amount, price).setScale(4, 4);
        detail.set("money", money);
        if ((comfirm) && (isNegativeStock.booleanValue()) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) && (BigDecimalUtils.compare(sckAmount, baseAmount) < 0))
        {
          if (SysConfig.getConfig(configName, 15).booleanValue())
          {
            Storage storage = (Storage)Storage.dao.findById(configName, detailSgeId);
            ResultHelp help = new ResultHelp(Integer.valueOf(trIndex), productId, product.getStr("fullName"), 
              product.getStr("code"), sckAmount, BigDecimalUtils.sub(sckAmount, baseAmount), detailSgeId, 
              storage.getStr("fullName"), storage.getStr("code"), costPrice, selectUnit);
            rList.add(help);
          }
        }
        else if (((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && (BigDecimalUtils.compare(sckAmount, baseAmount) < 0)) || ((!isNegativeStock.booleanValue()) && (BigDecimalUtils.compare(sckAmount, baseAmount) < 0)))
        {
          setAttr("message", "错误类型：商品库存数量不够<br/> 商品编号：" + product.getStr("code") + "<br/> 商品全名：" + product.getStr("fullName"));
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
      }
      moneysTotal = BigDecimalUtils.add(moneysTotal, detail.getBigDecimal("money"));
      amountTotal = BigDecimalUtils.add(amountTotal, amount);
    }
    if (rList.size() > 0)
    {
      Map<String, Object> map = new HashMap();
      map.put("statusCode", AioConstants.HTTP_RETURN200);
      map.put("dialog", Boolean.valueOf(true));
      map.put("url", getAttr("base") + "/stock/breakage/negativeOption");
      map.put("dialogTitle", "负库存提示");
      map.put("dialogId", "bsd_nagetive_info");
      map.put("width", "600");
      map.put("height", "350");
      setSessionAttr("bsdResultList", rList);
      renderJson(map);
      return;
    }
    Integer draftId = Integer.valueOf(0);
    
    int billStaffId = getParaToInt("staff.id", Integer.valueOf(0)).intValue();
    int billDepartmentId = getParaToInt("department.id", Integer.valueOf(0)).intValue();
    int billStorageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    if (billStaffId > 0) {
      bill.set("staffId", Integer.valueOf(billStaffId));
    }
    if (billDepartmentId > 0) {
      bill.set("departmentId", Integer.valueOf(billDepartmentId));
    }
    if (billStorageId > 0) {
      bill.set("storageId", Integer.valueOf(billStorageId));
    }
    if (BigDecimalUtils.compare(moneysTotal, BigDecimal.ZERO) == 1) {
      bill.set("moneys", moneysTotal);
    }
    if (BigDecimalUtils.compare(amountTotal, BigDecimal.ZERO) == 1) {
      bill.set("amounts", amountTotal);
    }
    String normalCode = getPara("billCode");
    if (!normalCode.equals(bill.getStr("code"))) {
      bill.set("codeIncrease", Integer.valueOf(-1));
    }
    Date time = new Date();
    bill.set("isRCW", Integer.valueOf(0));
    bill.set("updateTime", time);
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (bill.get("id") == null)
    {
      bill.save(configName);
    }
    else
    {
      bill.remove("id");
      if (bill.getInt("codeIncrease").intValue() != -1)
      {
        String codeCreateRule = AioerpSys.dao.getValue1(configName, "codeCreateRule");
        if ("2".equals(codeCreateRule))
        {
          int codeIncrease = bill.getInt("codeIncrease").intValue();
          bill.set("codeIncrease", Integer.valueOf(codeIncrease + 1));
        }
      }
      bill.save(configName);
      
      draftId = getParaToInt("draftId");
      BusinessDraft.dao.deleteById(configName, draftId);
      
      BreakageDraftBill.dao.deleteById(configName, billId);
      BreakageDraftDetail.dao.delDeail(configName, billId);
      
      StockDraftRecords.delRecords(configName, billTypeId, billId.intValue());
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    

    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
    for (int i = 0; i < detailList.size(); i++)
    {
      BreakageDetail detail = (BreakageDetail)detailList.get(i);
      storageId = detail.getInt("storageId");
      Integer productId = detail.getInt("productId");
      Product product = (Product)Product.dao.findById(configName, productId);
      Integer costArith = product.getInt("costArith");
      String batch = detail.getStr("batch");
      Date produceDate = detail.getDate("produceDate");
      Date produceEndDate = detail.getDate("produceEndDate");
      
      detail.set("billId", bill.getInt("id"));
      BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
      BigDecimal money = detail.getBigDecimal("money");
      BigDecimal basePrice = detail.getBigDecimal("basePrice");
      detail.set("updateTime", time);
      if (detail.get("id") != null) {
        detail.remove("id");
      }
      detail.save(configName);
      

      StockRecordsController.addRecords(configName, billTypeId, "out", bill, detail, basePrice, baseAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
      

      AvgpriceConTroller.addAvgprice(configName, "out", storageId, productId, baseAmount, money);
      

      StockController.stockChange(configName, "out", storageId.intValue(), productId.intValue(), costArith.intValue(), baseAmount, basePrice, batch, produceDate, produceEndDate);
    }
    Map<String, Object> record = new HashMap();
    record.put("proAccountList", detailList);
    record.put("moneys", bill.getBigDecimal("moneys"));
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), Integer.valueOf(0), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, record);
    

    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId.intValue() > 0)
    {
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
      setDraftStrs();
    }
    else
    {
      setAttr("navTabId", "breakageBillView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    printBeforSave1();
    

    renderJson();
  }
  
  @Before({Tx.class})
  public void updateDraft()
  {
    String configName = loginConfigName();
    Integer draftId = getParaToInt("draftId");
    

    boolean falg = editDraftVerify(draftId).booleanValue();
    if (falg) {
      return;
    }
    BreakageDraftBill bill = (BreakageDraftBill)getModel(BreakageDraftBill.class, "breakageBill");
    Integer billId = bill.getInt("id");
    if (billId == null) {
      billId = Integer.valueOf(0);
    }
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    Date recodeDate = bill.getTimestamp("recodeDate");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), BreakageDraftDetail.class, "brekageBillDetail");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    BigDecimal amountTotal = new BigDecimal(0);
    BigDecimal moneysTotal = new BigDecimal(0);
    

    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      BigDecimal amount = detail.getBigDecimal("amount");
      moneysTotal = BigDecimalUtils.add(moneysTotal, detail.getBigDecimal("money"));
      amountTotal = BigDecimalUtils.add(amountTotal, amount);
      if ((detail.getInt("storageId") == null) || (detail.getInt("storageId").intValue() == 0)) {
        detail.set("storageId", storageId);
      }
    }
    int billStaffId = getParaToInt("staff.id", Integer.valueOf(0)).intValue();
    int billDepartmentId = getParaToInt("department.id", Integer.valueOf(0)).intValue();
    int billStorageId = getParaToInt("storage.id", Integer.valueOf(0)).intValue();
    if (billStaffId > 0) {
      bill.set("staffId", Integer.valueOf(billStaffId));
    }
    if (billDepartmentId > 0) {
      bill.set("departmentId", Integer.valueOf(billDepartmentId));
    }
    if (billStorageId > 0) {
      bill.set("storageId", Integer.valueOf(billStorageId));
    }
    if (BigDecimalUtils.compare(moneysTotal, BigDecimal.ZERO) == 1) {
      bill.set("moneys", moneysTotal);
    }
    if (BigDecimalUtils.compare(amountTotal, BigDecimal.ZERO) == 1) {
      bill.set("amounts", amountTotal);
    }
    String normalCode = getPara("billCode");
    if (!normalCode.equals(bill.getStr("code"))) {
      bill.set("codeIncrease", Integer.valueOf(-1));
    }
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (draftId != null)
    {
      bill.update(configName);
      
      BusinessDraftController.updateBillDraft(configName, draftId.intValue(), bill, billTypeId, bill.getBigDecimal("moneys"));
      
      StockDraftRecords.delRecords(configName, billTypeId, billId.intValue());
    }
    else
    {
      if (bill.getInt("codeIncrease").intValue() != -1)
      {
        String codeCreateRule = AioerpSys.dao.getValue1(configName, "codeCreateRule");
        if ("2".equals(codeCreateRule))
        {
          int codeIncrease = bill.getInt("codeIncrease").intValue();
          bill.set("codeIncrease", Integer.valueOf(codeIncrease - 1));
        }
      }
      bill.save(configName);
      BusinessDraftController.saveBillDraft(configName, billTypeId, bill, bill.getBigDecimal("moneys"));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    List<Integer> oldDetailIds = BreakageDraftDetail.dao.getListByDetailId(configName, billId);
    
    List<Integer> newDetaiIds = new ArrayList();
    BreakageDraftDetail detail;
    for (int i = 0; i < detailList.size(); i++)
    {
      detail = (BreakageDraftDetail)detailList.get(i);
      Integer detailId = detail.getInt("id");
      detail.set("billId", bill.getInt("id"));
      BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
      BigDecimal money = detail.getBigDecimal("money");
      detail.set("basePrice", BigDecimalUtils.div(money, baseAmount));
      detail.set("updateTime", time);
      if (detailId == null)
      {
        detail.save(configName);
      }
      else
      {
        detail.update(configName);
        newDetaiIds.add(detailId);
      }
      StockDraftRecords.addRecords(configName, billTypeId, false, bill, detail, BigDecimalUtils.div(money, baseAmount), baseAmount, time, bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
    }
    for (Integer id : oldDetailIds) {
      if (!newDetaiIds.contains(id)) {
        BreakageDraftDetail.dao.deleteById(configName, id);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != null)
    {
      setAttr("message", "草稿单据：【" + bill.getStr("code") + "】更新成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "breakageBillView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    renderJson();
  }
  
  public void negativeOption()
  {
    List<ResultHelp> list = (List)getSessionAttr("bsdResultList");
    setAttr("rList", list);
    render("negativeOption.html");
  }
  
  public void setStockPrice()
  {
    List<ResultHelp> list = (List)getSessionAttr("bsdResultList");
    List<ResultHelp> recordList = new ArrayList();
    for (ResultHelp resultHelp : list)
    {
      BigDecimal avgPrice = resultHelp.getAvgPrice();
      if ((avgPrice == null) || (BigDecimalUtils.compare(avgPrice, BigDecimal.ZERO) == 0)) {
        recordList.add(resultHelp);
      }
    }
    getSession().removeAttribute("bsdResultList");
    setAttr("recordList", recordList);
    render("stockCostPriceOption.html");
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int orderId = getParaToInt(0, Integer.valueOf(0)).intValue();
    boolean isRCW = false;
    
    Model bill = BreakageBill.dao.findById(configName, Integer.valueOf(orderId));
    Integer orderIsRedRow = bill.getInt("isRCW");
    if (orderIsRedRow == null) {
      orderIsRedRow = Integer.valueOf(0);
    }
    List<Model> detailList = BreakageDetail.dao.getList(configName, Integer.valueOf(orderId));
    addTrSize(detailList, 15);
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    if (orderIsRedRow.intValue() == AioConstants.RCW_VS) {
      isRCW = true;
    } else {
      isRCW = false;
    }
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("isRCW", Boolean.valueOf(isRCW));
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    
    render("look.html");
  }
  
  public void toEditDraft()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int sourceId = getParaToInt(1, Integer.valueOf(0)).intValue();
    Model bill = BreakageDraftBill.dao.findById(configName, Integer.valueOf(billId));
    if (bill == null)
    {
      this.result.put("message", "该单据已删除，请核实!");
      this.result.put("statusCode", AioConstants.HTTP_RETURN300);
      renderJson(this.result);
      return;
    }
    if (bill.getInt("codeIncrease") == null)
    {
      Map<String, String> map = billCodeAuto(billTypeId);
      bill.set("codeIncrease", String.valueOf(map.get("codeIncrease")));
    }
    List<Model> detailList = BreakageDraftDetail.dao.getList(configName, Integer.valueOf(billId));
    addTrSize(detailList, 15);
    List<Model> rowList = BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE);
    setAttr("rowList", rowList);
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("draftId", Integer.valueOf(sourceId));
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    

    notEditStaff();
    setDraftAutoPost();
    render("editDraft.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> map = new HashMap();
    try
    {
      BreakageBill bill = (BreakageBill)BreakageBill.dao.findById(configName, billId);
      List<Model> details = BreakageDetail.dao.getDetailsByBillId(configName, billId);
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
      bill.update(configName);
      
      Date date = new Date();
      Timestamp time = new Timestamp(date.getTime());
      bill.remove("id");
      bill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
      bill.set("updateTime", time);
      
      billRcwAddRemark(bill, null);
      bill.save(configName);
      
      BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
      for (Model model : details)
      {
        BreakageDetail detail = (BreakageDetail)model;
        int storageId = detail.getInt("storageId").intValue();
        int productId = detail.getInt("productId").intValue();
        
        Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
        int costArith = product.getInt("costArith").intValue();
        BigDecimal money = detail.getBigDecimal("money");
        BigDecimal baseAmount = detail.getBigDecimal("baseAmount");
        BigDecimal price = BigDecimalUtils.div(money, baseAmount);
        String batch = detail.getStr("batch");
        Date produceDate = detail.getDate("produceDate");
        Date produceEndDate = detail.getDate("produceEndDate");
        
        detail.set("rcwId", detail.getInt("id"));
        detail.remove("id");
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", new Date());
        detail.save(configName);
        

        StockRecordsController.addRecords(configName, billTypeId, "in", bill, detail, price, baseAmount, new Date(), bill.getTimestamp("recodeDate"), detail.getInt("storageId"));
        

        AvgpriceConTroller.addAvgprice(configName, "in", Integer.valueOf(storageId), Integer.valueOf(productId), baseAmount, money);
        


        StockController.stockChange(configName, "in", storageId, productId, costArith, baseAmount, price, batch, produceDate, produceEndDate);
      }
      PayTypeController.rcwAccountsRecoder(configName, billTypeId, billId, bill.getInt("id"));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      map.put("status", Integer.valueOf(AioConstants.RCW_NO));
      map.put("message", "系统异常，请联系管理员！");
      return map;
    }
    map.put("status", Integer.valueOf(AioConstants.RCW_BY));
    return map;
  }
  
  public void print()
    throws ParseException
  {
    Integer billId = getParaToInt("billId", Integer.valueOf(0));
    Integer editId = getParaToInt("breakageBill.id");
    String configName = loginConfigName();
    
    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "报损单");
    List<String> headData = new ArrayList();
    
    String recodeDate = DateUtils.format(getParaToDate("breakageBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    headData.add("单据编号:" + getPara("breakageBill.code", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门:" + getPara("department.fullName", ""));
    headData.add("发货仓库:" + getPara("storage.fullName", ""));
    headData.add("摘要:" + getPara("breakageBill.remark", ""));
    headData.add("附加说明:" + getPara("breakageBill.memo", ""));
    
    Model bill = null;
    if ((billId != null) && (billId.intValue() != 0)) {
      bill = BreakageBill.dao.findById(configName, billId);
    } else if ((editId != null) && (editId.intValue() != 0)) {
      bill = BreakageDraftBill.dao.findById(configName, editId);
    }
    if (bill != null) {
      setBillUser(headData, bill.getInt("userId"));
    } else {
      setBillUser(headData, null);
    }
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
    colTitle.add("单位");
    colTitle.add("辅助数量");
    colTitle.add("数量");
    colTitle.add("生产日期");
    colTitle.add("批号");
    colTitle.add("条码");
    colTitle.add("单价");
    colTitle.add("金额");
    colTitle.add("备注");
    colTitle.add("到期日期");
    
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
    
    List<String> rowData = new ArrayList();
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), BreakageDetail.class, "brekageBillDetail");
    if (billId.intValue() > 0) {
      detailList = BreakageDetail.dao.getList(configName, billId);
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      Integer storageId = detail.getInt("storageId");
      Storage storage = (Storage)Storage.dao.findById(configName, storageId);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
      if (storage != null)
      {
        rowData.add(trimNull(storage.getStr("code")));
        rowData.add(trimNull(storage.getStr("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Integer selectUnitId = detail.getInt("selectUnitId");
      rowData.add(trimNull(DwzUtils.getSelectUnit(product, selectUnitId)));
      rowData.add(DwzUtils.helpAmount(detail.getBigDecimal("amount"), product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(detail.getBigDecimal("amount")));
      rowData.add(trimNull(detail.getDate("produceDate")));
      rowData.add(trimNull(detail.getStr("batch")));
      rowData.add(trimNull(product.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(trimNull(detail.getBigDecimal("price")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.get("memo")));
      rowData.add(trimNull(detail.getDate("produceEndDate")));
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
}
