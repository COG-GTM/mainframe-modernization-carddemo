package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.math.BigDecimal;

/**
 * Shared field layout for the online transaction record ({@code CVTRA05Y}) and the daily
 * transaction record ({@code CVTRA06Y}), which are byte-for-byte identical apart from the
 * primary-key field name. Concrete subclasses declare the {@code @Id}.
 */
@MappedSuperclass
public abstract class TransactionBase {

    /** TRAN-TYPE-CD PIC X(02). */
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    /** TRAN-CAT-CD PIC 9(04) — numeric category code. */
    @Column(name = "tran_cat_cd")
    private Integer tranCatCd;

    /** TRAN-SOURCE PIC X(10). */
    @Column(name = "tran_source", length = 10)
    private String tranSource;

    /** TRAN-DESC PIC X(100). */
    @Column(name = "tran_desc", length = 100)
    private String tranDesc;

    /** TRAN-AMT PIC S9(09)V99. */
    @Column(name = "tran_amt", precision = 11, scale = 2)
    private BigDecimal tranAmt;

    /** TRAN-MERCHANT-ID PIC 9(09) — kept as String to preserve leading zeros. */
    @Column(name = "tran_merchant_id", length = 9)
    private String tranMerchantId;

    /** TRAN-MERCHANT-NAME PIC X(50). */
    @Column(name = "tran_merchant_name", length = 50)
    private String tranMerchantName;

    /** TRAN-MERCHANT-CITY PIC X(50). */
    @Column(name = "tran_merchant_city", length = 50)
    private String tranMerchantCity;

    /** TRAN-MERCHANT-ZIP PIC X(10). */
    @Column(name = "tran_merchant_zip", length = 10)
    private String tranMerchantZip;

    /** TRAN-CARD-NUM PIC X(16) — logical FK to card.card_num. */
    @Column(name = "tran_card_num", length = 16)
    private String tranCardNum;

    /** TRAN-ORIG-TS PIC X(26). */
    @Column(name = "tran_orig_ts", length = 26)
    private String tranOrigTs;

    /** TRAN-PROC-TS PIC X(26). */
    @Column(name = "tran_proc_ts", length = 26)
    private String tranProcTs;

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
