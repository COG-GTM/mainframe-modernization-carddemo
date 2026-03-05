package com.carddemo.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity derived from COBOL copybook CVACT01Y.cpy (ACCOUNT-RECORD).
 *
 * <pre>
 * 01  ACCOUNT-RECORD.
 *     05  ACCT-ID                  PIC 9(11).
 *     05  ACCT-ACTIVE-STATUS       PIC X(01).
 *     05  ACCT-CURR-BAL            PIC S9(10)V99.
 *     05  ACCT-CREDIT-LIMIT        PIC S9(10)V99.
 *     05  ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99.
 *     05  ACCT-OPEN-DATE           PIC X(10).
 *     05  ACCT-EXPIRAION-DATE      PIC X(10).
 *     05  ACCT-REISSUE-DATE        PIC X(10).
 *     05  ACCT-CURR-CYC-CREDIT     PIC S9(10)V99.
 *     05  ACCT-CURR-CYC-DEBIT      PIC S9(10)V99.
 *     05  ACCT-ADDR-ZIP            PIC X(10).
 *     05  ACCT-GROUP-ID            PIC X(10).
 * </pre>
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "account_id", nullable = false)
    @NotNull
    private Long accountId;

    @Column(name = "account_status", length = 1)
    @Size(max = 1)
    private String accountStatus;

    @Column(name = "current_balance", precision = 12, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal cashCreditLimit;

    @Column(name = "open_date", length = 10)
    @Size(max = 10)
    private String openDate;

    @Column(name = "expiration_date", length = 10)
    @Size(max = 10)
    private String expirationDate;

    @Column(name = "reissue_date", length = 10)
    @Size(max = 10)
    private String reissueDate;

    @Column(name = "current_cycle_credit", precision = 12, scale = 2)
    private BigDecimal currentCycleCredit;

    @Column(name = "current_cycle_debit", precision = 12, scale = 2)
    private BigDecimal currentCycleDebit;

    @Column(name = "address_zip", length = 10)
    @Size(max = 10)
    private String addressZip;

    @Column(name = "group_id", length = 10)
    @Size(max = 10)
    private String groupId;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CardData> cardDataList = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CardCrossReference> cardCrossReferences = new ArrayList<>();
}
