package com.carddemo.online.user;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.SecurityUser;
import com.carddemo.online.common.CommonMessages;
import com.carddemo.online.common.OnlinePrograms;
import com.carddemo.online.common.ScreenHeader;
import com.carddemo.repository.SecurityUserRepository;
import org.springframework.stereotype.Service;

/**
 * COBOL program: COUSR01C — add a user to USRSEC (transaction CU01), BMS mapset COUSR01, record
 * copybook CSUSR01Y, COMMAREA copybook COCOM01Y.
 */
@Service
public class UserAddService {

    private final SecurityUserRepository securityUserRepository;

    public UserAddService(SecurityUserRepository securityUserRepository) {
        this.securityUserRepository = securityUserRepository;
    }

    /** First entry with a COMMAREA but {@code NOT CDEMO-PGM-REENTER}: an empty add screen. */
    public UserDetailResponse initialScreen(CardDemoCommarea commarea) {
        commarea.setProgramContext(1);
        return screen(new UserFormRequest(null, null, null, null, null), null, null, "FNAME", null);
    }

    /** COBOL paragraph: PROCESS-ENTER-KEY followed by WRITE-USER-SEC-FILE. */
    public UserDetailResponse processEnterKey(UserFormRequest request) {
        if (isEmpty(request.firstName())) {
            return screen(request, "First Name can NOT be empty...", null, "FNAME", null);
        }
        if (isEmpty(request.lastName())) {
            return screen(request, "Last Name can NOT be empty...", null, "LNAME", null);
        }
        if (isEmpty(request.userId())) {
            return screen(request, "User ID can NOT be empty...", null, "USERID", null);
        }
        if (isEmpty(request.password())) {
            return screen(request, "Password can NOT be empty...", null, "PASSWD", null);
        }
        if (isEmpty(request.userType())) {
            return screen(request, "User Type can NOT be empty...", null, "USRTYPE", null);
        }
        return writeUserSecurityFile(request);
    }

    /** COBOL paragraph: WRITE-USER-SEC-FILE (NORMAL / DUPKEY / DUPREC / other). */
    private UserDetailResponse writeUserSecurityFile(UserFormRequest request) {
        String userId = request.userId().trim();
        if (securityUserRepository.existsById(userId)) {
            // DFHRESP(DUPKEY) / DFHRESP(DUPREC)
            return screen(request, "User ID already exist...", null, "USERID", null);
        }

        securityUserRepository.save(SecurityUser.builder()
                .userId(userId)
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .password(request.password().trim())
                .userType(request.userType().trim())
                .build());

        // INITIALIZE-ALL-FIELDS then the green confirmation built with STRING ... DELIMITED BY SPACE.
        return screen(
                new UserFormRequest(null, null, null, null, null),
                "User " + userId + " has been added ...",
                UserDetailResponse.COLOR_GREEN,
                "FNAME",
                null);
    }

    /** COBOL paragraph: CLEAR-CURRENT-SCREEN (DFHPF4). */
    public UserDetailResponse processPf4Key() {
        return screen(new UserFormRequest(null, null, null, null, null), null, null, "FNAME", null);
    }

    /** DFHPF3: back to the admin menu. */
    public UserDetailResponse processPf3Key(CardDemoCommarea commarea) {
        commarea.setToProgram(OnlinePrograms.ADMIN_MENU);
        commarea.setToTransactionId(OnlinePrograms.TRANID_ADMIN_MENU);
        commarea.setFromTransactionId(OnlinePrograms.TRANID_USER_ADD);
        commarea.setFromProgram(OnlinePrograms.USER_ADD);
        commarea.setProgramContext(0);
        return screen(
                new UserFormRequest(null, null, null, null, null),
                null,
                null,
                null,
                OnlinePrograms.ADMIN_MENU);
    }

    /** {@code WHEN OTHER} of the EVALUATE EIBAID. */
    public UserDetailResponse processOtherKey(UserFormRequest request) {
        return screen(request, CommonMessages.INVALID_KEY, null, "FNAME", null);
    }

    private UserDetailResponse screen(
            UserFormRequest request,
            String message,
            String color,
            String cursorField,
            String nextProgram) {
        return new UserDetailResponse(
                ScreenHeader.of(OnlinePrograms.TRANID_USER_ADD, OnlinePrograms.USER_ADD),
                request.userId(),
                request.firstName(),
                request.lastName(),
                request.password(),
                request.userType(),
                message,
                color,
                cursorField,
                nextProgram,
                OnlinePrograms.transactionIdOf(nextProgram));
    }

    /** {@code = SPACES OR LOW-VALUES}. */
    private static boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }
}
