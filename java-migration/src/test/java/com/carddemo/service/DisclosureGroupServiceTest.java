package com.carddemo.service;

import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.DisclosureGroupId;
import com.carddemo.repository.DisclosureGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DisclosureGroupService}.
 */
@ExtendWith(MockitoExtension.class)
class DisclosureGroupServiceTest {

    @Mock
    private DisclosureGroupRepository repository;

    @InjectMocks
    private DisclosureGroupService service;

    private DisclosureGroup sampleEntity;
    private DisclosureGroupId sampleId;

    @BeforeEach
    void setUp() {
        sampleId = new DisclosureGroupId("A000000000", "01", 1);
        sampleEntity = new DisclosureGroup(sampleId, new BigDecimal("15.00"));
    }

    @Test
    @DisplayName("findById delegates to repository and returns result")
    void findById() {
        when(repository.findById(sampleId)).thenReturn(Optional.of(sampleEntity));

        Optional<DisclosureGroup> result = service.findById(sampleId);

        assertTrue(result.isPresent());
        assertEquals(sampleEntity, result.get());
        verify(repository).findById(sampleId);
    }

    @Test
    @DisplayName("findByAccountGroupId delegates to repository")
    void findByAccountGroupId() {
        when(repository.findByIdAccountGroupId("A000000000"))
                .thenReturn(List.of(sampleEntity));

        List<DisclosureGroup> results = service.findByAccountGroupId("A000000000");

        assertEquals(1, results.size());
        verify(repository).findByIdAccountGroupId("A000000000");
    }

    @Test
    @DisplayName("findByTransactionTypeCode delegates to repository")
    void findByTransactionTypeCode() {
        when(repository.findByIdTransactionTypeCode("01"))
                .thenReturn(List.of(sampleEntity));

        List<DisclosureGroup> results = service.findByTransactionTypeCode("01");

        assertEquals(1, results.size());
        verify(repository).findByIdTransactionTypeCode("01");
    }

    @Test
    @DisplayName("save delegates to repository")
    void save() {
        when(repository.save(sampleEntity)).thenReturn(sampleEntity);

        DisclosureGroup result = service.save(sampleEntity);

        assertEquals(sampleEntity, result);
        verify(repository).save(sampleEntity);
    }

    @Test
    @DisplayName("deleteById delegates to repository")
    void deleteById() {
        doNothing().when(repository).deleteById(sampleId);

        service.deleteById(sampleId);

        verify(repository).deleteById(sampleId);
    }

    @Test
    @DisplayName("count delegates to repository")
    void count() {
        when(repository.count()).thenReturn(51L);

        long count = service.count();

        assertEquals(51L, count);
        verify(repository).count();
    }
}
