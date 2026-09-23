package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** CVTRA03Y - transaction type (60 bytes). */
@Entity
@Table(name = "transaction_type")
public class TransactionType {

    @Id
    @Column(name = "tran_type", length = 2, nullable = false)
    private String typeCode;

    @Column(name = "tran_type_desc", length = 50)
    private String description;

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
