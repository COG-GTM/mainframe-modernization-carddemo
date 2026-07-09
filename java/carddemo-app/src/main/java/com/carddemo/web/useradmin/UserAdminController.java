package com.carddemo.web.useradmin;

import com.carddemo.service.useradmin.UserAdminException;
import com.carddemo.service.useradmin.UserAdminService;
import com.carddemo.web.useradmin.dto.CreateUserRequest;
import com.carddemo.web.useradmin.dto.MessageResponse;
import com.carddemo.web.useradmin.dto.UpdateUserRequest;
import com.carddemo.web.useradmin.dto.UserAdminErrorResponse;
import com.carddemo.web.useradmin.dto.UserListResponse;
import com.carddemo.web.useradmin.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin user-maintenance REST API — the online CRUD ported from {@code COUSR00C} (list),
 * {@code COUSR01C} (add), {@code COUSR02C} (update) and {@code COUSR03C} (delete).
 *
 * <ul>
 *   <li>{@code GET    /api/admin/users}          — paged list ({@code COUSR00}, BMS paging).</li>
 *   <li>{@code POST   /api/admin/users}          — add ({@code COUSR01}); {@code 201}.</li>
 *   <li>{@code PUT    /api/admin/users/{userId}} — update ({@code COUSR02}).</li>
 *   <li>{@code DELETE /api/admin/users/{userId}} — delete ({@code COUSR03}).</li>
 * </ul>
 *
 * <p>Every endpoint is admin-only ({@code ROLE_ADMIN}) — the programs are reachable only from
 * the admin menu {@code COADM01C}. A signed-on non-admin ({@code ROLE_USER}) gets {@code 403}.
 * Rejections carry the verbatim {@code COUSRxxC} message.</p>
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService service;

    public UserAdminController(UserAdminService service) {
        this.service = service;
    }

    @GetMapping
    public UserListResponse list(
            @RequestParam(name = "startUserId", required = false) String startUserId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return service.list(startUserId, page, size);
    }

    @PostMapping
    public ResponseEntity<UserResponse> add(@RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(request));
    }

    @PutMapping("/{userId}")
    public UserResponse update(@PathVariable("userId") String userId,
            @RequestBody UpdateUserRequest request) {
        return service.update(userId, request);
    }

    @DeleteMapping("/{userId}")
    public MessageResponse delete(@PathVariable("userId") String userId) {
        return new MessageResponse(service.delete(userId));
    }

    @ExceptionHandler(UserAdminException.class)
    public ResponseEntity<UserAdminErrorResponse> handle(UserAdminException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new UserAdminErrorResponse(ex.getMessage()));
    }
}
