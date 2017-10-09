package com.aioerp.bean;

import com.aioerp.model.base.Product;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.DwzUtils;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.util.Date;

public class FlowDetail
{
  private Integer productId;
  private Integer outStorageId;
  private Integer inStorageId;
  private Integer isInSck;
  private Integer selectUnitId;
  private BigDecimal amount;
  private BigDecimal price;
  private BigDecimal money;
  private String batch;
  private Date produceDate;
  private BigDecimal baseAmount;
  private BigDecimal basePrice;
  private String memo;
  private String message;
  private Integer status;
  private BigDecimal discount;
  private BigDecimal discountPrice;
  private BigDecimal discountMoney;
  private BigDecimal taxRate;
  private BigDecimal taxPrice;
  private BigDecimal taxes;
  private BigDecimal taxMoney;
  private BigDecimal retailPrice;
  private BigDecimal retailMoney;
  private BigDecimal costPrice;
  private BigDecimal currUnitCostPrice;
  private BigDecimal giftAmount;
  private BigDecimal costMoneys;
  private BigDecimal lastPrice;
  private BigDecimal adjustMoney;
  private BigDecimal lastMoney;
  
  public static void setComStorage(Record record, FlowDetail fBill, String inAttr, String outAttr)
  {
    if ((inAttr == null) && (outAttr == null)) {
      return;
    }
    String attr = "";
    if (inAttr == null) {
      attr = outAttr;
    }
    if (outAttr == null) {
      attr = inAttr;
    }
    if (fBill.getIsInSck().intValue() == 1)
    {
      record.set(attr, fBill.getInStorageId());
    }
    else if (fBill.getIsInSck().intValue() == -1)
    {
      record.set(attr, fBill.getOutStorageId());
    }
    else if (fBill.getIsInSck().intValue() == 2)
    {
      if (inAttr != null) {
        record.set(inAttr, fBill.getInStorageId());
      }
      if (outAttr != null) {
        record.set(outAttr, fBill.getOutStorageId());
      }
    }
  }
  
  public BigDecimal getCurrUnitCostPrice(String configName)
  {
    Product pro = (Product)Product.dao.findById(configName, this.productId);
    this.currUnitCostPrice = DwzUtils.getConversionPrice(this.costPrice, Integer.valueOf(1), pro.getInt("unitRelation1"), pro.getBigDecimal("unitRelation2"), pro.getBigDecimal("calculateUnit3"), this.selectUnitId);
    return this.currUnitCostPrice;
  }
  
  public void setCurrUnitCostPrice(BigDecimal currUnitCostPrice)
  {
    this.currUnitCostPrice = currUnitCostPrice;
  }
  
  public BigDecimal getLastPrice(String configName)
  {
    if (this.lastPrice == null) {
      this.lastPrice = BigDecimalUtils.div(getMoney(), getBaseAmount(configName));
    }
    return this.lastPrice;
  }
  
  public void setLastPrice(BigDecimal lastPrice)
  {
    this.lastPrice = lastPrice;
  }
  
  public BigDecimal getAdjustMoney()
  {
    return this.adjustMoney;
  }
  
  public void setAdjustMoney(BigDecimal adjustMoney)
  {
    this.adjustMoney = adjustMoney;
  }
  
  public BigDecimal getLastMoney()
  {
    if (this.lastMoney == null) {
      this.lastMoney = BigDecimalUtils.add(getMoney(), getAdjustMoney());
    }
    return this.lastMoney;
  }
  
  public void setLastMoney(BigDecimal lastMoney)
  {
    this.lastMoney = lastMoney;
  }
  
  public Integer getIsInSck()
  {
    if ((this.outStorageId != null) && (this.inStorageId != null)) {
      this.isInSck = Integer.valueOf(2);
    } else if (this.outStorageId != null) {
      this.isInSck = Integer.valueOf(-1);
    } else if (this.inStorageId != null) {
      this.isInSck = Integer.valueOf(1);
    } else {
      this.isInSck = Integer.valueOf(1);
    }
    return this.isInSck;
  }
  
  public void setIsInSck(Integer isInSck)
  {
    this.isInSck = isInSck;
  }
  
  public Integer getInStorageId()
  {
    return this.inStorageId;
  }
  
  public void setInStorageId(Integer inStorageId)
  {
    this.inStorageId = inStorageId;
  }
  
  public Integer getOutStorageId()
  {
    return this.outStorageId;
  }
  
  public void setOutStorageId(Integer outStorageId)
  {
    this.outStorageId = outStorageId;
  }
  
  public BigDecimal getDiscount()
  {
    if (this.discount == null) {
      this.discount = new BigDecimal(1);
    }
    return this.discount;
  }
  
  public void setDiscount(BigDecimal discount)
  {
    this.discount = discount;
  }
  
  public BigDecimal getDiscountPrice()
  {
    if (this.discountPrice == null) {
      this.discountPrice = getPrice();
    }
    return this.discountPrice;
  }
  
