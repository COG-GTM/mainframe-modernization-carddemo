package com.carddemo.batch.batch;

import com.carddemo.batch.model.DailyTransaction;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.core.io.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Factory for creating a FlatFileItemReader that parses 350-byte fixed-width
 * daily transaction records per the CVTRA06Y copybook layout.
 *
 * Layout (350 bytes total):
 *   DALYTRAN-ID             PIC X(16)   bytes 1-16
 *   DALYTRAN-TYPE-CD        PIC X(02)   bytes 17-18
 *   DALYTRAN-CAT-CD         PIC 9(04)   bytes 19-22
 *   DALYTRAN-SOURCE         PIC X(10)   bytes 23-32
 *   DALYTRAN-DESC           PIC X(100)  bytes 33-132
 *   DALYTRAN-AMT            PIC S9(09)V99  bytes 133-143 (11 chars with implied decimal)
 *   DALYTRAN-MERCHANT-ID    PIC 9(09)   bytes 144-152
 *   DALYTRAN-MERCHANT-NAME  PIC X(50)   bytes 153-202
 *   DALYTRAN-MERCHANT-CITY  PIC X(50)   bytes 203-252
 *   DALYTRAN-MERCHANT-ZIP   PIC X(10)   bytes 253-262
 *   DALYTRAN-CARD-NUM       PIC X(16)   bytes 263-278
 *   DALYTRAN-ORIG-TS        PIC X(26)   bytes 279-304
 *   DALYTRAN-PROC-TS        PIC X(26)   bytes 305-330
 *   FILLER                  PIC X(20)   bytes 331-350
 */
public class DailyTransactionReader {

    /** DB2 timestamp format: YYYY-MM-DD-HH.MM.SS.HH0000 */
    static final DateTimeFormatter DB2_TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd-HH.mm.ss.")
            .appendValue(ChronoField.MICRO_OF_SECOND, 6)
            .toFormatter();

    private DailyTransactionReader() {
    }

    public static FlatFileItemReader<DailyTransaction> create(Resource inputResource) {
        FlatFileItemReader<DailyTransaction> reader = new FlatFileItemReader<>();
        reader.setResource(inputResource);
        reader.setName("dailyTransactionReader");

        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setNames(
                "transactionId", "typeCode", "categoryCode", "source", "description",
                "amount", "merchantId", "merchantName", "merchantCity", "merchantZip",
                "cardNumber", "originTimestamp", "processedTimestamp", "filler"
        );
        tokenizer.setColumns(
                new Range(1, 16),    // transactionId
                new Range(17, 18),   // typeCode
                new Range(19, 22),   // categoryCode
                new Range(23, 32),   // source
                new Range(33, 132),  // description
                new Range(133, 143), // amount (S9(09)V99 = 11 chars)
                new Range(144, 152), // merchantId
                new Range(153, 202), // merchantName
                new Range(203, 252), // merchantCity
                new Range(253, 262), // merchantZip
                new Range(263, 278), // cardNumber
                new Range(279, 304), // originTimestamp
                new Range(305, 330), // processedTimestamp
                new Range(331, 350)  // filler
        );
        tokenizer.setStrict(false);

        FieldSetMapper<DailyTransaction> fieldSetMapper = fieldSet -> {
            DailyTransaction txn = new DailyTransaction();
            txn.setTransactionId(fieldSet.readString("transactionId").trim());
            txn.setTypeCode(fieldSet.readString("typeCode").trim());
            txn.setCategoryCode(Integer.parseInt(fieldSet.readString("categoryCode").trim()));
            txn.setSource(fieldSet.readString("source").trim());
            txn.setDescription(fieldSet.readString("description").trim());

            String amountStr = fieldSet.readString("amount").trim();
            txn.setAmount(parseCobolDecimal(amountStr));

            txn.setMerchantId(Long.parseLong(fieldSet.readString("merchantId").trim()));
            txn.setMerchantName(fieldSet.readString("merchantName").trim());
            txn.setMerchantCity(fieldSet.readString("merchantCity").trim());
            txn.setMerchantZip(fieldSet.readString("merchantZip").trim());
            txn.setCardNumber(fieldSet.readString("cardNumber").trim());

            String origTs = fieldSet.readString("originTimestamp").trim();
            if (!origTs.isEmpty()) {
                txn.setOriginTimestamp(parseDb2Timestamp(origTs));
            }

            String procTs = fieldSet.readString("processedTimestamp").trim();
            if (!procTs.isEmpty()) {
                txn.setProcessedTimestamp(parseDb2Timestamp(procTs));
            }

            return txn;
        };

        reader.setLineMapper((line, lineNumber) -> {
            return fieldSetMapper.mapFieldSet(tokenizer.tokenize(line));
        });

        return reader;
    }

    /**
     * Parses a COBOL S9(09)V99 numeric string into BigDecimal.
     * The format is a signed number with implied 2 decimal places.
     * Examples: "00000010000" = 100.00, "-00000010000" = -100.00
     */
    static BigDecimal parseCobolDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        String cleaned = value.replace("+", "").trim();
        if (cleaned.contains(".")) {
            return new BigDecimal(cleaned);
        }
        boolean negative = cleaned.startsWith("-");
        String digits = cleaned.replace("-", "");
        if (digits.length() <= 2) {
            digits = String.format("%03d", Long.parseLong(digits));
        }
        String intPart = digits.substring(0, digits.length() - 2);
        String decPart = digits.substring(digits.length() - 2);
        String formatted = (negative ? "-" : "") + intPart + "." + decPart;
        return new BigDecimal(formatted);
    }

    /**
     * Parses a DB2 timestamp string (YYYY-MM-DD-HH.MM.SS.HHHHHH) into LocalDateTime.
     */
    static LocalDateTime parseDb2Timestamp(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(value.trim(), DB2_TIMESTAMP_FORMAT);
    }
}
