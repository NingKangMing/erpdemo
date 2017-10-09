package com.aioerp.port.controller.om;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.sell.sellbook.SellbookBill;
import com.aioerp.model.sell.sellbook.SellbookDetail;
import com.aioerp.model.supadmin.WhichDb;
import com.aioerp.port.GetUrlProtData;
import com.aioerp.port.common.PortConstants;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.MD5Util;
import com.aioerp.util.UrlCode;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class OmPortController
  extends BaseController
{
  private Map<String, Object> map = new HashMap();
  
  @Before({Tx.class})
  public void index()
    throws SQLException
  {
    String configNameId = WhichDb.dao.getChildConfigNameId();
    if (configNameId == null)
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "未开启OM订单系统。");
      renderJson(this.map);
      return;
    }
    String methodCode = getPara("methodCode", "");
    if (methodCode.equals(""))
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口编号为空。");
      renderJson(this.map);
    }
    boolean flag = true;
    if (methodCode.equals("104")) {
      flag = portKeyVlidate(configNameId, false);
    } else {
      flag = portKeyVlidate(configNameId, true);
    }
    if (!flag)
    {
      renderJson(this.map);
      return;
    }
    if (methodCode.equals("101"))
    {
      getProdcutSortList(configNameId);
    }
    else if (methodCode.equals("102"))
    {
      getProductList(configNameId);
    }
    else if (methodCode.equals("103"))
    {
      setOmOrder(configNameId);
    }
    else if (methodCode.equals("104"))
    {
      getIsLoginUser(configNameId);
    }
    else if (methodCode.equals("105"))
    {
      setNewPwd(configNameId);
    }
    else if (methodCode.equals("106"))
    {
      getProductDetail(configNameId);
    }
    else if (methodCode.equals("107"))
    {
      setOmOrderOver(configNameId);
    }
    else if (methodCode.equals("108"))
    {
      setOmOrderDel(configNameId);
    }
    else
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口编号非法。");
      renderJson(this.map);
    }
  }
  
  @Before({Tx.class})
  public void getProdcutSortList(String configName)
    throws SQLException
  {
    int supId = 0;
    try
    {
      supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口传入参数非法。");
      renderJson(this.map);
      return;
    }
    List<Record> list = null;
    try
    {
      StringBuffer sql = new StringBuffer("select product.id,product.fullName,product.supId,");
      sql.append(" (select sum(st.amount) from b_product p left join cc_stock st on st.productId = p.id where p.pids like concat(product.pids,'%') and p.defaultPrice11>0 ) AS total_amount");
      sql.append(" from b_product product left join cc_stock stock on stock.productId=product.id  where ");
      sql.append(" product.node=" + AioConstants.NODE_2 + " and product.status=" + AioConstants.STATUS_ENABLE);
      if (supId != 0) {
        sql.append(" and product.supId=" + supId);
      }
      list = Db.use(configName).find(sql.toString());
      this.map.put("ret", PortConstants.RET0);
      this.map.put("msg", "操作成功");
      this.map.put("list", list);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET2);
      this.map.put("msg", "接口出现异常，请联系管理员。");
    }
    renderJson(this.map);
  }
  
  @Before({Tx.class})
  public void getProductList(String configName)
  {
    String stockAmount = "";
    int unitId = 0;
    int supId = 0;
    String fullName = null;
    String standard = null;
    String model = null;
    int pageNumber = 0;
    int pageSize = 0;
    try
    {
      stockAmount = getPara("stockAmount", "all");
      unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
      supId = getParaToInt("supId", Integer.valueOf(0)).intValue();
      fullName = UrlCode.aioDecode(getPara("fullName", ""));
      standard = UrlCode.aioDecode(getPara("standard", ""));
      model = UrlCode.aioDecode(getPara("model", ""));
      pageNumber = getParaToInt("pageNumber", Integer.valueOf(PortConstants.START_PAGE)).intValue();
      pageSize = getParaToInt("pageSize", Integer.valueOf(10)).intValue();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口传入参数非法。");
      renderJson(this.map);
      return;
    }
    Record unitObj = Db.use(configName).findFirst("select u.storageIds from om_port_user u left join b_unit unit on unit.id=u.unitId  where unit.id=" + unitId + " and unit.status=" + AioConstants.STATUS_ENABLE);
    if (unitObj == null)
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "传入unitId参数无效。");
      renderJson(this.map);
      return;
    }
    try
    {
      List<Object> para = new ArrayList();
      StringBuffer selectSql = new StringBuffer();
      selectSql.append("select temp.*");
      StringBuffer fromSql = new StringBuffer();
      fromSql.append(" from (select product.id,product.fullName,product.standard,product.model,product.calculateUnit1,product.defaultPrice11 price,product.supId,sum(stock.amount) stockStatus");
      fromSql.append(" from  cc_stock stock left join b_product product on stock.productId=product.id");
      fromSql.append(" where product.status=" + AioConstants.STATUS_ENABLE + " and product.node=" + AioConstants.NODE_1 + " and stock.storageId in(" + unitObj.getStr("storageIds") + ") and product.defaultPrice11 >0");
      fromSql.append(" union all ");
      fromSql.append("select product.id,product.fullName,product.standard,product.model,product.calculateUnit1,product.defaultPrice11 price,product.supId,0 stockStatus");
      fromSql.append(" from b_product product where product.status=" + AioConstants.STATUS_ENABLE + " and product.node=" + AioConstants.NODE_1 + " and product.defaultPrice11 >0");
      fromSql.append(") temp where 1=1 ");
      if (stockAmount.equals("lgZero")) {
        fromSql.append(" and temp.stockStatus>0");
      }
      if (supId != 0) {
        fromSql.append(" and temp.pids like '%{" + supId + "}%'");
      }
      if (!fullName.equals(""))
      {
        fromSql.append(" and temp.fullName like ?");
        para.add("%" + fullName + "%");
      }
      if (!standard.equals(""))
      {
        fromSql.append(" and temp.standard like ?");
        para.add("%" + standard + "%");
      }
      if (!model.equals(""))
      {
        fromSql.append(" and temp.model like ?");
        para.add("%" + model + "%");
      }
      fromSql.append(" and temp.id>0 group by temp.id");
      Page<Record> page = Db.use(configName).paginate(pageNumber, pageSize, selectSql.toString(), fromSql.toString(), null, para.toArray());
      this.map.put("ret", PortConstants.RET0);
      this.map.put("msg", "操作成功");
      this.map.put("page", page);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET2);
      this.map.put("msg", "接口出现异常，请联系管理员。");
    }
    renderJson(this.map);
  }
  
  public void getProductDetail(String configName)
  {
    int id = 0;
    try
    {
      id = getParaToInt("productId", Integer.valueOf(0)).intValue();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口传入参数非法。");
      renderJson(this.map);
      return;
    }
    if (id == 0)
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口传入商品ID参数非法。");
      renderJson(this.map);
      return;
    }
    try
    {
      StringBuffer sb = new StringBuffer("select product.id,product.fullName,product.standard,product.model,product.calculateUnit1,product.defaultPrice11 price,product.memo remark,file.savePath");
      sb.append(" from b_product product left join aioerp_file file on file.id=product.sysImgId where product.id=" + id);
      Record r = Db.use(configName).findFirst(sb.toString());
      if (r == null)
      {
        this.map.put("ret", PortConstants.RET2);
        this.map.put("msg", "接口传入的商品ID不合法。");
        renderJson(this.map);
      }
      this.map.put("ret", PortConstants.RET0);
      this.map.put("msg", "操作成功");
      this.map.put("id", r.getInt("id"));
      this.map.put("fullName", r.getStr("fullName"));
      this.map.put("standard", r.getStr("standard"));
      this.map.put("model", r.getStr("model"));
      this.map.put("calculateUnit1", r.getStr("calculateUnit1"));
      this.map.put("price", r.getBigDecimal("price"));
      this.map.put("imgPath", "/upLoadImg/" + r.getStr("savePath"));
      this.map.put("remark", r.getStr("remark"));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET2);
      this.map.put("msg", "接口出现异常，请联系管理员。");
    }
    renderJson(this.map);
  }
  
  @Before({Tx.class})
  public void setOmOrder(String configName)
    throws SQLException
  {
    int unitId = 0;
    String name = null;
    String linkman = null;
    String address = null;
    String phone = null;
    
    String totalMoney = null;
    String omOrderCode = null;
    String carts = null;
    try
    {
      unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
      name = UrlCode.aioDecode(getPara("name", ""));
      linkman = UrlCode.aioDecode(getPara("linkman", ""));
      address = UrlCode.aioDecode(getPara("address", ""));
      phone = UrlCode.aioDecode(getPara("phone", ""));
      totalMoney = getPara("moneys", "0");
      omOrderCode = UrlCode.aioDecode(getPara("omOrderCode", ""));
      carts = getPara("carts", "");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口传入参数非法。");
      renderJson(this.map);
      return;
    }
    if (carts.equals(""))
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "订单数据不能为空。");
      renderJson(this.map);
      return;
    }
    JSONArray jsonList = null;
    try
    {
      jsonList = new JSONArray(carts);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "订单数据json格式不正确。");
      renderJson(this.map);
      return;
    }
    Record r = null;
    if (unitId == 0)
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "请传入简易代理商Id 。");
      renderJson(this.map);
      return;
    }
    r = Db.use(configName).findFirst("select * from b_unit where id=" + unitId + " and status=" + AioConstants.STATUS_ENABLE);
    if ((r == null) || (r.getStr("code") == null))
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "非法用户。");
      renderJson(this.map);
      return;
    }
    if ((omOrderCode == null) || (omOrderCode.equals("")))
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "订单编号不能为空。");
      renderJson(this.map);
      return;
    }
    String str = "收货客户信息:【公司名称:" + name + ",负责人:" + linkman + ",电话：" + phone + ",收货地址：" + address + "】;";
    

    SellbookBill sellbook = new SellbookBill();
    sellbook.set("code", "XSDD-" + omOrderCode);
    sellbook.set("unitId", Integer.valueOf(unitId));
    sellbook.set("status", Integer.valueOf(AioConstants.BILL_STATUS3));
    sellbook.set("relStatus", Integer.valueOf(AioConstants.STATUS_DISABLE));
    Date time = new Date();
    sellbook.set("updateTime", time);
    sellbook.set("recodeDate", time);
    sellbook.set("discounts", Integer.valueOf(1));
    sellbook.set("memo", str);
    BigDecimal amountAll = BigDecimal.ZERO;
    BigDecimal moneysAll = BigDecimal.ZERO;
    
    List<SellbookDetail> sellbookDetailList = new ArrayList();
    SellbookDetail sellbookDetail = null;
    for (int i = 0; i < jsonList.length(); i++)
    {
      sellbookDetail = new SellbookDetail();
      JSONObject jobj = null;
      Integer omOrderId = Integer.valueOf(0);
      Integer productId = Integer.valueOf(0);
      BigDecimal amount = BigDecimal.ZERO;
      BigDecimal money = BigDecimal.ZERO;
      String remark = "";
      try
      {
        jobj = (JSONObject)jsonList.get(i);
        omOrderId = (Integer)jobj.get("id");
        productId = (Integer)jobj.get("productId");
        amount = new BigDecimal(jobj.get("amount").toString());
        money = new BigDecimal(jobj.get("money").toString());
        remark = UrlCode.aioDecode((String)jobj.get("remark"));
      }
      catch (Exception e)
      {
        e.printStackTrace();
        this.map.put("ret", PortConstants.RET1);
        this.map.put("msg", "订单数据json格式不正确。");
        renderJson(this.map);
        return;
      }
      if ((omOrderId == null) || (omOrderId.intValue() == 0))
      {
        this.map.put("ret", PortConstants.RET1);
        this.map.put("msg", "请传入订单明细Id。");
        renderJson(this.map);
        return;
      }
      if ((productId == null) || (productId.intValue() == 0))
      {
        this.map.put("ret", PortConstants.RET1);
        this.map.put("msg", "请传入商品Id。");
        renderJson(this.map);
        return;
      }
      sellbookDetail.set("productId", productId);
      sellbookDetail.set("selectUnitId", Integer.valueOf(1));
      if (BigDecimalUtils.compare(amount, BigDecimal.ZERO) < 1)
      {
        this.map.put("ret", PortConstants.RET1);
        this.map.put("msg", "商品数量要大于0。");
        renderJson(this.map);
        return;
      }
      sellbookDetail.set("baseAmount", amount);
      sellbookDetail.set("amount", amount);
      sellbookDetail.set("untreatedAmount", amount);
      BigDecimal defaultPrice11 = BigDecimalUtils.div(money, amount);
      sellbookDetail.set("basePrice", defaultPrice11);
      sellbookDetail.set("price", defaultPrice11);
      sellbookDetail.set("discountPrice", defaultPrice11);
      sellbookDetail.set("taxPrice", defaultPrice11);
      sellbookDetail.set("discount", Integer.valueOf(1));
      sellbookDetail.set("money", money);
      sellbookDetail.set("discountMoney", money);
      sellbookDetail.set("taxMoney", money);
      sellbookDetail.set("updateTime", time);
      sellbookDetail.set("memo", remark.equals("null") ? "" : remark);
      
      sellbookDetail.set("relStatus", Integer.valueOf(AioConstants.STATUS_DISABLE));
      sellbookDetail.put("omOrderId", omOrderId);
      amountAll = BigDecimalUtils.add(amountAll, amount);
      moneysAll = BigDecimalUtils.add(moneysAll, money);
      sellbookDetailList.add(sellbookDetail);
    }
    try
    {
      sellbook.set("amounts", amountAll);
      sellbook.set("moneys", moneysAll);
      sellbook.set("discountMoneys", moneysAll);
      sellbook.set("taxMoneys", moneysAll);
      sellbook.save(configName);
      
      Record om = new Record();
      om.set("sellbookId", sellbook.getInt("id"));
      om.set("unitId", Integer.valueOf(unitId));
      om.set("omOrderCode", omOrderCode);
      om.set("name", name);
      om.set("linkman", linkman);
      om.set("phone", phone);
      om.set("address", address);
      Db.use(configName).save("om_port_record", om);
      for (int i = 0; i < sellbookDetailList.size(); i++)
      {
        sellbookDetail = (SellbookDetail)sellbookDetailList.get(i);
        sellbookDetail.set("billId", sellbook.getInt("id"));
        int omOrderId = sellbookDetail.getInt("omOrderId").intValue();
        sellbookDetail.remove("omOrderId");
        sellbookDetail.save(configName);
        

        Record d = new Record();
        d.set("sellbookDetaillId", sellbookDetail.getInt("id"));
        d.set("omOrderId", Integer.valueOf(omOrderId));
        d.set("billId", new Integer(om.getLong("id").toString()));
        Db.use(configName).save("om_port_record_detail", d);
      }
      this.map.put("ret", PortConstants.RET0);
      this.map.put("msg", "操作成功");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      this.map.put("ret", PortConstants.RET2);
      this.map.put("msg", "接口出现异常，请联系管理员。");
    }
    renderJson(this.map);
  }
  
  public void getIsLoginUser(String configName)
  {
    String loginName = null;
    String loginPwd = null;
    try
    {
      loginName = UrlCode.aioDecode(getPara("loginName", ""));
      loginPwd = UrlCode.aioDecode(getPara("loginPwd", ""));
      
      loginPwd = MD5Util.string2MD5(loginPwd);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口传入参数非法。");
      renderJson(this.map);
      return;
    }
    Record r = Db.use(configName).findFirst("select * from om_port_user where loginName=? and loginPwd=? and status=" + PortConstants.OM_STATUS2, new Object[] { loginName, loginPwd });
    if (r == null)
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "用户名或密码有误");
      renderJson(this.map);
      return;
    }
    this.map.put("ret", PortConstants.RET0);
    this.map.put("unitId", r.getInt("unitId"));
    this.map.put("portKey", r.getStr("portKey"));
    this.map.put("msg", "操作成功");
    renderJson(this.map);
  }
  
  @Before({Tx.class})
  public void setNewPwd(String configName)
    throws SQLException
  {
    int unitId = 0;
    String loginName = null;
    String oldPwd = null;
    String newPwd = null;
    try
    {
      unitId = getParaToInt("unitId", Integer.valueOf(0)).intValue();
      loginName = UrlCode.aioDecode(getPara("loginName", ""));
      oldPwd = UrlCode.aioDecode(getPara("oldPwd", ""));
      newPwd = UrlCode.aioDecode(getPara("newPwd", ""));
      
      oldPwd = MD5Util.string2MD5(oldPwd);
      newPwd = MD5Util.string2MD5(newPwd);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口传入参数非法。");
      renderJson(this.map);
      return;
    }
    if (unitId == 0)
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "请传入接口的unitId。");
      renderJson(this.map);
      return;
    }
    Record r = Db.use(configName).findFirst("select * from om_port_user where unitId=? and loginName=? and loginPwd=? and status=" + PortConstants.OM_STATUS2, new Object[] { Integer.valueOf(unitId), loginName, oldPwd });
    if (r == null)
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "旧密码有误");
      renderJson(this.map);
      return;
    }
    r.set("loginPwd", newPwd);
    try
    {
      Db.use(configName).update("om_port_user", r);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      this.map.put("ret", PortConstants.RET2);
      this.map.put("msg", "接口出现异常，请联系管理员。");
      renderJson(this.map);
      return;
    }
    this.map.put("ret", PortConstants.RET0);
    this.map.put("msg", "操作成功");
    renderJson(this.map);
  }
  
  @Before({Tx.class})
  public void setOmOrderOver(String configName)
    throws SQLException
  {
    Integer[] omOrderdetailIds = null;
    try
    {
      omOrderdetailIds = getParaValuesToInt("detailId");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口传入参数非法。");
      renderJson(this.map);
      return;
    }
    if (omOrderdetailIds == null)
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "请传入接口订单明细Id。");
      renderJson(this.map);
      return;
    }
    String str = "";
    for (int i = 0; i < omOrderdetailIds.length; i++) {
      str = str + "," + omOrderdetailIds[i];
    }
    str = str.substring(1, str.length());
    List<Record> protDatas = Db.use(configName).find("select * from om_port_record_detail where omOrderId in(" + str + ")");
    if ((protDatas == null) || (protDatas.size() == 0))
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "不存在该订单明细Id");
      renderJson(this.map);
      return;
    }
    String sql = "";
    for (int i = 0; i < protDatas.size(); i++) {
      sql = sql + "," + ((Record)protDatas.get(i)).getInt("sellbookDetaillId");
    }
    sql = sql.substring(1, sql.length());
    List<Record> sellBookDetailList = Db.use(configName).find("select * from xs_sellbook_detail where id in(" + sql + ")");
    try
    {
      for (int i = 0; i < sellBookDetailList.size(); i++)
      {
        Record sellBookDetail = (Record)sellBookDetailList.get(i);
        sellBookDetail.set("forceAmount", sellBookDetail.getBigDecimal("untreatedAmount"));
        sellBookDetail.set("untreatedAmount", BigDecimal.ZERO);
        sellBookDetail.set("arrivalAmount", sellBookDetail.getBigDecimal("baseAmount"));
        Date time = new Date();
        sellBookDetail.set("relStatus", Integer.valueOf(AioConstants.STATUS_FORCE));
        Object memo = sellBookDetail.get("memo");
        if (memo != null) {
          sellBookDetail.set("memo", memo + "[强制完成]");
        } else {
          sellBookDetail.set("memo", "[强制完成]");
        }
        sellBookDetail.set("updateTime", time);
        Db.use(configName).update("xs_sellbook_detail", sellBookDetail);
        
        Integer billId = sellBookDetail.getInt("billId");
        List<Model> detail = SellbookDetail.dao.getUntreatedList(configName, billId);
        if ((detail == null) || (detail.size() == 0)) {
          SellbookBill.dao.findById(configName, billId).set("relStatus", Integer.valueOf(AioConstants.STATUS_ENABLE)).set("updateTime", new Date()).update(configName);
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      this.map.put("ret", PortConstants.RET2);
      this.map.put("msg", "系统出现异常，请联系管理员");
      renderJson(this.map);
      return;
    }
    try
    {
      int billId = ((Record)sellBookDetailList.get(0)).getInt("billId").intValue();
      List<Model> detail = SellbookDetail.dao.getUntreatedList(configName, Integer.valueOf(billId));
      if ((detail == null) || (detail.size() == 0)) {
        SellbookBill.dao.findById(configName, Integer.valueOf(billId)).set("relStatus", Integer.valueOf(AioConstants.STATUS_ENABLE)).set("updateTime", new Date()).update(configName);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET2);
      this.map.put("msg", "系统出现异常，请联系管理员");
      renderJson(this.map);
      return;
    }
    this.map.put("ret", PortConstants.RET0);
    this.map.put("msg", "操作成功");
    renderJson(this.map);
  }
  
  @Before({Tx.class})
  public void setOmOrderDel(String configName)
    throws SQLException
  {
    Integer[] omOrderdetailIds = null;
    try
    {
      omOrderdetailIds = getParaValuesToInt("detailId");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口传入参数非法。");
      renderJson(this.map);
      return;
    }
    if (omOrderdetailIds == null)
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "请传入接口订单明细Id。");
      renderJson(this.map);
      return;
    }
    String str = "";
    for (int i = 0; i < omOrderdetailIds.length; i++) {
      str = str + "," + omOrderdetailIds[i];
    }
    str = str.substring(1, str.length());
    List<Record> protDatas = Db.use(configName).find("select * from om_port_record_detail where omOrderId in(" + str + ")");
    if ((protDatas == null) || (protDatas.size() == 0))
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "不存在该订单明细Id");
      renderJson(this.map);
      return;
    }
    String sql = "";
    for (int i = 0; i < protDatas.size(); i++) {
      sql = sql + "," + ((Record)protDatas.get(i)).getInt("sellbookDetaillId");
    }
    sql = sql.substring(1, sql.length());
    List<Record> sellBookDetailList = Db.use(configName).find("select * from xs_sellbook_detail where id in(" + sql + ")");
    
    Record sellbookBill = Db.use(configName).findById("xs_sellbook_bill", ((Record)sellBookDetailList.get(0)).getInt("billId"));
    try
    {
      for (int i = 0; i < sellBookDetailList.size(); i++)
      {
        Record sellBookDetail = (Record)sellBookDetailList.get(i);
        if (BigDecimalUtils.compare(sellBookDetail.getBigDecimal("arrivalAmount"), BigDecimal.ZERO) == 0)
        {
          sellbookBill.set("amounts", BigDecimalUtils.sub(sellbookBill.getBigDecimal("amounts"), sellBookDetail.getBigDecimal("amount")));
          sellbookBill.set("moneys", BigDecimalUtils.sub(sellbookBill.getBigDecimal("moneys"), sellBookDetail.getBigDecimal("money")));
          sellbookBill.set("discountMoneys", sellbookBill.getBigDecimal("moneys"));
          sellbookBill.set("taxMoneys", sellbookBill.getBigDecimal("moneys"));
          Db.use(configName).delete("xs_sellbook_detail", (Record)sellBookDetailList.get(i));
        }
        else
        {
          this.map.put("ret", PortConstants.RET1);
          this.map.put("msg", "商品ID为" + sellBookDetail.getInt("productId") + "已经被引用不能删除");
          renderJson(this.map);
          return;
        }
      }
      int billId = ((Record)sellBookDetailList.get(0)).getInt("billId").intValue();
      List<Model> detail = SellbookDetail.dao.getUntreatedList(configName, Integer.valueOf(billId));
      if ((detail == null) || (detail.size() == 0)) {
        Db.use(configName).deleteById("xs_sellbook_bill", Integer.valueOf(billId));
      } else {
        Db.use(configName).update("xs_sellbook_bill", sellbookBill);
      }
      for (int i = 0; i < protDatas.size(); i++) {
        Db.use(configName).delete("om_port_record_detail", (Record)protDatas.get(i));
      }
      Record rbill = Db.use(configName).findFirst("select * from om_port_record_detail where billId=" + ((Record)protDatas.get(0)).getInt("billId"));
      if (rbill == null) {
        Db.use(configName).deleteById("om_port_record", ((Record)protDatas.get(0)).getInt("billId"));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      commonRollBack();
      this.map.put("ret", PortConstants.RET2);
      this.map.put("msg", "接口异常。");
      renderJson(this.map);
      return;
    }
    this.map.put("ret", PortConstants.RET0);
    this.map.put("msg", "操作成功");
    renderJson(this.map);
  }
  
  public static JSONObject portDominaNamePwd(String omDomainName, String dominaNamePwd)
  {
    StringBuffer urlStr = new StringBuffer(omDomainName + PortConstants.AIOOM_PORT_ROUTE + "?");
    Map<String, Object> map = new HashMap();
    map.put("methodCode", PortConstants.METHOD201);
    map.put("dominaNamePwd", MD5Util.string2MD5(dominaNamePwd));
    return GetUrlProtData.getJsonToPortByParms(urlStr.toString(), map, null);
  }
  
  public static JSONObject portNoticeOmHasDelivery(String omDomainName, List<Record> sellbookDetailList, String waybillnumber, String deliveryCompanyCode, String deliveryCompany)
  {
    String datas = JsonKit.toJson(sellbookDetailList, 2);
    StringBuffer urlStr = new StringBuffer(omDomainName + PortConstants.AIOOM_PORT_ROUTE + "?");
    Map<String, Object> map = new HashMap();
    map.put("methodCode", PortConstants.METHOD202);
    map.put("waybillnumber", waybillnumber);
    map.put("deliveryCompanyCode", deliveryCompanyCode);
    map.put("deliveryCompany", deliveryCompany);
    map.put("datas", datas);
    return GetUrlProtData.getJsonToPortByParms(urlStr.toString(), map, null);
  }
  
  public boolean portKeyVlidate(String configName, boolean flag)
  {
    if (!flag) {
      return true;
    }
    String portKey = getPara("portKey", "");
    if (portKey.equals(""))
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口密钥不能为空。");
      return false;
    }
    String unitId = getPara("unitId", "");
    Long count = Long.valueOf(0L);
    if (!unitId.equals("")) {
      count = Db.use(configName).queryLong("select count(*) from om_port_user where portKey=? and unitid=?", new Object[] { portKey, unitId });
    } else {
      count = Db.use(configName).queryLong("select count(*) from om_port_user where portKey=?", new Object[] { portKey });
    }
    if ((count == null) || (count.longValue() == 0L))
    {
      this.map.put("ret", PortConstants.RET1);
      this.map.put("msg", "接口密钥不存在。");
      return false;
    }
    return true;
  }
}
