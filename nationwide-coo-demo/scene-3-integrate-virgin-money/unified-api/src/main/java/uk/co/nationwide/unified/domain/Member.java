package uk.co.nationwide.unified.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Canonical, member-facing representation. Designed to be a superset of what
 * both source systems can express, with explicit source attribution per
 * field so the channel always knows where data came from.
 *
 * <p>Member identifiers use the prefix scheme defined in
 * {@link uk.co.nationwide.unified.domain.MemberId}.
 */
public record Member(
        String memberId,
        String firstName,
        String lastName,
        String email,
        String phone,
        Address address,
        LocalDate dateOfBirth,
        List<Account> accounts,
        SourceSystem primarySource
) {

    public record Address(
            String line1,
            String line2,
            String line3,
            String city,
            String postcode,
            String countryCode
    ) { }
}
