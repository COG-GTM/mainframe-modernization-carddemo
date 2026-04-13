package com.carddemo.service;

import com.carddemo.entity.*;
import com.carddemo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class mirroring COBOL business operations identified in Phase 1.
 * Operations include keyed READ, sequential READ, REWRITE, WRITE, and DELETE
 * as found in programs COACTUPC, COCRDLIC, COTRN00C, COUSR00C, etc.
 */
@Service
@Transactional
public class CardDemoService {

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final DailyTransactionRepository dailyTransactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionCategoryBalanceRepository transactionCategoryBalanceRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final UserSecurityRepository userSecurityRepository;

    public CardDemoService(
            AccountRepository accountRepository,
            CardRepository cardRepository,
            CardXrefRepository cardXrefRepository,
            CustomerRepository customerRepository,
            TransactionRepository transactionRepository,
            DailyTransactionRepository dailyTransactionRepository,
            TransactionTypeRepository transactionTypeRepository,
            TransactionCategoryRepository transactionCategoryRepository,
            TransactionCategoryBalanceRepository transactionCategoryBalanceRepository,
            DisclosureGroupRepository disclosureGroupRepository,
            UserSecurityRepository userSecurityRepository) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.transactionCategoryRepository = transactionCategoryRepository;
        this.transactionCategoryBalanceRepository = transactionCategoryBalanceRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.userSecurityRepository = userSecurityRepository;
    }

    // === Account operations (mirrors COACTUPC keyed READ/REWRITE) ===

    public Optional<Account> findAccountById(Long acctId) {
        return accountRepository.findById(acctId);
    }

    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }

    public List<Account> findAccountsByStatus(String status) {
        return accountRepository.findByAcctActiveStatus(status);
    }

    public List<Account> findAccountsByGroupId(String groupId) {
        return accountRepository.findByAcctGroupId(groupId);
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    public void deleteAccount(Long acctId) {
        accountRepository.deleteById(acctId);
    }

    // === Card operations (mirrors COCRDLIC sequential READ, COCRDUPC REWRITE) ===

    public Optional<Card> findCardByNum(String cardNum) {
        return cardRepository.findById(cardNum);
    }

    public List<Card> findCardsByAcctId(Long acctId) {
        return cardRepository.findByCardAcctId(acctId);
    }

    public List<Card> findCardsByStatus(String status) {
        return cardRepository.findByCardActiveStatus(status);
    }

    public Card saveCard(Card card) {
        return cardRepository.save(card);
    }

    public void deleteCard(String cardNum) {
        cardRepository.deleteById(cardNum);
    }

    // === Card cross-reference operations ===

    public Optional<CardXref> findCardXrefByCardNum(String cardNum) {
        return cardXrefRepository.findById(cardNum);
    }

    public List<CardXref> findCardXrefsByCustId(Long custId) {
        return cardXrefRepository.findByXrefCustId(custId);
    }

    public List<CardXref> findCardXrefsByAcctId(Long acctId) {
        return cardXrefRepository.findByXrefAcctId(acctId);
    }

    // === Customer operations ===

    public Optional<Customer> findCustomerById(Long custId) {
        return customerRepository.findById(custId);
    }

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> findCustomersByLastName(String lastName) {
        return customerRepository.findByCustLastName(lastName);
    }

    public List<Customer> findCustomersByState(String stateCd) {
        return customerRepository.findByCustAddrStateCd(stateCd);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long custId) {
        customerRepository.deleteById(custId);
    }

    // === Transaction operations (mirrors COTRN00C sequential READ, COTRN02C WRITE) ===

    public Optional<Transaction> findTransactionById(String tranId) {
        return transactionRepository.findById(tranId);
    }

    public List<Transaction> findTransactionsByCardNum(String cardNum) {
        return transactionRepository.findByTranCardNum(cardNum);
    }

    public List<Transaction> findTransactionsByType(String typeCd) {
        return transactionRepository.findByTranTypeCd(typeCd);
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(String tranId) {
        transactionRepository.deleteById(tranId);
    }

    // === Daily transaction operations (mirrors CBTRN02C batch processing) ===

    public List<DailyTransaction> findAllDailyTransactions() {
        return dailyTransactionRepository.findAll();
    }

    public List<DailyTransaction> findDailyTransactionsByCardNum(String cardNum) {
        return dailyTransactionRepository.findByDalytranCardNum(cardNum);
    }

    public DailyTransaction saveDailyTransaction(DailyTransaction dailyTransaction) {
        return dailyTransactionRepository.save(dailyTransaction);
    }

    // === Reference data operations ===

    public List<TransactionType> findAllTransactionTypes() {
        return transactionTypeRepository.findAll();
    }

    public List<TransactionCategory> findCategoriesByType(String typeCd) {
        return transactionCategoryRepository.findByTranTypeCd(typeCd);
    }

    public List<TransactionCategoryBalance> findBalancesByAcctId(Long acctId) {
        return transactionCategoryBalanceRepository.findByTrancatAcctId(acctId);
    }

    public List<DisclosureGroup> findDisclosureGroupsByGroupId(String groupId) {
        return disclosureGroupRepository.findByDisAcctGroupId(groupId);
    }

    // === User security operations (mirrors COSGN00C authentication) ===

    public Optional<UserSecurity> findUserById(String userId) {
        return userSecurityRepository.findById(userId);
    }

    public List<UserSecurity> findUsersByType(String userType) {
        return userSecurityRepository.findBySecUsrType(userType);
    }

    public UserSecurity saveUser(UserSecurity user) {
        return userSecurityRepository.save(user);
    }

    public void deleteUser(String userId) {
        userSecurityRepository.deleteById(userId);
    }
}
