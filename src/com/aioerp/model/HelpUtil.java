package com.aioerp.model;

import java.math.BigDecimal;

public class HelpUtil
{
  private String trIndex;
  private BigDecimal costPrice;
  private int orderType;
  private int id;
  private BigDecimal lastMoney;
  private BigDecimal settlementAmount;
  
  public String getTrIndex()
  {
    return this.trIndex;
  }
  
  public void setTrIndex(String trIndex)
  {
    this.trIndex = trIndex;
  }
  
  public BigDecimal getCostPrice()
  {
    return this.costPrice;
  }
  
  public void setCostPrice(BigDecimal costPrice)
  {
    this.costPrice = costPrice;
  }
  
  public int getOrderType()
  {
    return this.orderType;
  }
  
  public void setOrderType(int orderType)
  {
    this.orderType = orderType;
  }
  
  public int getId()
  {
    return this.id;
  }
  
  public void setId(int id)
  {
    this.id = id;
  }
  
  public BigDecimal getSettlementAmount()
  {
    return this.settlementAmount;
  }
  
  public void setSettlementAmount(BigDecimal settlementAmount)
  {
    this.settlementAmount = settlementAmount;
  }
  
  public BigDecimal getLastMoney()
  {
    return this.lastMoney;
  }
  
  public void setLastMoney(BigDecimal lastMoney)
  {
    this.lastMoney = lastMoney;
  }
}
