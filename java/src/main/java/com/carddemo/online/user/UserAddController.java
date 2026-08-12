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

/** COBOL program: COUSR01C (transaction CU01), BMS mapset COUSR01. */
@RestController
@RequestMapping("/api/users/add")
public class UserAddController {

    private final UserAddService userAddService;

    public UserAddController(UserAddService userAddService) {
        this.userAddService = userAddService;
    }

    @GetMapping
    public UserDetailResponse addScreen(HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        UserDetailResponse response = userAddService.initialScreen(commarea);
        CommareaSession.store(session, commarea);
        return response;
    }

    /** DFHENTER. */
    @PostMapping("/enter")
    public UserDetailResponse enter(@RequestBody UserFormRequest request, HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        return userAddService.processEnterKey(request);
    }

    /** DFHPF4: clear the screen. */
    @PostMapping("/pf4")
    public UserDetailResponse pf4(HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        return userAddService.processPf4Key();
    }

    /** DFHPF3. */
    @PostMapping("/pf3")
    public UserDetailResponse pf3(HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        UserDetailResponse response = userAddService.processPf3Key(commarea);
        CommareaSession.store(session, commarea);
        return response;
    }

    /** {@code WHEN OTHER}. */
    @PostMapping("/other-key")
    public UserDetailResponse otherKey(@RequestBody UserFormRequest request, HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        return userAddService.processOtherKey(request);
    }

    /** COBOL paragraph: RETURN-TO-SIGNON-SCREEN. */
    private UserDetailResponse returnToSignonScreen() {
        return new UserDetailResponse(
                ScreenHeader.of(OnlinePrograms.TRANID_USER_ADD, OnlinePrograms.USER_ADD),
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
