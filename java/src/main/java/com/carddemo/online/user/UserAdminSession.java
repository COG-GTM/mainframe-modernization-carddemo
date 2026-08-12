package com.carddemo.online.user;

import jakarta.servlet.http.HttpSession;

/** Holds the CDEMO-CU00/CU02/CU03 COMMAREA extension ({@link UserAdminState}) in the session. */
final class UserAdminSession {

    private UserAdminSession() {
    }

    static UserAdminState require(HttpSession session) {
        UserAdminState state = (UserAdminState) session.getAttribute(UserAdminState.SESSION_KEY);
        if (state == null) {
            state = new UserAdminState();
            session.setAttribute(UserAdminState.SESSION_KEY, state);
        }
        return state;
    }

    static void store(HttpSession session, UserAdminState state) {
        session.setAttribute(UserAdminState.SESSION_KEY, state);
    }
}
