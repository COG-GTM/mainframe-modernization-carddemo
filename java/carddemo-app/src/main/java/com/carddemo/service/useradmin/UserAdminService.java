package com.carddemo.service.useradmin;

import com.carddemo.domain.SecurityUser;
import com.carddemo.repository.SecurityUserRepository;
import com.carddemo.web.useradmin.dto.CreateUserRequest;
import com.carddemo.web.useradmin.dto.UpdateUserRequest;
import com.carddemo.web.useradmin.dto.UserListResponse;
import com.carddemo.web.useradmin.dto.UserResponse;
import com.carddemo.web.useradmin.dto.UserSummary;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Port of the online COBOL user-maintenance programs to a service over the USRSEC store
 * ({@link SecurityUser} / copybook {@code CSUSR01Y}):
 *
 * <ul>
 *   <li>{@link #list} ← {@code COUSR00C} — paged list ordered by {@code SEC-USR-ID}.</li>
 *   <li>{@link #add} ← {@code COUSR01C} — required-field validation then {@code WRITE}
 *       (duplicate key rejected).</li>
 *   <li>{@link #update} ← {@code COUSR02C} — required-field validation, {@code READ}
 *       (not-found), field-diff, then {@code REWRITE}; unchanged input is a no-op.</li>
 *   <li>{@link #delete} ← {@code COUSR03C} — {@code READ} (not-found) then {@code DELETE}.</li>
 * </ul>
 *
 * <p><strong>Password strategy.</strong> USRSEC stores 8-char <em>plaintext</em> passwords;
 * {@link com.carddemo.security.CardDemoUserDetails#getPassword()} prefixes the stored value
 * with {@code {noop}} for the delegating {@code PasswordEncoder}. New/updated passwords are
 * therefore persisted as plaintext to match the seed convention (see CS-2 docs) — running them
 * through {@code PasswordEncoder#encode} (bcrypt by default) would break both the 8-char column
 * and the {@code {noop}} comparison path. See {@code docs/mapping/CS-9-user-admin.md}.</p>
 */
@Service
public class UserAdminService {

    /** {@code USER-REC OCCURS 10 TIMES} on {@code COUSR00} — rows per page. */
    public static final int PAGE_SIZE = 10;

    private static final int USER_ID_MAX = 8;
    private static final int NAME_MAX = 20;
    private static final int PASSWORD_MAX = 8;

    private final SecurityUserRepository repository;

    public UserAdminService(SecurityUserRepository repository) {
        this.repository = repository;
    }

    /**
     * Paged user list ordered by {@code SEC-USR-ID} ascending. {@code startUserId} positions
     * the window like the {@code STARTBR RIDFLD(SEC-USR-ID)} browse (records with an id
     * &ge; the start key); a blank start begins at the first record.
     */
    @Transactional(readOnly = true)
    public UserListResponse list(String startUserId, int page, int size) {
        int pageNum = page < 1 ? 1 : page;
        int pageSize = size < 1 ? PAGE_SIZE : size;
        String start = normalize(startUserId);

        List<SecurityUser> all = repository.findAll(Sort.by(Sort.Direction.ASC, "secUsrId"));
        long totalUsers = all.size();

        List<UserSummary> filtered = all.stream()
                .filter(u -> start == null
                        || nullToEmpty(u.getSecUsrId()).trim().compareTo(start) >= 0)
                .map(UserSummary::from)
                .toList();

        int from = (pageNum - 1) * pageSize;
        if (from >= filtered.size()) {
            return new UserListResponse(pageNum, pageSize, false, totalUsers, List.of());
        }
        int to = Math.min(from + pageSize, filtered.size());
        List<UserSummary> pageRows = List.copyOf(filtered.subList(from, to));
        boolean moreAvailable = to < filtered.size();
        return new UserListResponse(pageNum, pageSize, moreAvailable, totalUsers, pageRows);
    }

    /**
     * Add a user — {@code COUSR01C}. Validates the required fields (in COBOL order) then
     * writes, rejecting a duplicate id with {@link UserAdminMessages#USER_ID_EXISTS}.
     */
    @Transactional
    public UserResponse add(CreateUserRequest request) {
        String firstName = normalize(request.firstName());
        String lastName = normalize(request.lastName());
        String userId = normalize(request.userId());
        String password = trimOrNull(request.password());
        String userType = normalizeType(request.userType());

        requireNotBlank(firstName, UserAdminMessages.FIRST_NAME_EMPTY);
        requireNotBlank(lastName, UserAdminMessages.LAST_NAME_EMPTY);
        requireNotBlank(userId, UserAdminMessages.USER_ID_EMPTY);
        requireNotBlank(password, UserAdminMessages.PASSWORD_EMPTY);
        requireNotBlank(userType, UserAdminMessages.USER_TYPE_EMPTY);
        validateWidthsAndType(userId, firstName, lastName, password, userType);

        if (repository.existsById(userId)) {
            throw new UserAdminException(UserAdminMessages.USER_ID_EXISTS, HttpStatus.CONFLICT);
        }

        SecurityUser user = new SecurityUser();
        user.setSecUsrId(userId);
        user.setSecUsrFname(firstName);
        user.setSecUsrLname(lastName);
        user.setSecUsrPwd(password);
        user.setSecUsrType(userType);
        SecurityUser saved = repository.save(user);
        return UserResponse.from(saved, UserAdminMessages.added(userId));
    }

    /**
     * Update a user — {@code COUSR02C}. Validates the required fields, reads the record
     * (not-found → 404), applies only the changed fields ({@code IF field NOT = stored}) and
     * rewrites; if nothing changed it is a no-op carrying {@link UserAdminMessages#NO_CHANGES}.
     */
    @Transactional
    public UserResponse update(String userId, UpdateUserRequest request) {
        String id = normalize(userId);
        String firstName = normalize(request.firstName());
        String lastName = normalize(request.lastName());
        String password = trimOrNull(request.password());
        String userType = normalizeType(request.userType());

        requireNotBlank(id, UserAdminMessages.USER_ID_EMPTY);
        requireNotBlank(firstName, UserAdminMessages.FIRST_NAME_EMPTY);
        requireNotBlank(lastName, UserAdminMessages.LAST_NAME_EMPTY);
        requireNotBlank(password, UserAdminMessages.PASSWORD_EMPTY);
        requireNotBlank(userType, UserAdminMessages.USER_TYPE_EMPTY);
        validateWidthsAndType(id, firstName, lastName, password, userType);

        SecurityUser user = repository.findById(id).orElseThrow(
                () -> new UserAdminException(UserAdminMessages.USER_ID_NOT_FOUND, HttpStatus.NOT_FOUND));

        boolean modified = false;
        if (!firstName.equals(nullToEmpty(user.getSecUsrFname()).trim())) {
            user.setSecUsrFname(firstName);
            modified = true;
        }
        if (!lastName.equals(nullToEmpty(user.getSecUsrLname()).trim())) {
            user.setSecUsrLname(lastName);
            modified = true;
        }
        if (!password.equals(nullToEmpty(user.getSecUsrPwd()).trim())) {
            user.setSecUsrPwd(password);
            modified = true;
        }
        if (!userType.equals(nullToEmpty(user.getSecUsrType()).trim())) {
            user.setSecUsrType(userType);
            modified = true;
        }

        if (!modified) {
            return UserResponse.from(user, UserAdminMessages.NO_CHANGES);
        }
        SecurityUser saved = repository.save(user);
        return UserResponse.from(saved, UserAdminMessages.updated(id));
    }

    /**
     * Delete a user — {@code COUSR03C}. Reads the record first (not-found → 404) then deletes.
     *
     * @return the verbatim {@code "User ... has been deleted ..."} confirmation.
     */
    @Transactional
    public String delete(String userId) {
        String id = normalize(userId);
        requireNotBlank(id, UserAdminMessages.USER_ID_EMPTY);
        if (!repository.existsById(id)) {
            throw new UserAdminException(UserAdminMessages.USER_ID_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        repository.deleteById(id);
        return UserAdminMessages.deleted(id);
    }

    /** Lookup a single user (used by the {@code COUSR02/03} screen handlers), or empty. */
    @Transactional(readOnly = true)
    public java.util.Optional<UserSummary> find(String userId) {
        return repository.findById(normalize(userId)).map(UserSummary::from);
    }

    // --- validation helpers ----------------------------------------------------------------

    private void validateWidthsAndType(String userId, String firstName, String lastName,
            String password, String userType) {
        if (userId != null && userId.length() > USER_ID_MAX) {
            throw new UserAdminException(UserAdminMessages.USER_ID_TOO_LONG, HttpStatus.BAD_REQUEST);
        }
        if (firstName != null && firstName.length() > NAME_MAX) {
            throw new UserAdminException(UserAdminMessages.FIRST_NAME_TOO_LONG, HttpStatus.BAD_REQUEST);
        }
        if (lastName != null && lastName.length() > NAME_MAX) {
            throw new UserAdminException(UserAdminMessages.LAST_NAME_TOO_LONG, HttpStatus.BAD_REQUEST);
        }
        if (password != null && password.length() > PASSWORD_MAX) {
            throw new UserAdminException(UserAdminMessages.PASSWORD_TOO_LONG, HttpStatus.BAD_REQUEST);
        }
        if (userType != null && !userType.equals("A") && !userType.equals("U")) {
            throw new UserAdminException(UserAdminMessages.INVALID_USER_TYPE, HttpStatus.BAD_REQUEST);
        }
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isEmpty()) {
            throw new UserAdminException(message, HttpStatus.BAD_REQUEST);
        }
    }

    /** Trim; blank → {@code null} (mirrors the COBOL {@code = SPACES OR LOW-VALUES} test). */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Trim only; blank → {@code null}. Passwords are not upper-cased (unlike {@code COSGN00C}). */
    private static String trimOrNull(String value) {
        return normalize(value);
    }

    /** Normalize {@code SEC-USR-TYPE}: trim + upper-case (mirrors the 'A'/'U' domain). */
    private static String normalizeType(String value) {
        String trimmed = normalize(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
