package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.TransactionTypeDto;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.model.TransactionType;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for transaction type CRUD operations.
 * Migrated from COBOL programs COTRTLIC (list) and COTRTUPC (update).
 */
@Service
public class TransactionTypeService {

    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionTypeService(TransactionTypeRepository transactionTypeRepository) {
        this.transactionTypeRepository = transactionTypeRepository;
    }

    /**
     * List all transaction types.
     * Mirrors COTRTLIC which browses the transaction type file.
     */
    public List<TransactionTypeDto> listTransactionTypes() {
        return transactionTypeRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Update a transaction type description.
     * Mirrors COTRTUPC which updates a transaction type record.
     */
    @Transactional
    public TransactionTypeDto updateTransactionType(String typeCode, TransactionTypeDto dto) {
        TransactionType type = transactionTypeRepository.findById(typeCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction type NOT found: " + typeCode));
        type.setDescription(dto.getDescription());
        TransactionType saved = transactionTypeRepository.save(type);
        return toDto(saved);
    }

    private TransactionTypeDto toDto(TransactionType entity) {
        return new TransactionTypeDto(entity.getTypeCode(), entity.getDescription());
    }
}
