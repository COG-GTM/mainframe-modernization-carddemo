package com.carddemo.auth.service;

import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.dto.UserInfoResponse;
import com.carddemo.auth.entity.UserSecurityEntity;
import com.carddemo.auth.exception.AuthenticationException;
import com.carddemo.auth.repository.UserSecurityRepository;
import com.carddemo.auth.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

/**
 * Authentication business logic service.
 *
 * Migrated from: COSGN00C.cbl, paragraphs PROCESS-ENTER-KEY (lines 108-140)
 *                and READ-USER-SEC-FILE (lines 209-257)
 * VSAM file: USRSEC (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS)
 * Original XCTL targets: COADM01C (admin menu), COMEN01C (regular user menu)
 *
 * The original COBOL flow:
 *   1. Receive USERID and PASSWORD from BMS map COSGN0A
 *   2. UPPER-CASE both inputs
 *   3. READ USRSEC file using USER-ID as RIDFLD key
 *   4. Compare SEC-USR-PWD against entered password
 *   5. On match: set COMMAREA fields, XCTL to admin or user menu
 *   6. On mismatch: display "Wrong Password. Try again ..."
 *   7. On NOTFND (RESP=13): display "User not found. Try again ..."
 */
@Service
public class AuthService {

    private final UserSecurityRepository userSecurityRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserSecurityRepository userSecurityRepository,
                       JwtTokenProvider jwtTokenProvider) {
        this.userSecurityRepository = userSecurityRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Authenticate a user with userId and password.
     *
     * Mirrors the COBOL READ-USER-SEC-FILE paragraph:
     *   - RESP=0 + password match  -> success (XCTL to menu)
     *   - RESP=0 + password mismatch -> "Wrong Password. Try again ..."
     *   - RESP=13 (NOTFND) -> "User not found. Try again ..."
     *
     * @param userId   the user ID (will be uppercased, max 8 chars)
     * @param password the password (will be uppercased, max 8 chars)
     * @return LoginResponse with JWT token and user details
     * @throws AuthenticationException on invalid credentials
     */
    public LoginResponse authenticate(String userId, String password) {
        // COBOL: MOVE FUNCTION UPPER-CASE(USERIDI) TO WS-USER-ID
        String normalizedUserId = userId.toUpperCase().trim();
        String normalizedPassword = password.toUpperCase().trim();

        // COBOL: EXEC CICS READ DATASET('USRSEC') INTO(SEC-USER-DATA)
        //              RIDFLD(WS-USER-ID) RESP(WS-RESP-CD)
        UserSecurityEntity user = userSecurityRepository.findById(normalizedUserId)
                .orElseThrow(() -> new AuthenticationException(
                        "User not found. Try again ..."));

        // COBOL: IF SEC-USR-PWD = WS-USER-PWD
        // TODO: Replace with BCrypt comparison after password migration
        if (!user.getPassword().trim().equals(normalizedPassword)) {
            throw new AuthenticationException("Wrong Password. Try again ...");
        }

        // COBOL: MOVE SEC-USR-TYPE TO CDEMO-USER-TYPE
        //        IF CDEMO-USRTYP-ADMIN -> XCTL PROGRAM('COADM01C')
        //        ELSE                  -> XCTL PROGRAM('COMEN01C')
        String token = jwtTokenProvider.createToken(
                user.getUserId().trim(), user.getUserType().trim());

        return new LoginResponse(
                token,
                user.getUserId().trim(),
                user.getFirstName().trim(),
                user.getLastName().trim(),
                user.getUserType().trim()
        );
    }

    /**
     * Retrieve user information by user ID (from JWT token).
     *
     * @param userId the authenticated user's ID from the JWT subject claim
     * @return UserInfoResponse with user details
     * @throws AuthenticationException if user no longer exists
     */
    public UserInfoResponse getUserInfo(String userId) {
        UserSecurityEntity user = userSecurityRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException(
                        "User not found. Try again ..."));

        return new UserInfoResponse(
                user.getUserId().trim(),
                user.getFirstName().trim(),
                user.getLastName().trim(),
                user.getUserType().trim()
        );
    }
}
