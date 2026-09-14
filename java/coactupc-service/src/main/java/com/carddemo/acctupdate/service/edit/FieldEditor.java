package com.carddemo.acctupdate.service.edit;

import com.carddemo.acctupdate.api.FieldFlag;
import com.carddemo.acctupdate.lookup.UsPhoneAreaCodes;
import com.carddemo.acctupdate.lookup.UsStateCodes;
import com.carddemo.acctupdate.lookup.UsStateZipPrefixes;
import java.time.Clock;
import java.time.LocalDate;

public class FieldEditor {
    private final Clock clock;

    public FieldEditor(Clock clock) {
        this.clock = clock;
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public FieldFlag editMandatory(String label, String value, EditContext context) {
        if (blank(value)) {
            context.flag(label, FieldFlag.BLANK, label + " must be supplied.");
            return FieldFlag.BLANK;
        }
        context.flag(label, FieldFlag.VALID, null);
        return FieldFlag.VALID;
    }

    public FieldFlag editAlphaReqd(String label, String value, EditContext context) {
        if (blank(value)) {
            context.flag(label, FieldFlag.BLANK, label + " must be supplied.");
            return FieldFlag.BLANK;
        }
        if (!value.matches("[A-Za-z ]+")) {
            context.flag(label, FieldFlag.NOT_OK, label + " can have alphabets only.");
            return FieldFlag.NOT_OK;
        }
        context.flag(label, FieldFlag.VALID, null);
        return FieldFlag.VALID;
    }

    public FieldFlag editAlphaOpt(String label, String value, EditContext context) {
        if (blank(value)) {
            context.flag(label, FieldFlag.VALID, null);
            return FieldFlag.VALID;
        }
        return editAlphaReqd(label, value, context);
    }

    public FieldFlag editNumReqd(String label, String value, int length, EditContext context) {
        String actual = value == null ? "" : value;
        String padded = String.format("%-" + length + "s", actual);
        if (blank(actual)) {
            context.flag(label, FieldFlag.BLANK, label + " must be supplied.");
            return FieldFlag.BLANK;
        }
        if (!padded.matches("[0-9]{" + length + "}")) {
            context.flag(label, FieldFlag.NOT_OK, label + " must be all numeric.");
            return FieldFlag.NOT_OK;
        }
        if (Long.parseLong(padded) == 0) {
            context.flag(label, FieldFlag.NOT_OK, label + " must not be zero.");
            return FieldFlag.NOT_OK;
        }
        context.flag(label, FieldFlag.VALID, null);
        return FieldFlag.VALID;
    }

    public FieldFlag editYesNo(String label, String value, EditContext context) {
        if (blank(value)) {
            context.flag(label, FieldFlag.BLANK, label + " must be supplied.");
            return FieldFlag.BLANK;
        }
        if (!"Y".equals(value) && !"N".equals(value)) {
            context.flag(label, FieldFlag.NOT_OK, label + " must be Y or N.");
            return FieldFlag.NOT_OK;
        }
        context.flag(label, FieldFlag.VALID, null);
        return FieldFlag.VALID;
    }

    public FieldFlag editSigned9V2(String label, String value, EditContext context) {
        if (blank(value)) {
            context.flag(label, FieldFlag.BLANK, label + " must be supplied.");
            return FieldFlag.BLANK;
        }
        if (!CobolNumeric.testNumvalC(value)) {
            context.flag(label, FieldFlag.NOT_OK, label + " is not valid");
            return FieldFlag.NOT_OK;
        }
        context.flag(label, FieldFlag.VALID, null);
        return FieldFlag.VALID;
    }

    public DateFlags editDateCcyymmdd(
        String label,
        String year,
        String month,
        String day,
        EditContext context
    ) {
        FieldFlag yearFlag = editYear(label, year, context);
        FieldFlag monthFlag = editMonth(label, month, context);
        FieldFlag dayFlag = editDay(label, day, context);

        if (yearFlag != FieldFlag.VALID
            || monthFlag != FieldFlag.VALID
            || dayFlag != FieldFlag.VALID) {
            return new DateFlags(yearFlag, monthFlag, dayFlag);
        }

        int yearValue = Integer.parseInt(year);
        int monthValue = Integer.parseInt(month);
        int dayValue = Integer.parseInt(day);

        if (dayValue == 31 && isShortMonth(monthValue)) {
            String message = label + ":Cannot have 31 days in this month.";
            context.flag(label + ".day", FieldFlag.NOT_OK, message);
            context.flag(label + ".month", FieldFlag.NOT_OK, null);
            return new DateFlags(yearFlag, FieldFlag.NOT_OK, FieldFlag.NOT_OK);
        }

        if (monthValue == 2 && dayValue == 30) {
            String message = label + ":Cannot have 30 days in this month.";
            context.flag(label + ".day", FieldFlag.NOT_OK, message);
            context.flag(label + ".month", FieldFlag.NOT_OK, null);
            return new DateFlags(yearFlag, FieldFlag.NOT_OK, FieldFlag.NOT_OK);
        }

        if (monthValue == 2 && dayValue == 29 && !java.time.Year.isLeap(yearValue)) {
            String message = label + ":Not a leap year.Cannot have 29 days in this month.";
            context.flag(label + ".year", FieldFlag.NOT_OK, message);
            context.flag(label + ".month", FieldFlag.NOT_OK, null);
            context.flag(label + ".day", FieldFlag.NOT_OK, null);
            return new DateFlags(FieldFlag.NOT_OK, FieldFlag.NOT_OK, FieldFlag.NOT_OK);
        }

        try {
            LocalDate.of(yearValue, monthValue, dayValue);
        } catch (RuntimeException exception) {
            context.flag(label + ".day", FieldFlag.NOT_OK, label + " validation error");
            context.flag(label + ".month", FieldFlag.NOT_OK, null);
            return new DateFlags(yearFlag, FieldFlag.NOT_OK, FieldFlag.NOT_OK);
        }

        return new DateFlags(yearFlag, monthFlag, dayFlag);
    }

    private FieldFlag editYear(String label, String year, EditContext context) {
        if (blank(year)) {
            context.flag(label + ".year", FieldFlag.BLANK, label + " : Year must be supplied.");
            return FieldFlag.BLANK;
        }
        if (!year.matches("\\d{4}")) {
            context.flag(label + ".year", FieldFlag.NOT_OK, label + " must be 4 digit number.");
            return FieldFlag.NOT_OK;
        }
        if (!year.startsWith("19") && !year.startsWith("20")) {
            context.flag(label + ".year", FieldFlag.NOT_OK, label + " : Century is not valid.");
            return FieldFlag.NOT_OK;
        }
        context.flag(label + ".year", FieldFlag.VALID, null);
        return FieldFlag.VALID;
    }

    private FieldFlag editMonth(String label, String month, EditContext context) {
        if (blank(month)) {
            context.flag(label + ".month", FieldFlag.BLANK, label + " : Month must be supplied.");
            return FieldFlag.BLANK;
        }
        if (!month.matches("\\d+") || Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
            context.flag(
                label + ".month",
                FieldFlag.NOT_OK,
                label + ": Month must be a number between 1 and 12."
            );
            return FieldFlag.NOT_OK;
        }
        context.flag(label + ".month", FieldFlag.VALID, null);
        return FieldFlag.VALID;
    }

    private FieldFlag editDay(String label, String day, EditContext context) {
        if (blank(day)) {
            context.flag(label + ".day", FieldFlag.BLANK, label + " : Day must be supplied.");
            return FieldFlag.BLANK;
        }
        if (!day.matches("\\d+") || Integer.parseInt(day) < 1 || Integer.parseInt(day) > 31) {
            context.flag(
                label + ".day",
                FieldFlag.NOT_OK,
                label + ":day must be a number between 1 and 31."
            );
            return FieldFlag.NOT_OK;
        }
        context.flag(label + ".day", FieldFlag.VALID, null);
        return FieldFlag.VALID;
    }

    private boolean isShortMonth(int month) {
        return month == 2 || month == 4 || month == 6 || month == 9 || month == 11;
    }

    public FieldFlag editDateOfBirth(
        String label,
        String year,
        String month,
        String day,
        EditContext context
    ) {
        try {
            LocalDate date = LocalDate.of(
                Integer.parseInt(year),
                Integer.parseInt(month),
                Integer.parseInt(day)
            );
            if (!date.isBefore(LocalDate.now(clock))) {
                flagDobParts(label, context, label + ":cannot be in the future ");
                return FieldFlag.NOT_OK;
            }
        } catch (RuntimeException exception) {
            flagDobParts(label, context, label + " validation error");
            return FieldFlag.NOT_OK;
        }
        context.flag(label, FieldFlag.VALID, null);
        return FieldFlag.VALID;
    }

    private void flagDobParts(String label, EditContext context, String message) {
        context.flag(label + ".year", FieldFlag.NOT_OK, message);
        context.flag(label + ".month", FieldFlag.NOT_OK, null);
        context.flag(label + ".day", FieldFlag.NOT_OK, null);
    }

    public FieldFlag editPhonePart(
        String label,
        String value,
        int length,
        boolean area,
        EditContext context
    ) {
        String type = area
            ? "Area code"
            : length == 3 ? "Prefix code" : "Line number code";
        if (blank(value)) {
            context.flag(label, FieldFlag.BLANK, label + ": " + type + " must be supplied.");
            return FieldFlag.BLANK;
        }
        if (!value.matches("\\d{" + length + "}")) {
            context.flag(
                label,
                FieldFlag.NOT_OK,
                label + ": " + type + " must be A " + length + " digit number."
            );
            return FieldFlag.NOT_OK;
        }
        if (Long.parseLong(value) == 0) {
            context.flag(label, FieldFlag.NOT_OK, label + ": " + type + " cannot be zero");
            return FieldFlag.NOT_OK;
        }
        if (area && !UsPhoneAreaCodes.VALID_GENERAL_PURP_CODE.contains(value)) {
            context.flag(
                label,
                FieldFlag.NOT_OK,
                label + ": Not valid North America general purpose area code"
            );
            return FieldFlag.NOT_OK;
        }
        context.flag(label, FieldFlag.VALID, null);
        return FieldFlag.VALID;
    }

    public boolean stateValid(String value) {
        return value != null && UsStateCodes.VALID_US_STATE_CODE.contains(value.trim());
    }

    public boolean zipStateValid(String state, String zip) {
        return zip != null
            && zip.length() >= 2
            && UsStateZipPrefixes.VALID_US_STATE_ZIP_CD2_COMBO.contains(state + zip.substring(0, 2));
    }
}
