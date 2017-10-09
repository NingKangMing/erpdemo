package com.aioerp.controller;

import com.aioerp.bean.FlowBill;
import com.aioerp.bean.FlowDetail;
import com.aioerp.common.AioConstants;
import com.aioerp.controller.sys.BillCodeConfigController;
import com.aioerp.db.reports.BusinessDraft;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Area;
import com.aioerp.model.base.Department;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.Staff;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.bought.PurchaseDraftBill;
import com.aioerp.model.bought.PurchaseDraftDetail;
import com.aioerp.model.bought.PurchaseReturnDraftBill;
import com.aioerp.model.bought.PurchaseReturnDraftDetail;
import com.aioerp.model.finance.AdjustCostDraftBill;
import com.aioerp.model.finance.AdjustCostDraftDetail;
import com.aioerp.model.finance.PayDraft;
import com.aioerp.model.fz.ReportRow;
import com.aioerp.model.sell.sell.SellDraftBill;
import com.aioerp.model.sell.sell.SellDraftDetail;
import com.aioerp.model.sell.sellreturn.SellReturnDraftBill;
import com.aioerp.model.sell.sellreturn.SellReturnDraftDetail;
import com.aioerp.model.stock.BreakageDraftBill;
import com.aioerp.model.stock.BreakageDraftDetail;
import com.aioerp.model.stock.DifftAllotDraftBill;
import com.aioerp.model.stock.DifftAllotDraftDetail;
import com.aioerp.model.stock.OverflowDraftBill;
import com.aioerp.model.stock.OverflowDraftDetail;
import com.aioerp.model.stock.ParityAllotDraftBill;
import com.aioerp.model.stock.ParityAllotDraftDetail;
import com.aioerp.model.stock.other.OtherInDraftBill;
import com.aioerp.model.stock.other.OtherInDraftDetail;
import com.aioerp.model.stock.other.OtherOutDraftBill;
import com.aioerp.model.stock.other.OtherOutDraftDetail;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillCodeConfig;
import com.aioerp.model.sys.BillCodeFlow;
import com.aioerp.model.sys.BillType;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.model.sys.SysUserSearchDate;
import com.aioerp.model.sys.User;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DateUtils;
import com.jfinal.core.Controller;
import com.jfinal.log.Log4jLogger;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

