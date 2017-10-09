package com.aioerp.model.stock;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.ComFunController;
import com.aioerp.model.BaseDbModel;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.ProductUnit;
import com.aioerp.model.base.Storage;
import com.aioerp.model.fz.PriceDiscountTrack;
import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class Stock
  extends BaseDbModel
{
  public static final Stock dao = new Stock();
  public static final String TABLE_NAME = "cc_stock";
  Map<String, Class<? extends Model>> map = new HashMap();
  public StringBuffer selectSql;
  public StringBuffer fromSql;
  public ArrayList<Object> paras;
  
  public void init(String whichCallBack, Integer storageId, String privs, int unitId, String priceTail, String discountTail)
  {
    String attr = ProductUnit.dao.getDefaultUnitByBillTypeName(whichCallBack);
    
    this.selectSql = new StringBuffer();
    this.fromSql = new StringBuffer();
    this.map.put("product", Product.class);
    this.selectSql.append(" select product.*,");
    
    this.selectSql.append(" (select sum(st.amount) from b_product as p");
    this.selectSql.append(" left join cc_stock st on st.productId = p.id");
    this.selectSql.append(" where p.pids like concat(product.pids,'%') and p.node=1");
    if ((storageId != null) && (storageId.intValue() > 0)) {
      this.selectSql.append(" and st.storageId=" + storageId);
    }
    this.selectSql.append(" ) samount");
    

    this.selectSql.append(",case when product.costArith = 1 then ");
    this.selectSql.append(" (select avg(zj.avgPrice) from zj_product_avgprice as zj where zj.productId=product.id");
    if ((storageId != null) && (storageId.intValue() > 0)) {
      this.selectSql.append(" and zj.storageId=" + storageId);
    } else {
      this.selectSql.append(" and zj.storageId is null");
    }
    this.selectSql.append(" )");
    this.selectSql.append(" else null end as costPrice");
    
    this.fromSql.append(" from  b_product product ");
    if (StringUtils.isNotBlank(attr))
    {
      this.selectSql.append(",pu.selectUnitId");
      this.fromSql.append(" left join b_product_unit pu on pu.productId=product.id");
    }
    if ((StringUtils.isNotBlank(priceTail)) || (StringUtils.isNotBlank(discountTail))) {
      this.fromSql.append(" left join fz_pricediscount_track as track on product.id = track.productId and track.selectUnitId = pu." + attr + " and track.unitId =" + unitId);
    }
    if (StringUtils.isNotBlank(priceTail))
    {
      if ("purchasePrice".equals(priceTail)) {
        this.selectSql.append(",track.lastCostPrice as price");
      } else if ("sellPrice".equals(priceTail)) {
        this.selectSql.append(",track.lastSellPrice as price");
      } else if ("retailPrice1".equals(priceTail)) {
        this.selectSql.append(",product.retailPrice1 as price");
      } else if ("defaultPrice11".equals(priceTail)) {
        this.selectSql.append(",product.defaultPrice11 as price");
      } else if ("defaultPrice12".equals(priceTail)) {
        this.selectSql.append(",product.defaultPrice12 as price");
      } else if ("defaultPrice13".equals(priceTail)) {
        this.selectSql.append(",product.defaultPrice13 as price");
      } else {
        this.selectSql.append(",null as price");
      }
    }
    else {
      this.selectSql.append(",null as price");
    }
    if (StringUtils.isNotBlank(discountTail))
    {
      if ("purchaseDiscount".equals(discountTail)) {
        this.selectSql.append(",track.lastCostDiscouont as discount");
      } else if ("sellDiscount".equals(discountTail)) {
        this.selectSql.append(",track.lastSellDiscouont as discount");
      } else {
        this.selectSql.append(",1 as discount");
      }
    }
    else {
      this.selectSql.append(",1 as discount");
    }
    this.fromSql.append(" where product.status=" + AioConstants.STATUS_ENABLE);
    if (StringUtils.isNotBlank(attr)) {
      this.fromSql.append(" and pu." + attr + " is not null");
    }
    this.fromSql = ComFunController.basePrivsSql(this.fromSql, privs, "product");
  }
  
  public void initStoragePrd(int productId)
  {
    this.selectSql = new StringBuffer();
    this.fromSql = new StringBuffer();
    this.map.put("stock", Stock.class);
    this.map.put("product", Product.class);
    this.map.put("storage", Storage.class);
    this.selectSql.append(" select sum(stock.amount) samount,storage.*,product.*");
    this.fromSql.append(" from (select * from cc_stock where productId=" + productId + ") stock");
    this.fromSql.append(" right join b_storage storage on storage.id = stock.storageId");
    this.fromSql.append(" left join b_product product on product.id=stock.productId");
    this.fromSql.append(" where  storage.status=" + AioConstants.STATUS_ENABLE);
  }
  
  public Map<String, Object> storageAllPrdStockSearch(String configName, String opertType, int productId, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
    throws SQLException, Exception
  {
    initStoragePrd(productId);
    if ((pars.size() == 0) || ((pars.containsKey("termVal")) && (pars.get("termVal").equals("")))) {
      opertType = "page";
    }
    if (opertType.equals("page")) {
      this.fromSql.append(" and storage.supId=" + supId);
    } else {
      opertType.equals("search");
    }
    StringBuffer appendStorageSql = appendStorageAllSql(orderField, orderDirection, pars);
    this.fromSql.append(appendStorageSql);
    

    Page page = paginate(configName, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), null, this.map, new Object[0]);
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(page.getTotalPage()));
    maps.put("totalCount", Integer.valueOf(page.getTotalRow()));
    maps.put("pageList", page.getList());
    maps.put("limit", Integer.valueOf((pageNum - 1) * numPerPage));
    return maps;
  }
  
  public Map<String, Object> storageHasPrdStockSearch(String configName, int productId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
    throws SQLException, Exception
  {
    initStoragePrd(productId);
    
    StringBuffer appendStorageSql = appendStorageHasSql("has", orderField, orderDirection, pars);
    this.fromSql.append(appendStorageSql);
    

    Page page = null;
    if (this.paras.toString().equals("[]")) {
      page = paginate(configName, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), null, this.map, new Object[0]);
    } else {
      page = paginate(configName, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), null, this.map, new Object[] { pars });
    }
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(page.getTotalPage()));
    maps.put("totalCount", Integer.valueOf(page.getTotalRow()));
    maps.put("pageList", page.getList());
    maps.put("limit", Integer.valueOf((pageNum - 1) * numPerPage));
    return maps;
  }
  
  public Map<String, Object> storageDown(String configName, int productId, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
    throws SQLException, Exception
  {
    initStoragePrd(productId);
    this.fromSql.append(" and storage.supId=" + supId);
    this.fromSql.append(" group by storage.id");
    Page page = null;
    if (this.paras.toString().equals("[]")) {
      page = paginate(configName, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), null, this.map, new Object[0]);
    } else {
      page = paginate(configName, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), null, this.map, new Object[] { pars });
    }
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(page.getTotalPage()));
    maps.put("totalCount", Integer.valueOf(page.getTotalRow()));
    maps.put("pageList", page.getList());
    maps.put("limit", Integer.valueOf((pageNum - 1) * numPerPage));
    return maps;
  }
  
  public Map<String, Object> storageUpPlace(String configName, int productId, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, int ObjectId, Map<String, Object> pars)
    throws SQLException, Exception
  {
    initStoragePrd(productId);
    List<Integer> ids = null;
    ids = Db.use(configName).query("select id from b_storage where supId=" + supId + " and status=" + AioConstants.STATUS_ENABLE + " order by rank");
    int objectIndex = 0;
    for (int i = 0; i < ids.size(); i++)
    {
      objectIndex++;
      if (((Integer)ids.get(i)).intValue() == ObjectId) {
        break;
      }
    }
    pageNum = objectIndex % numPerPage == 0 ? objectIndex / numPerPage : objectIndex / numPerPage + 1;
    this.fromSql.append(" and storage.supId=" + supId + " group by storage.id order by storage.rank asc");
    Page page = null;
    if ((pars != null) && (pars.size() > 0)) {
      page = paginate(configName, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), null, this.map, new Object[] { pars });
    } else {
      page = paginate(configName, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), null, this.map, new Object[0]);
    }
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(page.getTotalPage()));
    maps.put("totalCount", Integer.valueOf(page.getTotalRow()));
    maps.put("pageList", page.getList());
    maps.put("limit", Integer.valueOf((pageNum - 1) * numPerPage));
    
    return maps;
  }
  
  public Map<String, Object> storageSelectEdit(String configName, int productId, int supId, int selectId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
    throws SQLException, Exception
  {
    initStoragePrd(productId);
    this.fromSql.append(" and storage.supId=" + supId);
    StringBuffer appendStorageSql = appendStorageAllSql(orderField, orderDirection, pars);
    this.fromSql.append(appendStorageSql);
    Page page = null;
    if (this.paras.toString().equals("[]")) {
      page = paginate(configName, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), null, this.map, new Object[0]);
    } else {
      page = paginate(configName, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), null, this.map, new Object[] { pars });
    }
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(page.getTotalPage()));
    maps.put("totalCount", Integer.valueOf(page.getTotalRow()));
    maps.put("pageList", page.getList());
    maps.put("limit", Integer.valueOf((pageNum - 1) * numPerPage));
    return maps;
  }
  
  public Model getStorageByNum(String configName, String attrType, int productId, String val)
    throws SQLException, Exception
  {
    initStoragePrd(productId);
    if (attrType.equals("code")) {
      this.fromSql.append(" and storage.code='" + val + "'");
    } else if (attrType.equals("fullName")) {
      this.fromSql.append(" and storage.fullName='" + val + "'");
    }
    Page page = paginate(configName, 1, 2, this.selectSql.toString(), this.fromSql.toString(), null, this.map, new Object[0]);
    List<Model> storagelist = page.getList();
    if ((storagelist != null) && (storagelist.size() > 0)) {
      return (Model)storagelist.get(0);
    }
    return null;
  }
  
  public Record storageSearchBack(String configName, int productId, int storageId)
  {
    Record stockAmounts = Db.use(configName).findFirst("select sum(amount) samount from cc_stock where storageId =" + storageId + " and productId=" + productId);
    Record storage = Db.use(configName).findById("b_storage", Integer.valueOf(storageId));
    storage.set("samount", stockAmounts.get("samount"));
    return storage;
  }
  
  public Model getStockByNum(String configName, String whichCallBack, String privs, String attr, String val, Map<String, Object> pars, int unitId, String priceTail, String discountTail)
  {
    this.paras = new ArrayList();
    
    init(whichCallBack, (Integer)pars.get("storageId"), privs, unitId, priceTail, discountTail);
    if (attr.equals("code")) {
      this.fromSql.append(" and product.node=" + AioConstants.NODE_1 + " and (product.code like '%" + val + "%' or product.fullName like '%" + val + "%' or product.smallName like '%" + val + "%' or product.spell like '%" + val + "%' or product.standard like '%" + val + "%' or product.model like '%" + val + "%' or product.field like '%" + val + "%' or product.barCode1 like '%" + val + "%' or product.barCode2 like '%" + val + "%' or product.barCode3 like '%" + val + "%') group by product.id");
    } else if (attr.equals("fullName")) {
      this.fromSql.append(" and product.node=" + AioConstants.NODE_1 + " and product.fullName like '%" + val + "%' group by product.id");
    }
    return findFirst(configName, this.fromSql.toString(), this.map, this.paras.toArray());
  }
  
  public Map<String, Object> orderSearch(String configName, String privs, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
    throws Exception
  {
    init(null, (Integer)pars.get("storageId"), privs, 0, null, null);
    if ((pars.get("termVal") == null) || (pars.get("termVal").equals(""))) {
      this.fromSql.append(" and product.supId=0");
    } else {
      this.fromSql.append(" and product.node=" + AioConstants.NODE_1);
    }
    StringBuffer appendStockSql = appendStockSql(orderField, orderDirection, pars);
    this.fromSql.append(appendStockSql);
    return aioGetPageManyRecord(configName, "", pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), this.map, this.paras.toArray());
  }
  
  public Map<String, Object> orderStockSearch(String configName, String privs, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
    throws Exception
  {
    init(null, (Integer)pars.get("storageId"), privs, 0, null, null);
    if ((pars.get("termVal") != null) && (!pars.get("termVal").equals(""))) {
      this.fromSql.append(" and product.node=" + AioConstants.NODE_1);
    } else {
      this.fromSql.append(" and product.supId=" + supId);
    }
    StringBuffer appendStockSql = appendStockSql(orderField, orderDirection, pars);
    this.fromSql.append(appendStockSql);
    return aioGetPageManyRecord(configName, "", pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), this.map, this.paras.toArray());
  }
  
  public Map<String, Object> orderDown(String configName, String privs, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
    throws SQLException, Exception
  {
    init(null, (Integer)pars.get("storageId"), privs, 0, null, null);
    
    this.fromSql.append(" and product.supId=" + supId);
    StringBuffer appendStockSql = appendStockSql(orderField, orderDirection, pars);
    this.fromSql.append(appendStockSql);
    return orderDown(configName, privs, "", supId, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), this.map, this.paras.toArray());
  }
  
  public Map<String, Object> orderUpPlace(String configName, String privs, int supId, int pageNum, int numPerPage, String orderField, String orderDirection, int ObjectId, String listID, Map<String, Object> pars)
    throws SQLException, Exception
  {
    init(null, (Integer)pars.get("storageId"), privs, 0, null, null);
    
    this.fromSql.append(" and product.status=" + AioConstants.STATUS_ENABLE + " and product.supId=" + supId);
    StringBuffer appendStockSql = appendStockSql(orderField, orderDirection, pars);
    this.fromSql.append(appendStockSql);
    return orderUpPlace(configName, privs, "", supId, ObjectId, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), this.map, this.paras.toArray());
  }
  
  public List<Model> searchBack(String configName, String whichCallBack, String privs, String ids, Integer storageId, int unitId, String priceTail, String discountTail)
  {
    init(whichCallBack, storageId, privs, unitId, priceTail, discountTail);
    StringBuffer sb = new StringBuffer("select product.pids ");
    sb.append(this.fromSql);
    String[] strs = ids.split(",");
    for (int i = 0; i < strs.length; i++) {
      if (i == 0) {
        sb.append(" and product.pids like '%{" + strs[i] + "}%'");
      } else {
        sb.append(" or product.pids like '%{" + strs[i] + "}%'");
      }
    }
    sb.append(" group by product.id");
    List<Model> prds = find(configName, sb.toString(), this.map);
    String intIds = "";
    for (int i = 0; i < prds.size(); i++)
    {
      String strPids = ((Model)((Model)prds.get(i)).get("product")).getStr("pids");
      intIds = intIds + strPids.substring(strPids.lastIndexOf("{") + 1, strPids.lastIndexOf("}")) + ",";
    }
    intIds = intIds.substring(0, intIds.length() - 1);
    
    this.fromSql.append(" and product.node=" + AioConstants.NODE_1 + " and product.id in (" + intIds + ") group by product.id");
    this.selectSql.append(this.fromSql);
    List<Model> list = find(configName, this.selectSql.toString(), this.map);
    for (Model model : list) {
      if (model.getBigDecimal("price") == null)
      {
        Model product = (Model)model.get("product");
        if (product != null) {
          model.put("price", product.get("price"));
        }
      }
    }
    return list;
  }
  
  public Map<String, Object> orderStockPrdSelectEdit(String configName, String privs, int supId, int selectId, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> pars)
    throws SQLException, Exception
  {
    init(null, (Integer)pars.get("storageId"), privs, 0, null, null);
    
    this.fromSql.append(" and product.status=" + AioConstants.STATUS_ENABLE + " and product.supId=" + supId);
    StringBuffer appendStockSql = appendStockSql(orderField, orderDirection, pars);
    this.fromSql.append(appendStockSql);
    Map<String, Object> mapResult = orderStockPrdSelectEdit(configName, privs, "", selectId, pageNum, numPerPage, this.selectSql.toString(), this.fromSql.toString(), this.map, this.paras.toArray());
    return mapResult;
  }
  
  public Map<String, Object> orderStockPrdSelectEdit(String configName, String privs, String listID, int upObjectId, int pageNum, int numPerPage, String selectSql, String fromSql, Map<String, Class<? extends Model>> alias, Object... pars)
    throws SQLException, Exception
  {
    init(null, Integer.valueOf(0), privs, 0, null, null);
    List<Model> proIds = null;
    if ((pars.length > 0) && (!pars[0].toString().equals("[]"))) {
      proIds = find(configName, "select product.id " + fromSql, this.map, pars);
    } else {
      proIds = find(configName, "select product.id " + fromSql, this.map);
    }
    int objectIndex = 0;
    for (int i = 0; i < proIds.size(); i++)
    {
      objectIndex++;
      if (((Model)((Model)proIds.get(i)).get("product")).getInt("id").intValue() == upObjectId) {
        break;
      }
    }
    pageNum = objectIndex % numPerPage == 0 ? objectIndex / numPerPage : objectIndex / numPerPage + 1;
    Page page = null;
    if ((pars.length > 0) && (!pars[0].toString().equals("[]"))) {
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, this.map, pars);
    } else {
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, this.map, new Object[0]);
    }
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(page.getTotalPage()));
    maps.put("totalCount", Integer.valueOf(page.getTotalRow()));
    maps.put("pageList", page.getList());
    maps.put("listID", listID);
    maps.put("limit", Integer.valueOf((pageNum - 1) * numPerPage));
    return maps;
  }
  
  public Map<String, Object> orderUpPlace(String configName, String privs, String listID, int supId, int upObjectId, int pageNum, int numPerPage, String selectSql, String fromSql, Map<String, Class<? extends Model>> alias, Object... pars)
    throws SQLException, Exception
  {
    StringBuffer listIdSql = new StringBuffer("select id,pids from b_product where supId=" + supId + " and status=" + AioConstants.STATUS_ENABLE);
    
    listIdSql = ComFunController.basePrivsSql(listIdSql, privs, null);
    List<Model> listId = Product.dao.find(configName, listIdSql.toString());
    
    StringBuffer listAllIdSql = new StringBuffer("select id,pids from b_product where status=" + AioConstants.STATUS_ENABLE);
    
    listAllIdSql = ComFunController.basePrivsSql(listAllIdSql, privs, null);
    List<Model> listAllId = Product.dao.find(configName, listAllIdSql.toString());
    
    List<Integer> ids = null;
    
    StringBuffer idsSql = new StringBuffer("select id from b_product where supId=" + supId + " and status=" + AioConstants.STATUS_ENABLE);
    
    idsSql = ComFunController.basePrivsSql(idsSql, privs, null);
    ids = Db.use(configName).query(idsSql.toString());
    int objectIndex = 0;
    for (int i = 0; i < ids.size(); i++)
    {
      objectIndex++;
      if (((Integer)ids.get(i)).intValue() == upObjectId) {
        break;
      }
    }
    pageNum = objectIndex % numPerPage == 0 ? objectIndex / numPerPage : objectIndex / numPerPage + 1;
    Page page = null;
    if ((pars.length > 0) && (!pars[0].toString().equals("[]"))) {
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, alias, pars);
    } else {
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, alias, new Object[0]);
    }
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(page.getTotalPage()));
    maps.put("totalCount", Integer.valueOf(page.getTotalRow()));
    maps.put("pageList", page.getList());
    maps.put("listID", listID);
    maps.put("limit", Integer.valueOf((pageNum - 1) * numPerPage));
    
    maps.put("listAllIdAndPids", listAllId);
    maps.put("listSubIdAndPidsBySupId", listId);
    return maps;
  }
  
  public Map<String, Object> orderDown(String configName, String privs, String listID, int supId, int pageNum, int numPerPage, String selectSql, String fromSql, Map<String, Class<? extends Model>> alias, Object... pars)
    throws SQLException, Exception
  {
    Page page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, alias, pars);
    List pageList = page.getList();
    
    StringBuffer listIdSql = new StringBuffer("select id,pids from b_product where supId=" + supId + " and status=" + AioConstants.STATUS_ENABLE);
    
    listIdSql = ComFunController.basePrivsSql(listIdSql, privs, null);
    List<Model> listId = Product.dao.find(configName, listIdSql.toString());
    
    StringBuffer listAllIdSql = new StringBuffer("select id,pids from b_product where status=" + AioConstants.STATUS_ENABLE);
    
    listAllIdSql = ComFunController.basePrivsSql(listAllIdSql, privs, null);
    List<Model> listAllId = Product.dao.find(configName, listAllIdSql.toString());
    if ((pageList.size() <= 0) && (pageNum > 1))
    {
      int count = page.getTotalRow();
      pageNum = count / numPerPage;
      if (count / numPerPage > count / numPerPage) {
        pageNum++;
      }
      page = paginate(configName, pageNum, numPerPage, selectSql, fromSql, null, alias, pars);
      pageList = page.getList();
    }
    int tp = page.getTotalPage();
    int tr = page.getTotalRow();
    int limit = (pageNum - 1) * numPerPage;
    Map<String, Object> maps = new HashMap();
    maps.put("pageNum", Integer.valueOf(pageNum));
    maps.put("numPerPage", Integer.valueOf(numPerPage));
    maps.put("totalPage", Integer.valueOf(tp));
    maps.put("totalCount", Integer.valueOf(tr));
    maps.put("pageList", pageList);
    maps.put("listID", listID);
    maps.put("limit", Integer.valueOf(limit));
    
    maps.put("listAllIdAndPids", listAllId);
    maps.put("listSubIdAndPidsBySupId", listId);
    return maps;
  }
  
  public StringBuffer appendStockSql(String orderField, String orderDirection, Map<String, Object> pars)
  {
    StringBuffer sql = new StringBuffer("");
    this.paras = new ArrayList();
    String term = "";
    if ((pars != null) && (pars.size() > 0)) {
      term = (String)pars.get("term");
    }
    if ((pars != null) && (term != null) && (!term.equals(""))) {
      if (term.equals("quick"))
      {
        sql.append(" and( product.code like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or product.fullName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or product.spell like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or product.standard like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or product.model like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
        sql.append(" or product.field like ? )");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("code"))
      {
        sql.append(" and (product.code like ? or product.fullName like ? or product.spell like ? or product.standard like ? or product.model like ? or product.field like ? or product.barCode1 like ? or product.barCode2 like ? or product.barCode3 like ?)");
        this.paras.add("%" + pars.get("termVal") + "%");
        this.paras.add("%" + pars.get("termVal") + "%");
        this.paras.add("%" + pars.get("termVal") + "%");
        this.paras.add("%" + pars.get("termVal") + "%");
        this.paras.add("%" + pars.get("termVal") + "%");
        
        this.paras.add("%" + pars.get("termVal") + "%");
        this.paras.add("%" + pars.get("termVal") + "%");
        this.paras.add("%" + pars.get("termVal") + "%");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("fullName"))
      {
        sql.append(" and product.fullName like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("spell"))
      {
        sql.append(" and product.spell like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("standard"))
      {
        sql.append(" and product.standard like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("model"))
      {
        sql.append(" and product.model like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
      else if (term.equals("field"))
      {
        sql.append(" and product.field like ?");
        this.paras.add("%" + pars.get("termVal") + "%");
      }
    }
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by product.rank asc");
    }
    return sql;
  }
  
  public StringBuffer appendStorageAllSql(String orderField, String orderDirection, Map<String, Object> pars)
  {
    return appendStorageHasSql(null, orderField, orderDirection, pars);
  }
  
  public StringBuffer appendStorageHasSql(String type, String orderField, String orderDirection, Map<String, Object> pars)
  {
    StringBuffer sql = new StringBuffer("");
    this.paras = new ArrayList();
    String term = "";
    if ((pars != null) && (pars.size() > 0)) {
      term = (String)pars.get("term");
    }
    if ((pars != null) && (term != null) && (!term.equals(""))) {
      if (term.equals("quick"))
      {
        sql.append(" and( storage.code like '%" + pars.get("termVal") + "%'");
        sql.append(" or storage.fullName like '%" + pars.get("termVal") + "%'");
        sql.append(" or storage.spell like '%" + pars.get("termVal") + "%')");
      }
      else if (term.equals("code"))
      {
        sql.append(" and storage.code like '%" + pars.get("termVal") + "%'");
      }
      else if (term.equals("fullName"))
      {
        sql.append(" and storage.fullName like '%" + pars.get("termVal") + "%'");
      }
      else if (term.equals("spell"))
      {
        sql.append(" and storage.spell like '%" + pars.get("termVal") + "%'");
      }
    }
    sql.append(" group by storage.id");
    if ((type != null) && (!type.equals("")) && (type.equals("has"))) {
      sql.append(" having sum(stock.amount)>0");
    }
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    } else {
      sql.append(" order by storage.rank asc");
    }
    return sql;
  }
  
  public Stock getStock(String configName, int productId, int storageId, BigDecimal costPrice, String batchNum, Date produceDate, Date produceEndDate)
  {
    StringBuffer sql = new StringBuffer("select *  from cc_stock where 1=1");
    List<Object> param = new ArrayList();
    sql.append(" and productId =?");
    param.add(Integer.valueOf(productId));
    sql.append(" and storageId =?");
    param.add(Integer.valueOf(storageId));
    if (StringUtils.isNotBlank(batchNum))
    {
      sql.append(" and batchNum =?");
      param.add(batchNum);
    }
    else
    {
      sql.append(" and batchNum is null");
    }
    if (costPrice != null)
    {
      sql.append(" and costPrice =?");
      param.add(costPrice);
    }
    else
    {
      sql.append(" and costPrice is null");
    }
    if (produceDate != null)
    {
      sql.append(" and produceDate=?");
      param.add(produceDate);
    }
    else
    {
      sql.append(" and produceDate is null");
    }
    if (produceEndDate != null)
    {
      sql.append(" and produceEndDate=?");
      param.add(produceEndDate);
    }
    else
    {
      sql.append(" and produceEndDate is null");
    }
    return (Stock)dao.findFirst(configName, sql.toString(), param.toArray());
  }
  
  public Stock getStock(String configName, Integer productId, Integer storageId)
  {
    StringBuffer sql = new StringBuffer("select *  from cc_stock where 1=1");
    if (productId != null) {
      sql.append(" and productId =" + productId);
    } else {
      sql.append(" and productId is null");
    }
    if (storageId != null) {
      sql.append(" and storageId =" + storageId);
    } else {
      sql.append(" and storageId is null");
    }
    return (Stock)dao.findFirst(configName, sql.toString());
  }
  
  public Model getStockFastInFastOut(String configName, Integer storageId, Integer productId, String havingSql)
  {
    StringBuffer sql = new StringBuffer("select * from cc_stock where");
    sql.append(" productId =?");
    sql.append(" and storageId =?");
    sql.append(" and amount<>0");
    if ((havingSql != null) && (!havingSql.equals(""))) {
      sql.append(" having amount>0");
    }
    sql.append(" order by updateTime asc");
    return dao.findFirst(configName, sql.toString(), new Object[] { productId, storageId });
  }
  
  public Model getObj(String configName, Integer productId, Integer storageId, String batchNum, BigDecimal price, Date produceDate, Date produceEndDate)
  {
    StringBuffer sql = new StringBuffer("select * from cc_stock where 1=1");
    List<Object> param = new ArrayList();
    if (productId != null)
    {
      sql.append(" and productId =?");
      param.add(productId);
    }
    else
    {
      sql.append(" and productId is null");
    }
    if (storageId != null)
    {
      sql.append(" and storageId =?");
      param.add(storageId);
    }
    else
    {
      sql.append(" and storageId is null");
    }
    if (StringUtils.isNotBlank(batchNum))
    {
      sql.append(" and batchNum =?");
      param.add(batchNum);
    }
    else
    {
      sql.append(" and batchNum is null");
    }
    if (price != null)
    {
      sql.append(" and costPrice =?");
      param.add(price);
    }
    else
    {
      sql.append(" and costPrice is null");
    }
    if (produceDate != null) {
      sql.append(" and produceDate= '" + produceDate + "'");
    } else {
      sql.append(" and produceDate is null");
    }
    if (produceEndDate != null) {
      sql.append(" and produceEndDate= '" + produceEndDate + "'");
    } else {
      sql.append(" and produceEndDate is null");
    }
    return dao.findFirst(configName, sql.toString(), param.toArray());
  }
  
  public BigDecimal getStockAmount(String configName, int productId, int storageId, BigDecimal costPrice, String batchNum, Date produceDate, Date produceEndDate)
  {
    StringBuffer sql = new StringBuffer("select sum(amount)  samount from cc_stock where");
    List<Object> param = new ArrayList();
    sql.append(" productId =?");
    param.add(Integer.valueOf(productId));
    sql.append(" and storageId =?");
    param.add(Integer.valueOf(storageId));
    if (StringUtils.isNotBlank(batchNum))
    {
      sql.append(" and batchNum =?");
      param.add(batchNum);
    }
    else
    {
      sql.append(" and batchNum is null");
    }
    if (costPrice != null)
    {
      sql.append(" and costPrice =?");
      param.add(costPrice);
    }
    else
    {
      sql.append(" and costPrice is null");
    }
    if (produceDate != null)
    {
      sql.append(" and produceDate=?");
      param.add(produceDate);
    }
    else
    {
      sql.append(" and produceDate is null");
    }
    if (produceEndDate != null)
    {
      sql.append(" and produceEndDate=?");
      param.add(produceEndDate);
    }
    else
    {
      sql.append(" and produceEndDate is null");
    }
    return Db.use(configName).queryBigDecimal(sql.toString(), param.toArray());
  }
  
  public BigDecimal getStockAmount(String configName, Integer productId, Integer storageId)
  {
    StringBuffer sql = new StringBuffer("select sum(amount) samount from cc_stock where");
    sql.append(" productId =?");
    sql.append(" and storageId =?");
    Model model = dao.findFirst(configName, sql.toString(), new Object[] { productId, storageId });
    return model.getBigDecimal("samount");
  }
  
  public BigDecimal stockProdcutAmount(String configName, int prodcutId)
  {
    Model model = findFirst(configName, "select sum(amount) amount from cc_stock where productId=?", new Object[] { Integer.valueOf(prodcutId) });
    BigDecimal amount = model.getBigDecimal("amount");
    if ((model != null) && (BigDecimalUtils.compare(amount, BigDecimal.ZERO) == 1)) {
      return amount;
    }
    return BigDecimal.ZERO;
  }
  
  public BigDecimal getProAvgPriceByProIds(String configName, Integer productId)
  {
    Record record = Db.use(configName).findFirst("SELECT avgPrice from zj_product_avgprice where productId=?", new Object[] { productId });
    if (record != null) {
      return record.getBigDecimal("avgPrice");
    }
    return null;
  }
  
  public BigDecimal getProAvgPriceBySgeIdAndProIds(String configName, Integer storageId, Integer productId)
  {
    StringBuffer sql = new StringBuffer("SELECT avgPrice from zj_product_avgprice where 1=1 and productId=? ");
    if ((storageId == null) || (storageId.intValue() == 0)) {
      sql.append(" and storageId is null");
    } else {
      sql.append(" and storageId = " + storageId);
    }
    Record record = Db.use(configName).findFirst(sql.toString(), new Object[] { productId });
    if (record != null) {
      return record.getBigDecimal("avgPrice");
    }
    return null;
  }
  
  public BigDecimal getProAvgPriceBySgeIdAndProIds2(String configName, Integer storageId, Integer productId, int selectUnitId)
  {
    Record record = Db.use(configName).findFirst("SELECT avgPrice,p.unitRelation1,p.unitRelation2,p.unitRelation3 from zj_product_avgprice z left join b_product p on p.id=z.productId where z.storageId=? and z.productId=?", new Object[] { storageId, productId });
    if (record != null)
    {
      BigDecimal price = record.getBigDecimal("avgPrice");
      if (selectUnitId == 2) {
        price = BigDecimalUtils.mul(price, record.getBigDecimal("unitRelation2"));
      } else if (selectUnitId == 3) {
        price = BigDecimalUtils.mul(price, record.getBigDecimal("unitRelation3"));
      }
      return price;
    }
    return null;
  }
  
  public BigDecimal getProBuyPriceBySgeIdAndProIds(String configName, Integer productId, Integer selectUnitId)
  {
    PriceDiscountTrack priceDiscountTrack = PriceDiscountTrack.dao.getRecentlyObj(configName, productId.intValue(), selectUnitId.intValue());
    if (priceDiscountTrack != null) {
      return priceDiscountTrack.getBigDecimal("lastCostPrice");
    }
    return null;
  }
  
  public List<Model> getStockGroupAttr(String configName, Integer storageId, Integer productId)
  {
    StringBuffer sql = new StringBuffer("select * from cc_stock where amount >0");
    sql.append(" and productId =?");
    if ((storageId != null) && (storageId.intValue() > 0)) {
      sql.append(" and storageId =?");
    }
    sql.append(" group by productId,batchNum,costPrice,produceDate,produceEndDate,updateTime");
    sql.append(" order by updateTime ASC");
    if ((storageId != null) && (storageId.intValue() > 0)) {
      return dao.find(configName, sql.toString(), new Object[] { productId, storageId });
    }
    return dao.find(configName, sql.toString(), new Object[] { productId });
  }
  
  public List<Model> getStockGroupAttr(String configName, String sgePids, Integer productId)
  {
    StringBuffer sql = new StringBuffer("select * from cc_stock where amount is not null");
    sql.append(" and productId =?");
    sql.append(" AND storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT(?,'%') ) ");
    
    sql.append(" group by productId,storageId,batchNum,costPrice,produceDate,produceEndDate,updateTime");
    sql.append(" order by updateTime ASC");
    return dao.find(configName, sql.toString(), new Object[] { productId, sgePids });
  }
  
  public void notStockAndAddTypeProduct(String configName, int storageId, int productId, BigDecimal amouont, BigDecimal price, String batch, Date produceDate, Date produceEndDate)
  {
    Stock stock = new Stock();
    stock.set("storageId", Integer.valueOf(storageId));
    stock.set("productId", Integer.valueOf(productId));
    stock.set("amount", amouont);
    stock.set("costPrice", price);
    stock.set("batchNum", batch);
    stock.set("produceDate", produceDate);
    stock.set("produceEndDate", produceEndDate);
    stock.set("updateTime", new Date());
    stock.save(configName);
  }
  
  public static BigDecimal getAmountByItem(String configName, int item, int proId, String sgePids)
  {
    BigDecimal amount = BigDecimal.ZERO;
    BigDecimal xsAvg = BigDecimal.ZERO;
    BigDecimal cgAvg = BigDecimal.ZERO;
    
    String cgTable = " FROM cg_purchase_detail cd LEFT JOIN cg_purchase_bill cb ON cd.billId=cb.id ";
    String xsTable = " FROM xs_sell_detail cd LEFT JOIN xs_sell_bill cb ON cd.billId=cb.id ";
    String trem = " WHERE cd.productId=" + proId + " AND cd.storageId IN (SELECT id FROM b_storage WHERE pids LIKE CONCAT('" + sgePids + "','%')) ";
    

    StringBuffer cgSql = new StringBuffer("SELECT IFNULL(IFNULL(SUM(cd.baseAmount),0) / (DATEDIFF(IFNULL(MAX(cb.recodeDate),NOW()),IFNULL(MIN(cb.recodeDate),NOW()))+1),0) avgAmount ");
    cgSql.append(cgTable);
    cgSql.append(trem);
    

    StringBuffer xsSql = new StringBuffer("SELECT IFNULL(IFNULL(SUM(cd.baseAmount),0) / (DATEDIFF(IFNULL(MAX(cb.recodeDate),NOW()),IFNULL(MIN(cb.recodeDate),NOW()))+1),0) avgAmount ");
    xsSql.append(xsTable);
    xsSql.append(trem);
    

    StringBuffer thisSql = new StringBuffer("SELECT IFNULL(IFNULL(SUM(cd.baseAmount),0) / DAY(MAX(cb.recodeDate)),0) avgAmount ");
    thisSql.append(xsTable);
    thisSql.append(trem);
    thisSql.append("AND MONTH(cb.recodeDate) = MONTH(NOW())");
    

    StringBuffer upSql = new StringBuffer("SELECT IFNULL(IFNULL(SUM(cd.baseAmount),0) / DAY(MAX(cb.recodeDate)),0) avgAmount ");
    upSql.append(xsTable);
    upSql.append(trem);
    upSql.append("AND MONTH(cb.recodeDate) = MONTH(NOW())-1");
    switch (item)
    {
    case 0: 
      amount = BigDecimal.ZERO;
      break;
    case -1: 
      xsAvg = Db.use(configName).findFirst(xsSql.toString()).getBigDecimal("avgAmount");
      amount = BigDecimalUtils.mul(xsAvg, new BigDecimal(7));
      break;
    case -2: 
      xsAvg = Db.use(configName).findFirst(xsSql.toString()).getBigDecimal("avgAmount");
      amount = BigDecimalUtils.mul(xsAvg, new BigDecimal(30));
      break;
    case -3: 
      xsAvg = Db.use(configName).findFirst(xsSql.toString()).getBigDecimal("avgAmount");
      amount = BigDecimalUtils.mul(xsAvg, new BigDecimal(90));
      break;
    case -4: 
      BigDecimal upAvg = Db.use(configName).findFirst(upSql.toString()).getBigDecimal("avgAmount");
      amount = BigDecimalUtils.mul(upAvg, new BigDecimal(30));
      break;
    case -5: 
      BigDecimal thisAvg = Db.use(configName).findFirst(thisSql.toString()).getBigDecimal("avgAmount");
      amount = BigDecimalUtils.mul(thisAvg, new BigDecimal(30));
      break;
    case 1: 
      cgAvg = Db.use(configName).findFirst(cgSql.toString()).getBigDecimal("avgAmount");
      amount = BigDecimalUtils.mul(cgAvg, new BigDecimal(7));
      break;
    case 2: 
      cgAvg = Db.use(configName).findFirst(cgSql.toString()).getBigDecimal("avgAmount");
      amount = BigDecimalUtils.mul(cgAvg, new BigDecimal(30));
      break;
    case 3: 
      cgAvg = Db.use(configName).findFirst(cgSql.toString()).getBigDecimal("avgAmount");
      amount = BigDecimalUtils.mul(cgAvg, new BigDecimal(90));
      break;
    }
    return amount;
  }
  
  public Map<String, Object> stockExpiration(String configName, String listID, int pageNum, int numPerPage, String orderField, String orderDirection, int aheadDay, Map<String, Object> params)
    throws Exception
  {
    StringBuffer sel = new StringBuffer("select sum(stock.amount)as amount ,stock.productId,stock.costPrice,stock.storageId,stock.produceDate,stock.produceEndDate");
    StringBuffer sql = new StringBuffer("from cc_stock stock");
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("product", Product.class);
    map.put("storage", Storage.class);
    
    sel.append(",product.*,DATEDIFF(stock.produceEndDate,stock.produceDate) as dueDate");
    sel.append(",storage.*");
    sql.append(" left join b_product as product on product.id = stock.productId");
    sql.append(" left join b_storage as storage on storage.id = stock.storageId");
    sql.append(" where stock.produceDate is not null");
    

    sql.append(" and CURDATE()>date_sub(stock.produceEndDate, INTERVAL " + aheadDay + " day)");
    if (params != null)
    {
      String productPrivs = (String)params.get("productPrivs");
      if ((StringUtils.isNotBlank(productPrivs)) && (!"*".equals(productPrivs))) {
        sql.append(" and product.id in (" + productPrivs + ")");
      } else if (StringUtils.isBlank(productPrivs)) {
        sql.append(" and product.id = 0");
      }
      String storagePrivs = (String)params.get("storagePrivs");
      if ((StringUtils.isNotBlank(storagePrivs)) && (!"*".equals(storagePrivs))) {
        sql.append(" and storage.id in (" + storagePrivs + ")");
      } else if (StringUtils.isBlank(storagePrivs)) {
        sql.append(" and storage.id = 0");
      }
    }
    sql.append(" group by stock.productId,stock.costPrice,stock.storageId,stock.produceDate,stock.produceEndDate,stock.batchNum");
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    }
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), map, new Object[0]);
  }
  
  public Map<String, Object> minusStockPage(String configName, String listID, int pageNum, int numPerPage, String orderField, String orderDirection, Map<String, Object> params)
    throws Exception
  {
    StringBuffer sel = new StringBuffer("select stock.amount,product.*");
    
    StringBuffer sql = new StringBuffer("from ( select sum(amount)as amount,productId from cc_stock where 1=1");
    if (params != null)
    {
      String productPrivs = (String)params.get("productPrivs");
      if ((StringUtils.isNotBlank(productPrivs)) && (!"*".equals(productPrivs))) {
        sql.append(" and (productId in (" + productPrivs + ") or productId is null )");
      } else if (StringUtils.isBlank(productPrivs)) {
        sql.append(" and (productId = 0 or productId is null)");
      }
      String storagePrivs = (String)params.get("storagePrivs");
      if ((StringUtils.isNotBlank(storagePrivs)) && (!"*".equals(storagePrivs))) {
        sql.append(" and (storageId in (" + storagePrivs + ") or storageId is null )");
      } else if (StringUtils.isBlank(storagePrivs)) {
        sql.append(" and (storageId = 0 or storageId is null)");
      }
      if (params.get("storageId") != null) {
        sql.append(" and storageId = " + params.get("storageId"));
      }
    }
    sql.append(" group by productId) as stock");
    
    Map<String, Class<? extends Model>> map = new HashMap();
    map.put("product", Product.class);
    sql.append(" left join b_product as product on product.id = stock.productId");
    
    sql.append(" where 1=1");
    sql.append(" and stock.amount<0");
    if (StringUtils.isNotBlank(orderField)) {
      sql.append(" order by " + orderField + " " + orderDirection);
    }
    return aioGetPageManyRecord(configName, listID, pageNum, numPerPage, sel.toString(), sql.toString(), map, new Object[0]);
  }
}
