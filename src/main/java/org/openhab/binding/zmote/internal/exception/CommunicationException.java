package org.openhab.binding.zmote.internal.exception;

public class CommunicationException extends ZMoteBindingException {

    private static final long serialVersionUID = 5378207128596280049L;

    public CommunicationException() {
        super();
    }

    public CommunicationException(final String message) {
        super(message);
    }

    public CommunicationException(final Throwable cause) {
        super(cause);
    }

    public CommunicationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
