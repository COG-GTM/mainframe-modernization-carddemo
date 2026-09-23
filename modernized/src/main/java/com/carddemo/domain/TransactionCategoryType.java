package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** CVTRA04Y - transaction category type (60 bytes). */
@Entity
@Table(name = "transaction_category_type")
public class TransactionCategoryType {

    @EmbeddedId
    private TransactionCategoryTypeId id;

    @Column(name = "tran_cat_type_desc", length = 50)
    private String description;

    public TransactionCategoryTypeId getId() {
        return id;
    }

    public void setId(TransactionCategoryTypeId id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
