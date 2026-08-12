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
 * COBOL program: COADM01C (transaction CA00), BMS mapset COADM01.
 */
@RestController
@RequestMapping("/api/admin/menu")
public class AdminMenuController {

    private final AdminMenuService adminMenuService;

    public AdminMenuController(AdminMenuService adminMenuService) {
        this.adminMenuService = adminMenuService;
    }

    @GetMapping
    public MenuResponse menuScreen(HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        MenuResponse response = adminMenuService.menuScreen(commarea);
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
        MenuResponse response = adminMenuService.processEnterKey(request.option(), commarea);
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
        MenuResponse response = adminMenuService.processPf3Key(commarea);
        CommareaSession.store(session, commarea);
        return response;
    }

    /** {@code WHEN OTHER}. */
    @PostMapping("/other-key")
    public MenuResponse otherKey(HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        return adminMenuService.processOtherKey();
    }

    /** COBOL paragraph: RETURN-TO-SIGNON-SCREEN. */
    private MenuResponse returnToSignonScreen() {
        return new MenuResponse(
                ScreenHeader.of(OnlinePrograms.TRANID_ADMIN_MENU, OnlinePrograms.ADMIN_MENU),
                List.of(),
                null,
                null,
                OnlinePrograms.SIGNON,
                OnlinePrograms.TRANID_SIGNON);
    }
}
