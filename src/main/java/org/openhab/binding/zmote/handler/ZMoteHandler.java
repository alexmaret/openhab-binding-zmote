/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.handler;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.zmote.ZMoteBindingConstants;
import org.openhab.binding.zmote.internal.discovery.IZMoteDiscoveryService;
import org.openhab.binding.zmote.internal.exception.CommunicationException;
import org.openhab.binding.zmote.internal.exception.ConfigurationException;
import org.openhab.binding.zmote.internal.exception.DeviceBusyException;
import org.openhab.binding.zmote.internal.model.ZMoteConfig;
import org.openhab.binding.zmote.internal.model.ZMoteDevice;
import org.openhab.binding.zmote.internal.service.IZMoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZMoteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Maret-Huskinson - Initial contribution
 */
public class ZMoteHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ZMoteHandler.class);

    private final IZMoteService zmoteService;
    private final IZMoteDiscoveryService zmoteDiscoveryService;
    private ScheduledFuture<?> statusUpdateFuture = null;

    public ZMoteHandler(final Thing thing, final IZMoteService service, final IZMoteDiscoveryService discoveryService) {
        super(thing);
        zmoteService = service;
        zmoteDiscoveryService = discoveryService;
    }

    @Override
    public void dispose() {
        stopStatusUpdateWorker();
        unregisterDeviceConfiguration();
    }

    @Override
    public void initialize() {
        try {
            startStatusUpdateWorker();

            final ZMoteConfig config = getZMoteConfig();
            registerDeviceConfiguration(config);

        } catch (final Exception e) {
            updateStatusFromException(e);
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Channel {} called with command {} of type {}", channelUID, command,
                        (command != null ? command.getClass().getName() : null));
            }

            final ZMoteConfig zmoteConfig = getZMoteConfig();

            if (channelUID.getId().equals(ZMoteBindingConstants.CHANNEL_ONLINE)) {
                onChannelOnlineCommand(channelUID, command, zmoteConfig);

            } else if (channelUID.getId().equals(ZMoteBindingConstants.CHANNEL_SENDCODE)) {
                onChannelSendCodeCommand(channelUID, command, zmoteConfig);

            } else if (channelUID.getId().equals(ZMoteBindingConstants.CHANNEL_SENDKEY)) {
                onChannelSendKeyCommand(channelUID, command, zmoteConfig);

            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("Don't know how to handle command {} on channel {}!", command, channelUID);
                }
            }
        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to process command '{}' on channel '{}'!", command, channelUID);
            }
            updateStatusFromException(e);
        }
    }

    @Override
    public void handleConfigurationUpdate(final Map<String, Object> configurationParameters) {
        unregisterDeviceConfiguration();
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    protected void updateStatus(final ThingStatus status) {
        final ThingStatus currentStatus = getThing().getStatus();

        // we do not want to override status details previously set
        // so don't set the state if it is unchanged
        if (currentStatus != status) {
            super.updateStatus(status);
        }
    }

    private void onChannelOnlineCommand(final ChannelUID channelUID, final Command command, final ZMoteConfig config) {
        if (command instanceof RefreshType) {
            updateChannelOnlineState(config);
        }
    }

    private void onChannelSendCodeCommand(final ChannelUID channelUID, final Command command,
            final ZMoteConfig config) {
        if (!isThingOnline() || (command == null) || (command instanceof RefreshType)) {
            return;
        }

        if ((command != null) && (zmoteService != null)) {
            final String code = command.toString();
            zmoteService.sendCode(config, code);
        }
    }

    private void onChannelSendKeyCommand(final ChannelUID channelUID, final Command command, final ZMoteConfig config) {
        if (!isThingOnline() || (command == null) || (command instanceof RefreshType)) {
            return;
        }

        if ((command != null) && (zmoteService != null)) {
            final String button = command.toString();
            zmoteService.sendKey(config, button);
        }
    }

    private ZMoteConfig getZMoteConfig() {
        final ZMoteConfig config = getConfigAs(ZMoteConfig.class);

        if (config == null) {
            throw new ConfigurationException("Failed to read thing configuration!");
        }

        config.setAutoUrl(getThing().getProperties().get(ZMoteBindingConstants.PROP_URL));

        return config;
    }

    private ZMoteConfig getZMoteConfigValidated() {
        final ZMoteConfig config = getZMoteConfig();

        if (config.getUuid() == null) {
            throw new ConfigurationException("Thing has no UUID set!");
        }

        if (config.getUrl() == null) {
            throw new CommunicationException("The device has not yet been discovered!");
        }

        return config;
    }

    private boolean isThingOnline() {
        return ThingStatus.ONLINE.equals(getThing().getStatus());
    }

    private boolean isZMoteOnline(final ZMoteConfig config) {

        final String overrideUrl = config.getOverrideUrl();

        if ((overrideUrl != null) && !overrideUrl.isEmpty()) {
            return ((zmoteService != null) && zmoteService.checkOnline(config));
        }

        return ((zmoteDiscoveryService != null) && zmoteDiscoveryService.isOnline(config.getUuid()));
    }

    private void registerDeviceConfiguration(final ZMoteConfig config) {
        if (zmoteService == null) {
            throw new IllegalStateException("Internal plugin error: The ZMote service is not available!");
        }
        zmoteService.registerConfiguration(config);
    }

    private void unregisterDeviceConfiguration() {
        try {
            if (zmoteService == null) {
                throw new IllegalStateException("Internal plugin error: The ZMote service is not available!");
            }

            final ZMoteConfig zmoteConfig = getZMoteConfig();
            zmoteService.unregisterConfiguration(zmoteConfig);

        } catch (final Exception e) {
            // ignore it, the configuration was probably never registered as it is invalid
            if (logger.isDebugEnabled()) {
                logger.debug("Ignored exception while removing device configuration!", e);
            }
        }
    }

    private void startStatusUpdateWorker() {
        stopStatusUpdateWorker();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateStatusFromDiscoveryService();
            }
        };

        statusUpdateFuture = scheduler.scheduleWithFixedDelay(runnable, 0,
                ZMoteBindingConstants.DISCOVERY_UPDATE_INTERVAL, TimeUnit.SECONDS);
    }

    private void stopStatusUpdateWorker() {
        try {
            if (statusUpdateFuture != null) {
                statusUpdateFuture.cancel(true);
                statusUpdateFuture = null;
            }
        } catch (final Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignored exception while stopping status update worker!", e);
            }
        }
    }

    private void updateChannelOnlineState(final ZMoteConfig config) {
        if (isThingOnline()) {
            updateState(ZMoteBindingConstants.CHANNEL_ONLINE, OnOffType.ON);
        } else {
            updateState(ZMoteBindingConstants.CHANNEL_ONLINE, OnOffType.OFF);
        }
    }

    private void updateThingOnlineStatus(final ZMoteConfig zmoteConfig) {
        final ThingStatusInfo thingStatusInfo = getThing().getStatusInfo();
        final ThingStatus thingStatus = thingStatusInfo.getStatus();
        final ThingStatusDetail thingStatusDetail = thingStatusInfo.getStatusDetail();

        if (!isZMoteOnline(zmoteConfig)) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        if (ThingStatus.OFFLINE.equals(thingStatus)) {
            if (ThingStatusDetail.CONFIGURATION_ERROR.equals(thingStatusDetail)) {
                try {
                    getZMoteConfigValidated();
                } catch (final Exception e) {
                    return; // still invalid
                }

            } else if (ThingStatusDetail.COMMUNICATION_ERROR.equals(thingStatusDetail)) {
                if ((zmoteService != null) && !zmoteService.checkOnline(zmoteConfig)) {
                    return; // still offline
                }
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    private void updateThingProperties(final ZMoteDevice device) {
        final Map<String, String> properties = editProperties();

        properties.put(ZMoteBindingConstants.PROP_UUID, device.getUuid());
        properties.put(ZMoteBindingConstants.PROP_URL, device.getUrl());
        properties.put(ZMoteBindingConstants.PROP_MAKE, device.getMake());
        properties.put(ZMoteBindingConstants.PROP_MODEL, device.getModel());
        properties.put(ZMoteBindingConstants.PROP_REVISION, device.getRevision());
        properties.put(ZMoteBindingConstants.PROP_TYPE, device.getType());

        updateProperties(properties);
    }

    private void updateStatusFromDiscoveryService() {

        try {
            if (zmoteDiscoveryService == null) {
                return; // no discovery service available right now
            }

            ZMoteConfig zmoteConfig = getZMoteConfig(); // config might not be valid yet without discovery
            final ZMoteDevice zmoteDevice = zmoteDiscoveryService.getDevice(zmoteConfig.getUuid());

            if (zmoteDevice != null) {
                updateThingProperties(zmoteDevice);
                zmoteConfig = getZMoteConfigValidated();
            }

            // thing has to be updated first, then the channel!
            updateThingOnlineStatus(zmoteConfig);
            updateChannelOnlineState(zmoteConfig);

        } catch (final Exception e) {
            updateStatusFromException(e);
        }
    }

    private void updateStatusFromException(final Exception e) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Setting device to OFFLINE due to exception!", e);
            }

            if (e instanceof DeviceBusyException) {
                // no status change, only temporary

            } else if (e instanceof CommunicationException) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());

            } else if (e instanceof ConfigurationException) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());

            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            }

            updateState(ZMoteBindingConstants.CHANNEL_ONLINE, OnOffType.OFF);

        } catch (final Exception ex) {
            // this is usually called from an exception handler
            // we do not want to lose the initial error by throwing another exception
            if (logger.isDebugEnabled()) {
                logger.debug("Ignored exception while recovering from error!", ex);
            }
        }
    }
}
