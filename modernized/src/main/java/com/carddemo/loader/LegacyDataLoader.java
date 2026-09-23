package com.carddemo.loader;

import com.carddemo.config.CardDemoProperties;
import com.carddemo.domain.Account;
import com.carddemo.domain.Card;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.domain.DailyTransaction;
import com.carddemo.domain.DisclosureGroup;
import com.carddemo.domain.DisclosureGroupId;
import com.carddemo.domain.SecUser;
import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.domain.TransactionCategoryBalanceId;
import com.carddemo.domain.TransactionCategoryType;
import com.carddemo.domain.TransactionCategoryTypeId;
import com.carddemo.domain.TransactionType;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.SecUserRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionCategoryTypeRepository;
import com.carddemo.repository.TransactionTypeRepository;
import com.carddemo.util.FixedWidthRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Java replacement for the IDCAMS REPRO load jobs (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE,
 * DISCGRP, TCATBALF, TRANCATG, TRANTYPE, DUSRSECJ and the daily transaction load).
 *
 * <p>Each loader parses the fixed width ASCII extract under {@code carddemo.data-directory} using
 * the byte offsets of the corresponding copybook.
 */
@Component
public class LegacyDataLoader {

    private static final Logger log = LoggerFactory.getLogger(LegacyDataLoader.class);

    /** Users are not shipped as an ASCII extract; they come from the DUSRSECJ in-stream data. */
    private static final String USER_SEED_RESOURCE = "legacy-data/usrsec.txt";

    private final CardDemoProperties properties;
    private final SecUserRepository users;
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final CardRepository cards;
    private final CardXrefRepository xrefs;
    private final TransactionTypeRepository transactionTypes;
    private final TransactionCategoryTypeRepository categoryTypes;
    private final DisclosureGroupRepository disclosureGroups;
    private final TransactionCategoryBalanceRepository categoryBalances;
    private final DailyTransactionRepository dailyTransactions;

    public LegacyDataLoader(CardDemoProperties properties,
                            SecUserRepository users,
                            CustomerRepository customers,
                            AccountRepository accounts,
                            CardRepository cards,
                            CardXrefRepository xrefs,
                            TransactionTypeRepository transactionTypes,
                            TransactionCategoryTypeRepository categoryTypes,
                            DisclosureGroupRepository disclosureGroups,
                            TransactionCategoryBalanceRepository categoryBalances,
                            DailyTransactionRepository dailyTransactions) {
        this.properties = properties;
        this.users = users;
        this.customers = customers;
        this.accounts = accounts;
        this.cards = cards;
        this.xrefs = xrefs;
        this.transactionTypes = transactionTypes;
        this.categoryTypes = categoryTypes;
        this.disclosureGroups = disclosureGroups;
        this.categoryBalances = categoryBalances;
        this.dailyTransactions = dailyTransactions;
    }

