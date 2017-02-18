package org.openhab.binding.zmote.internal.model;

public class IRCode {

    private final String codeMain;
    private final String codeAlternate;

    private IRCodeState codeState = IRCodeState.MAIN;

    public IRCode(final String codeMain) {
        this(codeMain, null);
    }

    public IRCode(final String codeMain, final String codeAlternate) {
        this.codeMain = codeMain;
        this.codeAlternate = codeAlternate;

        if (codeMain == null) {
            throw new IllegalArgumentException("The main IR code cannot be null!");
        }
    }

    public String nextCode() {
        if ((codeState == IRCodeState.ALTERNATE) && (codeAlternate != null)) {
            codeState = IRCodeState.MAIN;
            return codeAlternate;
        }

        if (codeAlternate != null) {
            codeState = IRCodeState.ALTERNATE;
        }

        return codeMain;
    }
}
