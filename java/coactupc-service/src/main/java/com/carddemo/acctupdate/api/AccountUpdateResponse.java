package com.carddemo.acctupdate.api;

import java.util.LinkedHashMap;
import java.util.Map;

public class AccountUpdateResponse {
    private ChangeAction action;
    private String infoMessage;
    private String errorMessage;
    private Map<String, FieldFlag> fieldFlags = new LinkedHashMap<>();
    private AccountUpdateDetails details;
    public ChangeAction getAction() { return action; } public void setAction(ChangeAction v) { action = v; }
    public String getInfoMessage() { return infoMessage; } public void setInfoMessage(String v) { infoMessage = v; }
    public String getErrorMessage() { return errorMessage; } public void setErrorMessage(String v) { errorMessage = v; }
    public Map<String, FieldFlag> getFieldFlags() { return fieldFlags; } public void setFieldFlags(Map<String, FieldFlag> v) { fieldFlags = v; }
    public AccountUpdateDetails getDetails() { return details; } public void setDetails(AccountUpdateDetails v) { details = v; }
}
