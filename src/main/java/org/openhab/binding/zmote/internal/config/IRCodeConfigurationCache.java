package org.openhab.binding.zmote.internal.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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

        final String buttonKey = StringUtils.trimToNull(button);

        if (buttonKey == null) {
            throw new IllegalArgumentException("A button cannot be null or empty!");
        }

        updateCache();

        return codeCache.get(buttonKey.toLowerCase(Locale.getDefault()));
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
            final String name = StringUtils.trimToNull(button.getKey());
            final String code = StringUtils.trimToNull(button.getCode());
            final String tcode = StringUtils.trimToNull(button.getTcode());

            if ((name == null) || (code == null)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Skipping invalid button {} with code {} from configuration file!", name, code);
                }
                continue;
            }

            codeCache.put(name.toLowerCase(Locale.getDefault()), new IRCode(code, tcode));
        }

        return wasEmpty ? !codeCache.isEmpty() : true;
    }
}
