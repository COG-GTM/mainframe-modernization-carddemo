package com.carddemo.fixture;

import com.carddemo.domain.Account;
import com.carddemo.domain.Card;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.domain.DisclosureGroup;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategory;
import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.domain.TransactionType;
import com.carddemo.domain.User;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Loads the sample fixed-width data files shipped in {@code app/data/ASCII} into domain objects.
 *
 * <p>The files are exposed on the classpath under {@code carddemo-data/} by the {@code pom.xml}
 * resource mapping, so no copy of the sample data lives inside {@code java-app}. Records are padded
 * to their logical record length before parsing because the sample files have trailing blanks
 * stripped.
 *
 * <p>The USRSEC file has no ASCII counterpart, so its records come from {@link MockUserData}.
 */
@Component
public class FixedWidthFixtureLoader {

    public static final String DATA_CLASSPATH_PREFIX = "carddemo-data/";

    public List<Account> loadAccounts() {
        return load("acctdata.txt", Account.RECORD_LENGTH, Account::parse);
    }

    public List<Card> loadCards() {
        return load("carddata.txt", Card.RECORD_LENGTH, Card::parse);
    }

    public List<CardXref> loadCardXrefs() {
        return load("cardxref.txt", CardXref.RECORD_LENGTH, CardXref::parse);
    }

    public List<Customer> loadCustomers() {
        return load("custdata.txt", Customer.RECORD_LENGTH, Customer::parse);
    }

    /** The daily transaction input file (DALYTRAN, copybook CVTRA06Y). */
    public List<Transaction> loadDailyTransactions() {
        return load("dailytran.txt", Transaction.RECORD_LENGTH, Transaction::parse);
    }

    public List<DisclosureGroup> loadDisclosureGroups() {
        return load("discgrp.txt", DisclosureGroup.RECORD_LENGTH, DisclosureGroup::parse);
    }

    public List<TransactionCategoryBalance> loadTransactionCategoryBalances() {
        return load("tcatbal.txt", TransactionCategoryBalance.RECORD_LENGTH, TransactionCategoryBalance::parse);
    }

    public List<TransactionCategory> loadTransactionCategories() {
        return load("trancatg.txt", TransactionCategory.RECORD_LENGTH, TransactionCategory::parse);
    }

    public List<TransactionType> loadTransactionTypes() {
        return load("trantype.txt", TransactionType.RECORD_LENGTH, TransactionType::parse);
    }

    public List<User> loadUsers() {
        return MockUserData.users();
    }

    /** Loads every sample file into a single immutable snapshot of the canonical mock dataset. */
    public CardDemoDataSet loadAll() {
        return new CardDemoDataSet(
                loadAccounts(),
                loadCards(),
                loadCardXrefs(),
                loadCustomers(),
                loadDailyTransactions(),
                loadDisclosureGroups(),
                loadTransactionCategoryBalances(),
                loadTransactionCategories(),
                loadTransactionTypes(),
                loadUsers());
    }

    /**
     * Reads a fixed-width file from the classpath and maps each non-blank line with {@code parser}.
     *
     * @param fileName    file name relative to {@code carddemo-data/}
     * @param recordLength logical record length used to pad short lines
     */
    public <T> List<T> load(String fileName, int recordLength, Function<String, T> parser) {
        List<T> records = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(DATA_CLASSPATH_PREFIX + fileName);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.ISO_8859_1))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                records.add(parser.apply(padOrTruncate(line, recordLength)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read fixture file " + fileName, e);
        }
        return records;
    }

    private static String padOrTruncate(String line, int recordLength) {
        if (line.length() > recordLength) {
            return line.substring(0, recordLength);
        }
        return line + " ".repeat(recordLength - line.length());
    }
}
