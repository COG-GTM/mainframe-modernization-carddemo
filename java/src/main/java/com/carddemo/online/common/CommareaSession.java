package com.carddemo.online.common;

import com.carddemo.model.dto.CardDemoCommarea;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces the CICS pseudo-conversational COMMAREA plumbing
 * ({@code EXEC CICS RETURN TRANSID(...) COMMAREA(CARDDEMO-COMMAREA)}) of the online programs.
 *
 * <p>{@code EIBCALEN = 0} — the first entry into a transaction with no COMMAREA — corresponds to
 * an HTTP session that does not yet hold a {@link CardDemoCommarea}.
 */
public final class CommareaSession {

    private CommareaSession() {
    }

    /** Returns the COMMAREA held in the session, or {@code null} when EIBCALEN would be zero. */
    public static CardDemoCommarea find(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute(CardDemoCommarea.SESSION_KEY);
    }

    /** Returns the COMMAREA held in the session, creating an empty one when absent. */
    public static CardDemoCommarea require(HttpSession session) {
        CardDemoCommarea commarea = find(session);
        if (commarea == null) {
            commarea = new CardDemoCommarea();
            store(session, commarea);
        }
        return commarea;
    }

    public static void store(HttpSession session, CardDemoCommarea commarea) {
        session.setAttribute(CardDemoCommarea.SESSION_KEY, commarea);
    }

    /** {@code IF CDEMO-TO-PROGRAM = LOW-VALUES OR SPACES} in RETURN-TO-PREV-SCREEN. */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
