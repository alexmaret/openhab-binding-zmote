package org.openhab.binding.zmote.internal.discovery;

import org.openhab.binding.zmote.internal.model.ZMoteDevice;

public interface IZMoteDiscoveryService {

    void addListener(IDiscoveryListener listener);

    ZMoteDevice getDevice(String uuid);

    boolean isOnline(String uuid);

    void removeListener(IDiscoveryListener listener);

    void startDiscovery();

    void stopDiscovery();
}
