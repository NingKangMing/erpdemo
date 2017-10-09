package com.aioerp.controller.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AvgpriceConTroller;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.finance.AdjustCostBill;
import com.aioerp.model.finance.AdjustCostDetail;
import com.aioerp.model.finance.AdjustCostDraftBill;
import com.aioerp.model.finance.AdjustCostDraftDetail;
import com.aioerp.model.stock.Stock;
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
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class AdjustCostController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE20;
  
  public void index()
  {
    String configName = loginConfigName();
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    billCodeAuto(billTypeId);
    loadOnlyOneBaseInfo(configName);
    

    notEditStaff();
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
  {
    String configName = loginConfigName();
    
    printBeforSave1();
    AdjustCostBill bill = (AdjustCostBill)getModel(AdjustCostBill.class, "adjustCostBill");
    
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    bill.set("id", null);
    Integer storageId = getParaToInt("storage.id");
    
    boolean hasOther = AdjustCostBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(bill.getStr("code")))))
    {
      setAttr("message", "编号已经存在!");
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
    List<AdjustCostDetail> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), AdjustCostDetail.class, "adjustCostDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      AdjustCostDetail detail = (AdjustCostDetail)detailList.get(i);
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1);
      Integer productId = detail.getInt("productId");
      if (productId == null)
      {
        setAttr("message", "第" + trIndex + "行商品录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("lastPrice")))
      {
        setAttr("message", "第" + trIndex + "行调整单价录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (BigDecimalUtils.compare(detail.getBigDecimal("amount"), BigDecimal.ZERO) < 0)
      {
        setAttr("message", "第" + trIndex + "行数量错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        




        Integer costArith = product.getInt("costArith");
        

        BigDecimal sAmount = BigDecimal.ZERO;
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
          sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), storageId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        } else {
          sAmount = Stock.dao.getStockAmount(configName, productId, storageId);
        }
        if (BigDecimalUtils.compare(sAmount, BigDecimal.ZERO) <= 0)
        {
          setAttr("message", "错误类型：库存数量不够<br/> 商品编号：" + product.getStr("code") + "<br/> 商品全名：" + product.getStr("fullName"));
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
      }
    }
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    
    bill.set("storageId", storageId);
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    bill.save(configName);
    

    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("lastMoneys"));
    BigDecimal moneys = BigDecimal.ZERO;
    BigDecimal lastMoneys = BigDecimal.ZERO;
    for (int i = 0; i < detailList.size(); i++)
    {
      AdjustCostDetail detail = (AdjustCostDetail)detailList.get(i);
      detail.set("id", null);
      Integer productId = detail.getInt("productId");
      BigDecimal amount = detail.getBigDecimal("amount");
      BigDecimal price = detail.getBigDecimal("price");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer costArith = product.getInt("costArith");
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
          amount = Stock.dao.getStockAmount(configName, productId.intValue(), storageId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        } else {
          amount = Stock.dao.getStockAmount(configName, productId, storageId);
        }
        Stock stock = null;
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
          stock = Stock.dao.getStock(configName, productId.intValue(), storageId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        } else {
          stock = Stock.dao.getStock(configName, productId, storageId);
        }
        detail.set("amount", amount);
        BigDecimal baseAmount = amount;
        if (stock != null) {
          price = stock.getBigDecimal("costPrice");
        }
        detail.set("price", price);
        BigDecimal basePrice = price;
        detail.set("money", BigDecimalUtils.mul(amount, price));
        detail.set("lastMoney", BigDecimalUtils.mul(amount, detail.getBigDecimal("lastPrice")));
        moneys = BigDecimalUtils.add(moneys, BigDecimalUtils.mul(amount, price));
        lastMoneys = BigDecimalUtils.add(lastMoneys, BigDecimalUtils.mul(amount, detail.getBigDecimal("lastPrice")));
        
        detail.set("adjustMoney", BigDecimalUtils.sub(detail.getBigDecimal("lastMoney"), detail.getBigDecimal("money")));
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        detail.set("baseAmount", baseAmount);
        detail.set("basePrice", basePrice);
        if (detail.getBigDecimal("costPrice") == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, storageId.intValue(), product, detail.getBigDecimal("costPrice"), detail.getInt("selectUnitId").intValue());
          detail.set("costPrice", new BigDecimal((String)map.get("costPrice")));
        }
        AvgpriceConTroller.eidtAvgprice(configName, storageId, productId, detail.getBigDecimal("lastPrice"));
        if (stock != null)
        {
          stock.set("costPrice", basePrice);
          stock.update(configName);
        }
        detail.save(configName);
      }
    }
    bill.set("moneys", moneys);
    bill.set("lastMoneys", lastMoneys);
    bill.set("adjustMoneys", BigDecimalUtils.sub(lastMoneys, moneys));
    bill.update(configName);
    
    Map<String, Object> record = new HashMap();
    record.put("accounts", getOtherAccount(bill.getBigDecimal("moneys"), bill.getBigDecimal("lastMoneys"), bill.getBigDecimal("adjustMoneys")));
    record.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), null, bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, record);
    
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      setDraftStrs();
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      AdjustCostDraftBill.dao.deleteById(configName, draftBillId);
      AdjustCostDraftDetail.dao.delByBill(configName, draftBillId);
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "adjustCostView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    renderJson();
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> result = new HashMap();
    AdjustCostBill bill = (AdjustCostBill)AdjustCostBill.dao.findById(configName, billId);
    if (bill == null)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", "该成本调价单不存在");
      return result;
    }
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
    bill.update(configName);
    AdjustCostBill newBill = bill;
    newBill.set("id", null);
    newBill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
    Date time = new Date();
    newBill.set("updateTime", time);
    
    billRcwAddRemark(bill, null);
    newBill.save(configName);
    BillHistoryController.saveBillHistory(configName, newBill, billTypeId, newBill.getBigDecimal("lastMoneys"));
    Integer storageId = newBill.getInt("storageId");
    List<Model> detailList = AdjustCostDetail.dao.getListByBillId(configName, billId);
    for (Model odlDetail : detailList)
    {
      AdjustCostDetail newDetail = (AdjustCostDetail)odlDetail;
      
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.set("id", null);
      newDetail.set("billId", newBill.getInt("id"));
      newDetail.set("updateTime", time);
      newDetail.save(configName);
      
      BigDecimal baseAmount = newDetail.getBigDecimal("baseAmount");
      Integer productId = newDetail.getInt("productId");
      
      BigDecimal costPrice = newDetail.getBigDecimal("costPrice");
      
      BigDecimal newAvgprice = AvgpriceConTroller.eidtAvgprice(configName, storageId, productId, costPrice, baseAmount);
      Stock stock = null;
      Product product = (Product)Product.dao.findById(configName, productId);
      Integer costArith = product.getInt("costArith");
      if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
        stock = Stock.dao.getStock(configName, productId.intValue(), storageId.intValue(), newDetail.getBigDecimal("costPrice"), newDetail.getStr("batch"), newDetail.getDate("produceDate"), newDetail.getDate("produceEndDate"));
      } else {
        stock = Stock.dao.getStock(configName, productId, storageId);
      }
      if (stock != null)
      {
        stock.set("costPrice", newAvgprice);
        stock.update(configName);
      }
    }
    PayTypeController.rcwAccountsRecoder(configName, billTypeId, billId, newBill.getInt("id"));
    result.put("status", Integer.valueOf(AioConstants.RCW_BY));
    result.put("message", "红冲成功");
    return result;
  }
  
  public void look()
  {
    Integer id = getParaToInt(0, Integer.valueOf(0));
    String configName = loginConfigName();
    AdjustCostBill bill = (AdjustCostBill)AdjustCostBill.dao.findById(configName, id);
    List<Model> detailList = AdjustCostDetail.dao.getListByBillId(configName, id);
    detailList = addTrSize(detailList, 15);
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    render("look.html");
  }
  
  public static List<Record> getOtherAccount(BigDecimal moneys, BigDecimal lastMoneys, BigDecimal adjustMoneys)
  {
    List<Record> accountList = new ArrayList();
    Record spread = new Record();
    spread.set("moneyType", Integer.valueOf(AioConstants.ACCOUNT_MONEY1));
    spread.set("accountType", Integer.valueOf(AioConstants.ACCOUNT_TYPE0));
    spread.set("arapAccount", Integer.valueOf(AioConstants.ARAPACCOUNT0));
    spread.set("money", moneys);
    spread.set("account", "000412");
    accountList.add(spread);
    
    Record last = new Record();
    last.set("moneyType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    last.set("accountType", Integer.valueOf(AioConstants.ACCOUNT_TYPE0));
    last.set("arapAccount", Integer.valueOf(AioConstants.ARAPACCOUNT0));
    last.set("money", lastMoneys);
    last.set("account", "000412");
    accountList.add(last);
    
    Record adjust = new Record();
    adjust.set("moneyType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    adjust.set("accountType", Integer.valueOf(AioConstants.ACCOUNT_TYPE0));
    adjust.set("arapAccount", Integer.valueOf(AioConstants.ARAPACCOUNT0));
    adjust.set("money", adjustMoneys);
    adjust.set("account", "00025");
    accountList.add(adjust);
    
    return accountList;
  }
  
  public void print()
    throws ParseException
  {
    Integer billId = getParaToInt("billId");
    Integer draftId = getParaToInt("draftId");
    String configName = loginConfigName();
    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "成本调价单");
    
    List<String> headData = new ArrayList();
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门 :" + getPara("department.fullName", ""));
    headData.add("仓库:" + getPara("storage.fullName", ""));
    String recodeDate = DateUtils.format(getParaToDate("adjustCostBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    
    headData.add("收货仓库:" + getPara("storage.fullName", ""));
    headData.add("摘要:" + getPara("adjustCostBill.remark", ""));
    headData.add("附加说明 :" + getPara("adjustCostBill.memo", ""));
    headData.add("单据编号:" + getPara("adjustCostBill.code", ""));
    Integer userId = null;
    if ((billId != null) && (billId.intValue() != 0))
    {
      Model bill = AdjustCostBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    else if ((draftId != null) && (draftId.intValue() != 0))
    {
      Model bill = AdjustCostDraftBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
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
    colTitle.add("批号");
    colTitle.add("辅助数量");
    colTitle.add("基本数量");
    colTitle.add("辅助数量1");
    colTitle.add("辅助数量2");
    colTitle.add("数量");
    colTitle.add("调整前单价");
    colTitle.add("调整前金额");
    colTitle.add("调整后单价");
    colTitle.add("调整后金额");
    colTitle.add("调整金额");
    colTitle.add("零售价");
    colTitle.add("零售金额");
    colTitle.add("备注");
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
    
    List<String> rowData = new ArrayList();
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), AdjustCostDetail.class, "adjustCostDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailList = AdjustCostDetail.dao.getListByBillId(configName, billId);
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
      rowData.add(trimNull(detail.getStr("batch")));
      rowData.add(DwzUtils.helpAmount(detail.getBigDecimal("amount"), product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(1)) + trimNull(product.getStr("calculateUnit1")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(2)) + trimNull(product.getStr("calculateUnit2")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(3)) + trimNull(product.getStr("calculateUnit3")));
      rowData.add(trimNull(detail.getBigDecimal("amount")));
      rowData.add(trimNull(detail.getBigDecimal("price")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.getBigDecimal("lastPrice")));
      rowData.add(trimNull(detail.getBigDecimal("lastMoney")));
      rowData.add(trimNull(detail.getBigDecimal("adjustMoney")));
      rowData.add(trimNull(detail.getBigDecimal("retailPrice")));
      rowData.add(trimNull(detail.getBigDecimal("retailMoney")));
      rowData.add(trimNull(detail.get("memo")));
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
}
