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
