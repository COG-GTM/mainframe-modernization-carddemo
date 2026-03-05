package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for transaction type reference data.
 * Derived from transaction type codes used across the CardDemo application.
 */
@Entity
@Table(name = "transaction_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionType {

    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    @Size(max = 2)
    @NotNull
    private String typeCode;

    @Column(name = "type_description", length = 100)
    @Size(max = 100)
    private String typeDescription;
}
