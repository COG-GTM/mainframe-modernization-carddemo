package com.carddemo.interestcalc.service;

import com.carddemo.interestcalc.domain.AccountRecord;
import com.carddemo.interestcalc.domain.CardXrefRecord;
import com.carddemo.interestcalc.domain.DisclosureGroupRecord;
import com.carddemo.interestcalc.domain.TranCatBalRecord;
import com.carddemo.interestcalc.domain.TransactionRecord;
import com.carddemo.interestcalc.repository.AccountRepository;
import com.carddemo.interestcalc.repository.CardXrefRepository;
import com.carddemo.interestcalc.repository.DisclosureGroupRepository;
import com.carddemo.interestcalc.repository.TransactionWriter;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Java migration of batch program {@code app/cbl/CBACT04C.cbl} (interest calculator).
 *
 * <p>The PROCEDURE DIVISION main loop iterates the transaction category balance file
 * sequentially (records are keyed/ordered by account id + tran type + tran category),
 * performs a control break on account id change, looks up the account, the card
 * cross-reference and the disclosure group interest rate, computes monthly interest per
 * category, accumulates it per account, writes one interest transaction per category,
 * and rewrites the account record with the accumulated interest.
 *
 * <p>Each method maps 1:1 to a COBOL paragraph — see the README in {@code modernized/}
 * for the full paragraph-to-method table.
 */
public class InterestCalculationService {

    /** DB2 timestamp layout produced by paragraph {@code Z-GET-DB2-FORMAT-TIMESTAMP}: EEEE-MM-DD-UU.MM.SS.HH0000. */
    private static final DateTimeFormatter DB2_TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS'0000'");

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TransactionWriter transactionWriter;
    private final Clock clock;

    /** WS-MISC-VARS / WS-COUNTERS working storage. */
    private String lastAccountNum;                  // WS-LAST-ACCT-NUM  PIC X(11)
    private BigDecimal totalInterest;               // WS-TOTAL-INT      PIC S9(09)V99
    private boolean firstTime;                      // WS-FIRST-TIME     PIC X(01)
    private long recordCount;                       // WS-RECORD-COUNT   PIC 9(09)
    private long tranIdSuffix;                      // WS-TRANID-SUFFIX  PIC 9(06)

    /** Current account / xref "record buffers" (COBOL working storage from copybooks). */
    private AccountRecord account;                  // ACCOUNT-RECORD   (CVACT01Y)
    private CardXrefRecord cardXref;                // CARD-XREF-RECORD (CVACT03Y)

