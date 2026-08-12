package com.carddemo.online.menu;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.online.common.CommareaSession;
import com.carddemo.online.common.OnlinePrograms;
import com.carddemo.online.common.ScreenHeader;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * COBOL program: COMEN01C (transaction CM00), BMS mapset COMEN01.
 *
 * <p>A request without a COMMAREA in the session is the {@code IF EIBCALEN = 0} case and answers
 * with a transfer back to COSGN00C.
 */
@RestController
@RequestMapping("/api/menu")
public class MainMenuController {

    private final MainMenuService mainMenuService;

    public MainMenuController(MainMenuService mainMenuService) {
        this.mainMenuService = mainMenuService;
    }

    @GetMapping
    public MenuResponse menuScreen(HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        MenuResponse response = mainMenuService.menuScreen(commarea);
        CommareaSession.store(session, commarea);
        return response;
    }

    /** DFHENTER. */
    @PostMapping("/enter")
    public MenuResponse enter(@RequestBody MenuSelectionRequest request, HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        MenuResponse response = mainMenuService.processEnterKey(request.option(), commarea);
        CommareaSession.store(session, commarea);
        return response;
    }

    /** DFHPF3. */
    @PostMapping("/pf3")
    public MenuResponse pf3(HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        MenuResponse response = mainMenuService.processPf3Key(commarea);
        CommareaSession.store(session, commarea);
        return response;
    }

    /** {@code WHEN OTHER}. */
    @PostMapping("/other-key")
    public MenuResponse otherKey(HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        return mainMenuService.processOtherKey();
    }

    /** COBOL paragraph: RETURN-TO-SIGNON-SCREEN. */
    private MenuResponse returnToSignonScreen() {
        return new MenuResponse(
                ScreenHeader.of(OnlinePrograms.TRANID_MAIN_MENU, OnlinePrograms.MAIN_MENU),
                List.of(),
                null,
                null,
                OnlinePrograms.SIGNON,
                OnlinePrograms.TRANID_SIGNON);
    }
}
