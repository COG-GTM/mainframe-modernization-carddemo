package com.carddemo.session;

/**
 * Integration point for WAVE 3 online-session waves (CS-4..CS-9). A wave implements one
 * {@code ScreenHandler} per logical screen/program and registers it as a Spring bean; the
 * {@link ScreenRegistry} collects them and the {@link NavigationController} dispatches to
 * the handler bound to the {@link CardDemoProgram} currently in control.
 *
 * <p>A handler is the Java equivalent of one online COBOL program's turn: it inspects the
 * {@link CardDemoCommarea} (using {@link NavigationService#beginTurn} to tell ENTER from
 * RE-ENTER), reads/writes its selected-entity context on the commarea, and returns a
 * {@link ScreenResult} describing the next transfer-of-control.</p>
 */
public interface ScreenHandler {

    /** The program/transaction this handler implements. */
    CardDemoProgram program();

    /**
     * Handle one turn of the screen.
     *
     * @param request  the PF key and submitted screen fields.
     * @param commarea the session commarea (selected account/card/customer, context flag).
     * @return the control-transfer outcome for the framework to apply.
     */
    ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea);
}
