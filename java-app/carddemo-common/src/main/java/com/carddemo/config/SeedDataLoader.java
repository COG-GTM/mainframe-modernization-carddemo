package com.carddemo.config;

import com.carddemo.entity.*;
import com.carddemo.entity.TransactionCategoryBalance.TransactionCategoryBalanceId;
import com.carddemo.entity.DisclosureGroup.DisclosureGroupId;
import com.carddemo.entity.TransactionCategory.TransactionCategoryId;
import com.carddemo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads seed data from fixed-width ASCII files in app/data/ASCII/.
 * Parses each file according to the COBOL copybook field definitions.
 * Runs on application startup with the 'seed' profile.
 */
@Component
@Profile("seed")
public class SeedDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private final UserSecurityRepository userSecurityRepo;
    private final AccountRepository accountRepo;
    private final CardRepository cardRepo;
    private final CustomerRepository customerRepo;
    private final CardXrefRepository cardXrefRepo;
    private final TransactionRepository transactionRepo;
    private final DailyTransactionRepository dailyTransactionRepo;
    private final TransactionCategoryBalanceRepository tcatBalRepo;
    private final DisclosureGroupRepository discGrpRepo;
    private final TransactionTypeRepository tranTypeRepo;
    private final TransactionCategoryRepository tranCatRepo;

    @Value("${carddemo.seed.data-dir:classpath:seed/}")
    private String dataDir;

    @Value("${carddemo.seed.data-path:}")
    private String dataPath;

    public SeedDataLoader(
            UserSecurityRepository userSecurityRepo,
            AccountRepository accountRepo,
            CardRepository cardRepo,
            CustomerRepository customerRepo,
            CardXrefRepository cardXrefRepo,
            TransactionRepository transactionRepo,
            DailyTransactionRepository dailyTransactionRepo,
            TransactionCategoryBalanceRepository tcatBalRepo,
            DisclosureGroupRepository discGrpRepo,
            TransactionTypeRepository tranTypeRepo,
            TransactionCategoryRepository tranCatRepo) {
        this.userSecurityRepo = userSecurityRepo;
        this.accountRepo = accountRepo;
        this.cardRepo = cardRepo;
        this.customerRepo = customerRepo;
        this.cardXrefRepo = cardXrefRepo;
        this.transactionRepo = transactionRepo;
        this.dailyTransactionRepo = dailyTransactionRepo;
        this.tcatBalRepo = tcatBalRepo;
        this.discGrpRepo = discGrpRepo;
        this.tranTypeRepo = tranTypeRepo;
        this.tranCatRepo = tranCatRepo;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting seed data loading...");

        if (dataPath == null || dataPath.isBlank()) {
            log.warn("No data path configured. Set carddemo.seed.data-path to the ASCII data directory.");
            return;
        }

        loadAccounts();
        loadCards();
        loadCustomers();
        loadCardXrefs();
        loadDailyTransactions();
        loadTcatBalances();
        loadDisclosureGroups();
        loadTransactionTypes();
        loadTransactionCategories();
        loadDefaultUsers();

        log.info("Seed data loading completed.");
    }

    private List<String> readLines(String filename) {
        List<String> lines = new ArrayList<>();
        try {
            java.io.File file = new java.io.File(dataPath, filename);
            if (!file.exists()) {
                log.warn("Data file not found: {}", file.getAbsolutePath());
                return lines;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        lines.add(line);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reading file: {}", filename, e);
        }
        return lines;
    }

    /**
     * Parse COBOL signed numeric: last char indicates sign.
     * '{' = +0, 'A'-'I' = +1 to +9
     * '}' = -0, 'J'-'R' = -1 to -9
     */
    private BigDecimal parseSignedDecimal(String raw, int scale) {
        if (raw == null || raw.isBlank()) return BigDecimal.ZERO;
        raw = raw.trim();
        if (raw.isEmpty()) return BigDecimal.ZERO;

        char lastChar = raw.charAt(raw.length() - 1);
        String digits = raw.substring(0, raw.length() - 1);
        int lastDigit;
        boolean negative = false;

        if (lastChar == '{') { lastDigit = 0; }
        else if (lastChar >= 'A' && lastChar <= 'I') { lastDigit = lastChar - 'A' + 1; }
        else if (lastChar == '}') { lastDigit = 0; negative = true; }
        else if (lastChar >= 'J' && lastChar <= 'R') { lastDigit = lastChar - 'J' + 1; negative = true; }
        else if (Character.isDigit(lastChar)) {
            // Plain numeric, no sign encoding
            digits = raw;
            lastDigit = -1; // flag: don't append
        } else {
            return BigDecimal.ZERO;
        }

        String numStr;
        if (lastDigit >= 0) {
            numStr = digits + lastDigit;
        } else {
            numStr = digits;
        }

        BigDecimal value = new BigDecimal(numStr).movePointLeft(scale);
        return negative ? value.negate() : value;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        raw = raw.trim();
        try {
            return LocalDate.parse(raw, DATE_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime parseTimestamp(String raw) {
        if (raw == null || raw.isBlank()) return null;
        raw = raw.trim();
        try {
            return LocalDateTime.parse(raw, TS_FMT);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(raw.substring(0, 19),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private String substr(String line, int start, int length) {
        if (line.length() < start + length) {
            if (line.length() <= start) return "";
            return line.substring(start);
        }
        return line.substring(start, start + length);
    }

    /**
     * CVACT01Y.cpy ACCOUNT-RECORD (RECLN 300):
     * ACCT-ID PIC 9(11), ACCT-ACTIVE-STATUS PIC X(01),
     * ACCT-CURR-BAL S9(10)V99, ACCT-CREDIT-LIMIT S9(10)V99,
     * ACCT-CASH-CREDIT-LIMIT S9(10)V99, ACCT-OPEN-DATE X(10),
     * ACCT-EXPIRAION-DATE X(10), ACCT-REISSUE-DATE X(10),
     * ACCT-CURR-CYC-CREDIT S9(10)V99, ACCT-CURR-CYC-DEBIT S9(10)V99,
     * ACCT-ADDR-ZIP X(10), ACCT-GROUP-ID X(10), FILLER X(178)
     */
    private void loadAccounts() {
        List<String> lines = readLines("acctdata.txt");
        int pos;
        for (String line : lines) {
            pos = 0;
            Account a = new Account();
            a.setAcctId(Long.parseLong(substr(line, pos, 11).trim())); pos += 11;
            a.setActiveStatus(substr(line, pos, 1)); pos += 1;
            a.setCurrentBalance(parseSignedDecimal(substr(line, pos, 12), 2)); pos += 12;
            a.setCreditLimit(parseSignedDecimal(substr(line, pos, 12), 2)); pos += 12;
            a.setCashCreditLimit(parseSignedDecimal(substr(line, pos, 12), 2)); pos += 12;
            a.setOpenDate(parseDate(substr(line, pos, 10))); pos += 10;
            a.setExpirationDate(parseDate(substr(line, pos, 10))); pos += 10;
            a.setReissueDate(parseDate(substr(line, pos, 10))); pos += 10;
            a.setCurrentCycleCredit(parseSignedDecimal(substr(line, pos, 12), 2)); pos += 12;
            a.setCurrentCycleDebit(parseSignedDecimal(substr(line, pos, 12), 2)); pos += 12;
            a.setAddrZip(substr(line, pos, 10).trim()); pos += 10;
            a.setGroupId(substr(line, pos, 10).trim());
            accountRepo.save(a);
        }
        log.info("Loaded {} accounts", lines.size());
    }

    /**
     * CVACT02Y.cpy CARD-RECORD (RECLN 150):
     * CARD-NUM X(16), CARD-ACCT-ID 9(11), CARD-CVV-CD 9(03),
     * CARD-EMBOSSED-NAME X(50), CARD-EXPIRAION-DATE X(10),
     * CARD-ACTIVE-STATUS X(01), FILLER X(59)
     */
    private void loadCards() {
        List<String> lines = readLines("carddata.txt");
        int pos;
        for (String line : lines) {
            pos = 0;
            Card c = new Card();
            c.setCardNum(substr(line, pos, 16).trim()); pos += 16;
            c.setAcctId(Long.parseLong(substr(line, pos, 11).trim())); pos += 11;
            c.setCvvCode(Integer.parseInt(substr(line, pos, 3).trim())); pos += 3;
            c.setEmbossedName(substr(line, pos, 50).trim()); pos += 50;
            c.setExpirationDate(parseDate(substr(line, pos, 10))); pos += 10;
            c.setActiveStatus(substr(line, pos, 1));
            cardRepo.save(c);
        }
        log.info("Loaded {} cards", lines.size());
    }

    /**
     * CVCUS01Y.cpy CUSTOMER-RECORD (RECLN 500):
     * CUST-ID 9(09), CUST-FIRST-NAME X(25), CUST-MIDDLE-NAME X(25),
     * CUST-LAST-NAME X(25), CUST-ADDR-LINE-1 X(50), CUST-ADDR-LINE-2 X(50),
     * CUST-ADDR-LINE-3 X(50), CUST-ADDR-STATE-CD X(02),
     * CUST-ADDR-COUNTRY-CD X(03), CUST-ADDR-ZIP X(10),
     * CUST-PHONE-NUM-1 X(15), CUST-PHONE-NUM-2 X(15),
     * CUST-SSN 9(09), CUST-GOVT-ISSUED-ID X(20),
     * CUST-DOB-YYYY-MM-DD X(10), CUST-EFT-ACCOUNT-ID X(10),
     * CUST-PRI-CARD-HOLDER-IND X(01), CUST-FICO-CREDIT-SCORE 9(03), FILLER X(168)
     */
    private void loadCustomers() {
        List<String> lines = readLines("custdata.txt");
        int pos;
        for (String line : lines) {
            pos = 0;
            Customer c = new Customer();
            c.setCustId(Long.parseLong(substr(line, pos, 9).trim())); pos += 9;
            c.setFirstName(substr(line, pos, 25).trim()); pos += 25;
            c.setMiddleName(substr(line, pos, 25).trim()); pos += 25;
            c.setLastName(substr(line, pos, 25).trim()); pos += 25;
            c.setAddrLine1(substr(line, pos, 50).trim()); pos += 50;
            c.setAddrLine2(substr(line, pos, 50).trim()); pos += 50;
            c.setAddrLine3(substr(line, pos, 50).trim()); pos += 50;
            c.setAddrStateCd(substr(line, pos, 2).trim()); pos += 2;
            c.setAddrCountryCd(substr(line, pos, 3).trim()); pos += 3;
            c.setAddrZip(substr(line, pos, 10).trim()); pos += 10;
            c.setPhoneNum1(substr(line, pos, 15).trim()); pos += 15;
            c.setPhoneNum2(substr(line, pos, 15).trim()); pos += 15;
            c.setSsn(substr(line, pos, 9).trim()); pos += 9;
            c.setGovtIssuedId(substr(line, pos, 20).trim()); pos += 20;
            c.setDateOfBirth(parseDate(substr(line, pos, 10))); pos += 10;
            c.setEftAccountId(substr(line, pos, 10).trim()); pos += 10;
            c.setPriCardHolderInd(substr(line, pos, 1)); pos += 1;
            String ficoStr = substr(line, pos, 3).trim();
            c.setFicoCreditScore(ficoStr.isEmpty() ? 0 : Integer.parseInt(ficoStr));
            customerRepo.save(c);
        }
        log.info("Loaded {} customers", lines.size());
    }

    /**
     * CVACT03Y.cpy CARD-XREF-RECORD (RECLN 50):
     * XREF-CARD-NUM X(16), XREF-CUST-ID 9(09), XREF-ACCT-ID 9(11), FILLER X(14)
     */
    private void loadCardXrefs() {
        List<String> lines = readLines("cardxref.txt");
        int pos;
        for (String line : lines) {
            pos = 0;
            CardXref cx = new CardXref();
            cx.setCardNum(substr(line, pos, 16).trim()); pos += 16;
            cx.setCustId(Long.parseLong(substr(line, pos, 9).trim())); pos += 9;
            cx.setAcctId(Long.parseLong(substr(line, pos, 11).trim()));
            cardXrefRepo.save(cx);
        }
        log.info("Loaded {} card xrefs", lines.size());
    }

    /**
     * CVTRA06Y.cpy DALYTRAN-RECORD (RECLN 350):
     * DALYTRAN-ID X(16), DALYTRAN-TYPE-CD X(02), DALYTRAN-CAT-CD 9(04),
     * DALYTRAN-SOURCE X(10), DALYTRAN-DESC X(100),
     * DALYTRAN-AMT S9(09)V99, DALYTRAN-MERCHANT-ID 9(09),
     * DALYTRAN-MERCHANT-NAME X(50), DALYTRAN-MERCHANT-CITY X(50),
     * DALYTRAN-MERCHANT-ZIP X(10), DALYTRAN-CARD-NUM X(16),
     * DALYTRAN-ORIG-TS X(26), DALYTRAN-PROC-TS X(26), FILLER X(20)
     */
    private void loadDailyTransactions() {
        List<String> lines = readLines("dailytran.txt");
        int pos;
        for (String line : lines) {
            pos = 0;
            DailyTransaction dt = new DailyTransaction();
            dt.setTransactionId(substr(line, pos, 16).trim()); pos += 16;
            dt.setTypeCd(substr(line, pos, 2).trim()); pos += 2;
            String catStr = substr(line, pos, 4).trim();
            dt.setCategoryCd(catStr.isEmpty() ? 0 : Integer.parseInt(catStr)); pos += 4;
            dt.setSource(substr(line, pos, 10).trim()); pos += 10;
            dt.setDescription(substr(line, pos, 100).trim()); pos += 100;
            dt.setAmount(parseSignedDecimal(substr(line, pos, 11), 2)); pos += 11;
            dt.setMerchantId(Long.parseLong(substr(line, pos, 9).trim())); pos += 9;
            dt.setMerchantName(substr(line, pos, 50).trim()); pos += 50;
            dt.setMerchantCity(substr(line, pos, 50).trim()); pos += 50;
            dt.setMerchantZip(substr(line, pos, 10).trim()); pos += 10;
            dt.setCardNum(substr(line, pos, 16).trim()); pos += 16;
            dt.setOrigTimestamp(parseTimestamp(substr(line, pos, 26))); pos += 26;
            dt.setProcTimestamp(parseTimestamp(substr(line, pos, 26)));
            dailyTransactionRepo.save(dt);
        }
        log.info("Loaded {} daily transactions", lines.size());
    }

    /**
     * CVTRA01Y.cpy TRAN-CAT-BAL-RECORD (RECLN 50):
     * TRANCAT-ACCT-ID 9(11), TRANCAT-TYPE-CD X(02), TRANCAT-CD 9(04),
     * TRAN-CAT-BAL S9(09)V99, FILLER X(22)
     */
    private void loadTcatBalances() {
        List<String> lines = readLines("tcatbal.txt");
        int pos;
        for (String line : lines) {
            pos = 0;
            TransactionCategoryBalance tcb = new TransactionCategoryBalance();
            Long acctId = Long.parseLong(substr(line, pos, 11).trim()); pos += 11;
            String typeCd = substr(line, pos, 2).trim(); pos += 2;
            Integer catCd = Integer.parseInt(substr(line, pos, 4).trim()); pos += 4;
            tcb.setId(new TransactionCategoryBalanceId(acctId, typeCd, catCd));
            tcb.setBalance(parseSignedDecimal(substr(line, pos, 11), 2));
            tcatBalRepo.save(tcb);
        }
        log.info("Loaded {} transaction category balances", lines.size());
    }

    /**
     * CVTRA02Y.cpy DIS-GROUP-RECORD (RECLN 50):
     * DIS-ACCT-GROUP-ID X(10), DIS-TRAN-TYPE-CD X(02), DIS-TRAN-CAT-CD 9(04),
     * DIS-INT-RATE S9(04)V99, FILLER X(28)
     */
    private void loadDisclosureGroups() {
        List<String> lines = readLines("discgrp.txt");
        int pos;
        for (String line : lines) {
            pos = 0;
            DisclosureGroup dg = new DisclosureGroup();
            String groupId = substr(line, pos, 10).trim(); pos += 10;
            String typeCd = substr(line, pos, 2).trim(); pos += 2;
            Integer catCd = Integer.parseInt(substr(line, pos, 4).trim()); pos += 4;
            dg.setId(new DisclosureGroupId(groupId, typeCd, catCd));
            dg.setInterestRate(parseSignedDecimal(substr(line, pos, 6), 2));
            discGrpRepo.save(dg);
        }
        log.info("Loaded {} disclosure groups", lines.size());
    }

    /**
     * trantype.txt: TYPE-CODE X(02), TYPE-DESCRIPTION X(50), FILLER
     */
    private void loadTransactionTypes() {
        List<String> lines = readLines("trantype.txt");
        for (String line : lines) {
            TransactionType tt = new TransactionType();
            tt.setTypeCode(substr(line, 0, 2).trim());
            tt.setTypeDescription(substr(line, 2, 50).trim());
            tranTypeRepo.save(tt);
        }
        log.info("Loaded {} transaction types", lines.size());
    }

    /**
     * trancatg.txt: TYPE-CD X(02), CATEGORY-CD 9(04), CATEGORY-DESCRIPTION X(50), FILLER
     */
    private void loadTransactionCategories() {
        List<String> lines = readLines("trancatg.txt");
        for (String line : lines) {
            TransactionCategory tc = new TransactionCategory();
            String typeCd = substr(line, 0, 2).trim();
            Integer catCd = Integer.parseInt(substr(line, 2, 4).trim());
            tc.setId(new TransactionCategoryId(typeCd, catCd));
            tc.setCategoryDescription(substr(line, 6, 50).trim());
            tranCatRepo.save(tc);
        }
        log.info("Loaded {} transaction categories", lines.size());
    }

    /**
     * Load default admin and regular users with BCrypt-hashed passwords.
     */
    private void loadDefaultUsers() {
        if (userSecurityRepo.count() > 0) {
            log.info("Users already exist, skipping user seed.");
            return;
        }
        // Passwords stored as plaintext placeholders here.
        // The online module's SecurityConfig uses BCrypt.
        // Actual password hashing should be done when migrating to production.
        UserSecurity admin = new UserSecurity();
        admin.setUserId("admin001");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setPassword("$2a$10$ksNrcuaQdNL.SBqU1cFETe3us3IjD1O5oDOr1f8SKwOXksZh3mtGq");
        admin.setUserType(com.carddemo.enums.UserType.ADMIN);
        userSecurityRepo.save(admin);

        UserSecurity user = new UserSecurity();
        user.setUserId("user0001");
        user.setFirstName("Regular");
        user.setLastName("User");
        user.setPassword("$2a$10$HmwAp5DLMJT9groQT1OsYuhV3xaBncZrG9ID7ko.Yn9i2IQYOs0M2");
        user.setUserType(com.carddemo.enums.UserType.USER);
        userSecurityRepo.save(user);

        log.info("Loaded 2 default users (admin001, user0001)");
    }
}
