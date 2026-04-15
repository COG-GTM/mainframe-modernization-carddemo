package com.carddemo.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * JPA entity mapped from COBOL copybook CVTRA01Y.cpy — TRAN-CAT-BAL-RECORD (50 bytes).
 * Uses a composite key of accountId + typeCode + categoryCode.
 */
@Entity
@Table(name = "transaction_category_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCategoryBalance {

    @EmbeddedId
    private TransactionCategoryBalanceId id;

    @Column(name = "balance", precision = 11, scale = 2)
    private BigDecimal balance;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Embeddable
    public static class TransactionCategoryBalanceId implements Serializable {

        @Column(name = "acct_id")
        private Long accountId;

        @Column(name = "type_code", length = 2)
        private String typeCode;

        @Column(name = "category_code")
        private Integer categoryCode;
    }
}
