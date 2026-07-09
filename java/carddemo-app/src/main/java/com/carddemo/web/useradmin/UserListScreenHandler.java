package com.carddemo.web.useradmin;

import com.carddemo.service.useradmin.UserAdminService;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.web.useradmin.dto.UserListResponse;
import org.springframework.stereotype.Component;

/**
 * CS-3 screen handler for {@code COUSR00C} (List Users). Presents a page of the USRSEC list
 * and reproduces the {@code COUSR00C} navigation:
 *
 * <ul>
 *   <li>PF7 / PF8 page backward / forward (bounded at page 1).</li>
 *   <li>A row selection {@code 'U'} / {@code 'D'} transfers control to the update
 *       ({@code COUSR02C}) / delete ({@code COUSR03C}) screen; any other flag yields the
 *       verbatim {@code "Invalid selection. Valid values are U and D"} message.</li>
 * </ul>
 *
 * <p>Request fields: {@code startUserId} (browse start key), {@code page} (1-based),
 * {@code action} ({@code U}/{@code D} selection). The page model is a {@link UserListResponse}.</p>
 */
@Component
public class UserListScreenHandler implements ScreenHandler {

    private final UserAdminService service;

    public UserListScreenHandler(UserAdminService service) {
        this.service = service;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.USER_LIST;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        String action = request.field("action");
        if (action != null && !action.isBlank()) {
            switch (action.trim().toUpperCase()) {
                case "U":
                    return ScreenResult.transferTo(CardDemoProgram.USER_UPDATE);
                case "D":
                    return ScreenResult.transferTo(CardDemoProgram.USER_DELETE);
                default:
                    return ScreenResult.stay("Invalid selection. Valid values are U and D", null);
            }
        }

        int page = parsePage(request.field("page"));
        switch (request.pfKey()) {
            case PF7 -> page = Math.max(1, page - 1);
            case PF8 -> page = page + 1;
            default -> { }
        }
        UserListResponse model = service.list(request.field("startUserId"), page,
                UserAdminService.PAGE_SIZE);
        return ScreenResult.stay(null, model);
    }

    private static int parsePage(String value) {
        if (value == null || value.isBlank()) {
            return 1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
