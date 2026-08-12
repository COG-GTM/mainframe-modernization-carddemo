package com.carddemo.online.user;

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
 * COBOL program: COUSR00C (transaction CU00), BMS mapset COUSR00.
 *
 * <p>Like the COBOL program this screen performs no user type check of its own: it is reachable
 * only through COADM01C, where COSGN00C sends the CDEMO-USRTYP-ADMIN users.
 */
@RestController
@RequestMapping("/api/users")
public class UserListController {

    private final UserListService userListService;

    public UserListController(UserListService userListService) {
        this.userListService = userListService;
    }

    @GetMapping
    public UserListResponse listScreen(HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        UserAdminState state = UserAdminSession.require(session);
        UserListResponse response = userListService.initialScreen(commarea, state);
        CommareaSession.store(session, commarea);
        UserAdminSession.store(session, state);
        return response;
    }

    /** DFHENTER. */
    @PostMapping("/enter")
    public UserListResponse enter(@RequestBody UserListRequest request, HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        UserAdminState state = UserAdminSession.require(session);
        UserListResponse response = userListService.processEnterKey(request, commarea, state);
        CommareaSession.store(session, commarea);
        UserAdminSession.store(session, state);
        return response;
    }

    /** DFHPF7: page backward. */
    @PostMapping("/pf7")
    public UserListResponse pf7(@RequestBody UserListRequest request, HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        UserAdminState state = UserAdminSession.require(session);
        UserListResponse response = userListService.processPf7Key(request, state);
        UserAdminSession.store(session, state);
        return response;
    }

    /** DFHPF8: page forward. */
    @PostMapping("/pf8")
    public UserListResponse pf8(@RequestBody UserListRequest request, HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        UserAdminState state = UserAdminSession.require(session);
        UserListResponse response = userListService.processPf8Key(request, state);
        UserAdminSession.store(session, state);
        return response;
    }

    /** DFHPF3. */
    @PostMapping("/pf3")
    public UserListResponse pf3(HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.find(session);
        if (commarea == null) {
            return returnToSignonScreen();
        }
        UserAdminState state = UserAdminSession.require(session);
        UserListResponse response = userListService.processPf3Key(commarea, state);
        CommareaSession.store(session, commarea);
        return response;
    }

    /** {@code WHEN OTHER}. */
    @PostMapping("/other-key")
    public UserListResponse otherKey(@RequestBody UserListRequest request, HttpSession session) {
        if (CommareaSession.find(session) == null) {
            return returnToSignonScreen();
        }
        return userListService.processOtherKey(request, UserAdminSession.require(session));
    }

    /** COBOL paragraph: RETURN-TO-SIGNON-SCREEN. */
    private UserListResponse returnToSignonScreen() {
        return new UserListResponse(
                ScreenHeader.of(OnlinePrograms.TRANID_USER_LIST, OnlinePrograms.USER_LIST),
                List.of(),
                0,
                false,
                null,
                OnlinePrograms.SIGNON,
                OnlinePrograms.TRANID_SIGNON);
    }
}
