package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * JPA entity mapping the TCATBALF VSAM file record layout.
 *
 * COBOL Traceability: Maps CVTRA01Y.cpy TRAN-CAT-BAL-RECORD (RECLN = 50).
 * <pre>
 *   05 TRAN-CAT-KEY.
 *      10 TRANCAT-ACCT-ID   PIC 9(11)      -> accountId VARCHAR(11)
 *      10 TRANCAT-TYPE-CD   PIC X(02)      -> typeCode VARCHAR(2)
 *      10 TRANCAT-CD        PIC 9(04)      -> categoryCode INT
 *   05 TRAN-CAT-BAL         PIC S9(09)V99  -> balance NUMERIC(11,2)
 * </pre>
 */
@Entity
@Table(name = "category_balance")
@IdClass(CategoryBalanceEntity.CategoryBalanceId.class)
public class CategoryBalanceEntity {

    @Id
    @Column(name = "account_id", length = 11, nullable = false)
    private String accountId;

    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    @Id
    @Column(name = "category_code", nullable = false)
    private int categoryCode;

    @Column(name = "balance", precision = 11, scale = 2, nullable = false)
    private BigDecimal balance;

    public CategoryBalanceEntity() {
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(int categoryCode) {
        this.categoryCode = categoryCode;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * Composite key for category_balance table.
     * Maps COBOL composite key TRAN-CAT-KEY (TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD).
     */
    public static class CategoryBalanceId implements Serializable {
        private String accountId;
        private String typeCode;
        private int categoryCode;

        public CategoryBalanceId() {
        }

        public CategoryBalanceId(String accountId, String typeCode, int categoryCode) {
            this.accountId = accountId;
            this.typeCode = typeCode;
            this.categoryCode = categoryCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CategoryBalanceId that = (CategoryBalanceId) o;
            return categoryCode == that.categoryCode
                    && Objects.equals(accountId, that.accountId)
                    && Objects.equals(typeCode, that.typeCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, typeCode, categoryCode);
        }
    }
}
