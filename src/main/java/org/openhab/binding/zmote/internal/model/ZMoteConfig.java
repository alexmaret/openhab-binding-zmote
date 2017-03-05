package org.openhab.binding.zmote.internal.model;

import java.math.BigDecimal;

/**
 * The configuration of a ZMote thing.
 */
public class ZMoteConfig {

    String autoUrl;
    String configFile;
    String overrideUrl;
    BigDecimal retry;
    BigDecimal timeout;
    String uuid;

    public String getAutoUrl() {
        return autoUrl;
    }

    public void setAutoUrl(final String autoUrl) {
        this.autoUrl = autoUrl;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(final String configFile) {
        this.configFile = configFile;
    }

    public String getOverrideUrl() {
        return overrideUrl;
    }

    public void setOverrideUrl(final String overrideUrl) {
        this.overrideUrl = overrideUrl;
    }

    public BigDecimal getRetry() {
        return retry;
    }

    public void setRetry(final BigDecimal retry) {
        this.retry = retry;
    }

    public BigDecimal getTimeout() {
        return timeout;
    }

    public void setTimeout(final BigDecimal timeout) {
        this.timeout = timeout;
    }

    public String getUrl() {
        if ((overrideUrl != null) && !overrideUrl.isEmpty()) {
            return overrideUrl;
        }
        return autoUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("ZMoteConfig [");
        stringBuilder.append(" uuid=\"").append(uuid).append("\",");
        stringBuilder.append(" autoUrl=\"").append(autoUrl).append("\",");
        stringBuilder.append(" overrideUrl=\"").append(overrideUrl).append("\",");
        stringBuilder.append(" configFile=\"").append(configFile).append("\",");
        stringBuilder.append(" retry=\"").append(retry).append("\"");
        stringBuilder.append(" timeout=\"").append(timeout).append("\"");
        stringBuilder.append("]");

        return stringBuilder.toString();
    }
}
