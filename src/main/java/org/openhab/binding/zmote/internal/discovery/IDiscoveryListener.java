package org.openhab.binding.zmote.internal.discovery;

import org.openhab.binding.zmote.internal.model.ZMoteDevice;

public interface IDiscoveryListener {

    void deviceDiscovered(ZMoteDevice device);

    void discoveryFinished();

    void discoveryStarted();
}
