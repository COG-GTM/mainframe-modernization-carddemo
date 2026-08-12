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
 * COBOL program: COUSR03C — delete a USRSEC user (transaction CU03), BMS mapset COUSR03, record
 * copybook CSUSR01Y, COMMAREA copybook COCOM01Y plus the CDEMO-CU03-INFO extension carried by
 * {@link UserAdminState}.
 */
@Service
public class UserDeleteService {

    private final SecurityUserRepository securityUserRepository;

    public UserDeleteService(SecurityUserRepository securityUserRepository) {
        this.securityUserRepository = securityUserRepository;
    }

    /**
     * First entry with a COMMAREA but {@code NOT CDEMO-PGM-REENTER}: when COUSR00C passed a
     * selected user in CDEMO-CU03-USR-SELECTED the record is read straight away.
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

        Optional<SecurityUser> found = securityUserRepository.findById(request.userId().trim());
        if (found.isEmpty()) {
            return screen(
                    new UserFormRequest(request.userId(), null, null, null, null),
                    "User ID NOT found...",
                    null,
                    "USRIDIN",
                    null);
        }

        return screen(
                toRequest(found.get()),
                "Press PF5 key to delete this user ...",
                UserDetailResponse.COLOR_NEUTRAL,
                "USRIDIN",
                null);
    }

    /** COBOL paragraph: DELETE-USER-INFO followed by DELETE-USER-SEC-FILE (DFHPF5). */
    public UserDetailResponse processPf5Key(UserFormRequest request) {
        if (isEmpty(request.userId())) {
            return screen(request, "User ID can NOT be empty...", null, "USRIDIN", null);
        }

        String userId = request.userId().trim();
        Optional<SecurityUser> found = securityUserRepository.findById(userId);
        if (found.isEmpty()) {
            return screen(request, "User ID NOT found...", null, "USRIDIN", null);
        }

        securityUserRepository.delete(found.get());
        // INITIALIZE-ALL-FIELDS then the green confirmation.
        return screen(
                empty(),
                "User " + userId + " has been deleted ...",
                UserDetailResponse.COLOR_GREEN,
                "USRIDIN",
                null);
    }

    /** DFHPF3: back to CDEMO-FROM-PROGRAM, COADM01C when it is blank. Nothing is deleted. */
    public UserDetailResponse processPf3Key(CardDemoCommarea commarea) {
        String target = CommareaSession.isBlank(commarea.getFromProgram())
                ? OnlinePrograms.ADMIN_MENU
                : commarea.getFromProgram();
        return returnToPreviousScreen(target, commarea);
    }

    /** DFHPF12: back to the admin menu. */
    public UserDetailResponse processPf12Key(CardDemoCommarea commarea) {
        return returnToPreviousScreen(OnlinePrograms.ADMIN_MENU, commarea);
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
    private UserDetailResponse returnToPreviousScreen(String toProgram, CardDemoCommarea commarea) {
        commarea.setToProgram(toProgram);
        commarea.setToTransactionId(OnlinePrograms.transactionIdOf(toProgram));
        commarea.setFromTransactionId(OnlinePrograms.TRANID_USER_DELETE);
        commarea.setFromProgram(OnlinePrograms.USER_DELETE);
        commarea.setProgramContext(0);
        return screen(empty(), null, null, null, toProgram);
    }

    private static UserFormRequest empty() {
        return new UserFormRequest(null, null, null, null, null);
    }

    /** COUSR03C shows everything but the password. */
    private static UserFormRequest toRequest(SecurityUser user) {
        return new UserFormRequest(
                user.getUserId(), user.getFirstName(), user.getLastName(), null, user.getUserType());
    }

    private UserDetailResponse screen(
            UserFormRequest request,
            String message,
            String color,
            String cursorField,
            String nextProgram) {
        return new UserDetailResponse(
                ScreenHeader.of(OnlinePrograms.TRANID_USER_DELETE, OnlinePrograms.USER_DELETE),
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

    private static boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }
}
