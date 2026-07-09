package com.carddemo.service.menu;

import org.springframework.stereotype.Component;

import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.NavigationService;

/**
 * {@link com.carddemo.session.ScreenHandler} for the regular-user main menu — the migration
 * of {@code app/cbl/COMEN01C.cbl} (TRANSID {@code CM00}, BMS {@code COMEN01}, options from
 * {@code app/cpy/COMEN02Y.cpy}).
 *
 * <p>Retains the COBOL admin-only guard ({@code IF CDEMO-USRTYP-USER AND
 * CDEMO-MENU-OPT-USRTYPE(WS-OPTION) = 'A'} → "No access - Admin Only option...") via the
 * inherited {@link MenuScreenHandler#isAdminOnlyDenied} default.</p>
 */
@Component
public class MainMenuScreenHandler extends MenuScreenHandler {

    public MainMenuScreenHandler(NavigationService navigation, MenuCatalog catalog) {
        super(navigation, catalog);
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.MAIN_MENU;
    }
}
