package com.aioerp.controller.base;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Accounts;
import com.aioerp.model.base.Product;
import com.aioerp.util.NumberUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class AccountsController
  extends BaseController
{
  public void list()
  {
    String configName = loginConfigName();
    String whichRefresh = getPara("whichRefresh", "");
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int pSupId = 0;
    

    String searchPar1 = getPara("searchPar1");
    String searchValue1 = getPara("searchValue1");
    Integer node = getParaToInt("node", Integer.valueOf(0));
    String actionType = getPara("actionType", "");
    if (actionType.equals("line")) {
      node = Integer.valueOf(AioConstants.NODE_1);
    }
    String accountType = getPara(0, "");
    int supId = getParaToInt(1, getParaToInt("supId", Integer.valueOf(0))).intValue();
    String typeVal = typeNameToTypeCode(accountType);
    if (whichRefresh.equals("navTabRefresh")) {
      if (typeVal.equals(""))
      {
        Record record = Db.use(configName).findFirst("select id,supId from b_accounts where status<>" + AioConstants.STATUS_DELETE);
        pSupId = record.getInt("supId").intValue();
      }
      else
      {
        Record record = Db.use(configName).findFirst("select id,supId from b_accounts where status<>" + AioConstants.STATUS_DELETE + " and type=" + typeVal);
        if (supId == 0) {
          supId = record.getInt("id").intValue();
        }
      }
    }
    int upObjectId = getParaToInt(2, Integer.valueOf(0)).intValue();
    int objectId = 0;
    String refreshTable = returnRefreshDiv(accountType);
    if (upObjectId > 0)
    {
      setAttr("pageMap", Accounts.dao.getPageByUpObject(configName, basePrivs(ACCOUNT_PRIVS), refreshTable, supId, upObjectId, pageNum, numPerPage, searchPar1, searchValue1, ORDER_FIELD, ORDER_DIRECTION));
      objectId = upObjectId;
    }
    else
    {
      String showLastPage = getPara("showLastPage");
      int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
      objectId = selectedObjectId;
      if ((showLastPage != null) && (showLastPage.equals("true"))) {
        setAttr("pageMap", Accounts.dao.getPageByFilterLast(configName, basePrivs(ACCOUNT_PRIVS), refreshTable, supId, searchPar1, searchValue1, pageNum, numPerPage, ORDER_FIELD, ORDER_DIRECTION));
      } else {
        setAttr("pageMap", Accounts.dao.getPageByCondtion(configName, basePrivs(ACCOUNT_PRIVS), refreshTable, supId, node, searchPar1, searchValue1, pageNum, numPerPage, orderField, orderDirection));
      }
    }
    if (supId > 0) {
      pSupId = Accounts.dao.getPsupId(configName, supId);
    }
    setAttr("searchPar1", searchPar1);
    setAttr("searchValue1", searchValue1);
    setAttr("node", node);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("toAction", "list");
    setAttr("accountType", accountType);
    setAttr("trel", returnRefreshTreeDiv(accountType));
    setAttr("refreshTable", refreshTable);
    setAttr("objectId", Integer.valueOf(objectId));
    
    int accountTypeId = getAccountTypeId(typeVal);
    setAttr("topSupId", Integer.valueOf(accountTypeId));
    

    columnConfig("b503");
    if (whichRefresh.equals("navTabRefresh")) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void tree()
  {
    String configName = loginConfigName();
    String accountType = getPara(0, "0");
    int supId = 0;
    int pSupId = 0;
    Record record = null;
    String typeVal = typeNameToTypeCode(accountType);
    if (typeVal.equals(""))
    {
      record = Db.use(configName).findFirst("select id,supId from b_accounts where status<>" + AioConstants.STATUS_DELETE);
    }
    else
    {
      record = Db.use(configName).findFirst("select id,supId from b_accounts where status<>" + AioConstants.STATUS_DELETE + " and type=" + typeVal);
      supId = record.getInt("id").intValue();
    }
    pSupId = record.getInt("supId").intValue();
    List<Model> cats = Accounts.dao.getParentObjects(configName, basePrivs(ACCOUNT_PRIVS), supId);
    Iterator<Model> iter = cats.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model object = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", object.get("id"));
      node.put("pId", object.get("supId"));
      node.put("name", object.get("fullName"));
      node.put("url", "base/accounts/list/" + accountType + "-" + object.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(pSupId));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "科目");
    node.put("url", "base/accounts/list/" + accountType + "-" + supId);
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public void add()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Accounts.class);
      String accountType = getPara("accountType");
      int numAutoAdd = getParaToInt("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0)).intValue();
      String code = model.getStr("code");
      if (Accounts.dao.existObjectByNum(configName, 0, code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson(this.result);
        return;
      }
      String upids = "";
      int supId = model.getInt("supId").intValue();
      if (supId > 0) {
        upids = Accounts.dao.findById(configName, model.get("supId")).getStr("pids");
      } else {
        upids = "{0}";
      }
      model.set("node", Integer.valueOf(AioConstants.NODE_1));
      model.set("pids", upids);
      model.set("createTime", new Date());
      model.set("updateTime", new Date());
      model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      model.set("type", Integer.valueOf(0));
      model.set("isSysRow", Integer.valueOf(0));
      Boolean flag = Boolean.valueOf(model.save(configName));
      updateBasePrivs(model.getInt("id"), model.getInt("supId"), ACCOUNT_PRIVS);
      if (flag.booleanValue())
      {
        model.set("rank", model.getInt("id"));
        model.set("pids", model.get("pids") + "{" + model.getInt("id") + "}");
        model.update(configName);
        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("rel", returnRefreshDiv(accountType));
        this.result.put("base", getAttr("base"));
        if (numAutoAdd == AioConstants.NUMNOADD1)
        {
          code = NumberUtils.increase(code);
          
          this.result.put("loadDialogUrl", "/base/accounts/add/" + accountType + "-" + supId + "-" + URLEncoder.encode(code) + "-" + numAutoAdd);
        }
        else
        {
          this.result.put("loadDialogUrl", "/base/accounts/add/" + accountType + "-" + supId);
        }
        this.result.put("reset", "reset");
        this.result.put("dialogId", "b_accounts_id");
        this.result.put("opterate", "showLastPage");
        this.result.put("selectedObjectId", model.get("id"));
      }
      else
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "操作失败！！！");
      }
      renderJson(this.result);
    }
    else
    {
      String accountType = getPara(0, "");
      setAttr("accountType", accountType);
      int supId = getParaToInt(1, Integer.valueOf(0)).intValue();
      setAttr("supId", Integer.valueOf(supId));
      String one = getPara(2);
      if ((one != null) && (!one.equals("")))
      {
        setAttr("code", URLDecoder.decode(one));
        setAttr("numAutoAdd", getParaToInt(3));
      }
      setAttr("toAction", "add");
      render("dialog.html");
    }
  }
  
  public void copyAdd()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Accounts.class);
      String accountType = getPara("accountType");
      int numAutoAdd = getParaToInt("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0)).intValue();
      String code = model.getStr("code");
      if (Accounts.dao.existObjectByNum(configName, 0, code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson(this.result);
        return;
      }
      Model oldModel = Accounts.dao.findById(configName, model.getInt("id"));
      
      model.set("supId", oldModel.get("supId"));
      String oldPids = (String)oldModel.get("pids");
      model.set("pids", oldPids.subSequence(0, oldPids.lastIndexOf("{")));
      
      model.set("node", Integer.valueOf(AioConstants.NODE_1));
      model.set("createTime", new Date());
      model.set("updateTime", new Date());
      model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      model.set("type", Integer.valueOf(0));
      model.set("isSysRow", Integer.valueOf(0));
      model.set("id", null);
      Boolean flag = Boolean.valueOf(model.save(configName));
      updateBasePrivs(model.getInt("id"), model.getInt("supId"), ACCOUNT_PRIVS);
      if (flag.booleanValue())
      {
        model.set("pids", model.get("pids") + "{" + model.getInt("id") + "}");
        model.set("rank", model.getInt("id")).update(configName);
        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("rel", returnRefreshDiv(accountType));
        if (numAutoAdd == AioConstants.NUMNOADD1)
        {
          code = NumberUtils.increase(code);
          this.result.put("code", code);
        }
        this.result.put("opterate", "showLastPage");
        this.result.put("selectedObjectId", model.get("id"));
      }
      else
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "操作失败！！！");
      }
      renderJson(this.result);
    }
    else
    {
      String accountType = getPara(0, "");
      setAttr("accountType", accountType);
      Accounts accounts = (Accounts)Accounts.dao.getObjectById(configName, getParaToInt(1, Integer.valueOf(0)).intValue());
      setAttr("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0));
      setAttr("toAction", "copyAdd");
      setAttr("accounts", accounts);
      render("dialog.html");
    }
  }
  
  public void edit()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Accounts.class);
      String accountType = getPara("accountType");
      int numAutoAdd = getParaToInt("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0)).intValue();
      String code = model.getStr("code");
      if (Accounts.dao.existObjectByNum(configName, model.getInt("id").intValue(), code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson();
        return;
      }
      Model oldModel = Accounts.dao.findById(configName, model.getInt("id"));
      model.set("supId", oldModel.get("supId"));
      model.set("pids", oldModel.get("pids"));
      model.set("node", oldModel.get("node"));
      model.set("rank", oldModel.get("rank"));
      model.set("createTime", oldModel.get("createTime"));
      model.set("updateTime", new Date());
      model.set("status", oldModel.get("status"));
      model.set("isSysRow", oldModel.get("isSysRow"));
      
      Boolean flag = Boolean.valueOf(model.update(configName));
      if (flag.booleanValue())
      {
        if (numAutoAdd == AioConstants.NUMNOADD1) {
          setAttr("code", NumberUtils.increase(model.getStr("code")));
        }
        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("callbackType", "closeCurrent");
        this.result.put("rel", returnRefreshDiv(accountType));
        HashMap<String, Object> node = new HashMap();
        node.put("id", model.get("id"));
        node.put("name", model.get("fullName"));
        this.result.put("node", node);
        this.result.put("trel", returnRefreshTreeDiv(accountType));
        this.result.put("url", "base/accounts/list/" + accountType + "-" + model.get("supId"));
        
        this.result.put("selectedObjectId", model.get("id"));
      }
      renderJson(this.result);
    }
    else
    {
      String accountType = getPara(0, "");
      setAttr("accountType", accountType);
      Accounts accounts = (Accounts)Accounts.dao.getObjectById(configName, getParaToInt(1, Integer.valueOf(0)).intValue());
      setAttr("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0));
      setAttr("toAction", "edit");
      setAttr("accounts", accounts);
      render("dialog.html");
    }
  }
  
  @Before({Tx.class})
  public void delete()
  {
    String configName = loginConfigName();
    String accountType = getPara(0, "");
    int id = getParaToInt(1, Integer.valueOf(0)).intValue();
    




    boolean verify = Accounts.dao.verify(configName, "accountId", Integer.valueOf(id)).booleanValue();
    if (verify)
    {
      returnToPage(AioConstants.HTTP_RETURN300, "数据存在引用，不能删除!", "", "", "", "");
      renderJson();
      return;
    }
    if (Accounts.dao.hasChildren(configName, id))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "删除失败，请先删除下级!");
      renderJson(this.result);
      return;
    }
    Model<Accounts> accounts = Accounts.dao.findById(configName, Integer.valueOf(id));
    int supId = ((Integer)accounts.get("supId", Integer.valueOf(0))).intValue();
    if (Accounts.dao.deleteById(configName, Integer.valueOf(id)))
    {
      this.result.put("statusCode", Integer.valueOf(200));
      this.result.put("message", "");
      
      this.result.put("rel", returnRefreshDiv(accountType));
    }
    if ((!Accounts.dao.hasChildren(configName, supId)) && (supId != 0))
    {
      Model<Accounts> pAccounts = Accounts.dao.findById(configName, Integer.valueOf(supId));
      ((Accounts)pAccounts.set("node", Integer.valueOf(AioConstants.NODE_1))).update(configName);
      
      this.result.put("trel", returnRefreshTreeDiv(accountType));
      
      HashMap<String, Object> node = new HashMap();
      node.put("id", pAccounts.get("id"));
      node.put("pId", pAccounts.get("supId"));
      this.result.put("pnode", node);
      

      this.result.put("rel", returnRefreshDiv(accountType));
      this.result.put("url", "base/accounts/list/" + accountType + "-" + pAccounts.get("supId") + "-" + supId);
      this.result.put("selectedObjectId", pAccounts.get("id"));
    }
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void sort()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Accounts.class);
      String accountType = getPara("accountType");
      String code = model.getStr("code");
      if (Accounts.dao.existObjectByNum(configName, 0, code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson(this.result);
        return;
      }
      Model pAccounts = Accounts.dao.findById(configName, model.getInt("id"));
      model.set("supId", pAccounts.get("id"));
      model.set("node", Integer.valueOf(AioConstants.NODE_1));
      
      model.set("pids", pAccounts.getStr("pids"));
      model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      model.set("createTime", new Date());
      model.set("updateTime", new Date());
      model.set("type", Integer.valueOf(0));
      model.set("isSysRow", Integer.valueOf(0));
      model.set("id", null);
      Boolean flag = Boolean.valueOf(model.save(configName));
      updateBasePrivs(model.getInt("id"), model.getInt("supId"), ACCOUNT_PRIVS);
      if (flag.booleanValue())
      {
        model.set("pids", model.get("pids") + "{" + model.getInt("id") + "}");
        model.set("rank", model.getInt("id")).update(configName);
        
        pAccounts.set("node", Integer.valueOf(AioConstants.NODE_2)).update(configName);
        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("callbackType", "closeCurrent");
        
        this.result.put("rel", returnRefreshDiv(accountType));
        this.result.put("target", "ajax");
        
        this.result.put("trel", returnRefreshTreeDiv(accountType));
        this.result.put("url", "base/accounts/list/" + accountType + "-" + pAccounts.get("id"));
        HashMap<String, Object> node = new HashMap();
        node.put("id", pAccounts.get("id"));
        node.put("pId", pAccounts.get("supId"));
        node.put("name", pAccounts.get("fullName"));
        node.put("url", "base/accounts/list/" + accountType + "-" + pAccounts.get("id"));
        this.result.put("pnode", node);
        this.result.put("opterate", "showLastPage");
        this.result.put("selectedObjectId", model.get("id"));
      }
      else
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "操作失败！");
      }
      renderJson(this.result);
    }
    else
    {
      String accountType = getPara(0, "");
      setAttr("accountType", accountType);
      Accounts accounts = (Accounts)Accounts.dao.getObjectById(configName, getParaToInt(1, Integer.valueOf(0)).intValue());
      setAttr("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0));
      setAttr("supId", accounts.getInt("id"));
      setAttr("toAction", "sort");
      setAttr("accounts", accounts);
      render("dialog.html");
    }
  }
  
  public void saveRank()
  {
    String configName = loginConfigName();
    String id = getPara("ids");
    if (!id.equals(""))
    {
      String seq = getPara("orders");
      String[] ids = id.split(",");
      String[] seqs = seq.split(",");
      for (int i = 0; i < ids.length; i++) {
        Accounts.dao.findById(configName, ids[i]).set("rank", seqs[i]).update(configName);
      }
    }
    returnToPage("", "", "", "", "", "");
    renderJson();
  }
  
  public void disableOrEnable()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    Accounts accounts = (Accounts)Accounts.dao.getObjectById(configName, id);
    Boolean flag = Boolean.valueOf(false);
    if (accounts.getInt("status").intValue() == AioConstants.STATUS_DISABLE) {
      flag = Boolean.valueOf(accounts.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE)).update(configName));
    } else {
      flag = Boolean.valueOf(accounts.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE)).update(configName));
    }
    if (flag.booleanValue())
    {
      this.result.put("id", Integer.valueOf(id));
      this.result.put("status", accounts.getInt("status"));
    }
    renderJson(this.result);
  }
  
  public void filter()
  {
    if (isPost())
    {
      this.result.put("searchPar1", getPara("searchPar1"));
      this.result.put("searchValue1", getPara("searchValue1"));
      this.result.put("statusCode", Integer.valueOf(200));
      
      renderJson(this.result);
    }
    else
    {
      String accountType = getPara(0, "");
      setAttr("accountType", accountType);
      render("filter.html");
    }
  }
  
  public void filterData()
  {
    String configName = loginConfigName();
    String accountType = getPara(0, "");
    String refreshTable = returnRefreshDiv(accountType);
    String searchPar1 = getPara("searchPar1");
    String searchValue1 = getPara("searchValue1");
    setAttr("searchPar1", searchPar1);
    setAttr("searchValue1", searchValue1);
    int supId = getParaToInt("supId").intValue();
    int pageNum = 1;
    int numPerPage = getParaToInt("numPerPage").intValue();
    
    Map<String, Object> pageMap = Accounts.dao.getPageByCondtion(configName, basePrivs(ACCOUNT_PRIVS), refreshTable, supId, null, searchPar1, searchValue1, pageNum, numPerPage, ORDER_FIELD, ORDER_DIRECTION);
    setAttr("pageMap", pageMap);
    
    int accountTypeId = getAccountTypeId(typeNameToTypeCode(accountType));
    setAttr("pSupId", Integer.valueOf(0));
    setAttr("supId", Integer.valueOf(accountTypeId));
    

    setAttr("accountType", accountType);
    setAttr("trel", returnRefreshTreeDiv(accountType));
    setAttr("refreshTable", refreshTable);
    setAttr("topSupId", Integer.valueOf(accountTypeId));
    

    columnConfig("b503");
    render("page.html");
  }
  
  public String returnName(String accountType)
  {
    String name = "";
    if (accountType.equals("accounts")) {
      name = "会计科目";
    } else if (accountType.equals("property")) {
      name = "固定资产";
    } else if (accountType.equals("bank")) {
      name = "银行账户";
    } else if (accountType.equals("incomeType")) {
      name = "收入类型";
    } else if (accountType.equals("cost")) {
      name = "费用类型";
    }
    return name;
  }
  
  public String returnRefreshDiv(String accountType)
  {
    String listTable = "";
    if (accountType.equals("accounts")) {
      listTable = "b_accounts_listTable";
    } else if (accountType.equals("property")) {
      listTable = "propertyList";
    } else if (accountType.equals("bank")) {
      listTable = "bankList";
    } else if (accountType.equals("incomeType")) {
      listTable = "b_incomeType_listTable";
    } else if (accountType.equals("cost")) {
      listTable = "costList";
    }
    return listTable;
  }
  
  public String returnRefreshTreeDiv(String accountType)
  {
    String trel = "";
    if (accountType.equals("accounts")) {
      trel = "b_accounts_treeId";
    } else if (accountType.equals("property")) {
      trel = "propertyTree";
    } else if (accountType.equals("bank")) {
      trel = "bankTree";
    } else if (accountType.equals("incomeType")) {
      trel = "b_incomeType_treeId";
    } else if (accountType.equals("cost")) {
      trel = "costTree";
    }
    return trel;
  }
  
  public String typeNameToTypeCode(String type)
  {
    String typeVal = "";
    if (type.equals("accounts")) {
      typeVal = "";
    } else if (type.equals("property")) {
      typeVal = "0002";
    } else if (type.equals("bank")) {
      typeVal = "0004";
    } else if (type.equals("incomeType")) {
      typeVal = "00021";
    } else if (type.equals("cost")) {
      typeVal = "00036";
    }
    return typeVal;
  }
  
  public int getAccountTypeId(String typeCode)
  {
    String configName = loginConfigName();
    if (typeCode.equals("")) {
      return 0;
    }
    Record record = Db.use(configName).findFirst("select id,supId from b_accounts where status<>" + AioConstants.STATUS_DELETE + " and type=" + typeCode);
    return record.getInt("id").intValue();
  }
  
  public void option()
  {
    String configName = loginConfigName();
    String operType = getPara(0, "");
    
    int pageNum = AioConstants.START_PAGE;
    int numPerPage = 10;
    if (StringUtils.isNotBlank(getPara("pageNum"))) {
      pageNum = getParaToInt("pageNum").intValue();
    }
    if (StringUtils.isNotBlank(getPara("numPerPage"))) {
      numPerPage = getParaToInt("numPerPage").intValue();
    }
    String orderField = getPara("orderField", "rank");
    String orderDirection = getPara("orderDirection", "asc");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    String term = getPara("term", "");
    String termVal = getPara("termVal", "");
    setAttr("term", term);
    setAttr("termVal", termVal);
    
    String param = getPara("param");
    
    Map<String, Object> map = new HashMap();
    map.put("term", term);
    map.put("termVal", termVal);
    
    Map<String, Object> accountsMap = null;
    
    int supId = 0;
    int pSupId = 0;
    if ((operType.equals("page")) || (operType.equals("search")))
    {
      supId = getParaToInt("supId").intValue();
      if ((supId == 0) || (operType.equals("search")))
      {
        accountsMap = Accounts.dao.getTypeAccountList(configName, basePrivs(ACCOUNT_PRIVS), pageNum, numPerPage, orderField, orderDirection, param, map);
      }
      else
      {
        pSupId = Accounts.dao.getPsupId(configName, supId);
        accountsMap = Accounts.dao.getTypeAccountPageSearch(configName, basePrivs(ACCOUNT_PRIVS), supId, pageNum, numPerPage, orderField, orderDirection, map);
      }
    }
    else if (operType.equals("down"))
    {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
      pSupId = Accounts.dao.getPsupId(configName, supId);
      accountsMap = Accounts.dao.getTypeAccountPageSearch(configName, basePrivs(ACCOUNT_PRIVS), supId, pageNum, numPerPage, orderField, orderDirection, null);
    }
    else if (operType.equals("up"))
    {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
      int objectId = getParaToInt(2, Integer.valueOf(0)).intValue();
      
      Boolean isFrist = Boolean.valueOf(false);
      if ((objectId > 0) && (!StringUtil.isNull(param)))
      {
        Accounts account = (Accounts)Accounts.dao.findById(configName, Integer.valueOf(objectId));
        int id = account.getInt("supId").intValue();
        String[] params = param.split(",");
        for (int i = 0; i < params.length; i++) {
          if (id == Integer.parseInt(params[i]))
          {
            isFrist = Boolean.valueOf(true);
            break;
          }
        }
      }
      if (isFrist.booleanValue())
      {
        supId = 0;
        accountsMap = Accounts.dao.getTypeAccountList(configName, basePrivs(ACCOUNT_PRIVS), pageNum, numPerPage, orderField, orderDirection, param, map);
      }
      else
      {
        accountsMap = Accounts.dao.getTypeAccountFirst(configName, basePrivs(ACCOUNT_PRIVS), supId, pageNum, numPerPage, "");
      }
      setAttr("objectId", Integer.valueOf(objectId));
    }
    else
    {
      accountsMap = Accounts.dao.getTypeAccountList(configName, basePrivs(ACCOUNT_PRIVS), pageNum, numPerPage, orderField, orderDirection, param, map);
    }
    setAttr("pageMap", accountsMap);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("param", param);
    
    render("/WEB-INF/template/base/accounts/option.html");
  }
  
  public static Map<String, String> setAccountsArea(String whichCallBack)
  {
    Map<String, String> map = new HashMap();
    if (whichCallBack.equals("singleAccounts"))
    {
      map.put("0003", "noSelf");
      map.put("0004", "noSelf");
    }
    else if (whichCallBack.equals("getMoney"))
    {
      map.put("0003", "noSelf");
      map.put("0004", "noSelf");
    }
    else if (whichCallBack.equals("otherEarn"))
    {
      map.put("00021", "noSelf");
    }
    else if (whichCallBack.equals("payMoney"))
    {
      map.put("0003", "noSelf");
      map.put("0004", "noSelf");
    }
    else if (whichCallBack.equals("transfer"))
    {
      map.put("0003", "noSelf");
      map.put("0004", "noSelf");
    }
    else if (whichCallBack.equals("feeBill"))
    {
      map.put("00036", "noSelf");
    }
    else if (whichCallBack.equals("assets"))
    {
      map.put("0002", "noSelf");
    }
    else if (whichCallBack.equals("cwunitGetDown"))
    {
      map.put("00036", "noSelf");
    }
    else if (whichCallBack.equals("cwunitGetUp"))
    {
      map.put("00021", "noSelf");
    }
    else if (whichCallBack.equals("cwunitPayDown"))
    {
      map.put("00021", "noSelf");
    }
    else if (whichCallBack.equals("cwunitPayUp"))
    {
      map.put("00036", "noSelf");
    }
    else if (whichCallBack.equals("cwmoneyDown"))
    {
      map.put("00036", "noSelf");
    }
    else if (whichCallBack.equals("cwmoneyUp"))
    {
      map.put("00021", "noSelf");
    }
    else if (whichCallBack.equals("accountVoucher"))
    {
      map.put("0001", "self");
      map.put("0007", "self");
      map.put("0008", "self");
      map.put("00010", "self");
      map.put("00011", "self");
      map.put("00012", "self");
    }
    return map;
  }
  
  private int accountsSupIdIsZero(String configName, String whichCallBack, int selectSupId)
  {
    Model m = Accounts.dao.getAccountsByNum(configName, basePrivs(ACCOUNT_PRIVS), whichCallBack, "", "", selectSupId);
    if (m == null) {
      return 0;
    }
    Map<String, String> map = setAccountsArea(whichCallBack);
    if (map.containsKey(m.getStr("type"))) {
      return 0;
    }
    String pids = m.getStr("pids");
    String supPids = pids.substring(0, pids.lastIndexOf("{"));
    

    String supObjeId = supPids.substring(supPids.lastIndexOf("{") + 1, supPids.lastIndexOf("}"));
    
    Model mm = Accounts.dao.findById(configName, supObjeId);
    if (map.containsKey(mm.getStr("type"))) {
      return 0;
    }
    return selectSupId;
  }
  
  public void accountsOneRecord()
    throws UnsupportedEncodingException
  {
    String whichCallBack = getPara(0, "");
    String attrType = getPara(1, "");
    String attrVale = getPara(2, "");
    String configName = loginConfigName();
    attrVale = URLDecoder.decode(attrVale, "UTF-8");
    if ((!attrVale.equals("")) && (Accounts.dao.existObjectByIdCodeNode(configName, basePrivs(ACCOUNT_PRIVS), whichCallBack, 0, attrType, attrVale, AioConstants.NODE_1)))
    {
      this.result.put("success", AioConstants.HTTP_RETURN200);
      this.result.put("obj", Accounts.dao.getAccountsByNum(configName, basePrivs(ACCOUNT_PRIVS), whichCallBack, attrType, attrVale, 0));
    }
    else
    {
      this.result.put("success", AioConstants.HTTP_RETURN300);
    }
    this.result.put("whichCallBack", whichCallBack);
    renderJson(this.result);
  }
  
  public void accountsDialog()
    throws UnsupportedEncodingException
  {
    int pSupId = 0;
    int supId = 0;
    int upObjectId = 0;
    String oldDownSelectObjs = "";
    String newDownSelectObjs = "";
    String opterate = "";
    String term = "";
    String termVal = "";
    String configName = loginConfigName();
    String type = getPara(0, "toDialog");
    String whichCallBack = getPara("whichCallBack", getPara(1, "getMoney"));
    if (type.equals("toDialog"))
    {
      opterate = getPara(2, "");
    }
    else if (type.equals("page"))
    {
      supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
      
      oldDownSelectObjs = getPara("oldSelectCheckBoxIds", "");
      newDownSelectObjs = getPara("selectCheckBoxIds", "");
    }
    else if (type.equals("search"))
    {
      oldDownSelectObjs = getPara("oldSelectCheckBoxIds", "");
      newDownSelectObjs = getPara("selectCheckBoxIds", "");
      String attrType = getPara(2, "");
      termVal = getPara(3, "");
      opterate = getPara(4, "");
      if (opterate.equals("")) {
        opterate = getPara("opterate", "");
      }
      termVal = URLDecoder.decode(termVal, "UTF-8");
      if ((attrType != "") && (attrType.equals("accountsCode"))) {
        term = "accountsCode";
      } else if ((attrType != "") && (attrType.equals("accountsFullName"))) {
        term = "accountsFullName";
      }
    }
    else if (type.equals("down"))
    {
      supId = getParaToInt(1, Integer.valueOf(0)).intValue();
      opterate = getPara("opterate", "");
      oldDownSelectObjs = getPara("oldSelectCheckBoxIds", "");
      newDownSelectObjs = getPara("selectCheckBoxIds", "");
    }
    else if (type.equals("up"))
    {
      supId = getParaToInt("pSupId", Integer.valueOf(0)).intValue();
      upObjectId = getParaToInt("supId", Integer.valueOf(0)).intValue();
      opterate = getPara("opterate", "");
      setAttr("opterate", opterate);
      oldDownSelectObjs = getPara("oldSelectCheckBoxIds", "");
      newDownSelectObjs = getPara("selectCheckBoxIds", "");
    }
    if (supId > 0) {
      pSupId = Accounts.dao.getPsupId(configName, supId);
    }
    if (type.equals("up"))
    {
      supId = accountsSupIdIsZero(configName, whichCallBack, supId);
      if (supId == 0) {
        pSupId = 0;
      }
    }
    String orderField = getPara("orderField");
    String orderDirection = getPara("orderDirection");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    if (term.equals(""))
    {
      term = getPara("term");
      termVal = getPara("termVal");
    }
    setAttr("term", term);
    setAttr("termVal", termVal);
    Map<String, Object> map = new HashMap();
    map.put("term", term == null ? "" : term);
    map.put("termVal", termVal == null ? "" : termVal);
    





    Map<String, Object> pageMap = null;
    if (type.equals("toDialog"))
    {
      pageMap = Accounts.dao.orderStockSearch(configName, basePrivs(ACCOUNT_PRIVS), whichCallBack, supId, pageNum, numPerPage, orderField, orderDirection, map);
    }
    else if ((type.equals("search")) || (type.equals("page")) || (type.equals("down")) || (type.equals("up")))
    {
      if ((type.equals("up")) && (upObjectId > 0))
      {
        pageMap = Accounts.dao.orderUpPlace(configName, basePrivs(ACCOUNT_PRIVS), whichCallBack, supId, pageNum, numPerPage, orderField, orderDirection, upObjectId, "", map);
      }
      else if (type.equals("search"))
      {
        pageMap = Accounts.dao.orderSearch(configName, basePrivs(ACCOUNT_PRIVS), whichCallBack, pageNum, numPerPage, orderField, orderDirection, map);
        
        List searchList = (List)pageMap.get("pageList");
        if ((pageMap != null) && (searchList != null) && (searchList.size() > 0))
        {
          Model model = (Model)searchList.get(0);
          supId = Integer.valueOf(model.getInt("supId").intValue()).intValue();
        }
      }
      else if (type.equals("down"))
      {
        pageMap = Accounts.dao.orderDown(configName, basePrivs(ACCOUNT_PRIVS), whichCallBack, supId, pageNum, numPerPage, orderField, orderDirection, map);
      }
      else
      {
        pageMap = Accounts.dao.orderStockSearch(configName, basePrivs(ACCOUNT_PRIVS), whichCallBack, supId, pageNum, numPerPage, orderField, orderDirection, map);
      }
      List<Product> alllist = (List)pageMap.get("listAllIdAndPids");
      List<Model> subBySupIdlist = (List)pageMap.get("listSubIdAndPidsBySupId");
      

      List<String> newSelectList = StringUtil.strsArrToList(newDownSelectObjs.split(","));
      
      Map<String, List<String>> addAndDelAndEqMapList = StringUtil.getAddAndDelAndEqList(oldDownSelectObjs.split(","), newDownSelectObjs.split(","));
      List<String> deleteList = (List)addAndDelAndEqMapList.get("delete");
      if ((deleteList != null) && (alllist != null)) {
        for (int i = 0; i < deleteList.size(); i++)
        {
          String deleteId = (String)deleteList.get(i);
          for (int j = 0; j < alllist.size(); j++)
          {
            String id = ((Product)alllist.get(j)).getInt("id").toString();
            String pids = ((Product)alllist.get(j)).getStr("pids");
            if (deleteId.equals(id))
            {
              pids = pids.substring(1, pids.length() - 1);
              List<String> pidsList = StringUtil.strsArrToList(pids.split("\\}\\{"));
              for (int k = 0; k < pidsList.size(); k++)
              {
                String pidsIds = (String)pidsList.get(k);
                newSelectList.remove(pidsIds);
              }
            }
          }
        }
      }
      if ((newSelectList.contains(String.valueOf(supId))) && (subBySupIdlist != null)) {
        for (int i = 0; i < subBySupIdlist.size(); i++)
        {
          String id = ((Model)subBySupIdlist.get(i)).getInt("id").toString();
          if (!newSelectList.contains(id)) {
            newSelectList.add(id);
          }
        }
      }
      newDownSelectObjs = "";
      for (int i = 0; i < newSelectList.size(); i++) {
        newDownSelectObjs = newDownSelectObjs + "," + (String)newSelectList.get(i);
      }
      newDownSelectObjs = newDownSelectObjs.substring(1, newDownSelectObjs.length());
    }
    setAttr("whichCallBack", whichCallBack);
    setAttr("pageMap", pageMap);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("objectId", Integer.valueOf(upObjectId));
    setAttr("selectCheckboxObjs", newDownSelectObjs);
    if ("".equals(opterate)) {
      opterate = getPara("opterate", "");
    }
    if ("".equals(opterate)) {
      setAttr("opterate", "edit");
    } else {
      setAttr("opterate", opterate);
    }
    render("/WEB-INF/template/base/accounts/multiSelectOption/accountsMultiOption.html");
  }
  
  public void accountsDialogEdit()
  {
    int selectId = getParaToInt(0, Integer.valueOf(0)).intValue();
    int selectSupId = getParaToInt(1, Integer.valueOf(0)).intValue();
    String whichCallBack = getPara(2, "");
    String configName = loginConfigName();
    selectSupId = accountsSupIdIsZero(configName, whichCallBack, selectSupId);
    
    Map<String, Object> map = Accounts.dao.orderStockPrdSelectEdit(configName, basePrivs(ACCOUNT_PRIVS), whichCallBack, selectSupId, selectId, AioConstants.START_PAGE, 10, null, null);
    

    setAttr("whichCallBack", whichCallBack);
    setAttr("pageMap", map);
    setAttr("objectId", Integer.valueOf(selectId));
    setAttr("opterate", "edit");
    if (selectSupId == 0)
    {
      setAttr("supId", Integer.valueOf(0));
      setAttr("pSupId", Integer.valueOf(0));
    }
    else
    {
      setAttr("supId", Integer.valueOf(selectSupId));
      Model model = Accounts.dao.findById(configName, Integer.valueOf(selectSupId));
      setAttr("pSupId", model.getInt("supId"));
    }
    render("/WEB-INF/template/base/accounts/multiSelectOption/accountsMultiOption.html");
  }
  
  public void accountsSearchBack()
  {
    String configName = loginConfigName();
    String newDownSelectObjs = getPara("newDownSelectObjs", "");
    if (newDownSelectObjs.equals(""))
    {
      int accountsId = getParaToInt(0, Integer.valueOf(0)).intValue();
      newDownSelectObjs = String.valueOf(accountsId);
    }
    String whichCallBack = getPara("whichCallBack", "");
    List<Model> list = Accounts.dao.searchBack(configName, basePrivs(ACCOUNT_PRIVS), newDownSelectObjs);
    Map<String, Object> obj = new HashMap();
    if (("accountVoucher".equals(whichCallBack)) && 
      (list != null) && (list.size() > 0))
    {
      Accounts accounts = Accounts.dao.getSysParent(configName, basePrivs(ACCOUNT_PRIVS), ((Model)list.get(0)).getInt("id"));
      obj.put("sysAccounts", accounts);
    }
    obj.put("whichCallBack", whichCallBack);
    obj.put("list", list);
    renderJson(obj);
  }
  
  public void print()
  {
    String configName = loginConfigName();
    String accountType = getPara("accountType", "");
    
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    if (accountType.equals("accounts")) {
      data.put("reportName", "会计科目");
    } else if (accountType.equals("property")) {
      data.put("reportName", "固定资产");
    } else if (accountType.equals("bank")) {
      data.put("reportName", "银行账户");
    } else if (accountType.equals("incomeType")) {
      data.put("reportName", "收入类型");
    } else if (accountType.equals("cost")) {
      data.put("reportName", "费用类型");
    }
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    List<Model> detailList = new ArrayList();
    

    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int pageNum = AioConstants.START_PAGE;
    int numPerPage = getParaToInt("totalCount", Integer.valueOf(9999999)).intValue();
    if (numPerPage == 0)
    {
      renderJson();
      return;
    }
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String searchPar1 = getPara("searchPar1");
    String searchValue1 = getPara("searchValue1");
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    Map<String, Object> pageMap = null;
    String refreshTable = returnRefreshDiv(accountType);
    pageMap = Accounts.dao.getPageByCondtion(configName, basePrivs(ACCOUNT_PRIVS), refreshTable, supId, Integer.valueOf(node), searchPar1, searchValue1, pageNum, numPerPage, orderField, orderDirection);
    if (pageMap != null) {
      detailList = (List)pageMap.get("pageList");
    }
    printCommonData(headData);
    

    colTitle.add("行号");
    colTitle.add("科目编号");
    colTitle.add("科目全名");
    colTitle.add("科目简名");
    colTitle.add("科目拼音码");
    colTitle.add("科目备注");
    colTitle.add("状态");
    

    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    if ((detailList == null) || (detailList.size() == 0)) {
      for (int i = 0; i < colTitle.size(); i++) {
        rowData.add("");
      }
    }
    for (int i = 0; i < detailList.size(); i++)
    {
      Model detail = (Model)detailList.get(i);
      String status = "";
      if (detail.getInt("status").intValue() == AioConstants.STATUS_DISABLE) {
        status = "停用";
      } else {
        status = "启用";
      }
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("fullName")));
      rowData.add(trimNull(detail.getStr("smallName")));
      rowData.add(trimNull(detail.getStr("spell")));
      rowData.add(trimNull(detail.getStr("memo")));
      rowData.add(trimNull(status));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
