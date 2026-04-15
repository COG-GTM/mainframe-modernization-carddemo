package com.carddemo.transaction.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Transaction type lookup entity.
 *
 * Layout:
 *   typeCode      PIC X(02)  -> typeCode (@Id)
 *   description   PIC X(50)  -> description
 */
@Entity
@Table(name = "transaction_types")
public class TransactionType {

    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    @Column(name = "description", length = 50)
    private String description;

    public TransactionType() {
    }

    public TransactionType(String typeCode, String description) {
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
