package com.carddemo.online.user;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.online.common.CommareaSession;
import com.carddemo.online.common.OnlinePrograms;
import com.carddemo.online.common.ScreenHeader;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** COBOL program: COUSR03C (transaction CU03), BMS mapset COUSR03. */
@RestController
@RequestMapping("/api/users/delete")
public class UserDeleteController {

    private final UserDeleteService userDeleteService;

    public UserDeleteController(UserDeleteService userDeleteService) {
        this.userDeleteService = userDeleteService;
    }

    @GetMapping
    public UserDetailResponse deleteScreen(HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        UserDetailResponse response =
                userDeleteService.initialScreen(commarea, UserAdminSession.require(session));
        CommareaSession.store(session, commarea);
        return response;
    }

    /** DFHENTER: read the user. */
    @PostMapping("/enter")
    public UserDetailResponse enter(@RequestBody UserFormRequest request, HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        return userDeleteService.processEnterKey(request);
    }

    /** DFHPF5: delete the user. */
    @PostMapping("/pf5")
    public UserDetailResponse pf5(@RequestBody UserFormRequest request, HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        return userDeleteService.processPf5Key(request);
    }

    /** DFHPF4: clear the screen. */
    @PostMapping("/pf4")
    public UserDetailResponse pf4(HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        return userDeleteService.processPf4Key();
    }

    /** DFHPF3. */
    @PostMapping("/pf3")
    public UserDetailResponse pf3(HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        UserDetailResponse response = userDeleteService.processPf3Key(commarea);
        CommareaSession.store(session, commarea);
        return response;
    }

    /** DFHPF12. */
    @PostMapping("/pf12")
    public UserDetailResponse pf12(HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        UserDetailResponse response = userDeleteService.processPf12Key(commarea);
        CommareaSession.store(session, commarea);
        return response;
    }

    /** {@code WHEN OTHER}. */
    @PostMapping("/other-key")
    public UserDetailResponse otherKey(@RequestBody UserFormRequest request, HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        return userDeleteService.processOtherKey(request);
    }

    /** COBOL paragraph: RETURN-TO-SIGNON-SCREEN. */
    private UserDetailResponse returnToSignonScreen() {
        return new UserDetailResponse(
                ScreenHeader.of(OnlinePrograms.TRANID_USER_DELETE, OnlinePrograms.USER_DELETE),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                OnlinePrograms.SIGNON,
                OnlinePrograms.TRANID_SIGNON);
    }
}
