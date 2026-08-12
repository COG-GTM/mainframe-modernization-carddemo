package com.carddemo.online.auth;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.SecurityUser;
import com.carddemo.online.common.CommonMessages;
import com.carddemo.online.common.OnlinePrograms;
import com.carddemo.online.common.ScreenHeader;
import com.carddemo.repository.SecurityUserRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * COBOL program: COSGN00C — signon screen of the CardDemo application (transaction CC00).
 *
 * <p>Reads the USRSEC VSAM file through copybook CSUSR01Y (entity {@code SecurityUser}) and, on a
 * successful signon, fills the COCOM01Y COMMAREA before transferring to COADM01C
 * (CDEMO-USRTYP-ADMIN) or COMEN01C. Screen fields come from BMS mapset COSGN00.
 */
@Service
public class SignOnService {

    private final SecurityUserRepository securityUserRepository;

    public SignOnService(SecurityUserRepository securityUserRepository) {
        this.securityUserRepository = securityUserRepository;
    }

    /** {@code IF EIBCALEN = 0}: an empty signon screen with the cursor on the user id. */
    public SignOnResponse initialScreen(CardDemoCommarea commarea) {
        commarea.setProgramContext(0);
        return screen(null, null, "USERID", null);
    }

    /** COBOL paragraph: PROCESS-ENTER-KEY. */
    public SignOnResponse processEnterKey(SignOnRequest request, CardDemoCommarea commarea) {
        boolean errorFlag = false;
        String message = null;
        String cursorField = null;

        if (isEmpty(request.userId())) {
            errorFlag = true;
            message = "Please enter User ID ...";
            cursorField = "USERID";
        } else if (isEmpty(request.password())) {
            errorFlag = true;
            message = "Please enter Password ...";
            cursorField = "PASSWD";
        }

        String userId = upperCase(request.userId());
        String password = upperCase(request.password());
        commarea.setUserId(userId);

        if (errorFlag) {
            return screen(userId, message, cursorField, null);
        }
        return readUserSecurityFile(userId, password, commarea);
    }

    /** COBOL paragraph: READ-USER-SEC-FILE (RESP 0 / 13 / other). */
    private SignOnResponse readUserSecurityFile(
            String userId, String password, CardDemoCommarea commarea) {
        Optional<SecurityUser> found = securityUserRepository.findById(userId);
        if (found.isEmpty()) {
            // WS-RESP-CD = 13 (NOTFND)
            return screen(userId, "User not found. Try again ...", "USERID", null);
        }

        SecurityUser user = found.get();
        if (!password.equals(trim(user.getPassword()))) {
            // COSGN00C leaves WS-ERR-FLG off here, but the screen is redisplayed all the same.
            return screen(userId, "Wrong Password. Try again ...", "PASSWD", null);
        }

        commarea.setFromTransactionId(OnlinePrograms.TRANID_SIGNON);
        commarea.setFromProgram(OnlinePrograms.SIGNON);
        commarea.setUserId(userId);
        commarea.setUserType(user.getUserType());
        commarea.setProgramContext(0);

        String target = commarea.isAdmin() ? OnlinePrograms.ADMIN_MENU : OnlinePrograms.MAIN_MENU;
        commarea.setToProgram(target);
        commarea.setToTransactionId(OnlinePrograms.transactionIdOf(target));
        return new SignOnResponse(
                header(), userId, null, null, target, OnlinePrograms.transactionIdOf(target));
    }

    /** COBOL paragraph: SEND-PLAIN-TEXT after DFHPF3 (CCDA-MSG-THANK-YOU). */
    public SignOnResponse processPf3Key() {
        return screen(null, CommonMessages.THANK_YOU, null, null);
    }

    /** {@code WHEN OTHER} of the EVALUATE EIBAID (CCDA-MSG-INVALID-KEY). */
    public SignOnResponse processOtherKey() {
        return screen(null, CommonMessages.INVALID_KEY, null, null);
    }

    private SignOnResponse screen(
            String userId, String message, String cursorField, String nextProgram) {
        return new SignOnResponse(
                header(),
                userId,
                message,
                cursorField,
                nextProgram,
                OnlinePrograms.transactionIdOf(nextProgram));
    }

    private ScreenHeader header() {
        return ScreenHeader.of(OnlinePrograms.TRANID_SIGNON, OnlinePrograms.SIGNON);
    }

    /** {@code = SPACES OR LOW-VALUES} on a BMS input field. */
    private static boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }

    private static String upperCase(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
