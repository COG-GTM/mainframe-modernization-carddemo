package com.carddemo.acctupdate;

import com.carddemo.acctupdate.api.FieldFlag;
import com.carddemo.acctupdate.service.edit.EditContext;
import com.carddemo.acctupdate.service.edit.FieldEditor;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldEditorTest {
    private final FieldEditor editor = new FieldEditor(
        Clock.fixed(Instant.parse("2024-06-01T00:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void editMandatoryBlank() {
        EditContext context = new EditContext();

        assertEquals(FieldFlag.BLANK, editor.editMandatory("Address", "", context));
        assertEquals("Address must be supplied.", context.getReturnMsg());
    }

    @Test
    void editMandatoryValid() {
        assertEquals(
            FieldFlag.VALID,
            editor.editMandatory("Address", "1 Main Street", new EditContext())
        );
    }

    @Test
    void editAlphaRequiredBlank() {
        assertEquals(
            FieldFlag.BLANK,
            editor.editAlphaReqd("Name", "", new EditContext())
        );
    }

    @Test
    void editAlphaRequiredRejectsNonAlpha() {
        EditContext context = new EditContext();

        assertEquals(FieldFlag.NOT_OK, editor.editAlphaReqd("Name", "A1", context));
        assertEquals("Name can have alphabets only.", context.getReturnMsg());
    }

    @Test
    void editAlphaRequiredAllowsSpaces() {
        assertEquals(
            FieldFlag.VALID,
            editor.editAlphaReqd("Name", "Mary Ann", new EditContext())
        );
    }

    @Test
    void editAlphaOptionalBlankIsValid() {
        assertEquals(
            FieldFlag.VALID,
            editor.editAlphaOpt("Middle Name", "", new EditContext())
        );
    }

    @Test
    void editAlphaOptionalRejectsNonAlpha() {
        assertEquals(
            FieldFlag.NOT_OK,
            editor.editAlphaOpt("Middle Name", "A1", new EditContext())
        );
    }

    @Test
    void editNumericRequiredBlank() {
        assertEquals(
            FieldFlag.BLANK,
            editor.editNumReqd("FICO Score", "", 3, new EditContext())
        );
    }

    @Test
    void editNumericRequiredRejectsShortInput() {
        EditContext context = new EditContext();

        assertEquals(FieldFlag.NOT_OK, editor.editNumReqd("FICO Score", "12", 3, context));
        assertEquals("FICO Score must be all numeric.", context.getReturnMsg());
    }

    @Test
    void editNumericRequiredRejectsZero() {
        EditContext context = new EditContext();

        assertEquals(FieldFlag.NOT_OK, editor.editNumReqd("FICO Score", "000", 3, context));
        assertEquals("FICO Score must not be zero.", context.getReturnMsg());
    }

    @Test
    void editNumericRequiredAcceptsValidValue() {
        assertEquals(
            FieldFlag.VALID,
            editor.editNumReqd("FICO Score", "700", 3, new EditContext())
        );
    }

    @Test
    void editYesNoBlank() {
        assertEquals(
            FieldFlag.BLANK,
            editor.editYesNo("Status", "", new EditContext())
        );
    }

    @Test
    void editYesNoIsCaseSensitive() {
        EditContext context = new EditContext();

        assertEquals(FieldFlag.NOT_OK, editor.editYesNo("Status", "y", context));
        assertEquals("Status must be Y or N.", context.getReturnMsg());
    }

    @Test
    void editYesNoAcceptsN() {
        assertEquals(
            FieldFlag.VALID,
            editor.editYesNo("Status", "N", new EditContext())
        );
    }

    @Test
    void editSignedMoneyBlank() {
        assertEquals(
            FieldFlag.BLANK,
            editor.editSigned9V2("Amount", "", new EditContext())
        );
    }

    @Test
    void editSignedMoneyRejectsInvalidInput() {
        EditContext context = new EditContext();

        assertEquals(FieldFlag.NOT_OK, editor.editSigned9V2("Amount", "abc", context));
        assertEquals("Amount is not valid", context.getReturnMsg());
    }

    @Test
    void editSignedMoneyAcceptsCommaSeparatedInput() {
        assertEquals(
            FieldFlag.VALID,
            editor.editSigned9V2("Amount", "1,000.00", new EditContext())
        );
    }

    @Test
    void editDateRejectsInvalidCentury() {
        EditContext context = new EditContext();

        assertFalse(editor.editDateCcyymmdd("Date", "1850", "01", "01", context).isValid());
        assertEquals("Date : Century is not valid.", context.getReturnMsg());
    }

    @Test
    void editDateRejectsInvalidMonth() {
        EditContext context = new EditContext();

        assertFalse(editor.editDateCcyymmdd("Date", "2024", "13", "01", context).isValid());
        assertEquals("Date: Month must be a number between 1 and 12.", context.getReturnMsg());
    }

    @Test
    void editDateRejectsInvalidDay() {
        EditContext context = new EditContext();

        assertFalse(editor.editDateCcyymmdd("Date", "2024", "01", "32", context).isValid());
        assertEquals("Date:day must be a number between 1 and 31.", context.getReturnMsg());
    }

    @Test
    void editDateRejectsFebruary29In2023() {
        EditContext context = new EditContext();

        assertFalse(editor.editDateCcyymmdd("Date", "2023", "02", "29", context).isValid());
        assertEquals(FieldFlag.NOT_OK, context.getFieldFlags().get("Date.year"));
        assertEquals(FieldFlag.NOT_OK, context.getFieldFlags().get("Date.month"));
        assertEquals(FieldFlag.NOT_OK, context.getFieldFlags().get("Date.day"));
    }

    @Test
    void editDateAcceptsFebruary29In2024() {
        assertTrue(
            editor.editDateCcyymmdd("Date", "2024", "02", "29", new EditContext()).isValid()
        );
    }

    @Test
    void editDateAcceptsFebruary29In2000() {
        assertTrue(
            editor.editDateCcyymmdd("Date", "2000", "02", "29", new EditContext()).isValid()
        );
    }

    @Test
    void editDateRejectsFebruary29In1900() {
        EditContext context = new EditContext();

        assertFalse(editor.editDateCcyymmdd("Date", "1900", "02", "29", context).isValid());
        assertEquals("Date:Not a leap year.Cannot have 29 days in this month.", context.getReturnMsg());
    }

    @Test
    void editDateRejectsApril31() {
        EditContext context = new EditContext();

        assertFalse(editor.editDateCcyymmdd("Date", "2024", "04", "31", context).isValid());
        assertEquals("Date:Cannot have 31 days in this month.", context.getReturnMsg());
    }

    @Test
    void editDateRejectsFebruary30() {
        EditContext context = new EditContext();

        assertFalse(editor.editDateCcyymmdd("Date", "2024", "02", "30", context).isValid());
        assertEquals("Date:Cannot have 30 days in this month.", context.getReturnMsg());
    }

    @Test
    void editDateRejectsSeptember31() {
        assertFalse(
            editor.editDateCcyymmdd("Date", "2024", "09", "31", new EditContext()).isValid()
        );
    }

    @Test
    void editDobRejectsTomorrow() {
        EditContext context = new EditContext();

        assertEquals(
            FieldFlag.NOT_OK,
            editor.editDateOfBirth("DOB", "2024", "06", "02", context)
        );
        assertEquals("DOB:cannot be in the future ", context.getReturnMsg());
        assertEquals(FieldFlag.NOT_OK, context.getFieldFlags().get("DOB.year"));
        assertEquals(FieldFlag.NOT_OK, context.getFieldFlags().get("DOB.month"));
        assertEquals(FieldFlag.NOT_OK, context.getFieldFlags().get("DOB.day"));
    }

    @Test
    void editDobAcceptsYesterday() {
        assertEquals(
            FieldFlag.VALID,
            editor.editDateOfBirth("DOB", "2024", "05", "31", new EditContext())
        );
    }

    @Test
    void editPhoneRejectsBlankArea() {
        assertEquals(
            FieldFlag.BLANK,
            editor.editPhonePart("Phone.A", "", 3, true, new EditContext())
        );
    }

    @Test
    void editPhoneRejectsNonNumericArea() {
        assertEquals(
            FieldFlag.NOT_OK,
            editor.editPhonePart("Phone.A", "A12", 3, true, new EditContext())
        );
    }

    @Test
    void editPhoneRejectsZeroArea() {
        assertEquals(
            FieldFlag.NOT_OK,
            editor.editPhonePart("Phone.A", "000", 3, true, new EditContext())
        );
    }

    @Test
    void editPhoneAcceptsValidArea() {
        assertEquals(
            FieldFlag.VALID,
            editor.editPhonePart("Phone.A", "201", 3, true, new EditContext())
        );
    }

    @Test
    void editPhoneRejectsZeroPrefix() {
        assertEquals(
            FieldFlag.NOT_OK,
            editor.editPhonePart("Phone.B", "000", 3, false, new EditContext())
        );
    }

    @Test
    void editPhoneRejectsShortLine() {
        assertEquals(
            FieldFlag.NOT_OK,
            editor.editPhonePart("Phone.C", "12", 4, false, new EditContext())
        );
    }

    @Test
    void editStateRejectsUnknownCode() {
        assertFalse(editor.stateValid("ZZ"));
    }

    @Test
    void editZipRejectsNewYorkPrefix() {
        assertFalse(editor.zipStateValid("NY", "90210"));
    }

    @Test
    void editZipAcceptsCaliforniaPrefix() {
        assertTrue(editor.zipStateValid("CA", "90210"));
    }
}
