package org.openhab.binding.zmote.internal.exception;

public class ConfigurationException extends ZMoteBindingException {

    private static final long serialVersionUID = -9094749163639157572L;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(final String message) {
        super(message);
    }

    public ConfigurationException(final Throwable cause) {
        super(cause);
    }

    public ConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
