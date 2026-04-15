package com.carddemo.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Account entity mapped from COBOL copybook CVACT01Y.cpy (300-byte record).
 *
 * COBOL layout:
 *   ACCT-ID                  PIC 9(11)
 *   ACCT-ACTIVE-STATUS       PIC X(01)
 *   ACCT-CURR-BAL            PIC S9(10)V99
 *   ACCT-CREDIT-LIMIT        PIC S9(10)V99
 *   ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99
 *   ACCT-OPEN-DATE           PIC X(10)
 *   ACCT-EXPIRAION-DATE      PIC X(10)
 *   ACCT-REISSUE-DATE        PIC X(10)
 *   ACCT-CURR-CYC-CREDIT     PIC S9(10)V99
 *   ACCT-CURR-CYC-DEBIT      PIC S9(10)V99
 *   ACCT-ADDR-ZIP            PIC X(10)
 *   ACCT-GROUP-ID            PIC X(10)
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "active_status", length = 1)
    private String activeStatus;

    @Column(name = "current_balance", precision = 12, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal cashCreditLimit;

    @Column(name = "open_date", length = 10)
    private String openDate;

    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "reissue_date", length = 10)
    private String reissueDate;

    @Column(name = "current_cycle_credit", precision = 12, scale = 2)
    private BigDecimal currentCycleCredit;

    @Column(name = "current_cycle_debit", precision = 12, scale = 2)
    private BigDecimal currentCycleDebit;

    @Column(name = "address_zip", length = 10)
    private String addressZip;

    @Column(name = "group_id", length = 10)
    private String groupId;
}
