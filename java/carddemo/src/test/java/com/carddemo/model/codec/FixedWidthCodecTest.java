package com.carddemo.model.codec;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.Account;
import com.carddemo.model.Card;
import com.carddemo.model.SecurityUser;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FixedWidthCodecTest {

    private static final String ACCOUNT_RECORD =
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-20"
                    + "00000000000{00000000000{A000000000" + " ".repeat(188);

    @Test
    void decodesAccountRecord() {
        Account account = FixedWidthCodec.decode(ACCOUNT_RECORD, Account.class);

        assertThat(account.getId()).isEqualTo(1L);
        assertThat(account.getActiveStatus()).isEqualTo("Y");
        assertThat(account.getCurrBal()).isEqualByComparingTo(new BigDecimal("194.00"));
        assertThat(account.getCreditLimit()).isEqualByComparingTo(new BigDecimal("2020.00"));
        assertThat(account.getOpenDate()).isEqualTo("2014-11-20");
        assertThat(account.getAddrZip()).isEqualTo("A000000000");
    }

    @Test
    void encodeIsTheInverseOfDecode() {
        Account account = FixedWidthCodec.decode(ACCOUNT_RECORD, Account.class);

        assertThat(FixedWidthCodec.encode(account)).isEqualTo(ACCOUNT_RECORD);
    }

    @Test
    void writesFixedLengthRecordsForNewInstances() {
        Card card = new Card();
        card.setNum("4111111111111111");
        card.setAcctId(1L);
        card.setCvvCd(123);
        card.setEmbossedName("JOHN DOE");
        card.setExpiraionDate("2025-05-20");
        card.setActiveStatus("Y");

        String record = FixedWidthCodec.encode(card);

        assertThat(record).hasSize(150);
        assertThat(record).startsWith("411111111111111100000000001123JOHN DOE");
        assertThat(FixedWidthCodec.decode(record, Card.class).getEmbossedName()).isEqualTo("JOHN DOE");
    }

    @Test
    void exposesTheCopybookRecordLength() {
        assertThat(FixedWidthCodec.recordLength(Account.class)).isEqualTo(300);
        assertThat(FixedWidthCodec.recordLength(Card.class)).isEqualTo(150);
        assertThat(FixedWidthCodec.recordLength(SecurityUser.class)).isEqualTo(80);
    }
}
