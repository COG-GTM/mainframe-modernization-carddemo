package com.carddemo.batch.statement;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Checks the COBOL data-movement rules CBSTM03A relies on for the statement layout. */
class StatementFormatterTest {

    @Test
    void editsCurrentBalanceAsZeroFilledPicture() {
        // PIC 9(9).99- : nine zero-filled digits, trailing sign position.
        assertThat(StatementFormatter.editedZeroFilled(new BigDecimal("1234.56")))
                .isEqualTo("000001234.56 ")
                .hasSize(13);
        assertThat(StatementFormatter.editedZeroFilled(new BigDecimal("-42.07")))
                .isEqualTo("000000042.07-");
        assertThat(StatementFormatter.editedZeroFilled(BigDecimal.ZERO))
                .isEqualTo("000000000.00 ");
    }

    @Test
    void truncatesHighOrderDigitsBeyondNineIntegerPositions() {
        assertThat(StatementFormatter.editedZeroFilled(new BigDecimal("12345678901.23")))
                .isEqualTo("345678901.23 ");
    }

    @Test
    void editsTransactionAmountWithZeroSuppression() {
        // PIC Z(9).99- : leading zeros suppressed to spaces, trailing sign position.
        assertThat(StatementFormatter.editedSuppressed(new BigDecimal("100.00")))
                .isEqualTo("      100.00 ")
                .hasSize(13);
        assertThat(StatementFormatter.editedSuppressed(new BigDecimal("-1234.05")))
                .isEqualTo("     1234.05-");
        assertThat(StatementFormatter.editedSuppressed(new BigDecimal("0.75")))
                .isEqualTo("         .75 ");
        assertThat(StatementFormatter.editedSuppressed(BigDecimal.ZERO))
                .isEqualTo("         .00 ");
    }

    @Test
    void movesNumericDisplayItemsIntoAlphanumericFields() {
        // MOVE ACCT-ID PIC 9(11) TO ST-ACCT-ID PIC X(20).
        assertThat(StatementFormatter.numericToAlpha(11L, 11, 20)).isEqualTo("00000000011         ");
        // MOVE CUST-FICO-CREDIT-SCORE PIC 9(03) TO ST-FICO-SCORE PIC X(20).
        assertThat(StatementFormatter.numericToAlpha(750, 3, 20)).isEqualTo("750                 ");
    }

    @Test
    void transfersOnlyTheDelimitedPartOfASendingField() {
        assertThat(StatementFormatter.delimited("John", 25, " ")).isEqualTo("John");
        assertThat(StatementFormatter.delimited("Mary Jane", 25, " ")).isEqualTo("Mary");
        assertThat(StatementFormatter.delimited("", 25, " ")).isEmpty();
        assertThat(StatementFormatter.delimited("John A Doe ", 50, "  ")).isEqualTo("John A Doe");
    }
}
