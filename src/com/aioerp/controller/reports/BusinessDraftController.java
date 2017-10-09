package com.aioerp.controller.reports;

import com.aioerp.bean.FlowBill;
import com.aioerp.bean.FlowDetail;
import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Unit;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.stock.StockDraftRecords;
import com.aioerp.model.sys.BillType;
import com.aioerp.model.sys.User;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessDraftController
  extends BaseController
{
  private static final String LIST_ID = "businessDraftList";
  
  public void index()
    throws Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "postTime");
    String orderDirection = getPara("orderDirection", "asc");
    
    List<Model> billTypes = BillType.dao.getList(configName, AioConstants.BILL_SORT0, AioConstants.BILL_TYPE_1);
    setAttr("billTypes", billTypes);
    

    String startTime = getPara("startDate", getPara("startTime"));
    String endTime = getPara("endDate", getPara("endTime"));
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
    

    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
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
    


    Map<String, Object> map = BusinessDraft.dao.getPage(configName, "businessDraftList", pageNum, numPerPage, orderField, orderDirection, billTypeId.intValue(), 
      startTime, endTime, unitId, staffId, productId, storageId, departmentId, accountId, isMember, 
      billCode, remark, memo, isCoupon, 
      priceCase, price, discountCase, discount, taxrateCase, taxrate);
    List<Model> list = (List)map.get("pageList");
    BigDecimal moneys = new BigDecimal(0);
    for (Model model : list) {
      moneys = BigDecimalUtils.add(moneys, model.getBigDecimal("money"));
    }
    columnConfig("djzx501");
    

    setAttr("isCheckbox", Boolean.valueOf(true));
    setAttr("moneys", moneys);
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("page.html");
  }
  
  public void list()
    throws Exception
  {
    String configName = loginConfigName();
    Integer billTypeId = getParaToInt("selectVal1", Integer.valueOf(0));
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "postTime");
    String orderDirection = getPara("orderDirection", "asc");
    


    List<Model> billTypes = BillType.dao.getList(configName, AioConstants.BILL_SORT0, AioConstants.BILL_TYPE_1);
    setAttr("billTypes", billTypes);
    setAttr("billTypeId", billTypeId);
    

    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
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
    
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
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
    

    Map<String, Object> map = BusinessDraft.dao.getPage(configName, "businessDraftList", pageNum, numPerPage, orderField, orderDirection, billTypeId.intValue(), 
      startTime, endTime, unitId, staffId, productId, storageId, departmentId, accountId, isMember, 
      billCode, remark, memo, isCoupon, 
      priceCase, price, discountCase, discount, taxrateCase, taxrate);
    List<Model> list = (List)map.get("pageList");
    BigDecimal moneys = new BigDecimal(0);
    for (Model model : list) {
      moneys = BigDecimalUtils.add(moneys, model.getBigDecimal("money"));
    }
    columnConfig("djzx501");
    
    setAttr("isCheckbox", Boolean.valueOf(true));
    setAttr("moneys", moneys);
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    render("list.html");
  }
  
  @Before({Tx.class})
  public void delDraft()
    throws SQLException
  {
    String configName = loginConfigName();
    String key = "1109-s";
    boolean hashasPermitted = hasPermitted(key);
    Integer userId = Integer.valueOf(loginUserId());
    
    String checkObjIds = getPara("checkObjIds", "");
    String[] objIds = checkObjIds.split(",");
    for (int i = 0; i < objIds.length; i++)
    {
      String[] currs = objIds[i].split("-");
      Integer billId = Integer.valueOf(Integer.parseInt(currs[0]));
      Integer billType = Integer.valueOf(Integer.parseInt(currs[1]));
      Integer draftId = Integer.valueOf(Integer.parseInt(currs[2]));
      if (!hashasPermitted)
      {
        Model draft = BusinessDraft.dao.getRecrodById(configName, draftId);
        Integer draftUserId = draft.getInt("userId");
        if (userId != draftUserId)
        {
          setAttr("message", "该用户无删除他人草稿的权限!");
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
      }
      BusinessDraft.dao.deleteById(configName, draftId);
      
      String tableName = BillType.getValue1(configName, billType.intValue(), "biillTableName");
      
      String billTaName = tableName.replace("_bill", "_draft_bill");
      
      String detailTabName = tableName.replace("_bill", "_draft_detail");
      

      Db.use(configName).deleteById(billTaName, billId);
      

      BaseDbModel.delDetailByBillId(configName, detailTabName, billId);
      if (AioConstants.BILL_ROW_TYPE17 == billType.intValue()) {
        Db.use(configName).update("delete from cw_pay_by_draft_order where cwBillTypeId=" + billType + " and cwBillId=" + billId);
      } else if (AioConstants.BILL_ROW_TYPE19 == billType.intValue()) {
        Db.use(configName).update("delete from cw_pay_by_draft_order where cwBillTypeId=" + billType + " and cwBillId=" + billId);
      }
      StockDraftRecords.delRecords(configName, billType.intValue(), billId.intValue());
      
      PayDraft.dao.delete(configName, billId, billType);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("rel", "businessDraftList");
    renderJson();
  }
  
  @Before({Tx.class})
  public void copyDraft()
    throws SQLException
  {
    Integer billId = getParaToInt("billId");
    Integer billType = getParaToInt("billType");
    Integer drafId = getParaToInt("drafId");
    Integer typeId = getParaToInt("typeId");
    String configName = loginConfigName();
    

    String tableName = BillType.getValue1(configName, billType.intValue(), "biillTableName");
    String newTableName = BillType.getValue1(configName, typeId.intValue(), "biillTableName");
    
    String billTaName = tableName.replace("_bill", "_draft_bill");
    String newBillTaName = newTableName.replace("_bill", "_draft_bill");
    
    String detailTabName = tableName.replace("_bill", "_draft_detail");
    String newDetailTabName = newTableName.replace("_bill", "_draft_detail");
    

    FlowBill fBill = dearFlowBill(billTaName, billId.intValue(), billType.intValue());
    Record bill = loadFlowBill(fBill, typeId.intValue());
    Db.use(configName).save(newBillTaName, bill);
    Long newBillId = bill.getLong("id");
    


    List<Record> details = BaseDbModel.queryDetailsByBillId(configName, detailTabName, billId);
    for (Record r : details)
    {
      FlowDetail fDetail = dearFlowDetail(r, billType.intValue());
      Record detail = loadFlowDetail(fDetail, typeId.intValue());
      detail.set("billId", newBillId);
      Db.use(configName).save(newDetailTabName, detail);
    }
    BusinessDraft draft = (BusinessDraft)BusinessDraft.dao.findById(configName, drafId);
    draft.remove("id");
    draft.set("billId", newBillId);
    draft.set("billCode", bill.getStr("code"));
    draft.set("postTime", new Date());
    draft.set("remark", bill.getStr("remark"));
    draft.set("memo", bill.getStr("memo"));
    draft.set("producerId", bill.getInt("userId"));
    draft.set("billTypeId", typeId);
    draft.set("unitId", bill.getInt("unitId"));
    draft.set("staffId", bill.getInt("staffId"));
    draft.set("departmentId", bill.getInt("departmentId"));
    draft.save(configName);
    
    Integer isInSck = Integer.valueOf(BillType.getValue1(configName, typeId.intValue(), "isInSck"));
    

    List<Record> proDetails = StockDraftRecords.dao.getRecordByBillIdAndType(configName, billType.intValue(), billId.intValue());
    BigDecimal newAmount;
    for (Record r : proDetails)
    {
      r.remove("id");
      r.set("createTime", new Date());
      r.set("billTypeId", typeId);
      r.set("billId", newBillId);
      r.set("billCode", bill.getStr("code"));
      r.set("recodeTime", bill.getDate("recodeDate"));
      r.set("billAbstract", bill.getStr("remark"));
      r.set("unitId", bill.getInt("unitId"));
      r.set("userId", bill.getInt("userId"));
      
      newAmount = BigDecimal.ZERO;
      if (isInSck.intValue() == 1)
      {
        newAmount = r.getBigDecimal("inAmount");
        if (BigDecimalUtils.compare(newAmount, BigDecimal.ZERO) == 0) {
          newAmount = r.getBigDecimal("outAmount");
        }
        r.set("inAmount", newAmount);
        r.set("outAmount", null);
      }
      else if (isInSck.intValue() == -1)
      {
        newAmount = r.getBigDecimal("outAmount");
        if (BigDecimalUtils.compare(newAmount, BigDecimal.ZERO) == 0) {
          newAmount = r.getBigDecimal("inAmount");
        }
        r.set("inAmount", null);
        r.set("outAmount", newAmount);
      }
      else
      {
        isInSck.intValue();
      }
      Db.use(configName).save("cc_stock_records_draft", r);
    }
    List<Model> accountsDetails = PayDraft.dao.getListByBillId(configName, billId, billType, null);
    for (Model r : accountsDetails)
    {
      r.remove("id");
      r.set("billTypeId", typeId);
      r.set("billId", newBillId);
      r.set("userId", Integer.valueOf(loginUserId()));
      r.save(configName);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("rel", "businessDraftList");
    renderJson();
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "业务草稿");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    colTitle.add("单据编号");
    colTitle.add("录单时间");
    colTitle.add("存盘时间");
    colTitle.add("单据类型");
    colTitle.add("单位编号");
    colTitle.add("单位全名");
    colTitle.add("职员编号");
    colTitle.add("职员全名");
    colTitle.add("部门编号");
    colTitle.add("部门全名");
    colTitle.add("附加说明");
    colTitle.add("摘要");
    colTitle.add("金额");
    colTitle.add("制单人");
    colTitle.add("打印次数");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    
    Integer billTypeId = getParaToInt("selectVal1", Integer.valueOf(0));
    Integer pageNum = Integer.valueOf(1);
    Integer numPerPage = Integer.valueOf(totalCount);
    String orderField = getPara("orderField", "postTime");
    String orderDirection = getPara("orderDirection", "asc");
    


    String startTime = getPara("startTime");
    String endTime = getPara("endTime");
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
    
    setAttr("startTime", startTime);
    setAttr("endTime", endTime);
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
    

    Map<String, Object> pageMap = BusinessDraft.dao.getPage(configName, "businessDraftList", pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection, billTypeId.intValue(), 
      startTime, endTime, unitId, staffId, productId, storageId, departmentId, accountId, isMember, 
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
      rowData.add(trimNull(record.get("printAmout")));
      
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
  
  public void toBillType()
  {
    Integer billId = getParaToInt(0);
    Integer billType = getParaToInt(1);
    Integer drafId = getParaToInt(2);
    String configName = loginConfigName();
    

    List<Model> billTypes = BillType.dao.getList(configName, AioConstants.BILL_SORT2, 1);
    
    setAttr("billId", billId);
    setAttr("billType", billType);
    setAttr("drafId", drafId);
    setAttr("billTypes", billTypes);
    render("billTypes.html");
  }
  
  public static void saveBillDraft(String configName, int type, Model bill, BigDecimal moneys)
  {
    BusinessDraft draft = new BusinessDraft();
    draft.set("billId", bill.getInt("id"));
    draft.set("billCode", bill.get("code"));
    draft.set("recodeDate", bill.get("recodeDate"));
    draft.set("postTime", new Date());
    draft.set("remark", bill.get("remark"));
    draft.set("memo", bill.get("memo"));
    draft.set("money", moneys);
    draft.set("billTypeId", Integer.valueOf(type));
    draft.set("staffId", bill.get("staffId"));
    draft.set("departmentId", bill.get("departmentId"));
    draft.set("unitId", bill.get("unitId"));
    draft.set("producerId", bill.getInt("userId"));
    draft.set("userId", bill.get("userId"));
    draft.save(configName);
  }
  
  public static void updateBillDraft(String configName, int id, Model bill, int type, BigDecimal moneys)
  {
    BusinessDraft draft = (BusinessDraft)BusinessDraft.dao.findById(configName, Integer.valueOf(id));
    draft.set("billId", bill.getInt("id"));
    draft.set("billCode", bill.get("code"));
    draft.set("recodeDate", bill.get("recodeDate"));
    draft.set("postTime", new Date());
    draft.set("remark", bill.get("remark"));
    draft.set("memo", bill.get("memo"));
    draft.set("money", moneys);
    draft.set("billTypeId", Integer.valueOf(type));
    draft.set("staffId", bill.get("staffId"));
    draft.set("departmentId", bill.get("departmentId"));
    draft.set("unitId", bill.get("unitId"));
    draft.set("producerId", bill.getInt("userId"));
    draft.set("userId", bill.get("userId"));
    draft.update(configName);
  }
  
  public static void updateBillDraft(String configName, Model bill, int type, BigDecimal moneys)
  {
    BusinessDraft draft = (BusinessDraft)BusinessDraft.dao.getModeByBill(configName, bill.getInt("id"), Integer.valueOf(type));
    draft.set("billCode", bill.get("code"));
    draft.set("recodeDate", bill.get("recodeDate"));
    draft.set("postTime", new Date());
    draft.set("remark", bill.get("remark"));
    draft.set("memo", bill.get("memo"));
    draft.set("money", moneys);
    draft.set("billTypeId", Integer.valueOf(type));
    draft.set("staffId", bill.get("staffId"));
    draft.set("departmentId", bill.get("departmentId"));
    draft.set("unitId", bill.get("unitId"));
    draft.set("producerId", bill.getInt("userId"));
    draft.set("userId", bill.get("userId"));
    draft.update(configName);
  }
}
