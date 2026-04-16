package com.carddemo.etl.parser;

import com.carddemo.etl.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses fixed-length EBCDIC record files into typed Java objects.
 * Each parse method reads the raw bytes and uses EbcdicParser for field extraction.
 */
public final class RecordParser {

    private RecordParser() {
    }

    /**
     * Read a file as fixed-length records of the given size.
     */
    private static List<byte[]> readRecords(Path filePath, int recordLength) throws IOException {
        byte[] data = Files.readAllBytes(filePath);
        List<byte[]> records = new ArrayList<>();
        for (int offset = 0; offset + recordLength <= data.length; offset += recordLength) {
            byte[] record = new byte[recordLength];
            System.arraycopy(data, offset, record, 0, recordLength);
            records.add(record);
        }
        return records;
    }

    // ---- Account (CVACT01Y, 300 bytes) ----

    public static List<AccountRecord> parseAccounts(Path filePath) throws IOException {
        List<byte[]> rawRecords = readRecords(filePath, 300);
        List<AccountRecord> results = new ArrayList<>();
        for (byte[] rec : rawRecords) {
            AccountRecord r = new AccountRecord();
            int pos = 0;
            r.setAcctId(EbcdicParser.parseUnsignedNumeric(rec, pos, 11));          pos += 11;
            r.setAcctActiveStatus(EbcdicParser.parseString(rec, pos, 1));           pos += 1;
            r.setAcctCurrBal(EbcdicParser.parseSignedDecimal(rec, pos, 12, 2));     pos += 12;
            r.setAcctCreditLimit(EbcdicParser.parseSignedDecimal(rec, pos, 12, 2)); pos += 12;
            r.setAcctCashCreditLimit(EbcdicParser.parseSignedDecimal(rec, pos, 12, 2)); pos += 12;
            r.setAcctOpenDate(EbcdicParser.parseString(rec, pos, 10));              pos += 10;
            r.setAcctExpirationDate(EbcdicParser.parseString(rec, pos, 10));        pos += 10;
            r.setAcctReissueDate(EbcdicParser.parseString(rec, pos, 10));           pos += 10;
            r.setAcctCurrCycCredit(EbcdicParser.parseSignedDecimal(rec, pos, 12, 2)); pos += 12;
            r.setAcctCurrCycDebit(EbcdicParser.parseSignedDecimal(rec, pos, 12, 2));  pos += 12;
            r.setAcctAddrZip(EbcdicParser.parseString(rec, pos, 10));               pos += 10;
            r.setAcctGroupId(EbcdicParser.parseString(rec, pos, 10));
            results.add(r);
        }
        return results;
    }

    // ---- Customer (CVCUS01Y, 500 bytes) ----

    public static List<CustomerRecord> parseCustomers(Path filePath) throws IOException {
        List<byte[]> rawRecords = readRecords(filePath, 500);
        List<CustomerRecord> results = new ArrayList<>();
        for (byte[] rec : rawRecords) {
            CustomerRecord r = new CustomerRecord();
            int pos = 0;
            r.setCustId(EbcdicParser.parseUnsignedNumeric(rec, pos, 9));        pos += 9;
            r.setCustFirstName(EbcdicParser.parseString(rec, pos, 25));         pos += 25;
            r.setCustMiddleName(EbcdicParser.parseString(rec, pos, 25));        pos += 25;
            r.setCustLastName(EbcdicParser.parseString(rec, pos, 25));          pos += 25;
            r.setCustAddrLine1(EbcdicParser.parseString(rec, pos, 50));         pos += 50;
            r.setCustAddrLine2(EbcdicParser.parseString(rec, pos, 50));         pos += 50;
            r.setCustAddrLine3(EbcdicParser.parseString(rec, pos, 50));         pos += 50;
            r.setCustAddrStateCd(EbcdicParser.parseString(rec, pos, 2));        pos += 2;
            r.setCustAddrCountryCd(EbcdicParser.parseString(rec, pos, 3));      pos += 3;
            r.setCustAddrZip(EbcdicParser.parseString(rec, pos, 10));           pos += 10;
            r.setCustPhoneNum1(EbcdicParser.parseString(rec, pos, 15));         pos += 15;
            r.setCustPhoneNum2(EbcdicParser.parseString(rec, pos, 15));         pos += 15;
            r.setCustSsn(EbcdicParser.parseUnsignedNumeric(rec, pos, 9));       pos += 9;
            r.setCustGovtIssuedId(EbcdicParser.parseString(rec, pos, 20));      pos += 20;
            r.setCustDobYyyyMmDd(EbcdicParser.parseString(rec, pos, 10));       pos += 10;
            r.setCustEftAccountId(EbcdicParser.parseString(rec, pos, 10));      pos += 10;
            r.setCustPriCardHolderInd(EbcdicParser.parseString(rec, pos, 1));   pos += 1;
            r.setCustFicoCreditScore(EbcdicParser.parseUnsignedInt(rec, pos, 3));
            results.add(r);
        }
        return results;
    }

