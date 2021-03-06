/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.internal.service;

import org.openhab.binding.zmote.internal.model.ZMoteConfig;

/**
 * @author Alexander Maret-Huskinson - Initial contribution
 */
public interface IZMoteService {

    /**
     * Checks if the device is online.
     *
     * @param config The device configuration.
     *
     * @return True if the device is online, else false.
     *
     * @throws ConfigurationException if the configuration is invalid.
     */
    boolean checkOnline(ZMoteConfig config);

    /**
     * Sends the given IR code to the device.
     *
     * @param config The device configuration.
     * @param code The code to send.
     *
     * @return True if the code was sent, else false.
     *
     * @throws ConfigurationException if the configuration is invalid.
     * @throws CommunicationException if the communication with the device fails.
     */
    boolean sendCode(ZMoteConfig config, String code);

    /**
     * Sends the given IR code to the device.
     *
     * @param config The device configuration.
     * @param code The code to send.
     * @param repeat The number of times the code will be sent.
     *
     * @return True if the code was sent, else false.
     *
     * @throws ConfigurationException if the configuration is invalid.
     * @throws CommunicationException if the communication with the device fails.
     */
    boolean sendCode(ZMoteConfig config, String code, int repeat);

    /**
     * Sends the IR code of the given button key to the device.
     *
     * @param config The device configuration.
     * @param button The button key to send.
     *
     * @return True if the code was sent, else false.
     *
     * @throws ConfigurationException if the configuration is invalid.
     * @throws CommunicationException if the communication with the device fails.
     */
    boolean sendKey(ZMoteConfig config, String button);

    /**
     * Sends the IR code of the given button key to the device.
     *
     * @param config The device configuration.
     * @param button The button key to send.
     * @param repeat The number of times the code will be sent.
     *
     * @return True if the code was sent, else false.
     *
     * @throws ConfigurationException if the configuration is invalid.
     * @throws CommunicationException if the communication with the device fails.
     */
    boolean sendKey(ZMoteConfig config, String button, int repeat);

    /**
     * Registers a device at the service. This will cache its configuration file
     * and prepare a client.
     *
     * @param config The device to register.
     */
    void registerConfiguration(ZMoteConfig config);

    /**
     * Unregisters the given configuration which will free its resources.
     *
     * @param config The device to unregister.
     */
    void unregisterConfiguration(ZMoteConfig config);
}
