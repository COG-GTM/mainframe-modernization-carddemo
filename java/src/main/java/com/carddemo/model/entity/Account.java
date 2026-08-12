package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL copybook: CVACT01Y (ACCOUNT-RECORD), VSAM file ACCTDATA, RECLN 300.
 */
@Entity
@Table(name = "account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /** ACCT-ID PIC 9(11). */
    @Id
    @Column(name = "acct_id", nullable = false)
    private Long accountId;

    /** ACCT-ACTIVE-STATUS PIC X(01). */
    @Column(name = "acct_active_status", length = 1)
    private String activeStatus;

    /** ACCT-CURR-BAL PIC S9(10)V99. */
    @Column(name = "acct_curr_bal", precision = 12, scale = 2)
    private BigDecimal currentBalance;

    /** ACCT-CREDIT-LIMIT PIC S9(10)V99. */
    @Column(name = "acct_credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    /** ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99. */
    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal cashCreditLimit;

    /** ACCT-OPEN-DATE PIC X(10). */
    @Column(name = "acct_open_date", length = 10)
    private String openDate;

    /** ACCT-EXPIRAION-DATE PIC X(10) (spelling as in the copybook). */
    @Column(name = "acct_expiration_date", length = 10)
    private String expirationDate;

    /** ACCT-REISSUE-DATE PIC X(10). */
    @Column(name = "acct_reissue_date", length = 10)
    private String reissueDate;

    /** ACCT-CURR-CYC-CREDIT PIC S9(10)V99. */
    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal currentCycleCredit;

    /** ACCT-CURR-CYC-DEBIT PIC S9(10)V99. */
    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal currentCycleDebit;

    /** ACCT-ADDR-ZIP PIC X(10). */
    @Column(name = "acct_addr_zip", length = 10)
    private String addressZip;

    /** ACCT-GROUP-ID PIC X(10). */
    @Column(name = "acct_group_id", length = 10)
    private String groupId;
}
