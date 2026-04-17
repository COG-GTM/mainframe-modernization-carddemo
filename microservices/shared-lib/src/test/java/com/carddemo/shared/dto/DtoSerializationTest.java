package com.carddemo.shared.dto;

import com.carddemo.shared.enums.AccountStatus;
import com.carddemo.shared.enums.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DtoSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // --- UserType enum ---

    @Test
    void userType_serializeAdmin() throws Exception {
        String json = objectMapper.writeValueAsString(UserType.ADMIN);
        assertEquals("\"A\"", json);
    }

    @Test
    void userType_serializeRegular() throws Exception {
        String json = objectMapper.writeValueAsString(UserType.REGULAR);
        assertEquals("\"U\"", json);
    }

    @Test
    void userType_deserializeFromChar() {
        assertEquals(UserType.ADMIN, UserType.fromCode('A'));
        assertEquals(UserType.REGULAR, UserType.fromCode('U'));
    }

    @Test
    void userType_deserializeFromString() {
        assertEquals(UserType.ADMIN, UserType.fromCode("A"));
        assertEquals(UserType.REGULAR, UserType.fromCode("U"));
    }

    @Test
    void userType_invalidCode_throws() {
        assertThrows(IllegalArgumentException.class, () -> UserType.fromCode('X'));
    }

    @Test
    void userType_invalidStringCode_throws() {
        assertThrows(IllegalArgumentException.class, () -> UserType.fromCode("XX"));
    }

    // --- AccountStatus enum ---

    @Test
    void accountStatus_serializeActive() throws Exception {
        String json = objectMapper.writeValueAsString(AccountStatus.ACTIVE);
        assertEquals("\"Y\"", json);
    }

    @Test
    void accountStatus_serializeInactive() throws Exception {
        String json = objectMapper.writeValueAsString(AccountStatus.INACTIVE);
        assertEquals("\"N\"", json);
    }

    @Test
    void accountStatus_deserializeFromChar() {
        assertEquals(AccountStatus.ACTIVE, AccountStatus.fromCode('Y'));
        assertEquals(AccountStatus.INACTIVE, AccountStatus.fromCode('N'));
    }

    @Test
    void accountStatus_invalidCode_throws() {
        assertThrows(IllegalArgumentException.class, () -> AccountStatus.fromCode('X'));
    }

    // --- CardDemoCommarea ---

    @Test
    void commarea_roundTrip() throws Exception {
        GeneralInfo generalInfo = new GeneralInfo("CC00", "COSGN00C", "CM00", "COMEN01C",
                "USER0001", UserType.REGULAR, 0);
        CustomerInfo customerInfo = new CustomerInfo(123456789L, "John", "M", "Doe");
        AccountInfo accountInfo = new AccountInfo(12345678901L, "Y");
        CardInfo cardInfo = new CardInfo("1234567890123456");

        CardDemoCommarea commarea = new CardDemoCommarea(
                generalInfo, customerInfo, accountInfo, cardInfo,
                "COSGN0", "COSGN00");

        String json = objectMapper.writeValueAsString(commarea);
        CardDemoCommarea deserialized = objectMapper.readValue(json, CardDemoCommarea.class);

        assertEquals(commarea.getGeneralInfo().getFromTranId(), deserialized.getGeneralInfo().getFromTranId());
        assertEquals(commarea.getGeneralInfo().getUserId(), deserialized.getGeneralInfo().getUserId());
        assertEquals(commarea.getGeneralInfo().getUserType(), deserialized.getGeneralInfo().getUserType());
        assertEquals(commarea.getGeneralInfo().getPgmContext(), deserialized.getGeneralInfo().getPgmContext());
        assertTrue(deserialized.getGeneralInfo().isEnter());
        assertFalse(deserialized.getGeneralInfo().isReenter());
        assertEquals(commarea.getCustomerInfo().getCustId(), deserialized.getCustomerInfo().getCustId());
        assertEquals(commarea.getCustomerInfo().getFirstName(), deserialized.getCustomerInfo().getFirstName());
        assertEquals(commarea.getAccountInfo().getAcctId(), deserialized.getAccountInfo().getAcctId());
        assertEquals(commarea.getCardInfo().getCardNum(), deserialized.getCardInfo().getCardNum());
        assertEquals(commarea.getLastMap(), deserialized.getLastMap());
        assertEquals(commarea.getLastMapset(), deserialized.getLastMapset());
    }

    @Test
    void commarea_jsonContainsExpectedFields() throws Exception {
        GeneralInfo generalInfo = new GeneralInfo("CC00", "COSGN00C", "CM00", "COMEN01C",
                "USER0001", UserType.ADMIN, 1);
        CardDemoCommarea commarea = new CardDemoCommarea(
                generalInfo, null, null, null, null, null);

        String json = objectMapper.writeValueAsString(commarea);
        assertTrue(json.contains("\"fromTranId\""));
        assertTrue(json.contains("\"userId\""));
        assertTrue(json.contains("\"userType\""));
        assertTrue(json.contains("\"pgmContext\""));
    }

    // --- CardXrefRecord ---

    @Test
    void cardXrefRecord_roundTrip() throws Exception {
        CardXrefRecord record = new CardXrefRecord("4111111111111111", 123456789L, 12345678901L);

        String json = objectMapper.writeValueAsString(record);
        CardXrefRecord deserialized = objectMapper.readValue(json, CardXrefRecord.class);

        assertEquals(record.getCardNum(), deserialized.getCardNum());
        assertEquals(record.getCustId(), deserialized.getCustId());
        assertEquals(record.getAcctId(), deserialized.getAcctId());
    }

    // --- TransactionRecord ---

    @Test
    void transactionRecord_roundTrip() throws Exception {
        TransactionRecord record = new TransactionRecord(
                "0000000000000001", "SA", 5001, "ONLINE",
                "Purchase at Store", new BigDecimal("1234.56"),
                123456789L, "Acme Corp", "New York", "10001",
                "4111111111111111",
                "2024-01-15-10.30.00.000000",
                "2024-01-15-10.30.01.000000");

        String json = objectMapper.writeValueAsString(record);
        TransactionRecord deserialized = objectMapper.readValue(json, TransactionRecord.class);

        assertEquals(record.getTranId(), deserialized.getTranId());
        assertEquals(record.getTranTypeCd(), deserialized.getTranTypeCd());
        assertEquals(record.getTranCatCd(), deserialized.getTranCatCd());
        assertEquals(record.getTranSource(), deserialized.getTranSource());
        assertEquals(record.getTranDesc(), deserialized.getTranDesc());
        assertEquals(0, record.getTranAmt().compareTo(deserialized.getTranAmt()));
        assertEquals(record.getMerchantId(), deserialized.getMerchantId());
        assertEquals(record.getMerchantName(), deserialized.getMerchantName());
        assertEquals(record.getMerchantCity(), deserialized.getMerchantCity());
        assertEquals(record.getMerchantZip(), deserialized.getMerchantZip());
        assertEquals(record.getCardNum(), deserialized.getCardNum());
        assertEquals(record.getOrigTimestamp(), deserialized.getOrigTimestamp());
        assertEquals(record.getProcTimestamp(), deserialized.getProcTimestamp());
    }

    @Test
    void transactionRecord_bigDecimalPrecision() throws Exception {
        TransactionRecord record = new TransactionRecord();
        record.setTranAmt(new BigDecimal("999999999.99"));

        String json = objectMapper.writeValueAsString(record);
        TransactionRecord deserialized = objectMapper.readValue(json, TransactionRecord.class);

        assertEquals(0, new BigDecimal("999999999.99").compareTo(deserialized.getTranAmt()));
    }

    // --- AccountRecord ---

    @Test
    void accountRecord_roundTrip() throws Exception {
        AccountRecord record = new AccountRecord(
                12345678901L, "Y",
                new BigDecimal("5000.00"), new BigDecimal("10000.00"),
                new BigDecimal("2000.00"),
                "2020-01-15", "2025-01-15", "2023-06-01",
                new BigDecimal("1500.00"), new BigDecimal("3500.00"),
                "10001", "GRP001");

        String json = objectMapper.writeValueAsString(record);
        AccountRecord deserialized = objectMapper.readValue(json, AccountRecord.class);

        assertEquals(record.getAcctId(), deserialized.getAcctId());
        assertEquals(record.getActiveStatus(), deserialized.getActiveStatus());
        assertEquals(0, record.getCurrentBalance().compareTo(deserialized.getCurrentBalance()));
        assertEquals(0, record.getCreditLimit().compareTo(deserialized.getCreditLimit()));
        assertEquals(0, record.getCashCreditLimit().compareTo(deserialized.getCashCreditLimit()));
        assertEquals(record.getOpenDate(), deserialized.getOpenDate());
        assertEquals(record.getExpirationDate(), deserialized.getExpirationDate());
        assertEquals(record.getReissueDate(), deserialized.getReissueDate());
        assertEquals(0, record.getCurrentCycleCredit().compareTo(deserialized.getCurrentCycleCredit()));
        assertEquals(0, record.getCurrentCycleDebit().compareTo(deserialized.getCurrentCycleDebit()));
        assertEquals(record.getAddressZip(), deserialized.getAddressZip());
        assertEquals(record.getGroupId(), deserialized.getGroupId());
    }

    @Test
    void accountRecord_negativeBalance() throws Exception {
        AccountRecord record = new AccountRecord();
        record.setCurrentBalance(new BigDecimal("-1234.56"));

        String json = objectMapper.writeValueAsString(record);
        AccountRecord deserialized = objectMapper.readValue(json, AccountRecord.class);

        assertEquals(0, new BigDecimal("-1234.56").compareTo(deserialized.getCurrentBalance()));
    }

    // --- Default constructors ---

    @Test
    void allDtos_haveDefaultConstructor() {
        assertDoesNotThrow(() -> new GeneralInfo());
        assertDoesNotThrow(() -> new CustomerInfo());
        assertDoesNotThrow(() -> new AccountInfo());
        assertDoesNotThrow(() -> new CardInfo());
        assertDoesNotThrow(() -> new CardDemoCommarea());
        assertDoesNotThrow(() -> new CardXrefRecord());
        assertDoesNotThrow(() -> new TransactionRecord());
        assertDoesNotThrow(() -> new AccountRecord());
    }
}
