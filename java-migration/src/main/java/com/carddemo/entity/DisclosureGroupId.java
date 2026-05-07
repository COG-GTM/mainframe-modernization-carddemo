package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Composite primary key for the DisclosureGroup entity.
 * <p>
 * Maps to the COBOL copybook CVTRA02Y.cpy DIS-GROUP-KEY:
 * <ul>
 *   <li>DIS-ACCT-GROUP-ID  PIC X(10) -> accountGroupId</li>
 *   <li>DIS-TRAN-TYPE-CD   PIC X(02) -> transactionTypeCode</li>
 *   <li>DIS-TRAN-CAT-CD    PIC 9(04) -> transactionCategoryCode</li>
 * </ul>
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisclosureGroupId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Account group identifier.
     * COBOL: DIS-ACCT-GROUP-ID PIC X(10)
     */
    @Column(name = "account_group_id", length = 10, nullable = false)
    private String accountGroupId;

    /**
     * Transaction type code.
     * COBOL: DIS-TRAN-TYPE-CD PIC X(02)
     */
    @Column(name = "transaction_type_code", length = 2, nullable = false)
    private String transactionTypeCode;

    /**
     * Transaction category code.
     * COBOL: DIS-TRAN-CAT-CD PIC 9(04)
     */
    @Column(name = "transaction_category_code", nullable = false)
    private Integer transactionCategoryCode;
}