    public InterestCalculationService(AccountRepository accountRepository,
                                      CardXrefRepository cardXrefRepository,
                                      DisclosureGroupRepository disclosureGroupRepository,
                                      TransactionWriter transactionWriter,
                                      Clock clock) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.transactionWriter = transactionWriter;
        this.clock = clock;
    }

    /**
     * PROCEDURE DIVISION main loop ({@code PERFORM UNTIL END-OF-FILE = 'Y'}).
     *
     * @param tranCatBalRecords the TCATBAL file content in key sequence (sequential READ order)
     * @param parmDate          {@code PARM-DATE PIC X(10)} passed via JCL PARM — prefix for generated TRAN-IDs
     * @return number of TCATBAL records processed ({@code WS-RECORD-COUNT})
     */
    public long run(List<TranCatBalRecord> tranCatBalRecords, String parmDate) {
        lastAccountNum = null;
        totalInterest = BigDecimal.ZERO.setScale(2);
        firstTime = true;
        recordCount = 0;
        tranIdSuffix = 0;
        account = null;
        cardXref = null;

        for (TranCatBalRecord tcatbal : tranCatBalRecords) {     // 1000-TCATBALF-GET-NEXT
            recordCount++;
            String acctId = formatAccountId(tcatbal.accountId());
            if (!acctId.equals(lastAccountNum)) {
                if (!firstTime) {
                    updateAccount();                              // 1050-UPDATE-ACCOUNT
                } else {
                    firstTime = false;
                }
                totalInterest = BigDecimal.ZERO.setScale(2);
                lastAccountNum = acctId;
                getAccountData(tcatbal.accountId());              // 1100-GET-ACCT-DATA
                getXrefData(tcatbal.accountId());                 // 1110-GET-XREF-DATA
            }
            DisclosureGroupRecord discGroup = getInterestRate(    // 1200-GET-INTEREST-RATE
                    account.getGroupId(), tcatbal.typeCode(), tcatbal.categoryCode());
            if (discGroup != null && discGroup.interestRate().signum() != 0) {
                computeInterest(tcatbal, discGroup, parmDate);    // 1300-COMPUTE-INTEREST
                computeFees();                                    // 1400-COMPUTE-FEES
            }
        }

        // End-of-file flush of the final account's accumulated interest. This realizes the
        // intent of the COBOL main loop's ELSE PERFORM 1050-UPDATE-ACCOUNT branch (which is
        // unreachable as coded in CBACT04C because the PERFORM UNTIL exits as soon as
        // END-OF-FILE = 'Y'). See README "Behavioral notes".
        if (!firstTime) {
            updateAccount();                                      // 1050-UPDATE-ACCOUNT
        }
        return recordCount;
    }

    /**
     * Paragraph {@code 1050-UPDATE-ACCOUNT}: add accumulated interest to the account
     * balance, reset cycle credit/debit, and REWRITE the account record.
     */
    private void updateAccount() {
        account.setCurrentBalance(account.getCurrentBalance().add(totalInterest));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        accountRepository.update(account);
    }

    /** Paragraph {@code 1100-GET-ACCT-DATA}: keyed READ of the account master. */
    private void getAccountData(long accountId) {
        account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("ACCOUNT NOT FOUND: " + accountId));
    }

    /** Paragraph {@code 1110-GET-XREF-DATA}: READ of the xref file via the account-id alternate key. */
    private void getXrefData(long accountId) {
        cardXref = cardXrefRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalStateException("ACCOUNT NOT FOUND IN XREF: " + accountId));
    }

    /**
     * Paragraphs {@code 1200-GET-INTEREST-RATE} and {@code 1200-A-GET-DEFAULT-INT-RATE}:
     * keyed READ of the disclosure group file; on record-not-found (file status '23')
     * retry with group id {@code 'DEFAULT'}.
     */
    private DisclosureGroupRecord getInterestRate(String groupId, String typeCode, int categoryCode) {
        return disclosureGroupRepository.findByKey(groupId, typeCode, categoryCode)
                .orElseGet(() -> disclosureGroupRepository
                        .findByKey(DisclosureGroupRecord.DEFAULT_GROUP_ID, typeCode, categoryCode)
                        .orElseThrow(() -> new IllegalStateException(
                                "DEFAULT DISCLOSURE GROUP RECORD MISSING FOR " + typeCode + "/" + categoryCode)));
    }

    /**
     * Paragraph {@code 1300-COMPUTE-INTEREST}:
     * {@code COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200} (no ROUNDED →
     * truncation, see {@link InterestCalculator}), accumulate into WS-TOTAL-INT, then
     * write the interest transaction.
     */
    private void computeInterest(TranCatBalRecord tcatbal, DisclosureGroupRecord discGroup, String parmDate) {
        BigDecimal monthlyInterest = InterestCalculator.monthlyInterest(tcatbal.balance(), discGroup.interestRate());
        totalInterest = totalInterest.add(monthlyInterest);
        writeTransaction(monthlyInterest, parmDate);              // 1300-B-WRITE-TX
    }

    /**
     * Paragraph {@code 1300-B-WRITE-TX}: build and WRITE one interest transaction record.
     */
    private void writeTransaction(BigDecimal monthlyInterest, String parmDate) {
        tranIdSuffix++;
        // STRING PARM-DATE (X(10)), WS-TRANID-SUFFIX (9(06)) INTO TRAN-ID (X(16))
        String tranId = parmDate + String.format("%06d", tranIdSuffix);
        // STRING 'Int. for a/c ', ACCT-ID (9(11)) INTO TRAN-DESC
        String description = "Int. for a/c " + formatAccountId(account.getAccountId());
        String db2Timestamp = db2FormatTimestamp();               // Z-GET-DB2-FORMAT-TIMESTAMP

        transactionWriter.write(new TransactionRecord(
                tranId,
                "01",                                             // MOVE '01' TO TRAN-TYPE-CD
                "0005",                                           // MOVE '05' TO TRAN-CAT-CD PIC 9(04)
                "System",                                         // MOVE 'System' TO TRAN-SOURCE
                description,
                monthlyInterest,                                  // MOVE WS-MONTHLY-INT TO TRAN-AMT
                0L,                                               // MOVE 0 TO TRAN-MERCHANT-ID
                "",                                               // MOVE SPACES TO TRAN-MERCHANT-NAME
                "",                                               // MOVE SPACES TO TRAN-MERCHANT-CITY
                "",                                               // MOVE SPACES TO TRAN-MERCHANT-ZIP
                cardXref.cardNumber(),                            // MOVE XREF-CARD-NUM TO TRAN-CARD-NUM
                db2Timestamp,                                     // MOVE DB2-FORMAT-TS TO TRAN-ORIG-TS
                db2Timestamp));                                   // MOVE DB2-FORMAT-TS TO TRAN-PROC-TS
    }

    /** Paragraph {@code 1400-COMPUTE-FEES}: "To be implemented" in CBACT04C — intentionally a no-op. */
    private void computeFees() {
        // COBOL paragraph body is empty.
    }

    /** Paragraph {@code Z-GET-DB2-FORMAT-TIMESTAMP}: current time as a DB2 X(26) timestamp. */
    private String db2FormatTimestamp() {
        return LocalDateTime.now(clock).format(DB2_TS_FORMAT);
    }

    /** Zero-pads an account id to the COBOL display width of {@code ACCT-ID PIC 9(11)}. */
    private static String formatAccountId(long accountId) {
        return String.format("%011d", accountId);
    }
}
