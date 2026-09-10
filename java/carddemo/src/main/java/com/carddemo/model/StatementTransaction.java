package com.carddemo.model;

import java.math.BigDecimal;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** Transaction layout keyed by card number, used by statement reporting. Layout: copybook {@code COSTM01}, LRECL 350. */
@CobolRecord(copybook = "COSTM01", length = 350)
public class StatementTransaction {

    /** {@code TRNX-CARD-NUM PIC X(16)} */
    @CobolField(name = "TRNX-CARD-NUM", offset = 0, length = 16, type = PicType.ALPHANUMERIC)
    private String cardNum;

    /** {@code TRNX-ID PIC X(16)} */
    @CobolField(name = "TRNX-ID", offset = 16, length = 16, type = PicType.ALPHANUMERIC)
    private String id;

    /** {@code TRNX-TYPE-CD PIC X(02)} */
    @CobolField(name = "TRNX-TYPE-CD", offset = 32, length = 2, type = PicType.ALPHANUMERIC)
    private String typeCd;

    /** {@code TRNX-CAT-CD PIC 9(04)} */
    @CobolField(name = "TRNX-CAT-CD", offset = 34, length = 4, type = PicType.UNSIGNED)
    private Integer catCd;

    /** {@code TRNX-SOURCE PIC X(10)} */
    @CobolField(name = "TRNX-SOURCE", offset = 38, length = 10, type = PicType.ALPHANUMERIC)
    private String source;

    /** {@code TRNX-DESC PIC X(100)} */
    @CobolField(name = "TRNX-DESC", offset = 48, length = 100, type = PicType.ALPHANUMERIC)
    private String desc;

    /** {@code TRNX-AMT PIC S9(09)V99} */
    @CobolField(name = "TRNX-AMT", offset = 148, length = 11, type = PicType.SIGNED, scale = 2)
    private BigDecimal amt;

    /** {@code TRNX-MERCHANT-ID PIC 9(09)} */
    @CobolField(name = "TRNX-MERCHANT-ID", offset = 159, length = 9, type = PicType.UNSIGNED)
    private Long merchantId;

    /** {@code TRNX-MERCHANT-NAME PIC X(50)} */
    @CobolField(name = "TRNX-MERCHANT-NAME", offset = 168, length = 50, type = PicType.ALPHANUMERIC)
    private String merchantName;

    /** {@code TRNX-MERCHANT-CITY PIC X(50)} */
    @CobolField(name = "TRNX-MERCHANT-CITY", offset = 218, length = 50, type = PicType.ALPHANUMERIC)
    private String merchantCity;

    /** {@code TRNX-MERCHANT-ZIP PIC X(10)} */
    @CobolField(name = "TRNX-MERCHANT-ZIP", offset = 268, length = 10, type = PicType.ALPHANUMERIC)
    private String merchantZip;

    /** {@code TRNX-ORIG-TS PIC X(26)} */
    @CobolField(name = "TRNX-ORIG-TS", offset = 278, length = 26, type = PicType.ALPHANUMERIC)
    private String origTs;

    /** {@code TRNX-PROC-TS PIC X(26)} */
    @CobolField(name = "TRNX-PROC-TS", offset = 304, length = 26, type = PicType.ALPHANUMERIC)
    private String procTs;

    /** {@code FILLER PIC X(20)}, kept so record images round trip unchanged. */
    @CobolField(name = "FILLER", offset = 330, length = 20, type = PicType.ALPHANUMERIC)
    private String filler;

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

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
