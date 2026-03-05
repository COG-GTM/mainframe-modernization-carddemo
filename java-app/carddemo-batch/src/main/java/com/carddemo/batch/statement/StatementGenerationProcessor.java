package com.carddemo.batch.statement;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Customer;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Statement generation processor — translates CBSTM03A.CBL and CBSTM03B.CBL.
 * 
 * Note: The original uses z/OS-specific PSA/TCB/TIOT control block addressing for DD names
 * and ALTER...GO TO for dynamic dispatch. These are replaced with:
 * - Spring @ConfigurationProperties for file/output paths
 * - Strategy pattern / simple if-else for file operations
 */
public class StatementGenerationProcessor implements ItemProcessor<Account, StatementData> {

    private static final Logger log = LoggerFactory.getLogger(StatementGenerationProcessor.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;

    public StatementGenerationProcessor(TransactionRepository transactionRepository,
                                         CardXrefRepository cardXrefRepository,
                                         CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public StatementData process(Account account) throws Exception {
        Long acctId = account.getAcctId();

        // Get customer info via card xref
        List<CardXref> xrefs = cardXrefRepository.findByAcctId(acctId);
        Customer customer = null;
        if (!xrefs.isEmpty()) {
            Optional<Customer> custOpt = customerRepository.findById(xrefs.get(0).getCustId());
            customer = custOpt.orElse(null);
        }

        // Get transactions for statement period (last 30 days)
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        List<Transaction> transactions = transactionRepository.findByAcctIdAndDateRange(acctId, startDate, endDate);

        // Calculate totals
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            BigDecimal amt = t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO;
            String typeCd = t.getTypeCd() != null ? t.getTypeCd().trim() : "";
            if ("02".equals(typeCd) || "03".equals(typeCd)) {
                totalCredits = totalCredits.add(amt);
            } else {
                totalDebits = totalDebits.add(amt);
            }
        }

        return new StatementData(
                account,
                customer,
                transactions,
                totalDebits,
                totalCredits,
                startDate.toLocalDate(),
                endDate.toLocalDate()
        );
    }
}
