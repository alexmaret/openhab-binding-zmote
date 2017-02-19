package org.openhab.binding.zmote.internal.discovery;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.openhab.binding.zmote.ZMoteBindingConstants;
import org.openhab.binding.zmote.internal.model.ZMoteDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZMoteDiscoveryService implements IZMoteDiscoveryService {

    private static final int SOCKET_PORT = 9131;
    private static final int SOCKET_TIMEOUT = 10000;

    private static final String MCAST_GROUP = "::ffff:239.255.250.250";
    private static final int MCAST_REQ_PORT = 9130;
    private static final String MCAST_REQ_PREFIX = "SENDAMXB";
    private static final String MCAST_RES_PREFIX = "AMXB";

    private static final Pattern PATTERN_MAKE = Pattern.compile("<-Make=([^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_MODEL = Pattern.compile("<-Model=([^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_REVISION = Pattern.compile("<-Revision=([^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_TYPE = Pattern.compile("<-Type=([^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_URL = Pattern.compile("<-Config-URL=(http[^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_UUID = Pattern.compile("<-UUID=(CI[^>]+)>", Pattern.CASE_INSENSITIVE);

    private static final String ZMOTE_TYPE = "ZMT2";
    private static final String ZMOTE_MAKE = "zmote.io";

    private final Logger logger = LoggerFactory.getLogger(ZMoteDiscoveryService.class);
    private final Map<String, ZMoteDevice> discoveredDevices = new ConcurrentHashMap<>();
    private final DeviceDiscoveryListener discoveredDevicesListener = new DeviceDiscoveryListener(discoveredDevices);
    private final List<IDiscoveryListener> listeners = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(ZMoteDiscoveryService.class.getName());

    private ScheduledFuture<?> discoveryFuture = null;
    private ScheduledFuture<?> timeoutFuture = null;

    private MulticastSocket socket = null;

    public ZMoteDiscoveryService() {
        listeners.add(discoveredDevicesListener);
    }

    @Override
    public void addListener(final IDiscoveryListener listener) {
        listeners.add(listener);
    }

    @Override
    public ZMoteDevice getDevice(final String uuid) {
        if (uuid == null) {
            return null;
        }

        return discoveredDevices.get(uuid);
    }

    @Override
    public boolean isOnline(final String uuid) {
        return discoveredDevices.containsKey(uuid);
    }

    @Override
    public void removeListener(final IDiscoveryListener listener) {
        listeners.remove(listener);
    }

    @Override
    public synchronized void startDiscovery() {
        try {
            if (discoveryFuture != null) {
                return; // discovery already running
            }

            notifyStarted();

            discoveryFuture = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    executeDiscovery();
                }
            }, 0, TimeUnit.SECONDS);

            timeoutFuture = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    stopDiscovery();
                }
            }, ZMoteBindingConstants.DISCOVERY_TIMEOUT, TimeUnit.SECONDS);

        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to start ZMote discovery!", e);
            }
            stopDiscovery();
        }
    }

    @Override
    public synchronized void stopDiscovery() {
        if ((discoveryFuture != null) && !discoveryFuture.isCancelled()) {
            discoveryFuture.cancel(true);
            discoveryFuture = null;
        }

        if ((timeoutFuture != null) && !timeoutFuture.isCancelled()) {
            timeoutFuture.cancel(true);
            timeoutFuture = null;
        }

        safeCloseSocket(); // terminates receive() call on the socket
        notifyFinished();
    }

    private void notifyDiscovery(final ZMoteDevice device) {
        for (final IDiscoveryListener listener : listeners) {
            try {
                listener.deviceDiscovered(device);

            } catch (final Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Exception while notifying discovery listener.", e);
                }
            }
        }
    }

    private void notifyFinished() {
        for (final IDiscoveryListener listener : listeners) {
            try {
                listener.discoveryFinished();

            } catch (final Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Exception while notifying discovery listener.", e);
                }
            }
        }
    }

    private void notifyStarted() {
        for (final IDiscoveryListener listener : listeners) {
            try {
                listener.discoveryStarted();

            } catch (final Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Exception while notifying discovery listener.", e);
                }
            }
        }
    }

    private void executeDiscovery() {
        try {
            final InetAddress inetAddress = InetAddress.getByName(MCAST_GROUP);
            final byte[] datagramBuffer = new byte[512];
            socket = new MulticastSocket(SOCKET_PORT);
            socket.joinGroup(inetAddress);
            socket.setSoTimeout(SOCKET_TIMEOUT);
            socket.send(new DatagramPacket(MCAST_REQ_PREFIX.getBytes(), MCAST_REQ_PREFIX.length(), inetAddress,
                    MCAST_REQ_PORT));

            while (!Thread.currentThread().isInterrupted()) {
                final DatagramPacket recv = new DatagramPacket(datagramBuffer, datagramBuffer.length);

                try {
                    socket.receive(recv);
                } catch (final SocketTimeoutException e) {
                    return; // no more devices responded
                }

                final String responseMessage = new String(recv.getData(), recv.getOffset(), recv.getLength());
                final ZMoteDevice zmoteDevice = parseDiscoveryResponse(responseMessage);

                if (zmoteDevice != null) {
                    notifyDiscovery(zmoteDevice);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Failed to parse discovery response: {}", responseMessage);
                }
            }

        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("ZMote device discovery failed or has been aborted!", e);
            }
        } finally {
            safeCloseSocket();
        }
    }

    private ZMoteDevice parseDiscoveryResponse(final String msg) {
        if (!msg.startsWith(MCAST_RES_PREFIX)) {
            return null; // invalid response
        }

        // match them separately and not in one RegEx, so the order does not matter.
        final Matcher matcherMake = PATTERN_MAKE.matcher(msg);
        final Matcher matcherModel = PATTERN_MODEL.matcher(msg);
        final Matcher materchRevision = PATTERN_REVISION.matcher(msg);
        final Matcher matcherType = PATTERN_TYPE.matcher(msg);
        final Matcher matcherURL = PATTERN_URL.matcher(msg);
        final Matcher matcherUUID = PATTERN_UUID.matcher(msg);

        final String make = matcherMake.find() ? matcherMake.group(1) : null;
        final String model = matcherModel.find() ? matcherModel.group(1) : null;
        final String revision = materchRevision.find() ? materchRevision.group(1) : null;
        final String type = matcherType.find() ? matcherType.group(1) : null;
        final String url = matcherURL.find() ? matcherURL.group(1) : null;
        final String uuid = matcherUUID.find() ? matcherUUID.group(1) : null;

        if (!ZMOTE_MAKE.equalsIgnoreCase(make) || !ZMOTE_TYPE.equalsIgnoreCase(type) || (uuid == null)
                || (url == null)) {
            return null; // unsupported device
        }

        return new ZMoteDevice(make, type, model, revision, uuid, url);
    }

    private void safeCloseSocket() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (final RuntimeException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignored exception while safe-closing socket.", e);
            }
        } finally {
            socket = null;
        }
    }

    private static class DeviceDiscoveryListener implements IDiscoveryListener {

        private final Map<String, ZMoteDevice> activeDevices;
        private final Map<String, ZMoteDevice> discoveredDevices = new HashMap<>();

        public DeviceDiscoveryListener(final Map<String, ZMoteDevice> activeDevices) {
            this.activeDevices = activeDevices;
        }

        @Override
        public void deviceDiscovered(final ZMoteDevice device) {
            discoveredDevices.put(device.getUuid(), device);
        }

        @Override
        public void discoveryFinished() {
            final Set<String> activeUuids = activeDevices.keySet();
            final Set<String> discoveredUuids = discoveredDevices.keySet();

            // remove inactive devices
            final Iterator<String> activeIterator = activeUuids.iterator();

            while (activeIterator.hasNext()) {
                if (!discoveredUuids.contains(activeIterator.next())) {
                    activeIterator.remove();
                }
            }

            // add newly discovered devices
            final Iterator<String> discoveredIterator = discoveredUuids.iterator();

            while (discoveredIterator.hasNext()) {
                final String discoveredUuid = discoveredIterator.next();
                if (!activeUuids.contains(discoveredUuid)) {
                    activeDevices.put(discoveredUuid, discoveredDevices.get(discoveredUuid));
                }
            }
        }

        @Override
        public void discoveryStarted() {
            discoveredDevices.clear();
        }
    }
}
