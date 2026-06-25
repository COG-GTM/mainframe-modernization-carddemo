package com.carddemo.etl.validation;

import com.carddemo.etl.model.Account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies CardDemo business rules to a decoded {@link Account} before it is loaded.
 *
 * <p>Rules enforced (mirroring the constraints implied by the COBOL programs that maintain
 * {@code ACCTDAT}):
 *
 * <ul>
 *   <li>{@code ACCT-ID} must be positive.</li>
 *   <li>{@code ACCT-ACTIVE-STATUS} must be exactly {@code "Y"} or {@code "N"}.</li>
 *   <li>{@code ACCT-CREDIT-LIMIT} must be greater than or equal to {@code ACCT-CASH-CREDIT-LIMIT}
 *       (the cash advance line cannot exceed the overall credit line).</li>
 *   <li>Credit limit and cash credit limit must not be negative.</li>
 *   <li>If both open and expiration dates are present, expiration must not precede open.</li>
 * </ul>
 */
public final class AccountValidator {

    public ValidationResult validate(Account account) {
        List<String> violations = new ArrayList<>();

        if (account.acctId() <= 0) {
            violations.add("ACCT-ID must be positive but was " + account.acctId());
        }

        String status = account.activeStatus();
        if (!"Y".equals(status) && !"N".equals(status)) {
            violations.add("ACCT-ACTIVE-STATUS must be 'Y' or 'N' but was \"" + status + "\"");
        }

        BigDecimal creditLimit = account.creditLimit();
        BigDecimal cashLimit = account.cashCreditLimit();
        if (creditLimit != null && creditLimit.signum() < 0) {
            violations.add("ACCT-CREDIT-LIMIT must not be negative but was " + creditLimit);
        }
        if (cashLimit != null && cashLimit.signum() < 0) {
            violations.add("ACCT-CASH-CREDIT-LIMIT must not be negative but was " + cashLimit);
        }
        if (creditLimit != null && cashLimit != null && creditLimit.compareTo(cashLimit) < 0) {
            violations.add("ACCT-CREDIT-LIMIT (" + creditLimit
                    + ") must be >= ACCT-CASH-CREDIT-LIMIT (" + cashLimit + ")");
        }

        if (account.openDate() != null && account.expirationDate() != null
                && account.expirationDate().isBefore(account.openDate())) {
            violations.add("ACCT-EXPIRAION-DATE (" + account.expirationDate()
                    + ") must not precede ACCT-OPEN-DATE (" + account.openDate() + ")");
        }

        return new ValidationResult(violations);
    }
}
