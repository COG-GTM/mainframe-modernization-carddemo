package com.carddemo.service.menu;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.ProgramRegistry;
import com.carddemo.session.UserType;
import com.carddemo.web.menu.dto.MenuOptionDto;

/**
 * Source of the CardDemo online-menu option lists, ported field-for-field from the option
 * copybooks:
 *
 * <ul>
 *   <li>{@code app/cpy/COMEN02Y.cpy} — {@code CARDDEMO-MAIN-MENU-OPTIONS} (main menu,
 *       {@code COMEN01C}).</li>
 *   <li>{@code app/cpy/COADM02Y.cpy} — {@code CARDDEMO-ADMIN-MENU-OPTIONS} (admin menu,
 *       {@code COADM01C}).</li>
 * </ul>
 *
 * <p>Each option's TRANSID is resolved from the {@link ProgramRegistry} keyed by the
 * copybook {@code CDEMO-*-OPT-PGMNAME}, so this catalog never invents identifiers — it
 * reuses the shared program registry established by CS-3.</p>
 */
@Component
public class MenuCatalog {

    private static final String INVALID_PROGRAM =
        "Menu option references unknown program: ";

    private final Map<CardDemoProgram, List<MenuOptionDto>> optionsByMenu =
        new EnumMap<>(CardDemoProgram.class);

    public MenuCatalog(ProgramRegistry registry) {
        optionsByMenu.put(CardDemoProgram.MAIN_MENU, build(registry, mainMenuRawOptions()));
        optionsByMenu.put(CardDemoProgram.ADMIN_MENU, build(registry, adminMenuRawOptions()));
    }

    /** All options defined for {@code menu}, in copybook order (unfiltered). */
    public List<MenuOptionDto> options(CardDemoProgram menu) {
        return optionsByMenu.getOrDefault(menu, List.of());
    }

    /**
     * Options of {@code menu} visible to {@code userType} — admin-only options
     * ({@code CDEMO-MENU-OPT-USRTYPE = 'A'}) are hidden from a regular user, matching the
     * intent of the "No access - Admin Only option..." guard in {@code COMEN01C}.
     */
    public List<MenuOptionDto> visibleOptions(CardDemoProgram menu, UserType userType) {
        boolean admin = userType == UserType.ADMIN;
        List<MenuOptionDto> visible = new ArrayList<>();
        for (MenuOptionDto option : options(menu)) {
            if (admin || !option.adminOnly()) {
                visible.add(option);
            }
        }
        return visible;
    }

    /**
     * Look up an option by its one-based number ({@code CDEMO-*-OPT-NUM}) within the full
     * copybook list for {@code menu}. Numbers outside {@code 1..count} yield empty — the
     * "Please enter a valid option number..." case in {@code PROCESS-ENTER-KEY}.
     */
    public Optional<MenuOptionDto> optionByNumber(CardDemoProgram menu, int number) {
        List<MenuOptionDto> options = options(menu);
        if (number < 1 || number > options.size()) {
            return Optional.empty();
        }
        return Optional.of(options.get(number - 1));
    }

    private List<MenuOptionDto> build(ProgramRegistry registry, List<RawOption> raw) {
        List<MenuOptionDto> options = new ArrayList<>(raw.size());
        int number = 1;
        for (RawOption option : raw) {
            CardDemoProgram program = registry.byProgramName(option.programName())
                .orElseThrow(() -> new IllegalStateException(
                    INVALID_PROGRAM + option.programName()));
            options.add(new MenuOptionDto(
                String.format("%02d", number),
                option.name(),
                program.tranId(),
                program.programName(),
                option.adminOnly()));
            number++;
        }
        return List.copyOf(options);
    }

    // ---- Copybook data ---------------------------------------------------------------

    /** {@code COMEN02Y} — main-menu options ({@code CDEMO-MENU-OPT-USRTYPE} all {@code 'U'}). */
    private static List<RawOption> mainMenuRawOptions() {
        return List.of(
            new RawOption("Account View", "COACTVWC", false),
            new RawOption("Account Update", "COACTUPC", false),
            new RawOption("Credit Card List", "COCRDLIC", false),
            new RawOption("Credit Card View", "COCRDSLC", false),
            new RawOption("Credit Card Update", "COCRDUPC", false),
            new RawOption("Transaction List", "COTRN00C", false),
            new RawOption("Transaction View", "COTRN01C", false),
            new RawOption("Transaction Add", "COTRN02C", false),
            new RawOption("Transaction Reports", "CORPT00C", false),
            new RawOption("Bill Payment", "COBIL00C", false));
    }

    /** {@code COADM02Y} — admin-menu options (all admin-only). */
    private static List<RawOption> adminMenuRawOptions() {
        return List.of(
            new RawOption("User List (Security)", "COUSR00C", true),
            new RawOption("User Add (Security)", "COUSR01C", true),
            new RawOption("User Update (Security)", "COUSR02C", true),
            new RawOption("User Delete (Security)", "COUSR03C", true));
    }

    /** A copybook option entry before TRANSID resolution. */
    private record RawOption(String name, String programName, boolean adminOnly) {
    }
}
