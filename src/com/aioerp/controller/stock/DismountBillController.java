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
import com.aioerp.model.stock.DismountBill;
import com.aioerp.model.stock.DismountDetail;
import com.aioerp.model.stock.DismountDraftBill;
import com.aioerp.model.stock.DismountDraftDetail;
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
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class DismountBillController
  extends BaseController
{
  private static final int billTypeId = AioConstants.BILL_ROW_TYPE16;
  
  public void index()
  {
    String configName = loginConfigName();
    setAttr("todayTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(billTypeId));
    
    billCodeAuto(billTypeId);
    
    notEditStaff();
    render("add.html");
  }
  
  @Before({Tx.class})
  public void add()
    throws SQLException
  {
    String configName = loginConfigName();
    
    printBeforSave1();
    DismountBill bill = (DismountBill)getModel(DismountBill.class);
    
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
    boolean hasOther = DismountBill.dao.codeIsExist(configName, bill.getStr("code"), 0);
    
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
    List<Model> inDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), DismountDetail.class, "dismountInDetail");
    List<HelpUtil> helpInList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpInUtil");
    
    List<Model> outDetailList = ModelUtils.batchInjectSortObjModel(getRequest(), DismountDetail.class, "dismountOutDetail");
    List<HelpUtil> helpOutList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpOutUtil");
    if ((inDetailList.size() == 0) || (outDetailList.size() == 0))
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer inStorageId = getParaToInt("inStorage.id");
    Integer outStorageId = getParaToInt("outStorage.id");
    bill.set("staffId", getParaToInt("staff.id"));
    bill.set("departmentId", getParaToInt("department.id"));
    bill.set("outStorageId", outStorageId);
    bill.set("inStorageId", inStorageId);
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_NO));
    bill.set("userId", Integer.valueOf(loginUserId()));
    for (int i = 0; i < inDetailList.size(); i++)
    {
      Model inDetail = (Model)inDetailList.get(i);
      Integer detailstorage = inDetail.getInt("storageId");
      if (((inStorageId == null) || (inStorageId.intValue() == 0)) && ((detailstorage == null) || (detailstorage.intValue() == 0)))
      {
        int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpInList.get(i)).getTrIndex(), 1);
        
        setAttr("message", "第" + trIndex + "行多仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      BigDecimal amount = inDetail.getBigDecimal("amount");
      if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) < 1)
      {
        int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpInList.get(i)).getTrIndex(), 1);
        
        setAttr("message", "第" + trIndex + "行数量录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
    }
    Boolean isNegativeStock = SysConfig.getConfig(configName, 1);
    

    List<ResultHelp> rList = new ArrayList();
    for (int i = 0; i < outDetailList.size(); i++)
    {
      Model outDetail = (Model)outDetailList.get(i);
      Integer detailstorage = outDetail.getInt("storageId");
      int trIndex = StringUtil.strAddNumToInt(((HelpUtil)helpOutList.get(i)).getTrIndex(), 1);
      if (((outStorageId == null) || (outStorageId.intValue() == 0)) && ((detailstorage == null) || (detailstorage.intValue() == 0)))
      {
        setAttr("message", "第" + trIndex + "行多仓库录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      BigDecimal amount = outDetail.getBigDecimal("amount");
      if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) < 1)
      {
        setAttr("message", "第" + trIndex + "行数量录入错误!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      Integer productId = outDetail.getInt("productId");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        Integer selectUnitId = outDetail.getInt("selectUnitId");
        String selectUnit = product.getStr("calculateUnit1");
        if (2 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit2");
        } else if (3 == selectUnitId.intValue()) {
          selectUnit = product.getStr("calculateUnit3");
        }
        Integer detailStorageId = outDetail.getInt("storageId");
        if ((detailStorageId == null) || (detailStorageId.intValue() == 0)) {
          detailStorageId = outStorageId;
        }
        Integer costArith = product.getInt("costArith");
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal sAmount = BigDecimal.ZERO;
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
          sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), detailStorageId.intValue(), outDetail.getBigDecimal("costPrice"), outDetail.getStr("batch"), outDetail.getDate("produceDate"), outDetail.getDate("produceEndDate"));
        } else {
          sAmount = Stock.dao.getStockAmount(configName, productId, detailStorageId);
        }
        if (outDetail.getBigDecimal("costPrice") == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, detailStorageId.intValue(), product, outDetail.getBigDecimal("costPrice"), selectUnitId.intValue());
          outDetail.set("costPrice", new BigDecimal((String)map.get("costPrice")));
        }
        BigDecimal avgPrice = StockController.subAvgPrice(configName, outDetail, costArith, baseAmount, outDetail.getBigDecimal("costPrice"), detailStorageId.intValue(), productId.intValue());
        if ((comfirm) && (isNegativeStock.booleanValue()) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0))
        {
          if (SysConfig.getConfig(configName, 15).booleanValue())
          {
            Storage storage = (Storage)Storage.dao.findById(configName, detailStorageId);
            ResultHelp help = new ResultHelp(Integer.valueOf(trIndex), productId, product.getStr("fullName"), 
              product.getStr("code"), sAmount, BigDecimalUtils.sub(sAmount, baseAmount), detailStorageId, 
              storage.getStr("fullName"), storage.getStr("code"), avgPrice, selectUnit);
            rList.add(help);
          }
        }
        else if (((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && (BigDecimalUtils.compare(sAmount, baseAmount) < 0)) || ((!isNegativeStock.booleanValue()) && (BigDecimalUtils.compare(sAmount, baseAmount) < 0)))
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
    Date time = new Date();
    bill.set("updateTime", time);
    if (draftId != 0) {
      billCodeIncrease(bill, "draftPost");
    } else {
      billCodeIncrease(bill, "add");
    }
    bill.save(configName);
    

    BillHistoryController.saveBillHistory(configName, bill, billTypeId, bill.getBigDecimal("inMoney"));
    


    BigDecimal outMoney = BigDecimal.ZERO;
    for (int i = 0; i < outDetailList.size(); i++)
    {
      Model outDetail = (Model)outDetailList.get(i);
      outDetail.set("id", null);
      outDetail.set("type", Integer.valueOf(2));
      Integer detailstorage = outDetail.getInt("storageId");
      if ((detailstorage == null) || (detailstorage.intValue() == 0)) {
        outDetail.set("storageId", outStorageId);
      }
      Integer productId = outDetail.getInt("productId");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        
        outDetail.set("billId", bill.getInt("id"));
        outDetail.set("updateTime", time);
        
        Integer selectUnitId = outDetail.getInt("selectUnitId");
        Integer detailStorageId = outDetail.getInt("storageId");
        BigDecimal price = outDetail.getBigDecimal("price");
        BigDecimal amount = outDetail.getBigDecimal("amount");
        if ((outDetail.getBigDecimal("costPrice") != null) && (price == null))
        {
          price = outDetail.getBigDecimal("costPrice");
          outDetail.set("price", price);
          outDetail.set("money", BigDecimalUtils.mul(price, amount));
        }
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        BigDecimal stockPrice = outDetail.getBigDecimal("costPrice");
        BigDecimal costMoney = BigDecimalUtils.mul(baseAmount, stockPrice);
        if (outDetail.getBigDecimal("costPrice") == null)
        {
          Map<String, String> map = ComFunController.outProductGetCostPrice(configName, detailStorageId.intValue(), product, outDetail.getBigDecimal("costPrice"), selectUnitId.intValue());
          stockPrice = new BigDecimal((String)map.get("costPrice"));
          costMoney = BigDecimalUtils.mul(baseAmount, stockPrice);
          outDetail.set("costPrice", stockPrice);
          price = outDetail.getBigDecimal("costPrice");
          outDetail.set("price", price);
          outDetail.set("money", BigDecimalUtils.mul(price, amount));
        }
        outMoney = BigDecimalUtils.add(outMoney, outDetail.getBigDecimal("money"));
        
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        outDetail.set("baseAmount", baseAmount);
        outDetail.set("basePrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          outDetail.set("giftAmount", baseAmount);
        }
        Integer costArith = product.getInt("costArith");
        
        BigDecimal sAmount = BigDecimal.ZERO;
        if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
          sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), detailStorageId.intValue(), outDetail.getBigDecimal("costPrice"), outDetail.getStr("batch"), outDetail.getDate("produceDate"), outDetail.getDate("produceEndDate"));
        } else {
          sAmount = Stock.dao.getStockAmount(configName, productId, detailStorageId);
        }
        if (((!isNegativeStock.booleanValue()) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue())) || ((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && 
          (BigDecimalUtils.compare(sAmount, amount) == -1)))
        {
          commonRollBack();
          setAttr("message", "错误类型：库存数量不够<br/> 商品编号：" + product.getStr("code") + "<br/> 商品全名：" + product.getStr("fullName"));
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
        StockController.stockSubChange(configName, costArith.intValue(), baseAmount, detailStorageId.intValue(), productId.intValue(), stockPrice, outDetail.getStr("batch"), outDetail.getDate("produceDate"), outDetail.getDate("produceEndDate"), null);
        
        StockRecordsController.addRecords(configName, billTypeId, "out", bill, outDetail, stockPrice, baseAmount, time, bill.getTimestamp("recodeDate"), outDetail.getInt("storageId"));
        

        AvgpriceConTroller.addAvgprice(configName, "out", detailStorageId, productId, baseAmount, costMoney);
        
        outDetail.save(configName);
      }
    }
    bill.set("outMoney", outMoney);
    bill.update(configName);
    for (int i = 0; i < inDetailList.size(); i++)
    {
      Model inDetail = (Model)inDetailList.get(i);
      inDetail.set("id", null);
      inDetail.set("type", Integer.valueOf(1));
      Integer detailstorage = inDetail.getInt("storageId");
      if ((detailstorage == null) || (detailstorage.intValue() == 0)) {
        inDetail.set("storageId", inStorageId);
      }
      Integer productId = inDetail.getInt("productId");
      BigDecimal amount = inDetail.getBigDecimal("amount");
      if (productId != null)
      {
        Product product = (Product)Product.dao.findById(configName, productId);
        Integer unitRelation1 = product.getInt("unitRelation1");
        BigDecimal unitRelation2 = product.getBigDecimal("unitRelation2");
        BigDecimal unitRelation3 = product.getBigDecimal("unitRelation3");
        inDetail.set("billId", bill.getInt("id"));
        
        inDetail.set("updateTime", time);
        
        Integer selectUnitId = inDetail.getInt("selectUnitId");
        Integer detailStorageId = inDetail.getInt("storageId");
        BigDecimal price = inDetail.getBigDecimal("price");
        if (detailStorageId == null) {
          detailStorageId = inStorageId;
        }
        BigDecimal basePrice = DwzUtils.getConversionPrice(price, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        BigDecimal baseAmount = DwzUtils.getConversionAmount(amount, selectUnitId, unitRelation1, unitRelation2, unitRelation3, Integer.valueOf(1));
        
        AvgpriceConTroller.addAvgprice(configName, "in", detailStorageId, productId, baseAmount, BigDecimalUtils.mul(baseAmount, basePrice));
        
        StockController.stockChange(configName, "in", detailStorageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), baseAmount, basePrice, inDetail.getStr("batch"), inDetail.getDate("produceDate"), inDetail.getDate("produceEndDate"));
        
        StockRecordsController.addRecords(configName, billTypeId, "in", bill, inDetail, basePrice, baseAmount, time, bill.getTimestamp("recodeDate"), inDetail.getInt("storageId"));
        
        inDetail.set("costPrice", basePrice);
        if ((price == null) || (BigDecimalUtils.compare(price, BigDecimal.ZERO) == 0)) {
          inDetail.set("giftAmount", baseAmount);
        }
        inDetail.set("baseAmount", baseAmount);
        inDetail.set("basePrice", basePrice);
        inDetail.save(configName);
      }
    }
    Map<String, Object> outRecord = new HashMap();
    outRecord.put("proAccountType", Integer.valueOf(AioConstants.ACCOUNT_MONEY1));
    outRecord.put("proAccountList", outDetailList);
    outRecord.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, outRecord);
    Map<String, Object> inRecord = new HashMap();
    inRecord.put("proAccountType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    inRecord.put("proAccountList", inDetailList);
    inRecord.put("accounts", getOtherAccount(BigDecimalUtils.sub(bill.getBigDecimal("inMoney"), bill.getBigDecimal("outMoney"))));
    inRecord.put("loginUserId", Integer.valueOf(loginUserId()));
    PayTypeController.addAccountsRecoder(configName, billTypeId, bill.getInt("id").intValue(), bill.getInt("unitId"), bill.getInt("staffId"), bill.getInt("departmentId"), null, null, null, inRecord);
    

    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    if (draftId != 0)
    {
      setDraftStrs();
      BusinessDraft.dao.deleteById(configName, Integer.valueOf(draftId));
      DismountDraftBill.dao.deleteById(configName, draftBillId);
      DismountDraftDetail.dao.delByBill(configName, draftBillId);
      
      StockDraftRecords.delRecords(configName, billTypeId, draftBillId.intValue());
      setAttr("message", "过账成功！");
      setAttr("callbackType", "closeCurrent");
      setAttr("navTabId", "businessDraftView");
    }
    else
    {
      setAttr("navTabId", "dismountBillView");
    }
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO);
    renderJson();
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
    data.put("reportName", "生产拆装单");
    List<String> headData = new ArrayList();
    headData.add("发货仓库 :" + getPara("outStorage.fullName", ""));
    

    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("部门 :" + getPara("department.fullName", ""));
    headData.add("入库仓库:" + getPara("inStorage.fullName", ""));
    String recodeDate = DateUtils.format(getParaToDate("dismountBill.recodeDate", new Date()));
    headData.add("录单日期:" + recodeDate);
    

    headData.add("摘要:" + getPara("dismountBill.remark", ""));
    headData.add("附加说明 :" + getPara("dismountBill.memo", ""));
    
    headData.add("单据编号:" + getPara("dismountBill.code", ""));
    Integer userId = null;
    if ((billId != null) && (billId.intValue() != 0))
    {
      Model bill = DismountBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    else if ((draftId != null) && (draftId.intValue() != 0))
    {
      Model bill = DismountDraftBill.dao.findById(configName, billId);
      if (bill != null) {
        userId = bill.getInt("userId");
      }
    }
    setBillUser(headData, userId);
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("类型");
    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("仓库编号");
    colTitle.add("仓库全名");
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
    colTitle.add("备注");
    colTitle.add("状态");
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
    

    List<Model> detailOutList = ModelUtils.batchInjectSortObjModel(getRequest(), DismountDetail.class, "dismountOutDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailOutList = DismountDetail.dao.getListByBillId(configName, billId, Integer.valueOf(2));
    }
    if ((detailOutList == null) || (detailOutList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailOutList.size(); i++)
    {
      rowData.add("出库");
      Model detail = (Model)detailOutList.get(i);
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      Integer storageId = detail.getInt("storageId");
      Storage storage = (Storage)Storage.dao.findById(configName, storageId);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
      rowData.add(trimNull(product.getStr("field")));
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
      
      rowData.add(trimNull(detail.get("memo")));
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      rowData.add(status);
      rowData.add(trimNull(product.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(DwzUtils.helpUnit(product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(detail.getStr("message")));
    }
    List<Model> detailInList = ModelUtils.batchInjectSortObjModel(getRequest(), DismountDetail.class, "dismountInDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailInList = DismountDetail.dao.getListByBillId(configName, billId, Integer.valueOf(1));
    }
    if ((detailInList == null) || (detailInList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailInList.size(); i++)
    {
      rowData.add("入库");
      Model detail = (Model)detailInList.get(i);
      int productId = detail.getInt("productId").intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      Integer storageId = detail.getInt("storageId");
      Storage storage = (Storage)Storage.dao.findById(configName, storageId);
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(product.getStr("code")));
      rowData.add(trimNull(product.getStr("fullName")));
      rowData.add(trimNull(product.getStr("standard")));
      rowData.add(trimNull(product.getStr("model")));
      rowData.add(trimNull(product.getStr("field")));
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
      rowData.add(trimNull(detail.getDate("produceDate")));
      rowData.add(trimNull(detail.getStr("batch")));
      rowData.add(DwzUtils.helpAmount(detail.getBigDecimal("amount"), product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(1)) + trimNull(product.getStr("calculateUnit1")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(2)) + trimNull(product.getStr("calculateUnit2")));
      rowData.add(DwzUtils.getConversionAmount(detail.getBigDecimal("amount"), selectUnitId, product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3"), Integer.valueOf(3)) + trimNull(product.getStr("calculateUnit3")));
      rowData.add(trimNull(detail.getBigDecimal("amount")));
      rowData.add(trimNull(detail.getBigDecimal("price")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      
      rowData.add(trimNull(detail.get("memo")));
      String status = "";
      if (!BigDecimalUtils.notNullZero(detail.getBigDecimal("price"))) {
        status = "赠品";
      }
      rowData.add(status);
      rowData.add(trimNull(product.getStr("barCode" + detail.getInt("selectUnitId"))));
      rowData.add(DwzUtils.helpUnit(product.getStr("calculateUnit1"), product.getStr("calculateUnit2"), product.getStr("calculateUnit3"), product.getInt("unitRelation1"), product.getBigDecimal("unitRelation2"), product.getBigDecimal("unitRelation3")));
      rowData.add(trimNull(detail.getStr("message")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public void look()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    DismountBill bill = (DismountBill)DismountBill.dao.findById(configName, Integer.valueOf(id));
    List<Model> detailInList = DismountDetail.dao.getListByBillId(configName, Integer.valueOf(id), Integer.valueOf(1));
    detailInList = addTrSize(detailInList, 5);
    List<Model> detailOutList = DismountDetail.dao.getListByBillId(configName, Integer.valueOf(id), Integer.valueOf(2));
    detailOutList = addTrSize(detailOutList, 5);
    
    setAttr("rowList", BillRow.dao.getListByBillId(configName, billTypeId, AioConstants.STATUS_ENABLE));
    
    setAttr("bill", bill);
    setAttr("detailInList", detailInList);
    setAttr("detailOutList", detailOutList);
    setAttr("tableId", Integer.valueOf(billTypeId));
    setAttr("orderFuJianIds", orderFuJianIds(billTypeId, bill.getInt("id").intValue(), AioConstants.IS_DRAFT_NO));
    render("look.html");
  }
  
  @Before({Tx.class})
  public static Map<String, Object> doRCW(String configName, Integer billId)
  {
    Map<String, Object> result = new HashMap();
    DismountBill bill = (DismountBill)DismountBill.dao.findById(configName, billId);
    if (bill == null)
    {
      result.put("status", Integer.valueOf(AioConstants.RCW_NO));
      result.put("message", "该拆装单单不存在");
      return result;
    }
    List<Model> detailInList = DismountDetail.dao.getListByBillId(configName, billId, Integer.valueOf(1));
    boolean comfirm = SysConfig.getConfig(configName, 1).booleanValue();
    Integer productId;
    for (Model detail : detailInList)
    {
      BigDecimal amount = detail.getBigDecimal("baseAmount");
      productId = detail.getInt("productId");
      Integer storageId = detail.getInt("storageId");
      Product product = (Product)detail.get("product");
      Integer costArith = product.getInt("costArith");
      
      BigDecimal sAmount = BigDecimal.ZERO;
      if (costArith.intValue() == AioConstants.PRD_COST_PRICE4) {
        sAmount = Stock.dao.getStockAmount(configName, productId.intValue(), storageId.intValue(), detail.getBigDecimal("costPrice"), detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
      } else {
        sAmount = Stock.dao.getStockAmount(configName, productId, storageId);
      }
      if ((!comfirm) && (AioConstants.PRD_COST_PRICE1 == costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0))
      {
        result.put("status", Integer.valueOf(AioConstants.RCW_NO));
        result.put("message", "该拆装单有商品库存不足");
        return result;
      }
      if ((AioConstants.PRD_COST_PRICE1 != costArith.intValue()) && (BigDecimalUtils.compare(sAmount, amount) < 0))
      {
        result.put("status", Integer.valueOf(AioConstants.RCW_NO));
        result.put("message", "该拆装单有商品库存不足");
        return result;
      }
    }
    bill.set("isRCW", Integer.valueOf(AioConstants.RCW_BY));
    bill.update(configName);
    DismountBill newBill = bill;
    newBill.set("id", null);
    newBill.set("isRCW", Integer.valueOf(AioConstants.RCW_VS));
    Date time = new Date();
    newBill.set("updateTime", time);
    
    billRcwAddRemark(bill, null);
    newBill.save(configName);
    
    BillHistoryController.saveBillHistory(configName, newBill, billTypeId, newBill.getBigDecimal("inMoney"));
    DismountDetail newDetail;
    for (Model odlDetail : detailInList)
    {
      newDetail = (DismountDetail)odlDetail;
      
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.set("id", null);
      newDetail.set("billId", newBill.getInt("id"));
      newDetail.set("updateTime", time);
      BigDecimal amount = odlDetail.getBigDecimal("baseAmount");
       productId = odlDetail.getInt("productId");
      Integer storageId = odlDetail.getInt("storageId");
      Product product = (Product)odlDetail.get("product");
      Integer costArith = product.getInt("costArith");
      BigDecimal costPrice = newDetail.getBigDecimal("costPrice");
      
      StockController.stockSubChange(configName, costArith.intValue(), amount, storageId.intValue(), productId.intValue(), costPrice, newDetail.getStr("batch"), newDetail.getDate("produceDate"), newDetail.getDate("produceEndDate"), null);
      
      Timestamp times = new Timestamp(time.getTime());
      StockRecordsController.addRecords(configName, billTypeId, "out", newBill, newDetail, costPrice, amount, time, times, newDetail.getInt("storageId"));
      
      AvgpriceConTroller.addAvgprice(configName, "out", storageId, productId, amount, BigDecimalUtils.mul(amount, costPrice));
      newDetail.save(configName);
    }
    List<Model> detailOutList = DismountDetail.dao.getListByBillId(configName, billId, Integer.valueOf(2));
    for (Model odlDetail : detailOutList)
    {
       newDetail = (DismountDetail)odlDetail;
      
      newDetail.set("rcwId", newDetail.getInt("id"));
      newDetail.set("id", null);
      newDetail.set("billId", newBill.getInt("id"));
      newDetail.set("updateTime", time);
      newDetail.save(configName);
      
      BigDecimal amount = newDetail.getBigDecimal("baseAmount");
       productId = newDetail.getInt("productId");
      Integer storageId = newDetail.getInt("storageId");
      Product product = (Product)newDetail.get("product");
      BigDecimal costPrice = newDetail.getBigDecimal("costPrice");
      
      AvgpriceConTroller.addAvgprice(configName, "in", storageId, productId, amount, BigDecimalUtils.mul(amount, costPrice));
      StockController.stockChange(configName, "in", storageId.intValue(), productId.intValue(), product.getInt("costArith").intValue(), amount, costPrice, newDetail.getStr("batch"), newDetail.getDate("produceDate"), newDetail.getDate("produceEndDate"));
      Timestamp times = new Timestamp(time.getTime());
      StockRecordsController.addRecords(configName, billTypeId, "in", newBill, newDetail, costPrice, amount, time, times, newDetail.getInt("storageId"));
    }
    PayTypeController.rcwAccountsRecoder(configName, billTypeId, billId, newBill.getInt("id"));
    result.put("status", Integer.valueOf(AioConstants.RCW_BY));
    result.put("message", "红冲成功");
    return result;
  }
  
  public static List<Record> getOtherAccount(BigDecimal dismountSpread)
  {
    List<Record> accountList = new ArrayList();
    Record spread = new Record();
    spread.set("moneyType", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
    spread.set("accountType", Integer.valueOf(AioConstants.ACCOUNT_TYPE0));
    spread.set("arapAccount", Integer.valueOf(AioConstants.ARAPACCOUNT0));
    spread.set("money", dismountSpread);
    spread.set("account", "00528");
    accountList.add(spread);
    

    return accountList;
  }
}
