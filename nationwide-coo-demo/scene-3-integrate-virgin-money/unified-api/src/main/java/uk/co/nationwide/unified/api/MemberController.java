package uk.co.nationwide.unified.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nationwide.unified.domain.Member;
import uk.co.nationwide.unified.service.MemberLookupService;

/**
 * Unified Member API. One contract for the digital channel and contact
 * centre, regardless of which estate owns the underlying record.
 */
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberLookupService service;

    public MemberController(MemberLookupService service) {
        this.service = service;
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<Member> get(@PathVariable String memberId) {
        return service.findMember(memberId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
