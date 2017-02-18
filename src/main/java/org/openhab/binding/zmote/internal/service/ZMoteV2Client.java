package org.openhab.binding.zmote.internal.service;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.zmote.internal.exception.CommunicationException;
import org.openhab.binding.zmote.internal.exception.DeviceBusyException;
import org.openhab.binding.zmote.internal.exception.ZMoteBindingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZMoteV2Client implements IZMoteClient {
    private static final String SENDIR_SUCCESS = "completeir";
    private static final String SENDIR_BUSY = "busyIR";
    private static final String SENDIR_ERROR = "error";

    private final Logger logger = LoggerFactory.getLogger(ZMoteV2Client.class);

    private final HttpClient httpClient = new HttpClient();
    private final String baseUrl;
    private final int timeout;

    public ZMoteV2Client(final String baseUrl, final int timeout) {
        this.baseUrl = baseUrl;
        this.timeout = timeout;

    }

    @Override
    public void dispose() {
        try {
            httpClient.stop();
        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to dispose ZMote client!", e);
            }
        }
    }

    @Override
    public void initialize() {
        try {
            httpClient.start();
        } catch (final Exception e) {
            throw new ZMoteBindingException("Failed to initialize HTTP client!", e);
        }
    }

    @Override
    public synchronized void sendir(final String uuid, final String code) {
        try {
            final String url = String.format("%s/v2/%s", baseUrl, uuid);
            final String msg = String.format("sendir,1:1,0,%s", code);

            final ContentResponse response = httpClient.POST(url).content(new StringContentProvider(msg), "text/plain")
                    .timeout(timeout, TimeUnit.SECONDS).send();

            parseSendirResponse(response);

        } catch (final CommunicationException e) {
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
