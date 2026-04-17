package com.carddemo.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request DTO for creating a new transaction.
 *
 * COBOL Traceability: Maps the input fields from COTRN02C (Txn CT02)
 * screen map COTRN2A. Replaces the BMS RECEIVE MAP processing.
 * <p>
 * Either accountId or cardNumber must be provided (validated in service).
 * The COBOL program validates via VALIDATE-INPUT-KEY-FIELDS paragraph.
 */
public record CreateTransactionRequest(
        String accountId,
        String cardNumber,
        @NotBlank(message = "Type code is required")
        @Size(max = 2, message = "Type code must be at most 2 characters")
        String typeCode,
        @NotNull(message = "Category code is required")
        Integer categoryCode,
        @Size(max = 10, message = "Source must be at most 10 characters")
        String source,
        @NotBlank(message = "Description is required")
        @Size(max = 100, message = "Description must be at most 100 characters")
        String description,
        @NotNull(message = "Amount is required")
        BigDecimal amount,
        Long merchantId,
        @Size(max = 50, message = "Merchant name must be at most 50 characters")
        String merchantName,
        @Size(max = 50, message = "Merchant city must be at most 50 characters")
        String merchantCity,
        @Size(max = 10, message = "Merchant zip must be at most 10 characters")
        String merchantZip,
        String originDate,
        String processedDate
) {
}
