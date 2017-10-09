package com.aioerp.controller.fz;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.HelpUtil;
import com.aioerp.model.base.Product;
import com.aioerp.model.fz.ProduceTemplate;
import com.aioerp.model.fz.ProduceTemplateDetail;
import com.aioerp.model.stock.Stock;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.NumberUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ModelUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProduceTemplateController
  extends BaseController
{
  public static String listID = "produceTemplateList";
  
  public void index()
    throws SQLException, Exception
  {
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "tmp.id");
    String orderDirection = getPara("orderDirection", "asc");
    
    Map<String, Object> map = ProduceTemplate.dao.getListByParam(loginConfigName(), basePrivs(PRODUCT_PRIVS), listID, pageNum, numPerPage, orderField, orderDirection);
    

    columnConfig("fz501");
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    render("page.html");
  }
  
  public void list()
    throws SQLException, Exception
  {
    String configName = loginConfigName();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    String orderField = getPara("orderField", "tmp.id");
    String orderDirection = getPara("orderDirection", "asc");
    
    String showLastPage = getPara("showLastPage");
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    

    Map<String, Object> map = new HashMap();
    if ((showLastPage != null) && (showLastPage.equals("true"))) {
      map = ProduceTemplate.dao.getLastPage(configName, listID, pageNum, numPerPage, orderField, orderDirection);
    } else {
      map = ProduceTemplate.dao.getListByParam(configName, basePrivs(PRODUCT_PRIVS), listID, pageNum, numPerPage, orderField, orderDirection);
    }
    columnConfig("fz501");
    
    setAttr("pageMap", map);
    setAttr("objectId", Integer.valueOf(selectedObjectId));
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    render("list.html");
  }
  
  @Before({Tx.class})
  public void add()
  {
    String configName = loginConfigName();
    if (isPost())
    {
      ProduceTemplate produceTemplate = (ProduceTemplate)getModel(ProduceTemplate.class, "produceTmp");
      String tmpName = produceTemplate.getStr("tmpName");
      String copyTmpName = getPara("copyTmpName");
      if ((copyTmpName != null) && (!"".equals(copyTmpName))) {
        tmpName = copyTmpName;
      }
      Integer productId = getParaToInt("product.id");
      if ("".equals(tmpName))
      {
        setAttr("message", "没有指定模板名称!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if ((productId == null) || (productId.intValue() == 0))
      {
        setAttr("message", "没有指定生产商品!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      boolean hasOther = ProduceTemplate.dao.nameIsExist(configName, tmpName, 0);
      if (hasOther)
      {
        setAttr("message", "模板名称重复，请重新指定!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      List<ProduceTemplateDetail> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ProduceTemplateDetail.class, "produceTemplatetDetail");
      
      List<HelpUtil> uitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
      if (detailList.size() == 0)
      {
        setAttr("message", "没有指定配料信息!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      for (int i = 0; i < detailList.size(); i++)
      {
        ProduceTemplateDetail detail = (ProduceTemplateDetail)detailList.get(i);
        BigDecimal assortAmount = detail.getBigDecimal("assortAmount");
        
        int trIndex = StringUtil.strAddNumToInt(((HelpUtil)uitlList.get(i)).getTrIndex(), 1);
        if ((assortAmount == null) || (BigDecimalUtils.compare(assortAmount, BigDecimal.ZERO) != 1) || (BigDecimalUtils.compare(assortAmount, new BigDecimal(1000000)) != -1))
        {
          setAttr("message", "第" + trIndex + "行配套数量必须大于0且小于1000000!");
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
      }
      produceTemplate.remove("id");
      produceTemplate.set("tmpName", tmpName);
      produceTemplate.set("productId", productId);
      produceTemplate.save(configName);
      for (int i = 0; i < detailList.size(); i++)
      {
        ProduceTemplateDetail detail = (ProduceTemplateDetail)detailList.get(i);
        detail.remove("id");
        detail.set("tmpId", produceTemplate.getInt("id"));
        detail.save(configName);
      }
      Map<String, Object> data = new HashMap();
      data.put("showLastPage", "true");
      data.put("selectedObjectId", produceTemplate.getInt("id"));
      setAttr("data", data);
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "produceTemplateList");
      renderJson();
    }
    else
    {
      setAttr("toAction", "add");
      render("dialog.html");
    }
  }
  
  @Before({Tx.class})
  public void edit()
  {
    String configName = loginConfigName();
    List<Integer> oldDetailIds;
    if (isPost())
    {
      ProduceTemplate tmp = (ProduceTemplate)getModel(ProduceTemplate.class, "produceTmp");
      String tmpName = tmp.getStr("tmpName");
      Integer productId = getParaToInt("product.id");
      Integer billId = tmp.getInt("id");
      if ("".equals(tmpName))
      {
        setAttr("message", "没有指定模板名称!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      if ((productId == null) || (productId.intValue() == 0))
      {
        setAttr("message", "没有指定生产商品!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      boolean hasOther = ProduceTemplate.dao.nameIsExist(configName, tmpName, billId.intValue());
      if (hasOther)
      {
        setAttr("message", "模板名称重复，请重新指定!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      List<ProduceTemplateDetail> detailList = ModelUtils.batchInjectSortObjModel(getRequest(), ProduceTemplateDetail.class, "produceTemplatetDetail");
      
      List<HelpUtil> uitlList = ModelUtils.batchInjectSortHelpModel(getRequest(), HelpUtil.class, "helpUitl");
      if (detailList.size() == 0)
      {
        setAttr("message", "没有指定配料信息!");
        setAttr("statusCode", AioConstants.HTTP_RETURN300);
        renderJson();
        return;
      }
      for (int i = 0; i < detailList.size(); i++)
      {
        ProduceTemplateDetail detail = (ProduceTemplateDetail)detailList.get(i);
        BigDecimal assortAmount = detail.getBigDecimal("assortAmount");
        
        int trIndex = StringUtil.strAddNumToInt(((HelpUtil)uitlList.get(i)).getTrIndex(), 1);
        if ((assortAmount == null) || (BigDecimalUtils.compare(assortAmount, BigDecimal.ZERO) != 1) || (BigDecimalUtils.compare(assortAmount, new BigDecimal(1000000)) != -1))
        {
          setAttr("message", "第" + trIndex + "行配套数量必须大于0且小于1000000!");
          setAttr("statusCode", AioConstants.HTTP_RETURN300);
          renderJson();
          return;
        }
      }
      tmp.set("productId", productId);
      tmp.update(configName);
      

      oldDetailIds = ProduceTemplateDetail.dao.getListByDetailId(configName, billId);
      
      List<Integer> newDetaiIds = new ArrayList();
      ProduceTemplateDetail detail;
      for (int i = 0; i < detailList.size(); i++)
      {
        detail = (ProduceTemplateDetail)detailList.get(i);
        Integer detailId = detail.getInt("id");
        detail.set("tmpId", tmp.getInt("id"));
        if (detailId == null)
        {
          detail.save(configName);
        }
        else
        {
          detail.update(configName);
          newDetaiIds.add(detailId);
        }
      }
      for (Integer id : oldDetailIds) {
        if (!newDetaiIds.contains(id)) {
          ProduceTemplateDetail.dao.deleteById(configName, id);
        }
      }
      Map<String, Object> data = new HashMap();
      data.put("selectedObjectId", tmp.getInt("id"));
      setAttr("data", data);
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("rel", "produceTemplateList");
      renderJson();
    }
    else
    {
      Integer tmpId = getParaToInt(0);
      setAttr("produceTmp", ProduceTemplate.dao.findById(configName, tmpId));
      List<Model> list = ProduceTemplateDetail.dao.getDetailsByTmpId(configName, tmpId);
      BigDecimal sckAmount = null;
      int[] num = new int[list.size()];
      
      int i = 0;
      for (Model model : list)
      {
        Integer productId = model.getInt("productId");
        BigDecimal assortAmount = model.getBigDecimal("assortAmount");
        sckAmount = Stock.dao.stockProdcutAmount(configName, productId.intValue());
        int allowAmount;
      
        if (assortAmount == null) {
          allowAmount = sckAmount.intValue();
        } else {
          allowAmount = BigDecimalUtils.divInt(sckAmount, assortAmount);
        }
        if (BigDecimalUtils.compare(sckAmount, BigDecimal.ZERO) == 0) {
          sckAmount = null;
        }
        num[i] = allowAmount;
        i++;
        model.put("sckAmount", sckAmount);
      }
      int allowAmount = NumberUtils.getMinRrcodeByArray(num);
      setAttr("allowAmount", Integer.valueOf(allowAmount));
      setAttr("detailList", list);
      render("dialogEdit.html");
    }
  }
  
  public void copy()
  {
    render("inputTmpName.html");
  }
  
  @Before({Tx.class})
  public void del()
  {
    String configName = loginConfigName();
    Integer tid = getParaToInt(0);
    if (tid.intValue() == 0)
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("message", "删除失败,数据不存在");
      renderJson();
      return;
    }
    Boolean flag = Boolean.valueOf(false);
    Integer row = ProduceTemplateDetail.dao.deleteDetails(configName, tid);
    if (row.intValue() >= 0) {
      flag = Boolean.valueOf(ProduceTemplate.dao.deleteById(configName, tid));
    }
    if (flag.booleanValue())
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN200);
      setAttr("navTabId", "porduceTemplateView");
    }
    else
    {
      setAttr("statusCode", AioConstants.HTTP_RETURN301);
    }
    renderJson();
  }
  
  public void dialogList()
    throws SQLException, Exception
  {
    String term = getPara("term", "");
    String termVal = getPara("termVal", "");
    String isAll = getPara("isAll", "");
    if ((term != "") && (termVal != "") && (isAll != ""))
    {
      dialogSearch();
      return;
    }
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    String orderField = getPara("orderField", "tmp.id");
    String orderDirection = getPara("orderDirection", "asc");
    int selectedObjectId = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    
    Map<String, Object> map = ProduceTemplate.dao.getListByParam(loginConfigName(), basePrivs(PRODUCT_PRIVS), "", pageNum, numPerPage, orderField, orderDirection);
    
    setAttr("pageMap", map);
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    

    setAttr("objectId", Integer.valueOf(selectedObjectId));
    setAttr("toAction", "dialogList");
    render("option.html");
  }
  
  public void dialogSearch()
    throws Exception
  {
    String orderField = getPara("orderField", "tmp.id");
    String orderDirection = getPara("orderDirection", "asc");
    setAttr("orderField", orderField);
    setAttr("orderDirection", orderDirection);
    
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = getParaToInt("numPerPage", Integer.valueOf(10)).intValue();
    
    int id = getParaToInt("selectedObjectId", Integer.valueOf(0)).intValue();
    Map<String, Object> map = new HashMap();
    String term = getPara("term");
    String termVal = getPara("termVal");
    map.put("term", term);
    map.put("termVal", termVal);
    
    Map<String, Object> pageMap = ProduceTemplate.dao.getPageDialogByTerms(loginConfigName(), pageNum, numPerPage, orderField, orderDirection, map);
    
    setAttr("pageMap", pageMap);
    setAttr("term", term);
    setAttr("termVal", termVal);
    setAttr("objectId", Integer.valueOf(id));
    setAttr("toAction", "dialogSearch");
    render("option.html");
  }
  
  public void optionCallBack()
  {
    String configName = loginConfigName();
    Integer tid = getParaToInt(0, Integer.valueOf(0));
    Integer amount = getParaToInt("amount");
    if ((amount == null) || (amount.intValue() < 1))
    {
      setAttr("msg", "没有输入数量或输入数量超出范围，请输入拆装数量！");
      renderJson();
      return;
    }
    ProduceTemplate tmp = (ProduceTemplate)ProduceTemplate.dao.findById(configName, tid);
    Integer productId = tmp.getInt("productId");
    Product product = Product.dao.findObjById(configName, productId);
    product.put("amount", amount);
    


    List<Model> details = ProduceTemplateDetail.dao.getDetailsByTmpId(configName, tid);
    for (int i = 0; i < details.size(); i++)
    {
      Model recode = (Model)details.get(i);
      BigDecimal assortAmount = recode.getBigDecimal("assortAmount");
      
      ((Model)details.get(i)).put("amount", BigDecimalUtils.mul(assortAmount, new BigDecimal(amount.intValue())));
    }
    setAttr("product", product);
    setAttr("productDetail", details);
    renderJson();
  }
  
  public void print()
    throws SQLException, Exception
  {
    Map<String, Object> data = new HashMap();
    
    String configName = loginConfigName();
    
    data.put("reportNo", Integer.valueOf(301));
    data.put("reportName", "生产模板");
    List<String> headData = new ArrayList();
    List<String> colTitle = new ArrayList();
    List<String> colWidth = new ArrayList();
    

    printCommonData(headData);
    
    colTitle.add("行号");
    colTitle.add("模板名称");
    colTitle.add("商品编号");
    colTitle.add("商品全名");
    colTitle.add("商品简名");
    colTitle.add("商品拼音码");
    colTitle.add("商品规格");
    colTitle.add("商品型号");
    colTitle.add("商品产地");
    colTitle.add("基本条码");
    colTitle.add("基本单位");
    colTitle.add("辅助单位");
    
    colWidth = StringUtil.printWidthRemoveNo(colTitle.size() - 1);
    int totalCount = getParaToInt("totalCount", Integer.valueOf(1)).intValue() == 0 ? 1 : getParaToInt("totalCount", Integer.valueOf(1)).intValue();
    List<String> rowData = new ArrayList();
    

    Integer pageNum = Integer.valueOf(1);
    Integer numPerPage = Integer.valueOf(totalCount);
    String orderField = getPara("orderField", "tmp.id");
    String orderDirection = getPara("orderDirection", "asc");
    
    Map<String, Object> pageMap = ProduceTemplate.dao.getListByParam(configName, basePrivs(PRODUCT_PRIVS), listID, pageNum.intValue(), numPerPage.intValue(), orderField, orderDirection);
    


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
      ProduceTemplate tmp = (ProduceTemplate)record.get("tmp");
      rowData.add(trimNull(tmp.get("tmpName")));
      Product pro = (Product)record.get("pro");
      rowData.add(trimNull(pro.get("code")));
      rowData.add(trimNull(pro.get("fullName")));
      rowData.add(trimNull(pro.get("smallName")));
      rowData.add(trimNull(pro.get("spell")));
      rowData.add(trimNull(pro.get("standard")));
      rowData.add(trimNull(pro.get("model")));
      rowData.add(trimNull(pro.get("field")));
      rowData.add(trimNull(pro.get("barCode1")));
      rowData.add(trimNull(pro.get("calculateUnit1")));
      rowData.add(trimNull(pro.get("assistUnit")));
      
      i++;
    }
    data.put("headData", headData);
    data.put("colTitle", colTitle);
    data.put("colWidth", colWidth);
    data.put("rowData", rowData);
    
    renderJson(data);
  }
}
