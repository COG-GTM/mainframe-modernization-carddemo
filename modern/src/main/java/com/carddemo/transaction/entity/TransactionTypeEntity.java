package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapping the TRANTYPE VSAM file record layout.
 *
 * COBOL Traceability: Maps CVTRA03Y.cpy TRAN-TYPE-RECORD (RECLN = 60).
 * <pre>
 *   05 TRAN-TYPE       PIC X(02)  -> typeCode VARCHAR(2)
 *   05 TRAN-TYPE-DESC  PIC X(50)  -> description VARCHAR(50)
 * </pre>
 */
@Entity
@Table(name = "transaction_type")
public class TransactionTypeEntity {

    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    @Column(name = "description", length = 50, nullable = false)
    private String description;

    public TransactionTypeEntity() {
    }

    public TransactionTypeEntity(String typeCode, String description) {
        this.typeCode = typeCode;
        this.description = description;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
