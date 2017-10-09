package com.aioerp.bean;

import com.aioerp.util.BigDecimalUtils;
import com.jfinal.plugin.activerecord.Record;
import java.math.BigDecimal;
import java.util.Date;

public class FlowBill
{
  private Date recodeDate;
  private Integer unitId;
  private Integer staffId;
  private Integer departmentId;
  private Integer outStorageId;
  private Integer inStorageId;
  private Integer isInSck;
  private String remark;
  private String memo;
  private Date deliveryDate;
  private BigDecimal discounts;
  private BigDecimal amounts;
  private BigDecimal moneys;
  private BigDecimal discountMoneys;
  private BigDecimal taxes;
  private BigDecimal taxMoneys;
  private BigDecimal privilege;
  private BigDecimal privilegeMoney;
  private Date payDate;
  private BigDecimal retailMoneys;
  private BigDecimal payMoney;
  private BigDecimal adjustMoneys;
  private BigDecimal lastMoneys;
  
  public static void setComStorage(Record record, FlowBill fBill, String inAttr, String outAttr)
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
  
  public BigDecimal getAdjustMoneys()
  {
    return this.adjustMoneys;
  }
  
  public void setAdjustMoneys(BigDecimal adjustMoneys)
  {
    this.adjustMoneys = adjustMoneys;
  }
  
  public BigDecimal getLastMoneys()
  {
    if (this.lastMoneys == null) {
      this.lastMoneys = BigDecimalUtils.add(getMoneys(), getAdjustMoneys());
    }
    return this.lastMoneys;
  }
  
  public void setLastMoneys(BigDecimal lastMoneys)
  {
    this.lastMoneys = lastMoneys;
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
  
  public Date getPayDate()
  {
    return this.payDate;
  }
  
  public void setPayDate(Date payDate)
  {
    this.payDate = payDate;
  }
  
  public BigDecimal getRetailMoneys()
  {
    return this.retailMoneys;
  }
  
  public void setRetailMoneys(BigDecimal retailMoneys)
  {
    this.retailMoneys = retailMoneys;
  }
  
  public BigDecimal getPayMoney()
  {
    return this.payMoney;
  }
  
  public void setPayMoney(BigDecimal payMoney)
  {
    this.payMoney = payMoney;
  }
  
  public Date getRecodeDate()
  {
    return this.recodeDate;
  }
  
  public void setRecodeDate(Date recodeDate)
  {
    this.recodeDate = recodeDate;
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
  
  public Integer getDepartmentId()
  {
    return this.departmentId;
  }
  
  public void setDepartmentId(Integer departmentId)
  {
    this.departmentId = departmentId;
  }
  
  public Date getDeliveryDate()
  {
    return this.deliveryDate;
  }
  
  public void setDeliveryDate(Date deliveryDate)
  {
    this.deliveryDate = deliveryDate;
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
  
  public BigDecimal getDiscounts()
  {
    return this.discounts;
  }
  
  public void setDiscounts(BigDecimal discounts)
  {
    this.discounts = discounts;
  }
  
  public BigDecimal getAmounts()
  {
    return this.amounts;
  }
  
  public void setAmounts(BigDecimal amounts)
  {
    this.amounts = amounts;
  }
  
  public BigDecimal getMoneys()
  {
    return this.moneys;
  }
  
  public void setMoneys(BigDecimal moneys)
  {
    this.moneys = moneys;
  }
  
  public BigDecimal getDiscountMoneys()
  {
    if (this.discountMoneys == null) {
      this.discountMoneys = getMoneys();
    }
    return this.discountMoneys;
  }
  
  public void setDiscountMoneys(BigDecimal discountMoneys)
  {
    this.discountMoneys = discountMoneys;
  }
  
  public BigDecimal getTaxes()
  {
    return this.taxes;
  }
  
  public void setTaxes(BigDecimal taxes)
  {
    this.taxes = taxes;
  }
  
  public BigDecimal getTaxMoneys()
  {
    if (this.taxMoneys == null) {
      this.taxMoneys = getDiscountMoneys();
    }
    return this.taxMoneys;
  }
  
  public void setTaxMoneys(BigDecimal taxMoneys)
  {
    this.taxMoneys = taxMoneys;
  }
  
  public BigDecimal getPrivilege()
  {
    return this.privilege;
  }
  
  public void setPrivilege(BigDecimal privilege)
  {
    this.privilege = privilege;
  }
  
  public BigDecimal getPrivilegeMoney()
  {
    if (this.privilegeMoney == null) {
      this.privilegeMoney = getTaxMoneys();
    }
    return this.privilegeMoney;
  }
  
  public void setPrivilegeMoney(BigDecimal privilegeMoney)
  {
    this.privilegeMoney = privilegeMoney;
  }
}
