package com.aioerp.controller.reports;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.bought.PurchaseBarterBillController;
import com.aioerp.controller.bought.PurchaseBillController;
import com.aioerp.controller.bought.PurchaseReturnBillController;
import com.aioerp.controller.finance.AccountVoucherController;
import com.aioerp.controller.finance.AddAssetsController;
import com.aioerp.controller.finance.AdjustCostController;
import com.aioerp.controller.finance.DeprAssetsController;
import com.aioerp.controller.finance.OtherEarnController;
import com.aioerp.controller.finance.PayMoneyController;
import com.aioerp.controller.finance.SubAssetsController;
import com.aioerp.controller.finance.TransferController;
import com.aioerp.controller.finance.changegetorpay.ChangePayOrGetController;
import com.aioerp.controller.finance.feebill.FeeBillController;
import com.aioerp.controller.finance.getmoney.GetMoneyController;
import com.aioerp.controller.sell.SellBarterBillController;
import com.aioerp.controller.sell.sell.SellController;
import com.aioerp.controller.sell.sellreturn.SellReturnController;
import com.aioerp.controller.stock.BreakageController;
import com.aioerp.controller.stock.DifftAllotController;
import com.aioerp.controller.stock.DismountBillController;
import com.aioerp.controller.stock.OverflowController;
import com.aioerp.controller.stock.ParityAllotController;
import com.aioerp.controller.stock.other.OtherInController;
import com.aioerp.controller.stock.other.OtherOutController;
import com.aioerp.db.reports.BillHistory;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.aioerp.model.finance.PayType;
import com.aioerp.model.stock.StockRecords;
import com.aioerp.model.sys.BillType;
import com.aioerp.model.sys.User;
import com.aioerp.model.sys.end.YearEnd;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
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

