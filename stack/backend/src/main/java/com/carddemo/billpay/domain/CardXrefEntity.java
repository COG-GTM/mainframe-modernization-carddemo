package com.carddemo.billpay.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * CARD_XREF table — migrated from CXACAIX AIX / copybook CVACT03Y (CARD-XREF-RECORD, RECLN 50).
 * The COBOL program reads this file by account id (the AIX key) to obtain the card number.
 */
@Entity
@Table(name = "CARD_XREF")
public class CardXrefEntity {

    @Id
    @Column(name = "XREF_CARD_NUM", length = 16, nullable = false)
    private String cardNum;

    @Column(name = "XREF_CUST_ID")
    private Long custId;

    @Column(name = "XREF_ACCT_ID")
    private Long acctId;

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public Long getCustId() { return custId; }
    public void setCustId(Long custId) { this.custId = custId; }

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
}
