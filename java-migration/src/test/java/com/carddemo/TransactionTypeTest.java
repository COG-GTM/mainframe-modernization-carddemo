package com.carddemo;

import com.carddemo.entity.TransactionType;
import com.carddemo.repository.TransactionTypeRepository;
import com.carddemo.service.TransactionTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the TransactionType entity, repository, and service.
 * Verifies seed data from Flyway migration V1.1 and CRUD operations.
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class TransactionTypeTest {

    @Autowired
    private TransactionTypeRepository repository;

    @Autowired
    private TransactionTypeService service;

    @Test
    @DisplayName("Seed data should contain exactly 7 records")
    void seedDataCount() {
        List<TransactionType> all = repository.findAll();
        assertThat(all).hasSize(7);
    }

    @Test
    @DisplayName("First record should be type_code='01', description='Purchase'")
    void firstRecordValues() {
        Optional<TransactionType> result = repository.findById("01");
        assertThat(result).isPresent();
        assertThat(result.get().getTypeCode()).isEqualTo("01");
        assertThat(result.get().getDescription()).isEqualTo("Purchase");
    }

    @Test
    @DisplayName("All 7 type codes should be present in seed data")
    void allTypeCodesPresent() {
        assertThat(repository.findById("01")).isPresent();
        assertThat(repository.findById("02")).isPresent();
        assertThat(repository.findById("03")).isPresent();
        assertThat(repository.findById("04")).isPresent();
        assertThat(repository.findById("05")).isPresent();
        assertThat(repository.findById("06")).isPresent();
        assertThat(repository.findById("07")).isPresent();
    }

    @Test
    @DisplayName("Should create a new TransactionType via repository")
    void createTransactionType() {
        TransactionType newType = TransactionType.builder()
                .typeCode("08")
                .description("Balance Inquiry")
                .build();
        repository.save(newType);

        Optional<TransactionType> found = repository.findById("08");
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Balance Inquiry");
    }

    @Test
    @DisplayName("Should update an existing TransactionType")
    void updateTransactionType() {
        Optional<TransactionType> existing = repository.findById("01");
        assertThat(existing).isPresent();

        existing.get().setDescription("Purchase (updated)");
        repository.save(existing.get());

        Optional<TransactionType> updated = repository.findById("01");
        assertThat(updated).isPresent();
        assertThat(updated.get().getDescription()).isEqualTo("Purchase (updated)");
    }

    @Test
    @DisplayName("Should delete a TransactionType by type code")
    void deleteTransactionType() {
        repository.deleteById("07");
        Optional<TransactionType> deleted = repository.findById("07");
        assertThat(deleted).isNotPresent();
        assertThat(repository.findAll()).hasSize(6);
    }

    @Test
    @DisplayName("Custom finder: findByDescriptionContaining should return matching records")
    void findByDescriptionContaining() {
        List<TransactionType> results = repository.findByDescriptionContaining("ment");
        assertThat(results).isNotEmpty();

        List<String> descriptions = results.stream()
                .map(TransactionType::getDescription)
                .toList();
        assertThat(descriptions).contains("Payment", "Adjustment");
    }

    @Test
    @DisplayName("Service layer findAll should return all seeded records")
    void serviceFindAll() {
        List<TransactionType> all = service.findAll();
        assertThat(all).hasSize(7);
    }

    @Test
    @DisplayName("Service layer findByTypeCode should return correct record")
    void serviceFindByTypeCode() {
        Optional<TransactionType> result = service.findByTypeCode("05");
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Refund");
    }

    @Test
    @DisplayName("Service layer save should persist a new record")
    void serviceSave() {
        TransactionType newType = TransactionType.builder()
                .typeCode("09")
                .description("Cash Advance")
                .build();
        service.save(newType);

        Optional<TransactionType> found = service.findByTypeCode("09");
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Cash Advance");
    }
}
