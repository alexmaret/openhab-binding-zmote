package org.openhab.binding.zmote.internal.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.zmote.ZMoteBindingConstants;
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
    private final Map<String, IZMoteClient> clients = new ConcurrentHashMap<>();
    private final Map<String, IRCodeConfigurationCache> files = new ConcurrentHashMap<>();

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
        final IRCodeConfigurationCache codesCache = registerOrLookupFile(configFile);
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
            registerOrLookupFile(config.getConfigFile());
            registerOrLookupClient(config);

        } catch (final ConfigurationException e) {
            throw e;

        } catch (final Exception e) {
            throw new ConfigurationException(String.format("Failed to initialize configuration: %s!", config), e);
        }
    }

    @Override
    public synchronized void unregisterConfiguration(final ZMoteConfig config) {
        final String uuid = config.getUuid();
        final String configFile = config.getConfigFile();

        if (configFile != null) {
            files.remove(configFile);
        }

        if (uuid != null) {
            final IZMoteClient client = clients.get(uuid);

            if (client != null) {
                clients.remove(uuid);
                client.dispose();
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

        final Iterator<String> iterator = clients.keySet().iterator();

        while (iterator.hasNext()) {
            final IZMoteClient client = clients.get(iterator.next());

            if (client != null) {
                client.dispose();
            }

            iterator.remove();
        }
    }

    private IZMoteClient registerOrLookupClient(final ZMoteConfig config) {

        final String uuid = config.getUuid();
        IZMoteClient client = clients.get(uuid);

        if (client == null) {
            final String url = config.getUrl();
            final BigDecimal timeout = config.getTimeout();
            final int timeoutInt = (timeout != null) ? timeout.intValue() : ZMoteBindingConstants.DEFAULT_TIMEOUT;

            client = new ZMoteV2Client(url, timeoutInt);
            client.initialize();
            clients.put(uuid, client);
        }

        return client;
    }

    private IRCodeConfigurationCache registerOrLookupFile(final String configFile) {

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

    private boolean transmitCode(final ZMoteConfig config, final IRCode code, final int repeat) {
        final BigDecimal configRetry = config.getRetry();
        final int retry = (configRetry != null) ? configRetry.intValue() : ZMoteBindingConstants.DEFAULT_RETRY;
        final String uuid = config.getUuid();
        final IZMoteClient client = registerOrLookupClient(config);

        boolean success = false;

        for (int i = 0; i < repeat; ++i) {

            success = false;

            for (int j = 0; j <= retry; ++j) {
                try {
                    client.sendir(uuid, code.nextCode());
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
