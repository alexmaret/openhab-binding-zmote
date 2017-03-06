/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.internal.service;

/**
 * @author Alexander Maret-Huskinson - Initial contribution
 */
public interface IZMoteClient {

    /**
     * Checks if the device is online and if we are talking to the correct device.
     */
    void check(int timeout);

    /**
     * @return the URL used by this client.
     */
    String getUrl();

    /**
     * Sends the given IR code to the device.
     *
     * @param code The IR code to send.
     * @param timeout The timeout we wait for a response.
     */
    void sendir(String code, int timeout);
}
