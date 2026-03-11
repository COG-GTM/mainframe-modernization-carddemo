package com.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransactionRequest {
    @NotBlank private String cardNum;
    @NotBlank private String tranTypeCd;
    @NotNull private Integer tranCatCd;
    private String tranDesc;
    @NotNull private BigDecimal tranAmt;
    private Long merchantId;
    private String merchantName;
    private String merchantCity;
    private String merchantZip;
    public TransactionRequest() {}
    public String getCardNum() { return cardNum; }
    public void setCardNum(String v) { this.cardNum = v; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String v) { this.tranTypeCd = v; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer v) { this.tranCatCd = v; }
    public String getTranDesc() { return tranDesc; }
    public void setTranDesc(String v) { this.tranDesc = v; }
    public BigDecimal getTranAmt() { return tranAmt; }
    public void setTranAmt(BigDecimal v) { this.tranAmt = v; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long v) { this.merchantId = v; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String v) { this.merchantName = v; }
    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String v) { this.merchantCity = v; }
    public String getMerchantZip() { return merchantZip; }
    public void setMerchantZip(String v) { this.merchantZip = v; }
}
