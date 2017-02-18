package org.openhab.binding.zmote.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.zmote.ZMoteBindingConstants;
import org.openhab.binding.zmote.internal.model.ZMoteDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZMoteDiscoveryServiceParticipant extends AbstractDiscoveryService
        implements ExtendedDiscoveryService, IDiscoveryListener {

    private final Logger logger = LoggerFactory.getLogger(ZMoteDiscoveryServiceParticipant.class);
    private IZMoteDiscoveryService zmoteDiscovery = null;
    private DiscoveryServiceCallback discoveryServiceCallback = null;
    private ScheduledFuture<?> backgroundDiscoveryFuture = null;

    public ZMoteDiscoveryServiceParticipant() {
        super(ZMoteBindingConstants.SUPPORTED_THING_TYPES_UIDS, ZMoteBindingConstants.DISCOVERY_TIMEOUT, true);
    }

    protected void setZMoteDiscoveryService(final IZMoteDiscoveryService zmoteDiscoveryService) {
        zmoteDiscovery = zmoteDiscoveryService;

        if (zmoteDiscovery != null) {
            zmoteDiscovery.addListener(this);
        }
    }

    protected void unsetZMoteDiscoveryService(final IZMoteDiscoveryService zmoteDiscoveryService) {
        if (zmoteDiscovery != null) {
            zmoteDiscovery.removeListener(this);
        }

        zmoteDiscovery = null;
    }

    @Override
    protected void startScan() {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting ZMote device discovery");
        }

        if (zmoteDiscovery != null) {
            zmoteDiscovery.startDiscovery();
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("Cannot start discovery as the servie is not yet available!");
            }
        }
    }

    @Override
    public void deviceDiscovered(final ZMoteDevice device) {
        if (logger.isInfoEnabled()) {
            logger.info("Discovered: {}", device.toString());
        }

        final DiscoveryResult currentDiscoveryResult = createDiscoveryResult(device);
        final ThingUID currentThingUID = currentDiscoveryResult.getThingUID();

        if (discoveryServiceCallback != null) {
            final Thing existingThing = discoveryServiceCallback.getExistingThing(currentThingUID);
            updateThing(existingThing, device);

            final DiscoveryResult existingDiscoveryResult = discoveryServiceCallback
                    .getExistingDiscoveryResult(currentThingUID);

            if (!updateDiscoveryResultRequired(existingDiscoveryResult, device)) {
                return; // do not replace result as it did not change
            }
        }

        thingDiscovered(currentDiscoveryResult);
    }

    @Override
    public void discoveryFinished() {
        // we do not care
    }

    @Override
    public void discoveryStarted() {
        // we do not care
    }

    @Override
    public void setDiscoveryServiceCallback(final DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;

    }

    @Override
    protected synchronized void startBackgroundDiscovery() {
        if (backgroundDiscoveryFuture == null) {
            backgroundDiscoveryFuture = scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    startScan();
                }
            }, 0, ZMoteBindingConstants.DISCOVERY_INTERVAL, TimeUnit.SECONDS);
        }
    }

    @Override
    protected synchronized void stopBackgroundDiscovery() {
        if (backgroundDiscoveryFuture != null) {
            backgroundDiscoveryFuture.cancel(true);
            backgroundDiscoveryFuture = null;

            if (zmoteDiscovery != null) {
                zmoteDiscovery.stopDiscovery();
            }
        }
    }

    private DiscoveryResult createDiscoveryResult(final ZMoteDevice device) {
        final String uuid = device.getUuid();
        final String label = String.format("ZMote IR Transmitter (%s)", uuid);

        final Map<String, Object> properties = new HashMap<>(6);
        properties.put(ZMoteBindingConstants.PROP_UUID, uuid);
        properties.put(ZMoteBindingConstants.PROP_URL, device.getUrl());
        properties.put(ZMoteBindingConstants.PROP_MAKE, device.getMake());
        properties.put(ZMoteBindingConstants.PROP_MODEL, device.getModel());
        properties.put(ZMoteBindingConstants.PROP_REVISION, device.getRevision());
        properties.put(ZMoteBindingConstants.PROP_TYPE, device.getType());

        final ThingUID thingUID = new ThingUID(ZMoteBindingConstants.THING_TYPE_ZMT2, uuid);
        return DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(label).build();
    }

    private boolean updateDiscoveryResultRequired(final DiscoveryResult existingDiscoveryResult,
            final ZMoteDevice device) {
        if (existingDiscoveryResult == null) {
            return true; // device does not exist yet, needs update
        }

        final Map<String, Object> properties = existingDiscoveryResult.getProperties();

        if (hasChanged(device.getUuid(), properties.get(ZMoteBindingConstants.PROP_UUID))
                || hasChanged(device.getUrl(), properties.get(ZMoteBindingConstants.PROP_URL))
                || hasChanged(device.getMake(), properties.get(ZMoteBindingConstants.PROP_MAKE))
                || hasChanged(device.getModel(), properties.get(ZMoteBindingConstants.PROP_MODEL))
                || hasChanged(device.getRevision(), properties.get(ZMoteBindingConstants.PROP_REVISION))
                || hasChanged(device.getType(), properties.get(ZMoteBindingConstants.PROP_TYPE))) {
            return true; // device information changed, needs update
        }

        return false;
    }

    private void updateThing(final Thing thing, final ZMoteDevice device) {
        if (thing == null) {
            return; // nothing to update
        }

        thing.setProperty(ZMoteBindingConstants.PROP_UUID, device.getUuid());
        thing.setProperty(ZMoteBindingConstants.PROP_URL, device.getUrl());
        thing.setProperty(ZMoteBindingConstants.PROP_MAKE, device.getMake());
        thing.setProperty(ZMoteBindingConstants.PROP_MODEL, device.getModel());
        thing.setProperty(ZMoteBindingConstants.PROP_REVISION, device.getRevision());
        thing.setProperty(ZMoteBindingConstants.PROP_TYPE, device.getType());
    }

    private boolean hasChanged(final Object source, final Object target) {
        if ((source == null) || (target == null)) {
            return (source != target);
        }

        return !source.equals(target);
    }
}
