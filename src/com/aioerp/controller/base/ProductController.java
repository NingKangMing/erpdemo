package com.aioerp.controller.base;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.ProductUnit;
import com.aioerp.model.base.Unit;
import com.aioerp.model.fz.PriceDiscountTrack;
import com.aioerp.model.sys.AioerpFile;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DwzUtils;
import com.aioerp.util.IO;
import com.aioerp.util.NumberUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ProductController
  extends BaseController
{
  private static String LIST_TABLE = "b_product_paging";
  
  public void list()
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    
    int supId = getParaToInt(0, getParaToInt("supId", Integer.valueOf(0))).intValue();
    Integer node = getParaToInt("node", Integer.valueOf(0));
    String actionType = getPara("actionType", "");
    if (actionType.equals("line")) {
      node = Integer.valueOf(AioConstants.NODE_1);
    }
    String searchPar1 = getPara("searchPar1");
    String searchValue1 = getPara("searchValue1");
    
    Map<String, Object> pageMap = null;
    int objectId = 0;
    int upObjectId = getParaToInt(1, Integer.valueOf(0)).intValue();
    if (upObjectId > 0)
    {
      pageMap = Product.dao.getPageSelectUpObject(configName, basePrivs(PRODUCT_PRIVS), LIST_TABLE, supId, upObjectId, pageNum, numPerPage, searchPar1, searchValue1, ORDER_FIELD, ORDER_DIRECTION);
      objectId = upObjectId;
    }
    else
    {
      String showLastPage = getPara("showLastPage");
      int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
      objectId = selectedObjectId;
      if ((showLastPage != null) && (showLastPage.equals("true"))) {
        pageMap = Product.dao.getLastPageByCondition(configName, basePrivs(PRODUCT_PRIVS), LIST_TABLE, supId, searchPar1, searchValue1, pageNum, numPerPage, ORDER_FIELD, ORDER_DIRECTION);
      } else {
        pageMap = Product.dao.getPageByCondtion(configName, basePrivs(PRODUCT_PRIVS), LIST_TABLE, supId, node, searchPar1, searchValue1, pageNum, numPerPage, orderField, orderDirection);
      }
    }
    int pSupId = 0;
    if (supId > 0) {
      pSupId = Product.dao.getPsupIdBySupId(configName, supId);
    }
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    setAttr("supId", Integer.valueOf(supId));
    setAttr("pSupId", Integer.valueOf(pSupId));
    setAttr("node", node);
    setAttr("searchPar1", searchPar1);
    setAttr("searchValue1", searchValue1);
    setAttr("pageMap", pageMap);
    setAttr("objectId", Integer.valueOf(objectId));
    

    columnConfig("b501");
    String whichRefresh = getPara("whichRefresh", "");
    if (whichRefresh.equals("navTabRefresh")) {
      render("page.html");
    } else {
      render("list.html");
    }
  }
  
  public void tree()
  {
    String configName = loginConfigName();
    List<Model> cats = Product.dao.getTreeAllList(configName, basePrivs(PRODUCT_PRIVS));
    Iterator<Model> iter = cats.iterator();
    List<HashMap<String, Object>> nodeList = new ArrayList();
    while (iter.hasNext())
    {
      Model product = (Model)iter.next();
      HashMap<String, Object> node = new HashMap();
      node.put("id", product.get("id"));
      node.put("pId", product.get("supId"));
      node.put("name", product.get("fullName"));
      node.put("url", "base/product/list/" + product.get("id"));
      nodeList.add(node);
    }
    HashMap<String, Object> node = new HashMap();
    node.put("id", Integer.valueOf(0));
    node.put("pId", Integer.valueOf(0));
    node.put("name", "商品信息");
    node.put("url", "base/product/list/0");
    nodeList.add(node);
    renderJson(nodeList);
  }
  
  public void add()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Product.class);
      
      Map<String, Object> vesionMap = ComFunController.vaildateOneBaseUnit(model);
      if (Integer.valueOf(vesionMap.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(vesionMap);
        return;
      }
      int numAutoAdd = getParaToInt("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0)).intValue();
      String code = model.getStr("code");
      if (Product.dao.existObjectByNum(configName, 0, code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson(this.result);
        return;
      }
      String barCode1 = model.getStr("barCode1");
      String barCode2 = model.getStr("barCode2");
      String barCode3 = model.getStr("barCode3");
      String[] strs = new String[3];
      strs[0] = barCode1;
      strs[1] = barCode2;
      strs[2] = barCode3;
      if (StringUtil.getEQStrs(strs))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "同种商品，条码不能重复，请重新设置！");
        renderJson(this.result);
        return;
      }
      if (!SysConfig.getConfig(configName, 8).booleanValue())
      {
        if (Product.dao.existBarCode(configName, barCode1, Integer.valueOf(0)))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode1 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
        if (Product.dao.existBarCode(configName, barCode2, Integer.valueOf(0)))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode2 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
        if (Product.dao.existBarCode(configName, barCode3, Integer.valueOf(0)))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode3 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
      }
      if ((model.getInt("status") != null) && (model.getInt("status").intValue() == 1)) {
        model.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
      } else {
        model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      }
      int inDefaultUnit = getParaToInt("inDefaultUnit", Integer.valueOf(1)).intValue();
      int stockDefaultUnit = getParaToInt("stockDefaultUnit", Integer.valueOf(1)).intValue();
      int outDefaultUnit = getParaToInt("outDefaultUnit", Integer.valueOf(1)).intValue();
      


      Map<String, Object> map = validateUnitRelation(model, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
      if (!String.valueOf(map.get("statusCode")).equals("200"))
      {
        this.result.put("statusCode", map.get("statusCode"));
        this.result.put("message", map.get("message"));
        renderJson(this.result);
        return;
      }
      String upids = "";
      int supId = model.getInt("supId").intValue();
      if (supId > 0) {
        upids = Product.dao.findById(configName, model.get("supId")).getStr("pids") + "{" + model.getInt("supId") + "}";
      } else {
        upids = "{0}";
      }
      model.set("node", Integer.valueOf(AioConstants.NODE_1));
      model.set("pids", upids);
      model.set("createTime", new Date());
      model.set("updateTime", new Date());
      model.set("unitRelation1", Integer.valueOf(1));
      model.set("unitId1", Integer.valueOf(1));
      model.set("unitId2", Integer.valueOf(2));
      model.set("unitId3", Integer.valueOf(3));
      setAssistUnit(model);
      model.set("userId", Integer.valueOf(loginUserId()));
      Integer sysImgId = model.getInt("sysImgId");
      if ((sysImgId != null) && (sysImgId.intValue() != 0)) {
        model.set("savePath", AioerpFile.dao.findById(configName, sysImgId).getStr("savePath"));
      } else {
        model.set("savePath", null);
      }
      Boolean flag = Boolean.valueOf(model.save(configName));
      updateBasePrivs(model.getInt("id"), model.getInt("supId"), PRODUCT_PRIVS);
      if (flag.booleanValue())
      {
        model.set("pids", model.get("pids") + "{" + model.getInt("id") + "}");
        model.set("rank", model.getInt("id")).update(configName);
        
        ProductUnit.dao.addProductUnit(configName, model, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
        

        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("rel", LIST_TABLE);
        
        this.result.put("base", getAttr("base"));
        

        String url = "/base/product/add/";
        String confirmType = getPara("confirmType");
        if ("dialogCloseReload".equals(confirmType))
        {
          url = "/base/product/optionAdd/";
          setAttr("supId", Integer.valueOf(supId));
        }
        if (numAutoAdd == AioConstants.NUMNOADD1)
        {
          code = NumberUtils.increase(code);
          
          this.result.put("loadDialogUrl", url + supId + "-" + URLEncoder.encode(code) + "-" + numAutoAdd);
        }
        else
        {
          this.result.put("loadDialogUrl", url + supId);
        }
        this.result.put("reset", "reset");
        this.result.put("dialogId", "b_product_id");
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
      int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
      setAttr("supId", Integer.valueOf(supId));
      String one = getPara(1);
      if ((one != null) && (!one.equals("")))
      {
        setAttr("code", URLDecoder.decode(one));
        setAttr("numAutoAdd", getParaToInt(2));
      }
      setAttr("toAction", "add");
      setAttr("toActionCall", "add");
      render("dialog.html");
    }
  }
  
  @Before({Tx.class})
  public void copyAdd()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Product.class);
      
      Map<String, Object> vesionMap = ComFunController.vaildateOneBaseUnit(model);
      if (Integer.valueOf(vesionMap.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(vesionMap);
        return;
      }
      int numAutoAdd = getParaToInt("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0)).intValue();
      String code = model.getStr("code");
      if (Product.dao.existObjectByNum(configName, 0, code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson(this.result);
        return;
      }
      String barCode1 = model.getStr("barCode1");
      String barCode2 = model.getStr("barCode2");
      String barCode3 = model.getStr("barCode3");
      String[] strs = new String[3];
      strs[0] = barCode1;
      strs[1] = barCode2;
      strs[2] = barCode3;
      if (StringUtil.getEQStrs(strs))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "同种商品，条码不能重复，请重新设置！");
        renderJson(this.result);
        return;
      }
      if (!SysConfig.getConfig(configName, 8).booleanValue())
      {
        if (Product.dao.existBarCode(configName, barCode1, Integer.valueOf(0)))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode1 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
        if (Product.dao.existBarCode(configName, barCode2, Integer.valueOf(0)))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode2 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
        if (Product.dao.existBarCode(configName, barCode3, Integer.valueOf(0)))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode3 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
      }
      int inDefaultUnit = getParaToInt("inDefaultUnit", Integer.valueOf(1)).intValue();
      int stockDefaultUnit = getParaToInt("stockDefaultUnit", Integer.valueOf(1)).intValue();
      int outDefaultUnit = getParaToInt("outDefaultUnit", Integer.valueOf(1)).intValue();
      

      Map<String, Object> map = validateUnitRelation(model, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
      if (!String.valueOf(map.get("statusCode")).equals("200"))
      {
        this.result.put("statusCode", map.get("statusCode"));
        this.result.put("message", map.get("message"));
        renderJson(this.result);
        return;
      }
      Model oldModel = Product.dao.findById(configName, model.getInt("id"));
      model.set("supId", oldModel.get("supId"));
      String oldPids = (String)oldModel.get("pids");
      model.set("pids", oldPids.subSequence(0, oldPids.lastIndexOf("{")));
      model.set("createTime", new Date());
      model.set("updateTime", new Date());
      if ((model.getInt("status") != null) && (model.getInt("status").intValue() == 1)) {
        model.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
      } else {
        model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      }
      model.set("node", Integer.valueOf(AioConstants.NODE_1));
      model.set("createTime", new Date());
      model.set("updateTime", new Date());
      model.set("id", null);
      model.set("unitRelation1", Integer.valueOf(1));
      model.set("unitId1", Integer.valueOf(1));
      model.set("unitId2", Integer.valueOf(2));
      model.set("unitId3", Integer.valueOf(3));
      Integer sysImgId = model.getInt("sysImgId");
      if ((sysImgId != null) && (sysImgId.intValue() != 0)) {
        model.set("savePath", AioerpFile.dao.findById(configName, sysImgId).getStr("savePath"));
      } else {
        model.set("savePath", null);
      }
      model.set("userId", Integer.valueOf(loginUserId()));
      setAssistUnit(model);
      











      Boolean flag = Boolean.valueOf(model.save(configName));
      updateBasePrivs(model.getInt("id"), model.getInt("supId"), PRODUCT_PRIVS);
      if (flag.booleanValue())
      {
        model.set("pids", model.get("pids") + "{" + model.getInt("id") + "}");
        model.set("rank", model.getInt("id")).update(configName);
        
        ProductUnit.dao.addProductUnit(configName, model, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("rel", LIST_TABLE);
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
      Product product = (Product)Product.dao.findById(configName, getParaToInt(0, Integer.valueOf(0)));
      setAttr("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0));
      setAttr("toAction", "copyAdd");
      setAttr("toActionCall", "copyAdd");
      setAttr("product", product);
      render("dialog.html");
    }
  }
  
  public void edit()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Product.class);
      
      Map<String, Object> vesionMap = ComFunController.vaildateOneBaseUnit(model);
      if (Integer.valueOf(vesionMap.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(vesionMap);
        return;
      }
      int numAutoAdd = getParaToInt("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0)).intValue();
      String code = model.getStr("code");
      if (Product.dao.existObjectByNum(configName, model.getInt("id").intValue(), code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson();
        return;
      }
      String barCode1 = model.getStr("barCode1");
      String barCode2 = model.getStr("barCode2");
      String barCode3 = model.getStr("barCode3");
      String[] strs = new String[3];
      strs[0] = barCode1;
      strs[1] = barCode2;
      strs[2] = barCode3;
      if (StringUtil.getEQStrs(strs))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "同种商品，条码不能重复，请重新设置！");
        renderJson(this.result);
        return;
      }
      if (!SysConfig.getConfig(configName, 8).booleanValue())
      {
        if (Product.dao.existBarCode(configName, barCode1, model.getInt("id")))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode1 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
        if (Product.dao.existBarCode(configName, barCode2, model.getInt("id")))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode2 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
        if (Product.dao.existBarCode(configName, barCode3, model.getInt("id")))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode3 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
      }
      int inDefaultUnit = getParaToInt("inDefaultUnit", Integer.valueOf(1)).intValue();
      int stockDefaultUnit = getParaToInt("stockDefaultUnit", Integer.valueOf(1)).intValue();
      int outDefaultUnit = getParaToInt("outDefaultUnit", Integer.valueOf(1)).intValue();
      
      Map<String, Object> map = validateUnitRelation(model, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
      if (!String.valueOf(map.get("statusCode")).equals("200"))
      {
        this.result.put("statusCode", map.get("statusCode"));
        this.result.put("message", map.get("message"));
        renderJson(this.result);
        return;
      }
      if ((model.getInt("status") != null) && (model.getInt("status").intValue() == 1)) {
        model.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
      } else {
        model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      }
      Product oldModel = (Product)Product.dao.findById(configName, model.get("id"));
      model.set("node", oldModel.get("node"));
      model.set("supId", oldModel.get("supId"));
      model.set("rank", oldModel.get("rank"));
      model.set("pids", oldModel.get("pids"));
      model.set("createTime", oldModel.get("createTime"));
      model.set("updateTime", new Date());
      model.set("unitRelation1", Integer.valueOf(1));
      model.set("unitId1", oldModel.get("unitId1"));
      model.set("unitId2", oldModel.get("unitId2"));
      model.set("unitId3", oldModel.get("unitId3"));
      setAssistUnit(model);
      Integer sysImgId = model.getInt("sysImgId");
      if ((sysImgId != null) && (sysImgId.intValue() != 0))
      {
        model.set("savePath", AioerpFile.dao.findById(configName, sysImgId).getStr("savePath"));
      }
      else
      {
        model.set("savePath", null);
        String path = oldModel.getStr("savePath");
        if ((path != null) && (!path.equals(""))) {
          IO.deleteFile(new File(IO.getWebrootPath() + path));
        }
      }
      model.set("userId", Integer.valueOf(loginUserId()));
      Boolean flag = Boolean.valueOf(model.update(configName));
      
      ProductUnit.dao.updateProductUnit(configName, model, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
      if (flag.booleanValue())
      {
        if (numAutoAdd == AioConstants.NUMNOADD1) {
          setAttr("code", NumberUtils.increase(model.getStr("code")));
        }
        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("callbackType", "closeCurrent");
        this.result.put("rel", LIST_TABLE);
        HashMap<String, Object> node = new HashMap();
        node.put("id", model.get("id"));
        node.put("name", model.get("name"));
        this.result.put("node", node);
        this.result.put("trel", "b_product_treeId");
        this.result.put("url", "base/product/list/" + model.get("supId"));
        
        this.result.put("selectedObjectId", model.get("id"));
      }
      renderJson(this.result);
    }
    else
    {
      int productId = getParaToInt(0, Integer.valueOf(0)).intValue();
      Product product = (Product)Product.dao.findById(configName, Integer.valueOf(productId));
      boolean verify = Product.dao.verify(configName, "productId", Integer.valueOf(productId));
      if (verify) {
        setAttr("isRead", "true");
      } else {
        setAttr("isRead", "false");
      }
      Map<String, Integer> map = ProductUnit.dao.getDefaultUnit(configName, productId);
      setAttr("inDefaultUnit", map.get("inDefaultUnit"));
      setAttr("stockDefaultUnit", map.get("stockDefaultUnit"));
      setAttr("outDefaultUnit", map.get("outDefaultUnit"));
      
      setAttr("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0));
      setAttr("toAction", "edit");
      setAttr("toActionCall", "edit");
      setAttr("product", product);
      render("dialog.html");
    }
  }
  
  @Before({Tx.class})
  public void delete()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    if (Product.dao.hasChildBySupId(configName, basePrivs(PRODUCT_PRIVS), id))
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "删除失败，请先删除下级!");
      renderJson(this.result);
      return;
    }
    boolean verify = Product.dao.verify(configName, "productId", Integer.valueOf(id));
    if (verify)
    {
      returnToPage(AioConstants.HTTP_RETURN300, "数据存在引用，不能删除!", "", "", "", "");
      renderJson();
      return;
    }
    Model<Product> product = Product.dao.findById(configName, Integer.valueOf(id));
    int supId = ((Integer)product.get("supId", Integer.valueOf(0))).intValue();
    if (Product.dao.deleteById(configName, Integer.valueOf(id)))
    {
      this.result.put("statusCode", Integer.valueOf(200));
      this.result.put("message", "");
      this.result.put("rel", LIST_TABLE);
    }
    ProductUnit.dao.delProductUnit(configName, id);
    if ((!Product.dao.hasChildBySupId(configName, basePrivs(PRODUCT_PRIVS), supId)) && (supId != 0))
    {
      Model<Product> pProduct = Product.dao.findById(configName, Integer.valueOf(supId));
      ((Product)pProduct.set("node", Integer.valueOf(AioConstants.NODE_1))).update(configName);
      this.result.put("trel", "b_product_treeId");
      
      HashMap<String, Object> node = new HashMap();
      node.put("id", pProduct.get("id"));
      node.put("pId", pProduct.get("supId"));
      this.result.put("pnode", node);
      
      this.result.put("rel", LIST_TABLE);
      this.result.put("url", "base/product/list/" + pProduct.get("supId") + "-" + supId);
      this.result.put("selectedObjectId", pProduct.get("id"));
    }
    renderJson(this.result);
  }
  
  public void validateProduct()
  {
    String configName = loginConfigName();
    int id = getParaToInt("id", Integer.valueOf(0)).intValue();
    boolean verify = Product.dao.verify(configName, "productId", Integer.valueOf(id));
    if (verify)
    {
      this.result.put("statusCode", Integer.valueOf(300));
      this.result.put("message", "该编号已经存在！");
    }
    else
    {
      this.result.put("statusCode", Integer.valueOf(200));
    }
    renderJson(this.result);
  }
  
  @Before({Tx.class})
  public void sort()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      Model model = (Model)getModel(Product.class);
      
      Map<String, Object> vesionMap = ComFunController.vaildateOneBaseUnit(model);
      if (Integer.valueOf(vesionMap.get("statusCode").toString()).intValue() != 200)
      {
        renderJson(vesionMap);
        return;
      }
      String code = model.getStr("code");
      if (Product.dao.existObjectByNum(configName, 0, code))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "该编号已经存在！");
        renderJson(this.result);
        return;
      }
      String barCode1 = model.getStr("barCode1");
      String barCode2 = model.getStr("barCode2");
      String barCode3 = model.getStr("barCode3");
      String[] strs = new String[3];
      strs[0] = barCode1;
      strs[1] = barCode2;
      strs[2] = barCode3;
      if (StringUtil.getEQStrs(strs))
      {
        this.result.put("statusCode", Integer.valueOf(300));
        this.result.put("message", "同种商品，条码不能重复，请重新设置！");
        renderJson(this.result);
        return;
      }
      if (!SysConfig.getConfig(configName, 8).booleanValue())
      {
        if (Product.dao.existBarCode(configName, barCode1, Integer.valueOf(0)))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode1 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
        if (Product.dao.existBarCode(configName, barCode2, Integer.valueOf(0)))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode2 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
        if (Product.dao.existBarCode(configName, barCode3, Integer.valueOf(0)))
        {
          this.result.put("statusCode", Integer.valueOf(300));
          this.result.put("message", "商品条码【" + barCode3 + "】不能重复，请重新设置！");
          renderJson(this.result);
          return;
        }
      }
      int inDefaultUnit = getParaToInt("inDefaultUnit", Integer.valueOf(1)).intValue();
      int stockDefaultUnit = getParaToInt("stockDefaultUnit", Integer.valueOf(1)).intValue();
      int outDefaultUnit = getParaToInt("outDefaultUnit", Integer.valueOf(1)).intValue();
      
      Map<String, Object> map = validateUnitRelation(model, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
      if (!String.valueOf(map.get("statusCode")).equals("200"))
      {
        this.result.put("statusCode", map.get("statusCode"));
        this.result.put("message", map.get("message"));
        renderJson(this.result);
        return;
      }
      if ((model.getInt("status") != null) && (model.getInt("status").intValue() == 1)) {
        model.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE));
      } else {
        model.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
      }
      model.set("node", Integer.valueOf(AioConstants.NODE_1));
      
      Product pProduct = (Product)Product.dao.findById(configName, model.getInt("id"));
      
      model.set("supId", pProduct.getInt("id"));
      model.set("pids", pProduct.getStr("pids"));
      model.set("createTime", new Date());
      model.set("updateTime", new Date());
      model.set("id", null);
      model.set("unitRelation1", Integer.valueOf(1));
      model.set("unitId1", Integer.valueOf(1));
      model.set("unitId2", Integer.valueOf(2));
      model.set("unitId3", Integer.valueOf(3));
      setAssistUnit(model);
      model.set("userId", Integer.valueOf(loginUserId()));
      Boolean flag = Boolean.valueOf(model.save(configName));
      updateBasePrivs(model.getInt("id"), model.getInt("supId"), PRODUCT_PRIVS);
      if (flag.booleanValue())
      {
        model.set("pids", model.get("pids") + "{" + model.getInt("id") + "}");
        model.set("rank", model.getInt("id")).update(configName);
        
        pProduct.set("node", Integer.valueOf(AioConstants.NODE_2)).update(configName);
        
        ProductUnit.dao.addProductUnit(configName, model, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
        



        this.result.put("statusCode", Integer.valueOf(200));
        this.result.put("message", "");
        this.result.put("callbackType", "closeCurrent");
        this.result.put("rel", LIST_TABLE);
        this.result.put("target", "ajax");
        this.result.put("trel", "b_product_treeId");
        this.result.put("url", "base/product/list/" + pProduct.get("id"));
        HashMap<String, Object> node = new HashMap();
        node.put("id", pProduct.get("id"));
        node.put("pId", pProduct.get("supId"));
        node.put("name", pProduct.get("fullName"));
        node.put("url", "base/product/list/" + pProduct.get("id"));
        this.result.put("pnode", node);
        
        this.result.put("opterate", "showLastPage");
        this.result.put("selectedObjectId", model.get("id"));
      }
      renderJson(this.result);
    }
    else
    {
      Product product = (Product)Product.dao.findById(configName, getParaToInt(0, Integer.valueOf(0)));
      
      setAttr("numAutoAdd", Integer.valueOf(AioConstants.NUMADD0));
      setAttr("supId", product.getInt("id"));
      setAttr("toAction", "sort");
      setAttr("toActionCall", "sort");
      setAttr("product", product);
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
        Product.dao.findById(configName, ids[i]).set("rank", seqs[i]).update(configName);
      }
    }
    returnToPage("", "", "", "", "", "");
    renderJson();
  }
  
  public void disableOrEnable()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    Product product = (Product)Product.dao.findById(configName, Integer.valueOf(id));
    Boolean flag = Boolean.valueOf(false);
    if (product.getInt("status").intValue() == AioConstants.STATUS_DISABLE) {
      flag = Boolean.valueOf(product.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE)).update(configName));
    } else {
      flag = Boolean.valueOf(product.set("status", Integer.valueOf(AioConstants.STATUS_DISABLE)).update(configName));
    }
    if (flag.booleanValue())
    {
      this.result.put("id", Integer.valueOf(id));
      this.result.put("status", product.getInt("status"));
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
      render("filter.html");
    }
  }
  
  public void filterData()
  {
    String configName = loginConfigName();
    



    String searchPar1 = getPara("searchPar1");
    String searchValue1 = getPara("searchValue1");
    int supId = 0;
    int pageNum = 1;
    int numPerPage = getParaToInt("numPerPage").intValue();
    
    Map<String, Object> pageMap = Product.dao.getPageByCondtion(configName, basePrivs(PRODUCT_PRIVS), LIST_TABLE, supId, null, searchPar1, searchValue1, pageNum, numPerPage, ORDER_FIELD, ORDER_DIRECTION);
    
    setAttr("pageMap", pageMap);
    setAttr("pSupId", Integer.valueOf(0));
    setAttr("supId", Integer.valueOf(supId));
    



    setAttr("searchPar1", searchPar1);
    setAttr("searchValue1", searchValue1);
    

    columnConfig("b501");
    render("page.html");
  }
  
  public Map<String, Object> validateUnitRelation(Model model, int inDefaultUnit, int stockDefaultUnit, int outDefaultUnit)
  {
    Map<String, Object> map = new HashMap();
    map.put("statusCode", Integer.valueOf(200));
    
    String calculateUnit2 = model.getStr("calculateUnit2");
    String calculateUnit3 = model.getStr("calculateUnit3");
    BigDecimal unitRelation2 = model.getBigDecimal("unitRelation2");
    BigDecimal unitRelation3 = model.getBigDecimal("unitRelation3");
    if ((calculateUnit2 != null) && (!calculateUnit2.equals("")))
    {
      if (BigDecimalUtils.compare(unitRelation2, BigDecimal.ZERO) != 0)
      {
        if ((calculateUnit3 != null) && (!calculateUnit3.equals("")))
        {
          if (BigDecimalUtils.compare(unitRelation3, BigDecimal.ZERO) == 0)
          {
            map.put("statusCode", Integer.valueOf(300));
            map.put("message", "【辅助单位2】单位关系不能为空！");
            return map;
          }
        }
        else if (BigDecimalUtils.compare(unitRelation3, BigDecimal.ZERO) != 0)
        {
          map.put("statusCode", Integer.valueOf(300));
          map.put("message", "【辅助单位2】单位名称不能为空！");
          return map;
        }
      }
      else
      {
        map.put("statusCode", Integer.valueOf(300));
        map.put("message", "【辅助单位1】单位关系不能为空！");
        return map;
      }
    }
    else
    {
      if (BigDecimalUtils.compare(unitRelation2, BigDecimal.ZERO) != 0)
      {
        map.put("statusCode", Integer.valueOf(300));
        map.put("message", "【辅助单位1】单位名称不能为空！");
        return map;
      }
      if (((calculateUnit3 != null) && (!calculateUnit3.equals(""))) || (BigDecimalUtils.compare(unitRelation3, BigDecimal.ZERO) != 0))
      {
        map.put("statusCode", Integer.valueOf(300));
        map.put("message", "请和先填写【辅助单位1】的信息！");
        return map;
      }
    }
    if (BigDecimalUtils.compare(unitRelation3, BigDecimal.ZERO) == 0) {
      if (BigDecimalUtils.compare(unitRelation2, BigDecimal.ZERO) != 0)
      {
        if ((inDefaultUnit == 3) || (stockDefaultUnit == 3) || (outDefaultUnit == 3))
        {
          map.put("statusCode", Integer.valueOf(300));
          map.put("message", "【默认单位】不存在辅助单位2");
          return map;
        }
      }
      else
      {
        if ((inDefaultUnit == 3) || (stockDefaultUnit == 3) || (outDefaultUnit == 3))
        {
          map.put("statusCode", Integer.valueOf(300));
          map.put("message", "【默认单位】不存在辅助单位2");
          return map;
        }
        if ((inDefaultUnit == 2) || (stockDefaultUnit == 2) || (outDefaultUnit == 2))
        {
          map.put("statusCode", Integer.valueOf(300));
          map.put("message", "【默认单位】不存在辅助单位1");
          return map;
        }
      }
    }
    return map;
  }
  
  @Before({Tx.class})
  public void optionAdd()
    throws UnsupportedEncodingException
  {
    int supId = getParaToInt(0, Integer.valueOf(0)).intValue();
    setAttr("supId", Integer.valueOf(supId));
    String one = getPara(1);
    if ((one != null) && (!one.equals("")))
    {
      setAttr("code", URLDecoder.decode(one, "utf-8"));
      setAttr("numAutoAdd", getParaToInt(2));
    }
    setAttr("toAction", "add");
    setAttr("toActionCall", "optionAdd");
    setAttr("confirmType", "dialogCloseReload");
    
    render("dialog.html");
  }
  
  public void option()
  {
    String btnPattern = getPara("btnPattern", "");
    setAttr("btnPattern", btnPattern);
    String configName = loginConfigName();
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
    Map<String, Object> map = new HashMap();
    map.put("term", term);
    map.put("termVal", termVal);
    int id = 0;
    if (isPost())
    {
      Integer supId = getParaToInt(0, Integer.valueOf(0));
      id = getParaToInt(1, Integer.valueOf(0)).intValue();
      if (id == 0) {
        id = getParaToInt("supId", Integer.valueOf(0)).intValue();
      }
      if ((termVal == "") && (id == 0)) {
        setAttr("pageMap", Product.dao.search(configName, basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, orderField, orderDirection, map));
      } else {
        setAttr("pageMap", Product.dao.supIdSearch(configName, basePrivs(PRODUCT_PRIVS), id, pageNum, numPerPage, orderField, orderDirection, map));
      }
      setAttr("supId", Integer.valueOf(id));
      setAttr("pSupId", Integer.valueOf(Product.dao.getPsupId(configName, supId.intValue())));
    }
    else
    {
      id = getParaToInt(0, Integer.valueOf(0)).intValue();
      if (id > 0)
      {
        setAttr("pageMap", Product.dao.supIdSearch(configName, basePrivs(PRODUCT_PRIVS), id, pageNum, numPerPage, orderField, orderDirection, map));
        setAttr("pSupId", Integer.valueOf(Product.dao.getPsupId(configName, id)));
        setAttr("supId", Integer.valueOf(id));
      }
      else
      {
        setAttr("pageMap", Product.dao.getPageBySupId(configName, basePrivs(PRODUCT_PRIVS), pageNum, numPerPage, id, ""));
      }
    }
    setAttr("objectId", Integer.valueOf(id));
    render("option.html");
  }
  
  public void calculateUnit()
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    int objId = getParaToInt(1, Integer.valueOf(1)).intValue();
    setAttr("unitId", getParaToInt("unitId", Integer.valueOf(0)));
    setAttr("billType", getParaToInt("billType", Integer.valueOf(0)));
    setAttr("productId", Integer.valueOf(id));
    Model prd = Product.dao.getObjectAttrById(configName, "select calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3 ", id);
    if (prd == null)
    {
      setAttr("calculateUnit1", "");
      setAttr("calculateUnit2", "");
      setAttr("calculateUnit3", "");
      setAttr("unitRelation1", "");
      setAttr("unitRelation2", "");
      setAttr("unitRelation3", "");
      setAttr("retailPrice1", "");
      setAttr("retailPrice2", "");
      setAttr("retailPrice3", "");
    }
    else
    {
      setAttr("calculateUnit1", prd.get("calculateUnit1"));
      setAttr("calculateUnit2", prd.get("calculateUnit2"));
      setAttr("calculateUnit3", prd.get("calculateUnit3"));
      setAttr("unitRelation1", prd.get("unitRelation1"));
      setAttr("unitRelation2", prd.get("unitRelation2"));
      setAttr("unitRelation3", prd.get("unitRelation3"));
      setAttr("retailPrice1", prd.get("retailPrice1"));
      setAttr("retailPrice2", prd.get("retailPrice2"));
      setAttr("retailPrice3", prd.get("retailPrice3"));
    }
    setAttr("objId", Integer.valueOf(objId));
    render("calculate_unit.html");
  }
  
  public void calculateUnitCall()
  {
    String configName = loginConfigName();
    int productId = getParaToInt("productId", Integer.valueOf(0)).intValue();
    int selectUnitId = getParaToInt("selectUnitId", Integer.valueOf(1)).intValue();
    int unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
    int billType = getParaToInt("billType", Integer.valueOf(AioConstants.BILL_TYPE_XS)).intValue();
    if (unitId == 0)
    {
      this.result.put("price", Integer.valueOf(0));
      this.result.put("discount", Integer.valueOf(1));
      renderJson(this.result);
      return;
    }
    BigDecimal price = BigDecimal.ZERO;
    BigDecimal discount = new BigDecimal(1);
    boolean priceTrack = false;
    boolean discountTrack = false;
    if (billType == AioConstants.BILL_TYPE_JH)
    {
      priceTrack = SysConfig.getConfig(configName, 4).booleanValue();
      discountTrack = SysConfig.getConfig(configName, 13).booleanValue();
    }
    else if (billType == AioConstants.BILL_TYPE_XS)
    {
      priceTrack = SysConfig.getConfig(configName, 5).booleanValue();
      discountTrack = SysConfig.getConfig(configName, 14).booleanValue();
    }
    if (discountTrack)
    {
      PriceDiscountTrack priceDiscountTrack = PriceDiscountTrack.dao.getPriceTrackObj(configName, productId, selectUnitId, billType);
      if (priceDiscountTrack != null) {
        if (billType == AioConstants.BILL_TYPE_JH) {
          discount = priceDiscountTrack.getBigDecimal("lastCostDiscouont");
        } else if (billType == AioConstants.BILL_TYPE_XS) {
          discount = priceDiscountTrack.getBigDecimal("lastSellDiscouont");
        }
      }
    }
    if (priceTrack)
    {
      PriceDiscountTrack priceDiscountTrack = PriceDiscountTrack.dao.getPriceTrackObj(configName, productId, selectUnitId, billType);
      if (priceDiscountTrack != null)
      {
        if (billType == AioConstants.BILL_TYPE_JH) {
          price = priceDiscountTrack.getBigDecimal("lastCostPrice");
        } else if (billType == AioConstants.BILL_TYPE_XS) {
          price = priceDiscountTrack.getBigDecimal("lastSellPrice");
        }
      }
      else {
        priceTrack = false;
      }
    }
    if (!priceTrack)
    {
      Model unit = Unit.dao.findById(configName, Integer.valueOf(unitId));
      if ((unit != null) && (unit.getInt("fitPrice") != null) && (unit.getInt("fitPrice").intValue() != 0))
      {
        Model productUnit = ProductUnit.dao.getObj(configName, productId, selectUnitId);
        if (productUnit != null)
        {
          int fitPrice = unit.getInt("fitPrice").intValue();
          if (fitPrice == 1) {
            price = productUnit.getBigDecimal("retailPrice");
          } else if (fitPrice == 2) {
            price = productUnit.getBigDecimal("defaultPrice1");
          } else if (fitPrice == 3) {
            price = productUnit.getBigDecimal("defaultPrice2");
          } else if (fitPrice == 4) {
            price = productUnit.getBigDecimal("defaultPrice3");
          }
        }
      }
    }
    this.result.put("price", price);
    this.result.put("discount", discount);
    renderJson(this.result);
  }
  
  public void recordAmount()
    throws Exception
  {
    String configName = loginConfigName();
    int id = getParaToInt(0, Integer.valueOf(0)).intValue();
    String amount = getPara("amount", "0");
    BigDecimal objVal;
    try
    {
      objVal = new BigDecimal(amount);
    }
    catch (Exception e)
    {
      
      objVal = new BigDecimal(0);
    }
    String selectUnitId = URLDecoder.decode(getPara(1, "1"), "utf-8");
    

    id = getParaToInt("productId", getParaToInt(0, Integer.valueOf(0))).intValue();
    


    Model prd = Product.dao.getObjectAttrById(configName, "select calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3 ", id);
    if (prd == null)
    {
      setAttr("calculateUnit1", "");
      setAttr("calculateUnit2", "");
      setAttr("calculateUnit3", "");
      setAttr("unitRelation1", "");
      setAttr("unitRelation2", "");
      setAttr("unitRelation3", "");
    }
    else
    {
      int unitRelation1 = Integer.valueOf(prd.get("unitRelation1", "0").toString()).intValue();
      BigDecimal unitRelation2 = new BigDecimal(prd.get("unitRelation2", "0").toString());
      BigDecimal unitRelation3 = new BigDecimal(prd.get("unitRelation3", "0").toString());
      String calculateUnit1 = (String)prd.get("calculateUnit1");
      String calculateUnit2 = (String)prd.get("calculateUnit2");
      String calculateUnit3 = (String)prd.get("calculateUnit3");
      

      objVal = DwzUtils.getConversionAmount(objVal, Integer.valueOf(selectUnitId), Integer.valueOf(unitRelation1), unitRelation2, unitRelation3, Integer.valueOf(1));
      if ((BigDecimalUtils.compare(objVal, unitRelation3) != -1) && (BigDecimalUtils.compare(unitRelation3, BigDecimal.ZERO) == 1))
      {
        setAttr("i3", Integer.valueOf(BigDecimalUtils.divInt(objVal, unitRelation3)));
        objVal = BigDecimalUtils.model(objVal, unitRelation3);
      }
      if ((BigDecimalUtils.compare(objVal, unitRelation2) != -1) && (BigDecimalUtils.compare(unitRelation2, BigDecimal.ZERO) == 1))
      {
        setAttr("i2", Integer.valueOf(BigDecimalUtils.divInt(objVal, unitRelation2)));
        objVal = BigDecimalUtils.model(objVal, unitRelation2);
      }
      setAttr("i1", objVal);
      setAttr("calculateUnit1", calculateUnit1);
      setAttr("calculateUnit2", calculateUnit2);
      setAttr("calculateUnit3", calculateUnit3);
      setAttr("unitRelation1", unitRelation1 == 0 ? "" : Integer.valueOf(unitRelation1));
      setAttr("unitRelation2", BigDecimalUtils.notNullZero(unitRelation2) ? unitRelation2 : "");
      setAttr("unitRelation3", BigDecimalUtils.notNullZero(unitRelation3) ? unitRelation3 : "");
    }
    render("record_amount.html");
  }
  
  public Model setAssistUnit(Model model)
  {
    try
    {
      String calculateUnit1 = (String)model.get("calculateUnit1");
      String calculateUnit2 = (String)model.get("calculateUnit2");
      String calculateUnit3 = (String)model.get("calculateUnit3");
      int unitRelation1 = model.getInt("unitRelation1").intValue();
      BigDecimal unitRelation2 = model.getBigDecimal("unitRelation2");
      BigDecimal unitRelation3 = model.getBigDecimal("unitRelation3");
      model.set("assistUnit", DwzUtils.helpUnit(calculateUnit1, calculateUnit2, calculateUnit3, Integer.valueOf(unitRelation1), unitRelation2, unitRelation3));
    }
    catch (Exception e)
    {
      return model;
    }
    return model;
  }
  
  public void print()
  {
    String configName = loginConfigName();
    
    Map<String, Object> data = new HashMap();
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "商品信息");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    List<String> rowData = new ArrayList();
    List<Model> detailList = new ArrayList();
    

    int supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    int pageNum = AioConstants.START_PAGE;
    int numPerPage = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    String orderField = getPara("orderField", ORDER_FIELD);
    String orderDirection = getPara("orderDirection", ORDER_DIRECTION);
    String searchPar1 = getPara("searchPar1");
    String searchValue1 = getPara("searchValue1");
    int node = getParaToInt("node", Integer.valueOf(0)).intValue();
    Map<String, Object> pageMap = null;
    pageMap = Product.dao.getPageByCondtion(configName, basePrivs(PRODUCT_PRIVS), LIST_TABLE, supId, Integer.valueOf(node), searchPar1, searchValue1, pageNum, numPerPage, orderField, orderDirection);
    if (pageMap != null) {
      detailList = (List)pageMap.get("pageList");
    }
    printCommonData(headData);
    

    colTitle.add("行号");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("辅助单位");
    colTitle.add("条码");
    colTitle.add("基本单位名称 ");
    colTitle.add("零售价");
    colTitle.add("预设售价1");
    colTitle.add("预设售价2");
    colTitle.add("预设售价3");
    colTitle.add("商品备注");
    colTitle.add("状态");
    colTitle.add("成本算法");
    colTitle.add("有效期(天)");
    

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
      String costArithStr = "移动加权平均";
      if (detail.getInt("costArith").intValue() == AioConstants.PRD_COST_PRICE4) {
        costArithStr = "手工指定法";
      }
      rowData.add(trimNull(i + 1));
      rowData.add(trimNull(detail.getStr("code")));
      rowData.add(trimNull(detail.getStr("fullName")));
      rowData.add(trimNull(detail.getStr("smallName")));
      rowData.add(trimNull(detail.getStr("spell")));
      rowData.add(trimNull(detail.getStr("standard")));
      rowData.add(trimNull(detail.getStr("model")));
      rowData.add(trimNull(detail.getStr("field")));
      rowData.add(trimNull(detail.getStr("assistUnit")));
      rowData.add(trimNull(detail.getStr("barCode1")));
      rowData.add(trimNull(detail.getStr("calculateUnit1")));
      rowData.add(trimNull(detail.getBigDecimal("retailPrice1")));
      rowData.add(trimNull(detail.getBigDecimal("defaultPrice11")));
      rowData.add(trimNull(detail.getBigDecimal("defaultPrice12")));
      rowData.add(trimNull(detail.getBigDecimal("defaultPrice13")));
      rowData.add(trimNull(detail.getStr("memo")));
      rowData.add(trimNull(status));
      rowData.add(trimNull(costArithStr));
      rowData.add(trimNull(detail.getInt("validity")));
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    renderJson(data);
  }
}