    // ---- Card (CVACT02Y, 150 bytes) ----

    public static List<CardRecord> parseCards(Path filePath) throws IOException {
        List<byte[]> rawRecords = readRecords(filePath, 150);
        List<CardRecord> results = new ArrayList<>();
        for (byte[] rec : rawRecords) {
            CardRecord r = new CardRecord();
            int pos = 0;
            r.setCardNum(EbcdicParser.parseString(rec, pos, 16));                pos += 16;
            r.setCardAcctId(EbcdicParser.parseUnsignedNumeric(rec, pos, 11));    pos += 11;
            r.setCardCvvCd(EbcdicParser.parseUnsignedInt(rec, pos, 3));          pos += 3;
            r.setCardEmbossedName(EbcdicParser.parseString(rec, pos, 50));       pos += 50;
            r.setCardExpirationDate(EbcdicParser.parseString(rec, pos, 10));     pos += 10;
            r.setCardActiveStatus(EbcdicParser.parseString(rec, pos, 1));
            results.add(r);
        }
        return results;
    }

    // ---- Card Cross-Reference (CVACT03Y, 50 bytes) ----

    public static List<CardXrefRecord> parseCardXrefs(Path filePath) throws IOException {
        List<byte[]> rawRecords = readRecords(filePath, 50);
        List<CardXrefRecord> results = new ArrayList<>();
        for (byte[] rec : rawRecords) {
            CardXrefRecord r = new CardXrefRecord();
            int pos = 0;
            r.setXrefCardNum(EbcdicParser.parseString(rec, pos, 16));              pos += 16;
            r.setXrefCustId(EbcdicParser.parseUnsignedNumeric(rec, pos, 9));       pos += 9;
            r.setXrefAcctId(EbcdicParser.parseUnsignedNumeric(rec, pos, 11));
            results.add(r);
        }
        return results;
    }

    // ---- User Security (CSUSR01Y, 80 bytes) ----

    public static List<UserSecurityRecord> parseUserSecurity(Path filePath) throws IOException {
        List<byte[]> rawRecords = readRecords(filePath, 80);
        List<UserSecurityRecord> results = new ArrayList<>();
        for (byte[] rec : rawRecords) {
            UserSecurityRecord r = new UserSecurityRecord();
            int pos = 0;
            r.setSecUsrId(EbcdicParser.parseString(rec, pos, 8));        pos += 8;
            r.setSecUsrFname(EbcdicParser.parseString(rec, pos, 20));    pos += 20;
            r.setSecUsrLname(EbcdicParser.parseString(rec, pos, 20));    pos += 20;
            r.setSecUsrPwd(EbcdicParser.parseString(rec, pos, 8));       pos += 8;
            r.setSecUsrType(EbcdicParser.parseString(rec, pos, 1));
            results.add(r);
        }
        return results;
    }

    // ---- Transaction / Daily Transaction (CVTRA05Y / CVTRA06Y, 350 bytes) ----

    public static List<TransactionRecord> parseTransactions(Path filePath) throws IOException {
        List<byte[]> rawRecords = readRecords(filePath, 350);
        List<TransactionRecord> results = new ArrayList<>();
        for (byte[] rec : rawRecords) {
            results.add(parseTransactionRecord(rec));
        }
        return results;
    }

