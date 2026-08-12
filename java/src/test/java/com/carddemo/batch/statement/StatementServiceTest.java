package com.carddemo.batch.statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Customer;
import com.carddemo.model.entity.Transaction;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Verifies the CBSTM03A statement layout line by line. */
class StatementServiceTest {

    private static final String CARD = "4111111111111111";

    private StatementFileService fileService;
    private StatementService statementService;

    @BeforeEach
    void setUp() {
        fileService = mock(StatementFileService.class);
        statementService = new StatementService(fileService);
    }

    private CardXref xref() {
        return CardXref.builder().cardNumber(CARD).customerId(9L).accountId(11L).build();
    }

    private Customer customer() {
        return Customer.builder()
                .customerId(9L)
                .firstName("John")
                .middleName("A")
                .lastName("Doe")
                .addressLine1("100 Main St")
                .addressLine2("Apt 4")
                .addressLine3("Seattle")
                .stateCode("WA")
                .countryCode("USA")
                .zipCode("98101")
                .ficoCreditScore(750)
                .build();
    }

    private Account account() {
        return Account.builder().accountId(11L).currentBalance(new BigDecimal("1234.56")).build();
    }

    private Transaction transaction(String id, String description, String amount) {
        return Transaction.builder()
                .transactionId(id)
                .cardNumber(CARD)
                .description(description)
                .amount(new BigDecimal(amount))
                .build();
    }

    private StatementDocument statement() {
        return statementService.render(xref(), customer(), account(),
                List.of(transaction("0000000000000001", "GROCERY STORE", "100.00"),
                        transaction("0000000000000002", "REFUND", "-25.75")));
    }

    @Test
    void printsTheFixedWidthStatementHeader() {
        List<String> lines = statement().textLines();

        assertThat(lines).allSatisfy(line -> assertThat(line).hasSize(80));
        assertThat(lines.get(0))
                .isEqualTo("*".repeat(31) + "START OF STATEMENT" + "*".repeat(31));
        assertThat(lines.get(1)).isEqualTo("John A Doe " + " ".repeat(69));
        assertThat(lines.get(2)).isEqualTo("100 Main St" + " ".repeat(69));
        assertThat(lines.get(3)).isEqualTo("Apt 4" + " ".repeat(75));
        assertThat(lines.get(4)).isEqualTo("Seattle WA USA 98101 " + " ".repeat(59));
        assertThat(lines.get(5)).isEqualTo("-".repeat(80));
        assertThat(lines.get(6)).isEqualTo(" ".repeat(33) + "Basic Details " + " ".repeat(33));
        assertThat(lines.get(8))
                .isEqualTo("Account ID         :00000000011         " + " ".repeat(40));
        assertThat(lines.get(9))
                .isEqualTo("Current Balance    :000001234.56 " + " ".repeat(47));
        assertThat(lines.get(10))
                .isEqualTo("FICO Score         :750                 " + " ".repeat(40));
        assertThat(lines.get(12)).isEqualTo(" ".repeat(30) + "TRANSACTION SUMMARY " + " ".repeat(30));
        assertThat(lines.get(14))
                .isEqualTo("Tran ID         " + "Tran Details" + " ".repeat(39) + "  Tran Amount");
    }

    @Test
    void printsOneLinePerTransactionAndTheExpenseTotal() {
        StatementDocument statement = statement();
        List<String> lines = statement.textLines();

        assertThat(lines.get(16)).isEqualTo("0000000000000001 "
                + "GROCERY STORE" + " ".repeat(36) + "$      100.00 ");
        assertThat(lines.get(17)).isEqualTo("0000000000000002 "
                + "REFUND" + " ".repeat(43) + "$       25.75-");
        assertThat(lines.get(18)).isEqualTo("-".repeat(80));
        assertThat(lines.get(19))
                .isEqualTo("Total EXP:" + " ".repeat(56) + "$       74.25 ");
        assertThat(lines.get(20))
                .isEqualTo("*".repeat(32) + "END OF STATEMENT" + "*".repeat(32));
        assertThat(lines).hasSize(21);
        assertThat(statement.totalAmount()).isEqualByComparingTo("74.25");
    }

    @Test
    void printsAZeroTotalWhenTheCardHasNoTransactions() {
        StatementDocument statement =
                statementService.render(xref(), customer(), account(), List.of());
        List<String> lines = statement.textLines();

        assertThat(lines).hasSize(19);
        assertThat(lines.get(17)).isEqualTo("Total EXP:" + " ".repeat(56) + "$         .00 ");
        assertThat(statement.totalAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void printsTheHtmlStatementInTheSameOrderAsTheCobol() {
        List<String> lines = statement().htmlLines();

        assertThat(lines).allSatisfy(line -> assertThat(line).hasSize(100));
        assertThat(lines.stream().map(String::stripTrailing).toList())
                .startsWith("<!DOCTYPE html>",
                        "<html lang=\"en\">",
                        "<head>",
                        "<meta charset=\"utf-8\">",
                        "<title>HTML Table Layout</title>",
                        "</head>",
                        "<body style=\"margin:0px;\">",
                        "<table  align=\"center\" frame=\"box\" style=\"width:70%;"
                                + " font:12px Segoe UI,sans-serif;\">",
                        "<tr>",
                        "<td colspan=\"3\" style=\"padding:0px 5px;background-color:#1d1d96b3;\">",
                        "<h3>Statement for Account Number: 00000000011         </h3>")
                .contains("<p style=\"font-size:16px\">John A Doe  </p>",
                        "<p>100 Main St  </p>",
                        "<p>Apt 4  </p>",
                        "<p>Seattle WA USA 98101  </p>",
                        "<p>Account ID         : 00000000011         </p>",
                        "<p>Current Balance    : 000001234.56 </p>",
                        "<p>FICO Score         : 750                 </p>",
                        "<p>0000000000000001</p>",
                        "<p>      100.00 </p>",
                        "<p>       25.75-</p>")
                .endsWith("<tr>",
                        "<td colspan=\"3\" style=\"padding:0px 5px;background-color:#1d1d96b3;\">",
                        "<h3>End of Statement</h3>",
                        "</td>",
                        "</tr>",
                        "</table>",
                        "</body>",
                        "</html>");
    }

    @Test
    void printsOneStatementPerCrossReferenceRecord() {
        CardXref other = CardXref.builder()
                .cardNumber("4222222222222222").customerId(9L).accountId(11L).build();
        when(fileService.readXrefFile()).thenReturn(List.of(xref(), other));
        when(fileService.readTransactionFile())
                .thenReturn(List.of(transaction("0000000000000001", "GROCERY STORE", "100.00")));
        when(fileService.readCustomer(9L)).thenReturn(customer());
        when(fileService.readAccount(11L)).thenReturn(account());

        List<StatementDocument> statements = statementService.generateStatements();

        assertThat(statements).hasSize(2);
        assertThat(statements.get(0).totalAmount()).isEqualByComparingTo("100.00");
        assertThat(statements.get(1).totalAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void abendsWhenAKeyedReadFails() {
        when(fileService.readXrefFile()).thenReturn(List.of(xref()));
        when(fileService.readTransactionFile()).thenReturn(List.of());
        when(fileService.readCustomer(9L))
                .thenThrow(new StatementFileException("CUSTFILE", "23"));

        assertThatThrownBy(() -> statementService.generateStatements())
                .isInstanceOf(StatementFileException.class)
                .hasMessage("ERROR READING CUSTFILE RETURN CODE: 23");
    }
}
