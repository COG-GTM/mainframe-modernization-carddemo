package com.carddemo.acctupdate.service.edit;

import com.carddemo.acctupdate.api.FieldFlag;
import com.carddemo.acctupdate.lookup.UsPhoneAreaCodes;
import com.carddemo.acctupdate.lookup.UsStateCodes;
import com.carddemo.acctupdate.lookup.UsStateZipPrefixes;
import java.time.Clock;
import java.time.LocalDate;

public class FieldEditor {
    private final Clock clock;
    public FieldEditor(Clock clock) { this.clock = clock; }
    private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
    public FieldFlag editMandatory(String label, String value, EditContext c) {
        if (blank(value)) { c.flag(label, FieldFlag.BLANK, label + " must be supplied."); return FieldFlag.BLANK; }
        c.flag(label, FieldFlag.VALID, null); return FieldFlag.VALID;
    }
    public FieldFlag editAlphaReqd(String label, String value, EditContext c) {
        if (blank(value)) { c.flag(label, FieldFlag.BLANK, label + " must be supplied."); return FieldFlag.BLANK; }
        if (!value.matches("[A-Za-z ]+")) { c.flag(label, FieldFlag.NOT_OK, label + " can have alphabets only."); return FieldFlag.NOT_OK; }
        c.flag(label, FieldFlag.VALID, null); return FieldFlag.VALID;
    }
    public FieldFlag editAlphaOpt(String label, String value, EditContext c) {
        if (blank(value)) { c.flag(label, FieldFlag.VALID, null); return FieldFlag.VALID; }
        return editAlphaReqd(label, value, c);
    }
    public FieldFlag editNumReqd(String label, String value, int len, EditContext c) {
        String v = value == null ? "" : value;
        String padded = String.format("%-" + len + "s", v);
        if (blank(v)) { c.flag(label, FieldFlag.BLANK, label + " must be supplied."); return FieldFlag.BLANK; }
        if (!padded.matches("[0-9]{" + len + "}")) { c.flag(label, FieldFlag.NOT_OK, label + " must be all numeric."); return FieldFlag.NOT_OK; }
        if (Long.parseLong(padded) == 0) { c.flag(label, FieldFlag.NOT_OK, label + " must not be zero."); return FieldFlag.NOT_OK; }
        c.flag(label, FieldFlag.VALID, null); return FieldFlag.VALID;
    }
    public FieldFlag editYesNo(String label, String value, EditContext c) {
        if (blank(value)) { c.flag(label, FieldFlag.BLANK, label + " must be supplied."); return FieldFlag.BLANK; }
        if (!"Y".equals(value) && !"N".equals(value)) { c.flag(label, FieldFlag.NOT_OK, label + " must be Y or N."); return FieldFlag.NOT_OK; }
        c.flag(label, FieldFlag.VALID, null); return FieldFlag.VALID;
    }
    public FieldFlag editSigned9V2(String label, String value, EditContext c) {
        if (blank(value)) { c.flag(label, FieldFlag.BLANK, label + " must be supplied."); return FieldFlag.BLANK; }
        if (!CobolNumeric.testNumvalC(value)) { c.flag(label, FieldFlag.NOT_OK, label + " is not valid"); return FieldFlag.NOT_OK; }
        c.flag(label, FieldFlag.VALID, null); return FieldFlag.VALID;
    }
    public DateFlags editDateCcyymmdd(String label, String year, String month, String day, EditContext c) {
        FieldFlag yf, mf, df;
        if (blank(year)) { yf = FieldFlag.BLANK; c.flag(label + ".year", yf, label + " : Year must be supplied."); }
        else if (!year.matches("\\d{4}")) { yf = FieldFlag.NOT_OK; c.flag(label + ".year", yf, label + " must be 4 digit number."); }
        else if (!(year.startsWith("19") || year.startsWith("20"))) { yf = FieldFlag.NOT_OK; c.flag(label + ".year", yf, label + " : Century is not valid."); }
        else { yf = FieldFlag.VALID; c.flag(label + ".year", yf, null); }
        if (blank(month)) { mf = FieldFlag.BLANK; c.flag(label + ".month", mf, label + " : Month must be supplied."); }
        else if (!month.matches("\\d+") || Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) { mf = FieldFlag.NOT_OK; c.flag(label + ".month", mf, label + ": Month must be a number between 1 and 12."); }
        else { mf = FieldFlag.VALID; c.flag(label + ".month", mf, null); }
        if (blank(day)) { df = FieldFlag.BLANK; c.flag(label + ".day", df, label + " : Day must be supplied."); }
        else if (!day.matches("\\d+") || Integer.parseInt(day) < 1 || Integer.parseInt(day) > 31) { df = FieldFlag.NOT_OK; c.flag(label + ".day", df, label + ":day must be a number between 1 and 31."); }
        else { df = FieldFlag.VALID; c.flag(label + ".day", df, null); }
        if (yf == FieldFlag.VALID && mf == FieldFlag.VALID && df == FieldFlag.VALID) {
            int y = Integer.parseInt(year), m = Integer.parseInt(month), d = Integer.parseInt(day);
            String msg = null;
            if (d == 31 && (m == 2 || m == 4 || m == 6 || m == 9 || m == 11)) msg = label + ":Cannot have 31 days in this month.";
            else if (m == 2 && d == 30) msg = label + ":Cannot have 30 days in this month.";
            else if (m == 2 && d == 29 && !java.time.Year.isLeap(y)) msg = label + ":Not a leap year.Cannot have 29 days in this month.";
            else try { LocalDate.of(y, m, d); } catch (RuntimeException e) { msg = label + " is not valid"; }
            if (msg != null) { c.flag(label, FieldFlag.NOT_OK, msg); return new DateFlags(yf, mf, FieldFlag.NOT_OK); }
        }
        return new DateFlags(yf, mf, df);
    }
    public FieldFlag editDateOfBirth(String label, String year, String month, String day, EditContext c) {
        try {
            if (!LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day)).isBefore(LocalDate.now(clock))) {
                c.flag(label, FieldFlag.NOT_OK, label + ":cannot be in the future "); return FieldFlag.NOT_OK;
            }
        } catch (RuntimeException e) { c.flag(label, FieldFlag.NOT_OK, label + " is not valid"); return FieldFlag.NOT_OK; }
        c.flag(label, FieldFlag.VALID, null); return FieldFlag.VALID;
    }
    public FieldFlag editPhonePart(String label, String value, int len, boolean area, EditContext c) {
        if (blank(value)) { c.flag(label, FieldFlag.BLANK, label + ": " + (area ? "Area code" : len == 3 ? "Prefix code" : "Line number code") + " must be supplied."); return FieldFlag.BLANK; }
        if (!value.matches("\\d{" + len + "}")) { c.flag(label, FieldFlag.NOT_OK, label + ": " + (area ? "Area code must be A 3 digit number." : len == 3 ? "Prefix code must be A 3 digit number." : "Line number code must be A 4 digit number.")); return FieldFlag.NOT_OK; }
        if (Long.parseLong(value) == 0) { c.flag(label, FieldFlag.NOT_OK, label + ": " + (area ? "Area code cannot be zero" : len == 3 ? "Prefix code cannot be zero" : "Line number code cannot be zero")); return FieldFlag.NOT_OK; }
        if (area && !UsPhoneAreaCodes.VALID_GENERAL_PURP_CODE.contains(value)) { c.flag(label, FieldFlag.NOT_OK, label + ": Not valid North America general purpose area code"); return FieldFlag.NOT_OK; }
        c.flag(label, FieldFlag.VALID, null); return FieldFlag.VALID;
    }
    public boolean stateValid(String value) { return value != null && UsStateCodes.VALID_US_STATE_CODE.contains(value.trim()); }
    public boolean zipStateValid(String state, String zip) { return zip != null && zip.length() >= 2 && UsStateZipPrefixes.VALID_US_STATE_ZIP_CD2_COMBO.contains(state + zip.substring(0, 2)); }
}
