package com.carddemo.session;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSession;

/**
 * Stores the {@link CardDemoCommarea} as an {@link HttpSession} attribute so it persists
 * across REST calls the way the CICS COMMAREA persists across pseudo-conversational turns
 * ({@code EXEC CICS RETURN TRANSID(..) COMMAREA(..)}). This is the Java analogue of the
 * COMMAREA being re-presented on each terminal interaction.
 */
@Component
public class CommareaSessionStore {

    static final String ATTRIBUTE = "com.carddemo.session.CARDDEMO_COMMAREA";

    /** Return the session's commarea, creating and storing a fresh one if none exists. */
    public CardDemoCommarea getOrCreate(HttpSession session) {
        CardDemoCommarea commarea = get(session);
        if (commarea == null) {
            commarea = new CardDemoCommarea();
            save(session, commarea);
        }
        return commarea;
    }

    /** Return the session's commarea, or {@code null} if none has been stored yet. */
    public CardDemoCommarea get(HttpSession session) {
        Object value = session.getAttribute(ATTRIBUTE);
        return value instanceof CardDemoCommarea commarea ? commarea : null;
    }

    public void save(HttpSession session, CardDemoCommarea commarea) {
        session.setAttribute(ATTRIBUTE, commarea);
    }

    /** Drop the commarea (analogous to signing off / ending the CICS task). */
    public void clear(HttpSession session) {
        session.removeAttribute(ATTRIBUTE);
    }
}
