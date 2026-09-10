package com.carddemo.model;

import java.math.BigDecimal;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** Transaction master record (TRANSACT VSAM KSDS). Layout: copybook {@code CVTRA05Y}, LRECL 350. */
@CobolRecord(copybook = "CVTRA05Y", length = 350)
public class Transaction {

    /** {@code TRAN-ID PIC X(16)} */
    @CobolField(name = "TRAN-ID", offset = 0, length = 16, type = PicType.ALPHANUMERIC)
    private String id;

    /** {@code TRAN-TYPE-CD PIC X(02)} */
    @CobolField(name = "TRAN-TYPE-CD", offset = 16, length = 2, type = PicType.ALPHANUMERIC)
    private String typeCd;

    /** {@code TRAN-CAT-CD PIC 9(04)} */
    @CobolField(name = "TRAN-CAT-CD", offset = 18, length = 4, type = PicType.UNSIGNED)
    private Integer catCd;

    /** {@code TRAN-SOURCE PIC X(10)} */
    @CobolField(name = "TRAN-SOURCE", offset = 22, length = 10, type = PicType.ALPHANUMERIC)
    private String source;

    /** {@code TRAN-DESC PIC X(100)} */
    @CobolField(name = "TRAN-DESC", offset = 32, length = 100, type = PicType.ALPHANUMERIC)
    private String desc;

    /** {@code TRAN-AMT PIC S9(09)V99} */
    @CobolField(name = "TRAN-AMT", offset = 132, length = 11, type = PicType.SIGNED, scale = 2)
    private BigDecimal amt;

    /** {@code TRAN-MERCHANT-ID PIC 9(09)} */
    @CobolField(name = "TRAN-MERCHANT-ID", offset = 143, length = 9, type = PicType.UNSIGNED)
    private Long merchantId;

    /** {@code TRAN-MERCHANT-NAME PIC X(50)} */
    @CobolField(name = "TRAN-MERCHANT-NAME", offset = 152, length = 50, type = PicType.ALPHANUMERIC)
    private String merchantName;

    /** {@code TRAN-MERCHANT-CITY PIC X(50)} */
    @CobolField(name = "TRAN-MERCHANT-CITY", offset = 202, length = 50, type = PicType.ALPHANUMERIC)
    private String merchantCity;

    /** {@code TRAN-MERCHANT-ZIP PIC X(10)} */
    @CobolField(name = "TRAN-MERCHANT-ZIP", offset = 252, length = 10, type = PicType.ALPHANUMERIC)
    private String merchantZip;

    /** {@code TRAN-CARD-NUM PIC X(16)} */
    @CobolField(name = "TRAN-CARD-NUM", offset = 262, length = 16, type = PicType.ALPHANUMERIC)
    private String cardNum;

    /** {@code TRAN-ORIG-TS PIC X(26)} */
    @CobolField(name = "TRAN-ORIG-TS", offset = 278, length = 26, type = PicType.ALPHANUMERIC)
    private String origTs;

    /** {@code TRAN-PROC-TS PIC X(26)} */
    @CobolField(name = "TRAN-PROC-TS", offset = 304, length = 26, type = PicType.ALPHANUMERIC)
    private String procTs;

    /** {@code FILLER PIC X(20)}, kept so record images round trip unchanged. */
    @CobolField(name = "FILLER", offset = 330, length = 20, type = PicType.ALPHANUMERIC)
    private String filler;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public BigDecimal getAmt() {
        return amt;
    }

    public void setAmt(BigDecimal amt) {
        this.amt = amt;
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

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
