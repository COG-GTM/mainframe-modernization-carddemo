package com.carddemo.acctupdate.api;

import java.util.LinkedHashMap;
import java.util.Map;

public class AccountUpdateResponse {
    private ChangeAction action;
    private String infoMessage;
    private String errorMessage;
    private Map<String, FieldFlag> fieldFlags = new LinkedHashMap<>();
    private AccountUpdateDetails details;

    public ChangeAction getAction() {
        return action;
    }

    public void setAction(ChangeAction action) {
        this.action = action;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, FieldFlag> getFieldFlags() {
        return fieldFlags;
    }

    public void setFieldFlags(Map<String, FieldFlag> fieldFlags) {
        this.fieldFlags = fieldFlags;
    }

    public AccountUpdateDetails getDetails() {
        return details;
    }

    public void setDetails(AccountUpdateDetails details) {
        this.details = details;
    }
}
