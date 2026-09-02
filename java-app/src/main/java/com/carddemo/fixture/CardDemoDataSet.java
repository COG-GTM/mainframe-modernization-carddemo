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

import java.util.List;

/**
 * The canonical mock dataset: every sample file parsed into domain objects.
 *
 * <p>All online and batch components seed themselves from this snapshot so that programs can be
 * exercised without VSAM or a real database.
 */
public record CardDemoDataSet(
        List<Account> accounts,
        List<Card> cards,
        List<CardXref> cardXrefs,
        List<Customer> customers,
        List<Transaction> dailyTransactions,
        List<DisclosureGroup> disclosureGroups,
        List<TransactionCategoryBalance> transactionCategoryBalances,
        List<TransactionCategory> transactionCategories,
        List<TransactionType> transactionTypes,
        List<User> users) {
}
