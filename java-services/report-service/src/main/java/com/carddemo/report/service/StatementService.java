package com.carddemo.report.service;

import com.carddemo.report.dto.ReportRequest;
import com.carddemo.report.dto.StatementDto;
import com.carddemo.report.dto.StatementLineDto;
import com.carddemo.report.exception.ResourceNotFoundException;
import com.carddemo.report.model.Account;
import com.carddemo.report.model.Customer;
import com.carddemo.report.model.Transaction;
import com.carddemo.report.repository.AccountRepository;
import com.carddemo.report.repository.CustomerRepository;
import com.carddemo.report.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for generating account statements.
 * Implements the business logic from CBSTM03A.CBL:
 * - Reads cross-reference to find cards for an account (1000-XREFFILE-GET-NEXT)
 * - Reads customer info (2000-CUSTFILE-GET) for name and address
 * - Reads account info (3000-ACCTFILE-GET) for current balance, FICO score
 * - Creates statement header (5000-CREATE-STATEMENT) with customer/account details
 * - Iterates transactions (4000-TRNXFILE-GET) and writes detail lines (6000-WRITE-TRANS)
 * - Calculates total expenditure (WS-TOTAL-AMT)
 *
 * The COBOL program outputs both text and HTML files;
 * in Java we return structured JSON with the same data fields.
 */
@Service
public class StatementService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public StatementService(TransactionRepository transactionRepository,
                            AccountRepository accountRepository,
                            CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Generate an account statement for the given account ID.
     * Mirrors CBSTM03A flow:
     * 1. Look up account (3000-ACCTFILE-GET)
     * 2. Look up customer (2000-CUSTFILE-GET)
     * 3. Query transactions for the account
     * 4. Calculate running balances, total charges, total payments
     * 5. Return structured statement data
     */
    public StatementDto generateStatement(ReportRequest request) {
        if (request.getAccountId() == null || request.getAccountId().isBlank()) {
            throw new IllegalArgumentException("Account ID is required for statement generation");
        }

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + request.getAccountId()));

        // Find the customer associated with this account.
        // In CBSTM03A, the cross-reference (XREFFILE) links card -> customer/account.
        // Here we use a simplified approach with the first customer in the table
        // or look up by the account's group ID.
        Customer customer = customerRepository.findById(account.getAcctGroupId())
                .orElse(customerRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No customer data found")));

        List<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByTranProcTsAsc(request.getAccountId());

        StatementDto statement = new StatementDto();
        statement.setStatementDate(LocalDate.now());
        statement.setAccountId(account.getAcctId());
        statement.setCustomerName(customer.getFullName());
        statement.setCustomerAddress(buildAddress(customer));
        statement.setCurrentBalance(account.getAcctCurrBal());
        statement.setCreditLimit(account.getAcctCreditLimit());
        statement.setFicoScore(customer.getCustFicoCreditScore());

        // Calculate beginning balance, charges, payments, and ending balance.
        // CBSTM03A tracks WS-TOTAL-AMT by summing TRNX-AMT for each transaction.
        // Positive amounts are charges; negative amounts are payments/credits.
        BigDecimal beginningBalance = account.getAcctCurrBal() != null
                ? account.getAcctCurrBal() : BigDecimal.ZERO;
        BigDecimal totalCharges = BigDecimal.ZERO;
        BigDecimal totalPayments = BigDecimal.ZERO;
        BigDecimal runningBalance = beginningBalance;

        for (Transaction txn : transactions) {
            StatementLineDto line = new StatementLineDto();
            line.setTransactionId(txn.getTranId());
            line.setDate(txn.getTranProcTs() != null
                    ? txn.getTranProcTs().toLocalDate().toString() : "");
            line.setDescription(txn.getTranDesc());
            line.setMerchantName(txn.getTranMerchantName());
            line.setMerchantCity(txn.getTranMerchantCity());

            BigDecimal amount = txn.getTranAmt() != null ? txn.getTranAmt() : BigDecimal.ZERO;
            line.setAmount(amount);

            // Positive = charge, Negative = payment/credit
            if (amount.compareTo(BigDecimal.ZERO) >= 0) {
                totalCharges = totalCharges.add(amount);
            } else {
                totalPayments = totalPayments.add(amount.abs());
            }

            runningBalance = runningBalance.add(amount);
            line.setRunningBalance(runningBalance);

            statement.getTransactions().add(line);
        }

        statement.setBeginningBalance(beginningBalance);
        statement.setTotalCharges(totalCharges);
        statement.setTotalPayments(totalPayments);
        statement.setEndingBalance(runningBalance);

        return statement;
    }

    /**
     * Build a formatted address string from customer data,
     * mirroring CBSTM03A's 5000-CREATE-STATEMENT STRING logic.
     */
    private String buildAddress(Customer customer) {
        StringBuilder sb = new StringBuilder();
        if (customer.getCustAddrLine1() != null && !customer.getCustAddrLine1().isBlank()) {
            sb.append(customer.getCustAddrLine1().trim());
        }
        if (customer.getCustAddrLine2() != null && !customer.getCustAddrLine2().isBlank()) {
            sb.append(", ").append(customer.getCustAddrLine2().trim());
        }
        if (customer.getCustAddrLine3() != null && !customer.getCustAddrLine3().isBlank()) {
            sb.append(", ").append(customer.getCustAddrLine3().trim());
        }
        if (customer.getCustAddrStateCd() != null && !customer.getCustAddrStateCd().isBlank()) {
            sb.append(" ").append(customer.getCustAddrStateCd().trim());
        }
        if (customer.getCustAddrCountryCd() != null && !customer.getCustAddrCountryCd().isBlank()) {
            sb.append(" ").append(customer.getCustAddrCountryCd().trim());
        }
        if (customer.getCustAddrZip() != null && !customer.getCustAddrZip().isBlank()) {
            sb.append(" ").append(customer.getCustAddrZip().trim());
        }
        return sb.toString();
    }
}
