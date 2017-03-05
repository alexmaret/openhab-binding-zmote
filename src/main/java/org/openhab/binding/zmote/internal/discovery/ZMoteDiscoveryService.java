package org.openhab.binding.zmote.internal.discovery;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.openhab.binding.zmote.internal.model.ZMoteDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZMoteDiscoveryService implements IZMoteDiscoveryService {

    private static final int RESTART_INTERVAL = 60000;
    private static final int LAST_SEEN_THRESHOLD = 60000;

    private static final int DISCOVERY_SOCKET_PORT = 9131;
    private static final String MCAST_GROUP = "::ffff:239.255.250.250";
    private static final int MCAST_REQ_PORT = 9130;
    private static final String MCAST_REQ_PREFIX = "SENDAMXB";
    private static final String MCAST_RES_PREFIX = "AMXB";

    private static final Pattern PATTERN_MAKE = Pattern.compile("<-Make=([^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_MODEL = Pattern.compile("<-Model=([^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_REVISION = Pattern.compile("<-Revision=([^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_TYPE = Pattern.compile("<-Type=([^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_URL = Pattern.compile("<-Config-URL=(http[^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_UUID = Pattern.compile("<-UUID=([^>]+)>", Pattern.CASE_INSENSITIVE);

    private static final String ZMOTE_TYPE = "ZMT2";
    private static final String ZMOTE_MAKE = "zmote.io";

    private final Logger logger = LoggerFactory.getLogger(ZMoteDiscoveryService.class);

    private final Map<String, ZMoteDiscoveryResult> discoveryResults = new ConcurrentHashMap<>();
    private final List<IDiscoveryListener> discoveryListeners = new CopyOnWriteArrayList<>();

    private ScheduledExecutorService scheduler = null;
    private ScheduledFuture<?> discoveryFuture = null;
    private MulticastSocket discoverySocket = null;

    @Override
    public void addListener(final IDiscoveryListener listener) {
        discoveryListeners.add(listener);
    }

    @Override
    public ZMoteDevice getDevice(final String uuid) {
        if (uuid == null) {
            return null;
        }

        final ZMoteDiscoveryResult zmoteDiscoveryResult = discoveryResults.get(uuid);

        if (zmoteDiscoveryResult == null) {
            return null;
        }

        // make sure this device is not too old
        final long currentTime = System.currentTimeMillis();
        final long discoveryTime = zmoteDiscoveryResult.getLastSeen().getTime();

        if ((currentTime - discoveryTime) > LAST_SEEN_THRESHOLD) {
            discoveryResults.remove(uuid);
            return null; // too old
        }

        return zmoteDiscoveryResult.getDevice();
    }

    @Override
    public boolean isOnline(final String uuid) {
        return (getDevice(uuid) != null);
    }

    @Override
    public void removeListener(final IDiscoveryListener listener) {
        discoveryListeners.remove(listener);
    }

    @Override
    public synchronized void startScan() {
        MulticastSocket scanSocket = null;

        try {
            final InetAddress inetAddress = InetAddress.getByName(MCAST_GROUP);
            scanSocket = new MulticastSocket(0);
            scanSocket.setReuseAddress(true);
            scanSocket.joinGroup(inetAddress);
            scanSocket.send(new DatagramPacket(MCAST_REQ_PREFIX.getBytes(), MCAST_REQ_PREFIX.length(), inetAddress,
                    MCAST_REQ_PORT));

        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to scan for ZMote devices!", e);
            }

        } finally {
            safeClose(scanSocket);
        }
    }

    protected void activate() {
        try {
            if (scheduler != null) {
                deactivate();
            }

            scheduler = ThreadPoolManager.getScheduledPool(ZMoteDiscoveryService.class.getName());
            startDiscoveryFuture();
            startScan();

            if (logger.isDebugEnabled()) {
                logger.debug("Activated ZMote discovery service.");
            }

        } catch (final RuntimeException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to activate ZMote discovery service!", e);
            }
            deactivate();
            throw e;
        }
    }

    protected void deactivate() {
        try {
            if (scheduler != null) {
                scheduler.shutdown();
            }

            stopDiscoveryFuture();

            if (logger.isDebugEnabled()) {
                logger.debug("Deactivated ZMote discovery service.");
            }

        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Ignored exception while deactivating ZMote discovery service.", e);
            }

        } finally {
            scheduler = null;
        }
    }

    private void executeDiscovery() {
        try {
            final InetAddress inetAddress = InetAddress.getByName(MCAST_GROUP);
            final byte[] datagramBuffer = new byte[512];

            discoverySocket = new MulticastSocket(DISCOVERY_SOCKET_PORT);
            discoverySocket.setSoTimeout(0);
            discoverySocket.setReuseAddress(true);
            discoverySocket.joinGroup(inetAddress);

            while (!Thread.currentThread().isInterrupted()) {
                final DatagramPacket recv = new DatagramPacket(datagramBuffer, datagramBuffer.length);

                discoverySocket.receive(recv);

                final String responseMessage = new String(recv.getData(), recv.getOffset(), recv.getLength());
                final ZMoteDevice zmoteDevice = parseDiscoveryResponse(responseMessage);

                if (zmoteDevice != null) {
                    discoveryResults.put(zmoteDevice.getUuid(), new ZMoteDiscoveryResult(zmoteDevice));
                    notifyDiscovery(zmoteDevice);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Discovered ZMote device: {}", zmoteDevice.toString());
                    }

                } else if (logger.isDebugEnabled()) {
                    logger.debug("Discovered unsupported device: {}", responseMessage);
                }
            }

        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("ZMote device discovery failed or has been aborted!", e);
            }
        } finally {
            safeCloseDiscoverySocket();
        }
    }

    private void notifyDiscovery(final ZMoteDevice device) {
        for (final IDiscoveryListener listener : discoveryListeners) {
            try {
                listener.deviceDiscovered(device);

            } catch (final Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Exception while notifying discovery listener.", e);
                }
            }
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

    private void safeClose(final MulticastSocket multicastSocket) {
        try {
            if (multicastSocket != null) {
                multicastSocket.close();
            }
        } catch (final RuntimeException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignored exception while safe-closing multicast socket.", e);
            }
        }
    }

    private void safeCloseDiscoverySocket() {
        try {
            safeClose(discoverySocket);
        } finally {
            discoverySocket = null;
        }
    }

    private synchronized void startDiscoveryFuture() {
        try {
            if ((discoveryFuture != null) && !discoveryFuture.isDone()) {
                return; // already running
            }

            stopDiscoveryFuture(); // cleanup

            discoveryFuture = scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    executeDiscovery();
                }
            }, 0, RESTART_INTERVAL, TimeUnit.MILLISECONDS);

        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to start ZMote discovery!", e);
            }
            stopDiscoveryFuture();
        }
    }

    private synchronized void stopDiscoveryFuture() {
        try {
            if ((discoveryFuture != null) && !discoveryFuture.isCancelled()) {
                discoveryFuture.cancel(true);
            }

        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Ignored exception while stopping discovery future!", e);
            }

        } finally {
            safeCloseDiscoverySocket(); // terminates receive() call on the socket
            discoveryFuture = null;
            discoveryResults.clear();
        }
    }

    private static class ZMoteDiscoveryResult {

        private final Date lastSeen;
        private final ZMoteDevice device;

        public ZMoteDiscoveryResult(final ZMoteDevice device) {
            this.lastSeen = new Date();
            this.device = device;
        }

        public ZMoteDevice getDevice() {
            return device;
        }

        public Date getLastSeen() {
            return lastSeen;
        }
    }
}
