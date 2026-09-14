package com.carddemo.acctupdate;

import com.carddemo.acctupdate.api.FieldFlag;
import com.carddemo.acctupdate.service.edit.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class FieldEditorTest {
    private final FieldEditor editor = new FieldEditor(Clock.fixed(Instant.parse("2024-06-01T00:00:00Z"), ZoneOffset.UTC));
    @Test void editsBasicFields() {
        EditContext c = new EditContext();
        assertEquals(FieldFlag.BLANK, editor.editMandatory("Address", "", c));
        assertEquals(FieldFlag.NOT_OK, editor.editAlphaReqd("Name", "A1", c));
        assertEquals(FieldFlag.VALID, editor.editAlphaOpt("Middle", "", c));
        assertEquals(FieldFlag.NOT_OK, editor.editNumReqd("FICO", "12", 3, c));
        assertEquals(FieldFlag.VALID, editor.editYesNo("Status", "Y", c));
        assertEquals(FieldFlag.NOT_OK, editor.editSigned9V2("Amount", "abc", c));
    }
    @Test void editsDatesAndDob() {
        EditContext c = new EditContext();
        assertFalse(editor.editDateCcyymmdd("Date", "2023", "02", "29", c).isValid());
        assertTrue(editor.editDateCcyymmdd("Date", "2024", "02", "29", new EditContext()).isValid());
        assertTrue(editor.editDateCcyymmdd("Date", "2000", "02", "29", new EditContext()).isValid());
        assertFalse(editor.editDateCcyymmdd("Date", "1900", "02", "29", new EditContext()).isValid());
        assertFalse(editor.editDateCcyymmdd("Date", "2024", "04", "31", new EditContext()).isValid());
        assertEquals(FieldFlag.NOT_OK, editor.editDateOfBirth("DOB", "2024", "06", "02", new EditContext()));
    }
    @Test void editsPhoneAndLookups() {
        EditContext c = new EditContext();
        assertEquals(FieldFlag.BLANK, editor.editPhonePart("Phone.A", "", 3, true, c));
        assertEquals(FieldFlag.NOT_OK, editor.editPhonePart("Phone.A", "123", 3, true, c));
        assertEquals(FieldFlag.NOT_OK, editor.editPhonePart("Phone.A", "A12", 3, true, c));
        assertTrue(editor.stateValid("NY"));
        assertFalse(editor.stateValid("ZZ"));
        assertFalse(editor.zipStateValid("NY", "90210"));
        assertTrue(editor.zipStateValid("CA", "90210"));
    }
}
