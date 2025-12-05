package com.carddemo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * User List Response DTO with pagination
 * 
 * Maps to the COUSR00 (User List) screen functionality:
 * - Displays up to 10 users per page (USER-REC OCCURS 10 TIMES)
 * - Supports PF7 (page backward) and PF8 (page forward)
 * - Tracks CDEMO-CU00-PAGE-NUM for current page
 * - Tracks CDEMO-CU00-NEXT-PAGE-FLG for more pages indicator
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {

    /**
     * List of users on current page
     */
    private List<UserDto> users;

    /**
     * Current page number (1-based, like mainframe)
     * Maps to CDEMO-CU00-PAGE-NUM
     */
    private int currentPage;

    /**
     * Total number of pages
     */
    private int totalPages;

    /**
     * Total number of users
     */
    private long totalUsers;

    /**
     * Whether there are more pages (for PF8)
     * Maps to CDEMO-CU00-NEXT-PAGE-FLG
     */
    private boolean hasNextPage;

    /**
     * Whether there are previous pages (for PF7)
     */
    private boolean hasPreviousPage;
}
