package uk.co.nationwide.cards.posting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modernized form of the COBOL <code>ACCOUNT-RECORD</code> defined in copybook
 * <code>CVACT01Y.cpy</code>. Primary key <code>ACCT-ID</code> (PIC 9(11)) becomes
 * a 11-digit numeric primary key stored as Long for arithmetic-cheap joins.
 */
@Entity
@Table(name = "account")
public class Account {

    @Id
    @Column(name = "acct_id", nullable = false)
    private Long acctId;

    /** ACCT-ACTIVE-STATUS PIC X(01) — 'Y' / 'N'. */
    @Column(name = "acct_active_status", length = 1, nullable = false)
    private String acctActiveStatus;

    /** ACCT-CURR-BAL PIC S9(10)V99. */
    @Column(name = "acct_curr_bal", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCurrBal;

    @Column(name = "acct_credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCreditLimit;

    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCashCreditLimit;

    @Column(name = "acct_open_date")
    private LocalDate acctOpenDate;

    @Column(name = "acct_expiration_date")
    private LocalDate acctExpirationDate;

    @Column(name = "acct_reissue_date")
    private LocalDate acctReissueDate;

    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycCredit;

    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycDebit;

    @Column(name = "acct_addr_zip", length = 10)
    private String acctAddrZip;

    @Column(name = "acct_group_id", length = 10)
    private String acctGroupId;

    public Account() { }

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
    public String getAcctActiveStatus() { return acctActiveStatus; }
    public void setAcctActiveStatus(String acctActiveStatus) { this.acctActiveStatus = acctActiveStatus; }
    public BigDecimal getAcctCurrBal() { return acctCurrBal; }
    public void setAcctCurrBal(BigDecimal acctCurrBal) { this.acctCurrBal = acctCurrBal; }
    public BigDecimal getAcctCreditLimit() { return acctCreditLimit; }
    public void setAcctCreditLimit(BigDecimal acctCreditLimit) { this.acctCreditLimit = acctCreditLimit; }
    public BigDecimal getAcctCashCreditLimit() { return acctCashCreditLimit; }
    public void setAcctCashCreditLimit(BigDecimal acctCashCreditLimit) { this.acctCashCreditLimit = acctCashCreditLimit; }
    public LocalDate getAcctOpenDate() { return acctOpenDate; }
    public void setAcctOpenDate(LocalDate acctOpenDate) { this.acctOpenDate = acctOpenDate; }
    public LocalDate getAcctExpirationDate() { return acctExpirationDate; }
    public void setAcctExpirationDate(LocalDate acctExpirationDate) { this.acctExpirationDate = acctExpirationDate; }
    public LocalDate getAcctReissueDate() { return acctReissueDate; }
    public void setAcctReissueDate(LocalDate acctReissueDate) { this.acctReissueDate = acctReissueDate; }
    public BigDecimal getAcctCurrCycCredit() { return acctCurrCycCredit; }
    public void setAcctCurrCycCredit(BigDecimal acctCurrCycCredit) { this.acctCurrCycCredit = acctCurrCycCredit; }
    public BigDecimal getAcctCurrCycDebit() { return acctCurrCycDebit; }
    public void setAcctCurrCycDebit(BigDecimal acctCurrCycDebit) { this.acctCurrCycDebit = acctCurrCycDebit; }
    public String getAcctAddrZip() { return acctAddrZip; }
    public void setAcctAddrZip(String acctAddrZip) { this.acctAddrZip = acctAddrZip; }
    public String getAcctGroupId() { return acctGroupId; }
    public void setAcctGroupId(String acctGroupId) { this.acctGroupId = acctGroupId; }
}
