package com.carddemo.web.useradmin;

import com.carddemo.service.useradmin.UserAdminException;
import com.carddemo.service.useradmin.UserAdminService;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.web.useradmin.dto.CreateUserRequest;
import com.carddemo.web.useradmin.dto.UserResponse;
import org.springframework.stereotype.Component;

/**
 * CS-3 screen handler for {@code COUSR01C} (Add User). On first entry it presents an empty
 * form (COBOL {@code IF NOT CDEMO-PGM-REENTER ... SEND}); a subsequent ENTER submits the
 * fields to {@link UserAdminService#add} (validation + duplicate handling), and PF4 clears.
 * Validation/duplicate failures surface the verbatim {@code COUSR01C} message.
 *
 * <p>Request fields: {@code userId}, {@code firstName}, {@code lastName}, {@code password},
 * {@code userType}.</p>
 */
@Component
public class UserAddScreenHandler implements ScreenHandler {

    private final UserAdminService service;

    public UserAddScreenHandler(UserAdminService service) {
        this.service = service;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.USER_ADD;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        if (commarea.isEnter()) {
            return ScreenResult.stay();
        }
        if (request.pfKey() == com.carddemo.session.PfKey.PF4) {
            return ScreenResult.stay();
        }
        try {
            CreateUserRequest req = new CreateUserRequest(
                    request.field("userId"),
                    request.field("firstName"),
                    request.field("lastName"),
                    request.field("password"),
                    request.field("userType"));
            UserResponse result = service.add(req);
            return ScreenResult.stay(result.message(), result);
        } catch (UserAdminException e) {
            return ScreenResult.stay(e.getMessage(), null);
        }
    }
}
