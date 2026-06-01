package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Card / account / customer cross-reference.
 *
 * <p>Transpiled from copybook {@code CVACT03Y} ({@code CARD-XREF-RECORD}, 50 bytes), the record of
 * the {@code XREFFILE} VSAM KSDS. The KSDS primary key is the card number; CBACT04C reads it
 * through the alternate index on account id ({@code CXACAIX}). The AIX is modelled here as a
 * secondary index on {@code acct_id} (see {@link com.carddemo.repository.CardXrefRepository}).
 *
 * <pre>
 * 05 XREF-CARD-NUM PIC X(16) -> cardNum (primary key)
 * 05 XREF-CUST-ID  PIC 9(09) -> custId
 * 05 XREF-ACCT-ID  PIC 9(11) -> acctId  (alternate key)
 * </pre>
 */
@Entity
@Table(name = "card_xref", indexes = @Index(name = "ix_card_xref_acct_id", columnList = "acct_id"))
public class CardXref {

    /** XREF-CARD-NUM PIC X(16). */
    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    /** XREF-CUST-ID PIC 9(09). */
    @Column(name = "cust_id", nullable = false)
    private Long custId;

    /** XREF-ACCT-ID PIC 9(11) - alternate index (CXACAIX). */
    @Column(name = "acct_id", nullable = false)
    private Long acctId;

    public CardXref() {
    }

    public CardXref(String cardNum, Long custId, Long acctId) {
        this.cardNum = cardNum;
        this.custId = custId;
        this.acctId = acctId;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }
}
