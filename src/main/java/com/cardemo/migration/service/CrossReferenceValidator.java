package com.cardemo.migration.service;

import com.cardemo.migration.model.CopybookLayout;
import com.cardemo.migration.model.ValidationResult;
import com.cardemo.migration.model.VsamFileDescriptor;
import com.cardemo.migration.reader.CopybookLayouts;
import com.cardemo.migration.reader.EbcdicFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Validates cross-reference integrity between VSAM files and the migrated PostgreSQL tables.
 * Checks:
 * - Every card in CARDXREF maps to a valid account in ACCTDATA
 * - Every account in ACCTDATA maps to a valid customer in CUSTDATA
 * - Referential integrity is preserved in PostgreSQL foreign keys
 */
public class CrossReferenceValidator {

    private static final Logger log = LoggerFactory.getLogger(CrossReferenceValidator.class);

    private final EbcdicFileReader fileReader;
    private final JdbcTemplate jdbcTemplate;

    public CrossReferenceValidator(EbcdicFileReader fileReader, JdbcTemplate jdbcTemplate) {
        this.fileReader = fileReader;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Validates that every card cross-reference points to a valid account.
     * Reads all CARDXREF records and checks that each XREF-ACCT-ID exists in ACCTDATA.
     *
     * @return validation result
     */
    public ValidationResult validateCardToAccount() {
        ValidationResult result = new ValidationResult(
                "CrossRef:CardToAccount",
                "Verify every CARDXREF record maps to a valid ACCTDATA record");

        try {
            Set<String> accountIds = extractFieldValues(VsamFileDescriptor.ACCTDATA,
                    CopybookLayouts.CVACT01Y, "ACCT-ID");
            Set<String> xrefAccountIds = extractFieldValues(VsamFileDescriptor.CARDXREF,
                    CopybookLayouts.CVACT03Y, "XREF-ACCT-ID");

            Set<String> orphanedAccounts = new HashSet<>(xrefAccountIds);
            orphanedAccounts.removeAll(accountIds);

            if (orphanedAccounts.isEmpty()) {
                result.pass(String.format(
                        "All %d CARDXREF records map to valid accounts",
                        xrefAccountIds.size()));
            } else {
                result.fail(String.format(
                        "%d CARDXREF records reference non-existent accounts: %s",
                        orphanedAccounts.size(),
                        orphanedAccounts.size() <= 10 ? orphanedAccounts.toString() :
                                orphanedAccounts.stream().limit(10).toList() + "..."));
            }

            log.info("Card-to-Account validation: {} xrefs, {} accounts, {} orphaned",
                    xrefAccountIds.size(), accountIds.size(), orphanedAccounts.size());

        } catch (IOException e) {
            result.fail("Error reading EBCDIC files: " + e.getMessage());
            log.error("Error during card-to-account validation", e);
        }

        return result;
    }

    /**
     * Validates that every account in ACCTDATA has a corresponding customer in CUSTDATA
     * via the CARDXREF cross-reference.
     *
     * @return validation result
     */
    public ValidationResult validateAccountToCustomer() {
        ValidationResult result = new ValidationResult(
                "CrossRef:AccountToCustomer",
                "Verify account-to-customer chain via CARDXREF");

        try {
            Set<String> customerIds = extractFieldValues(VsamFileDescriptor.CUSTDATA,
                    CopybookLayouts.CVCUS01Y, "CUST-ID");
            Set<String> xrefCustomerIds = extractFieldValues(VsamFileDescriptor.CARDXREF,
                    CopybookLayouts.CVACT03Y, "XREF-CUST-ID");

            Set<String> orphanedCustomers = new HashSet<>(xrefCustomerIds);
            orphanedCustomers.removeAll(customerIds);

            if (orphanedCustomers.isEmpty()) {
                result.pass(String.format(
                        "All %d CARDXREF customer references map to valid customers",
                        xrefCustomerIds.size()));
            } else {
                result.fail(String.format(
                        "%d CARDXREF records reference non-existent customers: %s",
                        orphanedCustomers.size(),
                        orphanedCustomers.size() <= 10 ? orphanedCustomers.toString() :
                                orphanedCustomers.stream().limit(10).toList() + "..."));
            }

            log.info("Account-to-Customer validation: {} xref customers, {} customers, {} orphaned",
                    xrefCustomerIds.size(), customerIds.size(), orphanedCustomers.size());

        } catch (IOException e) {
            result.fail("Error reading EBCDIC files: " + e.getMessage());
            log.error("Error during account-to-customer validation", e);
        }

        return result;
    }

    /**
     * Validates the full card→account→customer chain in the EBCDIC files.
     *
     * @return validation result
     */
    public ValidationResult validateFullChain() {
        ValidationResult result = new ValidationResult(
                "CrossRef:FullChain",
                "Verify complete card→account→customer referential chain");

        try {
            Set<String> accountIds = extractFieldValues(VsamFileDescriptor.ACCTDATA,
                    CopybookLayouts.CVACT01Y, "ACCT-ID");
            Set<String> customerIds = extractFieldValues(VsamFileDescriptor.CUSTDATA,
                    CopybookLayouts.CVCUS01Y, "CUST-ID");

            CopybookLayout xrefLayout = CopybookLayouts.CVACT03Y;
            int xrefCount = fileReader.countRecords(VsamFileDescriptor.CARDXREF);

            int brokenChains = 0;
            for (int i = 0; i < xrefCount; i++) {
                Map<String, String> record = fileReader.readParsedRecord(
                        VsamFileDescriptor.CARDXREF, xrefLayout, i);

                String acctId = record.get("XREF-ACCT-ID");
                String custId = record.get("XREF-CUST-ID");

                if (!accountIds.contains(acctId) || !customerIds.contains(custId)) {
                    brokenChains++;
                }
            }

            if (brokenChains == 0) {
                result.pass(String.format(
                        "All %d cross-reference chains are valid (card→account→customer)",
                        xrefCount));
            } else {
                result.fail(String.format(
                        "%d of %d cross-reference chains are broken",
                        brokenChains, xrefCount));
            }

        } catch (IOException e) {
            result.fail("Error validating full chain: " + e.getMessage());
            log.error("Error during full chain validation", e);
        }

        return result;
    }

    /**
     * Validates referential integrity in the PostgreSQL database using foreign key checks.
     *
     * @return validation result
     */
    public ValidationResult validateDatabaseForeignKeys() {
        ValidationResult result = new ValidationResult(
                "CrossRef:DatabaseFK",
                "Verify PostgreSQL foreign key referential integrity");

        try {
            // Check card_xrefs → accounts FK
            int orphanedCardAccounts = countOrphanedForeignKeys(
                    "card_xrefs", "acct_id", "accounts", "acct_id");

            // Check card_xrefs → customers FK
            int orphanedCardCustomers = countOrphanedForeignKeys(
                    "card_xrefs", "cust_id", "customers", "cust_id");

            // Check cards → accounts FK
            int orphanedCards = countOrphanedForeignKeys(
                    "cards", "acct_id", "accounts", "acct_id");

            int totalOrphaned = orphanedCardAccounts + orphanedCardCustomers + orphanedCards;

            if (totalOrphaned == 0) {
                result.pass("All foreign key relationships are valid in PostgreSQL");
            } else {
                result.fail(String.format(
                        "Foreign key violations found: card_xrefs→accounts=%d, " +
                                "card_xrefs→customers=%d, cards→accounts=%d",
                        orphanedCardAccounts, orphanedCardCustomers, orphanedCards));
            }

        } catch (Exception e) {
            result.fail("Error checking database foreign keys: " + e.getMessage());
            log.error("Error during FK validation", e);
        }

        return result;
    }

    /**
     * Extracts all values for a given field from all records in a VSAM file.
     */
    Set<String> extractFieldValues(VsamFileDescriptor descriptor,
                                    CopybookLayout layout,
                                    String fieldName) throws IOException {
        Set<String> values = new HashSet<>();
        int recordCount = fileReader.countRecords(descriptor);
        for (int i = 0; i < recordCount; i++) {
            Map<String, String> record = fileReader.readParsedRecord(descriptor, layout, i);
            String value = record.get(fieldName);
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    /**
     * Counts rows in a child table that reference non-existent parent rows.
     */
    private int countOrphanedForeignKeys(String childTable, String childColumn,
                                          String parentTable, String parentColumn) {
        String sql = String.format(
                "SELECT COUNT(*) FROM %s c LEFT JOIN %s p ON c.%s = p.%s WHERE p.%s IS NULL",
                childTable, parentTable, childColumn, parentColumn, parentColumn);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
