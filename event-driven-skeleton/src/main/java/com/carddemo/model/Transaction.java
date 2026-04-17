package com.carddemo.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Transaction master entity.
 *
 * Maps: TRAN-RECORD from CVTRA05Y.cpy (RECLN 350)
 * VSAM KSDS key: TRAN-ID
 *
 * Written by CBTRN02C (2900-WRITE-TRANSACTION-FILE) during posting.
 * Read by CBSTM03A via COSTM01.CPY (TRNX-RECORD) for statement generation,
 * after being re-sorted by card number in CREASTMT STEP010.
 */
@Entity
@Table(name = "transaction")
public class Transaction {

    /** TRAN-ID PIC X(16) — VSAM primary key */
    @Id
    @Column(name = "tran_id", length = 16, nullable = false)
    private String tranId;

    /** TRAN-TYPE-CD PIC X(02) */
    @Column(name = "type_cd", length = 2)
    private String typeCd;

    /** TRAN-CAT-CD PIC 9(04) */
    @Column(name = "cat_cd")
    private Integer catCd;

    /** TRAN-SOURCE PIC X(10) */
    @Column(name = "source", length = 10)
    private String source;

    /** TRAN-DESC PIC X(100) */
    @Column(name = "description", length = 100)
    private String description;

    /** TRAN-AMT PIC S9(09)V99 — transaction amount, BigDecimal for COBOL fixed-point fidelity */
    @Column(name = "amount", precision = 11, scale = 2)
    private BigDecimal amount;

    /** TRAN-MERCHANT-ID PIC 9(09) */
    @Column(name = "merchant_id")
    private Long merchantId;

    /** TRAN-MERCHANT-NAME PIC X(50) */
    @Column(name = "merchant_name", length = 50)
    private String merchantName;

    /** TRAN-MERCHANT-CITY PIC X(50) */
    @Column(name = "merchant_city", length = 50)
    private String merchantCity;

    /** TRAN-MERCHANT-ZIP PIC X(10) */
    @Column(name = "merchant_zip", length = 10)
    private String merchantZip;

    /** TRAN-CARD-NUM PIC X(16) — used to group transactions per card for statements */
    @Column(name = "card_num", length = 16)
    private String cardNum;

    /** TRAN-ORIG-TS PIC X(26) — origination timestamp from source system */
    @Column(name = "origin_timestamp", length = 26)
    private String originTimestamp;

    /** TRAN-PROC-TS PIC X(26) — processing timestamp set during posting */
    @Column(name = "process_timestamp", length = 26)
    private String processTimestamp;

    protected Transaction() {
    }

    public Transaction(String tranId, String typeCd, Integer catCd, String source,
                       String description, BigDecimal amount, Long merchantId,
                       String merchantName, String merchantCity, String merchantZip,
                       String cardNum, String originTimestamp, String processTimestamp) {
        this.tranId = tranId;
        this.typeCd = typeCd;
        this.catCd = catCd;
        this.source = source;
        this.description = description;
        this.amount = amount;
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.merchantCity = merchantCity;
        this.merchantZip = merchantZip;
        this.cardNum = cardNum;
        this.originTimestamp = originTimestamp;
        this.processTimestamp = processTimestamp;
    }

    public String getTranId() {
        return tranId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public Integer getCatCd() {
        return catCd;
    }

    public String getSource() {
        return source;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantCity() {
        return merchantCity;
    }

    public String getMerchantZip() {
        return merchantZip;
    }

    public String getCardNum() {
        return cardNum;
    }

    public String getOriginTimestamp() {
        return originTimestamp;
    }

    public String getProcessTimestamp() {
        return processTimestamp;
    }
}
