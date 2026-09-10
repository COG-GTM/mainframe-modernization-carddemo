package com.carddemo.model;

import java.math.BigDecimal;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** Daily (unposted) transaction record (DALYTRAN). Layout: copybook {@code CVTRA06Y}, LRECL 350. */
@CobolRecord(copybook = "CVTRA06Y", length = 350)
public class DailyTransaction {

    /** {@code DALYTRAN-ID PIC X(16)} */
    @CobolField(name = "DALYTRAN-ID", offset = 0, length = 16, type = PicType.ALPHANUMERIC)
    private String id;

    /** {@code DALYTRAN-TYPE-CD PIC X(02)} */
    @CobolField(name = "DALYTRAN-TYPE-CD", offset = 16, length = 2, type = PicType.ALPHANUMERIC)
    private String typeCd;

    /** {@code DALYTRAN-CAT-CD PIC 9(04)} */
    @CobolField(name = "DALYTRAN-CAT-CD", offset = 18, length = 4, type = PicType.UNSIGNED)
    private Integer catCd;

    /** {@code DALYTRAN-SOURCE PIC X(10)} */
    @CobolField(name = "DALYTRAN-SOURCE", offset = 22, length = 10, type = PicType.ALPHANUMERIC)
    private String source;

    /** {@code DALYTRAN-DESC PIC X(100)} */
    @CobolField(name = "DALYTRAN-DESC", offset = 32, length = 100, type = PicType.ALPHANUMERIC)
    private String desc;

    /** {@code DALYTRAN-AMT PIC S9(09)V99} */
    @CobolField(name = "DALYTRAN-AMT", offset = 132, length = 11, type = PicType.SIGNED, scale = 2)
    private BigDecimal amt;

    /** {@code DALYTRAN-MERCHANT-ID PIC 9(09)} */
    @CobolField(name = "DALYTRAN-MERCHANT-ID", offset = 143, length = 9, type = PicType.UNSIGNED)
    private Long merchantId;

    /** {@code DALYTRAN-MERCHANT-NAME PIC X(50)} */
    @CobolField(name = "DALYTRAN-MERCHANT-NAME", offset = 152, length = 50, type = PicType.ALPHANUMERIC)
    private String merchantName;

    /** {@code DALYTRAN-MERCHANT-CITY PIC X(50)} */
    @CobolField(name = "DALYTRAN-MERCHANT-CITY", offset = 202, length = 50, type = PicType.ALPHANUMERIC)
    private String merchantCity;

    /** {@code DALYTRAN-MERCHANT-ZIP PIC X(10)} */
    @CobolField(name = "DALYTRAN-MERCHANT-ZIP", offset = 252, length = 10, type = PicType.ALPHANUMERIC)
    private String merchantZip;

    /** {@code DALYTRAN-CARD-NUM PIC X(16)} */
    @CobolField(name = "DALYTRAN-CARD-NUM", offset = 262, length = 16, type = PicType.ALPHANUMERIC)
    private String cardNum;

    /** {@code DALYTRAN-ORIG-TS PIC X(26)} */
    @CobolField(name = "DALYTRAN-ORIG-TS", offset = 278, length = 26, type = PicType.ALPHANUMERIC)
    private String origTs;

    /** {@code DALYTRAN-PROC-TS PIC X(26)} */
    @CobolField(name = "DALYTRAN-PROC-TS", offset = 304, length = 26, type = PicType.ALPHANUMERIC)
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
