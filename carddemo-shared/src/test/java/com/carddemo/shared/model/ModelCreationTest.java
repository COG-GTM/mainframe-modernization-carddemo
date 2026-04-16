package com.carddemo.shared.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the remaining model classes: CardXrefRecord, CustomerRecord,
 * SecUserData, TranCatBalRecord, DisGroupRecord, TranTypeRecord,
 * TransactionRecord, DailyTransactionRecord, StatementTransactionRecord,
 * CardDemoCommarea.
 */
class ModelCreationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // ---- CardXrefRecord (CVACT03Y) ----

    @Test
    void cardXrefRecord_createAndVerify() {
        CardXrefRecord record = new CardXrefRecord();
        record.setXrefCardNum("4111111111111111");
        record.setXrefCustId(123456789L);
        record.setXrefAcctId(12345678901L);

        assertEquals("4111111111111111", record.getXrefCardNum());
        assertEquals(123456789L, record.getXrefCustId());
        assertEquals(12345678901L, record.getXrefAcctId());
    }

    @Test
    void cardXrefRecord_equalsHashCodeToString() {
        CardXrefRecord r1 = new CardXrefRecord();
        r1.setXrefCardNum("4111111111111111");
        r1.setXrefCustId(1L);
        r1.setXrefAcctId(2L);

        CardXrefRecord r2 = new CardXrefRecord();
        r2.setXrefCardNum("4111111111111111");
        r2.setXrefCustId(1L);
        r2.setXrefAcctId(2L);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertTrue(r1.toString().contains("4111111111111111"));
    }

    @Test
    void cardXrefRecord_jsonRoundTrip() throws Exception {
        CardXrefRecord record = new CardXrefRecord();
        record.setXrefCardNum("5222222222222222");
        record.setXrefCustId(999999999L);
        record.setXrefAcctId(99999999999L);

        String json = mapper.writeValueAsString(record);
        CardXrefRecord deserialized = mapper.readValue(json, CardXrefRecord.class);
        assertEquals(record, deserialized);
    }

    // ---- CustomerRecord (CVCUS01Y) ----

    @Test
    void customerRecord_createAndVerify() {
        CustomerRecord record = new CustomerRecord();
        record.setCustId(123456789L);
        record.setCustFirstName("John");
        record.setCustMiddleName("M");
        record.setCustLastName("Doe");
        record.setCustAddrLine1("123 Main St");
        record.setCustAddrLine2("Apt 4B");
        record.setCustAddrLine3("");
        record.setCustAddrStateCd("NY");
        record.setCustAddrCountryCd("USA");
        record.setCustAddrZip("10001");
        record.setCustPhoneNum1("212-555-0100");
        record.setCustPhoneNum2("917-555-0200");
        record.setCustSsn(123456789L);
        record.setCustGovtIssuedId("DL12345678");
        record.setCustDobYyyyMmDd("1990-05-15");
        record.setCustEftAccountId("EFT0001234");
        record.setCustPriCardHolderInd("Y");
        record.setCustFicoCreditScore(750);

        assertEquals(123456789L, record.getCustId());
        assertEquals("John", record.getCustFirstName());
        assertEquals("Doe", record.getCustLastName());
        assertEquals("NY", record.getCustAddrStateCd());
        assertEquals(750, record.getCustFicoCreditScore());
    }

    @Test
    void customerRecord_equalsAndHashCode() {
        CustomerRecord r1 = new CustomerRecord();
        r1.setCustId(1L);
        r1.setCustFirstName("Jane");

        CustomerRecord r2 = new CustomerRecord();
        r2.setCustId(1L);
        r2.setCustFirstName("Jane");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void customerRecord_jsonRoundTrip() throws Exception {
        CustomerRecord record = new CustomerRecord();
        record.setCustId(999999999L);
        record.setCustFirstName("Test");
        record.setCustLastName("User");
        record.setCustFicoCreditScore(800);

        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("\"custId\":999999999"));
        CustomerRecord deserialized = mapper.readValue(json, CustomerRecord.class);
        assertEquals(record.getCustId(), deserialized.getCustId());
    }

    // ---- SecUserData (CSUSR01Y) ----

    @Test
    void secUserData_createAndVerify() {
        SecUserData data = new SecUserData();
        data.setSecUsrId("USER0001");
        data.setSecUsrFname("Admin");
        data.setSecUsrLname("User");
        data.setSecUsrPwd("PASS1234");
        data.setSecUsrType("A");

        assertEquals("USER0001", data.getSecUsrId());
        assertEquals("Admin", data.getSecUsrFname());
        assertEquals("User", data.getSecUsrLname());
        assertEquals("PASS1234", data.getSecUsrPwd());
        assertEquals("A", data.getSecUsrType());
        assertEquals(UserType.ADMIN, data.getUserType());
    }

    @Test
    void secUserData_getUserTypeReturnsNull() {
        SecUserData data = new SecUserData();
        assertNull(data.getUserType());
    }

    @Test
    void secUserData_passwordNotInToString() {
        SecUserData data = new SecUserData();
        data.setSecUsrPwd("SECRET");
        // toString intentionally omits password
        assertFalse(data.toString().contains("SECRET"));
    }

    @Test
    void secUserData_jsonRoundTrip() throws Exception {
        SecUserData data = new SecUserData();
        data.setSecUsrId("USER0002");
        data.setSecUsrType("U");

        String json = mapper.writeValueAsString(data);
        SecUserData deserialized = mapper.readValue(json, SecUserData.class);
        assertEquals(data, deserialized);
    }

    // ---- TranCatBalRecord (CVTRA01Y) ----

    @Test
    void tranCatBalRecord_createAndVerify() {
        TranCatBalRecord record = new TranCatBalRecord();
        record.setTrancatAcctId(12345678901L);
        record.setTrancatTypeCd("SA");
        record.setTrancatCd(1001);
        record.setTranCatBal(new BigDecimal("50000.99"));

        assertEquals(12345678901L, record.getTrancatAcctId());
        assertEquals("SA", record.getTrancatTypeCd());
        assertEquals(1001, record.getTrancatCd());
        assertEquals(new BigDecimal("50000.99"), record.getTranCatBal());
    }

    @Test
    void tranCatBalRecord_monetaryFieldIsBigDecimal() {
        TranCatBalRecord record = new TranCatBalRecord();
        record.setTranCatBal(new BigDecimal("-999999999.99"));
        assertInstanceOf(BigDecimal.class, record.getTranCatBal());
    }

    @Test
    void tranCatBalRecord_equalsHashCode() {
        TranCatBalRecord r1 = new TranCatBalRecord();
        r1.setTrancatAcctId(1L);
        r1.setTrancatTypeCd("SA");
        r1.setTrancatCd(100);
        r1.setTranCatBal(new BigDecimal("100.00"));

        TranCatBalRecord r2 = new TranCatBalRecord();
        r2.setTrancatAcctId(1L);
        r2.setTrancatTypeCd("SA");
        r2.setTrancatCd(100);
        r2.setTranCatBal(new BigDecimal("100.00"));

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    // ---- DisGroupRecord (CVTRA02Y) ----

    @Test
    void disGroupRecord_createAndVerify() {
        DisGroupRecord record = new DisGroupRecord();
        record.setDisAcctGroupId("GRP0000001");
        record.setDisTranTypeCd("SA");
        record.setDisTranCatCd(2001);
        record.setDisIntRate(new BigDecimal("1999.99"));

        assertEquals("GRP0000001", record.getDisAcctGroupId());
        assertEquals("SA", record.getDisTranTypeCd());
        assertEquals(2001, record.getDisTranCatCd());
        assertEquals(new BigDecimal("1999.99"), record.getDisIntRate());
    }

    @Test
    void disGroupRecord_jsonRoundTrip() throws Exception {
        DisGroupRecord record = new DisGroupRecord();
        record.setDisAcctGroupId("GRP001");
        record.setDisTranTypeCd("PR");
        record.setDisTranCatCd(100);
        record.setDisIntRate(new BigDecimal("15.50"));

        String json = mapper.writeValueAsString(record);
        DisGroupRecord deserialized = mapper.readValue(json, DisGroupRecord.class);
        assertEquals(record, deserialized);
    }

    // ---- TranTypeRecord (CVTRA03Y) ----

    @Test
    void tranTypeRecord_createAndVerify() {
        TranTypeRecord record = new TranTypeRecord();
        record.setTranType("SA");
        record.setTranTypeDesc("Sale Transaction");

        assertEquals("SA", record.getTranType());
        assertEquals("Sale Transaction", record.getTranTypeDesc());
    }

    @Test
    void tranTypeRecord_equalsHashCode() {
        TranTypeRecord r1 = new TranTypeRecord();
        r1.setTranType("CR");
        r1.setTranTypeDesc("Credit");

        TranTypeRecord r2 = new TranTypeRecord();
        r2.setTranType("CR");
        r2.setTranTypeDesc("Credit");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    // ---- TransactionRecord (CVTRA05Y) ----

    @Test
    void transactionRecord_createAndVerify() {
        TransactionRecord record = new TransactionRecord();
        record.setTranId("TXN0000000000001");
        record.setTranTypeCd("SA");
        record.setTranCatCd(5010);
        record.setTranSource("ONLINE");
        record.setTranDesc("Purchase at Store ABC");
        record.setTranAmt(new BigDecimal("125.50"));
        record.setTranMerchantId(123456789L);
        record.setTranMerchantName("Store ABC");
        record.setTranMerchantCity("New York");
        record.setTranMerchantZip("10001");
        record.setTranCardNum("4111111111111111");
        record.setTranOrigTs("2024-01-15-12.30.45.123456");
        record.setTranProcTs("2024-01-15-12.30.46.654321");

        assertEquals("TXN0000000000001", record.getTranId());
        assertEquals(new BigDecimal("125.50"), record.getTranAmt());
        assertEquals(123456789L, record.getTranMerchantId());
        assertInstanceOf(BigDecimal.class, record.getTranAmt());
    }

    @Test
    void transactionRecord_jsonRoundTrip() throws Exception {
        TransactionRecord record = new TransactionRecord();
        record.setTranId("TXN123");
        record.setTranAmt(new BigDecimal("99.99"));

        String json = mapper.writeValueAsString(record);
        TransactionRecord deserialized = mapper.readValue(json, TransactionRecord.class);
        assertEquals(record.getTranId(), deserialized.getTranId());
    }

    // ---- DailyTransactionRecord (CVTRA06Y) ----

    @Test
    void dailyTransactionRecord_createAndVerify() {
        DailyTransactionRecord record = new DailyTransactionRecord();
        record.setDalytranId("DALY000000000001");
        record.setDalytranTypeCd("SA");
        record.setDalytranCatCd(5010);
        record.setDalytranSource("POS");
        record.setDalytranDesc("Daily purchase");
        record.setDalytranAmt(new BigDecimal("250.00"));
        record.setDalytranMerchantId(987654321L);
        record.setDalytranMerchantName("Merchant XYZ");
        record.setDalytranMerchantCity("Chicago");
        record.setDalytranMerchantZip("60601");
        record.setDalytranCardNum("5222222222222222");
        record.setDalytranOrigTs("2024-02-01-08.00.00.000000");
        record.setDalytranProcTs("2024-02-01-08.00.01.000000");

        assertEquals("DALY000000000001", record.getDalytranId());
        assertEquals(new BigDecimal("250.00"), record.getDalytranAmt());
        assertInstanceOf(BigDecimal.class, record.getDalytranAmt());
    }

    @Test
    void dailyTransactionRecord_equalsHashCode() {
        DailyTransactionRecord r1 = new DailyTransactionRecord();
        r1.setDalytranId("D001");
        r1.setDalytranAmt(new BigDecimal("100.00"));

        DailyTransactionRecord r2 = new DailyTransactionRecord();
        r2.setDalytranId("D001");
        r2.setDalytranAmt(new BigDecimal("100.00"));

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    // ---- StatementTransactionRecord (COSTM01) ----

    @Test
    void statementTransactionRecord_createAndVerify() {
        StatementTransactionRecord record = new StatementTransactionRecord();
        record.setTrnxCardNum("4111111111111111");
        record.setTrnxId("STMT000000000001");
        record.setTrnxTypeCd("CR");
        record.setTrnxCatCd(6020);
        record.setTrnxSource("BATCH");
        record.setTrnxDesc("Statement credit");
        record.setTrnxAmt(new BigDecimal("500.00"));
        record.setTrnxMerchantId(111222333L);
        record.setTrnxMerchantName("Bank Corp");
        record.setTrnxMerchantCity("Boston");
        record.setTrnxMerchantZip("02101");
        record.setTrnxOrigTs("2024-03-01-00.00.00.000000");
        record.setTrnxProcTs("2024-03-01-00.00.01.000000");

        assertEquals("4111111111111111", record.getTrnxCardNum());
        assertEquals("STMT000000000001", record.getTrnxId());
        assertEquals(new BigDecimal("500.00"), record.getTrnxAmt());
        assertInstanceOf(BigDecimal.class, record.getTrnxAmt());
    }

    @Test
    void statementTransactionRecord_compositeKey() {
        StatementTransactionRecord r1 = new StatementTransactionRecord();
        r1.setTrnxCardNum("4111111111111111");
        r1.setTrnxId("TX001");

        StatementTransactionRecord r2 = new StatementTransactionRecord();
        r2.setTrnxCardNum("4111111111111111");
        r2.setTrnxId("TX001");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        r2.setTrnxId("TX002");
        assertNotEquals(r1, r2);
    }

    // ---- CardDemoCommarea (COCOM01Y) ----

    @Test
    void cardDemoCommarea_createAndVerify() {
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setCdemoFromTranid("CC00");
        commarea.setCdemoFromProgram("COSGN00C");
        commarea.setCdemoToTranid("CC01");
        commarea.setCdemoToProgram("COMEN01C");
        commarea.setCdemoUserId("USER0001");
        commarea.setCdemoUserType("A");
        commarea.setCdemoPgmContext(0);
        commarea.setCdemoCustId(123456789L);
        commarea.setCdemoCustFname("John");
        commarea.setCdemoCustMname("M");
        commarea.setCdemoCustLname("Doe");
        commarea.setCdemoAcctId(12345678901L);
        commarea.setCdemoAcctStatus("Y");
        commarea.setCdemoCardNum(4111111111111111L);
        commarea.setCdemoLastMap("MAP001");
        commarea.setCdemoLastMapset("MSET01");

        assertEquals("CC00", commarea.getCdemoFromTranid());
        assertEquals("COSGN00C", commarea.getCdemoFromProgram());
        assertEquals(UserType.ADMIN, commarea.getUserType());
        assertEquals(ProgramContext.ENTER, commarea.getProgramContext());
        assertEquals(123456789L, commarea.getCdemoCustId());
        assertEquals(4111111111111111L, commarea.getCdemoCardNum());
    }

    @Test
    void cardDemoCommarea_userTypeAndProgramContext() {
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setCdemoUserType("U");
        commarea.setCdemoPgmContext(1);

        assertEquals(UserType.USER, commarea.getUserType());
        assertEquals(ProgramContext.REENTER, commarea.getProgramContext());
    }

    @Test
    void cardDemoCommarea_nullUserType() {
        CardDemoCommarea commarea = new CardDemoCommarea();
        assertNull(commarea.getUserType());
    }

    @Test
    void cardDemoCommarea_equalsHashCode() {
        CardDemoCommarea c1 = new CardDemoCommarea();
        c1.setCdemoUserId("USER0001");
        c1.setCdemoUserType("A");

        CardDemoCommarea c2 = new CardDemoCommarea();
        c2.setCdemoUserId("USER0001");
        c2.setCdemoUserType("A");

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void cardDemoCommarea_jsonRoundTrip() throws Exception {
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setCdemoFromTranid("CC00");
        commarea.setCdemoUserId("ADMIN001");
        commarea.setCdemoCardNum(1234567890123456L);

        String json = mapper.writeValueAsString(commarea);
        CardDemoCommarea deserialized = mapper.readValue(json, CardDemoCommarea.class);
        assertEquals(commarea, deserialized);
    }
}
