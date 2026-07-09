package com.carddemo.batch.account;

import com.carddemo.domain.Account;
import com.carddemo.domain.Card;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import java.math.BigDecimal;

/**
 * Renders one entity as a fixed-width textual record image, reproducing the {@code DISPLAY} of a
 * record area in the read/print COBOL programs ({@code CBACT01C}, {@code CBACT02C},
 * {@code CBACT03C}, {@code CBCUS01C}).
 *
 * <p>Each field is laid out left-to-right at its copybook {@code PIC} width so the emitted line
 * mirrors the source record layout. Text/id fields are left-justified and space-padded; monetary
 * fields ({@code S9(n)V99}) are shown as a fixed-scale decimal, right-justified. This is a
 * human-readable image, not the raw EBCDIC/zoned-decimal bytes of the original VSAM record.</p>
 */
final class RecordFormatter {

    private RecordFormatter() {
    }

    private static String text(String value, int width) {
        String s = value == null ? "" : value;
        if (s.length() > width) {
            return s.substring(0, width);
        }
        return String.format("%-" + width + "s", s);
    }

    private static String money(BigDecimal value, int width) {
        String s = value == null ? "" : value.setScale(2, java.math.RoundingMode.UNNECESSARY).toPlainString();
        return String.format("%" + width + "s", s);
    }

    /** ACCOUNT-RECORD (copybook CVACT01Y). */
    static String account(Account a) {
        return text(a.getAcctId(), 11)
                + text(a.getAcctActiveStatus(), 1)
                + money(a.getAcctCurrBal(), 14)
                + money(a.getAcctCreditLimit(), 14)
                + money(a.getAcctCashCreditLimit(), 14)
                + text(a.getAcctOpenDate(), 10)
                + text(a.getAcctExpirationDate(), 10)
                + text(a.getAcctReissueDate(), 10)
                + money(a.getAcctCurrCycCredit(), 14)
                + money(a.getAcctCurrCycDebit(), 14)
                + text(a.getAcctAddrZip(), 10)
                + text(a.getAcctGroupId(), 10);
    }

    /** CARD-RECORD (copybook CVACT02Y). */
    static String card(Card c) {
        return text(c.getCardNum(), 16)
                + text(c.getCardAcctId(), 11)
                + text(c.getCardCvvCd(), 3)
                + text(c.getCardEmbossedName(), 50)
                + text(c.getCardExpirationDate(), 10)
                + text(c.getCardActiveStatus(), 1);
    }

    /** CARD-XREF-RECORD (copybook CVACT03Y). */
    static String cardXref(CardXref x) {
        return text(x.getXrefCardNum(), 16)
                + text(x.getXrefCustId(), 9)
                + text(x.getXrefAcctId(), 11);
    }

    /** CUSTOMER-RECORD (copybook CVCUS01Y). */
    static String customer(Customer c) {
        return text(c.getCustId(), 9)
                + text(c.getCustFirstName(), 25)
                + text(c.getCustMiddleName(), 25)
                + text(c.getCustLastName(), 25)
                + text(c.getCustAddrLine1(), 50)
                + text(c.getCustAddrLine2(), 50)
                + text(c.getCustAddrLine3(), 50)
                + text(c.getCustAddrStateCd(), 2)
                + text(c.getCustAddrCountryCd(), 3)
                + text(c.getCustAddrZip(), 10)
                + text(c.getCustPhoneNum1(), 15)
                + text(c.getCustPhoneNum2(), 15)
                + text(c.getCustSsn(), 9)
                + text(c.getCustGovtIssuedId(), 20)
                + text(c.getCustDobYyyyMmDd(), 10)
                + text(c.getCustEftAccountId(), 10)
                + text(c.getCustPriCardHolderInd(), 1)
                + String.format("%03d", c.getCustFicoCreditScore() == null ? 0 : c.getCustFicoCreditScore());
    }
}
