package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Transaction master record.
 *
 * <p>Transpiled from copybook {@code CVTRA05Y} ({@code TRAN-RECORD}, 350 bytes), the record of the
 * {@code TRANSACT} file. CBACT04C writes one interest transaction per non-zero category balance
 * (paragraph {@code 1300-B-WRITE-TX}) with {@code TRAN-TYPE-CD='01'}, {@code TRAN-CAT-CD=0005},
 * {@code TRAN-SOURCE='System'} and description {@code 'Int. for a/c <acctId>'}.
 */
@Entity
@Table(name = "transaction")
public class Transaction {

    /** TRAN-ID PIC X(16) = PARM-DATE(10) + zero-padded suffix(6). */
    @Id
    @Column(name = "tran_id", length = 16, nullable = false)
    private String tranId;

    /** TRAN-TYPE-CD PIC X(02). */
    @Column(name = "type_cd", length = 2)
    private String typeCd;

    /** TRAN-CAT-CD PIC 9(04). */
    @Column(name = "cat_cd")
    private Integer catCd;

    /** TRAN-SOURCE PIC X(10). */
    @Column(name = "source", length = 10)
    private String source;

    /** TRAN-DESC PIC X(100). */
    @Column(name = "description", length = 100)
    private String description;

    /** TRAN-AMT PIC S9(09)V99. */
    @Column(name = "amount", precision = 11, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    /** TRAN-MERCHANT-ID PIC 9(09). */
    @Column(name = "merchant_id")
    private Long merchantId;

    /** TRAN-MERCHANT-NAME PIC X(50). */
    @Column(name = "merchant_name", length = 50)
    private String merchantName;

    /** TRAN-MERCHANT-CITY PIC X(50). */
    @Column(name = "merchant_city", length = 50)
    private String merchantCity;

    /** TRAN-MERCHANT-ZIP PIC X(10). */
    @Column(name = "merchant_zip", length = 10)
    private String merchantZip;

    /** TRAN-CARD-NUM PIC X(16). */
    @Column(name = "card_num", length = 16)
    private String cardNum;

    /** TRAN-ORIG-TS PIC X(26). */
    @Column(name = "orig_ts", length = 26)
    private String origTs;

    /** TRAN-PROC-TS PIC X(26). */
    @Column(name = "proc_ts", length = 26)
    private String procTs;

    public Transaction() {
    }

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public Integer getCatCd() {
        return catCd;
    }

    public void setCatCd(Integer catCd) {
        this.catCd = catCd;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantCity() {
        return merchantCity;
    }

    public void setMerchantCity(String merchantCity) {
        this.merchantCity = merchantCity;
    }

    public String getMerchantZip() {
        return merchantZip;
    }

    public void setMerchantZip(String merchantZip) {
        this.merchantZip = merchantZip;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getOrigTs() {
        return origTs;
    }

    public void setOrigTs(String origTs) {
        this.origTs = origTs;
    }

    public String getProcTs() {
        return procTs;
    }

    public void setProcTs(String procTs) {
        this.procTs = procTs;
    }
}
