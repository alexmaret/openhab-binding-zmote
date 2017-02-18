package org.openhab.binding.zmote.internal.util;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.zmote.internal.exception.ConfigurationException;

public class ConfigurationUtils {

    private ConfigurationUtils() {
        // hidden constructor
    }

    public static Object getValue(final Thing thing, final String key, final boolean isRequired) {
        final Object value = thing.getConfiguration().get(key);

        if (value == null) {
            if (!isRequired) {
                return null;
            }

            final String errorMsg = String.format("The configuration parameter '%s' is required!", key);
            throw new ConfigurationException(errorMsg);
        }

        return value;
    }

    public static String getValueAsString(final Thing thing, final String key, final boolean isRequired) {
        final Object value = getValue(thing, key, isRequired);

        if (value == null) {
            return null;
        }

        if (!(value instanceof String)) {
            final String errorMsg = String.format("The configuration parameter '%s' needs to be a string!", key);
            throw new ConfigurationException(errorMsg);
        }

        final String strValue = (String) value;

        if (strValue.isEmpty()) {
            if (!isRequired) {
                return null;
            }

            final String errorMsg = String.format("The configuration parameter '%s' cannot be empty!", key);
            throw new ConfigurationException(errorMsg);
        }

        return strValue;
    }
}
