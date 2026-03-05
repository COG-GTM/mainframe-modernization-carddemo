package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity for transaction type reference data.
 * Migrated from: app/app-transaction-type-db2/ DDL
 * Data file: trantype.txt
 */
@Entity
@Table(name = "transaction_type")
public class TransactionType {

    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    @Column(name = "type_description", length = 50)
    private String typeDescription;

    public TransactionType() {}

    public TransactionType(String typeCode, String typeDescription) {
        this.typeCode = typeCode;
        this.typeDescription = typeDescription;
    }

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getTypeDescription() { return typeDescription; }
    public void setTypeDescription(String typeDescription) { this.typeDescription = typeDescription; }
}
