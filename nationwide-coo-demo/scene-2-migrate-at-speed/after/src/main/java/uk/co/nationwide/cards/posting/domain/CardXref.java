package uk.co.nationwide.cards.posting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Modernized form of the COBOL <code>CARD-XREF-RECORD</code> defined in copybook
 * <code>CVACT03Y.cpy</code>. Maps a 16-char card number to the owning customer
 * and account.
 *
 * <p>In the legacy estate this is one of the most-depended-upon VSAM KSDS
 * datasets (see scene-1 dependency graph).
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "xref_card_num", length = 16, nullable = false)
    private String xrefCardNum;

    @Column(name = "xref_cust_id", nullable = false)
    private Long xrefCustId;

    @Column(name = "xref_acct_id", nullable = false)
    private Long xrefAcctId;

    public CardXref() { }

    public String getXrefCardNum() { return xrefCardNum; }
    public void setXrefCardNum(String xrefCardNum) { this.xrefCardNum = xrefCardNum; }
    public Long getXrefCustId() { return xrefCustId; }
    public void setXrefCustId(Long xrefCustId) { this.xrefCustId = xrefCustId; }
    public Long getXrefAcctId() { return xrefAcctId; }
    public void setXrefAcctId(Long xrefAcctId) { this.xrefAcctId = xrefAcctId; }
}
