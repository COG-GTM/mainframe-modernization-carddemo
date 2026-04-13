package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapped from COBOL copybook CVTRA03Y.cpy (TRAN-TYPE-RECORD, RECLN 60).
 * Reference table for transaction type codes and descriptions.
 */
@Entity
@Table(name = "transaction_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionType {

    /** TRAN-TYPE — PIC X(02). Primary key, 2-character transaction type code. */
    @Id
    @Column(name = "tran_type", length = 2)
    private String tranType;

    /** TRAN-TYPE-DESC — PIC X(50). Description of the transaction type. */
    @Column(name = "tran_type_desc", length = 50)
    private String tranTypeDesc;
}
