package uk.co.nationwide.unified.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nationwide.unified.adapter.MemberAdapter;
import uk.co.nationwide.unified.domain.Member;
import uk.co.nationwide.unified.domain.MemberId;
import uk.co.nationwide.unified.domain.SourceSystem;

/**
 * Routes a canonical member lookup to the right source-system adapter based
 * on the prefix of the member ID.
 *
 * <p>In a brownfield estate this dispatch is intentionally simple: the
 * prefix scheme defined in {@link MemberId} is the routing key. A future
 * canonical-member registry would replace this dispatch with a registry
 * lookup; the public API surface would not change.
 */
@Service
public class MemberLookupService {

    private final NationwideRoute nationwideRoute;
    private final VirginMoneyRoute virginMoneyRoute;

    public MemberLookupService(List<MemberAdapter> adapters) {
        this.nationwideRoute = new NationwideRoute(adapters);
        this.virginMoneyRoute = new VirginMoneyRoute(adapters);
    }

    public Optional<Member> findMember(String memberId) {
        SourceSystem source = MemberId.sourceOf(memberId);
        return switch (source) {
            case NATIONWIDE -> nationwideRoute.adapter.findMember(memberId);
            case VIRGIN_MONEY -> virginMoneyRoute.adapter.findMember(memberId);
        };
    }

    private record NationwideRoute(MemberAdapter adapter) {
        NationwideRoute(List<MemberAdapter> adapters) {
            this(adapters.stream()
                    .filter(a -> a.source() == SourceSystem.NATIONWIDE)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("no NATIONWIDE adapter wired")));
        }
    }

    private record VirginMoneyRoute(MemberAdapter adapter) {
        VirginMoneyRoute(List<MemberAdapter> adapters) {
            this(adapters.stream()
                    .filter(a -> a.source() == SourceSystem.VIRGIN_MONEY)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("no VIRGIN_MONEY adapter wired")));
        }
    }
}
