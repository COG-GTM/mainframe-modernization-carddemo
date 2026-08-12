package com.carddemo.batch.posting;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.entity.DailyTransaction;
import com.carddemo.util.CobolUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * COBOL program: CBTRN02C — the REJECT-RECORD written by 2500-WRITE-REJECT-REC to DALYREJS
 * (RECFM=F, LRECL=430 in POSTTRAN.jcl): a 350-byte CVTRA06Y record plus the 80-byte trailer.
 */
class TransactionRejectRecordTest {

    private static final Path DATA = Path.of("../app/data/ASCII");

    @Test
    void rendersTheFullFourHundredAndThirtyByteRecord() {
        DailyTransaction tran = DailyTransaction.builder()
                .transactionId("0000000000683580")
                .typeCode("01")
                .categoryCode(1)
                .source("POS TERM")
                .description("Purchase at Abshire-Lowe")
                .amount(new BigDecimal("504.77"))
                .merchantId(800000000L)
                .merchantName("Abshire-Lowe")
                .merchantCity("North Enoshaven")
                .merchantZip("72112")
                .cardNumber("4859452612877065")
                .originTimestamp("2022-06-10 19:27:53.000000")
                .processTimestamp("")
                .build();

        TransactionRejectRecord reject =
                new TransactionRejectRecord(tran, PostingRejectReason.OVERLIMIT_TRANSACTION);
        String line = reject.toRecordLine();

        assertThat(line).hasSize(430);
        assertThat(reject.getTransactionData()).hasSize(TransactionRejectRecord.TRAN_DATA_LENGTH);
        assertThat(reject.getValidationTrailer()).hasSize(TransactionRejectRecord.TRAILER_LENGTH);
        assertThat(line.substring(0, 16)).isEqualTo("0000000000683580");
        // DALYTRAN-AMT PIC S9(09)V99, positive zoned decimal: 00000050477 -> 0000005047G
        assertThat(line.substring(132, 143)).isEqualTo("0000005047G");
        assertThat(line.substring(350, 354)).isEqualTo("0102");
        assertThat(line.substring(354).trim()).isEqualTo("OVERLIMIT TRANSACTION");
    }

    @Test
    void negativeAmountsUseTheNegativeOverpunch() {
        DailyTransaction tran = DailyTransaction.builder()
                .transactionId("0000000000000001")
                .typeCode("02")
                .categoryCode(2)
                .amount(new BigDecimal("-504.77"))
                .cardNumber("4859452612877065")
                .originTimestamp("2022-06-10 19:27:53.000000")
                .build();

        String data = new TransactionRejectRecord(tran, PostingRejectReason.INVALID_CARD_NUMBER)
                .getTransactionData();

        assertThat(data.substring(132, 143)).isEqualTo("0000005047P");
        assertThat(CobolUtils.decimal(data, 132, 11, 2)).isEqualByComparingTo("-504.77");
    }

    @Test
    void roundTripsTheSampleDalytranRecord() throws IOException {
        String line = Files.readAllLines(DATA.resolve("dailytran.txt"), StandardCharsets.ISO_8859_1)
                .get(0);
        DailyTransaction tran = parseDalytran(line);

        String rendered = new TransactionRejectRecord(tran, PostingRejectReason.ACCOUNT_NOT_FOUND)
                .getTransactionData();

        assertThat(rendered).isEqualTo(CobolUtils.padRight(line, 350));
    }

    /** CVTRA06Y offsets, mirroring the loader that reads app/data/ASCII/dailytran.txt. */
    private static DailyTransaction parseDalytran(String line) {
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