    /** Loads every legacy file, in dependency order. Returns the row count per table. */
    @Transactional
    public Map<String, Integer> loadAll() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("sec_user", loadUsers());
        counts.put("customer", load("custdata.txt", customers, LegacyDataLoader::customer));
        counts.put("account", load("acctdata.txt", accounts, LegacyDataLoader::account));
        counts.put("card", load("carddata.txt", cards, LegacyDataLoader::card));
        counts.put("card_xref", load("cardxref.txt", xrefs, LegacyDataLoader::cardXref));
        counts.put("transaction_type", load("trantype.txt", transactionTypes, LegacyDataLoader::transactionType));
        counts.put("transaction_category_type",
                load("trancatg.txt", categoryTypes, LegacyDataLoader::transactionCategoryType));
        counts.put("disclosure_group", load("discgrp.txt", disclosureGroups, LegacyDataLoader::disclosureGroup));
        counts.put("transaction_category_balance",
                load("tcatbal.txt", categoryBalances, LegacyDataLoader::categoryBalance));
        counts.put("daily_transaction", load("dailytran.txt", dailyTransactions, LegacyDataLoader::dailyTransaction));
        log.info("Loaded legacy CardDemo data: {}", counts);
        return counts;
    }

    @Transactional
    public int loadUsers() {
        List<SecUser> records = new ArrayList<>();
        for (String line : readSeedLines()) {
            FixedWidthRecord record = new FixedWidthRecord(line);
            SecUser user = new SecUser();
            user.setId(record.string(1, 8));
            user.setFirstName(record.string(9, 20));
            user.setLastName(record.string(29, 20));
            user.setPassword(record.string(49, 8));
            user.setUserType(record.string(57, 1));
            records.add(user);
        }
        users.saveAll(records);
        return records.size();
    }

    private <T> int load(String fileName, CrudRepository<T, ?> repository, Function<FixedWidthRecord, T> mapper) {
        Path file = Path.of(properties.getDataDirectory()).resolve(fileName);
        if (!Files.exists(file)) {
            log.warn("Legacy extract {} not found, skipping", file);
            return 0;
        }
        List<T> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.ISO_8859_1)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    records.add(mapper.apply(new FixedWidthRecord(line)));
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        repository.saveAll(records);
        return records.size();
    }

    private List<String> readSeedLines() {
        Path file = Path.of(properties.getDataDirectory()).resolve("usrsec.txt");
        try {
            if (Files.exists(file)) {
                return Files.readAllLines(file, StandardCharsets.ISO_8859_1);
            }
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(USER_SEED_RESOURCE)) {
                if (in == null) {
                    return List.of();
                }
                try (BufferedReader reader =
                             new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1))) {
                    return reader.lines().filter(line -> !line.isBlank()).toList();
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /** CVCUS01Y, 500 bytes. */
    static Customer customer(FixedWidthRecord record) {
        Customer customer = new Customer();
        customer.setId(record.longValue(1, 9));
        customer.setFirstName(record.string(10, 25));
        customer.setMiddleName(record.string(35, 25));
        customer.setLastName(record.string(60, 25));
        customer.setAddressLine1(record.string(85, 50));
        customer.setAddressLine2(record.string(135, 50));
        customer.setAddressLine3(record.string(185, 50));
        customer.setStateCode(record.string(235, 2));
        customer.setCountryCode(record.string(237, 3));
        customer.setZipCode(record.string(240, 10));
        customer.setPhoneNumber1(record.string(250, 15));
        customer.setPhoneNumber2(record.string(265, 15));
        customer.setSsn(record.longValue(280, 9));
        customer.setGovernmentIssuedId(record.string(289, 20));
        customer.setDateOfBirth(record.string(309, 10));
        customer.setEftAccountId(record.string(319, 10));
        customer.setPrimaryCardHolderIndicator(record.string(329, 1));
        customer.setFicoCreditScore(record.intValue(330, 3));
        return customer;
    }

    /** CVACT01Y, 300 bytes. */
    static Account account(FixedWidthRecord record) {
        Account account = new Account();
        account.setId(record.longValue(1, 11));
        account.setActiveStatus(record.string(12, 1));
        account.setCurrentBalance(record.signed(13, 12, 2));
        account.setCreditLimit(record.signed(25, 12, 2));
        account.setCashCreditLimit(record.signed(37, 12, 2));
        account.setOpenDate(record.string(49, 10));
        account.setExpirationDate(record.string(59, 10));
        account.setReissueDate(record.string(69, 10));
        account.setCurrentCycleCredit(record.signed(79, 12, 2));
        account.setCurrentCycleDebit(record.signed(91, 12, 2));
        account.setAddressZip(record.string(103, 10));
        account.setGroupId(record.string(113, 10));
        return account;
    }

    /** CVACT02Y, 150 bytes. */
    static Card card(FixedWidthRecord record) {
        Card card = new Card();
        card.setCardNumber(record.string(1, 16));
        card.setAccountId(record.longValue(17, 11));
        card.setCvvCode(record.intValue(28, 3));
        card.setEmbossedName(record.string(31, 50));
        card.setExpirationDate(record.string(81, 10));
        card.setActiveStatus(record.string(91, 1));
        return card;
    }

    /** CVACT03Y, 50 bytes. */
    static CardXref cardXref(FixedWidthRecord record) {
        CardXref xref = new CardXref();
        xref.setCardNumber(record.string(1, 16));
        xref.setCustomerId(record.longValue(17, 9));
        xref.setAccountId(record.longValue(26, 11));
        return xref;
    }

    /** CVTRA03Y, 60 bytes. */
    static TransactionType transactionType(FixedWidthRecord record) {
        TransactionType type = new TransactionType();
        type.setTypeCode(record.string(1, 2));
        type.setDescription(record.string(3, 50));
        return type;
    }

    /** CVTRA04Y, 60 bytes. */
    static TransactionCategoryType transactionCategoryType(FixedWidthRecord record) {
        TransactionCategoryType categoryType = new TransactionCategoryType();
        categoryType.setId(new TransactionCategoryTypeId(record.string(1, 2), record.intValue(3, 4)));
        categoryType.setDescription(record.string(7, 50));
        return categoryType;
    }

    /** CVTRA02Y, 50 bytes. */
    static DisclosureGroup disclosureGroup(FixedWidthRecord record) {
        DisclosureGroup group = new DisclosureGroup();
        group.setId(new DisclosureGroupId(record.string(1, 10), record.string(11, 2), record.intValue(13, 4)));
        group.setInterestRate(record.signed(17, 6, 2));
        return group;
    }

    /** CVTRA01Y, 50 bytes. */
    static TransactionCategoryBalance categoryBalance(FixedWidthRecord record) {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(
                record.longValue(1, 11), record.string(12, 2), record.intValue(14, 4));
        return new TransactionCategoryBalance(id, record.signed(18, 11, 2));
    }

    /** CVTRA06Y, 350 bytes. */
    static DailyTransaction dailyTransaction(FixedWidthRecord record) {
        DailyTransaction transaction = new DailyTransaction();
        transaction.setId(record.string(1, 16));
        transaction.setTypeCode(record.string(17, 2));
        transaction.setCategoryCode(record.intValue(19, 4));
        transaction.setSource(record.string(23, 10));
        transaction.setDescription(record.string(33, 100));
        transaction.setAmount(record.signed(133, 11, 2));
        transaction.setMerchantId(record.longValue(144, 9));
        transaction.setMerchantName(record.string(153, 50));
        transaction.setMerchantCity(record.string(203, 50));
        transaction.setMerchantZip(record.string(253, 10));
        transaction.setCardNumber(record.string(263, 16));
        transaction.setOriginTimestamp(record.string(279, 26));
        transaction.setProcessingTimestamp(record.string(305, 26));
        return transaction;
    }
}
