/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
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
        unregisterDeviceConfiguration(null);
    }

    @Override
    public void initialize() {
        try {
            final ZMoteConfig config = getZMoteConfigValidated();
            registerDeviceConfiguration(config);
            startStatusUpdateWorker();
            updateOnlineStatus(config);
            updateOnlineState(config);

        } catch (final Exception e) {
            stopStatusUpdateWorker();
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

            final ZMoteConfig zmoteConfig = getZMoteConfigValidated();

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
        unregisterDeviceConfiguration(null);
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
            updateOnlineState(config);
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

        // make sure we have at least a valid UUID
        if (StringUtils.trimToNull(config.getUuid()) == null) {
            throw new ConfigurationException("Thing has no UUID set!");
        }

        return config;
    }

    private ZMoteConfig getZMoteConfigValidated() {
        final ZMoteConfig config = getZMoteConfig();

        if (StringUtils.trimToNull(config.getConfigFile()) == null) {
            throw new ConfigurationException("A configuration file needs to be provided to use this thing.");
        }

        if (StringUtils.trimToNull(config.getRemote()) == null) {
            throw new ConfigurationException("A remote name has to be set to use this thing.");
        }

        if (StringUtils.trimToNull(config.getUrl()) == null) {
            throw new ConfigurationException("A URL has to be set to use this thing.");
        }

        return config;
    }

    private boolean isThingOnline() {
        return ThingStatus.ONLINE.equals(getThing().getStatus());
    }

    private void registerDeviceConfiguration(final ZMoteConfig config) {
        if (zmoteService == null) {
            throw new IllegalStateException("Internal plugin error: The ZMote service is not available!");
        }

        final ZMoteConfig zmoteConfig = (config != null) ? config : getZMoteConfigValidated();
        zmoteService.registerConfiguration(zmoteConfig);
    }

    private void unregisterDeviceConfiguration(final ZMoteConfig config) {
        try {
            if (zmoteService == null) {
                throw new IllegalStateException("Internal plugin error: The ZMote service is not available!");
            }

            final ZMoteConfig zmoteConfig = (config != null) ? config : getZMoteConfigValidated();
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
                try {
                    updateStatusAndConfigurationFromDiscoveryService();
                } catch (final Exception e) {
                    updateStatusFromException(e);
                }
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

    private void updateOnlineState(final ZMoteConfig config) {
        final String uuid = config.getUuid();

        if ((zmoteDiscoveryService != null) && zmoteDiscoveryService.isOnline(uuid)) {
            updateState(ZMoteBindingConstants.CHANNEL_ONLINE, OnOffType.ON);
        } else {
            updateState(ZMoteBindingConstants.CHANNEL_ONLINE, OnOffType.OFF);
        }
    }

    private void updateOnlineStatus(final ZMoteConfig zmoteConfig) {
        final ThingStatusInfo thingStatusInfo = getThing().getStatusInfo();
        final ThingStatus thingStatus = thingStatusInfo.getStatus();
        final ThingStatusDetail thingStatusDetail = thingStatusInfo.getStatusDetail();

        if ((zmoteDiscoveryService == null) || !zmoteDiscoveryService.isOnline(zmoteConfig.getUuid())) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        if (ThingStatus.OFFLINE.equals(thingStatus)) {
            if (ThingStatusDetail.CONFIGURATION_ERROR.equals(thingStatusDetail)) {
                try {
                    getZMoteConfigValidated();
                } catch (final Exception e) {
                    return; // config is still invalid
                }

            } else if (ThingStatusDetail.COMMUNICATION_ERROR.equals(thingStatusDetail)) {
                // TODO try to talk to the device using a REST call before setting it online again
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    private void updateStatusAndConfigurationFromDiscoveryService() {

        if (zmoteDiscoveryService == null) {
            return; // no discovery service available right now
        }

        final ZMoteConfig zmoteConfig = getZMoteConfig();
        final ZMoteDevice zmoteDevice = zmoteDiscoveryService.getDevice(zmoteConfig.getUuid());

        if (zmoteDevice != null) {
            final String configUrl = (String) getConfig().get(ZMoteBindingConstants.CONFIG_URL);
            final String deviceUrl = zmoteDevice.getUrl();

            if ((deviceUrl != null) && !deviceUrl.isEmpty() && !deviceUrl.equals(configUrl)) {
                final Map<String, Object> configParams = new HashMap<>();
                configParams.put(ZMoteBindingConstants.CONFIG_URL, deviceUrl);
                handleConfigurationUpdate(configParams); // make sure the device is reinitialized
            }
        }

        updateOnlineState(zmoteConfig);
        updateOnlineStatus(zmoteConfig);
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
