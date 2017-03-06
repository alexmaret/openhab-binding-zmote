/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.internal.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.openhab.binding.zmote.internal.exception.ConfigurationException;
import org.openhab.binding.zmote.internal.model.Remote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * A remote configuration file reader, that can read remote configuration files
 * in JSON format, as provided by the ZMote web app.
 *
 * @author Alexander Maret-Huskinson - Initial contribution
 */
public class RemoteConfiguration {

    private final Logger logger = LoggerFactory.getLogger(RemoteConfiguration.class);
    private final File file;
    private long lastModified = Long.MIN_VALUE;

    /**
     * Creates a new remote configuration reader.
     *
     * @param file An existing file.
     */
    public RemoteConfiguration(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null!");
        }

        if (!file.exists() || !file.canRead()) {
            throw new ConfigurationException(String.format("Configuration file '%s' does not exist or is not readable!",
                    file.getAbsolutePath()));
        }

        this.file = file;
    }

    /**
     * @return The file managed by this class.
     */
    public File getFile() {
        return file;
    }

    /**
     * Checks if the file has been modified since it was last read.
     *
     * @return True if the file was modified else false.
     */
    public boolean isModified() {
        try {
            return (file.lastModified() > lastModified);

        } catch (final SecurityException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Access to configuration file '{}' has been denied! Make sure it is readable by the openhab user.",
                        file.getAbsolutePath(), e);
            }
            return false;
        }
    }

    /**
     * Reads the configuration file.
     *
     * @return The remote configuration or null if the file does not exist or is invalid.
     */
    public Remote read() {

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(file));
            updateLastModified();
            final Gson gson = new Gson();
            return gson.fromJson(br, Remote.class);

        } catch (final Exception e) {
            final String errorMsg = String.format(
                    "Configuration file '%s' could not be read! Make sure it has the correct format and can be read by the openhab user.",
                    file.getAbsolutePath());
            logger.error(errorMsg, e);
            clearLastModified();
            throw new ConfigurationException(errorMsg, e);

        } finally {
            safeClose(br);
        }
    }

    private void safeClose(final Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }

        } catch (final Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Ignored exception while safe-closing file {}.", file.getAbsolutePath(), e);
            }
        }
    }

    private void clearLastModified() {
        lastModified = Long.MIN_VALUE;
    }

    private void updateLastModified() {
        try {
            lastModified = file.lastModified();

        } catch (final SecurityException e) {
            lastModified = Long.MIN_VALUE;

            if (logger.isWarnEnabled()) {
                logger.warn("Access denied for configuration file '{}'!", file.getAbsolutePath());
            }
        }
    }
}
