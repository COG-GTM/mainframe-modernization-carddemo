package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** COBOL copybook: CVTRA02Y, DIS-GROUP-KEY group of DIS-GROUP-RECORD. */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisclosureGroupId implements Serializable {

    private static final long serialVersionUID = 1L;

    /** DIS-ACCT-GROUP-ID PIC X(10). */
    @Column(name = "dis_acct_group_id", length = 10, nullable = false)
    private String accountGroupId;

    /** DIS-TRAN-TYPE-CD PIC X(02). */
    @Column(name = "dis_tran_type_cd", length = 2, nullable = false)
    private String transactionTypeCode;

    /** DIS-TRAN-CAT-CD PIC 9(04). */
    @Column(name = "dis_tran_cat_cd", nullable = false)
    private Integer transactionCategoryCode;
}
