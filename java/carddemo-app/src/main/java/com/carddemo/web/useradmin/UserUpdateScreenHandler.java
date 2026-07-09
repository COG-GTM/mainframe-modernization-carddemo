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
import com.carddemo.web.useradmin.dto.UpdateUserRequest;
import com.carddemo.web.useradmin.dto.UserResponse;
import org.springframework.stereotype.Component;

/**
 * CS-3 screen handler for {@code COUSR02C} (Update User). On first entry it presents an empty
 * form. On ENTER: with only a {@code userId} it looks the record up and displays it (the COBOL
 * {@code PROCESS-ENTER-KEY} → {@code READ-USER-SEC-FILE} step); with edited fields it applies
 * the change via {@link UserAdminService#update} (the {@code PF5} save step — the CS-3
 * {@link PfKey} enum has no PF5, so ENTER-with-edits stands in for it). PF4 clears; PF12
 * returns to the caller.
 *
 * <p>Request fields: {@code userId}, {@code firstName}, {@code lastName}, {@code password},
 * {@code userType}.</p>
 */
@Component
public class UserUpdateScreenHandler implements ScreenHandler {

    private final UserAdminService service;

    public UserUpdateScreenHandler(UserAdminService service) {
        this.service = service;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.USER_UPDATE;
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

        boolean hasEdits = anyNotBlank(request.field("firstName"), request.field("lastName"),
                request.field("password"), request.field("userType"));
        if (!hasEdits) {
            return service.find(userId)
                    .map(u -> ScreenResult.stay("Update the fields then press ENTER to save ...", u))
                    .orElse(ScreenResult.stay(UserAdminMessages.USER_ID_NOT_FOUND, null));
        }

        try {
            UpdateUserRequest req = new UpdateUserRequest(
                    request.field("firstName"),
                    request.field("lastName"),
                    request.field("password"),
                    request.field("userType"));
            UserResponse result = service.update(userId, req);
            return ScreenResult.stay(result.message(), result);
        } catch (UserAdminException e) {
            return ScreenResult.stay(e.getMessage(), null);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean anyNotBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return true;
            }
        }
        return false;
    }
}
