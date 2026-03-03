package com.carddemo.posttran.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Maps to TRAN-RECORD (CVTRA05Y copybook).
 * Represents a posted transaction written to the TRANFILE (transactions table).
 * COBOL record length: 350 bytes.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    /** TRAN-ID PIC X(16) */
    @Id
    @Column(name = "tran_id", length = 16, nullable = false)
    private String tranId;

    /** TRAN-TYPE-CD PIC X(02) */
    @Column(name = "tran_type_cd", length = 2, nullable = false)
    private String tranTypeCd;

    /** TRAN-CAT-CD PIC 9(04) */
    @Column(name = "tran_cat_cd", nullable = false)
    private int tranCatCd;

    /** TRAN-SOURCE PIC X(10) */
    @Column(name = "tran_source", length = 10)
    private String tranSource;

    /** TRAN-DESC PIC X(100) */
    @Column(name = "tran_desc", length = 100)
    private String tranDesc;

    /** TRAN-AMT PIC S9(09)V99 */
    @Column(name = "tran_amt", precision = 11, scale = 2, nullable = false)
    private BigDecimal tranAmt;

    /** TRAN-MERCHANT-ID PIC 9(09) */
    @Column(name = "tran_merchant_id")
    private long tranMerchantId;

    /** TRAN-MERCHANT-NAME PIC X(50) */
    @Column(name = "tran_merchant_name", length = 50)
    private String tranMerchantName;

    /** TRAN-MERCHANT-CITY PIC X(50) */
    @Column(name = "tran_merchant_city", length = 50)
    private String tranMerchantCity;

    /** TRAN-MERCHANT-ZIP PIC X(10) */
    @Column(name = "tran_merchant_zip", length = 10)
    private String tranMerchantZip;

    /** TRAN-CARD-NUM PIC X(16) */
    @Column(name = "tran_card_num", length = 16, nullable = false)
    private String tranCardNum;

    /** TRAN-ORIG-TS PIC X(26) */
    @Column(name = "tran_orig_ts", length = 26)
    private String tranOrigTs;

    /** TRAN-PROC-TS PIC X(26) — set at processing time via Z-GET-DB2-FORMAT-TIMESTAMP */
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

    public int getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(int tranCatCd) {
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

    public long getTranMerchantId() {
        return tranMerchantId;
    }

    public void setTranMerchantId(long tranMerchantId) {
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
