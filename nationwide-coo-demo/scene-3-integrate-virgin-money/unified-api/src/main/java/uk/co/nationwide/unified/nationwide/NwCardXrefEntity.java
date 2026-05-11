package uk.co.nationwide.unified.nationwide;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Nationwide card cross-reference, mapped from copybook
 * <code>CVACT03Y.cpy</code> (CARD-XREF-RECORD).
 */
@Entity
@Table(name = "nw_card_xref")
public class NwCardXrefEntity {

    @Id
    @Column(name = "xref_card_num", length = 16)
    private String xrefCardNum;

    @Column(name = "xref_cust_id", nullable = false)
    private Long xrefCustId;

    @Column(name = "xref_acct_id", nullable = false)
    private Long xrefAcctId;

    public String getXrefCardNum() { return xrefCardNum; }
    public void setXrefCardNum(String xrefCardNum) { this.xrefCardNum = xrefCardNum; }
    public Long getXrefCustId() { return xrefCustId; }
    public void setXrefCustId(Long xrefCustId) { this.xrefCustId = xrefCustId; }
    public Long getXrefAcctId() { return xrefAcctId; }
    public void setXrefAcctId(Long xrefAcctId) { this.xrefAcctId = xrefAcctId; }
}
