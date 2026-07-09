package com.carddemo.session;

import org.springframework.stereotype.Service;

/**
 * Reproduces the CICS pseudo-conversational transfer-of-control model on top of the
 * {@link CardDemoCommarea}.
 *
 * <p>Legacy mechanics being mirrored:</p>
 * <ul>
 *   <li>{@code XCTL PROGRAM(target) COMMAREA(..)} — hand control to another program. The
 *       caller sets {@code CDEMO-FROM-*} to itself, {@code CDEMO-TO-*} to the target and
 *       {@code MOVE ZEROS TO CDEMO-PGM-CONTEXT} so the target sees a first entry (ENTER).
 *       See {@link #transferControl}.</li>
 *   <li>{@code EXEC CICS RETURN TRANSID(self) COMMAREA(..)} — end the turn but stay on the
 *       same program; on first entry the program flips {@code CDEMO-PGM-CONTEXT} to
 *       {@code CDEMO-PGM-REENTER} so the next interaction is processed as input. See
 *       {@link #beginTurn} / {@link #stay}.</li>
 *   <li>{@code PF3} returns to the calling program (or the sign-on screen when there is no
 *       caller). See {@link #back}.</li>
 * </ul>
 *
 * <p>The service is stateless; the state lives entirely in the {@link CardDemoCommarea}
 * passed in (which callers keep in the HTTP session via {@link CommareaSessionStore}).</p>
 */
@Service
public class NavigationService {

    private final ProgramRegistry registry;

    public NavigationService(ProgramRegistry registry) {
        this.registry = registry;
    }

    public ProgramRegistry registry() {
        return registry;
    }

    /**
     * Initialise the commarea for a freshly signed-on user and route to the landing menu
     * (admin vs main) — the Java analogue of {@code COSGN00C} moving the user id/type into
     * the commarea and issuing {@code XCTL} to {@code COADM01C} or {@code COMEN01C}.
     *
     * @return the menu program control was transferred to.
     */
    public CardDemoProgram signon(CardDemoCommarea commarea, String userId, UserType userType) {
        CardDemoCommarea.GeneralInfo info = commarea.getGeneralInfo();
        info.setUserId(userId);
        info.setUserType(userType == null ? null : String.valueOf(userType.code()));
        CardDemoProgram menu = registry.menuFor(userType);
        transferControl(commarea, CardDemoProgram.SIGNON, menu);
        return menu;
    }

    /**
     * {@code XCTL} equivalent: transfer control from {@code from} to {@code to}. Records the
     * from/to routing in the commarea and resets the context to {@link ProgramContext#ENTER}
     * so the target program initialises itself on this turn.
     */
    public void transferControl(CardDemoCommarea commarea, CardDemoProgram from, CardDemoProgram to) {
        CardDemoCommarea.GeneralInfo info = commarea.getGeneralInfo();
        if (from != null) {
            info.setFromTranId(from.tranId());
            info.setFromProgram(from.programName());
        }
        info.setToTranId(to.tranId());
        info.setToProgram(to.programName());
        info.setPgmContext(ProgramContext.ENTER.code());
    }

    /**
     * Inspect the current turn for a program and, if it is a first entry (ENTER), flip the
     * context to RE-ENTER for the next turn — mirroring the standard
     * {@code IF NOT CDEMO-PGM-REENTER SET CDEMO-PGM-REENTER TO TRUE} preamble.
     *
     * @return {@code true} if this turn is the first entry into the program (state should be
     *         initialised), {@code false} if it is a re-entry (process user input).
     */
    public boolean beginTurn(CardDemoCommarea commarea) {
        boolean firstEntry = commarea.isEnter();
        if (firstEntry) {
            commarea.getGeneralInfo().setPgmContext(ProgramContext.REENTER.code());
        }
        return firstEntry;
    }

    /**
     * {@code RETURN TRANSID(self)} equivalent: remain on the current program for the next
     * turn, marking the context RE-ENTER so context is preserved rather than reset.
     */
    public void stay(CardDemoCommarea commarea) {
        commarea.getGeneralInfo().setPgmContext(ProgramContext.REENTER.code());
    }

    /**
     * {@code PF3} handling: return to the calling program recorded in {@code CDEMO-FROM-*};
     * when there is no caller (or it is unknown) fall back to the sign-on screen, matching
     * the {@code IF CDEMO-TO-PROGRAM = LOW-VALUES OR SPACES MOVE 'COSGN00C'} guard.
     *
     * @return the program control was transferred back to.
     */
    public CardDemoProgram back(CardDemoCommarea commarea) {
        CardDemoProgram current = currentProgram(commarea).orElse(null);
        CardDemoProgram target = registry.byProgramName(commarea.getFromProgram())
            .orElse(CardDemoProgram.SIGNON);
        transferControl(commarea, current, target);
        return target;
    }

    /**
     * Apply a PF key at the framework level. ENTER is a submit (the calling screen handles
     * it); PF3 returns to the caller. Other keys are returned as their logical action for
     * the screen to interpret.
     *
     * @return the program to transfer to when the key implies navigation, otherwise empty.
     */
    public java.util.Optional<CardDemoProgram> applyPfKey(CardDemoCommarea commarea, PfKey pfKey) {
        if (pfKey.action() == CommonAction.BACK) {
            return java.util.Optional.of(back(commarea));
        }
        return java.util.Optional.empty();
    }

    /** The program currently in control per {@code CDEMO-TO-PROGRAM}, if known. */
    public java.util.Optional<CardDemoProgram> currentProgram(CardDemoCommarea commarea) {
        return registry.byProgramName(commarea.getToProgram());
    }
}
