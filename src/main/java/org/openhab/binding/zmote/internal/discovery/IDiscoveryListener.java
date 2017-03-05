package org.openhab.binding.zmote.internal.discovery;

import org.openhab.binding.zmote.internal.model.ZMoteDevice;

public interface IDiscoveryListener {

    /**
     * Called whenever this device was discovered, either by an active scan
     * or by listening passively for device broadcasts on the local network.
     *
     * @param device The device which was discovered.
     */
    void deviceDiscovered(ZMoteDevice device);
}
