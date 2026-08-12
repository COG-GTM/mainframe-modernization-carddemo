package com.carddemo.online.menu;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.online.common.CommonMessages;
import com.carddemo.online.common.OnlinePrograms;
import com.carddemo.online.common.ScreenHeader;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * COBOL program: COADM01C — admin menu (transaction CA00), BMS mapset COADM01, menu table
 * copybook COADM02Y, COMMAREA copybook COCOM01Y.
 *
 * <p>COADM01C has no per-option user type column, so it performs no CDEMO-USRTYP-ADMIN check of
 * its own: only COSGN00C routes admins here. Its "coming soon" message also leaves the option name
 * out, because the STRING operand is commented out in the COBOL source.
 */
@Service
public class AdminMenuService {

    private final List<MenuOption> options;

    public AdminMenuService() {
        this(MenuOptions.ADMIN);
    }

    AdminMenuService(List<MenuOption> options) {
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
        if (selected.isImplemented()) {
            commarea.setFromTransactionId(OnlinePrograms.TRANID_ADMIN_MENU);
            commarea.setFromProgram(OnlinePrograms.ADMIN_MENU);
            commarea.setProgramContext(0);
            commarea.setToProgram(selected.programName());
            commarea.setToTransactionId(OnlinePrograms.transactionIdOf(selected.programName()));
            return screen(option, null, selected.programName());
        }

        return screen(option, "This option is coming soon ...", null);
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
                ScreenHeader.of(OnlinePrograms.TRANID_ADMIN_MENU, OnlinePrograms.ADMIN_MENU),
                options.stream().map(MenuOption::displayText).toList(),
                option,
                message,
                nextProgram,
                OnlinePrograms.transactionIdOf(nextProgram));
    }
}
