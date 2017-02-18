package org.openhab.binding.zmote.internal.exception;

public class DeviceBusyException extends CommunicationException {

    private static final long serialVersionUID = 3606658778708754552L;

    public DeviceBusyException() {
        super();
    }

    public DeviceBusyException(final String message) {
        super(message);
    }

    public DeviceBusyException(final Throwable cause) {
        super(cause);
    }

    public DeviceBusyException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
