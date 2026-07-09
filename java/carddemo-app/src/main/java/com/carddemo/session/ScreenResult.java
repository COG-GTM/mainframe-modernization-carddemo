package com.carddemo.session;

/**
 * Outcome of a {@link ScreenHandler} turn, telling the {@link NavigationService} what
 * transfer-of-control to perform next. Analogous to what an online program does at the end
 * of its logic: {@code RETURN TRANSID(self)} to stay, {@code XCTL} to move on, or route
 * back on PF3.
 */
public class ScreenResult {

    /** The kind of control transfer the framework should perform after the turn. */
    public enum Type {
        /** Re-display the same screen (RETURN TRANSID self / RE-ENTER). */
        STAY,
        /** Transfer control to another program (XCTL). */
        TRANSFER,
        /** Return to the calling program (PF3). */
        BACK
    }

    private final Type type;
    private final CardDemoProgram target;
    private final String message;
    private final Object model;

    private ScreenResult(Type type, CardDemoProgram target, String message, Object model) {
        this.type = type;
        this.target = target;
        this.message = message;
        this.model = model;
    }

    public static ScreenResult stay() {
        return new ScreenResult(Type.STAY, null, null, null);
    }

    public static ScreenResult stay(String message, Object model) {
        return new ScreenResult(Type.STAY, null, message, model);
    }

    public static ScreenResult transferTo(CardDemoProgram target) {
        return new ScreenResult(Type.TRANSFER, target, null, null);
    }

    public static ScreenResult transferTo(CardDemoProgram target, String message, Object model) {
        return new ScreenResult(Type.TRANSFER, target, message, model);
    }

    public static ScreenResult back() {
        return new ScreenResult(Type.BACK, null, null, null);
    }

    public Type type() {
        return type;
    }

    public CardDemoProgram target() {
        return target;
    }

    public String message() {
        return message;
    }

    public Object model() {
        return model;
    }
}
