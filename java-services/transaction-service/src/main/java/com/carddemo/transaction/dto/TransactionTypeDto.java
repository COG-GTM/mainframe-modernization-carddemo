package com.carddemo.transaction.dto;

public class TransactionTypeDto {

    private String typeCode;
    private String description;

    public TransactionTypeDto() {
    }

    public TransactionTypeDto(String typeCode, String description) {
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
