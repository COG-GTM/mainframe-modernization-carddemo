package com.carddemo.service;

import com.carddemo.domain.SecUser;
import com.carddemo.exception.BusinessRuleException;
import com.carddemo.repository.SecUserRepository;
import com.carddemo.web.dto.SignonRequest;
import com.carddemo.web.dto.SignonResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * COSGN00C - signon.
 *
 * <p>The COBOL program uppercased both fields, read USRSEC by the 8 character user id and routed
 * admins to COADM01C and everyone else to COMEN01C.
 */
@Service
public class SignonService {

    private static final String ADMIN_MENU_PROGRAM = "COADM01C";
    private static final String MAIN_MENU_PROGRAM = "COMEN01C";

    private final SecUserRepository users;

    public SignonService(SecUserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public SignonResponse signon(SignonRequest request) {
        String userId = normalize(request.userId());
        String password = normalize(request.password());

        if (userId.isEmpty()) {
            throw new BusinessRuleException("Please enter User ID ...");
        }
        if (password.isEmpty()) {
            throw new BusinessRuleException("Please enter Password ...");
        }

        SecUser user = users.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("User not found. Try again ..."));

        if (!password.equals(normalize(user.getPassword()))) {
            throw new BusinessRuleException("Wrong Password. Try again ...");
        }

        return new SignonResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserType(),
                user.isAdmin(),
                user.isAdmin() ? ADMIN_MENU_PROGRAM : MAIN_MENU_PROGRAM);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
