package com.carddemo.batch.statement;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Customer;
import com.carddemo.model.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Generates a statement for an account seeded from the sample VSAM extracts. */
@SpringBootTest
class StatementJobIntegrationTest {

    @Autowired
    private StatementService statementService;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private CardXref xref;

    @BeforeEach
    void seedTransactions() {
        transactionRepository.deleteAll();
        xref = cardXrefRepository.findAll().stream()
                .min((left, right) -> left.getCardNumber().compareTo(right.getCardNumber()))
                .orElseThrow();
        transactionRepository.saveAll(List.of(
                transaction("0000000000000001", "POS PURCHASE", "250.00"),
                transaction("0000000000000002", "PAYMENT THANK YOU", "-100.50")));
    }

    private Transaction transaction(String id, String description, String amount) {
        return Transaction.builder()
                .transactionId(id)
                .cardNumber(xref.getCardNumber())
                .typeCode("01")
                .categoryCode(1)
                .source("POS TERM")
                .description(description)
                .amount(new BigDecimal(amount))
                .build();
    }

    @Test
    void generatesOneStatementPerXrefRecordWithSeededMasterData() {
        List<StatementDocument> statements = statementService.generateStatements();

        assertThat(statements).hasSize((int) cardXrefRepository.count());

        StatementDocument statement = statements.stream()
                .filter(document -> document.cardNumber().equals(xref.getCardNumber()))
                .findFirst()
                .orElseThrow();
        Customer customer = customerRepository.findById(xref.getCustomerId()).orElseThrow();
        Account account = accountRepository.findById(xref.getAccountId()).orElseThrow();
        List<String> lines = statement.textLines();

        assertThat(lines.get(8))
                .isEqualTo("Account ID         :"
                        + String.format("%011d", account.getAccountId()) + " ".repeat(49));
        assertThat(lines.get(9))
                .startsWith("Current Balance    :"
                        + StatementFormatter.editedZeroFilled(account.getCurrentBalance()));
        assertThat(lines.get(10))
                .isEqualTo("FICO Score         :"
                        + String.format("%-20d", customer.getFicoCreditScore()) + " ".repeat(40));
        assertThat(lines.get(1)).startsWith(customer.getFirstName().split(" ")[0]);

        assertThat(lines.get(16)).isEqualTo("0000000000000001 "
                + "POS PURCHASE" + " ".repeat(37) + "$      250.00 ");
        assertThat(lines.get(17)).isEqualTo("0000000000000002 "
                + "PAYMENT THANK YOU" + " ".repeat(32) + "$      100.50-");
        assertThat(lines.get(19)).isEqualTo("Total EXP:" + " ".repeat(56) + "$      149.50 ");
        assertThat(statement.totalAmount()).isEqualByComparingTo("149.50");
        assertThat(statement.html()).contains("<h3>End of Statement</h3>");
    }
}
