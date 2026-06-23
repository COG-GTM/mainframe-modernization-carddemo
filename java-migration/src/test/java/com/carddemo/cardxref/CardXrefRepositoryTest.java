package com.carddemo.cardxref;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Verifies the CardXref entity mapping, Flyway seed data, and repository finders
 * against an H2 (PostgreSQL mode) database loaded by the real Flyway migrations.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CardXrefRepositoryTest {

    @Autowired
    private CardXrefRepository repository;

    @Test
    void seedDataLoadsExpectedRecordCount() {
        assertThat(repository.count()).isEqualTo(50);
    }

    @Test
    void firstSeedRecordFieldsMatchParsedValues() {
        CardXref xref = repository.findById("0500024453765740").orElseThrow();
        assertThat(xref.getXrefCustId()).isEqualTo(50L);
        assertThat(xref.getXrefAcctId()).isEqualTo(50L);
    }

    @Test
    void findByIdReturnsEmptyForUnknownKey() {
        assertThat(repository.findById("0000000000000000")).isEmpty();
    }

    @Test
    void findByXrefAcctIdReturnsMatchingRows() {
        List<CardXref> rows = repository.findByXrefAcctId(27L);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getXrefCardNum()).isEqualTo("0683586198171516");
    }

    @Test
    void findByXrefAcctIdReturnsEmptyForUnknownAccount() {
        assertThat(repository.findByXrefAcctId(999999999L)).isEmpty();
    }

    @Test
    void findByXrefCustIdReturnsMatchingRows() {
        List<CardXref> rows = repository.findByXrefCustId(2L);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getXrefCardNum()).isEqualTo("0923877193247330");
    }

    @Test
    void findByXrefCustIdReturnsEmptyForUnknownCustomer() {
        assertThat(repository.findByXrefCustId(999999999L)).isEmpty();
    }

    @Test
    void createPersistsNewCardXref() {
        CardXref xref = newCardXref("8000000000000001", 8001L, 80001L);
        repository.save(xref);

        Optional<CardXref> reloaded = repository.findById("8000000000000001");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getXrefCustId()).isEqualTo(8001L);
        assertThat(reloaded.get().getXrefAcctId()).isEqualTo(80001L);
    }

    @Test
    void updateModifiesExistingCardXref() {
        CardXref xref = repository.findById("0927987108636232").orElseThrow();
        xref.setXrefAcctId(123456L);
        repository.saveAndFlush(xref);

        assertThat(repository.findById("0927987108636232").orElseThrow().getXrefAcctId())
                .isEqualTo(123456L);
    }

    @Test
    void deleteRemovesCardXref() {
        repository.deleteById("0982496213629795");
        assertThat(repository.findById("0982496213629795")).isEmpty();
        assertThat(repository.count()).isEqualTo(49);
    }

    @Test
    void findAllMirrorsSequentialReadOfWholeFile() {
        assertThat(repository.findAll()).hasSize(50);
    }

    private static CardXref newCardXref(String cardNum, Long custId, Long acctId) {
        CardXref xref = new CardXref();
        xref.setXrefCardNum(cardNum);
        xref.setXrefCustId(custId);
        xref.setXrefAcctId(acctId);
        return xref;
    }
}
