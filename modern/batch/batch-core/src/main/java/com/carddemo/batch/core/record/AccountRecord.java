package com.carddemo.batch.core.record;

import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;
import java.math.BigDecimal;

/**
 * {@code ACCOUNT-RECORD} from {@code app/cpy/CVACT01Y.cpy} (RECLN 300), keyed by ACCT-ID.
 *
 * <pre>
 * offset len  field
 *      0  11  ACCT-ID                 9(11)
 *     11   1  ACCT-ACTIVE-STATUS      X(01)
 *     12  12  ACCT-CURR-BAL           S9(10)V99
 *     24  12  ACCT-CREDIT-LIMIT       S9(10)V99
 *     36  12  ACCT-CASH-CREDIT-LIMIT  S9(10)V99
 *     48  10  ACCT-OPEN-DATE          X(10)
 *     58  10  ACCT-EXPIRAION-DATE     X(10)
 *     68  10  ACCT-REISSUE-DATE       X(10)
 *     78  12  ACCT-CURR-CYC-CREDIT    S9(10)V99
 *     90  12  ACCT-CURR-CYC-DEBIT     S9(10)V99
 *    102  10  ACCT-ADDR-ZIP           X(10)
 *    112  10  ACCT-GROUP-ID           X(10)
 *    122 178  FILLER                  X(178)
 * </pre>
 */
public record AccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String addrZip,
        String groupId,
        String filler) implements FixedRecord {

    public static final int LENGTH = 300;

    public AccountRecord {
        acctId = CobolDecimal.toUnsignedField(acctId, 11);
        activeStatus = FixedWidth.fit(activeStatus, 1);
        currBal = CobolDecimal.toField(currBal, 10, 2);
        creditLimit = CobolDecimal.toField(creditLimit, 10, 2);
        cashCreditLimit = CobolDecimal.toField(cashCreditLimit, 10, 2);
        openDate = FixedWidth.fit(openDate, 10);
        expirationDate = FixedWidth.fit(expirationDate, 10);
        reissueDate = FixedWidth.fit(reissueDate, 10);
        currCycCredit = CobolDecimal.toField(currCycCredit, 10, 2);
        currCycDebit = CobolDecimal.toField(currCycDebit, 10, 2);
        addrZip = FixedWidth.fit(addrZip, 10);
        groupId = FixedWidth.fit(groupId, 10);
        filler = FixedWidth.fit(filler, 178);
    }

    public AccountRecord withCurrBal(BigDecimal value) {
        return new AccountRecord(acctId, activeStatus, value, creditLimit, cashCreditLimit, openDate,
                expirationDate, reissueDate, currCycCredit, currCycDebit, addrZip, groupId, filler);
    }

    public AccountRecord withCurrCycCredit(BigDecimal value) {
        return new AccountRecord(acctId, activeStatus, currBal, creditLimit, cashCreditLimit, openDate,
                expirationDate, reissueDate, value, currCycDebit, addrZip, groupId, filler);
    }

    public AccountRecord withCurrCycDebit(BigDecimal value) {
        return new AccountRecord(acctId, activeStatus, currBal, creditLimit, cashCreditLimit, openDate,
                expirationDate, reissueDate, currCycCredit, value, addrZip, groupId, filler);
    }

    @Override
    public byte[] toBytes() {
        return new RecordWriter(LENGTH)
                .unsigned(acctId, 11)
                .alpha(activeStatus, 1)
                .signed(currBal, 10, 2)
                .signed(creditLimit, 10, 2)
                .signed(cashCreditLimit, 10, 2)
                .alpha(openDate, 10)
                .alpha(expirationDate, 10)
                .alpha(reissueDate, 10)
                .signed(currCycCredit, 10, 2)
                .signed(currCycDebit, 10, 2)
                .alpha(addrZip, 10)
                .alpha(groupId, 10)
                .alpha(filler, 178)
                .toBytes();
    }

    public static AccountRecord fromBytes(byte[] bytes) {
        RecordReader r = new RecordReader(bytes, LENGTH);
        return new AccountRecord(
                r.unsigned(11), r.alpha(1), r.signed(10, 2), r.signed(10, 2), r.signed(10, 2),
                r.alpha(10), r.alpha(10), r.alpha(10), r.signed(10, 2), r.signed(10, 2),
                r.alpha(10), r.alpha(10), r.alpha(178));
    }
}
