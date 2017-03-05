package org.openhab.binding.zmote.internal.discovery;

import org.openhab.binding.zmote.internal.model.ZMoteDevice;

/**
 * A service that listens for ZMote device broadcasts on the local network.
 */
public interface IZMoteDiscoveryService {

    /**
     * Adds a listener that will be called every time a device broadcast has been received.
     */
    void addListener(IDiscoveryListener listener);

    /**
     * Returns the device with the given UUID if it has been discovered within the last minute.
     *
     * @param uuid The UUID of the device to return.
     *
     * @return The device information or null if that device is currently not available.
     */
    ZMoteDevice getDevice(String uuid);

    /**
     * Checks if a device with the given UUID has been seen within the last minute.
     *
     * @param uuid The UUID of the device to check.
     *
     * @return True if the device has been seen within the last minute, else false.
     */
    boolean isOnline(String uuid);

    /**
     * Removes a listener which was previously registered.
     *
     * @param listener The listener to remove.
     */
    void removeListener(IDiscoveryListener listener);

    /**
     * Starts an active scan for ZMote devices.
     */
    void startScan();
}
