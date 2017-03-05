package org.openhab.binding.zmote.internal.service;

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
