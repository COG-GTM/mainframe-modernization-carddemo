package com.carddemo.common.model;

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
 * JPA entity mapped from COBOL copybook CVACT01Y.cpy — ACCOUNT-RECORD (300 bytes).
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "acct_id")
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
