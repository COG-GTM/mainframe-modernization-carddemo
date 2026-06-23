package com.carddemo.transaction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Transaction}.
 *
 * <p>Mirrors the COBOL/VSAM access patterns for TRANSACT:</p>
 * <ul>
 *   <li>Keyed READ by {@code TRAN-ID} — {@link #findById} (TRANSACT-FILE is an
 *       INDEXED file keyed on {@code FD-TRANS-ID}; see {@code CBTRN02C.cbl}).</li>
 *   <li>Sequential READ of the whole file — {@link #findAll} (see {@code CBTRN01C.cbl}).</li>
 *   <li>Lookup by card number — {@link #findByTranCardNum}, mirroring the XREF
 *       card-to-transaction access path used during posting.</li>
 * </ul>
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /** Transactions for the given card number, mirroring the TRAN-CARD-NUM / XREF lookup. */
    List<Transaction> findByTranCardNum(String tranCardNum);

    /** Transactions of the given type code, mirroring TRAN-TYPE-CD scans. */
    List<Transaction> findByTranTypeCd(String tranTypeCd);
}
