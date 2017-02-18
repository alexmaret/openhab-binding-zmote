package org.openhab.binding.zmote.internal.util;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class NetworkUtils {

    private NetworkUtils() {
        // hidden constructor
    }

    /**
     * Gets every Address from each Interface except the loopback
     *
     * @return The collected addresses
     */
    public static Set<InterfaceAddress> getInterfaceAddresses() {
        final Set<InterfaceAddress> interfaceAddresses = new HashSet<InterfaceAddress>();

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                final NetworkInterface networkInterface = en.nextElement();

                if (networkInterface.isLoopback()) {
                    continue;
                }

                for (final InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    interfaceAddresses.add(interfaceAddress);
                }
            }
        } catch (SocketException e) {
        }

        return interfaceAddresses;
    }

}
