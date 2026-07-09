package com.carddemo.service.account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.carddemo.web.account.dto.AccountUpdateRequest;
import com.carddemo.web.account.dto.FieldValidationError;

/**
 * Ports {@code COACTUPC}'s {@code 1200-EDIT-MAP-INPUTS} field-edit orchestration: it runs
 * every field edit in the exact order the COBOL program does, using the same logical field
 * labels ({@code WS-EDIT-VARIABLE-NAME}) and producing the same messages.
 *
 * <p>The returned list holds one {@link FieldValidationError} per failing field, in edit
 * order. The first element therefore corresponds to the single {@code WS-RETURN-MSG} the
 * COBOL screen would have displayed (it only records the first message), while the full
 * list gives callers field-level detail.</p>
 */
@Component
public class AccountValidator {

    private final LookupCodes lookups;

    public AccountValidator(LookupCodes lookups) {
        this.lookups = lookups;
    }

    /** Run all field edits and collect failures in COBOL edit order. */
    public List<FieldValidationError> validate(AccountUpdateRequest r) {
        List<FieldValidationError> errors = new ArrayList<>();

        add(errors, "Account Status", FieldEditors.editYesNo("Account Status", r.acctActiveStatus()));
        add(errors, "Open Date", DateEditor.editDate("Open Date", r.openYear(), r.openMonth(), r.openDay()));
        add(errors, "Credit Limit", FieldEditors.editSigned9v2("Credit Limit", r.creditLimit()));
        add(errors, "Expiry Date", DateEditor.editDate("Expiry Date", r.expiryYear(), r.expiryMonth(), r.expiryDay()));
        add(errors, "Cash Credit Limit", FieldEditors.editSigned9v2("Cash Credit Limit", r.cashCreditLimit()));
        add(errors, "Reissue Date", DateEditor.editDate("Reissue Date", r.reissueYear(), r.reissueMonth(), r.reissueDay()));
        add(errors, "Current Balance", FieldEditors.editSigned9v2("Current Balance", r.currentBalance()));
        add(errors, "Current Cycle Credit Limit",
            FieldEditors.editSigned9v2("Current Cycle Credit Limit", r.currentCycleCredit()));
        add(errors, "Current Cycle Debit Limit",
            FieldEditors.editSigned9v2("Current Cycle Debit Limit", r.currentCycleDebit()));

        editSsn(errors, r);
        editDateOfBirth(errors, r);
        editFico(errors, r);

        add(errors, "First Name", FieldEditors.editAlphaReqd("First Name", r.firstName()));
        add(errors, "Middle Name", FieldEditors.editAlphaOpt("Middle Name", r.middleName()));
        add(errors, "Last Name", FieldEditors.editAlphaReqd("Last Name", r.lastName()));
        add(errors, "Address Line 1", FieldEditors.editMandatory("Address Line 1", r.addressLine1()));
        editState(errors, r);
        add(errors, "Zip", FieldEditors.editNumReqd("Zip", r.zipCode()));
        add(errors, "City", FieldEditors.editAlphaReqd("City", r.city()));
        add(errors, "Country", FieldEditors.editAlphaReqd("Country", r.countryCode()));
        editPhone(errors, "Phone Number 1", r.phone1Area(), r.phone1Prefix(), r.phone1Line());
        editPhone(errors, "Phone Number 2", r.phone2Area(), r.phone2Prefix(), r.phone2Line());
        add(errors, "EFT Account Id", FieldEditors.editNumReqd("EFT Account Id", r.eftAccountId()));
        add(errors, "Primary Card Holder",
            FieldEditors.editYesNo("Primary Card Holder", r.primaryCardHolderIndicator()));

        editStateZipCombo(errors, r);

        return errors;
    }

    private void add(List<FieldValidationError> errors, String field, String message) {
        if (message != null) {
            errors.add(new FieldValidationError(field, message));
        }
    }

    /** {@code 1265-EDIT-US-SSN}: three numeric parts plus the part-1 range rule. */
    private void editSsn(List<FieldValidationError> errors, AccountUpdateRequest r) {
        String part1Msg = FieldEditors.editNumReqd("SSN: First 3 chars", r.ssnPart1());
        if (part1Msg != null) {
            errors.add(new FieldValidationError("SSN: First 3 chars", part1Msg));
        } else {
            int p1 = Integer.parseInt(r.ssnPart1().trim());
            if (p1 == 0 || p1 == 666 || (p1 >= 900 && p1 <= 999)) {
                errors.add(new FieldValidationError("SSN: First 3 chars",
                    "SSN: First 3 chars: should not be 000, 666, or between 900 and 999"));
            }
        }
        add(errors, "SSN 4th & 5th chars", FieldEditors.editNumReqd("SSN 4th & 5th chars", r.ssnPart2()));
        add(errors, "SSN Last 4 chars", FieldEditors.editNumReqd("SSN Last 4 chars", r.ssnPart3()));
    }

