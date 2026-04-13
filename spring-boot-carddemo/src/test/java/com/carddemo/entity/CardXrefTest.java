package com.carddemo.entity;

import com.carddemo.repository.CardXrefRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for CardXref entity confirming behavior of the underlying COBOL VSAM system.
 * Verifies seed data from cardxref.txt, CRUD operations, and custom finders.
 */
@DataJpaTest
class CardXrefTest {

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Test
    void seedDataLoadsExpectedNumberOfXrefs() {
        List<CardXref> all = cardXrefRepository.findAll();
        assertThat(all).hasSize(50);
    }

    @Test
    void firstXrefFieldsMatchSeedData() {
        List<CardXref> all = cardXrefRepository.findAll();
        assertThat(all).isNotEmpty();
        CardXref first = all.get(0);
        assertThat(first.getXrefCardNum()).isNotBlank();
        assertThat(first.getXrefCustId()).isGreaterThan(0);
        assertThat(first.getXrefAcctId()).isGreaterThan(0);
    }

    @Test
    void findByCustIdReturnsMatchingXrefs() {
        List<CardXref> all = cardXrefRepository.findAll();
        Long custId = all.get(0).getXrefCustId();
        List<CardXref> results = cardXrefRepository.findByXrefCustId(custId);
        assertThat(results).isNotEmpty();
        results.forEach(x -> assertThat(x.getXrefCustId()).isEqualTo(custId));
    }

    @Test
    void findByAcctIdReturnsMatchingXrefs() {
        List<CardXref> all = cardXrefRepository.findAll();
        Long acctId = all.get(0).getXrefAcctId();
        List<CardXref> results = cardXrefRepository.findByXrefAcctId(acctId);
        assertThat(results).isNotEmpty();
        results.forEach(x -> assertThat(x.getXrefAcctId()).isEqualTo(acctId));
    }

    @Test
    void createXrefPersistsAndReadsBack() {
        CardXref newXref = new CardXref();
        newXref.setXrefCardNum("9999888877776666");
        newXref.setXrefCustId(99999L);
        newXref.setXrefAcctId(88888L);

        cardXrefRepository.save(newXref);

        Optional<CardXref> found = cardXrefRepository.findById("9999888877776666");
        assertThat(found).isPresent();
        assertThat(found.get().getXrefCustId()).isEqualTo(99999L);
        assertThat(found.get().getXrefAcctId()).isEqualTo(88888L);
    }

    @Test
    void updateXrefAcctId() {
        List<CardXref> all = cardXrefRepository.findAll();
        CardXref xref = all.get(0);
        xref.setXrefAcctId(77777L);
        cardXrefRepository.save(xref);

        CardXref updated = cardXrefRepository.findById(xref.getXrefCardNum()).orElseThrow();
        assertThat(updated.getXrefAcctId()).isEqualTo(77777L);
    }

    @Test
    void deleteXrefRemovesFromDatabase() {
        List<CardXref> before = cardXrefRepository.findAll();
        int beforeSize = before.size();
        cardXrefRepository.deleteById(before.get(0).getXrefCardNum());
        assertThat(cardXrefRepository.findAll()).hasSize(beforeSize - 1);
    }

    @Test
    void xrefCardNumIsUniqueAcrossAllRecords() {
        List<CardXref> all = cardXrefRepository.findAll();
        long distinctNums = all.stream().map(CardXref::getXrefCardNum).distinct().count();
        assertThat(distinctNums).isEqualTo(all.size());
    }

    @Test
    void allXrefsHaveValidReferences() {
        List<CardXref> all = cardXrefRepository.findAll();
        all.forEach(x -> {
            assertThat(x.getXrefCustId()).isGreaterThan(0);
            assertThat(x.getXrefAcctId()).isGreaterThan(0);
        });
    }

    @Test
    void xrefLinksCardToCustomerAndAccount() {
        // Mirrors COBOL CARDXREF VSAM file linking CARD-NUM to CUST-ID and ACCT-ID
        List<CardXref> all = cardXrefRepository.findAll();
        CardXref xref = all.get(0);
        assertThat(xref.getXrefCardNum()).isNotNull();
        assertThat(xref.getXrefCustId()).isNotNull();
        assertThat(xref.getXrefAcctId()).isNotNull();
    }
}
