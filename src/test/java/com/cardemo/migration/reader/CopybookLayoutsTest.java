package com.cardemo.migration.reader;

import com.cardemo.migration.model.CopybookField;
import com.cardemo.migration.model.CopybookLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CopybookLayouts - verifies all copybook layout definitions
 * have correct field offsets, lengths, and total record sizes.
 */
class CopybookLayoutsTest {

    @Test
    @DisplayName("CVACT01Y account layout has correct record length 300")
    void accountLayoutRecordLength() {
        CopybookLayout layout = CopybookLayouts.CVACT01Y;
        assertEquals(300, layout.recordLength());
        assertEquals("CVACT01Y", layout.name());
    }

    @Test
    @DisplayName("CVACT01Y fields sum to record length 300")
    void accountLayoutFieldsSum() {
        CopybookLayout layout = CopybookLayouts.CVACT01Y;
        int totalBytes = layout.fields().stream().mapToInt(CopybookField::length).sum();
        assertEquals(300, totalBytes);
    }

    @Test
    @DisplayName("CVACT02Y card layout has correct record length 150")
    void cardLayoutRecordLength() {
        CopybookLayout layout = CopybookLayouts.CVACT02Y;
        assertEquals(150, layout.recordLength());
        int totalBytes = layout.fields().stream().mapToInt(CopybookField::length).sum();
        assertEquals(150, totalBytes);
    }

    @Test
    @DisplayName("CVACT03Y xref layout has correct record length 50")
    void xrefLayoutRecordLength() {
        CopybookLayout layout = CopybookLayouts.CVACT03Y;
        assertEquals(50, layout.recordLength());
        int totalBytes = layout.fields().stream().mapToInt(CopybookField::length).sum();
        assertEquals(50, totalBytes);
    }

    @Test
    @DisplayName("CVCUS01Y customer layout has correct record length 500")
    void customerLayoutRecordLength() {
        CopybookLayout layout = CopybookLayouts.CVCUS01Y;
        assertEquals(500, layout.recordLength());
        int totalBytes = layout.fields().stream().mapToInt(CopybookField::length).sum();
        assertEquals(500, totalBytes);
    }

    @Test
    @DisplayName("CVTRA05Y transaction layout has correct record length 350")
    void transactionLayoutRecordLength() {
        CopybookLayout layout = CopybookLayouts.CVTRA05Y;
        assertEquals(350, layout.recordLength());
        int totalBytes = layout.fields().stream().mapToInt(CopybookField::length).sum();
        assertEquals(350, totalBytes);
    }

    @Test
    @DisplayName("CVTRA06Y daily transaction layout has correct record length 350")
    void dailyTransactionLayoutRecordLength() {
        CopybookLayout layout = CopybookLayouts.CVTRA06Y;
        assertEquals(350, layout.recordLength());
        int totalBytes = layout.fields().stream().mapToInt(CopybookField::length).sum();
        assertEquals(350, totalBytes);
    }

    @Test
    @DisplayName("CVTRA01Y transaction category balance layout 50 bytes")
    void tranCatBalLayoutRecordLength() {
        CopybookLayout layout = CopybookLayouts.CVTRA01Y;
        assertEquals(50, layout.recordLength());
        int totalBytes = layout.fields().stream().mapToInt(CopybookField::length).sum();
        assertEquals(50, totalBytes);
    }

    @Test
    @DisplayName("CVTRA02Y disclosure group layout 50 bytes")
    void disclosureGroupLayoutRecordLength() {
        CopybookLayout layout = CopybookLayouts.CVTRA02Y;
        assertEquals(50, layout.recordLength());
        int totalBytes = layout.fields().stream().mapToInt(CopybookField::length).sum();
        assertEquals(50, totalBytes);
    }

    @Test
    @DisplayName("CSUSR01Y user security layout 80 bytes")
    void userSecurityLayoutRecordLength() {
        CopybookLayout layout = CopybookLayouts.CSUSR01Y;
        assertEquals(80, layout.recordLength());
        int totalBytes = layout.fields().stream().mapToInt(CopybookField::length).sum();
        assertEquals(80, totalBytes);
    }

    @Test
    @DisplayName("getLayout returns correct layout by name")
    void getLayoutByName() {
        assertNotNull(CopybookLayouts.getLayout("CVACT01Y"));
        assertNotNull(CopybookLayouts.getLayout("CVACT02Y"));
        assertNotNull(CopybookLayouts.getLayout("CVACT03Y"));
        assertNotNull(CopybookLayouts.getLayout("CVCUS01Y"));
        assertNull(CopybookLayouts.getLayout("NONEXISTENT"));
    }

    @Test
    @DisplayName("dataFields() excludes FILLER fields")
    void dataFieldsExcludesFiller() {
        CopybookLayout layout = CopybookLayouts.CVACT01Y;
        assertTrue(layout.dataFields().stream()
                .noneMatch(f -> f.type() == CopybookField.FieldType.FILLER));
        // CVACT01Y has 12 data fields + 1 FILLER
        assertEquals(12, layout.dataFields().size());
    }

    @Test
    @DisplayName("findField returns correct field by name")
    void findFieldByName() {
        CopybookLayout layout = CopybookLayouts.CVACT01Y;
        CopybookField field = layout.findField("ACCT-ID");
        assertEquals(0, field.offset());
        assertEquals(11, field.length());
        assertEquals("acct_id", field.columnName());
    }

    @Test
    @DisplayName("findField throws for non-existent field")
    void findFieldNonExistent() {
        CopybookLayout layout = CopybookLayouts.CVACT01Y;
        assertThrows(IllegalArgumentException.class, () -> layout.findField("NONEXISTENT"));
    }

    @Test
    @DisplayName("Field offsets are contiguous across all layouts")
    void fieldOffsetsContiguous() {
        for (CopybookLayout layout : new CopybookLayout[]{
                CopybookLayouts.CVACT01Y, CopybookLayouts.CVACT02Y,
                CopybookLayouts.CVACT03Y, CopybookLayouts.CVCUS01Y,
                CopybookLayouts.CVTRA05Y, CopybookLayouts.CVTRA06Y,
                CopybookLayouts.CVTRA01Y, CopybookLayouts.CVTRA02Y,
                CopybookLayouts.CSUSR01Y
        }) {
            int expectedOffset = 0;
            for (CopybookField field : layout.fields()) {
                assertEquals(expectedOffset, field.offset(),
                        String.format("Layout %s, field %s: expected offset %d, got %d",
                                layout.name(), field.name(), expectedOffset, field.offset()));
                expectedOffset += field.length();
            }
            assertEquals(layout.recordLength(), expectedOffset,
                    String.format("Layout %s: total field bytes %d != record length %d",
                            layout.name(), expectedOffset, layout.recordLength()));
        }
    }
}
