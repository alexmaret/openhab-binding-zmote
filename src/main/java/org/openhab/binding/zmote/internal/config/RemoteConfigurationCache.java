package org.openhab.binding.zmote.internal.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zmote.internal.model.Button;
import org.openhab.binding.zmote.internal.model.Remote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteConfigurationCache {

    private final Logger logger = LoggerFactory.getLogger(RemoteConfigurationCache.class);
    private final Map<String, Button> buttonCache = new HashMap<>();
    private RemoteConfiguration remoteConfiguration;

    public RemoteConfigurationCache(final RemoteConfiguration remoteConfiguration) {
        setRemoteConfiguration0(remoteConfiguration);
    }

    public Button getButton(final String name) {
        updateCache();
        return buttonCache.get(name);
    }

    public void setRemoteConfiguration(final RemoteConfiguration remoteConfiguration) {
        setRemoteConfiguration0(remoteConfiguration);
    }

    private void setRemoteConfiguration0(final RemoteConfiguration remoteConfiguration) {
        this.remoteConfiguration = remoteConfiguration;
        updateCache();
    }

    private void updateCache() {
        if (remoteConfiguration == null) {
            updateCache(null);
            return;
        }

        if (!remoteConfiguration.isModified()) {
            return; // nothing to update
        }

        final Remote remote = remoteConfiguration.read();

        if (remote == null) {
            return; // failed to read file
        }

        final String filePath = remoteConfiguration.getFile().getAbsolutePath();

        if (logger.isDebugEnabled()) {
            logger.debug("Reading buttons from file {}.", filePath);
        }

        final Collection<Button> buttons = remote.getButtons();

        if ((buttons == null) || buttons.isEmpty()) {
            final String errorMsg = String.format("Configuration '%s' does not contain any buttons!", filePath);
            logger.error(errorMsg);
        }

        updateCache(buttons);
    }

    private void updateCache(final Collection<Button> buttons) {
        buttonCache.clear();

        if (buttons == null) {
            return;
        }

        for (final Button button : buttons) {
            final String name = button.getKey();
            final String code = button.getCode();

            if ((name == null) || name.isEmpty() || (code == null) || code.isEmpty()) {
                if (logger.isErrorEnabled()) {
                    logger.error("Skipping invalid button {} with code {}!", name, code);
                }
                continue;
            }

            buttonCache.put(name, button);
        }
    }
}
