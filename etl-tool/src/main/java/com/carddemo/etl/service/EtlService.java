package com.carddemo.etl.service;

import com.carddemo.etl.config.EtlProperties;
import com.carddemo.etl.model.*;
import com.carddemo.etl.parser.RecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Core ETL service that orchestrates parsing EBCDIC data files and loading into PostgreSQL.
 * Supports idempotent execution via truncate-before-load.
 */
@Service
public class EtlService {

    private static final Logger log = LoggerFactory.getLogger(EtlService.class);

    private final JdbcTemplate jdbcTemplate;
    private final EtlProperties properties;

    public EtlService(JdbcTemplate jdbcTemplate, EtlProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    /**
     * Execute the full ETL migration pipeline.
     */
    public MigrationReport runMigration() {
        MigrationReport report = new MigrationReport();
        Path sourceDir = Paths.get(properties.getSourceDir());

        log.info("Starting CardDemo EBCDIC data migration from: {}", sourceDir.toAbsolutePath());

        if (properties.isTruncateBeforeLoad()) {
            truncateAllTables();
        }

        // Load reference data first, then master data, then transactional data
        loadTranTypes(sourceDir, report);
        loadTranCategories(sourceDir, report);
        loadAccounts(sourceDir, report);
        loadCustomers(sourceDir, report);
        loadCards(sourceDir, report);
        loadCardXrefs(sourceDir, report);
        loadUserSecurity(sourceDir, report);
        loadDailyTransactions(sourceDir, report);
        loadTranCatBalances(sourceDir, report);
        loadDisclosureGroups(sourceDir, report);

        // Validate cross-reference integrity
        if (properties.isValidateXref()) {
            validateXrefIntegrity(report);
        }

        logReport(report);
        return report;
    }

    private void truncateAllTables() {
        log.info("Truncating all tables for idempotent reload...");
        String[] tables = {
            "card_xref", "tran_cat_balance", "disclosure_group",
            "daily_transactions", "transactions",
            "cards", "customers", "accounts",
            "tran_category", "tran_type", "user_security"
        };
        for (String table : tables) {
            jdbcTemplate.execute("DELETE FROM " + table);
        }
        log.info("All tables truncated.");
    }

    // ---- Account loading ----

    private void loadAccounts(Path sourceDir, MigrationReport report) {
        String fileName = "AWS.M2.CARDDEMO.ACCTDATA.PS";
        Path filePath = sourceDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            log.warn("Account data file not found: {}", filePath);
            report.addFileStats(fileName, 0, 0, 1, List.of("File not found: " + filePath));
            return;
        }

        List<String> errors = new ArrayList<>();
        try {
            List<AccountRecord> records = RecordParser.parseAccounts(filePath);
            int sourceCount = records.size();
            int loaded = 0;

            for (AccountRecord r : records) {
                try {
                    jdbcTemplate.update(
                        "INSERT INTO accounts (acct_id, acct_active_status, acct_curr_bal, acct_credit_limit, "
                            + "acct_cash_credit_limit, acct_open_date, acct_expiration_date, acct_reissue_date, "
                            + "acct_curr_cyc_credit, acct_curr_cyc_debit, acct_addr_zip, acct_group_id) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        r.getAcctId(), r.getAcctActiveStatus(), r.getAcctCurrBal(), r.getAcctCreditLimit(),
                        r.getAcctCashCreditLimit(), r.getAcctOpenDate(), r.getAcctExpirationDate(),
                        r.getAcctReissueDate(), r.getAcctCurrCycCredit(), r.getAcctCurrCycDebit(),
                        r.getAcctAddrZip(), r.getAcctGroupId());
                    loaded++;
                } catch (Exception e) {
                    String err = "Account record error (acct_id=" + r.getAcctId() + "): " + e.getMessage();
                    errors.add(err);
                    log.warn(err);
                }
            }

            log.info("Accounts: {} source records, {} loaded, {} errors", sourceCount, loaded, errors.size());
            report.addFileStats(fileName, sourceCount, loaded, errors.size(), errors);
        } catch (IOException e) {
            errors.add("Failed to parse file: " + e.getMessage());
            report.addFileStats(fileName, 0, 0, 1, errors);
            log.error("Failed to parse account file: {}", filePath, e);
        }
    }

    // ---- Customer loading ----

    private void loadCustomers(Path sourceDir, MigrationReport report) {
        String fileName = "AWS.M2.CARDDEMO.CUSTDATA.PS";
        Path filePath = sourceDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            log.warn("Customer data file not found: {}", filePath);
            report.addFileStats(fileName, 0, 0, 1, List.of("File not found: " + filePath));
            return;
        }

