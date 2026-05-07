package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key for TransactionCategoryBalance entity.
 * <p>
 * Maps to the COBOL composite key structure TRAN-CAT-KEY from copybook CVTRA01Y:
 * <ul>
 *   <li>TRANCAT-ACCT-ID  PIC 9(11) -> accountId (Long)</li>
 *   <li>TRANCAT-TYPE-CD  PIC X(02) -> typeCode (String)</li>
 *   <li>TRANCAT-CD       PIC 9(04) -> categoryCode (Integer)</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TransactionCategoryBalanceId implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Account identifier.
     * COBOL: TRANCAT-ACCT-ID PIC 9(11), offset 0, length 11.
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /**
     * Transaction type code.
     * COBOL: TRANCAT-TYPE-CD PIC X(02), offset 11, length 2.
     */
    @Column(name = "type_code", nullable = false, length = 2)
    private String typeCode;

    /**
     * Transaction category code.
     * COBOL: TRANCAT-CD PIC 9(04), offset 13, length 4.
     */
    @Column(name = "category_code", nullable = false)
    private Integer categoryCode;
}
