package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL copybook: CVTRA03Y (TRAN-TYPE-RECORD), file TRANTYPE, RECLN 60.
 */
@Entity
@Table(name = "tran_type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionType {

    /** TRAN-TYPE PIC X(02). */
    @Id
    @Column(name = "tran_type", length = 2, nullable = false)
    private String typeCode;

    /** TRAN-TYPE-DESC PIC X(50). */
    @Column(name = "tran_type_desc", length = 50)
    private String description;
}
