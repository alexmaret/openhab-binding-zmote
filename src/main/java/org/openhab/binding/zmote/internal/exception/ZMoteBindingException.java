package org.openhab.binding.zmote.internal.exception;

/**
 * The base exception for all ZMote related errors.
 */
public class ZMoteBindingException extends RuntimeException {

    private static final long serialVersionUID = -436906833815967484L;

    public ZMoteBindingException() {
        super();
    }

    public ZMoteBindingException(final String message) {
        super(message);
    }

    public ZMoteBindingException(final Throwable cause) {
        super(cause);
    }

    public ZMoteBindingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
