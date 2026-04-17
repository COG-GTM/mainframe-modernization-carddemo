package com.carddemo.transaction.dto;

import com.carddemo.transaction.entity.Transaction;
import java.math.BigDecimal;

/**
 * Response DTO for transaction data.
 * Maps fields displayed on COBOL screens COTRN0A (list) and COTRN1A (view).
 */
public class TransactionResponse {

    private String tranId;
    private String tranTypeCd;
    private Integer tranCatCd;
    private String tranSource;
    private String tranDesc;
    private BigDecimal tranAmt;
    private String tranMerchantId;
    private String tranMerchantName;
    private String tranMerchantCity;
    private String tranMerchantZip;
    private String tranCardNum;
    private String tranOrigTs;
    private String tranProcTs;

    public TransactionResponse() {
    }

    public static TransactionResponse fromEntity(Transaction entity) {
        TransactionResponse response = new TransactionResponse();
        response.setTranId(entity.getTranId());
        response.setTranTypeCd(entity.getTranTypeCd());
        response.setTranCatCd(entity.getTranCatCd());
        response.setTranSource(entity.getTranSource());
        response.setTranDesc(entity.getTranDesc());
        response.setTranAmt(entity.getTranAmt());
        response.setTranMerchantId(entity.getTranMerchantId());
        response.setTranMerchantName(entity.getTranMerchantName());
        response.setTranMerchantCity(entity.getTranMerchantCity());
        response.setTranMerchantZip(entity.getTranMerchantZip());
        response.setTranCardNum(entity.getTranCardNum());
        response.setTranOrigTs(entity.getTranOrigTs());
        response.setTranProcTs(entity.getTranProcTs());
        return response;
    }

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
