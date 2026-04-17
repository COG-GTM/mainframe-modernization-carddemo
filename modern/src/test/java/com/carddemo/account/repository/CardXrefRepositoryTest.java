package com.carddemo.account.repository;

import com.carddemo.account.entity.CardXrefEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for card cross-reference lookups.
 * Validates CXACAIX (AIX PATH) access pattern migration.
 */
@DataJpaTest
class CardXrefRepositoryTest {

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Test
    void findByAccountId_returnsLinkedCards() {
        // Account 80001000001 has 2 cards linked in seed data
        List<CardXrefEntity> xrefs = cardXrefRepository.findByAccountId(80001000001L);

        assertThat(xrefs).hasSize(2);
        assertThat(xrefs).extracting(CardXrefEntity::getCardNum)
                .containsExactlyInAnyOrder("4111111111111111", "4111111111112222");
        assertThat(xrefs).allMatch(x -> x.getCustId().equals(100000001L));
    }

    @Test
    void findByAccountId_singleCard() {
        List<CardXrefEntity> xrefs = cardXrefRepository.findByAccountId(80001000002L);

        assertThat(xrefs).hasSize(1);
        assertThat(xrefs.getFirst().getCardNum()).isEqualTo("4222222222221111");
    }

    @Test
    void findByAccountId_noCards_returnsEmptyList() {
        // Non-existent account
        List<CardXrefEntity> xrefs = cardXrefRepository.findByAccountId(99999999999L);

        assertThat(xrefs).isEmpty();
    }
}
