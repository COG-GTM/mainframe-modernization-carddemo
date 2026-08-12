package com.carddemo.online.user;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.SecurityUser;
import com.carddemo.online.common.CommareaSession;
import com.carddemo.online.common.CommonMessages;
import com.carddemo.online.common.OnlinePrograms;
import com.carddemo.online.common.ScreenHeader;
import com.carddemo.repository.SecurityUserRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * COBOL program: COUSR02C — update a USRSEC user (transaction CU02), BMS mapset COUSR02, record
 * copybook CSUSR01Y, COMMAREA copybook COCOM01Y plus the CDEMO-CU02-INFO extension carried by
 * {@link UserAdminState}.
 */
@Service
public class UserUpdateService {

    private final SecurityUserRepository securityUserRepository;

    public UserUpdateService(SecurityUserRepository securityUserRepository) {
        this.securityUserRepository = securityUserRepository;
    }

    /**
     * First entry with a COMMAREA but {@code NOT CDEMO-PGM-REENTER}: when COUSR00C passed a
     * selected user in CDEMO-CU02-USR-SELECTED the record is read straight away.
     */
    public UserDetailResponse initialScreen(CardDemoCommarea commarea, UserAdminState state) {
        commarea.setProgramContext(1);
        if (!isEmpty(state.getSelectedUserId())) {
            return processEnterKey(
                    new UserFormRequest(state.getSelectedUserId(), null, null, null, null));
        }
        return screen(empty(), null, null, "USRIDIN", null);
    }

    /** COBOL paragraph: PROCESS-ENTER-KEY, which reads the user and offers PF5. */
    public UserDetailResponse processEnterKey(UserFormRequest request) {
        if (isEmpty(request.userId())) {
            return screen(request, "User ID can NOT be empty...", null, "USRIDIN", null);
        }

        // The screen fields are cleared before the record is read.
        Optional<SecurityUser> found = securityUserRepository.findById(request.userId().trim());
        if (found.isEmpty()) {
            return screen(
                    new UserFormRequest(request.userId(), null, null, null, null),
                    "User ID NOT found...",
                    null,
                    "USRIDIN",
                    null);
        }

        SecurityUser user = found.get();
        return screen(
                toRequest(user),
                "Press PF5 key to save your updates ...",
                UserDetailResponse.COLOR_NEUTRAL,
                "USRIDIN",
                null);
    }

    /** COBOL paragraph: UPDATE-USER-INFO followed by UPDATE-USER-SEC-FILE (DFHPF5). */
    public UserDetailResponse processPf5Key(UserFormRequest request) {
        if (isEmpty(request.userId())) {
            return screen(request, "User ID can NOT be empty...", null, "USRIDIN", null);
        }
        if (isEmpty(request.firstName())) {
            return screen(request, "First Name can NOT be empty...", null, "FNAME", null);
        }
        if (isEmpty(request.lastName())) {
            return screen(request, "Last Name can NOT be empty...", null, "LNAME", null);
        }
        if (isEmpty(request.password())) {
            return screen(request, "Password can NOT be empty...", null, "PASSWD", null);
        }
        if (isEmpty(request.userType())) {
            return screen(request, "User Type can NOT be empty...", null, "USRTYPE", null);
        }

        String userId = request.userId().trim();
        Optional<SecurityUser> found = securityUserRepository.findById(userId);
        if (found.isEmpty()) {
            // READ answers NOTFND; the REWRITE that COUSR02C attempts next fails the same way.
            return screen(request, "User ID NOT found...", null, "USRIDIN", null);
        }

        SecurityUser user = found.get();
        boolean modified = false;
        if (!equalsField(request.firstName(), user.getFirstName())) {
            user.setFirstName(request.firstName().trim());
            modified = true;
        }
        if (!equalsField(request.lastName(), user.getLastName())) {
            user.setLastName(request.lastName().trim());
            modified = true;
        }
        if (!equalsField(request.password(), user.getPassword())) {
            user.setPassword(request.password().trim());
            modified = true;
        }
        if (!equalsField(request.userType(), user.getUserType())) {
            user.setUserType(request.userType().trim());
            modified = true;
        }

        if (!modified) {
            return screen(
                    toRequest(user),
                    "Please modify to update ...",
                    UserDetailResponse.COLOR_RED,
                    null,
                    null);
        }

        securityUserRepository.save(user);
        return screen(
                toRequest(user),
                "User " + userId + " has been updated ...",
                UserDetailResponse.COLOR_GREEN,
                null,
                null);
    }

    /**
     * DFHPF3: COUSR02C runs UPDATE-USER-INFO before transferring back to CDEMO-FROM-PROGRAM
     * (COADM01C when it is blank), so the pending changes are saved on the way out.
     */
    public UserDetailResponse processPf3Key(UserFormRequest request, CardDemoCommarea commarea) {
        UserDetailResponse updated = processPf5Key(request);
        String target = CommareaSession.isBlank(commarea.getFromProgram())
                ? OnlinePrograms.ADMIN_MENU
                : commarea.getFromProgram();
        return returnToPreviousScreen(target, commarea, updated.errorMessage(), updated.messageColor());
    }

    /** DFHPF12: back to the admin menu without saving. */
    public UserDetailResponse processPf12Key(CardDemoCommarea commarea) {
        return returnToPreviousScreen(OnlinePrograms.ADMIN_MENU, commarea, null, null);
    }

    /** COBOL paragraph: CLEAR-CURRENT-SCREEN (DFHPF4). */
    public UserDetailResponse processPf4Key() {
        return screen(empty(), null, null, "USRIDIN", null);
    }

    /** {@code WHEN OTHER} of the EVALUATE EIBAID. */
    public UserDetailResponse processOtherKey(UserFormRequest request) {
        return screen(request, CommonMessages.INVALID_KEY, null, null, null);
    }

    /** COBOL paragraph: RETURN-TO-PREV-SCREEN. */
    private UserDetailResponse returnToPreviousScreen(
            String toProgram, CardDemoCommarea commarea, String message, String color) {
        commarea.setToProgram(toProgram);
        commarea.setToTransactionId(OnlinePrograms.transactionIdOf(toProgram));
        commarea.setFromTransactionId(OnlinePrograms.TRANID_USER_UPDATE);
        commarea.setFromProgram(OnlinePrograms.USER_UPDATE);
        commarea.setProgramContext(0);
        return screen(empty(), message, color, null, toProgram);
    }

    private static UserFormRequest empty() {
        return new UserFormRequest(null, null, null, null, null);
    }

    private static UserFormRequest toRequest(SecurityUser user) {
        return new UserFormRequest(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword(),
                user.getUserType());
    }

    private UserDetailResponse screen(
            UserFormRequest request,
            String message,
            String color,
            String cursorField,
            String nextProgram) {
        return new UserDetailResponse(
                ScreenHeader.of(OnlinePrograms.TRANID_USER_UPDATE, OnlinePrograms.USER_UPDATE),
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

    /** Comparison of a BMS field with the stored record, blank padding aside. */
    private static boolean equalsField(String screenValue, String storedValue) {
        return trim(screenValue).equals(trim(storedValue));
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }
}
