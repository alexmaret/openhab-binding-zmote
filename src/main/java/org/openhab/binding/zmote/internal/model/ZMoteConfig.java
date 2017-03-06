/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.internal.model;

import java.math.BigDecimal;

/**
 * The configuration of a ZMote thing.
 *
 * @author Alexander Maret-Huskinson - Initial contribution
 */
public class ZMoteConfig {

    String autoUrl;
    String configFile;
    String overrideUrl;
    BigDecimal retry;
    BigDecimal timeout;
    String uuid;

    /**
     * Returns the URL which was discovered using auto-discovery.
     * This URL has to be set manually as it is not part of the Thing
     * configuration and will not be set automatically.
     *
     * @return The auto-discovery URL or null.
     */
    public String getAutoUrl() {
        return autoUrl;
    }

    public void setAutoUrl(final String autoUrl) {
        this.autoUrl = autoUrl;
    }

    /**
     * @return The configuration file path as it is set in the thing's configuration.
     */
    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(final String configFile) {
        this.configFile = configFile;
    }

    /**
     * @return The override URL as it is set in the thing's configuration.
     */
    public String getOverrideUrl() {
        return overrideUrl;
    }

    public void setOverrideUrl(final String overrideUrl) {
        this.overrideUrl = overrideUrl;
    }

    /**
     * @return The number of retries as set in the thing's configuration.
     */
    public BigDecimal getRetry() {
        return retry;
    }

    public void setRetry(final BigDecimal retry) {
        this.retry = retry;
    }

    /**
     * @return The connection timeout as set in the thing's configuration.
     */
    public BigDecimal getTimeout() {
        return timeout;
    }

    public void setTimeout(final BigDecimal timeout) {
        this.timeout = timeout;
    }

    /**
     * @return The override URL if it is set, else the auto-discovery URL.
     */
    public String getUrl() {
        if ((overrideUrl != null) && !overrideUrl.isEmpty()) {
            return overrideUrl;
        }
        return autoUrl;
    }

    /**
     * @return The UUID as set in the thing's configuration.
     */
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
