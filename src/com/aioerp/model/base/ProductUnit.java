package com.aioerp.model.base;

import com.aioerp.common.AioConstants;
import com.aioerp.model.BaseDbModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductUnit
  extends BaseDbModel
{
  public static final ProductUnit dao = new ProductUnit();
  public static final String TABLE_NAME = "b_product_unit";
  
  public String getDefaultUnitByBillTypeName(String billTypeName)
  {
    if (billTypeName == null) {
      return null;
    }
    String attr = "inDefaultUnit";
    
    Map<String, String> in = new HashMap();
    
    in.put("bought", "进货订单");
    in.put("purchase", "进货单");
    in.put("purchaseReturn", "进货退货单");
    in.put("purchaseBarter", "进货换货单");
    
    Map<String, String> stock = new HashMap();
    stock.put("takeStockBill", "takeStockBill");
    stock.put("breakageBill", "报损单");
    stock.put("overflowBill", "报溢单");
    stock.put("stockOtherin", "其它入库单");
    stock.put("stockOtherout", "其它出库单");
    stock.put("parityAllot", "同价调拨单");
    stock.put("difftAllotBill", "变价调拨单");
    stock.put("dismountBill", "生产拆装单");
    stock.put("adjustCost", "成本调价单");
    
    Map<String, String> out = new HashMap();
    out.put("sellBook", "销售订单");
    out.put("sell", "销售单");
    out.put("sellReturn", "销售退货单");
    out.put("sellBarter", "销售换货单");
    if (in.containsKey(billTypeName)) {
      attr = "inDefaultUnit";
    } else if (stock.containsKey(billTypeName)) {
      attr = "stockDefaultUnit";
    } else if (out.containsKey(billTypeName)) {
      attr = "outDefaultUnit";
    }
    return attr;
  }
  
  public Map<String, Integer> getDefaultUnit(String configName, int productId)
  {
    Map<String, Integer> map = new HashMap();
    List<Record> list = Db.use(configName).find("select * from b_product_unit where productId=" + productId);
    for (int i = 0; i < list.size(); i++)
    {
      Record r = (Record)list.get(i);
      if (r.getInt("inDefaultUnit") != null) {
        map.put("inDefaultUnit", r.getInt("selectUnitId"));
      }
      if (r.getInt("stockDefaultUnit") != null) {
        map.put("stockDefaultUnit", r.getInt("selectUnitId"));
      }
      if (r.getInt("outDefaultUnit") != null) {
        map.put("outDefaultUnit", r.getInt("selectUnitId"));
      }
    }
    return map;
  }
  
  public void setDefaultUnit(Record record, int selectUnitId, int inDefaultUnit, int stockDefaultUnit, int outDefaultUnit)
  {
    if (selectUnitId == inDefaultUnit) {
      record.set("inDefaultUnit", Integer.valueOf(inDefaultUnit));
    } else {
      record.set("inDefaultUnit", null);
    }
    if (selectUnitId == stockDefaultUnit) {
      record.set("stockDefaultUnit", Integer.valueOf(stockDefaultUnit));
    } else {
      record.set("stockDefaultUnit", null);
    }
    if (selectUnitId == outDefaultUnit) {
      record.set("outDefaultUnit", Integer.valueOf(outDefaultUnit));
    } else {
      record.set("outDefaultUnit", null);
    }
  }
  
  public void addProductUnit(String configName, Model model, int inDefaultUnit, int stockDefaultUnit, int outDefaultUnit)
  {
    if (model == null) {
      return;
    }
    Date time = new Date();
    Record p1 = new Record();
    p1.set("productId", model.getInt("id"));
    p1.set("selectUnitId", Integer.valueOf(1));
    p1.set("calculateUnit", model.getStr("calculateUnit1"));
    p1.set("unitRelation", model.getInt("unitRelation1"));
    p1.set("retailPrice", model.getBigDecimal("retailPrice1"));
    p1.set("defaultPrice1", model.getBigDecimal("defaultPrice11"));
    p1.set("defaultPrice2", model.getBigDecimal("defaultPrice12"));
    p1.set("defaultPrice3", model.getBigDecimal("defaultPrice13"));
    p1.set("createTime", time);
    p1.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
    setDefaultUnit(p1, 1, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
    Db.use(configName).save("b_product_unit", p1);
    
    String calculateUnit2 = model.getStr("calculateUnit2");
    BigDecimal unitRelation2 = model.getBigDecimal("unitRelation2");
    

    Record p2 = new Record();
    p2.set("productId", model.getInt("id"));
    p2.set("selectUnitId", Integer.valueOf(2));
    p2.set("calculateUnit", calculateUnit2);
    p2.set("unitRelation", unitRelation2);
    p2.set("retailPrice", model.getBigDecimal("retailPrice2"));
    p2.set("defaultPrice1", model.getBigDecimal("defaultPrice21"));
    p2.set("defaultPrice2", model.getBigDecimal("defaultPrice22"));
    p2.set("defaultPrice3", model.getBigDecimal("defaultPrice23"));
    p2.set("createTime", time);
    p2.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
    setDefaultUnit(p2, 2, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
    Db.use(configName).save("b_product_unit", p2);
    
    String calculateUnit3 = model.getStr("calculateUnit3");
    BigDecimal unitRelation3 = model.getBigDecimal("unitRelation3");
    

    Record p3 = new Record();
    p3.set("productId", model.getInt("id"));
    p3.set("selectUnitId", Integer.valueOf(3));
    p3.set("calculateUnit", calculateUnit3);
    p3.set("unitRelation", unitRelation3);
    p3.set("retailPrice", model.getBigDecimal("retailPrice3"));
    p3.set("defaultPrice1", model.getBigDecimal("defaultPrice31"));
    p3.set("defaultPrice2", model.getBigDecimal("defaultPrice32"));
    p3.set("defaultPrice3", model.getBigDecimal("defaultPrice33"));
    p3.set("createTime", time);
    p3.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
    setDefaultUnit(p3, 3, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
    Db.use(configName).save("b_product_unit", p3);
  }
  
  public void delProductUnit(String configName, int productId)
  {
    Db.use(configName).update("delete from b_product_unit where productId=" + productId);
  }
  
  public void updateProductUnit(String configName, Model model, int inDefaultUnit, int stockDefaultUnit, int outDefaultUnit)
  {
    int productId = model.getInt("id").intValue();
    List<Record> list = Db.use(configName).find("select * from b_product_unit where productId=" + productId);
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Record r = (Record)list.get(i);
        if (r.getInt("selectUnitId").intValue() == 1)
        {
          r.set("calculateUnit", model.getStr("calculateUnit1"));
          r.set("unitRelation", model.getInt("unitRelation1"));
          r.set("retailPrice", model.getBigDecimal("retailPrice1"));
          r.set("defaultPrice1", model.getBigDecimal("defaultPrice11"));
          r.set("defaultPrice2", model.getBigDecimal("defaultPrice12"));
          r.set("defaultPrice3", model.getBigDecimal("defaultPrice13"));
          r.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
          setDefaultUnit(r, 1, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
          Db.use(configName).update("b_product_unit", r);
        }
        else if (r.getInt("selectUnitId").intValue() == 2)
        {
          r.set("calculateUnit", model.getStr("calculateUnit2"));
          r.set("unitRelation", model.getBigDecimal("unitRelation2"));
          r.set("retailPrice", model.getBigDecimal("retailPrice2"));
          r.set("defaultPrice1", model.getBigDecimal("defaultPrice21"));
          r.set("defaultPrice2", model.getBigDecimal("defaultPrice22"));
          r.set("defaultPrice3", model.getBigDecimal("defaultPrice23"));
          r.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
          setDefaultUnit(r, 2, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
          Db.use(configName).update("b_product_unit", r);
        }
        else if (r.getInt("selectUnitId").intValue() == 3)
        {
          r.set("calculateUnit", model.getStr("calculateUnit3"));
          r.set("unitRelation", model.getBigDecimal("unitRelation3"));
          r.set("retailPrice", model.getBigDecimal("retailPrice3"));
          r.set("defaultPrice1", model.getBigDecimal("defaultPrice31"));
          r.set("defaultPrice2", model.getBigDecimal("defaultPrice32"));
          r.set("defaultPrice3", model.getBigDecimal("defaultPrice33"));
          r.set("status", Integer.valueOf(AioConstants.STATUS_ENABLE));
          setDefaultUnit(r, 3, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
          Db.use(configName).update("b_product_unit", r);
        }
      }
    } else {
      addProductUnit(configName, model, inDefaultUnit, stockDefaultUnit, outDefaultUnit);
    }
  }
  
  public ProductUnit getObj(String configName, int productId, int selectUnitId)
  {
    String sql = "select * from b_product_unit where productId =" + productId + " and selectUnitId =" + selectUnitId;
    
    return (ProductUnit)dao.findFirst(configName, sql);
  }
}
