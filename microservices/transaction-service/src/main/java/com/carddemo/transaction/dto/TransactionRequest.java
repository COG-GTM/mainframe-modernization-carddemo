package com.carddemo.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request DTO for creating a transaction.
 * Maps fields from COBOL COTRN02C input screen (COTRN2AI).
 */
public class TransactionRequest {

    @Size(max = 16)
    private String tranId;

    @NotBlank(message = "Type CD can NOT be empty")
    @Size(max = 2)
    private String tranTypeCd;

    @NotNull(message = "Category CD can NOT be empty")
    private Integer tranCatCd;

    @NotBlank(message = "Source can NOT be empty")
    @Size(max = 10)
    private String tranSource;

    @NotBlank(message = "Description can NOT be empty")
    @Size(max = 100)
    private String tranDesc;

    @NotNull(message = "Amount can NOT be empty")
    private BigDecimal tranAmt;

    @NotBlank(message = "Merchant ID can NOT be empty")
    @Size(max = 9)
    private String tranMerchantId;

    @NotBlank(message = "Merchant Name can NOT be empty")
    @Size(max = 50)
    private String tranMerchantName;

    @NotBlank(message = "Merchant City can NOT be empty")
    @Size(max = 50)
    private String tranMerchantCity;

    @NotBlank(message = "Merchant Zip can NOT be empty")
    @Size(max = 10)
    private String tranMerchantZip;

    @NotBlank(message = "Card Number must be entered")
    @Size(max = 16)
    private String tranCardNum;

    @NotBlank(message = "Orig Date can NOT be empty")
    @Size(max = 26)
    private String tranOrigTs;

    @Size(max = 26)
    private String tranProcTs;

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(Integer tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    public String getTranSource() {
        return tranSource;
    }

    public void setTranSource(String tranSource) {
        this.tranSource = tranSource;
    }

    public String getTranDesc() {
        return tranDesc;
    }

    public void setTranDesc(String tranDesc) {
        this.tranDesc = tranDesc;
    }

    public BigDecimal getTranAmt() {
        return tranAmt;
    }

    public void setTranAmt(BigDecimal tranAmt) {
        this.tranAmt = tranAmt;
    }

    public String getTranMerchantId() {
        return tranMerchantId;
    }

    public void setTranMerchantId(String tranMerchantId) {
        this.tranMerchantId = tranMerchantId;
    }

    public String getTranMerchantName() {
        return tranMerchantName;
    }

    public void setTranMerchantName(String tranMerchantName) {
        this.tranMerchantName = tranMerchantName;
    }

    public String getTranMerchantCity() {
        return tranMerchantCity;
    }

    public void setTranMerchantCity(String tranMerchantCity) {
        this.tranMerchantCity = tranMerchantCity;
    }

    public String getTranMerchantZip() {
        return tranMerchantZip;
    }

    public void setTranMerchantZip(String tranMerchantZip) {
        this.tranMerchantZip = tranMerchantZip;
    }

    public String getTranCardNum() {
        return tranCardNum;
    }

    public void setTranCardNum(String tranCardNum) {
        this.tranCardNum = tranCardNum;
    }

    public String getTranOrigTs() {
        return tranOrigTs;
    }

    public void setTranOrigTs(String tranOrigTs) {
        this.tranOrigTs = tranOrigTs;
    }

    public String getTranProcTs() {
        return tranProcTs;
    }

    public void setTranProcTs(String tranProcTs) {
        this.tranProcTs = tranProcTs;
    }
}
