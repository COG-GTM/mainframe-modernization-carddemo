package com.carddemo.acctupdate.api;

public class AccountUpdateRequest {
    private AccountUpdateDetails original;
    private AccountUpdateDetails updated;

    public AccountUpdateDetails getOriginal() {
        return original;
    }

    public void setOriginal(AccountUpdateDetails original) {
        this.original = original;
    }

    public AccountUpdateDetails getUpdated() {
        return updated;
    }

    public void setUpdated(AccountUpdateDetails updated) {
        this.updated = updated;
    }
}
