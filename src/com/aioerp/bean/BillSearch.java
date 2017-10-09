package com.aioerp.bean;

public class BillSearch
{
  private Integer unitId;
  private Integer staffId;
  private Integer proId;
  private Integer storageId;
  private Integer depmId;
  private Integer accountId;
  private String billCode;
  private Integer billTypeId;
  private String remark;
  private String memo;
  private Integer isMember;
  private Integer isCoupon;
  private Integer priceCase;
  private Double price;
  private Integer discountCase;
  private Double discount;
  private Integer taxrateCase;
  private Double taxrate;
  private String startDate;
  private String endDate;
  
  public BillSearch() {}
  
  public BillSearch(Integer unitId, Integer staffId, Integer proId, Integer depotId, Integer depmId, Integer accountId, String billCode, Integer billTyoeId, String remark, String memo, Integer isMember, Integer isCoupon, Integer priceCase, Double price, Integer discountCase, Double discount, Integer taxrateCase, Double taxrate, String startDate, String endDate)
  {
    this.unitId = unitId;
    this.staffId = staffId;
    this.proId = proId;
    this.storageId = depotId;
    this.depmId = depmId;
    this.accountId = accountId;
    this.billCode = billCode;
    this.billTypeId = billTyoeId;
    this.remark = remark;
    this.memo = memo;
    this.isMember = isMember;
    this.isCoupon = isCoupon;
    this.priceCase = priceCase;
    this.price = price;
    this.discountCase = discountCase;
    this.discount = discount;
    this.taxrateCase = taxrateCase;
    this.taxrate = taxrate;
    this.startDate = startDate;
    this.endDate = endDate;
  }
  
  public Integer getUnitId()
  {
    return this.unitId;
  }
  
  public void setUnitId(Integer unitId)
  {
    this.unitId = unitId;
  }
  
  public Integer getStaffId()
  {
    return this.staffId;
  }
  
  public void setStaffId(Integer staffId)
  {
    this.staffId = staffId;
  }
  
  public Integer getProId()
  {
    return this.proId;
  }
  
  public void setProId(Integer proId)
  {
    this.proId = proId;
  }
  
  public Integer getDepotId()
  {
    return this.storageId;
  }
  
  public void setDepotId(Integer depotId)
  {
    this.storageId = depotId;
  }
  
  public Integer getDepmId()
  {
    return this.depmId;
  }
  
  public void setDepmId(Integer depmId)
  {
    this.depmId = depmId;
  }
  
  public Integer getAccountId()
  {
    return this.accountId;
  }
  
  public void setAccountId(Integer accountId)
  {
    this.accountId = accountId;
  }
  
  public String getBillCode()
  {
    return this.billCode;
  }
  
  public void setBillCode(String billCode)
  {
    this.billCode = billCode;
  }
  
  public Integer getBillTyoeId()
  {
    return this.billTypeId;
  }
  
  public void setBillTyoeId(Integer billTyoeId)
  {
    this.billTypeId = billTyoeId;
  }
  
  public String getRemark()
  {
    return this.remark;
  }
  
  public void setRemark(String remark)
  {
    this.remark = remark;
  }
  
  public String getMemo()
  {
    return this.memo;
  }
  
  public void setMemo(String memo)
  {
    this.memo = memo;
  }
  
  public Integer getIsMember()
  {
    return this.isMember;
  }
  
  public void setIsMember(Integer isMember)
  {
    this.isMember = isMember;
  }
  
  public Integer getIsCoupon()
  {
    return this.isCoupon;
  }
  
  public void setIsCoupon(Integer isCoupon)
  {
    this.isCoupon = isCoupon;
  }
  
  public Integer getPriceCase()
  {
    return this.priceCase;
  }
  
  public void setPriceCase(Integer priceCase)
  {
    this.priceCase = priceCase;
  }
  
  public Double getPrice()
  {
    return this.price;
  }
  
  public void setPrice(Double price)
  {
    this.price = price;
  }
  
  public Integer getDiscountCase()
  {
    return this.discountCase;
  }
  
  public void setDiscountCase(Integer discountCase)
  {
    this.discountCase = discountCase;
  }
  
  public Double getDiscount()
  {
    return this.discount;
  }
  
  public void setDiscount(Double discount)
  {
    this.discount = discount;
  }
  
  public Integer getTaxrateCase()
  {
    return this.taxrateCase;
  }
  
  public void setTaxrateCase(Integer taxrateCase)
  {
    this.taxrateCase = taxrateCase;
  }
  
  public Double getTaxrate()
  {
    return this.taxrate;
  }
  
  public void setTaxrate(Double taxrate)
  {
    this.taxrate = taxrate;
  }
  
  public String getStartDate()
  {
    return this.startDate;
  }
  
  public void setStartDate(String startDate)
  {
    this.startDate = startDate;
  }
  
  public String getEndDate()
  {
    return this.endDate;
  }
  
  public void setEndDate(String endDate)
  {
    this.endDate = endDate;
  }
}
