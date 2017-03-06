/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.internal.exception;

/**
 * @author Alexander Maret-Huskinson - Initial contribution
 */
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