  public void setDiscountPrice(BigDecimal discountPrice)
  {
    this.discountPrice = discountPrice;
  }
  
  public BigDecimal getDiscountMoney()
  {
    if (this.discountMoney == null) {
      this.discountMoney = getMoney();
    }
    return this.discountMoney;
  }
  
  public void setDiscountMoney(BigDecimal discountMoney)
  {
    this.discountMoney = discountMoney;
  }
  
  public BigDecimal getTaxRate()
  {
    return this.taxRate;
  }
  
  public void setTaxRate(BigDecimal taxRate)
  {
    this.taxRate = taxRate;
  }
  
  public BigDecimal getTaxPrice()
  {
    if (this.taxPrice == null) {
      this.taxPrice = getDiscountPrice();
    }
    return this.taxPrice;
  }
  
  public void setTaxPrice(BigDecimal taxPrice)
  {
    this.taxPrice = taxPrice;
  }
  
  public BigDecimal getTaxes()
  {
    return this.taxes;
  }
  
  public void setTaxes(BigDecimal taxes)
  {
    this.taxes = taxes;
  }
  
  public BigDecimal getTaxMoney()
  {
    if (this.taxMoney == null) {
      this.taxMoney = getDiscountMoney();
    }
    return this.taxMoney;
  }
  
  public void setTaxMoney(BigDecimal taxMoney)
  {
    this.taxMoney = taxMoney;
  }
  
  public String getMessage()
  {
    return this.message;
  }
  
  public void setMessage(String message)
  {
    this.message = message;
  }
  
  public BigDecimal getRetailPrice()
  {
    return this.retailPrice;
  }
  
  public void setRetailPrice(BigDecimal retailPrice)
  {
    this.retailPrice = retailPrice;
  }
  
  public BigDecimal getRetailMoney()
  {
    return this.retailMoney;
  }
  
  public void setRetailMoney(BigDecimal retailMoney)
  {
    this.retailMoney = retailMoney;
  }
  
  public BigDecimal getCostPrice()
  {
    return this.costPrice;
  }
  
  public void setCostPrice(BigDecimal costPrice)
  {
    this.costPrice = costPrice;
  }
  
  public BigDecimal getGiftAmount(String configName)
  {
    if ((this.giftAmount == null) && (!BigDecimalUtils.notNullZero(getPrice()))) {
      this.giftAmount = getBaseAmount(configName);
    }
    return this.giftAmount;
  }
  
  public void setGiftAmount(BigDecimal giftAmount)
  {
    this.giftAmount = giftAmount;
  }
  
  public Integer getProductId()
  {
    return this.productId;
  }
  
  public void setProductId(Integer productId)
  {
    this.productId = productId;
  }
  
  public Integer getSelectUnitId()
  {
    return this.selectUnitId;
  }
  
  public void setSelectUnitId(Integer selectUnitId)
  {
    this.selectUnitId = selectUnitId;
  }
  
  public BigDecimal getAmount()
  {
    return this.amount;
  }
  
  public void setAmount(BigDecimal amount)
  {
    this.amount = amount;
  }
  
  public BigDecimal getPrice()
  {
    return this.price;
  }
  
  public void setPrice(BigDecimal price)
  {
    this.price = price;
  }
  
  public BigDecimal getMoney()
  {
    return this.money;
  }
  
  public void setMoney(BigDecimal money)
  {
    this.money = money;
  }
  
  public String getBatch()
  {
    return this.batch;
  }
  
  public void setBatch(String batch)
  {
    this.batch = batch;
  }
  
  public Date getProduceDate()
  {
    return this.produceDate;
  }
  
  public void setProduceDate(Date produceDate)
  {
    this.produceDate = produceDate;
  }
  
  public BigDecimal getBaseAmount(String configName)
  {
    if (this.baseAmount == null)
    {
      Product pro = (Product)Product.dao.findById(configName, this.productId);
      this.baseAmount = DwzUtils.getConversionAmount(this.amount, this.selectUnitId, pro, Integer.valueOf(1));
    }
    return this.baseAmount;
  }
  
  public void setBaseAmount(BigDecimal baseAmount)
  {
    this.baseAmount = baseAmount;
  }
  
  public BigDecimal getBasePrice()
  {
    return this.basePrice;
  }
  
  public void setBasePrice(BigDecimal basePrice)
  {
    this.basePrice = basePrice;
  }
  
  public String getMemo()
  {
    return this.memo;
  }
  
  public void setMemo(String memo)
  {
    this.memo = memo;
  }
  
  public Integer getStatus()
  {
    return this.status;
  }
  
  public void setStatus(Integer status)
  {
    this.status = status;
  }
  
  public BigDecimal getCostMoneys()
  {
    return this.costMoneys;
  }
  
  public void setCostMoneys(BigDecimal costMoneys)
  {
    this.costMoneys = costMoneys;
  }
}
