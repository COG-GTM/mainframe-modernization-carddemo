package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a Transaction Type record.
 *
 * <p>Migrated from COBOL copybook CVTRA03Y.cpy (RECLN = 60):
 * <pre>
 *   01  TRAN-TYPE-RECORD.
 *       05  TRAN-TYPE        PIC X(02).   -> {@link #typeCode}  (Primary Key)
 *       05  TRAN-TYPE-DESC   PIC X(50).   -> {@link #description}
 *       05  FILLER           PIC X(08).   (not mapped)
 * </pre>
 *
 * @see <a href="app/cpy/CVTRA03Y.cpy">COBOL Copybook</a>
 */
@Entity
@Table(name = "transaction_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionType {

    /**
     * Transaction type code (COBOL: TRAN-TYPE PIC X(02)).
     */
    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    /**
     * Transaction type description (COBOL: TRAN-TYPE-DESC PIC X(50)).
     */
    @Column(name = "description", length = 50, nullable = false)
    private String description;
}
