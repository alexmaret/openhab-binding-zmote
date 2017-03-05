package org.openhab.binding.zmote.internal.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.zmote.ZMoteBindingConstants;
import org.openhab.binding.zmote.internal.config.IRCodeConfigurationCache;
import org.openhab.binding.zmote.internal.config.RemoteConfiguration;
import org.openhab.binding.zmote.internal.exception.CommunicationException;
import org.openhab.binding.zmote.internal.exception.ConfigurationException;
import org.openhab.binding.zmote.internal.exception.DeviceBusyException;
import org.openhab.binding.zmote.internal.exception.ZMoteBindingException;
import org.openhab.binding.zmote.internal.model.IRCode;
import org.openhab.binding.zmote.internal.model.ZMoteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZMoteService implements IZMoteService {

    private final Logger logger = LoggerFactory.getLogger(ZMoteService.class);

    private final HttpClient httpClient = new HttpClient();

    private final Map<String, IZMoteClient> clients = new ConcurrentHashMap<>();
    private final Map<String, IRCodeConfigurationCache> files = new ConcurrentHashMap<>();

    @Override
    public boolean checkOnline(final ZMoteConfig config) {
        final IZMoteClient client = findOrCreateZmoteClient(config);

        try {
            client.check(getTimeout(config));
            return true;

        } catch (final CommunicationException e) {
            return false;
        }
    }

    @Override
    public boolean sendCode(final ZMoteConfig config, final String code) {
        return sendCode(config, code, 1);
    }

    @Override
    public boolean sendCode(final ZMoteConfig config, final String code, final int repeat) {
        return transmitCode(config, new IRCode(code), repeat);
    }

    @Override
    public boolean sendKey(final ZMoteConfig config, final String button) {
        return sendKey(config, button, 1);
    }

    @Override
    public boolean sendKey(final ZMoteConfig config, final String button, final int repeat) {
        final String configFile = config.getConfigFile();

        if (configFile == null) {
            if (logger.isErrorEnabled()) {
                logger.error("Cannot send button key {} to device {} as no configuration file has been set!",
                        configFile, config.getUuid());
            }
            return false;
        }

        final IRCodeConfigurationCache codesCache = findOrCreateIRCodeCache(configFile);
        final IRCode code = codesCache.getCode(button);

        if (code == null) {
            if (logger.isErrorEnabled()) {
                logger.error("No IR code found for button '{}' in config file '{}'.", button, configFile);
            }
            return false;
        }

        return transmitCode(config, code, repeat);
    }

    @Override
    public synchronized void registerConfiguration(final ZMoteConfig config) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Registering ZMote device configuration: {}", config);
            }

            final String configFile = config.getConfigFile();

            if (configFile != null) {
                findOrCreateIRCodeCache(configFile);
            }

            if (config.getUrl() != null) {
                findOrCreateZmoteClient(config);
            }

        } catch (final ConfigurationException e) {
            throw e;

        } catch (final Exception e) {
            throw new ConfigurationException(String.format("Failed to initialize configuration: %s!", config), e);
        }
    }

    @Override
    public synchronized void unregisterConfiguration(final ZMoteConfig config) {
        if (logger.isDebugEnabled()) {
            logger.debug("Unregistering ZMote device configuration: {}", config);
        }

        final String uuid = config.getUuid();
        final String configFile = config.getConfigFile();

        if (configFile != null) {
            files.remove(configFile);
        }

        if (uuid != null) {
            clients.remove(uuid);
        }
    }

    protected void activate() {
        try {
            httpClient.setFollowRedirects(true);
            httpClient.start();

        } catch (final Exception e) {
            throw new ZMoteBindingException("Failed to start HTTP client!", e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("ZMote service activated.");
        }
    }

    protected void deactivate() {

        files.clear();
        clients.clear();

        try {
            httpClient.stop();

        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to stop HTTP client!", e);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("ZMote service deactivated.");
        }
    }

    private IZMoteClient findOrCreateZmoteClient(final ZMoteConfig config) {

        final String uuid = config.getUuid();
        final String url = config.getUrl();

        if ((uuid == null) || (url == null)) {
            throw new IllegalArgumentException("Invalid ZMote configuration provided!");
        }

        IZMoteClient client = clients.get(uuid);

        if ((client != null) && !url.equals(client.getUrl())) {
            client = null; // URL changed
        }

        if (client == null) {
            client = new ZMoteV2Client(httpClient, url, uuid);
            clients.put(uuid, client);
        }

        return client;
    }

    private IRCodeConfigurationCache findOrCreateIRCodeCache(final String configFile) {

        IRCodeConfigurationCache cache;

        if (configFile != null) {
            cache = files.get(configFile);

            if (cache != null) {
                return cache;
            }
        }

        cache = new IRCodeConfigurationCache(new RemoteConfiguration(new File(configFile)));
        files.put(configFile, cache);

        return cache;
    }

    private int getRetry(final ZMoteConfig config) {
        final BigDecimal configRetry = config.getRetry();
        return (configRetry != null) ? configRetry.intValue() : ZMoteBindingConstants.DEFAULT_RETRY;
    }

    private int getTimeout(final ZMoteConfig config) {
        final BigDecimal timeout = config.getTimeout();
        return (timeout != null) ? timeout.intValue() : ZMoteBindingConstants.DEFAULT_TIMEOUT;
    }

    private boolean transmitCode(final ZMoteConfig config, final IRCode code, final int repeat) {
        final String uuid = config.getUuid();
        final int retry = getRetry(config);
        final int timeoutInt = getTimeout(config);
        final IZMoteClient client = findOrCreateZmoteClient(config);

        boolean success = false;

        for (int i = 0; i < repeat; ++i) {

            success = false;

            for (int j = 0; j <= retry; ++j) {
                try {
                    client.sendir(code.nextCode(), timeoutInt);
                    success = true;
                    break;
                } catch (final DeviceBusyException e) {
                    // busy or other communication errors
                }
            }

            if (success == false) {
                throw new CommunicationException(
                        String.format("Failed to send IR code to device '%s' after %d retries!", uuid, retry));
            }
        }

        return success;
    }
}
