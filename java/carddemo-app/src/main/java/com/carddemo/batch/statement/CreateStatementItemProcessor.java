package com.carddemo.batch.statement;

import com.carddemo.batch.statement.AccountStatement.StatementTransaction;
import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.domain.Transaction;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.Nullable;

/**
 * {@code ItemProcessor} for {@code creastmtJob}: gathers the data legacy {@code CBSTM03A} needs
 * to print one account statement.
 *
 * <p>For each {@link Account} (the {@code ItemReader} item, replacing the driving XREF/ACCT
 * sequential reads), it resolves the cardholder via the card cross-reference
 * ({@code CBSTM03B} XREF + CUSTFILE reads), gathers the account's transactions across all its
 * cards (the {@code WS-TRNX-TABLE} build + {@code 4000-TRNXFILE-GET} match loop) and computes
 * the statement total ({@code WS-TOTAL-AMT}, accumulated in {@link BigDecimal} at scale 2).</p>
 *
 * <p>Accounts with no cross-reference or no customer record yield no statement ({@code null},
 * filtered out by Spring Batch) rather than aborting the whole job.</p>
 */
public class CreateStatementItemProcessor implements ItemProcessor<Account, AccountStatement> {

    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public CreateStatementItemProcessor(CardXrefRepository cardXrefRepository,
                                        CustomerRepository customerRepository,
                                        TransactionRepository transactionRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Nullable
    @Override
    public AccountStatement process(Account account) {
        List<CardXref> xrefs = cardXrefRepository.findByXrefAcctId(account.getAcctId());
        if (xrefs.isEmpty()) {
            return null;
        }
        Customer customer = customerRepository.findById(xrefs.get(0).getXrefCustId()).orElse(null);
        if (customer == null) {
            return null;
        }

        List<Transaction> txns = new ArrayList<>();
        for (CardXref xref : xrefs) {
            txns.addAll(transactionRepository.findByTranCardNum(xref.getXrefCardNum()));
        }
        txns.sort(Comparator.comparing(Transaction::getTranCardNum,
                        Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(Transaction::getTranId,
                        Comparator.nullsFirst(Comparator.naturalOrder())));

        List<StatementTransaction> lines = new ArrayList<>(txns.size());
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Transaction t : txns) {
            BigDecimal amt = t.getTranAmt() == null ? BigDecimal.ZERO.setScale(2) : t.getTranAmt();
            lines.add(new StatementTransaction(t.getTranId(), t.getTranDesc(), amt));
            total = total.add(amt);
        }

        return new AccountStatement(
                account.getAcctId(),
                buildName(customer),
                nullToEmpty(customer.getCustAddrLine1()),
                nullToEmpty(customer.getCustAddrLine2()),
                buildAddressLine3(customer),
                account.getAcctCurrBal(),
                customer.getCustFicoCreditScore(),
                lines,
                total);
    }

    /** COBOL {@code ST-NAME}: first + ' ' + middle + ' ' + last (each DELIMITED BY ' '). */
    private static String buildName(Customer c) {
        return firstToken(c.getCustFirstName()) + " "
                + firstToken(c.getCustMiddleName()) + " "
                + firstToken(c.getCustLastName()) + " ";
    }

    /** COBOL {@code ST-ADD3}: addr-line-3 + ' ' + state + ' ' + country + ' ' + zip. */
    private static String buildAddressLine3(Customer c) {
        return firstToken(c.getCustAddrLine3()) + " "
                + firstToken(c.getCustAddrStateCd()) + " "
                + firstToken(c.getCustAddrCountryCd()) + " "
                + firstToken(c.getCustAddrZip()) + " ";
    }

    /** Emulates COBOL {@code STRING ... DELIMITED BY ' '}: content up to the first space. */
    private static String firstToken(String value) {
        if (value == null) {
            return "";
        }
        int space = value.indexOf(' ');
        return space < 0 ? value : value.substring(0, space);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
