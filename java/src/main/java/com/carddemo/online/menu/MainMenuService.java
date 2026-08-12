package com.carddemo.online.menu;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.online.common.CommonMessages;
import com.carddemo.online.common.OnlinePrograms;
import com.carddemo.online.common.ScreenHeader;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * COBOL program: COMEN01C — main menu for the regular users (transaction CM00), BMS mapset
 * COMEN01, menu table copybook COMEN02Y, COMMAREA copybook COCOM01Y.
 */
@Service
public class MainMenuService {

    private final List<MenuOption> options;

    public MainMenuService() {
        this(MenuOptions.MAIN);
    }

    MainMenuService(List<MenuOption> options) {
        this.options = options;
    }

    /** First entry with a COMMAREA but {@code NOT CDEMO-PGM-REENTER}: send the empty menu. */
    public MenuResponse menuScreen(CardDemoCommarea commarea) {
        commarea.setProgramContext(1);
        return screen(null, null, null);
    }

    /** COBOL paragraph: PROCESS-ENTER-KEY. */
    public MenuResponse processEnterKey(String optionInput, CardDemoCommarea commarea) {
        Integer option = MenuOptionParser.parse(optionInput);

        if (option == null || option > options.size() || option == 0) {
            return screen(option, "Please enter a valid option number...", null);
        }

        MenuOption selected = options.get(option - 1);
        if (CardDemoCommarea.USER_TYPE_USER.equalsIgnoreCase(commarea.getUserType())
                && "A".equals(selected.userType())) {
            return screen(option, "No access - Admin Only option... ", null);
        }

        if (selected.isImplemented()) {
            commarea.setFromTransactionId(OnlinePrograms.TRANID_MAIN_MENU);
            commarea.setFromProgram(OnlinePrograms.MAIN_MENU);
            commarea.setProgramContext(0);
            commarea.setToProgram(selected.programName());
            commarea.setToTransactionId(OnlinePrograms.transactionIdOf(selected.programName()));
            return screen(option, null, selected.programName());
        }

        return screen(
                option,
                "This option " + selected.nameDelimitedBySpace() + "is coming soon ...",
                null);
    }

    /** DFHPF3: back to the signon program. */
    public MenuResponse processPf3Key(CardDemoCommarea commarea) {
        commarea.setToProgram(OnlinePrograms.SIGNON);
        commarea.setToTransactionId(OnlinePrograms.TRANID_SIGNON);
        return screen(null, null, OnlinePrograms.SIGNON);
    }

    /** {@code WHEN OTHER} of the EVALUATE EIBAID. */
    public MenuResponse processOtherKey() {
        return screen(null, CommonMessages.INVALID_KEY, null);
    }

    private MenuResponse screen(Integer option, String message, String nextProgram) {
        return new MenuResponse(
                ScreenHeader.of(OnlinePrograms.TRANID_MAIN_MENU, OnlinePrograms.MAIN_MENU),
                options.stream().map(MenuOption::displayText).toList(),
                option,
                message,
                nextProgram,
                OnlinePrograms.transactionIdOf(nextProgram));
    }
}
