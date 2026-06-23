package com.carddemo.card;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Card}.
 *
 * <p>Mirrors the COBOL/VSAM access patterns for CARDDAT:</p>
 * <ul>
 *   <li>Keyed READ by {@code CARD-NUM} — {@link #findById} (see {@code COCRDSLC.cbl}
 *       keyed view and {@code COCRDUPC.cbl} read-for-update).</li>
 *   <li>Sequential READ of the whole file ordered by key — {@link #findAll} and
 *       {@link #findAllByOrderByCardNumAsc} (see {@code CBACT02C.cbl}).</li>
 *   <li>Lookup by owning account id ({@code CARD-ACCT-ID}) — {@link #findByCardAcctId}
 *       (cards belong to accounts; see the account filter in {@code COCRDSLC.cbl}).</li>
 *   <li>Filter by active-status flag — {@link #findByCardActiveStatus}.</li>
 * </ul>
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    /** All cards for the given owning account id, mirroring CARD-ACCT-ID lookups. */
    List<Card> findByCardAcctId(Long cardAcctId);

    /** Cards with the given active-status flag (Y/N), mirroring CARD-ACTIVE-STATUS scans. */
    List<Card> findByCardActiveStatus(String cardActiveStatus);

    /** Sequential read ordered by CARD-NUM, mirroring the CBACT02C keyed batch scan. */
    List<Card> findAllByOrderByCardNumAsc();
}
