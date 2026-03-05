package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * JPA entity for card cross-reference data.
 * Migrated from COBOL copybook: CVACT03Y.cpy (CARD-XREF-RECORD)
 * VSAM file: CARDXREF (RECLN 50)
 */
@Entity
@Table(name = "card_xref", indexes = {
    @Index(name = "idx_card_xref_acct_id", columnList = "acct_id"),
    @Index(name = "idx_card_xref_cust_id", columnList = "cust_id")
})
public class CardXref {

    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "acct_id")
    private Long acctId;

    public CardXref() {}

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public Long getCustId() { return custId; }
    public void setCustId(Long custId) { this.custId = custId; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
}
