package com.aioerp.controller.finance;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Unit;
import com.aioerp.model.finance.PayType;
import com.aioerp.model.reports.finance.AccountsCostRecords;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class PayTypeController
  extends BaseController
{
  public void option()
  {
    String payType = getPara("param", "payType");
    String ids = getPara("ids", "");
    String moneys = getPara("moneys", "");
    String[] idArra = ids.split(",");
    String[] moneyArra = moneys.split(",");
    List<Record> list = PayType.dao.payTypes(loginConfigName(), basePrivs(ACCOUNT_PRIVS));
    for (int i = 0; i < list.size(); i++) {
      for (int j = 0; j < idArra.length; j++) {
        if (String.valueOf(((Record)list.get(i)).getInt("id")).equals(idArra[j]))
        {
          ((Record)list.get(i)).set("aioMoney", moneyArra[j]);
          break;
        }
      }
    }
    setAttr("payTypes", list);
    setAttr("payType", payType);
    render("/WEB-INF/template/finance/payType/option.html");
  }
  
  public static void addAccountsRecoder(String configName, int modelType, int billId, Integer unitId, Integer staffId, Integer departmentId, BigDecimal privilege, String payTypeIds, String payTypeMoneys, Map<String, Object> map)
  {
    Date time = new Date();
    Integer privilegeType = Integer.valueOf(AioConstants.ACCOUNT_MONEY0);
    Integer getOrPayType = Integer.valueOf(AioConstants.ACCOUNT_MONEY0);
    int arapPrivilege = AioConstants.ARAPACCOUNT1;
    int arapgetOrPay = AioConstants.ARAPACCOUNT1;
    int loginUserId = 0;
    if (map != null) {
      loginUserId = Integer.valueOf(String.valueOf(map.get("loginUserId"))).intValue();
    }
    if ((AioConstants.BILL_ROW_TYPE1 != modelType) && 
      (AioConstants.BILL_ROW_TYPE2 != modelType) && 
      (AioConstants.BILL_ROW_TYPE3 != modelType)) {
      if (AioConstants.BILL_ROW_TYPE4 == modelType)
      {
        BigDecimal sellCosts = BigDecimal.ZERO;
        BigDecimal sellMoneys = BigDecimal.ZERO;
        BigDecimal needGetOrPay = BigDecimal.ZERO;
        BigDecimal taxes = BigDecimal.ZERO;
        if (map != null)
        {
          if (map.get("sellCosts") != null) {
            sellCosts = new BigDecimal(String.valueOf(map.get("sellCosts")));
          }
          if (map.get("taxes") != null) {
            taxes = new BigDecimal(String.valueOf(map.get("taxes")));
          }
          if (map.get("sellMoneys") != null) {
            sellMoneys = new BigDecimal(String.valueOf(map.get("sellMoneys")));
          }
          if (map.get("needGetOrPay") != null) {
            needGetOrPay = new BigDecimal(String.valueOf(map.get("needGetOrPay")));
          }
        }
        Accounts ac00033 = (Accounts)Accounts.dao.getModelFirst(configName, "00033");
        Accounts ac00016 = (Accounts)Accounts.dao.getModelFirst(configName, "00016");
        Accounts ac00019 = (Accounts)Accounts.dao.getModelFirst(configName, "00019");
        Accounts ac000413 = (Accounts)Accounts.dao.getModelFirst(configName, "000413");
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00033.getInt("id"), sellCosts, time, loginUserId);
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00016.getInt("id"), taxes, time, loginUserId);
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00019.getInt("id"), sellMoneys, time, loginUserId);
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000413.getInt("id"), needGetOrPay, time, loginUserId);
        
        Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
        List<Record> proAccountList = null;
        if (map != null) {
          proAccountList = (List)map.get("proAccountList");
        }
        for (int i = 0; i < proAccountList.size(); i++)
        {
          Record r = (Record)proAccountList.get(i);
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT1), ac000412.getInt("id"), r.getBigDecimal("costMoney"), time, loginUserId);
        }
        privilegeType = Integer.valueOf(AioConstants.ACCOUNT_MONEY0);
        getOrPayType = Integer.valueOf(AioConstants.ACCOUNT_MONEY0);
      }
      else if (AioConstants.BILL_ROW_TYPE5 == modelType)
      {
        BigDecimal payMoney = BigDecimal.ZERO;
        BigDecimal shouldPay = BigDecimal.ZERO;
        if (map != null)
        {
          payMoney = (BigDecimal)map.get("payMoney");
          shouldPay = BigDecimalUtils.sub((BigDecimal)map.get("purchaseMoneys"), BigDecimalUtils.add(payMoney, privilege));
          Accounts ac00013 = (Accounts)Accounts.dao.getModelFirst(configName, "00013");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00013.getInt("id"), shouldPay, time, loginUserId);
          BigDecimal taxes = (BigDecimal)map.get("taxes");
          if (BigDecimalUtils.notNullZero(taxes))
          {
            Accounts ac00016 = (Accounts)Accounts.dao.getModelFirst(configName, "00016");
            addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00016.getInt("id"), taxes, time, loginUserId);
          }
          List<Model> proAccountList = (List)map.get("proAccountList");
          
          Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
          for (int i = 0; i < proAccountList.size(); i++)
          {
            Model r = (Model)proAccountList.get(i);
            addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT1), ac000412.getInt("id"), r.getBigDecimal("discountMoney"), time, loginUserId);
          }
        }
        privilegeType = Integer.valueOf(AioConstants.ACCOUNT_MONEY1);
        getOrPayType = Integer.valueOf(AioConstants.ACCOUNT_MONEY1);
      }
      else if (AioConstants.BILL_ROW_TYPE7 == modelType)
      {
        BigDecimal sellReturnCosts = BigDecimal.ZERO;
        BigDecimal sellReturnMoneys = BigDecimal.ZERO;
        BigDecimal needGetOrPay = BigDecimal.ZERO;
        BigDecimal taxes = BigDecimal.ZERO;
        if (map != null)
        {
          if (map.get("sellReturnCosts") != null) {
            sellReturnCosts = new BigDecimal(String.valueOf(map.get("sellReturnCosts")));
          }
          if (map.get("taxes") != null) {
            taxes = new BigDecimal(String.valueOf(map.get("taxes")));
          }
          if (map.get("sellReturnMoneys") != null) {
            sellReturnMoneys = new BigDecimal(String.valueOf(map.get("sellReturnMoneys")));
          }
          if (map.get("needGetOrPay") != null) {
            needGetOrPay = new BigDecimal(String.valueOf(map.get("needGetOrPay")));
          }
        }
        Accounts ac00033 = (Accounts)Accounts.dao.getModelFirst(configName, "00033");
        Accounts ac00016 = (Accounts)Accounts.dao.getModelFirst(configName, "00016");
        Accounts ac00019 = (Accounts)Accounts.dao.getModelFirst(configName, "00019");
        Accounts ac000413 = (Accounts)Accounts.dao.getModelFirst(configName, "000413");
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00033.getInt("id"), sellReturnCosts, time, loginUserId);
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00016.getInt("id"), taxes, time, loginUserId);
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00019.getInt("id"), sellReturnMoneys, time, loginUserId);
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000413.getInt("id"), needGetOrPay, time, loginUserId);
        

        Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
        List<Record> proAccountList = null;
        if (map != null) {
          proAccountList = (List)map.get("proAccountList");
        }
        for (int i = 0; i < proAccountList.size(); i++)
        {
          Record r = (Record)proAccountList.get(i);
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT1), ac000412.getInt("id"), r.getBigDecimal("costMoney"), time, loginUserId);
        }
        privilegeType = Integer.valueOf(AioConstants.ACCOUNT_MONEY1);
        getOrPayType = Integer.valueOf(AioConstants.ACCOUNT_MONEY1);
      }
      else if (AioConstants.BILL_ROW_TYPE8 == modelType)
      {
        BigDecimal moneys = (BigDecimal)map.get("moneys");
        if (BigDecimalUtils.notNullZero(moneys))
        {
          Accounts ac00037 = (Accounts)Accounts.dao.getModelFirst(configName, "00037");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00037.getInt("id"), moneys, time, loginUserId);
        }
        Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
        List<Model> proAccountList = null;
        if (map != null) {
          proAccountList = (List)map.get("proAccountList");
        }
        for (int i = 0; i < proAccountList.size(); i++)
        {
          Model r = (Model)proAccountList.get(i);
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000412.getInt("id"), r.getBigDecimal("money"), time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE9 == modelType)
      {
        BigDecimal moneys = (BigDecimal)map.get("moneys");
        if (BigDecimalUtils.notNullZero(moneys))
        {
          Accounts ac00023 = (Accounts)Accounts.dao.getModelFirst(configName, "00023");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00023.getInt("id"), moneys, time, loginUserId);
        }
        Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
        List<Model> proAccountList = null;
        if (map != null) {
          proAccountList = (List)map.get("proAccountList");
        }
        for (int i = 0; i < proAccountList.size(); i++)
        {
          Model r = (Model)proAccountList.get(i);
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000412.getInt("id"), r.getBigDecimal("money"), time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE10 == modelType)
      {
        int accountId = 0;
        BigDecimal otherInCostMoney = BigDecimal.ZERO;
        if (map != null)
        {
          accountId = Integer.valueOf(String.valueOf(map.get("accountId"))).intValue();
          if (map.get("billMoneys") != null) {
            otherInCostMoney = new BigDecimal(String.valueOf(map.get("billMoneys")));
          }
        }
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), null, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), Integer.valueOf(accountId), otherInCostMoney, time, loginUserId);
        

        Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
        List<Record> proAccountList = null;
        if (map != null) {
          proAccountList = (List)map.get("proAccountList");
        }
        for (int i = 0; i < proAccountList.size(); i++)
        {
          Record r = (Record)proAccountList.get(i);
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), null, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT1), ac000412.getInt("id"), r.getBigDecimal("costMoney"), time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE11 == modelType)
      {
        int accountId = 0;
        BigDecimal otherOutCostMoney = BigDecimal.ZERO;
        if (map != null)
        {
          accountId = Integer.valueOf(String.valueOf(map.get("accountId"))).intValue();
          if (map.get("billMoneys") != null) {
            otherOutCostMoney = new BigDecimal(String.valueOf(map.get("billMoneys")));
          }
        }
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), null, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), Integer.valueOf(accountId), otherOutCostMoney, time, loginUserId);
        

        Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
        List<Record> proAccountList = null;
        if (map != null) {
          proAccountList = (List)map.get("proAccountList");
        }
        for (int i = 0; i < proAccountList.size(); i++)
        {
          Record r = (Record)proAccountList.get(i);
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), null, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT1), ac000412.getInt("id"), r.getBigDecimal("costMoney"), time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE12 == modelType)
      {
        BigDecimal payMoney = BigDecimal.ZERO;
        BigDecimal shouldPay = BigDecimal.ZERO;
        if (map != null)
        {
          payMoney = (BigDecimal)map.get("payMoney");
          shouldPay = BigDecimalUtils.sub((BigDecimal)map.get("gapMoney"), BigDecimalUtils.add(payMoney, privilege));
          Accounts ac00013 = (Accounts)Accounts.dao.getModelFirst(configName, "00013");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00013.getInt("id"), shouldPay, time, loginUserId);
          BigDecimal taxes = (BigDecimal)map.get("taxes");
          if (BigDecimalUtils.notNullZero(taxes))
          {
            Accounts ac00016 = (Accounts)Accounts.dao.getModelFirst(configName, "00016");
            addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00016.getInt("id"), taxes, time, loginUserId);
          }
          Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
          List<Model> proAccountListIn = (List)map.get("proAccountListIn");
          for (int i = 0; i < proAccountListIn.size(); i++)
          {
            Model r = (Model)proAccountListIn.get(i);
            addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT1), ac000412.getInt("id"), r.getBigDecimal("discountMoney"), time, loginUserId);
          }
          List<Record> proAccountListOut = (List)map.get("proAccountListOut");
          for (int i = 0; i < proAccountListOut.size(); i++)
          {
            Record r = (Record)proAccountListOut.get(i);
            addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT1), ac000412.getInt("id"), r.getBigDecimal("costMoney"), time, loginUserId);
          }
          Accounts ac00026 = (Accounts)Accounts.dao.getModelFirst(configName, "00026");
          for (int i = 0; i < proAccountListOut.size(); i++)
          {
            Record r = (Record)proAccountListOut.get(i);
            BigDecimal costMoney = r.getBigDecimal("costMoney");
            BigDecimal discountMoney = r.getBigDecimal("discountMoney");
            BigDecimal gapMoney = BigDecimalUtils.sub(costMoney, discountMoney);
            addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00026.getInt("id"), gapMoney, time, loginUserId);
          }
        }
        privilegeType = Integer.valueOf(AioConstants.ACCOUNT_MONEY1);
        getOrPayType = Integer.valueOf(AioConstants.ACCOUNT_MONEY1);
      }
      else if (AioConstants.BILL_ROW_TYPE15 == modelType)
      {
        BigDecimal inMoney = BigDecimal.ZERO;
        BigDecimal outMoney = BigDecimal.ZERO;
        
        Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
        List<Model> proAccountList = null;
        if (map != null) {
          proAccountList = (List)map.get("proAccountList");
        }
        for (int i = 0; i < proAccountList.size(); i++)
        {
          Model r = (Model)proAccountList.get(i);
          BigDecimal costMoney = r.getBigDecimal("discountMoney");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000412.getInt("id"), costMoney, time, loginUserId);
          inMoney = BigDecimalUtils.add(inMoney, costMoney);
        }
        for (int i = 0; i < proAccountList.size(); i++)
        {
          Model r = (Model)proAccountList.get(i);
          BigDecimal costMoney = BigDecimalUtils.mul(r.getBigDecimal("costPrice"), r.getBigDecimal("baseAmount"));
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000412.getInt("id"), costMoney, time, loginUserId);
          outMoney = BigDecimalUtils.add(outMoney, costMoney);
        }
        Accounts ac00027 = (Accounts)Accounts.dao.getModelFirst(configName, "00027");
        BigDecimal diffCJ = BigDecimalUtils.sub(inMoney, outMoney);
        if (BigDecimalUtils.notNullZero(diffCJ)) {
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00027.getInt("id"), diffCJ, time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE17 == modelType)
      {
        List<Model> accountList = null;
        BigDecimal needGetOrPay = BigDecimal.ZERO;
        if (map != null)
        {
          accountList = (List)map.get("accountList");
          if (map.get("needGetOrPay") != null) {
            needGetOrPay = new BigDecimal(String.valueOf(map.get("needGetOrPay")));
          }
        }
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT1), Integer.valueOf(accountId), money, time, loginUserId);
        }
        Accounts ac000413 = (Accounts)Accounts.dao.getModelFirst(configName, "000413");
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000413.getInt("id"), needGetOrPay, time, loginUserId);
        
        privilegeType = Integer.valueOf(AioConstants.ACCOUNT_MONEY0);
      }
      else if (AioConstants.BILL_ROW_TYPE19 == modelType)
      {
        List<Model> accountList = null;
        BigDecimal needGetOrPay = BigDecimal.ZERO;
        if (map != null)
        {
          accountList = (List)map.get("accountList");
          if (map.get("needGetOrPay") != null) {
            needGetOrPay = new BigDecimal(String.valueOf(map.get("needGetOrPay")));
          }
        }
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT1), Integer.valueOf(accountId), money, time, loginUserId);
        }
        Accounts ac00013 = (Accounts)Accounts.dao.getModelFirst(configName, "00013");
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00013.getInt("id"), needGetOrPay, time, loginUserId);
        
        privilegeType = Integer.valueOf(AioConstants.ACCOUNT_MONEY1);
      }
      else if (AioConstants.BILL_ROW_TYPE21 == modelType)
      {
        List<Model> accountList = (List)map.get("accountList");
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), Integer.valueOf(accountId), money, time, loginUserId);
        }
        Integer payId = (Integer)map.get("payAccountId");
        BigDecimal payMoney = (BigDecimal)map.get("payMoney");
        if ((payId != null) && (payId.intValue() > 0) && (BigDecimalUtils.notNullZero(payMoney))) {
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE1), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), payId, payMoney, time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE22 == modelType)
      {
        List<Model> accountList = null;
        BigDecimal needGetOrPay = BigDecimal.ZERO;
        if (map != null)
        {
          accountList = (List)map.get("accountList");
          if (map.get("needGetOrPay") != null) {
            needGetOrPay = new BigDecimal(String.valueOf(map.get("needGetOrPay")));
          }
        }
        Integer hasArap = Integer.valueOf(AioConstants.ARAPACCOUNT0);
        if ((unitId != null) && (unitId.intValue() != 0))
        {
          hasArap = Integer.valueOf(AioConstants.ARAPACCOUNT1);
          
          Accounts ac000413 = (Accounts)Accounts.dao.getModelFirst(configName, "000413");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000413.getInt("id"), needGetOrPay, time, loginUserId);
          

          getOrPayType = Integer.valueOf(AioConstants.ACCOUNT_MONEY1);
        }
        else
        {
          Accounts ac0006 = (Accounts)Accounts.dao.getModelFirst(configName, "0006");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac0006.getInt("id"), needGetOrPay, time, loginUserId);
          
          getOrPayType = Integer.valueOf(AioConstants.ACCOUNT_MONEY1);
          arapgetOrPay = AioConstants.ARAPACCOUNT0;
        }
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, hasArap, Integer.valueOf(accountId), money, time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE23 == modelType)
      {
        BigDecimal needPayMoney = (BigDecimal)map.get("needPayMoney");
        BigDecimal payMoney = (BigDecimal)map.get("payMoney");
        if (BigDecimalUtils.notNullZero(needPayMoney))
        {
          Accounts ac00013 = (Accounts)Accounts.dao.getModelFirst(configName, "00013");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00013.getInt("id"), needPayMoney, time, loginUserId);
        }
        List<Model> accountList = (List)map.get("accountList");
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT1), Integer.valueOf(accountId), money, time, loginUserId);
        }
        Integer payId = (Integer)map.get("payAccountId");
        if ((payId != null) && (payId.intValue() > 0) && (BigDecimalUtils.notNullZero(payMoney))) {
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE1), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), payId, payMoney, time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE24 == modelType)
      {
        List<Model> accountList = (List)map.get("accountList");
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), Integer.valueOf(accountId), money, time, loginUserId);
        }
        BigDecimal deprMoney = (BigDecimal)map.get("deprMoney");
        if (BigDecimalUtils.notNullZero(deprMoney))
        {
          Accounts ac00041 = (Accounts)Accounts.dao.getModelFirst(configName, "00041");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac00041.getInt("id"), deprMoney, time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE25 == modelType)
      {
        BigDecimal needGetMoney = (BigDecimal)map.get("needGetMoney");
        BigDecimal getMoney = (BigDecimal)map.get("getMoney");
        if (BigDecimalUtils.notNullZero(needGetMoney))
        {
          Accounts ac000413 = (Accounts)Accounts.dao.getModelFirst(configName, "000413");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000413.getInt("id"), needGetMoney, time, loginUserId);
        }
        List<Model> accountList = (List)map.get("accountList");
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT1), Integer.valueOf(accountId), money, time, loginUserId);
        }
        Integer getId = (Integer)map.get("getAccountId");
        if ((getId != null) && (getId.intValue() > 0) && (BigDecimalUtils.notNullZero(getMoney))) {
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE1), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), getId, getMoney, time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE26 == modelType)
      {
        List<Model> accountList = null;
        BigDecimal totalMoney = BigDecimal.ZERO;
        if (map != null)
        {
          accountList = (List)map.get("accountList");
          if (map.get("totalMoney") != null) {
            totalMoney = new BigDecimal(String.valueOf(map.get("totalMoney")));
          }
        }
        Accounts ac000413 = (Accounts)Accounts.dao.getModelFirst(configName, "000413");
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT1), ac000413.getInt("id"), totalMoney, time, loginUserId);
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), Integer.valueOf(accountId), money, time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE27 == modelType)
      {
        List<Model> accountList = null;
        BigDecimal totalMoney = BigDecimal.ZERO;
        if (map != null)
        {
          accountList = (List)map.get("accountList");
          if (map.get("totalMoney") != null) {
            totalMoney = new BigDecimal(String.valueOf(map.get("totalMoney")));
          }
        }
        Accounts ac000413 = (Accounts)Accounts.dao.getModelFirst(configName, "000413");
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT1), ac000413.getInt("id"), totalMoney, time, loginUserId);
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), Integer.valueOf(accountId), money, time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE28 == modelType)
      {
        List<Model> accountList = null;
        BigDecimal totalMoney = BigDecimal.ZERO;
        if (map != null)
        {
          accountList = (List)map.get("accountList");
          if (map.get("totalMoney") != null) {
            totalMoney = new BigDecimal(String.valueOf(map.get("totalMoney")));
          }
        }
        Accounts ac000413 = (Accounts)Accounts.dao.getModelFirst(configName, "000413");
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT1), ac000413.getInt("id"), totalMoney, time, loginUserId);
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), Integer.valueOf(accountId), money, time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE29 == modelType)
      {
        List<Model> accountList = null;
        BigDecimal totalMoney = BigDecimal.ZERO;
        if (map != null)
        {
          accountList = (List)map.get("accountList");
          if (map.get("totalMoney") != null) {
            totalMoney = new BigDecimal(String.valueOf(map.get("totalMoney")));
          }
        }
        Accounts ac000413 = (Accounts)Accounts.dao.getModelFirst(configName, "000413");
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE3), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT1), ac000413.getInt("id"), totalMoney, time, loginUserId);
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT0), Integer.valueOf(accountId), money, time, loginUserId);
        }
      }
      else if (AioConstants.BILL_ROW_TYPE30 == modelType)
      {
        List<Model> accountList = null;
        if (map != null) {
          accountList = (List)map.get("accountList");
        }
        for (int i = 0; i < accountList.size(); i++)
        {
          int accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
          BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
          addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT1), Integer.valueOf(accountId), money, time, loginUserId);
        }
        getOrPayType = Integer.valueOf(AioConstants.ACCOUNT_MONEY1);
      }
      else
      {
        int accountId;
        if (AioConstants.BILL_ROW_TYPE31 == modelType)
        {
          List<Model> accountList = null;
          if (map != null) {
            accountList = (List)map.get("accountList");
          }
          for (int i = 0; i < accountList.size(); i++)
          {
            accountId = ((Model)accountList.get(i)).getInt("accountsId").intValue();
            BigDecimal money = ((Model)accountList.get(i)).getBigDecimal("money");
            addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(AioConstants.ARAPACCOUNT1), Integer.valueOf(accountId), money, time, loginUserId);
          }
          getOrPayType = Integer.valueOf(AioConstants.ACCOUNT_MONEY0);
        }
        else
        {
          privilegeType = (Integer)map.get("privilegeType");
          getOrPayType = (Integer)map.get("getOrPayType");
          
          List<Record> accounts = (List)map.get("accounts");
          if (accounts != null) {
            for (Record record : accounts)
            {
              Integer moneyType = record.getInt("moneyType");
              Integer accountType = record.getInt("accountType");
              Integer arapAccount = record.getInt("arapAccount");
              BigDecimal money = record.getBigDecimal("money");
              Accounts account = (Accounts)Accounts.dao.getModelFirst(configName, record.getStr("account"));
              if ((account != null) && (BigDecimalUtils.notNullZero(money))) {
                addAccountsRecords(configName, accountType, moneyType, Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, arapAccount, account.getInt("id"), money, time, loginUserId);
              }
            }
          }
          List<Model> proAccountList = (List)map.get("proAccountList");
          if (proAccountList != null)
          {
            Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
            Integer proAccountType = (Integer)map.get("proAccountType");
            if (proAccountType == null) {
              proAccountType = Integer.valueOf(AioConstants.ACCOUNT_MONEY0);
            }
            int arapaccount = AioConstants.ARAPACCOUNT0;
            if ((unitId != null) && (unitId.intValue() != 0)) {
              arapaccount = AioConstants.ARAPACCOUNT1;
            }
            for (int i = 0; i < proAccountList.size(); i++)
            {
              Model r = (Model)proAccountList.get(i);
              
              BigDecimal money = r.getBigDecimal("discountMoney");
              if (money == null) {
                money = r.getBigDecimal("money");
              }
              BigDecimal costPrice = r.getBigDecimal("costPrice");
              if ((costPrice != null) && (proAccountType.intValue() != AioConstants.ACCOUNT_MONEY0))
              {
                BigDecimal baseAmount = r.getBigDecimal("baseAmount");
                money = BigDecimalUtils.mul(costPrice, baseAmount);
              }
              addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), proAccountType, Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(arapaccount), ac000412.getInt("id"), money, time, loginUserId);
            }
          }
          List<Model> proInList = (List)map.get("proInList");
          if (proInList != null)
          {
            Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
            for (int i = 0; i < proInList.size(); i++)
            {
              Model r = (Model)proInList.get(i);
              BigDecimal money = r.getBigDecimal("discountMoney");
              if (money == null) {
                money = r.getBigDecimal("money");
              }
              BigDecimal costPrice = r.getBigDecimal("costPrice");
              if (costPrice != null)
              {
                BigDecimal baseAmount = r.getBigDecimal("baseAmount");
                money = BigDecimalUtils.mul(costPrice, baseAmount);
              }
              addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY0), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000412.getInt("id"), money, time, loginUserId);
            }
          }
          List<Model> proOutList = (List)map.get("proOutList");
          if (proOutList != null)
          {
            Accounts ac000412 = (Accounts)Accounts.dao.getModelFirst(configName, "000412");
            for (int i = 0; i < proOutList.size(); i++)
            {
              Model r = (Model)proOutList.get(i);
              BigDecimal money = r.getBigDecimal("discountMoney");
              if (money == null) {
                money = r.getBigDecimal("money");
              }
              BigDecimal costPrice = r.getBigDecimal("costPrice");
              if (costPrice != null)
              {
                BigDecimal baseAmount = r.getBigDecimal("baseAmount");
                money = BigDecimalUtils.mul(costPrice, baseAmount);
              }
              addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE0), Integer.valueOf(AioConstants.ACCOUNT_MONEY1), Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, r.getInt("productId"), Integer.valueOf(AioConstants.ARAPACCOUNT0), ac000412.getInt("id"), money, time, loginUserId);
            }
          }
        }
      }
    }
    if ((privilege != null) && (BigDecimalUtils.notNullZero(privilege)))
    {
      Accounts ac = (Accounts)Accounts.dao.getModelFirst(configName, "00042");
      
      addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE2), privilegeType, Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(arapPrivilege), ac.getInt("id"), privilege, time, loginUserId);
    }
    if (StringUtils.isNotBlank(payTypeIds))
    {
      String[] accounts = payTypeIds.split(",");
      String[] payMoneys = payTypeMoneys.split(",");
      for (int i = 0; i < accounts.length; i++)
      {
        BigDecimal payMoneyi = new BigDecimal(payMoneys[i]);
        
        addAccountsRecords(configName, Integer.valueOf(AioConstants.ACCOUNT_TYPE1), getOrPayType, Integer.valueOf(modelType), Integer.valueOf(billId), unitId, staffId, departmentId, null, Integer.valueOf(arapgetOrPay), Integer.valueOf(accounts[i]), payMoneyi, time, loginUserId);
      }
    }
  }
  
  public static void rcwAccountsRecoder(String configName, int modelType, Integer oldBillId, Integer newBillId)
  {
    Date time = new Date();
    
    List<Model> dd1 = PayType.dao.getListByBillId1(configName, oldBillId, Integer.valueOf(modelType), null);
    if ((dd1 != null) && (dd1.size() > 0)) {
      for (int i = 0; i < dd1.size(); i++)
      {
        Model m = (Model)dd1.get(i);
        m.set("id", null);
        m.set("billId", newBillId);
        Integer type = m.getInt("type");
        if (type.intValue() == AioConstants.ACCOUNT_MONEY0) {
          m.set("type", Integer.valueOf(AioConstants.ACCOUNT_MONEY1));
        } else if (type.intValue() == AioConstants.ACCOUNT_MONEY1) {
          m.set("type", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
        }
        m.set("payTime", time);
        m.save(configName);
      }
    }
  }
  
  public static void editRcwAccountsRecoder(String configName, int modelType, Integer billId)
  {
    List<Model> dd1 = PayType.dao.getListByBillId1(configName, billId, Integer.valueOf(modelType), null);
    if ((dd1 != null) && (dd1.size() > 0)) {
      for (int i = 0; i < dd1.size(); i++)
      {
        Model m = (Model)dd1.get(i);
        
        Integer type = m.getInt("type");
        if (type.intValue() == AioConstants.ACCOUNT_MONEY0) {
          m.set("type", Integer.valueOf(AioConstants.ACCOUNT_MONEY1));
        } else if (type.intValue() == AioConstants.ACCOUNT_MONEY1) {
          m.set("type", Integer.valueOf(AioConstants.ACCOUNT_MONEY0));
        }
        m.update(configName);
      }
    }
  }
  
  public static Unit updateUnitNeedGetOrOut(String configName, String operate, int type, int unitId, BigDecimal payMoney, BigDecimal privilegeMoney)
  {
    Unit unit = (Unit)Unit.dao.findById(configName, Integer.valueOf(unitId));
    if (unit == null) {
      return null;
    }
    BigDecimal totalPay = unit.getBigDecimal("totalPay");
    BigDecimal totalGet = unit.getBigDecimal("totalGet");
    if ((operate.equals(AioConstants.OPERTE_RCW)) || (operate.equals(AioConstants.OPERTE_DEL))) {
      if (type == AioConstants.PAY_TYLE0) {
        type = AioConstants.PAY_TYLE1;
      } else if (type == AioConstants.PAY_TYLE1) {
        type = AioConstants.PAY_TYLE0;
      }
    }
    BigDecimal money = BigDecimalUtils.sub(privilegeMoney, payMoney);
    if (BigDecimalUtils.compare(money, BigDecimal.ZERO) == -1)
    {
      if (type == AioConstants.PAY_TYLE0) {
        type = AioConstants.PAY_TYLE1;
      } else if (type == AioConstants.PAY_TYLE1) {
        type = AioConstants.PAY_TYLE0;
      }
      money = BigDecimalUtils.negate(money);
    }
    if (type == AioConstants.PAY_TYLE0)
    {
      if (BigDecimalUtils.compare(totalGet, money) == 1)
      {
        totalGet = BigDecimalUtils.sub(totalGet, money);
        unit.set("totalGet", totalGet);
      }
      else if (BigDecimalUtils.compare(totalGet, money) == 0)
      {
        unit.set("totalGet", BigDecimal.ZERO);
      }
      else
      {
        totalPay = BigDecimalUtils.add(totalPay, BigDecimalUtils.sub(money, totalGet));
        unit.set("totalGet", BigDecimal.ZERO);
        unit.set("totalPay", totalPay);
      }
    }
    else if (type == AioConstants.PAY_TYLE1) {
      if (BigDecimalUtils.compare(totalPay, money) == 1)
      {
        totalPay = BigDecimalUtils.sub(totalPay, money);
        unit.set("totalPay", totalPay);
      }
      else if (BigDecimalUtils.compare(totalPay, money) == 0)
      {
        unit.set("totalPay", BigDecimal.ZERO);
      }
      else
      {
        totalGet = BigDecimalUtils.add(totalGet, BigDecimalUtils.sub(money, totalPay));
        unit.set("totalPay", BigDecimal.ZERO);
        unit.set("totalGet", totalGet);
      }
    }
    unit.set("updateTime", new Date());
    unit.update(configName);
    return unit;
  }
  
  public static void addPayRecords(String configName, int type, int billTypeId, int billId, int unitId, Integer accountId, BigDecimal payMoney, Date payTime)
  {
    PayType pay = new PayType();
    pay.set("type", Integer.valueOf(type));
    pay.set("billTypeId", Integer.valueOf(billTypeId));
    pay.set("billId", Integer.valueOf(billId));
    pay.set("unitId", Integer.valueOf(unitId));
    pay.set("accountId", accountId);
    pay.set("payMoney", payMoney);
    pay.set("payTime", payTime);
    pay.save(configName);
  }
  
  public static void addAccountsRecords(String configName, Integer accountType, Integer type, Integer billTypeId, Integer billId, Integer unitId, Integer staffId, Integer departmentId, Integer productId, Integer hasAccount, Integer accountId, BigDecimal payMoney, Date payTime, int loginUserId)
  {
    if (BigDecimalUtils.compare(payMoney, BigDecimal.ZERO) != 0)
    {
      PayType pay = new PayType();
      pay.set("accountType", accountType);
      pay.set("type", type);
      pay.set("billTypeId", billTypeId);
      pay.set("billId", billId);
      pay.set("unitId", unitId);
      pay.set("staffId", staffId);
      pay.set("departmentId", departmentId);
      pay.set("productId", productId);
      pay.set("hasAccount", hasAccount);
      pay.set("accountId", accountId);
      pay.set("payMoney", payMoney);
      pay.set("payTime", payTime);
      pay.set("userId", Integer.valueOf(loginUserId));
      pay.save(configName);
    }
  }
  
  public static BigDecimal getLastYearProfitMoney(String configName)
    throws ParseException
  {
    List<String> accT = new ArrayList();
    accT.add("0008");
    List<Record> accounts = AccountsCostRecords.dao.getList(configName, null, accT);
    BigDecimal getAllMoneys = null;
    if ((accounts != null) && (accounts.size() > 0)) {
      getAllMoneys = ((Record)accounts.get(0)).getBigDecimal("allMoneys");
    }
    accT = new ArrayList();
    accT.add("00010");
    accounts = AccountsCostRecords.dao.getList(configName, null, accT);
    BigDecimal payAllMoneys = null;
    if ((accounts != null) && (accounts.size() > 0)) {
      payAllMoneys = ((Record)accounts.get(0)).getBigDecimal("allMoneys");
    }
    return BigDecimalUtils.sub(getAllMoneys, payAllMoneys);
  }
  
  public static void delCurrentGetOrPay(String configName)
  {
    List<Record> list = Db.use(configName).find("select id from b_accounts b where b.pids like '{0}{9}%' or b.pids like '{0}{11}%'");
    String ids = "";
    if (list != null) {
      for (int i = 0; i < list.size(); i++) {
        ids = ids + ((Record)list.get(i)).getInt("id") + ",";
      }
    }
    if (!ids.equals("")) {
      ids = ids.substring(0, ids.length() - 1);
    }
    Db.use(configName).update("delete from cw_pay_type where accountId in(" + ids + ")");
  }
  
  public static void yearEndInitData(String configName)
  {
    Db.use(configName).update("delete from cc_stock_init");
    Db.use(configName).update("insert into cc_stock_init (id,productId,amount,price,money,batch,storageId,produceDate, updateTime)select id, productId, amount,costPrice,amount*costPrice,batchNum,storageId,produceDate,updateTime  from cc_stock");
    

    Db.use(configName).update("update b_unit set beginGetMoney=totalGet,beginPayMoney=totalPay,totalGet=0,totalPay=0");
    

    StringBuffer sb2 = new StringBuffer();
    sb2.append("update  b_accounts ac left join  cw_pay_type pt on pt.accountId=ac.id set ac.money=");
    
    sb2.append("(select sum(ifnull(ac.money,0)+ifnull(p.payMoney,0)) ");
    sb2.append("from cw_pay_type p where p.accountId=ac.id)");
    Db.use(configName).update(sb2.toString());
  }
  
  public static void updateAccountMoney(String configName, int type, Accounts account, BigDecimal payMoneyi, Date time)
  {
    BigDecimal money = account.getBigDecimal("money");
    account.set("updateTime", time);
    if (type == AioConstants.PAY_TYLE0) {
      account.set("money", BigDecimalUtils.sub(money, payMoneyi));
    } else if (type == AioConstants.PAY_TYLE1) {
      account.set("money", BigDecimalUtils.add(money, payMoneyi));
    }
    account.update(configName);
  }
}