    private static TransactionRecord parseTransactionRecord(byte[] rec) {
        TransactionRecord r = new TransactionRecord();
        int pos = 0;
        r.setTranId(EbcdicParser.parseString(rec, pos, 16));                  pos += 16;
        r.setTranTypeCd(EbcdicParser.parseString(rec, pos, 2));               pos += 2;
        r.setTranCatCd(EbcdicParser.parseUnsignedInt(rec, pos, 4));           pos += 4;
        r.setTranSource(EbcdicParser.parseString(rec, pos, 10));              pos += 10;
        r.setTranDesc(EbcdicParser.parseString(rec, pos, 100));               pos += 100;
        r.setTranAmt(EbcdicParser.parseSignedDecimal(rec, pos, 11, 2));       pos += 11;
        r.setTranMerchantId(EbcdicParser.parseUnsignedNumeric(rec, pos, 9));  pos += 9;
        r.setTranMerchantName(EbcdicParser.parseString(rec, pos, 50));        pos += 50;
        r.setTranMerchantCity(EbcdicParser.parseString(rec, pos, 50));        pos += 50;
        r.setTranMerchantZip(EbcdicParser.parseString(rec, pos, 10));         pos += 10;
        r.setTranCardNum(EbcdicParser.parseString(rec, pos, 16));             pos += 16;
        r.setTranOrigTs(EbcdicParser.parseString(rec, pos, 26));              pos += 26;
        r.setTranProcTs(EbcdicParser.parseString(rec, pos, 26));
        return r;
    }

    // ---- Transaction Category Balance (CVTRA01Y, 50 bytes) ----

    public static List<TranCatBalRecord> parseTranCatBalances(Path filePath) throws IOException {
        List<byte[]> rawRecords = readRecords(filePath, 50);
        List<TranCatBalRecord> results = new ArrayList<>();
        for (byte[] rec : rawRecords) {
            TranCatBalRecord r = new TranCatBalRecord();
            int pos = 0;
            r.setTrancatAcctId(EbcdicParser.parseUnsignedNumeric(rec, pos, 11));   pos += 11;
            r.setTrancatTypeCd(EbcdicParser.parseString(rec, pos, 2));             pos += 2;
            r.setTrancatCd(EbcdicParser.parseUnsignedInt(rec, pos, 4));            pos += 4;
            r.setTranCatBal(EbcdicParser.parseSignedDecimal(rec, pos, 11, 2));
            results.add(r);
        }
        return results;
    }

    // ---- Disclosure Group (CVTRA02Y, 50 bytes) ----

    public static List<DisclosureGroupRecord> parseDisclosureGroups(Path filePath) throws IOException {
        List<byte[]> rawRecords = readRecords(filePath, 50);
        List<DisclosureGroupRecord> results = new ArrayList<>();
        for (byte[] rec : rawRecords) {
            DisclosureGroupRecord r = new DisclosureGroupRecord();
            int pos = 0;
            r.setDisAcctGroupId(EbcdicParser.parseString(rec, pos, 10));           pos += 10;
            r.setDisTranTypeCd(EbcdicParser.parseString(rec, pos, 2));             pos += 2;
            r.setDisTranCatCd(EbcdicParser.parseUnsignedInt(rec, pos, 4));         pos += 4;
            r.setDisIntRate(EbcdicParser.parseSignedDecimal(rec, pos, 6, 2));
            results.add(r);
        }
        return results;
    }

    // ---- Transaction Type (CVTRA03Y, 60 bytes) ----

    public static List<TranTypeRecord> parseTranTypes(Path filePath) throws IOException {
        List<byte[]> rawRecords = readRecords(filePath, 60);
        List<TranTypeRecord> results = new ArrayList<>();
        for (byte[] rec : rawRecords) {
            TranTypeRecord r = new TranTypeRecord();
            int pos = 0;
            r.setTranType(EbcdicParser.parseString(rec, pos, 2));             pos += 2;
            r.setTranTypeDesc(EbcdicParser.parseString(rec, pos, 50));
            results.add(r);
        }
        return results;
    }

    // ---- Transaction Category (CVTRA04Y, 60 bytes) ----

    public static List<TranCategoryRecord> parseTranCategories(Path filePath) throws IOException {
        List<byte[]> rawRecords = readRecords(filePath, 60);
        List<TranCategoryRecord> results = new ArrayList<>();
        for (byte[] rec : rawRecords) {
            TranCategoryRecord r = new TranCategoryRecord();
            int pos = 0;
            r.setTranTypeCd(EbcdicParser.parseString(rec, pos, 2));              pos += 2;
            r.setTranCatCd(EbcdicParser.parseUnsignedInt(rec, pos, 4));          pos += 4;
            r.setTranCatTypeDesc(EbcdicParser.parseString(rec, pos, 50));
            results.add(r);
        }
        return results;
    }
}
