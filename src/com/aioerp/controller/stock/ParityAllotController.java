package com.aioerp.controller.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.base.AvgpriceConTroller;
import com.aioerp.controller.finance.PayTypeController;
import com.aioerp.controller.reports.BillHistoryController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Storage;
import com.aioerp.model.bought.ResultHelp;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.stock.ParityAllotBill;
import com.aioerp.model.stock.ParityAllotDetail;
import com.aioerp.model.stock.ParityAllotDraftBill;
import com.aioerp.model.stock.ParityAllotDraftDetail;
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
import org.apache.commons.lang3.StringUtils;

public class ParityAllotController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE14;
  
  public void index()
  {
    String configName = loginConfigName();
    setAttr("recodeTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    

    billCodeAuto(billTypeId);
    

    notEditStaff();
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
  {
    String configName = loginConfigName();
    
    printBeforSave1();
    ParityAllotBill bill = (ParityAllotBill)getModel(ParityAllotBill.class);
    
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bill.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    int draftId = getParaToInt("draftId", Integer.valueOf(0)).intValue();
    Integer draftBillId = bill.getInt("id");
    bill.set("id", null);
    boolean comfirm = getParaToBoolean("needComfirm", Boolean.valueOf(true)).booleanValue();
    boolean hasOther = ParityAllotBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
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
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ParityAllotDetail.class, "parityAllotDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer inStorageId = getParaToInt("inStorage.id");
    Integer outStorageId = getParaToInt("outStorage.id");
    
    Boolean isNegativeStock = SysConfig.getConfig(configName, 1);
    

    List<ResultHelp> rList = new ArrayList();
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Integer detailInStorage = detail.getInt("inStorageId");
      Integer detailOutStorage = detail.getInt("outStorageId");
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpUitlList.get(i)).getTrIndex(), 1);
      if (((inStorageId == null) || (inStorageId.intValue() == 0)) && ((detailInStorage == null) || (detailInStorage.intValue() == 0)))
      {
        setAttr("message", "第" + trIndex + "行多收货仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if (((outStorageId == null) || (outStorageId.intValue() == 0)) && ((detailOutStorage == null) || (detailOutStorage.intValue() == 0)))
      {
        setAttr("message", "第" + trIndex + "行多发货仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      Integer productId = detail.getInt("productId");
      BigDecimal amount = detail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        Integer selectUnitId = detail.getInt("selectUnitId");
        String selectUnit = product.getStr("calculateUnit1");
        if (2 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit2");
        } else if (3 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit3");
        }
        if ((detailInStorage == null) || (detailInStorage.intValue() == 0)) {
          detailInStorage = inStorageId;
        }
        if ((detailOutStorage == null) || (detailOutStorage.intValue() == 0)) {
          detailOutStorage = outStorageId;
        }
        Integer costArith = product.getInt("costArith");
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        BigDecimal sAmount = BigDecimal.ZERO;
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4)
        {
          sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), detailOutStorage.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        }
        else
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, detailOutStorage.intValue(), product, detail.getBigDecimal("costPrice"), selectUnitId.intValue());
          sAmount = new BigDecimal((String)map.get("stockAmount"));
          detail.set("costPrice", new BigDecimal((String)map.get("costPrice")));
        }
        BigDecimal avgPrice = StockController.subAvgPrice(configName, detail, costArith, baseAmount, detail.getBigDecimal("costPrice"), detailOutStorage.intValue(), productId.intValue());
        if ((comfirm) && (isNegativeStock.booleanValue()) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) && (BigDecimalUtils.compare(sAmount, baseAmount) < 0))
        {
          if (SysConfig.getConfig(configName, 15).booleanValue())
          {
            Storage storage = (Storage)Storage.dao.findById(configName, detailOutStorage);
            ResultHelp help = new ResultHelp(Integer.valueOf(trIndex), productId, product.getStr("fullName"), product.getStr("code"), sAmount, BigDecimalUtils.sub(sAmount, amount), detailOutStorage, storage.getStr("fullName"), storage.getStr("code"), avgPrice, selectUnit);
            rList.add(help);
          }
        }
        else if (((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0)) || ((!isNegativeStock.booleanValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0)))
        {
          setAttr("message", "错误类型：库存数量不够<br/> 商品编号：" + product.getStr("code") + "<br/> 商品全名：" + product.getStr("fullName"));
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
      }
    }
    if (rList.size() > 0)
    {
      Map<String, Object> map = new HashMap();
      map.put("statusCode", AioConstants.HTTP_RETURN200);
      map.put("dialog", Boolean.valueOf(true));
      map.put("url", getAttr("base") + "/bought/return/negativeOption");
      map.put("dialogTitle", "负库存");
      map.put("dialogId", "jhthd_nagetive_info");
      map.put("width", "600");
      map.put("height", "350");
      setSessionAttr("resultList", rList);
      renderJson(map);
      return;
    }
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    
    bill.set("inStorageId", inStorageId);
    bill.set("outStorageId", outStorageId);
    
    bill.set("userId", Integer.valueOf(loginUserId()));
    Date time = new Date();
    bill.set("updateTime", time);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    String staffName = getPara("staff.name");
    String remark = bill.getStr("remark");
    String inStorageFullName = getPara("inStorage.fullName");
    if (draftBillId == null) {
      if (StringUtils.isNotBlank(remark))
      {
        remark = remark + "调拨货物到";
        if (StringUtils.isNotBlank(inStorageFullName)) {
          remark = remark + "【" + inStorageFullName + "】";
        }
        if (StringUtils.isNotBlank(staffName)) {
          remark = remark + ":" + staffName;
        }
      }
      else
      {
        remark = "调拨货物到";
        if (StringUtils.isNotBlank(inStorageFullName)) {
          remark = remark + "【" + inStorageFullName + "】";
        }
        if (StringUtils.isNotBlank(staffName)) {
          remark = remark + ":" + staffName;
        }
      }
    }
    bill.set("remark", remark);
    if (draftId != 0) {
      billCodeIncrease(bill, "draftPost");
    } else {
      billCodeIncrease(bill, "add");
    }
    bill.save(configName);
    

    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("moneys"));
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      detail.set("id", null);
      Integer detailInStorage = detail.getInt("inStorageId");
      Integer detailOutStorage = detail.getInt("outStorageId");
      Integer productId = detail.getInt("productId");
      BigDecimal amount = detail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        Integer selectUnitId = detail.getInt("selectUnitId");
        if ((detailInStorage == null) || (detailInStorage.intValue() == 0)) {
          detailInStorage = inStorageId;
        }
        if ((detailOutStorage == null) || (detailOutStorage.intValue() == 0)) {
          detailOutStorage = outStorageId;
        }
        detail.set("inStorageId", detailInStorage);
        detail.set("outStorageId", detailOutStorage);
        Integer costArith = product.getInt("costArith");
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        BigDecimal costPrice = detail.getBigDecimal("costPrice");
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE1) {
          costPrice = ComFunController.outProductGetCostPriceAll(configName, billTypeId, bill.getInt("isRCW").intValue(), detailOutStorage.intValue(), product, detail, "costPrice");
        }
        if ((costPrice == null) || (BigDecimalUtils.compare(costPrice, BigDecimal.ZERO) == 0))
        {
          detail.set("giftAmount", baseAmount);
        }
        else
        {
          detail.set("baseAmount", baseAmount);
          detail.set("giftAmount", null);
        }
        detail.set("billId", bill.getInt("id"));
        detail.set("updateTime", time);
        detail.set("price", DwzUtils.getConversionPrice(costPrice, Integer.valueOf(1), unitRelation1, unitRelation2, unitRelation3, selectUnitId));
        detail.set("costPrice", costPrice);
        detail.set("money", BigDecimalUtils.mul(costPrice, baseAmount));
        
        BigDecimal costMoney = BigDecimalUtils.mul(baseAmount, costPrice);
        StockController.stockSubChange(configName, costArith.intValue(), baseAmount, detailOutStorage.intValue(), productId.intValue(), costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"), null);
        StockController.stockChange(configName, "in", detailInStorage.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, costPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        
        StockRecordsController.addRecords(configName, billTypeId, "out", bill, detail, costPrice, baseAmount, time, bill.getTimestamp("recodeDate"), detailOutStorage);
        StockRecordsController.addRecords(configName, billTypeId, "in", bill, detail, costPrice, baseAmount, time, bill.getTimestamp("recodeDate"), detailInStorage);
        
        AvgpriceConTroller.addAvgprice(configName, "out", detailOutStorage, productId, baseAmount, costMoney);
        AvgpriceConTroller.addAvgprice(configName, "in", detailInStorage, productId, baseAmount, costMoney);
        detail.save(configName);
      }
    }
    Map<String, Object> outRecord = new HashMap();
    outRecord.put("proAccountType", Integer.valueOf(AioConstants.ACCOUNT_MONEY1));
    outRecord.put("proAccountList", detailList);
    outRecord.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, outRecord);
    Map<String, Object> inRecord = new HashMap();
    inRecord.put("proAccountType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    inRecord.put("proAccountList", detailList);
    inRecord.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, inRecord);
    

    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      setDraftStrs();
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      

      ParityAllotDraftBill.dao.deleteById(configName, draftBillId);
      ParityAllotDraftDetail.dao.delByBill(configName, draftBillId);
      PayDraft.dao.delete(configName, draftBillId, Integer.valueOf(billTypeId));
      
      StockDraftRecords.delRecords(configName, billTypeId, draftBillId.intValue());
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "parityAllotView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    renderJson();
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    ParityAllotBill bill = (ParityAllotBill)ParityAllotBill.dao.findById(configName, Integer.valueOf(id));
    List<Model> detailList = ParityAllotDetail.dao.getListByBillId(configName, Integer.valueOf(id));
    detailList = addTrSize(detailList, 15);
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("bill", bill);
    setAttr("detailList", detailList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    render("look.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> result = new HashMap();
    ParityAllotBill bill = (ParityAllotBill)ParityAllotBill.dao.findById(configName, billId);
    if (bill == null)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", "该同价调拨单不存在");
      return result;
    }
    List<Model> detailList = ParityAllotDetail.dao.getListByBillId(configName, billId);
    boolean comfirm = SysConfig.getConfig(configName, 1).booleanValue();
    Integer productId;
    for (Model detail : detailList)
    {
      BigDecimal amount = detail.getBigDecimal("baseAmount");
      productId = detail.getInt("productId");
      Integer inStorageId = detail.getInt("inStorageId");
      
      Product product = (Product)detail.get("product");
      Integer costArith = product.getInt("costArith");
      
      BigDecimal sAmount = BigDecimal.ZERO;
      if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
        sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), inStorageId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
      } else {
        sAmount = Stock.dao.getStockAmount(configName, productId, inStorageId);
      }
      if ((!comfirm) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0))
      {
        result.put("status", Integer.valueOf(AioConstants.RCW_NO));
        result.put("message", "该同价调拨单有商品库存不足");
        return result;
      }
      if ((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0))
      {
        result.put("status", Integer.valueOf(AioConstants.RCW_NO));
        result.put("message", "该同价调拨单有商品库存不足");
        return result;
      }
    }
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
    bill.update(configName);
    ParityAllotBill newBill = bill;
    newBill.set("id", null);
    newBill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
    Date time = new Date();
    newBill.set("updateTime", time);
    
    billRcwAddRemark(bill, null);
    newBill.save(configName);
    
    BillHistoryController.saveBillHistory(configName, newBill, billTypeId, bill.getBigDecimal("moneys"));
    for (Model odlDetail : detailList)
    {
      ParityAllotDetail newDetail = (ParityAllotDetail)odlDetail;
      
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.set("id", null);
      newDetail.set("billId", newBill.getInt("id"));
      newDetail.set("updateTime", time);
      BigDecimal amount = odlDetail.getBigDecimal("baseAmount");
       productId = odlDetail.getInt("productId");
      Integer inStorageId = odlDetail.getInt("inStorageId");
      Integer outStorageId = odlDetail.getInt("outStorageId");
      Product product = (Product)odlDetail.get("product");
      Integer costArith = product.getInt("costArith");
      BigDecimal costPrice = newDetail.getBigDecimal("costPrice");
      
      StockController.stockSubChange(configName, costArith.intValue(), amount, inStorageId.intValue(), productId.intValue(), costPrice, newDetail.getStr("batch"), newDetail.getDate("produceDate"), newDetail.getDate("produceEndDate"), null);
      StockController.stockChange(configName, "in", outStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), amount, costPrice, newDetail.getStr("batch"), newDetail.getDate("produceDate"), newDetail.getDate("produceEndDate"));
      
      Timestamp times = new Timestamp(time.getTime());
      StockRecordsController.addRecords(configName, billTypeId, "out", newBill, newDetail, costPrice, amount, time, times, inStorageId);
      StockRecordsController.addRecords(configName, billTypeId, "in", newBill, newDetail, costPrice, amount, time, times, outStorageId);
      
      AvgpriceConTroller.addAvgprice(configName, "out", inStorageId, productId, amount, BigDecimalUtils.mul(amount, costPrice));
      AvgpriceConTroller.addAvgprice(configName, "in", outStorageId, productId, amount, BigDecimalUtils.mul(amount, costPrice));
      newDetail.save(configName);
    }
    PayTypeController.rcwAccountsRecoder(configName, billTypeId, billId, newBill.getInt("id"));
    result.put("status", Integer.valueOf(AioConstants.RCW_BY));
    result.put("message", "红冲成功");
    return result;
  }
  
  public void print()
    throws ParseException
  {
    String configName = loginConfigName();
    Integer billId = getParaToInt("billId");
    Integer draftId = getParaToInt("draftId");
    Map<String, Object> data = new HashMap();
    if (printBeforSave2(data, billId).booleanValue())
    {
      renderJson(data);
      return;
    }
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "同价调拨单");
    List<String> headData = new ArrayList();
    headData.add("发货仓库 :" + getPara("outStorage.fullName", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门 :" + getPara("department.fullName", ""));
    String recodeDate = DateUtils.format(getParaToDate("parityAllotBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    
    headData.add("收货仓库:" + getPara("inStorage.fullName", ""));
    
    headData.add("摘要:" + getPara("parityAllotBill.remark", ""));
    headData.add("附加说明 :" + getPara("parityAllotBill.memo", ""));
    headData.add("单据编号:" + getPara("parityAllotBill.code", ""));
    
    Integer userId = null;
    if ((billId != null) && (billId.intValue() != 0))
    {
      Model bill = ParityAllotBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    else if ((draftId != null) && (draftId.intValue() != 0))
    {
      Model bill = ParityAllotDraftBill.dao.findById(configName, billId);
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
    colTitle.add("发货仓库编号");
    colTitle.add("发货仓库全名");
    colTitle.add("收货仓库编号");
    colTitle.add("收货仓库全名");
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
    colTitle.add("零售价");
    colTitle.add("零售金额");
    colTitle.add("备注");
    colTitle.add("状态");
    colTitle.add("辅助单位");
    colTitle.add("附加信息");
    colTitle.add("条码");
    
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
    
    List<String> rowData = new ArrayList();
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ParityAllotDetail.class, "parityAllotDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailList = ParityAllotDetail.dao.getListByBillId(configName, billId);
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      Integer outStorageId = detail.getInt("outStorageId");
      Storage outStorage = (Storage)Storage.dao.findById(configName, outStorageId);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
      rowData.add(trimNull(product.getStr("field")));
      if (outStorage != null)
      {
        rowData.add(trimNull(outStorage.getStr("code")));
        rowData.add(trimNull(outStorage.getStr("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Integer inStorageId = detail.getInt("inStorageId");
      Storage inStorage = (Storage)Storage.dao.findById(configName, inStorageId);
      if (inStorage != null)
      {
        rowData.add(trimNull(inStorage.getStr("code")));
        rowData.add(trimNull(inStorage.getStr("fullName")));
      }
      else
      {
        rowData.add("");
        rowData.add("");
      }
      Integer selectUnitId = detail.getInt("selectUnitId");
      rowData.add(trimNull(DwzUtils.getSelectUnit(product, selectUnitId)));
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
      rowData.add(trimNull(detail.getBigDecimal("retailPrice")));
      rowData.add(trimNull(detail.getBigDecimal("retailMoney")));
      
      rowData.add(trimNull(detail.get("memo")));
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      rowData.add(status);
      rowData.add(DwzUtils.helpUnit(product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(detail.getStr("message")));
      rowData.add(trimNull(product.getStr("barCode" + detail.getInt("selectUnitId"))));
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
