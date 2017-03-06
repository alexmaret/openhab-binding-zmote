/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.internal.service;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.zmote.internal.exception.CommunicationException;
import org.openhab.binding.zmote.internal.exception.ConfigurationException;
import org.openhab.binding.zmote.internal.exception.DeviceBusyException;
import org.openhab.binding.zmote.internal.exception.ZMoteBindingException;

/**
 * @author Alexander Maret-Huskinson - Initial contribution
 */
public class ZMoteV2Client implements IZMoteClient {
    private static final String SENDIR_SUCCESS = "completeir";
    private static final String SENDIR_BUSY = "busyIR";
    private static final String SENDIR_ERROR = "error";

    // private final Logger logger = LoggerFactory.getLogger(ZMoteV2Client.class);

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String uuid;

    public ZMoteV2Client(final HttpClient httpClient, final String baseUrl, final String uuid) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.uuid = uuid;
    }

    @Override
    public synchronized void check(final int timeout) {
        try {
            final String url = String.format("%s/uuid", baseUrl, uuid);
            final ContentResponse response = httpClient.GET(url).getRequest().timeout(timeout, TimeUnit.SECONDS).send();
            final String actualContent = response.getContentAsString();
            final String expectedContent = String.format("uuid,%s", uuid).toLowerCase(Locale.ENGLISH);

            if (actualContent == null) {
                throw new CommunicationException("No response received from device!");
            }

            if (!actualContent.toLowerCase(Locale.ENGLISH).startsWith(expectedContent)) {
                throw new ConfigurationException(String
                        .format("The device URL '%s' does not point to a ZMote device with UUID '%s'!", baseUrl, uuid));
            }

        } catch (final ZMoteBindingException e) {
            throw e;

        } catch (final Exception e) {
            final String errorMsg = String.format("Failed to validate UUID from device '%s'!", uuid);
            throw new CommunicationException(errorMsg, e);
        }
    }

    @Override
    public String getUrl() {
        return baseUrl;
    }

    @Override
    public synchronized void sendir(final String code, final int timeout) {
        try {
            final String url = String.format("%s/v2/%s", baseUrl, uuid);
            final String msg = String.format("sendir,1:1,0,%s", code);

            final ContentResponse response = httpClient.POST(url).content(new StringContentProvider(msg), "text/plain")
                    .timeout(timeout, TimeUnit.SECONDS).send();

            parseSendirResponse(response);

        } catch (final ZMoteBindingException e) {
            throw e;

        } catch (final Exception e) {
            final String errorMsg = String.format("Failed to send IR code '%s' to device '%s'!", code, uuid);
            throw new CommunicationException(errorMsg, e);
        }
    }

    private void parseSendirResponse(final ContentResponse response) {

        final String responseContent = response.getContentAsString();

        if ((responseContent == null) || responseContent.isEmpty()) {
            throw new CommunicationException("Empty response received!");
        }

        if (responseContent.startsWith(SENDIR_SUCCESS)) {
            return;
        }

        if (responseContent.startsWith(SENDIR_BUSY)) {
            throw new DeviceBusyException("Device is busy!");
        }

        if ((response.getStatus() != HttpStatus.OK_200) || responseContent.startsWith(SENDIR_ERROR)) {
            throw new CommunicationException(String.format("Failed to send IR code: %s", responseContent));
        }
    }
}
