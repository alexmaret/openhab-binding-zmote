package org.openhab.binding.zmote.internal.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.zmote.internal.config.IRCodeConfigurationCache;
import org.openhab.binding.zmote.internal.config.RemoteConfiguration;
import org.openhab.binding.zmote.internal.exception.CommunicationException;
import org.openhab.binding.zmote.internal.exception.ConfigurationException;
import org.openhab.binding.zmote.internal.exception.DeviceBusyException;
import org.openhab.binding.zmote.internal.model.IRCode;
import org.openhab.binding.zmote.internal.model.ZMoteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZMoteService implements IZMoteService {

    private final Logger logger = LoggerFactory.getLogger(ZMoteService.class);

    private final Map<String, IZMoteClient> zmoteClients = new ConcurrentHashMap<>();
    private final Map<String, Map<String, IRCodeConfigurationCache>> configCache = new ConcurrentHashMap<>();

    @Override
    public boolean sendCode(final ZMoteConfig config, final String code) {
        return sendCode(config, code, 1);
    }

    @Override
    public boolean sendCode(final ZMoteConfig config, final String code, final int repeat) {
        final String uuid = config.getUuid();
        return transmitCode(uuid, new IRCode(code), repeat);
    }

    @Override
    public boolean sendKey(final ZMoteConfig config, final String button) {
        return sendKey(config, button, 1);
    }

    @Override
    public boolean sendKey(final ZMoteConfig config, final String button, final int repeat) {
        final String uuid = config.getUuid();
        final String remote = config.getRemote();
        final IRCode code = lookupCode(uuid, remote, button);

        if (code == null) {
            if (logger.isErrorEnabled()) {
                logger.error("No IR code found for button '{}' of remote '%s'.", button, remote);
            }
            return false;
        }

        return transmitCode(uuid, code, repeat);
    }

    @Override
    public synchronized void registerConfiguration(final ZMoteConfig config) {

        try {
            final String uuid = config.getUuid();
            final String url = config.getUrl();
            final BigDecimal timeout = config.getTimeout();
            final String remote = config.getRemote();
            final String configFilePath = config.getConfigFile();

            final int timeoutInt = (timeout != null) ? timeout.intValue() : 10;

            registerRemote(uuid, remote, configFilePath);
            registerClient(uuid, url, timeoutInt);

        } catch (final Exception e) {
            throw new ConfigurationException(
                    String.format("Failed to initialize configuration for device: %s!", config), e);
        }
    }

    @Override
    public synchronized void unregisterConfiguration(final ZMoteConfig config) {
        try {
            final String uuid = config.getUuid();
            final String remote = config.getRemote();

            final Map<String, IRCodeConfigurationCache> remotesCache = configCache.get(uuid);

            if (remotesCache != null) {
                remotesCache.remove(remote);
            }

            if ((remotesCache == null) || remotesCache.isEmpty()) {
                final IZMoteClient client = zmoteClients.get(uuid);

                if (client != null) {
                    client.dispose();
                    zmoteClients.remove(uuid);
                }
            }
        } catch (final Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error("Failed to remove device configuration {}!", config, e);
            }
        }
    }

    protected void activate() {
        if (logger.isDebugEnabled()) {
            logger.debug("ZMote service activated.");
        }
    }

    protected void deactivate() {
        if (logger.isDebugEnabled()) {
            logger.debug("ZMote service deactivated.");
        }

        final Set<String> clientUuids = zmoteClients.keySet();
        final Iterator<String> iterator = clientUuids.iterator();

        while (iterator.hasNext()) {
            final String uuid = iterator.next();
            final IZMoteClient client = zmoteClients.get(uuid);

            if (client != null) {
                client.dispose();
            }

            iterator.remove();
        }
    }

    private IRCode lookupCode(final String uuid, final String remote, final String buttonName) {
        final Map<String, IRCodeConfigurationCache> remotesCache = configCache.get(uuid);

        if (remotesCache == null) {
            return null;
        }

        final IRCodeConfigurationCache codesCache = remotesCache.get(remote);

        if (codesCache == null) {
            return null;
        }

        return codesCache.getCode(buttonName);
    }

    private void registerClient(final String uuid, final String url, final int timeout) {

        IZMoteClient client = zmoteClients.get(uuid);

        if (client == null) {
            client = new ZMoteV2Client(url, timeout);
            client.initialize();
            zmoteClients.put(uuid, client);
        }
    }

    private void registerRemote(final String uuid, final String remote, final String configFile) {

        Map<String, IRCodeConfigurationCache> remotesCache = configCache.get(uuid);

        if (remotesCache == null) {
            remotesCache = new ConcurrentHashMap<>();
            configCache.put(uuid, remotesCache);
        }

        remotesCache.put(remote, new IRCodeConfigurationCache(new RemoteConfiguration(new File(configFile))));
    }

    private boolean transmitCode(final String uuid, final IRCode code, final int repeat) {

        final IZMoteClient client = zmoteClients.get(uuid);

        if (client == null) {
            throw new IllegalStateException(String.format("No client available for uuid %s!", uuid));
        }

        boolean success = false;

        for (int i = 1; i <= repeat; ++i) {
            try {
                client.sendir(uuid, code.nextCode());
                success = true; // if at least one code was sent we consider it a success

            } catch (final DeviceBusyException e) {
                // busy or other communication errors
            } catch (final Exception e) {
                throw new CommunicationException("Communication with device failed!", e);
            }
        }

        return success;
    }
}
