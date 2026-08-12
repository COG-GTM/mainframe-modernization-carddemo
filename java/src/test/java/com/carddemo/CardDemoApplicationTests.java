package com.carddemo;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.SecurityUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CardDemoApplicationTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SecurityUserRepository securityUserRepository;

    @Test
    void loadsSampleVsamDataOnStartup() {
        assertThat(accountRepository.count()).isEqualTo(50);
        assertThat(cardRepository.count()).isEqualTo(50);
        assertThat(customerRepository.count()).isEqualTo(50);
        assertThat(securityUserRepository.findById("ADMIN001")).isPresent();
    }
}
