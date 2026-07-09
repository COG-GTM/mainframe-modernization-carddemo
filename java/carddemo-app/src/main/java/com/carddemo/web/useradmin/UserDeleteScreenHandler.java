package com.carddemo.web.useradmin;

import com.carddemo.service.useradmin.UserAdminException;
import com.carddemo.service.useradmin.UserAdminMessages;
import com.carddemo.service.useradmin.UserAdminService;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.PfKey;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import org.springframework.stereotype.Component;

/**
 * CS-3 screen handler for {@code COUSR03C} (Delete User). On first entry it presents an empty
 * form. On ENTER: without a {@code confirm=Y} field it looks the record up and displays it for
 * confirmation (the COBOL {@code PROCESS-ENTER-KEY} → {@code READ-USER-SEC-FILE} step); with
 * {@code confirm=Y} it deletes via {@link UserAdminService#delete} (the {@code PF5} step — the
 * CS-3 {@link PfKey} enum has no PF5, so a confirm flag stands in for it). PF4 clears; PF12
 * returns to the caller.
 *
 * <p>Request fields: {@code userId}, {@code confirm} ({@code Y} to delete).</p>
 */
@Component
public class UserDeleteScreenHandler implements ScreenHandler {

    private final UserAdminService service;

    public UserDeleteScreenHandler(UserAdminService service) {
        this.service = service;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.USER_DELETE;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        if (commarea.isEnter()) {
            return ScreenResult.stay();
        }
        if (request.pfKey() == PfKey.PF4) {
            return ScreenResult.stay();
        }
        if (request.pfKey() == PfKey.PF12) {
            return ScreenResult.back();
        }

        String userId = request.field("userId");
        if (isBlank(userId)) {
            return ScreenResult.stay(UserAdminMessages.USER_ID_EMPTY, null);
        }

        String confirm = request.field("confirm");
        if (confirm == null || !confirm.trim().equalsIgnoreCase("Y")) {
            return service.find(userId)
                    .map(u -> ScreenResult.stay("Press ENTER with confirm=Y to delete this user ...", u))
                    .orElse(ScreenResult.stay(UserAdminMessages.USER_ID_NOT_FOUND, null));
        }

        try {
            String message = service.delete(userId);
            return ScreenResult.stay(message, null);
        } catch (UserAdminException e) {
            return ScreenResult.stay(e.getMessage(), null);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
