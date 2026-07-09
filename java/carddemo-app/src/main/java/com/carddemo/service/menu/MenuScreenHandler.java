package com.carddemo.service.menu;

import java.util.List;
import java.util.Optional;

import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.NavigationService;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.session.UserType;
import com.carddemo.web.menu.dto.MenuOptionDto;

/**
 * Common menu behaviour shared by {@link MainMenuScreenHandler} ({@code COMEN01C}) and
 * {@link AdminMenuScreenHandler} ({@code COADM01C}), plugged into the CS-3 navigation
 * framework as a {@link ScreenHandler}.
 *
 * <p>Reproduces the online menu program's {@code PROCESS-ENTER-KEY} turn:</p>
 * <ul>
 *   <li>On first entry (ENTER) the menu is (re-)displayed — the option list is returned in
 *       the {@link ScreenResult} model, mirroring {@code BUILD-MENU-OPTIONS} +
 *       {@code SEND-MENU-SCREEN}.</li>
 *   <li>On a submit (RE-ENTER) the {@code option} field is parsed exactly as the COBOL
 *       {@code WS-OPTION-X}/{@code WS-OPTION} handling (blank → 0; non-numeric or
 *       out-of-range → "Please enter a valid option number..."), then control is transferred
 *       ({@code XCTL}) to the chosen option's program.</li>
 * </ul>
 *
 * <p>PF3 (BACK) is handled generically by the framework's {@code NavigationController} — it
 * routes back to the caller recorded in {@code CDEMO-FROM-PROGRAM} (the sign-on screen for a
 * freshly-entered menu), matching the {@code WHEN DFHPF3} branch of the menu programs.</p>
 */
public abstract class MenuScreenHandler implements ScreenHandler {

    /** BMS input field holding the selected option ({@code OPTION} on the menu maps). */
    static final String OPTION_FIELD = "option";

    static final String INVALID_OPTION_MESSAGE = "Please enter a valid option number...";
    static final String ADMIN_ONLY_MESSAGE = "No access - Admin Only option...";

    private final NavigationService navigation;
    private final MenuCatalog catalog;

    protected MenuScreenHandler(NavigationService navigation, MenuCatalog catalog) {
        this.navigation = navigation;
        this.catalog = catalog;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        UserType userType = commarea.getUserType();
        List<MenuOptionDto> visible = catalog.visibleOptions(program(), userType);

        // ENTER = first entry into the menu: (re-)display it. RE-ENTER = process the option.
        if (navigation.beginTurn(commarea)) {
            return ScreenResult.stay(null, visible);
        }

        Optional<Integer> parsed = parseOption(request.field(OPTION_FIELD));
        if (parsed.isEmpty()) {
            return ScreenResult.stay(INVALID_OPTION_MESSAGE, visible);
        }

        Optional<MenuOptionDto> selected = catalog.optionByNumber(program(), parsed.get());
        if (selected.isEmpty()) {
            return ScreenResult.stay(INVALID_OPTION_MESSAGE, visible);
        }

        MenuOptionDto option = selected.get();
        if (isAdminOnlyDenied(option, userType)) {
            return ScreenResult.stay(ADMIN_ONLY_MESSAGE, visible);
        }

        CardDemoProgram target = navigation.registry().byTranId(option.tranId())
            .orElseThrow(() -> new IllegalStateException(
                "Menu option references unknown TRANSID: " + option.tranId()));
        return ScreenResult.transferTo(target);
    }

    /**
     * Whether {@code COMEN01C}'s "No access - Admin Only option..." guard applies. Overridden
     * by the admin menu, whose {@code COADM01C} has no such per-option check.
     */
    protected boolean isAdminOnlyDenied(MenuOptionDto option, UserType userType) {
        return option.adminOnly() && userType != UserType.ADMIN;
    }

    /**
     * Parse the entered option the way {@code PROCESS-ENTER-KEY} does: an all-blank entry
     * collapses to {@code 0} (invalid), and a non-numeric entry is rejected. Returns empty
     * for anything that is not a positive whole number ({@code WS-OPTION = ZEROS} or
     * {@code IS NOT NUMERIC}).
     */
    private Optional<Integer> parseOption(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty() || !trimmed.chars().allMatch(Character::isDigit)) {
            return Optional.empty();
        }
        int value = Integer.parseInt(trimmed);
        return value == 0 ? Optional.empty() : Optional.of(value);
    }
}
