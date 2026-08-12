package com.carddemo.online.transaction;

import com.carddemo.model.entity.Transaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * COBOL programs: COTRN00C / COTRN02C — browse access to the TRANSACT VSAM KSDS.
 *
 * <p>Replaces the {@code EXEC CICS STARTBR / READNEXT / READPREV / ENDBR} sequences on the
 * TRANSACT file (copybook CVTRA05Y): a forward browse from a record key, a backward browse from a
 * record key and the "last record in the file" read used for transaction id generation.
 */
@Repository
public interface TransactionBrowseRepository extends JpaRepository<Transaction, String> {

    /** STARTBR (GTEQ) + repeated READNEXT. */
    List<Transaction> findByTransactionIdGreaterThanEqualOrderByTransactionIdAsc(
            String transactionId, Limit limit);

    /** STARTBR + repeated READPREV. */
    List<Transaction> findByTransactionIdLessThanEqualOrderByTransactionIdDesc(
            String transactionId, Limit limit);

    /** STARTBR on HIGH-VALUES + READPREV: the highest transaction id on file. */
    Optional<Transaction> findFirstByOrderByTransactionIdDesc();
}
