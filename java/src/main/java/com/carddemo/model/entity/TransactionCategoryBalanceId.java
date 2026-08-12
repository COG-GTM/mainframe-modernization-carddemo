package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** COBOL copybook: CVTRA01Y, TRAN-CAT-KEY group of TRAN-CAT-BAL-RECORD. */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategoryBalanceId implements Serializable {

    private static final long serialVersionUID = 1L;

    /** TRANCAT-ACCT-ID PIC 9(11). */
    @Column(name = "trancat_acct_id", nullable = false)
    private Long accountId;

    /** TRANCAT-TYPE-CD PIC X(02). */
    @Column(name = "trancat_type_cd", length = 2, nullable = false)
    private String typeCode;

    /** TRANCAT-CD PIC 9(04). */
    @Column(name = "trancat_cd", nullable = false)
    private Integer categoryCode;
}
