package com.carddemo.etl.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modern representation of a CardDemo Account record (copybook {@code CVACT01Y} / VSAM
 * {@code ACCTDAT}). Field order and semantics mirror {@code ACCOUNT-RECORD}.
 *
 * @param acctId          {@code ACCT-ID} — PIC 9(11), primary key
 * @param activeStatus    {@code ACCT-ACTIVE-STATUS} — PIC X(01), 'Y' or 'N'
 * @param currBal         {@code ACCT-CURR-BAL} — PIC S9(10)V99
 * @param creditLimit     {@code ACCT-CREDIT-LIMIT} — PIC S9(10)V99
 * @param cashCreditLimit {@code ACCT-CASH-CREDIT-LIMIT} — PIC S9(10)V99
 * @param openDate        {@code ACCT-OPEN-DATE} — PIC X(10), ISO date or null
 * @param expirationDate  {@code ACCT-EXPIRAION-DATE} — PIC X(10), ISO date or null
 * @param reissueDate     {@code ACCT-REISSUE-DATE} — PIC X(10), ISO date or null
 * @param currCycCredit   {@code ACCT-CURR-CYC-CREDIT} — PIC S9(10)V99
 * @param currCycDebit    {@code ACCT-CURR-CYC-DEBIT} — PIC S9(10)V99
 * @param addrZip         {@code ACCT-ADDR-ZIP} — PIC X(10), trimmed or null
 * @param groupId         {@code ACCT-GROUP-ID} — PIC X(10), trimmed or null
 */
public record Account(
        long acctId,
        String activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        LocalDate openDate,
        LocalDate expirationDate,
        LocalDate reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String addrZip,
        String groupId) {
}
