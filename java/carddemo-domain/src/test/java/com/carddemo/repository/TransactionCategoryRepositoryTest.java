package com.carddemo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.domain.TransactionCategory;
import com.carddemo.domain.TransactionCategoryId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class TransactionCategoryRepositoryTest {

    @Autowired
    private TransactionCategoryRepository repository;

    @Test
    void persistsAndReadsBackByCompositeKey() {
        TransactionCategoryId id = new TransactionCategoryId("01", 1);
        TransactionCategory c = new TransactionCategory();
        c.setId(id);
        c.setTranCatTypeDesc("Regular Sales Draft");
        repository.save(c);

        TransactionCategory found = repository.findById(new TransactionCategoryId("01", 1)).orElseThrow();
        assertThat(found.getTranCatTypeDesc()).isEqualTo("Regular Sales Draft");
        assertThat(found.getId().getTranTypeCd()).isEqualTo("01");
        assertThat(found.getId().getTranCatCd()).isEqualTo(1);
    }
}
