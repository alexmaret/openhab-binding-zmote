package org.openhab.binding.zmote.internal.model;

/**
 * An IR code which can handle toggle codes. Toggle codes send different
 * IR signals for the same button, alternating between both signals.
 */
public class IRCode {

    private final String codeMain;
    private final String codeAlternate;

    private IRCodeState nextState = IRCodeState.MAIN;

    /**
     * Creates a normal instance which always returns the given IR code.
     *
     * @param code The code to return when {@link #nextCode()} is called.
     */
    public IRCode(final String code) {
        this(code, null);
    }

    /**
     * Creates a toggle instance which toggles between the given IR codes.
     *
     * @param codeMain The main code to return.
     * @param codeAlternate The alternate code to return.
     */
    public IRCode(final String codeMain, final String codeAlternate) {
        this.codeMain = codeMain;
        this.codeAlternate = codeAlternate;

        if (codeMain == null) {
            throw new IllegalArgumentException("The main IR code cannot be null!");
        }
    }

    /**
     * @return The next code which should be sent.
     */
    public String nextCode() {
        if ((nextState == IRCodeState.ALTERNATE) && (codeAlternate != null)) {
            nextState = IRCodeState.MAIN;
            return codeAlternate;
        }

        if (codeAlternate != null) {
            nextState = IRCodeState.ALTERNATE;
        }

        return codeMain;
    }
}
