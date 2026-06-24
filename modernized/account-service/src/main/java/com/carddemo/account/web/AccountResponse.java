package com.carddemo.account.web;

import com.carddemo.account.model.Account;
import java.math.BigDecimal;

/**
 * REST representation of an account — the JSON face of the {@code ACCTDAT}
 * record. Money fields are serialized as exact decimals (from {@link BigDecimal}),
 * never floating point.
 */
public record AccountResponse(
        String acctId,
        String activeStatus,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currentCycleCredit,
        BigDecimal currentCycleDebit,
        String addressZip,
        String groupId) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAcctId(),
                account.getActiveStatus(),
                account.getCurrentBalance(),
                account.getCreditLimit(),
                account.getCashCreditLimit(),
                account.getOpenDate(),
                account.getExpirationDate(),
                account.getReissueDate(),
                account.getCurrentCycleCredit(),
                account.getCurrentCycleDebit(),
                account.getAddressZip(),
                account.getGroupId());
    }
}
