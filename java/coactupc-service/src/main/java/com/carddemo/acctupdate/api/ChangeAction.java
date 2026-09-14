package com.carddemo.acctupdate.api;

public enum ChangeAction {
    DETAILS_NOT_FETCHED(' '),
    SHOW_DETAILS('S'),
    CHANGES_NOT_OK('E'),
    CHANGES_OK_NOT_CONFIRMED('N'),
    CHANGES_OKAYED_AND_DONE('C'),
    CHANGES_OKAYED_LOCK_ERROR('L'),
    CHANGES_OKAYED_BUT_FAILED('F');

    private final char code;

    ChangeAction(char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }
}
