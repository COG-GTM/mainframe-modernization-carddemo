package com.carddemo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.domain.Account;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void persistsAndReadsBackWithMonetaryScale() {
        Account a = new Account();
        a.setAcctId("00000000042");
        a.setAcctActiveStatus("Y");
        a.setAcctCurrBal(new BigDecimal("1234.56"));
        a.setAcctCreditLimit(new BigDecimal("5000.00"));
        a.setAcctGroupId("GROUP1");
        accountRepository.save(a);

        Account found = accountRepository.findById("00000000042").orElseThrow();
        assertThat(found.getAcctActiveStatus()).isEqualTo("Y");
        assertThat(found.getAcctCurrBal()).isEqualByComparingTo("1234.56");
        assertThat(found.getAcctCurrBal().scale()).isEqualTo(2);
        assertThat(found.getAcctGroupId()).isEqualTo("GROUP1");
    }
}
