package com.cardemo.migration.service;

import com.cardemo.migration.model.VsamFileDescriptor;
import com.cardemo.migration.model.ValidationResult;
import com.cardemo.migration.reader.CopybookLayouts;
import com.cardemo.migration.reader.EbcdicFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CrossReferenceValidator - validates card→account→customer
 * referential integrity in EBCDIC files.
 */
class CrossReferenceValidatorTest {

    private static final Charset EBCDIC = Charset.forName("IBM037");

    @TempDir
    Path tempDir;

    private EbcdicFileReader fileReader;

    @BeforeEach
    void setUp() {
        fileReader = new EbcdicFileReader(tempDir);
    }

    private void createAccountFile(String... accountIds) throws IOException {
        byte[] data = new byte[300 * accountIds.length];
        for (int i = 0; i < accountIds.length; i++) {
            String paddedId = String.format("%-11s", accountIds[i]).replace(' ', '0');
            byte[] idBytes = paddedId.getBytes(EBCDIC);
            System.arraycopy(idBytes, 0, data, i * 300, 11);
            // Fill remaining with EBCDIC spaces
            for (int j = 11; j < 300; j++) {
                data[i * 300 + j] = 0x40;
            }
        }
        Files.write(tempDir.resolve("AWS.M2.CARDDEMO.ACCTDATA.PS"), data);
    }

    private void createCustomerFile(String... customerIds) throws IOException {
        byte[] data = new byte[500 * customerIds.length];
        for (int i = 0; i < customerIds.length; i++) {
            String paddedId = String.format("%-9s", customerIds[i]).replace(' ', '0');
            byte[] idBytes = paddedId.getBytes(EBCDIC);
            System.arraycopy(idBytes, 0, data, i * 500, 9);
            for (int j = 9; j < 500; j++) {
                data[i * 500 + j] = 0x40;
            }
        }
        Files.write(tempDir.resolve("AWS.M2.CARDDEMO.CUSTDATA.PS"), data);
    }

    private void createXrefFile(String[][] xrefs) throws IOException {
        // Each xref: [cardNum(16), custId(9), acctId(11)] = 36 bytes + 14 filler = 50 bytes
        byte[] data = new byte[50 * xrefs.length];
        for (int i = 0; i < xrefs.length; i++) {
            int offset = i * 50;
            String cardNum = String.format("%-16s", xrefs[i][0]);
            String custId = String.format("%-9s", xrefs[i][1]).replace(' ', '0');
            String acctId = String.format("%-11s", xrefs[i][2]).replace(' ', '0');

            byte[] cardBytes = cardNum.getBytes(EBCDIC);
            byte[] custBytes = custId.getBytes(EBCDIC);
            byte[] acctBytes = acctId.getBytes(EBCDIC);

            System.arraycopy(cardBytes, 0, data, offset, 16);
            System.arraycopy(custBytes, 0, data, offset + 16, 9);
            System.arraycopy(acctBytes, 0, data, offset + 25, 11);
            for (int j = 36; j < 50; j++) {
                data[offset + j] = 0x40;
            }
        }
        Files.write(tempDir.resolve("AWS.M2.CARDDEMO.CARDXREF.PS"), data);
    }

    @Test
    @DisplayName("Card-to-account chain valid when all xref accounts exist")
    void cardToAccountAllValid() throws IOException {
        createAccountFile("00000000001", "00000000002", "00000000003");
        createXrefFile(new String[][]{
                {"4000000000000001", "000000001", "00000000001"},
                {"4000000000000002", "000000002", "00000000002"}
        });

        CrossReferenceValidator validator = new CrossReferenceValidator(fileReader, null);
        ValidationResult result = validator.validateCardToAccount();
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Card-to-account chain fails with orphaned account reference")
    void cardToAccountOrphaned() throws IOException {
        createAccountFile("00000000001");
        createXrefFile(new String[][]{
                {"4000000000000001", "000000001", "00000000001"},
                {"4000000000000002", "000000002", "00000000099"} // non-existent account
        });

        CrossReferenceValidator validator = new CrossReferenceValidator(fileReader, null);
        ValidationResult result = validator.validateCardToAccount();
        assertFalse(result.isPassed());
    }

    @Test
    @DisplayName("Account-to-customer chain valid when all xref customers exist")
    void accountToCustomerAllValid() throws IOException {
        createCustomerFile("000000001", "000000002");
        createXrefFile(new String[][]{
                {"4000000000000001", "000000001", "00000000001"},
                {"4000000000000002", "000000002", "00000000002"}
        });

        CrossReferenceValidator validator = new CrossReferenceValidator(fileReader, null);
        ValidationResult result = validator.validateAccountToCustomer();
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Full chain validation passes with valid data")
    void fullChainValid() throws IOException {
        createAccountFile("00000000001", "00000000002");
        createCustomerFile("000000001", "000000002");
        createXrefFile(new String[][]{
                {"4000000000000001", "000000001", "00000000001"},
                {"4000000000000002", "000000002", "00000000002"}
        });

        CrossReferenceValidator validator = new CrossReferenceValidator(fileReader, null);
        ValidationResult result = validator.validateFullChain();
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Full chain validation fails with broken chain")
    void fullChainBroken() throws IOException {
        createAccountFile("00000000001");
        createCustomerFile("000000001");
        createXrefFile(new String[][]{
                {"4000000000000001", "000000001", "00000000001"},
                {"4000000000000002", "000000099", "00000000099"} // broken chain
        });

        CrossReferenceValidator validator = new CrossReferenceValidator(fileReader, null);
        ValidationResult result = validator.validateFullChain();
        assertFalse(result.isPassed());
    }

    @Test
    @DisplayName("Extract field values from VSAM file")
    void extractFieldValues() throws IOException {
        createAccountFile("00000000001", "00000000002", "00000000003");
        CrossReferenceValidator validator = new CrossReferenceValidator(fileReader, null);
        Set<String> values = validator.extractFieldValues(
                VsamFileDescriptor.ACCTDATA, CopybookLayouts.CVACT01Y, "ACCT-ID");
        assertEquals(3, values.size());
        assertTrue(values.contains("00000000001"));
        assertTrue(values.contains("00000000002"));
        assertTrue(values.contains("00000000003"));
    }
}
