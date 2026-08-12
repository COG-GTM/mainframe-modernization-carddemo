package com.carddemo.batch.interest;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.Transaction;
import com.carddemo.util.CobolUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * COBOL program: CBACT04C — interest calculator (replaces {@code app/jcl/INTCALC.jcl}).
 *
 * <p>Pure business arithmetic of the paragraphs 1300-COMPUTE-INTEREST, 1300-B-WRITE-TX,
 * 1050-UPDATE-ACCOUNT and Z-GET-DB2-FORMAT-TIMESTAMP, kept free of persistence so it can be
 * unit tested on its own. Copybooks: CVTRA01Y (TCATBALF), CVTRA02Y (DISCGRP), CVACT01Y
 * (ACCTDATA), CVACT03Y (CARDXREF), CVTRA05Y (TRANSACT).
 */
@Service
public class InterestCalculationService {

    /** Divisor of {@code COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200}. */
    public static final BigDecimal MONTHLY_DIVISOR = BigDecimal.valueOf(1200);

    /** TRAN-TYPE-CD moved by 1300-B-WRITE-TX. */
    public static final String INTEREST_TRAN_TYPE_CD = "01";

    /** TRAN-CAT-CD moved by 1300-B-WRITE-TX. */
    public static final int INTEREST_TRAN_CAT_CD = 5;

    /** TRAN-SOURCE moved by 1300-B-WRITE-TX. */
    public static final String INTEREST_TRAN_SOURCE = "System";

    /** Disclosure group used when the account group lookup fails with VSAM status 23. */
    public static final String DEFAULT_DISCLOSURE_GROUP = "DEFAULT";

    /**
     * {@code COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200}.
     *
     * <p>The COMPUTE carries no ROUNDED phrase, so the result is truncated (not half-up
     * rounded) into WS-MONTHLY-INT PIC S9(09)V99 — truncation towards zero for both signs.
     */
    public BigDecimal monthlyInterest(BigDecimal categoryBalance, BigDecimal annualInterestRate) {
        BigDecimal balance = CobolUtils.nvl(categoryBalance);
        BigDecimal rate = CobolUtils.nvl(annualInterestRate);
        return balance.multiply(rate).divide(MONTHLY_DIVISOR, 2, RoundingMode.DOWN);
    }

    /**
     * TRAN-ID built by {@code STRING PARM-DATE, WS-TRANID-SUFFIX DELIMITED BY SIZE}: the
     * ten-byte run date parameter followed by the six-digit PIC 9(06) counter, filling
     * TRAN-ID PIC X(16) exactly.
     */
    public String transactionId(String parmDate, int suffix) {
        return CobolUtils.padRight(parmDate, 10) + CobolUtils.padLeftZeros(suffix, 6);
    }

    /**
     * TRAN-DESC built by {@code STRING 'Int. for a/c ', ACCT-ID DELIMITED BY SIZE}, where
     * ACCT-ID is PIC 9(11) and therefore contributes eleven zero-padded digits.
     */
    public String interestDescription(Long accountId) {
        return "Int. for a/c " + CobolUtils.padLeftZeros(accountId, 11);
    }

    /**
     * Z-GET-DB2-FORMAT-TIMESTAMP: {@code EEEE-MM-DD-UU.MM.SS.HH0000}, i.e. the CURRENT-DATE
     * value down to hundredths of a second, padded with the literal '0000'.
     */
    public String db2Timestamp(LocalDateTime timestamp) {
        int hundredths = timestamp.getNano() / 10_000_000;
        return String.format("%04d-%02d-%02d-%02d.%02d.%02d.%02d0000",
                timestamp.getYear(), timestamp.getMonthValue(), timestamp.getDayOfMonth(),
                timestamp.getHour(), timestamp.getMinute(), timestamp.getSecond(), hundredths);
    }

    /** 1300-B-WRITE-TX: builds the interest transaction written to the TRANSACT file. */
    public Transaction buildInterestTransaction(String parmDate,
                                                int suffix,
                                                Long accountId,
                                                String cardNumber,
                                                BigDecimal monthlyInterest,
                                                LocalDateTime timestamp) {
        String db2Timestamp = db2Timestamp(timestamp);
        return Transaction.builder()
                .transactionId(transactionId(parmDate, suffix))
                .typeCode(INTEREST_TRAN_TYPE_CD)
                .categoryCode(INTEREST_TRAN_CAT_CD)
                .source(INTEREST_TRAN_SOURCE)
                .description(interestDescription(accountId))
                .amount(monthlyInterest)
                .merchantId(0L)
                .merchantName("")
                .merchantCity("")
                .merchantZip("")
                .cardNumber(cardNumber)
                .originTimestamp(db2Timestamp)
                .processTimestamp(db2Timestamp)
                .build();
    }

    /**
     * 1050-UPDATE-ACCOUNT: {@code ADD WS-TOTAL-INT TO ACCT-CURR-BAL} and reset of both cycle
     * buckets. The account instance is mutated in place, exactly like the REWRITE of the
     * ACCOUNT-RECORD area.
     */
    public void applyTotalInterest(Account account, BigDecimal totalInterest) {
        account.setCurrentBalance(CobolUtils.nvl(account.getCurrentBalance())
                .add(CobolUtils.nvl(totalInterest)));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
    }
}
