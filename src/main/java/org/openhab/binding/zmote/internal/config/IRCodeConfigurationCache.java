package org.openhab.binding.zmote.internal.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zmote.internal.exception.ConfigurationException;
import org.openhab.binding.zmote.internal.model.Button;
import org.openhab.binding.zmote.internal.model.IRCode;
import org.openhab.binding.zmote.internal.model.Remote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IRCodeConfigurationCache {

    private final Logger logger = LoggerFactory.getLogger(IRCodeConfigurationCache.class);
    private final Map<String, IRCode> codeCache = new HashMap<>();
    private final RemoteConfiguration remoteConfiguration;

    public IRCodeConfigurationCache(final RemoteConfiguration remoteConfiguration) {
        if (remoteConfiguration == null) {
            throw new IllegalArgumentException("Remote configuration cannot be null!");
        }

        this.remoteConfiguration = remoteConfiguration;
        updateCache();
    }

    public IRCode getCode(final String button) {
        updateCache();
        return codeCache.get(button);
    }

    private boolean updateCache() {

        if (!remoteConfiguration.isModified()) {
            return false; // unchanged
        }

        final String filePath = remoteConfiguration.getFile().getAbsolutePath();
        final Remote remote = remoteConfiguration.read();

        if (remote == null) {
            updateCodes(null);
            throw new ConfigurationException(
                    String.format("The configuration file '%s' contains invalid data and cannot be read!", filePath));
        }

        final Collection<Button> buttons = remote.getButtons();

        if ((buttons == null) || buttons.isEmpty()) {
            final String errorMsg = String.format("Configuration '%s' does not contain any buttons!", filePath);
            logger.error(errorMsg);
        }

        return updateCodes(buttons);
    }

    private boolean updateCodes(final Collection<Button> buttons) {

        final boolean wasEmpty = codeCache.isEmpty();

        codeCache.clear();

        if (buttons == null) {
            return wasEmpty; // code cache unchanged if it was empty before
        }

        for (final Button button : buttons) {
            final String name = cleanupValue(button.getKey());
            final String code = cleanupValue(button.getCode());
            final String tcode = cleanupValue(button.getTcode());

            if ((name == null) || (code == null)) {
                if (logger.isErrorEnabled()) {
                    logger.error("Skipping invalid button {} with code {}!", name, code);
                }
                continue;
            }

            codeCache.put(name, new IRCode(code, tcode));
        }

        return wasEmpty ? !codeCache.isEmpty() : true;
    }

    private String cleanupValue(final String value) {

        String result = value;

        if (result != null) {
            result = result.trim();

            if (result.isEmpty()) {
                result = null;
            }
        }

        return result;
    }
}
