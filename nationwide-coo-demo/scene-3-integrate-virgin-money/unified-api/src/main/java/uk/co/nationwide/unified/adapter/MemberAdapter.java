package uk.co.nationwide.unified.adapter;

import java.util.Optional;
import uk.co.nationwide.unified.domain.Member;
import uk.co.nationwide.unified.domain.SourceSystem;

/**
 * Anti-corruption layer contract: each source-system adapter knows how to
 * translate <em>its</em> native records into the canonical {@link Member}
 * domain. Adapters do not know about each other.
 */
public interface MemberAdapter {

    /** Which estate this adapter serves. */
    SourceSystem source();

    /**
     * Look up a member by their canonical {@link uk.co.nationwide.unified.domain.MemberId}.
     */
    Optional<Member> findMember(String memberId);
}