public class BillHistoryController
  extends BaseController
{
  private static final String LIST_ID = "billHistoryList";
  
  public void index()
    throws Exception
  {
    String configName = loginConfigName();
    String startTime = getPara("startDate", getPara("startTime"));
    String endTime = getPara("endDate", getPara("endTime"));
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "postTime");
    String orderDirection = getPara("orderDirection", "asc");
    

    setAttr("billTypes", BillType.dao.getList(configName, AioConstants.BILL_SORT0, AioConstants.BILL_TYPE_1));
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    

    String unitId = getPara("unitId");
    String staffId = getPara("staffId");
    String productId = getPara("productId");
    String storageId = getPara("storageId");
    String departmentId = getPara("departmentId");
    String accountId = getPara("accountId");
    Integer billTypeId = getParaToInt("billTypeId", Integer.valueOf(0));
    String isCoupon = getPara("isCoupon", "0");
    String billCode = getPara("billCode");
    String remark = getPara("remark");
    String memo = getPara("memo");
    String isMember = getPara("isMember", "0");
    String priceCase = getPara("priceCase", "0");
    String price = getPara("price");
    String discountCase = getPara("discountCase", "0");
    String discount = getPara("discount");
    String taxrateCase = getPara("taxrateCase", "0");
    String taxrate = getPara("taxrate");
    
    setAttr("unitId", unitId);
    setAttr("staffId", staffId);
    setAttr("productId", productId);
    setAttr("storageId", storageId);
    setAttr("departmentId", departmentId);
    setAttr("accountId", accountId);
    setAttr("billTypeId", billTypeId);
    setAttr("isCoupon", isCoupon);
    setAttr("billCode", billCode);
    setAttr("remark", remark);
    setAttr("memo", memo);
    setAttr("isMember", isMember);
    setAttr("priceCase", priceCase);
    setAttr("price", price);
    setAttr("discountCase", discountCase);
    setAttr("discount", discount);
    setAttr("taxrateCase", taxrateCase);
    setAttr("taxrate", taxrate);
    
    Map<String, Object> map = BillHistory.dao.getPage(configName, "billHistoryList", pageNum, numPerPage, orderField, orderDirection, startTime, endTime, billTypeId.intValue(), -1, 
      unitId, staffId, productId, storageId, departmentId, accountId, isMember, 
      billCode, remark, memo, isCoupon, 
      priceCase, price, discountCase, discount, taxrateCase, taxrate);
    

    columnConfig("djzx500");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("page.html");
  }
  
  public void list()
    throws Exception
  {
    String configName = loginConfigName();
    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    Integer billTypeId = getParaToInt("selectVal1", Integer.valueOf(0));
    Integer isRcw = getParaToInt("selectVal2", Integer.valueOf(0));
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "postTime");
    String orderDirection = getPara("orderDirection", "asc");
    


    setAttr("billTypes", BillType.dao.getList(configName, AioConstants.BILL_SORT0, AioConstants.BILL_TYPE_1));
    
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
    
    setAttr("billTypeId", billTypeId);
    setAttr("isRcw", isRcw);
    

    String unitId = getPara("unitId");
    String staffId = getPara("staffId");
    String productId = getPara("productId");
    String storageId = getPara("storageId");
    String departmentId = getPara("departmentId");
    String accountId = getPara("accountId");
    String isCoupon = getPara("isCoupon", "0");
    String billCode = getPara("billCode");
    String remark = getPara("remark");
    String memo = getPara("memo");
    String isMember = getPara("isMember", "0");
    String priceCase = getPara("priceCase", "0");
    String price = getPara("price");
    String discountCase = getPara("discountCase", "0");
    String discount = getPara("discount");
    String taxrateCase = getPara("taxrateCase", "0");
    String taxrate = getPara("taxrate");
    
    setAttr("unitId", unitId);
    setAttr("staffId", staffId);
    setAttr("productId", productId);
    setAttr("storageId", storageId);
    setAttr("departmentId", departmentId);
    setAttr("accountId", accountId);
    setAttr("isCoupon", isCoupon);
    setAttr("billCode", billCode);
    setAttr("remark", remark);
    setAttr("memo", memo);
    setAttr("isMember", isMember);
    setAttr("priceCase", priceCase);
    setAttr("price", price);
    setAttr("discountCase", discountCase);
    setAttr("discount", discount);
    setAttr("taxrateCase", taxrateCase);
    setAttr("taxrate", taxrate);
    

    Map<String, Object> map = BillHistory.dao.getPage(configName, "billHistoryList", pageNum, numPerPage, orderField, orderDirection, startTime, endTime, billTypeId.intValue(), isRcw.intValue(), 
      unitId, staffId, productId, storageId, departmentId, accountId, isMember, 
      billCode, remark, memo, isCoupon, 
      priceCase, price, discountCase, discount, taxrateCase, taxrate);
    

    columnConfig("djzx500");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("list.html");
  }
  
  @Before({Tx.class})
  public void doRCW()
    throws SQLException
  {
    String configName = loginConfigName();
    Integer billId = getParaToInt(0);
    Integer billType = getParaToInt(1);
    Integer recodeId = getParaToInt(2);
    
    Integer userId = Integer.valueOf(loginUserId());
    boolean hashasPermitted = hasPermitted("1102-s");
    if (!hashasPermitted)
    {
      Model bill = BillHistory.dao.getModelByUserId(configName, recodeId.intValue(), userId);
      if (bill != null)
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN301);
        setAttr("message", "该用户没有红冲自己单据的权限！");
        renderJson();
        return;
      }
    }
    boolean hasPermitted = hasPermitted("1103-s");
    if (!hasPermitted)
    {
      Model bill = BillHistory.dao.getModelById(configName, recodeId.intValue());
      if ((bill != null) && (userId != bill.getInt("userId")))
      {
        setAttr("statusCode", AioConstants.HTTP_RETURN301);
        setAttr("message", "该用户没有红冲他人单据的权限！");
        renderJson();
        return;
      }
    }
    if ((billId == null) || (billType == null) || (recodeId == null))
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN301);
      setAttr("message", "请选择单据！");
      renderJson();
      return;
    }
    Boolean flag = Boolean.valueOf(false);
    BillHistory bhty = (BillHistory)BillHistory.dao.findById(configName, recodeId);
    

    Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bhty.getTimestamp("recodeDate"));
    if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
    {
      renderJson(rmap);
      return;
    }
    Integer isRcw = bhty.getInt("isRCW");
    if ((isRcw.intValue() == AioConstants.RCW_BY) || (isRcw.intValue() == AioConstants.RCW_VS))
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN301);
      setAttr("message", "这张单据已经是红冲单据，不能再反冲！");
      renderJson();
      return;
    }
    Map<String, Object> map = new HashMap();
    try
    {
      if (AioConstants.BILL_ROW_TYPE3 != billType.intValue()) {
        if (AioConstants.BILL_ROW_TYPE4 == billType.intValue()) {
          map = SellController.doRCW(configName, billId.intValue());
        } else if (AioConstants.BILL_ROW_TYPE5 == billType.intValue()) {
          map = PurchaseBillController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE6 == billType.intValue()) {
          map = PurchaseReturnBillController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE7 == billType.intValue()) {
          map = SellReturnController.doRCW(configName, billId.intValue());
        } else if (AioConstants.BILL_ROW_TYPE8 == billType.intValue()) {
          map = BreakageController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE9 == billType.intValue()) {
          map = OverflowController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE10 == billType.intValue()) {
          map = OtherInController.doRCW(configName, billId.intValue());
        } else if (AioConstants.BILL_ROW_TYPE11 == billType.intValue()) {
          map = OtherOutController.doRCW(configName, billId.intValue());
        } else if (AioConstants.BILL_ROW_TYPE12 == billType.intValue()) {
          map = PurchaseBarterBillController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE13 == billType.intValue()) {
          map = SellBarterBillController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE14 == billType.intValue()) {
          map = ParityAllotController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE15 == billType.intValue()) {
          map = DifftAllotController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE16 == billType.intValue()) {
          map = DismountBillController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE17 == billType.intValue()) {
          map = GetMoneyController.doRCW(configName, billId.intValue());
        } else if (AioConstants.BILL_ROW_TYPE18 == billType.intValue()) {
          map = OtherEarnController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE19 == billType.intValue()) {
          map = PayMoneyController.doRCW(configName, billId.intValue());
        } else if (AioConstants.BILL_ROW_TYPE20 == billType.intValue()) {
          map = AdjustCostController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE21 == billType.intValue()) {
          map = TransferController.doRCW(configName, billId.intValue());
        } else if (AioConstants.BILL_ROW_TYPE22 == billType.intValue()) {
          map = FeeBillController.doRCW(configName, billId);
        } else if (AioConstants.BILL_ROW_TYPE23 == billType.intValue()) {
          map = AddAssetsController.doRCW(configName, billId.intValue());
        } else if (AioConstants.BILL_ROW_TYPE24 == billType.intValue()) {
          map = DeprAssetsController.doRCW(configName, billId.intValue());
        } else if (AioConstants.BILL_ROW_TYPE25 == billType.intValue()) {
          map = SubAssetsController.doRCW(configName, billId.intValue());
        } else if (AioConstants.BILL_ROW_TYPE26 == billType.intValue()) {
          map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
        } else if (AioConstants.BILL_ROW_TYPE27 == billType.intValue()) {
          map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
        } else if (AioConstants.BILL_ROW_TYPE28 == billType.intValue()) {
          map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
        } else if (AioConstants.BILL_ROW_TYPE29 == billType.intValue()) {
          map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
        } else if (AioConstants.BILL_ROW_TYPE30 == billType.intValue()) {
          map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
        } else if (AioConstants.BILL_ROW_TYPE31 == billType.intValue()) {
          map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
        } else if (AioConstants.BILL_ROW_TYPE32 == billType.intValue()) {
          map = AccountVoucherController.doRCW(configName, billId);
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      setAttr("statusCode", AioConstants.HTTP_RETURN301);
      setAttr("message", "系统异常，请联系管理员！");
      renderJson();
      return;
    }
    String mapStatus = map.get("status").toString();
    if (mapStatus.equals(String.valueOf(AioConstants.RCW_NO)))
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN301);
      setAttr("message", map.get("message"));
      renderJson();
      return;
    }
    flag = Boolean.valueOf(bhty.set("isRCW", Integer.valueOf(AioConstants.RCW_BY)).update(configName));
    StockRecords.batchUpdateIsRCW(configName, billType, billId);
    if (flag.booleanValue())
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "billHistoryList");
    }
    else
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "系统异常，请联系管理员！");
    }
    renderJson();
  }
  
  public void doRCWBillType()
    throws ParseException
  {
    String configName = loginConfigName();
    
    int[] bill_type = { AioConstants.BILL_ROW_TYPE4 };
    List<Model> list = BillHistory.dao.getList(configName, null, null, bill_type);
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Model mm = (Model)list.get(i);
        Integer billId = mm.getInt("billId");
        Integer billType = mm.getInt("billTypeId");
        Integer recodeId = mm.getInt("id");
        

        Integer userId = Integer.valueOf(loginUserId());
        boolean hashasPermitted = hasPermitted("1102-s");
        if (!hashasPermitted)
        {
          Model bill = BillHistory.dao.getModelByUserId(configName, recodeId.intValue(), userId);
          if (bill != null)
          {
            setAttr("statusCode", AioConstants.HTTP_RETURN301);
            setAttr("message", "该用户没有红冲自己单据的权限！");
            renderJson();
            return;
          }
        }
        boolean hasPermitted = hasPermitted("1103-s");
        if (!hasPermitted)
        {
          Model bill = BillHistory.dao.getModelById(configName, recodeId.intValue());
          if ((bill != null) && (userId != bill.getInt("userId")))
          {
            setAttr("statusCode", AioConstants.HTTP_RETURN301);
            setAttr("message", "该用户没有红冲他人单据的权限！");
            renderJson();
            return;
          }
        }
        if ((billId == null) || (billType == null) || (recodeId == null))
        {
          setAttr("statusCode", AioConstants.HTTP_RETURN301);
          setAttr("message", "请选择单据！");
          renderJson();
          return;
        }
        BillHistory bhty = (BillHistory)BillHistory.dao.findById(configName, recodeId);
        

        Map<String, Object> rmap = YearEnd.dao.validateBillDate(configName, bhty.getTimestamp("recodeDate"));
        if (Integer.valueOf(rmap.get("statusCode").toString()).intValue() != 200)
        {
          renderJson(rmap);
          return;
        }
        Integer isRcw = bhty.getInt("isRCW");
        if ((isRcw.intValue() == AioConstants.RCW_BY) || (isRcw.intValue() == AioConstants.RCW_VS))
        {
          setAttr("statusCode", AioConstants.HTTP_RETURN301);
          setAttr("message", "这张单据已经是红冲单据，不能再反冲！");
          renderJson();
          return;
        }
        Map<String, Object> map = new HashMap();
        try
        {
          if (AioConstants.BILL_ROW_TYPE3 != billType.intValue()) {
            if (AioConstants.BILL_ROW_TYPE4 == billType.intValue()) {
              map = SellController.doRCW(configName, billId.intValue());
            } else if (AioConstants.BILL_ROW_TYPE5 == billType.intValue()) {
              map = PurchaseBillController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE6 == billType.intValue()) {
              map = PurchaseReturnBillController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE7 == billType.intValue()) {
              map = SellReturnController.doRCW(configName, billId.intValue());
            } else if (AioConstants.BILL_ROW_TYPE8 == billType.intValue()) {
              map = BreakageController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE9 == billType.intValue()) {
              map = OverflowController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE10 == billType.intValue()) {
              map = OtherInController.doRCW(configName, billId.intValue());
            } else if (AioConstants.BILL_ROW_TYPE11 == billType.intValue()) {
              map = OtherOutController.doRCW(configName, billId.intValue());
            } else if (AioConstants.BILL_ROW_TYPE12 == billType.intValue()) {
              map = PurchaseBarterBillController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE13 == billType.intValue()) {
              map = SellBarterBillController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE14 == billType.intValue()) {
              map = ParityAllotController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE15 == billType.intValue()) {
              map = DifftAllotController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE16 == billType.intValue()) {
              map = DismountBillController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE17 == billType.intValue()) {
              map = GetMoneyController.doRCW(configName, billId.intValue());
            } else if (AioConstants.BILL_ROW_TYPE18 == billType.intValue()) {
              map = OtherEarnController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE19 == billType.intValue()) {
              map = PayMoneyController.doRCW(configName, billId.intValue());
            } else if (AioConstants.BILL_ROW_TYPE20 == billType.intValue()) {
              map = AdjustCostController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE21 == billType.intValue()) {
              map = TransferController.doRCW(configName, billId.intValue());
            } else if (AioConstants.BILL_ROW_TYPE22 == billType.intValue()) {
              map = FeeBillController.doRCW(configName, billId);
            } else if (AioConstants.BILL_ROW_TYPE23 == billType.intValue()) {
              map = AddAssetsController.doRCW(configName, billId.intValue());
            } else if (AioConstants.BILL_ROW_TYPE24 == billType.intValue()) {
              map = DeprAssetsController.doRCW(configName, billId.intValue());
            } else if (AioConstants.BILL_ROW_TYPE25 == billType.intValue()) {
              map = SubAssetsController.doRCW(configName, billId.intValue());
            } else if (AioConstants.BILL_ROW_TYPE26 == billType.intValue()) {
              map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
            } else if (AioConstants.BILL_ROW_TYPE27 == billType.intValue()) {
              map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
            } else if (AioConstants.BILL_ROW_TYPE28 == billType.intValue()) {
              map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
            } else if (AioConstants.BILL_ROW_TYPE29 == billType.intValue()) {
              map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
            } else if (AioConstants.BILL_ROW_TYPE30 == billType.intValue()) {
              map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
            } else if (AioConstants.BILL_ROW_TYPE31 == billType.intValue()) {
              map = ChangePayOrGetController.doRCW(configName, billId.intValue(), billType.intValue());
            } else if (AioConstants.BILL_ROW_TYPE32 == billType.intValue()) {
              map = AccountVoucherController.doRCW(configName, billId);
            }
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
          setAttr("statusCode", AioConstants.HTTP_RETURN301);
          setAttr("message", "系统异常，请联系管理员！");
          renderJson();
          return;
        }
        String mapStatus = map.get("status").toString();
        if (mapStatus.equals(String.valueOf(AioConstants.RCW_NO)))
        {
          setAttr("statusCode", AioConstants.HTTP_RETURN301);
          setAttr("message", map.get("message"));
          renderJson();
          return;
        }
        bhty.set("isRCW", Integer.valueOf(AioConstants.RCW_BY)).update(configName);
        StockRecords.batchUpdateIsRCW(configName, billType, billId);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "红冲billTypeId类型单据成功！");
    renderJson();
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Staff staff = (Staff)getModel(Staff.class, "staff");
      Department depm = (Department)getModel(Department.class, "depm");
      BillHistory billHty = (BillHistory)getModel(BillHistory.class, "billHty");
      
      int billId = getParaToInt("billId", Integer.valueOf(0)).intValue();
      String billTableName = getPara("billTableName");
      


      Record record = Db.use(configName).findById(billTableName, Integer.valueOf(billId));
      
      billHty.set("staffId", staff.get("id"));
      billHty.set("departmentId", depm.get("id"));
      
      Boolean flag = Boolean.valueOf(false);
      flag = Boolean.valueOf(billHty.update(configName));
      
      record.set("staffId", staff.get("id"));
      record.set("departmentId", depm.get("id"));
      record.set("code", billHty.get("billCode"));
      record.set("remark", billHty.get("remark"));
      flag = Boolean.valueOf(Db.use(configName).update(billTableName, record));
      if (flag.booleanValue())
      {
        setAttr("callbackType", "closeCurrent");
        setAttr("statusCode", Integer.valueOf(200));
        setAttr("rel", "billHistoryList");
      }
      renderJson();
    }
    else
    {
      Integer id = getParaToInt(2, Integer.valueOf(0));
      
      Model model = BillHistory.dao.getModelById(configName, id.intValue());
      
      setAttr("objId", id);
      setAttr("model", model);
      render("edit.html");
    }
  }
  
  public void copyBill()
    throws SQLException
  {
    Integer billId = getParaToInt(0);
    Integer billType = getParaToInt(1);
    Integer htyId = getParaToInt(2);
    String configName = loginConfigName();
    String htyTableName = BillHistory.dao.getTableName();
    String bsdtableName = BusinessDraft.dao.getTableName();
    try
    {
      String tableName = BillType.getValue1(configName, billType.intValue(), "biillTableName");
      String detailName = tableName.replace("_bill", "_detail");
      
      String draftBill = tableName.replace("_bill", "_draft_bill");
      String draftdetail = tableName.replace("_bill", "_draft_detail");
      

      Record bill = Db.use(configName).findById(tableName, billId);
      bill.remove("id");
      bill.set("updateTime", new Date());
      bill.set("userId", Integer.valueOf(loginUserId()));
      Db.use(configName).save(draftBill, bill);
      

      List<Record> details = BaseDbModel.queryDetailsByBillId(configName, detailName, billId);
      for (Record r : details)
      {
        r.remove("id");
        r.set("billId", bill.getLong("id"));
        Db.use(configName).save(draftdetail, r);
      }
      List<Record> proDetails = StockRecords.dao.getRecordByBillIdAndType(configName, billType, billId);
      for (Record r : proDetails)
      {
        r.remove("id");
        r.remove("isRCW");
        r.remove("remainAmount");
        r.remove("remainMoney");
        r.set("billId", bill.getLong("id"));
        Db.use(configName).save("cc_stock_records_draft", r);
      }
      List<Record> accountsDetails = PayType.dao.getPayDetailByBillAndType(configName, billType.intValue(), billId.intValue());
      for (Record r : accountsDetails)
      {
        r.remove("id");
        r.set("billId", bill.getLong("id"));
        Db.use(configName).save("cw_pay_draft", r);
      }
      Record hty = Db.use(configName).findById(htyTableName, htyId);
      hty.remove("id");
      hty.set("postTime", new Date());
      hty.set("billId", bill.getLong("id"));
      Db.use(configName).save(bsdtableName, hty);
      
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("message", "单据复制草稿成功,在业务草稿中查看！");
      setAttr("rel", "billHistoryList");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "异常，网络超时！");
    }
    renderJson();
  }
  
  public void copyAllNotRowBill()
  {
    String configName = loginConfigName();
    List<Model> list = BillHistory.dao.getList(configName, null, null, null);
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Model mm = (Model)list.get(i);
        

        Integer billId = mm.getInt("billId");
        Integer billType = mm.getInt("billTypeId");
        Integer htyId = mm.getInt("id");
        

        String htyTableName = BillHistory.dao.getTableName();
        String bsdtableName = BusinessDraft.dao.getTableName();
        try
        {
          String tableName = BillType.getValue1(configName, billType.intValue(), "biillTableName");
          String detailName = tableName.replace("_bill", "_detail");
          
          String draftBill = tableName.replace("_bill", "_draft_bill");
          String draftdetail = tableName.replace("_bill", "_draft_detail");
          

          Record bill = Db.use(configName).findById(tableName, billId);
          bill.remove("id");
          bill.set("updateTime", new Date());
          bill.set("userId", Integer.valueOf(loginUserId()));
          Db.use(configName).save(draftBill, bill);
          

          List<Record> details = BaseDbModel.queryDetailsByBillId(configName, detailName, billId);
          for (Record r : details)
          {
            r.remove("id");
            r.set("billId", bill.getLong("id"));
            Db.use(configName).save(draftdetail, r);
          }
          List<Record> proDetails = StockRecords.dao.getRecordByBillIdAndType(configName, billType, billId);
          for (Record r : proDetails)
          {
            r.remove("id");
            r.remove("isRCW");
            r.remove("remainAmount");
            r.remove("remainMoney");
            r.set("billId", bill.getLong("id"));
            Db.use(configName).save("cc_stock_records_draft", r);
          }
          List<Record> accountsDetails = PayType.dao.getPayDetailByBillAndType(configName, billType.intValue(), billId.intValue());
          for (Record r : accountsDetails)
          {
            r.remove("id");
            r.set("billId", bill.getLong("id"));
            Db.use(configName).save("cw_pay_draft", r);
          }
          Record hty = Db.use(configName).findById(htyTableName, htyId);
          hty.remove("id");
          hty.set("postTime", new Date());
          hty.set("billId", bill.getLong("id"));
          Db.use(configName).save(bsdtableName, hty);
        }
        catch (Exception localException) {}
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("message", "单据复制草稿成功,在业务草稿中查看！");
    renderJson();
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "经营历程");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    colTitle.add("单据编号");
    colTitle.add("录单时间");
    colTitle.add("过账时间");
    colTitle.add("单据类型");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("经手人编号");
    colTitle.add("经手人全名");
    colTitle.add("部门编号");
    colTitle.add("部门全名");
    colTitle.add("附加说明");
    colTitle.add("摘要");
    colTitle.add("金额");
    colTitle.add("制单人");
    colTitle.add("过账人");
    colTitle.add("打印次数");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    

    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
    
    Integer billTypeId = getParaToInt("selectVal1", Integer.valueOf(0));
    Integer isRcw = getParaToInt("selectVal2", Integer.valueOf(0));
    Integer pageNum = Integer.valueOf(1);
    Integer numPerPage = Integer.valueOf(totalCount);
    String orderField = getPara("orderField", "postTime");
    String orderDirection = getPara("orderDirection", "asc");
    

    String unitId = getPara("unitId");
    String staffId = getPara("staffId");
    String productId = getPara("productId");
    String storageId = getPara("storageId");
    String departmentId = getPara("departmentId");
    String accountId = getPara("accountId");
    String isCoupon = getPara("isCoupon", "0");
    String billCode = getPara("billCode");
    String remark = getPara("remark");
    String memo = getPara("memo");
    String isMember = getPara("isMember", "0");
    String priceCase = getPara("priceCase", "0");
    String price = getPara("price");
    String discountCase = getPara("discountCase", "0");
    String discount = getPara("discount");
    String taxrateCase = getPara("taxrateCase", "0");
    String taxrate = getPara("taxrate");
    
    setAttr("unitId", unitId);
    setAttr("staffId", staffId);
    setAttr("productId", productId);
    setAttr("storageId", storageId);
    setAttr("departmentId", departmentId);
    setAttr("accountId", accountId);
    setAttr("isCoupon", isCoupon);
    setAttr("billCode", billCode);
    setAttr("remark", remark);
    setAttr("memo", memo);
    setAttr("isMember", isMember);
    setAttr("priceCase", priceCase);
    setAttr("price", price);
    setAttr("discountCase", discountCase);
    setAttr("discount", discount);
    setAttr("taxrateCase", taxrateCase);
    setAttr("taxrate", taxrate);
    

    Map<String, Object> pageMap = BillHistory.dao.getPage(configName, "billHistoryList", pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection, startTime, endTime, billTypeId.intValue(), isRcw.intValue(), 
      unitId, staffId, productId, storageId, departmentId, accountId, isMember, 
      billCode, remark, memo, isCoupon, 
      priceCase, price, discountCase, discount, taxrateCase, taxrate);
    

    List<Model> list = (List)pageMap.get("pageList");
    if ((list == null) || (list.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    int i = 1;
    for (Model record : list)
    {
      rowData.add(trimNull(i));
      
      rowData.add(trimNull(record.get("billCode")));
      rowData.add(trimNull(record.get("recodeDate")));
      rowData.add(trimNull(record.get("postTime")));
      
      BillType bt = (BillType)record.get("bt");
      rowData.add(trimNull(bt.get("name")));
      
      Unit unit = (Unit)record.get("unit");
      rowData.add(trimNull(unit.get("code")));
      rowData.add(trimNull(unit.get("fullName")));
      
      Staff staff = (Staff)record.get("staff");
      rowData.add(trimNull(staff.get("code")));
      rowData.add(trimNull(staff.get("name")));
      
      Department dept = (Department)record.get("dept");
      rowData.add(trimNull(dept.get("code")));
      rowData.add(trimNull(dept.get("fullName")));
      
      rowData.add(trimNull(record.get("memo")));
      rowData.add(trimNull(record.get("remark")));
      rowData.add(trimNull(record.get("money")));
      
      User u = (User)User.dao.findById(configName, record.get("producerId"));
      if (u != null)
      {
        int grade = u.getInt("grade").intValue();
        if (grade == 2)
        {
          rowData.add("管理员");
        }
        else
        {
          Staff s = (Staff)Staff.dao.findById(configName, u.getInt("staffId"));
          if (s != null) {
            rowData.add(trimNull(s.getStr("fullName")));
          } else {
            rowData.add("");
          }
        }
      }
      else
      {
        rowData.add("");
      }
      User u2 = (User)User.dao.findById(configName, record.get("postUsreId"));
      if (u2 != null)
      {
        int grade = u2.getInt("grade").intValue();
        if (grade == 2)
        {
          rowData.add("管理员");
        }
        else
        {
          Staff s = (Staff)Staff.dao.findById(configName, u.getInt("staffId"));
          if (s != null) {
            rowData.add(trimNull(s.getStr("fullName")));
          } else {
            rowData.add("");
          }
        }
      }
      else
      {
        rowData.add("");
      }
      rowData.add(trimNull(record.get("printAmout")));
      
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public static void saveBillHistory(String configName, Model bill, int type, BigDecimal money)
  {
    BillHistory history = new BillHistory();
    history.set("userId", bill.getInt("userId"));
    history.set("billId", bill.getInt("id"));
    history.set("billCode", bill.get("code"));
    history.set("isRCW", bill.getInt("isRCW"));
    history.set("recodeDate", bill.get("recodeDate"));
    history.set("postTime", new Date());
    history.set("remark", bill.get("remark"));
    history.set("memo", bill.get("memo"));
    history.set("money", money);
    history.set("billTypeId", Integer.valueOf(type));
    history.set("staffId", bill.get("staffId"));
    history.set("unitId", bill.get("unitId"));
    history.set("departmentId", bill.get("departmentId"));
    history.set("producerId", bill.get("userId"));
    history.set("postUsreId", bill.get("userId"));
    history.set("userId", bill.get("userId"));
    history.save(configName);
  }
}
