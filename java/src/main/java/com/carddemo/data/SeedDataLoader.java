package com.carddemo.data;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.Card;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Customer;
import com.carddemo.model.entity.DailyTransaction;
import com.carddemo.model.entity.DisclosureGroup;
import com.carddemo.model.entity.DisclosureGroupId;
import com.carddemo.model.entity.SecurityUser;
import com.carddemo.model.entity.TransactionCategory;
import com.carddemo.model.entity.TransactionCategoryBalance;
import com.carddemo.model.entity.TransactionCategoryBalanceId;
import com.carddemo.model.entity.TransactionCategoryId;
import com.carddemo.model.entity.TransactionType;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.SecurityUserRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionCategoryRepository;
import com.carddemo.repository.TransactionTypeRepository;
import com.carddemo.util.CobolUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Loads the sample VSAM data shipped in {@code app/data/ASCII} into the relational database,
 * replacing the load JCLs (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, DISCGRP, TCATBALF,
 * TRANTYPE, TRANCATG, DUSRSECJ) and the fixed-width record layouts of the copybooks.
 */
@Component
public class SeedDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);

    private final Path dataDirectory;
    private final boolean enabled;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;
    private final DailyTransactionRepository dailyTransactionRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final SecurityUserRepository securityUserRepository;
    private final TransactionCategoryBalanceRepository transactionCategoryBalanceRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionTypeRepository transactionTypeRepository;

    public SeedDataLoader(@Value("${carddemo.data.directory:../app/data/ASCII}") String dataDirectory,
                          @Value("${carddemo.data.load-on-startup:true}") boolean enabled,
                          AccountRepository accountRepository,
                          CardRepository cardRepository,
                          CardXrefRepository cardXrefRepository,
                          CustomerRepository customerRepository,
                          DailyTransactionRepository dailyTransactionRepository,
                          DisclosureGroupRepository disclosureGroupRepository,
                          SecurityUserRepository securityUserRepository,
                          TransactionCategoryBalanceRepository transactionCategoryBalanceRepository,
                          TransactionCategoryRepository transactionCategoryRepository,
                          TransactionTypeRepository transactionTypeRepository) {
        this.dataDirectory = Path.of(dataDirectory);
        this.enabled = enabled;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.securityUserRepository = securityUserRepository;
        this.transactionCategoryBalanceRepository = transactionCategoryBalanceRepository;
        this.transactionCategoryRepository = transactionCategoryRepository;
        this.transactionTypeRepository = transactionTypeRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (!enabled) {
            return;
        }
        load();
    }

    /** Loads every sample file that is present; missing files are skipped with a warning. */
    public void load() throws IOException {
        loadUsers();
        save("custdata.txt", customerRepository, SeedDataLoader::toCustomer);
        save("acctdata.txt", accountRepository, SeedDataLoader::toAccount);
        save("carddata.txt", cardRepository, SeedDataLoader::toCard);
        save("cardxref.txt", cardXrefRepository, SeedDataLoader::toCardXref);
        save("discgrp.txt", disclosureGroupRepository, SeedDataLoader::toDisclosureGroup);
        save("tcatbal.txt", transactionCategoryBalanceRepository, SeedDataLoader::toCategoryBalance);
        save("trantype.txt", transactionTypeRepository, SeedDataLoader::toTransactionType);
        save("trancatg.txt", transactionCategoryRepository, SeedDataLoader::toTransactionCategory);
        save("dailytran.txt", dailyTransactionRepository, SeedDataLoader::toDailyTransaction);
    }

    /**
     * Users are defined inline in DUSRSECJ.jcl rather than in a sample data file, so the two
     * demo sign-on credentials are seeded directly (ADMIN001/USER0001, password ADMIN001).
     */
    private void loadUsers() {
        if (securityUserRepository.count() > 0) {
            return;
        }
        securityUserRepository.saveAll(List.of(
                SecurityUser.builder().userId("ADMIN001").firstName("Admin").lastName("User")
                        .password("ADMIN001").userType("A").build(),
                SecurityUser.builder().userId("USER0001").firstName("Regular").lastName("User")
                        .password("USER0001").userType("U").build()));
    }

    private <T> void save(String fileName,
                          org.springframework.data.jpa.repository.JpaRepository<T, ?> repository,
                          Function<String, T> mapper) throws IOException {
        if (repository.count() > 0) {
            return;
        }
        Path file = dataDirectory.resolve(fileName);
        if (!Files.exists(file)) {
            log.warn("Sample data file {} not found; skipping load", file.toAbsolutePath());
            return;
        }
        List<T> records = new ArrayList<>();
        for (String line : Files.readAllLines(file, StandardCharsets.ISO_8859_1)) {
            if (!line.isBlank()) {
                records.add(mapper.apply(line));
            }
        }
        repository.saveAll(records);
        log.info("Loaded {} records from {}", records.size(), fileName);
    }

    /** Copybook CVCUS01Y, RECLN 500. */
    static Customer toCustomer(String line) {
        return Customer.builder()
                .customerId(CobolUtils.num(line, 0, 9))
                .firstName(CobolUtils.str(line, 9, 25))
                .middleName(CobolUtils.str(line, 34, 25))
                .lastName(CobolUtils.str(line, 59, 25))
                .addressLine1(CobolUtils.str(line, 84, 50))
                .addressLine2(CobolUtils.str(line, 134, 50))
                .addressLine3(CobolUtils.str(line, 184, 50))
                .stateCode(CobolUtils.str(line, 234, 2))
                .countryCode(CobolUtils.str(line, 236, 3))
                .zipCode(CobolUtils.str(line, 239, 10))
                .phoneNumber1(CobolUtils.str(line, 249, 15))
                .phoneNumber2(CobolUtils.str(line, 264, 15))
                .ssn(CobolUtils.num(line, 279, 9))
                .governmentIssuedId(CobolUtils.str(line, 288, 20))
                .dateOfBirth(CobolUtils.str(line, 308, 10))
                .eftAccountId(CobolUtils.str(line, 318, 10))
                .primaryCardHolderIndicator(CobolUtils.str(line, 328, 1))
                .ficoCreditScore(CobolUtils.intNum(line, 329, 3))
                .build();
    }

    /** Copybook CVACT01Y, RECLN 300. */
    static Account toAccount(String line) {
        return Account.builder()
                .accountId(CobolUtils.num(line, 0, 11))
                .activeStatus(CobolUtils.str(line, 11, 1))
                .currentBalance(CobolUtils.decimal(line, 12, 12, 2))
                .creditLimit(CobolUtils.decimal(line, 24, 12, 2))
                .cashCreditLimit(CobolUtils.decimal(line, 36, 12, 2))
                .openDate(CobolUtils.str(line, 48, 10))
                .expirationDate(CobolUtils.str(line, 58, 10))
                .reissueDate(CobolUtils.str(line, 68, 10))
                .currentCycleCredit(CobolUtils.decimal(line, 78, 12, 2))
                .currentCycleDebit(CobolUtils.decimal(line, 90, 12, 2))
                .addressZip(CobolUtils.str(line, 102, 10))
                .groupId(CobolUtils.str(line, 112, 10))
                .build();
    }

    /** Copybook CVACT02Y, RECLN 150. */
    static Card toCard(String line) {
        return Card.builder()
                .cardNumber(CobolUtils.str(line, 0, 16))
                .accountId(CobolUtils.num(line, 16, 11))
                .cvvCode(CobolUtils.intNum(line, 27, 3))
                .embossedName(CobolUtils.str(line, 30, 50))
                .expirationDate(CobolUtils.str(line, 80, 10))
                .activeStatus(CobolUtils.str(line, 90, 1))
                .build();
    }

    /** Copybook CVACT03Y, RECLN 50. */
    static CardXref toCardXref(String line) {
        return CardXref.builder()
                .cardNumber(CobolUtils.str(line, 0, 16))
                .customerId(CobolUtils.num(line, 16, 9))
                .accountId(CobolUtils.num(line, 25, 11))
                .build();
    }

    /** Copybook CVTRA02Y, RECLN 50. */
    static DisclosureGroup toDisclosureGroup(String line) {
        return DisclosureGroup.builder()
                .id(DisclosureGroupId.builder()
                        .accountGroupId(CobolUtils.str(line, 0, 10))
                        .transactionTypeCode(CobolUtils.str(line, 10, 2))
                        .transactionCategoryCode(CobolUtils.intNum(line, 12, 4))
                        .build())
                .interestRate(CobolUtils.decimal(line, 16, 6, 2))
                .build();
    }

    /** Copybook CVTRA01Y, RECLN 50. */
    static TransactionCategoryBalance toCategoryBalance(String line) {
        return TransactionCategoryBalance.builder()
                .id(TransactionCategoryBalanceId.builder()
                        .accountId(CobolUtils.num(line, 0, 11))
                        .typeCode(CobolUtils.str(line, 11, 2))
                        .categoryCode(CobolUtils.intNum(line, 13, 4))
                        .build())
                .balance(CobolUtils.decimal(line, 17, 11, 2))
                .build();
    }

    /** Copybook CVTRA03Y, RECLN 60. */
    static TransactionType toTransactionType(String line) {
        return TransactionType.builder()
                .typeCode(CobolUtils.str(line, 0, 2))
                .description(CobolUtils.str(line, 2, 50))
                .build();
    }

    /** Copybook CVTRA04Y, RECLN 60. */
    static TransactionCategory toTransactionCategory(String line) {
        return TransactionCategory.builder()
                .id(TransactionCategoryId.builder()
                        .typeCode(CobolUtils.str(line, 0, 2))
                        .categoryCode(CobolUtils.intNum(line, 2, 4))
                        .build())
                .description(CobolUtils.str(line, 6, 50))
                .build();
    }

    /** Copybook CVTRA06Y, RECLN 350. */
    static DailyTransaction toDailyTransaction(String line) {
        return DailyTransaction.builder()
                .transactionId(CobolUtils.str(line, 0, 16))
                .typeCode(CobolUtils.str(line, 16, 2))
                .categoryCode(CobolUtils.intNum(line, 18, 4))
                .source(CobolUtils.str(line, 22, 10))
                .description(CobolUtils.str(line, 32, 100))
                .amount(CobolUtils.decimal(line, 132, 11, 2))
                .merchantId(CobolUtils.num(line, 143, 9))
                .merchantName(CobolUtils.str(line, 152, 50))
                .merchantCity(CobolUtils.str(line, 202, 50))
                .merchantZip(CobolUtils.str(line, 252, 10))
                .cardNumber(CobolUtils.str(line, 262, 16))
                .originTimestamp(CobolUtils.str(line, 278, 26))
                .processTimestamp(CobolUtils.str(line, 304, 26))
                .build();
    }
}