public class BaseController
  extends Controller
{
  protected static String ORDER_FIELD = "rank";
  protected static String ORDER_DIRECTION = "asc";
  protected Map<String, Object> result = new HashMap();
  public static String PRODUCT_PRIVS = "productPrivs";
  public static String UNIT_PRIVS = "unitPrivs";
  public static String STORAGE_PRIVS = "storagePrivs";
  public static String ACCOUNT_PRIVS = "accountPrivs";
  public static String AREA_PRIVS = "areaPrivs";
  public static String STAFF_PRIVS = "staffPrivs";
  public static String DEPARTMENT_PRIVS = "departmentPrivs";
  public static String ALL_PRIVS = "*";
  
  public static void commonMainRollBack()
    throws SQLException
  {
    Config cc = DbKit.getConfig(AioConstants.CONFIG_NAME);
    Connection con = cc.getConnection();
    con.rollback();
    cc.close(con);
  }
  
  public void commonRollBack()
    throws SQLException
  {
    Record r = (Record)getSessionAttr("user");
    String configName = r.getStr("loginConfigId");
    Config cc = DbKit.getConfig(configName);
    Connection con = cc.getConnection();
    con.setAutoCommit(false);
    con.rollback();
  }
  
  public static void commonRollBack(String configName)
    throws SQLException
  {
    Config cc = DbKit.getConfig(configName);
    Connection con = cc.getConnection();
    con.rollback();
    cc.close(con);
  }
  
  public String loginConfigName()
  {
    Record r = (Record)getSessionAttr("user");
    String loginConfigName = r.getStr("loginConfigId");
    if (StringUtils.isBlank(loginConfigName)) {
      loginConfigName = AioConstants.CONFIG_NAME;
    }
    return loginConfigName;
  }
  
  public String loginAccountName()
  {
    Record r = (Record)getSessionAttr("user");
    String loginAccountName = r.getStr("loginConfigName");
    return loginAccountName;
  }
  
  public int loginUserId()
  {
    Record r = (Record)getSessionAttr("user");
    return r.getInt("id").intValue();
  }
  
  public static Record modelChangeDb(Model model)
  {
    String[] attr = model.getAttrNames();
    Object[] val = model.getAttrValues();
    Record record = new Record();
    for (int i = 0; i < attr.length; i++) {
      record.set(attr[i], val[i]);
    }
    return record;
  }
  
  public boolean isPost()
  {
    return "POST".equals(getRequest().getMethod().toUpperCase());
  }
  
  public boolean isAjax()
  {
    return "XMLHttpRequest".equals(getRequest().getHeader("x-requested-with"));
  }
  
  protected String getParaOne(String name)
  {
    String[] paras = getParaValues(name + "[]");
    if ((paras != null) && (StringUtils.isNotBlank(paras[0]))) {
      return paras[0];
    }
    return getPara(name);
  }
  
  public Map<String, Object> requestPageToMap(Map<String, Object> map, String orderField)
  {
    if ((orderField == null) || (orderField.equals(""))) {
      orderField = ORDER_FIELD;
    }
    if (map == null) {
      map = new HashMap();
    }
    map.put("pageNum", getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)));
    map.put("numPerPage", getParaToInt("numPerPage", Integer.valueOf(20)));
    map.put("orderField", getPara("orderField", orderField));
    map.put("orderDirection", getPara("orderDirection", ORDER_DIRECTION));
    return map;
  }
  
  public void mapToResponse(Map<String, Object> map)
  {
    if ((map != null) || (map.size() > 0)) {
      for (String dataKey : map.keySet()) {
        setAttr(dataKey, map.get(dataKey));
      }
    }
  }
  
  public void returnToPage(String statusCode, String message, String navTabId, String rel, String callbackType, String forwardUrl)
  {
    if (StringUtils.isNotBlank(statusCode)) {
      setAttr("statusCode", statusCode);
    }
    if (StringUtils.isNotBlank(message)) {
      setAttr("message", message);
    }
    if (StringUtils.isNotBlank(navTabId)) {
      setAttr("navTabId", navTabId);
    }
    if (StringUtils.isNotBlank(rel)) {
      setAttr("rel", rel);
    }
    if (StringUtils.isNotBlank(callbackType)) {
      setAttr("callbackType", callbackType);
    }
    if (StringUtils.isNotBlank(forwardUrl)) {
      setAttr("forwardUrl", forwardUrl);
    }
  }
  
  public void notEditStaff()
  {
    String configName = loginConfigName();
    Record user = (Record)getSessionAttr("user");
    if (user != null)
    {
      Integer grade = user.getInt("grade");
      Integer staffId = user.getInt("staffId");
      if ((grade.intValue() == 1) && (SysConfig.getConfig(configName, 11).booleanValue()))
      {
        setAttr("notEditStaff", Boolean.valueOf(true));
        Staff staff = (Staff)Staff.dao.findById(configName, staffId);
        setAttr("staff", staff);
        Integer depmId = staff.getInt("depmId");
        if (depmId != null)
        {
          Department depm = (Department)Department.dao.findById(configName, depmId);
          setAttr("depm", depm);
        }
      }
    }
  }
  
  public static Map<String, Object> verifyUnitMaxGetMoney(String configName, String operate, Integer unitId, int type, BigDecimal payMoney, BigDecimal privilegeMoney)
  {
    if ((unitId == null) || (unitId.intValue() == 0)) {
      return null;
    }
    Map<String, Object> result = new HashMap();
    if (SysConfig.getConfig(configName, 10).booleanValue())
    {
      Unit unit = (Unit)Unit.dao.findById(configName, unitId);
      BigDecimal getMoneyLimit = unit.getBigDecimal("getMoneyLimit");
      if (getMoneyLimit != null)
      {
        BigDecimal totalGet = unit.getBigDecimal("totalGet");
        BigDecimal totalPay = unit.getBigDecimal("totalPay");
        BigDecimal arapGet = BigDecimal.ZERO;
        BigDecimal arapMoney = BigDecimalUtils.sub(privilegeMoney, payMoney);
        if ((operate.equals(AioConstants.OPERTE_RCW)) || (operate.equals(AioConstants.OPERTE_DEL))) {
          if (type == AioConstants.WAY_GET) {
            type = AioConstants.WAY_PAY;
          } else if (type == AioConstants.WAY_PAY) {
            type = AioConstants.WAY_GET;
          }
        }
        if (type == AioConstants.WAY_GET)
        {
          if (BigDecimalUtils.notNullZero(totalPay))
          {
            arapMoney = BigDecimalUtils.sub(totalPay, arapMoney);
            arapMoney = arapMoney.abs();
          }
          arapGet = BigDecimalUtils.add(totalGet, arapMoney);
        }
        else if ((type == AioConstants.WAY_PAY) && 
          (BigDecimalUtils.compare(totalGet, arapMoney) == 1))
        {
          arapGet = BigDecimalUtils.sub(totalGet, arapMoney);
        }
        if (BigDecimalUtils.compare(arapGet, getMoneyLimit) == 1)
        {
          result.put("message", "往来单位超出应收上限不能过账!");
          result.put("statusCode", AioConstants.HTTP_RETURN300);
          return result;
        }
      }
    }
    return null;
  }
  
  protected Boolean editDraftVerify(Integer draftId)
  {
    if (draftId != null)
    {
      String key1 = "1105-s";
      boolean hashasPermitted1 = hasPermitted(key1);
      if (!hashasPermitted1)
      {
        setAttr("message", "该用户无修改草稿的权限!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return Boolean.valueOf(true);
      }
      String key2 = "1108-s";
      boolean hashasPermitted2 = hasPermitted(key2);
      if (!hashasPermitted2)
      {
        Integer userId = Integer.valueOf(loginUserId());
        Model draft = BusinessDraft.dao.getRecrodById(loginConfigName(), draftId);
        Integer draftUserId = draft.getInt("userId");
        if (userId != draftUserId)
        {
          setAttr("message", "该用户无修改他人草稿的权限!");
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return Boolean.valueOf(true);
        }
      }
    }
    return Boolean.valueOf(false);
  }
  
  protected void setDraftAutoPost()
  {
    Boolean flag = (Boolean)getSessionAttr("draftAutoPost");
    if ((flag != null) && (flag.booleanValue())) {
      setAttr("autoPost", "true");
    }
  }
  
  protected void setDraftStrs()
  {
    String draftStrs = (String)getSessionAttr("draftStrs");
    if ((!"".equals(draftStrs)) && (draftStrs != null)) {
      setAttr("draftStrs", draftStrs);
    }
  }
  
  protected void printBeforSave1()
  {
    String postForm = getPara("postForm", "");
    String printType = getPara("printType", "");
    String printUrl = getPara("printUrl", "");
    if ((!"".equals(printType)) && (!"".equals(printUrl)))
    {
      setAttr("postForm", postForm);
      setAttr("printType", printType);
      setAttr("printUrl", printUrl);
    }
  }
  
  protected Boolean printBeforSave2(Map<String, Object> data, Integer billId)
  {
    if (billId == null) {
      billId = Integer.valueOf(0);
    }
    Boolean ifSave = getParaToBoolean("ifSave", Boolean.valueOf(true));
    String printType = getPara("printType", "");
    data.put("printType", printType);
    if ((ifSave.booleanValue()) && (billId.intValue() == 0) && (SysConfig.getConfig(loginConfigName(), 12).booleanValue()))
    {
      data.put("postForm", getPara("postForm", ""));
      data.put("printUrl", getPara("printUrl", ""));
      return Boolean.valueOf(true);
    }
    if (!ifSave.booleanValue())
    {
      data.put("callbackType", getPara("callbackType", ""));
      data.put("navTabId", getPara("navTabId", ""));
      data.put("isClose", getPara("isClose", ""));
    }
    return Boolean.valueOf(false);
  }
  
  public boolean codeIsExist(String configName, String tableName, String attr, String val, int id)
  {
    return Db.use(configName).queryLong("select count(*) from " + tableName + " where code =? and id != ?", new Object[] { val, Integer.valueOf(id) }).longValue() > 0L;
  }
  
  public static void billCountChange(String configName, String type)
  {
    if (type == null) {
      type = "add";
    }
    int billCount = ((Integer)AioConstants.accountBillCount.get(configName)).intValue();
    if (type.equals("add")) {
      billCount++;
    } else {
      billCount--;
    }
    AioConstants.accountBillCount.put(configName, Integer.valueOf(billCount));
  }
  
  public void addPayRecords(String configName, Integer type, int whichCallBack, String payTypeIds, String payTypeMoneys, int billId, Integer unitId, Date time)
  {
    if (StringUtils.isNotBlank(payTypeIds))
    {
      String[] accounts = payTypeIds.split(",");
      String[] payMoneys = payTypeMoneys.split(",");
      for (int i = 0; i < accounts.length; i++)
      {
        BigDecimal payMoneyi = new BigDecimal(payMoneys[i]);
        PayDraft.dao.addPayRecords(configName, type, whichCallBack, billId, unitId, Integer.valueOf(accounts[i]), payMoneyi, time);
      }
    }
  }
  
  public void setPayTypeAttr(String configName, String type, int billTypeId, int billId)
  {
    Map<String, String> map = ComFunController.billPayTypeAttr(configName, type, billTypeId, billId);
    setAttr("payTypeIdStrs", map.get("payTypeIdStrs"));
    setAttr("payTypeMoneyStrs", map.get("payTypeMoneyStrs"));
    setAttr("payTypeAccounts", map.get("payTypeAccounts"));
    setAttr("payOrgetMoneys", map.get("payOrgetMoneys"));
  }
  
  public int delTableDetailIdsByBillId(String tableName, Integer billId, List<Integer> ids)
  {
    StringBuffer sql = new StringBuffer("delete from " + tableName + " where 1=1");
    List<Object> paras = new ArrayList();
    if (billId != null)
    {
      sql.append(" and billId = ?");
      paras.add(billId);
    }
    else
    {
      sql.append(" and billId = ?");
      paras.add(Integer.valueOf(0));
    }
    if (ids != null) {
      for (Integer id : ids) {
        if (id != null)
        {
          sql.append(" and id != ?");
          paras.add(id);
        }
      }
    }
    return Db.use(loginConfigName()).update(sql.toString(), paras.toArray());
  }
  
  public static String basePrivs(String basePrivs)
  {
    Subject subject = SecurityUtils.getSubject();
    
    Record user = (Record)subject.getSession().getAttribute("user");
    String privs = user.getStr(basePrivs);
    if (StringUtils.isNotBlank(privs))
    {
      if (privs.startsWith(",")) {
        privs = privs.replaceFirst(",", "");
      }
      if (privs.endsWith(",")) {
        privs = privs.substring(0, privs.length() - 1);
      }
    }
    return privs;
  }
  
  public void updateBasePrivs(Integer baseId, Integer supId, String basePrivs)
  {
    String configName = loginConfigName();
    List<Model> userList = User.dao.getPrivsList(configName, basePrivs);
    for (Model user : userList)
    {
      String privs = user.getStr(basePrivs);
      String idsStr = "";
      if (PRODUCT_PRIVS.equals(basePrivs)) {
        idsStr = Product.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, supId.intValue());
      } else if (UNIT_PRIVS.equals(basePrivs)) {
        idsStr = Unit.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, supId.intValue());
      } else if (STORAGE_PRIVS.equals(basePrivs)) {
        idsStr = Storage.dao.getAllChildIdsBySupId(configName, supId, ALL_PRIVS);
      } else if (ACCOUNT_PRIVS.equals(basePrivs)) {
        idsStr = Accounts.dao.getAllChildIdsBySupId(configName, ALL_PRIVS, supId);
      } else if (AREA_PRIVS.equals(basePrivs)) {
        idsStr = Area.dao.getAllChildIdsBySupId(configName, supId);
      } else if (STAFF_PRIVS.equals(basePrivs)) {
        idsStr = Staff.dao.getAllChildIdsBySupId(configName, supId);
      } else if (DEPARTMENT_PRIVS.equals(basePrivs)) {
        idsStr = Department.dao.getAllChildIdsBySupId(configName, supId);
      }
      String[] ids = idsStr.split(",");
      boolean include = true;
      for (String id : ids) {
        if ((privs.indexOf("," + id + ",") == -1) && (!id.equals(baseId.toString())))
        {
          include = false;
          break;
        }
      }
      if (include)
      {
        privs = privs + baseId + ",";
        user.set(basePrivs, privs);
        user.update(configName);
        Record pUser = (Record)getSessionAttr("user");
        if (pUser.getInt("id") == user.getInt("id"))
        {
          pUser.set(basePrivs, privs);
          setSessionAttr("user", pUser);
        }
      }
    }
  }
  
  public void toReportBaseFilter()
  {
    setAttr("submitFormId", getPara(0, ""));
    String baseType = getPara(1, "");
    if (baseType.equals("product")) {
      render("/WEB-INF/template/base/baseSearchComDialog/product/filter.html");
    } else if (baseType.equals("unit")) {
      render("/WEB-INF/template/base/baseSearchComDialog/unit/filter.html");
    } else if (baseType.equals("department")) {
      render("/WEB-INF/template/base/baseSearchComDialog/department/filter.html");
    } else if (baseType.equals("staff")) {
      render("/WEB-INF/template/base/baseSearchComDialog/staff/filter.html");
    } else if (baseType.equals("area")) {
      render("/WEB-INF/template/base/baseSearchComDialog/area/filter.html");
    } else if (baseType.equals("storage")) {
      render("/WEB-INF/template/base/baseSearchComDialog/storage/filter.html");
    }
  }
  
  public boolean isInitReportOtherCondition(Map<String, Object> map)
  {
    boolean flag = false;
    String searchBaseAttr = getPara("searchBaseAttr", "");
    String searchBaseVal = getPara("searchBaseVal", "");
    map.put("searchBaseAttr", searchBaseAttr);
    map.put("searchBaseVal", searchBaseVal);
    if (((searchBaseAttr == null) || (searchBaseAttr.equals("")) || (searchBaseVal == null) || (searchBaseVal.equals(""))) && 
      (searchBaseAttr != null) && (!searchBaseAttr.equals("")))
    {
      map.put("searchBaseAttr", "");
      map.put("supId", Integer.valueOf(0));
      map.put("node", Integer.valueOf(AioConstants.NODE_2));
      flag = true;
    }
    return flag;
  }
  
  public static void billRcwAddRemark(Model bill, String attr)
  {
    if ((attr == null) || (attr.equals(""))) {
      attr = "remark";
    }
    String remark = bill.getStr(attr);
    StringBuffer str = null;
    if (remark == null) {
      str = new StringBuffer().append("(红字反冲)");
    } else {
      str = new StringBuffer().append(bill.getStr("remark")).append("(红字反冲)");
    }
    bill.set(attr, str.toString());
  }
  
  public void loadOnlyOneBaseInfo(String configName)
  {
    Map<String, Integer> map = new HashMap();
    map.put("b_storage", Integer.valueOf(AioConstants.STATUS_ENABLE));
    if (map != null) {
      for (String tableName : map.keySet())
      {
        Integer status = (Integer)map.get(tableName);
        if (tableName.equals("b_storage"))
        {
          Record record = ComFunController.isOnlyOne(configName, "b_storage", status);
          setAttr("storage", record);
        }
      }
    }
  }
  
  public void columnConfig(String columnType)
  {
    setAttr("columnType", columnType);
    List<Model> list = ReportRow.dao.getListByType(loginConfigName(), columnType, AioConstants.STATUS_ENABLE);
    setAttr("rowList", list);
  }
  
  public List<Model> addTrSize(List<Model> detailList, int addTrSize)
  {
    if (detailList == null) {
      detailList = new ArrayList();
    }
    if (detailList.size() < addTrSize) {
      addTrSize -= detailList.size();
    }
    for (int i = 0; i < addTrSize; i++)
    {
      Model m = new Model() {};
      m.put("id", null);
      detailList.add(m);
    }
    return detailList;
  }
  
  public boolean hasPermitted(String key)
  {
    Subject currentUser = SecurityUtils.getSubject();
    return currentUser.isPermitted(key);
  }
  
  public void setStartDateAndEndDate()
    throws ParseException
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Record record = SysUserSearchDate.dao.getUserSearchDate(loginConfigName(), loginUserId());
    setAttr("startDate", sdf.format(record.getDate("startDate")));
    setAttr("endDate", sdf.format(record.getDate("endDate")));
    
    setAttr("startTime", sdf.format(record.getDate("startDate")));
    setAttr("endTime", sdf.format(record.getDate("endDate")));
  }
  
  public String orderFuJianIds(int billTypeId, int billId, int draft)
  {
    String ids = "";
    List<Record> list = Db.use(loginConfigName()).find("select id from aioerp_file where tableId=" + billTypeId + " and recordId=" + billId + " and isDraft=" + draft);
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++) {
        ids = ids + "," + ((Record)list.get(i)).getInt("id");
      }
    }
    if (!ids.equals("")) {
      ids = ids.substring(1, ids.length());
    }
    return ids;
  }
  
  public Map<String, String> billCodeAuto(int billTypeId)
  {
    Map<String, String> map = new HashMap();
    

    Logger log = Log4jLogger.getLogger(BillCodeConfigController.class);
    
    String configName = loginConfigName();
    
    String isAuto = AioerpSys.dao.getValue1(configName, "codeSelfCreate");
    if ("1".equals(isAuto)) {
      return map;
    }
    setAttr("codeAllowEdit", AioerpSys.dao.getValue1(configName, "codeAllowEdit"));
    

    String codeCustomChar = AioerpSys.dao.getValue1(configName, "codeCustomChar");
    
    String codeDateSep = AioerpSys.dao.getValue1(configName, "codeDateSep");
    
    String codeIncreaseRule = AioerpSys.dao.getValue1(configName, "codeIncreaseRule");
    if (StringUtils.isBlank(codeIncreaseRule)) {
      codeIncreaseRule = "1";
    }
    String codeFlowLen = AioerpSys.dao.getValue1(configName, "codeFlowLen");
    
    String codeCreateRule = AioerpSys.dao.getValue1(configName, "codeCreateRule");
    
    int len = 0;
    if (StringUtils.isNotBlank(codeFlowLen)) {
      len = Integer.parseInt(codeFlowLen);
    }
    StringBuilder billCode = new StringBuilder("");
    String billTabName = "";
    

    BillType type = (BillType)BillType.dao.findById(configName, Integer.valueOf(billTypeId));
    if (type != null)
    {
      billCode.append(type.getStr("prefix"));
      billTabName = type.getStr("biillTableName");
    }
    else
    {
      return map;
    }
    if (StringUtils.isNotBlank(codeCustomChar)) {
      billCode.append("-" + codeCustomChar);
    }
    BillCodeConfig config = BillCodeConfig.dao.getObj(configName, Integer.valueOf(billTypeId));
    try
    {
      if (config == null)
      {
        billCode.append("-" + BillCodeConfigController.getFormatDefault(codeDateSep));
      }
      else
      {
        String formatCurrent = BillCodeConfigController.getFormatCurrent(configName, codeDateSep, config);
        if (StringUtils.isNotBlank(formatCurrent)) {
          billCode.append("-" + formatCurrent);
        }
      }
    }
    catch (ParseException e)
    {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    Date currDate = new Date();
    int dateY = DateUtils.getYear(currDate);
    int dateM = DateUtils.getMonth(currDate);
    int dateD = DateUtils.getDay(currDate);
    

    String newCodeFlag = "";
    Integer codeIncrease = Integer.valueOf(0);
    if ("1".equals(codeCreateRule))
    {
      BillCodeFlow codeFlow = BillCodeFlow.dao.getObj(configName, billTypeId, "z");
      if (codeFlow == null)
      {
        newCodeFlag = BillCodeConfigController.getCodeFlow("1", len);
        codeFlow = new BillCodeFlow();
        codeFlow.set("codeIncrease", Integer.valueOf(2));
        codeFlow.set("type", "z");
        codeFlow.set("updateDate", currDate);
        codeFlow.set("billId", Integer.valueOf(billTypeId));
        codeFlow.save(configName);
      }
      else
      {
        Date flowDate = codeFlow.getDate("updateDate");
        int flowDateY = DateUtils.getYear(flowDate);
        int flowDateM = DateUtils.getMonth(flowDate);
        int flowDateD = DateUtils.getDay(flowDate);
        
        codeIncrease = codeFlow.getInt("codeIncrease");
        if (codeIncrease == null) {
          codeIncrease = Integer.valueOf(0);
        }
        boolean isNewFlow = false;
        if ("0".equals(codeIncreaseRule))
        {
          if ((dateY == flowDateY) && (dateM == flowDateM) && (dateD > flowDateD)) {
            isNewFlow = true;
          }
        }
        else if ("1".equals(codeIncreaseRule))
        {
          if ((dateY == flowDateY) && (dateM > flowDateM)) {
            isNewFlow = true;
          }
        }
        else if (("2".equals(codeIncreaseRule)) && 
          (dateY > flowDateY)) {
          isNewFlow = true;
        }
        if (isNewFlow)
        {
          newCodeFlag = BillCodeConfigController.getCodeFlow("1", len);
          codeIncrease = Integer.valueOf(2);
        }
        else
        {
          newCodeFlag = BillCodeConfigController.getCodeFlow(codeIncrease.toString(), len);
          codeIncrease = Integer.valueOf(codeIncrease.intValue() + 1);
        }
        codeFlow.set("codeIncrease", codeIncrease);
        codeFlow.set("updateDate", currDate);
        codeFlow.update(configName);
      }
    }
    else
    {
      Integer maxNum = Integer.valueOf(BillCodeFlow.dao.getBillMaxCodeNum(configName, currDate, billTabName, codeIncreaseRule));
      newCodeFlag = BillCodeConfigController.getCodeFlow(maxNum.toString(), len);
      codeIncrease = maxNum;
      

      Map<String, String> codeMap = new HashMap();
      codeMap.put("newCodeFlag", newCodeFlag);
      codeMap.put("codeIncrease", String.valueOf(codeIncrease));
      reloadSameCode(configName, billTabName, billCode.toString(), len, codeMap);
      codeIncrease = Integer.valueOf((String)codeMap.get("codeIncrease"));
      newCodeFlag = (String)codeMap.get("newCodeFlag");
    }
    setAttr("codeIncrease", codeIncrease);
    billCode.append("-" + newCodeFlag);
    setAttr("code", billCode.toString());
    
    map.put("code", billCode.toString());
    map.put("codeIncrease", String.valueOf(codeIncrease));
    return map;
  }
  
  public static void reloadSameCode(String configName, String billTabName, String billCode, int len, Map<String, String> codeMap)
  {
    Integer codeIncrease = Integer.valueOf((String)codeMap.get("codeIncrease"));
    String newCodeFlag = (String)codeMap.get("newCodeFlag");
    Record r = Db.use(configName).findFirst("select * from " + billTabName + " where code='" + billCode.toString() + "-" + newCodeFlag + "'");
    if (r != null)
    {
      codeIncrease = Integer.valueOf(codeIncrease.intValue() + 1);
      newCodeFlag = BillCodeConfigController.getCodeFlow(codeIncrease.toString(), len);
      codeMap.put("newCodeFlag", newCodeFlag);
      codeMap.put("codeIncrease", String.valueOf(codeIncrease));
      reloadSameCode(configName, billTabName, billCode, len, codeMap);
    }
  }
  
  public void billCodeIncrease(Model bill, String model)
  {
    String normalCode = getPara("billCode");
    if (!normalCode.equals(bill.getStr("code"))) {
      bill.set("codeIncrease", Integer.valueOf(-1));
    }
    if ((("draftPost".equals(model)) || ("draftAdd".equals(model))) && 
      (bill.getInt("codeIncrease").intValue() != -1))
    {
      String codeCreateRule = AioerpSys.dao.getValue1(loginConfigName(), "codeCreateRule");
      if ("2".equals(codeCreateRule))
      {
        int codeIncrease = bill.getInt("codeIncrease").intValue();
        if ("draftPost".equals(model)) {
          bill.set("codeIncrease", Integer.valueOf(codeIncrease + 1));
        } else if ("draftAdd".equals(model)) {
          bill.set("codeIncrease", Integer.valueOf(codeIncrease - 1));
        }
      }
    }
  }
  
  public void printCommonData(List<String> headData) {}
  
  public void printUnitCommonData(List<String> headData, Model unit)
  {
    if (unit != null)
    {
      headData.add("单位编号 :" + trimNull(unit.getStr("code")));
      headData.add("单位简名 :" + trimNull(unit.getStr("smallName")));
      headData.add("单位税号 :" + trimNull(unit.getStr("tariff")));
      headData.add("单位地址 :" + trimNull(unit.getStr("address")));
      headData.add("单位电话 :" + trimNull(unit.getStr("phone")));
      headData.add("手机一 :" + trimNull(unit.getStr("mobile1")));
      headData.add("开户银行 :" + trimNull(unit.getStr("bank")));
      headData.add("银行账号 :" + trimNull(unit.getStr("bankAccount")));
      headData.add("联系人一 :" + trimNull(unit.getStr("contact1")));
      headData.add("邮编 :" + trimNull(unit.getStr("zipCode")));
      headData.add("传真号 :" + trimNull(unit.getStr("fax")));
    }
  }
  
  protected void setHeadUnitData(List<String> headData, Integer unitId)
  {
    String configName = loginConfigName();
    Unit unit = (Unit)Unit.dao.findById(configName, unitId);
    if (unit != null)
    {
      headData.add("单位编号 :" + trimNull(unit.get("code")));
      headData.add("单位全名 :" + trimNull(unit.get("fullName")));
      headData.add("单位简称 :" + trimNull(unit.get("smallName")));
      headData.add("科目拼音码 :" + trimNull(unit.get("spell")));
      headData.add("单位地址:" + trimNull(unit.get("address")));
      headData.add("单位电话 :" + trimNull(unit.get("phone")));
      headData.add("电子邮件:" + trimNull(unit.get("email")));
      headData.add("联系人一:" + trimNull(unit.get("contact1")));
      headData.add("手机一:" + trimNull(unit.get("mobile1")));
      headData.add("联系人二:" + trimNull(unit.get("contact2")));
      headData.add("手机二:" + trimNull(unit.get("mobile2")));
      headData.add("手机二:" + trimNull(unit.get("mobile2")));
      Staff staff = unit.getStaff(configName);
      if (staff != null) {
        headData.add("默认经手人:" + trimNull(staff.get("name")));
      }
      headData.add("开户银行 :" + trimNull(unit.get("bank")));
      headData.add("银行账号 :" + trimNull(unit.get("bankAccount")));
      headData.add("邮政编码 :" + trimNull(unit.get("zipCode")));
      headData.add("期初应收款 :" + trimNull(unit.get("beginGetMoney")));
      headData.add("期初应付款 :" + trimNull(unit.get("beginPayMoney")));
      headData.add("传真 :" + trimNull(unit.get("fax")));
      headData.add("应收款上限 :" + trimNull(unit.get("getMoneyLimit")));
      headData.add("应付款上限 :" + trimNull(unit.get("payMoneyLimit")));
      Area area = unit.getArea(configName);
      if (area != null) {
        headData.add("地区:" + trimNull(area.get("fullName")));
      }
      headData.add("换货期限 :" + trimNull(unit.get("replacePrdPeriod")));
      headData.add("换货比例 :" + trimNull(unit.get("replacePrdPercentage")));
      headData.add("结算期限(天):" + trimNull(unit.get("settlePeriod")));
      headData.add("累计应收:" + trimNull(unit.get("totalGet")));
      headData.add("累计应付:" + trimNull(unit.get("totalPay")));
      Integer fitPrice = unit.getInt("fitPrice");
      String fitPriceStr = "";
      if (fitPrice.intValue() == 0) {
        fitPriceStr = "无";
      } else if (fitPrice.intValue() == 1) {
        fitPriceStr = "零售价";
      } else if (fitPrice.intValue() == 2) {
        fitPriceStr = "预设售价1";
      } else if (fitPrice.intValue() == 3) {
        fitPriceStr = "预设售价2";
      } else if (fitPrice.intValue() == 4) {
        fitPriceStr = "预设售价3";
      }
      headData.add("适用价格:" + trimNull(fitPriceStr));
      headData.add("备注:" + trimNull(unit.get("memo")));
      Integer status = unit.getInt("status");
      String statusStr = "";
      if (status.intValue() == 1) {
        statusStr = "停用";
      } else if (status.intValue() == 2) {
        statusStr = "启用";
      }
      headData.add("状态:" + trimNull(statusStr));
    }
    else
    {
      headData.add("");
    }
  }
  
  protected void setOrderUserId(boolean flag, Model bill, String actionType, List<String> headData)
  {
    Integer userId = null;
    if ((flag) && (!actionType.equals("look"))) {
      userId = Integer.valueOf(loginUserId());
    } else {
      bill.getInt("userId");
    }
    setBillUser(headData, userId);
  }
  
  public void setBillUser(List<String> headData, Integer userId)
  {
    String configName = loginConfigName();
    if ((userId != null) && (userId.intValue() != 0))
    {
      Model user = User.dao.findById(configName, userId);
      headData.add("制单人:" + user.getStr("username"));
    }
    else
    {
      Record user = (Record)getSessionAttr("user");
      User.dao.findById(configName, userId);
      headData.add("制单人:" + user.getStr("username"));
    }
  }
  
  public static String trimNull(Object obj)
  {
    String str = "";
    if (obj != null) {
      if ((obj instanceof BigDecimal))
      {
        str = str + BigDecimalUtils.trim(new BigDecimal(String.valueOf(obj)).setScale(4, 4));
        if ("0".equalsIgnoreCase(str)) {
          str = "";
        }
      }
      else
      {
        str = str + obj;
      }
    }
    return str;
  }
  
  public String trimRecordDateNull(Date date)
  {
    if ((date == null) || (date.equals(""))) {
      return "";
    }
    try
    {
      return DateUtils.format(date, "yyyy-MM-dd");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return "";
  }
  
  public String trimNull(Object obj, boolean costPrivs)
  {
    String str = "";
    if (obj != null) {
      if (!costPrivs) {
        str = "***";
      } else if ((obj instanceof BigDecimal)) {
        str = str + new BigDecimal(String.valueOf(obj)).doubleValue();
      } else {
        str = str + obj;
      }
    }
    return str;
  }
  
  protected FlowBill dearFlowBill(String billTaName, int billId, int typeId)
  {
    String configName = loginConfigName();
    Record bill = Db.use(configName).findById(billTaName, Integer.valueOf(billId));
    FlowBill fBill = new FlowBill();
    
    fBill.setRecodeDate(bill.getDate("recodeDate"));
    fBill.setUnitId(bill.getInt("unitId"));
    fBill.setStaffId(bill.getInt("staffId"));
    fBill.setDepartmentId(bill.getInt("departmentId"));
    fBill.setRemark(bill.getStr("remark"));
    fBill.setMemo(bill.getStr("memo"));
    fBill.setAmounts(bill.getBigDecimal("amounts"));
    fBill.setMoneys(bill.getBigDecimal("moneys"));
    if (AioConstants.BILL_ROW_TYPE4 == typeId)
    {
      SellDraftBill.dao.outPublic(bill, fBill);
    }
    else if (AioConstants.BILL_ROW_TYPE5 == typeId)
    {
      PurchaseDraftBill.dao.outPublic(bill, fBill);
    }
    else if (AioConstants.BILL_ROW_TYPE6 == typeId)
    {
      PurchaseReturnDraftBill.dao.outPublic(bill, fBill);
    }
    else if (AioConstants.BILL_ROW_TYPE7 == typeId)
    {
      SellReturnDraftBill.dao.outPublic(bill, fBill);
    }
    else if (AioConstants.BILL_ROW_TYPE8 == typeId)
    {
      fBill.setOutStorageId(bill.getInt("storageId"));
    }
    else if (AioConstants.BILL_ROW_TYPE9 == typeId)
    {
      fBill.setInStorageId(bill.getInt("storageId"));
    }
    else if (AioConstants.BILL_ROW_TYPE10 == typeId)
    {
      OtherInDraftBill.dao.outPublic(bill, fBill);
    }
    else if (AioConstants.BILL_ROW_TYPE11 == typeId)
    {
      OtherOutDraftBill.dao.outPublic(bill, fBill);
    }
    else if (AioConstants.BILL_ROW_TYPE14 == typeId)
    {
      ParityAllotDraftBill.dao.outPublic(bill, fBill);
    }
    else if (AioConstants.BILL_ROW_TYPE15 == typeId)
    {
      fBill.setOutStorageId(bill.getInt("outStorageId"));
      fBill.setInStorageId(bill.getInt("inStorageId"));
      fBill.setDiscounts(bill.getBigDecimal("discounts"));
    }
    else if (AioConstants.BILL_ROW_TYPE20 == typeId)
    {
      AdjustCostDraftBill.dao.outPublic(bill, fBill);
    }
    return fBill;
  }
  
  protected Record loadFlowBill(FlowBill fBill, int newTypeId)
  {
    Record bill = new Record();
    
    Map<String, String> map = billCodeAuto(newTypeId);
    bill.set("code", String.valueOf(map.get("code")));
    bill.set("recodeDate", fBill.getRecodeDate());
    bill.set("updateTime", new Date());
    bill.set("userId", Integer.valueOf(loginUserId()));
    if (AioConstants.BILL_ROW_TYPE4 == newTypeId) {
      SellDraftBill.dao.inPublic(bill, fBill);
    } else if (AioConstants.BILL_ROW_TYPE5 == newTypeId) {
      PurchaseDraftBill.dao.inPublic(bill, fBill);
    } else if (AioConstants.BILL_ROW_TYPE6 == newTypeId) {
      PurchaseReturnDraftBill.dao.inPublic(bill, fBill);
    } else if (AioConstants.BILL_ROW_TYPE7 == newTypeId) {
      SellReturnDraftBill.dao.inPublic(bill, fBill);
    } else if (AioConstants.BILL_ROW_TYPE8 == newTypeId) {
      BreakageDraftBill.dao.inPublic(bill, fBill);
    } else if (AioConstants.BILL_ROW_TYPE9 == newTypeId) {
      OverflowDraftBill.dao.inPublic(bill, fBill);
    } else if (AioConstants.BILL_ROW_TYPE10 == newTypeId) {
      OtherInDraftBill.dao.inPublic(bill, fBill);
    } else if (AioConstants.BILL_ROW_TYPE11 == newTypeId) {
      OtherOutDraftBill.dao.inPublic(bill, fBill);
    } else if (AioConstants.BILL_ROW_TYPE14 == newTypeId) {
      ParityAllotDraftBill.dao.inPublic(bill, fBill);
    } else if (AioConstants.BILL_ROW_TYPE15 == newTypeId) {
      DifftAllotDraftBill.dao.inPublic(bill, fBill);
    } else if (AioConstants.BILL_ROW_TYPE20 == newTypeId) {
      AdjustCostDraftBill.dao.inPublic(bill, fBill);
    }
    return bill;
  }
  
  protected FlowDetail dearFlowDetail(Record detail, int typeId)
  {
    FlowDetail fDetail = new FlowDetail();
    
    fDetail.setProductId(detail.getInt("productId"));
    fDetail.setSelectUnitId(detail.getInt("selectUnitId"));
    fDetail.setAmount(detail.getBigDecimal("amount"));
    fDetail.setPrice(detail.getBigDecimal("price"));
    fDetail.setBatch(detail.getStr("batch"));
    fDetail.setProduceDate(detail.getDate("produceDate"));
    fDetail.setMoney(detail.getBigDecimal("money"));
    fDetail.setBaseAmount(detail.getBigDecimal("baseAmount"));
    fDetail.setBasePrice(detail.getBigDecimal("basePrice"));
    fDetail.setMemo(detail.getStr("memo"));
    if (AioConstants.BILL_ROW_TYPE4 == typeId)
    {
      SellDraftDetail.dao.outPublic(detail, fDetail);
    }
    else if (AioConstants.BILL_ROW_TYPE5 == typeId)
    {
      PurchaseDraftDetail.dao.outPublic(detail, fDetail);
    }
    else if (AioConstants.BILL_ROW_TYPE6 == typeId)
    {
      PurchaseReturnDraftDetail.dao.outPublic(detail, fDetail);
    }
    else if (AioConstants.BILL_ROW_TYPE7 == typeId)
    {
      SellReturnDraftDetail.dao.outPublic(detail, fDetail);
    }
    else if (AioConstants.BILL_ROW_TYPE8 == typeId)
    {
      fDetail.setCostPrice(detail.getBigDecimal("basePrice"));
      fDetail.setOutStorageId(detail.getInt("storageId"));
    }
    else if (AioConstants.BILL_ROW_TYPE9 == typeId)
    {
      fDetail.setCostPrice(detail.getBigDecimal("basePrice"));
      fDetail.setInStorageId(detail.getInt("storageId"));
    }
    else if (AioConstants.BILL_ROW_TYPE10 == typeId)
    {
      OtherInDraftDetail.dao.outPublic(detail, fDetail);
    }
    else if (AioConstants.BILL_ROW_TYPE11 == typeId)
    {
      OtherOutDraftDetail.dao.outPublic(detail, fDetail);
    }
    else if (AioConstants.BILL_ROW_TYPE14 == typeId)
    {
      ParityAllotDraftDetail.dao.outPublic(detail, fDetail);
    }
    else if (AioConstants.BILL_ROW_TYPE15 == typeId)
    {
      DifftAllotDraftDetail.dao.outPublic(detail, fDetail);
    }
    else if (AioConstants.BILL_ROW_TYPE20 == typeId)
    {
      AdjustCostDraftDetail.dao.outPublic(detail, fDetail);
    }
    return fDetail;
  }
  
  protected Record loadFlowDetail(FlowDetail fDetail, int newTypeId)
  {
    String configName = loginConfigName();
    Record detail = new Record();
    if (AioConstants.BILL_ROW_TYPE4 == newTypeId) {
      SellDraftDetail.dao.inPublic(configName, detail, fDetail, loginUserId());
    } else if (AioConstants.BILL_ROW_TYPE5 == newTypeId) {
      PurchaseDraftDetail.dao.inPublic(configName, detail, fDetail, loginUserId());
    } else if (AioConstants.BILL_ROW_TYPE6 == newTypeId) {
      PurchaseReturnDraftDetail.dao.inPublic(configName, detail, fDetail, loginUserId());
    } else if (AioConstants.BILL_ROW_TYPE7 == newTypeId) {
      SellReturnDraftDetail.dao.inPublic(configName, detail, fDetail, loginUserId());
    } else if (AioConstants.BILL_ROW_TYPE8 == newTypeId) {
      BreakageDraftDetail.dao.inPublic(configName, detail, fDetail, loginUserId());
    } else if (AioConstants.BILL_ROW_TYPE9 == newTypeId) {
      OverflowDraftDetail.dao.inPublic(configName, detail, fDetail, loginUserId());
    } else if (AioConstants.BILL_ROW_TYPE10 == newTypeId) {
      OtherInDraftDetail.dao.inPublic(configName, detail, fDetail, loginUserId());
    } else if (AioConstants.BILL_ROW_TYPE11 == newTypeId) {
      OtherOutDraftDetail.dao.inPublic(configName, detail, fDetail, loginUserId());
    } else if (AioConstants.BILL_ROW_TYPE14 == newTypeId) {
      ParityAllotDraftDetail.dao.inPublic(configName, detail, fDetail, loginUserId());
    } else if (AioConstants.BILL_ROW_TYPE15 == newTypeId) {
      DifftAllotDraftDetail.dao.inPublic(configName, detail, fDetail, loginUserId());
    } else if (AioConstants.BILL_ROW_TYPE20 == newTypeId) {
      AdjustCostDraftDetail.dao.inPublic(configName, detail, fDetail, loginUserId());
    }
    return detail;
  }
}
