package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * JPA entity representing a credit card transaction.
 * Migrated from COBOL copybook CVTRA05Y (TRAN-RECORD, 350 bytes).
 *
 * Original VSAM file: TRANSACT (KSDS, keyed by TRAN-ID)
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "tran_id", length = 16, nullable = false)
    private String tranId;

    @Column(name = "tran_type_cd", length = 2, nullable = false)
    private String tranTypeCd;

    @Column(name = "tran_cat_cd", nullable = false)
    private Integer tranCatCd;

    @Column(name = "tran_source", length = 10, nullable = false)
    private String tranSource;

    @Column(name = "tran_desc", length = 100, nullable = false)
    private String tranDesc;

    @Column(name = "tran_amt", precision = 11, scale = 2, nullable = false)
    private BigDecimal tranAmt;

    @Column(name = "tran_merchant_id", nullable = false)
    private Integer tranMerchantId;

    @Column(name = "tran_merchant_name", length = 50, nullable = false)
    private String tranMerchantName;

    @Column(name = "tran_merchant_city", length = 50, nullable = false)
    private String tranMerchantCity;

    @Column(name = "tran_merchant_zip", length = 10, nullable = false)
    private String tranMerchantZip;

    @Column(name = "tran_card_num", length = 16, nullable = false)
    private String tranCardNum;

    @Column(name = "tran_orig_ts", length = 26)
    private String tranOrigTs;

    @Column(name = "tran_proc_ts", length = 26)
    private String tranProcTs;

    public Transaction() {
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

    public Integer getTranMerchantId() {
        return tranMerchantId;
    }

    public void setTranMerchantId(Integer tranMerchantId) {
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
