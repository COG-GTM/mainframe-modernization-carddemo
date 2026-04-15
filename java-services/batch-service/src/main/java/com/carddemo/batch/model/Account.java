package com.carddemo.batch.model;

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
 * Account entity mapped from COBOL copybook CVACT01Y (ACCOUNT-RECORD).
 * Original COBOL record length: 300 bytes.
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
    private Long acctId;

    @Column(name = "active_status", length = 1)
    private String activeStatus;

    @Column(name = "curr_bal", precision = 12, scale = 2)
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

    @Column(name = "curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal currentCycleCredit;

    @Column(name = "curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal currentCycleDebit;

    @Column(name = "addr_zip", length = 10)
    private String addrZip;

    @Column(name = "group_id", length = 10)
    private String groupId;
}