        List<String> errors = new ArrayList<>();
        try {
            List<CustomerRecord> records = RecordParser.parseCustomers(filePath);
            int sourceCount = records.size();
            int loaded = 0;

            for (CustomerRecord r : records) {
                try {
                    jdbcTemplate.update(
                        "INSERT INTO customers (cust_id, cust_first_name, cust_middle_name, cust_last_name, "
                            + "cust_addr_line_1, cust_addr_line_2, cust_addr_line_3, cust_addr_state_cd, "
                            + "cust_addr_country_cd, cust_addr_zip, cust_phone_num_1, cust_phone_num_2, "
                            + "cust_ssn, cust_govt_issued_id, cust_dob_yyyy_mm_dd, cust_eft_account_id, "
                            + "cust_pri_card_holder_ind, cust_fico_credit_score) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        r.getCustId(), r.getCustFirstName(), r.getCustMiddleName(), r.getCustLastName(),
                        r.getCustAddrLine1(), r.getCustAddrLine2(), r.getCustAddrLine3(),
                        r.getCustAddrStateCd(), r.getCustAddrCountryCd(), r.getCustAddrZip(),
                        r.getCustPhoneNum1(), r.getCustPhoneNum2(), r.getCustSsn(),
                        r.getCustGovtIssuedId(), r.getCustDobYyyyMmDd(), r.getCustEftAccountId(),
                        r.getCustPriCardHolderInd(), r.getCustFicoCreditScore());
                    loaded++;
                } catch (Exception e) {
                    String err = "Customer record error (cust_id=" + r.getCustId() + "): " + e.getMessage();
                    errors.add(err);
                    log.warn(err);
                }
            }

            log.info("Customers: {} source records, {} loaded, {} errors", sourceCount, loaded, errors.size());
            report.addFileStats(fileName, sourceCount, loaded, errors.size(), errors);
        } catch (IOException e) {
            errors.add("Failed to parse file: " + e.getMessage());
            report.addFileStats(fileName, 0, 0, 1, errors);
            log.error("Failed to parse customer file: {}", filePath, e);
        }
    }

    // ---- Card loading ----

    private void loadCards(Path sourceDir, MigrationReport report) {
        String fileName = "AWS.M2.CARDDEMO.CARDDATA.PS";
        Path filePath = sourceDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            log.warn("Card data file not found: {}", filePath);
            report.addFileStats(fileName, 0, 0, 1, List.of("File not found: " + filePath));
            return;
        }

        List<String> errors = new ArrayList<>();
        try {
            List<CardRecord> records = RecordParser.parseCards(filePath);
            int sourceCount = records.size();
            int loaded = 0;

            for (CardRecord r : records) {
                try {
                    jdbcTemplate.update(
                        "INSERT INTO cards (card_num, card_acct_id, card_cvv_cd, card_embossed_name, "
                            + "card_expiration_date, card_active_status) VALUES (?, ?, ?, ?, ?, ?)",
                        r.getCardNum(), r.getCardAcctId(), r.getCardCvvCd(),
                        r.getCardEmbossedName(), r.getCardExpirationDate(), r.getCardActiveStatus());
                    loaded++;
                } catch (Exception e) {
                    String err = "Card record error (card_num=" + r.getCardNum() + "): " + e.getMessage();
                    errors.add(err);
                    log.warn(err);
                }
            }

            log.info("Cards: {} source records, {} loaded, {} errors", sourceCount, loaded, errors.size());
            report.addFileStats(fileName, sourceCount, loaded, errors.size(), errors);
        } catch (IOException e) {
            errors.add("Failed to parse file: " + e.getMessage());
            report.addFileStats(fileName, 0, 0, 1, errors);
            log.error("Failed to parse card file: {}", filePath, e);
        }
    }

    // ---- Card Cross-Reference loading ----

    private void loadCardXrefs(Path sourceDir, MigrationReport report) {
        String fileName = "AWS.M2.CARDDEMO.CARDXREF.PS";
        Path filePath = sourceDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            log.warn("Card cross-reference data file not found: {}", filePath);
            report.addFileStats(fileName, 0, 0, 1, List.of("File not found: " + filePath));
            return;
        }

        List<String> errors = new ArrayList<>();
        try {
            List<CardXrefRecord> records = RecordParser.parseCardXrefs(filePath);
            int sourceCount = records.size();
            int loaded = 0;

            for (CardXrefRecord r : records) {
                try {
                    jdbcTemplate.update(
                        "INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) VALUES (?, ?, ?)",
                        r.getXrefCardNum(), r.getXrefCustId(), r.getXrefAcctId());
                    loaded++;
                } catch (Exception e) {
                    String err = "CardXref record error (card_num=" + r.getXrefCardNum() + "): " + e.getMessage();
                    errors.add(err);
                    log.warn(err);
                }
            }

            log.info("CardXrefs: {} source records, {} loaded, {} errors", sourceCount, loaded, errors.size());
            report.addFileStats(fileName, sourceCount, loaded, errors.size(), errors);
        } catch (IOException e) {
            errors.add("Failed to parse file: " + e.getMessage());
            report.addFileStats(fileName, 0, 0, 1, errors);
            log.error("Failed to parse card xref file: {}", filePath, e);
        }
    }

    // ---- User Security loading ----

    private void loadUserSecurity(Path sourceDir, MigrationReport report) {
        String fileName = "AWS.M2.CARDDEMO.USRSEC.PS";
        Path filePath = sourceDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            log.warn("User security data file not found: {}", filePath);
            report.addFileStats(fileName, 0, 0, 1, List.of("File not found: " + filePath));
            return;
        }

        List<String> errors = new ArrayList<>();
        try {
            List<UserSecurityRecord> records = RecordParser.parseUserSecurity(filePath);
            int sourceCount = records.size();
            int loaded = 0;

            for (UserSecurityRecord r : records) {
                try {
                    jdbcTemplate.update(
                        "INSERT INTO user_security (sec_usr_id, sec_usr_fname, sec_usr_lname, sec_usr_pwd, sec_usr_type) "
                            + "VALUES (?, ?, ?, ?, ?)",
                        r.getSecUsrId(), r.getSecUsrFname(), r.getSecUsrLname(),
                        r.getSecUsrPwd(), r.getSecUsrType());
                    loaded++;
                } catch (Exception e) {
                    String err = "UserSecurity record error (usr_id=" + r.getSecUsrId() + "): " + e.getMessage();
                    errors.add(err);
                    log.warn(err);
                }
            }

            log.info("UserSecurity: {} source records, {} loaded, {} errors", sourceCount, loaded, errors.size());
            report.addFileStats(fileName, sourceCount, loaded, errors.size(), errors);
        } catch (IOException e) {
            errors.add("Failed to parse file: " + e.getMessage());
            report.addFileStats(fileName, 0, 0, 1, errors);
            log.error("Failed to parse user security file: {}", filePath, e);
        }
    }

    // ---- Daily Transaction loading ----

    private void loadDailyTransactions(Path sourceDir, MigrationReport report) {
        String fileName = "AWS.M2.CARDDEMO.DALYTRAN.PS";
        Path filePath = sourceDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            log.warn("Daily transaction data file not found: {}", filePath);
            report.addFileStats(fileName, 0, 0, 1, List.of("File not found: " + filePath));
            return;
        }

        List<String> errors = new ArrayList<>();
        try {
            List<TransactionRecord> records = RecordParser.parseTransactions(filePath);
            int sourceCount = records.size();
            int loaded = 0;

            for (TransactionRecord r : records) {
                try {
                    jdbcTemplate.update(
                        "INSERT INTO daily_transactions (tran_id, tran_type_cd, tran_cat_cd, tran_source, "
                            + "tran_desc, tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, "
                            + "tran_merchant_zip, tran_card_num, tran_orig_ts, tran_proc_ts) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        r.getTranId(), r.getTranTypeCd(), r.getTranCatCd(), r.getTranSource(),
                        r.getTranDesc(), r.getTranAmt(), r.getTranMerchantId(),
                        r.getTranMerchantName(), r.getTranMerchantCity(), r.getTranMerchantZip(),
                        r.getTranCardNum(), r.getTranOrigTs(), r.getTranProcTs());
                    loaded++;
                } catch (Exception e) {
                    String err = "DailyTransaction record error (tran_id=" + r.getTranId() + "): " + e.getMessage();
                    errors.add(err);
                    log.warn(err);
                }
            }

            log.info("DailyTransactions: {} source records, {} loaded, {} errors", sourceCount, loaded, errors.size());
            report.addFileStats(fileName, sourceCount, loaded, errors.size(), errors);
        } catch (IOException e) {
            errors.add("Failed to parse file: " + e.getMessage());
            report.addFileStats(fileName, 0, 0, 1, errors);
            log.error("Failed to parse daily transaction file: {}", filePath, e);
        }
    }

    // ---- Transaction Category Balance loading ----

    private void loadTranCatBalances(Path sourceDir, MigrationReport report) {
        String fileName = "AWS.M2.CARDDEMO.TCATBALF.PS";
        Path filePath = sourceDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            log.warn("Transaction category balance data file not found: {}", filePath);
            report.addFileStats(fileName, 0, 0, 1, List.of("File not found: " + filePath));
            return;
        }

        List<String> errors = new ArrayList<>();
        try {
            List<TranCatBalRecord> records = RecordParser.parseTranCatBalances(filePath);
            int sourceCount = records.size();
            int loaded = 0;

            for (TranCatBalRecord r : records) {
                try {
                    jdbcTemplate.update(
                        "INSERT INTO tran_cat_balance (trancat_acct_id, trancat_type_cd, trancat_cd, tran_cat_bal) "
                            + "VALUES (?, ?, ?, ?)",
                        r.getTrancatAcctId(), r.getTrancatTypeCd(), r.getTrancatCd(), r.getTranCatBal());
                    loaded++;
                } catch (Exception e) {
                    String err = "TranCatBalance record error (acct_id=" + r.getTrancatAcctId() + "): " + e.getMessage();
                    errors.add(err);
                    log.warn(err);
                }
            }

            log.info("TranCatBalances: {} source records, {} loaded, {} errors", sourceCount, loaded, errors.size());
            report.addFileStats(fileName, sourceCount, loaded, errors.size(), errors);
        } catch (IOException e) {
            errors.add("Failed to parse file: " + e.getMessage());
            report.addFileStats(fileName, 0, 0, 1, errors);
            log.error("Failed to parse tran cat balance file: {}", filePath, e);
        }
    }

    // ---- Disclosure Group loading ----

    private void loadDisclosureGroups(Path sourceDir, MigrationReport report) {
        String fileName = "AWS.M2.CARDDEMO.DISCGRP.PS";
        Path filePath = sourceDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            log.warn("Disclosure group data file not found: {}", filePath);
            report.addFileStats(fileName, 0, 0, 1, List.of("File not found: " + filePath));
            return;
        }

        List<String> errors = new ArrayList<>();
        try {
            List<DisclosureGroupRecord> records = RecordParser.parseDisclosureGroups(filePath);
            int sourceCount = records.size();
            int loaded = 0;

            for (DisclosureGroupRecord r : records) {
                try {
                    jdbcTemplate.update(
                        "INSERT INTO disclosure_group (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd, dis_int_rate) "
                            + "VALUES (?, ?, ?, ?)",
                        r.getDisAcctGroupId(), r.getDisTranTypeCd(), r.getDisTranCatCd(), r.getDisIntRate());
                    loaded++;
                } catch (Exception e) {
                    String err = "DisclosureGroup record error (group_id=" + r.getDisAcctGroupId() + "): " + e.getMessage();
                    errors.add(err);
                    log.warn(err);
                }
            }

            log.info("DisclosureGroups: {} source records, {} loaded, {} errors", sourceCount, loaded, errors.size());
            report.addFileStats(fileName, sourceCount, loaded, errors.size(), errors);
        } catch (IOException e) {
            errors.add("Failed to parse file: " + e.getMessage());
            report.addFileStats(fileName, 0, 0, 1, errors);
            log.error("Failed to parse disclosure group file: {}", filePath, e);
        }
    }

    // ---- Transaction Type loading ----

    private void loadTranTypes(Path sourceDir, MigrationReport report) {
        String fileName = "AWS.M2.CARDDEMO.TRANTYPE.PS";
        Path filePath = sourceDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            log.warn("Transaction type data file not found: {}", filePath);
            report.addFileStats(fileName, 0, 0, 1, List.of("File not found: " + filePath));
            return;
        }

        List<String> errors = new ArrayList<>();
        try {
            List<TranTypeRecord> records = RecordParser.parseTranTypes(filePath);
            int sourceCount = records.size();
            int loaded = 0;

            for (TranTypeRecord r : records) {
                try {
                    jdbcTemplate.update(
                        "INSERT INTO tran_type (tran_type, tran_type_desc) VALUES (?, ?)",
                        r.getTranType(), r.getTranTypeDesc());
                    loaded++;
                } catch (Exception e) {
                    String err = "TranType record error (type=" + r.getTranType() + "): " + e.getMessage();
                    errors.add(err);
                    log.warn(err);
                }
            }

            log.info("TranTypes: {} source records, {} loaded, {} errors", sourceCount, loaded, errors.size());
            report.addFileStats(fileName, sourceCount, loaded, errors.size(), errors);
        } catch (IOException e) {
            errors.add("Failed to parse file: " + e.getMessage());
            report.addFileStats(fileName, 0, 0, 1, errors);
            log.error("Failed to parse tran type file: {}", filePath, e);
        }
    }

    // ---- Transaction Category loading ----

    private void loadTranCategories(Path sourceDir, MigrationReport report) {
        String fileName = "AWS.M2.CARDDEMO.TRANCATG.PS";
        Path filePath = sourceDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            log.warn("Transaction category data file not found: {}", filePath);
            report.addFileStats(fileName, 0, 0, 1, List.of("File not found: " + filePath));
            return;
        }

        List<String> errors = new ArrayList<>();
        try {
            List<TranCategoryRecord> records = RecordParser.parseTranCategories(filePath);
            int sourceCount = records.size();
            int loaded = 0;

            for (TranCategoryRecord r : records) {
                try {
                    jdbcTemplate.update(
                        "INSERT INTO tran_category (tran_type_cd, tran_cat_cd, tran_cat_type_desc) VALUES (?, ?, ?)",
                        r.getTranTypeCd(), r.getTranCatCd(), r.getTranCatTypeDesc());
                    loaded++;
                } catch (Exception e) {
                    String err = "TranCategory record error (type=" + r.getTranTypeCd() + ", cat=" + r.getTranCatCd() + "): " + e.getMessage();
                    errors.add(err);
                    log.warn(err);
                }
            }

            log.info("TranCategories: {} source records, {} loaded, {} errors", sourceCount, loaded, errors.size());
            report.addFileStats(fileName, sourceCount, loaded, errors.size(), errors);
        } catch (IOException e) {
            errors.add("Failed to parse file: " + e.getMessage());
            report.addFileStats(fileName, 0, 0, 1, errors);
            log.error("Failed to parse tran category file: {}", filePath, e);
        }
    }

    // ---- Cross-Reference Integrity Validation ----

    private void validateXrefIntegrity(MigrationReport report) {
        log.info("Validating cross-reference integrity...");

        // Check that all xref account IDs exist in the accounts table
        List<Long> orphanAcctIds = jdbcTemplate.queryForList(
            "SELECT DISTINCT x.xref_acct_id FROM card_xref x "
                + "WHERE NOT EXISTS (SELECT 1 FROM accounts a WHERE a.acct_id = x.xref_acct_id)",
            Long.class);

        for (Long acctId : orphanAcctIds) {
            report.addXrefError("Card XREF references non-existent account: " + acctId);
        }

        // Check that all xref customer IDs exist in the customers table
        List<Long> orphanCustIds = jdbcTemplate.queryForList(
            "SELECT DISTINCT x.xref_cust_id FROM card_xref x "
                + "WHERE NOT EXISTS (SELECT 1 FROM customers c WHERE c.cust_id = x.xref_cust_id)",
            Long.class);

        for (Long custId : orphanCustIds) {
            report.addXrefError("Card XREF references non-existent customer: " + custId);
        }

        // Check that all xref card numbers exist in the cards table
        List<String> orphanCardNums = jdbcTemplate.queryForList(
            "SELECT DISTINCT x.xref_card_num FROM card_xref x "
                + "WHERE NOT EXISTS (SELECT 1 FROM cards c WHERE c.card_num = x.xref_card_num)",
            String.class);

        for (String cardNum : orphanCardNums) {
            report.addXrefError("Card XREF references non-existent card: " + cardNum);
        }

        if (report.isXrefValidationPassed()) {
            log.info("Cross-reference integrity validation PASSED");
        } else {
            log.warn("Cross-reference integrity validation FAILED with {} errors", report.getXrefErrors().size());
        }
    }

    // ---- Report logging ----

    private void logReport(MigrationReport report) {
        log.info("========================================");
        log.info("  CardDemo ETL Migration Report");
        log.info("========================================");

        for (var entry : report.getFileStats().entrySet()) {
            MigrationReport.FileStats stats = entry.getValue();
            log.info("  {} -> source={}, loaded={}, errors={}",
                entry.getKey(), stats.sourceRecords(), stats.loadedRecords(), stats.errorCount());
            for (String err : stats.errors()) {
                log.warn("    ERROR: {}", err);
            }
        }

        log.info("----------------------------------------");
        log.info("  Cross-Reference Validation: {}", report.isXrefValidationPassed() ? "PASSED" : "FAILED");
        for (String err : report.getXrefErrors()) {
            log.warn("    XREF ERROR: {}", err);
        }

        log.info("========================================");
        log.info("  Overall Status: {}", report.isFullySuccessful() ? "SUCCESS" : "COMPLETED WITH ISSUES");
        log.info("========================================");
    }
}
