package com.aioerp.model;

import java.math.BigDecimal;

public class StockComfirmUtil
{
  private String trIndex;
  private int storageId;
  private int productId;
  private String productCode;
  private String productFullName;
  private int costArith;
  private int selectUnitId;
  private String prdUnitName;
  private BigDecimal stockAmount;
  private BigDecimal negativeStockAmount;
  private int notStock;
  
  public String getTrIndex()
  {
    return this.trIndex;
  }
  
  public void setTrIndex(String trIndex)
  {
    this.trIndex = trIndex;
  }
  
  public String getPrdUnitName()
  {
    return this.prdUnitName;
  }
  
  public void setPrdUnitName(String prdUnitName)
  {
    this.prdUnitName = prdUnitName;
  }
  
  public int getStorageId()
  {
    return this.storageId;
  }
  
  public void setStorageId(int storageId)
  {
    this.storageId = storageId;
  }
  
  public int getProductId()
  {
    return this.productId;
  }
  
  public void setProductId(int productId)
  {
    this.productId = productId;
  }
  
  public String getProductCode()
  {
    return this.productCode;
  }
  
  public void setProductCode(String productCode)
  {
    this.productCode = productCode;
  }
  
  public String getProductFullName()
  {
    return this.productFullName;
  }
  
  public void setProductFullName(String productFullName)
  {
    this.productFullName = productFullName;
  }
  
  public int getCostArith()
  {
    return this.costArith;
  }
  
  public void setCostArith(int costArith)
  {
    this.costArith = costArith;
  }
  
  public int getSelectUnitId()
  {
    return this.selectUnitId;
  }
  
  public void setSelectUnitId(int selectUnitId)
  {
    this.selectUnitId = selectUnitId;
  }
  
  public BigDecimal getStockAmount()
  {
    return this.stockAmount;
  }
  
  public void setStockAmount(BigDecimal stockAmount)
  {
    this.stockAmount = stockAmount;
  }
  
  public BigDecimal getNegativeStockAmount()
  {
    return this.negativeStockAmount;
  }
  
  public void setNegativeStockAmount(BigDecimal negativeStockAmount)
  {
    this.negativeStockAmount = negativeStockAmount;
  }
  
  public int getNotStock()
  {
    return this.notStock;
  }
  
  public void setNotStock(int notStock)
  {
    this.notStock = notStock;
  }
}
