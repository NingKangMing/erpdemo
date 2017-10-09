package com.aioerp.model.bought;

import java.math.BigDecimal;

public class ResultHelp
{
  private Integer trIndex;
  private Integer productId;
  private String productName;
  private String productCode;
  private String productUnit;
  private BigDecimal stockAmount;
  private BigDecimal negativeAmount;
  private Integer storageId;
  private String storageCode;
  private String storageName;
  private BigDecimal avgPrice;
  
  public ResultHelp(Integer trIndex, Integer productId, String productName, String productCode, BigDecimal stockAmount, BigDecimal negativeAmount, Integer storageId, String storageCode, String storageName, BigDecimal avgPrice, String productUnit)
  {
    this.trIndex = trIndex;
    this.productId = productId;
    this.productName = productName;
    this.productCode = productCode;
    this.stockAmount = stockAmount;
    this.negativeAmount = negativeAmount;
    this.storageId = storageId;
    this.storageCode = storageCode;
    this.storageName = storageName;
    this.avgPrice = avgPrice;
    this.productUnit = productUnit;
  }
  
  public String getProductUnit()
  {
    return this.productUnit;
  }
  
  public void setProductUnit(String productUnit)
  {
    this.productUnit = productUnit;
  }
  
  public BigDecimal getAvgPrice()
  {
    return this.avgPrice;
  }
  
  public void setAvgPrice(BigDecimal avgPrice)
  {
    this.avgPrice = avgPrice;
  }
  
  public Integer getProductId()
  {
    return this.productId;
  }
  
  public void setProductId(Integer productId)
  {
    this.productId = productId;
  }
  
  public String getProductName()
  {
    return this.productName;
  }
  
  public void setProductName(String productName)
  {
    this.productName = productName;
  }
  
  public String getProductCode()
  {
    return this.productCode;
  }
  
  public void setProductCode(String productCode)
  {
    this.productCode = productCode;
  }
  
  public BigDecimal getStockAmount()
  {
    return this.stockAmount;
  }
  
  public void setStockAmount(BigDecimal stockAmount)
  {
    this.stockAmount = stockAmount;
  }
  
  public BigDecimal getNegativeAmount()
  {
    return this.negativeAmount;
  }
  
  public void setNegativeAmount(BigDecimal negativeAmount)
  {
    this.negativeAmount = negativeAmount;
  }
  
  public Integer getStorageId()
  {
    return this.storageId;
  }
  
  public void setStorageId(Integer storageId)
  {
    this.storageId = storageId;
  }
  
  public String getStorageCode()
  {
    return this.storageCode;
  }
  
  public void setStorageCode(String storageCode)
  {
    this.storageCode = storageCode;
  }
  
  public String getStorageName()
  {
    return this.storageName;
  }
  
  public void setStorageName(String storageName)
  {
    this.storageName = storageName;
  }
  
  public Integer getTrIndex()
  {
    return this.trIndex;
  }
  
  public void setTrIndex(Integer trIndex)
  {
    this.trIndex = trIndex;
  }
}