    /** {@code EDIT-DATE-CCYYMMDD} then, when the date is valid, {@code EDIT-DATE-OF-BIRTH}. */
    private void editDateOfBirth(List<FieldValidationError> errors, AccountUpdateRequest r) {
        String dateMsg = DateEditor.editDate("Date of Birth", r.dobYear(), r.dobMonth(), r.dobDay());
        if (dateMsg != null) {
            errors.add(new FieldValidationError("Date of Birth", dateMsg));
            return;
        }
        add(errors, "Date of Birth",
            DateEditor.editDateOfBirth("Date of Birth", r.dobYear(), r.dobMonth(), r.dobDay()));
    }

    /** {@code 1245-EDIT-NUM-REQD} then, when valid, {@code 1275-EDIT-FICO-SCORE} (300-850). */
    private void editFico(List<FieldValidationError> errors, AccountUpdateRequest r) {
        String numMsg = FieldEditors.editNumReqd("FICO Score", r.ficoScore());
        if (numMsg != null) {
            errors.add(new FieldValidationError("FICO Score", numMsg));
            return;
        }
        int fico = Integer.parseInt(r.ficoScore().trim());
        if (fico < 300 || fico > 850) {
            errors.add(new FieldValidationError("FICO Score", "FICO Score: should be between 300 and 850"));
        }
    }

    /** {@code 1225-EDIT-ALPHA-REQD} then, when valid, {@code 1270-EDIT-US-STATE-CD}. */
    private void editState(List<FieldValidationError> errors, AccountUpdateRequest r) {
        String alphaMsg = FieldEditors.editAlphaReqd("State", r.stateCode());
        if (alphaMsg != null) {
            errors.add(new FieldValidationError("State", alphaMsg));
            return;
        }
        if (!lookups.isValidStateCode(r.stateCode())) {
            errors.add(new FieldValidationError("State", "State: is not a valid state code"));
        }
    }

    /** {@code 1260-EDIT-US-PHONE-NUM}: optional, but fully validated when any part is present. */
    private void editPhone(List<FieldValidationError> errors, String name,
            String area, String prefix, String line) {
        boolean allBlank = isBlank(area) && isBlank(prefix) && isBlank(line);
        if (allBlank) {
            return;
        }
        String message = editPhoneParts(name, area, prefix, line);
        if (message != null) {
            errors.add(new FieldValidationError(name, message));
        }
    }

    private String editPhoneParts(String name, String area, String prefix, String line) {
        // Area code
        if (isBlank(area)) {
            return name + ": Area code must be supplied.";
        }
        if (!isDigits(area)) {
            return name + ": Area code must be A 3 digit number.";
        }
        if (Integer.parseInt(area.trim()) == 0) {
            return name + ": Area code cannot be zero";
        }
        if (!lookups.isValidAreaCode(area)) {
            return name + ": Not valid North America general purpose area code";
        }
        // Prefix
        if (isBlank(prefix)) {
            return name + ": Prefix code must be supplied.";
        }
        if (!isDigits(prefix)) {
            return name + ": Prefix code must be A 3 digit number.";
        }
        if (Integer.parseInt(prefix.trim()) == 0) {
            return name + ": Prefix code cannot be zero";
        }
        // Line number
        if (isBlank(line)) {
            return name + ": Line number code must be supplied.";
        }
        if (!isDigits(line)) {
            return name + ": Line number code must be A 4 digit number.";
        }
        if (Integer.parseInt(line.trim()) == 0) {
            return name + ": Line number code cannot be zero";
        }
        return null;
    }

    /** {@code 1280-EDIT-US-STATE-ZIP-CD}: cross-field state + first-two-of-zip combination. */
    private void editStateZipCombo(List<FieldValidationError> errors, AccountUpdateRequest r) {
        boolean stateValid = FieldEditors.editAlphaReqd("State", r.stateCode()) == null
            && lookups.isValidStateCode(r.stateCode());
        boolean zipValid = FieldEditors.editNumReqd("Zip", r.zipCode()) == null;
        if (stateValid && zipValid && !lookups.isValidStateZip2(r.stateCode(), r.zipCode())) {
            errors.add(new FieldValidationError("Zip", "Invalid zip code for state"));
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isDigits(String value) {
        String v = value.trim();
        if (v.isEmpty()) {
            return false;
        }
        for (int i = 0; i < v.length(); i++) {
            if (!Character.isDigit(v.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** Convenience used by the service to parse a validated signed amount. */
    static BigDecimal money(String value) {
        return FieldEditors.parseSigned9v2(value);
    }
}
