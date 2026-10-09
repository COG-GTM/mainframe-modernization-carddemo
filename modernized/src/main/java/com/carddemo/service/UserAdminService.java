package com.carddemo.service;

import com.carddemo.domain.SecUser;
import com.carddemo.exception.BusinessRuleException;
import com.carddemo.exception.RecordNotFoundException;
import com.carddemo.repository.SecUserRepository;
import com.carddemo.web.dto.PageResponse;
import com.carddemo.web.dto.UserRequest;
import com.carddemo.web.dto.UserView;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** COUSR00C (list), COUSR01C (add), COUSR02C (update) and COUSR03C (delete). */
@Service
public class UserAdminService {

    /** COUSR00C displayed ten rows per screen. */
    public static final int DEFAULT_PAGE_SIZE = 10;

    private final SecUserRepository users;

    public UserAdminService(SecUserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserView> list(int page, int size) {
        var result = users.findAll(PageRequest.of(page, size, Sort.by("id")));
        if (result.isEmpty()) {
            throw new RecordNotFoundException("You are already at the top of the page...");
        }
        return PageResponse.of(result, UserAdminService::toView);
    }

    @Transactional(readOnly = true)
    public UserView view(String userId) {
        return toView(find(userId));
    }

    @Transactional
    public UserView add(UserRequest request) {
        validate(request, true);
        String userId = request.userId().trim().toUpperCase();
        if (users.existsById(userId)) {
            throw new BusinessRuleException("User ID already exist...");
        }
        SecUser user = new SecUser();
        user.setId(userId);
        apply(user, request);
        return toView(users.save(user));
    }

    @Transactional
    public UserView update(String userId, UserRequest request) {
        SecUser user = find(userId);
        validate(request, false);
        if (user.getFirstName().trim().equals(request.firstName().trim())
                && user.getLastName().trim().equals(request.lastName().trim())
                && user.getPassword().trim().equals(request.password().trim())
                && user.getUserType().trim().equalsIgnoreCase(request.userType().trim())) {
            throw new BusinessRuleException("Please modify to update ...");
        }
        apply(user, request);
        return toView(users.save(user));
    }

    @Transactional
    public void delete(String userId) {
        users.delete(find(userId));
    }

    private SecUser find(String userId) {
        String id = userId == null ? "" : userId.trim().toUpperCase();
        if (id.isEmpty()) {
            throw new BusinessRuleException("User ID can NOT be empty...");
        }
        return users.findById(id).orElseThrow(() -> new RecordNotFoundException("User ID NOT found..."));
    }

    private void apply(SecUser user, UserRequest request) {
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPassword(request.password().trim());
        user.setUserType(request.userType().trim().toUpperCase());
    }

    private void validate(UserRequest request, boolean requireUserId) {
        if (requireUserId && isEmpty(request.userId())) {
            throw new BusinessRuleException("User ID can NOT be empty...");
        }
        if (requireUserId && request.userId().trim().length() > 8) {
            throw new BusinessRuleException("User ID must be 8 characters or less...");
        }
        if (isEmpty(request.firstName())) {
            throw new BusinessRuleException("First Name can NOT be empty...");
        }
        if (isEmpty(request.lastName())) {
            throw new BusinessRuleException("Last Name can NOT be empty...");
        }
        if (isEmpty(request.password())) {
            throw new BusinessRuleException("Password can NOT be empty...");
        }
        if (isEmpty(request.userType())) {
            throw new BusinessRuleException("User Type can NOT be empty...");
        }
        String type = request.userType().trim().toUpperCase();
        if (!type.equals("A") && !type.equals("U")) {
            throw new BusinessRuleException("User Type must be A (Admin) or U (User)...");
        }
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    static UserView toView(SecUser user) {
        return new UserView(user.getId(), user.getFirstName(), user.getLastName(), user.getUserType());
    }
}
