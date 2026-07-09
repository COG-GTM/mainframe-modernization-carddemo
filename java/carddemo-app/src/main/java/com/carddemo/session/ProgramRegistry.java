package com.carddemo.session;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * Lookup facade over {@link CardDemoProgram}: resolves programs by CICS {@code TRANSID} or
 * by COBOL program name, and picks the correct landing menu for a signed-on user type
 * (the {@code COSGN00C} "admin vs main menu" routing decision).
 */
@Component
public class ProgramRegistry {

    private final Map<String, CardDemoProgram> byTranId = new java.util.HashMap<>();
    private final Map<String, CardDemoProgram> byProgramName = new java.util.HashMap<>();
    private final Map<UserType, CardDemoProgram> landingMenu = new EnumMap<>(UserType.class);

    public ProgramRegistry() {
        for (CardDemoProgram p : CardDemoProgram.values()) {
            byTranId.put(normalise(p.tranId()), p);
            byProgramName.put(normalise(p.programName()), p);
        }
        landingMenu.put(UserType.ADMIN, CardDemoProgram.ADMIN_MENU);
        landingMenu.put(UserType.USER, CardDemoProgram.MAIN_MENU);
    }

    /** Resolve by four-character TRANSID (case-insensitive), if known. */
    public Optional<CardDemoProgram> byTranId(String tranId) {
        return Optional.ofNullable(tranId).map(this::normalise).map(byTranId::get);
    }

    /** Resolve by COBOL program name (case-insensitive), if known. */
    public Optional<CardDemoProgram> byProgramName(String programName) {
        return Optional.ofNullable(programName).map(this::normalise).map(byProgramName::get);
    }

    /**
     * The menu a user lands on after sign-on — mirrors {@code COSGN00C}:
     * {@code IF CDEMO-USRTYP-ADMIN XCTL COADM01C ELSE XCTL COMEN01C}.
     */
    public CardDemoProgram menuFor(UserType userType) {
        CardDemoProgram menu = landingMenu.get(userType);
        return menu != null ? menu : CardDemoProgram.MAIN_MENU;
    }

    private String normalise(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}
