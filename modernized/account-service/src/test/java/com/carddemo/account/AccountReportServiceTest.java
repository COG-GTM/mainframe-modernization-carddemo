package com.carddemo.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.carddemo.account.batch.AccountReportService;
import com.carddemo.account.exception.AccountFileException;
import com.carddemo.account.model.Account;
import com.carddemo.account.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

@ExtendWith(MockitoExtension.class)
class AccountReportServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountReportService accountReportService;

    private static Account account(String id) {
        return new Account(id, "Y", new BigDecimal("194.00"), new BigDecimal("2020.00"),
                new BigDecimal("1020.00"), "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO, "A000000000", "");
    }

    @Test
    void readsAndCountsEveryRecord() {
        when(accountRepository.findAllByOrderByAcctIdAsc())
                .thenReturn(List.of(account("00000000001"), account("00000000002"), account("00000000003")));

        assertThat(accountReportService.runReport()).isEqualTo(3);
    }

    @Test
    void emptyFileReadsZeroRecords() {
        when(accountRepository.findAllByOrderByAcctIdAsc()).thenReturn(List.of());

        assertThat(accountReportService.runReport()).isZero();
    }

    @Test
    void abendsWhenOpenFails() {
        when(accountRepository.findAllByOrderByAcctIdAsc())
                .thenThrow(new DataAccessResourceFailureException("file unavailable"));

        assertThatThrownBy(() -> accountReportService.runReport())
                .isInstanceOf(AccountFileException.class);
    }
}
