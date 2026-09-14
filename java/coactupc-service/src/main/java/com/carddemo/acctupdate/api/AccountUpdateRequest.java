package com.carddemo.acctupdate.api;

public class AccountUpdateRequest {
    private AccountUpdateDetails original;
    private AccountUpdateDetails updated;
    public AccountUpdateDetails getOriginal() { return original; }
    public void setOriginal(AccountUpdateDetails v) { original = v; }
    public AccountUpdateDetails getUpdated() { return updated; }
    public void setUpdated(AccountUpdateDetails v) { updated = v; }
}
