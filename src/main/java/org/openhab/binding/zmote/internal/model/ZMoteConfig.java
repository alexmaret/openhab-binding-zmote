package org.openhab.binding.zmote.internal.model;

import java.math.BigDecimal;

/**
 * The configuration of a ZMote thing.
 */
public class ZMoteConfig {

    String configFile;
    BigDecimal retry;
    BigDecimal timeout;
    String url;
    String uuid;

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(final String configFile) {
        this.configFile = configFile;
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
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
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
        stringBuilder.append(" url=\"").append(url).append("\",");
        stringBuilder.append(" configFile=\"").append(configFile).append("\",");
        stringBuilder.append(" retry=\"").append(retry).append("\"");
        stringBuilder.append(" timeout=\"").append(timeout).append("\"");
        stringBuilder.append("]");

        return stringBuilder.toString();
    }
}
