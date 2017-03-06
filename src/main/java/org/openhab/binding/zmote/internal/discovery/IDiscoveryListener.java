/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.internal.discovery;

import org.openhab.binding.zmote.internal.model.ZMoteDevice;

/**
 * @author Alexander Maret-Huskinson - Initial contribution
 */
public interface IDiscoveryListener {

    /**
     * Called whenever this device was discovered, either by an active scan
     * or by listening passively for device broadcasts on the local network.
     *
     * @param device The device which was discovered.
     */
    void deviceDiscovered(ZMoteDevice device);
}
