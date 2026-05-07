package com.carddemo.service;

import com.carddemo.entity.TransactionCategory;
import com.carddemo.entity.TransactionCategoryId;
import com.carddemo.repository.TransactionCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TransactionCategoryService}.
 */
@ExtendWith(MockitoExtension.class)
class TransactionCategoryServiceTest {

    @Mock
    private TransactionCategoryRepository repository;

    @InjectMocks
    private TransactionCategoryService service;

    private TransactionCategory sampleCategory;
    private TransactionCategoryId sampleId;

    @BeforeEach
    void setUp() {
        sampleId = new TransactionCategoryId("01", 1);
        sampleCategory = new TransactionCategory(sampleId, "Regular Sales Draft");
    }

    @Test
    @DisplayName("findAll returns all categories from repository")
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(sampleCategory));
        List<TransactionCategory> results = service.findAll();
        assertEquals(1, results.size());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("findById with composite key returns category")
    void testFindById() {
        when(repository.findById(sampleId)).thenReturn(Optional.of(sampleCategory));
        Optional<TransactionCategory> result = service.findById(sampleId);
        assertTrue(result.isPresent());
        assertEquals("Regular Sales Draft", result.get().getDescription());
    }

    @Test
    @DisplayName("findById with typeCode and categoryCode delegates correctly")
    void testFindByTypeAndCode() {
        when(repository.findById(any(TransactionCategoryId.class)))
                .thenReturn(Optional.of(sampleCategory));
        Optional<TransactionCategory> result = service.findById("01", 1);
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("findByTypeCode delegates to repository")
    void testFindByTypeCode() {
        when(repository.findByIdTypeCode("01")).thenReturn(List.of(sampleCategory));
        List<TransactionCategory> results = service.findByTypeCode("01");
        assertEquals(1, results.size());
        verify(repository).findByIdTypeCode("01");
    }

    @Test
    @DisplayName("searchByDescription delegates to repository")
    void testSearchByDescription() {
        when(repository.findByDescriptionContainingIgnoreCase("Sales"))
                .thenReturn(List.of(sampleCategory));
        List<TransactionCategory> results = service.searchByDescription("Sales");
        assertEquals(1, results.size());
        verify(repository).findByDescriptionContainingIgnoreCase("Sales");
    }

    @Test
    @DisplayName("save delegates to repository and returns saved entity")
    void testSave() {
        when(repository.save(sampleCategory)).thenReturn(sampleCategory);
        TransactionCategory result = service.save(sampleCategory);
        assertEquals(sampleCategory, result);
        verify(repository).save(sampleCategory);
    }

    @Test
    @DisplayName("deleteById delegates to repository")
    void testDeleteById() {
        doNothing().when(repository).deleteById(sampleId);
        service.deleteById(sampleId);
        verify(repository).deleteById(sampleId);
    }

    @Test
    @DisplayName("count returns repository count")
    void testCount() {
        when(repository.count()).thenReturn(18L);
        assertEquals(18L, service.count());
        verify(repository).count();
    }
}
