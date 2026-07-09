package com.carddemo.web.useradmin.dto;

import java.util.List;

/**
 * Result of {@code GET /api/admin/users} — one page of the {@code COUSR00} user list.
 *
 * <p>Mirrors the BMS paging of {@code COUSR00C}: a fixed page window (default 10 rows, matching
 * the {@code USER-REC OCCURS 10 TIMES} array) over the USRSEC records ordered by
 * {@code SEC-USR-ID} ascending, with {@code moreAvailable} standing in for the COBOL
 * {@code CDEMO-CU00-NEXT-PAGE-FLG} (PF8 has another page).</p>
 *
 * @param page          1-based page number ({@code CDEMO-CU00-PAGE-NUM})
 * @param size          rows per page (10 = {@code USER-REC OCCURS 10})
 * @param moreAvailable whether a further page exists (PF8 next-page flag)
 * @param totalUsers    total USRSEC records (all users, ignoring the start filter)
 * @param users         the page rows
 */
public record UserListResponse(
        int page,
        int size,
        boolean moreAvailable,
        long totalUsers,
        List<UserSummary> users) {
}
