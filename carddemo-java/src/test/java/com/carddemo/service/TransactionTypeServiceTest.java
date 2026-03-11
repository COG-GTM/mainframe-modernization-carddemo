package com.carddemo.service;

import com.carddemo.entity.TransactionType;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.TransactionTypeCategoryRepository;
import com.carddemo.repository.TransactionTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionTypeServiceTest {
    @Mock private TransactionTypeRepository typeRepository;
    @Mock private TransactionTypeCategoryRepository categoryRepository;
    @InjectMocks private TransactionTypeService service;

    @Test
    void listTypes() {
        TransactionType tt = new TransactionType();
        tt.setTranType("PR");
        when(typeRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(tt)));
        Page<TransactionType> result = service.listTransactionTypes(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getType_notFound() {
        when(typeRepository.findById("XX")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getTransactionType("XX"));
    }

    @Test
    void createType() {
        TransactionType tt = new TransactionType();
        tt.setTranType("CA");
        tt.setTranTypeDesc("Cash Advance");
        when(typeRepository.save(any())).thenReturn(tt);
        TransactionType result = service.createTransactionType(tt);
        assertEquals("CA", result.getTranType());
    }

    @Test
    void updateType() {
        TransactionType existing = new TransactionType();
        existing.setTranType("PR");
        existing.setTranTypeDesc("Purchase");
        when(typeRepository.findById("PR")).thenReturn(Optional.of(existing));
        when(typeRepository.save(any())).thenReturn(existing);
        TransactionType update = new TransactionType();
        update.setTranTypeDesc("Updated Purchase");
        TransactionType result = service.updateTransactionType("PR", update);
        assertNotNull(result);
    }
}
