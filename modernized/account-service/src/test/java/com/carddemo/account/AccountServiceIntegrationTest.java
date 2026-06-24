package com.carddemo.account;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.account.batch.AccountReportService;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.account.web.AccountResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountServiceIntegrationTest {

    private static final int SEED_RECORD_COUNT = 50;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountReportService accountReportService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void seedDataIsLoadedOnStartup() {
        assertThat(accountRepository.count()).isEqualTo(SEED_RECORD_COUNT);
    }

    @Test
    void reportReadsEverySeededRecord() {
        assertThat(accountReportService.runReport()).isEqualTo(SEED_RECORD_COUNT);
    }

    @Test
    void listEndpointReturnsAllAccountsOrderedByKey() {
        ResponseEntity<List<AccountResponse>> response = restTemplate.exchange(
                "/api/accounts", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<AccountResponse> accounts = response.getBody();
        assertThat(accounts).hasSize(SEED_RECORD_COUNT);
        assertThat(accounts.get(0).acctId()).isEqualTo("00000000001");
    }

    @Test
    void getEndpointReturnsSingleAccount() {
        ResponseEntity<AccountResponse> response =
                restTemplate.getForEntity("/api/accounts/00000000001", AccountResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().acctId()).isEqualTo("00000000001");
        assertThat(response.getBody().currentBalance()).isEqualByComparingTo("194.00");
    }

    @Test
    void getEndpointReturns404ForUnknownAccount() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/accounts/99999999999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
