package com.carddemo.user.dto;

import java.util.List;

/**
 * Response DTO for paginated user lists.
 * Mirrors the COUSR00C (User List) screen which displays paginated user records
 * with page navigation (PF7=backward, PF8=forward).
 */
public class UserListResponse {

    private List<UserDto> users;
    private int currentPage;
    private int totalPages;
    private long totalElements;

    public UserListResponse() {
    }

    public UserListResponse(List<UserDto> users, int currentPage, int totalPages, long totalElements) {
        this.users = users;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
}
