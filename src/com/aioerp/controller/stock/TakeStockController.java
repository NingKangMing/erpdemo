package com.aioerp.controller.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.controller.reports.BusinessDraftController;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.stock.BreakageDraftBill;
import com.aioerp.model.stock.BreakageDraftDetail;
import com.aioerp.model.stock.OverflowDraftBill;
import com.aioerp.model.stock.OverflowDraftDetail;
import com.aioerp.model.stock.Stock;
import com.aioerp.model.stock.TakeStockBill;
import com.aioerp.model.stock.TakeStockDetail;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillRow;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class TakeStockController
  extends BaseController
{
  private static final String Reload_ID = "takeStockDisposePage";
  
  public void index()
  {
    String configName = loginConfigName();
    
    billCodeAuto(AioConstants.BILL_ROW_TYPE3);
    
    setAttr("createTime", DateUtils.getCurrentTime());
    setAttr("rowList", BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE3, AioConstants.STATUS_ENABLE));
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE3));
    
    loadOnlyOneBaseInfo(configName);
    

    notEditStaff();
    render("page.html");
  }
  
  @Before({Tx.class})
  public void add()
  {
    String configName = loginConfigName();
    TakeStockBill takeStockBill = (TakeStockBill)getModel(TakeStockBill.class);
    
    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, takeStockBill.getTimestamp("createTime"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    Date recodeDate = takeStockBill.getTimestamp("createTime");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    boolean hasOther = TakeStockBill.dao.codeIsExist(configName, takeStockBill.getStr("code"), 0);
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(takeStockBill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE3));
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer staffId = getParaToInt("staff.id", Integer.valueOf(0));
    if (staffId.intValue() == 0)
    {
      setAttr("message", "请经手人!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    if (storageId.intValue() == 0)
    {
      setAttr("message", "请选择盘点仓库!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), TakeStockDetail.class, "takeStockDetail");
    List<HelpUtil> helpUitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    String normalCode = getPara("billCode");
    if (!normalCode.equals(takeStockBill.getStr("code"))) {
      takeStockBill.set("codeIncrease", Integer.valueOf(-1));
    }
    takeStockBill.set("staffId", staffId);
    takeStockBill.set("storageId", storageId);
    takeStockBill.set("userId", Integer.valueOf(loginUserId()));
    

    takeStockBill.save(configName);
    
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE3, takeStockBill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      Integer productId = detail.getInt("productId");
      BigDecimal amount = detail.getBigDecimal("takeStockAmount");
      if (productId != null)
      {
        BigDecimal costPrice = ((HelpUtil)helpUitlList.get(i)).getCostPrice();
        BigDecimal avgPrice = null;
        BigDecimal stockAmount = null;
        BigDecimal money = null;
        if (costPrice == null)
        {
          avgPrice = Stock.dao.getProAvgPriceBySgeIdAndProIds(configName, storageId, productId);
          stockAmount = Stock.dao.getStockAmount(configName, productId, storageId);
        }
        else
        {
          avgPrice = costPrice;
          stockAmount = Stock.dao.getStockAmount(configName, productId.intValue(), storageId.intValue(), avgPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        }
        detail.set("billId", takeStockBill.getInt("id"));
        detail.set("price", avgPrice);
        detail.set("stockAmount", stockAmount);
        money = BigDecimalUtils.mul(stockAmount, avgPrice);
        money.setScale(4, 4);
        detail.set("money", money);
        detail.set("gainAndLossAmount", BigDecimalUtils.sub(amount, stockAmount));
        BigDecimal gainMoney = BigDecimalUtils.mul(amount, avgPrice).setScale(4, 4);
        detail.set("gainAndLossMoney", BigDecimalUtils.sub(gainMoney, money));
        
        detail.save(configName);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("navTabId", "takeStockView");
    if (!SysConfig.getConfig(configName, 9).booleanValue()) {
      setAttr("isClose", Boolean.valueOf(true));
    }
    renderJson();
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String configName = loginConfigName();
    TakeStockBill takeStockBill = (TakeStockBill)getModel(TakeStockBill.class);
    Integer billId = takeStockBill.getInt("id");
    
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    Date recodeDate = takeStockBill.getTimestamp("createTime");
    if (("0".equals(codeCurrentFit)) && (!DateUtils.isEqualDate(new Date(), recodeDate)))
    {
      setAttr("message", "录单日期必须和当前日期一致!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    boolean hasOther = TakeStockBill.dao.codeIsExist(configName, takeStockBill.getStr("code"), billId.intValue());
    if (("1".equals(codeAllowRep)) && (
      (hasOther) || (StringUtils.isBlank(takeStockBill.getStr("code")))))
    {
      setAttr("reloadCodeTitle", "编号已经存在,是否重新生成编号？");
      setAttr("billTypeId", Integer.valueOf(AioConstants.BILL_ROW_TYPE3));
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer staffId = getParaToInt("staff.id", Integer.valueOf(0));
    if (staffId.intValue() == 0)
    {
      setAttr("message", "请经手人!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    Integer storageId = getParaToInt("storage.id", Integer.valueOf(0));
    if (storageId.intValue() == 0)
    {
      setAttr("message", "请选择盘点仓库!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), TakeStockDetail.class, "takeStockDetail");
    if (detailList.size() == 0)
    {
      setAttr("message", "请选择商品!");
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      renderJson();
      return;
    }
    String normalCode = getPara("billCode");
    if (!normalCode.equals(takeStockBill.getStr("code"))) {
      takeStockBill.set("codeIncrease", Integer.valueOf(-1));
    }
    takeStockBill.set("staffId", staffId);
    takeStockBill.set("storageId", storageId);
    takeStockBill.set("userId", Integer.valueOf(loginUserId()));
    takeStockBill.update(configName);
    
    ComFunController.orderFuJian(configName, getPara("orderFuJianIds", ""), AioConstants.BILL_ROW_TYPE3, takeStockBill.getInt("id").intValue(), AioConstants.IS_DRAFT);
    

    List<Integer> oldIds = TakeStockDetail.dao.getListByDetailId(configName, billId);
    List<Integer> newIds = new ArrayList();
    Model detail;
    for (int i = 0; i < detailList.size(); i++)
    {
      detail = (Model)detailList.get(i);
      Integer id = detail.getInt("id");
      newIds.add(id);
      Integer productId = detail.getInt("productId");
      Product product = (Product)Product.dao.findById(configName, productId);
      Integer costArith = product.getInt("costArith");
      BigDecimal amount = detail.getBigDecimal("takeStockAmount");
      if (productId != null)
      {
        BigDecimal avgPrice = null;
        BigDecimal stockAmount = null;
        BigDecimal money = null;
        if (costArith.intValue() == 1)
        {
          avgPrice = Stock.dao.getProAvgPriceBySgeIdAndProIds(configName, storageId, productId);
          stockAmount = Stock.dao.getStockAmount(configName, productId, storageId);
        }
        else if (costArith.intValue() == 4)
        {
          avgPrice = detail.getBigDecimal("price");
          stockAmount = Stock.dao.getStockAmount(configName, productId.intValue(), storageId.intValue(), avgPrice, detail.getStr("batch"), detail.getDate("produceDate"), detail.getDate("produceEndDate"));
        }
        detail.set("billId", takeStockBill.getInt("id"));
        detail.set("price", avgPrice);
        detail.set("stockAmount", stockAmount);
        money = BigDecimalUtils.mul(stockAmount, avgPrice);
        money.setScale(4, 4);
        detail.set("money", money);
        detail.set("gainAndLossAmount", BigDecimalUtils.sub(amount, stockAmount));
        BigDecimal gainMoney = BigDecimalUtils.mul(amount, avgPrice).setScale(4, 4);
        detail.set("gainAndLossMoney", BigDecimalUtils.sub(gainMoney, money));
        if (id == null)
        {
          detail.set("billId", takeStockBill.getInt("id"));
          detail.save(configName);
        }
        else if (id != null)
        {
          detail.update(configName);
        }
      }
    }
    for (Integer id : oldIds) {
      if (!newIds.contains(id)) {
        TakeStockDetail.dao.deleteById(configName, id);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("callbackType", "closeCurrent");
    setAttr("navTabId", "takeStockDisposeView");
    renderJson();
  }
  
  public void searchTakeStock()
    throws ParseException
  {
    setStartDateAndEndDate();
    render("searchTakeStock.html");
  }
  
  public void takeStockDisposePage()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "takBill.createTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    String startDate = getPara("startDate");
    String endDate = getPara("endDate");
    String storageId = getPara("storage.id", getPara("storageId"));
    if (getPara("userSearchDate", "").equals("checked")) {
      SysUserSearchDate.dao.setUserSearchDate(configName, loginUserId(), startDate, endDate);
    }
    Storage storage = (Storage)Storage.dao.findById(configName, storageId);
    String sgePids = "";
    if (storage != null) {
      sgePids = storage.getStr("pids");
    }
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    pmap.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    
    Map<String, Object> map = TakeStockBill.dao.getPageBySgePids(configName, pmap, sgePids, startDate, endDate, "takeStockDisposePage", pageNum, numPerPage, orderField, orderDirection);
    

    columnConfig("cc530");
    
    setAttr("startDate", startDate);
    setAttr("endDate", endDate);
    setAttr("storageId", storageId);
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    render("takeStockDisposePage.html");
  }
  
  public void editBill()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0).intValue();
    TakeStockBill tBill = (TakeStockBill)TakeStockBill.dao.findById(configName, Integer.valueOf(billId));
    if (tBill == null) {
      return;
    }
    List<Model> detailList = TakeStockDetail.dao.getList(configName, Integer.valueOf(billId));
    addTrSize(detailList, 15);
    List<Model> rowList = BillRow.dao.getListByBillId(configName, AioConstants.BILL_ROW_TYPE3, AioConstants.STATUS_ENABLE);
    int status = tBill.getInt("status").intValue();
    setAttr("bill", tBill);
    setAttr("details", detailList);
    setAttr("rowList", rowList);
    setAttr("tableId", Integer.valueOf(AioConstants.BILL_ROW_TYPE3));
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    
    setAttr("orderFuJianIds", orderFuJianIds(AioConstants.BILL_ROW_TYPE3, tBill.getInt("id").intValue(), AioConstants.IS_DRAFT));
    if (status == 1) {
      render("edit.html");
    } else {
      render("look.html");
    }
  }
  
  @Before({Tx.class})
  public void delBill()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0).intValue();
    Boolean flag = Boolean.valueOf(TakeStockBill.dao.deleteById(configName, Integer.valueOf(billId)));
    int row = Db.use(configName).update(" DELETE FROM cc_takestock_detail WHERE billId = ? ", new Object[] { Integer.valueOf(billId) });
    if ((flag.booleanValue()) && (row >= 0))
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "takeStockDisposePage");
    }
    else
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "异常，网络超时！");
    }
    renderJson();
  }
  
  @Before({Tx.class})
  public void takeStockDispose()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0).intValue();
    TakeStockBill tBill = (TakeStockBill)TakeStockBill.dao.findById(configName, Integer.valueOf(billId));
    tBill.set("status", Integer.valueOf(2));
    tBill.update(configName);
    
    String sql = "SELECT * FROM cc_takestock_detail WHERE billId=?";
    List<Model> detailList = TakeStockDetail.dao.find(configName, sql, new Object[] { Integer.valueOf(billId) });
    
    Boolean breakgeBillFlag = Boolean.valueOf(false);
    Boolean overflowBillFlag = Boolean.valueOf(false);
    
    List<Model> bList = new ArrayList();
    List<Model> oList = new ArrayList();
    BigDecimal bAmounts = null;
    BigDecimal bMoneys = null;
    BigDecimal oAmounts = null;
    BigDecimal oMoneys = null;
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      BigDecimal gainAndLossAmount = detail.getBigDecimal("gainAndLossAmount");
      if (BigDecimalUtils.compare(gainAndLossAmount, BigDecimal.ZERO) == -1)
      {
        breakgeBillFlag = Boolean.valueOf(true);
        bAmounts = BigDecimalUtils.add(bAmounts, BigDecimalUtils.mul(gainAndLossAmount, new BigDecimal(-1)));
        bMoneys = BigDecimalUtils.add(bMoneys, BigDecimalUtils.mul(detail.getBigDecimal("gainAndLossMoney"), new BigDecimal(-1)));
        bList.add(detail);
      }
      else if (BigDecimalUtils.compare(gainAndLossAmount, BigDecimal.ZERO) == 1)
      {
        overflowBillFlag = Boolean.valueOf(true);
        oAmounts = BigDecimalUtils.add(oAmounts, gainAndLossAmount);
        oMoneys = BigDecimalUtils.add(oMoneys, detail.getBigDecimal("gainAndLossMoney"));
        oList.add(detail);
      }
    }
    StringBuffer msg;
    if ((breakgeBillFlag.booleanValue()) || (overflowBillFlag.booleanValue()))
    {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
       msg = new StringBuffer("盘点单生成草稿成功！生成下列编号的草稿:");
      if (breakgeBillFlag.booleanValue())
      {
        BreakageDraftBill bBill = new BreakageDraftBill();
        String code = "BSD-" + sdf.format(new Date());
        bBill.set("code", code);
        bBill.set("recodeDate", tBill.getTimestamp("createTime"));
        bBill.set("staffId", tBill.getInt("staffId"));
        bBill.set("departmentId", tBill.getInt("departmentId"));
        bBill.set("storageId", tBill.getInt("storageId"));
        bBill.set("remark", "库存盘点草稿");
        bBill.set("amounts", bAmounts);
        bBill.set("moneys", bMoneys);
        bBill.set("updateTime", DateUtils.getCurrentTime());
        bBill.set("userId", Integer.valueOf(loginUserId()));
        Map<String, String> map = billCodeAuto(AioConstants.BILL_ROW_TYPE8);
        bBill.set("codeIncrease", String.valueOf(map.get("codeIncrease")));
        bBill.save(configName);
        for (Model model : bList)
        {
          BreakageDraftDetail bDetail = new BreakageDraftDetail();
          bDetail.set("billId", bBill.getInt("id"));
          bDetail.set("productId", model.getInt("productId"));
          bDetail.set("storageId", bBill.getInt("storageId"));
          bDetail.set("selectUnitId", model.getInt("selectUnitId"));
          bDetail.set("amount", BigDecimalUtils.mul(model.getBigDecimal("gainAndLossAmount"), new BigDecimal(-1)));
          bDetail.set("price", model.getBigDecimal("price"));
          bDetail.set("batch", model.getStr("batch"));
          bDetail.set("produceDate", model.getDate("produceDate"));
          bDetail.set("money", BigDecimalUtils.mul(model.getBigDecimal("gainAndLossMoney"), new BigDecimal(-1)));
          bDetail.set("baseAmount", BigDecimalUtils.mul(model.getBigDecimal("gainAndLossAmount"), new BigDecimal(-1)));
          bDetail.set("basePrice", model.getBigDecimal("price"));
          bDetail.set("updateTime", DateUtils.getCurrentTime());
          
          bDetail.save(configName);
        }
        BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE8, bBill, bBill.getBigDecimal("moneys"));
        msg.append("【" + code + "】");
      }
      if (overflowBillFlag.booleanValue())
      {
        OverflowDraftBill oBill = new OverflowDraftBill();
        String code = "BYD-" + sdf.format(new Date());
        oBill.set("code", code);
        oBill.set("recodeDate", tBill.getTimestamp("createTime"));
        oBill.set("staffId", tBill.getInt("staffId"));
        oBill.set("departmentId", tBill.getInt("departmentId"));
        oBill.set("storageId", tBill.getInt("storageId"));
        oBill.set("amounts", oAmounts);
        oBill.set("moneys", oMoneys);
        oBill.set("remark", "库存盘点草稿");
        oBill.set("userId", Integer.valueOf(loginUserId()));
        oBill.set("updateTime", DateUtils.getCurrentTime());
        Map<String, String> map = billCodeAuto(AioConstants.BILL_ROW_TYPE9);
        oBill.set("codeIncrease", String.valueOf(map.get("codeIncrease")));
        oBill.save(configName);
        for (Model model : oList)
        {
          OverflowDraftDetail oDetail = new OverflowDraftDetail();
          oDetail.set("billId", oBill.getInt("id"));
          oDetail.set("productId", model.getInt("productId"));
          oDetail.set("storageId", oBill.getInt("storageId"));
          oDetail.set("selectUnitId", model.getInt("selectUnitId"));
          oDetail.set("amount", model.getBigDecimal("gainAndLossAmount"));
          oDetail.set("price", model.getBigDecimal("price"));
          oDetail.set("batch", model.getStr("batch"));
          oDetail.set("produceDate", model.getDate("produceDate"));
          oDetail.set("money", model.getBigDecimal("gainAndLossMoney"));
          oDetail.set("baseAmount", model.getBigDecimal("gainAndLossAmount"));
          oDetail.set("basePrice", model.getBigDecimal("price"));
          oDetail.set("updateTime", DateUtils.getCurrentTime());
          
          oDetail.save(configName);
        }
        BusinessDraftController.saveBillDraft(configName, AioConstants.BILL_ROW_TYPE9, oBill, oBill.getBigDecimal("moneys"));
        msg.append("【" + code + "】");
      }
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "takeStockDisposePage");
    }
    else
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      msg = new StringBuffer("此张盘点单明细所产生的盈亏数量都为0，无法生成报损、报溢单，请确认后重试");
    }
    setAttr("message", msg.toString());
    renderJson();
  }
  
  public void print()
  {
    Integer billId = getParaToInt("billId");
    Integer editId = getParaToInt("takeStockBill.id");
    String configName = loginConfigName();
    
    SysConfig.getConfig(configName, 12).booleanValue();
    
    Map<String, Object> data = new HashMap();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "库存盘点单");
    List<String> headData = new ArrayList();
    
    headData.add("盘点时间:" + getPara("takeStockBill.createTime", ""));
    headData.add("单据编号:" + getPara("takeStockBill.code", ""));
    headData.add("经手人 :" + getPara("staff.name", ""));
    headData.add("盘点仓库:" + getPara("storage.fullName", ""));
    headData.add("摘要:" + getPara("takeStockBill.remark", ""));
    
    Model bill = null;
    if ((billId != null) && (billId.intValue() != 0)) {
      bill = TakeStockBill.dao.findById(configName, billId);
    } else if ((editId != null) && (editId.intValue() != 0)) {
      bill = TakeStockBill.dao.findById(configName, editId);
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
    colTitle.add("单位");
    colTitle.add("盘点数量");
    colTitle.add("辅助盘点数量");
    colTitle.add("库存数量");
    colTitle.add("库存辅助数量");
    colTitle.add("单价");
    colTitle.add("金额");
    colTitle.add("盈亏数量");
    colTitle.add("盈亏金额");
    colTitle.add("批号");
    colTitle.add("生产日期");
    colTitle.add("条码");
    colTitle.add("零售价");
    colTitle.add("零售金额");
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
    colWidth.add("500");
    colWidth.add("500");
    colWidth.add("500");
    
    List<String> rowData = new ArrayList();
    List<Model> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), TakeStockDetail.class, "takeStockDetail");
    if ((billId != null) && (billId.intValue() != 0)) {
      detailList = TakeStockDetail.dao.getList(configName, billId);
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
      rowData.add(trimNull(product.getStr("calculateUnit1")));
      rowData.add(trimNull(detail.getBigDecimal("takeStockAmount")));
      rowData.add(trimNull(detail.getStr("helpTakeStockAmount")));
      rowData.add(trimNull(detail.getBigDecimal("stockAmount")));
      rowData.add(trimNull(detail.getStr("helpStockAmount")));
      rowData.add(trimNull(detail.getBigDecimal("price")));
      rowData.add(trimNull(detail.getBigDecimal("money")));
      rowData.add(trimNull(detail.getBigDecimal("gainAndLossAmount")));
      rowData.add(trimNull(detail.getBigDecimal("gainAndLossMoney")));
      rowData.add(trimNull(detail.getStr("batchNum")));
      rowData.add(trimNull(detail.getDate("creatDate")));
      rowData.add(trimNull(product.getStr("barCode1")));
      rowData.add(trimNull(detail.getBigDecimal("rlPrice")));
      rowData.add(trimNull(detail.getBigDecimal("rlMoney")));
      rowData.add(trimNull(detail.getDate("produceEndDate")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public void printReports()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "盘点处理");
    List<String> headData = new ArrayList();
    
    String startDate = getPara("startDate", "");
    String endDate = getPara("endDate", "");
    String storageId = getPara("storage.id", getPara("storageId"));
    
    Storage storage = (Storage)Storage.dao.findById(configName, storageId);
    
    headData.add("查询日期:" + startDate + " 至 " + endDate);
    if ("0".equals(storageId))
    {
      headData.add("盘点仓库:全部仓库");
    }
    else
    {
      Storage sge = (Storage)Storage.dao.findById(configName, storageId);
      headData.add("盘点仓库:" + sge.getStr("fullName"));
    }
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    
    colTitle.add("行号");
    colTitle.add("单据编号");
    colTitle.add("录单时间");
    colTitle.add("仓库编号");
    colTitle.add("仓库名称");
    colTitle.add("职员编号");
    colTitle.add("职员名称");
    colTitle.add("摘要");
    colTitle.add("状态");
    
    colWidth.add("30");
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
    

    int pageNum = 1;
    int numPerPage = totalCount;
    String orderField = getPara("orderField", "takBill.createTime");
    String orderDirection = getPara("orderDirection", "desc");
    

    String sgePids = "";
    if (storage != null) {
      sgePids = storage.getStr("pids");
    }
    Map<String, Object> pmap = new HashMap();
    pmap.put(PRODUCT_PRIVS, basePrivs(PRODUCT_PRIVS));
    pmap.put(STORAGE_PRIVS, basePrivs(STORAGE_PRIVS));
    pmap.put(STAFF_PRIVS, basePrivs(STAFF_PRIVS));
    
    Map<String, Object> pageMap = TakeStockBill.dao.getPageBySgePids(configName, pmap, sgePids, startDate, endDate, "takeStockDisposePage", pageNum, numPerPage, orderField, orderDirection);
    
    List<Model> list = (List)pageMap.get("pageList");
    for (int i = 0; i < list.size(); i++)
    {
      TakeStockBill r = (TakeStockBill)list.get(i);
      rowData.add(trimNull(i + 1));
      
      TakeStockBill tb = (TakeStockBill)r.get("takBill");
      Storage sge = (Storage)r.get("sge");
      Staff stf = (Staff)r.get("stf");
      
      rowData.add(trimNull(tb.get("code")));
      rowData.add(trimNull(tb.get("createTime")));
      rowData.add(trimNull(sge.get("code")));
      rowData.add(trimNull(sge.get("fullName")));
      rowData.add(trimNull(stf.get("code")));
      rowData.add(trimNull(stf.get("fullName")));
      rowData.add(trimNull(tb.get("remark")));
      int status = ((Integer)tb.get("status")).intValue();
      if (status == 2) {
        rowData.add("已处理");
      } else {
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
