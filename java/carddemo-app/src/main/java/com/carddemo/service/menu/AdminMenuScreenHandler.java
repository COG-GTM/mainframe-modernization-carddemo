package com.carddemo.service.menu;

import org.springframework.stereotype.Component;

import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.NavigationService;
import com.carddemo.session.UserType;
import com.carddemo.web.menu.dto.MenuOptionDto;

/**
 * {@link com.carddemo.session.ScreenHandler} for the admin menu — the migration of
 * {@code app/cbl/COADM01C.cbl} (TRANSID {@code CA00}, BMS {@code COADM01}, options from
 * {@code app/cpy/COADM02Y.cpy}).
 *
 * <p>The admin menu itself is only reachable by an admin (the framework enforces
 * {@code CardDemoProgram.ADMIN_MENU} access), and {@code COADM01C} has no per-option
 * admin-only check, so {@link #isAdminOnlyDenied} is disabled here — every listed option
 * transfers control directly.</p>
 */
@Component
public class AdminMenuScreenHandler extends MenuScreenHandler {

    public AdminMenuScreenHandler(NavigationService navigation, MenuCatalog catalog) {
        super(navigation, catalog);
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.ADMIN_MENU;
    }

    /** {@code COADM01C} has no per-option access guard — never deny here. */
    @Override
    protected boolean isAdminOnlyDenied(MenuOptionDto option, UserType userType) {
        return false;
    }
}
