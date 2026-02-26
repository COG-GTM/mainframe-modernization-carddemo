package com.carddemo.transaction.validation;

import com.carddemo.transaction.dto.AddTransactionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that at least one of accountId or cardNumber is provided
 * in the AddTransactionRequest.
 *
 * Replaces COBOL logic:
 *   WHEN ACTIDINI NOT = SPACES AND LOW-VALUES -> use account
 *   WHEN CARDNINI NOT = SPACES AND LOW-VALUES -> use card
 *   WHEN OTHER -> error "Account or Card Number must be entered..."
 */
public class AccountOrCardValidator implements ConstraintValidator<AccountOrCard, AddTransactionRequest> {

    @Override
    public boolean isValid(AddTransactionRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean hasAccountId = request.getAccountId() != null;
        boolean hasCardNumber = request.getCardNumber() != null
                && !request.getCardNumber().isBlank();

        return hasAccountId || hasCardNumber;
    }
}
