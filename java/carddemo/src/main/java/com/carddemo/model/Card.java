package com.carddemo.model;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** Card master record (CARDDAT / MFE.CARDDEMO.CARDDATA). Layout: copybook {@code CVACT02Y}, LRECL 150. */
@CobolRecord(copybook = "CVACT02Y", length = 150)
public class Card {

    /** {@code CARD-NUM PIC X(16)} */
    @CobolField(name = "CARD-NUM", offset = 0, length = 16, type = PicType.ALPHANUMERIC)
    private String num;

    /** {@code CARD-ACCT-ID PIC 9(11)} */
    @CobolField(name = "CARD-ACCT-ID", offset = 16, length = 11, type = PicType.UNSIGNED)
    private Long acctId;

    /** {@code CARD-CVV-CD PIC 9(03)} */
    @CobolField(name = "CARD-CVV-CD", offset = 27, length = 3, type = PicType.UNSIGNED)
    private Integer cvvCd;

    /** {@code CARD-EMBOSSED-NAME PIC X(50)} */
    @CobolField(name = "CARD-EMBOSSED-NAME", offset = 30, length = 50, type = PicType.ALPHANUMERIC)
    private String embossedName;

    /** {@code CARD-EXPIRAION-DATE PIC X(10)} */
    @CobolField(name = "CARD-EXPIRAION-DATE", offset = 80, length = 10, type = PicType.ALPHANUMERIC)
    private String expiraionDate;

    /** {@code CARD-ACTIVE-STATUS PIC X(01)} */
    @CobolField(name = "CARD-ACTIVE-STATUS", offset = 90, length = 1, type = PicType.ALPHANUMERIC)
    private String activeStatus;

    /** {@code FILLER PIC X(59)}, kept so record images round trip unchanged. */
    @CobolField(name = "FILLER", offset = 91, length = 59, type = PicType.ALPHANUMERIC)
    private String filler;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public Integer getCvvCd() {
        return cvvCd;
    }

    public void setCvvCd(Integer cvvCd) {
        this.cvvCd = cvvCd;
    }

    public String getEmbossedName() {
        return embossedName;
    }

    public void setEmbossedName(String embossedName) {
        this.embossedName = embossedName;
    }

    public String getExpiraionDate() {
        return expiraionDate;
    }

    public void setExpiraionDate(String expiraionDate) {
        this.expiraionDate = expiraionDate;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
