package com.cardemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapped from COBOL copybook CVTRA03Y.cpy (TRAN-TYPE-RECORD).
 * Total COBOL record length: 60 bytes.
 * Seed data file: trantype.txt.
 * Reference data: maps transaction type codes to descriptions.
 */
@Entity
@Table(name = "transaction_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionType {

    /** TRAN-TYPE — PIC X(02), bytes [0:2]. Transaction type code (primary key). */
    @Id
    @Column(name = "tran_type", length = 2)
    private String tranType;

    /** TRAN-TYPE-DESC — PIC X(50), bytes [2:52]. Transaction type description. */
    @Column(name = "tran_type_desc", length = 50)
    private String tranTypeDesc;

    // FILLER — PIC X(08), bytes [52:60]. Padding — not mapped.
}
