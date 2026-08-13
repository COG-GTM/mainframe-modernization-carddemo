package com.carddemo.online.account;

import com.carddemo.model.dto.CardDemoCommarea;
import jakarta.servlet.http.HttpSession;

/**
 * COBOL copybook: COCOM01Y (CARDDEMO-COMMAREA) — pseudo-conversational state handling
 * shared by the online account (COACTVWC, COACTUPC) and card (COCRDLIC, COCRDSLC,
 * COCRDUPC) transactions. In CICS the COMMAREA travels on the EXEC CICS RETURN;
 * here it lives in the HTTP session under {@link CardDemoCommarea#SESSION_KEY}.
 */
public final class OnlineSession {

    private OnlineSession() {
    }

    /** Returns the COMMAREA held in the session, creating an empty one when EIBCALEN = 0. */
    public static CardDemoCommarea commarea(HttpSession session) {
        Object stored = session.getAttribute(CardDemoCommarea.SESSION_KEY);
        if (stored instanceof CardDemoCommarea) {
            return (CardDemoCommarea) stored;
        }
        return new CardDemoCommarea();
    }

    public static void store(HttpSession session, CardDemoCommarea commarea) {
        session.setAttribute(CardDemoCommarea.SESSION_KEY, commarea);
    }
}
