package com.carddemo.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key for TransactionCategoryBalance entity.
 * Maps to the COBOL composite key: TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategoryBalanceId implements Serializable {

    private Long acctId;
    private String typeCd;
    private Integer catCd;
}
